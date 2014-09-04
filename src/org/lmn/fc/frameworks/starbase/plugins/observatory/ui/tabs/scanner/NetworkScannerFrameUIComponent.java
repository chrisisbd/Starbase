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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.scanner;

import org.lmn.fc.common.actions.ContextAction;
import org.lmn.fc.common.net.NetworkScanner;
import org.lmn.fc.common.utilities.misc.Semaphore;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ObservatoryInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.common.IPVersion;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.DAOWrapperInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.NetworkScannerUIComponentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.widgets.IpTextField;
import org.lmn.fc.model.registry.NavigationUtilities;
import org.lmn.fc.model.registry.RegistryModelUtilities;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.ui.UIComponentPlugin;
import org.lmn.fc.ui.components.UIComponent;
import org.lmn.fc.ui.components.UIComponentHelper;
import org.lmn.fc.ui.reports.ReportTableHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;


/***************************************************************************************************
 * NetworkScannerFrameUIComponent.
 */

public final class NetworkScannerFrameUIComponent extends UIComponent
    {
    // String Resources
    private static final String TITLE_FRAME = "Starinet Scanner";
    private static final String LABEL_ADDRESS_START = "Start Address";
    private static final String LABEL_ADDRESS_END = "End Address";
    private static final String LABEL_PORT = "UDP Port";

    private static final String TOOLTIP_ADDRESS_START = "Enter the starting IP Address for the scan (local network only!)";
    private static final String TOOLTIP_ADDRESS_END = "Enter the end IP Address for the scan (local network only!)";
    private static final String TOOLTIP_PORT = "Enter the UDP Port number which listening for Starinet communications";

    private static final String ACTION_RESCAN = "Rescan the Starinet Network (256 addresses maximum)";
    private static final String ACTION_STOP = "Stop the scan immediately";
    private static final String ACTION_DISPOSE = "Remove the scan results";

    private static final String DEFAULT_ADDRESS_START = "192.168.1.1";
    private static final String DEFAULT_ADDRESS_END = "192.168.1.254";

    private static final int LENGTH_ADDRESS_PORT = 5;
    // This number is unassigned, see: http://en.wikipedia.org/wiki/List_of_TCP_and_UDP_port_numbers
    private static final int SOURCE_PORT = 32001;
    private static final int SCAN_TIMEOUT = 2000;

    // Injections
    private final TaskPlugin pluginTask;
    private final ObservatoryInstrumentInterface hostInstrument;
    private final String strResourceKey;

    // UI
    private NetworkScannerUIComponentInterface scannerUIComponent;
    private JToolBar toolBar;
    private final IpTextField textStartAddress;
    private final IpTextField textEndAddress;
    private final JTextField textPort;
    private final JButton buttonToolbarRescan;
    private final JButton buttonToolbarStop;
    private final JButton buttonToolbarDispose;
    private int intToolbarComponentCount;
    private final Semaphore semScanning;


    /***********************************************************************************************
     * Construct a NetworkScannerFrameUIComponent.
     *
     * @param task
     * @param hostinstrument
     * @param resourcekey
     */

    public NetworkScannerFrameUIComponent(final TaskPlugin task,
                                          final ObservatoryInstrumentInterface hostinstrument,
                                          final String resourcekey)
        {
        // UIComponent has a BorderLayout
        super();

        // Injections
        this.pluginTask = task;
        this.hostInstrument = hostinstrument;
        this.strResourceKey = resourcekey;

        this.scannerUIComponent = null;
        this.toolBar = null;
        this.intToolbarComponentCount = 0;

        this.textStartAddress = new IpTextField(IPVersion.IPV4, null);
        this.textEndAddress = new IpTextField(IPVersion.IPV4, null);
        this.textPort = new JTextField(LENGTH_ADDRESS_PORT);

        this.buttonToolbarRescan = new JButton();
        getRescanButton().setBorder(BORDER_BUTTON);
        getRescanButton().setText(EMPTY_STRING);

        getRescanButton().setAction(null);
        getRescanButton().setToolTipText(EMPTY_STRING);

        // Ensure that no text appears next to the Icon...
        getRescanButton().setHideActionText(true);

        // The button will be enabled when a valid Address Range is entered
        getRescanButton().setEnabled(false);

        this.buttonToolbarStop = new JButton();
        getStopButton().setBorder(BORDER_BUTTON);
        getStopButton().setText(EMPTY_STRING);

        getStopButton().setAction(null);
        getStopButton().setToolTipText(EMPTY_STRING);

        // Ensure that no text appears next to the Icon...
        getStopButton().setHideActionText(true);
        getStopButton().setEnabled(false);

        this.buttonToolbarDispose = new JButton();
        getDisposeButton().setBorder(BORDER_BUTTON);
        getDisposeButton().setText(EMPTY_STRING);

        getDisposeButton().setAction(null);
        getDisposeButton().setToolTipText(EMPTY_STRING);

        // Ensure that no text appears next to the Icon...
        getDisposeButton().setHideActionText(true);
        getDisposeButton().setEnabled(true);

        // Start in the not-scanning state
        this.semScanning = new Semaphore(false);
        }


    /***********************************************************************************************
     * Initialise this UIComponent.
     */

    public void initialiseUI()
        {
        final String SOURCE = "NetworkScannerFrameUIComponent.initialiseUI() ";

        super.initialiseUI();

        // Colours
        setBackground(UIComponentPlugin.DEFAULT_COLOUR_CANVAS.getColor());

        // Create the NetworkScanner JPanel and initialise it
        // Do this first to get the colours and fonts
        setNetworkScannerPanel(new NetworkScannerUIComponent(getTask(),
                                                             getHostInstrument(),
                                                             getResourceKey()));
        getNetworkScannerPanel().initialiseUI();

        // Create the JToolBar and initialise it
        setToolBar(new JToolBar());
        getToolBar().setFloatable(false);
        getToolBar().setMinimumSize(DIM_TOOLBAR_SIZE);
        getToolBar().setPreferredSize(DIM_TOOLBAR_SIZE);
        getToolBar().setMaximumSize(DIM_TOOLBAR_SIZE);
        getToolBar().setBackground(UIComponentPlugin.DEFAULT_COLOUR_TAB_BACKGROUND.getColor());

        initialiseToolbar(getToolBar());

        // Put the components together
        add(getToolBar(), BorderLayout.NORTH);
        add((Component) getNetworkScannerPanel(), BorderLayout.CENTER);

        getScanningSemaphore().setState(false);
        }


    /***********************************************************************************************
     * Run this UIComponent.
     */

    public void runUI()
        {
        super.runUI();

        if (getNetworkScannerPanel() != null)
            {
            // runUI() will create ContextActions in the ReportTable and add to the NetworkScanner Panel
            // Then transfer from the NetworkScanner Panel to this NetworkScannerFrameUIComponent
            UIComponentHelper.runComponentAndTransferActions((Component) getNetworkScannerPanel(), this);
            }
        }


    /***********************************************************************************************
     * Stop this UIComponent.
     */

    public void stopUI()
        {
        super.stopUI();

        if (getNetworkScannerPanel() != null)
            {
            getNetworkScannerPanel().stopUI();
            }
        }


    /***********************************************************************************************
     * Dispose of all components of this UIComponent.
     */

    public void disposeUI()
        {
        // Scanning could continue if we are not looking, but not if we are never coming back
        getScanningSemaphore().setState(false);

        stopUI();

        if (getToolBar() != null)
            {
            getToolBar().removeAll();
            setToolBar(null);
            }

        if (getNetworkScannerPanel() != null)
            {
            getNetworkScannerPanel().disposeUI();
            setNetworkScannerPanel(null);
            }

        super.disposeUI();
        }


    /***********************************************************************************************
     * Set the data from the DAO finished() method, or from any Command doing a realtime update.
     *
     * @param daowrapper
     * @param updatemetadata
     */

    public void setWrappedData(final DAOWrapperInterface daowrapper,
                               final boolean updatemetadata)
        {
        final String SOURCE = "NetworkScannerFrameUIComponent.setWrappedData() ";

        // We have no need of the Scan Data on the Toolbar etc.
        }


    /***********************************************************************************************
     * Initialise the Toolbar.
     *
     * @param toolbar
     */

    private void initialiseToolbar(final JToolBar toolbar)
        {
        final String SOURCE = "NetworkScannerFrameUIComponent.initialiseToolbar() ";

        if (toolbar != null)
            {
            final JLabel labelName;
            final JLabel labelAddressStart;
            final JLabel labelAddressEnd;
            final JLabel labelPort;
            final ContextAction actionRescan;
            final ContextAction actionStop;
            final ContextAction actionDispose;
            int intCount;

            labelName = new JLabel(TITLE_FRAME,
                                   RegistryModelUtilities.getAtomIcon(getHostInstrument().getHostAtom(),
                                                                      ObservatoryInterface.FILENAME_ICON_NETWORK_SCANNER),
                                   SwingConstants.LEFT)
                {
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

            labelAddressStart = new JLabel(LABEL_ADDRESS_START);
            labelAddressStart.setToolTipText(TOOLTIP_ADDRESS_START);

            labelAddressEnd = new JLabel(LABEL_ADDRESS_END);
            labelAddressEnd.setToolTipText(TOOLTIP_ADDRESS_END);

            labelPort = new JLabel(LABEL_PORT);
            labelPort.setToolTipText(TOOLTIP_PORT);

            // We should have an initialised panel by now
            if (getNetworkScannerPanel() != null)
                {
                // Copy the style of the NetworkScanner Panel
                labelName.setForeground(getNetworkScannerPanel().getTextColour().getColor());
                labelName.setFont(getNetworkScannerPanel().getReportFont().getFont().deriveFont(ReportTableHelper.SIZE_HEADER_FONT).deriveFont(Font.BOLD));
                labelName.setIconTextGap(TOOLBAR_ICON_TEXT_GAP);

                labelAddressStart.setForeground(getNetworkScannerPanel().getTextColour().getColor());
                labelAddressStart.setFont(getNetworkScannerPanel().getReportFont().getFont());

                labelAddressEnd.setForeground(getNetworkScannerPanel().getTextColour().getColor());
                labelAddressEnd.setFont(getNetworkScannerPanel().getReportFont().getFont());

                labelPort.setForeground(getNetworkScannerPanel().getTextColour().getColor());
                labelPort.setFont(getNetworkScannerPanel().getReportFont().getFont());
                }

            intCount = 0;
            toolbar.removeAll();
            toolbar.setFloatable(false);

            toolbar.addSeparator(UIComponentPlugin.DIM_TOOLBAR_SEPARATOR_BUTTON);
            toolbar.add(labelName);
            toolbar.add(Box.createHorizontalGlue());
            intCount++;

            toolbar.addSeparator(DIM_LABEL_SEPARATOR);
            intCount++;

            toolbar.add(labelAddressStart);
            intCount++;

            toolbar.addSeparator(DIM_LABEL_SEPARATOR);
            intCount++;

            toolbar.add(NetworkScannerHelper.initialiseAddress(getNetworkScannerPanel(),
                                                               getStartAddressText(),
                                                               getStartAddressText(),
                                                               getEndAddressText(),
                                                               getPortText(),
                                                               getRescanButton(),
                                                               TOOLTIP_ADDRESS_START,
                                                               DEFAULT_ADDRESS_START));
            intCount++;

            toolbar.addSeparator(DIM_TOOLBAR_SEPARATOR);
            intCount++;

            toolbar.add(labelAddressEnd);
            intCount++;

            toolbar.addSeparator(DIM_LABEL_SEPARATOR);
            intCount++;

            toolbar.add(NetworkScannerHelper.initialiseAddress(getNetworkScannerPanel(),
                                                               getEndAddressText(),
                                                               getStartAddressText(),
                                                               getEndAddressText(),
                                                               getPortText(),
                                                               getRescanButton(),
                                                               TOOLTIP_ADDRESS_END,
                                                               DEFAULT_ADDRESS_END));
            intCount++;

            toolbar.addSeparator(DIM_TOOLBAR_SEPARATOR);
            intCount++;

            toolbar.add(labelPort);
            intCount++;

            toolbar.addSeparator(DIM_LABEL_SEPARATOR);
            intCount++;

            toolbar.add(NetworkScannerHelper.initialisePort(getNetworkScannerPanel(),
                                                            getPortText(),
                                                            getStartAddressText(),
                                                            getEndAddressText(),
                                                            getRescanButton(),
                                                            TOOLTIP_PORT,
                                                            DEFAULT_STARINET_UDP_PORT));
            intCount++;

            toolbar.addSeparator(DIM_TOOLBAR_SEPARATOR_BUTTON);
            intCount++;

            //-------------------------------------------------------------------------------------
            // Create the toolbar button to rescan the Network

            actionRescan = new ContextAction(ACTION_RESCAN,
                                             RegistryModelUtilities.getCommonIcon(UIComponentPlugin.FILENAME_ICON_REFRESH),
                                             ACTION_RESCAN,
                                             KeyEvent.VK_R,
                                             false,
                                             true)
                {
                public void actionPerformed(final ActionEvent event)
                    {
                    getScanningSemaphore().setState(true);
                    getRescanButton().setEnabled(false);
                    getStopButton().setEnabled(true);
                    getDisposeButton().setEnabled(false);

                    // Remove the previous scan immediately
                    if (getNetworkScannerPanel() != null)
                        {
                        getNetworkScannerPanel().disposeReport();
                        getNetworkScannerPanel().refreshTable();
                        }

                    // This generates the Report using the parameters from the Toolbar,
                    // and runs on a separate thread
                    // Only change the toolbar UI state when the thread has stopped!
                    rescanNetwork(getStartAddressText(),
                                  getEndAddressText(),
                                  getPortText(),
                                  getScanningSemaphore(),
                                  getRescanButton(),
                                  getStopButton(),
                                  getDisposeButton());
                    }
                };

            getRescanButton().setAction(actionRescan);
            getRescanButton().setToolTipText((String) actionRescan.getValue(Action.SHORT_DESCRIPTION));
            getRescanButton().setEnabled(false);

            toolbar.add(getRescanButton());
            toolbar.addSeparator(UIComponentPlugin.DIM_TOOLBAR_SEPARATOR_BUTTON);
            intCount++;

            //-------------------------------------------------------------------------------------
            // Create the toolbar button to stop the scan

            actionStop = new ContextAction(ACTION_STOP,
                                           RegistryModelUtilities.getCommonIcon(UIComponentPlugin.FILENAME_ICON_STOP),
                                           ACTION_STOP,
                                           KeyEvent.VK_S,
                                           false,
                                           true)
                {
                public void actionPerformed(final ActionEvent event)
                    {
                    getScanningSemaphore().setState(false);
                    getRescanButton().setEnabled(NetworkScannerHelper.isValidRangeAndPortFromUI(getStartAddressText(),
                                                                                                getEndAddressText(),
                                                                                                getPortText()));
                    getStopButton().setEnabled(false);
                    getDisposeButton().setEnabled(true);
                    }
                };

            getStopButton().setAction(actionStop);
            getStopButton().setToolTipText((String) actionStop.getValue(Action.SHORT_DESCRIPTION));
            getStopButton().setEnabled(false);

            toolbar.add(getStopButton());
            toolbar.addSeparator(UIComponentPlugin.DIM_TOOLBAR_SEPARATOR_BUTTON);
            intCount++;

            //-------------------------------------------------------------------------------------
            // Create the toolbar button to remove the scan results

            actionDispose = new ContextAction(ACTION_DISPOSE,
                                              RegistryModelUtilities.getCommonIcon(UIComponentPlugin.FILENAME_ICON_DISPOSE),
                                              ACTION_DISPOSE,
                                              KeyEvent.VK_D,
                                              false,
                                              true)
                {
                public void actionPerformed(final ActionEvent event)
                    {
                    getScanningSemaphore().setState(false);
                    getRescanButton().setEnabled(NetworkScannerHelper.isValidRangeAndPortFromUI(getStartAddressText(),
                                                                                                getEndAddressText(),
                                                                                                getPortText()));
                    getStopButton().setEnabled(false);
                    getDisposeButton().setEnabled(false);

                    if (getNetworkScannerPanel() != null)
                        {
                        getNetworkScannerPanel().disposeReport();
                        getNetworkScannerPanel().refreshTable();
                        }
                    }
                };

            getDisposeButton().setAction(actionDispose);
            getDisposeButton().setToolTipText((String) actionDispose.getValue(Action.SHORT_DESCRIPTION));
            getDisposeButton().setEnabled(false);

            toolbar.add(getDisposeButton());
            toolbar.addSeparator(UIComponentPlugin.DIM_TOOLBAR_SEPARATOR_BUTTON);
            intCount++;

            setToolbarComponentCount(intCount);
            NavigationUtilities.updateComponentTreeUI(toolbar);
            }
        }


    /***********************************************************************************************
     * Rescan the Network using the data from the UI, and reapply the data to the Instrument.
     * This UIComponent must behave like a DAO and return a DAOWrapper,
     * containing the Scanner data in the UserObject.
     *
     * @param textstart
     * @param textend
     * @param textport
     * @param semaphore
     * @param rescanbutton
     * @param stopbutton
     * @param disposebutton
     */

    private void rescanNetwork(final IpTextField textstart,
                               final IpTextField textend,
                               final JTextField textport,
                               final Semaphore semaphore,
                               final JButton rescanbutton,
                               final JButton stopbutton,
                               final JButton disposebutton)
        {
        final String SOURCE = "NetworkScannerFrameUIComponent.rescanNetwork() ";
        final List<String> errors;

        errors = new ArrayList<String>(5);

        try
            {
//            System.out.println("RESCAN! [start=" + textstart.getIpAddressAsString().trim()
//                               + "] [end=" + textend.getIpAddressAsString().trim()
//                               + "] [semaphore=" + semaphore.getState() + "]");

            NetworkScanner.scanNetwork(getHostInstrument(),
                                       textstart.getIPAddressAsLong(),
                                       textend.getIPAddressAsLong(),
                                       SOURCE_PORT,
                                       Integer.parseInt(textport.getText()),
                                       SCAN_TIMEOUT,
                                       semaphore,
                                       rescanbutton,
                                       stopbutton,
                                       disposebutton);
            }

        catch (UnknownHostException exception)
            {
            errors.add(SOURCE + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR);
            }

        catch (SecurityException exception)
            {
            errors.add(SOURCE + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR);
            }

        catch (NumberFormatException exception)
            {
            errors.add(SOURCE + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR);
            }

        LOGGER.errors(SOURCE, errors);
        }


    /**********************************************************************************************/
    /* Utilities                                                                                  */
    /***********************************************************************************************
     * Get the NetworkScanner JToolBar.
     *
     * @return JToolBar
     */

    private JToolBar getToolBar()
        {
        return (this.toolBar);
        }


    /***********************************************************************************************
     * Set the NetworkScanner JToolBar.
     *
     * @param toolbar
     */

    private void setToolBar(final JToolBar toolbar)
        {
        this.toolBar = toolbar;
        }


    /***********************************************************************************************
     * Get the StartAddress entry box.
     *
     * @return IpTextField
     */

    private IpTextField getStartAddressText()
        {
        return (this.textStartAddress);
        }


    /***********************************************************************************************
     * Get the EndAddress entry box.
     *
     * @return IpTextField
     */

    private IpTextField getEndAddressText()
        {
        return (this.textEndAddress);
        }


    /***********************************************************************************************
     * Get the Port entry box.
     *
     * @return JTextField
     */

    private JTextField getPortText()
        {
        return (this.textPort);
        }


    /***********************************************************************************************
     * Get the JButton used to rescan the network.
     *
     * @return JButton
     */

    private JButton getRescanButton()
        {
        return (this.buttonToolbarRescan);
        }


    /***********************************************************************************************
     * Get the JButton used to stop the scan.
     *
     * @return JButton
     */

    private JButton getStopButton()
        {
        return (this.buttonToolbarStop);
        }


    /***********************************************************************************************
     * Get the JButton used to dispose of the scan result.
     *
     * @return JButton
     */

    private JButton getDisposeButton()
        {
        return (this.buttonToolbarDispose);
        }


    /***********************************************************************************************
     * Get the NetworkScanner Panel.
     * This is public to allow exportNetworkScan().
     *
     * @return NetworkScannerUIComponentInterface
     */

    public NetworkScannerUIComponentInterface getNetworkScannerPanel()
        {
        return (this.scannerUIComponent);
        }


    /***********************************************************************************************
     * Set the NetworkScanner Panel.
     *
     * @param panel
     */

    private void setNetworkScannerPanel(final NetworkScannerUIComponentInterface panel)
        {
        this.scannerUIComponent = panel;
        }


    /***********************************************************************************************
     * Get the ToolbarComponentCount.
     *
     * @return int
     */

    private int getToolbarComponentCount()
        {
        return intToolbarComponentCount;
        }


    /***********************************************************************************************
     * Set the ToolbarComponentCount.
     *
     * @param count
     */

    private void setToolbarComponentCount(final int count)
        {
        this.intToolbarComponentCount = count;
        }


    /***********************************************************************************************
     * Get the Semaphore which indicates the state of the scan.
     *
     * @return Semaphore
     */

    public Semaphore getScanningSemaphore()
        {
        return (this.semScanning);
        }


    /**********************************************************************************************/
    /* Injections                                                                                 */
    /***********************************************************************************************
     * Get the Task on which this Report is based.
     *
     * @return TaskData
     */

    private TaskPlugin getTask()
        {
        return (this.pluginTask);
        }


    /***********************************************************************************************
     * Get the ObservatoryInstrument to which this UIComponent is attached.
     *
     * @return ObservatoryInstrumentInterface
     */

    private ObservatoryInstrumentInterface getHostInstrument()
        {
        return (this.hostInstrument);
        }


    /***********************************************************************************************
     * Get the ResourceKey for the Report.
     *
     * @return String
     */

    private String getResourceKey()
        {
        return (this.strResourceKey);
        }
    }
