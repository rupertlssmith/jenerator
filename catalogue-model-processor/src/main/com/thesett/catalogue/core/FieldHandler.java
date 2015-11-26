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

/**
 * A FieldHandler defines an optional transformation that can be applied to the fields of a type declaration in the
 * model, in order to assist with transforming it into first order logic. The transformations applied are <tt>
 * String</tt> to <tt>String</tt> mappings, and if a handler does not need to process a particular field it may ignore
 * it with a <tt>null</tt> mapping.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities
 * <tr><td> Apply a string to string mapping to a type declaration field.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public interface FieldHandler
{
    /**
     * Applies a <tt>String</tt> to <tt>String</tt> mapping to the specified type declaration field, as a named
     * property/value pair. If the handler does not wish to apply any mapping it should map the input onto <tt>
     * null</tt>.
     *
     * @param  property The name of the field to map.
     * @param  value    The value of the field.
     * @param  more     If the field is one of a sequence and there are more in the sequence.
     *
     * @return A transformed field, or <tt>null</tt> to apply no transformation.
     */
    String handleField(String property, Object value, boolean more);
}
