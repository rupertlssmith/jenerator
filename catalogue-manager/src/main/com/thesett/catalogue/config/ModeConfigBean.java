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

import java.io.Serializable;

import com.thesett.common.config.ConfigBean;
import com.thesett.common.config.ConfigBeanContext;
import com.thesett.common.config.ConfigException;

/**
 * The catalogue supports different modes with slightly different behaviour depending on the context in which it is
 * being run. In development mode, it completely re-creates the database every time it is started and populates this
 * database with any knowledge level data that is mapped into the database. In production mode, it must be started with
 * an existng database schema loaded and in place, already populated with any required knowledge level data. When
 * starting in production mode the database schema will be verified against the catalogue to ensure that its schema
 * matches the catalogue and that all knowledge level data is correctly populated; it will fail to start if this is not
 * the case.
 *
 * <p/>Development mode allows the catalogue to evolve and the database to be kept up to date with it. Production mode
 * exists to ensure that data really is persistent beyond the runtime life cycle of the application; typically
 * production applications do not drop or alter database tables either for added safety of the persistent data.
 *
 * <p/>Other operation mode switches and parameters may be added to this config bean.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Set or clear the development mode switch.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class ModeConfigBean implements ConfigBean, Serializable
{
    /** Holds the value of the development mode switch. */
    private boolean devMode;

    /** Flag used to indicate that this config bean has been succesfully run. */
    private boolean configured = false;

    /**
     * Tells the bean to perform whatever configuration it is intended to do.
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

        // Nothing to do, just set the configured flag.
        configured = true;
    }

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
     * Sets the value of the development mode switch.
     *
     * @param devMode The development mode switch.
     */
    public void setDevMode(boolean devMode)
    {
        this.devMode = devMode;
    }

    /**
     * Gets the value of the development mode switch.
     *
     * @return The value of the development mode switch.
     */
    public boolean isDevMode()
    {
        return devMode;
    }
}
