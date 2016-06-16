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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.thesett.aima.state.ComponentType;
import com.thesett.aima.state.Type;
import com.thesett.catalogue.model.FactType;

/**
 * FactTypeImpl is the type of a fact table in a data warehouse. A fact in an entity, as it is persisted to a database,
 * and generally speaking facts contain only numerical values or references to dimensions. In addition to numerical
 * values facts may sometimes contain discrete valued fields, for example enumerations. The more general rule is that
 * facts contain data elements that are amenable to aggregation and statistical analysis, at the intersection of
 * instances of one or more dimensions.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> None yet.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class FactTypeImpl extends EntityTypeImpl implements FactType
{
    /**
     * Creates a fact type with the specified name, set of fields and implementing class.
     *
     * @param name                 The name of the fact.
     * @param attributes           The fields of the fact.
     * @param presentAsAliases     A map from names to externally presented names, if defined.
     * @param operationalClassName An implementing class.
     * @param immediateAncestors   The immediate ancestors of this type.
     */
    public FactTypeImpl(String name, Map<String, Type> attributes, Map<String, String> presentAsAliases,
        String operationalClassName, Set<ComponentType> immediateAncestors)
    {
        super(name, attributes, presentAsAliases, new HashSet<String>(), operationalClassName, immediateAncestors);
    }
}
