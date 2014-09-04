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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.charts;


import info.clearthought.layout.TableLayout;
import org.jfree.data.UnknownKeyException;
import org.jfree.data.general.Series;
import org.jfree.data.xy.XYDataset;
import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.common.datatranslators.DatasetType;
import org.lmn.fc.common.utilities.ui.AlignedListCellRenderer;
import org.lmn.fc.frameworks.starbase.plugins.observatory.MetadataDictionary;
import org.lmn.fc.frameworks.starbase.plugins.observatory.common.ObservatoryConstants;
import org.lmn.fc.frameworks.starbase.plugins.observatory.events.DatasetChangedEvent;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.MetadataHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.ChannelSelectionMode;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.ChannelSelectorUIComponentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.ChartUIComponentPlugin;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;
import org.lmn.fc.ui.UIComponentPlugin;
import org.lmn.fc.ui.layout.BoxLayoutFixed;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;


/***************************************************************************************************
 * ChannelSelectorHelper.
 */

public final class ChannelSelectorHelper implements FrameworkConstants,
                                                    FrameworkStrings,
                                                    FrameworkMetadata,
                                                    FrameworkSingletons,
                                                    ObservatoryConstants
    {
    // String Resources
    private static final String TITLE_REMOVE_DECIMATION = "Remove decimation for a large dataset";
    private static final String TITLE_CROP_RAWDATA = "Crop RawData to selected range";
    private static final String BUTTON_LINEAR = "Linear";
    private static final String BUTTON_LOG = "Logarithmic";
    private static final String TOOLTIP_LINEAR = "Select a Linear plot";
    private static final String TOOLTIP_LOGARITHMIC = "Select a Logarithmic plot";

    private static final int PANEL_SPACER = 4;
    private static final int COMPONENT_SPACER = 3;
    private static final int RADIO_SPACER = 4;
    private static final Dimension DIM_CHANNEL_INDICATOR = new Dimension(5, 37);
    private static final Insets INSETS_CHECKBOX = new Insets(0, 5, 0, 5);


    /***********************************************************************************************
     * Build the Channel Selectors on a panel.
     * Return a reference to the Decimation checkbox so we can change its state later.
     * This may be NULL if initialisation failed.
     *
     * @param selector
     * @param selectorpanel
     * @param hostchart
     * @param datasettype
     * @param xydatasetprimary
     * @param xydatasetsecondaries
     * @param metadatalist
     * @param debug
     *
     * @return JCheckBox
     */

    public static JCheckBox buildChannelSelectors(final ChannelSelectorUIComponentInterface selector,
                                                  final JPanel selectorpanel,
                                                  final ChartUIComponentPlugin hostchart,
                                                  final DatasetType datasettype,
                                                  final XYDataset xydatasetprimary,
                                                  final List<XYDataset> xydatasetsecondaries,
                                                  final List<Metadata> metadatalist,
                                                  final boolean debug)
        {
        final String SOURCE = "ChannelSelectorHelper.buildChannelSelectors() ";

        // TableLayout row and column size definitions
        final double[][] size =
            {
                { // Columns
                2,
                TableLayout.PREFERRED,
                2,
                TableLayout.PREFERRED,
                TableLayout.FILL
                },
                { // Rows
                1,
                TableLayout.PREFERRED,
                COMPONENT_SPACER,
                TableLayout.PREFERRED,
                1
                }
            };

        // TableLayout constraints for Channel panels
        // The horizontal justification is specified before the vertical justification
        final String[] constraints =
            { // Column, Row, Justification
             "1, 1, 1, 3, CENTER, CENTER",  // Channel Indicator
             "3, 1, LEFT, CENTER",          // Channel Name Label
             "3, 3, LEFT, CENTER"           // Channel Selection Mode combo box
            };

        final JCheckBox chkDecimate;
        final boolean boolDecimationDefault;

        MetadataHelper.showMetadataList(selector.getMetadata(),
                                        SOURCE,
                                        LOADER_PROPERTIES.isMetadataDebug());

        // Default the Decimation checkbox depending on the size of the Dataset
        // Do this here to ensure a final reference of the checkbox for the inner class
        boolDecimationDefault = isDecimationEnabled(hostchart, xydatasetprimary);

        chkDecimate = new JCheckBox(ChannelSelectorUIComponentInterface.LABEL_DECIMATE, boolDecimationDefault);
        chkDecimate.setForeground(UIComponentPlugin.DEFAULT_COLOUR_TEXT.getColor());
        chkDecimate.setBackground(UIComponentPlugin.DEFAULT_COLOUR_CANVAS.getColor());
        chkDecimate.setFont(UIComponentPlugin.DEFAULT_FONT.getFont());
        chkDecimate.setToolTipText(ChannelSelectorUIComponentInterface.TOOLTIP_DECIMATE);
        chkDecimate.setAlignmentX(Component.LEFT_ALIGNMENT);
        chkDecimate.setMargin(INSETS_CHECKBOX);

        // Ensure that the checkbox and the selector state start the same
        LOGGER.debug(debug,
                     SOURCE + "Set decimation default [default=" + boolDecimationDefault + "]");

        chkDecimate.setEnabled(boolDecimationDefault);
        selector.setDecimating(boolDecimationDefault);
        selector.debugSelector(debug, SOURCE);

        // Start with an empty panel regardless
        selectorpanel.removeAll();
        selectorpanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        selector.getChannelSelectionModes().clear();
        selector.getChannelMultipliers().clear();
        selector.getLabelsForMetadata().clear();

        if ((selector.getChannelCount() > 0)
            && (xydatasetprimary != null)
            && (datasettype != null))
            {
            final JPanel panelSundries;
            final JCheckBox chkAutorange;
            final JCheckBox chkLegend;
            final ItemListener listenerAutorange;
            final ItemListener listenerDecimation;
            final ItemListener listenerLegend;
            final JButton buttonReset;
//            final JButton buttonCrop;

            selectorpanel.setBorder(BorderFactory.createEmptyBorder(6, 2, 2, 2));

            //--------------------------------------------------------------------------------------
            // Place the sundries at the top, otherwise they get lost if there is a lot of channels

            panelSundries = new JPanel();
            panelSundries.setLayout(new BoxLayoutFixed(panelSundries, BoxLayoutFixed.Y_AXIS));
            panelSundries.setBackground(UIComponentPlugin.DEFAULT_COLOUR_CANVAS.getColor());
            panelSundries.setAlignmentX(Component.LEFT_ALIGNMENT);

            //--------------------------------------------------------------------------------------
            // Check that the Chart can handle both Lin and Log displays
            // otherwise there's no point in having the radio buttons
            // If so, add the Lin-Log radio buttons to the Sundries panel,
            // but only if there's a single Y-axis defined in the Metadata
            // Any more than that is far too complicated!

            if ((hostchart != null)
                && (hostchart.hasLinearMode())
                && (hostchart.hasLogarithmicMode())
                && (hasSingleYaxis(metadatalist)))
                {
                final JPanel panelLinLog;

                // Select Linear mode by default
                panelLinLog = createLinLogSelector(selector,
                                                   hostchart.hasLinearMode(),
                                                   debug);

                // Add to the Sundries panel
                panelSundries.add(panelLinLog);
                panelSundries.add(Box.createVerticalStrut(PANEL_SPACER));
                }

            //--------------------------------------------------------------------------------------
            // Add the Autorange checkbox to the Sundries panel
            // only if the Chart can support Autoranging

            if ((hostchart != null)
                && (hostchart.canAutorange()))
                {
                // Default the Autorange checkbox to checked
                selector.setAutoranging(hostchart.canAutorange());
                selector.debugSelector(debug, SOURCE);

                chkAutorange = new JCheckBox(ChannelSelectorUIComponentInterface.LABEL_AUTORANGE, selector.isAutoranging());
                chkAutorange.setForeground(UIComponentPlugin.DEFAULT_COLOUR_TEXT.getColor());
                chkAutorange.setBackground(UIComponentPlugin.DEFAULT_COLOUR_CANVAS.getColor());
                chkAutorange.setFont(UIComponentPlugin.DEFAULT_FONT.getFont());
                chkAutorange.setToolTipText(ChannelSelectorUIComponentInterface.TOOLTIP_AUTORANGE);
                chkAutorange.setAlignmentX(Component.LEFT_ALIGNMENT);
                chkAutorange.setMargin(INSETS_CHECKBOX);

                listenerAutorange = new ItemListener()
                    {
                    public void itemStateChanged(final ItemEvent event)
                        {
                        selector.setAutoranging(chkAutorange.isSelected());
                        selector.notifyChannelSelectionChangedEvent(selector, false);
                        selector.debugSelector(debug, SOURCE);
                        }
                    };
                chkAutorange.addItemListener(listenerAutorange);

                panelSundries.add(chkAutorange);
                panelSundries.add(Box.createVerticalStrut(PANEL_SPACER));
                }
            else
                {
                selector.setAutoranging(false);
                selector.debugSelector(debug, SOURCE);
                }

            //--------------------------------------------------------------------------------------
            // Add the Decimation checkbox to the Sundries panel
            // All Charts can decimate

            // Using setSelected will trigger ItemListener
            // See: http://geekycoder.wordpress.com/2008/07/28/tipjava-difference-between-actionlistener-and-itemlistener/

            listenerDecimation = new ItemListener()
                {
                public void itemStateChanged(final ItemEvent event)
                    {
                    final ItemListener listenerSelf;

                    // Required for call from inner class
                    listenerSelf = this;

                    if (event.getStateChange() == ItemEvent.DESELECTED)
                        {
                        // See if we really should be decimating
                        if (boolDecimationDefault)
                            {
                            // See: http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6988854
                            // See: http://www.java-forums.org/awt-swing/50031-why-does-check-box-untick-itself.html
                            // JOptionPane.showMessageDialog inside the itemStateChanged of JCheckBox fails
                            SwingUtilities.invokeLater(new Runnable()
                                {
                                public void run()
                                    {
                                    final int intChoice;
                                    final String [] strMessage =
                                        {
                                        "Removing decimation of a large dataset may cause performance problems",
                                        "Decimation does not affect the underlying dataset!",
                                        "Are you sure that you still wish to remove decimation?"
                                        };

                                    intChoice = JOptionPane.showOptionDialog(null,
                                                                             strMessage,
                                                                             TITLE_REMOVE_DECIMATION,
                                                                             JOptionPane.YES_NO_OPTION,
                                                                             JOptionPane.WARNING_MESSAGE,
                                                                             null,
                                                                             null,
                                                                             null);
                                    if (intChoice == JOptionPane.YES_OPTION)
                                        {
                                        // The User really wants to remove decimation
                                        // Ensure that the checkbox and the selector state stay the same!
                                        LOGGER.debug(debug,
                                                     SOURCE + "Turn off decimation");

                                        selector.setDecimating(false);
                                        selector.notifyChannelSelectionChangedEvent(selector, false);
                                        selector.debugSelector(debug, SOURCE);
                                        }
                                    else
                                        {
                                        // We must restore the selected state so that we continue to decimate...
                                        // Using setSelected() will trigger ItemListener, so we must remove the listener
                                        // ChannelSelectionChangedEvent is not required, since nothing changed
                                        restoreDecimationState(selector,
                                                               chkDecimate,
                                                               listenerSelf,
                                                               true,
                                                               debug);
                                        }
                                    }
                                });
                            }
                        else
                            {
                            // Ensure that the checkbox and the selector state stay the same!
                            LOGGER.debug(debug,
                                         SOURCE + "--> setDecimating() A [isselected=" + chkDecimate.isSelected() + "]");

                            selector.setDecimating(chkDecimate.isSelected());
                            selector.notifyChannelSelectionChangedEvent(selector, false);
                            selector.debugSelector(debug, SOURCE);
                            }
                        }
                    else if (event.getStateChange() == ItemEvent.SELECTED)
                        {
                        // Ensure that the checkbox and the selector state stay the same!
                        LOGGER.debug(debug,
                                     SOURCE + "--> setDecimating() B [isselected=" + chkDecimate.isSelected() + "]");

                        selector.setDecimating(chkDecimate.isSelected());
                        selector.notifyChannelSelectionChangedEvent(selector, false);
                        selector.debugSelector(debug, SOURCE);
                        }

                    LOGGER.debug(debug,
                                 SOURCE + "DECIMATE: at end of checkbox listener [isselected="
                                        + chkDecimate.isSelected() + "] [isdecimating="
                                        + selector.isDecimating() + "] Event=" + event.paramString());

                    LOGGER.debug(debug,
                                 SOURCE + "DECIMATE: at end of checkbox listener [isselected="
                                        + chkDecimate.isSelected() + "] [isdecimating="
                                        + selector.isDecimating() + "] Event=" + event.paramString());
                    }
                };
            chkDecimate.addItemListener(listenerDecimation);

            panelSundries.add(chkDecimate);
            panelSundries.add(Box.createVerticalStrut(PANEL_SPACER));

            //--------------------------------------------------------------------------------------
            // Add the Legend checkbox to the Sundries panel

            // Always start with no legend
            selector.setLegend(false);
            selector.debugSelector(debug, SOURCE);

            chkLegend = new JCheckBox(ChannelSelectorUIComponentInterface.LABEL_LEGEND, selector.hasLegend());
            chkLegend.setForeground(UIComponentPlugin.DEFAULT_COLOUR_TEXT.getColor());
            chkLegend.setBackground(UIComponentPlugin.DEFAULT_COLOUR_CANVAS.getColor());
            chkLegend.setFont(UIComponentPlugin.DEFAULT_FONT.getFont());
            chkLegend.setToolTipText(ChannelSelectorUIComponentInterface.TOOLTIP_LEGEND);
            chkLegend.setAlignmentX(Component.LEFT_ALIGNMENT);
            chkLegend.setMargin(INSETS_CHECKBOX);

            listenerLegend = new ItemListener()
                {
                public void itemStateChanged(final ItemEvent event)
                    {
                    selector.setLegend(chkLegend.isSelected());
                    selector.debugSelector(debug, SOURCE);
                    selector.notifyChannelSelectionChangedEvent(selector, false);
                    }
                };
            chkLegend.addItemListener(listenerLegend);

            panelSundries.add(chkLegend);
            panelSundries.add(Box.createVerticalStrut(PANEL_SPACER));
            panelSundries.add(Box.createVerticalStrut(PANEL_SPACER));

            //--------------------------------------------------------------------------------------
            // Add the Crop button to the Sundries panel

//            buttonCrop = new JButton(ChannelSelectorUIComponentInterface.BUTTON_CROP);
//            buttonCrop.setMaximumSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));
//            buttonCrop.setForeground(UIComponentPlugin.DEFAULT_COLOUR_TEXT.getColor());
//            buttonCrop.setBackground(UIComponentPlugin.DEFAULT_COLOUR_CANVAS.getColor());
//            buttonCrop.setFont(UIComponentPlugin.DEFAULT_FONT.getFont());
//            buttonCrop.setToolTipText(ChannelSelectorUIComponentInterface.TOOLTIP_CROP);
//            buttonCrop.setAlignmentX(Component.LEFT_ALIGNMENT);
//
//            buttonCrop.addActionListener(new ActionListener()
//                {
//                public void actionPerformed(final ActionEvent event)
//                    {
//                    final int intChoice;
//                    final String [] strMessage =
//                        {
//                        "AWAITING DEVELOPMENT!",
//                        "This action will permanently remove the RawData outside the selected range",
//                        "Are you sure that you wish to do this?",
//                        "AWAITING DEVELOPMENT!"
//                        };
//
//                    intChoice = JOptionPane.showOptionDialog(null,
//                                                             strMessage,
//                                                             TITLE_CROP_RAWDATA,
//                                                             JOptionPane.YES_NO_OPTION,
//                                                             JOptionPane.WARNING_MESSAGE,
//                                                             null,
//                                                             null,
//                                                             null);
//                    if (intChoice == JOptionPane.YES_OPTION)
//                        {
//                        selector.notifyChannelSelectionChangedEvent(selector, true);
//                        }
//                    }
//                });
//
//            panelSundries.add(buttonCrop);
//            panelSundries.add(Box.createVerticalStrut(PANEL_SPACER));
//            panelSundries.add(Box.createVerticalStrut(PANEL_SPACER));

            //--------------------------------------------------------------------------------------
            // Add the Reset All button to the Sundries panel

            buttonReset = new JButton(ChannelSelectorUIComponentInterface.BUTTON_RESET_ALL);
            buttonReset.setMaximumSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));
            buttonReset.setForeground(UIComponentPlugin.DEFAULT_COLOUR_TEXT.getColor());
            buttonReset.setBackground(UIComponentPlugin.DEFAULT_COLOUR_CANVAS.getColor());
            buttonReset.setFont(UIComponentPlugin.DEFAULT_FONT.getFont());
            buttonReset.setToolTipText(ChannelSelectorUIComponentInterface.TOOLTIP_RESET_ALL);
            buttonReset.setAlignmentX(Component.LEFT_ALIGNMENT);

            buttonReset.addActionListener(new ActionListener()
                {
                public void actionPerformed(final ActionEvent event)
                    {
                    final List<ChannelSelectorComboBox> listChannelMultipliers;

                    listChannelMultipliers = selector.getChannelMultipliers();

                    for (int intChannelIndex = 0;
                         intChannelIndex < listChannelMultipliers.size();
                         intChannelIndex++)
                        {
                        final ChannelSelectorComboBox comboMultiplier;

                        comboMultiplier = listChannelMultipliers.get(intChannelIndex);
                        // Make sure we fire only one event, on the *last* combo box
                        comboMultiplier.setEnableActionListener(intChannelIndex == (listChannelMultipliers.size()-1));
                        comboMultiplier.setSelectedItem(ChannelSelectionMode.X1);
                        }
                    }
                });

            panelSundries.add(buttonReset);
            panelSundries.add(Box.createVerticalStrut(PANEL_SPACER));
            panelSundries.add(Box.createVerticalStrut(PANEL_SPACER));

            selectorpanel.add(panelSundries);

            //--------------------------------------------------------------------------------------
            // Add one selector per channel, but only if asked to do so.
            // e.g. GPS Scatter does not require channel controls

            if (selector.showChannels())
                {
                for (int intChannelIndex = 0;
                          intChannelIndex < selector.getChannelCount();
                          intChannelIndex++)
                    {
                    final int intChannelSelected;
                    final JPanel panelChannel;
                    final JLabel labelIndicator;
                    final JLabel labelChannelName;
                    final ChannelSelectorComboBox comboChannelMode;
                    final ActionListener choiceListener;
                    final ChannelSelectionMode[] channelSelectionModes;
                    final ColourInterface colourChannel;
                    final String strChannelName;
                    final String strChannelDescription;
                    final Series seriesChannel;

                    // A final needed for an inner class
                    intChannelSelected = intChannelIndex;

                    panelChannel = new JPanel();
                    panelChannel.setLayout(new TableLayout(size));
                    panelChannel.setBackground(UIComponentPlugin.DEFAULT_COLOUR_CANVAS.getColor());
                    panelChannel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(),
                                                                              BorderFactory.createEmptyBorder(2, 2, 2, 2)));
                    panelChannel.setAlignmentX(Component.LEFT_ALIGNMENT);

                    //----------------------------------------------------------------------------------
                    // Add the Channel Indicator

                    labelIndicator = new JLabel();
                    labelIndicator.setPreferredSize(DIM_CHANNEL_INDICATOR);
                    labelIndicator.setHorizontalAlignment(SwingConstants.LEFT);
                    labelIndicator.setOpaque(true);

                    panelChannel.add(labelIndicator,
                                     constraints[0]);
                    selector.getLabelsForMetadata().add(labelIndicator);

                    colourChannel = MetadataHelper.getChannelColour(selector.getMetadata(),
                                                                    intChannelSelected,
                                                                    selector.hasTemperatureChannel());
                    if (colourChannel != null)
                        {
                        labelIndicator.setBackground(colourChannel.getColor());
                        labelIndicator.setToolTipText(EMPTY_STRING);
                        }
                    else
                        {
                        labelIndicator.setBackground(Color.gray);
                        labelIndicator.setToolTipText(ChannelSelectorUIComponentInterface.TOOLTIP_COLOUR_NOT_FOUND);
                        }

                    //----------------------------------------------------------------------------------
                    // Channel Name label

                    labelChannelName = new JLabel();
                    labelChannelName.setForeground(UIComponentPlugin.DEFAULT_COLOUR_TEXT.getColor());
                    labelChannelName.setFont(UIComponentPlugin.DEFAULT_FONT.getFont());
                    labelChannelName.setHorizontalAlignment(SwingConstants.LEFT);
                    labelChannelName.setHorizontalTextPosition(SwingConstants.LEFT);

                    strChannelName = MetadataHelper.getChannelName(selector.getMetadata(),
                                                                   intChannelSelected,
                                                                   selector.hasTemperatureChannel());
                    strChannelDescription = MetadataHelper.getChannelDescription(selector.getMetadata(),
                                                                                 intChannelSelected,
                                                                                 selector.hasTemperatureChannel());
                    // Italicise the name if for some reason it is missing
                    if ((strChannelName == null)
                        || (FrameworkStrings.EMPTY_STRING.equals(strChannelName.trim())))
                        {
                        final String strChannelNameItalic;

                        strChannelNameItalic = FrameworkStrings.HTML_PREFIX_ITALIC
                                                   + "Channel "
                                                   + intChannelSelected
                                                   + FrameworkStrings.HTML_SUFFIX_ITALIC;
                        labelChannelName.setText(strChannelNameItalic);
                        }
                    else
                        {
                        labelChannelName.setText(strChannelName);
                        }

                    // Italicise the description if for some reason it is missing
                    if ((strChannelDescription == null)
                        || (FrameworkStrings.EMPTY_STRING.equals(strChannelDescription.trim())))
                        {
                        final String strChannelDescriptionItalic;

                        strChannelDescriptionItalic = FrameworkStrings.HTML_PREFIX_ITALIC
                                                          + ChannelSelectorUIComponentInterface.TOOLTIP_DESCRIPTION_NOT_FOUND
                                                          + FrameworkStrings.HTML_SUFFIX_ITALIC;
                        labelChannelName.setToolTipText(strChannelDescriptionItalic);
                        }
                    else
                        {
                        labelChannelName.setToolTipText(strChannelDescription);
                        }

                    panelChannel.add(labelChannelName,
                                     constraints[1]);
                    selector.getLabelsForMetadata().add(labelChannelName);

                    //----------------------------------------------------------------------------------
                    // Change the name of the Series Keys, which appear on the chart legend

                    seriesChannel = ChartHelper.getSeriesForIndex(datasettype,
                                                                  xydatasetprimary,
                                                                  xydatasetsecondaries,
                                                                  intChannelSelected,
                                                                  debug);
                    try
                        {
                        if (seriesChannel != null)
                            {
                            LOGGER.debug(debug,
                                         SOURCE + "Set Series Key [index=" + intChannelSelected + "] [key=" + strChannelName + "]");
                            seriesChannel.setKey(strChannelName);
                            }
                        else
                            {
                            LOGGER.error(SOURCE + "Invalid Series [index=" + intChannelSelected + "]");
                            }
                        }

                    catch (final UnknownKeyException exception)
                        {
                        LOGGER.error(SOURCE + "Invalid Series Key [key=" + strChannelName + "]");
                        exception.printStackTrace();
                        }

                    //----------------------------------------------------------------------------------
                    // Add the selection mode drop-down

                    comboChannelMode = new ChannelSelectorComboBox();
                    comboChannelMode.setFont(UIComponentPlugin.DEFAULT_FONT.getFont());
                    comboChannelMode.setForeground(UIComponentPlugin.DEFAULT_COLOUR_TEXT.getColor());
                    comboChannelMode.setRenderer(new AlignedListCellRenderer(SwingConstants.LEFT,
                                                                             UIComponentPlugin.DEFAULT_FONT,
                                                                             UIComponentPlugin.DEFAULT_COLOUR_TEXT,
                                                                             null));
                    selector.getChannelMultipliers().add(comboChannelMode);
                    comboChannelMode.setAlignmentX(0);

                    comboChannelMode.setToolTipText(ChannelSelectorUIComponentInterface.TOOLTIP_SELECTION_MODE);
                    comboChannelMode.setEnabled(true);
                    comboChannelMode.setEditable(false);
                    comboChannelMode.setMaximumRowCount(ChannelSelectionMode.values().length);

                    // Start with Events enabled
                    comboChannelMode.setEnableActionListener(true);

                    // Now add all items for the drop-down
                    channelSelectionModes = ChannelSelectionMode.values();

                    for (int j = 0;
                         j < channelSelectionModes.length;
                         j++)
                        {
                        comboChannelMode.addItem(channelSelectionModes[j]);
                        }

                    comboChannelMode.setSelectedItem(ChannelSelectionMode.X1);
                    comboChannelMode.revalidate();

                    // Record the current selection of each channel
                    selector.getChannelSelectionModes().add((ChannelSelectionMode)comboChannelMode.getSelectedItem());

                    // Add a ChannelSelectionChanged Listener for each drop-down
                    choiceListener = new ActionListener()
                    {
                    public void actionPerformed(final ActionEvent event)
                        {
                        // Record the new selection of each channel
                        selector.getChannelSelectionModes().set(intChannelSelected,
                                                                (ChannelSelectionMode) comboChannelMode.getSelectedItem());

                        // See if we must tell the World
                        if (comboChannelMode.isEnableActionListener())
                            {
                            selector.notifyChannelSelectionChangedEvent(selector, false);
                            }

                        // Always leave Events enabled
                        comboChannelMode.setEnableActionListener(true);
                        }
                    };

                    comboChannelMode.addActionListener(choiceListener);
                    comboChannelMode.setAlignmentX(Component.LEFT_ALIGNMENT);

                    panelChannel.add(comboChannelMode,
                                     constraints[2]);

                    selectorpanel.add(panelChannel);
                    selectorpanel.add(Box.createVerticalStrut(PANEL_SPACER));
                    }
                }
            else
                {
                LOGGER.debug(debug,
                             SOURCE + "Host Chart does not require ChannelSelector updates");
                }
            }
        else
            {
            LOGGER.debug(debug,
                         SOURCE + "ChannelSelector was not built [channelcount.raw=" + selector.getChannelCount() + "]");
            }

        selectorpanel.add(Box.createVerticalGlue());

        return (chkDecimate);
        }


    /***********************************************************************************************
     * Update the ChannelSelector labels from the specified Metadata List.
     *
     * @param selector
     * @param metadatalist
     * @param debug
     */

    public static void updateChannelSelectorMetadata(final ChannelSelectorUIComponent selector,
                                                     final List<Metadata> metadatalist,
                                                     final boolean debug)
        {
        final String SOURCE = "ChannelSelectorHelper.updateChannelSelectorMetadata() ";

        if ((selector.showChannels())
            && (metadatalist != null)
            && (selector.getLabelsForMetadata() != null)
            && (selector.getLabelsForMetadata().size() == (selector.getChannelCount() << 1)))
            {
            MetadataHelper.showMetadataList(metadatalist,
                                            SOURCE,
                                            debug);

            for (int intChannelIndex = 0;
                 intChannelIndex < selector.getChannelCount();
                 intChannelIndex++)
                {
                final JLabel labelIndicator;
                final JLabel labelChannelName;
                final ColourInterface colourChannel;
                final String strChannelName;
                final String strChannelDescription;

                labelIndicator = selector.getLabelsForMetadata().get(intChannelIndex << 1);
                labelChannelName = selector.getLabelsForMetadata().get((intChannelIndex << 1) + 1);

                // Update ChannelColour
                colourChannel = MetadataHelper.getChannelColour(metadatalist,
                                                                intChannelIndex,
                                                                selector.hasTemperatureChannel());
                if (colourChannel != null)
                    {
                    labelIndicator.setBackground(colourChannel.getColor());
                    labelIndicator.setToolTipText(EMPTY_STRING);
                    }
                else
                    {
                    labelIndicator.setBackground(Color.gray);
                    labelIndicator.setToolTipText(ChannelSelectorUIComponentInterface.TOOLTIP_COLOUR_NOT_FOUND);
                    }

                // Update ChannelName, ChannelDescription
                // Get the channel names from the Metadata

                strChannelName = MetadataHelper.getChannelName(metadatalist,
                                                               intChannelIndex,
                                                               selector.hasTemperatureChannel());
                strChannelDescription = MetadataHelper.getChannelDescription(metadatalist,
                                                                             intChannelIndex,
                                                                             selector.hasTemperatureChannel());
                // Italicise the name if for some reason it is missing
                if ((strChannelName != null)
                    && (!FrameworkStrings.EMPTY_STRING.equals(strChannelName.trim())))
                    {
                    labelChannelName.setText(strChannelName);
                    }
                else
                    {
                    final String strChannelNameItalic;

                    strChannelNameItalic = FrameworkStrings.HTML_PREFIX_ITALIC
                                               + "Channel "
                                               + intChannelIndex
                                               + FrameworkStrings.HTML_SUFFIX_ITALIC;
                    labelChannelName.setText(strChannelNameItalic);
                    }

                // Italicise the description if for some reason it is missing
                if ((strChannelDescription != null)
                    && (!FrameworkStrings.EMPTY_STRING.equals(strChannelDescription.trim())))
                    {
                    labelChannelName.setToolTipText(strChannelDescription);
                    }
                else
                    {
                    final String strChannelDescriptionItalic;

                    strChannelDescriptionItalic = FrameworkStrings.HTML_PREFIX_ITALIC
                                                    + ChannelSelectorUIComponentInterface.TOOLTIP_DESCRIPTION_NOT_FOUND
                                                    + FrameworkStrings.HTML_SUFFIX_ITALIC;
                    labelChannelName.setToolTipText(strChannelDescriptionItalic);
                    }
                }
            }
        }


    /***********************************************************************************************
     * Create the radio buttons to select between Linear or Log mode.
     *
     * @param selector
     * @param lineardefault
     * @param debug
     *
     * @return JPanel
     */

    private static JPanel createLinLogSelector(final ChannelSelectorUIComponentInterface selector,
                                               final boolean lineardefault,
                                               final boolean debug)
        {
        final String SOURCE = "ChannelSelectorHelper.createLinLogSelector() ";
        final JPanel panelLinLog;
        final ButtonGroup buttonGroup;
        final JRadioButton buttonLinear;
        final JRadioButton buttonLog;
        final ActionListener listenerLinear;
        final ActionListener listenerLog;

        // Set the default selection to the supplied state
        selector.setLinearMode(lineardefault);
        selector.debugSelector(debug, SOURCE);

        panelLinLog = new JPanel();
        panelLinLog.setLayout(new BoxLayoutFixed(panelLinLog, BoxLayoutFixed.Y_AXIS));
        panelLinLog.setBackground(UIComponentPlugin.DEFAULT_COLOUR_CANVAS.getColor());
        panelLinLog.setBorder(BorderFactory.createEmptyBorder(0, 5, 2, 2));
        panelLinLog.setAlignmentX(Component.LEFT_ALIGNMENT);

        buttonGroup = new ButtonGroup();

        // Set up the Linear button
        buttonLinear = new JRadioButton(BUTTON_LINEAR);
        buttonLinear.setHorizontalTextPosition(SwingConstants.RIGHT);
        buttonLinear.setToolTipText(TOOLTIP_LINEAR);
        buttonLinear.setBackground(UIComponentPlugin.DEFAULT_COLOUR_CANVAS.getColor());
        buttonLinear.setFont(UIComponentPlugin.DEFAULT_FONT.getFont());
        buttonLinear.setForeground(UIComponentPlugin.DEFAULT_COLOUR_TEXT.getColor());

        listenerLinear = new ActionListener()
            {
            public void actionPerformed(final ActionEvent event)
                {
                selector.setLinearMode(buttonLinear.isSelected());
                selector.notifyChannelSelectionChangedEvent(selector, false);
                selector.debugSelector(debug, SOURCE);
                }
            };

        buttonLinear.addActionListener(listenerLinear);
        buttonGroup.add(buttonLinear);
        buttonGroup.setSelected(buttonLinear.getModel(), lineardefault);

        // Set up the Log button
        buttonLog = new JRadioButton(BUTTON_LOG);
        buttonLog.setHorizontalTextPosition(SwingConstants.RIGHT);
        buttonLog.setToolTipText(TOOLTIP_LOGARITHMIC);
        buttonLog.setBackground(UIComponentPlugin.DEFAULT_COLOUR_CANVAS.getColor());
        buttonLog.setFont(UIComponentPlugin.DEFAULT_FONT.getFont());
        buttonLog.setForeground(UIComponentPlugin.DEFAULT_COLOUR_TEXT.getColor());

        listenerLog = new ActionListener()
            {
            public void actionPerformed(final ActionEvent event)
                {
                selector.setLinearMode(!buttonLog.isSelected());
                selector.notifyChannelSelectionChangedEvent(selector, false);
                selector.debugSelector(debug, SOURCE);
                }
            };

        buttonLog.addActionListener(listenerLog);
        buttonGroup.add(buttonLog);
        buttonGroup.setSelected(buttonLog.getModel(), !lineardefault);

        panelLinLog.add(buttonLinear);
        //panelLinLog.add(Box.createVerticalStrut(RADIO_SPACER));
        panelLinLog.add(buttonLog);

        return (panelLinLog);
        }


    /***********************************************************************************************
     * Test to see if decimation should be enabled.
     * Check against changes in the XYDataset contents.
     *
     * @param hostchart
     * @param xydatasetprimary
     *
     * @return boolean
     */

    public static boolean isDecimationEnabled(final ChartUIComponentPlugin hostchart,
                                              final XYDataset xydatasetprimary)
        {
        final boolean boolDecimationEnabled;

        boolDecimationEnabled = (xydatasetprimary != null)
                                    && (hostchart != null)
                                    && ((xydatasetprimary.getSeriesCount() > 0)
                                    && (xydatasetprimary.getItemCount(0) > hostchart.getDisplayLimit()));

        return (boolDecimationEnabled);
        }


    /***********************************************************************************************
     * Test to see if decimation should be enabled.
     * Check against changes notified by the DatasetChangedEvent.
     *
     * @param hostchart
     * @param event
     *
     * @return boolean
     */

    public static boolean isDecimationEnabled(final ChartUIComponentPlugin hostchart,
                                              final DatasetChangedEvent event)
        {
        final boolean boolDecimationEnabled;

        boolDecimationEnabled = ((event != null)
                                     && (event.getSeriesCount() > 0)
                                     && (event.getItemCountSeries0() > 0)
                                     && (hostchart != null)
                                     && (event.getItemCountSeries0() > hostchart.getDisplayLimit()));

        return (boolDecimationEnabled);
        }


    /***********************************************************************************************
     * Restore the state of the Decimation checkbox.
     * Necessary because setSelected() will trigger an unwanted ItemEvent.
     *
     * @param selector
     * @param checkbox
     * @param listener
     * @param state
     * @param debug
     */

    private static void restoreDecimationState(final ChannelSelectorUIComponentInterface selector,
                                               final JCheckBox checkbox,
                                               final ItemListener listener,
                                               final boolean state,
                                               final boolean debug)
        {
        final String SOURCE = "ChannelSelectorHelper.restoreDecimationState() ";

        LOGGER.debug(debug,
                     SOURCE + "--> setDecimating() [state=" + state + "]");

        checkbox.removeItemListener(listener);
        checkbox.setSelected(state);
        selector.setDecimating(state);
        checkbox.addItemListener(listener);

        selector.debugSelector(debug, SOURCE);
        }


    /***********************************************************************************************
     * Test to see if the Metadata specifies a single Y axis or more.
     * Look for (Observation.Axis.Label.Y.0 AND NOT Observation.Axis.Label.Y.1).
     *
     * @param metadatalist
     *
     * @return boolean
     */

    private static boolean hasSingleYaxis(final List<Metadata> metadatalist)
        {
        // Add the Lin-Log radio buttons to the Sundries panel,
        // but only if there's a single Y-axis defined in the Metadata
        // Any more than that is far too complicated!
        // So we do NOT expect to find Observation.Axis.Label.Y.1

        return ((MetadataHelper.getMetadataByKey(metadatalist,
                                                 MetadataDictionary.KEY_OBSERVATION_AXIS_LABEL_Y.getKey() + MetadataDictionary.SUFFIX_SERIES_ZERO) != null)
                && (MetadataHelper.getMetadataByKey(metadatalist,
                                                    MetadataDictionary.KEY_OBSERVATION_AXIS_LABEL_Y.getKey() + MetadataDictionary.SUFFIX_SERIES_ONE) == null));
        }
    }
