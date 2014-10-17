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
package com.thesett.catalogue.model;

import java.io.Serializable;

/**
 * An EntityInstance is a {@link com.thesett.aima.state.State} that is an instance of an {@link EntityType}. In addition
 * to the set of named and typed fields that a state has, all entities have a unique id that identifies them within
 * their realm of persistent storage. For example, the id may be a natural or surragate database key.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Provide an internal storage key to uniquely identify an entity.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public interface EntityInstance<K extends Serializable> extends ComponentInstance
{
    /**
     * Supplies the entities id.
     *
     * @return The entities id.
     */
    K getId();

    /**
     * Establishes the entities id.
     *
     * @param id The entities id.
     */
    void setId(K id);

    /** {@inheritDoc} */
    EntityType getComponentType();
}
