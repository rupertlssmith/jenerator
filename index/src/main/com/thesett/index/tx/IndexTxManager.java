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
package com.thesett.index.tx;

import java.util.HashMap;
import java.util.Map;

import javax.transaction.xa.Xid;

/**
 * IndexTxManager is a helper class for setting up local transaction ids on the current thread to assist in calling
 * {@link com.thesett.index.TransactionalIndex}es. When running in a transactional mode, a transactional index
 * expects to have its methods called with a valid transaction id attached to the current thread as a thread local
 * variable. Passing variables as thread locals means that the index methods do not have to be polluted with extra
 * parameters for transaction id passing, so transactional indexes can have exactly the same interface as
 * non-transactional ones. This class provides methods to create, assign and remove transaction ids to threads.
 *
 * <p/>Transactions may be coordinated by an external transaction manager which creates its own unique transaction
 * identifiers. This class provides a method {@link #assignTxIdToThread(Xid)} that maps such an external id onto
 * the internal one, creating a new internal one if one does not already exist for that external id. Subsequent calls
 * to this method for the same external id will result in the same internal id being reattached to the thread, unless
 * the mapping has been invalidated, in which case a new local id will be assigned.
 *
 * <p/>Local transactions not involiving an external transaction manager should use the {@link #createTxIdToThread}
 * method to create and assign local ids to the current thread.
 *
 * <p/>When a transaction id is invalidated because a transaction has been completed, the transaction id will
 * call back the {@link #invalidateTxId} method to notify this class of the invalidation. The response is to remove
 * any mapping from external transaction ids to local ones for the invalidated local id.
 *
 * <p/>The current transaction id may be removed from the current thread and re-attached at will using the
 * {@link #removeTxIdFromThread}, {@link #assignTxIdToThread(IndexTxId)} and {@link #assignTxIdToThread(Xid)} methods.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Provide a mapping from exernal transaction ids to local ones.
 * <tr><td> Generate local transaction ids.
 * <tr><td> Attach and detach local transaction ids on the current thread.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class IndexTxManager
{
    /** Used for logging. */
    //private static final Logger log = Logger.getLogger(IndexTxManager.class);

    /** Forward mapping of Xid to IndexTxId. */
    private static Map<Xid, IndexTxId> xidToIndexTxIdMapping = new HashMap<Xid, IndexTxId>();

    /** Reverse mapping of IndexTxId to Xid. */
    private static Map<IndexTxId, Xid> indexTxIdToXidMapping = new HashMap<IndexTxId, Xid>();

    /** Provides thread local variable assignment of index transaction ids. */
    private static ThreadLocal<IndexTxId> threadLocalTxId =
        new ThreadLocal<IndexTxId>()
        {
            /**
             * Ids are always initialized to null before being assigned.
             *
             * @return Always null.
             */
            protected synchronized IndexTxId initialValue()
            {
                return null;
            }
        };

    /**
     * Finds an existing mapping from external transaction id to local, or creates a new local transaction id for
     * the external one.
     *
     * @param xid The external transaction id to map to a local id.
     *
     * @return The mapped local transaction id.
     */
    public static IndexTxId mapXidToTxId(Xid xid)
    {
        //log.debug("public static IndexTxId mapXidToTxId(Xid xid): called");

        // Try to find the external id in a mapping.
        IndexTxId txId = xidToIndexTxIdMapping.get(xid);

        // No mapping exists for the external id so create a new local id for it.
        if (txId == null)
        {
            //log.debug("Could not find a mapped local tx id for the xid");

            // Create new local id.
            txId = IndexTxIdImpl.createIndexTxId();

            // Add it to the forward and reverse mappings.
            xidToIndexTxIdMapping.put(xid, txId);
            indexTxIdToXidMapping.put(txId, xid);

            //log.debug("Created mapping from xid = " + xid + ", to txId = " + txId);
        }
        /*else
        {
            //log.debug("Found mapping from xid = " + xid + ", to txId = " + txId);
        }*/

        // Return the new local id.
        return txId;
    }

    /**
     * Find an existing mapping from external transaction id to local, or creates a new local transaction id for
     * the external one. The local id is assigned to the current thread.
     *
     * @param xid The external transaction id to map to a local id.
     */
    public static void assignTxIdToThread(Xid xid)
    {
        //log.debug("public static void assignTxIdToThread(Xid xid): called");

        // Try to find the external id in a mapping.
        IndexTxId txId = xidToIndexTxIdMapping.get(xid);

        // No mapping exists for the external id so create a new local id for it.
        if (txId == null)
        {
            // Create new local id.
            txId = IndexTxIdImpl.createIndexTxId();

            // Add it to the forward and reverse mappings.
            xidToIndexTxIdMapping.put(xid, txId);
            indexTxIdToXidMapping.put(txId, xid);
        }

        // Assign the mapped local id to the current thread.
        assignTxIdToThread(txId);
    }

    /**
     * Called when a local transaction id is invalidated. This method removes any corresponding mapping to external
     * transaction ids.
     *
     * @param txId The local transaction id invalidated.
     */
    public static void invalidateTxId(IndexTxId txId)
    {
        //log.debug("public static void invalidateTxId(IndexTxId txId): called");

        // Try to find the local id in a mapping to an external id.
        Xid xid = indexTxIdToXidMapping.get(txId);

        if (xid != null)
        {
            // Remove the forward and reverse mappings.
            indexTxIdToXidMapping.remove(txId);
            xidToIndexTxIdMapping.remove(xid);
        }

        // No mapping found so do nothing.
    }

    /**
     * Creates a new local transaction id and assigns it to the current thread.
     *
     * @return The newly created local transaction id.
     */
    public static IndexTxId createTxIdToThread()
    {
        //log.debug("public static IndexTxId createTxIdToThread(): called");

        IndexTxId txId = IndexTxIdImpl.createIndexTxId();
        threadLocalTxId.set(txId);

        return txId;
    }

    /**
     * Creates a new local transaction id.
     *
     * @return The newly created local transaction id.
     */
    public static IndexTxId createTxId()
    {
        //log.debug("public static IndexTxId createTxId(): called");

        IndexTxId txId = IndexTxIdImpl.createIndexTxId();

        return txId;
    }

    /**
     * Attaches the specified local transaction id to the current thread.
     *
     * @param txId The transaction id to attach.
     */
    public static void assignTxIdToThread(IndexTxId txId)
    {
        //log.debug("public static void assignTxIdToThread(IndexTxId txId): called");
        //log.debug("txId = " + txId);

        threadLocalTxId.set(txId);
    }

    /**
     * Gets the currently assigned local transaction id from the current thread.
     *
     * @return The currently assigned local transaction id from the current thread.
     */
    public static IndexTxId getTxIdFromThread()
    {
        //log.debug("public static IndexTxId getTxIdFromThread(): called");

        return threadLocalTxId.get();
    }

    /**
     * Removes the currently assigned local transaction id from the current thread and returns its value.
     *
     * @return The currently assigned local transaction id from the current thread just prior to this method being
     *         called.
     */
    public static IndexTxId removeTxIdFromThread()
    {
        //log.debug("public static IndexTxId removeTxIdFromThread(): called");

        IndexTxId txId = threadLocalTxId.get();
        threadLocalTxId.remove();

        return txId;
    }
}
