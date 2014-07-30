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
import java.util.LinkedHashMap;
import java.util.Map;

import org.antlr.stringtemplate.CommonGroupLoader;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;

import com.thesett.aima.attribute.impl.DateRangeType;
import com.thesett.aima.attribute.impl.DateRangeTypeVisitor;
import com.thesett.aima.attribute.impl.EnumeratedStringAttribute;
import com.thesett.aima.attribute.impl.EnumeratedStringTypeVisitor;
import com.thesett.aima.attribute.impl.HierarchyType;
import com.thesett.aima.attribute.impl.HierarchyTypeVisitor;
import com.thesett.aima.attribute.impl.TimeRangeType;
import com.thesett.aima.attribute.impl.TimeRangeTypeVisitor;
import com.thesett.aima.state.Type;
import com.thesett.aima.state.impl.JavaType;
import com.thesett.catalogue.interfaces.Catalogue;
import com.thesett.catalogue.interfaces.EntityType;
import com.thesett.catalogue.interfaces.EntityTypeVisitor;
import com.thesett.common.util.FileUtils;

/**
 * HibernateGenerator is a {@link Generator} that outputs Hibernate configuration XML and custom user types from a
 * catalogue model.
 *
 * <p/>The hibernate configuration file needs to have its elements speicified in a certain order. In particular user
 * defined type mappings must come earlier in the file than class mappings. For this reason, the hibernate generator
 * uses a customized template output handler, that retains the output for all of the fragments of configuration XML, and
 * then outputs all of the fragments in the correct order during the post-processing step.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Generate hibernate configuration for a catalogue model.
 * <tr><td> Generate hibernate custom user types as required for a catalogue model.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class HibernateGenerator extends BaseGenerator implements HierarchyTypeVisitor, EnumeratedStringTypeVisitor,
    EntityTypeVisitor, DateRangeTypeVisitor, TimeRangeTypeVisitor
{
    /** Defines the name of the template group for creating Hibernate configurations. */
    private static final String HIBERNATE_ONLINE_TEMPLATES_GROUP = "HibernateOnline";

    /** Defines the name of the template group for creating Hibernate user type definition configurations. */
    private static final String HIBERNATE_USERTYPE_CONFIG_TEMPLATES_GROUP = "HibernateUserTypeConfig";

    /** Defines the name of the template group for creating Hibernate configurations. */
    private static final String HIBERNATE_WAREHOUSE_TEMPLATES_GROUP = "HibernateWarehouse";

    /** Holds the string template group to generate Hibernate online configurations from. */
    private StringTemplateGroup hibernateOnlineTemplates;

    /** Holds the string template group to generate Hibernate user type configurations from. */
    private StringTemplateGroup hibernateUserTypeConfigTemplates;

    /** Holds the string template group to generate Hibernate warehouse configurations from. */
    private StringTemplateGroup hibernateWarehouseTemplates;

    /** Holds the name of the file to output the hibernate mapping to. */
    private String mappingFileName;

    /** Output handler used to build up custom user type def configurations in. */
    private ProcessedTemplateHandler userTypeDefHandler = new BufferingTemplateHandler();

    /** Output handler used to build up class mappings for normalized custom type definitions. */
    private ProcessedTemplateHandler normalizedTypeDefHandler = new BufferingTemplateHandler();

    /** Output handler used to build up the online database mapping configuration in. */
    private ProcessedTemplateHandler onlineMappingHandler = new BufferingTemplateHandler();

    /** Output handler used to build up the warehouse database mapping configuration in. */
    private ProcessedTemplateHandler warehouseMappingHandler = new BufferingTemplateHandler();

    /**
     * Creates a generator for hibernate configuration XML and custom user types to output to the specified directory
     * root.
     *
     * @param outputDirName   The directory root to generate to.
     * @param mappingFileName The name of the file to output the hibernate mapping to.
     * @param templateDir     An alternative directory to load templates from, may be <tt>null</tt> to use defaults.
     */
    public HibernateGenerator(String outputDirName, String mappingFileName, String templateDir)
    {
        super(outputDirName);

        this.mappingFileName = mappingFileName;

        registerTemplateLoader(templateDir);

        hibernateOnlineTemplates = StringTemplateGroup.loadGroup(HIBERNATE_ONLINE_TEMPLATES_GROUP);
        hibernateOnlineTemplates.registerRenderer(String.class, new CamelCaseRenderer());

        hibernateUserTypeConfigTemplates = StringTemplateGroup.loadGroup(HIBERNATE_USERTYPE_CONFIG_TEMPLATES_GROUP);
        hibernateUserTypeConfigTemplates.registerRenderer(String.class, new CamelCaseRenderer());

        hibernateWarehouseTemplates = StringTemplateGroup.loadGroup(HIBERNATE_WAREHOUSE_TEMPLATES_GROUP);
        hibernateWarehouseTemplates.registerRenderer(String.class, new CamelCaseRenderer());
    }

    /** Creates the opening section of a hibernate configuration file. */
    public void generateHibernateConfigOpening()
    {
        String outputFileName = nameToFileNameInRootGenerationDir(mappingFileName);

        // Instantiate the template to generate from.
        StringTemplate stringTemplate = hibernateOnlineTemplates.getInstanceOf(FILE_OPEN_TEMPLATE);
        stringTemplate.setAttribute("catalogue", model);

        FileUtils.writeObjectToFile(outputFileName, stringTemplate, false);
    }

    /** Creates the closing section of a hibnerate configuration file. */
    public void generateHibernateConfigClosing()
    {
        String outputFileName = nameToFileNameInRootGenerationDir(mappingFileName);

        // Instantiate the template to generate from.
        StringTemplate stringTemplate = hibernateOnlineTemplates.getInstanceOf(FILE_CLOSE_TEMPLATE);

        FileUtils.writeObjectToFile(outputFileName, stringTemplate, true);
    }

    /** Creates a closing section to the user type definitions. */
    public void generateHibernateUserTypeClosing()
    {
        String outputFileName = nameToFileNameInRootGenerationDir(mappingFileName);

        // Instantiate the template to generate from.
        StringTemplate stringTemplate = hibernateUserTypeConfigTemplates.getInstanceOf(FILE_CLOSE_TEMPLATE);

        FileUtils.writeObjectToFile(outputFileName, stringTemplate, true);
    }

    /**
     * {@inheritDoc}
     *
     * <p/>Ensures that the opening section of the hibernate config file is created.
     */
    public void preApply(Catalogue catalogue)
    {
        generateHibernateConfigOpening();
    }

    /**
     * {@inheritDoc}
     *
     * <p/>Ensures that the closing section of the hibernate config file is created.
     */
    public void postApply(Catalogue catalogue)
    {
        // Output the configuration sections built up in the output buffers in the correct order to the configuration file.
        String outputFileName = nameToFileNameInRootGenerationDir(mappingFileName);

        FileUtils.writeObjectToFile(outputFileName, userTypeDefHandler, true);
        generateHibernateUserTypeClosing();
        FileUtils.writeObjectToFile(outputFileName, normalizedTypeDefHandler, true);
        FileUtils.writeObjectToFile(outputFileName, onlineMappingHandler, true);
        FileUtils.writeObjectToFile(outputFileName, warehouseMappingHandler, true);

        generateHibernateConfigClosing();
    }

    /**
     * {@inheritDoc}
     *
     * <p/>Generates hibernate configuration XML for a hierarchy type.
     *
     * @param type The type to generate from.
     */
    public void visit(final HierarchyType type)
    {
        final TypeDecorator decoratedType = TypeDecoratorFactory.decorateType(type);

        StringTemplateGroup[] templates = { hibernateOnlineTemplates, hibernateUserTypeConfigTemplates };
        String[] names =
            new String[]
            {
                nameToFileNameInRootGenerationDir(mappingFileName), nameToFileNameInRootGenerationDir(mappingFileName)
            };
        Map<String, Type> fields =
            new LinkedHashMap<String, Type>()
            {
                {
                    for (String label : type.getLevelNames())
                    {
                        put(label, TypeDecoratorFactory.decorateType(JavaType.STRING_TYPE));
                    }
                }
            };

        Map<String, Type> extraFields = null;

        ProcessedTemplateHandler[] handlers =
            new ProcessedTemplateHandler[] { normalizedTypeDefHandler, userTypeDefHandler };

        generate(model, decoratedType, templates, names, fields, extraFields, handlers);
    }

    /**
     * {@inheritDoc}
     *
     * <p/>Generates hibernate configuration XML for a date range type.
     *
     * @param type The type to generate from.
     */
    public void visit(DateRangeType type)
    {
        final TypeDecorator decoratedType = TypeDecoratorFactory.decorateType(type);

        StringTemplateGroup[] templates = { hibernateOnlineTemplates, hibernateUserTypeConfigTemplates };
        String[] names =
            new String[]
            {
                nameToFileNameInRootGenerationDir(mappingFileName), nameToFileNameInRootGenerationDir(mappingFileName)
            };
        Map<String, Type> fields = new LinkedHashMap<String, Type>();

        Map<String, Type> extraFields = null;

        ProcessedTemplateHandler[] handlers =
            new ProcessedTemplateHandler[] { normalizedTypeDefHandler, userTypeDefHandler };

        generate(model, decoratedType, templates, names, fields, extraFields, handlers);
    }

    /**
     * {@inheritDoc}
     *
     * <p/>Generates hibernate configuration XML for a time range type.
     *
     * @param type The type to generate from.
     */
    public void visit(TimeRangeType type)
    {
        final TypeDecorator decoratedType = TypeDecoratorFactory.decorateType(type);

        StringTemplateGroup[] templates = { hibernateOnlineTemplates, hibernateUserTypeConfigTemplates };
        String[] names =
            new String[]
            {
                nameToFileNameInRootGenerationDir(mappingFileName), nameToFileNameInRootGenerationDir(mappingFileName)
            };
        Map<String, Type> fields = new LinkedHashMap<String, Type>();

        Map<String, Type> extraFields = null;

        ProcessedTemplateHandler[] handlers =
            new ProcessedTemplateHandler[] { normalizedTypeDefHandler, userTypeDefHandler };

        generate(model, decoratedType, templates, names, fields, extraFields, handlers);
    }

    /**
     * {@inheritDoc}
     *
     * <p/>Generates hibernate configuration XML for an enumeration type.
     *
     * @param type The type to generate from.
     */
    public void visit(EnumeratedStringAttribute.EnumeratedStringType type)
    {
        final TypeDecorator decoratedType = TypeDecoratorFactory.decorateType(type);

        StringTemplateGroup[] templates = { hibernateOnlineTemplates, hibernateUserTypeConfigTemplates };
        String[] names =
            new String[]
            {
                nameToFileNameInRootGenerationDir(mappingFileName), nameToFileNameInRootGenerationDir(mappingFileName)
            };
        Map<String, Type> fields = new LinkedHashMap<String, Type>();

        Map<String, Type> extraFields = null;

        ProcessedTemplateHandler[] handlers =
            new ProcessedTemplateHandler[] { normalizedTypeDefHandler, userTypeDefHandler };

        generate(model, decoratedType, templates, names, fields, extraFields, handlers);
    }

    /**
     * {@inheritDoc}
     *
     * <p/>Generates hibnerate configuration XML for an entity or sub-type.
     *
     * @param type The type to generate from.
     */
    public void visit(EntityType type)
    {
        ComponentTypeDecorator decoratedType = (ComponentTypeDecorator) TypeDecoratorFactory.decorateType(type);

        StringTemplateGroup[] templates = { hibernateOnlineTemplates, hibernateWarehouseTemplates };
        String[] names =
            new String[]
            {
                nameToFileNameInRootGenerationDir(mappingFileName), nameToFileNameInRootGenerationDir(mappingFileName)
            };
        Map<String, Type> fields = decoratedType.getAllPropertyTypes();
        Map<String, Type> extraFields = null;
        ProcessedTemplateHandler[] handlers =
            new ProcessedTemplateHandler[] { onlineMappingHandler, warehouseMappingHandler };

        generate(model, decoratedType, templates, names, fields, extraFields, handlers);
    }

    /**
     * Converts a name to the name of a file in the root output directory for the generation, and ensures that that
     * directory exists if it has not already been created.
     *
     * @param  name The name to convert to a path to a file in the root generator output directory.
     *
     * @return The full path to the file to output to.
     */
    protected String nameToFileNameInRootGenerationDir(String name)
    {
        // Ensure that the output directory exists for the location, if it has not already been created.
        if (!createdOutputDirectories.contains(outputDirName))
        {
            File dir = new File(outputDirName);
            dir.mkdirs();
            createdOutputDirectories.add(outputDirName);
        }

        // Build the full path to the output file.
        return outputDirName + File.separatorChar + name;
    }

    /**
     * BufferingTemplateHandler is a processed template handler, that retains the output fragments from all processed
     * templates that it recieves in a buffer, so that the contents of the buffer may be output in a correct sequence at
     * a later time.
     *
     * <pre><p/><table id="crc"><caption>CRC Card</caption>
     * <tr><th> Responsibilities <th> Collaborations
     * <tr><td> Retain the output of a template for later processing.
     * </table></pre>
     */
    private class BufferingTemplateHandler implements ProcessedTemplateHandler
    {
        /** The buffer to build up the output in. */
        private StringBuffer buffer = new StringBuffer();

        /** {@inheritDoc} */
        public void processed(StringTemplate template, String outputName)
        {
            buffer.append(template);
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
}
