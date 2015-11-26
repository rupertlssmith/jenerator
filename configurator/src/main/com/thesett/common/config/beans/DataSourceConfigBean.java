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
package com.thesett.common.config.beans;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.log4j.Logger;

import com.thesett.common.config.ConfigBean;
import com.thesett.common.config.ConfigBeanContext;
import com.thesett.common.config.ConfigException;

/**
 * DataSourceConfigBean registers a {@link javax.sql.DataSource} in the default initial naming context.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Binds a data source to a JNDI name.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class DataSourceConfigBean implements ConfigBean
{
    /** Used for logging. */
    private static final Logger log = Logger.getLogger(DataSourceConfigBean.class);

    /** The JNDI name to register the data source under. */
    private String jndiName;

    /** The DataSource implementation class. */
    private String dataSourceClass;

    /* The server name property for the data source. */
    //private String databaseServer;

    /* The port for the data source. */
    //private int databasePort;

    /* The database name for the data source. */
    //private String databaseName;

    /** Flag to represent configuration status of this configure bean. */
    private boolean configured;

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
     * Sets the JNDI name under which to bind the data source.
     *
     * @param jndiName The JNDI name under which to bind the data source.
     */
    public void setJndiName(String jndiName)
    {
        this.jndiName = jndiName;
    }

    /**
     * Sets the data source class name to instantiate.
     *
     * @param dataSourceClass The data source class name to instantiate.
     */
    public void setDataSourceClass(String dataSourceClass)
    {
        this.dataSourceClass = dataSourceClass;
    }

    /*
     * Sets the database server name or ip address.
     *
     * @param databaseServer The database server name or ip address.
     */
    /*public void setDatabaseServer(String databaseServer)
    {
        this.databaseServer = databaseServer;
    }*/

    /*
     * Sets the datbase server port or ip address.
     *
     * @param databasePort The datbase server port or ip address.
     */
    /*public void setDatabasePort(int databasePort)
    {
        this.databasePort = databasePort;
    }*/

    /*
     * Sets the database name on the server.
     *
     * @param databaseName The database name on the server.
     */
    /*public void setDatabaseName(String databaseName)
    {
        this.databaseName = databaseName;
    }*/

    /**
     * Creates an instance of the data source and binds it to the JNDI name.
     *
     * @param  force             Setting this to true tells the config bean to re-run its configuration action even if
     *                           it has already been run.
     * @param  configBeanContext A reference to the configurator that is managing the whole configuration process.
     *
     * @throws ConfigException If some error occurs that means that the configuration cannot be succesfully completed.
     *
     * @todo   The database name, address and port number all need to be set in the data source.
     */
    public void doConfigure(boolean force, ConfigBeanContext configBeanContext) throws ConfigException
    {
        // Check if the configuration has already been done but the force flag is false, in which case return
        // without doing the configuration.
        if (configured && !force)
        {
            log.debug("Already configured and force not set, so returning without doing any configuration.");

            return;
        }

        try
        {
            // Create the data source
            DataSource ds = (DataSource) Class.forName(dataSourceClass).newInstance();

            // Configure the data source
            BeanInfo beanInfo = Introspector.getBeanInfo(Class.forName(dataSourceClass));
            PropertyDescriptor[] descs = beanInfo.getPropertyDescriptors();

            log.debug("Properties of the DataSource are:");

            for (int i = 0; i < descs.length; i++)
            {
                log.debug("Property " + descs[i].getName() + " has type " + descs[i].getPropertyType());
            }

            // Get reference to initial context
            Context ctx = new InitialContext();

            // Unbind the JNDI name if it already exists
            try
            {
                ctx.unbind(jndiName);
            }
            catch (NameNotFoundException e)
            {
                log.debug("The JNDI name " + jndiName + " was not already bound");

                // Exception ignored, as it simply means that the name was not already bound and therefore did not
                // need to be removed.
                e = null;
            }
            catch (NamingException e)
            {
                log.debug("There was a naming exception whilst unbinding the name " + jndiName);

                throw new ConfigException("Got a NamingException whilst trying to unbind '" + jndiName + "'.", e, null,
                    null);
            }

            // Bind the new data source to the JNDI name
            ctx.bind(jndiName, ds);

            // Configuration was succesfull so set the configured flag.
            configured = true;
        }
        catch (Exception e)
        {
            log.debug("There was an exception whilst creating and binding data source to JNDI name.", e);

            // Rethrow this as a ConfigException
            throw new ConfigException("Exception whilst creating and binding data source to JNDI name: " +
                e.getMessage(), e, null, null);
        }
    }
}
