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

import java.io.IOException;
import java.io.Serializable;
import java.util.Properties;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import com.thesett.common.config.ConfigBean;
import com.thesett.common.config.ConfigBeanContext;
import com.thesett.common.config.ConfigException;
import com.thesett.common.util.PropertiesHelper;

/**
 * Sets up hibernate from properties and mapping files. Allows two properties file to set up two session factories, but
 * the second one is optional.
 *
 * <p/>Configuration is handled differently by this bean depending on the value of the development mode switch set up
 * using the {@link ModeConfigBean}. If development mode is turned on then the database schema is dropped and re-built
 * by this config bean when the session factories are created. In production mode the database schema is verified
 * against the hibernate mapping. This is done by setting the value of the 'hibernate.hbm2ddl.auto' property to
 * 'create-drop' for development mode, and to 'validate' for production mode.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class HibernateConfigBean implements ConfigBean, Serializable
{
    /** Holds a reference to the hibernate session. */
    private SessionFactory factory;

    /** Holds a reference to the secondary hibernate session. */
    private SessionFactory secondaryFactory;

    /** Flag used to indicate that this config bean has been succesfully run. */
    private boolean configured = false;

    /** Holds the resource name of the hibernate mapping to use for the catalogue. */
    private String mappingResource;

    /** Holds the resource name of the hibernate configuration properties to use. */
    private String propertiesResource;

    /** Holds the resource name of the optional secondary hibernate configuration properties to use. */
    private String secondaryPropertiesResource;

    /**
     * Checks whether or not the config bean has been succesfully run and is in a configured state.
     *
     * @return True if the config bean has run its configuration succesfully.
     */
    public boolean getConfigured()
    {
        return configured;
    }

    /**
     * Builds a hibernate session factory for the catalogue model.
     *
     * @param  force             Setting this to true tells the config bean to re-run its configuration action even if
     *                           it has already been run.
     * @param  configBeanContext A reference to the configurator that is managing the whole configuration process.
     *
     * @throws ConfigException If some error occurs that means that the configuration cannot be succesfully completed.
     */
    public void doConfigure(boolean force, ConfigBeanContext configBeanContext) throws ConfigException
    {
        // If already configured then only reconfigure if force is set to true
        if (configured && !force)
        {
            return;
        }

        // Check if a session factory from a previous configuration exists and close it if so.
        if (factory != null)
        {
            factory.close();
        }

        // Ensure that the mode config bean has been set up, and get a reference to it.
        ModeConfigBean modeBean =
            (ModeConfigBean) configBeanContext.getConfiguredBean("com.thesett.catalogue.config.ModeConfigBean");

        // Create the primary hibernate configuration using the defined properties and catalogue model.
        Properties configProperties = extractProperties(getPropertiesResource());
        configProperties = modifyPropertiesForMode(configProperties, modeBean);
        factory = createSessionFactory(configProperties);

        // If an optional secondary resource was specified, create a secondary session factory for it.
        if (getSecondaryPropertiesResource() != null)
        {
            configProperties = extractProperties(getSecondaryPropertiesResource());
            configProperties = modifyPropertiesForMode(configProperties, modeBean);
            secondaryFactory = createSessionFactory(configProperties);
        }

        // Configuration succesfull so set the flag.
        configured = true;
    }

    /**
     * Sets the hibernate mapping file resource name.
     *
     * @param resourceName The hibernate mapping file resource name.
     */
    public void setMappingResource(String resourceName)
    {
        this.mappingResource = resourceName;
    }

    /**
     * Gets the hibernate mapping file resource name.
     *
     * @return The hibernate mapping file resource name.
     */
    public String getMappingResource()
    {
        return mappingResource;
    }

    /**
     * Sets the hibernate properties file resource name.
     *
     * @param resourceName The hibernate properties file resource name.
     */
    public void setPropertiesResource(String resourceName)
    {
        this.propertiesResource = resourceName;
    }

    /**
     * Gets the hibernate properties file resource name.
     *
     * @return The hibernate file resource name.
     */
    public String getPropertiesResource()
    {
        return propertiesResource;
    }

    /**
     * Sets the hibernate secondary properties file resource name.
     *
     * @param resourceName The hibernate secondary properties file resource name.
     */
    public void setSecondaryPropertiesResource(String resourceName)
    {
        this.secondaryPropertiesResource = resourceName;
    }

    /**
     * Gets the hibernate secondary properties file resource name.
     *
     * @return The hibernate file resource name.
     */
    public String getSecondaryPropertiesResource()
    {
        return secondaryPropertiesResource;
    }

    /**
     * Obtains a hibernate session for the hibernate configuration and session factory set up by this config bean.
     *
     * @return A hibernate session for the catalogue.
     */
    public Session getSession()
    {
        if (factory != null)
        {
            return factory.openSession();
        }
        else
        {
            return null;
        }
    }

    /**
     * Returns the hibernate session factory.
     *
     * @return The hibernate session factory.
     */
    public SessionFactory getSessionFactory()
    {
        return factory;
    }

    /**
     * Obtains a hibernate session for the hibernate secondary configuration and session factory set up by this config
     * bean, if the optional second resource file was specified.
     *
     * @return A hibernate session for the catalogue.
     */
    public Session getSecondarySession()
    {
        if (secondaryFactory != null)
        {
            return secondaryFactory.openSession();
        }
        else
        {
            return null;
        }
    }

    /**
     * Creates a hibernate session factory from a set of properties.
     *
     * @param  configProperties The properties.
     *
     * @return A hibernate session factory.
     *
     * @throws ConfigException If any configuration errors occurr within Hibernate.
     */
    private SessionFactory createSessionFactory(Properties configProperties) throws ConfigException
    {
        SessionFactory sessionFactory;

        try
        {
            Configuration config =
                new Configuration().addProperties(configProperties).addResource(getMappingResource());

            // Create the session factory.
            sessionFactory = config.buildSessionFactory();
        }
        catch (HibernateException e)
        {
            throw new ConfigException(
                "Hibernate threw an exception during creation of its configuration or the session factory.", e, null,
                null);
        }

        return sessionFactory;
    }

    /**
     * Extracts the properties from a named resource.
     *
     * @param  propertiesResource The name of the resource.
     *
     * @return The properties extracted from the resource.
     *
     * @throws ConfigException If the resource cannot be found, or parsed as a properties file.
     */
    private Properties extractProperties(String propertiesResource) throws ConfigException
    {
        // Load the hibernate properties.
        Properties configProperties = null;

        try
        {
            configProperties =
                PropertiesHelper.getProperties(this.getClass().getClassLoader().getResourceAsStream(
                        propertiesResource));
        }
        catch (IOException e)
        {
            throw new ConfigException("The hibernate properties resource, " + propertiesResource +
                ", cannot be loaded.", e, null, null);
        }

        return configProperties;
    }

    /**
     * Modifes the hibernate properties appropriately for the catalogue runtime mode being used. In development mode the
     * 'hibernate.hbm2ddl.auto' property is set to 'create-drop', and in production mode it is set to 'validate'.
     *
     * @param  properties The properties hibernate session factory is being configured with.
     * @param  modeBean   A reference to the mode configuration bean.
     *
     * @return The properties modified appropriately for the mode.
     */
    private Properties modifyPropertiesForMode(Properties properties, ModeConfigBean modeBean)
    {
        // Check if in development mode.
        if (modeBean.isDevMode())
        {
            properties.setProperty("hibernate.hbm2ddl.auto", "create");
        }

        // Otherwise in production mode.
        else
        {
            properties.setProperty("hibernate.hbm2ddl.auto", "validate");
        }

        return properties;
    }
}
