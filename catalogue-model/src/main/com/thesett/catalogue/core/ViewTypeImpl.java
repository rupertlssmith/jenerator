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
package com.thesett.catalogue.core;

import java.util.Map;
import java.util.Set;

import com.thesett.aima.state.ComponentType;
import com.thesett.aima.state.Type;
import com.thesett.catalogue.interfaces.ViewType;

/**
 * ViewTypeImpl is the type of view components. A view component is a sub-set of the fields of a component, with
 * identical types of the fields in the subset to their corresponding types in a larger component. In this way a
 * component that 'conforms' to the view is a sub-type of it, and a restricted view of the component may be accessed
 * through a particular view. A view is analogous to an interface in Java.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> None yet.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class ViewTypeImpl extends ComponentTypeImpl implements ViewType
{
    /**
     * Creates a named view with the specified set of fields and underlying implementation.
     *
     * @param name                 The name of the view.
     * @param attributes           The fields of the view.
     * @param operationalClassName An implementing class.
     * @param immediateAncestors   The immediate ancestors of this type.
     */
    public ViewTypeImpl(String name, Map<String, Type> attributes, String operationalClassName,
        Set<ComponentType> immediateAncestors)
    {
        super(name, attributes, operationalClassName, immediateAncestors);
    }
}
