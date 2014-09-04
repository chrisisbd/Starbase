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


import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.lmn.fc.common.actions.ContextAction;
import org.lmn.fc.common.utilities.files.FileUtilities;
import org.lmn.fc.common.utilities.misc.Utilities;
import org.lmn.fc.common.utilities.threads.SwingWorker;
import org.lmn.fc.common.utilities.ui.AlignedListCellRenderer;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryUIInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.InstrumentUIComponentDecorator;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.JythonEditorFrameUIComponentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.JythonEditorUIComponentInterface;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.model.registry.NavigationUtilities;
import org.lmn.fc.model.registry.RegistryModelUtilities;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;
import org.lmn.fc.ui.UIComponentPlugin;
import org.lmn.fc.ui.choosers.ChooserInterface;
import org.lmn.fc.ui.components.UIComponentHelper;
import org.lmn.fc.ui.reports.ReportTableHelper;
import org.lmn.fc.ui.reports.ReportTablePlugin;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.print.PageFormat;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


/***************************************************************************************************
 * JythonEditorFrameUIComponent.
 */

public final class JythonEditorFrameUIComponent extends InstrumentUIComponentDecorator
                                                implements JythonEditorFrameUIComponentInterface
    {
    private static final long serialVersionUID = -7695306074475235601L;

    // UI
    private UIComponentPlugin uiJythonConsole;
    private JythonEditorUIComponentInterface uiJythonEditor;
    private JToolBar toolbarEditorCommands;
    private final JComboBox comboSyntaxHighlight;
    private JButton buttonOpenFile;
    private JButton buttonSaveAs;
    private JButton buttonPageSetup;
    private JButton buttonPrint;
    private JButton buttonReset;


    /***********************************************************************************************
     * Initialise the Commands Toolbar.
     *
     * @param toolbar
     * @param obsinstrument
     * @param jefui
     * @param fontdata
     * @param colourforeground
     * @param colourbackground
     * @param debug
     */

    private static void initialiseCommandsToolbar(final JToolBar toolbar,
                                                  final ObservatoryInstrumentInterface obsinstrument,
                                                  final JythonEditorFrameUIComponentInterface jefui,
                                                  final FontInterface fontdata,
                                                  final ColourInterface colourforeground,
                                                  final ColourInterface colourbackground,
                                                  final boolean debug)
        {
        final String SOURCE = "JythonEditorFrameUIComponent.initialiseCommandsToolbar() ";
        final String TEXT_PREFIX = "text/";
        final JLabel labelName;
        final ActionListener choiceListener;
        final ContextAction actionOpenFile;
        final ContextAction actionSaveAs;
        final ContextAction actionPageSetup;
        final ContextAction actionPrint;
        final ContextAction actionReset;

        toolbar.setBackground(colourbackground.getColor());

        //-------------------------------------------------------------------------------------
        // Initialise the Label

        labelName = new JLabel(JYTHON_EDITOR_NAME,
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
        // Initialise the Syntax Highlight ComboBox

        jefui.getSyntaxHighlightCombo().setFont(fontdata.getFont());
        jefui.getSyntaxHighlightCombo().setForeground(colourforeground.getColor());
        jefui.getSyntaxHighlightCombo().setRenderer(new AlignedListCellRenderer(SwingConstants.LEFT,
                                                                                fontdata,
                                                                                colourforeground,
                                                                                DEFAULT_COLOUR_TAB_BACKGROUND));

        // Do NOT allow the combo box to take up all the remaining space!
        jefui.getSyntaxHighlightCombo().setPreferredSize(new Dimension(100, HEIGHT_TOOLBAR_ICON - 4));
        jefui.getSyntaxHighlightCombo().setMaximumSize(new Dimension(100, HEIGHT_TOOLBAR_ICON - 4));
        jefui.getSyntaxHighlightCombo().setAlignmentX(0);

        jefui.getSyntaxHighlightCombo().setToolTipText("Choose a language syntax");
        jefui.getSyntaxHighlightCombo().setEnabled(true);
        jefui.getSyntaxHighlightCombo().setEditable(false);

        // It would be helpful if SyntaxConstants were converted to an enum!
        jefui.getSyntaxHighlightCombo().addItem(SyntaxConstants.SYNTAX_STYLE_C.substring(TEXT_PREFIX.length()));
        jefui.getSyntaxHighlightCombo().addItem(SyntaxConstants.SYNTAX_STYLE_CPLUSPLUS.substring(TEXT_PREFIX.length()));
        jefui.getSyntaxHighlightCombo().addItem(SyntaxConstants.SYNTAX_STYLE_DTD.substring(TEXT_PREFIX.length()));
        jefui.getSyntaxHighlightCombo().addItem(SyntaxConstants.SYNTAX_STYLE_HTML.substring(TEXT_PREFIX.length()));
        jefui.getSyntaxHighlightCombo().addItem(SyntaxConstants.SYNTAX_STYLE_JAVA.substring(TEXT_PREFIX.length()));
        jefui.getSyntaxHighlightCombo().addItem(SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT.substring(TEXT_PREFIX.length()));
        jefui.getSyntaxHighlightCombo().addItem(SyntaxConstants.SYNTAX_STYLE_JSON.substring(TEXT_PREFIX.length()));
        jefui.getSyntaxHighlightCombo().addItem(SyntaxConstants.SYNTAX_STYLE_NONE.substring(TEXT_PREFIX.length()));
        jefui.getSyntaxHighlightCombo().addItem(SyntaxConstants.SYNTAX_STYLE_PROPERTIES_FILE.substring(TEXT_PREFIX.length()));
        jefui.getSyntaxHighlightCombo().addItem(SyntaxConstants.SYNTAX_STYLE_PYTHON.substring(TEXT_PREFIX.length()));
        jefui.getSyntaxHighlightCombo().addItem(SyntaxConstants.SYNTAX_STYLE_SQL.substring(TEXT_PREFIX.length()));
        jefui.getSyntaxHighlightCombo().addItem(SyntaxConstants.SYNTAX_STYLE_XML.substring(TEXT_PREFIX.length()));

        if (jefui.getSyntaxHighlightCombo().getItemCount() > 0)
            {
            // Force the initial selection to 'python'
            jefui.getSyntaxHighlightCombo().setSelectedItem(SyntaxConstants.SYNTAX_STYLE_PYTHON.substring(TEXT_PREFIX.length()));
            //setSelectedIndex(0);
            jefui.getSyntaxHighlightCombo().revalidate();
            }

        choiceListener = new ActionListener()
            {
            public void actionPerformed(final ActionEvent event)
                {
                if ((jefui.getJythonEditorUI() != null)
                    && (jefui.getJythonEditorUI().getTextArea() != null))
                    {
                    final String strSyntax;

                    strSyntax = TEXT_PREFIX + jefui.getSyntaxHighlightCombo().getSelectedItem();
                    jefui.getJythonEditorUI().getTextArea().setSyntaxEditingStyle(strSyntax);
                    jefui.getJythonEditorUI().getTextArea().revalidate();
                    }
                }
            };

        jefui.getSyntaxHighlightCombo().addActionListener(choiceListener);

        //-------------------------------------------------------------------------------------
        // Initialise the Buttons

        jefui.setOpenFileButton(new JButton());
        jefui.getOpenFileButton().setBorder(BORDER_BUTTON);

        // Ensure that no text appears next to the Icon...
        jefui.getOpenFileButton().setHideActionText(true);

        jefui.setSaveAsButton(new JButton());
        jefui.getSaveAsButton().setBorder(BORDER_BUTTON);

        // Ensure that no text appears next to the Icon...
        jefui.getSaveAsButton().setHideActionText(true);

        jefui.setPageSetupButton(new JButton());
        jefui.getPageSetupButton().setBorder(BORDER_BUTTON);

        // Ensure that no text appears next to the Icon...
        jefui.getPageSetupButton().setHideActionText(true);

        jefui.setPrintButton(new JButton());
        jefui.getPrintButton().setBorder(BORDER_BUTTON);

        // Ensure that no text appears next to the Icon...
        jefui.getPrintButton().setHideActionText(true);

        jefui.setResetButton(new JButton());
        jefui.getResetButton().setBorder(BORDER_BUTTON);

        // Ensure that no text appears next to the Icon...
        jefui.getResetButton().setHideActionText(true);

        //-------------------------------------------------------------------------------------
        // OpenFile

        actionOpenFile = new ContextAction("OpenFile",
                                            RegistryModelUtilities.getCommonIcon(FILENAME_ICON_OPEN_FILE),
                                            ACTION_TOOLTIP_OPEN_FILE,
                                            KeyEvent.VK_O,
                                            false,
                                            true)
            {
            final static String SOURCE = "ContextAction:OpenFile ";
            private static final long serialVersionUID = 6802400471966299436L;


            public void actionPerformed(final ActionEvent event)
                {
                if ((jefui.getJythonEditorUI() != null)
                    && (jefui.getJythonEditorUI().getTextArea() != null))
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

                    intStatus = chooser.showOpenDialog((Component)jefui.getJythonEditorUI());

                    if (intStatus == JFileChooser.APPROVE_OPTION)
                        {
                        try
                            {
                            final File file;
                            final StringBuffer buffer;

                            file = chooser.getSelectedFile();
                            buffer = FileUtilities.readFileAsString(file);
                            jefui.getJythonEditorUI().getTextArea().setText(buffer.toString());
                            }

                        catch (IOException exception)
                            {
                            // ToDo error to log and editor window?
                            LOGGER.error(SOURCE + "File read error [exception=" + exception.getMessage() + "]");
                            }
                        }
                    }
                else
                    {
                    LOGGER.error(SOURCE + "JythonEditor unexpectedly NULL");
                    }
                }
            };

        jefui.getOpenFileButton().setAction(actionOpenFile);
        jefui.getOpenFileButton().setToolTipText((String) actionOpenFile.getValue(Action.SHORT_DESCRIPTION));
        jefui.getOpenFileButton().setEnabled(true);

        //-------------------------------------------------------------------------------------
        // SaveAs

        actionSaveAs = new ContextAction("SaveAs",
                                         RegistryModelUtilities.getCommonIcon(FILENAME_ICON_SAVE_AS_FILE),
                                         ACTION_TOOLTIP_SAVE_AS_FILE,
                                         KeyEvent.VK_S,
                                         false,
                                         true)
            {
            final static String SOURCE = "ContextAction:SaveAs ";
            private static final long serialVersionUID = 2289353484857897775L;


            public void actionPerformed(final ActionEvent event)
                {
                if ((jefui.getJythonEditorUI() != null)
                    && (jefui.getJythonEditorUI().getTextArea() != null))
                    {
                    final JFileChooser chooser;
                    final int intStatus;

                    chooser = new JFileChooser();
                    chooser.setDialogTitle(ACTION_TOOLTIP_SAVE_AS_FILE);
                    chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                    chooser.setForeground(colourforeground.getColor());

                    chooser.setApproveButtonText(ChooserInterface.BUTTON_SELECT);
                    chooser.setApproveButtonToolTipText("Enter the filename");

                    Utilities.setContainerHierarchyFonts(chooser.getComponents(),
                                                         DEFAULT_FONT);

                    Utilities.setContainerHierarchyColours(chooser.getComponents(),
                                                           colourforeground,
                                                           null);

                    intStatus = chooser.showSaveDialog((Component)jefui.getJythonEditorUI());

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
                                outputStream.write(jefui.getJythonEditorUI().getTextArea().getText().getBytes());
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
                    LOGGER.error(SOURCE + "JythonEditor unexpectedly NULL");
                    }
                }
            };

        jefui.getSaveAsButton().setAction(actionSaveAs);
        jefui.getSaveAsButton().setToolTipText((String) actionSaveAs.getValue(Action.SHORT_DESCRIPTION));
        jefui.getSaveAsButton().setEnabled(true);

        //-------------------------------------------------------------------------------------
        // Page Setup

        actionPageSetup = new ContextAction(ReportTablePlugin.PREFIX_PAGE_SETUP + MSG_JYTHON_EDITOR,
                                            RegistryModelUtilities.getCommonIcon(FILENAME_ICON_PAGE_SETUP),
                                            ReportTablePlugin.PREFIX_PAGE_SETUP + MSG_JYTHON_EDITOR,
                                            KeyEvent.VK_S,
                                            false,
                                            true)
            {
            final static String SOURCE = "ContextAction:PageSetup ";
            private static final long serialVersionUID = 6802400471966299436L;


            public void actionPerformed(final ActionEvent event)
                {
                if ((jefui.getJythonEditorUI() != null)
                    && (jefui.getJythonEditorUI().getTextArea() != null))
                    {
                    final PrinterJob printerJob;
                    final PageFormat pageFormat;

                    printerJob = PrinterJob.getPrinterJob();
                    pageFormat = printerJob.pageDialog(jefui.getJythonEditorUI().getPageFormat());

                    if (pageFormat != null)
                        {
                        jefui.getJythonEditorUI().setPageFormat(pageFormat);
                        }
                    }
                }
            };

        jefui.getPageSetupButton().setAction(actionPageSetup);
        jefui.getPageSetupButton().setToolTipText((String) actionPageSetup.getValue(Action.SHORT_DESCRIPTION));
        jefui.getPageSetupButton().setEnabled(true);

        //-------------------------------------------------------------------------------------
        // Print

        actionPrint = new ContextAction(ReportTablePlugin.PREFIX_PRINT + MSG_JYTHON_EDITOR,
                                        RegistryModelUtilities.getCommonIcon(FILENAME_ICON_PRINT),
                                        ReportTablePlugin.PREFIX_PRINT + MSG_JYTHON_EDITOR,
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
                            MSG_EDITOR_PRINTED,
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
                                                  ReportTablePlugin.PREFIX_PRINT + MSG_JYTHON_EDITOR,
                                                  JOptionPane.WARNING_MESSAGE,
                                                  RegistryModelUtilities.getCommonIcon(FILENAME_ICON_DIALOG_PRINT));
                    boolSuccess = false;
                    }
                else
                    {
                    final PrinterJob printerJob;

                    printerJob = PrinterJob.getPrinterJob();

                    if ((jefui.getJythonEditorUI() != null)
                        && (jefui.getJythonEditorUI().getTextArea() != null)
                        && (printerJob.printDialog()))
                        {
                        final PageFormat pageFormat;

                        pageFormat = jefui.getJythonEditorUI().getPageFormat();

                        if (pageFormat != null)
                            {
                            // The JythonEditor is Printable
                            // ToDo Header & Footer MessageFormats
                            printerJob.setPrintable(jefui.getJythonEditorUI().getTextArea().getPrintable(null, null),
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

        jefui.getPrintButton().setAction(actionPrint);
        jefui.getPrintButton().setToolTipText((String) actionPrint.getValue(Action.SHORT_DESCRIPTION));
        jefui.getPrintButton().setEnabled(true);

        //-------------------------------------------------------------------------------------
        // Reset

        actionReset = new ContextAction("Reset",
                                        RegistryModelUtilities.getCommonIcon(FILENAME_ICON_RESET_JYTHON),
                                        TOOLTIP_RESET_EDITOR,
                                        KeyEvent.VK_R,
                                        false,
                                        true)
            {
            final static String SOURCE = "ContextAction:Reset ";
            private static final long serialVersionUID = 6802400471966299436L;


            public void actionPerformed(final ActionEvent event)
                {
                if ((jefui.getJythonEditorUI() != null)
                    && (jefui.getJythonEditorUI().getTextArea() != null))
                    {
                    jefui.getJythonEditorUI().getTextArea().setText(EMPTY_STRING);
                    }
                }
            };

        jefui.getResetButton().setAction(actionReset);
        jefui.getResetButton().setToolTipText((String) actionReset.getValue(Action.SHORT_DESCRIPTION));
        jefui.getResetButton().setEnabled(true);

        //-------------------------------------------------------------------------------------
        // Put it all together

        toolbar.removeAll();

        toolbar.addSeparator(DIM_TOOLBAR_SEPARATOR_BUTTON);
        toolbar.add(labelName);
        toolbar.addSeparator(DIM_TOOLBAR_SEPARATOR);

        toolbar.add(Box.createHorizontalGlue());

        toolbar.add(jefui.getSyntaxHighlightCombo());
        toolbar.addSeparator(DIM_TOOLBAR_SEPARATOR);

        toolbar.add(jefui.getOpenFileButton());
        toolbar.addSeparator(DIM_TOOLBAR_SEPARATOR);

        toolbar.add(jefui.getSaveAsButton());
        toolbar.addSeparator(DIM_TOOLBAR_SEPARATOR);

        toolbar.add(jefui.getPageSetupButton());
        toolbar.addSeparator(DIM_TOOLBAR_SEPARATOR);

        toolbar.add(jefui.getPrintButton());
        toolbar.addSeparator(DIM_TOOLBAR_SEPARATOR);

        toolbar.add(jefui.getResetButton());
        toolbar.addSeparator(DIM_TOOLBAR_SEPARATOR);

        NavigationUtilities.updateComponentTreeUI(toolbar);
        }


    /***********************************************************************************************
     * Construct a JythonEditorFrameUIComponent.
     *
     * @param hostinstrument
     * @param instrumentxml
     * @param hostui
     * @param task
     * @param font
     * @param colour
     * @param resourcekey
     */

    public JythonEditorFrameUIComponent(final ObservatoryInstrumentInterface hostinstrument,
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
        this.uiJythonConsole = null;
        this.uiJythonEditor = null;
        this.toolbarEditorCommands = null;
        this.comboSyntaxHighlight = new JComboBox();
        this.buttonOpenFile = null;
        this.buttonSaveAs = null;
        this.buttonPageSetup = null;
        this.buttonPrint = null;
        this.buttonReset = null;

        // This sets the debug state for all of the JythonEditor
        setDebug(false);
        }


    /**********************************************************************************************/
    /* UI State                                                                                   */
    /***********************************************************************************************
     * Initialise this UIComponent.
     */

    public void initialiseUI()
        {
        final String SOURCE = "JythonEditorFrameUIComponent.initialiseUI() ";

        LOGGER.debug(isDebug(), SOURCE);

        // Colours
        setBackground(DEFAULT_COLOUR_CANVAS.getColor());

        // Create the JythonEditorUIComponent and initialise it
        // Do this first to get the colours and fonts
        // This is the only creation of JythonEditorUIComponent
        setJythonEditorUI(new JythonEditorUIComponent(getHostTask(),
                                                      getObservatoryUI(),
                                                      getHostInstrument(),
                                                      getFontData(),
                                                      getColourData(),
                                                      getResourceKey(),
                                                      isDebug()));
        getJythonEditorUI().initialiseUI();

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
        add((Component) getJythonEditorUI(), BorderLayout.CENTER);
        }


    /***********************************************************************************************
     * Run this UIComponent.
     */

    public void runUI()
        {
        final String SOURCE = "JythonEditorFrameUIComponent.runUI() ";

        LOGGER.debug(isDebug(), SOURCE);

        super.runUI();

        if (getJythonEditorUI() != null)
            {
            UIComponentHelper.runComponentAndTransferActions((Component) getJythonEditorUI(), this);
            }
        }


    /***********************************************************************************************
     * Stop this UIComponent.
     */

    public void stopUI()
        {
        final String SOURCE = "JythonEditorFrameUIComponent.stopUI() ";

        LOGGER.debug(isDebug(), SOURCE);

        super.stopUI();

        if (getJythonEditorUI() != null)
            {
            getJythonEditorUI().stopUI();
            }
        }


    /***********************************************************************************************
     * Dispose of all components of this UIComponent.
     */

    public void disposeUI()
        {
        final String SOURCE = "JythonEditorFrameUIComponent.disposeUI() ";

        LOGGER.debug(isDebug(), SOURCE);

        stopUI();

        if (getToolBar() != null)
            {
            getToolBar().removeAll();
            setToolBar(null);
            }

        if (getJythonEditorUI() != null)
            {
            getJythonEditorUI().disposeUI();
            setJythonEditorUI(null);
            }

        super.disposeUI();
        }


    /**********************************************************************************************/
    /* UI                                                                                         */
    /***********************************************************************************************
     * Get the associated JythonConsoleFrame.
     *
     * @return UIComponentPlugin
     */

    public UIComponentPlugin getJythonConsoleFrame()
        {
        return (this.uiJythonConsole);
        }


    /***********************************************************************************************
     * Set the associated JythonConsoleFrame.
     *
     * @param jythonconsole
     */

    public void setJythonConsoleFrame(final UIComponentPlugin jythonconsole)
        {
        this.uiJythonConsole = jythonconsole;
        }


    /***********************************************************************************************
     * Get the JythonEditor UI.
     *
     * @return JythonEditorUIComponentInterface
     */

    public JythonEditorUIComponentInterface getJythonEditorUI()
        {
        return (this.uiJythonEditor);
        }


    /***********************************************************************************************
     * Set the JythonEditor UI.
     *
     * @param jythoneditorui
     */

    private void setJythonEditorUI(final JythonEditorUIComponentInterface jythoneditorui)
        {
        this.uiJythonEditor = jythoneditorui;
        }


    /***********************************************************************************************
     * Get the JythonEditor JToolBar.
     *
     * @return JToolBar
     */

    private JToolBar getToolBar()
        {
        return (this.toolbarEditorCommands);
        }


    /***********************************************************************************************
     * Set the JythonEditor JToolBar.
     *
     * @param toolbar
     */

    private void setToolBar(final JToolBar toolbar)
        {
        this.toolbarEditorCommands = toolbar;
        }


    /***********************************************************************************************
     * Get the Syntax Highlight combo box.
     *
     * @return JComboBox
     */

    public JComboBox getSyntaxHighlightCombo()
        {
        return (this.comboSyntaxHighlight);
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
     * Get the SaveAs button.
     *
     * @return JButton
     */

    public JButton getSaveAsButton()
        {
        return (this.buttonSaveAs);
        }


    /***********************************************************************************************
     * Set the SaveAs button.
     *
     * @param button
     */

    public void setSaveAsButton(final JButton button)
        {
        this.buttonSaveAs = button;
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
