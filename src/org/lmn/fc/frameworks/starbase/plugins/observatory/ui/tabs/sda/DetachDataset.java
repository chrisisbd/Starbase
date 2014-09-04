// Copyright 2000, 2001, 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009,
//           2010, 2011, 2012, 2013, 2014
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
import org.lmn.fc.common.utilities.ui.ListCellDatasetState;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.DatasetManagerInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.DatasetViewerUIComponentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.SuperposedDataAnalyserUIComponentInterface;

import javax.swing.*;
import java.awt.*;


/***************************************************************************************************
 * DetachDataset.
 */

public final class DetachDataset implements FrameworkConstants,
                                            FrameworkStrings,
                                            FrameworkMetadata,
                                            FrameworkSingletons
    {
    // String Resources
    private static final String TITLE_DETACH_PRIMARY = "Detach Primary Dataset";
    private static final String TITLE_DETACH_ALL = "Detach All Datasets";
    private static final String MSG_DETACH_0 = "The selected dataset is marked as the Primary.";
    private static final String MSG_DETACH_1 = "If you detach it, all other attachments will be cleared.";
    private static final String MSG_DETACH_2 = "Are you sure that you wish to do this?";
    private static final String MSG_DETACH_3 = "Are you sure that you wish to do detach all datasets from the composite view?";


    /***********************************************************************************************
     * Detach a Dataset.
     * The User will be asked if they really want to Detach the Primary,
     * which will clear all current attachments.
     * Return true if successful.
     *
     * @param sdaui
     * @param viewerui
     * @param action
     *
     * @return boolean
     */

    public static boolean doDetachDataset(final SuperposedDataAnalyserUIComponentInterface sdaui,
                                          final DatasetViewerUIComponentInterface viewerui,
                                          final ContextAction action)
        {
        final String SOURCE = "DetachDataset.doDetachDataset() ";
        boolean boolSuccess;

        boolSuccess = false;

        if ((sdaui != null)
            && (sdaui.getDatasetManager() != null)
            && (viewerui != null)
            && (action != null))
            {
            if (sdaui.getDatasetManager().canDetach())
                {
                if (sdaui.getDatasetManager().isPrimary())
                    {
                    final int intChoice;
                    final String [] strMessage =
                        {
                        MSG_DETACH_0,
                        MSG_DETACH_1,
                        MSG_DETACH_2
                        };

                    intChoice = JOptionPane.showOptionDialog(null,
                                                             strMessage,
                                                             TITLE_DETACH_PRIMARY,
                                                             JOptionPane.YES_NO_OPTION,
                                                             JOptionPane.QUESTION_MESSAGE,
                                                             null,
                                                             null,
                                                             null);

                    if (intChoice == JOptionPane.YES_OPTION)
                        {
                        // Detach all current Attachments
                        boolSuccess = sdaui.getDatasetManager().detachAll();

                        // Keep all items on the DatasetSelector in step, regardless of selection
                        if ((boolSuccess)
                            && (sdaui.getDatasetManager().size() == viewerui.getDatasetSelector().getItemCount()))
                            {
                            // There will be a non-zero count, because we checked with canDetach()
                            for (int intItemIndex = 0;
                                 intItemIndex < viewerui.getDatasetSelector().getItemCount();
                                 intItemIndex++)
                                {
                                if (viewerui.getDatasetSelector().getItemAt(intItemIndex) instanceof ListCellDatasetState)
                                    {
                                    final ListCellDatasetState datasetState;

                                    datasetState = (ListCellDatasetState)viewerui.getDatasetSelector().getItemAt(intItemIndex);
                                    datasetState.setTooltipText(EMPTY_STRING);
                                    datasetState.setIndex(DatasetManagerInterface.NO_SELECTION_INDEX);
                                    datasetState.setDatasetState(sdaui.getDatasetManager().getSecondaryStates().get(intItemIndex));
                                    }
                                }
                            }
                        }
                    else
                        {
                        final String [] message =
                            {
                            DatasetManagerInterface.MSG_FAIL_DETACH,
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
                    // Detach only the currently selected Secondary Dataset from the Composite
                    boolSuccess = sdaui.getDatasetManager().detach();
                    }

                if ((boolSuccess)
                    && (viewerui.isValidViewerUI()))
                    {
                    // Keep the UI in step with the new States
                    SuperposedDataAnalyserHelper.updateDatasetSelectorStates(sdaui, viewerui);

                    // Ready to Attach again
                    viewerui.getAttachButton().setEnabled(sdaui.getDatasetManager().canAttach());

                    // Cannot Detach again
                    viewerui.getDetachButton().setEnabled(sdaui.getDatasetManager().canDetach());

                    // Offset may now be adjusted again, in the case the dataset was set as the Primary
                    viewerui.getOffsetControl().setEnabled(sdaui.getDatasetManager().canAdjustOffset());

                    // Metadata are always available
                    viewerui.getMetadataButton().setEnabled(true);

                    viewerui.getRemoveButton().setEnabled(sdaui.getDatasetManager().canRemove());
                    }
                else
                    {
                    LOGGER.error(SOURCE + "Detachment failed, or UI components corrupted");
                    }
                }
            else
                {
                LOGGER.error(SOURCE + "No dataset to detach");
                }
            }
        else
            {
            LOGGER.error(SOURCE + SuperposedDataAnalyserUIComponentInterface.MSG_INCORRECT_DAO);
            }

        return (boolSuccess);
        }


    /***********************************************************************************************
     * Detach All Datasets.
     * Return true if successful.
     *
     * @param sdaui
     * @param viewerui
     * @param action
     *
     * @return boolean
     */

    public static boolean doDetachAllDatasets(final SuperposedDataAnalyserUIComponentInterface sdaui,
                                              final DatasetViewerUIComponentInterface viewerui,
                                              final ContextAction action)
        {
        final String SOURCE = "DetachDataset.doDetachAllDatasets() ";
        boolean boolSuccess;

        boolSuccess = false;

        try
            {
            if ((sdaui != null)
                && (sdaui.getDatasetManager() != null)
                && (viewerui != null)
                && (action != null))
                {
                if (sdaui.getDatasetManager().canDetachAll())
                    {
                    final int intChoice;

                    intChoice = JOptionPane.showOptionDialog(null,
                                                             MSG_DETACH_3,
                                                             TITLE_DETACH_ALL,
                                                             JOptionPane.YES_NO_OPTION,
                                                             JOptionPane.QUESTION_MESSAGE,
                                                             null,
                                                             null,
                                                             null);

                    if (intChoice == JOptionPane.YES_OPTION)
                        {
                        // Detach all current Attachments
                        boolSuccess = sdaui.getDatasetManager().detachAll();

                        // Keep all items on the DatasetSelector in step, regardless of selection
                        if ((boolSuccess)
                            && (sdaui.getDatasetManager().size() == viewerui.getDatasetSelector().getItemCount()))
                            {
                            // There will be a non-zero count, because we checked with canDetach()
                            for (int intItemIndex = 0;
                                 intItemIndex < viewerui.getDatasetSelector().getItemCount();
                                 intItemIndex++)
                                {
                                if (viewerui.getDatasetSelector().getItemAt(intItemIndex) instanceof ListCellDatasetState)
                                    {
                                    final ListCellDatasetState datasetState;

                                    datasetState = (ListCellDatasetState)viewerui.getDatasetSelector().getItemAt(intItemIndex);
                                    datasetState.setTooltipText(EMPTY_STRING);
                                    datasetState.setIndex(DatasetManagerInterface.NO_SELECTION_INDEX);
                                    datasetState.setDatasetState(sdaui.getDatasetManager().getSecondaryStates().get(intItemIndex));
                                    }
                                }
                            }
                        else
                            {
                            final String [] message =
                                {
                                DatasetManagerInterface.MSG_FAIL_DETACHALL,
                                DatasetManagerInterface.MSG_EVENT_LOG
                                };

                            Toolkit.getDefaultToolkit().beep();
                            JOptionPane.showMessageDialog(null,
                                                          message,
                                                          (String) action.getValue(Action.SHORT_DESCRIPTION),
                                                          JOptionPane.WARNING_MESSAGE);
                            }
                        }

                    if ((boolSuccess)
                        && (viewerui.isValidViewerUI()))
                        {
                        // Keep the UI in step with the new States
                        SuperposedDataAnalyserHelper.updateDatasetSelectorStates(sdaui, viewerui);

                        // Ready to Attach again
                        viewerui.getAttachButton().setEnabled(sdaui.getDatasetManager().canAttach());

                        // Cannot Detach again
                        viewerui.getDetachButton().setEnabled(sdaui.getDatasetManager().canDetach());

                        // Offset may now be adjusted again, in the case the dataset was set as the Primary
                        viewerui.getOffsetControl().setEnabled(sdaui.getDatasetManager().canAdjustOffset());

                        // Metadata are always available
                        viewerui.getMetadataButton().setEnabled(true);

                        viewerui.getRemoveButton().setEnabled(sdaui.getDatasetManager().canRemove());
                        }
                    else
                        {
                        LOGGER.error(SOURCE + "Detachment failed, or UI components corrupted");
                        }
                    }
                else
                    {
                    LOGGER.error(SOURCE + "No dataset to detach");
                    }
                }
            else
                {
                LOGGER.error(SOURCE + SuperposedDataAnalyserUIComponentInterface.MSG_INCORRECT_DAO);
                }
            }

        catch (final HeadlessException exception)
            {
            LOGGER.error(SOURCE + "HeadlessException");
            exception.printStackTrace();
            }

        catch (final Exception exception)
            {
            LOGGER.error(SOURCE + "Exception");
            exception.printStackTrace();
            }

        return (boolSuccess);
        }
    }
