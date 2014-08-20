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
package com.thesett.catalogue.core;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.NDC;

import com.thesett.aima.attribute.impl.EnumeratedStringAttribute;
import com.thesett.aima.attribute.impl.HierarchyAttribute;
import com.thesett.aima.attribute.time.DateOnly;
import com.thesett.aima.attribute.time.TimeOnly;
import com.thesett.aima.state.ComponentType;
import com.thesett.aima.state.Type;
import com.thesett.catalogue.interfaces.Catalogue;
import com.thesett.catalogue.setup.BooleanType;
import com.thesett.catalogue.setup.CatalogueDefinition;
import com.thesett.catalogue.setup.ComponentDefType;
import com.thesett.catalogue.setup.DateType;
import com.thesett.catalogue.setup.DimensionDefType;
import com.thesett.catalogue.setup.EnumerationType;
import com.thesett.catalogue.setup.FieldDeclrType;
import com.thesett.catalogue.setup.HierarchyDefType;
import com.thesett.catalogue.setup.HierarchyType;
import com.thesett.catalogue.setup.IntegerType;
import com.thesett.catalogue.setup.RealType;
import com.thesett.catalogue.setup.SetupModelHelper;
import com.thesett.catalogue.setup.StringType;
import com.thesett.catalogue.setup.TimeType;
import com.thesett.catalogue.setup.TypeDefType;

/**
 * CatalogueTestBase tests a power-typed catalogue against a raw catalogue model, and runs many checks against it to
 * ensure that the catalogue is correctly created from the model; that the catalogue type checks correctly, and that it
 * contains all of the types defined in the raw model.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Check that all component types in the raw model are present in the catalogue.
 * <tr><td> Check that all component types listed by the catalogue are present in the raw model.
 * <tr><td> Check that all top level type definitions in the raw model are present in the catalogue.
 * <tr><td> Check that all type definitions in the catalogue are present in the raw model.
 * </table></pre>
 */
public class CatalogueTestBase extends ConfiguratorTestBase
{
    /** The catalogue model to test. */
    Catalogue catalogue;

    /** The raw catalogue model to test against, extracted from the XML. */
    CatalogueDefinition definition;

    /**
     * Creates a catalogue test for the named test method, over the specified catalogue and raw catalogue definition.
     *
     * @param name       The name of the test to run.
     * @param catalogue  The catalogue knowledge level implementation to test.
     * @param definition The raw catalogue definition.
     */
    public CatalogueTestBase(String name, Catalogue catalogue, CatalogueDefinition definition)
    {
        super(name);

        this.catalogue = catalogue;
        this.definition = definition;
    }

    /** Check that all component types in the raw model are present in the catalogue. */
    public void testAllComponentsInCatalogue()
    {
        String errorMessage = "";

        for (ComponentDefType dimensionDef : SetupModelHelper.getAllComponentDefs(definition))
        {
            String dimensionName = dimensionDef.getName();

            ComponentType dimension = catalogue.getComponentType(dimensionName);

            if (dimension == null)
            {
                errorMessage += "The component, " + dimensionName + ", was not found in the catalogue.\n";
            }
        }

        // Report any errors.
        assertTrue(errorMessage, errorMessage.equals(""));
    }

    /** Check that all component types listed by the catalogue are present in the raw model. */
    public void testAllComponentsInRawModel()
    {
        String errorMessage = "";

        for (ComponentType component : catalogue.getAllComponentTypes())
        {
            String componentName = component.getName();

            // Skip compound_name components, as these are implicit components promoted to the top-level, for example
            // where an anonymous collection of fields in a collection exists.
            if (!"compound_name".equals(componentName))
            {
                ComponentDefType dimensionDef = SetupModelHelper.getComponentDefByName(definition, componentName);

                if (dimensionDef == null)
                {
                    errorMessage += "The component, " + componentName + ", was not found in the raw catalogue model.\n";
                }
            }
        }

        // Report any errors.
        assertTrue(errorMessage, errorMessage.equals(""));
    }

