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
package com.mysql.management.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;

/**
 * @author Eric Herman <eric@mysql.com>
 * @version $Id: FileUtil.java,v 1.13 2005/07/27 23:41:27 eherman Exp $
 */
public class FileUtil {

    public static final String JAVA_IO_TMPDIR = "java.io.tmpdir";

    private Shell.Factory shellFactory;
    private char separatorChar;
    private Streams streams;

    public FileUtil() {
        this(new Shell.Factory(), File.separatorChar, new Streams());
    }

    FileUtil(Shell.Factory shellFactory, char separatorChar, Streams streams) {
        this.shellFactory = shellFactory;
        this.separatorChar = separatorChar;
        this.streams = streams;
    }

    public File tmp() {
        return new File(System.getProperty(JAVA_IO_TMPDIR));
    }

    /**
     * Depth First traversal of the directory. Attempts to delete every file in
     * the structure.
     * 
     * @return true if the file passed in is successfully deleted
     */
    public boolean deleteTree(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                deleteTree(files[i]);
            }
        }
        return file.delete();
    }

    /* TODO make this more platform independant */
    /**
     * On UNIX systems, in order for a file to be executable, it needs to have
     * the execute bit set. This method executes a "chmod +x filename"
     */
    public void addExecutableRights(File executable, PrintStream out,
            PrintStream err) {
        if (isWindows()) {
            return;
        }
        String[] args = { "chmod", "+x", executable.getPath() };
        String tName = "make " + executable + " runable";
        shellFactory.newShell(args, tName, out, err).run();
    }

    public boolean isWindows() {
        return separatorChar == '\\';
    }

    public String asString(File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        try {
            return streams.readString(fis);
        } finally {
            fis.close();
        }
    }

    public File nullFile() {
        return new File("");
    }

    public File newFile(Object fileName) {
        if (fileName == null) {
            return nullFile();
        }
        return new File(fileName.toString());
    }
}