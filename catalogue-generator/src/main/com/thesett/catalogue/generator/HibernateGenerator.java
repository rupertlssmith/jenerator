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

import java.util.LinkedHashMap;
import java.util.Map;

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
import com.thesett.catalogue.model.Catalogue;
import com.thesett.catalogue.model.EntityType;
import com.thesett.catalogue.model.EntityTypeVisitor;
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

    /** Defines the name of the template group for creating Hibernate configurations. */
    private static final String HIBERNATE_USERTYPE_TEMPLATES_GROUP = "HibernateUserType";

    /** Holds the string template group to generate Hibernate online configurations from. */
    private StringTemplateGroup hibernateOnlineTemplates;

    /** Holds the string template group to generate Hibernate user type configurations from. */
    private StringTemplateGroup hibernateUserTypeConfigTemplates;

    /** Holds the string template group to generate Hibernate warehouse configurations from. */
    private StringTemplateGroup hibernateWarehouseTemplates;

    /** Holds the string template group to generate user types from. */
    private StringTemplateGroup hibernateUserTypeTemplates;

    /** The name of the directory to output hibernate mappings to. */
    private String mappingDirName;

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

    /** Holds a file output handler that overwrites files. */
    protected FileOutputProcessedTemplateHandler fileOutputProcessedTemplateHandler =
        new FileOutputProcessedTemplateHandler(false);

    /**
     * Creates a generator for hibernate configuration XML and custom user types to output to the specified directory
     * root.
     *
     * @param templateDir An alternative directory to load templates from, may be <tt>null</tt> to use defaults.
     */
    public HibernateGenerator(String templateDir)
    {
        super(templateDir);

        hibernateOnlineTemplates = StringTemplateGroup.loadGroup(HIBERNATE_ONLINE_TEMPLATES_GROUP);
        hibernateOnlineTemplates.registerRenderer(String.class, new CamelCaseRenderer());

        hibernateUserTypeConfigTemplates = StringTemplateGroup.loadGroup(HIBERNATE_USERTYPE_CONFIG_TEMPLATES_GROUP);
        hibernateUserTypeConfigTemplates.registerRenderer(String.class, new CamelCaseRenderer());

        hibernateWarehouseTemplates = StringTemplateGroup.loadGroup(HIBERNATE_WAREHOUSE_TEMPLATES_GROUP);
        hibernateWarehouseTemplates.registerRenderer(String.class, new CamelCaseRenderer());

        hibernateUserTypeTemplates = StringTemplateGroup.loadGroup(HIBERNATE_USERTYPE_TEMPLATES_GROUP);
        hibernateUserTypeTemplates.registerRenderer(String.class, new CamelCaseRenderer());
    }

    /**
     * Establishes the output directory to write the hibernate mapping to.
     *
     * @param mappingOutputDir The directory root to generate mappings to.
     */
    public void setMappingOutputDir(String mappingOutputDir)
    {
        this.mappingDirName = mappingOutputDir;
    }

    /**
     * Estbalishes the name of the hibernate mapping file.
     *
     * @param mappingFileName The name of the file to output the hibernate mapping to.
     */
    public void setMappingFileName(String mappingFileName)
    {
        this.mappingFileName = mappingFileName;
    }

    /** Creates the opening section of a hibernate configuration file. */
    public void generateHibernateConfigOpening()
    {
        String outputFileName = nameToFileNameInRootGenerationDir(mappingFileName, mappingDirName);

        // Instantiate the template to generate from.
        StringTemplate stringTemplate = hibernateOnlineTemplates.getInstanceOf(FILE_OPEN_TEMPLATE);
        stringTemplate.setAttribute("catalogue", model);

        FileUtils.writeObjectToFile(outputFileName, stringTemplate, false);
    }

    /** Creates the closing section of a hibnerate configuration file. */
    public void generateHibernateConfigClosing()
    {
        String outputFileName = nameToFileNameInRootGenerationDir(mappingFileName, mappingDirName);

        // Instantiate the template to generate from.
        StringTemplate stringTemplate = hibernateOnlineTemplates.getInstanceOf(FILE_CLOSE_TEMPLATE);

        FileUtils.writeObjectToFile(outputFileName, stringTemplate, true);
    }

    /** Creates a closing section to the user type definitions. */
    public void generateHibernateUserTypeClosing()
    {
        String outputFileName = nameToFileNameInRootGenerationDir(mappingFileName, mappingDirName);

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
        String outputFileName = nameToFileNameInRootGenerationDir(mappingFileName, mappingDirName);

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
     * <p/>Generates hibernate configuration XML for a hierarchy type, and a Java user type to acess it through.
     *
     * @param type The type to generate from.
     */
    public void visit(final HierarchyType type)
    {
        final TypeDecorator decoratedType = TypeDecoratorFactory.decorateType(type);

        StringTemplateGroup[] templates =
            { hibernateOnlineTemplates, hibernateUserTypeConfigTemplates, hibernateUserTypeTemplates };
        String[] names =
            new String[]
            {
                nameToFileNameInRootGenerationDir(mappingFileName, mappingDirName),
                nameToFileNameInRootGenerationDir(mappingFileName, mappingDirName),
                nameToJavaFileName(outputDir, "", type.getName(), "UserType")
            };

        Map<String, Type> fields =
            new LinkedHashMap<String, Type>()
            {
                {
                    for (String label : type.getLevelNames())
                    {
                        put(label, TypeDecoratorFactory.decorateType(JavaType.STRING_TYPE));
                    }

                    put(type.getName(), decoratedType);
                }
            };

        Map<String, Type> extraFields =
            new LinkedHashMap<String, Type>()
            {
                {
                    for (String label : type.getLevelNames())
                    {
                        put(label, TypeDecoratorFactory.decorateType(JavaType.STRING_TYPE));
                    }
                }
            };

        ProcessedTemplateHandler[] handlers =
            new ProcessedTemplateHandler[]
            {
                normalizedTypeDefHandler, userTypeDefHandler, fileOutputProcessedTemplateHandler
            };

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
                nameToFileNameInRootGenerationDir(mappingFileName, mappingDirName),
                nameToFileNameInRootGenerationDir(mappingFileName, mappingDirName)
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
                nameToFileNameInRootGenerationDir(mappingFileName, mappingDirName),
                nameToFileNameInRootGenerationDir(mappingFileName, mappingDirName)
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
     * <p/>Generates hibernate configuration XML for an enumeration type, and a Java user type to access it through.
     *
     * @param type The type to generate from.
     */
    public void visit(final EnumeratedStringAttribute.EnumeratedStringType type)
    {
        final TypeDecorator decoratedType = TypeDecoratorFactory.decorateType(type);

        StringTemplateGroup[] templates =
            { hibernateOnlineTemplates, hibernateUserTypeConfigTemplates, hibernateUserTypeTemplates };
        String[] names =
            new String[]
            {
                nameToFileNameInRootGenerationDir(mappingFileName, mappingDirName),
                nameToFileNameInRootGenerationDir(mappingFileName, mappingDirName),
                nameToJavaFileName(outputDir, "", type.getName(), "UserType")
            };

        Map<String, Type> fields =
            new LinkedHashMap<String, Type>()
            {
                {
                    put(type.getName(), decoratedType);
                }
            };

        Map<String, Type> extraFields =
            new LinkedHashMap<String, Type>()
            {
                {
                    put("value", TypeDecoratorFactory.decorateType(JavaType.STRING_TYPE));
                }
            };

        ProcessedTemplateHandler[] handlers =
            new ProcessedTemplateHandler[]
            {
                normalizedTypeDefHandler, userTypeDefHandler, fileOutputProcessedTemplateHandler
            };

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
                nameToFileNameInRootGenerationDir(mappingFileName, mappingDirName),
                nameToFileNameInRootGenerationDir(mappingFileName, mappingDirName)
            };
        Map<String, Type> fields = decoratedType.getAllPropertyTypes();
        Map<String, Type> extraFields = null;
        ProcessedTemplateHandler[] handlers =
            new ProcessedTemplateHandler[] { onlineMappingHandler, warehouseMappingHandler };

        generate(model, decoratedType, templates, names, fields, extraFields, handlers);
    }
}
