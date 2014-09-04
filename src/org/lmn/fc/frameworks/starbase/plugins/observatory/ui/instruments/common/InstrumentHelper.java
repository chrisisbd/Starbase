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


import org.apache.xmlbeans.XmlObject;
import org.lmn.fc.common.constants.*;
import org.lmn.fc.frameworks.starbase.plugins.observatory.common.IPVersion;
import org.lmn.fc.frameworks.starbase.plugins.observatory.events.InstrumentStateChangedEvent;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.*;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.ExportableComponentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.misc.BlankExportableComponent;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.serial.SerialConfigurationHelper;
import org.lmn.fc.frameworks.starbase.portcontroller.PortRxStreamInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.PortTxStreamInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.impl.CommandProcessorContext;
import org.lmn.fc.frameworks.starbase.portcontroller.impl.StreamUtilities;
import org.lmn.fc.frameworks.starbase.portcontroller.impl.streams.StreamType;
import org.lmn.fc.model.registry.InstallationFolder;
import org.lmn.fc.model.xmlbeans.instruments.Controller;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;
import org.lmn.fc.model.xmlbeans.instruments.PluginType;
import org.lmn.fc.model.xmlbeans.instruments.ResponseType;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;
import org.lmn.fc.ui.reports.ReportIcon;

import java.net.URL;
import java.util.List;
import java.util.Vector;


/***************************************************************************************************
 * InstrumentHelper.
 */

