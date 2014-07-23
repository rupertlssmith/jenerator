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
package com.thesett.catalogue.generator;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.thesett.aima.attribute.impl.EnumeratedStringAttribute;
import com.thesett.aima.state.Type;

/**
 * HierarchyTypeDecorator decorates a hierarchy type, exposing all of the available methods on hierarchy types in the
 * decorated type.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Decorate a hierarchy type.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class EnumeratedStringTypeDecorator extends TypeDecorator<EnumeratedStringAttribute>
    implements EnumeratedStringAttribute.EnumeratedStringType
{
    /**
     * Creates a decorator on a hierarchy type, that exposes the methods of hierarchy types as a decorated type.
     *
     * @param enumeratedStringAttributeType The hierarchy type to decorate.
     */
    public EnumeratedStringTypeDecorator(Type<EnumeratedStringAttribute> enumeratedStringAttributeType)
    {
        super(enumeratedStringAttributeType);
    }

    /** {@inheritDoc} */
    public Iterator<EnumeratedStringAttribute> getAllPossibleValuesIterator(boolean failOnNonFinalized)
    {
        return ((EnumeratedStringAttribute.EnumeratedStringType) type).getAllPossibleValuesIterator(failOnNonFinalized);
    }

    /** {@inheritDoc} */
    public Set<EnumeratedStringAttribute> getAllPossibleValuesSet(boolean failOnNonFinalized)
    {
        return ((EnumeratedStringAttribute.EnumeratedStringType) type).getAllPossibleValuesSet(failOnNonFinalized);
    }

    /**
     * Provides all of the pre-defined labels in the this enumeration type, whether it is finalized or not.
     *
     * @return A map of name/value pairs.
     */
    public Map<String, String> getLabels()
    {
        Map<String, String> result = new LinkedHashMap<String, String>();

        for (EnumeratedStringAttribute attribute : getAllPossibleValuesSet(false))
        {
            result.put(attribute.getStringValue(), attribute.getStringValue());
        }

        return result;
    }
}
