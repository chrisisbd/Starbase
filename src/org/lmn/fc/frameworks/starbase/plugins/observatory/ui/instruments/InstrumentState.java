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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments;

/***************************************************************************************************
 * InstrumentState.
 * See: http://en.wikipedia.org/wiki/Finite-state_machine.
 */

public enum InstrumentState
    {
    CREATED         (0,   true, false, "Created",      "Created"),    // AbstractObservatoryInstrument constructor
                                                                      // , FSM startup

    INITIALISED     (1,   true, false, "Initialised",  "Off"),        // AbstractObservatoryInstrument.initialise()

    READY           (2,  false,  true, "Ready",        "Ready"),      // AbstractObservatoryInstrument.start()
                                                                      // AbstractObservatoryInstrumentDAO.start()
                                                                      // AbstractObservatoryInstrumentDAO.abortCommand()
                                                                      // ExecuteCommandHelper.executeCommand() [2]
                                                                      // CommandProcessorUtilities.createAbortButton()

    BUSY            (3,  false,  true, "Busy",         "Busy"),       // ExecuteCommandHelper.executeCommand()
                                                                      // DAOHelper.executeCommandOnSameThread()

    REPEATING       (4,  false,  true, "Repeating",    "Repeating"),  // ExecuteCommandHelper.executeCommand()

    STOPPED         (5,   true, false, "Stopped",      "Off"),        // AbstractObservatoryInstrument.stop()
                                                                      // AbstractObservatoryInstrumentDAO.stop()

    RECORD_MACRO    (6,  false,  true, "RecordMacro",  "Recording a new Macro"),
    EDIT_MACRO      (7,  false,  true, "EditMacro",    "Editing selected Macro"),
    DELETE_MACRO    (8,  false,  true, "DeleteMacro",  "Deleting selected Macro"),
    LOAD_MACROS     (9,  false,  true, "LoadMacro",    "Loading Instrument Macros"),
    SAVE_MACROS     (10, false,  true, "SaveMacro",    "Saving Instrument Macros"),
    SHOW_MACRO      (11, false,  true, "ShowMacro",    "Displaying selected Macro"),

    DISPOSED        (12, true,  false, "Disposed",     "Disposed"),   // AbstractObservatoryInstrument.dispose()
    ERROR           (13, false, false, "Error",        "Error");


    private final int intValue;
    private final boolean boolIsOff;
    private final boolean boolIsOn;
    private final String strName;
    private final String strStatus;


    /***********************************************************************************************
     * Indicate if the Instrument is currently Off.
     *
     * @param instrument
     *
     * @return boolean
     */

    public static boolean isOff(final ObservatoryInstrumentInterface instrument)
        {
        return ((instrument != null)
                && (instrument.getInstrumentState().isOff()));
        }


    /***********************************************************************************************
     * Indicate if the Instrument is currently On.
     *
     * @param instrument
     *
     * @return boolean
     */

    public static boolean isOn(final ObservatoryInstrumentInterface instrument)
        {
        return ((instrument != null)
                && (instrument.getInstrumentState().isOn()));
        }


    /***********************************************************************************************
     * Indicate if the Instrument is currently READY,
     * i.e can be controlled with a change of Command.
     *
     * @param instrument
     *
     * @return boolean
     */

    public static boolean isReady(final ObservatoryInstrumentInterface instrument)
        {
        return ((instrument != null)
                && (READY.equals(instrument.getInstrumentState())));
        }


    /***********************************************************************************************
     * A convenience method to indicate if the Instrument is 'doing something',
     * i.e. not stopped executing a Command, or waiting.
     *
     * @param instrument
     *
     * @return boolean
     */

    public static boolean isDoingSomething(final ObservatoryInstrumentInterface instrument)
        {
        return ((instrument != null)
                && ((READY.equals(instrument.getInstrumentState()))
                    || (BUSY.equals(instrument.getInstrumentState()))
                    || (REPEATING.equals(instrument.getInstrumentState()))
                    || (RECORD_MACRO.equals(instrument.getInstrumentState()))
                    || (EDIT_MACRO.equals(instrument.getInstrumentState()))
                    || (DELETE_MACRO.equals(instrument.getInstrumentState()))
                    || (LOAD_MACROS.equals(instrument.getInstrumentState()))
                    || (SAVE_MACROS.equals(instrument.getInstrumentState()))
                    || (SHOW_MACRO.equals(instrument.getInstrumentState()))));
        }


    /***********************************************************************************************
     * A convenience method to indicate if the Instrument is 'occupied' executing a Command.
     * This is to let the User know they must try again later.
     *
     * @param instrument
     *
     * @return boolean
     */

    public static boolean isOccupied(final ObservatoryInstrumentInterface instrument)
        {
        return ((instrument != null)
                && ((BUSY.equals(instrument.getInstrumentState()))
                    || (REPEATING.equals(instrument.getInstrumentState()))));
        }


    /***********************************************************************************************
     * InstrumentState.
     *
     * @param value
     * @param isoff
     * @param ison
     * @param name
     * @param status
     */

    private InstrumentState(final int value,
                            final boolean isoff,
                            final boolean ison,
                            final String name,
                            final String status)
        {
        intValue = value;
        boolIsOff = isoff;
        boolIsOn  = ison;
        strName = name;
        strStatus = status;
        }


    /***********************************************************************************************
     * Get the TypeID.
     *
     * @return int
     */

    public int getTypeID()
        {
        return (this.intValue);
        }


    /***********************************************************************************************
     * Get the state name.
     *
     * @return String
     */

    public String getName()
        {
        return (this.strName);
        }


    /***********************************************************************************************
     * Indicate if the Instrument is Off.
     *
     * @return boolean
     */

    public boolean isOff()
        {
        return (this.boolIsOff);
        }


    /***********************************************************************************************
     * Indicate if the Instrument is On.
     *
     * @return boolean
     */

    public boolean isOn()
        {
        return (this.boolIsOn);
        }


    /***********************************************************************************************
     * Get the status text.
     *
     * @return String
     */

    public String getStatus()
        {
        return (this.strStatus);
        }


    /***********************************************************************************************
     * Get the state name.
     *
     * @return
     */

    public String toString()
        {
        return (this.strName);
        }
    }
