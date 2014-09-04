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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;

import junit.framework.TestCase;

/**
 * @author Eric Herman <eric@mysql.com>
 * @version $Id: FileUtilTest.java,v 1.5 2005/07/07 21:35:01 eherman Exp $
 */
public class FileUtilTest extends TestCase {
    FileUtil fileUtil;

    protected void setUp() {
        fileUtil = new FileUtil();
    }

    public void testTempDir() {
        String property = System.getProperty("java.io.tmpdir");
        assertNotNull(property);
        File javaTmp = new File(property);
        assertEquals(javaTmp.getPath(), true, javaTmp.exists());
        assertEquals(javaTmp, fileUtil.tmp());
    }

    public void testDeleteTree() throws FileNotFoundException {
        File foo = new File(fileUtil.tmp(), "foo");
        File bar = new File(foo, "bar");
        bar.mkdirs();
        assertEquals(true, bar.exists());

        File baz = new File(foo, "baz");
        FileOutputStream fos = new FileOutputStream(baz);
        PrintWriter out = new PrintWriter(fos);
        out.print("baz");
        out.close();

        assertEquals(true, baz.exists());
        assertEquals(3, baz.length());

        assertEquals(true, fileUtil.deleteTree(foo));
        assertEquals(false, foo.exists());
    }

    public void testMakeExecutable() {
        class FakeShellStub extends Shell.Stub {
            String[] args;

            int runCalled = 0;

            public void run() {
                runCalled++;
            }
        }
        final FakeShellStub shell = new FakeShellStub();
        class FakeShellFactory extends Shell.Factory {
            public Shell newShell(String[] args, String name, PrintStream out,
                    PrintStream err) {
                assertNotNull(name);
                assertNotNull(out);
                assertNotNull(err);
                shell.args = args;
                return shell;
            }
        }

        fileUtil = new FileUtil(new FakeShellFactory(), '\\', new Streams());
        fileUtil.addExecutableRights(new File("bogus"), System.out, System.err);
        assertNull(shell.args);
        assertEquals(0, shell.runCalled);

        fileUtil = new FileUtil(new FakeShellFactory(), '/', new Streams());
        fileUtil.addExecutableRights(new File("bogus"), System.out, System.err);
        assertEquals(1, shell.runCalled);
        assertEquals(3, shell.args.length);
        assertEquals("chmod", shell.args[0]);
        assertEquals("+x", shell.args[1]);
        assertTrue(shell.args[2].indexOf("bogus") >= 0);
    }
}
