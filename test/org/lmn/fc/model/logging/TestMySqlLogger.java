// Copyright 2000, 2001, 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012
// Laurence Newell
// starbase@ukraa.com
// radio.telescope@btinternet.com
//
// This file is part of Starbase.
//
// Starbase is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// Starbase is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with Starbase.  If not, see http://www.gnu.org/licenses.

package org.lmn.fc.model.logging;

import junit.framework.TestCase;
import junit.textui.TestRunner;
import org.lmn.fc.common.constants.*;
import org.lmn.fc.common.exceptions.FrameworkException;
import org.lmn.fc.database.DatabaseOptions;
import org.lmn.fc.database.DatabaseProperties;
import org.lmn.fc.database.DatabaseType;
import org.lmn.fc.model.dao.DataStore;
import org.lmn.fc.model.logging.impl.EventLogMySqlDAO;

import java.sql.SQLException;

/***************************************************************************************************
 * TestMySqlLogger.
 */

public final class TestMySqlLogger extends TestCase
                                   implements FrameworkConstants,
                                              FrameworkStrings,
                                              FrameworkMetadata,
                                              FrameworkSingletons,
                                              ResourceKeys
    {
    /***********************************************************************************************
     * Run the tests!
     *
     * @param args
     */

    public static void main(final String[] args)
        {
        TestRunner.run(TestMySqlLogger.class);
        }


    /***********************************************************************************************
     * setUp().
     *
     * @throws Exception -
     */

    public void setUp() throws Exception
        {
        final DatabaseOptions options;
        final DatabaseProperties propertiesEmbedded;

        super.setUp();

        options = new DatabaseOptions();
        options.setDataStore(DataStore.MYSQL);
        options.setDatabaseType(DatabaseType.EMBEDDED);

        propertiesEmbedded = new DatabaseProperties();
        propertiesEmbedded.setDriver("");
        propertiesEmbedded.setDataSource("jdbc:mysql://127.0.0.1");
        propertiesEmbedded.setPort("jdbc:mysql://127.0.0.1");
        propertiesEmbedded.setDatabase("framework");
        propertiesEmbedded.setCredentialsInline(false);
        propertiesEmbedded.setUsername("framework");
        propertiesEmbedded.setPassword("password");
        options.setDatabaseProperties(DatabaseType.EMBEDDED, propertiesEmbedded);

        // Set up the FrameworkDatabase using the requested database and credentials
        DATABASE.setDatabaseOptions(options);

        // Attempt to connect to the requested database
        // or drop back to the default
        LOGGER.console("Attempt to deploy database");
        DATABASE.openConnection();
        }


    /***********************************************************************************************
     * testMySqlLogger().
     */

    public void testMySqlLogger()
        {
        final EventLogDAOInterface dao;

        LOGGER.console("Starting testMySqlLogger()");

        assertNotNull("Connection is NULL", DATABASE.getConnection());

        try
            {
            assertTrue("Connection is CLOSED", !DATABASE.getConnection().isClosed());
            }

        catch (SQLException e)
            {
            fail("Caught unexpected SQLException");
            }

        dao = new EventLogMySqlDAO();
        assertNotNull("EventLogMySqlDAO is NULL", dao);

        try
            {
            dao.getAllEventsReport();
            System.out.println("get all");
            }

        catch (FrameworkException e)
            {
            fail("Caught unexpected FrameworkException");
            }

        System.out.println("get atom");
        dao.getAtomEventsReport(REGISTRY.getFramework());

        for (int i = 0; i < 10; i++)
            {
            dao.logEvent(1,
                         1,
                         1,
                         "first",
                         "yyyy" + i,
                         EventStatus.INFO);

            }
        System.out.println("get all events");
        dao.getAllEventsReport();

        System.out.println("get atom events");
        dao.getAtomEventsReport(REGISTRY.getFramework());

        System.out.println("delete all events");
        dao.deleteAllEvents();

        System.out.println("insert f/w events");
        for (int i = 0; i < 10; i++)
            {
            dao.logEvent(REGISTRY.getFramework().getID(),
                         1,
                         1,
                         "f/w events",
                         "dddd" + (i+ 10),
                         EventStatus.WARNING);

            }

        System.out.println("delete f/w events");
        dao.deleteAtomEvents(REGISTRY.getFramework());

        System.out.println("add events");
        for (int i = 0; i < 10; i++)
            {
            dao.logEvent(99,
                         1,
                         1,
                         "third",
                         "xxxx" + (i+20),
                         EventStatus.QUESTION);
            }
        }


    /***********************************************************************************************
     * tearDown().
     *
     * @throws Exception -
     */

    public void tearDown() throws Exception
        {
        super.tearDown();

        DATABASE.closeConnection();
        }
    }
