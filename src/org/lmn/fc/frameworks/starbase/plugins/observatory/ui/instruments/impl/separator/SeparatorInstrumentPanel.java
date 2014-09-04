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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.separator;

import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryUIInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.InstrumentUIComponentDecorator;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.InstrumentUIHelper;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.model.registry.RegistryModelUtilities;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;

import javax.swing.*;
import java.awt.*;


/***************************************************************************************************
 * A SeparatorInstrumentPanel to fill the visible Instrument Panel when no real UI is available.
 */

public final class SeparatorInstrumentPanel extends InstrumentUIComponentDecorator
    {
    // Injections
    private String strName;
    private final String strHeaderIconFilename;
    private final boolean boolDrawHeader;

    private final ImageIcon iconUKRAA;

    private ColourInterface colourCanvas;
    private ColourInterface colourGradientTop;
    private ColourInterface colourGradientBottom;


    /***********************************************************************************************
     * Construct a SeparatorInstrumentPanel with an optional gradient fill.
     *
     * @param instrument
     * @param instrumentxml
     * @param hostui
     * @param task
     * @param resourcekey
     * @param iconfilename
     * @param font
     * @param colour
     * @param gradientfill
     * @param drawheader
     */

    public SeparatorInstrumentPanel(final ObservatoryInstrumentInterface instrument,
                                    final Instrument instrumentxml,
                                    final ObservatoryUIInterface hostui,
                                    final TaskPlugin task,
                                    final String resourcekey,
                                    final String iconfilename,
                                    final FontInterface font,
                                    final ColourInterface colour,
                                    final boolean gradientfill,
                                    final boolean drawheader)
        {
        super(instrument,
              instrumentxml,
              hostui,
              task,
              font,
              colour,
              resourcekey, 1);

        strName = EMPTY_STRING;
        strHeaderIconFilename = iconfilename;

        if (gradientfill)
            {
            colourCanvas = DEFAULT_COLOUR_CANVAS;
            colourGradientTop = DEFAULT_COLOUR_GRADIENT_TOP;
            colourGradientBottom = DEFAULT_COLOUR_GRADIENT_BOTTOM;
            }
        else
            {
            colourCanvas = DEFAULT_COLOUR_CANVAS;
            colourGradientTop = null;
            colourGradientBottom = null;
            }

        this.boolDrawHeader = drawheader;

        iconUKRAA = RegistryModelUtilities.getAtomIcon(REGISTRY.getFramework(),
                                                       UKRAA_LOGO_SEPARATOR_FILENAME);
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
     * Initialise this UIComponent.
     */

    public void initialiseUI()
        {
        // Only add a header and new client area if required...
        if (this.boolDrawHeader)
            {
            super.initialiseUI();

            InstrumentUIHelper.configureInstrumentPanelHeader(getHeaderUIComponent(),
                                                              getObservatoryUI(),
                                                              this,
                                                              getHostTask().getParentAtom(),
                                                              getInstrument(),
                                                              strHeaderIconFilename,
                                                              getFontData(),
                                                              getColourData());

            // Add a client area for the BoxLayout to enable gradient painting
            this.add(getClientArea());
            }
        else
            {
            removeAll();
            setLayout(new BorderLayout());
            }

        setOpaque(false);

        if (colourCanvas != null)
            {
            setBackground(colourCanvas.getColor());
            }
        }


    /***********************************************************************************************
     * Paint this UIComponent.
     *
     * @param graphics
     */

    public void paint(final Graphics graphics)
        {
        super.paint(graphics);

        if (!boolDrawHeader)
            {
            final GradientPaint gradientPaint;

            // The original background colour cannot be seen if gradient paint is used
            if ((graphics != null)
                && (getGradientColourTop() != null)
                && (getGradientColourBottom() != null))
                {
                gradientPaint = new GradientPaint(getWidth() >> 1,
                                                  0,
                                                  getGradientColourTop().getColor(),
                                                  getWidth() >> 1,
                                                  getHeight(),
                                                  getGradientColourBottom().getColor());

                ((Graphics2D)graphics).setPaint(gradientPaint);
                ((Graphics2D)graphics).fill(new Rectangle(getWidth(), getHeight()));
                }

            // Show the name over whatever background was painted
            if ((graphics != null)
                && (strName != null)
                && (getWidth() > 0)
                && (getHeight() > 0))
                {
                graphics.setColor(Color.ORANGE);
                graphics.drawString(strName, getWidth()/3, getHeight() >> 1);
                //System.out.println("drawString-->" + strName);
                }
            }
        }


    /***********************************************************************************************
     * A panel for the client area to allow painting separately from the Header.
     *
     * @return JPanel
     */

    private JPanel getClientArea()
        {
        final JPanel panel;

        panel = new JPanel()
            {
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

                //paintGradientAndIcon(graphics);
                }
            };

        panel.setBackground(DEFAULT_COLOUR_CANVAS.getColor());
        panel.setAlignmentX(LEFT_ALIGNMENT);

        return (panel);
        }


    /***************************************************************************************************
     * Paint the Gradient and an Icon.
     *
     * @param graphics
     */

    private void paintGradientAndIcon(final Graphics graphics)
        {
        final GradientPaint gradientPaint;

        // The original background colour cannot be seen if gradient paint is used
        if ((graphics != null)
            && (getGradientColourTop() != null)
            && (getGradientColourBottom() != null))
            {
            gradientPaint = new GradientPaint(getWidth() >> 1,
                                              0,
                                              getGradientColourTop().getColor(),
                                              getWidth() >> 1,
                                              getHeight(),
                                              getGradientColourBottom().getColor());

            ((Graphics2D)graphics).setPaint(gradientPaint);
            ((Graphics2D)graphics).fill(new Rectangle(getWidth(), getHeight()));
            }

        // Show the name over whatever background was painted
        if ((graphics != null)
            && (strName != null)
            && (getWidth() > 0)
            && (getHeight() > 0))
            {
            graphics.setColor(Color.ORANGE);
            graphics.drawString(strName, getWidth()/3, getHeight() >> 1);
            //System.out.println("drawString-->" + strName);
            }

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
     * A utility so other classes can get canvas colour.
     *
     * @return Color
     */

    public final Color getCanvasColour()
        {
        return (colourCanvas.getColor());
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
            colourCanvas = colour;
            setBackground(colourCanvas.getColor());
            }
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
     * Set the Colour of the top of the gradient paint.
     *
     * @param colour
     */

    public final void setGradientColourTop(final ColourInterface colour)
        {
        colourGradientTop = colour;
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


    /***********************************************************************************************
     * Set the Colour of the bottom of the gradient paint.
     *
     * @param colour
     */

    public final void setGradientColourBottom(final ColourInterface colour)
        {
        colourGradientBottom = colour;
        }
    }