public final class InstrumentHelper implements FrameworkConstants,
                                               FrameworkStrings,
                                               FrameworkMetadata,
                                               FrameworkSingletons,
                                               FrameworkRegex,
                                               ResourceKeys
    {
    // String Resources
    public static final String METADATA_ICON = "metadata.png";
    public static final String RESPONSE_ICON = "response.png";


    /***********************************************************************************************
     * A convenience method for notifying of a change in the Instrument state.
     *
     * @param instrument
     */

    public static void notifyInstrumentChanged(final ObservatoryInstrumentInterface instrument)
        {
        // Something has changed, we may need to update indicators etc.
        if ((instrument != null)
            && (instrument.getContext() != null))
            {
            // Maintain the same InstrumentState
            // This should only occur in INITIALISE, READY and BUSY
            instrument.notifyInstrumentStateChangedEvent(instrument,
                                                         instrument,
                                                         instrument.getInstrumentState(),
                                                         instrument.getInstrumentState(),
                                                         CommandProcessorContext.getRepeatNumber(instrument.getContext()),
                                                         CommandProcessorContext.getRepeatText(instrument.getContext()));
            }
        }


    /***********************************************************************************************
     * Add a List of Metadata to the specified Report.
     * Module may be NULL.
     *
     * @param reportdata
     * @param keylist
     * @param instrument
     * @param module
     * @param metadata
     * @param parentresourcekey
     */

    public static void addMetadataToReport(final Vector<Vector> reportdata,
                                           final List<String> keylist,
                                           final Instrument instrument,
                                           final XmlObject module,
                                           final List<Metadata> metadata,
                                           final String parentresourcekey)
        {
        if ((reportdata != null)
            && (keylist != null)
            && (instrument != null)
            && (metadata != null)
            && (!metadata.isEmpty()))
            {
            for (int i = 0; i < metadata.size(); i++)
                {
                final Metadata metaData;

                metaData = metadata.get(i);

                if (metaData != null)
                    {
                    final String strKey;

                    strKey = buildResourceKey(keylist,
                                              instrument,
                                              module,
                                              metaData.getKey(),
                                              parentresourcekey);

                    if ((strKey != null)
                        && (!EMPTY_STRING.equals(strKey)))
                        {
                        final Vector<Object> vecRow;

                        // We are building Icon:Updated:Property:Value
                        vecRow = new Vector<Object>(ConfigurationHelper.CONFIG_DATA_SIZE);
                        vecRow.add(ReportIcon.getIcon(METADATA_ICON));
                        vecRow.add(metaData.getValue() != null);

                        vecRow.add(strKey);

                        if (metaData.getValue() != null)
                            {
                            vecRow.add(metaData.getValue());
                            }
                        else
                            {
                            vecRow.add(InstrumentUIComponentDecoratorInterface.MSG_NO_DATA_AVAILABLE);
                            }

                        reportdata.add(vecRow);
                        }
                    }
                }
            }
        }


    /***********************************************************************************************
     * Add a Response value to the specified Report.
     * Module may be NULL.
     *
     * @param reportdata
     * @param keylist
     * @param instrument
     * @param module
     * @param response
     * @param parentresourcekey
     */

    public static void addResponseToReport(final Vector<Vector> reportdata,
                                           final List<String> keylist,
                                           final Instrument instrument,
                                           final XmlObject module,
                                           final ResponseType response,
                                           final String parentresourcekey)
        {
        // Non-Metadata can only be Response values
        if ((reportdata != null)
            && (keylist != null)
            && (instrument != null)
            && (response != null))
            {
            final String strKey;

            strKey = buildResourceKey(keylist,
                                      instrument,
                                      module,
                                      response.getName(),
                                      parentresourcekey);

            if (!EMPTY_STRING.equals(strKey))
                {
                final Vector<Object> vecRow;

                // We are building Icon:Updated:Property:Value
                vecRow = new Vector<Object>(ConfigurationHelper.CONFIG_DATA_SIZE);
                vecRow.add(ReportIcon.getIcon(RESPONSE_ICON));
                vecRow.add(response.getValue() != null);
                vecRow.add(strKey);

                if (response.getValue() != null)
                    {
                    vecRow.add(response.getValue());
                    LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                           "InstrumentUIHelper.addResponseToReport() [value=" + response.getValue() + "]");
                    }
                else
                    {
                    vecRow.add(InstrumentUIComponentDecoratorInterface.MSG_NO_DATA_AVAILABLE);
                    LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                           "InstrumentUIHelper.addResponseToReport() [value=" + InstrumentUIComponentDecoratorInterface.MSG_NO_DATA_AVAILABLE + "]");
                    }

                reportdata.add(vecRow);
                }
            }
        }


    /***********************************************************************************************
     * Build the ResourceKey for the specified context.
     *
     * @param listkeys
     * @param instrument
     * @param module
     * @param key
     * @param parentresourcekey
     *
     * @return String
     */

    public static String buildResourceKey(final List<String> listkeys,
                                          final Instrument instrument,
                                          final XmlObject module,
                                          final String key,
                                          final String parentresourcekey)
        {
        final StringBuffer bufferKey;

        bufferKey = new StringBuffer();

        if ((listkeys != null)
            && (instrument != null))
//            && (XmlBeansUtilities.isValidXml(instrument)))
            {
            bufferKey.append(parentresourcekey);
            bufferKey.append(instrument.getResourceKey());

            if (module != null)
                {
                if (module instanceof Controller)
                    {
                    bufferKey.append(DOT);
                    bufferKey.append(((Controller) module).getResourceKey());

                    if (key != null)
                        {
                        bufferKey.append(DOT);
                        bufferKey.append(key);
                        }
                    }
                else if (module instanceof PluginType)
                    {
                    bufferKey.append(DOT);
                    bufferKey.append(((PluginType) module).getResourceKey());

                    if (key != null)
                        {
                        bufferKey.append(DOT);
                        bufferKey.append(key);
                        }
                    }
                else
                    {
                    // This is an error!
                    bufferKey.append(DOT);
                    bufferKey.append(InstrumentUIComponentDecoratorInterface.INVALID_RESOURCE_KEY);
                    }
                }
            else
                {
                // These are the top-level keys for the Instrument Configuration
                if (key != null)
                    {
                    bufferKey.append(DOT);
                    bufferKey.append(key);
                    }
                }

            // Record the keys to ensure that they are unique
            if (!listkeys.contains(bufferKey.toString()))
                {
                listkeys.add(bufferKey.toString());
                }
            else
                {
                // Indicate that we already have this key
                bufferKey.setLength(0);
                }
            }

        return (bufferKey.toString());
        }


    /***********************************************************************************************
     * Get the URL of a specified file in the Help folder for the specified Instrument.
     * This assumes that the Instrument help is in a folder beneath the Instrument root,
     * called InstallationFolder.HELP.
     *
     * @param decorator
     * @param filename
     *
     * @return URL
     */

    public static URL getInstrumentHelpURL(final InstrumentUIComponentDecoratorInterface decorator,
                                           final String filename)
        {
        URL urlHelp;

        urlHelp = null;

        if ((decorator != null)
            && (filename != null)
            && (!EMPTY_STRING.equals(filename)))
            {
            urlHelp = decorator.getClass().getResource(getInstrumentInstallationFolder(decorator,
                                                                                       InstallationFolder.HELP) + filename);
            }

        return (urlHelp);
        }


    /***********************************************************************************************
     * Get the pathname of the InstallationFolder for the specified Instrument.
     *
     * @param decorator
     * @param folder
     *
     * @return String
     */

    public static String getInstrumentInstallationFolder(final InstrumentUIComponentDecoratorInterface decorator,
                                                         final InstallationFolder folder)
        {
        String strRoot;

        strRoot = EMPTY_STRING;

        if ((decorator != null)
            && (folder != null))
            {
            strRoot = '/' + decorator.getClass().getPackage().getName();
            strRoot = strRoot.replace('.', '/');
            strRoot = strRoot + "/" + folder.getName() + "/";
            strRoot = strRoot.replace(System.getProperty("file.separator").charAt(0), '/');
            }

        //System.out.println("INSTRUMENT INSTALLATION FOLDER =" + strRoot);

        return (strRoot);
        }


    /***********************************************************************************************
     * Get the pathname of the InstallationFolder below the specified Class.
     *
     * @param aClass
     * @param folder
     *
     * @return String
     */

    public static String getInstallationFolderBelowClass(final Class aClass,
                                                         final InstallationFolder folder)
        {
        String strRoot;

        strRoot = EMPTY_STRING;

        if ((aClass != null)
            && (folder != null))
            {
            strRoot = '/' + aClass.getPackage().getName();
            strRoot = strRoot.replace('.', '/');
            strRoot = strRoot + "/" + folder.getName() + "/";
            strRoot = strRoot.replace(System.getProperty("file.separator").charAt(0), '/');
            }

        //System.out.println("INSTALLATION FOLDER =" + strRoot);

        return (strRoot);
        }


    /***********************************************************************************************
     * Format an EhternetController's IP Address for display on a ControlPanel.
     *
     * @param event
     *
     * @return String
     */


    public static String formatEthernetControllerIPAddress(final InstrumentStateChangedEvent event)
        {
        String strDisplayAddress;
        final String strIPAddress;

        strIPAddress = IPVersion.stripTrailingPaddingFromIPAddressAndPort(event.getInstrument().getInstrument().getController().getIPAddress());

        // Remove the protocol, to enlarge the text a bit...
        if (strIPAddress.startsWith(ControlPanelInterface.PREFIX_HTTP))
            {
            strDisplayAddress = strIPAddress.substring(ControlPanelInterface.PREFIX_HTTP.length());
            }
        else if (strIPAddress.startsWith(ControlPanelInterface.PREFIX_UDP))
            {
            strDisplayAddress = strIPAddress.substring(ControlPanelInterface.PREFIX_UDP.length());
            }
        else
            {
            strDisplayAddress = strIPAddress;
            }

        // Remove any port, to enlarge the text a bit...
        if (strDisplayAddress.contains(COLON))
            {
            strDisplayAddress = strDisplayAddress.substring(0, strDisplayAddress.indexOf(
                    COLON));
            }

        return (strDisplayAddress);
        }


    /***********************************************************************************************
     * Find the Instrument Staribus Address, IP Address or Virtual Address,
     * depending on the type of Instrument.
     *
     * @param instrument
     *
     * @return String
     */

    public static String findInstrumentAddress(final ObservatoryInstrumentInterface instrument)
        {
        String strAddress;

        if ((instrument != null)
            && (instrument.getInstrument() != null)
            && (instrument.getInstrument().getController() != null))
            {
            // We don't yet know what kind of Controller we have...
            if (instrument.getInstrument().getController().getIPAddress() != null)
                {
                final String strIPAddress;

                strIPAddress = IPVersion.stripTrailingPaddingFromIPAddressAndPort(instrument.getInstrument().getController().getIPAddress());

                // Remove the protocol, to enlarge the text a bit...
                if (strIPAddress.startsWith(ControlPanelInterface.PREFIX_HTTP))
                    {
                    strAddress = strIPAddress.substring(ControlPanelInterface.PREFIX_HTTP.length());
                    }
                else if (strIPAddress.startsWith(ControlPanelInterface.PREFIX_UDP))
                    {
                    strAddress = strIPAddress.substring(ControlPanelInterface.PREFIX_UDP.length());
                    }
                else
                    {
                    strAddress = strIPAddress;
                    }

                // Remove any port, to enlarge the text a bit...
                if (strAddress.contains(COLON))
                    {
                    strAddress = strAddress.substring(0, strAddress.indexOf(COLON));
                    }
                }
            else if (instrument.getInstrument().getController().getStaribusAddress() != null)
                {
                strAddress = instrument.getInstrument().getController().getStaribusAddress();
                }
            else if (instrument.getInstrument().getController().getVirtualAddress() != null)
                {
                strAddress = SPACE;
                }
            else
                {
                // Invalid Configuration
                strAddress = QUERY;
                LOGGER.error("InstrumentUIHelper.findInstrumentAddress() Instrument appears to have an invalid Address configuration");
                }
            }
        else
            {
            // No Controller, so no Address
            strAddress = ControlPanelInterface.DEFAULT_ADDRESS;
            }

        return (strAddress);
        }


    /***********************************************************************************************
     * Find the Instrument communications port identifier, e.g. COM1.
     *
     * @param instrument
     *
     * @return String
     */

    public static String findInstrumentPort(final ObservatoryInstrumentInterface instrument)
        {
        String strPort;

        strPort = QUERY;

        // The Instrument must have a DAO in order to have a Port
        if ((instrument != null)
            && (instrument.getDAO() != null)
            && (instrument.getDAO().getPort() != null)
            && (instrument.getDAO().getPort().getTxStream() != null)
            && (instrument.getInstrumentConfiguration() != null))
            {
            final String strKeyPortName;
            final Vector<Vector> vecConfig;

            strKeyPortName = instrument.getDAO().getPort().getTxStream().getResourceKey() + ResourceKeys.KEY_PORT_NAME;

            vecConfig = instrument.getInstrumentConfiguration();
            ConfigurationHelper.debugConfiguration(vecConfig,
                                                   "InstrumentUIHelper.findInstrumentPort()",
                                                   true);

            // Get the KEY_PORT_NAME configuration
            strPort = ObservatoryInstrumentHelper.getConfigurationValueByKey(vecConfig,
                                                                             strKeyPortName);

            if (EMPTY_STRING.equals(strPort))
                {
                LOGGER.error("InstrumentUIHelper.findInstrumentPort() Could not find Instrument Port in Configuration [key=" + strKeyPortName + "]");
                }
            }

        return (strPort);
        }


    /***********************************************************************************************
     * Find the Instrument with the specified Identifier.
     *
     * @param instrumentlist
     * @param identifier
     *
     * @return Instrument
     */

    public static Instrument findInstrumentForIdentifier(final List<Instrument> instrumentlist,
                                                         final String identifier)
        {
        final String SOURCE = "InstrumentHelper.findInstrumentForIdentifier() ";
        Instrument instrument;
        boolean boolFoundIt;

        instrument = null;
        boolFoundIt = false;

        if ((instrumentlist != null)
            && (!instrumentlist.isEmpty())
            && (identifier != null))
            {
            for (int intInstrumentIndex = 0;
                 ((intInstrumentIndex < instrumentlist.size())
                    && (!boolFoundIt));
                 intInstrumentIndex++)
                {
                final Instrument instrumentTest;

                instrumentTest = instrumentlist.get(intInstrumentIndex);

                if (identifier.equals(instrumentTest.getIdentifier()))
                    {
                    instrument = instrumentTest;
                    boolFoundIt = true;
                    }
                }
            }

        return (instrument);
        }


    /***********************************************************************************************
     * Indicate if the Instrument has Rx and Tx Streams of the specified StreamType.
     *
     * @param instrument
     * @param streamtype
     *
     * @return boolean
     */

    public static boolean hasValidRxTxStreams(final Instrument instrument,
                                              final StreamType streamtype)
        {
        final String SOURCE = "InstrumentHelper.hasValidRxTxStreams() ";
        boolean boolValid;

        if (instrument != null)
            {
            if (instrument.getDAO() != null)
                {
                // What kind of Port is specified?
                if (instrument.getDAO().getPort() != null)
                    {
                    final String strRxStreamClassname;
                    final String strTxStreamClassname;
                    final PortRxStreamInterface rxStream;
                    final PortTxStreamInterface txStream;

                    strRxStreamClassname = instrument.getDAO().getPort().getRxStream();
                    strTxStreamClassname = instrument.getDAO().getPort().getTxStream();

                    // Get some dummy streams, so we can get the StreamTypes
                    rxStream = StreamUtilities.instantiateRxStream(strRxStreamClassname, EMPTY_STRING);
                    txStream = StreamUtilities.instantiateTxStream(strTxStreamClassname, EMPTY_STRING);

                    // In this test the Rx and Tx streams must be of the same type
                    boolValid = ((rxStream != null)
                                 && (streamtype.equals(rxStream.getStreamType())));

                    boolValid = ((boolValid)
                                 && (txStream != null)
                                 && (streamtype.equals(txStream.getStreamType())));
                    }
                else if (instrument.getDAO().getStaribusPort() != null)
                    {
                    final String strStaribusPort;

                    strStaribusPort = instrument.getDAO().getStaribusPort();

                    boolValid = ((SerialConfigurationHelper.PORT_COMMON_ID.equals(strStaribusPort))
                                    && (streamtype.equals(StreamType.STARIBUS)));
                    }
                else
                    {
                    // There is no Port specified, but there is a DAO, so fail
                    LOGGER.error(SOURCE + "There is no Port specified, but there is a DAO");

                    boolValid = false;
                    }
                }
            else
                {
                // There is no DAO, but this is allowed for e.g. the TerminalEmulator

                boolValid = true;
                }
            }
        else
            {
            // There is no Instrument, so fail
            LOGGER.error(SOURCE + "There is no Instrument!");

            boolValid = false;
            }

        return (boolValid);
        }


    /***********************************************************************************************
     * Reset the specified list of ExportableComponents to contain only BlankExportableComponent.
     *
     * @param exportables
     */

    public static void resetExportableComponents(final List<ExportableComponentInterface> exportables)
        {
        if (exportables != null)
            {
            final ExportableComponentInterface exportable;

            exportable = new BlankExportableComponent();

            // Fill up the ExportableComponents array with blanks
            for (int i = 0;
                 i < ObservatoryInstrumentDAOInterface.EXPORTABLE_INDEX_MAX;
                 i++)
                {
                exportables.add(exportable);
                }
            }
        else
            {
            LOGGER.error("InstrumentUIComponentDecorator.resetExportableComponents() Exportables List unexpectedly NULL");
            }
        }
    }
