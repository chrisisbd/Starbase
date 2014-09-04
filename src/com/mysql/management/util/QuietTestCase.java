package com.mysql.management.util;

import java.io.PrintStream;

import junit.framework.TestCase;

public abstract class QuietTestCase extends TestCase {
    private PrintStream systemDotOut;

    private PrintStream systemDotErr;

    protected void setUp() {
        this.systemDotOut = System.out;
        this.systemDotErr = System.err;
        System.setOut(getTestStream(systemDotOut));
        System.setErr(getTestStream(systemDotErr));
    }

    protected void tearDown() {
        resetOutAndErr();
    }

    protected void resetOutAndErr() {
        System.setOut(systemDotOut);
        System.setErr(systemDotErr);
    }

    protected void warn(String msg) {
        systemDotErr.println(msg);
    }

    private PrintStream getTestStream(PrintStream real) {
        String defaultVal = Boolean.TRUE.toString();
        String silentStr = System.getProperty("c-mxj_test_silent", defaultVal);
        Boolean b = Boolean.valueOf(silentStr);
        return b.booleanValue() ? new NullPrintStream() : real;
    }
}
