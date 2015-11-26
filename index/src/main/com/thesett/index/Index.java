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

import java.util.Map;

/**
 * An Index is a mapping from string based queries to records. Implementations will are normally expected to index the
 * records in such a way that enhances query performance for fast look ups.
 *
 * <p/>The records stored in the index are normally a subset of the full data records that contain just enough
 * information to generate summary listings of search results as well as some sort of identifier that can be used to
 * retrieve the full record. The full records are not normally stored in the index to save space.
 *
 * <p/>Any type of object can be indexed and multiple object types may be simultaneously indexed in the sam index
 * provided they share a common superclass (which could be java.lang.Object). This interface is polymorphic over the
 * class of the indexable objects superclass, the class of the summary records and the class of the record identifier.
 * The index will accept {@link IndexMapping} objects that describe the list of fields that are to be indexed for
 * different types and extract the data from those fields and convert them to strings to build the index around. It is
 * expected that the record type will have getter methods for the fields that are to be extracted from it; for field
 * 'exampleField' there should be a method 'getExampleField' on the record class otherwise an exception will be thrown.
 *
 * <p/>The stop words, synonyms and fuzzy tolerance levels can all be set up. This is normally done when the index is
 * first initialized and then not changed after that. The stop words are words that are not indexed; usually because
 * they are too common, for example, 'the' is not normally indexed as it is a useless term to search against. Synonyms
 * may be set up so that search terms can be expanded into their synonyms so that terms with the same meaning as the
 * query term will also be found. The fuzzy tolerance level is open to interpretation by implementations and an example
 * of its interpretation is the 'edit distance', that is, the number of insertions, deletions, or alterations of
 * characters in the query term needed to match terms in the records being searched. Matches below or equal to the
 * specified upper limit on edit distance will be accepted and those above it will not.
 *
 * <p/>One of the summary records fields must be used to inform the index of the records rating. This is a floating
 * point number used to order the search results by relevance when combined with the search hit score. If a given
 * implementation of the index measures the degree to which a query matches a record, for example, zero being a non-hit
 * and one being a perfect match, it will also use the rating to decide on the order in which results are to be
 * returned, for example by multiplying the rating by the hit degree. If no rating field is specified then a uniform
 * rating of one should be used.
 *
 * <p/>Records can be inserted into the index and removed from it with the {@link #add} and {@link #remove} operations.
 * There is also a {@link #clear} method to reset the index. There are two {@link #update} operations, one that only
 * replaces the summary record for a given identifier (also used to update the rating) and one that updates and
 * re-indexes the entire record against an altered full record.
 *
 * <p/>The index can be searched using the {@link #search} operation with a query string. The query will be parsed into
 * search terms with any punctuation characters stripped out as white space and any synonym matches expanded into the
 * full set of synonym terms. Search results are returned as a list in order of relevance.
 *
 * <p/>There is an optional {@link #cleanup} method that implementations may make use of to perform deffered clean-up
 * operations after modifications to an index have left it in a less than optimal state. The possibility of an external
 * index manager scheduling periodic execution of this method is the reason that this method is exposed in this
 * interface.
 *
 * <p/>To give an example, when a record is removed from an index which has an inverted term-to-record data structure it
 * may be time consuming to go through the index term by term and remove every reference to the record being removed. An
 * alternative may be to store the record in a wrapper and to mark that wrapper as invalidated when the record is
 * removed. As invalidated records are discovered by queries into the index they can be cleaned up, or this method could
 * be implemented to perform a clean up of the whole index in one go for many record removals thus making best use of
 * the time consuming iteration through all terms.
 *
 * <p/>Another example is that when new records are added the data strucutres for index may require a time consuming
 * calculation to be maintained in exactly the right state for a particular indexing algorithm. It may be possible that
 * an approximate (incremental) insert could be performed in less time. This cleanup method could then be used to
 * perform the full calculation less frequently. This use may find application in vector space based models where
 * approximate update of a reduced rank term-by-document matrix is used.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities
 * <tr><td> Index objects by a subset of their fields for string matching.
 * <tr><td> Search indexed objects for matches to a query string.
 * <tr><td> Accept list of synonyms to expand query terms by.
 * <tr><td> Accept upper limit on degree of fuzzy matching.
 * <tr><td> Accept list of stop words to exclude from indexing.
 * <tr><td> Accept index mappings to determine how to extract data from different record types.
 * <tr><td> Perform house keeping on the index.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public interface Index<K, D, E>
{
    /*
     * Adds a data extraction type mapping to the index to tell it how to extract data from a record type.
     *
     * @param clsFull    The class of the full records mapped.
     * @param clsSummary The class of the summary records mapped.
     * @param im The index type mapping.
     */
    //public void addMapping(Class<? extends D> clsFull, Class<? extends E> clsSummary, IndexMapping im);

    /*
     * Sets the stop words that should be ignored whenever found in the data records and not indexed.
     *
     * @param words A collection of stop words.
     */
    //public void setStopWords(Collection<String> words);

    /*
     * Sets the synonyms that matching query terms should be expanded into prior to searching the index. The synonym
     * dictionary is a map from words to sets of words with the same meaning. This mapping must be given in every
     * direction in which it is to be used. For example if 'large' also means 'big' and 'big' means 'large' then
     * both mappings must be specified. However, if 'massive' means 'big' but 'big' does not mean 'massive' then
     * only the mapping from 'massive' to 'big' should be specified. The query term 'massive' will be expanded into
     * 'massive big' but 'big' will not be expanded.
     *
     * @param synonyms The map of synonyms.
     */
    //public void setSynonyms(Map<String, Set<String>> synonyms);

    /*
     * Sets the degree of fuzzy matching to limit searches to. Fuzzy matching limit usually means the edit distance
     * but can be interpreted differently by different implementations.
     *
     * @param limit The maximum fuzzy matching limit.
     */
    //public void setFuzzyTolerance(int limit);

    /**
     * Adds a record to the search index. Its indexed fields are extracted as strings before being added to the index
     * data structure.
     *
     * @param  key        A key that uniquely identifies the record to insert.
     * @param  fullRecord The full data record to build the index from, fields will be extracted from this record.
     * @param  indexEntry The data record to add to the index, this is the record that searches will return.
     *
     * @throws IndexMappingException If the record being added to the index cannot be extracted because no mapping
     *                               exists for it or if a field specified in a matching mapping cannot be found on the
     *                               object being mapped.
     */
    void add(K key, D fullRecord, E indexEntry) throws IndexMappingException;

    /**
     * Updates a record in the index. Its indexed fields are extracted from the full record again and the new index
     * entry replaces any existing entry for the specified key.
     *
     * @param  key        A key that uniquely identifies the record to update.
     * @param  fullRecord The full data record to build the index from, fields will be extracted from this record.
     * @param  indexEntry The data record to add to the index, this is the record that searches will return.
     *
     * @throws IndexMappingException    If the record being added to the index cannot be extracted because no mapping
     *                                  exists for it or if a field specified in a matching mapping cannot be found on
     *                                  the object being mapped.
     * @throws IndexUnknownKeyException When the key is not already in the index, or has been removed from it.
     */
    void update(K key, D fullRecord, E indexEntry) throws IndexMappingException, IndexUnknownKeyException;

    /**
     * Updates a record in the index without re-indexing it. Only the key of the record to index an the entry to be
     * returned on matching searches are updated. Fields are not extracted from the full record and the indexing is not
     * changed.
     *
     * @param  key        A key that uniquely identifies the record to update.
     * @param  indexEntry The data record to add to the index, this is the record that searches will return.
     *
     * @throws IndexMappingException    If the record being added to the index cannot be extracted because no mapping
     *                                  exists for it or if a field specified in a matching mapping cannot be found on
     *                                  the object being mapped.
     * @throws IndexUnknownKeyException When the key is not already in the index, or has been removed from it.
     */
    void update(K key, E indexEntry) throws IndexMappingException, IndexUnknownKeyException;

    /**
     * Removes a record from the search index.
     *
     * @param  key A key that uniquely identifies the record to remove.
     *
     * @throws IndexUnknownKeyException When the key is not already in the index, or has been removed from it.
     */
    void remove(K key) throws IndexUnknownKeyException;

    /**
     * Performs a string matching query over the index. The query string should have any punctuation characters removed
     * and be parsed into words seperated by white space (spaces, new lines and tabs). Any stop words will be removed
     * from the query, any synonym matches will be expanded into the query.
     *
     * @param  query The search string to match against.
     *
     * @return A list of matching data records in order of relevance.
     */
    Map<K, E> search(String query);

    /** Removes all records from the index to produce a completely empty index. */
    void clear();

    /**
     * This is an optional method that implementations may make use of to perform deffered clean-up operations after
     * modifications to an index have left it in a less than optimal state. The possibility of an external index manager
     * scheduling periodic execution of this method is the reason that it is exposed in this interface.
     */
    void cleanup();
}
