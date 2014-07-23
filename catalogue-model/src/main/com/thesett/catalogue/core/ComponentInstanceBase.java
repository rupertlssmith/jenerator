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

import com.thesett.aima.state.impl.ExtendableBeanState;
import com.thesett.catalogue.interfaces.ComponentInstance;

/**
 * ComponentInstanceBase is an abstract base implementation of the {@link ComponentInstance} interface. It should be
 * extended by all components to provide additional fields specific to them.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Provide the components type and type name.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public abstract class ComponentInstanceBase extends ExtendableBeanState implements ComponentInstance, Serializable
{
    /**
     * Gets the database id of this dimension element.
     *
     * @return The database id of this dimension element.
     */
    protected abstract Long getId();

    /**
     * Sets the database id of this dimension element.
     *
     * @param id The database id of this dimension element.
     */
    protected abstract void setId(Long id);
}
