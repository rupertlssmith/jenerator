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
package com.thesett.catalogue.core;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;

import com.thesett.aima.state.ComponentType;
import com.thesett.catalogue.interfaces.Catalogue;
import com.thesett.catalogue.interfaces.ComponentInstance;
import com.thesett.catalogue.interfaces.EntityInstance;
import com.thesett.catalogue.interfaces.ExternalId;
import com.thesett.catalogue.interfaces.ExternallyIdentified;
import com.thesett.catalogue.interfaces.ViewInstance;
import com.thesett.index.Index;
import com.thesett.index.IndexMappingException;
import com.thesett.index.IndexStore;
import com.thesett.index.IndexUnknownKeyException;

/**
 * CatalogueManipulatorBase extracts common functions for working with catalogue models.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class CatalogueManipulatorBase
{
    /** Used for logging. */
    private static final Logger log = Logger.getLogger(CatalogueManipulatorBase.class);

    /** Holds a reference to the index store. */
    private IndexStore indexStore;

    /** Used to hold the catalogue. */
    private Catalogue catalogue;

    /**
     * Gets the catalogue.
     *
     * @return The catalogue.
     */
    public Catalogue getCatalogue()
    {
        return catalogue;
    }

    /**
     * Sets the catalogue to use.
     *
     * @param catalogue The catalogue to use.
     */
    protected void setCatalogue(Catalogue catalogue)
    {
        this.catalogue = catalogue;
    }

    /**
     * Gets the index store.
     *
     * @return The index store.
     */
    protected IndexStore getIndexStore()
    {
        return indexStore;
    }

    /**
     * Sets the index store to use.
     *
     * @param indexStore The index store to use.
     */
    protected void setIndexStore(IndexStore indexStore)
    {
        this.indexStore = indexStore;
    }

    /**
     * Rebuilds all the indexes in the catalgoue in the specified hibernate session. Allowing the hibernate session to
     * be passed in means that different session setups can be used at config and run time.
     *
     * @param session The hibernate session to use.
     */
    protected void rebuildIndexesInSession(Session session)
    {
        // Empty all the indexes.
        for (String name : getCatalogue().getAllIndexes())
        {
            clearIndex(name);
            log.debug("Cleared index: " + name);
        }

        // Get a list of all dimensions and loop through them all.
        for (ComponentType dimension : getCatalogue().getAllComponentTypes())
        {
            // Check that the dimension requires indexing.
            List<String> indexesForDimension = getCatalogue().getIndexesForDimension(dimension.getName());

            if ((indexesForDimension != null) && !indexesForDimension.isEmpty())
            {
                // Extract all data rows from the dimension and loop over them.
                String entityName = dimension.getName() + Catalogue.ONLINE_TABLE_EXT;
                Criteria selectCriteria = session.createCriteria(entityName);

                for (Object entity : selectCriteria.list())
                {
                    EntityInstance element = (EntityInstance) entity;

                    // Loop over the list of all indexes that the dimension should be indexed in.
                    for (String indexName : indexesForDimension)
                    {
                        // Insert the element into the index.
                        addToIndex(indexName, ((ExternallyIdentified) element).getExternalId(), element);
                        log.debug("Re-indexed, " + element + ", in index " + indexName);
                    }
                }
            }
        }
    }

    /**
     * Fetches the named index from the index store.
     *
     * @param  indexName The name of the index to get.
     *
     * @return An index matching the specified name.
     */
    protected Index<ExternalId, ComponentInstance, ViewInstance> getIndex(String indexName)
    {
        return getIndexStore().getNamedIndex(indexName);
    }

    /**
     * Closes the index if needed.
     *
     * @param index The index to close.
     */
    protected void closeIndex(Index index)
    {
    }

    /**
     * Adds a record to the search index. Its indexed fields are extracted as strings before being added to the index
     * data structure.
     *
     * @param  indexName  The name of the index to update.
     * @param  key        A key that uniquely identifies the record to insert.
     * @param  fullRecord The full data record to build the index from, fields will be extracted from this record.
     *
     * @throws IndexMappingException If the record being added to the index cannot be extracted because no mapping
     *                               exists for it or if a field specified in a matching mapping cannot be found on the
     *                               object being mapped.
     *
     * @todo   Add a summary parameter.
     */
    protected void addToIndex(String indexName, ExternalId key, EntityInstance fullRecord) throws IndexMappingException
    {
        // Get a connection to the specified index.
        Index ic = getIndex(indexName);

        try
        {
            // Add the item to the index.
            ic.add(key, fullRecord, null);
        }
        finally
        {
            // Close the index if needed.
            closeIndex(ic);
        }
    }

    /**
     * Updates a record in the index. Its indexed fields are extracted from the full record again and the new index
     * entry replaces any existing entry for the specified key.
     *
     * @param  indexName  The name of the index to update.
     * @param  key        A key that uniquely identifies the record to update.
     * @param  fullRecord The full data record to build the index from, fields will be extracted from this record.
     *
     * @throws IndexMappingException    If the record being added to the index cannot be extracted because no mapping
     *                                  exists for it or if a field specified in a matching mapping cannot be found on
     *                                  the object being mapped.
     * @throws IndexUnknownKeyException When the key is not already in the index, or has been removed from it.
     *
     * @todo   Add a summary parameter.
     */
    protected void updateIndex(String indexName, ExternalId key, ComponentInstance fullRecord)
        throws IndexMappingException, IndexUnknownKeyException
    {
        // Get a connection to the sepcified index.
        Index ic = getIndex(indexName);

        try
        {
            // Delegate the method call to it.
            ic.update(key, fullRecord, null);
        }
        finally
        {
            // Close the connection.
            closeIndex(ic);
        }
    }

    /**
     * Removes a record from the search index.
     *
     * @param  indexName The name of the index to update.
     * @param  key       A key that uniquely identifies the record to remove.
     *
     * @throws IndexUnknownKeyException When the key is not already in the index, or has been removed from it.
     */
    protected void removeFromIndex(String indexName, ExternalId key) throws IndexUnknownKeyException
    {
        // Get a connection to the named index.
        Index ic = getIndex(indexName);

        try
        {
            // Delegate the method call to it.
            ic.remove(key);
        }
        finally
        {
            // Close the connection.
            closeIndex(ic);
        }
    }

    /**
     * Clears the named index.
     *
     * @param indexName The name of the index to clear.
     */
    protected void clearIndex(String indexName)
    {
        // Get a connection to the named index.
        Index ic = getIndex(indexName);

        try
        {
            // Delegate the method call to it.
            ic.clear();
        }
        finally
        {
            // Close the connection.
            closeIndex(ic);
        }
    }

    /**
     * Updates a record in the index without re-indexing it. Only the key of the record to index an the entry to be
     * returned on matching searches are updated. Fields are not extracted from the full record and the indexing is not
     * changed.
     *
     * @param  indexName  The name of the index to update.
     * @param  key        A key that uniquely identifies the record to update.
     * @param  indexEntry The data record to add to the index, this is the record that searches will return.
     *
     * @throws IndexMappingException    If the record being added to the index cannot be extracted because no mapping
     *                                  exists for it or if a field specified in a matching mapping cannot be found on
     *                                  the object being mapped.
     * @throws IndexUnknownKeyException When the key is not already in the index, or has been removed from it.
     */
    protected void updateIndex(String indexName, ExternalId key, ViewInstance indexEntry) throws IndexMappingException,
        IndexUnknownKeyException
    {
        // Get a connection to the test index.
        Index ic = getIndex(indexName);

        try
        {
            // Delegate the method call to it.
            ic.update(key, indexEntry);
        }
        finally
        {
            // Close the connection.
            closeIndex(ic);
        }
    }
}
