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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.log4j.Logger;
import org.apache.log4j.NDC;

import com.thesett.index.IndexTestBase;
import com.thesett.index.TransactionalIndex;
import com.thesett.index.TransactionalIndexTestBase;

/**
 * ProtoIndexTest is a pure unit test class for the prototype index implementation, {@link ProtoIndex}.
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
public class ProtoIndexTest extends TestCase
{
    /** Used for logging. */
    private static final Logger log = Logger.getLogger(ProtoIndexTest.class);

    public ProtoIndexTest(String testName)
    {
        super(testName);
    }

    /**
     * Compile all the tests for the default test implementation of an index into a test suite plus any tests defined in
     * this test class.
     */
    public static Test suite()
    {
        // Build a new test suite
        TestSuite suite = new TestSuite("ProtoIndex Tests");

        ProtoIndex testIndex;

        // Add all tests defined in the IndexTestBase class.
        testIndex = new ProtoIndex();
        suite.addTest(new IndexTestBase("testIndexedFieldsRead", testIndex, testIndex));
        testIndex = new ProtoIndex();
        suite.addTest(new IndexTestBase("testIndexedFieldsReadForAllMappings", testIndex, testIndex));
        testIndex = new ProtoIndex();
        suite.addTest(new IndexTestBase("testAddRecordOk", testIndex, testIndex));
        testIndex = new ProtoIndex();
        suite.addTest(new IndexTestBase("testAddRecordFailsNoMapping", testIndex, testIndex));
        testIndex = new ProtoIndex();
        suite.addTest(new IndexTestBase("testAddRecordFailsMissingFields", testIndex, testIndex));
        testIndex = new ProtoIndex();
        suite.addTest(new IndexTestBase("testUpdateSummaryRecordOk", testIndex, testIndex));
        testIndex = new ProtoIndex();
        suite.addTest(new IndexTestBase("testUpdateSummaryRecordFailsUnknownKey", testIndex, testIndex));
        testIndex = new ProtoIndex();
        suite.addTest(new IndexTestBase("testUpdateRatingOk", testIndex, testIndex));
        testIndex = new ProtoIndex();
        suite.addTest(new IndexTestBase("testUpdateFullRecordOk", testIndex, testIndex));
        testIndex = new ProtoIndex();
        suite.addTest(new IndexTestBase("testUpdateFullRecordFailsUnknownKey", testIndex, testIndex));
        testIndex = new ProtoIndex();
        suite.addTest(new IndexTestBase("testUpdateFullRecordCorrectlyUpdatesIndexing", testIndex, testIndex));
        testIndex = new ProtoIndex();
        suite.addTest(new IndexTestBase("testRemoveRecordOk", testIndex, testIndex));
        testIndex = new ProtoIndex();
        suite.addTest(new IndexTestBase("testRemoveRecordFailsUnknownKey", testIndex, testIndex));
        testIndex = new ProtoIndex();
        suite.addTest(new IndexTestBase("testEmptyIndexNoMatches", testIndex, testIndex));
        testIndex = new ProtoIndex();
        suite.addTest(new IndexTestBase("testResultsInRatingOrder", testIndex, testIndex));
        testIndex = new ProtoIndex();
        suite.addTest(new IndexTestBase("testStopWordsNotIndexed", testIndex, testIndex));
        testIndex = new ProtoIndex();
        suite.addTest(new IndexTestBase("testSynonymMatchesExpanded", testIndex, testIndex));

        // Check that all the isolation modes can be set.
        testIndex = new ProtoIndex();
        suite.addTest(new TransactionalIndexTestBase("testLevelReadUncommittedOk", testIndex, testIndex));
        testIndex = new ProtoIndex();
        suite.addTest(new TransactionalIndexTestBase("testLevelReadCommittedOk", testIndex, testIndex));
        testIndex = new ProtoIndex();
        suite.addTest(new TransactionalIndexTestBase("testLevelRepeatableReadOk", testIndex, testIndex));
        testIndex = new ProtoIndex();
        suite.addTest(new TransactionalIndexTestBase("testLevelSerializableOk", testIndex, testIndex));

        // Check that commits and rollbacks work ok in read uncommitted mode.
        ProtoIndex readUncommittedIndex = new ProtoIndex();
        readUncommittedIndex.setTransactionalMode(TransactionalIndex.IsolationLevel.ReadUncommitted);
        suite.addTest(new TransactionalIndexTestBase("testRollbacksDropped", readUncommittedIndex,
                readUncommittedIndex));
        suite.addTest(new TransactionalIndexTestBase("testCommitsSaved", readUncommittedIndex, readUncommittedIndex));

        // Check that commits, rollbacks and no dirty reads work ok in read committed mode.
        ProtoIndex readCommittedIndex = new ProtoIndex();
        readCommittedIndex.setTransactionalMode(TransactionalIndex.IsolationLevel.ReadCommitted);
        suite.addTest(new TransactionalIndexTestBase("testRollbacksDropped", readCommittedIndex, readCommittedIndex));
        suite.addTest(new TransactionalIndexTestBase("testCommitsSaved", readCommittedIndex, readCommittedIndex));
        suite.addTest(new TransactionalIndexTestBase("testNoDirtyReads", readCommittedIndex, readCommittedIndex));

        // Check that commits, rollbacks, no dirty reads and repeatable reads work ok in repeatable read mode.
        /*
        ProtoIndex repeatableReadIndex = new ProtoIndex();
        repeatableReadIndex.setTransactionalMode(TransactionalIndex.IsolationLevel.RepeatableRead);
        suite.addTest(new TransactionalIndexTestBase("testRollbacksDropped", repeatableReadIndex));
        suite.addTest(new TransactionalIndexTestBase("testCommitsSaved", repeatableReadIndex));
        suite.addTest(new TransactionalIndexTestBase("testNoDirtyReads", repeatableReadIndex));
        suite.addTest(new TransactionalIndexTestBase("testNoNonRepeatableReads", repeatableReadIndex));
        suite.addTest(new TransactionalIndexTestBase("testRepeatableReads", repeatableReadIndex));

        // Check that commits, rollbacks, no dirty reads, repeatable reads and no phantom reads work ok in serializable mode.
        ProtoIndex serializableIndex = new ProtoIndex();
        serializableIndex.setTransactionalMode(TransactionalIndex.IsolationLevel.Serializable);
        suite.addTest(new TransactionalIndexTestBase("testRollbacksDropped", serializableIndex));
        suite.addTest(new TransactionalIndexTestBase("testCommitsSaved", serializableIndex));
        suite.addTest(new TransactionalIndexTestBase("testNoDirtyReads", serializableIndex));
        suite.addTest(new TransactionalIndexTestBase("testNoNonRepeatableReads", serializableIndex));
        suite.addTest(new TransactionalIndexTestBase("testRepeatableReads", serializableIndex));
        suite.addTest(new TransactionalIndexTestBase("testNoPhantomReads", serializableIndex));
        */

        // Add all the tests defined in this class (using the default constructor)
        // suite.addTestSuite(ProtoIndexTest.class);

        return suite;
    }

    protected void setUp() throws Exception
    {
        NDC.push(getName());
    }

    protected void tearDown() throws Exception
    {
        NDC.pop();
    }
}
