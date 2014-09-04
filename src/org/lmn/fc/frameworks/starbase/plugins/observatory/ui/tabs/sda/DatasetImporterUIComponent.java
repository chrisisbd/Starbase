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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.sda;

import org.lmn.fc.common.actions.ContextAction;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.InstrumentState;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.DAOHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.ExecuteCommandHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.ActivityIndicatorUIComponentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.ExecuteCommandUIComponentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.ExecutionContextInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.SuperposedDataAnalyserUIComponentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.commands.ExecutionContext;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.commands.StarscriptHelper;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.model.registry.NavigationUtilities;
import org.lmn.fc.model.registry.RegistryModelUtilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import static org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.commands.CommanderToolbarHelper.TITLE_DIALOG_COMMAND_EXECUTION;


/***************************************************************************************************
 * DatasetImporterUIComponent.
 */

public final class DatasetImporterUIComponent extends ExecuteCommandUIComponent
    {
    // String Resources
    private static final String MSG_DATASET_IMPORTER = "Dataset Importer";
    private static final String TOOLTIP_DATASET_VIEWER = "Show the Dataset Viewer";
    public static final String ICON_DATASET_VIEWER = "toolbar-dataset-viewer.png";
    private static final String MODULE_IMPORTER = "Importer";
    private static final String COMMAND_IMPORT_RAW_DATA_LOCAL = "importRawDataLocal";


    // Injections
    private final SuperposedDataAnalyserUIComponentInterface uiSDA;


    /***********************************************************************************************
     * Create the Command Execute button.
     *
     * @param sdaui
     *
     * @return JButton
     */

    private static JButton createExecuteButton(final SuperposedDataAnalyserUIComponentInterface sdaui)
        {
        final String SOURCE = "DatasetImporterUIComponent.createExecuteButton() ";
        final JButton buttonExecute;

        // The Execute button
        buttonExecute = new JButton(BUTTON_EXECUTE);

        buttonExecute.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttonExecute.setAlignmentY(Component.CENTER_ALIGNMENT);
        buttonExecute.setMinimumSize(DIM_BUTTON_TOOLBAR_EXECUTE);
        buttonExecute.setMaximumSize(DIM_BUTTON_TOOLBAR_EXECUTE);
        buttonExecute.setPreferredSize(DIM_BUTTON_TOOLBAR_EXECUTE);
        buttonExecute.setFont(sdaui.getFontData().getFont());
        buttonExecute.setForeground(sdaui.getColourData().getColor());

        buttonExecute.setEnabled(false);
        buttonExecute.setToolTipText(ObservatoryInstrumentInterface.TOOLTIP_COMMAND_EXECUTE);

//        if (!InstrumentState.isDoingSomething(sdaui.getHostInstrument()))
//            {
//            buttonExecute.setToolTipText(ObservatoryInstrumentInterface.TOOLTIP_STOPPED);
//            }

        buttonExecute.addActionListener(new ActionListener()
            {
            public synchronized void actionPerformed(final ActionEvent e)
                {
                final List<String> errors;

                errors = new ArrayList<String>(10);

                // We can only execute if currently in READY
                // It is not necessary to check for 'unavailable'
                if ((sdaui.getDatasetImporter() != null)
                    && (sdaui.getDatasetImporter().getExecutionContext() != null)
                    && (sdaui.getDatasetImporter().getExecutionContext().getObservatoryInstrument() != null)
                    && (sdaui.getDatasetImporter().getExecutionContext().getObservatoryInstrument().getDAO() != null)
                    && (InstrumentState.READY.equals(sdaui.getHostInstrument().getInstrumentState())))
                    {
                    final ExecutionContextInterface context;
                    final ObservatoryInstrumentDAOInterface daoImport;

                    context = sdaui.getDatasetImporter().getExecutionContext();

                    // Instantiate a new DAO to execute the Command
                    daoImport = DAOHelper.instantiateDAO(context.getObservatoryInstrument(),
                                                         SuperposedDataAnalyserUIComponentInterface.CLASSNAME_DAO_IMPORT);
                    context.setExecutionDAO(daoImport);

                    // We can execute a Local Command at any time
                    // However, if there is a Port, check that it is not busy before trying to Execute a SendToPort Command
                    // TODO Allow execution of Macros!

                    if ((ExecutionContext.isExecutable(context))
                        && (context.getStarscriptMacro() == null))
                        {
                        final String strStarscript;
                        boolean boolSuccess;

                        // Prevent further Command execution for a while
                        context.getExecuteButton().setEnabled(false);

                        if (context.getRepeatButton() != null)
                            {
                            context.getRepeatButton().setEnabled(false);
                            }

                        // Update the Command to show any changes from the parsers
                        // This will disable the Abort button
                        StarscriptHelper.updateStarscript(context);

                        if (context.getAbortButton() != null)
                            {
                            context.getAbortButton().setEnabled(true);
                            }

                        strStarscript = StarscriptHelper.buildExpandedStarscript(context, false);

                        // Initialise the DAO for execution
                        // Use the same ResourceKey so that we use the host Instrument's Properties
                        // for every dataset DAO, otherwise it gets very complicated
                        boolSuccess = context.getExecutionDAO().initialiseDAO(context.getObservatoryInstrument().getResourceKey());

                        if (boolSuccess)
                            {
                            // Attempt to execute the Command using the specified DAO, on another Thread
                            // These parameters must be valid if the prepared Command is valid
                            // This is a single execution of the Command, i.e. not repeating
                            boolSuccess = ExecuteCommandHelper.executeCommandOnDAO(context.getObservatoryInstrument(),
                                                                                   context.getExecutionDAO(),
                                                                                   context.getStarscriptInstrument(),
                                                                                   context.getStarscriptModule(),
                                                                                   context.getStarscriptCommand(),
                                                                                   context.getStarscriptExecutionParameters(),
                                                                                   strStarscript,
                                                                                   false,
                                                                                   0,
                                                                                   EMPTY_STRING,
                                                                                   errors);
                            }

                        if (!boolSuccess)
                            {
                            // We couldn't execute it, or it failed
                            JOptionPane.showMessageDialog(null,
                                                          errors.toArray(),
                                                          context.getStarscriptInstrument().getName()
                                                            + TITLE_DIALOG_COMMAND_EXECUTION,
                                                          JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    else
                        {
                        // The Port is still busy doing something else
                        errors.add(ObservatoryInstrumentInterface.PORT_BUSY);
                        JOptionPane.showMessageDialog(null,
                                                      errors.toArray(),
                                                      context.getStarscriptInstrument().getName()
                                                        + TITLE_DIALOG_COMMAND_EXECUTION,
                                                      JOptionPane.ERROR_MESSAGE);
                        }
                    }
                else
                    {
                    // Leave the Execute button enabled, so that the User can try again
                    // Are there any errors?
                    ExecuteCommandHelper.handleErrors(sdaui.getDatasetImporter().getExecutionContext(),
                                                      errors,
                                                      TITLE_DIALOG_COMMAND_EXECUTION);
                    }

                LOGGER.errors(SOURCE, errors);
                }
            });

        return (buttonExecute);
        }


    /***********************************************************************************************
     * Create the Viewer button.
     *
     * @param sdaui
     * @param executeui
     *
     * @return JButton
     */

    private static JButton createViewerButton(final SuperposedDataAnalyserUIComponentInterface sdaui,
                                              final ExecuteCommandUIComponentInterface executeui)
        {
        final String SOURCE = "DatasetImporterUIComponent.createViewerButton() ";
        final ContextAction actionViewer;
        final JButton buttonViewer;

        buttonViewer = new JButton();
        buttonViewer.setBorder(BORDER_BUTTON);

        // Ensure that no text appears next to the Icon...
        buttonViewer.setHideActionText(true);

        // Switch back to Dataset Viewer
        actionViewer = new ContextAction("Viewer",
                                         RegistryModelUtilities.getAtomIcon(executeui.getObservatoryInstrument().getHostAtom(),
                                                                            ICON_DATASET_VIEWER),
                                         TOOLTIP_DATASET_VIEWER,
                                         KeyEvent.VK_V,
                                         false,
                                         true)
            {
            public void actionPerformed(final ActionEvent event)
                {
                // Clicking this button must switch the displayed panel to the DatasetViewer,
                // which could be showing the Chart or the Metadata, but force the Chart
                // This calls refreshChart()
                SuperposedDataAnalyserHelper.switchDatasetViewerDisplayMode(sdaui, SuperposedDataAnalyserDisplayMode.DATASET_VIEWER_CHART);
                }
            };

        buttonViewer.setAction(actionViewer);
        buttonViewer.setToolTipText((String) actionViewer.getValue(Action.SHORT_DESCRIPTION));
        // Viewer is always enabled

        return (buttonViewer);
        }


    /***********************************************************************************************
     * Construct a DatasetImporterUIComponent.
     *
     * @param obsinstrument
     * @param sdaui
     * @param fontdata
     * @param colourdata
     */

    public DatasetImporterUIComponent(final ObservatoryInstrumentInterface obsinstrument,
                                      final SuperposedDataAnalyserUIComponentInterface sdaui,
                                      final FontInterface fontdata,
                                      final ColourInterface colourdata)
        {
        super(obsinstrument,
              obsinstrument.getInstrument().getIdentifier(),
              MODULE_IMPORTER,
              COMMAND_IMPORT_RAW_DATA_LOCAL,
              fontdata,
              colourdata);

        this.uiSDA = sdaui;
        }


    /***********************************************************************************************
     * Initialise the Dataset Importer Toolbar.
     */

    public void initialiseToolbar()
        {
        final String SOURCE = "DatasetImporterUIComponent.initialiseToolbar() ";
        final ActivityIndicatorUIComponentInterface activityUI;

        // Add the Status Indicator and Command buttons
        // Activity, Status, Execute, Abort

        // Activity Indicator must be initialised,
        // because it is a UIComponent - it may get complicated later on!
        activityUI = createActivityIndicator(this);
        activityUI.initialiseUI();
        setActivityIndicator(activityUI);

        // Also attach to the Instrument Context
        getObservatoryInstrument().getContext().addActivityIndicator(activityUI);

        setExecuteButton(createExecuteButton(getSdaUI()));
        setAbortButton(createAbortButton(this));
        setViewerButton(createViewerButton(getSdaUI(), this));

        // Put it all together
        getToolBar().removeAll();

        getToolBar().add(Box.createHorizontalStrut(3));
        getToolBar().add((Component) getActivityIndicator());
        getToolBar().add(Box.createHorizontalStrut(9));
        getToolBar().add(getExecuteButton());
        getToolBar().add(Box.createHorizontalStrut(5));
        getToolBar().add(getAbortButton());

        getToolBar().add(Box.createHorizontalGlue());

        getToolBar().add(getViewerButton());
        getToolBar().addSeparator(DIM_TOOLBAR_SEPARATOR);

        NavigationUtilities.updateComponentTreeUI(getToolBar());
        }


    /**********************************************************************************************/
    /* Injections                                                                                 */
    /***********************************************************************************************
     * Get the host SuperposedDataAnalyser UIComponent.
     *
     * @return SuperposedDataAnalyserUIComponentInterface
     */

    private SuperposedDataAnalyserUIComponentInterface getSdaUI()
        {
        return (this.uiSDA);
        }
    }
