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

import java.math.BigDecimal;

import com.thesett.aima.attribute.impl.BigDecimalType;

/**
 * BigDecimalTypeDecorator is a {@link TypeDecorator} for {@link BigDecimalType}s. It provides the scale and precision
 * fields of the decorated type.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Provide the precision and scale. <td> {@link BigDecimalType}
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class BigDecimalTypeDecorator extends TypeDecorator<BigDecimal> implements BigDecimalType
{
    /**
     * Creates a decorated big decimal type.
     *
     * @param type The big decimal type to decorate.
     */
    public BigDecimalTypeDecorator(BigDecimalType type)
    {
        super(type);
    }

    /** {@inheritDoc} */
    public int getPrecision()
    {
        return ((BigDecimalType) type).getPrecision();
    }

    /** {@inheritDoc} */
    public int getScale()
    {
        return ((BigDecimalType) type).getScale();
    }
}
