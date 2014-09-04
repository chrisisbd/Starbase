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

import java.io.File;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import junit.framework.TestCase;

import com.mysql.jdbc.SocketFactory;
import com.mysql.jdbc.StandardSocketFactory;
import com.mysql.management.MysqldResourceI;
import com.mysql.management.MysqldResourceTestImpl;

public class ServerLauncherSocketFactoryTest extends TestCase {
    static class FakeMysqldFactory implements MysqldFactory {
        List resources = new ArrayList();

        File baseDir;

        public MysqldResourceI newMysqldResource(File base) {
            this.baseDir = base;
            MysqldResourceI newMysqldResourceI = new MysqldResourceTestImpl();
            resources.add(newMysqldResourceI);
            return newMysqldResourceI;
        }
    }

    static class FakeSocketFactory implements SocketFactory {
        int afterHandshakeCalled = 0;

        int beforeHandshakeCalled = 0;

        int connectCalled = 0;

        Object[] connectInfo;

        public Socket afterHandshake() {
            afterHandshakeCalled++;
            return null;
        }

        public Socket beforeHandshake() {
            beforeHandshakeCalled++;
            return null;
        }

        public Socket connect(String host, int portNumber, Properties props) {
            connectInfo = new Object[] { host, new Integer(portNumber), props };
            connectCalled++;
            return null;
        }
    }

    public void testDefaultConstruction() {
        ServerLauncherSocketFactory sf = new ServerLauncherSocketFactory();
        assertEquals(StandardSocketFactory.class, sf.getSocketFactory()
                .getClass());
        assertEquals(MysqldFactory.Default.class, sf.getResourceFactory()
                .getClass());
    }

    public void testComposition() throws Exception, Exception {
        ServerLauncherSocketFactory sf = new ServerLauncherSocketFactory();
        sf.setMyMysqlds(new HashMap());
        FakeSocketFactory fake = new FakeSocketFactory();
        sf.setSocketFactory(fake);
        sf.setResourceFactory(new FakeMysqldFactory());

        assertEquals(0, fake.connectCalled);
        assertEquals(0, fake.beforeHandshakeCalled);
        assertEquals(0, fake.afterHandshakeCalled);

        sf.connect(null, 0, new Properties());

        assertEquals(1, fake.connectCalled);
        assertEquals(0, fake.beforeHandshakeCalled);
        assertEquals(0, fake.afterHandshakeCalled);

        sf.beforeHandshake();

        assertEquals(1, fake.connectCalled);
        assertEquals(1, fake.beforeHandshakeCalled);
        assertEquals(0, fake.afterHandshakeCalled);

        sf.afterHandshake();

        assertEquals(1, fake.connectCalled);
        assertEquals(1, fake.beforeHandshakeCalled);
        assertEquals(1, fake.afterHandshakeCalled);
    }

    public void testMultipleConnectionsAndShutdownListener() throws Exception {
        Map testMysqlds = new HashMap();
        ServerLauncherSocketFactory sf = new ServerLauncherSocketFactory();
        sf.setMyMysqlds(testMysqlds);
        FakeMysqldFactory factory = new FakeMysqldFactory();
        sf.setResourceFactory(factory);
        sf.setSocketFactory(new FakeSocketFactory());

        assertEquals(0, factory.resources.size());
        String host = "localhost";
        int port = 3306;

        Properties props = new Properties();
        props.setProperty("foo", "bar");
        props
                .setProperty(ServerLauncherSocketFactory.SERVER_DOT + "baz",
                        "wiz");

        assertEquals(0, testMysqlds.size());
        sf.connect(host, port, props);

        assertEquals(1, testMysqlds.size());

        MysqldResourceI mysqldResource = (MysqldResourceI) factory.resources
                .get(0);
        Map serverParams = mysqldResource.getServerOptions();
        assertEquals("wiz", serverParams.get("baz"));
        assertFalse("wiz", serverParams.containsKey("foo"));

        sf.connect(host, port, props);

        assertEquals(1, testMysqlds.size());

        sf.connect(host, port + 1, props);

        assertEquals(2, testMysqlds.size());

        String mapKey = testMysqlds.keySet().iterator().next().toString();
        MysqldResourceI mysqld = (MysqldResourceI) testMysqlds.get(mapKey);
        mysqld.shutdown();

        assertEquals(1, testMysqlds.size());
    }
}
