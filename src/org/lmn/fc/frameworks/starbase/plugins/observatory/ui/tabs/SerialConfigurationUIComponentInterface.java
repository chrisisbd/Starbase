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

import com.mxgraph.model.mxICell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryUIInterface;
import org.lmn.fc.ui.UIComponentPlugin;

import java.util.Hashtable;


/***************************************************************************************************
 * SerialConfigurationUIComponentInterface.
 */

public interface SerialConfigurationUIComponentInterface extends UIComponentPlugin
    {
    // String Resources
    String TITLE_STARIBUS_PORT = "Staribus Port";
    String TITLE_PROTOCOL_STARIBUS = "RS485";
    String TITLE_PROTOCOL_SERIAL = "RS232";
    String TITLE_INVALID_STARIBUS_PORT = "Invalid Staribus Port";
    String TITLE_INVALID_SERIAL_PORT = "Invalid Serial Port";
    String MSG_STARIBUS_PORT_0 = "The Staribus Port is missing or incorrectly configured.";
    String MSG_STARIBUS_PORT_1 = "Please check the file StaribusPort-properties.xml";


    /**********************************************************************************************/
    /* Graph State                                                                                */
    /***********************************************************************************************
     * Get the Graph.
     *
     * @return mxGraph
     */

    mxGraph getGraph();


    /***********************************************************************************************
     * Get the GraphComponent.
     *
     * @return mxGraphComponent
     */

    mxGraphComponent getGraphComponent();


    /***********************************************************************************************
     * Get the Graph Parent.
     *
     * @return Object
     */

    Object getGraphParent();


    /***********************************************************************************************
     * Get the SerialPort Cells.
     *
     * @return Hashtable<String, mxICell>
     */

    Hashtable<String, mxICell> getSerialPortCells();


    /***********************************************************************************************
     * Set the Cell for the Unknown Port.
     *
     * @param cell
     */

    void setUnknownPortCell(mxICell cell);


    /***********************************************************************************************
     * Get the Cell for the Unknown Port.
     *
     * @return mxICell
     */

    mxICell getUnknownPortCell();


    /***********************************************************************************************
     * Get the StaribusInstrument Cells.
     *
     * @return Hashtable<String, mxICell>
     */

    Hashtable<String, mxICell> getStaribusInstrumentCells();


    /***********************************************************************************************
     * Set the Cell for the StaribusHub.
     *
     * @param cell
     */

    void setStaribusHubCell(mxICell cell);


    /***********************************************************************************************
     * Get the Cell for the StaribusHub.
     *
     * @return mxICell
     */

    mxICell getStaribusHubCell();


    /***********************************************************************************************
     * Get the SerialInstrument Cells.
     *
     * @return Hashtable<String, mxICell>
     */

    Hashtable<String, mxICell> getSerialInstrumentCells();


    /**********************************************************************************************/
    /* UI                                                                                         */
    /***********************************************************************************************
     * Get the Port Config Indicator.
     *
     * @return SerialConfigurationIndicatorInterface
     */

    SerialConfigurationIndicatorInterface getConfigIndicator();


    /**********************************************************************************************/
    /* Injections                                                                                 */
    /***********************************************************************************************
     * Get the host ObservatoryUI.
     *
     * @return ObservatoryUIInterface
     */

    ObservatoryUIInterface getObservatoryUI();
    }
