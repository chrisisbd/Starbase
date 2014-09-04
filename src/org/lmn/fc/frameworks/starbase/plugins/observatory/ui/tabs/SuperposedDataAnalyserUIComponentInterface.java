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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs;

import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.InstrumentUIComponentDecoratorInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.sda.SuperposedDataAnalyserDisplayMode;
import org.lmn.fc.ui.UIComponentPlugin;

import javax.swing.*;


/***************************************************************************************************
 * SuperposedDataAnalyserUIComponentInterface.
 */

public interface SuperposedDataAnalyserUIComponentInterface extends InstrumentUIComponentDecoratorInterface
    {
    // String Resources
    String TITLE_COMPOSITE = "Superposed Data Analyser";
    String TITLE_DATASET = "Imported Dataset";
    String MSG_INCORRECT_DAO = "The Instrument primary DAO is incompatible with this function";
    String NO_SELECTION_MSG = "No Selection";

    // Starscript
    // Neither of these DAOs supplies data to the host Instrument
    String CLASSNAME_DAO_COMPOSITE   = "org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.dao.BasicInstrumentChildDAO";
    String CLASSNAME_DAO_IMPORT   = "org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.dao.BasicInstrumentChildDAO";

    int DIVIDER_SIZE = 15;
    double DIVIDER_LOCATION = 0.5;
    double RESIZE_WEIGHT = 0.5;
    int BOTTOM_MINIMUM_HEIGHT = 93;
    double DEFAULT_COMPOSITE_RANGE = 1000.0;


    /***********************************************************************************************
     * Get the DatasetManager, which handles the state of all imported Datasets.
     *
     * @return DatasetManagerInterface
     */

    DatasetManagerInterface getDatasetManager();


    /***********************************************************************************************
     * Get the Composite DAO.
     *
     * @return ObservatoryInstrumentDAOInterface
     */

    ObservatoryInstrumentDAOInterface getCompositeDAO();


    /**********************************************************************************************/
    /* Top Panel                                                                                  */
    /***********************************************************************************************
     * Get the Top Panel.
     *
     * @return JPanel
     */

    JPanel getTopPanel();


    /***********************************************************************************************
     * Get the UIComponent, which appears at the top of the UI.
     *
     * @return UIComponentPlugin
     */

    UIComponentPlugin getTopUIComponent();


    /***********************************************************************************************
     * Set the UIComponent, which appears at the top of the UI.
     *
     * @param uicomponent
     */

    void setTopUIComponent(UIComponentPlugin uicomponent);


    /***********************************************************************************************
     * Get the Composite Viewer, which appears at the top of the UI.
     *
     * @return CompositeViewerUIComponentInterface
     */

    CompositeViewerUIComponentInterface getCompositeViewer();


    /***********************************************************************************************
     * Get the CompositeExporter, which appears at the top of the UI.
     *
     * @return ExecuteCommandUIComponentInterface
     */

    ExecuteCommandUIComponentInterface getCompositeExporter();


    /***********************************************************************************************
     * Get the SDA CompositeViewer Display Mode.
     *
     * @return SuperposedDataAnalyserDisplayMode
     */

    SuperposedDataAnalyserDisplayMode getCompositeViewerMode();


    /***********************************************************************************************
     * Set the SDA CompositeViewer Display Mode.
     *
     * @param mode
     */

    void setCompositeViewerMode(SuperposedDataAnalyserDisplayMode mode);


    /**********************************************************************************************/
    /* Bottom Panel                                                                               */
    /***********************************************************************************************
     * Get the Bottom Panel.
     *
     * @return JPanel
     */

    JPanel getBottomPanel();


    /***********************************************************************************************
     * Get the UIComponent, which appears at the bottom of the UI.
     *
     * @return UIComponentPlugin
     */

    UIComponentPlugin getBottomUIComponent();


    /***********************************************************************************************
     * Set the UIComponent, which appears at the bottom of the UI.
     *
     * @param uicomponent
     */

    void setBottomUIComponent(UIComponentPlugin uicomponent);


    /***********************************************************************************************
     * Get the DatasetViewer, which appears at the bottom of the UI.
     *
     * @return DatasetViewerUIComponentInterface
     */

    DatasetViewerUIComponentInterface getDatasetViewer();


    /***********************************************************************************************
     * Get the DatasetImporter, which appears at the bottom of the UI.
     *
     * @return ExecuteCommandUIComponentInterface
     */

    ExecuteCommandUIComponentInterface getDatasetImporter();


    /***********************************************************************************************
     * Get the SDA DatasetViewer Display Mode.
     *
     * @return SuperposedDataAnalyserDisplayMode
     */

    SuperposedDataAnalyserDisplayMode getDatasetViewerMode();


    /***********************************************************************************************
     * Set the SDA DatasetViewer Display Mode.
     *
     * @param mode
     */

    void setDatasetViewerMode(SuperposedDataAnalyserDisplayMode mode);
    }
