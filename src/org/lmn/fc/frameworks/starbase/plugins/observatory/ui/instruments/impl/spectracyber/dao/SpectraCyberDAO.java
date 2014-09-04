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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.spectracyber.dao;

import org.apache.xmlbeans.XmlObject;
import org.jfree.data.xy.XYDataset;
import org.lmn.fc.frameworks.starbase.plugins.observatory.MetadataDictionary;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.AbstractObservatoryInstrumentDAO;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.CommandPoolList;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.*;
import org.lmn.fc.frameworks.starbase.portcontroller.AbstractResponseMessage;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageStatus;
import org.lmn.fc.model.datatypes.DataTypeDictionary;
import org.lmn.fc.model.xmlbeans.datatypes.SchemaDataType;
import org.lmn.fc.model.xmlbeans.instruments.CommandType;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;
import org.lmn.fc.model.xmlbeans.instruments.ParameterType;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;
import org.lmn.fc.model.xmlbeans.metadata.SchemaUnits;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

//--------------------------------------------------------------------------------------------------
// The frequency mapping of the hydrogen line is as follows  :
// (the rest for hydrogen is rounded off to 1420.405 Mhz)

// The spectracyber is a triple conversion receiver in spectral mode.
// The first l.o. is 1012.797 Mhz, the second l.o. is 337.599 Mhz
// and the third l.o. is the one that you have frequency control of. 48.6 +/- 2 mhz.
//
// The receiver covers hydrogen rest +/- 2 Mhz. so here is an example of the
// frequency scheme for a received frequency of 1420.405.
//--------------------------------------------------------------------------------------------------
//
// Input at the antenna (1420.405)  minus first l.o. (1012.797) =407.608  Mhz which is the first I.F.
//
// First I.F. (407.608Mhz) minus second l.o. (337.599 Mhz)= second I.F. (70.009 Mhz).
//
// Second I.F. (70.009 Mhz) minus programmable third l.o. (48.600 Mhz)=third I.F.  (21.4 Mhz)
//
// The third I.F. is filtered to 15 Khz bandwidth and sent to the detector.
//
// The programmable third l.o. can sweep 48.6 +/- 2 Mhz
// which gives  4 Mhz of range centered on hydrogen rest.
//
// i.e. range is 1418.405 to 1422.405 MHz --> 078H (46.6MHz) to 398H (50.6MHz)
// i.e. 800 steps of 5kHz centred on 1420.405 MHz
//
/***************************************************************************************************
 * SpectraCyberDAO.
 */

