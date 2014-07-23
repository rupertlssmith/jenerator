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
 * <p/>Some entities may also provide an external id, which serves to uniquely identify the entity over a much larger
 * realm and for a much longer time than the entities storage key.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities
 * <tr><td> Provide a long lived external key to uniquely identify an entity.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public interface ExternallyIdentified
{
    /**
     * Gets the external id of the element.
     *
     * @return The external id of the element.
     */
    public ExternalId getExternalId();

    /**
     * Sets the external id of the element.
     *
     * @param id The external id of the element.
     */
    void setExternalId(ExternalId id);
}
