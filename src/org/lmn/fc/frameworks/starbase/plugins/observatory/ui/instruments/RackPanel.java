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

import java.awt.*;


/***********************************************************************************************
 * A RackPanel for the Instrument Selector.
 */

public enum RackPanel
    {
    PANEL_1U (1,
              "1U_60px.png",
              61,
              250),

    PANEL_2U (2,
              "2U_90px.png",
              91,
              PANEL_1U.getPixelWidth()),

    PANEL_3U (3,
              "3U_135px.png",
              136,
              PANEL_1U.getPixelWidth()),

    PANEL_4U (4,
              "4U_180px.png",
              181,
              PANEL_1U.getPixelWidth()),

    PANEL_5U (5,
              "5U_225px.png",
              226,
              PANEL_1U.getPixelWidth()),

    PANEL_6U (6,
              "6U_270px.png",
              271,
              PANEL_1U.getPixelWidth()),

    PANEL_7U (7,
              "7U_315px.png",
              316,
              PANEL_1U.getPixelWidth()),

    PANEL_8U (8,
              "8U_360px.png",
              361,
              PANEL_1U.getPixelWidth()),

    PANEL_9U (9,
              "9U_405px.png",
              406,
              PANEL_1U.getPixelWidth()),

    PANEL_10U (10,
               "10U_450px.png",
               451,
               PANEL_1U.getPixelWidth());


    public static final int HOST_PANEL_INSET_WIDTH = 36;
    public static final int HOST_PANEL_INSET_HEIGHT = 8;

    private final int intUnitHeight;
    private final String strImageFileName;
    private final int intPixelHeight;
    private final int intPixelWidth;


    /***********************************************************************************************
     * Get the height of panel to be used for the InstrumentUIComponentDecorator header.
     * This is to ensure that the Rack lines up with the Header.
     *
     * @return RackPanel
     */

    public static  RackPanel getInstrumentHeaderU()
        {
        return (PANEL_1U);
        }


    /***********************************************************************************************
     * Get the Dimension of the ControlPanel given the SelectorPanel height, in 'U'.
     * This panel contains the displays and any extra controls (i.e. not Start and Stop).
     *
     * @param height
     *
     * @return Dimension
     */

    public static Dimension getControlPanelSize(final int height)
        {
        return (new Dimension(getHostPanelSize(height).width,
                              getHostPanelSize(height).height - 18));
        }


    /***********************************************************************************************
     * Get the Dimension of the HostPanel given the SelectorPanel height, in 'U'.
     * This panel is inset inside the SelectorPanel, to leave an empty border.
     *
     * @param height
     *
     * @return Dimension
     */

    public static Dimension getHostPanelSize(final int height)
        {
        return (new Dimension(getSelectorPanelSize(height).width - HOST_PANEL_INSET_WIDTH,
                              getSelectorPanelSize(height).height - HOST_PANEL_INSET_HEIGHT));
        }


    /***********************************************************************************************
     * Get the Dimension of the SelectorPanel given the SelectorPanel height, in 'U'.
     * This is the background image to the whole Selector.
     *
     * @param height
     *
     * @return Dimension
     */

    public static Dimension getSelectorPanelSize(final int height)
        {
        final Dimension dimPanel;

        if (height == 1)
            {
            dimPanel = new Dimension(PANEL_1U.getPixelWidth(),
                                     PANEL_1U.getPixelHeight() + 1);
            }
        else
            {
            // TODO Improve this! 45 pixels each size except first
            dimPanel = new Dimension(PANEL_1U.getPixelWidth(),
                                     (45 * height) + 1);
            }

        return (dimPanel);
        }


    /***********************************************************************************************
     * Look up the RackPanel enumeration corresponding to the specified height.
     *
     * @param height
     *
     * @return RackPanel
     */

    public static RackPanel getRackPanelForHeight(final int height)
        {
        RackPanel rackPanel;

        // Prepare a default value
        rackPanel = PANEL_1U;

        if ((height >= InstrumentSelector.RACK_PANEL_HEIGHT_MIN)
            && (height <= InstrumentSelector.RACK_PANEL_HEIGHT_MAX))
            {
            final RackPanel[] panels;
            boolean boolFound;

            panels = values();
            boolFound = false;

            for (int i = 0;
                 ((i < panels.length) && (!boolFound));
                 i++)
                {
                if (panels[i].getUnitHeight() == height)
                    {
                    rackPanel = panels[i];
                    boolFound = true;
                    }
                }
            }

        return (rackPanel);
        }


    /***********************************************************************************************
     * Look up the (smallest) RackPanel height in 'U' corresponding to the specified height in pixels.
     * Return 1U on any range violation or failure.
     *
     * @param pixels
     *
     * @return int
     */

    public static int getRackPanelHeightForPixels(final int pixels)
        {
        int intUnitHeight;

        // Prepare a default value
        intUnitHeight = 1;

        // RackPanel measures the height of a panel in U = (image_pixels + 1)
        if ((pixels > 0)
            && (pixels <= (PANEL_10U.getPixelHeight()+1)))
            {
            final RackPanel[] panels;
            boolean boolFound;

            panels = values();
            boolFound = false;

            // Search in reverse, to find the first U less than the pixel height
            for (int intIndexU = (panels.length-1);
                 ((intIndexU >= 0) && (!boolFound));
                 intIndexU--)
                {
//                System.out.println("RackPanel.getRackPanelHeightForPixels() Searching [index=" + intIndexU + "] [pixels=" + pixels + "]");

                if (panels[intIndexU].getPixelHeight() <= (pixels+1))
                    {
                    intUnitHeight = panels[intIndexU].getUnitHeight();
//                    System.out.println("RackPanel.getRackPanelHeightForPixels() Found it! [pixels=" + pixels
//                                       + "] [unit.height=" + intUnitHeight
//                                       + "] [pixel.height=" + panels[intIndexU].getPixelHeight() + "]" );
                    boolFound = true;
                    }
                }

//            System.out.println("RackPanel.getRackPanelHeightForPixels() [pixels=" + pixels + "] [unit.height=" + intUnitHeight + "]");
            }
//        else
//            {
//            System.out.println("RackPanel.getRackPanelHeightForPixels() Out of range! [pixels=" + pixels + "]");
//            }

        return (intUnitHeight);
        }


    /***********************************************************************************************
     * Construct a Rack Panel.
     *
     * @param unitheight
     * @param imagefilename
     * @param height
     * @param width
     */

    private RackPanel(final int unitheight,
                      final String imagefilename,
                      final int height,
                      final int width)
        {
        intUnitHeight = unitheight;
        strImageFileName = imagefilename;
        intPixelHeight = height;
        intPixelWidth = width;
        }


    /***********************************************************************************************
     * Get the height in Units.
     *
     * @return int
     */

    public int getUnitHeight()
        {
        return (this.intUnitHeight);
        }


    /***********************************************************************************************
     * Get the ImageFileName.
     *
     * @return String
     */

    public String getImageFileName()
        {
        return(this.strImageFileName);
        }


    /***********************************************************************************************
     * Get the height of the panel in pixels.
     *
     * @return int
     */

    public int getPixelHeight()
        {
        return (this.intPixelHeight);
        }


    /***********************************************************************************************
     * Get the width of the panel in pixels.
     *
     * @return int
     */

    public int getPixelWidth()
        {
        return (this.intPixelWidth);
        }
    }
