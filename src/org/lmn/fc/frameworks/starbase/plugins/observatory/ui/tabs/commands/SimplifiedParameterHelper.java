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
import org.lmn.fc.frameworks.starbase.plugins.observatory.ObservatoryInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.InstrumentState;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.InstrumentHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.ExecutionContextInterface;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.DataTypeDictionary;
import org.lmn.fc.model.datatypes.FontInterface;
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/***************************************************************************************************
 * SimplifiedParameterHelper.
 */

public final class SimplifiedParameterHelper implements FrameworkConstants,
                                                        FrameworkStrings,
                                                        FrameworkMetadata,
                                                        FrameworkSingletons,
                                                        ResourceKeys
    {
    /***********************************************************************************************
     * Create the ParametersPanel in a JScrollPane, and place on a JPanel.
     * All parameters are assumed to be not NULL.
     *
     * @param executioncontext
     * @param fontdata
     * @param colourforeground
     * @param colourbackground
     * @param drawborder
     *
     * @return JPanel
     */

    public static JPanel createParametersPanel(final ExecutionContextInterface executioncontext,
                                               final FontInterface fontdata,
                                               final ColourInterface colourforeground,
                                               final ColourInterface colourbackground,
                                               final boolean drawborder)
        {
        final String SOURCE = "SimplifiedParameterHelper.createParametersPanel() ";
        final JPanel panelScrollableParameters;
        final JPanel panelAssembledParameters;
        final JScrollPane scrollParameters;

        panelScrollableParameters = new JPanel();
        panelScrollableParameters.setLayout(new BorderLayout());

        if (drawborder)
            {
            final TitledBorder titledBorder;
            final Border border;

            titledBorder = BorderFactory.createTitledBorder(ParameterHelper.TITLE_PARAMETERS);
            titledBorder.setTitleFont(fontdata.getFont());
            titledBorder.setTitleColor(colourforeground.getColor());
            border = BorderFactory.createCompoundBorder(titledBorder,
                                                        BorderFactory.createEmptyBorder(4, 4, 4, 4));
            panelScrollableParameters.setBorder(border);
            }
        else
            {
            panelScrollableParameters.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
            }

        panelScrollableParameters.setBackground(colourbackground.getColor());
        panelScrollableParameters.setMinimumSize(new Dimension(100, 100));
        panelScrollableParameters.setPreferredSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        // This will expand Parameters if appropriate
        panelAssembledParameters = new JPanel();
        rebuildParametersPanel(executioncontext,
                               panelAssembledParameters,
                               fontdata,
                               colourforeground,
                               colourbackground);

        StarscriptHelper.updateStarscript(executioncontext);
        InstrumentHelper.notifyInstrumentChanged(executioncontext.getObservatoryInstrument());

        // Present the rebuilt Parameters panel on a scroll pane
        scrollParameters = new JScrollPane(panelAssembledParameters,
                                           JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                           JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollParameters.setBorder(BorderFactory.createEmptyBorder());
        scrollParameters.setMinimumSize(new Dimension(100, 100));
        scrollParameters.setPreferredSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        // The setDividerLocation(double) method has no effect if the split pane has no size
        // (typically true if it isn't onscreen yet). You can either use setDividerLocation(int)
        // or specify the preferred sizes of the split pane's contained components
        // and the split pane's resize weight instead.

        panelScrollableParameters.add(scrollParameters, BorderLayout.CENTER);

        return (panelScrollableParameters);
        }


    /***********************************************************************************************
     * Rebuild the Parameters panel to show text fields or combo boxes for each Parameter required.
     *
     * @param executioncontext
     * @param parameterspanel
     * @param fontdata
     * @param colourforeground
     * @param colourbackground
     */

    private static void rebuildParametersPanel(final ExecutionContextInterface executioncontext,
                                               final JPanel parameterspanel,
                                               final FontInterface fontdata,
                                               final ColourInterface colourforeground,
                                               final ColourInterface colourbackground)
        {
        final String SOURCE = "SimplifiedParameterHelper.rebuildParametersPanel() ";

        parameterspanel.removeAll();
        parameterspanel.setLayout(new SpringLayout());
        parameterspanel.setBackground(colourbackground.getColor());

        // No Parameters used for execution yet
        executioncontext.getStarscriptExecutionParameters().clear();

        if (executioncontext.isSelectedMacroOrCommand())
            {
            // Iterate over the Parameters as specified by the Instrument XML, i.e. not yet expanded
            if ((executioncontext.getStarscriptCommand() != null)
                && (executioncontext.getStarscriptCommand().getParameterList() != null)
                && (!executioncontext.getStarscriptCommand().getParameterList().isEmpty()))
                {
                final Iterator<ParameterType> iterCommandParameters;
                int intRowCount;

                iterCommandParameters = executioncontext.getStarscriptCommand().getParameterList().iterator();
                intRowCount = 0;

                while (iterCommandParameters.hasNext())
                    {
                    final ParameterType parameterCommand;

                    parameterCommand = iterCommandParameters.next();

                    if (parameterCommand != null)
                        {
                        final List<ParameterType> listSubParameters;

                        listSubParameters = buildParameter(executioncontext,
                                                           parameterCommand,
                                                           parameterspanel,
                                                           fontdata,
                                                           colourforeground,
                                                           colourbackground);

//                        System.out.println("ADDING MAIN PARAM TO EXECUTED " + parameterCommand.getName()
//                                           + " SUB PARAM INDEX=" + parameterCommand.getSubParameterIndex()
//                                           + " VALUE=" + parameterCommand.getValue());
                        executioncontext.getStarscriptExecutionParameters().add(parameterCommand);
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
                                    buildParameter(executioncontext,
                                                   parameterSub,
                                                   parameterspanel,
                                                   fontdata,
                                                   colourforeground,
                                                   colourbackground);

//                                    System.out.println("ADDING SUB PARAM " + parameterSub.getName()
//                                                       + " SUB PARAM INDEX " + parameterSub.getSubParameterIndex()
//                                                       + " VALUE=" + parameterSub.getValue());
                                    executioncontext.getStarscriptExecutionParameters().add(parameterSub);
                                    intRowCount++;
                                    }
                                }
                            }
                        else
                            {
                            //System.out.println("NO SUBSEQUENT PARAMS TO ADD");
                            }
                        }
                    else
                        {
                        //System.out.println("NULL PARAM");
                        }
                    }

                // Layout the resulting Parameters (or not)
                if (intRowCount > 0)
                    {
                    SpringUtilities.makeCompactGrid(parameterspanel,
                                                    intRowCount,
                                                    ParameterHelper.PARAMETER_COLUMNS,
                                                    ParameterHelper.PARAMETER_PADDING,
                                                    ParameterHelper.PARAMETER_PADDING,
                                                    ParameterHelper.PARAMETER_PADDING,
                                                    ParameterHelper.PARAMETER_PADDING);
                    }
                else
                    {
                    //System.out.println("ROW COUNT 0, NOTHING TO LAY OUT");
                    CommandProcessorUtilities.showEmptySet(parameterspanel,
                                                           fontdata,
                                                           colourforeground,
                                                           ObservatoryInstrumentInterface.PARAMETERS_NOT_FOUND);
                    }
                }
            else
                {
                //System.out.println("NO COMMAND PARAMS TO ADD");
                CommandProcessorUtilities.showEmptySet(parameterspanel,
                                                       fontdata,
                                                       colourforeground,
                                                       ObservatoryInstrumentInterface.PARAMETERS_NOT_REQUIRED);
                }
            }
        else
            {
            //System.out.println("NOT SELECTED CMD OR MACRO");
            if (CommandProcessorUtilities.hasController(executioncontext.getObservatoryInstrument().getInstrument()))
                {
                CommandProcessorUtilities.showEmptySet(parameterspanel,
                                                       fontdata,
                                                       colourforeground,
                                                       CommandProcessorUtilities.NO_SELECTION);
                }
            }

        parameterspanel.revalidate();
        parameterspanel.repaint();

        StarscriptHelper.updateStarscript(executioncontext);
        }


    /***********************************************************************************************
     * Build a single Parameter.
     * Return a List of Sub Parameters, if any.
     * These might originate from the default combo box selection.
     *
     * @param executioncontext
     * @param parameter
     * @param parameterspanel
     * @param fontdata
     * @param colourforeground
     * @param colourbackground
     *
     * @return List<ParameterType>
     */

    private static List<ParameterType> buildParameter(final ExecutionContextInterface executioncontext,
                                                      final ParameterType parameter,
                                                      final JPanel parameterspanel,
                                                      final FontInterface fontdata,
                                                      final ColourInterface colourforeground,
                                                      final ColourInterface colourbackground)
        {
        final String SOURCE = "SimplifiedParameterHelper.buildParameter() ";
        final JLabel labelName;
        final JLabel labelUnits;
        final JLabel labelType;
        final DataTypeDictionary dataType;
        final List<ParameterType> listSubParameters;

        // Label the Parameter
        labelName = new JLabel(parameter.getName(), JLabel.LEADING);
        labelName.setFont(fontdata.getFont());
        labelName.setForeground(colourforeground.getColor());
        labelName.setToolTipText(ParameterHelper.TOOLTIP_PARAMETER_NAME);
        parameterspanel.add(labelName);

        // Get hold of the DataType in order to find the DisplayName
        dataType = DataTypeDictionary.getDataTypeDictionaryEntryForName(parameter.getInputDataType().getDataTypeName().toString());

        // Create a drop-down of the allowed choices (Thanks to Alan Melia...)
        if ((parameter.getChoices() != null)
            && (!EMPTY_STRING.equals(parameter.getChoices())))
            {
            // Choices can never be invalid, so don't test the Value
            listSubParameters = buildChoicesDropDown(executioncontext,
                                                     parameter,
                                                     parameterspanel,
                                                     fontdata,
                                                     colourforeground,
                                                     colourbackground);
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
                textValue.setToolTipText(ParameterHelper.TOOLTIP_SELECT_PARAMETER);
                }

            textValue.setMaximumSize(new Dimension(Integer.MAX_VALUE, CommandProcessorUtilities.HEIGHT_BUTTON));
            textValue.setPreferredSize(new Dimension(Integer.MAX_VALUE, CommandProcessorUtilities.HEIGHT_BUTTON));
            textValue.setMargin(new Insets(0, 5, 0, 5));
            textValue.setFont(fontdata.getFont());
            textValue.setForeground(colourforeground.getColor().darker());
            labelName.setLabelFor(textValue);

            // Validate the Parameter JTextField every time it is rebuilt
            // in case e.g. the selection of a new Filter has changed the state of everything
            ParameterHelper.indicateParameterValidity(parameter, textValue, colourforeground);

            // Does this parameter have an associated Chooser?
            if ((dataType.getChooserClassname() != null)
                && (!EMPTY_STRING.equals(dataType.getChooserClassname())))
                {
                final JPanel panelWrapper;
                final JButton buttonChooser;

                panelWrapper = new JPanel();
                panelWrapper.setBorder(BorderFactory.createEmptyBorder());
                panelWrapper.setLayout(new BoxLayoutFixed(panelWrapper, BoxLayoutFixed.X_AXIS));
                panelWrapper.setBackground(colourbackground.getColor());

                panelWrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, CommandProcessorUtilities.HEIGHT_BUTTON));

                buttonChooser = new JButton(CommandProcessorUtilities.BUTTON_CHOOSER);
                buttonChooser.setFont(fontdata.getFont());
                buttonChooser.setForeground(colourforeground.getColor());

                buttonChooser.setMinimumSize(new Dimension(CommandProcessorUtilities.WIDTH_CHOOSER_BUTTON, CommandProcessorUtilities.HEIGHT_BUTTON));
                buttonChooser.setMaximumSize(new Dimension(CommandProcessorUtilities.WIDTH_CHOOSER_BUTTON, CommandProcessorUtilities.HEIGHT_BUTTON));
                buttonChooser.setPreferredSize(new Dimension(CommandProcessorUtilities.WIDTH_CHOOSER_BUTTON, CommandProcessorUtilities.HEIGHT_BUTTON));

                buttonChooser.setToolTipText(ParameterHelper.MSG_CHOOSE_PARAMETER);

                buttonChooser.addActionListener(new ActionListener()
                    {
                    public synchronized void actionPerformed(final ActionEvent event)
                        {
                        final ChooserInterface chooser;

                        // Instantiate and show the appropriate Chooser with the current Value
                        chooser = ChooserHelper.instantiateChooser(dataType.getChooserClassname(),
                                                                   executioncontext.getObservatoryInstrument(),
                                                                   fontdata,
                                                                   colourforeground,
                                                                   textValue.getText());
                        if (chooser != null)
                            {
                            // Tell the Chooser the current value of the Parameter
                            // Go modal...
                            chooser.showChooser((Component) executioncontext.getObservatoryInstrument().getHostUI().getUIOccupant());

                            // Do we still have a working Instrument?
                            if (InstrumentState.isReady(executioncontext.getObservatoryInstrument()))
                                {
                                // Don't forget to update the Starscript indicator with the chosen value
                                textValue.setText(chooser.getValue());
                                parameter.setValue(textValue.getText());

                                StarscriptHelper.updateStarscript(executioncontext);

                                // Parameter values provided by Choosers should never fail!
                                ParameterHelper.indicateParameterValidity(parameter, textValue, colourforeground);
                                }
                            else
                                {
                                CommandProcessorUtilities.showUnavailableDialog(executioncontext.getObservatoryInstrument());
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

                parameterspanel.add(panelWrapper);
                }
            else
                {
                // Just display the parameter without a Chooser
                parameterspanel.add(textValue);
                }

            // Text Field Listener
            listener = new DocumentListener()
                {
                public void insertUpdate(final DocumentEvent event)
                    {
                    if (InstrumentState.isReady(executioncontext.getObservatoryInstrument()))
                        {
                        parameter.setValue(textValue.getText());
                        StarscriptHelper.updateStarscript(executioncontext);
                        ParameterHelper.indicateParameterValidity(parameter, textValue, colourforeground);
                        }
                    else
                        {
                        CommandProcessorUtilities.showUnavailableDialog(executioncontext.getObservatoryInstrument());
                        }
                    }

                public void removeUpdate(final DocumentEvent event)
                    {
                    if (InstrumentState.isReady(executioncontext.getObservatoryInstrument()))
                        {
                        parameter.setValue(textValue.getText());
                        StarscriptHelper.updateStarscript(executioncontext);
                        ParameterHelper.indicateParameterValidity(parameter, textValue, colourforeground);
                        }
                    else
                        {
                        CommandProcessorUtilities.showUnavailableDialog(executioncontext.getObservatoryInstrument());
                        }
                    }

                public void changedUpdate(final DocumentEvent event)
                    {
                    if (InstrumentState.isReady(executioncontext.getObservatoryInstrument()))
                        {
                        parameter.setValue(textValue.getText());
                        StarscriptHelper.updateStarscript(executioncontext);
                        ParameterHelper.indicateParameterValidity(parameter, textValue, colourforeground);
                        }
                    else
                        {
                        CommandProcessorUtilities.showUnavailableDialog(executioncontext.getObservatoryInstrument());
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
        labelUnits.setFont(fontdata.getFont());
        labelUnits.setForeground(colourforeground.getColor());
        labelUnits.setToolTipText(ParameterHelper.TOOLTIP_PARAMETER_UNITS);
        parameterspanel.add(labelUnits);

        labelType = new JLabel(dataType.getDisplayName(),
                               JLabel.LEADING);
        labelType.setFont(fontdata.getFont());
        labelType.setForeground(colourforeground.getColor());
        labelType.setToolTipText(ParameterHelper.TOOLTIP_PARAMETER_DATA_TYPE);
        parameterspanel.add(labelType);

        return (listSubParameters);
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
     *  !OscillatorWaveformList
     *
     * then expand to a list of the appropriate items.
     * Return a List of Sub Parameters, if any.
     * These might originate from the default combo box selection.
     *
     * @param executioncontext
     * @param parameter
     * @param parameterspanel
     * @param fontdata
     * @param colourforeground
     * @param colourbackground
     *
     * @return List<ParameterType>
     */

    private static List<ParameterType> buildChoicesDropDown(final ExecutionContextInterface executioncontext,
                                                            final ParameterType parameter,
                                                            final JPanel parameterspanel,
                                                            final FontInterface fontdata,
                                                            final ColourInterface colourforeground,
                                                            final ColourInterface colourbackground)
        {
        final String SOURCE = "SimplifiedParameterHelper.buildChoicesDropDown() ";
        final String[] arrayOriginalChoices;
        final List<String> listProcessedChoices;
        List<ParameterType> listSubParameters;
        boolean boolSubParametersSupported;
        boolean boolHelpSupported;

        // Add the choices from the comma-separated list
        arrayOriginalChoices = parameter.getChoices().split(COMMA);

        // Process the array of choices to see if it contains a token to be expanded
        listProcessedChoices = new ArrayList<String>(50);

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
                        ParameterListBuilder.buildPluginList(executioncontext.getStarscriptInstrument(), listProcessedChoices);
                        boolSubParametersSupported = token.hasSubParameters();
                        boolHelpSupported = token.hasHelp();
                        break;
                        }

                    case MIXER_SOURCE_LIST:
                        {
                        // Expand the List to include the Audio Mixers
                        ParameterListBuilder.buildMixerSourceLineList(listProcessedChoices);
                        boolSubParametersSupported = token.hasSubParameters();
                        boolHelpSupported = token.hasHelp();
                        break;
                        }

                    case EPHEMERIS_TARGETS:
                        {
                        // Expand the List to include all Ephemeris Targets
                        ParameterListBuilder.buildEphemerisTargets((ObservatoryInterface) executioncontext.getObservatoryInstrument().getHostUI().getHostAtom(),
                                                                   listProcessedChoices);
                        boolSubParametersSupported = token.hasSubParameters();
                        boolHelpSupported = token.hasHelp();
                        break;
                        }

                    case FILTER_LIST:
                        {
                        // Expand the List to include all Filters
                        // See if the current selection has any subsequent Parameters
                        listSubParameters = ParameterListBuilder.buildFilterList(listProcessedChoices, parameter);
                        boolSubParametersSupported = token.hasSubParameters();
                        boolHelpSupported = token.hasHelp();
                        break;
                        }

                    case EPOCH_LIST:
                        {
                        // Expand the List to include the Epochs
                        ParameterListBuilder.buildEpochList(listProcessedChoices);
                        boolSubParametersSupported = token.hasSubParameters();
                        boolHelpSupported = token.hasHelp();
                        break;
                        }

                    case EXPORTABLE_TABS_LIST:
                        {
                        // Expand the List to include the ExportableTabs
                        ParameterListBuilder.buildExportableTabsList(executioncontext.getObservatoryInstrument().getInstrumentPanel(),
                                                                     listProcessedChoices);
                        boolSubParametersSupported = token.hasSubParameters();
                        boolHelpSupported = token.hasHelp();
                        break;
                        }

                    case OSCILLATOR_WAVEFORM_LIST:
                        {
                        // Expand the List to include all OscillatorWaveforms
                        ParameterListBuilder.buildOscillatorWaveformList(executioncontext.getObservatoryInstrument().getHostAtom(),
                                                                         listProcessedChoices,
                                                                         null);
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

        // Let's assume for now that this way of choosing a Parameter doesn't have a way of showing Help
        buildChoicesCombo(executioncontext,
                          parameter,
                          parameterspanel,
                          listProcessedChoices,
                          boolSubParametersSupported,
                          //boolHelpSupported,
                          false,
                          fontdata,
                          colourforeground,
                          colourbackground);

        return (listSubParameters);
        }


    /***********************************************************************************************
     * Build the Choices JComboBox, with or without a Help button.
     * SEE THE SAME CODE IN PARAMETER HELPER!
     *
     * @param executioncontext
     * @param parameter
     * @param parameterspanel
     * @param choiceslist
     * @param subparameterspossible
     * @param helpsupported
     * @param fontdata
     * @param colourforeground
     * @param colourbackground
     */

    private static void buildChoicesCombo(final ExecutionContextInterface executioncontext,
                                          final ParameterType parameter,
                                          final JPanel parameterspanel,
                                          final List<String> choiceslist,
                                          final boolean subparameterspossible,
                                          final boolean helpsupported,
                                          final FontInterface fontdata,
                                          final ColourInterface colourforeground,
                                          final ColourInterface colourbackground)
        {
        final JComboBox comboChoices;
        final ActionListener choiceListener;

        // Now make the Combo Box from the processed List of Choices
        comboChoices = new JComboBox(choiceslist.toArray());
        comboChoices.setFont(fontdata.getFont());
        comboChoices.setForeground(colourforeground.getColor());
        comboChoices.setRenderer(new AlignedListCellRenderer(SwingConstants.LEFT,
                                                             fontdata,
                                                             colourforeground,
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
            comboChoices.setToolTipText(ParameterHelper.TOOLTIP_SELECT_PARAMETER);
            }

        comboChoices.setEnabled(true);
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

        StarscriptHelper.updateStarscript(executioncontext);

        choiceListener = new ActionListener()
            {
            public void actionPerformed(final ActionEvent event)
                {
                if (InstrumentState.isReady(executioncontext.getObservatoryInstrument()))
                    {
                    // The XSD says the choices must be Strings
                    parameter.setValue(((String)comboChoices.getSelectedItem()));

                    // A change of Choice must cause the whole Parameter panel to be redrawn,
                    // but only if the Choice is of a type which has subsequent Parameters
                    if (subparameterspossible)
                        {
                        rebuildParametersPanel(executioncontext,
                                               parameterspanel,
                                               fontdata,
                                               colourforeground,
                                               colourbackground);
                        }

                    StarscriptHelper.updateStarscript(executioncontext);
                    }
                else
                    {
                    CommandProcessorUtilities.showUnavailableDialog(executioncontext.getObservatoryInstrument());
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

            buttonHelp = new JButton(ParameterHelper.BUTTON_HELP);
            buttonHelp.setFont(fontdata.getFont());
            buttonHelp.setForeground(colourforeground.getColor());

            buttonHelp.setMinimumSize(new Dimension(ParameterHelper.WIDTH_HELP_BUTTON, CommandProcessorUtilities.HEIGHT_BUTTON));
            buttonHelp.setMaximumSize(new Dimension(ParameterHelper.WIDTH_HELP_BUTTON, CommandProcessorUtilities.HEIGHT_BUTTON));
            buttonHelp.setPreferredSize(new Dimension(ParameterHelper.WIDTH_HELP_BUTTON, CommandProcessorUtilities.HEIGHT_BUTTON));

            buttonHelp.setToolTipText(ParameterHelper.TOOLTIP_PARAMETER_HELP);

            buttonHelp.addActionListener(new ActionListener()
                {
                public synchronized void actionPerformed(final ActionEvent event)
                    {
                    //                    System.out.println("HELP! Help file names:");
                    //                    for (int i = 0;
                    //                         i < choiceslist.size();
                    //                         i++)
                    //                        {
                    //                        String strChoice = choiceslist.get(i);
                    //
                    //                        System.out.println(strChoice + "Help.html");
                    //                        }
                    System.out.println("HELP!");

                    if (executioncontext.getParameterHelpViewer() != null)
                        {
                        executioncontext.getParameterHelpViewer().setHTMLText("HELP from "
                                                                                 + comboChoices.getSelectedItem().toString()
                                                                                 + "Help.html");
                        }
                    // SEE THE SAME CODE IN PARAMETER HELPER!
                    }
                });

            panelWrapper.add(comboChoices);
            panelWrapper.add(Box.createHorizontalStrut(5));
            panelWrapper.add(buttonHelp);

            parameterspanel.add(panelWrapper);
            }
        else
            {
            // Just display the parameter without a Help Button
            parameterspanel.add(comboChoices);
            }
        }
    }
