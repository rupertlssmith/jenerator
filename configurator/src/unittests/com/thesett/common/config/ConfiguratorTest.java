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
package com.thesett.common.config;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.log4j.Logger;
import org.apache.log4j.NDC;

/**
 * Checks that the {@link Configurator} correctly creates and configures config beans.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Loading an invalid config beans definition causes a config exception.
 * <tr><td> Loading a valid config beans definition succeeds.
 * <tr><td> Setting non-existant properties on a config beans fails upon configuration.
 * <tr><td> Setting properties on a config bean that cannot be converted to an appropriate type fails upon configuration.
 * <tr><td> Setting valid properties on a config bean succeeds.
 * <tr><td> Configuring an already configured bean does not cause its configuration to be forced.
 * <tr><td> Configuring an already configured bean causes its configuration to be forced when the force flag is set.
 * <tr><td> Configuring a bean succeeds.
 * <tr><td> Configuring a bean fails with a config exception when the underlying beans config fails.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class ConfiguratorTest extends TestCase
{
    /** Used for logging. */
    private static final Logger log = Logger.getLogger(ConfiguratorTest.class);

    /** Holds the name and path of the resource (on the classpath) from which to load the test configuration. */
    public static final String TEST_CONFIG = "com/thesett/common/config/test_config.xml";

    /** Holds the name and path of the resource (on the classpath) from which to load the invalid test configuration. */
    public static final String INVALID_TEST_CONFIG = "com/thesett/common/config/invalid_test_config.xml";

    /**
     * Holds the name and path of the resource (on the classpath) from which to load the test configuration with extra
     * fields that the config beans do not have.
     */
    public static final String EXTRAFIELDS_TEST_CONFIG = "com/thesett/common/config/extrafields_test_config.xml";

    /**
     * Holds the name and path of the resource (on the classpath) from which to load the test configuration with fields
     * that do not match the types of the fields of he config bean.
     */
    public static final String BADTYPE_TEST_CONFIG = "com/thesett/common/config/badtype_test_config.xml";

    /**
     * Holds the name and path of the resource (on the classpath) from which to load the test configuration with the
     * force flag set.
     */
    public static final String FORCE_TEST_CONFIG = "com/thesett/common/config/force_test_config.xml";

    public ConfiguratorTest(String name)
    {
        super(name);
    }

    /** Compile all the tests for the this test class into a test suite. */
    public static Test suite()
    {
        // Build a new test suite
        TestSuite suite = new TestSuite("Configurator Tests");

        // Add all the tests defined in this class (using the default constructor)
        suite.addTestSuite(ConfiguratorTest.class);

        return suite;
    }

    public void setUp() throws Exception
    {
        NDC.push(getName());
    }

    public void tearDown() throws Exception
    {
        // Restore the state of the test config bean to not configured, to not throw exceptions and to false for the
        // value of the most recent force flag.
        TestConfigBean.mostRecentForceFlag = false;
        TestConfigBean.throwConfigExceptions = false;
        TestConfigBean.isConfigured = false;

        NDC.pop();
    }

    public void testXx()
    {
    }

    /** Loading an invalid config beans definition causes a config exception. */
    /*
    public void testErrorOnInvalidConfigBeansDef() throws Exception
    {
        // Create a configurator based on an invalid config beans def.
        Configurator configurator = new Configurator(INVALID_TEST_CONFIG);

        // Try to load the beans.
        boolean testPassed = false;

        try
        {
            configurator.loadConfigBeans();
        }
        // Check that an exception is raised.
        catch (ConfigException e)
        {
            if (e.getMessage().startsWith("The config beans setup file, " + INVALID_TEST_CONFIG +
                                          ", contains invalid xml."))
            {
                testPassed = true;
            }
        }

        assertTrue("Appropriate ConfigException should be thrown when loading an invalid config beans def.", testPassed);
    }
    */

    /** Loading a valid config beans definition succeeds. */
    public void testValidConfigBeansDefLoadsOk() throws Exception
    {
        // Create a configurator based on an a valid config beans def.
        Configurator configurator = new Configurator(TEST_CONFIG);

        // Try to load the beans.
        configurator.loadConfigBeans();
    }

    /** Setting non-existant properties on a config beans fails upon configuration. */
    /*
    public void testErrorOnNonExistantProperties() throws Exception
    {
        // Create a configurator based on an a config beans def with extra fields.
        Configurator configurator = new Configurator(EXTRAFIELDS_TEST_CONFIG);

        // Try to load the beans.
        boolean testPassed = false;

        try
        {
            configurator.loadConfigBeans();
        }
        // Check that an exception is raised.
        catch (ConfigException e)
        {
            if (e.getMessage().startsWith("The config beans setup file, " + EXTRAFIELDS_TEST_CONFIG +
                                          ", contains fields that are not found in one of the bean classes."))
            {
                testPassed = true;
            }
        }

        assertTrue("Appropriate ConfigException should be thrown when loading a config beans def with fields that do " +
                   "not match the field names on the config beans being loaded.", testPassed);
    }
    */

    /** Setting properties on a config bean that cannot be converted to an appropriate type fails upon configuration. */
    public void testErrorOnBadlyTypedProperties() throws Exception
    {
        // Create a configurator based on an a config beans def with types not maching the config beans.
        Configurator configurator = new Configurator(BADTYPE_TEST_CONFIG);

        // Try to load the beans.
        boolean testPassed = false;

        try
        {
            configurator.loadConfigBeans();
        }

        // Check that an exception is raised.
        catch (ConfigException e)
        {
            log.debug("ConfigException", e);
            testPassed = true;
        }

        assertTrue("ConfigException should be thrown when loading a config beans def with field types that do not " +
            "match those on the config beans being loaded.", testPassed);
    }

    /** Setting valid properties on a config bean succeeds. */
    public void testSettingValidPropertiesSucceeds() throws Exception
    {
        // Create a configurator based on an a valid config beans def.
        Configurator configurator = new Configurator(TEST_CONFIG);

        // Try to load the beans.
        configurator.loadConfigBeans();

        // Check all fields have been set to the right values.
        TestConfigBean testBean = TestConfigBean.mostRecentInstance;

        assertEquals(1, testBean.testInt);
        assertEquals(1, testBean.testByte);
        assertEquals('a', testBean.testChar);
        assertEquals(128000000000L, testBean.testLong);
        assertEquals(true, testBean.testBoolean);
        assertEquals(1.23456789f, testBean.testFloat);
        assertEquals(1.23456789123456789123456789d, testBean.testDouble);
        assertEquals("Test", testBean.testString);
    }

    /** Configuring an already configured bean does not cause its configuration to be forced. */
    public void testAlreadyConfiguredBeanNotForced() throws Exception
    {
        // Set the configuration state of the test been to configured.
        TestConfigBean.isConfigured = true;

        // Create a configurator based on an a valid config beans def.
        Configurator configurator = new Configurator(TEST_CONFIG);

        // Load and configure the beans.
        configurator.loadConfigBeans();
        configurator.configureAll();

        // Check that the force flag was not set on the last call to configure the bean.
        assertFalse("The force flag should have been false when not set in the config bean def.",
            TestConfigBean.mostRecentForceFlag);
    }

    /** Configuring an already configured bean causes its configuration to be forced when the force flag is set. */
    /*
    public void testAlreadyConfiguredBeanForcedWhenForceFlagUsed() throws Exception
    {
        // Set the configuration state of the test been to configured.
        TestConfigBean.isConfigured = true;

        // Create a configurator based on an a valid config beans def with the force flag set for the test bean.
        Configurator configurator = new Configurator(FORCE_TEST_CONFIG);

        // Load and configure the beans.
        configurator.loadConfigBeans();
        configurator.configureAll();

        // Check that the force flag was not set on the last call to configure the bean.
        assertTrue("The force flag should have been true when set in the config bean def.",
                   TestConfigBean.mostRecentForceFlag);
    }
    */

    /** Configuring a bean succeeds. */
    public void testBeanConfigSucceeds() throws Exception
    {
        // Create a configurator based on an a valid config beans def.
        Configurator configurator = new Configurator(TEST_CONFIG);

        // Try to load the beans.
        configurator.loadConfigBeans();

        // Configure all the beans.
        configurator.configureAll();
    }

    /** Configuring a bean fails with a config exception when the underlying beans config fails. */
    public void testErrorOnUnderlyingConfigBeanFailure() throws Exception
    {
        // Create a configurator based on an a valid config beans def.
        Configurator configurator = new Configurator(TEST_CONFIG);

        // Try to load the beans.
        configurator.loadConfigBeans();

        // Tell the test config bean to throw exceptions on its configure method.
        TestConfigBean.throwConfigExceptions = true;

        // Configure all the beans.
        boolean testPassed = false;
        String message = null;

        try
        {
            configurator.configureAll();
        }

        // Check that an exception is raised.
        catch (ConfigException e)
        {
            testPassed = true;
            message = e.getMessage();
        }

        assertEquals("ConfigException should be thrown when configuring a beans that throws a config exception. " +
            "The expected exception message for the test config bean is \"TestConfigBean test Exception.\"",
            "TestConfigBean test Exception.", message);
    }
}
