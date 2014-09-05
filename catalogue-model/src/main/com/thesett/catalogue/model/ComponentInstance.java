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
package com.thesett.catalogue.model;

import com.thesett.aima.state.State;

/**
 * A ComponentInstance is an instance of a {@link com.thesett.aima.state.ComponentType} type.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities
 * <tr><td> Provide the types of fields that make up a component.
 * <tr><td> Provide an internal id to identify the component instance uniquely by.
 * <tr><td> Provide a long lived external id to identify the component instance uniquely by.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public interface ComponentInstance extends State
{
}
