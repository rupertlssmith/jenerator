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
package com.thesett.catalogue.customtypes;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.logging.Logger;

import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.type.StringType;
import org.hibernate.type.Type;
import org.hibernate.usertype.CompositeUserType;

import com.thesett.aima.attribute.impl.HierarchyAttribute;
import com.thesett.aima.attribute.impl.HierarchyAttributeFactory;

/**
 * Implements a hibernate custom type mapping for hierarchy attributes, exposing their fields as a composite type so
 * that they can be mapped to individual database columns. This class is abstract because the number of fields depends
 * on the number of levels in the hierarchy, so implementations of it should be provided for every hierarchy attribute
 * type, declaring the appropriate fields by implementing the {@link #getPropertyNames} method and the hierarchy
 * attribute type through the {@link #getTypeName()} method.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Map a hierarchy type as a Hibernate user type onto multiple string columns.
 *     <td> {@link HierarchyAttribute}, {@link HierarchyAttributeFactory}
 * </table></pre>
 *
 * @author Rupert Smith
 */
public abstract class HierarchyAttributeCompositeUserType implements CompositeUserType
{
    /** Used for debugging purposes. */
    private static final Logger LOG = Logger.getLogger(HierarchyAttributeCompositeUserType.class.getName());

    /**
     * Gets the attribyte type class name.
     *
     * @return The attribyte type class name.
     */
    public abstract String getTypeName();

    /**
     * Provides the count of the number of columns that this hierarchy type uses.
     *
     * @return The count of the number of columns that this hierarchy type uses.
     */
    public abstract int getColumnCount();

    /**
     * Returns the attribute names of the hierarchy attribute. Implementation should provide names and the correct
     * number to correspond to the depth of the hierarchy.
     *
     * @return The property names of the hierarchy.
     */
    public String[] getPropertyNames()
    {
        HierarchyAttributeFactory factory = HierarchyAttribute.getFactoryForClass(getTypeName());

        return factory.getLevelNames();
    }

    /**
     * Gets the class that this is a hibernate user type for.
     *
     * @return HierarchyAttribute class.
     */
    public Class returnedClass()
    {
        LOG.fine("public Class returnedClass(): called");

        return HierarchyAttribute.class;
    }

    /**
     * Implements a comparator for hierarchy attribute types.
     *
     * @param  x The first object to compare.
     * @param  y The second object to compare.
     *
     * @return <tt>true</tt>If the two objects are identical, or identical hierarchy attributes.
     */
    public boolean equals(Object x, Object y)
    {
        LOG.fine("public boolean equals(Object x, Object y): called");
        LOG.fine("x = " + x);
        LOG.fine("y = " + y);

        boolean result = (x == y) ? true : (((x != null) && (y != null)) ? x.equals(y) : false);
        LOG.fine("result = " + result);

        return result;
    }

    /**
     * Implements a delegator to the hierarchy attributes hash code.
     *
     * @param  o The hierarchy attribute to get the hash code for.
     *
     * @return The hash code of the hierarchy attribute.
     */
    public int hashCode(Object o)
    {
        LOG.fine("public int hashCode(Object o): called");

        return o.hashCode();
    }

    /**
     * Creates a deep copy of a hierarchy attribute.
     *
     * @param  value The hierarchy attribute to copy.
     *
     * @return An independant copy of the hierarchy attribute.
     */
    public Object deepCopy(Object value)
    {
        LOG.fine("public Object deepCopy(Object value): called");

        // Cast the object to be copied to a hierarchy attribute.
        HierarchyAttribute h = (HierarchyAttribute) value;
        LOG.fine("h (to copy) = " + h);

        // Extract the type name and int representation of the attribute.
        String typeName = h.getType().getName();
        int intRepresentation = h.getIntFromAttribute();

        // Get the factory for the named type.
        HierarchyAttributeFactory factory = HierarchyAttribute.getFactoryForClass(typeName);

        // Use the factory to build a new hierarchy attribute from its int representation.
        HierarchyAttribute copy = factory.getAttributeFromInt(intRepresentation);
        LOG.fine("copy = " + copy);

        return copy;
    }

    /**
     * Reports whether or not the hierarchy attribute type is mutable. It is but this method reports it as immutable
     * because it should not be used in a mutable way.
     *
     * @return <tt>false</tt>
     */
    public boolean isMutable()
    {
        LOG.fine("public boolean isMutable(): called");

        return false;
    }

    /**
     * Extracts a hierarchy attribute from a result set. Any trailing nulls are removed before trying to create the
     * hierarchy attribute from the label path.
     *
     * @param  resultSet The result set.
     * @param  names     The column names to extract fields from.
     * @param  session   The hibernate session.
     * @param  owner     The owner of the object.
     *
     * @return A hierarchy attribute.
     *
     * @throws SQLException If there is an underlying SQLException it is allowed to fall through.
     */
    public Object nullSafeGet(ResultSet resultSet, String[] names, SessionImplementor session, Object owner)
        throws SQLException
    {
        LOG.fine(
            "public Object nullSafeGet(ResultSet resultSet, String[] names, SessionImplementor session, Object owner): called");
        LOG.fine("resultSet = " + resultSet);
        LOG.fine("resultSet.getMetaData().getColumnCount() = " + resultSet.getMetaData().getColumnCount());

        // Extract all the named fields as a string array.
        String[] fields = new String[names.length];

        // Scan the path list until a null is encountered. The path list terminates once a trailing null is found.
        int trueSize = 0;

        for (int i = 0; i < fields.length; i++)
        {
            fields[i] = resultSet.getString(names[i]);

            LOG.fine("names[" + i + "] = " + names[i]);
            LOG.fine("fields[" + i + "] = " + fields[i]);

            if (fields[i] != null)
            {
                trueSize++;
            }
            else
            {
                break;
            }
        }

        LOG.fine("trueSize = " + trueSize);

        String[] trueFields = new String[trueSize];
        System.arraycopy(fields, 0, trueFields, 0, trueSize);

        // Create a factory for the named attribute class.
        HierarchyAttributeFactory factory = HierarchyAttribute.getFactoryForClass(getTypeName());

        // Convert the array of fields into a hierarhcy attribute using the factory.
        HierarchyAttribute h = factory.createHierarchyAttribute(trueFields);

        return h;
    }

