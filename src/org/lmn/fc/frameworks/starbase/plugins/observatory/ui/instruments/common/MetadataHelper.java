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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.lmn.fc.common.constants.*;
import org.lmn.fc.common.datatranslators.MetadataFactory;
import org.lmn.fc.common.utilities.misc.Utilities;
import org.lmn.fc.common.xml.XmlBeansUtilities;
import org.lmn.fc.frameworks.starbase.plugins.observatory.MetadataDictionary;
import org.lmn.fc.frameworks.starbase.plugins.observatory.MetadataType;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ObservatoryInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.DAOWrapperInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.charts.ChartUIHelper;
import org.lmn.fc.model.dao.DataStore;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.DataTypeDictionary;
import org.lmn.fc.model.datatypes.DataTypeHelper;
import org.lmn.fc.model.datatypes.types.ColourDataType;
import org.lmn.fc.model.logging.Logger;
import org.lmn.fc.model.plugins.FrameworkPlugin;
import org.lmn.fc.model.registry.InstallationFolder;
import org.lmn.fc.model.xmlbeans.instruments.*;
import org.lmn.fc.model.xmlbeans.metadata.*;

import java.io.File;
import java.io.IOException;
import java.util.*;


/***************************************************************************************************
 * MetadataHelper.
 */

