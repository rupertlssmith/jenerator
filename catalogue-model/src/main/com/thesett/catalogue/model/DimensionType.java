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

import java.io.Serializable;

/**
 * A DimensionType is a {@link com.thesett.aima.state.Type} that is an {@link EntityType} that, in addition to the
 * storage layout of an entity type, has a special denormalized storage layout of that sort that is used to build 'star
 * schemas' for data warehousing.
 *
 * <p/>A dimension is an 'axis of investigation' relating to a factual entry in a data warehouse. For example, if the
 * fact is a record of a completed purchase, a dimension may represent the time, date, place of purchase, item
 * purchased, customer demographic and so on relating to the purchase. Dimensions are used to query and group facts in
 * order to provide statistical answers to questions such as who?, when?, what?, where? and ultimately why? around the
 * related fact.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Provide the types and names of fields that make up an 'axis of investigation'.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public interface DimensionType extends EntityType, Serializable
{
    /** Enumeration to specify the dimension type, dimension or conformed dimension. */
    enum ConformedType implements Serializable
    {
        /** Used to indicate an ordinary dimension. */
        Dimension,

        /** Used to indicate a conformed dimension. */
        Conformed
    }
}
