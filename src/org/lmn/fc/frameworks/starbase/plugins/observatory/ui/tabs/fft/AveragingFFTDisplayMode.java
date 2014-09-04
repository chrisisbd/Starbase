// Copyright 2000, 2001, 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009,
//           2010, 2011, 2012, 2013, 2014
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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.fft;


/***************************************************************************************************
 * AveragingFFTDisplayMode indicates what is displayed in the FFT Frame.
 */

public enum AveragingFFTDisplayMode
    {
    FFT_VIEWER_CHART    (0, "FFTChartViewer"),
    FFT_VIEWER_METADATA (1, "FFTMetadataViewer"),
    FFT_VIEWER_HELP     (2, "FFTHelpViewer");


    private final int intMode;
    private final String strName;


    /***********************************************************************************************
     * AveragingFFTDisplayMode.
     *
     * @param mode
     * @param name
     */

    private AveragingFFTDisplayMode(final int mode,
                                    final String name)
        {
        intMode = mode;
        strName = name;
        }


    /***********************************************************************************************
     * Get the AveragingFFTDisplayMode.
     *
     * @return int
     */

    public int getFftDisplayMode()
        {
        return (this.intMode);
        }


    /***********************************************************************************************
     * Get the AveragingFFTDisplayMode name.
     *
     * @return String
     */

    public String getName()
        {
        return (this.strName);
        }


    /***********************************************************************************************
     * Get the AveragingFFTDisplayMode name.
     *
     * @return String
     */

    public String toString()
        {
        return (this.strName);
        }
    }
