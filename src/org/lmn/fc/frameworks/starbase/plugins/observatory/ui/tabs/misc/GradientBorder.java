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

import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.ui.UIComponentPlugin;

import javax.swing.border.Border;
import java.awt.*;


/***************************************************************************************************
 * GradientBorder.
 */

public final class GradientBorder implements Border
    {
    private final ColourInterface colourCanvas;
    private final ColourInterface colourGradientTop;
    private final ColourInterface colourGradientBottom;
    private final Insets NO_INSETS;
    private final Rectangle rectangle;


    /***********************************************************************************************
     * Construct a Border with a gradient fill.
     */

    public GradientBorder()
        {
        rectangle = new Rectangle();
        NO_INSETS = new Insets(0, 0, 0, 0);
        colourCanvas = UIComponentPlugin.DEFAULT_COLOUR_CANVAS;
        colourGradientTop = UIComponentPlugin.DEFAULT_COLOUR_GRADIENT_TOP;
        colourGradientBottom = UIComponentPlugin.DEFAULT_COLOUR_GRADIENT_BOTTOM;
        }


    /***********************************************************************************
     * Paints the border for the specified component with the specified
     * position and size.
     *
     * @param component the component for which this border is being painted
     * @param graphics the paint graphics
     * @param x the x position of the painted border
     * @param y the y position of the painted border
     * @param width the width of the painted border
     * @param height the height of the painted border
     */

    public void paintBorder(final Component component,
                            final Graphics graphics,
                            final int x,
                            final int y,
                            final int width,
                            final int height)
        {
        final GradientPaint gradientPaint;

        // The original background colour cannot be seen if gradient paint is used
        if (graphics != null)
            {
            if ((getGradientColourTop() != null)
                && (getGradientColourBottom() != null))
                {
                gradientPaint = new GradientPaint(width >> 1,
                                                  0,
                                                  getGradientColourTop().getColor(),
                                                  width >> 1,
                                                  height,
                                                  getGradientColourBottom().getColor());

                ((Graphics2D)graphics).setPaint(gradientPaint);
                ((Graphics2D)graphics).fill(new Rectangle(width, height));
                System.out.println("painted gradient");
                }

            // See if any Components are on the Container, which need to be painted
            if (component instanceof Container)
                {
                final Container container;

                container = (Container) component;

                for (int i = 0, n = container.getComponentCount(); i < n; i++)
                    {
                    final Component comp;
                    final Graphics graphicsTemp;

                    comp = container.getComponent(i);
                    comp.getBounds(rectangle);
                    graphicsTemp = graphics.create(rectangle.x, rectangle.y,
                                                   rectangle.width, rectangle.height);
                    comp.paint(graphicsTemp);
                    graphicsTemp.dispose();
                    }
                System.out.println("painted buttons");
                }
            }
        }


    /***********************************************************************************************
     * Returns the insets of the border.
     *
     * @param component the component for which this border insets value applies
     *
     * @return Insets
     */

    public Insets getBorderInsets(final Component component)
        {
        return (NO_INSETS);
        }


    /***********************************************************************************
     * Returns whether or not the border is opaque.
     * If the border is opaque, it is responsible for filling in its own
     * background when painting.
     *
     * @return boolean
     */

    public boolean isBorderOpaque()
        {
        return (true);
        }


    /***********************************************************************************************
     * A utility so other classes can get canvas colour.
     *
     * @return Color
     */

    public final Color getCanvasColour()
        {
        return (this.colourCanvas.getColor());
        }


    /***********************************************************************************************
     * Get the Colour of the top of the gradient paint.
     *
     * @return ColourPlugin
     */

    private ColourInterface getGradientColourTop()
        {
        return (this.colourGradientTop);
        }


    /***********************************************************************************************
     * Get the Colour of the bottom of the gradient paint.
     *
     * @return ColourPlugin
     */

    private ColourInterface getGradientColourBottom()
        {
        return (this.colourGradientBottom);
        }
    }
