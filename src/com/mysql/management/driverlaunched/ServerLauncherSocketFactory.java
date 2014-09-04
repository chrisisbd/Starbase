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
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.mysql.jdbc.SocketFactory;
import com.mysql.jdbc.StandardSocketFactory;
import com.mysql.management.MysqldResourceI;
import com.mysql.management.util.FileUtil;
import com.mysql.management.util.Str;

/**
 * This class is final simply as a hint to the compiler, it may be un-finalized
 * safely.
 * 
 * @author Eric Herman <eric@mysql.com>
 */
public final class ServerLauncherSocketFactory implements SocketFactory {

    public static final String SERVER_DOT = "server.";

    private static Map startedMysqlds = new HashMap();
    private static int launchCount = 0;

    private Map myMysqlds;
    private MysqldFactory resourceFactory;
    private SocketFactory socketFactory;

    public ServerLauncherSocketFactory() {
        setResourceFactory(new MysqldFactory.Default());
        setSocketFactory(new StandardSocketFactory());
        setMyMysqlds(startedMysqlds);
    }

    public Socket connect(String host, int portNumber, Properties props)
            throws SocketException, IOException {
        String mapKey = host + ":" + portNumber;
        synchronized (getStartedMysqlds()) {
            ensureMysqlStarted(mapKey, portNumber, props);
        }

        return getSocketFactory().connect(host, portNumber, props);
    }

    private void ensureMysqlStarted(final String mapKey, int port,
            Properties props) {
        if (getStartedMysqlds().containsKey(mapKey)) {
            return;
        }
        Map serverOpts = new HashMap();
        for (Enumeration enums = props.propertyNames(); enums.hasMoreElements();) {
            String key = enums.nextElement().toString();
            if (key.startsWith(SERVER_DOT)) {
                serverOpts.put(key.substring(SERVER_DOT.length()), props
                        .getProperty(key));
            }
        }
        serverOpts.put(MysqldResourceI.PORT, Integer.toString(port));
        File baseDir = new FileUtil().newFile(serverOpts
                .get(MysqldResourceI.BASEDIR));
        MysqldResourceI mysqld = resourceFactory.newMysqldResource(baseDir);
        mysqld.addCompletionListenser(new Runnable() {
            public void run() {
                remove(mapKey);
            }
        });
        launchCount++;
        String threadName = "driver_launched_mysqld_" + launchCount;
        mysqld.start(threadName, serverOpts);
        getStartedMysqlds().put(mapKey, mysqld);
    }

    private void remove(String mapKey) {
        synchronized (getStartedMysqlds()) {
            getStartedMysqlds().remove(mapKey);
        }
    }

    public Socket afterHandshake() throws SocketException, IOException {
        return getSocketFactory().afterHandshake();
    }

    public Socket beforeHandshake() throws SocketException, IOException {
        return getSocketFactory().beforeHandshake();
    }

    void setResourceFactory(MysqldFactory resourceFactory) {
        this.resourceFactory = resourceFactory;
    }

    MysqldFactory getResourceFactory() {
        return resourceFactory;
    }

    void setSocketFactory(SocketFactory socketFactory) {
        this.socketFactory = socketFactory;
    }

    SocketFactory getSocketFactory() {
        return socketFactory;
    }

    void setMyMysqlds(Map mysqldMap) {
        this.myMysqlds = mysqldMap;
    }

    Map getStartedMysqlds() {
        return myMysqlds;
    }

    public static void shutdown(String hostColonPort) {
        shutdown(hostColonPort, System.out);
    }

    public static void shutdown(String hostColonPort, PrintStream out) {
        synchronized (startedMysqlds) {
            Object obj = startedMysqlds.get(hostColonPort);
            MysqldResourceI mysqld = (MysqldResourceI) obj;
            if (mysqld != null) {
                startedMysqlds.remove(hostColonPort);
                mysqld.shutdown();
            } else {
                String list = new Str().toString(startedMysqlds.keySet());
                out.println("mysqld [" + hostColonPort + "] not found in "
                        + list);
            }
        }
    }
}
