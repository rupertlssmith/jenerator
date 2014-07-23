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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * InQuotesFieldHandler transforms a matching subset of its input fields into a functors of arity 1. The name of
 * the functor is the name of the field, and the argument is its value as a string literal in quotes.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Transform some type declaration fields into a functor of arity one with string argument.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class InQuotesFieldHandler implements FieldHandler
{
    /** Holds the set of field name to match for transformation. */
    Set<String> propertiesInQuotes = new HashSet<String>();

    /**
     * Creats a new in-quotes handler on the specified set of named fields.
     *
     * @param properties The set of fields to transform.
     */
    public InQuotesFieldHandler(String[] properties)
    {
        Collections.addAll(propertiesInQuotes, properties);
    }

    /**
     * {@inheritDoc}
     *
     * <p/>This mapping transform the property/value pair into a functor, property("value"), optionally adding a
     * continuation ',' if there are more fields in the sequence, so that the sequence may be parsed as a list
     * body.
     */
    public String handleField(String property, Object value, boolean more)
    {
        if (propertiesInQuotes.contains(property))
        {
            return property + "(\"" + value.toString() + "\")" + (more ? ", " : "");
        }

        return null;
    }
}
