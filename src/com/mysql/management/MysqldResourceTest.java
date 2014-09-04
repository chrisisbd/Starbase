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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;

import com.mysql.management.util.ClassUtil;
import com.mysql.management.util.FileUtil;
import com.mysql.management.util.NullPrintStream;
import com.mysql.management.util.QuietTestCase;
import com.mysql.management.util.Shell;
import com.mysql.management.util.Str;
import com.mysql.management.util.Streams;
import com.mysql.management.util.TeeOutputStream;
import com.mysql.management.util.TestUtil;
import com.mysql.management.util.Threads;

/**
 * @author Eric Herman <eric@mysql.com>
 * @version $Id: MysqldResourceTest.java,v 1.32 2005/07/27 23:41:27 eherman Exp $
 */
public class MysqldResourceTest extends QuietTestCase {

    private MysqldResource mysqldResource;
    private TestFileUtil fileUtil;
    private Threads threads;
    private TestUtil testUtil;
    private Shell.Factory shellFactory;
    private Streams streams;
    private Str str;
    private ClassUtil classUtil;

    protected void setUp() {
        super.setUp();
        testUtil = new TestUtil();
        threads = new Threads();
        fileUtil = new TestFileUtil();
        shellFactory = new Shell.Factory();
        streams = new Streams();
        str = new Str();
        classUtil = new ClassUtil();

        mysqldResource = new MysqldResource(fileUtil.nullFile(), System.out,
                System.err, fileUtil, shellFactory, streams, threads, str,
                classUtil);
        mysqldResource.setKillDelay(10000);
        File baseDir = mysqldResource.getBaseDir();
        fileUtil.deleteTree(baseDir);
        if (baseDir.exists()) {
            warn("residual files");
        }
    }

