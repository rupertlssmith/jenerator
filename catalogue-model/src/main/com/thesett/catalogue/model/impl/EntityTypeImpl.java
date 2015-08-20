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
package com.thesett.catalogue.model.impl;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.thesett.aima.state.ComponentType;
import com.thesett.aima.state.Type;
import com.thesett.aima.state.TypeVisitor;
import com.thesett.catalogue.model.EntityType;
import com.thesett.catalogue.model.EntityTypeVisitor;

/**
 * EntityType is the type of an entity, which is a component that can be persisted to a database.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Accept a type visitor, applying it to this if it is an entity visitor.
 *     <td> {@link com.thesett.aima.state.TypeVisitor}, {@link EntityTypeVisitor}
 * <tr><td> Indicate whether or not an entity has a long lived external identifier.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class EntityTypeImpl extends ComponentTypeImpl implements EntityType
{
    /** Flag to indicate when true, that this entity type supports external ids. */
    protected boolean externalIdFlag = false;

    /** Describes relationships roots on fields of this entity. */
    private Map<String, Relationship> relationships = new LinkedHashMap<String, Relationship>();

    /**
     * Creates an entity type with the specified name, fields and implementing class.
     *
     * @param name                 The name of the entity.
     * @param attributes           The fields of the entity.
     * @param presentAsAliases     A map from names to externally presented names, if defined.
     * @param naturalKeyFields     The set of fields forming the natural key of the component.
     * @param operationalClassName An implementing class.
     * @param immediateAncestors   The immediate ancestors of this type.
     */
    public EntityTypeImpl(String name, Map<String, Type> attributes, Map<String, String> presentAsAliases,
        Set<String> naturalKeyFields, String operationalClassName, Set<ComponentType> immediateAncestors)
    {
        super(attributes, presentAsAliases, naturalKeyFields, name, operationalClassName, immediateAncestors);
    }

    /**
     * {@inheritDoc}
     *
     * <p/>If the visitiro is an {@link EntityTypeVisitor}, it is applied to this, otherwise it is chained up to the
     * super visitor method.
     */
    public void acceptVisitor(TypeVisitor visitor)
    {
        if (visitor instanceof EntityTypeVisitor)
        {
            ((EntityTypeVisitor) visitor).visit(this);
        }
        else
        {
            super.acceptVisitor(visitor);
        }
    }

    /** {@inheritDoc} */
    public boolean isExternalId()
    {
        return externalIdFlag;
    }

    /**
     * Sets the external id flag to indicate whether or not entities of this type support external ids.
     *
     * @param externalIdFlag <tt>true</tt> if this entity type supports external ids.
     */
    public void setExternalIdFlag(boolean externalIdFlag)
    {
        this.externalIdFlag = externalIdFlag;
    }

    /** {@inheritDoc} */
    public Map<String, Relationship> getRelationships()
    {
        return relationships;
    }
}