public final class MetadataHelper implements FrameworkConstants,
                                             FrameworkStrings,
                                             FrameworkMetadata,
                                             FrameworkRegex,
                                             FrameworkSingletons,
                                             ResourceKeys
    {
    // String Resources
    private static final String MSG_TYPE_MISMATCH = "DataType specified in Metadata does not match actual data [metadata=";
    private static final String KEY_INSTRUMENT = "Instrument";

    private static final String FILENAME_METADATA_SUFFIX = "-metadata.xml";

    // This is located in the starbase/imports folder
    private static final String FILENAME_FRAMEWORK_METADATA = "Framework" + FILENAME_METADATA_SUFFIX;

    // These are located in the observatory/imports folder
    private static final String FILENAME_OBSERVATORY_METADATA = "Observatory" + FILENAME_METADATA_SUFFIX;
    private static final String FILENAME_OBSERVER_METADATA = "Observer" + FILENAME_METADATA_SUFFIX;

    // The following are prefixed by the Instrument Identifier
    private static final String FILENAME_OBSERVATION_METADATA = "-Observation" + FILENAME_METADATA_SUFFIX;
    private static final String FILENAME_INSTRUMENT_METADATA = FILENAME_METADATA_SUFFIX;
    private static final String FILENAME_CONTROLLER_METADATA = "-controller" + FILENAME_METADATA_SUFFIX;

    private static final int INITIAL_CAPACITY = 50;


    /***********************************************************************************************
     * Collections                                                                                */
    /***********************************************************************************************
     * Gather all available MetadataMetadata for e.g. exporters.
     * This always returns with Metadata, default if necessary.
     *
     * @param hostinstrument
     * @param dao
     * @param wrapper
     *
     * @return List<Metadata>
     */

    public static List<Metadata> collectMetadataMetadata(final ObservatoryInstrumentInterface hostinstrument,
                                                         final ObservatoryInstrumentDAOInterface dao,
                                                         final DAOWrapperInterface wrapper)
        {
        final String SOURCE = "MetadataHelper.collectMetadataMetadata() ";
        final List<Metadata> listMetadataMetadata;

        listMetadataMetadata = new ArrayList<Metadata>(10);

        // There are no Observatory or Instrument Metadata

        // Add the DAO MetadataMetadata if any...
        if ((dao != null)
            && (dao.getMetadataMetadata() != null)
            && (!dao.getMetadataMetadata().isEmpty()))
            {
            listMetadataMetadata.addAll(dao.getMetadataMetadata());
            }
        else if ((wrapper != null)
            && (wrapper.getMetadataMetadata() != null)
            && (!wrapper.getMetadataMetadata().isEmpty()))
            {
            listMetadataMetadata.addAll(wrapper.getMetadataMetadata());
            }
        else
            {
            // If not, create some default MetadataMetadata,
            // which will work, but it will be a bit bland :-)
            listMetadataMetadata.addAll(MetadataFactory.createDefaultMetadataMetadata());
            }

        return (listMetadataMetadata);
        }


    /**********************************************************************************************
     * Gather all available Metadata, for display or export, i.e. without MetadataMetadata.
     * Framework, Observatory, Observer, Observation, Instrument, Command.
     * Leave out Controller and Plugin for individual processing.
     * NOTE THAT The DAO data take precedence over those in the Wrapper.
     *
     * @param framework
     * @param observatory
     * @param hostinstrument
     * @param dao
     * @param wrapper
     * @param tracemessage
     * @param traceon
     *
     * @return List<Metadata>
     */

    public static List<Metadata> collectAggregateMetadataTraced(final FrameworkPlugin framework,
                                                                final ObservatoryInterface observatory,
                                                                final ObservatoryInstrumentInterface hostinstrument,
                                                                final ObservatoryInstrumentDAOInterface dao,
                                                                final DAOWrapperInterface wrapper,
                                                                final String tracemessage,
                                                                final boolean traceon)
        {
        final String SOURCE = "\nMetadataHelper.collectAggregateMetadataTraced() ";
        final List<Metadata> listMetadata;

        listMetadata = new ArrayList<Metadata>(100);

        //------------------------------------------------------------------------------------------
        // Framework

        if ((framework != null)
            && (framework.getFrameworkMetadata() != null)
            && (!framework.getFrameworkMetadata().isEmpty()))
            {
            LOGGER.debug(traceon,
                         tracemessage + "Framework Metadata");
            addOrUpdateMetadataListTraced(listMetadata,
                                          framework.getFrameworkMetadata(),
                                          MetadataType.FRAMEWORK.getName(),
                                          traceon);
            }

        //------------------------------------------------------------------------------------------
        // Observatory

        if ((observatory != null)
            && (observatory.getObservatoryMetadata() != null)
            && (!observatory.getObservatoryMetadata().isEmpty()))
            {
            LOGGER.debug(traceon,
                         tracemessage + "Observatory Metadata");
            addOrUpdateMetadataListTraced(listMetadata,
                                          observatory.getObservatoryMetadata(),
                                          MetadataType.OBSERVATORY.getName(),
                                          traceon);
            }

        //------------------------------------------------------------------------------------------
        // Observer

        if ((observatory != null)
            && (observatory.getObserverMetadata() != null)
            && (!observatory.getObserverMetadata().isEmpty()))
            {
            LOGGER.debug(traceon,
                         tracemessage + "Observer Metadata");
            addOrUpdateMetadataListTraced(listMetadata,
                                          observatory.getObserverMetadata(),
                                          MetadataType.OBSERVER.getName(),
                                          traceon);
            }

        //------------------------------------------------------------------------------------------
        // Instrument Schema

        if ((hostinstrument != null)
            && (hostinstrument.getInstrument() != null))
            {
            // We have an Instrument
            // Add the Instrument Metadata if any...
            // These Metadata come from the original Instrument XML configuration
            // and are not removed by a DAO reset()

            if ((hostinstrument.getInstrument().getInstrumentMetadataList() != null)
                && (!hostinstrument.getInstrument().getInstrumentMetadataList().isEmpty()))
                {
                LOGGER.debug(traceon,
                             tracemessage + "Instrument Metadata");
                addOrUpdateMetadataListTraced(listMetadata,
                                              hostinstrument.getInstrument().getInstrumentMetadataList(),
                                              MetadataType.INSTRUMENT.getName(),
                                              traceon);
                }

            // Add the Instrument's Controller Metadata if any...
//            if ((hostinstrument.getInstrument().getController() != null)
//                && (hostinstrument.getInstrument().getController().getControllerMetadataList() != null)
//                && (!hostinstrument.getInstrument().getController().getControllerMetadataList().isEmpty()))
//                {
//                LOGGER.debug(traceon,
//                             SOURCE + "Instrument Controller Metadata");
//                addOrUpdateMetadataListTraced(listMetadata,
//                                              hostinstrument.getInstrument().getController().getControllerMetadataList(),
//                                              MetadataType.CONTROLLER.getName(),
//                                              traceon);
//                }

            // Add the Instrument's Controller Commands Metadata if any...
            if ((hostinstrument.getInstrument().getController() != null)
                && (hostinstrument.getInstrument().getController().getCommandList() != null)
                && (!hostinstrument.getInstrument().getController().getCommandList().isEmpty()))
                {
                final Iterator<CommandType> iterControllerCommands;

                iterControllerCommands = hostinstrument.getInstrument().getController().getCommandList().iterator();

                while (iterControllerCommands.hasNext())
                    {
                    final CommandType commandType;

                    commandType = iterControllerCommands.next();

                    if ((commandType != null)
                        && (commandType.getCommandMetadataList() != null)
                        && (!commandType.getCommandMetadataList().isEmpty()))
                        {
                        LOGGER.debug(traceon,
                                     tracemessage + "Instrument Controller Command Metadata");
                        addOrUpdateMetadataListTraced(listMetadata,
                                                      commandType.getCommandMetadataList(),
                                                      "Instrument Controller Command",
                                                      traceon);
                        }
                    }
                }

            // And all of the Instrument's Plugins Metadata
            if ((hostinstrument.getInstrument().getController() != null)
                && (hostinstrument.getInstrument().getController().getPluginList() != null)
                && (!hostinstrument.getInstrument().getController().getPluginList().isEmpty()))
                {
                final List<PluginType> listPlugins;

                listPlugins = hostinstrument.getInstrument().getController().getPluginList();

                for (int i = 0; i < listPlugins.size(); i++)
                    {
                    final PluginType plugin;

                    plugin = listPlugins.get(i);

                    // Plugins Metadata
//                    if ((plugin != null)
//                        && (plugin.getPluginMetadataList() != null)
//                        && (!plugin.getPluginMetadataList().isEmpty()))
//                        {
//                        LOGGER.debug(traceon,
//                                     tracemessage + "Instrument Plugin Metadata");
//                        addOrUpdateMetadataListTraced(listMetadata,
//                                                      plugin.getPluginMetadataList(),
//                                                      MetadataType.PLUGIN.getName(),
//                                                      traceon);
//                        }

                    // Add the Plugins Commands Metadata if any...
                    if ((plugin != null)
                        && (plugin.getCommandList() != null)
                        && (!plugin.getCommandList().isEmpty()))
                        {
                        final Iterator<CommandType> iterPluginCommands;

                        iterPluginCommands = plugin.getCommandList().iterator();

                        while (iterPluginCommands.hasNext())
                            {
                            final CommandType commandType;

                            commandType = iterPluginCommands.next();

                            if ((commandType != null)
                                && (commandType.getCommandMetadataList() != null)
                                && (!commandType.getCommandMetadataList().isEmpty()))
                                {
                                LOGGER.debug(traceon,
                                             tracemessage + "Instrument Plugin Command Metadata");
                                addOrUpdateMetadataListTraced(listMetadata,
                                                              commandType.getCommandMetadataList(),
                                                              "Instrument Plugin Command",
                                                              traceon);
                                }
                            }
                        }
                    }
                }
            }

        //------------------------------------------------------------------------------------------
        // Now add in the DAO Metadata directly from the DAO or from the Wrapper
        // These Metadata have been Captured or Imported

        if (dao != null)
            {
            if ((dao.getMetadataMetadata() != null)
                && (!dao.getMetadataMetadata().isEmpty()))
                {
                LOGGER.debug(traceon,
                             tracemessage + "DAO Metadata Metadata");
                addOrUpdateMetadataListTraced(listMetadata,
                                              dao.getMetadataMetadata(),
                                              "DAO MetadataMetadata",
                                              traceon);
                }

            if ((dao.getCurrentObservatoryMetadata() != null)
                && (!dao.getCurrentObservatoryMetadata().isEmpty()))
                {
                LOGGER.debug(traceon,
                             tracemessage + "Current Observatory Metadata");
                addOrUpdateMetadataListTraced(listMetadata,
                                              dao.getCurrentObservatoryMetadata(),
                                              "DAO Observatory",
                                              traceon);
                }

            if ((dao.getCurrentObserverMetadata() != null)
                && (!dao.getCurrentObserverMetadata().isEmpty()))
                {
                LOGGER.debug(traceon,
                             tracemessage + "Current Observer Metadata");
                addOrUpdateMetadataListTraced(listMetadata,
                                              dao.getCurrentObserverMetadata(),
                                              "DAO Observer",
                                              traceon);
                }

            if ((dao.getObservationMetadata() != null)
                && (!dao.getObservationMetadata().isEmpty()))
                {
                LOGGER.debug(traceon,
                             tracemessage + "DAO Observation Metadata");
                addOrUpdateMetadataListTraced(listMetadata,
                                              dao.getObservationMetadata(),
                                              "DAO Observation",
                                              traceon);
                }

            if ((dao.getInstrumentMetadata() != null)
                && (!dao.getInstrumentMetadata().isEmpty()))
                {
                LOGGER.debug(traceon,
                             tracemessage + "DAO Instrument Metadata");
                addOrUpdateMetadataListTraced(listMetadata,
                                              dao.getInstrumentMetadata(),
                                              "DAO Instrument",
                                              traceon);
                }

            //            if ((dao.getControllerMetadata() != null)
            //                && (!dao.getControllerMetadata().isEmpty()))
            //                {
            //                LOGGER.debug(traceon,
            //                             tracemessage + "DAO Controller Metadata");
            //                addOrUpdateMetadataListTraced(listMetadata,
            //                                              dao.getControllerMetadata(),
            //                                              "DAO Controller",
            //                                              traceon);
            //                }
            //
            //            if ((dao.getPluginMetadata() != null)
            //                && (!dao.getPluginMetadata().isEmpty()))
            //                {
            //                LOGGER.debug(traceon,
            //                             tracemessage + "DAO Plugin Metadata");
            //                addOrUpdateMetadataListTraced(listMetadata,
            //                                              dao.getPluginMetadata(),
            //                                              "DAO Plugin",
            //                                              traceon);
            //                }

            if ((dao.getRawDataMetadata() != null)
                && (!dao.getRawDataMetadata().isEmpty()))
                {
                LOGGER.debug(traceon,
                             tracemessage + "DAO RawData Metadata");
                addOrUpdateMetadataListTraced(listMetadata,
                                              dao.getRawDataMetadata(),
                                              "DAO RawData",
                                              traceon);
                }

            if ((dao.getXYDatasetMetadata() != null)
                && (!dao.getXYDatasetMetadata().isEmpty()))
                {
                LOGGER.debug(traceon,
                             tracemessage + "DAO XYDataset Metadata");
                addOrUpdateMetadataListTraced(listMetadata,
                                              dao.getXYDatasetMetadata(),
                                              "DAO XYDataset",
                                              traceon);
                }
            }

        //------------------------------------------------------------------------------------------
        // If no DAO present, then use the Wrapper data

        else if (wrapper != null)
            {
            if ((wrapper.getMetadataMetadata() != null)
                && (!wrapper.getMetadataMetadata().isEmpty()))
                {
                LOGGER.debug(traceon,
                             tracemessage + "Wrapper Metadata Metadata");
                addOrUpdateMetadataListTraced(listMetadata,
                                              wrapper.getMetadataMetadata(),
                                              "Wrapper MetadataMetadata",
                                              traceon);
                }

            if ((wrapper.getCurrentObservatoryMetadata() != null)
                && (!wrapper.getCurrentObservatoryMetadata().isEmpty()))
                {
                LOGGER.debug(traceon,
                             tracemessage + "Current Observatory Metadata");
                addOrUpdateMetadataListTraced(listMetadata,
                                              wrapper.getCurrentObservatoryMetadata(),
                                              "Wrapper Observatory",
                                              traceon);
                }

            if ((wrapper.getCurrentObserverMetadata() != null)
                && (!wrapper.getCurrentObserverMetadata().isEmpty()))
                {
                LOGGER.debug(traceon,
                             tracemessage + "Current Observer Metadata");
                addOrUpdateMetadataListTraced(listMetadata,
                                              wrapper.getCurrentObserverMetadata(),
                                              "Wrapper Observer",
                                              traceon);
                }

            if ((wrapper.getObservationMetadata() != null)
                && (!wrapper.getObservationMetadata().isEmpty()))
                {
                LOGGER.debug(traceon,
                             tracemessage + "Wrapper Observation Metadata");
                addOrUpdateMetadataListTraced(listMetadata,
                                              wrapper.getObservationMetadata(),
                                              "Wrapper Observation",
                                              traceon);
                }

            if ((wrapper.getInstrumentMetadata() != null)
                && (!wrapper.getInstrumentMetadata().isEmpty()))
                {
                LOGGER.debug(traceon,
                             tracemessage + "Wrapper Instrument Metadata");
                addOrUpdateMetadataListTraced(listMetadata,
                                              wrapper.getInstrumentMetadata(),
                                              "Wrapper Instrument",
                                              traceon);
                }

//            if ((wrapper.getControllerMetadata() != null)
//                && (!wrapper.getControllerMetadata().isEmpty()))
//                {
//                LOGGER.debug(traceon,
//                             tracemessage + "Wrapper Controller Metadata");
//                addOrUpdateMetadataListTraced(listMetadata,
//                                              wrapper.getControllerMetadata(),
//                                              "Wrapper Controller",
//                                              traceon);
//                }

//            if ((wrapper.getPluginMetadata() != null)
//                && (!wrapper.getPluginMetadata().isEmpty()))
//                {
//                LOGGER.debug(traceon,
//                             tracemessage + "Wrapper Plugin Metadata");
//                addOrUpdateMetadataListTraced(listMetadata,
//                                              wrapper.getPluginMetadata(),
//                                              "Wrapper Plugin",
//                                              traceon);
//                }

            if ((wrapper.getRawDataMetadata() != null)
                && (!wrapper.getRawDataMetadata().isEmpty()))
                {
                LOGGER.debug(traceon,
                             tracemessage + "Wrapper RawData Metadata");
                addOrUpdateMetadataListTraced(listMetadata,
                                              wrapper.getRawDataMetadata(),
                                              "Wrapper RawData",
                                              traceon);
                }

            if ((wrapper.getXYDatasetMetadata() != null)
                && (!wrapper.getXYDatasetMetadata().isEmpty()))
                {
                LOGGER.debug(traceon,
                             tracemessage + "Wrapper XYDataset Metadata");
                addOrUpdateMetadataListTraced(listMetadata,
                                              wrapper.getXYDatasetMetadata(),
                                              "Wrapper XYDataset",
                                              traceon);
                }
            }
        else
            {
            LOGGER.debug(traceon,
                         tracemessage + "No DAO or Wrapper metadata found");
            }

        LOGGER.debug(traceon,
                     tracemessage + "END OF METADATA TRACE");

        return (listMetadata);
        }


    /**********************************************************************************************
     * Gather all available Metadata from a DAO, without MetadataMetadata.
     * Intended for Charts, so Observatory, Observer, Command Metadata etc. are not collected.
     *
     * Collects:
     *      ObservationMetadata
     *      InstrumentMetadata
     *      ControllerMetadata
     *      PluginMetadata
     *      RawDataMetadata
     *      XYDatasetMetadata
     *
     * @param dao
     *
     * @return List<Metadata>
     */

    public static List<Metadata> collectMetadataForChartFromDAO(final ObservatoryInstrumentDAOInterface dao)
        {
        final String SOURCE = "MetadataHelper.collectMetadataForChartFromDAO() ";
        final List<Metadata> listMetadata;
        final boolean traceon;

        listMetadata = new ArrayList<Metadata>(INITIAL_CAPACITY);
        traceon = LOADER_PROPERTIES.isMetadataDebug()
                    || LOADER_PROPERTIES.isChartDebug();

        // Now add in the DAO Metadata from the DataTranslator, RawData, XYDataset (or wherever)
        if (dao != null)
            {
            if ((dao.getObservationMetadata() != null)
                && (!dao.getObservationMetadata().isEmpty()))
                {
                addOrUpdateMetadataListTraced(listMetadata,
                                              dao.getObservationMetadata(),
                                              SOURCE,
                                              traceon);
                }

            if ((dao.getInstrumentMetadata() != null)
                && (!dao.getInstrumentMetadata().isEmpty()))
                {
                addOrUpdateMetadataListTraced(listMetadata,
                                              dao.getInstrumentMetadata(),
                                              SOURCE,
                                              traceon);
                }

            if ((dao.getControllerMetadata() != null)
                && (!dao.getControllerMetadata().isEmpty()))
                {
                addOrUpdateMetadataListTraced(listMetadata,
                                              dao.getControllerMetadata(),
                                              SOURCE,
                                              traceon);
                }

            if ((dao.getPluginMetadata() != null)
                && (!dao.getPluginMetadata().isEmpty()))
                {
                addOrUpdateMetadataListTraced(listMetadata,
                                              dao.getPluginMetadata(),
                                              SOURCE,
                                              traceon);
                }

            if ((dao.getRawDataMetadata() != null)
                && (!dao.getRawDataMetadata().isEmpty()))
                {
                addOrUpdateMetadataListTraced(listMetadata,
                                              dao.getRawDataMetadata(),
                                              SOURCE,
                                              traceon);
                }

            if ((dao.getXYDatasetMetadata() != null)
                && (!dao.getXYDatasetMetadata().isEmpty()))
                {
                addOrUpdateMetadataListTraced(listMetadata,
                                              dao.getXYDatasetMetadata(),
                                              SOURCE,
                                              traceon);
                }
            }

        return (listMetadata);
        }


    /***********************************************************************************************
     * Collect all available Metadata for export, from the DAO.
     * Only include the MetadataMetadata if asked to do so.
     *
     * @param dao
     * @param includemetadatametadata
     */

    public static List<Metadata> collectMetadataForExportFromDAO(final ObservatoryInstrumentDAOInterface dao,
                                                                 final boolean includemetadatametadata)
        {
        final String SOURCE = "MetadataHelper.collectMetadataForExportFromDAO() ";
        final List<Metadata> listMetadata;

        // Combine the Metadata produced by the DAO with that from the Instrument and Observatory
        // The return value cannot be NULL, but it may be an empty List
        listMetadata = collectAggregateMetadataTraced(REGISTRY.getFramework(),
                                                      dao.getHostInstrument().getContext().getObservatory(),
                                                      dao.getHostInstrument(),
                                                      dao,
                                                      null,
                                                      SOURCE,
                                                      LOADER_PROPERTIES.isMetadataDebug());
        if (includemetadatametadata)
            {
            final List<Metadata> listMetadataMetadata;

            listMetadataMetadata = collectMetadataMetadata(dao.getHostInstrument(),
                                                           dao,
                                                           null);
            if ((listMetadataMetadata != null)
                && (!listMetadataMetadata.isEmpty()))
                {
                listMetadata.addAll(listMetadataMetadata);
                }
            }

        return (listMetadata);
        }


    /***********************************************************************************************
     * Collect all available Metadata for export, from the DAOWrapper.
     * Only include the MetadataMetadata if asked to do so.
     *
     * @param wrapper
     * @param includemetadatametadata
     */

    public static List<Metadata> collectMetadataForExportFromWrapper(final DAOWrapperInterface wrapper,
                                                                     final boolean includemetadatametadata)
        {
        final String SOURCE = "MetadataHelper.collectMetadataForExportFromWrapper() ";
        final List<Metadata> listMetadata;
        final ObservatoryInstrumentDAOInterface dao;

        dao = wrapper.getWrappedDAO();

        // Combine the Metadata produced by the DAO with that from the Instrument and Observatory
        // The return value cannot be NULL, but it may be an empty List
        listMetadata = collectAggregateMetadataTraced(REGISTRY.getFramework(),
                                                      dao.getHostInstrument().getContext().getObservatory(),
                                                      dao.getHostInstrument(),
                                                      dao,
                                                      wrapper,
                                                      SOURCE,
                                                      LOADER_PROPERTIES.isMetadataDebug());
        if (includemetadatametadata)
            {
            final List<Metadata> listMetadataMetadata;

            listMetadataMetadata = collectMetadataMetadata(dao.getHostInstrument(),
                                                           dao,
                                                           wrapper);
            if ((listMetadataMetadata != null)
                && (!listMetadataMetadata.isEmpty()))
                {
                listMetadata.addAll(listMetadataMetadata);
                }
            }

        return (listMetadata);
        }


    /**********************************************************************************************/
    /* Importers                                                                                  */
    /***********************************************************************************************
     * Import the Framework Metadata.
     *
     * @param metadatatype
     * @param framework
     *
     * @return boolean
     *
     * @throws XmlException
     * @throws IOException
     */

    public static boolean importFrameworkMetadata(final MetadataType metadatatype,
                                                  final FrameworkPlugin framework) throws XmlException, IOException
        {
        final String SOURCE = "MetadataHelper.importFrameworkMetadata() ";
        boolean boolSuccess;

        boolSuccess = false;

        if (framework != null)
            {
            final String strFilename;
            final String strPathnameMetadata;
            final File fileMetadata;
            final FrameworkMetadataDocument docFrameworkMetadata;

            // The filename is Framework-metadata.xml
            strFilename = FILENAME_FRAMEWORK_METADATA;

            // Construct the name of the file containing the metadata
            strPathnameMetadata = InstallationFolder.getTerminatedUserDir()
                                       + DataStore.CONFIG.getLoadFolder()
                                       + System.getProperty("file.separator")
                                       + strFilename;
            fileMetadata = new File(strPathnameMetadata);
            docFrameworkMetadata = FrameworkMetadataDocument.Factory.parse(fileMetadata);

            if (XmlBeansUtilities.isValidXml(docFrameworkMetadata))
                {
                final FrameworkMetadataDocument.FrameworkMetadata frameworkMetadata;

                frameworkMetadata = docFrameworkMetadata.getFrameworkMetadata();

                if (frameworkMetadata != null)
                    {
                    if (!frameworkMetadata.getMetadataList().isEmpty())
                        {
                        addOrUpdateMetadataList(framework.getFrameworkMetadata(),
                                                frameworkMetadata.getMetadataList());
                        boolSuccess = true;
                        }
                    }
                }
            }

        return (boolSuccess);
        }


    /***********************************************************************************************
     * Import the Observatory Metadata.
     *
     * @param metadatatype
     * @param observatory
     * @param traceon
     *
     * @return boolean
     *
     * @throws XmlException
     * @throws IOException
     */

    public static boolean importObservatoryMetadata(final MetadataType metadatatype,
                                                    final ObservatoryInterface observatory,
                                                    final boolean traceon) throws XmlException, IOException
        {
        final String SOURCE = "MetadataHelper.importObservatoryMetadata() ";
        boolean boolSuccess;

        boolSuccess = false;

        if (observatory != null)
            {
            final String strFilename;
            final String strPathnameMetadata;
            final File fileMetadata;
            final ObservatoryMetadataDocument docObservatoryMetadata;

            // The filename is Observatory-metadata.xml
            strFilename = FILENAME_OBSERVATORY_METADATA;

            strPathnameMetadata = InstallationFolder.getTerminatedUserDir()
                                       + PATH_PLUGINS_OBSERVATORY
                                       + DataStore.CONFIG.getLoadFolder()
                                       + System.getProperty("file.separator")
                                       + strFilename;
            fileMetadata = new File(strPathnameMetadata);
            docObservatoryMetadata = ObservatoryMetadataDocument.Factory.parse(fileMetadata);

            if (XmlBeansUtilities.isValidXml(docObservatoryMetadata))
                {
                final ObservatoryMetadataDocument.ObservatoryMetadata observatoryMetadata;

                observatoryMetadata = docObservatoryMetadata.getObservatoryMetadata();

                if ((observatoryMetadata != null)
                    && (!observatoryMetadata.getMetadataList().isEmpty())
                    && (observatory.getObservatoryMetadata() != null))
                    {
                    // Remove all previous Metadata
                    observatory.getObservatoryMetadata().clear();

                    addOrUpdateMetadataListTraced(observatory.getObservatoryMetadata(),
                                                  observatoryMetadata.getMetadataList(),
                                                  SOURCE,
                                                  traceon);
                    boolSuccess = true;
                    }
                }
            }

        return (boolSuccess);
        }


    /***********************************************************************************************
     * Import the Observer Metadata.
     *
     * @param metadatatype
     * @param observatory
     * @param traceon
     *
     * @return boolean
     *
     * @throws XmlException
     * @throws IOException
     */

    public static boolean importObserverMetadata(final MetadataType metadatatype,
                                                 final ObservatoryInterface observatory,
                                                 final boolean traceon) throws XmlException, IOException
        {
        final String SOURCE = "MetadataHelper.importObserverMetadata() ";
        boolean boolSuccess;

        boolSuccess = false;

        if (observatory != null)
            {
            final String strFilename;
            final String strPathnameMetadata;
            final File fileMetadata;
            final ObserverMetadataDocument docObserverMetadata;

            // The filename is Observer-metadata.xml
            strFilename = FILENAME_OBSERVER_METADATA;

            strPathnameMetadata = InstallationFolder.getTerminatedUserDir()
                                       + PATH_PLUGINS_OBSERVATORY
                                       + DataStore.CONFIG.getLoadFolder()
                                       + System.getProperty("file.separator")
                                       + strFilename;
            fileMetadata = new File(strPathnameMetadata);

            docObserverMetadata = ObserverMetadataDocument.Factory.parse(fileMetadata);

            if (XmlBeansUtilities.isValidXml(docObserverMetadata))
                {
                final ObserverMetadataDocument.ObserverMetadata observerMetadata;

                observerMetadata = docObserverMetadata.getObserverMetadata();

                if (observerMetadata != null)
                    {
                    if (!observerMetadata.getMetadataList().isEmpty())
                        {
                        addOrUpdateMetadataListTraced(observatory.getObserverMetadata(),
                                                      observerMetadata.getMetadataList(),
                                                      SOURCE,
                                                      traceon);
                        boolSuccess = true;
                        }
                    }
                }
            }

        return (boolSuccess);
        }


    /***********************************************************************************************
     * Import the Observation Metadata.
     *
     * @param metadatatype
     * @param dao
     *
     * @return boolean
     *
     * @throws XmlException
     * @throws IOException
     */

    public static boolean importObservationMetadata(final MetadataType metadatatype,
                                                    final ObservatoryInstrumentDAOInterface dao) throws XmlException, IOException
        {
        final String SOURCE = "MetadataHelper.importObservationMetadata() ";
        boolean boolSuccess;

        boolSuccess = false;

        if ((dao != null)
            && (dao.getHostInstrument() != null)
            && (dao.getHostInstrument().getInstrument() != null))
            {
            final String strFilename;
            final String strPathnameMetadata;
            final File fileMetadata;
            final ObservationMetadataDocument docObservationMetadata;
            final String strIdentifier;

            // The filename is <Instrument.Identifier>-Observation-metadata.xml
            strIdentifier = dao.getHostInstrument().getInstrument().getIdentifier();
            strFilename = strIdentifier + FILENAME_OBSERVATION_METADATA;

            // The file is still in the imports folder
            strPathnameMetadata = InstallationFolder.getTerminatedUserDir()
                                       + PATH_PLUGINS_OBSERVATORY
                                       + DataStore.CONFIG.getLoadFolder()
                                       + System.getProperty("file.separator")
                                       + strFilename;
            fileMetadata = new File(strPathnameMetadata);

            docObservationMetadata = ObservationMetadataDocument.Factory.parse(fileMetadata);

            if (XmlBeansUtilities.isValidXml(docObservationMetadata))
                {
                final ObservationMetadataDocument.ObservationMetadata observationMetadata;

                observationMetadata = docObservationMetadata.getObservationMetadata();

                if (observationMetadata != null)
                    {
                    if (!observationMetadata.getMetadataList().isEmpty())
                        {
                        addOrUpdateMetadataList(dao.getObservationMetadata(),
                                                observationMetadata.getMetadataList());
                        boolSuccess = true;
                        }
                    }
                }
            }

        return (boolSuccess);
        }


    /***********************************************************************************************
     * Import the Instrument Metadata.
     *
     * @param metadatatype
     * @param dao
     *
     * @return boolean
     *
     * @throws XmlException
     * @throws IOException
     */

    public static boolean importInstrumentMetadata(final MetadataType metadatatype,
                                                   final ObservatoryInstrumentDAOInterface dao) throws XmlException, IOException
        {
        final String SOURCE = "MetadataHelper.importInstrumentMetadata() ";
        boolean boolSuccess;

        boolSuccess = false;

        if ((dao != null)
            && (dao.getHostInstrument() != null)
            && (dao.getHostInstrument().getInstrument() != null))
            {
            final String strFilename;
            final String strPathnameMetadata;
            final File fileMetadata;
            final InstrumentMetadataDocument docInstrumentMetadata;
            final String strIdentifier;

            // The filename is <Instrument.Identifier>-metadata.xml
            strIdentifier = dao.getHostInstrument().getInstrument().getIdentifier();
            strFilename = strIdentifier + FILENAME_INSTRUMENT_METADATA;

            // The file is still in the imports folder
            strPathnameMetadata = InstallationFolder.getTerminatedUserDir()
                                       + PATH_PLUGINS_OBSERVATORY
                                       + DataStore.CONFIG.getLoadFolder()
                                       + System.getProperty("file.separator")
                                       + strFilename;
            fileMetadata = new File(strPathnameMetadata);

            docInstrumentMetadata = InstrumentMetadataDocument.Factory.parse(fileMetadata);

            if (XmlBeansUtilities.isValidXml(docInstrumentMetadata))
                {
                final InstrumentMetadataDocument.InstrumentMetadata instrumentMetadata;

                instrumentMetadata = docInstrumentMetadata.getInstrumentMetadata();

                if (instrumentMetadata != null)
                    {
                    if (!instrumentMetadata.getMetadataList().isEmpty())
                        {
                        addOrUpdateMetadataList(dao.getInstrumentMetadata(),
                                                instrumentMetadata.getMetadataList());
                        boolSuccess = true;
                        }
                    }
                }
            }

        return (boolSuccess);
        }


    /***********************************************************************************************
     * Import the Controller Metadata.
     * Modify the Key of each item to show that it came from the Instrument.
     *
     * @param metadatatype
     * @param dao
     *
     * @return boolean
     *
     * @throws XmlException
     * @throws IOException
     */

    public static boolean importControllerMetadata(final MetadataType metadatatype,
                                                   final ObservatoryInstrumentDAOInterface dao) throws XmlException, IOException
        {
        final String SOURCE = "MetadataHelper.importControllerMetadata() ";
        boolean boolSuccess;

        boolSuccess = false;

        if ((dao != null)
            && (dao.getHostInstrument() != null)
            && (dao.getHostInstrument().getInstrument() != null))
            {
            final String strFilename;
            final String strPathnameMetadata;
            final File fileMetadata;
            final ControllerMetadataDocument docControllerMetadata;
            final String strIdentifier;

            // The filename is <Instrument.Identifier>-controller-metadata.xml
            strIdentifier = dao.getHostInstrument().getInstrument().getIdentifier();
            strFilename = strIdentifier + FILENAME_CONTROLLER_METADATA;

            // The file is still in the imports folder
            strPathnameMetadata = InstallationFolder.getTerminatedUserDir()
                                       + PATH_PLUGINS_OBSERVATORY
                                       + DataStore.CONFIG.getLoadFolder()
                                       + System.getProperty("file.separator")
                                       + strFilename;
            fileMetadata = new File(strPathnameMetadata);

            docControllerMetadata = ControllerMetadataDocument.Factory.parse(fileMetadata);

            if (XmlBeansUtilities.isValidXml(docControllerMetadata))
                {
                final ControllerMetadataDocument.ControllerMetadata controllerMetadata;

                controllerMetadata = docControllerMetadata.getControllerMetadata();

                if ((controllerMetadata != null)
                   && (controllerMetadata.getMetadataList() != null)
                   && (!controllerMetadata.getMetadataList().isEmpty()))
                    {
                    // Modify the Key of each item to show that it came from the Instrument
                    for (int intIndex = 0;
                         intIndex < controllerMetadata.getMetadataList().size();
                         intIndex++)
                        {
                        final Metadata metadata;

                        metadata = controllerMetadata.getMetadataList().get(intIndex);
                        metadata.setKey(KEY_INSTRUMENT + DOT + metadata.getKey());
                        }

                    // Pretend the Metadata are coming from the Instrument
                    addOrUpdateMetadataList(dao.getInstrumentMetadata(),
                                            controllerMetadata.getMetadataList());
//                    addOrUpdateMetadataList(dao.getControllerMetadata(),
//                                            controllerMetadata.getMetadataList());
                    boolSuccess = true;
                    }
                }
            }

        return (boolSuccess);
        }


    /***********************************************************************************************
     * Import the Plugin Metadata.
     *
     * @param metadatatype
     * @param moduleidentifier
     * @param dao
     *
     * @return boolean
     *
     * @throws XmlException
     * @throws IOException
     */

    public static boolean importPluginMetadata(final MetadataType metadatatype,
                                               final String moduleidentifier,
                                               final ObservatoryInstrumentDAOInterface dao) throws XmlException, IOException
        {
        final String SOURCE = "MetadataHelper.importPluginMetadata() ";
        boolean boolSuccess;

        boolSuccess = false;

        if ((dao != null)
            && (dao.getHostInstrument() != null)
            && (dao.getHostInstrument().getInstrument() != null))
            {
            final String strFilename;
            final String strPathnameMetadata;
            final File fileMetadata;
            final PluginMetadataDocument docPluginMetadata;
            final String strInstrumentIdentifier;

            strInstrumentIdentifier = dao.getHostInstrument().getInstrument().getIdentifier();

            // The filename is <Instrument>-<Plugin>-metadata.xml
            strFilename = strInstrumentIdentifier + HYPHEN + moduleidentifier + FILENAME_METADATA_SUFFIX;

            // The file is still in the imports folder
            strPathnameMetadata = InstallationFolder.getTerminatedUserDir()
                                       + PATH_PLUGINS_OBSERVATORY
                                       + DataStore.CONFIG.getLoadFolder()
                                       + System.getProperty("file.separator")
                                       + strFilename;
            fileMetadata = new File(strPathnameMetadata);

            docPluginMetadata = PluginMetadataDocument.Factory.parse(fileMetadata);

            if (XmlBeansUtilities.isValidXml(docPluginMetadata))
                {
                final PluginMetadataDocument.PluginMetadata pluginMetadata;

                pluginMetadata = docPluginMetadata.getPluginMetadata();

                if ((pluginMetadata != null)
                    && (pluginMetadata.getMetadataList() != null)
                    && (!pluginMetadata.getMetadataList().isEmpty()))
                    {
                    // Modify the Key of each item to show that it came from the Instrument
                    for (int intIndex = 0;
                         intIndex < pluginMetadata.getMetadataList().size();
                         intIndex++)
                        {
                        final Metadata metadata;

                        metadata = pluginMetadata.getMetadataList().get(intIndex);
                        metadata.setKey(KEY_INSTRUMENT + DOT + metadata.getKey());
                        }

                    addOrUpdateMetadataList(dao.getPluginMetadata(),
                                            pluginMetadata.getMetadataList());
                    boolSuccess = true;
                    }
                }
            }

        return (boolSuccess);
        }


    /***********************************************************************************************
     * Try to find the Metadata with the specified Key
     * in one of the containers attached to the Framework, Observatory, Instrument or DAO.
     * We could look directly in the collection of Instrument Aggregate Metadata
     * by using getAggregateMetadata() but this may not be up to date?
     * Return NULL if the Key is not found.
     *
     * @param framework
     * @param observatory
     * @param hostinstrument
     * @param key
     *
     * @return Metadata
     */

//    public static Metadata findMetadataSomewhere(final FrameworkPlugin framework,
//                                                 final ObservatoryInterface observatory,
//                                                 final ObservatoryInstrumentInterface hostinstrument,
//                                                 final String key)
//        {
//        final String SOURCE = "MetadataHelper.findMetadataSomewhere() ";
//        Metadata metadata;
//
//        metadata = null;
//
//        //------------------------------------------------------------------------------------------
//        // Look first in the Framework
//
//        if ((framework != null)
//            && (key != null)
//            && (!EMPTY_STRING.equals(key)))
//            {
//            // We have an Framework and a Key, so check FrameworkMetadata
//            metadata = getMetadataByKey(framework.getFrameworkMetadata(), key);
//            }
//
//        //------------------------------------------------------------------------------------------
//        // Now look in the Observatory
//
//        if ((metadata == null)
//            && (observatory != null)
//            && (key != null)
//            && (!EMPTY_STRING.equals(key)))
//            {
//            // We have an Observatory and a Key, so check ObservatoryMetadata and ObserverMetadata
//            metadata = getMetadataByKey(observatory.getObservatoryMetadata(), key);
//
//            if (metadata == null)
//                {
//                metadata = getMetadataByKey(observatory.getObserverMetadata(), key);
//                }
//            }
//
//        //------------------------------------------------------------------------------------------
//        // Now look in the Host Instrument original XML configuration, i.e. the schema
//
//        if ((metadata == null)
//            && (hostinstrument.getInstrument() != null)
//            && (key != null)
//            && (!EMPTY_STRING.equals(key)))
//            {
//            // InstrumentMetadata
//            metadata = getMetadataByKey(hostinstrument.getInstrument().getInstrumentMetadataList(), key);
//
//            // ControllerMetadata
//            if ((metadata == null)
//                && (hostinstrument.getInstrument().getController() != null))
//                {
//                metadata = getMetadataByKey(hostinstrument.getInstrument().getController().getControllerMetadataList(), key);
//                }
//
//            // Controller CommandMetadata
//            if ((metadata == null)
//                && (hostinstrument.getInstrument().getController() != null)
//                && (hostinstrument.getInstrument().getController().getCommandList() != null)
//                && (!hostinstrument.getInstrument().getController().getCommandList().isEmpty()))
//                {
//                final Iterator<CommandType> iterControllerCommands;
//
//                iterControllerCommands = hostinstrument.getInstrument().getController().getCommandList().iterator();
//
//                while ((metadata == null)
//                    && (iterControllerCommands.hasNext()))
//                    {
//                    final CommandType commandType;
//
//                    commandType = iterControllerCommands.next();
//
//                    if ((commandType != null)
//                        && (commandType.getCommandMetadataList() != null)
//                        && (!commandType.getCommandMetadataList().isEmpty()))
//                        {
//                        metadata = getMetadataByKey(commandType.getCommandMetadataList(), key);
//                        }
//                    }
//                }
//
//            // And all of the Instrument's Plugins Metadata
//            if ((metadata == null)
//                && (hostinstrument.getInstrument().getController() != null)
//                && (hostinstrument.getInstrument().getController().getPluginList() != null)
//                && (!hostinstrument.getInstrument().getController().getPluginList().isEmpty()))
//                {
//                final List<PluginType> listPlugins;
//
//                listPlugins = hostinstrument.getInstrument().getController().getPluginList();
//
//                for (int i = 0;
//                     ((metadata == null) && i < listPlugins.size());
//                     i++)
//                    {
//                    final PluginType plugin;
//
//                    plugin = listPlugins.get(i);
//
//                    if ((plugin != null)
//                        && (plugin.getPluginMetadataList() != null)
//                        && (!plugin.getPluginMetadataList().isEmpty()))
//                        {
//                        metadata = getMetadataByKey(plugin.getPluginMetadataList(), key);
//                        }
//
//                    // Plugin CommandMetadata
//                    if ((metadata != null)
//                        && (plugin != null)
//                        && (plugin.getCommandList() != null)
//                        && (!plugin.getCommandList().isEmpty()))
//                        {
//                        final Iterator<CommandType> iterPluginCommands;
//
//                        iterPluginCommands = plugin.getCommandList().iterator();
//
//                        while ((metadata != null)
//                            && (iterPluginCommands.hasNext()))
//                            {
//                            final CommandType commandType;
//
//                            commandType = iterPluginCommands.next();
//
//                            if ((commandType != null)
//                                && (commandType.getCommandMetadataList() != null)
//                                && (!commandType.getCommandMetadataList().isEmpty()))
//                                {
//                                metadata = getMetadataByKey(commandType.getCommandMetadataList(), key);
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//
//        //------------------------------------------------------------------------------------------
//        // Now try the DAO
//
//        if ((metadata == null)
//            && (hostinstrument != null)
//            && (hostinstrument.getDAO() != null)
//            && (key != null)
//            && (!EMPTY_STRING.equals(key)))
//            {
//            metadata = getMetadataByKey(hostinstrument.getDAO().getObservationMetadata(), key);
//
//            if (metadata == null)
//                {
//                metadata = getMetadataByKey(hostinstrument.getDAO().getEphemerisMetadata(), key);
//                }
//
//            if (metadata == null)
//                {
//                metadata = getMetadataByKey(hostinstrument.getDAO().getControllerMetadata(), key);
//                }
//
//            if (metadata == null)
//                {
//                metadata = getMetadataByKey(hostinstrument.getDAO().getPluginMetadata(), key);
//                }
//
//            if (metadata == null)
//                {
//                metadata = getMetadataByKey(hostinstrument.getDAO().getRawDataMetadata(), key);
//                }
//
//            if (metadata == null)
//                {
//                metadata = getMetadataByKey(hostinstrument.getDAO().getXYDatasetMetadata(), key);
//                }
//
//            if (metadata == null)
//                {
//                metadata = getMetadataByKey(hostinstrument.getDAO().getMetadata(), key);
//                }
//            }
//
//        //------------------------------------------------------------------------------------------
//        // If still not found, check the DAO Wrapper containers, if they exist
//
//        if ((metadata == null)
//            && (hostinstrument != null)
//            && (hostinstrument.getDAO() != null)
//            && (hostinstrument.getDAO().getWrappedData() != null)
//            && (key != null)
//            && (!EMPTY_STRING.equals(key)))
//            {
//            final DAOWrapperInterface wrapper;
//
//            wrapper = hostinstrument.getDAO().getWrappedData();
//
//            metadata = getMetadataByKey(wrapper.getObservationMetadata(), key);
//
//            if (metadata == null)
//                {
//                metadata = getMetadataByKey(wrapper.getEphemerisMetadata(), key);
//                }
//
//            if (metadata == null)
//                {
//                metadata = getMetadataByKey(wrapper.getControllerMetadata(), key);
//                }
//
//            if (metadata == null)
//                {
//                metadata = getMetadataByKey(wrapper.getPluginMetadata(), key);
//                }
//
//            if (metadata == null)
//                {
//                metadata = getMetadataByKey(wrapper.getRawDataMetadata(), key);
//                }
//
//            if (metadata == null)
//                {
//                metadata = getMetadataByKey(wrapper.getXYDatasetMetadata(), key);
//                }
//
//            if (metadata == null)
//                {
//                metadata = getMetadataByKey(wrapper.getMetadata(), key);
//                }
//            }
//
//        return (metadata);
//        }


    /**********************************************************************************************/
    /* Validators                                                                                 */
    /***********************************************************************************************
     * Indicate if the specified Metadata item is valid.
     * Check that the Metadata key, DataType and Units appear in the appropriate Dictionaries.
     * If Regex is supplied in the Metadata,
     * then this takes precedence over any Regex in the DataType definition.
     *
     * @param metadata
     *
     * @return boolean
     */

    public static boolean isValidMetadataItem(final Metadata metadata)
        {
        final String SOURCE = "MetadataHelper.isValidMetadataItem() ";
        boolean boolIsValid;
        final DataTypeDictionary dataType;
        final SchemaUnits.Enum unitsEnum;

        boolIsValid = false;

        // Check that the Metadata key, DataType and Units
        // appear in the appropriate Dictionaries
        dataType = DataTypeDictionary.getDataTypeDictionaryEntryForName(metadata.getDataTypeName().toString());
        unitsEnum = SchemaUnits.Enum.forString(metadata.getUnits().toString());

        // DataType and Units will have been checked by the above lookups
        if ((dataType != null)
            && (unitsEnum != null))
            {
            final int intFailures;
            final List<String> errors;

            errors = new ArrayList<String>(10);

            // If Regex is supplied in the Metadata,
            // then this takes precedence over any Regex in the DataType definition
            intFailures = DataTypeHelper.validateDataTypeOfMetadataValue(metadata.getValue(),
                                                                         DataTypeDictionary.getDataTypeDictionaryEntryForName(metadata.getDataTypeName().toString()),
                                                                         metadata.getRegex(),
                                                                         errors);
            if (intFailures == 0)
                {
                boolIsValid = true;
                }
            else
                {
                LOGGER.error(METADATA_TARGET_METADATA
                               + METADATA_ACTION_VALIDATE
                               + METADATA_RESULT + "The Metadata has an invalid Value" + TERMINATOR_SPACE
                               + "[key=" + metadata.getKey() + TERMINATOR_SPACE
                               + "[value=" + metadata.getValue() + TERMINATOR);
                LOGGER.errors(SOURCE, errors);
                }
            }
        else
            {
            LOGGER.error(METADATA_TARGET_METADATA
                           + METADATA_ACTION_VALIDATE
                           + METADATA_RESULT + "Metadata DataType or Units are invalid" + TERMINATOR_SPACE
                           + "[key=" + metadata.getKey() + TERMINATOR);
            }

        return (boolIsValid);
        }


    /***********************************************************************************************
     * Indicate if the DataTypeDictionary entry specified in the Metadata is consistent for use
     * with real data of type Double. Warn the user if not.
     *
     * @param datatype
     *
     * @return boolean
     */

    public static boolean isMetadataTypeDouble(final DataTypeDictionary datatype)
        {
        final String SOURCE = "MetadataHelper.isMetadataTypeDouble() ";
        final boolean boolConsistent;

        // Both Double and Float will work as a Double
        boolConsistent = (DataTypeDictionary.DECIMAL_DOUBLE.getName().equals(datatype.getName())
                            || DataTypeDictionary.DECIMAL_FLOAT.getName().equals(datatype.getName()));

        if (!boolConsistent)
            {
            LOGGER.warn(LOADER_PROPERTIES.isMetadataDebug(),
                        SOURCE + MSG_TYPE_MISMATCH + datatype.getName() + "] [datatype=double]");
            }

        return (boolConsistent);
        }


    /***********************************************************************************************
     * Indicate if the DataTypeDictionary entry specified in the Metadata is consistent for use
     * with real data of type Float. Warn the user if not.
     *
     * @param datatype
     *
     * @return boolean
     */

    public static boolean isMetadataTypeFloat(final DataTypeDictionary datatype)
        {
        final String SOURCE = "MetadataHelper.isMetadataTypeFloat() ";
        final boolean boolConsistent;

        // Both Double and Float will work as a Double
        boolConsistent = (DataTypeDictionary.DECIMAL_DOUBLE.getName().equals(datatype.getName())
                          || DataTypeDictionary.DECIMAL_FLOAT.getName().equals(datatype.getName()));

        if (!boolConsistent)
            {
            LOGGER.warn(LOADER_PROPERTIES.isMetadataDebug(),
                        SOURCE + MSG_TYPE_MISMATCH + datatype.getName() + "] [datatype=float]");
            }

        return (boolConsistent);
        }


    /***********************************************************************************************
     * Indicate if the DataType specified in the Metadata is consistent for use with real data
     * of type Integer. Warn the user if not.
     *
     * @param datatype
     *
     * @return boolean
     */

    public static boolean isMetadataTypeInteger(final DataTypeDictionary datatype)
        {
        final String SOURCE = "MetadataHelper.isMetadataTypeInteger() ";
        final boolean boolConsistent;

        boolConsistent = ((DataTypeDictionary.DECIMAL_INTEGER.getName().equals(datatype.getName()))
                            || (DataTypeDictionary.HEX_INTEGER.getName().equals(datatype.getName()))
                            || (DataTypeDictionary.SCALED_HEX_INTEGER.getName().equals(datatype.getName())));

        if (!boolConsistent)
            {
            LOGGER.warn(LOADER_PROPERTIES.isMetadataDebug(),
                        SOURCE + MSG_TYPE_MISMATCH + datatype.getName() + "] [data=integer]");
            }

        return (boolConsistent);
        }


    /**********************************************************************************************/
    /* Channel Interrogators                                                                      */
    //----------------------------------------------------------------------------------------------
    // Metadata Access
    //
    // Observation.Title
    // Observation.Channel.Value.n
    // Observation.Channel.Name.n
    // Observation.Channel.Colour.n
    // Observation.Channel.DataType.n
    // Observation.Channel.Units.n
    // Observation.Channel.Description.n
    // Observation.Axis.Label.X
    // Observation.Axis.Label.Y.n
    //
    /***********************************************************************************************
     * Get the Name of the specified Channel from the Metadata,
     * taking account of a Temperature channel if present.
     * Return NO_DATA if no Name is found.
     *
     * @param metadatalist
     * @param channelid
     * @param temperaturechannel
     *
     * @return String
     */

    public static String getChannelName(final List<Metadata> metadatalist,
                                        final int channelid,
                                        final boolean temperaturechannel)
        {
        final String SOURCE = "MetadataHelper.getChannelName() ";
        final String strChannelName;

        // Index:           0  1  2  3  4  5  6  7  8  ...
        // Label: Temperature  0  1  2  3  4  5  6  7  ...
        // Label:           0  1  2  3  4  5  6  7  8  ...

        if (temperaturechannel)
            {
            if (channelid == 0)
                {
                strChannelName = getMetadataValueByKey(metadatalist,
                                                       MetadataDictionary.KEY_OBSERVATION_CHANNEL_NAME_TEMPERATURE.getKey());
                }
            else
                {
                strChannelName = getMetadataValueByKey(metadatalist,
                                                       MetadataDictionary.KEY_OBSERVATION_CHANNEL_NAME.getKey() + (channelid -1));
                }
            }
        else
            {
            strChannelName = getMetadataValueByKey(metadatalist,
                                                   MetadataDictionary.KEY_OBSERVATION_CHANNEL_NAME.getKey() + channelid);
            }
        //System.out.println("lookup channel=" + channel + " temp=" + temperaturechannel + " [name=" + strChannelName + "]");

        return (strChannelName);
        }


    /***********************************************************************************************
     * Get the Value of the specified Channel from the Metadata,
     * taking account of a Temperature channel if present.
     * Return NO_DATA if no Value is found.
     *
     * @param metadatalist
     * @param channelid
     * @param temperaturechannel
     *
     * @return String
     */

    public static String getChannelValue(final List<Metadata> metadatalist,
                                         final int channelid,
                                         final boolean temperaturechannel)
        {
        final String SOURCE = "MetadataHelper.getChannelValue() ";
        final String strChannelValue;

        // Index:           0  1  2  3  4  5  6  7  8  ...
        // Label: Temperature  0  1  2  3  4  5  6  7  ...
        // Label:           0  1  2  3  4  5  6  7  8  ...

        if (temperaturechannel)
            {
            if (channelid == 0)
                {
                strChannelValue = getMetadataValueByKey(metadatalist,
                                                        MetadataDictionary.KEY_OBSERVATION_CHANNEL_VALUE_TEMPERATURE.getKey());
                }
            else
                {
                strChannelValue = getMetadataValueByKey(metadatalist,
                                                        MetadataDictionary.KEY_OBSERVATION_CHANNEL_VALUE.getKey() + (channelid -1));
                }
            }
        else
            {
            strChannelValue = getMetadataValueByKey(metadatalist,
                                                    MetadataDictionary.KEY_OBSERVATION_CHANNEL_VALUE.getKey() + channelid);
            }

        //System.out.println(SOURCE + "channel=" + channelid + " temp=" + temperaturechannel + " [value=" + strChannelValue + "]");

        return (strChannelValue);
        }


    /***********************************************************************************************
     * Get the DataTypeDictionary entry of the specified Channel from the Metadata,
     * taking account of a Temperature channel if present.
     * Return STRING if the DataType is not specified correctly.
     *
     * @param metadatalist
     * @param channelid
     * @param temperaturechannel
     *
     * @return DataTypeDictionary
     */

    public static DataTypeDictionary getChannelDataType(final List<Metadata> metadatalist,
                                                        final int channelid,
                                                        final boolean temperaturechannel)
        {
        final String SOURCE = "MetadataHelper.getChannelDataType() ";
        final String strDataTypeName;
        DataTypeDictionary dataType;

        // Index:           0  1  2  3  4  5  6  7  8  ...
        // Label: Temperature  0  1  2  3  4  5  6  7  ...
        // Label:           0  1  2  3  4  5  6  7  8  ...

        // The Metadata Value is the Name of the DataType as a String
        if (temperaturechannel)
            {
            if (channelid == 0)
                {
                strDataTypeName = getMetadataValueByKey(metadatalist,
                                                        MetadataDictionary.KEY_OBSERVATION_CHANNEL_DATA_TYPE_TEMPERATURE.getKey());
                }
            else
                {
                strDataTypeName = getMetadataValueByKey(metadatalist,
                                                        MetadataDictionary.KEY_OBSERVATION_CHANNEL_DATA_TYPE.getKey() + (channelid -1));
                }
            }
        else
            {
            strDataTypeName = getMetadataValueByKey(metadatalist,
                                                    MetadataDictionary.KEY_OBSERVATION_CHANNEL_DATA_TYPE.getKey() + channelid);
            }

        if ((strDataTypeName != null)
            && (!NO_DATA.equals(strDataTypeName)))
            {
            // Found the Name, so map to a DataType
            dataType = DataTypeDictionary.getDataTypeDictionaryEntryForName(strDataTypeName);
            }
        else
            {
            LOGGER.error(SOURCE + "Did not find DataType value by key [channelid=" + channelid + "]");
            dataType = null;
            }

        // Check that we know about this DataType
        if (dataType == null)
            {
            dataType = DataTypeDictionary.STRING;
            }

        return (dataType);
        }


    /***********************************************************************************************
     * Get the Units of the specified Channel from the Metadata,
     * taking account of a Temperature channel if present.
     * Return DIMENSIONLESS if the Units is not specified correctly.
     *
     * @param metadatalist
     * @param channelid
     * @param temperaturechannel
     *
     * @return SchemaUnits.Enum
     */

    public static SchemaUnits.Enum getChannelUnits(final List<Metadata> metadatalist,
                                                   final int channelid,
                                                   final boolean temperaturechannel)
        {
        final String SOURCE = "MetadataHelper.getChannelUnits() ";
        SchemaUnits.Enum unitsEnum;
        final String strUnits;

        // Index:           0  1  2  3  4  5  6  7  8  ...
        // Label: Temperature  0  1  2  3  4  5  6  7  ...
        // Label:           0  1  2  3  4  5  6  7  8  ...

        unitsEnum = SchemaUnits.DIMENSIONLESS;

        if (temperaturechannel)
            {
            if (channelid == 0)
                {
                strUnits = getMetadataValueByKey(metadatalist,
                                                 MetadataDictionary.KEY_OBSERVATION_CHANNEL_UNITS_TEMPERATURE.getKey());
                }
            else
                {
                strUnits = getMetadataValueByKey(metadatalist,
                                                 MetadataDictionary.KEY_OBSERVATION_CHANNEL_UNITS.getKey() + (channelid -1));
                }
            }
        else
            {
            strUnits = getMetadataValueByKey(metadatalist,
                                             MetadataDictionary.KEY_OBSERVATION_CHANNEL_UNITS.getKey() + channelid);
            }

        if ((strUnits != null)
            && (!NO_DATA.equals(strUnits)))
            {
            unitsEnum = SchemaUnits.Enum.forString(strUnits);
            }

        // Check that we know about these Units
        if (unitsEnum == null)
            {
            unitsEnum = SchemaUnits.DIMENSIONLESS;
            }

        return (unitsEnum);
        }


    /***********************************************************************************************
     * Get the Description of the specified Channel from the Metadata,
     * taking account of a Temperature channel if present.
     * Return NO_DATA if no Description is found.
     *
     * @param metadatalist
     * @param channelid
     * @param temperaturechannel
     *
     * @return String
     */

    public static String getChannelDescription(final List<Metadata> metadatalist,
                                               final int channelid,
                                               final boolean temperaturechannel)
        {
        final String SOURCE = "MetadataHelper.getChannelDescription() ";
        final String strChannelDescription;

        // Index:           0  1  2  3  4  5  6  7  8  ...
        // Label: Temperature  0  1  2  3  4  5  6  7  ...
        // Label:           0  1  2  3  4  5  6  7  8  ...

        if (temperaturechannel)
            {
            if (channelid == 0)
                {
                strChannelDescription = getMetadataValueByKey(metadatalist,
                                                              MetadataDictionary.KEY_OBSERVATION_CHANNEL_DESCRIPTION_TEMPERATURE.getKey());
                }
            else
                {
                strChannelDescription = getMetadataValueByKey(metadatalist,
                                                              MetadataDictionary.KEY_OBSERVATION_CHANNEL_DESCRIPTION.getKey() + (channelid -1));
                }
            }
        else
            {
            strChannelDescription = getMetadataValueByKey(metadatalist,
                                                          MetadataDictionary.KEY_OBSERVATION_CHANNEL_DESCRIPTION.getKey() + channelid);
            }

        return (strChannelDescription);
        }


    /***********************************************************************************************
     * Get the Colour of the specified Channel from the Metadata,
     * taking account of a Temperature channel if present.
     * Return the standard colour for the channel if not specified correctly.
     *
     * @param metadatalist
     * @param channelid
     * @param temperaturechannel
     *
     * @return ColourPlugin
     */

    public static ColourInterface getChannelColour(final List<Metadata> metadatalist,
                                                   final int channelid,
                                                   final boolean temperaturechannel)
        {
        final String SOURCE = "MetadataHelper.getChannelColour() ";
        ColourInterface colour;
        final String strColourRGB;

        // Index:           0  1  2  3  4  5  6  7  8  ...
        // Label: Temperature  0  1  2  3  4  5  6  7  ...
        // Label:           0  1  2  3  4  5  6  7  8  ...

        // Default to our standard Colours
        colour = ChartUIHelper.getStandardColour(channelid);

        if (temperaturechannel)
            {
            if (channelid == 0)
                {
                strColourRGB = getMetadataValueByKey(metadatalist,
                                                     MetadataDictionary.KEY_OBSERVATION_CHANNEL_COLOUR_TEMPERATURE.getKey());
                }
            else
                {
                strColourRGB = getMetadataValueByKey(metadatalist,
                                                     MetadataDictionary.KEY_OBSERVATION_CHANNEL_COLOUR.getKey() + (channelid -1));
                }
            }
        else
            {
            strColourRGB = getMetadataValueByKey(metadatalist,
                                                 MetadataDictionary.KEY_OBSERVATION_CHANNEL_COLOUR.getKey() + channelid);
            }

        try
            {
            if ((strColourRGB != null)
                && (!NO_DATA.equals(strColourRGB)))
                {
                colour = new ColourDataType(strColourRGB);
                }
            }

        catch (NumberFormatException exception)
            {
            // This is a real error that the User needs to know about
            LOGGER.error(SOURCE + "The channel colour was incorrectly specified in the Metadata "
                            + " [channel=" + channelid + "] [colour=" + strColourRGB + "]");
            }

        return (colour);
        }


    /**********************************************************************************************/
    /* Metadata                                                                                   */
    /***********************************************************************************************
     * Create a fully-specified item of Metadata.
     * This should be the ONLY place where Metadata are created in the Observatory!
     *
     * @param key
     * @param value
     * @param regex
     * @param datatype
     * @param units
     * @param description
     *
     * @return Metadata
     */

    public static Metadata createMetadata(final String key,
                                          final String value,
                                          final String regex,
                                          final DataTypeDictionary datatype,
                                          final SchemaUnits.Enum units,
                                          final String description)
        {
        final String SOURCE = "MetadataHelper.createMetadata() ";
        final Metadata metaData;

        // This should be the ONLY place where Metadata are created in the Observatory!
        metaData = Metadata.Factory.newInstance();

        // Allow the Value, Regex and Description to be an empty String, but not null
        if ((key != null)
            && (!EMPTY_STRING.equals(key))
            && (value != null)
            && (regex != null)
            && (datatype != null)
            && (units != null)
            && (description != null))
            {
            metaData.setKey(key);
            metaData.setValue(value);
            metaData.setRegex(regex);
            metaData.setDataTypeName(datatype.getSchemaDataType());
            metaData.setUnits(units);
            metaData.setDescription(description);
            }

        return (metaData);
        }


    /***********************************************************************************************
     * Add a fully-specified item of new metadata to a List of Metadata.
     *
     * @param metadatalist
     * @param key
     * @param value
     * @param regex
     * @param datatype
     * @param units
     * @param description
     *
     * @return Metadata
     */

    public static Metadata addNewMetadata(final List<Metadata> metadatalist,
                                          final String key,
                                          final String value,
                                          final String regex,
                                          final DataTypeDictionary datatype,
                                          final SchemaUnits.Enum units,
                                          final String description)
        {
        final String SOURCE = "MetadataHelper.addNewMetadata() ";
        final Metadata metaData;

        metaData = createMetadata(key,
                                  value,
                                  regex,
                                  datatype,
                                  units,
                                  description);

        // Did we get some valid Metadata?
        if ((metadatalist != null)
            && (key != null)
            && (key.equals(metaData.getKey())))
            {
            metadatalist.add(metaData);
            }

        return (metaData);
        }


    /**********************************************************************************************/
    /* Metadata List Handlers                                                                     */
    /***********************************************************************************************
     * Add all items of Metadata to the specified List if possible,
     * otherwise just update their values.
     *
     * @param metadatalist
     * @param metadataitems
     */

    public static void addOrUpdateMetadataList(final List<Metadata> metadatalist,
                                               final List<Metadata> metadataitems)
        {
        final String SOURCE = "MetadataHelper.addOrUpdateMetadataList() ";

        // It is ok if the metadatalist is empty
        if ((metadatalist != null)
            && (metadataitems != null)
            && (!metadataitems.isEmpty()))
            {
            final Iterator<Metadata> iterMetadataItems;

            iterMetadataItems = metadataitems.iterator();

            while (iterMetadataItems.hasNext())
                {
                final Metadata metadataItem;

                metadataItem = iterMetadataItems.next();
                addOrUpdateMetadataItemTraced(metadatalist,
                                              metadataItem,
                                              SOURCE,
                                              LOADER_PROPERTIES.isMetadataDebug());
                }
            }
        }


    /***********************************************************************************************
     * Add all items of Metadata to the specified List if possible,
     * otherwise just update their values.
     * Trace the source of the Metadata, for debugging.
     *
     * @param metadatalist
     * @param metadataitems
     * @param tracemessage
     * @param traceon
     */

    public static void addOrUpdateMetadataListTraced(final List<Metadata> metadatalist,
                                                     final List<Metadata> metadataitems,
                                                     final String tracemessage,
                                                     final boolean traceon)
        {
        final String SOURCE = "MetadataHelper.addOrUpdateMetadataListTraced() ";

        // It is ok if the metadatalist is empty
        if ((metadatalist != null)
            && (metadataitems != null)
            && (!metadataitems.isEmpty()))
            {
            final Iterator<Metadata> iterMetadataItems;

            iterMetadataItems = metadataitems.iterator();

            while (iterMetadataItems.hasNext())
                {
                final Metadata metadataItem;

                metadataItem = iterMetadataItems.next();
                addOrUpdateMetadataItemTraced(metadatalist,
                                              metadataItem,
                                              tracemessage,
                                              traceon);
                }
            }
        }


    /***********************************************************************************************
     * Add an existing item of Metadata to the specified List if it is not present,
     * otherwise just update its value.
     * Currently used only in SegmentTimeSeries.
     *
     * @param metadatalist
     * @param metadataitem
     */

    public static void addOrUpdateMetadataItem(final List<Metadata> metadatalist,
                                               final Metadata metadataitem)
        {
        final String SOURCE = "MetadataHelper.addOrUpdateMetadataItem() ";

        // It is ok if the metadatalist is empty
        if ((metadatalist != null)
            && (metadataitem != null)
            && (isValidMetadataItem(metadataitem)))
            {
            final Metadata existingMetadata;

            existingMetadata = getMetadataByKey(metadatalist, metadataitem.getKey());

            if (existingMetadata == null)
                {
                // The Key cannot be found, so add the new Metadata item
                metadatalist.add(metadataitem);

                showMetadata(metadataitem,
                             SOURCE + "Adding New Metadata [key=" + metadataitem.getKey()
                                + "] [value=" + metadataitem.getValue() + "]",
                             LOADER_PROPERTIES.isMetadataDebug());
                }
            else
                {
                final String strOldValue;

                strOldValue = existingMetadata.getValue();

                // Just update the existing value
                existingMetadata.setValue(metadataitem.getValue());

                showMetadata(existingMetadata,
                             SOURCE + "Updating Existing Metadata Value [key=" + metadataitem.getKey()
                                + "] [old_value=" + strOldValue
                                + "] [new_value=" + existingMetadata.getValue() + "]",
                             LOADER_PROPERTIES.isMetadataDebug());
                }
            }
        else
            {
            LOGGER.error(SOURCE + "Unable to handle Metadata item [list.isnull="
                         + (metadatalist == null)
                         + "] [item.isnull="
                         + (metadataitem == null) + "]");
            }
        }


    /***********************************************************************************************
     * Add an item of Metadata to the specified List if it is not present,
     * otherwise just update its value.
     * Trace the source of the Metadata, for debugging.
     *
     * @param metadatalist
     * @param metadataitem
     * @param tracemessage
     * @param traceon
     */

    public static void addOrUpdateMetadataItemTraced(final List<Metadata> metadatalist,
                                                     final Metadata metadataitem,
                                                     final String tracemessage,
                                                     final boolean traceon)
        {
        final String SOURCE = "MetadataHelper.addOrUpdateMetadataItemTraced() ";

        // It is ok if the metadatalist is empty
        if ((metadatalist != null)
            && (metadataitem != null)
            && (isValidMetadataItem(metadataitem)))
            {
            final Metadata existingMetadata;

            existingMetadata = getMetadataByKey(metadatalist, metadataitem.getKey());

            if (existingMetadata == null)
                {
                // The Key cannot be found, so add the new Metadata item
                metadatalist.add(metadataitem);

                showMetadata(metadataitem,
                             SOURCE + "Adding New Metadata [message=" + tracemessage
                                + "] [key=" + metadataitem.getKey()
                                + "] [value=" + metadataitem.getValue() + "]",
                             traceon);
                }
            else
                {
                // Just update the existing value, but only if it is changed
                if (existingMetadata.getValue() != null)
                    {
                    final String strOldValue;

                    strOldValue = existingMetadata.getValue();

                    if (strOldValue.equals(metadataitem.getValue()))
                        {
                        LOGGER.debug(traceon,
                                     SOURCE + "Metadata already present with same Value, no action taken [message=" + tracemessage
                                        + "] [key=" + metadataitem.getKey()
                                        + "] [value=" + metadataitem.getValue()
                                        + "]");
                        }
                    else
                        {
                        // Just update the existing value
                        existingMetadata.setValue(metadataitem.getValue());

                        showMetadata(metadataitem,
                                     SOURCE + "Updating Existing Metadata Value [message=" + tracemessage
                                         + "] [key=" + metadataitem.getKey()
                                         + "] [value.old=" + strOldValue
                                         + "] [value.new=" + metadataitem.getValue()
                                         + "]",
                                     traceon);
                        }
                    }
                else
                    {
                    LOGGER.error(SOURCE + "Metadata Value is NULL [message=" + tracemessage
                                    + "] [key=" + metadataitem.getKey() + "]");
                    }
                }
            }
        else
            {
            LOGGER.error(SOURCE + "Unable to handle Metadata item [message=" + tracemessage
                             + "] [list.isnull=" + (metadatalist == null)
                             + "] [item.isnull=" + (metadataitem == null)
                             + "] [valid=" + isValidMetadataItem(metadataitem) + "]");
            }
        }


    /***********************************************************************************************
     * Create a new item of Metadata and add to the specified List,
     * otherwise just update its value if it is already present in the List.
     * Return either the created or modified Metadata item, or NULL if there was an error.
     * Trace the source of the Metadata, for debugging.
     *
     * @param metadatalist
     * @param key
     * @param value
     * @param regex
     * @param datatype
     * @param units
     * @param description
     * @param tracemessage
     * @param traceon
     *
     * @return Metadata
     */

    public static Metadata createOrUpdateMetadataItemTraced(final List<Metadata> metadatalist,
                                                            final String key,
                                                            final String value,
                                                            final String regex,
                                                            final DataTypeDictionary datatype,
                                                            final SchemaUnits.Enum units,
                                                            final String description,
                                                            final String tracemessage,
                                                            final boolean traceon)
        {
        final String SOURCE = "MetadataHelper.createOrUpdateMetadataItemTraced() ";
        Metadata metaData;

        metaData = null;

        // It is ok if the metadatalist is empty, but it must not be null
        if (metadatalist != null)
            {
            final Metadata existingMetadata;

            // First check to see if the Key already exists in the List
            existingMetadata = getMetadataByKey(metadatalist, key);

            if (existingMetadata == null)
                {
                // The Key cannot be found, so create and add the new Metadata item
                metaData = createMetadata(key,
                                          value,
                                          regex,
                                          datatype,
                                          units,
                                          description);

                if (isValidMetadataItem(metaData))
                    {
                    metadatalist.add(metaData);

                    showMetadata(metaData,
                                 SOURCE + "Adding new Metadata " + tracemessage,
                                 traceon);
                    }
                else
                    {
                    // Return NULL, but don't change the incoming List
                    metaData = null;
                    LOGGER.error(SOURCE + "Metadata item incorrectly specified"
                                     + "  [key=" + key
                                     + "] [value=" + value
                                     + "] [regex=" + regex
                                     + "] [datatype=" + datatype
                                     + "] [units=" + units
                                     + "] [description=" + description + "]");
                    }
                }
            else
                {
                // Just update the existing value, but only if it is changed
                if (existingMetadata.getValue() != null)
                    {
                    if (existingMetadata.getValue().equals(value))
                        {
                        // Return the existing item, but don't change the incoming List
                        metaData = existingMetadata;

                        LOGGER.debug(traceon,
                                     SOURCE + "Metadata with identical Value already present, no action taken [key=" + key + "]");
                        }
                    else
                        {
                        // Change the item in the incoming List, but only if the Value is valid
                        if (setValueOnlyIfValid(existingMetadata, value))
                            {
                            // Return the existing item
                            metaData = existingMetadata;

                            showMetadata(metaData,
                                         SOURCE + "WARNING! Updating changed Metadata Value for existing key from " + tracemessage,
                                         traceon);
                            }
                        else
                            {
                            // Return NULL, but don't change the incoming List
                            metaData = null;
                            LOGGER.error(SOURCE + "Metadata value incorrectly specified"
                                         + "  [key=" + key
                                         + "] [value=" + value
                                         + "] [regex=" + regex
                                         + "] [datatype=" + datatype
                                         + "] [units=" + units
                                         + "] [description=" + description + "]");
                            }
                        }
                    }
                else
                    {
                    LOGGER.error(SOURCE + "Metadata Value is NULL, no action taken [key=" + key + "]");
                    }
                }
            }
        else
            {
            LOGGER.error(SOURCE + "Unable to handle Metadata item [list.isnull=true]");
            }

        return (metaData);
        }


        /***********************************************************************************************
        * Find the most appropriate Framework or DAO container for Metadata with the specified Key.
        * Return NULL if no suitable container is found.
        *
        * @param dao
        * @param metadatakey
        * @param tracemessage
        * @param traceon
        *
        * @return List<Metadata>
        */

    public static List<Metadata> findMetadataContainerByKeyTraced(final ObservatoryInstrumentDAOInterface dao,
                                                                  final String metadatakey,
                                                                  final String tracemessage,
                                                                  final boolean traceon)
        {
        final String SOURCE = "MetadataHelper.findMetadataContainerByKeyTraced() ";
        final List<Metadata> listMetadata;

        // BEWARE! isValidMetadataDictionaryKey() is very strict about what constitutes a valid Kay
        if ((metadatakey != null)
            && (!EMPTY_STRING.equals(metadatakey))
            && (MetadataDictionary.isValidMetadataDictionaryKey(metadatakey)))
            {
            // Work down the Metadata object model
            if ((metadatakey.startsWith(MetadataDictionary.KEY_FRAMEWORK_ROOT.getKey()))
                && (REGISTRY.getFramework() != null))
                {
                LOGGER.debug(traceon,
                             tracemessage + MetadataDictionary.KEY_FRAMEWORK_ROOT.getKey());
                listMetadata = REGISTRY.getFramework().getFrameworkMetadata();
                }

            else if (dao != null)
                {
                final String DAO = " DAO ";

                if (metadatakey.startsWith(MetadataDictionary.KEY_OBSERVATORY_ROOT.getKey()))
                    {
                    LOGGER.debug(traceon,
                                 tracemessage + DAO + MetadataDictionary.KEY_OBSERVATORY_ROOT.getKey());
                    listMetadata = dao.getCurrentObservatoryMetadata();
                    }
                else if (metadatakey.startsWith(MetadataDictionary.KEY_OBSERVER_ROOT.getKey()))
                    {
                    LOGGER.debug(traceon,
                                 tracemessage + DAO + MetadataDictionary.KEY_OBSERVER_ROOT.getKey());
                    listMetadata = dao.getCurrentObserverMetadata();
                    }
                else if (metadatakey.startsWith(MetadataDictionary.KEY_OBSERVATION_CHANNEL_ROOT.getKey()))
                    {
                    // ChannelID syntax was checked by the MetadataDictionary lookup
                    LOGGER.debug(traceon,
                                 tracemessage + DAO + MetadataDictionary.KEY_OBSERVATION_CHANNEL_ROOT.getKey());
                    listMetadata = dao.getObservationMetadata();
                    }
                else if (metadatakey.startsWith(MetadataDictionary.KEY_OBSERVATION_ROOT.getKey()))
                    {
                    LOGGER.debug(traceon,
                                 tracemessage + DAO + MetadataDictionary.KEY_OBSERVATION_ROOT.getKey());
                    listMetadata = dao.getObservationMetadata();
                    }
                else if (metadatakey.contains(MetadataDictionary.KEY_CONTROLLER_ROOT.getKey()))
                    {
                    LOGGER.debug(traceon,
                                 tracemessage + DAO + MetadataDictionary.KEY_CONTROLLER_ROOT.getKey());
                    listMetadata = dao.getControllerMetadata();
                    }
                else if (metadatakey.startsWith(MetadataDictionary.KEY_INSTRUMENT_ROOT.getKey()))
                    {
                    LOGGER.debug(traceon,
                                 tracemessage + DAO + MetadataDictionary.KEY_INSTRUMENT_ROOT.getKey());
                    listMetadata = dao.getInstrumentMetadata();
                    }
                else if (metadatakey.startsWith(MetadataDictionary.KEY_RSP_ROOT.getKey()))
                    {
                    // All RSP data are Observation attributes
                    LOGGER.debug(traceon,
                                 tracemessage + DAO + MetadataDictionary.KEY_RSP_ROOT.getKey());
                    listMetadata = dao.getObservationMetadata();
                    }
                else if (metadatakey.startsWith(MetadataDictionary.KEY_METADATA_ROOT.getKey()))
                    {
                    LOGGER.debug(traceon,
                                 tracemessage + DAO + MetadataDictionary.KEY_METADATA_ROOT.getKey());
                    listMetadata = dao.getMetadataMetadata();
                    }
                else
                    {
                    // Everything else ends up in Observation Metadata,
                    // but it won't get removed by removeMetadata(Observation)
                    // TODO Review Plugin Metadata
                    LOGGER.warn(SOURCE + DAO + "Adding unexpected Metadata to ObservationMetadata [key=" + metadatakey + "]");
                    listMetadata = dao.getObservationMetadata();
                    }
                }
            else
                {
                listMetadata = null;
                LOGGER.error(SOURCE +  "DAO is NULL");
                }
            }
        else
            {
            listMetadata = null;

            // This is probably too much information
            //LOGGER.error(SOURCE +  "Metadata Key is NULL or is not known to the MetadataDictionary [key=" + metadatakey + "]");

            if (!MetadataDictionary.isValidMetadataDictionaryKey(metadatakey))
                {
                LOGGER.error(SOURCE +  "Metadata Key is not in the MetadataDictionary, or has an invalid syntax [key=" + metadatakey + "]");
                }
            }

        return (listMetadata);
        }


    /***********************************************************************************************
     * Add the last timestamped Value of each Channel to the ObservationMetadata.
     *
     * @param dao
     */

    public static void addLastTimestampedValuesToAllChannels(final ObservatoryInstrumentDAOInterface dao)
        {
        final String SOURCE = "MetadataHelper.addLastTimestampedValuesToAllChannels() ";

        if ((dao != null)
            && (dao.getHostInstrument() != null)
            && (dao.getHostInstrument().getContext() != null)
            && (dao.getRawData() != null)
            && (dao.getObservationMetadata() != null))
            {
            final List<Metadata> listMetadata;

            // Combine the Metadata produced by the DAO with that from the Instrument and Observatory
            // The return value cannot be NULL, but it may be an empty List
            listMetadata = collectAggregateMetadataTraced(REGISTRY.getFramework(),
                                                          dao.getHostInstrument().getContext().getObservatory(),
                                                          dao.getHostInstrument(),
                                                          dao, dao.getWrappedData(),
                                                          SOURCE,
                                                          LOADER_PROPERTIES.isMetadataDebug());
            for (int intChannelIndex = 0;
                intChannelIndex < dao.getRawDataChannelCount();
                intChannelIndex++)
                {
                addLastTimestampedValueToMetadata(dao.getObservationMetadata(),
                                                  dao.getRawData(),
                                                  intChannelIndex,
                                                  dao.hasTemperatureChannel(),
                                                  getChannelDataType(listMetadata,
                                                                     intChannelIndex,
                                                                     dao.hasTemperatureChannel()),
                                                  getChannelUnits(listMetadata,
                                                                  intChannelIndex,
                                                                  dao.hasTemperatureChannel()),
                                                  getChannelName(listMetadata,
                                                                 intChannelIndex,
                                                                 dao.hasTemperatureChannel()));
                }
            }
        }


    /***********************************************************************************************
     * Add the latest Timestamped Double, Float or Integer value of the specified channel
     * to the specified Metadata List. The caller must validate the channel number,
     * and indicate if the first channel is the Temperature.
     * Other data types may follow...
     * Currently used in:
     *
     * ImportRawDataRemote.doImportGOESRemote()
     * CaptureCommandHelper.doIteratedDataCaptureCommand()
     * CaptureCommandHelper.doIteratedStaribusMultichannelDataCaptureCommand()
     * FilterHelper.filterCalendarisedRawDataAndTemperature()
     * FilterHelper.filterCalendarisedRawDataToTimeSeries()
     *
     * @param metadatalist
     * @param rawdata
     * @param channelid
     * @param temperaturechannel
     * @param datatype
     * @param unitsenum
     * @param description
     */

    public static void addLastTimestampedValueToMetadata(final List<Metadata> metadatalist,
                                                         final Vector<Object> rawdata,
                                                         final int channelid,
                                                         final boolean temperaturechannel,
                                                         final DataTypeDictionary datatype,
                                                         final SchemaUnits.Enum unitsenum,
                                                         final String description)
        {
        final String SOURCE = "MetadataHelper.addLastTimestampedValueToMetadata() ";

        if ((metadatalist != null)
            && (rawdata != null)
            && (!rawdata.isEmpty())
            && (rawdata.get(rawdata.size()-1) instanceof Vector)
            && (channelid >= 0)
            && (datatype != null)
            && (unitsenum != null))
            {
            final int intChannelIndexIntoVector;
            final String strKey;
            final Vector vecLastSample;

            // The Timestamped sample format in the Vector is one of:
            //
            //            0            1           2           3
            // <Calendar> <Temperature> <Channel0> [<Channel1> <Channel2> ...]
            //
            //            0           1           2
            // <Calendar> <Channel0> [<Channel1> <Channel2> ...]

            if (temperaturechannel)
                {
                if (channelid == 0)
                    {
                    intChannelIndexIntoVector = channelid + 1;
                    strKey = MetadataDictionary.KEY_OBSERVATION_CHANNEL_VALUE_TEMPERATURE.getKey();
                    }
                else
                    {
                    intChannelIndexIntoVector = channelid + 1;
                    strKey = MetadataDictionary.KEY_OBSERVATION_CHANNEL_VALUE.getKey() + (intChannelIndexIntoVector-2);
                    }
                }
            else
                {
                intChannelIndexIntoVector = channelid + 1;
                strKey = MetadataDictionary.KEY_OBSERVATION_CHANNEL_VALUE.getKey() + (intChannelIndexIntoVector-1);
                }

            // Read the most recent sample
            vecLastSample = (Vector)rawdata.get(rawdata.size()-1);

            if (vecLastSample != null)
                {
                // ToDo Review how data are stored!
                addChannelValueMetadata(metadatalist,
                                        strKey,
                                        vecLastSample,
                                        intChannelIndexIntoVector,
                                        datatype,
                                        unitsenum,
                                        description,
                                        SOURCE);
                }
            else
                {
                LOGGER.error(SOURCE + EXCEPTION_PARAMETER_NULL + " (Last data sample in Vector)");
                }
            }
        else
            {
            LOGGER.error(SOURCE + "Unable to Add Last Timestamped Value (faulty parameters)");
            }
        }


    /***********************************************************************************************
     * Add the last columnar Value of each Channel to the ObservationMetadata.
     *
     * @param dao
     */

    public static void addLastColumnarValuesToAllChannels(final ObservatoryInstrumentDAOInterface dao)
        {
        final String SOURCE = "MetadataHelper.addLastColumnarValuesToAllChannels() ";

        if ((dao != null)
            && (dao.getHostInstrument() != null)
            && (dao.getHostInstrument().getContext() != null)
            && (dao.getRawData() != null)
            && (dao.getObservationMetadata() != null))
            {
            final List<Metadata> listMetadata;

            // Combine the Metadata produced by the DAO with that from the Instrument and Observatory
            // The return value cannot be NULL, but it may be an empty List
            listMetadata = collectAggregateMetadataTraced(REGISTRY.getFramework(),
                                                          dao.getHostInstrument().getContext().getObservatory(),
                                                          dao.getHostInstrument(),
                                                          dao,
                                                          dao.getWrappedData(),
                                                          SOURCE,
                                                          LOADER_PROPERTIES.isMetadataDebug());
            for (int intChannelIndex = 0;
                 intChannelIndex < dao.getRawDataChannelCount();
                 intChannelIndex++)
                {
                addLastColumnarValueToMetadata(dao.getObservationMetadata(),
                                               dao.getRawData(),
                                               intChannelIndex,
                                               dao.hasTemperatureChannel(),
                                               getChannelDataType(listMetadata,
                                                                  intChannelIndex,
                                                                  dao.hasTemperatureChannel()),
                                               getChannelUnits(listMetadata,
                                                               intChannelIndex,
                                                               dao.hasTemperatureChannel()),
                                               getChannelName(listMetadata,
                                                              intChannelIndex,
                                                              dao.hasTemperatureChannel()));
                }
            }
        }


    /***********************************************************************************************
     * Add the latest Double, Float or Integer value of the specified channel
     * to the Instrument Metadata. The caller must validate the channel number,
     * and indicate if the first channel is the Temperature.
     * Other data types may follow...
     * Currently used only in DAOCommandHelper.doSteppedDataCaptureCommand().
     *
     * @param metadatalist
     * @param rawdata
     * @param channelid
     * @param temperaturechannel
     * @param datatype
     * @param unitsenum
     * @param description
     */

    public static void addLastColumnarValueToMetadata(final List<Metadata> metadatalist,
                                                      final Vector<Object> rawdata,
                                                      final int channelid,
                                                      final boolean temperaturechannel,
                                                      final DataTypeDictionary datatype,
                                                      final SchemaUnits.Enum unitsenum,
                                                      final String description)
        {
        final String SOURCE = "MetadataHelper.addLastColumnarValueToMetadata() ";

        if ((metadatalist != null)
            && (rawdata != null)
            && (!rawdata.isEmpty())
            && (rawdata.get(rawdata.size()-1) instanceof Vector)
            && (channelid >= 0)
            && (datatype != null)
            && (unitsenum != null))
            {
            final String strKey;
            final Vector vecLastSample;

            // The Indexed sample format in the Vector is one of:
            //
            // 0          1             2           3
            // <X-Value>  <Temperature> <Channel0> [<Channel1> <Channel2> ...]
            //
            // 0          1           2
            // <X-Value>  <Channel0> [<Channel1> <Channel2> ...]

            // We want the Channel Value, not the Index
            if (temperaturechannel)
                {
                if (channelid == 0)
                    {
                    strKey = MetadataDictionary.KEY_OBSERVATION_CHANNEL_VALUE_TEMPERATURE.getKey();
                    }
                else
                    {
                    strKey = MetadataDictionary.KEY_OBSERVATION_CHANNEL_VALUE.getKey() + (channelid-1);
                    }
                }
            else
                {
                strKey = MetadataDictionary.KEY_OBSERVATION_CHANNEL_VALUE.getKey() + channelid;
                }

            // Read the most recent sample
            vecLastSample = (Vector)rawdata.get(rawdata.size()-1);

            if (vecLastSample != null)
                {
                // ToDo Review how data are stored!
                // We want the Channel Value, not the Index, so skip over...
                addChannelValueMetadata(metadatalist,
                                        strKey,
                                        vecLastSample,
                                        (channelid + 1),
                                        datatype,
                                        unitsenum,
                                        description,
                                        SOURCE);
                }
            else
                {
                LOGGER.error(SOURCE + EXCEPTION_PARAMETER_NULL + " (Last data sample in Vector)");
                }
            }
        }


    /***********************************************************************************************
     * Add the last Double, Float or Integer Channel Value to the specified Metadata list.
     * Check the datatype of the specified channel data,
     * log an error if the type is not consistent with the Metadata type.
     * Attempt to update an existing Metadata Item,
     * or create a new one if the Key cannot be found in the specified List.
     *
     * @param metadatalist
     * @param key
     * @param channels
     * @param channelid
     * @param datatype
     * @param unitsenum
     * @param description
     * @param source
     */

    private static void addChannelValueMetadata(final List<Metadata> metadatalist,
                                                final String key,
                                                final Vector channels,
                                                final int channelid,
                                                final DataTypeDictionary datatype,
                                                final SchemaUnits.Enum unitsenum,
                                                final String description,
                                                final String source)
        {
        final String SOURCE = "MetadataHelper.addChannelValueMetadata() ";

        if ((metadatalist != null)
            && (channels != null)
            && (channels.size() > channelid))
            {
            // Convert only the supported numeric DataTypes to Strings for the Metadata Value
            if (channels.get(channelid) instanceof Double)
                {
                // Channel data are usually stored as Double, but may have come from a Float or Integer
                // Don't change the original Metadata configuration if so
                if ((isMetadataTypeDouble(datatype))
                    || (isMetadataTypeFloat(datatype))
                    || (isMetadataTypeInteger(datatype)))
                    {
                    final double dblSample;

                    dblSample = (Double) channels.get(channelid);

                    // Attempt to update an existing Metadata Item,
                    // or create a new one if the Key cannot be found in the specified List
                    updateMetadataValueTraced(metadatalist,
                                              key,
                                              Double.toString(dblSample),
                                              datatype.getRegex(),
                                              datatype,
                                              unitsenum,
                                              description,
                                              source + "Double data",
                                              LOADER_PROPERTIES.isMetadataDebug());
                    }
                else
                    {
                    LOGGER.warn(source + "Numeric data not consistent with Metadata DataType [type=" + datatype.getName() + "]");
                    }
                }
            else if (channels.get(channelid) instanceof Float)
                {
                // Channel data are usually stored as Double, but may have come from a Float or Integer
                // Check for the reverse, just in case :-)
                if ((isMetadataTypeDouble(datatype))
                    || (isMetadataTypeFloat(datatype))
                    || (isMetadataTypeInteger(datatype)))
                    {
                    final float floatSample;

                    floatSample = (Float) channels.get(channelid);

                    // Attempt to update an existing Metadata Item,
                    // or create a new one if the Key cannot be found in the specified List
                    updateMetadataValueTraced(metadatalist,
                                              key,
                                              Float.toString(floatSample),
                                              datatype.getRegex(),
                                              datatype,
                                              unitsenum,
                                              description,
                                              source + "Float data",
                                              LOADER_PROPERTIES.isMetadataDebug());
                    }
                else
                    {
                    LOGGER.warn(source + "Numeric data not consistent with Metadata DataType [type=" + datatype.getName() + "]");
                    }
                }
            else if (channels.get(channelid) instanceof Integer)
                {
                // Integers really should be Integers
                // ToDo Review - The Integer Radix is unknown, but does that matter??
                if (isMetadataTypeInteger(datatype))
                    {
                    final int intSample;
                    final String strValue;

                    intSample = (Integer) channels.get(channelid);

                    // Attempt to update an existing Metadata Item,
                    // or create a new one if the Key cannot be found in the specified List
                    // 2014-05-15 We must use the DataType Radix here!
                    // The Value DataType must be the same as the Channel DataType

                    if (datatype.getRadix() == 10)
                        {
                        strValue = Integer.toString(intSample, 10);
                        }
                    else if (datatype.getRadix() == 16)
                        {
                        strValue = Utilities.intToFourHexString(intSample);
                        }
                    else
                        {
                        LOGGER.error(SOURCE + "Unsupported Integer Radix [radix=" + datatype.getRadix() + "]");
                        strValue = "0";
                        }

                    updateMetadataValueTraced(metadatalist,
                                              key,
                                              strValue,
                                              datatype.getRegex(),
                                              datatype,
                                              unitsenum,
                                              description,
                                              source + "Integer data",
                                              LOADER_PROPERTIES.isMetadataDebug());
                    }
                else
                    {
                    LOGGER.warn(source + "Integer data not consistent with Metadata DataType [type=" + datatype.getName() + "]");
                    }
                }
            else
                {
                LOGGER.error(source + " Unsupported Metadata Numeric DataType (Should be Double, Float or Integer) [data_class="
                                 + channels.get(channelid).getClass().getName() + "]");
                }
            }
        else
            {
            LOGGER.error(SOURCE + EXCEPTION_PARAMETER_NULL + " or inconsistent Channel count");
            }
        }


    /***********************************************************************************************
     * Attempt to update an existing Metadata Item,
     * or create a new one if the Key cannot be found in the specified List.
     *
     * @param metadatalist
     * @param key
     * @param value
     * @param regex
     * @param datatype
     * @param unitsenum
     * @param description
     * @param tracemessage
     * @param traceon
     */

    public static void updateMetadataValueTraced(final List<Metadata> metadatalist,
                                                 final String key,
                                                 final String value,
                                                 final String regex,
                                                 final DataTypeDictionary datatype,
                                                 final SchemaUnits.Enum unitsenum,
                                                 final String description,
                                                 final String tracemessage,
                                                 final boolean traceon)
        {
        final String SOURCE = "MetadataHelper.updateMetadataValueTraced() ";
        Metadata metaData;

        // Do we already have this Metadata item in the specified List?
        metaData = getMetadataByKey(metadatalist, key);

        if (metaData != null)
            {
            // Found the Key, so just update the Value
            metaData.setValue(value);

            showMetadata(metaData,
                         SOURCE + "Updating Metadata for existing key from " + tracemessage,
                         traceon);

            }
        else
            {
            // Key not found, so create a new item using the details supplied
            metaData = createMetadata(key,
                                      value,
                                      regex,
                                      datatype,
                                      unitsenum,
                                      description);
            metadatalist.add(metaData);

            showMetadata(metaData,
                         SOURCE + "Adding new Metadata " + tracemessage,
                         traceon);
            }
        }


    /***********************************************************************************************
     * Get an item of Metadata, given its Key.
     * Return null if the key cannot be found in the List.
     *
     * @param metadatalist
     * @param key
     *
     * @return Metadata
     */

    public static Metadata getMetadataByKey(final List<Metadata> metadatalist,
                                            final String key)
        {
        final String SOURCE = "MetadataHelper.getMetadataByKey() ";
        Metadata metaData;

        metaData = null;

        if ((metadatalist != null)
            && (!metadatalist.isEmpty())
            && (key != null)
            && (!EMPTY_STRING.equals(key)))
            {
            final Iterator<Metadata> iterMetadata;

            iterMetadata = metadatalist.iterator();

            // Just iterate over all Metadata (there are unlikely to be many...)
            while ((iterMetadata.hasNext())
                && (metaData == null))
                {
                final Metadata metadata;

                metadata = iterMetadata.next();

                if ((metadata != null)
                    && (key.equals(metadata.getKey())))
                    {
                    metaData = metadata;
                    }
                }
            }

        return (metaData);
        }


    /***********************************************************************************************
     * Get the Value of an item of Metadata, given its Key.
     * Return NO_DATA if the key cannot be found in the List.
     *
     * @param metadatalist
     * @param key
     *
     * @return String
     */

    public static String getMetadataValueByKey(final List<Metadata> metadatalist,
                                               final String key)
        {
        final String SOURCE = "MetadataHelper.getMetadataValueByKey() ";

        String strValue;
        final Metadata metaData;

        strValue = NO_DATA;

        // Return NULL if the key cannot be found in the List, or the List is NULL
        metaData = getMetadataByKey(metadatalist, key);

        if (metaData != null)
            {
            strValue = metaData.getValue();
            }

        return (strValue);
        }


    /***********************************************************************************************
     * Set the Value of the specified Metadata item, but only if it will result in a valid item.
     * i.e. the DataType, Units and Regex validation will all be checked.
     * Return a flag indicating if the value was changed.
     *
     * @param metadata
     * @param value
     *
     * @return boolean
     */

    public static boolean setValueOnlyIfValid(final Metadata metadata,
                                              final String value)
        {
        boolean boolSuccess;

        boolSuccess = false;

        if ((metadata != null)
            && (value != null))
            {
            final String strRevertValue;

            strRevertValue = metadata.getValue();

            // Try the new Value
            metadata.setValue(value);

            boolSuccess = isValidMetadataItem(metadata);

            // If it failed, revert the Value
            if (!boolSuccess)
                {
                metadata.setValue(strRevertValue);
                }
            }

        return (boolSuccess);
        }


    /***********************************************************************************************
     * Add the Parameter Values in a Command to the specified Metadata List.
     * This is currently called to put the Parameters into the <b>Wrapper InstrumentMetadata only</b>.
     *
     * @param metadatalist
     * @param obsinstrument
     * @param instrument
     * @param module
     * @param command
     */

    public static void addParameterValuesToMetadata(final List<Metadata> metadatalist,
                                                    final ObservatoryInstrumentInterface obsinstrument,
                                                    final Instrument instrument,
                                                    final XmlObject module,
                                                    final CommandType command)
        {
        final String SOURCE = "MetadataHelper.addParameterValuesToMetadata() ";

        if ((metadatalist != null)
            && (command != null))
            {
            final List<ParameterType> listParameters;

            listParameters = command.getParameterList();

            if ((listParameters != null)
                && (!listParameters.isEmpty()))
                {
                for (int i = 0;
                     i < listParameters.size();
                     i++)
                    {
                    final ParameterType parameter;

                    parameter = listParameters.get(i);

                    if (parameter != null)
                        {
                        final String strKey;
                        final Metadata metadataItem;

                        // Form the Key of the Metadata we are looking for
                        strKey = buildParameterKey(obsinstrument, instrument, module, command, parameter);

                        if ((strKey != null)
                            && (!EMPTY_STRING.equals(strKey)))
                            {
                            // See if we already have this item of Metadata
                            // If so, just update its Value
                            metadataItem = getMetadataByKey(metadatalist, strKey);

                            if (metadataItem != null)
                                {
                                metadataItem.setValue(parameter.getValue());

                                showMetadata(metadataItem,
                                             SOURCE + "Updating Metadata with Parameter Value [key=" + strKey + "] [value=" + parameter.getValue() + "]",
                                             LOADER_PROPERTIES.isMetadataDebug());
                                }
                            else
                                {
                                final String strRegex;

                                if (parameter.getRegex() != null)
                                    {
                                    strRegex = parameter.getRegex();
                                    }
                                else
                                    {
                                    strRegex = REGEX_NONE;
                                    }

                                // We need to add a new item in the list
                                addNewMetadata(metadatalist,
                                               strKey,
                                               parameter.getValue(),
                                               strRegex,
                                               DataTypeDictionary.getDataTypeDictionaryEntryForName(parameter.getInputDataType().getDataTypeName().toString()),
                                               parameter.getUnits(),
                                               parameter.getTooltip());

                                showMetadata(metadataItem,
                                             SOURCE + "Adding New Metadata for Parameter Value [key=" + strKey + "] [value=" + parameter.getValue() + "]",
                                             LOADER_PROPERTIES.isMetadataDebug());
                                }
                            }
                        else
                            {
                            LOGGER.error(SOURCE + EXCEPTION_PARAMETER_INVALID);
                            }
                        }
                    else
                        {
                        LOGGER.error(SOURCE + EXCEPTION_PARAMETER_NULL);
                        }
                    }
                }
            }
        else
            {
            LOGGER.error(SOURCE + EXCEPTION_PARAMETER_NULL);
            }
        }

    /***********************************************************************************************
     * Build the Parameter Key for the specified context.
     *
     * @param hostinstrument
     * @param instrument
     * @param module
     * @param command
     * @param parameter
     *
     * @return String
     */

    private static String buildParameterKey(final ObservatoryInstrumentInterface hostinstrument,
                                            final Instrument instrument,
                                            final XmlObject module,
                                            final CommandType command,
                                            final ParameterType parameter)
        {
        final String SOURCE = "MetadataHelper.buildParameterKey() ";

        final StringBuffer buffer;

        buffer = new StringBuffer();

        if ((hostinstrument != null)
            && (instrument != null)
            && (module != null)
            && (command != null)
            && (parameter != null))
            {
            if (module instanceof Controller)
                {
                buffer.append(KEY_INSTRUMENT);
                buffer.append(DOT);
                buffer.append(((Controller)module).getIdentifier());
                buffer.append(DOT);
                buffer.append(parameter.getName());
                }
            else if (module instanceof PluginType)
                {
                buffer.append(KEY_INSTRUMENT);
                buffer.append(DOT);
                buffer.append(((PluginType)module).getIdentifier());
                buffer.append(DOT);
                buffer.append(parameter.getName());
                }
            }

        return (buffer.toString());
        }


    /***********************************************************************************************
     * Sort the specified List of Metadata by their Keys, and return the sorted Keys.
     *
     * @param metadatalist
     *
     * @return List<String>
     */

    public static List<String> sortMetadataByKeys(final List<Metadata> metadatalist)
        {
        final List<String> listKeys;

        listKeys = new ArrayList<String>(metadatalist.size());

        if (!metadatalist.isEmpty())
            {
            for (int i = 0;
                 i < metadatalist.size();
                 i++)
                {
                final Metadata metadata;
                final String strKey;

                metadata = metadatalist.get(i);
                strKey = metadata.getKey();
                listKeys.add(strKey);
                }

            // Sort the Keys alphabetically to improve the readability
            Collections.sort(listKeys);
            }

        return (listKeys);
        }


    /**********************************************************************************************/
    /* Utilities                                                                                  */
    /***********************************************************************************************
     * Alphabetically sort a List of Metadata by their Keys.
     *
     * @param metadatalist
     *
     * @return List<Metadata>
     */

    public static List<String> sortMetadataByKey(final List<Metadata> metadatalist)
        {
        final String SOURCE = "MetadataHelper.sortMetadataByKey() ";
        final List<String> listKeys;

        listKeys = new ArrayList<String>(metadatalist.size());

        if (!metadatalist.isEmpty())
            {
            for (int i = 0;
                 i < metadatalist.size();
                 i++)
                {
                final Metadata md;
                final String strKey;

                md = metadatalist.get(i);
                strKey = md.getKey();
                listKeys.add(strKey);
                }

            // Sort the Keys alphabetically to improve the readability
            Collections.sort(listKeys);
            }

        return (listKeys);
        }


    /***********************************************************************************************
     * Show all of the Metadata in a DAO Wrapper for debugging purposes.
     *
     * @param wrapper
     * @param title
     * @param debug
     */

    public static void showWrapperMetadata(final DAOWrapperInterface wrapper,
                                           final String title,
                                           final boolean debug)
        {
        final String SOURCE = "MetadataHelper.showWrapperMetadata() ";

        if (debug)
            {
            if (wrapper != null)
                {
                LOGGER.log(Logger.CONSOLE_SEPARATOR_MAJOR);
                LOGGER.log(title);
                showMetadataList(wrapper.getMetadataMetadata(), "MetadataMetadata", debug);
                showMetadataList(wrapper.getObservationMetadata(), "ObservationMetadata", debug);
                showMetadataList(wrapper.getInstrumentMetadata(), "InstrumentMetadata", debug);
                showMetadataList(wrapper.getControllerMetadata(), "ControllerMetadata", debug);
                showMetadataList(wrapper.getPluginMetadata(), "PluginMetadata", debug);
                showMetadataList(wrapper.getRawDataMetadata(), "RawDataMetadata", debug);
                showMetadataList(wrapper.getXYDatasetMetadata(), "XYDatasetMetadata", debug);
                }
            else
                {
                LOGGER.error(SOURCE + "Wrapper was NULL");
                }
            }
        }


    /***********************************************************************************************
     * Show all of the Metadata in a DAO for debugging purposes.
     *
     * @param dao
     * @param title
     * @param debug
     */

    public static void showDAOMetadata(final ObservatoryInstrumentDAOInterface dao,
                                       final String title,
                                       final boolean debug)
        {
        final String SOURCE = "MetadataHelper.showDAOMetadata() ";

        if (debug)
            {
            if (dao != null)
                {
                LOGGER.log(Logger.CONSOLE_SEPARATOR_MAJOR);
                LOGGER.log(title);
                showMetadataList(dao.getMetadataMetadata(), "MetadataMetadata", debug);
                showMetadataList(dao.getObservationMetadata(), "ObservationMetadata", debug);
                showMetadataList(dao.getInstrumentMetadata(), "InstrumentMetadata", debug);
                showMetadataList(dao.getControllerMetadata(), "ControllerMetadata", debug);
                showMetadataList(dao.getPluginMetadata(), "PluginMetadata", debug);
                showMetadataList(dao.getRawDataMetadata(), "RawDataMetadata", debug);
                showMetadataList(dao.getXYDatasetMetadata(), "XYDatasetMetadata", debug);
                }
            else
                {
                LOGGER.error(SOURCE + "DAO was NULL");
                }
            }
        }


    /***********************************************************************************************
     * Show a List of Metadata for debugging purposes.
     *
     * @param metadatalist
     * @param title
     * @param debug
     */

    public static void showMetadataList(final List<Metadata> metadatalist,
                                        final String title,
                                        final boolean debug)
        {
        final String SOURCE = "MetadataHelper.showMetadataList() ";

        if (debug)
            {
            final StringBuffer buffer;

            buffer = new StringBuffer();

            if (metadatalist != null)
                {
                final Iterator<Metadata> iterMetadata;

                buffer.append(Logger.CONSOLE_SEPARATOR);
                buffer.append("\n");
                buffer.append(title);

                if (!metadatalist.isEmpty())
                    {
                    iterMetadata = metadatalist.iterator();

                    while (iterMetadata.hasNext())
                        {
                        final Metadata metadata;

                        metadata = iterMetadata.next();

                        if (metadata != null)
                            {
                            buffer.append("\n    ");
                            buffer.append(metadata.getKey());
                            buffer.append(" = ");
                            buffer.append(metadata.getValue());
                            buffer.append("  [units=");
                            buffer.append(metadata.getUnits().toString());
                            buffer.append("] [datatype=");
                            buffer.append(metadata.getDataTypeName().toString());
                            buffer.append("]");
                            }
                        }
                    }
                else
                    {
                    buffer.append("    No Metadata in this category");
                    }

                buffer.append(Logger.CONSOLE_SEPARATOR);
                }
//            else
//                {
//                LOGGER.error(SOURCE + "Metadata List was NULL");
//                }

            LOGGER.log(buffer.toString());
            }
        }


    /***********************************************************************************************
     * Show an item of Metadata for debugging.
     *
     * @param metadata
     * @param message
     * @param debug
     */

    public static void showMetadata(final Metadata metadata,
                                    final String message,
                                    final boolean debug)
        {
        final String SOURCE = "MetadataHelper.showMetadata() ";

        if ((metadata != null)
            && (debug))
            {
            LOGGER.log(message);
            LOGGER.log("    [key=" + metadata.getKey() + "]");
            LOGGER.log("    [value=" + metadata.getValue() + "]");
            LOGGER.log("    [regex=" + metadata.getRegex() + "]");
            LOGGER.log("    [datatype=" + metadata.getDataTypeName() + "]");
            LOGGER.log("    [units=" + metadata.getUnits().toString() + "]");
            LOGGER.log("    [description=" + metadata.getDescription() + "]");
            }
        }


    /***********************************************************************************************
     * Get a deep copy of the specified Plugin MetadataList.
     *
     * @param plugin
     *
     * @return List<Metadata>
     */

    public static List<Metadata> getCopyOfPluginMetadataList(final PluginType plugin)
        {
        final List<Metadata> listCopiedPluginMetadata;

        if ((plugin != null)
            && (plugin.getPluginMetadataList() != null)
            && (!plugin.getPluginMetadataList().isEmpty()))
            {
            final List<Metadata> listPluginMetadata;

            listPluginMetadata = ((PluginType)plugin.copy()).getPluginMetadataList();
            listCopiedPluginMetadata = new ArrayList<Metadata>(listPluginMetadata.size());

            // We need a deep copy of the Metadata
            // Not too sure if this is really necessary....
            for (int intMetadataIndex = 0;
                 intMetadataIndex < listPluginMetadata.size();
                 intMetadataIndex++)
                {
                final Metadata metadata;

                metadata = listPluginMetadata.get(intMetadataIndex);

                listCopiedPluginMetadata.add((Metadata)metadata.copy());
                }
            }
        else
            {
            listCopiedPluginMetadata = new ArrayList<Metadata>(1);
            }

        return (listCopiedPluginMetadata);
        }


    /***********************************************************************************************
     * Get a deep copy of the specified MetadataList.
     * Not sure if the is really necessary!
     *
     * @param metadatalist
     *
     * @return List<Metadata>
     */

    public static List<Metadata> getCopyOfMetadataList(final List<Metadata> metadatalist)
        {
        final List<Metadata> listCopiedMetadata;

        if ((metadatalist != null)
            && (!metadatalist.isEmpty()))
            {
            listCopiedMetadata = new ArrayList<Metadata>(metadatalist.size());

            // We need a deep copy of the Metadata
            for (int intMetadataIndex = 0;
                 intMetadataIndex < metadatalist.size();
                 intMetadataIndex++)
                {
                final Metadata metadata;

                metadata = metadatalist.get(intMetadataIndex);

                listCopiedMetadata.add((Metadata) metadata.copy());
                }
            }
        else
            {
            listCopiedMetadata = new ArrayList<Metadata>(1);
            }

        return (listCopiedMetadata);
        }


    /***********************************************************************************************
     * Reload the default Observatory Metadata from the XML files,
     * but only if the Metadata containers are empty.
     *
     * @param obsinstrument
     * @param traceon
     *
     * @return boolean
     *
     * @throws XmlException
     * @throws IOException
     */

    public static boolean reloadObservatoryDefaultMetadata(final ObservatoryInstrumentInterface obsinstrument,
                                                           final boolean traceon) throws XmlException, IOException
        {
        boolean boolLoaded;

        boolLoaded = false;

        if ((obsinstrument != null)
            && (obsinstrument.getContext() != null)
            && (obsinstrument.getContext().getObservatory() != null)
            && (!obsinstrument.getContext().getObservatory().areObservatoryMetadataLoaded()))
            {
            // Now reload the above from the default file
            // If these don't exist, the User will soon realise
            boolLoaded = importObservatoryMetadata(MetadataType.OBSERVATORY,
                                                   obsinstrument.getContext().getObservatory(),
                                                   traceon);

            obsinstrument.getContext().getObservatory().setObservatoryMetadataLoaded(boolLoaded);
            }

        return (boolLoaded);
        }


    /***********************************************************************************************
     * Reload the default Observer Metadata from the XML files,
     * but only if the Metadata containers are empty.
     *
     * @param obsinstrument
     * @param traceon
     *
     * @return boolean
     *
     * @throws XmlException
     * @throws IOException
     */

    public static boolean reloadObserverDefaultMetadata(final ObservatoryInstrumentInterface obsinstrument,
                                                        final boolean traceon) throws XmlException, IOException
        {
        boolean boolLoaded;

        boolLoaded = false;

        if ((obsinstrument != null)
            && (obsinstrument.getContext() != null)
            && (obsinstrument.getContext().getObservatory() != null)
            && (!obsinstrument.getContext().getObservatory().areObserverMetadataLoaded()))
            {
            // Now reload the above from the default file
            // If these don't exist, the User will soon realise
            boolLoaded = importObserverMetadata(MetadataType.OBSERVER,
                                                obsinstrument.getContext().getObservatory(),
                                                traceon);

            obsinstrument.getContext().getObservatory().setObserverMetadataLoaded(boolLoaded);
            }

        return (boolLoaded);
        }


    /***********************************************************************************************
     * Add Column Metadata.
     * Intended for export of reports.
     *
     * @param metadatalist
     * @param name
     * @param regex
     * @param datatype
     * @param units
     * @param description
     */

    public static void addColumnMetadata(final List<Metadata> metadatalist,
                                         final String name,
                                         final String regex,
                                         final DataTypeDictionary datatype,
                                         final SchemaUnits.Enum units,
                                         final String description)
        {
        addNewMetadata(metadatalist,
                       MetadataDictionary.KEY_COLUMN_NAME.getKey() + name,
                       name,
                       REGEX_STRING,
                       DataTypeDictionary.STRING,
                       SchemaUnits.DIMENSIONLESS,
                       "The name of the " + name + " column");

        addNewMetadata(metadatalist,
                       MetadataDictionary.KEY_COLUMN_REGEX.getKey() + name,
                       regex,
                       REGEX_STRING,
                       DataTypeDictionary.STRING,
                       SchemaUnits.DIMENSIONLESS,
                       "The Regex of the " + name + " column");

        addNewMetadata(metadatalist,
                       MetadataDictionary.KEY_COLUMN_DATATYPE.getKey() + name,
                       datatype.toString(),
                       REGEX_NONE,
                       DataTypeDictionary.DATA_TYPE,
                       SchemaUnits.DIMENSIONLESS,
                       "The DataType of the " + name + " column");

        addNewMetadata(metadatalist,
                       MetadataDictionary.KEY_COLUMN_UNITS.getKey() + name,
                       units.toString(),
                       REGEX_NONE,
                       DataTypeDictionary.UNITS,
                       SchemaUnits.DIMENSIONLESS,
                       "The Units of the " + name + " column");

        addNewMetadata(metadatalist,
                       MetadataDictionary.KEY_COLUMN_DESCRIPTION.getKey() + name,
                       description,
                       REGEX_STRING,
                       DataTypeDictionary.STRING,
                       SchemaUnits.DIMENSIONLESS,
                       "The Description of the " + name + " column");
        }
    }
