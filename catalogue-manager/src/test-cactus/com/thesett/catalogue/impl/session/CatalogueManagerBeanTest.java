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
package com.thesett.catalogue.impl.session;

import org.apache.cactus.ServletTestCase;
import org.apache.log4j.Logger;
import org.apache.log4j.NDC;

import com.thesett.catalogue.impl.interfaces.CatalogueManagerRemote;
import com.thesett.catalogue.interfaces.Catalogue;
import com.thesett.catalogue.interfaces.CatalogueManagerService;
import com.thesett.common.locator.ServiceLocatorConfigBean;

/**
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class CatalogueManagerBeanTest extends ServletTestCase
{
    /** Used for logging. */
    private static final Logger log = Logger.getLogger(CatalogueManagerBeanTest.class);

    /** The test name. */
    private String testName;

    public CatalogueManagerBeanTest(String name)
    {
        super(name);

        // Keep the test name.
        this.testName = name;
    }

    public void setUp() throws Exception
    {
        log.info("public void setUp(): called");

        // Push a client identifier onto the Nested Diagnostic Context so that Log4J will be able to identify all
        // logging output for this tests.
        NDC.push(testName);

        // Ensure that the service locator is configured by forcing its reconfiguration. The 'local' deployment context
        // is used as the session bean under test is located on the same application server as this test is being run
        // and they are deployed together in the same .ear file.
        ServiceLocatorConfigBean slConfigBean = new ServiceLocatorConfigBean();
        slConfigBean.setContextName("local");
        slConfigBean.doConfigure(true, null);
    }

    public void tearDown() throws Exception
    {
        // Clear the nested diagnostic context for this test.
        NDC.pop();
    }

    public void testXx() throws Exception
    {
        // Obtain a reference to the catalogue manager service through the service locator.
        CatalogueManagerService catalogueService = CatalogueManagerRemote.getInstance();

        // Get the catalogue.
        Catalogue catalogue = catalogueService.getCatalogue();
    }
}
