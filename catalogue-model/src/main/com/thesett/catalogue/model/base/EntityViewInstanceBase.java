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
import com.thesett.aima.state.impl.ExtendableBeanState;
import com.thesett.catalogue.model.ExternalId;
import com.thesett.catalogue.model.InternalId;
import com.thesett.catalogue.model.ViewInstance;
import com.thesett.catalogue.model.impl.InternalIdImpl;
import com.thesett.common.error.NotImplementedException;

/**
 * EntityViewInstanceBase provides a base class for implementing views that are stored as entities.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * </table></pre>
 *
 * @author Rupert Smith
 * @todo   Drop this class, as it was introduced because the type normalization does not yet support automatic promotion
 *         of components or views to entities when needed. Instead work out when views or components require mapping
 *         onto the database and promote them to the required kind of persistent component and use the kinds base class
 *         as the base class for the implementation.
 */
public class EntityViewInstanceBase extends ExtendableBeanState implements ViewInstance, Serializable
{
    /** Holds the name of the dimension that this summary belongs to. */
    protected String entityTypeName;

    /** Holds the database id. */
    protected long id;

    /** Holds the external id. */
    protected ExternalId externalId;

    /** Creates an empty entity view instance. */
    public EntityViewInstanceBase()
    {
    }

    /**
     * Creates an entity view instance.
     *
     * @param entityTypeName The name of the entity type that this view belongs to.
     * @param id             The database id.
     * @param externalId     The external id.
     */
    public EntityViewInstanceBase(String entityTypeName, long id, ExternalId externalId)
    {
        this.entityTypeName = entityTypeName;
        this.id = id;
        this.externalId = externalId;
    }

    /**
     * Gets the id of the element.
     *
     * @return The id of the element.
     */
    public InternalId getOpaqueId()
    {
        Long id = getId();

        if (id == null)
        {
            return null;
        }
        else
        {
            return new InternalIdImpl(id);
        }
    }

    /**
     * Gets the external id of the element.
     *
     * @return The external id of the element.
     */
    public ExternalId getExternalId()
    {
        return externalId;
    }

    /**
     * Gets the name of the dimension type of the element.
     *
     * @return The name of the dimension type of the element.
     */
    public String getComponentTypeName()
    {
        return entityTypeName;
    }

    /** {@inheritDoc} */
    public ComponentType getComponentType()
    {
        throw new NotImplementedException();
    }

    /**
     * Render the dimension element as a string for debugging purposes.
     *
     * @return The dimension element as a string for debugging purposes.
     */
    public String toString()
    {
        return "EntityViewInstanceBase: [ entityTypeName = " + entityTypeName + ", id = " + id + ", externalId = " +
            externalId + " ]";
    }

    /**
     * Gets the real database id. This is protected so as to not expose it. Should use the {@link #getOpaqueId} method
     * instead to get an opaque id.
     *
     * @return The real database id.
     */
    protected Long getId()
    {
        return id;
    }
}
