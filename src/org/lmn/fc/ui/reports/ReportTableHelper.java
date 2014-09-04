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

package org.lmn.fc.ui.reports;


import org.lmn.fc.common.actions.ContextAction;
import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.common.exceptions.ReportException;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.model.registry.NavigationUtilities;
import org.lmn.fc.model.registry.RegistryModelUtilities;
import org.lmn.fc.ui.UIComponentPlugin;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.awt.print.PageFormat;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;


/***************************************************************************************************
 * ReportTableHelper.
 */

public final class ReportTableHelper implements FrameworkConstants,
                                                FrameworkStrings,
                                                FrameworkMetadata,
                                                FrameworkSingletons
    {
    // String resources
    private static final String TOOLTIP_DISPOSE                  = "Remove all of the report data to save memory";
    private static final String TOOLTIP_TRUNCATE                 = "Truncate the report to save memory";
    private static final String TOOLTIP_REMOVE                   = "Remove the visible report to save memory (does not remove data)";
    private static final String TOOLTIP_REFRESH                  = "Refresh the report display";
    private static final String TOOLTIP_PRINT                    = "Print the current report to a printer or to a file";

    private static final String TOOLTIP_DISPLAY_LIMIT            = "The maximum number of lines to display";
    private static final String EXCEPTION_VIEWLIMIT_RANGE        = "DataViewLimit out of range";

    private static final String MSG_DISPOSE_0                    = "Are you sure that you wish to delete the Report?";
    private static final String MSG_DISPOSE_1                    = "This action will permanently remove all data!";
    private static final String MSG_NO_PRINTER                   = "No printer found!";

    private static final String GREY_CELL_PREFIX                 = "<html><font color=gray><i>";
    private static final String GREY_CELL_SUFFIX                 = "</i></font></html>";
    private static final String COLOUR_CELL_PREFIX               = "<html><font color=";
    private static final String COLOUR_CELL_PREFIX_TERMINATOR    = ">";
    private static final String COLOUR_CELL_SUFFIX               = "</font></html>";
    private static final String HIGHLIGHT_CELL_PREFIX            = "<html><font color=red><i>";
    private static final String HIGHLIGHT_CELL_SUFFIX            = "</i></font></html>";
    public static final String CR_LF                             = "\r\n";
    private static final String ALIGN_LEFT                       = "left";
    private static final String ALIGN_RIGHT                      = "right";
    private static final String ALIGN_CENTER                     = "center";

    public static final Dimension DIM_VIEW_LIMIT = new Dimension(50, 20);
    public static final float SIZE_HEADER_FONT = 13.0f;


    /***********************************************************************************************
     * Initialise the Horizontal Toolbar.
     * Return the JTextField to simplify updates.
     * Horizontal toolbars have RefreshView (with DataViewMode), RemoveView and DisposeAll (left to right).
     *
     * @param report
     * @param toolbar
     * @param toolbarstate
     * @param toolbaricon
     *
     * @return JToolBar
     */

    public static JTextField initialiseHorizontalToolbar(final ReportTablePlugin report,
                                                         final JToolBar toolbar,
                                                         final ReportTableToolbar toolbarstate,
                                                         final Icon toolbaricon)
        {
        final JTextField textViewLimit;

        // Return the JTextField to simplify updates
        textViewLimit = new JTextField(10);
        textViewLimit.setAlignmentX(Component.LEFT_ALIGNMENT);
        textViewLimit.setMinimumSize(DIM_VIEW_LIMIT);
        textViewLimit.setPreferredSize(DIM_VIEW_LIMIT);
        textViewLimit.setMaximumSize(DIM_VIEW_LIMIT);
        textViewLimit.setMargin(new Insets(0, 5, 0, 5));
        textViewLimit.setToolTipText(TOOLTIP_DISPLAY_LIMIT);
        textViewLimit.setFont(report.getReportFont().getFont());
        textViewLimit.setForeground(report.getTextColour().getColor());
        textViewLimit.setText(EMPTY_STRING);

        if ((report != null)
            &&(toolbar != null))
            {
            final JLabel labelName;
            final ButtonGroup buttonGroup;
            final JButton buttonRefresh;

            // These are needed in advance, awkwardly...
            buttonGroup = new ButtonGroup();
            buttonRefresh = new JButton();

            // Assemble the Toolbar as we go
            toolbar.removeAll();
            toolbar.setFloatable(false);
            toolbar.addSeparator(UIComponentPlugin.DIM_TOOLBAR_SEPARATOR_START);

            //--------------------------------------------------------------------------------------
            // Report Name, always visible

            // If the value of toolbaricon is null, nothing is displayed
            labelName = new JLabel(report.getReportTabName(),
                                   toolbaricon,
                                   SwingConstants.LEFT)
                {
                private static final long serialVersionUID = -7599008206447812328L;


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

            labelName.setFont(report.getReportFont().getFont().deriveFont(SIZE_HEADER_FONT).deriveFont(Font.BOLD));
            labelName.setForeground(report.getTextColour().getColor());
            labelName.setIconTextGap(UIComponentPlugin.TOOLBAR_ICON_TEXT_GAP);

            toolbar.add(labelName);
            toolbar.addSeparator(UIComponentPlugin.DIM_TOOLBAR_SEPARATOR);
            toolbar.add(Box.createHorizontalGlue());

            //--------------------------------------------------------------------------------------
            // Text Parameter input

            if (toolbarstate.hasRanges())
                {
                final DocumentListener listenerViewLimit;
                final DataViewMode[] arrayViews;
                final Enumeration<AbstractButton> enumButtons;

                arrayViews = DataViewMode.values();

                report.setDataViewLimit(ReportTablePlugin.DEFAULT_DATA_VIEW_LIMIT);
                textViewLimit.setText(Integer.toString(report.getDataViewLimit()));

                // Text Field Listener
                listenerViewLimit = new DocumentListener()
                    {
                    public void insertUpdate(final DocumentEvent event)
                        {
                        // Force the user to enter a correct value
                        // This only changes the enable state of the Refresh button
                        processViewLimit(report,
                                         buttonGroup,
                                         textViewLimit,
                                         buttonRefresh);
                        }

                    public void removeUpdate(final DocumentEvent event)
                        {
                        processViewLimit(report,
                                         buttonGroup,
                                         textViewLimit,
                                         buttonRefresh);
                        }

                    public void changedUpdate(final DocumentEvent event)
                        {
                        processViewLimit(report,
                                         buttonGroup,
                                         textViewLimit,
                                         buttonRefresh);
                        }
                    };

                textViewLimit.getDocument().addDocumentListener(listenerViewLimit);

                //--------------------------------------------------------------------------------------
                // DataViewMode Buttons

                for (int intViewIndex = 0;
                     intViewIndex < arrayViews.length;
                     intViewIndex++)
                    {
                    final DataViewMode viewMode;
                    final JRadioButton buttonView;
                    final ActionListener listenerView;
                    final ButtonModel model;

                    viewMode = arrayViews[intViewIndex];

                    buttonView = new JRadioButton(viewMode.getName());
                    buttonView.setHorizontalTextPosition(SwingConstants.LEFT);
                    buttonView.setToolTipText(viewMode.getTooltip());
                    buttonView.setBackground(toolbar.getBackground());
                    buttonView.setFont(report.getReportFont().getFont());
                    buttonView.setForeground(report.getTextColour().getColor());

                    listenerView = new ActionListener()
                                        {
                                        public void actionPerformed(final ActionEvent event)
                                            {
                                            // Use the new selected DataViewMode to refresh the Report
                                            updateDataView(report,
                                                           buttonGroup,
                                                           viewMode,
                                                           textViewLimit.getText());
                                            }
                                        };

                    buttonView.addActionListener(listenerView);
                    buttonGroup.add(buttonView);

                    // If the DataViewMode is the default, select the radio button;
                    // any currently selected radio button is deselected.
                    // This operation does not cause any action events to be fired.
                    model = buttonView.getModel();
                    buttonGroup.setSelected(model, viewMode.isDefaultSelection());

                    // Make sure that the report is in sync with the buttons
                    if (viewMode.isDefaultSelection())
                        {
                        report.setDataViewMode(viewMode);
                        }
                    }

                // Now add the buttons to the toolbar
                enumButtons = buttonGroup.getElements();

                while (enumButtons.hasMoreElements())
                    {
                    final AbstractButton button;

                    button = enumButtons.nextElement();
                    toolbar.add(button);
                    toolbar.addSeparator(UIComponentPlugin.DIM_TOOLBAR_SEPARATOR_BUTTON);
                    }

                toolbar.add(textViewLimit);
                toolbar.addSeparator(UIComponentPlugin.DIM_TOOLBAR_SEPARATOR_BUTTON);
                }

            //-------------------------------------------------------------------------------------
            // PageSetup and Print

            if ((toolbarstate.hasPrinting())
                && (report.isPrintable()))
                {
                final String strTitle;

                final ContextAction actionPageSetup;
                final ContextAction actionPrint;
                final JButton buttonPageSetup;
                final JButton buttonPrint;

                strTitle = report.getReportTabName();

                actionPageSetup = new ContextAction(ReportTablePlugin.PREFIX_PAGE_SETUP + strTitle,
                                                    RegistryModelUtilities.getCommonIcon(UIComponentPlugin.FILENAME_ICON_PAGE_SETUP),
                                                    ReportTablePlugin.PREFIX_PAGE_SETUP + strTitle,
                                                    KeyEvent.VK_S,
                                                    false,
                                                    true)
                    {
                    final static String SOURCE = "ContextAction:PageSetup ";
                    private static final long serialVersionUID = 6802400471966299436L;


                    public void actionPerformed(final ActionEvent event)
                        {
                        if ((report != null)
                            && (PrinterJob.lookupPrintServices().length > 0))
                            {
                            final PrinterJob printerJob;
                            final PageFormat pageFormatModified;

                            printerJob = PrinterJob.getPrinterJob();
                            pageFormatModified = printerJob.pageDialog(report.getPageFormat());

                            if (pageFormatModified != null)
                                {
                                report.setPageFormat(pageFormatModified);
                                }
                            }
                        else
                            {
                            LOGGER.error(SOURCE + "Report unexpectedly NULL, or no Print Services");
                            }
                        }
                    };

                buttonPageSetup = new JButton();
                buttonPageSetup.setBorder(UIComponentPlugin.BORDER_BUTTON);
                buttonPageSetup.setAction(actionPageSetup);
                buttonPageSetup.setHideActionText(true);
                buttonPageSetup.setToolTipText((String) actionPageSetup.getValue(Action.SHORT_DESCRIPTION));
                buttonPageSetup.setEnabled(true);

                buttonPrint = new JButton();
                buttonPrint.setBorderPainted(false);
                buttonPrint.setIcon(RegistryModelUtilities.getCommonIcon(UIComponentPlugin.FILENAME_ICON_PRINT));
                buttonPrint.setToolTipText(TOOLTIP_PRINT);

                buttonPrint.addActionListener(new ActionListener()
                    {
                    public void actionPerformed(final ActionEvent event)
                        {
                        doPrint(report);
                        }
                    });

                toolbar.add(buttonPageSetup);
                toolbar.addSeparator(UIComponentPlugin.DIM_TOOLBAR_SEPARATOR_BUTTON);
                toolbar.add(buttonPrint);
                toolbar.addSeparator(UIComponentPlugin.DIM_TOOLBAR_SEPARATOR_BUTTON);
                }

            //-------------------------------------------------------------------------------------
            // RefreshView

            if ((toolbarstate.hasRefreshView())
                && (buttonRefresh != null))
                {
                buttonRefresh.setBorderPainted(false);
                buttonRefresh.setIcon(RegistryModelUtilities.getCommonIcon(UIComponentPlugin.FILENAME_ICON_REFRESH));
                buttonRefresh.setToolTipText(TOOLTIP_REFRESH);

                buttonRefresh.addActionListener(new ActionListener()
                    {
                    public void actionPerformed(final ActionEvent event)
                        {
                        // Use the current DataViewMode to refresh the Report
                        updateDataView(report,
                                       buttonGroup,
                                       report.getDataViewMode(),
                                       textViewLimit.getText());
                        }
                    });

                toolbar.add(buttonRefresh);
                toolbar.addSeparator(UIComponentPlugin.DIM_TOOLBAR_SEPARATOR_BUTTON);
                }

            //-------------------------------------------------------------------------------------
            // Remove Report

            if (toolbarstate.hasRemoveView())
                {
                final JButton buttonRemove;

                buttonRemove = new JButton();
                buttonRemove.setBorderPainted(false);
                buttonRemove.setIcon(RegistryModelUtilities.getCommonIcon(UIComponentPlugin.FILENAME_ICON_REMOVE));
                buttonRemove.setToolTipText(TOOLTIP_REMOVE);

                buttonRemove.addActionListener(new ActionListener()
                    {
                    public void actionPerformed(final ActionEvent event)
                        {
                        // Deselect all DataViewMode buttons (Java 6 onwards)
                        if (buttonGroup != null)
                            {
                            buttonGroup.clearSelection();
                            }

                        report.setDataViewMode(null);
                        report.refreshTable();
                        ObservatoryInstrumentHelper.runGarbageCollector();
                        }
                    });

                toolbar.add(buttonRemove);
                toolbar.addSeparator(UIComponentPlugin.DIM_TOOLBAR_SEPARATOR_BUTTON);
                }

            //-------------------------------------------------------------------------------------
            // Truncate Report View

            if ((toolbarstate.hasTruncateView())
                && (report.isTruncateable()))
                {
                final JButton buttonTruncate;

                buttonTruncate = new JButton();
                buttonTruncate.setBorderPainted(false);
                buttonTruncate.setIcon(RegistryModelUtilities.getCommonIcon(UIComponentPlugin.FILENAME_ICON_TRUNCATE));
                buttonTruncate.setToolTipText(TOOLTIP_TRUNCATE);

                buttonTruncate.addActionListener(new ActionListener()
                    {
                    public void actionPerformed(final ActionEvent event)
                        {
                        // If the report has a truncation method, it will give a message box
                        report.truncateReport();
                        report.refreshTable();
                        ObservatoryInstrumentHelper.runGarbageCollector();
                        }
                    });

                toolbar.add(buttonTruncate);
                toolbar.addSeparator(UIComponentPlugin.DIM_TOOLBAR_SEPARATOR_BUTTON);
                }

            //-------------------------------------------------------------------------------------
            // Dispose Report

            if (toolbarstate.hasDisposeAll())
                {
                final JButton buttonDispose;

                buttonDispose = new JButton();
                buttonDispose.setBorderPainted(false);
                buttonDispose.setIcon(RegistryModelUtilities.getCommonIcon(UIComponentPlugin.FILENAME_ICON_DISPOSE));
                buttonDispose.setToolTipText(TOOLTIP_DISPOSE);

                buttonDispose.addActionListener(new ActionListener()
                    {
                    public void actionPerformed(final ActionEvent event)
                        {
                        final int intChoice;
                        final String [] strMessage =
                            {
                            MSG_DISPOSE_0,
                            MSG_DISPOSE_1
                            };

                        intChoice = JOptionPane.showOptionDialog(null,
                                                                 strMessage,
                                                                 report.getReportTabName(),
                                                                 JOptionPane.YES_NO_OPTION,
                                                                 JOptionPane.QUESTION_MESSAGE,
                                                                 null,
                                                                 null,
                                                                 null);

                        if (intChoice == JOptionPane.YES_OPTION)
                            {
                            report.disposeReport();
                            report.refreshTable();
                            ObservatoryInstrumentHelper.runGarbageCollector();
                            }
                        }
                    });

                toolbar.add(buttonDispose);
                toolbar.addSeparator(UIComponentPlugin.DIM_TOOLBAR_SEPARATOR_BUTTON);
                }

            NavigationUtilities.updateComponentTreeUI(toolbar);
            }

        return (textViewLimit);
        }


    /***********************************************************************************************
     * Initialise the Vertical Toolbar.
     * Vertical toolbars may have DisposeAll, TruncateView and Printing (top to bottom).
     * There is no label or icon.
     * Return the JTextField to simplify updates, but this is not used.
     *
     * @param report
     * @param toolbar
     * @param toolbarstate
     *
     * @return JToolBar
     */

    public static JTextField initialiseVerticalToolbar(final ReportTablePlugin report,
                                                       final JToolBar toolbar,
                                                       final ReportTableToolbar toolbarstate)
        {
        final JTextField textViewLimit;

        textViewLimit = new JTextField(10);

        if ((report != null)
            && (toolbar != null))
            {
            // For historical consistency, probably pointless
            report.setDataViewLimit(ReportTablePlugin.DEFAULT_DATA_VIEW_LIMIT);
            textViewLimit.setText(Integer.toString(report.getDataViewLimit()));

            // Assemble the Toolbar as we go
            toolbar.removeAll();
            toolbar.setFloatable(false);
            toolbar.addSeparator(UIComponentPlugin.DIM_TOOLBAR_SEPARATOR_BUTTON);

            //-------------------------------------------------------------------------------------
            // Dispose Report

            if (toolbarstate.hasDisposeAll())
                {
                final JButton buttonDispose;

                buttonDispose = new JButton();
                buttonDispose.setBorderPainted(false);
                buttonDispose.setIcon(RegistryModelUtilities.getCommonIcon(UIComponentPlugin.FILENAME_ICON_DISPOSE));
                buttonDispose.setToolTipText(TOOLTIP_DISPOSE);

                buttonDispose.addActionListener(new ActionListener()
                    {
                    public void actionPerformed(final ActionEvent event)
                        {
                        final int intChoice;
                        final String [] strMessage =
                            {
                            MSG_DISPOSE_0,
                            MSG_DISPOSE_1
                            };

                        intChoice = JOptionPane.showOptionDialog(null,
                                                                 strMessage,
                                                                 report.getReportTabName(),
                                                                 JOptionPane.YES_NO_OPTION,
                                                                 JOptionPane.QUESTION_MESSAGE,
                                                                 null,
                                                                 null,
                                                                 null);

                        if (intChoice == JOptionPane.YES_OPTION)
                            {
                            report.disposeReport();
                            report.refreshTable();
                            ObservatoryInstrumentHelper.runGarbageCollector();
                            }
                        }
                    });

                toolbar.add(buttonDispose);
                }

            //-------------------------------------------------------------------------------------
            // Truncate Report View

            if ((toolbarstate.hasTruncateView())
                && (report.isTruncateable()))
                {
                final JButton buttonTruncate;

                buttonTruncate = new JButton();
                buttonTruncate.setBorderPainted(false);
                buttonTruncate.setIcon(RegistryModelUtilities.getCommonIcon(UIComponentPlugin.FILENAME_ICON_TRUNCATE));
                buttonTruncate.setToolTipText(TOOLTIP_TRUNCATE);

                buttonTruncate.addActionListener(new ActionListener()
                    {
                    public void actionPerformed(final ActionEvent event)
                        {
                        // If the report has a truncation method, it will give a message box
                        report.truncateReport();
                        report.refreshTable();
                        ObservatoryInstrumentHelper.runGarbageCollector();
                        }
                    });

                toolbar.add(buttonTruncate);
                }

            //-------------------------------------------------------------------------------------
            // PageSetup and Print

            if ((toolbarstate.hasPrinting())
                && (report.isPrintable()))
                {
                final String strTitle;

                final ContextAction actionPageSetup;
                final ContextAction actionPrint;
                final JButton buttonPageSetup;
                final JButton buttonPrint;

                buttonPrint = new JButton();
                buttonPrint.setBorderPainted(false);
                buttonPrint.setIcon(RegistryModelUtilities.getCommonIcon(UIComponentPlugin.FILENAME_ICON_PRINT));
                buttonPrint.setToolTipText(TOOLTIP_PRINT);

                buttonPrint.addActionListener(new ActionListener()
                    {
                    public void actionPerformed(final ActionEvent event)
                        {
                        doPrint(report);
                        }
                    });

                strTitle = report.getReportTabName();

                actionPageSetup = new ContextAction(ReportTablePlugin.PREFIX_PAGE_SETUP + strTitle,
                                                    RegistryModelUtilities.getCommonIcon(UIComponentPlugin.FILENAME_ICON_PAGE_SETUP),
                                                    ReportTablePlugin.PREFIX_PAGE_SETUP + strTitle,
                                                    KeyEvent.VK_S,
                                                    false,
                                                    true)
                    {
                    final static String SOURCE = "ContextAction:PageSetup ";
                    private static final long serialVersionUID = 6802400471966299436L;


                    public void actionPerformed(final ActionEvent event)
                        {
                        if ((report != null)
                            && (PrinterJob.lookupPrintServices().length > 0))
                            {
                            final PrinterJob printerJob;
                            final PageFormat pageFormatModified;

                            printerJob = PrinterJob.getPrinterJob();
                            pageFormatModified = printerJob.pageDialog(report.getPageFormat());

                            if (pageFormatModified != null)
                                {
                                report.setPageFormat(pageFormatModified);
                                }
                            }
                        else
                            {
                            LOGGER.error(SOURCE + "Report unexpectedly NULL, or no Print Services");
                            }
                        }
                    };

                buttonPageSetup = new JButton();
                buttonPageSetup.setBorder(UIComponentPlugin.BORDER_BUTTON);
                buttonPageSetup.setAction(actionPageSetup);
                buttonPageSetup.setHideActionText(true);
                buttonPageSetup.setToolTipText((String) actionPageSetup.getValue(Action.SHORT_DESCRIPTION));
                buttonPageSetup.setEnabled(true);

                toolbar.add(buttonPrint);
                toolbar.add(buttonPageSetup);
                }

            toolbar.add(Box.createVerticalGlue());
            NavigationUtilities.updateComponentTreeUI(toolbar);
            }

        return (textViewLimit);
        }


    /***********************************************************************************************
     * Add a default footer to the specified Report, beginning at the specified row index.
     * Be aware of ReportTablePlugin.HEADER_ROWS_PER_COLUMN.
     *
     * @param header
     * @param rowindex
     */

    public static void addDefaultFooter(final ObservatoryInstrumentInterface hostinstrument,
                                        final Vector<String> header,
                                        final int rowindex)
        {
        final int ADDED_ROW_COUNT = 7;

        for (int intRowSpacerIndex = 0;
             intRowSpacerIndex < (ReportTablePlugin.HEADER_ROWS_PER_COLUMN - rowindex - ADDED_ROW_COUNT);
             intRowSpacerIndex++)
            {
            header.add(EMPTY_STRING);
            }

        header.add("Framework.User " + REGISTRY_MODEL.getLoggedInUser().getName());
        header.add(ReportTablePlugin.FOOTER_WEBSITE);
        header.add(ReportTablePlugin.FOOTER_EMAIL);

        if ((REGISTRY.getVersionNumbers().containsKey(REGISTRY.getFramework().getName()))
            && (REGISTRY.getBuildNumbers().containsKey(REGISTRY.getFramework().getName()))
            && (REGISTRY.getBuildStatuses().containsKey(REGISTRY.getFramework().getName())))
            {
            header.add("Starbase.Version " + REGISTRY.getVersionNumbers().get(REGISTRY.getFramework().getName()));
            header.add("Starbase.Build " + REGISTRY.getBuildNumbers().get(REGISTRY.getFramework().getName()));
            header.add("Starbase.Status " + REGISTRY.getBuildStatuses().get(REGISTRY.getFramework().getName()));
            }

        header.add(ReportTablePlugin.MSG_REPORT_CREATED + hostinstrument.getObservatoryClock().getDateTimeNowAsString());
        }


    /***********************************************************************************************
     * Print the Report if possible.
     *
     * @param report
     */

    private static void doPrint(final ReportTablePlugin report)
        {
        // Check to see that we actually have a printer...
        if (PrinterJob.lookupPrintServices().length == 0)
            {
            JOptionPane.showMessageDialog(null,
                                          MSG_NO_PRINTER,
                                          "Print " + report.getReportTabName(),
                                          JOptionPane.WARNING_MESSAGE);
            return;
            }

        // Print the Report
        REGISTRY_MODEL.getUserInterface().getUI().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        report.printReport();
        REGISTRY_MODEL.getUserInterface().getUI().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }


    /***********************************************************************************************
     * Process the User's ViewLimit entry, and make sure it is correct before allowing the
     * DataViewMode to change.
     *
     * @param report
     * @param group
     * @param viewlimit
     * @param refresh
     */

    private static void processViewLimit(final ReportTablePlugin report,
                                         final ButtonGroup group,
                                         final JTextField viewlimit,
                                         final JButton refresh)
        {
        Enumeration<AbstractButton> enumButtons;

        // Disable the Report
        report.setDataViewMode(null);
        report.setDataViewLimit(-1);

        // Show the report as empty during limit selection
        report.refreshTable();

        // Deselect all DataViewMode buttons (Java 6 onwards)
        group.clearSelection();

        // Disable all DataViewMode buttons
        enumButtons = group.getElements();

        while (enumButtons.hasMoreElements())
            {
            final AbstractButton button;

            button = enumButtons.nextElement();
            button.setEnabled(false);
            }

        // Disable the Refresh button
        refresh.setEnabled(false);

        // Is the ViewLimit entry valid?
        try
            {
            final int intViewLimit;

            intViewLimit = Integer.parseInt(viewlimit.getText());

            // Allow {1 ... Integer.MAX_VALUE}
            if (intViewLimit < 1)
                {
                throw new NumberFormatException(EXCEPTION_VIEWLIMIT_RANGE);
                }

            viewlimit.setForeground(report.getTextColour().getColor());

            // Apply the new DataViewLimit to the Report
            // but wait for the User to select a DataViewMode
            report.setDataViewLimit(intViewLimit);

            // Re-enable all DataViewMode buttons
            enumButtons = group.getElements();

            while (enumButtons.hasMoreElements())
                {
                final AbstractButton button;

                button = enumButtons.nextElement();
                button.setEnabled(true);
                }

            // Re-enable the Refresh button
            refresh.setEnabled(true);
            }

        catch (final NumberFormatException exception)
            {
            Toolkit.getDefaultToolkit().beep();
            viewlimit.setForeground(Color.red);
            }
        }


    /***********************************************************************************************
     * Update the Report DataViewMode and DataViewLimit as a result of a change in the radio buttons
     * or text box, if appropriate for this context. Refresh the report only if anything has changed.
     *
     * @param report
     * @param buttongroup
     * @param viewmode
     * @param viewlimit
     */

    private static void updateDataView(final ReportTablePlugin report,
                                       final ButtonGroup buttongroup,
                                       final DataViewMode viewmode,
                                       final String viewlimit)
        {
        if ((report != null)
            && (buttongroup != null)
            && (viewmode != null)
            && (viewlimit != null))
            {
            final DataViewMode modePrevious;
            final int intViewLimitPrevious;
            int intViewLimit;

            // Save the previous mode, may be null
            modePrevious = report.getDataViewMode();
            intViewLimitPrevious = report.getDataViewLimit();

            // Apply the new DataViewMode to the Report
            report.setDataViewMode(viewmode);

            // Check that DataViewLimit is valid, regardless of the new DataViewMode
            // Use the default if not
            try
                {
                intViewLimit = Integer.parseInt(viewlimit);

                // Allow {1 ... Integer.MAX_VALUE}
                if (intViewLimit < 1)
                    {
                    throw new NumberFormatException(EXCEPTION_VIEWLIMIT_RANGE);
                    }
                }

            catch (final NumberFormatException exception)
                {
                Toolkit.getDefaultToolkit().beep();
                intViewLimit = ReportTablePlugin.DEFAULT_DATA_VIEW_LIMIT;
                }

            // Apply the new DataViewLimit to the Report
            report.setDataViewLimit(intViewLimit);

            // Refresh only if the mode or the number of lines to display have changed
            // Remember the previous mode may be null
            if (((modePrevious == null) && (report.getDataViewMode() != null))
                || ((modePrevious != null) && (!modePrevious.equals(report.getDataViewMode())))
                || (intViewLimitPrevious != report.getDataViewLimit()))
                {
                report.refreshTable();
                }
            }
        }


    /***********************************************************************************************
     * Draw a StringBuffer scaled to fit the available column width.
     *
     * @param graphics
     * @param buffer
     * @param scaledwidth
     * @param x
     * @param y
     */

    public synchronized static void drawScaledText(final Graphics graphics,
                                                   final StringBuffer buffer,
                                                   final int scaledwidth,
                                                   final int x,
                                                   final int y)
        {
        final FontMetrics metricsTrial;
        Rectangle2D rectangleTrial;

        if ((graphics != null)
            && (buffer != null)
            && (buffer.length() > 0)
            && (scaledwidth > 0))
            {
            metricsTrial = graphics.getFontMetrics(graphics.getFont());
            rectangleTrial = metricsTrial.getStringBounds(buffer.toString(),
                                                          graphics);

            // See if we have exceeded the dimensions of the space allowed
            // Leave a small gap between columns
            if (rectangleTrial.getWidth() <= (scaledwidth - 2))
                {
                // (x, y) is the baseline of the left-most character
                graphics.drawString(buffer.toString(),
                                    x,
                                    y);
                }
            else
                {
                final String strText;

                // Save the original text before scaling
                strText = buffer.toString();

                if (buffer.length() > 0)
                    {
                    // Prune the String until it fits!
                    do
                        {
                        buffer.deleteCharAt(buffer.length() - 1);

                        // Allow a little space at the end between columns
                        rectangleTrial = metricsTrial.getStringBounds(buffer.toString()
                                                                          + FrameworkStrings.ELLIPSIS,
                                                                      graphics);
                        }

                    while ((buffer.length() > 0)
                        && (rectangleTrial != null)
                        && (rectangleTrial.getWidth() > scaledwidth));

                    // Did we prune too much?!
                    if ((buffer.length() <= 2)
                        && (strText != null))
                        {
                        // Try again without the ellipsis
                        buffer.setLength(0);
                        buffer.append(strText);

                        // Prune the String until it fits!
                        do
                            {
                            buffer.deleteCharAt(buffer.length() - 1);

                            // Allow a little space at the end between columns
                            rectangleTrial = metricsTrial.getStringBounds(buffer.toString(),
                                                                          graphics);
                            }

                        while ((buffer.length() > 0)
                            && (rectangleTrial != null)
                            && (rectangleTrial.getWidth() > scaledwidth));

                        graphics.drawString(buffer.toString() + FrameworkStrings.DOT,
                                            x,
                                            y);
                        }
                    else
                        {
                        // Remember to add an ellipsis at the end...
                        graphics.drawString(buffer.toString() + FrameworkStrings.ELLIPSIS,
                                            x,
                                            y);
                        }
                    }
                else
                    {
                    // There's no String!
                    graphics.drawString(FrameworkStrings.ELLIPSIS,
                                        x,
                                        y);
                    }
                }
            }
        }


    /***********************************************************************************************
     * Add HTML tags to grey out a Table cell as required.
     *
     * @param content
     * @param flag
     *
     * @return String
     */

    public synchronized static String greyCell(final String content,
                                               final boolean flag)
        {
        if (flag)
            {
            return (GREY_CELL_PREFIX + content + GREY_CELL_SUFFIX);
            }
        else
            {
            return (content);
            }
        }


    /***********************************************************************************************
     * Add HTML tags to grey out a Table cell as required.
     *
     * @param content
     * @param colour
     * @param flag
     *
     * @return String
     */

    public synchronized static String colourCell(final String content,
                                                 final String colour,
                                                 final boolean flag)
        {
        if (flag)
            {
            return (COLOUR_CELL_PREFIX + colour + COLOUR_CELL_PREFIX_TERMINATOR + content + COLOUR_CELL_SUFFIX);
            }
        else
            {
            return (content);
            }
        }


    /***********************************************************************************************
     * Add HTML tags to highlight a Table cell as required.
     *
     * @param content
     * @param flag
     *
     * @return String
     */

    public synchronized static String highlightCell(final String content,
                                                    final boolean flag)
        {
        if (flag)
            {
            return (HIGHLIGHT_CELL_PREFIX + content + HIGHLIGHT_CELL_SUFFIX);
            }
        else
            {
            return (content);
            }
        }


    /***********************************************************************************************
     * Add the copyright messages to the Header.
     *
     * @param header
     * @param copyrights
     * @param title
     */

    public synchronized static void addCopyrights(final Vector<String> header,
                                                  final Vector<String> copyrights,
                                                  final String title)
        {
        if ((header != null)
            && (copyrights != null))
            {
            final Iterator<String> iterCopyrights;

            if ((title != null)
                && (!title.equals(FrameworkStrings.EMPTY_STRING)))
                {
                header.add(FrameworkStrings.SPACE);
                header.add(title);
                }

            iterCopyrights = copyrights.iterator();

            while (iterCopyrights.hasNext())
                {
                header.add(iterCopyrights.next());
                }
            }
        }


    /***********************************************************************************************
     * Scale the specified pixels according to the page width and the total column width.
     *
     * @param pagewidth
     * @param totalcolumnwidth
     * @param pixels
     *
     * @return int
     */

    public synchronized static int scale(final int pagewidth,
                                         final int totalcolumnwidth,
                                         final int pixels)
        {
        return ((int)(((double)pagewidth/(double)totalcolumnwidth) * pixels));
        }


    /***********************************************************************************************
     * Render a single table cell in HTML with an optional style.
     *
     * @param buffer
     * @param contents
     * @param css
     * @param align
     */

    public synchronized static void renderHTMLCell(final StringBuffer buffer,
                                                   final String contents,
                                                   final String css,
                                                   final int align)
        {
        String strAlign;

        strAlign = ALIGN_LEFT;

        if (align == SwingConstants.LEFT)
            {
            strAlign = ALIGN_LEFT;
            }
        else if (align == SwingConstants.RIGHT)
            {
            strAlign = ALIGN_RIGHT;
            }
        else if (align == SwingConstants.CENTER)
            {
            strAlign = ALIGN_CENTER;
            }

        buffer.append("<td");
        buffer.append(css);
        buffer.append(" align=");
        buffer.append(strAlign);
        buffer.append(">");
        buffer.append(CR_LF);
        buffer.append(contents);
        buffer.append(CR_LF);
        buffer.append("</td>");
        buffer.append(CR_LF);
        }


    /***********************************************************************************************
     * Write a plain text version of this Table to the specified filename.
     *
     * @param outputfile
     * @param tabledata
     *
     * @throws ReportException - if the file exists but is a directory rather than a file,
     * does not exist but cannot be created, or cannot be opened for any other reason
     */

    public synchronized static void toFile(final String outputfile,
                                           final String tabledata) throws ReportException
        {
        final File fileExport;
        final FileWriter writerOutput;

        try
            {
            fileExport = new File(outputfile);

            // Overwrite existing output file or create a new file
            if (fileExport.exists())
                {
                fileExport.delete();
                fileExport.createNewFile();
                }
            else
                {
                fileExport.createNewFile();
                }

            // Write the Table contents to the file
            writerOutput = new FileWriter(fileExport);
            writerOutput.write(tabledata);
            writerOutput.close();
            }

        catch (final IOException exception)
            {
            throw new ReportException(exception.getMessage(), exception);
            }
        }


    /***********************************************************************************************
     * Get a Report from the specified Tab on the JTabbedPane.
     * May return <code>null</code>.
     *
     * @param tabbedpane
     * @param index
     *
     * @return ReportTable
     */

    public static ReportTablePlugin getReportOnTab(final JTabbedPane tabbedpane,
                                                   final int index)
        {
        if ((tabbedpane != null)
            && (tabbedpane.getComponentAt(index) != null)
            && (tabbedpane.getComponentAt(index) instanceof ReportTablePlugin))
            {
            return ((ReportTablePlugin)tabbedpane.getComponentAt(index));
            }
        else
            {
            return (null);
            }
        }


    /***********************************************************************************************
     * Set a ReportTable on the specified Tab on the JTabbedPane.
     *
     * @param tabbedpane
     * @param index
     * @param report
     */

    public static void setReportOnTab(final JTabbedPane tabbedpane,
                                      final int index,
                                      final ReportTablePlugin report)
        {
        if (tabbedpane != null)
            {
            tabbedpane.setComponentAt(index, (Component)report);

            // This seems to be necessary in order to re-parse
            // any HTML used in the tab titles!
            NavigationUtilities.updateComponentTreeUI(tabbedpane);

            if ((report != null)
                && (tabbedpane.getComponentAt(index).isShowing()))
                {
                // This will start the Report Timer, if required
                report.runUI();
                }
            }
        }


    /***********************************************************************************************
     * Debug a Vector containing a single row of data.
     *
     * @param vector
     * @param debug
     */

    public static void debugVector(final Vector<Object> vector,
                                   final boolean debug)
        {
        if ((vector != null)
            && (vector.size() > 0)
            && (debug))
            {
            final Iterator<Object> iterObject;
            final StringBuffer buffer;

            iterObject = vector.iterator();
            buffer= new StringBuffer();

            while (iterObject.hasNext())
                {
                final Object item;

                item = iterObject.next();

                buffer.append(item);
                buffer.append("  ");
                }

            LOGGER.log(buffer.toString());
            }
        }
    }
