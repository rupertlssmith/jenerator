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
package com.thesett.catalogue.core;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.log4j.Logger;

import com.thesett.common.config.ConfigBeanContext;
import com.thesett.common.config.ConfigException;
import com.thesett.common.config.Configurator;

/**
 * ConfiguratorTestBase provides a basis for writing unit tests that require a configuration to be loaded through the
 * {@link Configurator} before they can run their test cases. The loaded and run configurators for a given configuration
 * file are held in a static field, that acts as a singleton configuration. However, the configuration method provided
 * by this base class can be set to re-load and re-run the configuration on every single test, or just once the very
 * first time it is called. The advantage of having static singleton configurations accross all tests that need to use
 * the configurator, is that lengthy configurations can be run just once over a suite of tests.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Maintain a static map of configurations per file name. <td> {@link Configurator}
 * <tr><td> Allow configurations to be run on every test, or just once. <td> {@link Configurator}
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class ConfiguratorTestBase extends TestCase
{
    /** Used for debugging purposes. */
    private static final Logger log = Logger.getLogger(ConfiguratorTestBase.class);

    /** Holds a static map of configurations by file name. */
    private static Map<String, Configurator> configurations = new HashMap<String, Configurator>();

    /**
     * Creates a test with the specified name.
     *
     * @param name The name of the test.
     */
    public ConfiguratorTestBase(String name)
    {
        super(name);
    }

    /**
     * Runs the configurator on the specified resource, retaining the results in a static map. If the force flag is set,
     * the creation, loading and running of the configurator is repeated, even if a prior configuration already exists,
     * otherwise a prior configuration for a given resource name, will result in that configuration being returned.
     *
     * @param  resourceName The resource on the class path to load the configuration from.
     * @param  force        <tt>true</tt> to force reconfiguration every time.
     *
     * @return The configurator for the specified resource.
     */
    protected static ConfigBeanContext configure(String resourceName, boolean force)
    {
        try
        {
            Configurator configurator = configurations.get(resourceName);

            if ((configurator == null) || force)
            {
                configurator = new Configurator(resourceName);
                configurator.loadConfigBeans();

                configurator.configureAll();

                configurations.put(resourceName, configurator);
            }

            return configurator;
        }
        catch (ConfigException e)
        {
            log.warn("There was a configuration exception whilst loading the test setup.", e);
            throw new IllegalStateException(e);
        }
    }
}
