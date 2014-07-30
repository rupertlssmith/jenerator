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
import org.hibernate.HibernateException;
import org.hibernate.usertype.UserType;

import com.thesett.aima.attribute.time.TimeOnly;

/**
 * TimeOnlyUserType implements a hibernate custom type mapping for TimeOnly timestamps.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Transform a timestamp in milliseconds into a database column entry and the other way around.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class TimeOnlyUserType implements UserType
{
    /** Used for logging. */
    private static final Logger log = Logger.getLogger(TimeOnlyUserType.class);

    /** Holds the column types that the enumeration is persisted to. */
    private static final int[] SQL_TYPES = { Types.INTEGER };

    /** {@inheritDoc} */
    public int[] sqlTypes()
    {
        return SQL_TYPES;
    }

    /** {@inheritDoc} */
    public Class returnedClass()
    {
        log.debug("public Class returnedClass(): called");

        return TimeOnly.class;
    }

    /** {@inheritDoc} */
    public boolean equals(Object x, Object y) throws HibernateException
    {
        log.debug("public boolean equals(Object x, Object y): called");
        log.debug("x = " + x);
        log.debug("y = " + y);

        boolean result = (x == y) ? true : (((x != null) && (y != null)) ? x.equals(y) : false);
        log.debug("result = " + result);

        return result;
    }

    /** {@inheritDoc} */
    public int hashCode(Object o) throws HibernateException
    {
        log.debug("public int hashCode(Object o): called");

        return o.hashCode();
    }

    /** {@inheritDoc} */
    public Object nullSafeGet(ResultSet rs, String[] names, Object owner) throws HibernateException, SQLException
    {
        log.debug("public Object nullSafeGet(ResultSet resultSet, String[] names, Object owner): called");
        log.debug("resultSet = " + rs);
        log.debug("resultSet.getMetaData().getColumnCount() = " + rs.getMetaData().getColumnCount());

        // Extract the timestamp as a string from the result set.
        long value = rs.getInt(names[0]);
        log.debug("value = " + value);

        // Convert the array of fields into a timeonly.
        return new TimeOnly(value);
    }

    /** {@inheritDoc} */
    public void nullSafeSet(PreparedStatement st, Object value, int index) throws HibernateException, SQLException
    {
        log.debug("public void nullSafeSet(PreparedStatement statement, Object value, int index): called");
        log.debug("value = " + value);
        log.debug("index = " + index);

        // Cast the value to insert to a time only.
        TimeOnly h = (TimeOnly) value;

        // Check if the value to set is null and set a null value if so.
        if (value == null)
        {
            st.setNull(index, Types.INTEGER);
        }
        else // The value is not a null so set the integer field.
        {
            long timestamp = h.getMilliseconds();
            log.debug("valueAsLong = " + timestamp);
            st.setLong(index, timestamp);
        }
    }

    /** {@inheritDoc} */
    public Object deepCopy(Object value) throws HibernateException
    {
        log.debug("public Object deepCopy(Object value): called");

        // Cast the object to be copied to an enumerated attribute.
        TimeOnly t = (TimeOnly) value;
        log.debug("t (to copy) = " + t);

        // Createa copy with the same timestamp.
        TimeOnly copy = new TimeOnly(t.getMilliseconds());
        log.debug("copy = " + copy);

        return copy;
    }

    /** {@inheritDoc} */
    public boolean isMutable()
    {
        log.debug("public boolean isMutable(): called");

        return false;
    }

    /** {@inheritDoc} */
    public Serializable disassemble(Object value) throws HibernateException
    {
        return (Serializable) value;
    }

    /** {@inheritDoc} */
    public Object assemble(Serializable cached, Object owner) throws HibernateException
    {
        return cached;
    }

    /** {@inheritDoc} */
    public Object replace(Object original, Object target, Object owner) throws HibernateException
    {
        return deepCopy(original);
    }
}
