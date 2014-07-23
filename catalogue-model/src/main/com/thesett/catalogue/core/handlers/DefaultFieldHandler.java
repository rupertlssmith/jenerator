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
 * DefaultFieldHandler transforms its input field into a functor of arity 1. The name of the functor is the
 * name of the field, and the argument is its value.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Transforms a type declaration field into a functor of arity one.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class DefaultFieldHandler implements FieldHandler
{
    /**
     * {@inheritDoc}
     *
     * <p/>This mapping transform the property/value pair into a functor, property(value), optionally adding a
     * continuation ',' if there are more fields in the sequence, so that the sequence may be parsed as a list
     * body.
     */
    public String handleField(String property, Object value, boolean more)
    {
        return property + "(" + value + ")" + (more ? ", " : "");
    }
}
