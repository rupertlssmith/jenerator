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

import com.thesett.catalogue.core.FieldHandler;

/**
 * DocRootHandler transforms the optional 'root' attribute into an root/0 constant, indicating that a component should
 * be considered to be at the root of a document tree.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Transform a root attribute into a root/0 constant.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class DocRootHandler implements FieldHandler
{
    /**
     * {@inheritDoc}
     *
     * <p/>This transformation expects a list of {@link com.thesett.catalogue.setup.View}s as the fields argument and
     * transforms these into a recursive list. This transformation only applies to 'view' fields.
     */
    public String handleField(String property, Object value, boolean more)
    {
        if ("externalId".equals(property))
        {
            // Cast the field value to a list of views.
            Boolean isRoot = (Boolean) value;

            if (isRoot != null)
            {
                return "root";
            }
        }

        return null;
    }
}
