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
package com.thesett.catalogue.generator;

import com.thesett.aima.state.Type;
import com.thesett.catalogue.model.CollectionType;
import com.thesett.catalogue.model.MapType;

/**
 * MapTypeDecorator is a {@link TypeDecorator} for {@link MapType}s. It automatically decorates the type of the map keys
 * and elements when accessed through it.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Decorate a collection type and the type of its keys and elements too.
 *     <td> {@link TypeDecoratorFactory}, {@link MapType}.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class MapTypeDecorator<K, E> extends CollectionTypeDecorator<E> implements MapType<K, E>
{
    /**
     * Creates a type decorator for a map type, that decorates the type of the maps keys and elements too.
     *
     * @param type The map type to decorate.
     */
    public MapTypeDecorator(CollectionType type)
    {
        super(type);
    }

    /** {@inheritDoc} */
    public Type<K> getKeyType()
    {
        return TypeDecoratorFactory.decorateType(((MapType) type).getKeyType());
    }

    /** {@inheritDoc} */
    public void setKeyType(Type<K> type)
    {
        ((MapType) type).setKeyType(type);
    }
}
