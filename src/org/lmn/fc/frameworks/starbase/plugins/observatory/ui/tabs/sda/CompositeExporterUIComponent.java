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
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.ExecuteCommandHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.ActivityIndicatorUIComponentInterface;
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
 * CompositeExporterUIComponent.
 */

public final class CompositeExporterUIComponent extends ExecuteCommandUIComponent
    {
    // String Resources
    private static final String MSG_COMPOSITE_EXPORTER = "Composite Exporter";
    private static final String TOOLTIP_COMPOSITE_VIEWER = "Show the Composite Viewer";
    private static final String ICON_COMPOSITE_VIEWER = "toolbar-dataset-viewer.png";
    private static final String MODULE_EXPORTER = "Exporter";
    private static final String COMMAND_EXPORT_CHART = "exportChart";


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
        final String SOURCE = "CompositeExporterUIComponent.createExecuteButton() ";
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

        buttonExecute.addActionListener(new ActionListener()
            {
            public synchronized void actionPerformed(final ActionEvent e)
                {
                final List<String> errors;

                errors = new ArrayList<String>(10);

                // We can only execute if currently in READY
                // It is not necessary to check for 'unavailable'
                if ((sdaui.getCompositeExporter() != null)
                    && (sdaui.getCompositeExporter().getExecutionContext() != null)
                    && (sdaui.getCompositeExporter().getExecutionContext().getObservatoryInstrument() != null)
                    && (sdaui.getCompositeExporter().getExecutionContext().getObservatoryInstrument().getDAO() != null)
                    && (InstrumentState.READY.equals(sdaui.getHostInstrument().getInstrumentState())))
                    {
                    final ExecutionContextInterface context;

                    context = sdaui.getCompositeExporter().getExecutionContext();
                    context.setExecutionDAO(sdaui.getCompositeDAO());

                    // We can execute a Local Command at any time
                    // However, if there is a Port, check that it is not busy before trying to Execute a SendToPort Command
                    // TODO Allow execution of Macros!

                    if ((ExecutionContext.isExecutable(context))
                        && (context.getStarscriptMacro() == null))
                        {
                        final String strStarscript;
                        final boolean boolSuccess;

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
                    ExecuteCommandHelper.handleErrors(sdaui.getCompositeExporter().getExecutionContext(),
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
                                              final ExecuteCommandUIComponent executeui)
        {
        final String SOURCE = "CompositeExporterUIComponent.createViewerButton() ";
        final ContextAction actionViewer;
        final JButton buttonViewer;

        buttonViewer = new JButton();
        buttonViewer.setBorder(BORDER_BUTTON);

        // Ensure that no text appears next to the Icon...
        buttonViewer.setHideActionText(true);

        // Switch back to Dataset Viewer
        actionViewer = new ContextAction("Viewer",
                                         RegistryModelUtilities.getAtomIcon(executeui.getObservatoryInstrument().getHostAtom(),
                                                                            ICON_COMPOSITE_VIEWER),
                                         TOOLTIP_COMPOSITE_VIEWER,
                                         KeyEvent.VK_V,
                                         false,
                                         true)
            {
            public void actionPerformed(final ActionEvent event)
                {
                // Clicking this button must switch the displayed panel to the CompositeChart Viewer,
                // which could be showing the Chart or the Metadata, but force the Chart
                // This calls refreshChart()
                SuperposedDataAnalyserHelper.switchCompositeViewerDisplayMode(sdaui, SuperposedDataAnalyserDisplayMode.COMPOSITE_VIEWER_CHART);
                }
            };

        buttonViewer.setAction(actionViewer);
        buttonViewer.setToolTipText((String) actionViewer.getValue(Action.SHORT_DESCRIPTION));
        // Viewer is always enabled

        return (buttonViewer);
        }


    /***********************************************************************************************
     * Construct a CompositeExporterUIComponent.
     *
     * @param obsinstrument
     * @param sdaui
     * @param fontdata
     * @param colourdata
     */

    public CompositeExporterUIComponent(final ObservatoryInstrumentInterface obsinstrument,
                                        final SuperposedDataAnalyserUIComponentInterface sdaui,
                                        final FontInterface fontdata,
                                        final ColourInterface colourdata)
        {
        super(obsinstrument,
              obsinstrument.getInstrument().getIdentifier(),
              MODULE_EXPORTER,
              COMMAND_EXPORT_CHART,
              fontdata,
              colourdata);

        this.uiSDA = sdaui;
        }


    /***********************************************************************************************
     * Initialise the Composite Exporter Toolbar.
     */

    public void initialiseToolbar()
        {
        final String SOURCE = "CompositeExporterUIComponent.initialiseToolbar() ";
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
