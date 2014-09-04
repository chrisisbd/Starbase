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
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.mysql.management.util.ClassUtil;
import com.mysql.management.util.CommandLineOptionsParser;
import com.mysql.management.util.Exceptions;
import com.mysql.management.util.FileUtil;
import com.mysql.management.util.Platform;
import com.mysql.management.util.ProcessUtil;
import com.mysql.management.util.Shell;
import com.mysql.management.util.Str;
import com.mysql.management.util.Streams;
import com.mysql.management.util.TeeOutputStream;
import com.mysql.management.util.Threads;

/**
 * This class is final simply as a hint to the compiler, it may be un-finalized
 * safely.
 * 
 * @author Eric Herman <eric@mysql.com>
 * @version $Id: MysqldResource.java,v 1.56 2005/08/24 23:31:58 eherman Exp $
 */
public final class MysqldResource implements MysqldResourceI {
    private String versionString;
    private Map options;
    private Shell shell;
    private File baseDir;
    private File pidFile;
    private String msgPrefix;
    private String pid;
    private String osName;
    private String osArch;
    private PrintStream out;
    private PrintStream err;
    private Exception trace;
    private int killDelay;
    private List completionListensers;
    private boolean readyForConnections;

    // collaborators
    private HelpOptionsParser optionParser;

    // utilities
    private FileUtil fileUtil;
    private Streams streams;
    private Shell.Factory shellFactory;
    private ClassUtil classUtil;
    private Threads threads;
    private Str str;

    public MysqldResource() {
        this(new FileUtil().nullFile());
    }

    public MysqldResource(File baseDir) {
        this(baseDir, System.out, System.err);
    }

    public MysqldResource(File baseDir, PrintStream out, PrintStream err) {
        this(baseDir, out, err, new FileUtil(), new Shell.Factory(),
                new Streams(), new Threads(), new Str(), new ClassUtil());
    }

    MysqldResource(File baseDir, PrintStream out, PrintStream err,
            FileUtil fileUtil, Shell.Factory shellFactory, Streams streams,
            Threads threads, Str str, ClassUtil classUtil) {
        this.out = out;
        this.err = err;
        this.fileUtil = fileUtil;
        this.shellFactory = shellFactory;
        this.streams = streams;
        this.threads = threads;
        this.str = str;
        this.classUtil = classUtil;
        this.optionParser = new HelpOptionsParser(err, threads, classUtil, str);
        this.versionString = MysqldResourceI.DEFAULT_VERSION;
        this.killDelay = 30000;
        if (baseDir.equals(fileUtil.nullFile())) {
            this.baseDir = new File(fileUtil.tmp(), "mysql-c.mxj");
        } else {
            this.baseDir = baseDir;
        }
        this.pidFile = null;
        this.msgPrefix = "[" + classUtil.shortName(getClass()) + "] ";
        this.options = new HashMap();
        this.setShell(null);
        setOsAndArch(System.getProperty(Platform.OS_NAME), System
                .getProperty(Platform.OS_ARCH));
        this.trace = new Exception();
        this.completionListensers = new ArrayList();
    }

