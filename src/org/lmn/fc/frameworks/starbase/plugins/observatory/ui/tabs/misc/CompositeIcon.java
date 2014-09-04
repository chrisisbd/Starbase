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

import javax.swing.*;
import java.awt.*;


/**
 * CompositeIcon is an Icon implementation which draws two icons with a specified relative position:
 * LEFT, RIGHT, TOP, BOTTOM specify how icon1 is drawn relative to icon2
 * CENTER: icon1 is drawn first, icon2 is drawn over it
 *
 * and with horizontal and vertical orientations within the alloted space
 *
 * It's useful with VerticalTextIcon when you want an icon with your text:
 * if icon1 is the graphic icon and icon2 is the VerticalTextIcon, you get a similar effect
 * to a JLabel with a graphic icon and text
 */
public final class CompositeIcon implements Icon,
                                            SwingConstants
    {
    private final Icon fIcon1;
    private final Icon fIcon2;
    private final int fPosition;
    private final int fHorizontalOrientation;
    private final int fVerticalOrientation;


    /**
     * Create a CompositeIcon from the specified Icons,
     * using the default relative position (icon1 above icon2)
     * and orientations (centered horizontally and vertically)
     */
    public CompositeIcon(final Icon icon1,
                         final Icon icon2)
        {
        this(icon1,
             icon2,
             TOP);
        }


    /**
     * Create a CompositeIcon from the specified Icons,
     * using the specified relative position
     * and default orientations (centered horizontally and vertically)
     */
    private CompositeIcon(final Icon icon1,
                          final Icon icon2,
                          final int position)
        {
        this(icon1,
             icon2,
             position,
             CENTER,
             CENTER);
        }


    /**
     * Create a CompositeIcon from the specified Icons,
     * using the specified relative position
     * and orientations
     */
    private CompositeIcon(final Icon icon1,
                          final Icon icon2,
                          final int position,
                          final int horizontalOrientation,
                          final int verticalOrientation)
        {
        fIcon1 = icon1;
        fIcon2 = icon2;
        fPosition = position;
        fHorizontalOrientation = horizontalOrientation;
        fVerticalOrientation = verticalOrientation;
        }


    /**
     * Draw the icon at the specified location.  Icon implementations
     * may use the Component argument to get properties useful for
     * painting, e.g. the foreground or background color.
     */
    public void paintIcon(final Component component,
                          final Graphics graphics,
                          final int x,
                          final int y)
        {
        final int width = getIconWidth();
        final int height = getIconHeight();

        if (fPosition == LEFT || fPosition == RIGHT)
            {
            final Icon leftIcon;
            final Icon rightIcon;

            if (fPosition == LEFT)
                {
                leftIcon = fIcon1;
                rightIcon = fIcon2;
                }
            else
                {
                leftIcon = fIcon2;
                rightIcon = fIcon1;
                }

            // "Left" orientation, because we specify the x position
            paintIcon(component,
                      graphics,
                      leftIcon,
                      x,
                      y,
                      width,
                      height,
                      LEFT,
                      fVerticalOrientation);

            paintIcon(component,
                      graphics,
                      rightIcon,
                      x + leftIcon.getIconWidth(),
                      y,
                      width,
                      height,
                      LEFT,
                      fVerticalOrientation);
            }
        else if (fPosition == TOP || fPosition == BOTTOM)
            {
            final Icon topIcon;
            final Icon bottomIcon;

            if (fPosition == TOP)
                {
                topIcon = fIcon1;
                bottomIcon = fIcon2;
                }
            else
                {
                topIcon = fIcon2;
                bottomIcon = fIcon1;
                }

            // "Top" orientation, because we specify the y position
            paintIcon(component,
                      graphics,
                      topIcon,
                      x,
                      y,
                      width,
                      height,
                      fHorizontalOrientation,
                      TOP);

            paintIcon(component,
                      graphics,
                      bottomIcon,
                      x,
                      y + topIcon.getIconHeight(),
                      width,
                      height,
                      fHorizontalOrientation,
                      TOP);
            }
        else
            {
            paintIcon(component,
                      graphics,
                      fIcon1,
                      x,
                      y,
                      width,
                      height,
                      fHorizontalOrientation,
                      fVerticalOrientation);

            paintIcon(component,
                      graphics,
                      fIcon2,
                      x,
                      y,
                      width,
                      height,
                      fHorizontalOrientation,
                      fVerticalOrientation);
            }
        }


    /* Paints one icon in the specified rectangle with the given orientations
     */
    private void paintIcon(final Component c,
                           final Graphics g,
                           final Icon icon,
                           final int x,
                           final int y,
                           final int width,
                           final int height,
                           final int horizontalOrientation,
                           final int verticalOrientation)
        {

        final int xIcon;
        final int yIcon;

//        g.setColor(Color.red);
//        g.drawRect(x, y, width, height);

        switch (horizontalOrientation)
            {
            case LEFT:
                xIcon = x;
                break;
            case RIGHT:
                xIcon = x + width - icon.getIconWidth();
                break;
            default:
                xIcon = x + ((width - icon.getIconWidth()) >> 1);
                break;
            }

        switch (verticalOrientation)
            {
            case TOP:
                yIcon = y;
                break;
            case BOTTOM:
                yIcon = y + height - icon.getIconHeight();
                break;
            default:
                yIcon = y + ((height - icon.getIconHeight()) >> 1);
                break;
            }

        icon.paintIcon(c,
                       g,
                       xIcon,
                       yIcon);
        }


    /**
     * Returns the icon's width.
     *
     * @return an int specifying the fixed width of the icon.
     */
    public int getIconWidth()
        {
        if (fPosition == LEFT || fPosition == RIGHT)
            {
            return fIcon1.getIconWidth() + fIcon2.getIconWidth();
            }

        return Math.max(fIcon1.getIconWidth(),
                        fIcon2.getIconWidth());
        }


    /**
     * Returns the icon's height.
     *
     * @return an int specifying the fixed height of the icon.
     */
    public int getIconHeight()
        {
        if (fPosition == TOP || fPosition == BOTTOM)
            {
            return fIcon1.getIconHeight() + fIcon2.getIconHeight();
            }

        return Math.max(fIcon1.getIconHeight(), fIcon2.getIconHeight());
        }
    }