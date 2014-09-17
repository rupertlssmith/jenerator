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

import java.util.List;

import com.thesett.aima.logic.fol.Clause;
import com.thesett.aima.logic.fol.Parser;
import com.thesett.aima.logic.fol.Sentence;
import com.thesett.aima.logic.fol.isoprologparser.Token;
import com.thesett.catalogue.setup.CatalogueDefinition;
import com.thesett.catalogue.setup.TypeDefType;

/**
 * Extracts a catalogue model as facts in first order logic, from a raw catalogue model.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> Transform the raw catalogue model into first order logic. </td></tr>
 * </table></pre>
 */
public interface ModelTermBuilder
{
    /**
     * Converts types extracted from the raw catalogue model into first order logic clauses that encapsulate all the
     * parameters that make up the type. For each type definition in the catalogue model, the fields are extracted and
     * passed through the custom handlers in turn, until one of them applies a transformation to the field. If none of
     * the custom handlers applies a transformation, then the
     * {@link com.thesett.catalogue.core.handlers.DefaultFieldHandler} is used to transform the field into a name(value)
     * functor.
     *
     * @param <T>            The type of types to extract.
     * @param catalogueDef   The catalogue definition to extract types from.
     * @param parser         The parser to parser terms with.
     * @param clauses        The list of clauses to accumulate parsed clauses in.
     * @param typeClass      The class of types to extract.
     * @param properties     The properties to extract from the type bean.
     * @param customHandlers A chain of custom handlers to apply to fields of type definitions.
     */
    <T extends TypeDefType> void convertTypeToTerm(CatalogueDefinition catalogueDef, Parser<Clause, Token> parser,
        List<Sentence<Clause>> clauses, Class<T> typeClass, String[] properties, FieldHandler... customHandlers);
}
