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
//  19-02-02    LMN created file
//  13-08-03    LMN changing for improved layout as in PropertyEditor
//  22-08-03    LMN finished layout changes...
//  30-08-03    LMN finishing Icon and Help file browsers
//  28-10-03    LMN extending EditorUtilities
//  18-11-03    LMN changed constructor parameter to AtomData
//  19-10-04    LMN changed for DateCreated, TimeCreated, DateModified, TimeModified columns
//  25-10-04    LMN fixed Active bug, added Labels etc.
//
//--------------------------------------------------------------------------------------------------
package org.lmn.fc.ui.editors;


//--------------------------------------------------------------------------------------------------
// Imports

import org.lmn.fc.common.exceptions.FrameworkException;
import org.lmn.fc.common.utilities.time.Chronos;
import org.lmn.fc.common.utilities.time.ChronosHelper;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.plugins.AtomPlugin;
import org.lmn.fc.model.registry.RegistryModelPlugin;
import org.lmn.fc.model.registry.RegistryModelUtilities;
import org.lmn.fc.model.root.RootPlugin;
import org.lmn.fc.model.root.UserObjectPlugin;
import org.lmn.fc.ui.components.EditorUIComponent;
import org.lmn.fc.ui.components.EditorUtilities;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.GregorianCalendar;
import java.util.Vector;


/***************************************************************************************************
 * The PluginEditor.
 *
 * ToDo Focus traversal policy
 * ToDo String Resources
 * ToDo Exceptions
 */
