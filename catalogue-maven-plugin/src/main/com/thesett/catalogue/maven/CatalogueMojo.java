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
package com.thesett.catalogue.maven;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.TransformerException;

import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import com.thesett.catalogue.config.ModelLoaderConfigBean;
import com.thesett.catalogue.generator.GeneratorTool;
import com.thesett.catalogue.interfaces.Catalogue;
import com.thesett.common.config.ConfigException;
import com.thesett.common.config.Configurator;
import com.thesett.common.xml.XslTransformerUtils;

/**
 * CatalogueMojo implements a Maven 2 plugin, that calls {@link com.thesett.javasource.generator.SourceCodeGenerator} on
 * a specified catalgoue model using the catalogue code generation template files, and an output location and package.
 * It also transforms the catalogue model into a Hibernate mapping file, and an index mapping file for text based
 * searching.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Call the source code generator with arguments passed from the pom.
 * <tr><td> Apply an XSLT transform to generate the Hibernate mapping.
 * <tr><td> Apply an XSLT transform to generate the index mapping.
 * </table></pre>
 *
 * @author                       Rupert Smith
 * @goal                         generate
 * @phase                        generate-sources
 * @requiresDependencyResolution compile
 */
public class CatalogueMojo extends AbstractMojo
{
    /** Holds the resource name of the configuration. */
    private static final String CONFIG = "generator-config.xml";

    /** Holds the resource name of the catalogue index mapping transform. */
    private static final String INDEX_MAPPING_TRANSFORM = "catalogue-def-to-index-config.xsl";

    /**
     * The maven project model.
     *
     * @parameter property="project"
     * @required
     * @readonly
     */
    public MavenProject project;

    /**
     * The directory containing generated sources.
     *
     * @parameter property="project.build.directory/generated-sources/javasource"
     */
    public String generatedSourcesDirectory;

    /**
     * The directory containing generated test sources.
     *
     * @parameter property="project.build.directory/generated-sources/javasourcetest"
     */
    public String generatedTestSourcesDirectory;

    /**
     * The model file, to generate from.
     *
     * @parameter property="model"
     */
    public String model;

    /**
     * The optional template override dir, to load custom templates from.
     *
     * @parameter property="templateDir"
     */
    public String templateDir;

    /**
     * The output java package name.
     *
     * @parameter property="jpackage"
     */
    public String jpackage;

    /**
     * The debug functors file name.
     *
     * @parameter property="debugModelFilename"
     */
    public String debugModelFilename;

    /**
     * The output filename for the hibernate mapping.
     *
     * @parameter
     * @required
     */
    public String hibernateMappingFilename;

    /**
     * The output filename for the hibernate mapping.
     *
     * @parameter
     * @required
     */
    public String indexMappingFilename;

    /**
     * Applies the source code generator to the specified catalogue model using all of the catalogue generation
     * templates. Applies XSLT transforms to the model to generate the hibernate and index mapping files.
     *
     * @throws MojoExecutionException If any io or transform failures arise.
     * @throws MojoFailureException   If any of the resource files cannot be opened.
     */
    public void execute() throws MojoExecutionException, MojoFailureException
    {
        getLog().info("public void execute(): called");

        // Load the configuration, substituting in the model file name specified on the command line.
        try
        {
            // Create a configurator for the setup and run it.
            Configurator configurator = new Configurator(CONFIG);
            configurator.loadConfigBeans();

            // Set the model file name on the model loader bean.
            ModelLoaderConfigBean modelBean =
                (ModelLoaderConfigBean) configurator.getLoadedBean(
                    "com.thesett.catalogue.config.ModelLoaderConfigBean");
            modelBean.setModelFile(model);

            if ((debugModelFilename != null) && !"".equals(debugModelFilename))
            {
                modelBean.setDebugRawFileName(debugModelFilename);
            }

            // Run the configuration.
            configurator.configureAll();

            // Generate from the loaded model.
            Catalogue catalogue = modelBean.getCatalogue();

            GeneratorTool.generate(catalogue, generatedSourcesDirectory, generatedTestSourcesDirectory,
                hibernateMappingFilename, templateDir);
        }
        catch (ConfigException e)
        {
            MojoFailureException mfe = new MojoFailureException("The configurator failed to run.");
            mfe.initCause(e);

            throw mfe;
        }

        // Set up parameters to pass to all of the XSLT transforms.
        HashMap<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("package", jpackage);

        // Generate the index mapping files.
        transformModel(model, INDEX_MAPPING_TRANSFORM,
            generatedSourcesDirectory + File.separatorChar + indexMappingFilename, parameters);

        // Add generated sources to maven compilation path.
        if (new File(generatedSourcesDirectory).exists())
        {
            project.addCompileSourceRoot(generatedSourcesDirectory);
        }

        // Add generated test soruces to maven test compilation path.
        if (new File(generatedTestSourcesDirectory).exists())
        {
            project.addTestCompileSourceRoot(generatedTestSourcesDirectory);
        }

        // Add generated resources to the classpath.
        if (new File(generatedSourcesDirectory).exists())
        {
            Resource resource = new Resource();
            resource.setDirectory(generatedSourcesDirectory);
            resource.addInclude(hibernateMappingFilename);
            resource.addInclude(indexMappingFilename);

            project.addResource(resource);
        }
    }

    /**
     * Applies an XSLT transform to a catalogue model file.
     *
     * @param  modelFileName         The file containing the model.
     * @param  transformResourceName The resource on the classpath containing the transform to apply.
     * @param  outputFileName        The file to output the results to.
     * @param  parameters            Any parameters to pass to the transformation.
     *
     * @throws MojoFailureException   If the input or output files cannot be opened.
     * @throws MojoExecutionException If the transform fails, or there is an IOException whilst operating on the files.
     */
    private void transformModel(String modelFileName, String transformResourceName, String outputFileName,
        Map<String, Object> parameters) throws MojoFailureException, MojoExecutionException
    {
        // Holds the URL of the XSLT transform to apply.
        URL transformURL = this.getClass().getClassLoader().getResource(transformResourceName);

        // Used to read the catalogue model XML with.
        Reader modelReader;

        // Used to write the results of the transformation out with.
        Writer resultWriter;

        // Open the model file.
        try
        {
            modelReader = new FileReader(modelFileName);
        }
        catch (FileNotFoundException e)
        {
            MojoFailureException failureException =
                new MojoFailureException("Unable to open the model file '" + modelFileName + "' for reading.");
            failureException.initCause(e);
            throw failureException;
        }

        // Open the output file.
        try
        {
            resultWriter = new FileWriter(outputFileName);
        }
        catch (IOException e)
        {
            MojoFailureException failureException =
                new MojoFailureException("Unable to open the output file '" + outputFileName + "' for writing.");
            failureException.initCause(e);
            throw failureException;
        }

        // Apply the transformation.
        try
        {
            XslTransformerUtils.performXslTransformation(modelReader, transformURL, parameters, resultWriter);
            modelReader.close();
            resultWriter.close();
        }
        catch (TransformerException e)
        {
            throw new MojoExecutionException("There was an exception whilst transforming the model using the '" +
                transformResourceName + "' transformation.", e);
        }
        catch (IOException e)
        {
            throw new MojoExecutionException("Got IOException whilst trying to close a file.", e);
        }
    }
}
