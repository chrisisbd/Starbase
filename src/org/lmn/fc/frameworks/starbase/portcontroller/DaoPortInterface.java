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

import org.lmn.fc.common.constants.*;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryClockInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.impl.PortController;

import java.util.List;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;


public interface DaoPortInterface extends FrameworkConstants,
                                          FrameworkStrings,
                                          FrameworkMetadata,
                                          FrameworkSingletons,
                                          ResourceKeys
    {
    // This cannot be in FrameworkSingletons because it won't be loaded at the right time...
    PortControllerInterface PORT_CONTROLLER = PortController.getInstance();

    int QUEUE_CAPACITY = 10;


    /***********************************************************************************************
     * Open the Port.
     *
     * @return boolean
     */

    boolean open();


    /***********************************************************************************************
     * Close the Port.
     *
     * @return boolean
     */

    boolean close();


    /***********************************************************************************************
     * Indicate if this Port is Open.
     *
     * @return boolean
     */

    boolean isPortOpen();


    /***********************************************************************************************
     * Queue a Command message for sending to the Port.
     * Wait for the specified time for space to appear in the queue, then give up.
     *
     * @param message
     * @param timeoutmillis
     * @param clock
     * @param notifymonitors
     * @param debug
     */

    void queueCommandMessage(CommandMessageInterface message,
                             long timeoutmillis,
                             ObservatoryClockInterface clock,
                             boolean notifymonitors,
                             boolean debug);


    /***********************************************************************************************
     * Get a message from the Port receive queue from the specified source.
     * Wait for the Timeout for an item to appear in the queue, then give up, returning null.
     *
     * @param dao
     * @param timeoutmillis
     * @param notifymonitors
     * @param debug
     *
     * @return ResponseMessageInterface
     */

    ResponseMessageInterface dequeueResponseMessage(ObservatoryInstrumentDAOInterface dao,
                                                    long timeoutmillis,
                                                    boolean notifymonitors,
                                                    boolean debug);


    /***********************************************************************************************
     * Clear both Tx and Rx Queues of all messages.
     */

    void clearQueues();


    /***********************************************************************************************
     * Get the PortController controlling this Port.
     *
     * @return PortControllerInterface
     */

    PortControllerInterface getPortController();


    /***********************************************************************************************
     * Get the name of the DaoPort.
     *
     * @return String
     */

    String getName();


    /***********************************************************************************************
     * Get the Description of the DaoPort.
     *
     * @return String
     */

    String getDescription();


    /***********************************************************************************************
     * Get the ResourceKey for the Port.
     *
     * @return String
     */

    String getResourceKey();


    /***********************************************************************************************
     * Get the List of host DAOs for this Port.
     *
     * @return List<ObservatoryInstrumentDAOInterface>
     */

    List<ObservatoryInstrumentDAOInterface> getHostDAOs();


    /***********************************************************************************************
     * Add a host DAO to this DaoPort.
     *
     * @param dao
     */

    void addHostDAO(final ObservatoryInstrumentDAOInterface dao);


    /***********************************************************************************************
     * Remove a host DAO from this DaoPort.
     *
     * @param dao
     */

    void removeHostDAO(ObservatoryInstrumentDAOInterface dao);


    /***********************************************************************************************
     * Get the TxStream.
     *
     * @return PortTxStreamInterface
     */

    PortTxStreamInterface getTxStream();


    /***********************************************************************************************
     * Set the TxStream.
     *
     * @param stream
     */

    void setTxStream(PortTxStreamInterface stream);


    /***********************************************************************************************
     * Get the RxStream.
     *
     * @return PortRxStreamInterface
     */

    PortRxStreamInterface getRxStream();


    /***********************************************************************************************
     * Set the RxStream.
     *
     * @param stream
     */

    void setRxStream(PortRxStreamInterface stream);


    /***********************************************************************************************
     * Get the Tx Queue.
     *
     * @return BlockingQueue<CommandMessageInterface>
     */

    BlockingQueue<CommandMessageInterface> getTxQueue();


    /***********************************************************************************************
     * Get the Rx Queue.
     *
     * @return BlockingQueue<ResponseMessageInterface>
     */

    BlockingQueue<ResponseMessageInterface> getRxQueue();


    /***********************************************************************************************
     * Get the Vector of extra data to append to a Report.
     *
     * @return Vector<Vector>
     */

    Vector<Vector> getPortConfiguration();


    /***********************************************************************************************
     * Indicate if this Port is the StaribusPort for the Observatory.
     *
     * @return boolean
     */

    boolean isStaribusPort();


    /***********************************************************************************************
     * Make this Port into the StaribusPort for the Observatory.
     */

    void makeStaribusPort();


    /***********************************************************************************************
     * Indicate if this Port is Busy.
     *
     * @return boolean
     */

    boolean isPortBusy();


    /***********************************************************************************************
     * Control the Busy state of this Port.
     */

    void setPortBusy(boolean busy);


    /***********************************************************************************************
     * Get the ObservatoryClock.
     *
     * @return ObservatoryClockInterface
     */

    ObservatoryClockInterface getObservatoryClock();
    }
