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
package com.thesett.catalogue.model.base;

import java.io.Serializable;

import com.thesett.aima.state.ComponentType;
import com.thesett.catalogue.model.EntityInstance;
import com.thesett.catalogue.model.EntityType;

/**
 * EntityInstanceBase provides a base class for implementing entity instances.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Provide an opaque internal storage key to uniquely identify an entity. <td> {@link com.thesett.catalogue.model.impl.InternalIdImpl}
 * </table></pre>
 *
 * @author Rupert Smith
 */
public abstract class EntityInstanceBase extends ComponentInstanceBase implements EntityInstance<Long>, Serializable
{
    /** {@inheritDoc} */
    public abstract EntityType getComponentType();

    /** {@inheritDoc} */
    public String getComponentTypeName()
    {
        ComponentType type = getComponentType();

        if (type == null)
        {
            return null;
        }
        else
        {
            return type.getName();
        }
    }
}
