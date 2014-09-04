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

import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.*;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.InstrumentUIComponentDecorator;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.model.registry.RegistryModelUtilities;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;

import javax.swing.*;
import java.awt.*;


/***************************************************************************************************
 * A SeparatorControlPanel.
 */

public final class SeparatorControlPanel extends InstrumentUIComponentDecorator
    {
    private static final int X_INSET = RackPanel.HOST_PANEL_INSET_WIDTH >> 1;
    private static final int X_ICON_CENTRE = X_INSET
                                                + ((InstrumentSelector.DIM_BUTTON.width << 1)
                                                + InstrumentSelector.WIDTH_BUTTON_SPACING) >> 1;

    // Injections
    private final SeparatorPanel separatorPanel;
    private final String strSeparatorName;

    private ColourInterface colourCanvas;
    private ColourInterface colourGradientTop;
    private ColourInterface colourGradientBottom;


    /***********************************************************************************************
     * Construct a SeparatorControlPanel with name and an optional gradient fill.
     *
     * @param instrument
     * @param instrumentxml
     * @param hostui
     * @param task
     * @param font
     * @param colour
     * @param resourcekey
     * @param separatorpanel
     * @param name
     */

    public SeparatorControlPanel(final ObservatoryInstrumentInterface instrument,
                                 final Instrument instrumentxml,
                                 final ObservatoryUIInterface hostui,
                                 final TaskPlugin task,
                                 final FontInterface font,
                                 final ColourInterface colour,
                                 final String resourcekey,
                                 final SeparatorPanel separatorpanel,
                                 final String name)
        {
        super(instrument,
              instrumentxml,
              hostui,
              task,
              font,
              colour,
              resourcekey,
              1);

        // Injections
        separatorPanel = separatorpanel;
        strSeparatorName = name;

        // Not used currently, the fill is in the background image itself
        if (separatorpanel.isGradientFill())
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
        }


    /***********************************************************************************************
     * Initialise this UIComponent.
     */

    public void initialiseUI()
        {
        final Dimension dimSize;

        // This removes everything from the *ControlPanel*, i.e. no buttons or text,
        // but the ControlPanel sizes are still as set in AbstractObservatoryInstrument.setControlPanel()
        removeAll();
        setLayout(new BorderLayout());

        setOpaque(false);

        if (getCanvasColour() != null)
            {
            setBackground(getCanvasColour());
            }

        // Adjust the HostPanel and ControlPanel to be the sizes required by the SeparatorPanel icon
        dimSize = new Dimension(getSeparatorPanel().getPixelWidth(),
                                getSeparatorPanel().getPixelHeight());
        getHostInstrument().getHostPanel().setMinimumSize(dimSize);
        getHostInstrument().getHostPanel().setMaximumSize(dimSize);
        getHostInstrument().getHostPanel().setPreferredSize(dimSize);
        setMinimumSize(dimSize);
        setMaximumSize(dimSize);
        setPreferredSize(dimSize);

        // Now clear the HostPanel and put the ControlPanel back on its own,
        // now at the same size as the HostPanel, and SelectorPanel
        getHostInstrument().getHostPanel().removeAll();
        getHostInstrument().getHostPanel().add(this);
        }


    /***********************************************************************************************
     * Paint this UIComponent.
     *
     * @param graphics
     */

    public void paint(final Graphics graphics)
        {
        final String SOURCE = "SeparatorControlPanel.paint() ";

        super.paint(graphics);

        // The original background colour or image cannot be seen if gradient paint is used
        if ((getSeparatorPanel() != null)
            && (getSeparatorPanel().isGradientFill())
            && (graphics != null)
            && (getGradientColourTop() != null)
            && (getGradientColourBottom() != null))
            {
            final Graphics2D graphics2D;
            final GradientPaint gradientPaint;

            graphics2D = (Graphics2D)graphics;

            gradientPaint = new GradientPaint(getWidth() >> 1,
                                              0,
                                              getGradientColourTop().getColor(),
                                              getWidth() >> 1,
                                              getHeight(),
                                              getGradientColourBottom().getColor());

            graphics2D.setPaint(gradientPaint);
            graphics2D.fill(new Rectangle(getWidth(), getHeight()));
            }

        // Show the name over whatever background was painted
        if ((getSeparatorPanel() != null)
            && (getSeparatorName() != null)
            && (graphics != null)
            && (getWidth() > 0)
            && (getHeight() > 0))
            {
            final Graphics2D graphics2D;
            final JComponent selectorPanel;

            graphics2D = (Graphics2D)graphics;
            selectorPanel = getHostInstrument().getSelectorPanel();

            // The Rectangle is the StringBounds of the Name text in the current Font
            // The baseline of the leftmost character is at position (x, y)
            // The y coordinate of drawString specifies the baseline position
            // See: http://www.javafaq.nu/java-bookpage-13-8.html
            // The most reliable means of vertically centering text they found turned out to be baseline + ascent/4
            // Ascent /3 works better than the obvious /2, or than the suggested /4

            // Separator text colour is always COLOUR_RAG_TEXT, CATEGORY separators are brighter
            //graphics2D.setColor(getColourData().getColor());

            switch (getSeparatorPanel())
                {
                case CATEGORY:
                    {
                    final FontMetrics metrics;

                    // Obtain the Font from the SeparatorPanel enum
                    graphics2D.setFont(getSeparatorPanel().getFont());
                    graphics2D.setColor(COLOUR_RAG_TEXT.getColor().brighter());

                    metrics = getFontMetrics(graphics2D.getFont());

                    // It doesn't look good this small with antialiasing

                    // Category text is vertically centred, inset by X_INSET
                    // Coordinates relative to SelectorPanel
                    graphics2D.drawString(getSeparatorName(),
                                          X_INSET,
                                          ((selectorPanel.getHeight() >> 1)
                                              + (metrics.getAscent() / 3)));
                    break;
                    }

                case PLAIN:
                    {
                    // No text in this panel!
                    break;
                    }

                case GROUP:
                    {
                    final ImageIcon graphicIcon;
                    final FontMetrics metrics;
                    final int intIconX;
                    final int intTextX;

                    // Draw the Group icon first
                    graphicIcon = RegistryModelUtilities.getAtomIcon(getHostInstrument().getHostAtom(),
                                                                     getObservatoryUI().getGroupDefinitionsTable().get(getObservatoryUI().getCurrentGroupID()).getIconFilename());
                    if (graphicIcon != null)
                        {
                        intIconX = X_ICON_CENTRE - (graphicIcon.getIconWidth() >> 1);
                        intTextX = (intIconX << 1) + graphicIcon.getIconWidth();

                        //System.out.println("X_ICON_CENTRE=" + X_ICON_CENTRE + " intIconX=" + intIconX + " intTextX=" + intTextX);
                        // The image is drawn with its top-left corner at (x, y)
                        // in this graphics context's coordinate space.
                        graphics.drawImage(graphicIcon.getImage(),
                                           intIconX,
                                           (selectorPanel.getHeight() >> 1) - (graphicIcon.getIconHeight() >> 1),
                                           this);
                        }
                    else
                        {
                        LOGGER.error(SOURCE + "Unable to locate Group icon");
                        intIconX = X_INSET;
                        intTextX = intIconX;
                        }

                    // Obtain the Font from the SeparatorPanel
                    graphics2D.setFont(getSeparatorPanel().getFont());
                    graphics2D.setColor(COLOUR_RAG_TEXT.getColor());
                    metrics = getFontMetrics(graphics2D.getFont());

                    // For antialiasing text
                    graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                                                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                    // Group text is vertically centred, inset by X_INSET, modified by Icon width
//                    System.out.println("GROUP y=" +  ((selectorPanel.getHeight() >> 1)
//                                              + (metrics.getAscent() / 3)) + " selector width" + selectorPanel.getWidth() + " selector height=" + selectorPanel.getHeight() + " sep height=" + getSeparatorPanel().getPixelHeight() + " ascent/3=" + (metrics.getAscent() / 3));
                    graphics2D.drawString(getSeparatorName(),
                                          intTextX,
                                          ((selectorPanel.getHeight() >> 1)
                                              + (metrics.getAscent() / 3)));
                    break;
                    }

                default:
                    {
                    // Do nothing, something went wrong anyway...
                    LOGGER.error(SOURCE + "Invalid SeparatorPanel");
                    }
                }
            }
        }


    /***********************************************************************************************
     * Get the SeparatorPanel.
     *
     * @return SeparatorPanel
     */

    private SeparatorPanel getSeparatorPanel()
        {
        return (this.separatorPanel);
        }


    /***********************************************************************************************
     * Get the Separator name.
     *
     * @return String
     */

    private String getSeparatorName()
        {
        return (this.strSeparatorName);
        }


    /***********************************************************************************************
     * A utility so other classes can get canvas colour.
     *
     * @return Color
     */

    private Color getCanvasColour()
        {
        return (colourCanvas.getColor());
        }


    /***********************************************************************************************
     * A utility so other classes can set canvas colour.
     *
     * @param colour
     */

    private void setCanvasColour(final ColourInterface colour)
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

    private void setGradientColourTop(final ColourInterface colour)
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

    private void setGradientColourBottom(final ColourInterface colour)
        {
        colourGradientBottom = colour;
        }
    }
