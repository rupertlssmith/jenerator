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
package com.thesett.catalogue.hibernate;

import org.hibernate.Session;

import com.thesett.catalogue.config.HibernateConfigBean;
import com.thesett.common.config.ConfigBeanContext;
import com.thesett.common.config.ConfigException;
import com.thesett.common.config.Configurator;

/**
 * HibernateUtil.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class HibernateUtil
{
    /** Holds a reference to the hibernate config bean that supplies the sessions. */
    private static HibernateConfigBean hibernateBean;

    /**
     * Gets a hibernate session associated with the current thread, creating a new one if necessary.
     *
     * @return A hibernate session associated with the current thread.
     */
    public static Session getCurrentSession()
    {
        // Ensure that this utility has been initialized with its hibernate config bean.
        initialize();

        return hibernateBean.getSessionFactory().getCurrentSession();
    }

    /** Closes the session associated with the current thread and clears the association with the thread. */
    public static void closeSession()
    {
        // Ensure that this utility has been initialized with its hibernate config bean.
        initialize();

        hibernateBean.getSessionFactory().getCurrentSession().close();
    }

    /** Associates a transaction with the current thread, creating a new one if necessary. */
    public static void beginTransaction()
    {
        // Ensure that this utility has been initialized with its hibernate config bean.
        initialize();

        hibernateBean.getSessionFactory().getCurrentSession().beginTransaction();
    }

    /**
     * Commits the transaction associated with the current thread if one exists, provided it has not already been
     * committed or rolled back. Removes the association of the current thread with the transaction.
     *
     * <p/>If hibernate throws any exceptions during the commit of the transaction then rollback is called instead.
     */
    public static void commitTransaction()
    {
        // Ensure that this utility has been initialized with its hibernate config bean.
        initialize();

        hibernateBean.getSessionFactory().getCurrentSession().getTransaction().commit();
    }

    /**
     * Rolls back the transaction associated with the current thread if one exists, provided it has not already been
     * committed or rolled back. Removes the association of the current thread with the transaction and closes the
     * current threads session.
     */
    public static void rollbackTransaction()
    {
        // Ensure that this utility has been initialized with its hibernate config bean.
        initialize();

        hibernateBean.getSessionFactory().getCurrentSession().getTransaction().rollback();
    }

    /**
     * Initialized this utility class by looking up its required hibernate config been with the configurator. The
     * hibernate config bean must be set up or a runtime exception will be raised.
     */
    private static synchronized void initialize()
    {
        // Check that initilaization has not already been done.
        if (hibernateBean == null)
        {
            // Get the configurator and extract the required hibernate config bean from it.
            ConfigBeanContext configBeanContext = Configurator.lookupConfigurator();

            try
            {
                hibernateBean =
                    (HibernateConfigBean) configBeanContext.getConfiguredBean(HibernateConfigBean.class.getName());
            }
            catch (ConfigException e)
            {
                throw new IllegalStateException(e);
            }
        }
    }
}
