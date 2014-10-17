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
package com.thesett.catalogue.impl.standalone;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.thesett.catalogue.core.CatalogueManipulatorBase;
import com.thesett.catalogue.model.Catalogue;
import com.thesett.catalogue.model.ComponentInstance;
import com.thesett.catalogue.model.EntityInstance;
import com.thesett.catalogue.model.EntityType;
import com.thesett.catalogue.model.ExternalId;
import com.thesett.catalogue.model.ExternallyIdentified;
import com.thesett.catalogue.model.HierarchyInstance;
import com.thesett.catalogue.model.InternalId;
import com.thesett.catalogue.model.PagingResult;
import com.thesett.catalogue.model.ViewInstance;
import com.thesett.catalogue.model.ViewType;
import com.thesett.catalogue.model.base.EntityViewInstanceBase;
import com.thesett.catalogue.model.impl.InternalIdImpl;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Conjunction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.ResultTransformer;

import com.thesett.aima.attribute.impl.HierarchyAttribute;
import com.thesett.aima.attribute.impl.HierarchyAttributeFactory;
import com.thesett.aima.attribute.impl.HierarchyType;
import com.thesett.aima.state.Attribute;
import com.thesett.aima.state.ComponentType;
import com.thesett.aima.state.Type;
import com.thesett.catalogue.config.CatalogueConfigBean;
import com.thesett.catalogue.config.CatalogueConfigBeanImpl;
import com.thesett.catalogue.hibernate.HibernateUtil;
import com.thesett.catalogue.model.CatalogueManagerService;
import com.thesett.common.config.ConfigBeanContext;
import com.thesett.common.config.ConfigException;
import com.thesett.common.config.Configurator;
import com.thesett.common.util.ReflectionUtils;
import com.thesett.common.util.StringUtils;
import com.thesett.index.Index;
import com.thesett.index.IndexUnknownKeyException;
import com.thesett.index.config.IndexStoreConfigBean;

/**
 * CatalogueManagerServiceImpl provides a standalone implementation of the {@link CatalogueManagerService} that does not
 * run under an application server and handles its own transactions.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Perform CRUD operations on dimension elements.
 * <tr><td> Perform catalogue queries by hierarchical attributes.
 * <tr><td> Perform free text catalogue searches.
 * <tr><td> Perform element searches by name.
 * <tr><td> Generate long-lived external ids.
 * </table></pre>
 *
 * @author Rupert Smith
 * @todo   Remove the hard coding of the results list block size. Make it a configurable property.
 */
public class CatalogueManagerServiceImpl extends CatalogueManipulatorBase implements CatalogueManagerService
{
    /** Used for logging. */
    private static final Logger log = Logger.getLogger(CatalogueManagerServiceImpl.class);

    /** Temporary hard coding of the block size for query result lists. */
    protected static final int BLOCK_SIZE = 20;

    /** Creates a standalone instance of the {@link CatalogueManagerService}. */
    public CatalogueManagerServiceImpl()
    {
        // Get the configurator and extract the required catalogue config bean from it.
        ConfigBeanContext configBeanContext = Configurator.lookupConfigurator();

        // Extract and keep the catalogue from the catalogue config bean.
        try
        {
            CatalogueConfigBean catalogueBean =
                (CatalogueConfigBean) configBeanContext.getConfiguredBean(CatalogueConfigBeanImpl.class.getName());
            setCatalogue(catalogueBean.getCatalogue());

            // Extract and keep a reference to the index store.
            IndexStoreConfigBean indexStoreBean =
                (IndexStoreConfigBean) configBeanContext.getConfiguredBean(
                    "com.thesett.index.config.IndexStoreConfigBean");
            setIndexStore(indexStoreBean.getIndexStore());
        }
        catch (ConfigException e)
        {
            throw new RuntimeException(e);
        }
    }

    /** {@inheritDoc} */
    public void createHierarchyInstance(HierarchyInstance hierarchy)
    {
        log.debug("public void createHierarchyInstance(State hierarchy): called");
        log.debug("hierarchy = " + hierarchy);

        Session session = HibernateUtil.getCurrentSession();
        HibernateUtil.beginTransaction();

        session.save(hierarchy);
    }

