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

/**
 * CollectionTypeDecorator is a {@link TypeDecorator} for {@link CollectionType}s. It automatically decorates the type
 * of the collections elements when accessed through it.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Decorate a collection type and the type of its elements too.
 *     <td> {@link TypeDecoratorFactory}, {@link CollectionType}.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class CollectionTypeDecorator<T> extends TypeDecorator implements CollectionType<T>
{
    /**
     * Creates a type decorator for a collection type, that decorates the type of the collections elements too.
     *
     * @param type The collection type to decorate.
     */
    public CollectionTypeDecorator(CollectionType type)
    {
        super(type);
    }

    /** {@inheritDoc} */
    public Type<T> getElementType()
    {
        return TypeDecoratorFactory.decorateType(((CollectionType<T>) type).getElementType());
    }

    /** {@inheritDoc} */
    public void setElementType(Type<T> type)
    {
        ((CollectionType<T>) type).setElementType(type);
    }

    /** {@inheritDoc} */
    public CollectionKind getCollectionKind()
    {
        return ((CollectionType<T>) type).getCollectionKind();
    }

    /**
     * Reports whether or not this collection is a map.
     *
     * @return <tt>true</tt> if this collection is a map.
     */
    public boolean isMap()
    {
        return CollectionKind.Map.equals(getCollectionKind());
    }
}
