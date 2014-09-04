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

import org.jgraph.JGraph;
import org.jgraph.graph.CellView;
import org.jgraph.graph.CellViewRenderer;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.PortView;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;


public class OutputPortRenderer extends JComponent
                                implements CellViewRenderer,
                                           Serializable
    {
    /** Cache the current graph for drawing */
    // protected transient JGraph graph;
    /**
     * Cache the current edgeview for drawing.
     */
    protected transient PortView view;

    /**
     * Cache the current graph background.
     */
    protected Color graphBackground = Color.white;

    /**
     * Cached hasFocus and selected value.
     */
    transient protected boolean hasFocus, selected, preview, xorEnabled;

    /**
     * Constructs a renderer that may be used to render ports.
     */

    public OutputPortRenderer()
        {
        setForeground(Color.gray);
        setBackground(Color.cyan);
        }


    /***********************************************************************************************
     * Configure and return the renderer based on the passed in components. The
     * value is typically set from messaging the graph with
     * <code>convertValueToString</code>.
     *
     * @param graph   the graph that that defines the rendering context.
     * @param cellview    the cell view that should be rendered.
     * @param sel     whether the object is selected.
     * @param focus   whether the object has the focus.
     * @param pv whether we are drawing a preview.
     *
     * @return the component used to render the value.
     */

    public Component getRendererComponent(final JGraph graph,
                                          final CellView cellview,
                                          final boolean sel,
                                          final boolean focus,
                                          final boolean pv)
        {
        // Check type
        if ((cellview instanceof PortView)
            && (graph != null))
            {
            graphBackground = graph.getBackground();
            this.view = (PortView) cellview;
            this.xorEnabled = graph.isXorEnabled();

            // Basic cell states
            this.hasFocus = focus;
            this.selected = sel;
            this.preview = pv;

            return this;
            }

        return null;
        }


    /***********************************************************************************************
     * Paint the renderer. Overrides superclass paint to add specific painting.
     * Note: The preview flag is interpreted as "highlight" in this context.
     * (This is used to highlight the port if the mouse is over it.)
     */

    public void paint(final Graphics graphics)
        {
        if (xorEnabled)
            {
            graphics.setColor(graphBackground);
            graphics.setXORMode(graphBackground);
            }

        super.paint(graphics);

        // The graphics context is at the top-left corner of the Port BOUNDS
        // i.e. the text label AND the Port pad
        // The Location of a Port gives its Centre (x)
        // The Bounds give the (x,y) of the top-left corner
        //
        // OutputPort
        //             -------------------------
        //                                     |
        //           ....................... ..|..
        //           .         label       . . x .
        //           ....................... ..| .
        //                                     |
        //

        if (preview)
            {
            graphics.fill3DRect(PatchPanelPortView.WIDTH_PORT_LABEL + PatchPanelPortView.WIDTH_PORT_LABEL_SEPARATOR,
                                0,
                                PatchPanelPortView.PORT_SIZE,
                                PatchPanelPortView.PORT_SIZE,
                                true);
            }
        else
            {
            graphics.fillRect(PatchPanelPortView.WIDTH_PORT_LABEL + PatchPanelPortView.WIDTH_PORT_LABEL_SEPARATOR,
                              0,
                              PatchPanelPortView.PORT_SIZE,
                              PatchPanelPortView.PORT_SIZE);
            }

        final boolean offset = (GraphConstants.getOffset(view.getAllAttributes()) != null);

        graphics.setColor(getForeground());

        if (!offset)
            {
            graphics.fillRect(PatchPanelPortView.WIDTH_PORT_LABEL + PatchPanelPortView.WIDTH_PORT_LABEL_SEPARATOR + 1,
                              1,
                              PatchPanelPortView.PORT_SIZE - 2,
                              PatchPanelPortView.PORT_SIZE - 2);
            }
        else if (!preview)
            {
            graphics.drawRect(PatchPanelPortView.WIDTH_PORT_LABEL + PatchPanelPortView.WIDTH_PORT_LABEL_SEPARATOR + 1,
                              1,
                              PatchPanelPortView.PORT_SIZE - 3,
                              PatchPanelPortView.PORT_SIZE - 3);
            }

        // Add a Port label if possible
        if (this.view.getCell() instanceof OutputPort)
            {
            final String strLabel;
            final FontMetrics metrics;
            final Rectangle2D bounds;

            strLabel = ((OutputPort)this.view.getCell()).getPortLabel();

            graphics.setFont(new Font(PatchPanelPortView.PORT_FONT_NAME,
                                      PatchPanelPortView.PORT_FONT_STYLE,
                                      PatchPanelPortView.PORT_FONT_SIZE));
            graphics.setColor(PatchPanelPortView.PORT_COLOR_OUTPUT);

            metrics = graphics.getFontMetrics();
            bounds = metrics.getStringBounds(strLabel, graphics);

            // Drawing occurs in the coordinate system of the BOUNDS
            graphics.drawString(strLabel,
                                PatchPanelPortView.WIDTH_PORT_LABEL - (int)bounds.getWidth(),
                                (int)bounds.getHeight() - metrics.getMaxDescent());
            }
        }


    /**********************************************************************************************/
    /* Mysterious overrides                                                                       */

    /**
     * Overridden for performance reasons. See the <a
     * href="#override">Implementation Note</a> for more information.
     */
    public void validate()
        {
        }

    /**
     * Overridden for performance reasons. See the <a
     * href="#override">Implementation Note</a> for more information.
     */
    public void revalidate()
        {
        }

    /**
     * Overridden for performance reasons. See the <a
     * href="#override">Implementation Note</a> for more information.
     */
    public void repaint(final long tm, final int x, final int y, final int width, final int height)
        {
        }

    /**
     * Overridden for performance reasons. See the <a
     * href="#override">Implementation Note</a> for more information.
     */
    public void repaint(final Rectangle r)
        {
        }

    /**
     * Overridden for performance reasons. See the <a
     * href="#override">Implementation Note</a> for more information.
     */
    protected void firePropertyChange(final String propertyName, final Object oldValue,
                                      final Object newValue)
        {
        // Strings get interned...
        if ("text".equals(propertyName))
            {
            super.firePropertyChange(propertyName, oldValue, newValue);
            }
        }

    /**
     * Overridden for performance reasons. See the <a
     * href="#override">Implementation Note</a> for more information.
     */
    public void firePropertyChange(final String propertyName, final byte oldValue,
                                   final byte newValue)
        {
        }

    /**
     * Overridden for performance reasons. See the <a
     * href="#override">Implementation Note</a> for more information.
     */
    public void firePropertyChange(final String propertyName, final char oldValue,
                                   final char newValue)
        {
        }

    /**
     * Overridden for performance reasons. See the <a
     * href="#override">Implementation Note</a> for more information.
     */
    public void firePropertyChange(final String propertyName, final short oldValue,
                                   final short newValue)
        {
        }

    /**
     * Overridden for performance reasons. See the <a
     * href="#override">Implementation Note</a> for more information.
     */
    public void firePropertyChange(final String propertyName, final int oldValue,
                                   final int newValue)
        {
        }

    /**
     * Overridden for performance reasons. See the <a
     * href="#override">Implementation Note</a> for more information.
     */
    public void firePropertyChange(final String propertyName, final long oldValue,
                                   final long newValue)
        {
        }

    /**
     * Overridden for performance reasons. See the <a
     * href="#override">Implementation Note</a> for more information.
     */
    public void firePropertyChange(final String propertyName, final float oldValue,
                                   final float newValue)
        {
        }

    /**
     * Overridden for performance reasons. See the <a
     * href="#override">Implementation Note</a> for more information.
     */
    public void firePropertyChange(final String propertyName, final double oldValue,
                                   final double newValue)
        {
        }

    /**
     * Overridden for performance reasons. See the <a
     * href="#override">Implementation Note</a> for more information.
     */
    public void firePropertyChange(final String propertyName, final boolean oldValue,
                                   final boolean newValue)
        {
        }
    }
