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


import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxICell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxUtils;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxStylesheet;
import gnu.io.CommPortIdentifier;
import org.lmn.fc.common.constants.*;
import org.lmn.fc.common.utilities.files.ClassPathLoader;
import org.lmn.fc.common.utilities.misc.Utilities;
import org.lmn.fc.frameworks.starbase.plugins.observatory.common.ObservatoryConstants;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.InstrumentHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.SerialConfigurationCellDataInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.SerialConfigurationIndicatorInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.SerialConfigurationUIComponentInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.PortControllerInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.impl.PortController;
import org.lmn.fc.frameworks.starbase.portcontroller.impl.streams.StreamType;
import org.lmn.fc.model.registry.RegistryModelPlugin;
import org.lmn.fc.model.resources.PropertyPlugin;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;
import org.lmn.fc.ui.UIComponentPlugin;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;
import java.util.List;


/***************************************************************************************************
 * SerialConfigurationHelper.
 */

public final class SerialConfigurationHelper implements FrameworkConstants,
                                                        FrameworkStrings,
                                                        FrameworkMetadata,
                                                        FrameworkSingletons,
                                                        FrameworkRegex,
                                                        ResourceKeys,
                                                        ObservatoryConstants
    {
    // This cannot be in FrameworkSingletons because it won't be loaded at the right time...
    private static final PortControllerInterface PORT_CONTROLLER = PortController.getInstance();

    // String Resources
    public static final String PORT_COMMON_ID = "Observatory";
    private static final String STYLE_ASSIGNMENT = "assignment";

    private static final int ORIGIN_X = 25;
    private static final int ORIGIN_Y = 25;
    private static final int WIDTH_SEPARATOR_X = 100;
    private static final int HEIGHT_SEPARATOR_Y = 20;


    /**********************************************************************************************/
    /* Graph State                                                                                */
    /***********************************************************************************************
     * Configure general graph settings.
     *
     * @param graph the graph to configure.
     */

    public static void configureGraph(final mxGraph graph)
        {
        final String SOURCE = "SerialConfigurationHelper.configureGraph() ";
        final mxStylesheet stylesheet;

        graph.setEnabled(false);
        graph.setGridEnabled(true);
        graph.setCellsResizable(false);
        graph.setConstrainChildren(true);
        graph.setExtendParents(true);
        graph.setExtendParentsOnAdd(true);
        graph.setDefaultOverlap(0);
        graph.setCellsEditable(false);

        // Specifies if multiple edges in the same direction between
        // the same pair of vertices are allowed. Default is true.
        graph.setMultigraph(false);

        // Disable dropping on edges
        graph.setSplitEnabled(false);

        // Do not allow Edges to remain disconnected
        graph.setAllowDanglingEdges(false);

        graph.addPropertyChangeListener(new PropertyChangeListener()
            {
            public void propertyChange(final PropertyChangeEvent event)
                {
                if (event != null)
                    {
                    System.out.println("mxGraph PropertyChangeEvent [name=" + event.getPropertyName()
                                       + "] [ value.old=" + event.getOldValue()
                                       + "] [ value.new=" + event.getNewValue()
                                       + "]");
                    }
                }
            });

        stylesheet = graph.getStylesheet();

        // Instruments
        stylesheet.putCellStyle(SerialConfigurationNodeType.STARIBUS_INSTRUMENT.getName(),
                                SerialConfigurationNodeType.STARIBUS_INSTRUMENT.getStyleMap());
        stylesheet.putCellStyle(SerialConfigurationNodeType.SERIAL_INSTRUMENT.getName(),
                                SerialConfigurationNodeType.SERIAL_INSTRUMENT.getStyleMap());

        // Ports
        stylesheet.putCellStyle(SerialConfigurationNodeType.STARIBUS_HUB.getName(),
                                SerialConfigurationNodeType.STARIBUS_HUB.getStyleMap());
        stylesheet.putCellStyle(SerialConfigurationNodeType.SERIAL_PORT.getName(),
                                SerialConfigurationNodeType.SERIAL_PORT.getStyleMap());
        stylesheet.putCellStyle(SerialConfigurationNodeType.UNKNOWN_PORT.getName(),
                                SerialConfigurationNodeType.UNKNOWN_PORT.getStyleMap());

        // Assignments
        stylesheet.putCellStyle(STYLE_ASSIGNMENT, createAssignmentStyle());
        }


    /**********************************************************************************************
     * Configure general graph component settings.
     *
     * @param scui
     * @param graph
     * @param graphcomponent
     * @param debug
     */

    public static void configureGraphComponent(final SerialConfigurationUIComponentInterface scui,
                                               final mxGraph graph,
                                               final mxGraphComponent graphcomponent,
                                               final boolean debug)
        {
        final String SOURCE = "SerialConfigurationHelper.configureGraphComponent() ";

        graphcomponent.getViewport().setOpaque(true);
        graphcomponent.getViewport().setBackground(UIComponentPlugin.DEFAULT_COLOUR_TAB_BACKGROUND.getColor());

        // Do not allow creation of Edges
        graphcomponent.setConnectable(false);

        graphcomponent.setToolTips(true);
        graphcomponent.setGridVisible(true);
        graphcomponent.setGridColor(UIComponentPlugin.DEFAULT_COLOUR_GRADIENT_BOTTOM.getColor());

        // Increase mousewheel scrollspeed
        graphcomponent.getHorizontalScrollBar().setUnitIncrement(32);
        graphcomponent.getVerticalScrollBar().setUnitIncrement(32);

        // Initial validation
        graphcomponent.clearCellOverlays();
        graphcomponent.validateGraph();

        graphcomponent.getGraphControl().addMouseListener(new MouseAdapter()
            {
            public void mouseReleased(final MouseEvent event)
                {
                final SerialConfigurationIndicatorInterface indicator;
                final Object cell;

                LOGGER.debug(debug, SOURCE + "mouseReleased() START");

                indicator = scui.getConfigIndicator();
                cell = graphcomponent.getCellAt(event.getX(), event.getY());

                if (cell != null)
                    {
                    if ((cell instanceof mxICell)
                        && (((mxICell)cell).getValue() instanceof SerialConfigurationCellDataInterface))
                        {
                        final mxICell mxiCellClicked;

                        mxiCellClicked = (mxICell)cell;

                        if (SerialConfigurationNodeType.SERIAL_PORT.equals(((SerialConfigurationCellDataInterface)mxiCellClicked.getValue()).getNodeType()))
                            {
                            // getSource() and getTarget() are not in the Interface!!
                            if ((mxiCellClicked.isVertex())
                                && (mxiCellClicked.getEdgeCount() == 1)
                                && (((mxCell) mxiCellClicked.getEdgeAt(0)).getSource() != null)
                                && (!((mxCell) mxiCellClicked.getEdgeAt(0)).getSource().equals(cell))
                                && (((mxCell) mxiCellClicked.getEdgeAt(0)).getTarget() != null)
                                && (((mxCell) mxiCellClicked.getEdgeAt(0)).getTarget().equals(cell)))
                                {
                                final mxICell mxiEdge;
                                final mxICell mxiEdgeSource;

                                // There should be only ONE Incoming Edge, connected to the Port source
                                mxiEdge = mxiCellClicked.getEdgeAt(0);

                                mxiEdgeSource = ((mxCell) mxiEdge).getSource();

                                // The Serial Port Source must have a valid UserObject
                                if (mxiEdgeSource.getValue() instanceof SerialConfigurationCellDataInterface)
                                    {
                                    // Show the data from the clicked cell
                                    indicator.setSelectedCellData((SerialConfigurationCellDataInterface) mxiCellClicked.getValue(),
                                                                  debug);

                                    debugCellData(mxiCellClicked,
                                                  (SerialConfigurationCellDataInterface)mxiCellClicked.getValue(),
                                                  SOURCE + "mouseReleased() 0",
                                                  debug);
                                    logPortProperties(((SerialConfigurationCellDataInterface) mxiCellClicked.getValue()).getResourceKey(),
                                                      ((SerialConfigurationCellDataInterface) mxiCellClicked.getValue()).getStreamType(),
                                                      SOURCE + "mouseReleased() 0",
                                                      debug);
                                    }
                                else
                                    {
                                    indicator.setSelectedCellData(null, debug);

                                    LOGGER.debug(debug, "Invalid Source UserObject");
                                    }
                                }
                            else
                                {
                                indicator.setSelectedCellData(null, debug);

                                LOGGER.debug(debug, "The Graph is currently invalid, or the node has no connections");
                                }
                            }
                        else
                            {
                            // Show the data from the clicked cell
                            indicator.setSelectedCellData((SerialConfigurationCellDataInterface) mxiCellClicked.getValue(),
                                                          debug);

                            debugCellData(mxiCellClicked,
                                          (SerialConfigurationCellDataInterface)mxiCellClicked.getValue(),
                                          SOURCE + "mouseReleased() 1",
                                          debug);
                            logPortProperties(((SerialConfigurationCellDataInterface) mxiCellClicked.getValue()).getResourceKey(),
                                              ((SerialConfigurationCellDataInterface) mxiCellClicked.getValue()).getStreamType(),
                                              SOURCE + "mouseReleased() 1",
                                              debug);
                            }
                        }
                    else
                        {
                        indicator.setSelectedCellData(null, debug);

                        LOGGER.debug(debug, "\nUser Object");
                        LOGGER.debug(debug, INDENT + "No UserObject data, or node is an edge");
                        }
                    }
                else
                    {
                    indicator.setSelectedCellData(null, debug);
                    }

                LOGGER.debug(debug, SOURCE + "mouseReleased() FINISH" + "\n");
                }
            });
        }


    /***********************************************************************************************
     * Redraw the graph.
     *
     * @param scui
     * @param debug
     */

    public static void rebuildGraph(final SerialConfigurationUIComponentInterface scui,
                                    final boolean debug)
        {
        final String SOURCE = "SerialConfigurationHelper.rebuildGraph() ";

        LOGGER.debug(debug, SOURCE + "START");

        scui.getGraph().getModel().beginUpdate();
        // http://forum.jgraph.com/questions/28/how-to-best-remove-all-cells
        scui.getGraph().removeCells(scui.getGraph().getChildCells(scui.getGraph().getDefaultParent(), true, true));
        scui.getGraph().getModel().endUpdate();

        scui.getGraph().getModel().beginUpdate();

        try
            {
            int intX;
            int intY;
            final Enumeration enumCommPortIDs;
            Object objStaribusHubVertex;
            final Object objUnknownPortVertex;

            scui.getSerialPortCells().clear();
            scui.setUnknownPortCell(null);
            scui.getStaribusInstrumentCells().clear();
            scui.getSerialInstrumentCells().clear();

            objStaribusHubVertex = null;

            //---------------------------------------------------------------------------------
            // Firstly add ALL of the physical Serial Ports in the right hand column

            enumCommPortIDs = CommPortIdentifier.getPortIdentifiers();

            intX = ORIGIN_X
                       + SerialConfigurationNodeType.STARIBUS_INSTRUMENT.getVertexWidth()
                       + SerialConfigurationNodeType.STARIBUS_HUB.getVertexWidth()
                       + (WIDTH_SEPARATOR_X << 1);
            intY = ORIGIN_Y;

            if (enumCommPortIDs != null)
                {
                while (enumCommPortIDs.hasMoreElements())
                    {
                    final CommPortIdentifier commPortId;

                    commPortId = (CommPortIdentifier) enumCommPortIDs.nextElement();

                    // We may need to indicate if the Port is owned?
                    if (commPortId.isCurrentlyOwned())
                        {
                        LOGGER.debug(debug,
                                     INDENT + "[port=" + commPortId.getName()
                                     + "] [owner=" + commPortId.getCurrentOwner() + TERMINATOR);
                        }
                    else
                        {
                        LOGGER.debug(debug,
                                     INDENT + "[port=" + commPortId.getName()
                                     + "] [not owned]");
                        }

                    if (commPortId.getPortType() == CommPortIdentifier.PORT_SERIAL)
                        {
                        final Object objSerialPortVertex;

                        int offset;

                        if (commPortId.isCurrentlyOwned())
                            {
                            offset = 10;
                            }
                        else
                            {
                            offset = 0;
                            }
                        objSerialPortVertex = scui.getGraph().insertVertex(scui.getGraphParent(),
                                                                           null,
                                                                           commPortId.getName(),
                                                                           intX + offset,
                                                                           intY,
                                                                           SerialConfigurationNodeType.SERIAL_PORT.getVertexWidth(),
                                                                           SerialConfigurationNodeType.SERIAL_PORT.getVertexHeight(),
                                                                           SerialConfigurationNodeType.SERIAL_PORT.getName());

                        // Why doesn't JGraphX just return mxICell?
                        if (objSerialPortVertex instanceof mxICell)
                            {
                            final SerialConfigurationCellDataInterface userData;

                            userData = new SerialConfigurationCellData();
                            userData.setLabel(commPortId.getName());
                            userData.setNodeType(SerialConfigurationNodeType.SERIAL_PORT);
                            // The SerialPort StreamType must change to match its incoming Source StreamType,
                            // i.e. SERIAL or STARIBUS
                            userData.setStreamType(StreamType.SERIAL);
                            // The SerialPort DaoPort must change to match its incoming Source DaoPort
                            userData.setDaoPort(null);
                            userData.setOpen(false);
                            // The SerialPort ResourceKey must change to match its incoming Source ResourceKey
                            userData.setResourceKey("UNINITIALISED.");
                            userData.setChanged(false);
                            userData.setDebug(debug);

                            LOGGER.debug(debug, SOURCE + "Set SerialConfigurationCellData for SerialPort");
                            ((mxICell) objSerialPortVertex).setValue(userData);
                            scui.getSerialPortCells().put(commPortId.getName(), (mxICell) objSerialPortVertex);
                            }

                        // Move down the right hand column
                        intY = intY
                                   + SerialConfigurationNodeType.SERIAL_PORT.getVertexHeight()
                                   + HEIGHT_SEPARATOR_Y;
                        }
                    else
                        {
                        // Ignore all other kinds of ports
                        }
                    }
                }
            else
                {
                LOGGER.debug(debug, SOURCE + "There are no CommPortIDs");
                }

            // Add one vertex for Unknown Ports, at the bottom of the right hand column
            // i.e. those Ports which have been requested in the XML, but don't exist on this host
            objUnknownPortVertex = scui.getGraph().insertVertex(scui.getGraphParent(),
                                                                null,
                                                                "Unknown",
                                                                intX,
                                                                intY,
                                                                SerialConfigurationNodeType.UNKNOWN_PORT.getVertexWidth(),
                                                                SerialConfigurationNodeType.UNKNOWN_PORT.getVertexHeight(),
                                                                SerialConfigurationNodeType.UNKNOWN_PORT.getName());
            // Why doesn't JGraphX just return mxICell?
            if (objUnknownPortVertex instanceof mxICell)
                {
                final SerialConfigurationCellDataInterface userData;

                userData = new SerialConfigurationCellData();
                userData.setLabel("Unknown");
                userData.setNodeType(SerialConfigurationNodeType.UNKNOWN_PORT);
                // The UnknownPort is always Virtual
                userData.setStreamType(StreamType.VIRTUAL);
                // The Unknown Port does not use the DaoPort
                userData.setDaoPort(null);
                userData.setOpen(false);
                // The Unknown Port does not use the ResourceKey
                userData.setResourceKey("UNINITIALISED.");
                userData.setChanged(false);
                userData.setDebug(debug);

                LOGGER.debug(debug, SOURCE + "Set SerialConfigurationCellData for UnknownPort");
                ((mxICell) objUnknownPortVertex).setValue(userData);
                scui.setUnknownPortCell(((mxICell) objUnknownPortVertex));
                }

            //---------------------------------------------------------------------------------
            // Process all Instruments, which will appear in the left hand column
            // Check that the Observatory collection of Instruments has been initialised

            intX = ORIGIN_X;
            intY = ORIGIN_Y;

            if ((scui.getObservatoryUI() !=  null)
                && (scui.getObservatoryUI().getInstrumentsDoc() != null)
                && (scui.getObservatoryUI().getInstrumentsDoc().getInstruments() != null)
                && (scui.getObservatoryUI().getInstrumentsDoc().getInstruments().getInstrumentList() != null)
                && (!scui.getObservatoryUI().getInstrumentsDoc().getInstruments().getInstrumentList().isEmpty())
                && (scui.getObservatoryUI().getObservatoryInstruments() != null)
                && (!scui.getObservatoryUI().getObservatoryInstruments().isEmpty()))
                {
                String strStaribusPortKey;
                final List<Instrument> listInstruments;
                final Iterator<Instrument> iterInstruments;
                final List<ObservatoryInstrumentInterface> listObservatoryInstruments;
                final Iterator<ObservatoryInstrumentInterface> iterObservatoryInstruments;

                //---------------------------------------------------------------------------------
                // First create a single Staribus Hub, to which all Staribus Instruments will connect
                // This appears at the top of the centre column
                // If no Staribus Instruments are found, we'll remove this later

                strStaribusPortKey = EMPTY_STRING;

                if (scui.getObservatoryUI().getInstrumentsDoc().getInstruments().getStaribusPort() != null)
                    {
                    // Look for Starbase.Observatory.Staribus.
                    strStaribusPortKey = scui.getObservatoryUI().getResourceKey()
                                             + scui.getObservatoryUI().getInstrumentsDoc().getInstruments().getStaribusPort().getResourceKey()
                                             + RegistryModelPlugin.DELIMITER_RESOURCE;

                    objStaribusHubVertex = scui.getGraph().insertVertex(scui.getGraphParent(),
                                                                        null,
                                                                        scui.getObservatoryUI().getInstrumentsDoc().getInstruments().getStaribusPort().getResourceKey(),
                                                                        intX
                                                                            + SerialConfigurationNodeType.STARIBUS_INSTRUMENT.getVertexWidth()
                                                                            + WIDTH_SEPARATOR_X,
                                                                        intY,
                                                                        SerialConfigurationNodeType.STARIBUS_HUB.getVertexWidth(),
                                                                        SerialConfigurationNodeType.STARIBUS_HUB.getVertexHeight(),
                                                                        SerialConfigurationNodeType.STARIBUS_HUB.getName());

                    // Why doesn't JGraphX just return mxICell?
                    if (objStaribusHubVertex instanceof mxICell)
                        {
                        final SerialConfigurationCellDataInterface userData;

                        userData = new SerialConfigurationCellData();
                        userData.setLabel(scui.getObservatoryUI().getInstrumentsDoc().getInstruments().getStaribusPort().getResourceKey());
                        userData.setNodeType(SerialConfigurationNodeType.STARIBUS_HUB);
                        // The StaribusHub is always of StreamType STARIBUS
                        userData.setStreamType(StreamType.STARIBUS);
                        // The StaribusHub is always connected to the Observatory Staribus Port, if it exists
                        userData.setDaoPort(PORT_CONTROLLER.getStaribusPort());

                        if (userData.getDaoPort() != null)
                            {
                            userData.setOpen(PORT_CONTROLLER.getStaribusPort().isPortOpen());
                            }
                        else
                            {
                            userData.setOpen(false);
                            }

                        // The StaribusHub always has the same resources as the StaribusPort
                        userData.setResourceKey(strStaribusPortKey);
                        userData.setChanged(false);
                        userData.setDebug(debug);

                        LOGGER.debug(debug, SOURCE + "Set SerialConfigurationCellData for StaribusHub");
                        scui.setStaribusHubCell((mxICell)objStaribusHubVertex);
                        scui.getStaribusHubCell().setValue(userData);
                        debugCellData(scui.getStaribusHubCell(),
                                      userData,
                                      SOURCE,
                                      debug);
                        logPortProperties(userData.getResourceKey(),
                                          userData.getStreamType(),
                                          SOURCE,
                                          debug);
                        }
                    }
                else
                    {
                    LOGGER.debug(debug, SOURCE + "There is no StaribusPort defined in the Instrument");
                    scui.setStaribusHubCell(null);
                    }

                //---------------------------------------------------------------------------------
                // Staribus Instruments
                //---------------------------------------------------------------------------------
                // Show all Staribus Instruments in the left hand column
                // We don't need the DaoPort, so we can use the Instrument XML directly

                listInstruments = scui.getObservatoryUI().getInstrumentsDoc().getInstruments().getInstrumentList();
                iterInstruments = listInstruments.iterator();

                while (iterInstruments.hasNext())
                    {
                    final Instrument instrument;

                    instrument = iterInstruments.next();

                    // If there is no DAO or Port, there can't be any serial IO for Staribus
                    // Check that the User has set up the correct Rx and Tx streams
                    if ((instrument != null)
                        && (instrument.getDAO() != null)
                        && (ObservatoryInstrumentHelper.isStaribusController(instrument))
                        && (InstrumentHelper.hasValidRxTxStreams(instrument, StreamType.STARIBUS)))
                        {
                        final Object objStaribusInstrumentVertex;
                        final Object objBusEdge;
                        final StringBuffer bufferValue;

                        bufferValue = new StringBuffer();
                        bufferValue.append(instrument.getIdentifier());
                        bufferValue.append(" [");
                        bufferValue.append(Utilities.intPositiveToThreeDecimalString(ObservatoryInstrumentHelper.getStaribusAddressAsInteger(instrument)));
                        bufferValue.append("]");

                        objStaribusInstrumentVertex = scui.getGraph().insertVertex(scui.getGraphParent(),
                                                                                   null,
                                                                                   bufferValue.toString(),
                                                                                   intX,
                                                                                   intY,
                                                                                   SerialConfigurationNodeType.STARIBUS_INSTRUMENT.getVertexWidth(),
                                                                                   SerialConfigurationNodeType.STARIBUS_INSTRUMENT.getVertexHeight(),
                                                                                   SerialConfigurationNodeType.STARIBUS_INSTRUMENT.getName());
                        // Move down the left hand column
                        intY = intY
                               + SerialConfigurationNodeType.STARIBUS_INSTRUMENT.getVertexHeight()
                               + HEIGHT_SEPARATOR_Y;

                        // Why doesn't JGraphX just return mxICell?
                        if (objStaribusInstrumentVertex instanceof mxICell)
                            {
                            final SerialConfigurationCellDataInterface userData;

                            userData = new SerialConfigurationCellData();
                            userData.setLabel(bufferValue.toString());
                            userData.setNodeType(SerialConfigurationNodeType.STARIBUS_INSTRUMENT);
                            // Staribus Instruments are always of StreamType STARIBUS
                            userData.setStreamType(StreamType.STARIBUS);
                            // A Staribus Instrument is always connected to the Observatory Staribus Port, if it exists
                            userData.setDaoPort(PORT_CONTROLLER.getStaribusPort());

                            if (userData.getDaoPort() != null)
                                {
                                userData.setOpen(PORT_CONTROLLER.getStaribusPort().isPortOpen());
                                }
                            else
                                {
                                userData.setOpen(false);
                                }

                            // The Staribus Instrument always has the same resources as the Observatory Staribus Port
                            userData.setResourceKey(strStaribusPortKey);
                            userData.setChanged(false);
                            userData.setDebug(debug);

                            LOGGER.debug(debug, SOURCE + "Set SerialConfigurationCellData for StaribusInstrument");
                            ((mxICell) objStaribusInstrumentVertex).setValue(userData);
                            scui.getStaribusInstrumentCells().put(instrument.getIdentifier(), (mxICell)objStaribusInstrumentVertex);
                            debugCellData((mxICell) objStaribusInstrumentVertex,
                                          userData,
                                          SOURCE,
                                          debug);
                            logPortProperties(userData.getResourceKey(),
                                              userData.getStreamType(),
                                              SOURCE,
                                              debug);
                            }

                        // Connect the Staribus Instrument to the Observatory Staribus Hub Vertex
                        LOGGER.debug(debug, SOURCE + "Connect the StaribusInstrument to the StaribusHub");
                        objBusEdge = scui.getGraph().insertEdge(scui.getGraphParent(),
                                                                null,
                                                                EMPTY_STRING,
                                                                objStaribusInstrumentVertex,
                                                                objStaribusHubVertex,
                                                                STYLE_ASSIGNMENT);
                        }
                    else
                        {
                        LOGGER.debug(debug, SOURCE + "There is no DAO or Port, or the Instrument is invalid");
                        }
                    }

                //---------------------------------------------------------------------------------
                // Link the Staribus Hub Vertex to the Staribus Serial Port if possible

                if (!scui.getStaribusInstrumentCells().isEmpty()
                    && !scui.getSerialPortCells().isEmpty()
                    && (objStaribusHubVertex != null)
                    && (scui.getObservatoryUI().getInstrumentsDoc().getInstruments().getStaribusPort() != null))
                    {
                    // See if we found the Observatory Staribus (physical) Port,
                    // check that all the Serial Properties are in the Registry,
                    // and if so, link the Staribus Serial Port to the StaribusPort vertex.

                    if (hasValidPortProperties(strStaribusPortKey, StreamType.STARIBUS))
                        {
                        final String strStaribusPortName;
                        final Object objBusEdge;

                        // Look for Starbase.Observatory.Staribus.Port.Name
                        strStaribusPortName = REGISTRY.getStringProperty(strStaribusPortKey + KEY_PORT_NAME);

                        if (scui.getSerialPortCells().containsKey(strStaribusPortName))
                            {
                            final mxICell cellStaribusSerialPort;

                            cellStaribusSerialPort = scui.getSerialPortCells().get(strStaribusPortName);

                            // Why doesn't JGraphX just return mxICell?
                            if ((cellStaribusSerialPort.getValue() instanceof SerialConfigurationCellDataInterface)
                                && (objStaribusHubVertex instanceof mxICell)
                                && (((mxICell)objStaribusHubVertex).getValue() instanceof SerialConfigurationCellDataInterface))
                                {
                                final SerialConfigurationCellDataInterface userDataStaribusHub;
                                final SerialConfigurationCellDataInterface userDataStaribusSerialPort;

                                userDataStaribusHub = (SerialConfigurationCellDataInterface)((mxICell)objStaribusHubVertex).getValue();
                                userDataStaribusSerialPort = (SerialConfigurationCellDataInterface) cellStaribusSerialPort.getValue();

                                // Connect the StaribusHub to the Staribus SerialPort
                                userDataStaribusSerialPort.setStreamType(userDataStaribusHub.getStreamType());
                                userDataStaribusSerialPort.setDaoPort(userDataStaribusHub.getDaoPort());
                                userDataStaribusSerialPort.setOpen(userDataStaribusHub.isOpen());
                                userDataStaribusSerialPort.setResourceKey(userDataStaribusHub.getResourceKey());
                                userDataStaribusSerialPort.setChanged(true);
                                }

                            LOGGER.debug(debug, SOURCE + "Connect the StaribusHub to the StaribusPort");
                            objBusEdge = scui.getGraph().insertEdge(scui.getGraphParent(),
                                                                    null,
                                                                    EMPTY_STRING,
                                                                    objStaribusHubVertex,
                                                                    cellStaribusSerialPort,
                                                                    STYLE_ASSIGNMENT);
                            }
                        else
                            {
                            LOGGER.debug(debug, SOURCE + "Connect the StaribusHub to the UnknownPort");
                            objBusEdge = scui.getGraph().insertEdge(scui.getGraphParent(),
                                                                    null,
                                                                    EMPTY_STRING,
                                                                    objStaribusHubVertex,
                                                                    objUnknownPortVertex,
                                                                    STYLE_ASSIGNMENT);
                            }
                        }
                    else
                        {
                        LOGGER.warn(SOURCE + SerialConfigurationUIComponentInterface.MSG_STARIBUS_PORT_0);
                        }
                    }
                else
                    {
                    // Remove the unwanted Staribus Hub vertex,
                    // since there are no Staribus Instruments and/or Port
                    LOGGER.debug(debug, SOURCE + "There are no Staribus Instruments and/or Port");
                    scui.getGraph().getModel().remove(objStaribusHubVertex);
                    }

                //---------------------------------------------------------------------------------
                // Serial Instruments
                //---------------------------------------------------------------------------------
                // Now show all non-Staribus Instruments in the left hand column,
                // i.e. those with connected directly to a serial interface on the host
                // This is indicated by a <VirtualAddress> of 001
                // We need the DaoPort, so we can must the Collection of Observatory Instruments

                listObservatoryInstruments = scui.getObservatoryUI().getObservatoryInstruments();
                iterObservatoryInstruments = listObservatoryInstruments.iterator();

                while (iterObservatoryInstruments.hasNext())
                    {
                    final ObservatoryInstrumentInterface obsInstrument;

                    obsInstrument = iterObservatoryInstruments.next();

                    // If there is no DAO or Port, there can't be any serial IO,
                    // except for the TerminalEmulator, so don't test for a DAO!
                    if ((obsInstrument != null)
                        && (obsInstrument.getInstrument() != null)
                        && (ObservatoryInstrumentHelper.isVirtualController(obsInstrument.getInstrument())))
                        {
                        // Test the DAO (if any) for a single Serial Port '001'
                        // Check that the User has set up the correct Rx and Tx streams
                        if ((ObservatoryInstrumentHelper.getVirtualAddressAsInteger(obsInstrument.getInstrument()) == 1)
                            && (InstrumentHelper.hasValidRxTxStreams(obsInstrument.getInstrument(), StreamType.SERIAL)))
                            {
                            final Object objSerialInstrumentVertex;
                            final String strInstrumentIdentifier;
                            final String strSerialInstrumentPortKey;

                            strInstrumentIdentifier = obsInstrument.getInstrument().getIdentifier();

                            objSerialInstrumentVertex = scui.getGraph().insertVertex(scui.getGraphParent(),
                                                                                     null,
                                                                                     strInstrumentIdentifier,
                                                                                     intX,
                                                                                     intY,
                                                                                     SerialConfigurationNodeType.SERIAL_INSTRUMENT.getVertexWidth(),
                                                                                     SerialConfigurationNodeType.SERIAL_INSTRUMENT.getVertexHeight(),
                                                                                     SerialConfigurationNodeType.SERIAL_INSTRUMENT.getName());
                            // Move down the left hand column
                            intY = intY
                                   + SerialConfigurationNodeType.SERIAL_INSTRUMENT.getVertexHeight()
                                   + HEIGHT_SEPARATOR_Y;

                            // For each Serial Instrument found,
                            // check that all the Serial Properties are in the Registry,
                            // and if so, link the Instrument to the requested Port.
                            // If not, link the Instrument to the Unknown Port.

                            // First build the ResourceKey
                            if ((obsInstrument.getInstrument().getDAO() != null)
                                && (obsInstrument.getInstrument().getDAO().getPort() != null))
                                {
                                // Instrument Port ResourceKey = Starbase.Observatory.InstrumentKey.PortKey.
                                strSerialInstrumentPortKey = scui.getObservatoryUI().getResourceKey()
                                                               + strInstrumentIdentifier
                                                               + RegistryModelPlugin.DELIMITER_RESOURCE
                                                               + obsInstrument.getInstrument().getDAO().getPort().getResourceKey()
                                                               + RegistryModelPlugin.DELIMITER_RESOURCE;
                                }
                            else
                                {
                                // There is no DAO or Port, so there cannot be a Port Key
                                // Instrument ResourceKey = Starbase.Observatory.InstrumentKey.
                                strSerialInstrumentPortKey = scui.getObservatoryUI().getResourceKey()
                                                               + strInstrumentIdentifier
                                                               + RegistryModelPlugin.DELIMITER_RESOURCE;
                                }

                            // Configure the UserObject
                            // Why doesn't JGraphX just return mxICell?
                            if (objSerialInstrumentVertex instanceof mxICell)
                                {
                                final SerialConfigurationCellDataInterface userData;

                                userData = new SerialConfigurationCellData();
                                userData.setLabel(strInstrumentIdentifier);
                                userData.setNodeType(SerialConfigurationNodeType.SERIAL_INSTRUMENT);
                                // Serial Instruments are always of StreamType SERIAL
                                userData.setStreamType(StreamType.SERIAL);

                                if (obsInstrument.getDAO() != null)
                                    {
                                    userData.setDaoPort(obsInstrument.getDAO().getPort());
                                    userData.setOpen(obsInstrument.getDAO().getPort().isPortOpen());
                                    }
                                else
                                    {
                                    userData.setDaoPort(null);
                                    userData.setOpen(false);
                                    }

                                userData.setResourceKey(strSerialInstrumentPortKey);
                                userData.setChanged(false);
                                userData.setDebug(debug);

                                LOGGER.debug(debug, SOURCE + "Set SerialConfigurationCellData for SerialInstrument");
                                ((mxICell) objSerialInstrumentVertex).setValue(userData);
                                scui.getSerialInstrumentCells().put(strInstrumentIdentifier, (mxICell)objSerialInstrumentVertex);
                                debugCellData((mxICell) objSerialInstrumentVertex,
                                              userData,
                                              SOURCE,
                                              debug);
                                logPortProperties(userData.getResourceKey(),
                                                  userData.getStreamType(),
                                                  SOURCE,
                                                  debug);
                                }

                            // Link the Instrument to the requested Port if possible
                            if (hasValidPortProperties(strSerialInstrumentPortKey, StreamType.SERIAL))
                                {
                                final String strSerialPortName;
                                final Object objBusEdge;

                                strSerialPortName = REGISTRY.getStringProperty(strSerialInstrumentPortKey + KEY_PORT_NAME);

                                if (scui.getSerialPortCells().containsKey(strSerialPortName))
                                    {
                                    final mxICell cellSerialPort;

                                    cellSerialPort = scui.getSerialPortCells().get(strSerialPortName);

                                    if ((cellSerialPort.getValue() instanceof SerialConfigurationCellDataInterface)
                                        && (objSerialInstrumentVertex instanceof mxICell)
                                        && (((mxICell)objSerialInstrumentVertex).getValue() instanceof SerialConfigurationCellDataInterface))
                                        {
                                        final SerialConfigurationCellDataInterface userDataSerialInstrument;
                                        final SerialConfigurationCellDataInterface userDataSerialPort;

                                        userDataSerialInstrument = (SerialConfigurationCellDataInterface)((mxICell)objSerialInstrumentVertex).getValue();
                                        userDataSerialPort = (SerialConfigurationCellDataInterface) cellSerialPort.getValue();

                                        // Connect the SerialInstrument to the SerialPort
                                        userDataSerialPort.setStreamType(userDataSerialInstrument.getStreamType());

                                        if (obsInstrument.getDAO() != null)
                                            {
                                            userDataSerialPort.setDaoPort(userDataSerialInstrument.getDaoPort());
                                            userDataSerialPort.setOpen(userDataSerialInstrument.isOpen());
                                            }
                                        else
                                            {
                                            userDataSerialPort.setDaoPort(null);
                                            userDataSerialPort.setOpen(false);
                                            }

                                        userDataSerialPort.setResourceKey(userDataSerialInstrument.getResourceKey());
                                        userDataSerialPort.setChanged(true);
                                        }

                                    LOGGER.debug(debug, SOURCE + "Connect the SerialInstrument to the SerialPort");
                                    objBusEdge = scui.getGraph().insertEdge(scui.getGraphParent(),
                                                                            null,
                                                                            EMPTY_STRING,
                                                                            objSerialInstrumentVertex,
                                                                            cellSerialPort,
                                                                            STYLE_ASSIGNMENT);
                                    }
                                else
                                    {
                                    LOGGER.debug(debug, SOURCE + "Connect the SerialInstrument to the UnknownPort");
                                    objBusEdge = scui.getGraph().insertEdge(scui.getGraphParent(),
                                                                            null,
                                                                            EMPTY_STRING,
                                                                            objSerialInstrumentVertex,
                                                                            objUnknownPortVertex,
                                                                            STYLE_ASSIGNMENT);
                                    }
                                }
                            else
                                {
                                LOGGER.warn(SOURCE + SerialConfigurationUIComponentInterface.TITLE_INVALID_SERIAL_PORT);
                                }
                            }
                        }
                    else
                        {
                        // Ignore other Virtual Controller categories
                        LOGGER.debug(debug, SOURCE + "There is no DAO or Port, or the Instrument is invalid");
                        }
                    }
                }
            else
                {
                LOGGER.debug(debug, SOURCE + "ObservatoryUI Instrument lists are empty or NULL");
                }
            }

        catch (UnsatisfiedLinkError exception)
            {
            exception.printStackTrace();
            LOGGER.error(SOURCE
                             + METADATA_EXCEPTION
                             + exception.getMessage()
                             + TERMINATOR);
            ClassPathLoader.showClassLoaderSearchPaths(debug);
            }

        finally
            {
            scui.getGraph().getModel().endUpdate();
            }

        LOGGER.debug(debug, SOURCE + "FINISH");
        }


    /*********************************************************************************************/
    /* Edges                                                                                     */
    /**********************************************************************************************
     * Create a new style for port assignments.
     *
     * @return Hashtable<String, Object>
     */

    private static Hashtable<String, Object> createAssignmentStyle()
        {
        final Hashtable<String, Object> style;

        style = new Hashtable<String, Object>(10);

        //style.put(mxConstants.STYLE_EDGE, mxConstants.EDGESTYLE_ENTITY_RELATION);
        style.put(mxConstants.STYLE_STROKECOLOR, mxUtils.getHexColorString(Color.BLACK));
        style.put(mxConstants.STYLE_STROKEWIDTH, 1.5);
        style.put(mxConstants.STYLE_STARTARROW, mxConstants.ARROW_OVAL);
        style.put(mxConstants.STYLE_ENDARROW, mxConstants.ARROW_CLASSIC);

        style.put(mxConstants.STYLE_FONTFAMILY, "Courier");
        style.put(mxConstants.STYLE_FONTSTYLE, mxConstants.FONT_BOLD);
        style.put(mxConstants.STYLE_FONTSIZE, 14);

        return (style);
        }


    /***********************************************************************************************
     * See if the Registry contains a complete set of Properties
     * which define a Port with the specified Key.
     *
     * @param resourcekey
     * @param streamtype
     *
     * @return boolean
     */

    private static boolean hasValidPortProperties(final String resourcekey,
                                                  final StreamType streamtype)
        {
        final String SOURCE = "SerialConfigurationHelper.hasValidPortProperties() ";
        final String strPortName;
        final int intPortBaudrate;
        final int intPortDataBits;
        final int intPortStopBits;
        final int intPortParity;
        final String strPortFlowControl;
        boolean boolValid;

        // Do not use the usual Registry methods, we don't want Exceptions!
        switch (streamtype)
            {
            case VIRTUAL:
                {
                // Virtual doesn't have Ports, so let's assume this is valid
                boolValid = true;
                break;
                }

            case STARIBUS:
                {
                PropertyPlugin propertyData;

                propertyData = (PropertyPlugin)REGISTRY.getProperties().get(resourcekey + KEY_PORT_NAME);

                if ((propertyData != null)
                    && (propertyData.getDataType().equals(String.class.getName()))
                    && (propertyData.getResource() != null))
                    {
                    strPortName = (String)propertyData.getResource();
                    }
                else
                    {
                    strPortName = NO_DATA;
                    }

                propertyData = (PropertyPlugin)REGISTRY.getProperties().get(resourcekey + KEY_PORT_BAUDRATE);

                if ((propertyData != null)
                    && (propertyData.getDataType().equals(Integer.class.getName()))
                    && (propertyData.getResource() != null))
                    {
                    intPortBaudrate = (Integer)propertyData.getResource();
                    }
                else
                    {
                    intPortBaudrate = -1;
                    }

                propertyData = (PropertyPlugin)REGISTRY.getProperties().get(resourcekey + KEY_PORT_DATA_BITS);

                if ((propertyData != null)
                    && (propertyData.getDataType().equals(Integer.class.getName()))
                    && (propertyData.getResource() != null))
                    {
                    intPortDataBits = (Integer)propertyData.getResource();
                    }
                else
                    {
                    intPortDataBits = -1;
                    }

                propertyData = (PropertyPlugin)REGISTRY.getProperties().get(resourcekey + KEY_PORT_STOP_BITS);

                if ((propertyData != null)
                    && (propertyData.getDataType().equals(Integer.class.getName()))
                    && (propertyData.getResource() != null))
                    {
                    intPortStopBits = (Integer)propertyData.getResource();
                    }
                else
                    {
                    intPortStopBits = -1;
                    }

                propertyData = (PropertyPlugin)REGISTRY.getProperties().get(resourcekey + KEY_PORT_PARITY);

                if ((propertyData != null)
                    && (propertyData.getDataType().equals(Integer.class.getName()))
                    && (propertyData.getResource() != null))
                    {
                    intPortParity = (Integer)propertyData.getResource();
                    }
                else
                    {
                    intPortParity = -1;
                    }

                propertyData = (PropertyPlugin)REGISTRY.getProperties().get(resourcekey + KEY_PORT_FLOW_CONTROL);

                if ((propertyData != null)
                    && (propertyData.getDataType().equals(String.class.getName()))
                    && (propertyData.getResource() != null))
                    {
                    strPortFlowControl = (String)propertyData.getResource();
                    }
                else
                    {
                    strPortFlowControl = NO_DATA;
                    }

                boolValid = (!NO_DATA.equals(strPortName));
                boolValid = boolValid && (!EMPTY_STRING.equals(strPortName));

//                // Allowed values: 9600 and 57600, depending on the controller configuration
//                // The default value is 57600 Baud
//                boolValid = boolValid
//                                && ((intPortBaudrate == SerialBaudRate.RATE_9600.getBaudRate())
//                                    || (intPortBaudrate == SerialBaudRate.RATE_57600.getBaudRate()));
//
//                // The Data Bits setting is 7 for Staribus
//                boolValid = boolValid && (intPortDataBits == SerialDataBits.DATABITS_7.getDataBits());
//
//                // The Stop Bits setting is ONE for Staribus
//                boolValid = boolValid && (intPortStopBits == SerialStopBits.STOPBITS_1.getStopBits());
//
//                // The Parity setting is EVEN for Staribus
//                boolValid = boolValid && (intPortParity == SerialParity.PARITY_EVEN.getParity());
//
//                // The flow control method of the serial port (None, XON/XOFF, RTS/CTS)
//                // This is usually None for Staribus, but allow others
//                boolValid = boolValid
//                                && (!NO_DATA.equalsIgnoreCase(strPortFlowControl))
//                                && ((SerialFlowControl.FLOW_NONE.getFlowControl().equalsIgnoreCase(strPortFlowControl))
//                                    || (SerialFlowControl.FLOW_XON_XOFF.getFlowControl().equalsIgnoreCase(strPortFlowControl))
//                                    || (SerialFlowControl.FLOW_RTS_CTS.getFlowControl().equalsIgnoreCase(strPortFlowControl)));

                // NOTE: Do not insist on Staribus spec for now, it causes problems by removing the Edge from the Hub to the Port!
                // Allowed values: 1200, 2400, 4800, 9600, 19200, 38400, 57600, 115200 depending on the controller configuration
                // The default value is 57600 Baud
                boolValid = boolValid
                            && (intPortBaudrate >= SerialBaudRate.RATE_1200.getBaudRate())
                            && (intPortBaudrate <= SerialBaudRate.RATE_115200.getBaudRate());

                // The Data Bits setting for the serial port (5, 6, 7, 8)
                boolValid = boolValid
                            && (intPortDataBits >= SerialDataBits.DATABITS_5.getDataBits())
                            && (intPortDataBits <= SerialDataBits.DATABITS_8.getDataBits());

                // The Stop Bits setting for the serial port (ONE=1, TWO=2, ONE.FIVE=3)
                boolValid = boolValid
                            && (intPortStopBits >= SerialStopBits.STOPBITS_1.getStopBits())
                            && (intPortStopBits <= SerialStopBits.STOPBITS_1_5.getStopBits());

                // The Parity setting for the serial port (NONE=0, ODD=1, EVEN=2, MARK=3, SPACE=4)
                boolValid = boolValid
                            && (intPortParity >= SerialParity.PARITY_NONE.getParity())
                            && (intPortParity <= SerialParity.PARITY_SPACE.getParity());

                // The flow control method of the serial port (None, XON/XOFF, RTS/CTS)
                boolValid = boolValid
                            && (!NO_DATA.equalsIgnoreCase(strPortFlowControl))
                            && ((SerialFlowControl.FLOW_NONE.getFlowControl().equalsIgnoreCase(strPortFlowControl))
                                || (SerialFlowControl.FLOW_XON_XOFF.getFlowControl().equalsIgnoreCase(strPortFlowControl))
                                || (SerialFlowControl.FLOW_RTS_CTS.getFlowControl().equalsIgnoreCase(strPortFlowControl)));
                break;
                }

            case SERIAL:
                {
                PropertyPlugin propertyData;

                propertyData = (PropertyPlugin)REGISTRY.getProperties().get(resourcekey + KEY_PORT_NAME);

                if ((propertyData != null)
                    && (propertyData.getDataType().equals(String.class.getName()))
                    && (propertyData.getResource() != null))
                    {
                    strPortName = (String)propertyData.getResource();
                    }
                else
                    {
                    strPortName = NO_DATA;
                    }

                propertyData = (PropertyPlugin)REGISTRY.getProperties().get(resourcekey + KEY_PORT_BAUDRATE);

                if ((propertyData != null)
                    && (propertyData.getDataType().equals(Integer.class.getName()))
                    && (propertyData.getResource() != null))
                    {
                    intPortBaudrate = (Integer)propertyData.getResource();
                    }
                else
                    {
                    intPortBaudrate = -1;
                    }

                propertyData = (PropertyPlugin)REGISTRY.getProperties().get(resourcekey + KEY_PORT_DATA_BITS);

                if ((propertyData != null)
                    && (propertyData.getDataType().equals(Integer.class.getName()))
                    && (propertyData.getResource() != null))
                    {
                    intPortDataBits = (Integer)propertyData.getResource();
                    }
                else
                    {
                    intPortDataBits = -1;
                    }

                propertyData = (PropertyPlugin)REGISTRY.getProperties().get(resourcekey + KEY_PORT_STOP_BITS);

                if ((propertyData != null)
                    && (propertyData.getDataType().equals(Integer.class.getName()))
                    && (propertyData.getResource() != null))
                    {
                    intPortStopBits = (Integer)propertyData.getResource();
                    }
                else
                    {
                    intPortStopBits = -1;
                    }

                propertyData = (PropertyPlugin)REGISTRY.getProperties().get(resourcekey + KEY_PORT_PARITY);

                if ((propertyData != null)
                    && (propertyData.getDataType().equals(Integer.class.getName()))
                    && (propertyData.getResource() != null))
                    {
                    intPortParity = (Integer)propertyData.getResource();
                    }
                else
                    {
                    intPortParity = -1;
                    }

                propertyData = (PropertyPlugin)REGISTRY.getProperties().get(resourcekey + KEY_PORT_FLOW_CONTROL);

                if ((propertyData != null)
                    && (propertyData.getDataType().equals(String.class.getName()))
                    && (propertyData.getResource() != null))
                    {
                    strPortFlowControl = (String)propertyData.getResource();
                    }
                else
                    {
                    strPortFlowControl = NO_DATA;
                    }

                boolValid = (!NO_DATA.equals(strPortName));
                boolValid = boolValid && (!EMPTY_STRING.equals(strPortName));

                // Allowed values: 1200, 2400, 4800, 9600, 19200, 38400, 57600, 115200 depending on the controller configuration
                // The default value is 57600 Baud
                boolValid = boolValid
                                && (intPortBaudrate >= SerialBaudRate.RATE_1200.getBaudRate())
                                && (intPortBaudrate <= SerialBaudRate.RATE_115200.getBaudRate());

                // The Data Bits setting for the serial port (5, 6, 7, 8)
                boolValid = boolValid
                                && (intPortDataBits >= SerialDataBits.DATABITS_5.getDataBits())
                                && (intPortDataBits <= SerialDataBits.DATABITS_8.getDataBits());

                // The Stop Bits setting for the serial port (ONE=1, TWO=2, ONE.FIVE=3)
                boolValid = boolValid
                                && (intPortStopBits >= SerialStopBits.STOPBITS_1.getStopBits())
                                && (intPortStopBits <= SerialStopBits.STOPBITS_1_5.getStopBits());

                // The Parity setting for the serial port (NONE=0, ODD=1, EVEN=2, MARK=3, SPACE=4)
                boolValid = boolValid
                                && (intPortParity >= SerialParity.PARITY_NONE.getParity())
                                && (intPortParity <= SerialParity.PARITY_SPACE.getParity());

                // The flow control method of the serial port (None, XON/XOFF, RTS/CTS)
                boolValid = boolValid
                                && (!NO_DATA.equalsIgnoreCase(strPortFlowControl))
                                && ((SerialFlowControl.FLOW_NONE.getFlowControl().equalsIgnoreCase(strPortFlowControl))
                                    || (SerialFlowControl.FLOW_XON_XOFF.getFlowControl().equalsIgnoreCase(strPortFlowControl))
                                    || (SerialFlowControl.FLOW_RTS_CTS.getFlowControl().equalsIgnoreCase(strPortFlowControl)));
                break;
                }

            case STARINET:
                {
                // ToDo Decide what this means!
                boolValid = true;
                break;
                }

            case ETHERNET:
                {
                // ToDo Decide what this means!
                boolValid = true;
                break;
                }

            default:
                {
                boolValid = false;
                }
            }


        return (boolValid);
        }


    /***********************************************************************************************
     * Indicate if the SerialPort portcell has a valid connection to the StaribusHub hubcell.
     *
     * @param portcell
     * @param hubcell
     * @param debug
     *
     * @return boolean
     */

    public static boolean isValidStaribusPort(final mxICell portcell,
                                              final mxICell hubcell,
                                              final boolean debug)
        {
        final String SOURCE = "SerialConfigurationHelper.isValidStaribusPort() ";
        boolean boolValidStaribusPort;

        boolValidStaribusPort = false;

        // The Port Cell must have the correct kind of UserObject
        if ((portcell != null)
            && (portcell.getValue() != null)
            && (portcell.getValue() instanceof SerialConfigurationCellDataInterface))
            {
            final SerialConfigurationCellDataInterface configDataPort;

            configDataPort = (SerialConfigurationCellDataInterface)portcell.getValue();

            debugCellData(portcell,
                          configDataPort,
                          SOURCE + " Port Cell",
                          debug);

            // The SerialPort Cell must be of type SERIAL_PORT
            if (SerialConfigurationNodeType.SERIAL_PORT.equals(configDataPort.getNodeType()))
                {
                // getSource() and getTarget() are not in the Interface!!
                // The graph must be valid, so look only for ONE Incoming Edge
                if ((portcell.isVertex())
                    && (portcell.getEdgeCount() == 1)
                    && (((mxCell) portcell.getEdgeAt(0)).getSource() != null)
                    && (!((mxCell) portcell.getEdgeAt(0)).getSource().equals(portcell))
                    && (((mxCell) portcell.getEdgeAt(0)).getTarget() != null)
                    && (((mxCell) portcell.getEdgeAt(0)).getTarget().equals(portcell)))
                    {
                    final mxICell mxiEdge;
                    final mxICell cellSourceOfEdge;

                    // There should be only ONE Incoming Edge, connected to the Port source
                    mxiEdge = portcell.getEdgeAt(0);
                    cellSourceOfEdge = ((mxCell) mxiEdge).getSource();

                    // The Serial Port Source must be the StaribusHub,
                    // and also have a valid UserObject
                    if ((cellSourceOfEdge != null)
                        && (cellSourceOfEdge.equals(hubcell))
                        && (cellSourceOfEdge.getValue() instanceof SerialConfigurationCellDataInterface))
                        {
                        final SerialConfigurationCellDataInterface configDataHub;

                        configDataHub = (SerialConfigurationCellDataInterface)cellSourceOfEdge.getValue();

                        debugCellData(hubcell,
                                      configDataHub,
                                      SOURCE + " StaribusHub Cell",
                                      debug);

                        // The StaribusHub Cell must be of type STARIBUS_HUB
                        boolValidStaribusPort = SerialConfigurationNodeType.STARIBUS_HUB.equals(configDataHub.getNodeType());
                        }
                    else
                        {
                        LOGGER.debug(debug, "INVALID STARIBUS HUB USER OBJECT");
                        }
                    }
                else
                    {
                    LOGGER.debug(debug, "THE GRAPH IS CURRENTLY INVALID (SERIAL PORT)");
                    }
                }
            else
                {
                LOGGER.debug(debug, "SERIAL PORT HAS USER DATA WITH INVALID NODETYPE");
                }
            }
        else
            {
            LOGGER.debug(debug, "INVALID SERIAL PORT CELL");
            }

        return (boolValidStaribusPort);
        }


    /***********************************************************************************************
     * Show in the console log the Properties of the Port with the specified Key.
     *
     * @param resourcekey
     * @param streamtype
     * @param message
     * @param debug
     *
     * @return boolean
     */

    public static void logPortProperties(final String resourcekey,
                                         final StreamType streamtype,
                                         final String message,
                                         final boolean debug)
        {
        final String SOURCE = "SerialConfigurationHelper.logPortProperties() ";
        final StringBuffer buffer;

        buffer = new StringBuffer();
        buffer.append("Port Properties ");
        buffer.append(message);

        // Do not use the usual Registry methods, we don't want Exceptions!
        switch (streamtype)
            {
            case VIRTUAL:
                {
                // Virtual doesn't have Ports
                buffer.append("\n");
                buffer.append(INDENT);
                buffer.append("[");
                buffer.append(NO_DATA);
                buffer.append(TERMINATOR);
                break;
                }

            case STARIBUS:
                {
                PropertyPlugin propertyData;

                propertyData = (PropertyPlugin)REGISTRY.getProperties().get(resourcekey + KEY_PORT_NAME);
                buffer.append("\n");
                buffer.append(INDENT);
                buffer.append("[");
                buffer.append(resourcekey);
                buffer.append(KEY_PORT_NAME);
                buffer.append(EQUALS);

                if ((propertyData != null)
                    && (propertyData.getDataType().equals(String.class.getName()))
                    && (propertyData.getResource() != null))
                    {
                    buffer.append((String)propertyData.getResource());
                    }
                else
                    {
                    buffer.append(NO_DATA);
                    }
                buffer.append(TERMINATOR);

                propertyData = (PropertyPlugin)REGISTRY.getProperties().get(resourcekey + KEY_PORT_BAUDRATE);
                buffer.append("\n");
                buffer.append(INDENT);
                buffer.append("[");
                buffer.append(resourcekey);
                buffer.append(KEY_PORT_BAUDRATE);
                buffer.append(EQUALS);

                if ((propertyData != null)
                    && (propertyData.getDataType().equals(Integer.class.getName()))
                    && (propertyData.getResource() != null))
                    {
                    buffer.append(propertyData.getResource());
                    }
                else
                    {
                    buffer.append(NO_DATA);
                    }
                buffer.append(TERMINATOR);

                propertyData = (PropertyPlugin)REGISTRY.getProperties().get(resourcekey + KEY_PORT_DATA_BITS);
                buffer.append("\n");
                buffer.append(INDENT);
                buffer.append("[");
                buffer.append(resourcekey);
                buffer.append(KEY_PORT_DATA_BITS);
                buffer.append(EQUALS);

                if ((propertyData != null)
                    && (propertyData.getDataType().equals(Integer.class.getName()))
                    && (propertyData.getResource() != null))
                    {
                    buffer.append(propertyData.getResource());
                    }
                else
                    {
                    buffer.append(NO_DATA);
                    }
                buffer.append(TERMINATOR);

                propertyData = (PropertyPlugin)REGISTRY.getProperties().get(resourcekey + KEY_PORT_STOP_BITS);
                buffer.append("\n");
                buffer.append(INDENT);
                buffer.append("[");
                buffer.append(resourcekey);
                buffer.append(KEY_PORT_STOP_BITS);
                buffer.append(EQUALS);

                if ((propertyData != null)
                    && (propertyData.getDataType().equals(Integer.class.getName()))
                    && (propertyData.getResource() != null))
                    {
                    buffer.append(propertyData.getResource());
                    }
                else
                    {
                    buffer.append(NO_DATA);
                    }
                buffer.append(TERMINATOR);

                propertyData = (PropertyPlugin)REGISTRY.getProperties().get(resourcekey + KEY_PORT_PARITY);
                buffer.append("\n");
                buffer.append(INDENT);
                buffer.append("[");
                buffer.append(resourcekey);
                buffer.append(KEY_PORT_PARITY);
                buffer.append(EQUALS);

                if ((propertyData != null)
                    && (propertyData.getDataType().equals(Integer.class.getName()))
                    && (propertyData.getResource() != null))
                    {
                    buffer.append(propertyData.getResource());
                    }
                else
                    {
                    buffer.append(NO_DATA);
                    }
                buffer.append(TERMINATOR);

                propertyData = (PropertyPlugin)REGISTRY.getProperties().get(resourcekey + KEY_PORT_FLOW_CONTROL);
                buffer.append("\n");
                buffer.append(INDENT);
                buffer.append("[");
                buffer.append(resourcekey);
                buffer.append(KEY_PORT_FLOW_CONTROL);
                buffer.append(EQUALS);

                if ((propertyData != null)
                    && (propertyData.getDataType().equals(String.class.getName()))
                    && (propertyData.getResource() != null))
                    {
                    buffer.append((String)propertyData.getResource());
                    }
                else
                    {
                    buffer.append(NO_DATA);
                    }
                buffer.append(TERMINATOR);

                break;
                }

            case SERIAL:
                {
                PropertyPlugin propertyData;

                propertyData = (PropertyPlugin)REGISTRY.getProperties().get(resourcekey + KEY_PORT_NAME);
                buffer.append("\n");
                buffer.append(INDENT);
                buffer.append("[");
                buffer.append(resourcekey);
                buffer.append(KEY_PORT_NAME);
                buffer.append(EQUALS);

                if ((propertyData != null)
                    && (propertyData.getDataType().equals(String.class.getName()))
                    && (propertyData.getResource() != null))
                    {
                    buffer.append((String)propertyData.getResource());
                    }
                else
                    {
                    buffer.append(NO_DATA);
                    }
                buffer.append(TERMINATOR);

                propertyData = (PropertyPlugin)REGISTRY.getProperties().get(resourcekey + KEY_PORT_BAUDRATE);
                buffer.append("\n");
                buffer.append(INDENT);
                buffer.append("[");
                buffer.append(resourcekey);
                buffer.append(KEY_PORT_BAUDRATE);
                buffer.append(EQUALS);

                if ((propertyData != null)
                    && (propertyData.getDataType().equals(Integer.class.getName()))
                    && (propertyData.getResource() != null))
                    {
                    buffer.append(propertyData.getResource());
                    }
                else
                    {
                    buffer.append(NO_DATA);
                    }
                buffer.append(TERMINATOR);

                propertyData = (PropertyPlugin)REGISTRY.getProperties().get(resourcekey + KEY_PORT_DATA_BITS);
                buffer.append("\n");
                buffer.append(INDENT);
                buffer.append("[");
                buffer.append(resourcekey);
                buffer.append(KEY_PORT_DATA_BITS);
                buffer.append(EQUALS);

                if ((propertyData != null)
                    && (propertyData.getDataType().equals(Integer.class.getName()))
                    && (propertyData.getResource() != null))
                    {
                    buffer.append(propertyData.getResource());
                    }
                else
                    {
                    buffer.append(NO_DATA);
                    }
                buffer.append(TERMINATOR);

                propertyData = (PropertyPlugin)REGISTRY.getProperties().get(resourcekey + KEY_PORT_STOP_BITS);
                buffer.append("\n");
                buffer.append(INDENT);
                buffer.append("[");
                buffer.append(resourcekey);
                buffer.append(KEY_PORT_STOP_BITS);
                buffer.append(EQUALS);

                if ((propertyData != null)
                    && (propertyData.getDataType().equals(Integer.class.getName()))
                    && (propertyData.getResource() != null))
                    {
                    buffer.append(propertyData.getResource());
                    }
                else
                    {
                    buffer.append(NO_DATA);
                    }
                buffer.append(TERMINATOR);

                propertyData = (PropertyPlugin)REGISTRY.getProperties().get(resourcekey + KEY_PORT_PARITY);
                buffer.append("\n");
                buffer.append(INDENT);
                buffer.append("[");
                buffer.append(resourcekey);
                buffer.append(KEY_PORT_PARITY);
                buffer.append(EQUALS);

                if ((propertyData != null)
                    && (propertyData.getDataType().equals(Integer.class.getName()))
                    && (propertyData.getResource() != null))
                    {
                    buffer.append(propertyData.getResource());
                    }
                else
                    {
                    buffer.append(NO_DATA);
                    }
                buffer.append(TERMINATOR);

                propertyData = (PropertyPlugin)REGISTRY.getProperties().get(resourcekey + KEY_PORT_FLOW_CONTROL);
                buffer.append("\n");
                buffer.append(INDENT);
                buffer.append("[");
                buffer.append(resourcekey);
                buffer.append(KEY_PORT_FLOW_CONTROL);
                buffer.append(EQUALS);

                if ((propertyData != null)
                    && (propertyData.getDataType().equals(String.class.getName()))
                    && (propertyData.getResource() != null))
                    {
                    buffer.append((String)propertyData.getResource());
                    }
                else
                    {
                    buffer.append(NO_DATA);
                    }
                buffer.append(TERMINATOR);

                break;
                }

            case STARINET:
                {
                buffer.append("\n");
                buffer.append(INDENT);
                buffer.append("[");
                buffer.append(NO_DATA);
                buffer.append(TERMINATOR);
                break;
                }

            case ETHERNET:
                {
                buffer.append("\n");
                buffer.append(INDENT);
                buffer.append("[");
                buffer.append(NO_DATA);
                buffer.append(TERMINATOR);
                break;
                }

            default:
                {
                buffer.append("\n");
                buffer.append(INDENT);
                buffer.append("[");
                buffer.append(NO_DATA);
                buffer.append(TERMINATOR);
                }
            }

        LOGGER.debug(debug, buffer.toString());
        }


    /***********************************************************************************************
     * Show the contents of the SerialConfigurationCellData UserObject.
     *
     * @param cell
     * @param celldata
     * @param message
     * @param debug
     */

    public static void debugCellData(final mxICell cell,
                                     final SerialConfigurationCellDataInterface celldata,
                                     final String message,
                                     final boolean debug)
        {
        final StringBuffer buffer;

        buffer = new StringBuffer();
        buffer.append("SerialConfiguration CellData ");
        buffer.append(message);

        if (cell != null)
            {
            buffer.append("\n");
            buffer.append(INDENT);
            buffer.append("[is.vertex=");
            buffer.append(cell.isVertex());
            buffer.append(TERMINATOR);

            buffer.append("\n");
            buffer.append(INDENT);
            buffer.append("[is.edge=");
            buffer.append(cell.isEdge());
            buffer.append(TERMINATOR);

            buffer.append("\n");
            buffer.append(INDENT);
            buffer.append("[edge.count=");
            buffer.append(cell.getEdgeCount());
            buffer.append(TERMINATOR);
            }

        if (celldata != null)
            {
            buffer.append("\n");
            buffer.append(INDENT);
            buffer.append("[cell.label=");
            buffer.append(celldata.getLabel());
            buffer.append(TERMINATOR);

            buffer.append("\n");
            buffer.append(INDENT);
            buffer.append("[node.type=");
            buffer.append(celldata.getNodeType().getName());
            buffer.append(TERMINATOR);

            buffer.append("\n");
            buffer.append(INDENT);
            buffer.append("[stream.type=");
            buffer.append(celldata.getStreamType().getName());
            buffer.append(TERMINATOR);

            buffer.append("\n");
            buffer.append(INDENT);
            buffer.append("[dao.port=");
            if (celldata.getDaoPort() != null)
                {
                buffer.append(celldata.getDaoPort().getName());
                }
            else
                {
                buffer.append("null");
                }
            buffer.append(TERMINATOR);

            buffer.append("\n");
            buffer.append(INDENT);
            buffer.append("[dao.port.is.open=");
            if (celldata.getDaoPort() != null)
                {
                buffer.append(celldata.getDaoPort().isPortOpen());
                }
            else
                {
                buffer.append("null");
                }
            buffer.append(TERMINATOR);

            buffer.append("\n");
            buffer.append(INDENT);
            buffer.append("[resourcekey=");
            buffer.append(celldata.getResourceKey());
            buffer.append(TERMINATOR);

            buffer.append("\n");
            buffer.append(INDENT);
            buffer.append("[is.changed=");
            buffer.append(celldata.isChanged());
            buffer.append(TERMINATOR);
            }

        LOGGER.debug(debug, buffer.toString());
        }
    }
