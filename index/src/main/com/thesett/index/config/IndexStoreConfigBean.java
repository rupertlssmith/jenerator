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
package com.thesett.index.config;

import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;

import com.thesett.common.config.ConfigBean;
import com.thesett.common.config.ConfigBeanContext;
import com.thesett.common.config.ConfigException;
import com.thesett.common.util.FileUtils;
import com.thesett.common.util.ReflectionUtils;
import com.thesett.common.util.StringUtils;
import com.thesett.index.IndexMapping;
import com.thesett.index.IndexSetup;
import com.thesett.index.IndexStore;
import com.thesett.index.prototype.ProtoIndexStore;
import com.thesett.index.setup.FieldType;
import com.thesett.index.setup.IndexConfigurationType;
import com.thesett.index.setup.IndexConfigurations;
import com.thesett.index.setup.MappingType;
import com.thesett.index.setup.StopWordsBaseType;
import com.thesett.index.setup.StopWordsType;
import com.thesett.index.setup.SynonymsBaseType;
import com.thesett.index.setup.SynonymsType;

/**
 * IndexStoreConfigBean is a {@link com.thesett.common.config.ConfigBean} for setting up a pre-built
 * {@link com.thesett.index.IndexStore} that contains indexes setup from a configuration XML file. The configuration XML
 * file specifies the stop-words, synonyms and type mappings for a set of indexes that are made available in the index
 * store.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Report succesfull configuration of the index store.
 * <tr><td> Perform configuration of the index store from a parsed configuration file.
 * </table></pre>
 *
 * @author Rupert Smith
 * @todo   This only supports {@link com.thesett.index.prototype.ProtoIndex} and
 *         {@link com.thesett.index.prototype.ProtoIndexStore} at the moment. Different implementations to be added,
 *         either by passing a parameter to this config bean or in the configuration file itself.
 */
public class IndexStoreConfigBean implements ConfigBean, Serializable
{
    /** Used for logging. */
    private static final Logger log = Logger.getLogger(IndexStoreConfigBean.class);

    /** Defines the default resource to use to configure the index. */
    private static final String RESOURCE_NAME = "index-store.xml";

    /** Holds resource name to use to configure the service locators. */
    private String resourceName = RESOURCE_NAME;

    /** Holds the index store that this bean configures. */
    private IndexStore indexStore;

    /** Flag to represent configuration status of this configure bean. */
    private final boolean configured = false;

    /**
     * Tells the bean to perform whatever configuration it is intended to do.
     *
     * @param  force             Setting this to true tells the config bean to re-run its configuration action even if
     *                           it has already been run.
     * @param  configBeanContext A reference to the configurator that is managing the whole configuration process.
     *
     * @throws ConfigException If some error occurs that means that the configuration cannot be succesfully completed.
     */
    public void doConfigure(boolean force, ConfigBeanContext configBeanContext) throws ConfigException
    {
        log.debug("public void doConfigure(boolean force) throws ConfigException");

        // Check that all the mandatory configuration parameters are set.
        // There are none at the moment.

        // Check if the configuration has already been done but the force flag is false, in which case return without
        // doing the configuration.
        if (!force && configured)
        {
            log.debug("Already configured and force not set, so returning without doing any configuration.");

            return;
        }

        // Open the specified resource and unmarshal the index configurations from it.
        IndexConfigurations indexConfigurations;

        try
        {
            Reader configReader =
                new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream(getResourceName()));

            // Open the specified resource and unmarshal the template from it.
            JAXBContext jc = JAXBContext.newInstance("com.thesett.index.setup");
            Unmarshaller u = jc.createUnmarshaller();
            indexConfigurations = (IndexConfigurations) u.unmarshal(configReader);
        }
        catch (JAXBException e)
        {
            throw new ConfigException("The configuration resource, " + getResourceName() + " cannot be unmarshalled", e,
                null, null);
        }

        // Perform validation on the configurations that are not supported by the schema validation itself.
        // This consists of identifying that any external resources are present, such as stop-words and synonym
        // files, and checking that any referenced stop-words and synonyms definitions at the top level of
        // the configuration file match the references given by name in the index configurations. It also
        // checks that the mapped class types are valid sub-types of the base record and summary class types
        // for the index.
        String errorMessages = validate(indexConfigurations);

        // Check if there were any error messages and throw them inside a config exception if there were.
        if ((errorMessages != null) && !"".equals(errorMessages))
        {
            throw new ConfigException("The configuration is not valid: " + errorMessages, null, null, null);
        }

        // Used to hold top-level stop-words definitions by name.
        Map<String, String[]> stopWordsDefs = new HashMap<String, String[]>();

        // Used to hold top-level synonyms definitions by name.
        //Map<String, Map<String, Set<String>>> synonymsDefs = new HashMap<String, Map<String, Set<String>>>();

