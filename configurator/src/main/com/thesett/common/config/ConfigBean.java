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

/**
 * A ConfigBean is a bean (no arg constructor, get and set property fields) that is reponsible for configuring some
 * aspect of a system, often at start up time. They are beans so that they are easily set up by a configurator from a
 * properties or other config file. In addition they support a method to report that thay have carried out their
 * configuration duties succesfully and a method to trigger the configuration process that they represent. Config beans
 * must work in conjunction with a {@link Configurator}, and an instance of it is passed to the {@link #doConfigure}
 * method. The configurator provides methods for a config bean to query other config beans that it may depend upon.
 *
 * <p>The {@link #doConfigure} method takes a force parameter as an argument. If this is set to true then it should
 * force re-configuration to take place at run time. ConfigBeans should support runtime reconfiguration unless it is
 * impossible. Every effort should be made to support it because it extremely usefull to be able to reconfigure a
 * running system.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities
 * <tr><td> Report succesful configuration.
 * <tr><td> Perform configuration with respect to a configurator <td> {@link Configurator}
 * <tr><td> Support runtime reconfiguration.
 * </table></pre>
 *
 * @author Rupert Smith
 * @todo   A mechanism is to be added so that config beans can declare there dependencies. The configurator is then
 *         responsible for calling the config beans in the right order to allow depenedencies to be resolved.
 */
public interface ConfigBean
{
    /**
     * Tells the bean to perform whatever configuration it is intended to do.
     *
     * @param  force             Setting this to true tells the config bean to re-run its configuration action even if
     *                           it has already been run.
     * @param  configBeanContext A reference to the configurator that is managing the whole configuration process.
     *
     * @throws ConfigException If some error occurs that means that the configuration cannot be succesfully completed.
     */
    void doConfigure(boolean force, ConfigBeanContext configBeanContext) throws ConfigException;

    /**
     * Checks whether or not the config bean has been succesfully run and is in a configured state.
     *
     * @return True if the config bean has run its configuration succesfully.
     */
    boolean getConfigured();
}
