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
//  30-10-03    LMN created from ExceptionData
//  19-10-04    LMN changed for DateCreated, TimeCreated, DateModified, TimeModified columns
//  26-10-04    LMN fixing truncation bugs, changed screen layout etc.
//
//--------------------------------------------------------------------------------------------------

package org.lmn.fc.ui.editors;

//--------------------------------------------------------------------------------------------------
// Imports

import org.lmn.fc.common.exceptions.FrameworkException;
import org.lmn.fc.common.utilities.time.Chronos;
import org.lmn.fc.common.utilities.time.ChronosHelper;
import org.lmn.fc.database.impl.FrameworkDatabase;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.resources.QueryPlugin;
import org.lmn.fc.model.root.UserObjectPlugin;
import org.lmn.fc.ui.components.EditorUIComponent;
import org.lmn.fc.ui.components.EditorUtilities;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.sql.PreparedStatement;
import java.util.GregorianCalendar;
import java.util.Vector;


/***************************************************************************************************
 * The QueryEditor used by the FrameworkManager.
 *
 * ToDo Focus traversal policy
 * ToDo String Resources
 * ToDo Exception handlers
 * ToDo Multiline message boxes
 */

public final class QueryEditor extends EditorUIComponent
    {
    private static final FrameworkDatabase DATABASE = FrameworkDatabase.getInstance();

    // Framework String Resources
    private static final String RESOURCE_KEY = "Editor.Query.";

    private static final String KEY_LABEL_NAME = "Label.Name";

    // The number of standard height rows (i.e. not the Description)
    private static final int ROW_COUNT = 9;

    // The height of the Query Editor box
    private static final int HEIGHT_QUERYEDIT = 200;
    private static final Dimension DIM_QUERYEDIT_SPACER = new Dimension(1, HEIGHT_QUERYEDIT- EditorUtilities.HEIGHT_ROW+(int)DIM_ROW_SPACER.getHeight());

    private QueryPlugin queryPlugin;

    private boolean boolSavedEditable;
    private long longSavedExecutionCount;
    private long longSavedExecutionTime;
    private Object objSavedValue;
    private String strSavedDescription;
    private GregorianCalendar dateSavedDateModified;
    private GregorianCalendar timeSavedTimeModified;

    private JPanel panelEditor;
    private JPanel panelLabel;
    private JPanel panelData;
    private JPanel panelButtons;

    private JLabel labelName;
    private JLabel labelEditable;
    private JLabel labelExecutionCount;
    private JLabel labelExecutionTime;
    private JLabel labelValue;
    private JLabel labelDateCreated;
    private JLabel labelTimeCreated;
    private JLabel labelDateModified;
    private JLabel labelTimeModified;
    private JLabel labelDescription;

    private JTextField textName;
    private JCheckBox checkEditable;
    private JTextField textExecutionCount;
    private JTextField textExecutionTime;
    private JScrollPane scrollPane;
    private JTextArea textValue;
    private JTextField textDateCreated;
    private JTextField textTimeCreated;
    private JTextField textDateModified;
    private JTextField textTimeModified;
    private JTextArea textDescription;

    private JButton buttonRevert;
    private JButton buttonCommit;
    private JButton buttonResetCount;


    /***********************************************************************************************
     * Constructor creates a editor panel for the supplied QueryData object.
     *
     * @param plugin
     */

    public QueryEditor(final UserObjectPlugin plugin)
        {
        // Create the EditorUtilities for <Framework>.Editor.Query.
        super(REGISTRY.getFrameworkResourceKey() + RESOURCE_KEY);

        try
            {
            // Save the Query to be edited
            queryPlugin = (QueryPlugin)plugin;
            saveQuery(queryPlugin);
            }

        catch (ClassCastException exception)
            {
            LOGGER.handleAtomException(REGISTRY.getFramework(),
                                       REGISTRY.getFramework().getRootTask(),
                                       this.getClass().getName(),
                                       new FrameworkException(REGISTRY.getException(getResourceKey() + "ClassCast")),
                                       REGISTRY.getString(getResourceKey() + "Title"),
                                       EventStatus.FATAL);
            }

        // Read colours etc.
        readResources();

        // Attempt to lay out the Editor
        if (!createEditorPanel())
            {
            LOGGER.handleAtomException(REGISTRY.getFramework(),
                                       REGISTRY.getFramework().getRootTask(),
                                       this.getClass().getName(),
                                       new FrameworkException(REGISTRY.getException(getResourceKey() + "Create")),
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
        final Vector vecButtons;

        // The left-hand label panel

        labelName = createLabel(getTextColour(),
                                getLabelFont(),
                                KEY_LABEL_NAME);

        labelEditable = createLabel(getTextColour(),
                                    getLabelFont(),
                                    "Label.Editable");

        labelExecutionCount = createLabel(getTextColour(),
                                          getLabelFont(),
                                          "Label.ExecutionCount");

        labelExecutionTime = createLabel(getTextColour(),
                                         getLabelFont(),
                                         "Label.ExecutionTime");

        labelValue = createLabel(getTextColour(),
                                 getLabelFont(),
                                 "Label.Value");

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
                                   queryPlugin.getPathname(),
                                   "Tooltip.Name",
                                   false);

        checkEditable = createCheckBox(getCanvasColour(),
                                       "Tooltip.Editable",
                                       queryPlugin.isEditable(),
                                       queryPlugin.isEditable());

        textExecutionCount = createTextField(getTextColour(),
                                             getDataFont(),
                                             Long.toString(queryPlugin.getExecutionCount()),
                                             "Tooltip.ExecutionCount",
                                             false);
        EditorUtilities.adjustNarrowField(textExecutionCount);

        textExecutionTime = createTextField(getTextColour(),
                                            getDataFont(),
                                            Long.toString(queryPlugin.getExecutionTime()),
                                            "Tooltip.ExecutionTime",
                                            false);
        EditorUtilities.adjustNarrowField(textExecutionTime);

        textValue = createTextArea(getTextColour(),
                                    getDataFont(),
                                    queryPlugin.getResource().toString(),
                                    "Tooltip.Value",
                                    HEIGHT_QUERYEDIT,
                                    queryPlugin.isEditable());

        textDateCreated = createTextField(getTextColour(),
                                          getDataFont(),
                                          ChronosHelper.toDateString(queryPlugin.getCreatedDate()),
                                          "Tooltip.Created",
                                          false);

        textTimeCreated = createTextField(getTextColour(),
                                          getDataFont(),
                                          ChronosHelper.toTimeString(queryPlugin.getCreatedTime()),
                                          "Tooltip.Created",
                                          false);

        textDateModified = createTextField(getTextColour(),
                                           getDataFont(),
                                           ChronosHelper.toDateString(queryPlugin.getModifiedDate()),
                                           "Tooltip.Modified",
                                           false);

        textTimeModified = createTextField(getTextColour(),
                                           getDataFont(),
                                           ChronosHelper.toTimeString(queryPlugin.getModifiedTime()),
                                           "Tooltip.Modified",
                                           false);

        // Adjust the sizes of the DateTime fields to reduce screen clutter
        EditorUtilities.adjustNarrowField(textDateCreated);
        EditorUtilities.adjustNarrowField(textTimeCreated);
        EditorUtilities.adjustNarrowField(textDateModified);
        EditorUtilities.adjustNarrowField(textTimeModified);

        textDescription = createTextArea(getTextColour(),
                                         getDataFont(),
                                         queryPlugin.getDescription(),
                                         "Tooltip.Description",
                                         100,
                                         queryPlugin.isEditable());

        //------------------------------------------------------------------------------------------
        // Add the ActionListeners now we have stopped changing component states

        // Editable checkbox
        final ItemListener editableListener = new ItemListener()
            {
            public void itemStateChanged(final ItemEvent event)
                {
                LOGGER.debug("checkEditable changed");

                // The database daemon must not write data until saveEventLog is complete
                dataChanged();

                if (event.getStateChange() == ItemEvent.SELECTED)
                    {
                    textValue.setEnabled(true);
                    textDescription.setEnabled(true);
                    buttonResetCount.setEnabled(true);
                    }
                else
                    {
                    textValue.setEnabled(false);
                    textDescription.setEnabled(false);
                    buttonResetCount.setEnabled(false);
                    }
                }
            };

        checkEditable.addItemListener(editableListener);

        // QueryValue text box
        final KeyListener keyListener = new KeyListener()
            {
            // Handle the key typed event from the text field
            public void keyTyped(final KeyEvent event)
                {
                // No action required
                }

            // Handle the key pressed event from the text field
            public void keyPressed(final KeyEvent event)
                {
                // No action required
                }

            // Handle the key released event from the text field
            public void keyReleased(final KeyEvent event)
                {
                dataChanged();
                }
            };

        textValue.addKeyListener(keyListener);

        // QueryDescription text box
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
        // The Commit button and its listener

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
                final boolean boolEditable;
                String strStringValue;
                String strDescription;
                String strLogText;
                final int intChoice;
                final PreparedStatement psEdit;

                // We must check each item for validity, and write to the
                // RegistryModel if all is Ok, otherwise, give the user another chance

                // See if the user is making this Property uneditable
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
                // Issue a truncation warning if so
                strDescription = textDescription.getText();
                strDescription = strDescription.trim();

                if (strDescription.length() > QueryPlugin.DESCRIPTION_LENGTH)
                    {
                    strDescription = strDescription.substring(0, QueryPlugin.DESCRIPTION_LENGTH-1);

                    JOptionPane.showMessageDialog(null,
                                                  REGISTRY.getString(getResourceKey() + "Warning.TruncatedDescription") + SPACE + QueryPlugin.DESCRIPTION_LENGTH + " characters",
                                                  REGISTRY.getString(getResourceKey() + "Title"),
                                                  JOptionPane.WARNING_MESSAGE);
                    }

                // Check that the Value is not too large
                // Issue a truncation warning if so
                strStringValue = textValue.getText();

                if (strStringValue.length() > QueryPlugin.QUERY_LENGTH)
                    {
                    strStringValue = strStringValue.substring(0, QueryPlugin.QUERY_LENGTH-1);

                    JOptionPane.showMessageDialog(null,
                                                  REGISTRY.getString(getResourceKey() + "Warning.TruncatedValue") + SPACE + QueryPlugin.QUERY_LENGTH + " characters",
                                                  REGISTRY.getString(getResourceKey() + "Title"),
                                                  JOptionPane.WARNING_MESSAGE);
                    }

                // Check the syntax of the SQL by trying to make the PreparedStatement
//                try
//                    {
//                    // Create the new PreparedStatement
//                    // Use the existing Connection from the earlier statement
//                    psEdit = queryPlugin.getPreparedStatement(DATABASE, DataStore.MYSQL).getConnection().prepareStatement(strStringValue);
//                    }
//
//                catch (SQLException exception)
//                    {
//                    JOptionPane.showMessageDialog(null,
//                                                  REGISTRY.getString(getResourceKey() + "Warning.Syntax") + " [" + exception + "]",
//                                                  REGISTRY.getString(getResourceKey() + "Title"),
//                                                  JOptionPane.ERROR_MESSAGE);
//                    return;
//                    }

                // If we get this far, set all changed values ready for the update
                queryPlugin.setResource(strStringValue);
                queryPlugin.setEditable(boolEditable);

                queryPlugin.setDescription(strDescription);
                queryPlugin.setModifiedDate(Chronos.getCalendarDateNow());
                queryPlugin.setModifiedTime(Chronos.getCalendarTimeNow());

                // Update the display
                // Do not update the editable checkbox,
                // because it doesn't need to be redrawn,
                // and the change would produce an event we don't want
                textValue.setText(queryPlugin.getResource().toString());
                textDescription.setText(queryPlugin.getDescription());
                textDateModified.setText(ChronosHelper.toDateString(queryPlugin.getModifiedDate()));
                textTimeModified.setText(ChronosHelper.toTimeString(queryPlugin.getModifiedTime()));

                // The edit was completed successfully!
                saveQuery(queryPlugin);

                // Prevent further user interaction
                buttonCommit.setEnabled(false);
                buttonRevert.setEnabled(false);

                // Log the change
                strLogText = METADATA_QUERY_EDIT + DELIMITER;
                strLogText = strLogText + "[name=" + queryPlugin.getPathname() + TERMINATOR + DELIMITER;
                strLogText = strLogText + "[value=" + queryPlugin.getResource().toString() + TERMINATOR + DELIMITER;
                strLogText = strLogText + "[editable=" + queryPlugin.isEditable() + TERMINATOR + DELIMITER;
                strLogText = strLogText + "[count=" + queryPlugin.getExecutionCount() + TERMINATOR + DELIMITER;

                LOGGER.logAtomEvent(REGISTRY.getFramework(),
                                    REGISTRY.getFramework().getRootTask(),
                                    this.getClass().getName(),
                                    strLogText,
                                    EventStatus.INFO);

                // The DataDaemon can write Query data when it next runs
                queryPlugin.setUpdateAllowed(true);
                queryPlugin.setUpdated(true);
                }
            };

        buttonCommit.addActionListener(commitListener);

        //------------------------------------------------------------------------------------------
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
                // These changes will enable the buttons!

                queryPlugin.setEditable(boolSavedEditable);
                checkEditable.setSelected(queryPlugin.isEditable());

                queryPlugin.setExecutionCount(longSavedExecutionCount);
                textExecutionCount.setText(Long.toString(queryPlugin.getExecutionCount()));

                queryPlugin.setExecutionTime(longSavedExecutionTime);
                textExecutionTime.setText(Long.toString(queryPlugin.getExecutionTime()));

                queryPlugin.setResource(objSavedValue);
                textValue.setText(queryPlugin.getResource().toString());

                queryPlugin.setDescription(strSavedDescription);
                textDescription.setText(queryPlugin.getDescription());

                queryPlugin.setModifiedDate(dateSavedDateModified);
                textDateModified.setText(ChronosHelper.toDateString(queryPlugin.getModifiedDate()));

                queryPlugin.setModifiedTime(timeSavedTimeModified);
                textTimeModified.setText(ChronosHelper.toTimeString(queryPlugin.getModifiedTime()));

                queryPlugin.setUpdated(false);

                // Prevent further user interaction
                buttonCommit.setEnabled(false);
                buttonRevert.setEnabled(false);

                // The database daemon can write data again (but not this Property)
                queryPlugin.setUpdateAllowed(true);
                }
            };

        buttonRevert.addActionListener(revertListener);

        //------------------------------------------------------------------------------------------
        // The ResetCount button and its listener

        buttonResetCount = createButton(getTextColour(),
                                        getLabelFont(),
                                        "Label.ResetCount",
                                        "Tooltip.ResetCount",
                                        "buttonResetCount",
                                        true);

        final ActionListener resetListener = new ActionListener()
            {
            public void actionPerformed(final ActionEvent event)
                {
                queryPlugin.setExecutionCount(0);
                queryPlugin.setExecutionTime(0);

                textExecutionCount.setText(Long.toString(queryPlugin.getExecutionCount()));
                textExecutionTime.setText(Long.toString(queryPlugin.getExecutionTime()));

                dataChanged();
                }
            };

        buttonResetCount.addActionListener(resetListener);

        //------------------------------------------------------------------------------------------
        // Put all the panels together in the right order

        intLabelHeight = (int)(DIM_ROW_SPACER.getHeight() * ROW_COUNT)
                          + (EditorUtilities.HEIGHT_ROW * (ROW_COUNT-1))
                          + HEIGHT_QUERYEDIT
                          + HEIGHT_DESCRIPTION;

        panelEditor = EditorUtilities.createEditorPanel(getCanvasColour());
        panelLabel = EditorUtilities.createLabelPanel(getCanvasColour(), intLabelHeight);
        panelData = EditorUtilities.createDataPanel(getCanvasColour());

        vecButtons = new Vector();
        vecButtons.add(buttonResetCount);
        vecButtons.add(buttonRevert);
        vecButtons.add(buttonCommit);
        panelButtons = EditorUtilities.createButtonPanel(getCanvasColour(), vecButtons);

        panelLabel.add(labelName);
        panelLabel.add(Box.createRigidArea(DIM_ROW_SPACER));
        panelLabel.add(labelEditable);
        panelLabel.add(Box.createRigidArea(DIM_ROW_SPACER));
        panelLabel.add(labelValue);
        panelLabel.add(Box.createRigidArea(DIM_QUERYEDIT_SPACER));
        panelLabel.add(labelDescription);
        panelLabel.add(Box.createRigidArea(DIM_DESCRIPTION_SPACER));
        panelLabel.add(labelExecutionCount);
        panelLabel.add(Box.createRigidArea(DIM_ROW_SPACER));
        panelLabel.add(labelExecutionTime);
        panelLabel.add(Box.createRigidArea(DIM_ROW_SPACER));
        panelLabel.add(labelDateCreated);
        panelLabel.add(Box.createRigidArea(DIM_ROW_SPACER));
        panelLabel.add(labelTimeCreated);
        panelLabel.add(Box.createRigidArea(DIM_ROW_SPACER));
        panelLabel.add(labelDateModified);
        panelLabel.add(Box.createRigidArea(DIM_ROW_SPACER));
        panelLabel.add(labelTimeModified);

        panelData.add(textName);
        panelData.add(Box.createRigidArea(DIM_ROW_SPACER));
        panelData.add(checkEditable);
        panelData.add(Box.createRigidArea(DIM_ROW_SPACER));

        // Remove the Query value sizes...
        textValue.setMinimumSize(null);
        textValue.setPreferredSize(null);
        textValue.setMaximumSize(null);

        // Put the Query value in a JScrollPane, sized appropriately
        scrollPane = new JScrollPane(textValue);
        scrollPane.setMinimumSize(new Dimension(0, HEIGHT_QUERYEDIT));
        scrollPane.setMaximumSize(new Dimension(MAX_UNIVERSE, HEIGHT_QUERYEDIT));
        scrollPane.setPreferredSize(new Dimension(EditorUtilities.DIM_MAGIC, HEIGHT_QUERYEDIT));

        panelData.add(scrollPane);
        panelData.add(Box.createRigidArea(DIM_ROW_SPACER));
        panelData.add(textDescription);
        panelData.add(Box.createRigidArea(DIM_ROW_SPACER));

        panelData.add(textExecutionCount);
        panelData.add(Box.createRigidArea(DIM_ROW_SPACER));
        panelData.add(textExecutionTime);
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
     * Save the QueryData for later Revert.
     *
     * @param queryPlugin
     */

    private void saveQuery(final QueryPlugin queryPlugin)
        {
        boolSavedEditable = queryPlugin.isEditable();
        longSavedExecutionCount = queryPlugin.getExecutionCount();
        longSavedExecutionTime = queryPlugin.getExecutionTime();
        objSavedValue = queryPlugin.getResource();
        strSavedDescription = EditorUtilities.replaceNull(queryPlugin.getDescription());
        dateSavedDateModified = EditorUtilities.replaceNull(queryPlugin.getModifiedDate());
        timeSavedTimeModified = EditorUtilities.replaceNull(queryPlugin.getModifiedTime());
        }


    /***********************************************************************************************
     * A utility to indicate when the data has changed as a result of an edit.
     */

    private void dataChanged()
        {
        queryPlugin.setUpdateAllowed(false);
        buttonCommit.setEnabled(true);
        buttonRevert.setEnabled(true);
        }
    }


//--------------------------------------------------------------------------------------------------
// End of File

