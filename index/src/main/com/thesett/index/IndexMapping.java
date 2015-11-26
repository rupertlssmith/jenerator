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
package com.thesett.index;

import java.io.Serializable;

/**
 * An IndexMapping encapsulates information that tells an {@link Index} how to extract and index fields from different
 * classes.
 *
 * <p/>The {@link #IndexMapping(String[], String)} method accepts a list of field name on the full record (D) that are
 * to be extracted as Strings and indexed and the name of a field on the summary record (E) that is used to extract the
 * records rating for search results ordering.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Record class, fields to extract and rating field.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class IndexMapping implements Serializable
{
    /** Holds the names of the fields to extract for indexing. */
    private final String[] fieldNames;

    /** Holds the name of the field to extract the rating from. */
    private final String ratingFieldName;

    /**
     * Creates an index mapping for the specifeid class, the names of the fields that are to be extracted and indexed
     * and the name of the field that the indexed records rating is to be extracted from.
     *
     * @param fieldNames      The names of the fields to extract.
     * @param ratingFieldName The name of the field to extract the rating from.
     */
    public IndexMapping(String[] fieldNames, String ratingFieldName)
    {
        this.fieldNames = fieldNames;
        this.ratingFieldName = ratingFieldName;
    }

    /**
     * Gets the fieldNames to extract.
     *
     * @return The mapping field names.
     */
    public String[] getFieldNames()
    {
        return fieldNames;
    }

    /**
     * Gets the rating field name.
     *
     * @return The mapping rating field name.
     */
    public String getRatingFieldName()
    {
        return ratingFieldName;
    }
}
