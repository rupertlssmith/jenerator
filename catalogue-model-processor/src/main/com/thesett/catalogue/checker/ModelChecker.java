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
package com.thesett.catalogue.checker;

import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.thesett.aima.logic.fol.Clause;
import com.thesett.aima.logic.fol.Variable;
import com.thesett.aima.logic.fol.interpreter.ResolutionEngine;
import com.thesett.aima.logic.fol.prolog.PrologCompiledClause;
import com.thesett.catalogue.config.ModelLoaderConfigBean;
import com.thesett.catalogue.core.CatalogueModelFactory;
import com.thesett.common.config.ConfigException;
import com.thesett.common.config.Configurator;
import com.thesett.common.util.CommandLineParser;

/**
 * ModelChecker implements a command line type checking tool for the catalogue model. A catalogue model in XML form is
 * specified on the command line, and a {@link CatalogueModelFactory} object is created from it. This model is then
 * queried for all raw types, normalized types, types that pass type checking, and types that fail type checking and the
 * results are output to the console.
 *
 * <p/>This type checker accepts the following command line arguments:
 *
 * <pre>
 * -modelfile filename The file containing the XML model.
 * </pre>
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Query a catalogue model for the types its contains. <td> {@link CatalogueModelFactory}
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class ModelChecker
{
    /** Used for debugging purposes. */
    static final Logger log = Logger.getLogger(ModelChecker.class);

    /** Used for printing on the console. */
    static final Logger console = Logger.getLogger("CONSOLE." + ModelChecker.class.getName());

    /** Holds the resource name of the configuration. */
    static final String CONFIG = "checker-config.xml";

    /**
     * Launches the type checker from the catalogue rules on a specified catalogue model file.
     *
     * @param args The command line.
     */
    public static void main(String[] args)
    {
        // Use the command line parser to evaluate the command line.
        CommandLineParser commandLine =
            new CommandLineParser(
                new String[][]
                {
                    {
                        "modelresource", "The resource on the classpath containing the XML model.", "resource", "true"
                    },
                    { "file", "The optional file to write out the raw types to.", "file", "false" }
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
        String modelResourceName = options.getProperty("modelresource");
        String modelDebugFile = options.getProperty("file");

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
            modelBean.setModelResource(modelResourceName);

            // Create a file to send the raw model to, if one was specified on the command line.
            if (modelDebugFile != null)
            {
                modelBean.setDebugRawFileName(modelDebugFile);
            }

            // Run the configuration.
            configurator.configureAll();

            // Create an instance of this model and run its type checker.
            CatalogueModelFactory model = modelBean.getCatalogueFactory();

            typeCheckModel(model);
        }
        catch (ConfigException e)
        {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Runs queries against the catalogue model for the raw types it contains, the normalized types, and the types which
     * pass or fail type checking and prints these to the console.
     *
     * @param model The catalogue model.
     */
    static void typeCheckModel(CatalogueModelFactory model)
    {
        // Get the resolution engine used by the model to perform type checking.
        ResolutionEngine<Clause, PrologCompiledClause, PrologCompiledClause> engine = model.getEngine();

        // Print out a list of all types in the model, valid or not
        Iterable<Map<String, Variable>> rawTypes = model.getRawTypes();
        console.info("\n/* ================== Type Instances to Check ==================== */\n");

        for (Map<String, Variable> solution : rawTypes)
        {
            console.info(engine.printSolution(solution));
        }

        // Print out a list of all normalized types in the model.
        Iterable<Map<String, Variable>> normalizedTypes = model.getNormalizedTypes();
        console.info("\n/* ======================= Normalized Types ======================= */\n");

        for (Map<String, Variable> solution : normalizedTypes)
        {
            console.info(engine.printSolution(solution));
        }

        // Print out a list of all valid types in the model.
        Iterable<Map<String, Variable>> checkedTypes = model.getCheckedTypes();
        console.info("\n/* ====================== Type Checked Ok ======================= */\n");

        for (Map<String, Variable> solution : checkedTypes)
        {
            console.info(engine.printSolution(solution));
        }

        // Print out a list of all types in the model which failed to type check.
        Iterable<Map<String, Variable>> failedCheckTypes = model.getFailedCheckTypes();
        console.info("\n/* ====================== Failed To Type Check ================== */\n");

        for (Map<String, Variable> solution : failedCheckTypes)
        {
            console.info(engine.printSolution(solution));
        }
    }
}
