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
 * <p/>Some entities may also provide an external id, which serves to uniquely identify the entity over a much larger
 * realm and for a much longer time than the entities storage key.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities
 * <tr><td> Provide a long lived external key to uniquely identify an entity.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public interface ExternallyIdentified
{
    /**
     * Gets the external id of the element.
     *
     * @return The external id of the element.
     */
    public ExternalId getExternalId();

    /**
     * Sets the external id of the element.
     *
     * @param id The external id of the element.
     */
    void setExternalId(ExternalId id);
}
