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
package com.thesett.index.prototype;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.thesett.common.error.NotImplementedException;
import com.thesett.common.reflect.BeanMemento;
import com.thesett.common.reflect.Memento;
import com.thesett.common.util.ReflectionUtils;
import com.thesett.index.IndexMapping;
import com.thesett.index.IndexMappingException;
import com.thesett.index.IndexSetup;
import com.thesett.index.IndexUnknownKeyException;
import com.thesett.index.TransactionalIndex;
import com.thesett.index.tx.IndexTxId;
import com.thesett.index.tx.IndexTxManager;

/**
 * ProtoIndex is a prototype implementation of a free text search index. It does not provide fuzzy matching and makes no
 * attempt to optimize the data structures used. This implementation uses a hash map into which the indexed terms are
 * placed along with references to the summary records that they match. Searching the index simply looks up all the
 * query terms in the hashmap, computes the intersection of the results for each term, and returns that as the list of
 * results.
 *
 * <p/>The deadlock prevention strategy used by this implementation is intended to best fit the way in which indexes are
 * used. An index will normally be fairly static, servicing many read only requests as quickly and as concurrently as
 * possible. Occasionly a large amount of data will be uploaded into an index all at once, as new data is uploaded.
 * Slightly more frequently, but still much less frequently than the read operations, individual records will be udpated
 * as their ratings change. Other parts of the software are expected to try and reduce the frequency of ratings
 * alterations as much as possible. Write operations are expected to need only very low concurrency. Read deadlocks are
 * prevented by not requiring read locks to be mutually exclusive. Write deadlocks are prevented by having a single
 * write lock, effectively forcing the locking of all needed resources in a single step.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
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
public class ProtoIndex<K, D, E> implements TransactionalIndex<K, D, E>, IndexSetup<D, E>, Serializable
{
    /** Used for logging. */
    // private static final Logger log = Logger.getLogger(ProtoIndex.class);

    /** Holds the mappings from classes to fields to extract and index. */
    private Map<Class<? extends D>, IndexMapping> mappings = new HashMap<Class<? extends D>, IndexMapping>();

    /** Holds a mapping from the summary record types to the appropriate class mapping. */
    private Map<Class<? extends E>, IndexMapping> summaryMappings = new HashMap<Class<? extends E>, IndexMapping>();

    /** Holds the stop words as a hashed set for quick lookup. */
    private Set<String> stopWords = new HashSet<String>();

    /** Holds the synonyms for query expansion. */
    private Map<String, Set<String>> synonyms = new HashMap<String, Set<String>>();

    /** Holds the free text search index. */
    private Map<String, Set<InvalidateableKey<K>>> index = new HashMap<String, Set<InvalidateableKey<K>>>();

    /** Holds a mapping from the indexed record keys to the index record for quick look up by key. */
    private Map<K, IndexRecord> indexRecordsByKey = new HashMap<K, IndexRecord>();

    /** Holds a invalidated records that are pending full removal from the index. */
    // private Collection<IndexRecord> invalidatedRecords = new ArrayList<IndexRecord>();

    /** Holds the current transaction isolation level. */
    IsolationLevel isolationLevel = IsolationLevel.None;

    /** Holds the global write lock that ensures that only one writer at a time is allowed to alter the index. */
    ReadWriteLock globalLock = new ReentrantReadWriteLock();

    /** A condition on the global write lock that is used to signal when it becomes unowned by any transaction. */
    Condition globalWriteLockFree = globalLock.writeLock().newCondition();

    /** Holds the transaction id of the only transaction that is allowed to use the global write lock. */
    IndexTxId globalWriteLockTxId = null;

    /** Holds the write-behind cache of changes made by transactions. */
    private Map<IndexTxId, List<RecordAlteration>> txWrites = new HashMap<IndexTxId, List<RecordAlteration>>();

    /** Creates a prototype index. */
    public ProtoIndex()
    {
    }

    /**
     * Adds a data extraction type mapping to the index to tell it how to extract data from a record type.
     *
     * @param clsFull    The class of the full records mapped.
     * @param clsSummary The class of the summary records mapped.
     * @param im         The index type mapping.
     */
    public void addMapping(Class<? extends D> clsFull, Class<? extends E> clsSummary, IndexMapping im)
    {
        // log.debug("public void addMapping(Class<? extends D> clsFull, "
        // + "Class<? extends E> clsSummary, IndexMapping im): called");

        // Add the new mapping to the set of mappings for this index.
        mappings.put(clsFull, im);
        summaryMappings.put(clsSummary, im);
    }

    /**
     * Sets the stop words that should be ignored whenever found in the data records and not indexed.
     *
     * @param words A collection of stop words.
     */
    public void setStopWords(Collection<String> words)
    {
        // log.debug("public void setStopWords(Collection<String> words): called");

        // Keep the stop words in a hashed set.
        stopWords = new HashSet<String>(words);
    }

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
    public void setSynonyms(Map<String, Set<String>> synonyms)
    {
        // log.debug("public void setSynonyms(Map<String, Set<String>> synonyms): called");

        // Take a copy of the synonym thesaurus.
        this.synonyms = new HashMap<String, Set<String>>(synonyms);
    }

    /**
     * Sets the degree of fuzzy matching to limit searches to. Fuzzy matching limit usually means the edit distance but
     * can be interpreted differently by different implementations.
     *
     * @param limit The maximum fuzzy matching limit.
     */
    public void setFuzzyTolerance(int limit)
    {
        // log.debug("public void setFuzzyTolerance(int limit): called");

        throw new NotImplementedException();
    }

    /**
     * Adds a record to the search index. Its indexed fields are extracted as strings before being added to the index
     * data structure.
     *
     * @param key        A key that uniquely identifies the record to insert.
     * @param fullRecord The full data record to build the index from, fields will be extracted from this record.
     * @param indexEntry The data record to add to the index, this is the record that searches will return.
     *
     * @todo  Note that when extracting the rating field the first matching type mapping (against the full records type)
     *        is taken as specifying the field name of the rating field to extract. This algorithm should probably be
     *        changed to extract the narrowest fitting match, that is, the one that most specifically matches the record
     *        type. So if there is a mapping on Object and one one MyClass and there is a rating field match for both
     *        types on a record of type MyClass, the MyClass match is the most specific so it should be taken. The first
     *        match algorithm means that it cannot be determined which match will be taken as the mappings are not
     *        iterated over in a controlled order.
     */
    public void add(K key, D fullRecord, E indexEntry)
    {
        // log.debug("public void add(K key, D fullRecord, E indexEntry): called");
        // log.debug("txId on current thread = " + IndexTxManager.getTxIdFromThread());
        // log.debug("isolationLevel = " + isolationLevel);

        // Check if in a higher transactional mode than none and capture the transaction id if so.
        IndexTxId txId = null;

        if (isolationLevel.compareTo(IsolationLevel.None) > 0)
        {
            // Extract the current transaction id.
            txId = IndexTxManager.getTxIdFromThread();

            // Wait until the global write lock can be acquired by this transaction.
            try
            {
                acquireGlobalWriteLock(txId);
            }
            catch (InterruptedException e)
            {
                throw new IllegalStateException("Interrupted whilst waiting for global write lock.", e);
            }
        }

        // Extract the indexable fields of the record as a text to index.
        String indexableText = extractIndexableText(fullRecord);

        // Convert the text to be indexed to a set-of-words. Term frequencies are not used in this implementation.
        Set<String> setOfWords = ParsingUtils.toSetOfWords(indexableText, stopWords);

        // Get the new records rating.
        float rating = extractRating(indexEntry);

        // Add the record to the index straight away if not in transactional mode.
        if (isolationLevel.equals(IsolationLevel.None))
        {
            addNewRecord(key, indexEntry, rating, setOfWords);
        }

        // Otherwise defer adding the record until transaction commit.
        else
        {
            RecordAlteration cachedWriteOperation = new AddRecord(key, indexEntry, rating, setOfWords);
            addCachedWriteOperation(txId, cachedWriteOperation);
        }
    }

    /**
     * Updates a record in the index. Its indexed fields are extracted from the full record again and the new index
     * entry replaces any existing entry for the specified key.
     *
     * @param  key        A key that uniquely identifies the record to update.
     * @param  fullRecord The full data record to build the index from, fields will be extracted from this record.
     * @param  indexEntry The data record to add to the index, this is the record that searches will return.
     *
     * @throws IndexUnknownKeyException When the key is not already in the index, or has been removed from it.
     */
    public void update(K key, D fullRecord, E indexEntry) throws IndexUnknownKeyException
    {
        // log.debug("public void update(K key, D fullRecord, E indexEntry): called");
        // log.debug("txId on current thread = " + IndexTxManager.getTxIdFromThread());
        // log.debug("isolationLevel = " + isolationLevel);

        // Check if in a higher transactional mode than none and use write locking if so.
        if (isolationLevel.compareTo(IsolationLevel.None) > 0)
        {
            // Extract the current transaction id.
            IndexTxId txId = IndexTxManager.getTxIdFromThread();

            // Wait until the global write lock can be acquired by this transaction.
            try
            {
                acquireGlobalWriteLock(txId);
            }
            catch (InterruptedException e)
            {
                throw new IllegalStateException("Interrupted whilst waiting for global write lock.", e);
            }
        }

        // Remove the record and add it again.
        remove(key);
        add(key, fullRecord, indexEntry);
    }

    /**
     * Updates a record in the index without re-indexing it. Only the key of the record to index an the entry to be
     * returned on matching searches are updated. Fields are not extracted from the full record and the indexing is not
     * changed.
     *
     * @param  key        A key that uniquely identifies the record to update.
     * @param  indexEntry The data record to add to the index, this is the record that searches will return.
     *
     * @throws IndexUnknownKeyException When the key is not already in the index, or has been removed from it.
     */
    public void update(K key, E indexEntry) throws IndexUnknownKeyException
    {
        // log.debug("public void update(K key, E indexEntry): called");
        // log.debug("txId on current thread = " + IndexTxManager.getTxIdFromThread());
        // log.debug("isolationLevel = " + isolationLevel);

        // Check if in a higher transactional mode than none and capture the transaction id if so.
        IndexTxId txId = null;

        if (isolationLevel.compareTo(IsolationLevel.None) > 0)
        {
            // Extract the current transaction id.
            txId = IndexTxManager.getTxIdFromThread();

            // Wait until the global write lock can be acquired by this transaction.
            try
            {
                acquireGlobalWriteLock(txId);
            }
            catch (InterruptedException e)
            {
                throw new IllegalStateException("Interrupted whilst waiting for global write lock.", e);
            }
        }

        // Check that the record to update already exists.
        // log.debug("Testing records by key for key, " + key);

        if (!indexRecordsByKey.containsKey(key))
        {
            throw new IndexUnknownKeyException("The key, " + key + ", cannot be found in the index.", null, null, null);
        }

        // Extract the rating to update.
        float newRating = extractRating(indexEntry);

        // Update the record in the index straight away if not in transactional mode.
        if (isolationLevel.equals(IsolationLevel.None))
        {
            updateRecord(key, indexEntry, newRating);
        }

        // Otherwise defer updating the record until transaction commit.
        else
        {
            RecordAlteration cachedWriteOperation = new UpdateRecord(key, indexEntry, newRating);
            addCachedWriteOperation(txId, cachedWriteOperation);
        }
    }

    /**
     * Removes a record from the search index.
     *
     * @param  key A key that uniquely identifies the record to remove.
     *
     * @throws IndexUnknownKeyException When the key is not already in the index, or has been removed from it.
     */
    public void remove(K key) throws IndexUnknownKeyException
    {
        // log.debug("public void remove(K key): called");
        // log.debug("txId on current thread = " + IndexTxManager.getTxIdFromThread());
        // log.debug("isolationLevel = " + isolationLevel);

        // Check if in a higher transactional mode than none and capture the transaction id if so.
        IndexTxId txId = null;

        if (isolationLevel.compareTo(IsolationLevel.None) > 0)
        {
            // Extract the current transaction id.
            txId = IndexTxManager.getTxIdFromThread();

            // Wait until the global write lock can be acquired by this transaction.
            try
            {
                acquireGlobalWriteLock(txId);
            }
            catch (InterruptedException e)
            {
                throw new IllegalStateException("Interrupted whilst waiting for global write lock.", e);
            }
        }

        // Check that the record to delete actually exists in the index.
        // log.debug("Testing records by key for key, " + key);

        if (!indexRecordsByKey.containsKey(key))
        {
            throw new IndexUnknownKeyException("The key, " + key + ", cannot be found in the index.", null, null, null);
        }

        // Remove the record from the index straight away if not in transactional mode.
        if (isolationLevel.equals(IsolationLevel.None))
        {
            removeRecord(key);
        }

        // Otherwise defer removing the record until transaction commit.
        else
        {
            RecordAlteration cachedWriteOperation = new RemoveRecord(key);
            addCachedWriteOperation(txId, cachedWriteOperation);
        }
    }

    /** Removes all records from the index to produce a completely empty index. */
    public void clear()
    {
        // log.debug("public void clear(): called");
        // log.debug("txId on current thread = " + IndexTxManager.getTxIdFromThread());
        // log.debug("isolationLevel = " + isolationLevel);

        // Check if in a higher transactional mode than none and capture the transaction id if so.
        IndexTxId txId = null;

        if (isolationLevel.compareTo(IsolationLevel.None) > 0)
        {
            // Extract the current transaction id.
            txId = IndexTxManager.getTxIdFromThread();

            // Wait until the global write lock can be acquired by this transaction.
            try
            {
                acquireGlobalWriteLock(txId);
            }
            catch (InterruptedException e)
            {
                throw new IllegalStateException("Interrupted whilst waiting for global write lock.", e);
            }
        }

        // Clear the index straight away if not in transactional mode.
        if (isolationLevel.equals(IsolationLevel.None))
        {
            clearAllRecords();
        }

        // Otherwise defer clearing the index until transaction commit time.
        else
        {
            RecordAlteration cachedWriteOperation = new ClearAllRecords();
            addCachedWriteOperation(txId, cachedWriteOperation);
        }
    }

    /** Resets the index, removing all mappings, stop words and synonym mappings from it. */
    public void reset()
    {
        mappings.clear();
        summaryMappings.clear();
        stopWords.clear();
        synonyms.clear();
    }

    /** Scans through every term in the index and sweeps out any deleted records. */
    public void cleanup()
    {
        // log.debug("public void cleanup(): called");

        // Scan through every term in the index looking for records that are invalidated.
        {
            // If a matching record is found then remove it from indexing against the term.

            // Drop the removed record from the invalidated records collection.
        }

        throw new NotImplementedException();
    }

    /**
     * Performs a string matching query over the index. The query string should have any punctuation characters removed
     * and be parsed into words seperated by white space (spaces, new lines and tabs). Any stop words will be removed
     * from the query, any synonym matches will be expanded into the query.
     *
     * @param  query The search string to match against.
     *
     * @return A list of matching data records in order of relevance.
     */
    public Map<K, E> search(String query)
    {
        // log.debug("public Map<K, E> search(String query): called");
        // log.debug("txId on current thread = " + IndexTxManager.getTxIdFromThread());

        // Check if in a higher transactional mode than none and capture the transaction id if so.
        IndexTxId txId = null;

        if (isolationLevel.compareTo(IsolationLevel.None) > 0)
        {
            // Extract the current transaction id.
            txId = IndexTxManager.getTxIdFromThread();
        }

        // Check if in a higher transactional mode than read uncommitted and ensure the global read lock
        // has been acquired if so.
        if (isolationLevel.compareTo(IsolationLevel.ReadUncommitted) > 0)
        {
            // Wait until the global read lock can be acquired by this transaction.
            acquireGlobalReadLock(txId);
        }

        // Tokenize the query into a set-of-words, dropping all punctuation and splitting on whitespace.
        Set<String> setOfWords = ParsingUtils.toSetOfWords(query, stopWords);

        // For each term in the query get the set of matching records and take the intersection of it with the full
        // query result.

        // Used to build up the results in.
        Collection<IndexRecord> result = new HashSet<IndexRecord>();

        // Used to indicate the search on the first word is in progress. The first words results are added to the result
        // set and subsequent words results take the intersection with it.
        boolean firstPass = true;

        // log.debug("result = " + result);

        for (String word : setOfWords)
        {
            // log.debug("word = " + word);

            Collection<IndexRecord> wordResult = new HashSet<IndexRecord>();

            // Try to expand the query term using the synonym database.
            if (synonyms.containsKey(word))
            {
                // log.debug("Synonym matches found for: " + word);

                // Query all synonym terms and take the union of their lists of results as the result for the
                // expanded term.
                for (String synonym : synonyms.get(word))
                {
                    Set<IndexRecord> synonymResult = queryLiveRecords(synonym);

                    // log.debug("synonymResult = " + synonymResult);

                    if (synonymResult != null)
                    {
                        wordResult.addAll(synonymResult);
                    }
                }
            }

            // The word has no synonyms.
            else
            {
                Set<IndexRecord> tempResult = queryLiveRecords(word);

                // log.debug("tempResult = " + tempResult);

                if (tempResult != null)
                {
                    wordResult.addAll(tempResult);
                }
            }

            // log.debug("wordResult = " + wordResult);

            // If on the first word add all its results to the results set.
            if (firstPass)
            {
                firstPass = false;

                result.addAll(wordResult);
            }

            // If on a subsequent word take the intersection of its results with the results set built so far.
            else
            {
                result.retainAll(wordResult);
            }

            // log.debug("result = " + result);

            // If any invalidated records are uncovered whilst querying a term then remove them from indexing
            // against the term.
        }

        // Sort the search results by their ratings.
        List<IndexRecord> sortedResults = new ArrayList();
        sortedResults.addAll(result);
        Collections.sort(sortedResults, new RatingComparator());

        // log.debug("sortedResults = " + sortedResults);
        // log.debug("sortedResults.size() = " + sortedResults.size());

        // Turn the set of results into a map from keys to summary records (strip out the internal representation of
        // them into a paired structure).
        Map<K, E> searchResults = new LinkedHashMap<K, E>();

        for (IndexRecord record : sortedResults)
        {
            searchResults.put(record.key.key, record.summaryRecord);
        }

        // Check if in a higher transactional more than read uncommitted and ensure the global read lock
        // acquired at the start of this operation gets released if so.
        if (isolationLevel.compareTo(IsolationLevel.ReadUncommitted) > 0)
        {
            // Wait until the global write lock can be acquired by this transaction.
            releaseGlobalReadLock();
        }

        return searchResults;
    }

    /**
     * Used to set the transaction isolation level.
     *
     * @param isolationLevel The transaction isolation level to support.
     */
    public void setTransactionalMode(IsolationLevel isolationLevel)
    {
        // log.debug("public void setTransactionalMode(IsolationLevel isolationLevel): called");

        // Keep the new isolation level.
        this.isolationLevel = isolationLevel;
    }

    /**
     * Gets the isolation level in force.
     *
     * @return The isolation level in force.
     */
    public IsolationLevel getTransationalMode()
    {
        return this.isolationLevel;
    }

    /**
     * When operating in transactional mode causes any changes since the last commit to be made visible to the search
     * method.
     */
    public void commit()
    {
        // log.debug("public void commit(): called");
        // log.debug("txId on current thread = " + IndexTxManager.getTxIdFromThread());

        // Check if in a higher transactional mode than none, otherwise commit does nothing.
        IndexTxId txId = null;

        if (!isolationLevel.equals(IsolationLevel.None))
        {
            // Extract the current transaction id.
            txId = IndexTxManager.getTxIdFromThread();

            // Wait until the global write lock can be acquired by this transaction.
            try
            {
                acquireGlobalWriteLock(txId);
            }
            catch (InterruptedException e)
            {
                throw new IllegalStateException("Interrupted whilst waiting for global write lock.", e);
            }

            // Check that this transaction has made changes to be committed.
            List<RecordAlteration> alterations = txWrites.get(txId);

            if (alterations != null)
            {
                // Loop through all the writes that the transaction wants to apply to the index.
                for (RecordAlteration nextAlteration : alterations)
                {
                    // If a record is to be changed or deleted then require the individual write lock on it.

                    // If a new record is being inserted then create it with the individual write lock on it.

                    // Apply the change and update the term index.
                    nextAlteration.execute();

                    // Release the indiviudal record write lock.
                }

                // Clear the write behind cache for this transaction as its work has been completed.
                txWrites.remove(txId);
            }

            // Release the global write lock.
            releaseGlobalWriteLock();
        }
    }

    /**
     * When operation in transactional mode causes any changes since the last commit to be dropped and never made
     * visible to the search method.
     */
    public void rollback()
    {
        // log.debug("public void rollback(): called");
        // log.debug("txId on current thread = " + IndexTxManager.getTxIdFromThread());

        // Check if in a higher transactional mode than none, otherwise commit does nothing.
        IndexTxId txId = null;

        if (!isolationLevel.equals(IsolationLevel.None))
        {
            // Extract the current transaction id.
            txId = IndexTxManager.getTxIdFromThread();

            // Wait until the global write lock can be acquired by this transaction.
            try
            {
                acquireGlobalWriteLock(txId);
            }
            catch (InterruptedException e)
            {
                throw new IllegalStateException("Interrupted whilst waiting for global write lock.", e);
            }

            // Check if the current transaction holds the global write lock on this index, otherwise nothing needs to
            // be done.
            {
                // Discard all the changes that the transaction was going to make.
                txWrites.remove(txId);
            }

            // Release the global write lock.
            releaseGlobalWriteLock();
        }
    }

    /** Releases the global write lock from being assigned to a transaction. */
    public void releaseGlobalWriteLock()
    {
        // Get the global write lock to ensure only one thread at a time can execute this code.
        globalLock.writeLock().lock();

        // Use a try block so that the corresponding finally block guarantees release of the thread lock.
        try
        {
            // Release the global write lock, assigning it to no transaction.
            globalWriteLockTxId = null;

            // Signal that the write lock is now free.
            globalWriteLockFree.signal();
        }

        // Ensure that the thread lock is released once assignment of the write lock to the transaction is complete.
        finally
        {
            globalLock.writeLock().unlock();
        }
    }

    /** Releases the global write lock from being assigned to a transaction. */
    public void releaseGlobalReadLock()
    {
        // Get the global write lock to ensure only one thread at a time can execute this code.
        globalLock.readLock().unlock();
    }

    /**
     * Adds a record alteration entry to the transactional write-behind cache for the specified transaction.
     *
     * @param txId                 The transaction id to store the operation against.
     * @param cachedWriteOperation The write operation to store.
     */
    private void addCachedWriteOperation(IndexTxId txId, RecordAlteration cachedWriteOperation)
    {
        List<RecordAlteration> writeCache = txWrites.get(txId);

        if (writeCache == null)
        {
            writeCache = new ArrayList<RecordAlteration>();
            txWrites.put(txId, writeCache);
        }

        writeCache.add(cachedWriteOperation);
    }

    /**
     * Adds a new record to the index.
     *
     * @param key        The new records key.
     * @param indexEntry The new summary record.
     * @param rating     The new records rating.
     * @param setOfWords The set of words to index the new entry against.
     */
    private void addNewRecord(K key, E indexEntry, float rating, Iterable<String> setOfWords)
    {
        // log.debug("private void addNewRecord(K key, E indexEntry, float rating, Set<String> setOfWords): called");

        // Create a new invalidateable key for the new key to insert.
        InvalidateableKey<K> newKey = new InvalidateableKey<K>(key);

        // Pair together the key and the summary record for index storage and place a reference to this record by its
        // key in the quick look up mapping.
        IndexRecord indexRecord = new IndexRecord();
        indexRecord.key = newKey;
        indexRecord.summaryRecord = indexEntry;
        indexRecord.rating = rating;
        indexRecordsByKey.put(key, indexRecord);
        // log.debug("Put key, " + key + ", and summary record, " + indexRecord + ", in records by key.");

        // For each unique term extracted from the new record, store a reference to the key against that term.
        for (String term : setOfWords)
        {
            // Check if the term is already in the index and create a new results set for it if not.
            if (!index.containsKey(term))
            {
                index.put(term, new HashSet<InvalidateableKey<K>>());
            }

            // Get the results set to add the index record to.
            Set<InvalidateableKey<K>> resultsSet = index.get(term);

            // Add the indexed entry by key to it for the term, replaceing any existing key that may already be there.

            if (resultsSet.contains(newKey))
            {
                resultsSet.remove(newKey);
                // log.debug("Removed matching (invalidated) key to the new key.");
            }

            resultsSet.add(newKey);
        }
    }

    /**
     * Extracts the rating from the summary record depending on the rating field name specified for the mapping for the
     * full record type.
     *
     * @param  indexEntry The summary record to extract the rating field from.
     *
     * @return The summary records rating.
     *
     * @throws IndexMappingException If no type mapping is found for the full record type, or the rating field is not a
     *                               float, or the rating field does not exist on the summary record.
     */
    private float extractRating(E indexEntry) throws IndexMappingException
    {
        // log.debug("private float extractRating(E indexEntry): called");

        // Extract the rating score from the summary record. This is done by scanning through the list of type
        // mappings and taking the first match.
        // Wrap the summary record in a bean memento to simplify access to its properties.
        Memento indexEntryMemento = new BeanMemento(indexEntry);
        indexEntryMemento.capture();

        // Used to indicate that a mapping has been found for the rating field and that the field exists on the
        // summary record.
        boolean ratingFieldFound = false;

        // Holds the rating.
        float rating = 0.0f;

        for (Class<? extends E> mappingClass : summaryMappings.keySet())
        {
            // Check if the full record type matches the mapping (is a sub-type of its class).
            if (mappingClass.isInstance(indexEntry))
            {
                ratingFieldFound = true;

                // Get the rating value.
                String ratingFieldName = summaryMappings.get(mappingClass).getRatingFieldName();
                Object ratingValue = null;

                // Make sure that the rating field exists on the summary record.
                try
                {
                    ratingValue = indexEntryMemento.get(null, ratingFieldName);
                }
                catch (NoSuchFieldException e)
                {
                    throw new IndexMappingException("The rating field, " + ratingFieldName +
                        ", does not exist on the class, " + indexEntry.getClass() + ", of summary records.", e, null,
                        null);
                }

                if (ratingValue == null)
                {
                    throw new IndexMappingException("The rating field, " + ratingFieldName +
                        ", is null on the class, " + indexEntry.getClass() + ", of summary records.", null, null, null);
                }

                // Make sure that the rating can be cast to a float.
                try
                {
                    rating = ((Float) ratingValue).floatValue();
                }
                catch (ClassCastException e)
                {
                    throw new IndexMappingException("The rating field, " + ratingFieldName + ", is not a float.", e,
                        null, null);
                }
            }
        }

        // Check that a rating field was found and extracted from the summary record.
        if (!ratingFieldFound)
        {
            throw new IndexMappingException("No rating field mapping could be found for the class, " +
                indexEntry.getClass() + ", of record being inserted.", null, null, null);
        }

        return rating;
    }

    /**
     * Extracts the mapped fields from a record and concatenates them together into a String.
     *
     * @param  fullRecord The record to extract the fields from.
     *
     * @return All the mapped fields of the record concatenated toghether as Strings.
     *
     * @throws IndexMappingException If some of the mapped fields cannot be found on the specified record for the type
     *                               mappings given to this class, or if at least one matching type mapping is not found
     *                               for the records type.
     */
    private String extractIndexableText(D fullRecord) throws IndexMappingException
    {
        // log.debug("private String extractIndexableText(D fullRecord): called");

        // Capture the full record in a bean memento to simplify extracting its fields.
        Memento fullRecordMemento = new BeanMemento(fullRecord);
        fullRecordMemento.capture();

        // Loop through all the type mappings and find those that match the new record and extract fields for the mapping.
        String indexableText = "";

        // Used to indicate when at least one matching type mapping has been found for the new record, if none are found
        // then an exception is raised.
        boolean atLeastOneMappingFound = false;

        for (Class<? extends D> mappingClass : mappings.keySet())
        {
            // Check if the full record type matches the mapping (is a sub-type of its class).
            if (ReflectionUtils.isSubTypeOf(mappingClass, fullRecord.getClass()))
            {
                atLeastOneMappingFound = true;

                IndexMapping mapping = mappings.get(mappingClass);

                // Extract the fields to be indexed from the full record as strings.
                String[] fieldNames = mapping.getFieldNames();

                for (int i = 0; i < fieldNames.length; i++)
                {
                    String nextField = fieldNames[i];

                    // Use the bean memento to extract the data.
                    // Add a space to the indexable text before more fields are appended to make sure words on the
                    // beginings and ends of fields don't get concatenated.
                    Object nextFieldValue = null;

                    try
                    {
                        nextFieldValue = fullRecordMemento.get(null, nextField);
                    }
                    catch (NoSuchFieldException e)
                    {
                        throw new IndexMappingException("The field, " + nextField + ", cannot be accessed " +
                            "in the record of type " + fullRecord.getClass(), e, null, null);
                    }

                    if (nextFieldValue == null)
                    {
                        nextFieldValue = "";
                    }

                    indexableText += nextFieldValue.toString() + " ";
                }
            }
        }

        // Raise an exception if no matching type mapping was found for the new record.
        if (!atLeastOneMappingFound)
        {
            throw new IndexMappingException("No index mapping was found for the class, " + fullRecord.getClass() +
                ", of record being inserted.", null, null, null);
        }

        return indexableText;
    }

    /**
     * Updates a summary record and its rating.
     *
     * @param key        The records key.
     * @param indexEntry The new summary record.
     * @param newRating  The new record rating.
     */
    private void updateRecord(K key, E indexEntry, float newRating)
    {
        // log.debug("private void updateRecord(K key, E indexEntry, float newRating): called");

        // Look up the indexed record by its key and replace the summary record with the new one.
        IndexRecord indexRecord = indexRecordsByKey.get(key);
        // log.debug("Got record, " + indexRecord + ", for key, " + key + ", from records by key.");

        // Update the summary record and also the rating value in the indexed record.
        indexRecord.summaryRecord = indexEntry;
        indexRecord.rating = newRating;
    }

    /**
     * Deletes a record from the index.
     *
     * @param key The records key.
     */
    private void removeRecord(K key)
    {
        // log.debug("private void removeRecord(K key): called");

        // Find the indexed record for the key in the quick look up map and invalidate it and move it to
        // the invalidated collection.
        IndexRecord indexRecord = indexRecordsByKey.remove(key);
        indexRecord.key.invalidated = true;

        // invalidatedRecords.add(indexRecord);
        // log.debug("Removed record, " + indexRecord + ", for key, " + key
        // + ", from records by key, invalidated it and added it to invalidated records.");
    }

    /** Clears all records from the index and resets all mappings, stop words and synonyms. */
    private void clearAllRecords()
    {
        // log.debug("private void clearAllRecords(): called");

        index.clear();
        indexRecordsByKey.clear();
        // invalidatedRecords.clear();
        // log.debug("Cleared records by key.");
    }

    /**
     * Queries the index for a term. The index maps terms onto keys, and the keys matched must be looked up in the look
     * up map from keys to index records. If records have been deleted they will have been removed from the look up map,
     * so they are not returned by this method. Any dangling keys detected by this method are cleaned out of the index.
     *
     * @param  term The term to search for.
     *
     * @return A set of matching index records.
     */
    private Set<IndexRecord> queryLiveRecords(String term)
    {
        // log.debug("private Set<IndexRecord> queryLiveRecords(String term): called");

        Set<IndexRecord> results = new HashSet<IndexRecord>();

        // Used to build up a list of invalidated keys to sweep out of the index.
        Collection<InvalidateableKey<K>> keysToRemove = new HashSet<InvalidateableKey<K>>();

        // Query the index for all keys matching the term.
        Set<InvalidateableKey<K>> keys = index.get(term);

        if (keys != null)
        {
            // Scan through the keys looking for ones that map to live records, and cleaning up others.
            for (InvalidateableKey<K> key : keys)
            {
                // Check if the key is marked as invalid and add it to the set of keys to remove if so.
                if (key.invalidated)
                {
                    keysToRemove.add(key);
                    // log.debug("Added key, " + key + ", to the set of keys to remove.");
                }
                else
                {
                    // Try to get a live record for the key.
                    IndexRecord record = indexRecordsByKey.get(key.key);
                    results.add(record);
                }
            }

            // Remove all invalidated keys that were encountered, from the index.
            keys.removeAll(keysToRemove);
            // log.debug("Removed all invalidated keys encountered.");
        }

        return results;
    }

    /**
     * Waits until the global write lock can be acquired by the specified transaction.
     *
     * @param  txId The transaction id to acquite the global write lock for.
     *
     * @throws InterruptedException If interrupted whilst waiting for the global write lock.
     */
    private void acquireGlobalWriteLock(IndexTxId txId) throws InterruptedException
    {
        // Get the global write lock to ensure only one thread at a time can execute this code.
        globalLock.writeLock().lock();

        // Use a try block so that the corresponding finally block guarantees release of the thread lock.
        try
        {
            // Check that this transaction does not already own the lock.
            if (!txId.equals(globalWriteLockTxId))
            {
                // Wait until the write lock becomes free.
                while (globalWriteLockTxId != null)
                {
                    globalWriteLockFree.await();
                }

                // Assign the global write lock to this transaction.
                globalWriteLockTxId = txId;
            }
        }
        finally
        {
            // Ensure that the thread lock is released once assignment of the write lock to the transaction is complete.
            globalLock.writeLock().unlock();
        }
    }

    /**
     * Waits until a global read lock can be acquired by the specified transaction.
     *
     * @param txId The transaction id to acquite a global read lock for.
     */
    private void acquireGlobalReadLock(IndexTxId txId)
    {
        // Get the global write lock to ensure only one thread at a time can execute this code.
        globalLock.readLock().lock();
    }

    /**
     * InvalidateableKey is a key wrapper class that allows a key to be marked as invalid, once it is removed from the
     * index. Terms link to this structure, from which the key to look up an index record can be obtained. Whenever a
     * key that has been marked as invalid is encountered it is swept out of the index.
     *
     * <p/>Terms link to sets of keys. Whenever a new key is inserted against a term any existing invalidateable key is
     * removed and replaced with the fresh not invalidated key.
     *
     * <p/>InvalidateableKeys are identified by their underlying keys. The equality and hashCode methods are based on
     * the key only.
     */
    private class InvalidateableKey<K>
    {
        /** Holds the key. */
        public K key;

        /** Marks whether or not the key has been invalidated. */
        boolean invalidated = false;

        /**
         * Creates an invalidateable key from an underlying key.
         *
         * @param key The key.
         */
        public InvalidateableKey(K key)
        {
            this.key = key;
        }

        /**
         * Checks if two invalidateable keys are equal by their keys.
         *
         * @param  o The object to compare this one with.
         *
         * @return <tt>true</tt> if this object has the same key as the comparator, <tt>false</tt> otherwise.
         */
        public boolean equals(Object o)
        {
            if (this == o)
            {
                return true;
            }

            if (!(o instanceof InvalidateableKey))
            {
                return false;
            }

            InvalidateableKey invalidateableKey = (InvalidateableKey) o;

            if ((key != null) ? (!key.equals(invalidateableKey.key)) : (invalidateableKey.key != null))
            {
                return false;
            }

            return true;
        }

        /**
         * Computes a hash code based on the key only.
         *
         * @return A hash code based on the key.
         */
        public int hashCode()
        {
            return ((key != null) ? key.hashCode() : 0);
        }

        /**
         * Generates string representation for debugging.
         *
         * @return A string representation for debugging
         */
        public String toString()
        {
            return "key: [" + key + "], invalidated: " + invalidated;
        }
    }

    /**
     * This IndexRecord structure is used to encapsulate the index record key and summary record together so that they
     * may be refered to together by the index.
     *
     * <p/>IndexRecords are identified by their keys. The equality and hashCode methods are based on the key only.
     */
    private class IndexRecord
    {
        /** The key used to identify the indexed record. */
        public InvalidateableKey<K> key;

        /** The summary record that gets kept in the index. */
        public E summaryRecord;

        /** Holds the record rating, extrcated for convenience from the summary record. */
        public float rating;

        /** The write lock for this record that allows concurrent reads. */
        public ReadWriteLock writeLock = new ReentrantReadWriteLock();

        /**
         * Checks if two index records are equal by their keys.
         *
         * @param  o The object to compare this one with.
         *
         * @return <tt>true</tt> if this object has the same key as the comparator, <tt>false</tt> otherwise.
         */
        public boolean equals(Object o)
        {
            try
            {
                return ((IndexRecord) o).key.equals(key);
            }

            // This exception is swallowed and the correct result, false, is returned. This use of exceptions in flow of
            // logic is not ideal. However, the expression (o instanceof IndexRecord) cannot be compiled as it results in
            // the compile error, "illegal generic type for instanceof", because this class is parameterized by the
            // type variables of its parent class.
            catch (ClassCastException e)
            {
                return false;
            }
        }

        /**
         * Computes a hash code for the index record by its key.
         *
         * @return A hash code based on the key.
         */
        public int hashCode()
        {
            return key.hashCode();
        }

        /**
         * Generates string representation for debugging.
         *
         * @return A string representation for debugging
         */
        public String toString()
        {
            return "key: [" + key + "], rating: " + rating + ", summaryRecord: [" + summaryRecord + "]";
        }
    }

    /**
     * Insertions, deletions and changes to index records are not applied to the index immediately in transactional mode
     * but are stored in a write-behind cache and applied upon transaction commit only. This class records the different
     * types of alterations that are to be made to the index record set.
     */
    private abstract class RecordAlteration
    {
        /** Applies the write-behind cached operation to the index. */
        public abstract void execute();
    }

    /**
     * Encapsulates an update to the index as a cached transactional write-behind operation.
     */
    private class UpdateRecord extends RecordAlteration
    {
        /** Used for logging. */
        //private final Logger log = Logger.getLogger(UpdateRecord.class);

        /** The key of the record to modify. */
        private K key;

        /** The new summary record. */
        private E indexEntry;

        /** The new rating. */
        private float rating;

        /**
         * Creates a cached write-behind entry for updating a record.
         *
         * @param key        The key of the record to modify.
         * @param indexEntry The new summary record.
         * @param rating     The new records rating.
         */
        public UpdateRecord(K key, E indexEntry, float rating)
        {
            this.key = key;
            this.indexEntry = indexEntry;
            this.rating = rating;
        }

        /** Applies the cached write-behind operation to the index, updating the record. */
        public void execute()
        {
            // log.debug("public void execute(): called");
            updateRecord(key, indexEntry, rating);
        }
    }

    /**
     * Encapsulates removal of a record as a cached transaction write-behind operation.
     */
    private class RemoveRecord extends RecordAlteration
    {
        /** Used for logging. */
        //private final Logger log = Logger.getLogger(RemoveRecord.class);

        /** The key of the record to be removed. */
        private K key;

        /**
         * Creates a cached write-behind operation for removing a record.
         *
         * @param key The key of the record to be removed.
         */
        public RemoveRecord(K key)
        {
            this.key = key;
        }

        /** Executes the cached write behind operation, removing the record from the index. */
        public void execute()
        {
            // log.debug("public void execute(): called");
            removeRecord(key);
        }
    }

    /**
     * Encapsulates clearing the whole index as a cached transactional write-behind operation.
     */
    private class ClearAllRecords extends RecordAlteration
    {
        /** Used for logging. */
        //private final Logger log = Logger.getLogger(ClearAllRecords.class);

        /** Creates the cached write behind operation. */
        public ClearAllRecords()
        {
        }

        /** Exceutes the clearing of the whole index. */
        public void execute()
        {
            // log.debug("public void execute(): called");
            clearAllRecords();
        }
    }

    /**
     * Encapsulates adding a new record to the index as a cached transactional write-behind operation.
     */
    private class AddRecord extends RecordAlteration
    {
        /** Used for logging. */
        //private final Logger log = Logger.getLogger(AddRecord.class);

        /** The key of the new record. */
        K key;

        /** The new summary record to store. */
        E indexEntry;

        /** The new records rating. */
        float rating;

        /** The set of words to index the new record against. */
        Set<String> setOfWords;

        /**
         * Creates a cached transaction write-behind operation to add a new record to the index.
         *
         * @param key        The new records key.
         * @param indexEntry The new summary record.
         * @param rating     The new records rating.
         * @param setOfWords The set of words to index the new record against.
         */
        public AddRecord(K key, E indexEntry, float rating, Set<String> setOfWords)
        {
            this.key = key;
            this.indexEntry = indexEntry;
            this.rating = rating;
            this.setOfWords = setOfWords;
        }

        /** Executes the cached transaction write-behind operation, adding a new record to the index. */
        public void execute()
        {
            // log.debug("public void execute(): called");
            addNewRecord(key, indexEntry, rating, setOfWords);
        }
    }

    /**
     * This comparator is used to compare index records by their ratings to facilitate the ordering of search results by
     * their ratings.
     */
    private class RatingComparator implements Comparator<IndexRecord>
    {
        /**
         * Compares two ratings. This is a reversed comparator because the results list should be returned in order of
         * the highest ratings first.
         *
         * @param  record1 The first index record to compare by rating.
         * @param  record2 The second index record to compare by rating.
         *
         * @return -1 if record1 has a higher rating score, 0 if they have the same rating score and, 1 if record1 has a
         *         lower rating score.
         */
        public int compare(IndexRecord record1, IndexRecord record2)
        {
            return (record1.rating > record2.rating) ? -1 : ((record1.rating < record2.rating) ? 1 : 0);
        }
    }
}
