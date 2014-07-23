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
    private int column;

    /** Holds the error line location. */
    private int line;

    /** Holds the error message. */
    private String message;

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
