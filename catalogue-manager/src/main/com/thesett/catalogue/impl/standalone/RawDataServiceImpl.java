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
package com.thesett.catalogue.impl.standalone;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import com.thesett.catalogue.data.FieldAssignmentType;
import com.thesett.catalogue.data.Identifier;
import com.thesett.catalogue.data.IntAttributeType;
import com.thesett.catalogue.data.StringAttributeType;
import com.thesett.catalogue.model.Catalogue;
import com.thesett.catalogue.model.CatalogueManagerService;
import com.thesett.catalogue.model.EntityInstance;
import com.thesett.catalogue.model.EntityType;
import com.thesett.catalogue.model.ExternallyIdentified;
import com.thesett.catalogue.model.InternalId;
import com.thesett.catalogue.model.RawDataService;
import com.thesett.catalogue.model.base.EntityInstanceBase;
import com.thesett.catalogue.model.impl.InternalIdImpl;
import com.thesett.catalogue.setup.HierarchyType;
import org.apache.log4j.Logger;

import com.thesett.aima.attribute.impl.HierarchyAttribute;
import com.thesett.aima.attribute.impl.HierarchyAttributeFactory;
import com.thesett.aima.state.ComponentType;
import com.thesett.aima.state.Type;
import com.thesett.common.error.MultipleUserErrorException;
import com.thesett.common.error.UserReadableError;
import com.thesett.common.error.UserReadableErrorImpl;
import com.thesett.common.util.ReflectionUtils;
import com.thesett.common.util.StringUtils;

