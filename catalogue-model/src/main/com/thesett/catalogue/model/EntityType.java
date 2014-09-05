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

import com.thesett.aima.state.ComponentType;

/**
 * An EntityType is a {@link com.thesett.aima.state.Type} that is a {@link ComponentType} that can be mapped onto
 * persistent and queryable storage, most often a relational database.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Provide the types and names of fields that make up a persistent entity.
 * <tr><td> Indicate whether or not an entity has a long lived external identifier.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public interface EntityType extends ComponentType, Serializable
{
    /**
     * Indicates whether or not the entity has a long lived external identifier.
     *
     * @return <tt>true</tt> if the entity has an external id.
     *
     * @see    ExternalId
     */
    boolean isExternalId();
}
