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

package org.lmn.fc.frameworks.starbase.portcontroller;

import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.frameworks.starbase.portcontroller.events.CommandLifecycleEventInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.events.CommandLifecycleListener;
import org.lmn.fc.frameworks.starbase.portcontroller.events.PortControllerHeartbeatListener;
import org.lmn.fc.frameworks.starbase.portcontroller.events.PortMessageListener;
import org.lmn.fc.model.xmlbeans.instruments.InstrumentsDocument;


/***************************************************************************************************
 * PortControllerInterface.
 */

public interface PortControllerInterface extends Runnable,
                                                 FrameworkConstants,
                                                 FrameworkStrings,
                                                 FrameworkMetadata,
                                                 FrameworkSingletons
    {
    int LATENCY_MILLISEC = 100;


    /***********************************************************************************************
     * Start the Port Controller.
     * Specify the Instruments document in order to give the context of all Messages.
     *
     * @param instrumentsdoc
     */

    boolean start(InstrumentsDocument instrumentsdoc);


    /***********************************************************************************************
     * Stop the Port Controller.
     */

    boolean stop();


    /***********************************************************************************************
     * Request that a DaoPort be attached to the PortController as soon as possible.
     *
     * @param port
     */

    void requestPortAddition(DaoPortInterface port);


    /***********************************************************************************************
     * Request that a DaoPort be removed from the PortController as soon as possible.
     *
     * @param port
     */

    void requestPortRemoval(DaoPortInterface port);


    /***********************************************************************************************
     * Get a flag indicating if the PortController is running.
     *
     * @return boolean
     */

    boolean isRunning();


    /***********************************************************************************************
     * Set a flag indicating if the PortController should run.
     *
     * @param running
     */

    void setRunning(boolean running);


    /***********************************************************************************************
     * Get the StaribusPort, or null if not assigned.
     *
     * @return DaoPortInterface
     */

    DaoPortInterface getStaribusPort();


    /***********************************************************************************************
     * Set the StaribusPort if not already assigned, or null.
     * Return false if the StaribusPort is already set.
     *
     * @param staribusPort
     */

    boolean setStaribusPort(DaoPortInterface staribusPort);


    /***********************************************************************************************
     * Get the Port Controller latency between Transmit and Receive.
     *
     * @return long
     */

    long getLatencyMillis();


    /***********************************************************************************************
     * Set the Port Controller latency between Transmit and Receive.
     *
     * @param latency
     */

    void setLatencyMillis(long latency);


    /***********************************************************************************************
     * Notify all listeners of PortMessage Events.
     *
     * @param source
     * @param commandmessage
     * @param responsemessage
     */

    void notifyPortMessageEvent(Object source,
                                CommandMessageInterface commandmessage,
                                ResponseMessageInterface responsemessage);


    /***********************************************************************************************
     * Add a PortMessage Listener.
     *
     * @param listener
     */

    void addPortMessageListener(PortMessageListener listener);


    /***********************************************************************************************
     * Remove a PortMessage Listener.
     *
     * @param listener
     */

    void removePortMessageListener(PortMessageListener listener);


    /***********************************************************************************************
     * Notify all listeners of a CommandLifecycleEvent.
     *
     * @param source
     * @param commandmessage
     * @param responsemessage
     *
     * @return CommandLifecycleEventInterface
     */

    CommandLifecycleEventInterface notifyCommandLifecycleEvent(Object source,
                                                              CommandMessageInterface commandmessage,
                                                              ResponseMessageInterface responsemessage);


    /***********************************************************************************************
     * Notify all listeners of a CommandLifecycleEvent, when an event is already prepared.
     *
     * @param source
     *
     * @return CommandLifecycleEventInterface
     */

    CommandLifecycleEventInterface notifyCommandLifecycleEvent(Object source,
                                                               CommandLifecycleEventInterface event);


    /***********************************************************************************************
     * Add a CommandLifecycle Listener.
     *
     * @param listener
     */

    void addCommandLifecycleListener(CommandLifecycleListener listener);


    /***********************************************************************************************
     * Remove a CommandLifecycle Listener.
     *
     * @param listener
     */

    void removeCommandLifecycleListener(CommandLifecycleListener listener);


    /***********************************************************************************************
     * Notify all listeners of PortControllerHeartbeat Events.
     *
     * @param source
     */

    void notifyPortControllerHeartbeatEvent(Object source);


    /***********************************************************************************************
     * Add a PortControllerHeartbeat Listener.
     *
     * @param listener
     */

    void addPortControllerHeartbeatListener(PortControllerHeartbeatListener listener);


    /***********************************************************************************************
     * Remove a PortControllerHeartbeat Listener.
     *
     * @param listener
     */

    void removePortControllerHeartbeatListener(PortControllerHeartbeatListener listener);
    }
