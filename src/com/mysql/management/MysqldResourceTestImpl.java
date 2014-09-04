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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mysql.management.util.Str;

/**
 * @author Eric Herman <eric@mysql.com>
 * @version $Id: MysqldResourceTestImpl.java,v 1.4 2005/04/13 19:53:24 eherman
 *          Exp $
 */
public class MysqldResourceTestImpl implements MysqldResourceI {

    private String running;

    private Map currentOptions;

    private List completionListensers;

    public MysqldResourceTestImpl() {
        this(new HashMap());
    }

    public MysqldResourceTestImpl(Map options) {
        this.running = null;
        this.currentOptions = options;
        this.completionListensers = new ArrayList();
    }

    public void start(String threadName, Map mysqldArgs) {
        if (running != null) {
            Str str = new Str();
            String msg = "already running " + running + " mysqldArgs: "
                    + str.toString(mysqldArgs.entrySet());
            throw new RuntimeException(msg);
        }
        currentOptions.putAll(mysqldArgs);
        running = threadName;
    }

    public void shutdown() {
        running = null;
        for (int i = 0; i < completionListensers.size(); i++) {
            Runnable listener = (Runnable) completionListensers.get(i);
            listener.run();
        }
    }

    public Map getServerOptions() {
        return new HashMap(currentOptions);
    }

    public boolean isRunning() {
        return running != null;
    }

    public String getVersion() {
        return MysqldResourceI.DEFAULT_VERSION;
    }

    public void addCompletionListenser(Runnable listener) {
        completionListensers.add(listener);
    }

    public void setVersion(int MajorVersion, int minorVersion, int patchLevel) {
        String msg = "version: " + MajorVersion + "." + minorVersion + "."
                + patchLevel;
        throw new RuntimeException(msg);
    }

    public void setKillDelay(int millis) {
        throw new RuntimeException("Millis: " + millis);
    }

    public boolean isReadyForConnections() {
        return isRunning();
    }
}