public final class PluginEditor extends EditorUIComponent
    {
    // String Resources
    private static final String RESOURCE_KEY = "Editor.Plugin.";

    // The number of standard height rows (i.e. not the Description)
    private static final int ROW_COUNT = 10;

    private final AtomPlugin plugin;
    private boolean boolSavedActive;
    private boolean boolSavedEditable;
    private boolean boolSavedLoadAtStart;
    private String strSavedIconFilename;
    private String strSavedHelpFilename;
    private String strSavedDescription;
    private GregorianCalendar dateSavedDateModified;
    private GregorianCalendar timeSavedTimeModified;
    private JPanel panelEditor;
    private JPanel panelLabel;
    private JPanel panelData;
    private JPanel panelButtons;
    private JPanel panelBrowserIcon;
    private JPanel panelBrowserHelp;
    private JLabel labelName;
    private JLabel labelActive;
    private JLabel labelEditable;
    private JLabel labelLoadAtStart;
    private JLabel labelIconFilename;
    private JLabel labelHelpFilename;
    private JLabel labelDateCreated;
    private JLabel labelTimeCreated;
    private JLabel labelDateModified;
    private JLabel labelTimeModified;
    private JLabel labelDescription;
    private JTextField textName;
    private JCheckBox checkActive;
    private JCheckBox checkEditable;
    private JCheckBox checkLoadAtStart;
    private JTextField textIconFilename;
    private JTextField textHelpFilename;
    private JTextField textDateCreated;
    private JTextField textTimeCreated;
    private JTextField textDateModified;
    private JTextField textTimeModified;
    private JTextArea textDescription;
    private Vector<JButton> vecButtons;
    private JButton buttonScript;
    private JButton buttonRevert;
    private JButton buttonCommit;
    private JButton buttonBrowserIcon;
    private JButton buttonBrowserHelp;


    /***********************************************************************************************
     * Constructor creates a editor panel for the supplied Application.
     *
     * @param rootdata
     */
    public PluginEditor(final UserObjectPlugin rootdata)
        {
        // Create the EditorUtilities for <Framework>.Editor.Plugin.
        super(REGISTRY.getFrameworkResourceKey() + RESOURCE_KEY);

        // Save the Application to be edited by upcasting the incoming AtomData
        plugin = (AtomPlugin)rootdata;
        saveApplication(plugin);

        // Read colours etc.
        readResources();

        // Attempt to lay out the Editor
        if(!createEditorPanel())
            {
            LOGGER.handleAtomException(REGISTRY.getFramework(),
                                       REGISTRY.getFramework().getRootTask(),
                                       this.getClass().getName(),
                                       new FrameworkException("Todo could not create editor"),
                                       "TODO EXCEPTION_CREATE_APPLICATION",
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

        labelLoadAtStart = createLabel(getTextColour(),
                                       getLabelFont(),
                                       "Label.LoadAtStart");

        labelIconFilename = createLabel(getTextColour(),
                                        getLabelFont(),
                                        "Label.IconFilename");

        labelHelpFilename = createLabel(getTextColour(),
                                        getLabelFont(),
                                        "Label.HelpFilename");

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

        //  The right-hand data panel
        textName =
            createTextField(getTextColour(),
                            getDataFont(),
                            plugin.getPathname(),
                            "Tooltip.Name",
                            false);

        checkActive =
            createCheckBox(getCanvasColour(),
                           "Tooltip.Active",
                           plugin.isEditable(),
                           plugin.isActive());

        checkEditable =
            createCheckBox(getCanvasColour(),
                           "Tooltip.Editable",
                           plugin.isEditable(),
                           plugin.isEditable());


        checkLoadAtStart =
            createCheckBox(getCanvasColour(),
                           "Tooltip.LoadAtStart",
                           true,
                           plugin.isLoadAtStart());

        textIconFilename =
            createTextField(getTextColour(),
                            getDataFont(),
                            plugin.getIconFilename(),
                            "Tooltip.IconFilename",
                            true);

        textHelpFilename =
            createTextField(getTextColour(),
                            getDataFont(),
                            plugin.getHelpFilename(),
                            "Tooltip.HelpFilename",
                            true);

        textDateCreated =
            createTextField(getTextColour(),
                            getDataFont(),
                            ChronosHelper.toDateString(plugin.getCreatedDate()),
                            "Tooltip.Created",
                            false);

        textTimeCreated =
            createTextField(getTextColour(),
                            getDataFont(),
                            ChronosHelper.toTimeString(plugin.getCreatedTime()),
                            "Tooltip.Created",
                            false);

        textDateModified =
            createTextField(getTextColour(),
                            getDataFont(),
                            ChronosHelper.toDateString(plugin.getModifiedDate()),
                            "Tooltip.Modified",
                            false);

        textTimeModified =
            createTextField(getTextColour(),
                            getDataFont(),
                            ChronosHelper.toTimeString(plugin.getModifiedTime()),
                            "Tooltip.Modified",
                            false);

        // Adjust the sizes of the DateTime fields to reduce screen clutter
        EditorUtilities.adjustNarrowField(textDateCreated);
        EditorUtilities.adjustNarrowField(textTimeCreated);
        EditorUtilities.adjustNarrowField(textDateModified);
        EditorUtilities.adjustNarrowField(textTimeModified);

        textDescription =
            createTextArea(getTextColour(),
                           getDataFont(),
                           plugin.getDescription(),
                           "Tooltip.Description",
                           HEIGHT_DESCRIPTION,
                           plugin.isEditable());


        //------------------------------------------------------------------------------------------
        // Add the ActionListeners now we have stopped changing component states
        // The database daemon must not write data until saveEventLog is complete
        // ApplicationActive
        final ItemListener activeListener =
            new ItemListener()
                {
                public void itemStateChanged(final ItemEvent event)
                    {
                    LOGGER.debug("checkActive changed");

                    // The database daemon must not write data until saveEventLog is complete
                    dataChanged();
                    }
                };

        checkActive.addItemListener(activeListener);

        // ApplicationEditable
        final ItemListener editableListener =
            new ItemListener()
                {
                public void itemStateChanged(final ItemEvent event)
                    {
                    LOGGER.debug("checkEditable changed");

                    // The database daemon must not write data until saveEventLog is complete
                    dataChanged();

                    if(event.getStateChange() == ItemEvent.SELECTED)
                        {
                        checkActive.setEnabled(true);
                        checkLoadAtStart.setEnabled(true);
                        textIconFilename.setEnabled(true);
                        textHelpFilename.setEnabled(true);
                        textDescription.setEnabled(true);
                        buttonBrowserIcon.setEnabled(true);
                        buttonBrowserHelp.setEnabled(true);
                        }
                    else
                        {
                        checkActive.setEnabled(false);
                        checkLoadAtStart.setEnabled(false);
                        textIconFilename.setEnabled(false);
                        textHelpFilename.setEnabled(false);
                        textDescription.setEnabled(false);
                        buttonBrowserIcon.setEnabled(false);
                        buttonBrowserHelp.setEnabled(false);
                        }
                    }
                };

        checkEditable.addItemListener(editableListener);

        // ApplicationLoadAtStart
        final ItemListener listenerLoadAtStart =
            new ItemListener()
                {
                public void itemStateChanged(final ItemEvent event)
                    {
                    LOGGER.debug("checkLoadAtStart changed");

                    // The database daemon must not write data until saveEventLog is complete
                    dataChanged();
                    }
                };

        checkLoadAtStart.addItemListener(listenerLoadAtStart);

        // Text Box Listener
        final DocumentListener listenerText =
            new DocumentListener()
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

        textIconFilename.getDocument().addDocumentListener(listenerText);
        textHelpFilename.getDocument().addDocumentListener(listenerText);
        textDescription.getDocument().addDocumentListener(listenerText);

        //------------------------------------------------------------------------------------------
        // The Commit button and its listener
        buttonCommit =
            createButton(getTextColour(),
                         getLabelFont(),
                         "Label.Commit",
                         "Tooltip.Commit",
                         "buttonCommit",
                         false);

        final ActionListener commitListener =
            new ActionListener()
                {
                public void actionPerformed(final ActionEvent event)
                    {
                    final boolean boolActive;
                    final String strIconFilename;
                    final String strHelpFilename;
                    String strDescription;
                    String strFailureReason;
                    String strLogText;
                    boolean boolCommit;
                    int intChoice;

                    boolCommit = true;
                    strFailureReason = "";

                    // We must check each item for validity, and write to the
                    // RegistryModel if all is Ok, otherwise, give the user another chance
                    // Initialise the chain of choices
                    intChoice = JOptionPane.OK_OPTION;

                    // Warn the user if the Application is being deactivated
                    boolActive = checkActive.isSelected();

                    if((!boolActive) && (boolActive != boolSavedActive))
                        {
                        intChoice =
                            JOptionPane.showOptionDialog(null,
                                                         REGISTRY.getString(getResourceKey() +
                                                                                     "Warning.Deactivating"),
                                                         REGISTRY.getString(getResourceKey() +
                                                                                     "Title"),
                                                         JOptionPane.YES_NO_OPTION,
                                                         JOptionPane.WARNING_MESSAGE,
                                                         null,
                                                         null,
                                                         null);
                        }

                    if(intChoice == JOptionPane.OK_OPTION)
                        {
                        // Warn the user if this Application won't be loaded next time...
                        if(!checkLoadAtStart.isSelected())
                            {
                            intChoice =
                                JOptionPane.showOptionDialog(null,
                                                             REGISTRY.getString(getResourceKey() +
                                                                                         "Warning.LoadAtStart"),
                                                             REGISTRY.getString(getResourceKey() +
                                                                                         "Title"),
                                                             JOptionPane.YES_NO_OPTION,
                                                             JOptionPane.WARNING_MESSAGE,
                                                             null,
                                                             null,
                                                             null);
                            }

                        if(intChoice == JOptionPane.OK_OPTION)
                            {
                            // Check that the Icon filename is valid
                            strIconFilename = textIconFilename.getText().trim();

                            if(!strIconFilename.equals(""))
                                {
                                // See if the Icon file exists
                                final File fileIcon =
                                    RegistryModelUtilities.getCommonImageAsFile(strIconFilename);

                                if(fileIcon.exists())
                                    {
                                    // The file exists, so try to make an Icon of valid size
                                    final ImageIcon imageIcon =
                                        RegistryModelUtilities.getCommonIcon(strIconFilename);
                                    LOGGER.debug("Icon size: width=" +
                                                     imageIcon.getIconWidth() + " height=" +
                                                     imageIcon.getIconHeight());

                                    if((imageIcon.getIconWidth() > RegistryModelPlugin.ICON_WIDTH) ||
                                           (imageIcon.getIconHeight() > RegistryModelPlugin.ICON_HEIGHT))
                                        {
                                        boolCommit = false;
                                        strFailureReason =
                                            strFailureReason + "The Icon size exceeds " +
                                            RegistryModelPlugin.ICON_WIDTH + " by " +
                                            RegistryModelPlugin.ICON_HEIGHT + "\n";
                                        }
                                    }
                                else
                                    {
                                    boolCommit = false;
                                    strFailureReason =
                                        strFailureReason + "The Icon file does not exist\n";
                                    }
                                }

                            // Check that the Help filename is valid
                            strHelpFilename = textHelpFilename.getText().trim();

                            if(!strHelpFilename.equals(""))
                                {
                                // See if the Help file exists
                                final File file =
                                    new File(RegistryModelUtilities.getHelpRoot() +
                                             strHelpFilename);

                                if(!file.exists())
                                    {
                                    boolCommit = false;
                                    strFailureReason =
                                        strFailureReason + "The Help file does not exist\n";
                                    }
                                }

                            // Check that the Description is not too large
                            // Issue a truncation warning if so
                            strDescription = textDescription.getText().trim();

                            if(strDescription.length() > RootPlugin.DESCRIPTION_LENGTH)
                                {
                                strDescription =
                                    strDescription.substring(0,
                                                             RootPlugin.DESCRIPTION_LENGTH -
                                                             1);

                                JOptionPane.showMessageDialog(null,
                                                              REGISTRY.getString(getResourceKey() +
                                                                                          "Warning.TruncatedDescription") +
                                                              SPACE +
                                                              RootPlugin.DESCRIPTION_LENGTH +
                                                              " characters",
                                                              REGISTRY.getString(getResourceKey() +
                                                                                          "Title"),
                                                              JOptionPane.WARNING_MESSAGE);
                                }

                            if(boolCommit)
                                {
                                // Set all changed values ready for the update
                                // This creates an ActiveChangeEvent when the state changes
                                plugin.setActive(checkActive.isSelected());
                                plugin.setEditable(checkEditable.isSelected());
                                plugin.setLoadAtStart(checkLoadAtStart.isSelected());

                                plugin.setIconFilename(strIconFilename);
                                plugin.setHelpFilename(strHelpFilename);
                                plugin.setDescription(strDescription);
                                plugin.setModifiedDate(Chronos.getCalendarDateNow());
                                plugin.setModifiedTime(Chronos.getCalendarTimeNow());

                                // Update the display
                                // Do not update the checkboxes, because they don't need to be redrawn,
                                // and the change would produce an event we don't want
                                textIconFilename.setText(plugin.getIconFilename());
                                textHelpFilename.setText(plugin.getHelpFilename());
                                textDescription.setText(plugin.getDescription());
                                textDateModified.setText(ChronosHelper.toDateString(plugin.getModifiedDate()));
                                textTimeModified.setText(ChronosHelper.toTimeString(plugin.getModifiedTime()));

                                // The edit was completed successfully!
                                saveApplication(plugin);

                                // Prevent further user interaction
                                buttonCommit.setEnabled(false);
                                buttonRevert.setEnabled(false);

                                // Log the changes
                                strLogText = METADATA_PLUGIN_EDIT + DELIMITER;
                                strLogText = strLogText + "[name=" + plugin.getPathname() + "] ";
                                strLogText = strLogText + "[active=" + plugin.isActive() + "] ";
                                strLogText = strLogText + "[editable=" + plugin.isEditable() + "] ";
                                strLogText = strLogText + "[loadatstart=" + plugin.isLoadAtStart() + "] ";
                                strLogText = strLogText + "[iconfilename=" + plugin.getIconFilename() + "] ";
                                strLogText =strLogText + "[helpfilename=" + plugin.getHelpFilename() + "] ";

                                LOGGER.logAtomEvent(plugin,
                                                    plugin.getRootTask(),
                                                    getClass().getName(),
                                                    strLogText,
                                                    EventStatus.INFO);

                                // The database daemon can write Application data when it next runs
                                plugin.setUpdateAllowed(true);
                                plugin.setUpdated(true);
                                }
                            else
                                {
                                // An error was found, so we cannot saveEventLog to the changes
                                JOptionPane.showMessageDialog(null,
                                                              "The changes cannot be saved because:\n" +
                                                              strFailureReason,
                                                              "Invalid Parameters",
                                                              JOptionPane.ERROR_MESSAGE);
                                }
                            }
                        }
                    }
                };

        buttonCommit.addActionListener(commitListener);

        //--------------------------------------------------------------------------------------------------------------------------------------------
        // The Revert button and its listener
        buttonRevert =
            createButton(getTextColour(),
                         getLabelFont(),
                         "Label.Revert",
                         "Tooltip.Revert",
                         "buttonRevert",
                         false);

        final ActionListener revertListener =
            new ActionListener()
                {
                public void actionPerformed(final ActionEvent event)
                    {
                    // Undo any edits
                    // These changes will enable the buttons!
                    plugin.setActive(boolSavedActive);
                    checkActive.setSelected(plugin.isActive());

                    plugin.setEditable(boolSavedEditable);
                    checkEditable.setSelected(plugin.isEditable());

                    plugin.setLoadAtStart(boolSavedLoadAtStart);
                    checkLoadAtStart.setSelected(plugin.isLoadAtStart());

                    plugin.setIconFilename(strSavedIconFilename);
                    textIconFilename.setText(plugin.getIconFilename());

                    plugin.setHelpFilename(strSavedHelpFilename);
                    textHelpFilename.setText(plugin.getHelpFilename());

                    plugin.setDescription(strSavedDescription);
                    textDescription.setText(plugin.getDescription());

                    plugin.setModifiedDate(dateSavedDateModified);
                    plugin.setModifiedTime(timeSavedTimeModified);
                    textDateModified.setText(ChronosHelper.toDateString(plugin.getModifiedDate()));
                    textTimeModified.setText(ChronosHelper.toTimeString(plugin.getModifiedTime()));

                    plugin.setUpdated(false);

                    // Prevent further user interaction
                    buttonCommit.setEnabled(false);
                    buttonRevert.setEnabled(false);

                    // The database daemon can write data again (but not this Application)
                    plugin.setUpdateAllowed(true);
                    }
                };

        buttonRevert.addActionListener(revertListener);

        //------------------------------------------------------------------------------------------
        // The Script button and its listener
        buttonScript =
            createButton(getTextColour(),
                         getLabelFont(),
                         "Label.Script",
                         "Tooltip.Script",
                         "buttonRevert",
                         true);

        final ActionListener scriptListener =
            new ActionListener()
                {
                public void actionPerformed(final ActionEvent event)
                    {
//                    if(plugin.writeScripts(plugin,
//                                                        REGISTRY.getFramework().getExportsFolder()))
//                        {
//                        String[] strMessage =
//                        {
//                            "The Install and Uninstall Scripts have been written to" + SPACE,
//
//
//                        REGISTRY.getFramework().getExportsFolder() +
//                            System.getProperty("file.separator") + plugin.getName() +
//                            FILETYPE_SQL
//                        };
//
//                        JOptionPane.showMessageDialog(null, strMessage,
//                                                      "Application Script Creation",
//                                                      JOptionPane.INFORMATION_MESSAGE);
//                        }
//                    else
//                        {
//                        String[] strMessage =
//                        {"An error has occurred trying to create the DELETE and INSERT scripts"};
//
//                        JOptionPane.showMessageDialog(null, strMessage,
//                                                      "Application Script Creation",
//                                                      JOptionPane.ERROR_MESSAGE);
//                        }
                    }
                };

        buttonScript.addActionListener(scriptListener);

        //------------------------------------------------------------------------------------------
        // The IconBrowser button and its listener
        buttonBrowserIcon =
            createBrowserButton(getTextColour(),
                                getLabelFont(),
                                "Label.IconBrowser",
                                "Tooltip.IconBrowser",
                                "buttonBrowserIcon",
                                true);

        buttonBrowserIcon.addActionListener(
                EditorUtilities.createBrowserListener(RegistryModelUtilities.getCommonImagesRoot(),
                                                      "Select an Icon for this Application",
                                                      textIconFilename));

        //------------------------------------------------------------------------------------------
        // The Help Browser button and its listener
        buttonBrowserHelp =
            createBrowserButton(getTextColour(),
                                getLabelFont(),
                                "Label.HelpBrowser",
                                "Tooltip.HelpBrowser",
                                "buttonBrowserHelp",
                                true);

        buttonBrowserHelp.addActionListener(
                EditorUtilities.createBrowserListener(RegistryModelUtilities.getHelpRoot(),
                                                      "Select a Help file for this Application",
                                                      textHelpFilename));

        //------------------------------------------------------------------------------------------
        // Put all the panels together in the right order
        intLabelHeight =
            (int)(DIM_ROW_SPACER.getHeight() * ROW_COUNT) + (EditorUtilities.HEIGHT_ROW * ROW_COUNT) +
            HEIGHT_DESCRIPTION;

        panelEditor = EditorUtilities.createEditorPanel(getCanvasColour());
        panelLabel = EditorUtilities.createLabelPanel(getCanvasColour(),
                                                      intLabelHeight);
        panelData = EditorUtilities.createDataPanel(getCanvasColour());

        vecButtons = new Vector<JButton>(3);
        //vecButtons.add(buttonScript);
        vecButtons.add(buttonRevert);
        vecButtons.add(buttonCommit);
        panelButtons = EditorUtilities.createButtonPanel(getCanvasColour(),
                                                         vecButtons);

        panelBrowserIcon =
            EditorUtilities.createBrowserPanel(getCanvasColour(),
                                               textIconFilename,
                                               buttonBrowserIcon);

        panelBrowserHelp =
            EditorUtilities.createBrowserPanel(getCanvasColour(),
                                               textHelpFilename,
                                               buttonBrowserHelp);

        panelLabel.add(labelName);
        panelLabel.add(Box.createRigidArea(DIM_ROW_SPACER));
        panelLabel.add(labelActive);
        panelLabel.add(Box.createRigidArea(DIM_ROW_SPACER));
        panelLabel.add(labelEditable);
        panelLabel.add(Box.createRigidArea(DIM_ROW_SPACER));
        panelLabel.add(labelLoadAtStart);
        panelLabel.add(Box.createRigidArea(DIM_ROW_SPACER));
        panelLabel.add(labelIconFilename);
        panelLabel.add(Box.createRigidArea(DIM_ROW_SPACER));
        panelLabel.add(labelHelpFilename);
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
        panelLabel.add(Box.createRigidArea(DIM_ROW_SPACER));

        panelData.add(textName);
        panelData.add(Box.createRigidArea(DIM_ROW_SPACER));
        panelData.add(checkActive);
        panelData.add(Box.createRigidArea(DIM_ROW_SPACER));
        panelData.add(checkEditable);
        panelData.add(Box.createRigidArea(DIM_ROW_SPACER));
        panelData.add(checkLoadAtStart);
        panelData.add(Box.createRigidArea(DIM_ROW_SPACER));
        panelData.add(panelBrowserIcon);
        panelData.add(Box.createRigidArea(DIM_ROW_SPACER));
        panelData.add(panelBrowserHelp);
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
     * Save the Application for later Revert.
     *
     * @param data
     */
    private void saveApplication(final AtomPlugin data)
        {
        boolSavedActive = data.isActive();
        boolSavedEditable = data.isEditable();
        boolSavedLoadAtStart = data.isLoadAtStart();
        strSavedIconFilename = EditorUtilities.replaceNull(data.getIconFilename());
        strSavedHelpFilename = EditorUtilities.replaceNull(data.getHelpFilename());
        strSavedDescription = EditorUtilities.replaceNull(data.getDescription());
        dateSavedDateModified = EditorUtilities.replaceNull(data.getModifiedDate());
        timeSavedTimeModified = EditorUtilities.replaceNull(data.getModifiedTime());
        }


    /***********************************************************************************************
     * A utility to indicate when the data has changed as a result of an edit.
     */
    private void dataChanged()
        {
        plugin.setUpdateAllowed(false);
        buttonCommit.setEnabled(true);
        buttonRevert.setEnabled(true);
        }
    }


//--------------------------------------------------------------------------------------------------
// End of File
