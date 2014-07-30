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
package com.thesett.index;

/**
 * IndexStore provides a mapping from index names to index implementations.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Map names to indexes.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public interface IndexStore
{
    /**
     * Retrieves a handle to the named index. If an index with this name does not already exist then a new one is
     * created.
     *
     * @param  indexName The name of the index to retrieve.
     *
     * @return The named index.
     */
    TransactionalIndex getNamedIndex(String indexName);

    /**
     * Retrieves the named indexes setup instance.
     *
     * @param  indexName The name of the index to get the setup instance for.
     *
     * @return The indexes setup instance.
     */
    IndexSetup getNamedIndexSetup(String indexName);
}
