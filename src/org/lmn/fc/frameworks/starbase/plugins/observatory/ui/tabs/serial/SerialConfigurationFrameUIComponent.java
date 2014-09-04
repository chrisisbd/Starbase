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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.serial;


import com.mxgraph.io.mxCodec;
import com.mxgraph.model.mxICell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxCellRenderer;
import com.mxgraph.util.mxXmlUtils;
import com.mxgraph.util.png.mxPngEncodeParam;
import com.mxgraph.util.png.mxPngImageEncoder;
import org.lmn.fc.common.actions.ContextAction;
import org.lmn.fc.common.utilities.threads.SwingWorker;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ObservatoryInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryUIInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.InstrumentUIComponentDecorator;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.SerialConfigurationCellDataInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.SerialConfigurationFrameUIComponentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.SerialConfigurationUIComponentInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.DaoPortInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.PortControllerInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.impl.PortController;
import org.lmn.fc.frameworks.starbase.portcontroller.impl.streams.StreamType;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.model.registry.NavigationUtilities;
import org.lmn.fc.model.registry.RegistryModelUtilities;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;
import org.lmn.fc.ui.UIComponentPlugin;
import org.lmn.fc.ui.components.UIComponentHelper;
import org.lmn.fc.ui.reports.ReportTableHelper;
import org.lmn.fc.ui.reports.ReportTablePlugin;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Enumeration;


/***************************************************************************************************
 * SerialConfigurationFrameUIComponent.
 */

