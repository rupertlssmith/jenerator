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

import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.antlr.stringtemplate.CommonGroupLoader;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.StringTemplateGroupLoader;
import org.antlr.stringtemplate.language.AngleBracketTemplateLexer;
import org.apache.log4j.Logger;

import com.thesett.aima.state.Type;
import com.thesett.aima.state.TypeVisitor;
import com.thesett.catalogue.interfaces.Catalogue;
import com.thesett.common.util.FileUtils;
import com.thesett.common.util.StringUtils;

/**
 * BaseGenerator is a {@link Generator} that uses 'stringtemplate' as a code generation engine. An interface for the
 * template is defined in 'Generator.sti'. The generation process consists of setting up a catalogue, a type, a type
 * decorator and a set of fields of the type, on a template and writing the output of the template to a file.
 *
 * <p/>The generation process is driven by querying the catalogue model for all top-level types defined in it, and
 * then applying this as a type visitor to those types. The default type visitor method is implemented by this base
 * class to do nothing. Concrete implementations can override the default type visitor method, or more specific type
 * visitor methods to selectively generate code for the top-level catalogue types that they need to generate code from.
 *
 * <p/>This generator base class implements optional pre and post processing on the entire generation process, and on
 * a per-type basis. The {@link #preApply(Catalogue)}, {@link #postApply(Catalogue)}, {@link #getPreVisitor()} and
 * {@link #getPostVisitor()} methods can be overridden by concrete implementation to provide pre and post processing
 * when it is required. As the {@link #getPreVisitor()} and {@link #getPostVisitor()} methods supply visitors over
 * types they can be made to select just some subset of types to apply processing to. The pre and post type processing
 * is applied to all types in a single step before and after the main processing, rather than doing pre, main and post
 * on each type in turn. This means that pre-methods can be used to gether information about all types, ahead of
 * the main processing, or output for all types after the main processing, if some sort of multiple phase processing
 * is required.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Load string templates from a known location.
 * <tr><td> Apply generation to every top-level type in the catalogue. <td> {@link TypeVisitor}.
 * <tr><td> Apply a sequence of templates to a cataloge mode, type, decorator and sequence of fields.
 * <tr><td> Apply optional pre and post processing to whole catalogue.
 * <tr><td> Apply optional pre and psot processing to every type in the catalogue.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public abstract class BaseGenerator implements Generator, TypeVisitor
{
    /** Used for debugging purposes. */
    public static final Logger log = Logger.getLogger(BaseGenerator.class);

    /** The default path to load templates from. */
    public static final String DEFAULT_TEMPLATE_PATH = "com/thesett/catalogue/generator/defaulttemplates";

    /** Defines the name of the template to create the opening section of output files. */
    protected static final String FILE_OPEN_TEMPLATE = "file_open";

    /** Defines the name of the catalogue component bean template. */
    protected static final String FOR_BEAN_TEMPLATE = "for_bean";

    /** Defines the name of the template to create the closing section of output files. */
    protected static final String FILE_CLOSE_TEMPLATE = "file_close";

    /** Defines the classpath relative path where the generation templates can be found. */
    private static final String TEMPLATES_PATH = "com/thesett/catalogue/generator";

    /** Holds the name of the root directory to output to. */
    protected String outputDirName;

    /** Holds the catalogue model to generate from. */
    protected Catalogue model;

    /** Used to keep track of output directories that have been created. */
    protected Set<String> createdOutputDirectories = new HashSet<String>();

    /**
     * Creates a stringtemplate generator to output to the specified directory root.
     *
     * @param outputDirName   The root directory to generate output to.
     */
    protected BaseGenerator(String outputDirName)
    {
        this.outputDirName = outputDirName;

        StringTemplateGroupLoader loader = new CommonGroupLoader(TEMPLATES_PATH, new DummyErrorHandler());
        StringTemplateGroup.registerGroupLoader(loader);
        StringTemplateGroup.registerDefaultLexer(AngleBracketTemplateLexer.class);
    }

    /** {@inheritDoc} */
    public Boolean apply(Catalogue catalogue)
    {
        // Keep the catalogue to generate from.
        this.model = catalogue;

        // Apply pre processing to the whole catalogue.
        preApply(catalogue);

        // Apply pre processing on a per type basis.
        TypeVisitor preVisitor = getPreVisitor();

        if (preVisitor != null)
        {
            for (Type type : catalogue.getAllTypes())
            {
                type.acceptVisitor(preVisitor);
            }
        }

        for (Type type : catalogue.getAllTypes())
        {
            type.acceptVisitor(this);

        }

        // Apply post processing on a per type basis.
        TypeVisitor postVisitor = getPostVisitor();

        if (postVisitor != null)
        {
            for (Type type : catalogue.getAllTypes())
            {
                type.acceptVisitor(postVisitor);
            }
        }

        // Apply post processing to the whole catalogue.
        postApply(catalogue);

        return true;
    }

    /**
     * {@inheritDoc}
     *
     * <p/>A default generator for a generic type. Does nothing.
     */
    public <T> void visit(Type<T> type)
    {
    }

    /**
     * Sub-classes can override this to return non-null, if they want to apply a pre generation method to every
     * type generated from, or <tt>null</tt> if they do not require a pre method.
     *
     * @return A type visitor to apply pre-generating from a type.
     */
    public TypeVisitor getPreVisitor()
    {
        return null;
    }

    /**
     * Sub-classes can override this to return non-null, if they want to apply a post generation method to every
     * type generated from, or <tt>null</tt> if they do not require a post method.
     *
     * @return A type visitor to apply post-generating from a type.
     */
    public TypeVisitor getPostVisitor()
    {
        return null;
    }

    /**
     * Called before applying type visitor to every type in the catalogue. Sub-classes may override to implement any
     * pre-generation functionality that they require.
     *
     * @param catalogue The catalogue model to generate from.
     */
    public void postApply(Catalogue catalogue)
    {
    }

    /**
     * Called after applying type visitor to every type in the catalogue. Sub-classes may override to implement any
     * post-generation functionality that they require.
     *
     * @param catalogue The catalogue model to generate from.
     */
    public void preApply(Catalogue catalogue)
    {
    }

    /**
     * Applies a sequence of templates to a catalogue model, type from the catalogue, and set of fields from the type,
     * and optioanlly an extra set of fields. These parameters are the parameters that the "for_bean" string template
     * function is exptecting and they are passed to such a template, and the results written to the specified file.
     *
     * @param model       The catalogue model.
     * @param type        The type to generate for.
     * @param templates   The sequence of templates to apply.
     * @param outputName  A sequence of named resources, such as files, to write the results to.
     * @param fields      A set of fields from the type to generate for.
     * @param extraFields A optional secondary set of fields from the type to generate for.
     * @param handler     A sequence of output handlers, to apply to the results.
     */
    protected void generate(Catalogue model, Type type, StringTemplateGroup[] templates, String[] outputName,
        Map<String, Type> fields, Map<String, Type> extraFields, ProcessedTemplateHandler[] handler)
    {
        for (int i = 0; i < templates.length; i++)
        {
            // Instantiate the template to generate from.
            StringTemplate stringTemplate = templates[i].getInstanceOf(FOR_BEAN_TEMPLATE);

            stringTemplate.setAttribute("decorator", type);
            stringTemplate.setAttribute("catalogue", model);
            stringTemplate.setAttribute("fields", fields);
            stringTemplate.setAttribute("extraFields", extraFields);

            handler[i].processed(stringTemplate, outputName[i]);
        }
    }

    /**
     * Converts a name to camel case and appends and prepends string onto it, then calculates the directory for the
     * model package relative to the generation output directory and returns the result as the full path name of the
     * file to output to, for Java code generation.
     *
     * @param prefix  The prefix to add to the java file name.
     * @param name    The name to convert to camel case as the main part of the java file name.
     * @param postfix The postfix to add to the java file name.
     *
     * @return The full path to the java source file to output to.
     */
    protected String nameToJavaFileName(String prefix, String name, String postfix)
    {
        // Work out the full path to the location to write to.
        String packagePath = model.getModelPackage().replace('.', '/');
        final String fullOutputDirName = outputDirName + File.separator + packagePath;

        // Ensure that the output directory exists for the location, if it has not already been created.
        if (!createdOutputDirectories.contains(fullOutputDirName))
        {
            File dir = new File(fullOutputDirName);
            dir.mkdirs();
            createdOutputDirectories.add(fullOutputDirName);
        }

        // Build the full path to the output file.
        return fullOutputDirName + File.separatorChar + prefix + StringUtils.toCamelCaseUpper(name) + postfix + ".java";
    }

    /**
     * ProcessedTemplateHandler is a call-back interface, that is used to call-back upon completion of every string
     * template, in order that output may be generated from it.
     *
     * <pre><p/><table id="crc"><caption>CRC Card</caption>
     * <tr><th> Responsibilities
     * <tr><td> Accept notification of the processing of a string template.
     * </table></pre>
     */
    protected interface ProcessedTemplateHandler
    {
        /**
         * Notified once processing of a template is complete.
         *
         * @param template   The completed template.
         * @param outputName The name of the output resource, such as a file.
         */
        public void processed(StringTemplate template, String outputName);
    }

    /**
     * FileOutputProcessedTemplateHandler is a processed template handler, that outputs its results to a file. The
     * handler can be created to append or overwrite files.
     */
    protected class FileOutputProcessedTemplateHandler implements ProcessedTemplateHandler
    {
        /** Flag used to indicate if the output file should be appended to. */
        private boolean append;

        /**
         * Creates a file output handler, that appends or overwrites files.
         *
         * @param append <tt>true</tt> to append to files.
         */
        public FileOutputProcessedTemplateHandler(boolean append)
        {
            this.append = append;
        }

        /** {@inheritDoc} */
        public void processed(StringTemplate template, String outputName)
        {
            FileUtils.writeObjectToFile(outputName, template, append);
        }
    }
}
