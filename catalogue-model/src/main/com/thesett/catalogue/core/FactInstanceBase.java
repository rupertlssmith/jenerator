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

import com.thesett.catalogue.interfaces.FactInstance;
import com.thesett.catalogue.interfaces.FactType;

/**
 * FactInstanceBase provides a base class for implementing fact instances.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td>
 * </table></pre>
 *
 * @author Rupert Smith
 */
public abstract class FactInstanceBase extends EntityInstanceBase implements FactInstance, Serializable
{
    /** {@inheritDoc} */
    public abstract FactType getComponentType();
}
