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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.commands;


import org.lmn.fc.common.constants.*;
import org.lmn.fc.common.utilities.ui.AlignedListCellRenderer;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.InstrumentState;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.InstrumentHelper;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandProcessorContextInterface;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.DataTypeDictionary;
import org.lmn.fc.model.datatypes.DataTypeHelper;
import org.lmn.fc.model.xmlbeans.datatypes.SchemaDataType;
import org.lmn.fc.model.xmlbeans.instruments.ParameterType;
import org.lmn.fc.ui.UIComponentPlugin;
import org.lmn.fc.ui.choosers.ChooserHelper;
import org.lmn.fc.ui.choosers.ChooserInterface;
import org.lmn.fc.ui.components.SpringUtilities;
import org.lmn.fc.ui.layout.BoxLayoutFixed;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/***************************************************************************************************
 * ParameterHelper.
 */

public final class ParameterHelper implements FrameworkConstants,
                                              FrameworkStrings,
                                              FrameworkMetadata,
                                              FrameworkSingletons,
                                              ResourceKeys
    {
    // String Resources
    public static final String TITLE_PARAMETERS = "Parameters";
    public static final String TOOLTIP_PARAMETER_NAME = "The name of the Parameter";
    public static final String TOOLTIP_PARAMETER_UNITS = "The Units of the Parameter";
    public static final String TOOLTIP_PARAMETER_DATA_TYPE = "The Data Type of the Parameter";
    public static final String TOOLTIP_SELECT_PARAMETER = "Select a value for the Parameter";
    public static final String TOOLTIP_PARAMETER_HELP = "Obtain help for this parameter";
    public static final String MSG_CHOOSE_PARAMETER = "Choose the parameter value";
    public static final String BUTTON_HELP = "Help";

    public static final int PARAMETER_COLUMNS = 4;
    public static final int PARAMETER_PADDING = 6;

    public static final int WIDTH_HELP_BUTTON = CommandProcessorUtilities.WIDTH_CHOOSER_BUTTON;

    private static final Color COLOR_PARAMETER_VALID_BG = Color.white;
    private static final Color COLOR_PARAMETER_INVALID_FG = Color.red.brighter();
    private static final Color COLOR_PARAMETER_INVALID_BG = new Color(255, 255, 153);


    /***********************************************************************************************
     * Create the ParametersPanel in a JScrollPane, and place on a JPanel.
     *
     * @param context The CommandProcessorContext
     * @param drawborder
     *
     * @return JPanel
     */

    public static JPanel createParametersPanel(final CommandProcessorContextInterface context,
                                               final boolean drawborder)
        {
        final String SOURCE = "ParameterHelper.createParametersPanel() ";
        final JPanel panelParameters;
        final JScrollPane scrollParameters;

        panelParameters = new JPanel();
        panelParameters.setLayout(new BorderLayout());

        if (drawborder)
            {
            final TitledBorder titledBorder;
            final Border border;

            titledBorder = BorderFactory.createTitledBorder(TITLE_PARAMETERS);
            titledBorder.setTitleFont(context.getFontData().getFont());
            titledBorder.setTitleColor(context.getColourData().getColor());
            border = BorderFactory.createCompoundBorder(titledBorder,
                                                        BorderFactory.createEmptyBorder(4, 4, 4, 4));
            panelParameters.setBorder(border);
            }
        else
            {
            panelParameters.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
            }

        panelParameters.setBackground(UIComponentPlugin.DEFAULT_COLOUR_PANEL.getColor());
        panelParameters.setMinimumSize(new Dimension(100, 100));
        panelParameters.setPreferredSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        // This will expand Parameters if appropriate
        rebuildParametersPanel(context);
        StarscriptHelper.updateStarscript(context);
        InstrumentHelper.notifyInstrumentChanged(context.getObservatoryInstrument());

        // Present the rebuilt Parameters panel on a scroll pane
        scrollParameters = new JScrollPane(context.getParameterPanel(),
                                           JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                           JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollParameters.setBorder(BorderFactory.createEmptyBorder());
        scrollParameters.setMinimumSize(new Dimension(100, 100));
        scrollParameters.setPreferredSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        // The setDividerLocation(double) method has no effect if the split pane has no size
        // (typically true if it isn't onscreen yet). You can either use setDividerLocation(int)
        // or specify the preferred sizes of the split pane's contained components
        // and the split pane's resize weight instead.

        panelParameters.add(scrollParameters, BorderLayout.CENTER);

        return (panelParameters);
        }


    /***********************************************************************************************
     * Rebuild the Parameters panel to show text fields or combo boxes for each Parameter required.
     *
     * @param context The CommandProcessorContext
     */

    public static void rebuildParametersPanel(final CommandProcessorContextInterface context)
        {
        final String SOURCE = "ParameterHelper.rebuildParametersPanel() ";

        context.getParameterPanel().removeAll();
        context.getParameterPanel().setLayout(new SpringLayout());
        context.getParameterPanel().setBackground(UIComponentPlugin.DEFAULT_COLOUR_PANEL.getColor());

        // No Parameters used for execution yet
        context.getExecutionParameters().clear();

        if (context.isSelectedMacroOrCommand())
            {
            // Iterate over the Parameters as specified by the Instrument XML, i.e. not yet expanded
            if ((context.getCommandParameters() != null)
                && (!context.getCommandParameters().isEmpty()))
                {
                final Iterator<ParameterType> iterCommandParameters;
                int intRowCount;

                iterCommandParameters = context.getCommandParameters().iterator();
                intRowCount = 0;

                while (iterCommandParameters.hasNext())
                    {
                    final ParameterType parameterCommand;

                    parameterCommand = iterCommandParameters.next();

                    if (parameterCommand != null)
                        {
                        final List<ParameterType> listSubParameters;

                        listSubParameters = buildParameter(context, parameterCommand);

//                        System.out.println("ADDING MAIN PARAM TO EXECUTED " + parameterCommand.getName()
//                                           + " SUB PARAM INDEX=" + parameterCommand.getSubParameterIndex()
//                                           + " VALUE=" + parameterCommand.getValue());
                        context.getExecutionParameters().add(parameterCommand);
                        intRowCount++;

                        if ((listSubParameters != null)
                            && (!listSubParameters.isEmpty()))
                            {
                            final Iterator<ParameterType> iterSubParameters;

                            iterSubParameters = listSubParameters.iterator();

                            while (iterSubParameters.hasNext())
                                {
                                final ParameterType parameterSub;

                                parameterSub = iterSubParameters.next();

                                if (parameterSub != null)
                                    {
                                    // Don't allow fully recursive parameter building just yet!
                                    // i.e. ignore any more Sub Parameters
                                    buildParameter(context, parameterSub);

//                                    System.out.println("ADDING SUB PARAM " + parameterSub.getName()
//                                                       + " SUB PARAM INDEX " + parameterSub.getSubParameterIndex()
//                                                       + " VALUE=" + parameterSub.getValue());
                                    context.getExecutionParameters().add(parameterSub);
                                    intRowCount++;
                                    }
                                }
                            }
                        else
                            {
//                            System.out.println("NO SUBSEQUENT PARAMS TO ADD");
                            }
                        }
                    else
                        {
//                        System.out.println("NULL PARAM");
                        }
                    }

                // Layout the resulting Parameters (or not)
                if (intRowCount > 0)
                    {
                    SpringUtilities.makeCompactGrid(context.getParameterPanel(),
                                                    intRowCount,
                                                    PARAMETER_COLUMNS,
                                                    PARAMETER_PADDING,
                                                    PARAMETER_PADDING,
                                                    PARAMETER_PADDING,
                                                    PARAMETER_PADDING);
                    }
                else
                    {
//                    System.out.println("ROW COUNT 0, NOTHING TO LAY OUT");
                    CommandProcessorUtilities.showEmptySet(context.getParameterPanel(),
                                                           context.getFontData(),
                                                           context.getColourData(),
                                                           ObservatoryInstrumentInterface.PARAMETERS_NOT_FOUND);
                    }
                }
            else
                {
//                System.out.println("NO COMMAND PARAMS TO ADD");
                CommandProcessorUtilities.showEmptySet(context.getParameterPanel(),
                                                       context.getFontData(),
                                                       context.getColourData(),
                                                       ObservatoryInstrumentInterface.PARAMETERS_NOT_REQUIRED);
                }
            }
        else
            {
//            System.out.println("NOT SELECTED CMD OR MACRO");
            if (CommandProcessorUtilities.hasController(context))
                {
                CommandProcessorUtilities.showEmptySet(context.getParameterPanel(),
                                                       context.getFontData(),
                                                       context.getColourData(),
                                                       CommandProcessorUtilities.NO_SELECTION);
                }
            }

        // Reset the selected tab to the Parameters panel
        if (context.getExecutionTabs() != null)
            {
            context.getExecutionTabs().setSelectedIndex(0);
            }

        // Clear any Parameter Help regardless of the above
        if (context.getParameterHelpViewer() != null)
            {
            context.getParameterHelpViewer().setHTMLText(HTML_PREFIX
                                                         + "Click on a Parameter Help button for information about the parameter. Help is not available for all parameter types."
                                                         + HTML_SUFFIX);
            }

        context.getParameterPanel().revalidate();
        context.getParameterPanel().repaint();
        StarscriptHelper.updateStarscript(context);
        }


    /***********************************************************************************************
     * Build a single Parameter.
     * Return a List of Sub Parameters, if any.
     * These might originate from the default combo box selection.
     *
     * @param context
     * @param parameter
     *
     * @return List<ParameterType>
     */

    private static List<ParameterType> buildParameter(final CommandProcessorContextInterface context,
                                                      final ParameterType parameter)
        {
        final String SOURCE = "ParameterHelper.buildParameter() ";
        final JLabel labelName;
        final JLabel labelUnits;
        final JLabel labelType;
        final DataTypeDictionary dataType;
        final List<ParameterType> listSubParameters;

        // Label the Parameter
        labelName = new JLabel(parameter.getName(), JLabel.LEADING);
        labelName.setFont(context.getFontData().getFont());
        labelName.setForeground(context.getColourData().getColor());
        labelName.setToolTipText(TOOLTIP_PARAMETER_NAME);
        context.getParameterPanel().add(labelName);

        // Get hold of the DataType in order to find the DisplayName
        dataType = DataTypeDictionary.getDataTypeDictionaryEntryForName(parameter.getInputDataType().getDataTypeName().toString());

        // Create a drop-down of the allowed choices (Thanks to Alan Melia...)
        if ((parameter.getChoices() != null)
            && (!EMPTY_STRING.equals(parameter.getChoices())))
            {
            // Choices can never be invalid, so don't test the Value
            // The Choices DropDown may or may not have a Help button
            listSubParameters = buildChoicesDropDown(context, parameter);
            }
        else
            {
            final JTextField textValue;
            final DocumentListener listener;

            // Allow free-text entry
            // Warning! Do not use the no-parameter constructor with SpringLayout!
            textValue = new JTextField(5);

            if ((parameter.getValue() != null)
                && (!EMPTY_STRING.equals(parameter.getValue())))
                {
                textValue.setText(parameter.getValue());
                }
            else
                {
                textValue.setText(EMPTY_STRING);
                }

            if ((parameter.getTooltip() != null)
                && (!EMPTY_STRING.equals(parameter.getTooltip())))
                {
                textValue.setToolTipText(parameter.getTooltip());
                }
            else
                {
                textValue.setToolTipText(TOOLTIP_SELECT_PARAMETER);
                }

            textValue.setMaximumSize(new Dimension(Integer.MAX_VALUE, CommandProcessorUtilities.HEIGHT_BUTTON));
            textValue.setPreferredSize(new Dimension(Integer.MAX_VALUE, CommandProcessorUtilities.HEIGHT_BUTTON));
            textValue.setMargin(new Insets(0, 5, 0, 5));
            textValue.setFont(context.getFontData().getFont());
            textValue.setForeground(context.getColourData().getColor().darker());
            labelName.setLabelFor(textValue);

            // Validate the Parameter JTextField every time it is rebuilt
            // in case e.g. the selection of a new Filter has changed the state of everything
            indicateParameterValidity(context, parameter, textValue);

            // Does this parameter have an associated Chooser?
            if ((dataType.getChooserClassname() != null)
                && (!EMPTY_STRING.equals(dataType.getChooserClassname())))
                {
                final JPanel panelWrapper;
                final JButton buttonChooser;

                panelWrapper = new JPanel();
                panelWrapper.setBorder(BorderFactory.createEmptyBorder());
                panelWrapper.setLayout(new BoxLayoutFixed(panelWrapper, BoxLayoutFixed.X_AXIS));
                panelWrapper.setBackground(UIComponentPlugin.DEFAULT_COLOUR_PANEL.getColor());

                panelWrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, CommandProcessorUtilities.HEIGHT_BUTTON));

                buttonChooser = new JButton(CommandProcessorUtilities.BUTTON_CHOOSER);
                buttonChooser.setFont(context.getFontData().getFont());
                buttonChooser.setForeground(context.getColourData().getColor());

                buttonChooser.setMinimumSize(new Dimension(CommandProcessorUtilities.WIDTH_CHOOSER_BUTTON, CommandProcessorUtilities.HEIGHT_BUTTON));
                buttonChooser.setMaximumSize(new Dimension(CommandProcessorUtilities.WIDTH_CHOOSER_BUTTON, CommandProcessorUtilities.HEIGHT_BUTTON));
                buttonChooser.setPreferredSize(new Dimension(CommandProcessorUtilities.WIDTH_CHOOSER_BUTTON, CommandProcessorUtilities.HEIGHT_BUTTON));

                buttonChooser.setToolTipText(MSG_CHOOSE_PARAMETER);

                buttonChooser.addActionListener(new ActionListener()
                    {
                    public synchronized void actionPerformed(final ActionEvent event)
                        {
                        final ChooserInterface chooser;

                        // Instantiate and show the appropriate Chooser with the current Value
                        chooser = ChooserHelper.instantiateChooser(dataType.getChooserClassname(),
                                                                   context.getObservatoryInstrument(),
                                                                   context.getFontData(),
                                                                   context.getColourData(),
                                                                   textValue.getText());
                        if (chooser != null)
                            {
                            // Tell the Chooser the current value of the Parameter
                            // Go modal...
                            chooser.showChooser((Component)context.getObservatoryUI().getUIOccupant());

                            // Do we still have a working Instrument?
                            if (InstrumentState.isReady(context.getObservatoryInstrument()))
                                {
                                // Don't forget to update the Starscript indicator with the chosen value
                                textValue.setText(chooser.getValue());
                                parameter.setValue(textValue.getText());
                                StarscriptHelper.updateStarscript(context);

                                // Parameter values provided by Choosers should never fail!
                                indicateParameterValidity(context, parameter, textValue);
                                }
                            else
                                {
                                CommandProcessorUtilities.showUnavailableDialog(context);
                                }
                            }
                        else
                            {
                            LOGGER.error(SOURCE + "Can't make chooser [classname=" + dataType.getChooserClassname() + "]");
                            }
                        }
                    });

                panelWrapper.add(textValue);
                panelWrapper.add(Box.createHorizontalStrut(5));
                panelWrapper.add(buttonChooser);

                context.getParameterPanel().add(panelWrapper);
                }
            else
                {
                // Just display the parameter without a Chooser
                context.getParameterPanel().add(textValue);
                }

            // Text Field Listener
            listener = new DocumentListener()
                {
                public void insertUpdate(final DocumentEvent event)
                    {
                    if (InstrumentState.isReady(context.getObservatoryInstrument()))
                        {
                        parameter.setValue(textValue.getText());
                        StarscriptHelper.updateStarscript(context);
                        indicateParameterValidity(context, parameter, textValue);
                        }
                    else
                        {
                        CommandProcessorUtilities.showUnavailableDialog(context);
                        }
                    }

                public void removeUpdate(final DocumentEvent event)
                    {
                    if (InstrumentState.isReady(context.getObservatoryInstrument()))
                        {
                        parameter.setValue(textValue.getText());
                        StarscriptHelper.updateStarscript(context);
                        indicateParameterValidity(context, parameter, textValue);
                        }
                    else
                        {
                        CommandProcessorUtilities.showUnavailableDialog(context);
                        }
                    }

                public void changedUpdate(final DocumentEvent event)
                    {
                    if (InstrumentState.isReady(context.getObservatoryInstrument()))
                        {
                        parameter.setValue(textValue.getText());
                        StarscriptHelper.updateStarscript(context);
                        indicateParameterValidity(context, parameter, textValue);
                        }
                    else
                        {
                        CommandProcessorUtilities.showUnavailableDialog(context);
                        }
                    }
                };

            textValue.getDocument().addDocumentListener(listener);

            // There are no subsequent Parameters
            listSubParameters = null;
            }

        // Now show the Units and DataType
        labelUnits = new JLabel(parameter.getUnits().toString(),
                                JLabel.LEADING);
        labelUnits.setFont(context.getFontData().getFont());
        labelUnits.setForeground(context.getColourData().getColor());
        labelUnits.setToolTipText(TOOLTIP_PARAMETER_UNITS);
        context.getParameterPanel().add(labelUnits);

        labelType = new JLabel(dataType.getDisplayName(),
                               JLabel.LEADING);
        labelType.setFont(context.getFontData().getFont());
        labelType.setForeground(context.getColourData().getColor());
        labelType.setToolTipText(TOOLTIP_PARAMETER_DATA_TYPE);
        context.getParameterPanel().add(labelType);

        return (listSubParameters);
        }


    /***********************************************************************************************
     * Test an individual Parameter to see if it is currently valid.
     * This uses the DataType parsers, so will use any Regex if supplied.
     *
     * @param parameter
     *
     * @return boolean
     */

    public static boolean isParameterValid(final ParameterType parameter)
        {
        final boolean boolValid;

        if ((parameter != null)
            && (parameter.getValue() != null)
            && (parameter.getInputDataType() != null)
            && (parameter.getInputDataType().getDataTypeName() != null))
            {
            final DataTypeDictionary dataType;
            final List<String> errors;
            final int intFailures;

            errors = new ArrayList<String>(10);

            // Get the DataType to use for parsing the input
            dataType = DataTypeDictionary.getDataTypeDictionaryEntryForName(parameter.getInputDataType().getDataTypeName().toString());

            intFailures = DataTypeHelper.validateDataTypeOfParameterValue(parameter, dataType, errors);

            boolValid = (intFailures == 0);
            }
        else
            {
            boolValid = false;
            }

        return (boolValid);
        }


    /***********************************************************************************************
     * Indicate the current validity (or otherwise) of the specified Parameter
     * by changing the appearance of the specified text field.
     *
     * @param context
     * @param parameter
     * @param textfield
     */

    public static void indicateParameterValidity(final CommandProcessorContextInterface context,
                                                 final ParameterType parameter,
                                                 final JTextField textfield)
        {
        if (isParameterValid(parameter))
            {
            textfield.setForeground(context.getColourData().getColor().darker());
            textfield.setBackground(COLOR_PARAMETER_VALID_BG);
            }
        else
            {
            textfield.setForeground(COLOR_PARAMETER_INVALID_FG);
            textfield.setBackground(COLOR_PARAMETER_INVALID_BG);
            }
        }


    /***********************************************************************************************
     * Indicate the current validity (or otherwise) of the specified Parameter
     * by changing the appearance of the specified text field.
     *
     * @param parameter
     * @param textfield
     * @param colourdata
     */

    public static void indicateParameterValidity(final ParameterType parameter,
                                                 final JTextField textfield,
                                                 final ColourInterface colourdata)
        {
        if (isParameterValid(parameter))
            {
            textfield.setForeground(colourdata.getColor().darker());
            textfield.setBackground(COLOR_PARAMETER_VALID_BG);
            }
        else
            {
            textfield.setForeground(COLOR_PARAMETER_INVALID_FG);
            textfield.setBackground(COLOR_PARAMETER_INVALID_BG);
            }
        }


    /**********************************************************************************************
     * Build the drop-down from the Choices tag.
     * If the choice list contains
     *  !PluginList,
     *  !MixerList,
     *  !EphemerisTargets,
     *  !FilterList
     *  !EpochList
     *  !ExportableTabsList
     *
     * then expand to a list of the appropriate items.
     * Return a List of Sub Parameters, if any.
     * These might originate from the default combo box selection.
     *
     * @param context
     * @param parameter
     *
     * @return List<ParameterType>
     */

    private static List<ParameterType> buildChoicesDropDown(final CommandProcessorContextInterface context,
                                                            final ParameterType parameter)
        {
        final String SOURCE = "ParameterHelper.buildChoicesDropDown() ";
        final String[] arrayOriginalChoices;
        final List<String> listProcessedChoices;
        final List<URL> listHelpFileURLs;
        List<ParameterType> listSubParameters;
        boolean boolSubParametersSupported;
        boolean boolHelpSupported;

        // Add the choices from the comma-separated list
        arrayOriginalChoices = parameter.getChoices().split(COMMA);

        // Process the array of choices to see if it contains a token to be expanded
        listProcessedChoices = new ArrayList<String>(50);

        // The List of URLs of Parameter Help associated with each item on the drop-down
        listHelpFileURLs = new ArrayList<URL>(50);

        listSubParameters = null;
        boolSubParametersSupported = false;
        boolHelpSupported = false;

        for (int i = 0; i < arrayOriginalChoices.length; i++)
            {
            final String strChoiceToken;
            final ParameterChoiceToken token;

            strChoiceToken = arrayOriginalChoices[i];

            token = ParameterChoiceToken.getParameterChoiceToken(strChoiceToken);

            // Is this Token recognjsed as one to be expanded?
            if (token != null)
                {
                switch (token)
                    {
                    case PLUGIN_LIST:
                        {
                        // Expand the List to include the Plugin Identifiers
                        ParameterListBuilder.buildPluginList(context, listProcessedChoices);
                        boolSubParametersSupported = token.hasSubParameters();
                        boolHelpSupported = token.hasHelp();
                        break;
                        }

                    case MIXER_SOURCE_LIST:
                        {
                        // Expand the List to include the Audio Mixers
                        ParameterListBuilder.buildMixerSourceLineList(context, listProcessedChoices);
                        boolSubParametersSupported = token.hasSubParameters();
                        boolHelpSupported = token.hasHelp();
                        break;
                        }

                    case EPHEMERIS_TARGETS:
                        {
                        // Expand the List to include all Ephemeris Targets
                        ParameterListBuilder.buildEphemerisTargets(context, listProcessedChoices);
                        boolSubParametersSupported = token.hasSubParameters();
                        boolHelpSupported = token.hasHelp();
                        break;
                        }

                    case FILTER_LIST:
                        {
                        // Expand the List to include all Filters
                        // See if the current selection has any subsequent Parameters
                        listSubParameters = ParameterListBuilder.buildFilterList(context,
                                                                                 listProcessedChoices,
                                                                                 parameter,
                                                                                 listHelpFileURLs);
//                        if (listSubParameters != null)
//                            {
//                            System.out.println("FILTER TYPE COULD HAVE SUBPARAMS=" + token.hasSubParameters() + " subparamlistsize" + listSubParameters.size());
//                            }
//                        else
//                            {
//                            System.out.println("FILTER HAS SUBPARAMS=" + token.hasSubParameters());
//                            }
                        boolSubParametersSupported = token.hasSubParameters();
                        boolHelpSupported = token.hasHelp();
                        break;
                        }

                    case EPOCH_LIST:
                        {
                        // Expand the List to include the Epochs
                        ParameterListBuilder.buildEpochList(context, listProcessedChoices);
                        boolSubParametersSupported = token.hasSubParameters();
                        boolHelpSupported = token.hasHelp();
                        break;
                        }

                    case EXPORTABLE_TABS_LIST:
                        {
                        // Expand the List to include the ExportableTabs
                        ParameterListBuilder.buildExportableTabsList(context, listProcessedChoices);
                        boolSubParametersSupported = token.hasSubParameters();
                        boolHelpSupported = token.hasHelp();
                        break;
                        }

                    case OSCILLATOR_WAVEFORM_LIST:
                        {
                        // Expand the List to include all OscillatorWaveforms
                        ParameterListBuilder.buildOscillatorWaveformList(context.getObservatoryUI().getHostAtom(),
                                                                         listProcessedChoices,
                                                                         listHelpFileURLs);
                        boolSubParametersSupported = token.hasSubParameters();
                        boolHelpSupported = token.hasHelp();
                        break;
                        }

                    default:
                        {
                        // Simply add the unexpanded Choice (but this should never happen)
                        listProcessedChoices.add(strChoiceToken);
                        boolSubParametersSupported = false;
                        boolHelpSupported = false;
                        }
                    }
                }
            else
                {
                // Simply add the unexpanded Choice
                listProcessedChoices.add(strChoiceToken);
                boolSubParametersSupported = false;
                boolHelpSupported = false;
                }
            }

        buildChoicesCombo(context,
                          parameter,
                          listProcessedChoices,
                          boolSubParametersSupported,
                          boolHelpSupported,
                          listHelpFileURLs);

        return (listSubParameters);
        }


    /***********************************************************************************************
     * Build the Choices JComboBox, with or without a Help button.
     * SEE THE SAME CODE IN SIMPLIFIED PARAMETER HELPER!
     *
     * @param context
     * @param parameter
     * @param choiceslist
     * @param subparameterspossible
     * @param helpsupported
     * @param helpfileurls
     */

    private static void buildChoicesCombo(final CommandProcessorContextInterface context,
                                          final ParameterType parameter,
                                          final List<String> choiceslist,
                                          final boolean subparameterspossible,
                                          final boolean helpsupported,
                                          final List<URL> helpfileurls)
        {
        final JComboBox comboChoices;
        final boolean boolChoicesValid;
        final ActionListener choiceListener;

        // Now make the Combo Box from the processed List of Choices
        comboChoices = new JComboBox(choiceslist.toArray());
        comboChoices.setFont(context.getFontData().getFont());
        comboChoices.setForeground(context.getColourData().getColor());
        comboChoices.setRenderer(new AlignedListCellRenderer(SwingConstants.LEFT,
                                                             context.getFontData(),
                                                             context.getColourData(),
                                                             null));

        // Do NOT allow the combo box to take up all the remaining space!
        if (helpsupported)
            {
            comboChoices.setPreferredSize(new Dimension(CommandProcessorUtilities.WIDTH_BUTTON,
                                                        CommandProcessorUtilities.HEIGHT_BUTTON));
            comboChoices.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                                                      CommandProcessorUtilities.HEIGHT_BUTTON));
            }
        else
            {
            comboChoices.setPreferredSize(new Dimension(CommandProcessorUtilities.WIDTH_BUTTON,
                                                        CommandProcessorUtilities.HEIGHT_BUTTON));
            comboChoices.setMaximumSize(new Dimension(CommandProcessorUtilities.WIDTH_BUTTON,
                                                      CommandProcessorUtilities.HEIGHT_BUTTON));
            }

        comboChoices.setAlignmentX(0);

        if ((parameter.getTooltip() != null)
            && (!EMPTY_STRING.equals(parameter.getTooltip())))
            {
            comboChoices.setToolTipText(parameter.getTooltip());
            }
        else
            {
            comboChoices.setToolTipText(TOOLTIP_SELECT_PARAMETER);
            }

        // 2013-12-23 Make sure that we should be allowing choices,
        // e.g. if there are no Audio Mixers, then prevent execution
        boolChoicesValid = ((choiceslist.size() > 0)
                            && (!choiceslist.contains(ParameterListBuilder.NO_AUDIO_MIXERS)));

        comboChoices.setEnabled(boolChoicesValid);
        comboChoices.setEditable(false);
        comboChoices.setMaximumRowCount(Math.max(choiceslist.size(), 50));

        // 2011-05-16 Attempt to retain the previous selection on the drop-down
        if ((parameter.getValue() != null)
            && (!EMPTY_STRING.equals(parameter.getValue())))
            {
            // This may not work, but it's worth a try...
            comboChoices.setSelectedItem(parameter.getValue());
            }
        else
            {
            // The user may not select anything from the drop-down...
            comboChoices.setSelectedIndex(0);
            parameter.setValue((comboChoices.getSelectedItem().toString()));
            }

        StarscriptHelper.updateStarscript(context);

        choiceListener = new ActionListener()
            {
            public void actionPerformed(final ActionEvent event)
                {
                if (InstrumentState.isReady(context.getObservatoryInstrument()))
                    {
                    // The XSD says the choices must be Strings
                    parameter.setValue(((String)comboChoices.getSelectedItem()));

                    // A change of Choice must cause the whole Parameter panel to be redrawn,
                    // but only if the Choice is of a type which has subsequent Parameters
                    if (subparameterspossible)
                        {
                        rebuildParametersPanel(context);
                        }

                    StarscriptHelper.updateStarscript(context);
                    }
                else
                    {
                    CommandProcessorUtilities.showUnavailableDialog(context);
                    }
                }
            };

        comboChoices.addActionListener(choiceListener);

        // Make a wrapper panel to hold the JComboBox and the Help Button
        if (helpsupported)
            {
            final JPanel panelWrapper;
            final JButton buttonHelp;

            panelWrapper = new JPanel();
            panelWrapper.setBorder(BorderFactory.createEmptyBorder());
            panelWrapper.setLayout(new BoxLayoutFixed(panelWrapper, BoxLayoutFixed.X_AXIS));
            panelWrapper.setBackground(UIComponentPlugin.DEFAULT_COLOUR_PANEL.getColor());

            panelWrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, CommandProcessorUtilities.HEIGHT_BUTTON));

            buttonHelp = new JButton(BUTTON_HELP);
            buttonHelp.setFont(context.getFontData().getFont());
            buttonHelp.setForeground(context.getColourData().getColor());

            buttonHelp.setMinimumSize(new Dimension(WIDTH_HELP_BUTTON, CommandProcessorUtilities.HEIGHT_BUTTON));
            buttonHelp.setMaximumSize(new Dimension(WIDTH_HELP_BUTTON, CommandProcessorUtilities.HEIGHT_BUTTON));
            buttonHelp.setPreferredSize(new Dimension(WIDTH_HELP_BUTTON, CommandProcessorUtilities.HEIGHT_BUTTON));

            buttonHelp.setToolTipText(TOOLTIP_PARAMETER_HELP);

            buttonHelp.addActionListener(new ActionListener()
                {
                public synchronized void actionPerformed(final ActionEvent event)
                    {
                    // Display the Parameter Help, if there is a valid selection
                    if ((context.getExecutionTabs() != null)
                        && (context.getParameterHelpViewer() != null)
                        && (helpfileurls != null)
                        && (!helpfileurls.isEmpty())
                        && (comboChoices.getSelectedIndex() >= 0)
                        && (comboChoices.getSelectedIndex() < helpfileurls.size()))
                        {
                        try
                            {
                            context.getParameterHelpViewer().gotoURL(helpfileurls.get(comboChoices.getSelectedIndex()));

                            // Set the selected tab to the Parameters Help viewer
                            context.getExecutionTabs().setSelectedComponent(context.getParameterHelpViewer());
                            }

                        catch (IndexOutOfBoundsException exception)
                            {
                            // Fail silently
                            }

                        catch (IllegalArgumentException exception)
                            {
                            // Fail silently
                            }
                        }
                    }
                });

            panelWrapper.add(comboChoices);
            panelWrapper.add(Box.createHorizontalStrut(5));
            panelWrapper.add(buttonHelp);

            context.getParameterPanel().add(panelWrapper);
            }
        else
            {
            // Just display the parameter without a Help Button
            context.getParameterPanel().add(comboChoices);
            }
        }


    /**********************************************************************************************/
    /* Parameter Access                                                                           */
    /***********************************************************************************************
     * Get a Parameter, given its Name.
     * Return NULL if the Name cannot be found in the List.
     *
     * @param parameterlist
     * @param name
     *
     * @return ParameterType
     */

    public static ParameterType getParameterByName(final List<ParameterType> parameterlist,
                                                   final String name)
        {
        final String SOURCE = "ParameterHelper.getParameterByName() ";
        ParameterType parameter;

        parameter = null;

        if ((parameterlist != null)
            && (!parameterlist.isEmpty())
            && (name != null)
            && (!EMPTY_STRING.equals(name)))
            {
            final Iterator<ParameterType> iterParameters;

            iterParameters = parameterlist.iterator();

            // Just iterate over all Parameters (there are unlikely to be many...)
            while ((iterParameters.hasNext())
                   && (parameter == null))
                {
                final ParameterType parameterType;

                parameterType = iterParameters.next();

                if ((parameterType != null)
                    && (name.equals(parameterType.getName())))
                    {
                    parameter = parameterType;
                    }
                }
            }

        return (parameter);
        }


    /***********************************************************************************************
     * Get the Value of a Parameter, given its Name.
     * Return NO_DATA if the Name cannot be found in the List.
     *
     * @param parameterlist
     * @param name
     *
     * @return String
     */

    public static String getParameterValueByName(final List<ParameterType> parameterlist,
                                                 final String name)
        {
        final String SOURCE = "ParameterHelper.getParameterValueByName() ";

        String strValue;
        final ParameterType parameter;

        strValue = NO_DATA;

        // Return NULL if the Name cannot be found in the List, or the List is NULL
        parameter = getParameterByName(parameterlist, name);

        if (parameter != null)
            {
            strValue = parameter.getValue();
            }

        return (strValue);
        }


    /***********************************************************************************************
     * Get a Parameter by Name, as a DecimalInteger.
     *
     * @param parameterlist
     * @param name
     *
     * @return int
     */

    public static int getParameterByNameAsDecimalInteger(final List<ParameterType> parameterlist,
                                                         final String name)
        {
        final ParameterType parameter;
        int intValue;

        parameter = getParameterByName(parameterlist, name);

        if ((parameter != null)
            && (SchemaDataType.DECIMAL_INTEGER.equals(parameter.getInputDataType().getDataTypeName())))
            {
            try
                {
                final String strValue;

                strValue = parameter.getValue();
                intValue = Integer.parseInt(strValue);
                }

            catch (NumberFormatException exception)
                {
                // This should of course never happen!
                intValue = 0;
                }
            }
        else
            {
            intValue = 0;
            }

        return (intValue);
        }
    }