    protected void tearDown() {
        threads.pause(50);
        File baseDir = mysqldResource.getBaseDir();
        try {
            if (mysqldResource.isRunning()) {
                mysqldResource.shutdown();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        fileUtil.deleteTree(baseDir);
        super.tearDown();
    }

    private void setSystemPropertiesToWinNT() {
        mysqldResource.setOsAndArch("Windows NT", "x86");
        assertTrue(mysqldResource.isWindows());
    }

    private void setSytemPropertiesToLinux() {
        mysqldResource.setOsAndArch("Linux", "i386");
        assertFalse(mysqldResource.isWindows());
    }

    public void testLaunch() throws Exception {
        Map args = new HashMap();
        String port = "" + testUtil.testPort();
        args.put(MysqldResourceI.PORT, port);
        String url = "jdbc:mysql://localhost:" + port + "/test";
        String threadName = "testLaunch";
        assertFalse("mysqld should not be running", mysqldResource.isRunning());

        mysqldResource.start(threadName, args);
        Shell s1 = mysqldResource.getShell();

        /* this asserts the thread starts */
        assertRunning();

        /* pause for mysqld to bind to port */
        for (int i = 0; i < 100 && !mysqldResource.isReadyForConnections(); i++) {
            threads.pause(25);
        }

        testUtil.assertConnectViaJDBC(url, true);
        mysqldResource.start(threadName, args);
        Shell s2 = mysqldResource.getShell();
        assertEquals(s1, s2);
        assertTrue(s1.isDaemon());
        assertRunningThenShutdown();
    }

    public void testUseDatabase() throws Exception {
        String url = "jdbc:mysql://localhost:" + testUtil.testPort() + "/test";
        String threadName = "testLaunch";
        assertFalse("mysqld should not be running", mysqldResource.isRunning());

        startMysql(threadName);
        Shell s1 = mysqldResource.getShell();

        /* this asserts the thread starts */
        assertRunning();

        /* pause for mysqld to bind to port */
        for (int i = 0; i < 100 && !mysqldResource.isReadyForConnections(); i++) {
            threads.pause(25);
        }

        testUtil.assertConnectViaJDBC(url);

        mysqldResource.start(threadName, new HashMap());
        Shell s2 = mysqldResource.getShell();
        assertEquals(s1, s2);
        assertTrue(s1.isDaemon());
        assertRunningThenShutdown();
    }

    private void startMysql(String threadName) {
        Map map = new HashMap();
        map.put(MysqldResourceI.PORT, "" + testUtil.testPort());
        mysqldResource.start(threadName, map);
    }

    public void testGetFileName() {
        File mysqld = mysqldResource.getMysqldFilePointer();
        assertNotNull(mysqld);
        String name = mysqld.getPath();
        assertTrue(name, name.indexOf("mysqld") > 0);
    }

    public void testWindowsFileName() {
        setSystemPropertiesToWinNT();
        String resourceName = mysqldResource.getResourceName();
        String fileName = mysqldResource.getMysqldFilePointer().getName();
        assertTrue(resourceName.indexOf("-nt.exe") > 0);
        assertTrue(fileName.indexOf("-nt.exe") > 0);
    }

    private void checkMysqldFile() {
        File mysqld = mysqldResource.makeMysqld();
        assertTrue(mysqld.exists());
        assertTrue(mysqld.length() > 100);
    }

    public void testGetMysqldNative() {
        checkMysqldFile();
    }

    public void testGetMysqldWinNT() {
        setSystemPropertiesToWinNT();
        checkMysqldFile();
    }

    public void testGetMysqldLinux() {
        setSytemPropertiesToLinux();
        checkMysqldFile();
        assertTrue(fileUtil.madeExecutable(mysqldResource
                .getMysqldFilePointer()));
    }

    public void testUnknownOs() {
        mysqldResource.setOsAndArch("bogus", "x86");
        Exception expected = null;
        try {
            mysqldResource.makeMysqld();
        } catch (MissingResourceException e) {
            expected = e;
        }
        assertNotNull("" + mysqldResource.getMysqldFilePointer(), expected);
        assertTrue(expected.getMessage().indexOf("bogus") > 0);
    }

    public void testCreateDbFiles() {
        File dataDir = new File(mysqldResource.getBaseDir(), "test-data");
        File dbDataDir = new File(dataDir, "mysql");
        File host_frm = new File(dbDataDir, "host.frm");
        assertEquals(false, host_frm.exists());
        assertEquals(false, dataDir.exists());

        Map args = new HashMap();
        args.put("datadir", dataDir.getPath());

        File data = mysqldResource.dataDir(args);
        mysqldResource.ensureEssentialFilesExist(data);
        assertEquals(dataDir, data);

        assertTrue(host_frm.exists());
    }

    private void assertRunningThenShutdown() {
        assertRunning();
        mysqldResource.shutdown();
        assertNotRunning();
    }

    private void assertNotRunning() {
        for (int i = 0; i < 500; i++) {
            if (!mysqldResource.isRunning())
                break;
            threads.pause(25);
        }
        assertFalse("mysqld should not be running", mysqldResource.isRunning());
    }

    private void assertRunning() {
        for (int i = 0; i < 500; i++) {
            if (mysqldResource.isRunning())
                break;
            threads.pause(25);
        }
        assertTrue("mysqld should be running", mysqldResource.isRunning());
    }

    public void testServerOptions() {
        Map optionsMap = mysqldResource.getServerOptions();
        String expectedBaseDir = mysqldResource.getBaseDir().getPath();
        assertEquals(expectedBaseDir, optionsMap.get(MysqldResourceI.BASEDIR));
    }

    public void testTestReporting() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream captured = new PrintStream(baos);
        mysqldResource = new MysqldResource(fileUtil.nullFile(), captured,
                captured, fileUtil, shellFactory, streams, threads, str,
                classUtil);

        mysqldResource.reportIfNoPidfile(true);
        captured.flush();
        assertEquals("", new String(baos.toByteArray()));

        mysqldResource.reportIfNoPidfile(false);
        captured.flush();
        String output = new String(baos.toByteArray());
        testUtil.assertContainsIgnoreCase(output, "pid-file not found");
        testUtil.assertContainsIgnoreCase(output, fileUtil.tmp().toString());
    }

    public void testForceKill() {
        startMysql("killMe");
        assertTrue(mysqldResource.isRunning());
        mysqldResource.issueForceKill();
        assertFalse(mysqldResource.isRunning());
    }

    public void testDestroyShell() {
        /** TODO: improve this test */
        startMysql("DestroyMe");
        assertTrue(mysqldResource.isRunning());
        mysqldResource.destroyShell();
        if (mysqldResource.isRunning()) {
            new MysqldResource(fileUtil.nullFile()).shutdown();
        }
    }

    public void testVersion() {
        assertEquals(MysqldResourceI.DEFAULT_VERSION, mysqldResource
                .getVersion());
        mysqldResource.setVersion(5, 11, 42);
        assertEquals("5.11.42", mysqldResource.getVersion());
    }

    public void testNoPidFile() {
        assertEquals(mysqldResource.pid(), "No PID");
        startMysql("pid file");
        assertTrue(Integer.parseInt(mysqldResource.pid()) > 0);
    }

    public void testTestFinalize() throws Throwable {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream captured = new PrintStream(baos);
        mysqldResource = new MysqldResource(fileUtil.nullFile(), captured,
                captured, fileUtil, shellFactory, streams, threads, str,
                classUtil);
        mysqldResource.finalize();
        captured.flush();
        String output = new String(baos.toByteArray());
        assertEquals("", output);

        PrintStream devNull = new NullPrintStream();
        String[] none = new String[0];
        mysqldResource.setShell(new Shell.Default(none, "bogus", devNull,
                devNull));

        mysqldResource.finalize();
        captured.flush();
        output = new String(baos.toByteArray());
        testUtil.assertContainsIgnoreCase(output, "<init>");
    }

    public void testUsage() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream captured = new PrintStream(baos);
        TeeOutputStream newOut = new TeeOutputStream(System.out, captured);
        PrintStream stdout = System.out;
        System.setOut(new PrintStream(newOut));
        try {
            MysqldResource.main(new String[0]);
        } finally {
            System.setOut(stdout);
        }
        assertTrue(baos.toString().indexOf("Usage") >= 0);
    }

    public void testJarName() {
        setSystemPropertiesToWinNT();
        assertEquals("win_share_dir.jar", mysqldResource.shareJar());
        setSytemPropertiesToLinux();
        assertEquals("share_dir.jar", mysqldResource.shareJar());
    }

    // -------------------
    private static class TestFileUtil extends FileUtil {
        private List execFiles = new ArrayList();

        public void addExecutableRights(File mysqld, PrintStream out,
                PrintStream err) {
            execFiles.add(mysqld);
            super.addExecutableRights(mysqld, out, err);
        }

        public boolean madeExecutable(File file) {
            return execFiles.contains(file);
        }
    }
}