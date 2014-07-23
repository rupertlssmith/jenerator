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
package com.thesett.catalogue.core;

import java.util.Map;
import java.util.Set;

import com.thesett.aima.state.ComponentType;
import com.thesett.aima.state.Type;
import com.thesett.catalogue.interfaces.DimensionType;

/**
 * DimensionTypeImpl is the type of a dimension, which is an entity, that has a secondary storage representation in a
 * data warehouse.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> None yet.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class DimensionTypeImpl extends EntityTypeImpl implements DimensionType
{
    /**
     * Creates a dimension with the specified name, set of fields and implementing class.
     *
     * @param name                 The name of the entity.
     * @param attributes           The fields of the entity.
     * @param operationalClassName An implementing class.
     * @param immediateAncestors   The immediate ancestors of this type.
     */
    public DimensionTypeImpl(String name, Map<String, Type> attributes, String operationalClassName,
        Set<ComponentType> immediateAncestors)
    {
        super(name, attributes, operationalClassName, immediateAncestors);
    }
}
