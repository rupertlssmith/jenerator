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
package com.thesett.catalogue.generator;

import java.util.Locale;

import org.stringtemplate.v4.AttributeRenderer;

import com.thesett.common.util.StringUtils;

/**
 * CamelCaseRenderer is a string template renderer for transforming output strings into camel case format. Use the
 * format name "cc" for camel case, and "ccu" for camel case with the first letter in upper case.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Render strings in camel case.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class CamelCaseRenderer implements AttributeRenderer
{
    /** {@inheritDoc} */
    public String toString(Object o, String s, Locale locale)
    {
        return toString(o, s);
    }

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
        else if ("ccl".equals(s))
        {
            return StringUtils.toCamelCaseLower(o.toString());
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
