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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.clock.dao;

import org.apache.xmlbeans.XmlObject;
import org.lmn.fc.common.utilities.time.ChronosHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.events.ObservatoryClockChangedEvent;
import org.lmn.fc.frameworks.starbase.plugins.observatory.events.ObservatoryClockChangedListener;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.*;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.commands.clock.CalculateTopocentricEphemeris;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.ResponseMessageHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.clock.ObservatoryClockDAOInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageInterface;
import org.lmn.fc.model.xmlbeans.instruments.CommandType;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;

import java.util.List;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;


/***********************************************************************************************
 * AbstractClockDAO.
 */

public abstract class AbstractClockDAO extends AbstractObservatoryInstrumentDAO
                                       implements ObservatoryInstrumentDAOInterface,
                                                  ObservatoryClockDAOInterface
    {
    protected ObservatoryClockInterface clock;
    private final Vector<ObservatoryClockChangedListener> vecObservatoryClockChangedListeners;


    /***********************************************************************************************
     * Build the CommandPool using method names in this DAO.
     *
     * @param pool
     */

    private static void addSubclassToCommandPool(final CommandPoolList pool)
        {
        pool.add("getDate");
        pool.add("getTime");
        pool.add("getTimeZone");
        pool.add("synchroniseNow");
        pool.add("calculateToposEphemeris");
        }


    /***********************************************************************************************
     * Construct an AbstractClockDAO.
     *
     * @param hostinstrument
     */

    public AbstractClockDAO(final ObservatoryInstrumentInterface hostinstrument)
        {
        super(hostinstrument);

        this.clock = null;
        this.vecObservatoryClockChangedListeners = new Vector<ObservatoryClockChangedListener>(10);

        addSubclassToCommandPool(getCommandPool());
        }


    /***********************************************************************************************
     * Initialise the DAO and start the Clock if possible.
     *
     * @param resourcekey
     *
     * @return boolean
     */

    public boolean initialiseDAO(final String resourcekey)
        {
        boolean boolSuccess;

        boolSuccess = false;

        if ((super.initialiseDAO(resourcekey))
            && (clock != null))
            {
            boolSuccess = clock.startClock();
            }

        return (boolSuccess);
        }


    /***********************************************************************************************
     * Shut down the DAO and dispose of all Resources.
     */

    public void disposeDAO()
        {
        if (clock != null)
            {
            clock.stopClock();
            }

        if (getObservatoryClockChangedListeners() != null)
            {
            getObservatoryClockChangedListeners().clear();
            }

        super.disposeDAO();
        }


    /***********************************************************************************************
     * Construct a CommandMessage appropriate to this DAO.
     *
     * @param dao
     * @param instrumentxml
     * @param module
     * @param command
     * @param starscript
     *
     * @return CommandMessageInterface
     */

    public CommandMessageInterface constructCommandMessage(final ObservatoryInstrumentDAOInterface dao,
                                                           final Instrument instrumentxml,
                                                           final XmlObject module,
                                                           final CommandType command,
                                                           final String starscript)
        {
        return new ClockCommandMessage(dao,
                                       instrumentxml,
                                       module,
                                       command,
                                       starscript);
        }


    /***********************************************************************************************
     * Construct a ResponseMessage appropriate to this DAO.
     *
     *
     * @param portname
     * @param instrumentxml
     * @param module
     * @param command
     * @param starscript
     * @param responsestatusbits
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface constructResponseMessage(final String portname,
                                                             final Instrument instrumentxml,
                                                             final XmlObject module,
                                                             final CommandType command,
                                                             final String starscript,
                                                             final int responsestatusbits)
        {
        return new ClockResponseMessage(portname,
                                        instrumentxml,
                                        module,
                                        command,
                                        starscript,
                                        responsestatusbits);
        }



    /**********************************************************************************************/
    /* DAO Local Commands                                                                         */
    /***********************************************************************************************
     * Get the Date.
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface getDate(final CommandMessageInterface commandmessage)
        {
        final CommandType commandType;
        final ResponseMessageInterface responseMessage;

        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               "AbstractClockDAO.getDate()");

        // Don't affect the CommandType of the incoming Command
        commandType = (CommandType)commandmessage.getCommandType().copy();

        // Get the latest Resources
        readResources();

        // Create the ResponseMessage
        commandType.getResponse().setValue(ChronosHelper.toDateString(getObservatoryClock().getCalendarDateNow()));

        responseMessage = ResponseMessageHelper.constructSuccessfulResponse(this,
                                                                            commandmessage,
                                                                            commandType);
        return (responseMessage);
        }


    /***********************************************************************************************
     * Get the Time.
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface getTime(final CommandMessageInterface commandmessage)
        {
        final CommandType commandType;
        final ResponseMessageInterface responseMessage;

        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               "AbstractClockDAO.getTime()");

        // Don't affect the CommandType of the incoming Command
        commandType = (CommandType)commandmessage.getCommandType().copy();

        // Get the latest Resources
        readResources();

        // Create the ResponseMessage
        commandType.getResponse().setValue(ChronosHelper.toTimeString(getObservatoryClock().getCalendarTimeNow()));

        responseMessage = ResponseMessageHelper.constructSuccessfulResponse(this,
                                                                            commandmessage,
                                                                            commandType);
        return (responseMessage);
        }


    /***********************************************************************************************
     * Get the TimeZone.
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface getTimeZone(final CommandMessageInterface commandmessage)
        {
        final CommandType commandType;
        final ResponseMessageInterface responseMessage;

        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               "AbstractClockDAO.getTimeZone()");

        // Don't affect the CommandType of the incoming Command
        commandType = (CommandType)commandmessage.getCommandType().copy();

        // Get the latest Resources
        readResources();

        // Create the ResponseMessage
        commandType.getResponse().setValue(REGISTRY.getFrameworkTimeZone().getDisplayName());

        responseMessage = ResponseMessageHelper.constructSuccessfulResponse(this,
                                                                            commandmessage,
                                                                            commandType);
        return (responseMessage);
        }


    /***********************************************************************************************
     * synchroniseNow().
     * This may be overridden by DAOs which can really synchroniseNow the Clock.
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface synchroniseNow(final CommandMessageInterface commandmessage)
        {
        final CommandType commandType;
        final ResponseMessageInterface responseMessage;

        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               "AbstractClockDAO.synchroniseNow() LOCAL COMMAND");

        // Don't affect the CommandType of the incoming Command
        commandType = (CommandType)commandmessage.getCommandType().copy();

        // The synchroniseNow() operation normally just requires an Ack, i.e. no Response

        // Create the ResponseMessage
        responseMessage = ResponseMessageHelper.constructSuccessfulResponse(this,
                                                                            commandmessage,
                                                                            commandType);
        return (responseMessage);
        }


    /***********************************************************************************************
     * calculateToposEphemeris().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface calculateToposEphemeris(final CommandMessageInterface commandmessage)
        {
        return (CalculateTopocentricEphemeris.doCalculateTopocentricEphemeris(this, commandmessage));
        }


    /**********************************************************************************************/
    /* Events                                                                                     */
    /***********************************************************************************************
     * Notify all listeners of ObservatoryClockChangedEvents.
     *
     * @param eventsource
     * @param changed
     */

    public final void notifyObservatoryClockChangedEvent(final Object eventsource,
                                                         final boolean changed)
        {
        List<ObservatoryClockChangedListener> listeners;
        final ObservatoryClockChangedEvent changeEvent;

        // Create a Thread-safe List of Listeners
        listeners = new CopyOnWriteArrayList<ObservatoryClockChangedListener>(getObservatoryClockChangedListeners());

        // Create an ObservatoryClockChangedEvent
        changeEvent = new ObservatoryClockChangedEvent(eventsource, changed);

        // Fire the event to every listener
        synchronized(listeners)
            {
            for (int i = 0; i < listeners.size(); i++)
                {
                final ObservatoryClockChangedListener changeListener;

                changeListener = listeners.get(i);
                changeListener.clockChanged(changeEvent);
                }
            }

        // Help the GC?
        listeners = null;
        }


    /***********************************************************************************************
     * Get the ObservatoryClockChanged Listeners (mostly for testing).
     *
     * @return Vector<ObservatoryClockChangedListener>
     */

    public final Vector<ObservatoryClockChangedListener> getObservatoryClockChangedListeners()
        {
        return (this.vecObservatoryClockChangedListeners);
        }


    /***********************************************************************************************
     * Add a listener for this event.
     *
     * @param listener
     */

    public final void addObservatoryClockChangedListener(final ObservatoryClockChangedListener listener)
        {
        if ((listener != null)
            && (getObservatoryClockChangedListeners() != null))
            {
            getObservatoryClockChangedListeners().addElement(listener);
            }
        }


    /***********************************************************************************************
     * Remove a listener for this event.
     *
     * @param listener
     */

    public final void removeObservatoryClockChangedListener(final ObservatoryClockChangedListener listener)
        {
        if ((listener != null)
            && (getObservatoryClockChangedListeners() != null))
            {
            getObservatoryClockChangedListeners().removeElement(listener);
            }
        }
    }
