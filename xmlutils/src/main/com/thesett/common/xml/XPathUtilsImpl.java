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

import java.util.Properties;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathFunctionResolver;
import javax.xml.xpath.XPathVariableResolver;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * XPathUtilsImpl provides an implementation of the {@link XPathUtils} xpath context interface that is built using the
 * default JAXP XPath evaluator set by the system property "javax.xml.xpath.XPathFactory".
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Evaluate XPath expressions to different data types.
 * <tr><td> Hold variables to pass to XPath queries.
 * <tr><td> Accept a node from a DOM as the root of a query.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class XPathUtilsImpl implements XPathUtils, XPathVariableResolver
{
    /** Used for logging. */
    private static final Logger log = Logger.getLogger(XPathUtils.class);

    /** The current root to query from. */
    private Node node = null;

    /** Holds the variables. */
    private Properties variables = new Properties();

    /** Holds a reference to the XPath factory. */
    XPathFactory xpFactory = XPathFactory.newInstance();

    /** Holds a reference to the namespace context. */
    NamespaceContext namespaceContext = null;

    /** Holds a reference to the function library. */
    XPathFunctionResolver functionResolver = null;

    /** Create a new XPath context. */
    public XPathUtilsImpl()
    {
        // Set this as the variable resolver on the XPath factory.
        xpFactory.setXPathVariableResolver(this);
    }

    /**
     * Evaluates an XPath query that expects to find a long as its result.
     *
     * @param  path The XPath.
     *
     * @return The long value found at the XPath location.
     */
    public long evalLong(String path)
    {
        return Long.parseLong(evalString(path));
    }

    /**
     * Evaluates an XPath query that expects to find a int as its result.
     *
     * @param  path The XPath.
     *
     * @return The int value found at the XPath location.
     */
    public int evalInt(String path)
    {
        return Integer.parseInt(evalString(path));
    }

    /**
     * Evaluates an XPath query that expects to find a double as its result.
     *
     * @param  path The XPath.
     *
     * @return The double value found at the XPath location.
     */
    public double evalDouble(String path)
    {
        return Double.parseDouble(evalString(path));
    }

    /**
     * Evaluates an XPath query that expects to find a boolean as its result.
     *
     * @param  path The XPath.
     *
     * @return The boolean value found at the XPath location.
     */
    public boolean evalBoolean(String path)
    {
        return Boolean.parseBoolean(evalString(path));
    }

    /**
     * Evaluates an XPath query that expects to find a string as its result.
     *
     * @param  path The XPath.
     *
     * @return The string value found at the XPath location.
     *
     * @throws IllegalArgumentException If the value at the XPath location is not, or cannot be converted, to a string,
     *                                  or the XPath expression is not valid.
     */
    public String evalString(String path) throws IllegalArgumentException
    {
        XPath xp = createXPath();

        try
        {
            return xp.evaluate(path, node);
        }
        catch (XPathExpressionException e)
        {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Evaluates an XPath query that expects to find a node list as its result.
     *
     * @param  path The XPath.
     *
     * @return The node list found at the XPath location.
     *
     * @throws IllegalArgumentException If the value at the XPath location is not a node list or the XPath expression is
     *                                  invalid.
     */
    public NodeList evalNodeList(String path) throws IllegalArgumentException
    {
        XPath xp = createXPath();

        try
        {
            return (NodeList) xp.evaluate(path, node, XPathConstants.NODESET);
        }
        catch (XPathExpressionException e)
        {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Evaluates an XPath query that expects to find a node list as its result.
     *
     * @param  path The XPath.
     *
     * @return The node list found at the XPath location.
     *
     * @throws IllegalArgumentException If the value at the XPath location is not a node list or the XPath expression is
     *                                  invalid.
     */
    public Node evalNode(String path) throws IllegalArgumentException
    {
        XPath xp = createXPath();

        try
        {
            return (Node) xp.evaluate(path, node, XPathConstants.NODE);
        }
        catch (XPathExpressionException e)
        {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Sets a DOM document as the root of the XML to be queried in this XPath context.
     *
     * @param d The document to query.
     */
    public void setDocument(Document d)
    {
        // Keep the document to query over.
        node = d;
    }

    /**
     * Sets a node from a DOM as the root of the XML to be queried in this XPath context.
     *
     * @param n The node to query.
     */
    public void setNode(Node n)
    {
        // Keep the node to query over.
        node = n;
    }

    /**
     * Sets a variable to add to the XPath context.
     *
     * @param name  The variable name.
     * @param value The value of the variable.
     */
    public void setVariable(String name, Object value)
    {
        // Keep the variable.
        variables.put(name, value);
    }

    /**
     * Provides the call back for the query evaluator to access the variables.
     *
     * @param  name The name of the variable to get.
     *
     * @return The variable from the variable set, or null if no matching name exists.
     */
    public Object resolveVariable(QName name)
    {
        log.debug("public Object resolveVariable(QName name): called");
        log.debug("name.getNamespaceURI() = " + name.getNamespaceURI());
        log.debug("name.getLocalPart() = " + name.getLocalPart());

        return variables.getProperty(name.getNamespaceURI() + name.getLocalPart());
    }

    /**
     * Sets a name space context to use with the XPaths to translate between namespace prefixes and full URIs.
     *
     * @param n The namespace context to use.
     */
    public void setNamespaceContext(NamespaceContext n)
    {
        namespaceContext = n;
    }

    /**
     * Sets a function resolver to add a function library to the XPaths handled by this evaluator.
     *
     * @param f The function library.
     */
    public void setXPathFunctionResolver(XPathFunctionResolver f)
    {
        functionResolver = f;
    }

    /**
     * Helper to crate the XPath and set up its resolvers and contexts.
     *
     * @return An XPath evaluator.
     */
    private XPath createXPath()
    {
        XPath xp = xpFactory.newXPath();

        // Set this as the variable resolver.
        xp.setXPathVariableResolver(this);

        // Set up the namespace context if one is defined.
        if (namespaceContext != null)
        {
            xp.setNamespaceContext(namespaceContext);
        }

        // Set up the function library if one is defined.
        if (functionResolver != null)
        {
            xp.setXPathFunctionResolver(functionResolver);
        }

        return xp;
    }
}
