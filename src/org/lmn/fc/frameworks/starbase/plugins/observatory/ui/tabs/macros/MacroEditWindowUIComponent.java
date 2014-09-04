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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.macros;

import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.exceptions.ReportException;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.InstrumentState;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryClockInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.ObservatoryUIHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.MacroEditWindowInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.MacroStepProcessorInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandProcessorContextInterface;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.registry.RegistryModelUtilities;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.model.xmlbeans.datatypes.SchemaDataType;
import org.lmn.fc.model.xmlbeans.instruments.*;
import org.lmn.fc.model.xmlbeans.metadata.SchemaUnits;
import org.lmn.fc.ui.reports.ReportColumnMetadata;
import org.lmn.fc.ui.reports.ReportTable;
import org.lmn.fc.ui.reports.ReportTableToolbar;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.util.List;
import java.util.Vector;


/***************************************************************************************************
 * A general purpose MacroEditWindowUIComponent.
 */

public final class MacroEditWindowUIComponent extends ReportTable
                                              implements MacroEditWindowInterface
    {
    // String Resources
    private static final String REPORT_NAME = "MacroEditor";
    private static final String REPORT_HEADER = "MacroEditor Report created at";

    private static final String TITLE_STATUS = SPACE;  // Status Icon - Do not use EMPTY_STRING!
    private static final String TITLE_LABEL = "Label";
    private static final String TITLE_RESPONSE = "Response";
    private static final String TITLE_EQUALS = SPACE;
    private static final String TITLE_STARSCRIPT = "Starscript"; // Also used for Comment field

    private static final String LABEL_MACRO = "Macro";

    private static final String TOOLTIP_TEST  = "Click on a row to edit";

    private static final int COLUMN_COUNT = 5;
    private static final int DEFAULT_MACRO_LENGTH = 100;

    // Injections
    private final ObservatoryInstrumentInterface hostInstrument;

    // The MacroContext
    private CommandProcessorContextInterface macroContext;


    /***********************************************************************************************
     * Construct a MacroEditWindowUIComponent.
     * The ResourceKey is always that of the host Framework.
     *
     * @param task
     * @param hostinstrument
     * @param hostresourcekey
     * @param context
     */

    public MacroEditWindowUIComponent(final TaskPlugin task,
                                      final ObservatoryInstrumentInterface hostinstrument,
                                      final String hostresourcekey,
                                      final CommandProcessorContextInterface context)
        {
        super(task,
              REPORT_NAME,
              hostresourcekey,
              PRINTABLE,
              EXPORTABLE,
              NON_REFRESHABLE,
              REFRESH_NONE,
              NON_REORDERABLE,
              NON_TRUNCATEABLE,
              LOCK_TOP_ROW,
              SCROLL_LEFT_COLUMNS,
              0,
              ReportTableToolbar.NONE,
              null);

        // Injections
        this.hostInstrument = hostinstrument;
        this.macroContext = context;

        // Uniquely identify this UIComponent
        if ((hostinstrument != null)
            && (hostinstrument.getInstrument() != null))
            {
            setReportUniqueName(hostinstrument.getInstrument().getName()+ SPACE + getReportTabName());
            }
        }


    /***********************************************************************************************
     * initialiseUI().
     */

    public synchronized void initialiseUI()
        {
        super.initialiseUI();

        setShowGrid(false);
        }


    /***********************************************************************************************
     * Dispose of all UI components and remove the Toolbar Actions.
     */

    public synchronized void disposeUI()
        {
        super.disposeUI();

        if (getMacroContext() != null)
            {
            setMacroContext(null);
            }
        }


    /***********************************************************************************************
     * Override the ReportTable.initialiseReport().
     *
     * @return JTable
     *
     * @throws ReportException
     */

    public synchronized JTable initialiseReport() throws ReportException
        {
        final String SOURCE = "initialiseReport() ";
        final JTable tableReport;

        tableReport = super.initialiseReport();

        if (tableReport != null)
            {
            tableReport.setToolTipText(TOOLTIP_TEST);

            // Listen for clicks on each Row
            tableReport.getSelectionModel().addListSelectionListener(new ListSelectionListener()
                {
                public void valueChanged(final ListSelectionEvent event)
                    {
                    final ListSelectionModel modelSelection;

                    // Ignore extra messages
                    if (event.getValueIsAdjusting())
                        {
                        return;
                        }

                    modelSelection = (ListSelectionModel)event.getSource();

                    // Remember to update the underlying ReportTable otherwise the selection will be empty
                    if (!modelSelection.isSelectionEmpty())
                        {
                        final int intRowSelected;

                        intRowSelected = modelSelection.getMinSelectionIndex();

                        // Retrieve the Data from this row
                        if ((getReportTableModel() != null)
                            && (getReportTableModel().getRowCount() > 0))
                            {
                            final Vector vecSelectedRow;

                            vecSelectedRow = getReportTableModel().getRowAt(intRowSelected);

                            if (vecSelectedRow != null)
                                {
                                // The row selected is intRowSelected
                                // The Step index is intRowSelected - 1
                                // since the first row is the Macro definition
                                // Debug the selected Step index if possible
                                // We can't select a row past the end
                                if (intRowSelected > 0)
                                    {
                                    MacroManagerUtilities.debugMacroStep(getMacro(), intRowSelected - 1);
                                    }
                                else
                                    {
                                    Toolkit.getDefaultToolkit().beep();
                                    }
                                }
                            else
                                {
                                LOGGER.error(SOURCE + "Null edit row");
                                }
                            }
                        else
                            {
                            LOGGER.error(SOURCE + "Empty editor");
                            }
                        }
                    else
                        {
                        LOGGER.error(SOURCE + "Empty editor selection");
                        }
                    }
                });

            tableReport.setRowSelectionAllowed(true);
            tableReport.setSelectionBackground(COLOR_SELECTION_BACKGROUND);
            tableReport.setSelectionForeground(COLOR_SELECTION_FOREGROUND);
            }

        return (tableReport);
        }


    /***********************************************************************************************
     * Generate the report header.
     *
     * @return Vector
     */

    public synchronized final Vector<String> generateHeader()
        {
        final Vector<String> vecHeader;

        vecHeader = new Vector<String>(1);

        vecHeader.add(REPORT_HEADER + SPACE + getObservatoryClock().getDateTimeNowAsString());

        return (vecHeader);
        }


    /***********************************************************************************************
     * Define the report columns.
     *
     * @return Vector
     */

    public synchronized final Vector<ReportColumnMetadata> defineColumns()
        {
        final Vector<ReportColumnMetadata> vecColumns;

        vecColumns = new Vector<ReportColumnMetadata>(defineColumnWidths().length);

        vecColumns.add(new ReportColumnMetadata(TITLE_STATUS,
                                                SchemaDataType.STRING,
                                                SchemaUnits.DIMENSIONLESS,
                                                "The status of the Macro or Step",
                                                SwingConstants.CENTER));
        vecColumns.add(new ReportColumnMetadata(TITLE_LABEL,
                                                SchemaDataType.STRING,
                                                SchemaUnits.DIMENSIONLESS,
                                                "The Macro or Step Label (optional)",
                                                SwingConstants.LEFT ));
        vecColumns.add(new ReportColumnMetadata(TITLE_RESPONSE,
                                                SchemaDataType.STRING,
                                                SchemaUnits.DIMENSIONLESS,
                                                "The Response returned by executing the Macro or Step",
                                                SwingConstants.RIGHT ));
        vecColumns.add(new ReportColumnMetadata(TITLE_EQUALS,
                                                SchemaDataType.STRING,
                                                SchemaUnits.DIMENSIONLESS,
                                                SPACE,
                                                SwingConstants.CENTER ));
        vecColumns.add(new ReportColumnMetadata(TITLE_STARSCRIPT,
                                                SchemaDataType.STRING,
                                                SchemaUnits.DIMENSIONLESS,
                                                "The Starscript expression of the Macro or Step",
                                                SwingConstants.LEFT ));
        return (vecColumns);
        }


    /***********************************************************************************************
     * Define the widths of each column in terms of the objects which they will contain.
     *
     * @return Object []
     */

    public synchronized final Object [] defineColumnWidths()
        {
        final Object [] columnWidths;

        columnWidths = new Object[]
            {
            IMAGE_ICON_PLAIN,
            "MMMMMMM",
            "MMMMMMM.MMMMMM",
            "=",
            "MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM"
            };

        return (columnWidths);
        }


    /***********************************************************************************************
     * Generate the report data table.
     *
     * @return Vector of report rows
     */

    public final Vector<Vector> generateReport()
        {
        final Vector<Vector> vecReport;

        // System.out.println("WINDOW generate report");
        LOGGER.debugTimedEvent(FrameworkSingletons.LOADER_PROPERTIES.isTimingDebug(),
                               "MacroEditWindowUIComponent.generateReport() [isselectedinstrument="
                                + ObservatoryUIHelper.isSelectedInstrument(getHostInstrument()) + "]");

        // System.out.println("WINDOW generateReport selected " + (ObservatoryUIHelper.isSelectedInstrument(getHostInstrument())));
//        // System.out.println("generateReport report not null " + (getReportTable() != null));
//
//        if (getReportTable() != null)
//            {
//            // System.out.println("generateReport uicomp table header showing " + (UIComponent.isComponentShowing(getReportTable().getTableHeader())));
//            }

        // Only generate a Report if this UIComponent is showing
        // Trap the case of disposeUI(), when there won't be a ReportTable
        if ((ObservatoryUIHelper.isSelectedInstrument(getHostInstrument()))
//            && (getReportTable() != null)
//            && (UIComponent.isComponentShowing(getReportTable().getTableHeader())))
        )
            {
            LOGGER.debugTimedEvent(FrameworkSingletons.LOADER_PROPERTIES.isTimingDebug(),
                                   "MacroEditWindowUIComponent.generateReport() SHOWING");
            //System.out.println("****************** A MacroEditWindowUIComponent.generateReport() SHOWING");

            vecReport = generateRawReport();
            }
        else
            {
            LOGGER.debugTimedEvent(FrameworkSingletons.LOADER_PROPERTIES.isTimingDebug(),
                                   "MacroEditWindowUIComponent.generateReport() NOT SHOWING");
            // System.out.println("B MacroEditWindowUIComponent.generateReport() NOT SHOWING");

            vecReport = new Vector<Vector>(1);
            }

        return (vecReport);
        }


    /***********************************************************************************************
     * Generate the raw Report, i.e not truncated,
     * and regardless of whether the component is visible. This is used for e.g. exports.
     *
     * @return Vector<Vector>
     *
     * @throws ReportException
     */

    public Vector<Vector> generateRawReport() throws ReportException
        {
        final String SOURCE = "MacroEditWindowUIComponent.generateRawReport() ";
        final Vector<Vector> vecReport;

        LOGGER.debugTimedEvent(FrameworkSingletons.LOADER_PROPERTIES.isTimingDebug(),
                               "MacroEditWindowUIComponent.generateRawReport()");

         //System.out.println(SOURCE + "RAW REPORT start ++++++++++++++++++++++++++++++++++++++++++++");
        vecReport = new Vector<Vector>(DEFAULT_MACRO_LENGTH);

         //System.out.println(SOURCE + "macro context not null " + (getMacroContext() != null));
         //System.out.println(SOURCE + "macro not null " + (getMacro() != null));

        if (getMacroContext() != null)
            {
             //System.out.println(SOURCE + "selected module not null " + (getMacroContext().getSelectedModule() != null));
            }

        if ((getMacro() != null)
            && (getMacroContext() != null)
            && (getMacroContext().getSelectedModule() != null)
            && (getMacroContext().getSelectedModule().size() == 1))
            {
            final Vector<Object> vecMacroRow;
            final String strMacroStarscript;
            final List<StepType> listSteps;

            //System.out.println("WINDOW make report");
            //--------------------------------------------------------------------------------------
            // Firstly show the Macro definition, with Parameters

            vecMacroRow = new Vector<Object>(defineColumnWidths().length);

            // The Status Icon
            vecMacroRow.add(IMAGE_ICON_MACRO);

            // Macro Label
            vecMacroRow.add(LABEL_MACRO);

            // Response, Equals
            if (getMacro().getResponse() != null)
                {
                vecMacroRow.add(HTML_PREFIX_BOLD + getMacro().getResponse().getName() + HTML_SUFFIX_BOLD);
                vecMacroRow.add(HTML_PREFIX_BOLD + EQUALS + HTML_SUFFIX_BOLD);
                }
            else if (getMacro().getAck() != null)
                {
                vecMacroRow.add(HTML_PREFIX_BOLD + RESPONSE_ACK + HTML_SUFFIX_BOLD);
                vecMacroRow.add(HTML_PREFIX_BOLD + EQUALS + HTML_SUFFIX_BOLD);
                }
            else
                {
                vecMacroRow.add(SPACE);
                vecMacroRow.add(SPACE);
                }

            // Starscript
            strMacroStarscript = MacroManagerUtilities.buildMacroStarscript(getHostInstrument().getInstrument(),
                                                                                getMacroContext().getSelectedModule().get(0),
                                                                                getMacro());
            vecMacroRow.add(HTML_PREFIX_BOLD + strMacroStarscript + HTML_SUFFIX_BOLD);

            // Description
            vecMacroRow.add(HTML_PREFIX_MACRO_DESCRIPTION + getMacro().getDescription() + HTML_SUFFIX_MACRO_DESCRIPTION);

            // Add to the Report
            vecReport.add(vecMacroRow);

            //--------------------------------------------------------------------------------------
            // Now the individual Macro Steps

            listSteps = getMacro().getStepList();

            for (int i = 0;
                 i < listSteps.size();
                 i++)
                {
                final StepType step;
                final Vector<Object> vecStepRow;

                vecStepRow = new Vector<Object>(defineColumnWidths().length);

                step = listSteps.get(i);

                // The Status Icon
                vecStepRow.add(RegistryModelUtilities.getCommonIcon(EventStatus.PLAIN.getIconFilename()));

                // The Label if present
                if (step.getLabel() != null)
                    {
                    vecStepRow.add(HTML_PREFIX_MACRO_LABEL + step.getLabel() + HTML_SUFFIX_MACRO_LABEL);
                    }
                else
                    {
                    vecStepRow.add(SPACE);
                    }

                // The Comment if present, in the Starscript field
                if (step.getComment() != null)
                    {
                    // Step over Response and Equals
                    vecStepRow.add(SPACE);
                    vecStepRow.add(SPACE);
                    vecStepRow.add(HTML_PREFIX_MACRO_COMMENT + step.getComment() + HTML_SUFFIX_MACRO_COMMENT);
                    }
                else if (step.getStarscript() != null)
                    {
                    final MacroStepProcessorInterface processor;
                    final CommandType cmdStep;

                    processor = new MacroStepProcessor(getMacroContext(), step);
                    cmdStep = processor.locateStepContext();

                    if (cmdStep != null)
                        {
                        final StepCommandType stepCommand;
                        final StringBuffer bufferStarscript;

                        // Response could be just an Ack, or have a Type
                        if (cmdStep.getResponse() != null)
                            {
                            vecStepRow.add(cmdStep.getResponse().getName());
                            }
                        else if (cmdStep.getAck() != null)
                            {
                            vecStepRow.add(RESPONSE_ACK);
                            }

                        // Equals to make it look pretty
                        vecStepRow.add(EQUALS);

                        // Now the full Starscript
                        stepCommand = step.getStarscript();
                        bufferStarscript = new StringBuffer();

                        bufferStarscript.append(HTML_PREFIX);

                        if (stepCommand.getInstrument() != null)
                            {
                            bufferStarscript.append(stepCommand.getInstrument());

                            if (stepCommand.getModule() != null)
                                {
                                bufferStarscript.append(".");
                                bufferStarscript.append(stepCommand.getModule());

                                if ((stepCommand.getCommand() != null)
                                    && (stepCommand.getCommand().getIdentifier() != null))
                                    {
                                    bufferStarscript.append(".");
                                    bufferStarscript.append(stepCommand.getCommand().getIdentifier());

                                    if ((stepCommand.getCommand().getParameterList() != null)
                                        && (!stepCommand.getCommand().getParameterList().isEmpty()))
                                        {
                                        final List<MacroParameterType> listParameters;

                                        listParameters = stepCommand.getCommand().getParameterList();
                                        bufferStarscript.append("(");

                                        for (int j = 0;
                                             j < listParameters.size();
                                             j++)
                                            {
                                            final MacroParameterType parameter;

                                            parameter = listParameters.get(j);
                                            bufferStarscript.append(HTML_PREFIX_MACRO_PARAMETER);
                                            bufferStarscript.append(parameter.getToken());
                                            bufferStarscript.append(HTML_SUFFIX_MACRO_PARAMETER);

                                            if (j < listParameters.size() - 1)
                                                {
                                                bufferStarscript.append(", ");
                                                }
                                            }

                                        bufferStarscript.append(")");
                                        }
                                    else
                                        {
                                        bufferStarscript.append("()");
                                        }
                                    }
                                }

                            bufferStarscript.append(HTML_SUFFIX);
                            }
                        else
                            {
                            bufferStarscript.append(QUERY);
                            }

                        vecStepRow.add(bufferStarscript.toString());
                        }
                    else
                        {
                        LOGGER.error(SOURCE + "Corrupt Macro - unable to find Command corresponding to Starscript ");
                        }
                    }

                vecReport.add(vecStepRow);
                }
            }
        else
            {
            LOGGER.error(SOURCE + "Unable to display the Macro");
            }

        return (vecReport);
        }


    /***********************************************************************************************
     * Refresh the Report data table.
     *
     * @return Vector
     */

    public synchronized final Vector<Vector> refreshReport()
        {
        // System.out.println("WINDOW refresh report");
        return (generateReport());
        }


    /***********************************************************************************************
     * Get the MacroContext being displayed.
     * Return NULL if none.
     *
     * @return MacroType
     */

    public CommandProcessorContextInterface getMacroContext()
        {
        return (this.macroContext);
        }


    /***********************************************************************************************
     * Set the MacroContext to display.
     *
     * @param context
     */

    public void setMacroContext(final CommandProcessorContextInterface context)
        {
        this.macroContext = context;
        //System.out.println("MACRO EDIT WINDOW got context " + (macroContext != null));

        macroChanged();
        }


    /***********************************************************************************************
     * Get a copied version of the currently selected Macro.
     *
     * @return MacroType
     */

    private MacroType getMacro()
        {
        MacroType macro;

        macro = null;

        if ((getMacroContext() != null)
            && (getMacroContext().getSelectedMacro() != null)
            && (getMacroContext().getSelectedMacro().size() == 1))
            {
            macro = (MacroType)getMacroContext().getSelectedMacro().get(0).copy();


//             System.out.println("WINDOW getMacro()************************************************WINDOW");
//            MacroManagerUtilities.debugMacro(macro);
//             System.out.println("WINDOW getMacro()************************************************WINDOW");
            }

        return (macro);
        }


    /***********************************************************************************************
     * Indicate that there has been a change of Macro.
     */

    public synchronized final void macroChanged()
        {
        // System.out.println("WINDOW macro changed");
        // Force an immediate update using the new data, if any
        // Trap the case of disposeUI(), when there won't be a ReportTable

        if ((getMacroContext() != null)
            && (getMacroContext().getSelectedMacro() != null)
            && (getMacroContext().getSelectedMacro().size() == 1))
            {
            //System.out.println("WINDOW************************************************WINDOW");
            MacroManagerUtilities.debugMacro(getMacroContext().getSelectedMacro().get(0));
            //System.out.println("WINDOW************************************************WINDOW");
            }
        else
            {
            //System.out.println("WINDOW macro context invalid in window");
            }

//        System.out.println("WINDOW context not null " + (getMacroContext() != null));
//        System.out.println("WINDOW doing something " + (InstrumentState.isDoingSomething(getHostInstrument())));
//        System.out.println("WINDOW selected " + (ObservatoryUIHelper.isSelectedInstrument(getHostInstrument())));
//        System.out.println("WINDOW report not null " + (getReportTable() != null));

//        if (getReportTable() != null)
//            {
//            System.out.println("uicomp table header showing " + (UIComponent.isComponentShowing(
//                    getReportTable().getTableHeader())));
//            }

        if ((getMacroContext() != null)
            && (InstrumentState.isDoingSomething(getHostInstrument()))
            && ((ObservatoryUIHelper.isSelectedInstrument(getHostInstrument()))
//            && (getReportTable() != null)
//            && (UIComponent.isComponentShowing(getReportTable().getTableHeader()))))
        ))
            {
            //System.out.println("WINDOW refresh edit table");
            refreshTable();
            }
        else
            {
            //System.out.println("WINDOW can't refresh");
            }
        }


    /***********************************************************************************************
     * Read all the Resources required by the MacroEditWindowUIComponent.
     */

    public final void readResources()
        {
        super.readResources();
        }


    /***********************************************************************************************
     * Get the ObservatoryInstrument to which this UIComponent is attached.
     *
     * @return ObservatoryInstrumentInterface
     */

    private synchronized ObservatoryInstrumentInterface getHostInstrument()
        {
        return (this.hostInstrument);
        }


    /***********************************************************************************************
     * Get the ObservatoryClock.
     *
     * @return ObservatoryClockInterface
     */

    private ObservatoryClockInterface getObservatoryClock()
        {
        final ObservatoryClockInterface clock;

        clock = getHostInstrument().getObservatoryClock();

        return (clock);
        }
    }
