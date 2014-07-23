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
package com.thesett.catalogue.interfaces;

/**
 * The raw data service provides CRUD and replace operation on operational level catalogue data, for raw data in XML
 * format. It also provides validation of the raw data against the catalogue knowledge level and detailed error reporting
 * of any validation failures.
 *
 * <p/>This service is intended to be used by the data upload and extract tools.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Perform CRUD operations on dimension elements. <td> {@link CatalogueManagerService}
 * <tr><td> Validate raw operational data against the catalogue knowledge level.
 * <tr><td> Provide error reporting on invalid data.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public interface RawDataService
{
    /**
     * Validates and uploads the data set from raw XML.
     *
     * @param dataSet The data set to upload in XML.
     */
    public void uploadDataSet(String dataSet); //throws MultipleUserErrorException;

    /**
     * Used to ping the service to check it is reachable.
     *
     * @return <tt>true</tt> always.
     */
    public boolean ping();
}
