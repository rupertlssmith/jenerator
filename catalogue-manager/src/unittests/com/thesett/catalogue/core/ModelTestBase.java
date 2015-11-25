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
package com.thesett.catalogue.core;

import java.util.HashSet;
import java.util.Set;

import org.hibernate.Session;
import org.hibernate.Transaction;

import com.thesett.catalogue.config.HibernateConfigBean;
import com.thesett.catalogue.model.ExternalId;
import com.thesett.catalogue.model.base.EntityInstanceBase;
import com.thesett.catalogue.model.impl.InternalIdImpl;
import com.thesett.common.config.ConfigBeanContext;

/**
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td>
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class ModelTestBase extends ConfiguratorTestBase
{
    /** Holds a reference to the hibernate config bean, to get hibernate sessions from. */
    protected HibernateConfigBean hibernateBean;

    /** Holds a list of tables dirtied by tests, so that the tear down method knows which tables to clean up. */
    protected Set<String> dirtiedTables = new HashSet<String>();

    /** Create a configurator for the test setup. */
    protected ConfigBeanContext configBeanContext;

    /**
     * Creates a test with the specified name.
     *
     * @param name The name of the test case.
     */
    public ModelTestBase(String name)
    {
        super(name);
    }

    /**
     * Saves an entity, or sub-type of entity, with the specified external id to the specified table. The external id is
     * saved, then the entity, then the entity is loaded in a new transaction and compared with the original for
     * equality.
     *
     * @param  testEntity The entity to save and restore.
     * @param  testId     The external id of the entity, may be <tt>null</tt> if none is to be saved.
     * @param  tableName  The table to store to.
     *
     * @return The entity re-loaded from the table.
     */
    protected EntityInstanceBase checkSaveRestoreEntity(EntityInstanceBase testEntity, ExternalId testId,
        String tableName)
    {
        Session session = getSession();
        Transaction transaction = session.beginTransaction();

        try
        {
            // Mark the tables affected by the test as dirty so that they are cleaned up.
            dirtiedTables.add(tableName);
            //dirtiedTables.add("external_id");

            // Save the entities external id in the external id table.
            if (testId != null)
            {
                session.save(testId);
            }

            // Save the test dimension in the online database.
            session.save(tableName, testEntity);

            // Commit the changes.
            transaction.commit();
            session.close();

            // Re-load the dimension.
            session = getSession();
            transaction = session.beginTransaction();

            EntityInstanceBase loaded =
                (EntityInstanceBase) session.get(tableName, ((InternalIdImpl) testEntity.getOpaqueId()).getValue());

            // Compare the re-loaded dimension against the original.
            assertTrue("The test entity (or sub-type of entity) " + testEntity +
                " was not equal to its restored value " + loaded + ".", testEntity.equals(loaded));

            transaction.commit();

            return loaded;
        }
        finally
        {
            session.close();
        }
    }

    /**
     * Provides a hibernate session set up from the hibernate config bean.
     *
     * @return A hibernate session suitable to run tests in.
     */
    protected Session getSession()
    {
        return hibernateBean.getSession();
    }

    /** Takes a reference to the configured 'HibernateConfigBean' and 'IndexStoreConfigBean'. */
    protected void setUp() throws Exception
    {
        // Take a reference to the hibernate config bean so that the getSession convenience method can call it.
        hibernateBean =
            (HibernateConfigBean) configBeanContext.getConfiguredBean(
                "com.thesett.catalogue.config.HibernateConfigBean");
    }
}
