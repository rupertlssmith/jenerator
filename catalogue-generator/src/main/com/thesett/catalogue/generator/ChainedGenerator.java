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

import java.util.List;

import com.thesett.catalogue.interfaces.Catalogue;

/**
 * ChainedGenerator, chains a sequence of {@link Generator}s together, calling them succesively on the catalogue model.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Apply a sequence of generators to a catalogue model.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class ChainedGenerator implements Generator
{
    /** Holds the sequence of generators to apply. */
    private List<Generator> generators;

    /**
     * Creates a chained generator from the specified sequence of generators.
     *
     * @param generators The sequence of generators to apply.
     */
    public ChainedGenerator(List<Generator> generators)
    {
        this.generators = generators;
    }

    /** {@inheritDoc} */
    public Boolean apply(Catalogue catalogue)
    {
        boolean result = true;

        for (Generator generator : generators)
        {
            if (!generator.apply(catalogue))
            {
                result = false;

                break;
            }
        }

        return result;
    }
}
