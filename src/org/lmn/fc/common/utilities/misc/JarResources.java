// Copyright 2000, 2001, 2002, 2003, 04, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013, 2013
// Laurence Newell
// starbase@ukraa.com
// radio.telescope@btinternet.com
//
// This file is part of Starbase.
//
// Starbase is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// Starbase is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with Starbase.  If not, see http://www.gnu.org/licenses.

package org.lmn.fc.common.utilities.misc;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;


/**
 * JarResources: JarResources maps all resources included in a
 * Zip or Jar file. Additionaly, it provides a method to extract one
 * as a blob.
 */
public final class JarResources
    {

    // external debug flag
    public boolean debugOn ;

    // jar resource mapping tables
    private final Hashtable htSizes = new Hashtable();
    private final Hashtable htJarContents = new Hashtable();

    // a jar file
    private final String jarFileName;


    /**
     * Is a test driver. Given a jar file and a resource name, it trys to
     * extract the resource and then tells us whether it could or not.
     *
     * <strong>Example</strong>
     * Let's say you have a JAR file which jarred up a bunch of gif image
     * files. Now, by using JarResources, you could extract, create, and display
     * those images on-the-fly.
     * <pre>
     *     ...
     *     JarResources JR=new JarResources("GifBundle.jar");
     *     Image image=Toolkit.createImage(JR.getResource("logo.gif");
     *     Image logo=Toolkit.getDefaultToolkit().createImage(
     *                   JR.getResources("logo.gif")
     *                   );
     *     ...
     * </pre>
     */
    public static void main(final String[] args)
        {
        if (args.length != 2)
            {
            System.err.println(
                    "usage: java JarResources <jar file name> <resource name>"
            );
            System.exit(1);
            }
        final JarResources jr = new JarResources(args[0]);
        final byte[] buff = jr.getResource(args[1]);
        if (buff == null)
            {
            System.out.println("Could not find " + args[1] + ".");
            }
        else
            {
            System.out.println("Found " + args[1] + " (length=" + buff.length + ").");
            }
        }


    /**
     * creates a JarResources. It extracts all resources from a Jar
     * into an internal hashtable, keyed by resource names.
     *
     * @param fileName a jar or zip file
     */
    public JarResources(final String fileName)
        {
        this.jarFileName = fileName;
        init();
        }


    /**
     * Extracts a jar resource as a blob.
     *
     * @param name a resource name.
 * @return
     */
    public byte[] getResource(final String name)
        {
        return (byte[]) htJarContents.get(name);
        }


    /**
     * initializes internal hash tables with Jar file resources.
     */
    private void init()
        {
        try
            {
            // extracts just sizes only.
            final ZipFile zf = new ZipFile(jarFileName);
            final Enumeration e = zf.entries();
            while (e.hasMoreElements())
                {
                final ZipEntry ze = (ZipEntry) e.nextElement();
                if (debugOn)
                    {
                    System.out.println(dumpZipEntry(ze));
                    }
                htSizes.put(ze.getName(),
                            (int) ze.getSize());
                }
            zf.close();

            // extract resources and put them into the hashtable.
            final FileInputStream fis = new FileInputStream(jarFileName);
            final BufferedInputStream bis = new BufferedInputStream(fis);
            final ZipInputStream zis = new ZipInputStream(bis);
            ZipEntry ze  ;
            while ((ze = zis.getNextEntry()) != null)
                {
                if (ze.isDirectory())
                    {
                    continue;
                    }
                if (debugOn)
                    {
                    System.out.println(
                            "ze.getName()=" + ze.getName() + "," + "getSize()=" + ze.getSize()
                    );
                    }
                int size = (int) ze.getSize();
                // -1 means unknown size.
                if (size == -1)
                    {
                    size = (Integer) htSizes.get(ze.getName());
                    }
                final byte[] b = new byte[size];
                int rb = 0;
                int chunk  ;
                while ((size - rb) > 0)
                    {
                    chunk = zis.read(b,
                                     rb,
                                     size - rb);
                    if (chunk == -1)
                        {
                        break;
                        }
                    rb += chunk;
                    }
                // add to internal resource hashtable
                htJarContents.put(ze.getName(),
                                  b);
                if (debugOn)
                    {
                    System.out.println(
                            ze.getName() + "  rb=" + rb +
                                    ",size=" + size +
                                    ",csize=" + ze.getCompressedSize()
                    );
                    }
                }
            }
        catch (NullPointerException e)
            {
            System.out.println("done.");
            }
        catch (FileNotFoundException e)
            {
            e.printStackTrace();
            }
        catch (IOException e)
            {
            e.printStackTrace();
            }
        }


    /**
     * Dumps a zip entry into a string.
     *
     * @param ze a ZipEntry
 * @return
     */
    private static String dumpZipEntry(final ZipEntry ze)
        {
        final StringBuffer sb = new StringBuffer();
        if (ze.isDirectory())
            {
            sb.append("d ");
            }
        else
            {
            sb.append("f ");
            }
        if (ze.getMethod() == ZipEntry.STORED)
            {
            sb.append("stored   ");
            }
        else
            {
            sb.append("defalted ");
            }
        sb.append(ze.getName());
        sb.append("\t");
        sb.append("" + ze.getSize());
        if (ze.getMethod() == ZipEntry.DEFLATED)
            {
            sb.append("/" + ze.getCompressedSize());
            }
        return (sb.toString());
        }


    }
