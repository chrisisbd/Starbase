package com.mysql.management.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

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

/**
 * String utility methods.
 * 
 * This class is final simply as a hint to the compiler, it may be un-finalized
 * safely.
 * 
 * @author Eric Herman <eric@mysql.com>
 * @version $Id: Str.java,v 1.10 2005/05/07 14:19:52 eherman Exp $
 */
public final class Str {
    /* merge with other string utility */

    private String newLine;

    public Str() {
        newLine = System.getProperty("line.separator");
    }

    /**
     * returns the contentents of the collection as a string a collections with
     * "a", "b", null, and new Integer(1) would return: {[a][b][null][1]}
     */
    public String toString(Collection strings) {
        return toString(strings, "[", "][", "]");
    }

    /**
     * returns the contentents of the collection as a string a collections with
     * "a", "b", null, and new Integer(1) would return: {[a][b][null][1]}
     */
    public String toString(Object[] objects) {
        return toString(Arrays.asList(objects));
    }

    /**
     * @param objs
     *            collection
     * @param prefix
     * @param separator
     * @param postfix
     * @return the contentents of the collection as a string
     */
    public String toString(Collection objs, String prefix, String separator,
            String postfix) {
        StringBuffer buf = new StringBuffer(prefix);
        for (Iterator iter = objs.iterator(); iter.hasNext();) {
            Object next = iter.next();
            if (next instanceof Object[]) {
                next = toString((Object[]) next);
            } else if (next instanceof Collection) {
                next = toString((Collection) next);
            }

            buf.append(next);

            if (iter.hasNext()) {
                buf.append(separator);
            }
        }
        buf.append(postfix);
        return buf.toString();
    }

    public boolean containsIgnoreCase(String searchIn, String searchFor) {
        return searchIn.toLowerCase().indexOf(searchFor.toLowerCase()) != -1;
    }

    public String newLine() {
        return newLine;
    }

    public String[] toStringArray(List strings) {
        return (String[]) strings.toArray(new String[strings.size()]);
    }
}