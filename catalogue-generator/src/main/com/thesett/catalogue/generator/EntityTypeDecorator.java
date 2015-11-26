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

import java.util.Map;

import com.thesett.aima.state.ComponentType;
import com.thesett.catalogue.model.EntityType;
import com.thesett.catalogue.model.impl.Relationship;

/**
 * EntityTypeDecorator is a {@link TypeDecorator} for {@link com.thesett.catalogue.model.EntityType}s. It automatically
 * decorates the types of any fields of the entity that are accessed through it, and provides all of the underlying
 * properties of the decorated entity.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Decorate a component type, and the types of all of its fields.
 *     <td> {@link TypeDecoratorFactory}, {@link com.thesett.aima.state.ComponentType}.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class EntityTypeDecorator extends ComponentTypeDecorator implements EntityType
{
    /**
     * Creates a type decorator for an entity type, that returns decorated types for all reachable child types on all of
     * the fields of the entity.
     *
     * @param type The entity type to decorate.
     */
    public EntityTypeDecorator(ComponentType type)
    {
        super(type);
    }

    /** {@inheritDoc} */
    public boolean isExternalId()
    {
        return ((EntityType) type).isExternalId();
    }

    /** {@inheritDoc} */
    public Map<String, Relationship> getRelationships()
    {
        return ((EntityType) type).getRelationships();
    }
}
