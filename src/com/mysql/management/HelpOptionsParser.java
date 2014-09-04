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
package com.mysql.management;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import com.mysql.management.util.ClassUtil;
import com.mysql.management.util.Exceptions;
import com.mysql.management.util.Shell;
import com.mysql.management.util.Str;
import com.mysql.management.util.Threads;

/*
 * TODO Replace this with something less fragile than text parsing. Perhaps an
 * api could be added to the server?
 */

/**
 * This class is final simply as a hint to the compiler, it may be un-finalized
 * safely.
 * 
 * @author Eric Herman <eric@mysql.com>
 * @version $Id: HelpOptionsParser.java,v 1.16 2005/07/27 23:41:27 eherman Exp $
 */
final class HelpOptionsParser {

    private static final String DIVIDER = "--------------------------------- -----------------------------";
    private static final String END_TEXT = "To see what values a running";
    private static final String NO_DEFAULT_VALUE = "(No default value)";

    private Threads threads;
    private PrintStream err;
    private ClassUtil classUtil;
    private Str str;

    HelpOptionsParser(PrintStream err, Threads threads, ClassUtil classUtil,
            Str str) {
        this.err = err;
        this.threads = threads;
        this.classUtil = classUtil;
        this.str = str;
    }

    public Map parseHelp(String help) {
        String trimmed = trimToOptions(help);
        final Map map = new HashMap();
        final BufferedReader reader = new BufferedReader(new StringReader(
                trimmed));
        Exceptions.VoidBlock block = new Exceptions.VoidBlock() {
            public void inner() throws Exception {
                while (true) {
                    String line = reader.readLine();
                    if (line == null) {
                        break;
                    }
                    if (line.indexOf(' ') <= 0) {
                        continue;
                    }
                    String key = line.substring(0, line.indexOf(' '));
                    String val = line.substring(key.length()).trim();
                    if (val.equals(NO_DEFAULT_VALUE)) {
                        val = "";
                    }
                    map.put(key, val);
                }
            }
        };
        block.exec();

        return map;
    }

    String trimToOptions(String help) {
        boolean success = false;
        try {
            int dividerPos = help.indexOf(DIVIDER);
            int start = dividerPos + DIVIDER.length();
            int stop = help.indexOf(END_TEXT);
            if (dividerPos == -1) {
                throw new RuntimeException("Can not parse: <" + help + ">");
            }
            help = help.substring(start, stop);
            help = help + System.getProperty("line.separator");
            success = true;
            return help;
        } finally {
            if (!success) {
                StringBuffer msg = new StringBuffer();
                msg.append("[");
                msg.append(classUtil.shortName(this));
                msg.append("] parsing unseccessful:");
                msg.append(str.newLine());
                msg.append(help);
                err.println(msg);
            }
        }

    }

    private Map getOptionsFromHelp(MysqldResource mysqldResource, Map params) {
        Map options;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PrintStream capturedOut = new PrintStream(bos);

        params.put("help", null);
        params.put("verbose", null);

        final Shell s = mysqldResource.exec("getOptions", params, capturedOut,
                capturedOut, false);

        new Exceptions.VoidBlock() {
            protected void inner() throws InterruptedException {
                s.join();
            }
        }.exec();

        threads.pause(100);
        capturedOut.flush();
        capturedOut.close(); // should flush();

        options = parseHelp(new String(bos.toByteArray()));

        options.remove("help");
        params.remove("help");

        options.remove("verbose");
        params.remove("verbose");

        return options;
    }

    public Map getCurrentOptions(final MysqldResource mysqldResource,
            final Map params) {

        return (Map) new Exceptions.Block() {
            public Object inner() throws Exception {
                return getOptionsFromHelp(mysqldResource, params);
            }
        }.exec();
    }
}