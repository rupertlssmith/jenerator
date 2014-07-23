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

import java.util.LinkedList;
import java.util.Properties;

import com.thesett.catalogue.config.ModelLoaderConfigBean;
import com.thesett.catalogue.interfaces.Catalogue;
import com.thesett.common.config.ConfigException;
import com.thesett.common.config.Configurator;
import com.thesett.common.util.CommandLineParser;

/**
 * Generator creates source code for the operational level implementation of a catalogue from its knowledge level
 * representation. The knowledge level describes the types of data in the catalogue and declares how this data
 * should be stored, the operational level implements this as Java beans with a mapping to a persistence layer.
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

            generate(model, outDirName, testOutDirName, hibernateMappingFileName);
        }
        catch (ConfigException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Generates a set of Java bean classes and a Hibernate mapping file for those that require persisting, from
     * a catalogue model.
     *
     * @param model                    The model to generate from.
     * @param outDirName               The directory to write the output to.
     * @param testOutDirName           The directory to write the generated test output to.
     * @param hibernateMappingFileName The name of the hibernate mapping file to write to.
     */
    public static void generate(Catalogue model, final String outDirName, final String testOutDirName,
        final String hibernateMappingFileName)
    {
        // Generate from the loaded model for Java with a Hibernate persistence layer.
        Generator generator = new ChainedGenerator(new LinkedList<Generator>()
            {
                {
                    add(new JavaBeanGenerator(outDirName));
                    add(new HibernateGenerator(outDirName, hibernateMappingFileName));
                    /*add(new JavaTestGenerator(testOutDirName));*/
                }
            });

        generator.apply(model);
    }
}
