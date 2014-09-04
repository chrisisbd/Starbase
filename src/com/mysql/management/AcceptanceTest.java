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
package com.mysql.management;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.mysql.management.util.FileUtil;
import com.mysql.management.util.QuietTestCase;
import com.mysql.management.util.Shell;
import com.mysql.management.util.Str;
import com.mysql.management.util.TestUtil;

/**
 * @author Eric Herman <eric@mysql.com>
 * @version $Id: AcceptanceTest.java,v 1.29 2005/07/06 17:12:23 eherman Exp $
 */
public class AcceptanceTest extends QuietTestCase {

    private Connection conn = null;

    private Statement stmt = null;

    private ResultSet rs = null;

    private MysqldResourceI mysqld;

    private File baseDir;

    FileUtil fileUtil;

    Shell.Factory shellFactory;

    protected void setUp() {
        super.setUp();
        fileUtil = new FileUtil();
        shellFactory = new Shell.Factory();
    }

    protected void tearDown() {
        super.tearDown();
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        if (conn != null) {
            try {
                conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            conn = null;
        }

        if (mysqld != null) {
            try {
                mysqld.shutdown();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (baseDir != null) {
            new FileUtil().deleteTree(baseDir);
        }
    }

    public void testMain() throws Exception {
        TestUtil testUtil = new TestUtil();
        int port1 = testUtil.testPort();
        String url1 = "jdbc:mysql://127.0.0.1:" + port1 + "/test";
        String[] startArgs1 = new String[] { "--port=" + port1 };
        String[] stopArgs1 = new String[] { "--shutdown" };

        int port2 = port1 + 1000;
        String url2 = "jdbc:mysql://127.0.0.1:" + port2 + "/test";
        File baseDir2 = new File(new FileUtil().tmp(), "cmxj-dir2");
        String[] startArgs2 = new String[] {
                "--" + MysqldResourceI.PORT + "=" + port2,
                "--" + MysqldResourceI.BASEDIR + "=" + baseDir2 };
        String[] stopArgs2 = new String[] {
                "--" + MysqldResourceI.BASEDIR + "=" + baseDir2, "--shutdown" };

        MysqldResource.main(startArgs1);
        MysqldResource.main(startArgs2);
        testUtil.assertConnectViaJDBC(url1);
        testUtil.assertConnectViaJDBC(url2);

        MysqldResource.main(stopArgs1);

        testUtil.assertConnectViaJDBC(url2);

        SQLException expected = null;
        try {
            testUtil.assertConnectViaJDBC(url1);
        } catch (SQLException e) {
            expected = e;
        }
        assertNotNull(expected);

        MysqldResource.main(stopArgs2);
        expected = null;
        try {
            testUtil.assertConnectViaJDBC(url2);
        } catch (SQLException e) {
            expected = e;
        }
        assertNotNull(expected);
    }

    public void testCreateUser() throws Exception {
        // resetOutAndErr();
        baseDir = new File(new FileUtil().tmp(), "mxj-user-test");
        fileUtil.deleteTree(baseDir);
        mysqld = new MysqldResource(baseDir);
        baseDir.mkdirs();

        Map params = new HashMap();
        int port = new TestUtil().testPort();
        params.put(MysqldResourceI.PORT, Integer.toString(port));

        mysqld.start("mxj-user-test", params);

        // String url = "jdbc:mysql://127.0.0.1:" + port + "/";
        String url = "jdbc:mysql://localhost:" + port + "/";
        String rootUser = "root";
        String rootPassword = "";
        makeDb(url + "test", rootUser, rootPassword);
        checkVersion(mysqld.getVersion());

        conn.close();
        conn = null;

        com.mysql.jdbc.Driver driver = new com.mysql.jdbc.Driver();
        Properties props = new Properties();
        props.setProperty("user", "JAVA");
        props.setProperty("password", "SAPR3");

        Exception exception = null;
        try {
            conn = driver.connect(url + "MY1", props);
        } catch (Exception e) {
            exception = e;
        }

        assertNull("" + exception, exception);

        checkVersion(mysqld.getVersion());
    }

    private void checkVersion(String version) throws SQLException {
        stmt = conn.createStatement();
        rs = stmt.executeQuery("SELECT VERSION()");
        int cols = rs.getMetaData().getColumnCount();
        assertTrue(rs.next());
        assertTrue(cols >= 1);
        String searchIn = rs.getString(1);
        assertTrue("<" + version + "> not found in <" + searchIn + ">",
                new Str().containsIgnoreCase(searchIn, version));
        assertEquals(cols, 1);
        assertFalse(rs.next());
        rs.close();
        stmt.close();
    }

    private void makeDb(String url, String userName, String password)
            throws Exception {
        new TestUtil().assertConnectViaJDBC(url, userName, password, true);
        Class.forName(com.mysql.jdbc.Driver.class.getName());
        conn = DriverManager.getConnection(url, userName, password);
        stmt = conn.createStatement();
        stmt.execute("CREATE DATABASE MY1");
        stmt.execute("USE MY1");

        String sql = "GRANT ALL PRIVILEGES ON MY1.*"
                + " TO 'JAVA'@'%' IDENTIFIED BY 'SAPR3'" + " WITH GRANT OPTION";
        stmt.execute(sql);

        sql = "GRANT ALL PRIVILEGES ON MY1.*"
                + " TO 'JAVA'@'localhost' IDENTIFIED BY 'SAPR3'"
                + " WITH GRANT OPTION";
        stmt.execute(sql);

        stmt.execute("commit");
        stmt.close();
    }
}