    /** Check that all hierarchies definitions in the raw model are present in the catalogue. */
    public void testAllHierarchiesInRawModelAreInCatalogue() throws Exception
    {
        String errorMessage = "";

        for (HierarchyDefType hierarchyDef : SetupModelHelper.getAllHierarchyDefs(definition))
        {
            String hierarchyName = hierarchyDef.getName();

            // Check that the catalogue contains the hierarchy.
            com.thesett.aima.attribute.impl.HierarchyType hierarchyType = catalogue.getHierarchyType(hierarchyName);

            if (hierarchyType == null)
            {
                errorMessage +=
                    "The hierarchy definition, " + hierarchyName +
                    ", in the raw model was not found in the catalogue.\n";
            }
            else
            {
                // Check that their type names match up.
                String hierarchyTypeTypeName = hierarchyType.getName();

                if (!hierarchyTypeTypeName.equals(hierarchyName))
                {
                    errorMessage +=
                        "The hierarchy defintion, " + hierarchyName + ", has type, " + hierarchyName +
                        ", in the raw model but in the catalogue has type, " + hierarchyTypeTypeName + ".\n";
                }
            }
        }

        // Report any errors.
        assertTrue(errorMessage, errorMessage.equals(""));
    }

    /** Check that all hierarchies in all product types are present in the raw model. */
    public void testAllHierarchiesInCatalogueAreInRawModel() throws Exception
    {
        String errorMessage = "";

        for (com.thesett.aima.attribute.impl.HierarchyType hierarchyType : catalogue.getAllHierarchyTypes())
        {
            String hierarchyTypeName = hierarchyType.getName();

            // Get the hierarchy declaration from the raw model.
            HierarchyDefType hierarchyDef = SetupModelHelper.getHierarchyDefByName(definition, hierarchyTypeName);

            if (hierarchyDef == null)
            {
                errorMessage += "The hierarchy, " + hierarchyTypeName + ", was not found in the raw catalogue model.\n";
            }

            // Check that they match up.
            String hierarchyName = hierarchyDef.getName();

            if (!hierarchyName.equals(hierarchyTypeName))
            {
                errorMessage +=
                    "The hierarchy, " + hierarchyName + ", has type, " + hierarchyTypeName +
                    ", but in the raw model has type, " + hierarchyName + ".\n";
            }
        }

        // Report any errors.
        assertTrue(errorMessage, errorMessage.equals(""));
    }

    /** Check that all component fields in all product types in the raw model are present in the catalogue. */
    public void testAllComponentFieldsAllProductsInCatalogue() throws Exception
    {
        String errorMessage =
            checkAllProductFieldsOfTypeInCatalogue("component", ComponentTypeImpl.class,
                com.thesett.catalogue.setup.ComponentType.class);

        // Report any errors.
        assertTrue(errorMessage, errorMessage.equals(""));
    }

    /** Check that all date fields in all product types in the raw model are present in the catalogue. */
    public void testAllDateFieldsAllProductsInCatalogue() throws Exception
    {
        String errorMessage = checkAllProductFieldsOfTypeInCatalogue("date", DateOnly.class, DateType.class);

        // Report any errors.
        assertTrue(errorMessage, errorMessage.equals(""));
    }

    /** Check that all enumeration fields in all product types in the raw model are present in the catalogue. */
    public void testAllEnumerationFieldsAllProductsInCatalogue() throws Exception
    {
        String errorMessage =
            checkAllProductFieldsOfTypeInCatalogue("enumeration", EnumeratedStringAttribute.class,
                EnumerationType.class);

        // Report any errors.
        assertTrue(errorMessage, errorMessage.equals(""));
    }

    /** Check that all hierarchy fields in all product types in the raw model are present in the catalogue. */
    public void testAllHierarchyFieldsAllProductsInCatalogue() throws Exception
    {
        String errorMessage =
            checkAllProductFieldsOfTypeInCatalogue("hierarchy", HierarchyAttribute.class, HierarchyType.class);

        // Report any errors.
        assertTrue(errorMessage, errorMessage.equals(""));
    }

    /** Check that all boolean fields in all product types in the raw model are present in the catalogue. */
    public void testAllBooleanFieldsAllProductsInCatalogue() throws Exception
    {
        String errorMessage = checkAllProductFieldsOfTypeInCatalogue("boolean", Boolean.class, BooleanType.class);

        // Report any errors.
        assertTrue(errorMessage, errorMessage.equals(""));
    }

    /** Check that all integer fields in all product types in the raw model are present in the catalogue. */
    public void testAllIntegerFieldsAllProductsInCatalogue() throws Exception
    {
        String errorMessage = checkAllProductFieldsOfTypeInCatalogue("integer", Integer.class, IntegerType.class);

        // Report any errors.
        assertTrue(errorMessage, errorMessage.equals(""));
    }

    /** Check that all real fields in all product types in the raw model are present in the catalogue. */
    public void testAllRealFieldsAllProductsInCatalogue() throws Exception
    {
        Set<Class> classSet = new HashSet<Class>();
        classSet.add(Float.class);
        classSet.add(BigDecimal.class);

        String errorMessage = checkAllProductFieldsOfTypeInCatalogue("real", classSet, RealType.class);

        // Report any errors.
        assertTrue(errorMessage, errorMessage.equals(""));
    }

