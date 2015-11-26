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
package com.thesett.catalogue.model;

/**
 * The raw data service provides CRUD and replace operation on operational level catalogue data, for raw data in XML
 * format. It also provides validation of the raw data against the catalogue knowledge level and detailed error
 * reporting of any validation failures.
 *
 * <p/>This service is intended to be used by the data upload and extract tools.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Perform CRUD operations on dimension elements. <td> {@link CatalogueManagerService}
 * <tr><td> Validate raw operational data against the catalogue knowledge level.
 * <tr><td> Provide error reporting on invalid data.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public interface RawDataService
{
    /**
     * Validates and uploads the data set from raw XML.
     *
     * @param dataSet The data set to upload in XML.
     */
    void uploadDataSet(String dataSet); //throws MultipleUserErrorException;

    /**
     * Used to ping the service to check it is reachable.
     *
     * @return <tt>true</tt> always.
     */
    boolean ping();
}
