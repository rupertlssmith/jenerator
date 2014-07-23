/* Copyright Rupert Smith, 2005 to 2008, all rights reserved. */
package com.thesett.index.prototype;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.log4j.Logger;
import org.apache.log4j.NDC;

import com.thesett.index.TransactionalIndex;
import com.thesett.index.TransactionalIndexPerfTestBase;

/**
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class ProtoIndexTestStress extends TestCase
{
    /** Used for logging. */
    private static final Logger log = Logger.getLogger(ProtoIndexTestStress.class);

    public ProtoIndexTestStress(String testName)
    {
        super(testName);
    }

    /**
     * Compile all the tests for the default test implementation of an index into a test suite plus any tests defined
     * in this test class.
     */
    public static Test suite()
    {
        // Build a new test suite
        TestSuite suite = new TestSuite("ProtoIndex Stress Tests");

        // Run performance tests in read committed mode.
        ProtoIndex readCommittedIndex = new ProtoIndex();
        readCommittedIndex.setTransactionalMode(TransactionalIndex.IsolationLevel.ReadCommitted);
        suite.addTest(new TransactionalIndexPerfTestBase("testIndexUnderLoad", readCommittedIndex, readCommittedIndex));

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
