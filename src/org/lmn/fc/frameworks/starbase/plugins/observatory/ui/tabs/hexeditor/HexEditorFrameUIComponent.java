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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.hexeditor;


import org.fife.ui.hex.ByteBuffer;
import org.fife.ui.hex.event.HexEditorEvent;
import org.fife.ui.hex.event.SelectionChangedEvent;
import org.fife.ui.hex.swing.HexTableModel;
import org.lmn.fc.common.actions.ContextAction;
import org.lmn.fc.common.utilities.misc.Utilities;
import org.lmn.fc.common.utilities.threads.SwingWorker;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryUIInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.InstrumentUIComponentDecorator;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.HexEditorFrameUIComponentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.HexEditorUIComponentInterface;
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
import java.io.*;


/***************************************************************************************************
 * HexEditorFrameUIComponent.
 */

public final class HexEditorFrameUIComponent extends InstrumentUIComponentDecorator
                                             implements HexEditorFrameUIComponentInterface
    {
    private static final long serialVersionUID = -7695306074475235601L;

    // UI
    private HexEditorUIComponentInterface uiHexEditor;
    private JToolBar toolbarHexEditor;

    private JButton buttonOpenFile;
    private JButton buttonSaveAsFile;
    private JButton buttonCut;
    private JButton buttonCopy;
    private JButton buttonPaste;
    private JButton buttonDelete;
    private JButton buttonUndo;
    private JButton buttonRedo;
    private JButton buttonPageSetup;
    private JButton buttonPrint;
    private JButton buttonClearAll;


    private JCheckBox colHeaderCB;
    private JCheckBox rowHeaderCB;
    private JCheckBox showGridCB;
    private JComboBox lafCombo;
    private JCheckBox altRowBGCB;
    private JCheckBox altColBGCB;
    private JCheckBox highlightAsciiSelCB;
    private JComboBox highlightAsciiSelCombo;
    private JCheckBox lowBytePaddingCB;
    private JTextField infoField;
    private JTextField selField;
    private JTextField sizeField;


    /***********************************************************************************************
     * Initialise the Commands Toolbar.
     *
     * @param toolbar
     * @param obsinstrument
     * @param hefui
     * @param fontdata
     * @param colourforeground
     * @param colourbackground
     * @param debug
     */

    private static void initialiseCommandsToolbar(final JToolBar toolbar,
                                                  final ObservatoryInstrumentInterface obsinstrument,
                                                  final HexEditorFrameUIComponentInterface hefui,
                                                  final FontInterface fontdata,
                                                  final ColourInterface colourforeground,
                                                  final ColourInterface colourbackground,
                                                  final boolean debug)
        {
        final String SOURCE = "HexEditorFrameUIComponent.initialiseCommandsToolbar() ";
        final JLabel labelName;
        final ContextAction actionOpenFile;
        final ContextAction actionSaveAsFile;
        final ContextAction actionCut;
        final ContextAction actionCopy;
        final ContextAction actionPaste;
        final ContextAction actionDelete;
        final ContextAction actionUndo;
        final ContextAction actionRedo;
        final ContextAction actionPageSetup;
        final ContextAction actionPrint;
        final ContextAction actionClearAll;

        toolbar.setBackground(colourbackground.getColor());

        //-------------------------------------------------------------------------------------
        // Initialise the Label

        labelName = new JLabel(HEX_EDITOR_NAME,
                               RegistryModelUtilities.getCommonIcon(FILENAME_ICON_HEX_EDITOR),
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

        hefui.setOpenFileButton(new JButton());
        hefui.getOpenFileButton().setBorder(BORDER_BUTTON);

        // Ensure that no text appears next to the Icon...
        hefui.getOpenFileButton().setHideActionText(true);

        hefui.setSaveAsFileButton(new JButton());
        hefui.getSaveAsFileButton().setBorder(BORDER_BUTTON);

        // Ensure that no text appears next to the Icon...
        hefui.getSaveAsFileButton().setHideActionText(true);

        hefui.setCutButton(new JButton());
        hefui.getCutButton().setBorder(BORDER_BUTTON);

        // Ensure that no text appears next to the Icon...
        hefui.getCutButton().setHideActionText(true);

        hefui.setCopyButton(new JButton());
        hefui.getCopyButton().setBorder(BORDER_BUTTON);

        // Ensure that no text appears next to the Icon...
        hefui.getCopyButton().setHideActionText(true);

        hefui.setPasteButton(new JButton());
        hefui.getPasteButton().setBorder(BORDER_BUTTON);

        // Ensure that no text appears next to the Icon...
        hefui.getPasteButton().setHideActionText(true);

        hefui.setDeleteButton(new JButton());
        hefui.getDeleteButton().setBorder(BORDER_BUTTON);

        // Ensure that no text appears next to the Icon...
        hefui.getDeleteButton().setHideActionText(true);

        hefui.setUndoButton(new JButton());
        hefui.getUndoButton().setBorder(BORDER_BUTTON);

        // Ensure that no text appears next to the Icon...
        hefui.getUndoButton().setHideActionText(true);

        hefui.setRedoButton(new JButton());
        hefui.getRedoButton().setBorder(BORDER_BUTTON);

        // Ensure that no text appears next to the Icon...
        hefui.getRedoButton().setHideActionText(true);

        hefui.setPageSetupButton(new JButton());
        hefui.getPageSetupButton().setBorder(BORDER_BUTTON);

        // Ensure that no text appears next to the Icon...
        hefui.getPageSetupButton().setHideActionText(true);

        hefui.setPrintButton(new JButton());
        hefui.getPrintButton().setBorder(BORDER_BUTTON);

        // Ensure that no text appears next to the Icon...
        hefui.getPrintButton().setHideActionText(true);

        hefui.setClearAllButton(new JButton());
        hefui.getClearAllButton().setBorder(BORDER_BUTTON);

        // Ensure that no text appears next to the Icon...
        hefui.getClearAllButton().setHideActionText(true);

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
                if ((hefui.getHexEditorUI() != null)
                    && (hefui.getHexEditorUI().getHexEditor() != null))
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

                    intStatus = chooser.showOpenDialog((Component)hefui.getHexEditorUI());

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
                                { // In a jar
                                hefui.getHexEditorUI().getHexEditor().open(new BufferedInputStream(inputStream));
                                }
                            else
                                {
                                if (file.isFile())
                                    {
                                    hefui.getHexEditorUI().getHexEditor().open(file.getAbsolutePath());
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
                    LOGGER.error(SOURCE + "HexEditor unexpectedly NULL");
                    }
                }
            };

        hefui.getOpenFileButton().setAction(actionOpenFile);
        hefui.getOpenFileButton().setToolTipText((String) actionOpenFile.getValue(Action.SHORT_DESCRIPTION));
        hefui.getOpenFileButton().setEnabled(true);


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
                if ((hefui.getHexEditorUI() != null)
                    && (hefui.getHexEditorUI().getHexEditor() != null)
                    && (hefui.getHexEditorUI().getHexEditor().getTableModel() != null))
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

                    intStatus = chooser.showSaveDialog((Component)hefui.getHexEditorUI());

                    if (intStatus == JFileChooser.APPROVE_OPTION)
                        {
                        final File file;

                        file = chooser.getSelectedFile();

                        if (file != null)
                            {
                            try
                                {
                                final HexTableModel model;
                                final ByteBuffer byteBuffer;
                                final FileOutputStream outputStream;

                                model = hefui.getHexEditorUI().getHexEditor().getTableModel();
                                byteBuffer = model.getByteBuffer();

                                outputStream = new FileOutputStream(file);
                                outputStream.write(byteBuffer.getBuffer());
                                outputStream.close();

                                //LOGGER.log(HexFileHelper.dumpHex(byteBuffer.getBuffer(), 16));
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
                    LOGGER.error(SOURCE + "HexEditor or TableModel unexpectedly NULL");
                    }
                }
            };

        hefui.getSaveAsFileButton().setAction(actionSaveAsFile);
        hefui.getSaveAsFileButton().setToolTipText((String) actionSaveAsFile.getValue(Action.SHORT_DESCRIPTION));
        hefui.getSaveAsFileButton().setEnabled(true);


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
                if ((hefui.getHexEditorUI() != null)
                    && (hefui.getHexEditorUI().getHexEditor() != null)
                    && (hefui.getHexEditorUI().getHexEditor().getByteCount() > 0))
                    {
                    hefui.getHexEditorUI().getHexEditor().cut();
                    }
                else
                    {
                    UIManager.getLookAndFeel().provideErrorFeedback((Component) hefui);
                    }
                }
            };

        hefui.getCutButton().setAction(actionCut);
        hefui.getCutButton().setToolTipText((String) actionCut.getValue(Action.SHORT_DESCRIPTION));
        hefui.getCutButton().setEnabled(true);


        //-------------------------------------------------------------------------------------
        // Copy

        actionCopy = new ContextAction(ACTION_NAME_COPY,
                                       RegistryModelUtilities.getCommonIcon(FILENAME_ICON_COPY),
                                       HexEditorFrameUIComponentInterface.ACTION_TOOLTIP_COPY,
                                       KeyEvent.VK_C,
                                       false,
                                       true)
            {
            final static String SOURCE = "ContextAction:Copy ";
            private static final long serialVersionUID = -9191567200213385853L;


            public void actionPerformed(final ActionEvent event)
                {
                if ((hefui.getHexEditorUI() != null)
                    && (hefui.getHexEditorUI().getHexEditor() != null)
                    && (hefui.getHexEditorUI().getHexEditor().getByteCount() > 0))
                    {
                    hefui.getHexEditorUI().getHexEditor().copy();
                    }
                else
                    {
                    UIManager.getLookAndFeel().provideErrorFeedback((Component) hefui);
                    }
                }
            };

        hefui.getCopyButton().setAction(actionCopy);
        hefui.getCopyButton().setToolTipText((String) actionCopy.getValue(Action.SHORT_DESCRIPTION));
        hefui.getCopyButton().setEnabled(true);


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
                if ((hefui.getHexEditorUI() != null)
                    && (hefui.getHexEditorUI().getHexEditor() != null))
                    {
                    hefui.getHexEditorUI().getHexEditor().paste();
                    }
                else
                    {
                    LOGGER.error(SOURCE + "HexEditor unexpectedly NULL");
                    }
                }
            };

        hefui.getPasteButton().setAction(actionPaste);
        hefui.getPasteButton().setToolTipText((String) actionPaste.getValue(Action.SHORT_DESCRIPTION));
        hefui.getPasteButton().setEnabled(true);


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
                if ((hefui.getHexEditorUI() != null)
                    && (hefui.getHexEditorUI().getHexEditor() != null))
                    {
                    hefui.getHexEditorUI().getHexEditor().delete();
                    }
                else
                    {
                    LOGGER.error(SOURCE + "HexEditor unexpectedly NULL");
                    }
                }
            };

        hefui.getDeleteButton().setAction(actionDelete);
        hefui.getDeleteButton().setToolTipText((String) actionDelete.getValue(Action.SHORT_DESCRIPTION));
        hefui.getDeleteButton().setEnabled(true);


        //-------------------------------------------------------------------------------------
        // Undo

        actionUndo = new ContextAction(ACTION_NAME_UNDO,
                                       RegistryModelUtilities.getCommonIcon(FILENAME_ICON_UNDO),
                                       ACTION_TOOLTIP_UNDO,
                                       KeyEvent.VK_U,
                                       false,
                                       true)
            {
            final static String SOURCE = "ContextAction:Undo ";
            private static final long serialVersionUID = -1408705835772303316L;


            public void actionPerformed(final ActionEvent event)
                {
                if ((hefui.getHexEditorUI() != null)
                    && (hefui.getHexEditorUI().getHexEditor() != null))
                    {
                    hefui.getHexEditorUI().getHexEditor().undo();
                    }
                else
                    {
                    LOGGER.error(SOURCE + "HexEditor unexpectedly NULL");
                    }
                }
            };

        hefui.getUndoButton().setAction(actionUndo);
        hefui.getUndoButton().setToolTipText((String) actionUndo.getValue(Action.SHORT_DESCRIPTION));
        hefui.getUndoButton().setEnabled(true);


        //-------------------------------------------------------------------------------------
        // Redo

        actionRedo = new ContextAction(ACTION_NAME_REDO,
                                       RegistryModelUtilities.getCommonIcon(FILENAME_ICON_REDO),
                                       ACTION_TOOLTIP_REDO,
                                       KeyEvent.VK_R,
                                       false,
                                       true)
            {
            final static String SOURCE = "ContextAction:Redo ";
            private static final long serialVersionUID = 423926301609774538L;


            public void actionPerformed(final ActionEvent event)
                {
                if ((hefui.getHexEditorUI() != null)
                    && (hefui.getHexEditorUI().getHexEditor() != null))
                    {
                    hefui.getHexEditorUI().getHexEditor().redo();
                    }
                else
                    {
                    LOGGER.error(SOURCE + "HexEditor unexpectedly NULL");
                    }
                }
            };

        hefui.getRedoButton().setAction(actionRedo);
        hefui.getRedoButton().setToolTipText((String) actionRedo.getValue(Action.SHORT_DESCRIPTION));
        hefui.getRedoButton().setEnabled(true);


        //-------------------------------------------------------------------------------------
        // Printing
        //-------------------------------------------------------------------------------------
        // Page Setup

        actionPageSetup = new ContextAction(ReportTablePlugin.PREFIX_PAGE_SETUP + MSG_HEX_EDITOR,
                                            RegistryModelUtilities.getCommonIcon(FILENAME_ICON_PAGE_SETUP),
                                            ReportTablePlugin.PREFIX_PAGE_SETUP + MSG_HEX_EDITOR,
                                            KeyEvent.VK_S,
                                            false,
                                            true)
            {
            final static String SOURCE = "ContextAction:PageSetup ";
            private static final long serialVersionUID = 6802400471966299436L;


            public void actionPerformed(final ActionEvent event)
                {
                if (hefui.getHexEditorUI() != null)
                    {
                    final PrinterJob printerJob;
                    final PageFormat pageFormat;

                    printerJob = PrinterJob.getPrinterJob();
                    pageFormat = printerJob.pageDialog(hefui.getHexEditorUI().getPageFormat());

                    if (pageFormat != null)
                        {
                        hefui.getHexEditorUI().setPageFormat(pageFormat);
                        }
                    }
                else
                    {
                    LOGGER.error(SOURCE + "HexEditor unexpectedly NULL");
                    }
                }
            };

        hefui.getPageSetupButton().setAction(actionPageSetup);
        hefui.getPageSetupButton().setToolTipText((String) actionPageSetup.getValue(Action.SHORT_DESCRIPTION));
        hefui.getPageSetupButton().setEnabled(true);

        //-------------------------------------------------------------------------------------
        // Print

        actionPrint = new ContextAction(ReportTablePlugin.PREFIX_PRINT + MSG_HEX_EDITOR,
                                        RegistryModelUtilities.getCommonIcon(FILENAME_ICON_PRINT),
                                        ReportTablePlugin.PREFIX_PRINT + MSG_HEX_EDITOR,
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
                            MSG_HEX_EDITOR_PRINTED,
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
                                                  ReportTablePlugin.PREFIX_PRINT + MSG_HEX_EDITOR,
                                                  JOptionPane.WARNING_MESSAGE,
                                                  RegistryModelUtilities.getCommonIcon(FILENAME_ICON_DIALOG_PRINT));
                    boolSuccess = false;
                    }
                else
                    {
                    final PrinterJob printerJob;

                    printerJob = PrinterJob.getPrinterJob();

                    if ((hefui.getHexEditorUI() != null)
                        && (hefui.getHexEditorUI().getHexEditor() != null)
                        && (printerJob.printDialog()))
                        {
                        final PageFormat pageFormat;

                        pageFormat = hefui.getHexEditorUI().getPageFormat();

                        if (pageFormat != null)
                            {
                            // The HexTable is Printable
                            // ToDo Header & Footer MessageFormats
                            printerJob.setPrintable(hefui.getHexEditorUI().getHexEditor().getTable().getPrintable(JTable.PrintMode.FIT_WIDTH,
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

        hefui.getPrintButton().setAction(actionPrint);
        hefui.getPrintButton().setToolTipText((String) actionPrint.getValue(Action.SHORT_DESCRIPTION));
        hefui.getPrintButton().setEnabled(true);

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
                if ((hefui.getHexEditorUI() != null)
                    && (hefui.getHexEditorUI().getHexEditor() != null))
                    {
                    try
                        {
                        final InputStream inputStream;

                        inputStream = new ByteArrayInputStream(EMPTY_RECORD.getBytes());
                        hefui.getHexEditorUI().getHexEditor().open(inputStream);
                        }

                    catch (IOException exception)
                        {
                        LOGGER.error(SOURCE + " Unexpected IOException [exception=" + exception.getMessage() + "]");
                        }
                    }
                else
                    {
                    LOGGER.error(SOURCE + "HexEditor unexpectedly NULL");
                    }
                }
            };

        hefui.getClearAllButton().setAction(actionClearAll);
        hefui.getClearAllButton().setToolTipText((String) actionClearAll.getValue(Action.SHORT_DESCRIPTION));
        hefui.getClearAllButton().setEnabled(true);


        //-------------------------------------------------------------------------------------
        // Put it all together

        toolbar.removeAll();

        toolbar.addSeparator(DIM_TOOLBAR_SEPARATOR_BUTTON);
        toolbar.add(labelName);
        toolbar.addSeparator(DIM_TOOLBAR_SEPARATOR);

        toolbar.add(Box.createHorizontalGlue());

        toolbar.add(hefui.getOpenFileButton());
        toolbar.addSeparator(DIM_TOOLBAR_SEPARATOR);

        toolbar.add(hefui.getSaveAsFileButton());
        toolbar.addSeparator(DIM_TOOLBAR_SEPARATOR);

        toolbar.add(hefui.getCutButton());
        toolbar.addSeparator(DIM_TOOLBAR_SEPARATOR);

        toolbar.add(hefui.getCopyButton());
        toolbar.addSeparator(DIM_TOOLBAR_SEPARATOR);

        toolbar.add(hefui.getPasteButton());
        toolbar.addSeparator(DIM_TOOLBAR_SEPARATOR);

        toolbar.add(hefui.getDeleteButton());
        toolbar.addSeparator(DIM_TOOLBAR_SEPARATOR);

        toolbar.add(hefui.getUndoButton());
        toolbar.addSeparator(DIM_TOOLBAR_SEPARATOR);

        toolbar.add(hefui.getRedoButton());
        toolbar.addSeparator(DIM_TOOLBAR_SEPARATOR);

        toolbar.add(hefui.getPageSetupButton());
        toolbar.addSeparator(DIM_TOOLBAR_SEPARATOR);

        toolbar.add(hefui.getPrintButton());
        toolbar.addSeparator(DIM_TOOLBAR_SEPARATOR);

        toolbar.add(hefui.getClearAllButton());
        toolbar.addSeparator(DIM_TOOLBAR_SEPARATOR);

        NavigationUtilities.updateComponentTreeUI(toolbar);
        }


    /***********************************************************************************************
     * Construct a HexEditorFrameUIComponent.
     *
     * @param hostinstrument
     * @param instrumentxml
     * @param hostui
     * @param task
     * @param font
     * @param colour
     * @param resourcekey
     */

    public HexEditorFrameUIComponent(final ObservatoryInstrumentInterface hostinstrument,
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
        this.uiHexEditor = null;
        this.toolbarHexEditor = null;
        this.buttonOpenFile = null;
        this.buttonSaveAsFile = null;
        this.buttonCut = null;
        this.buttonCopy = null;
        this.buttonPaste = null;
        this.buttonDelete = null;
        this.buttonUndo = null;
        this.buttonRedo = null;
        this.buttonPageSetup = null;
        this.buttonPrint = null;
        this.buttonClearAll = null;

        // This sets the debug state for all of the HexEditor
        setDebug(false);
        }


    /**********************************************************************************************/
    /* UI State                                                                                   */
    /***********************************************************************************************
     * Initialise this UIComponent.
     */

    public void initialiseUI()
        {
        final String SOURCE = "HexEditorFrameUIComponent.initialiseUI() ";

        LOGGER.debug(isDebug(), SOURCE);

        // Colours
        setBackground(DEFAULT_COLOUR_CANVAS.getColor());

        // Create the HexEditorUIComponent and initialise it
        // Do this first to get the colours and fonts
        // This is the only creation of HexEditorUIComponent
        setHexEditorUI(new HexEditorUIComponent(getHostTask(),
                                                getObservatoryUI(),
                                                getHostInstrument(),
                                                getFontData(),
                                                getColourData(),
                                                getResourceKey(),
                                                isDebug()));
        getHexEditorUI().initialiseUI();

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
        add((Component) getHexEditorUI(), BorderLayout.CENTER);

        getHexEditorUI().getHexEditor().addHexEditorListener(this);
        getHexEditorUI().getHexEditor().addSelectionChangedListener(this);


        //        final JPanel temp = new JPanel(new BorderLayout());
//        final ConfigPanel configPanel = new ConfigPanel();
//        temp.add(configPanel, BorderLayout.LINE_START);
//
//        infoField = new JTextField();
//        infoField.setEditable(false);
//        infoField.setBorder(BorderFactory.createCompoundBorder(
//                BorderFactory.createEmptyBorder(5, 5, 5, 5),
//                infoField.getBorder()));
//
//        selField = new JTextField(30);
//        selField.setEditable(false);
//        selField.setBorder(BorderFactory.createCompoundBorder(
//                BorderFactory.createEmptyBorder(5, 0, 5, 5),
//                selField.getBorder()));
//
//        sizeField = new JTextField(15);
//        sizeField.setEditable(false);
//        sizeField.setBorder(BorderFactory.createCompoundBorder(
//                BorderFactory.createEmptyBorder(5, 0, 5, 5),
//                sizeField.getBorder()));
//
//        final JPanel temp2 = new JPanel(new BorderLayout());
//        final JPanel temp3 = new JPanel(new BorderLayout());
//
//        temp2.add(infoField);
//
//        temp3.add(selField, BorderLayout.LINE_START);
//        temp3.add(sizeField, BorderLayout.LINE_END);
//
//        temp2.add(temp3, BorderLayout.LINE_END);
//
//        temp.add(temp2, BorderLayout.SOUTH);
//        add(temp, BorderLayout.SOUTH);
//
//        // Add the editor after infoField as it listens to byte changes.

        }


    /***********************************************************************************************
     * Run this UIComponent.
     */

    public void runUI()
        {
        final String SOURCE = "HexEditorFrameUIComponent.runUI() ";

        LOGGER.debug(isDebug(), SOURCE);

        super.runUI();

        if (getHexEditorUI() != null)
            {
            UIComponentHelper.runComponentAndTransferActions((Component) getHexEditorUI(), this);
            }
        }


    /***********************************************************************************************
     * Stop this UIComponent.
     */

    public void stopUI()
        {
        final String SOURCE = "HexEditorFrameUIComponent.stopUI() ";

        LOGGER.debug(isDebug(), SOURCE);

        super.stopUI();

        if (getHexEditorUI() != null)
            {
            getHexEditorUI().stopUI();
            }
        }


    /***********************************************************************************************
     * Dispose of all components of this UIComponent.
     */

    public void disposeUI()
        {
        final String SOURCE = "HexEditorFrameUIComponent.disposeUI() ";

        LOGGER.debug(isDebug(), SOURCE);

        stopUI();

        if (getToolBar() != null)
            {
            getToolBar().removeAll();
            setToolBar(null);
            }

        if (getHexEditorUI() != null)
            {
            getHexEditorUI().disposeUI();
            setHexEditorUI(null);
            }

        super.disposeUI();
        }


    /**********************************************************************************************/
    /* UI                                                                                         */
    /***********************************************************************************************
     * Get the HexEditor UI.
     *
     * @return HexEditorUIComponentInterface
     */

    public HexEditorUIComponentInterface getHexEditorUI()
        {
        return (this.uiHexEditor);
        }


    /***********************************************************************************************
     * Set the HexEditor UI.
     *
     * @param hexeditorui
     */

    private void setHexEditorUI(final HexEditorUIComponentInterface hexeditorui)
        {
        this.uiHexEditor = hexeditorui;
        }


    /***********************************************************************************************
     * Get the HexEditor JToolBar.
     *
     * @return JToolBar
     */

    private JToolBar getToolBar()
        {
        return (this.toolbarHexEditor);
        }


    /***********************************************************************************************
     * Set the HexEditor JToolBar.
     *
     * @param toolbar
     */

    private void setToolBar(final JToolBar toolbar)
        {
        this.toolbarHexEditor = toolbar;
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
     * Get the Undo button.
     *
     * @return JButton
     */

    public JButton getUndoButton()
        {
        return (this.buttonUndo);
        }


    /***********************************************************************************************
     * Set the Undo button.
     *
     * @param button
     */

    public void setUndoButton(final JButton button)
        {
        this.buttonUndo = button;
        }


    /***********************************************************************************************
     * Get the Redo button.
     *
     * @return JButton
     */

    public JButton getRedoButton()
        {
        return (this.buttonRedo);
        }


    /***********************************************************************************************
     * Set the Redo button.
     *
     * @param button
     */

    public void setRedoButton(final JButton button)
        {
        this.buttonRedo = button;
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


    /**********************************************************************************************
     * Called when the bytes in the hex editor change.
     *
     * @param event
     */

    public void hexBytesChanged(final HexEditorEvent event)
        {

//        System.out.println("HexEditorFrameUIComponent.hexBytesChanged "
//                                + "[ismodification=" + event.isModification()
//                                + "] [offset=" + event.getOffset()
//                                + "] [added=" + event.getAddedCount()
//                                + "] [removed=" + event.getRemovedCount()
//                                + "]");
//        String text;
//
//        if (e.isModification())
//            {
//            text = getInfoString("InfoFieldModified",
//                                 e.getOffset(), e.getAddedCount());
//            }
//        else
//            {
//            final int added = e.getAddedCount();
//            final int removed = e.getRemovedCount();
//            if (added > 0 && removed == 0)
//                {
//                text = getInfoString("InfoFieldAdded",
//                                     e.getOffset(), e.getAddedCount());
//                }
//            else if (added == 0 && removed > 0)
//                {
//                text = getInfoString("InfoFieldRemoved",
//                                     e.getOffset(), e.getRemovedCount());
//                }
//            else
//                {
//                text = getInfoString("InfoFieldBoth", e.getOffset(),
//                                     e.getAddedCount(), e.getRemovedCount());
//                }
//            }
//        infoField.setText(text);
//
//        text = msg.getString("SizeField");
//        text = MessageFormat.format(text,
//                                    new Object[]{e.getHexEditor().getByteCount()});
//        sizeField.setText(text);

        }


    /**********************************************************************************************
     * Called when the selection changes in the hex editor.
     *
     * @param event
     */

    public void selectionChanged(final SelectionChangedEvent event)
        {
//        System.out.println("HexEditorFrameUIComponent.selectionChanged "
//                               + "[start.new=" + event.getNewSelecStart()
//                               + "] [end.new=" + event.getNewSelecEnd()
//                               + "] [start.previous=" + event.getPreviousSelecStart()
//                               + "] [end.previous=" + event.getPreviousSelecEnd()
//                               + "]");

//        final int offs = e.getNewSelecStart();
//        final int count = e.getNewSelecEnd() - offs + 1;
//        final String subject;
//        if (count == 1)
//            {
//            subject = " byte";
//            }
//        else
//            {
//            subject = " bytes";
//            }
//        selField.setText(count + subject + " selected at offset " + offs);
        }


    //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!


    //    /**
    //     * Listens for events in this panel.
    //     *
    //     * @param e The event that occurred.
    //     */
    //    public void actionPerformed(final ActionEvent e)
    //        {
    //
    //        final Object source = e.getSource();
    //
    //        if (source.equals(colHeaderCB))
    //            {
    //            editor.setShowColumnHeader(colHeaderCB.isSelected());
    //            }
    //        else if (source.equals(rowHeaderCB))
    //            {
    //            editor.setShowRowHeader(rowHeaderCB.isSelected());
    //            }
    //        else if (source.equals(showGridCB))
    //            {
    //            editor.setShowGrid(showGridCB.isSelected());
    //            }
    //        else if (source.equals(lafCombo))
    //            {
    //            final String value = (String) lafCombo.getSelectedItem();
    //            String laf = null;
    //            if ("System".equals(value))
    //                {
    //                laf = UIManager.getSystemLookAndFeelClassName();
    //                }
    //            else if ("Metal".equals(value))
    //                {
    //                laf = UIManager.getCrossPlatformLookAndFeelClassName();
    //                }
    //            else if ("Motif".equals(value))
    //                {
    //                laf = "com.sun.java.swing.plaf.motif.MotifLookAndFeel";
    //                }
    //            else if ("Nimbus".equals(value))
    //                {
    //                final UIManager.LookAndFeelInfo[] infos = UIManager.getInstalledLookAndFeels();
    //                for (int i = 0;
    //                     i < infos.length;
    //                     i++)
    //                    {
    //                    if ("Nimbus".equals(infos[i].getName()))
    //                        {
    //                        laf = infos[i].getClassName();
    //                        break;
    //                        }
    //                    }
    //                }
    //            try
    //                {
    //                UIManager.setLookAndFeel(laf);
    //                final Window w = SwingUtilities.getWindowAncestor(this);
    //                if (w != null)
    //                    {
    //                    SwingUtilities.updateComponentTreeUI(w);
    //                    }
    //                else
    //                    {
    //                    SwingUtilities.updateComponentTreeUI(this);
    //                    }
    //                }
    //            catch (Exception ex)
    //                {
    //                ex.printStackTrace();
    //                }
    //            }
    //        else if (source.equals(altColBGCB))
    //            {
    //            editor.setAlternateColumnBG(altColBGCB.isSelected());
    //            }
    //        else if (source.equals(altRowBGCB))
    //            {
    //            editor.setAlternateRowBG(altRowBGCB.isSelected());
    //            }
    //        else if (source.equals(highlightAsciiSelCB))
    //            {
    //            editor.setHighlightSelectionInAsciiDump(
    //                    highlightAsciiSelCB.isSelected());
    //            }
    //        else if (source.equals(highlightAsciiSelCombo))
    //            {
    //            editor.setHighlightSelectionInAsciiDumpColor(
    //                    (Color) highlightAsciiSelCombo.getSelectedItem());
    //            }
    //        else if (source.equals(lowBytePaddingCB))
    //            {
    //            editor.setPadLowBytes(lowBytePaddingCB.isSelected());
    //            }
    //
    //        }


    //    private static String getInfoString(final String key,
    //                                        final int offs,
    //                                        final int param)
    //        {
    //        String text = msg.getString(key);
    //        text = MessageFormat.format(text,
    //                                    new Object[]{offs,
    //                                                 param});
    //        return text;
    //        }
    //
    //
    //    private static String getInfoString(final String key,
    //                                        final int offs,
    //                                        final int param1,
    //                                        final int param2)
    //        {
    //        String text = msg.getString(key);
    //        text = MessageFormat.format(text,
    //                                    new Object[]{offs,
    //                                                 param1,
    //                                                 param2});
    //        return text;
    //        }


    //
    //
    //    /**
    //     * Returns whether Nimbus is supported by the current JVM.  Nimbus was
    //     * added in 6u10, and the package containing it changed in Java 7, so this
    //     * is the only reliable way to check for it.
    //     *
    //     * @return Whether Nimbus is installed.
    //     */
    //    private static boolean isNimbusSupported()
    //        {
    //        // Overridden to always return false, since Nimbus doesn't uninstall
    //        // cleanly (leaves table selection color behind) and we don't want it
    //        // to look like a HexEditor bug.
    //        return false;
    //        /*
    //		LookAndFeelInfo[] infos = UIManager.getInstalledLookAndFeels();
    //		for (int i=0; i<infos.length; i++) {
    //			if ("Nimbus".equals(infos[i].getName())) {
    //				return true;
    //			}
    //		}
    //		return false;
    //		*/
    //        }
    //
    //


    //    /**
//     * Renderer for JComboBox content lists containing colors.
//     *
//     * @author Robert Futrell
//     * @version 1.0
//     */
//    private static class ColorCellRenderer
//            extends DefaultListCellRenderer
//            implements Icon
//        {
//
//        private static final long serialVersionUID = 1L;
//
//        private Color c;
//
//
//        public Component getListCellRendererComponent(final JList list,
//                                                      final Object value,
//                                                      final int index,
//                                                      final boolean selected,
//                                                      final boolean hasFocus)
//            {
//            super.getListCellRendererComponent(list, null, index, selected,
//                                               hasFocus);
//            if (value instanceof Color)
//                {
//                c = (Color) value;
//                }
//            return this;
//            }
//
//
//        public Icon getIcon()
//            {
//            return this;
//            }
//
//
//        public int getIconHeight()
//            {
//            return 16;
//            }
//
//
//        public int getIconWidth()
//            {
//            return 16;
//            }
//
//
//        public void paintIcon(final Component comp,
//                              final Graphics g,
//                              final int x,
//                              final int y)
//            {
//            g.setColor(c);
//            g.fillRect(x, y, getIconWidth(), getIconHeight());
//            g.setColor(Color.BLACK);
//            g.drawRect(x, y, getIconWidth(), getIconHeight());
//            }
//
//        }
//
//
//    private class ConfigPanel
//            extends JPanel
//        {
//
//        private static final long serialVersionUID = 1L;
//
//
//        private ConfigPanel()
//            {
//
//            setLayout(new BorderLayout());
//            setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
//
//            JPanel temp = new JPanel(new GridLayout(3, 3, 5, 5));
//
//            colHeaderCB = new JCheckBox(msg.getString("ColHeaderCB"), true);
//            colHeaderCB.addActionListener(HexEditorUIComponent.this);
//            temp.add(colHeaderCB);
//
//            rowHeaderCB = new JCheckBox(msg.getString("RowHeaderCB"), true);
//            rowHeaderCB.addActionListener(HexEditorUIComponent.this);
//            temp.add(rowHeaderCB);
//
//            showGridCB = new JCheckBox(msg.getString("GridLinesCB"), false);
//            showGridCB.addActionListener(HexEditorUIComponent.this);
//            temp.add(showGridCB);
//
//            altRowBGCB = new JCheckBox(msg.getString("AlternateRowBG"), false);
//            altRowBGCB.addActionListener(HexEditorUIComponent.this);
//            temp.add(altRowBGCB);
//
//            altColBGCB = new JCheckBox(msg.getString("AlternateColBG"), false);
//            altColBGCB.addActionListener(HexEditorUIComponent.this);
//            temp.add(altColBGCB);
//
//            lowBytePaddingCB = new JCheckBox(msg.getString("PadLowBytesCB"), true);
//            lowBytePaddingCB.addActionListener(HexEditorUIComponent.this);
//            temp.add(lowBytePaddingCB);
//
//            highlightAsciiSelCB = new JCheckBox(
//                    msg.getString("HighlightAsciiSel"), true);
//            highlightAsciiSelCB.addActionListener(HexEditorUIComponent.this);
//            temp.add(highlightAsciiSelCB);
//
//            highlightAsciiSelCombo = new JComboBox();
//            highlightAsciiSelCombo.setRenderer(new ColorCellRenderer());
//            highlightAsciiSelCombo.addItem(new Color(255, 255, 192));
//            highlightAsciiSelCombo.addItem(new Color(224, 224, 255));
//            highlightAsciiSelCombo.addItem(new Color(224, 224, 224));
//            highlightAsciiSelCombo.addActionListener(HexEditorUIComponent.this);
//            JPanel temp2 = new JPanel(new BorderLayout());
//            final String text = msg.getString("HighlightColor");
//            temp2.add(new JLabel(text), BorderLayout.LINE_START);
//            temp2.add(highlightAsciiSelCombo);
//            temp.add(temp2);
//
//            add(temp);
//
//            temp = new JPanel(new BorderLayout());
//            temp.add(new JLabel(msg.getString("LafLabel")),
//                     BorderLayout.LINE_START);
//            lafCombo = new JComboBox();
//            lafCombo.addItem("System");
//            lafCombo.addItem("Metal");
//            lafCombo.addItem("Motif");
//            if (isNimbusSupported())
//                {
//                lafCombo.addItem("Nimbus");
//                }
//            lafCombo.addActionListener(HexEditorUIComponent.this);
//            temp.add(lafCombo);
//            temp2 = new JPanel(new BorderLayout());
//            temp2.add(temp, BorderLayout.LINE_START);
//            add(temp2, BorderLayout.SOUTH);
//
//            }
//
//        }
//
    }
