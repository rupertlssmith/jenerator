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
package com.thesett.javasource.generator;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Stack;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.xpath.XPathFunction;
import javax.xml.xpath.XPathFunctionException;
import javax.xml.xpath.XPathFunctionResolver;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.thesett.common.util.CommandLineParser;
import com.thesett.common.util.StringUtils;
import com.thesett.common.xml.XPathUtils;
import com.thesett.common.xml.XPathUtilsImpl;
import com.thesett.common.xml.XmlUtils;
import com.thesett.javasource.generator.model.CompilationUnitType;
import com.thesett.javasource.generator.model.DeclarationsType;
import com.thesett.javasource.generator.model.EmptyType;
import com.thesett.javasource.generator.model.ExprType;
import com.thesett.javasource.generator.model.ForType;
import com.thesett.javasource.generator.model.IfType;
import com.thesett.javasource.generator.model.JavaType;
import com.thesett.javasource.generator.model.Tl;
import com.thesett.javasource.generator.model.TlrootType;
import com.thesett.javasource.generator.model.ValueType;
import com.thesett.javasource.generator.model.VarType;

/**
 * SourceCodeGenerator transforms XML template files that conform to the template language schema (tl.xsd) into
 * Java source code. The template contains fragments of java code intermixed with control statements and expressions
 * over a model. The model must be expressed in XML and the control statements are given in terms of XPaths over that
 * model. The result of combining a model and a template is a text file, usually Java source code, but not necessarily.
 *
 * <p/>The generation process works by using two nested loops. The outer loop is over the model and the inner loop
 * is over the template. At the start, the root element of the model is taken as the outer loop (a loop of one element)
 * and the first level of nodes in the template as the inner loop. Control statements in the template may contain
 * child nodes and some constructs (such as for loops) can select multiple nodes from the model to loop over and
 * perform a repetitive generation from the template. In this sense, the model drives the generation process, supplying
 * the template with sequences of elements to transform into source code. This can be illustrated with a trivial
 * example:
 *
 * <p/>The model file:
 * <pre>
 * &lt;model&gt;
 *  &lt;item test="hello"/&gt;
 *  &lt;item test=" "/&gt;
 *  &lt;item test="world"/&gt;
 * &lt;/model&gt;
 * </pre>
 *
 * <p/>The template file:
 * <pre>
 * public class Test {
 *     public void main(String[] args)
 *     {
 *      &lt;for path="/model/item"&gt;
 *      System.out.print("&lt;value path="@test"/&gt;");
 *      &lt;/for&gt;
 *     }
 * }
 * </pre>
 *
 * <p/>The for loop selects a sequence of items from the model. The outer loop iterates over these, repeatedly processing
 * the template for each one.
 *
 * <p/>The elements in the template language are:
 *
 * <pre><p/><table><caption>Template Lanaguge</caption>
 * <tr><th> Element <th> Usage
 * <tr><td>
 * </table></pre>
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Transform a template and a data model into Java source code.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class SourceCodeGenerator
{
    /** Used for logging. */
    private static final Logger log = Logger.getLogger(SourceCodeGenerator.class);

    /** Flag used to signal that a for loop over the model is on its last iteration. */
    private boolean lastInFor = false;

    /**
     * Entry point. Specfy the command line options:
     *
     * <pre>
     *  -templatefile filename   The file containing the template.
     *  -modelfile    filename   The file containing the model.
     *  -dir          out dir    The directory to write the generated source code to.
     *  -package      my.package The Java package to output the generated source code to.
     * </pre>
     *
     * @param args The command line arguments.
     */
    public static void main(String[] args)
    {
        // Use the command line parser to evaluate the command line.
        CommandLineParser commandLine =
            new CommandLineParser(
                new String[][]
                {
                    { "templatefile", "The file containing the template.", "filename", "true" },
                    { "modelfile", "The file containing the XML model.", "filename", "true" },
                    { "dir", "Directory to output the generated files to.", "out dir", "true" },
                    { "package", "The Java package to output the generated model to.", "my.package", "true" },
                    { "v", "Verbose mode. Prints information about the processing as it goes." }
                });

        Properties options = null;

        try
        {
            options = commandLine.parseCommandLine(args);
        }
        catch (IllegalArgumentException e)
        {
            System.out.println(commandLine.getErrors());
            System.out.println(commandLine.getUsage());
            System.exit(0);
        }

        String outputDirectoryName = (String) options.get("dir");
        String templateFileName = (String) options.get("templatefile");
        String modelFileName = (String) options.get("modelfile");
        String packageName = (String) options.get("package");

        // Try to locate and open the template file.
        Reader templateFileReader = null;

        try
        {
            templateFileReader = new FileReader(templateFileName);
        }
        catch (FileNotFoundException e)
        {
            throw new RuntimeException("The specified file, " + options.get("templatefile") + ", cannot be opened.", e);
        }

        // Try to locate and open the model file.
        Reader modelFileReader = null;

        try
        {
            modelFileReader = new FileReader(modelFileName);
        }
        catch (FileNotFoundException e)
        {
            throw new RuntimeException("The specified file, " + options.get("modelfile") + ", cannot be opened.", e);
        }

        generateFromReaders(templateFileReader, modelFileReader, packageName, outputDirectoryName);
    }

    /**
     * Generates source code given readers for the template and model, an output package name, and an output directory
     * name.
     *
     * @param templateFileReader  A reader for the template.
     * @param modelFileReader     A reader for the model.
     * @param packageName         The output package name.
     * @param outputDirectoryName The output directory name.
     */
    public static void generateFromReaders(Reader templateFileReader, Reader modelFileReader, String packageName,
        String outputDirectoryName)
    {
        // Load and validate the template.
        Tl template = null;

        try
        {
            // Open the specified resource and unmarshal the template from it.
            JAXBContext jc = JAXBContext.newInstance("com.thesett.javasource.generator.model");
            Unmarshaller u = jc.createUnmarshaller();
            template = (Tl) u.unmarshal(templateFileReader);
        }
        catch (JAXBException e)
        {
            throw new RuntimeException("The template cannot be unmarshalled.", e);
        }

        // Load, validate and parse the model XML file.
        Document model = null;

        try
        {
            model = XmlUtils.load(modelFileReader);
        }
        catch (IOException e)
        {
            throw new RuntimeException("The model file cannot be read.", e);
        }

        // Check if the output directory exists and create it if not.

        // Create a code generator and run the bean generation process.
        SourceCodeGenerator generator = new SourceCodeGenerator();

        try
        {
            // Set up the source code writer to output to the specified directory.
            SourceCodeWriter out = new SourceCodeWriter(outputDirectoryName);

            // Set up package name as a variable to pass to the generator.
            Map<String, String> variables = new HashMap<String, String>();
            variables.put("packagename", packageName);

            generator.generate(template, model, out, variables);
            out.flush();
        }
        catch (IOException e)
        {
            throw new RuntimeException("There was an i/o exception during writing of the source code.", e);
        }
    }

    /**
     * Performs source code generation from a template and an XML document model.
     *
     * <p/>This implementation of the generation process has been written with an explicit stack and an iterative
     * style, rather than using recursive calls, because the Java stack is limited in size and the language is
     * not ideally designed for recursion.
     *
     * <p/>There are two nested loops as explained in the class comment. The stack loop forms a third outer loop to
     * these. Whenever a template element with child nodes is encountered a new stack frame is generated, the
     * current context is saved on the stack, the new stack frame is also placed on the stack, the two nested
     * loops are suspended, processing breaks to the outer loop which pops the new context from the stack and
     * processes it. Generation continues until the stack is empty.
     *
     * @param template  The source code template.
     * @param model     The XML model data source to generate from.
     * @param out       The writer to send the generated file to.
     * @param variables The variables to pass to the generation process.
     *
     * @throws IOException If there is an i/o exception on the writer whilst writing the source code out.
     */
    public void generate(Tl template, Document model, SourceCodeWriter out, Map<String, String> variables)
        throws IOException
    {
        // Create a context stack to hold the current generation context.
        Stack<GenerationContext> genContexts = new Stack<GenerationContext>();

        // Create the initial model node to iterate over.
        List<Node> modelNodes = new ArrayList<Node>();
        modelNodes.add(model);

        // Create the initial template node list to iterate over.
        List<Serializable> templateNodes = template.getContent();

        // Push the intial state onto the generation context stack.
        GenerationContext startState = new GenerationContext(modelNodes, templateNodes);
        genContexts.push(startState);

        // Loop until the context stack is exhausted.
        while (!genContexts.empty())
        {
            log.debug("Context stack contains " + genContexts.size() + " elements.");

            // There are still frames on the context stack, get the next one.
            GenerationContext currentContext = genContexts.pop();
            log.debug("Popped context off the stack: " + currentContext);

            // Loop over the model context.
            while (currentContext.modelPos < currentContext.modelNodes.size())
            {
                // A flag used to indicate that the inner loop has pushed a new context onto the stack and is
                // jumping to the outermost loop to generate from that context.
                boolean jumpToOuter = false;

                // Get the next node from the model.
                Node nextModelNode = currentContext.modelNodes.get(currentContext.modelPos);
                log.debug("Got next model node: " + nextModelNode.getNodeName());

                // Set up the last in for flag based on the current position in the model context.
                lastInFor = (currentContext.modelPos == (currentContext.modelNodes.size() - 1));

                // Generate an XPath querying utility for this point in the model.
                XPathUtils xpEvaluatorForModel = new XPathUtilsImpl();
                xpEvaluatorForModel.setNode(nextModelNode);
                xpEvaluatorForModel.setXPathFunctionResolver(new GenerationFunctions());
                xpEvaluatorForModel.setNamespaceContext(new ModelNamespace());

                for (String key : variables.keySet())
                {
                    xpEvaluatorForModel.setVariable(key, variables.get(key));
                }

                // Loop over the contents of the template to be generated with respect to the current model context.
                while (currentContext.templatePos < currentContext.templateNodes.size())
                {
                    // Get the next node from the template.
                    Object nextTemplateNode = currentContext.templateNodes.get(currentContext.templatePos);
                    log.debug("Got next template node.");

                    // Advance the current template context now that the next template node has been examined.
                    currentContext.templatePos++;
                    log.debug("Advanced template position to: " + (currentContext.templatePos + 1) + " out of " +
                        currentContext.templateNodes.size());

                    // If the next node in the template is an element, process it as a template instruction.
                    if (nextTemplateNode instanceof JAXBElement)
                    {
                        Object nextTemplateElement = ((JAXBElement) nextTemplateNode).getValue();
                        log.debug("Got JAXBElement: " + ((JAXBElement) nextTemplateNode).getName());

                        if (nextTemplateElement instanceof ForType)
                        {
                            ForType forType = (ForType) nextTemplateElement;
                            log.debug("Got ForType.");

                            // Get the XPath of the for loop.
                            String path = forType.getPath();
                            log.debug("path = " + path);

                            // Evaluate the XPath to get a list of nodes to loop over.
                            NodeList nodes = xpEvaluatorForModel.evalNodeList(path);
                            log.debug("XPath matched " + nodes.getLength() + " nodes.");

                            // Extract a list of model nodes to loop over.
                            List<Node> newModelNodes = new ArrayList<Node>();
                            int nodesLength = nodes.getLength();

                            for (int i = 0; i < nodesLength; i++)
                            {
                                Node nextNode = nodes.item(i);
                                log.debug("Got model node: " + nextNode.getNodeName());

                                newModelNodes.add(nextNode);
                            }

                            // Extract the list of child nodes of the for loop in the template.
                            List<Serializable> newTemplateNodes = forType.getContent();

                            // Create a new generation context for the child nodes of the for loop in the template
                            // and the set of model nodes to loop over.
                            GenerationContext newContext = new GenerationContext(newModelNodes, newTemplateNodes);

                            // Put the current generation context back on the stack to continue it later.
                            genContexts.push(currentContext);
                            log.debug("Pushed current context back onto the stack to continue later.");

                            // Push the new generation context and jump to the outermost loop to generate from it.
                            genContexts.push(newContext);
                            jumpToOuter = true;
                            log.debug("Pushed new context onto the stack, jumping to outer loop.");

                            break;
                        }
                        else if (nextTemplateElement instanceof DeclarationsType)
                        {
                            log.debug("Got DeclarationsType.");
                        }
                        else if (nextTemplateElement instanceof EmptyType)
                        {
                            log.debug("Got EmptyType.");
                        }
                        else if (nextTemplateElement instanceof ExprType)
                        {
                            log.debug("Got ExprType.");
                        }
                        else if (nextTemplateElement instanceof IfType)
                        {
                            IfType ifType = (IfType) nextTemplateElement;
                            log.debug("Got IfType.");

                            // Get the XPath to extract the conditional from.
                            String path = ifType.getPath();
                            log.debug("path = " + path);

                            // Evaluate the conditional.
                            boolean condition = xpEvaluatorForModel.evalBoolean(path);
                            log.debug("condition = " + condition);

                            // Check if the condition was met and only process child nodes if it was.
                            if (condition)
                            {
                                // Create a new template context on the stack for the child nodes.
                                establishNonLoopingContext(nextModelNode, ifType, genContexts, currentContext);

                                // Continue in the new context at the outer loop.
                                jumpToOuter = true;

                                break;
                            }
                        }
                        else if (nextTemplateElement instanceof JavaType)
                        {
                            log.debug("Got JavaType.");
                        }
                        else if (nextTemplateElement instanceof ValueType)
                        {
                            ValueType valueType = (ValueType) nextTemplateElement;
                            log.debug("Got ValueType.");

                            // Get the XPath to extract the value from.
                            String path = valueType.getPath();
                            log.debug("path = " + path);

                            // Evaluate the XPath to get the value.
                            String value = xpEvaluatorForModel.evalString(path);
                            log.debug("value = " + value);

                            // Print it.
                            out.write(value);
                        }
                        else if (nextTemplateElement instanceof CompilationUnitType)
                        {
                            CompilationUnitType unitType = (CompilationUnitType) nextTemplateElement;
                            log.debug("Got CompilationUnitType.");

                            String packageName = xpEvaluatorForModel.evalString(unitType.getPackage());
                            String fileName = xpEvaluatorForModel.evalString(unitType.getFileName());
                            log.debug("packageName = " + packageName);
                            log.debug("fileName = " + fileName);

                            // Set up the new file as the output location on the source writer.
                            out.setCompilationUnit(packageName, fileName);

                            // Create a new template context on the stack for the child nodes.
                            establishNonLoopingContext(nextModelNode, unitType, genContexts, currentContext);

                            // Continue in the new context at the outer loop.
                            jumpToOuter = true;

                            break;
                        }
                        else if (nextTemplateElement instanceof VarType)
                        {
                            VarType varType = (VarType) nextTemplateElement;
                            log.debug("Got VarType.");

                            // Extract the variable name and value path expression.
                            String name = varType.getName();
                            String value = varType.getValue();
                            log.debug("name = " + name);
                            log.debug("value = " + value);

                            // Evaluate the XPath to get the true value.
                            String trueValue = xpEvaluatorForModel.evalString(value);
                            log.debug("trueValue = " + trueValue);

                            // Set the name/value pair on the xpath evaluator for future use.
                            xpEvaluatorForModel.setVariable(name, trueValue);
                            variables.put(name, trueValue);
                        }
                    }

                    // The next value in the template is not an element, so must be text to be output just as it appears.
                    else
                    {
                        log.debug("Got object: " + nextTemplateNode);
                        out.write(nextTemplateNode.toString());
                    }
                }

                // Check if the inner loop was suspended because of a change in context and jump to the outermost
                // loop if so.
                if (jumpToOuter)
                {
                    log.debug("Jump to outer flag is set, suspending current model context loop.");

                    break;
                }

                // Advance the current model context now that the next model node has been examined and all template
                // nodes generated from it have been completed.
                currentContext.modelPos++;
                log.debug("Advanced model position to " + (currentContext.modelPos + 1) + " out of " +
                    currentContext.modelNodes.size());

                // Check if there are more nodes in the model to generate from, and reset the position within the
                // current template context ready for the next model node if so.
                if (currentContext.modelPos < currentContext.modelNodes.size())
                {
                    log.debug("Inner loop completed, reseting template context for next model node.");

                    currentContext.templatePos = 0;
                }
            }
        }
    }

    /**
     * Helper method for non-looping template elements with child nodes. Saves the current generation context onto
     * the stack and establishes a new context for the child template nodes and the current model node, also placed
     * onto the stack.
     *
     * @param modelNode      The current model node to establish the new context with.
     * @param element        The non-looping element, the child nodes of which to create a new context for.
     * @param genContexts    The context stack.
     * @param currentContext The current context to be saved, for later continuation.
     */
    private void establishNonLoopingContext(Node modelNode, TlrootType element, Stack<GenerationContext> genContexts,
        GenerationContext currentContext)
    {
        // The child nodes of the non looping element are only to be generated once for
        // the current generation context. If the non looping element is inside a looping element
        // over multiple model contexts, then it will be re-evaluated for each model element in
        // turn. Only add the current model node to the context to evaluate the child nodes in.
        List<Node> newModelNodes = new ArrayList<Node>();
        newModelNodes.add(modelNode);

        // Extract the list of child nodes of the non-looping element in the template.
        List<Serializable> newTemplateNodes = element.getContent();

        // Create a new generation context for the child nodes of the condition in the template
        // and the current set of model nodes to loop over.
        GenerationContext newContext = new GenerationContext(newModelNodes, newTemplateNodes);

        // Put the current generation context back on the stack to continue it later.
        genContexts.push(currentContext);
        log.debug("Pushed current context back onto the stack to continue later.");

        // Push the new generation context and jump to the outermost loop to generate from it.
        genContexts.push(newContext);
        log.debug("Pushed new context onto the stack, jumping to outer loop.");
    }

    /**
     * Holds the current source code generation context. A new context is generated every time an element in the
     * source code template is encountered that contains sub-nodes. Sometimes a sub-context in the template will
     * also have a sub-context in the model associated with it. This happens where a control statement in the template
     * selects a context path within the model; this model context is passed down to all child nodes in the template.
     * If no sub-context in the model is selected then the parent one is used.
     *
     * <p/>The context maintains two indexes, one into the template context and one into the model context. As items
     * from these contexts are processed, the indexes must be incremented. When a context is suspended onto the stack
     * for later continuation, the indexes are preserved so that processing can continue where it was left off once
     * the context is restored.
     */
    public static class GenerationContext
    {
        /**
         * The current list of model nodes being generated from. For most model contexts this will just be a single
         * node, for looping constructs it will be all the nodes matched to perform a repetitive generation over.
         */
        public List<Node> modelNodes;

        /** Holds the current position in the model context. */
        public int modelPos = 0;

        /**
         * The current list of template nodes being generated. This is the inner generation loop and holds the context
         * of the part of the template that is currently being generated from.
         */
        public List<Serializable> templateNodes;

        /** Holds the current position in the template context. */
        public int templatePos = 0;

        /**
         * Creates a code generation context for a list of model nodes and template nodes.
         *
         * @param modelNodes    The model nodes.
         * @param templateNodes The template nodes.
         */
        public GenerationContext(List<Node> modelNodes, List<Serializable> templateNodes)
        {
            // Keep the model and template nodes.
            this.modelNodes = modelNodes;
            this.templateNodes = templateNodes;
        }

        /**
         * Prints the context as a string for debugging purposes.
         *
         * @return The context as a string for debugging purposes.
         */
        public String toString()
        {
            return "current model context = " +
                ((modelPos < modelNodes.size()) ? modelNodes.get(modelPos).getNodeName() : "exhausted") +
                ", current template context = " +
                ((templatePos < templateNodes.size()) ? templateNodes.get(templatePos) : "exhausted");
        }
    }

    /**
     * Provides a library of additional functions that can form part of the XPath expressions used in the code templates.
     */
    public class GenerationFunctions implements XPathFunctionResolver
    {
        /** The URI of this function library. */
        public static final String URI = "http://thebadgerset.co.uk/source-code-generator-0.1";

        /** The prefix name of this function library. */
        public static final String PREFIX = "gen";

        /** Holds the function library by name. */
        private Map<String, XPathFunction> functions = new HashMap<String, XPathFunction>();

        /**
         * Builds the library of generator functions.
         */
        public GenerationFunctions()
        {
            // Create the function library.
            functions.put("notLastInFor", new NotLastInFor());
            functions.put("toCamelCaseUpper", new ToCamelCaseUpper());
            functions.put("toCamelCase", new ToCamelCase());
        }

        /**
         * Returns generator functions from this libraries namespace.
         *
         * @param functionName The name of the function to get.
         * @param arity        The number of arguments it takes.
         *
         * @return <tt>null</tt> if no matching function is found, otherwise the matching function.
         */
        public XPathFunction resolveFunction(QName functionName, int arity)
        {
            //log.debug("public XPathFunction resolveFunction(QName functionName, int arity): called");
            //log.debug("functionName.getNamespaceURI() = " + functionName.getNamespaceURI());
            //log.debug("functionName.getPrefix() = " + functionName.getPrefix());
            //log.debug("functionName.getLocalPart() = " + functionName.getLocalPart());
            //log.debug("arity = " + arity);

            String function = functionName.getLocalPart();
            //log.debug("function = " + function);

            return functions.get(function);
        }

        /**
         * This function examines the {@link SourceCodeGenerator#lastInFor} flag, exposed on the generator class,
         * to check if a currently executing for loop over the model is on its last iteration. This is usefull when
         * outputing delimeters (such as commas) between elements in a sequence, where no delimiter is to be output
         * after the last element in a sequence. The function takes no arguments, and can only be true within
         * a for loop.
         */
        public class NotLastInFor implements XPathFunction
        {
            /** Used for logging. */
            //private final Logger log = Logger.getLogger(NotLastInFor.class);

            /**
             * Determines whether or not the model context is currently looping over the final node of a sequence.
             *
             * @param args Does not accept any arguments, ignored.
             *
             * @return True only if the model context is currently looping over the final node of a sequence.
             */
            public Object evaluate(List args)
            {
                //log.debug("public Object evaluate(List args): called");

                return !lastInFor;
            }
        }
    }

    /**
     * This function translates arbitrary names into Java class names. The first letter is capitalized, any '_'
     * characters are removed and the character immediately after them is capitalized to translate the name into
     * camel case.
     */
    public static class ToCamelCaseUpper implements XPathFunction
    {
        /**
         * Turns an arbitrary name into a Java class name.
         *
         * @param args Accepts one string argument.
         *
         * @return The argument translated into a Java class name.
         *
         * @throws XPathFunctionException If the argument is not a string, or if it cannot be translated into
         *                                a legal Java class name.
         */
        public Object evaluate(List args) throws XPathFunctionException
        {
            // Get the first argument.
            return StringUtils.toCamelCaseUpper((String) args.get(0));
        }
    }

    /**
     * This function translates arbitrary names into Java field names. Any '_' characters are removed and the character
     * immediately after them is capitalized to translate the name into camel case.
     */
    public static class ToCamelCase implements XPathFunction
    {
        /**
         * Turns an arbitrary name into a Java class name.
         *
         * @param args Accepts one string argument.
         *
         * @return The argument translated into a Java class name.
         *
         * @throws XPathFunctionException If the argument is not a string, or if it cannot be translated into
         *                                a legal Java class name.
         */
        public Object evaluate(List args) throws XPathFunctionException
        {
            // Get the first argument.
            return StringUtils.toCamelCase((String) args.get(0));
        }
    }

    /**
     * Handles namespace mapping for the XML model for the XPath evaluator.
     *
     * @todo This is hard coded for the test model. Need to get namespace mapping from the model when it is loaded.
     */
    public static class ModelNamespace implements NamespaceContext
    {
        /**
         * Get Namespace URI bound to a prefix in the current scope.
         *
         * When requesting a Namespace URI by prefix, the following table describes the returned Namespace URI value
         * for all possible prefix values:
         *
         * <pre><p/><table><caption>getNamespaceURI(prefix) return value for specified prefixes</caption>
         * <tr><th> prefix parameter <th> Namespace URI return value
         * <tr><td> DEFAULT_NS_PREFIX ("")
         *     <td> default Namespace URI in the current scope or XMLConstants.NULL_NS_URI("") when there is no
         *          default Namespace URI in the current scope
         * <tr><td> bound prefix <td> Namespace URI bound to prefix in current scope
         * <tr><td> unbound prefix <td> XMLConstants.NULL_NS_URI("")
         * <tr><td> XMLConstants.XML_NS_PREFIX ("xml")
         *     <td> XMLConstants.XML_NS_URI ("http://www.w3.org/XML/1998/namespace")
         * <tr><td> XMLConstants.XMLNS_ATTRIBUTE ("xmlns")
         *     <td> XMLConstants.XMLNS_ATTRIBUTE_NS_URI ("http://www.w3.org/2000/xmlns/")
         * <tr><td> null <td> IllegalArgumentException is thrown
         * </table></pre>
         *
         * @param prefix The namespace prefix to get the URI for.
         *
         * @return The URI for the prefix as specified in the table above.
         */
        public String getNamespaceURI(String prefix)
        {
            log.debug("public String getNamespaceURI(String prefix): called");
            log.debug("prefix = " + prefix);

            if (prefix == null)
            {
                throw new IllegalArgumentException("The prefix was null.");
            }
            else if (prefix.equals(XMLConstants.DEFAULT_NS_PREFIX))
            {
                return XMLConstants.NULL_NS_URI;
            }
            else if (prefix.equals(XMLConstants.XML_NS_PREFIX))
            {
                return XMLConstants.XML_NS_URI;
            }
            else if (prefix.equals(XMLConstants.XMLNS_ATTRIBUTE))
            {
                return XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
            }
            else if ("cat".equals(prefix))
            {
                return "http://thebadgerset.co.uk/catalogue-def-0.1";
            }
            else if ("gen".equals(prefix))
            {
                return "http://thebadgerset.co.uk/source-code-generator-0.1";
            }
            else
            {
                return null;
            }
        }

        /**
         * Get prefix bound to Namespace URI in the current scope.
         *
         * When requesting a prefix by Namespace URI, the following table describes the returned prefix value for all
         * Namespace URI values:
         *
         * <pre><p/><table><caption>getPrefix(namespaceURI) return value for specified Namespace URIs</caption>
         * <tr><th> Namespace URI parameter <th> prefix value returned
         * <tr><td> &lt;default Namespace URI&gt; <td> XMLConstants.DEFAULT_NS_PREFIX ("")
         * <tr><td> bound Namespace URI
         *     <td> prefix bound to Namespace URI in the current scope, if multiple prefixes
         *          are bound to the Namespace URI in the current scope, a single arbitrary prefix, whose choice is
         *          implementation dependent, is returned
         * <tr><td> unbound Namespace URI <td> null
         * <tr><td> XMLConstants.XML_NS_URI ("http://www.w3.org/XML/1998/namespace")
         *     <td> XMLConstants.XML_NS_PREFIX ("xml")
         * <tr><td> XMLConstants.XMLNS_ATTRIBUTE_NS_URI ("http://www.w3.org/2000/xmlns/")
         *     <td> XMLConstants.XMLNS_ATTRIBUTE ("xmlns")
         * <tr><td> null <td> IllegalArgumentException is thrown
         * </table></pre>
         *
         * @param namespaceURI The URI to get a prefix for.
         *
         * @return The prefix.
         */
        public String getPrefix(String namespaceURI)
        {
            log.debug("public String getPrefix(String namespaceURI): called");
            log.debug("namespaceURI" + namespaceURI);

            if (namespaceURI == null)
            {
                throw new IllegalArgumentException("The URI was null.");
            }
            else if ("http://thebadgerset.co.uk/catalogue-def-0.1".equals(namespaceURI))
            {
                return XMLConstants.DEFAULT_NS_PREFIX;
            }
            else if ("http://thebadgerset.co.uk/source-code-generator-0.1".equals(namespaceURI))
            {
                return "gen";
            }
            else if (namespaceURI.equals(XMLConstants.XML_NS_URI))
            {
                return XMLConstants.XML_NS_PREFIX;
            }
            else if (namespaceURI.equals(XMLConstants.XMLNS_ATTRIBUTE_NS_URI))
            {
                return XMLConstants.XMLNS_ATTRIBUTE;
            }

            /*else if (namespaceURI.equals("http://thebadgerset.co.uk/catalogue-def-0.1"))
            {
                return "cat";
            }*/
            else
            {
                return null;
            }
        }

        /**
         * Get all prefixes bound to a Namespace URI in the current scope.
         *
         * @param namespaceURI The URI to get prefixes for.
         *
         * @return An iterator over all prefixes for the URI.
         */
        public Iterator getPrefixes(String namespaceURI)
        {
            List<String> result = new ArrayList<String>();
            result.add(getPrefix(namespaceURI));

            return result.iterator();
        }
    }
}
