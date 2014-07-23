/* Copyright Rupert Smith, 2005 to 2008, all rights reserved. */
package com.thesett.index;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.NDC;

import com.thesett.index.tx.IndexTxId;
import com.thesett.index.tx.IndexTxManager;
import com.thesett.junit.concurrency.TestRunnable;
import com.thesett.junit.concurrency.ThreadTestCoordinator;

/**
 * TransactionalIndexTestBase is a pure unit test base class for deriving test classes for implementations of the
 * {@link TransactionalIndex} interface. The class name deliberately does not end in Test so that it will not be run
 * as a unit test by default. There is no suitable constructor for building and calling objects of this class from
 * JUnit. It is designed to be called explicitly from sub-classes that implement unit tests for specific transactional
 * index implementations that re-use the tests defined here.
 *
 * <p/>It is up to the caller of the transaction isolation testing methods to set the desired isolation level to
 * be tested on the index passed to this class before running the test. Only some of these tests are appropriate
 * in some of the isolation levels, only all of them are appropriate to the highest level 'serializable'.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Check that the isolation level, 'none', may be set.
 * <tr><td> Check that the isolation level, 'read uncommitted', may be set.
 * <tr><td> Check that the isolation level, 'read committed', may be set.
 * <tr><td> Check that the isolation level, 'repeatable read', may be set.
 * <tr><td> Check that the isolation level, 'serializable', may be set.
 * <tr><td> Check that transactions cannot perform dirty reads of each others uncommitted writes.
 * <tr><td> Check that transactions do not perform non-repeatable reads of each others committed writes.
 * <tr><td> Check that transactions can perform repeatable reads even when another has committed writes to the data.
 * <tr><td> Check that committed transactions are visible to later transactions.
 * <tr><td> Check that rolled-back transactions are not visible to later transactions.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class TransactionalIndexTestBase extends TestCase
{
    /** Used for logging. */
    private static final Logger log = Logger.getLogger(TransactionalIndexTestBase.class);

    /** Sets the deadlock timeout to break blocked multi-thread interactions after. In milliseconds. */
    private static final int DEADLOCK_TIMEOUT = 5000;

    /** Sequence numbers used to make data unique accross many threads running the tests at the same time. */
    static AtomicLong sequence = new AtomicLong();

    /** The {@link TransactionalIndex} to test.*/
    TransactionalIndex testIndex;

    /** The indexes setup instance to configure it with. */
    IndexSetup testIndexSetup;

    /** First test transaction id. */
    //IndexTxId txId1;

    /** Second test transactio id. */
    //IndexTxId txId2;

    /**
     * Builds the tests to be run on a supplied transactional index implementation. This allows the tests in this class
     * to be applied to arbitrary index implementations in sub-classes of this test class.
     *
     * @param testName The name of the unit test.
     * @param testIndex The {@link TransactionalIndex} to test.
     */
    public TransactionalIndexTestBase(String testName, TransactionalIndex testIndex, IndexSetup setup)
    {
        super(testName);

        // Keep reference to the index implementation to test.
        this.testIndex = testIndex;
        this.testIndexSetup = setup;
    }

    /** Check that the isolation level, 'None', may be set. */
    public void testLevelNoneOk() throws Exception
    {
        testIndex.setTransactionalMode(TransactionalIndex.IsolationLevel.None);
    }

    /** Check that the isolation level, 'Read uncommitted', may be set. */
    public void testLevelReadUncommittedOk() throws Exception
    {
        testIndex.setTransactionalMode(TransactionalIndex.IsolationLevel.ReadUncommitted);
    }

    /** Check that the isolation level, 'Read Committed', may be set. */
    public void testLevelReadCommittedOk() throws Exception
    {
        testIndex.setTransactionalMode(TransactionalIndex.IsolationLevel.ReadCommitted);
    }

    /** Check that the isolation level, 'Repeatable Read', may be set. */
    public void testLevelRepeatableReadOk() throws Exception
    {
        testIndex.setTransactionalMode(TransactionalIndex.IsolationLevel.RepeatableRead);
    }

    /** Check that the isolation level, 'Serializable', may be set. */
    public void testLevelSerializableOk() throws Exception
    {
        testIndex.setTransactionalMode(TransactionalIndex.IsolationLevel.Serializable);
    }

    /** Check that transactions cannot perform dirty reads of each others uncommitted writes. */
    public void testNoDirtyReads() throws Exception
    {
        String errorMessage = "";

        // Create two local transaction ids to use in isolation tests between two transactions.
        final IndexTxId txId1 = IndexTxManager.createTxId();
        final IndexTxId txId2 = IndexTxManager.createTxId();

        // Grab some sequence numbers to make the records unique to this test. This allows the test to be run
        // multiple times concurrently without interfering with itself.
        long u1 = sequence.getAndIncrement();

        // Create some sample records to insert into the index.
        final TestRecord testRecord1 =
            new TestRecord(1L + (1000000L * u1), "The quick brown fox jumped over the lazy dogs.", "TestRecord1" + u1,
                1.0f);

        final TestRecord testRecord1Altered =
            new TestRecord(1L + (1000000L * u1), "The quick brown fox jumped over the lazy dogs.",
                "TestRecord1Altered" + u1, 1.0f);

        // Add a mapping to the index for the sample record.
        IndexMapping mapping = new IndexMapping(new String[] { "text", "title" }, "rating");
        testIndexSetup.addMapping(TestRecord.class, TestRecord.TestRecordSummary.class, mapping);

        // Index the record in transaction 1 and commit it.
        t(txId1);
        testIndex.add(testRecord1.getKey(), testRecord1, testRecord1.getSummaryRecord());
        testIndex.commit();

        // Create the first test transaction in its own thread.
        TestRunnable thread1 =
            new TestRunnable()
            {
                public void runWithExceptions() throws Exception
                {
                    NDC.push(getName());

                    // Set this thread to be associated with transaction 1. This remains for the life of the thread.
                    t(txId1);

                    boolean committedOk = false;

                    try
                    {
                        // Allow the second transaction to begin before making changes.
                        waitFor(new int[] { 1 }, false);

                        // Make an alteration to record 1. Transaction isolation may cause blocking at this point if
                        // transaction 2 has a blocking read lock on record 1.
                        testIndex.update(testRecord1.getKey(), testRecord1Altered,
                            testRecord1Altered.getSummaryRecord());

                        // Allow the second transaction to attempt to make a dirty read of the change and wait for it to get
                        // as far as doing this.
                        allow(new int[] { 1 });
                        waitFor(new int[] { 1 }, false);

                        // Commit this transaction.
                        testIndex.commit();
                        committedOk = true;
                    }
                    finally
                    {
                        if (!committedOk)
                        {
                            testIndex.rollback();
                        }

                        NDC.pop();
                    }
                }
            };

        // Create the second transaction in its own thread.
        TestRunnable thread2 =
            new TestRunnable()
            {
                public void runWithExceptions() throws Exception
                {
                    NDC.push(getName());

                    // Set this thread to be associated with transaction 2. This remains for the life of the thread.
                    t(txId2);

                    boolean committedOk = false;

                    try
                    {
                        // Read record1.
                        if (!canRead(testRecord1, testIndex))
                        {
                            addErrorMessage("Failed to read record 1 in transaction 2.\n");
                        }

                        // Allow transaction 1 to make some changes to the record and wait until it has completed
                        // those changes, or else is blocked on the write attempt due to serialization of the
                        // transactions.
                        allow(new int[] { 0 });
                        waitFor(new int[] { 0 }, true);

                        // Check that the altered record cannot be read before it has been committed or that this second
                        // read attempt is forced to happen before transaction 1 makes its write by the isolation
                        // mechanism blocking transaction 1.
                        if (canRead(testRecord1Altered, testIndex))
                        {
                            addErrorMessage("Can read uncommitted record 1 changes in transaction 2.\n");
                        }

                        // Allow transaction 1 to run to completion.
                        allow(new int[] { 0 });

                        // Commit this transaction.
                        testIndex.commit();
                        committedOk = true;
                    }
                    finally
                    {
                        if (!committedOk)
                        {
                            testIndex.rollback();
                        }

                        NDC.pop();
                    }
                }
            };

        // Run both the test threads at the same time.
        ThreadTestCoordinator tt = new ThreadTestCoordinator(2);
        tt.addTestThread(thread1, 0);
        tt.addTestThread(thread2, 1);
        tt.setDeadlockTimeout(DEADLOCK_TIMEOUT);
        tt.run();
        errorMessage += tt.joinAndRetrieveMessages();

        for (Exception e : tt.getExceptions())
        {
            errorMessage += e.getMessage();
            log.warn("There was an exception: ", e);
        }

        // Check that there were no errors during the test and display them if there were.
        assertTrue("Isolation level is: " + testIndex.getTransationalMode() + "\n" + errorMessage,
            "".equals(errorMessage));
    }

    /** Check that transactions do not perform non-repeatable reads of each others committed writes. */
    public void testNoNonRepeatableReads() throws Exception
    {
        String errorMessage = "";

        // Create two local transaction ids to use in isolation tests between two transactions.
        final IndexTxId txId1 = IndexTxManager.createTxId();
        final IndexTxId txId2 = IndexTxManager.createTxId();

        // Grab some sequence numbers to make the records unique to this test. This allows the test to be run
        // multiple times concurrently without interfering with itself.
        long u1 = sequence.getAndIncrement();

        // Create some sample records to insert into the index.
        final TestRecord testRecord1 =
            new TestRecord(1L + (1000000L * u1), "The quick brown fox jumped over the lazy dogs.", "TestRecord1-" + u1,
                1.0f);

        final TestRecord testRecord1Altered =
            new TestRecord(1L + (1000000L * u1), "The quick brown fox jumped over the lazy dogs.",
                "TestRecord1Altered-" + u1, 1.0f);

        // Add a mapping to the index for the sample record.
        IndexMapping mapping = new IndexMapping(new String[] { "text", "title" }, "rating");
        testIndexSetup.addMapping(TestRecord.class, TestRecord.TestRecordSummary.class, mapping);

        // Index the record in transaction 1 and commit it.
        t(txId1);
        testIndex.add(testRecord1.getKey(), testRecord1, testRecord1.getSummaryRecord());
        testIndex.commit();

        // Create the first test transaction in its own thread.
        TestRunnable thread1 =
            new TestRunnable()
            {
                public void runWithExceptions() throws Exception
                {
                    NDC.push(getName());

                    // Set this thread to be associated with transaction 1. This remains for the life of the thread.
                    t(txId1);

                    boolean committedOk = false;

                    try
                    {
                        // Allow the second transaction to begin before making changes.
                        waitFor(new int[] { 1 }, false);

                        // Make an alteration to record 1. Transaction isolation may cause blocking at this point if
                        // transaction 2 has a blocking read lock on record 1.
                        testIndex.update(testRecord1.getKey(), testRecord1Altered,
                            testRecord1Altered.getSummaryRecord());

                        // Commit this transaction.
                        testIndex.commit();
                        committedOk = true;

                        // Allow the second transaction to attempt to make a dirty read of the change and wait for it to get
                        // as far as doing this.
                        allow(new int[] { 1 });
                        waitFor(new int[] { 1 }, false);
                    }
                    finally
                    {
                        if (!committedOk)
                        {
                            testIndex.rollback();
                        }

                        NDC.pop();
                    }
                }
            };

        // Create the second transaction in its own thread.
        TestRunnable thread2 =
            new TestRunnable()
            {
                public void runWithExceptions() throws Exception
                {
                    NDC.push(getName());

                    // Set this thread to be associated with transaction 2. This remains for the life of the thread.
                    t(txId2);

                    boolean committedOk = false;

                    try
                    {
                        // Read record1.
                        if (!canRead(testRecord1, testIndex))
                        {
                            addErrorMessage("Failed to read record 1 in transaction 2.\n");
                        }

                        // Allow transaction 1 to make some changes to the record and wait until it has completed
                        // those changes, or else is blocked on the write attempt due to serialization of the
                        // transactions.
                        allow(new int[] { 0 });
                        waitFor(new int[] { 0 }, true);

                        // Check that the altered record cannot be read, even after it has been committed.
                        if (canRead(testRecord1Altered, testIndex))
                        {
                            addErrorMessage("Can read committed record 1 changes in transaction 2.\n");
                        }

                        // Allow transaction 1 to run to completion.
                        allow(new int[] { 0 });

                        // Commit this transaction.
                        testIndex.commit();
                        committedOk = true;
                    }
                    finally
                    {
                        if (!committedOk)
                        {
                            testIndex.rollback();
                        }

                        NDC.pop();
                    }
                }
            };

        // Run both the test threads at the same time.
        ThreadTestCoordinator tt = new ThreadTestCoordinator(2);
        tt.addTestThread(thread1, 0);
        tt.addTestThread(thread2, 1);
        tt.setDeadlockTimeout(DEADLOCK_TIMEOUT);
        tt.run();
        errorMessage += tt.joinAndRetrieveMessages();

        for (Exception e : tt.getExceptions())
        {
            errorMessage += e.getMessage();
        }

        // Check that there were no errors during the test and display them if there were.
        assertTrue("Isolation level is: " + testIndex.getTransationalMode() + "\n" + errorMessage,
            "".equals(errorMessage));
    }

    /** Check that transactions can perform repeatable reads even when another has committed writes to the data. */
    public void testRepeatableReads() throws Exception
    {
        String errorMessage = "";

        // Create two local transaction ids to use in isolation tests between two transactions.
        final IndexTxId txId1 = IndexTxManager.createTxId();
        final IndexTxId txId2 = IndexTxManager.createTxId();

        // Grab some sequence numbers to make the records unique to this test. This allows the test to be run
        // multiple times concurrently without interfering with itself.
        long u1 = sequence.getAndIncrement();

        // Create some sample records to insert into the index.
        final TestRecord testRecord1 =
            new TestRecord(1L + (1000000L * u1), "The quick brown fox jumped over the lazy dogs.", "TestRecord1-" + u1,
                1.0f);

        final TestRecord testRecord1Altered =
            new TestRecord(1L + (1000000L * u1), "The quick brown fox jumped over the lazy dogs.",
                "TestRecord1Altered-" + u1, 1.0f);

        // Add a mapping to the index for the sample record.
        IndexMapping mapping = new IndexMapping(new String[] { "text", "title" }, "rating");
        testIndexSetup.addMapping(TestRecord.class, TestRecord.TestRecordSummary.class, mapping);

        // Index the record in transaction 1 and commit it.
        t(txId1);
        testIndex.add(testRecord1.getKey(), testRecord1, testRecord1.getSummaryRecord());
        testIndex.commit();

        // Create the first test transaction in its own thread.
        TestRunnable thread1 =
            new TestRunnable()
            {
                public void runWithExceptions() throws Exception
                {
                    NDC.push(getName());

                    // Set this thread to be associated with transaction 1. This remains for the life of the thread.
                    t(txId1);

                    boolean committedOk = false;

                    try
                    {
                        // Allow the second transaction to begin before making changes.
                        waitFor(new int[] { 1 }, false);

                        // Make an alteration to record 1. Transaction isolation may cause blocking at this point if
                        // transaction 2 has a blocking read lock on record 1.
                        testIndex.update(testRecord1.getKey(), testRecord1Altered,
                            testRecord1Altered.getSummaryRecord());

                        // Commit this transaction.
                        testIndex.commit();
                        committedOk = true;

                        // Allow the second transaction to attempt to make a dirty read of the change and wait for it to get
                        // as far as doing this.
                        allow(new int[] { 1 });
                        waitFor(new int[] { 1 }, false);
                    }
                    finally
                    {
                        if (!committedOk)
                        {
                            testIndex.rollback();
                        }

                        NDC.pop();
                    }
                }
            };

        // Create the second transaction in its own thread.
        TestRunnable thread2 =
            new TestRunnable()
            {
                public void runWithExceptions() throws Exception
                {
                    NDC.push(getName());

                    // Set this thread to be associated with transaction 2. This remains for the life of the thread.
                    t(txId2);

                    boolean committedOk = false;

                    try
                    {
                        // Read record1.
                        if (!canRead(testRecord1, testIndex))
                        {
                            addErrorMessage("Failed to read record 1 in transaction 2.\n");
                        }

                        // Allow transaction 1 to make some changes to the record and wait until it has completed
                        // those changes, or else is blocked on the write attempt due to serialization of the
                        // transactions.
                        allow(new int[] { 0 });
                        waitFor(new int[] { 0 }, true);

                        // Check that the altered record cannot be read before even after it has been committed.
                        if (!canRead(testRecord1, testIndex))
                        {
                            addErrorMessage("Cannot read original record 1 in transaction 2 after it was altered " +
                                "and committed in transaction 1.\n");
                        }

                        // Allow transaction 1 to run to completion.
                        allow(new int[] { 0 });

                        // Commit this transaction.
                        testIndex.commit();
                        committedOk = true;
                    }
                    finally
                    {
                        if (!committedOk)
                        {
                            testIndex.rollback();
                        }

                        NDC.pop();
                    }
                }
            };

        // Run both the test threads at the same time.
        ThreadTestCoordinator tt = new ThreadTestCoordinator(2);
        tt.addTestThread(thread1, 0);
        tt.addTestThread(thread2, 1);
        tt.setDeadlockTimeout(DEADLOCK_TIMEOUT);
        tt.run();
        errorMessage += tt.joinAndRetrieveMessages();

        for (Exception e : tt.getExceptions())
        {
            errorMessage += e.getMessage();
        }

        // Check that there were no errors during the test and display them if there were.
        assertTrue("Isolation level is: " + testIndex.getTransationalMode() + "\n" + errorMessage,
            "".equals(errorMessage));
    }

    /** Check that transactions cannot perform phantom creates. */
    public void testNoPhantomReads() throws Exception
    {
        String errorMessage = "";

        // Create two local transaction ids to use in isolation tests between two transactions.
        final IndexTxId txId1 = IndexTxManager.createTxId();
        final IndexTxId txId2 = IndexTxManager.createTxId();

        // Grab some sequence numbers to make the records unique to this test. This allows the test to be run
        // multiple times concurrently without interfering with itself.
        long u1 = sequence.getAndIncrement();

        // Create some sample records to insert into the index.
        final TestRecord testRecord1 =
            new TestRecord(1L + (1000000L * u1), "The quick brown fox jumped over the lazy dogs.", "TestRecord1-" + u1,
                1.0f);

        // Add a mapping to the index for the sample record.
        IndexMapping mapping = new IndexMapping(new String[] { "text", "title" }, "rating");
        testIndexSetup.addMapping(TestRecord.class, TestRecord.TestRecordSummary.class, mapping);

        // Create the first test transaction in its own thread.
        TestRunnable thread1 =
            new TestRunnable()
            {
                public void runWithExceptions() throws Exception
                {
                    NDC.push(getName());

                    // Set this thread to be associated with transaction 1. This remains for the life of the thread.
                    t(txId1);

                    boolean committedOk = false;

                    try
                    {
                        // Allow the second transaction to begin before making changes.
                        waitFor(new int[] { 1 }, false);

                        // Create the record in transaction 1 and commit it.
                        testIndex.add(testRecord1.getKey(), testRecord1, testRecord1.getSummaryRecord());
                        testIndex.commit();
                        committedOk = true;

                        // Allow the second transaction to attempt to make a phantom read of the change and wait for it
                        // to get as far as doing this.
                        allow(new int[] { 1 });
                        waitFor(new int[] { 1 }, false);

                    }
                    finally
                    {
                        if (!committedOk)
                        {
                            testIndex.rollback();
                        }

                        NDC.pop();
                    }
                }
            };

        // Create the second transaction in its own thread.
        TestRunnable thread2 =
            new TestRunnable()
            {
                public void runWithExceptions() throws Exception
                {
                    NDC.push(getName());

                    // Set this thread to be associated with transaction 2. This remains for the life of the thread.
                    t(txId2);

                    boolean committedOk = false;

                    try
                    {
                        // Read record1.
                        if (canRead(testRecord1, testIndex))
                        {
                            addErrorMessage(
                                "Read record 1 in transaction 2 but transaction 1 has not created it yet.\n");
                        }

                        // Allow transaction 1 to create the record and wait until it has completed that.
                        allow(new int[] { 0 });
                        waitFor(new int[] { 0 }, false);

                        // Check that the new record cannot be read because it was created after this transaction started.
                        if (canRead(testRecord1, testIndex))
                        {
                            addErrorMessage("Can read new committed record 1 as a phantom in transaction 2.\n");
                        }

                        // Allow transaction 1 to run to completion.
                        allow(new int[] { 0 });

                        // Commit this transaction.
                        testIndex.commit();
                        committedOk = true;
                    }
                    finally
                    {
                        if (!committedOk)
                        {
                            testIndex.rollback();
                        }

                        NDC.pop();
                    }
                }
            };

        // Run both the test threads at the same time.
        ThreadTestCoordinator tt = new ThreadTestCoordinator(2);
        tt.addTestThread(thread1, 0);
        tt.addTestThread(thread2, 1);
        tt.setDeadlockTimeout(DEADLOCK_TIMEOUT);
        tt.run();
        errorMessage += tt.joinAndRetrieveMessages();

        for (Exception e : tt.getExceptions())
        {
            errorMessage += e.getMessage();
        }

        // Check that there were no errors during the test and display them if there were.
        assertTrue("Isolation level is: " + testIndex.getTransationalMode() + "\n" + errorMessage,
            "".equals(errorMessage));
    }

    /** Check that committed transactions are visible to later transactions. */
    public void testCommitsSaved() throws Exception
    {
        String errorMessage = "";

        // Create two local transaction ids to use in isolation tests between two transactions.
        final IndexTxId txId1 = IndexTxManager.createTxId();
        final IndexTxId txId2 = IndexTxManager.createTxId();

        // Grab some sequence numbers to make the records unique to this test. This allows the test to be run
        // multiple times concurrently without interfering with itself.
        long u1 = sequence.getAndIncrement();

        // Create some sample records to insert into the index.
        TestRecord testRecord1 =
            new TestRecord(1L + (1000000L * u1), "The quick brown fox jumped over the lazy dogs.", "TestRecord1-" + u1,
                1.0f);

        // Add a mapping to the index for the sample record.
        IndexMapping mapping = new IndexMapping(new String[] { "text", "title" }, "rating");
        testIndexSetup.addMapping(TestRecord.class, TestRecord.TestRecordSummary.class, mapping);

        // Index records in transaction 1 and commit them.
        t(txId1);
        testIndex.add(testRecord1.getKey(), testRecord1, testRecord1.getSummaryRecord());
        testIndex.commit();

        // In transaction 2 check that the records can be read.
        t(txId2);

        if (!canRead(testRecord1, testIndex))
        {
            errorMessage += "Cannot read committed record 1 in transaction 2.\n";
        }

        testIndex.commit();

        // Check that there were no errors during the test and display them if there were.
        assertTrue("Isolation level is: " + testIndex.getTransationalMode() + "\n" + errorMessage,
            "".equals(errorMessage));
    }

    /** Check that rolled-back transactions are not visible to later transactions. */
    public void testRollbacksDropped() throws Exception
    {
        String errorMessage = "";

        // Create two local transaction ids to use in isolation tests between two transactions.
        final IndexTxId txId1 = IndexTxManager.createTxId();
        final IndexTxId txId2 = IndexTxManager.createTxId();

        // Grab some sequence numbers to make the records unique to this test. This allows the test to be run
        // multiple times concurrently without interfering with itself.
        long u1 = sequence.getAndIncrement();

        // Create some sample records to insert into the index.
        TestRecord testRecord1 =
            new TestRecord(1L + (1000000L * u1), "The quick brown fox jumped over the lazy dogs.", "TestRecord1-" + u1,
                1.0f);

        // Add a mapping to the index for the sample record.
        IndexMapping mapping = new IndexMapping(new String[] { "text", "title" }, "rating");
        testIndexSetup.addMapping(TestRecord.class, TestRecord.TestRecordSummary.class, mapping);

        // Index records in transaction 1 but roll them back.
        t(txId1);
        testIndex.add(testRecord1.getKey(), testRecord1, testRecord1.getSummaryRecord());
        testIndex.rollback();

        // In transaction 2 check that the records cannot be read.
        t(txId2);

        if (canRead(testRecord1, testIndex))
        {
            errorMessage += "Can read rolled back record 1 in transaction 2.\n";
        }

        testIndex.commit();

        // Check that there were no errors during the test and display them if there were.
        assertTrue("Isolation level is: " + testIndex.getTransationalMode() + "\n" + errorMessage,
            "".equals(errorMessage));
    }

    /**
     * @throws Exception Any exceptions fall through this method and fail the test.
     */
    protected void setUp() throws Exception
    {
        // Create a transaction to clear the index in.
        IndexTxId txId1 = IndexTxManager.createTxId();

        // Make sure the index is empty.
        /*
        t(txId1);
        testIndex.clear();
        testIndex.commit();
        */

        NDC.push(getName());
    }

    /**
     * @throws Exception Any exceptions fall through this method and fail the test.
     */
    protected void tearDown() throws Exception
    {
        NDC.pop();
    }

    /** Helper method that associates transaction 1 with the current thread. */
    /*private void t(txId1)
    {
        IndexTxManager.assignTxIdToThread(txId1);
    } */

    /** Helper method that associates transaction 2 with the current thread. */
    /*private void t(txId2)
    {
        IndexTxManager.assignTxIdToThread(txId2);
    } */

    /**
     * Helper method that associates specified transaction id with the current thread.
     *
     * @param txId The transaction id.
     */
    private void t(IndexTxId txId)
    {
        IndexTxManager.assignTxIdToThread(txId);
    }

    /**
     * Helper method that verifies whether or not the specified record can be found in the specified index.
     * It is assumed that the test records title has been indexed and is a unique term to search for the record on
     * in that index. The test record should be the first and only result returned by the search.
     *
     * @param tr The TestRecord to search for.
     * @param i  The index to search in.
     *
     * @return <tt>true</tt> if the test record is the first and only result returned by the search on its title.
     */
    private boolean canRead(TestRecord tr, Index i)
    {
        // Search against the records unique title.
        Map<Long, TestRecord.TestRecordSummary> results = i.search(tr.getTitle());

        if (results.size() != 1)
        {
            return false;
        }

        if (tr.getTitle().equals(results.values().iterator().next().title))
        {
            return true;
        }

        return false;
    }
}
