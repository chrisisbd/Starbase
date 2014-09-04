package com.mysql.management.jmx;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTestsSuite {
    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(ConnectorMXJPropertiesTransformTest.class);
        suite.addTestSuite(MysqldDynamicMBeanTest.class);
        suite.addTestSuite(SimpleMysqldDynamicMBeanTest.class);

        suite.addTest(com.mysql.management.jmx.jboss.AllTestsSuite.suite());

        // slow tests:
        suite.addTestSuite(AcceptanceTest.class);
        return suite;
    }
}
