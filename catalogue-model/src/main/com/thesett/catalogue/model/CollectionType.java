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

import com.thesett.aima.state.Type;

/**
 * A CollectionType is a {@link com.thesett.aima.state.Type} that acts as a container for many instances of another
 * type.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th>Responsibilities <th>Collaborations
 * <tr><td>Encapsulate an element type.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public interface CollectionType<E> extends Type, Serializable
{
    /** Enumerates the different kinds of collections that the model supports. */
    public enum CollectionKind
    {
        /** The collection is an unordered set, each element may only appear once. */
        Set,

        /** The collection is an ordered list. */
        List,

        /** The collection is an unordered bag, each element may appear multiple times. */
        Bag,

        /** The collection is a key/value mapping. */
        Map
    }

    /**
     * Provides the type of the elements that this collection contains.
     *
     * @return The type of the elements that this collection contains.
     */
    public Type<E> getElementType();

    /**
     * Establishes the type of the elements that this collection contains.
     *
     * @param type The type of the elements that this collection contains.
     */
    public void setElementType(Type<E> type);

    /**
     * Provides the kind of collection that this is.
     *
     * @return The kind of collection that this is.
     */
    public CollectionKind getCollectionKind();
}
