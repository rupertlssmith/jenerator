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
package com.thesett.catalogue.generator;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.thesett.aima.attribute.impl.EnumeratedStringAttribute;
import com.thesett.aima.state.Type;

/**
 * HierarchyTypeDecorator decorates a hierarchy type, exposing all of the available methods on hierarchy types in the
 * decorated type.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Decorate a hierarchy type.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class EnumeratedStringTypeDecorator extends TypeDecorator<EnumeratedStringAttribute>
    implements EnumeratedStringAttribute.EnumeratedStringType
{
    /**
     * Creates a decorator on a hierarchy type, that exposes the methods of hierarchy types as a decorated type.
     *
     * @param enumeratedStringAttributeType The hierarchy type to decorate.
     */
    public EnumeratedStringTypeDecorator(Type<EnumeratedStringAttribute> enumeratedStringAttributeType)
    {
        super(enumeratedStringAttributeType);
    }

    /** {@inheritDoc} */
    public Iterator<EnumeratedStringAttribute> getAllPossibleValuesIterator(boolean failOnNonFinalized)
    {
        return ((EnumeratedStringAttribute.EnumeratedStringType) type).getAllPossibleValuesIterator(failOnNonFinalized);
    }

    /** {@inheritDoc} */
    public Set<EnumeratedStringAttribute> getAllPossibleValuesSet(boolean failOnNonFinalized)
    {
        return ((EnumeratedStringAttribute.EnumeratedStringType) type).getAllPossibleValuesSet(failOnNonFinalized);
    }

    /** {@inheritDoc} */
    public Map<String, EnumeratedStringAttribute> getAllPossibleValuesMap(boolean failOnNonFinalized)
    {
        return ((EnumeratedStringAttribute.EnumeratedStringType) type).getAllPossibleValuesMap(failOnNonFinalized);
    }

    /**
     * Provides all of the pre-defined labels in the this enumeration type, whether it is finalized or not.
     *
     * @return A map of name/value pairs.
     */
    public Map<String, String> getLabels()
    {
        Map<String, String> result = new LinkedHashMap<String, String>();

        for (EnumeratedStringAttribute attribute : getAllPossibleValuesSet(false))
        {
            result.put(attribute.getStringValue(), attribute.getStringValue());
        }

        return result;
    }
}