    /**
     * Sets the component fields of a hierarchy attribute in a prepared statement ready for writing to the database.
     *
     * @param  statement The prepared statement to write the hierarchy attribute into.
     * @param  value     The hierarchy attribute to write.
     * @param  index     The index to start inserting into the prepared statement at.
     * @param  session   The hibernate session.
     *
     * @throws SQLException If there is an underlying SQLException it is allowed to fall through.
     */
    public void nullSafeSet(PreparedStatement statement, Object value, int index, SessionImplementor session)
        throws SQLException
    {
        LOG.fine(
            "public void nullSafeSet(PreparedStatement statement, Object value, int index, SessionImplementor session): called");
        LOG.fine("value = " + value);
        LOG.fine("index = " + index);

        // Cast the value to insert to a hierarcy attribute.
        HierarchyAttribute h = (HierarchyAttribute) value;

        // Get the maximum depth of the hierarchy type.
        int maxLevels = getColumnCount();

        // Walk down the hierarchy, inserting all its values into the statement.
        for (int i = 0; i < maxLevels; i++)
        {
            // Check if the value to set is null and set all null values if so.
            if (value == null)
            {
                statement.setNull(index + i, Types.VARCHAR);
            }

            // The value is not a null so set all the string fields.
            else
            {
                String valueAtLevel = h.getValueAtLevel(i);
                LOG.fine("valueAtLevel(" + i + ") = " + valueAtLevel);

                statement.setString(index + i, valueAtLevel);
            }
        }
    }

    /**
     * Returns an array of string field types, the correct size to hold the hierarchy attribute.
     *
     * @return The correct number of string field types.
     */
    public Type[] getPropertyTypes()
    {
        // All fields are of type string, just need to know how many there are, so get this from the property names
        // method.
        String[] names = getPropertyNames();

        // Create a type array big enough for all the fields and fill it with string types.
        Type[] result = new Type[names.length];

        for (int i = 0; i < result.length; i++)
        {
            result[i] = StringType.INSTANCE;
        }

        return result;
    }

    /**
     * Extracts an indexed field from a hierarchy attribute.
     *
     * @param  component The hierarchy attribute to extract an indexed field from.
     * @param  property  The index of the field to extract.
     *
     * @return The indexed field of the hierarchy attribute.
     */
    public Object getPropertyValue(Object component, int property)
    {
        // Cast the object to extract indexes values from to a hierarchy attribute.
        HierarchyAttribute h = (HierarchyAttribute) component;

        // Extract the indexed property from a hierarchy level.
        return h.getValueAtLevel(property);
    }

    /**
     * Tries to set an indexed field of a hierarchy attribute. This operation is not supported as piece-wise assignment
     * to hierarchy attributes is not supported.
     *
     * @param component The hierarchy attribute to set an indexed field of.
     * @param property  The index of the field to set.
     * @param value     The value to set.
     */
    public void setPropertyValue(Object component, int property, Object value)
    {
        throw new UnsupportedOperationException(
            "HierarchyAttribute does not support piece-wise assignment of its fields.");
    }

    /**
     * Provides additional operations to perform during deserialization. Does nothing and just returns the deserialized
     * object untouched.
     *
     * @param  cached  The serializable object.
     * @param  session The hibernate session.
     * @param  owner   The owner of the object.
     *
     * @return The untouched serializable object.
     */
    public Object assemble(Serializable cached, SessionImplementor session, Object owner)
    {
        return cached;
    }

    /**
     * Provides additional operations to perform during serialization. Does nothing and just returns the object as a
     * serializable object.
     *
     * @param  value   The object to convert into a serializable.
     * @param  session The hibernate session.
     *
     * @return The object to serialize as a serializable object.
     */
    public Serializable disassemble(Object value, SessionImplementor session)
    {
        return (Serializable) value;
    }

    /**
     * During merge, replace the existing (target) value in the entity we are merging to with a new (original) value
     * from the detached entity we are merging. For immutable objects, or null values, it is safe to simply return the
     * first parameter. For mutable objects, it is safe to return a copy of the first parameter. However, since
     * composite user types often define component values, it might make sense to recursively replace component values
     * in the target object.
     *
     * <p/>HierarchyAttribute types are mutable just not declared to be to hibernate. For this reason a deep copy of the
     * original is returned.
     *
     * @param  original The original object to merge into the target.
     * @param  target   The target object to merge into.
     * @param  session  The hibernate session.
     * @param  owner    The owner of the object.
     *
     * @return A deep copy of the original.
     */
    public Object replace(Object original, Object target, SessionImplementor session, Object owner)
    {
        return deepCopy(original);
    }
}
