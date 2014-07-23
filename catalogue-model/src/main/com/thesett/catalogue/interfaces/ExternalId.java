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
package com.thesett.catalogue.interfaces;

import java.io.Serializable;

/**
 * ExternalId defines an id type that is safe to pass to exernal systems because they are guaranteed to have a very long
 * life span and to not change or get issued to different data items.
 *
 * <p/>Items in the catalogue can map to a URL (or other externally nameable resource). Internally, items may have
 * database keys but these can and will change as items are deleted, merged, relegated and as the structure of the
 * database evolves. The sequences used to generate these surrogate keys may also be unique only within a single data
 * type and not across all data types. The idea behind external identifiers is that they will be unique across all
 * externally referenceable data types and that once issued they will live forever and never be re-issued for a
 * different item of data. When items are deleted permanently from the catalogue their external identifiers must be kept
 * to ensure that they are never re-issued.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities
 * <tr><td> Provide a long-lived identifier for a resource.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class ExternalId implements Serializable
{
    /** Holds the UUID external id. */
    private String id;

    /** Holds the name of the resource class that this external id is for. */
    private String resource;

    /**
     * Creates an uninitialized external id.
     */
    public ExternalId()
    {
    }

    /**
     * Creates an external id by id and resource name.
     *
     * @param id       The id.
     * @param resource The resource name or type.
     */
    public ExternalId(String id, String resource)
    {
        this.id = id;
        this.resource = resource;
    }

    /**
     * Gets the unique external id.
     *
     * @return The unique external id.
     */
    public String getId()
    {
        return id;
    }

    /**
     * Sets the unique external id.
     *
     * @param id The unique external id.
     */
    public void setId(String id)
    {
        this.id = id;
    }

    /**
     * Gets the resource that this is an external id for.
     *
     * @return The resource that this is an external id for.
     */
    public String getResource()
    {
        return resource;
    }

    /**
     * Sets the resource that this is an external id for.
     *
     * @param resource The resource that this is an external id for.
     */
    public void setResource(String resource)
    {
        this.resource = resource;
    }

    /**
     * Renders the external id as a string for debugging purposes.
     *
     * @return The external id as a string for debugging purposes.
     */
    public String toString()
    {
        return "id: " + id + ", resource: " + resource;
    }

    /**
     * Compares two external id's by their id values.
     *
     * @param o The external id to compare to.
     *
     * @return <tt>true</tt> if they are equal by id.
     */
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }

        if (!(o instanceof ExternalId))
        {
            return false;
        }

        final ExternalId externalId = (ExternalId) o;

        if ((id != null) ? (!id.equals(externalId.id)) : (externalId.id != null))
        {
            return false;
        }

        return true;
    }

    /**
     * Computes a hash code based on the id value only.
     *
     * @return A hash code based on the id value only.
     */
    public int hashCode()
    {
        return ((id != null) ? id.hashCode() : 0);
    }
}
