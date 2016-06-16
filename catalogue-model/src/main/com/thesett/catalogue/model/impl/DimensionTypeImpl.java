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
package com.thesett.catalogue.model.impl;

import java.util.Map;
import java.util.Set;

import com.thesett.aima.state.ComponentType;
import com.thesett.aima.state.Type;
import com.thesett.catalogue.model.DimensionType;

/**
 * DimensionTypeImpl is the type of a dimension, which is an entity, that has a secondary storage representation in a
 * data warehouse.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> None yet.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class DimensionTypeImpl extends EntityTypeImpl implements DimensionType
{
    /**
     * Creates a dimension with the specified name, set of fields and implementing class.
     *
     * @param name                 The name of the entity.
     * @param attributes           The fields of the entity.
     * @param presentAsAliases     A map from names to externally presented names, if defined.
     * @param naturalKeyFields     The set of fields forming the natural key of the component.
     * @param operationalClassName An implementing class.
     * @param immediateAncestors   The immediate ancestors of this type.
     */
    public DimensionTypeImpl(String name, Map<String, Type> attributes, Map<String, String> presentAsAliases,
        Set<String> naturalKeyFields, String operationalClassName, Set<ComponentType> immediateAncestors)
    {
        super(name, attributes, presentAsAliases, naturalKeyFields, operationalClassName, immediateAncestors);
    }
}
