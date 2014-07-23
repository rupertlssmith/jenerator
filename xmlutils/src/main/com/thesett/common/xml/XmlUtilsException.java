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

import java.util.Collection;
import java.util.Comparator;
import java.util.PriorityQueue;

import com.thesett.common.error.UserReadableRuntimeException;

/**
 * XmlUtilsException is used to represent a wide variety of XML related exceptions. For example parsing errors,
 * parser unsupported feature errors, validation errors, malformed xml errors and so on. Generally speaking methods
 * in the {@link XmlUtils} class throw this as a convenience exception to capture non-recoverable error conditions.
 *
 * <p/>Recoverable conditions have more specific exception types to represent them.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Represent an unrecoverable condition when working with XML utility methods.
 * <tr><td> Act as a container for multiple parsing errors.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class XmlUtilsException extends UserReadableRuntimeException
{
    /** Holds any parsing errors associated with this exception, sorted by line number. */
    private Collection<ParsingError> parsingErrors;

    /**
     * Creates a new xml utils exception.
     *
     * @param message     The exception message.
     * @param cause       The wrapped exception underlying this one.
     * @param key         A key to look up user readable messages with.
     * @param userMessage The user readable message or data string.
     */
    public XmlUtilsException(String message, Throwable cause, String key, String userMessage)
    {
        super(message, cause, key, userMessage);
    }

    /**
     * Associates a parsing error with this exception.
     *
     * @param error The parsing error.
     */
    public void addParsingError(ParsingError error)
    {
        if (parsingErrors == null)
        {
            parsingErrors = new PriorityQueue<ParsingError>(1, new ParsingErrorComparator());
        }

        parsingErrors.add(error);
    }

    /**
     * Gets all the parsing errors associated with this exception, in order of the line numbers where they occurred.
     *
     * @return The parsing errors associated with this exception, in order of the line numbers where they occurred.
     */
    public Collection<ParsingError> getParsingErrors()
    {
        return parsingErrors;
    }

    /**
     * Compares parsing errors by line number.
     */
    public static class ParsingErrorComparator implements Comparator<ParsingError>
    {
        /**
         * Compares two parsing errors by line number.
         *
         * @param e1 The first parsing error.
         * @param e2 The second parsing error.
         *
         * @return A negative integer, zero, or a positive integer as the first argument is less than, equal to, or
         *         greater than the second.
         */
        public int compare(ParsingError e1, ParsingError e2)
        {
            int l1 = e1.getLine();
            int l2 = e2.getLine();

            return (l1 == l2) ? 0 : ((l1 < l2) ? -1 : 1);
        }

        /**
         * Checks if another comparator is the same as this one.
         *
         * @param o The object to compare to.
         *
         * @return <tt>true</tt> only if the specified object is also a comparator and it imposes the same ordering as
         *         this comparator.
         */
        public boolean equals(Object o)
        {
            return o instanceof ParsingErrorComparator;
        }
    }
}
