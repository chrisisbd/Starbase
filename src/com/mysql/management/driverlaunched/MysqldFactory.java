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
package com.mysql.management.driverlaunched;

import java.io.File;

import com.mysql.management.MysqldResource;
import com.mysql.management.MysqldResourceI;

public interface MysqldFactory {
    MysqldResourceI newMysqldResource(File baseDir);

    public static final class Default implements MysqldFactory {
        public MysqldResourceI newMysqldResource(File baseDir) {
            return new MysqldResource(baseDir);
        }
    }
}