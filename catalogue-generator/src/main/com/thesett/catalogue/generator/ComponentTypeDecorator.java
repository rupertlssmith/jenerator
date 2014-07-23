/*
 * © Copyright Rupert Smith, 2005 to 2013.
 *
 * ALL RIGHTS RESERVED. Any unauthorized reproduction or use of this
 * material is prohibited. No part of this work may be reproduced or
 * transmitted in any form or by any means, electronic or mechanical,
 * including photocopying, recording, or by any information storage
 * and retrieval system without express written permission from the
 * author.
 */
package com.thesett.catalogue.generator;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.thesett.aima.state.ComponentType;
import com.thesett.aima.state.State;
import com.thesett.aima.state.Type;

/**
 * ComponentTypeDecorator is a {@link TypeDecorator} for {@link ComponentType}s. It automatically decorates the types
 * of any fields of the component that are accessed through it.
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
     * Creates a type decorator for a component type, that returns decorated types for all reachable child types on
     * all of the fields of the component.
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
    public Set<ComponentType> getImmediateAncestors()
    {
        return ((ComponentType) type).getImmediateAncestors();
    }

    /** {@inheritDoc} */
    public void setImmediateAncestors(Set<ComponentType> immediateAncestors)
    {
        ((ComponentType) type).setImmediateAncestors(immediateAncestors);
    }
}
