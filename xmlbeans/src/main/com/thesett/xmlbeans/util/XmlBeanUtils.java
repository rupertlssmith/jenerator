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
package com.thesett.xmlbeans.util;

import com.thesett.common.reflect.Memento;
import com.thesett.common.util.ReflectionUtils;
import com.thesett.xmlbeans.reflect.XmlBeanMemento;
import com.thesett.xmlbeans.types.BeanType;

/**
 * Provides helper methods for working with Java beans for serializing or deserializing them to XML using the xmlbeans
 * 'prog-type.xsd' schema.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Deserialize a bean definition from XML.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class XmlBeanUtils
{
    /**
     * Creates a Java bean from its {@link BeanType} form. The BeanType form is a serialized form of the bean used
     * to marshall/unmarshall it as XML. Prior to calling this method, the bean class to instantiate should be checked
     * against the BeanType form, in order to make sure that their fields match. This method does not expect to
     * encounter any errors, so if it does, they will be thrown as Runtimes.
     *
     * @param beanType The BeanType stored form of the bean.
     * @param <E>      The type of bean to instantiate.
     *
     * @return An instantiated Java bean with its parameters filled in from the BeanType form.
     */
    public static <E> E instantiateFromBeanType(BeanType beanType)
    {
        try
        {
            String className = beanType.getImplClass();
            Memento memento = new XmlBeanMemento(beanType);
            memento.capture();

            E bean = (E) ReflectionUtils.newInstance(ReflectionUtils.forName(className));
            memento.restore(bean);

            return bean;
        }
        catch (NoSuchFieldException e)
        {
            // Validation should already have ensured that this cannot happen. Consider altering the memento
            // interface so that this can be checked for, or allow this method to fail with a checked
            // exception.
            throw new RuntimeException(e);
        }
    }
}
