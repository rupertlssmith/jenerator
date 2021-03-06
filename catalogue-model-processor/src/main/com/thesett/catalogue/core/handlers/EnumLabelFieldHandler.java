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
package com.thesett.catalogue.core.handlers;

import java.util.Iterator;
import java.util.List;

import com.thesett.catalogue.core.FieldHandler;
import com.thesett.catalogue.setup.LabelType;

/**
 * EnumLabelFieldHandler transforms 'label' fields into a labels/1 functor, that holds a list of enumeration labels
 * taken from the fields value, as its argument.
 *
 * <p/>
 * <pre><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Transform an enumeration type declarations labels into a list. <td> {@link LabelType}.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class EnumLabelFieldHandler implements FieldHandler
{
    /**
     * {@inheritDoc}
     *
     * <p/>This transformation expects a list of {@link LabelType}s as the fields value, and transforms these into a
     * recursive list. This transformation is only applied to 'label' fields.
     */
    public String handleField(String property, Object value, boolean more)
    {
        if ("label".equals(property))
        {
            // Convert the property value to a list of labels.
            List<LabelType> labels = (List<LabelType>) value;

            String result = "labels([";

            for (Iterator<LabelType> i = labels.iterator(); i.hasNext();)
            {
                LabelType label = i.next();
                result += label.getName() + (i.hasNext() ? ", " : "");
            }

            result += "])" + (more ? ", " : "");

            return result;
        }

        return null;
    }
}
