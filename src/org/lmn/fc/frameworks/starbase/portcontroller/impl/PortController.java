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

import org.lmn.fc.common.utilities.misc.CRC16;
import org.lmn.fc.common.utilities.misc.Utilities;
import org.lmn.fc.common.utilities.time.ChronosHelper;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.DaoPortInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.PortControllerInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.events.*;
import org.lmn.fc.model.xmlbeans.instruments.InstrumentsDocument;

import javax.swing.*;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;


/***************************************************************************************************
 * The PortController.
 */

public final class PortController implements PortControllerInterface
    {
    private volatile static PortControllerInterface PORTCONTROLLER_INSTANCE;

    private InstrumentsDocument docInstruments;
    private boolean boolRunning;
    private Thread threadController;
    private DaoPortInterface staribusPort;

    private final Queue<DaoPortInterface> queuePortAdditions;
    private final Queue<DaoPortInterface> queuePortRemovals;
    private final List<DaoPortInterface> listPorts;

    // ToDo Make these into List
    private final Vector<PortMessageListener> vecPortMessageListeners;
    private final Vector<PortControllerHeartbeatListener> vecHeartbeatListeners;
    private final Vector<CommandLifecycleListener> vecLifecycleListeners;

    private long longLatencyMillis;


    /***********************************************************************************************
     * The PortController is a Singleton!
     *
     * @return Registry
     */

    public static PortControllerInterface getInstance()
        {
        if (PORTCONTROLLER_INSTANCE == null)
            {
            synchronized (PortController.class)
                {
                if (PORTCONTROLLER_INSTANCE == null)
                    {
                    PORTCONTROLLER_INSTANCE = new PortController();
                    }
                }
            }

        return (PORTCONTROLLER_INSTANCE);
        }


    /***********************************************************************************************
     * Show a message that something went wrong when trying to control the Port.
     */

    private static void unableToControlPort()
        {
        final String [] strMessage =
            {
            "It has not been possible to establish a connection with the Staribus Port.",
            "The Observatory can run in this mode, but all Staribus commands will time out.",
            "Instruments with virtual controllers will function correctly.",
            "Examine the EventLog for possible causes of this problem."
            };

        JOptionPane.showMessageDialog(null,
                                      strMessage,
                                      "Port ObservatoryMonitor",
                                      JOptionPane.WARNING_MESSAGE);
        }


    /***********************************************************************************************
     * Show the Tx Queue.
     *
     * @param txqueue
     */

    private static void showTxQueue(final BlockingQueue<CommandMessageInterface> txqueue)
        {
//        final BlockingQueue<CommandMessageInterface> txQueue;
//
//        txQueue = (BlockingQueue<CommandMessageInterface>)Collections.synchronizedCollection(getTxQueue());

        synchronized(txqueue)
            {
            if (txqueue != null)
                {
                LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                       "PortController Transmit Queue");

                if (!txqueue.isEmpty())
                    {
                    final Iterator<CommandMessageInterface> iterTxQueue;

                    iterTxQueue = txqueue.iterator();

                    while (iterTxQueue.hasNext())
                        {
                        final CommandMessageInterface messageInterface;

                        messageInterface = iterTxQueue.next();

                        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                               (INDENT + messageInterface.getDAO().getResourceKey()
                                     + SPACE + ChronosHelper.toCalendarString(messageInterface.getTxCalendar())
                                     + SPACE + messageInterface.toString()));
                        }
                    }
                else
                    {
                    LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                           INDENT + "Tx Queue is empty");
                    }
                }
            }
        }


    /***********************************************************************************************
     * Privately construct the PortController.
     */

    private PortController()
        {
        this.docInstruments = null;
        this.boolRunning = false;
        this.threadController = null;
        this.staribusPort = null;

        this.queuePortAdditions = new ConcurrentLinkedQueue<DaoPortInterface>();
        this.queuePortRemovals = new ConcurrentLinkedQueue<DaoPortInterface>();
        this.listPorts = new ArrayList<DaoPortInterface>(10);
        this.vecPortMessageListeners = new Vector<PortMessageListener>(10);
        this.vecHeartbeatListeners = new Vector<PortControllerHeartbeatListener>(10);
        this.vecLifecycleListeners = new Vector<CommandLifecycleListener>(10);

        this.longLatencyMillis = LATENCY_MILLISEC;
        }


    /***********************************************************************************************
     * Start the PortController.
     * Specify the Instruments document in order to give the context of all Messages.
     *
     * @param instrumentsdoc
     */

    public synchronized boolean start(final InstrumentsDocument instrumentsdoc)
        {
        try
            {
            //LOGGER.debugTimedEvent("PortController.start()");

            // Reflect input values in the CRC table
            CRC16.generateCrcTable(true);

            setRunning(false);

            if ((getControllerThread() != null)
                && (getControllerThread().isAlive()))
                {
                //LOGGER.debugTimedEvent("PortController.start() Thread is ALIVE, WAIT for Thread to stop");
                getControllerThread().join(getLatencyMillis() << 1);
                }
            }

        catch (InterruptedException exception)
            {
            Thread.currentThread().interrupt();
            //LOGGER.debugTimedEvent("PortController.start() InterruptedException");
            }

        finally
            {
            setInstrumentsDoc(instrumentsdoc);

            setControllerThread(new Thread(REGISTRY.getThreadGroup(),
                                this,
                                "Thread PortController"));
            setRunning(true);

            getControllerThread().start();

            // ToDo log in ObservatoryLog
            //LOGGER.debugTimedEvent("PortController running");
            }

        return (isRunning());
        }


    /***********************************************************************************************
     * Stop the PortController.
     */

    public synchronized boolean stop()
        {
        // ToDo log in ObservatoryLog
        //LOGGER.debugTimedEvent("PortController.stop()");

        try
            {
            setRunning(false);

            if ((getControllerThread() != null)
                && (getControllerThread().isAlive()))
                {
                //LOGGER.debugTimedEvent("PortController.stop() Thread is ALIVE, WAIT for Thread to stop");
                getControllerThread().join(getLatencyMillis() << 1);
                }
            }

        catch (InterruptedException exception)
            {
            Thread.currentThread().interrupt();
            //LOGGER.debugTimedEvent("PortController.stop() InterruptedException");
            }

        finally
            {
            setInstrumentsDoc(null);

            // ToDo log in ObservatoryLog
            //LOGGER.debugTimedEvent("PortController stopped");
            }

        return(true);
        }


    /***********************************************************************************************
     * Request that a DaoPort be attached to the PortController as soon as possible.
     *
     * @param daoport
     */

    public synchronized void requestPortAddition(final DaoPortInterface daoport)
        {
        if ((daoport != null)
            && (getPortAdditions() != null))
            {
            try
                {
                getPortAdditions().add(daoport);
                //LOGGER.debugTimedEvent("PortController.requestPortAddition() [name=" + daoport.getName() + "]");
                }

            catch (IllegalStateException  exception)
                {
                LOGGER.error("The PortController Port Additions queue is full");
                }
            }
        }


    /***********************************************************************************************
     * Request that a DaoPort be removed from the PortController as soon as possible.
     *
     * @param daoport
     */

    public synchronized void requestPortRemoval(final DaoPortInterface daoport)
        {
        if ((daoport != null)
            && (getPortRemovals() != null))
            {
            try
                {
                getPortRemovals().add(daoport);
                //LOGGER.debugTimedEvent("PortController.requestPortRemoval() [name=" + daoport.getName() + "]");
                }

            catch (IllegalStateException  exception)
                {
                LOGGER.error("The PortController Port Removal queue is full");
                }
            }
        }


    /***********************************************************************************************
     * Empty the Tx Queue into the Tx Stream, and empty the Rx Stream into the Rx Queue.
     */

    // BlockingQueue implementations are thread-safe.
    // All queueing methods achieve their effects atomically using internal locks
    // or other forms of concurrency control.

    public void run()
        {
        final String SOURCE = "PortController.run() ";
        final boolean boolDebug;

        boolDebug = (LOADER_PROPERTIES.isTimingDebug()
                     || LOADER_PROPERTIES.isStaribusDebug()
                     || LOADER_PROPERTIES.isStarinetDebug()
                     || LOADER_PROPERTIES.isThreadsDebug());

        while (isRunning())
            {
            // Let everyone know we are still alive...
            notifyPortControllerHeartbeatEvent(this);

            try
                {
                final Iterator iterPorts;

                // Dequeue any pending DaoPort Removals from the PortList
                // because we must not allow modifications of the PortList during Iteration!
                // peek() Retrieves, but does not remove, the head of this queue, or returns null if this queue is empty.

                while ((getPortRemovals() != null)
                    && (!getPortRemovals().isEmpty())
                    && (getPortRemovals().peek() != null))
                    {
                    getPortList().remove(getPortRemovals().remove());
                    }

                // Dequeue any pending DaoPort Additions into the PortList

                while ((getPortAdditions() != null)
                    && (!getPortAdditions().isEmpty())
                    && (getPortAdditions().peek() != null))
                    {
                    getPortList().add(getPortAdditions().remove());
                    }

                // Now it is safe to iterate over the DaoPorts to be controlled
                iterPorts = getPortList().iterator();

                // Remember that there are no Ports until at least one Instrument with a DAO has been started
                // so wait here until ready to proceed
                if (!iterPorts.hasNext())
                    {
                    Utilities.safeSleep(getLatencyMillis());
                    }

                while (iterPorts.hasNext())
                    {
                    final DaoPortInterface port;

                    port = (DaoPortInterface) iterPorts.next();

                    // Empty the Tx Queue into the Tx Stream for each Port
                    // Only Remote Commands can appear in the Transmit Queue
                    // so we don't need to check sendToPort()
                    if ((port != null)
                        && (port.isPortOpen())
                        && (port.getTxQueue() != null)
                        && (port.getTxStream() != null)
                        && (port.getTxStream().isStreamOpen()))
                        {
                        if (!port.getTxQueue().isEmpty())
                            {
                            CommandMessageInterface commandmessage;

                            // Retrieves, but does not remove, the head of this queue,
                            // or returns null if this queue is empty.
                            // We know that an item *should* be immediately available
                            // TxQueue.take() would generate an InterruptedException
                            commandmessage = port.getTxQueue().peek();

                            if (commandmessage != null)
                                {
                                LOGGER.debugTimedEvent(boolDebug,
                                                       SOURCE + "TxQueue.remove() --> TxStream.write() [Tx_queuesize=" + port.getTxQueue().size()
                                                            + "] [latency=" + getLatencyMillis() + "msec]");

                                commandmessage = port.getTxQueue().remove();
                                port.getTxStream().write(getInstrumentsDoc(),
                                                         commandmessage);

                                // Tell any Port Monitors...
                                notifyPortMessageEvent(this,
                                                       commandmessage,
                                                       null);
                                }
                            else
                                {
                                LOGGER.error(SOURCE + "TxQueue unexpectedly empty");
                                }
                            }
                        }
                    else
                        {
                        //LOGGER.debugTimedEvent(SOURCE + "Closed or NULL DaoPort, TxQueue, or TxStream");
                        }

                    //------------------------------------------------------------------------------
                    // Wait for a response?

                    Utilities.safeSleep(getLatencyMillis());

                    //------------------------------------------------------------------------------

                    // Read from the Rx Stream into the Rx Queue for each Port
                    // Only Responses from Remote Commands can appear in the Receive Stream
                    if ((port != null)
                        && (port.isPortOpen())
                        && (port.getRxQueue() != null)
                        && (port.getRxStream() != null)
                        && (port.getRxStream().isStreamOpen()))
                        {
                        final ResponseMessageInterface responsemessage;

                        responsemessage = port.getRxStream().read(getInstrumentsDoc());

                        // A null Response means that there's nothing in the RxStream,
                        // so just wait until next time around...
                        if (responsemessage != null)
                            {
                            final boolean boolRxSuccess;

                            LOGGER.debugTimedEvent(boolDebug,
                                                   SOURCE + "Response Message received from RxStream");

                            // Queue the received response
                            // Inserts the specified element into this queue
                            // if it is possible to do so immediately without violating capacity restrictions
                            // We know that space *should* be immediately available
                            // RxQueue().put() would generate an InterruptedException
                            boolRxSuccess = port.getRxQueue().offer(responsemessage);

                            if (boolRxSuccess)
                                {
                                LOGGER.debugTimedEvent(boolDebug,
                                                       SOURCE + "Response received RxStream.read() --> RxQueue.offer() [Rx_queuesize=" + port.getRxQueue().size() + "]");

                                // Tell any Response consumers and Port Monitors...
                                notifyPortMessageEvent(this,
                                                       null,
                                                       responsemessage);
                                }
                            else
                                {
                                LOGGER.error(SOURCE + "Unable to put ResponseMessage into RxQueue");
                                }
                            }
                        }
                    else
                        {
                        //LOGGER.debugTimedEvent(SOURCE + "Closed or NULL DaoPort, RxQueue or RxStream");
                        }
                    }
                }

            catch (IOException exception)
                {
                LOGGER.error(SOURCE + "IOException "
                                + METADATA_EXCEPTION
                                + exception.getMessage()
                                + TERMINATOR);
                }

            catch (NoSuchElementException exception)
                {
                LOGGER.error(SOURCE + "NoSuchElementException "
                                + METADATA_EXCEPTION
                                + exception.getMessage()
                                + TERMINATOR);
                }

            catch (IllegalArgumentException exception)
                {
                LOGGER.error(SOURCE + "IllegalArgumentException "
                                + METADATA_EXCEPTION
                                + exception.getMessage()
                                + TERMINATOR);
                }

            catch (ConcurrentModificationException exception)
                {
                LOGGER.error(SOURCE + "ConcurrentModificationException "
                             + METADATA_EXCEPTION
                             + exception.getMessage()
                             + TERMINATOR);
                exception.printStackTrace();
                }

            catch (Exception exception)
                {
                LOGGER.error(SOURCE + "Exception "
                             + METADATA_EXCEPTION
                             + exception.getMessage()
                             + TERMINATOR);
                exception.printStackTrace();
                }
            }

        //LOGGER.debugTimedEvent(SOURCE + "Thread leaving run() loop");
        }


    /**********************************************************************************************/
    /* Utilities                                                                                  */
    /***********************************************************************************************
     * Get a flag indicating if the PortController is running.
     *
     * @return boolean
     */

    public synchronized boolean isRunning()
        {
        return (this.boolRunning);
        }


    /***********************************************************************************************
     * Set a flag indicating if the PortController should run.
     *
     * @param running
     */

    public synchronized void setRunning(final boolean running)
        {
        //LOGGER.debugTimedEvent("PortController.setRunning() " + running);
        this.boolRunning = running;
        }


    /***********************************************************************************************
     * Get the StaribusPort, or null if not assigned.
     *
     * @return DaoPortInterface
     */

    public synchronized DaoPortInterface getStaribusPort()
        {
        return (this.staribusPort);
        }


    /***********************************************************************************************
     * Set the StaribusPort if not already assigned, or null.
     * Return false if the StaribusPort is already set.
     *
     * @param staribusport
     */

    public synchronized boolean setStaribusPort(final DaoPortInterface staribusport)
        {
        final boolean boolSuccess;

        if ((getStaribusPort() == null)
            && (staribusport.isStaribusPort()))
            {
            this.staribusPort = staribusport;
            boolSuccess = true;
            }
        else
            {
            // There is already a StaribusPort, or the one specified is incorrectly configured
            boolSuccess = false;
            }

        return (boolSuccess);
        }


    /***********************************************************************************************
     * Get the InstrumentsDocument.
     *
     * @return InstrumentsDocument
     */

    private synchronized InstrumentsDocument getInstrumentsDoc()
        {
        return (this.docInstruments);
        }


    /***********************************************************************************************
     * Set the InstrumentsDocument.
     *
     * @param doc
     */

    private synchronized void setInstrumentsDoc(final InstrumentsDocument doc)
        {
        this.docInstruments = doc;
        }


    /***********************************************************************************************
     * Get the Queue of DaoPorts waiting to be added to the PortController.
     *
     * @return Queue<DaoPortInterface>
     */

    private synchronized Queue<DaoPortInterface> getPortAdditions()
        {
        return(this.queuePortAdditions);
        }


    /***********************************************************************************************
     * Get the Queue of DaoPorts waiting to be removed from the PortController.
     *
     * @return Queue<DaoPortInterface>
     */

    private synchronized Queue<DaoPortInterface> getPortRemovals()
        {
        return(this.queuePortRemovals);
        }


    /***********************************************************************************************
     * Get the List of DaoPorts controlled by the PortController.
     *
     * @return List<DaoPortInterface>
     */

    private synchronized List<DaoPortInterface> getPortList()
        {
        return(this.listPorts);
        }


    /***********************************************************************************************
     * Get the PortController Thread.
     *
     * @return Thread
     */

    private Thread getControllerThread()
        {
        return (this.threadController);
        }


    /***********************************************************************************************
     * Set the PortController Thread.
     *
     * @param thread
     */

    private void setControllerThread(final Thread thread)
        {
        this.threadController = thread;
        }


    /***********************************************************************************************
     * Get the Port Controller latency between Transmit and Receive.
     *
     * @return long
     */

    public long getLatencyMillis()
        {
        return(this.longLatencyMillis);
        }


    /***********************************************************************************************
     * Set the Port Controller latency between Transmit and Receive.
     *
     * @param latency
     */

    public void setLatencyMillis(final long latency)
        {
        this.longLatencyMillis = latency;
        }


    /**********************************************************************************************/
    /* PortMessageEvents                                                                          */
    /***********************************************************************************************
     * Notify all listeners of PortMessage Events.
     *
     * @param source
     * @param commandmessage
     * @param responsemessage
     */

    public final void notifyPortMessageEvent(final Object source,
                                             final CommandMessageInterface commandmessage,
                                             final ResponseMessageInterface responsemessage)
        {
        List<PortMessageListener> listeners;
        final PortMessageEvent portEvent;

        // Create a Thread-safe List of Listeners
        listeners = new CopyOnWriteArrayList<PortMessageListener>(getPortMessageListeners());

        // Create a PortMessageEvent
        portEvent = new PortMessageEvent(source,
                                         commandmessage,
                                         responsemessage);

        // Fire the event to every listener
        synchronized(listeners)
            {
            for (int i = 0; i < listeners.size(); i++)
                {
                final PortMessageListener portListener;

                portListener = listeners.get(i);
                portListener.messageChanged(portEvent);
                }

            // Help the GC?
            listeners = null;
            }
        }


    /***********************************************************************************************
     * Get the PortMessageListeners (mostly for testing).
     *
     * @return Vector<PortMessageListener>
     */

    private Vector<PortMessageListener> getPortMessageListeners()
        {
        return (this.vecPortMessageListeners);
        }


    /***********************************************************************************************
     * Add a PortMessage Listener.
     *
     * @param listener
     */

    public final void addPortMessageListener(final PortMessageListener listener)
        {
        if ((listener != null)
            && (getPortMessageListeners() != null))
            {
            //LOGGER.debugTimedEvent("PortController Add a PortMessage Listener");
            getPortMessageListeners().addElement(listener);
            }
        }


    /***********************************************************************************************
     * Remove a PortMessage Listener.
     *
     * @param listener
     */

    public final void removePortMessageListener(final PortMessageListener listener)
        {
        if ((listener != null)
            && (getPortMessageListeners() != null))
            {
            //LOGGER.debugTimedEvent("PortController Remove a PortMessage Listener");
            getPortMessageListeners().removeElement(listener);
            }
        }


    /**********************************************************************************************/
    /* CommandLifecyleEvents                                                                      */
    /***********************************************************************************************
     * Notify all listeners of a CommandLifecycleEvent.
     *
     * @param source
     * @param commandmessage
     * @param responsemessage
     *
     * @return CommandLifecycleEventInterface
     */

    public final CommandLifecycleEventInterface notifyCommandLifecycleEvent(final Object source,
                                                                            final CommandMessageInterface commandmessage,
                                                                            final ResponseMessageInterface responsemessage)
        {
        List<CommandLifecycleListener> listeners;
        final CommandLifecycleEventInterface lifecycleEvent;

        // Create a Thread-safe List of Listeners
        listeners = new CopyOnWriteArrayList<CommandLifecycleListener>(getCommandLifecycleListeners());

        // Create a CommandLifecycleEvent
        lifecycleEvent = new CommandLifecycleEvent(source,
                                                   commandmessage,
                                                   responsemessage);
        // Fire the event to every listener
        synchronized(listeners)
            {
            for (int i = 0; i < listeners.size(); i++)
                {
                final CommandLifecycleListener lifecycleListener;

                lifecycleListener = listeners.get(i);
                lifecycleListener.commandChanged(lifecycleEvent);
                }

            // Help the GC?
            listeners = null;
            }

        return (lifecycleEvent);
        }


    /***********************************************************************************************
     * Notify all listeners of a CommandLifecycleEvent, when an event is already prepared.
     *
     * @param source
     * @param event
     *
     * @return CommandLifecycleEvent
     */

    public final CommandLifecycleEventInterface notifyCommandLifecycleEvent(final Object source,
                                                                            final CommandLifecycleEventInterface event)
        {
        List<CommandLifecycleListener> listeners;

        // Don't use source for now?

        // Create a Thread-safe List of Listeners
        listeners = new CopyOnWriteArrayList<CommandLifecycleListener>(getCommandLifecycleListeners());

        // Fire the event to every listener
        synchronized(listeners)
            {
            for (int i = 0; i < listeners.size(); i++)
                {
                final CommandLifecycleListener lifecycleListener;

                lifecycleListener = listeners.get(i);
                lifecycleListener.commandChanged(event);
                }

            // Help the GC?
            listeners = null;
            }

        return (event);
        }


    /***********************************************************************************************
     * Get the CommandLifecycleListeners (mostly for testing).
     *
     * @return Vector<CommandLifecycleListener>
     */

    private Vector<CommandLifecycleListener> getCommandLifecycleListeners()
        {
        return (this.vecLifecycleListeners);
        }


    /***********************************************************************************************
     * Add a CommandLifecycle Listener.
     *
     * @param listener
     */

    public final void addCommandLifecycleListener(final CommandLifecycleListener listener)
        {
        if ((listener != null)
            && (getCommandLifecycleListeners() != null))
            {
            //LOGGER.debugTimedEvent("PortController Add a CommandLifecycle Listener");
            getCommandLifecycleListeners().addElement(listener);
            }
        }


    /***********************************************************************************************
     * Remove a CommandLifecycle Listener.
     *
     * @param listener
     */

    public final void removeCommandLifecycleListener(final CommandLifecycleListener listener)
        {
        if ((listener != null)
            && (getCommandLifecycleListeners() != null))
            {
            //LOGGER.debugTimedEvent("PortController Remove a CommandLifecycle Listener");
            getCommandLifecycleListeners().removeElement(listener);
            }
        }


    /**********************************************************************************************/
    /* PortControllerHeartbeatEvents                                                              */
    /***********************************************************************************************
     * Notify all listeners of PortControllerHeartbeatEvents.
     *
     * @param source
     */

    public final void notifyPortControllerHeartbeatEvent(final Object source)
        {
        List<PortControllerHeartbeatListener> listeners;
        final PortControllerHeartbeatEvent portControllerEvent;

        // Create a Thread-safe List of Listeners
        listeners = new CopyOnWriteArrayList<PortControllerHeartbeatListener>(getPortControllerHeartbeatListeners());

        // Create a PortControllerHeartbeatListener
        portControllerEvent = new PortControllerHeartbeatEvent(source);

        // Fire the event to every listener
        synchronized(listeners)
            {
            for (int i = 0; i < listeners.size(); i++)
                {
                final PortControllerHeartbeatListener portControllerHeartbeatListener;

                portControllerHeartbeatListener = listeners.get(i);
                portControllerHeartbeatListener.heartBeat(portControllerEvent);
                }

            // Help the GC?
            listeners = null;
            }
        }


    /***********************************************************************************************
     * Get the PortControllerHeartbeatListeners (mostly for testing).
     *
     * @return Vector<PortControllerHeartbeatListener>
     */

    private Vector<PortControllerHeartbeatListener> getPortControllerHeartbeatListeners()
        {
        return (this.vecHeartbeatListeners);
        }


    /***********************************************************************************************
     * Add a PortControllerHeartbeat Listener.
     *
     * @param listener
     */

    public final void addPortControllerHeartbeatListener(final PortControllerHeartbeatListener listener)
        {
        if ((listener != null)
            && (getPortControllerHeartbeatListeners() != null))
            {
            //LOGGER.debugTimedEvent("PortController Add a PortControllerHeartbeat Listener");
            getPortControllerHeartbeatListeners().addElement(listener);
            }
        }


    /***********************************************************************************************
     * Remove a PortControllerHeartbeat Listener.
     *
     * @param listener
     */

    public final void removePortControllerHeartbeatListener(final PortControllerHeartbeatListener listener)
        {
        if ((listener != null)
            && (getPortControllerHeartbeatListeners() != null))
            {
            //LOGGER.debugTimedEvent("PortController Remove a PortControllerHeartbeat Listener");
            getPortControllerHeartbeatListeners().removeElement(listener);
            }
        }
    }
