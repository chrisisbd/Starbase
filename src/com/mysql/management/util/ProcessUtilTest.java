package com.mysql.management.util;

import java.io.File;

import junit.framework.TestCase;

public class ProcessUtilTest extends TestCase {
    public void testNullPid() {
        ProcessUtil kp = new ProcessUtil(null, null, null);
        assertEquals("-1", kp.pid());
    }

    public void testPidWithEOL() {
        String pid = " 3343\n";
        ProcessUtil kp = new ProcessUtil(pid, null, null);
        assertEquals("3343", kp.pid());
    }

    public void testKillCommandLineUnix() {
        FileUtil fu = new FileUtil() {
            public boolean isWindows() {
                return false;
            }
        };
        String pid = "2342";
        ProcessUtil kp = new ProcessUtil(fu, pid, null, null, new Str(), null,
                null, null);
        String[] args = kp.killArgs(false);
        assertEquals(args[0], "kill");
        assertEquals(pid, args[args.length - 1]);
    }

    public void testKillCommandLineWindows() {
        FileUtil fu = new FileUtil() {
            public boolean isWindows() {
                return true;
            }
        };
        String pid = "2342";
        ProcessUtil kp = new ProcessUtil(fu, pid, null, null, new Str(), null,
                new Streams(), null);
        String[] args = kp.killArgs(false);
        assertTrue(args[0], args[0].endsWith("kill.exe"));
        assertEquals(pid, args[args.length - 1]);
    }

    public void testForce() {
        ProcessUtil kp = new ProcessUtil("4321", null, null);
        String[] args = kp.killArgs(true);
        assertEquals("-9", args[1]);
    }

    public void testIsRunning() {
        String pid = "5234";
        ProcessUtil kp = new ProcessUtil(pid, null, null);
        String[] args = kp.isRunningArgs();
        new TestUtil().assertContainsIgnoreCase(args[0], "kill");
        assertEquals("-0", args[1]);
        assertEquals(pid, args[2]);
    }

    public void testFileCreation() {
        ProcessUtil pu = new ProcessUtil("1234", null, null);
        File winKill;
        winKill = pu.getWindowsKillFile();
        assertTrue(winKill.exists());
        winKill.delete();
        assertFalse(winKill.exists());
        winKill = pu.getWindowsKillFile();
        assertTrue(winKill.exists());
    }
}