    /** {@inheritDoc} */
    public void deleteHierarchyInstance(HierarchyType type, InternalId id)
    {
        log.debug("public void deleteHierarchyInstance(HierarchyAttribute.HierarchyType type, InternalId id): called");

        Session session = HibernateUtil.getCurrentSession();
        HibernateUtil.beginTransaction();

        // Cast the id to expose its known implementation type.
        InternalIdImpl idImpl = (InternalIdImpl) id;

        // Look the hierarchy instance up in the normalized online database by its id.
        String hierarchyClassName = StringUtils.toCamelCaseUpper(type.getName());
        Class hierarchyClass = ReflectionUtils.forName(getCatalogue().getModelPackage() + "." + hierarchyClassName);

        HierarchyInstance result = (HierarchyInstance) session.get(hierarchyClass, idImpl.getValue());

        log.debug("hierarchy instance to delete = " + result);
        session.delete(result);
    }

    /** {@inheritDoc} */
    public List<HierarchyInstance> retreiveHierarchyInstances(HierarchyInstance hierarchy, boolean queryChildren)
    {
        List<HierarchyInstance> result = new LinkedList<HierarchyInstance>();

        // Get name of hierarchy type to query.
        String hierarchyTypeName = hierarchy.getHierarchyType().getName();

        // Work out the database table name for the hierarchy.
        String databaseEntityName =
            getCatalogue().getModelPackage() + "." + StringUtils.toCamelCaseUpper(hierarchyTypeName);

        Session session = HibernateUtil.getCurrentSession();
        HibernateUtil.beginTransaction();

        Criteria selectCriteria = session.createCriteria(databaseEntityName);

        // Build up the query criteria.
        Conjunction subCriterion = Restrictions.conjunction();

        // Get the level names in the hierarchy.
        HierarchyAttribute hierarchyAttribute = hierarchy.getHierarchy();
        HierarchyAttributeFactory factory = hierarchyAttribute.getFactory();
        String[] levelNames = factory.getLevelNames();

        // Build up the property matching clause for this restriction. Levels defined in the restricting
        // hierarchy must be matched exactly on the joined hierarchy property.
        for (String level : levelNames)
        {
            String value = hierarchyAttribute.getValueAtLevel(level);

            // Only add restrictions for non null values specified in the grouping hierarchy.
            if (value != null)
            {
                subCriterion.add(Restrictions.eq(StringUtils.toCamelCase(hierarchyTypeName) + "." + level, value));
            }
        }

        selectCriteria.add(subCriterion);

        return selectCriteria.list();
    }

    /** {@inheritDoc} */
    public void createEntityInstance(EntityInstance element)
    {
        log.debug("public void createEntityInstance(DimensionElement element): called");
        log.debug("element = " + element);

        Session session = HibernateUtil.getCurrentSession();
        HibernateUtil.beginTransaction();

        // Set up its external id if it requires one.
        if (element.getComponentType().isExternalId())
        {
            if (((ExternallyIdentified) element).getExternalId() == null)
            {
                // Store the dimension name as the external ids resource type.
                String resourceName = element.getComponentType().getName();

                // Create a new external id with a null primary key as hibernate will generate that.
                ExternalId externalId = new ExternalId(null, resourceName);

                // Set up the new external if on the dimension element and save it.
                ((ExternallyIdentified) element).setExternalId(externalId);
                session.save(externalId);
            }
        }

        // Store the new dimension element in the normalized online database.
        session.save(element.getComponentType().getName() + Catalogue.ONLINE_TABLE_EXT, element);

        // Check if it needs indexing and index it if so.
        List<String> indexes = getCatalogue().getIndexesForDimension(element.getComponentType().getName());

        if (indexes != null)
        {
            for (String index : indexes)
            {
                addToIndex(index, ((ExternallyIdentified) element).getExternalId(), element);
            }
        }
    }

