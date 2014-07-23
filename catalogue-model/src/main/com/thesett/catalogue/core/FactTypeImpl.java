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
import com.thesett.catalogue.interfaces.FactType;

/**
 * FactTypeImpl is the type of a fact table in a data warehouse. A fact in an entity, as it is persisted to a database,
 * and generally speaking facts contain only numerical values or references to dimensions. In addition to numerical
 * values facts may sometimes contain discrete valued fields, for example enumerations. The more general rule is that
 * facts contain data elements that are amenable to aggregation and statistical analysis, at the intersection of
 * instances of one or more dimensions.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> None yet.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class FactTypeImpl extends EntityTypeImpl implements FactType
{
    /**
     * Creates a fact type with the specified name, set of fields and implementing class.
     *
     * @param name                 The name of the fact.
     * @param attributes           The fields of the fact.
     * @param operationalClassName An implementing class.
     * @param immediateAncestors   The immediate ancestors of this type.
     */
    public FactTypeImpl(String name, Map<String, Type> attributes, String operationalClassName,
        Set<ComponentType> immediateAncestors)
    {
        super(name, attributes, operationalClassName, immediateAncestors);
    }
}