    /**
     * Starts mysqld passing it the parameters specified in the arguments map.
     * No effect if MySQL is already running
     */
    public void start(String threadName, Map mysqldArgs) {
        if ((getShell() != null) || processRunning()) {
            printMessage("mysqld already running (process: " + pid() + ")");
            return;
        }

        options = optionParser.getCurrentOptions(this, mysqldArgs);

        // printMessage("mysqld : " + str.toString(mysqldArgs.entrySet()));
        out.flush();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ByteArrayOutputStream baes = new ByteArrayOutputStream();
        TeeOutputStream teeOutStream = new TeeOutputStream(out, baos);
        TeeOutputStream teeErrStream = new TeeOutputStream(err, baes);
        addCompletionListenser(new Runnable() {
            public void run() {
                setReadyForConnection(false);
                setShell(null);
                completionListensers.remove(this);
            }
        });
        PrintStream psOut = new PrintStream(teeOutStream);
        PrintStream psErr = new PrintStream(teeErrStream);
        setShell(exec(threadName, mysqldArgs, psOut, psErr, true));
        long launchtime = System.currentTimeMillis();

        reportPid();

        boolean ready = false;
        long giveUp = launchtime + killDelay;
        while (!ready && (System.currentTimeMillis() < giveUp)) {
            String lookFor = "ready for connections";
            String lookIn1 = baos.toString();
            String lookIn2 = baes.toString();
            ready = str.containsIgnoreCase(lookIn1, lookFor);
            ready |= str.containsIgnoreCase(lookIn2, lookFor);
            threads.pause(100);
        }
        teeOutStream.nullStreamTwo();
        teeErrStream.nullStreamTwo();
        setReadyForConnection(ready);
    }

    private void setReadyForConnection(boolean ready) {
        readyForConnections = ready;
    }

    public synchronized boolean isReadyForConnections() {
        return readyForConnections;
    }

    private void reportPid() {
        boolean printed = false;
        long giveUp = System.currentTimeMillis() + 3000;
        while (!printed && (System.currentTimeMillis() < giveUp)) {
            if (pidFile().exists() && pidFile().length() > 0) {
                threads.pause(25);
                printMessage("mysqld running as process: " + pid());

                out.flush();
                printed = true;
            }
            threads.pause(100);
        }

        reportIfNoPidfile(printed);
    }

    synchronized String pid() {
        if (pid == null) {
            if (!pidFile().exists()) {
                return "No PID";
            }
            Exceptions.Block block = new Exceptions.Block() {
                public Object inner() throws IOException {
                    return fileUtil.asString(pidFile()).trim();
                }
            };
            pid = (String) block.exec();
        }
        return pid;
    }

    void reportIfNoPidfile(boolean pidFileFound) {
        if (!pidFileFound) {
            printWarning("mysqld pid-file not found:  " + pidFile());
        }
    }

    /**
     * Kills the MySQL process.
     */
    public synchronized void shutdown() {
        boolean haveShell = (getShell() != null);
        if (!pidFile().exists() && !haveShell) {
            printMessage("Mysqld not running. No file: " + pidFile());
            return;
        }
        printMessage("stopping mysqld (process: " + pid() + ")");

        issueNormalKill();

        if (processRunning()) {
            issueForceKill();
        }

        if (shellRunning()) {
            destroyShell();
        }
        setShell(null);

        if (processRunning()) {
            printWarning("Process " + pid + "still running; not deleting "
                    + pidFile());
        } else {
            threads.pause(150);
            System.gc();
            threads.pause(150);
            pidFile().deleteOnExit();
            pidFile().delete();
            pidFile = null;
            pid = null;
        }

        setReadyForConnection(false);

        printMessage("clearing options");
        options.clear();
        out.flush();

        printMessage("shutdown complete");
    }

    void destroyShell() {
        String shellName = getShell().getName();
        printWarning("attempting to destroy thread " + shellName);
        getShell().destroyProcess();
        waitForShellToDie();
        String msg = (shellRunning() ? "not " : "") + "destroyed.";
        printWarning(shellName + " " + msg);
    }

    void issueForceKill() {
        printWarning("attempting to \"force kill\" " + pid());
        Exceptions.VoidBlock block = new Exceptions.VoidBlock() {
            public void inner() {
                new ProcessUtil(pid(), err, err).forceKill();
            }
        };
        block.execSwallowingException(err);

        waitForProcessToDie();
        if (processRunning()) {
            String msg = (processRunning() ? "not " : "") + "killed.";
            printWarning(pid() + " " + msg);
        } else {
            printMessage("force kill " + pid() + " issued.");
        }
    }

