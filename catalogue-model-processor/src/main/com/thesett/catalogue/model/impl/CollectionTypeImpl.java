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

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import com.thesett.aima.state.BaseType;
import com.thesett.aima.state.InfiniteValuesException;
import com.thesett.aima.state.Type;
import com.thesett.aima.state.TypeVisitor;
import com.thesett.catalogue.model.CollectionType;
import com.thesett.catalogue.model.CollectionTypeVisitor;
import com.thesett.common.util.ReflectionUtils;

/**
 * CollectionTypeImpl implements a type that is a collection of typed elements. The underlying collection implementation
 * is also encapsulated by this type.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Encapsulate an element type and a collection implementation.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class CollectionTypeImpl<T> extends BaseType implements CollectionType<T>
{
    /** Holds the type of elements that this collection type contains. */
    private Type<T> elementType;

    /** Holds the underlying class that implements the collection. */
    private Class<Collection<T>> collectionImplClass;

    /** Holds the basic kind of collection that this is. */
    private CollectionKind kind;

    /**
     * Creates a collection type over the specified element type, using the given collection implementation class.
     *
     * @param elementType         The type of elements that this collection type contains.
     * @param collectionImplClass The underlying class that implements the collection.
     * @param kind                The fundamental kind of collection that this is.
     */
    public CollectionTypeImpl(Type<T> elementType, Class<Collection<T>> collectionImplClass, CollectionKind kind)
    {
        this.elementType = elementType;
        this.collectionImplClass = collectionImplClass;
        this.kind = kind;
    }

    /** {@inheritDoc} */
    public Type<T> getElementType()
    {
        return elementType;
    }

    /** {@inheritDoc} */
    public void setElementType(Type<T> type)
    {
        this.elementType = type;
    }

    /** {@inheritDoc} */
    public Object getDefaultInstance()
    {
        return ReflectionUtils.newInstance(collectionImplClass);
    }

    /** {@inheritDoc} */
    public String getName()
    {
        return "collection";
    }

    /** {@inheritDoc} */
    public Class getBaseClass()
    {
        return collectionImplClass;
    }

    /** {@inheritDoc} */
    public String getBaseClassName()
    {
        return getBaseClass().getName();
    }

    /** {@inheritDoc} */
    public int getNumPossibleValues()
    {
        return -1;
    }

    /** {@inheritDoc} */
    public Set getAllPossibleValuesSet() throws InfiniteValuesException
    {
        throw new InfiniteValuesException("Collection types can take on too many values to enumerate.", null);
    }

    /** {@inheritDoc} */
    public Iterator getAllPossibleValuesIterator() throws InfiniteValuesException
    {
        throw new InfiniteValuesException("Collection types can take on too many values to enumerate.", null);
    }

    /** {@inheritDoc} */
    public CollectionKind getCollectionKind()
    {
        return kind;
    }

    /** {@inheritDoc} */
    public void acceptVisitor(TypeVisitor visitor)
    {
        if (visitor instanceof CollectionTypeVisitor)
        {
            ((CollectionTypeVisitor) visitor).visit(this);
        }
        else
        {
            super.acceptVisitor(visitor);
        }
    }
}
