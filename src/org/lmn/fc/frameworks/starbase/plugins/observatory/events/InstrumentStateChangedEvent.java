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

package org.lmn.fc.frameworks.starbase.plugins.observatory.events;

import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.InstrumentState;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;

import java.util.EventObject;


/***********************************************************************************************
 * InstrumentStateChangedEvent.
 */

public final class InstrumentStateChangedEvent extends EventObject
    {
    private final ObservatoryInstrumentInterface instrumentInterface;
    private final InstrumentState currentState;
    private final InstrumentState nextState;
    private final long longRepeatNumber;
    private final String strRepeatText;


    /***********************************************************************************************
     * Construct an InstrumentStateChangedEvent.
     *
     * @param eventsource
     * @param instrument
     * @param currentstate
     * @param nextstate
     * @param repeatnumber
     * @param repeattext
     */

    public InstrumentStateChangedEvent(final Object eventsource,
                                       final ObservatoryInstrumentInterface instrument,
                                       final InstrumentState currentstate,
                                       final InstrumentState nextstate,
                                       final long repeatnumber,
                                       final String repeattext)
        {
        super(eventsource);

        this.instrumentInterface = instrument;
        this.currentState = currentstate;
        this.nextState = nextstate;
        this.longRepeatNumber = repeatnumber;
        this.strRepeatText = repeattext;
        }


    /***********************************************************************************************
     * Get the Instrument to which this Event relates.
     *
     * @return ObservatoryInstrumentInterface
     */

    public ObservatoryInstrumentInterface getInstrument()
        {
        return (this.instrumentInterface);
        }


    /***********************************************************************************************
     * Get the CurrentState of the Instrument.
     *
     * @return InstrumentState
     */

    public InstrumentState getCurrentState()
        {
        return (this.currentState);
        }


    /***********************************************************************************************
     * Get the requested NextState of the Instrument.
     *
     * @return InstrumentState
     */

    public InstrumentState getNextState()
        {
        return (this.nextState);
        }


    /***********************************************************************************************
     * Get the RepeatNumber, i.e. msec or execution counter.
     *
     * @return long
     */

    public long getRepeatNumber()
        {
        return (this.longRepeatNumber);
        }


    /***********************************************************************************************
     * Get the RepeatText.
     *
     * @return String
     */

    public String getRepeatText()
        {
        return (this.strRepeatText);
        }
    }
