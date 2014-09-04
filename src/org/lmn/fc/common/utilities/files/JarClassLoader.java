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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.Attributes;


public class JarClassLoader extends URLClassLoader
    {
    private final URL url;

    public JarClassLoader(final URL url)
        {
        super(new URL[]{url});
        this.url = url;

        System.out.println("JarClassLoader URL=" + this.url);
        }

    public final String getMainClassName() throws IOException
        {
        final URL u = new URL("jar", "", url + "!/");
        final JarURLConnection uc = (JarURLConnection) u.openConnection();
        final Attributes attr = uc.getMainAttributes();
        if (attr != null)
            {
            return attr.getValue(Attributes.Name.MAIN_CLASS);
            }
        else
            {
            return null;
            }
        }

    public final void invokeClass(final String name, final String[] args)
        throws ClassNotFoundException,
               NoSuchMethodException,
               InvocationTargetException
        {
        final Class c = loadClass(name);
        final Method m = c.getMethod("main", new Class[]{args.getClass()});
        m.setAccessible(true);
        final int mods = m.getModifiers();
        if ((!m.getReturnType().equals(void.class))
            || !Modifier.isStatic(mods)
            || !Modifier.isPublic(mods))
            {
            throw new NoSuchMethodException("main");
            }
        try
            {
            m.invoke(null, new Object[]{args});
            }
        catch (IllegalAccessException e)
            {
            // This should not happen, as we have disabled access checks
            e.printStackTrace();
            }
        }

    }
