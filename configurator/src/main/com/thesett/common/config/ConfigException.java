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
package com.thesett.common.config;

import com.thesett.common.error.UserReadableException;

/**
 * ConfigException represents the failure of a configuration bean to succesfully configure itself.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Represent the failure of a configuration bean to succesfully configure itself.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class ConfigException extends UserReadableException
{
    /**
     * Creates the ConfigException.
     *
     * @param message the exception message
     * @param e the wrapped exception underlying this one
     * @param userMessageKey a key to look up user readable messages with
     * @param userMessage the user readable message or data string
     */
    public ConfigException(String message, Exception e, String userMessageKey, String userMessage)
    {
        super(message, e, userMessageKey, userMessage);
    }
}
