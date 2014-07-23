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

import java.util.List;

import com.thesett.catalogue.interfaces.Catalogue;

/**
 * ChainedGenerator, chains a sequence of {@link Generator}s together, calling them succesively on the catalogue
 * model.
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
