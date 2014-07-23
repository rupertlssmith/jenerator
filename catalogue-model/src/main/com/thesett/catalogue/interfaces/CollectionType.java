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

import java.io.Serializable;

import com.thesett.aima.state.Type;

/**
 * A CollectionType is a {@link com.thesett.aima.state.Type} that acts as a container for many instances of another
 * type.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th>Responsibilities <th>Collaborations
 * <tr><td>Encapsulate an element type.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public interface CollectionType<E> extends Type, Serializable
{
    /** Enumerates the different kinds of collections that the model supports. */
    public enum CollectionKind
    {
        /** The collection is an unordered set, each element may only appear once. */
        Set,

        /** The collection is an ordered list. */
        List,

        /** The collection is an unordered bag, each element may appear multiple times. */
        Bag,

        /** The collection is a key/value mapping. */
        Map
    }

    /**
     * Provides the type of the elements that this collection contains.
     *
     * @return The type of the elements that this collection contains.
     */
    public Type<E> getElementType();

    /**
     * Establishes the type of the elements that this collection contains.
     *
     * @param type The type of the elements that this collection contains.
     */
    public void setElementType(Type<E> type);

    /**
     * Provides the kind of collection that this is.
     *
     * @return The kind of collection that this is.
     */
    public CollectionKind getCollectionKind();
}
