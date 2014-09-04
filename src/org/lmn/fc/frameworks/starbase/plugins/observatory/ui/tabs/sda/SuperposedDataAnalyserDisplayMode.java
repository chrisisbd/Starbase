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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.sda;


/***************************************************************************************************
 * SuperposedDataAnalyserDisplayMode indicates what is displayed in the CompositeViewer or DatasetViewer..
 */

public enum SuperposedDataAnalyserDisplayMode
    {
    COMPOSITE_VIEWER_CHART      (0, "CompositeChartViewer"),
    COMPOSITE_VIEWER_METADATA   (1, "CompositeMetadataViewer"),
    COMPOSITE_EXPORTER          (2, "CompositeExporter"),

    DATASET_VIEWER_CHART        (3, "DatasetChartViewer"),
    DATASET_VIEWER_METADATA     (4, "DatasetMetadataViewer"),
    DATASET_IMPORTER            (5, "DatasetImporter");


    private final int intMode;
    private final String strName;


    /***********************************************************************************************
     * SuperposedDataAnalyserDisplayMode.
     *
     * @param mode
     * @param name
     */

    private SuperposedDataAnalyserDisplayMode(final int mode,
                                              final String name)
        {
        intMode = mode;
        strName = name;
        }


    /***********************************************************************************************
     * Get the SuperposedDataAnalyserDisplayMode.
     *
     * @return int
     */

    public int getSdaDisplayMode()
        {
        return (this.intMode);
        }


    /***********************************************************************************************
     * Get the SuperposedDataAnalyserDisplayMode name.
     *
     * @return String
     */

    public String getName()
        {
        return (this.strName);
        }


    /***********************************************************************************************
     * Get the SuperposedDataAnalyserDisplayMode name.
     *
     * @return
     */

    public String toString()
        {
        return (this.strName);
        }
    }
