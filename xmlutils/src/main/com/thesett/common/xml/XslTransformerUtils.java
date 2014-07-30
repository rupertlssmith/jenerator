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
package com.thesett.common.xml;

import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.util.Map;

import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 * Utility Class to perform xsl transformations.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Perform a parameterized xsl transformation between two character streams.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class XslTransformerUtils
{
    /**
     * Convert an XML string to another XML string using the XSL specified.
     *
     * @param  documentReader A reader to read the input document through.
     * @param  xslt           The URL of the xsl transformation file.
     * @param  parameters     A map of (String, Object) parameters to pass into the transformation.
     * @param  outputWriter   A writer that will accept the output of the transformation.
     *
     * @throws TransformerException If there was error during the tranformation.
     */
    public static void performXslTransformation(Reader documentReader, URL xslt, Map<String, Object> parameters,
        Writer outputWriter) throws TransformerException
    {
        // Create a new XSLT transformer to perform the transformation with, using the specified transformation.
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        StreamSource xslSource = new StreamSource(xslt.toString());
        Templates xslTemplates = transformerFactory.newTemplates(xslSource);
        Transformer transformer = xslTemplates.newTransformer();

        // Create a StreamSource to read the XML to be transformed.
        StreamSource xmlSource = new StreamSource(documentReader);

        // Pass any parameters to the transformation if some have been passed to this method.
        if (parameters != null)
        {
            for (Map.Entry<String, Object> nextEntry : parameters.entrySet())
            {
                transformer.setParameter(nextEntry.getKey(), nextEntry.getValue());
            }
        }

        // Create a StreamResult to write the transformed XML to.
        StreamResult result = new StreamResult(outputWriter);

        // Apply the transform.
        transformer.transform(xmlSource, result);
    }
}
