/*
 * Copyright The Sett Ltd, 2005 to 2014.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
