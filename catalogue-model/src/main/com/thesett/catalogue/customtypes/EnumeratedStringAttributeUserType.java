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

import org.apache.log4j.Logger;
import org.hibernate.usertype.UserType;

import com.thesett.aima.attribute.impl.EnumeratedStringAttribute;

/**
 * Implements a hibernate custom type mapping for enumerated attributes.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Transform an enumerate string into a database column entry and the other way around.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public abstract class EnumeratedStringAttributeUserType implements UserType
{
    /** Used for logging. */
    private static final Logger log = Logger.getLogger(EnumeratedStringAttributeUserType.class);

    /** Holds the column types that the enumeration is persisted to. */
    private static final int[] SQL_TYPES = { Types.VARCHAR };

    /**
     * Gets the attribyte type class name.
     *
     * @return The attribyte type class name.
     */
    public abstract String getTypeName();

    /**
     * Returns the column types that the enumeration is persisted to.
     *
     * @return The column types that the enumeration is persisted to.
     */
    public int[] sqlTypes()
    {
        return SQL_TYPES;
    }

    /**
     * Gets the class that this is a hibernate user type for.
     *
     * @return HierarchyAttribute class.
     */
    public Class returnedClass()
    {
        log.debug("public Class returnedClass(): called");

        return EnumeratedStringAttribute.class;
    }

    /**
     * Implements a comparator for enumerated attribute types.
     *
     * @param  x The first object to compare.
     * @param  y The second object to compare.
     *
     * @return <tt>true</tt>If the two objects are identical, or identical enumerated attributes.
     */
    public boolean equals(Object x, Object y)
    {
        log.debug("public boolean equals(Object x, Object y): called");
        log.debug("x = " + x);
        log.debug("y = " + y);

        boolean result = (x == y) ? true : (((x != null) && (y != null)) ? x.equals(y) : false);
        log.debug("result = " + result);

        return result;
    }

    /**
     * Implements a delegator to the enumerated attributes hash code.
     *
     * @param  o The enumerated attribute to get the hash code for.
     *
     * @return The hash code of the enumerated attribute.
     */
    public int hashCode(Object o)
    {
        log.debug("public int hashCode(Object o): called");

        return o.hashCode();
    }

    /**
     * Extracts a enumerated attribute from a result set.
     *
     * @param  resultSet The result set.
     * @param  names     The column names to extract fields from.
     * @param  owner     The owner of the object.
     *
     * @return An enumerated attribute.
     *
     * @throws SQLException If there is an underlying SQLException it is allowed to fall through.
     */
    public Object nullSafeGet(ResultSet resultSet, String[] names, Object owner) throws SQLException
    {
        log.debug("public Object nullSafeGet(ResultSet resultSet, String[] names, Object owner): called");
        log.debug("resultSet = " + resultSet);
        log.debug("resultSet.getMetaData().getColumnCount() = " + resultSet.getMetaData().getColumnCount());

        // Extract the enumeration value as a string from the result set.
        String value = resultSet.getString(names[0]);
        log.debug("value = " + value);

        // Create a factory for the named attribute class.
        EnumeratedStringAttribute.EnumeratedStringAttributeFactory factory =
            EnumeratedStringAttribute.getFactoryForClass(getTypeName());

        // Convert the array of fields into a hierarhcy attribute using the factory.
        EnumeratedStringAttribute h = factory.createStringAttribute(value);

        return h;
    }

    /**
     * Sets the component field of an enumerated attribute in a prepared statement ready for writing to the database.
     *
     * @param  statement The prepared statement to write the enumerated attribute into.
     * @param  value     The enumerated attribute to write.
     * @param  index     The index to start inserting into the prepared statement at.
     *
     * @throws SQLException If there is an underlying SQLException it is allowed to fall through.
     */
    public void nullSafeSet(PreparedStatement statement, Object value, int index) throws SQLException
    {
        log.debug("public void nullSafeSet(PreparedStatement statement, Object value, int index): called");
        log.debug("value = " + value);
        log.debug("index = " + index);

        // Cast the value to insert to a hierarcy attribute.
        EnumeratedStringAttribute h = (EnumeratedStringAttribute) value;

        // Check if the value to set is null and set a null value if so.
        if (value == null)
        {
            statement.setNull(index, Types.VARCHAR);
        }

        // The value is not a null so set the string field.
        else
        {
            String valueAsString = h.getStringValue();
            log.debug("valueAsString = " + valueAsString);
            statement.setString(index, valueAsString);
        }
    }

    /**
     * Creates a deep copy of an enuermated attribute.
     *
     * @param  value The enumerated attribute to copy.
     *
     * @return An independant copy of the enumerated attribute.
     *
     * @todo   Consider providing this clone method as a convenience method on enumerated attribute itself.
     */
    public Object deepCopy(Object value)
    {
        log.debug("public Object deepCopy(Object value): called");

        // Cast the object to be copied to an enumerated attribute.
        EnumeratedStringAttribute h = (EnumeratedStringAttribute) value;
        log.debug("h (to copy) = " + h);

        // Extract the type name and byte representation of the attribute.
        String typeName = h.getType().getName();
        byte byteRepresentation = h.getByteFromAttribute();

        // Get the factory for the named type.
        EnumeratedStringAttribute.EnumeratedStringAttributeFactory factory =
            EnumeratedStringAttribute.getFactoryForClass(typeName);

        // Use the factory to build a new enumerated attribute from its int representation.
        EnumeratedStringAttribute copy = factory.getAttributeFromByte(byteRepresentation);
        log.debug("copy = " + copy);

        return copy;
    }

    /**
     * Reports whether or not the enumerated attribute type is mutable. It is not.
     *
     * @return <tt>false</tt>
     */
    public boolean isMutable()
    {
        log.debug("public boolean isMutable(): called");

        return false;
    }

    /**
     * Provides additional operations to perform during deserialization. Does nothing and just returns the deserialized
     * object untouched.
     *
     * @param  cached The serializable object.
     * @param  owner  The owner of the object.
     *
     * @return The untouched serializable object.
     */
    public Object assemble(Serializable cached, Object owner)
    {
        return cached;
    }

    /**
     * Provides additional operations to perform during serialization. Does nothing and just returns the object as a
     * serializable object.
     *
     * @param  value The object to convert into a serializable.
     *
     * @return The object to serialize as a serializable object.
     */
    public Serializable disassemble(Object value)
    {
        return (Serializable) value;
    }

    /**
     * During merge, replace the existing (target) value in the entity we are merging to with a new (original) value
     * from the detached entity we are merging. For immutable objects, or null values, it is safe to simply return the
     * first parameter. For mutable objects, it is safe to return a copy of the first parameter. However, since user
     * types can define component values, it might make sense to recursively replace component values in the target
     * object.
     *
     * <p/>EnumeratedStringAttribute types are mutable just not declared to be to hibernate. For this reason a deep copy
     * of the original is returned.
     *
     * @param  original The original object to merge into the target.
     * @param  target   The target object to merge into.
     * @param  owner    The owner of the object.
     *
     * @return A deep copy of the original.
     */
    public Object replace(Object original, Object target, Object owner)
    {
        return deepCopy(original);
    }
}
