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

import com.thesett.aima.state.ComponentType;

/**
 * An EntityType is a {@link com.thesett.aima.state.Type} that is a {@link ComponentType} that can be mapped onto
 * persistent and queryable storage, most often a relational database.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Provide the types and names of fields that make up a persistent entity.
 * <tr><td> Indicate whether or not an entity has a long lived external identifier.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public interface EntityType extends ComponentType, Serializable
{
    /**
     * Indicates whether or not the entity has a long lived external identifier.
     *
     * @return <tt>true</tt> if the entity has an external id.
     *
     * @see ExternalId
     */
    boolean isExternalId();
}
