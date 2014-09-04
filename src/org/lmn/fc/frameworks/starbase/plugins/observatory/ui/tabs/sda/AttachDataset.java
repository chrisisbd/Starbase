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
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.DatasetManagerInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.DatasetViewerUIComponentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.SuperposedDataAnalyserUIComponentInterface;

import javax.swing.*;
import java.awt.*;


/***************************************************************************************************
 * AttachDataset.
 */

public final class AttachDataset implements FrameworkConstants,
                                            FrameworkStrings,
                                            FrameworkMetadata,
                                            FrameworkSingletons
    {
    /***********************************************************************************************
     * Attach a Dataset.
     * Return true if successful.
     *
     * @param sdaui
     * @param viewerui
     * @param action
     *
     * @return boolean
     */

    public static boolean doAttachDataset(final SuperposedDataAnalyserUIComponentInterface sdaui,
                                          final DatasetViewerUIComponentInterface viewerui,
                                          final ContextAction action)
        {
        final String SOURCE = "AttachDataset.doAttachDataset() ";
        boolean boolSuccess;

        boolSuccess = false;

        if ((sdaui != null)
            && (sdaui.getDatasetManager() != null)
            && (viewerui != null)
            && (action != null))
            {
            if (sdaui.getDatasetManager().hasSelection())
                {
                // Attach the currently selected Secondary Dataset to the Composite
                // The DatasetManager will mark the first Attachment as the Primary
                boolSuccess = sdaui.getDatasetManager().attach();

                // Keep the UI in step with the State
                if ((boolSuccess)
                    && (viewerui.isValidViewerUI()))
                    {
                    // Keep the UI in step with the new States
                    SuperposedDataAnalyserHelper.updateDatasetSelectorStates(sdaui, viewerui);

                    // Cannot Attach again
                    viewerui.getAttachButton().setEnabled(sdaui.getDatasetManager().canAttach());

                    // The User will be asked if they really want to Detach the Primary,
                    // which will clear all current attachments
                    viewerui.getDetachButton().setEnabled(sdaui.getDatasetManager().canDetach());

                    // The Offset may have been forced to zero if this Dataset was chosen as the Primary
                    viewerui.getOffsetIndicator().setValue(SuperposedDataAnalyserHelper.PATTERN_OFFSET.format(sdaui.getDatasetManager().getSelectedOffset()));

                    // Offset may now be adjusted again, but not for the Primary
                    viewerui.getOffsetControl().setEnabled(sdaui.getDatasetManager().canAdjustOffset());

                    // BEWARE setValue() will fire a ChangeEvent,
                    // which will use the *current* SecondaryDatasetIndex
                    viewerui.getOffsetControl().setValue(sdaui.getDatasetManager().getSelectedOffset());

                    // Metadata are always available
                    viewerui.getMetadataButton().setEnabled(true);

                    viewerui.getRemoveButton().setEnabled(sdaui.getDatasetManager().canRemove());
                    }
                else
                    {
                    final String [] message =
                        {
                        DatasetManagerInterface.MSG_FAIL_ATTACH,
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
                LOGGER.error(SOURCE + "No dataset to attach");
                }
            }
        else
            {
            LOGGER.error(SOURCE + SuperposedDataAnalyserUIComponentInterface.MSG_INCORRECT_DAO);
            }

        return (boolSuccess);
        }
    }
