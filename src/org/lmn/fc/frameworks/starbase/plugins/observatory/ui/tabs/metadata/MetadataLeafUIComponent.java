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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.metadata;

import org.lmn.fc.common.utilities.ui.AlignedListCellRenderer;
import org.lmn.fc.frameworks.starbase.plugins.observatory.MetadataDictionary;
import org.lmn.fc.frameworks.starbase.plugins.observatory.MetadataItemState;
import org.lmn.fc.frameworks.starbase.plugins.observatory.events.MetadataChangedEvent;
import org.lmn.fc.frameworks.starbase.plugins.observatory.events.MetadataChangedListener;
import org.lmn.fc.frameworks.starbase.plugins.observatory.events.ObservatoryMetadataChangedEvent;
import org.lmn.fc.frameworks.starbase.plugins.observatory.events.ObserverMetadataChangedEvent;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.InstrumentState;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.InstrumentHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.MetadataHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.ChartUIComponentPlugin;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.MetadataLeafUIComponentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.logs.SimpleEventLogUIComponent;
import org.lmn.fc.model.datatypes.DataTypeDictionary;
import org.lmn.fc.model.datatypes.DataTypeHelper;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.registry.RegistryModelUtilities;
import org.lmn.fc.model.units.UnitsDictionary;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;
import org.lmn.fc.model.xmlbeans.metadata.SchemaUnits;
import org.lmn.fc.ui.UIComponentPlugin;
import org.lmn.fc.ui.UIComponentState;
import org.lmn.fc.ui.choosers.ChooserHelper;
import org.lmn.fc.ui.choosers.ChooserInterface;
import org.lmn.fc.ui.components.EditorUIComponent;
import org.lmn.fc.ui.components.EditorUtilities;
import org.lmn.fc.ui.components.UIComponent;
import org.lmn.fc.ui.layout.BoxLayoutFixed;
import org.lmn.fc.ui.panels.HTMLPanel;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;


/***************************************************************************************************
 * MetadataLeafUIComponent.
 *
 * Metadata consists of
 *
 *      Key
 *      Value
 *      Regex
 *      DataType
 *      Units
 *      Description
 */

