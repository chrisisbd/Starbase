package com.mysql.management.util;

import java.lang.reflect.Method;

import junit.framework.TestCase;

public class RuntimeTest extends TestCase {

    public void testSome() {
        Runtime realRuntime = Runtime.getRuntime();
        RuntimeI runtime = new RuntimeI.Default();
        assertEquals(realRuntime.availableProcessors(), runtime
                .availableProcessors());
        assertEquals(realRuntime.freeMemory(), runtime.freeMemory());
        assertEquals(realRuntime.maxMemory(), runtime.maxMemory());
        assertEquals(realRuntime.totalMemory(), runtime.totalMemory());
    }

    public void testStub() throws Exception {
        final RuntimeI stub = new RuntimeI.Stub();
        final TestUtil testUtil = new TestUtil();
        final Method[] methods = RuntimeI.class.getDeclaredMethods();
        for (int i = 0; i < methods.length; i++) {
            final int x = i;
            testUtil.assertNotImplemented(new TestUtil.Block() {
                public void exec() throws Exception {
                    testUtil.invoke(stub, methods[x]);
                }
            });
        }
    }
}