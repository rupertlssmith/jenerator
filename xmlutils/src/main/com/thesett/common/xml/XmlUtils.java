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

import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.thesett.common.validate.Validation;

/**
 * Provides a set of general purpose utility methods for working with XML.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th>Responsibilities <th>Collaborations
 * <tr><td>Perform schema validation of xml documents.
 * <tr><td>Parse and load xml documents into a document object model.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class XmlUtils
{
    /** Used for logging. */
    private static final Logger log = Logger.getLogger(XmlUtils.class);

    /**
     * Loads and validates an XML document against an XML schema.
     *
     * @param  documentReader A reader for the document to be validated.
     * @param  namespace      The target namespace of the schema. If null no namespace will be used.
     * @param  schemaLocation The location of the schema to validate against.
     *
     * @return The parsed document.
     *
     * @throws XmlUtilsException If a parsing or validation error occurs.
     * @throws IOException       If the document cannot be read from its reader.
     */
    public static Document loadAndValidateSchema(Reader documentReader, String namespace, String schemaLocation)
        throws XmlUtilsException, IOException
    {
        DocumentBuilder docBuilder = null;

        // Set up the parser.
        try
        {
            // Create a parser to parse the document with.
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();

            // Turn on full validation on the parser.
            docBuilderFactory.setFeature("http://apache.org/xml/features/validation/dynamic", true);
            //parser.setFeature("http://apache.org/xml/features/validation/dynamic", true);
            //parser.setFeature("http://apache.org/xml/features/validation/schema", true);
            //parser.setFeature("http://apache.org/xml/features/validation/schema-full-checking", true);

            // Give the parser the location of the schema to validate against.
            if (Validation.isEmpty(namespace))
            {
                docBuilderFactory.setAttribute(
                    "http://apache.org/xml/properties/schema/external-noNamespaceSchemaLocation", schemaLocation);
            }
            else
            {
                SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                Schema schema = sf.newSchema(new URL(schemaLocation));
                docBuilderFactory.setNamespaceAware(true);
                docBuilderFactory.setSchema(schema);
            }

            docBuilder = docBuilderFactory.newDocumentBuilder();
        }
        catch (SAXException e)
        {
            throw new XmlUtilsException("The could not locate the schema.", e, null, null);
        }
        catch (ParserConfigurationException e)
        {
            throw new XmlUtilsException("The XML parser could not be created.", e, null, null);
        }

        // Parse the document. There were no errors so return the parsed document.
        return parse(docBuilder, documentReader);
    }

    /**
     * Parses an XML document from the specified reader. Any parsing errors will be returned inside an
     * {@link XmlUtilsException}.
     *
     * @param  documentReader The reader to read the XML as a string from.
     *
     * @return The XML parsed as a W3C DOM.
     *
     * @throws XmlUtilsException If a parsing or validation error occurs.
     * @throws IOException       If an i/o execption occurs whilst reading the underlying document reader stream.
     */
    public static Document loadAndValidate(Reader documentReader) throws XmlUtilsException, IOException
    {
        DocumentBuilder docBuilder = null;

        // Set up the parser.
        try
        {
            // Create a parser to parse the document with.
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();

            // Turn on full validation on the parser.
            docBuilderFactory.setValidating(true);

            docBuilder = docBuilderFactory.newDocumentBuilder();
        }
        catch (ParserConfigurationException e)
        {
            throw new XmlUtilsException("The XML parser could not be created.", e, null, null);
        }

        // Parse the document.
        // There were no errors so return the parsed document.
        return parse(docBuilder, documentReader);
    }

    /**
     * Parses an XML document from the specified reader. Any parsing errors will be returned inside an
     * {@link XmlUtilsException}.
     *
     * @param  documentReader The reader to read the XML as a string from.
     *
     * @return The XML parsed as a W3C DOM.
     *
     * @throws XmlUtilsException If a parsing or validation error occurs.
     * @throws IOException       If an i/o execption occurs whilst reading the underlying document reader stream.
     */
    public static Document load(Reader documentReader) throws XmlUtilsException, IOException
    {
        try
        {
            // Create a parser to parse the document with.
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();

            // Parse the document.
            Document doc = parse(docBuilder, documentReader);

            // There were no errors so return the parsed document.
            return doc;
        }
        catch (ParserConfigurationException e)
        {
            throw new XmlUtilsException("The XML parser could not be created.", e, null, null);
        }
    }

    /**
     * Helper method to parse an XML document. The parser should be passed to this method with its features and
     * properties already set up. This method performs the parsing and captures any errors.
     *
     * @param  parser         The DOM parser.
     * @param  documentReader The reader to get the raw document from.
     *
     * @return The parsed DOM document.
     *
     * @throws IOException       If an i/o exception occurs on the document reader.
     * @throws XmlUtilsException If a parsing error occurs. The errors will be wrapped inside the exception.
     */
    private static Document parse(DocumentBuilder parser, Reader documentReader) throws IOException, XmlUtilsException
    {
        // Used to capture any parsing errors.
        ValidationErrorHandler errorHandler;

        Document doc;

        try
        {
            // Set up an error handler for the parser to call back with any parsing or validation errors.
            errorHandler = new ValidationErrorHandler();
            parser.setErrorHandler(errorHandler);

            // Parse and validate the document.
            InputSource source = new InputSource(documentReader);
            doc = parser.parse(source);
        }
        catch (SAXException e)
        {
            throw new XmlUtilsException("The XML parser encountered a general error condition.", e, null, null);
        }

        // Check if there were any errors return them inside an exception if so.
        if (errorHandler.errorFound)
        {
            XmlUtilsException e = new XmlUtilsException("There were parsing errors.", null, null, null);

            for (ParsingError error : errorHandler.errors)
            {
                e.addParsingError(error);
            }

            throw e;
        }

        return doc;
    }

    /**
     * Implementation of SAX ErrorHandler used to handle errors during validation or parsing of a document.
     */
    private static class ValidationErrorHandler implements ErrorHandler
    {
        /** Flag used to indicate that errors have been found. */
        protected boolean errorFound = false;

        /** Used to accumulate the errors. */
        Collection<ParsingError> errors = new ArrayList<ParsingError>();

        /**
         * Called when the parser encounters a fatal error during parsing.
         *
         * @param ex The parser exception for the fatal error.
         */
        public void fatalError(SAXParseException ex)
        {
            errorFound = true;
            log.warn(ex);

            errors.add(new ParsingError(ex.getColumnNumber(), ex.getLineNumber(), ex.getMessage()));
        }

        /**
         * Called when the parser encounters an error during parsing.
         *
         * @param ex The parser exception for the error.
         */
        public void error(SAXParseException ex)
        {
            errorFound = true;
            log.debug(ex);

            errors.add(new ParsingError(ex.getColumnNumber(), ex.getLineNumber(), ex.getMessage()));
        }

        /**
         * Called when the parser encounters a warning during parsing.
         *
         * @param ex The parser exception for the warning.
         */
        public void warning(SAXParseException ex)
        {
            // Log the warning as a warning but don't register it as an error.
            log.debug(ex);
        }
    }
}
