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

//------------------------------------------------------------------------------
// Revision History
//
//  10-03-00    LMN created file
//
//------------------------------------------------------------------------------

package org.lmn.fc.ui.widgets.impl;

//------------------------------------------------------------------------------
// Imports

import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.common.exceptions.IndicatorException;
import org.lmn.fc.common.utilities.time.Chronos;
import org.lmn.fc.ui.layout.BoxLayoutFixed;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import java.awt.*;
import java.awt.geom.Rectangle2D;

//------------------------------------------------------------------------------

public class StatusLights extends JPanel
    {
    private final Dimension dimStatusDimension;
    private final int intLightCount;

    private BoxLayoutFixed statusBoxLayout;
    private Border statusBorder;

    private JPanel lightPanel;
    private GridLayout lightGridLayout;
    private Border lightBorder;
    private JLabel lights[];

    private JPanel labelPanel;
    private GridLayout labelGridLayout;
    private final String strDummyLabel;
    private JLabel labels[];

    private final Font fontLabel;
    private final Color colorLabel;
    private final Color colourDefaultOn;
    private final Color colourDefaultOff;
    private Color colourLightOn[];
    private String tooltipOn[];
    private String tooltipOff[];

    private long longTimerMilliseconds;
    private boolean boolDebugMode;          // Controls debug messages
    private boolean boolTimingMode;         // Controls timing messages


    public StatusLights(final Dimension statusDimension,
                        final int lightCount,
                        final Font font,
                        final Color labelcolor,
                        final String format) throws IndicatorException
        {
        // Make the JPanel
        super();

        this.dimStatusDimension = statusDimension;
        this.intLightCount = lightCount;
        fontLabel = font;
        colorLabel = labelcolor;

        this.colourDefaultOn = Color.green;
        this.colourDefaultOff = Color.red;
        this.strDummyLabel = format;

        this.boolDebugMode = false;

        initialiseStatusLights();
        }


    //--------------------------------------------------------------------------
    // Instance Methods
    //--------------------------------------------------------------------------

    private void initialiseStatusLights() throws IndicatorException
        {
        int i;                          // Loop counter
        int intPointSize;               // Iteration for font to fit the space
        Font chosenFont;                // The font calculated to fit a label

        // 50 indicators might fit in 1000 pixels...
        if ((intLightCount < 1) || (intLightCount > 50))
            {
            throw new IndicatorException(FrameworkStrings.EXCEPTION_STATUSOUTOFRANGE);
            }
        else
            {
            // Attend to the main container panel
            statusBorder = new CompoundBorder(BorderFactory.createRaisedBevelBorder(),
                                              BorderFactory.createLoweredBevelBorder());
            this.setBorder(statusBorder);

            // Find out how big the areas and borders are, allowing free space
            final Insets statusInsets = statusBorder.getBorderInsets(this);
            final int heightGap = statusInsets.top + statusInsets.bottom;
            final int widthGap = statusInsets.left + statusInsets.right;
            final Dimension dimFreeArea = new Dimension(dimStatusDimension.width-widthGap,
                                                  dimStatusDimension.height-heightGap);

            statusBoxLayout = new BoxLayoutFixed(this, BoxLayoutFixed.X_AXIS);
            this.setLayout(statusBoxLayout);

            // Create the panel for the lights
            lightPanel = new JPanel();
            lightGridLayout = new GridLayout(intLightCount, 1);
            lightPanel.setLayout(lightGridLayout);
            lightPanel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
            final Dimension lightDimension = new Dimension(2*(dimFreeArea.width/10),
                                                     dimFreeArea.height);
            lightPanel.setPreferredSize(lightDimension);
            lightPanel.setMaximumSize(lightDimension);
            lightPanel.setMinimumSize(lightDimension);

            // Create the panel for the labels
            labelPanel = new JPanel();
            labelGridLayout = new GridLayout(intLightCount, 1);
            labelPanel.setLayout(labelGridLayout);
            labelPanel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
            final Dimension labelDimension = new Dimension(8*(dimFreeArea.width/10),
                                                        dimFreeArea.height);
            labelPanel.setPreferredSize(labelDimension);
            labelPanel.setMaximumSize(labelDimension);
            labelPanel.setMinimumSize(labelDimension);

            // Create as many lights and labels as we need
            lights = new JLabel[intLightCount];
            colourLightOn = new Color[intLightCount];
            labels = new JLabel[intLightCount];
            tooltipOn = new String[intLightCount];
            tooltipOff = new String[intLightCount];
            lightBorder = BorderFactory.createEtchedBorder(Color.gray, Color.lightGray);

            for (i = 0; i < intLightCount; i++)
                {
                tooltipOn[i] = "On";
                tooltipOff[i] = "Off";
                colourLightOn[i] = colourDefaultOn;

                lights[i] = new JLabel("");
                lights[i].setBorder(lightBorder);
                lights[i].setOpaque(true);
                lights[i].setForeground(colourLightOn[i]);
                lights[i].setBackground(colourLightOn[i]);
                lights[i].setToolTipText(tooltipOn[i]);

                lightPanel.add(lights[i]);

                labels[i] = new JLabel("");
                labels[i].setHorizontalAlignment(SwingConstants.LEFT);
                labels[i].setForeground(colorLabel);

                labelPanel.add(labels[i]);
                }

            // Arrange the requested Font for the labels
            // This takes a (very) long time if there are a lot of fonts installed!
            final GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();

            // Iterate until the correct font point size is found which fits the
            // available space. I can't see any other way to do this!
            chosenFont = fontLabel.deriveFont(1f);

            for (intPointSize = 6;
                 intPointSize < 100;
                 intPointSize = intPointSize + 2)
                {
                final Font trialFont = fontLabel.deriveFont((float)intPointSize);

                // There must always be at least one label!
                labels[0].setFont(trialFont);
                final FontMetrics trialMetrics = labels[0].getFontMetrics(trialFont);
                final Rectangle2D trialRectangle = trialMetrics.getStringBounds(strDummyLabel, labels[0].getGraphics());

                // See if we have exceeded the dimensions of the space allowed
                if ((trialRectangle.getWidth() > labelDimension.getWidth())
                    || (trialRectangle.getHeight() > (labelDimension.getHeight()/intLightCount)))
                    {
                    // We've found a suitable font...
                    // Back up the point size a bit

                    chosenFont = fontLabel.deriveFont((float)intPointSize-2);
                    break;
                    }
                }

            // Set all of the label fonts to the one calculated above
            for (i = 0; i < intLightCount; i++)
                {
                labels[i].setFont(chosenFont);
                }

            // Put all the components together
            this.add(lightPanel);
            this.add(labelPanel);

            this.setPreferredSize(dimStatusDimension);
            this.setMaximumSize(dimStatusDimension);
            this.setMinimumSize(dimStatusDimension);
            }
        }


    //--------------------------------------------------------------------------
    // Set the state of a light

    public void setState(final int light, final boolean state) throws IndicatorException
        {
        if ((light < 0) || (light >= this.intLightCount))
            {
            throw new IndicatorException(FrameworkStrings.EXCEPTION_STATUSLIGHTOUTOFRANGE);
            }
        else
            {
            if (state)
                {
                lights[light].setForeground(colourLightOn[light]);
                lights[light].setBackground(colourLightOn[light]);
                lights[light].setToolTipText(tooltipOn[light]);
                }
            else
                {
                lights[light].setForeground(colourDefaultOff);
                lights[light].setBackground(colourDefaultOff);
                lights[light].setToolTipText(tooltipOff[light]);
                }
            }
        }


    //--------------------------------------------------------------------------
    // Get the state of a light

    public boolean getState(final int light) throws IndicatorException
        {
        if ((light < 0) || (light >= this.intLightCount))
            {
            throw new IndicatorException(FrameworkStrings.EXCEPTION_STATUSLIGHTOUTOFRANGE);
            }
        else
            {
            // We could have a separate array of light states...
            if (lights[light].getForeground() == colourLightOn[light])
                {
                return(true);
                }
            else
                {
                return(false);
                }
            }
        }


    //--------------------------------------------------------------------------
    // Set the tooltip text for a light's On or Off state

    public void setToolTip(final int light, final boolean state, final String tooltip) throws IndicatorException
        {
        if ((light < 0) || (light >= this.intLightCount))
            {
            throw new IndicatorException(FrameworkStrings.EXCEPTION_STATUSLIGHTOUTOFRANGE);
            }
        else
            {
            // First, store the changed tooltip
            if (state)
                {
                tooltipOn[light] = tooltip;
                }
            else
                {
                tooltipOff[light] = tooltip;
                }

            // Next, update the light's displayed tooltip
            if (getState(light))
                {
                lights[light].setToolTipText(tooltipOn[light]);
                }
            else
                {
                lights[light].setToolTipText(tooltipOff[light]);
                }
            }
        }


    //--------------------------------------------------------------------------
    // Set the colour of the On state of a light

    public void setColour(final int light, final Color colour) throws IndicatorException
        {
        final boolean boolState;

        if ((light < 0) || (light >= this.intLightCount))
            {
            throw new IndicatorException(FrameworkStrings.EXCEPTION_STATUSLIGHTOUTOFRANGE);
            }
        else
            {
            // Get the state of the light before we change the colour
            boolState = getState(light);

            colourLightOn[light] = colour;
            this.setState(light, boolState);
            }
        }


    //--------------------------------------------------------------------------
    // Get the colour of the On state of a light

    public Color getColour(final int light) throws IndicatorException
        {
        if ((light < 0) || (light >= this.intLightCount))
            {
            throw new IndicatorException(FrameworkStrings.EXCEPTION_STATUSLIGHTOUTOFRANGE);
            }
        else
            {
            return (colourLightOn[light]);
            }
        }


    //--------------------------------------------------------------------------
    // Set the label text for a light

    public void setLabel(final int light, final String label) throws IndicatorException
        {
        if ((light < 0) || (light >= this.intLightCount))
            {
            throw new IndicatorException(FrameworkStrings.EXCEPTION_STATUSLIGHTOUTOFRANGE);
            }
        else
            {
            // Test for a maximum of 'M's (a bit of a bodge)
            if (label.length() > (strDummyLabel.length()+3))
                {
                throw new IndicatorException(FrameworkStrings.EXCEPTION_STATUSLABELTOOLONG);
                }
            else
                {
                labels[light].setText(label);
                }
            }
        }


    //--------------------------------------------------------------------------
    // Get the label text for a light

    public String getLabel(final int light) throws IndicatorException
        {
        if ((light < 0) || (light >= this.intLightCount))
            {
            throw new IndicatorException(FrameworkStrings.EXCEPTION_STATUSLIGHTOUTOFRANGE);
            }
        else
            {
            return(labels[light].getText());
            }
        }


    /***********************************************************************************************
     * Get the JComponent representing the specified Light.
     *
     * @param light
     *
     * @return JComponent
     *
     * @throws IndicatorException
     */

    public JComponent getLight(final int light) throws IndicatorException
        {
        if ((light < 0) || (light >= this.intLightCount))
            {
            throw new IndicatorException(FrameworkStrings.EXCEPTION_STATUSLIGHTOUTOFRANGE);
            }
        else
            {
            return(lights[light]);
            }
        }


    //--------------------------------------------------------------------------
    // Set the visibility of a light

    public void setVisible(final int light, final boolean visible) throws IndicatorException
        {
        if ((light < 0) || (light >= this.intLightCount))
            {
            throw new IndicatorException(FrameworkStrings.EXCEPTION_STATUSLIGHTOUTOFRANGE);
            }
        else
            {
            lights[light].setVisible(visible);
            labels[light].setVisible(visible);
            }
        }


    //--------------------------------------------------------------------------
    // Get the Debug Mode flag

    public boolean getDebugMode()
        {
        return(this.boolDebugMode);
        }


    //--------------------------------------------------------------------------
    // Set the Debug Mode flag

    public void setDebugMode(final boolean flag)
        {
        this.boolDebugMode = flag;
        }


    //--------------------------------------------------------------------------
    // Show a debug message

    private void showDebugMessage(final String message)
        {
        final String strSeparator;

        if (boolDebugMode)
            {
            if (message.startsWith("."))
                {
                strSeparator = "";
                }
            else
                {
                strSeparator = " ";
                }

            System.out.println(Chronos.timeNow() + " " + this.getClass().getName() + strSeparator + message);
            }
        }


    //--------------------------------------------------------------------------
    // Get the Timing Mode flag

    public boolean getTimingMode()
        {
        return this.boolTimingMode;
        }


    //--------------------------------------------------------------------------
    // Set the Timing Mode flag

    public void setTimingMode(final boolean flag)
        {
        this.boolTimingMode = flag;
        }


    //--------------------------------------------------------------------------
    // Show a timing message

    private void showTimingMessage(final String message)
        {
        final String strSeparator;

        if (boolTimingMode)
            {
            if (message.startsWith("."))
                {
                strSeparator = "";
                }
            else
                {
                strSeparator = " ";
                }

            // longTimerMilliseconds = System.currentTimeMillis() - longTimerMilliseconds;
            longTimerMilliseconds = System.currentTimeMillis();

            System.out.println(Chronos.timeNow() + " "
                               + longTimerMilliseconds
                               + " " + this.getClass().getName()
                               + strSeparator + message);
            }
        }

    }

//------------------------------------------------------------------------------
// End of File

