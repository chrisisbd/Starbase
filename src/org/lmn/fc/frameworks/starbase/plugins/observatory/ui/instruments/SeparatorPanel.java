// Copyright 2000, 2001, 2002, 2003, 04, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013, 2013
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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments;

import org.lmn.fc.model.datatypes.types.FontDataType;

import java.awt.*;


/***************************************************************************************************
 * SeparatorPanel.
 */

public enum SeparatorPanel
    {
    // A thin Category separator within a Group in the rack
    CATEGORY         (1,
                      "SeparatorCategory.",
                      20,
                      RackPanel.PANEL_1U.getPixelWidth(),
                      false,
                      new FontDataType("font=dialog style=plain size=11").getFont()),

    // A medium plain separator, used e.g. in ObservatoryManager, with no text
    PLAIN            (2,
                      "SeparatorPlain.",
                      RackPanel.PANEL_1U.getPixelHeight() >> 1,
                      RackPanel.PANEL_1U.getPixelWidth(),
                      false,
                      new FontDataType("font=dialog style=plain size=1").getFont()),

    // A large separator, used as a Group header in the rack
    GROUP            (3,
                      "SeparatorGroup.",
                      RackPanel.PANEL_1U.getPixelHeight(),
                      RackPanel.PANEL_1U.getPixelWidth(),
                      false,
                      new FontDataType("font=dialog style=plain size=22").getFont());


    // SeparatorPanel dimensions
    private static final int SEPARATOR_PANEL_HEIGHT_MIN = 1;
    private static final int SEPARATOR_PANEL_HEIGHT_MAX = 3;

    private final int intUnitHeight;
    private final String strResourceKey;
    private final int intPixelHeight;
    private final int intPixelWidth;
    private final boolean boolGradientFill;
    private final Font fontSeparator;


    /***********************************************************************************************
     * Find the SeparatorPanel given the ResourceKey.
     * This is a bit of a bodge to avoid changing the schema...
     * Note that the XML may show a different SelectorPanelHeight
     * from that obtained from the SeparatorPanel derived via the ResourceKey.
     * The SelectorPanelHeight explicity stated in the XML must take precedence.
     *
     * @param key
     *
     * @return SeparatorMode
     */

    public static SeparatorPanel findSeparatorFromResourceKey(final String key)
        {
        SeparatorPanel separatorPanel;
        final SeparatorPanel[] panels;
        boolean boolFoundIt;

        // Default to a plain Separator
        separatorPanel = PLAIN;

//        System.out.println("Try to find Mode with key=" + key);

        panels = values();
        boolFoundIt = false;

        for (int i = 0;
             ((!boolFoundIt) && (i < panels.length));
             i++)
            {
            final SeparatorPanel panel;

            panel = panels[i];

            // The incoming Key is of the form Starbase.Observatory.SeparatorPlain.
            if (key.endsWith(panel.getResourceKey()))
                {
                separatorPanel = panel;
                boolFoundIt = true;
                }
//            else
//                {
//                System.out.println("Skipping Mode=" + mode.getResourceKey() + " ResourceKey=" + key);
//                }
            }

//        System.out.println("Return Mode=" + separatorMode.getResourceKey());

        return (separatorPanel);
        }


    /***********************************************************************************************
     * Look up the SeparatorPanel enumeration corresponding to the specified height.
     *
     * @param height
     *
     * @return SeparatorPanel
     */

    public static SeparatorPanel getSeparatorPanelForHeight(final int height)
        {
        SeparatorPanel separatorPanel;

        // Prepare a default value
        separatorPanel = PLAIN;

        if ((height >= SEPARATOR_PANEL_HEIGHT_MIN)
            && (height <= SEPARATOR_PANEL_HEIGHT_MAX))
            {
            final SeparatorPanel[] panels;
            boolean boolFound;

            panels = values();
            boolFound = false;

            for (int i = 0;
                 ((i < panels.length) && (!boolFound));
                 i++)
                {
                if (panels[i].getUnitHeight() == height)
                    {
                    separatorPanel = panels[i];
                    boolFound = true;
                    }
                }
            }

        return (separatorPanel);
        }


    /***********************************************************************************************
     * Construct a SeparatorPanel.
     *
     * @param unitheight
     * @param resourcekey
     * @param height
     * @param width
     * @param gradientfill
     * @param font
     */

    private SeparatorPanel(final int unitheight,
                           final String resourcekey,
                           final int height,
                           final int width,
                           final boolean gradientfill,
                           final Font font)
        {
        intUnitHeight = unitheight;
        strResourceKey = resourcekey;
        intPixelHeight = height;
        intPixelWidth = width;
        boolGradientFill = gradientfill;
        fontSeparator = font;
        }


    /***********************************************************************************************
     * Get the SeparatorPanel height in Units.
     *
     * @return int
     */

    public int getUnitHeight()
        {
        return (this.intUnitHeight);
        }


    /***********************************************************************************************
     * Get the SeparatorPanel ResourceKey.
     *
     * @return String
     */

    public String getResourceKey()
        {
        return (this.strResourceKey);
        }


    /***********************************************************************************************
     * Get the height of the SeparatorPanel in pixels.
     *
     * @return int
     */

    public int getPixelHeight()
        {
        return (this.intPixelHeight);
        }


    /***********************************************************************************************
     * Get the width of the SeparatorPanel in pixels.
     *
     * @return int
     */

    public int getPixelWidth()
        {
        return (this.intPixelWidth);
        }


    /***********************************************************************************************
     * Indicate the SeparatorPanel Gradient Fill mode.
     *
     * @return boolean
     */

    public boolean isGradientFill()
        {
        return (this.boolGradientFill);
        }


    /***********************************************************************************************
     * Get the SeparatorPanel Font.
     *
     * @return Font
     */

    public Font getFont()
        {
        return (this.fontSeparator);
        }


    /***********************************************************************************************
     * Get the SeparatorPanel as a String.
     *
     * @return String
     */

    public String toString()
        {
        return (this.strResourceKey);
        }
    }
