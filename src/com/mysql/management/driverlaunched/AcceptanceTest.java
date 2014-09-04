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
package com.mysql.management.driverlaunched;

import com.mysql.management.util.NullPrintStream;
import com.mysql.management.util.QuietTestCase;
import com.mysql.management.util.TestUtil;

/**
 * @author Eric Herman <eric@mysql.com>
 * @version $Id: AcceptanceTest.java,v 1.11 2005/07/06 15:58:34 eherman Exp $
 */
public class AcceptanceTest extends QuietTestCase {

    private String hostAndPort;

    protected void tearDown() {
        try {
            ServerLauncherSocketFactory.shutdown(hostAndPort,
                    new NullPrintStream());
        } finally {
            super.tearDown();
        }
    }

    public void testServerDriverLauncherFactory() throws Exception {
        int port = new TestUtil().testPort();
        hostAndPort = "localhost:" + port;
        Class sf = com.mysql.management.driverlaunched.ServerLauncherSocketFactory.class;
        String url = "jdbc:mysql://" + hostAndPort + "/test?"
                + "socketFactory=" + sf.getName();

        new TestUtil().assertConnectViaJDBC(url);

        ServerLauncherSocketFactory.shutdown(hostAndPort);
    }
}