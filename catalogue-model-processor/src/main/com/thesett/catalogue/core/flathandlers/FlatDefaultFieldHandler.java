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

import com.thesett.catalogue.core.FieldHandler;

/**
 * DefaultFieldHandler transforms its input field into a functor of arity 1. The name of the functor is the name of the
 * field, and the argument is its value.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Transforms a type declaration field into a functor of arity one.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class FlatDefaultFieldHandler implements FieldHandler
{
    /**
     * {@inheritDoc}
     *
     * <p/>This mapping transform the property/value pair into a functor, property(value), optionally adding a
     * continuation ',' if there are more fields in the sequence, so that the sequence may be parsed as a list body.
     */
    public String handleField(String property, Object value, boolean more)
    {
        return property + "(" + value + ")\n";
    }
}
