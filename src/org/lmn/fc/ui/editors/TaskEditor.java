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

//--------------------------------------------------------------------------------------------------
// Revision History
//
//  20-02-02    LMN created file
//  16-06-03    LMN revised for new Tasks, fixed Active initialisation bug
//  22-10-03    LMN converted to use StringData, changed layout to be the same as PropertyEditor
//  23-10-03    LMN moved some code to EditorUtilities
//  20-10-04    LMN changed for DateCreated, TimeCreated, DateModified, TimeModified columns
//
//--------------------------------------------------------------------------------------------------

package org.lmn.fc.ui.editors;

//--------------------------------------------------------------------------------------------------
// Imports

import org.lmn.fc.common.exceptions.FrameworkException;
import org.lmn.fc.common.utilities.time.Chronos;
import org.lmn.fc.common.utilities.time.ChronosHelper;
import org.lmn.fc.model.dao.mysql.TasksSqlDAO;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.root.UserObjectPlugin;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.ui.components.EditorUIComponent;
import org.lmn.fc.ui.components.EditorUtilities;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.GregorianCalendar;


/***************************************************************************************************
 * The TaskEditor for TaskData.
 *
 * ToDo Focus traversal policy
 * ToDo String Resources
 * ToDo Exceptions
 * ToDO dataChanged()
 * ToDo row counter
 * Todo check for running or locked task
 * ToDo improve logging messages
 */

