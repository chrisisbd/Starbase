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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.serial;


import com.mxgraph.io.mxCodecRegistry;
import com.mxgraph.io.mxObjectCodec;
import com.mxgraph.model.mxICell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryUIInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.SerialConfigurationIndicatorInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.SerialConfigurationUIComponentInterface;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.ui.components.UIComponent;

import java.awt.*;
import java.util.Hashtable;


/***********************************************************************************************
 * A UIComponent to show the SerialConfiguration, using JGraphX.
 *
 * See: http://www.vainolo.com/2011/04/11/another-day-with-jgraph-styles-and-constrained-children/
 */

public final class SerialConfigurationUIComponent extends UIComponent
                                                  implements SerialConfigurationUIComponentInterface
    {
    private static final long serialVersionUID = -2065831140983759773L;

    // Injections
    private final TaskPlugin pluginTask;
    private final ObservatoryUIInterface observatoryUI;
    private final ObservatoryInstrumentInterface hostInstrument;
    private final FontInterface pluginFont;
    private final ColourInterface pluginColour;
    private final String strResourceKey;

    // Graph State
    private mxGraph graphMx;
    private mxGraphComponent graphComponent;
    private Object objParent;
    private final Hashtable<String, mxICell> hashtableSerialPorts;
    private mxICell cellUnknownPort;
    private final Hashtable<String, mxICell> hashtableStaribusInstruments;
    private mxICell cellStaribusHub;
    private final Hashtable<String, mxICell> hashtableSerialInstruments;

    // UI
    private SerialConfigurationIndicatorInterface uiConfigIndicator;


    /***********************************************************************************************
     * Construct a SerialConfigurationUIComponent.
     *
     * @param task
     * @param hostui
     * @param hostinstrument
     * @param font
     * @param colour
     * @param resourcekey
     * @param debug
     */

    public SerialConfigurationUIComponent(final TaskPlugin task,
                                          final ObservatoryUIInterface hostui,
                                          final ObservatoryInstrumentInterface hostinstrument,
                                          final FontInterface font,
                                          final ColourInterface colour,
                                          final String resourcekey,
                                          final boolean debug)
        {
        // UIComponent has a BorderLayout
        super();

        // Injections
        this.pluginTask = task;
        this.observatoryUI = hostui;
        this.hostInstrument = hostinstrument;
        this.pluginFont = font;
        this.pluginColour = colour;
        this.strResourceKey = resourcekey;
        setDebug(debug);

        // Graph State
        this.graphMx = null;
        this.graphComponent = null;
        this.objParent = null;
        this.hashtableSerialPorts = new Hashtable<String, mxICell>(10);
        this.cellUnknownPort = null;
        this.hashtableStaribusInstruments = new Hashtable<String, mxICell>(10);
        this.cellStaribusHub = null;
        this.hashtableSerialInstruments = new Hashtable<String, mxICell>(10);

        // UI
        this.uiConfigIndicator = null;
        }


    /***********************************************************************************************
     /* UI State                                                                                  */
    /***********************************************************************************************
     * Initialise this UIComponent.
     */

    public final void initialiseUI()
        {
        final String SOURCE = "SerialConfigurationUIComponent.initialiseUI() ";

        LOGGER.debug(isDebug(), SOURCE);

        removeAll();

        // Reset Graph state
        setGraph(null);
        setGraphComponent(null);
        setGraphParent(null);
        getSerialPortCells().clear();
        setUnknownPortCell(null);
        getStaribusInstrumentCells().clear();
        setStaribusHubCell(null);
        getSerialInstrumentCells().clear();

        setConfigIndicator(new SerialConfigurationIndicator(getHostInstrument(),
                                                            this,
                                                            getFontData(),
                                                            getColourData(),
                                                            DEFAULT_COLOUR_TAB_BACKGROUND,
                                                            isDebug()));
        setGraph(new SerialConfigurationGraph(isDebug()));
        SerialConfigurationHelper.configureGraph(getGraph());
        setGraphParent(getGraph().getDefaultParent());

        setGraphComponent(new SerialConfigurationGraphComponent(this,
                                                                getGraph(),
                                                                isDebug()));

        SerialConfigurationHelper.configureGraphComponent(this,
                                                          getGraph(),
                                                          getGraphComponent(),
                                                          isDebug());

        // When using Java objects as user objects, make sure to add the package name containing the class,
        // and register a codec for the user object class as follows:
        //
        // mxCodecRegistry.addPackage("com.example");
        // mxCodecRegistry.register(new mxObjectCodec(new com.example.CustomUserObject()));
        //
        // Note that the object must have an empty constructor and a setter and
        // getter for each property to be persisted. The object must not have
        // a property called ID as this property is reserved for resolving cell
        // references and will cause problems when used inside the user object.

        mxCodecRegistry.addPackage("org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.serial");
        mxCodecRegistry.register(new mxObjectCodec(new SerialConfigurationCellData()));

        // The host UIComponent uses BorderLayout
        // Put the components together
        add((Component) getConfigIndicator(), BorderLayout.NORTH);
        add(getGraphComponent(), BorderLayout.CENTER);
        }


    /***********************************************************************************************
     * Run this UIComponent.
     */

    public void runUI()
        {
        final String SOURCE = "SerialConfigurationUIComponent.runUI() ";

        LOGGER.debug(isDebug(), SOURCE);

        super.runUI();

        SerialConfigurationHelper.rebuildGraph(this,
                                               isDebug());

        if (getGraphComponent() != null)
            {
            getGraphComponent().clearCellOverlays();
            getGraphComponent().validateGraph();
            }
        }


    /***********************************************************************************************
     * Dispose of all components of this UIComponent.
     */

    public void disposeUI()
        {
        final String SOURCE = "SerialConfigurationUIComponent.disposeUI() ";

        LOGGER.debug(isDebug(), SOURCE);

        // Reset Graph state
        setGraph(null);
        setGraphComponent(null);
        setGraphParent(null);
        getSerialPortCells().clear();
        setUnknownPortCell(null);
        getStaribusInstrumentCells().clear();
        setStaribusHubCell(null);
        getSerialInstrumentCells().clear();

        super.disposeUI();
        }


    /**********************************************************************************************/
    /* Graph State                                                                                */
    /***********************************************************************************************
     * Get the Graph.
     *
     * @return mxGraph
     */

    public mxGraph getGraph()
        {
        return (this.graphMx);
        }


    /***********************************************************************************************
     * Set the Graph.
     *
     * @param graph
     */

    private void setGraph(final mxGraph graph)
        {
        this.graphMx = graph;
        }


    /***********************************************************************************************
     * Get the GraphComponent.
     *
     * @return mxGraphComponent
     */

    public mxGraphComponent getGraphComponent()
        {
        return (this.graphComponent);
        }


    /***********************************************************************************************
     * Set the GraphComponent.
     *
     * @param graphcomponent
     */

    private void setGraphComponent(final mxGraphComponent graphcomponent)
        {
        this.graphComponent = graphcomponent;
        }


    /***********************************************************************************************
     * Get the Graph Parent.
     *
     * @return Object
     */

    public Object getGraphParent()
        {
        return (this.objParent);
        }


    /***********************************************************************************************
     * Set the Graph Parent.
     *
     * @param parent
     */

    private void setGraphParent(final Object parent)
        {
        this.objParent = parent;
        }


    /***********************************************************************************************
     * Get the SerialPort Cells.
     *
     * @return Hashtable<String, mxICell>
     */

    public Hashtable<String, mxICell> getSerialPortCells()
        {
        return (this.hashtableSerialPorts);
        }


    /***********************************************************************************************
     * Set the Cell for the Unknown Port.
     *
     * @param cell
     */

    public void setUnknownPortCell(final mxICell cell)
        {
        this.cellUnknownPort = cell;
        }


    /***********************************************************************************************
     * Get the Cell for the Unknown Port.
     *
     * @return mxICell
     */

    public mxICell getUnknownPortCell()
        {
        return (this.cellUnknownPort);
        }


    /***********************************************************************************************
     * Get the StaribusInstrument Cells.
     *
     * @return Hashtable<String, mxICell>
     */

    public Hashtable<String, mxICell> getStaribusInstrumentCells()
        {
        return (this.hashtableStaribusInstruments);
        }


    /***********************************************************************************************
     * Set the Cell for the StaribusHub.
     *
     * @param cell
     */

    public void setStaribusHubCell(final mxICell cell)
        {
        this.cellStaribusHub = cell;
        }


    /***********************************************************************************************
     * Get the Cell for the StaribusHub.
     *
     * @return mxICell
     */

    public mxICell getStaribusHubCell()
        {
        return (this.cellStaribusHub);
        }


    /***********************************************************************************************
     * Get the SerialInstrument Cells.
     *
     * @return Hashtable<String, mxICell>
     */

    public Hashtable<String, mxICell> getSerialInstrumentCells()
        {
        return (this.hashtableSerialInstruments);
        }


    /**********************************************************************************************/
    /* UI                                                                                         */
    /***********************************************************************************************
     * Get the Port Config Indicator.
     *
     * @return SerialConfigurationIndicatorInterface
     */

    public SerialConfigurationIndicatorInterface getConfigIndicator()
        {
        return (this.uiConfigIndicator);
        }


    /***********************************************************************************************
     * Set the Port Config Indicator.
     *
     * @param toolbar
     */

    private void setConfigIndicator(final SerialConfigurationIndicatorInterface toolbar)
        {
        this.uiConfigIndicator = toolbar;
        }


    /**********************************************************************************************/
    /* Injections                                                                                 */
    /***********************************************************************************************
     * Get the Task on which this Report is based.
     *
     * @return TaskData
     */

    private TaskPlugin getTask()
        {
        return (this.pluginTask);
        }


    /***********************************************************************************************
     * Get the host ObservatoryUI.
     *
     * @return ObservatoryUIInterface
     */

    public ObservatoryUIInterface getObservatoryUI()
        {
        return (this.observatoryUI);
        }


    /***********************************************************************************************
     * Get the ObservatoryInstrument to which this UIComponent is attached.
     *
     * @return ObservatoryInstrumentInterface
     */

    private ObservatoryInstrumentInterface getHostInstrument()
        {
        return (this.hostInstrument);
        }


    /***********************************************************************************************
     * Get the FontDataType.
     *
     * @return FontPlugin
     */

    private FontInterface getFontData()
        {
        return (this.pluginFont);
        }


    /***********************************************************************************************
     * Get the ColourDataType.
     *
     * @return ColourPlugin
     */

    private ColourInterface getColourData()
        {
        return (this.pluginColour);
        }


    /***********************************************************************************************
     * Get the ResourceKey for the Report.
     *
     * @return String
     */

    private String getResourceKey()
        {
        return (this.strResourceKey);
        }
    }