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
package com.thesett.index.prototype;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.thesett.index.IndexSetup;
import com.thesett.index.IndexStore;
import com.thesett.index.TransactionalIndex;

/**
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Map names to indexes.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class ProtoIndexStore implements IndexStore, Serializable
{
    /* Holds a reference to the singleton instance of this class. */
    //private static ProtoIndexStore singleton;

    /** Holds references to the named indexes. */
    private static final Map<String, ProtoIndex> indexStore = new HashMap<String, ProtoIndex>();

    /** Private constructor to ensure that only singleton is ever created. */
    public ProtoIndexStore()
    {
    }

    /*
     * Gets a reference to an instance of the index store.
     *
     * @return A reference to an instance of the index store.
     */
    /*public static synchronized IndexStore getInstance()
    {
        if (singleton == null)
        {
            singleton = new ProtoIndexStore();
        }

        return singleton;
    }*/

    /**
     * Retrieves a handle to the named index. If an index with this name does not already exist then a new one is
     * created.
     *
     * @param  indexName The name of the index to retrieve.
     *
     * @return The named index.
     */
    public TransactionalIndex getNamedIndex(String indexName)
    {
        // Ensure that the named index has been created.
        if (!indexStore.containsKey(indexName))
        {
            indexStore.put(indexName, new ProtoIndex());
        }

        // Return a reference to the named index.
        return indexStore.get(indexName);
    }

    /**
     * Retrieves the named indexes setup instance.
     *
     * @param  indexName The name of the index to get the setup instance for.
     *
     * @return The indexes setup instance.
     */
    public IndexSetup getNamedIndexSetup(String indexName)
    {
        // Ensure that the named index has been created.
        if (!indexStore.containsKey(indexName))
        {
            indexStore.put(indexName, new ProtoIndex());
        }

        // Return a reference to the named index.
        return indexStore.get(indexName);
    }
}
