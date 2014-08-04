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
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.usertype.UserType;

import com.thesett.aima.attribute.time.DateOnly;

/**
 * DateOnlyUserType implements a Hibernate user type for {@link DateOnly} objects, providing the necessary details to
 * load and store to a database column.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td>  Transform a date only into a database column entry and the other way around.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class DateOnlyUserType implements UserType
{
    /** Used for logging. */
    private static final Logger log = Logger.getLogger(DateOnlyUserType.class);

    /** Holds the column types that the enumeration is persisted to. */
    private static final int[] SQL_TYPES = { Types.DATE };

    /** {@inheritDoc} */
    public int[] sqlTypes()
    {
        return SQL_TYPES;
    }

    /** {@inheritDoc} */
    public Class returnedClass()
    {
        log.debug("public Class returnedClass(): called");

        return DateOnly.class;
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
    public Object nullSafeGet(ResultSet rs, String[] names, SessionImplementor sessionImplementor, Object o)
        throws HibernateException, SQLException
    {
        log.debug("public Object nullSafeGet(ResultSet resultSet, String[] names, Object owner): called");
        log.debug("resultSet = " + rs);
        log.debug("resultSet.getMetaData().getColumnCount() = " + rs.getMetaData().getColumnCount());

        // Extract the timestamp as a string from the result set.
        Date value = rs.getDate(names[0]);
        log.debug("value = " + value);

        // Convert the array of fields into a date only.
        if (value == null)
        {
            return null;
        }
        else
        {
            return new DateOnly(value.getYear(), value.getMonth(), value.getDate());
        }
    }

    /** {@inheritDoc} */
    public void nullSafeSet(PreparedStatement st, Object value, int index, SessionImplementor sessionImplementor)
        throws HibernateException, SQLException
    {
        log.debug("public void nullSafeSet(PreparedStatement statement, Object value, int index): called");
        log.debug("value = " + value);
        log.debug("index = " + index);

        // Cast the value to insert to a time only.
        DateOnly h = (DateOnly) value;

        // Check if the value to set is null and set a null value if so.
        if (value == null)
        {
            st.setNull(index, Types.DATE);
        }
        else // The value is not a null so set the integer field.
        {
            Date date = new Date(h.getYear(), h.getMonth(), h.getDate());
            log.debug("valueAsLong = " + date);
            st.setDate(index, date);
        }
    }

    /** {@inheritDoc} */
    public Object deepCopy(Object value) throws HibernateException
    {
        log.debug("public Object deepCopy(Object value): called");

        if (value == null)
        {
            return null;
        }

        // Cast the object to be copied to an enumerated attribute.
        DateOnly date = (DateOnly) value;
        log.debug("t (to copy) = " + date);

        // Create a copy with the same timestamp.
        DateOnly copy = new DateOnly(date.getYear(), date.getMonth(), date.getDate());
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
