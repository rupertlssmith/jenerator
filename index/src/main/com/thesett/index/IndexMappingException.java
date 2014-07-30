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

import com.thesett.common.error.UserReadableRuntimeException;

/**
 * IndexMappingException represents a failure to map a type onto an index, either because no mapping exists for a type
 * or because a field exists in a mapping that cannot be found in an object being mapped.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Represent failure to map an object onto an index.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class IndexMappingException extends UserReadableRuntimeException
{
    /**
     * Creates the IndexMappingException.
     *
     * @param message        The exception message.
     * @param userMessageKey A key to look up user readable messages with.
     * @param userMessage    The user readable message or data string.
     * @param cause          The wrapped exception underlying this one.
     */
    public IndexMappingException(String message, Throwable cause, String userMessageKey, String userMessage)
    {
        super(message, cause, userMessageKey, userMessage);
    }
}
