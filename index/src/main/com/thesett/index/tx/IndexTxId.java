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
    /**
     * Invalidates the transaction id.
     */
    public void invalidate();

    /**
     * Checks whether or not this transaction id is valid.
     *
     * @return <tt>true if this is a valid 'live' transaction id, <tt>false</tt> otherwise.
     */
    public boolean isValid();

    /**
     * Checks if this index transaction id is the same as another one.
     *
     * @param o The object to compare to.
     *
     * @return <tt>true</tt>If the comparator is also an index tx id the same as this one, <tt>false</tt> otherwise.
     */
    public boolean equals(Object o);

    /**
     * Computes a hashCode of index tx ids to allow them to be used efficiently in hashing data structures.
     *
     * @return A hash code of the transaction id.
     */
    public int hashCode();
}
