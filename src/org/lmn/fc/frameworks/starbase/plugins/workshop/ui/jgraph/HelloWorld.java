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
package org.lmn.fc.frameworks.starbase.plugins.workshop.ui.jgraph;

import org.jgraph.JGraph;
import org.jgraph.graph.*;
import org.lmn.fc.frameworks.starbase.ui.dataprocessor.InputPort;
import org.lmn.fc.frameworks.starbase.ui.dataprocessor.OutputPort;
import org.lmn.fc.frameworks.starbase.ui.dataprocessor.PatchPanelPortView;
import org.lmn.fc.frameworks.starbase.ui.dataprocessor.PortLabelCell;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;


public class HelloWorld
    {
    private static final int WIDTH_VERTEX = 250;

    public static class MyModel extends DefaultGraphModel
        {
        // Override Superclass Method
        public boolean acceptsSource(Object edge, Object port)
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

        // Override Superclass Method
        public boolean acceptsTarget(Object edge, Object port)
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


    public static void main(String[] args)
        {
        GraphModel model = new MyModel();
        JGraph graph = new JGraph(model);

        graph.setPortsVisible(true);
        graph.setPortsScaled(true);
        graph.setHandleSize(4);
        graph.setGridEnabled(true);
        graph.setGridVisible(true);
        graph.setGridSize(10.0);
        graph.setGridColor(Color.blue);
        graph.setCloneable(true);

        // Enable edit without final RETURN keystroke
        graph.setInvokesStopCellEditing(true);

        // When over a cell, jump to its default port (we only have one, anyway)
        graph.setJumpToDefaultPort(true);

        graph.getGraphLayoutCache().setFactory(new DefaultCellViewFactory()
            {
            protected PortView createPortView(Object p)
                {
                return new PatchPanelPortView(p);
                }
            });

        DefaultGraphCell[] cells = new DefaultGraphCell[5];

        cells[0] = createSourceVertex("Data Logger", 50, 50, WIDTH_VERTEX, 75, new Color(153, 153, 255), true);
        cells[1] = createFunctionVertex("Time Constant", 400, 200, WIDTH_VERTEX, 75, new Color(204, 255, 102), true);
        cells[2] = createDisplayVertex("Chart", 800, 400, WIDTH_VERTEX, 75, new Color(255, 153, 153), true);

        DefaultEdge edge0 = new DefaultEdge();
        DefaultEdge edge1 = new DefaultEdge();

        // Fetch the ports from the new vertices, and connect them with the edges
        edge0.setSource(cells[0].getChildAt(0));
        edge0.setTarget(cells[1].getChildAt(0));

        edge1.setSource(cells[1].getChildAt(1));
        edge1.setTarget(cells[2].getChildAt(0));

        configureEdge(edge0);
        configureEdge(edge1);

        cells[3] = edge0;
        cells[4] = edge1;

        // Insert the cells via the cache, so they get selected
        graph.getGraphLayoutCache().insert(cells);

        // Show in Frame
        JFrame frame = new JFrame();
        frame.getContentPane().add(new JScrollPane(graph));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        }



    private static void configureEdge(DefaultEdge edge0)
        {
        GraphConstants.setLineWidth(edge0.getAttributes(), 2.0f);
        GraphConstants.setLineColor(edge0.getAttributes(), new Color(0, 153, 153));
        GraphConstants.setLineEnd(edge0.getAttributes(), GraphConstants.ARROW_CLASSIC);
        GraphConstants.setEndFill(edge0.getAttributes(), true);
        }



    public static DefaultGraphCell createSourceVertex(String name,
                                                double x,
                                                double y,
                                                double w,
                                                double h,
                                                Color bg,
                                                boolean raised)
        {
        // Create vertex with the given name
        DefaultGraphCell cell = createDefaultGraphCell(name, 0, 3);

        // Set bounds
        GraphConstants.setBounds(cell.getAttributes(), new Rectangle2D.Double(x, y, w, h));

        // Set fill color
        if (bg != null)
            {
            GraphConstants.setGradientColor(cell.getAttributes(), bg);
            GraphConstants.setOpaque(cell.getAttributes(), true);
            }

        // Set raised border
        if (raised)
            GraphConstants.setBorder(cell.getAttributes(), BorderFactory.createRaisedBevelBorder());
        else
        // Set black border
            GraphConstants.setBorderColor(cell.getAttributes(), Color.black);

        return cell;
        }

    public static DefaultGraphCell createFunctionVertex(String name,
                                                double x,
                                                double y,
                                                double w,
                                                double h,
                                                Color bg,
                                                boolean raised)
        {
        // Create vertex with the given name
        DefaultGraphCell cell = createDefaultGraphCell(name, 1, 1);

        // Set bounds
        GraphConstants.setBounds(cell.getAttributes(), new Rectangle2D.Double(x, y, w, h));

        // Set fill color
        if (bg != null)
            {
            GraphConstants.setGradientColor(cell.getAttributes(), bg);
            GraphConstants.setOpaque(cell.getAttributes(), true);
            }

        // Set raised border
        if (raised)
            GraphConstants.setBorder(cell.getAttributes(), BorderFactory.createRaisedBevelBorder());
        else
        // Set black border
            GraphConstants.setBorderColor(cell.getAttributes(), Color.black);

        return cell;
        }

    public static DefaultGraphCell createDisplayVertex(String name,
                                                double x,
                                                double y,
                                                double w,
                                                double h,
                                                Color bg,
                                                boolean raised)
        {
        // Create vertex with the given name
        DefaultGraphCell cell = createDefaultGraphCell(name, 4, 0);

        // Set bounds
        GraphConstants.setBounds(cell.getAttributes(), new Rectangle2D.Double(x, y, w, h));

        // Set fill color
        if (bg != null)
            {
            GraphConstants.setGradientColor(cell.getAttributes(), bg);
            GraphConstants.setOpaque(cell.getAttributes(), true);
            }

        // Set raised border
        if (raised)
            GraphConstants.setBorder(cell.getAttributes(), BorderFactory.createRaisedBevelBorder());
        else
        // Set black border
            GraphConstants.setBorderColor(cell.getAttributes(), Color.black);

        return cell;
        }

    public static DefaultGraphCell createDefaultGraphCell(String name, int ip, int op)
        {
        DefaultGraphCell cell = new PortLabelCell(name);

//        // Add a random number of ports on either side of the vertex
//        Random random = new Random();
//        double numLeftSidePorts = random.nextInt(6) + 2;
//        double numRightSidePorts = random.nextInt(6) + 2;

        double height = Math.max(ip, op);

        for (double i = 1.0; i <= height; i++)
            {
            if (i <= ip)
                {
                DefaultPort port = new InputPort("(0, GraphConstants.PERMILLE / " + new Double(i / (ip + 1)) + ")",
                                                 "INPUT");
                Point2D point = new Point2D.Double(0,
                                                   i * (GraphConstants.PERMILLE / (ip + 1)));
                GraphConstants.setOffset(port.getAttributes(), point);
                GraphConstants.setBackground(port.getAttributes(), Color.RED);
                cell.add(port);
                }

            if (i <= op)
                {
                DefaultPort port = new OutputPort("(GraphConstants.PERMILLE, GraphConstants.PERMILLE / " + new Double(i / (op + 1)) + ")",
                                                  "OUTPUT");
                Point2D point = new Point2D.Double(GraphConstants.PERMILLE,
                                                   i * (GraphConstants.PERMILLE / (op + 1)));
                GraphConstants.setOffset(port.getAttributes(), point);
                GraphConstants.setBackground(port.getAttributes(), Color.YELLOW);
                cell.add(port);
                }
            }

        return cell;
        }
    }