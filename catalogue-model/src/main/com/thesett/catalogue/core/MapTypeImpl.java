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
package com.thesett.catalogue.core;

import com.thesett.aima.state.Type;
import com.thesett.aima.state.TypeVisitor;
import com.thesett.catalogue.interfaces.MapType;
import com.thesett.catalogue.interfaces.MapTypeVisitor;

/**
 * MapTypeImpl implements a type that is a map of typed key/element pairs. The underlying map implementation is also
 * encapsulated by this type.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Encapsulate a key/element types and a map implementation.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class MapTypeImpl extends CollectionTypeImpl implements MapType
{
    /** Holds the type of the keys of the map. */
    private Type keyType;

    /**
     * Creates a map type with the specified key and elements types and underlying implementation.
     *
     * @param keyType             The type of the keys in the map.
     * @param elementType         The type of the elements in the map.
     * @param collectionImplClass The underlying map implementation to use.
     */
    public MapTypeImpl(Type keyType, Type elementType, Class collectionImplClass)
    {
        super(elementType, collectionImplClass, CollectionKind.Map);
        this.keyType = keyType;
    }

    /** {@inheritDoc} */
    public Type getKeyType()
    {
        return keyType;
    }

    /** {@inheritDoc} */
    public void setKeyType(Type type)
    {
        this.keyType = type;
    }

    /** {@inheritDoc} */
    public void acceptVisitor(TypeVisitor visitor)
    {
        if (visitor instanceof MapTypeVisitor)
        {
            ((MapTypeVisitor) visitor).visit(this);
        }
        else
        {
            super.acceptVisitor(visitor);
        }
    }
}
