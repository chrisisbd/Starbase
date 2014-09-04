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

package org.lmn.fc.common.xml;

import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.common.constants.ResourceKeys;
import org.lmn.fc.model.logging.Logger;

import java.util.ArrayList;
import java.util.List;


public final class XmlBeansUtilities implements FrameworkConstants,
                                                FrameworkStrings,
                                                FrameworkMetadata,
                                                ResourceKeys
    {
    private static final Logger LOGGER = Logger.getInstance();


    /***********************************************************************************************
     * Validate some XML, and log error messages if any.
     * If not enabled, always return TRUE.
     *
     * @param xml
     * @param enabled
     *
     * @return boolean
     */

    public static boolean isValidXml(final XmlObject xml,
                                     final boolean enabled)
        {
        if (enabled)
            {
            return (isValidXml(xml));
            }
        else
            {
            return (true);
            }
        }


    /***********************************************************************************************
     * Validate some XML, and log error messages if any.
     * Beware that this can take a long time to run on large XML structures.
     *
     * @param xml
     *
     * @return boolean
     *
     * @throws IllegalArgumentException
     */

    public static boolean isValidXml(final XmlObject xml) throws IllegalArgumentException
        {
        if (xml == null)
            {
            throw new IllegalArgumentException(EXCEPTION_PARAMETER_NULL);
            }

        final XmlOptions xmlOptions;
        final ArrayList listErrors;
        final boolean boolValid;

        xmlOptions = new XmlOptions();
        listErrors = new ArrayList(10);
        xmlOptions.setErrorListener(listErrors);
        boolValid = xml.validate(xmlOptions);

        if (!boolValid)
            {
            for (int i = 0; i < listErrors.size(); i++)
                {
                final XmlError xmlError = (XmlError)listErrors.get(i);

                LOGGER.error("XML Error=["  + xmlError.getMessage() + "]");
                LOGGER.error("Invalid XML at cursor=["  + xmlError.getCursorLocation().xmlText() + "]");
                LOGGER.error("Invalid XML=\n["  + xml.toString() + "]");
                }
            }

        return (boolValid);
        }


    /***********************************************************************************************
     * Validate some XML, adding error messages if any to the specified List.
     *
     * @param xml
     * @param enabled
     * @param errors
     *
     * @return boolean
     */

    public static boolean isValidXml(final XmlObject xml,
                                     final boolean enabled,
                                     final List<String> errors)
        {
        if ((xml == null)
            || (errors == null))
            {
            throw new IllegalArgumentException(EXCEPTION_PARAMETER_NULL);
            }

        final XmlOptions xmlOptions;
        final ArrayList listErrors;
        final boolean boolValid;

        xmlOptions = new XmlOptions();
        listErrors = new ArrayList(10);
        xmlOptions.setErrorListener(listErrors);
        boolValid = xml.validate(xmlOptions);

        if (!boolValid)
            {
            for (int i = 0; i < listErrors.size(); i++)
                {
                final XmlError xmlError = (XmlError)listErrors.get(i);
                errors.add(xmlError.getMessage());

                LOGGER.debug("XML Error=["  + xmlError.getMessage() + "]");
                LOGGER.debug("Invalid XML=["  + xmlError.getCursorLocation().xmlText() + "]");
                }
            }

        return (boolValid);
        }
    }
