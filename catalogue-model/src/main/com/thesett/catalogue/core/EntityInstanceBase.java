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

import java.io.Serializable;

import com.thesett.aima.state.ComponentType;
import com.thesett.catalogue.interfaces.EntityInstance;
import com.thesett.catalogue.interfaces.EntityType;
import com.thesett.catalogue.interfaces.InternalId;

/**
 * EntityInstanceBase provides a base class for implementing entity instances.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Provide an opaque internal storage key to uniquely identify an entity. <td> {@link InternalIdImpl}
 * </table></pre>
 *
 * @author Rupert Smith
 */
public abstract class EntityInstanceBase extends ComponentInstanceBase implements EntityInstance, Serializable
{
    /** {@inheritDoc} */
    public abstract EntityType getComponentType();

    /** {@inheritDoc} */
    public InternalId getOpaqueId()
    {
        Long id = getId();

        if (null == id)
        {
            return null;
        }
        else
        {
            return new InternalIdImpl(id);
        }
    }

    /** {@inheritDoc} */
    public String getComponentTypeName()
    {
        ComponentType type = getComponentType();

        if (type == null)
        {
            return null;
        }
        else
        {
            return type.getName();
        }
    }
}
