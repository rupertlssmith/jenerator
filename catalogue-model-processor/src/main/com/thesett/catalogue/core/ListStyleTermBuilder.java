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
import com.thesett.aima.logic.fol.Functor;
import com.thesett.aima.logic.fol.Parser;
import com.thesett.aima.logic.fol.Sentence;
import com.thesett.aima.logic.fol.interpreter.ResolutionEngine;
import com.thesett.aima.logic.fol.isoprologparser.Token;
import com.thesett.aima.logic.fol.isoprologparser.TokenSource;
import com.thesett.aima.logic.fol.prolog.PrologCompiledClause;
import com.thesett.aima.state.impl.WrappedBeanState;
import com.thesett.catalogue.core.handlers.DefaultFieldHandler;
import com.thesett.catalogue.core.handlers.FieldHandler;
import com.thesett.catalogue.setup.CatalogueDefinition;
import com.thesett.catalogue.setup.ComponentDefType;
import com.thesett.catalogue.setup.DateRangeType;
import com.thesett.catalogue.setup.DecimalType;
import com.thesett.catalogue.setup.DimensionDefType;
import com.thesett.catalogue.setup.EntityDefType;
import com.thesett.catalogue.setup.EnumerationDefType;
import com.thesett.catalogue.setup.FactDefType;
import com.thesett.catalogue.setup.HierarchyDefType;
import com.thesett.catalogue.setup.IntegerRangeType;
import com.thesett.catalogue.setup.RealRangeType;
import com.thesett.catalogue.setup.SetupModelHelper;
import com.thesett.catalogue.setup.StringPatternType;
import com.thesett.catalogue.setup.TimeRangeType;
import com.thesett.catalogue.setup.TypeDefType;
import com.thesett.catalogue.setup.ViewDefType;
import com.thesett.common.parsing.SourceCodeException;
import com.thesett.common.util.maps.HashArray;

/**
 * The default term builder, produces terms with each defined type at the top, and the remainder of the AST making up
 * the type held in a structured list below it. Each type maps onto exactly one term.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> Transform the raw catalogue model into first order logic list style. </td></tr>
 * </table></pre>
 */
public class ListStyleTermBuilder implements ModelTermBuilder
{
    /** Used for debugging purposes. */
    private static final Logger log = Logger.getLogger(ListStyleTermBuilder.class.getName());

    /** Holds the prolog engine to process the type checking rules with. */
    private final ResolutionEngine<Clause, PrologCompiledClause, PrologCompiledClause> engine;

    /** Holds an optional writer to write the raw model out to. */
    private final Writer modelWriter;

    /**
     * Creates a model term builder.
     *
     * @param engine      The Prolog engine to generate the terms for.
     * @param modelWriter An optional writer to output a copy of the terms to, may be <tt>null</tt>.
     */
    public ListStyleTermBuilder(ResolutionEngine<Clause, PrologCompiledClause, PrologCompiledClause> engine,
        Writer modelWriter)
    {
        this.engine = engine;
        this.modelWriter = modelWriter;
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
            String termText = "type_instance(" + name + ", " + kind + ", [";

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

            termText += "]).";
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
                    throw new RuntimeException("Error whilst writing out the raw model.", e);
                }
            }

            // Parse the instance clause into a prolog clause and add it to the list of clauses.
            parser.setTokenSource(TokenSource.getTokenSourceForString(termText));

            try
            {
                Sentence<Clause> sentence = parser.parse();
                clauses.add(sentence);
            }
            catch (SourceCodeException e)
            {
                throw new RuntimeException("Badly formed typedef conversion to logical term.", e);
            }
        }
    }

    /**
     * Provides the prolog atom name for the specified type class.
     *
     * @param  type An instance of the type class to get the atom for.
     *
     * @return The atom name of the types representation in prolog.
     */
    private Functor typeClassToAtom(TypeDefType type)
    {
        String kind = null;

        if (type instanceof DecimalType)
        {
            kind = "decimal_type";
        }

        if (type instanceof IntegerRangeType)
        {
            kind = "integer_range";
        }
        else if (type instanceof RealRangeType)
        {
            kind = "real_range";
        }
        else if (type instanceof StringPatternType)
        {
            kind = "string_pattern";
        }
        else if (type instanceof DateRangeType)
        {
            kind = "date_range";
        }
        else if (type instanceof TimeRangeType)
        {
            kind = "time_range";
        }
        else if (type instanceof EnumerationDefType)
        {
            kind = "enumeration_type";
        }
        else if (type instanceof HierarchyDefType)
        {
            kind = "hierarchy_type";
        }
        else if (type instanceof DimensionDefType)
        {
            kind = "dimension_type";
        }
        else if (type instanceof EntityDefType)
        {
            kind = "entity_type";
        }
        else if (type instanceof FactDefType)
        {
            kind = "fact_type";
        }
        else if (type instanceof ViewDefType)
        {
            kind = "view_type";
        }
        else if (type instanceof ComponentDefType)
        {
            kind = "component_type";
        }

        int id = engine.internFunctorName(kind, 0);

        return new Functor(id, null);
    }
}
