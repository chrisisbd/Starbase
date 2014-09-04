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

import org.lmn.fc.model.registry.RegistryModelUtilities;
import org.lmn.fc.ui.components.BlankUIComponent;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;


/***************************************************************************************************
 * StarbaseBrandingUIComponent.
 */

public final class StarbaseBrandingUIComponent extends BlankUIComponent
    {
    private final ImageIcon iconUKRAA;


    /***********************************************************************************************
     * Create a branding panel for Starbase.
     */

    public StarbaseBrandingUIComponent()
        {
        super(VERSION_BRANDING
              + REGISTRY.getFramework().getVersionNumber()
              + DOT
              + REGISTRY.getFramework().getBuildNumber()
              + " (" + REGISTRY.getFramework().getBuildStatus() + ")");

        iconUKRAA = RegistryModelUtilities.getAtomIcon(REGISTRY.getFramework(),
                                                       UKRAA_LOGO_FILENAME);
        }


    /***********************************************************************************************
     * Paint this Component.
     *
     * @param graphics
     */

    public void paint(final Graphics graphics)
        {
        super.paint(graphics);

        if ((graphics != null)
            && (iconUKRAA != null))
            {
            graphics.drawImage(iconUKRAA.getImage(),
                               (getWidth() >> 1) - (iconUKRAA.getIconWidth() >> 1),
                               (getHeight() >> 1) - (iconUKRAA.getIconHeight() >> 1),
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
        // Offset to below the Icon
        graphics2D.drawString(text,
                              ((getWidth() - (int) rectangle2D.getWidth()) >> 1),
                              (getHeight() + (int) rectangle2D.getHeight() + iconUKRAA.getIconHeight()) >> 1);
        }
    }
