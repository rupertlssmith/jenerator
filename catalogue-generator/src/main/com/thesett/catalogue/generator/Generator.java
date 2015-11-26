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

import com.thesett.catalogue.model.Catalogue;
import com.thesett.common.util.Function;

/**
 * Generator defines a controller for generating transformations of a catalogue model. In this case the transformation
 * is expected to have the side effect of writing out generated code from the model, and the return type is a boolean
 * flag, used to indicate that the generation process was succesfull.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Generate output from the catalogue model, indicating if succesfull.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public interface Generator extends Function<Catalogue, Boolean>
{
    /**
     * Generates output from a catalogue model.
     *
     * @param  catalogue The model to generate from.
     *
     * @return <tt>true</tt> if the generation was succesfull.
     */
    Boolean apply(Catalogue catalogue);
}