public final class MetadataLeafUIComponent extends EditorUIComponent
                                           implements MetadataLeafUIComponentInterface
    {
    private static final long serialVersionUID = 6233781555085829452L;

    // Injections
    private final ObservatoryInstrumentInterface hostInstrument;

    private JPanel panelEditor;
    private JPanel panelLabels;
    private JPanel panelData;
    private JPanel panelButtons;

    private JLabel labelKey;
    private JLabel labelValue;
    private JLabel labelRegex;
    private JLabel labelDataType;
    private JLabel labelUnits;
    private JLabel labelDescription;

    private JLabel labelEditable;

    private JTextField textKey;
    private JTextField textValue;
    private JTextArea textRegex;
    private JComboBox comboDataType;
    private JComboBox comboUnits;
    private JTextArea textDescription;
    private JButton buttonChooser;

    private JCheckBox checkEditable;

    private JButton buttonRevert;
    private JButton buttonCommit;

    private Metadata metadataEdited;
    private Metadata metadataSaved;

    // Events
    private final Vector<MetadataChangedListener> vecMetadataChangedListeners;


    /***********************************************************************************************
     * MetadataLeafUIComponent.
     *
     * @param hostinstrument
     * @param informer
     */

    public MetadataLeafUIComponent(final ObservatoryInstrumentInterface hostinstrument,
                                   final boolean informer)
        {
        // Create the EditorUtilities for <Framework>.Editor.Metadata.
        // Note that the Colours and Fonts are common to all editors, and so use a different Key
        // Come in with a BorderLayout
        super(REGISTRY.getFrameworkResourceKey() + RESOURCE_KEY);

        // Injections
        this.hostInstrument = hostinstrument;

        this.metadataEdited = null;
        this.metadataSaved = null;

        this.vecMetadataChangedListeners = new Vector<MetadataChangedListener>(10);

        // See if we need to inform listeners of MetadataChanged events
        // Listen to the Observatory and Observer Metadata
        if ((informer)
            && (hostinstrument != null)
            && (hostinstrument.getContext() != null)
            && (hostinstrument.getContext().getObservatory() != null))
            {
            hostinstrument.getContext().getObservatory().addObservatoryMetadataChangedListener(this);
            hostinstrument.getContext().getObservatory().addObserverMetadataChangedListener(this);

//            System.out.println("MetadataExpanderUIComponent: ObservatoryMetadata Listeners Count=" + hostinstrument.getContext().getObservatory().getObservatoryMetadataChangedListeners().size());
//            System.out.println("MetadataExpanderUIComponent: ObserverMetadata Listeners Count=" + hostinstrument.getContext().getObservatory().getObserverMetadataChangedListeners().size());
            }
        }


    /***********************************************************************************************
     * Initialise this UIComponent.
     */

    public void initialiseUI()
        {
        super.initialiseUI();

        // Read colours etc.
        readResources();

        // Attempt to lay out the Editor on the EditorUIComponent UIComponent
        createEditorPanel();
        }


    /***********************************************************************************************
     * Run this UIComponent.
     */

    public void runUI()
        {
        super.runUI();
        }


    /***********************************************************************************************
     * Stop this UIComponent.
     */

    public void stopUI()
        {
        super.stopUI();
        }


    /***********************************************************************************************
     * Dispose of all components of this UIComponent.
     */

    public void disposeUI()
        {
        super.disposeUI();

        // Stop listening to MetadataChanged events
        if ((getHostInstrument() != null)
            && (getHostInstrument().getContext() != null)
            && (getHostInstrument().getContext().getObservatory() != null))
            {
            getHostInstrument().getContext().getObservatory().removeObservatoryMetadataChangedListener(this);
            getHostInstrument().getContext().getObservatory().removeObserverMetadataChangedListener(this);

//            System.out.println("LEAF  UI: ObservatoryMetadata Listeners Count=" + getHostInstrument().getContext().getObservatory().getObservatoryMetadataChangedListeners().size());
//            System.out.println("LEAF  UI: ObserverMetadata Listeners Count=" + getHostInstrument().getContext().getObservatory().getObserverMetadataChangedListeners().size());
            }
        }


    /***********************************************************************************************
     * Get the Metadata which the Editor is currently using.
     *
     * @return Metadata
     */

    private Metadata getEditedMetadata()
        {
        return (this.metadataEdited);
        }


    /***********************************************************************************************
     * Set the Metadata for this Editor.
     * Save a reference to the original Metadata for any reverts, edit only a copy.
     *
     * @param metadataitem
     */

    public void setEditedMetadata(final Metadata metadataitem)
        {
        //LOGGER.log("MetadataLeafUIComponent.setEditedMetadata()");

        // Protect the User from unwanted changes
        saveOriginalMetadata(metadataitem);

        this.metadataEdited = (Metadata)metadataitem.copy();

        // Show and Edit the copy
        showMetadataInEditor(getEditedMetadata());

        buttonCommit.setEnabled(false);
        buttonRevert.setEnabled(false);
        }


    /***********************************************************************************************
     * Get the saved original Metadata.
     *
     * @return Metadata
     */

    private Metadata getOriginalMetadata()
        {
        return (this.metadataSaved);
        }


    /***********************************************************************************************
     * Save a reference to the *original* Metadata for later Revert.
     *
     * @param metadataitem
     */

    private void saveOriginalMetadata(final Metadata metadataitem)
        {
        this.metadataSaved = metadataitem;
        }


    /***********************************************************************************************
     * Show the specified Metadata in the Editor.
     *
     * @param metadataitem
     */

    private void showMetadataInEditor(final Metadata metadataitem)
        {
        if (metadataitem != null)
            {
            // Redraw the UI if possible
            textKey.setText(metadataitem.getKey());
            textValue.setText(metadataitem.getValue());
            textRegex.setText(metadataitem.getRegex());
            comboDataType.setSelectedItem(metadataitem.getDataTypeName().toString());
            comboUnits.setSelectedItem(metadataitem.getUnits().toString());
            textDescription.setText(metadataitem.getDescription());

            // Set up the Chooser button appropriately for the DataType of the new Metadata
            if (buttonChooser != null)
                {
                final DataTypeDictionary dataType;

                // Is there a Chooser available?
                dataType = DataTypeDictionary.getDataTypeDictionaryEntryForName(metadataitem.getDataTypeName().toString());
                buttonChooser.setEnabled((dataType.getChooserClassname() != null)
                                          && (!EMPTY_STRING.equals(dataType.getChooserClassname().trim())));
                }
            }
        else
            {
            // We can't do much...
            textKey.setText(MSG_NODATA);
            textValue.setText(MSG_NODATA);
            textRegex.setText(MSG_NODATA);
            comboDataType.setSelectedItem(null);
            comboUnits.setSelectedItem(null);
            textDescription.setText(MSG_NODATA);

            // Set up the Chooser button appropriately
            if (buttonChooser != null)
                {
                buttonChooser.setEnabled(false);
                }
            }
        }


    /***********************************************************************************************
     * Create the Editor panel.
     */

    private void createEditorPanel()
        {
        final String SOURCE = "MetadataLeafUIComponent.createEditorPanel() ";
        final MetadataLeafUIComponentInterface thisLeaf;
        final int intLabelPanelHeight;
        final String strKey;
        final String strValue;
        final String strRegex;
        final String strDescription;
        final DataTypeDictionary[] arrayDataTypes;
        final List<String> listDataTypeNames;
        final UIComponent uiHtmlHelp;
        final JPanel panelValueWrapper;
        final JScrollPane editorScrollPane;
        final JPanel panelMainEditor;
        final JSplitPane splitPane;
        final boolean boolDebug;

        boolDebug = LOADER_PROPERTIES.isMetadataDebug()
                    || LOADER_PROPERTIES.isChartDebug();

        // For use in inner classes
        thisLeaf = this;

        buttonChooser = new JButton(BUTTON_CHOOSER);

        labelKey = createLabel(getTextColour(),
                               getLabelFont(),
                               KEY_LABEL_KEY);

        labelEditable = createLabel(getTextColour(),
                                    getLabelFont(),
                                    KEY_LABEL_EDITABLE);

        labelValue = createLabel(getTextColour(),
                                 getLabelFont(),
                                 KEY_LABEL_VALUE);

        labelRegex = createLabel(getTextColour(),
                                 getLabelFont(),
                                 KEY_LABEL_REGEX);

        labelDataType = createLabel(getTextColour(),
                                    getLabelFont(),
                                    KEY_LABEL_DATA_TYPE);

        labelUnits = createLabel(getTextColour(),
                                 getLabelFont(),
                                 KEY_LABEL_UNITS);

        labelDescription = createLabel(getTextColour(),
                                       getLabelFont(),
                                       KEY_LABEL_DESCRIPTION);

        //------------------------------------------------------------------------------------------
        // Key

        if (getEditedMetadata() != null)
            {
            strKey = getEditedMetadata().getKey();
            }
        else
            {
            strKey = MSG_NODATA;
            }

        textKey = createTextField(getTextColour(),
                                  getDataFont(),
                                  strKey,
                                  KEY_TOOLTIP_KEY,
                                  false);

        //------------------------------------------------------------------------------------------
        // Editable

        checkEditable = createCheckBox(getCanvasColour(),
                                       KEY_TOOLTIP_EDITABLE,
                                       ENABLED,
                                       ENABLED);

        //------------------------------------------------------------------------------------------
        // Value

        if (getEditedMetadata() != null)
            {
            strValue = getEditedMetadata().getValue();
            }
        else
            {
            strValue = MSG_NODATA;
            }

        textValue = createTextField(getTextColour(),
                                    getDataFont(),
                                    strValue,
                                    KEY_TOOLTIP_VALUE,
                                    ENABLED);

        //------------------------------------------------------------------------------------------
        // Regex

        if (getEditedMetadata() != null)
            {
            strRegex = getEditedMetadata().getRegex();
            }
        else
            {
            strRegex = EMPTY_STRING;
            }

        textRegex = createTextArea(getTextColour(),
                                   getDataFont(),
                                   strRegex,
                                   KEY_TOOLTIP_REGEX,
                                   HEIGHT_REGEX,
                                   ENABLED);

        //------------------------------------------------------------------------------------------
        // DataType

        arrayDataTypes = DataTypeDictionary.values();
        listDataTypeNames = new ArrayList<String>(arrayDataTypes.length);

        // Only list the DataTypes which may be used in Metadata
        for (int intDataTypeIndex = 0;
             intDataTypeIndex < arrayDataTypes.length;
             intDataTypeIndex++)
            {
            if (arrayDataTypes[intDataTypeIndex].isMetadataType())
                {
                listDataTypeNames.add(arrayDataTypes[intDataTypeIndex].getName());
                }
            }

        // Sort the DataType names
        Collections.sort(listDataTypeNames);

        comboDataType = createComboBox(getTextColour(),
                                       getDataFont(),
                                       KEY_TOOLTIP_DATA_TYPE,
                                       ENABLED,
                                       null,
                                       null);
        comboDataType.setRenderer(new AlignedListCellRenderer(SwingConstants.LEFT,
                                                              getDataFont(),
                                                              getTextColour(),
                                                              null));
        comboDataType.removeAllItems();

        for (int i = 0;
             i < listDataTypeNames.size();
             i++)
            {
            comboDataType.addItem(listDataTypeNames.get(i));
            }

        if (getEditedMetadata() != null)
            {
            comboDataType.setSelectedItem(getEditedMetadata().getDataTypeName().toString());
            }
        else
            {
            comboDataType.setSelectedIndex(0);
            }

        //------------------------------------------------------------------------------------------
        // Units

        comboUnits = createComboBox(getTextColour(),
                                    getDataFont(),
                                    KEY_TOOLTIP_UNITS,
                                    ENABLED,
                                    null,
                                    null);
        comboUnits.setRenderer(new AlignedListCellRenderer(SwingConstants.LEFT,
                                                           getDataFont(),
                                                           getTextColour(),
                                                           null));
        comboUnits.removeAllItems();

        // Don't sort the Units, in order to maintain their natural grouping
        for (int intUnitsIndex = 1;
             intUnitsIndex < (SchemaUnits.Enum.table.lastInt() + 1);
             intUnitsIndex++)
            {
            final SchemaUnits.Enum unit;

            unit = SchemaUnits.Enum.forInt(intUnitsIndex);
            comboUnits.addItem(unit.toString());
            }

        if (getEditedMetadata() != null)
            {
            comboUnits.setSelectedItem(getEditedMetadata().getUnits().toString());
            }
        else
            {
            comboUnits.setSelectedIndex(0);
            }

        //------------------------------------------------------------------------------------------
        // Description

        if (getEditedMetadata() != null)
            {
            strDescription = getEditedMetadata().getDescription();
            }
        else
            {
            strDescription = MSG_NODATA;
            }

        textDescription = createTextArea(getTextColour(),
                                         getDataFont(),
                                         strDescription,
                                         KEY_TOOLTIP_DESCRIPTION,
                                         HEIGHT_DESCRIPTION,
                                         ENABLED);

        /******************************************************************************************/
        /* Listeners                                                                              */
        /******************************************************************************************/
        // Add the ActionListeners now we have stopped changing component states

        // Editable checkbox
        final ItemListener listenerEditable = new ItemListener()
            {
            public void itemStateChanged(final ItemEvent event)
                {
                dataChanged();

                if (event.getStateChange() == ItemEvent.SELECTED)
                    {
                    textValue.setEnabled(true);
                    comboDataType.setEnabled(true);
                    comboUnits.setEnabled(true);
                    textDescription.setEnabled(true);
                    }
                else
                    {
                    textValue.setEnabled(false);
                    comboDataType.setEnabled(false);
                    comboUnits.setEnabled(false);
                    textDescription.setEnabled(false);
                    }
                }
            };

        checkEditable.addItemListener(listenerEditable);

        //------------------------------------------------------------------------------------------
        // Value text box

        final KeyListener listenerValue = new KeyListener()
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

        textValue.addKeyListener(listenerValue);

        //------------------------------------------------------------------------------------------
        // Regex text box

        final KeyListener listenerRegex = new KeyListener()
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

        textRegex.addKeyListener(listenerRegex);

        //------------------------------------------------------------------------------------------
        // Now add a Value Chooser button appropriate to the DataType

        panelValueWrapper = new JPanel();
        panelValueWrapper.setBorder(BorderFactory.createEmptyBorder());
        panelValueWrapper.setLayout(new BoxLayoutFixed(panelValueWrapper, BoxLayoutFixed.X_AXIS));
        panelValueWrapper.setBackground(getCanvasColour().getColor());
        panelValueWrapper.setAlignmentX(0);

        panelValueWrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, HEIGHT_BUTTON));

        buttonChooser.setFont(getLabelFont().getFont());
        buttonChooser.setForeground(getTextColour().getColor());

        buttonChooser.setMinimumSize(new Dimension(WIDTH_CHOOSER_BUTTON, HEIGHT_BUTTON));
        buttonChooser.setMaximumSize(new Dimension(WIDTH_CHOOSER_BUTTON, HEIGHT_BUTTON));
        buttonChooser.setPreferredSize(new Dimension(WIDTH_CHOOSER_BUTTON, HEIGHT_BUTTON));

        buttonChooser.setToolTipText(MSG_CHOOSE_VALUE);

        buttonChooser.addActionListener(new ActionListener()
        {
        public synchronized void actionPerformed(final ActionEvent event)
            {
            final DataTypeDictionary dataType;

            // Instantiate and show the appropriate Chooser for the currently selected DataType
            dataType = DataTypeDictionary.getDataTypeDictionaryEntryForName((String) comboDataType.getSelectedItem());

            // Is there a Chooser still available?
            if ((dataType.getChooserClassname() != null)
                && (!EMPTY_STRING.equals(dataType.getChooserClassname().trim())))
                {
                final ChooserInterface chooser;
                final String strInitialValue;

                if ((textValue.getText() == null)
                    || (EMPTY_STRING.equals(textValue.getText().trim())))
                    {
                    strInitialValue = SchemaUnits.DIMENSIONLESS.toString();
                    }
                else
                    {
                    strInitialValue = textValue.getText().trim();
                    }

                // Instantiate and show the appropriate Chooser with the current Value
                chooser = ChooserHelper.instantiateChooser(dataType.getChooserClassname(),
                                                           getHostInstrument(),
                                                           getHostInstrument().getFontData(),
                                                           getHostInstrument().getColourData(),
                                                           strInitialValue);
                if (chooser != null)
                    {
                    // Tell the Chooser the current value of the Metadata
                    // Go modal...
                    chooser.showChooser((Component) getHostInstrument().getInstrumentPanel());

                    textValue.setText(chooser.getValue());
                    dataChanged();
                    }
                else
                    {
                    LOGGER.error("MetadataLeafUIComponent.createEditorPanel() can't make chooser [classname=" + dataType.getChooserClassname() + "]");
                    }
                }
            else
                {
                Toolkit.getDefaultToolkit().beep();
                }
            }
        });

        panelValueWrapper.add(textValue);
        panelValueWrapper.add(Box.createHorizontalStrut(5));
        panelValueWrapper.add(buttonChooser);

        //------------------------------------------------------------------------------------------
        // DataType drop-down

        final ActionListener typeListener = new ActionListener()
            {
            public void actionPerformed(final ActionEvent event)
                {
                dataChanged();

                // Warn the user that changing the data type could be a problem...
                if (!comboDataType.getSelectedItem().equals(getOriginalMetadata().getDataTypeName().toString()))
                    {
                    JOptionPane.showMessageDialog(null,
                                                  REGISTRY.getString(getResourceKey() + KEY_WARNING_DATATYPE),
                                                  REGISTRY.getString(getResourceKey() + KEY_TITLE),
                                                  JOptionPane.WARNING_MESSAGE);

                    // Set up the Chooser button appropriately
                    if (buttonChooser != null)
                        {
                        final DataTypeDictionary dataType;

                        // Is there a Chooser available for this new selection?
                        dataType = DataTypeDictionary.getDataTypeDictionaryEntryForName((String)comboDataType.getSelectedItem());
                        buttonChooser.setEnabled((dataType.getChooserClassname() != null)
                                                  && (!EMPTY_STRING.equals(dataType.getChooserClassname().trim())));
                        }
                    }
                }
            };

        comboDataType.addActionListener(typeListener);

        //------------------------------------------------------------------------------------------
        // Units drop-down

        final ActionListener unitsListener = new ActionListener()
            {
            public void actionPerformed(final ActionEvent event)
                {
                dataChanged();

                // Warn the user that changing the Units could be a problem...
                if (!comboUnits.getSelectedItem().equals(getOriginalMetadata().getUnits().toString()))
                    {
                    JOptionPane.showMessageDialog(null,
                                                  REGISTRY.getString(getResourceKey() + KEY_WARNING_UNITS),
                                                  REGISTRY.getString(getResourceKey() + KEY_TITLE),
                                                  JOptionPane.WARNING_MESSAGE);
                    }
                }
            };

        comboUnits.addActionListener(unitsListener);

        //------------------------------------------------------------------------------------------
        // Description text box

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
                                    KEY_LABEL_COMMIT,
                                    KEY_TOOLTIP_COMMIT,
                                    "buttonCommit",
                                    false);

        final ActionListener commitListener = new ActionListener()
            {
            public void actionPerformed(final ActionEvent event)
                {
                final StringBuffer bufferValue;
                String strDescription;
                boolean boolPatternValid;
                final List<String> errors;

                errors = new ArrayList<String>(10);

                //----------------------------------------------------------------------------------
                // Check that the Description is not too large
                // Issue a truncation warning if so

                strDescription = textDescription.getText();
                strDescription = strDescription.trim();

                if (strDescription.length() > LENGTH_DESCRIPTION)
                    {
                    strDescription = strDescription.substring(0, LENGTH_DESCRIPTION-1);

                    JOptionPane.showMessageDialog(null,
                                                  REGISTRY.getString(getResourceKey() + KEY_WARNING_TRUNCATED_DESCRIPTION) + SPACE + LENGTH_DESCRIPTION + " characters",
                                                  REGISTRY.getString(getResourceKey() + KEY_TITLE),
                                                  JOptionPane.WARNING_MESSAGE);
                    }

                //----------------------------------------------------------------------------------
                // Validate the Regex as it now is

                try
                    {
                    Pattern.compile(textRegex.getText());
                    boolPatternValid = true;
                    }

                catch (final PatternSyntaxException exception)
                    {
                    boolPatternValid = false;
                    JOptionPane.showMessageDialog(null,
                                                  REGISTRY.getString(getResourceKey() + KEY_WARNING_REGEX),
                                                  REGISTRY.getString(getResourceKey() + KEY_TITLE),
                                                  JOptionPane.WARNING_MESSAGE);
                    }


                //----------------------------------------------------------------------------------
                // Try to parse the Value into the currently selected DataType

                bufferValue = new StringBuffer(textValue.getText());

                // Ensure Value is less than 1000 characters when saving
                if (bufferValue.length() > LENGTH_DATATYPE_VALUE)
                    {
                    bufferValue.setLength(LENGTH_DATATYPE_VALUE);
                    LOGGER.error("Metadata Value has been truncated to " + LENGTH_DATATYPE_VALUE);
                    }

                // If Regex is supplied, then this takes precedence over any Regex in the DataType
                // The Instrument must not be active, otherwise Metadata could change during the commit
                if ((boolPatternValid)
                   && (DataTypeHelper.validateDataTypeOfMetadataValue(bufferValue.toString(),
                                                                      DataTypeDictionary.getDataTypeDictionaryEntryForName((String) comboDataType.getSelectedItem()),
                                                                      textRegex.getText().trim(),
                                                                      errors) == 0)
                   && (InstrumentState.isReady(getHostInstrument())))
                    {
                    // Set all changed values on the *original* Metadata
                    // The Value may have been trimmed or reformatted in some way
                    getOriginalMetadata().setValue(bufferValue.toString());

                    // We know this Regex must be a valid pattern
                    getOriginalMetadata().setRegex(textRegex.getText().trim());

                    // We need a SchemaDataType.Enum, given the DataType name as a String
                    getOriginalMetadata().setDataTypeName(DataTypeDictionary.getDataTypeDictionaryEntryForName((String) comboDataType.getSelectedItem()).getSchemaDataType());

                    // We need a SchemaUnits.Enum, given the Units name as a String
                    getOriginalMetadata().setUnits(UnitsDictionary.getSchemaUnitsForName((String) comboUnits.getSelectedItem()));

                    // The Description may have been truncated
                    getOriginalMetadata().setDescription(strDescription);

                    // The Original and the Edited are now the same, so update the display
                    // Do not update the drop-downs, because they don't need to be redrawn,
                    // and the change would produce an event we don't want
                    textValue.setText(getOriginalMetadata().getValue());
                    textRegex.setText(getOriginalMetadata().getRegex());
                    textDescription.setText(getOriginalMetadata().getDescription());

//                    MetadataHelper.showMetadata(getOriginalMetadata(), "Original before swap", true);
//                    MetadataHelper.showMetadata(getEditedMetadata(), "Edited before swap", true);

                    // Prepare for more edits with a new copy of the edited Original
                    setEditedMetadata(getOriginalMetadata());

                    // Prevent further user interaction
                    buttonCommit.setEnabled(false);
                    buttonRevert.setEnabled(false);

                    MetadataHelper.showMetadata(getOriginalMetadata(),
                                                SOURCE + "COMMIT: Original Metadata after swap",
                                                boolDebug);
                    MetadataHelper.showMetadata(getEditedMetadata(),
                                                SOURCE + "COMMIT: Edited Metadata after swap",
                                                boolDebug);

                    notifyMetadataChangedEvent(thisLeaf,
                                               getOriginalMetadata().getKey(),
                                               getOriginalMetadata().getValue(),
                                               MetadataItemState.EDIT);

                    // It is clearly possible to put Framework metadata under another root,
                    // but then the User is on their own...
                    if (getOriginalMetadata().getKey().startsWith(MetadataDictionary.KEY_FRAMEWORK_ROOT.getKey()))
                        {
                        // The Framework has changed in some way
                        REGISTRY.getFramework().notifyFrameworkChangedEvent(thisLeaf);
                        }

                    if ((getOriginalMetadata().getKey().startsWith(MetadataDictionary.KEY_OBSERVATORY_ROOT.getKey()))
                        && (getHostInstrument().getContext().getObservatory() != null))
                        {
                        getHostInstrument().getContext().getObservatory().notifyObservatoryMetadataChangedEvent(thisLeaf,
                                                                                                                getOriginalMetadata().getKey(),
                                                                                                                MetadataItemState.EDIT);
                        }
                    else if ((getOriginalMetadata().getKey().startsWith(MetadataDictionary.KEY_OBSERVER_ROOT.getKey()))
                        && (getHostInstrument().getContext().getObservatory() != null))
                        {
                        getHostInstrument().getContext().getObservatory().notifyObserverMetadataChangedEvent(thisLeaf,
                                                                                                             getOriginalMetadata().getKey(),
                                                                                                             MetadataItemState.EDIT);
                        }

                    // Something has changed, we may need to update indicators etc.
                    InstrumentHelper.notifyInstrumentChanged(getHostInstrument());

                    // Log the change if we have an InstrumentPanel and a DAO
                    if ((getHostInstrument().getInstrumentPanel() != null)
                        && (getHostInstrument().getDAO() != null)
                        && (getHostInstrument().getDAO().getWrappedData() != null))
                        {
                        final StringBuffer buffer;

                        buffer = new StringBuffer();
                        buffer.append(METADATA_TARGET);
                        buffer.append(getHostInstrument().getInstrument().getIdentifier());
                        buffer.append(TERMINATOR);
                        buffer.append(METADATA_ACTION_EDIT_METADATA);
                        buffer.append("[key=");
                        buffer.append(getOriginalMetadata().getKey());
                        buffer.append("] ");
                        buffer.append("[value=");
                        buffer.append(getOriginalMetadata().getValue());
                        buffer.append("] ");
                        buffer.append("[datatype=");
                        buffer.append(getOriginalMetadata().getDataTypeName().toString());
                        buffer.append("] ");
                        buffer.append("[units=");
                        buffer.append(getOriginalMetadata().getUnits().toString());
                        buffer.append("] ");
                        buffer.append("[description=");
                        buffer.append(getOriginalMetadata().getDescription());
                        buffer.append("] ");

                        SimpleEventLogUIComponent.logEvent(getHostInstrument().getDAO().getEventLogFragment(),
                                                           EventStatus.INFO,
                                                           buffer.toString(),
                                                           getHostInstrument().getDAO().getLocalHostname(),
                                                           getHostInstrument().getDAO().getObservatoryClock());

                        // Force the log to update, since we are not working directly via a DAO
                        // This will repeat notifyInstrumentChanged()
                        getHostInstrument().getInstrumentPanel().flushLogFragments(getHostInstrument().getDAO().getWrappedData());

                        // Re-apply the PrimaryDataset to the Chart, to cause a read of the Metadata we have changed
                        if (getHostInstrument().getInstrumentPanel().getChartTab() != null)
                            {
                            final UIComponentPlugin chart;

                            chart = getHostInstrument().getInstrumentPanel().getChartTab();

                            if (chart instanceof ChartUIComponentPlugin)
                                {
                                ((ChartUIComponentPlugin) chart).setPrimaryXYDataset(getHostInstrument().getDAO(),
                                                                                     ((ChartUIComponentPlugin) chart).getPrimaryXYDataset());
                                }
                            }
                        }
                    else
                        {
                        LOGGER.error("MetadataLeafUIComponent.createEditorPanel() Unable to log editing change");
                        }
                    }
                else
                    {
                    if (!InstrumentState.isReady(getHostInstrument()))
                        {
                        JOptionPane.showMessageDialog(null,
                                                      "The Instrument must be swiched on and Ready in order to edit Metadata",
                                                      REGISTRY.getString(getResourceKey() + KEY_TITLE),
                                                      JOptionPane.ERROR_MESSAGE);
                        }
                    else
                        {
                        // Could not instantiate the DataType, so ask the user to try again
                        // Do not make any changes to the stored data
                        JOptionPane.showMessageDialog(null,
                                                      REGISTRY.getString(getResourceKey() + KEY_INVALID_VALUE),
                                                      REGISTRY.getString(getResourceKey() + KEY_TITLE),
                                                      JOptionPane.ERROR_MESSAGE);
                        }

                    LOGGER.errors(SOURCE, errors);
                    }

//                MetadataHelper.showMetadata(getOriginalMetadata(), "Original", true);
//                MetadataHelper.showMetadata(getEditedMetadata(), "Edited", true);
                }
            };

        buttonCommit.addActionListener(commitListener);
        buttonCommit.setEnabled(false);

        //------------------------------------------------------------------------------------------
        // The Revert button and its listener

        buttonRevert = createButton(getTextColour(),
                                    getLabelFont(),
                                    KEY_LABEL_REVERT,
                                    KEY_TOOLTIP_REVERT,
                                    "buttonRevert",
                                    false);

        final ActionListener revertListener = new ActionListener()
            {
            public void actionPerformed(final ActionEvent event)
                {
                // Undo any edits
                // These changes will enable the buttons!
                // This makes a new copy each time
                setEditedMetadata(getOriginalMetadata());

                // Prevent further user interaction
                buttonCommit.setEnabled(false);
                buttonRevert.setEnabled(false);
                }
            };

        buttonRevert.addActionListener(revertListener);
        buttonRevert.setEnabled(false);

        //------------------------------------------------------------------------------------------
        // These all have BoxLayout

        intLabelPanelHeight = (EditorUtilities.HEIGHT_ROW * ROW_COUNT)
                              + HEIGHT_REGEX
                              + HEIGHT_DESCRIPTION
                              + (int)(DIM_ROW_SPACER.getHeight() * (ROW_COUNT));

        panelEditor = EditorUtilities.createEditorPanel(getCanvasColour());
        panelLabels = EditorUtilities.createLabelPanel(getCanvasColour(),
                                                       intLabelPanelHeight);
        panelData = EditorUtilities.createDataPanel(getCanvasColour());
        panelButtons = EditorUtilities.createButtonPanel(getCanvasColour(),
                                                         buttonRevert,
                                                         buttonCommit);
        // Labels
        panelLabels.add(labelKey);
        panelLabels.add(Box.createRigidArea(DIM_ROW_SPACER));