public final class SpectraCyberDAO extends AbstractObservatoryInstrumentDAO
                                   implements ObservatoryInstrumentDAOInterface
    {
    private static final int DAO_CHANNEL_COUNT = 1;


    /***********************************************************************************************
     * Create the Metadata to describe the Spectrum Chart.
     *
     * @return List<Metadata>
     */

    private static List<Metadata> createSpectrumChartMetadata()
        {
        final List<Metadata> listMetadata;

        listMetadata = new ArrayList<Metadata>(3);

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_OBSERVATION_TITLE.getKey(),
                                      "SpectraCyber Hydrogen Spectrum",
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      EMPTY_STRING);

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_OBSERVATION_AXIS_LABEL_X.getKey(),
                                      "Frequency Offset",
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      EMPTY_STRING);

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_OBSERVATION_AXIS_LABEL_Y.getKey() + MetadataDictionary.SUFFIX_SERIES_ZERO,
                                      "Spectrometer Output",
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      EMPTY_STRING);

        return (listMetadata);
        }


    /***********************************************************************************************
     * Create the Metadata to describe the Continuum Chart.
     *
     * @return List<Metadata>
     */

    private static List<Metadata> createContinuumChartMetadata()
        {
        final List<Metadata> listMetadata;

        listMetadata = new ArrayList<Metadata>(3);

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_OBSERVATION_TITLE.getKey(),
                                      "SpectraCyber Hydrogen Continuum",
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      EMPTY_STRING);

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_OBSERVATION_AXIS_LABEL_X.getKey(),
                                      "Time (UT)",
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      EMPTY_STRING);

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_OBSERVATION_AXIS_LABEL_Y.getKey() + MetadataDictionary.SUFFIX_SERIES_ZERO,
                                      "Radiometer Output",
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      EMPTY_STRING);

        return (listMetadata);
        }


    /***********************************************************************************************
     * Build the CommandPool using method names in this DAO.
     *
     * @param pool
     */

    private static void addSubclassToCommandPool(final CommandPoolList pool)
        {
        pool.add("captureContinuum");
        pool.add("captureSpectrum");
        pool.add("captureTestSpectrum");
        }


    /***********************************************************************************************
     * Construct a SpectraCyberDAO.
     *
     * @param hostinstrument
     */

    public SpectraCyberDAO(final ObservatoryInstrumentInterface hostinstrument)
        {
        super(hostinstrument);

        setRawData(new Vector<Object>(1000));

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
                               "SpectraCyberDAO.initialiseDAO() [resourcekey=" + resourcekey + "]");

        super.initialiseDAO(resourcekey);

        DAOHelper.loadSubClassResourceBundle(this);

        setRawData(new Vector<Object>(1000));

        return (true);
        }


    /***********************************************************************************************
     * Shut down the DAO and dispose of all Resources.
     */

    public void disposeDAO()
        {
        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               "SpectraCyberDAO.disposeDAO()");

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
        return (new SpectraCyberCommandMessage(dao,
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
        return (new SpectraCyberResponseMessage(portname,
                                                instrumentxml,
                                                module,
                                                command,
                                                starscript.trim(),
                                                responsestatusbits));
        }


    /**********************************************************************************************/
    /* DAO Local Commands                                                                         */
    /***********************************************************************************************
     * captureContinuum().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface captureContinuum(final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "SpectraCyberDAO.captureContinuum()";

        // TODO Scale value??

        // Only generate a ResponseMessage when completed
        return (CaptureCommandHelper.doIteratedDataCaptureCommand(this,
                                                                  commandmessage,
                                                                  createContinuumChartMetadata(),
                                                                  SOURCE,
                                                                  false));
        }


    /***********************************************************************************************
     * captureSpectrum().
     * There are six Command parameters: the starting value, the end value, the step size,
     * the wait time, realtime updates and verbose logging control.
     * The starting value is used to drive the first command in the list.
     * The wait time is sent to the next command in the list which takes a Parameter.
     * There are therefore TWO Parameters which must be sent to the SteppedCommands.
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface captureSpectrum(final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "SpectraCyberDAO.captureSpectrum()";

        // Only generate a ResponseMessage when completed
        // ToDo Add Metadata for ControlPanel indicators
        return (CaptureCommandHelper.doSteppedDataCaptureCommand(this,
                                                                 commandmessage,
                                                                 createSpectrumChartMetadata(),
                                                                 null,
                                                                 null,
                                                                 0,
                                                                 6,
                                                                 2,
                                                                 SOURCE,
                                                                 false));
        }


    /***********************************************************************************************
     * captureTestSpectrum().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface captureTestSpectrum(final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "SpectraCyberDAO.captureTestSpectrum() ";
        final CommandType commandType;
        ResponseMessageInterface responseMessage;

        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               "SpectraCyberDAO.captureTestSpectrum()");

        // Get the latest Resources
        readResources();

        // Don't affect the CommandType of the incoming Command
        commandType = (CommandType)commandmessage.getCommandType().copy();

        // TODO REVIEW Initialise all DAO data containers if possible...
        //clearData();

        try
            {
            final List<ParameterType> listParameters;
            final int CHANNEL_ID;

            // Do the captureSpectrum() operation
            // We expect two parameters, for the start and end frequency offsets
            listParameters = commandType.getParameterList();
            CHANNEL_ID = 0;

            if ((listParameters != null)
                && (listParameters.size() == 2)
                && (SchemaDataType.DECIMAL_FLOAT.equals(listParameters.get(0).getInputDataType().getDataTypeName()))
                && (SchemaDataType.DECIMAL_FLOAT.equals(listParameters.get(1).getInputDataType().getDataTypeName())))
                {
                final int intStart;
                final int intEnd;
                final Vector<Object> vecSpectrum;

                // These should never throw NumberFormatException, because they have already been parsed
                intStart = (int)Float.parseFloat(listParameters.get(0).getValue());
                intEnd = (int)Float.parseFloat(listParameters.get(1).getValue());
                vecSpectrum = HydrogenSpectrumHelper.getSpectrumByOffsetAsVector(intStart, intEnd, CHANNEL_ID);

                if (vecSpectrum != null)
                    {
                    final List<Metadata> listMetaData;
                    final XYDataset xyDataset;
                    final String strKey;

                    commandType.getResponse().setValue(ResponseMessageStatus.SUCCESS.getResponseValue());
                    listMetaData = HydrogenSpectrumHelper.createHydrogenSpectrumMetadata();
                    strKey = MetadataHelper.getMetadataValueByKey(listMetaData,
                                                                  MetadataDictionary.KEY_OBSERVATION_CHANNEL_NAME.getKey() + CHANNEL_ID);
                    xyDataset = HydrogenSpectrumHelper.getXYDataset(vecSpectrum,
                                                                    strKey);

                    // Create the ResponseMessage
                    responseMessage = new SpectraCyberResponseMessage(ResponseMessageHelper.getPortName(this),
                                                                      commandmessage.getInstrument(),
                                                                      commandmessage.getModule(),
                                                                      commandType,
                                                                      AbstractResponseMessage.buildResponseResourceKey(commandmessage.getInstrument(),
                                                                                                                       commandmessage.getModule(),
                                                                                                                       commandType),
                                                                      ResponseMessageStatus.SUCCESS.getBitMask());

                    // Add any data or metadata produced by this command
                    // ToDo add metadata calls to stub data
                    addAllMetadataToContainersTraced(listMetaData,
                                                     SOURCE,
                                                     LOADER_PROPERTIES.isMetadataDebug());
                    setRawData(vecSpectrum);
                    setXYDataset(xyDataset);

                    setRawDataChannelCount(1);
                    setTemperatureChannel(false);
                    setUnsavedData(true);

                    responseMessage.setWrappedData(new DAOWrapper(commandmessage,
                                                                  responseMessage,
                                                                  commandType.getResponse().getValue(),
                                                                  this));
                    }
                else
                    {
                    throw new NumberFormatException(ResponseMessageStatus.INVALID_PARAMETER.getName());
                    }
                }
            else
                {
                throw new NumberFormatException(ResponseMessageStatus.INVALID_PARAMETER.getName());
                }
            }

        catch (NumberFormatException exception)
            {
            // Invalid Parameters
            // Create the ResponseMessage
            responseMessage = new SpectraCyberResponseMessage(ResponseMessageHelper.getPortName(this),
                                                              commandmessage.getInstrument(),
                                                              commandmessage.getModule(),
                                                              commandType,
                                                              AbstractResponseMessage.buildResponseResourceKey(commandmessage.getInstrument(),
                                                                                                               commandmessage.getModule(),
                                                                                                               commandType),
                                                              ResponseMessageStatus.INVALID_PARAMETER.getBitMask());

            // Add any data or metadata produced by this command
            setRawDataChannelCount(1);
            setTemperatureChannel(false);
            setUnsavedData(false);

            responseMessage.setWrappedData(new DAOWrapper(commandmessage,
                                                          responseMessage,
                                                          EMPTY_STRING,
                                                          this));
            }

        return (responseMessage);
        }


    /***********************************************************************************************
     *  Read all the Resources required by the DAO.
     */

    public void readResources()
        {
        //System.out.println("SpectraCyberDAO.readResources() [ResourceKey=" + getResourceKey() + "]");

        super.readResources();
        }
    }
