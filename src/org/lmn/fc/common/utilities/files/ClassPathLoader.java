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

package org.lmn.fc.common.utilities.files;

import org.lmn.fc.common.constants.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.List;


/***************************************************************************************************
 * See http://forum.java.sun.com/thread.jspa?tstart=0&forumID=32&threadID=300557&trange=15
 */

public final class ClassPathLoader implements FrameworkConstants,
                                              FrameworkStrings,
                                              FrameworkMetadata,
                                              FrameworkSingletons,
                                              ResourceKeys
    {
    private static final String FIELD_SYS_PATHS = "sys_paths";
    private static final String FIELD_USR_PATHS = "usr_paths";
    private static final String METHOD_ADD_URL = "addURL";

    private static final Class[] parameters = new Class[]{URL.class};


    /***********************************************************************************************
     * Construct the ClassPathLoader.
     */

    public ClassPathLoader()
        {
        }

//    public static void addFileToSystemClassLoader(final String s) throws IOException
//        {
//        final File f = new File(s);
//        addFileToSystemClassLoader(f);
//        }

    /***********************************************************************************************
     * AddFileToSystemClassLoader.
     *
     * @param file
     *
     * @throws IOException
     */

//    public static void addFileToSystemClassLoader(final File file) throws IOException
//        {
//        addURLToSystemClassLoader(file.toURL());
//        }


    /***********************************************************************************************
     * AddFileToClassLoader.
     *
     * @param loader
     * @param file
     *
     * @throws IOException
     */

    public static void addFileToClassLoader(final URLClassLoader loader,
                                            final File file) throws IOException
        {
        // ToDo REVIEW deprecation??
        addURLToClassLoader(loader, file.toURL());
        }


    /***********************************************************************************************
     * Add a URL to the System ClassLoader.
     *
     * @param url
     *
     * @throws IOException
     */

    public static void addURLToSystemClassLoader(final URL url) throws IOException
        {
        final URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        final Class sysclass = URLClassLoader.class;

        try
            {
            final Method method = sysclass.getDeclaredMethod(METHOD_ADD_URL, parameters);
            method.setAccessible(true);

            // Invokes the underlying method represented by this Method object,
            // on the specified object with the specified parameters.
            method.invoke(sysloader, url);
            }

        catch (Throwable t)
            {
            throw new IOException("Error, could not add URL to system classloader");
            }
        }


    /***********************************************************************************************
     * Add a URL to the specified ClassLoader.
     *
     * @param loader
     * @param url
     *
     * @throws IOException
     */

    public static void addURLToClassLoader(final URLClassLoader loader,
                                           final URL url) throws IOException
        {
        try
            {
            final Class classLoader;
            final Method method;

            classLoader = URLClassLoader.class;

            // Returns a Method object that reflects the specified declared method of the class
            // or interface represented by this Class object.
            // The name parameter is a String that specifies the simple name of the desired method,
            // and the parameterTypes parameter is an array of Class objects that identify
            // the method's formal parameter types, in declared order.
            // If more than one method with the same parameter types is declared in a class,
            // and one of these methods has a return type that is more specific than
            // any of the others, that method is returned;
            // otherwise one of the methods is chosen arbitrarily.
            // If the name is "<init>"or "<clinit>" a NoSuchMethodException is raised.

            method = classLoader.getDeclaredMethod(METHOD_ADD_URL, parameters);
            method.setAccessible(true);
            method.invoke(loader, url);
            }

        catch (NoSuchMethodException e)
            {
            throw new IOException("NoSuchMethodException: Could not add URL to classloader");
            }

        catch (IllegalAccessException e)
            {
            throw new IOException("IllegalAccessException: Could not add URL to classloader");
            }

        catch (InvocationTargetException e)
            {
            throw new IOException("InvocationTargetException: Could not add URL to classloader");
            }
        }


    /***********************************************************************************************
     * Force a reload of java.library.path by ClassLoader.
     * Uses Reflection to set private static sys_paths to <code>null</code>.
     * Any subsequent call to System.load() or System.loadLibrary() will have a new path to search.
     */

    public static void forceReloadOfJavaLibraryPaths()
        {
        try
            {
            final Field field;

            field = ClassLoader.class.getDeclaredField(FIELD_SYS_PATHS);
            field.setAccessible(true);
            field.set(null, null);

            //LOGGER.debug("java.library.path reloaded!");
            }

        catch (IllegalAccessException e)
            {
            LOGGER.error("showClassLoaderSearchPaths() IllegalAccessException");
            }

        catch (NoSuchFieldException e)
            {
            LOGGER.error("showClassLoaderSearchPaths() NoSuchFieldException");
            }
        }


    /***********************************************************************************************
     * Show the search path of URLs for loading classes and resources.
     * This includes the original list of URLs specified to the ClassLoader constructor,
     * along with any URLs subsequently appended by the addURLToSystemClassLoader() method.
     *
     * @param loader
     */

    public static void showURLClassLoaderSearchPath(final URLClassLoader loader)
        {
        final URL[] urls;

        if (loader != null)
            {
            LOGGER.log("URLClassLoader Search Path [loader=" + loader.getClass().getName() + "]");

            urls = loader.getURLs();

            for (int i = 0; i < urls.length; i++)
                {
                final URL url = urls[i];
                LOGGER.log(INDENT + url.getPath());
                }
            }
        }


    /***********************************************************************************************
     * Show the ClassLoader search paths.
     * This is a serious bodge using Reflection!
     */

    public static void showClassLoaderSearchPaths(final boolean show)
        {
        if (show)
            {
            try
                {
                Field field;
                List list;

                field = ClassLoader.class.getDeclaredField(FIELD_USR_PATHS);
                field.setAccessible(true);

                if (field.get(null) != null)
                    {
                    list = Arrays.asList((String[])field.get(null));

                    LOGGER.log("ClassLoader java.library.path");

                    for (int i = 0; i < list.size(); i++)
                        {
                        LOGGER.log(INDENT + list.get(i));
                        }
                    }

                field = ClassLoader.class.getDeclaredField(FIELD_SYS_PATHS);
                field.setAccessible(true);

                if (field.get(null) != null)
                    {
                    list = Arrays.asList((String[])field.get(null));

                    LOGGER.log("ClassLoader sun.boot.library.path");

                    for (int i = 0; i < list.size(); i++)
                        {
                        LOGGER.log(INDENT + list.get(i));
                        }
                    }
                }

            catch (IllegalAccessException e)
                {
                LOGGER.error("showClassLoaderSearchPaths() IllegalAccessException");
                }

            catch (NoSuchFieldException e)
                {
                LOGGER.error("showClassLoaderSearchPaths() NoSuchFieldException");
                }
            }
        }
    }
