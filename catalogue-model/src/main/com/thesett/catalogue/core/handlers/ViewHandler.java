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

import com.thesett.catalogue.setup.View;

/**
 * ViewHandler transforms 'view' fields into a views/1 functor with a list of view references as its argument.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Transform a view field into a list of view references. <td> {@link View}
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class ViewHandler implements FieldHandler
{
    /**
     * {@inheritDoc}
     *
     * <p/>This transformation expects a list of {@link View}s as the fields argument and transforms these into
     * a recursive list. This transformation only applies to 'view' fields.
     */
    public String handleField(String property, Object value, boolean more)
    {
        if ("view".equals(property))
        {
            // Cast the field value to a list of views.
            List<View> views = (List<View>) value;

            String result = "views([";

            for (Iterator<View> i = views.iterator(); i.hasNext();)
            {
                View view = i.next();
                result += view.getType() + (i.hasNext() ? ", " : "");
            }

            result += "])" + (more ? ", " : "");

            return result;
        }

        return null;
    }
}
