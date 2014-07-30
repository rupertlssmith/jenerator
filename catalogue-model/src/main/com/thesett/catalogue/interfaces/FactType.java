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
package com.thesett.catalogue.interfaces;

import java.io.Serializable;

/**
 * A FactType is a {@link com.thesett.aima.state.Type} that is an {@link EntityType} that holds numerical values at the
 * intersection of many axes of investigation. A fact is an entity because it is persistently stored, but facts are only
 * ever stored in denormalized forms in data warehouses and not in online databases.
 *
 * <p/>Facts usually represent a single occurrence, or event, whereas an online database entity may represent an ongoing
 * situation. For example, if an entity is used to maintain the state of a customer order, through order assembly,
 * quote, purchase, dispatch, completion and so on, each of these individual states tracked by an entity may trigger an
 * individual fact to be inserted into a data warehouse, if the event is of statistical interest.
 *
 * <p/>The reason that facts usually only hold numerical values, is that facts are not intended to be consumed on an
 * individual basis. Facts exist at the intersection of many dimensions, which are used to group them together. For
 * example if a fact records the price of a purchase order, and a dimension links the purchase event to a customer
 * demographic, then the purchases may be queried by demographic groupings. Each query will return a set of facts which
 * need to be statistically summarized, and numerical data is the most amenable to providing averages, variances, max
 * and mins and other statistical results.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Provide the types and names of numerical fields that make up a data warehouse fact.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public interface FactType extends EntityType, Serializable
{
}
