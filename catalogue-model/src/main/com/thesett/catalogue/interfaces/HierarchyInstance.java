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
package com.thesett.catalogue.interfaces;

import com.thesett.aima.attribute.impl.HierarchyAttribute;
import com.thesett.aima.attribute.impl.HierarchyType;
import com.thesett.aima.state.State;

/**
 * An HierarchyInstance is a {@link com.thesett.aima.state.State} that is an instance of a
 * {@link com.thesett.aima.attribute.impl.HierarchyType}. In addition to the set of named and typed fields that a state has, all
 * hierarchy instances have a unique id that identifies them within their realm of persistent storage.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Provide an opaque internal storage key to uniquely identify a hierarchy instance.
 * <tr><td> Provide the hierarchy type of an instance.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public interface HierarchyInstance extends State
{
    /**
     * Gets the id of the element.
     *
     * @return The id of the element.
     */
    public InternalId getOpaqueId();

    /**
     * Provides the hierarchy type of this instance.
     *
     * @return The hierarchy type of this instance.
     */
    HierarchyType getHierarchyType();

    /**
     * Gets the underlying hierarchy attribute that this hierarchy bean wraps.
     *
     * @return The underlying hierarchy attribute that this hierarchy bean wraps.
     */
    HierarchyAttribute getHierarchy();
}
