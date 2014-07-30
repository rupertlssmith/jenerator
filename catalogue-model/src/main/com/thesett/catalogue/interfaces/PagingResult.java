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
package com.thesett.catalogue.interfaces;

import java.io.Serializable;
import java.util.List;

/**
 * PagingResult is used to encapsulate a list and a size as a single returnable object. It is intended to be used in
 * conjunction with paging of results of queries. {@link com.thesett.common.util.LazyPagingList} implementations of the
 * 'getBlock' method may call paging methods but these methods need to be able to tell the caller that the size of a
 * results set has changed since the last call as well as passing the actual page of results.
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
