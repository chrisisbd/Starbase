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

package org.lmn.fc.common.utilities.streams;

import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.common.utilities.time.Chronos;

import javax.swing.*;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;


/**
 * Class Console creates a Java Console for GUI based Java Applications. Once
 * created, a Console component receives all the data directed to the standard
 * output (System.out) and error (System.err) streams.
 * <p>
 * For example, once a Java Console is created for an application, data passed
 * on to any methods of System.out (e.g., System.out.println(SPACE)) and
 * System.err (e.g., stack trace in case of uncought exceptions) will be
 * received by the Console.
 *
 * @author Subrahmanyam Allamaraju (sallamar@cvimail.cv.com)
 */

public final class Console extends JPanel
                           implements StreamObserver,
                                      FrameworkConstants,
                                      FrameworkStrings
    {
    private static final int BUFFER_SIZE = 50000;

    private JTextArea textArea;
    private int intBufferSize;

    private ObservableOutputStream errorDevice;
    private ObservableOutputStream outputDevice;

    private ByteArrayOutputStream arrayErrorDevice;
    private ByteArrayOutputStream arrayOutputDevice;

    private PrintStream streamConsoleError;
    private PrintStream streamConsoleOutput;

    private PrintStream streamDefaultError;
    private PrintStream streamDefaultOutput;

    private boolean boolOutputInstalled;
    private boolean boolErrorInstalled;
    private boolean boolDebugMode;                    // Controls debug messages


    /***********************************************************************************************
     * Creates a Java Console.
     *
     */

    public Console()
        {
        // Create the JPanel which is the Console plug-in
        super(new BorderLayout());

        // The text area for the output
        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setDoubleBuffered(true);

        // Set a default for the text buffer size
        intBufferSize = BUFFER_SIZE;

        // Allow the user to scroll back into the buffer
        JScrollPane scrollPane = new JScrollPane(textArea);
        JScrollBar scrollBar = scrollPane.getVerticalScrollBar();
        // Todo strings
        scrollBar.setToolTipText("Warning! If you scroll back, this window will no longer autoscroll forward");
        add(scrollPane);

        // Save the default streams
        streamDefaultError = System.err;
        streamDefaultOutput = System.out;

        // Create new output devices
        arrayOutputDevice = new ByteArrayOutputStream();
        arrayErrorDevice = new ByteArrayOutputStream();

        boolOutputInstalled = false;
        boolErrorInstalled = false;
        boolDebugMode= false;
        }


    /***********************************************************************************************
     *
     * @param font
     */

    public final void setTextFont(Font font)
        {
        textArea.setFont(font);
        }


    /***********************************************************************************************
     *
     * @param color
     */

    public final void setPanelColour(Color color)
        {
        textArea.setBackground(color);
        }


    /***********************************************************************************************
     *
     * @param color
     */

    public final void setTextColour(Color color)
        {
        textArea.setForeground(color);
        }


    /***********************************************************************************************
     * Clears the Console.
     *
     */

    public final void clearConsole()
        {
        if (boolOutputInstalled)
            {
            try
                {
                outputDevice.writeTo(arrayOutputDevice);
                }
            catch (IOException e)
                {
                }
            outputDevice.reset();
            }

        if (boolErrorInstalled)
            {
            try
                {
                errorDevice.writeTo(arrayErrorDevice);
                }
            catch (IOException e)
                {
                }
            errorDevice.reset();

            textArea.setText("");
            }
        }


    /***********************************************************************************************
     * Sets the error device to the Console if not set already.
     *
     * @see #resetErrorDevice
     */

    public final void setErrorDevice()
        {
        // Create a new Error stream
        errorDevice = new ObservableOutputStream(ObservableOutputStream.STREAM_SYSTEM_ERR);
        errorDevice.addStreamObserver(this);

        // The output stream to which values and objects will be printed
        // Set true to flush on writes
        streamConsoleError = new PrintStream(errorDevice, true);

        // Make System use this stream
        System.setErr(streamConsoleError);
        boolErrorInstalled = true;
        }


    /***********************************************************************************************
     * Resets the error device to the default. Console will no longer receive
     * data directed to the error stream.
     *
     * @see #setErrorDevice
     */

    public final void resetErrorDevice()
        {
        System.setErr(streamDefaultError);
        boolErrorInstalled = false;
        }


    /***********************************************************************************************
     * Sets the output device to the Console if not set already.
     *
     * @see #resetOutputDevice
     */

    public final void setOutputDevice()
        {
        outputDevice = new ObservableOutputStream(ObservableOutputStream.STREAM_SYSTEM_OUT);
        outputDevice.addStreamObserver(this);

        streamConsoleOutput = new PrintStream(outputDevice, true);

        System.setOut(streamConsoleOutput);
        boolOutputInstalled = true;
        }


    /***********************************************************************************************
     * Resets the output device to the default. Console will no longer receive
     * data directed to the output stream.
     *
     * @see #setOutputDevice
     */

    public final void resetOutputDevice()
        {
        System.setOut(streamDefaultOutput);
        boolOutputInstalled = false;
        }


    /***********************************************************************************************
     *
     * @return
     */

    public final int getBufferSize()
        {
        return (intBufferSize);
        }


    /***********************************************************************************************
     *
     * @param size
     */

    public final void setBufferSize(int size)
        {
        if ((size > 100)
            && (size < 100000))
            {
            intBufferSize = size;
            }
        else
            {
            intBufferSize = BUFFER_SIZE;
            }
        }


    /***********************************************************************************************
     *
     */

    public final void streamChanged()
        {
        int intLength;

        textArea.append(outputDevice.toString());

        // Check buffer size
        intLength = textArea.getText().length();

        try
            {
            outputDevice.writeTo(arrayOutputDevice);
            }

        catch (IOException e)
            {
            }
        outputDevice.reset();

        streamConsoleError.checkError();

        textArea.append(errorDevice.toString());

        // Check buffer size
        intLength = textArea.getText().length();

        if (intLength > getBufferSize())
            {
            textArea.setText(textArea.getText().substring(intLength-getBufferSize()));
            }

        try
            {
            errorDevice.writeTo(arrayErrorDevice);
            }

        catch (IOException e)
            {
            }
        errorDevice.reset();

        // Move the cursor to the end of the entries
//        textArea.setCaretPosition(textArea.getText().length());
//        textArea.moveCaretPosition(textArea.getText().length());
//        Caret caretConsole = textArea.getCaret();
//        caretConsole.setDot(textArea.getText().length()-2);
        }


    /***********************************************************************************************
     * Returns contents of the error device directed to it so far. Calling
     * <a href="#clearConsole">clearConsole</a> has no effect on the return data of this method.
     */

    public ByteArrayOutputStream getErrorContent() throws IOException
        {
        ByteArrayOutputStream newStream = new ByteArrayOutputStream();
        arrayErrorDevice.writeTo(newStream);

        return newStream;
        }


    /***********************************************************************************************
     * Returns contents of the output device directed to it so far. Calling
     * <a href="#clearConsole">clearConsole</a> has no effect on the return data of this method.
     */

    public ByteArrayOutputStream getOutputContent() throws IOException
        {
        ByteArrayOutputStream newStream = new ByteArrayOutputStream();
        arrayOutputDevice.writeTo(newStream);

        return newStream;
        }


    /***********************************************************************************************
     * Get the Debug Mode flag.
     * @return
     */

    public final boolean getDebugMode()
        {
        return (this.boolDebugMode);
        }


    /***********************************************************************************************
     * Set the Debug Mode flag.
     *
     * @param flag
     */

    public final void setDebugMode(boolean flag)
        {
        this.boolDebugMode = flag;
        }


    /***********************************************************************************************
     * Show a debug message.
     *
     * @param message
     */

    private final void showDebugMessage(final String message)
        {
        final String strSeparator;

        if (getDebugMode())
            {
            if (message.startsWith("."))
                {
                strSeparator = "";
                }
            else
                {
                strSeparator = SPACE;
                }

            System.out.println(Chronos.timeNow()
                               + SPACE
                               + this.getClass().getName()
                               + strSeparator
                               + message);
            }
        }
    }



