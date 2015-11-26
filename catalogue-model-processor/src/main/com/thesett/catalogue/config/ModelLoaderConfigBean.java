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
package com.thesett.catalogue.config;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import com.thesett.aima.logic.fol.Clause;
import com.thesett.aima.logic.fol.interpreter.ResolutionEngine;
import com.thesett.aima.logic.fol.prolog.PrologCompiledClause;
import com.thesett.aima.logic.fol.prolog.PrologEngine;
import com.thesett.catalogue.core.CatalogueModelFactory;
import com.thesett.catalogue.model.Catalogue;
import com.thesett.catalogue.model.impl.CatalogueModel;
import com.thesett.catalogue.setup.CatalogueDefinition;
import com.thesett.common.config.ConfigBean;
import com.thesett.common.config.ConfigBeanContext;
import com.thesett.common.config.ConfigException;
import com.thesett.common.parsing.SourceCodeException;

/**
 * The purpose of ModelLoaderConfigBean is to load and validate a catalogue setup configuration file. Much of the
 * validation of such a file is handled by its XML schema, but not all of it. There are some things that need to be
 * checked that cannot be handled by a schema.
 *
 * <p/>One of the 'modelFile' or 'modelResource' fields must be set on this bean. If both are set, the file name is
 * taken in preference to the resource name.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th>Responsibilities <th>Collaborations
 * <tr><td>Load and validate a catalogue model.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class ModelLoaderConfigBean implements ConfigBean, Serializable
{
    /** Flag used to indicate that this config bean has been succesfully run. */
    private boolean configured;

    /** Holds the name of the file to load the catalogue model from. */
    private String modelFile;

    /** Holds the name of the resoruce to load the catalogue model from. */
    private String modelResource;

    /** Holds the optional name of a file to output the raw model as logical functors to. */
    private String debugRawFileName;

    /** Holds the raw catalogue model. */
    private CatalogueDefinition catalogueDefinition;

    /** Holds the catalogue as a first order logic model. */
    private CatalogueModel model;

    /**
     * Holds the factory that was used to load the catalogue. Note that this is transient as config beans must be
     * serializeable. It is usefull for testing to be able to obtain a reference to the factory so this field has been
     * left in for that reason only. If the bean is stored in a JNDI namespace the factory may become null upon
     * recovery.
     */
    private transient CatalogueModelFactory modelFactory;

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
     * Ensures that all hierarchy attribute classes are established in the database and in memory.
     *
     * @param  force             Setting this to true tells the config bean to re-run its configuration action even if
     *                           it has already been run.
     * @param  configBeanContext A reference to the configurator that is managing the whole configuration process.
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

        // Open the specified resource and unmarshal the catalogue model from it.
        CatalogueDefinition catalogueDefinition;

        // Used to hold a writer to send the raw model to for debugging purposes.
        Writer rawModelWriter = null;

        try
        {
            JAXBContext jc = JAXBContext.newInstance("com.thesett.catalogue.setup");
            Unmarshaller u = jc.createUnmarshaller();

            if (modelFile != null)
            {
                catalogueDefinition = (CatalogueDefinition) u.unmarshal(new File(modelFile));
            }
            else if (modelResource != null)
            {
                catalogueDefinition =
                    (CatalogueDefinition) u.unmarshal(this.getClass().getClassLoader().getResourceAsStream(
                            modelResource));
            }
            else
            {
                throw new ConfigException(
                    "One of the 'modelResource' and 'modelFile' fields must be set on the ModelLoaderConfigBEan.", null,
                    null, null);
            }

            // Create a first order logic resolution engine to perform the type checking with.
            ResolutionEngine<Clause, PrologCompiledClause, PrologCompiledClause> engine = new PrologEngine();
            engine.reset();

            // Create a file to send the raw model to, if one was specified on the command line.
            if (debugRawFileName != null)
            {
                rawModelWriter = new FileWriter(debugRawFileName);
            }

            // Create the catalogue logical model from the raw model and run its type checker.
            modelFactory = new CatalogueModelFactory(engine, catalogueDefinition, rawModelWriter);
            model = modelFactory.initializeModel();

            // Flush and close the model writer if one was created.
            if (rawModelWriter != null)
            {
                rawModelWriter.flush();
                rawModelWriter.close();
            }

            // Keep the loaded model.
            this.catalogueDefinition = catalogueDefinition;

            // Configuration was succesfull so set the configured flag.
            configured = true;
        }
        catch (JAXBException e)
        {
            throw new ConfigException("The configuration cannot be unmarshalled from " + modelFile + ".", e, null,
                null);
        }
        catch (IOException e)
        {
            throw new ConfigException("Got an IO exception whilst openening the debug file '" + debugRawFileName +
                "' for the raw model.", e, null, null);
        }
        catch (SourceCodeException e)
        {
            throw new ConfigException("Got a SourceCodeException whilst creating the catalogue model.", e, null, null);
        }
    }

    /**
     * Returns the loaded catalogue model.
     *
     * @return The catalogue model loaded by this config bean.
     */
    public CatalogueDefinition getModel()
    {
        return catalogueDefinition;
    }

    /**
     * Gets the catalogue knowledge level model that this config bean has created from the raw catalogue model.
     *
     * @return The catalogue knowledge level model.
     */
    public Catalogue getCatalogue()
    {
        return model;
    }

    /**
     * Provides the factory that was used to load the catalogue.
     *
     * @return The factory that was used to load the catalogue.
     */
    public CatalogueModelFactory getCatalogueFactory()
    {
        return modelFactory;
    }

    /**
     * Gets the resource name to load the catalogue model from.
     *
     * @return The resource name to load the catalogue model from.
     */
    public String getModelResource()
    {
        return modelResource;
    }

    /**
     * Sets the resource to load the catalogue model from (as an XML file).
     *
     * @param modelResource The resource to load the catalogue model from (as an XML file).
     */
    public void setModelResource(String modelResource)
    {
        this.modelResource = modelResource;
    }

    /**
     * Sets the file to load the catalogue model from (as an XML file).
     *
     * @param fileName The file to load the catalogue model from (as an XML file).
     */
    public void setModelFile(String fileName)
    {
        this.modelFile = fileName;
    }

    /**
     * Gets the file name to load the catalogue model from.
     *
     * @return The file name to load the catalogue model from.
     */
    public String getModelFile()
    {
        return modelFile;
    }

    /**
     * Sets the name of the file to send the raw model as logicla functors to.
     *
     * @param debugRawFileName The file to write to.
     */
    public void setDebugRawFileName(String debugRawFileName)
    {
        this.debugRawFileName = debugRawFileName;
    }
}
