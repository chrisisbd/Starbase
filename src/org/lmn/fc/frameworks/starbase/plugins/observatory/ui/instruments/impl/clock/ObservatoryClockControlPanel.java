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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.clock;

import org.lmn.fc.common.exceptions.FrameworkException;
import org.lmn.fc.common.utilities.time.TimeSystem;
import org.lmn.fc.frameworks.starbase.plugins.observatory.MetadataDictionary;
import org.lmn.fc.frameworks.starbase.plugins.observatory.events.InstrumentStateChangedEvent;
import org.lmn.fc.frameworks.starbase.plugins.observatory.events.ObservatoryClockChangedEvent;
import org.lmn.fc.frameworks.starbase.plugins.observatory.events.ObservatoryClockChangedListener;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ControlPanelInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryUIInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.InstrumentUIComponentDecorator;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.InstrumentUIHelper;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.model.plugins.FrameworkPlugin;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;
import org.lmn.fc.ui.widgets.impl.Indicator;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/***********************************************************************************************
 * The ObservatoryClockControlPanel.
 */

public final class ObservatoryClockControlPanel extends InstrumentUIComponentDecorator
                                                implements ControlPanelInterface,
                                                           ObservatoryClockChangedListener
    {
    // String Resources
    private static final String ICON_CONTROL    = "clock-control.png";
    private static final String DEFAULT_TIME    = "00:00:00 ";

    private static final int INDEX_LEFT = 0;
    private static final int INDEX_RIGHT = 1;

    // Injections
    private final FrameworkPlugin pluginFramework;

    private TimeSystem timeSystemLeft;
    private TimeSystem timeSystemRight;


    /***********************************************************************************************
     * Construct the ObservatoryClockControlPanel.
     *
     * @param instrument
     * @param instrumentxml
     * @param hostui
     * @param task
     * @param font
     * @param colour
     * @param resourcekey
     * @param framework
     */

    public ObservatoryClockControlPanel(final ObservatoryInstrumentInterface instrument,
                                        final Instrument instrumentxml,
                                        final ObservatoryUIInterface hostui,
                                        final TaskPlugin task,
                                        final FontInterface font,
                                        final ColourInterface colour,
                                        final String resourcekey,
                                        final FrameworkPlugin framework)
        {
        super(instrument,
              instrumentxml,
              hostui,
              task,
              font,
              colour,
              resourcekey,
              INDICATOR_COUNT_2);

        if ((framework == null)
            || (!framework.validatePlugin()))
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_INVALID);
            }

        // Injections
        this.pluginFramework = framework;

        this.timeSystemLeft = TimeSystem.UT;
        this.timeSystemRight = TimeSystem.UT;
        }


    /***********************************************************************************************
     * Initialise this UIComponent.
     */

    public final void initialiseUI()
        {
        final String SOURCE = "ObservatoryClockControlPanel.initialiseUI() ";

        resetControlPanelIndicators();

        if ((getHostInstrument() != null)
            && (getHostInstrument().getInstrument() != null)
            && (getHostInstrument().getInstrument().getIndicatorMetadataKeyList() != null)
            && (getHostInstrument().getInstrument().getIndicatorMetadataKeyList().size() > 0))
            {
            final Iterator<String> iterKeys;
            final List<TimeSystem> listTimeSystem;

            iterKeys = getHostInstrument().getInstrument().getIndicatorMetadataKeyList().iterator();
            listTimeSystem = new ArrayList<TimeSystem>(getHostInstrument().getInstrument().getIndicatorMetadataKeyList().size());

            // Find out how many keys we have been given, and if they are of correct syntax
            // We must go round this at least once, but may not find a valid key
            while (iterKeys.hasNext())
                {
                final String key;

                key = iterKeys.next();

                if ((key != null)
                    && (key.startsWith(MetadataDictionary.KEY_CLOCK_DISPLAY_TIME_SYSTEM.getKey())))
                    {
                    final String strTimeSystemMnemonic;
                    final TimeSystem timeSystem;

                    strTimeSystemMnemonic = key.substring(MetadataDictionary.KEY_CLOCK_DISPLAY_TIME_SYSTEM.getKey().length());

                    // Map the metadata key fragment to a TimeSystem enum if possible
                    timeSystem = TimeSystem.getTimeSystemForMnemonic(strTimeSystemMnemonic);

                    if (timeSystem != null)
                        {
                        // Accumulate TimeSystems in the order in which they are defined
                        listTimeSystem.add(timeSystem);
                        }
                    else
                        {
                        LOGGER.error(SOURCE + "Unable to recognise TimeSystem [system=" + strTimeSystemMnemonic + "]");
                        }
                    }
                }

            //--------------------------------------------------------------------------------------
            // Did we find a left hand clock?

            if (listTimeSystem.size() >= 1)
                {
                // The first entry is always taken to be the left hand clock
                setTimeSystemLeft(listTimeSystem.get(INDEX_LEFT));
                setIndicator0(new Indicator(DIM_CONTROL_PANEL_INDICATOR_DOUBLE,
                                            DEFAULT_TIME + getTimeSystemLeft().getMnemonic(),
                                            EMPTY_STRING,
                                            getTimeSystemLeft().getName(),
                                            INDICATOR_BORDER));
                getIndicator0().setAlignmentY(Component.CENTER_ALIGNMENT);
                }

            //--------------------------------------------------------------------------------------
            // Did we find a right hand clock?

            if (listTimeSystem.size() >= 2)
                {
                // The second entry is always taken to be the right hand clock
                setTimeSystemRight(listTimeSystem.get(INDEX_RIGHT));
                setIndicator1(new Indicator(DIM_CONTROL_PANEL_INDICATOR_DOUBLE,
                                            DEFAULT_TIME + getTimeSystemRight().getMnemonic(),
                                            EMPTY_STRING,
                                            getTimeSystemRight().getName(),
                                            INDICATOR_BORDER));
                getIndicator1().setAlignmentY(Component.CENTER_ALIGNMENT);
                }

            //--------------------------------------------------------------------------------------
            // If we had no valid IndicatorMetadata Keys, then default to a UT clock

            if (listTimeSystem.size() == 0)
                {
                // Just set up a single UT clock
                setTimeSystemLeft(TimeSystem.UT);
                setIndicator0(new Indicator(DIM_CONTROL_PANEL_INDICATOR_DOUBLE,
                                            DEFAULT_TIME + getTimeSystemLeft().getMnemonic(),
                                            EMPTY_STRING,
                                            getTimeSystemLeft().getName(),
                                            INDICATOR_BORDER));
                getIndicator0().setAlignmentY(Component.CENTER_ALIGNMENT);

                setTimeSystemRight(null);
                setIndicator1(null);
                }
            }
        else
            {
            // There are no IndicatorMetadata Keys at all
            // Just set up a single UT clock
            setTimeSystemLeft(TimeSystem.UT);
            setIndicator0(new Indicator(DIM_CONTROL_PANEL_INDICATOR_DOUBLE,
                                        DEFAULT_TIME + getTimeSystemLeft().getMnemonic(),
                                        EMPTY_STRING,
                                        getTimeSystemLeft().getName(),
                                        INDICATOR_BORDER));
            getIndicator0().setAlignmentY(Component.CENTER_ALIGNMENT);

            setTimeSystemRight(null);
            setIndicator1(null);
            }

        // Do not specify Metadata because it will never be displayed,
        // but accept a warning message on startup from InstrumentUIHelper.updateControlPanelIndicators()

        setIndicator0(getIndicator0());
        setIndicator1(getIndicator1());

        getControlPanelIndicators().add(getIndicator0());
        getControlPanelIndicators().add(getIndicator1());

        InstrumentUIHelper.assembleControlPanel(this, this, ICON_CONTROL);
        }


    /***********************************************************************************************
     * Indicate that the state of the Instrument has changed.
     *
     * @param event
     */

    public void instrumentChanged(final InstrumentStateChangedEvent event)
        {
        LOGGER.debugIndicators("ObservatoryClockControlPanel.instrumentChanged() --> update indicators");

        super.instrumentChanged(event);

        // Update the Indicators using the real Metadata if possible, defaults if not
        InstrumentUIHelper.updateControlPanelIndicators(event, this);
        }


    /*******************************************************************************************
     * Indicate that the ObservatoryClock has changed.
     *
     * @param event
     */

    public void clockChanged(final ObservatoryClockChangedEvent event)
        {
        // The Clock is almost always visible, because most Groups have a Clock,
        // so try to update every time regardless of visibility
        if ((event != null)
            && (event.hasChanged()))
            {
            if ((getObservatoryClock() != null)
                && (getObservatoryClock().getAstronomicalCalendar() != null))
                {
                if (getIndicator0() != null)
                    {
                    getIndicator0().setValue(getObservatoryClock().getAstronomicalCalendar().toString_HH_MM_SS(getTimeSystemLeft()) + SPACE + getTimeSystemLeft().getMnemonic());
                    }

                if (getIndicator1() != null)
                    {
                    getIndicator1().setValue(getObservatoryClock().getAstronomicalCalendar().toString_HH_MM_SS(getTimeSystemRight()) + SPACE + getTimeSystemRight().getMnemonic());
                    }
                }
            else
                {
                if (getIndicator0() != null)
                    {
                    getIndicator0().setValue(NO_CLOCK);
                    }

                if (getIndicator1() != null)
                    {
                    getIndicator1().setValue(NO_CLOCK);
                    }
                }
            }
        }


    /***********************************************************************************************
     * Get the host Framework.
     *
     * @return FrameworkPlugin
     */

    private FrameworkPlugin getFramework()
        {
        return (this.pluginFramework);
        }


    /***********************************************************************************************
     * Get the Left TimeSystem.
     *
     * @return TimeSystem
     */

    private TimeSystem getTimeSystemLeft()
        {
        return (this.timeSystemLeft);
        }


    /***********************************************************************************************
     * Set the Left TimeSystem.
     *
     * @param timesystem
     */

    private void setTimeSystemLeft(final TimeSystem timesystem)
        {
        this.timeSystemLeft = timesystem;
        }


    /***********************************************************************************************
     * Get the Right TimeSystem.
     *
     * @return TimeSystem
     */

    private TimeSystem getTimeSystemRight()
        {
        return (this.timeSystemRight);
        }


    /***********************************************************************************************
     * Set the Right TimeSystem.
     *
     * @param timesystem
     */

    private void setTimeSystemRight(final TimeSystem timesystem)
        {
        this.timeSystemRight = timesystem;
        }
    }
