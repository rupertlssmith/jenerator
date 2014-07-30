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
     * Creates a Java bean from its {@link BeanType} form. The BeanType form is a serialized form of the bean used to
     * marshall/unmarshall it as XML. Prior to calling this method, the bean class to instantiate should be checked
     * against the BeanType form, in order to make sure that their fields match. This method does not expect to
     * encounter any errors, so if it does, they will be thrown as Runtimes.
     *
     * @param  beanType The BeanType stored form of the bean.
     * @param  <E>      The type of bean to instantiate.
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
