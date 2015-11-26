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
package com.thesett.common.xml;

/**
 * Wraps a single parsing error, giving details of the column and line where it occurred.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Hold parsing errors.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class ParsingError
{
    /** Holds the error column location. */
    private final int column;

    /** Holds the error line location. */
    private final int line;

    /** Holds the error message. */
    private final String message;

    /**
     * Creates a new parsing error for a given location.
     *
     * @param column  The error column location.
     * @param line    The error line location.
     * @param message The error message.
     */
    public ParsingError(int column, int line, String message)
    {
        this.column = column;
        this.line = line;
        this.message = message;
    }

    /**
     * Gets the column location of the error.
     *
     * @return The column location of the error.
     */
    public int getColumn()
    {
        return column;
    }

    /**
     * Gets the line location of the error.
     *
     * @return The line location of the error.
     */
    public int getLine()
    {
        return line;
    }

    /**
     * Gets the error message.
     *
     * @return The error message.
     */
    public String getMessage()
    {
        return message;
    }
}