        // Cache any stop-words definitions by name.
        for (StopWordsType stopWordsDef : indexConfigurations.getStopWordsDef())
        {
            String refName = stopWordsDef.getRefName();

            // Check if the definition uses an external file and load the stop words from it if so.
            String fileName = stopWordsDef.getFile();
            String stopWords;

            if (fileName != null)
            {
                stopWords = FileUtils.readFileAsString(fileName);
            }

            // Otherwise read the stop words from the element content.
            else
            {
                stopWords = stopWordsDef.getValue();
            }

            // Reduce the stop words to a set of words to remove any duplicates and to treat punctuation, stemming etc.
            String[] stopWordsArray = StringUtils.listToArray(stopWords, "");

            // Cache the stop words definition.
            stopWordsDefs.put(refName, stopWordsArray);
        }

        // Cache any synonyms definitions by name.
        /*for (SynonymsType synonymDefs : indexConfigurations.getSynonymsDef())
        {
            // Check if the definition uses an external file and load the synonyms from it if so.
            {
            }

            // Otherwise read the synonyms from the element content.
            {
            }

            // Check if reverse mapping is used and transform the synonyms so that all synonyms defined on a line
            // map to each other.

            // Cache the synonyms definition.
        }*/

        // Create the index store.
        indexStore = new ProtoIndexStore();

        // Loop through all the index configurations adding each one to the index store
        for (IndexConfigurationType nextIndexConfig : indexConfigurations.getIndexConfiguration())
        {
            // Get the index name and create a new index (a proto index, different implementations to be added).
            String indexName = nextIndexConfig.getName();
            IndexSetup indexSetup = indexStore.getNamedIndexSetup(indexName);
            //TransactionalIndex index = indexStore.getNamedIndex(indexName);

            // Put the index into read committed mode by default.
            //index.setTransactionalMode(TransactionalIndex.IsolationLevel.ReadCommitted);

            // Extract all the mappings and add them to the index.
            for (MappingType nextMapping : nextIndexConfig.getMapping())
            {
                // Get all the mapped record fields.
                String[] fieldNames = new String[nextMapping.getRecordClass().getField().size()];

                int i = 0;

                for (FieldType nextField : nextMapping.getRecordClass().getField())
                {
                    fieldNames[i++] = nextField.getName();
                }

                // Get the mapped ratings field.
                String ratingsField = nextMapping.getSummaryClass().getRatingField().getName();

                // Get the record class and the summary class.
                Class recordClass;
                Class summaryClass;

                try
                {
                    recordClass = Class.forName(nextMapping.getRecordClass().getName());
                    summaryClass = Class.forName(nextMapping.getSummaryClass().getName());
                }

                // This should not happen as the verification step has already confirmed that these classes do exist
                // and can be found, so this is rethrown as a config exception.
                catch (ClassNotFoundException e)
                {
                    throw new ConfigException("Class not found whilst loading record or summary class. " +
                        "Should not happen as validation step has already confirmed these classes.", e, null, null);
                }

                // Build the mapping object for these classes.
                IndexMapping mapping = new IndexMapping(fieldNames, ratingsField);

                // Add the mapping to the index.
                indexSetup.addMapping(recordClass, summaryClass, mapping);
            }

            // Get all the stop-words and add them to the index.
            // Also add any referenced top level stop-words.
            for (IndexConfigurationType.StopWordsRef swRef : nextIndexConfig.getStopWordsRef())
            {
                String[] stopWords = stopWordsDefs.get(swRef.getRefName());
                indexSetup.setStopWords(Arrays.asList(stopWords));
            }

            // Get all the synonyms and add them to the index.
            // Also add any referenced to level synonyms.
        }
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
     * Gets the name of the resource to load the index config file from.
     *
     * @return The name of the resource to load the index config file from.
     */
    public String getResourceName()
    {
        return resourceName;
    }

    /**
     * Sets the name of the resource to load the index config file from.
     *
     * @param resourceName The name of the resource to load the index config file from.
     */
    public void setResourceName(String resourceName)
    {
        this.resourceName = resourceName;
    }

    /**
     * Gets the index store configured by this config bean.
     *
     * @return The index store configured by this config bean.
     */
    public IndexStore getIndexStore()
    {
        return indexStore;
    }

