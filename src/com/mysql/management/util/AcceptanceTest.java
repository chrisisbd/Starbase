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
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Eric Herman <eric@mysql.com>
 * @version $Id: AcceptanceTest.java,v 1.3 2005/07/05 21:19:40 eherman Exp $
 */
public class AcceptanceTest extends QuietTestCase {

    public void testPlatformMain() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));

        List propKeys = new ArrayList();
        propKeys.add("java.vm.vendor");
        propKeys.add("java.vm.version");
        propKeys.add("os.name");
        propKeys.add("os.arch");
        propKeys.add("os.version");

        Map pairs = new LinkedHashMap();
        for (int i = 0; i < propKeys.size(); i++) {
            String propertyKey = (String) propKeys.get(i);
            pairs.put(propertyKey, System.getProperty(propertyKey));
        }

        Platform.main(new String[0]);

        String output = new String(baos.toByteArray());

        for (Iterator it = pairs.entrySet().iterator(); it.hasNext();) {
            String propEqVal = it.next().toString();
            assertTrue(output.indexOf(propEqVal) >= 0);
        }
    }
}