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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.clock;


import org.lmn.fc.frameworks.starbase.plugins.observatory.events.ObservatoryClockChangedListener;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryClockInterface;

import java.util.Vector;


/***************************************************************************************************
 * ObservatoryClockDAOInterface.
 * Use this in *conjunction* with ObservatoryInstrumentDAOInterface,
 * to ensure that DAOHelper.instantiateDAO() works correctly.
 */

public interface ObservatoryClockDAOInterface
    {
    /***********************************************************************************************
     * Get the ObservatoryClock being synthesised by this DAO.
     *
     * @return ObservatoryClockInterface
     */

    ObservatoryClockInterface getObservatoryClock();


    /**********************************************************************************************/
    /* Events                                                                                     */
    /***********************************************************************************************
     * Notify all listeners of ObservatoryClockChangedEvents.
     *
     * @param eventsource
     * @param changed
     */

    void notifyObservatoryClockChangedEvent(Object eventsource, boolean changed);


    /***********************************************************************************************
     * Get the ObservatoryClockChanged Listeners (mostly for testing).
     *
     * @return Vector<ObservatoryClockChangedListener>
     */

    Vector<ObservatoryClockChangedListener> getObservatoryClockChangedListeners();


    /***********************************************************************************************
     * Add a listener for this event.
     *
     * @param listener
     */

    void addObservatoryClockChangedListener(ObservatoryClockChangedListener listener);


    /***********************************************************************************************
     * Remove a listener for this event.
     *
     * @param listener
     */

    void removeObservatoryClockChangedListener(ObservatoryClockChangedListener listener);
    }
