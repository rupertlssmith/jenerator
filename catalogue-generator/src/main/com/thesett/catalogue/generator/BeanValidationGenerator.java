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

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

import com.thesett.aima.attribute.impl.EnumeratedStringAttribute;
import com.thesett.aima.attribute.impl.EnumeratedStringTypeVisitor;
import com.thesett.aima.state.ComponentType;
import com.thesett.aima.state.Type;
import com.thesett.catalogue.model.Catalogue;
import com.thesett.catalogue.model.ComponentTypeVisitor;
import com.thesett.common.util.FileUtils;

/**
 * BeanValidationGenerator is a {@link com.thesett.catalogue.generator.Generator} that outputs an XML configuration for
 * the Java Bean Validation framework, that validates the model as Java beans against the type constraints defined in
 * the model.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Generate Java Bean Validation configuration for the model.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class BeanValidationGenerator extends BaseGenerator implements ComponentTypeVisitor, EnumeratedStringTypeVisitor
{
    /** Defines the name of the template group for creating Java Bean Validation configurations. */
    private static final String BEAN_VALIDATION_TEMPLATES_GROUP = "BeanValidation";

    /** Holds the string template group to generate Java Bean Validation configurations from. */
    private final STGroup beanValidationTemplates;

    /** Output handler used to build up bean validation mappings. */
    private final RenderTemplateHandler beanValidationHandler = new BufferingTemplateHandler();

    private final String validationFileName = "constraints.xml";

    /**
     * Creates a generator for Java Bean Validation configuration XML.
     *
     * @param templateDir An alternative directory to load templates from, may be <tt>null</tt> to use defaults.
     */
    public BeanValidationGenerator(String templateDir)
    {
        super(templateDir);

        beanValidationTemplates = new STGroupFile(templateGroupToFileName(BEAN_VALIDATION_TEMPLATES_GROUP));
        beanValidationTemplates.registerRenderer(String.class, new CamelCaseRenderer());
    }

    /** Generates the opening section of the validation configuration. */
    public void generateConfigOpening()
    {
        String outputFileName = nameToFileNameInRootGenerationDir(validationFileName, outputDir);

        // Instantiate the template to generate from.
        ST stringTemplate = beanValidationTemplates.getInstanceOf(FILE_OPEN_TEMPLATE);
        stringTemplate.add("catalogue", model);

        fileOutputHandlerOverwrite.render(stringTemplate, outputFileName);
    }

    /** Generates the closing section of the validation configuration. */
    public void generateConfigClosing()
    {
        String outputFileName = nameToFileNameInRootGenerationDir(validationFileName, outputDir);

        // Instantiate the template to generate from.
        ST stringTemplate = beanValidationTemplates.getInstanceOf(FILE_CLOSE_TEMPLATE);

        fileOutputHandlerAppend.render(stringTemplate, outputFileName);
    }

    /**
     * {@inheritDoc}
     *
     * <p/>Ensures that the opening section of the config file is created.
     */
    public void preApply(Catalogue catalogue)
    {
        generateConfigOpening();
    }

    /**
     * {@inheritDoc}
     *
     * <p/>Ensures that the closing section of the config file is created.
     */
    public void postApply(Catalogue catalogue)
    {
        // Output the configuration sections built up in the output buffers in the correct order to the configuration file.
        String outputFileName = nameToFileNameInRootGenerationDir(validationFileName, outputDir);

        FileUtils.writeObjectToFile(outputFileName, beanValidationHandler, true);

        generateConfigClosing();
    }

    /** {@inheritDoc} */
    public void visit(ComponentType type)
    {
        ComponentType decoratedType = (ComponentTypeDecorator) TypeDecoratorFactory.decorateType(type);

        STGroup[] templates = new STGroup[] { beanValidationTemplates };
        String[] names = new String[] { nameToFileNameInRootGenerationDir(validationFileName, outputDir) };
        Map<String, Type> fields = decoratedType.getAllPropertyTypes();
        Map<String, Type> extraFields = null;
        RenderTemplateHandler[] handlers = new RenderTemplateHandler[] { beanValidationHandler };

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

        STGroup[] templates = new STGroup[] { beanValidationTemplates };
        String[] names = new String[] { nameToFileNameInRootGenerationDir(validationFileName, outputDir) };
        Map<String, Type> fields =
            new LinkedHashMap<String, Type>()
            {
                {
                    put(type.getName(), decoratedType);
                }
            };

        Map<String, Type> extraFields = null;
        RenderTemplateHandler[] handlers = new RenderTemplateHandler[] { beanValidationHandler };

        generate(model, decoratedType, templates, names, fields, extraFields, handlers);
    }
}
