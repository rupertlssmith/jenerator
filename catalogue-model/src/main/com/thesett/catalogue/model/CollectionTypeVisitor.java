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
package com.thesett.catalogue.model;

/**
 * Specifies a visitor for collection types.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Visit a collection type.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public interface CollectionTypeVisitor
{
    /**
     * Visits a collection type.
     *
     * @param <E>  The type of the elements of the collection.
     * @param type The collection type to visit.
     */
    <E> void visit(CollectionType<E> type);
}
