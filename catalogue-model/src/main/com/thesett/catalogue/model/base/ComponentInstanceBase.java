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
package com.thesett.catalogue.model.base;

import java.io.Serializable;

import com.thesett.aima.state.impl.ExtendableBeanState;
import com.thesett.catalogue.model.ComponentInstance;

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
}
