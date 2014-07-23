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
    private static Map<String, ProtoIndex> indexStore = new HashMap<String, ProtoIndex>();

    /**
     * Private constructor to ensure that only singleton is ever created.
     */
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
     * @param indexName The name of the index to retrieve.
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
     * @param indexName The name of the index to get the setup instance for.
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
