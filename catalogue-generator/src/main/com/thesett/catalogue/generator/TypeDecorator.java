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

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.thesett.aima.attribute.impl.EnumeratedStringAttribute;
import com.thesett.aima.attribute.impl.HierarchyType;
import com.thesett.aima.attribute.time.DateOnly;
import com.thesett.aima.attribute.time.TimeOnly;
import com.thesett.aima.state.ComponentType;
import com.thesett.aima.state.InfiniteValuesException;
import com.thesett.aima.state.RandomInstanceNotSupportedException;
import com.thesett.aima.state.Type;
import com.thesett.aima.state.TypeVisitor;
import com.thesett.aima.state.restriction.TypeRestriction;
import com.thesett.catalogue.model.CollectionType;
import com.thesett.catalogue.model.DimensionType;
import com.thesett.catalogue.model.EntityType;
import com.thesett.catalogue.model.FactType;
import com.thesett.catalogue.model.ViewType;

/**
 * TypeDecorators are used to decorate {@link com.thesett.aima.state.Type}s with additional derived flags, which can be
 * easily tested to determine properties of types that are relevant to the catalogue generators.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Decorate types with catalogue relevant flags.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class TypeDecorator<T> implements Type<T>
{
    /** Enumerates all of the different kinds of types that can appear in the catalogue. */
    public enum Kind
    {
        /** The type is a component. */
        Component,

        /** The type is an entity. */
        Entity,

        /** The type is a dimension. */
        Dimension,

        /** The type is a view. */
        View,

        /** The type is a fact. */
        Fact,

        /** The type is a hierarchy. */
        Hierarchy,

        /** The type is an enumeration. */
        Enumeration,

        /** The type is a collection. */
        Collection,

        /** The type is a BigDecimal. */
        BigDecimal,

        /** The type is a string. */
        String,

        /** The type is a date. */
        DateOnly,

        /** The type is a time. */
        TimeOnly,

        /** The type is a timestamp. */
        Timestamp,

        /** The type is a basic java type. */
        Basic
    }

    /** Enumerates all of the different kinds of primitives that can appear in the catalogue. */
    public enum PrimitiveKind
    {
        /** The type is a boolean. */
        Boolean,

        /** The type is a character. */
        Char,

        /** The type is a short. */
        Short,

        /** The type is am int. */
        Int,

        /** The type is a long. */
        Long,

        /** The type is a float. */
        Float,

        /** The type is a double. */
        Double,

        /** The type is an object. */
        Object
    }

    /** Holds the wrapped type that this decorates. */
    protected Type<T> type;

    /** Holds the kind of the decorated type. */
    protected Kind kind;

    /** Holds the primitive kind of the decorated type. */
    protected PrimitiveKind primitiveKind;

    /**
     * Decorates the specified type with catalogue specific flags.
     *
     * @param type The type to decorate.
     */
    public TypeDecorator(Type<T> type)
    {
        this.type = type;

        if (type instanceof ViewType)
        {
            kind = Kind.View;
            primitiveKind = PrimitiveKind.Object;
        }
        else if (type instanceof DimensionType)
        {
            kind = Kind.Dimension;
            primitiveKind = PrimitiveKind.Object;
        }
        else if (type instanceof FactType)
        {
            kind = Kind.Fact;
            primitiveKind = PrimitiveKind.Object;
        }
        else if (type instanceof EntityType)
        {
            kind = Kind.Entity;
            primitiveKind = PrimitiveKind.Object;
        }
        else if (type instanceof ComponentType)
        {
            kind = Kind.Component;
            primitiveKind = PrimitiveKind.Object;
        }
        else if (type instanceof EnumeratedStringAttribute.EnumeratedStringType)
        {
            kind = Kind.Enumeration;
            primitiveKind = PrimitiveKind.Object;
        }
        else if (type instanceof HierarchyType)
        {
            kind = Kind.Hierarchy;
            primitiveKind = PrimitiveKind.Object;
        }
        else if (type instanceof CollectionType)
        {
            kind = Kind.Collection;
            primitiveKind = PrimitiveKind.Object;
        }
        else if (String.class.equals(type.getBaseClass()))
        {
            kind = Kind.String;
            primitiveKind = PrimitiveKind.Object;
        }
        else if (DateOnly.class.equals(type.getBaseClass()))
        {
            kind = Kind.DateOnly;
            primitiveKind = PrimitiveKind.Object;
        }
        else if (TimeOnly.class.equals(type.getBaseClass()))
        {
            kind = Kind.TimeOnly;
            primitiveKind = PrimitiveKind.Object;
        }
        else if (BigDecimal.class.equals(type.getBaseClass()))
        {
            kind = Kind.BigDecimal;
            primitiveKind = PrimitiveKind.Object;
        }
        else
        {
            kind = Kind.Basic;

            if (Boolean.class.equals(type.getBaseClass()))
            {
                primitiveKind = PrimitiveKind.Boolean;
            }
            else if (Character.class.equals(type.getBaseClass()))
            {
                primitiveKind = PrimitiveKind.Char;
            }
            else if (Short.class.equals(type.getBaseClass()))
            {
                primitiveKind = PrimitiveKind.Short;
            }
            else if (Integer.class.equals(type.getBaseClass()))
            {
                primitiveKind = PrimitiveKind.Int;
            }
            else if (Long.class.equals(type.getBaseClass()))
            {
                primitiveKind = PrimitiveKind.Long;
            }
            else if (Float.class.equals(type.getBaseClass()))
            {
                primitiveKind = PrimitiveKind.Float;
            }
            else if (Double.class.equals(type.getBaseClass()))
            {
                primitiveKind = PrimitiveKind.Double;
            }
            else
            {
                primitiveKind = PrimitiveKind.Object;
            }
        }
    }

    /**
     * Provides an enumeration giving the kind of type that this is a decorator for.
     *
     * @return The kind of type that this is a decorator for.
     */
    public Kind getKind()
    {
        return kind;
    }

    /**
     * Provides an enumeration giving the primitive kind of type that this is a decorator for.
     *
     * @return The primitive kind of type that this is a decorator for.
     */
    public PrimitiveKind getPrimitiveKind()
    {
        return primitiveKind;
    }

    /**
     * Determines if the decorated type is a component or sub-type.
     *
     * @return <tt>true</tt> if the decorated type is a component or sub-type.
     */
    public boolean isComponentSubType()
    {
        switch (kind)
        {
        case Component:
        case Entity:
        case Dimension:
        case View:
        case Fact:
            return true;

        default:
            return false;
        }
    }

    /**
     * Determines if the decorated type is a component.
     *
     * @return <tt>true</tt> if the decorated type is a component.
     */
    public boolean isComponent()
    {
        return Kind.Component.equals(kind);
    }

    /**
     * Determines if the decorated type is an entity or sub-type.
     *
     * @return <tt>true</tt> if the decorated type is an entity or sub-type.
     */
    public boolean isEntitySubType()
    {
        switch (kind)
        {
        case Entity:
        case Dimension:
        case Fact:
            return true;

        default:
            return false;
        }
    }

    /**
     * Determines if the decorated type is an entity.
     *
     * @return <tt>true</tt> if the decorated type is an entity.
     */
    public boolean isEntity()
    {
        return Kind.Entity.equals(kind);
    }

    /**
     * Determines if the decorated type is a dimension.
     *
     * @return <tt>true</tt> if the decorated type is a dimension.
     */
    public boolean isDimension()
    {
        return Kind.Dimension.equals(kind);
    }

    /**
     * Determines if the decorated type is a view.
     *
     * @return <tt>true</tt> if the decorated type is a view.
     */
    public boolean isView()
    {
        return Kind.View.equals(kind);
    }

    /**
     * Determines if the decorated type is a fact.
     *
     * @return <tt>true</tt> if the decorated type is a fact.
     */
    public boolean isFact()
    {
        return Kind.Fact.equals(kind);
    }

    /**
     * Determines if the decorated type is a hierarchy.
     *
     * @return <tt>true</tt> if the decorated type is a hierarchy.
     */
    public boolean isHierarchyType()
    {
        return Kind.Hierarchy.equals(kind);
    }

    /**
     * Determines if the decorated type is an enumeration.
     *
     * @return <tt>true</tt> if the decorated type is an enumeration.
     */
    public boolean isEnumerationType()
    {
        return Kind.Enumeration.equals(kind);
    }

    /**
     * Determines whether the decorated type is considered a primitive type.
     *
     * @return <tt>true</tt> if the decorated type is considered a primitive type.
     */
    public boolean isPrimitive()
    {
        return !PrimitiveKind.Object.equals(primitiveKind);
    }

    /**
     * Determines whether the decorated type is considered a basic type.
     *
     * @return <tt>true</tt> if the decorated type is considered a basic type.
     */
    public boolean isBasic()
    {
        return Kind.Basic.equals(kind);
    }

    /**
     * Determines whether the decorated type has extra constraints on its instance values.
     *
     * @return <tt>true</tt> iff the decorated type has extra constraints.
     */
    public boolean isRestricted()
    {
        List<TypeRestriction> restrictions = type.getRestrictions();

        return (restrictions != null) && !restrictions.isEmpty();
    }

    /** {@inheritDoc} */
    public T getDefaultInstance()
    {
        return type.getDefaultInstance();
    }

    /** {@inheritDoc} */
    public T getRandomInstance() throws RandomInstanceNotSupportedException
    {
        return type.getRandomInstance();
    }

    /** {@inheritDoc} */
    public String getName()
    {
        return type.getName();
    }

    /** {@inheritDoc} */
    public Class<T> getBaseClass()
    {
        return type.getBaseClass();
    }

    /** {@inheritDoc} */
    public String getBaseClassName()
    {
        return type.getBaseClassName();
    }

    /** {@inheritDoc} */
    public List<TypeRestriction> getRestrictions()
    {
        return type.getRestrictions();
    }

    /** {@inheritDoc} */
    public int getNumPossibleValues()
    {
        return type.getNumPossibleValues();
    }

    /** {@inheritDoc} */
    public Set<T> getAllPossibleValuesSet() throws InfiniteValuesException
    {
        return type.getAllPossibleValuesSet();
    }

    /** {@inheritDoc} */
    public Iterator<T> getAllPossibleValuesIterator() throws InfiniteValuesException
    {
        return type.getAllPossibleValuesIterator();
    }

    /** {@inheritDoc} */
    public void acceptVisitor(TypeVisitor visitor)
    {
        type.acceptVisitor(visitor);
    }
}
