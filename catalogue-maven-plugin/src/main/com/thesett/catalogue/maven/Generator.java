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
package com.thesett.catalogue.maven;

import java.util.Map;

/**
 * Generator is used to match and capture the configuration for a generator from within the plugin set up in a Maven
 * POM.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> Capture generator config parameters as a map. </td></tr>
 * </table></pre>
 */
public class Generator
{
    /** @parameter */
    private Map<String, String> config;

    /**
     * Provides the config map.
     *
     * @return The config map.
     */
    public Map<String, String> getConfig()
    {
        return config;
    }

    /**
     * Accepts the config map.
     *
     * @param config The config map.
     */
    public void setConfig(Map<String, String> config)
    {
        this.config = config;
    }
}
