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

import java.util.Map;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;

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
public class BeanValidationGenerator extends BaseGenerator implements ComponentTypeVisitor
{
    /** Defines the name of the template group for creating Java Bean Validation configurations. */
    private static final String BEAN_VALIDATION_TEMPLATES_GROUP = "BeanValidation";

    /** Holds the string template group to generate Java Bean Validation configurations from. */
    private StringTemplateGroup beanValidationTemplates;

    /** Output handler used to build up bean validation mappings. */
    private ProcessedTemplateHandler beanValidationHandler = new BufferingTemplateHandler();

    private String validationFileName = "validation.xml";

    /**
     * Creates a generator for Java Bean Validation configuration XML.
     *
     * @param templateDir An alternative directory to load templates from, may be <tt>null</tt> to use defaults.
     */
    public BeanValidationGenerator(String templateDir)
    {
        super(templateDir);

        beanValidationTemplates = StringTemplateGroup.loadGroup(BEAN_VALIDATION_TEMPLATES_GROUP);
        beanValidationTemplates.registerRenderer(String.class, new CamelCaseRenderer());
    }

    /** Generates the opening section of the validation configuration. */
    public void generateConfigOpening()
    {
        String outputFileName = nameToFileNameInRootGenerationDir(validationFileName, outputDir);

        // Instantiate the template to generate from.
        StringTemplate stringTemplate = beanValidationTemplates.getInstanceOf(FILE_OPEN_TEMPLATE);
        stringTemplate.setAttribute("catalogue", model);

        FileUtils.writeObjectToFile(outputFileName, stringTemplate, false);
    }

    /** Generates the closing section of the validation configuration. */
    public void generateConfigClosing()
    {
        String outputFileName = nameToFileNameInRootGenerationDir(validationFileName, outputDir);

        // Instantiate the template to generate from.
        StringTemplate stringTemplate = beanValidationTemplates.getInstanceOf(FILE_CLOSE_TEMPLATE);

        FileUtils.writeObjectToFile(outputFileName, stringTemplate, true);
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
        ComponentTypeDecorator decoratedType = (ComponentTypeDecorator) TypeDecoratorFactory.decorateType(type);

        StringTemplateGroup[] templates;
        String[] names;
        Map<String, Type> fields = decoratedType.getAllPropertyTypes();
        Map<String, Type> extraFields = null;
        ProcessedTemplateHandler[] handlers = new ProcessedTemplateHandler[] { beanValidationHandler };

        templates = new StringTemplateGroup[] { beanValidationTemplates };
        names = new String[] { nameToFileNameInRootGenerationDir(validationFileName, outputDir) };

        generate(model, decoratedType, templates, names, fields, extraFields, handlers);
    }
}
