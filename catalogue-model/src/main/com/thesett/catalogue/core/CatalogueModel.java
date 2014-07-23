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
package com.thesett.catalogue.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.thesett.aima.attribute.impl.DateRangeType;
import com.thesett.aima.attribute.impl.EnumeratedStringAttribute;
import com.thesett.aima.attribute.impl.HierarchyType;
import com.thesett.aima.attribute.impl.TimeRangeType;
import com.thesett.aima.state.ComponentType;
import com.thesett.aima.state.Type;
import com.thesett.catalogue.interfaces.Catalogue;
import com.thesett.catalogue.interfaces.DimensionType;
import com.thesett.catalogue.interfaces.EntityType;
import com.thesett.catalogue.interfaces.FactType;
import com.thesett.catalogue.interfaces.ViewType;
import com.thesett.common.error.NotImplementedException;
import com.thesett.common.util.Filterators;
import com.thesett.common.util.SubclassFilterator;

/**
 * CatalogueModel implements a model of a catalogue of data types in first order logic. It provides queries over the raw
 * catalogue to produce a canonical reduction of its types to a normalized form.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Report the contents of a catalogue.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class CatalogueModel implements Catalogue, Serializable
{
    /** Holds the namespace of the catalogue. */
    protected String packageName;

    /** Holds a mapping of all top-level types in the model by name. */
    protected Map<String, Type> catalogueTypes = new HashMap<String, Type>();

    /**
     * Creates the cataloge on the specified set of types, implemented in the specified Java package.
     *
     * @param packageName    The Java package holding the catalogue implementation.
     * @param catalogueTypes The types in the catalogue.
     */
    public CatalogueModel(String packageName, Map<String, Type> catalogueTypes)
    {
        this.packageName = packageName;
        this.catalogueTypes = catalogueTypes;
    }

    /** {@inheritDoc} */
    public String getModelPackage()
    {
        return packageName;
    }

    /** {@inheritDoc} */
    public ComponentType getComponentType(String name)
    {
        return (ComponentType) catalogueTypes.get(name);
    }

    /** {@inheritDoc} */
    public EntityType getEntityType(String name)
    {
        return (EntityType) catalogueTypes.get(name);
    }

    /** {@inheritDoc} */
    public DimensionType getDimensionType(String name)
    {
        return (DimensionType) catalogueTypes.get(name);
    }

    /** {@inheritDoc} */
    public FactType getFactType(String name)
    {
        return (FactType) catalogueTypes.get(name);
    }

    /** {@inheritDoc} */
    public ViewType getViewType(String name)
    {
        return (ViewType) catalogueTypes.get(name);
    }

    /** {@inheritDoc} */
    public HierarchyType getHierarchyType(String name)
    {
        return (HierarchyType) catalogueTypes.get(name);
    }

    /** {@inheritDoc} */
    public EnumeratedStringAttribute.EnumeratedStringType getEnumerationType(String name)
    {
        return (EnumeratedStringAttribute.EnumeratedStringType) catalogueTypes.get(name);
    }

    /** {@inheritDoc} */
    public DateRangeType getDateRangeType(String name)
    {
        return (DateRangeType) catalogueTypes.get(name);
    }

    /** {@inheritDoc} */
    public TimeRangeType getTimeRangeType(String name)
    {
        return (TimeRangeType) catalogueTypes.get(name);
    }

    /** {@inheritDoc} */
    public Collection<Type> getAllTypes()
    {
        return catalogueTypes.values();
    }

    /** {@inheritDoc} */
    public Collection<ComponentType> getAllComponentTypes()
    {
        return Filterators.collectIterator(new SubclassFilterator<Type, ComponentType>(
            catalogueTypes.values().iterator(), ComponentType.class), new ArrayList<ComponentType>());
    }

    /** {@inheritDoc} */
    public Collection<EntityType> getAllEntityTypes()
    {
        return Filterators.collectIterator(new SubclassFilterator<Type, EntityType>(catalogueTypes.values().iterator(),
                EntityType.class), new ArrayList<EntityType>());
    }

    /** {@inheritDoc} */
    public Collection<HierarchyType> getAllHierarchyTypes()
    {
        return Filterators.collectIterator(new SubclassFilterator<Type, HierarchyType>(
            catalogueTypes.values().iterator(), HierarchyType.class), new ArrayList<HierarchyType>());
    }

    /** {@inheritDoc} */
    public Collection<EnumeratedStringAttribute.EnumeratedStringType> getAllEnumTypes()
    {
        return Filterators.collectIterator(new SubclassFilterator<Type, EnumeratedStringAttribute.EnumeratedStringType>(
            catalogueTypes.values().iterator(), EnumeratedStringAttribute.EnumeratedStringType.class),
            new ArrayList<EnumeratedStringAttribute.EnumeratedStringType>());
    }

    /** {@inheritDoc} */
    public List<String> getIndexesForDimension(String dimensionName)
    {
        return new LinkedList<String>();
    }

    /** {@inheritDoc} */
    public Set<String> getAllIndexes()
    {
        throw new NotImplementedException();
    }

    /**
     * Prints the contents of the catalogue for debugging purposes.
     *
     * @return The contents of the catalogue as a debugging string.
     */
    public String toString()
    {
        String catalogueTypesString = "";

        for (Map.Entry<String, Type> entry : catalogueTypes.entrySet())
        {
            catalogueTypesString += "[" + entry.getKey() + ", " + entry.getValue() + "]";
        }

        return "CatalogueModel: [ packageName = " + packageName + " catalogueTypes = [" + catalogueTypesString + "]]";
    }
}
