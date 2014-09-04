package com.mysql.management.jmx.jboss;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTestsSuite {
    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(JBossMysqldDynamicMBeanTest.class);
        return suite;
    }
}
