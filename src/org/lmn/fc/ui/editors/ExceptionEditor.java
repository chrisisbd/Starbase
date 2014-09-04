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
//  30-10-03    LMN created from StringData
//  19-10-04    LMN changed for DateCreated, TimeCreated, DateModified, TimeModified columns
//
//--------------------------------------------------------------------------------------------------

package org.lmn.fc.ui.editors;

//--------------------------------------------------------------------------------------------------
// Imports

import org.lmn.fc.common.exceptions.FrameworkException;
import org.lmn.fc.common.utilities.time.Chronos;
import org.lmn.fc.common.utilities.time.ChronosHelper;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.resources.ExceptionPlugin;
import org.lmn.fc.model.root.UserObjectPlugin;
import org.lmn.fc.ui.components.EditorUIComponent;
import org.lmn.fc.ui.components.EditorUtilities;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.*;
import java.util.GregorianCalendar;


/***************************************************************************************************
 * The ExceptionEditor used by the FrameworkManager.
 *
 * ToDo Focus traversal policy
 * ToDo String Resources
 * ToDo Exceptions
 * ToDo show/choose Language
 */

public final class ExceptionEditor extends EditorUIComponent
    {
    // String Resources
    private static final String RESOURCE_KEY = "Editor.Exception.";

    // The number of standard height rows (i.e. not the Description)
    private static final int ROW_COUNT = 7;

    private ExceptionPlugin exceptionPlugin;        // The String being edited

    private boolean boolSavedEditable;          // Saved data for Revert function
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
    private JLabel labelValue;
    private JLabel labelDateCreated;
    private JLabel labelTimeCreated;
    private JLabel labelDateModified;
    private JLabel labelTimeModified;
    private JLabel labelDescription;

    private JTextField textName;
    private JCheckBox checkEditable;
    private JTextField textValue;
    private JTextField textDateCreated;
    private JTextField textTimeCreated;
    private JTextField textDateModified;
    private JTextField textTimeModified;
    private JTextArea textDescription;

    private JButton buttonRevert;
    private JButton buttonCommit;


    /***********************************************************************************************
     * Constructor creates a editor panel for the supplied ExceptionData object.
     *
     * @param plugin
     */

    public ExceptionEditor(final UserObjectPlugin plugin)
        {
        // Create the EditorUtilities for <Framework>.Editor.Exception.
        super(REGISTRY.getFrameworkResourceKey() + RESOURCE_KEY);

        try
            {
            // Save the String to be edited
            exceptionPlugin = (ExceptionPlugin)plugin;
            saveException(exceptionPlugin);
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

        // The left-hand label panel

        labelName = createLabel(getTextColour(),
                                getLabelFont(),
                                "Label.Name");

        labelEditable = createLabel(getTextColour(),
                                    getLabelFont(),
                                    "Label.Editable");

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
                                   exceptionPlugin.getPathname(),
                                   "Tooltip.Name",
                                   false);

        checkEditable = createCheckBox(getCanvasColour(),
                                       "Tooltip.Editable",
                                       exceptionPlugin.isEditable(),
                                       exceptionPlugin.isEditable());

        textValue = createTextField(getTextColour(),
                                    getDataFont(),
                                    exceptionPlugin.getResource().toString(),
                                    "Tooltip.Value",
                                    exceptionPlugin.isEditable());

        textDateCreated = createTextField(getTextColour(),
                                          getDataFont(),
                                          ChronosHelper.toDateString(exceptionPlugin.getCreatedDate()),
                                          "Tooltip.Created",
                                          false);

        textTimeCreated = createTextField(getTextColour(),
                                          getDataFont(),
                                          ChronosHelper.toTimeString(exceptionPlugin.getCreatedTime()),
                                          "Tooltip.Created",
                                          false);

        textDateModified = createTextField(getTextColour(),
                                           getDataFont(),
                                           ChronosHelper.toDateString(exceptionPlugin.getModifiedDate()),
                                           "Tooltip.Modified",
                                           false);

        textTimeModified = createTextField(getTextColour(),
                                           getDataFont(),
                                           ChronosHelper.toTimeString(exceptionPlugin.getModifiedTime()),
                                           "Tooltip.Modified",
                                           false);

        // Adjust the sizes of the DateTime fields to reduce screen clutter
        EditorUtilities.adjustNarrowField(textDateCreated);
        EditorUtilities.adjustNarrowField(textTimeCreated);
        EditorUtilities.adjustNarrowField(textDateModified);
        EditorUtilities.adjustNarrowField(textTimeModified);

        textDescription = createTextArea(getTextColour(),
                                         getDataFont(),
                                         exceptionPlugin.getDescription(),
                                         "Tooltip.Description",
                                         HEIGHT_DESCRIPTION,
                                         exceptionPlugin.isEditable());

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
                    }
                else
                    {
                    textValue.setEnabled(false);
                    textDescription.setEnabled(false);
                    }
                }
            };

        checkEditable.addItemListener(editableListener);

        // StringValue text box
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

        // PropertyDescription text box
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

                if (strDescription.length() > ExceptionPlugin.DESCRIPTION_LENGTH)
                    {
                    strDescription = strDescription.substring(0, ExceptionPlugin.DESCRIPTION_LENGTH-1);

                    JOptionPane.showMessageDialog(null,
                                                  REGISTRY.getString(getResourceKey() + "Warning.TruncatedDescription") + SPACE + ExceptionPlugin.DESCRIPTION_LENGTH + " characters",
                                                  REGISTRY.getString(getResourceKey() + "Title"),
                                                  JOptionPane.WARNING_MESSAGE);
                    }

                // Check that the Value is not too large
                // Issue a truncation warning if so
                strStringValue = textValue.getText();

                if (strStringValue.length() > ExceptionPlugin.EXCEPTION_LENGTH)
                    {
                    strStringValue = strStringValue.substring(0, ExceptionPlugin.EXCEPTION_LENGTH-1);

                    JOptionPane.showMessageDialog(null,
                                                  REGISTRY.getString(getResourceKey() + "Warning.TruncatedValue") + SPACE + ExceptionPlugin.EXCEPTION_LENGTH + " characters",
                                                  REGISTRY.getString(getResourceKey() + "Title"),
                                                  JOptionPane.WARNING_MESSAGE);
                    }

                // Set all changed values ready for the update
                exceptionPlugin.setResource(strStringValue);
                exceptionPlugin.setEditable(boolEditable);

                // Normalise the format of the created date
