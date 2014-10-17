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
package com.thesett.catalogue.impl.standalone;

import org.apache.log4j.Logger;
import org.apache.log4j.NDC;
import org.hibernate.Session;

import com.thesett.catalogue.hibernate.HibernateUtil;
import com.thesett.common.config.ConfigBeanContext;

/**
 * Tests the functioning of the catalogue manager, create, retrieve, update, delete and a variety of queries. This test
 * calls the catalogue manager using explicitly managed transactions.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Check that the catalogue manager service returns the catalogue ok.
 * <tr><td> Check creation of dimension elements works ok.
 * <tr><td> Check retrieval of dimension elements works ok.
 * <tr><td> Check saving changes to dimension elements works ok.
 * <tr><td> Check deleting dimension elements works ok.
 * <tr><td> Check querying a single dimension by a complete hierarchy works ok.
 * <tr><td> Check querying a single dimension by a multiple complete hierarchies works ok.
 * <tr><td> Check querying a single dimension by an incomplete hierarchy works ok.
 * <tr><td> Check querying a dimension by element name works ok.
 * <tr><td> Check that re-indexing an entire catalogue works ok.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class CatalogueManagerServiceImplTest extends ConfiguratorTestBase implements CatalogueManagerServiceTestsUtils
{
    /** Used for logging. */
    private static final Logger log = Logger.getLogger(CatalogueManagerServiceImplTest.class);

    /** Holds the resource name of the test configuration. */
    private static final String TEST_CONFIG = "testconfig.xml";

    /** Delegate that implements the actual tests in a deployment neutral manner. */
    CatalogueManagerServiceTests tests;

    public CatalogueManagerServiceImplTest(String name)
    {
        super(name);

        ConfigBeanContext configBeanContext = configure(TEST_CONFIG, false);

        // Set up the deployment neutral delegate that implements the test cases.
        tests = new CatalogueManagerServiceTests(this, configBeanContext);
    }

    /** {@inheritDoc} */
    public CatalogueManagerService getCatalogueManager()
    {
        return new CatalogueManagerServiceImpl();
    }

    /** {@inheritDoc} */
    public Session getDirectHibernateSession()
    {
        return HibernateUtil.getCurrentSession();
    }

    /** {@inheritDoc} */
    public void closeDirectHibernateSession()
    {
        HibernateUtil.closeSession();
    }

    /** {@inheritDoc} */
    public void beginTransaction()
    {
        HibernateUtil.beginTransaction();
    }

    /** {@inheritDoc} */
    public void commitTransaction()
    {
        HibernateUtil.commitTransaction();
    }

    /** Check that the catalogue manager service returns the catalogue ok. */
    public void testGetCatalogueOk() throws Exception
    {
        tests.testGetCatalogueOk();
    }

    /** Check creation of dimension elements works ok. */
    public void testCreateDimensionElementOk() throws Exception
    {
        tests.testCreateDimensionElementOk();
    }

    /** Check retrieval of dimension elements works ok. */
    public void testRetrieveDimensionElementOk() throws Exception
    {
        tests.testRetrieveDimensionElementOk();
    }

    /** Check saving changes to dimension elements works ok. */
    public void testUpdateDimensionElementOk() throws Exception
    {
        tests.testUpdateDimensionElementOk();
    }

    /** Check deleting dimension elements works ok. */
    public void testDeleteDimensionElementOk() throws Exception
    {
        tests.testDeleteDimensionElementOk();
    }

    /** Check querying a single dimension by a complete hierarchy works ok. */
    public void testQueryDimensionByHierarchyOk() throws Exception
    {
        tests.testQueryDimensionByHierarchyOk();
    }

    /** Check querying a single dimension by a multiple complete hierarchies works ok. */
    /*public void testQueryDimensionByMultipleHierarchyOk() throws Exception
    {
        tests.testQueryDimensionByMultipleHierarchyOk();
    }*/

    /** Check querying a single dimension by an incomplete hierarchy works ok. */
    public void testQueryDimensionByIncompleteHierarchyOk() throws Exception
    {
        tests.testQueryDimensionByIncompleteHierarchyOk();
    }

    /** Check querying a dimension by element name works ok. */
    public void testQueryDimensionByNameOk() throws Exception
    {
        tests.testQueryDimensionByNameOk();
    }

    /** Check querying a dimension by external id works ok. */
    public void testQueryDimensionByExternalId() throws Exception
    {
        tests.testQueryDimensionByExternalId();
    }

    /** Check that re-indexing an entire catalogue works ok. */
    /*public void testRebuildIndexesOk() throws Exception
    {
        tests.testRebuildIndexesOk();
    }*/

    protected void setUp() throws Exception
    {
        // Push a client identifier onto the Nested Diagnostic Context so that Log4J will be able to identify all
        // logging output for this tests.
        NDC.push(getName());

        tests.setUp();
    }

    protected void tearDown() throws Exception
    {
        try
        {
            tests.tearDown();
        }
        finally
        {
            // Remove the client identifier for the test.
            NDC.pop();
        }
    }
}
