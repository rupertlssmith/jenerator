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
