package com.mysql.management.util;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTestsSuite {
    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(CausedSQLExceptionTest.class);
        suite.addTestSuite(ClassUtilTest.class);
        suite.addTestSuite(CommandLineOptionsParserTest.class);
        suite.addTestSuite(DefaultsMapTest.class);
        suite.addTestSuite(EqualsTest.class);
        suite.addTestSuite(ExceptionsTest.class);
        suite.addTestSuite(FileUtilTest.class);
        suite.addTestSuite(MapEntryTest.class);
        suite.addTestSuite(PlatformTest.class);
        suite.addTestSuite(ProcessUtilTest.class);
        suite.addTestSuite(RuntimeTest.class);
        suite.addTestSuite(ShellTest.class);
        suite.addTestSuite(StreamsTest.class);
        suite.addTestSuite(StreamConnectorTest.class);
        suite.addTestSuite(StrTest.class);

        suite.addTestSuite(AcceptanceTest.class);

        return suite;
    }
}
