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
import com.mxgraph.swing.handler.mxGraphHandler;
import com.mxgraph.swing.handler.mxKeyboardHandler;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.mxGraphOutline;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxEventSource;
import com.mxgraph.view.mxGraph;
import org.lmn.fc.common.constants.*;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.SerialConfigurationCellDataInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.SerialConfigurationUIComponentInterface;
import org.lmn.fc.model.resources.PropertyPlugin;

import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.Enumeration;


/***************************************************************************************************
 * SerialConfigurationGraphComponent.
 */

public class SerialConfigurationGraphComponent extends mxGraphComponent
                                               implements FrameworkConstants,
                                                          FrameworkStrings,
                                                          FrameworkMetadata,
                                                          FrameworkSingletons,
                                                          FrameworkRegex,
                                                          ResourceKeys
    {
    private static final long serialVersionUID = -1872784201029032068L;

    // Injections
    private final SerialConfigurationUIComponentInterface uiSerialConfig;
    private final boolean boolDebug;


    /***********************************************************************************************
     * Make sure all Serial Ports connect to Instruments with the same Port.Name.
     * Used by validateGraph().
     *
     * @param scui
     * @param debug
     */

    private static void reconnectSerialPorts(final SerialConfigurationUIComponentInterface scui,
                                             final boolean debug)
        {
        final String SOURCE = "SerialConfigurationGraphComponent.reconnectSerialPorts() ";

        LOGGER.debug(debug, SOURCE + "START");

        if (scui.getSerialPortCells() != null)
            {
            final Enumeration<mxICell> enumSerialPorts;

            enumSerialPorts = scui.getSerialPortCells().elements();

            while(enumSerialPorts.hasMoreElements())
                {
                final mxICell cellSerialPort;

                cellSerialPort = enumSerialPorts.nextElement();

                if ((cellSerialPort != null)
                    && (cellSerialPort.getValue() != null)
                    && (cellSerialPort.getValue() instanceof SerialConfigurationCellDataInterface))
                    {
                    final SerialConfigurationCellDataInterface configDataTarget;

                    configDataTarget = (SerialConfigurationCellDataInterface)cellSerialPort.getValue();

                    LOGGER.debug(debug,
                                 SOURCE + "Connecting a Source to SERIAL_PORT "+ configDataTarget.getLabel());

                    // Everything in the SerialPort Hashtable should have a UserData NodeType of SERIAL_PORT
                    if (SerialConfigurationNodeType.SERIAL_PORT.equals(configDataTarget.getNodeType()))
                        {
                        // getSource() and getTarget() are not in the Interface!!
                        // The graph must be valid, so look only for ONE Incoming Edge
                        if ((cellSerialPort.isVertex())
                            && (cellSerialPort.getEdgeCount() == 1)
                            && (((mxCell) cellSerialPort.getEdgeAt(0)).getSource() != null)
                            && (!((mxCell) cellSerialPort.getEdgeAt(0)).getSource().equals(cellSerialPort))
                            && (((mxCell) cellSerialPort.getEdgeAt(0)).getTarget() != null)
                            && (((mxCell) cellSerialPort.getEdgeAt(0)).getTarget().equals(cellSerialPort)))
                            {
                            final mxICell mxiEdge;
                            final mxICell cellSourceOfEdge;

                            // There should be only ONE Incoming Edge, connected to the Port source
                            mxiEdge = cellSerialPort.getEdgeAt(0);
                            cellSourceOfEdge = ((mxCell) mxiEdge).getSource();

                            // The Serial Port Source must also have a valid UserObject
                            if (cellSourceOfEdge.getValue() instanceof SerialConfigurationCellDataInterface)
                                {
                                final String strSourceResourceKey;
                                final PropertyPlugin propertyData;

                                // This event can't occur unless there is a valid ResourceKey selection
                                strSourceResourceKey = ((SerialConfigurationCellDataInterface) cellSourceOfEdge.getValue()).getResourceKey() + KEY_PORT_NAME;
                                propertyData = (PropertyPlugin)REGISTRY.getProperties().get(strSourceResourceKey);

                                if ((propertyData != null)
                                    && (propertyData.getDataType().equals(String.class.getName()))
                                    && (propertyData.getResource() != null)
                                    && (!propertyData.getResource().equals(configDataTarget.getLabel())))
                                    {
                                    final SerialConfigurationCellDataInterface configDataSource;

                                    configDataSource = (SerialConfigurationCellDataInterface)cellSourceOfEdge.getValue();

                                    // Make sure that the Edge Source Port.Name shows the Serial Port Label
                                    propertyData.setResource(configDataTarget.getLabel());
                                    LOGGER.debug(debug, SOURCE + "SERIAL: SET PROPERTY PORT.NAME TO " + configDataTarget.getLabel());

                                    // We changed the Source, because it's now connected to something else
                                    configDataSource.setChanged(true);

                                    // Copy the Source data to the Target data
                                    // Keep the Target Config Data in step with that of the cell's Source Config Data
                                    // Label and NodeType never change
                                    // Called via by validateGraph(), so don't validate again!

                                    // The SerialPort StreamType must change to match its incoming Source StreamType,
                                    // i.e. SERIAL or STARIBUS
                                    configDataTarget.setStreamType(configDataSource.getStreamType());

                                    configDataTarget.setDaoPort(configDataSource.getDaoPort());

                                    if (configDataSource.getDaoPort() != null)
                                        {
                                        configDataTarget.setOpen(configDataSource.getDaoPort().isPortOpen());
                                        }
                                    else
                                        {
                                        configDataTarget.setOpen(false);
                                        }

                                    configDataTarget.setResourceKey(configDataSource.getResourceKey());
                                    configDataTarget.setChanged(true);

                                    LOGGER.debug(debug,
                                                 SOURCE + "Connecting " + configDataSource.getLabel() + " to "+ configDataTarget.getLabel());
                                    SerialConfigurationHelper.debugCellData(cellSourceOfEdge,
                                                                            configDataSource,
                                                                            SOURCE + " Source Cell",
                                                                            debug);
                                    SerialConfigurationHelper.debugCellData(cellSerialPort,
                                                                            configDataTarget,
                                                                            SOURCE + " Target Cell",
                                                                            debug);
                                    }
                                else
                                    {
                                    LOGGER.debug(debug, SOURCE + "No change is possible, or required [label=" + configDataTarget.getLabel() + "]");
                                    }
                                }
                            else
                                {
                                LOGGER.debug(debug, SOURCE + "Invalid SerialPort Source UserObject");
                                }
                            }
                        else
                            {
                            LOGGER.debug(debug, SOURCE + "THE GRAPH IS CURRENTLY INVALID (SERIAL PORT)");
                            }
                        }
                    else
                        {
                        LOGGER.debug(debug, SOURCE + "SERIAL PORT HASHTABLE HAS USER DATA WITH INVALID NODETYPE " +
                                                "[node.type=" + configDataTarget.getNodeType().getName() + "]");
                        }
                    }
                else
                    {
                    LOGGER.debug(debug, SOURCE + "Invalid or NULL CellData UserObject");
                    }
                }
            }
        else
            {
            LOGGER.debug(debug, SOURCE + "There are no Serial Port cells");
            }

        LOGGER.debug(debug, SOURCE + "FINISH");
        }


    /***********************************************************************************************
     * Make sure the Unknown Port connects to Instruments with the same Port.Name.
     * Used by validateGraph().
     *
     * @param scui
     * @param debug
     */

    private static void reconnectUnknownPort(final SerialConfigurationUIComponentInterface scui,
                                             final boolean debug)
        {
        final String SOURCE = "SerialConfigurationGraphComponent.reconnectUnknownPort() ";

        LOGGER.debug(debug, SOURCE + "START");

        if ((scui.getUnknownPortCell() != null)
            && (scui.getUnknownPortCell().getValue() != null)
            && (scui.getUnknownPortCell().getValue() instanceof SerialConfigurationCellDataInterface))
            {
            final mxICell cellUnknownPort;
            final SerialConfigurationCellDataInterface configDataUnknown;

            cellUnknownPort = scui.getUnknownPortCell();
            configDataUnknown = (SerialConfigurationCellDataInterface)cellUnknownPort.getValue();

            if (SerialConfigurationNodeType.UNKNOWN_PORT.equals(configDataUnknown.getNodeType()))
                {
                // The Unknown Port can have any number of Incoming Edges, but zero is Ok
                if ((cellUnknownPort.isVertex())
                    && (cellUnknownPort.getEdgeCount() > 0))
                    {
                    for (int intEdgeIndex = 0;
                         intEdgeIndex < cellUnknownPort.getEdgeCount();
                         intEdgeIndex++)
                        {
                        LOGGER.debug(debug, SOURCE + "CHECKING UNKNOWN EDGE INDEX=" + intEdgeIndex);

                        // getSource() and getTarget() are not in the Interface!!
                        // The graph must be valid, so look only for Incoming Edges
                        if ((((mxCell) cellUnknownPort.getEdgeAt(intEdgeIndex)).getSource() != null)
                            && (!((mxCell) cellUnknownPort.getEdgeAt(intEdgeIndex)).getSource().equals(cellUnknownPort))
                            && (((mxCell) cellUnknownPort.getEdgeAt(intEdgeIndex)).getTarget() != null)
                            && (((mxCell) cellUnknownPort.getEdgeAt(intEdgeIndex)).getTarget().equals(cellUnknownPort)))
                            {
                            final mxICell mxiEdge;
                            final mxICell cellSourceOfEdge;

                            LOGGER.debug(debug, SOURCE + "UNKNOWN PORT 00000000000000000");

                            mxiEdge = cellUnknownPort.getEdgeAt(intEdgeIndex);
                            cellSourceOfEdge = ((mxCell) mxiEdge).getSource();

                            // The Unknown Port Source must also have a valid UserObject
                            if (cellSourceOfEdge.getValue() instanceof SerialConfigurationCellDataInterface)
                                {
                                final PropertyPlugin propertyData;

                                // This event can't occur unless there is a valid ResourceKey selection
                                propertyData = (PropertyPlugin)REGISTRY.getProperties().get(((SerialConfigurationCellDataInterface) cellSourceOfEdge.getValue()).getResourceKey() + KEY_PORT_NAME);

                                if ((propertyData != null)
                                    && (propertyData.getDataType().equals(String.class.getName()))
                                    && (propertyData.getResource() != null)
                                    && (!propertyData.getResource().equals(configDataUnknown.getLabel())))
                                    {
                                    final SerialConfigurationCellDataInterface configDataSource;

                                    configDataSource = (SerialConfigurationCellDataInterface)cellSourceOfEdge.getValue();

                                    LOGGER.debug(debug, SOURCE + "Unknown Port: Source UserData [port.name=" + configDataSource.getLabel()
                                                            + "] Source Registry PropertyData [port.name=" + propertyData.getResource() + "]");

                                    // Make sure that the Edge Source Port.Name shows the Unknown Port Label
                                    propertyData.setResource(configDataUnknown.getLabel());
                                    LOGGER.debug(debug, SOURCE + "Set Port.Name [port.name=" + configDataUnknown.getLabel() + "]");

                                    // We changed the Source, because it's now connected to something else
                                    configDataSource.setChanged(true);

                                    // Keep the Target Config Data in step with that of the cell's Source Config Data
                                    // Label, NodeType and StreamType never change
                                    // Called via by validateGraph(), so don't validate again!
                                    // The Unknown Port does not use the DaoPort
                                    configDataUnknown.setDaoPort(null);
                                    configDataUnknown.setOpen(false);
                                    // The Unknown Port does not use the ResourceKey
                                    configDataUnknown.setResourceKey("UNINITIALISED.");
                                    configDataUnknown.setChanged(true);

                                    SerialConfigurationHelper.debugCellData(cellSourceOfEdge,
                                                                            configDataSource,
                                                                            SOURCE + " Source Cell",
                                                                            debug);
                                    SerialConfigurationHelper.debugCellData(cellUnknownPort,
                                                                            configDataUnknown,
                                                                            SOURCE + " Target Cell",
                                                                            debug);
                                    }
                                else
                                    {
                                    LOGGER.debug(debug, SOURCE + "No change in Property, or invalid Property 222222222222222222222222222");
                                    }
                                }
                            else
                                {
                                LOGGER.debug(debug, SOURCE + "INVALID UNKNOWN PORT SOURCE USER OBJECT");
                                }
                            }
                        else
                            {
                            LOGGER.debug(debug, SOURCE + "THE UNKNOWN PORT HAS INVALID EDGE CONNECTIONS");
                            }
                        }
                    }
                else
                    {
                    LOGGER.debug(debug, SOURCE + "UNKNOWN PORT HAS ZERO EDGES, OR MAY BE INVALID");
                    }
                }
            else
                {
                LOGGER.debug(debug, SOURCE + "UNKNOWN PORT HAS USER DATA WITH INVALID NODETYPE");
                }
            }
        else
            {
            LOGGER.debug(debug, SOURCE + "The UnknownPort cell is invalid");
            }

        LOGGER.debug(debug, SOURCE + "FINISH");
        }


    /***********************************************************************************************
     * Construct a SerialConfigurationGraphComponent.
     *
     * @param scui
     * @param mxgraph
     * @param debug
     */

    public SerialConfigurationGraphComponent(final SerialConfigurationUIComponentInterface scui,
                                             final mxGraph mxgraph,
                                             final boolean debug)
        {
        super(mxgraph);

        // Injections
        this.uiSerialConfig = scui;
        this.boolDebug = debug;

        // Performance related settings
        setTripleBuffered(true);
        // setAntiAlias( false);
        // setTextAntiAlias( false);

        // Enables rubberband selection
        //new mxRubberband(this);
        new mxKeyboardHandler(this);

        // Installs automatic validation
        // (use editor.validation = true if you are using an mxEditor instance)
        mxgraph.getModel().addListener(mxEvent.CHANGE, new mxEventSource.mxIEventListener()
            {
            public void invoke(final Object sender,
                               final mxEventObject event)
                {
                clearCellOverlays();
                validateGraph();
                }
            });

        // Install listeners (e.g. mousewheel for zooming)
        installListeners();
        }


    /**********************************************************************************************
     * Validates the graph by validating each descendant of the given cell or
     * the root of the model. Context is an object that contains the validation
     * state for the complete validation run. The validation errors are attached
     * to their cells using <setWarning>.
     * This function returns NULL if no validation errors exist in the graph.
     */

    public String validateGraph()
        {
        final String strStatus;

        strStatus = super.validateGraph();

        // If the graph is valid, make sure all Ports show correct Names and Labels etc.
        if (strStatus == null)
            {
            reconnectSerialPorts(getSerialConfigUI(), isDebug());
            reconnectUnknownPort(getSerialConfigUI(), isDebug());
            }

        return (strStatus);
        }


    /***********************************************************************************************
     * Create the GraphHandler.
     * Do not allow cells to leave the parent cell area.
     *
     * @return mxGraphHandler
     */

    public mxGraphHandler createGraphHandler()
        {
        return new mxGraphHandler(this)
            {
            @Override protected boolean shouldRemoveCellFromParent(final Object parent,
                                                                   final Object[] arraycells,
                                                                   final MouseEvent event)
                {
                return (false);
                }
            };
        }


    /***********************************************************************************************
     * Install any Event Listeners.
     */

    private void installListeners()
        {
        // Zoom with mousewheel
        final MouseWheelListener wheelTracker = new MouseWheelListener()
            {
            public void mouseWheelMoved(final MouseWheelEvent event)
                {

                if (event.getSource() instanceof mxGraphOutline || event.isControlDown())
                    {
                    if (event.getWheelRotation() < 0)
                        {
                        zoomIn();
                        }
                    else
                        {
                        zoomOut();
                        }
                    }
                }

            };

        addMouseWheelListener(wheelTracker);
        }


    /**********************************************************************************************/
    /* Injections                                                                                 */
    /***********************************************************************************************
     * Get the Serial Config UI.
     *
     * @return SerialConfigurationUIComponentInterface
     */

    private SerialConfigurationUIComponentInterface getSerialConfigUI()
        {
        return (this.uiSerialConfig);
        }


    /***********************************************************************************************
     * Indicate if we are in debug mode.
     *
     * @return boolean
     */

    private boolean isDebug()
        {
        return (this.boolDebug);
        }
    }