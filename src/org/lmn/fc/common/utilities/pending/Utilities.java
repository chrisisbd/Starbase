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

package org.lmn.fc.common.utilities.pending;

import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.util.Random;


public final class Utilities
    {
    /***********************************************************************************************
     * Convert the contents of the specified array to a <code><b>String</b></code>,
     * starting at an offset of <code>start</code>, for <code>length</code>.
     *
     * @param array
     * @param start
     * @param length
     *
     * @return String
     */

    public static String arrayToString(final byte[] array,
                                       final int start,
                                       final int length)
        {
        int i;
        int count;
        final byte[] temp;

        count = 0;
        temp = new byte[length];

        for (i = start; i < (start + length); i++)
            {
            if ((array[i] >= 0)
                && (array[i] < 32))
                {
                // Control characters are replaced by spaces
                temp[count] = 32;
                }
            else
                {
                temp[count] = array[i];
                }

            count++;
            }

        return (new String(temp));
        }


//    private static final org.apache.commons.logging.Log log =
//        org.apache.commons.logging.LogFactory.getLog(Utilities.class);



    /**
     * Creates a new Utilities object.
     */
    private Utilities()
        {
        // Private constructor to prevent instantiation
        }

    /**
     * Interpret the supplied path as a classloader resource or a directory path as appropriate.
     *
     * @param path
     * @return The absolute filesystem path to the supplied path
     */
//    public static String getFileAsPathOrUrl(final String path)
//        {
//        String retVal = path;
//
//        System.out.println("trying to find {" + path + "}");
//        // First attempt to use the classloader to find a resource
//        URL url = Utilities.class.getClassLoader().getResource(path);
//
//        if (url != null)
//            {
////            log.debug("Used classloader to get resource " + url.getPath());
//            retVal = url.getPath();
//            }
//
//        return retVal;
//        }


    /**
     * Escape all characters which have significance to regular expressions
     *
     * @param string -
     * @return -
     */
    public static String escapeRegExp(final String string)
        {
        String retVal = string;
        retVal = retVal.replaceAll("\\\\", "\\\\");
        retVal = retVal.replaceAll("\\$", "\\\\\\$");
        retVal = retVal.replaceAll("\\.", "\\\\\\.");
        retVal = retVal.replaceAll("\\*", "\\\\\\*");
        retVal = retVal.replaceAll("\\(", "\\\\\\(");
        retVal = retVal.replaceAll("\\)", "\\\\\\)");
        retVal = retVal.replaceAll("\\[", "\\\\\\[");
        retVal = retVal.replaceAll("\\]", "\\\\\\]");
        retVal = retVal.replaceAll("\\{", "\\\\\\{");
        retVal = retVal.replaceAll("\\}", "\\\\\\}");
        retVal = retVal.replaceAll("\\^", "\\\\\\^");
        retVal = retVal.replaceAll("\\?", "\\\\\\?");
        retVal = retVal.replaceAll("\\+", "\\\\\\+");
        retVal = retVal.replaceAll("\\|", "\\\\\\|");

        return retVal;
        }


    /**
     * Replace whitespace with under-scores
     *
     * @param inString String to be processed
     * @return New String with whitespace replaced by under-scores
     */
    public static String noWhitespace(final String inString)
        {
        String returnString = "";

        for (int loop = 0; loop < inString.length(); loop++)
            {
            if ((inString.charAt(loop) == ' ') || (inString.charAt(loop) == '\t'))
                {
                returnString += "_";
                }
            else
                {
                returnString += inString.charAt(loop);
                }
            }

        return returnString;
        }


    /**
     * Create a random string of the specified length containing only characters in allowedChars
     *
     * @param allowedChars Allowed characters
     * @param length       Length of the string
     * @return Random string of specified length
     */
    public static String randomString(final String allowedChars,
                                      final int length)
        {
        StringBuffer retVal = new StringBuffer();
        Random rand = new Random(new java.util.Date().getTime());

        for (int i = 0; i < length; i++)
            {
            int index = rand.nextInt(allowedChars.length());
            retVal.append(allowedChars.charAt(index));
            }

        return retVal.toString();
        }


    /**
     * Wraps the call to Thread.sleep() in a catch block to save repeating the same code block in several places
     *
     * @param periodInMs period to sleep for in milliseconds
     */
//    public static void safeSleep(final long periodInMs)
//        {
//        Date start = new Date();
//        Date now = new Date();
//
//        while((now.getTime() - start.getTime()) < periodInMs)
//            {
//            try
//                {
//                Thread.sleep(Constants.MS_PER_HALF_SECOND);
//                }
//            catch(InterruptedException e)
//                {
//                // Ignored
//                }
//
//            now = new Date();
//            }
//        }


    /**
     * Return a String representation of a <code>Properties</code> instance
     *
     * @param props
     *
     * @return String representation of a <code>Properties</code> instance
     */
//    public static String toString(final Properties props)
//        {
//        DelimitedStructure retVal = new DelimitedStructure("\n");
//        Iterator keyIterator = props.keySet().iterator();
//
//        while(keyIterator.hasNext())
//            {
//            String key = (String)keyIterator.next();
//
//            retVal.addValue(key + " = " + props.getProperty(key));
//            }
//
//        return retVal.toString();
//        }


    /**
     * Return a String representation of the supplied <code>Map</code>
     *
     * @param map
     *
     * @return String representation of the supplied <code>Map</code>
     */
//    public static String toString(final Map map)
//        {
//        DelimitedStructure retVal = new DelimitedStructure("\n");
//        Iterator keyIterator = map.keySet().iterator();
//
//        while(keyIterator.hasNext())
//            {
//            Object key = keyIterator.next();
//
//            retVal.addValue(key + " = " + map.get(key));
//            }
//
//        return retVal.toString();
//        }





    /**
     * Replace HTML escape sequences with the correct characters
     *
     * @param escapedHTML The original string
     * @return The result, after substitution
     */
    public static String unEscapeHTML(final String escapedHTML)
        {
        String retVal = escapedHTML;

        retVal = retVal.replaceAll("&gt;", ">");
        retVal = retVal.replaceAll("&lt;", "<");
        retVal = retVal.replaceAll("&amp;", "&");
        retVal = retVal.replaceAll("&nbsp;", " ");

        return retVal;
        }


    /**
     * Check if a string is null or empty
     *
     * @param string
     * @return -
     */
    public static boolean isNullOrEmpty(final String string)
        {
        return (string == null) || (string == "");
        }


    /***********************************************************************************************
     * Show the Member Modifiers.
     *
     * @param member
     *
     * @return String
     */

    public static String showModifiers(final Member member)
        {
        final int modifiers;
        final StringBuffer buffer;

        buffer = new StringBuffer();

        if (member != null)
            {
            modifiers = member.getModifiers();

            if (Modifier.isAbstract(modifiers))
                {
                buffer.append("Abstract ");
                }

            if (Modifier.isFinal(modifiers))
                {
                buffer.append("Final ");
                }

            if (Modifier.isInterface(modifiers))
                {
                buffer.append("Interface ");
                }

            if (Modifier.isNative(modifiers))
                {
                buffer.append("Native ");
                }

            if (Modifier.isPrivate(modifiers))
                {
                buffer.append("Private ");
                }

            if (Modifier.isProtected(modifiers))
                {
                buffer.append("Protected ");
                }

            if (Modifier.isPublic(modifiers))
                {
                buffer.append("Public ");
                }

            if (Modifier.isStatic(modifiers))
                {
                buffer.append("Static ");
                }

            if (Modifier.isStrict(modifiers))
                {
                buffer.append("Strict ");
                }

            if (Modifier.isSynchronized(modifiers))
                {
                buffer.append("Synchronized ");
                }

            if (Modifier.isTransient(modifiers))
                {
                buffer.append("Transient ");
                }

            if (Modifier.isVolatile(modifiers))
                {
                buffer.append("Volatile ");
                }
            }

        return (buffer.toString().trim());
        }
    }
