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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments;

import org.lmn.fc.common.exceptions.FrameworkException;
import org.lmn.fc.common.xml.XmlBeansUtilities;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ObservatoryInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.PointOfInterestType;
import org.lmn.fc.frameworks.starbase.plugins.observatory.events.InstrumentStateChangedEvent;
import org.lmn.fc.frameworks.starbase.plugins.observatory.events.InstrumentStateChangedListener;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.InstrumentHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.MetadataHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.PointOfInterestHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.ViewingMode;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.logs.LogHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.logs.SimpleEventLogUIComponent;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.macros.MacroManagerUtilities;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandProcessorContextInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.PortControllerInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.impl.CommandProcessorContext;
import org.lmn.fc.frameworks.starbase.portcontroller.impl.PortController;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.plugins.AtomPlugin;
import org.lmn.fc.model.registry.RegistryModelPlugin;
import org.lmn.fc.model.registry.RegistryModelUtilities;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;
import org.lmn.fc.model.xmlbeans.instruments.MacrosDocument;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;
import org.lmn.fc.model.xmlbeans.poi.LineOfInterest;
import org.lmn.fc.model.xmlbeans.poi.PointOfInterest;
import org.lmn.fc.ui.layout.BoxLayoutFixed;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;


/***************************************************************************************************
 * The base class for an ObservatoryInstrument.
 */

