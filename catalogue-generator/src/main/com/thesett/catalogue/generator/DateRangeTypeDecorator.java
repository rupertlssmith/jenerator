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

import com.thesett.aima.attribute.impl.DateRangeType;

/**
 * DateRangeTypeDecorator decorates a date range type.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Mark a date range type as a range type.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class DateRangeTypeDecorator extends TypeDecorator
{
    /**
     * Creates a decorator on a date range type.
     *
     * @param type The date range type to decorate.
     */
    public DateRangeTypeDecorator(DateRangeType type)
    {
        super(type);
    }

    /** {@inheritDoc} */
    public boolean isRangeType()
    {
        return true;
    }
}
