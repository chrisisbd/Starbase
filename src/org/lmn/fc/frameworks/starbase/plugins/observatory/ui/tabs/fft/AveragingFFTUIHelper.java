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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.fft;


import info.clearthought.layout.TableLayout;
import org.lmn.fc.common.actions.ContextAction;
import org.lmn.fc.common.constants.*;
import org.lmn.fc.common.utilities.misc.Utilities;
import org.lmn.fc.common.utilities.printing.PrintUtilities;
import org.lmn.fc.common.utilities.threads.SwingWorker;
import org.lmn.fc.common.utilities.ui.AlignedListCellRenderer;
import org.lmn.fc.frameworks.starbase.plugins.observatory.MetadataItemState;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ObservatoryInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.events.CommitChangeEvent;
import org.lmn.fc.frameworks.starbase.plugins.observatory.events.CommitChangeListener;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryUIInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.commands.AwaitingDevelopment;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.MetadataHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.AveragingFFTUIComponentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.MetadataNVPViewerUIComponentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.commands.CommandProcessorUtilities;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.metadata.MetadataNVPViewerUIComponent;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.DataTypeDictionary;
import org.lmn.fc.model.datatypes.DecimalFormatPattern;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.model.registry.RegistryModelUtilities;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;
import org.lmn.fc.model.xmlbeans.metadata.SchemaUnits;
import org.lmn.fc.ui.UIComponentPlugin;
import org.lmn.fc.ui.choosers.ChooserInterface;
import org.lmn.fc.ui.layout.BoxLayoutFixed;
import org.lmn.fc.ui.reports.ReportTableHelper;
import org.lmn.fc.ui.reports.ReportTablePlugin;
import org.lmn.fc.ui.widgets.ControlKnobInterface;
import org.lmn.fc.ui.widgets.IndicatorInterface;
import org.lmn.fc.ui.widgets.impl.CoarseFineKnobs;
import org.lmn.fc.ui.widgets.impl.ToolbarIndicator;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.print.PageFormat;
import java.awt.print.PrinterJob;
import java.io.File;
import java.util.ArrayList;
import java.util.List;


/***************************************************************************************************
 * AveragingFFTUIHelper.
 */

