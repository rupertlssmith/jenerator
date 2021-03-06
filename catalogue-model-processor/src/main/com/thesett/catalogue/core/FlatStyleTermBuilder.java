/*
 * Copyright The Sett Ltd, 2005 to 2014.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.thesett.catalogue.core;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.thesett.aima.logic.fol.Clause;
import com.thesett.aima.logic.fol.Parser;
import com.thesett.aima.logic.fol.Sentence;
import com.thesett.aima.logic.fol.interpreter.ResolutionEngine;
import com.thesett.aima.logic.fol.isoprologparser.Token;
import com.thesett.aima.logic.fol.prolog.PrologCompiledClause;
import com.thesett.aima.state.impl.WrappedBeanState;
import com.thesett.catalogue.core.handlers.DefaultFieldHandler;
import com.thesett.catalogue.setup.CatalogueDefinition;
import com.thesett.catalogue.setup.SetupModelHelper;
import com.thesett.catalogue.setup.TypeDefType;
import com.thesett.common.util.maps.HashArray;

/**
 * FlatStyleTermBuilder produces terms as fragments of an AST, that link to each other by reference. This results in
 * several simple terms for each type definition. This is generally easier to query on than the list style terms, which
 * require predicates to navigate into the lists to extract facts that are more easily exposed as top-level facts.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> Transform the raw catalogue model into first order logic list style. </td></tr>
 * </table></pre>
 */
public class FlatStyleTermBuilder extends BaseTermBuilder
{
    /** Used for debugging purposes. */
    private static final Logger log = Logger.getLogger(FlatStyleTermBuilder.class.getName());

    /**
     * Creates a model term builder.
     *
     * @param engine      The Prolog engine to generate the terms for.
     * @param modelWriter An optional writer to output a copy of the terms to, may be <tt>null</tt>.
     */
    public FlatStyleTermBuilder(ResolutionEngine<Clause, PrologCompiledClause, PrologCompiledClause> engine,
        Writer modelWriter)
    {
        super(engine, modelWriter);
    }

    /** {@inheritDoc} */
    public <T extends TypeDefType> void convertTypeToTerm(CatalogueDefinition catalogueDef,
        Parser<Clause, Token> parser, List<Sentence<Clause>> clauses, Class<T> typeClass, String[] properties,
        FieldHandler... customHandlers)
    {
        // Create an instance of the default field handler.
        FieldHandler defaultHandler = new DefaultFieldHandler();

        // Loop over all instances of the specified type class found in the model.
        for (TypeDefType type : SetupModelHelper.getAllTypeDefsOfType(typeClass, catalogueDef))
        {
            // Get the name of the type class.
            String kind = engine.getFunctorName(typeClassToAtom(type));

            // If the type is not known then ignore it and move on to the next one.
            if (kind == null)
            {
                continue;
            }

            // Wrap the type as a bean state so that its properties can be extracted by name.
            WrappedBeanState typeBean = new WrappedBeanState(type);

            // Get the name of the type.
            String name = (String) typeBean.getProperty("name");

            // Build the first part of the type instance clause.
            String termText = "type_instance(" + name + ", " + kind + ")\n";

            // Extract all the specified properties from the type bean, filtering out any nulls.
            Map<String, Object> nonNullProperties = new HashArray<String, Object>();

            for (String property : properties)
            {
                if (typeBean.hasProperty(property))
                {
                    Object value = typeBean.getProperty(property);

                    if (value != null)
                    {
                        nonNullProperties.put(property, value);
                    }
                }
            }

            // Build the rest of the type instance clause from the non-null properties.
            for (Iterator<Map.Entry<String, Object>> i = nonNullProperties.entrySet().iterator(); i.hasNext();)
            {
                Map.Entry<String, Object> entry = i.next();
                String property = entry.getKey();
                Object value = entry.getValue();

                // Chain the raw property value down to the custom field handler if one is defined.
                boolean handled = false;

                for (FieldHandler handler : customHandlers)
                {
                    String result = handler.handleField(property, value, i.hasNext());

                    if (result != null)
                    {
                        handled = true;
                        termText += result;

                        // Break out once one handler has responded.
                        break;
                    }
                }

                // If the custom handler did not handle the field then use the default.
                if (!handled)
                {
                    termText += defaultHandler.handleField(property, value, i.hasNext());
                }
            }

            log.fine(termText);

            // CC the raw model text to the specified raw model writer, only if one was set.
            if (modelWriter != null)
            {
                try
                {
                    modelWriter.write(termText + "\n");
                    modelWriter.flush();
                }
                catch (IOException e)
                {
                    throw new IllegalStateException("Error whilst writing out the raw model.", e);
                }
            }

            // Parse the instance clause into a prolog clause and add it to the list of clauses.
            /*parser.setTokenSource(TokenSource.getTokenSourceForString(termText));

            try
            {
                Sentence<Clause> sentence = parser.parse();
                clauses.add(sentence);
            }
            catch (SourceCodeException e)
            {
                throw new IllegalStateException("Badly formed typedef conversion to logical term.", e);
            }*/
        }
    }

}