    /** Check that all string fields in all product types in the raw model are present in the catalogue. */
    public void testAllStringFieldsAllProductsInCatalogue() throws Exception
    {
        String errorMessage = checkAllProductFieldsOfTypeInCatalogue("string", String.class, StringType.class);

        // Report any errors.
        assertTrue(errorMessage, errorMessage.equals(""));
    }

    /** Check that all time fields in all product types in the raw model are present in the catalogue. */
    public void testAllTimeFieldsAllProductsInCatalogue() throws Exception
    {
        String errorMessage = checkAllProductFieldsOfTypeInCatalogue("time", TimeOnly.class, TimeType.class);

        // Report any errors.
        assertTrue(errorMessage, errorMessage.equals(""));
    }

    /** Check that all fields in all product types are present in the raw model. */
    public void testAllFieldsAllProductsInRawModel() throws Exception
    {
        String errorMessage = "";

        for (ComponentType dimension : catalogue.getAllComponentTypes())
        {
            String dimensionName = dimension.getName();

            DimensionDefType dimensionDef = SetupModelHelper.getDimensionDefByName(definition, dimensionName);

            for (String attributeName : dimension.getAllPropertyTypes().keySet())
            {
                // Get the attribute type from the dimension.
                Type type = dimension.getPropertyType(attributeName);

                // Check string fields.
                if (type.getBaseClass().equals(String.class))
                {
                    // Get the attribute declaration from the raw model.
                    StringType attributeDeclr = SetupModelHelper.getStringAttributeByName(dimensionDef, attributeName);

                    // Check that the raw model contains the attribute.
                    if (attributeDeclr == null)
                    {
                        errorMessage +=
                            "The attribute, " + attributeName + ", on dimension ," + dimensionName +
                            ", was not found in the raw catalogue model.\n";
                    }
                }

                // Check boolean fields.
                if (type.getBaseClass().equals(Boolean.class))
                {
                    // Get the attribute declaration from the raw model.
                    BooleanType attributeDeclr =
                        SetupModelHelper.getBooleanAttributeByName(dimensionDef, attributeName);

                    // Check that the raw model contains the attribute.
                    if (attributeDeclr == null)
                    {
                        errorMessage +=
                            "The attribute, " + attributeName + ", on dimension ," + dimensionName +
                            ", was not found in the raw catalogue model.\n";
                    }
                }

                // Check int fields.
                if (type.getBaseClass().equals(Integer.class))
                {
                    // Get the attribute declaration from the raw model.
                    IntegerType attributeDeclr =
                        SetupModelHelper.getIntegerAttributeByName(dimensionDef, attributeName);

                    // Check that the raw model contains the attribute.
                    if (attributeDeclr == null)
                    {
                        errorMessage +=
                            "The attribute, " + attributeName + ", on dimension ," + dimensionName +
                            ", was not found in the raw catalogue model.\n";
                    }
                }
            }
        }

        // Report any errors.
        assertTrue(errorMessage, errorMessage.equals(""));
    }

    /** Check that all top level type definitions in the raw model are present in the catalogue. */
    public void testAllTypeDefsInRawModel() throws Exception
    {
        String errorMessage = "";

        // Loop over all type defs in the catalogue, checking they are all in the raw model.
        // Only hierarchy type defs are possible at the moment.
        for (com.thesett.aima.attribute.impl.HierarchyType hierarchyType : catalogue.getAllHierarchyTypes())
        {
            // Extract the type name.
            String typeName = hierarchyType.getName();

            // Check it is in the raw model.
            TypeDefType typeDef = SetupModelHelper.getTypeDefByName(definition, typeName);

            if (typeDef == null)
            {
                errorMessage +=
                    "Hierarchy type, " + typeName + ", was found in the catalogue but not in the raw model.\n";
            }
            else if (!(typeDef instanceof HierarchyDefType))
            {
                errorMessage +=
                    "Hierarchy type, " + typeName +
                    ", was found in the catalogue but in the raw model is not a hierarchy definition.\n";
            }
        }

        // Report any errors.
        assertTrue(errorMessage, errorMessage.equals(""));
    }

