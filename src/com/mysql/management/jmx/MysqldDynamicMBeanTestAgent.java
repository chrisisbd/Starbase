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

import javax.management.DynamicMBean;
import javax.management.InstanceAlreadyExistsException;
import javax.management.JMException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

/**
 * This Simple JMX Agent is useful for testing DynamicMBeans.
 * 
 * @author Eric Herman <eric@mysql.com>
 * @version $Id: MysqldDynamicMBeanTestAgent.java,v 1.1 2005/02/16 21:46:10
 *          eherman Exp $
 */
public class MysqldDynamicMBeanTestAgent {

    private final MBeanServer mbs;

    /** creates the MBean server */
    public MysqldDynamicMBeanTestAgent(String agentName) {
        this(MBeanServerFactory.createMBeanServer(agentName));
    }

    public MysqldDynamicMBeanTestAgent(MBeanServer mbs) {
        this.mbs = mbs;
    }

    /**
     * adds the MysqldDynamicMBean with the name specified
     * 
     * @param domain
     * @param name
     * @param bean
     * @throws MalformedObjectNameException
     * @throws InstanceAlreadyExistsException
     * @throws MBeanRegistrationException
     * @throws NotCompliantMBeanException
     */
    public void addBean(String domain, String name, DynamicMBean bean)
            throws JMException {
        ObjectName beanName = new ObjectName(domain + ":name=" + name);
        mbs.registerMBean(bean, beanName);
    }

    /** releases the MBean server */
    public void shutdown() {
        MBeanServerFactory.releaseMBeanServer(mbs);
    }

    /** @return the MBean server */
    public MBeanServer get() {
        return mbs;
    }

    // ---------------
    /**
     * starts an MBean server with: 1) a MySQL bean
     */
    public static void main(String args[]) throws Exception {
        new MysqldDynamicMBeanTestAgent("mysql");
    }

}
