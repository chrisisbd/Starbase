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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.commands.datasets;

import org.apache.commons.math.stat.correlation.PearsonsCorrelation;
import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.frameworks.starbase.plugins.observatory.common.ObservatoryConstants;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.MetadataHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.ResponseMessageHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.logs.SimpleEventLogUIComponent;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageInterface;
import org.lmn.fc.model.datatypes.DecimalFormatPattern;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.xmlbeans.datatypes.SchemaDataType;
import org.lmn.fc.model.xmlbeans.instruments.CommandType;
import org.lmn.fc.model.xmlbeans.metadata.SchemaUnits;

import java.util.Vector;


/***************************************************************************************************
 * EvaluateTemperatureCorrelation.
 */

public final class EvaluateTemperatureCorrelation implements FrameworkConstants,
                                                             FrameworkStrings,
                                                             FrameworkMetadata,
                                                             FrameworkSingletons,
                                                             ObservatoryConstants
    {
    // String Resources
    private static final String RESPONSE_NAME = "CorrelationCoefficients";
    private static final String MSG_CORRELATION_NOT_POSSIBLE = "Correlation not possible";

    private static final int INDEX_TEMPERATURE_CHANNEL = 1;


    /***********************************************************************************************
     * Test the Pearson Correlation between the Temperature Channel and each Data Channel, if possible.
     *
     * @param dao
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public static ResponseMessageInterface doEvaluateTemperatureCorrelation(final ObservatoryInstrumentDAOInterface dao,
                                                                            final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "EvaluateTemperatureCorrelation.doEvaluateTemperatureCorrelation() ";
        final CommandType commandType;
        final ResponseMessageInterface responseMessage;

        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               SOURCE + "LOCAL COMMAND");

        // Don't affect the CommandType of the incoming Command
        commandType = (CommandType)commandmessage.getCommandType().copy();

        if ((commandType != null)
            && (commandType.getResponse() != null)
            && (RESPONSE_NAME.equals(commandType.getResponse().getName()))
            && (SchemaDataType.STRING.equals(commandType.getResponse().getDataTypeName()))
            && (SchemaUnits.DIMENSIONLESS.equals(commandType.getResponse().getUnits())))
            {
            final StringBuffer buffer;

            buffer = new StringBuffer();

            // Calculate the Pearson's Correlation between the Temperature Channel
            // and each Data Channel in turn, if possible
            // Check that we can correlate against Temperature
            // and that the correlation matrix is rectangular
            if ((dao != null)
                && (dao.getWrappedData() != null)
                && (dao.getWrappedData().hasTemperatureChannel())
                && (dao.getWrappedData().getRawData() != null)
                && (dao.getWrappedData().getRawData().size() > 1)
                && (dao.getWrappedData().getRawDataChannelCount() > 0))
                {
                final int intDataRows;
                final double[] dblTemperature;
                final Vector<Object> vecRawData;
                final int intVectorSize;

                vecRawData = dao.getWrappedData().getRawData();
                intDataRows = vecRawData.size();

                // There must be one Calendar, one Temperature, and Channel samples in the Vector...
                // The ChannelCount includes the Temperature channel if present
                intVectorSize = dao.getWrappedData().getRawDataChannelCount() + 1;

                dblTemperature = new double[intDataRows];

                // Fill up the Temperature data
                for (int intRowIndex = 0;
                     intRowIndex < intDataRows;
                     intRowIndex++)
                    {
                    final Vector vecSample;

                    vecSample = (Vector)vecRawData.get(intRowIndex);

                    if ((vecSample != null)
                        && (vecSample.size() == intVectorSize))
                        {
                        final Object objSample;

                        objSample = vecSample.get(INDEX_TEMPERATURE_CHANNEL);

                        // Offset over the Calendar to the Temperature Channel
                        if (objSample instanceof Double)
                            {
                            dblTemperature[intRowIndex] = (Double)objSample;
                            }
                        else if (objSample instanceof Integer)
                            {
                            dblTemperature[intRowIndex] = (Integer)objSample;
                            }
                        else
                            {
                            LOGGER.error(SOURCE + MSG_UNSUPPORTED_FORMAT);
                            }
                        }
                    }

                // Accumulate the data for each channel in turn (excluding Temperature)
                for (int intChannelIndex = 0;
                     intChannelIndex < (dao.getWrappedData().getRawDataChannelCount() - 1);
                     intChannelIndex++)
                    {
                    try
                        {
                        final double[] dblChannel;
                        final PearsonsCorrelation pearsonsCorrelation;
                        final double dblCoefficient;

                        dblChannel = new double[intDataRows];

                        // Fill up the Channel data
                        for (int intRowIndex = 0;
                             intRowIndex < intDataRows;
                             intRowIndex++)
                            {
                            final Vector vecSample;

                            vecSample = (Vector)vecRawData.get(intRowIndex);

                            // There must be one Calendar, one Temperature, and Channel samples in the Vector...
                            if ((vecSample != null)
                                && (vecSample.size() == intVectorSize))
                                {
                                final Object objSample;

                                objSample = vecSample.get(intChannelIndex + 2);

                                // Offset over the Calendar and Temperature Channel
                                if (objSample instanceof Double)
                                    {
                                    dblChannel[intRowIndex] = (Double)objSample;
                                    }
                                else if (objSample instanceof Integer)
                                    {
                                    dblChannel[intRowIndex] = (Integer)objSample;
                                    }
                                else
                                    {
                                    LOGGER.error(SOURCE + MSG_UNSUPPORTED_FORMAT);
                                    }
                                }
                            }

                        pearsonsCorrelation = new PearsonsCorrelation();
                        dblCoefficient = pearsonsCorrelation.correlation(dblTemperature, dblChannel);

                        if (intChannelIndex > 0)
                            {
                            buffer.append(", ");
                            }

                        buffer.append(MetadataHelper.getChannelName(dao.getWrappedData().getRawDataMetadata(), intChannelIndex+1, true));
                        buffer.append("=");

                        // Not sure how to do this with Decimal Format!
                        if (Double.isNaN(dblCoefficient))
                            {
                            buffer.append("NaN");
                            }
                        else
                            {
                            if (dblCoefficient >= 0.0)
                                {
                                buffer.append("+");
                                }

                            buffer.append(DecimalFormatPattern.PEARSON.format(dblCoefficient));
                            }
                        }

                    catch (IllegalArgumentException exception)
                        {
                        buffer.append(MetadataHelper.getChannelName(dao.getWrappedData().getRawDataMetadata(), intChannelIndex+1, true));
                        buffer.append("=");
                        buffer.append("failed");
                        }
                    }
                }

            // Create the ResponseMessage
            if (buffer.length() > 0)
                {
                SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                   EventStatus.INFO,
                                                   METADATA_TARGET_CORRELATION + SPACE
                                                        + METADATA_RESULT + buffer.toString() + TERMINATOR,
                                                   dao.getLocalHostname(),
                                                   dao.getObservatoryClock());
                commandType.getResponse().setValue(buffer.toString());

                //System.out.println("CORRELATION BUFFER={" + Utilities.byteArrayToSpacedHex(buffer.toString().getBytes()) + "}");
                }
            else
                {
                final String strReason;

                strReason = findReason(dao);

                SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                   EventStatus.WARNING,
                                                   METADATA_TARGET_CORRELATION + SPACE
                                                        + METADATA_RESULT + MSG_CORRELATION_NOT_POSSIBLE + TERMINATOR_SPACE
                                                        + METADATA_REASON + strReason + TERMINATOR,
                                                   dao.getLocalHostname(),
                                                   dao.getObservatoryClock());
                commandType.getResponse().setValue(MSG_CORRELATION_NOT_POSSIBLE);
                }

            responseMessage = ResponseMessageHelper.constructSuccessfulResponse(dao,
                                                                                commandmessage,
                                                                                commandType);
            }
        else
            {
            responseMessage = ResponseMessageHelper.constructFailedResponseIfNull(dao,
                                                                                  commandmessage,
                                                                                  commandType,
                                                                                  null);
            }

        return (responseMessage);
        }


    /***********************************************************************************************
     * Find the reason why Correlation is not possible.
     *
     * @param dao
     *
     * @return String
     */

    private static String findReason(final ObservatoryInstrumentDAOInterface dao)
        {
        final StringBuffer buffer;

        buffer = new StringBuffer();

        if (dao == null)
            {
            buffer.append("Null DAO");
            }
        else if (dao.getWrappedData() == null)
            {
            appendComma(buffer);
            buffer.append("NullDataWrapper");
            }
        else
            {
            if (!dao.getWrappedData().hasTemperatureChannel())
                {
                appendComma(buffer);
                buffer.append("MissingTemperature");
                }

            if (dao.getWrappedData().getRawData() == null)
                {
                appendComma(buffer);
                buffer.append("NullRawData");
                }

            if ((dao.getWrappedData().getRawData() != null)
                && (dao.getWrappedData().getRawData().size() <= 0))
                {
                appendComma(buffer);
                buffer.append("MissingRawData");
                }

            if (dao.getWrappedData().getRawDataChannelCount() <= 0)
                {
                appendComma(buffer);
                buffer.append("InvalidChannelCount");
                }
            }

        return (buffer.toString());
        }


    /***********************************************************************************************
     * Append a comma if necessary.
     *
     * @param buffer
     */

    private static void appendComma(final StringBuffer buffer)
        {
        if (buffer.length() > 0)
            {
            buffer.append(", ");
            }
        }
    }
