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

import java.util.Map;
import java.util.Set;

import com.thesett.aima.state.ComponentType;
import com.thesett.aima.state.Type;
import com.thesett.catalogue.model.ViewType;

/**
 * ViewTypeImpl is the type of view components. A view component is a sub-set of the fields of a component, with
 * identical types of the fields in the subset to their corresponding types in a larger component. In this way a
 * component that 'conforms' to the view is a sub-type of it, and a restricted view of the component may be accessed
 * through a particular view. A view is analogous to an interface in Java.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> None yet.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class ViewTypeImpl extends ComponentTypeImpl implements ViewType
{
    /**
     * Creates a named view with the specified set of fields and underlying implementation.
     *
     * @param name                 The name of the view.
     * @param attributes           The fields of the view.
     * @param presentAsAliases     A map from names to externally presented names, if defined.
     * @param naturalKeyFields     The set of fields forming the natural key of the component.
     * @param operationalClassName An implementing class.
     * @param immediateAncestors   The immediate ancestors of this type.
     */
    public ViewTypeImpl(String name, Map<String, Type> attributes, Map<String, String> presentAsAliases,
        Set<String> naturalKeyFields, String operationalClassName, Set<ComponentType> immediateAncestors)
    {
        super(attributes, presentAsAliases, naturalKeyFields, name, operationalClassName, immediateAncestors);
    }
}
