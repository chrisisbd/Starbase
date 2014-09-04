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
import javax.management.AttributeNotFoundException;

import com.mysql.management.MysqldResourceI;
import com.mysql.management.util.Exceptions;

/**
 * MySQL DynamicMBean
 * 
 * @author Eric Herman <eric@mysql.com>
 * @version $Id: MysqldDynamicMBean.java,v 1.6 2005/07/27 18:49:35 eherman Exp $
 */
public final class MysqldDynamicMBean extends SimpleMysqldDynamicMBean {

    public MysqldDynamicMBean() {
        super();
    }

    MysqldDynamicMBean(MysqldResourceI mysqldResource) {
        super(mysqldResource);
    }

    public synchronized void setAttribute(Attribute attribute)
            throws AttributeNotFoundException {
        super.setAttribute(attribute);
        if (attribute.getName().equals(AUTOSTART_ATTR)) {
            Object val = attribute.getValue().toString().toLowerCase();
            if (val.equals(Boolean.TRUE.toString())) {
                invokeStart();
            }
        }
    }

    private Object invokeStart() {
        Exceptions.Block block = new Exceptions.Block() {
            public Object inner() throws Exception {
                return invoke(START_METHOD, null, null);
            }
        };
        return block.exec();
    }
}