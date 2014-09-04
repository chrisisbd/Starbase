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

package org.lmn.fc.frameworks.starbase.ui.dataprocessor;

import org.jgraph.graph.CellViewRenderer;
import org.jgraph.graph.PortView;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;


/***********************************************************************************************
 * PatchPanelPortView.
 */

public class PatchPanelPortView extends PortView
    {
    public static final int HEIGHT_PORT_LABEL = 15;
    public static final int WIDTH_PORT_LABEL = 100;
    public static final int WIDTH_PORT_LABEL_SEPARATOR = 4;
    public static final int PORT_SIZE = HEIGHT_PORT_LABEL;
    public static final String PORT_FONT_NAME = "Monospaced";
    public static final int PORT_FONT_STYLE = Font.PLAIN;
    public static final int PORT_FONT_SIZE = 11;
    public static final Color PORT_COLOR_INPUT = Color.red;
    public static final Color PORT_COLOR_OUTPUT = Color.blue;

    private OutputPortRenderer rendererOutputPort;
    private InputPortRenderer rendererInputPort;


    /***********************************************************************************************
     * Construct a PatchPanelPortView.
     *
     * @param cell
     */

    public PatchPanelPortView(final Object cell)
        {
        super(cell);

        rendererOutputPort = new OutputPortRenderer();
        rendererInputPort = new InputPortRenderer();
        }


    /***********************************************************************************************
     * Returns the bounds for the port view.
     *
     * @return Rectangle2D
     */

    public Rectangle2D getBounds()
        {
//        if (portIcon != null)
//            {
//            int width = portIcon.getIconWidth();
//            int height = portIcon.getIconHeight();
//            }
//        return super.getBounds();


        final Point2D location = (Point2D) getLocation().clone();
        final Rectangle2D bounds = new Rectangle2D.Double();

        // The Location of a Port gives its Centre (x)
        // The Bounds give the (x,y) of the top-left corner
        //
        // InputPort
        //           -------------------------
        //           |
        //         ..|.. .......................
        //         . x . .    label            .
        //         ..|.. .......................
        //           |
        //
        // OutputPort
        //             -------------------------
        //                                     |
        //           ....................... ..|..
        //           .         label       . . x .
        //           ....................... ..| .
        //                                     |
        //

        if (getCell() instanceof InputPort)
            {
            bounds.setFrame(location.getX() - (PORT_SIZE >> 1),
                            location.getY() - (PORT_SIZE >> 1),
                            WIDTH_PORT_LABEL + WIDTH_PORT_LABEL_SEPARATOR + PORT_SIZE,
                            HEIGHT_PORT_LABEL);
            }
        else
            {
            bounds.setFrame(location.getX() - (PORT_SIZE >> 1) - WIDTH_PORT_LABEL_SEPARATOR - WIDTH_PORT_LABEL,
                            location.getY() - (PORT_SIZE >> 1),
                            WIDTH_PORT_LABEL + WIDTH_PORT_LABEL_SEPARATOR + PORT_SIZE,
                            HEIGHT_PORT_LABEL);
            }

        return (bounds);
        }


    /***********************************************************************************************
     * Get the CellViewRenderer.
     *
     * @return CellViewRenderer
     */

    public CellViewRenderer getRenderer()
        {
        if (getCell() instanceof OutputPort)
            {
            return (this.rendererOutputPort);
            }
        else
            {
            return (this.rendererInputPort);
            }
        }
    }
