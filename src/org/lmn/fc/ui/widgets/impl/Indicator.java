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
//  16-05-00    LMN edited file
//  17-05-00    LMN seemed to master the layouts!
//  19-05-00    LMN made scalable fonts work!
//  05-03-02    LMN returned to the problem two years on!
//  12-03-02    LMN added constructor for an Indicator without a status field
//  13-03-02    LMN added foreground & background colour changes
//  19-04-02    LMN added setFont() to simplify use in DigitalClock
//  19-04-02    LMN changed Value from JPanel to JLayeredPane, for overlaid components
//
//------------------------------------------------------------------------------
// Widgets package

package org.lmn.fc.ui.widgets.impl;

//------------------------------------------------------------------------------
// Imports

import org.lmn.fc.common.exceptions.IndicatorException;
import org.lmn.fc.common.utilities.time.Chronos;
import org.lmn.fc.ui.layout.BoxLayoutFixed;
import org.lmn.fc.ui.widgets.IndicatorInterface;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.MouseListener;
import java.awt.geom.Rectangle2D;


/***************************************************************************************************
 * Indicator.
 */

public class Indicator extends JPanel
                       implements IndicatorInterface
    {
    // The overall container
    private BoxLayoutFixed indicatorLayout;
    private Dimension dimIndicator;

    // The Indicated Value
    private JLayeredPane indicatorPane;
    private JLabel indicatorValue;
    private Border indicatorValueBorder;
    private Dimension dimValue;
    private String strValueFont;
    private String strValueFontDefault;
    private String strValueFormat;
    private String strValueIndicated;
    private Color colourForeground;
    private Color colourBackground;

    // The Indicated Units
    private JLabel indicatorUnits;
    private String strUnitsIndicated;
    private boolean boolUnitsVisible;

    // The Indicated Status
    private JLabel indicatorStatus;
    private Border indicatorStatusBorder;
    private Dimension dimStatus;
    private String strStatusFont;
    private String strStatusFontDefault;
    private String strStatusFormat;
    private String strStatusIndicated;

    private String strToolTipString;

    private boolean boolStatusEnable;   // Controls the Status window
    private boolean boolDebugMode;      // Controls debug messages


    /***********************************************************************************************
     * Get the specified number of 'M's to use as a Value format string.
     *
     * @param ems
     *
     * @return String
     */

    public static String getValueFormatAsEms(final int ems)
        {
        final StringBuffer buffer;

        buffer = new StringBuffer();

        for (int intIndex = 0;
            intIndex < ems;
            intIndex++)
            {
            buffer.append("M");
            }

        return (buffer.toString());
        }


    /***********************************************************************************************
     * A utility to do the font iteration.
     * Arrange the best-fit Font for the specified Indicator field.
     *
     * @param field
     * @param dimfield
     * @param fontname
     * @param template
     */

    private static void findFontSize(final JLabel field,
                                     final Dimension dimfield,
                                     final String fontname,
                                     final String template)
        {
        float floatPointSize;
        boolean boolFoundFont;
        Rectangle2D rectangleTrial;
        final Font fontStart;

        // This takes a (very) long time if there are a lot of fonts installed!
//        GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
//        String[] fontFamilyNames = graphicsEnvironment.getAvailableFontFamilyNames();

        // Iterate until the correct font point size is found which fits the
        // available space. I can't see any other way to do this!
        boolFoundFont = false;
        fontStart = new Font(fontname, Font.PLAIN, 1);

        for (floatPointSize = 8.0f;
             ((floatPointSize < 50.0f) && (!boolFoundFont));
             floatPointSize += 0.5f)
            {
            Font fontTrial;
            FontMetrics metricsTrial;

            fontTrial = fontStart.deriveFont(floatPointSize);
            field.setFont(fontTrial);
            metricsTrial = field.getFontMetrics(fontTrial);

            // The returned bounds is in baseline-relative coordinates
            rectangleTrial = metricsTrial.getStringBounds(template, field.getGraphics());

//            System.out.println("try font "
//                               + "[size=" + floatPointSize
//                               + "] [template=" + template
//                               + "] [dimwidth=" + dimfield.getWidth()
//                               + "] [dimheight=" + dimfield.getHeight()
//                               + "] [strwidth=" + rectangleTrial.getWidth()
//                               + "] [strheight=" + rectangleTrial.getHeight() + "]");

            // See if we have exceeded the dimensions of the space allowed
            // If so, back up the point size a bit
            if ((rectangleTrial.getWidth() >= (dimfield.getWidth() - 5))
                || (rectangleTrial.getHeight() >= (dimfield.getHeight() - 1)))
                {
                final Rectangle2D rectangleNew;

                // Set the Indicator font for the specified field
                fontTrial = fontTrial.deriveFont((floatPointSize - 0.25f));
                field.setFont(fontTrial);
                metricsTrial = field.getFontMetrics(fontTrial);
                rectangleNew = metricsTrial.getStringBounds(template, field.getGraphics());

//                System.out.println("back off font "
//                                   + "[newsize=" + (floatPointSize-0.25f)
//                                   + "] [oldwidth=" + rectangleTrial.getWidth()
//                                   + "] [adjwidth=" + rectangleNew.getWidth()
//                                   + "] [oldheight=" + rectangleTrial.getHeight()
//                                   + "] [adjheight=" + rectangleNew.getHeight() + "]");

                boolFoundFont = true;
                }
            }
        }


    /***********************************************************************************************
     * Make an Indicator *with* a status field.
     *
     * @param dimension
     * @param strValue
     * @param strUnits
     * @param strStatus
     * @param strToolTip
     * @param border
     */

    public Indicator(final Dimension dimension,
                     final String strValue,
                     final String strUnits,
                     final String strStatus,
                     final String strToolTip,
                     final Border border)
        {
        // Make the Indicator Panel and capture the parameters
        super();

        this.dimIndicator = dimension;

        this.strValueFontDefault = "Dialog";
        this.strValueFont = this.strValueFontDefault;
        this.strValueFormat = "MM:MM:MM";
        this.strValueIndicated = strValue;
        this.strUnitsIndicated = strUnits;
        this.boolUnitsVisible = true;

        this.colourForeground = DEFAULT_VALUE_COLOR;
        this.colourBackground = Color.black;

        this.strStatusFontDefault = "Dialog";
        this.strStatusFont = this.strStatusFontDefault;
        this.strStatusFormat = "MMMMMMMMMMMMMMMMMMMM";
        this.strStatusIndicated = strStatus;

        this.strToolTipString = strToolTip;
        this.boolStatusEnable = true;

        setBorder(border);
        initialiseIndicator(boolStatusEnable);
        }


    /***********************************************************************************************
     * An alternative constructor for an Indicator *without* a status field.
     *
     * @param dimension
     * @param strValue
     * @param strUnits
     * @param strToolTip
     * @param border
     */

    public Indicator(final Dimension dimension,
                     final String strValue,
                     final String strUnits,
                     final String strToolTip,
                     final Border border)
        {
        // Make the Indicator Panel and capture the parameters
        super();

        this.dimIndicator = dimension;

        this.strValueFontDefault = "Dialog";
        this.strValueFont = this.strValueFontDefault;
        this.strValueFormat = "MM:MM:MM";
        this.strValueIndicated = strValue;
        this.strUnitsIndicated = strUnits;
        this.boolUnitsVisible = true;

        this.colourForeground = DEFAULT_VALUE_COLOR;
        this.colourBackground = Color.black;

        this.strStatusFontDefault = "Dialog";
        this.strStatusFont = this.strStatusFontDefault;
        this.strStatusFormat = "XX";
        this.strStatusIndicated = "";

        this.strToolTipString = strToolTip;
        this.boolStatusEnable = false;

        setBorder(border);
        initialiseIndicator(boolStatusEnable);
        }


    /***********************************************************************************************
     * Initialise the Indicator.
     */

    public void initialiseUI()
        {
        initialiseIndicator(boolStatusEnable);
        }


    /***********************************************************************************************
     * Initialise the Indicator.
     *
     * @param hasstatus
     *
     * @throws IndicatorException
     */

    private void initialiseIndicator(final boolean hasstatus) throws IndicatorException
        {
        final int heightGap;
        final int widthGap;
        final int insetRight;

        // Find out how big the areas and borders are, allowing space
        if (getBorder() != null)
            {
            final Insets indicatorInsets;

            indicatorInsets = getBorder().getBorderInsets(this);
            heightGap = indicatorInsets.top + indicatorInsets.bottom;
            widthGap = indicatorInsets.left + indicatorInsets.right;
            insetRight = indicatorInsets.right + 6;
            }
        else
            {
            heightGap = 0;
            widthGap = 0;
            insetRight = 6;
            }

        // Are we making a status field or not?
        if (hasstatus)
            {
            dimValue = new Dimension(dimIndicator.width-widthGap,
                                     7*(dimIndicator.height-heightGap)/10);
            dimStatus = new Dimension(dimIndicator.width-widthGap,
                                      3*(dimIndicator.height-heightGap)/10);
            }
        else
            {
            dimValue = new Dimension(dimIndicator.width-widthGap,
                                     dimIndicator.height-heightGap);
            }

        // Lay out the Indicator (and Status) labels vertically in line
        indicatorLayout = new BoxLayoutFixed(this, BoxLayoutFixed.Y_AXIS);
        this.setLayout(indicatorLayout);

        //----------------------------------------------------------------------
        // Create the Indicator Value field, in a panel

        //indicatorValuePanel = new JPanel();
        //indicatorValuePanel.setLayout(new OverlayLayout(indicatorValuePanel));
        //indicatorValuePanel.setAlignmentX((float)0.5);

        indicatorPane = new JLayeredPane();

        // Add a units indicator at the right hand end
        // This must go first in order to be 'on top'
        indicatorUnits = new JLabel(strUnitsIndicated + "  ");

        indicatorUnits.setFont(new Font("Dialog", Font.ITALIC, 10));
        indicatorUnits.setBackground(colourBackground);
        indicatorUnits.setForeground(colourForeground);
        final Dimension dimUnits = new Dimension(dimIndicator.width/4,
                                                 (dimIndicator.height-heightGap)/3);
        indicatorUnits.setBorder(null);
        indicatorUnits.setHorizontalAlignment(SwingConstants.RIGHT);
        indicatorUnits.setOpaque(false);

        indicatorUnits.setPreferredSize(dimUnits);
        indicatorUnits.setMaximumSize(dimUnits);
        indicatorUnits.setMinimumSize(dimUnits);

        // The '-3' allows a gap between Value and Units?
        indicatorUnits.setBounds((int)dimValue.getWidth() - (int)dimUnits.getWidth() - insetRight,
                                 -2,
                                 (int)dimUnits.getWidth(),
                                 (int)dimUnits.getHeight() - 3);
        indicatorPane.add(indicatorUnits);
        indicatorPane.moveToFront(indicatorUnits);

        // Now the Indicated Value label
        indicatorValue = new JLabel(strValueIndicated);
//            {
//            // Enable Antialiasing in Java 1.5
//            protected void paintComponent(final Graphics graphics)
//                {
//                final Graphics2D graphics2D;
//                final FontMetrics metricsTrial;
//                final Rectangle2D rectangleTrial;
//
//                graphics2D = (Graphics2D) graphics;
//                super.paintComponent(graphics2D);
//
//                metricsTrial = getFontMetrics(getFont());
//
//                // The returned bounds is in baseline-relative coordinates
//                rectangleTrial = metricsTrial.getStringBounds(getText(), graphics2D);
//
//                if ((rectangleTrial != null)
//                    && (graphics2D != null))
//                    {
//                    final int x;
//                    final int y;
//
//                    // The returned bounds is in baseline-relative coordinates
//                    x = (int) (getWidth() - rectangleTrial.getWidth()) >> 1;
//                    y = (int) (getHeight() - rectangleTrial.getHeight()) >> 1;
//
//                    graphics2D.setColor(Color.YELLOW);
//
//                    graphics2D.drawRect(x,
//                                        y,
//                                        (int) rectangleTrial.getWidth(),
//                                        (int) rectangleTrial.getHeight());
//                    }
//                }
//            };

//            {
//            // Enable Antialiasing in Java 1.5
//            protected void paintComponent(final Graphics graphics)
//                {
//                final Graphics2D graphics2D = (Graphics2D) graphics;
//
//                // For antialiasing text
//                graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
//                                            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
//                super.paintComponent(graphics2D);
//                }
//            };

        indicatorValueBorder = BorderFactory.createEtchedBorder(EtchedBorder.RAISED);
        indicatorValue.setBorder(indicatorValueBorder);

        indicatorValue.setOpaque(true);
        indicatorValue.setBackground(colourBackground);
        indicatorValue.setForeground(colourForeground);
        indicatorValue.setHorizontalAlignment(SwingConstants.CENTER);

        // Set the sizes before setting the font!
        indicatorValue.setPreferredSize(dimValue);
        indicatorValue.setMaximumSize(dimValue);
        indicatorValue.setMinimumSize(dimValue);

        // Set up a default font and format, sized appropriately
        setValueFont(strValueFontDefault);
        setValueFormat(strValueFormat);

        indicatorValue.setToolTipText(strToolTipString);

        indicatorValue.setBounds(0,
                                 0,
                                 (int)dimValue.getWidth(),
                                 (int)dimValue.getHeight());
        indicatorPane.add(indicatorValue);
        indicatorPane.moveToBack(indicatorValue);

        this.add(indicatorPane);

        //------------------------------------------------------------------------------------------
        // Create the Status display if required

        if (hasstatus)
            {
            indicatorStatus = new JLabel("")
                {
                // Enable Antialiasing in Java 1.5
                protected void paintComponent(final Graphics graphics)
                    {
                    final Graphics2D graphics2D = (Graphics2D) graphics;

                    // For antialiasing text
                    graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                                                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    super.paintComponent(graphics2D);
                    }
                };

            indicatorStatus.setBorder(null);
            indicatorStatus.setAlignmentX(Component.CENTER_ALIGNMENT);

            indicatorStatus.setOpaque(true);
            indicatorStatus.setBackground(Color.lightGray);
            indicatorStatus.setForeground(Color.black);
            indicatorStatus.setHorizontalAlignment(SwingConstants.LEFT);

            // Set the sizes before setting the font!
            indicatorStatus.setPreferredSize(dimStatus);
            indicatorStatus.setMaximumSize(dimStatus);
            indicatorStatus.setMinimumSize(dimStatus);

            // Set up a default font and format, sized appropriately
            setStatusFont(strStatusFontDefault);
            setStatusFormat(strStatusFormat);

            indicatorStatus.setToolTipText(strToolTipString);

            this.add(indicatorStatus);
            }

        this.setToolTipText(strToolTipString);

        // Start out with a fixed size
        this.setPreferredSize(dimIndicator);
        this.setMaximumSize(dimIndicator);
        this.setMinimumSize(dimIndicator);
        }


    /***********************************************************************************************
     * Set the font name to use for the Value field, and resize to fit.
     *
     * @param fontname
     *
     * @throws IndicatorException
     */

    public final void setValueFont(final String fontname) throws IndicatorException
        {
        if (fontname.equals(""))
            {
            //throw new IndicatorException(ExceptionLibrary.EXCEPTION_NULL);
            }
        else
            {
            this.strValueFont = fontname;
            indicatorValue.setVisible(false);

            // Iterate to find the best-fit font size for the Value field
            findFontSize(indicatorValue,
                         dimValue,
                         strValueFont,
                         strValueFormat);

            // Restore the real indicated value in the new font
            setValue(strValueIndicated);
            indicatorValue.setVisible(true);
            }

        indicatorValue.revalidate();
        indicatorValue.repaint();
        }


    /***********************************************************************************************
     * Update the Value font to make best use of the space with the current Value.
     * @param template
     */

    private void applyBestFitValueFont(final String template)
        {
        // Iterate to find the best-fit font size for the supplied template,
        // not the Value format
        findFontSize(indicatorValue,
                     dimValue,
                     strValueFont,
                     template);
        }


    /***********************************************************************************************
     * Get the Indicator Value Format.
     *
     * @return String
     */

    public final String getValueFormat()
        {
        return (this.strValueFormat);
        }


    /***********************************************************************************************
     * Set the format required for the Value field, and resize the current font.
     *
     * @param format
     *
     * @throws IndicatorException
     */

    public final void setValueFormat(final String format) throws IndicatorException
        {
        if (format.equals(""))
            {
            //throw new IndicatorException(ExceptionLibrary.EXCEPTION_NULL);
            }
        else
            {
            this.strValueFormat = format;
            indicatorValue.setVisible(false);

            // Iterate to find the best-fit font size for the Value field
            findFontSize(indicatorValue,
                         dimValue,
                         strValueFont,
                         strValueFormat);

            // Restore the real indicated value in the new font
            setValue(strValueIndicated);
            indicatorValue.setVisible(true);
            }

        indicatorValue.revalidate();
        indicatorValue.repaint();
        }


    /***********************************************************************************************
     * Read the Font name currently used for the Value window.
     *
     * @return
     */

    public String getValueFont()
        {
        return(this.strValueFont);
        }


    /***********************************************************************************************
     * Set the Status Font.
     *
     * @param font
     *
     * @throws IndicatorException
     */

    public final void setStatusFont(final Font font) throws IndicatorException
        {
        setStatusFont(font.getFontName());
        }


    /***********************************************************************************************
     * Set the font name to use for the Status field, and resize to fit.
     *
     * @param fontname
     *
     * @throws IndicatorException
     */

    public final void setStatusFont(final String fontname) throws IndicatorException
        {
        if (boolStatusEnable)
            {
            if (fontname.equals(""))
                {
                //throw new IndicatorException(ExceptionLibrary.EXCEPTION_NULL);
                }
            else
                {
                this.strStatusFont = fontname;
                indicatorStatus.setVisible(false);

                // Iterate to find the best-fit font for the Status field
                findFontSize(indicatorStatus,
                             dimStatus,
                             strStatusFont,
                             strStatusFormat);

                // Restore the real indicated status in the new font
                setStatus(strStatusIndicated);
                indicatorStatus.setVisible(true);
                }

            indicatorValue.revalidate();
            indicatorValue.repaint();
            }
        }


    /***********************************************************************************************
     * Set the Color of the Status text.
     *
     * @param color
     */

    public final void setStatusColour(final Color color)
        {
        indicatorStatus.setForeground(color);
        indicatorValue.revalidate();
        indicatorValue.repaint();
        }

    /***********************************************************************************************
     * Set the Color of the Status bvackground.
     *
     * @param color
     */

    public final void setStatusBackground(final Color color)
        {
        indicatorStatus.setBackground(color);
        indicatorValue.revalidate();
        indicatorValue.repaint();
        }


    /***********************************************************************************************
     * Set the format required for the Status field, and resize the current font.
     *
     * @param format
     *
     * @throws IndicatorException
     */

    public final void setStatusFormat(final String format) throws IndicatorException
        {
        if (boolStatusEnable)
            {
            if (format.equals(""))
                {
                //throw new IndicatorException(ExceptionLibrary.EXCEPTION_NULL);
                }
            else
                {
                this.strStatusFormat = format;
                indicatorStatus.setVisible(false);

                // Iterate to find the best-fit font for the Status field
                findFontSize(indicatorStatus,
                             dimStatus,
                             strStatusFont,
                             strStatusFormat);

                // Restore the real indicated value in the new font
                setStatus(strStatusIndicated);
                indicatorStatus.setVisible(true);
                }

            indicatorValue.revalidate();
            indicatorValue.repaint();
            }
        }


    /***********************************************************************************************
     * Read the Font name currently used for the Status window.
     *
     * @return String
     */

    public String getStatusFont()
        {
        return(this.strStatusFont);
        }


    /***********************************************************************************************
     * Set the text to be displayed as the indicated value.
     *
     * @param value
     *
     * @throws IndicatorException
     */

    public final void setValue(final String value) throws IndicatorException
        {
        strValueIndicated = value;

        // Adjust the display for the current alignment
        switch(indicatorValue.getHorizontalAlignment())
            {
            case SwingConstants.LEFT:
                {
                System.out.println("Indicator - unexpected alignment!");
                indicatorValue.setText(SPACE + strValueIndicated);
                break;
                }

            case SwingConstants.RIGHT:
                {
                System.out.println("Indicator - unexpected alignment!");
                indicatorValue.setText(strValueIndicated + SPACE);
                break;
                }

            default:
                {
                //System.out.println("set indic value to " + strValueIndicated);
                indicatorValue.setText(strValueIndicated);
                break;
                }
            }

        applyBestFitValueFont(indicatorValue.getText());
        revalidate();
        repaint();
        }


    /***********************************************************************************************
     * Set the text to be displayed on the units indicator.
     *
     * @param units
     */

    public final void setUnits(final String units)
        {
        strUnitsIndicated = units;
        indicatorUnits.setText(strUnitsIndicated);
        indicatorUnits.revalidate();
        indicatorUnits.repaint();
        }


    /***********************************************************************************************
     * Indicate if the Units should be displayed.
     *
     * @return boolean
     */

    public boolean areUnitsVisible()
        {
        return (this.boolUnitsVisible);
        }


    /***********************************************************************************************
     * Indicate if the Units should be displayed.
     *
     * @param visible
     */

    public void setUnitsVisible(final boolean visible)
        {
        this.boolUnitsVisible = visible;
        }


    /***********************************************************************************************
     * Set the text to be displayed on the status bar, but only if there is one!
     *
     * @param status
     *
     * @throws IndicatorException
     */

    public final void setStatus(final String status) throws IndicatorException
        {
        if (boolStatusEnable)
            {
            // Test for more than the template, because 'M's are so big!
            if (status.length() > (strStatusFormat.length()+5))
                {
                indicatorStatus.setText("STRINGTOOLONG");
                indicatorStatus.revalidate();
                indicatorStatus.repaint();
                //throw new IndicatorException(ExceptionLibrary.EXCEPTION_STRINGTOOLONG + " {Status=" + strStatus + "}");
                }
            else
                {
                strStatusIndicated = status;
                indicatorStatus.setText(SPACE + strStatusIndicated);
                indicatorStatus.revalidate();
                indicatorStatus.repaint();
                }
            }
        }


    /***********************************************************************************************
     * Set the foreground colour of the indicated Value.
     *
     * @param foreground
     */

    public final void setValueForeground(final Color foreground)
        {
        this.colourForeground = foreground;
        indicatorValue.setForeground(colourForeground);
        indicatorValue.revalidate();
        indicatorValue.repaint();
        }


    /***********************************************************************************************
     * Set the background colour of the indicated Value.
     *
     * @param background
     */

    public final void setValueBackground(final Color background)
        {
        this.colourBackground = background;
        indicatorValue.setBackground(colourBackground);
        indicatorValue.revalidate();
        indicatorValue.repaint();
        }


    /***********************************************************************************************
     * Get the Tooltip.
     *
     * @return String
     */

    public final String getTooltip()
        {
        return (this.strToolTipString);
        }


    /***********************************************************************************************
     * Set the tooltip text for the whole indicator.
     *
     * @param tooltip
     */

    public final void setToolTip(final String tooltip)
        {
        // Record the new setting
        strToolTipString = tooltip;

        indicatorValue.setToolTipText(strToolTipString);
        indicatorValue.revalidate();
        indicatorValue.repaint();

        if (boolStatusEnable)
            {
            indicatorStatus.setToolTipText(strToolTipString);
            indicatorStatus.revalidate();
            indicatorStatus.repaint();
            }

        this.setToolTipText(strToolTipString);
        this.revalidate();
        this.repaint();
        }


    /***********************************************************************************************
     * Set the alignment of the text in the Value window.
     *
     * @param alignment
     *
     * @throws IllegalArgumentException
     */

    public final void setAlignment(final int alignment) throws IllegalArgumentException
        {
        indicatorValue.setHorizontalAlignment(alignment);
        indicatorValue.revalidate();
        indicatorValue.repaint();
        }


    /***********************************************************************************************
     * Add a mouse listener to the Indicated Value field.
     * This is done this way because the JLabel is inaccessible from outside.
     *
     * @param listener
     */

    public final void addMouseListenerToValue(final MouseListener listener)
        {
        indicatorValue.addMouseListener(listener);
        }


    /***********************************************************************************************
     * Get the Debug Mode flag.
     *
     * @return boolean
     */

    public boolean getDebugMode()
        {
        return(this.boolDebugMode);
        }


    /***********************************************************************************************
     * Set the Debug Mode flag.
     *
     * @param flag
     */

    public final void setDebugMode(final boolean flag)
        {
        this.boolDebugMode = flag;
        }


    /***********************************************************************************************
     * Show a debug message.
     *
     * @param message
     */

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
                strSeparator = SPACE;
                }

            LOGGER.debug(Chronos.timeNow() + SPACE
                               + this.getClass().getName()
                               + strSeparator
                               + message);
            }
        }
    }