    /** Check that all type definitions in the catalogue are present in the raw model. */
    public void testAllTypeDefsInCatalogue() throws Exception
    {
        String errorMessage = "";

        // Loop over all type defs in the raw model, checking they are all in the catalogue.
        for (TypeDefType typeDef : SetupModelHelper.getAllTypeDefs(definition))
        {
            // At the moment only hierarchy defs are possible.
            if (typeDef instanceof HierarchyDefType)
            {
                HierarchyDefType hierarchyDef = (HierarchyDefType) typeDef;

                // Extract the type definition name.
                String typeDefName = hierarchyDef.getName();

                // Check it is in the hiearchy.
                com.thesett.aima.attribute.impl.HierarchyType hierarchyType = catalogue.getHierarchyType(typeDefName);

                if (hierarchyType == null)
                {
                    errorMessage +=
                        "Hierarchy type def, " + typeDefName +
                        ", was found in the raw model but not in the catalogue.\n";
                }
            }
        }

        // Report any errors.
        assertTrue(errorMessage, errorMessage.equals(""));
    }

    protected void setUp() throws Exception
    {
        // Push a client identifier onto the Nested Diagnostic Context so that Log4J will be able to identify all
        // logging output for this tests.
        NDC.push(getName());
    }

    protected void tearDown() throws Exception
    {
        // Clear the nested diagnostic context for this test.
        NDC.pop();
    }

    /**
     * For all product types defined in the raw catalogue model, checks that all fields of the specified kind in the raw
     * catalogue model, exist in the catalogue, and have the expected type.
     *
     * @param  rawTypeName        The name of the type in the raw catalogue model.
     * @param  javaTypeClass      The class of the expected type in catalogue.
     * @param  catalogueSetupType The class of the field declaration in the setup catalogue model.
     *
     * @return A string containing any mimatches discovered, or the empty string if none.
     */
    private String checkAllProductFieldsOfTypeInCatalogue(String rawTypeName, Class javaTypeClass,
        Class<? extends FieldDeclrType> catalogueSetupType)
    {
        String errorMessage = "";

        for (DimensionDefType dimensionDef : SetupModelHelper.getAllDimensionDefs(definition))
        {
            String dimensionName = dimensionDef.getName();

            ComponentType component = catalogue.getComponentType(dimensionName);

            // Check all the fields defined in the raw model.
            for (FieldDeclrType attributeDeclr :
                SetupModelHelper.getAllAttributesOfType(catalogueSetupType, dimensionDef))
            {
                String attributeName = attributeDeclr.getName();

                // Check that the component in the catalogue contains the attribute.
                Type type = component.getPropertyType(attributeName);

                if (type == null)
                {
                    errorMessage +=
                        "The " + rawTypeName + " attribute '" + attributeName + "' on component '" + dimensionName +
                        "' in the raw model was not found in the catalogue.\n";
                }
                else // Check that the attribute has the correct type in the catalogue.
                {
                    if (!type.getBaseClass().equals(javaTypeClass))
                    {
                        errorMessage +=
                            "The " + rawTypeName + " attribute '" + attributeName + "' on component '" + dimensionName +
                            "' has type " + rawTypeName + " in the raw model, mapping onto " + javaTypeClass +
                            ", but in the catalogue has type " + type.getBaseClass() + ".\n";
                    }
                }
            }
        }

        return errorMessage;
    }

    private String checkAllProductFieldsOfTypeInCatalogue(String rawTypeName, Set<Class> classSet,
        Class<? extends FieldDeclrType> catalogueSetupType)
    {
        String errorMessage = "";

        for (DimensionDefType dimensionDef : SetupModelHelper.getAllDimensionDefs(definition))
        {
            String dimensionName = dimensionDef.getName();

            ComponentType component = catalogue.getComponentType(dimensionName);

            // Check all the fields defined in the raw model.
            for (FieldDeclrType attributeDeclr :
                SetupModelHelper.getAllAttributesOfType(catalogueSetupType, dimensionDef))
            {
                String attributeName = attributeDeclr.getName();

                // Check that the component in the catalogue contains the attribute.
                Type type = component.getPropertyType(attributeName);

                if (type == null)
                {
                    errorMessage +=
                        "The " + rawTypeName + " attribute '" + attributeName + "' on component '" + dimensionName +
                        "' in the raw model was not found in the catalogue.\n";
                }
                else // Check that the attribute has the correct type in the catalogue.
                {
                    if (!classSet.contains(type.getBaseClass()))
                    {
                        errorMessage +=
                            "The " + rawTypeName + " attribute '" + attributeName + "' on component '" + dimensionName +
                            "' has type " + rawTypeName + " in the raw model, mapping onto " + classSet +
                            ", but in the catalogue has type " + type.getBaseClass() + ".\n";
                    }
                }
            }
        }

        return errorMessage;
    }
}
