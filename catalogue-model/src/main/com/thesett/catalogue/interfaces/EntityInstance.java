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

/**
 * An EntityInstance is a {@link com.thesett.aima.state.State} that is an instance of an {@link EntityType}. In
 * addition to the set of named and typed fields that a state has, all entities have a unique id that identifies them
 * within their realm of persistent storage. For example, the id may be a natural or surragate database key.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Provide an opaque internal storage key to uniquely identify an entity.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public interface EntityInstance extends ComponentInstance
{
    /**
     * Gets the internal persistence id of the entity.
     *
     * @return The id of the element. If the entity has not been persisted, this will be <tt>null</tt>.
     */
    public InternalId getOpaqueId();

    /** {@inheritDoc} */
    EntityType getComponentType();
}
