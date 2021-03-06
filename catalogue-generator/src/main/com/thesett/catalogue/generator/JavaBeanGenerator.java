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

import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

import com.thesett.aima.attribute.impl.EnumeratedStringAttribute;
import com.thesett.aima.attribute.impl.EnumeratedStringTypeVisitor;
import com.thesett.aima.attribute.impl.HierarchyType;
import com.thesett.aima.attribute.impl.HierarchyTypeVisitor;
import com.thesett.aima.state.ComponentType;
import com.thesett.aima.state.Type;
import com.thesett.aima.state.impl.JavaType;
import com.thesett.catalogue.model.ComponentTypeVisitor;

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
public class JavaBeanGenerator extends BaseGenerator implements ComponentTypeVisitor, HierarchyTypeVisitor,
    EnumeratedStringTypeVisitor
{
    /** Defines the name of the template group for creating Java beans. */
    private static final String JAVA_BEAN_TEMPLATES_GROUP = "JavaBean";

    /** Defines the name of the template group for creating Java interfaces. */
    private static final String JAVA_INTERFACE_TEMPLATES_GROUP = "JavaInterface";

    /** Holds the string template group to generate Java beans from. */
    private final STGroup javaBeanTemplates;

    /** Holds the string template group to generate Java interfaces from. */
    private final STGroup javaInterfaceTemplates;

    /** Holds a file output handler that overwrites files. */
    protected RenderTemplateHandler fileOutputProcessedTemplateHandler =
        new FileOutputRenderTemplateHandler(false, true);

    /** Indicates whether implementations should be generated for views or not. */
    private boolean viewImplementations;

    /**
     * Creates a Java generator to output to the specified directory root.
     *
     * @param templateDir An alternative directory to load templates from, may be <tt>null</tt> to use defaults.
     */
    public JavaBeanGenerator(String templateDir)
    {
        super(templateDir);

        javaBeanTemplates = new STGroupFile(templateGroupToFileName(JAVA_BEAN_TEMPLATES_GROUP));
        javaBeanTemplates.registerRenderer(String.class, new CamelCaseRenderer());

        javaInterfaceTemplates = new STGroupFile(templateGroupToFileName(JAVA_INTERFACE_TEMPLATES_GROUP));
        javaInterfaceTemplates.registerRenderer(String.class, new CamelCaseRenderer());
    }

    public void setViewImplementations(String value)
    {
        viewImplementations = "true".equals(value);
    }

    /**
     * Generates a bean class for a component type in the catalogue model.
     *
     * @param type The component type to create a bean for.
     */
    public void visit(ComponentType type)
    {
        ComponentTypeDecorator decoratedType = (ComponentTypeDecorator) TypeDecoratorFactory.decorateType(type);

        STGroup[] templates;
        String[] names;
        Map<String, Type> fields = decoratedType.getAllPropertyTypes();
        Map<String, Type> extraFields = null;
        RenderTemplateHandler[] handlers =
            new RenderTemplateHandler[] { fileOutputProcessedTemplateHandler, fileOutputProcessedTemplateHandler };

        if (decoratedType.isView())
        {
            if (viewImplementations)
            {
                templates = new STGroup[] { javaBeanTemplates, javaInterfaceTemplates };
                names =
                    new String[]
                    {
                        nameToJavaFileName(outputDir, "", type.getName(), "Impl"),
                        nameToJavaFileName(outputDir, "", type.getName(), "")
                    };
            }
            else
            {
                templates = new STGroup[] { javaInterfaceTemplates };
                names = new String[] { nameToJavaFileName(outputDir, "", type.getName(), "") };
            }
        }
        else
        {
            templates = new STGroup[] { javaBeanTemplates };
            names = new String[] { nameToJavaFileName(outputDir, "", type.getName(), "") };
        }

        generate(model, decoratedType, templates, names, fields, extraFields, handlers);
    }

    /**
     * {@inheritDoc}</p>
     *
     * Generates a bean class for each enum type.
     */
    public void visit(EnumeratedStringAttribute.EnumeratedStringType type)
    {
        generateBeanOnly(type);
    }

    /**
     * {@inheritDoc}
     *
     * <p/>Generates a bean class for each hierarchy type.
     */
    public void visit(HierarchyType type)
    {
        generateBeanOnly(type);
    }

    /**
     * Generates a bean class for the type specified.
     *
     * @param type The type to generate a bean class for.
     */
    private void generateBeanOnly(final Type type)
    {
        final TypeDecorator decoratedType = TypeDecoratorFactory.decorateType(type);

        STGroup[] templates = new STGroup[] { javaBeanTemplates };
        String[] names = new String[] { nameToJavaFileName(outputDir, "", type.getName(), "") };

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

        RenderTemplateHandler[] handlers = new RenderTemplateHandler[] { fileOutputProcessedTemplateHandler };

        generate(model, decoratedType, templates, names, fields, extraFields, handlers);
    }
}
