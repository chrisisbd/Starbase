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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.audio;


import org.lmn.fc.common.actions.ContextAction;
import org.lmn.fc.common.utilities.threads.SwingWorker;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ObservatoryInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryUIInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.InstrumentUIComponentDecorator;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.AudioExplorerFrameUIComponentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.AudioExplorerUIComponentInterface;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.model.registry.NavigationUtilities;
import org.lmn.fc.model.registry.RegistryModelUtilities;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;
import org.lmn.fc.ui.components.UIComponentHelper;
import org.lmn.fc.ui.reports.ReportTableHelper;
import org.lmn.fc.ui.reports.ReportTablePlugin;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.print.PageFormat;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;


/***************************************************************************************************
 * AudioExplorerFrameUIComponent.
 */

public final class AudioExplorerFrameUIComponent extends InstrumentUIComponentDecorator
                                                 implements AudioExplorerFrameUIComponentInterface
    {
    private static final long serialVersionUID = 242747879307554737L;

    // UI
    private AudioExplorerUIComponentInterface uiAudioExplorer;
    private JToolBar toolBar;
    private JButton buttonRescan;
    private JButton buttonPageSetup;
    private JButton buttonPrint;


    /***********************************************************************************************
     * Initialise the Commands Toolbar.
     *
     * @param toolbar
     * @param obsinstrument
     * @param aefui
     * @param fontdata
     * @param colourforeground
     * @param colourbackground
     * @param debug
     */

    private static void initialiseCommandsToolbar(final JToolBar toolbar,
                                                  final ObservatoryInstrumentInterface obsinstrument,
                                                  final AudioExplorerFrameUIComponentInterface aefui,
                                                  final FontInterface fontdata,
                                                  final ColourInterface colourforeground,
                                                  final ColourInterface colourbackground,
                                                  final boolean debug)
        {
        final String SOURCE = "AudioExplorerFrameUIComponent.initialiseCommandsToolbar() ";
        final JLabel labelName;
        final ContextAction actionRescan;
        final ContextAction actionPageSetup;
        final ContextAction actionPrint;

        toolbar.setBackground(colourbackground.getColor());

        //-------------------------------------------------------------------------------------
        // Initialise the Label

        labelName = new JLabel(AUDIO_EXPLORER_NAME,
                               RegistryModelUtilities.getAtomIcon(obsinstrument.getHostAtom(),
                                                                  ObservatoryInterface.FILENAME_ICON_AUDIO_EXPLORER),
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

        aefui.setRescanButton(new JButton());
        aefui.getRescanButton().setBorder(BORDER_BUTTON);
        aefui.getRescanButton().setHideActionText(true);

        aefui.setPageSetupButton(new JButton());
        aefui.getPageSetupButton().setBorder(BORDER_BUTTON);
        aefui.getPageSetupButton().setHideActionText(true);

        aefui.setPrintButton(new JButton());
        aefui.getPrintButton().setBorder(BORDER_BUTTON);
        aefui.getPrintButton().setHideActionText(true);

        //-------------------------------------------------------------------------------------
        // Rescan

        actionRescan = new ContextAction(ACTION_NAME_RESCAN,
                                         RegistryModelUtilities.getAtomIcon(obsinstrument.getHostAtom(),
                                                                            ObservatoryInterface.FILENAME_ICON_AUDIO_EXPLORER_RESCAN),
                                         ACTION_TOOLTIP_RESCAN,
                                         KeyEvent.VK_R,
                                         false,
                                         true)
            {
            final static String SOURCE = "ContextAction:Rescan ";
            private static final long serialVersionUID = 423926301609774538L;


            public void actionPerformed(final ActionEvent event)
                {
                if (aefui.getAudioExplorerUI() != null)
                    {
                    // Rebuild the JTree of audio devices
                    aefui.getAudioExplorerUI().stopUI();
                    aefui.getAudioExplorerUI().disposeUI();
                    aefui.getAudioExplorerUI().initialiseUI();
                    }
                }
            };

        aefui.getRescanButton().setAction(actionRescan);
        aefui.getRescanButton().setToolTipText((String) actionRescan.getValue(Action.SHORT_DESCRIPTION));
        aefui.getRescanButton().setEnabled(true);


        //-------------------------------------------------------------------------------------
        // Printing
        //-------------------------------------------------------------------------------------
        // Page Setup

        actionPageSetup = new ContextAction(ReportTablePlugin.PREFIX_PAGE_SETUP + MSG_AUDIO_EXPLORER,
                                            RegistryModelUtilities.getCommonIcon(FILENAME_ICON_PAGE_SETUP),
                                            ReportTablePlugin.PREFIX_PAGE_SETUP + MSG_AUDIO_EXPLORER,
                                            KeyEvent.VK_S,
                                            false,
                                            true)
            {
            final static String SOURCE = "ContextAction:PageSetup ";
            private static final long serialVersionUID = 6802400471966299436L;


            public void actionPerformed(final ActionEvent event)
                {
                if (aefui.getAudioExplorerUI() != null)
                    {
                    final PrinterJob printerJob;
                    final PageFormat pageFormat;

                    printerJob = PrinterJob.getPrinterJob();
                    pageFormat = printerJob.pageDialog(aefui.getAudioExplorerUI().getPageFormat());

                    if (pageFormat != null)
                        {
                        aefui.getAudioExplorerUI().setPageFormat(pageFormat);
                        }
                    }
                else
                    {
                    LOGGER.error(SOURCE + "AudioExplorer unexpectedly NULL");
                    }
                }
            };

        aefui.getPageSetupButton().setAction(actionPageSetup);
        aefui.getPageSetupButton().setToolTipText((String) actionPageSetup.getValue(Action.SHORT_DESCRIPTION));
        aefui.getPageSetupButton().setEnabled(true);

        //-------------------------------------------------------------------------------------
        // Print

        actionPrint = new ContextAction(ReportTablePlugin.PREFIX_PRINT + MSG_AUDIO_EXPLORER,
                                        RegistryModelUtilities.getCommonIcon(FILENAME_ICON_PRINT),
                                        ReportTablePlugin.PREFIX_PRINT + MSG_AUDIO_EXPLORER,
                                        KeyEvent.VK_P,
                                        false,
                                        true)
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
                            MSG_AUDIO_EXPLORER_PRINTED,
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
                                                          RegistryModelUtilities.getCommonIcon(FILENAME_ICON_DIALOG_PRINT));
                            }
                        else
                            {
                            JOptionPane.showMessageDialog(null,
                                                          strSuccess[1],
                                                          DIALOG_PRINT,
                                                          JOptionPane.INFORMATION_MESSAGE,
                                                          RegistryModelUtilities.getCommonIcon(FILENAME_ICON_DIALOG_PRINT));
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
                                                  ReportTablePlugin.PREFIX_PRINT + MSG_AUDIO_EXPLORER,
                                                  JOptionPane.WARNING_MESSAGE,
                                                  RegistryModelUtilities.getCommonIcon(FILENAME_ICON_DIALOG_PRINT));
                    boolSuccess = false;
                    }
                else
                    {
                    final PrinterJob printerJob;

                    printerJob = PrinterJob.getPrinterJob();

                    if ((aefui.getAudioExplorerUI() != null)
                        && (aefui.getAudioExplorerUI().getReportTable() != null)
                        && (printerJob.printDialog()))
                        {
                        final PageFormat pageFormat;

                        pageFormat = aefui.getAudioExplorerUI().getPageFormat();

                        if (pageFormat != null)
                            {
                            // The AudioExplorer is Printable
                            // ToDo Header & Footer MessageFormats
                            printerJob.setPrintable(aefui.getAudioExplorerUI().getReportTable().getReportTable().getPrintable(JTable.PrintMode.FIT_WIDTH,
                                                                                                                              null,
                                                                                                                              null),
                                                    pageFormat);
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
                    else
                        {
                        boolSuccess = false;
                        }
                    }

                return (boolSuccess);
                }
            };

        aefui.getPrintButton().setAction(actionPrint);
        aefui.getPrintButton().setToolTipText((String) actionPrint.getValue(Action.SHORT_DESCRIPTION));
        aefui.getPrintButton().setEnabled(true);

        //-------------------------------------------------------------------------------------
        // Put it all together

        toolbar.removeAll();

        toolbar.addSeparator(DIM_TOOLBAR_SEPARATOR_BUTTON);
        toolbar.add(labelName);
        toolbar.addSeparator(DIM_TOOLBAR_SEPARATOR);

        toolbar.add(Box.createHorizontalGlue());

        toolbar.add(aefui.getPageSetupButton());
        toolbar.addSeparator(DIM_TOOLBAR_SEPARATOR);

        toolbar.add(aefui.getPrintButton());
        toolbar.addSeparator(DIM_TOOLBAR_SEPARATOR);

        toolbar.add(aefui.getRescanButton());
        toolbar.addSeparator(DIM_TOOLBAR_SEPARATOR);

        NavigationUtilities.updateComponentTreeUI(toolbar);
        }


    /***********************************************************************************************
     * Construct a AudioExplorerFrameUIComponent.
     *
     * @param hostinstrument
     * @param instrumentxml
     * @param hostui
     * @param task
     * @param font
     * @param colour
     * @param resourcekey
     */

    public AudioExplorerFrameUIComponent(final ObservatoryInstrumentInterface hostinstrument,
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
        this.uiAudioExplorer = null;
        this.toolBar = null;
        this.buttonRescan = null;
        this.buttonPageSetup = null;
        this.buttonPrint = null;
        }


    /**********************************************************************************************/
    /* UI State                                                                                   */
    /***********************************************************************************************
     * Initialise this UIComponent.
     */

    public void initialiseUI()
        {
        final String SOURCE = "AudioExplorerFrameUIComponent.initialiseUI() ";

        LOGGER.debug(isDebug(), SOURCE);

        // Colours
        setBackground(DEFAULT_COLOUR_CANVAS.getColor());

        // Create the AudioExplorerUIComponent and initialise it
        // Do this first to get the colours and fonts
        // This is the only creation of AudioExplorerUIComponent
        setAudioExplorerUI(new AudioExplorerUIComponent(getHostInstrument(),
                                                        getHostInstrument().getInstrument(),
                                                        getObservatoryUI(),
                                                        getHostTask(),
                                                        getFontData(),
                                                        getColourData(),
                                                        getResourceKey(),
                                                        isDebug()));
        getAudioExplorerUI().initialiseUI();

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
        add((Component) getAudioExplorerUI(), BorderLayout.CENTER);
        }


    /***********************************************************************************************
     * Run this UIComponent.
     */

    public void runUI()
        {
        final String SOURCE = "AudioExplorerFrameUIComponent.runUI() ";

        LOGGER.debug(isDebug(), SOURCE);

        super.runUI();

        if (getAudioExplorerUI() != null)
            {
            UIComponentHelper.runComponentAndTransferActions((Component) getAudioExplorerUI(), this);
            }
        }


    /***********************************************************************************************
     * Stop this UIComponent.
     */

    public void stopUI()
        {
        final String SOURCE = "AudioExplorerFrameUIComponent.stopUI() ";

        LOGGER.debug(isDebug(), SOURCE);

        super.stopUI();

        if (getAudioExplorerUI() != null)
            {
            getAudioExplorerUI().stopUI();
            }
        }


    /***********************************************************************************************
     * Dispose of all components of this UIComponent.
     */

    public void disposeUI()
        {
        final String SOURCE = "AudioExplorerFrameUIComponent.disposeUI() ";

        LOGGER.debug(isDebug(), SOURCE);

        stopUI();

        if (getToolBar() != null)
            {
            getToolBar().removeAll();
            setToolBar(null);
            }

        if (getAudioExplorerUI() != null)
            {
            getAudioExplorerUI().disposeUI();
            setAudioExplorerUI(null);
            }

        super.disposeUI();
        }


    /**********************************************************************************************/
    /* UI                                                                                         */
    /***********************************************************************************************
     * Get the AudioExplorer UI.
     *
     * @return AudioExplorerUIComponentInterface
     */

    public AudioExplorerUIComponentInterface getAudioExplorerUI()
        {
        return (this.uiAudioExplorer);
        }


    /***********************************************************************************************
     * Set the AudioExplorer UI.
     *
     * @param explorerui
     */

    private void setAudioExplorerUI(final AudioExplorerUIComponentInterface explorerui)
        {
        this.uiAudioExplorer = explorerui;
        }


    /***********************************************************************************************
     * Get the AudioExplorer JToolBar.
     *
     * @return JToolBar
     */

    private JToolBar getToolBar()
        {
        return (this.toolBar);
        }


    /***********************************************************************************************
     * Set the AudioExplorer JToolBar.
     *
     * @param toolbar
     */

    private void setToolBar(final JToolBar toolbar)
        {
        this.toolBar = toolbar;
        }


    /***********************************************************************************************
     * Get the Rescan button.
     *
     * @return JButton
     */

    public JButton getRescanButton()
        {
        return (this.buttonRescan);
        }


    /***********************************************************************************************
     * Set the Rescan button.
     *
     * @param button
     */

    public void setRescanButton(final JButton button)
        {
        this.buttonRescan = button;
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
    }
