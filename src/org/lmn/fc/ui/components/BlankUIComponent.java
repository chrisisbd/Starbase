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

package org.lmn.fc.ui.components;

import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.FontInterface;

import java.awt.*;
import java.awt.geom.Rectangle2D;


/***************************************************************************************************
 * A BlankUIComponent to fill the visible panel when no real UI is available.
 */

public class BlankUIComponent extends UIComponent
    {
    private String strName;
    private ColourInterface colourCanvas;
    private ColourInterface colourGradientTop;
    private ColourInterface colourGradientBottom;
    private ColourInterface colourComponent;
    private FontInterface fontComponent;


    /***********************************************************************************************
     * BlankUIComponent.
     */

    public BlankUIComponent()
        {
        super();

        LOGGER.debugNavigation("NEW BlankUIComponent!");

        if (REGISTRY.isFrameworkLoaded())
            {
            strName = REGISTRY.getFramework().getName();
            }
        else
            {
            strName = EMPTY_STRING;
            }

        colourCanvas = DEFAULT_COLOUR_CANVAS;
        colourGradientTop = COLOUR_RAG_NIGHT;
        colourGradientBottom = COLOUR_RAG_SKY;

        // Set some defaults
        this.colourComponent = COLOUR_RAG_TEXT;
        this.fontComponent = FontInterface.DEFAULT_FONT_BANNER;
        }


    /***********************************************************************************************
     * BlankUIComponent.
     *
     * @param name
     */

    public BlankUIComponent(final String name)
        {
        super();

        strName = name;
        colourCanvas = DEFAULT_COLOUR_CANVAS;
        colourGradientTop = COLOUR_RAG_NIGHT;
        colourGradientBottom = COLOUR_RAG_SKY;

        // Set some defaults
        this.colourComponent = COLOUR_RAG_TEXT;
        this.fontComponent = FontInterface.DEFAULT_FONT_BANNER;
        }


    /***********************************************************************************************
     * BlankUIComponent.
     *
     * @param name
     * @param colourcanvas
     * @param colourtext
     */

    public BlankUIComponent(final String name,
                            final ColourInterface colourcanvas,
                            final ColourInterface colourtext)
        {
        super();

        strName = name;
        colourCanvas = colourcanvas;
        colourGradientTop = null;
        colourGradientBottom = null;

        // Set some defaults
        this.colourComponent = colourtext;
        this.fontComponent = FontInterface.DEFAULT_FONT_BANNER;
        }


    /***********************************************************************************************
     * Set the name of this UIComponent.
     *
     * @param name
     */

    public void setComponentName(final String name)
        {
        this.strName = name;
        }


    /***********************************************************************************************
     * Get the name of this UIComponent.
     *
     * @return String
     */

    public String getComponentName()
        {
        return (this.strName);
        }


    /***********************************************************************************************
     * Initialise this UIComponent.
     */

    public void initialiseUI()
        {
        super.initialiseUI();

        if (colourCanvas != null)
            {
            setBackground(colourCanvas.getColor());
            }
        }


    /***********************************************************************************************
     * Paint this Component.
     *
     * @param graphics
     */

    public void paint(final Graphics graphics)
        {
        super.paint(graphics);

        if (graphics != null)
            {
            // The original background colour cannot be seen if gradient paint is used
            if ((getGradientColourTop() != null)
                && (getGradientColourBottom() != null))
                {
                final GradientPaint gradientPaint;

                gradientPaint = new GradientPaint(getWidth() >> 1,
                                                  0,
                                                  getGradientColourTop().getColor(),
                                                  getWidth() >> 1,
                                                  getHeight(),
                                                  getGradientColourBottom().getColor());

                ((Graphics2D)graphics).setPaint(gradientPaint);
                ((Graphics2D)graphics).fill(new Rectangle(getWidth(), getHeight()));
                }
            else
                {
                final Graphics2D graphics2D;

                // Just fill the background with the Canvas colour
                graphics2D = (Graphics2D)graphics;
                graphics2D.setColor(getCanvasColour());
                graphics2D.fill(new Rectangle(getWidth(), getHeight()));
                }
            }

        // Show the name text over whatever background was painted
        if ((graphics != null)
            && (strName != null)
            && (getWidth() > 0)
            && (getHeight() > 0))
            {
            drawString(graphics, strName);
            }
        }


    /***********************************************************************************************
     * Draw some text.
     *
     * @param graphics
     * @param text
     */

    public void drawString(final Graphics graphics,
                           final String text)
        {
        final Graphics2D graphics2D;
        final FontMetrics metrics;
        final Rectangle2D rectangle2D;

        graphics2D = (Graphics2D)graphics;

        graphics2D.setColor(getComponentColour().getColor());
        graphics2D.setFont(getComponentFont().getFont());

        metrics = getFontMetrics(graphics2D.getFont());
        rectangle2D = metrics.getStringBounds(text, graphics);

        // For antialiasing text
        graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // The incoming Rectangle is the StringBounds of the Name text in the current Font
        // The baseline of the leftmost character is at position (x, y)
        graphics2D.drawString(text,
                              (getWidth() - (int) rectangle2D.getWidth()) >> 1,
                              (getHeight() + (int) rectangle2D.getHeight()) >> 1);
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
     * A utility so other classes can set canvas colour.
     *
     * @param colour
     */

    public final void setCanvasColour(final ColourInterface colour)
        {
        if (colour != null)
            {
            this.colourCanvas = colour;
            setBackground(colourCanvas.getColor());
            }
        }


    /***********************************************************************************************
     * Get the colour at the top of the gradient.
     *
     * @return ColourPlugin
     */

    public final ColourInterface getGradientColourTop()
        {
        return (this.colourGradientTop);
        }


    /***********************************************************************************************
     * Set the colour at the top of the gradient.
     *
     * @param colour
     */

    public final void setGradientColourTop(final ColourInterface colour)
        {
        colourGradientTop = colour;
        }


    /***********************************************************************************************
     * Get the colour at the bottom of the gradient.
     *
     * @return ColourPlugin
     */

    public final ColourInterface getGradientColourBottom()
        {
        return (this.colourGradientBottom);
        }


    /***********************************************************************************************
     * Set the colour at the bottom of the gradient.
     *
     * @param colour
     */

    public final void setGradientColourBottom(final ColourInterface colour)
        {
        colourGradientBottom = colour;
        }


    /***********************************************************************************************
     * Get the colour of this component.
     *
     * @return ColourPlugin
     */

    public ColourInterface getComponentColour()
        {
        return colourComponent;
        }


    /***********************************************************************************************
     * Set the colour of this component.
     *
     * @param colour
     */

    public void setComponentColour(final ColourInterface colour)
        {
        this.colourComponent = colour;
        }


    /***********************************************************************************************
     * Get the font of this component.
     *
     * @return FontPlugin
     */

    public FontInterface getComponentFont()
        {
        return fontComponent;
        }


    /***********************************************************************************************
     * Set the font of this component.
     *
     * @param font
     */

    public void setComponentFont(final FontInterface font)
        {
        this.fontComponent = font;
        }
    }
