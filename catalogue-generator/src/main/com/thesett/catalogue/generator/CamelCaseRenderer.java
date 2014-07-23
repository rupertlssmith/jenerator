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

import org.antlr.stringtemplate.AttributeRenderer;

import com.thesett.common.util.StringUtils;

/**
 * CamelCaseRenderer is a string template renderer for transforming output strings into camel case format.
 * Use the format name "cc" for camel case, and "ccu" for camel case with the first letter in upper case.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Render strings in camel case.
 * </table></pre>
 *
 * @author Rupert Smith
 */
class CamelCaseRenderer implements AttributeRenderer
{
    /** {@inheritDoc} */
    public String toString(Object o)
    {
        return o.toString();
    }

    /**
     * {@inheritDoc}
     *
     * <p/>Use the format name "cc" for camel case, and "ccu" for camel case with the first letter in uper case.
     */
    public String toString(Object o, String s)
    {
        if ("cc".equals(s))
        {
            return StringUtils.toCamelCase(o.toString());
        }
        else if ("ccu".equals(s))
        {
            return StringUtils.toCamelCaseUpper(o.toString());
        }
        else if ("u".equals(s))
        {
            return o.toString().toUpperCase();
        }
        else
        {
            return o.toString();
        }
    }
}
