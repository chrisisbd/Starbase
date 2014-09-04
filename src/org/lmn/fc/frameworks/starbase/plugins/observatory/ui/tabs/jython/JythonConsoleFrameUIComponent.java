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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.jython;


import org.lmn.fc.common.actions.ContextAction;
import org.lmn.fc.common.utilities.misc.Utilities;
import org.lmn.fc.common.utilities.threads.SwingWorker;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryUIInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.InstrumentUIComponentDecorator;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.JythonConsoleFrameUIComponentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.JythonConsoleInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.JythonConsoleUIComponentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.JythonEditorFrameUIComponentInterface;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.DataTypeDictionary;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.model.registry.NavigationUtilities;
import org.lmn.fc.model.registry.RegistryModelUtilities;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;
import org.lmn.fc.ui.UIComponentPlugin;
import org.lmn.fc.ui.choosers.ChooserHelper;
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
import java.io.FileOutputStream;
import java.io.IOException;


/***************************************************************************************************
 * JythonConsoleFrameUIComponent.
 */

public final class JythonConsoleFrameUIComponent extends InstrumentUIComponentDecorator
                                                 implements JythonConsoleFrameUIComponentInterface
    {
    private static final long serialVersionUID = -7695306074475235601L;

    // UI
    private UIComponentPlugin uiJythonEditor;
    private JythonConsoleUIComponentInterface uiJythonConsole;
    private JToolBar toolbarJythonCommands;
    private JButton buttonExecuteScript;
    private JButton buttonExecuteFile;
    private JButton buttonExport;
    private JButton buttonPageSetup;
    private JButton buttonPrint;
    private JButton buttonReset;


    /***********************************************************************************************
     * Initialise the Commands Toolbar.
     *
     * @param toolbar
     * @param obsinstrument
     * @param jcfui
     * @param fontdata
     * @param colourforeground
     * @param colourbackground
     * @param debug
     */

    private static void initialiseCommandsToolbar(final JToolBar toolbar,
                                                  final ObservatoryInstrumentInterface obsinstrument,
                                                  final JythonConsoleFrameUIComponentInterface jcfui,
                                                  final FontInterface fontdata,
                                                  final ColourInterface colourforeground,
                                                  final ColourInterface colourbackground,
                                                  final boolean debug)
        {
        final String SOURCE = "JythonConsoleFrameUIComponent.initialiseCommandsToolbar() ";
        final JLabel labelName;
        final ContextAction actionExecuteScript;
        final ContextAction actionExecuteFile;
        final ContextAction actionExport;
        final ContextAction actionPageSetup;
        final ContextAction actionPrint;
        final ContextAction actionReset;

        toolbar.setBackground(colourbackground.getColor());

        //-------------------------------------------------------------------------------------
        // Initialise the Labels

        labelName = new JLabel(JYTHON_CONSOLE_NAME,
                               RegistryModelUtilities.getCommonIcon(FILENAME_ICON_PYTHON),
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

        jcfui.setExecuteScriptButton(new JButton());
        jcfui.getExecuteScriptButton().setBorder(BORDER_BUTTON);

        // Ensure that no text appears next to the Icon...
        jcfui.getExecuteScriptButton().setHideActionText(true);

        jcfui.setExecuteFileButton(new JButton());
        jcfui.getExecuteFileButton().setBorder(BORDER_BUTTON);

        // Ensure that no text appears next to the Icon...
        jcfui.getExecuteFileButton().setHideActionText(true);

        jcfui.setExportButton(new JButton());
        jcfui.getExportButton().setBorder(BORDER_BUTTON);

        // Ensure that no text appears next to the Icon...
        jcfui.getExportButton().setHideActionText(true);

        jcfui.setPageSetupButton(new JButton());
        jcfui.getPageSetupButton().setBorder(BORDER_BUTTON);

        // Ensure that no text appears next to the Icon...
        jcfui.getPageSetupButton().setHideActionText(true);

        jcfui.setPrintButton(new JButton());
        jcfui.getPrintButton().setBorder(BORDER_BUTTON);

        // Ensure that no text appears next to the Icon...
        jcfui.getPrintButton().setHideActionText(true);

        jcfui.setResetButton(new JButton());
        jcfui.getResetButton().setBorder(BORDER_BUTTON);

        // Ensure that no text appears next to the Icon...
        jcfui.getResetButton().setHideActionText(true);

        //-------------------------------------------------------------------------------------
        // Execute Script

        actionExecuteScript = new ContextAction("Execute",
                                                RegistryModelUtilities.getCommonIcon(FILENAME_ICON_EXECUTE_JYTHON_SCRIPT),
                                                TOOLTIP_EXECUTE_JYTHON_SCRIPT,
                                                KeyEvent.VK_E,
                                                false,
                                                true)
            {
            final static String SOURCE = "ContextAction:ExecuteScript ";
            private static final long serialVersionUID = 6802400471966299436L;


            public void actionPerformed(final ActionEvent event)
                {
                if ((jcfui.getJythonConsoleUI() != null)
                    && (jcfui.getJythonConsoleUI().getJythonConsole() != null)
                    && (jcfui.getJythonConsoleUI().getJythonConsole().getInterpreter() != null)
                    && (jcfui.getJythonEditorFrame() instanceof JythonEditorFrameUIComponentInterface)
                    && (((JythonEditorFrameUIComponentInterface)jcfui.getJythonEditorFrame()).getJythonEditorUI() != null)
                    && (((JythonEditorFrameUIComponentInterface)jcfui.getJythonEditorFrame()).getJythonEditorUI().getTextArea() != null))
                    {
                    // Make sure the interpreter is not already busy
                    if (!jcfui.getJythonConsoleUI().getJythonConsole().isRunning())
                        {
                        try
                            {
                            jcfui.getJythonConsoleUI().getJythonConsole().setText(jcfui.getJythonConsoleUI().getJythonConsole().getText()
                                                                                  + JythonConsoleInterface.MSG_EXECUTE_SCRIPT);
                            jcfui.getJythonConsoleUI().getJythonConsole().getInterpreter().exec((((JythonEditorFrameUIComponentInterface)jcfui.getJythonEditorFrame()).getJythonEditorUI().getTextArea().getText()));
                            jcfui.getJythonConsoleUI().getJythonConsole().setText(jcfui.getJythonConsoleUI().getJythonConsole().getText()
                                                                                  + JythonConsoleInterface.MSG_SIGN_ON);
                            }

                        catch (final Exception exception)
                            {
                            // ToDo add to event log and console?
                            LOGGER.error(SOURCE + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR);
                            }
                        }
                    else
                        {
                        final String [] messageRunning =
                            {
                            MSG_RUNNING_0,
                            MSG_RUNNING_1
                            };

                        Toolkit.getDefaultToolkit().beep();
                        JOptionPane.showMessageDialog(null,
                                                      messageRunning,
                                                      TITLE_DIALOG_JYTHON,
                                                      JOptionPane.WARNING_MESSAGE);
                        }
                    }
                }
            };

        jcfui.getExecuteScriptButton().setAction(actionExecuteScript);
        jcfui.getExecuteScriptButton().setToolTipText((String) actionExecuteScript.getValue(Action.SHORT_DESCRIPTION));
        jcfui.getExecuteScriptButton().setEnabled(true);

        //-------------------------------------------------------------------------------------
        // Execute File

        actionExecuteFile = new ContextAction("Execute",
                                              RegistryModelUtilities.getCommonIcon(FILENAME_ICON_EXECUTE_JYTHON_FILE),
                                              TOOLTIP_EXECUTE_JYTHON_FILE,
                                              KeyEvent.VK_X,
                                              false,
                                              true)
            {
            final static String SOURCE = "ContextAction:ExecuteFile ";
            private static final long serialVersionUID = 6802400471966299436L;


            public void actionPerformed(final ActionEvent event)
                {
                if ((jcfui.getJythonConsoleUI() != null)
                    && (jcfui.getJythonConsoleUI().getJythonConsole() != null)
                    && (jcfui.getJythonConsoleUI().getJythonConsole().getInterpreter() != null))
                    {
                    // Make sure the interpreter is not already busy
                    if (!jcfui.getJythonConsoleUI().getJythonConsole().isRunning())
                        {
                        final ChooserInterface chooser;

                        // Instantiate and show the appropriate Chooser with the current Value
                        chooser = ChooserHelper.instantiateChooser(DataTypeDictionary.FILE_NAME.getChooserClassname(),
                                                                   obsinstrument,
                                                                   fontdata,
                                                                   colourforeground,
                                                                   "./*.py");
                        if (chooser != null)
                            {
                            // Go modal...
                            chooser.showChooser((Component)jcfui.getJythonConsoleUI());

                            try
                                {
                                jcfui.getJythonConsoleUI().getJythonConsole().setText(jcfui.getJythonConsoleUI().getJythonConsole().getText()
                                                                                      + JythonConsoleInterface.MSG_EXECUTE_FILE);
                                // TODO put in stream, do on a thread?
                                // We only need the filename, so need to use a JFileChooser directly
                                jcfui.getJythonConsoleUI().getJythonConsole().getInterpreter().execfile(chooser.getValue());
                                jcfui.getJythonConsoleUI().getJythonConsole().setText(jcfui.getJythonConsoleUI().getJythonConsole().getText()
                                                                                      + JythonConsoleInterface.MSG_SIGN_ON);
                                }

                            catch (Exception exception)
                                {
                                // ToDo add to event log and console?
                                LOGGER.error(SOURCE + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR);
                                }
                            }
                        }
                    else
                        {
                        final String [] messageRunning =
                            {
                            MSG_RUNNING_0,
                            MSG_RUNNING_1
                            };

                        Toolkit.getDefaultToolkit().beep();
                        JOptionPane.showMessageDialog(null,
                                                      messageRunning,
                                                      TITLE_DIALOG_JYTHON,
                                                      JOptionPane.WARNING_MESSAGE);
                        }
                    }
                }
            };

        jcfui.getExecuteFileButton().setAction(actionExecuteFile);
        jcfui.getExecuteFileButton().setToolTipText((String) actionExecuteFile.getValue(Action.SHORT_DESCRIPTION));
        jcfui.getExecuteFileButton().setEnabled(true);

        //-------------------------------------------------------------------------------------
        // Export

        actionExport = new ContextAction("Export",
                                         RegistryModelUtilities.getCommonIcon(FILENAME_ICON_EXPORT_JYTHON),
                                         TOOLTIP_EXPORT_JYTHON,
                                         KeyEvent.VK_E,
                                         false,
                                         true)
            {
            final static String SOURCE = "ContextAction:Export ";
            private static final long serialVersionUID = 2289353484857897775L;


            public void actionPerformed(final ActionEvent event)
                {
                if ((jcfui.getJythonConsoleUI() != null)
                    && (jcfui.getJythonConsoleUI().getJythonConsole() != null))
                    {
                    final JFileChooser chooser;
                    final int intStatus;

                    chooser = new JFileChooser();
                    chooser.setDialogTitle(TOOLTIP_EXPORT_JYTHON);
                    chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                    chooser.setForeground(colourforeground.getColor());

                    chooser.setApproveButtonText(ChooserInterface.BUTTON_SELECT);
                    chooser.setApproveButtonToolTipText("Enter the filename");

                    Utilities.setContainerHierarchyFonts(chooser.getComponents(),
                                                         DEFAULT_FONT);

                    Utilities.setContainerHierarchyColours(chooser.getComponents(),
                                                           colourforeground,
                                                           null);

                    intStatus = chooser.showSaveDialog((Component)jcfui.getJythonConsoleUI());

                    if (intStatus == JFileChooser.APPROVE_OPTION)
                        {
                        final File file;

                        file = chooser.getSelectedFile();

                        if (file != null)
                            {
                            try
                                {
                                final FileOutputStream outputStream;

                                outputStream = new FileOutputStream(file);
                                outputStream.write(jcfui.getJythonConsoleUI().getJythonConsole().getText().getBytes());
                                outputStream.close();
                                }

                            catch (IOException exception)
                                {
                                LOGGER.error(SOURCE + "File write error [exception=" + exception.getMessage() + "]");
                                }
                            }
                        else
                            {
                            LOGGER.error(SOURCE + "File unexpectedly NULL");
                            }
                        }
                    }
                else
                    {
                    LOGGER.error(SOURCE + "JythonConsole unexpectedly NULL");
                    }
                }
            };

        jcfui.getExportButton().setAction(actionExport);
        jcfui.getExportButton().setToolTipText((String) actionExport.getValue(Action.SHORT_DESCRIPTION));
        jcfui.getExportButton().setEnabled(true);

        //-------------------------------------------------------------------------------------
        // Page Setup

        actionPageSetup = new ContextAction(ReportTablePlugin.PREFIX_PAGE_SETUP + MSG_JYTHON_CONSOLE,
                                            RegistryModelUtilities.getCommonIcon(FILENAME_ICON_PAGE_SETUP),
                                            ReportTablePlugin.PREFIX_PAGE_SETUP + MSG_JYTHON_CONSOLE,
                                            KeyEvent.VK_S,
                                            false,
                                            true)
            {
            final static String SOURCE = "ContextAction:PageSetup ";
            private static final long serialVersionUID = 6802400471966299436L;


            public void actionPerformed(final ActionEvent event)
                {
                if ((jcfui.getJythonConsoleUI() != null)
                    && (jcfui.getJythonConsoleUI().getJythonConsole() != null))
                    {
                    final PrinterJob printerJob;
                    final PageFormat pageFormat;

                    printerJob = PrinterJob.getPrinterJob();
                    pageFormat = printerJob.pageDialog(jcfui.getJythonConsoleUI().getPageFormat());

                    if (pageFormat != null)
                        {
                        jcfui.getJythonConsoleUI().setPageFormat(pageFormat);
                        }
                    }
                }
            };

        jcfui.getPageSetupButton().setAction(actionPageSetup);
        jcfui.getPageSetupButton().setToolTipText((String) actionPageSetup.getValue(Action.SHORT_DESCRIPTION));
        jcfui.getPageSetupButton().setEnabled(true);

        //-------------------------------------------------------------------------------------
        // Print

        actionPrint = new ContextAction(ReportTablePlugin.PREFIX_PRINT + MSG_JYTHON_CONSOLE,
                                        RegistryModelUtilities.getCommonIcon(FILENAME_ICON_PRINT),
                                        ReportTablePlugin.PREFIX_PRINT + MSG_JYTHON_CONSOLE,
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
                                                  ReportTablePlugin.PREFIX_PRINT + MSG_JYTHON_CONSOLE,
                                                  JOptionPane.WARNING_MESSAGE,
                                                  RegistryModelUtilities.getCommonIcon(FILENAME_ICON_DIALOG_PRINT));
                    boolSuccess = false;
                    }
                else
                    {
                    final PrinterJob printerJob;

                    printerJob = PrinterJob.getPrinterJob();

                    if ((jcfui.getJythonConsoleUI() != null)
                        && (jcfui.getJythonConsoleUI().getJythonConsole() != null)
                        && (printerJob.printDialog()))
                        {
                        final PageFormat pageFormat;

                        pageFormat = jcfui.getJythonConsoleUI().getPageFormat();

                        if (pageFormat != null)
                            {
                            // The JythonConsole is Printable
                            // ToDo Header & Footer MessageFormats
                            printerJob.setPrintable(jcfui.getJythonConsoleUI().getJythonConsole().getPrintable(null, null),
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

        jcfui.getPrintButton().setAction(actionPrint);
        jcfui.getPrintButton().setToolTipText((String) actionPrint.getValue(Action.SHORT_DESCRIPTION));
        jcfui.getPrintButton().setEnabled(true);

        //-------------------------------------------------------------------------------------
        // Reset

        actionReset = new ContextAction("Reset",
                                        RegistryModelUtilities.getCommonIcon(FILENAME_ICON_REMOVE),
                                        TOOLTIP_RESET_JYTHON,
                                        KeyEvent.VK_R,
                                        false,
                                        true)
            {
            final static String SOURCE = "ContextAction:Reset ";
            private static final long serialVersionUID = 6802400471966299436L;


            public void actionPerformed(final ActionEvent event)
                {
                if ((jcfui.getJythonConsoleUI() != null)
                    && (jcfui.getJythonConsoleUI().getJythonConsole() != null)
                    && (jcfui.getJythonConsoleUI().getJythonConsole().getInterpreter() != null))
                    {
                    jcfui.getJythonConsoleUI().getJythonConsole().getInterpreter().resetbuffer();
                    jcfui.getJythonConsoleUI().getJythonConsole().setText(JythonConsoleInterface.MSG_SIGN_ON);
                    }
                }
            };

        jcfui.getResetButton().setAction(actionReset);
        jcfui.getResetButton().setToolTipText((String) actionReset.getValue(Action.SHORT_DESCRIPTION));
        jcfui.getResetButton().setEnabled(true);

        //-------------------------------------------------------------------------------------
        // Put it all together

        toolbar.removeAll();

        toolbar.addSeparator(DIM_TOOLBAR_SEPARATOR_BUTTON);
        toolbar.add(labelName);
        toolbar.addSeparator(DIM_TOOLBAR_SEPARATOR);

        toolbar.add(Box.createHorizontalGlue());

        toolbar.add(jcfui.getExecuteScriptButton());
        toolbar.addSeparator(DIM_TOOLBAR_SEPARATOR);

        toolbar.add(jcfui.getExecuteFileButton());
        toolbar.addSeparator(DIM_TOOLBAR_SEPARATOR);

        toolbar.add(jcfui.getExportButton());
        toolbar.addSeparator(DIM_TOOLBAR_SEPARATOR);

        toolbar.add(jcfui.getPageSetupButton());
        toolbar.addSeparator(DIM_TOOLBAR_SEPARATOR);

        toolbar.add(jcfui.getPrintButton());
        toolbar.addSeparator(DIM_TOOLBAR_SEPARATOR);

        toolbar.add(jcfui.getResetButton());
        toolbar.addSeparator(DIM_TOOLBAR_SEPARATOR);

        NavigationUtilities.updateComponentTreeUI(toolbar);
        }


    /***********************************************************************************************
     * Construct a JythonConsoleFrameUIComponent.
     *
     * @param hostinstrument
     * @param instrumentxml
     * @param hostui
     * @param task
     * @param font
     * @param colour
     * @param resourcekey
     */

    public JythonConsoleFrameUIComponent(final ObservatoryInstrumentInterface hostinstrument,
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
        this.uiJythonEditor = null;
        this.uiJythonConsole = null;
        this.toolbarJythonCommands = null;
        this.buttonExecuteScript = null;
        this.buttonExecuteFile = null;
        this.buttonExport = null;
        this.buttonPageSetup = null;
        this.buttonPrint = null;
        this.buttonReset = null;

        // This sets the debug state for all of the JythonConsole
        setDebug(false);
        }


    /**********************************************************************************************/
    /* UI State                                                                                   */
    /***********************************************************************************************
     * Initialise this UIComponent.
     */

    public void initialiseUI()
        {
        final String SOURCE = "JythonConsoleFrameUIComponent.initialiseUI() ";

        LOGGER.debug(isDebug(), SOURCE);

        // Colours
        setBackground(DEFAULT_COLOUR_CANVAS.getColor());

        // Create the JythonConsoleUIComponent and initialise it
        // Do this first to get the colours and fonts
        // This is the only creation of JythonConsoleUIComponent
        setJythonConsoleUI(new JythonConsoleUIComponent(getHostTask(),
                                                        getObservatoryUI(),
                                                        getHostInstrument(),
                                                        getFontData(),
                                                        getColourData(),
                                                        getResourceKey(),
                                                        isDebug()));
        getJythonConsoleUI().initialiseUI();

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
        add((Component) getJythonConsoleUI(), BorderLayout.CENTER);
        }


    /***********************************************************************************************
     * Run this UIComponent.
     */

    public void runUI()
        {
        final String SOURCE = "JythonConsoleFrameUIComponent.runUI() ";

        LOGGER.debug(isDebug(), SOURCE);

        super.runUI();

        if (getJythonConsoleUI() != null)
            {
            UIComponentHelper.runComponentAndTransferActions((Component) getJythonConsoleUI(), this);
            }
        }


    /***********************************************************************************************
     * Stop this UIComponent.
     */

    public void stopUI()
        {
        final String SOURCE = "JythonConsoleFrameUIComponent.stopUI() ";

        LOGGER.debug(isDebug(), SOURCE);

        super.stopUI();

        if (getJythonConsoleUI() != null)
            {
            getJythonConsoleUI().stopUI();
            }
        }


    /***********************************************************************************************
     * Dispose of all components of this UIComponent.
     */

    public void disposeUI()
        {
        final String SOURCE = "JythonConsoleFrameUIComponent.disposeUI() ";

        LOGGER.debug(isDebug(), SOURCE);

        stopUI();

        if (getToolBar() != null)
            {
            getToolBar().removeAll();
            setToolBar(null);
            }

        if (getJythonConsoleUI() != null)
            {
            getJythonConsoleUI().disposeUI();
            setJythonConsoleUI(null);
            }

        super.disposeUI();
        }


    /**********************************************************************************************/
    /* UI                                                                                         */
    /***********************************************************************************************
     * Get the associated JythonEditorFrame.
     *
     * @return UIComponentPlugin
     */

    public UIComponentPlugin getJythonEditorFrame()
        {
        return (this.uiJythonEditor);
        }


    /***********************************************************************************************
     * Set the associated JythonEditorFrame.
     *
     * @param jythoneditor
     */

    public void setJythonEditorFrame(final UIComponentPlugin jythoneditor)
        {
        this.uiJythonEditor = jythoneditor;
        }


    /***********************************************************************************************
     * Get the JythonConsole UI.
     *
     * @return JythonConsoleUIComponentInterface
     */

    public JythonConsoleUIComponentInterface getJythonConsoleUI()
        {
        return (this.uiJythonConsole);
        }


    /***********************************************************************************************
     * Set the JythonConsole UI.
     *
     * @param jythonconsoleui
     */

    private void setJythonConsoleUI(final JythonConsoleUIComponentInterface jythonconsoleui)
        {
        this.uiJythonConsole = jythonconsoleui;
        }


    /***********************************************************************************************
     * Get the JythonConsole JToolBar.
     *
     * @return JToolBar
     */

    private JToolBar getToolBar()
        {
        return (this.toolbarJythonCommands);
        }


    /***********************************************************************************************
     * Set the JythonConsole JToolBar.
     *
     * @param toolbar
     */

    private void setToolBar(final JToolBar toolbar)
        {
        this.toolbarJythonCommands = toolbar;
        }


    /***********************************************************************************************
     * Get the Execute Script button.
     *
     * @return JButton
     */

    public JButton getExecuteScriptButton()
        {
        return (this.buttonExecuteScript);
        }


    /***********************************************************************************************
     * Set the Execute Script button.
     *
     * @param button
     */

    public void setExecuteScriptButton(final JButton button)
        {
        this.buttonExecuteScript = button;
        }


    /***********************************************************************************************
     * Get the Execute File button.
     *
     * @return JButton
     */

    public JButton getExecuteFileButton()
        {
        return (this.buttonExecuteFile);
        }


    /***********************************************************************************************
     * Set the Execute File button.
     *
     * @param button
     */

    public void setExecuteFileButton(final JButton button)
        {
        this.buttonExecuteFile = button;
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
     * Get the Reset button.
     *
     * @return JButton
     */

    public JButton getResetButton()
        {
        return (this.buttonReset);
        }


    /***********************************************************************************************
     * Set the Reset button.
     *
     * @param button
     */

    public void setResetButton(final JButton button)
        {
        this.buttonReset = button;
        }
    }
