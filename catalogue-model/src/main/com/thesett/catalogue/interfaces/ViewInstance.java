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
package com.thesett.catalogue.interfaces;

/**
 * A ViewInstance is an instance of a {@link ViewType}. It is a {@link com.thesett.aima.state.State} that contains the
 * fields of name and type matching some {@link ViewType}.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Provide a subset of the types of fields that make up a component.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public interface ViewInstance extends ComponentInstance
{
}
