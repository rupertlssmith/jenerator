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
package com.thesett.common.xml;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPathFunctionResolver;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * XPathUtils provides a context for executing XPath queries over XML data. The context consists of the document
 * or document fragment to execute queries over and a set of variables (string, object pairs).
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities
 * <tr><td> Evaluate XPath expressions to different data types.
 * <tr><td> Hold variables to pass to XPath queries.
 * <tr><td> Accept a node from a DOM as the root of a query.
 * </table></pre>
 *
 * @todo Add support for function libraries and namesspaces.
 *
 * @author Rupert Smith
 */
public interface XPathUtils
{
    /**
     * Evaluates an XPath query that expects to find a long as its result.
     *
     * @param path The XPath.
     *
     * @return The long value found at the XPath location.
     *
     * @throws IllegalArgumentException If the value at the XPath location is not, or cannot be converted, to a long.
     */
    long evalLong(String path) throws IllegalArgumentException;

    /**
     * Evaluates an XPath query that expects to find a int as its result.
     *
     * @param path The XPath.
     *
     * @return The int value found at the XPath location.
     *
     * @throws IllegalArgumentException If the value at the XPath location is not, or cannot be converted, to a int.
     */
    int evalInt(String path) throws IllegalArgumentException;

    /**
     * Evaluates an XPath query that expects to find a double as its result.
     *
     * @param path The XPath.
     *
     * @return The double value found at the XPath location.
     *
     * @throws IllegalArgumentException If the value at the XPath location is not, or cannot be converted, to a double.
     */
    double evalDouble(String path) throws IllegalArgumentException;

    /**
     * Evaluates an XPath query that expects to find a boolean as its result.
     *
     * @param path The XPath.
     *
     * @return The boolean value found at the XPath location.
     *
     * @throws IllegalArgumentException If the value at the XPath location is not, or cannot be converted, to a boolean.
     */
    boolean evalBoolean(String path) throws IllegalArgumentException;

    /**
     * Evaluates an XPath query that expects to find a string as its result.
     *
     * @param path The XPath.
     *
     * @return The string value found at the XPath location.
     *
     * @throws IllegalArgumentException If the value at the XPath location is not, or cannot be converted, to a string.
     */
    String evalString(String path) throws IllegalArgumentException;

    /**
     * Evaluates an XPath query that expects to find a node list as its result.
     *
     * @param path The XPath.
     *
     * @return The node list found at the XPath location.
     *
     * @throws IllegalArgumentException If the value at the XPath location is not a node list.
     */
    NodeList evalNodeList(String path) throws IllegalArgumentException;

    /**
     * Evaluates an XPath query that expects to find a node as its result.
     *
     * @param path The XPath.
     *
     * @return The node found at the XPath location.
     *
     * @throws IllegalArgumentException If the value at the XPath location is not a node.
     */
    Node evalNode(String path) throws IllegalArgumentException;

    /**
     * Sets a DOM document as the root of the XML to be queried in this XPath context.
     *
     * @param d The document to query.
     */
    void setDocument(Document d);

    /**
     * Sets a node from a DOM as the root of the XML to be queried in this XPath context.
     *
     * @param n The node to query.
     */
    void setNode(Node n);

    /**
     * Sets a variable to add to the XPath context.
     *
     * @param name  The variable name.
     * @param value The value of the variable.
     */
    void setVariable(String name, Object value);

    /**
     * Sets a name space context to use with the XPaths to translate between namespace prefixes and full URIs.
     *
     * @param n The namespace context to use.
     */
    void setNamespaceContext(NamespaceContext n);

    /**
     * Sets a function resolver to add a function library to the XPaths handled by this evaluator.
     *
     * @param f The function library.
     */
    void setXPathFunctionResolver(XPathFunctionResolver f);
}
