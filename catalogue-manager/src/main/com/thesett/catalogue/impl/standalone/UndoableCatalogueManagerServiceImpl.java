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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.thesett.catalogue.model.UndoableCatalogueManagerService;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;

import com.thesett.aima.attribute.impl.HierarchyType;
import com.thesett.aima.state.Attribute;
import com.thesett.aima.state.ComponentType;
import com.thesett.aima.state.Type;
import com.thesett.catalogue.hibernate.HibernateUtil;
import com.thesett.common.util.UndoStackBase;

/**
 * UndoableCatalogueManagerServiceImpl implements an undo stack over the {@link CatalogueManagerService}. The catalogue
 * service provides CRUD methods over entities defined in a catalogue model. Retreive operations are non-side effecting,
 * but create, update and delete operations all alter the state of the database. Create and delete are opposites so are
 * undone by respectively deleting or creating the entity. Update operations are undone by storing the original entity
 * that is written to and updating it back to the original upon undo.
 *
 * <p/>The undo stack concept is clear, but the implementation is complicated by several things. Fristly, some entities
 * have automatically generated ids, for example monotonic sequences, so a delete undone by a create will result in a
 * new entity with a different id to the original. Secondly, the underlying service may cache entities, leading to
 * issues where undone versions of entities may cause multiple different copies to exist in the cache.
 *
 * <p/>In this implementation the id problem is overcome by tracking changes to ids as entities are re-created.
 *
 * <p/>Carefull session management with hibernate is practised to ensure that temporary entities are not kept in the
 * cache, leading to a cleaner cache management and the avoidance of problems.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Restore catalogue state to a save point.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class UndoableCatalogueManagerServiceImpl extends UndoStackBase implements UndoableCatalogueManagerService
{
    /** Used for debugging purposes. */
    public static final Logger log = Logger.getLogger(UndoableCatalogueManagerServiceImpl.class);

    /** Used to track shifts in assigned ids for types with assigned ids. */
    private Map<Type, Map<InternalId, InternalId>> idsByType = new HashMap<Type, Map<InternalId, InternalId>>();

    /** Holds the catalogue manager service that is wrapped as an undo stack. */
    private CatalogueManagerService catalogueManagerService;

    /**
     * Wraps a catalogue manager service implementation as an undo stack.
     *
     * @param catalogueManagerService The catalogue manager implementation to wrap.
     */
    public UndoableCatalogueManagerServiceImpl(CatalogueManagerService catalogueManagerService)
    {
        this.catalogueManagerService = catalogueManagerService;
    }

    /** {@inheritDoc} */
    public Catalogue getCatalogue()
    {
        return catalogueManagerService.getCatalogue();
    }

    /** {@inheritDoc} */
    public void createHierarchyInstance(HierarchyInstance hierarchy)
    {
        // Create the hierarchy.
        catalogueManagerService.createHierarchyInstance(hierarchy);

        // Create an undo operation for the hierarchy creation.
        Undoable undoOperation = new UndoCreateHierarchy(hierarchy.getOpaqueId(), hierarchy);

        // Post the undo operation onto the undo stack.
        undoStack.offer(undoOperation);
    }

    /** {@inheritDoc} */
    public void deleteHierarchyInstance(HierarchyType type, InternalId id)
    {
        catalogueManagerService.deleteHierarchyInstance(type, id);
    }

    /** {@inheritDoc} */
    public List<HierarchyInstance> retreiveHierarchyInstances(HierarchyInstance hierarchy, boolean queryChildren)
    {
        return catalogueManagerService.retreiveHierarchyInstances(hierarchy, queryChildren);
    }

    /** {@inheritDoc} */
    public void createEntityInstance(EntityInstance element)
    {
        // Create the entity.
        catalogueManagerService.createEntityInstance(element);

        // Create an undo operation for the entity creation.
        Undoable undoOperation = new UndoCreateEntity(element.getOpaqueId(), element);

        // Post the undo operation onto the undo stack.
        undoStack.offer(undoOperation);
    }

    /** {@inheritDoc} */
    public EntityInstance retrieveEntityInstance(EntityType entityType, InternalId id)
    {
        return catalogueManagerService.retrieveEntityInstance(entityType, id);
    }

    /** {@inheritDoc} */
    public void updateEntityInstance(EntityInstance element)
    {
        // Create an undo operation for the entity update.
        Session session = HibernateUtil.getCurrentSession();
        EntityInstance instanceToUpdate = retrieveEntityInstance(element.getComponentType(), element.getOpaqueId());
        session.evict(instanceToUpdate);

        Undoable undoOperation = new UndoModifyEntity(instanceToUpdate.getOpaqueId(), instanceToUpdate);

        // Modify the entity.
        catalogueManagerService.updateEntityInstance(element);

        // Post the undo operation onto the undo stack.
        undoStack.offer(undoOperation);
    }

    /** {@inheritDoc} */
    public void deleteEntityInstance(EntityType entityType, InternalId id)
    {
        // Get a detached instance of the entity to delete, in order that it may be later recreated.
        Session session = HibernateUtil.getCurrentSession();
        EntityInstance instanceToDelete = retrieveEntityInstance(entityType, id);
        session.evict(instanceToDelete);

        // Delete the entity.
        catalogueManagerService.deleteEntityInstance(entityType, id);

        // Create an undo operation for the entity deletion.
        Undoable undoOperation = new UndoDeleteEntity(instanceToDelete.getOpaqueId(), instanceToDelete);

        // Post the undo operation onto the undo stack.
        undoStack.offer(undoOperation);
    }

    /** {@inheritDoc} */
    public Map<EntityType, List<ViewInstance>> browse(Map<String, Attribute> matchings, String viewTypeName)
    {
        return catalogueManagerService.browse(matchings, viewTypeName);
    }

    /** {@inheritDoc} */
    public List<ViewInstance> browse(EntityType entityType, Map<String, Attribute> matchings, String viewTypeName)
    {
        return catalogueManagerService.browse(entityType, matchings, viewTypeName);
    }

    /** {@inheritDoc} */
    public List<ViewInstance> freeTextSearch(String indexName, String query, ViewType view)
    {
        return catalogueManagerService.freeTextSearch(indexName, query, view);
    }

    /** {@inheritDoc} */
    public Map<ComponentType, List<ViewInstance>> freeTextSearchByEntityType(String indexName, String query,
        ViewType view)
    {
        return catalogueManagerService.freeTextSearchByEntityType(indexName, query, view);
    }

    /** {@inheritDoc} */
    public PagingResult executePagedQuery(int from, int number, String databaseEntityName, String entityTypeName,
        String viewTypeName, Criterion criterion, Map<String, Criterion> joins)
    {
        return catalogueManagerService.executePagedQuery(from, number, databaseEntityName, entityTypeName, viewTypeName,
            criterion, joins);
    }

    /** {@inheritDoc} */
    public EntityInstance retrieveByExternalId(String externalId)
    {
        return catalogueManagerService.retrieveByExternalId(externalId);
    }

    /** {@inheritDoc} */
    public void rebuildIndexes()
    {
        catalogueManagerService.rebuildIndexes();
    }

    /**
     * Gets the id tracking map for a given type, ensuring that if the type does not yet exist in the map, that it is
     * created.
     *
     * @param  type The type to get the id tracking map for.
     *
     * @return A map that tracks changing ids for types with assigned ids.
     */
    private Map<InternalId, InternalId> getIdMapForType(Type type)
    {
        Map<InternalId, InternalId> result = idsByType.get(type);

        if (result == null)
        {
            result = new HashMap<InternalId, InternalId>();
            idsByType.put(type, result);
        }

        return result;
    }

    /**
     * Provides the updated id for an entity, if its value has been shifted by re-create operations during the unwinding
     * of the undo stack. If no id translation has taken place, the original id will be returned.
     *
     * @param  instance The entity instance to get the current id of.
     * @param  id       The original id of the entity.
     *
     * @return The current id of an entity.
     */
    private InternalId getMappedIdForInstance(EntityInstance instance, InternalId id)
    {
        InternalId internalId;
        Map<InternalId, InternalId> idMap = getIdMapForType(instance.getComponentType());

        InternalId alteredId = idMap.get(instance.getOpaqueId());

        if (alteredId == null)
        {
            internalId = id;
        }
        else
        {
            internalId = alteredId;
            log.debug("Using altered id: " + alteredId);
        }

        return internalId;
    }

    /**
     * Defines the undo operation for created hierarchies.
     */
    private class UndoCreateHierarchy implements Undoable
    {
        /** Holds the original id of the database entity. */
        private InternalId originalId;

        /** Holds the created hierarchy to be deleted by the undo operation. */
        private HierarchyInstance hierarchyInstance;

        /**
         * Creates an undo operation that deletes the specified hierarchy element.
         *
         * @param hierarchy  The hierarchy element instance to delete upon undo.
         * @param originalId The original id of the database entity.
         */
        private UndoCreateHierarchy(InternalId originalId, HierarchyInstance hierarchy)
        {
            this.originalId = originalId;
            this.hierarchyInstance = hierarchy;
        }

        /** {@inheritDoc} */
        public void undo()
        {
            catalogueManagerService.deleteHierarchyInstance(hierarchyInstance.getHierarchyType(),
                hierarchyInstance.getOpaqueId());
            log.debug("Undo, delete hierarchy instance: " + hierarchyInstance);
        }
    }

    /**
     * Defines the undo operation for created entities.
     */
    private class UndoCreateEntity implements Undoable
    {
        /** Holds the original id of the database entity. */
        private InternalId originalId;

        /** Holds the created entity to be deleted by the undo operation. */
        private EntityInstance entityInstance;

        /**
         * Creates an undo operation that deletes the specified entity.
         *
         * @param entityInstance The entity instance to delete on undo.
         * @param originalId     The original id of the database entity.
         */
        private UndoCreateEntity(InternalId originalId, EntityInstance entityInstance)
        {
            this.originalId = originalId;
            this.entityInstance = entityInstance;
        }

        /** {@inheritDoc} */
        public void undo()
        {
            // The id may have changed during a creation process, if the entity has an autmatically managed id.
            // Check if an altered id for the entity to delete exists in the id map and use that instead if there
            // is one.
            InternalId internalId = getMappedIdForInstance(entityInstance, originalId);

            catalogueManagerService.deleteEntityInstance(entityInstance.getComponentType(), internalId);
            log.debug("Undo, deleted entity: " + entityInstance);
        }

    }

    /**
     * Defines the undo operation for deleted entities.
     */
    private class UndoDeleteEntity implements Undoable
    {
        /** Holds the original id of the database entity. */
        private InternalId originalId;

        /** Holds the deleted entity to be re-created by the undo operation. */
        private EntityInstance entityInstance;

        /**
         * Creates an undo operation that creates the specified entity.
         *
         * @param instanceToDelete The deleted entity instance to create on undo.
         * @param originalId       The original id of the database entity.
         */
        public UndoDeleteEntity(InternalId originalId, EntityInstance instanceToDelete)
        {
            this.originalId = originalId;
            this.entityInstance = instanceToDelete;
        }

        /** {@inheritDoc} */
        public void undo()
        {
            catalogueManagerService.createEntityInstance(entityInstance);

            // The id may have changed during the creation process, if the entity has an autmatically managed id.
            // Record any changes relative to its orignal id.
            Map<InternalId, InternalId> idMap = getIdMapForType(entityInstance.getComponentType());
            idMap.put(originalId, entityInstance.getOpaqueId());

            log.debug("Undo, created entity: " + entityInstance);
        }
    }

    /**
     * Defines the undo operation for modified entities.
     */
    private class UndoModifyEntity implements Undoable
    {
        /** Holds the original id of the database entity. */
        private InternalId originalId;

        /** Holds the modified entity to be restored by the undo operation. */
        private EntityInstance entityInstance;

        /**
         * Creates an undo operation that restores a modified entity upon undo.
         *
         * @param instanceToUpdate The entity to be updated, that should be restored.
         * @param originalId       The original id of the database entity.
         */
        public UndoModifyEntity(InternalId originalId, EntityInstance instanceToUpdate)
        {
            this.originalId = originalId;
            this.entityInstance = instanceToUpdate;
        }

        /** {@inheritDoc} */
        public void undo()
        {
            catalogueManagerService.updateEntityInstance(entityInstance);

            Session session = HibernateUtil.getCurrentSession();
            session.evict(entityInstance);

            log.debug("Undo, restored entity: " + entityInstance);
        }
    }
}
