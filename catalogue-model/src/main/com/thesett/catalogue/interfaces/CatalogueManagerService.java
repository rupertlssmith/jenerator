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
package com.thesett.catalogue.interfaces;

import java.util.List;
import java.util.Map;

import org.hibernate.criterion.Criterion;

import com.thesett.aima.attribute.impl.HierarchyType;
import com.thesett.aima.state.Attribute;
import com.thesett.aima.state.ComponentType;

/**
 * The catalogue manager service provides CRUD operations on entities and their inter-relationships. It also provides
 * methods to search the catalogue by free text search, or partially or fully instantiated entity instances.
 *
 * <p/>The retrieve operation takes the type of the entity to be retrieved. From this the implementation can figure out
 * what fields need to be retrieved and from what data source.
 *
 * <p/>The browse operations are worth mentioning. They take a mapping of property names and attribute values to be
 * matched. The use of a map allows multiple properties to be specified in a single call. The values to be matched are
 * specified as attributes and the query to fetch elements matching the specified values is built from this. It is
 * possible to use attributes that specify ranges of values, to retrieve entities matching the range. The browse method
 * {@link #browse(EntityType, java.util.Map, String)} takes the type of entity to be retrieved, and restricts its
 * results to just that entity type, the other browse method {@link #browse(java.util.Map, String)} does not restrict to
 * a type, so can retrieve multiple types of entity in a single query.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities
 * <tr><td> Provide the catalogue that this service manages.
 * <tr><td> Perform CRUD operations on entities in the catalogue.
 * <tr><td> Perform catalogue queries by partial attribute specification.
 * <tr><td> Perform catalouge queries by external id.
 * <tr><td> Perform free text catalogue searches.
 * <tr><td> Generate long-lived external ids.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public interface CatalogueManagerService
{
    /**
     * Gets the catalogue instance.
     *
     * @return The catalogue.
     */
    public Catalogue getCatalogue();

    /**
     * Creates a new persistent hierarchy bean.
     *
     * @param hierarchy The hierarchy bean to create.
     */
    public void createHierarchyInstance(HierarchyInstance hierarchy);

    /**
     * Permanently deletes a hierarchy instance, specified by its internal id.
     *
     * @param type The type of the instance to delete.
     * @param id   The id of the instance to delete.
     */
    public void deleteHierarchyInstance(HierarchyType type, InternalId id);

    /**
     * Given an instance of a hierarchy, queries the database to see if that instance exists in it, and optionally if
     * any child instances of it exist too. The resulting hierarchy instances are all returned in a list.
     *
     * @param  hierarchy     The hierarchy instance to query against.
     * @param  queryChildren <tt>true</tt> to also query against children of the instance.
     *
     * @return A list matching hierarchy instances, or an empty list if none are found.
     */
    public List<HierarchyInstance> retreiveHierarchyInstances(HierarchyInstance hierarchy, boolean queryChildren);

    /**
     * Creates a new persistent entity.
     *
     * @param element The transient entity to create as a persistent entity.
     */
    public void createEntityInstance(EntityInstance element);

    /**
     * Fetches an entity of the specified type by its internal id.
     *
     * @param  entityType The type of the entity to fetch.
     * @param  id         The id of the element to fetch.
     *
     * @return The entity matching the entity type and id, or <tt>null</tt> if no match can be found.
     */
    public EntityInstance retrieveEntityInstance(EntityType entityType, InternalId id);

    /**
     * Looks up an external id and resolves it into the entity with that id.
     *
     * @param  id The external id to look up.
     *
     * @return An entity with matching external id, or <tt>null</tt> if no match can be found.
     */
    public EntityInstance retrieveByExternalId(String id);

    /**
     * Overwrites an existing entity with a new value.
     *
     * @param element The new value to overwrite the entity with.
     */
    public void updateEntityInstance(EntityInstance element);

    /**
     * Permanently deletes an entity, specified by its internal id.
     *
     * @param entityType The type of entity to delete.
     * @param id         The id of the element to delete.
     */
    public void deleteEntityInstance(EntityType entityType, InternalId id);

    /**
     * Provides a listing by entity type of views of entities matching a set of named attributes. The attributes do not
     * have to be fully specified, range and wild-card attributes are accepted. Any attribute that is a member of an
     * entity, and is not specified at all in the query will match any value of that attribute in the returned entities.
     * A view type is passed to specify the view onto the entity that is to be returned by the browse operation,
     * allowing a subset of the entities fields to be retrieved in order to provide a browsable summary of the available
     * entities. Only entities that conform to the specified view will be returned.
     *
     * @param  matchings    The attributes to match.
     * @param  viewTypeName The name of the view type to match and return.
     *
     * @return A map from entity types to matching entities.
     */
    public Map<EntityType, List<ViewInstance>> browse(Map<String, Attribute> matchings, String viewTypeName);

    /**
     * Provides a listing by entity type of views of entities matching a set of named attributes. The attributes do not
     * have to be fully specified, range and wild-card attributes are accepted. Any attribute that is a member of an
     * entity, and is not specified at all in the query will match any value of that attribute in the returned entities.
     * A view type is passed to specify the view onto the entity that is to be returned by the browse operation,
     * allowing a subset of the entities fields to be retrieved in order to provide a browsable summary of the available
     * entities. Only entities that conform to the specified view will be returned.
     *
     * @param  entityType   The type of entity to restrict the results to.
     * @param  matchings    The attributes to match.
     * @param  viewTypeName The name of the view type to match and return.
     *
     * @return A map from entity types to matching entities.
     */
    public List<ViewInstance> browse(EntityType entityType, Map<String, Attribute> matchings, String viewTypeName);

    /**
     * Performs a free text search over indexed entities, returning views onto all entities that conform to the
     * specified view type.
     *
     * @param  indexName The name of the index to query.
     * @param  query     The free text query.
     * @param  view      The type of the summary view to search over.
     *
     * @return A list of views onto all matching entities that conform to the specified view type.
     */
    public List<ViewInstance> freeTextSearch(String indexName, String query, ViewType view);

    /**
     * Performs a free text search over indexed entities, returning views onto all entities that conform to the
     * specified view type, grouped by entity type.
     *
     * @param  indexName The name of the index to query.
     * @param  query     The free text query.
     * @param  view      The type of the summary view to search over.
     *
     * @return A map of views onto all matching entities that conform to the specified view type, grouped by entity
     *         type.
     */
    public Map<ComponentType, List<ViewInstance>> freeTextSearchByEntityType(String indexName, String query,
        ViewType view);

    /**
     * Executes a query specified in parts and returns the results in pages. The query to execute consists of an entity
     * name to query on, optional criterion to apply to that entity, and optional joined entity names and criterion to
     * restrict by. This is built into two criteria to be exceuted against the current session; one to count how many
     * rows the result will contain and one to fetch a single page of those results.
     *
     * @param  from               The index to get from (the start of the page).
     * @param  number             The number of results to return (the size of the page).
     * @param  databaseEntityName The database entity to query.
     * @param  entityTypeName     The type name of the entity to query.
     * @param  viewTypeName       The view type to project the results onto.
     * @param  criterion          The optional criterion to apply to the entity.
     * @param  joins              A map of related entities and criterion to restrict the query by.
     *
     * @return A list of dimension element summaries.
     */
    public PagingResult executePagedQuery(int from, int number, String databaseEntityName, String entityTypeName,
        String viewTypeName, Criterion criterion, Map<String, Criterion> joins);

    /** Causes all indexes in the catalogue to be brough up-to-date with their entity data. */
    public void rebuildIndexes();
}
