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
