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

import com.thesett.aima.state.StorageType;

/**
 * Relationship describes a relationship between components. This describes one direction of the relationship rooted on
 * some component/field. If a relationship is bi-directional the other end of it will be held against the target
 * component/field.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> Describe one direction of a relationship. </td></tr>
 * </table></pre>
 */
public class Relationship
{
    public enum Arity
    {
        One, Many
    }

    /** The target component name. */
    private final String target;

    /** The target field name within the target component. */
    private final String targetFieldName;

    /** <tt>true</tt> iff the relationship is bi-drectional. */
    private final boolean biDirectional;

    /** The arity at this end. */
    private final Arity from;

    /** The arity at the target end. */
    private final Arity to;

    /** <tt>true</tt> iff this end is the 'owner' of the relationship. */
    private final boolean owner;

    /** A name for this relationship. */
    private final String name;

    /** The storage type for the relationship. */
    private final StorageType storageType;

    public Relationship(String target, String targetFieldName, boolean biDirectional, Arity arityFrom, Arity arityTo,
        boolean owner, String name, StorageType storageType)
    {
        this.target = target;
        this.targetFieldName = targetFieldName;
        this.biDirectional = biDirectional;
        this.from = arityFrom;
        this.to = arityTo;
        this.owner = owner;
        this.name = name;
        this.storageType = storageType;
    }

    public String getTarget()
    {
        return target;
    }

    public String getTargetFieldName()
    {
        return targetFieldName;
    }

    public boolean isBiDirectional()
    {
        return biDirectional;
    }

    public Arity getFrom()
    {
        return from;
    }

    public Arity getTo()
    {
        return to;
    }

    public boolean isFromOne()
    {
        return Arity.One.equals(from);
    }

    public boolean isFromMany()
    {
        return Arity.Many.equals(from);
    }

    public boolean isToOne()
    {
        return Arity.One.equals(to);
    }

    public boolean isToMany()
    {
        return Arity.Many.equals(to);
    }

    public boolean isOwner()
    {
        return owner;
    }

    public String getName()
    {
        return name;
    }
}
