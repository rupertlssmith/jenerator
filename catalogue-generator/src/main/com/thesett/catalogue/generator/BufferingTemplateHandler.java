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

import org.stringtemplate.v4.ST;

/**
 * BufferingTemplateHandler is a processed template handler, that retains the output fragments from all processed
 * templates that it recieves in a buffer, so that the contents of the buffer may be output in a correct sequence at a
 * later time.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Retain the output of a template for later processing.
 * </table></pre>
 */
public class BufferingTemplateHandler implements BaseGenerator.RenderTemplateHandler
{
    /** The buffer to build up the output in. */
    private StringBuffer buffer = new StringBuffer();

    /** {@inheritDoc} */
    public void render(ST template, String outputName)
    {
        buffer.append(template.render());
    }

    /** Clears the internal buffer. */
    public void clear()
    {
        buffer = new StringBuffer();
    }

    /**
     * Provides the contents of the buffer that template output has been written to.
     *
     * @return The contents of the buffer that template output has been written to.
     */
    public String toString()
    {
        return buffer.toString();
    }
}
