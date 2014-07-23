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

import com.thesett.aima.state.Type;
import com.thesett.catalogue.interfaces.CollectionType;

/**
 * CollectionTypeDecorator is a {@link TypeDecorator} for {@link CollectionType}s. It automatically decorates the type
 * of the collections elements when accessed through it.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Decorate a collection type and the type of its elements too.
 *     <td> {@link TypeDecoratorFactory}, {@link CollectionType}.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class CollectionTypeDecorator<T> extends TypeDecorator implements CollectionType<T>
{
    /**
     * Creates a type decorator for a collection type, that decorates the type of the collections elements too.
     *
     * @param type The collection type to decorate.
     */
    public CollectionTypeDecorator(CollectionType type)
    {
        super(type);
    }

    /** {@inheritDoc} */
    public Type<T> getElementType()
    {
        return TypeDecoratorFactory.decorateType(((CollectionType<T>) type).getElementType());
    }

    /** {@inheritDoc} */
    public void setElementType(Type<T> type)
    {
        ((CollectionType<T>) type).setElementType(type);
    }

    /** {@inheritDoc} */
    public CollectionKind getCollectionKind()
    {
        return ((CollectionType<T>) type).getCollectionKind();
    }

    /**
     * Reports whether or not this collection is a map.
     *
     * @return <tt>true</tt> if this collection is a map.
     */
    public boolean isMap()
    {
        return CollectionKind.Map.equals(getCollectionKind());
    }
}
