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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.staribusmagnetometer.dao;

import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.CommandPoolList;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.commands.publishers.PublishRealtime;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.CaptureCommandHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.DAOHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.StaribusHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.dao.StaribusCoreDAO;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageInterface;


/***************************************************************************************************
 * StaribusMagnetometerDAO.
 *
 * See: http://wdc.kugi.kyoto-u.ac.jp/igrf/gggm/gmexp.html
 *      http://wdc.kugi.kyoto-u.ac.jp/igrf/gggm/index.html
 *      http://www.resurgentsoftware.com/geomag.html
 *      http://www.ngdc.noaa.gov/geomag/magfield.shtml
 */

public final class StaribusMagnetometerDAO extends StaribusCoreDAO
                                           implements ObservatoryInstrumentDAOInterface
    {
    /***********************************************************************************************
     * Build the CommandPool using method names in this DAO.
     *
     * @param pool
     */

    private static void addSubclassToCommandPool(final CommandPoolList pool)
        {
        }


    /***********************************************************************************************
     * Construct a StaribusMagnetometerDAO.
     *
     * @param hostinstrument
     */

    public StaribusMagnetometerDAO(final ObservatoryInstrumentInterface hostinstrument)
        {
        super(hostinstrument);

        addSubclassToCommandPool(getCommandPool());
        }


    /***********************************************************************************************
     * Initialise the DAO.
     *
     * @param resourcekey
     */

    public boolean initialiseDAO(final String resourcekey)
        {
        final String SOURCE = "StaribusMagnetometerDAO.initialiseDAO() ";

        LOGGER.logTimedEvent(SOURCE + "[resourcekey=" + resourcekey + "]");

        super.initialiseDAO(resourcekey);

        DAOHelper.loadSubClassResourceBundle(this);

        return (true);
        }


    /**********************************************************************************************/
    /* DAO Local Commands                                                                         */
    /***********************************************************************************************
     * captureRawDataRealtime().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface captureRawDataRealtime(final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "StaribusMagnetometerDAO.captureRawDataRealtime()";

        // Only generate a ResponseMessage when completed
        return (CaptureCommandHelper.doIteratedStaribusMultichannelDataCaptureCommand(this,
                                                                                      commandmessage,
                                                                                      StaribusHelper.createMultichannelChartLegendMetadata("Staribus Magnetometer Logger",
                                                                                                                                           "Time (UT)",
                                                                                                                                           "Magnetometer Outputs"),
                                                                                      SOURCE,
                                                                                      false));
        }


    /***********************************************************************************************
     * publishChartRealtime().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface publishChartRealtime(final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "StaribusMagnetometerDAO.publishChartRealtime() ";

        return (PublishRealtime.doPublishChartRealtime(this,
                                                       commandmessage,
                                                       StaribusHelper.createMultichannelChartLegendMetadata("Staribus Magnetometer Publisher",
                                                                                                            "Time (UT)",
                                                                                                            "Magnetometer Outputs"),
                                                       SOURCE,
                                                       false));
        }


    /***********************************************************************************************
     * publishChartRealtimeDay().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface publishChartRealtimeDay(final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "StaribusMagnetometerDAO.publishChartRealtimeDay() ";

        return (PublishRealtime.doPublishChartRealtimeDay(this,
                                                          commandmessage,
                                                          StaribusHelper.createMultichannelChartLegendMetadata("Staribus Magnetometer Publisher",
                                                                                                               "Time (UT)",
                                                                                                               "Magnetometer Outputs"),
                                                          SOURCE,
                                                          false));
        }


    /***********************************************************************************************
     *  Read all the Resources required by the DAO.
     *
     * KEY_DAO_ONERROR_CONTINUE
     * KEY_DAO_TIMEOUT_DEFAULT
     */

    public void readResources()
        {
        final String SOURCE = "StaribusMagnetometerDAO.readResources() ";

        //LOGGER.debugTimedEvent("StaribusLoggerDAO.readResources() [ResourceKey=" + getResourceKey() + "]");

        super.readResources();
        }
    }
