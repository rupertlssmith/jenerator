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
package com.thesett.catalogue.core.flathandlers;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.thesett.catalogue.core.FieldHandler;

/**
 * InQuotesFieldHandler transforms a matching subset of its input fields into a functors of arity 1. The name of the
 * functor is the name of the field, and the argument is its value as a string literal in quotes.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Transform some type declaration fields into a functor of arity one with string argument.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class FlatInQuotesFieldHandler implements FieldHandler
{
    /** Holds the set of field name to match for transformation. */
    Set<String> propertiesInQuotes = new HashSet<String>();

    /**
     * Creats a new in-quotes handler on the specified set of named fields.
     *
     * @param properties The set of fields to transform.
     */
    public FlatInQuotesFieldHandler(String[] properties)
    {
        Collections.addAll(propertiesInQuotes, properties);
    }

    /**
     * {@inheritDoc}
     *
     * <p/>This mapping transform the property/value pair into a functor, property("value"), optionally adding a
     * continuation ',' if there are more fields in the sequence, so that the sequence may be parsed as a list body.
     */
    public String handleField(String property, Object value, boolean more)
    {
        if (propertiesInQuotes.contains(property))
        {
            return property + "(\"" + value.toString() + "\")\n";
        }

        return null;
    }
}
