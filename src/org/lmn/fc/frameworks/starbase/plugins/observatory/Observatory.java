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

package org.lmn.fc.frameworks.starbase.plugins.observatory;

import gnu.io.CommPortIdentifier;
import org.apache.xmlbeans.XmlException;
import org.lmn.fc.common.actions.ContextAction;
import org.lmn.fc.common.constants.ResourceKeys;
import org.lmn.fc.common.loaders.LoaderProperties;
import org.lmn.fc.common.utilities.files.FileUtilities;
import org.lmn.fc.common.utilities.misc.Utilities;
import org.lmn.fc.common.utilities.threads.SwingWorker;
import org.lmn.fc.common.xml.XmlBeansUtilities;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.ephemerides.EphemerisDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.common.IPVersion;
import org.lmn.fc.frameworks.starbase.plugins.observatory.common.ObservatoryConstants;
import org.lmn.fc.frameworks.starbase.plugins.observatory.events.ObservatoryMetadataChangedEvent;
import org.lmn.fc.frameworks.starbase.plugins.observatory.events.ObservatoryMetadataChangedListener;
import org.lmn.fc.frameworks.starbase.plugins.observatory.events.ObserverMetadataChangedEvent;
import org.lmn.fc.frameworks.starbase.plugins.observatory.events.ObserverMetadataChangedListener;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.*;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.EphemeridesHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.ObservatoryUI;
import org.lmn.fc.frameworks.starbase.portcontroller.PortControllerInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.impl.PortController;
import org.lmn.fc.frameworks.starbase.portcontroller.impl.UDPUtilities;
import org.lmn.fc.model.dao.DataStore;
import org.lmn.fc.model.plugins.AtomPlugin;
import org.lmn.fc.model.plugins.impl.PluginData;
import org.lmn.fc.model.registry.InstallationFolder;
import org.lmn.fc.model.registry.RegistryModelUtilities;
import org.lmn.fc.model.registry.impl.BeanFactoryXml;
import org.lmn.fc.model.resources.ResourcePlugin;
import org.lmn.fc.model.root.ActionGroup;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.model.xmlbeans.attributes.AttributesDocument;
import org.lmn.fc.model.xmlbeans.ephemerides.EphemeridesDocument;
import org.lmn.fc.model.xmlbeans.ephemerides.Ephemeris;
import org.lmn.fc.model.xmlbeans.groups.ObservatoryGroupDefinitionsDocument;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;
import org.lmn.fc.model.xmlbeans.instruments.InstrumentsDocument;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;
import org.lmn.fc.model.xmlbeans.poi.LineOfInterest;
import org.lmn.fc.model.xmlbeans.poi.PointOfInterest;
import org.lmn.fc.model.xmlbeans.properties.PropertiesDocument;
import org.lmn.fc.model.xmlbeans.properties.PropertyResource;
import org.lmn.fc.ui.components.BlankUIComponent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.PortUnreachableException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.IllegalBlockingModeException;
import java.util.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


/***************************************************************************************************
 * Observatory.
 */

