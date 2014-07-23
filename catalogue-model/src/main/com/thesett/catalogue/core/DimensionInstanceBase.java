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

import com.thesett.catalogue.interfaces.DimensionInstance;
import com.thesett.catalogue.interfaces.DimensionType;

/**
 * DimensionInstanceBase provides a base class for implementing dimension instances.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * </table></pre>
 *
 * @author Rupert Smith
 */
public abstract class DimensionInstanceBase extends EntityInstanceBase implements DimensionInstance, Serializable
{
    /** {@inheritDoc} */
    public abstract DimensionType getComponentType();
}
