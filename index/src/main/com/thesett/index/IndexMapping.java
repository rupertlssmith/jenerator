/*
 * © Copyright Rupert Smith, 2005 to 2013.
 *
 * ALL RIGHTS RESERVED. Any unauthorized reproduction or use of this
 * material is prohibited. No part of this work may be reproduced or
 * transmitted in any form or by any means, electronic or mechanical,
 * including photocopying, recording, or by any information storage
 * and retrieval system without express written permission from the
 * author.
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
    private String[] fieldNames;

    /** Holds the name of the field to extract the rating from. */
    private String ratingFieldName;

    /**
     * Creates an index mapping for the specifeid class, the names of the fields that are to be extracted and indexed and
     * the name of the field that the indexed records rating is to be extracted from.
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
