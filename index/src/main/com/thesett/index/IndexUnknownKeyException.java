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

import com.thesett.common.error.UserReadableException;

/**
 * IndexUnknownKeyException represents a failure to find a record in the index being refered to by its key.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Represent failure to find a record in an index by its key.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class IndexUnknownKeyException extends UserReadableException
{
    /**
     * Creates the IndexUnknownKeyException.
     *
     * @param message        The exception message.
     * @param userMessageKey A key to look up user readable messages with.
     * @param userMessage    The user readable message or data string.
     * @param cause          The wrapped exception underlying this one.
     */
    public IndexUnknownKeyException(String message, Throwable cause, String userMessageKey, String userMessage)
    {
        super(message, cause, userMessageKey, userMessage);
    }
}