    /**
     * Validates the index configurations, returning any error messages in a string.
     *
     * @param  configurations The configurations to validate.
     *
     * @return Any error messages, empty string or null if there are none.
     */
    protected String validate(IndexConfigurations configurations)
    {
        String errorMessages = "";

        // Used to hold all the stop-words definition names.
        Set<String> stopWordsRefNames = new HashSet<String>();

        // Used to hold all the synonyms definition names.
        Set<String> synonymsRefNames = new HashSet<String>();

        // Validate the external files for all top-level stop words definitions.
        for (StopWordsType nextStopWords : configurations.getStopWordsDef())
        {
            errorMessages += validateStopWordsBaseType(nextStopWords);

            // Add the reference name to the set of names for later checking references against.
            stopWordsRefNames.add(nextStopWords.getRefName());
        }

        // Validate the external files for all top-level synonyms definitions.
        for (SynonymsType nextSynonyms : configurations.getSynonymsDef())
        {
            errorMessages += validateSynonymsBaseType(nextSynonyms);

            // Add the reference name to the set of names for later checking references against.
            synonymsRefNames.add(nextSynonyms.getRefName());
        }

        // Validate the index configurations.
        for (IndexConfigurationType nextIndexConfig : configurations.getIndexConfiguration())
        {
            // Check that the key class exists.
            if (!ReflectionUtils.classExistsAndIsLoadable(nextIndexConfig.getKeyBaseClass().getName()))
            {
                errorMessages +=
                    "The key class, " + nextIndexConfig.getKeyBaseClass().getName() + ", cannot be found.\n";
            }

            // Check that the base record class exists.
            String baseRecordClassName = nextIndexConfig.getRecordBaseClass().getName();

            if (!ReflectionUtils.classExistsAndIsLoadable(baseRecordClassName))
            {
                errorMessages += "The record base class, " + baseRecordClassName + ", cannot be found.\n";
            }

            // Check that the base summary class exists.
            String baseSummaryClassName = nextIndexConfig.getSummaryBaseClass().getName();

            if (!ReflectionUtils.classExistsAndIsLoadable(baseSummaryClassName))
            {
                errorMessages += "The summary base class, " + baseSummaryClassName + ", cannot be found.\n";
            }

            // Validate all the mappings.
            for (MappingType nextMapping : nextIndexConfig.getMapping())
            {
                // Check that the record class is a sub-type of the base record class.
                String recordClassName = nextMapping.getRecordClass().getName();

                if (!ReflectionUtils.isSubTypeOf(baseRecordClassName, recordClassName))
                {
                    errorMessages +=
                        "The mapped class, " + recordClassName + ", is not a subtype of the base record class, " +
                        baseRecordClassName + ".\n";
                }

                // Check that the summary class is a sub-type of the base summary class.
                String summaryClassName = nextMapping.getSummaryClass().getName();

                if (!ReflectionUtils.isSubTypeOf(baseSummaryClassName, summaryClassName))
                {
                    errorMessages +=
                        "The mapped summary class, " + summaryClassName +
                        ", is not a subtype of the base summary class, " + baseSummaryClassName + ",\n";
                }
            }

            // Validate the external files for all stop words definitions.
            for (StopWordsBaseType nextStopWords : nextIndexConfig.getStopWords())
            {
                errorMessages += validateStopWordsBaseType(nextStopWords);
            }

            // Validate all stop words references against the top level definitions.
            for (IndexConfigurationType.StopWordsRef nextStopWordsRef : nextIndexConfig.getStopWordsRef())
            {
                if (!stopWordsRefNames.contains(nextStopWordsRef.getRefName()))
                {
                    errorMessages +=
                        "The stop words reference, " + nextStopWordsRef.getRefName() + ", is not defined.\n";
                }
            }

            // Validate the external files for all synonyms definitions.
            for (SynonymsBaseType nextSynonyms : nextIndexConfig.getSynonyms())
            {
                errorMessages += validateSynonymsBaseType(nextSynonyms);
            }

            // Validate all synonyms references against the top level definitions.
            for (IndexConfigurationType.SynonymsRef nextSynonymsRef : nextIndexConfig.getSynonymsRef())
            {
                if (!synonymsRefNames.contains(nextSynonymsRef.getRefName()))
                {
                    errorMessages += "The synonyms reference, " + nextSynonymsRef.getRefName() + ", is not defined.\n";
                }
            }
        }

        return errorMessages;
    }

    /**
     * Performs external resource validation of a stop-words definition. If the stop-words definition references an
     * external file, checks that that file is available as a resource on the classpath.
     *
     * @param  stopWords The stop-words definition to validate.
     *
     * @return Any error messages, empty string or null if there are none.
     */
    protected String validateStopWordsBaseType(StopWordsBaseType stopWords)
    {
        if (stopWords.getFile() != null)
        {
            if (!externalResourceExists(stopWords.getFile()))
            {
                return "The stop words file: " + stopWords.getFile() + " cannot be found.\n";
            }
        }

        return "";
    }

    /**
     * Performs external resource validation of a synonyms definition. If the synonyms definition references an external
     * file, checks that that file is available as a resource on the classpath.
     *
     * @param  synonyms The synonyms definition to validate.
     *
     * @return Any error messages, empty string or null if there are none.
     */
    protected String validateSynonymsBaseType(SynonymsBaseType synonyms)
    {
        if (synonyms.getFile() != null)
        {
            if (!externalResourceExists(synonyms.getFile()))
            {
                return "The synonyms file: " + synonyms.getFile() + " cannot be found.\n";
            }
        }

        return "";
    }

    /**
     * Checks that the named resource can be found using the resource loading capabilities of the class loader on the
     * current class path.
     *
     * @param  resourceName The name of the resource file.
     *
     * @return <tt>true</tt> if the named resource exists, <tt>flase</tt> otherwise.
     */
    protected boolean externalResourceExists(String resourceName)
    {
        return !(this.getClass().getClassLoader().getResource(resourceName) == null);
    }
}
