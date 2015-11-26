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

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * An IndexSetup is an interface for configuring indexes. An index is configured with a type mapping telling it what
 * fields to extract and index from different classes of objects, a set of stop words, a fuzzy matching tolerance level
 * and possibly alos a set of synonyms to expand queries with. This is seperate from the main interface for indexing and
 * querying records because the setup activities are normally conducted seperately, only once at application start up
 * time, and not normally exposed to clients using an index with transactional support.
 *
 * <p/>Any type of object can be indexed and multiple object types may be simultaneously indexed in the sam index
 * provided they share a common superclass (which could be java.lang.Object). This interface is polymorphic over the
 * class of the indexable objects superclass and the class of the summary records. The index setup will accept
 * {@link IndexMapping} objects that describe the list of fields that are to be indexed for different types and extract
 * the data from those fields and convert them to strings to build the index around. It is expected that the record type
 * will have getter methods for the fields that are to be extracted from it; for field 'exampleField' there should be a
 * method 'getExampleField' on the record class otherwise an exception will be thrown.
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
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities
 * <tr><td> Accept list of synonyms to expand query terms by.
 * <tr><td> Accept upper limit on degree of fuzzy matching.
 * <tr><td> Accept list of stop words to exclude from indexing.
 * <tr><td> Accept index mappings to determine how to extract data from different record types.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public interface IndexSetup<D, E>
{
    /**
     * Adds a data extraction type mapping to the index to tell it how to extract data from a record type.
     *
     * @param clsFull    The class of the full records mapped.
     * @param clsSummary The class of the summary records mapped.
     * @param im         The index type mapping.
     */
    void addMapping(Class<? extends D> clsFull, Class<? extends E> clsSummary, IndexMapping im);

    /**
     * Sets the stop words that should be ignored whenever found in the data records and not indexed.
     *
     * @param words A collection of stop words.
     */
    void setStopWords(Collection<String> words);

    /**
     * Sets the synonyms that matching query terms should be expanded into prior to searching the index. The synonym
     * dictionary is a map from words to sets of words with the same meaning. This mapping must be given in every
     * direction in which it is to be used. For example if 'large' also means 'big' and 'big' means 'large' then both
     * mappings must be specified. However, if 'massive' means 'big' but 'big' does not mean 'massive' then only the
     * mapping from 'massive' to 'big' should be specified. The query term 'massive' will be expanded into 'massive big'
     * but 'big' will not be expanded.
     *
     * @param synonyms The map of synonyms.
     */
    void setSynonyms(Map<String, Set<String>> synonyms);

    /**
     * Sets the degree of fuzzy matching to limit searches to. Fuzzy matching limit usually means the edit distance but
     * can be interpreted differently by different implementations.
     *
     * @param limit The maximum fuzzy matching limit.
     */
    void setFuzzyTolerance(int limit);

    /** Resets the index setup, deleting all mappings, synonym mappings and stop words. */
    void reset();
}
