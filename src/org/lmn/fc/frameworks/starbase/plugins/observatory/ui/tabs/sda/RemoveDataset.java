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
import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.DatasetManagerInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.DatasetViewerUIComponentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.SuperposedDataAnalyserUIComponentInterface;

import javax.swing.*;
import java.awt.*;


/***************************************************************************************************
 * RemoveDataset.
 */

public final class RemoveDataset implements FrameworkConstants,
                                            FrameworkStrings,
                                            FrameworkMetadata,
                                            FrameworkSingletons
    {
    // String Resources
    private static final String TITLE_REMOVE = "Remove Superposed Dataset";
    private static final String MSG_REMOVE_0 = "Are you sure that you wish to remove the selected Dataset?";
    private static final String MSG_REMOVE_1 = "This will not delete the data file, only remove it from Superposed Data Analyser.";


    /***********************************************************************************************
     * Remove a Dataset.
     * Return true if successful.
     *
     * @param sdaui
     * @param viewerui
     * @param action
     *
     * @return boolean
     */

    public static boolean doRemoveDataset(final SuperposedDataAnalyserUIComponentInterface sdaui,
                                          final DatasetViewerUIComponentInterface viewerui,
                                          final ContextAction action)
        {
        final String SOURCE = "RemoveDataset.doRemoveDataset() ";
        boolean boolSuccess;

        //LOGGER.logTimedEvent(SOURCE);

        boolSuccess = false;

        if ((sdaui != null)
            && (sdaui.getDatasetManager() != null)
            && (viewerui != null)
            && (action != null))
            {
            final int intOriginalSelectedIndex;

            intOriginalSelectedIndex = sdaui.getDatasetManager().getSelectedIndex();

            // Double check the current selection state
            if ((sdaui.getDatasetManager().hasSelection())
                && (SuperposedDataAnalyserHelper.isValidSelectionState(sdaui,
                                                                       viewerui.getDatasetSelector(),
                                                                       intOriginalSelectedIndex)))
                {
                final int intChoice;
                final String [] strMessage =
                    {
                    MSG_REMOVE_0,
                    MSG_REMOVE_1
                    };

                intChoice = JOptionPane.showOptionDialog(null,
                                                         strMessage,
                                                         TITLE_REMOVE,
                                                         JOptionPane.YES_NO_OPTION,
                                                         JOptionPane.QUESTION_MESSAGE,
                                                         null,
                                                         null,
                                                         null);
                if (intChoice == JOptionPane.YES_OPTION)
                    {
                    final int intNewSelectedIndex;

                    // Stop UI selections for a while
                    if (viewerui.getDatasetSemaphore() != null)
                        {
                        viewerui.getDatasetSemaphore().setState(SuperposedDataAnalyserHelper.BLOCK_EVENTS);
                        }
                    viewerui.getDatasetSelector().setEnabled(false);

                    // Ask the DatasetManager to remove the current selected DAO
                    sdaui.getDatasetManager().remove();

                    // If there is a UI selection, then OriginalSelectedIndex must be >= 0
                    viewerui.getDatasetSelector().removeItemAt(intOriginalSelectedIndex);

                    // Did we delete the only item?
                    if (viewerui.getDatasetSelector().getItemCount() == 0)
                        {
                        viewerui.getDatasetSelector().setSelectedIndex(DatasetManagerInterface.NO_SELECTION_INDEX);

                        if (viewerui.getChartViewer() != null)
                            {
                            // This will happen immediately, not on another Thread
                            // This displays a BlankUIComponent
                            viewerui.getChartViewer().removeUIIdentity();
                            }

                        if (viewerui.getDatasetTypeLabel() != null)
                            {
                            viewerui.getDatasetTypeLabel().setText(SuperposedDataAnalyserHelper.MSG_WAITING_FOR_IMPORT);
                            }

                        // Always return to a known state
                        // This calls refreshChart()
                        SuperposedDataAnalyserHelper.switchDatasetViewerDisplayMode(sdaui, SuperposedDataAnalyserDisplayMode.DATASET_VIEWER_CHART);
                        }
                    else
                        {
                        // There's at least one remaining item on the drop-down
                        // Return to select the first item each time
                        // Don't change the display mode
                        viewerui.getDatasetSelector().setSelectedIndex(0);

                        // Keep the UI in step with the new States
                        SuperposedDataAnalyserHelper.updateDatasetSelectorStates(sdaui, viewerui);
                        }

                    intNewSelectedIndex = viewerui.getDatasetSelector().getSelectedIndex();

                    // Update the DatasetViewer UI with the details for the new selection, if any
                    if ((intNewSelectedIndex > DatasetManagerInterface.NO_SELECTION_INDEX)
                        && (sdaui.getDatasetManager().isValidManager())
                        && (sdaui.getDatasetManager().hasSelection())
                        && (viewerui.isValidViewerUI()))
                        {
                        viewerui.getOffsetIndicator().setValue(SuperposedDataAnalyserHelper.PATTERN_OFFSET.format(sdaui.getDatasetManager().getSelectedOffset()));
                        viewerui.getOffsetControl().setEnabled(sdaui.getDatasetManager().canAdjustOffset());

                        // BEWARE setValue() will fire a ChangeEvent,
                        // which will use the *current* SecondaryDatasetIndex
                        // so use BLOCK_EVENTS
                        viewerui.getOffsetControl().setValue(sdaui.getDatasetManager().getSelectedOffset());

                        // Show the selected Chart and Metadata using the new DAO
                        // This calls refreshChart()
                        SuperposedDataAnalyserHelper.showSelectedXYDatasetOnDatasetViewer(sdaui.getDatasetManager(),
                                                                                          viewerui,
                                                                                          true,
                                                                                          LOADER_PROPERTIES.isChartDebug());

                        // Can Attach if not already Attached and not already Locked
                        viewerui.getAttachButton().setEnabled(sdaui.getDatasetManager().canAttach());

                        // The User will be asked if they really want to Detach the Primary,
                        // which will clear all current attachments
                        viewerui.getDetachButton().setEnabled(sdaui.getDatasetManager().canDetach());

                        viewerui.getMetadataButton().setEnabled(true);

                        // A Primary Dataset cannot be removed directly, it must be Detached first
                        viewerui.getRemoveButton().setEnabled(sdaui.getDatasetManager().canRemove());
                        }
                    else
                        {
                        // There may be NO_SELECTION
                        if (viewerui.isValidViewerUI())
                            {
                            viewerui.getOffsetIndicator().setValue(SuperposedDataAnalyserHelper.PATTERN_OFFSET.format(0.0));
                            viewerui.getOffsetControl().setEnabled(false);
                            viewerui.getOffsetControl().setCentre();
                            viewerui.getAttachButton().setEnabled(false);
                            viewerui.getDetachButton().setEnabled(false);
                            viewerui.getMetadataButton().setEnabled(false);
                            viewerui.getRemoveButton().setEnabled(false);
                            }
                        else
                            {
                            LOGGER.error(SOURCE + "UI components corrupted");
                            }
                        }

                    // Allow DatasetViewer selections to work again
                    if (viewerui.getDatasetSemaphore() != null)
                        {
                        viewerui.getDatasetSemaphore().setState(SuperposedDataAnalyserHelper.ALLOW_EVENTS);
                        }
                    viewerui.getDatasetSelector().setEnabled(true);

                    // Regardless of the above, re-enable the Import button
                    viewerui.getImportButton().setEnabled(true);

                    // Try to tidy up, if we are allowed to run the gc
                    ObservatoryInstrumentHelper.runGarbageCollector2();

                    boolSuccess = true;
                    }
                else
                    {
                    final String [] message =
                        {
                        DatasetManagerInterface.MSG_FAIL_REMOVE,
                        DatasetManagerInterface.MSG_EVENT_LOG
                        };

                    Toolkit.getDefaultToolkit().beep();
                    JOptionPane.showMessageDialog(null,
                                                  message,
                                                  (String) action.getValue(Action.SHORT_DESCRIPTION),
                                                  JOptionPane.WARNING_MESSAGE);
                    }
                }
            else
                {
                LOGGER.error(SOURCE + "No dataset to remove, no dataset selector, or corrupted data structures");
                }
            }
        else
            {
            LOGGER.error(SOURCE + SuperposedDataAnalyserUIComponentInterface.MSG_INCORRECT_DAO);
            }

        return (boolSuccess);
        }
    }
