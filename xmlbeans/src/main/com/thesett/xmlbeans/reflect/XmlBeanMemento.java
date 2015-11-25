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
package com.thesett.xmlbeans.reflect;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBElement;

import com.thesett.common.error.NotImplementedException;
import com.thesett.common.reflect.BeanMemento;
import com.thesett.common.reflect.Memento;
import com.thesett.common.util.TypeConverter;
import com.thesett.xmlbeans.types.BeanType;
import com.thesett.xmlbeans.types.BoolType;
import com.thesett.xmlbeans.types.DateType;
import com.thesett.xmlbeans.types.DoubleType;
import com.thesett.xmlbeans.types.FloatType;
import com.thesett.xmlbeans.types.IntType;
import com.thesett.xmlbeans.types.LongType;
import com.thesett.xmlbeans.types.RootType;
import com.thesett.xmlbeans.types.ShortType;
import com.thesett.xmlbeans.types.StringType;

/**
 * XmlBeanMemento is a {@link Memento} that captures its stage from a Java bean serialized as XML, that conforms to the
 * schema defined in 'prog-types.xsd'. This schema includes a type for instantiating Java beans from, that has been
 * compiled using JAXB into the model class {@link BeanType}. This memento captures its state from a {@link BeanType}
 * and can restore it to a Java bean that matches the BeanType.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities
 * <tr><td> Create a memento from an object. <td> {@link BeanType}.
 * <tr><td> Write to an objects fields from a memento.
 * <tr><td> Read field values.
 * <tr><td> Modifiy field values.
 * <tr><td> Get list of all fields.
 * </table></pre>
 *
 * @author Rupert Smith
 * @todo   Make this so that it can capture/restore from both beans and BeanTypes. Then it can be used universally to
 *         serialize/deserialize beans from xml.
 */
public class XmlBeanMemento implements Memento
{
    /** Holds the properties of the object that this memento has captured. */
    private Map<String, Object> values = new HashMap<String, Object>();

    /** The object that this is a memento for. */
    BeanType beanType;

    /**
     * Creates an XML Bean Memento on the specified BeanType object.
     *
     * @param beanType The BeanType to create the memento on.
     */
    public XmlBeanMemento(BeanType beanType)
    {
        this.beanType = beanType;
    }

    /** Captures an objects properties in this memento. */
    public void capture()
    {
        for (JAXBElement<? extends RootType> element : beanType.getAssignment())
        {
            RootType rootType = element.getValue();
            String propertyName = rootType.getName();
            TypeConverter.MultiTypeData multiTypeData = null;

            if (rootType instanceof BoolType)
            {
                BoolType boolType = (BoolType) rootType;
                multiTypeData = TypeConverter.getMultiTypeData(boolType.isValueOf());
            }
            else if (rootType instanceof DateType)
            {
                DateType dateType = (DateType) rootType;
                multiTypeData = TypeConverter.getMultiTypeData(dateType.getValueOf());
            }
            else if (rootType instanceof DoubleType)
            {
                DoubleType doubleType = (DoubleType) rootType;
                multiTypeData = TypeConverter.getMultiTypeData(doubleType.getValueOf());
            }
            else if (rootType instanceof FloatType)
            {
                FloatType floatType = (FloatType) rootType;
                multiTypeData = TypeConverter.getMultiTypeData(floatType.getValueOf());
            }
            else if (rootType instanceof IntType)
            {
                IntType intType = (IntType) rootType;
                multiTypeData = TypeConverter.getMultiTypeData(intType.getValueOf());
            }
            else if (rootType instanceof LongType)
            {
                LongType longType = (LongType) rootType;
                multiTypeData = TypeConverter.getMultiTypeData(longType.getValueOf());
            }
            else if (rootType instanceof ShortType)
            {
                ShortType shortType = (ShortType) rootType;
                multiTypeData = TypeConverter.getMultiTypeData(shortType.getValueOf());
            }
            else if (rootType instanceof StringType)
            {
                StringType stringType = (StringType) rootType;
                multiTypeData = TypeConverter.getMultiTypeData(stringType.getValueOf());
            }
            else
            {
                throw new RuntimeException("Unknown sub-type of 'RootType' encountered.");
            }

            values.put(propertyName, multiTypeData);
        }
    }

    /** {@inheritDoc} */
    public void captureNonNull()
    {
        throw new RuntimeException("Not implemented.");
    }

    /**
     * Restores the properties currently in this memento to the specified object.
     *
     * @param  ob The object to which the values from this memento should be restored.
     *
     * @throws NoSuchFieldException If a setter method could not be found for a property.
     */
    public void restore(Object ob) throws NoSuchFieldException
    {
        BeanMemento.restoreValues(ob, values);
    }

    /**
     * Gets the value of the named property of the specified class.
     *
     * @param  cls      The class in which the property to get is declared.
     * @param  property The name of the property.
     *
     * @return The object value of the property.
     *
     * @throws NoSuchFieldException If the named field does not exist on the class.
     */
    public Object get(Class cls, String property) throws NoSuchFieldException
    {
        throw new NotImplementedException();
    }

    /**
     * Sets the value of the named property as a multi type object.
     *
     * @param cls      The class in which the property is declared.
     * @param property The name of the property to set.
     * @param value    The multi type object to set that value from.
     */
    public void put(Class cls, String property, TypeConverter.MultiTypeData value)
    {
        throw new NotImplementedException();
    }

    /**
     * Places the specified value into the memento based on the property's declaring class and name.
     *
     * @param cls      The class in which the property is declared.
     * @param property The name of the property.
     * @param value    The value to store into this memento.
     */
    public void put(Class cls, String property, Object value)
    {
        throw new NotImplementedException();
    }

    /**
     * Generates a list of all the fields of the object that this memento maps for a given class.
     *
     * @param  cls The class to get all field names for.
     *
     * @return A collection of the field names or null if the specified class is not part of the objects class hierarchy
     *         chain.
     */
    public Collection getAllFieldNames(Class cls)
    {
        throw new NotImplementedException();
    }
}
