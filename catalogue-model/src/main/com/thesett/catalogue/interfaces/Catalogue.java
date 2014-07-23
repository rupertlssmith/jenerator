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
package com.thesett.catalogue.interfaces;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.thesett.aima.attribute.impl.DateRangeType;
import com.thesett.aima.attribute.impl.EnumeratedStringAttribute;
import com.thesett.aima.attribute.impl.HierarchyType;
import com.thesett.aima.attribute.impl.TimeRangeType;
import com.thesett.aima.state.ComponentType;
import com.thesett.aima.state.Type;

/**
 * Catalogue is the root type of the knowledge level of a catalogue. It can be queried as to what {@link ComponentType}s
 * exist in the catalogue.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th>Responsibilities <th>Collaborations
 * <tr><td>Report the contents of a catalogue.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public interface Catalogue
{
    /** The extension to add to a dimension name to get the name of the table in the online database. */
    String ONLINE_TABLE_EXT = "_Online";

    /** The extension to add to a hierarchy type name to get the name of the table in the online database. */
    String HIERARCHY_TABLE_EXT = "_hierarchy";

    /**
     * Provides the namespace that the catalogue package exists under.
     *
     * @return The namespace that the catalogue package exists under.
     */
    public String getModelPackage();

    /**
     * Gets the hierarchy type of a named hierarchy type definition in the catalogue.
     *
     * @param  name The hierarchy type definition to get the type for.
     *
     * @return The hierarchies type or null if it is not part of this catalogue.
     */
    public HierarchyType getHierarchyType(String name);

    /**
     * Gets the enumeration type of a named enumeration definition in the catalogue.
     *
     * @param  name The enumeration type definition to get the type for.
     *
     * @return The enumeration type of <tt>null</tt> if it is not part of this catalogue.
     */
    public EnumeratedStringAttribute.EnumeratedStringType getEnumerationType(String name);

    /**
     * Gets the named component type from the catalogue.
     *
     * @param  name The name of the component type to retrieve.
     *
     * @return The component type, or null if none with a matching name can be found.
     */
    public ComponentType getComponentType(String name);

    /**
     * Gets the named entity type from the catalogue.
     *
     * @param  name The name of the entity type to retrieve.
     *
     * @return The entity type, or null if none with a matching name can be found.
     */
    public EntityType getEntityType(String name);

    /**
     * Gets the named dimension type from the catalogue.
     *
     * @param  name The name of the dimension type to retrieve.
     *
     * @return The dimension type, or null if none with a matching name can be found.
     */
    public DimensionType getDimensionType(String name);

    /**
     * Gets the named fact type from the catalogue.
     *
     * @param  name The name of the fact type to retrieve.
     *
     * @return The fact type, or null if none with a matching name can be found.
     */
    public FactType getFactType(String name);

    /**
     * Gets the named view type from the catalogue.
     *
     * @param  name The name of the view type to retrieve.
     *
     * @return The view type, or null if none with a matching name can be found.
     */
    public ViewType getViewType(String name);

    /**
     * Gets the named date range type from the catalogue.
     *
     * @param  name The name of the date range type to retrieve.
     *
     * @return The date range type, or null if none with a matching name can be found.
     */
    public DateRangeType getDateRangeType(String name);

    /**
     * Gets the named time range type from the catalogue.
     *
     * @param  name The name of the time range type to retrieve.
     *
     * @return The time range type, or null if none with a matching name can be found.
     */
    public TimeRangeType getTimeRangeType(String name);

    /**
     * Lists all top-level types defined in the catalogue.
     *
     * @return A list of all top-level types defined in the catalogue.
     */
    public Collection<Type> getAllTypes();

    /**
     * Gets all the hierarchy type definitions from the catalogue top level.
     *
     * @return A list of all the hierarchy type definitions from the catalogue top level.
     */
    public Collection<HierarchyType> getAllHierarchyTypes();

    /**
     * Gets all the enumeration type definitions from the catalogue top level.
     *
     * @return A list of all the enumeration type definitions from the catalogue top level.
     */
    public Collection<EnumeratedStringAttribute.EnumeratedStringType> getAllEnumTypes();

    /**
     * Lists all the component types in the catalogue.
     *
     * @return A list of all the component types in the catalogue.
     */
    public Collection<ComponentType> getAllComponentTypes();

    /**
     * Lists all the entity types in the catalogue.
     *
     * @return A list of all the entity types in the catalogue.
     */
    public Collection<EntityType> getAllEntityTypes();

    /**
     * Reports the names of all free text indexes to which a dimension is indexed.
     *
     * @param  dimensionName The name of the dimension.
     *
     * @return A list of all the free text search indexes that the dimension is indexed by.
     */
    public List<String> getIndexesForDimension(String dimensionName);

    /**
     * Reports the names of all free text indexes in the catalogue.
     *
     * @return A list of all the free text search indexes in the catalogue.
     */
    public Set<String> getAllIndexes();
}
