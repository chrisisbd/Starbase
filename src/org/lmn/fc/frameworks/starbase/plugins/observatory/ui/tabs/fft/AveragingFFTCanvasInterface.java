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


import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.DAOWrapperInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.ChartUIComponentPlugin;
import org.lmn.fc.ui.UIComponentPlugin;

import java.awt.*;


/***************************************************************************************************
 * AveragingFFTCanvasInterface.
 */

public interface AveragingFFTCanvasInterface extends UIComponentPlugin
    {
    /***********************************************************************************************
     * Set the data from the DAO finished() method.
     * Optionally refresh the UI of Data or Metadata.
     *
     * @param daowrapper
     * @param updatedata
     * @param updatemetadata
     */

    void setWrappedData(DAOWrapperInterface daowrapper,
                        boolean updatedata,
                        boolean updatemetadata);


    /***********************************************************************************************
     * Get the Chart Viewer.
     *
     * @return ChartUIComponentPlugin
     */

    ChartUIComponentPlugin getChartViewer();


    /***********************************************************************************************
     * Get the Printable Component of the Canvas.
     *
     * @return Component
     */

    Component getPrintableComponent();
    }