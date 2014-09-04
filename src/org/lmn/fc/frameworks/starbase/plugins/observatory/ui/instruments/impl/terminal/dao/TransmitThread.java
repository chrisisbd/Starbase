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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.terminal.dao;

import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.common.exceptions.FrameworkException;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.tasks.TaskPlugin;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ArrayBlockingQueue;


/***********************************************************************************************
 * The Thread which will take a character from the Queue
 * and send it to the serial port OutputStream.
 */

public final class TransmitThread extends Thread
                                  implements FrameworkStrings
    {
    private final TaskPlugin pluginTask;
    private final ArrayBlockingQueue<Byte> inputQueue;
    private final OutputStream outputStream;
    private boolean boolRunning;


    /*******************************************************************************************
     * Construct the TransmitThread.
     *
     * @param task
     * @param input
     * @param output
     */

    public TransmitThread(final TaskPlugin task,
                          final ArrayBlockingQueue<Byte> input,
                          final OutputStream output)
        {
        if ((task == null)
            || (!task.validatePlugin())
            || (input == null)
            || (output == null))
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_INVALID);
            }

        pluginTask = task;
        inputQueue = input;
        outputStream = output;
        boolRunning = false;
        }


    /***********************************************************************************************
     * Run the Thread which will take a character from the InputQueue
     * and send it to the OutputStream.
     */

    public final void run()
        {
        try
            {
            while ((boolRunning)
                && (!isInterrupted())
                && (inputQueue != null)
                && (outputStream != null))
                {
                // We know that the Queue contains only Bytes...
                // Retrieves and removes the head of this queue,
                // **waiting** if necessary until an element becomes available.
                outputStream.write(inputQueue.take());
                }
            }

        catch (IOException exception)
            {
            getTask().handleException(exception,
                                      "TransmitThread.run()",
                                      EventStatus.WARNING);
            }

        catch (InterruptedException exception)
            {
            // InterruptedException is not an error in this case
            }
        }


    /***********************************************************************************************
     * Set a flag to control this Thread.
     *
     * @param state
     */

    public void setRunning(final boolean state)
        {
        this.boolRunning = state;
        }


    /**********************************************************************************************
     * Get the host Task.
     *
     * @return TaskData
     */

    private TaskPlugin getTask()
        {
        return (this.pluginTask);
        }
    }
