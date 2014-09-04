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

import org.jfree.data.time.TimeSeries;
import org.lmn.fc.common.utilities.time.AstronomicalCalendarInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.events.ObservatoryClockChangedListener;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.*;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.logs.SimpleEventLogUIComponent;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.plugins.AtomPlugin;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;

import java.awt.*;
import java.util.*;


/***********************************************************************************************
 * An ObservatoryClock.
 */

public final class ObservatoryClock extends AbstractObservatoryInstrument
                                    implements ObservatoryInstrumentInterface,
                                               ObservatoryClockInterface
    {
    private static final Dimension DIMENSION_CLOCK = new Dimension(140, 80);

    private AstronomicalCalendarInterface calendarAstro;


    /***********************************************************************************************
     * Construct an ObservatoryClock.
     *
     * @param instrument
     * @param plugin
     * @param hostui
     * @param resourcekey
     */

    public ObservatoryClock(final Instrument instrument,
                            final AtomPlugin plugin,
                            final ObservatoryUIInterface hostui,
                            final String resourcekey)
        {
        super(instrument, plugin, hostui, resourcekey);

        this.calendarAstro = null;
        }


    /***********************************************************************************************
     * Initialise the ObservatoryClock Instrument.
     */

    public void initialise()
        {
        final InstrumentUIComponentDecoratorInterface controlPanel;
        final InstrumentUIComponentDecoratorInterface instrumentPanel;

        // Read the Resources for the ObservatoryClock
        readResources();

        super.initialise();

        // Create and initialise the ObservatoryClockControlPanel
        controlPanel = new ObservatoryClockControlPanel(this,
                                                        getInstrument(),
                                                        getHostUI(),
                                                        (TaskPlugin)getHostAtom().getRootTask(),
                                                        getFontData(),
                                                        getColourData(),
                                                        getResourceKey(),
                                                        REGISTRY.getFramework());
        setControlPanel(controlPanel, getInstrument().getName());
        addInstrumentStateChangedListener(controlPanel);
        getControlPanel().initialiseUI();

        instrumentPanel = new ObservatoryClockInstrumentPanel(this,
                                                              getInstrument(),
                                                              getHostUI(),
                                                              (TaskPlugin)getHostAtom().getRootTask(),
                                                              getFontData(),
                                                              getColourData(),
                                                              getResourceKey(),
                                                              REGISTRY.getFramework(),
                                                              DIMENSION_CLOCK);
        setInstrumentPanel(instrumentPanel);
        addInstrumentStateChangedListener(instrumentPanel);
        getInstrumentPanel().initialiseUI();
        }


    /***********************************************************************************************
     * Start this ObservatoryInstrument.
     * Add the ObservatoryClockChangedListener.
     *
     * @return boolean
     */

    public synchronized boolean start()
        {
        final boolean boolSuccess;

        boolSuccess = super.start();

        // Allow the ControlPanel to receive updates from the ObservatoryClock
        // If there is no DAO, then there is no Clock
        if ((boolSuccess)
            && (getDAO() != null)
            && (getDAO() instanceof ObservatoryClockDAOInterface))
            {
            // Allow the ObservatoryClock ControlPanel to receive updates from the ObservatoryClock
            ((ObservatoryClockDAOInterface)getDAO()).addObservatoryClockChangedListener((ObservatoryClockChangedListener) getControlPanel());

            this.calendarAstro = ObservatoryClockHelper.createCalendar(this);
            }

        return (boolSuccess);
        }


    /***********************************************************************************************
     * Stop this ObservatoryInstrument.
     * Remove the ObservatoryClockChangedListener.
     *
     * @return boolean
     */

    public synchronized boolean stop()
        {
        final boolean boolSuccess;

        this.calendarAstro = null;

        if ((getDAO() != null)
            && (getDAO() instanceof ObservatoryClockDAOInterface))
            {
            // If the Clock is not running, no-one can listen to it!
            ((ObservatoryClockDAOInterface)getDAO()).getObservatoryClockChangedListeners().clear();
            }

        boolSuccess = super.stop();

        return (boolSuccess);
        }


    /**********************************************************************************************/
    /* ObservatoryClockInterface                                                                  */
    /* See also: SimplePlatformClock, NTPSynchronisedProxyClock                                   */
    /***********************************************************************************************
     * Start the ObservatoryClock.
     *
     * @return boolean
     */

    public boolean startClock()
        {
        // This is handled by the Instrument's DAO

        return (true);
        }



    /***********************************************************************************************
     * Stop the ObservatoryClock.
     */

    public void stopClock()
        {
        // This is handled by the Instrument's DAO
        }


    /***********************************************************************************************
     * Synchronise the Clock using the specified time offset in milliseconds.
     * Long.MAX_VALUE specified if synchronisation failed.
     *
     * @param offsetmillis
     * @param timeseries
     * @param timezone
     * @param locale
     * @param verboselogging
     * @param log
     *
     * @return boolean
     */

    public boolean synchronise(final long offsetmillis,
                               final TimeSeries timeseries,
                               final TimeZone timezone,
                               final Locale locale,
                               final boolean verboselogging,
                               final Vector<Vector> log)
        {
        final String SOURCE = "ObservatoryClock.synchronise() ";

        // This is handled by the Instrument's DAO
        LOGGER.error(SOURCE + "Invalid command for the ObservatoryClock Instrument. This should never occur!");

        SimpleEventLogUIComponent.logEvent(log,
                                           EventStatus.INFO,
                                           METADATA_TARGET_CLOCK
                                               + METADATA_ACTION_SYNCHRONISE
                                               + METADATA_RESULT + "Invalid command for the ObservatoryClock Instrument" + TERMINATOR,
                                           SOURCE,
                                           this);

        return (true);
        }


    /***********************************************************************************************
     * Get the System Time in milliseconds.
     *
     * @return long
     */

    public long getSystemTimeMillis()
        {
        final ObservatoryClockInterface clock;
        long longSystemTime;

        clock = getInstrumentClock();
        longSystemTime = 0;
//        LOGGER.debugTimedEvent("ObservatoryClock obtaining SystemTime");

        if (clock != null)
            {
            longSystemTime = clock.getSystemTimeMillis();
            }

        return (longSystemTime);
        }


    /**********************************************************************************************
     * Get the System Date, for SQL.
     *
     * @return Date
     */

    public java.sql.Date getSystemDateNow()
        {
        final ObservatoryClockInterface clock;
        java.sql.Date dateNow;

        clock = getInstrumentClock();
        dateNow = null;
//        LOGGER.debugTimedEvent("ObservatoryClock obtaining SystemDateNow");

        if (clock != null)
            {
            dateNow = clock.getSystemDateNow();
            }

        return (dateNow);
        }


    /***********************************************************************************************
     * Get the Calendar Date NOW.
     *
     * @return GregorianCalendar
     */

    public GregorianCalendar getCalendarDateNow()
        {
        final ObservatoryClockInterface clock;
        GregorianCalendar calDateNow;

        clock = getInstrumentClock();
        calDateNow = null;
//        LOGGER.debugTimedEvent("ObservatoryClock obtaining CalendarDateNow");

        if (clock != null)
            {
            calDateNow = clock.getCalendarDateNow();
            }

        return (calDateNow);
        }


    /***********************************************************************************************
     * Get the Calendar Time NOW.
     *
     * @return GregorianCalendar
     */

    public GregorianCalendar getCalendarTimeNow()
        {
        final ObservatoryClockInterface clock;
        GregorianCalendar calTimeNow;

        clock = getInstrumentClock();
        calTimeNow = null;
//        LOGGER.debugTimedEvent("ObservatoryClock obtaining CalendarTimeNow");

        if (clock != null)
            {
            calTimeNow = clock.getCalendarTimeNow();
            }

        return (calTimeNow);
        }


    /***********************************************************************************************
     * Find the time Now in the format "yyyy-MM-dd HH:mm:ss.SSS".
     *
     * @return String
     */

    public String getDateTimeNowAsString()
        {
        final ObservatoryClockInterface clock;
        String strTimeNow;

        clock = getInstrumentClock();
        strTimeNow = null;
//        LOGGER.debugTimedEvent("ObservatoryClock obtaining DateTimeNowAsString");

        if (clock != null)
            {
            strTimeNow = clock.getDateTimeNowAsString();
            }

        return (strTimeNow);
        }


    /***********************************************************************************************
     * Get the System time as a Calendar.
     *
     * @param timezone
     * @param locale
     *
     * @return Calendar
     */

    public Calendar getSystemCalendar(final TimeZone timezone,
                                      final Locale locale)
        {
        final ObservatoryClockInterface clock;
        Calendar calSystem;

        clock = getInstrumentClock();
        calSystem = null;
//        LOGGER.debugTimedEvent("ObservatoryClock obtaining SystemCalendar");

        if (clock != null)
            {
            calSystem = clock.getSystemCalendar(timezone, locale);
            }

        return (calSystem);
        }


    /***********************************************************************************************
     * Get the AstronomicalCalendar.
     *
     * @return AstronomicalCalendarInterface
     */

    public synchronized AstronomicalCalendarInterface getAstronomicalCalendar()
        {
        // This will recalculate all astronomical TimeSystems
        if (this.calendarAstro != null)
            {
            this.calendarAstro.setTimeInMillis(getSystemTimeMillis());
            }

        return (this.calendarAstro);
        }


    /***********************************************************************************************
     * Get the DAO which is synthesising this Clock, but as an ObservatoryClockDAOInterface.
     *
     * @return ObservatoryClockDAOInterface
     */

    public ObservatoryClockDAOInterface getClockDAO()
        {
        if ((getDAO() != null)
            && (getDAO() instanceof ObservatoryClockDAOInterface))
            {
            return ((ObservatoryClockDAOInterface)getDAO());
            }
        else
            {
            return (null);
            }
        }


    /***********************************************************************************************
     * Get either the Clock which is being synthesised by the DAO,
     * or the PlatformClock if there is no DAO.
     *
     * @return ObservatoryClockInterface
     */

    private ObservatoryClockInterface getInstrumentClock()
        {
        final ObservatoryClockInterface clock;

        if (getDAO() != null)
            {
            // Get the Clock which is being synthesised by the DAO
//            LOGGER.debugTimedEvent("ObservatoryClock.getInstrumentClock() Obtaining ObservatoryClock from DAO");
            clock = getDAO().getObservatoryClock();
            }
        else
            {
            // If this Instrument does not have a valid DAO,
            // then use the PlatformClock as the source of Time,
            // since this must exist
//            LOGGER.debugTimedEvent("ObservatoryClock.getInstrumentClock() Obtaining PlatformClock");
            clock = getHostUI().getPlatformClock();
            }

        return (clock);
        }
    }
