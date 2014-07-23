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

import java.util.Iterator;
import java.util.List;

import com.thesett.catalogue.setup.LabelType;

/**
 * EnumLabelFieldHandler transforms 'label' fields into a labels/1 functor, that holds a list of enumeration
 * labels taken from the fields value, as its argument.
 *
 * <p/><pre><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Transform an enumeration type declarations labels into a list. <td> {@link LabelType}.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class EnumLabelFieldHandler implements FieldHandler
{
    /**
     * {@inheritDoc}
     *
     * <p/>This transformation expects a list of {@link LabelType}s as the fields value, and transforms these into
     * a recursive list. This transformation is only applied to 'label' fields.
     */
    public String handleField(String property, Object value, boolean more)
    {
        if ("label".equals(property))
        {
            // Convert the property value to a list of labels.
            List<LabelType> labels = (List<LabelType>) value;

            String result = "labels([";

            for (Iterator<LabelType> i = labels.iterator(); i.hasNext();)
            {
                LabelType label = i.next();
                result += label.getName() + (i.hasNext() ? ", " : "");
            }

            result += "])" + (more ? ", " : "");

            return result;
        }

        return null;
    }
}
