
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

import java.net.URL;


/***************************************************************************************************
 * An ReportIcon intended for Flags, created to retain the original filename
 * so that HTML renderers etc. can find associated files.
 */

public final class FlagIcon extends ReportIcon
    {
    private String strCountryCode;


    /***********************************************************************************************
     * Get an FlagIcon, using a URL.
     *
     * @param code
     * @param filename
     *
     * @return FlagIcon
     */

    public static FlagIcon getIcon(String code,
                                   String filename)
        {
        URL url = FlagIcon.class.getResource(RegistryModelUtilities.getCommonImagesRoot() + filename);

        if (url != null)
            {
            return (new FlagIcon(code, url));
            }
        else
            {
            return (new FlagIcon());
            }
        }


    /***********************************************************************************************
     * Construct a FlagIcon.
     */

    public FlagIcon()
        {
        super("");

        strCountryCode = "";
        }


    /***********************************************************************************************
     * Construct a FlagIcon.
     *
     * @param code
     * @param flagname
     */

    public FlagIcon(String code,
                    String flagname)
        {
        super(flagname);

        strCountryCode = code;
        }


    /***********************************************************************************************
     * Construct a FlagIcon.
     *
     * @param code
     * @param url
     */

    public FlagIcon(String code,
                    URL url)
        {
        super(url);

        strCountryCode = code;
        }


    /***********************************************************************************************
     *
     * @return String
     */

    public String getCountryCode()
        {
        return (this.strCountryCode);
        }
    }

