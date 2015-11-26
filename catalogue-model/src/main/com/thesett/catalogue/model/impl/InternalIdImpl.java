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

import java.io.Serializable;

import com.thesett.catalogue.model.InternalId;

/**
 * Implements entity ids as longs.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Uniquely identify entities.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class InternalIdImpl implements InternalId, Serializable
{
    /** The actual id. */
    private final long id;

    /**
     * Creates an internal id for the specified value.
     *
     * @param id The id value.
     */
    public InternalIdImpl(long id)
    {
        this.id = id;
    }

    /**
     * Compares this id to another for equality.
     *
     * @param  o The object to compare to.
     *
     * @return <tt>true</tt> if the comparator is also an internal id the same as this one.
     */
    public boolean equals(Object o)
    {
        return (o instanceof InternalIdImpl) && (((InternalIdImpl) o).id == id);
    }

    /**
     * Generates a hash code that conforms with the equality method.
     *
     * @return A hash code that conforms with the equality method.
     */
    public int hashCode()
    {
        return Long.valueOf(id).hashCode();
    }

    /**
     * Returns the value of the id.
     *
     * @return The value of the id.
     */
    public long getValue()
    {
        return id;
    }

    /**
     * Renders the id as a string, mainly for debugging purposes.
     *
     * @return The id as a string.
     */
    public String toString()
    {
        return "InternalIdImpl: [ id = " + id + " ]";
    }
}
