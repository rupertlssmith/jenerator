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

import com.thesett.aima.attribute.impl.TimeRangeType;
import com.thesett.aima.state.Type;

/**
 * TimeRangeTypeDecorator decorates a time range type.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Mark a time range type as a range type.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class TimeRangeTypeDecorator extends TypeDecorator
{
    /**
     * Creates a decorator on a time range type.
     *
     * @param type The time range type to decorate.
     */
    public TimeRangeTypeDecorator(Type type)
    {
        super(type);
    }

    /** {@inheritDoc} */
    public boolean isRangeType()
    {
        return true;
    }
}
