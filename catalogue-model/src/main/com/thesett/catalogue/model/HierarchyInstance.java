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

import com.thesett.aima.attribute.impl.HierarchyAttribute;
import com.thesett.aima.attribute.impl.HierarchyType;
import com.thesett.aima.state.State;

/**
 * An HierarchyInstance is a {@link com.thesett.aima.state.State} that is an instance of a
 * {@link com.thesett.aima.attribute.impl.HierarchyType}. In addition to the set of named and typed fields that a state
 * has, all hierarchy instances have a unique id that identifies them within their realm of persistent storage.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Provide an opaque internal storage key to uniquely identify a hierarchy instance.
 * <tr><td> Provide the hierarchy type of an instance.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public interface HierarchyInstance extends State
{
    /**
     * Gets the id of the element.
     *
     * @return The id of the element.
     */
    InternalId getOpaqueId();

    /**
     * Provides the hierarchy type of this instance.
     *
     * @return The hierarchy type of this instance.
     */
    HierarchyType getHierarchyType();

    /**
     * Gets the underlying hierarchy attribute that this hierarchy bean wraps.
     *
     * @return The underlying hierarchy attribute that this hierarchy bean wraps.
     */
    HierarchyAttribute getHierarchy();
}
