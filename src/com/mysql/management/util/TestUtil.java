/*
 Copyright (C) 2004 MySQL AB

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License version 2 as 
 published by the Free Software Foundation.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 */
package com.mysql.management.util;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;

public class TestUtil {
    private int port;

    public TestUtil() {
        this(Integer.parseInt(System.getProperty("c-mxj_test_port", "3336")));
    }

    public TestUtil(int port) {
        this.port = port;
    }

    public int testPort() {
        return port;
    }

    public void assertContainsIgnoreCase(String searchIn, String searchFor) {
        if (new Str().containsIgnoreCase(searchIn, searchFor)) {
            return;
        }
        String msg = "<" + searchFor + "> not found in <" + searchIn + ">";
        throw new AssertionFailedError(msg);
    }

    public void assertConnectViaJDBC(String url, boolean dbInUrl)
            throws ClassNotFoundException, InstantiationException,
            IllegalAccessException, SQLException {

        assertConnectViaJDBC(url, "root", "", dbInUrl);
    }

    public void assertConnectViaJDBC(String url) throws ClassNotFoundException,
            InstantiationException, IllegalAccessException, SQLException {

        assertConnectViaJDBC(url, false);
    }

    public void assertConnectViaJDBC(String url, String user, String password)
            throws ClassNotFoundException, InstantiationException,
            IllegalAccessException, SQLException {

        assertConnectViaJDBC(url, user, password, false);
    }

    public void assertConnectViaJDBC(String url, String user, String password,
            boolean dbInUrl) throws ClassNotFoundException,
            InstantiationException, IllegalAccessException, SQLException {

        String name = com.mysql.jdbc.Driver.class.getName();
        Class c = Class.forName(name);
        c.newInstance();

        Connection conn = DriverManager.getConnection(url, user, password);
        try {
            if (!dbInUrl) {
                useDbTest(conn);
            }
            checkVersion(conn);
            checkBigInt(conn);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    /** basic check to see if the database is there, selects the version */
    private void checkVersion(Connection conn) throws SQLException {
        ResultSet rs = null;
        try {
            Statement stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT VERSION()");
            Assert.assertTrue(rs.next());
            String version = rs.getString(1);
            Assert.assertTrue(version, version.startsWith("4."));
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void useDbTest(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        try {
            stmt.executeUpdate("use test");
        } finally {
            stmt.close();
        }
    }

    /** creates table, inserts, selects, drops table */
    private void checkBigInt(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rs = null;
        try {
            stmt.executeUpdate("DROP TABLE IF EXISTS bigIntRegression");
            stmt.executeUpdate("CREATE TABLE bigIntRegression "
                    + "( val BIGINT NOT NULL)");
            stmt.executeUpdate("INSERT INTO bigIntRegression "
                    + "VALUES (6692730313872877584)");
            rs = stmt.executeQuery("SELECT val FROM bigIntRegression");

            while (rs.next()) {
                // check retrieval
                long retrieveAsLong = rs.getLong(1);
                Assert.assertTrue(retrieveAsLong == 6692730313872877584L);
            }

            rs.close();
            stmt.executeUpdate("DROP TABLE IF EXISTS bigIntRegression");

            String bigIntAsString = "6692730313872877584";

            long parsedBigIntAsLong = Long.parseLong(bigIntAsString);

            // check JDK parsing
            Assert.assertTrue(bigIntAsString.equals(String
                    .valueOf(parsedBigIntAsLong)));
        } finally {
            stmt.executeUpdate("DROP TABLE IF EXISTS bigIntRegression");
        }
    }

    public void assertNotImplemented(Block block) throws Exception {
        try {
            block.exec();
        } catch (NotImplementedException e) {
            return;
        } catch (Exception e) {
            String msg = e.getMessage();
            if (msg != null && msg.indexOf("Not implemented") >= 0) {
                return;
            }
            Throwable cause = e.getCause();
            if (cause instanceof NotImplementedException) {
                return;
            }
            if (cause != null) {
                msg = cause.getMessage();
            }
            if (msg != null && msg.indexOf("Not implemented") >= 0) {
                return;
            }
            throw e;
        }
        throw new RuntimeException("This is now implemented.");
    }

    public void invoke(final Object target, final Method method)
            throws Exception {
        Class[] paramTypes = method.getParameterTypes();
        Object[] params = new Object[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            Object obj = null;
            if (paramTypes[i].equals(int.class)
                    || paramTypes[i].equals(Integer.class)) {
                obj = new Integer(0);
            } else if (paramTypes[i].equals(boolean.class)
                    || paramTypes[i].equals(Boolean.class)) {
                obj = Boolean.FALSE;
            } else if (paramTypes[i].equals(Object[].class)
                    || paramTypes[i].equals(String[].class)) {
                obj = new String[0];
            } else if (paramTypes[i].equals(Thread.class)
                    || paramTypes[i].equals(Runnable.class)) {
                obj = new Thread();
            }
            params[i] = obj;
        }
        method.invoke(target, params);
    }

    public static interface Block {
        void exec() throws Exception;
    }
}