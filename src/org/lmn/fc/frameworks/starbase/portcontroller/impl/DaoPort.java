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

package org.lmn.fc.frameworks.starbase.portcontroller.impl;

import org.lmn.fc.common.exceptions.FrameworkException;
import org.lmn.fc.common.utilities.misc.Utilities;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryClockInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.ConfigurationHelper;
import org.lmn.fc.frameworks.starbase.portcontroller.*;
import org.lmn.fc.model.resources.PropertyPlugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Vector;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;


/***************************************************************************************************
 * DaoPort.
 */

public final class DaoPort implements DaoPortInterface
    {
    // Injections
    private final PortControllerInterface portController;
    private final String strName;
    private final String strDescription;
    private final String strResourceKey;
    private PortTxStreamInterface txStream;
    private PortRxStreamInterface rxStream;
    private final ObservatoryClockInterface clock;

    private final List<ObservatoryInstrumentDAOInterface> listHostDAOs;
    private final BlockingQueue<CommandMessageInterface> queueTx;
    private final BlockingQueue<ResponseMessageInterface> queueRx;
    private final Vector<Vector> vecPortConfiguration;
    private boolean boolStaribusPort;
    private boolean boolPortOpen;
    private boolean boolPortBusy;


    /***********************************************************************************************
     * Construct a DaoPort using the specified streams.
     *
     * @param portcontroller
     * @param portname
     * @param description
     * @param resourcekey
     * @param txstream
     * @param rxstream
     * @param obsclock
     */

    public DaoPort(final PortControllerInterface portcontroller,
                   final String portname,
                   final String description,
                   final String resourcekey,
                   final PortTxStreamInterface txstream,
                   final PortRxStreamInterface rxstream,
                   final ObservatoryClockInterface obsclock)
        {
        this.portController = portcontroller;
        this.strName = portname;
        this.strDescription = description;
        this.strResourceKey = resourcekey;
        this.txStream = txstream;
        this.rxStream = rxstream;
        this.clock = obsclock;

        this.vecPortConfiguration = new Vector<Vector>(10);
        this.boolStaribusPort = false;
        this.boolPortOpen = false;
        this.boolPortBusy = false;

        // Cross-connect the streams to allow loopback testing
        getTxStream().setLoopbackRxStream(getRxStream());
        getRxStream().setLoopbackTxStream(getTxStream());

        // Find out those DAOs to which we are attached
        this.listHostDAOs = new ArrayList<ObservatoryInstrumentDAOInterface>(10);

        // Creates an ArrayBlockingQueue with the given (fixed) capacity
        // and the specified access policy - fair
        this.queueTx = new ArrayBlockingQueue<CommandMessageInterface>(QUEUE_CAPACITY, true);
        this.queueRx = new ArrayBlockingQueue<ResponseMessageInterface>(QUEUE_CAPACITY, true);

        // ToDo Review
        this.queueTx.clear();
        this.queueRx.clear();
        }


    /***********************************************************************************************
     * Open the Port.
     *
     * @return boolean
     */

    public boolean open()
        {
        final String SOURCE = "DaoPort.open() ";
        final boolean boolDebug;
        boolean boolSuccess;

        boolDebug = (LOADER_PROPERTIES.isTimingDebug()
                     || LOADER_PROPERTIES.isStaribusDebug()
                     || LOADER_PROPERTIES.isStarinetDebug()
                     || LOADER_PROPERTIES.isThreadsDebug());

        LOGGER.debugTimedEvent(boolDebug,
                               SOURCE + "[name=" + getName() + "]");

        // Add some configuration for Reports
        LOGGER.debugTimedEvent(boolDebug,
                               SOURCE + "Adding name & description to PortConfiguration");

        getPortConfiguration().clear();

        ConfigurationHelper.addItemToConfiguration(getPortConfiguration(),
                                                   PropertyPlugin.PROPERTY_ICON,
                                                   getResourceKey() + KEY_NAME,
                                                   getName());
        ConfigurationHelper.addItemToConfiguration(getPortConfiguration(),
                                                   PropertyPlugin.PROPERTY_ICON,
                                                   getResourceKey() + KEY_DESCRIPTION,
                                                   getDescription());
        boolSuccess = false;
        setPortBusy(false);

        if (getTxStream() != null)
            {
            try
                {
                // Reload the Stream Configuration every time the Resources are read
                if (getTxStream().getStreamConfiguration() != null)
                    {
                    getTxStream().getStreamConfiguration().clear();
                    }

                boolSuccess = getTxStream().open();
                }

            catch (IOException exception)
                {
                LOGGER.error("DaoPort unable to open TxStream " + exception.getMessage());
                boolSuccess = false;
                }
            }

        // Only try the RxStream if the TxStream worked
        if ((boolSuccess)
            && (getRxStream() != null))
            {
            try
                {
                // Reload the Stream Configuration every time the Resources are read
                if (getRxStream().getStreamConfiguration() != null)
                    {
                    getRxStream().getStreamConfiguration().clear();
                    }

                boolSuccess = getRxStream().open();
                }

            catch (IOException exception)
                {
                LOGGER.error("DaoPort unable to open RxStream " + exception.getMessage());
                boolSuccess = false;
                }
            }

        // Mark this Port as Open, if successful
        this.boolPortOpen = boolSuccess;

        return (boolSuccess);
        }


    /***********************************************************************************************
     * Close the Port.
     *
     * @return boolean
     */

    public boolean close()
        {
        final String SOURCE = "DaoPort.close() ";
        final boolean boolDebug;
        boolean boolSuccess;

        boolDebug = (LOADER_PROPERTIES.isTimingDebug()
                     || LOADER_PROPERTIES.isStaribusDebug()
                     || LOADER_PROPERTIES.isStarinetDebug()
                     || LOADER_PROPERTIES.isThreadsDebug());

        LOGGER.debugTimedEvent(boolDebug,
                               SOURCE + "[name=" + getName() + "]");

        boolSuccess = true;

        if (getTxStream() != null)
            {
            try
                {
                if (getTxStream().getStreamConfiguration() != null)
                    {
                    getTxStream().getStreamConfiguration().clear();
                    }

                getTxStream().close();
                }

            catch (IOException exception)
                {
                LOGGER.error("DaoPort unable to close TxStream " + exception.getMessage());
                boolSuccess = false;
                }
            }

        if (getRxStream() != null)
            {
            try
                {
                if (getRxStream().getStreamConfiguration() != null)
                    {
                    getRxStream().getStreamConfiguration().clear();
                    }

                getRxStream().close();
                }

            catch (IOException exception)
                {
                LOGGER.error("DaoPort unable to close RxStream " + exception.getMessage());
                boolSuccess = false;
                }
            }

        getPortConfiguration().clear();
        getHostDAOs().clear();

        // Mark this Port as Closed
        this.boolPortOpen = false;
        setPortBusy(false);

        return (boolSuccess);
        }


    /***********************************************************************************************
     * Indicate if this Port is Open.
     *
     * @return boolean
     */

    public boolean isPortOpen()
        {
        return (this.boolPortOpen);
        }


    /***********************************************************************************************
     * Queue a Command message for sending to the Port.
     * Wait for the specified time for space to appear in the queue, then give up.
     *
     * @param message
     * @param timeoutmillis
     * @param obsclock
     * @param notifymonitors
     * @param debug
     */

    public void queueCommandMessage(final CommandMessageInterface message,
                                    final long timeoutmillis,
                                    final ObservatoryClockInterface obsclock,
                                    final boolean notifymonitors,
                                    final boolean debug)
        {
        final String SOURCE = "DaoPort.queueCommandMessage() ";

        if ((getTxQueue() != null)
            && (getRxQueue() != null)
            && (message != null))
            {
            try
                {
                final boolean boolSuccess;

                // Queue it!
                LOGGER.debugTimedEvent(debug,
                                       SOURCE + "Offering Command to Port Controller Tx Queue [command=" + message.getStarScript()
                                           + "] [txqueuesize=" + getTxQueue().size()
                                           + "] [rxqueuesize=" + getRxQueue().size()
                                           + "] [thread.group=" + REGISTRY.getThreadGroup().getName()
                                           + "] [thread.name=" + Thread.currentThread().getName() + "]");

                // Inserts the specified element into this queue if it is possible
                // to do so immediately without violating capacity restrictions
                boolSuccess = getTxQueue().offer(message);
                }

//            catch (ConcurrentModificationException exception)
//                {
//                LOGGER.error("DaoPort.queueCommandMessage() ConcurrentModificationException");
//                }

//            catch (InterruptedException exception)
//                {
//                LOGGER.error("DaoPort.queueCommandMessage() InterruptedException while waiting for TxQueue");
//                exception.printStackTrace();
//                }

            catch (IllegalArgumentException exception)
                {
                LOGGER.error(SOURCE + "Illegal Message argument for TxQueue");
                }
            }
        else
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_NULL);
            }
        }


    /***********************************************************************************************
     * Get a message from the Port receive queue from the specified source.
     * Wait for the specified time for an item to appear in the queue, then give up, returning null.
     *
     * @param dao
     * @param timeoutmillis
     * @param notifymonitors
     * @param debug
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface dequeueResponseMessage(final ObservatoryInstrumentDAOInterface dao,
                                                           final long timeoutmillis,
                                                           final boolean notifymonitors,
                                                           final boolean debug)
        {
        final String SOURCE = "DaoPort.dequeueResponseMessage() ";
        ResponseMessageInterface responseMessage;

        responseMessage = null;

        // We won't get anywhere without a queue or Thread!
        if ((getTxQueue() != null)
            && (getRxQueue() != null)
            && (dao != null))
            {
            final long longTimeoutMillis;
            final long longStartTimeMillis;

            longTimeoutMillis = timeoutmillis;

            // We only need to count milliseconds, so there's no need for Date
            longStartTimeMillis = System.currentTimeMillis();

            LOGGER.debugTimedEvent(debug,
                                   SOURCE + "QUEUE Timeout started [requested_timeout=" + longTimeoutMillis
                                       + " msec] [txqueuesize=" + getTxQueue().size()
                                       + "] [rxqueuesize=" + getRxQueue().size()
                                       + "] [thread.group=" + REGISTRY.getThreadGroup().getName()
                                       + "] [thread.name=" + Thread.currentThread().getName() + "]");

            // Don't poll the queue if we have run out of time
            while (((System.currentTimeMillis() - longStartTimeMillis) < longTimeoutMillis)
                && (responseMessage == null))
                {
                // Allow things to happen...
                Utilities.safeSleep(5);

                // peek() Retrieves, but does not remove, the head of this queue,
                // or returns null if this queue is empty.
                responseMessage = getRxQueue().peek();
                }

            LOGGER.debugTimedEvent(debug,
                                   SOURCE + "QUEUE left waiting loop [txqueuesize=" + getTxQueue().size()
                                        + "] [rxqueuesize=" + getRxQueue().size()
                                        + "] [thread.group=" + REGISTRY.getThreadGroup().getName()
                                        + "] [thread.name=" + Thread.currentThread().getName() + "]");

            if (responseMessage != null)
                {
                try
                    {
                    //LOGGER.debugProtocolEvent("DaoPort.dequeueResponseMessage() Response obtained from RxQueue [elapsed=" + (System.currentTimeMillis() - longStart) + "msec]");

                    // Now remove the item from the queue
                    // This should never fail!
                    responseMessage = getRxQueue().remove();
                    ObservatoryInstrumentHelper.timestampResponseMessage(responseMessage,
                                                                         dao.getObservatoryClock());

//                    if ((responseMessage.getCommandType() != null)
//                        && (responseMessage.getCommandType().getResponse() != null))
//                        {
//                        LOGGER.debugTimedEvent("DaoPort.dequeueResponseMessage() [ResponseValue=" + responseMessage.getCommandType().getResponse().getValue() + "]");
//                        }
//                    else
//                        {
//                        LOGGER.debugTimedEvent("DaoPort.dequeueResponseMessage() [ResponseValue=NULL]");
//                        }
                    }

                catch (NoSuchElementException exception)
                    {
                    LOGGER.error(SOURCE + "NoSuchElementException - QUEUE was unexpectedly empty" );
                    responseMessage = null;
                    }
                }
            else
                {
                // Timeout occurred
                LOGGER.debugTimedEvent(debug,
                                       SOURCE + "QUEUE Timeout occurred [requested_timeout=" + timeoutmillis
                                            + "msec] [elapsed=" + (System.currentTimeMillis() - longStartTimeMillis)
                                            + "msec] [txqueuesize=" + getTxQueue().size()
                                            + "] [rxqueuesize=" + getRxQueue().size()
                                            + "] [thread.group=" + REGISTRY.getThreadGroup().getName()
                                            + "] [thread.name=" + Thread.currentThread().getName() + "]");

                // The ResponseMessage is NULL, so we can't set ResponseStatus in the message
                // Set the status in the DAO to be consistent with the DAO Timer
                dao.getResponseMessageStatusList().add(ResponseMessageStatus.TIMEOUT);
                }
            }
        else
            {
            // We should never need to know about this!
            //throw new FrameworkException(EXCEPTION_PARAMETER_NULL);
            }

        return (responseMessage);
        }

    // Removed - this fails if we are driven by a Timer rather than a SwingWorker!
//    (dao.getExecuteWorker() != null)
//            && (!dao.getExecuteWorker().isStopping())
//            &&

    /***********************************************************************************************
     * Clear both Tx and Rx Queues of all messages.
     */

    public void clearQueues()
        {
        final String SOURCE = "DaoPort.clearQueues() ";
        final boolean boolDebug;

        boolDebug = (LOADER_PROPERTIES.isTimingDebug()
                     || LOADER_PROPERTIES.isStaribusDebug()
                     || LOADER_PROPERTIES.isStarinetDebug()
                     || LOADER_PROPERTIES.isThreadsDebug());

        if (getTxQueue() != null)
            {
            LOGGER.debugTimedEvent(boolDebug,
                                   SOURCE + "Transmit [size=" + getTxQueue().size() + "]");
            getTxQueue().drainTo(new Vector<CommandMessageInterface>(getTxQueue().size()));
            }

        if (getRxQueue() != null)
            {
            LOGGER.debugTimedEvent(boolDebug,
                                   SOURCE + "Receive [size=" + getRxQueue().size() + "]");
            getRxQueue().drainTo(new Vector<ResponseMessageInterface>(getTxQueue().size()));
            }
        }


    /***********************************************************************************************
     * Get the PortController controlling this Port.
     *
     * @return PortControllerInterface
     */

    public PortControllerInterface getPortController()
        {
        return (this.portController);
        }


    /***********************************************************************************************
     * Get the name of the DaoPort.
     *
     * @return String
     */

    public String getName()
        {
        return (this.strName);
        }


    /***********************************************************************************************
     * Get the Description of the DaoPort.
     *
     * @return String
     */

    public String getDescription()
        {
        return (this.strDescription);
        }


    /***********************************************************************************************
     * Get the ResourceKey for the Port.
     *
     * @return String
     */

    public String getResourceKey()
        {
        return (this.strResourceKey);
        }


    /***********************************************************************************************
     * Get the List of host DAOs for this Port.
     *
     * @return List<ObservatoryInstrumentDAOInterface>
     */

    public List<ObservatoryInstrumentDAOInterface> getHostDAOs()
        {
        return (this.listHostDAOs);
        }


    /***********************************************************************************************
     * Add a host DAO to this DaoPort.
     *
     * @param dao
     */

    public void addHostDAO(final ObservatoryInstrumentDAOInterface dao)
        {
        final String SOURCE = "DaoPort.addHostDAO() ";
        final boolean boolDebug;

        boolDebug = (LOADER_PROPERTIES.isTimingDebug()
                     || LOADER_PROPERTIES.isStaribusDebug()
                     || LOADER_PROPERTIES.isStarinetDebug()
                     || LOADER_PROPERTIES.isThreadsDebug());

        if ((dao != null)
            && (getHostDAOs() != null)
            && (!getHostDAOs().contains(dao)))
            {
            LOGGER.debugTimedEvent(boolDebug,
                                   SOURCE + "Adding DAO [name=" + dao.getClass().getName() + "]");
            getHostDAOs().add(dao);
            }
        }


    /***********************************************************************************************
     * Remove a host DAO from this DaoPort.
     *
     * @param dao
     */

    public void removeHostDAO(final ObservatoryInstrumentDAOInterface dao)
        {
        final String SOURCE = "DaoPort.removeHostDAO() ";
        final boolean boolDebug;

        boolDebug = (LOADER_PROPERTIES.isTimingDebug()
                     || LOADER_PROPERTIES.isStaribusDebug()
                     || LOADER_PROPERTIES.isStarinetDebug()
                     || LOADER_PROPERTIES.isThreadsDebug());

        if ((dao != null)
            && (getHostDAOs() != null)
            && (getHostDAOs().contains(dao)))
            {
            LOGGER.debugTimedEvent(boolDebug,
                                   SOURCE + "Removing DAO [name=" + dao.getClass().getName() + "]");
            getHostDAOs().remove(dao);
            }
        }


    /***********************************************************************************************
     * Get the TxStream.
     *
     * @return PortTxStreamInterface
     */

    public PortTxStreamInterface getTxStream()
        {
        return (this.txStream);
        }


    /***********************************************************************************************
     * Set the TxStream.
     *
     * @param stream
     */

    public void setTxStream(final PortTxStreamInterface stream)
        {
        this.txStream = stream;
        }


    /***********************************************************************************************
     * Get the RxStream.
     *
     * @return PortRxStreamInterface
     */

    public PortRxStreamInterface getRxStream()
        {
        return (this.rxStream);
        }


    /***********************************************************************************************
     * Set the RxStream.
     *
     * @param stream
     */

    public void setRxStream(final PortRxStreamInterface stream)
        {
        this.rxStream = stream;
        }


    /***********************************************************************************************
     * Get the Tx Queue.
     *
     * @return BlockingQueue<CommandMessageInterface>
     */

    public BlockingQueue<CommandMessageInterface> getTxQueue()
        {
        return (this.queueTx);
        }


    /***********************************************************************************************
     * Get the Rx Queue.
     *
     * @return BlockingQueue<ResponseMessageInterface>
     */

    public BlockingQueue<ResponseMessageInterface> getRxQueue()
        {
        return (this.queueRx);
        }


    /***********************************************************************************************
     * Get the Vector of extra data to append to a Report.
     *
     * @return Vector<Vector>
     */

    public Vector<Vector> getPortConfiguration()
        {
        return (this.vecPortConfiguration);
        }


    /***********************************************************************************************
     * Indicate if this Port is the StaribusPort for the Observatory.
     *
     * @return boolean
     */

    public boolean isStaribusPort()
        {
        return (this.boolStaribusPort);
        }


    /***********************************************************************************************
     * Make this Port into the StaribusPort.
     * This action cannot be undone.
     */

    public void makeStaribusPort()
        {
        this.boolStaribusPort = true;
        }


    /***********************************************************************************************
     * Indicate if this Port is Busy.
     *
     * @return boolean
     */

    public synchronized boolean isPortBusy()
        {
        return (this.boolPortBusy);
        }


    /***********************************************************************************************
     * Control the Busy state of this Port.
     */

    public synchronized void setPortBusy(final boolean busy)
        {
        final String SOURCE = "DaoPort.setPortBusy() ";
        final boolean boolDebug;

        boolDebug = (LOADER_PROPERTIES.isTimingDebug()
                     || LOADER_PROPERTIES.isStaribusDebug()
                     || LOADER_PROPERTIES.isStarinetDebug()
                     || LOADER_PROPERTIES.isThreadsDebug());

        this.boolPortBusy = busy;

        LOGGER.debugTimedEvent(boolDebug,
                               SOURCE + "[busy=" + isPortBusy() + "]");
        }


    /***********************************************************************************************
     * Get the ObservatoryClock.
     *
     * @return ObservatoryClockInterface
     */

    public ObservatoryClockInterface getObservatoryClock()
        {
        return (this.clock);
        }
    }
