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
package com.thesett.catalogue.generator;

import org.antlr.stringtemplate.StringTemplateErrorListener;

/**
 * A string template error handler. Does nothing.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Ignore all errors.
 * </table></pre>
 *
 * @author Rupert Smith
 */
class DummyErrorHandler implements StringTemplateErrorListener
{
    /**
     * Does nothing.
     *
     * @param s         Ignored.
     * @param throwable Ignored.
     */
    public void error(String s, Throwable throwable)
    {
    }

    /**
     * Does nothing.
     *
     * @param s Ignored.
     */
    public void warning(String s)
    {
    }
}
