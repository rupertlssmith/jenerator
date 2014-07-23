/* Copyright Rupert Smith, 2005 to 2008, all rights reserved. */
package com.thesett.index;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.NDC;

/**
 * IndexTestBase is a pure unit test base class for deriving test classes for implementations of the
 * {@link Index} interface. The class name deliberately does not end in Test so that it will not be run
 * as a unit test by default. There is no suitable constructor for building and calling objects of this class from
 * JUnit. It is designed to be called explicitly from sub-classes that implement unit tests for specific index
 * implementations that re-use the tests defined here.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Check that the indexed fields are read from test records.
 * <tr><td> Check that the indexed fields are read from test records for all class mappings that the record is an
 *          instance of.
 * <tr><td> Check that adding a record to the index succeeds.
 * <tr><td> Check that adding a record to the index fails where there is no type mapping for it.
 * <tr><td> Check that adding a record to the index fails where the record is missing some fields in the type mapping.
 * <tr><td> Check that updating a summary record succeeds.
 * <tr><td> Check that updating a summary record for an unknown record key fails.
 * <tr><td> Check that rating can be updated on the summary record.
 * <tr><td> Check that updating a full record succeeds.
 * <tr><td> Check that updating a full record fails for an unknown record key.
 * <tr><td> Check that updating a full record correctly updates its indexing.
 * <tr><td> Check that removing a record from the index succeeds.
 * <tr><td> Check that removing a record from the index fails for an unknown record key.
 * <tr><td> Check that the index produces no matches once cleared.
 * <tr><td> Check that otherwise identical records are returned in rating order.
 * <tr><td> Check that stop words are not indexed.
 * <tr><td> Check that synonym matches are expanded.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class IndexTestBase extends TestCase
{
    /** Used for logging. */
    private static final Logger log = Logger.getLogger(IndexTestBase.class);

    /** The {@link Index} to test.*/
    Index testIndex;

    /** The test indexes setup instance. */
    IndexSetup testIndexSetup;

    /**
     * Builds the tests to be run on a supplied index implementation. This allows the tests in this class to
     * be applied to arbitrary index implementations in sub-classes of this test class.
     *
     * @param testName  The name of the unit test.
     * @param testIndex The {@link Index} to test.
     * @param setup     The test indexes setup interface.
     */
    public IndexTestBase(String testName, Index testIndex, IndexSetup setup)
    {
        super(testName);

        // Keep reference to the index implementation to test.
        this.testIndex = testIndex;
        this.testIndexSetup = setup;
    }

    /**
     * Exposes the set up method as public so that it can be called from outside this class.
     *
     * @throws Exception All test exceptions are allowed to fall through.
     */
    public void publicSetUp() throws Exception
    {
        setUp();
    }

    /**
     * Exposes the tear down method as public so that it can be called from outside this class.
     *
     * @throws Exception All test exceptions are allowed to fall through.
     */
    public void publicTearDown() throws Exception
    {
        tearDown();
    }

    /**
     * Allows the index to be tested to be set after the constructor is called. Must be called before the set up method.
     *
     * @param index The index to test.
     */
    public void setTestIndex(Index index)
    {
        this.testIndex = index;
    }

    /**
     * Allows the index setup interface to be tested to be set after the constructor is called. Must be called before
     * the set up method.
     *
     * @param setup The test index setup interface.
     */
    public void setTestIndexSetup(IndexSetup setup)
    {
        this.testIndexSetup = setup;
    }

    /**
     * Check that the indexed fields are read from test records.
     *
     * @throws Exception All test exceptions are allowed to fall through.
     */
    public void testIndexedFieldsRead() throws Exception
    {
        String errorMessage = "";

        // Create a sample record to insert into the index.
        TestRecord testRecord =
            new TestRecord(1L, "The quick brown fox jumped over the lazy dogs.", "Test Record", 1.0f);

        // Add a mapping to the index for the sample record.
        IndexMapping mapping = new IndexMapping(new String[] { "text", "title" }, "rating");
        testIndexSetup.addMapping(TestRecord.class, TestRecord.TestRecordSummary.class, mapping);

        // Index the sample record.
        testIndex.add(testRecord.getKey(), testRecord, testRecord.getSummaryRecord());

        // Check that the correct fields were read from the sample record.
        if (!testRecord.textFieldAccessed)
        {
            errorMessage += "The mapped field \"text\" from the test record was not read.\n";
        }

        if (!testRecord.titleFieldAccessed)
        {
            errorMessage += "The mapped field \"title\" from the test record was not read.\n";
        }

        // Check that there were no errors during the test and display them if there were.
        assertTrue(errorMessage, "".equals(errorMessage));
    }

    /**
     * Check that the indexed fields are read from test records for all class mappings that the record is an
     * instance of.
     *
     * @throws Exception All test exceptions are allowed to fall through.
     */
    public void testIndexedFieldsReadForAllMappings() throws Exception
    {
        String errorMessage = "";

        // Create a sample record to insert into the index.
        TestRecord testRecord =
            new TestRecord(1L, "The quick brown fox jumped over the lazy dogs.", "Test Record", 1.0f);

        // Add a mapping to the index for all objects for the "text" property.
        IndexMapping baseMapping = new IndexMapping(new String[] { "text" }, "rating");
        testIndexSetup.addMapping(Object.class, TestRecord.TestRecordSummary.class, baseMapping);

        // Add a mapping to the index for the sample record type for the "title" property.
        IndexMapping mapping = new IndexMapping(new String[] { "title" }, "rating");
        testIndexSetup.addMapping(TestRecord.class, TestRecord.TestRecordSummary.class, mapping);

        // Index the sample record.
        testIndex.add(testRecord.getKey(), testRecord, testRecord.getSummaryRecord());

        // Check that the correct fields were read from the sample record.
        if (!testRecord.textFieldAccessed)
        {
            errorMessage += "The mapped field \"text\" from the test record was not read.\n";
        }

        if (!testRecord.titleFieldAccessed)
        {
            errorMessage += "The mapped field \"title\" from the test record was not read.\n";
        }

        // Check that there were no errors during the test and display them if there were.
        assertTrue(errorMessage, "".equals(errorMessage));
    }

    /**
     * Check that adding a record to the index succeeds.
     *
     * @throws Exception All test exceptions are allowed to fall through.
     */
    public void testAddRecordOk() throws Exception
    {
        // Create a sample record to insert into the index.
        TestRecord testRecord =
            new TestRecord(1L, "The quick brown fox jumped over the lazy dogs.", "Test Record", 1.0f);

        // Add a mapping to the index for the sample record.
        IndexMapping mapping = new IndexMapping(new String[] { "text", "title" }, "rating");
        testIndexSetup.addMapping(TestRecord.class, TestRecord.TestRecordSummary.class, mapping);

        // Index the sample record.
        testIndex.add(testRecord.getKey(), testRecord, testRecord.getSummaryRecord());
    }

    /**
     * Check that adding a record to the index fails where there is no type mapping for it. *
     *
     * @throws Exception All test exceptions are allowed to fall through.
     */
    public void testAddRecordFailsNoMapping() throws Exception
    {
        // Create a sample record to insert into the index.
        TestRecord testRecord =
            new TestRecord(1L, "The quick brown fox jumped over the lazy dogs.", "Test Record", 1.0f);

        // Try to index the sample record.
        boolean testPassed = false;

        try
        {
            testIndex.add(testRecord.getKey(), testRecord, testRecord.getSummaryRecord());
        }
        catch (IndexMappingException e)
        {
            e = null;
            testPassed = true;
        }

        // Check that the correct exception was thrown.
        assertTrue("IndexMappingException was not thrown but no mapping was given for the test record.", testPassed);
    }

    /**
     * Check that adding a record to the index fails where the record is missing some fields in the type mapping. *
     *
     * @throws Exception All test exceptions are allowed to fall through.
     */
    public void testAddRecordFailsMissingFields() throws Exception
    {
        // Create a sample record to insert into the index.
        TestRecord testRecord =
            new TestRecord(1L, "The quick brown fox jumped over the lazy dogs.", "Test Record", 1.0f);

        // Add a mapping to the index for the sample record that contains fields not in the sample record.
        IndexMapping mapping = new IndexMapping(new String[] { "nosuchfield" }, "rating");
        testIndexSetup.addMapping(TestRecord.class, TestRecord.TestRecordSummary.class, mapping);

        // Try to index the sample record.
        boolean testPassed = false;

        try
        {
            testIndex.add(testRecord.getKey(), testRecord, testRecord.getSummaryRecord());
        }
        catch (IndexMappingException e)
        {
            e = null;
            testPassed = true;
        }

        // Check that the correct exception was thrown.
        assertTrue(
            "IndexMappingException was not thrown but a mapping specifying unknown fields was set on the index " +
            "for the test record type.", testPassed);
    }

    /**
     * Check that updating a summary record succeeds. *
     *
     * @throws Exception All test exceptions are allowed to fall through.
     */
    public void testUpdateSummaryRecordOk() throws Exception
    {
        // Create a sample record to insert into the index.
        TestRecord testRecord =
            new TestRecord(1L, "The quick brown fox jumped over the lazy dogs.", "Test Record", 1.0f);

        // Add a mapping to the index for the sample record.
        IndexMapping mapping = new IndexMapping(new String[] { "text", "title" }, "rating");
        testIndexSetup.addMapping(TestRecord.class, TestRecord.TestRecordSummary.class, mapping);

        // Index the sample record.
        testIndex.add(testRecord.getKey(), testRecord, testRecord.getSummaryRecord());

        // Update summary record of the indexed sample.
        TestRecord.TestRecordSummary summary = testRecord.getSummaryRecord();
        summary.rating = 2.0f;
        testIndex.update(1L, summary);
    }

    /**
     * Check that updating a summary record for an unknown record key fails. *
     *
     * @throws Exception All test exceptions are allowed to fall through.
     */
    public void testUpdateSummaryRecordFailsUnknownKey() throws Exception
    {
        // Create a sample record to insert into the index.
        TestRecord testRecord =
            new TestRecord(1L, "The quick brown fox jumped over the lazy dogs.", "Test Record", 1.0f);

        // Add a mapping to the index for the sample record.
        IndexMapping mapping = new IndexMapping(new String[] { "text", "title" }, "rating");
        testIndexSetup.addMapping(TestRecord.class, TestRecord.TestRecordSummary.class, mapping);

        // Index the sample record.
        testIndex.add(testRecord.getKey(), testRecord, testRecord.getSummaryRecord());

        // Try to update the summary record of the indexed sample but use the wrong key.
        boolean testPassed = false;

        try
        {
            TestRecord.TestRecordSummary summary = testRecord.getSummaryRecord();
            summary.rating = 2.0f;
            testIndex.update(2L, summary);
        }
        catch (IndexUnknownKeyException e)
        {
            e = null;
            testPassed = true;
        }

        // Check that the correct exception was thrown.
        assertTrue("IndexUnknownKeyException was not thrown but an attempt to update a record by an " +
            "incorrect key was made.", testPassed);
    }

    /**
     * Check that rating can be updated on the summary record. *
     *
     * @throws Exception All test exceptions are allowed to fall through.
     */
    public void testUpdateRatingOk() throws Exception
    {
        String errorMessage = "";

        // Create two sample records to insert into the index with identical texts but different ratings.
        TestRecord testRecord1 = new TestRecord(1L, "The quick brown fox jumped over the lazy dogs.", "Record1", 0.5f);
        TestRecord testRecord2 = new TestRecord(2L, "The quick brown fox jumped over the lazy dogs.", "Record2", 1.0f);

        // Add a mapping to the index for the sample records.
        IndexMapping mapping = new IndexMapping(new String[] { "text" }, "rating");
        testIndexSetup.addMapping(TestRecord.class, TestRecord.TestRecordSummary.class, mapping);

        // Index the sample records.
        testIndex.add(testRecord1.getKey(), testRecord1, testRecord1.getSummaryRecord());
        testIndex.add(testRecord2.getKey(), testRecord2, testRecord2.getSummaryRecord());

        // Run a query against the index to retrieve both identically indexed records in their orignal order.
        Map<Long, TestRecord.TestRecordSummary> resultsMap = testIndex.search("fox");
        Iterator<TestRecord.TestRecordSummary> results = resultsMap.values().iterator();

        // Check that the ratings mean that the second record comes first as it has a higher rating score.
        String resultsTitle1 = results.next().title;

        if (!"Record2".equals(resultsTitle1))
        {
            errorMessage +=
                "Was expecting first record in results, before adjusting ratings, to be \"Record2\" but was " +
                resultsTitle1 + ".\n";
        }

        String resultsTitle2 = results.next().title;

        if (!"Record1".equals(resultsTitle2))
        {
            errorMessage +=
                "Was expecting second record in results, before adjusting ratings, to be \"Record1\" but was " +
                resultsTitle2 + ".\n";
        }

        // Update the rating of the second record so that it is lower than the first one.
        TestRecord.TestRecordSummary summary = testRecord2.getSummaryRecord();
        summary.rating = 0.25f;
        testIndex.update(testRecord2.getKey(), summary);

        // Run a query against the index to retrieve both identically indexed records.
        resultsMap = testIndex.search("fox");
        results = resultsMap.values().iterator();

        // Check that the rating change on the second record has succeeded so that it comes after the first record
        // in the results ordering.
        resultsTitle1 = results.next().title;

        if (!"Record1".equals(resultsTitle1))
        {
            errorMessage += "Was expecting first record in results to be \"Record1\" but was " + resultsTitle1 + ".\n";
        }

        resultsTitle2 = results.next().title;

        if (!"Record2".equals(resultsTitle2))
        {
            errorMessage += "Was expecting second record in results to be \"Record2\" but was " + resultsTitle2 + ".\n";
        }

        // Check that there were no errors during the test and display them if there were.
        assertTrue(errorMessage, "".equals(errorMessage));
    }

    /**
     * Check that updating a full record succeeds. *
     *
     * @throws Exception All test exceptions are allowed to fall through.
     */
    public void testUpdateFullRecordOk() throws Exception
    {
        // Create a sample record to insert into the index.
        TestRecord testRecord = new TestRecord(1L, "The quick brown fox jumped over the lazy dogs.", "Record1", 1.0f);

        // Add a mapping to the index for the sample record.
        IndexMapping mapping = new IndexMapping(new String[] { "text", "title" }, "rating");
        testIndexSetup.addMapping(TestRecord.class, TestRecord.TestRecordSummary.class, mapping);

        // Index the sample record.
        testIndex.add(testRecord.getKey(), testRecord, testRecord.getSummaryRecord());

        // Create a new sample record with the same key and update the index with it.
        TestRecord testRecord2 = new TestRecord(1L, "A man, a plan, a canal Panama.", "Record2", 1.0f);
        testIndex.update(testRecord2.getKey(), testRecord2, testRecord2.getSummaryRecord());

        // Run a query and check that the replacement record is returned.
        Map<Long, TestRecord.TestRecordSummary> resultsMap = testIndex.search("canal");
        TestRecord.TestRecordSummary summaryRecord = resultsMap.get(1L);

        assertNotNull("Was expecting a result for key 1L, but got null for it.", summaryRecord);

        assertTrue("Was expecting \"Record2\" but got " + summaryRecord.title + ".\n",
            "Record2".equals(summaryRecord.title));
    }

    /**
     * Check that updating a full record fails for an unknown record key. *
     *
     * @throws Exception All test exceptions are allowed to fall through.
     */
    public void testUpdateFullRecordFailsUnknownKey() throws Exception
    {
        // Create a sample record to insert into the index.
        TestRecord testRecord =
            new TestRecord(1L, "The quick brown fox jumped over the lazy dogs.", "Test Record", 1.0f);

        // Add a mapping to the index for the sample record.
        IndexMapping mapping = new IndexMapping(new String[] { "text", "title" }, "rating");
        testIndexSetup.addMapping(TestRecord.class, TestRecord.TestRecordSummary.class, mapping);

        // Index the sample record.
        testIndex.add(testRecord.getKey(), testRecord, testRecord.getSummaryRecord());

        // Try to update the full record of the indexed sample but use the wrong key.
        boolean testPassed = false;

        try
        {
            testRecord = new TestRecord(2L, "Too many cooks spoil the broth.", "Test Record", 1.0f);
            testIndex.update(testRecord.getKey(), testRecord, testRecord.getSummaryRecord());
        }
        catch (IndexUnknownKeyException e)
        {
            e = null;
            testPassed = true;
        }

        // Check that the correct exception was thrown.
        assertTrue("IndexUnknownKeyException was not thrown but an attempt to update a record by an " +
            "incorrect key was made.", testPassed);
    }

    /**
     * Check that updating a full record correctly updates its indexing.
     *
     * @throws Exception All test exceptions are allowed to fall through.
     */
    public void testUpdateFullRecordCorrectlyUpdatesIndexing() throws Exception
    {
        // Create a sample record to insert into the index.
        TestRecord testRecord = new TestRecord(1L, "The quick brown fox jumped over the lazy dogs.", "Record1", 1.0f);

        // Add a mapping to the index for the sample record.
        IndexMapping mapping = new IndexMapping(new String[] { "text", "title" }, "rating");
        testIndexSetup.addMapping(TestRecord.class, TestRecord.TestRecordSummary.class, mapping);

        // Index the sample record.
        testIndex.add(testRecord.getKey(), testRecord, testRecord.getSummaryRecord());

        // Create a new sample record with the same key and update the index with it.
        TestRecord testRecord2 = new TestRecord(1L, "A man, a plan, a canal Panama.", "Record2", 1.0f);
        testIndex.update(testRecord2.getKey(), testRecord2, testRecord2.getSummaryRecord());

        // Run a query against terms that only appear in to original record and check that the new record is not returned.
        Map<Long, TestRecord.TestRecordSummary> resultsMap = testIndex.search("dogs");

        assertTrue("There should be no results for the query, but there were " + resultsMap.size() + ".",
            resultsMap.isEmpty());
    }

    /**
     * Check that removing a record from the index succeeds. *
     *
     * @throws Exception All test exceptions are allowed to fall through.
     */
    public void testRemoveRecordOk() throws Exception
    {
        // Create a sample record to insert into the index.
        TestRecord testRecord = new TestRecord(1L, "The quick brown fox jumped over the lazy dogs.", "Record1", 1.0f);

        // Add a mapping to the index for the sample record.
        IndexMapping mapping = new IndexMapping(new String[] { "text", "title" }, "rating");
        testIndexSetup.addMapping(TestRecord.class, TestRecord.TestRecordSummary.class, mapping);

        // Index the sample record.
        testIndex.add(testRecord.getKey(), testRecord, testRecord.getSummaryRecord());

        // Remove the sample record.
        testIndex.remove(1L);

        // Run a query and check that it returns no results.
        Map<Long, TestRecord.TestRecordSummary> resultsMap = testIndex.search("fox");

        assertTrue("Was expecting the results set to be empty but it contains " + resultsMap.size() + " records.\n",
            resultsMap.isEmpty());
    }

    /**
     * Check that removing a record from the index fails for an unknown record key. *
     *
     * @throws Exception All test exceptions are allowed to fall through.
     */
    public void testRemoveRecordFailsUnknownKey() throws Exception
    {
        // Create a sample record to insert into the index.
        TestRecord testRecord =
            new TestRecord(1L, "The quick brown fox jumped over the lazy dogs.", "Test Record", 1.0f);

        // Add a mapping to the index for the sample record.
        IndexMapping mapping = new IndexMapping(new String[] { "text", "title" }, "rating");
        testIndexSetup.addMapping(TestRecord.class, TestRecord.TestRecordSummary.class, mapping);

        // Index the sample record.
        testIndex.add(testRecord.getKey(), testRecord, testRecord.getSummaryRecord());

        // Try to remove the record but use the wrong key.
        boolean testPassed = false;

        try
        {
            testIndex.remove(2L);
        }
        catch (IndexUnknownKeyException e)
        {
            e = null;
            testPassed = true;
        }

        // Check that the correct exception was thrown.
        assertTrue("IndexUnknownKeyException was not thrown but an attempt to remove a record by an " +
            "incorrect key was made.", testPassed);
    }

    /**
     * Check that the index produces no matches once cleared. *
     *
     * @throws Exception All test exceptions are allowed to fall through.
     */
    public void testEmptyIndexNoMatches() throws Exception
    {
        // Run a query and check that it returns no results.
        Map<Long, TestRecord.TestRecordSummary> resultsMap = testIndex.search("test");

        assertTrue("Was expecting the results set to be empty but it contains " + resultsMap.size() + " records.\n",
            resultsMap.isEmpty());
    }

    /**
     * Check that otherwise identical records are returned in rating order. *
     *
     * @throws Exception All test exceptions are allowed to fall through.
     */
    public void testResultsInRatingOrder() throws Exception
    {
        String errorMessage = "";

        // Create two sample records to insert into the index with identical texts but different ratings.
        TestRecord testRecord1 = new TestRecord(1L, "The quick brown fox jumped over the lazy dogs.", "Record1", 1.0f);
        TestRecord testRecord2 = new TestRecord(2L, "The quick brown fox jumped over the lazy dogs.", "Record2", 0.5f);

        // Add a mapping to the index for the sample records.
        IndexMapping mapping = new IndexMapping(new String[] { "text" }, "rating");
        testIndexSetup.addMapping(TestRecord.class, TestRecord.TestRecordSummary.class, mapping);

        // Index the sample records.
        testIndex.add(testRecord1.getKey(), testRecord1, testRecord1.getSummaryRecord());
        testIndex.add(testRecord2.getKey(), testRecord2, testRecord2.getSummaryRecord());

        // Run a query against the index to retrieve both identically indexed records.
        Map<Long, TestRecord.TestRecordSummary> resultsMap = testIndex.search("fox");
        Iterator<TestRecord.TestRecordSummary> results = resultsMap.values().iterator();

        // Check that the ratings the records has ordered the results so that the first record comes first.
        String resultsTitle1 = results.next().title;

        if (!"Record1".equals(resultsTitle1))
        {
            errorMessage += "Was expecting first record in results to be \"Record1\" but was " + resultsTitle1 + ".\n";
        }

        String resultsTitle2 = results.next().title;

        if (!"Record2".equals(resultsTitle2))
        {
            errorMessage += "Was expecting first record in results to be \"Record2\" but was " + resultsTitle2 + ".\n";
        }

        // Check that there were no errors during the test and display them if there were.
        assertTrue(errorMessage, "".equals(errorMessage));
    }

    /**
     * Check that stop words are not indexed. *
     *
     * @throws Exception All test exceptions are allowed to fall through.
     */
    public void testStopWordsNotIndexed() throws Exception
    {
        // Create a sample record to insert into the index.
        TestRecord testRecord = new TestRecord(1L, "The quick brown fox jumped over the lazy dogs.", "Record1", 1.0f);

        // Add a mapping to the index for the sample record.
        IndexMapping mapping = new IndexMapping(new String[] { "text", "title" }, "rating");
        testIndexSetup.addMapping(TestRecord.class, TestRecord.TestRecordSummary.class, mapping);

        // Set up some stop words.
        Collection<String> stopWords = Arrays.asList("fox");
        testIndexSetup.setStopWords(stopWords);

        // Index the sample record.
        testIndex.add(testRecord.getKey(), testRecord, testRecord.getSummaryRecord());

        // Run a query against a stop word and check that it returns no results.
        Map<Long, TestRecord.TestRecordSummary> resultsMap = testIndex.search("fox");

        for (TestRecord.TestRecordSummary summary : resultsMap.values())
        {
            log.info(summary.title);
        }

        assertTrue("Was expecting the results set to be empty but it contains " + resultsMap.size() + " records.\n",
            resultsMap.isEmpty());
    }

    /**
     * Check that synonym matches are expanded. *
     *
     * @throws Exception All test exceptions are allowed to fall through.
     */
    public void testSynonymMatchesExpanded() throws Exception
    {
        // Create a sample record to insert into the index.
        TestRecord testRecord = new TestRecord(1L, "The quick brown fox jumped over the lazy dogs.", "Record1", 1.0f);

        // Add a mapping to the index for the sample record.
        IndexMapping mapping = new IndexMapping(new String[] { "text", "title" }, "rating");
        testIndexSetup.addMapping(TestRecord.class, TestRecord.TestRecordSummary.class, mapping);

        // Set up some synonyms.
        Collection<String> matchingWords = Arrays.asList("quick");
        Set<String> matchingWordsSet = new HashSet<String>(matchingWords);
        Map<String, Set<String>> synonyms = new HashMap<String, Set<String>>();
        synonyms.put("fast", matchingWordsSet);
        testIndexSetup.setSynonyms(synonyms);

        // Index the sample record.
        testIndex.add(testRecord.getKey(), testRecord, testRecord.getSummaryRecord());

        // Run a query and check that the synonym matched record is returned.
        Map<Long, TestRecord.TestRecordSummary> resultsMap = testIndex.search("fast");
        TestRecord.TestRecordSummary summaryRecord = resultsMap.get(1L);

        assertTrue("Was expecting \"Record1\" but got " + summaryRecord.title + ".\n",
            "Record1".equals(summaryRecord.title));
    }

    /**
     * Clears the index to be tested. Resets its configuration.
     *
     * @throws Exception Any exceptions fall through this method and fail the test.
     */
    protected void setUp() throws Exception
    {
        // Make sur the index is in non-transactional mode and is empty.
        //testIndex.setTransactionalMode(false);
        testIndex.clear();
        testIndexSetup.reset();

        NDC.push(getName());
    }

    /**
     * No test specific tear down to do.
     *
     * @throws Exception Any exceptions fall through this method and fail the test.
     */
    protected void tearDown() throws Exception
    {
        NDC.pop();
    }
}
