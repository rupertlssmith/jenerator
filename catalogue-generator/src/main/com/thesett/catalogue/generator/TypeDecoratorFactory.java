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

import com.thesett.aima.attribute.impl.BigDecimalType;
import com.thesett.aima.attribute.impl.BigDecimalTypeVisitor;
import com.thesett.aima.attribute.impl.DateRangeType;
import com.thesett.aima.attribute.impl.DateRangeTypeVisitor;
import com.thesett.aima.attribute.impl.EnumeratedStringAttribute;
import com.thesett.aima.attribute.impl.EnumeratedStringTypeVisitor;
import com.thesett.aima.attribute.impl.HierarchyType;
import com.thesett.aima.attribute.impl.HierarchyTypeVisitor;
import com.thesett.aima.attribute.impl.TimeRangeType;
import com.thesett.aima.attribute.impl.TimeRangeTypeVisitor;
import com.thesett.aima.state.ComponentType;
import com.thesett.aima.state.Type;
import com.thesett.aima.state.TypeVisitor;
import com.thesett.catalogue.interfaces.CollectionType;
import com.thesett.catalogue.interfaces.CollectionTypeVisitor;
import com.thesett.catalogue.interfaces.ComponentTypeVisitor;
import com.thesett.catalogue.interfaces.EntityType;
import com.thesett.catalogue.interfaces.EntityTypeVisitor;
import com.thesett.catalogue.interfaces.MapType;
import com.thesett.catalogue.interfaces.MapTypeVisitor;

/**
 * TypeDecoratorFactory is a {@link TypeVisitor}, that can visit a {@link com.thesett.aima.state.Type}, and based on the
 * kind of Type encountered, produce a {@link TypeDecorator} for that Type, that can transparently act as a substitute
 * for the original Type, whilst dynamically adding new behaviour to it.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Create a default type decorator for any types. <td> {@link TypeDecorator}.
 * <tr><td> Create a type decorator for component types. <td> {@link ComponentType}.
 * <tr><td> Create a type decorator for collection types. <td> {@link CollectionType}.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class TypeDecoratorFactory implements TypeVisitor, ComponentTypeVisitor, EntityTypeVisitor,
    CollectionTypeVisitor, HierarchyTypeVisitor, MapTypeVisitor, EnumeratedStringTypeVisitor, BigDecimalTypeVisitor,
    TimeRangeTypeVisitor, DateRangeTypeVisitor
{
    /** Holds the type decorator for the most recently visited type. */
    protected TypeDecorator typeDecorator;

    /**
     * Private constructor to prevent instantiation, types should be decorated by the static {@link #decorateType}
     * method.
     */
    private TypeDecoratorFactory()
    {
    }

    /**
     * Decorates a type.
     *
     * @param  <T>  The underlying Java type of the type to decorate.
     * @param  type The type to decorate.
     *
     * @return The decorated type.
     */
    public static <T> TypeDecorator<T> decorateType(Type<T> type)
    {
        TypeDecoratorFactory decoratorFactory = new TypeDecoratorFactory();
        type.acceptVisitor(decoratorFactory);

        TypeDecorator<T> decorator = decoratorFactory.getDecorator();

        return decorator;
    }

    /** {@inheritDoc} */
    public TypeDecorator getDecorator()
    {
        return typeDecorator;
    }

    /** {@inheritDoc} */
    public <T> void visit(Type<T> type)
    {
        typeDecorator = new TypeDecorator<T>(type);
    }

    /** {@inheritDoc} */
    public void visit(ComponentType type)
    {
        typeDecorator = new ComponentTypeDecorator(type);
    }

    /** {@inheritDoc} */
    public void visit(EntityType type)
    {
        typeDecorator = new EntityTypeDecorator(type);
    }

    /** {@inheritDoc} */
    public <E> void visit(CollectionType<E> type)
    {
        typeDecorator = new CollectionTypeDecorator(type);
    }

    /** {@inheritDoc} */
    public <K, E> void visit(MapType<K, E> type)
    {
        typeDecorator = new MapTypeDecorator(type);
    }

    /** {@inheritDoc} */
    public void visit(HierarchyType type)
    {
        typeDecorator = new HierarchyTypeDecorator(type);
    }

    /** {@inheritDoc} */
    public void visit(EnumeratedStringAttribute.EnumeratedStringType type)
    {
        typeDecorator = new EnumeratedStringTypeDecorator(type);
    }

    /** {@inheritDoc} */
    public void visit(BigDecimalType type)
    {
        typeDecorator = new BigDecimalTypeDecorator(type);
    }

    /** {@inheritDoc} */
    public void visit(DateRangeType type)
    {
        typeDecorator = new DateRangeTypeDecorator(type);
    }

    /** {@inheritDoc} */
    public void visit(TimeRangeType type)
    {
        typeDecorator = new TimeRangeTypeDecorator(type);
    }
}
