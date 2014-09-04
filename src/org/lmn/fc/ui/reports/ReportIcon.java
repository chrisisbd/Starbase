
// Copyright 2000, 2001, 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013
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

package org.lmn.fc.ui.reports;

import org.lmn.fc.model.registry.RegistryModelUtilities;

import javax.swing.*;
import java.net.URL;


/***************************************************************************************************
 * An ImageIcon intended for Report Icons, created to retain the original filename
 * so that HTML renderers etc. can find associated files.
 */

public class ReportIcon extends ImageIcon
    {
    private String strFilename;


    /***********************************************************************************************
     * Get an ReportIcon, using a filename relative to ImagesRoot.
     *
     * @param filename
     *
     * @return ReportIcon
     */

    public static ReportIcon getIcon(final String filename)
        {
        final URL url;

        url = ReportIcon.class.getResource(RegistryModelUtilities.getCommonImagesRoot() + filename);

        if (url != null)
            {
            return (new ReportIcon(url));
            }
        else
            {
            return (new ReportIcon());
            }
        }


    /***********************************************************************************************
     * Construct a ReportIcon.
     */

    public ReportIcon()
        {
        super();

        strFilename = "";
        }


    /***********************************************************************************************
     * Construct a ReportIcon.
     *
     * @param filename
     */

    public ReportIcon(final String filename)
        {
        super(filename);

        strFilename = filename;
        }


    /***********************************************************************************************
     * Construct a ReportIcon.
     *
     * @param url
     */

    public ReportIcon(final URL url)
        {
        super(url);

        // TODo read file part of URL
        strFilename = "";
        }


    /***********************************************************************************************
     *
     * @return String
     */

    public final String getFilename()
        {
        return (strFilename);
        }
    }

