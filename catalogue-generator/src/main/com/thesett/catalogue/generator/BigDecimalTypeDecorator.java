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

import java.math.BigDecimal;

import com.thesett.aima.attribute.impl.BigDecimalType;
import com.thesett.aima.state.Type;

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
    public BigDecimalTypeDecorator(Type type)
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
