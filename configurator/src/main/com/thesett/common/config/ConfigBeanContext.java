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
 * ConfigBeanContext provides a context in which beans that have been created and configured by a configuration system
 * may be located by other beans requiring their services. This interface is effectively a service locator.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities
 * <tr><td> Locate managed configuration beans.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public interface ConfigBeanContext
{
    /**
     * Gets successfully configured beans from the cache of configured beans.
     *
     * @param  name The class name of the bean to fetch.
     *
     * @return The matching configured bean, or null if none with a matching name can be found.
     *
     * @throws ConfigException If there is an error whilst looking up the bean, or if the bean is not configured.
     */
    ConfigBean getConfiguredBean(String name) throws ConfigException;
}
