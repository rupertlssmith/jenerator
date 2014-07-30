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