//                exceptionData.setCreatedDate(normaliseDate(exceptionData.getCreatedDate()));
                exceptionPlugin.setModifiedDate(Chronos.getCalendarDateNow());
                exceptionPlugin.setModifiedTime(Chronos.getCalendarTimeNow());
                exceptionPlugin.setDescription(strDescription);

                // Update the display
                // Do not update the editable checkbox or the classname drop-down,
                // because they don't need to be redrawn,
                // and the change would produce an event we don't want
                textValue.setText(exceptionPlugin.getResource().toString());
                textDateCreated.setText(ChronosHelper.toDateString(exceptionPlugin.getCreatedDate()));
                textTimeCreated.setText(ChronosHelper.toTimeString(exceptionPlugin.getCreatedTime()));
                textDateModified.setText(ChronosHelper.toDateString(exceptionPlugin.getModifiedDate()));
                textTimeModified.setText(ChronosHelper.toTimeString(exceptionPlugin.getModifiedTime()));
                textDescription.setText(exceptionPlugin.getDescription());

                // The edit was completed successfully!
                saveException(exceptionPlugin);

                // Prevent further user interaction
                buttonCommit.setEnabled(false);
                buttonRevert.setEnabled(false);

                // Log the change
                strLogText = METADATA_EXCEPTION_EDIT + DELIMITER;
                strLogText = strLogText + "[name" + exceptionPlugin.getPathname() + "] ";
                strLogText = strLogText + "[value=" + exceptionPlugin.getResource().toString() + "] ";
                strLogText = strLogText + "[datatype=" + exceptionPlugin.getDataType() + "] ";
                strLogText = strLogText + "[editable=" + exceptionPlugin.isEditable() + "] ";

                LOGGER.logAtomEvent(REGISTRY.getFramework(),
                                    REGISTRY.getFramework().getRootTask(),
                                    this.getClass().getName(),
                                    strLogText,
                                    EventStatus.INFO);

                // The DataDaemon can write Exception data when it next runs
                exceptionPlugin.setUpdateAllowed(true);
                exceptionPlugin.setUpdated(true);
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

                exceptionPlugin.setEditable(boolSavedEditable);
                checkEditable.setSelected(exceptionPlugin.isEditable());

                exceptionPlugin.setResource(objSavedValue);
                textValue.setText(exceptionPlugin.getResource().toString());

                exceptionPlugin.setDescription(strSavedDescription);
                textDescription.setText(exceptionPlugin.getDescription());

                exceptionPlugin.setModifiedDate(dateSavedDateModified);
                exceptionPlugin.setModifiedTime(timeSavedTimeModified);
                textDateModified.setText(ChronosHelper.toDateString(exceptionPlugin.getModifiedDate()));
                textTimeModified.setText(ChronosHelper.toTimeString(exceptionPlugin.getModifiedTime()));

                exceptionPlugin.setUpdated(false);

                // Prevent further user interaction
                buttonCommit.setEnabled(false);
                buttonRevert.setEnabled(false);

                // The database daemon can write data again (but not this Property)
                exceptionPlugin.setUpdateAllowed(true);
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
        panelLabel.add(labelEditable);
        panelLabel.add(Box.createRigidArea(DIM_ROW_SPACER));
        panelLabel.add(labelValue);
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
        panelData.add(checkEditable);
        panelData.add(Box.createRigidArea(DIM_ROW_SPACER));
        panelData.add(textValue);
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
     * Save the ExceptionData for later Revert.
     *
     * @param plugin
     */

    private void saveException(final ExceptionPlugin plugin)
        {
        boolSavedEditable = plugin.isEditable();
        objSavedValue = plugin.getResource();
        strSavedDescription = EditorUtilities.replaceNull(plugin.getDescription());
        dateSavedDateModified = EditorUtilities.replaceNull(plugin.getModifiedDate());
        timeSavedTimeModified = EditorUtilities.replaceNull(plugin.getModifiedTime());
        }


    /***********************************************************************************************
     * A utility to indicate when the data has changed as a result of an edit.
     */

    private void dataChanged()
        {
        exceptionPlugin.setUpdateAllowed(false);
        buttonCommit.setEnabled(true);
        buttonRevert.setEnabled(true);
        }
    }


//--------------------------------------------------------------------------------------------------
// End of File

