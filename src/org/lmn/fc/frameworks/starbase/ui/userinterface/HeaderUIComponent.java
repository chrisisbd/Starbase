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

package org.lmn.fc.frameworks.starbase.ui.userinterface;

import org.lmn.fc.ui.components.BlankUIComponent;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;


/***************************************************************************************************
 * HeaderUIComponent, used in InstrumentUIComponentDecorator.
 */

public final class HeaderUIComponent extends BlankUIComponent
    {
    private static final int X_INSET = 25;

    private ImageIcon iconHeader;


    // ----------------------------------------
    //
    //       y......
    //       .     .       --------------
    // inset .width. inset |txttxttxttxt|
    //       .     .       y-------------
    //       .......
    //
    // ----------------------------------------


    /***********************************************************************************************
     * Create a Header panel for the InstrumentUIComponentDecorator.
     */

    public HeaderUIComponent()
        {
        super(EMPTY_STRING);

        this.iconHeader = null;
        }


    /***********************************************************************************************
     * Paint this Component.
     *
     * @param graphics
     */

    public void paint(final Graphics graphics)
        {
        // This will call drawString()
        super.paint(graphics);

        // Paint the Icon centred vertically, and inset from the left
        // The Icon is drawn with its top-left corner at (x, y)
        // x = 25
        // y = (height/2) - (iconheight/2)

        if ((graphics != null)
            && (iconHeader != null))
            {
            graphics.drawImage(iconHeader.getImage(),
                               X_INSET,
                               (getHeight() >> 1) - (iconHeader.getIconHeight() >> 1),
                               this);
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
        rectangle2D = metrics.getStringBounds(text, graphics2D);

        // For antialiasing text
        graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // The Rectangle is the StringBounds of the Name text in the current Font
        // The baseline of the leftmost character is at position (x, y)
        // x = (inset * 2) + (iconwidth)
        // y = (height - textheight)/2 + textheight

        // See: http://www.javafaq.nu/java-bookpage-13-8.html
        // The most reliable means of vertically centering text we found turned out to be baseline + ascent/4

        // The y coordinate of drawString specifies the baseline position
        // Ascent /3 works better than the obvious /2, or than the suggested /4
        graphics.drawString(getComponentName(),
                            (X_INSET << 1) + (iconHeader.getIconWidth()),
                            ((getHeight() >> 1) + ((int) metrics.getAscent() / 3)));
        }


    /***********************************************************************************************
     * Set the Header Text.
     *
     * @param text
     */

    public void setHeaderText(final String text)
        {
        setComponentName(text);
        }


    /***********************************************************************************************
     * Get the Header Icon.
     *
     * @return ImageIcon
     */

    public ImageIcon getHeaderIcon()
        {
        return (this.iconHeader);
        }


    /***********************************************************************************************
     * Set the Header Icon.
     *
     * @param icon
     */

    public void setHeaderIcon(final ImageIcon icon)
        {
        this.iconHeader = icon;
        }
    }
