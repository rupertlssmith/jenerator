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

import org.antlr.stringtemplate.StringTemplateGroup;

import com.thesett.aima.state.ComponentType;
import com.thesett.aima.state.Type;
import com.thesett.catalogue.interfaces.ComponentTypeVisitor;

/**
 * JavaBeanGenerator is a {@link Generator} that outputs Java beans and interfaces from a catalogue model.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Generate Java code from a catalogue model.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class JavaTestGenerator extends BaseGenerator implements ComponentTypeVisitor
{
    /** Defines the name of the template group for creating Java tests. */
    private static final String JAVA_TEST_TEMPLATES_GROUP = "JavaTest";

    /** Holds the string template group to generate Java tests from. */
    private StringTemplateGroup javaTestTemplates;

    /** Holds a file output handler that overwrites files. */
    protected FileOutputProcessedTemplateHandler fileOutputProcessedTemplateHandler =
        new FileOutputProcessedTemplateHandler(false);

    /**
     * Creates a Java generator to output to the specified directory root.
     *
     * @param outputDirName The root directory to generate output to.
     */
    public JavaTestGenerator(String outputDirName)
    {
        super(outputDirName);

        javaTestTemplates = StringTemplateGroup.loadGroup(JAVA_TEST_TEMPLATES_GROUP);
        javaTestTemplates.registerRenderer(String.class, new CamelCaseRenderer());
    }

    /**
     * Generates a bean class for a component type in the catalogue model.
     *
     * @param type The component type to create a bean for.
     */
    public void visit(ComponentType type)
    {
        ComponentTypeDecorator decoratedType = (ComponentTypeDecorator) TypeDecoratorFactory.decorateType(type);

        StringTemplateGroup[] templates = new StringTemplateGroup[] { javaTestTemplates };
        String[] names = new String[] { nameToJavaFileName("", type.getName(), "Test") };
        Map<String, Type> fields = decoratedType.getAllPropertyTypes();
        Map<String, Type> extraFields = null;
        ProcessedTemplateHandler[] handlers = new ProcessedTemplateHandler[] { fileOutputProcessedTemplateHandler };

        generate(model, decoratedType, templates, names, fields, extraFields, handlers);
    }
}