    private void issueNormalKill() {
        if (!pidFile().exists()) {
            printWarning("Not running? File not found: " + pidFile());
            return;
        }
        Exceptions.VoidBlock block = new Exceptions.VoidBlock() {
            public void inner() {
                new ProcessUtil(pid(), err, err).kill();
            }
        };
        block.execSwallowingException(err);
        waitForProcessToDie();
    }

    private void waitForProcessToDie() {
        long giveUp = System.currentTimeMillis() + killDelay;
        while (processRunning() && System.currentTimeMillis() < giveUp) {
            threads.pause(250);
        }
    }

    private void waitForShellToDie() {
        long giveUp = System.currentTimeMillis() + killDelay;
        while (shellRunning() && System.currentTimeMillis() < giveUp) {
            threads.pause(250);
        }
    }

    public synchronized Map getServerOptions() {
        if (options.isEmpty()) {
            options = optionParser.getCurrentOptions(this, new HashMap());
        }
        return new HashMap(options);
    }

    public synchronized boolean isRunning() {
        return shellRunning() || processRunning();
    }

    private boolean processRunning() {
        return pidFile().exists()
                && new ProcessUtil(pid(), out, err).isRunning();
    }

    private boolean shellRunning() {
        return (getShell() != null) && (getShell().isAlive());
    }

    public synchronized String getVersion() {
        return versionString;
    }

    private String getVersionDir() {
        return getVersion().replaceAll("\\.", "-");
    }

    public synchronized void setVersion(int MajorVersion, int minorVersion,
            int patchLevel) {
        versionString = MajorVersion + "." + minorVersion + "." + patchLevel;
    }

    private void printMessage(String msg) {
        println(out, msg);
    }

    private void printWarning(String msg) {
        println(err, "");
        println(err, msg);
    }

    private void println(PrintStream stream, String msg) {
        stream.println(msgPrefix + msg);
    }

    /* called from constructor, over-ride with care */
    final void setOsAndArch(String osName, String osArch) {
        if (osName.indexOf("Win") != -1) {
            osName = "Win";
        }
        this.osName = osName;
        this.osArch = osArch;
    }

    /** called from option parser as well */
    synchronized Shell exec(String threadName, Map mysqldArgs,
            PrintStream outStream, PrintStream errStream, boolean withListeners) {
        mysqldArgs.put(MysqldResourceI.BASEDIR, baseDir.getPath());
        mysqldArgs.put(MysqldResourceI.PID_FILE, pidFile().getPath());
        socket(mysqldArgs);
        File dataDir = dataDir(mysqldArgs);

        makeMysqld();
        ensureEssentialFilesExist(dataDir);
        String[] args = constructArgs(mysqldArgs);
        outStream.println(str.toString(args));
        Shell launch = shellFactory.newShell(args, threadName, outStream,
                errStream);
        if (withListeners) {
            for (int i = 0; i < completionListensers.size(); i++) {
                Runnable listener = (Runnable) completionListensers.get(i);
                launch.addCompletionListener(listener);
            }
        }
        launch.setDaemon(true);

        printMessage("launching mysqld (" + threadName + ")");

        launch.start();
        return launch;
    }

    File makeMysqld() {
        final File mysqld = getMysqldFilePointer();
        if (!mysqld.exists()) {
            mysqld.getParentFile().mkdirs();
            streams.createFileFromResource(getResourceName(), mysqld);
        }
        fileUtil.addExecutableRights(mysqld, out, err);
        return mysqld;
    }

    String getResourceName() {
        String dir = osName + "-" + osArch;
        String name = executableName();
        return getVersionDir() + Streams.RESOURCE_SEPARATOR + dir
                + Streams.RESOURCE_SEPARATOR + name;
    }

    private String executableName() {
        String mysqld = "mysqld";
        return ((isWindows()) ? mysqld + "-nt.exe" : mysqld);
    }

    boolean isWindows() {
        return osName.equals("Win");
    }

    File getMysqldFilePointer() {
        File bin = new File(baseDir, "bin");
        return new File(bin, executableName());
    }

