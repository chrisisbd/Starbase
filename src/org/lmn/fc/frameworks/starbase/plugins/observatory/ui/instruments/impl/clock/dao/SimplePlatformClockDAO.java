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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.clock.dao;

import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryClockInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.DAOHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.clock.ObservatoryClockDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.clock.ObservatoryClockHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.clock.clocks.SimplePlatformClock;


/***********************************************************************************************
 * SimplePlatformClockDAO.
 * WARNING! This DAO must NOT use getObservatoryClock() to get the time,
 * since this DAO is responsible for providing the source of Time!
 */

public final class SimplePlatformClockDAO extends AbstractClockDAO
                                          implements ObservatoryInstrumentDAOInterface,
                                                     ObservatoryClockDAOInterface
    {
    /***********************************************************************************************
     * Construct a SimplePlatformClockDAO.
     *
     * @param hostinstrument
     */

    public SimplePlatformClockDAO(final ObservatoryInstrumentInterface hostinstrument)
        {
        super(hostinstrument);

        // Create the Clock driven by this DAO
        this.clock = new SimplePlatformClock(this);
        }


    /***********************************************************************************************
     * Initialise the DAO.
     *
     * @param resourcekey
     */

    public boolean initialiseDAO(final String resourcekey)
        {
        final String SOURCE = "SimplePlatformClockDAO.initialiseDAO() ";

        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               SOURCE + "[resourcekey=" + resourcekey + "]");

        super.initialiseDAO(resourcekey);

        DAOHelper.loadSubClassResourceBundle(this);

        // Add some Metadata to the result
        addAllMetadataToContainersTraced(ObservatoryClockHelper.createClockOffsetChannelMetadata(),
                                         SOURCE,
                                         LOADER_PROPERTIES.isMetadataDebug());

        return (true);
        }


    /***********************************************************************************************
     * Get the ObservatoryClock being synthesised by this DAO.
     *
     * @return ObservatoryClockInterface
     */

    public ObservatoryClockInterface getObservatoryClock()
        {
        final String SOURCE = "SimplePlatformClockDAO.getObservatoryClock() ";

        //LOGGER.log(SOURCE + "Getting synthesised SimplePlatformClock ObservatoryClock");

        return (this.clock);
        }
    }
