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
package com.thesett.javasource.maven;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.project.MavenProject;

import com.thesett.javasource.generator.SourceCodeGenerator;

/**
 * JavaSourceMojo implements a Maven 2 plugin, that calls {@link SourceCodeGenerator} on a specified model and template,
 * and an output location and package.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Call the source code generator with arguments passed from the pom.
 * </table></pre>
 *
 * @author Rupert Smith
 *
 * @goal generate
 * @phase generate-sources
 * @requiresDependencyResolution compile
 */
public class JavaSourceMojo extends AbstractMojo
{
    /**
     * The maven project model.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * The directory containing generated sources.
     *
     * @parameter expression="${project.build.directory}/generated-sources/javasource"
     */
    private String generatedSourcesDirectory;

    /**
     * The template file, to generate from.
     *
     * @parameter expression="${template}"
     */
    private String[] templates;

    /**
     * The model file, to generate from.
     *
     * @parameter expression="${model}"
     */
    private String model;

    /**
     * The output java package name.
     *
     * @parameter expression="${jpackage}"
     */
    private String jpackage;

    /**
     * Implementation of the "generate" goal.
     */
    public void execute()
    {
        for (String template : templates)
        {
            SourceCodeGenerator.main(
                new String[]
                {
                    "-templatefile", template, "-modelfile", model, "-dir", generatedSourcesDirectory, "-package",
                    jpackage
                });
        }

        // Add generated sources to maven compilation path.
        if (new File(generatedSourcesDirectory).exists())
        {
            project.addCompileSourceRoot(generatedSourcesDirectory);
        }
    }
}
