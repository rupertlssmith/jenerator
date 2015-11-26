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

import java.lang.reflect.Constructor;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.thesett.common.util.Function;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import com.thesett.catalogue.config.ModelLoaderConfigBean;
import com.thesett.catalogue.generator.ChainedGenerator;
import com.thesett.catalogue.model.Catalogue;
import com.thesett.common.config.ConfigException;
import com.thesett.common.config.Configurator;
import com.thesett.common.util.ReflectionUtils;

/**
 * CatalogueMojo implements a Maven 2 plugin, that invokes code generators derived from
 * {@link com.thesett.catalogue.generator.BaseGenerator}, with parameters defined in the POM.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Call the source code generators with arguments passed from the POM.
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

    /**
     * The maven project model.
     *
     * @parameter property="project"
     * @required
     * @readonly
     */
    public MavenProject project;

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
     * The debug functors file name.
     *
     * @parameter property="debugModelFilename"
     */
    public String debugModelFilename;

    /**
     * The generator definitions.
     *
     * @parameter property="generators"
     */
    public List<Generator> generators;

    /**
     * Applies the source code generator to the specified catalogue model using all of the catalogue generation
     * templates. Applies XSLT transforms to the model to generate the hibernate and index mapping files.
     *
     * @throws MojoExecutionException If any io or transform failures arise.
     * @throws MojoFailureException   If any of the resource files cannot be opened.
     */
    public void execute() throws MojoExecutionException, MojoFailureException
    {
        getLog().info("Jenerator Applying CodeGen to the Model.");

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

            generate(catalogue, templateDir, generators);
        }
        catch (ConfigException e)
        {
            MojoFailureException mfe = new MojoFailureException("The configurator failed to run.");
            mfe.initCause(e);

            throw mfe;
        }
    }

    /**
     * Generates a set of Java bean classes and a Hibernate mapping file for those that require persisting, from a
     * catalogue model.
     *
     * @param model       The model to generate from.
     * @param templateDir An alternative directory to load templates from, may be <tt>null</tt> to use defaults.
     * @param generators  A list of generator configurations.
     */
    private void generate(Catalogue model, final String templateDir, final Iterable<Generator> generators)
    {
        // Generate from the loaded model for Java with a Hibernate persistence layer.
        Function generator =
            new ChainedGenerator(new LinkedList<com.thesett.catalogue.generator.Generator>()
                {
                    {
                        for (Generator generatorConfig : generators)
                        {
                            Map<String, String> params = generatorConfig.getConfig();
                            Class<? extends com.thesett.catalogue.generator.BaseGenerator> generatorClass =
                                (Class<? extends com.thesett.catalogue.generator.BaseGenerator>) ReflectionUtils
                                .forName(params.get("type"));
                            Constructor<? extends com.thesett.catalogue.generator.BaseGenerator> constructor =
                                ReflectionUtils.getConstructor(generatorClass, new Class[] { String.class });

                            com.thesett.catalogue.generator.BaseGenerator generatorImpl =
                                ReflectionUtils.newInstance(constructor, new Object[] { templateDir });

                            for (Map.Entry<String, String> param : params.entrySet())
                            {
                                if ("type".equals(param.getKey()))
                                {
                                    continue;
                                }

                                generatorImpl.setProperty(param.getKey(), param.getValue());
                            }

                            add(generatorImpl);
                        }
                    }
                });

        generator.apply(model);
    }
}
