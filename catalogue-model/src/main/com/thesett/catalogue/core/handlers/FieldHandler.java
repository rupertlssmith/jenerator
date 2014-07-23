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
package com.thesett.catalogue.core.handlers;

/**
 * A FieldHandler defines an optional transformation that can be applied to the fields of a type declaration
 * in the model, in order to assist with transforming it into first order logic. The transformations applied
 * are <tt>String</tt> to <tt>String</tt> mappings, and if a handler does not need to process a particular field
 * it may ignore it with a <tt>null</tt> mapping.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities
 * <tr><td> Apply a string to string mapping to a type declaration field.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public interface FieldHandler
{
    /**
     * Applies a <tt>String</tt> to <tt>String</tt> mapping to the specified type declaration field, as a named
     * property/value pair. If the handler does not wish to apply any mapping it should map the input onto
     * <tt>null</tt>.
     *
     * @param property The name of the field to map.
     * @param value    The value of the field.
     * @param more     If the field is one of a sequence and there are more in the sequence.
     *
     * @return A transformed field, or <tt>null</tt> to apply no transformation.
     */
    public String handleField(String property, Object value, boolean more);
}
