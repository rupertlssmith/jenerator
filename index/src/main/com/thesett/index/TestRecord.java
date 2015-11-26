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

/**
 * An example record to unit test indexes against.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Supply some sample fields for testing the index.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class TestRecord
{
    /** Holds the key of the record. */
    private final Long key;

    /** Holds a text field for indexing. */
    private final String text;

    /** A flag used to indicate that the text field has been accessed. */
    public boolean textFieldAccessed = false;

    /** Holds a text field for the summary record. */
    private final String title;

    /** A flag used to indicate that the title field has been accessed. */
    public boolean titleFieldAccessed = false;

    /** Holds the records rating. */
    private final float rating;

    /**
     * Creates a test record from a text.
     *
     * @param key    The records key.
     * @param text   The text to be indexed.
     * @param title  A title for the indexed text.
     * @param rating The records rating.
     */
    public TestRecord(long key, String text, String title, float rating)
    {
        // Keep the key and sample text.
        this.key = key;
        this.text = text;
        this.title = title;
        this.rating = rating;
    }

    /**
     * Gets the records key.
     *
     * @return The records key.
     */
    public Long getKey()
    {
        return key;
    }

    /**
     * Gets the records text body.
     *
     * @return The records text body.
     */
    public String getText()
    {
        textFieldAccessed = true;

        return text;
    }

    /**
     * Gets the records title.
     *
     * @return The records title.
     */
    public String getTitle()
    {
        titleFieldAccessed = true;

        return title;
    }

    /**
     * Gets the records summary record. This is an abbreviated version that just contains the title and rating.
     *
     * @return The records summary record.
     */
    public TestRecordSummary getSummaryRecord()
    {
        return new TestRecordSummary(title, rating);
    }

    /**
     * Describes the summary recrod that will be stored in the index.
     */
    public static class TestRecordSummary
    {
        /** The title. */
        public String title;

        /** The records rating. */
        public float rating;

        /**
         * Creates a new summary record.
         *
         * @param title  The title.
         * @param rating The rating.
         */
        public TestRecordSummary(String title, float rating)
        {
            this.title = title;
            this.rating = rating;
        }

        /**
         * Gets the rating.
         *
         * @return The rating.
         */
        public float getRating()
        {
            return rating;
        }

        /**
         * Prints the record as a string for debugging purposes.
         *
         * @return The record as a string for debugging purposes.
         */
        public String toString()
        {
            return title + ", " + rating;
        }
    }
}
