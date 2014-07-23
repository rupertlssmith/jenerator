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

import com.thesett.aima.state.Type;

/**
 * A MapType is a {@link com.thesett.aima.state.Type} that acts as a container for key/value instances of other types.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Encapsulate a key type.
 * <tr><td> Encapsulate an element type.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public interface MapType<K, E> extends CollectionType<E>
{
    /**
     * Provides the type of the keys that this map contains.
     *
     * @return The type of the keys that this map contains.
     */
    public Type<K> getKeyType();

    /**
     * Establishes the type of the keys that this map contains.
     *
     * @param type The type of the keys that this map contains.
     */
    public void setKeyType(Type<K> type);
}
