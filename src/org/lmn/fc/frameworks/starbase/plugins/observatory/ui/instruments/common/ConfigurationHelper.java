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


import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.model.resources.ResourcePlugin;
import org.lmn.fc.model.root.RootPlugin;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;
import org.lmn.fc.ui.reports.ReportIcon;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;


/***************************************************************************************************
 * ConfigurationHelper.
 */

public final class ConfigurationHelper implements FrameworkConstants,
                                                  FrameworkStrings,
                                                  FrameworkMetadata,
                                                  FrameworkSingletons
    {
    public static final int CONFIG_DATA_SIZE = 4;
    private static final int COLUMN_INDEX_ICON      = 0;
    private static final int COLUMN_INDEX_UPDATED   = 1;
    private static final int COLUMN_INDEX_KEY       = 2;
    private static final int COLUMN_INDEX_VALUE     = 3;


    /***********************************************************************************************
     * Collect some extra configuration data directly from the Registry.
     * We are building a List of Icon:Updated:Property:Value
     *
     * @param configresult
     * @param resourcekey
     */

    public static void appendRegistryPropertiesForKey(final Vector<Vector> configresult,
                                                      final String resourcekey)
        {
        if ((configresult != null)
            && (resourcekey != null))
            {
            Hashtable<String, RootPlugin> properties;
            Enumeration<String> enumKeys;

            properties = FrameworkSingletons.REGISTRY.getProperties();
            enumKeys = properties.keys();

            while (enumKeys.hasMoreElements())
                {
                final String key;

                key = enumKeys.nextElement();

                if (key.startsWith(resourcekey))
                    {
                    final RootPlugin resource;
                    final Vector<Object> vecRow;

                    resource = properties.get(key);
                    vecRow = new Vector<Object>(4);

                    // Remember that all data entries must be Strings
                    vecRow.add(ReportIcon.getIcon(resource.getIconFilename()));
                    vecRow.add(resource.isUpdated());
                    vecRow.add(key);
                    vecRow.add(((ResourcePlugin)resource).getResource().toString());

                    configresult.add(vecRow);
                    }
                }

            // Help the GC?
            properties = null;
            enumKeys = null;
            }
        }


    /***********************************************************************************************
     * Collect some extra Instrument configuration data for a Report.
     * The MetaData from the Instrument, Controller and Plugins,
     * and any extra Configuration (usually from the Instrument static configuration).
     * We are building a List of Icon:Updated:Property:Value
     *
     * @param collection
     * @param instrument
     * @param parentresourcekey
     */

    public static void collectInstrumentConfiguration(final Vector<Vector> collection,
                                                      final Instrument instrument,
                                                      final String parentresourcekey)
        {
        FrameworkSingletons.LOGGER.debugTimedEvent(FrameworkSingletons.LOADER_PROPERTIES.isTimingDebug(),
                               "InstrumentUIHelper.collectInstrumentConfiguration()");

        // TODO REVIEW  collectInstrumentConfiguration
//        if ((false)
//            && (collection != null)
//            && (instrument != null)
//            && (XmlBeansUtilities.isValidXml(instrument)))
//            {
//            final List<String> listKeys;
//            final Controller controller;
//
//            // Always start with no Configuration, and add from Dynamic & Static sources
//            collection.clear();
//            listKeys = new ArrayList<String>(50);
//
//            // The Metadata for the Instrument (No Module)
//            addMetadataToReport(collection,
//                                listKeys,
//                                instrument,
//                                null,
//                                instrument.getInstrumentMetadataList(),
//                                parentresourcekey);
//
//            // Now do the Controller Module
//            controller = instrument.getController();
//
//            if (controller != null)
//                {
//                final List<CommandType> listControllerCommands;
//                final List<PluginType> listPlugins;
//
//                // The Metadata for the Controller Module
//                addMetadataToReport(collection,
//                                    listKeys,
//                                    instrument,
//                                    controller,
//                                    controller.getControllerMetadataList(),
//                                    parentresourcekey);
//
//                // The Metadata for the Controller Module Commands
////                listControllerCommands = controller.getCommandList();
////
////                if (listControllerCommands != null)
////                    {
////                    for (int j = 0; j < listControllerCommands.size(); j++)
////                        {
////                        final CommandType command;
////
////                        command = listControllerCommands.get(j);
////
////                        if (command != null)
////                            {
////                            // The Value for the Controller Module Command Response
////                            addResponseToReport(configresult,
////                                                listKeys,
////                                                instrument,
////                                                controller,
////                                                command.getResponse(),
////                                                parentresourcekey);
////                            }
////                        }
////                    }
//
//                // Now do the Plugins
//                listPlugins = controller.getPluginList();
//
//                if (listPlugins != null)
//                    {
//                    for (int i = 0; i < listPlugins.size(); i++)
//                        {
//                        final PluginType plugin;
//
//                        plugin = listPlugins.get(i);
//
//                        if (plugin != null)
//                            {
//                            final List<CommandType> listPluginCommands;
//
//                            // The Metadata for the Plugin Module
//                            addMetadataToReport(collection,
//                                                listKeys,
//                                                instrument,
//                                                plugin,
//                                                plugin.getPluginMetadataList(),
//                                                parentresourcekey);
//
//                            // The Metadata for the Plugin Module Commands
////                            listPluginCommands = plugin.getCommandList();
////
////                            if (listPluginCommands != null)
////                                {
////                                for (int j = 0; j < listPluginCommands.size(); j++)
////                                    {
////                                    final CommandType command;
////
////                                    command = listPluginCommands.get(j);
////
////                                    if (command != null)
////                                        {
////                                        // The Value for the Plugin Module Command Response
////                                        addResponseToReport(configresult,
////                                                            listKeys,
////                                                            instrument,
////                                                            plugin,
////                                                            command.getResponse(),
////                                                            parentresourcekey);
////                                        }
////                                    }
////                                }
//                            }
//                        }
//                    }
//                }
//
//            // Finally, append any extra configuration data,
//            // which may have nothing to do with the Registry
//            // This is usually the Port configuration
//            // The format is Icon:Updated:Key:Value
////            if ((staticconfig != null)
////                && (!staticconfig.isEmpty()))
////                {
////                FrameworkSingletons.LOGGER.debugProtocolEvent("InstrumentUIHelper.collectInstrumentConfiguration()  ADDING ALL 'static' CONFIG [size=" + staticconfig.size() + "]");
////                configresult.addAll(staticconfig);
////                }
//            }
        }


    /***********************************************************************************************
     * Add an item to the specified Collection, for a ConfigurationReport.
     *
     * @param collection
     * @param icon
     * @param key
     * @param value
     */

    public static void addItemToConfiguration(final Vector<Vector> collection,
                                              final String icon,
                                              final String key,
                                              final String value)
        {
        if ((collection != null)
            && (key != null)
            && (value != null))
            {
            final Vector<Object> vecItem;

            // We are building Icon:Updated:Key:Value
            vecItem = new Vector<Object>(CONFIG_DATA_SIZE);

            vecItem.add(ReportIcon.getIcon(icon));
            vecItem.add(Boolean.FALSE);
            vecItem.add(key);
            vecItem.add(value);

            //System.out.println("InstrumentUIHelper.addItemToConfiguration() ADDED [key=" + key + "] [value=" + value + "]");
            collection.add(vecItem);
            }
        else
            {
            FrameworkSingletons.LOGGER.error("InstrumentUIHelper.addItemToConfiguration() Unsuccessful call [key=" + key + "] [value=" + value + "]");
            }
        }


    /***********************************************************************************************
     * Add DAO configuration data to the specified Collection, for a ConfigurationReport.
     *
     * @param instrument
     *
     * @return Vector<Vector>
     */

    public static Vector<Vector> assembleInstrumentConfiguration(final ObservatoryInstrumentInterface instrument)
        {
        final Vector<Vector> vecConfiguration;

        vecConfiguration = new Vector<Vector>(50);

        // Add Instrument, DAO, Port & Stream configuration to supplied Collection

        if (instrument != null)
            {
            if (instrument.getDAO() != null)
                {
                // Add the DAO data
                instrument.readResources();
                vecConfiguration.addAll(instrument.getDAO().getDAOConfiguration());

                if (instrument.getDAO().getPort() != null)
                    {
                    // Port Configuration contains the Name and Description of the port
                    vecConfiguration.addAll(instrument.getDAO().getPort().getPortConfiguration());
                    //System.out.println("InstrumentUIHelper.assembleInstrumentConfiguration() [Port_config_size=" + instrument.getDAO().getPort().getPortConfiguration().size() + "]");

                    // Reload the Stream Configuration every time the Resources are read
                    if (instrument.getDAO().getPort().getTxStream() != null)
                        {
                        instrument.getDAO().getPort().getTxStream().readResources();
                        vecConfiguration.addAll(instrument.getDAO().getPort().getTxStream().getStreamConfiguration());
                        //System.out.println("InstrumentUIHelper.assembleInstrumentConfiguration() [TxStream_config_size=" + instrument.getDAO().getPort().getTxStream().getStreamConfiguration().size() + "]");
                        }

                    if (instrument.getDAO().getPort().getRxStream() != null)
                        {
                        instrument.getDAO().getPort().getRxStream().readResources();
                        vecConfiguration.addAll(instrument.getDAO().getPort().getRxStream().getStreamConfiguration());
                        //System.out.println("InstrumentUIHelper.assembleInstrumentConfiguration() [RxStream_config_size=" + instrument.getDAO().getPort().getRxStream().getStreamConfiguration().size() + "]");
                        }
                    }
                }
            }

        return (vecConfiguration);
        }


    /***********************************************************************************************
     * Debug the specified Configuration.
     *
     * @param config
     * @param message
     * @param debug
     */

    public static void debugConfiguration(final Vector<Vector> config,
                                          final String message,
                                          final boolean debug)
        {
        if ((config != null)
            && (!config.isEmpty())
            && (debug))
            {
            final Iterator<Vector> iterConfig;

            System.out.println(message);

            iterConfig = config.iterator();

            while (iterConfig.hasNext())
                {
                final Vector<Object> vecConfig;

                // The format is Icon:Updated:Key:Value
                vecConfig = iterConfig.next();

                if ((vecConfig != null)
                    && (vecConfig.get(COLUMN_INDEX_KEY) != null)
                    && (vecConfig.get(COLUMN_INDEX_VALUE) != null))
                    {
                    final StringBuffer buffer;

                    buffer = new StringBuffer();

                    buffer.append(vecConfig.get(COLUMN_INDEX_KEY).toString());
                    buffer.append(EQUALS);
                    buffer.append(vecConfig.get(COLUMN_INDEX_VALUE).toString());

                    System.out.println(buffer);
                    }
                }
            }
        }
    }