    /** {@inheritDoc} */
    public EntityInstance retrieveEntityInstance(EntityType entityType, InternalId id)
    {
        Session session = HibernateUtil.getCurrentSession();
        HibernateUtil.beginTransaction();

        // Cast the id to expose its known implementation type.
        InternalIdImpl idImpl = (InternalIdImpl) id;

        // Look the dimension element up in the normalized online database by its id.
        return (EntityInstance) session.get(entityType.getName() + Catalogue.ONLINE_TABLE_EXT, idImpl.getValue());
    }

    /** {@inheritDoc} */
    public void updateEntityInstance(EntityInstance element)
    {
        log.debug("public void updateEntityInstance(EntityInstance element): called");
        log.debug("element = " + element);

        Session session = HibernateUtil.getCurrentSession();
        HibernateUtil.beginTransaction();

        // Store the new dimension element in the normalized online database.
        session.saveOrUpdate(element.getComponentType().getName() + Catalogue.ONLINE_TABLE_EXT, element);

        // Check if it needs indexing and index it if so.
        List<String> indexes = getCatalogue().getIndexesForDimension(element.getComponentType().getName());

        if (indexes != null)
        {
            try
            {
                for (String index : indexes)
                {
                    updateIndex(index, ((ExternallyIdentified) element).getExternalId(), element);
                }
            }
            catch (IndexUnknownKeyException e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    /** {@inheritDoc} */
    public void deleteEntityInstance(EntityType dimension, InternalId id)
    {
        log.debug("public void deleteEntityInstance(EntityType dimension, InternalId id): called");

        Session session = HibernateUtil.getCurrentSession();
        HibernateUtil.beginTransaction();

        // Cast the id to expose its known implementation type.
        InternalIdImpl idImpl = (InternalIdImpl) id;

        // Look the dimension element up in the normalized online database by its id.
        EntityInstance result =
            (EntityInstance) session.get(dimension.getName() + Catalogue.ONLINE_TABLE_EXT, idImpl.getValue());
        session.delete(result);

        // Check if it needs indexing and index it if so.
        List<String> indexes = getCatalogue().getIndexesForDimension(dimension.getName());

        if (indexes != null)
        {
            try
            {
                for (String index : indexes)
                {
                    removeFromIndex(index, ((ExternallyIdentified) result).getExternalId());
                }
            }
            catch (IndexUnknownKeyException e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    /** {@inheritDoc} */
    public Map<EntityType, List<ViewInstance>> browse(Map<String, Attribute> matchings, String viewTypeName)
    {
        log.debug(
            "public Map<EntityType, List<ViewInstance>> browse(Map<String, Attribute> groupings, ViewType view): called");

        // Ensure that a view type has been specified.
        if (viewTypeName == null)
        {
            throw new IllegalArgumentException("The 'view' parameter must not be null.");
        }

        HibernateUtil.beginTransaction();

        ViewType viewType = getCatalogue().getViewType(viewTypeName);

        // Get all entities in the catalogue and then filter down to just those that match the specified set of field
        // names and types, and conform to the specified view type.
        Collection<EntityType> allEntities = getCatalogue().getAllEntityTypes();
        Collection<EntityType> entitiesMatchingFields = filterEntitiesMatchingFields(allEntities, matchings);
        Collection<EntityType> entitiesMatchingViews = filterEntitiesMatchingViews(entitiesMatchingFields, viewType);

        // Run a query against each matching entity type to build up the results.
        Map<EntityType, List<ViewInstance>> results = new HashMap<EntityType, List<ViewInstance>>();

        for (EntityType nextEntityType : entitiesMatchingViews)
        {
            List<ViewInstance> nextResult = browse(nextEntityType, matchings, viewTypeName);

            // Check that it actually contains some matches before adding it to the results.
            if (!nextResult.isEmpty())
            {
                results.put(nextEntityType, nextResult);
                log.debug("Got results for dimension: " + nextEntityType + ".");
            }
        }

        return results;
    }

    /** {@inheritDoc} */
    public List<ViewInstance> browse(EntityType entityType, Map<String, Attribute> matchings, String viewTypeName)
    {
        HibernateUtil.beginTransaction();

        // Ensure that a view type has been specified.
        if (viewTypeName == null)
        {
            throw new IllegalArgumentException("The 'viewTypeName' parameter must not be null.");
        }

        ViewType viewType = getCatalogue().getViewType(viewTypeName);

        // Get the name of the entity table to fetch the matching entity from.
        String entityTableName = entityType.getName() + Catalogue.ONLINE_TABLE_EXT;
        String entityTypeName = entityType.getName();

        // Check that the specified entity type contains attributes of the correct type to match the query.
        for (String propName : matchings.keySet())
        {
            // Get the type name of the attribute in the parameter.
            Attribute attribute = matchings.get(propName);
            String attributeTypeName = attribute.getType().getName();

            // Get the type name of the field in the entity.
            Type type = entityType.getPropertyType(propName);

            if (type != null)
            {
                String fieldTypeName = type.getName();

                // Check that they are compatible.
                if (!attributeTypeName.equals(fieldTypeName))
                {
                    throw new IllegalArgumentException("The type of query parameter " + propName + " is " +
                        attributeTypeName + " which is not compatibale with the field of type " + fieldTypeName +
                        " on entity type " + entityType.getName());
                }
            }
            else
            {
                throw new IllegalArgumentException("The query parameter " + propName +
                    " does not match any field name of entity type " + entityType.getName());
            }
        }

        // Get the hibernate query criterions for the requested attribute matchings.
        Map<String, Criterion> joins = getByAttributeCriterions(matchings);

        // Build a paged list to fetch the results on demand.
        SummaryList results =
            new SummaryList(0, BLOCK_SIZE, entityTableName, entityTypeName, viewTypeName, null, joins, this, isLocal());

        // Ensure that the first page is pre-fetched in the list.
        // The use local flag is forced on for this call as obviously a local call can be made to this class at
        // this point in time.
        boolean useLocal = results.isUseLocal();
        results.setUseLocal(true);
        results.cacheBlock(0);
        results.setUseLocal(useLocal);

        return results;
    }

    /** {@inheritDoc} */
    public List<ViewInstance> freeTextSearch(String indexName, String query, ViewType view)
    {
        log.debug(
            "public Map<Dimension, List<DimensionElementSummary>> freeTextSearch(String indexName, String query): called");

        // Get an index, or index connection for the named index.
        Index<ExternalId, ComponentInstance, ViewInstance> index = getIndex(indexName);

        // Pass the free text query to it.
        Map<ExternalId, ViewInstance> searchResults = index.search(query);

        // Return the results as a list.
        return new ArrayList<ViewInstance>(searchResults.values());
    }

    /** {@inheritDoc} */
    public Map<ComponentType, List<ViewInstance>> freeTextSearchByEntityType(String indexName, String query,
        ViewType view)
    {
        log.debug(
            "public Map<Dimension, List<DimensionElementSummary>> freeTextSearch(String indexName, String query): called");

        // Get an index, or index connection for the named index.
        Index<ExternalId, ComponentInstance, ViewInstance> index = getIndex(indexName);

        // Pass the free text query to it.
        Map<ExternalId, ViewInstance> searchResults = index.search(query);

        // Re-organize the results by dimension.
        Map<ComponentType, List<ViewInstance>> results = new HashMap<ComponentType, List<ViewInstance>>();

        for (ViewInstance summary : searchResults.values())
        {
            // Get the entity type from the summary.
            ComponentType componentType = summary.getComponentType();

            // Insert the dimension into the results if not there already.
            List<ViewInstance> listToAddTo = results.get(componentType);

            if (listToAddTo == null)
            {
                listToAddTo = new ArrayList<ViewInstance>();
                results.put(componentType, listToAddTo);
            }

            // Add the result to the list for the dimension.
            listToAddTo.add(summary);
        }

        return results;
    }

    /** {@inheritDoc} */
    public PagingResult executePagedQuery(int from, int number, String databaseEntityName, String entityTypeName,
        String viewTypeName, Criterion criterion, Map<String, Criterion> joins)
    {
        log.debug("public PagingResult executePagedQuery(int from = " + from + ", int number = " + number +
            ", String databaseEntityName = " + databaseEntityName + ", String entityTypeName = " + entityTypeName +
            ", String viewTypeName = " + viewTypeName + ", Criterion criterion, " +
            "Map<String, Criterion> joins): called");

        Session session = HibernateUtil.getCurrentSession();
        HibernateUtil.beginTransaction();

        // Project the id and external id properties and just the remaining properties that are required to project
        // the results onto the specified view type.
        ProjectionList properties =
            Projections.projectionList().add(Projections.id()).add(Property.forName("externalId"));

        ViewType viewType = getCatalogue().getViewType(viewTypeName);

        for (String fieldName : viewType.getAllPropertyTypes().keySet())
        {
            properties.add(Property.forName(fieldName));
        }

        // Create the selection criteria for the block.
        Criteria selectCriteria = session.createCriteria(databaseEntityName);

        if (criterion != null)
        {
            selectCriteria.add(criterion);
        }

        if (joins != null)
        {
            for (Map.Entry<String, Criterion> entry : joins.entrySet())
            {
                String joinEntity = entry.getKey();
                Criterion joinCriterion = entry.getValue();

                selectCriteria.createCriteria(joinEntity).add(joinCriterion);
            }
        }

        selectCriteria.setProjection(properties).setFirstResult(from).setMaxResults(number).setResultTransformer(
            new ViewInstanceTransformer(viewType, entityTypeName));

        // Create the count criteria.
        Criteria countCriteria = session.createCriteria(databaseEntityName);

        if (criterion != null)
        {
            countCriteria.add(criterion);
        }

        if (joins != null)
        {
            for (Map.Entry<String, Criterion> entry : joins.entrySet())
            {
                String joinEntity = entry.getKey();
                Criterion joinCriterion = entry.getValue();

                countCriteria.createCriteria(joinEntity).add(joinCriterion);
            }
        }

        countCriteria.setProjection(Projections.rowCount());

        // Run a query to find out how many results there will be and update the list size.
        int count = (Integer) countCriteria.uniqueResult();

        // Execute the query to get the block.
        List<ViewInstance> results = selectCriteria.list();

        return new PagingResult(count, results);
    }

    /**
     * Looks up an external id and resolves it into the dimension element with that id.
     *
     * @param  externalId The external dimension element id.
     *
     * @return A the dimension element with matching external id, or null if no match can be found.
     */
    public EntityInstance retrieveByExternalId(String externalId)
    {
        Session session = HibernateUtil.getCurrentSession();
        HibernateUtil.beginTransaction();

        // Look up the external id by its string representation.
        ExternalId id = (ExternalId) session.get(ExternalId.class, externalId);

        // Check that a matching external id was found.
        if (id != null)
        {
            // Get the dimension that the external id's resource class matches.
            ComponentType dimension = getCatalogue().getComponentType(id.getResource());

            // Create a query on the dimension for the matching external id.
            Criteria selectCriteria =
                session.createCriteria(dimension.getName() + Catalogue.ONLINE_TABLE_EXT).createAlias("externalId", "e")
                .add(Expression.eq("e.id", id.getId()));

            // Return the unique result or null if there are no matches.
            return (EntityInstance) selectCriteria.uniqueResult();
        }
        else
        {
            return null;
        }
    }

    /**
     * Causes all indexes in the catalogue to be emptied and re-built from their dimensional data.
     *
     * <p/>A fetch size could be set on the selectCriteria to batch fetch dimensional data in chunks in the most
     * efficient way by experimenting with different sizes. This should already be set to an optimal value on the
     * underlying JDBC driver so this is not done. In any case, list is used rather than iterate as iterate would use an
     * N + 1 strategy.
     */
    public void rebuildIndexes()
    {
        log.debug("public void rebuildIndexes(): called");

        Session session = HibernateUtil.getCurrentSession();
        HibernateUtil.beginTransaction();

        rebuildIndexesInSession(session);
    }

    /**
     * Build a map of entity field names and criterion to apply to them in order to select entities by the specified
     * attributes.
     *
     * @param  matchings The attributes to match.
     *
     * @return A map of entity field names and criterion to apply to them.
     */
    protected Map<String, Criterion> getByAttributeCriterions(Map<String, Attribute> matchings)
    {
        Map<String, Criterion> criterions = new HashMap<String, Criterion>();

        // Loop over all the attribute matchings to create criterions for.
        for (String propName : matchings.keySet())
        {
            // Get the attribute type name of the parameter to match against.
            Attribute attribute = matchings.get(propName);
            String attributeTypeName = attribute.getType().getName();

            if (attribute instanceof HierarchyAttribute)
            {
                // Create join criterions for selecting entities by hierarchies.
                // One join criterion, plus entity name, will be created for each property to be restricted by hierarchy.
                HierarchyAttribute hierarchyAttribute = (HierarchyAttribute) attribute;
                HierarchyAttributeFactory factory = hierarchyAttribute.getFactory();

                // Get the next property to restrict by attribute value and create a criterion to restrict on that property.
                Conjunction subCriterion = Restrictions.conjunction();
                criterions.put(propName, subCriterion);

                // Get the level names in the hierarchy.
                String[] levelNames = factory.getLevelNames();

                // Build up the property matching clause for this restriction. Levels defined in the restricting
                // hierarchy must be matched exactly on the joined hierarchy property.
                for (String level : levelNames)
                {
                    String value = hierarchyAttribute.getValueAtLevel(level);

                    // Only add restrictions for non null values specified in the grouping hierarchy.
                    if (value != null)
                    {
                        subCriterion.add(Restrictions.eq(attributeTypeName + "." + level, value));
                    }
                }
            }
        }

        return criterions;
    }

    /**
     * Creates the criterion for querying dimension elements by a particular name.
     *
     * @param  name The name to query for.
     *
     * @return A criterion to restrict to the specified name.
     */
    protected Criterion getByNameCriterion(String name)
    {
        // Create the criterion for selecting dimension element summaries by name.
        Criterion byNameCriterion = Expression.eq("name", name);

        return byNameCriterion;
    }

    /**
     * Reports whether or not this service is local.
     *
     * @return Always <tt>true</tt>.
     */
    protected boolean isLocal()
    {
        return true;
    }

    /**
     * Filters a collection of entities down to those that have fields of matching name and type to a specified set of
     * fields.
     *
     * @param  entities  The set of entities to filter.
     * @param  matchings The set of field names and types to match.
     *
     * @return A collection of entities from the original set that match the specified fields.
     */
    private Collection<EntityType> filterEntitiesMatchingFields(Collection<EntityType> entities,
        Map<String, Attribute> matchings)
    {
        log.debug(
            "private Collection<EntityType> filterEntitiesMatchingFields(Collection<EntityType> entities, Map<String, Attribute> matchings): called");

        // Build a list of all entity types that contain the named attributes as field with matching name and type.
        List<EntityType> results = new ArrayList<EntityType>();

        for (EntityType entityType : entities)
        {
            // Loop over all the properties and their attributes. An entity must match all before it is added to
            // the query. Start by assuming that the entity does match.
            boolean match = true;

            for (String propName : matchings.keySet())
            {
                // Get the attribute type name of the attribute in the parameter.
                Attribute attribute = matchings.get(propName);
                String attributeTypeName = attribute.getType().getName();

                // Get the attribute type name of the attribute in the entity.
                Type type = entityType.getPropertyType(propName);

                if (type != null)
                {
                    String entityTypeName = type.getName();

                    // Check that they are compatible.
                    if (!attributeTypeName.equals(entityTypeName))
                    {
                        // The entity parameter type does not match the type of the query.
                        match = false;

                        break;
                    }
                }
                else
                {
                    // The entity does not have a field with name matching the attribute name in the query.
                    match = false;

                    break;
                }
            }

            // Check if the entity type matched the query and add it to the list if so.
            if (match)
            {
                results.add(entityType);
                log.debug("Matched entity type: " + entityType + ".");
            }
        }

        return results;
    }

    /**
     * Filters a collection of entities down to those that conform to a specified view type.
     *
     * @param  entities The set of entities to filter.
     * @param  view     The view type to filter to.
     *
     * @return A collection of entities from the original set that conform to the specified view type.
     */
    private Collection<EntityType> filterEntitiesMatchingViews(Collection<EntityType> entities, ViewType view)
    {
        log.debug(
            "private Collection<EntityType> filterEntitiesMatchingViews(Collection<EntityType> entities, ViewType view): called");

        // Build a list of all entity types that conform to the specified view type.
        List<EntityType> results = new ArrayList<EntityType>();

        for (EntityType entityType : entities)
        {
            Set<ComponentType> ancestors = entityType.getImmediateAncestors();

            if (ancestors.contains(view))
            {
                results.add(entityType);
                log.debug("Matched entity type: " + entityType + ".");
            }
        }

        return results;
    }

    /**
     * ViewInstanceTransformer transforms results sets containing an Object array, consisting of a long id, external id,
     * and the fields that make up a particular view instance into a sub-class {@link ViewInstance} implementing the
     * specified view type.
     */
    public static class ViewInstanceTransformer implements ResultTransformer
    {
        /** Holds the view type to project onto. */
        ViewType viewType;

        /** The name of the dimension that the summary belongs to. */
        String entityTypeName;

        /**
         * Creates a result transformer to transform tuples from result sets into view instances.
         *
         * @param viewType       The view type to project onto.
         * @param entityTypeName The name of the entity type that the view is of.
         */
        public ViewInstanceTransformer(ViewType viewType, String entityTypeName)
        {
            this.viewType = viewType;
            this.entityTypeName = entityTypeName;
        }

        /**
         * Returns the input list with no transformation applied to it.
         *
         * @param  collection The input list.
         *
         * @return The input list untouched.
         */
        public List transformList(List collection)
        {
            return collection;
        }

        /**
         * Transforms the object tuple, { id, external id, ... } into a {@link ViewInstance} object.
         *
         * @param  tuple   The object tuple.
         * @param  aliases The alias names for the fields in the tuple. Ignored.
         *
         * @return An instance of ViewInstance.
         */
        public Object transformTuple(Object[] tuple, String[] aliases)
        {
            log.debug("public Object transformTuple(Object[] tuple, String[] aliases): called");

            Class viewImplClass = viewType.getBaseClass();
            log.debug("viewImplClass = " + viewImplClass);

            Class[] constructorArgTypes = new Class[tuple.length];

            /*for (int i = 0; i < (tuple.length); i++)
            {
                Object arg = tuple[i];
                log.debug("arg = " + arg);

                constructorArgTypes[i] = arg.getClass();

                log.debug("constructorArgTypes[ " + i + "] = " + constructorArgTypes[i]);
            }*/

            constructorArgTypes[0] = Long.class;
            constructorArgTypes[1] = ExternalId.class;

            int i = 2;

            for (Type argType : viewType.getAllPropertyTypes().values())
            {
                Object arg = tuple[i];

                if (null == arg)
                {
                    constructorArgTypes[i] = argType.getBaseClass();
                }
                else
                {
                    constructorArgTypes[i] = arg.getClass();
                }

                log.debug("constructorArgTypes[ " + i + "] = " + constructorArgTypes[i]);
                i++;
            }

            Constructor constructor = ReflectionUtils.getConstructor(viewImplClass, constructorArgTypes);
            EntityViewInstanceBase instance = (EntityViewInstanceBase) ReflectionUtils.newInstance(constructor, tuple);

            //return new EntityViewInstanceBase(entityTypeName, (Long) tuple[0], (ExternalId) tuple[1]);
            return instance;
        }
    }
}
