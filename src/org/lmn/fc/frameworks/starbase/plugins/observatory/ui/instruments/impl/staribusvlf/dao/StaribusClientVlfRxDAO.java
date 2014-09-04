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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.staribusvlf.dao;

import org.apache.xmlbeans.XmlObject;
import org.lmn.fc.common.utilities.misc.Utilities;
import org.lmn.fc.frameworks.starbase.plugins.observatory.MetadataDictionary;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.CommandPoolList;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.commands.audio.*;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.commands.publishers.PublishRealtime;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.*;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.dao.OscillatorInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.dao.StaribusCoreDAO;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.dao.StarinetCommandMessage;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.dao.StarinetResponseMessage;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.logs.SimpleEventLogUIComponent;
import org.lmn.fc.frameworks.starbase.portcontroller.AbstractResponseMessage;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageStatus;
import org.lmn.fc.frameworks.starbase.portcontroller.impl.streams.StreamType;
import org.lmn.fc.model.datatypes.DataTypeDictionary;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.xmlbeans.datatypes.SchemaDataType;
import org.lmn.fc.model.xmlbeans.instruments.CommandType;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;
import org.lmn.fc.model.xmlbeans.instruments.ParameterType;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;
import org.lmn.fc.model.xmlbeans.metadata.SchemaUnits;

import java.util.List;


/***************************************************************************************************
 * StaribusClientVlfRxDAO.
 */

