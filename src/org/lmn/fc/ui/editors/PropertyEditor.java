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
//  14-02-02    LMN created file
//  18-02-02    LMN added Editable check and message boxes
//  22-04-02    LMN added new Astronomy DataTypes
//  22-07-03    LMN changed for new Properties storage and data types
//  24-07-03    LMN finished error handling
//  25-07-03    LMN made layout work correctly?!
//  29-07-03    LMN added editable checkbox, event logging, tidied behaviour
//  24-10-03    LMN extended EditorUtilities
//  19-10-04    LMN changed for DateCreated, TimeCreated, DateModified, TimeModified columns
//  06-08-06    LMN making changes for new structure...
//
//--------------------------------------------------------------------------------------------------

package org.lmn.fc.ui.editors;

//--------------------------------------------------------------------------------------------------
// Imports

import org.lmn.fc.common.exceptions.FrameworkException;
import org.lmn.fc.common.utilities.time.Chronos;
import org.lmn.fc.common.utilities.time.ChronosHelper;
import org.lmn.fc.model.datatypes.DataTypeDictionary;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.registry.impl.BeanFactoryXml;
import org.lmn.fc.model.resources.PropertyPlugin;
import org.lmn.fc.model.root.UserObjectPlugin;
import org.lmn.fc.ui.components.EditorUIComponent;
import org.lmn.fc.ui.components.EditorUtilities;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.*;
import java.util.GregorianCalendar;
import java.util.Vector;


/***************************************************************************************************
 * The PropertyEditor used by the FrameworkManager.
 *
 * ToDo Focus traversal policy
 * ToDo String Resources
 * ToDo Exceptions
 */

