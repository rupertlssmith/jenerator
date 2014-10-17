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

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Iterator;

import com.thesett.catalogue.model.Catalogue;
import com.thesett.catalogue.model.ViewType;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.thesett.aima.attribute.impl.EnumeratedStringAttribute;
import com.thesett.aima.attribute.impl.HierarchyAttribute;
import com.thesett.aima.attribute.impl.HierarchyType;
import com.thesett.aima.state.ComponentType;
import com.thesett.aima.state.Type;
import com.thesett.common.config.ConfigBeanContext;
import com.thesett.common.config.ConfigException;
import com.thesett.common.util.ReflectionUtils;
import com.thesett.common.util.StringUtils;
import com.thesett.index.config.IndexStoreConfigBean;

/**
 * CatalogueConfigBeanImpl performs application start-up time configurations to prepare a catalogue model for use. It
 * loads and validates the in-memory knowledge level model of the catalogue.
 *
 * <p/>'Reference types' are types that specify a restricted set of values that are enumerated in a database. For
 * example enumerations such as { male; female } or hierarchical labelings and so on are called reference types.
 *
 * <p/>The start-up configuration consists of checking that the reference types have been created and populated in the
 * database. If the database has been created from scratch, then the reference tables need to be filled in (this is
 * usually only done during testing against a temporary database). For existing reference tables, the data is loaded
 * into memory for each type, to establish type in memory and possibly finalize it.
 *
 * <p/>This config bean depends on {@link ModelLoaderConfigBean}, {@link HibernateConfigBean}, {@link ModeConfigBean}
 * and {@link IndexStoreConfigBean} having been succesfully configured first.
 *
 * <p/>Configuration is handled differently depending on the value of the development mode switch:
 *
 * <ul>
 * <li>In development mode the database is created (by {@link HibernateConfigBean}) and any reference types, are
 * populated by this bean. The database will always be empty at the end of the catalogue configuration.
 * <li>In production mode the database schema is verified against the catalogue model and reference type data is
 * verified against the catalogue model and loaded into memory by this bean. At the moment the free text search
 * {@link com.thesett.index.Index} implementations used do not persist their data so all the data in the database is
 * re-indexed during configuration too. Once persistent indexes are implemented this step will be changed to an optional
 * verification step that checks that the indexes and database are in synch and corrects any mismatches.</li>
 * </ul>
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Transform the raw catalogue model into the knowledge level catalogue model.
 * <tr><td> Verify or populate the database reference types.
 * <tr><td> Re-index all text search indexes.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class CatalogueConfigBeanImpl implements Serializable, CatalogueConfigBean
{
    /** Used for logging. */
    private static final Logger log = Logger.getLogger(CatalogueConfigBeanImpl.class);

    /** Flag used to indicate that this config bean has been succesfully run. */
    private boolean configured = false;

    /** Used to hold the package name under which the model has been generated. */
    private String modelPackage;

    /** Holds the catalogue as a first order logic model. */
    private Catalogue model;

    /** Holds a referece to the hibernate config bean to get hibernate sessions from. */
    protected HibernateConfigBean hibernateBean;

    /**
     * Checks whether or not the config bean has been succesfully run and is in a configured state.
     *
     * @return True if the config bean has run its configuration succesfully.
     */
    public boolean getConfigured()
    {
        return configured;
    }

    /** {@inheritDoc} */
    public Catalogue getCatalogue()
    {
        return model;
    }

    /** {@inheritDoc} */
    public String getModelPackage()
    {
        return modelPackage;
    }

    /** {@inheritDoc} */
    public void setModelPackage(String packageName)
    {
        this.modelPackage = packageName;
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

        // Ensure that the catalogue model loader config bean has been run, and get a reference to it.
        ModelLoaderConfigBean loaderBean =
            (ModelLoaderConfigBean) configBeanContext.getConfiguredBean(
                "com.thesett.catalogue.config.ModelLoaderConfigBean");

        // Ensure that the hibernate config bean has been set up, and get a reference to it.
        hibernateBean =
            (HibernateConfigBean) configBeanContext.getConfiguredBean(
                "com.thesett.catalogue.config.HibernateConfigBean");

        // Ensure that the mode config bean has been set up, and get a reference to it.
        ModeConfigBean modeBean =
            (ModeConfigBean) configBeanContext.getConfiguredBean("com.thesett.catalogue.config.ModeConfigBean");

        // Ensure that the index store config bean has been set up, and get a reference to it.
        IndexStoreConfigBean indexBean =
            (IndexStoreConfigBean) configBeanContext.getConfiguredBean("com.thesett.index.config.IndexStoreConfigBean");

        // Create the catalogue logical model from the raw model.
        model = loaderBean.getCatalogue();

        // Set up a static references to the component types on each component in the model.
        initializeAllTypes();

        // Check if running in development mode.
        if (modeBean.isDevMode())
        {
            log.debug("In dev mode, creating reference types in the database.");

            createReferenceTypes();
        }

        // Not in development mode so running in production mode.
        else
        {
            log.warn("Todo: In production mode, loading and checking reference types from the database.");
        }

        // Rebuild the indexes from the database if necessary.
        if (!modeBean.isDevMode())
        {
            log.warn("Todo: In production mode, re-building indexes from the database.");
        }
    }

    /**
     * Passes the catalogue model to all component type class implementations, so that they can reference back to their
     * component types in the knowledge level of the catalogue model.
     */
    private void initializeAllTypes()
    {
        log.debug("private void initializeComponentTypes(): called");

        for (ComponentType componentType : model.getAllComponentTypes())
        {
            setStaticCatalogue(componentType);
        }

        for (HierarchyType hierarchyType : model.getAllHierarchyTypes())
        {
            setStaticCatalogue(hierarchyType);
        }

        for (EnumeratedStringAttribute.EnumeratedStringType enumerationType : model.getAllEnumTypes())
        {
            setStaticCatalogue(enumerationType);
        }
    }

    /**
     * Calls the static 'setCatalogue' method on a type, so that the type can reach the catalogue.
     *
     * @param type The type to call the static 'setCatalogue' method on.
     */
    private void setStaticCatalogue(Type type)
    {
        try
        {
            String typeName = type.getName();
            String typeClassName = StringUtils.toCamelCaseUpper(typeName);

            Class clz = null;

            if (type instanceof ViewType)
            {
                clz = ReflectionUtils.forName(model.getModelPackage() + "." + typeClassName + "Impl");
            }
            else
            {
                clz = ReflectionUtils.forName(model.getModelPackage() + "." + typeClassName);
            }

            //Class clz = type.getBaseClass();

            Method setCatalogueMethod = clz.getMethod("setCatalogue", Catalogue.class);
            ReflectionUtils.callStaticMethod(setCatalogueMethod, new Object[] { model });
        }
        catch (NoSuchMethodException e)
        {
            throw new RuntimeException(e);
        }
    }

    /** Initialized all reference types in the database. */
    private void createReferenceTypes()
    {
        createHierarchyReferenceTypes();
        createEnumerationReferenceTypes();
    }

    /** Initializes all hierarchy reference types in the database. */
    private void createHierarchyReferenceTypes()
    {
        log.debug("private void createHierarchyReferenceTypes(): called");

        for (HierarchyType hierarchyType : model.getAllHierarchyTypes())
        {
            String hierarchyName = hierarchyType.getName();
            String hierarchyClassName = StringUtils.toCamelCaseUpper(hierarchyName);

            Class theBeanClass = ReflectionUtils.forName(model.getModelPackage() + "." + hierarchyClassName);

            for (Iterator<HierarchyAttribute> hierarchyIterator = hierarchyType.getAllPossibleValuesIterator(false);
                    hierarchyIterator.hasNext();)
            {
                HierarchyAttribute hierarchyAttribute = hierarchyIterator.next();

                Session session = hibernateBean.getSecondarySession();
                Transaction transaction = session.beginTransaction();

                // Create an instance of the hierarchy bean class using a constructor on the hierarchy value.
                Class[] arguments = new Class[] { HierarchyAttribute.class };
                Constructor beanConstructor = ReflectionUtils.getConstructor(theBeanClass, arguments);

                Object theBean = ReflectionUtils.newInstance(beanConstructor, new Object[] { hierarchyAttribute });

                log.debug("Created hierarchy bean: " + theBean);

                // Store the hierarchy value in the database.
                session.save(theBean);

                transaction.commit();
                session.close();
            }
        }
    }

    /** Initializes all enumeration reference types in the database. */
    private void createEnumerationReferenceTypes()
    {
        log.debug("private void createEnumerationReferenceTypes(): called");

        for (EnumeratedStringAttribute.EnumeratedStringType enumType : model.getAllEnumTypes())
        {
            for (Iterator<EnumeratedStringAttribute> enumIterator = enumType.getAllPossibleValuesIterator(false);
                    enumIterator.hasNext();)
            {
                EnumeratedStringAttribute enumAttribute = enumIterator.next();
                String enumName = enumAttribute.getType().getName();
                String enumClassName = StringUtils.toCamelCaseUpper(enumName);

                Session session = hibernateBean.getSecondarySession();
                Transaction transaction = session.beginTransaction();

                // Create an instance of the enumeration bean class using a constructor on the enumeration value.
                Class theBeanClass = ReflectionUtils.forName(model.getModelPackage() + "." + enumClassName);

                Class[] arguments = new Class[] { EnumeratedStringAttribute.class };
                Constructor beanConstructor = ReflectionUtils.getConstructor(theBeanClass, arguments);

                Object theBean = ReflectionUtils.newInstance(beanConstructor, new Object[] { enumAttribute });

                log.debug("Created enum bean: " + theBean);

                // Store the hierarchy value in the database.
                session.save(theBean);

                transaction.commit();
                session.close();
            }
        }
    }
}
