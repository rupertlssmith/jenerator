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
package com.thesett.catalogue.impl.standalone;

import org.hibernate.Session;

import com.thesett.catalogue.model.CatalogueManagerService;

/**
 * CatalogueManagerServiceTestsUtils provides a set of callback methods for {@link CatalogueManagerServiceTests}. This
 * allows the tests to be written in a deployment neutral way, for example using explicit or container managed
 * transactions.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities
 * <tr><td> Begin a transaction on test data.
 * <tr><td> Commit changes to test data.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public interface CatalogueManagerServiceTestsUtils
{
    /**
     * Obtains a reference to the catalogue manager service interface.
     *
     * @return A reference to the catalogue manager service interface.
     */
    public CatalogueManagerService getCatalogueManager();

    /**
     * Obtains a direct hibernate session onto the test data, bypassing any container management.
     *
     * @return A hibernate session onto the test data.
     */
    public Session getDirectHibernateSession();

    /** Closes the direct hibernate session onto the test data. */
    public void closeDirectHibernateSession();

    /** Begins a transaction on test data. */
    public void beginTransaction();

    /** Commits a transaction on test data. */
    public void commitTransaction();
}
