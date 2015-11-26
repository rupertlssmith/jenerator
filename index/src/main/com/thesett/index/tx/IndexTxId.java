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

/**
 * IndexTxId is simply a marker object that is used to identify a transaction. Object already defines the
 * {@link #equals} and {@link #hashCode} methods but this interface restates them to formalize the properties of an
 * transaction id. This id also contains methods to invalidate the id and to test its validity. Once a transaction id
 * has been used it should be invalidated (at commit or rollback).
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Act as identifier for a transaction.
 * <tr><td> Maintain transaction id validity.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public interface IndexTxId
{
    /** Invalidates the transaction id. */
    void invalidate();

    /**
     * Checks whether or not this transaction id is valid.
     *
     * @return <tt>true if this is a valid 'live' transaction id, <tt>false</tt> otherwise.
     */
    boolean isValid();

    /**
     * Checks if this index transaction id is the same as another one.
     *
     * @param  o The object to compare to.
     *
     * @return <tt>true</tt>If the comparator is also an index tx id the same as this one, <tt>false</tt> otherwise.
     */
    boolean equals(Object o);

    /**
     * Computes a hashCode of index tx ids to allow them to be used efficiently in hashing data structures.
     *
     * @return A hash code of the transaction id.
     */
    int hashCode();
}
