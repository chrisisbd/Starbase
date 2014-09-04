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

package org.lmn.fc.ui.widgets.impl;

import org.lmn.fc.frameworks.starbase.plugins.observatory.events.CommitChangeEvent;
import org.lmn.fc.frameworks.starbase.plugins.observatory.events.CommitChangeListener;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.ui.components.UIComponent;
import org.lmn.fc.ui.widgets.ControlKnobInterface;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Arc2D;


/***************************************************************************************************
 * ControlKnobUIComponent.
 *
 * I am not sure where the original code came from, but it is getting some fairly heavy reworking...
 *
 * Some useful links:
 *
 * http://developmentality.wordpress.com/2010/02/02/how-to-make-a-solar-system-introduction-to-affine-transformations-and-java-2d/
 */

public class ControlKnobUIComponent extends UIComponent
                                    implements ControlKnobInterface
    {
    private static final Color COLOR_POINTER = new Color(255, 255, 51);
    private final static Color COLOR_FOCUS   = new Color(255, 100, 113);
    private final static Color COLOR_KNOB    = new Color(198, 198, 198);
    private final static Color COLOR_SPOT    = new Color(137, 137, 137);
    private final static Color COLOR_TICKS   = new Color(0, 0, 0);
    private static final Color COLOR_IDLE    = new Color(142, 207, 237);

    // Set the antialiasing to get the right look!
    private final static RenderingHints ANTI_ALIAS = new RenderingHints(RenderingHints.KEY_ANTIALIASING,
                                                                        RenderingHints.VALUE_ANTIALIAS_ON);

    private static final int TICK_COUNT = 8;
    private static final double RADIANS_PER_TICK = 2.0 * Math.PI / TICK_COUNT;
    private static final int EXCLUDED_TICK = 2;
    private static final float CLICK_SPEED = 0.01f;

    // Arc angle START begins at the 3 o'clock point, measured anticlockwise
    // So 3 o'clock is zero, 9 o'clock is 180 degrees, 6 o'clock is 270 degrees
    // Arc EXTENT begins at the angle of START, and moves clockwise
    // See e.g. http://johnsogg.blogspot.co.uk/2010/01/how-to-use-javas-javaawtgeomarc2d.html

    private final static double ARC_START_DEGREES  = (5.0 * 360.0) / 8.0;
    private final static double ARC_EXTENT_DEGREES = (6.0 * 360.0) / 8.0;
    private final static double ARC_END_DEGREES = (7.0 * 360.0) / 8.0;

    private final static double ARC_START_RADIANS  = (ARC_START_DEGREES / 360) * Math.PI * 2;
    private final static double ARC_EXTENT_RADIANS = (ARC_EXTENT_DEGREES / 360) * Math.PI * 2;
    private final static double ARC_END_RADIANS = (ARC_END_DEGREES / 360) * Math.PI * 2;


    // Injections
    private final Dimension dimKnobSize;
    private final boolean   boolShowTicks;
    private final int intTickLength;
    private final double dblScaleMax;
    private final double dblScaleMin;
    private final String strTooltip;
    private final ColourInterface colourBackground;

    private final EventListenerList listListeners;
    private final Arc2D arc2D;
    private ChangeEvent changeEvent;
    private CommitChangeEvent commitChangeEvent;
    private double dblKnobValue;
    private double dblInitialValue;
    private double dblKnobAngleToDraw;
    private double dblLastAngle;
    private int intCentrePx;


    /***********************************************************************************************
     * The ControlKnobUIComponent.
     *
     * @param size
     * @param showticks
     * @param ticklength
     * @param scalemin
     * @param scalemax
     * @param tooltip
     * @param colourbackground
     */

    public ControlKnobUIComponent(final Dimension size,
                                  final boolean showticks,
                                  final int ticklength,
                                  final double scalemin,
                                  final double scalemax,
                                  final String tooltip,
                                  final ColourInterface colourbackground)
        {
        super();

        // Injections
        this.dimKnobSize = size;
        this.boolShowTicks = showticks;
        this.intTickLength = ticklength;
        this.dblScaleMin = scalemin;
        this.dblScaleMax = scalemax;
        this.strTooltip = tooltip;
        this.colourBackground = colourbackground;

        this.arc2D = new Arc2D.Float(Arc2D.PIE);
        this.listListeners = new EventListenerList();
        this.changeEvent = null;
        this.commitChangeEvent = null;
        this.dblKnobValue = 0.0;
        this.dblKnobAngleToDraw = ARC_START_RADIANS;
        }


    /**********************************************************************************************/
    /* UI State                                                                                   */
    /***********************************************************************************************
     * Initialise this UIComponent.
     */

    public void initialiseUI()
        {
        final String SOURCE = "ControlKnobUIComponent.initialiseUI() ";

        super.initialiseUI();

        // Build the UI
        removeAll();
        setMinimumSize(getKnobSize());
        setPreferredSize(getKnobSize());
        setMaximumSize(getKnobSize());
        setToolTipText(getTooltip());
        setBackground(getBackgroundColour().getColor());

        // ToDo Sort this mess out....
        arc2D.setAngleStart(ARC_START_DEGREES);

        addMouseListener(new MouseAdapter()
            {
            public void mousePressed(final MouseEvent event)
                {
                if (isEnabled())
                    {
                    final int xpos;
                    final int ypos;

                    dblInitialValue = getValue();

                    // Fix last angle
                    xpos = intCentrePx - event.getX();
                    ypos = intCentrePx - event.getY();

                    dblLastAngle = Math.atan2(xpos, ypos);

                    requestFocus();
                    }
                }

            public void mouseClicked(final MouseEvent event)
                {
                if (isEnabled())
                    {
                    if (SwingUtilities.isLeftMouseButton(event)
                        && (!event.isShiftDown())
                        && (!event.isAltDown())
                        && (event.getClickCount() == 1))
                        {
                        if (!event.isControlDown())
                            {
                            // ToDo Review arc extent
                            arc2D.setAngleExtent(-(ARC_EXTENT_DEGREES + 20));

                            if (arc2D.contains(event.getX(), event.getY()))
                                {
                                // ToDo Review arc extent
                                arc2D.setAngleExtent((180.0 / Math.PI) * (dblKnobAngleToDraw - ARC_START_RADIANS) - 10);

                                if (arc2D.contains(event.getX(), event.getY()))
                                    {
                                    decValue();
                                    }
                                else
                                    {
                                    incValue();
                                    }
                                }
                            }
                        else
                            {
                            // ctrl-Click resets the control to the centre position
                            setCentre();

                            // Treat this as the end of a movement, and commit
                            fireCommitChangeEvent();
                            }
                        }
                    }
                }


            public void mouseReleased(final MouseEvent event)
                {
                if (isEnabled())
                    {
                    fireCommitChangeEvent();
                    }
                }
            });

        // Let the user control the knob with the mouse
        addMouseMotionListener(new MouseMotionAdapter()
            {
            public void mouseDragged(final MouseEvent mouseEvent)
                {
                if (isEnabled())
                    {
                    final int intXpx;
                    final int intYpx;
                    final double dblMouseAngle;
                    final double dblDeltaAngle;

                    //System.out.println("DRAG [mouse.x=" + mouseEvent.getX() + "] [mouse.y=" + mouseEvent.getY() + "] [centrepx=" + intCentrePx + "]");

                    // Do not allow the cursor to move out of the allowed arc
                    //                    if ((mouseEvent.getY() > intCentrePx)
                    //                        && (Math.abs(intCentrePx - mouseEvent.getX()) < (getWidth() >> 2)))
                    //                        {
                    //                        System.out.println("Y KEEPOUT");
                    //                        }
                    //                    else
                    //                        {
                    final double dblCentreScale;
                    final double dblPreviousValue;
                    final double dblDeltaValue;
                    final double dblNewValue;

                    dblCentreScale = (getScaleMax() - getScaleMin()) / 2.0;
                    dblPreviousValue = getValue();

                    // Measure relative to the middle of the knob!
                    intXpx = intCentrePx - mouseEvent.getX();
                    intYpx = intCentrePx - mouseEvent.getY();

                    dblMouseAngle = Math.atan2(intXpx, intYpx);
                    dblDeltaAngle = dblLastAngle - dblMouseAngle;
                    dblDeltaValue = (getScaleMax() - getScaleMin()) * dblDeltaAngle / ARC_EXTENT_RADIANS;
                    dblNewValue = getValue() + dblDeltaValue;

                    //System.out.println("DRAG dblAngleDifference=" + dblDeltaAngle + " dblDeltaValue=" + dblDeltaValue + " dblNewValue=" + dblNewValue);

                    // Can we make use of this new value?
                    // Block anything that swaps sides below the horizontal centre
                    if ((((dblPreviousValue > dblCentreScale)
                          && (dblNewValue < dblCentreScale))
                         || ((dblPreviousValue < dblCentreScale)
                             && (dblNewValue > dblCentreScale)))
                        && (mouseEvent.getY() > intCentrePx))
                        {
                        //System.out.println("DRAG !!!!!!!!!!!!!!!!!!!!!!!!!!!! Could not use new value dblNewValue=" + dblNewValue);
                        }
                    else
                        {
                        // This will set the knob angle to be drawn, and is range limited
                        setValue(dblNewValue);
                        }

                    dblLastAngle = dblMouseAngle;
                    //                        }
                    }
                }


            public void mouseMoved(final MouseEvent me)
                {
                }
            });

        // Let the user control the knob with the keyboard
        addKeyListener(new KeyListener()
            {
            public void keyTyped(final KeyEvent e)
                {
                }


            public void keyReleased(final KeyEvent keyEvent)
                {
                if (isEnabled())
                    {
                    final int intKey;

                    intKey = keyEvent.getKeyCode();

                    if ((intKey == KeyEvent.VK_RIGHT)
                        || (intKey == KeyEvent.VK_KP_RIGHT)
                        || (intKey == KeyEvent.VK_LEFT)
                        || (intKey == KeyEvent.VK_KP_LEFT))
                        {
                        fireCommitChangeEvent();
                        }
                    }
                }


            public void keyPressed(final KeyEvent keyEvent)
                {
                if (isEnabled())
                    {
                    final int intKey;

                    intKey = keyEvent.getKeyCode();

                    if ((intKey == KeyEvent.VK_RIGHT) || (intKey == KeyEvent.VK_KP_RIGHT))
                        {
                        incValue();
                        }
                    else if ((intKey == KeyEvent.VK_LEFT) || (intKey == KeyEvent.VK_KP_LEFT))
                        {
                        decValue();
                        }
                    }
                }
            });

        // Handle focus so that the knob gets the correct focus highlighting.
        addFocusListener(new FocusListener()
            {
            public void focusGained(final FocusEvent event)
                {
                if (isEnabled())
                    {
                    repaint();
                    }
                }

            public void focusLost(final FocusEvent event)
                {
                if (isEnabled())
                    {
                    repaint();
                    }
                }
            });
        }


    /***********************************************************************************************
     * Run this UIComponent.
     */

    public void runUI()
        {
        final String SOURCE = "ControlKnobUIComponent.runUI() ";

        super.runUI();

        LOGGER.debugStateEvent(LOADER_PROPERTIES.isStateDebug(),
                               SOURCE + "[ui.state=" + getUIState().getName() + "]");
        }


    /***********************************************************************************************
     * Stop this UIComponent.
     */

    public void stopUI()
        {
        final String SOURCE = "ControlKnobUIComponent.stopUI() ";

        super.stopUI();

        LOGGER.debugStateEvent(LOADER_PROPERTIES.isStateDebug(),
                               SOURCE + "[ui.state=" + getUIState().getName() + "]");
        }


    /***********************************************************************************************
     * Dispose of all components of this UIComponent.
     */

    public void disposeUI()
        {
        final String SOURCE = "ControlKnobUIComponent.disposeUI() ";

        stopUI();
        super.disposeUI();

        LOGGER.debugStateEvent(LOADER_PROPERTIES.isStateDebug(),
                               SOURCE + "[ui.state=" + getUIState().getName() + "]");
        }


    /***********************************************************************************************
     * Paint the Control.
     *
     * @param graphics
     */

    public void paint(final Graphics graphics)
        {
        final String SOURCE = "ControlKnobUIComponent.paint() ";
        final Graphics2D graphics2D;
        final int intWidth;
        final int intHeight;
        final int intOffset;
        final int intKnobSize;
        final int intPointerTipX;
        final int intPointerTipY;
        final int intCircleInset;
        final int intPointerCentreDx;
        final int intPointerCentreDy;

        // Don't forget to call the super method
        super.paintComponent(graphics);

        graphics2D = (Graphics2D) graphics;
        intWidth = getWidth();
        intHeight = getHeight();

        // Calculate the size and centre of the Knob
        if (isShowTicks())
            {
            // Leave an area around the knob to hold the ticks
            intOffset = getTickLength();
            intKnobSize = Math.min(intWidth, intHeight) - (intOffset << 1);
            intCentrePx = (intKnobSize >> 1) + intOffset;
            }
        else
            {
            intOffset = 0;
            intKnobSize = Math.min(intWidth, intHeight);
            intCentrePx = (intKnobSize >> 1);
            }

        graphics2D.setBackground(getBackgroundColour().getColor());
        graphics2D.addRenderingHints(ANTI_ALIAS);

        // Set the size of the mouse click area
        arc2D.setFrame(intOffset,
                       intOffset,
                       intKnobSize + (intOffset << 1),
                       intKnobSize + (intOffset << 1));

        //-----------------------------------------------------------------------------------------

        if (boolShowTicks)
            {
            graphics.setColor(COLOR_TICKS);

            // Translate the origin to the centre of the knob
            graphics2D.translate(intCentrePx, intCentrePx);

            for (int intTickIndex = 0;
                 intTickIndex < TICK_COUNT;
                 intTickIndex++)
                {
                if (intTickIndex != EXCLUDED_TICK)
                    {
                    // We have rotated about the origin
                    // Draw a ray out along the x axis of new coordinate system
                    // Try to centralise the tick - preferable to have an odd number of pixels
                    graphics2D.fillRect((intKnobSize >> 1),
                                        -(getTickLength() >> 1),
                                        getTickLength(),
                                        getTickLength());
                    }

                graphics2D.rotate(RADIANS_PER_TICK);
                }

            // Translate the context back again
            graphics2D.translate(-intCentrePx, -intCentrePx);
            }

        //-----------------------------------------------------------------------------------------
        // Paint the focus indicator circle
        // Overwrite any tick line centres

        if ((hasFocus())
            && (isEnabled()))
            {
            graphics.setColor(COLOR_FOCUS);
            }
        else
            {
            graphics.setColor(COLOR_IDLE);
            }

        graphics.fillOval(intOffset,
                          intOffset,
                          intKnobSize,
                          intKnobSize);

        // Refill the centre of the knob, always the same colour
        graphics.setColor(COLOR_KNOB);
        graphics.fillOval(intOffset + getTickLength(),
                          intOffset + getTickLength(),
                          intKnobSize - (getTickLength() << 1),
                          intKnobSize - (getTickLength() << 1));

        //-----------------------------------------------------------------------------------------
        // Inner circle and pointer

        graphics.setColor(COLOR_POINTER);

        intCircleInset = Math.max(intKnobSize / 7, 3);

        graphics.drawOval(intOffset + intCircleInset,
                          intOffset + intCircleInset,
                          intKnobSize - (intCircleInset << 1),
                          intKnobSize - (intCircleInset << 1));
        graphics.drawOval(intOffset + intCircleInset + 1,
                          intOffset + intCircleInset + 1,
                          intKnobSize - (intCircleInset << 1) - 2,
                          intKnobSize - (intCircleInset << 1) - 2);

        graphics.setColor(COLOR_KNOB.darker());
        graphics.drawOval(intOffset + intCircleInset + 2,
                          intOffset + intCircleInset + 2,
                          intKnobSize - (intCircleInset << 1) - 4,
                          intKnobSize - (intCircleInset << 1) - 4);
        graphics.drawOval(intOffset + intCircleInset + 3,
                          intOffset + intCircleInset + 3,
                          intKnobSize - (intCircleInset << 1) - 6,
                          intKnobSize - (intCircleInset << 1) - 6);

        // Draw the pointer as three lines to the same point at the tip
        intPointerTipX = intOffset + (intKnobSize >> 1) + (int) ((intKnobSize >> 1) * Math.cos(dblKnobAngleToDraw));
        intPointerTipY = intOffset + (intKnobSize >> 1) - (int) ((intKnobSize >> 1) * Math.sin(dblKnobAngleToDraw));
        intPointerCentreDx = (int) (Math.sin(dblKnobAngleToDraw) * 2);
        intPointerCentreDy = (int) (Math.cos(dblKnobAngleToDraw) * 2);

        graphics.setColor(COLOR_KNOB.darker().darker());
        graphics.drawLine(intOffset + (intKnobSize >> 1),
                          intOffset + (intKnobSize >> 1),
                          intPointerTipX,
                          intPointerTipY);

        graphics.drawLine(intOffset + (intKnobSize >> 1) + intPointerCentreDx,
                          intOffset + (intKnobSize >> 1) + intPointerCentreDy,
                          intPointerTipX,
                          intPointerTipY);

        graphics.drawLine(intOffset + (intKnobSize >> 1) - intPointerCentreDx,
                          intOffset + (intKnobSize >> 1) - intPointerCentreDy,
                          intPointerTipX,
                          intPointerTipY);

        // Finally the darker centre spot, ideally an odd number of pixels
        graphics.setColor(COLOR_SPOT);
        graphics.fillOval(intCentrePx - 2,
                          intCentrePx - 2,
                          5,
                          5);
        }


    /***********************************************************************************************
     * Control the enable state of the Control.
     *
     * @param enabled
     */

    public void setEnabled(final boolean enabled)
        {
        super.setEnabled(enabled);
        }


    /**********************************************************************************************/
    /* Knob State                                                                                 */
    /***********************************************************************************************
     * Get the current value of the Control.
     *
     * @return float
     */

    public double getValue()
        {
        return (this.dblKnobValue);
        }


    /***********************************************************************************************
     * Set the Value on the Control, forcing the value to be {ScaleMin...ScaleMax}.
     *
     * @param value
     */

    public void setValue(final double value)
        {
        this.dblKnobValue = value;

        if (getValue() < getScaleMin())
            {
            this.dblKnobValue = getScaleMin();
            }

        if (getValue() > getScaleMax())
            {
            this.dblKnobValue = getScaleMax();
            }

        dblKnobAngleToDraw = ARC_START_RADIANS - ARC_EXTENT_RADIANS * ((getValue()  - getScaleMin()) / (getScaleMax() - getScaleMin())) ;

        repaint();
        fireChangeEvent();
        }


    /***********************************************************************************************
     * Set the Control to indicate centre, wherever that may be.
     */

    public void setCentre()
        {
        setValue(((getScaleMax() - getScaleMin()) / 2.0) + getScaleMin());
        }


    /***********************************************************************************************
     * Increment the Knob Value by a fixed fraction of the range.
     */

    private void incValue()
        {
        setValue(getValue() + ((getScaleMax() - getScaleMin()) * CLICK_SPEED));
        }


    /***********************************************************************************************
     * Decrement the Knob Value by a fixed fraction of the range.
     */

    private void decValue()
        {
        setValue(getValue() - ((getScaleMax() - getScaleMin()) * CLICK_SPEED));
        }


    /**********************************************************************************************/
    /* Events                                                                                     */
    /***********************************************************************************************
     * Add a ChangeListener.
     *
     * @param listener
     */

    public void addChangeListener(final ChangeListener listener)
        {
        listListeners.add(ChangeListener.class, listener);
        }


    /***********************************************************************************************
     * Remove a ChangeListener.
     *
     * @param listener
     */

    public void removeChangeListener(final ChangeListener listener)
        {
        listListeners.remove(ChangeListener.class, listener);
        }


    /***********************************************************************************************
     * Add a CommitChangeListener.
     *
     * @param listener
     */

    public void addCommitChangeListener(final CommitChangeListener listener)
        {
        listListeners.add(CommitChangeListener.class, listener);
        }


    /***********************************************************************************************
     * Remove a CommitChangeListener.
     *
     * @param listener
     */

    public void removeCommitChangeListener(final CommitChangeListener listener)
        {
        listListeners.remove(CommitChangeListener.class, listener);
        }


    /***********************************************************************************************
     * Fire a ChangeEvent to show that the Control has changed.
     */

    public void fireChangeEvent()
        {
        // Guaranteed to return a non-null array
        final Object[] listeners;

        listeners = listListeners.getListenerList();

        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2)
            {
            if (listeners[i].equals(ChangeListener.class))
                {
                // Lazily create the event:
                if (changeEvent == null)
                    {
                    changeEvent = new ChangeEvent(this);
                    }

                ((ChangeListener) listeners[i + 1]).stateChanged(changeEvent);
                }
            }
        }


    /***********************************************************************************************
     * Fire a CommitChangeEvent to show that the Control change is complete.
     */

    public void fireCommitChangeEvent()
        {
        // Guaranteed to return a non-null array
        final Object[] listeners;

        listeners = listListeners.getListenerList();

        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2)
            {
            if (listeners[i].equals(CommitChangeListener.class))
                {
                // Lazily create the event:
                if (commitChangeEvent == null)
                    {
                    commitChangeEvent = new CommitChangeEvent(this);
                    }

                ((CommitChangeListener) listeners[i + 1]).commitChange(commitChangeEvent);
                }
            }
        }


    /**********************************************************************************************/
    /* Injections                                                                                 */
    /***********************************************************************************************
     * Get the size of an individual knob.
     *
     * @return Dimension
     */

    private Dimension getKnobSize()
        {
        return (this.dimKnobSize);
        }


    /***********************************************************************************************
     * Indicate if the knobs should show tick marks.
     *
     * @return boolean
     */

    private boolean isShowTicks()
        {
        return (this.boolShowTicks);
        }


    /***********************************************************************************************
     * Get the length of the tick marks.
     *
     * @return int
     */

    private int getTickLength()
        {
        return (this.intTickLength);
        }


    /***********************************************************************************************
     * Get the scale Minimum value of the control.
     *
     * @return double
     */

    private double getScaleMin()
        {
        return (this.dblScaleMin);
        }


    /***********************************************************************************************
     * Get the scale Maximum value of the control.
     *
     * @return double
     */

    private double getScaleMax()
        {
        return (this.dblScaleMax);
        }


    /***********************************************************************************************
     * Get the tooltip for the control.
     *
     * @return String
     */

    private String getTooltip()
        {
        return (this.strTooltip);
        }


    /***********************************************************************************************
     * Get the background colour for the control.
     *
     * @return ColourInterface
     */

    private ColourInterface getBackgroundColour()
        {
        return (this.colourBackground);
        }
    }
