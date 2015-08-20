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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.thesett.aima.state.ComponentType;
import com.thesett.aima.state.State;
import com.thesett.aima.state.Type;
import com.thesett.catalogue.model.CollectionType;

/**
 * ComponentTypeDecorator is a {@link TypeDecorator} for {@link ComponentType}s. It automatically decorates the types of
 * any fields of the component that are accessed through it.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Decorate a component type, and the types of all of its fields.
 *     <td> {@link TypeDecoratorFactory}, {@link ComponentType}.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class ComponentTypeDecorator extends TypeDecorator implements ComponentType
{
    /**
     * Creates a type decorator for a component type, that returns decorated types for all reachable child types on all
     * of the fields of the component.
     *
     * @param type The component type to decorate.
     */
    public ComponentTypeDecorator(ComponentType type)
    {
        super(type);
    }

    /** {@inheritDoc} */
    public Map<String, Type> getAllPropertyTypes()
    {
        Map<String, Type> fields = new LinkedHashMap<String, Type>();
        Map<String, Type> originalFields = ((ComponentType) type).getAllPropertyTypes();

        for (Map.Entry<String, Type> field : originalFields.entrySet())
        {
            fields.put(field.getKey(), TypeDecoratorFactory.decorateType(field.getValue()));
        }

        return fields;
    }

    /**
     * Provides the types within the component that have extra restrictions on them.
     *
     * @return The types within the component that have extra restrictions on them, by name.
     */
    public Map<String, Type> getAllRestrictedPropertyTypes()
    {
        Map<String, Type> fields = new LinkedHashMap<String, Type>();
        Map<String, Type> originalFields = ((ComponentType) type).getAllPropertyTypes();

        for (Map.Entry<String, Type> field : originalFields.entrySet())
        {
            TypeDecorator decoratedField = TypeDecoratorFactory.decorateType(field.getValue());

            if (decoratedField.isRestricted())
            {
                fields.put(field.getKey(), decoratedField);
            }
        }

        return fields;
    }

    /**
     * Provides the types for the natural key field set. These types will also be decorated.
     *
     * @return The types for the natural key field set, or <tt>null</tt> if the component does not have a natural key
     *         set.
     */
    public Map<String, Type> getNaturalKeyFieldTypes()
    {
        Set<String> naturalKeyFieldNames = ((ComponentType) type).getNaturalKeyFieldNames();

        if ((naturalKeyFieldNames == null) || naturalKeyFieldNames.isEmpty())
        {
            return null;
        }

        Map<String, Type> fields = new LinkedHashMap<String, Type>();
        Map<String, Type> originalFields = ((ComponentType) type).getAllPropertyTypes();

        for (String fieldName : naturalKeyFieldNames)
        {
            fields.put(fieldName, TypeDecoratorFactory.decorateType(originalFields.get(fieldName)));
        }

        return fields;
    }

    /** {@inheritDoc} */
    public State getInstance()
    {
        return ((ComponentType) type).getInstance();
    }

    /** {@inheritDoc} */
    public Type getPropertyType(String name)
    {
        return TypeDecoratorFactory.decorateType(((ComponentType) type).getPropertyType(name));
    }

    /** {@inheritDoc} */
    public String getPropertyPresentAsAlias(String name)
    {
        return ((ComponentType) type).getPropertyPresentAsAlias(name);
    }

    /** {@inheritDoc} */
    public void setPropertyType(String name, Type type)
    {
        ((ComponentType) type).setPropertyType(name, type);
    }

    /** {@inheritDoc} */
    public Set<String> getAllPropertyNames()
    {
        return ((ComponentType) type).getAllPropertyNames();
    }

    /** {@inheritDoc} */
    public Set<String> getNaturalKeyFieldNames()
    {
        return ((ComponentType) type).getNaturalKeyFieldNames();
    }

    /** {@inheritDoc} */
    public Set<ComponentType> getImmediateAncestors()
    {
        return ((ComponentType) type).getImmediateAncestors();
    }

    /** {@inheritDoc} */
    public void setImmediateAncestors(Set<ComponentType> immediateAncestors)
    {
        ((ComponentType) type).setImmediateAncestors(immediateAncestors);
    }

    /** {@inheritDoc} */
    public State getMetaModel()
    {
        return ((ComponentType) type).getMetaModel();
    }

    public boolean isWithMapField()
    {
        return scanForCollectionKind(CollectionType.CollectionKind.Map);
    }

    public boolean isWithSetField()
    {
        return scanForCollectionKind(CollectionType.CollectionKind.Set);
    }

    public boolean isWithListField()
    {
        return scanForCollectionKind(CollectionType.CollectionKind.List);
    }

    public boolean isWithBigDecimalField()
    {
        return scanForKind(Kind.BigDecimal);
    }

    public boolean isWithDateOnlyField()
    {
        return scanForKind(Kind.DateOnly);
    }

    public boolean isWithTimeOnlyField()
    {
        return scanForKind(Kind.TimeOnly);
    }

    public boolean isWithTimestampField()
    {
        return scanForKind(Kind.Timestamp);
    }

    private boolean scanForKind(Kind kind)
    {
        for (Type type : getAllPropertyTypes().values())
        {
            if (((TypeDecorator) type).getKind() == kind)
            {
                return true;
            }
        }

        return false;
    }

    private boolean scanForCollectionKind(CollectionType.CollectionKind collectionKind)
    {
        for (Type type : getAllPropertyTypes().values())
        {
            if (((TypeDecorator) type).getKind() == Kind.Collection)
            {
                if (((CollectionTypeDecorator) type).getCollectionKind() == collectionKind)
                {
                    return true;
                }
            }
        }

        return false;
    }
}
