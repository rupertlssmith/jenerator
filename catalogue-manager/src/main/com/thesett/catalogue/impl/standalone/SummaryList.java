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
package com.thesett.catalogue.impl.standalone;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.thesett.catalogue.model.PagingResult;
import com.thesett.catalogue.model.ViewInstance;
import org.hibernate.criterion.Criterion;

import com.thesett.common.util.LazyPagingList;

/**
 * SummaryList is a lazy paging list containing {@link ViewInstance}'s. It encapsulates the name of the entity and
 * criterion to be applied to it and its related entities in order to generate the list of results.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Encapsulate query criteria for pages of dimension element summaries.
 * <tr><td> Automatically call-back the paging methods of the catalogue manager to get paged results.
 * <table>
 *
 * @author Rupert Smith
 */
public class SummaryList extends LazyPagingList<ViewInstance> implements Serializable
{
    /** Holds the database entity name to query for the summary list. */
    String databaseEntityName;

    /** Holds the name of the entity type that the results belong to. */
    String entityTypeName;

    /** Holds the name of the view type to project the results onto. */
    String viewTypeName;

    /** Holds the optional criterion to apply to the entity. */
    Criterion criterion = null;

    /** Holds the optional map of related entities to apply criterions to to restrict the results. */
    Map<String, Criterion> joins = null;

    /** Holds a reference to the catalogue manager service to call to get more list elements. */
    private transient CatalogueManagerServiceImpl service = null;

    /** Flag to indicate whether local or remote calling of the catalogue service should be used. */
    boolean useLocal = false;

    /**
     * Create a new summary list.
     *
     * @param size               The total size of the list.
     * @param blockSize          The block size to page.
     * @param databaseEntityName The database entity to query.
     * @param entityTypeName     The entity type to query.
     * @param viewTypeName       The view type to project the results onto.
     * @param criterion          The criterion to apply to the entity.
     * @param joins              The join criteria to apply to the entity.
     * @param service            The optional catalogue manager service implementation to call to get pages.
     * @param useLocal           Set to <tt>true</tt> to use local call-back, <tt>false</tt> to use remote.
     */
    public SummaryList(int size, int blockSize, String databaseEntityName, String entityTypeName, String viewTypeName,
        Criterion criterion, Map<String, Criterion> joins, CatalogueManagerServiceImpl service, boolean useLocal)
    {
        super(size, blockSize);

        // Keep the entity name, criterion and joins.
        this.databaseEntityName = databaseEntityName;
        this.entityTypeName = entityTypeName;
        this.viewTypeName = viewTypeName;
        this.criterion = criterion;
        this.joins = joins;
        this.service = service;
        this.useLocal = useLocal;
    }

    /**
     * No-arg constructor for serialization.
     */
    public SummaryList()
    {
    }

    /**
     * Sets the use local flag.
     *
     * @param useLocal The value of the use local flag.
     */
    public void setUseLocal(boolean useLocal)
    {
        this.useLocal = useLocal;
    }

    /**
     * Returns the value of the use local flag.
     *
     * @return The value of the use local flag.
     */
    public boolean isUseLocal()
    {
        return useLocal;
    }

    /**
     * Gets a page of results by calling the catalogue manager services paging method, through the service locator.
     *
     * @param start  The start offset to get from.
     * @param number The number of results to get.
     *
     * @return A {@link PagingResult} containing the new total results size and one page of results.
     */
    public List<ViewInstance> getBlock(int start, int number)
    {
        PagingResult result = null;

        // Get the requested block using local or remote calling.
        if (useLocal)
        {
            result =
                service.executePagedQuery(start, number, databaseEntityName, entityTypeName, viewTypeName, criterion,
                    joins);
        }
        else
        {
            throw new UnsupportedOperationException("Remote operation not supported.");
        }

        // Update the lists size in response to any changes to the results set.
        setSize(result.size);

        // Return the list.
        return result.list;
    }
}
