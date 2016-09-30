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

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import org.apache.log4j.Logger;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;

import com.thesett.aima.state.Type;
import com.thesett.aima.state.TypeVisitor;
import com.thesett.aima.state.impl.ExtendableBeanState;
import com.thesett.catalogue.model.Catalogue;
import com.thesett.common.util.FileUtils;
import com.thesett.common.util.StringUtils;

/**
 * BaseGenerator is a {@link Generator} that uses 'stringtemplate' as a code generation engine. An interface for the
 * template is defined in 'Generator.sti'. The generation process consists of setting up a catalogue, a type, a type
 * decorator and a set of fields of the type, on a template and writing the output of the template to a file.
 *
 * <p/>The generation process is driven by querying the catalogue model for all top-level types defined in it, and then
 * applying this as a type visitor to those types. The default type visitor method is implemented by this base class to
 * do nothing. Concrete implementations can override the default type visitor method, or more specific type visitor
 * methods to selectively generate code for the top-level catalogue types that they need to generate code from.
 *
 * <p/>This generator base class implements optional pre and post processing on the entire generation process, and on a
 * per-type basis. The {@link #preApply(Catalogue)}, {@link #postApply(Catalogue)}, {@link #getPreVisitor()} and
 * {@link #getPostVisitor()} methods can be overridden by concrete implementation to provide pre and post processing
 * when it is required. As the {@link #getPreVisitor()} and {@link #getPostVisitor()} methods supply visitors over types
 * they can be made to select just some subset of types to apply processing to. The pre and post type processing is
 * applied to all types in a single step before and after the main processing, rather than doing pre, main and post on
 * each type in turn. This means that pre-methods can be used to gether information about all types, ahead of the main
 * processing, or output for all types after the main processing, if some sort of multiple phase processing is required.
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
public abstract class BaseGenerator extends ExtendableBeanState implements Generator, TypeVisitor
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

    /** Holds the catalogue model to generate from. */
    protected Catalogue model;

    /** The directory to output the model to. */
    protected String outputDir;

    /** The Java package name to generate to. */
    protected String outputPackage;

    /** Used to keep track of output directories that have been created. */
    protected Collection<String> createdOutputDirectories = new HashSet<String>();

    protected String templateRootPath;

    /** Holds a file output handler that overwrites files. */
    protected RenderTemplateHandler fileOutputHandlerOverwrite = new FileOutputRenderTemplateHandler(false, true);

    /** Holds a file output handler that appends to files. */
    protected RenderTemplateHandler fileOutputHandlerAppend = new FileOutputRenderTemplateHandler(true, true);

    /** Creates a StringTemplate generator. */
    protected BaseGenerator(String templateDir)
    {
        registerTemplateLoader(templateDir);
    }

    /**
     * Establishes the main output directory.
     *
     * @param outputDir The root directory to generate model output to.
     */
    public void setOutputDir(String outputDir)
    {
        this.outputDir = outputDir;
    }

    /**
     * Establishes the Java package name to generate to.
     *
     * @param outputPackage The Java package name to generate to.
     */
    public void setOutputPackage(String outputPackage)
    {
        this.outputPackage = outputPackage;
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
     * Sub-classes can override this to return non-null, if they want to apply a pre generation method to every type
     * generated from, or <tt>null</tt> if they do not require a pre method.
     *
     * @return A type visitor to apply pre-generating from a type.
     */
    public TypeVisitor getPreVisitor()
    {
        return null;
    }

    /**
     * Sub-classes can override this to return non-null, if they want to apply a post generation method to every type
     * generated from, or <tt>null</tt> if they do not require a post method.
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
     * and optionally an extra set of fields. These parameters are the parameters that the "for_bean" string template
     * function is expecting and they are passed to such a template, and the results written to the specified handler.
     *
     * <p/>Applies the default "for_bean" template.
     *
     * @param model       The catalogue model.
     * @param type        The type to generate for.
     * @param templates   The sequence of templates to apply.
     * @param outputName  A sequence of named resources, such as files, to write the results to.
     * @param fields      A set of fields from the type to generate for.
     * @param extraFields A optional secondary set of fields from the type to generate for.
     * @param handler     A sequence of output handlers, to apply to the results.
     */
    protected void generate(Catalogue model, Type type, STGroup[] templates, String[] outputName,
        Map<String, Type> fields, Map<String, Type> extraFields, RenderTemplateHandler[] handler)
    {
        generate(model, type, templates, outputName, fields, extraFields, handler, FOR_BEAN_TEMPLATE);
    }

    /**
     * Applies a sequence of templates to a catalogue model, type from the catalogue, and set of fields from the type,
     * and optionally an extra set of fields. These parameters are the parameters that the string template function is
     * expecting and they are passed to such a template, and the results written to the specified handler.
     *
     * @param model        The catalogue model.
     * @param type         The type to generate for.
     * @param templates    The sequence of templates to apply.
     * @param outputName   A sequence of named resources, such as files, to write the results to.
     * @param fields       A set of fields from the type to generate for.
     * @param extraFields  A optional secondary set of fields from the type to generate for.
     * @param handler      A sequence of output handlers, to apply to the results.
     * @param templateName The name of the template to apply.
     */
    protected void generate(Catalogue model, Type type, STGroup[] templates, String[] outputName,
        Map<String, Type> fields, Map<String, Type> extraFields, RenderTemplateHandler[] handler, String templateName)
    {
        for (int i = 0; i < templates.length; i++)
        {
            // Instantiate the template to generate from.
            ST stringTemplate = templates[i].getInstanceOf(templateName);

            stringTemplate.add("decorator", type);
            stringTemplate.add("catalogue", model);
            stringTemplate.add("package", outputPackage);
            stringTemplate.add("fields", fields);
            stringTemplate.add("extraFields", extraFields);

            handler[i].render(stringTemplate, outputName[i]);
        }
    }

    /**
     * Converts a name to camel case and appends and prepends strings onto it, then calculates the directory for the
     * model package relative to the generation output directory and returns the result as the full path name of the
     * file to output to, for Java code generation.
     *
     * @param  rootDirName The root directory to output to, package directory will come under this.
     * @param  prefix      The prefix to add to the java file name.
     * @param  name        The name to convert to camel case as the main part of the java file name.
     * @param  postfix     The postfix to add to the java file name.
     *
     * @return The full path to the java source file to output to.
     */
    protected String nameToJavaFileName(String rootDirName, String packageName, String prefix, String name,
        String postfix)
    {
        return nameToFileName(rootDirName, packageName, prefix, name, postfix, ".java");
    }

    /**
     * Converts a name to camel case and appends and prepends strings onto it, then calculates the directory for the
     * model package relative to the generation output directory and returns the result as the full path name of the
     * file to output to, for code generation.
     *
     * @param  rootDirName The root directory to output to, package directory will come under this.
     * @param  packageName The package name, dot separated, will be converted to a directory.
     * @param  prefix      The prefix to add to the java file name.
     * @param  name        The name to convert to camel case as the main part of the java file name.
     * @param  postfix     The postfix to add to the java file name.
     * @param  extension   The file output extension.
     *
     * @return The full path to the java source file to output to.
     */
    protected String nameToFileName(String rootDirName, String packageName, String prefix, String name,
                                    String postfix, String extension)
    {
        // Work out the full path to the location to write to.
        String packagePath = packageName.replace('.', '/');

        String fullOutputDirName = rootDirName + File.separator + packagePath;

        // Ensure that the output directory exists for the location, if it has not already been created.
        if (!createdOutputDirectories.contains(fullOutputDirName))
        {
            File dir = new File(fullOutputDirName);
            dir.mkdirs();
            createdOutputDirectories.add(fullOutputDirName);
        }

        // Build the full path to the output file.
        return fullOutputDirName + File.separatorChar + prefix + StringUtils.toCamelCaseUpper(name) + postfix + extension;
    }

    /**
     * Converts a name to camel case and appends and prepends strings onto it, then calculates the directory for the
     * model package relative to the generation output directory and returns the result as the full path name of the
     * file to output to, for Java code generation.
     *
     * @param  rootDirName The root directory to output to, package directory will come under this.
     * @param  prefix      The prefix to add to the java file name.
     * @param  name        The name to convert to camel case as the main part of the java file name.
     * @param  postfix     The postfix to add to the java file name.
     *
     * @return The full path to the java source file to output to.
     */
    protected String nameToJavaFileName(String rootDirName, String prefix, String name, String postfix)
    {
        // Work out the full path to the location to write to.
        String packageName = (outputPackage != null) ? outputPackage : model.getModelPackage();

        return nameToJavaFileName(rootDirName, packageName, prefix, name, postfix);
    }

    /**
     * Registers a StringTemplate group loader for templates. This will either be the default set of templates, when no
     * override is specified, or a set of custom templates when one is.
     *
     * @param templateDir The path of a directory holding customer templates, or <tt>null</tt> to use the defaults.
     */
    protected void registerTemplateLoader(String templateDir)
    {
        if ((templateDir == null) || "".equals(templateDir))
        {
            //loaderGroup = new STGroupDir(DEFAULT_TEMPLATE_PATH);
            this.templateRootPath = DEFAULT_TEMPLATE_PATH;
        }
        else
        {
            File f = new File(templateDir);

            if (!f.exists() || !f.isDirectory())
            {
                throw new IllegalStateException(
                    "'templateDir' must be a valid path to a directory containing templates.");
            }

            //loaderGroup = new STGroupDir(templateDir);
            this.templateRootPath = templateDir;
        }

        //STGroup.registerGroupLoader(groupLoader);
    }

    /**
     * Converts a name to the name of a file in the root output directory for the generation, and ensures that that
     * directory exists if it has not already been created.
     *
     * @param  name    The name to convert to a path to a file in the root generator output directory.
     * @param  dirName The name of the directory to create the file in.
     *
     * @return The full path to the file to output to.
     */
    protected String nameToFileNameInRootGenerationDir(String name, String dirName)
    {
        // Ensure that the output directory exists for the location, if it has not already been created.
        if (!createdOutputDirectories.contains(dirName))
        {
            File dir = new File(dirName);
            dir.mkdirs();
            createdOutputDirectories.add(dirName);
        }

        // Build the full path to the output file.
        return dirName + File.separatorChar + name;
    }

    /**
     * Translates a template group name into the name of the file containing that group. A ".stg" ending is added to the
     * group to form the filename, and the {@link #templateRootPath} is prepended onto it to form the full path. The
     * resulting file name can be understood by StringTemplate as either a relative file path, or a path within the
     * classpath to locate the template group file.
     *
     * @param  group The name of the group to get the file name of.
     *
     * @return The full filename of the template group.
     */
    protected String templateGroupToFileName(String group)
    {
        return templateRootPath + File.separator + group + ".stg";
    }

    /**
     * RenderTemplateHandler is a call-back interface, that is used to call-back upon readiness of a StringTemplate, in
     * order that output may be generated from it through invoking the templates 'render' method.
     *
     * <pre><p/><table id="crc"><caption>CRC Card</caption>
     * <tr><th> Responsibilities
     * <tr><td> Accept notification of the processing of a string template.
     * </table></pre>
     */
    protected interface RenderTemplateHandler
    {
        /**
         * Notified once processing of a template is complete.
         *
         * @param template   The completed template.
         * @param outputName The name of the output resource, such as a file.
         */
        public void render(ST template, String outputName);
    }

    /**
     * FileOutputProcessedTemplateHandler is a processed template handler, that outputs its results to a file. The
     * handler can be created to append or overwrite files.
     */
    protected class FileOutputRenderTemplateHandler implements RenderTemplateHandler
    {
        /** Flag used to indicate if the output file should be appended to. */
        private final boolean append;

        /** Flag used to indicate if existing files should be replaced. */
        private final boolean replace;

        /**
         * Creates a file output handler, that appends or overwrites files.
         *
         * @param append  <tt>true</tt> to append to files.
         * @param replace <tt>true</tt> iff existing files should be replaced by default.
         */
        public FileOutputRenderTemplateHandler(boolean append, boolean replace)
        {
            this.append = append;
            this.replace = replace;
        }

        /** {@inheritDoc} */
        public void render(ST template, String outputName)
        {
            // Check if files should not be replaced, but the file already exists, in which case ignore it.
            if (!replace)
            {
                File file = new File(outputName);

                if (file.exists())
                {
                    return;
                }
            }

            FileUtils.writeObjectToFile(outputName, template.render(), append);
        }
    }

    /**
     * Generate captures all the parameters to a call to the 'generate' methods. This can be useful if a call is to be
     * delayed for some reason, such as calling the generate methods in a different order to the traversal over the
     * model.
     */
    protected class Generate
    {
        private final Catalogue model;
        private final ComponentTypeDecorator decoratedType;
        private final STGroup[] templates;
        private final String[] names;
        private final Map<String, Type> fields;
        private final Map<String, Type> extraFields;
        private final RenderTemplateHandler[] handlers;
        private final String templateName;

        public Generate(Catalogue model, ComponentTypeDecorator decoratedType, STGroup[] templates, String[] names,
            Map<String, Type> fields, Map<String, Type> extraFields, RenderTemplateHandler[] handlers,
            String templateName)
        {
            this.model = model;
            this.decoratedType = decoratedType;
            this.templates = templates;
            this.names = names;
            this.fields = fields;
            this.extraFields = extraFields;
            this.handlers = handlers;
            this.templateName = templateName;
        }

        public Generate(Catalogue model, ComponentTypeDecorator decoratedType, STGroup[] templates, String[] names,
            Map<String, Type> fields, Map<String, Type> extraFields, RenderTemplateHandler[] handlers)
        {
            this.model = model;
            this.decoratedType = decoratedType;
            this.templates = templates;
            this.names = names;
            this.fields = fields;
            this.extraFields = extraFields;
            this.handlers = handlers;
            this.templateName = null;
        }

        public void apply()
        {
            if (templateName == null)
            {
                generate(model, decoratedType, templates, names, fields, extraFields, handlers);
            }
            else
            {
                generate(model, decoratedType, templates, names, fields, extraFields, handlers, templateName);
            }
        }
    }
}
