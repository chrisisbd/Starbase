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


import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.InstrumentUIComponentDecoratorInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.AveragingFFTUIComponentInterface;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;
import org.lmn.fc.ui.UIComponentPlugin;

import java.util.List;


/***************************************************************************************************
 * AveragingFFTFrameUIComponentInterface.
 */

public interface AveragingFFTFrameUIComponentInterface extends InstrumentUIComponentDecoratorInterface
    {
    // This DAO does not supply data to the host Instrument, only to this Frame
    String CLASSNAME_DAO_FFT   = "org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.fft.dao.AveragingFFTDAO";


    /**********************************************************************************************/
    /* DAO                                                                                        */
    /***********************************************************************************************
     * Get the DAO.
     *
     * @return AveragingFFTDAOInterface
     */

    AveragingFFTDAOInterface getDAO();


    /**********************************************************************************************/
    /* User Interface                                                                             */
    /***********************************************************************************************
     * Get the ToolBar.
     *
     * @return AveragingFFTToolbarInterface
     */

    AveragingFFTToolbarInterface getToolbar();


    /***********************************************************************************************
     * Get the Viewer UI container.
     *
     * @return UIComponentPlugin
     */

    UIComponentPlugin getViewerContainer();


    /***********************************************************************************************
     * Get the FFT Viewer.
     *
     * @return AveragingFFTUIComponentInterface
     */

    AveragingFFTUIComponentInterface getFFTViewer();


    /***********************************************************************************************
     * Get the Metadata Viewer.
     *
     * @return UIComponentPlugin
     */

    UIComponentPlugin getMetadataViewer();


    /***********************************************************************************************
     * Get the Help Viewer.
     *
     * @return UIComponentPlugin
     */

    UIComponentPlugin getHelpViewer();


    /***********************************************************************************************
     * Get the FFT Viewer Display Mode.
     *
     * @return AveragingFFTDisplayMode
     */

    AveragingFFTDisplayMode getViewerMode();


    /***********************************************************************************************
     * Set the FFT Viewer Display Mode.
     *
     * @param mode
     */

    void setViewerMode(AveragingFFTDisplayMode mode);


    /***********************************************************************************************
     * Get the Metadata associated with this UIComponent.
     *
     * @return List<Metadata>
     */

    List<Metadata> getMetadataList();
    }