/**
 * RawDataServiceImpl is a standalone implementation of the {@link RawDataService}.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class RawDataServiceImpl implements RawDataService
{
    /** Used for logging. */
    private static final Logger log = Logger.getLogger(RawDataServiceImpl.class);

    /** Holds the accumulated error messages from the uploading process. */
    List<UserReadableError> errorMessages = new ArrayList<UserReadableError>();

    /**
     * Validates and uploads the data set from raw XML.
     *
     * @param rawDataSet The data set to upload in raw XML.
     */
    public void uploadDataSet(String rawDataSet)
    {
        log.debug("public void uploadDataSet(String dataSet): called");

        try
        {
            // Retrieve the catalogue knowledge level.
            Catalogue catalogue;

            try
            {
                CatalogueManagerService catalogueManager = CatalogueManagerRemote.getInstance();
                catalogue = catalogueManager.getCatalogue();
            }
            catch (ServiceFailureException e)
            {
                throw new MultipleUserErrorException(
                    "Got service failure exception whilst trying to communicate with the catalogue server: ", e, null,
                    "Unable to connect to the catalogue server.");
            }

            // Parse the raw xml into a dataset.
            com.thesett.catalogue.data.DimensionalData dataSet = null;

            try
            {
                dataSet = readDataSet(rawDataSet);
            }
            catch (MultipleUserErrorException e)
            {
                // If the file could not be parsed as a data set then error messages will be genereted.
                // Add the error message to the accumulated messages.
                errorMessages.addAll(e.getErrors());
            }

            // Sort and group the data sets into the correct sequence for uploading, depending on relationships between
            // the data items.
            // Not needed yet, as relationships not supported yet.

            try
            {
                // Verify the dataset against the catalogue.
                validateDataSet(dataSet, catalogue);

                // Upload the dataset.
                uploadDataSet(dataSet, catalogue);
            }
            catch (MultipleUserErrorException e)
            {
                // If verification or uploading fails, there will be error messages.
                // Add the error message to the accumulated messages.
                errorMessages.addAll(e.getErrors());
            }
        }
        catch (MultipleUserErrorException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Parses raw xml data as a data set. If the XML is not well-formed then exceptions will be raised.
     *
     * @param  rawData The raw XML data to be used.
     *
     * @return The data parsed as a data set.
     *
     * @throws MultipleUserErrorException If the raw data cannot be parsed as a data set.
     */
    public com.thesett.catalogue.data.DimensionalData readDataSet(String rawData) throws MultipleUserErrorException
    {
        log.debug("public com.thesett.catalogue.data.DimensionalData readDataSet(String rawData): called");

        // Open the specified file and unmarshal the catalogue model from it.
        com.thesett.catalogue.data.DimensionalData dataSet = null;

        try
        {
            JAXBContext jc = JAXBContext.newInstance("com.thesett.catalogue.data");

            Unmarshaller u = jc.createUnmarshaller();
            dataSet = (com.thesett.catalogue.data.DimensionalData) u.unmarshal(new StringReader(rawData));
        }

        // There were errors parsing the file.
        catch (JAXBException e)
        {
            log.warn(e);
            throw new MultipleUserErrorException("Raw data could not be unmarshalled: ", e, null,
                "The data set does not contain a valid data set. Ignoring it.");
        }

        return dataSet;
    }

    /**
     * Validates the structure of the data set to be uploaded against the catalogue knowledge level of the catalouge
     * that is being uploaded to.
     *
     * @param  dataSet   The data set to verify.
     * @param  catalogue The catalogue knowledge level to verify against.
     *
     * @throws MultipleUserErrorException If the validation fails. Error messages describing the reason for the failure
     *                                    are supplied.
     *
     * @todo   Refactor the cut and paste coding used here.
     */
    public void validateDataSet(com.thesett.catalogue.data.DimensionalData dataSet, Catalogue catalogue)
        throws MultipleUserErrorException
    {
        log.debug(
            "public void validateDataSet(com.thesett.catalogue.data.DimensionalData dataSet, Catalogue catalogue, String fileName): called");

        // Used to build up error messages in.
        List<UserReadableError> errors = new ArrayList<UserReadableError>();

        // Get the data item from the data set.
        com.thesett.catalogue.data.DimensionElement item = dataSet.getDimensionElement();

        log.debug("item has " + item.getFieldAssignment().size() + " fields to validate.");

        // Look up the items dimension in the catalogue.
        String dimensionName = item.getDimension();
        ComponentType dimension = catalogue.getComponentType(dimensionName);

        // Check that the dimension exists.
        if (dimension == null)
        {
            throw new MultipleUserErrorException("Dimension, " + dimensionName + ", not found in the catalogue.", null,
                null,
                "The data item has dimension, " + dimensionName +
                ", but this dimension does not exist in the catalogue.");
        }

        // Extract each of the items fields and check that they match the types in the catalogue.
        for (JAXBElement<? extends FieldAssignmentType> fieldElement : item.getFieldAssignment())
        {
            FieldAssignmentType field = fieldElement.getValue();
            String fieldName = field.getName();

            log.debug("Validating property, " + fieldName + ".");

            if (field instanceof IntAttributeType)
            {
                // Look up the field in the catalogue.
                Type type = dimension.getPropertyType(fieldName);

                // Check that the property exists in the catalogue.
                if (type == null)
                {
                    errors.add(new UserReadableErrorImpl(null,
                            "The data item has property, " + fieldName + ", of type, " + Integer.class +
                            ", but this property does not exist in the catalogue."));

                    // Continue checking next field.
                    continue;
                }

                // Check that the types match.
                else if (!type.getBaseClass().equals(Integer.class))
                {
                    errors.add(new UserReadableErrorImpl(null,
                            "The data item has property, " + fieldName + ", of type, " + Integer.class +
                            ", but in the catalogue this property has type, " + type.getName() + "."));

                    // Continue checking next field.
                    continue;
                }

                log.debug("Passed as int.");

            }
            else if (field instanceof DateAttributeType)
            {
                throw new MultipleUserErrorException("Dates not supported yet.", null, null,
                    "Dates not supported yet.");
            }
            else if (field instanceof StringAttributeType)
            {
                // Look up the field in the catalogue.
                Type type = dimension.getPropertyType(fieldName);

                // Check that the property exists in the catalogue.
                if (type == null)
                {
                    errors.add(new UserReadableErrorImpl(null,
                            "The data item has property, " + fieldName + ", of type, " + String.class +
                            ", but this property does not exist in the catalogue."));

                    // Continue checking next field.
                    continue;
                }

                // Check that the types match.
                else if (!type.getBaseClass().equals(String.class))
                {
                    errors.add(new UserReadableErrorImpl(null,
                            "The data item has property, " + fieldName + ", of type, " + String.class +
                            ", but in the catalogue this property has type, " + type.getName() + "."));

                    // Continue checking next field.
                    continue;
                }

                log.debug("Passed as string.");
            }
            else if (field instanceof HierarchyType)
            {
                HierarchyType hierarchyAttribute = (HierarchyType) field;

                // Look up the field in the catalogue.
                com.thesett.aima.attribute.impl.HierarchyType type =
                    (com.thesett.aima.attribute.impl.HierarchyType) dimension.getPropertyType(fieldName);

                // Check that the property exists in the catalogue.
                if (type == null)
                {
                    errors.add(new UserReadableErrorImpl(null,
                            "The data item has hierarchy property, " + fieldName +
                            ", but this hierarchy property does not exist in the catalogue."));

                    // Continue checking next field.
                    continue;
                }

                // Check that the value is a legal and allowable member of the hierarchy.
                HierarchyAttributeFactory factory = HierarchyAttribute.getFactoryForClass(type.getName());

                String valueAsDelimitedString = hierarchyAttribute.getValueOf();
                String[] value = StringUtils.listToArray(valueAsDelimitedString, "/");

                try
                {
                    factory.createHierarchyAttribute(value);
                }
                catch (IllegalArgumentException e)
                {
                    errors.add(new UserReadableErrorImpl(null,
                            "The data item has property, " + fieldName + ", of value, " + valueAsDelimitedString +
                            ", but in the catalogue this property has type, " + type.getName() +
                            ", of which this is not an allowable value."));

                    // Continue checking next field.
                    continue;
                }

                log.debug("Passed as hierarchy.");
            }
        }

        // Check if there were any errors and raise an exception if there were.
        if (!errors.isEmpty())
        {
            log.warn("There were validation errors.");

            for (UserReadableError error : errors)
            {
                log.debug("next error = " + error);
            }

            throw new MultipleUserErrorException("There were validation errors.", null, errors);
        }
    }

    /**
     * Uploads a validated data set to the catalogue.
     *
     * @param  dataSet   The data set to upload.
     * @param  catalogue The catalogue knowledge level.
     *
     * @throws MultipleUserErrorException This will not normally be thrown because the data set to upload should already
     *                                    have been validated. It may happen due to some runtime condition such as the
     *                                    catalogue server going down, or if this method is not called with incorrectly
     *                                    validated data sets.
     *
     * @todo   Perform a look up by natural key.
     */
    public void uploadDataSet(com.thesett.catalogue.data.DimensionalData dataSet, Catalogue catalogue)
        throws MultipleUserErrorException
    {
        log.debug(
            "public void uploadDataSet(com.thesett.catalogue.data.DimensionalData dataSet, Catalogue catalogue, String fileName): called");

        // Get the data item from the data set.
        com.thesett.catalogue.data.DimensionElement item = dataSet.getDimensionElement();

        // Look up the items dimension in the catalogue.
        String dimensionName = item.getDimension();
        EntityType entityType = (EntityType) catalogue.getComponentType(dimensionName);

        // Instantiate a new transient element from the dimension knowledge level. This element does not get created
        // in the database which is why it is called transient.
        EntityInstanceBase elementToUpload = (EntityInstanceBase) entityType.getInstance();

        // Check if the item already has a surrogate id and set this is up if it does.
        Identifier id = item.getIdentifier();
        InternalId opaqueId = null;

        if (id != null)
        {
            ReflectionUtils.callMethodOverridingIllegalAccess(elementToUpload, "setId", new Object[] { id.getValue() },
                new Class[] { Long.class });
            opaqueId = elementToUpload.getOpaqueId();

            log.debug("Got already instantiated surrogate id, " + id.getValue());
        }

        // Extract the field values for the element and set them on the item bean.
        for (JAXBElement<? extends FieldAssignmentType> fieldElement : item.getFieldAssignment())
        {
            FieldAssignmentType field = fieldElement.getValue();
            String fieldName = field.getName();

            if (field instanceof IntAttributeType)
            {
                IntAttributeType intAttribute = (IntAttributeType) field;
                int value = intAttribute.getValueOf();

                elementToUpload.setProperty(fieldName, value);

                log.debug("Set property, " + fieldName + ", to value: " + value);
            }
            else if (field instanceof StringAttributeType)
            {
                StringAttributeType stringAttribute = (StringAttributeType) field;
                String value = stringAttribute.getValueOf();

                // Check if value parameter is not set, in which case use the body of the tag.
                if (value == null)
                {
                    value = stringAttribute.getValue();
                }

                elementToUpload.setProperty(fieldName, value);

                log.debug("Set property, " + fieldName + ", to value: " + value);
            }
            else if (field instanceof HierarchyType)
            {
                HierarchyType hierarchyAttribute = (HierarchyType) field;

                // Extract the delimited string value of the hierarchy.
                String valueAsDelimitedString = hierarchyAttribute.getValueOf();
                String[] valueAsArray = StringUtils.listToArray(valueAsDelimitedString, "/");

                // Find the hierarchies type and get its factory.
                com.thesett.aima.attribute.impl.HierarchyType type =
                    (com.thesett.aima.attribute.impl.HierarchyType) entityType.getPropertyType(fieldName);
                HierarchyAttributeFactory factory = HierarchyAttribute.getFactoryForClass(type.getName());

                // Use the factory to instantiate it.
                HierarchyAttribute value = factory.createHierarchyAttribute(valueAsArray);

                elementToUpload.setProperty(fieldName, value);

                log.debug("Set property, " + fieldName + ", to value: " + value);
            }
        }

        CatalogueManagerService catalogueManager = CatalogueManagerRemote.getInstance();

        // Check if the element to modify already exists in the catalogue, looked up by id.
        EntityInstance elementById = null;
        EntityInstance elementByNaturalKey = null;

        if (id != null)
        {
            elementById = catalogueManager.retrieveEntityInstance(entityType, opaqueId);
        }

        // Look up by natural key.

        log.debug("elementById = " + elementById);
        log.debug("elementByNaturalKey = " + elementByNaturalKey);

        // Perform the create, update, delete or replacement action on the catalogue.
        // If the delete flag is set then perform a delete.
        if (dataSet.getDelete() != null)
        {
            log.debug("Deleting");

            // Check that a surrogate id exists on the data item, it is needed to identify which element to delete.
            if (id == null)
            {
                throw new MultipleUserErrorException("No surrogate id on item, " + elementToUpload + ", to delete.",
                    null, null,
                    "The data item cannot be deleted because it does not have a surrogate id. " +
                    "Items to delete must have been extracted from the catalogue and have surrogate id's set up.");
            }

            // Check that the item to delete actually exists.
            if (elementById == null)
            {
                throw new MultipleUserErrorException("Element to delete, " + elementToUpload +
                    ", cannot be found in catalogue.", null, null,
                    "The element to delete in the catalogue has a surrogate id but cannot be found in the catalogue. " +
                    "It may already have been deleted since it was extracted from the catalogue.");
            }

            catalogueManager.deleteEntityInstance(entityType, opaqueId);
        }

        // If the replace flag is set then perform a replace.
        else if (dataSet.getReplace() != null)
        {
            log.debug("Replacing.");

            throw new UnsupportedOperationException("Replacement is not supported yet.");
        }

        // If there is a surrogate id already set up then update then existing element in the catalogue.
        else if (id != null)
        {
            log.debug("Updating as surrogate id already set.");

            // Check that the item to update still exists.
            if (elementById == null)
            {
                throw new MultipleUserErrorException("Element to update, " + elementToUpload +
                    ", cannot be found in catalogue.", null, null,
                    "The element to update in the catalogue has a surrogate id but cannot be found in the catalogue. " +
                    "It may have been deleted since it was extracted from the catalogue. " +
                    "To re-insert the item, remove its surrogate key and try again. " +
                    "A new surrogate key will be generated for it.");
            }

            // Make sure its external id is set the same as the element found by natural key. This will change
            // when external id only logic is introduced to the uploader.
            if (elementToUpload.getComponentType().isExternalId())
            {
                ((ExternallyIdentified) elementToUpload).setExternalId(((ExternallyIdentified) elementById)
                    .getExternalId());
            }

            catalogueManager.updateEntityInstance(elementToUpload);
        }

        // If there is no surrogate id but there is a matching element by natural key then perform an update.
        else if (elementByNaturalKey != null)
        {
            log.debug("Updating as natural key already exists.");

            long realId = ((InternalIdImpl) elementByNaturalKey.getOpaqueId()).getValue();
            ReflectionUtils.callMethodOverridingIllegalAccess(elementToUpload, "setId", new Object[] { realId },
                new Class[] { Long.class });

            log.debug("Set real id, " + realId + ", on elementToUpload");

            // Make sure its external id is set the same as the element found by natural key. This will change
            // when external id only logic is introduced to the uploader.
            if (elementToUpload.getComponentType().isExternalId())
            {
                ((ExternallyIdentified) elementToUpload).setExternalId(((ExternallyIdentified) elementByNaturalKey)
                    .getExternalId());
            }

            catalogueManager.updateEntityInstance(elementToUpload);
        }

        // If there is no surrogate id or matching element by natural key then create a new item.
        else
        {
            log.debug("Creating as no key exists yet.");

            catalogueManager.createEntityInstance(elementToUpload);
        }
    }

    /**
     * Used to ping the service to check it is reachable.
     *
     * @return <tt>true</tt> always.
     */
    public boolean ping()
    {
        return true;
    }
}