//        panelLabels.add(labelEditable);
//        panelLabels.add(Box.createRigidArea(DIM_ROW_SPACER));
        panelLabels.add(labelValue);
        panelLabels.add(Box.createRigidArea(DIM_ROW_SPACER));
        panelLabels.add(labelRegex);
        panelLabels.add(Box.createRigidArea(DIM_REGEX_SPACER));
        panelLabels.add(labelDataType);
        panelLabels.add(Box.createRigidArea(DIM_ROW_SPACER));
        panelLabels.add(labelUnits);
        panelLabels.add(Box.createRigidArea(DIM_ROW_SPACER));
        panelLabels.add(labelDescription);

        // Data
        panelData.add(textKey);
        panelData.add(Box.createRigidArea(DIM_ROW_SPACER));
//        panelData.add(checkEditable);
//        panelData.add(Box.createRigidArea(DIM_ROW_SPACER));
        panelData.add(panelValueWrapper);
        panelData.add(Box.createRigidArea(DIM_ROW_SPACER));
        panelData.add(textRegex);
        panelData.add(Box.createRigidArea(DIM_ROW_SPACER));
        panelData.add(comboDataType);
        panelData.add(Box.createRigidArea(DIM_ROW_SPACER));
        panelData.add(comboUnits);
        panelData.add(Box.createRigidArea(DIM_ROW_SPACER));
        panelData.add(textDescription);

        panelEditor.add(panelLabels, null);
        panelEditor.add(panelData, null);
        panelEditor.setAlignmentX(Component.CENTER_ALIGNMENT);

        panelButtons.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Create an HTML window for the help text
        uiHtmlHelp = new HTMLPanel(RegistryModelUtilities.getHelpURL(getHostInstrument().getHostAtom(),
                                                                     METADATA_EDITOR_HELP),
                                   true,
                                   DEFAULT_COLOUR_HELP_BACKGROUND.getColor());
        uiHtmlHelp.setAlignmentX(Component.CENTER_ALIGNMENT);

        // It's ok to initialise here, there's nothing in HTMLPanel.runUI()
        uiHtmlHelp.initialiseUI();

        //------------------------------------------------------------------------------------------
        // Put all the panels together in the right order

        panelMainEditor = new JPanel();
        panelMainEditor.setLayout(new BoxLayoutFixed(panelMainEditor, BoxLayoutFixed.Y_AXIS));
        panelMainEditor.setBackground(getCanvasColour().getColor());
        panelMainEditor.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelMainEditor.setBorder(BorderFactory.createEmptyBorder(EditorUtilities.WIDTH_EMPTY_BORDER,
                                                                  EditorUtilities.WIDTH_EMPTY_BORDER,
                                                                  EditorUtilities.WIDTH_EMPTY_BORDER,
                                                                  EditorUtilities.WIDTH_EMPTY_BORDER));

        panelMainEditor.add(panelEditor);
        panelMainEditor.add(Box.createVerticalStrut(20));
        panelMainEditor.add(panelButtons);
        panelMainEditor.add(Box.createVerticalStrut(10));
        panelMainEditor.add(Box.createVerticalGlue());

        editorScrollPane = new JScrollPane(panelMainEditor);
        editorScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        editorScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        editorScrollPane.setMinimumSize(new Dimension(EditorUtilities.DIM_MAGIC, EditorUtilities.DIM_MAGIC));

        // Assemble the underlying EditorUIComponent
        removeAll();

        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

        splitPane.setBorder(BorderFactory.createEmptyBorder());
        splitPane.setOneTouchExpandable(true);
        splitPane.setContinuousLayout(false);
        splitPane.setDividerSize(DIVIDER_SIZE);

        // Setting the location explicitly seems to be the best I can do to reveal the panel at startup...
        splitPane.setDividerLocation(DIVIDER_LOCATION);
        splitPane.setResizeWeight(RESIZE_WEIGHT);

        setMinimumSize(new Dimension(EditorUtilities.DIM_MAGIC, EditorUtilities.DIM_MAGIC));
        setBackground(getCanvasColour().getColor());

        splitPane.setTopComponent(editorScrollPane);
        splitPane.setBottomComponent(uiHtmlHelp);

        // The host UIComponent uses BorderLayout
        add(splitPane, BorderLayout.CENTER);
        }


    /***********************************************************************************************
     * A utility to indicate when the data has changed as a result of an edit.
     */

    private void dataChanged()
        {
        buttonCommit.setEnabled(true);
        buttonRevert.setEnabled(true);
        }


    /***********************************************************************************************
     * Get the host Observatory Instrument.
     *
     * @return ObservatoryInstrumentInterface
     */

    private ObservatoryInstrumentInterface getHostInstrument()
        {
        return (this.hostInstrument);
        }


    /******************************************************************************************/
    /* Events                                                                                 */
    /***********************************************************************************************
     * Notify all listeners of MetadataChangedEvents.
     *
     * @param eventsource
     * @param metadatakey
     * @param metadatavalue
     * @param state
     */

    public final void notifyMetadataChangedEvent(final Object eventsource,
                                                 final String metadatakey,
                                                 final String metadatavalue,
                                                 final MetadataItemState state)
        {
        final String SOURCE = "MetadataLeafUIComponent.notifyMetadataChangedEvent() ";
        List<MetadataChangedListener> listeners;
        final MetadataChangedEvent changeEvent;

        // Create a Thread-safe List of Listeners
        listeners = new CopyOnWriteArrayList<MetadataChangedListener>(getMetadataChangedListeners());

        // Create an MetadataChangedEvent
        changeEvent = new MetadataChangedEvent(eventsource, metadatakey, metadatavalue, state);

        // Fire the event to every listener
        synchronized(listeners)
            {
            for (int i = 0; i < listeners.size(); i++)
                {
                final MetadataChangedListener changeListener;

                changeListener = listeners.get(i);
                changeListener.metadataChanged(changeEvent);
                }
            }

        // Help the GC?
        listeners = null;
        }


    /***********************************************************************************************
     * Get the MetadataChanged Listeners (mostly for testing).
     *
     * @return Vector<MetadataChangedListener>
     */

    public final Vector<MetadataChangedListener> getMetadataChangedListeners()
        {
        return (this.vecMetadataChangedListeners);
        }


    /***********************************************************************************************
     * Add a listener for this event, uniquely.
     *
     * @param listener
     */

    public final void addMetadataChangedListener(final MetadataChangedListener listener)
        {
        final String SOURCE = "MetadataLeafUIComponent.addMetadataChangedListener() ";

        if ((listener != null)
            && (getMetadataChangedListeners() != null)
            && (!getMetadataChangedListeners().contains(listener)))
            {
            getMetadataChangedListeners().addElement(listener);
            LOGGER.debug(LOADER_PROPERTIES.isMetadataDebug(),
                         SOURCE + "[count=" + getMetadataChangedListeners().size()
                         + "] [class=" + listener.getClass().getName() + "]");
            }
        }


    /***********************************************************************************************
     * Remove a listener for this event.
     *
     * @param listener
     */

    public final void removeMetadataChangedListener(final MetadataChangedListener listener)
        {
        final String SOURCE = "MetadataLeafUIComponent.removeMetadataChangedListener() ";

        if ((listener != null)
            && (getMetadataChangedListeners() != null))
            {
            getMetadataChangedListeners().removeElement(listener);
            LOGGER.debug(LOADER_PROPERTIES.isMetadataDebug(),
                         SOURCE + "[count=" + getMetadataChangedListeners().size()
                         + "] [class=" + listener.getClass().getName() + "]");
            }
        }


    /*******************************************************************************************
     * Indicate that the ObservatoryMetadata has changed.
     * Only respond to Events which originated elsewhere, i.e. not in this LeafUI.
     *
     * @param event
     */

    public void observatoryChanged(final ObservatoryMetadataChangedEvent event)
        {
        final String SOURCE = "MetadataLeafUIComponent.observatoryChanged() ";

        // Only update if we are editing the same Observatory metadata item we've been told about,
        // or if all Observatory items have changed
        if ((event != null)
            && (!event.getSource().equals(this))
            && (UIComponentState.RUNNING.equals(getUIState()))
            && (getOriginalMetadata().getKey().startsWith(MetadataDictionary.KEY_OBSERVATORY_ROOT.getKey()))
            && ((getOriginalMetadata().getKey().equals(event.getMetadataKey()))
                || (EMPTY_STRING.equals(event.getMetadataKey()))))
            {
            LOGGER.debug(LOADER_PROPERTIES.isMetadataDebug(),
                         SOURCE + "[state=" + event.getItemState().getName()
                                + "] [key=" + event.getMetadataKey()
                                + "] [new_value=" + getOriginalMetadata().getValue()
                                + "] [instrument=" + getHostInstrument().getInstrument().getIdentifier()
                                + "] [source=" + event.getSource().getClass().getName()
                                + "] [this=" + this.getClass().getName() + "]");

            setEditedMetadata(getOriginalMetadata());
            }
        }


    /*******************************************************************************************
     * Indicate that the ObserverMetadata has changed.
     * Only respond to Events which originated elsewhere, i.e. not in this LeafUI.
     *
     * @param event
     */

    public void observerChanged(final ObserverMetadataChangedEvent event)
        {
        final String SOURCE = "MetadataLeafUIComponent.observerChanged() ";

        // Only update if we are editing the same Observer metadata item we've been told about,
        // or if all Observer items have changed
        if ((event != null)
            && (!event.getSource().equals(this))
            && (UIComponentState.RUNNING.equals(getUIState()))
            && (getOriginalMetadata().getKey().startsWith(MetadataDictionary.KEY_OBSERVER_ROOT.getKey()))
            && ((getOriginalMetadata().getKey().equals(event.getMetadataKey()))
                || (EMPTY_STRING.equals(event.getMetadataKey()))))
            {
            LOGGER.debug(LOADER_PROPERTIES.isMetadataDebug(),
                         SOURCE + "[state=" + event.getItemState().getName()
                                + "] [key=" + event.getMetadataKey()
                                + "] [new_value=" + getOriginalMetadata().getValue()
                                + "] [instrument=" + getHostInstrument().getInstrument().getIdentifier()
                                + "] [source=" + event.getSource().getClass().getName()
                                + "] [this=" + this.getClass().getName() + "]");

            setEditedMetadata(getOriginalMetadata());
            }
        }
    }
