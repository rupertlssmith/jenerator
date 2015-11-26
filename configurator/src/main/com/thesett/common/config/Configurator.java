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

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;

import com.thesett.common.config.setup.BeanSetup;
import com.thesett.common.config.setup.ConfigBeans;
import com.thesett.common.config.setup.JndiConfig;
import com.thesett.common.config.setup.Property;
import com.thesett.common.config.setup.SetUpProperty;
import com.thesett.common.reflect.BeanMemento;
import com.thesett.common.reflect.Memento;
import com.thesett.common.util.TypeConverter;

/**
 * The Configurator is responsible for reading a config beans set up definition file, creating the beans described in
 * the file and applying all start up properties to them and triggering the configuration of all the loaded beans.
 *
 * <p/>Configurator is typically used at application start up time to configure an application. This does not have to be
 * the case though, it can be used at any time.
 *
 * <p/>Once beans have been succesfully configured, they are stored in a cache against their class names. The
 * configurator can be queried, using the {@link #getConfiguredBean(String)} method to fetch them.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Create config beans from a setup file.
 * <tr><td> Trigger the configuration of all beans.
 * <tr><td> Supply configured beans by class name.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class Configurator implements Serializable, ConfigBeanContext
{
    /** Used for debuggin purposes. */
    private static final Logger log = Logger.getLogger(Configurator.class);

    /** Defines the root context under which to store configured beans in the JNDI context. */
    private static final String CONFIG_BEAN_ROOT = "java:configbeans";

    /** Defines the JNDI name under which the configurator itself is stored. */
    public static final String CONFIGURATOR_JNDI_NAME = CONFIG_BEAN_ROOT + "/configurator";

    /** Defines the default resource to use to load the config beans definition file from. */
    private static final String RESOURCE_NAME = "configbeans.xml";

    /** Holds a reference to the JNDI context to place configured beans under. */
    private static Context jndiContext = null;

    /** Holds resource name to use to load the config beans definition file from. */
    private String resourceName = RESOURCE_NAME;

    /** Holds the loaded beans that are awaiting configuration. */
    private Map<String, ConfigBean> loadedBeans = null;

    /** Creates a new configurator that loads the config beans definition from the default resource location. */
    public Configurator()
    {
    }

    /**
     * Creates a new configurator that loads the config beans definition from the specified location.
     *
     * @param resourceName The resource name to load the config beans definition file from.
     */
    public Configurator(String resourceName)
    {
        // Keep the resource name to load the configuration from.
        this.resourceName = resourceName;
    }

    /**
     * A static helper method to look up the configurator in the JNDI context.
     *
     * @return An instance of the configurator.
     */
    public static Configurator lookupConfigurator()
    {
        try
        {
            Configurator configurator = (Configurator) jndiContext.lookup(CONFIGURATOR_JNDI_NAME);

            return configurator;
        }
        catch (NamingException e)
        {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Creates the config beans by parsing the beans config definition file.
     *
     * @throws ConfigException If any of the underlying beans throws a connfig exception or there is a problem loading
     *                         or parsing the configuration file.
     */
    public void loadConfigBeans() throws ConfigException
    {
        // log.debug("public void loadConfigBeans(): called");

        // The loaded beans are kept in a hash array to preserve their loading order.
        // loadedBeans = new HashArray<String, ConfigBean>();
        loadedBeans = new LinkedHashMap<String, ConfigBean>();

        // Open the config definition resource for reading.
        InputStream resourceStream = this.getClass().getClassLoader().getResourceAsStream(resourceName);

        if (resourceStream == null)
        {
            throw new ConfigException("The configuration resource, " + resourceName +
                ", could not be found on the classpath.", null, null, null);
        }

        // Unmarshall the XML from the config definition into a set of beans describing its content.
        Reader configReader = new InputStreamReader(resourceStream);
        ConfigBeans configBeans;

        try
        {
            // Open the specified resource and unmarshal the template from it.
            JAXBContext jc = JAXBContext.newInstance("com.thesett.common.config.setup");

            Unmarshaller u = jc.createUnmarshaller();
            configBeans = (ConfigBeans) u.unmarshal(configReader);
        }
        catch (JAXBException e)
        {
            throw new ConfigException("The config beans setup file, " + resourceName + ", could not be unmarshalled.",
                e, null, null);
        }

        // Extract the JNDI configuration properties.
        Properties jndiProps = new Properties();

        JndiConfig jndiConfig = configBeans.getJndiConfig();

        for (Property prop : jndiConfig.getProperty())
        {
            jndiProps.setProperty(prop.getName(), prop.getValue());
        }

        // Take a reference to the JNDI context to store configured beans under that is specified by the JNDI properties.
        try
        {
            jndiContext = new InitialContext(jndiProps);
            jndiContext.createSubcontext(CONFIG_BEAN_ROOT);
        }
        catch (NameAlreadyBoundException e)
        {
            // This exception can safely be ignored. The name may already have been bound on a previous run of the
            // configurator.
            e = null;
            // log.debug("Got NameAlreadyBoundException whilst creating config beans root context, safely ignored.", e);
        }
        catch (NamingException e)
        {
            throw new ConfigException(
                "The JNDI context for configured beans could not be established, using properties: " + jndiProps, e,
                null, null);
        }

        // Loop through all the config beans definitions loading each one.
        for (BeanSetup nextBeanConfig : configBeans.getBeanSetup())
        {
            // Create an instance of the named bean class.
            String beanClassName = nextBeanConfig.getBeanClassName();
            // log.debug("beanClassName = " + beanClassName);

            Class theBeanClass = null;
            ConfigBean theBean = null;

            try
            {
                theBeanClass = Class.forName(beanClassName);
                theBean = (ConfigBean) theBeanClass.newInstance();
            }
            catch (ClassNotFoundException e)
            {
                throw new ConfigException("The config bean class, " + beanClassName + ", could not be found.", e, null,
                    null);
            }
            catch (InstantiationException e)
            {
                throw new ConfigException("The config bean class, " + beanClassName + ", could not be instantiated.", e,
                    null, null);
            }
            catch (IllegalAccessException e)
            {
                throw new ConfigException("The config bean class, " + beanClassName + ", cannot be legally accessed.",
                    e, null, null);
            }

            // Create a Memento for the bean to restore its stored state from. The config bean is not captured at this
            // point as don't want to read its properties now, only set them.
            Memento memento = new BeanMemento(theBean);

            // Loop through all the beans properties that are to be set.
            for (SetUpProperty nextPropertyConfig : nextBeanConfig.getSetUpProperty())
            {
                String propertyName = nextPropertyConfig.getName();
                String propertyValue = nextPropertyConfig.getValue();

                // Convert the set up value into a multi-type so that the bean memento can find th best matching
                // setter method to call with its value.
                TypeConverter.MultiTypeData multiValue = TypeConverter.getMultiTypeData(propertyValue);

                // Check the Memento to ensure that the bean has a property with that name.
                // Use the Memento to get the type of the beans property that is to be set.
                // Attempt to parse the data from the config definition into that type.

                // Set the bean property in the Memento.
                memento.put(theBean.getClass(), propertyName, multiValue);
            }

            // Trigger the Memento to load all the properties into the bean.
            try
            {
                memento.restore(theBean);
            }
            catch (NoSuchFieldException e)
            {
                throw new ConfigException("The config beans setup file, " + resourceName +
                    ", contains fields that are not found in one of the bean classes.", e, null, null);
            }

            // Add the bean to the list of beans to be configured.
            loadedBeans.put(beanClassName, theBean);
        }
    }

    /**
     * Triggers the configuration of all the config beans that were loaded by the most recent call to the
     * {@link #loadConfigBeans} method.
     *
     * @throws ConfigException If any of the underlying beans throws a config exception it is allowed to fall through.
     */
    public void configureAll() throws ConfigException
    {
        doConfigAll(false);
    }

    /**
     * Triggers the re-configuration of all the config beans that were loaded by the most recent call to the
     * {@link #loadConfigBeans} method.
     *
     * @throws ConfigException If any of the underlying beans throws a config exception it is allowed to fall through.
     */
    public void reConfigureAll() throws ConfigException
    {
        doConfigAll(true);
    }

    /** Removes all the configured beans from the JNDI context. */
    public void removeAll()
    {
        // log.debug("public void removeAll(): called");

        try
        {
            // Loop through all the loaded beans.
            for (Map.Entry<String, ConfigBean> entry : loadedBeans.entrySet())
            {
                String beanClassName = entry.getKey();

                // Unbind the beans JNDI name, but ignore any failures.
                try
                {
                    jndiContext.unbind(CONFIG_BEAN_ROOT + "/" + beanClassName);
                }
                catch (NameNotFoundException e)
                {
                    // Ignore, because the name was not found so was not bound succesfully at the configuration time.
                    e = null;
                }
            }

            // Unbind the configurator itself from its JNDI name.
            try
            {
                jndiContext.unbind(CONFIGURATOR_JNDI_NAME);
            }
            catch (NameNotFoundException e)
            {
                // Ignore, the configurator was not found because configuration failed.
                e = null;
            }
        }
        catch (NamingException e)
        {
            // Ignore, but print a warning. JNDI may not be accessible for some reason, configuration probably failed too.
            log.warn("There was a naming execption whilst cleaning up the config beans." +
                " Ignored but should not happen. " +
                " Most likely JNDI is not accessible and configuration failed too, cauing the clean up attempt to also fail.",
                e);
            e = null;
        }
    }

    /** {@inheritDoc} */
    public ConfigBean getConfiguredBean(String name) throws ConfigException
    {
        // log.debug("public ConfigBean getConfiguredBean(String name): called");
        // log.debug("name = " + name);

        try
        {
            return (ConfigBean) jndiContext.lookup(CONFIG_BEAN_ROOT + "/" + name);
        }
        catch (NameNotFoundException e)
        {
            throw new ConfigException("Dependancy bean " + name + " is not configured.", e, null, null);
        }
        catch (NamingException e)
        {
            throw new ConfigException("There was a naming exception whilst trying to look up the bean: " + name, e,
                null, null);
        }
    }

    /**
     * Gets a loaded config bean by name, or returns null if none with a matching name can be found.
     *
     * @param  name The class name of the bean to fetch.
     *
     * @return The loaded config bean, or null if none with a matching name can be found.
     */
    public ConfigBean getLoadedBean(String name)
    {
        return loadedBeans.get(name);
    }

    /**
     * Calls doConfigure on all loaded config beans with the specified value of the force flag.
     *
     * @param  force The force flag.
     *
     * @throws ConfigException If any bean throws a config exception or there were problems storing the configured beans
     *                         in the JNDI context.
     */
    private void doConfigAll(boolean force) throws ConfigException
    {
        try
        {
            // Loop through all the loaded beans.
            for (Map.Entry<String, ConfigBean> entry : loadedBeans.entrySet())
            {
                ConfigBean theBean = entry.getValue();
                String beanClassName = entry.getKey();

                // Trigger the beans doConfigure method using the correct force flag value.
                theBean.doConfigure(force, this);

                // Bind the configured bean to its JNDI name.
                jndiContext.rebind(CONFIG_BEAN_ROOT + "/" + beanClassName, theBean);
            }

            jndiContext.rebind(CONFIGURATOR_JNDI_NAME, this);
        }
        catch (NamingException e)
        {
            throw new ConfigException("Could not store configured bean or configurator in the JNDI context.", e, null,
                null);
        }
    }
}
