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

import com.thesett.catalogue.model.Catalogue;
import com.thesett.common.config.ConfigBean;

/**
 * CatalogueConfigBean performs application start-up time configurations to prepare a catalogue model for use. It loads
 * and validates the in-memory knowledge level model of the catalogue.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities
 * <tr><td> Provide a fully configured catalogue model from a raw configuration.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public interface CatalogueConfigBean extends ConfigBean
{
    /**
     * Gets the catalogue knowledge level model that this config bean has created from the raw catalogue model.
     *
     * @return The catalogue knowledge level model.
     */
    Catalogue getCatalogue();

    /**
     * Gets the package name under which the model has been generated.
     *
     * @return The package name under which the model has been generated.
     */
    String getModelPackage();

    /**
     * Sets the package name under which the model has been generated.
     *
     * @param packageName The package name under which the model has been generated.
     */
    void setModelPackage(String packageName);
}
