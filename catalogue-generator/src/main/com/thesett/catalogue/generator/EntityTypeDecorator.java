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

import com.thesett.catalogue.interfaces.EntityType;

/**
 * EntityTypeDecorator is a {@link TypeDecorator} for {@link com.thesett.catalogue.interfaces.EntityType}s. It
 * automatically decorates the types of any fields of the entity that are accessed through it, and provides all of
 * the underlying properties of the decorated entity.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Decorate a component type, and the types of all of its fields.
 *     <td> {@link TypeDecoratorFactory}, {@link com.thesett.aima.state.ComponentType}.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class EntityTypeDecorator extends ComponentTypeDecorator implements EntityType
{
    /**
     * Creates a type decorator for an entity type, that returns decorated types for all reachable child types on
     * all of the fields of the entity.
     *
     * @param type The entity type to decorate.
     */
    public EntityTypeDecorator(EntityType type)
    {
        super(type);
    }

    /** {@inheritDoc} */
    public boolean isExternalId()
    {
        return ((EntityType) type).isExternalId();
    }
}
