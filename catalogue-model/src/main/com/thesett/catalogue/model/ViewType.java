/*
 * Copyright The Sett Ltd.
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
import java.util.Set;

import com.thesett.aima.state.ComponentType;

/**
 * ViewType is a {@link com.thesett.aima.state.Type} which is a {@link ComponentType} defining a set of named and typed
 * fields. A ViewType may provide a sub-set of the fields that a ComponentType exposes, and a ComponentType may be able
 * to be presented as a ViewType which is a sub-set of its fields.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Provide a subset of the types and names of fields that make up a component. </td></tr>
 * <tr><td> Identify all components implementing the view. </td></tr>
 * </table></pre>
 *
 * @author Rupert Smith
 */
public interface ViewType extends ComponentType, Serializable
{
    /**
     * Provides the set of all components that implement this view.
     *
     * @return The set of all components that implement this view.
     */
    Set<ComponentType> getDescendants();

    /**
     * Establishes the set of all components that implement this view.
     *
     * @param descendants The set of all components that implement this view.
     */
    void setDescendants(Set<ComponentType> descendants);
}
