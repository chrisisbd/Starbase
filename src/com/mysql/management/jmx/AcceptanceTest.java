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
package com.mysql.management.jmx;

import javax.management.Attribute;

import com.mysql.jdbc.NonRegisteringDriver;
import com.mysql.management.MysqldResource;
import com.mysql.management.MysqldResourceI;
import com.mysql.management.jmx.jboss.JBossMysqldDynamicMBean;
import com.mysql.management.util.QuietTestCase;
import com.mysql.management.util.TestUtil;
import com.mysql.management.util.Threads;

/**
 * @author Eric Herman <eric@mysql.com>
 * @version $Id: AcceptanceTest.java,v 1.8 2005/07/05 21:19:40 eherman Exp $
 */
public class AcceptanceTest extends QuietTestCase {

    private MysqldDynamicMBeanTestAgent agent;

    private SimpleMysqldDynamicMBean bean;

    private Threads threads = new Threads();

    protected void tearDown() {
        try {
            if (bean != null) {
                try {
                    bean.invoke(SimpleMysqldDynamicMBean.STOP_METHOD, null,
                            null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                threads.pause(50);
            }
            if (agent != null) {
                agent.shutdown();
            }
        } finally {
            super.tearDown();
        }
    }

    public void testConnectorMXJPropertiesTransformDefaultConstructor() {
        ConnectorMXJPropertiesTransform munger = null;
        agent = new MysqldDynamicMBeanTestAgent("mysql");
        munger = new ConnectorMXJPropertiesTransform();
        assertEquals(agent.get(), munger.getMBeanServer());
    }

    public void testEverything() throws Exception {
        agent = new MysqldDynamicMBeanTestAgent("mysql");

        String url = "jdbc:mysql:///test" + "?"
                + NonRegisteringDriver.PROPERTIES_TRANSFORM_KEY + "="
                + ConnectorMXJPropertiesTransform.class.getName();

        bean = new MysqldDynamicMBean();
        MysqldResourceI mysqldResource = bean.getMysqldResource();
        // mysqldResource.setKillDelay(3000);
        agent.addBean("mysql", "MySQL1", bean);

        assertEquals(false, mysqldResource.isRunning());
        String port = "" + new TestUtil().testPort();

        assertEquals("3306", mysqldResource.getServerOptions().get(
                MysqldResourceI.PORT));

        bean.setAttribute(new Attribute(MysqldResourceI.PORT, port));
        bean.invoke(SimpleMysqldDynamicMBean.START_METHOD, null, null);
        int i = 0;
        while (++i < 100) {
            if (mysqldResource.isRunning()) {
                break;
            }
            threads.pause(50);
        }
        assertTrue("still not started: " + i, mysqldResource.isRunning());
        assertEquals(port, mysqldResource.getServerOptions().get(
                MysqldResourceI.PORT));

        new TestUtil().assertConnectViaJDBC(url);
    }

    public void testJBossDefaultConstructor() throws Exception {
        SimpleMysqldDynamicMBean jbossbean = new JBossMysqldDynamicMBean();
        MysqldResourceI mysqldResource = jbossbean.getMysqldResource();
        assertEquals(MysqldResource.class, mysqldResource.getClass());
    }
}
