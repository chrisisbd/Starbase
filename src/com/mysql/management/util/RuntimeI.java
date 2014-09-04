package com.mysql.management.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface RuntimeI {

    /**
     * @see java.lang.Runtime#addShutdownHook(Thread hook) {
     */
    void addShutdownHook(Thread hook);

    /**
     * @see java.lang.Runtime#availableProcessors()
     */
    int availableProcessors();

    /**
     * @see java.lang.Runtime#exec(java.lang.String, java.lang.String[],
     *      java.io.File)
     */
    Process exec(String command, String[] envp, File dir) throws IOException;

    /**
     * @see java.lang.Runtime#exec(java.lang.String, java.lang.String[])
     */
    Process exec(String cmd, String[] envp) throws IOException;

    /**
     * @see java.lang.Runtime#exec(java.lang.String)
     */
    Process exec(String command) throws IOException;

    /**
     * @see java.lang.Runtime#exec(java.lang.String[], java.lang.String[],
     *      java.io.File)
     */
    Process exec(String[] cmdarray, String[] envp, File dir) throws IOException;

    /**
     * @see java.lang.Runtime#exec(java.lang.String[], java.lang.String[])
     */
    Process exec(String[] cmdarray, String[] envp) throws IOException;

    /**
     * @see java.lang.Runtime#exec(java.lang.String[])
     */
    Process exec(String[] cmdarray) throws IOException;

    /**
     * @see java.lang.Runtime#exit(int)
     */
    void exit(int status);

    /**
     * @see java.lang.Runtime#freeMemory()
     */
    long freeMemory();

    /**
     * @see java.lang.Runtime#gc()
     */
    void gc();

    /**
     * @see java.lang.Runtime#getLocalizedInputStream(java.io.InputStream)
     * @deprecated As of JDK&nbsp;1.1, the preferred way to translate a byte
     *             stream in the local encoding into a character stream in
     *             Unicode is via the <code>InputStreamReader</code> and
     *             <code>BufferedReader</code> classes.
     */
    InputStream getLocalizedInputStream(InputStream in);

    /**
     * @see java.lang.Runtime#getLocalizedOutputStream(java.io.OutputStream)
     * @deprecated As of JDK&nbsp;1.1, the preferred way to translate a Unicode
     *             character stream into a byte stream in the local encoding is
     *             via the <code>OutputStreamWriter</code>,
     *             <code>BufferedWriter</code>, and <code>PrintWriter</code>
     *             classes.
     */
    OutputStream getLocalizedOutputStream(OutputStream out);

    /**
     * @see java.lang.Runtime#halt(int)
     */
    void halt(int status);

    /**
     * @see java.lang.Runtime#load(java.lang.String)
     */
    void load(String filename);

    /**
     * @see java.lang.Runtime#loadLibrary(java.lang.String)
     */
    void loadLibrary(String libname);

    /**
     * @see java.lang.Runtime#maxMemory()
     */
    long maxMemory();

    /**
     * @see java.lang.Runtime#removeShutdownHook(java.lang.Thread)
     */
    boolean removeShutdownHook(Thread hook);

    /**
     * @see java.lang.Runtime#runFinalization()
     */
    void runFinalization();

    /**
     * @see java.lang.Runtime#totalMemory()
     */
    long totalMemory();

    /**
     * @see java.lang.Runtime#traceInstructions(boolean)
     */
    void traceInstructions(boolean on);

    /**
     * @see java.lang.Runtime#traceMethodCalls(boolean)
     */
    void traceMethodCalls(boolean on);

    // -------------------------------------
    public static final class Default extends RuntimeI.Stub {
        Runtime runtime = Runtime.getRuntime();

        // public void addShutdownHook(Thread hook) {
        // runtime.addShutdownHook(hook);
        // }

        public int availableProcessors() {
            return runtime.availableProcessors();
        }

        // public Process exec(String command, String[] envp, File dir)
        // throws IOException {
        // return runtime.exec(command, envp, dir);
        // }

        // public Process exec(String cmd, String[] envp) throws IOException {
        // return runtime.exec(cmd, envp);
        // }

        // public Process exec(String command) throws IOException {
        // return runtime.exec(command);
        // }

        public Process exec(String[] cmdarray, String[] envp, File dir)
                throws IOException {
            return runtime.exec(cmdarray, envp, dir);
        }

        // public Process exec(String[] cmdarray, String[] envp)
        // throws IOException {
        // return runtime.exec(cmdarray, envp);
        // }

        // public Process exec(String[] cmdarray) throws IOException {
        // return runtime.exec(cmdarray);
        // }

        // public void exit(int status) {
        // runtime.exit(status);
        // }

        public long freeMemory() {
            return runtime.freeMemory();
        }

        // public void gc() {
        // runtime.gc();
        // }

        // /** @deprecated */
        // public InputStream getLocalizedInputStream(InputStream in) {
        // return runtime.getLocalizedInputStream(in);
        // }

        // /** @deprecated */
        // public OutputStream getLocalizedOutputStream(OutputStream out) {
        // return runtime.getLocalizedOutputStream(out);
        // }

        // public void halt(int status) {
        // runtime.halt(status);
        // }

        // public void load(String filename) {
        // runtime.load(filename);
        // }

        // public void loadLibrary(String libname) {
        // runtime.loadLibrary(libname);
        // }

        public long maxMemory() {
            return runtime.maxMemory();
        }

        // public boolean removeShutdownHook(Thread hook) {
        // return runtime.removeShutdownHook(hook);
        // }

        // public void runFinalization() {
        // runtime.runFinalization();
        // }

        public long totalMemory() {
            return runtime.totalMemory();
        }

        // public void traceInstructions(boolean on) {
        // runtime.traceInstructions(on);
        // }

        // public void traceMethodCalls(boolean on) {
        // runtime.traceMethodCalls(on);
        // }
    }

    // -------------------------------------
    public static class Stub implements RuntimeI {

        public void addShutdownHook(Thread hook) {
            throw new NotImplementedException(hook);
        }

        public int availableProcessors() {
            throw new NotImplementedException();
        }

        public Process exec(String command, String[] envp, File dir)
        /* throws IOException */{
            throw new NotImplementedException(command, envp, dir);
        }

        public Process exec(String cmd, String[] envp)
        /* throws IOException */{
            throw new NotImplementedException(cmd, envp);
        }

        public Process exec(String command)
        /* throws IOException */{
            throw new NotImplementedException(command);
        }

        public Process exec(String[] cmdarray, String[] envp, File dir)
                throws IOException {
            Object[] params = new Object[] { cmdarray, envp, dir };
            String msg = new Str().toString(params);
            throw new IOException("Not implemented: " + msg);
        }

        public Process exec(String[] cmdarray, String[] envp)
        /* throws IOException */{
            throw new NotImplementedException(cmdarray, envp);
        }

        public Process exec(String[] cmdarray)
        /* throws IOException */{
            throw new NotImplementedException(cmdarray);
        }

        public void exit(int status) {
            throw new NotImplementedException(new Integer(status));
        }

        public long freeMemory() {
            throw new NotImplementedException();
        }

        public void gc() {
            throw new NotImplementedException();
        }

        public InputStream getLocalizedInputStream(InputStream in) {
            throw new NotImplementedException(in);
        }

        public OutputStream getLocalizedOutputStream(OutputStream out) {
            throw new NotImplementedException(out);
        }

        public void halt(int status) {
            throw new NotImplementedException(new Integer(status));
        }

        public void load(String filename) {
            throw new NotImplementedException(filename);
        }

        public void loadLibrary(String libname) {
            throw new NotImplementedException(libname);
        }

        public long maxMemory() {
            throw new NotImplementedException();
        }

        public boolean removeShutdownHook(Thread hook) {
            throw new NotImplementedException(hook);
        }

        public void runFinalization() {
            throw new NotImplementedException();
        }

        public long totalMemory() {
            throw new NotImplementedException();
        }

        public void traceInstructions(boolean on) {
            throw new NotImplementedException(new Boolean(on));
        }

        public void traceMethodCalls(boolean on) {
            throw new NotImplementedException(new Boolean(on));
        }
    }
}