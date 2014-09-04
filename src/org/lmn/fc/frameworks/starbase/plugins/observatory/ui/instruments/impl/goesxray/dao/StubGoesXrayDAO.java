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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.goesxray.dao;

import org.apache.xmlbeans.XmlObject;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.AbstractObservatoryInstrumentDAO;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.CommandPoolList;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.DAOHelper;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageInterface;
import org.lmn.fc.model.xmlbeans.instruments.CommandType;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;

/***************************************************************************************************
 * SAMPLE DATA from http://sec.noaa.gov/
 *
 * e.g. http://sec.noaa.gov/ftpdir/lists/xray/20070225_G12xr_5m.txt Positive values only
 *                                            20070227_G12xr_5m.txt Has negative values
 *
    :Data_list: 20070208_G12xr_5m.txt
    :Created: 2007 Feb 09 0015 UTC
    # Prepared by the U.S. Dept. of Commerce, NOAA, Space Environment Center.
    # Please send comments and suggestions to SEC.Webmaster@noaa.gov
    #
    # Label: Short = 0.05- 0.4 nanometer
    # Label: Long  = 0.1 - 0.8 nanometer
    # Units: Short = Watts per meter squared
    # Units: Long  = Watts per meter squared
    # Source: GOES-12
    # Location: W075
    # Missing data: -1.00e+05
    #
    #                         GOES-12 Solar X-ray Flux
    #
    #                 Modified Seconds
    # UTC Date  Time   Julian  of the
    # YR MO DA  HHMM    Day     Day       Short       Long        Ratio
    #-------------------------------------------------------------------
    2007 02 08  0000   54139      0     1.98e-09    2.48e-08    7.96e-02
    2007 02 08  0005   54139    300     1.97e-09    2.60e-08    7.57e-02
    2007 02 08  0010   54139    600     1.97e-09    2.76e-08    7.13e-02
    2007 02 08  0015   54139    900     1.96e-09    2.64e-08    7.44e-02
    2007 02 08  0020   54139   1200     1.98e-09    2.52e-08    7.87e-02

***************************************************************************************************/

/***************************************************************************************************
 * StubGoesXrayDAO.
 * Delivers data from a sample local file.
 */

public class StubGoesXrayDAO extends AbstractObservatoryInstrumentDAO
                             implements ObservatoryInstrumentDAOInterface
    {
    public static final String STUB_DATA = "20070227_GOES_STUB.txt";


    /***********************************************************************************************
     * Build the CommandPool using method names in this DAO.
     *
     * @param pool
     */

    private static void addSubclassToCommandPool(final CommandPoolList pool)
        {
        }


    /***********************************************************************************************
     * Construct a StubGoesXrayDAO.
     *
     * @param hostinstrument
     */

    public StubGoesXrayDAO(final ObservatoryInstrumentInterface hostinstrument)
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
        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               "StubGoesXrayDAO.initialiseDAO() [resourcekey=" + resourcekey + "]");

        super.initialiseDAO(resourcekey);

        DAOHelper.loadSubClassResourceBundle(this);

        return (true);
        }


    /***********************************************************************************************
     * Shut down the DAO and dispose of all Resources.
     */

    public void disposeDAO()
        {
        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               "StubGoesXrayDAO.disposeDAO()");

        super.disposeDAO();
        }


    /***********************************************************************************************
     * Construct a CommandMessage appropriate to this DAO.
     *
     * @param dao
     * @param instrumentxml
     * @param module
     * @param command
     * @param starscript
     *
     * @return CommandMessageInterface
     */

    public CommandMessageInterface constructCommandMessage(final ObservatoryInstrumentDAOInterface dao,
                                                           final Instrument instrumentxml,
                                                           final XmlObject module,
                                                           final CommandType command,
                                                           final String starscript)
        {
        return (new GoesXrayCommandMessage(dao,
                                           instrumentxml,
                                           module,
                                           command,
                                           starscript.trim()));
        }


    /***********************************************************************************************
     * Construct a ResponseMessage appropriate to this DAO.
     *
     *
     * @param portname
     * @param instrumentxml
     * @param module
     * @param command
     * @param starscript
     * @param responsestatusbits
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface constructResponseMessage(final String portname,
                                                             final Instrument instrumentxml,
                                                             final XmlObject module,
                                                             final CommandType command,
                                                             final String starscript,
                                                             final int responsestatusbits)
        {
        return (new GoesXrayResponseMessage(portname,
                                            instrumentxml,
                                            module,
                                            command,
                                            starscript.trim(),
                                            responsestatusbits));
        }


    /***********************************************************************************************
     * Read all the Resources required by the StubGoesXrayDAO.
     */

    public void readResources()
        {
        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               "StubGoesXrayDAO.readResources() [ResourceKey=" + getResourceKey() + "]");

        super.readResources();
        }
    }
