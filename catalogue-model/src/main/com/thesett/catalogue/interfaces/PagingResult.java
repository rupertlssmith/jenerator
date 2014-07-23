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
import java.util.List;

/**
 * PagingResult is used to encapsulate a list and a size as a single returnable object. It is intended to be used in
 * conjunction with paging of results of queries. {@link com.thesett.common.util.LazyPagingList} implementations
 * of the 'getBlock' method may call paging methods but these methods need to be able to tell the caller that
 * the size of a results set has changed since the last call as well as passing the actual page of results.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Encapsulate a list and a size.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class PagingResult implements Serializable
{
    /** Holds the total size of the results set for all results. */
    public int size;

    /** Holds the list containing one page of results. */
    public List<ViewInstance> list;

    /**
     * Creates a return object encapsulating a size and a list.
     *
     * @param size The size.
     * @param list The list.
     */
    public PagingResult(int size, List<ViewInstance> list)
    {
        this.size = size;
        this.list = list;
    }
}
