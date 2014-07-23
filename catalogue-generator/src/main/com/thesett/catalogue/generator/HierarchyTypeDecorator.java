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
import java.util.Set;

import com.thesett.aima.attribute.impl.HierarchyAttribute;
import com.thesett.aima.attribute.impl.HierarchyType;
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
public class HierarchyTypeDecorator extends TypeDecorator<HierarchyAttribute> implements HierarchyType
{
    /**
     * Creates a decorator on a hierarchy type, that exposes the methods of hierarchy types as a decorated type.
     *
     * @param hierarchyAttributeType The hierarchy type to decorate.
     */
    public HierarchyTypeDecorator(Type<HierarchyAttribute> hierarchyAttributeType)
    {
        super(hierarchyAttributeType);
    }

    /** {@inheritDoc} */
    public String[] getLevelNames()
    {
        return ((HierarchyType) type).getLevelNames();
    }

    /** {@inheritDoc} */
    public Iterator<HierarchyAttribute> getValuesAtLevelIterator(String level)
    {
        return ((HierarchyType) type).getValuesAtLevelIterator(level);
    }

    /** {@inheritDoc} */
    public Iterator<HierarchyAttribute> getSubHierarchyValuesIterator(HierarchyAttribute parent, String level)
    {
        return ((HierarchyType) type).getSubHierarchyValuesIterator(parent, level);
    }

    /** {@inheritDoc} */
    public Set<HierarchyAttribute> getValuesAtLevelSet(String level)
    {
        return ((HierarchyType) type).getValuesAtLevelSet(level);
    }

    /** {@inheritDoc} */
    public Set<HierarchyAttribute> getSubHierarchyValuesSet(HierarchyAttribute parent, String level)
    {
        return ((HierarchyType) type).getSubHierarchyValuesSet(parent, level);
    }

    /** {@inheritDoc} */
    public Iterator<HierarchyAttribute> getAllPossibleValuesIterator(boolean failOnNonFinalized)
    {
        return ((HierarchyType) type).getAllPossibleValuesIterator(failOnNonFinalized);
    }

    /** {@inheritDoc} */
    public Set<HierarchyAttribute> getAllPossibleValuesSet(boolean failOnNonFinalized)
    {
        return ((HierarchyType) type).getAllPossibleValuesSet(failOnNonFinalized);
    }
}
