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

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

/**
 * @author Eric Herman <eric@mysql.com>
 * @version $Id: PlatformTest.java,v 1.1 2005/02/23 01:20:01 eherman Exp $
 */
public class PlatformTest extends TestCase {

    private static final String EOL = System.getProperty("line.separator");

    public void testGetKeys() {
        Platform platform = new Platform(new NullPrintWriter());
        List keys = platform.platformProps();
        assertTrue(keys.contains("os.name"));
        assertTrue(keys.contains("os.arch"));
    }

    public void testReport() {
        System.setProperty("foo", "bar");
        System.setProperty("baz", "wiz");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Platform platform = new Platform(new PrintWriter(baos));

        List list = new ArrayList();
        list.add("foo");
        list.add("baz");

        platform.report(list);

        String expected = "foo=bar" + EOL + "baz=wiz" + EOL;
        assertEquals(expected, new String(baos.toByteArray()));
    }

    public void testPrintAllProperties() {
        StringWriter out = new StringWriter();
        Platform platform = new Platform(new PrintWriter(out));
        Set keys = System.getProperties().keySet();
        platform.report(keys);
        // System.out.println(out.toString());
    }

}