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
package com.thesett.common.config.beans;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import com.thesett.common.config.ConfigBean;
import com.thesett.common.config.ConfigBeanContext;
import com.thesett.common.config.ConfigException;

/**
 * Configuration bean for Log4J. Allows the config .xml file name to be specified.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Configure Log4J from XML config file
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class Log4JConfigBean implements ConfigBean
{
    /** Defines the default resource to use to configure Log4J. */
    private static final String RESOURCE_NAME = "log4j.xml";

    /** Holds resource name to use to configure Log4J. */
    private String resourceName = RESOURCE_NAME;

    /** Holds the verbose flag, which determines whether System.out gets spammed with the log4j config details. */
    private boolean verbose = false;

    /** Flag to represent configuration status of this configure bean. */
    private boolean configured = false;

    /**
     * Creates a new Log4JConfigBean object.
     */
    public Log4JConfigBean()
    {
    }

    /**
     * Sets verbose mode, that tells this bean to print information about the attempted log4j configuration to
     * System.out.
     *
     * @param verbose The setting of the verbose flag.
     */
    public void setVerboseMode(boolean verbose)
    {
        this.verbose = verbose;
    }

    /**
     * Sets the resource within the classpath from which the log4j configuration file should be loaded.
     *
     * @param resourceName The resource within the classpath from which the log4j configuration file should be loaded.
     */
    public void setResourceName(String resourceName)
    {
        this.resourceName = resourceName;
    }

    /**
     * Checks whether or not the config bean has been succesfully run and is in a configured state.
     *
     * @return True if the config bean has run its configuration succesfully.
     */
    public boolean getConfigured()
    {
        return configured;
    }

    /**
     * Loads the log4j config file and class the log4j DOM configurator with it.
     *
     * @param force Setting this to true tells the config bean to re-run its configuration action even if it has
     *              already been run.
     * @param configBeanContext A reference to the configurator that is managing the whole configuration process.
     *
     * @throws ConfigException If some error occurs that means that the configuration cannot be succesfully completed.
     */
    public void doConfigure(boolean force, ConfigBeanContext configBeanContext) throws ConfigException
    {
        // If already configured then only reconfigure if force is set to true
        if (configured && !force)
        {
            return;
        }

        try
        {
            if (verbose)
            {
                System.out.println("Attempting to configure Log4J");
                System.out.println("Resource URL for log4j.xml is: " +
                    this.getClass().getClassLoader().getResource(resourceName));
                System.out.println("Class loader used is: " + this.getClass().getClassLoader());
            }

            // Set up Log4J
            DOMConfigurator.configure(this.getClass().getClassLoader().getResource(resourceName));

            // Print out a log statement to show that Log4J was correctly configured
            Logger log = Logger.getLogger(Log4JConfigBean.class);

            log.debug("Log4J configured and working");

            // Set configured flag to true
            configured = true;
        }
        catch (Exception e)
        {
            // Set configured flag to false
            configured = false;

            // Rethrow this as a config exception
            throw new ConfigException("Exception whilst configuring Log4J: " + e.getMessage(), e, null, null);
        }
    }
}