public final class StaribusClientVlfRxDAO extends StaribusCoreDAO
                                          implements ObservatoryInstrumentDAOInterface
    {
    private OscillatorInterface oscillator;


    /***********************************************************************************************
     * Build the CommandPool using method names in this DAO.
     *
     * @param pool
     */

    private static void addSubclassToCommandPool(final CommandPoolList pool)
        {
        // Signal Processor
        pool.add("runOscillator");
        pool.add("measureFrequencyResponse");
        pool.add("configureOscillator");
        pool.add("setWaveformType");
        pool.add("setWaveformSampleRate");
        pool.add("setOscillatorFrequency");
        pool.add("setOscillatorAmplitude");
        pool.add("playAudioFile");
        pool.add("importAudioFile");
        }


    /***********************************************************************************************
     * Construct a StaribusClientVlfRxDAO.
     *
     * @param hostinstrument
     */

    public StaribusClientVlfRxDAO(final ObservatoryInstrumentInterface hostinstrument)
        {
        super(hostinstrument);

        this.oscillator = new AudioOscillator();

        addSubclassToCommandPool(getCommandPool());
        }


    /***********************************************************************************************
     * Initialise the DAO.
     *
     * @param resourcekey
     */

    public boolean initialiseDAO(final String resourcekey)
        {
        final String SOURCE = "StaribusClientVlfRxDAO.initialiseDAO() ";

        LOGGER.logTimedEvent(SOURCE + "[resourcekey=" + resourcekey + "]");

        super.initialiseDAO(resourcekey);

        DAOHelper.loadSubClassResourceBundle(this);

        return (true);
        }


    /***********************************************************************************************
     * Shut down the DAO and dispose of all Resources.
     */

    public void disposeDAO()
        {
        final String SOURCE = "StaribusClientVlfRxDAO.disposeDAO() ";

        if (getOscillator() != null)
            {
            getOscillator().dispose();
            this.oscillator = null;
            }

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
        // We are sending Staribus over Starinet
        // Use the Staribus address set by setStaribusAddress
        // If the Metadata item does not exist, create it, and return a default value of zero
        return (new StarinetCommandMessage(dao,
                                           instrumentxml,
                                           module,
                                           command,
                                           StreamType.STARIBUS,
                                           getStaribusAddressFromMetadata(),
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
        // Build messages only from the StaribusAddress
        // If the Metadata item does not exist, create it, and return a default value of zero
        return (new StarinetResponseMessage(portname,
                                            instrumentxml,
                                            module,
                                            command,
                                            getStaribusAddressFromMetadata(),
                                            starscript.trim(),
                                            responsestatusbits));
        }


    /**********************************************************************************************/
    /* DAO Local Commands                                                                         */
    /***********************************************************************************************
     * getStaribusAddress().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface getStaribusAddress(final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "StaribusClientVlfRxDAO.getStaribusAddress() ";
        final CommandType cmdGetAddress;
        final ResponseMessageInterface responseMessage;

        LOGGER.debug(isDebugMode(),
                     SOURCE + "LOCAL COMMAND");

        // Don't affect the CommandType of the incoming Command
        cmdGetAddress = (CommandType)commandmessage.getCommandType().copy();

        // Return the value of the local variable, wrapped in a ResponseMessage
        if (cmdGetAddress.getResponse() != null)
            {
            // If the Metadata item does not exist, create it, and return a default value of zero
            cmdGetAddress.getResponse().setValue(Utilities.intPositiveToThreeDecimalString(getStaribusAddressFromMetadata()));
            }

        getResponseMessageStatusList().add(ResponseMessageStatus.SUCCESS);

        // Create the ResponseMessage - this creates a DAOWrapper containing the data and logs
        responseMessage = ResponseMessageHelper.constructResponse(this,
                                                                  commandmessage,
                                                                  commandmessage.getInstrument(),
                                                                  commandmessage.getModule(),
                                                                  cmdGetAddress,
                                                                  AbstractResponseMessage.buildResponseResourceKey(commandmessage.getInstrument(),
                                                                                                                   commandmessage.getModule(),
                                                                                                                   cmdGetAddress));
        return (responseMessage);
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
        final String SOURCE = "StaribusClientVlfRxDAO.setStaribusAddress() ";
        final int PARAMETER_COUNT = 1;
        final CommandType cmdSetAddress;
        final List<ParameterType> listParameters;
        final ResponseMessageInterface responseMessage;

        LOGGER.debug(isDebugMode(),
                     SOURCE + "LOCAL COMMAND");

        // Don't affect the CommandType of the incoming Command
        cmdSetAddress = (CommandType)commandmessage.getCommandType().copy();

        // Parameters
        listParameters = cmdSetAddress.getParameterList();

        // Do not affect any data containers, channel count, or temperature indicator
        // Expect one Parameter, the StaribusAddress

        if ((getHostInstrument() != null)
            && (getHostInstrument().getInstrument() != null)
            && (listParameters != null)
            && (listParameters.size() == PARAMETER_COUNT)
            && (listParameters.get(0) != null)
            && (SchemaDataType.DECIMAL_INTEGER.equals(listParameters.get(0).getInputDataType().getDataTypeName())))
            {
            final String strIdentifier;

            strIdentifier = getHostInstrument().getInstrument().getIdentifier();

            try
                {
                String strStaribusAddress;
                final int intNewStaribusAddress;

                // Reformat the address with the correct number of leading zeroes
                strStaribusAddress = listParameters.get(0).getValue();
                intNewStaribusAddress = Integer.parseInt(strStaribusAddress);
                strStaribusAddress = Utilities.intToString(intNewStaribusAddress, RADIX_DECIMAL, 3);
                listParameters.get(0).setValue(strStaribusAddress);

                // If the Metadata item does not exist, create it and set the specified value
                setStaribusAddressToMetadata(intNewStaribusAddress);

                SimpleEventLogUIComponent.logEvent(getEventLogFragment(),
                                                   EventStatus.INFO,
                                                   METADATA_TARGET
                                                       + strIdentifier + TERMINATOR
                                                       + METADATA_ACTION_SET_ADDRESS
                                                       + METADATA_ADDRESS
                                                       + strStaribusAddress
                                                       + TERMINATOR,
                                                   SOURCE,
                                                   getObservatoryClock());
                getResponseMessageStatusList().add(ResponseMessageStatus.SUCCESS);
                }

            catch (final NumberFormatException exception)
                {
                SimpleEventLogUIComponent.logEvent(getEventLogFragment(),
                                                   EventStatus.FATAL,
                                                   METADATA_TARGET
                                                       + strIdentifier + TERMINATOR
                                                       + METADATA_ACTION_SET_ADDRESS
                                                       + METADATA_RESULT
                                                       + ObservatoryInstrumentDAOInterface.ERROR_PARSE_INPUT
                                                       + exception.getMessage()
                                                       + TERMINATOR,
                                                   SOURCE,
                                                   getObservatoryClock());
                getResponseMessageStatusList().add(ResponseMessageStatus.INVALID_PARAMETER);
                }
            }
        else
            {
            // Incorrectly configured XML
            getResponseMessageStatusList().add(DAOCommandHelper.logInvalidXML(this,
                                                                              SOURCE,
                                                                              METADATA_TARGET_UNKNOWN,
                                                                              METADATA_ACTION_SET_ADDRESS));
            }

        // Create the ResponseMessage
        if ((getResponseMessageStatusList().contains(ResponseMessageStatus.SUCCESS))
            || (getResponseMessageStatusList().contains(ResponseMessageStatus.ABORT)))
            {
            // Create the ResponseMessage - this creates a DAOWrapper containing the data and logs
            cmdSetAddress.getResponse().setValue(ResponseMessageStatus.SUCCESS.getResponseValue());

            responseMessage = ResponseMessageHelper.constructResponse(this,
                                                                      commandmessage,
                                                                      commandmessage.getInstrument(),
                                                                      commandmessage.getModule(),
                                                                      cmdSetAddress,
                                                                      AbstractResponseMessage.buildResponseResourceKey(commandmessage.getInstrument(),
                                                                                                                       commandmessage.getModule(),
                                                                                                                       cmdSetAddress));
            }
        else
            {
            // Create the failed ResponseMessage, indicating the last Status received
            responseMessage = ResponseMessageHelper.constructEmptyResponse(this,
                                                                           commandmessage,
                                                                           commandmessage.getInstrument(),
                                                                           commandmessage.getModule(),
                                                                           cmdSetAddress,
                                                                           AbstractResponseMessage.buildResponseResourceKey(commandmessage.getInstrument(),
                                                                                                                            commandmessage.getModule(),
                                                                                                                            cmdSetAddress));
            }

        return (responseMessage);
        }


    /***********************************************************************************************
     * captureRawDataRealtime().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface captureRawDataRealtime(final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "StaribusClientVlfRxDAO.captureRawDataRealtime()";

        // Only generate a ResponseMessage when completed
        return (CaptureCommandHelper.doIteratedStaribusMultichannelDataCaptureCommand(this,
                                                                                      commandmessage,
                                                                                      StaribusHelper.createMultichannelChartLegendMetadata("Staribus VLF Logger",
                                                                                                                                           "Time (UT)",
                                                                                                                                           "VLF Receiver Output"),
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
        final String SOURCE = "StaribusClientVlfRxDAO.publishChartRealtime() ";

        return (PublishRealtime.doPublishChartRealtime(this,
                                                       commandmessage,
                                                       StaribusHelper.createMultichannelChartLegendMetadata("Staribus VLF Publisher",
                                                                                                            "Time (UT)",
                                                                                                            "VLF Receiver Output"),
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
        final String SOURCE = "StaribusClientVlfRxDAO.publishChartRealtimeDay() ";

        return (PublishRealtime.doPublishChartRealtimeDay(this,
                                                          commandmessage,
                                                          StaribusHelper.createMultichannelChartLegendMetadata("Staribus VLF Publisher",
                                                                                                               "Time (UT)",
                                                                                                               "VLF Receiver Output"),
                                                          SOURCE,
                                                          false));
        }


    /***********************************************************************************************
     * configureOscillator().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface configureOscillator(final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "StaribusClientVlfRxDAO.setWaveformType()";

        return (ConfigureOscillator.doConfigureOscillator(this, getOscillator(), commandmessage));
        }


    /***********************************************************************************************
     * runOscillator().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface runOscillator(final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "StaribusClientVlfRxDAO.runOscillator()";

        return (RunOscillator.doRunOscillator(this, getOscillator(), commandmessage));
        }


    /***********************************************************************************************
     * measureFrequencyResponse().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface measureFrequencyResponse(final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "StaribusClientVlfRxDAO.measureFrequencyResponse()";

        return (MeasureFrequencyResponse.doMeasureFrequencyResponse(this,
                                                                    commandmessage,
                                                                    getOscillator(),
                                                                    StaribusHelper.createMultichannelChartLegendMetadata("Staribus VLF Receiver Frequency Response",
                                                                                                                         "Frequency Hz",
                                                                                                                         "VLF Receiver Output")));
        }


    /***********************************************************************************************
     * setWaveformType().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface setWaveformType(final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "StaribusClientVlfRxDAO.setWaveformType()";

        return (SetOscillatorWaveformType.doSetWaveformType(this, getOscillator(), commandmessage));
        }


    /***********************************************************************************************
     * setWaveformSampleRate().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface setWaveformSampleRate(final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "StaribusClientVlfRxDAO.setWaveformSampleRate()";

        return (SetOscillatorWaveformSampleRate.doSetWaveformSampleRate(this, getOscillator(), commandmessage));
        }


    /***********************************************************************************************
     * setOscillatorFrequency().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface setOscillatorFrequency(final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "StaribusClientVlfRxDAO.setOscillatorFrequency()";

        return (SetOscillatorFrequency.doSetOscillatorFrequency(this, getOscillator(), commandmessage));
        }


    /***********************************************************************************************
     * setOscillatorAmplitude().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface setOscillatorAmplitude(final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "StaribusClientVlfRxDAO.setOscillatorAmplitude()";

        return (SetOscillatorAmplitude.doSetOscillatorAmplitude(this, getOscillator(), commandmessage));
        }


    /***********************************************************************************************
     * playAudioFile().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface playAudioFile(final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "StaribusClientVlfRxDAO.playAudioFile()";

        return (PlayAudioFile.doPlayAudioFile(this, commandmessage));
        }


    /***********************************************************************************************
     * importAudioFile().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface importAudioFile(final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "StaribusClientVlfRxDAO.importAudioFile() ";

        return (ImportAudioFile.doImportAudioFile(this, commandmessage));
        }


    /***********************************************************************************************
     * Get the Audio Oscillator.
     *
     * @return OscillatorInterface
     */

    private OscillatorInterface getOscillator()
        {
        return (this.oscillator);
        }


    /***********************************************************************************************
     * Get the Staribus Address from the DAO Metadata.
     * If the Metadata item does not exist, create it, and return a default value of zero.
     *
     * @return int
     */

    private int getStaribusAddressFromMetadata()
        {
        final String SOURCE = "StaribusClientVlfRxDAO.getStaribusAddressFromMetadata() ";
        final int DEFAULT_ADDRESS = 0;
        final List<Metadata> listAggregateMetadata;
        int intStaribusAddress;

        listAggregateMetadata = MetadataHelper.collectAggregateMetadataTraced(REGISTRY.getFramework(),
                                                                              getHostInstrument().getContext().getObservatory(),
                                                                              getHostInstrument(),
                                                                              this,
                                                                              getWrappedData(),
                                                                              SOURCE,
                                                                              isDebugMode());
        if ((listAggregateMetadata != null)
            && (!listAggregateMetadata.isEmpty()))
            {
            final Metadata metadata;

            MetadataHelper.showMetadataList(listAggregateMetadata, SOURCE, isDebugMode());

            metadata = MetadataHelper.getMetadataByKey(listAggregateMetadata,
                                                       MetadataDictionary.KEY_INSTRUMENT_STARIBUS_ADDRESS.getKey());
            if ((metadata != null)
                && (metadata.getValue() != null))
                {
                try
                    {
                    intStaribusAddress = Integer.parseInt(metadata.getValue());
                    }

                catch (final NumberFormatException exception)
                    {
                    intStaribusAddress = DEFAULT_ADDRESS;
                    setStaribusAddressToMetadata(intStaribusAddress);
                    LOGGER.error(SOURCE + "Metadata value was invalid, using default [address=" + intStaribusAddress + "]");
                    }
                }
            else
                {
                intStaribusAddress = DEFAULT_ADDRESS;
                setStaribusAddressToMetadata(intStaribusAddress);
                LOGGER.error(SOURCE + "Metadata item did not exist, created new [address=" + intStaribusAddress + "]");
                }
            }
        else
            {
            intStaribusAddress = DEFAULT_ADDRESS;
            LOGGER.error(SOURCE + "Metadata collection failed to find any Metadata items, using default [address=" + intStaribusAddress + "]");
            }

        return (intStaribusAddress);
        }


    /***********************************************************************************************
     * Set the Staribus Address into the DAO Metadata.
     * If the Metadata item does not exist, create it and set the specified value.
     *
     * @param address
     */

    private void setStaribusAddressToMetadata(final int address)
        {
        final String SOURCE = "StaribusClientVlfRxDAO.setStaribusAddressToMetadata() ";
        final List<Metadata> listAggregateMetadata;

        listAggregateMetadata = MetadataHelper.collectAggregateMetadataTraced(REGISTRY.getFramework(),
                                                                              getHostInstrument().getContext().getObservatory(),
                                                                              getHostInstrument(),
                                                                              this,
                                                                              getWrappedData(),
                                                                              SOURCE,
                                                                              isDebugMode());
        if ((listAggregateMetadata != null)
            && (!listAggregateMetadata.isEmpty()))
            {
            MetadataHelper.showMetadataList(listAggregateMetadata, SOURCE, isDebugMode());

            MetadataHelper.updateMetadataValueTraced(listAggregateMetadata,
                                                     MetadataDictionary.KEY_INSTRUMENT_STARIBUS_ADDRESS.getKey(),
                                                     Integer.toString(address),
                                                     REGEX_SIGNED_DECIMAL_INTEGER,
                                                     DataTypeDictionary.DECIMAL_INTEGER,
                                                     SchemaUnits.DIMENSIONLESS,
                                                     "The Staribus address of the Instrument to which all remote commands will be sent",
                                                     "Staribus Address",
                                                     isDebugMode());
            }
        else
            {
            LOGGER.error(SOURCE + "Metadata collection failed to find any Metadata items, no action taken");
            }
        }


    /***********************************************************************************************
     *  Read all the Resources required by the DAO.
     *
     * KEY_DAO_ONERROR_CONTINUE
     * KEY_DAO_TIMEOUT_DEFAULT
     */

    public void readResources()
        {
        final String SOURCE = "StaribusClientVlfRxDAO.readResources() ";

        //LOGGER.debug(isDebugMode(), "StaribusClientVlfRxDAO.readResources() [ResourceKey=" + getResourceKey() + "]");

        super.readResources();
        }
    }
