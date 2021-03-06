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
package com.thesett.index.tx;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Provides an implementation of the {@link IndexTxId} interface.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Act as identifier for a transaction.
 * <tr><td> Maintain transaction id validity.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class IndexTxIdImpl implements IndexTxId
{
    /** Holds the current transaction id generation sequence number. */
    private static long currentTxId = 1L;

    /** Holds the current set of valid transaction identifiers. These all correspond to 'live' transactions. */
    private static final Collection<IndexTxId> liveTransactions = new HashSet<IndexTxId>();

    /** Holds the transaction id number of this transaction id. */
    private final long txId;

    /** Creates a new index transaction id. */
    private IndexTxIdImpl()
    {
        txId = currentTxId++;
    }

    /**
     * Thread safe method to generate a new transaction id. The newly created id is considered live.
     *
     * @return A new and unique transaction id.
     */
    public static synchronized IndexTxId createIndexTxId()
    {
        // Create the new transaction id.
        IndexTxId id = new IndexTxIdImpl();

        // Add it to the live set.
        liveTransactions.add(id);

        return id;
    }

    /** Invalidates the transaction id. */
    public void invalidate()
    {
        // Remove this from the set of live transactions.
        liveTransactions.remove(this);

        // Inform the local transaction manager of the invalidation so that it can remove any external to internal
        // id mappings that it may have set up.
        IndexTxManager.invalidateTxId(this);
    }

    /**
     * Checks whether or not this transaction id is valid.
     *
     * @return <tt>true if this is a valid 'live' transaction id, <tt>false</tt> otherwise.
     */
    public boolean isValid()
    {
        return liveTransactions.contains(this);
    }

    /**
     * Checks if this index transaction id is the same as another one.
     *
     * @param  o The object to compare to.
     *
     * @return <tt>true</tt>If the comparator is also an index tx id the same as this one, <tt>false</tt> otherwise.
     */
    public boolean equals(Object o)
    {
        if (o == null)
        {
            return false;
        }

        if (o instanceof IndexTxIdImpl)
        {
            IndexTxIdImpl other = (IndexTxIdImpl) o;

            return other.txId == this.txId;
        }

        return false;
    }

    /**
     * Computes a hashCode of index tx ids to allow them to be used efficiently in hashing data structures.
     *
     * @return A hash code of the transaction id.
     */
    public int hashCode()
    {
        return Long.valueOf(txId).hashCode();
    }

    /**
     * Returns a string containing this transaction id. USed for debugging purposes.
     *
     * @return A string containing this transaction id. USed for debugging purposes.
     */
    public String toString()
    {
        return "" + txId;
    }
}