public final class SerialConfigurationFrameUIComponent extends InstrumentUIComponentDecorator
                                                       implements SerialConfigurationFrameUIComponentInterface
    {
    // This cannot be in FrameworkSingletons because it won't be loaded at the right time...
    private static final PortControllerInterface PORT_CONTROLLER = PortController.getInstance();

    private static final long serialVersionUID = -7695306074475235601L;

    // UI
    private SerialConfigurationUIComponentInterface uiSerialConfig;
    private JToolBar toolbarConfigCommands;
    private JButton buttonRevert;
    private JButton buttonCommit;
    private JButton buttonPageSetup;
    private JButton buttonPrint;
    private JButton buttonExport;


    /***********************************************************************************************
     * Initialise the Commands Toolbar.
     *
     * @param toolbar
     * @param obsinstrument
     * @param scfui
     * @param fontdata
     * @param colourforeground
     * @param colourbackground
     * @param debug
     */

    private static void initialiseCommandsToolbar(final JToolBar toolbar,
                                                  final ObservatoryInstrumentInterface obsinstrument,
                                                  final SerialConfigurationFrameUIComponentInterface scfui,
                                                  final FontInterface fontdata,
                                                  final ColourInterface colourforeground,
                                                  final ColourInterface colourbackground,
                                                  final boolean debug)
        {
        final String SOURCE = "SerialConfigurationFrameUIComponent.initialiseCommandsToolbar() ";
        final JLabel labelName;
        final ContextAction actionRevert;
        final ContextAction actionCommit;
        final ContextAction actionPageSetup;
        final ContextAction actionPrint;
        final ContextAction actionExport;
        final ImageIcon iconPageSetup;
        final ImageIcon iconPrint;
        final ImageIcon iconPrintMessage;

        toolbar.setBackground(colourbackground.getColor());

        //-------------------------------------------------------------------------------------
        // Initialise the Labels

        labelName = new JLabel("Serial Port Configuration",
                               RegistryModelUtilities.getAtomIcon(obsinstrument.getHostAtom(),
                                                                  ObservatoryInterface.FILENAME_ICON_SERIAL_CONFIG),
                               SwingConstants.LEFT)
            {
            private static final long serialVersionUID = 7580736117336162922L;

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

        labelName.setFont(fontdata.getFont().deriveFont(ReportTableHelper.SIZE_HEADER_FONT).deriveFont(Font.BOLD));
        labelName.setForeground(colourforeground.getColor());
        labelName.setIconTextGap(TOOLBAR_ICON_TEXT_GAP);

        //-------------------------------------------------------------------------------------
        // Initialise the Buttons

        scfui.setRevertButton(new JButton());
        scfui.getRevertButton().setBorder(BORDER_BUTTON);

        // Ensure that no text appears next to the Icon...
        scfui.getRevertButton().setHideActionText(true);

        scfui.setCommitButton(new JButton());
        scfui.getCommitButton().setBorder(BORDER_BUTTON);

        // Ensure that no text appears next to the Icon...
        scfui.getCommitButton().setHideActionText(true);

        scfui.setPageSetupButton(new JButton());
        scfui.getPageSetupButton().setBorder(BORDER_BUTTON);

        // Ensure that no text appears next to the Icon...
        scfui.getPageSetupButton().setHideActionText(true);

        scfui.setPrintButton(new JButton());
        scfui.getPrintButton().setBorder(BORDER_BUTTON);

        // Ensure that no text appears next to the Icon...
        scfui.getPrintButton().setHideActionText(true);

        scfui.setExportButton(new JButton());
        scfui.getExportButton().setBorder(BORDER_BUTTON);

        // Ensure that no text appears next to the Icon...
        scfui.getExportButton().setHideActionText(true);

        //-------------------------------------------------------------------------------------
        // Revert

        actionRevert = new ContextAction("Revert",
                                         RegistryModelUtilities.getCommonIcon(FILENAME_ICON_CONFIG_REVERT),
                                         TOOLTIP_REVERT,
                                         KeyEvent.VK_R,
                                         false,
                                         true)
            {
            private final static String SOURCE = "ContextAction:Revert ";
            private static final long serialVersionUID = -2756856894123207182L;

            public void actionPerformed(final ActionEvent event)
                {
                if (scfui.getSerialConfigUI().getConfigIndicator() != null)
                    {
                    scfui.getSerialConfigUI().getConfigIndicator().setSelectedCellData(null, debug);
                    }

                SerialConfigurationHelper.rebuildGraph(scfui.getSerialConfigUI(),
                                                       debug);
                }
            };

        scfui.getRevertButton().setAction(actionRevert);
        scfui.getRevertButton().setToolTipText((String) actionRevert.getValue(Action.SHORT_DESCRIPTION));
        scfui.getRevertButton().setEnabled(true);

        //-------------------------------------------------------------------------------------
        // Commit

        actionCommit = new ContextAction("Commit",
                                         RegistryModelUtilities.getCommonIcon(FILENAME_ICON_CONFIG_COMMIT),
                                         TOOLTIP_COMMIT,
                                         KeyEvent.VK_C,
                                         false,
                                         true)
            {
            final static String SOURCE = "ContextAction:Commit ";
            private static final long serialVersionUID = 1285165066912474149L;


            public void actionPerformed(final ActionEvent event)
                {
                if ((scfui.getSerialConfigUI() != null)
                   && (scfui.getSerialConfigUI().getGraphComponent() != null))
                    {
                    final String strStatus;

                    // Make sure that the Graph is valid before committing
                    strStatus = scfui.getSerialConfigUI().getGraphComponent().validateGraph();

                    if (strStatus == null)
                        {
                        final String [] strMessage =
                            {
                            "The configuration connections have been checked and are correct,",
                            "however, applying changes to serial ports may affect Instruments which are running.",
                            "Are you sure that you still wish to apply these changes?"
                            };
                        final ImageIcon iconCommitDialog;
                        final int intChoice;

                        iconCommitDialog = RegistryModelUtilities.getCommonIcon(FILENAME_ICON_DIALOG_COMMIT);

                        intChoice = JOptionPane.showOptionDialog(null,
                                                                 strMessage,
                                                                 TITLE_APPLY_CHANGES,
                                                                 JOptionPane.YES_NO_OPTION,
                                                                 JOptionPane.WARNING_MESSAGE,
                                                                 iconCommitDialog,
                                                                 null,
                                                                 null);
                        if (intChoice == JOptionPane.YES_OPTION)
                            {
                            commitChanges();
                            }
                        }
                    else
                        {
                        JOptionPane.showMessageDialog(null,
                                                      MSG_SERIAL_CONFIGURATION + " is invalid",
                                                      "Serial Configuration Validation",
                                                      JOptionPane.WARNING_MESSAGE);
                        }
                    }
                else
                    {
                    LOGGER.error(SOURCE + "Something was NULL");
                    }
                }


            /**************************************************************************************
             * Commit all changes to the Ports.
             * Applying changes to serial ports may affect Instruments which are running.
             */

            private void commitChanges()
                {
                final String SOURCE0 = SOURCE + "commitChanges() ";
                final boolean boolStaribusOpen;

                LOGGER.debug(debug,
                             SOURCE0 + "Apply the configuration changes to all Serial Ports, including the StaribusPort");

                //---------------------------------------------------------------------------------
                // First close the Staribus Port if possible.
                // Do this explicitly via the PortController,
                // since we can't rely on having a valid connection to a physical port.

                boolStaribusOpen = closeStaribusPortStreams();

                // Now close all SerialPorts, ignoring the StaribusPort
                if (scfui.getSerialConfigUI().getSerialPortCells() != null)
                    {
                    Enumeration<mxICell> enumSerialPorts;

                    LOGGER.debug(debug,
                                 SOURCE0 + "Close ALL Serial Ports, except the StaribusPort");

                    enumSerialPorts = scfui.getSerialConfigUI().getSerialPortCells().elements();

                    // Close ALL Serial Ports except the StaribusPort,
                    // but don't mark as closed, since we need to re-open with the new parameters
                    while(enumSerialPorts.hasMoreElements())
                        {
                        final mxICell cellSerialPort;

                        cellSerialPort = enumSerialPorts.nextElement();

                        if ((cellSerialPort != null)
                            && (cellSerialPort.getValue() != null)
                            && (cellSerialPort.getValue() instanceof SerialConfigurationCellDataInterface))
                            {
                            final SerialConfigurationCellDataInterface configData;

                            configData = (SerialConfigurationCellDataInterface)cellSerialPort.getValue();

                            // Everything in the SerialPort Hashtable should have a UserData NodeType of SERIAL_PORT,
                            // but make sure we ignore STARIBUS Streams by looking only for SERIAL
                            if ((SerialConfigurationNodeType.SERIAL_PORT.equals(configData.getNodeType()))
                                && (StreamType.SERIAL.equals(configData.getStreamType())))
                                {
                                try
                                    {
                                    SerialConfigurationHelper.debugCellData(cellSerialPort,
                                                                            configData,
                                                                            SOURCE0 + SerialConfigurationNodeType.SERIAL_PORT.getName(),
                                                                            debug);
                                    SerialConfigurationHelper.logPortProperties(configData.getResourceKey(),
                                                                                configData.getStreamType(),
                                                                                SOURCE0 + SerialConfigurationNodeType.SERIAL_PORT.getName(),
                                                                                debug);
                                    if (configData.getDaoPort() != null)
                                        {
                                        LOGGER.debug(debug,
                                                     SOURCE0 + "Closing Serial Port Streams [port.name=" + configData.getLabel() + "]");

                                        if (configData.getDaoPort().getRxStream() != null)
                                            {
                                            LOGGER.debug(debug, SOURCE0 + "RxStream Close");
                                            configData.getDaoPort().getRxStream().close();
                                            }
                                        else
                                            {
                                            LOGGER.debug(debug, SOURCE0 + "RxStream is NULL, so no action taken");
                                            }

                                        if (configData.getDaoPort().getTxStream() != null)
                                            {
                                            LOGGER.debug(debug, SOURCE0 + "TxStream Close");
                                            configData.getDaoPort().getTxStream().forceClose();
                                            configData.getDaoPort().getTxStream().close();
                                            }
                                        else
                                            {
                                            LOGGER.debug(debug, SOURCE0 + "TxStream is NULL, so no action taken");
                                            }
                                        }
                                    else
                                        {
                                        LOGGER.debug(debug, SOURCE0 + "DaoPort is NULL, so no action taken (Instrument probably not running)");
                                        }
                                    }

                                catch (IOException exception)
                                    {
                                    LOGGER.debug(debug, "IOException [label=" + configData.getLabel() + "]");
                                    }
                                }
                            else
                                {
                                LOGGER.debug(debug, SOURCE0 + "Serial Port cell has UserObject with invalid NodeType or StreamType, so skip this port");
                                SerialConfigurationHelper.debugCellData(cellSerialPort,
                                                                        configData,
                                                                        SOURCE0 + SerialConfigurationNodeType.SERIAL_PORT.getName(),
                                                                        debug);
                                }
                            }
                        else
                            {
                            LOGGER.debug(debug, SOURCE0 + "Serial Port cell has invalid UserObject");
                            }
                        }

                    //-----------------------------------------------------------------------------
                    // Now re-open the StaribusPort if it is already open,
                    // even if it is not connected to a real Port on the graph

                    if (boolStaribusOpen)
                        {
                        openStaribusPortStreams();
                        }
                    else
                        {
                        LOGGER.debug(debug, SOURCE0 + "StaribusPort does not need to be re-opened");
                        }

                    LOGGER.debug(debug,
                                 SOURCE0 + "Re-open ALL Serial Ports, except the StaribusPort");

                    enumSerialPorts = scfui.getSerialConfigUI().getSerialPortCells().elements();

                    // Now re-open all Serial Ports which are currently marked as open,
                    // ignoring the StaribusPort
                    while(enumSerialPorts.hasMoreElements())
                        {
                        final mxICell cellSerialPort;

                        cellSerialPort = enumSerialPorts.nextElement();

                        if ((cellSerialPort != null)
                            && (cellSerialPort.getValue() != null)
                            && (cellSerialPort.getValue() instanceof SerialConfigurationCellDataInterface))
                            {
                            final SerialConfigurationCellDataInterface configData;

                            configData = (SerialConfigurationCellDataInterface)cellSerialPort.getValue();

                            // Everything in the SerialPort Hashtable should have a UserData NodeType of SERIAL_PORT
                            // but make sure we ignore STARIBUS Streams by looking only for SERIAL
                            if ((SerialConfigurationNodeType.SERIAL_PORT.equals(configData.getNodeType()))
                                && (StreamType.SERIAL.equals(configData.getStreamType())))
                                {
                                try
                                    {
                                    SerialConfigurationHelper.debugCellData(cellSerialPort,
                                                                            configData,
                                                                            SOURCE0 + SerialConfigurationNodeType.SERIAL_PORT.getName(),
                                                                            debug);
                                    SerialConfigurationHelper.logPortProperties(configData.getResourceKey(),
                                                                                configData.getStreamType(),
                                                                                SOURCE0 + SerialConfigurationNodeType.SERIAL_PORT.getName(),
                                                                                debug);
                                    // Only re-open those Port Streams which are currently marked as open
                                    // Don't change the open status of the Port
                                    if ((configData.getDaoPort() != null)
                                        && (configData.isOpen()))
                                        {
                                        LOGGER.debug(debug,
                                                     SOURCE0 + "Re-opening Serial Port Streams [port.name=" + configData.getLabel() + "]");

                                        if (configData.getDaoPort().getTxStream() != null)
                                            {
                                            LOGGER.debug(debug, SOURCE0 + "TxStream Open");
                                            configData.getDaoPort().getTxStream().open();
                                            }
                                        else
                                            {
                                            LOGGER.debug(debug, SOURCE0 + "TxStream is NULL, so no action taken");
                                            configData.setOpen(false);
                                            }

                                        if (configData.getDaoPort().getRxStream() != null)
                                            {
                                            LOGGER.debug(debug, SOURCE0 + "RxStream Open");
                                            configData.getDaoPort().getRxStream().open();
                                            }
                                        else
                                            {
                                            LOGGER.debug(debug, SOURCE0 + "RxStream is NULL, so no action taken");
                                            configData.setOpen(false);
                                            }
                                        }
                                    else
                                        {
                                        LOGGER.debug(debug, SOURCE0 + "DaoPort is closed or NULL, so no action taken (Instrument probably not running)");
                                        configData.setOpen(false);
                                        }
                                    }

                                catch (IOException exception)
                                    {
                                    LOGGER.debug(debug, "IOException [label=" + configData.getLabel() + "]");
                                    configData.setOpen(false);
                                    }

                                // We've dealt with this Port now...
                                configData.setChanged(false);
                                }
                            else
                                {
                                LOGGER.debug(debug, SOURCE0 + "Serial Port cell has UserObject with invalid NodeType or StreamType, so skip this port");
                                SerialConfigurationHelper.debugCellData(cellSerialPort,
                                                                        configData,
                                                                        SOURCE0 + SerialConfigurationNodeType.SERIAL_PORT.getName(),
                                                                        debug);
                                }
                            }
                        else
                            {
                            LOGGER.debug(debug, SOURCE0 + "Serial Port cell has invalid UserObject");
                            }
                        }
                    }
                else
                    {
                    LOGGER.debug(debug, SOURCE0 + "There are no Serial Port cells on the graph");
                    }
                }


            /**************************************************************************************
             * Open the Staribus Port Streams if possible.
             * Do this explicitly via the PortController,
             * since we can't rely on having a valid connection to a physical port.
             * NOTE that the Port itself must already be OPEN.
             */

            private void openStaribusPortStreams()
                {
                final String SOURCE1 = SOURCE + "openStaribusPortStreams() ";
                final DaoPortInterface portStaribus;

                LOGGER.debug(debug, SOURCE1);

                // Staribus Port
                portStaribus = PORT_CONTROLLER.getStaribusPort();

                if ((portStaribus != null)
                    && (portStaribus.isPortOpen()))
                    {
                    final boolean boolStaribusDebug;

                    LOGGER.debug(debug,
                                 SOURCE1 + "Opening StaribusPort Streams");

                    // Save the previous debug state
                    boolStaribusDebug = LOADER_PROPERTIES.isStaribusDebug();
                    LOADER_PROPERTIES.setStaribusDebug(debug);

                    // This will open the Rx and Tx Streams,
                    // even if they are already open?
                    portStaribus.open();

                    // Restore the previous debug state
                    LOADER_PROPERTIES.setStaribusDebug(boolStaribusDebug);

                    LOGGER.debug(debug,
                                 SOURCE1 + "Staribus Port has been opened");
                    }
                else
                    {
                    LOGGER.debug(debug,
                                 SOURCE1 + "Staribus Port is closed or NULL [null=" + (portStaribus == null) + "]");
                    }
                }


            /**************************************************************************************
             * Close the Staribus Port Streams if possible.
             * Do this explicitly via the PortController,
             * since we can't rely on having a valid connection to a physical port.
             * Return a flag to show if the port was open before closure,
             * and so needs to be re-opened.
             *
             * @return boolean
             */

            private boolean closeStaribusPortStreams()
                {
                final String SOURCE2 = SOURCE + "closeStaribusPortStreams() ";
                final DaoPortInterface portStaribus;
                boolean boolStaribusReopen;

                LOGGER.debug(debug, SOURCE2);

                // Staribus Port
                portStaribus = PORT_CONTROLLER.getStaribusPort();

                if ((portStaribus != null)
                    && (portStaribus.isPortOpen()))
                    {
                    final boolean boolStaribusDebug;

                    LOGGER.debug(debug,
                                 SOURCE2 + "Closing StaribusPort Streams");

                    // Save the previous debug state
                    boolStaribusDebug = LOADER_PROPERTIES.isStaribusDebug();
                    LOADER_PROPERTIES.setStaribusDebug(debug);

                    // Remember if we need to re-open this port
                    boolStaribusReopen = portStaribus.isPortOpen();

                    // We can't use DaoPort.close()
                    // because it removes the host DAOs and we only want to affect the Streams
                    // and because that won't work for Staribus TxStream,
                    // where we must use forceClose()
                    portStaribus.clearQueues();

                    if (portStaribus.getRxStream() != null)
                        {
                        try
                            {
                            portStaribus.getRxStream().close();
                            }

                        catch (IOException exception)
                            {
                            LOGGER.error(SOURCE2 + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR);
                            boolStaribusReopen = false;
                            }
                        }

                    if (portStaribus.getTxStream() != null)
                        {
                        try
                            {
                            portStaribus.getTxStream().forceClose();
                            }

                        catch (IOException exception)
                            {
                            LOGGER.error(SOURCE2 + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR);
                            boolStaribusReopen = false;
                            }
                        }

                    // Restore the previous debug state
                    LOADER_PROPERTIES.setStaribusDebug(boolStaribusDebug);

                    LOGGER.debug(debug,
                                 SOURCE2 + "Staribus Port has been closed");
                    }
                else
                    {
                    LOGGER.debug(debug,
                                 SOURCE2 + "Staribus Port is already closed or NULL [null=" + (portStaribus == null) + "]");
                    boolStaribusReopen = false;
                    }

                return (boolStaribusReopen);
                }
            };

        scfui.getCommitButton().setAction(actionCommit);
        scfui.getCommitButton().setToolTipText((String) actionCommit.getValue(Action.SHORT_DESCRIPTION));
        scfui.getCommitButton().setEnabled(true);

        //-------------------------------------------------------------------------------------
        // Page Setup

        iconPageSetup = RegistryModelUtilities.getCommonIcon(FILENAME_ICON_PAGE_SETUP);

        if (iconPageSetup != null)
            {
            actionPageSetup = new ContextAction(ReportTablePlugin.PREFIX_PAGE_SETUP + MSG_SERIAL_CONFIGURATION,
                                                iconPageSetup,
                                                ReportTablePlugin.PREFIX_PAGE_SETUP + MSG_SERIAL_CONFIGURATION,
                                                KeyEvent.VK_S,
                                                false,
                                                false)
                {
                final static String SOURCE = "ContextAction:PageSetup ";
                private static final long serialVersionUID = 6802400471966299436L;


                public void actionPerformed(final ActionEvent event)
                    {
                    if (scfui.getSerialConfigUI() != null)
                        {
                        final PrinterJob printerJob;
                        final mxGraphComponent graphComponent;
                        final PageFormat pageFormat;

                        printerJob = PrinterJob.getPrinterJob();
                        graphComponent = scfui.getSerialConfigUI().getGraphComponent();
                        pageFormat = printerJob.pageDialog(graphComponent.getPageFormat());

                        if (pageFormat != null)
                            {
                            graphComponent.setPageFormat(pageFormat);
                            graphComponent.zoomAndCenter();
                            }
                        }
                    }
                };

            scfui.getPageSetupButton().setAction(actionPageSetup);
            scfui.getPageSetupButton().setToolTipText((String) actionPageSetup.getValue(Action.SHORT_DESCRIPTION));
            scfui.getPageSetupButton().setEnabled(true);
            }

        //-------------------------------------------------------------------------------------
        // Print

        iconPrint = RegistryModelUtilities.getCommonIcon(FILENAME_ICON_PRINT);
        iconPrintMessage = RegistryModelUtilities.getCommonIcon(FILENAME_ICON_DIALOG_PRINT);

        if (iconPrint != null)
            {
            actionPrint = new ContextAction(ReportTablePlugin.PREFIX_PRINT + MSG_SERIAL_CONFIGURATION,
                                            iconPrint,
                                            ReportTablePlugin.PREFIX_PRINT + MSG_SERIAL_CONFIGURATION,
                                            KeyEvent.VK_P,
                                            false,
                                            false)
                {
                final static String SOURCE = "ContextAction:Print ";
                private static final long serialVersionUID = 8346968631811861938L;


                public void actionPerformed(final ActionEvent event)
                    {
                    final SwingWorker workerPrinter;

                    workerPrinter = new SwingWorker(REGISTRY.getThreadGroup(),
                                                    "SwingWorker Printer")
                        {
                        public Object construct()
                            {
                            LOGGER.debug(debug, SOURCE + "SwingWorker construct()");

                            // Let the user know what happened
                            return (printDialog());
                            }

                        // Display updates occur on the Event Dispatching Thread
                        public void finished()
                            {
                            final String [] strSuccess =
                                {
                                MSG_DIAGRAM_PRINTED,
                                MSG_PRINT_CANCELLED
                                };

                            if ((get() != null)
                                && (get() instanceof Boolean)
                                && ((Boolean) get())
                                && (!isStopping()))
                                {
                                JOptionPane.showMessageDialog(null,
                                                              strSuccess[0],
                                                              DIALOG_PRINT,
                                                              JOptionPane.INFORMATION_MESSAGE,
                                                              iconPrintMessage);
                                }
                            else
                                {
                                JOptionPane.showMessageDialog(null,
                                                              strSuccess[1],
                                                              DIALOG_PRINT,
                                                              JOptionPane.INFORMATION_MESSAGE,
                                                              iconPrintMessage);
                                }
                            }
                        };

                    // Start the Print Thread
                    workerPrinter.start();
                    }


                /**********************************************************************************
                 * Show the Print dialog.
                 *
                 * @return boolean
                 */

                private boolean printDialog()
                    {
                    boolean boolSuccess;

                    // Check to see that we actually have a printer...
                    if (PrinterJob.lookupPrintServices().length == 0)
                        {
                        JOptionPane.showMessageDialog(null,
                                                      ReportTablePlugin.MSG_NO_PRINTER,
                                                      ReportTablePlugin.PREFIX_PRINT + MSG_SERIAL_CONFIGURATION,
                                                      JOptionPane.WARNING_MESSAGE,
                                                      iconPrintMessage);
                        boolSuccess = false;
                        }
                    else
                        {
                        final PrinterJob printerJob;

                        printerJob = PrinterJob.getPrinterJob();

                        if ((scfui.getSerialConfigUI() != null)
                            && (printerJob.printDialog()))
                            {
                            final mxGraphComponent graphComponent;
                            final PageFormat pageFormat;

                            graphComponent = scfui.getSerialConfigUI().getGraphComponent();
                            pageFormat = graphComponent.getPageFormat();
                            printerJob.setPrintable(graphComponent, pageFormat);

                            try
                                {
                                printerJob.print();
                                boolSuccess = true;
                                }

                            catch (final PrinterException exception)
                                {
                                LOGGER.error(SOURCE + "[exception=" + exception.getMessage() + "]");
                                boolSuccess = false;
                                }
                            }
                        else
                            {
                            boolSuccess = false;
                            }
                        }

                    return (boolSuccess);
                    }
                };

            scfui.getPrintButton().setAction(actionPrint);
            scfui.getPrintButton().setToolTipText((String) actionPrint.getValue(Action.SHORT_DESCRIPTION));
            scfui.getPrintButton().setEnabled(true);
            }

        //-------------------------------------------------------------------------------------
        // Export

        actionExport = new ContextAction("Export",
                                         RegistryModelUtilities.getCommonIcon(FILENAME_ICON_EXPORT_CONFIG),
                                         TOOLTIP_EXPORT,
                                         KeyEvent.VK_E,
                                         false,
                                         true)
            {
            final static String SOURCE = "ContextAction:Export ";
            private static final long serialVersionUID = 2289353484857897775L;


            public void actionPerformed(final ActionEvent event)
                {
                FileOutputStream outputStream;

                outputStream = null;

                try
                    {
                    final String FILENAME_EXPORT = "workspace/serial-configuration.png";
                    final BufferedImage image;
                    final mxCodec codec;
                    final String strXML;
                    final mxPngEncodeParam param;
                    final mxPngImageEncoder encoder;

                    image = mxCellRenderer.createBufferedImage(scfui.getSerialConfigUI().getGraph(),
                                                               null,
                                                               1,
                                                               Color.WHITE,
                                                               scfui.getSerialConfigUI().getGraphComponent().isAntiAlias(),
                                                               null,
                                                               scfui.getSerialConfigUI().getGraphComponent().getCanvas());

                    // Creates the URL-encoded XML data
                    codec = new mxCodec();
                    strXML = URLEncoder.encode(mxXmlUtils.getXml(codec.encode(scfui.getSerialConfigUI().getGraph().getModel())),
                                               "UTF-8");
                    param = mxPngEncodeParam.getDefaultEncodeParam(image);
                    param.setCompressedText(new String[] { "mxGraphModel", strXML });

                    // Saves as a PNG file
                    outputStream = new FileOutputStream(new File(FILENAME_EXPORT));

                    encoder = new mxPngImageEncoder(outputStream, param);

                    if (image != null)
                        {
                        final String [] strSuccess =
                            {
                            "The Serial Configuration Diagram has been exported to:",
                            FILENAME_EXPORT
                            };
                        final ImageIcon iconExportConfig;

                        encoder.encode(image);
                        iconExportConfig = RegistryModelUtilities.getCommonIcon(FILENAME_ICON_DIALOG_EXPORT_CONFIG);

                        // If we get here, then it must have succeeded
                        JOptionPane.showMessageDialog(null,
                                                      strSuccess,
                                                      DIALOG_EXPORT,
                                                      JOptionPane.INFORMATION_MESSAGE,
                                                      iconExportConfig);
                        }
                    else
                        {
                        LOGGER.error(SOURCE + "No Image");
                        }


                    //ToDo REVIEW Use DataExporter.exportImage(image, ) instead?
                    }

                catch (IllegalStateException exception)
                    {
                    exception.printStackTrace();
                    }

                catch (UnsupportedEncodingException exception)
                    {
                    exception.printStackTrace();
                    }

                catch (IOException exception)
                    {
                    exception.printStackTrace();
                    }

                finally
                    {
                    if (outputStream != null)
                        {
                        try
                            {
                            outputStream.close();
                            }

                        catch (IOException exception)
                            {
                            exception.printStackTrace();
                            }
                        }
                    }
                }
            };

        scfui.getExportButton().setAction(actionExport);
        scfui.getExportButton().setToolTipText((String) actionExport.getValue(Action.SHORT_DESCRIPTION));
        scfui.getExportButton().setEnabled(true);

        //-------------------------------------------------------------------------------------
        // Put it all together

        toolbar.removeAll();

        toolbar.addSeparator(DIM_TOOLBAR_SEPARATOR_BUTTON);
        toolbar.add(labelName);
        toolbar.addSeparator(DIM_TOOLBAR_SEPARATOR);

        toolbar.add(Box.createHorizontalGlue());

        //toolbar.add(scfui.getRevertButton());
        //toolbar.addSeparator(DIM_TOOLBAR_SEPARATOR);

        toolbar.add(scfui.getCommitButton());
        toolbar.addSeparator(DIM_TOOLBAR_SEPARATOR);

        toolbar.add(scfui.getPageSetupButton());
        toolbar.addSeparator(DIM_TOOLBAR_SEPARATOR);

        toolbar.add(scfui.getPrintButton());
        toolbar.addSeparator(DIM_TOOLBAR_SEPARATOR);

        toolbar.add(scfui.getExportButton());
        toolbar.addSeparator(DIM_TOOLBAR_SEPARATOR);

        NavigationUtilities.updateComponentTreeUI(toolbar);
        }


    /***********************************************************************************************
     * Construct a SerialConfigurationFrameUIComponent.
     *
     * @param hostinstrument
     * @param instrumentxml
     * @param hostui
     * @param task
     * @param font
     * @param colour
     * @param resourcekey
     */

    public SerialConfigurationFrameUIComponent(final ObservatoryInstrumentInterface hostinstrument,
                                               final Instrument instrumentxml,
                                               final ObservatoryUIInterface hostui,
                                               final TaskPlugin task,
                                               final FontInterface font,
                                               final ColourInterface colour,
                                               final String resourcekey)
        {
        super(hostinstrument,
              instrumentxml,
              hostui,
              task,
              font,
              colour,
              resourcekey);

        // UI
        this.uiSerialConfig = null;
        this.toolbarConfigCommands = null;
        this.buttonRevert = null;
        this.buttonCommit = null;
        this.buttonPageSetup = null;
        this.buttonPrint = null;
        this.buttonExport = null;

        // This sets the debug state for all of the Serial Configuration Utility
        setDebug(false);
        }


    /**********************************************************************************************/
    /* UI State                                                                                   */
    /***********************************************************************************************
     * Initialise this UIComponent.
     */

    public void initialiseUI()
        {
        final String SOURCE = "SerialConfigurationFrameUIComponent.initialiseUI() ";

        LOGGER.debug(isDebug(), SOURCE);

        // Colours
        setBackground(UIComponentPlugin.DEFAULT_COLOUR_CANVAS.getColor());

        // Create the Config JPanel and initialise it
        // Do this first to get the colours and fonts
        setSerialConfigUI(new SerialConfigurationUIComponent(getHostTask(),
                                                             getObservatoryUI(),
                                                             getHostInstrument(),
                                                             getFontData(),
                                                             getColourData(),
                                                             getResourceKey(),
                                                             isDebug()));
        getSerialConfigUI().initialiseUI();

        // Create the JToolBar and initialise it
        setToolBar(new JToolBar());
        getToolBar().setFloatable(false);
        getToolBar().setMinimumSize(DIM_TOOLBAR_SIZE);
        getToolBar().setPreferredSize(DIM_TOOLBAR_SIZE);
        getToolBar().setMaximumSize(DIM_TOOLBAR_SIZE);

        initialiseCommandsToolbar(getToolBar(),
                                  getHostInstrument(),
                                  this,
                                  getFontData(),
                                  getColourData(),
                                  DEFAULT_COLOUR_TAB_BACKGROUND,
                                  isDebug());

        // Put the components together
        add(getToolBar(), BorderLayout.NORTH);
        add((Component) getSerialConfigUI(), BorderLayout.CENTER);
        }


    /***********************************************************************************************
     * Run this UIComponent.
     */

    public void runUI()
        {
        final String SOURCE = "SerialConfigurationFrameUIComponent.runUI() ";

        LOGGER.debug(isDebug(), SOURCE);

        super.runUI();

        if (getSerialConfigUI() != null)
            {
            UIComponentHelper.runComponentAndTransferActions((Component) getSerialConfigUI(), this);
            }
        }


    /***********************************************************************************************
     * Stop this UIComponent.
     */

    public void stopUI()
        {
        final String SOURCE = "SerialConfigurationFrameUIComponent.stopUI() ";

        LOGGER.debug(isDebug(), SOURCE);

        super.stopUI();

        if (getSerialConfigUI() != null)
            {
            getSerialConfigUI().stopUI();
            }
        }


    /***********************************************************************************************
     * Dispose of all components of this UIComponent.
     */

    public void disposeUI()
        {
        final String SOURCE = "SerialConfigurationFrameUIComponent.disposeUI() ";

        LOGGER.debug(isDebug(), SOURCE);

        stopUI();

        if (getToolBar() != null)
            {
            getToolBar().removeAll();
            setToolBar(null);
            }

        if (getSerialConfigUI() != null)
            {
            getSerialConfigUI().disposeUI();
            setSerialConfigUI(null);
            }

        super.disposeUI();
        }


    /**********************************************************************************************/
    /* UI                                                                                         */
    /***********************************************************************************************
     * Get the Serial Config UI.
     *
     * @return SerialConfigurationUIComponentInterface
     */

    public SerialConfigurationUIComponentInterface getSerialConfigUI()
        {
        return (this.uiSerialConfig);
        }


    /***********************************************************************************************
     * Set the Serial Config UI.
     *
     * @param configui
     */

    private void setSerialConfigUI(final SerialConfigurationUIComponentInterface configui)
        {
        this.uiSerialConfig = configui;
        }


    /***********************************************************************************************
     * Get the Config JToolBar.
     *
     * @return JToolBar
     */

    private JToolBar getToolBar()
        {
        return (this.toolbarConfigCommands);
        }


    /***********************************************************************************************
     * Set the Config JToolBar.
     *
     * @param toolbar
     */

    private void setToolBar(final JToolBar toolbar)
        {
        this.toolbarConfigCommands = toolbar;
        }


    /***********************************************************************************************
     * Get the Revert button.
     *
     * @return JButton
     */

    public JButton getRevertButton()
        {
        return (this.buttonRevert);
        }


    /***********************************************************************************************
     * Set the Revert button.
     *
     * @param button
     */

    public void setRevertButton(final JButton button)
        {
        this.buttonRevert = button;
        }


    /***********************************************************************************************
     * Get the Commit button.
     *
     * @return JButton
     */

    public JButton getCommitButton()
        {
        return (this.buttonCommit);
        }


    /***********************************************************************************************
     * Set the Commit button.
     *
     * @param button
     */

    public void setCommitButton(final JButton button)
        {
        this.buttonCommit = button;
        }


    /***********************************************************************************************
     * Get the PageSetup button.
     *
     * @return JButton
     */

    public JButton getPageSetupButton()
        {
        return (this.buttonPageSetup);
        }


    /***********************************************************************************************
     * Set the PageSetup button.
     *
     * @param button
     */

    public void setPageSetupButton(final JButton button)
        {
        this.buttonPageSetup = button;
        }


    /***********************************************************************************************
     * Get the Print button.
     *
     * @return JButton
     */

    public JButton getPrintButton()
        {
        return (this.buttonPrint);
        }


    /***********************************************************************************************
     * Set the Print button.
     *
     * @param button
     */

    public void setPrintButton(final JButton button)
        {
        this.buttonPrint = button;
        }


    /***********************************************************************************************
     * Get the Export button.
     *
     * @return JButton
     */

    public JButton getExportButton()
        {
        return (this.buttonExport);
        }


    /***********************************************************************************************
     * Set the Export button.
     *
     * @param button
     */

    public void setExportButton(final JButton button)
        {
        this.buttonExport = button;
        }
    }
