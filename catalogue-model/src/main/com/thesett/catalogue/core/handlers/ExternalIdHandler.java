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
package com.thesett.catalogue.core.handlers;

import com.thesett.catalogue.setup.ExternalId;

/**
 * ExternalIdHandler transforms the optional 'externalId' field into an externalid/0 constant, indicating that and
 * entity should support long lived external ids.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Transform an externalId field into an externalid/0 constant.
 *     <td> {@link com.thesett.catalogue.setup.ExternalId}
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class ExternalIdHandler implements FieldHandler
{
    /**
     * {@inheritDoc}
     *
     * <p/>This transformation expects a list of {@link com.thesett.catalogue.setup.View}s as the fields argument
     * and transforms these into a recursive list. This transformation only applies to 'view' fields.
     */
    public String handleField(String property, Object value, boolean more)
    {
        if ("externalId".equals(property))
        {
            // Cast the field value to a list of views.
            ExternalId views = (ExternalId) value;

            if (views != null)
            {
                return "externalid";
            }
        }

        return null;
    }
}
