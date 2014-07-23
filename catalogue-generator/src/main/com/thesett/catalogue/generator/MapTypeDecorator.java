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
import com.thesett.catalogue.interfaces.MapType;

/**
 * MapTypeDecorator is a {@link TypeDecorator} for {@link MapType}s. It automatically decorates the type
 * of the map keys and elements when accessed through it.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Decorate a collection type and the type of its keys and elements too.
 *     <td> {@link TypeDecoratorFactory}, {@link MapType}.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class MapTypeDecorator<K, E> extends CollectionTypeDecorator<E> implements MapType<K, E>
{
    /**
     * Creates a type decorator for a map type, that decorates the type of the maps keys and elements too.
     *
     * @param type The map type to decorate.
     */
    public MapTypeDecorator(MapType type)
    {
        super(type);
    }

    /** {@inheritDoc} */
    public Type<K> getKeyType()
    {
        return TypeDecoratorFactory.decorateType(((MapType) type).getKeyType());
    }

    /** {@inheritDoc} */
    public void setKeyType(Type<K> type)
    {
        ((MapType) type).setKeyType(type);
    }
}