public abstract class AbstractObservatoryInstrument implements ObservatoryInstrumentInterface
    {
    // This is the same as SimpleEventLogUIComponent.COLUMN_COUNT
    // The default EventLog has Icon, Date, Time, Event, Source
    private static final int DEFAULT_EVENTLOG_WIDTH = 5;

    // This is the same as SimpleInstrumentLogUIComponent.COLUMN_COUNT
    private static final int DEFAULT_INSTRUMENTLOG_WIDTH = 5;

    private static final int LOGSIZE_MAX = 5000;

    // This cannot be in FrameworkSingletons because it won't be loaded at the right time...
    private static final PortControllerInterface PORT_CONTROLLER = PortController.getInstance();

    // Instrument Context
    private Instrument instrumentXml;
    private final AtomPlugin hostAtom;
    private final ObservatoryUIInterface hostUI;
    private ObservatoryInstrumentDAOInterface daoInstrument;
    private final String strResourceKey;

    // Instrument State
    private InstrumentState state;
    private CommandProcessorContextInterface contextInterface;
    private Vector<Vector> vecInstrumentConfiguration;
    private MacrosDocument docInstrumentMacros;
    private Vector<InstrumentStateChangedListener> vecInstrumentStateChangedListeners;

    // Composite Data
    private List<Metadata> listAggregateMetadata;
    private List<PointOfInterest> listCompositePOIs;
    private List<LineOfInterest> listCompositeLOIs;

    // UI Components
    private JComponent hostPanel;
    private JComponent selectorPanel;
    private InstrumentUIComponentDecoratorInterface controlPanel;
    private InstrumentUIComponentDecoratorInterface instrumentPanel;
    private final JButton buttonOn;
    private final JButton buttonOff;

    // Utilities
    private final Vector<Vector> vecEventLog;
    private int intEventLogIndex;
    private final Vector<Vector> vecInstrumentLog;
    private int intInstrumentLogIndex;

    // Configurable Resources
    private FontInterface pluginFont;
    private ColourInterface pluginColour;


    /***********************************************************************************************
     * Construct an AbstractObservatoryInstrument.
     * All subclasses must have constructors with the same signature,
     * in order for instantiateInstrument() to work via reflection.
     *
     * @param instrument
     * @param hostatom
     * @param hostui
     * @param resourcekey
     */

    public AbstractObservatoryInstrument(final Instrument instrument,
                                         final AtomPlugin hostatom,
                                         final ObservatoryUIInterface hostui,
                                         final String resourcekey)
        {
        if ((instrument == null)
            || (!XmlBeansUtilities.isValidXml(instrument))
            || (instrument.getResourceKey() == null)
            || (EMPTY_STRING.equals(instrument.getResourceKey().trim()))
            || (hostatom == null)
            || (!hostatom.validatePlugin())
            || (hostui == null)
            || (hostui.getInstrumentsDoc() == null)
            || (!XmlBeansUtilities.isValidXml(hostui.getInstrumentsDoc()))
            || (resourcekey == null)
            || (EMPTY_STRING.equals(resourcekey)))
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_INVALID);
            }

        this.instrumentXml = instrument;
        this.hostAtom = hostatom;
        this.hostUI = hostui;

        // Form the composite ResourceKey from the HostAtom and the Instrument keys
        this.strResourceKey = resourcekey
                                + instrument.getResourceKey()
                                + RegistryModelPlugin.DELIMITER_RESOURCE;

        this.selectorPanel = new JPanel();
        this.hostPanel = new JPanel();
        this.controlPanel = null;
        this.buttonOn = new JButton();
        this.buttonOff = new JButton();
        this.instrumentPanel = null;

        this.state = InstrumentState.CREATED;
        this.vecInstrumentStateChangedListeners = new Vector<InstrumentStateChangedListener>(10);
        this.vecInstrumentConfiguration = new Vector<Vector>(50);
        this.docInstrumentMacros = null;

        // Aggregate Data
        this.listAggregateMetadata = new ArrayList<Metadata>(50);
        this.listCompositePOIs = new ArrayList<PointOfInterest>(50);
        this.listCompositeLOIs = new ArrayList<LineOfInterest>(50);

        this.vecEventLog = new Vector<Vector>(1000);
        this.intEventLogIndex = 0;
        this.vecInstrumentLog = new Vector<Vector>(1000);
        this.intInstrumentLogIndex = 0;
        }


    /***********************************************************************************************
     * Initialise the ObservatoryInstrument.
     * TODO - don't believe this! ---- The InstrumentPanel must be created by the subclasses, before this method is called.
     */

    public void initialise()
        {
        final String SOURCE = "AbstractObservatoryInstrument.initialise() ";
        final ObservatoryInstrumentInterface observatoryInstrument;
        final JLabel labelRackPanel;
        ImageIcon iconPanelBackground;
        final Dimension dimPanel;

        // For use within the MouseAdaptor
        observatoryInstrument = this;

        //LOGGER.debugTimedEvent("ObservatoryInstrument initialise() " + getInstrument().getName());

        // The SelectorPanel holds everything
        setSelectorPanel(new JPanel());
        getSelectorPanel().setLayout(new OverlayLayout(getSelectorPanel()));
        getSelectorPanel().setOpaque(true);
        getSelectorPanel().setToolTipText(getInstrument().getDescription());

        setHostPanel(new JPanel());
        getHostPanel().setLayout(new BoxLayoutFixed(getHostPanel(), BoxLayoutFixed.Y_AXIS));
        getHostPanel().setOpaque(false);

        //-----------------------------------------------------------------------------------------
        // The background Icon must be the *last* item to be added...
        // See if an Custom background image is supplied. If not, use the standard RackPanel icon.

        if ((getInstrument().getSelectorPanelFilename() != null)
            && (!EMPTY_STRING.equals(getInstrument().getSelectorPanelFilename().trim())))
            {
            final URL url;

//            System.out.println(SOURCE + "Custom SelectorPanel requested [filename=" + getInstrument().getSelectorPanelFilename() + "]");

            // Find a resource of the specified name from the search path used to load classes
            // This method locates the resource through the system class loader
            // URL() has a different way of addressing resources!
            // The usual path is "/org/lmn/fc/frameworks/starbase/plugins/observatory/imports"
            url = getClass().getResource(getInstrument().getSelectorPanelFilename());
            iconPanelBackground = new ImageIcon(url);

            // Now make SelectorPanelHeight consistent with the image just loaded
            if ((iconPanelBackground.getIconHeight() > 0)
                && (iconPanelBackground.getIconWidth() > 0))
                {
                // Quantise pixel heights to heights in RackPanel 'U'
                getInstrument().setSelectorPanelHeight(RackPanel.getRackPanelHeightForPixels(iconPanelBackground.getIconHeight()));

                // ToDo Decide what to do about anomalies in the width!
//                System.out.println(SOURCE + "Adjusting SelectorPanelHeight [height.panel=" + getInstrument().getSelectorPanelHeight()
//                                    + "] [height.icon=" + iconPanelBackground.getIconHeight() + "]");
                }
            else
                {
                final RackPanel rackPanel;

                // Force use of the default, determined by the SelectorPanelHeight **which must exist**
                // The sizes in this route will be incorrect if no filename is supplied for a Separator
                // Ideally need to determine if the panel is a Separator,
                // and use SeparatorPanel instead of RackPanel
//                System.out.println(SOURCE + "Invalid Custom SelectorPanel dimensions [height=" + iconPanelBackground.getIconHeight()
//                                    + "] [width=" + iconPanelBackground.getIconWidth() + "]");
//
//                System.out.println(SOURCE + "Using SelectorPanelHeight [height=" + getInstrument().getSelectorPanelHeight() + "]");

                // Return RackPanel.PANEL_1U on failure
                rackPanel = RackPanel.getRackPanelForHeight(getInstrument().getSelectorPanelHeight());
                // Resources found with getClass().getResource()
                iconPanelBackground = RegistryModelUtilities.getAtomIcon(getHostAtom(), rackPanel.getImageFileName());
                }
            }
        else
            {
            final RackPanel rackPanel;

            // No Custom SelectorPanel is defined, so use a default, determined by the SelectorPanelHeight **which must exist**
            // The sizes in this route will be incorrect if no filename is supplied for a Separator
            // Ideally need to determine if the panel is a Separator,
            // and use SeparatorPanel instead of RackPanel
//            System.out.println(SOURCE + "Using SelectorPanelHeight [height=" + getInstrument().getSelectorPanelHeight() + "]");

            // Return RackPanel.PANEL_1U on failure
            rackPanel = RackPanel.getRackPanelForHeight(getInstrument().getSelectorPanelHeight());
            // Resources found with getClass().getResource()
            iconPanelBackground = RegistryModelUtilities.getAtomIcon(getHostAtom(), rackPanel.getImageFileName());
            }

        //-----------------------------------------------------------------------------------------
        // Set up the host panel and its label

        dimPanel = new Dimension(iconPanelBackground.getIconWidth() - RackPanel.HOST_PANEL_INSET_WIDTH,
                                 iconPanelBackground.getIconHeight() - RackPanel.HOST_PANEL_INSET_HEIGHT);

        getHostPanel().setMinimumSize(dimPanel);
        getHostPanel().setMaximumSize(dimPanel);
        getHostPanel().setPreferredSize(dimPanel);

//        getHostPanel().setMinimumSize(RackPanel.getHostPanelSize(intPanelHeight));
//        getHostPanel().setMaximumSize(RackPanel.getHostPanelSize(intPanelHeight));
//        getHostPanel().setPreferredSize(RackPanel.getHostPanelSize(intPanelHeight));
//
        getHostPanel().setAlignmentX(InstrumentSelector.ALIGNMENT_XY);
        getHostPanel().setAlignmentY(InstrumentSelector.ALIGNMENT_XY);
        getSelectorPanel().add(getHostPanel());

        labelRackPanel = new JLabel();
        labelRackPanel.setIcon(iconPanelBackground);
        labelRackPanel.setToolTipText(getInstrument().getDescription());
        labelRackPanel.setAlignmentX(InstrumentSelector.ALIGNMENT_XY);
        labelRackPanel.setAlignmentY(InstrumentSelector.ALIGNMENT_XY);
        getSelectorPanel().add(labelRackPanel);

        final MouseAdapter adapter = new MouseAdapter()
            {
            public void mouseClicked(final MouseEvent event)
                {
                //LOGGER.debugTimedEvent("Click on SelectorPanel of " + getInstrument().getName());

                // Allow the User to switch Instruments by left-clicking on the SelectorPanel
                if (SwingUtilities.isLeftMouseButton(event)
                        && (!event.isShiftDown())
                        && (!event.isAltDown())
                        && (event.getClickCount() == 1))
                    {
                    if ((getHostUI() != null)
                        && (getInstrumentPanel() != null))
                        {
                        // Select the Instrument
                        // See similar code in ObservatoryInstrumentHelper.createButtonPanel()
                        if (getHostUI().getCurrentGroupInstrumentSelector() != null)
                            {
    //                        LOGGER.debugProtocolEvent("AbstractObservatoryInstrument.initialise()  mouseClicked() SELECT "
    //                                                    + getInstrument().getName());
                            getHostUI().getCurrentGroupInstrumentSelector().setSelectedInstrument(observatoryInstrument);
                            }

                        // Hide the existing UI, if different from the requested UI
                        // Show the new UI in its previous state
                        getHostUI().setUIOccupant(getInstrumentPanel());
                        }
                    }

                // Show a Popup Menu to allow Group restructuring
                if ((getHostUI().getInstrumentRearrangerMenu() != null)
                    && (SwingUtilities.isRightMouseButton(event))
                    && (!event.isShiftDown())
                    && (!event.isAltDown())
                    && (event.getClickCount() == 1))
                    {
                    // Show the menu to the right of the ControlPanel,
                    // if the Instrument is not locked against rearrangements
                    getHostUI().getInstrumentRearrangerMenu().show(getHostUI(),
                                                                   observatoryInstrument,
                                                                   labelRackPanel,
                                                                   labelRackPanel.getWidth(),
                                                                   0,
                                                                   event.isControlDown());
                    }

                // Capture the image of the ControlPanel for documentation
                if ((SwingUtilities.isRightMouseButton(event))
                    && (event.isShiftDown())
                    && (event.isControlDown())
                    && (event.getClickCount() == 1))
                    {
                    System.out.println("CLICK! Capture the image of the ControlPanel for documentation");
                    }
                }
            };

        labelRackPanel.addMouseListener(adapter);

        // The only call outside of CommandProcessorContext.instrumentChanged()
        setInstrumentState(InstrumentState.CREATED);

        // Make things cleaner by passing round a context object...
        setContext(new CommandProcessorContext((ObservatoryInterface)getHostAtom(),
                                               (TaskPlugin)getHostAtom().getRootTask(),
                                               getHostUI(),
                                               this,
                                               getInstrument(),
                                               getInstrumentState(),
                                               getFontData(),
                                               getColourData(),
                                               getResourceKey()));

        // ...which knows everything that's going on
        addInstrumentStateChangedListener(getContext());

        // Anything on the InstrumentPanel may now make use of the Context
        if (getInstrumentPanel() != null)
            {
            getInstrumentPanel().initialise();
            }

        // Make sure all message-driven UI is in sync
        notifyInstrumentStateChangedEvent(this,
                                          this,
                                          getInstrumentState(),
                                          InstrumentState.INITIALISED,
                                          0,
                                          UNEXPECTED);
        }


    /***********************************************************************************************
     * Start this ObservatoryInstrument.
     * See the code in AbstractObservatoryInstrumentDAO start() and stop(),
     * and ObservatoryInstrumentHelper.createButtonPanel() and start().
     *
     * @return boolean
     */

    public synchronized boolean start()
        {
        boolean boolSuccess;

        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               "AbstractObservatoryInstrument.start() " + getInstrument().getName());

        boolSuccess = false;

        try
            {
            boolSuccess = ObservatoryInstrumentHelper.startInstrument(this);

            if (getContext() != null)
                {
                // Always come back viewing Commands
                getContext().setViewingMode(ViewingMode.COMMAND_LOG);
                MacroManagerUtilities.restoreCommandLifecycleLog(getContext());
                }
            }

        // This will catch 'InvocationTargetExceptions', so we know what they are
        catch (final Exception exception)
            {
            LOGGER.error("AbstractObservatoryInstrument.start() [exception=" + exception.getMessage() + "]");

            exception.printStackTrace();
            }

        if (boolSuccess)
            {
            // Move into the READY state
            notifyInstrumentStateChangedEvent(this,
                                              this,
                                              getInstrumentState(),
                                              InstrumentState.READY,
                                              0,
                                              UNEXPECTED);
            }

        return (boolSuccess);
        }


    /***********************************************************************************************
     * Stop this ObservatoryInstrument.
     * See the code in AbstractObservatoryInstrumentDAO start() and stop(),
     * and ObservatoryInstrumentHelper.createButtonPanel() and stop().
     *
     * @return boolean
     */

    public synchronized boolean stop()
        {
        boolean boolSuccess;

        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               "AbstractObservatoryInstrument.stop() " + getInstrument().getName());

        boolSuccess= false;

        try
            {
            if (getDAO() != null)
                {
                // Close the Port and Streams if we can
                boolSuccess = ObservatoryInstrumentHelper.stopInstrument(this, getDAO(), getDAO().getPort());

                // Finally remove the DAO
                getDAO().disposeDAO();
                setDAO(null);
                }
            }

        catch (final IOException exception)
            {
            LOGGER.error("AbstractObservatoryInstrument.stop() [exception=" + exception.getMessage() + "]");
            }

        if (getContext() != null)
            {
            // Always come back viewing Commands
            getContext().setViewingMode(ViewingMode.COMMAND_LOG);
            MacroManagerUtilities.restoreCommandLifecycleLog(getContext());
            }

        // Move into the STOPPED state regardless of the outcome
        notifyInstrumentStateChangedEvent(this,
                                          this,
                                          getInstrumentState(),
                                          InstrumentState.STOPPED,
                                          0,
                                          UNEXPECTED);

        return (boolSuccess);
        }


    /***********************************************************************************************
     * Dispose of all components of this Instrument.
     */

    public synchronized void dispose()
        {
        //LOGGER.debugTimedEvent("ObservatoryInstrument dispose() " + getInstrument().getName());

        stop();

        if (getControlPanel() != null)
            {
            getControlPanel().disposeUI();
            }

        if (getInstrumentPanel() != null)
            {
            getInstrumentPanel().disposeUI();
            getInstrumentPanel().dispose();
            }

        // This may be academic now, but do it anyway...
        if ((getContext() != null)
            && (getContext().getFSM() != null))
            {
            getContext().getFSM().doTransition(getInstrumentState(), InstrumentState.DISPOSED);
            }
        }


    /***********************************************************************************************
     * Reset the ObservatoryInstrument.
     *
     * @param resetmode
     */

    public void reset(final ResetMode resetmode)
        {
        final String SOURCE = "AbstractObservatoryInstrument.reset() ";
        final boolean boolDebug;

        boolDebug = (LOADER_PROPERTIES.isMasterDebug()
                     || LOADER_PROPERTIES.isStateDebug()
                     || LOADER_PROPERTIES.isTimingDebug()
                     || LOADER_PROPERTIES.isStaribusDebug()
                     || LOADER_PROPERTIES.isStarinetDebug());

        LOGGER.debugTimedEvent(boolDebug,
                               SOURCE + getInstrument().getName());

        // Update the Instrument Resources
        readResources();

        // Do the reset() operations for the Instrument, its DAO and anything else...
        switch (resetmode)
            {
            case DEFAULTS:
                {
                LOGGER.debugTimedEvent(boolDebug,
                                       SOURCE + "--> reset(Defaults)");

                // Remove the Instrument Aggregate Metadata
                if (getAggregateMetadata() != null)
                    {
                    getAggregateMetadata().clear();
                    }
                else
                    {
                    setAggregateMetadata(new ArrayList<Metadata>(100));
                    }

                // DAO reset() must NOT remove Framework, Observatory or Observer Metadata!
                // The DAO can control only Observation, Instrument, Controller, Plugin

                // Initialise all DAO data and Metadata containers if possible
                if (getDAO() != null)
                    {
                    // Get the latest DAO Resources
                    getDAO().readResources();

                    if ((getDAO().getWrappedData() != null)
                        && (getDAO().getWrappedData().getEventLogFragment() != null))
                        {
                        getDAO().getWrappedData().getEventLogFragment().clear();
                        }

                    getDAO().clearEventLogFragment();

                    // This does setWrappedData()
                    // This takes account of isInstrumentDataConsumer()
                    getDAO().clearData();

                    ObservatoryInstrumentHelper.logReset(this, getInstrument(), getDAO(), resetmode, SOURCE);
                    }

                // Recollect the Instrument's Aggregate Metadata just to be sure
                // This should contain any in Framework, Observatory, Observer or Instrument schema
                if ((getInstrument() != null)
                    && (getContext() != null)
                    && (getDAO() != null))
                    {
                    final String strIdentifier;
                    final List<Metadata> listMetadata;

                    strIdentifier = getInstrument().getIdentifier();

                    listMetadata = MetadataHelper.collectAggregateMetadataTraced(REGISTRY.getFramework(),
                                                                                 getContext().getObservatory(),
                                                                                 this,
                                                                                 getDAO(),
                                                                                 getDAO().getWrappedData(),
                                                                                 SOURCE,
                                                                                 LOADER_PROPERTIES.isMetadataDebug());
                    // Set the Aggregate Metadata on the host Instrument
                    // All e.g. Control panel data are taken from here
                    // NOTE THAT The DAO data take precedence over those in the Wrapper
                    setAggregateMetadata(listMetadata);

                    SimpleEventLogUIComponent.logEvent(getDAO().getEventLogFragment(),
                                                       EventStatus.INFO,
                                                       METADATA_TARGET
                                                            + strIdentifier + TERMINATOR
                                                       + METADATA_ACTION_COLLECT_METADATA,
                                                       SOURCE,
                                                       getObservatoryClock());
                    }

                // Also remove all POI and LOI
                PointOfInterestHelper.removeFrameworkPOIandLOI(PointOfInterestType.FRAMEWORK,
                                                               REGISTRY.getFramework(),
                                                               getDAO());

                PointOfInterestHelper.removeObservatoryPOIandLOI(PointOfInterestType.OBSERVATORY,
                                                                 getDAO());


                // Clear the Instrument Composite POIs and LOIs (NOT those from the schema)
                PointOfInterestHelper.removeInstrumentPOIandLOI(PointOfInterestType.INSTRUMENT,
                                                                getDAO());

                // Leave the configuration alone, since it is never changed by the DAO
//                if (getDynamicConfiguration() != null)
//                    {
//                    getDynamicConfiguration().clear();
//                    }

                // Clear the InstrumentLog, but not the EventLog
                getInstrumentLog().clear();

                // Something has changed, we may need to update indicators etc.
                REGISTRY.getFramework().notifyFrameworkChangedEvent(this);
                InstrumentHelper.notifyInstrumentChanged(this);

                // A bit naughty, but a good idea here
                ObservatoryInstrumentHelper.runGarbageCollector();

                break;
                }

            case SOFT:
                {
                LOGGER.debugTimedEvent(boolDebug,
                                       SOURCE + "--> reset(Soft)");
                if (getDAO() != null)
                    {
                    // Get the latest DAO Resources
                    getDAO().readResources();

                    // Leave all Data, Metadata, POI and LOI alone
                    if ((getDAO().getWrappedData() != null)
                        && (getDAO().getWrappedData().getEventLogFragment() != null))
                        {
                        getDAO().getWrappedData().getEventLogFragment().clear();
                        }

                    getDAO().clearEventLogFragment();

                    ObservatoryInstrumentHelper.logReset(this, getInstrument(), getDAO(), resetmode, SOURCE);
                    }

                // Something has changed, we may need to update indicators etc.
                REGISTRY.getFramework().notifyFrameworkChangedEvent(this);
                InstrumentHelper.notifyInstrumentChanged(this);

                break;
                }

            case STARIBUS:
                {
                LOGGER.debugTimedEvent(boolDebug,
                                       SOURCE + "--> reset(Staribus)");
                if (getDAO() != null)
                    {
                    // Get the latest DAO Resources
                    getDAO().readResources();

                    // Leave all Data, Metadata, POI and LOI alone
                    if ((getDAO().getWrappedData() != null)
                        && (getDAO().getWrappedData().getEventLogFragment() != null))
                        {
                        getDAO().getWrappedData().getEventLogFragment().clear();
                        }

                    getDAO().clearEventLogFragment();

                    ObservatoryInstrumentHelper.logReset(this, getInstrument(), getDAO(), resetmode, SOURCE);
                    }

                // Something has changed, we may need to update indicators etc.
                REGISTRY.getFramework().notifyFrameworkChangedEvent(this);
                InstrumentHelper.notifyInstrumentChanged(this);

                break;
                }

            default:
                {
                LOGGER.error(SOURCE + "Invalid ResetMode");
                }
            }

        if (getInstrumentPanel() != null)
            {
            LOGGER.debugTimedEvent(boolDebug,
                                   SOURCE + "--> Go to reset instrument panel");

            getInstrumentPanel().reset(resetmode);
            }
        }


    /***********************************************************************************************
     * Get the ObservatoryInstrumentDAO.
     *
     * @return ObservatoryInstrumentDAOInterface
     */

    public synchronized ObservatoryInstrumentDAOInterface getDAO()
        {
        return (this.daoInstrument);
        }


    /***********************************************************************************************
     * Set the ObservatoryInstrumentDAO.
     *
     * @param dao
     */

    public synchronized void setDAO(final ObservatoryInstrumentDAOInterface dao)
        {
        this.daoInstrument = dao;
        }


    /***********************************************************************************************
     * This method is called (on the Event Dispatching Thread)
     * by the Update SwingWorker when the update operation is complete and data are available.
     * The Instrument may pass data to a UIComponent, or perform further processing.
     * Optionally refresh the UI of data tabs or update the associated Metadata.
     *
     * @param daowrapper
     * @param forcerefreshdata
     * @param updatemetadata
     */

    public void setWrappedData(final DAOWrapperInterface daowrapper,
                               final boolean forcerefreshdata,
                               final boolean updatemetadata)
        {
        final String SOURCE = "AbstractObservatoryInstrument.setWrappedData() " ;
        final boolean boolDebug;

        boolDebug = (LOADER_PROPERTIES.isTimingDebug()
                     || LOADER_PROPERTIES.isChartDebug()
                     || LOADER_PROPERTIES.isMetadataDebug()
                     || LOADER_PROPERTIES.isThreadsDebug()
                     || LOADER_PROPERTIES.isStaribusDebug()
                     || LOADER_PROPERTIES.isStarinetDebug());

        LOGGER.debug(boolDebug,
                     SOURCE + "INSTRUMENT------------------------------------------------------------------------------------");
        LOGGER.debug(boolDebug,
                     SOURCE + "Set Wrapped data on the INSTRUMENT [instrument.class" + getClass().getName() + "]");

        // In this current implementation, the wrapped data are not passed to the ControlPanel

        if ((daowrapper != null)
            && (getInstrumentPanel() != null))
            {
            LOGGER.debug(boolDebug,
                         SOURCE + "Pass WrappedData to the InstrumentPanel [forcerefreshdata=" + forcerefreshdata
                                + "] [updatemetadata=" + updatemetadata + "]");

            getInstrumentPanel().setWrappedData(daowrapper,
                                                forcerefreshdata,
                                                updatemetadata);
            }
        }


    /***********************************************************************************************
     * Remove any Data associated with this Instrument's appearance on the UI,
     * on the InstrumentPanel. For instance, remove a Chart.
     */

    public void removeInstrumentIdentity()
        {
        final String SOURCE = "AbstractObservatoryInstrument.removeInstrumentIdentity() " ;

        LOGGER.debug(LOADER_PROPERTIES.isChartDebug(),
                     SOURCE);

        if (getInstrumentPanel() != null)
            {
            getInstrumentPanel().removeInstrumentIdentity();
            }
        }


    /***********************************************************************************************
     * Get the InstrumentState.
     *
     * @return InstrumentState
     */

    public final InstrumentState getInstrumentState()
        {
        return (this.state);
        }


    /***********************************************************************************************
     * Set the InstrumentState.
     *
     * @param newstate
     */

    public final void setInstrumentState(final InstrumentState newstate)
        {
        // The transitions are validated in a simple finite state machine
        // in CommandProcessorContext.instrumentChanged()
        // which is reached after a notifyInstrumentStateChangedEvent().

        this.state = newstate;
        }


    /**********************************************************************************************/
    /* Provide access to useful Instruments                                                       */
    /***********************************************************************************************
     * Get the ObservatoryClock.
     *
     * @return ObservatoryClockInterface
     */

    public ObservatoryClockInterface getObservatoryClock()
        {
        ObservatoryClockInterface clock;

        if ((getHostUI() != null)
            && (getHostUI().getObservatoryClock() != null))
            {
            clock = getHostUI().getObservatoryClock();
            }
        else
            {
            // We should have a real or simulated Clock by this stage...
            throw new FrameworkException(EXCEPTION_PARAMETER_NULL);
            }

        return (clock);
        }


    /**********************************************************************************************/
    /* UI Components                                                                              */
    /***********************************************************************************************
     * Get the Instrument HostPanel.
     *
     * @return JComponent
     */

    public JComponent getHostPanel()
        {
        return (this.hostPanel);
        }


    /***********************************************************************************************
     * Set the Instrument HostPanel.
     *
     * @param panel
     */

    private void setHostPanel(final JComponent panel)
        {
        this.hostPanel = panel;
        }


    /***********************************************************************************************
     * Get the Instrument SelectorPanel (used by RackCabinet).
     *
     * @return JComponent
     */

    public final JComponent getSelectorPanel()
        {
        return (this.selectorPanel);
        }


    /***********************************************************************************************
     * Set the Instrument SelectorPanel.
     *
     * @param panel
     */

    public final void setSelectorPanel(final JComponent panel)
        {
        this.selectorPanel = panel;
        }


    /***********************************************************************************************
     * Get the Instrument ControlPanel.
     *
     * @return InstrumentUIComponentDecorator
     */

    public final InstrumentUIComponentDecoratorInterface getControlPanel()
        {
        return (this.controlPanel);
        }


    /***********************************************************************************************
     * Set the ControlPanel to a decorated version of a UIComponent.
     * Show the specified display name in the lower right of the panel.
     *
     * @param panel
     * @param displayname
     */

    public final void setControlPanel(final InstrumentUIComponentDecoratorInterface panel,
                                      final String displayname)
        {
        final JComponent footerPanel;
        final JPanel panelButtons;
        final JLabel labelText;
        final StringBuffer buffer;

        if (panel != null)
            {
            this.controlPanel = panel;

            getControlPanel().setOpaque(false);
            getControlPanel().setMinimumSize(RackPanel.getControlPanelSize(getInstrument().getSelectorPanelHeight()));
            getControlPanel().setMaximumSize(RackPanel.getControlPanelSize(getInstrument().getSelectorPanelHeight()));
            getControlPanel().setPreferredSize(RackPanel.getControlPanelSize(getInstrument().getSelectorPanelHeight()));

            panelButtons = ObservatoryInstrumentHelper.createButtonPanel(this,
                                                                         getOnButton(),
                                                                         getOffButton());

            footerPanel = new JPanel();
            footerPanel.setLayout(new BoxLayoutFixed(footerPanel, BoxLayoutFixed.X_AXIS));
            footerPanel.setOpaque(false);

            buffer = new StringBuffer();
            buffer.append("<html><font size=2>");
            buffer.append(displayname);
            buffer.append("</font></html>");

            labelText = new JLabel(buffer.toString(), SwingConstants.RIGHT)
                {
                private static final long serialVersionUID = -6272016227154441262L;


                // Enable Antialiasing in Java 1.5
                protected void paintComponent(final Graphics graphics)
                    {
                    final Graphics2D graphics2D = (Graphics2D) graphics;

                    // For antialiasing text
                    graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                                                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    super.paintComponent(graphics2D);
                    }
                };
            labelText.setFont(getFontData().getFont());
            labelText.setForeground(getColourData().getColor());

            footerPanel.add(panelButtons);
            footerPanel.add(labelText);

            // Put it all together on the HostPanel, which sits in front of the image
            // on the label, which is on the SelectorPanel
            getHostPanel().removeAll();
            getHostPanel().add((JComponent)getControlPanel());
            getHostPanel().add(footerPanel);
            }
        else
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_NULL);
            }
        }


    /***********************************************************************************************
     * Get the On Button.
     *
     * @return JButton
     */

    public final JButton getOnButton()
        {
        return (this.buttonOn);
        }


    /***********************************************************************************************
     * Get the Off Button.
     *
     * @return JButton
     */

    public final JButton getOffButton()
        {
        return (this.buttonOff);
        }


    /***********************************************************************************************
     * Get the Instrument InstrumentPanel.
     *
     * @return InstrumentUIComponentDecorator
     */

    public final InstrumentUIComponentDecoratorInterface getInstrumentPanel()
        {
        return (this.instrumentPanel);
        }


    /***********************************************************************************************
     * Set the Instrument InstrumentPanel.
     *
     * @param panel
     */

    public final void setInstrumentPanel(final InstrumentUIComponentDecoratorInterface panel)
        {
        this.instrumentPanel = panel;
        }


    /***********************************************************************************************
     * Get the host AtomPlugin.
     *
     * @return AtomPlugin
     */

    public final AtomPlugin getHostAtom()
        {
        return (this.hostAtom);
        }


    /***********************************************************************************************
     * Get the Instrument Xml.
     *
     * @return Instrument
     */

    public final Instrument getInstrument()
        {
        return (this.instrumentXml);
        }


    /***********************************************************************************************
     * Set the Instrument Xml.
     *
     * @param  xml
     */

    public void setInstrument(final Instrument xml)
        {
        this.instrumentXml = xml;
        }


    /***********************************************************************************************
     * Get the host UI.
     *
     * @return ObservatoryUIInterface
     */

    public final ObservatoryUIInterface getHostUI()
        {
        return (this.hostUI);
        }


    /***********************************************************************************************
     * Get the FontDataType.
     *
     * @return FontPlugin
     */

    public FontInterface getFontData()
        {
        return (this.pluginFont);
        }


    /***********************************************************************************************
     * Get the ColourDataType.
     *
     * @return ColourPlugin
     */

    public ColourInterface getColourData()
        {
        return (this.pluginColour);
        }


    /**********************************************************************************************/
    /* Configuration and Metadata                                                                 */
    /***********************************************************************************************
     * Get the Vector of configuration data to append to a Report.
     *
     * @return Vector<Vector>
     */

    public Vector<Vector> getInstrumentConfiguration()
        {
        return (this.vecInstrumentConfiguration);
        }


    /***************************************************************************************************
     * Set the Vector of configuration data to append to a Report.
     *
     * @param config
     */

    public void setInstrumentConfiguration(final Vector<Vector> config)
        {
        this.vecInstrumentConfiguration = config;
        }


    /***********************************************************************************************
     * Get the Macros associated with this Instrument.
     *
     * @return MacrosDocument
     */

    public MacrosDocument getInstrumentMacros()
        {
        return (this.docInstrumentMacros);
        }


    /***********************************************************************************************
     * Set the Macros associated with this Instrument.
     *
     * @param macros
     */

    public void setInstrumentMacros(final MacrosDocument macros)
        {
        this.docInstrumentMacros = macros;
        }


    /**********************************************************************************************/
    /* Metadata                                                                                   */
    /***********************************************************************************************
     * Get the ObservatoryInstrument Aggregate Metadata.
     *
     * @return Vector<Vector>
     */

    public List<Metadata> getAggregateMetadata()
        {
        return (this.listAggregateMetadata);
        }


    /***********************************************************************************************
     * Set the ObservatoryInstrument Aggregate Metadata.
     *
     * @param metadata
     */

    public void setAggregateMetadata(final List<Metadata> metadata)
        {
        this.listAggregateMetadata = metadata;
        }


    /**********************************************************************************************/
    /* PointOfInterest                                                                            */
    /***********************************************************************************************
     * Get the list of Composite PointsOfInterest.
     *
     * @return List<PointOfInterest>
     */

    public List<PointOfInterest> getCompositePointOfInterestList()
        {
        return (this.listCompositePOIs);
        }


    /***********************************************************************************************
     * Set the Composite Points of Interest for the Instrument.
     *
     * @param pois
     */

    public final void setCompositePointOfInterestList(final List<PointOfInterest> pois)
        {
        this.listCompositePOIs = pois;
        }


    /**********************************************************************************************/
    /* LineOfInterest                                                                             */
    /***********************************************************************************************
     * Get the list of Composite LinesOfInterest.
     *
     * @return List<LineOfInterest>
     */

    public List<LineOfInterest> getCompositeLineOfInterestList()
        {
        return (this.listCompositeLOIs);
        }


    /***********************************************************************************************
     * Set the Composite Lines of Interest for the Instrument.
     *
     * @param lois
     */

    public final void setCompositeLineOfInterestList(final List<LineOfInterest> lois)
        {
        this.listCompositeLOIs = lois;
        }


    /**********************************************************************************************/
    /* Logging                                                                                    */
    /***********************************************************************************************
     * Get the ObservatoryInstrument EventLog.
     *
     * @return Vector<Vector>
     */

    public Vector<Vector> getEventLog()
        {
        return (this.vecEventLog);
        }


    /***********************************************************************************************
     * Get the number of columns in the ObservatoryInstrument EventLog.
     * Instruments must override this if their logs are different from the default.
     *
     * @return int
     */

    public int getEventLogWidth()
        {
        return (DEFAULT_EVENTLOG_WIDTH);
        }


    /***********************************************************************************************
     * Get the ObservatoryInstrument EventLog Metadata, i.e describing the columns of the EventLog.
     * Instruments must override this if their EventLog implementations are different from the default.
     * SimpleEventLogUIComponent has five columns: Icon, Date, Time, Event, Source.
     *
     * @return List<Metadata>
     */

    public List<Metadata> getEventLogMetadata()
        {
        return (LogHelper.createDefaultEventLogMetadata());
        }


    /***********************************************************************************************
     * Add an ObservatoryInstrument EventLog fragment, i.e. a collection of entries.
     * The EventLog will be sorted on any Report.
     * This is really a Vector<Vector<Object>> !
     *
     * Entry0   object0  object1  object2
     * Entry1   objectA  objectB  objectC
     * Entry2   objectP  objectQ  objectR
     *
     * @param logfragment
     */

    public synchronized void addEventLogFragment(final Vector<Vector> logfragment)
        {
        if ((getEventLog() != null)
            && (logfragment != null)
            && (!logfragment.isEmpty())
            && (logfragment.get(0).size() == getEventLogWidth()))
            {
            final Iterator iterFragmentEntries;

            // We can't use addAll() because we need to add a sorting index
            iterFragmentEntries = logfragment.iterator();

            while (iterFragmentEntries.hasNext())
                {
                final Vector vecFragmentEntry;

                // clone() to avoid affecting the original data!
                vecFragmentEntry = (Vector<Object>)((Vector) iterFragmentEntries.next()).clone();

                // Add the LogIndex at the end (used for sorting, not display)
                vecFragmentEntry.add(intEventLogIndex);

                // Count a new entry to the Log, so they are shown in descending order
                intEventLogIndex--;

                // Add a log fragment, i.e. a collection of entries, to the existing Log
                getEventLog().add(vecFragmentEntry);
                }

            // Limit the Log size! (don't do this inside the Iterator)
            while (getEventLog().size() >= LOGSIZE_MAX)
                {
                // Just remove the oldest entry in the Log
                getEventLog().removeElementAt(0);
                }
            }
        else
            {
//            LOGGER.debugProtocolEvent("AbstractObservatoryInstrument.addEventLogFragment() Unable to add log entries");
//
//            if (getEventLog() == null)
//                {
//                System.out.println("AbstractObservatoryInstrument.addEventLogFragment() LOG IS NULL");
//                }
//
//            if (logfragment == null)
//                {
//                System.out.println("AbstractObservatoryInstrument.addEventLogFragment() LOG FRAGMENT ENTRIES NULL");
//                }
//            else
//                {
//                if (logfragment.isEmpty())
//                    {
//                    System.out.println("AbstractObservatoryInstrument.addEventLogFragment() LOG FRAGMENT ENTRIES EMPTY");
//                    }
//                else
//                    {
//                    if (logfragment.get(0) instanceof Vector)
//                        {
//                        if (logfragment.get(0).size() != getEventLogWidth())
//                            {
//                            System.out.println("AbstractObservatoryInstrument.addEventLogFragment() SIZE OF VECTOR INCORRECT is " + logfragment.get(0).size() + " should be " + getEventLogWidth());
//                            }
//                        }
//                    else
//                        {
//                        System.out.println("AbstractObservatoryInstrument.addEventLogFragment() ENTRY NOT A VECTOR");
//                        }
//                    }
//                }
            }
        }


    /***********************************************************************************************
     * Get the ObservatoryInstrument InstrumentLog.
     *
     * @return Vector<Vector>
     */

    public Vector<Vector> getInstrumentLog()
        {
        return (this.vecInstrumentLog);
        }


    /***********************************************************************************************
     * Get the number of columns in the ObservatoryInstrument InstrumentLog.
     * Instruments must override this if their logs are different from the default.
     *
     * @return int
     */

    public int getInstrumentLogWidth()
        {
        return (DEFAULT_INSTRUMENTLOG_WIDTH);
        }


    /***********************************************************************************************
     * Get the ObservatoryInstrument InstrumentLog Metadata,
     * i.e describing the columns of the InstrumentLog.
     * Instruments must override this if their log implementations are different from the default.
     * SimpleEventLogUIComponent has five columns: Icon, Date, Time, Event, Source.
     *
     * @return List<Metadata>
     */

    public List<Metadata> getInstrumentLogMetadata()
        {
        return (LogHelper.createDefaultEventLogMetadata());
        }


    /***********************************************************************************************
     * Add an ObservatoryInstrument InstrumentLog fragment, i.e. a collection of entries.
     * The InstrumentLog will be sorted on any Report.
     * This is really a Vector<Vector<Object>> !
     *
     * Entry0   object0  object1  object2
     * Entry1   objectA  objectB  objectC
     * Entry2   objectP  objectQ  objectR
     *
     * @param logfragment
     */

    public synchronized void addInstrumentLogFragment(final Vector<Vector> logfragment)
        {
        if ((getInstrumentLog() != null)
            && (logfragment != null)
            && (!logfragment.isEmpty())
            && (logfragment.get(0).size() == getInstrumentLogWidth()))
            {
            final Iterator iterFragmentEntries;

            // We can't use addAll() because we need to add a sorting index
            iterFragmentEntries = logfragment.iterator();

            while (iterFragmentEntries.hasNext())
                {
                final Vector vecFragmentEntry;

                // clone() to avoid affecting the original data!
                vecFragmentEntry = (Vector<Object>)((Vector) iterFragmentEntries.next()).clone();

                // Add the LogIndex at the end (used for sorting, not display)
                vecFragmentEntry.add(intInstrumentLogIndex);

                // Count a new entry to the Log, so they are shown in descending order
                intInstrumentLogIndex--;

                // Add a log fragment, i.e. a collection of entries, to the existing Log
                getInstrumentLog().add(vecFragmentEntry);
                }

            // Limit the Log size! (don't do this inside the Iterator)
            while (getInstrumentLog().size() >= LOGSIZE_MAX)
                {
                // Just remove the oldest entry in the Log
                getInstrumentLog().removeElementAt(0);
                }
            }
        else
            {
//            LOGGER.debugProtocolEvent("AbstractObservatoryInstrument.addInstrumentLogFragment() Unable to add log entries");
//
//            if (getInstrumentLog() == null)
//                {
//                System.out.println("AbstractObservatoryInstrument.addInstrumentLogFragment() LOG IS NULL");
//                }
//
//            if (logfragment == null)
//                {
//                System.out.println("AbstractObservatoryInstrument.addInstrumentLogFragment() ENTRIES NULL");
//                }
//            else
//                {
//                if (logfragment.isEmpty())
//                    {
//                    System.out.println("AbstractObservatoryInstrument.addInstrumentLogFragment() ENTRIES EMPTY");
//                    }
//                else
//                    {
//                    if (logfragment.get(0) instanceof Vector)
//                        {
//                        if (logfragment.get(0).size() != getInstrumentLogWidth())
//                            {
//                            System.out.println("AbstractObservatoryInstrument.addInstrumentLogFragment() SIZE OF VECTOR INCORRECT is " + logfragment.get(0).size() + " should be " + getInstrumentLogWidth());
//                            }
//                        }
//                    else
//                        {
//                        System.out.println("AbstractObservatoryInstrument.addInstrumentLogFragment() ENTRY NOT A VECTOR");
//                        }
//                    }
//                }
            }
        }


    /**********************************************************************************************/
    /* Utilities                                                                                  */
    /***********************************************************************************************
     * Get the Instrument ResourceKey.
     *
     * @return String
     */

    public final String getResourceKey()
        {
        return (this.strResourceKey);
        }


    /***********************************************************************************************
     * Get the CommandProcessorContext.
     *
     * @return CommandProcessorContext
     */

    public CommandProcessorContextInterface getContext()
        {
        return (this.contextInterface);
        }


    /***********************************************************************************************
     * Set the CommandProcessorContext.
     *
     * @param cpc
     */

    public void setContext(final CommandProcessorContextInterface cpc)
        {
        this.contextInterface = cpc;
        }


    /***********************************************************************************************
     * Indicate if the Instrument is in debug mode.
     *
     * @return boolean
     */

    public boolean isDebugMode()
        {
        // Override if an Instrument requires debugging!
        return (false);
        }


    /***********************************************************************************************
     *  Read all the Resources required by the ObservatoryInstrument.
     */

    public void readResources()
        {
        // Use the parent Framework's Colour and Font
        pluginFont = (FontInterface)REGISTRY.getProperty(REGISTRY.getFrameworkResourceKey() + KEY_FONT_LABEL);
        pluginColour = (ColourInterface)REGISTRY.getProperty(REGISTRY.getFrameworkResourceKey() + KEY_COLOUR_TEXT);
        }


    /**********************************************************************************************/
    /* Events                                                                                     */
    /***********************************************************************************************
     * Notify all listeners of InstrumentStateChangedEvents.
     *
     * @param eventsource
     * @param instrument
     * @param currentstate
     * @param nextstate
     * @param repeatnumber
     * @param repeattext
     */

    public final void notifyInstrumentStateChangedEvent(final Object eventsource,
                                                        final ObservatoryInstrumentInterface instrument,
                                                        final InstrumentState currentstate,
                                                        final InstrumentState nextstate,
                                                        final long repeatnumber,
                                                        final String repeattext)
        {
        List<InstrumentStateChangedListener> listeners;
        final InstrumentStateChangedEvent changeEvent;

        // Create a Thread-safe List of Listeners
        listeners = new CopyOnWriteArrayList<InstrumentStateChangedListener>(getInstrumentStateChangedListeners());

        // Create an InstrumentStateChangedEvent
        changeEvent = new InstrumentStateChangedEvent(eventsource,
                                                      instrument,
                                                      currentstate,
                                                      nextstate,
                                                      repeatnumber,
                                                      repeattext);
        // Fire the event to every listener
        synchronized(listeners)
            {
            for (int i = 0; i < listeners.size(); i++)
                {
                final InstrumentStateChangedListener changeListener;

                changeListener = listeners.get(i);
                changeListener.instrumentChanged(changeEvent);
                }
            }

        // Help the GC?
        listeners = null;
        }


    /***********************************************************************************************
     * Get the InstrumentStateChanged Listeners (mostly for testing).
     *
     * @return Vector<InstrumentStateChangedListener>
     */

    public final Vector<InstrumentStateChangedListener> getInstrumentStateChangedListeners()
        {
        return (this.vecInstrumentStateChangedListeners);
        }


    /***********************************************************************************************
     * Add a listener for this event.
     *
     * @param listener
     */

    public final void addInstrumentStateChangedListener(final InstrumentStateChangedListener listener)
        {
        if ((listener != null)
            && (getInstrumentStateChangedListeners() != null))
            {
            getInstrumentStateChangedListeners().addElement(listener);
            }
        }


    /***********************************************************************************************
     * Remove a listener for this event.
     *
     * @param listener
     */

    public final void removeInstrumentStateChangedListener(final InstrumentStateChangedListener listener)
        {
        if ((listener != null)
            && (getInstrumentStateChangedListeners() != null))
            {
            getInstrumentStateChangedListeners().removeElement(listener);
            }
        }
    }
