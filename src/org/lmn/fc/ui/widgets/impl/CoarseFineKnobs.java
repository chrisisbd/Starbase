// Copyright 2000, 2001, 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012
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
import org.lmn.fc.ui.layout.BoxLayoutFixed;
import org.lmn.fc.ui.widgets.ControlKnobInterface;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import java.awt.*;


/***************************************************************************************************
 * CoarseFineKnobs.
 */

public class CoarseFineKnobs extends UIComponent
                             implements ControlKnobInterface
    {
    private static final int WIDTH_SEPARATOR = 5;

    // Injections
    private final Dimension dimKnobSize;
    private final boolean boolShowTicks;
    private final int intTickLength;
    private final double dblCoarseScaleMin;
    private final double dblCoarseScaleMax;
    private final double dblFineScaleMin;
    private final double dblFineScaleMax;
    private final String strCoarseTooltip;
    private final String strFineTooltip;
    private final ColourInterface colourBackground;

    private ControlKnobInterface knobCoarse;
    private ControlKnobInterface knobFine;
    private ChangeEvent changeEvent;
    private CommitChangeEvent commitChangeEvent;
    private final EventListenerList listListeners;


    /***********************************************************************************************
     * Construct CoarseFineKnobs.
     * The coarse knob has values {CoarseScaleMin...CoarseScaleMax}, the fine knob {FineScaleMin...FineScaleMax}.
     * The combined value cannot be less than CoarseScaleMin, or exceed CoarseScaleMax.
     * If both FineScaleMin and FineScaleMax are zero, then do not add a fine control knob.
     *
     * @param knobsize
     * @param showticks
     * @param ticklength
     * @param coarsescalemin
     * @param coarsescalemax
     * @param finescalemin
     * @param finescalemax
     * @param coarsetooltip
     * @param finetooltip
     */

    public CoarseFineKnobs(final Dimension knobsize,
                           final boolean showticks,
                           final int ticklength,
                           final double coarsescalemin,
                           final double coarsescalemax,
                           final double finescalemin,
                           final double finescalemax,
                           final String coarsetooltip,
                           final String finetooltip,
                           final ColourInterface colourbackground)
        {
        super();

        // Injections
        this.dimKnobSize = knobsize;
        this.boolShowTicks = showticks;
        this.intTickLength = ticklength;
        this.dblCoarseScaleMin = coarsescalemin;
        this.dblCoarseScaleMax = coarsescalemax;
        this.dblFineScaleMin = finescalemin;
        this.dblFineScaleMax = finescalemax;
        this.strCoarseTooltip = coarsetooltip;
        this.strFineTooltip = finetooltip;
        this.colourBackground = colourbackground;

        this.knobCoarse = null;
        this.knobFine = null;
        this.changeEvent = null;
        this.commitChangeEvent = null;
        this.listListeners = new EventListenerList();
        }


    /**********************************************************************************************/
    /* UI State                                                                                   */
    /***********************************************************************************************
     * Initialise this UIComponent.
     */

    public void initialiseUI()
        {
        final String SOURCE = "CoarseFineKnobs.initialiseUI() ";
        final Dimension dimControl;

        super.initialiseUI();

        // Build the UI
        removeAll();
        setLayout(new BoxLayoutFixed(this, BoxLayoutFixed.X_AXIS));
        setBackground(getBackgroundColour().getColor());
        setAlignmentX(Component.LEFT_ALIGNMENT);
        setAlignmentY(Component.CENTER_ALIGNMENT);

        // See if the fine adjustment is required
        if (!((getFineScaleMin() == 0) && (getFineScaleMax() == 0)))
            {
            dimControl = new Dimension((int) (getKnobSize().getWidth() * 2) + WIDTH_SEPARATOR,
                                       (int) getKnobSize().getHeight());
            }
        else
            {
            dimControl = new Dimension((int) getKnobSize().getWidth(),
                                       (int) getKnobSize().getHeight());
            }

        setMinimumSize(dimControl);
        setMaximumSize(dimControl);
        setPreferredSize(dimControl);

        setCoarseKnob(new ControlKnobUIComponent(getKnobSize(),
                                                 isShowTicks(),
                                                 getTickLength(),
                                                 getCoarseScaleMin(),
                                                 getCoarseScaleMax(),
                                                 getCoarseTooltip(),
                                                 getBackgroundColour()));
        getCoarseKnob().initialiseUI();
        getCoarseKnob().setCentre();
        getCoarseKnob().addChangeListener(new ChangeListener()
            {
            public void stateChanged(final ChangeEvent event)
                {
                // If the Coarse knob changes, always set the Fine knob to its zero position
                getFineKnob().setCentre();
                fireChangeEvent();
                }
            });

        getCoarseKnob().addCommitChangeListener(new CommitChangeListener()
            {
            public void commitChange(final CommitChangeEvent event)
                {
                fireCommitChangeEvent();
                }
            });

        setFineKnob(new ControlKnobUIComponent(getKnobSize(),
                                               isShowTicks(),
                                               getTickLength(),
                                               getFineScaleMin(),
                                               getFineScaleMax(),
                                               getFineTooltip(),
                                               getBackgroundColour()));
        getFineKnob().initialiseUI();
        getFineKnob().setCentre();
        getFineKnob().addChangeListener(new ChangeListener()
            {
            public void stateChanged(final ChangeEvent event)
                {
                fireChangeEvent();
                }
            });

        getFineKnob().addCommitChangeListener(new CommitChangeListener()
            {
            public void commitChange(final CommitChangeEvent event)
                {
                fireCommitChangeEvent();
                }
            });

        // Assemble the UI
        add((Component)getCoarseKnob());

        // See if the fine adjustment is required
        if (!((getFineScaleMin() == 0) && (getFineScaleMax() == 0)))
            {
            add(Box.createHorizontalStrut(WIDTH_SEPARATOR));
            add((Component) getFineKnob());
            }
        }

    /***********************************************************************************************
     * Run this UIComponent.
     */

    public void runUI()
        {
        final String SOURCE = "CoarseFineKnobs.runUI() ";

        super.runUI();

        if (getCoarseKnob() != null)
            {
            getCoarseKnob().runUI();
            }

        if (getFineKnob() != null)
            {
            getFineKnob().runUI();
            }

        LOGGER.debugStateEvent(LOADER_PROPERTIES.isStateDebug(),
                               SOURCE + "[ui.state=" + getUIState().getName() + "]");
        }


    /***********************************************************************************************
     * Stop this UIComponent.
     */

    public void stopUI()
        {
        final String SOURCE = "CoarseFineKnobs.stopUI() ";

        if (getCoarseKnob() != null)
            {
            getCoarseKnob().stopUI();
            }

        if (getFineKnob() != null)
            {
            getFineKnob().stopUI();
            }

        super.stopUI();

        LOGGER.debugStateEvent(LOADER_PROPERTIES.isStateDebug(),
                               SOURCE + "[ui.state=" + getUIState().getName() + "]");
        }


    /***********************************************************************************************
     * Dispose of all components of this UIComponent.
     */

    public void disposeUI()
        {
        final String SOURCE = "CoarseFineKnobs.disposeUI() ";

        stopUI();

        if (getCoarseKnob() != null)
            {
            getCoarseKnob().disposeUI();
            }

        if (getFineKnob() != null)
            {
            getFineKnob().disposeUI();
            }

        super.disposeUI();

        LOGGER.debugStateEvent(LOADER_PROPERTIES.isStateDebug(),
                               SOURCE + "[ui.state=" + getUIState().getName() + "]");
        }


    /***********************************************************************************************
     * Control the enable state of the Control.
     *
     * @param enabled
     */

    public void setEnabled(final boolean enabled)
        {
        super.setEnabled(enabled);

        if (getCoarseKnob() != null)
            {
            getCoarseKnob().setEnabled(enabled);
            }

        if (getFineKnob() != null)
            {
            getFineKnob().setEnabled(enabled);
            }
        }


    /**********************************************************************************************/
    /* Knob State                                                                                 */
    /**********************************************************************************************
     * Get the current value of the Control.
     * This combines the Coarse value and scale with the Fine value and scale.
     * The coarse knob has values {CoarseScaleMin...CoarseScaleMax}, the fine knob {FineScaleMin...FineScaleMax}.
     * The combined value cannot be less than CoarseScaleMin, or exceed CoarseScaleMax.
     *
     * @return double
     */

    public double getValue()
        {
        double dblValue;

        dblValue = getCoarseKnob().getValue() + getFineKnob().getValue();

        dblValue = Math.max(dblValue, getCoarseScaleMin());
        dblValue = Math.min(dblValue, getCoarseScaleMax());

        return (dblValue);
        }


    /**********************************************************************************************
     * Set the Value on the Control.
     * This will always set the Fine control back to zero.
     * The coarse knob has values {CoarseScaleMin...CoarseScaleMax}, the fine knob {FineScaleMin...FineScaleMax}.
     * The combined value cannot be less than CoarseScaleMin, or exceed CoarseScaleMax.
     *
     * @param value
     */

    public void setValue(final double value)
        {
        double dblRealValue;

        dblRealValue = Math.max(value, getCoarseScaleMin());
        dblRealValue = Math.min(dblRealValue, getCoarseScaleMax());

        getCoarseKnob().setValue(dblRealValue);
        getFineKnob().setCentre();
        }


    /***********************************************************************************************
     * Set the Control to indicate centre, wherever that may be.
     */

    public void setCentre()
        {
        getCoarseKnob().setCentre();
        getFineKnob().setCentre();
        }


    /***********************************************************************************************
     * Get the Coarse Knob.
     *
     * @return ControlKnobInterface
     */

    private ControlKnobInterface getCoarseKnob()
        {
        return (this.knobCoarse);
        }


    /***********************************************************************************************
     * Set the Coarse Knob.
     *
     * @param knob
     */

    private void setCoarseKnob(final ControlKnobInterface knob)
        {
        this.knobCoarse = knob;
        }


    /***********************************************************************************************
     * Get the Fine Knob.
     *
     * @return ControlKnobInterface
     */

    private ControlKnobInterface getFineKnob()
        {
        return (this.knobFine);
        }


    /***********************************************************************************************
     * Set the Fine Knob.
     *
     * @param knob
     */

    private void setFineKnob(final ControlKnobInterface knob)
        {
        this.knobFine = knob;
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
        getListenerList().add(ChangeListener.class, listener);
        }


    /***********************************************************************************************
     * Remove a ChangeListener.
     *
     * @param listener
     */

    public void removeChangeListener(final ChangeListener listener)
        {
        getListenerList().remove(ChangeListener.class, listener);
        }


    /***********************************************************************************************
     * Add a CommitChangeListener.
     *
     * @param listener
     */

    public void addCommitChangeListener(final CommitChangeListener listener)
        {
        getListenerList().add(CommitChangeListener.class, listener);
        }


    /***********************************************************************************************
     * Remove a CommitChangeListener.
     *
     * @param listener
     */

    public void removeCommitChangeListener(final CommitChangeListener listener)
        {
        getListenerList().remove(CommitChangeListener.class, listener);
        }


    /***********************************************************************************************
     * Fire a ChangeEvent to show that the Control has changed.
     */

    public void fireChangeEvent()
        {
        // Guaranteed to return a non-null array
        final Object[] listeners;

        listeners = getListenerList().getListenerList();

        // Process the listeners last to first,
        // notifying those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2)
            {
            if (listeners[i].equals(ChangeListener.class))
                {
                // Lazily create the event:
                if (this.changeEvent == null)
                    {
                    this.changeEvent = new ChangeEvent(this);
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

        listeners = getListenerList().getListenerList();

        // Process the listeners last to first,
        // notifying those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2)
            {
            if (listeners[i].equals(CommitChangeListener.class))
                {
                // Lazily create the event:
                if (this.commitChangeEvent == null)
                    {
                    this.commitChangeEvent = new CommitChangeEvent(this);
                    }

                ((CommitChangeListener) listeners[i + 1]).commitChange(commitChangeEvent);
                }
            }
        }


    /***********************************************************************************************
     * Get the List of EventListeners.
     *
     * @return EventListenerList
     */

    private EventListenerList getListenerList()
        {
        return (this.listListeners);
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
     * Get the scale factor Minimum for the Coarse knob.
     *
     * @return double
     */

    private double getCoarseScaleMin()
        {
        return (this.dblCoarseScaleMin);
        }


    /***********************************************************************************************
     * Get the scale factor Maximum for the Coarse knob.
     *
     * @return double
     */

    private double getCoarseScaleMax()
        {
        return (this.dblCoarseScaleMax);
        }


    /***********************************************************************************************
     * Get the scale factor Minimum for the Fine knob.
     *
     * @return double
     */

    private double getFineScaleMin()
        {
        return (this.dblFineScaleMin);
        }


    /***********************************************************************************************
     * Get the scale factor Maximum for the Fine knob.
     *
     * @return double
     */

    private double getFineScaleMax()
        {
        return (this.dblFineScaleMax);
        }


    /***********************************************************************************************
     * Get the tooltip for the Coarse knob.
     *
     * @return String
     */

    private String getCoarseTooltip()
        {
        return (this.strCoarseTooltip);
        }


    /***********************************************************************************************
     * Get the tooltip for the Fine knob.
     *
     * @return String
     */

    private String getFineTooltip()
        {
        return (this.strFineTooltip);
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
