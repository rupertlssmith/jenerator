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
package com.thesett.catalogue.core;

import java.io.Serializable;

import com.thesett.catalogue.interfaces.InternalId;

/**
 * Implements entity ids as longs.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Uniquely identify entities.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class InternalIdImpl implements InternalId, Serializable
{
    /** The actual id. */
    private long id;

    /**
     * Creates an internal id for the specified value.
     *
     * @param id The id value.
     */
    public InternalIdImpl(long id)
    {
        this.id = id;
    }

    /**
     * Compares this id to another for equality.
     *
     * @param o The object to compare to.
     *
     * @return <tt>true</tt> if the comparator is also an internal id the same as this one.
     */
    public boolean equals(Object o)
    {
        return (o instanceof InternalIdImpl) && (((InternalIdImpl) o).id == id);
    }

    /**
     * Generates a hash code that conforms with the equality method.
     *
     * @return A hash code that conforms with the equality method.
     */
    public int hashCode()
    {
        return Long.valueOf(id).hashCode();
    }

    /**
     * Returns the value of the id.
     *
     * @return The value of the id.
     */
    public long getValue()
    {
        return id;
    }

    /**
     * Renders the id as a string, mainly for debugging purposes.
     *
     * @return The id as a string.
     */
    public String toString()
    {
        return "InternalIdImpl: [ id = " + id + " ]";
    }
}
