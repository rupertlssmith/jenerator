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
package com.thesett.common.webapp.servlets;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.log4j.Logger;

import com.thesett.common.config.ConfigException;
import com.thesett.common.config.Configurator;

/**
 * ConfigServlet loads and configures config beans at application startup time. Its service method is not implemented
 * as it is not intented to be used as a normal servlet but merely as a convenient place to load config beans. It
 * should normally be set to load on startup and be the first servlet loaded.
 *
 * <p/>Here is an example of how to set it up in the web.xml. This assumes that the configuration file has been placed
 * in a file called 'config.xml' which can be found in the root of the classpath of the WAR file.
 *
 * <p/><pre>
 * &lt;servlet&gt;
 *   &lt;servlet-name&gt;configservlet&lt;/servlet-name&gt;
 *   &lt;servlet-class&gt;com.thesett.common.webapp.servlets.ConfigServlet&lt;/servlet-class&gt;
 *   &lt;init-param&gt;
 *     &lt;param-name&gt;configResource&lt;/param-name&gt;
 *     &lt;param-value&gt;config.xml&lt;/param-value&gt;
 *   &lt;/init-param&gt;
 *   &lt;load-on-startup&gt;1&lt;/load-on-startup&gt;
 * &lt;/servlet&gt;
 * </pre>
 *
 * <p/>Another way to execute start up code when the web container is started would be to implement a
 * ServletContextListener. There is no way to pass parameters to one in the web.xml so the servlet approach has been
 * used instead.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Run the configurator using the specified configuration resource.
 * </table></pre>
 *
 * @todo Could make this servlet return some status information about the config beans if its service method is called.
 *
 * @author Rupert Smith
 */
public class ConfigServlet extends HttpServlet
{
    /** Used for logging. */
    private static final Logger log = Logger.getLogger(ConfigServlet.class);

    /** The name of the config resource parameter. */
    public static final String CONFIG_RESOURCE = "configResource";

    /**
     * Runs the configurator.
     *
     * @throws ServletException If the Log4J config bean fails to configure Log4J.
     */
    public void init() throws ServletException
    {
        log.debug("public void init(): called");

        // Get the name of the configuration resource from the servlet intialization parameters.
        String configResource = getServletConfig().getInitParameter(CONFIG_RESOURCE);

        // Run the configurator.
        Configurator configurator = new Configurator(configResource);

        try
        {
            // Load the config beans.
            configurator.loadConfigBeans();

            // Set up all the loaded config beans.
            configurator.configureAll();
        }
        catch (ConfigException e)
        {
            log.warn("There was a configuration exception whilst loading or running the config beans.", e);
            throw new ServletException(e);
        }
    }

    /**
     * Tells the configurator to remove all its config beans from the JNDI context in which they are registered.
     */
    public void destroy()
    {
        // Let the super class do its clean up work if it needs to.
        super.destroy();

        // Try to get a reference to the configurator and tell it to remove all its confugred beans from the JNDI
        // context where they are registered.
        Configurator configurator = Configurator.lookupConfigurator();
        configurator.removeAll();
    }
}
