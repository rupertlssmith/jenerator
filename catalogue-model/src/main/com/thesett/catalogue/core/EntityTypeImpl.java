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
import com.thesett.aima.state.TypeVisitor;
import com.thesett.catalogue.interfaces.EntityType;
import com.thesett.catalogue.interfaces.EntityTypeVisitor;

/**
 * EntityType is the type of an entity, which is a component that can be persisted to a database.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Accept a type visitor, applying it to this if it is an entity visitor.
 *     <td> {@link com.thesett.aima.state.TypeVisitor}, {@link EntityTypeVisitor}
 * <tr><td> Indicate whether or not an entity has a long lived external identifier.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class EntityTypeImpl extends ComponentTypeImpl implements EntityType
{
    /** Flag to indicate when true, that this entity type supports external ids. */
    protected boolean externalIdFlag = false;

    /**
     * Creates an entity type with the specified name, fields and implementing class.
     *
     * @param name                 The name of the entity.
     * @param attributes           The fields of the entity.
     * @param operationalClassName An implementing class.
     * @param immediateAncestors   The immediate ancestors of this type.
     */
    public EntityTypeImpl(String name, Map<String, Type> attributes, String operationalClassName,
        Set<ComponentType> immediateAncestors)
    {
        super(name, attributes, operationalClassName, immediateAncestors);
    }

    /**
     * {@inheritDoc}
     *
     * <p/>If the visitiro is an {@link EntityTypeVisitor}, it is applied to this, otherwise it is chained up to the
     * super visitor method.
     */
    public void acceptVisitor(TypeVisitor visitor)
    {
        if (visitor instanceof EntityTypeVisitor)
        {
            ((EntityTypeVisitor) visitor).visit(this);
        }
        else
        {
            super.acceptVisitor(visitor);
        }
    }

    /** {@inheritDoc} */
    public boolean isExternalId()
    {
        return externalIdFlag;
    }

    /**
     * Sets the external id flag to indicate whether or not entities of this type support external ids.
     *
     * @param externalIdFlag <tt>true</tt> if this entity type supports external ids.
     */
    public void setExternalIdFlag(boolean externalIdFlag)
    {
        this.externalIdFlag = externalIdFlag;
    }
}
