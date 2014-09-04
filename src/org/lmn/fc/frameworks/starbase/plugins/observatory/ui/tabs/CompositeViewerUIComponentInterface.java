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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs;

import org.lmn.fc.frameworks.starbase.plugins.observatory.events.MetadataChangedListener;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.ui.UIComponentPlugin;

import javax.swing.*;


/***************************************************************************************************
 * DatasetViewerUIComponentInterface.
 */

public interface CompositeViewerUIComponentInterface extends UIComponentPlugin,
                                                             MetadataChangedListener
    {
    String TITLE_COMPOSITE = "Superposed Data Analyser";

    String MSG_PRINT_COMPOSITE = "Composite";

    String TOOLTIP_CHANNELS = "The number of data Channels available to the Composite chart";
    String TOOLTIP_DETACH_ALL = "Detach all Datasets from Composite";
    String TOOLTIP_CHART = "Show the Composite Chart";
    String TOOLTIP_METADATA = "Show the Metadata for the Composite Dataset";
    String TOOLTIP_COMPOSITE_EXPORTER = "Export the Composite Chart";


    /**********************************************************************************************/
    /* UI Components                                                                              */
    /***********************************************************************************************
     * Get the JLabel which indicates the number of Channels.
     *
     * @return JLabel
     */

    JLabel getChannelsLabel();


    /***********************************************************************************************
     * Set the JLabel which indicates the number of Channels.
     *
     * @param label
     */

    void setChannelsLabel(JLabel label);


    /***********************************************************************************************
     * Get the Detach All button.
     *
     * @return JButton
     */

    JButton getDetachAllButton();


    /***********************************************************************************************
     * Set the Detach All button.
     *
     * @param button
     */

    void setDetachAllButton(JButton button);


    /***********************************************************************************************
     * Get the Metadata button.
     *
     * @return JButton
     */

    JButton getMetadataButton();


    /***********************************************************************************************
     * Set the Metadata button.
     *
     * @param button
     */

    void setMetadataButton(JButton button);


    /***********************************************************************************************
     * Get the Export button.
     *
     * @return JButton
     */

    JButton getExportButton();


    /***********************************************************************************************
     * Set the Export button.
     *
     * @param button
     */

    void setExportButton(JButton button);


    /***********************************************************************************************
     * Get the PageSetup button.
     *
     * @return JButton
     */

    JButton getPageSetupButton();


    /***********************************************************************************************
     * Set the PageSetup button.
     *
     * @param button
     */

    void setPageSetupButton(JButton button);


    /***********************************************************************************************
     * Get the Print button.
     *
     * @return JButton
     */

    JButton getPrintButton();


    /***********************************************************************************************
     * Set the Print button.
     *
     * @param button
     */

    void setPrintButton(JButton button);


    /**********************************************************************************************/
    /* Viewers                                                                                    */
    /***********************************************************************************************
     * Get the Viewer UI container.
     *
     * @return UIComponentPlugin
     */

    UIComponentPlugin getViewerContainer();


    /***********************************************************************************************
     * Get the  Chart Viewer.
     *
     * @return ChartUIComponentPlugin
     */

    ChartUIComponentPlugin getChartViewer();


    /***********************************************************************************************
     * Get the Metadata Viewer, which appears at the bottom of the UI.
     *
     * @return UIComponentPlugin
     */

    UIComponentPlugin getMetadataViewer();


    /***********************************************************************************************
     * Indicate if all of the Viewer UI Components are not NULL.
     *
     * @return boolean
     */

    boolean isValidViewerUI();


    /**********************************************************************************************/
    /* Injections                                                                                 */
    /***********************************************************************************************
     * Get the host ObservatoryInstrument.
     *
     * @return ObservatoryInstrumentInterface
     */

    ObservatoryInstrumentInterface getObservatoryInstrument();


    /***********************************************************************************************
     * Get the host SuperposedDataAnalyser UIComponent.
     *
     * @return SuperposedDataAnalyserUIComponentInterface
     */

    SuperposedDataAnalyserUIComponentInterface getSdaUI();


    /***********************************************************************************************
     * Get the FontData.
     *
     * @return FontPlugin
     */

    FontInterface getFontData();


    /***********************************************************************************************
     * Get the ColourData.
     *
     * @return ColourPlugin
     */

    ColourInterface getColourData();
    }