public final class Observatory extends PluginData
                               implements ObservatoryMBean,
                                          ObservatoryConstants,
                                          ObservatoryInterface
    {
    // This cannot be in FrameworkSingletons because it won't be loaded at the right time...
    private static final PortControllerInterface PORT_CONTROLLER = PortController.getInstance();
    private static final BeanFactoryXml BEAN_FACTORY_XML = BeanFactoryXml.getInstance();
    private static final LoaderProperties LOADER_PROPERTIES = LoaderProperties.getInstance();

    public static final String FILENAME_INSTRUMENTS_ATTRIBUTES = "instruments-attributes.xml";
    private static final String FILENAME_OBSERVATORY_EPHEMERIDES = "Observatory-ephemerides.xml";
    private static final String FILENAME_OBSERVATORY_GROUPS = "Observatory-groups.xml";
    private static final String FILENAME_INSTRUMENTS_COMMON = "instruments-common.xml";
    private static final String FILENAME_INSTRUMENT_SUFFIX = "-instrument." + FileUtilities.xml;
    private static final String FILENAME_PROPERTIES_SUFFIX = "-properties." + FileUtilities.xml;
    private static final String TITLE_DUPLICATED_IDENTIFIER = " Duplicated Instrument Identifier";
    private static final String TITLE_DUPLICATED_STARIBUS_ADDRESS = " Duplicated Controller Staribus Address";
    private static final String TITLE_DUPLICATED_IP_ADDRESS = " Duplicated Controller IP Address";
    private static final String MSG_IDENTIFIER_IN_USE = " has an Instrument Identifier which is already in use";
    private static final String MSG_CHANGE_IDENTIFIER = "You must ensure that all Identifiers are unique";
    private static final String MSG_STARIBUS_ADDRESS_IN_USE = " has a Controller Staribus Address which is already in use by ";
    private static final String MSG_IP_ADDRESS_IN_USE = " has a Controller IPAddress which is already in use by ";
    private static final String MSG_CHANGE_ADDRESS = "You must ensure that all Addresses are unique";
    private static final String MSG_CONTROLLER_ID = "Controller identification message from: ";

    private static final int PORT_UDP_LISTENER = 30303;
    private static final long RETRY_WAIT_MILLIS = 1000;
    private static final int UDP_TIMEOUT_MILLIS = 10000;
    private static final int INITIAL_CAPACITY = 50;

    // Discovery Listener
    private SwingWorker workerUDP;
    private final DatagramSocket socketUDP;

    // Ephemerides
    private EphemeridesDocument docEphemerides;
    private final Hashtable<String, EphemerisDAOInterface> hashtableEphemeris;

    // Observatory and Observer (current User) Metadata
    private final List<Metadata> listObservatoryMetadata;
    private final List<Metadata> listObserverMetadata;
    private boolean boolObservatoryMetadataLoaded;
    private boolean boolObserverMetadataLoaded;

    // Observatory POIs and LOIs
    private List<PointOfInterest> listObservatoryPOIs;
    private List<LineOfInterest> listObservatoryLOIs;

    private boolean boolRecordMacroMode;

    // Events
    private final Vector<ObservatoryMetadataChangedListener> vecObservatoryMetadataChangedListeners;
    private final Vector<ObserverMetadataChangedListener> vecObserverMetadataChangedListeners;


    /***********************************************************************************************
     * Examine the File array and collect Instrument definitions into the XML document.
     *
     * @param files
     * @param docInstruments
     *
     * @throws XmlException
     * @throws IOException
     */

    private static void collectInstruments(final File[] files,
                                           final InstrumentsDocument docInstruments) throws XmlException,
                                                                                     IOException
        {
        final List<String> listIdentifiers;
        final Map<String, String> mapAddresses;

        listIdentifiers = new ArrayList<String>(20);
        mapAddresses = new Hashtable<String,  String>(20);

        for (final File file : files)
            {
            // Read all files with names 'XXX-instrument.xml' in the imports folder
            if ((file != null)
                && (file.isFile())
                && (file.getName().endsWith(FILENAME_INSTRUMENT_SUFFIX)))
                {
                final InstrumentsDocument docSingleInstrument;

                docSingleInstrument = InstrumentsDocument.Factory.parse(file);

                // We really do need to validate this!
                if (XmlBeansUtilities.isValidXml(docSingleInstrument))
                    {
                    final List<Instrument> listInstrument;

                    listInstrument = docSingleInstrument.getInstruments().getInstrumentList();

                    if ((listInstrument != null)
                        && (listInstrument.size() >= 1))
                        {
                        //System.out.println("ADDING " + fileGroups.getName() + "list size=" + listInstrument.size());
                        docInstruments.getInstruments().getInstrumentList().addAll(listInstrument);

                        // Now check for Identifier and address duplication...
                        for (int i = 0;
                             i < listInstrument.size();
                             i++)
                            {
                            final Instrument instrument;

                            instrument = listInstrument.get(i);

                            // First check for duplicate Instrument Identifiers
                            if (listIdentifiers.contains(instrument.getIdentifier()))
                                {
                                final String [] message =
                                    {
                                    instrument.getIdentifier() + MSG_IDENTIFIER_IN_USE,
                                    MSG_CHANGE_IDENTIFIER
                                    };

                                // We know about this Instrument already
                                Toolkit.getDefaultToolkit().beep();
                                JOptionPane.showMessageDialog(null,
                                                              message,
                                                              instrument.getIdentifier() + TITLE_DUPLICATED_IDENTIFIER,
                                                              JOptionPane.WARNING_MESSAGE);
                                }
                            else
                                {
                                // Record the Identifier for later
                                listIdentifiers.add(instrument.getIdentifier());
                                }

                            // Now check the Addresses, but only if we have a Controller
                            // NOTE: The Hashtable Key is the Address, the Value is the Identifier
                            if (instrument.getController() != null)
                                {
                                if (instrument.getController().getStaribusAddress() != null)
                                    {
                                    // Only check Staribus for Real Controllers
                                    if (ObservatoryInstrumentHelper.isStaribusController(instrument))
                                        {
                                        if (mapAddresses.containsKey(instrument.getController().getStaribusAddress()))
                                            {
                                            final String [] message =
                                                {
                                                instrument.getIdentifier()
                                                        + MSG_STARIBUS_ADDRESS_IN_USE
                                                        + mapAddresses.get(instrument.getController().getStaribusAddress()),
                                                MSG_CHANGE_ADDRESS
                                                };

                                            // We know about this Instrument already
                                            Toolkit.getDefaultToolkit().beep();
                                            JOptionPane.showMessageDialog(null,
                                                                          message,
                                                                          instrument.getIdentifier() + TITLE_DUPLICATED_STARIBUS_ADDRESS,
                                                                          JOptionPane.WARNING_MESSAGE);
                                            }
                                        else
                                            {
                                            // Record the Staribus Address and Identifier for later
                                            mapAddresses.put(instrument.getController().getStaribusAddress(),
                                                             instrument.getIdentifier());
                                            }
                                        }
                                    }
                                else if (instrument.getController().getIPAddress() != null)
                                    {
                                    if (mapAddresses.containsKey(IPVersion.stripTrailingPaddingFromIPAddressAndPort(instrument.getController().getIPAddress())))
                                        {
                                        final String [] message =
                                            {
                                            instrument.getIdentifier()
                                                    + MSG_IP_ADDRESS_IN_USE
                                                    + mapAddresses.get(IPVersion.stripTrailingPaddingFromIPAddressAndPort(instrument.getController().getIPAddress())),
                                            MSG_CHANGE_ADDRESS};

                                        // We know about this Instrument already
                                        Toolkit.getDefaultToolkit().beep();
                                        JOptionPane.showMessageDialog(null,
                                                                      message,
                                                                      instrument.getIdentifier() + TITLE_DUPLICATED_IP_ADDRESS,
                                                                      JOptionPane.WARNING_MESSAGE);
                                        }
                                    else
                                        {
                                        // Record the IPAddress and Identifier for later
                                        mapAddresses.put(IPVersion.stripTrailingPaddingFromIPAddressAndPort(instrument.getController().getIPAddress()),
                                                         instrument.getIdentifier());
                                        }
                                    }
                                else if (instrument.getController().getVirtualAddress() != null)
                                    {
                                    // Ignore Virtual Controllers, since we can't tell them apart!
                                    }
                                else
                                    {
                                    // This should never occur!
                                    LOGGER.error("Observatory: Faulty Instrument Address definition for " + instrument.getIdentifier());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }


    /***********************************************************************************************
     * Listen for UDP packets on the specified port, and send a message to the console.
     *
     * @return SwingWorker
     */

    private static SwingWorker runUdpListener()
        {
        final String SOURCE = "Observatory.runUdpListener() ";
        final SwingWorker workerNew;

        workerNew = new SwingWorker(REGISTRY.getThreadGroup(), SOURCE)
            {
            /***********************************************************************************
             * Wait for a UDP Datagram.
             *
             * @return Object
             */

            public Object construct()
                {
                ByteBuffer buffer;
                boolean boolSuccess;

                buffer = null;
                boolSuccess = true;

                // Effectively loop forever, on another Thread
                while (!isStopping() && boolSuccess)
                    {
                    DatagramSocket socket;

                    socket = null;

                    try
                        {
                        final byte[] arrayResponse;

                        // The socket will be bound to the wildcard address, an IP address chosen by the kernel
                        socket = new DatagramSocket(PORT_UDP_LISTENER);
                        socket.setSoTimeout(UDP_TIMEOUT_MILLIS);

                        // Timeout is not possible up to here...
                        // Check to see if the SwingWorker has been asked to stop before trying again

                        if (!isStopping())
                            {
                            // This method blocks until a broadcast UDP Datagram is received
                            // This Command is being executed on its own SwingWorker, so this doesn't matter...
                            arrayResponse = UDPUtilities.receive(socket);

                            // Wrap up the Response to return
                            buffer = ByteBuffer.wrap(arrayResponse);

                            //LOGGER.logTimedEvent(MSG_CONTROLLER_ID + new String(buffer.array()));
                            LOGGER.logTimedEvent(new String(buffer.array()));

                            // If we get there, it all happened without a Timeout
                            boolSuccess = true;
                            }

                        // Timeouts will close the socket anyway
                        UDPUtilities.closeSocket(socket);
                        }

                    catch (SocketTimeoutException exception)
                        {
                        UDPUtilities.closeSocket(socket);
                        Utilities.safeSleep(RETRY_WAIT_MILLIS);
                        }

                    catch (PortUnreachableException exception)
                        {
                        UDPUtilities.closeSocket(socket);
                        Utilities.safeSleep(RETRY_WAIT_MILLIS);
                        LOGGER.error(SOURCE + exception.getMessage());
                        boolSuccess = false;
                        }

                    catch (IOException exception)
                        {
                        UDPUtilities.closeSocket(socket);
                        Utilities.safeSleep(RETRY_WAIT_MILLIS);
                        LOGGER.error(SOURCE + exception.getMessage());
                        boolSuccess = false;
                        }

                    catch (IllegalArgumentException exception)
                        {
                        UDPUtilities.closeSocket(socket);
                        Utilities.safeSleep(RETRY_WAIT_MILLIS);
                        LOGGER.error(SOURCE + exception.getMessage());
                        }

                    catch (SecurityException exception)
                        {
                        UDPUtilities.closeSocket(socket);
                        Utilities.safeSleep(RETRY_WAIT_MILLIS);
                        LOGGER.error(SOURCE + exception.getMessage());
                        }

                    catch (IllegalBlockingModeException exception)
                        {
                        UDPUtilities.closeSocket(socket);
                        Utilities.safeSleep(RETRY_WAIT_MILLIS);
                        LOGGER.error(SOURCE + exception.getMessage());
                        }

                    catch (Exception exception)
                        {
                        UDPUtilities.closeSocket(socket);
                        Utilities.safeSleep(RETRY_WAIT_MILLIS);
                        LOGGER.error(SOURCE + exception.getMessage());
                        }

                    finally
                        {
                        // Make sure that the Socket is released
                        UDPUtilities.closeSocket(socket);
                        }
                    }

                // buffer may be null if there are no data
                return (buffer);
                }


            /***********************************************************************************
             * Get any response from the UDP write().
             */

            public void finished()
                {
                if ((get() != null)
                    && (get() instanceof ByteBuffer)
                    && (((ByteBuffer)get()).array() != null))
                    {
                    //LOGGER.logTimedEvent(MSG_CONTROLLER_ID + new String(((ByteBuffer)get()).array()));
                    LOGGER.logTimedEvent(new String(((ByteBuffer)get()).array()));
                    }
                }
            };

        // Start the Thread we have prepared...
        workerNew.start();

        return (workerNew);
        }


    /***********************************************************************************************
     * Initialise the Ephemerides.
     *
     * @param daotable
     * @param SOURCE
     *
     * @return EphemeridesDocument
     *
     * @throws XmlException
     * @throws IOException
     */

    private static EphemeridesDocument initialiseEphemerides(final Hashtable<String, EphemerisDAOInterface> daotable,
                                                             final String SOURCE) throws XmlException, IOException
        {
        final String strFolderEphemerides;
        final File fileEphemerides;
        final EphemeridesDocument docEphemeridesImport;
        EphemeridesDocument docEphemeridesReturn;

        daotable.clear();

        strFolderEphemerides = InstallationFolder.getTerminatedUserDir()
                                   + "plugins/observatory/"
                                   + DataStore.CONFIG.getLoadFolder()
                                   + System.getProperty("file.separator")
                                   + FILENAME_OBSERVATORY_EPHEMERIDES;
        fileEphemerides = new File(strFolderEphemerides);

        docEphemeridesImport = EphemeridesDocument.Factory.parse(fileEphemerides);

        if (XmlBeansUtilities.isValidXml(docEphemeridesImport))
            {
            final EphemeridesDocument.Ephemerides ephemerides;

            // Preserve the document in case we need to add to it
            docEphemeridesReturn = docEphemeridesImport;
            ephemerides = docEphemeridesReturn.getEphemerides();

            if (ephemerides != null)
                {
                final List<Ephemeris> listLoadedEphemeris;

                listLoadedEphemeris = ephemerides.getEphemerisList();

                for (int intEphemerisIndex = 0;
                     intEphemerisIndex < listLoadedEphemeris.size();
                     intEphemerisIndex++)
                    {
                    final Ephemeris ephemeris;

                    ephemeris = listLoadedEphemeris.get(intEphemerisIndex);

                    if (ephemeris != null)
                        {
                        final EphemerisDAOInterface daoInterface;

                        // Check that we can instantiate the DAO associated with this Ephemeris
                        daoInterface = EphemeridesHelper.instantiateEphemerisDAO(ephemeris);

//                        System.out.println("Ephemeris " + intEphemerisIndex);
//                        System.out.println("Mnemonic=" + ephemeris.getMnemonic());
//                        System.out.println("Name=" + ephemeris.getName());
//                        System.out.println("Description=" + ephemeris.getDescription());
//                        System.out.println("URL=" + ephemeris.getURL());
//                        System.out.println("DaoClassname=" + ephemeris.getDaoClassname());
//
//                        if (ephemeris.getRaDec() != null)
//                            {
//                            System.out.println("RA=" + ephemeris.getRaDec().getRA());
//                            System.out.println("Dec=" + ephemeris.getRaDec().getDec());
//                            }
//                        else if (ephemeris.getAzEl() != null)
//                            {
//                            System.out.println("Alt=" + ephemeris.getAzEl().getAz());
//                            System.out.println("Az=" + ephemeris.getAzEl().getEl());
//                            }
//                        else if (ephemeris.getGalactic() != null)
//                            {
//                            System.out.println("b=" + ephemeris.getGalactic().getB());
//                            System.out.println("l=" + ephemeris.getGalactic().getL());
//                            }

                        if (daoInterface != null)
                            {
                            // Save the DAO for use later...
                            daotable.put(ephemeris.getName(), daoInterface);
                            }
                        else
                            {
                            LOGGER.error(SOURCE + "Unable to instantiate the Ephemeris [ephemeris=" + ephemeris.getName() + "]");
                            }
                        }
                    else
                        {
                        LOGGER.error(SOURCE + "The loaded Ephemeris was NULL");
                        docEphemeridesReturn = null;
                        }
                    }
                }
            else
                {
                LOGGER.error(SOURCE + "The EphemeridesDocument Ephemerides was NULL");
                docEphemeridesReturn = null;
                }
            }
        else
            {
            LOGGER.error(SOURCE + "Ephemerides " + EXCEPTION_XML_VALIDATION);
            docEphemeridesReturn = null;
            }

        return (docEphemeridesReturn);
        }


    /***********************************************************************************************
     * Set the ObservatoryLog on each of the instantiated Ephemerides.
     *
     * @param daotable
     * @param log
     */

    private static void setObservatoryLogOnEphemerides(final Hashtable<String, EphemerisDAOInterface> daotable,
                                                       final ObservatoryLogInterface log)
        {
        if ((daotable != null)
            && (!daotable.isEmpty()))
            {
            final Enumeration<String> keys;

            keys = daotable.keys();

            while (keys.hasMoreElements())
                {
                final EphemerisDAOInterface ephemeris;

                ephemeris = daotable.get(keys.nextElement());

                // The Log may be NULL
                ephemeris.setObservatoryLog(log);
                }
            }
        }


    /***********************************************************************************************
     * Construct the Observatory.
     */

    private Observatory()
        {
        super(1294733925502686148L);

        this.workerUDP = null;
        this.socketUDP = null;

        // Ephemerides
        this.docEphemerides = null;
        this.hashtableEphemeris = new Hashtable<String, EphemerisDAOInterface>(20);

        // Metadata
        this.listObservatoryMetadata = new ArrayList<Metadata>(INITIAL_CAPACITY);
        this.listObserverMetadata = new ArrayList<Metadata>(INITIAL_CAPACITY);
        this.boolObservatoryMetadataLoaded = false;
        this.boolObserverMetadataLoaded = false;

        // POIs and LOIs
        this.listObservatoryPOIs = new ArrayList<PointOfInterest>(50);
        this.listObservatoryLOIs = new ArrayList<LineOfInterest>(50);

        this.boolRecordMacroMode = false;

        this.vecObservatoryMetadataChangedListeners = new Vector<ObservatoryMetadataChangedListener>(10);
        this.vecObserverMetadataChangedListeners = new Vector<ObserverMetadataChangedListener>(10);
        }


    /***********************************************************************************************
     * Start up the Atom.
     *
     * @return boolean
     */

    public boolean startupAtom()
        {
        final String SOURCE = "Observatory.startupAtom() ";
        final AtomPlugin atom;
        final URL imageURL;
        boolean boolSuccess;

        // For use within inner classes
        atom = this;

        // Initialise the Atom
        if (super.startupAtom())
            {
            // Always start in Command Mode...
            setRecordMacroMode(false);

            // ...with no Metadata
            setObservatoryMetadataLoaded(false);
            setObserverMetadataLoaded(false);

            if (REGISTRY_MODEL.getLoggedInUser().getRole().isFrameworkViewer())
                {
                // Test Actions
                imageURL = getClass().getResource(RegistryModelUtilities.getCommonImagesRoot()
                                                      + ACTION_ICON_TEST2);

                final ContextAction action0 = new ContextAction("Observatory Action",
                                                                new ImageIcon(imageURL),
                                                                "Observatory Action",
                                                                KeyEvent.VK_O,
                                                                true,
                                                                false)
                    {
                    public void actionPerformed(final ActionEvent event)
                        {
                        LOGGER.logTimedEvent("Observatory do nothing in dummy test Action");
                        }
                    };

                getUserObjectContextActionGroupByIndex(ActionGroup.PLUGIN).addContextAction(action0);
                }

            //--------------------------------------------------------------------------------------
            // Install the Instruments for this Observatory
            // ToDo Use the current DataStore (not CONFIG), and a DAO
            // ToDo Remove knowledge of observatory name?

            try
                {
                final String strFolderCommon;
                final File fileCommon;
                final InstrumentsDocument docInstruments;

                // Note that the StaribusPort must be defined *before* all other Instruments
                // and so is read in from a different file
                strFolderCommon = InstallationFolder.getTerminatedUserDir()
                                   + "plugins/observatory/"
                                   + DataStore.CONFIG.getLoadFolder()
                                   + System.getProperty("file.separator")
                                   + FILENAME_INSTRUMENTS_COMMON;
                fileCommon = new File(strFolderCommon);

                docInstruments = InstrumentsDocument.Factory.parse(fileCommon);

                if (XmlBeansUtilities.isValidXml(docInstruments))
                    {
                    final String strFolderImports;
                    final String strFolderInstruments;
                    final File dirImports;
                    final File dirInstruments;

                    // Now read the individual Instrument configurations
                    strFolderImports = InstallationFolder.getTerminatedUserDir()
                                       + "plugins/observatory/"
                                       + DataStore.CONFIG.getLoadFolder();

                    dirImports = new File(strFolderImports);

                    // We must have an Imports folder
                    if (dirImports != null)
                        {
                        final String strFolderGroups;
                        final File fileGroups;
                        final ObservatoryGroupDefinitionsDocument docObservatoryGroups;

                        //--------------------------------------------------------------------------
                        // Ephemerides

                        this.docEphemerides = initialiseEphemerides(getEphemerisDaoTable(),
                                                                    SOURCE);

                        //--------------------------------------------------------------------------
                        // Observatory Groups

                        strFolderGroups = InstallationFolder.getTerminatedUserDir()
                                           + "plugins/observatory/"
                                           + DataStore.CONFIG.getLoadFolder()
                                           + System.getProperty("file.separator")
                                           + FILENAME_OBSERVATORY_GROUPS;
                        fileGroups = new File(strFolderGroups);

                        docObservatoryGroups = ObservatoryGroupDefinitionsDocument.Factory.parse(fileGroups);

                        if (XmlBeansUtilities.isValidXml(docObservatoryGroups))
                            {
                            final String strFolderAttributes;
                            final File fileAttributes;
                            final AttributesDocument docAttributes;

                            strFolderAttributes = InstallationFolder.getTerminatedUserDir()
                                                   + "plugins/observatory/"
                                                   + DataStore.CONFIG.getLoadFolder()
                                                   + System.getProperty("file.separator")
                                                   + FILENAME_INSTRUMENTS_ATTRIBUTES;
                            fileAttributes = new File(strFolderAttributes);

                            docAttributes = AttributesDocument.Factory.parse(fileAttributes);

                            if (XmlBeansUtilities.isValidXml(docAttributes))
                                {
                                strFolderInstruments = InstallationFolder.getTerminatedUserDir()
                                                       + "plugins/observatory/"
                                                       + DataStore.CONFIG.getLoadFolder();

                                dirInstruments = new File(strFolderInstruments);

                                if (dirInstruments != null)
                                    {
                                    final File [] arrayFiles;

                                    // If this abstract pathname does not denote a directory,
                                    // then this method returns null.
                                    arrayFiles = dirInstruments.listFiles();

                                    if (arrayFiles != null)
                                        {
                                        // Collect the Instruments
                                        collectInstruments(arrayFiles, docInstruments);

                                        // Register the Properties, if any
                                        registerProperties(arrayFiles);
                                        }
                                    else
                                        {
                                        // There are no Instruments
                                        LOGGER.error("Observatory: No Instruments found");
                                        }
                                    }
                                else
                                    {
                                    // Unable to read the Instruments
                                    // This should never occur!
                                    LOGGER.error("Observatory: Faulty Instrument directory structure");
                                    }

                                // Build the UI and initialise it from the list of Instruments,
                                // assigning to Roles and Groups
                                final ObservatoryUIInterface ui;

                                // All three documents are known to be valid
                                // WARNING! This builds the Instruments, Control and Instrument panels,
                                // and so cannot refer to things which don't exist yet! e.g. ObservatoryLog
                                ui = new ObservatoryUI(docInstruments,
                                                       docAttributes,
                                                       docObservatoryGroups,
                                                       REGISTRY_MODEL.getLoggedInUser(),
                                                       atom,
                                                       getResourceKey());
                                setUIComponent(ui);

                                // This will identify the ObservatoryClock and the ObservatoryLog
                                // and set up a Popup Menu to allow Group restructuring
                                getUIComponent().initialiseUI();
                                boolSuccess = PORT_CONTROLLER.start(docInstruments);

                                if (boolSuccess)
                                    {
                                    final int intLatency;

                                    // The PortController Latency is the delay between sending a message and looking for a response
                                    // Defaults to a value of zero
                                    intLatency = REGISTRY.getIntegerProperty(getResourceKey() + ResourceKeys.KEY_PORTCONTROLLER_LATENCY);

                                    if ((intLatency >= 0)
                                        && (intLatency <= 1000))
                                        {
                                        PORT_CONTROLLER.setLatencyMillis(intLatency);
                                        }

                                    LOGGER.login("Port Controller start() [latency=" + PORT_CONTROLLER.getLatencyMillis() + "msec]");

                                    // It is now possible to set the ObservatoryLog on each of the instantiated Ephemerides
                                    setObservatoryLogOnEphemerides(getEphemerisDaoTable(), ui.getObservatoryLog());
                                    }
                                }
                            else
                                {
                                LOGGER.error(ATOM_STARTUP + SPACE + EXCEPTION_XML_VALIDATION);
                                boolSuccess = false;
                                }
                            }
                        else
                            {
                            LOGGER.error(ATOM_STARTUP + SPACE + EXCEPTION_XML_VALIDATION);
                            boolSuccess = false;
                            }
                        }
                    else
                        {
                        LOGGER.error(ATOM_STARTUP + SPACE + "Cannot find imports folder");
                        boolSuccess = false;
                        }
                    }
                else
                    {
                    LOGGER.error(ATOM_STARTUP + SPACE + EXCEPTION_XML_VALIDATION);
                    boolSuccess = false;
                    }
                }

            catch (IOException exception)
                {
                LOGGER.error(ATOM_STARTUP + SPACE + exception.getMessage());
                boolSuccess = false;
                }

            catch (XmlException exception)
                {
                LOGGER.error(ATOM_STARTUP + SPACE + exception.getMessage());
                boolSuccess = false;
                }
            }
        else
            {
            LOGGER.error(ATOM_STARTUP);
            boolSuccess = false;
            }

        if (boolSuccess)
            {
            // Begin to listen for UDP packets on port 30303,
            // so we know the IP addresses of attached controllers
            setUDPWorker(runUdpListener());

            // Pre-load the serial comms library to speed things up later
            CommPortIdentifier.getPortIdentifiers();
            }
        else
            {
            // We need a UIComponent for the Observatory even if the startup failed
            setUIComponent(new BlankUIComponent(getName()));
            getUIComponent().initialiseUI();

            REGISTRY_CONTROLLER.unableToControlPlugin(this);
            }

        return (boolSuccess);
        }


    /***********************************************************************************************
     * Shut down the Atom.
     *
     * @return boolean
     */

    public boolean shutdownAtom()
        {
        // Make sure the UDP Listener goes away
        SwingWorker.disposeWorker(getUDPWorker(), true, SWING_WORKER_STOP_DELAY);
        setUDPWorker(null);

        if (super.shutdownAtom())
            {
            PORT_CONTROLLER.stop();

            // Shutdown the UI
            stopUI();

            if (getUIComponent() != null)
                {
                // Stop all running Instruments, and dispose
                if (getUIComponent() instanceof ObservatoryUIInterface)
                    {
                    final ObservatoryUIInterface ui;
                    final InstrumentSelector selector;
                    final List<ObservatoryInstrumentInterface> instruments;

                    ui = (ObservatoryUIInterface)getUIComponent();

                    // Make sure there is now no Instrument selected
                    selector = ui.getCurrentGroupInstrumentSelector();

                    if (selector != null)
                        {
                        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                               "Observatory.shutdownAtom() NO INSTRUMENT SELECTED");
                        selector.setSelectedInstrument(null);
                        }

                    // Now stop and dispose of *all* Instruments in the Observatory
                    instruments = ui.getObservatoryInstruments();

                    if ((instruments != null)
                        && (!instruments.isEmpty()))
                        {
                        final Iterator<ObservatoryInstrumentInterface> iterInstruments;

                        iterInstruments = instruments.iterator();

                        while (iterInstruments.hasNext())
                            {
                            final ObservatoryInstrumentInterface instrument;

                            instrument = iterInstruments.next();

                            // Stop the Instrument regardless
                            if (InstrumentState.isDoingSomething(instrument))
                                {
                                instrument.stop();
                                }

                            // Is there a DAO Port left to close?
                            if ((instrument.getDAO() != null)
                                && (instrument.getDAO().getPort() != null))
                                {
                                instrument.getDAO().getPort().close();
                                }

                            instrument.dispose();
                            }
                        }
                    }

                getUIComponent().disposeUI();
                setUIComponent(null);

                // Remove all UIComponent ContextActions from the Menu and Toolbar
                REGISTRY_MODEL.rebuildNavigation((TaskPlugin)getRootTask(), null);
                }

            // Remove all Ephemeris entries...
            if (getEphemerisDaoTable() != null)
                {
                getEphemerisDaoTable().clear();
                }

            // ...and Metadata
            if (getObservatoryMetadata() != null)
                {
                getObservatoryMetadata().clear();
                }

            if (getObserverMetadata() != null)
                {
                getObserverMetadata().clear();
                }

            if (getObservatoryMetadataChangedListeners() != null)
                {
                getObservatoryMetadataChangedListeners().clear();
                }

            if (getObserverMetadataChangedListeners() != null)
                {
                getObserverMetadataChangedListeners().clear();
                }

            setObservatoryMetadataLoaded(false);
            setObserverMetadataLoaded(false);

            // ...and POIs, LOIs
            if (getPointOfInterestList() != null)
                {
                getPointOfInterestList().clear();
                }

            if (getLineOfInterestList() != null)
                {
                getLineOfInterestList().clear();
                }

            return (true);
            }
        else
            {
            REGISTRY_CONTROLLER.unableToControlPlugin(this);

            return (false);
            }
        }


    /***********************************************************************************************
     * Run the UI of this UserObjectPlugin when its tree node is selected.
     * setUIOccupant() uses this from the navigation tree, a menu, or a toolbar button.
     */

    public void runUI()
        {
        if (getUIComponent() != null)
            {
            getUIComponent().runUI();
            }

        setCaption(getPathname());
        setStatus(getName());
        }


    /**********************************************************************************************
     * Stop the UI of this UserObjectPlugin when its tree node is deselected.
     */

    public void stopUI()
        {
        if (getUIComponent() != null)
            {
            getUIComponent().stopUI();
            }
        }


    /**********************************************************************************************/
    /* Ephemerides                                                                                */
    /***********************************************************************************************
     * Get the Observatory Ephemerides XML document.
     *
     * @return EphemeridesDocument
     */

    public EphemeridesDocument getEphemeridesDoc()
        {
        return (this.docEphemerides);
        }


    /***********************************************************************************************
     * Get the table of Ephemeris DAOs available to the Observatory (may be empty),
     * keyed by EphemerisName.
     *
     * @return Hashtable{String, EphemerisDAOInterface}
     */

    public Hashtable<String, EphemerisDAOInterface> getEphemerisDaoTable()
        {
        return (this.hashtableEphemeris);
        }


    /**********************************************************************************************/
    /* Metadata                                                                                   */
    /***********************************************************************************************
     * Get the ObservatoryMetadata.
     *
     * @return List<Metadata>
     */

    public List<Metadata> getObservatoryMetadata()
        {
        return (this.listObservatoryMetadata);
        }


    /***********************************************************************************************
     * Get the ObserverMetadata.
     *
     * @return List<Metadata>
     */

    public List<Metadata> getObserverMetadata()
        {
        return (this.listObserverMetadata);
        }


    /***********************************************************************************************
     * Clear all Metadata attached to the Observatory.
     */

    public void clearMetadata()
        {
        if (getObservatoryMetadata() != null)
            {
            getObservatoryMetadata().clear();
            }

        if (getObserverMetadata() != null)
            {
            getObserverMetadata().clear();
            }
        }


    /***********************************************************************************************
     * Indicate if the ObservatoryMetadata are currently loaded.
     *
     * @return boolean
     */

    public boolean areObservatoryMetadataLoaded()
        {
        return (this.boolObservatoryMetadataLoaded);
        }


    /***********************************************************************************************
     * Indicate if the ObservatoryMetadata are currently loaded.
     *
     * @param loaded
     */

    public void setObservatoryMetadataLoaded(final boolean loaded)
        {
        this.boolObservatoryMetadataLoaded = loaded;
        }


    /***********************************************************************************************
     * Indicate if the ObserverMetadata are currently loaded.
     *
     * @return boolean
     */

    public boolean areObserverMetadataLoaded()
        {
        return (this.boolObserverMetadataLoaded);
        }


    /***********************************************************************************************
     * Indicate if the ObserverMetadata are currently loaded.
     *
     * @param loaded
     */

    public void setObserverMetadataLoaded(final boolean loaded)
        {
        this.boolObserverMetadataLoaded = loaded;
        }


    /**********************************************************************************************/
    /* PointOfInterest                                                                            */
    /***********************************************************************************************
     * Add a PointOfInterest to the Observatory.
     *
     * @param poi
     */

    public final void addPointOfInterest(final PointOfInterest poi)
        {
        if ((poi != null)
            && (getPointOfInterestList() != null))
            {
            // Add the PointOfInterest to the list to be rendered
            getPointOfInterestList().add(poi);
            }
        }


    /***********************************************************************************************
     * Remove all PointsOfInterest from the Observatory.
     */

    public void clearPointsOfInterest()
        {
        if (getPointOfInterestList() != null)
            {
            getPointOfInterestList().clear();
            }
        }


    /***********************************************************************************************
     * Get the list of PointsOfInterest.
     *
     * @return List<PointOfInterest>
     */

    public List<PointOfInterest> getPointOfInterestList()
        {
        return (this.listObservatoryPOIs);
        }


    /***********************************************************************************************
     * Set the Points of Interest for the Observatory.
     *
     * @param pois
     */

    public final void setPointOfInterestList(final List<PointOfInterest> pois)
        {
        this.listObservatoryPOIs = pois;
        }


    /**********************************************************************************************/
    /* LineOfInterest                                                                             */
    /***********************************************************************************************
     * Add a LineOfInterest to the Observatory.
     *
     * @param loi
     */

    public final void addLineOfInterest(final LineOfInterest loi)
        {
        if ((loi != null)
            && (getLineOfInterestList() != null))
            {
            // Add the LineOfInterest to the list to be rendered
            getLineOfInterestList().add(loi);
            }
        }


    /***********************************************************************************************
     * Remove all LinesOfInterest from the Observatory.
     */

    public void clearLinesOfInterest()
        {
        if (getLineOfInterestList() != null)
            {
            getLineOfInterestList().clear();
            }
        }


    /***********************************************************************************************
     * Get the list of LinesOfInterest.
     *
     * @return List<LineOfInterest>
     */

    public List<LineOfInterest> getLineOfInterestList()
        {
        return (this.listObservatoryLOIs);
        }


    /***********************************************************************************************
     * Set the Lines of Interest for the Observatory.
     *
     * @param lois
     */

    public final void setLineOfInterestList(final List<LineOfInterest> lois)
        {
        this.listObservatoryLOIs = lois;
        }


    /***********************************************************************************************
     * Collect any Properties from Files named XXX-properties.xml and enter into the Registry.
     *
     * @param files
     */

    private void registerProperties(final File[] files)
        {
        final String SOURCE = "Observatory.registerProperties() ";

        try
            {
            final PropertiesDocument docProperties;

            // Create a container for discovered Properties
            docProperties = PropertiesDocument.Factory.newInstance();
            docProperties.addNewProperties();

            for (final File fileProperties : files)
                {
                // Skip the JARs and the Instruments
                if ((fileProperties != null)
                    && (fileProperties.isFile())
                    && (fileProperties.getName().endsWith(FILENAME_PROPERTIES_SUFFIX)))
                    {
                    final PropertiesDocument docInstrumentProperties;

                    //System.out.println(SOURCE + "PARSING LOADER_PROPERTIES XML " + fileProperties.getName());
                    docInstrumentProperties = PropertiesDocument.Factory.parse(fileProperties);

                    // This document will still be valid if it does not contain any PropertyResources,
                    // but it must contain one Properties element
                    if ((XmlBeansUtilities.isValidXml(docInstrumentProperties))
                        && (docInstrumentProperties.getProperties() != null))
                        {
                        final List<PropertyResource> listPropertyResources;

                        listPropertyResources = docInstrumentProperties.getProperties().getPropertyResourceList();

                        if ((listPropertyResources != null)
                            && (listPropertyResources.size() >= 1))
                            {
                            //System.out.println("ADDING " + fileProperties.getName() + " listResources size=" + listPropertyResources.size());
                            docProperties.getProperties().getPropertyResourceList().addAll(listPropertyResources);
                            }
                        }
                    }
                }

            //--------------------------------------------------------------------------------------
            // Now create all of the PropertyResources, if any

            if ((docProperties.getProperties() != null)
                && (docProperties.getProperties().sizeOfPropertyResourceArray() > 0))
                {
                final PropertiesDocument.Properties properties;
                final List<PropertyResource> listResources;
                final Iterator<PropertyResource> iterResources;

                properties = docProperties.getProperties();
                listResources = properties.getPropertyResourceList();
                iterResources = listResources.iterator();

                while (iterResources.hasNext())
                    {
                    final PropertyResource propertyXml;

                    propertyXml = iterResources.next();

                    // Check that we know enough to import this Property
                    if ((propertyXml != null)
                        && (XmlBeansUtilities.isValidXml(propertyXml)))
                        {
                        final ResourcePlugin plugin;

                        // Initialise the ResourcePlugin from the XML Property configuration
                        plugin = (ResourcePlugin) BEAN_FACTORY_XML.createProperty(this,
                                                                                  propertyXml,
                                                                                  "en");
                        if ((plugin != null)
                            && (plugin.isInstalled())
                            && (plugin.getName() != null)
                            && (plugin.getHostTreeNode() != null)
                            && (plugin.getResourceKey() != null)
                            && (getPropertyExpander() != null))
                            {
                            // Create a unique ID from the host's ID and the plugin hashcode
                            plugin.setID(getID() + plugin.hashCode());

                            // Add this Property to the host Atom
                            addProperty(plugin);

                            if (!REGISTRY.getProperties().containsKey(plugin.getResourceKey()))
                                {
                                // Add this Property to the Registry
                                LOGGER.login("Observatory Registering Property " + plugin.getName());
                                REGISTRY.addProperty(plugin.getResourceKey(), plugin);
                                }

                            // Do some debugging as the import proceeds
                            plugin.setDebugMode(getDebugMode());
                            plugin.showDebugData();
                            }
                        else
                            {
                            LOGGER.error(SOURCE + EXCEPTION_CREATE_PROPERTY + SPACE + propertyXml.getDescription());
                            }
                        }
                    else
                        {
                        LOGGER.error(SOURCE + EXCEPTION_PARAMETER_INVALID);
                        }
                    }
                }

            // Do some debugging as the import proceeds
            showAttachedProperties(getDebugMode());
            REGISTRY.showProperties(getDebugMode());
            }

        catch (Exception exception)
            {
            LOGGER.login(SOURCE + "[exception=" + exception + "]");
            }
        }


    /***********************************************************************************************
     * Get the SwingWorker used to listen for UDP identification messages.
     *
     * @return SwingWorker
     */

    private SwingWorker getUDPWorker()
        {
        return (this.workerUDP);
        }


    /***********************************************************************************************
     * Set the SwingWorker used to listen for UDP identification messages.
     *
     * @param worker
     */

    private void setUDPWorker(final SwingWorker worker)
        {
        this.workerUDP = worker;
        }


    /***********************************************************************************************
     * Indicate TRUE if the Observatory is in Record Macro capture mode,
     * or FALSE if Commands and Macros may be executed.
     *
     * @return boolean
     */

    public boolean isRecordMacroMode()
        {
        return (this.boolRecordMacroMode);
        }


    /***********************************************************************************************
     * Set TRUE if the Observatory is in Record Macro capture mode,
     * or FALSE if Commands and Macros may be executed.
     *
     * @param mode
     */

    public void setRecordMacroMode(final boolean mode)
        {
        this.boolRecordMacroMode = mode;
        }


    /***********************************************************************************************
     * Get an array of the Instruments currently in the Observatory, via the MBean interface for JMX.
     *
     * @return String[]
     */

    public String[] getInstruments()
        {
        final List<String> listNames;

        listNames = new ArrayList<String>(100);

        if ((getUIComponent() != null)
            && (getUIComponent() instanceof ObservatoryUIInterface))
            {
            final List<ObservatoryInstrumentInterface> listObservatoryInstruments;

            listObservatoryInstruments = ((ObservatoryUIInterface)getUIComponent()).getObservatoryInstruments();

            if (listObservatoryInstruments != null)
                {
                final Iterator<ObservatoryInstrumentInterface> iterInstruments;

                iterInstruments = listObservatoryInstruments.iterator();

                while (iterInstruments.hasNext())
                    {
                    final ObservatoryInstrumentInterface instrument;

                    instrument = iterInstruments.next();

                    if ((instrument != null)
                        && (instrument.getInstrument() != null))
                        {
                        listNames.add(instrument.getInstrument().getIdentifier() + " is " + instrument.getInstrumentState().getStatus());
                        }
                    }
                }
            }

        return ((String[])listNames.toArray());
        }


    /***********************************************************************************************
     * Read all Resources required by Observatory.
     */

    public void readResources()
        {
        super.readResources();

        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               "Observatory.readResources() [ResourceKey=" + getResourceKey() + "]");
        }


    /**********************************************************************************************/
    /* Events                                                                                     */
    /***********************************************************************************************
     * Notify all listeners of ObservatoryMetadataChangedEvents.
     *
     * @param eventsource
     * @param metadatakey
     * @param state
     */

    public final void notifyObservatoryMetadataChangedEvent(final Object eventsource,
                                                            final String metadatakey,
                                                            final MetadataItemState state)
        {
        List<ObservatoryMetadataChangedListener> listeners;
        final ObservatoryMetadataChangedEvent changeEvent;

        // Create a Thread-safe List of Listeners
        listeners = new CopyOnWriteArrayList<ObservatoryMetadataChangedListener>(getObservatoryMetadataChangedListeners());

        // Create an ObservatoryMetadataChangedEvent
        changeEvent = new ObservatoryMetadataChangedEvent(eventsource, metadatakey, state);

        // Fire the event to every listener
        synchronized(listeners)
            {
            for (int i = 0; i < listeners.size(); i++)
                {
                final ObservatoryMetadataChangedListener changeListener;

                changeListener = listeners.get(i);
                changeListener.observatoryChanged(changeEvent);
                }
            }

        // Help the GC?
        listeners = null;
        }


    /***********************************************************************************************
     * Get the ObservatoryMetadataChanged Listeners (mostly for testing).
     *
     * @return Vector<ObservatoryMetadataChangedListener>
     */

    public final Vector<ObservatoryMetadataChangedListener> getObservatoryMetadataChangedListeners()
        {
        return (this.vecObservatoryMetadataChangedListeners);
        }


    /***********************************************************************************************
     * Add a listener for this event, uniquely.
     *
     * @param listener
     */

    public final void addObservatoryMetadataChangedListener(final ObservatoryMetadataChangedListener listener)
        {
        if ((listener != null)
            && (getObservatoryMetadataChangedListeners() != null)
            && (!getObservatoryMetadataChangedListeners().contains(listener)))
            {
            getObservatoryMetadataChangedListeners().addElement(listener);
            LOGGER.debug(LOADER_PROPERTIES.isMetadataDebug(),
                         "Observatory.addObservatoryMetadataChangedListener() [count=" + getObservatoryMetadataChangedListeners().size()
                               + "] [class=" + listener.getClass().getName() + "]");
            }
        }


    /***********************************************************************************************
     * Remove a listener for this event.
     *
     * @param listener
     */

    public final void removeObservatoryMetadataChangedListener(final ObservatoryMetadataChangedListener listener)
        {
        if ((listener != null)
            && (getObservatoryMetadataChangedListeners() != null))
            {
            getObservatoryMetadataChangedListeners().removeElement(listener);
            LOGGER.debug(LOADER_PROPERTIES.isMetadataDebug(),
                         "Observatory.removeObservatoryMetadataChangedListener() [count=" + getObservatoryMetadataChangedListeners().size()
                               + "] [class=" + listener.getClass().getName() + "]");
            }
        }


    /***********************************************************************************************
     * Notify all listeners of ObserverMetadataChangedEvents.
     *
     * @param eventsource
     * @param metadatakey
     * @param state
     */

    public final void notifyObserverMetadataChangedEvent(final Object eventsource,
                                                         final String metadatakey,
                                                         final MetadataItemState state)
        {
        List<ObserverMetadataChangedListener> listeners;
        final ObserverMetadataChangedEvent changeEvent;

        // Create a Thread-safe List of Listeners
        listeners = new CopyOnWriteArrayList<ObserverMetadataChangedListener>(getObserverMetadataChangedListeners());

        // Create an ObserverMetadataChangedEvent
        changeEvent = new ObserverMetadataChangedEvent(eventsource, metadatakey, state);

        // Fire the event to every listener
        synchronized(listeners)
            {
            for (int i = 0; i < listeners.size(); i++)
                {
                final ObserverMetadataChangedListener changeListener;

                changeListener = listeners.get(i);
                changeListener.observerChanged(changeEvent);
                }
            }

        // Help the GC?
        listeners = null;
        }


    /***********************************************************************************************
     * Get the ObserverMetadataChanged Listeners (mostly for testing).
     *
     * @return Vector<ObserverMetadataChangedListener>
     */

    public final Vector<ObserverMetadataChangedListener> getObserverMetadataChangedListeners()
        {
        return (this.vecObserverMetadataChangedListeners);
        }


    /***********************************************************************************************
     * Add a listener for this event, uniquely.
     *
     * @param listener
     */

    public final void addObserverMetadataChangedListener(final ObserverMetadataChangedListener listener)
        {
        if ((listener != null)
            && (getObserverMetadataChangedListeners() != null)
            && (!getObserverMetadataChangedListeners().contains(listener)))
            {
            getObserverMetadataChangedListeners().addElement(listener);
            LOGGER.debug(LOADER_PROPERTIES.isMetadataDebug(),
                         "Observatory.addObserverMetadataChangedListener() [count=" + getObserverMetadataChangedListeners().size()
                               + "] [class=" + listener.getClass().getName() + "]");
            }
        }


    /***********************************************************************************************
     * Remove a listener for this event.
     *
     * @param listener
     */

    public final void removeObserverMetadataChangedListener(final ObserverMetadataChangedListener listener)
        {
        if ((listener != null)
            && (getObserverMetadataChangedListeners() != null))
            {
            getObserverMetadataChangedListeners().removeElement(listener);
            LOGGER.debug(LOADER_PROPERTIES.isMetadataDebug(),
                         "Observatory.removeObserverMetadataChangedListener() [count=" + getObserverMetadataChangedListeners().size()
                               + "] [class=" + listener.getClass().getName() + "]");
            }
        }
    }