public final class AveragingFFTUIHelper implements FrameworkConstants,
                                                   FrameworkStrings,
                                                   FrameworkMetadata,
                                                   FrameworkRegex,
                                                   FrameworkSingletons,
                                                   ResourceKeys
    {
    // String Resources
    private static final String BUTTON_HELP = "Help";
    private static final String BUTTON_START = "Start";
    private static final String BUTTON_PAUSE = "Pause";
    private static final String BUTTON_STOP = "Stop";
    private static final String TOOLTIP_AMPLITUDE = "Amplitude Offset";
    private static final String TOOLTIP_COARSE_AMPLITUDE = "Rotate to vary the amplitude offset correction";
    private static final String TOOLTIP_COARSE_PHASE = "Rotate to vary the phase offset correction";
    private static final String TOOLTIP_FINE_AMPLITUDE = "Rotate to fine tune the amplitude offset correction)";
    private static final String TOOLTIP_FINE_PHASE = "Rotate to fine tune the phase offset correction";
    private static final String TOOLTIP_HELP = "Show Help for the FFT Averager";
    private static final String TOOLTIP_PHASE = "Phase Offset";
    private static final String TOOLTIP_START = "Start the FFT Averager";
    private static final String TOOLTIP_PAUSE = "Pause the FFT Averager";
    private static final String TOOLTIP_STOP = "Stop the FFT Averager";

    public static final DecimalFormatPattern PATTERN_AMPLITUDE = DecimalFormatPattern.PHASE;
    public static final DecimalFormatPattern PATTERN_PHASE = DecimalFormatPattern.PHASE;
    public static final double AMPLITUDE_MIN = 0.01;
    public static final double AMPLITUDE_MAX = 10.0;
    public static final double PHASE_MIN = -1.0;
    public static final double PHASE_MAX = 1.0;

    private static final Dimension DIM_COMBO = new Dimension(140, 20);

    private static final Dimension DIM_INDICATOR_AMPLITUDE = new Dimension(70, 24);
    private static final Dimension DIM_CONTROL_KNOB_AMPLITUDE = new Dimension(28, 28);

    private static final Dimension DIM_INDICATOR_PHASE = new Dimension(70, 24);
    private static final Dimension DIM_CONTROL_KNOB_PHASE = new Dimension(28, 28);

    private static final int TICKLENGTH_AMPLITUDE = 2;
    private static final int TICKLENGTH_PHASE = 2;


    /***********************************************************************************************
     * Create the Control button panel.
     *
     * @param hostframeui
     * @param fontdata
     * @param foregroundcolour
     * @param backgroundcolour
     * @param metadatalist
     * @param resourcekey
     *
     * @return JPanel
     */

    public static JPanel createControlButtons(final AveragingFFTFrameUIComponentInterface hostframeui,
                                              final FontInterface fontdata,
                                              final ColourInterface foregroundcolour,
                                              final ColourInterface backgroundcolour,
                                              final List<Metadata> metadatalist,
                                              final String resourcekey)
        {
        final String SOURCE = "AveragingFFTUIHelper.createControlButtons() ";
        final JProgressBar progressBar;
        final JPanel panelButtons;
        final JButton buttonControl;
        final JButton buttonHelp;
        final JPanel panelControl;

        progressBar = new JProgressBar(0, 100);
        progressBar.setMinimumSize(new Dimension(50, CommandProcessorUtilities.HEIGHT_BUTTON));
        progressBar.setPreferredSize(new Dimension(120, CommandProcessorUtilities.HEIGHT_BUTTON));
        progressBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, CommandProcessorUtilities.HEIGHT_BUTTON));
        progressBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        progressBar.setForeground(new Color(15, 244, 28));
        progressBar.setBackground(new Color(255, 204, 204));
        progressBar.setValue(0);

        // The Toolbar has been created previously
        hostframeui.getToolbar().setProgressBar(progressBar);

        buttonControl = new JButton(BUTTON_START);
        buttonControl.setMinimumSize(new Dimension(25, CommandProcessorUtilities.HEIGHT_BUTTON));
        buttonControl.setPreferredSize(new Dimension(60, CommandProcessorUtilities.HEIGHT_BUTTON));
        buttonControl.setMaximumSize(new Dimension(Integer.MAX_VALUE, CommandProcessorUtilities.HEIGHT_BUTTON));
        buttonControl.setAlignmentY(Component.CENTER_ALIGNMENT);
        buttonControl.setHideActionText(true);
        buttonControl.setToolTipText(TOOLTIP_START);
        buttonControl.setFont(fontdata.getFont());
        buttonControl.setForeground(foregroundcolour.getColor());
        buttonControl.setBackground(backgroundcolour.getColor());
        buttonControl.setEnabled(true);

        buttonControl.addActionListener(new ActionListener()
            {
            public void actionPerformed(final ActionEvent event)
                {
                final ObservatoryInstrumentDAOInterface dao;

                dao = hostframeui.getDAO();

                if (dao != null)
                    {
                    ((AveragingFFTDAOInterface)dao).processData(hostframeui,
                                                                hostframeui.getToolbar().getProgressBar(),
                                                                !((AveragingFFTDAOInterface) dao).isRunning());

                    if (((AveragingFFTDAOInterface) dao).isRunning())
                        {
                        buttonControl.setText(BUTTON_STOP);
                        buttonControl.setToolTipText(TOOLTIP_STOP);
                        }
                    else
                        {
                        buttonControl.setText(BUTTON_START);
                        buttonControl.setToolTipText(TOOLTIP_START);
                        }
                    }
                }
            });

        buttonHelp = new JButton(BUTTON_HELP);
        buttonHelp.setMinimumSize(new Dimension(25, CommandProcessorUtilities.HEIGHT_BUTTON));
        buttonHelp.setPreferredSize(new Dimension(60, CommandProcessorUtilities.HEIGHT_BUTTON));
        buttonHelp.setMaximumSize(new Dimension(Integer.MAX_VALUE, CommandProcessorUtilities.HEIGHT_BUTTON));
        buttonHelp.setAlignmentY(Component.CENTER_ALIGNMENT);
        buttonHelp.setHideActionText(true);
        buttonHelp.setToolTipText(TOOLTIP_HELP);
        buttonHelp.setFont(fontdata.getFont());
        buttonHelp.setForeground(foregroundcolour.getColor());
        buttonHelp.setBackground(backgroundcolour.getColor());
        buttonHelp.setEnabled(true);

        buttonHelp.addActionListener(new ActionListener()
            {
            public void actionPerformed(final ActionEvent event)
                {
                System.out.println("HELP!!!!!!!!!!!!!!!!!");
                }
            });

        panelButtons = new JPanel();
        // In general, all the components controlled by a top-to-bottom BoxLayout object should have the same X alignment.
        // Similarly, all the components controlled by a left-to-right Boxlayout should generally have the same Y alignment.
        panelButtons.setLayout(new BoxLayoutFixed(panelButtons, BoxLayoutFixed.X_AXIS));
        panelButtons.setBackground(backgroundcolour.getColor());
        panelButtons.setAlignmentX(Component.LEFT_ALIGNMENT);

        panelButtons.add(buttonControl);
        panelButtons.add(Box.createHorizontalStrut(5));
        panelButtons.add(buttonHelp);

        panelControl = new JPanel();
        panelControl.setBorder(UIComponentPlugin.BORDER_SIDEBAR_ITEM);
        // In general, all the components controlled by a top-to-bottom BoxLayout object should have the same X alignment.
        // Similarly, all the components controlled by a left-to-right Boxlayout should generally have the same Y alignment.
        panelControl.setLayout(new BoxLayoutFixed(panelControl, BoxLayoutFixed.Y_AXIS));
        panelControl.setBackground(backgroundcolour.getColor());
        panelControl.setAlignmentX(Component.LEFT_ALIGNMENT);

        panelControl.add(progressBar);
        panelControl.add(Box.createVerticalStrut(10));
        panelControl.add(panelButtons);

        return (panelControl);
        }


    /***********************************************************************************************
     * Create a drop-down of SampleRates.
     *
     * @param consumer
     * @param metadatakey
     * @param font
     * @param foreground
     * @param background
     *
     * @return JPanel
     */

    public static JPanel createSampleRateSelector(final AveragingFFTSidebarInterface consumer,
                                                  final String metadatakey,
                                                  final FontInterface font,
                                                  final ColourInterface foreground,
                                                  final ColourInterface background)
        {
        final String SOURCE = "AveragingFFTUIHelper.createSampleRateSelector() ";
        final ActionListener choiceListener;
        final JPanel panelSampleRate;
        final JLabel labelSampleRate;
        final JComboBox comboSampleRate;

        labelSampleRate = new JLabel("Sample Rate");
        labelSampleRate.setFont(font.getFont());
        labelSampleRate.setForeground(foreground.getColor());

        comboSampleRate = new JComboBox();
        comboSampleRate.setFont(font.getFont());
        comboSampleRate.setForeground(foreground.getColor());
        comboSampleRate.setBackground(background.getColor());
        comboSampleRate.setRenderer(new AlignedListCellRenderer(SwingConstants.LEFT,
                                                                font,
                                                                foreground,
                                                                background,
                                                                "Sample Rate kHz"));

        // Do NOT allow the combo box to take up all the remaining space!
        comboSampleRate.setPreferredSize(DIM_COMBO);
        comboSampleRate.setMaximumSize(DIM_COMBO);
        comboSampleRate.setAlignmentX(0);

        comboSampleRate.setToolTipText("Sample Rate kHz");
        comboSampleRate.setEnabled(true);
        comboSampleRate.setEditable(false);

        // Load up the ComboBox
        // Add the enum Object, not just the name
        comboSampleRate.addItem(1024);
        comboSampleRate.addItem(2048);

        // Beware that there might not have been any valid SampleRates
        if ((comboSampleRate.getItemCount() > 0)
            && (consumer.getMetadataList() != null))
            {
            final Metadata metadata;

            metadata = MetadataHelper.getMetadataByKey(consumer.getMetadataList(),
                                                       metadatakey);
            if (metadata != null)
                {
                try
                    {
                    // Set the default ComboBox selection to the supplied state
                    comboSampleRate.setSelectedItem(Integer.parseInt(metadata.getValue()));
                    }

                catch (final NumberFormatException exception)
                    {
                    comboSampleRate.setSelectedItem(2048);
                    }
                }
            else
                {
                comboSampleRate.setSelectedItem(2048);
                }

            comboSampleRate.revalidate();
            }

        choiceListener = new ActionListener()
            {
            public void actionPerformed(final ActionEvent event)
                {
                final Metadata metadata;

                // Update the Metadata to show the new ComboBox election
                metadata = MetadataHelper.getMetadataByKey(consumer.getMetadataList(),
                                                           metadatakey);
                if (metadata != null)
                    {
                    // NumberFormatException should not be possible
                    metadata.setValue(comboSampleRate.getSelectedItem().toString());

                    consumer.notifyMetadataChangedEvent(this,
                                                        metadatakey,
                                                        metadata.getValue(),
                                                        MetadataItemState.EDIT);
                    }
                }
            };

        comboSampleRate.addActionListener(choiceListener);

        panelSampleRate = new JPanel();
        panelSampleRate.setBorder(UIComponentPlugin.BORDER_SIDEBAR_ITEM);

        // In general, all the components controlled by a top-to-bottom BoxLayout object should have the same X alignment.
        // Similarly, all the components controlled by a left-to-right Boxlayout should generally have the same Y alignment.
        panelSampleRate.setLayout(new BoxLayoutFixed(panelSampleRate, BoxLayoutFixed.X_AXIS));

        panelSampleRate.setBackground(background.getColor());
        panelSampleRate.setAlignmentX(Component.LEFT_ALIGNMENT);

        panelSampleRate.add(labelSampleRate);
        panelSampleRate.add(Box.createHorizontalGlue());
        panelSampleRate.add(comboSampleRate);

        return (panelSampleRate);
        }


    /***********************************************************************************************
     * Create a drop-down of FFTLengths.
     *
     * @param consumer
     * @param metadatakey
     * @param font
     * @param foreground
     * @param background
     *
     * @return JPanel
     */

    public static JPanel createFFTLengthSelector(final AveragingFFTSidebarInterface consumer,
                                                 final String metadatakey,
                                                 final FontInterface font,
                                                 final ColourInterface foreground,
                                                 final ColourInterface background)
        {
        final String SOURCE = "AveragingFFTUIHelper.createFFTLengthSelector() ";
        final FFTLength[] arrayFFTLengths;
        final ActionListener choiceListener;
        final JPanel panelFFTLength;
        final JLabel labelFFTLength;
        final JComboBox comboFFTLength;

        labelFFTLength = new JLabel("FFT Length");
        labelFFTLength.setFont(font.getFont());
        labelFFTLength.setForeground(foreground.getColor());

        comboFFTLength = new JComboBox();
        comboFFTLength.setFont(font.getFont());
        comboFFTLength.setForeground(foreground.getColor());
        comboFFTLength.setBackground(background.getColor());
        comboFFTLength.setRenderer(new AlignedListCellRenderer(SwingConstants.LEFT,
                                                               font,
                                                               foreground,
                                                               background,
                                                               FFTLength.TOOLTIP));

        // Do NOT allow the combo box to take up all the remaining space!
        comboFFTLength.setPreferredSize(DIM_COMBO);
        comboFFTLength.setMaximumSize(DIM_COMBO);
        comboFFTLength.setAlignmentX(0);

        comboFFTLength.setToolTipText(FFTLength.TOOLTIP);
        comboFFTLength.setEnabled(true);
        comboFFTLength.setEditable(false);

        arrayFFTLengths = FFTLength.values();

        // Load up the ComboBox
        for (int intLengthIndex = 0;
             intLengthIndex < arrayFFTLengths.length;
             intLengthIndex++)
            {
            final FFTLength length;

            length = arrayFFTLengths[intLengthIndex];

            // Add the enum Object, not just the name
            comboFFTLength.addItem(length);
            }

        // Beware that there might not have been any valid FFTLengths
        if ((comboFFTLength.getItemCount() > 0)
            && (consumer.getMetadataList() != null))
            {
            final Metadata metadata;

            metadata = MetadataHelper.getMetadataByKey(consumer.getMetadataList(),
                                                       metadatakey);
            if (metadata != null)
                {
                // Set the default ComboBox selection to the supplied state
                if (FFTLength.getFFTLengthForName(metadata.getValue()) != null)
                    {
                    comboFFTLength.setSelectedItem(FFTLength.getFFTLengthForName(metadata.getValue()));
                    }
                else
                    {
                    comboFFTLength.setSelectedItem(FFTLength.FFT_32);
                    }
                }
            else
                {
                comboFFTLength.setSelectedItem(FFTLength.FFT_32);
                }

            comboFFTLength.revalidate();
            }

        choiceListener = new ActionListener()
            {
            public void actionPerformed(final ActionEvent event)
                {
                final Metadata metadata;

                // Update the Metadata to show the new ComboBox election
                metadata = MetadataHelper.getMetadataByKey(consumer.getMetadataList(),
                                                           metadatakey);
                if (metadata != null)
                    {
                    // NumberFormatException should not be possible
                    metadata.setValue(Integer.toString(((FFTLength) comboFFTLength.getSelectedItem()).getLength()));

                    consumer.notifyMetadataChangedEvent(this,
                                                        metadatakey,
                                                        metadata.getValue(),
                                                        MetadataItemState.EDIT);
                    }
                }
            };

        comboFFTLength.addActionListener(choiceListener);

        panelFFTLength = new JPanel();
        panelFFTLength.setBorder(UIComponentPlugin.BORDER_SIDEBAR_ITEM);

        // In general, all the components controlled by a top-to-bottom BoxLayout object should have the same X alignment.
        // Similarly, all the components controlled by a left-to-right Boxlayout should generally have the same Y alignment.
        panelFFTLength.setLayout(new BoxLayoutFixed(panelFFTLength, BoxLayoutFixed.X_AXIS));

        panelFFTLength.setBackground(background.getColor());
        panelFFTLength.setAlignmentX(Component.LEFT_ALIGNMENT);

        panelFFTLength.add(labelFFTLength);
        panelFFTLength.add(Box.createHorizontalGlue());
        panelFFTLength.add(comboFFTLength);

        return (panelFFTLength);
        }


    /***********************************************************************************************
     * Create a drop-down of DisplayModes.
     *
     * @param consumer
     * @param metadatakey
     * @param font
     * @param foreground
     * @param background
     *
     * @return JPanel
     */

    public static JPanel createDisplayModeSelector(final AveragingFFTSidebarInterface consumer,
                                                   final String metadatakey,
                                                   final FontInterface font,
                                                   final ColourInterface foreground,
                                                   final ColourInterface background)
        {
        final String SOURCE = "AveragingFFTUIHelper.createDisplayModeSelector() ";
        final FFTDisplayMode[] arrayDisplayModes;
        final ActionListener choiceListener;
        final JPanel panelDisplayMode;
        final JLabel labelDisplayMode;
        final JComboBox comboDisplayMode;

        labelDisplayMode = new JLabel("Display");
        labelDisplayMode.setFont(font.getFont());
        labelDisplayMode.setForeground(foreground.getColor());

        comboDisplayMode = new JComboBox();
        comboDisplayMode.setFont(font.getFont());
        comboDisplayMode.setForeground(foreground.getColor());
        comboDisplayMode.setBackground(background.getColor());
        comboDisplayMode.setRenderer(new AlignedListCellRenderer(SwingConstants.LEFT,
                                                                 font,
                                                                 foreground,
                                                                 background,
                                                                 FFTDisplayMode.TOOLTIP));

        // Do NOT allow the combo box to take up all the remaining space!
        comboDisplayMode.setPreferredSize(DIM_COMBO);
        comboDisplayMode.setMaximumSize(DIM_COMBO);
        comboDisplayMode.setAlignmentX(0);

        comboDisplayMode.setToolTipText(FFTLength.TOOLTIP);
        comboDisplayMode.setEnabled(true);
        comboDisplayMode.setEditable(false);

        arrayDisplayModes = FFTDisplayMode.values();

        // Load up the ComboBox
        for (int intLengthIndex = 0;
             intLengthIndex < arrayDisplayModes.length;
             intLengthIndex++)
            {
            final FFTDisplayMode mode;

            mode = arrayDisplayModes[intLengthIndex];

            // Add the enum Object, not just the name
            comboDisplayMode.addItem(mode);
            }

        // Beware that there might not have been any valid DisplayModes
        if ((comboDisplayMode.getItemCount() > 0)
            && (consumer.getMetadataList() != null))
            {
            final Metadata metadata;

            metadata = MetadataHelper.getMetadataByKey(consumer.getMetadataList(),
                                                       metadatakey);
            if (metadata != null)
                {
                // Set the default ComboBox selection to the supplied state
                if (FFTDisplayMode.getDisplayModeForName(metadata.getValue()) != null)
                    {
                    comboDisplayMode.setSelectedItem(FFTDisplayMode.getDisplayModeForName(metadata.getValue()));
                    }
                else
                    {
                    comboDisplayMode.setSelectedItem(FFTDisplayMode.SINGLE_SPECTRUM);
                    }
                }
            else
                {
                comboDisplayMode.setSelectedItem(FFTDisplayMode.SINGLE_SPECTRUM);
                }

            comboDisplayMode.revalidate();
            }

        choiceListener = new ActionListener()
            {
            public void actionPerformed(final ActionEvent event)
                {
                final Metadata metadata;

                // Update the Metadata to show the new ComboBox election
                metadata = MetadataHelper.getMetadataByKey(consumer.getMetadataList(),
                                                           metadatakey);
                if (metadata != null)
                    {
                    metadata.setValue(((FFTDisplayMode) comboDisplayMode.getSelectedItem()).getName());

                    consumer.notifyMetadataChangedEvent(this,
                                                        metadatakey,
                                                        metadata.getValue(),
                                                        MetadataItemState.EDIT);
                    }
                }
            };

        comboDisplayMode.addActionListener(choiceListener);

        panelDisplayMode = new JPanel();
        panelDisplayMode.setBorder(UIComponentPlugin.BORDER_SIDEBAR_ITEM);

        // In general, all the components controlled by a top-to-bottom BoxLayout object should have the same X alignment.
        // Similarly, all the components controlled by a left-to-right Boxlayout should generally have the same Y alignment.
        panelDisplayMode.setLayout(new BoxLayoutFixed(panelDisplayMode, BoxLayoutFixed.X_AXIS));

        panelDisplayMode.setBackground(background.getColor());
        panelDisplayMode.setAlignmentX(Component.LEFT_ALIGNMENT);

        panelDisplayMode.add(labelDisplayMode);
        panelDisplayMode.add(Box.createHorizontalGlue());
        panelDisplayMode.add(comboDisplayMode);

        return (panelDisplayMode);
        }


    /***********************************************************************************************
     * Create a drop-down of WindowingFunctions.
     *
     * @param consumer
     * @param metadatakey
     * @param font
     * @param foreground
     * @param background
     *
     * @return JPanel
     */

    public static JPanel createWindowingFunctionSelector(final AveragingFFTSidebarInterface consumer,
                                                         final String metadatakey,
                                                         final FontInterface font,
                                                         final ColourInterface foreground,
                                                         final ColourInterface background)
        {
        final String SOURCE = "AveragingFFTUIHelper.createWindowingFunctionSelector() ";
        final WindowingFunction[] arrayWindowingFunctions;
        final ActionListener choiceListener;
        final JPanel panelWindowingFunction;
        final JLabel labelWindowingFunction;
        final JComboBox comboWindowingFunction;

        labelWindowingFunction = new JLabel("Window");
        labelWindowingFunction.setFont(font.getFont());
        labelWindowingFunction.setForeground(foreground.getColor());

        comboWindowingFunction = new JComboBox();
        comboWindowingFunction.setFont(font.getFont());
        comboWindowingFunction.setForeground(foreground.getColor());
        comboWindowingFunction.setBackground(background.getColor());
        comboWindowingFunction.setRenderer(new AlignedListCellRenderer(SwingConstants.LEFT,
                                                                       font,
                                                                       foreground,
                                                                       background,
                                                                       WindowingFunction.TOOLTIP));

        // Do NOT allow the combo box to take up all the remaining space!
        comboWindowingFunction.setPreferredSize(DIM_COMBO);
        comboWindowingFunction.setMaximumSize(DIM_COMBO);
        comboWindowingFunction.setAlignmentX(0);

        comboWindowingFunction.setToolTipText(FFTLength.TOOLTIP);
        comboWindowingFunction.setEnabled(true);
        comboWindowingFunction.setEditable(false);

        arrayWindowingFunctions = WindowingFunction.values();

        // Load up the ComboBox
        for (int intLengthIndex = 0;
             intLengthIndex < arrayWindowingFunctions.length;
             intLengthIndex++)
            {
            final WindowingFunction function;

            function = arrayWindowingFunctions[intLengthIndex];

            // Add the enum Object, not just the name
            comboWindowingFunction.addItem(function);
            }

        // Beware that there might not have been any valid WindowingFunctions
        if ((comboWindowingFunction.getItemCount() > 0)
            && (consumer.getMetadataList() != null))
            {
            final Metadata metadata;

            metadata = MetadataHelper.getMetadataByKey(consumer.getMetadataList(),
                                                       metadatakey);
            if (metadata != null)
                {
                // Set the default ComboBox selection to the supplied state
                if (WindowingFunction.getWindowingFunctionForName(metadata.getValue()) != null)
                    {
                    comboWindowingFunction.setSelectedItem(WindowingFunction.getWindowingFunctionForName(metadata.getValue()));
                    }
                else
                    {
                    comboWindowingFunction.setSelectedItem(WindowingFunction.WINDOW_NONE);
                    }
                }
            else
                {
                comboWindowingFunction.setSelectedItem(WindowingFunction.WINDOW_NONE);
                }

            comboWindowingFunction.revalidate();
            }

        choiceListener = new ActionListener()
            {
            public void actionPerformed(final ActionEvent event)
                {
                final Metadata metadata;

                // Update the Metadata to show the new ComboBox election
                metadata = MetadataHelper.getMetadataByKey(consumer.getMetadataList(),
                                                           metadatakey);
                if (metadata != null)
                    {
                    metadata.setValue(((WindowingFunction) comboWindowingFunction.getSelectedItem()).getName());

                    consumer.notifyMetadataChangedEvent(this,
                                                        metadatakey,
                                                        metadata.getValue(),
                                                        MetadataItemState.EDIT);
                    }
                }
            };

        comboWindowingFunction.addActionListener(choiceListener);

        panelWindowingFunction = new JPanel();
        panelWindowingFunction.setBorder(UIComponentPlugin.BORDER_SIDEBAR_ITEM);

        // In general, all the components controlled by a top-to-bottom BoxLayout object should have the same X alignment.
        // Similarly, all the components controlled by a left-to-right Boxlayout should generally have the same Y alignment.
        panelWindowingFunction.setLayout(new BoxLayoutFixed(panelWindowingFunction, BoxLayoutFixed.X_AXIS));

        panelWindowingFunction.setBackground(background.getColor());
        panelWindowingFunction.setAlignmentX(Component.LEFT_ALIGNMENT);

        panelWindowingFunction.add(labelWindowingFunction);
        panelWindowingFunction.add(Box.createHorizontalGlue());
        panelWindowingFunction.add(comboWindowingFunction);

        return (panelWindowingFunction);
        }


    /***********************************************************************************************
     * Create the radio buttons to swap between I and Q.
     *
     * @param consumer
     * @param metadatakey
     * @param font
     * @param foreground
     * @param background
     *
     * @return JPanel
     */

    public static JPanel createRadioButtonPanel(final AveragingFFTSidebarInterface consumer,
                                                final String metadatakey,
                                                final FontInterface font,
                                                final ColourInterface foreground,
                                                final ColourInterface background)
        {
        final String SOURCE = "AveragingFFTUIHelper.createRadioButtonPanel() ";
        final JPanel panelIQ;
        final JLabel labelIQ;
        final ButtonGroup buttonGroup;
        final JRadioButton buttonI;
        final JRadioButton buttonQ;
        final ActionListener listenerI;
        final ActionListener listenerQ;

        labelIQ = new JLabel("Swap");
        labelIQ.setFont(font.getFont());
        labelIQ.setForeground(foreground.getColor());

        buttonGroup = new ButtonGroup();

        // Set up the I button
        buttonI = new JRadioButton("I");
        buttonI.setHorizontalTextPosition(SwingConstants.RIGHT);
        buttonI.setToolTipText("Swap I");
        buttonI.setFont(font.getFont());
        buttonI.setForeground(foreground.getColor());
        buttonI.setBackground(background.getColor());

        listenerI = new ActionListener()
            {
            public void actionPerformed(final ActionEvent event)
                {
                // Update the Metadata to show the new selection
                updateRadioButton(consumer,
                                  consumer.getMetadataList(),
                                  metadatakey,
                                  buttonI,
                                  "I",
                                  "Q");
                }
            };

        buttonI.addActionListener(listenerI);
        buttonGroup.add(buttonI);

        // Set up the Q button
        buttonQ = new JRadioButton("Q");
        buttonQ.setHorizontalTextPosition(SwingConstants.RIGHT);
        buttonQ.setToolTipText("Swap Q");
        buttonQ.setFont(font.getFont());
        buttonQ.setForeground(foreground.getColor());
        buttonQ.setBackground(background.getColor());

        listenerQ = new ActionListener()
            {
            public void actionPerformed(final ActionEvent event)
                {
                // Update the Metadata to show the new selection
                updateRadioButton(consumer,
                                  consumer.getMetadataList(),
                                  metadatakey,
                                  buttonI,
                                  "I",
                                  "Q");
                }
            };

        buttonQ.addActionListener(listenerQ);
        buttonGroup.add(buttonQ);

        if (consumer.getMetadataList() != null)
            {
            final Metadata metadata;

            metadata = MetadataHelper.getMetadataByKey(consumer.getMetadataList(),
                                                       metadatakey);
            if (metadata != null)
                {
                if ("I".equals(metadata.getValue()))
                    {
                    // Set the default selection to the supplied state
                    buttonGroup.setSelected(buttonI.getModel(), true);
                    buttonGroup.setSelected(buttonQ.getModel(), false);
                    }
                else
                    {
                    buttonGroup.setSelected(buttonI.getModel(), false);
                    buttonGroup.setSelected(buttonQ.getModel(), true);
                    }
                }
            else
                {
                buttonGroup.setSelected(buttonI.getModel(), true);
                buttonGroup.setSelected(buttonQ.getModel(), false);
                }
            }

        panelIQ = new JPanel();
        panelIQ.setBorder(UIComponentPlugin.BORDER_SIDEBAR_ITEM);
        // In general, all the components controlled by a top-to-bottom BoxLayout object should have the same X alignment.
        // Similarly, all the components controlled by a left-to-right Boxlayout should generally have the same Y alignment.
        panelIQ.setLayout(new BoxLayoutFixed(panelIQ, BoxLayoutFixed.X_AXIS));
        panelIQ.setBackground(background.getColor());
        panelIQ.setAlignmentX(Component.LEFT_ALIGNMENT);

        panelIQ.add(labelIQ);
        panelIQ.add(Box.createHorizontalGlue());
        panelIQ.add(buttonI);
        panelIQ.add(Box.createHorizontalStrut(UIComponentPlugin.DIM_RADIOBUTTON_SEPARATOR.width));
        panelIQ.add(buttonQ);

        return (panelIQ);
        }


    /***********************************************************************************************
     * Create the radio buttons to swap between Linear or Logarithmic plots.
     *
     * @param consumer
     * @param metadatakey
     * @param font
     * @param foreground
     * @param background
     *
     * @return JPanel
     */

    public static JPanel createLinLogSelector(final AveragingFFTSidebarInterface consumer,
                                              final String metadatakey,
                                              final FontInterface font,
                                              final ColourInterface foreground,
                                              final ColourInterface background)
        {
        final String SOURCE = "AveragingFFTUIHelper.createLinLogSelector() ";
        final JPanel panelLinLog;
        final JLabel labelLinLog;
        final ButtonGroup buttonGroup;
        final JRadioButton buttonLin;
        final JRadioButton buttonLog;
        final ActionListener listenerLin;
        final ActionListener listenerLog;

        labelLinLog = new JLabel("Plot");
        labelLinLog.setFont(font.getFont());
        labelLinLog.setForeground(foreground.getColor());

        buttonGroup = new ButtonGroup();

        // Set up the Linear button
        buttonLin = new JRadioButton("Lin");
        buttonLin.setHorizontalTextPosition(SwingConstants.LEFT);
        buttonLin.setToolTipText("Linear Plot");
        buttonLin.setFont(font.getFont());
        buttonLin.setForeground(foreground.getColor());
        buttonLin.setBackground(background.getColor());

        listenerLin = new ActionListener()
            {
            public void actionPerformed(final ActionEvent event)
                {
                // Update the Metadata to show the new selection
                updateRadioButton(consumer,
                                  consumer.getMetadataList(),
                                  metadatakey,
                                  buttonLin,
                                  "Lin",
                                  "Log");
                }
            };

        buttonLin.addActionListener(listenerLin);
        buttonGroup.add(buttonLin);

        // Set up the Logarithmic button
        buttonLog = new JRadioButton("Log");
        buttonLog.setHorizontalTextPosition(SwingConstants.LEFT);
        buttonLog.setToolTipText("Logarithmic Plot");
        buttonLog.setFont(font.getFont());
        buttonLog.setForeground(foreground.getColor());
        buttonLog.setBackground(background.getColor());

        listenerLog = new ActionListener()
            {
            public void actionPerformed(final ActionEvent event)
                {
                // Update the Metadata to show the new selection
                updateRadioButton(consumer,
                                  consumer.getMetadataList(),
                                  metadatakey,
                                  buttonLin,
                                  "Lin",
                                  "Log");
                }
            };

        buttonLog.addActionListener(listenerLog);
        buttonGroup.add(buttonLog);

        if (consumer.getMetadataList() != null)
            {
            final Metadata metadata;

            metadata = MetadataHelper.getMetadataByKey(consumer.getMetadataList(),
                                                       metadatakey);
            if (metadata != null)
                {
                if ("Lin".equals(metadata.getValue()))
                    {
                    // Set the default selection to the supplied state
                    buttonGroup.setSelected(buttonLin.getModel(), true);
                    buttonGroup.setSelected(buttonLog.getModel(), false);
                    }
                else
                    {
                    buttonGroup.setSelected(buttonLin.getModel(), false);
                    buttonGroup.setSelected(buttonLog.getModel(), true);
                    }
                }
            else
                {
                buttonGroup.setSelected(buttonLin.getModel(), true);
                buttonGroup.setSelected(buttonLog.getModel(), false);
                }
            }

        panelLinLog = new JPanel();
        panelLinLog.setBorder(UIComponentPlugin.BORDER_SIDEBAR_ITEM);
        // In general, all the components controlled by a top-to-bottom BoxLayout object should have the same X alignment.
        // Similarly, all the components controlled by a left-to-right Boxlayout should generally have the same Y alignment.
        panelLinLog.setLayout(new BoxLayoutFixed(panelLinLog, BoxLayoutFixed.X_AXIS));
        panelLinLog.setBackground(background.getColor());
        panelLinLog.setAlignmentX(Component.LEFT_ALIGNMENT);

        panelLinLog.add(labelLinLog);
        panelLinLog.add(Box.createHorizontalGlue());
        panelLinLog.add(buttonLin);
        panelLinLog.add(Box.createHorizontalStrut(UIComponentPlugin.DIM_RADIOBUTTON_SEPARATOR.width));
        panelLinLog.add(buttonLog);

        return (panelLinLog);
        }


    /***********************************************************************************************
     * Make the specified Metadata consistent with the state of the specified RadioButton.
     *
     * @param consumer
     * @param metadatalist
     * @param metadatakey
     * @param button
     * @param selected
     * @param deselected
     */

    private static void updateRadioButton(final AveragingFFTSidebarInterface consumer,
                                          final List<Metadata> metadatalist,
                                          final String metadatakey,
                                          final JRadioButton button,
                                          final String selected,
                                          final String deselected)
        {
        final Metadata metadata;

        metadata = MetadataHelper.getMetadataByKey(metadatalist, metadatakey);

        if (metadata != null)
            {
            if (button.isSelected())
                {
                metadata.setValue(selected);
                }
            else
                {
                metadata.setValue(deselected);
                }

            consumer.notifyMetadataChangedEvent(consumer,
                                                metadatakey,
                                                metadata.getValue(),
                                                MetadataItemState.EDIT);
            }
        }


    /***********************************************************************************************
     * Create an Amplitude Adjuster.
     *
     * @param consumer
     * @param metadatakey
     * @param fontdata
     * @param foregroundcolour
     * @param backgroundcolour
     *
     * @return JPanel
     */

    private static JPanel createAmplitudeAdjuster(final AveragingFFTSidebarInterface consumer,
                                                  final String metadatakey,
                                                  final FontInterface fontdata,
                                                  final ColourInterface foregroundcolour,
                                                  final ColourInterface backgroundcolour)
        {
        final String SOURCE = "AveragingFFTUIHelper.createAmplitudeAdjuster() ";
        final JPanel panelAdjustAmplitude;
        final JLabel labelAdjustAmplitude;
        final IndicatorInterface indicatorAmplitude;
        final ControlKnobInterface controlAmplitude;

        labelAdjustAmplitude = new JLabel("Amplitude");
        labelAdjustAmplitude.setFont(fontdata.getFont());
        labelAdjustAmplitude.setForeground(foregroundcolour.getColor());

        indicatorAmplitude = new ToolbarIndicator(DIM_INDICATOR_AMPLITUDE,
                                                  EMPTY_STRING,
                                                  TOOLTIP_AMPLITUDE);
        indicatorAmplitude.setValueFormat("0.00");
        indicatorAmplitude.setValueBackground(Color.BLACK);

        // If both FineScaleMin and FineScaleMax are zero, then do not add a fine control knob
        controlAmplitude = new CoarseFineKnobs(DIM_CONTROL_KNOB_AMPLITUDE,
                                               true,
                                               TICKLENGTH_AMPLITUDE,
                                               AMPLITUDE_MIN,
                                               AMPLITUDE_MAX,
                                               0,
                                               0,
                                               TOOLTIP_COARSE_AMPLITUDE,
                                               TOOLTIP_FINE_AMPLITUDE,
                                               backgroundcolour);

        controlAmplitude.addChangeListener(new ChangeListener()
            {
            public void stateChanged(final ChangeEvent event)
                {
                // Update the Indicator to show the new value
                indicatorAmplitude.setValue(PATTERN_AMPLITUDE.format(controlAmplitude.getValue()));
                }
            });

        controlAmplitude.addCommitChangeListener(new CommitChangeListener()
            {
            public void commitChange(final CommitChangeEvent event)
                {
                final Metadata metadata;

                // Update the Metadata to show the new value
                metadata = MetadataHelper.getMetadataByKey(consumer.getMetadataList(),
                                                           metadatakey);
                if (metadata != null)
                    {
                    metadata.setValue(PATTERN_AMPLITUDE.format(controlAmplitude.getValue()));

                    consumer.notifyMetadataChangedEvent(this,
                                                        metadatakey,
                                                        metadata.getValue(),
                                                        MetadataItemState.EDIT);
                    }

                indicatorAmplitude.setValue(PATTERN_AMPLITUDE.format(controlAmplitude.getValue()));
                }
            });

        controlAmplitude.initialiseUI();
        controlAmplitude.setEnabled(true);

        // Set the default selection to the supplied state
        if (consumer.getMetadataList() != null)
            {
            final Metadata metadata;

            metadata = MetadataHelper.getMetadataByKey(consumer.getMetadataList(),
                                                       metadatakey);
            try
                {
                if (metadata != null)
                    {
                    final double dblValue;

                    dblValue = Double.parseDouble(metadata.getValue());

                    // Set the default selection to the supplied state
                    controlAmplitude.setValue(dblValue);
                    indicatorAmplitude.setValue(PATTERN_AMPLITUDE.format(dblValue));
                    }
                else
                    {
                    controlAmplitude.setValue(0.0);
                    indicatorAmplitude.setValue(PATTERN_AMPLITUDE.format(0.0));
                    }
                }

            catch (final NumberFormatException exception)
                {
                controlAmplitude.setValue(0.0);
                indicatorAmplitude.setValue(PATTERN_AMPLITUDE.format(0.0));
                }
            }

        panelAdjustAmplitude = new JPanel();
        // In general, all the components controlled by a top-to-bottom BoxLayout object should have the same X alignment.
        // Similarly, all the components controlled by a left-to-right Boxlayout should generally have the same Y alignment.
        panelAdjustAmplitude.setLayout(new BoxLayoutFixed(panelAdjustAmplitude, BoxLayoutFixed.X_AXIS));
        panelAdjustAmplitude.setBorder(UIComponentPlugin.BORDER_SIDEBAR_ITEM);
        panelAdjustAmplitude.setBackground(backgroundcolour.getColor());
        panelAdjustAmplitude.setAlignmentX(Component.LEFT_ALIGNMENT);

        panelAdjustAmplitude.add(labelAdjustAmplitude);
        panelAdjustAmplitude.add(Box.createHorizontalGlue());
        panelAdjustAmplitude.add((Component) indicatorAmplitude);
        panelAdjustAmplitude.add(Box.createHorizontalStrut(UIComponentPlugin.DIM_KNOB_SEPARATOR.width));
        panelAdjustAmplitude.add((Component) controlAmplitude);

        return (panelAdjustAmplitude);
        }


    /***********************************************************************************************
     * Create a Phase Adjuster.
     *
     * @param consumer
     * @param metadatakey
     * @param fontdata
     * @param foregroundcolour
     * @param backgroundcolour
     *
     * @return JPanel
     */

    private static JPanel createPhaseAdjuster(final AveragingFFTSidebarInterface consumer,
                                              final String metadatakey,
                                              final FontInterface fontdata,
                                              final ColourInterface foregroundcolour,
                                              final ColourInterface backgroundcolour)
        {
        final String SOURCE = "AveragingFFTUIHelper.createPhaseAdjuster() ";
        final JPanel panelAdjustPhase;
        final JLabel labelAdjustPhase;
        final IndicatorInterface indicatorPhase;
        final ControlKnobInterface controlPhase;

        labelAdjustPhase = new JLabel("Phase");
        labelAdjustPhase.setFont(fontdata.getFont());
        labelAdjustPhase.setForeground(foregroundcolour.getColor());

        indicatorPhase = new ToolbarIndicator(DIM_INDICATOR_PHASE,
                                              EMPTY_STRING,
                                              TOOLTIP_PHASE);
        indicatorPhase.setValueFormat("0.00");
        indicatorPhase.setValueBackground(Color.BLACK);

        // If both FineScaleMin and FineScaleMax are zero, then do not add a fine control knob
        controlPhase = new CoarseFineKnobs(DIM_CONTROL_KNOB_PHASE,
                                           true,
                                           TICKLENGTH_PHASE,
                                           PHASE_MIN,
                                           PHASE_MAX,
                                           0,
                                           0,
                                           TOOLTIP_COARSE_PHASE,
                                           TOOLTIP_FINE_PHASE,
                                           backgroundcolour);

        controlPhase.addChangeListener(new ChangeListener()
            {
            public void stateChanged(final ChangeEvent event)
                {
                // Update the Indicator to show the new value
                indicatorPhase.setValue(PATTERN_PHASE.format(controlPhase.getValue()));
                }
            });

        controlPhase.addCommitChangeListener(new CommitChangeListener()
            {
            public void commitChange(final CommitChangeEvent event)
                {
                final Metadata metadata;

                // Update the Metadata to show the new selection
                metadata = MetadataHelper.getMetadataByKey(consumer.getMetadataList(),
                                                           metadatakey);
                if (metadata != null)
                    {
                    metadata.setValue(PATTERN_PHASE.format(controlPhase.getValue()));

                    consumer.notifyMetadataChangedEvent(this,
                                                        metadatakey,
                                                        metadata.getValue(),
                                                        MetadataItemState.EDIT);
                    }

                indicatorPhase.setValue(PATTERN_PHASE.format(controlPhase.getValue()));
                }
            });

        controlPhase.initialiseUI();
        controlPhase.setEnabled(true);

        // Set the default selection to the supplied state
        if (consumer.getMetadataList() != null)
            {
            final Metadata metadata;

            metadata = MetadataHelper.getMetadataByKey(consumer.getMetadataList(),
                                                       metadatakey);
            try
                {
                if (metadata != null)
                    {
                    final double dblValue;

                    dblValue = Double.parseDouble(metadata.getValue());

                    // Set the default selection to the supplied state
                    controlPhase.setValue(dblValue);
                    indicatorPhase.setValue(PATTERN_PHASE.format(dblValue));
                    }
                else
                    {
                    controlPhase.setValue(0.0);
                    indicatorPhase.setValue(PATTERN_PHASE.format(0.0));
                    }
                }

            catch (final NumberFormatException exception)
                {
                controlPhase.setValue(0.0);
                indicatorPhase.setValue(PATTERN_PHASE.format(0.0));
                }
            }

        panelAdjustPhase = new JPanel();
        // In general, all the components controlled by a top-to-bottom BoxLayout object should have the same X alignment.
        // Similarly, all the components controlled by a left-to-right Boxlayout should generally have the same Y alignment.
        panelAdjustPhase.setLayout(new BoxLayoutFixed(panelAdjustPhase, BoxLayoutFixed.X_AXIS));
        panelAdjustPhase.setBorder(UIComponentPlugin.BORDER_SIDEBAR_ITEM);
        panelAdjustPhase.setBackground(backgroundcolour.getColor());
        panelAdjustPhase.setAlignmentX(Component.LEFT_ALIGNMENT);

        panelAdjustPhase.add(labelAdjustPhase);
        panelAdjustPhase.add(Box.createHorizontalGlue());
        panelAdjustPhase.add((Component) indicatorPhase);
        panelAdjustPhase.add(Box.createHorizontalStrut(UIComponentPlugin.DIM_KNOB_SEPARATOR.width));
        panelAdjustPhase.add((Component) controlPhase);

        return (panelAdjustPhase);
        }


    /***********************************************************************************************
     * Create the checkbox to sync to I or Q.
     *
     * @param consumer
     * @param metadatakey
     * @param font
     * @param foreground
     * @param background
     *
     * @return JPanel
     */

    private static JPanel createIQCheckbox(final AveragingFFTSidebarInterface consumer,
                                           final String metadatakey,
                                           final FontInterface font,
                                           final ColourInterface foreground,
                                           final ColourInterface background)
        {
        final String SOURCE = "AveragingFFTUIHelper.createIQCheckbox() ";
        final JPanel panelCheckBox;
        final JLabel labelIQ;
        final JCheckBox checkBox;
        final ActionListener listener;

        labelIQ = new JLabel("Swap IQ");
        labelIQ.setFont(font.getFont());
        labelIQ.setForeground(foreground.getColor());

        // Set up the CheckBox
        checkBox = new JCheckBox();
        checkBox.setToolTipText("Swap I and Q");
        checkBox.setFont(font.getFont());
        checkBox.setForeground(foreground.getColor());
        checkBox.setBackground(background.getColor());

        listener = new ActionListener()
            {
            public void actionPerformed(final ActionEvent event)
                {
                // Update the Metadata to show the new selection
                updateCheckbox(consumer,
                               consumer.getMetadataList(),
                               metadatakey,
                               checkBox,
                               "I",
                               "Q");
                }
            };

        checkBox.addActionListener(listener);

        // Set the initial state of the CheckBox from the Metadata
        if (consumer.getMetadataList() != null)
            {
            final Metadata metadata;

            metadata = MetadataHelper.getMetadataByKey(consumer.getMetadataList(),
                                                       metadatakey);
            if (metadata != null)
                {
                if ("I".equals(metadata.getValue()))
                    {
                    // Set the default selection to the supplied state
                    checkBox.setSelected(true);
                    }
                else
                    {
                    checkBox.setSelected(false);
                    }
                }
            else
                {
                checkBox.setSelected(true);
                }
            }

        panelCheckBox = new JPanel();
        // In general, all the components controlled by a top-to-bottom BoxLayout object should have the same X alignment.
        // Similarly, all the components controlled by a left-to-right Boxlayout should generally have the same Y alignment.
        panelCheckBox.setLayout(new BoxLayoutFixed(panelCheckBox, BoxLayoutFixed.X_AXIS));
        panelCheckBox.setBackground(background.getColor());
        panelCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        panelCheckBox.add(labelIQ);
        panelCheckBox.add(Box.createHorizontalStrut(10));
        panelCheckBox.add(checkBox);

        return (panelCheckBox);
        }


    /***********************************************************************************************
     * Make the specified Metadata consistent with the state of the specified CheckBox.
     *
     * @param metadatalist
     * @param metadatakey
     * @param checkbox
     * @param selected
     * @param deselected
     */

    private static void updateCheckbox(final AveragingFFTSidebarInterface consumer,
                                       final List<Metadata> metadatalist,
                                       final String metadatakey,
                                       final JCheckBox checkbox,
                                       final String selected,
                                       final String deselected)
        {
        final Metadata metadata;

        metadata = MetadataHelper.getMetadataByKey(metadatalist, metadatakey);

        if (metadata != null)
            {
            if (checkbox.isSelected())
                {
                metadata.setValue(selected);
                }
            else
                {
                metadata.setValue(deselected);
                }

            consumer.notifyMetadataChangedEvent(consumer,
                                                metadatakey,
                                                metadata.getValue(),
                                                MetadataItemState.EDIT);
            }
        }


    /***********************************************************************************************
     * Create the panel with the IQ plot and adjusters for Amplitude and Phase.
     *
     * @param consumer
     * @param key_offsetamplitude
     * @param key_offsetphase
     * @param key_swapiq
     * @param fontdata
     * @param foregroundcolour
     * @param backgroundcolour
     *
     * @return JPanel
     */

    public static JPanel createIQAdjuster(final AveragingFFTSidebarInterface consumer,
                                          final String key_offsetamplitude,
                                          final String key_offsetphase,
                                          final String key_swapiq,
                                          final FontInterface fontdata,
                                          final ColourInterface foregroundcolour,
                                          final ColourInterface backgroundcolour)
        {
        final String SOURCE = "AveragingFFTUIHelper.createIQAdjuster() ";
        final Dimension DIM_IQPLOT = new Dimension(121, 121);
        final JPanel panelIQAdjuster;

        final JPanel panelDisplay;
        final ImageIcon iconIQAdjuster;
        final JPanel panelIQPlot;
        final JPanel panelSwapIQ;

        final JPanel panelAdjustAmplitude;
        final JPanel panelAdjustPhase;

        // TableLayout row and column size definitions
        final double[][] size =
            {
                { // Columns
                  10,
                  TableLayout.PREFERRED,    // Icon & Checkbox
                  5,
                  TableLayout.PREFERRED,    // IQPlot
                  10
                },
                { // Rows
                  5,
                  TableLayout.PREFERRED,    // Icon
                  TableLayout.FILL,         // Spacer
                  TableLayout.PREFERRED,    // Checkbox
                  5
                }
            };

        // TableLayout constraints
        // http://www.clearthought.info/sun/products/jfc/tsc/articles/tablelayout/Cells.html
        // The horizontal justification is specified before the vertical justification
        // Multiple Cells
        // A component can also be added to a rectangular set of cells.
        // This is done by specifying the upper, left and lower, right corners of that set.
        // Components that occupy more than one cell will have a size equal to the total area
        // of all cells that component occupies.
        // There is no justification attribute for multi-celled components.

        final String[] constraints =
            { // Column, Row, JustificationX, JustificationY

              "1, 1, LEFT, TOP",         // Icon

              "1, 3, LEFT, CENTER",      // CheckBox

              "3, 1, 3, 3"               // IQPlot
            };

        iconIQAdjuster = RegistryModelUtilities.getAtomIcon(consumer.getObservatoryUI().getHostAtom(),
                                                            ObservatoryInterface.FILENAME_ICON_IQ_ADJUSTER);

        panelIQPlot = new JPanel();
        panelIQPlot.setBackground(UIComponentPlugin.DEFAULT_COLOUR_CANVAS.getColor());
        panelIQPlot.setMinimumSize(DIM_IQPLOT);
        panelIQPlot.setPreferredSize(DIM_IQPLOT);
        panelIQPlot.setMaximumSize(DIM_IQPLOT);
        panelIQPlot.setBorder(BorderFactory.createLineBorder(Color.black));

        // Allow access from the DAO
        consumer.setIQPlot(panelIQPlot);

        panelSwapIQ = createIQCheckbox(consumer,
                                       key_swapiq,
                                       fontdata,
                                       foregroundcolour,
                                       backgroundcolour);

        // Assemble the Plotter etc.
        panelDisplay = new JPanel();
        panelDisplay.setLayout(new TableLayout(size));
        panelDisplay.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelDisplay.setBackground(backgroundcolour.getColor());

        panelDisplay.add(new JLabel(iconIQAdjuster), constraints[0]);
        panelDisplay.add(panelSwapIQ, constraints[1]);
        panelDisplay.add(panelIQPlot, constraints[2]);

        panelAdjustAmplitude = createAmplitudeAdjuster(consumer,
                                                       key_offsetamplitude,
                                                       fontdata,
                                                       foregroundcolour,
                                                       backgroundcolour);

        panelAdjustPhase = createPhaseAdjuster(consumer,
                                               key_offsetphase,
                                               fontdata,
                                               foregroundcolour,
                                               backgroundcolour);

        // Assemble everything
        panelIQAdjuster = new JPanel();
        // In general, all the components controlled by a top-to-bottom BoxLayout object should have the same X alignment.
        // Similarly, all the components controlled by a left-to-right Boxlayout should generally have the same Y alignment.
        panelIQAdjuster.setLayout(new BoxLayoutFixed(panelIQAdjuster, BoxLayoutFixed.Y_AXIS));
        panelIQAdjuster.setBackground(backgroundcolour.getColor());
        panelIQAdjuster.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelIQAdjuster.setBorder(BorderFactory.createCompoundBorder(UIComponentPlugin.BORDER_SIDEBAR_ITEM,
                                                                     BorderFactory.createEtchedBorder(EtchedBorder.RAISED)));

        panelIQAdjuster.add(Box.createVerticalStrut(5));
        panelIQAdjuster.add(panelDisplay);
        panelIQAdjuster.add(Box.createVerticalStrut(5));
        panelIQAdjuster.add(panelAdjustAmplitude);
        panelIQAdjuster.add(Box.createVerticalStrut(5));
        panelIQAdjuster.add(panelAdjustPhase);
        panelIQAdjuster.add(Box.createVerticalStrut(5));

        return (panelIQAdjuster);
        }


    /***********************************************************************************************
     * Create a Metadata Name-Value Pair Viewer for the sidebar.
     *
     * @param hostframeui
     * @param fontdata
     * @param foregroundcolour
     * @param backgroundcolour
     * @param metadatalist
     * @param resourcekey
     *
     * @return MetadataNVPViewerUIComponentInterface
     */

    public static MetadataNVPViewerUIComponentInterface createMetadataNVPViewer(final AveragingFFTFrameUIComponentInterface hostframeui,
                                                                                final FontInterface fontdata,
                                                                                final ColourInterface foregroundcolour,
                                                                                final ColourInterface backgroundcolour,
                                                                                final List<Metadata> metadatalist,
                                                                                final String resourcekey)
        {
        final MetadataNVPViewerUIComponentInterface uiViewer;

        uiViewer = new MetadataNVPViewerUIComponent(hostframeui.getHostTask(),
                                                    hostframeui.getHostInstrument(),
                                                    metadatalist,
                                                    resourcekey);

        // In general, all the components controlled by a top-to-bottom BoxLayout object should have the same X alignment.
        // Similarly, all the components controlled by a left-to-right Boxlayout should generally have the same Y alignment.
        uiViewer.setAlignmentX(Component.LEFT_ALIGNMENT);

        uiViewer.setBorder(UIComponentPlugin.BORDER_SIDEBAR_ITEM);
        uiViewer.setBackground(backgroundcolour.getColor());

        return (uiViewer);
        }


    /***********************************************************************************************
     * Create the set of Metadata to be controlled by the FFTAverager.
     *
     * @param traceon
     *
     * @return List<Metadata>
     */

    public static List<Metadata> createAveragerMetadata(final boolean traceon)
        {
        final String SOURCE = "AveragingFFTUIHelper.createAveragerMetadata() ";
        final List<Metadata> listMetadata;

        listMetadata = new ArrayList<Metadata>(10);

        MetadataHelper.createOrUpdateMetadataItemTraced(listMetadata,
                                                        AveragingFFTUIComponentInterface.KEY_FFT_LENGTH,
                                                        "256",
                                                        "^(32|64|128|256|512|1024|2048)$",
                                                        DataTypeDictionary.DECIMAL_INTEGER,
                                                        SchemaUnits.DIMENSIONLESS,
                                                        "The length of the FFT, which must be a power of two",
                                                        SOURCE,
                                                        traceon);

        MetadataHelper.createOrUpdateMetadataItemTraced(listMetadata,
                                                        AveragingFFTUIComponentInterface.KEY_MODE_IQ,
                                                        "I",
                                                        "^(I|Q)$",
                                                        DataTypeDictionary.STRING,
                                                        SchemaUnits.DIMENSIONLESS,
                                                        "Swap In-Phase or Quadrature to reverse spectrum",
                                                        SOURCE,
                                                        traceon);

        MetadataHelper.createOrUpdateMetadataItemTraced(listMetadata,
                                                        AveragingFFTUIComponentInterface.KEY_MODE_PLOT,
                                                        "Lin",
                                                        "^(Lin|Log)$",
                                                        DataTypeDictionary.STRING,
                                                        SchemaUnits.DIMENSIONLESS,
                                                        "Select Linear or Logarithmic plot",
                                                        SOURCE,
                                                        traceon);

        MetadataHelper.createOrUpdateMetadataItemTraced(listMetadata,
                                                        AveragingFFTUIComponentInterface.KEY_MODE_DISPLAY,
                                                        "Spectrum",
                                                        REGEX_STRING,
                                                        DataTypeDictionary.STRING,
                                                        SchemaUnits.DIMENSIONLESS,
                                                        "Select the type of spectrum display",
                                                        SOURCE,
                                                        traceon);

        MetadataHelper.createOrUpdateMetadataItemTraced(listMetadata,
                                                        AveragingFFTUIComponentInterface.KEY_WINDOW,
                                                        "None",
                                                        REGEX_STRING,
                                                        DataTypeDictionary.STRING,
                                                        SchemaUnits.DIMENSIONLESS,
                                                        "The Windowing Function applied to the FFT data",
                                                        SOURCE,
                                                        traceon);

        MetadataHelper.createOrUpdateMetadataItemTraced(listMetadata,
                                                        AveragingFFTUIComponentInterface.KEY_FFT_COUNT,
                                                        "0",
                                                        REGEX_SIGNED_DECIMAL_INTEGER,
                                                        DataTypeDictionary.DECIMAL_INTEGER,
                                                        SchemaUnits.DIMENSIONLESS,
                                                        "The number of Fast Fourier Transforms being averaged",
                                                        SOURCE,
                                                        traceon);

        MetadataHelper.createOrUpdateMetadataItemTraced(listMetadata,
                                                        AveragingFFTUIComponentInterface.KEY_TIME_CONSTANT,
                                                        "0",
                                                        REGEX_SIGNED_DECIMAL_FLOAT,
                                                        DataTypeDictionary.DECIMAL_FLOAT,
                                                        SchemaUnits.SECONDS,
                                                        "The Integration Time in seconds",
                                                        SOURCE,
                                                        traceon);

        MetadataHelper.createOrUpdateMetadataItemTraced(listMetadata,
                                                        AveragingFFTUIComponentInterface.KEY_SAMPLE_RATE,
                                                        "2048",
                                                        REGEX_SIGNED_DECIMAL_FLOAT,
                                                        DataTypeDictionary.DECIMAL_FLOAT,
                                                        SchemaUnits.K_HZ,
                                                        "The Sample Rate in kHz",
                                                        SOURCE,
                                                        traceon);

        MetadataHelper.createOrUpdateMetadataItemTraced(listMetadata,
                                                        AveragingFFTUIComponentInterface.KEY_OFFSET_AMPLITUDE,
                                                        "0.0",
                                                        REGEX_SIGNED_DECIMAL_FLOAT,
                                                        DataTypeDictionary.DECIMAL_FLOAT,
                                                        SchemaUnits.DIMENSIONLESS,
                                                        "The Amplitude Offset",
                                                        SOURCE,
                                                        traceon);

        MetadataHelper.createOrUpdateMetadataItemTraced(listMetadata,
                                                        AveragingFFTUIComponentInterface.KEY_OFFSET_PHASE,
                                                        "0.0",
                                                        REGEX_SIGNED_DECIMAL_FLOAT,
                                                        DataTypeDictionary.DECIMAL_FLOAT,
                                                        SchemaUnits.DIMENSIONLESS,
                                                        "The Phase Offset",
                                                        SOURCE,
                                                        traceon);

        MetadataHelper.createOrUpdateMetadataItemTraced(listMetadata,
                                                        AveragingFFTUIComponentInterface.KEY_FILE_BLOCKS,
                                                        "0",
                                                        REGEX_SIGNED_DECIMAL_INTEGER,
                                                        DataTypeDictionary.DECIMAL_INTEGER,
                                                        SchemaUnits.DIMENSIONLESS,
                                                        "The number of File Blocks being processed",
                                                        SOURCE,
                                                        traceon);

        MetadataHelper.createOrUpdateMetadataItemTraced(listMetadata,
                                                        AveragingFFTUIComponentInterface.KEY_FILE_NAME,
                                                        "iq.bin",
                                                        REGEX_STRING,
                                                        DataTypeDictionary.STRING,
                                                        SchemaUnits.DIMENSIONLESS,
                                                        "The name of the IQ data file being processed",
                                                        SOURCE,
                                                        traceon);

        MetadataHelper.createOrUpdateMetadataItemTraced(listMetadata,
                                                        AveragingFFTUIComponentInterface.KEY_TEMPERATURE_FACTOR,
                                                        "0",
                                                        REGEX_SIGNED_DECIMAL_FLOAT,
                                                        DataTypeDictionary.DECIMAL_FLOAT,
                                                        SchemaUnits.D_B,
                                                        "The Temperature Factor in dB",
                                                        SOURCE,
                                                        traceon);
        return (listMetadata);
        }


    /***********************************************************************************************
     * Create the Toolbar Buttons.
     *
     * @param hostui
     * @param hostinstrument
     * @param hostframe
     * @param ffttoolbar
     * @param fontdata
     * @param colourforeground
     * @param colourbackground
     * @param debug
     *
     * @return List<Component>
     */

    public static List<Component> createToolbarComponents(final ObservatoryUIInterface hostui,
                                                          final ObservatoryInstrumentInterface hostinstrument,
                                                          final AveragingFFTFrameUIComponentInterface hostframe,
                                                          final AveragingFFTToolbarInterface ffttoolbar,
                                                          final FontInterface fontdata,
                                                          final ColourInterface colourforeground,
                                                          final ColourInterface colourbackground,
                                                          final boolean debug)
        {
        final List<Component> listComponents;
        final JLabel labelName;

        final ContextAction actionOpenFile;
        final ContextAction actionPlayPause;
        final ContextAction actionStop;
        final JProgressBar progressBar;

        final ContextAction actionMetadata;
        final ContextAction actionExport;
        final ContextAction actionPageSetup;
        final ContextAction actionPrint;
        final ContextAction actionHelp;

        listComponents = new ArrayList<Component>(10);

        //-------------------------------------------------------------------------------------
        // Initialise the Label and Icon

        labelName = new JLabel(ffttoolbar.getTitle(),
                               RegistryModelUtilities.getAtomIcon(hostinstrument.getHostAtom(),
                                                                  ObservatoryInterface.FILENAME_ICON_AVERAGING_FFT),
                               SwingConstants.LEFT)
            {
            private static final long serialVersionUID = 7580736117336162922L;

            // Enable Antialiasing in Java 1.5
            protected void paintComponent(final Graphics graphics)
                {
                final Graphics2D graphics2D = (Graphics2D) graphics;

                // For antialiasing text
                graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                                            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                super.paintComponent(graphics2D);
                }
            };

        labelName.setFont(fontdata.getFont().deriveFont(ReportTableHelper.SIZE_HEADER_FONT).deriveFont(Font.BOLD));
        labelName.setForeground(colourforeground.getColor());
        labelName.setIconTextGap(UIComponentPlugin.TOOLBAR_ICON_TEXT_GAP);

        //-------------------------------------------------------------------------------------
        // Inputs
        //-------------------------------------------------------------------------------------
        // OpenFile

        actionOpenFile = new ContextAction(AveragingFFTToolbarInterface.ACTION_NAME_OPEN_FILE,
                                           RegistryModelUtilities.getCommonIcon(UIComponentPlugin.FILENAME_ICON_OPEN_FILE),
                                           AveragingFFTToolbarInterface.ACTION_TOOLTIP_OPEN_FILE,
                                           KeyEvent.VK_O,
                                           false,
                                           true)
            {
            static final String SOURCE = "ContextAction:OpenFile ";
            private static final long serialVersionUID = -598953341739066982L;


            public void actionPerformed(final ActionEvent event)
                {
                if (hostui != null)
                    {
                    final JFileChooser chooser;
                    final int intStatus;

                    chooser = new JFileChooser();
                    chooser.setDialogTitle("Choose an IQ data file");
                    chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                    chooser.setForeground(colourforeground.getColor());

                    chooser.setApproveButtonText(ChooserInterface.BUTTON_SELECT);
                    chooser.setApproveButtonToolTipText("Enter the filename");

                    Utilities.setContainerHierarchyFonts(chooser.getComponents(),
                                                         UIComponentPlugin.DEFAULT_FONT);

                    Utilities.setContainerHierarchyColours(chooser.getComponents(),
                                                           colourforeground,
                                                           null);

                    intStatus = chooser.showOpenDialog((Component)hostui);

                    if (intStatus == JFileChooser.APPROVE_OPTION)
                        {
                        final File file;

                        file = chooser.getSelectedFile();

                        if ((file != null)
                            && (file.isFile()))
                            {
                            if ((hostframe.getDAO() != null)
                                && (hostframe.getMetadataList() != null))
                                {
                                final Metadata metadata;

                                metadata = MetadataHelper.getMetadataByKey(hostframe.getMetadataList(),
                                                                           AveragingFFTUIComponentInterface.KEY_FILE_NAME);
                                if (metadata != null)
                                    {
                                    metadata.setValue(file.getPath().replace(System.getProperty("file.separator").charAt(0), '/'));

                                    // Redraw the NVP list of Metadata
                                    hostframe.getFFTViewer().getSidebar().notifyMetadataChangedEvent(this,
                                                                                                     AveragingFFTUIComponentInterface.KEY_FILE_NAME,
                                                                                                     metadata.getValue(),
                                                                                                     MetadataItemState.EDIT);

                                    }
                                }
                            }
                        else
                            {
                            LOGGER.error(SOURCE + "Resource not found");
                            }
                        }
                    }
                else
                    {
                    LOGGER.error(SOURCE + "UI unexpectedly NULL");
                    }
                }
            };

        ffttoolbar.getOpenFileButton().setAction(actionOpenFile);
        ffttoolbar.getOpenFileButton().setBorderPainted(false);
        ffttoolbar.getOpenFileButton().setBorder(UIComponentPlugin.BORDER_BUTTON);
        ffttoolbar.getOpenFileButton().setHideActionText(true);
        ffttoolbar.getOpenFileButton().setToolTipText((String) actionOpenFile.getValue(Action.SHORT_DESCRIPTION));
        ffttoolbar.getOpenFileButton().setEnabled(true);

        //-------------------------------------------------------------------------------------
        // PlayPause

        actionPlayPause = new ContextAction(AveragingFFTToolbarInterface.ACTION_NAME_PLAY,
                                            RegistryModelUtilities.getCommonIcon(UIComponentPlugin.FILENAME_ICON_PLAY),
                                            AveragingFFTToolbarInterface.ACTION_TOOLTIP_PLAY,
                                            KeyEvent.VK_G,
                                            false,
                                            true)
            {
            final static String SOURCE = "ContextAction:PlayPause ";
            private static final long serialVersionUID = -598953341739066982L;


            public void actionPerformed(final ActionEvent event)
                {
                final ObservatoryInstrumentDAOInterface dao;

                dao = hostframe.getDAO();

                if (dao != null)
                    {
                    ((AveragingFFTDAOInterface)dao).processData(hostframe,
                                                                hostframe.getToolbar().getProgressBar(),
                                                                !((AveragingFFTDAOInterface) dao).isRunning());

                    if (((AveragingFFTDAOInterface) dao).isRunning())
                        {
                        putValue(Action.SMALL_ICON,
                                 RegistryModelUtilities.getCommonIcon(UIComponentPlugin.FILENAME_ICON_PAUSE));
                        putValue(Action.NAME,
                                 BUTTON_PAUSE);
                        putValue(Action.LONG_DESCRIPTION,
                                 TOOLTIP_PAUSE);
                        }
                    else
                        {
                        putValue(Action.SMALL_ICON,
                                 RegistryModelUtilities.getCommonIcon(UIComponentPlugin.FILENAME_ICON_PLAY));
                        putValue(Action.NAME,
                                 BUTTON_START);
                        putValue(Action.LONG_DESCRIPTION,
                                 TOOLTIP_START);
                        }
                    }
                }
            };

        ffttoolbar.getPlayPauseButton().setAction(actionPlayPause);
        ffttoolbar.getPlayPauseButton().setBorderPainted(false);
        ffttoolbar.getPlayPauseButton().setBorder(UIComponentPlugin.BORDER_BUTTON);
        ffttoolbar.getPlayPauseButton().setHideActionText(true);
        ffttoolbar.getPlayPauseButton().setToolTipText((String) actionPlayPause.getValue(Action.SHORT_DESCRIPTION));
        ffttoolbar.getPlayPauseButton().setEnabled(true);

        //-------------------------------------------------------------------------------------
        // Stop

        actionStop = new ContextAction(AveragingFFTToolbarInterface.ACTION_NAME_STOP,
                                       RegistryModelUtilities.getCommonIcon(UIComponentPlugin.FILENAME_ICON_MEDIA_STOP),
                                       AveragingFFTToolbarInterface.ACTION_TOOLTIP_STOP,
                                       KeyEvent.VK_SPACE,
                                       false,
                                       true)
            {
            final static String SOURCE = "ContextAction:Stop ";
            private static final long serialVersionUID = -598953341739066982L;


            public void actionPerformed(final ActionEvent event)
                {
                final ObservatoryInstrumentDAOInterface dao;

                dao = hostframe.getDAO();

                if (dao != null)
                    {
                    ((AveragingFFTDAOInterface)dao).processData(hostframe,
                                                                hostframe.getToolbar().getProgressBar(),
                                                                false);
                    }
                }
            };

        ffttoolbar.getStopButton().setAction(actionStop);
        ffttoolbar.getStopButton().setBorderPainted(false);
        ffttoolbar.getStopButton().setBorder(UIComponentPlugin.BORDER_BUTTON);
        ffttoolbar.getStopButton().setHideActionText(true);
        ffttoolbar.getStopButton().setToolTipText((String) actionStop.getValue(Action.SHORT_DESCRIPTION));
        ffttoolbar.getStopButton().setEnabled(true);

        //-------------------------------------------------------------------------------------
        // Progress Bar

        progressBar = new JProgressBar(0, 100);
        progressBar.setMinimumSize(ToolbarIndicator.DIM_TOOLBAR_PROGRESS);
        progressBar.setPreferredSize(ToolbarIndicator.DIM_TOOLBAR_PROGRESS);
        progressBar.setMaximumSize(ToolbarIndicator.DIM_TOOLBAR_PROGRESS);
        progressBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        progressBar.setForeground(new Color(15, 244, 28));
        progressBar.setBackground(new Color(255, 204, 204));
        progressBar.setValue(0);

        // The Toolbar has been created previously
        ffttoolbar.setProgressBar(progressBar);

        //-------------------------------------------------------------------------------------
        // Outputs
        //-------------------------------------------------------------------------------------
        // Metadata

        actionMetadata = new ContextAction(AveragingFFTToolbarInterface.ACTION_NAME_METADATA,
                                           RegistryModelUtilities.getCommonIcon(UIComponentPlugin.FILENAME_ICON_METADATA_EDITOR),
                                           AveragingFFTToolbarInterface.ACTION_TOOLTIP_METADATA,
                                           KeyEvent.VK_M,
                                           false,
                                           true)
            {
            static final long serialVersionUID = -307436773020050527L;
            static final String SOURCE = "ContextAction:Metadata ";


            public void actionPerformed(final ActionEvent event)
                {
                System.out.println("METADATA!!!!");
                }
            };

        ffttoolbar.getMetadataButton().setAction(actionMetadata);
        ffttoolbar.getMetadataButton().setBorderPainted(false);
        ffttoolbar.getMetadataButton().setBorder(UIComponentPlugin.BORDER_BUTTON);
        ffttoolbar.getMetadataButton().setHideActionText(true);
        ffttoolbar.getMetadataButton().setToolTipText((String) actionMetadata.getValue(Action.SHORT_DESCRIPTION));
        ffttoolbar.getMetadataButton().setEnabled(true);

        //-------------------------------------------------------------------------------------
        // Export

        actionExport = new ContextAction(AveragingFFTToolbarInterface.ACTION_NAME_EXPORT,
                                         RegistryModelUtilities.getCommonIcon(UIComponentPlugin.FILENAME_ICON_SAVE_AS_FILE),
                                         AveragingFFTToolbarInterface.ACTION_TOOLTIP_EXPORT,
                                         KeyEvent.VK_X,
                                         false,
                                         true)
            {
            final static String SOURCE = "ContextAction:Export ";
            private static final long serialVersionUID = -598953341739066982L;


            public void actionPerformed(final ActionEvent event)
                {
                if (hostui != null)
                    {
                    final JFileChooser chooser;
                    final int intStatus;

                    chooser = new JFileChooser();
                    chooser.setDialogTitle("Save data in a file");
                    chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                    chooser.setForeground(colourforeground.getColor());

                    chooser.setApproveButtonText(ChooserInterface.BUTTON_SELECT);
                    chooser.setApproveButtonToolTipText("Enter the filename");

                    Utilities.setContainerHierarchyFonts(chooser.getComponents(),
                                                         UIComponentPlugin.DEFAULT_FONT);

                    Utilities.setContainerHierarchyColours(chooser.getComponents(),
                                                           colourforeground,
                                                           null);

                    intStatus = chooser.showSaveDialog((Component) hostui);

                    if (intStatus == JFileChooser.APPROVE_OPTION)
                        {
                        final File file;

                        file = chooser.getSelectedFile();

                        if (file != null)
                            {
                            AwaitingDevelopment.showAwaitingDevelopment("File Save is under development!");
                            }
                        else
                            {
                            LOGGER.error(SOURCE + "File unexpectedly NULL");
                            }
                        }
                    }
                else
                    {
                    LOGGER.error(SOURCE + "UI or TableModel unexpectedly NULL");
                    }
                }
            };

        ffttoolbar.getExportButton().setAction(actionExport);
        ffttoolbar.getExportButton().setBorderPainted(false);
        ffttoolbar.getExportButton().setBorder(UIComponentPlugin.BORDER_BUTTON);
        ffttoolbar.getExportButton().setHideActionText(true);
        ffttoolbar.getExportButton().setToolTipText((String) actionExport.getValue(Action.SHORT_DESCRIPTION));
        ffttoolbar.getExportButton().setEnabled(true);

        //-------------------------------------------------------------------------------------
        // Page Setup

        actionPageSetup = new ContextAction(ReportTablePlugin.PREFIX_PAGE_SETUP + ffttoolbar.getTitle(),
                                            RegistryModelUtilities.getCommonIcon(UIComponentPlugin.FILENAME_ICON_PAGE_SETUP),
                                            ReportTablePlugin.PREFIX_PAGE_SETUP + ffttoolbar.getTitle(),
                                            KeyEvent.VK_S,
                                            false,
                                            true)
            {
            static final long serialVersionUID = 6802400471966299436L;
            static final String SOURCE = "ContextAction:PageSetup ";

            public void actionPerformed(final ActionEvent event)
                {
                if (hostframe != null)
                    {
                    final PrinterJob printerJob;
                    final PageFormat pageFormat;

                    printerJob = PrinterJob.getPrinterJob();
                    pageFormat = printerJob.pageDialog(hostframe.getPageFormat());

                    if (pageFormat != null)
                        {
                        hostframe.setPageFormat(pageFormat);
                        }
                    }
                else
                    {
                    LOGGER.error(SOURCE + "UIComponent unexpectedly NULL");
                    }
                }
            };

        ffttoolbar.getPageSetupButton().setAction(actionPageSetup);
        ffttoolbar.getPageSetupButton().setBorderPainted(false);
        ffttoolbar.getPageSetupButton().setBorder(UIComponentPlugin.BORDER_BUTTON);
        ffttoolbar.getPageSetupButton().setHideActionText(true);
        ffttoolbar.getPageSetupButton().setToolTipText((String) actionPageSetup.getValue(Action.SHORT_DESCRIPTION));
        ffttoolbar.getPageSetupButton().setEnabled(true);

        //-------------------------------------------------------------------------------------
        // Printing

        // ToDo DEFAULT Header & Footer MessageFormats
        // ToDo See: http://java-sl.com/JEditorPanePrinter.html

        actionPrint = new ContextAction(ReportTablePlugin.PREFIX_PRINT + ffttoolbar.getTitle(),
                                        RegistryModelUtilities.getCommonIcon(UIComponentPlugin.FILENAME_ICON_PRINT),
                                        ReportTablePlugin.PREFIX_PRINT + ffttoolbar.getTitle(),
                                        KeyEvent.VK_P,
                                        false,
                                        true)
            {
            static final long serialVersionUID = 8346968631811861938L;
            static final String SOURCE = "ContextAction:Print ";

            public void actionPerformed(final ActionEvent event)
                {
                final SwingWorker workerPrinter;

                workerPrinter = new SwingWorker(REGISTRY.getThreadGroup(),
                                                "SwingWorker Printer")
                    {
                    public Object construct()
                        {
                        LOGGER.debug(hostframe.isDebug(), SOURCE + "SwingWorker construct()");

                        // Let the user know what happened
                        return (printDialog());
                        }


                    // Display updates occur on the Event Dispatching Thread
                    public void finished()
                        {
                        final String[] strSuccess =
                            {
                            "The panel is ready for printing",
                            UIComponentPlugin.MSG_PRINT_CANCELLED
                            };

                        if ((get() != null)
                            && (get() instanceof Boolean)
                            && ((Boolean) get())
                            && (!isStopping()))
                            {
                            JOptionPane.showMessageDialog(null,
                                                          strSuccess[0],
                                                          "Print Panel",
                                                          JOptionPane.INFORMATION_MESSAGE,
                                                          RegistryModelUtilities.getCommonIcon(UIComponentPlugin.FILENAME_ICON_DIALOG_PRINT));
                            }
                        else
                            {
                            JOptionPane.showMessageDialog(null,
                                                          strSuccess[1],
                                                          "Print Panel",
                                                          JOptionPane.INFORMATION_MESSAGE,
                                                          RegistryModelUtilities.getCommonIcon(UIComponentPlugin.FILENAME_ICON_DIALOG_PRINT));
                            }
                        }
                    };

                // Start the Print Thread
                workerPrinter.start();
                }


            /*********************************************************************************
             * Show the Print dialog.
             *
             * @return boolean
             */

            private boolean printDialog()
                {
                final boolean boolSuccess;

                // Check to see that we actually have a printer...
                if (PrinterJob.lookupPrintServices().length == 0)
                    {
                    JOptionPane.showMessageDialog(null,
                                                  ReportTablePlugin.MSG_NO_PRINTER,
                                                  ReportTablePlugin.PREFIX_PRINT + ffttoolbar.getTitle(),
                                                  JOptionPane.WARNING_MESSAGE,
                                                  RegistryModelUtilities.getCommonIcon(UIComponentPlugin.FILENAME_ICON_DIALOG_PRINT));
                    boolSuccess = false;
                    }
                else
                    {
                    if ((hostframe != null)
                        && (hostframe.getFFTViewer().getCanvas() != null))
                        {
                        final PageFormat pageFormat;

                        pageFormat = hostframe.getPageFormat();

                        if (pageFormat != null)
                            {
                            // ToDo Header & Footer MessageFormats
                            boolSuccess = PrintUtilities.printComponent(hostframe.getFFTViewer().getCanvas().getPrintableComponent(),
                                                                        pageFormat);
                            }
                        else
                            {
                            boolSuccess = false;
                            }
                        }
                    else
                        {
                        boolSuccess = false;
                        }
                    }

                return (boolSuccess);
                }
            };

        ffttoolbar.getPrintButton().setAction(actionPrint);
        ffttoolbar.getPrintButton().setBorderPainted(false);
        ffttoolbar.getPrintButton().setBorder(UIComponentPlugin.BORDER_BUTTON);
        ffttoolbar.getPrintButton().setHideActionText(true);
        ffttoolbar.getPrintButton().setToolTipText((String) actionPrint.getValue(Action.SHORT_DESCRIPTION));
        ffttoolbar.getPrintButton().setEnabled(true);

        //-------------------------------------------------------------------------------------
        // Help

        actionHelp = new ContextAction(AveragingFFTToolbarInterface.ACTION_NAME_HELP,
                                       RegistryModelUtilities.getCommonIcon(UIComponentPlugin.FILENAME_ICON_HELP),
                                       AveragingFFTToolbarInterface.ACTION_TOOLTIP_HELP,
                                       KeyEvent.VK_H,
                                       false,
                                       true)
            {
            static final long serialVersionUID = 6802400471966299436L;
            static final String SOURCE = "ContextAction:Help ";

            public void actionPerformed(final ActionEvent event)
                {
                if (hostframe != null)
                    {
                    System.out.println("HELP!!!!");
                    }
                else
                    {
                    LOGGER.error(SOURCE + "UIComponent unexpectedly NULL");
                    }
                }
            };

        ffttoolbar.getHelpButton().setAction(actionHelp);
        ffttoolbar.getHelpButton().setBorderPainted(false);
        ffttoolbar.getHelpButton().setBorder(UIComponentPlugin.BORDER_BUTTON);
        ffttoolbar.getHelpButton().setHideActionText(true);
        ffttoolbar.getHelpButton().setToolTipText((String) actionHelp.getValue(Action.SHORT_DESCRIPTION));
        ffttoolbar.getHelpButton().setEnabled(true);

        //-------------------------------------------------------------------------------------
        // Put it all together

        listComponents.clear();

        listComponents.add(new JToolBar.Separator(UIComponentPlugin.DIM_TOOLBAR_SEPARATOR_BUTTON));
        listComponents.add(labelName);
        listComponents.add(new JToolBar.Separator(UIComponentPlugin.DIM_TOOLBAR_SEPARATOR));

        listComponents.add(Box.createHorizontalGlue());

        listComponents.add(ffttoolbar.getOpenFileButton());
        listComponents.add(new JToolBar.Separator(UIComponentPlugin.DIM_TOOLBAR_SEPARATOR));
        listComponents.add(ffttoolbar.getPlayPauseButton());
        listComponents.add(new JToolBar.Separator(UIComponentPlugin.DIM_TOOLBAR_SEPARATOR));
        listComponents.add(ffttoolbar.getStopButton());
        listComponents.add(new JToolBar.Separator(UIComponentPlugin.DIM_TOOLBAR_SEPARATOR));
        listComponents.add(ffttoolbar.getProgressBar());
        listComponents.add(new JToolBar.Separator(UIComponentPlugin.DIM_TOOLBAR_SEPARATOR));
        listComponents.add(new JToolBar.Separator(UIComponentPlugin.DIM_TOOLBAR_SEPARATOR));
        listComponents.add(new JToolBar.Separator(UIComponentPlugin.DIM_TOOLBAR_SEPARATOR));
        listComponents.add(ffttoolbar.getMetadataButton());
        listComponents.add(new JToolBar.Separator(UIComponentPlugin.DIM_TOOLBAR_SEPARATOR));
        listComponents.add(ffttoolbar.getExportButton());
        listComponents.add(new JToolBar.Separator(UIComponentPlugin.DIM_TOOLBAR_SEPARATOR));
        listComponents.add(ffttoolbar.getPageSetupButton());
        listComponents.add(new JToolBar.Separator(UIComponentPlugin.DIM_TOOLBAR_SEPARATOR));
        listComponents.add(ffttoolbar.getPrintButton());
        listComponents.add(new JToolBar.Separator(UIComponentPlugin.DIM_TOOLBAR_SEPARATOR));
        listComponents.add(ffttoolbar.getHelpButton());
        listComponents.add(new JToolBar.Separator(UIComponentPlugin.DIM_TOOLBAR_SEPARATOR));

        return (listComponents);
        }
    }
