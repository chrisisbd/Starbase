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
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Map;

import junit.framework.TestCase;

import com.mysql.management.util.ClassUtil;
import com.mysql.management.util.Str;
import com.mysql.management.util.Streams;
import com.mysql.management.util.Threads;

/**
 * @author Eric Herman <eric@mysql.com>
 * @version $Id: HelpOptionsParserTest.java,v 1.1 2005/02/16 21:46:11 eherman
 *          Exp $
 */
public class HelpOptionsParserTest extends TestCase {

    public void testOptionParser() throws Exception {

        String resourceVersionDir = MysqldResourceI.DEFAULT_VERSION.replaceAll(
                "\\.", "-");
        assertEquals("4-1-13", resourceVersionDir);
        String sampleHelp = resourceVersionDir
                + "/com/mysql/management/MySQL_Help.txt";

        ClassLoader cl = getClass().getClassLoader();
        InputStream is = cl.getResourceAsStream(sampleHelp);
        String help = new Streams().readString(is);
        is.close();

        HelpOptionsParser parser = new HelpOptionsParser(System.err,
                new Threads(), new ClassUtil(), new Str());
        Map parsed = parser.parseHelp(help);
        assertEquals("/usr/local/mysql/", parsed.get(MysqldResourceI.BASEDIR));
        assertEquals("TRUE", parsed.get("bdb"));
        // assertEquals("(No default value)", parsed.get("time-format"));
        assertEquals("", parsed.get("time-format"));
    }

    public void testTrimOptionsErrorMsg() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        HelpOptionsParser parser = new HelpOptionsParser(new PrintStream(out),
                new Threads(), new ClassUtil(), new Str());
        Exception expected = null;
        try {
            parser.trimToOptions("bogus");
        } catch (Exception e) {
            expected = e;
        }
        assertNotNull(expected);
        String errMsg = new String(out.toByteArray());
        assertTrue(errMsg.indexOf("bogus") >= 0);
    }
}