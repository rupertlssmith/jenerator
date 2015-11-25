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

import java.util.LinkedList;
import java.util.Properties;

import com.thesett.catalogue.config.ModelLoaderConfigBean;
import com.thesett.catalogue.model.Catalogue;
import com.thesett.common.config.ConfigException;
import com.thesett.common.config.Configurator;
import com.thesett.common.util.CommandLineParser;

/**
 * Generator creates source code for the operational level implementation of a catalogue from its knowledge level
 * representation. The knowledge level describes the types of data in the catalogue and declares how this data should be
 * stored, the operational level implements this as Java beans with a mapping to a persistence layer.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Generates source code from the catalogue knowledge level model.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class GeneratorTool
{
    /** Holds the resource name of the configuration. */
    private static final String CONFIG = "generator-config.xml";

    /**
     * Runs the generator tool, to create an implementation of a catalogue model. The command line arguments are:
     *
     * <pre><p/><table><caption>Usage</caption>
     * <tr><td> model <td> The name of a file containing the catalogue model XML.
     * <tr><td> h     <td> The name of a file to output the hibernate mapping to.
     * <tr><td> dir   <td> The root directory to output the results to.
     * </table></pre>
     *
     * @param args The command line arguments.
     */
    public static void main(String[] args)
    {
        // Use the command line parser to evaluate the command line.
        CommandLineParser commandLine =
            new CommandLineParser(
                new String[][]
                {
                    { "model", "The file containing the XML model.", "resource", "true" },
                    { "h", "The name of the file to write the hibernate mapping to.", "file", "true" },
                    { "dir", "The directory to write the output to.", "directory", "true" },
                    { "testdir", "The directory to write the output tests to.", "directory", "true" },
                    { "raw", "The raw model file to write to.", "file", "false" }
                });

        Properties options = null;

        try
        {
            options = commandLine.parseCommandLine(args);
        }
        catch (IllegalArgumentException e)
        {
            System.out.println(commandLine.getErrors());
            System.out.println(commandLine.getUsage());
            System.exit(0);
        }

        // Extract all the command line options.
        String modelFileName = options.getProperty("model");
        String outDirName = options.getProperty("dir");
        String testOutDirName = options.getProperty("testdir");
        String hibernateMappingFileName = options.getProperty("h");
        String rawFileName = options.getProperty("raw");

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
            modelBean.setModelFile(modelFileName);

            if (null != rawFileName)
            {
                modelBean.setDebugRawFileName(rawFileName);
            }

            // Run the configuration.
            configurator.configureAll();

            // Get the loaded catalogue model from its config bean.
            final Catalogue model = modelBean.getCatalogue();

            generate(model, outDirName, outDirName, outDirName, hibernateMappingFileName, null);
        }
        catch (ConfigException e)
        {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Generates a set of Java bean classes and a Hibernate mapping file for those that require persisting, from a
     * catalogue model.
     *
     * @param model                    The model to generate from.
     * @param modelDirName             The directory to write the output the model to.
     * @param daoDirName               The directory to output DAO code to.
     * @param mappingDirName           The directory to output mapping configuration to.
     * @param hibernateMappingFileName The name of the hibernate mapping file to write to.
     * @param templateDir              An alternative directory to load templates from, may be <tt>null</tt> to use
     *                                 defaults.
     */
    public static void generate(Catalogue model, final String modelDirName, final String daoDirName,
        final String mappingDirName, final String hibernateMappingFileName, final String templateDir)
    {
        // Generate from the loaded model for Java with a Hibernate persistence layer.
        Generator generator = new ChainedGenerator(new LinkedList<Generator>()
            {
                {
                    JavaBeanGenerator javaBeanGenerator = new JavaBeanGenerator(templateDir);
                    javaBeanGenerator.setOutputDir(modelDirName);
                    add(javaBeanGenerator);

                    HibernateGenerator hibernateGenerator = new HibernateGenerator(templateDir);
                    hibernateGenerator.setOutputDir(daoDirName);
                    hibernateGenerator.setMappingOutputDir(mappingDirName);
                    hibernateGenerator.setMappingFileName(hibernateMappingFileName);
                    add(hibernateGenerator);
                }
            });

        generator.apply(model);
    }
}
