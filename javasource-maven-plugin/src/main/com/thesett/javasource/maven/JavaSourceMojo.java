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
 * @author                       Rupert Smith
 * @goal                         generate
 * @phase                        generate-sources
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

    /** Implementation of the "generate" goal. */
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