    void ensureEssentialFilesExist(File dataDir) {
        streams.expandResourceJar(dataDir, getVersionDir()
                + Streams.RESOURCE_SEPARATOR + "data_dir.jar");
        streams.expandResourceJar(baseDir, getVersionDir()
                + Streams.RESOURCE_SEPARATOR + shareJar());
    }

    void socket(Map mysqldArgs) {
        String socketString = (String) mysqldArgs.get(MysqldResourceI.SOCKET);
        if (socketString != null) {
            return;
        }

        File socket = new File(baseDir, "mysql.sock");
        mysqldArgs.put(MysqldResourceI.SOCKET, socket.toString());
    }

    File dataDir(Map mysqldArgs) {
        String dataDirString = (String) mysqldArgs.get(MysqldResourceI.DATADIR);
        if (dataDirString != null) {
            return new File(dataDirString);
        }

        File dataDir = new File(baseDir, "data");
        mysqldArgs.put(MysqldResourceI.DATADIR, dataDir.toString());
        return dataDir;
    }

    String[] constructArgs(Map mysqldArgs) {
        List strs = new ArrayList();
        strs.add(getMysqldFilePointer().getPath());

        strs.add("--no-defaults");
        if (isWindows()) {
            strs.add("--console");
        }
        Iterator it = mysqldArgs.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();
            StringBuffer buf = new StringBuffer("--");
            buf.append(key);
            if (value != null) {
                buf.append("=");
                buf.append(value);
            }
            strs.add(buf.toString());
        }

        return str.toStringArray(strs);
    }

    protected void finalize() throws Throwable {
        super.finalize();
        if (getShell() != null) {
            printWarning("resource released without closure.");
            trace.printStackTrace(err);
        }
    }

    String shareJar() {
        String shareJar = "share_dir.jar";
        if (isWindows()) {
            shareJar = "win_" + shareJar;
        }
        return shareJar;
    }

    void setShell(Shell shell) {
        this.shell = shell;
    }

    Shell getShell() {
        return shell;
    }

    File getBaseDir() {
        return baseDir;
    }

    public synchronized void setKillDelay(int millis) {
        this.killDelay = millis;
    }

    public synchronized void addCompletionListenser(Runnable listener) {
        completionListensers.add(listener);
    }

    void printUsage() {
        String command = "java " + MysqldResource.class.getName();
        String basedir = " --" + MysqldResourceI.BASEDIR;
        String datadir = " --" + MysqldResourceI.DATADIR;
        out.println("Usage to start: ");
        out.println(command + " [ server options ]");
        out.println();
        out.println("Usage to shutdown: ");
        out.println(command + " --shutdown [" + basedir
                + "=/full/path/to/basedir ]");
        out.println();
        out.println("Common server options include:");
        out.println(basedir + "=/full/path/to/basedir");
        out.println(datadir + "=/full/path/to/datadir");
        out.println(" --" + MysqldResourceI.SOCKET
                + "=/full/path/to/socketfile");
        out.println();
        out.println("Example:");
        out.println(command + basedir + "=/home/duke/dukeapp/db" + datadir
                + "=/data/dukeapp/data" + " --max_allowed_packet=65000000");
        out.println(command + " --shutdown" + basedir
                + "=/home/duke/dukeapp/db");
        out.println();
    }

    synchronized private File pidFile() {
        if (pidFile == null) {
            String className = classUtil.shortName(getClass());
            pidFile = new File(this.baseDir, className + ".pid");
        }
        return pidFile;
    }

    // ---------------------------------------------------------
    public static void main(String[] args) {
        CommandLineOptionsParser clop = new CommandLineOptionsParser(args);
        MysqldResource mysqld = new MysqldResource(clop.getBaseDir());
        if (args.length == 0) {
            mysqld.printUsage();
        } else if (clop.isShutdown()) {
            mysqld.shutdown();
        } else {
            mysqld.start(new Threads().newName(), clop.asMap());
        }
    }
}