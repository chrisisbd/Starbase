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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;

/**
 * @author Eric Herman <eric@mysql.com>
 * @version $Id: ShellTest.java,v 1.10 2005/07/06 17:14:12 eherman Exp $
 */
public class ShellTest extends QuietTestCase {

    private Shell.Default shell;

    private int shellName = 0;

    private TestProcess testProcess;

    private int processSleep = 0;

    protected void setUp() {
        super.setUp();
        processSleep = 0;
        String[] commandLineArgs = new String[] { "java" };
        String threadName = "ShellTest" + shellName++;

        shell = new Shell.Default(commandLineArgs, threadName, System.out,
                System.err);
        setRuntime(0);
    }

    private void setRuntime(final int returnCode) {
        RuntimeI fakeRuntime = new RuntimeI.Stub() {
            public Process exec(String[] cmdarray, String[] envp, File dir) {
                assertNotNull(cmdarray);
                assertNull(envp);
                assertNull(dir);
                testProcess = new TestProcess(returnCode, processSleep);
                return testProcess;
            }
        };
        shell.setRuntime(fakeRuntime);
    }

    public void testCompletionListener() {
        Exception expected = null;
        try {
            shell.addCompletionListener(null);
        } catch (IllegalArgumentException e) {
            expected = e;
        }
        assertNotNull(expected);
        class TestListener implements Runnable {
            int timesRun = 0;

            public void run() {
                timesRun++;
            }
        }
        TestListener listener = new TestListener();
        shell.addCompletionListener(listener);
        shell.run();
        for (int c = 0; c < 5 && (listener.timesRun == 0); c++) {
            new Threads().pause(10);
        }
        assertEquals(1, listener.timesRun);
    }

    public void testShellReturnsWithReturnCode() {
        int returnCode = 7;
        setRuntime(returnCode);
        shell.run();
        assertTrue(shell.hasReturned());
        assertEquals(returnCode, shell.returnCode());
    }

    public void testSetters() {
        shell.setRuntime(new RuntimeI.Default());
        shell.setEnvironment(new String[0]);
        shell.setWorkingDir(new FileUtil().tmp());
        shell.run();
        assertTrue(shell.hasReturned());
        assertEquals(1, shell.returnCode());
    }

    public void testDoubleRun() {
        processSleep = 2000;
        shell.start();
        new Threads().pause(20);
        Exception expected = null;
        try {
            shell.run();
        } catch (IllegalStateException e) {
            expected = e;
        }
        assertNotNull(expected);
    }

    public void testShellThrowsIfNotYetReturned() {
        assertFalse(shell.hasReturned());
        Exception expected = null;
        try {
            shell.returnCode();
        } catch (Exception e) {
            expected = e;
        }
        assertNotNull("Should have throws", expected);
    }

    public void testDestroy() throws Exception {
        processSleep = 2000;
        assertNull(testProcess);
        shell.start();
        new Threads().pause(20);
        shell.destroyProcess();
        assertEquals(1, testProcess.destroyCalled);
    }

    public void testForThrownExceptions() throws Exception {
        shell = new Shell.Default(null, "foo", null, null);
        Exception expected = null;
        try {
            shell.run();
        } catch (Exception e) {
            expected = e;
        }

        assertNotNull(expected);
    }

    public void testStub() throws Exception {
        final Method[] methods = Shell.class.getDeclaredMethods();
        final Shell stub = new Shell.Stub();
        final TestUtil testUtil = new TestUtil();
        for (int i = 0; i < methods.length; i++) {
            final int x = i;
            testUtil.assertNotImplemented(new TestUtil.Block() {
                public void exec() throws Exception {
                    testUtil.invoke(stub, methods[x]);
                }
            });
        }
    }

    // ----------------
    public static class TestProcess extends TestStubProcess {
        private int returnCode;

        int destroyCalled;

        int sleep;

        public TestProcess(int returnCode, int sleep) {
            this.returnCode = returnCode;
            this.destroyCalled = 0;
            this.sleep = sleep;
        }

        public InputStream getInputStream() {
            return new ByteArrayInputStream(new byte[0]);
        }

        public InputStream getErrorStream() {
            return new ByteArrayInputStream(new byte[0]);
        }

        public int waitFor() {
            new Threads().pause(sleep);
            return returnCode;
        }

        public void destroy() {
            destroyCalled++;
        }
    }
}
