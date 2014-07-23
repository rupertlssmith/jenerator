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

import com.thesett.catalogue.interfaces.Catalogue;
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
     * @param catalogue The model to generate from.
     *
     * @return <tt>true</tt> if the generation was succesfull.
     */
    public Boolean apply(Catalogue catalogue);
}
