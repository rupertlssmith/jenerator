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
package com.thesett.index;

/**
 * TransactionIndex is an extension of the {@link Index} interface to support transactional manipulation of indexes.
 * Transactional indexes support concurrent access to an index in such a way that changes made by simultaneous processes
 * appear atomically to each other and can isolate their visibility from each other. Other transactional properties,
 * such as saving the state permanently to disk, can also be supported.
 *
 * <p/>Transactional indexes support a transactional mode which is controlled by setting the desired transaction
 * isolation level. This is used where multiple processes are using the index simultanesouly and is particularly usefull
 * when used in conjunction with a database. It may be the case that records are stored in a database as well as cached
 * in an index for free text searching and in this case it is necessary to provide transactional support so that updates
 * can be made accross both the index and the database.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities
 * <tr><td> Accept setting of transaction isolation level.
 * <tr><td> Commit transactional changes to an index.
 * <tr><td> Rollback transactional changes to an index.
 * </table></pre>
 *
 * @todo The transactional mode stuff needs a bit of a rethink. Can't do it quite so simply. Must support multiple
 *       transactions making changes at the same time. Need to keep track of which transaction has made which changes
 *       and only commit those relevant to the particular transaction that commits. Need to think also about the isolation
 *       level. I think implement the simplest level that will do (or maybe consider how different levels can be supported
 *       under different configurations and just implement the one that is needed for now, leaving the others for later
 *       completion). Consider: T1 begins, T2 begins and updates index and commits, T1 reads index, can it see T2's
 *       changes? If so reads are not repeatable. Can T1 see T2's changes before T2 commits? If so reads are dirty. If T2
 *       creates new items in the index will T1 see them? not repeatable. Before T2 commits? phantom reads. I think T1
 *       should not see T2's changes until T2 commits but that non-repeatable reads will be acceptable. After all once T2
 *       commits then the data should be there so any further attempts to access it should be ok (provided of course that
 *       its isolation level is read commited too, if its serializable it won't be visible yet). That is to say that, read
 *       committed level should suffice.  An implementation will need to create a seperate data structure to hold the
 *       workings of a transaction in and on commit to merge that into the committed index.
 *
 * @todo How do methods know which transaction is running? Is this obtained from the current thread, a transaction manager
 *       or is it passed explicitly? The workings of this need to be understood and adjustments made to the method
 *       signatures if needed. An implementation will need to create a seperate data structure to hold the workings of
 *       a transaction in and on commit to merge that into the committed index.
 *
 * @todo Rename this as Transactional and make TransactionlIndex an extension of it an Index. Transactional is a
 *       re-usable concept.
 *
 * @author Rupert Smith
 */
public interface TransactionalIndex<K, D, E> extends Index<K, D, E>
{
    /** Defines the different transaction isolation levels. */
    public enum IsolationLevel
    {
        /** Used to indiciate the 'None' transaction isolation level. */
        None,

        /** Used to indiciate the 'ReadUncommitted' transaction isolation level. */
        ReadUncommitted,

        /** Used to indiciate the 'ReadCommitted' transaction isolation level. */
        ReadCommitted,

        /** Used to indiciate the 'RepeatableRead' transaction isolation level. */
        RepeatableRead,

        /** Used to indiciate the 'Serializable' transaction isolation level. */
        Serializable;
    }

    /**
     * Used to set the transaction isolation level.
     *
     * @param isolationLevel The transaction isolation level to support.
     */
    public void setTransactionalMode(IsolationLevel isolationLevel);

    /**
     * Gets the isolation level in force.
     *
     * @return The isolation level in force.
     */
    public IsolationLevel getTransationalMode();

    /**
     * When operating in transactional mode causes any changes since the last commit to be made visible to the
     * search method.
     */
    public void commit();

    /**
     * When operation in transactional mode causes any changes since the last commit to be dropped and never made
     * visible to the search method.
     */
    public void rollback();
}