public final class TaskEditor extends EditorUIComponent
    {
    // String Resources
    private static final String RESOURCE_KEY = "Editor.Task.";

    // The number of standard height rows (i.e. not the Description)
    private static final int ROW_COUNT = 10;

    private final TaskPlugin pluginTask;            // The Task being edited

    private boolean boolSavedActive;            // Saved data for Revert function
    private boolean boolSavedEditable;
    private boolean boolSavedPublic;
    private boolean boolSavedRunnable;
    private boolean boolSavedRunAtStart;
    private String strSavedDescription;
    private GregorianCalendar dateSavedDateModified;
    private GregorianCalendar timeSavedTimeModified;

    private JPanel panelEditor;
    private JPanel panelLabel;
    private JPanel panelData;
    private JPanel panelButtons;

    private JLabel labelName;
    private JLabel labelActive;
    private JLabel labelEditable;
    private JLabel labelPublic;
    private JLabel labelRunnable;
    private JLabel labelRunAtStart;
    private JLabel labelDateCreated;
    private JLabel labelTimeCreated;
    private JLabel labelDateModified;
    private JLabel labelTimeModified;
    private JLabel labelDescription;

    private JTextField textName;
    private JCheckBox checkActive;
    private JCheckBox checkEditable;
    private JCheckBox checkPublic;
    private JCheckBox checkRunnable;
    private JCheckBox checkRunAtStart;
    private JTextField textDateCreated;
    private JTextField textTimeCreated;
    private JTextField textDateModified;
    private JTextField textTimeModified;
    private JTextArea textDescription;

    private JButton buttonRevert;
    private JButton buttonCommit;


    /***********************************************************************************************
     * Constructor creates an editor panel for the supplied TaskData.
     *
     * @param plugin
     */

    public TaskEditor(final UserObjectPlugin plugin)
        {
        // Create the EditorUtilities for <Framework>.Editor.Task.
        super(REGISTRY.getFrameworkResourceKey() + RESOURCE_KEY);
        System.out.println("construct task editor!!!!!!!!!!!!!!!");
        // Save the Task to be edited
        pluginTask = (TaskPlugin)plugin;
        saveTask(pluginTask);

        // Read colours etc.
        readResources();

        // Attempt to lay out the Editor
        if (!createEditorPanel())
            {
            LOGGER.handleAtomException(REGISTRY.getFramework(),
                                       REGISTRY.getFramework().getRootTask(),
                                       this.getClass().getName(),
                                       new FrameworkException(getResourceKey() + "Create"),
                                       REGISTRY.getString(getResourceKey() + "Title"),
                                       EventStatus.FATAL);
            }
        }


    /***********************************************************************************************
     * Create the Editor panel.
     *
     * @return boolean
     */

    private boolean createEditorPanel()
        {
        final int intLabelHeight;

        // The left-hand label panel

        labelName = createLabel(getTextColour(),
                                getLabelFont(),
                                "Label.Name");

        labelActive = createLabel(getTextColour(),
                                  getLabelFont(),
                                  "Label.Active");

        labelEditable = createLabel(getTextColour(),
                                    getLabelFont(),
                                    "Label.Editable");

        labelPublic = createLabel(getTextColour(),
                                  getLabelFont(),
                                  "Label.Public");

        labelRunnable = createLabel(getTextColour(),
                                    getLabelFont(),
                                    "Label.Runnable");

        labelRunAtStart = createLabel(getTextColour(),
                                      getLabelFont(),
                                      "Label.RunAtStart");

        labelDateCreated = createLabel(getTextColour(),
                                       getLabelFont(),
                                       "Label.DateCreated");

        labelTimeCreated = createLabel(getTextColour(),
                                       getLabelFont(),
                                       "Label.TimeCreated");

        labelDateModified = createLabel(getTextColour(),
                                        getLabelFont(),
                                        "Label.DateModified");

        labelTimeModified = createLabel(getTextColour(),
                                        getLabelFont(),
                                        "Label.TimeModified");

        labelDescription = createLabel(getTextColour(),
                                       getLabelFont(),
                                       "Label.Description");

        // The right-hand data panel

        textName = createTextField(getTextColour(),
                                   getDataFont(),
                                   pluginTask.getPathname(),
                                   "Tooltip.Name",
                                   false);

        checkActive = createCheckBox(getCanvasColour(),
                                     "Tooltip.Active",
                                     pluginTask.isEditable(),
                                     pluginTask.isActive());

        checkEditable = createCheckBox(getCanvasColour(),
                                       "Tooltip.Editable",
                                       pluginTask.isEditable(),
                                       pluginTask.isEditable());

        checkPublic = createCheckBox(getCanvasColour(),
                                     "Tooltip.Public",
                                     pluginTask.isEditable(),
                                     pluginTask.isPublic());

        checkRunnable = createCheckBox(getCanvasColour(),
                                       "Tooltip.Runnable",
                                       pluginTask.isEditable(),
                                       pluginTask.isRunnable());

        checkRunAtStart = createCheckBox(getCanvasColour(),
                                         "Tooltip.RunAtStart",
                                         pluginTask.isEditable(),
                                         pluginTask.isRunAtStart());

        textDateCreated = createTextField(getTextColour(),
                                          getDataFont(),
                                          ChronosHelper.toDateString(pluginTask.getCreatedDate()),
                                          "Tooltip.Created",
                                          false);

        textTimeCreated = createTextField(getTextColour(),
                                          getDataFont(),
                                          ChronosHelper.toTimeString(pluginTask.getCreatedTime()),
                                          "Tooltip.Created",
                                          false);

        textDateModified = createTextField(getTextColour(),
                                           getDataFont(),
                                           ChronosHelper.toDateString(pluginTask.getModifiedDate()),
                                           "Tooltip.Modified",
                                           false);

        textTimeModified = createTextField(getTextColour(),
                                           getDataFont(),
                                           ChronosHelper.toTimeString(pluginTask.getModifiedTime()),
                                           "Tooltip.Modified",
                                           false);

        // Adjust the sizes of the DateTime fields to reduce screen clutter
        EditorUtilities.adjustNarrowField(textDateCreated);
        EditorUtilities.adjustNarrowField(textTimeCreated);
        EditorUtilities.adjustNarrowField(textDateModified);
        EditorUtilities.adjustNarrowField(textTimeModified);

        textDescription = createTextArea(getTextColour(),
                                         getDataFont(),
                                         pluginTask.getDescription(),
                                         "Tooltip.Description",
                                         HEIGHT_DESCRIPTION,
                                         pluginTask.isEditable());

        //------------------------------------------------------------------------------------------
        // Add the ActionListeners now we have stopped changing component states

        // TaskActive
        checkActive.addItemListener(new ItemListener()
            {
            public void itemStateChanged(final ItemEvent event)
                {
                LOGGER.debug("checkActive changed");

                // The database daemon must not write data until saveEventLog is complete
                dataChanged();
                }
            });

        checkEditable.addItemListener(new ItemListener()
            {
            public void itemStateChanged(final ItemEvent event)
                {
                LOGGER.debug("checkEditable changed");

                // The database daemon must not write data until saveEventLog is complete
                dataChanged();

                if (event.getStateChange() == ItemEvent.SELECTED)
                    {
                    checkActive.setEnabled(true);
                    checkPublic.setEnabled(true);
                    checkRunnable.setEnabled(true);
                    checkRunAtStart.setEnabled(true);
                    textDescription.setEnabled(true);
                    }
                else
                    {
                    checkActive.setEnabled(false);
                    checkPublic.setEnabled(false);
                    checkRunnable.setEnabled(false);
                    checkRunAtStart.setEnabled(false);
                    textDescription.setEnabled(false);
                    }
                }
            });

        checkPublic.addItemListener(new ItemListener()
            {
            public void itemStateChanged(final ItemEvent event)
                {
                LOGGER.debug("checkPublic changed");

                // The database daemon must not write data until saveEventLog is complete
                dataChanged();
                }
            });

        checkRunnable.addItemListener(new ItemListener()
            {
            public void itemStateChanged(final ItemEvent event)
                {
                LOGGER.debug("checkRunnable changed");

                // The database daemon must not write data until saveEventLog is complete
                dataChanged();
                }
            });

        checkRunAtStart.addItemListener(new ItemListener()
            {
            public void itemStateChanged(final ItemEvent event)
                {
                LOGGER.debug("checkRunAtStart changed");

                // The database daemon must not write data until saveEventLog is complete
                dataChanged();
                }
            });

        // TaskDescription
        final DocumentListener descriptionListener = new DocumentListener()
            {
            public void insertUpdate(final DocumentEvent event)
                {
                dataChanged();
                }

            public void removeUpdate(final DocumentEvent event)
                {
                dataChanged();
                }

            public void changedUpdate(final DocumentEvent event)
                {
                dataChanged();
                }
            };

        textDescription.getDocument().addDocumentListener(descriptionListener);

        //------------------------------------------------------------------------------------------
        // The Commit button panel

        buttonCommit = createButton(getTextColour(),
                                    getLabelFont(),
                                    "Label.Commit",
                                    "Tooltip.Commit",
                                    "buttonCommit",
                                    false);

        final ActionListener commitListener = new ActionListener()
            {
            public void actionPerformed(final ActionEvent event)
                {
                String strDescription;
                final String strFailureReason;
                final boolean boolCommit;
                final boolean boolEditable;
                final int intChoice;

                // We must check each item for validity, and write to the
                // RegistryModel if all is Ok, otherwise, give the user another chance
                // Initialise everything
                strDescription = textDescription.getText();
                boolCommit = true;
                strFailureReason = "";

                // See if the user is making this Task uneditable
                boolEditable = checkEditable.isSelected();

                if (!boolEditable)
                    {
                    intChoice = JOptionPane.showOptionDialog(null,
                                                             REGISTRY.getString(getResourceKey() + "Warning.Uneditable"),
                                                             REGISTRY.getString(getResourceKey() + "Title"),
                                                             JOptionPane.YES_NO_OPTION,
                                                             JOptionPane.WARNING_MESSAGE,
                                                             null,
                                                             null,
                                                             null);

                    if (intChoice != JOptionPane.OK_OPTION)
                        {
                        return;
                        }
                    }

                // Check that the Description is not too large
                strDescription = strDescription.trim();
                if (strDescription.length() > TasksSqlDAO.DESCRIPTION_LENGTH)
                    {
                    strDescription = strDescription.substring(0, TasksSqlDAO.DESCRIPTION_LENGTH-1);

                    JOptionPane.showMessageDialog(null,
                                                  REGISTRY.getString(getResourceKey() + "Warning.TruncatedDescription") + SPACE + TasksSqlDAO.DESCRIPTION_LENGTH + " characters",
                                                  REGISTRY.getString(getResourceKey() + "Warning.TooLongDescription"),
                                                  JOptionPane.WARNING_MESSAGE);
                    }

                // Did we pass all the tests?
                // The .set() mark the task for updating, and handle DateModified
                if ((boolCommit)
                    && (pluginTask != null))
                    {
                    // Set all changed values ready for the update
                    pluginTask.setActive(checkActive.isSelected());
                    pluginTask.setEditable(checkEditable.isSelected());
                    pluginTask.setPublic(checkPublic.isSelected());
                    pluginTask.setRunnable(checkRunnable.isSelected());
                    pluginTask.setRunAtStart(checkRunAtStart.isSelected());
                    pluginTask.setDescription(strDescription);
                    pluginTask.setModifiedDate(Chronos.getCalendarDateNow());
                    pluginTask.setModifiedTime(Chronos.getCalendarTimeNow());

                    // Update the display
                    // Do not update the checkboxes,
                    // because they don't need to be redrawn,
                    // and the change would produce an event we don't want
                    textDescription.setText(pluginTask.getDescription());
                    textDateModified.setText(ChronosHelper.toDateString(pluginTask.getModifiedDate()));
                    textTimeModified.setText(ChronosHelper.toTimeString(pluginTask.getModifiedTime()));

                    // Log the change of Active state
                    if (pluginTask.isActive()!= boolSavedActive)
                        {
                        LOGGER.logAtomEvent(pluginTask.getParentAtom(),
                                            pluginTask,
                                            getClass().getName(),
                                            METADATA_TASK_EDIT
                                                + SPACE
                                                + METADATA_NAME
                                                + pluginTask.getPathname()
                                                + TERMINATOR
                                                + SPACE
                                                + METADATA_STATE
                                                + pluginTask.isActive()
                                                + TERMINATOR,
                                            EventStatus.INFO);
                        }

                    // The edit was completed successfully!
                    saveTask(pluginTask);

                    // Prevent further user interaction
                    buttonCommit.setEnabled(false);
                    buttonRevert.setEnabled(false);

                    // The database daemon can write Task data when it next runs
                    pluginTask.setUpdateAllowed(true);
                    pluginTask.setUpdated(true);
                    }
                else
                    {
                    // Something failed, so ask the user to try again
                    JOptionPane.showMessageDialog(null,
                                                  strFailureReason,
                                                  REGISTRY.getString(getResourceKey() + "Warning.InvalidEntry"),
                                                  JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            };
        buttonCommit.addActionListener(commitListener);

        //--------------------------------------------------------------------------------------------------------------------------------------------
        // The Revert button and its listener

        buttonRevert = createButton(getTextColour(),
                                    getLabelFont(),
                                    "Label.Revert",
                                    "Tooltip.Revert",
                                    "buttonRevert",
                                    false);

        final ActionListener revertListener = new ActionListener()
            {
            public void actionPerformed(final ActionEvent event)
                {
                // Undo any edits
                // These changes will enable the buttons via the listeners!

                pluginTask.setActive(boolSavedActive);
                checkActive.setSelected(pluginTask.isActive());

                pluginTask.setEditable(boolSavedEditable);
                checkEditable.setSelected(pluginTask.isEditable());

                pluginTask.setPublic(boolSavedPublic);
                checkPublic.setSelected(pluginTask.isPublic());

                pluginTask.setRunnable(boolSavedRunnable);
                checkRunnable.setSelected(pluginTask.isRunnable());

                pluginTask.setRunAtStart(boolSavedRunAtStart);
                checkRunAtStart.setSelected(pluginTask.isRunAtStart());

                pluginTask.setModifiedDate(dateSavedDateModified);
                pluginTask.setModifiedTime(timeSavedTimeModified);
                textDateModified.setText(ChronosHelper.toDateString(pluginTask.getModifiedDate()));
                textTimeModified.setText(ChronosHelper.toTimeString(pluginTask.getModifiedTime()));

                pluginTask.setDescription(strSavedDescription);
                textDescription.setText(pluginTask.getDescription());

                pluginTask.setUpdated(false);

                // Prevent further user interaction
                buttonCommit.setEnabled(false);
                buttonRevert.setEnabled(false);

                // The database daemon can write data again (but not this Task)
                pluginTask.setUpdateAllowed(true);
                }
            };

        buttonRevert.addActionListener(revertListener);

        //------------------------------------------------------------------------------------------
        // Put all the panels together in the right order

        intLabelHeight = (int)(DIM_ROW_SPACER.getHeight() * ROW_COUNT)
                          + (EditorUtilities.HEIGHT_ROW * ROW_COUNT)
                          + HEIGHT_DESCRIPTION;

        panelEditor = EditorUtilities.createEditorPanel(getCanvasColour());
        panelLabel = EditorUtilities.createLabelPanel(getCanvasColour(), intLabelHeight);
        panelData = EditorUtilities.createDataPanel(getCanvasColour());
        panelButtons = EditorUtilities.createButtonPanel(getCanvasColour(), buttonRevert,
                                                         buttonCommit);

        panelLabel.add(labelName);
        panelLabel.add(Box.createRigidArea(DIM_ROW_SPACER));
        panelLabel.add(labelActive);
        panelLabel.add(Box.createRigidArea(DIM_ROW_SPACER));
        panelLabel.add(labelEditable);
        panelLabel.add(Box.createRigidArea(DIM_ROW_SPACER));
        panelLabel.add(labelPublic);
        panelLabel.add(Box.createRigidArea(DIM_ROW_SPACER));
        panelLabel.add(labelRunnable);
        panelLabel.add(Box.createRigidArea(DIM_ROW_SPACER));
        panelLabel.add(labelRunAtStart);
        panelLabel.add(Box.createRigidArea(DIM_ROW_SPACER));
        panelLabel.add(labelDescription);
        panelLabel.add(Box.createRigidArea(DIM_DESCRIPTION_SPACER));
        panelLabel.add(labelDateCreated);
        panelLabel.add(Box.createRigidArea(DIM_ROW_SPACER));
        panelLabel.add(labelTimeCreated);
        panelLabel.add(Box.createRigidArea(DIM_ROW_SPACER));
        panelLabel.add(labelDateModified);
        panelLabel.add(Box.createRigidArea(DIM_ROW_SPACER));
        panelLabel.add(labelTimeModified);

        panelData.add(textName);
        panelData.add(Box.createRigidArea(DIM_ROW_SPACER));
        panelData.add(checkActive);
        panelData.add(Box.createRigidArea(DIM_ROW_SPACER));
        panelData.add(checkEditable);
        panelData.add(Box.createRigidArea(DIM_ROW_SPACER));
        panelData.add(checkPublic);
        panelData.add(Box.createRigidArea(DIM_ROW_SPACER));
        panelData.add(checkRunnable);
        panelData.add(Box.createRigidArea(DIM_ROW_SPACER));
        panelData.add(checkRunAtStart);
        panelData.add(Box.createRigidArea(DIM_ROW_SPACER));
        panelData.add(textDescription);
        panelData.add(Box.createRigidArea(DIM_ROW_SPACER));
        panelData.add(textDateCreated);
        panelData.add(Box.createRigidArea(DIM_ROW_SPACER));
        panelData.add(textTimeCreated);
        panelData.add(Box.createRigidArea(DIM_ROW_SPACER));
        panelData.add(textDateModified);
        panelData.add(Box.createRigidArea(DIM_ROW_SPACER));
        panelData.add(textTimeModified);

        installPanels(getCanvasColour(),
                      panelEditor,
                      panelLabel,
                      panelData,
                      panelButtons);

        return (true);
        }


    /***********************************************************************************************
     * Save the modifiable TaskData for later Revert.
     *
     * @param data
     */

    private void saveTask(final TaskPlugin data)
        {
        boolSavedActive = data.isActive();
        boolSavedEditable = data.isEditable();
        boolSavedPublic = data.isPublic();
        boolSavedRunnable = data.isRunnable();
        boolSavedRunAtStart = data.isRunAtStart();
        strSavedDescription = EditorUtilities.replaceNull(data.getDescription());
        dateSavedDateModified = EditorUtilities.replaceNull(data.getModifiedDate());
        timeSavedTimeModified = EditorUtilities.replaceNull(data.getModifiedTime());
        }


    /***********************************************************************************************
     * A utility to indicate when the data has changed as a result of an edit.
     */

    private void dataChanged()
        {
        pluginTask.setUpdateAllowed(false);
        buttonCommit.setEnabled(true);
        buttonRevert.setEnabled(true);
        }
    }


//--------------------------------------------------------------------------------------------------
// End of File
