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
package com.thesett.catalogue.model.impl;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.log4j.Logger;

import com.thesett.catalogue.config.ModelLoaderConfigBean;
import com.thesett.catalogue.core.ConfiguratorTestBase;
import com.thesett.catalogue.model.Catalogue;
import com.thesett.catalogue.model.base.CatalogueTestBase;
import com.thesett.catalogue.setup.CatalogueDefinition;
import com.thesett.common.config.ConfigBeanContext;

/**
 * CatalogueTest creates a {@link com.thesett.catalogue.core.CatalogueModelFactory} and checks it against the raw model
 * using the tests defined in {@link com.thesett.catalogue.model.base.CatalogueTestBase}.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Check that the catalogue model passes checking against the raw model.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class CatalogueModelTest extends CatalogueTestBase
{
    /** Used for debugging. */
    private static final Logger log = Logger.getLogger(CatalogueTestBase.class);

    /** Holds the resource name of the test configuration. */
    private static final String TEST_CONFIG = "testconfig.xml";

    public CatalogueModelTest(String name, Catalogue catalogue, CatalogueDefinition definition)
    {
        super(name, catalogue, definition);
    }

    /**
     * Compile all the tests for the default tests for unifiers into a suite, plus the tests defined in this class.
     *
     * @return A test suite.
     */
    public static Test suite() throws Exception
    {
        // Build a new test suite.
        TestSuite suite = new TestSuite("Catalogue Tests");

        ConfigBeanContext configBeanContext = ConfiguratorTestBase.configure(TEST_CONFIG, false);

        // Take a reference to the model loader bean to get the catalogue model from.
        ModelLoaderConfigBean loaderBean =
            (ModelLoaderConfigBean) configBeanContext.getConfiguredBean(
                "com.thesett.catalogue.config.ModelLoaderConfigBean");

        // Get the catalogue to test.
        Catalogue catalogue = loaderBean.getCatalogue();

        // Get the raw model to test against.
        CatalogueDefinition definition = loaderBean.getModel();

        suite.addTest(new CatalogueModelTest("testAllComponentsInCatalogue", catalogue, definition));
        suite.addTest(new CatalogueModelTest("testAllComponentsInRawModel", catalogue, definition));
        suite.addTest(new CatalogueModelTest("testAllHierarchiesInRawModelAreInCatalogue", catalogue, definition));
        suite.addTest(new CatalogueModelTest("testAllHierarchiesInCatalogueAreInRawModel", catalogue, definition));

        //suite.addTest(new CatalogueModelTest("testAllComponentFieldsAllProductsInCatalogue", catalogue, definition));
        //suite.addTest(new CatalogueModelTest("testAllFieldsAllProductsInRawModel", catalogue, definition));

        suite.addTest(new CatalogueModelTest("testAllDateFieldsAllProductsInCatalogue", catalogue, definition));
        suite.addTest(new CatalogueModelTest("testAllEnumerationFieldsAllProductsInCatalogue", catalogue, definition));
        suite.addTest(new CatalogueModelTest("testAllHierarchyFieldsAllProductsInCatalogue", catalogue, definition));
        suite.addTest(new CatalogueModelTest("testAllBooleanFieldsAllProductsInCatalogue", catalogue, definition));
        suite.addTest(new CatalogueModelTest("testAllIntegerFieldsAllProductsInCatalogue", catalogue, definition));

        //suite.addTest(new CatalogueModelTest("testAllRealFieldsAllProductsInCatalogue", catalogue, definition));
        suite.addTest(new CatalogueModelTest("testAllStringFieldsAllProductsInCatalogue", catalogue, definition));
        suite.addTest(new CatalogueModelTest("testAllTimeFieldsAllProductsInCatalogue", catalogue, definition));

        suite.addTest(new CatalogueModelTest("testAllTypeDefsInRawModel", catalogue, definition));
        suite.addTest(new CatalogueModelTest("testAllTypeDefsInCatalogue", catalogue, definition));

        return suite;
    }
}
