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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.dao;

import org.apache.xmlbeans.XmlObject;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.AbstractObservatoryInstrumentDAO;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.CommandPoolList;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.commands.capture.*;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.commands.configuration.GetPluginManifest;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.commands.configuration.ValidateConfigurationXML;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.commands.configuration.WriteConfigurationXML;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.commands.interfaces.GetStaribusAddress;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.commands.interfaces.SetStaribusAddress;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.DAOHelper;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageInterface;
import org.lmn.fc.model.xmlbeans.instruments.CommandType;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;

import javax.swing.*;
import java.io.File;
import java.io.RandomAccessFile;


/***************************************************************************************************
 * StaribusCoreDAO.
 */

public class StaribusCoreDAO extends AbstractObservatoryInstrumentDAO
                             implements ObservatoryInstrumentDAOInterface,
                                        StaribusCoreHostMemoryInterface
    {
    public static final int DAO_CHANNEL_COUNT = 7;

    // HostMemory and Logger state
    private File fileHostMemory;
    private RandomAccessFile randomAccessFile;
    private int intSampleRate;
    private boolean boolCaptureMode;
    private Timer timerCapture;


    /***********************************************************************************************
     * Build the CommandPool using method names in this DAO.
     *
     * @param pool
     */

    private static void addStaribusCoreToCommandPool(final CommandPoolList pool)
        {
        // This really only makes sense for Staribus and Starinet for now
        // The are individually defined in Logger, Magnetometer and VlfRx DAOs,
        // because they have different Metadata for the Charts
        pool.add("captureRawDataRealtime");
        pool.add("publishChartRealtime");
        pool.add("publishChartRealtimeDay");

        // These are generic
        pool.add("getData");
        pool.add("getTestData");
        pool.add("getStaribusAddress");
        pool.add("setStaribusAddress");
        pool.add("getPluginManifest");
        pool.add("validateConfigurationXML");
        pool.add("writeConfigurationXML");
        pool.add("getStatus");

        // These are for the HostMemory Logging facility
        pool.add("getSpace");
        pool.add("getDataBlockCount");
        pool.add("getDataBlock");
        pool.add("getRate");
        pool.add("setRate");
        pool.add("capture");
        }


    /***********************************************************************************************
     * Construct a StaribusCoreDAO.
     *
     * @param hostinstrument
     */

    public StaribusCoreDAO(final ObservatoryInstrumentInterface hostinstrument)
        {
        super(hostinstrument);

        addStaribusCoreToCommandPool(getCommandPool());

        // HostMemory and Logger state
        this.fileHostMemory = null;
        this.randomAccessFile = null;
        this.intSampleRate = 1;
        this.boolCaptureMode = false;
        this.timerCapture = null;
        }


    /***********************************************************************************************
     * Initialise the DAO.
     *
     * @param resourcekey
     */

    public boolean initialiseDAO(final String resourcekey)
        {
        final String SOURCE = "StaribusCoreDAO.initialiseDAO() ";
        final boolean boolSuccess;

        //LOGGER.logTimedEvent(SOURCE + "[resourcekey=" + resourcekey + "]");

        boolSuccess = super.initialiseDAO(resourcekey);

        // This DAO is the superclass of all Staribus devices, and has its own common Resource Bundle
        // This is added after the CommonBundle, and before any subclass
        DAOHelper.loadCommonSuperclassResourceBundle(this);

        // Stop any existing Capture Timer
        if (getCaptureTimer() != null)
            {
            getCaptureTimer().stop();
            setCaptureTimer(null);

            LOGGER.debugStarinetEvent(LOADER_PROPERTIES.isStarinetDebug(),
                                      SOURCE + "DAO Capture Timer DISPOSED");
            }

        setCaptureMode(false);

        return (boolSuccess);
        }


    /***********************************************************************************************
     * Shut down the DAO and dispose of all Resources.
     */

    public void disposeDAO()
        {
        final String SOURCE = "StaribusCoreDAO.disposeDAO() ";

        // Stop any Capture Timer
        if (getCaptureTimer() != null)
            {
            getCaptureTimer().stop();
            setCaptureTimer(null);

            LOGGER.debugStarinetEvent(LOADER_PROPERTIES.isStarinetDebug(),
                                      SOURCE + "DAO Capture Timer DISPOSED");
            }

        setCaptureMode(false);
        Capture.closeDAOHostMemoryFile(getHostInstrument(), this);

        super.disposeDAO();
        }


    /**********************************************************************************************/
    /* Messaging                                                                                  */
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
        return (new StaribusCommandMessage(dao,
                                           instrumentxml,
                                           module,
                                           command,
                                           starscript.trim()));
        }


    /***********************************************************************************************
     * Construct a ResponseMessage appropriate to this DAO.
     *
     * @param portname
     * @param instrumentxml
     * @param module
     * @param command
     * @param starscript
     * @param responsestatusbits
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface constructResponseMessage(final String portname,
                                                             final Instrument instrumentxml,
                                                             final XmlObject module,
                                                             final CommandType command,
                                                             final String starscript,
                                                             final int responsestatusbits)
        {
        return (new StaribusResponseMessage(portname,
                                            instrumentxml,
                                            module,
                                            command,
                                            starscript.trim(),
                                            responsestatusbits));
        }


    /**********************************************************************************************/
    /* DAO Local Commands                                                                         */
    /***********************************************************************************************
     * getData().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

     public ResponseMessageInterface getData(final CommandMessageInterface commandmessage)
         {
         final String SOURCE = "StaribusCoreDAO.getData() ";

         return (GetData.doGetData(this, commandmessage));
         }


    /***********************************************************************************************
     * getStaribusAddress().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface getStaribusAddress(final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "StaribusCoreDAO.getStaribusAddress() ";

        return (GetStaribusAddress.doGetStaribusAddress(this, commandmessage));
        }


    /***********************************************************************************************
     * setStaribusAddress().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface setStaribusAddress(final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "StaribusCoreDAO.setStaribusAddress() ";

        return (SetStaribusAddress.doSetStaribusAddress(this, commandmessage));
        }


    /***********************************************************************************************
     * getPluginManifest().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface getPluginManifest(final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "StaribusCoreDAO.getPluginManifest() ";

        return (GetPluginManifest.doGetPluginManifest(this, commandmessage));
        }


    /***********************************************************************************************
     * validateConfigurationXML().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface validateConfigurationXML(final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "StaribusCoreDAO.validateConfigurationXML() ";

        return (ValidateConfigurationXML.doValidateConfigurationXML(this, commandmessage));
        }


    /***********************************************************************************************
     * writeConfigurationXML().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface writeConfigurationXML(final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "StaribusCoreDAO.writeConfigurationXML() ";

        return (WriteConfigurationXML.doWriteConfigurationXML(this, commandmessage));
        }


    /**********************************************************************************************/
    /* HostMemory DataCapture Commands                                                            */
    /***********************************************************************************************
     * getSpace().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface getSpace(final CommandMessageInterface commandmessage)
        {
        return (GetSpace.doGetSpace(this, commandmessage, MAX_FILE_SIZE_BYTES));
        }


    /***********************************************************************************************
     * getDataBlockCount().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface getDataBlockCount(final CommandMessageInterface commandmessage)
        {
        return (GetDataBlockCount.doGetDataBlockCount(this, commandmessage));
        }


    /***********************************************************************************************
     * getDataBlock().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface getDataBlock(final CommandMessageInterface commandmessage)
        {
        return (GetDataBlock.doGetDataBlock(this, commandmessage));
        }


    /***********************************************************************************************
     * getRate().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface getRate(final CommandMessageInterface commandmessage)
        {
        return (GetRate.doGetRate(this, commandmessage));
        }


    /***********************************************************************************************
     * setRate().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface setRate(final CommandMessageInterface commandmessage)
        {
        return (SetRate.doSetRate(this, commandmessage));
        }


    /***********************************************************************************************
     * capture().
     * This is a Local version, to capture to HostMemory.
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface capture(final CommandMessageInterface commandmessage)
        {
        return (Capture.doCaptureToHostMemory(this, commandmessage));
        }


    /**********************************************************************************************/
    /* Utilities                                                                                  */
    /***********************************************************************************************
     * Get the File used to simulate HostMemory.
     *
     * @return File
     */

    public File getHostMemoryFile()
        {
        return (this.fileHostMemory);
        }


    /***********************************************************************************************
     * Set the File used to simulate HostMemory.
     *
     * @param file
     */

    public void setHostMemoryFile(final File file)
        {
        this.fileHostMemory = file;
        }


    /***********************************************************************************************
     * Get the RandomAccessFile used to simulate HostMemory.
     *
     * @return RandomAccessFile
     */

    public RandomAccessFile getRandomAccessFile()
        {
        return (this.randomAccessFile);
        }


    /***********************************************************************************************
     * Set the RandomAccessFile used to simulate HostMemory.
     *
     * @param stream
     */

    public void setRandomAccessFile(final RandomAccessFile stream)
        {
        this.randomAccessFile = stream;
        }


    /***********************************************************************************************
     * Get the Sample Rate.
     *
     * @return int
     */

    public int getSampleRate()
        {
        return (this.intSampleRate);
        }


    /***********************************************************************************************
     * Set the Sample Rate.
     *
     * @param rate
     */

    public void setSampleRate(final int rate)
        {
        this.intSampleRate = rate;
        }


    /***********************************************************************************************
     * Indicate if we are in capture mode.
     *
     * @return boolean
     */

    public boolean isCaptureMode()
        {
        return (this.boolCaptureMode);
        }


    /***********************************************************************************************
     * Indicate if we are in capture mode.
     *
     * @param capture
     */

    public void setCaptureMode(final boolean capture)
        {
        this.boolCaptureMode = capture;
        }


    /***********************************************************************************************
     * Get the Capture Timer.
     *
     * @return Timer
     */

    public Timer getCaptureTimer()
        {
        return (this.timerCapture);
        }


    /***********************************************************************************************
     * Set the Capture Timer.
     *
     * @param timer
     */

    public void setCaptureTimer(final Timer timer)
        {
        this.timerCapture = timer;
        }
    }