public final class PropertyEditor extends EditorUIComponent
    {
    private static final BeanFactoryXml BEAN_FACTORY_XML = BeanFactoryXml.getInstance();

    // String Resources
    private static final String RESOURCE_KEY = "Editor.Property.";

    // The number of standard height rows (i.e. not the Description)
    private static final int ROW_COUNT = 8;

    private PropertyPlugin propertyPlugin;      // The Property being edited

    private boolean boolSavedEditable;          // Saved data for Revert function
    private Object objSavedValue;
    private String strSavedDataType;
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
    private JLabel labelClassName;
    private JLabel labelDateCreated;
    private JLabel labelTimeCreated;
    private JLabel labelDateModified;
    private JLabel labelTimeModified;
    private JLabel labelDescription;

    private JTextField textName;
    private JCheckBox checkEditable;
    private JTextField textValue;
    private JComboBox comboDataType;
    private JTextField textDateCreated;
    private JTextField textTimeCreated;
    private JTextField textDateModified;
    private JTextField textTimeModified;
    private JTextArea textDescription;

    private JButton buttonRevert;
    private JButton buttonCommit;


    /***********************************************************************************************
     * Constructor creates a editor panel for the supplied PropertyData object.
     *
     * @param plugin
     */

    public PropertyEditor(final UserObjectPlugin plugin)
        {
        // Create the EditorUtilities for <Framework>.Editor.Property.
        super(REGISTRY.getFrameworkResourceKey() + RESOURCE_KEY);

        try
            {
            // Save the Property to be edited
            propertyPlugin = (PropertyPlugin)plugin;
            saveProperty(propertyPlugin);
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

        labelClassName = createLabel(getTextColour(),
                                     getLabelFont(),
                                     "Label.DataType");

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
                                   propertyPlugin.getPathname(),
                                   "Tooltip.Name",
                                   false);

        checkEditable = createCheckBox(getCanvasColour(),
                                       "Tooltip.Editable",
                                       propertyPlugin.isEditable(),
                                       propertyPlugin.isEditable());

        textValue = createTextField(getTextColour(),
                                    getDataFont(),
                                    propertyPlugin.getResource().toString(),
                                    "Tooltip.Value",
                                    propertyPlugin.isEditable());


        comboDataType = createComboBox(getTextColour(),
                                       getDataFont(),
                                       "Tooltip.DataType",
                                       propertyPlugin.isEditable(),
                                       new Vector(1).iterator(),
                                       "");
        comboDataType.removeAllItems();

        final DataTypeDictionary[] types;

        types = DataTypeDictionary.values();

        for (int i = 0;
             i < types.length;
             i++)
            {
            DataTypeDictionary type = types[i];
            comboDataType.addItem(type.getName());

            }

        comboDataType.setSelectedItem(propertyPlugin.getDataType());

        textDateCreated = createTextField(getTextColour(),
                                          getDataFont(),
                                          ChronosHelper.toDateString(propertyPlugin.getCreatedDate()),
                                          "Tooltip.Created",
                                          false);

        textTimeCreated = createTextField(getTextColour(),
                                          getDataFont(),
                                          ChronosHelper.toTimeString(propertyPlugin.getCreatedTime()),
                                          "Tooltip.Created",
                                          false);

        textDateModified = createTextField(getTextColour(),
                                           getDataFont(),
                                           ChronosHelper.toDateString(propertyPlugin.getModifiedDate()),
                                           "Tooltip.Modified",
                                           false);

        textTimeModified = createTextField(getTextColour(),
                                           getDataFont(),
                                           ChronosHelper.toTimeString(propertyPlugin.getModifiedTime()),
                                           "Tooltip.Modified",
                                           false);

        // Adjust the sizes of the DateTime fields to reduce screen clutter
        EditorUtilities.adjustNarrowField(textDateCreated);
        EditorUtilities.adjustNarrowField(textTimeCreated);
        EditorUtilities.adjustNarrowField(textDateModified);
        EditorUtilities.adjustNarrowField(textTimeModified);

        textDescription = createTextArea(getTextColour(),
                                         getDataFont(),
                                         propertyPlugin.getDescription(),
                                         "Tooltip.Description",
                                         HEIGHT_DESCRIPTION,
                                         propertyPlugin.isEditable());

        //------------------------------------------------------------------------------------------
        // Add the ActionListeners now we have stopped changing component states

        // Editable checkbox
        final ItemListener editableListener = new ItemListener()
            {
            public void itemStateChanged(final ItemEvent event)
                {
                dataChanged();

                if (event.getStateChange() == ItemEvent.SELECTED)
                    {
                    textValue.setEnabled(true);
                    comboDataType.setEnabled(true);
                    textDescription.setEnabled(true);
                    }
                else
                    {
                    textValue.setEnabled(false);
                    comboDataType.setEnabled(false);
                    textDescription.setEnabled(false);
                    }
                }
            };

        checkEditable.addItemListener(editableListener);

        // DataType ClassName drop-down
        final ActionListener typeListener = new ActionListener()
            {
            public void actionPerformed(final ActionEvent event)
                {
                dataChanged();

                // Warn the user that changing the data type could be a problem...
                if (!comboDataType.getSelectedItem().equals(strSavedDataType))
                    {
                    JOptionPane.showMessageDialog(null,
                                                  REGISTRY.getString(getResourceKey() + "Warning.DataType"),
                                                  REGISTRY.getString(getResourceKey() + "Title"),
                                                  JOptionPane.WARNING_MESSAGE);
                    }
                }
            };

        comboDataType.addActionListener(typeListener);

        // PropertyValue text box
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
                final String strClassName;
                final boolean boolEditable;
                String strPropertyValue;
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

                if (strDescription.length() > PropertyPlugin.DESCRIPTION_LENGTH)
                    {
                    strDescription = strDescription.substring(0, PropertyPlugin.DESCRIPTION_LENGTH-1);

                    JOptionPane.showMessageDialog(null,
                                                  REGISTRY.getString(getResourceKey() + "Warning.TruncatedDescription") + SPACE + PropertyPlugin.DESCRIPTION_LENGTH + " characters",
                                                  REGISTRY.getString(getResourceKey() + "Title"),
                                                  JOptionPane.WARNING_MESSAGE);
                    }

                // Get the selected DataType
                strClassName = (String)comboDataType.getSelectedItem();

                // Check that the Value is not too large
                // Issue a truncation warning if so
                strPropertyValue = textValue.getText();

                if (strPropertyValue.length() > PropertyPlugin.VALUE_LENGTH)
                    {
                    strPropertyValue = strPropertyValue.substring(0, PropertyPlugin.VALUE_LENGTH-1);

                    JOptionPane.showMessageDialog(null,
                                                  REGISTRY.getString(getResourceKey() + "Warning.TruncatedValue") + SPACE + PropertyPlugin.VALUE_LENGTH + " characters",
                                                  REGISTRY.getString(getResourceKey() + "Title"),
                                                  JOptionPane.WARNING_MESSAGE);
                    }

                // Try to instantiate the Property from the given (text) value
                final Object objProperty = BEAN_FACTORY_XML.validateResourceDataType(strClassName, strPropertyValue);

                if (objProperty != null)
                    {
                    // Record the instantiation, since this may be the first time...
                    propertyPlugin.setInstantiated(true);

                    // Set all changed values ready for the update
                    propertyPlugin.setResource(objProperty);
                    propertyPlugin.setEditable(boolEditable);
                    propertyPlugin.setDataType(strClassName);
                    propertyPlugin.setDescription(strDescription);
                    propertyPlugin.setModifiedDate(Chronos.getCalendarDateNow());
                    propertyPlugin.setModifiedTime(Chronos.getCalendarTimeNow());

                    // Update the display
                    // Do not update the editable checkbox or the classname drop-down,
                    // because they don't need to be redrawn,
                    // and the change would produce an event we don't want
                    textValue.setText(propertyPlugin.getResource().toString());
                    textDescription.setText(propertyPlugin.getDescription());
                    textDateCreated.setText(ChronosHelper.toDateString(propertyPlugin.getCreatedDate()));
                    textTimeCreated.setText(ChronosHelper.toTimeString(propertyPlugin.getCreatedTime()));
                    textDateModified.setText(ChronosHelper.toDateString(propertyPlugin.getModifiedDate()));
                    textTimeModified.setText(ChronosHelper.toTimeString(propertyPlugin.getModifiedTime()));

                    // The edit was completed successfully!
                    saveProperty(propertyPlugin);

                    // Prevent further user interaction
                    buttonCommit.setEnabled(false);
                    buttonRevert.setEnabled(false);

                    // Log the change
                    strLogText = METADATA_PROPERTY_EDIT + DELIMITER;
                    strLogText = strLogText + "[name=" + propertyPlugin.getPathname() + "] ";
                    strLogText = strLogText + "[value=" + propertyPlugin.getResource().toString() + "] ";
                    strLogText = strLogText + "[datatype=" + propertyPlugin.getDataType() + "] ";
                    strLogText = strLogText + "[editable=" + propertyPlugin.isEditable() + "] ";

                    LOGGER.logAtomEvent(REGISTRY.getFramework(),
                                        REGISTRY.getFramework().getRootTask(),
                                        this.getClass().getName(),
                                        strLogText,
                                        EventStatus.INFO);

                    // The DataDaemon can write Property data when it next runs
                    propertyPlugin.setUpdateAllowed(true);
                    propertyPlugin.setUpdated(true);
                    }
                else
                    {
                    // Could not instantiate the Property, so ask the user to try again
                    // Do not make any changes to the stored data
                    JOptionPane.showMessageDialog(null,
                                                  REGISTRY.getString(getResourceKey() + "Invalid.Property") + strClassName,
                                                  REGISTRY.getString(getResourceKey() + "Invalid.Entry"),
                                                  JOptionPane.ERROR_MESSAGE);
                    }
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

                propertyPlugin.setEditable(boolSavedEditable);
                checkEditable.setSelected(propertyPlugin.isEditable());

                propertyPlugin.setResource(objSavedValue);
                textValue.setText(propertyPlugin.getResource().toString());

                propertyPlugin.setDataType(strSavedDataType);
                comboDataType.setSelectedItem(propertyPlugin.getDataType());

                propertyPlugin.setDescription(strSavedDescription);
                textDescription.setText(propertyPlugin.getDescription());

                propertyPlugin.setModifiedDate(dateSavedDateModified);
                propertyPlugin.setModifiedTime(timeSavedTimeModified);
                textDateModified.setText(ChronosHelper.toDateString(propertyPlugin.getModifiedDate()));
                textTimeModified.setText(ChronosHelper.toTimeString(propertyPlugin.getModifiedTime()));

                propertyPlugin.setUpdated(false);

                // Prevent further user interaction
                buttonCommit.setEnabled(false);
                buttonRevert.setEnabled(false);

                // The database daemon can write data again (but not this Property)
                propertyPlugin.setUpdateAllowed(true);
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
        panelLabel.add(labelClassName);
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
        panelData.add(comboDataType);
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
     * Save the PropertyData for later Revert.
     *
     * @param plugin
     */

    private void saveProperty(final PropertyPlugin plugin)
        {
        boolSavedEditable = plugin.isEditable();
        objSavedValue = plugin.getResource();
        strSavedDataType = EditorUtilities.replaceNull(plugin.getDataType());
        strSavedDescription = EditorUtilities.replaceNull(plugin.getDescription());
        dateSavedDateModified = EditorUtilities.replaceNull(plugin.getModifiedDate());
        timeSavedTimeModified = EditorUtilities.replaceNull(plugin.getModifiedTime());
        }


    /***********************************************************************************************
     * A utility to indicate when the data has changed as a result of an edit.
     */

    private void dataChanged()
        {
        propertyPlugin.setUpdateAllowed(false);
        buttonCommit.setEnabled(true);
        buttonRevert.setEnabled(true);
        }
    }


//--------------------------------------------------------------------------------------------------
// End of File
