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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.timezones;


import org.lmn.fc.common.actions.ContextAction;
import org.lmn.fc.common.utilities.misc.Utilities;
import org.lmn.fc.common.utilities.threads.SwingWorker;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ObservatoryInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryUIInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.InstrumentUIComponentDecorator;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.TimeZonesFrameUIComponentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.TimeZonesUIComponentInterface;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.model.registry.NavigationUtilities;
import org.lmn.fc.model.registry.RegistryModelUtilities;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;
import org.lmn.fc.ui.choosers.ChooserInterface;
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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;


/***************************************************************************************************
 * TimeZonesFrameUIComponent.
 */

public final class TimeZonesFrameUIComponent extends InstrumentUIComponentDecorator
                                             implements TimeZonesFrameUIComponentInterface
    {
    private static final long serialVersionUID = -7695306074475235601L;

    // UI
    private TimeZonesUIComponentInterface uiComponent;
    private JToolBar toolBar;

    private JButton buttonOpenFile;
    private JButton buttonSaveAsFile;
    private JButton buttonCut;
    private JButton buttonCopy;
    private JButton buttonPaste;
    private JButton buttonDelete;
    private JButton buttonPageSetup;
    private JButton buttonPrint;
    private JButton buttonClearAll;


    /***********************************************************************************************
     * Initialise the Commands Toolbar.
     *
     * @param toolbar
     * @param obsinstrument
     * @param tzfui
     * @param fontdata
     * @param colourforeground
     * @param colourbackground
     * @param debug
     */

    private static void initialiseCommandsToolbar(final JToolBar toolbar,
                                                  final ObservatoryInstrumentInterface obsinstrument,
                                                  final TimeZonesFrameUIComponentInterface tzfui,
                                                  final FontInterface fontdata,
                                                  final ColourInterface colourforeground,
                                                  final ColourInterface colourbackground,
                                                  final boolean debug)
        {
        final String SOURCE = "TimeZonesFrameUIComponent.initialiseCommandsToolbar() ";
        final JLabel labelName;
        final ContextAction actionOpenFile;
        final ContextAction actionSaveAsFile;
        final ContextAction actionCut;
        final ContextAction actionCopy;
        final ContextAction actionPaste;
        final ContextAction actionDelete;
        final ContextAction actionPageSetup;
        final ContextAction actionPrint;
        final ContextAction actionClearAll;

        toolbar.setBackground(colourbackground.getColor());

        //-------------------------------------------------------------------------------------
        // Initialise the Label

        labelName = new JLabel(TITLE_TIME_ZONES,
                               RegistryModelUtilities.getAtomIcon(obsinstrument.getHostAtom(),
                                                                  ObservatoryInterface.FILENAME_ICON_TIME_ZONES),
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

        tzfui.setOpenFileButton(new JButton());
        tzfui.getOpenFileButton().setBorder(BORDER_BUTTON);

        // Ensure that no text appears next to the Icon...
        tzfui.getOpenFileButton().setHideActionText(true);

        tzfui.setSaveAsFileButton(new JButton());
        tzfui.getSaveAsFileButton().setBorder(BORDER_BUTTON);

        // Ensure that no text appears next to the Icon...
        tzfui.getSaveAsFileButton().setHideActionText(true);

        tzfui.setCutButton(new JButton());
        tzfui.getCutButton().setBorder(BORDER_BUTTON);

        // Ensure that no text appears next to the Icon...
        tzfui.getCutButton().setHideActionText(true);

        tzfui.setCopyButton(new JButton());
        tzfui.getCopyButton().setBorder(BORDER_BUTTON);

        // Ensure that no text appears next to the Icon...
        tzfui.getCopyButton().setHideActionText(true);

        tzfui.setPasteButton(new JButton());
        tzfui.getPasteButton().setBorder(BORDER_BUTTON);

        // Ensure that no text appears next to the Icon...
        tzfui.getPasteButton().setHideActionText(true);

        tzfui.setDeleteButton(new JButton());
        tzfui.getDeleteButton().setBorder(BORDER_BUTTON);

        // Ensure that no text appears next to the Icon...
        tzfui.getDeleteButton().setHideActionText(true);

        tzfui.setPageSetupButton(new JButton());
        tzfui.getPageSetupButton().setBorder(BORDER_BUTTON);

        // Ensure that no text appears next to the Icon...
        tzfui.getPageSetupButton().setHideActionText(true);

        tzfui.setPrintButton(new JButton());
        tzfui.getPrintButton().setBorder(BORDER_BUTTON);

        // Ensure that no text appears next to the Icon...
        tzfui.getPrintButton().setHideActionText(true);

        tzfui.setClearAllButton(new JButton());
        tzfui.getClearAllButton().setBorder(BORDER_BUTTON);

        // Ensure that no text appears next to the Icon...
        tzfui.getClearAllButton().setHideActionText(true);

        //-------------------------------------------------------------------------------------
        // OpenFile

        actionOpenFile = new ContextAction(ACTION_NAME_OPEN_FILE,
                                           RegistryModelUtilities.getCommonIcon(FILENAME_ICON_OPEN_FILE),
                                           ACTION_TOOLTIP_OPEN_FILE,
                                           KeyEvent.VK_O,
                                           false,
                                           true)
            {
            final static String SOURCE = "ContextAction:OpenFile ";
            private static final long serialVersionUID = -598953341739066982L;


            public void actionPerformed(final ActionEvent event)
                {
                if (tzfui.getTimeZonesUI() != null)
                    {
                    final JFileChooser chooser;
                    final int intStatus;

                    chooser = new JFileChooser();
                    chooser.setDialogTitle("Load data from a file");
                    chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                    chooser.setForeground(colourforeground.getColor());

                    chooser.setApproveButtonText(ChooserInterface.BUTTON_SELECT);
                    chooser.setApproveButtonToolTipText("Enter the filename");

                    Utilities.setContainerHierarchyFonts(chooser.getComponents(),
                                                         DEFAULT_FONT);

                    Utilities.setContainerHierarchyColours(chooser.getComponents(),
                                                           colourforeground,
                                                           null);

                    intStatus = chooser.showOpenDialog((Component)tzfui.getTimeZonesUI());

                    if (intStatus == JFileChooser.APPROVE_OPTION)
                        {
                        final File file;
                        final ClassLoader classLoader;
                        final InputStream inputStream;

                        file = chooser.getSelectedFile();
                        classLoader = this.getClass().getClassLoader();
                        inputStream = classLoader.getResourceAsStream(file.getAbsolutePath());

                        try
                            {
                            if (inputStream != null)
                                {

                                }
                            else
                                {
                                if (file.isFile())
                                    {

                                    }
                                else
                                    {
                                    throw new IOException("Resource not found: " + file.getAbsolutePath());
                                    }
                                }
                            }

                        catch (IOException exception)
                            {
                            LOGGER.error(SOURCE + "File read error [exception=" + exception.getMessage() + "]");

//                                final String message = msg.getString("Error.Title");
//                                String title = msg.getString("Error.Desc");
//                                title = MessageFormat.format(title,
//                                                             new Object[]{ioe.toString()});
//                                JOptionPane.showMessageDialog(this, message, title,
//                                                              JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    }
                else
                    {
                    LOGGER.error(SOURCE + "TimeZonesUI unexpectedly NULL");
                    }
                }
            };

        tzfui.getOpenFileButton().setAction(actionOpenFile);
        tzfui.getOpenFileButton().setToolTipText((String) actionOpenFile.getValue(Action.SHORT_DESCRIPTION));
        tzfui.getOpenFileButton().setEnabled(true);


        //-------------------------------------------------------------------------------------
        // SaveAsFile

        actionSaveAsFile = new ContextAction(ACTION_NAME_SAVE_AS_FILE,
                                             RegistryModelUtilities.getCommonIcon(FILENAME_ICON_SAVE_AS_FILE),
                                             ACTION_TOOLTIP_SAVE_AS_FILE,
                                             KeyEvent.VK_S,
                                             false,
                                             true)
            {
            final static String SOURCE = "ContextAction:SaveAsFile ";
            private static final long serialVersionUID = -598953341739066982L;


            public void actionPerformed(final ActionEvent event)
                {
                if (tzfui.getTimeZonesUI() != null)
                    {
                    final JFileChooser chooser;
                    final int intStatus;

                    chooser = new JFileChooser();
                    chooser.setDialogTitle("Save data in a file");
                    chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                    chooser.setForeground(colourforeground.getColor());

                    chooser.setApproveButtonText(ChooserInterface.BUTTON_SELECT);
                    chooser.setApproveButtonToolTipText("Enter the filename");

                    Utilities.setContainerHierarchyFonts(chooser.getComponents(),
                                                         DEFAULT_FONT);

                    Utilities.setContainerHierarchyColours(chooser.getComponents(),
                                                           colourforeground,
                                                           null);

                    intStatus = chooser.showSaveDialog((Component)tzfui.getTimeZonesUI());

                    if (intStatus == JFileChooser.APPROVE_OPTION)
                        {
                        final File file;

                        file = chooser.getSelectedFile();

                        if (file != null)
                            {


                            }
                        else
                            {
                            LOGGER.error(SOURCE + "File unexpectedly NULL");
                            }
                        }
                    }
                else
                    {
                    LOGGER.error(SOURCE + "TimeZonesUI or TableModel unexpectedly NULL");
                    }
                }
            };

        tzfui.getSaveAsFileButton().setAction(actionSaveAsFile);
        tzfui.getSaveAsFileButton().setToolTipText((String) actionSaveAsFile.getValue(Action.SHORT_DESCRIPTION));
        tzfui.getSaveAsFileButton().setEnabled(true);


        //-------------------------------------------------------------------------------------
        // Cut

        actionCut = new ContextAction(ACTION_NAME_CUT,
                                      RegistryModelUtilities.getCommonIcon(FILENAME_ICON_CUT),
                                      ACTION_TOOLTIP_CUT,
                                      KeyEvent.VK_X,
                                      false,
                                      true)
            {
            final static String SOURCE = "ContextAction:Cut ";
            private static final long serialVersionUID = 2326420135186092102L;


            public void actionPerformed(final ActionEvent event)
                {
                if (tzfui.getTimeZonesUI() != null)
                    {

                    }
                else
                    {
                    UIManager.getLookAndFeel().provideErrorFeedback((Component) tzfui);
                    }
                }
            };

        tzfui.getCutButton().setAction(actionCut);
        tzfui.getCutButton().setToolTipText((String) actionCut.getValue(Action.SHORT_DESCRIPTION));
        tzfui.getCutButton().setEnabled(true);


        //-------------------------------------------------------------------------------------
        // Copy

        actionCopy = new ContextAction(ACTION_NAME_COPY,
                                       RegistryModelUtilities.getCommonIcon(FILENAME_ICON_COPY),
                                       TimeZonesFrameUIComponentInterface.ACTION_TOOLTIP_COPY,
                                       KeyEvent.VK_C,
                                       false,
                                       true)
            {
            final static String SOURCE = "ContextAction:Copy ";
            private static final long serialVersionUID = -9191567200213385853L;


            public void actionPerformed(final ActionEvent event)
                {
                if (tzfui.getTimeZonesUI() != null)
                    {

                    }
                else
                    {
                    UIManager.getLookAndFeel().provideErrorFeedback((Component) tzfui);
                    }
                }
            };

        tzfui.getCopyButton().setAction(actionCopy);
        tzfui.getCopyButton().setToolTipText((String) actionCopy.getValue(Action.SHORT_DESCRIPTION));
        tzfui.getCopyButton().setEnabled(true);


        //-------------------------------------------------------------------------------------
        // Paste

        actionPaste = new ContextAction(ACTION_NAME_PASTE,
                                        RegistryModelUtilities.getCommonIcon(FILENAME_ICON_PASTE),
                                        ACTION_TOOLTIP_PASTE,
                                        KeyEvent.VK_V,
                                        false,
                                        true)
            {
            final static String SOURCE = "ContextAction:Paste ";
            private static final long serialVersionUID = -8502728476222597390L;


            public void actionPerformed(final ActionEvent event)
                {
                if (tzfui.getTimeZonesUI() != null)
                    {

                    }
                else
                    {
                    LOGGER.error(SOURCE + "TimeZonesUI unexpectedly NULL");
                    }
                }
            };

        tzfui.getPasteButton().setAction(actionPaste);
        tzfui.getPasteButton().setToolTipText((String) actionPaste.getValue(Action.SHORT_DESCRIPTION));
        tzfui.getPasteButton().setEnabled(true);


        //-------------------------------------------------------------------------------------
        // Delete

        actionDelete = new ContextAction(ACTION_NAME_DELETE,
                                         RegistryModelUtilities.getCommonIcon(FILENAME_ICON_DELETE),
                                         ACTION_TOOLTIP_DELETE,
                                         KeyEvent.VK_D,
                                         false,
                                         true)
            {
            final static String SOURCE = "ContextAction:Delete ";
            private static final long serialVersionUID = -8607127328098604137L;


            public void actionPerformed(final ActionEvent event)
                {
                if (tzfui.getTimeZonesUI() != null)
                    {

                    }
                else
                    {
                    LOGGER.error(SOURCE + "TimeZonesUI unexpectedly NULL");
                    }
                }
            };

        tzfui.getDeleteButton().setAction(actionDelete);
        tzfui.getDeleteButton().setToolTipText((String) actionDelete.getValue(Action.SHORT_DESCRIPTION));
        tzfui.getDeleteButton().setEnabled(true);


        //-------------------------------------------------------------------------------------
        // Printing
        //-------------------------------------------------------------------------------------
        // Page Setup

        actionPageSetup = new ContextAction(ReportTablePlugin.PREFIX_PAGE_SETUP + MSG_TZ,
                                            RegistryModelUtilities.getCommonIcon(FILENAME_ICON_PAGE_SETUP),
                                            ReportTablePlugin.PREFIX_PAGE_SETUP + MSG_TZ,
                                            KeyEvent.VK_S,
                                            false,
                                            true)
            {
            final static String SOURCE = "ContextAction:PageSetup ";
            private static final long serialVersionUID = 6802400471966299436L;


            public void actionPerformed(final ActionEvent event)
                {
                if (tzfui.getTimeZonesUI() != null)
                    {
                    final PrinterJob printerJob;
                    final PageFormat pageFormat;

                    printerJob = PrinterJob.getPrinterJob();
                    pageFormat = printerJob.pageDialog(tzfui.getTimeZonesUI().getPageFormat());

                    if (pageFormat != null)
                        {
                        tzfui.getTimeZonesUI().setPageFormat(pageFormat);
                        }
                    }
                else
                    {
                    LOGGER.error(SOURCE + "TimeZonesUI unexpectedly NULL");
                    }
                }
            };

        tzfui.getPageSetupButton().setAction(actionPageSetup);
        tzfui.getPageSetupButton().setToolTipText((String) actionPageSetup.getValue(Action.SHORT_DESCRIPTION));
        tzfui.getPageSetupButton().setEnabled(true);

        //-------------------------------------------------------------------------------------
        // Print

        actionPrint = new ContextAction(ReportTablePlugin.PREFIX_PRINT + MSG_TZ,
                                        RegistryModelUtilities.getCommonIcon(FILENAME_ICON_PRINT),
                                        ReportTablePlugin.PREFIX_PRINT + MSG_TZ,
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
                                    MSG_TZ_PRINTED,
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
                                                  ReportTablePlugin.PREFIX_PRINT + MSG_TZ,
                                                  JOptionPane.WARNING_MESSAGE,
                                                  RegistryModelUtilities.getCommonIcon(FILENAME_ICON_DIALOG_PRINT));
                    boolSuccess = false;
                    }
                else
                    {
                    final PrinterJob printerJob;

                    printerJob = PrinterJob.getPrinterJob();

                    if ((tzfui.getTimeZonesUI() != null)
                        && (printerJob.printDialog()))
                        {
                        final PageFormat pageFormat;

                        pageFormat = tzfui.getTimeZonesUI().getPageFormat();

                        if (pageFormat != null)
                            {
//                            final Paper paper;
//                            final double dblMargin;
//
//                            paper = pageFormat.getPaper();
//                            dblMargin = PRINT_MARGIN;
//
//                            paper.setImageableArea(dblMargin,
//                                                   dblMargin,
//                                                   paper.getWidth() - dblMargin * 2,
//                                                   paper.getHeight() - dblMargin * 2);

                            // The TimeZonesUI is Printable
                            // ToDo Header & Footer MessageFormats
//                            printerJob.setPrintable(tfui.getTimeZonesUI().getHe xEditor().getTable().getPrintable(JTable.PrintMode.FIT_WIDTH,
//                                                                                                                  null,
//                                                                                                                  null),
//                                                    pageFormat);

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

        tzfui.getPrintButton().setAction(actionPrint);
        tzfui.getPrintButton().setToolTipText((String) actionPrint.getValue(Action.SHORT_DESCRIPTION));
        tzfui.getPrintButton().setEnabled(true);

        //-------------------------------------------------------------------------------------
        // ClearAll

        actionClearAll = new ContextAction(ACTION_NAME_CLEAR_ALL,
                                           RegistryModelUtilities.getCommonIcon(FILENAME_ICON_REMOVE),
                                           ACTION_TOOLTIP_CLEAR_ALL,
                                           KeyEvent.VK_A,
                                           false,
                                           true)
            {
            final static String SOURCE = "ContextAction:ClearAll ";
            private static final long serialVersionUID = -598953341739066982L;


            public void actionPerformed(final ActionEvent event)
                {
                if (tzfui.getTimeZonesUI() != null)
                    {

                    }
                else
                    {
                    LOGGER.error(SOURCE + "TimeZonesUI unexpectedly NULL");
                    }
                }
            };

        tzfui.getClearAllButton().setAction(actionClearAll);
        tzfui.getClearAllButton().setToolTipText((String) actionClearAll.getValue(Action.SHORT_DESCRIPTION));
        tzfui.getClearAllButton().setEnabled(true);


        //-------------------------------------------------------------------------------------
        // Put it all together

        toolbar.removeAll();

        toolbar.addSeparator(DIM_TOOLBAR_SEPARATOR_BUTTON);
        toolbar.add(labelName);
        toolbar.addSeparator(DIM_TOOLBAR_SEPARATOR);

        toolbar.add(Box.createHorizontalGlue());

        toolbar.add(tzfui.getOpenFileButton());
        toolbar.addSeparator(DIM_TOOLBAR_SEPARATOR);

        toolbar.add(tzfui.getSaveAsFileButton());
        toolbar.addSeparator(DIM_TOOLBAR_SEPARATOR);

        toolbar.add(tzfui.getCutButton());
        toolbar.addSeparator(DIM_TOOLBAR_SEPARATOR);

        toolbar.add(tzfui.getCopyButton());
        toolbar.addSeparator(DIM_TOOLBAR_SEPARATOR);

        toolbar.add(tzfui.getPasteButton());
        toolbar.addSeparator(DIM_TOOLBAR_SEPARATOR);

        toolbar.add(tzfui.getDeleteButton());
        toolbar.addSeparator(DIM_TOOLBAR_SEPARATOR);

        toolbar.add(tzfui.getPageSetupButton());
        toolbar.addSeparator(DIM_TOOLBAR_SEPARATOR);

        toolbar.add(tzfui.getPrintButton());
        toolbar.addSeparator(DIM_TOOLBAR_SEPARATOR);

        toolbar.add(tzfui.getClearAllButton());
        toolbar.addSeparator(DIM_TOOLBAR_SEPARATOR);

        NavigationUtilities.updateComponentTreeUI(toolbar);
        }


    /***********************************************************************************************
     * Construct a TimeZonesFrameUIComponent.
     *
     * @param hostinstrument
     * @param instrumentxml
     * @param hostui
     * @param task
     * @param font
     * @param colour
     * @param resourcekey
     */

    public TimeZonesFrameUIComponent(final ObservatoryInstrumentInterface hostinstrument,
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
        this.uiComponent = null;
        this.toolBar = null;

        this.buttonOpenFile = null;
        this.buttonSaveAsFile = null;
        this.buttonCut = null;
        this.buttonCopy = null;
        this.buttonPaste = null;
        this.buttonDelete = null;
        this.buttonPageSetup = null;
        this.buttonPrint = null;
        this.buttonClearAll = null;

        // This sets the debug state for all of the TimeZonesUI
        setDebug(false);
        }


    /**********************************************************************************************/
    /* UI State                                                                                   */
    /***********************************************************************************************
     * Initialise this UIComponent.
     */

    public void initialiseUI()
        {
        final String SOURCE = "TimeZonesFrameUIComponent.initialiseUI() ";

        // Do NOT use super.initialiseUI() in this context!

        LOGGER.debug(isDebug(), SOURCE);

        // Colours
        setBackground(DEFAULT_COLOUR_CANVAS.getColor());

        // Create the TimeZonesUIComponent and initialise it
        // Do this first to get the colours and fonts
        // This is the only creation of TimeZonesUIComponent
        setTimeZonesUI(new TimeZonesUIComponent(getHostTask(),
                                                getObservatoryUI(),
                                                getHostInstrument(),
                                                getFontData(),
                                                getColourData(),
                                                getResourceKey(),
                                                isDebug()));
        getTimeZonesUI().initialiseUI();

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
        add((Component) getTimeZonesUI(), BorderLayout.CENTER);
        }


    /***********************************************************************************************
     * Run this UIComponent.
     */

    public void runUI()
        {
        final String SOURCE = "TimeZonesFrameUIComponent.runUI() ";

        LOGGER.debug(isDebug(), SOURCE);

        super.runUI();

        if (getTimeZonesUI() != null)
            {
            UIComponentHelper.runComponentAndTransferActions((Component) getTimeZonesUI(), this);
            }
        }


    /***********************************************************************************************
     * Stop this UIComponent.
     */

    public void stopUI()
        {
        final String SOURCE = "TimeZonesFrameUIComponent.stopUI() ";

        LOGGER.debug(isDebug(), SOURCE);

        super.stopUI();

        if (getTimeZonesUI() != null)
            {
            getTimeZonesUI().stopUI();
            }
        }


    /***********************************************************************************************
     * Dispose of all components of this UIComponent.
     */

    public void disposeUI()
        {
        final String SOURCE = "TimeZonesFrameUIComponent.disposeUI() ";

        LOGGER.debug(isDebug(), SOURCE);

        stopUI();

        if (getToolBar() != null)
            {
            getToolBar().removeAll();
            setToolBar(null);
            }

        if (getTimeZonesUI() != null)
            {
            getTimeZonesUI().disposeUI();
            setTimeZonesUI(null);
            }

        super.disposeUI();
        }


    /**********************************************************************************************/
    /* UI                                                                                         */
    /***********************************************************************************************
     * Get the TimeZones UI.
     *
     * @return TimeZonesUIComponentInterface
     */

    public TimeZonesUIComponentInterface getTimeZonesUI()
        {
        return (this.uiComponent);
        }


    /***********************************************************************************************
     * Set the TimeZones UI.
     *
     * @param timezonesui
     */

    private void setTimeZonesUI(final TimeZonesUIComponentInterface timezonesui)
        {
        this.uiComponent = timezonesui;
        }


    /***********************************************************************************************
     * Get the TimeZonesUIComponentInterface JToolBar.
     *
     * @return JToolBar
     */

    private JToolBar getToolBar()
        {
        return (this.toolBar);
        }


    /***********************************************************************************************
     * Set the TimeZonesUIComponentInterface JToolBar.
     *
     * @param toolbar
     */

    private void setToolBar(final JToolBar toolbar)
        {
        this.toolBar = toolbar;
        }


    /***********************************************************************************************
     * Get the OpenFile button.
     *
     * @return JButton
     */

    public JButton getOpenFileButton()
        {
        return (this.buttonOpenFile);
        }


    /***********************************************************************************************
     * Set the OpenFile button.
     *
     * @param button
     */

    public void setOpenFileButton(final JButton button)
        {
        this.buttonOpenFile = button;
        }


    /***********************************************************************************************
     * Get the SaveAsFile button.
     *
     * @return JButton
     */

    public JButton getSaveAsFileButton()
        {
        return (this.buttonSaveAsFile);
        }


    /***********************************************************************************************
     * Set the SaveAsFile button.
     *
     * @param button
     */

    public void setSaveAsFileButton(final JButton button)
        {
        this.buttonSaveAsFile = button;
        }


    /***********************************************************************************************
     * Get the Cut button.
     *
     * @return JButton
     */

    public JButton getCutButton()
        {
        return (this.buttonCut);
        }


    /***********************************************************************************************
     * Set the Cut button.
     *
     * @param button
     */

    public void setCutButton(final JButton button)
        {
        this.buttonCut = button;
        }


    /***********************************************************************************************
     * Get the Copy button.
     *
     * @return JButton
     */

    public JButton getCopyButton()
        {
        return (this.buttonCopy);
        }


    /***********************************************************************************************
     * Set the Copy button.
     *
     * @param button
     */

    public void setCopyButton(final JButton button)
        {
        this.buttonCopy = button;
        }


    /***********************************************************************************************
     * Get the Paste button.
     *
     * @return JButton
     */

    public JButton getPasteButton()
        {
        return (this.buttonPaste);
        }


    /***********************************************************************************************
     * Set the Copy button.
     Paste
     * @param button
     */

    public void setPasteButton(final JButton button)
        {
        this.buttonPaste = button;
        }


    /***********************************************************************************************
     * Get the Delete button.
     *
     * @return JButton
     */

    public JButton getDeleteButton()
        {
        return (this.buttonDelete);
        }


    /***********************************************************************************************
     * Set the Delete button.
     *
     * @param button
     */

    public void setDeleteButton(final JButton button)
        {
        this.buttonDelete = button;
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
     * Get the ClearAll button.
     *
     * @return JButton
     */

    public JButton getClearAllButton()
        {
        return (this.buttonClearAll);
        }


    /***********************************************************************************************
     * Set the ClearAll button.
     *
     * @param button
     */

    public void setClearAllButton(final JButton button)
        {
        this.buttonClearAll = button;
        }
    }
