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
package com.thesett.catalogue.interfaces;

import com.thesett.aima.state.Type;

/**
 * A MapType is a {@link com.thesett.aima.state.Type} that acts as a container for key/value instances of other types.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Encapsulate a key type.
 * <tr><td> Encapsulate an element type.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public interface MapType<K, E> extends CollectionType<E>
{
    /**
     * Provides the type of the keys that this map contains.
     *
     * @return The type of the keys that this map contains.
     */
    public Type<K> getKeyType();

    /**
     * Establishes the type of the keys that this map contains.
     *
     * @param type The type of the keys that this map contains.
     */
    public void setKeyType(Type<K> type);
}
