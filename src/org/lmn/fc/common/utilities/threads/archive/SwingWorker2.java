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

package org.lmn.fc.common.utilities.threads.archive;

import javax.swing.*;

/***************************************************************************************************
 * This is the 3rd version of SwingWorker (also known as
 * SwingWorker 3), an abstract class that you subclass to
 * perform GUI-related work in a dedicated thread.  For
 * instructions on using this class, see:
 * <p/>
 * http://java.sun.com/docs/books/tutorial/uiswing/misc/threads.html
 * <p/>
 * Note that the API changed slightly in the 3rd version:
 * You must now invoke start() on the SwingWorker after
 * creating it.
 *
 * Time-consuming tasks should not be run on the Event Dispatch Thread.
 * Otherwise the application becomes unresponsive.
 * Swing components should be accessed on the Event Dispatch Thread only.
 * These constraints mean that a GUI application with time intensive computing
 * needs at least two threads:
 * 1) a thread to perform the lengthy task and
 * 2) the Event Dispatch Thread (EDT) for all GUI-related activities.
 * This involves inter-thread communication which can be tricky to implement.
 * SwingWorker is designed for situations where you need to have a long running task
 * run in a background thread and provide updates to the UI either when done, or while processing.
 */

public abstract class SwingWorker2
    {
    /***********************************************************************************************
     * Class to maintain reference to current worker thread
     * under separate synchronization control.
     */

    private static class ThreadVar
        {
        private Thread threadWorker;
        private boolean boolProcessing;

        ThreadVar(final Thread t)
            {
            threadWorker = t;
            boolProcessing = false;
            }

        synchronized Thread get()
            {
            return threadWorker;
            }

        synchronized void clear()
            {
            threadWorker = null;
            boolProcessing = false;
            }

        }

    private ThreadVar threadVar;
    private Object value;
    private volatile boolean boolRequestStop;


    /***********************************************************************************************
     * Start a thread that will call the <code>construct</code> task
     * and then exit.
     *
     * @param threadgroup
     * @param name
     */

    public SwingWorker2(final ThreadGroup threadgroup,
                        final String name)
        {
        final Runnable doFinished;
        final Runnable doConstruct;
        final Thread workerThread;

        doFinished = new Runnable()
            {
            public void run()
                {
                System.out.println("SW2 start FINISHED run()" + showName());
                try
                    {
                    finished();
                    }

                catch (Exception e)
                    {
                    e.printStackTrace();
                    }

                System.out.println("SW2 end FINISHED run()" + showName());
                }
            };

        doConstruct = new Runnable()
            {
            public void run()
                {
                System.out.println("SW2 start CONSTRUCT run()" + showName());
                try
                    {
                    if (!isStopping())
                        {
                        setValue(construct());
                        }
                    }

                catch (Exception e)
                    {
                    e.printStackTrace();
                    }

                finally
                    {
                    threadVar.clear();
                    }

                try
                    {
                    SwingUtilities.invokeLater(doFinished);
                    }
                catch (Exception e)
                    {
                    e.printStackTrace();
                    }

                System.out.println("SW2 end CONSTRUCT run()" + showName());
                }
            };

        // Construct the result on a new Thread
        if ((threadgroup != null)
            && (name != null)
            && (!"".equals(name)))
            {
            //System.out.println("ADDING NEW THREAD TO GROUP [" + threadgroup.getName() + "]");
//            workerThread = new Thread(threadgroup,
//                                      doConstruct,
//                                      name);
            workerThread = new Thread(doConstruct, "SwingWorker2 " + name);
            }
        else
            {
            workerThread = new Thread(doConstruct, "SwingWorker2 " + name);
            }

        // Save a reference to this result Thread
        this.threadVar = new ThreadVar(workerThread);
        System.out.println("MAKE NEW SW2 Thread" + showName());

        this.value = null;
        this.boolRequestStop = false;
        }


    /***********************************************************************************************
     * Compute the value to be returned by the <code>get</code> task.
     *
     * @return Object
     */

    public abstract Object construct();


    /***********************************************************************************************
     * Called on the event dispatching thread (not on the worker thread)
     * after the <code>construct</code> task has returned.
     */

    public abstract void finished();


    /***********************************************************************************************
     * Return the value created by the <code>construct</code> task.
     * Returns null if either the constructing thread or the current
     * thread was interrupted before a value was produced.
     *
     * @return the value created by the <code>construct</code> task
     */

    public Object get()
        {
        while (true)
            {
            final Thread workerThread;

            workerThread = threadVar.get();

            // Has the Thread run to completion and produced a result?
            if (workerThread == null)
                {
                System.out.println("SW2 get() returning result" + showName());
                return getValue();
                }
            try
                {
                // Waits for this Thread to die
                System.out.println("SW2 WAITING for thread to die" + showName());
                workerThread.join();
                }

            catch (InterruptedException e)
                {
                Thread.currentThread().interrupt(); // propagate
                return null;
                }
            }
        }


    /***********************************************************************************************
     * Get the value produced by the worker thread,
     * or null if it hasn't been constructed yet.
     *
     * @return Object
     */

    private synchronized Object getValue()
        {
        return(this.value);
        }


    /***********************************************************************************************
     * Set the value produced by worker thread.
     *
     * @param object
     */

    private synchronized void setValue(final Object object)
        {
        this.value = object;
        }


    /***********************************************************************************************
     * Start the worker thread.
     * Causes this thread to begin execution;
     * the Java Virtual Machine calls the run method of this thread.
     */

    public void start()
        {
        final Thread workerThread;

        workerThread = threadVar.get();

        if (workerThread != null)
            {
            workerThread.start();
            System.out.println("SW2 starting" + showName());
            }
        }


    /***********************************************************************************************
     * Stop this thread gracefully.
     * If the thread is already stopped, does nothing.
     * For this to work, this.run must exit by checking stopping() at
     * convenient intervals and returning if it is true.
     *
     * @param interrupt true if this thread should be interrupted from sleep or
     *                  from doing an i/o (which might close the channel),
     *                  before stopping it.
     * @param timeout   How long in milliseconds to wait for the thread to die
     *                  before giving up. 0 means wait forever. -1 means don't
     *                  wait at all.
     */

    public void controlledStop(final boolean interrupt,
                               final long timeout )
        {
        final Thread workerThread;

        System.out.println("SW2 BEGIN CONTROLLED STOP" + showName());
        workerThread = threadVar.get();

        if (workerThread != null)
            {
            if (!workerThread.isAlive())
                {
                System.out.println("SW2 DEAD THREAD " + showName());
                return;
                }
            }
        else
            {
            System.out.println("SW2 NULL THREAD");
            return;
            }


        // Ask live Threads to stop
        synchronized (this)
            {
            // Ask this thread to stop
            System.out.println("SW2 Ask this thread to stop" + showName());
            this.boolRequestStop = true;
            }

        // Wake this thread up if it is sleeping,
        // and get it to notice the stopping() flag.
        // Will also interrupt i/o.

        if (interrupt)
            {
            System.out.println("SW2 DO INTERRUPT" + showName());
            workerThread.interrupt();
            }

        // 0 means wait forever
        if (timeout >= 0)
            {
            try
                {
                // Wait for the thread to die
                System.out.println("SW2 DO JOIN [timeout=" + timeout
                    + "] [stopping="
                    + boolRequestStop + "]" + showName());
                workerThread.join(timeout);
                System.out.println("SW2 Resumed...");
                }

            catch (InterruptedException e)
                {
                // Do nothing...
                System.out.println("SW2 InterruptedException" + showName());
                }
            }
        System.out.println("SW2 END CONTROLLED STOP" + showName());
        }


    /***********************************************************************************************
     * Get the name of the underlying Thread.
     *
     * @return String
     */

    public String getName()
        {
        if ((threadVar != null)
            && (threadVar.get() != null))
            {
            return (threadVar.get().getName());
            }
        else
            {
            return ("NullThread");
            }

        }

    /***********************************************************************************************
     * Destroy this SwingWorker.
     */

    public void destroy()
        {
        System.out.println("SW2 DESTROY" + showName());
        threadVar.clear();

        }

    /***********************************************************************************************
     * Cause the WorkerThread to sleep for milliseconds.
     *
     * @param millis
     */

//    public void sleep(final long millis)
//        {
//        final Thread workerThread;
//
//        workerThread = threadVar.get();
//
//        if (workerThread != null)
//            {
//            try
//                {
//                Thread.sleep(millis);
//                }
//
//            catch (InterruptedException e)
//                {
//
//                }
//            }
//        }


    /***********************************************************************************************
     * Indicate if this Thread has been asked to stop.
     *
     * @return boolean
     */

    public synchronized final boolean isStopping()
        {
        return(this.boolRequestStop);
        }


    /***********************************************************************************************
     *
     * @return
     */

    private String showName()
        {
        if ((threadVar != null)
            && (threadVar.get() != null))
            {
            return (" [thread=" + threadVar.get().getName() + "]");
            }
        else
            {
            return (" [thread=null]");
            }
        }
    }
