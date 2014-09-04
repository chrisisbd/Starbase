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

//--------------------------------------------------------------------------------------------------
// Revision History
//
//  13-01-05    LMN created file
//  26-01-05    LMN formalising SerialPortData
//  04-04-05    LMN tidying logging
//  09-08-06    LMN changing for the new structure
//
//--------------------------------------------------------------------------------------------------

package org.lmn.fc.frameworks.starbase.tasks;

import org.lmn.fc.common.utilities.terminal.SerialPortData;
import org.lmn.fc.frameworks.starbase.ui.emulator.TerminalEmulatorUI;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.model.datatypes.types.ColourDataType;
import org.lmn.fc.model.datatypes.types.FontDataType;
import org.lmn.fc.model.tasks.impl.TaskData;
import org.lmn.fc.ui.UIComponentPlugin;

import java.awt.*;


/***************************************************************************************************
 * The Framework TerminalEmulator.
 */

public final class TerminalEmulator extends TaskData
    {
    // String Resources
    private static final String STATUS_TERMINAL = "Terminal Emulator";
    private static final String NO_PARITY = "NP";

    // Resources
    private FontInterface pluginFont;                // The Font of the display text
    private ColourInterface pluginColour;            // The colour of the display text
    private ColourInterface colourTable;             // The colour of the table on which the text is drawn
    private ColourInterface colourCanvas;            // The colour of the display canvas
    private final SerialPortData serialportData;  // The configuration of the serial port


    /***********************************************************************************************
     * Construct a TerminalEmulator.
     */

    private TerminalEmulator()
        {
        super(8781128148715011321L);

        // Initialise the properties with some defaults
        pluginFont = new FontDataType(UIComponentPlugin.DEFAULT_FONT_SPEC);
        pluginColour = new ColourDataType(Color.BLACK);
        colourTable = new ColourDataType(Color.BLACK);
        colourCanvas = new ColourDataType(Color.WHITE);
        serialportData = new SerialPortData();
        setDebugMode(false);
        }


    /***********************************************************************************************
     * Initialise the TerminalEmulator.
     *
     * @return boolean Flag indicating success or failure
     */

    public final boolean initialiseTask()
        {
        // Get the latest Resources
        readResources();

        // Clear the ContextActionGroups for the Task
        clearUserObjectContextActionGroups();

        // Create a TerminalEmulatorInstrumentPanel and initialise it
        setUIComponent(new TerminalEmulatorUI(this,
                                              pluginFont,
                                              pluginColour,
                                              colourTable,
                                              colourCanvas,
                                              serialportData));
        getUIComponent().setDebug(getDebugMode());
        getUIComponent().initialiseUI();

        // There is no Editor, and we are always in Browse mode
        setEditorComponent(null);
        setBrowseMode(true);

        return (true);
        }


    /***********************************************************************************************
     * Run the TerminalEmulator.
     *
     * @return boolean Flag indicating success or failure
     */

    public final boolean runTask()
        {
        // Get the latest Resources
        readResources();

        if (getUIComponent() != null)
            {
            // Begin to capture data!
            ((TerminalEmulatorUI)getUIComponent()).startSession();
            }

        return (true);
        }


    /***********************************************************************************************
     * Put the TerminalEmulator in Idle.
     *
     * @return boolean Flag indicating success or failure
     */

    public final boolean idleTask()
        {
        if (getUIComponent() != null)
            {
            // Stop the session
            ((TerminalEmulatorUI)getUIComponent()).stopSession();

            // Stop all UIComponents
            stopUI();
            }

        return (true);
        }


    /***********************************************************************************************
     * Shutdown the TerminalEmulator.
     *
     * @return boolean Flag indicating success or failure
     */

    public final boolean shutdownTask()
        {
        if (getUIComponent() != null)
            {
            // Stop the session
            ((TerminalEmulatorUI)getUIComponent()).stopSession();

            // Stop all UIComponents
            stopUI();

            // Remove the TerminalEmulatorInstrumentPanel
            getUIComponent().disposeUI();
            setUIComponent(null);
            }

        // Clear the ContextActionGroups for the TerminalEmulator Task
        clearUserObjectContextActionGroups();

        return (true);
        }


    /***********************************************************************************************
     * Run the UI of this UserObjectPlugin.
     */

    public final void runUI()
        {
        if (getUIComponent() != null)
            {
            getUIComponent().runUI();
            }

        setCaption(getPathname());
        setStatus(REGISTRY.getFramework().getPathname()
                      + SPACE
                      + STATUS_TERMINAL
                      + SPACE
                      + getPortConfiguration());
        }


    /***********************************************************************************************
     * Stop the UI of this UserObjectPlugin.
     */

    public final void stopUI()
        {
        if (getUIComponent() != null)
            {
            // Reduce resources as far as possible
            getUIComponent().stopUI();
            }
        }


    /***********************************************************************************************
     * Get the Port Configuration as (COM1 9600, 8, 1, NP).
     *
     * @return String
     */

    private String getPortConfiguration()
        {
        final String strParity;
        final String strPortConfiguration;

        if (serialportData != null)
            {
            if (serialportData.getParity() == 0)
                {
                strParity = NO_PARITY;
                }
            else
                {
                strParity = Integer.toString(serialportData.getParity());
                }

            strPortConfiguration = LEFT_PARENTHESIS
                                    + serialportData.getPortName()
                                    + SPACE
                                    + serialportData.getBaudrate()
                                    + COMMA + SPACE
                                    + serialportData.getDatabits()
                                    + COMMA + SPACE
                                    + serialportData.getStopbits()
                                    + COMMA + SPACE
                                    + strParity
                                    + RIGHT_PARENTHESIS;
            }
        else
            {
            strPortConfiguration = EMPTY_STRING;
            }

        return (strPortConfiguration);
        }


    /***********************************************************************************************
     * Read all the Resources required by the TerminalEmulator.
     */

    public final void readResources()
        {
        // getResourceKey() returns '<Framework>.TerminalEmulator.'
        setDebugMode(REGISTRY.getBooleanProperty(getResourceKey() + KEY_ENABLE_DEBUG));

        pluginFont = (FontInterface)REGISTRY.getProperty(getResourceKey() + KEY_FONT);
        pluginColour = (ColourInterface)REGISTRY.getProperty(getResourceKey() + KEY_COLOUR_TEXT);
        colourTable = (ColourInterface) REGISTRY.getProperty(getResourceKey() + KEY_COLOUR_TABLE);
        colourCanvas = (ColourInterface)REGISTRY.getProperty(getResourceKey() + KEY_COLOUR_CANVAS);

        if (serialportData != null)
            {
            serialportData.setPortOwner(REGISTRY.getStringProperty(getResourceKey() + KEY_PORT_OWNER));
            serialportData.setPortName(REGISTRY.getStringProperty(getResourceKey() + KEY_PORT_NAME));
            serialportData.setBaudrate(REGISTRY.getIntegerProperty(getResourceKey() + KEY_PORT_BAUDRATE));
            serialportData.setDatabits(REGISTRY.getIntegerProperty(getResourceKey() + KEY_PORT_DATA_BITS));
            serialportData.setStopbits(REGISTRY.getIntegerProperty(getResourceKey() + KEY_PORT_STOP_BITS));
            serialportData.setParity(REGISTRY.getIntegerProperty(getResourceKey() + KEY_PORT_PARITY));
            serialportData.setFlowControl(REGISTRY.getStringProperty(getResourceKey() + KEY_PORT_FLOW_CONTROL));
            serialportData.setBufferSize(REGISTRY.getIntegerProperty(getResourceKey() + KEY_BUFFER_SIZE));

            // Todo local echo
            }

        // Strings
        //ACTION_CLEAR = REGISTRY.getString(getResourceKey() + "Action.ClearScreen");
        }
    }


//--------------------------------------------------------------------------------------------------
// End of File
