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
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.thesett.aima.state.BaseType;
import com.thesett.aima.state.ComponentType;
import com.thesett.aima.state.InfiniteValuesException;
import com.thesett.aima.state.RandomInstanceFactory;
import com.thesett.aima.state.RandomInstanceNotSupportedException;
import com.thesett.aima.state.State;
import com.thesett.aima.state.Type;
import com.thesett.aima.state.TypeVisitor;
import com.thesett.catalogue.model.CollectionType;
import com.thesett.catalogue.model.ComponentInstance;
import com.thesett.catalogue.model.ComponentTypeVisitor;
import com.thesett.common.util.ReflectionUtils;
import com.thesett.common.util.StringUtils;

/**
 * A ComponentTypeImpl consists of a set of types named as fields which are themselves sets of possible values, hence it
 * is a cross product of types.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Provide the types and names of fields that make up a component.
 * <tr><td> Create a transient instance of the component for which this is the type.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class ComponentTypeImpl extends BaseType implements ComponentType, Serializable, RandomInstanceFactory
{
    /** The name of the component type. */
    private String name;

    /** The fully qualified name of the operational level implementation class of the component type. */
    private String operationalClassName;

    /** Holds a map from names to the property types of the component type. */
    private Map<String, Type> properties;

    /** Holds the names of the fields forming the natural key of the component. */
    private Set<String> naturalKeyFields;

    /** Holds the ancestor types of this component. */
    private Set<ComponentType> immediateAncestors;

    /**
     * Creates a knowledge level description of a component type with the specified name for a set of attribute types.
     *
     * @param name                 The name of the component type.
     * @param attributes           The attributes of the component type.
     * @param naturalKeyFields     The set of fields forming the natural key of the component.
     * @param operationalClassName The fully qualified name of the class that this component type describes.
     * @param immediateAncestors   The immediate ancestors of this component type.
     */
    public ComponentTypeImpl(String name, Map<String, Type> attributes, Set<String> naturalKeyFields,
        String operationalClassName, Set<ComponentType> immediateAncestors)
    {
        this.properties = attributes;
        this.naturalKeyFields = naturalKeyFields;
        this.name = name;
        this.operationalClassName = operationalClassName;
        this.immediateAncestors = immediateAncestors;
    }

    /** {@inheritDoc} */
    public String getName()
    {
        return name;
    }

    /** {@inheritDoc} */
    public Type getPropertyType(String name)
    {
        return properties.get(name);
    }

    /** {@inheritDoc} */
    public void setPropertyType(String name, Type type)
    {
        properties.put(name, type);
    }

    /** {@inheritDoc} */
    public Set<String> getAllPropertyNames()
    {
        return properties.keySet();
    }

    /** {@inheritDoc} */
    public Set<String> getNaturalKeyFieldNames()
    {
        return naturalKeyFields;
    }

    /** {@inheritDoc} */
    public Set<ComponentType> getImmediateAncestors()
    {
        return immediateAncestors;
    }

    /** {@inheritDoc} */
    public void setImmediateAncestors(Set<ComponentType> immediateAncestors)
    {
        this.immediateAncestors = immediateAncestors;
    }

    /** {@inheritDoc} */
    public Map<String, Type> getAllPropertyTypes()
    {
        return properties;
    }

    /** {@inheritDoc} */
    public String getBaseClassName()
    {
        return operationalClassName;
    }

    /** {@inheritDoc} */
    public State getInstance()
    {
        return (State) ReflectionUtils.newInstance(ReflectionUtils.forName(operationalClassName));
    }

    /** {@inheritDoc} */
    public Object getDefaultInstance()
    {
        return ReflectionUtils.newInstance(ReflectionUtils.forName(operationalClassName));
    }

    /** {@inheritDoc} */
    public Object createRandomInstance() throws RandomInstanceNotSupportedException
    {
        ComponentInstance instance = (ComponentInstance) getDefaultInstance();

        for (Map.Entry<String, Type> field : getAllPropertyTypes().entrySet())
        {
            Type fieldType = field.getValue();
            Object fieldValue;

            if (fieldType instanceof ComponentType)
            {
                fieldValue = null;
            }
            else if (fieldType instanceof CollectionType)
            {
                fieldValue = null;
            }
            else
            {
                fieldValue = fieldType.getRandomInstance();
            }

            instance.setProperty(StringUtils.toCamelCase(field.getKey()), fieldValue);
        }

        return instance;
    }

    /** {@inheritDoc} */
    public Class getBaseClass()
    {
        return ReflectionUtils.forName(operationalClassName);
    }

    /** {@inheritDoc} */
    public int getNumPossibleValues()
    {
        return -1;
    }

    /** {@inheritDoc} */
    public Set getAllPossibleValuesSet() throws InfiniteValuesException
    {
        throw new InfiniteValuesException("Too many possible values to enumerate.", null);
    }

    /** {@inheritDoc} */
    public Iterator getAllPossibleValuesIterator() throws InfiniteValuesException
    {
        throw new InfiniteValuesException("Too many possible values to enumerate.", null);
    }

    /** {@inheritDoc} */
    public void acceptVisitor(TypeVisitor visitor)
    {
        if (visitor instanceof ComponentTypeVisitor)
        {
            ((ComponentTypeVisitor) visitor).visit(this);
        }
        else
        {
            super.acceptVisitor(visitor);
        }
    }

    /**
     * Tests this component for equality with another. Two component types are equal if they have the same name.
     *
     * @param  o The object to compare to.
     *
     * @return <tt>true</tt> if the comparator is a component type with the same name as this one.
     */
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }

        if (!(o instanceof ComponentTypeImpl))
        {
            return false;
        }

        final ComponentTypeImpl component = (ComponentTypeImpl) o;

        return !((name != null) ? (!name.equals(component.name)) : (component.name != null));
    }

    /**
     * Computes a hash code for this component type based on its name.
     *
     * @return A hash code for this component type based on its name.
     */
    public int hashCode()
    {
        return ((name != null) ? name.hashCode() : 0);
    }

    /**
     * Renders the component type as a string, mainly for debugging purposes.
     *
     * @return The component type as a string.
     */
    public String toString()
    {
        return "ComponentTypeImpl: [ name = " + name + ", operationalClassName = " + operationalClassName + " ]";
    }
}