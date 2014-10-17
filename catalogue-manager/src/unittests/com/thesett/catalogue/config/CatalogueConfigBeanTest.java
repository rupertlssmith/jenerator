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
package com.thesett.catalogue.config;

import junit.framework.TestCase;

import org.apache.log4j.NDC;

/**
 * Tests the {@link CatalogueConfigBeanImpl} works correctly in dev and prod mode.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Check that catalogue configuration succeeds in development mode.
 * <tr><td> Check that catalogue configuration succeeds in production mode.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class CatalogueConfigBeanTest extends TestCase
{
    /** Holds the resource name of the dev mode test configuration. */
    private static final String TEST_CONFIG_DEV = "testconfig.xml";

    /**
     * Creates a test with the specified name.
     *
     * @param name The name of the test to create.
     */
    public CatalogueConfigBeanTest(String name)
    {
        super(name);
    }

    public void testXx()
    {
    }

    /** Check that catalogue configuration succeeds in development mode. */
    /*
    public void testDevModeConfig() throws Exception
    {
        // Create a configurator for the test setup and run it.
        Configurator configurator = new Configurator(TEST_CONFIG_DEV);
        configurator.loadConfigBeans();
        configurator.configureAll();
    }
    */

    /** Check that catalogue configuration succeeds in production mode. */
    /*
    public void testProdModeConfig() throws Exception
    {
        // Create a configurator for the test setup and run it. This is a dev mode setup.
        Configurator configurator = new Configurator(TEST_CONFIG_DEV);
        configurator.loadConfigBeans();
        configurator.configureAll();

        // Get a reference to the mode bean and set it to production mode.
        ModeConfigBean modeBean =
            (ModeConfigBean) configurator.getLoadedBean("com.thesett.catalogue.config.ModeConfigBean");
        modeBean.setDevMode(false);

        // Re-run the configuration in production mode.
        configurator.reConfigureAll();
    }
    */

    protected void setUp() throws Exception
    {
        // Push a client identifier onto the Nested Diagnostic Context so that Log4J will be able to identify all
        // logging output for these tests.
        NDC.push(getName());
    }

    protected void tearDown() throws Exception
    {
        // Clear the nested diagnostic context for this test.
        NDC.pop();
    }
}
