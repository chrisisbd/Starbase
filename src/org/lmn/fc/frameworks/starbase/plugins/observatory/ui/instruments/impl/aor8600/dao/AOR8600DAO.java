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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.aor8600.dao;

import org.apache.xmlbeans.XmlObject;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.AbstractObservatoryInstrumentDAO;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.CommandPoolList;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.CaptureCommandHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.DAOHelper;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageInterface;
import org.lmn.fc.model.datatypes.DataTypeDictionary;
import org.lmn.fc.model.xmlbeans.instruments.CommandType;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;


/***********************************************************************************************
 * AOR8600DAO.
 */

public final class AOR8600DAO extends AbstractObservatoryInstrumentDAO
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
        final Metadata dataTitle;
        final Metadata dataAxisX;
        final Metadata dataAxisY;
        final DataTypeDictionary typeString;

        listMetadata = new ArrayList<Metadata>(3);
//        typeString = DataTypeDictionary.getDataTypeDictionaryEntryForName(SchemaDataType.STRING.toString());
//
//        dataTitle = Meta data.Factory.newInstance();
//        dataTitle.setKey(MetadataDictionary.KEY_OBSERVATION_TITLE.getKey());
//        dataTitle.setValue("AOR8600 Spectrum");
//        dataTitle.setDataTypeName(typeString.getSchemaDataType());
//        dataTitle.setDescription(EMPTY_STRING);
//        dataTitle.setUnits(SchemaUnits.DIMENSIONLESS);
//        listMetadata.add(dataTitle);
//
//        dataAxisX = Meta data.Factory.newInstance();
//        dataAxisX.setKey(MetadataDictionary.KEY_OBSERVATION_AXIS_LABEL_X.getKey());
//        dataAxisX.setValue("Frequency Offset");
//        dataAxisX.setDataTypeName(typeString.getSchemaDataType());
//        dataAxisX.setDescription(EMPTY_STRING);
//        dataAxisX.setUnits(SchemaUnits.DIMENSIONLESS);
//        listMetadata.add(dataAxisX);
//
//        dataAxisY = Meta data.Factory.newInstance();
//        dataAxisY.setKey(MetadataDictionary.KEY_OBSERVATION_AXIS_LABEL_Y.getKey() + "0");
//        dataAxisY.setValue("Spectrometer Output");
//        dataAxisY.setDataTypeName(typeString.getSchemaDataType());
//        dataAxisY.setDescription(EMPTY_STRING);
//        dataAxisY.setUnits(SchemaUnits.DIMENSIONLESS);
//        listMetadata.add(dataAxisY);

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
        final Metadata dataTitle;
        final Metadata dataAxisX;
        final Metadata dataAxisY;
        final DataTypeDictionary typeString;

        listMetadata = new ArrayList<Metadata>(3);
//        typeString = DataTypeDictionary.getDataTypeDictionaryEntryForName(SchemaDataType.STRING.toString());
//
//        dataTitle = Metad ata.Factory.newInstance();
//        dataTitle.setKey(MetadataDictionary.KEY_OBSERVATION_TITLE.getKey());
//        dataTitle.setValue("AOR8600 Continuum");
//        dataTitle.setDataTypeName(typeString.getSchemaDataType());
//        dataTitle.setDescription(EMPTY_STRING);
//        dataTitle.setUnits(SchemaUnits.DIMENSIONLESS);
//        listMetadata.add(dataTitle);
//
//        dataAxisX = Meta data.Factory.newInstance();
//        dataAxisX.setKey(MetadataDictionary.KEY_OBSERVATION_AXIS_LABEL_X.getKey());
//        dataAxisX.setValue("Time (UT)");
//        dataAxisX.setDataTypeName(typeString.getSchemaDataType());
//        dataAxisX.setDescription(EMPTY_STRING);
//        dataAxisX.setUnits(SchemaUnits.DIMENSIONLESS);
//        listMetadata.add(dataAxisX);
//
//        dataAxisY = Metada ta.Factory.newInstance();
//        dataAxisY.setKey(MetadataDictionary.KEY_OBSERVATION_AXIS_LABEL_Y.getKey() + "0");
//        dataAxisY.setValue("Radiometer Output");
//        dataAxisY.setDataTypeName(typeString.getSchemaDataType());
//        dataAxisY.setDescription(EMPTY_STRING);
//        dataAxisY.setUnits(SchemaUnits.DIMENSIONLESS);
//        listMetadata.add(dataAxisY);

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
        }


    /***********************************************************************************************
     * Construct a AOR8600DAO.
     *
     * @param hostinstrument
     */

    public AOR8600DAO(final ObservatoryInstrumentInterface hostinstrument)
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
                               "AOR8600DAO.initialiseDAO() [resourcekey=" + resourcekey + "]");

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
                               "AOR8600DAO.disposeDAO()");

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
        return new AOR8600CommandMessage(dao,
                                         instrumentxml,
                                         module,
                                         command,
                                         starscript.trim());
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
        return new AOR8600ResponseMessage(portname,
                                          instrumentxml,
                                          module,
                                          command,
                                          starscript.trim(),
                                          responsestatusbits);
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
        final String SOURCE = "AOR8600DAO.captureContinuum()";

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
        final String SOURCE = "AOR8600DAO.captureSpectrum()";

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
     *  Read all the Resources required by the DAO.
     *
     * KEY_DAO_TIMEOUT_DEFAULT
     * KEY_DAO_UPDATE_PERIOD
     */

    public void readResources()
        {
        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               "AOR8600DAO.readResources() [ResourceKey=" + getResourceKey() + "]");

        super.readResources();
        }
    }
