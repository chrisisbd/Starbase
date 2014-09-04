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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.misc;

import org.jgraph.JGraph;
import org.jgraph.graph.*;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryUIInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.InstrumentUIComponentDecorator;
import org.lmn.fc.frameworks.starbase.ui.dataprocessor.InputPort;
import org.lmn.fc.frameworks.starbase.ui.dataprocessor.OutputPort;
import org.lmn.fc.frameworks.starbase.ui.dataprocessor.PatchPanelPortView;
import org.lmn.fc.frameworks.starbase.ui.dataprocessor.PortLabelCell;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.model.xmlbeans.instruments.DAO;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/***************************************************************************************************
 * PatchPanelUIComponent.
 */

public final class PatchPanelUIComponent extends InstrumentUIComponentDecorator
    {
    private static final int WIDTH_VERTEX = 250;
    private static final int HEIGHT_PORT = 25;
    private static final double SIZE_GRID = 10.0;
    private static final int SIZE_HANDLE = 4;
    private static final Color COLOR_GRID = Color.blue;
    private static final Color COLOR_VERTEX_SOURCE = new Color(153, 153, 255);
    private static final boolean RAISED = true;
    private static final Color COLOR_PORT_INPUT = Color.RED;
    private static final Color COLOR_PORT_OUTPUT = Color.YELLOW;


    /***********************************************************************************************
     * Create a SourceVertex.
     *
     * @param dao
     * @param inputs
     * @param outputs
     * @param x
     * @param y
     * @param width
     * @param height
     * @param color
     * @param raised
     *
     * @return DefaultGraphCell
     */

    private static DefaultGraphCell createSourceVertex(final DAO dao,
                                                       final int inputs,
                                                       final int outputs,
                                                       final double x,
                                                       final double y,
                                                       final double width,
                                                       final double height,
                                                       final Color color,
                                                       final boolean raised)
        {
        final DefaultGraphCell cell;

        // Create vertex with the given name
        cell = createDefaultGraphCell(dao, inputs, outputs);

        // Set bounds
        GraphConstants.setBounds(cell.getAttributes(),
                                 new Rectangle2D.Double(x, y, width, height));
        // Set fill color
        if (color != null)
            {
            GraphConstants.setGradientColor(cell.getAttributes(), color);
            GraphConstants.setOpaque(cell.getAttributes(), true);
            }

        if (raised)
            {
            // Set raised border
            GraphConstants.setBorder(cell.getAttributes(), BorderFactory.createRaisedBevelBorder());
            }
        else
            {
            // Set black border
            GraphConstants.setBorderColor(cell.getAttributes(), Color.black);
            }

        return (cell);
        }


    /***********************************************************************************************
     * Create a FunctionVertex.
     *
     * @param dao
     * @param inputs
     * @param outputs
     * @param x
     * @param y
     * @param width
     * @param height
     * @param color
     * @param raised
     *
     * @return DefaultGraphCell
     */

    private static DefaultGraphCell createFunctionVertex(final DAO dao,
                                                         final int inputs,
                                                         final int outputs,
                                                         final double x,
                                                         final double y,
                                                         final double width,
                                                         final double height,
                                                         final Color color,
                                                         final boolean raised)
        {
        final DefaultGraphCell cell;

        // Create vertex with the given name
        cell = createDefaultGraphCell(dao, inputs, outputs);

        // Set bounds
        GraphConstants.setBounds(cell.getAttributes(), new Rectangle2D.Double(x, y, width, height));

        // Set fill color
        if (color != null)
            {
            GraphConstants.setGradientColor(cell.getAttributes(), color);
            GraphConstants.setOpaque(cell.getAttributes(), true);
            }

        if (raised)
            {
            // Set raised border
            GraphConstants.setBorder(cell.getAttributes(), BorderFactory.createRaisedBevelBorder());
            }
        else
            {
            // Set black border
            GraphConstants.setBorderColor(cell.getAttributes(), Color.black);
            }

        return (cell);
        }


    /***********************************************************************************************
     * Create a RendererVertex.
     *
     * @param dao
     * @param inputs
     * @param outputs
     * @param x
     * @param y
     * @param w
     * @param h
     * @param bg
     * @param raised
     *
     * @return DefaultGraphCell
     */

    private static DefaultGraphCell createRendererVertex(final DAO dao,
                                                         final int inputs,
                                                         final int outputs,
                                                         final double x,
                                                         final double y,
                                                         final double w,
                                                         final double h,
                                                         final Color bg,
                                                         final boolean raised)
        {
        final DefaultGraphCell cell;

        // Create vertex with the given name
        cell = createDefaultGraphCell(dao, inputs, outputs);

        // Set bounds
        GraphConstants.setBounds(cell.getAttributes(), new Rectangle2D.Double(x, y, w, h));

        // Set fill color
        if (bg != null)
            {
            GraphConstants.setGradientColor(cell.getAttributes(), bg);
            GraphConstants.setOpaque(cell.getAttributes(), true);
            }

        if (raised)
            {
            // Set raised border
            GraphConstants.setBorder(cell.getAttributes(), BorderFactory.createRaisedBevelBorder());
            }
        else
            {
            // Set black border
            GraphConstants.setBorderColor(cell.getAttributes(), Color.black);
            }

        return (cell);
        }


    /***********************************************************************************************
     * Create a DefaultGraphCell.
     *
     * @param dao
     * @param inputs
     * @param outputs
     *
     * @return DefaultGraphCell
     */

    private static DefaultGraphCell createDefaultGraphCell(final DAO dao,
                                                           final int inputs,
                                                           final int outputs)
        {
        final DefaultGraphCell cell;
        final int maxports;

        cell = new PortLabelCell(dao.getName());
        maxports = Math.max(inputs, outputs);

        for (int i = 1; i <= maxports; i++)
            {
            // Add all available Inputs
            if (i <= inputs)
                {
                final DefaultPort port;
                final Point2D point;

                port = new InputPort("InputUserObject",
                                     dao.getInputList().get(i-1).getName());
                point = new Point2D.Double(0,
                                           i * (GraphConstants.PERMILLE / ((double)inputs + 1)));

                GraphConstants.setOffset(port.getAttributes(), point);
                GraphConstants.setBackground(port.getAttributes(), COLOR_PORT_INPUT);
                cell.add(port);
                }

            // Add all available Outputs
            if (i <= outputs)
                {
                final DefaultPort port;
                final Point2D point;

                port = new OutputPort("OutputUserObject",
                                      dao.getOutputList().get(i-1).getName());
                point = new Point2D.Double(GraphConstants.PERMILLE,
                                           i * (GraphConstants.PERMILLE / ((double)outputs + 1)));

                GraphConstants.setOffset(port.getAttributes(), point);
                GraphConstants.setBackground(port.getAttributes(), COLOR_PORT_OUTPUT);
                cell.add(port);
                }
            }

        return (cell);
        }


    /***********************************************************************************************
     * Configure the specified Edge.
     *
     * @param edge
     */

    private static void configureEdge(final DefaultEdge edge)
        {
        GraphConstants.setLineWidth(edge.getAttributes(), 2.0f);
        GraphConstants.setLineColor(edge.getAttributes(), new Color(0, 153, 153));
        GraphConstants.setLineEnd(edge.getAttributes(), GraphConstants.ARROW_CLASSIC);
        GraphConstants.setEndFill(edge.getAttributes(), true);
        }


    /***********************************************************************************************
     * Construct a PatchPanelUIComponent.
     *
     * @param hostinstrument
     * @param instrumentxml
     * @param hostui
     * @param task
     * @param font
     * @param colour
     * @param resourcekey
     */

    public PatchPanelUIComponent(final ObservatoryInstrumentInterface hostinstrument,
                                 final Instrument instrumentxml,
                                 final ObservatoryUIInterface hostui,
                                 final TaskPlugin task,
                                 final FontInterface font,
                                 final ColourInterface colour,
                                 final String resourcekey)
        {
        super(hostinstrument,
              instrumentxml,
              hostui,
              task,
              font,
              colour,
              resourcekey, 1);
        }



    /***********************************************************************************************
     * Initialise this UIComponent.
     */

    public final void initialiseUI()
        {
        final GraphModel model;
        final JGraph graph;

        model = new PatchPanelModel();
        graph = new JGraph(model);

        graph.setPortsVisible(true);
        graph.setPortsScaled(true);
        graph.setHandleSize(SIZE_HANDLE);
        graph.setGridEnabled(true);
        graph.setGridVisible(true);
        graph.setGridSize(SIZE_GRID);
        graph.setGridColor(COLOR_GRID);
        graph.setCloneable(true);

        // Enable edit without final RETURN keystroke
        graph.setInvokesStopCellEditing(true);

        // When over a cell, jump to its default port (we only have one, anyway)
        graph.setJumpToDefaultPort(true);

        // Modify the way we draw Ports
        graph.getGraphLayoutCache().setFactory(new DefaultCellViewFactory()
            {
            protected PortView createPortView(final Object p)
                {
                return (new PatchPanelPortView(p));
                }
            });

        // Add one SourceVertex for each DAO for each Instrument
        if ((getObservatoryUI() !=  null)
            && (getObservatoryUI().getInstrumentsDoc() != null)
//            && (XmlBeansUtilities.isValidXml(getObservatoryUI().getInstrumentsDoc()))
            && (getObservatoryUI().getInstrumentsDoc().getInstruments() != null))
            {
            final List<DefaultGraphCell> listCells;
            final List<Instrument> listInstruments;
            final Iterator<Instrument> iterInstruments;
            final int intX;
            int intY;

            listCells = new ArrayList<DefaultGraphCell>(10);
            intX = 25;
            intY = 25;

            listInstruments = getObservatoryUI().getInstrumentsDoc().getInstruments().getInstrumentList();
            iterInstruments = listInstruments.iterator();

            while (iterInstruments.hasNext())
                {
                final Instrument instrument;

                instrument = iterInstruments.next();

                if ((instrument != null)
                    && (instrument.getDAO() != null))
                    {
                    final DAO dao;
                    final int intInputCount;
                    final int intOutputCount;
                    int intHeight;

                    dao = instrument.getDAO();

                    if (dao.getInputList() != null)
                        {
                        intInputCount = dao.getInputList().size();
                        }
                    else
                        {
                        intInputCount = 0;
                        }

                    if (dao.getOutputList() != null)
                        {
                        intOutputCount = dao.getOutputList().size();
                        }
                    else
                        {
                        intOutputCount = 0;
                        }

                    intHeight = Math.max(intInputCount, intOutputCount);
                    intHeight = intHeight * HEIGHT_PORT;

                    // If there are no Inputs and no Outputs, it is not much of a DAO!
                    if (intHeight > 0)
                        {
                        listCells.add(createSourceVertex(dao,
                                                         intInputCount,
                                                         intOutputCount,
                                                         intX,
                                                         intY,
                                                         WIDTH_VERTEX,
                                                         intHeight,
                                                         COLOR_VERTEX_SOURCE,
                                                         RAISED));
                        intY = intY + intHeight + 20;
                        }
                    }
                }

            // Add one RendererVertex for each Output DAO in the DataProcessor



            // Insert the cells via the cache, so they get selected
            graph.getGraphLayoutCache().insert(listCells.toArray());

            // The host UIComponent uses BorderLayout
            add(new JScrollPane(graph), BorderLayout.CENTER);
            }
        }


    /***********************************************************************************************
     * The GraphModel for the PatchPanel.
     */

    public static class PatchPanelModel extends DefaultGraphModel
        {
        /*******************************************************************************************
         * Check to see if the specified Edge can be connected to the specified Port.
         *
         * @param edge
         * @param port
         *
         * @return boolean
         */

        public boolean acceptsSource(final Object edge,
                                     final Object port)
            {
            boolean boolAccept;

            boolAccept = !(((Edge) edge).getTarget().equals(port));
            boolAccept = boolAccept && (port instanceof OutputPort);

//            if (boolAccept)
//                {
//                System.out.println("ACCEPT SOURCE label=" + ((OutputPort)port).getPortLabel());
//                }
            return (boolAccept);
            }


        /*******************************************************************************************
         * Check to see if the specified Edge can be connected to the specified Port.
         *
         * @param edge
         * @param port
         *
         * @return boolean
         */

        public boolean acceptsTarget(final Object edge,
                                     final Object port)
            {
            boolean boolAccept;

            boolAccept = !(((Edge) edge).getSource().equals(port));
            boolAccept = boolAccept && (port instanceof InputPort);

//            if (boolAccept)
//                {
//                System.out.println("ACCEPT TARGET label=" + ((InputPort)port).getPortLabel());
//                }
            return (boolAccept);
            }
        }
    }



 // Fetch the ports from the new vertices, and connect them with the edges
//cells[1] = createFunctionVertex("Time Constant", 400, 200, WIDTH_VERTEX, 75, new Color(204, 255, 102), true);
//cells[2] = createRendererVertex("Chart", 800, 400, WIDTH_VERTEX, 75, new Color(255, 153, 153), true);
//final DefaultEdge edge0 = new DefaultEdge();
//final DefaultEdge edge1 = new DefaultEdge();

//        edge0.setSource(cells[0].getChildAt(0));
//        edge0.setTarget(cells[1].getChildAt(0));
//
//        edge1.setSource(cells[1].getChildAt(1));
//        edge1.setTarget(cells[2].getChildAt(0));
//
//        configureEdge(edge0);
//        configureEdge(edge1);
//
//        cells[3] = edge0;
//        cells[4] = edge1;