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

import java.util.Arrays;

import junit.framework.TestCase;

/**
 * @author Eric Herman <eric@mysql.com>
 * @version $Id: StrTest.java,v 1.1 2005/02/23 01:20:01 eherman Exp $
 */
public class StrTest extends TestCase {

    public void testContainsIgnoreCase() throws Exception {
        Str str = new Str();
        assertTrue(str.containsIgnoreCase("foobarbaz", "bar"));
        assertTrue(str.containsIgnoreCase("foobarbaz", "BAR"));
        assertTrue(str.containsIgnoreCase("fooBARbaz", "bar"));

        assertFalse(str.containsIgnoreCase("foobarbaz", "whiz"));
    }

    public void testToString() throws Exception {
        Str str = new Str();
        assertEquals("[foo]", str.toString(new Object[] { "foo" }));
        assertEquals("[foo][bar]", str.toString(new Object[] { "foo", "bar" }));
        Object[] objects = new Object[] { "foo", new Object[] { "bar", "baz" },
                "wiz" };
        assertEquals("[foo][[bar][baz]][wiz]", str.toString(objects));

        objects = new Object[] { "foo",
                Arrays.asList(new Object[] { "bar", "baz" }), "wiz" };
        assertEquals("[foo][[bar][baz]][wiz]", str.toString(objects));
    }
}