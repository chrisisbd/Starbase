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


import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.common.datafilters.DataFilterHelper;
import org.lmn.fc.common.datafilters.DataFilterInterface;
import org.lmn.fc.common.datafilters.DataFilterType;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ObservatoryInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.ephemerides.EphemerisDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.utilities.Epoch;
import org.lmn.fc.frameworks.starbase.plugins.observatory.audio.OscillatorWaveform;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.InstrumentUIComponentDecoratorInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandProcessorContextInterface;
import org.lmn.fc.model.plugins.AtomPlugin;
import org.lmn.fc.model.registry.RegistryModelUtilities;
import org.lmn.fc.model.xmlbeans.instruments.Controller;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;
import org.lmn.fc.model.xmlbeans.instruments.ParameterType;
import org.lmn.fc.model.xmlbeans.instruments.PluginType;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;


/***************************************************************************************************
 * ParameterListBuilder.
 * ToDo Sort out the duplication... one day
 */

public final class ParameterListBuilder implements FrameworkConstants,
                                                   FrameworkStrings,
                                                   FrameworkMetadata,
                                                   FrameworkSingletons
    {
    // String Resources

    // This should really be part of an 'Unset Parameter' enum?
    public static final String NO_AUDIO_MIXERS = "No Audio Mixers";

    private static final String SUFFIX_FILTER_HELP = "FilterHelp.html";
    private static final String OSCILLATOR_WAVEFORM_HELP = "audio/help/OscillatorWaveformHelp.html";


    /***********************************************************************************************
     * Build the List of Plugins to add to the choices drop-down.
     *
     * @param context
     * @param choices
     */

    public static void buildPluginList(final CommandProcessorContextInterface context,
                                       final List<String> choices)
        {
        final String SOURCE = "ParameterListBuilder.buildPluginList() ";

        if ((context != null)
            && (context.getInstrument() != null)
            && (CommandProcessorUtilities.hasController(context))
            && (choices != null))
            {
            final Controller controller;

            controller = context.getInstrument().getController();

            // Now add Choices for each of the Controller's Plugins
            if ((controller.getPluginList() != null)
                && (!controller.getPluginList().isEmpty()))
                {
                final List<PluginType> plugins;

                plugins = controller.getPluginList();

                if ((plugins != null)
                    && (!plugins.isEmpty()))
                    {
                    final StringBuffer bufferChoice;
                    final Iterator<PluginType> iterPlugins;

                    bufferChoice = new StringBuffer(25);
                    iterPlugins = plugins.iterator();

                    while (iterPlugins.hasNext())
                        {
                        final PluginType plugin;

                        plugin = iterPlugins.next();
                        bufferChoice.setLength(0);

                        if (plugin != null)
                            {
                            bufferChoice.append(plugin.getIdentifier());
                            bufferChoice.append(ParameterChoiceToken.CHOICE_SUFFIX_MODULE);
                            choices.add(bufferChoice.toString());
                            }
                        }
                    }
                }
            else
                {
                // No Controller Plugins were found, which is acceptable
                }
            }
        else
            {
            // No Controller was found, which is acceptable
            }
        }


    /***********************************************************************************************
     * Build the List of Plugins to add to the choices drop-down.
     *
     * @param instrument
     * @param choices
     */

    public static void buildPluginList(final Instrument instrument,
                                       final List<String> choices)
        {
        final String SOURCE = "ParameterHelper.buildPluginList() ";

        if ((instrument != null)
            && (CommandProcessorUtilities.hasController(instrument))
            && (choices != null))
            {
            final Controller controller;

            controller = instrument.getController();

            // Now add Choices for each of the Controller's Plugins
            if ((controller.getPluginList() != null)
                && (!controller.getPluginList().isEmpty()))
                {
                final List<PluginType> plugins;

                plugins = controller.getPluginList();

                if ((plugins != null)
                    && (!plugins.isEmpty()))
                    {
                    final StringBuffer bufferChoice;
                    final Iterator<PluginType> iterPlugins;

                    bufferChoice = new StringBuffer(25);
                    iterPlugins = plugins.iterator();

                    while (iterPlugins.hasNext())
                        {
                        final PluginType plugin;

                        plugin = iterPlugins.next();
                        bufferChoice.setLength(0);

                        if (plugin != null)
                            {
                            bufferChoice.append(plugin.getIdentifier());
                            bufferChoice.append(ParameterChoiceToken.CHOICE_SUFFIX_MODULE);
                            choices.add(bufferChoice.toString());
                            }
                        }
                    }
                }
            else
                {
                // No Controller Plugins were found, which is acceptable
                }
            }
        else
            {
            // No Controller was found, which is acceptable
            }
        }


    /***********************************************************************************************
     * Build the List of Epochs to add to the choices drop-down.
     *
     * @param context
     * @param choices
     */

    public static void buildEpochList(final CommandProcessorContextInterface context,
                                      final List<String> choices)
        {
        final String SOURCE = "ParameterListBuilder.buildEpochList() ";

        if ((context != null)
            && (choices != null))
            {
            final Epoch[] arrayEpoch;

            arrayEpoch = Epoch.values();

            for (int intEpochIndex = 0;
                 intEpochIndex < arrayEpoch.length;
                 intEpochIndex++)
                {
                final Epoch epoch;

                epoch = arrayEpoch[intEpochIndex];

                // Leave the ordering as in the enum
                choices.add(epoch.getName());
                }
            }
        }


    /***********************************************************************************************
     * Build the List of Epochs to add to the choices drop-down.
     *
     * @param choices
     */

    public static void buildEpochList(final List<String> choices)
        {
        final String SOURCE = "ParameterHelper.buildEpochList() ";

        if (choices != null)
            {
            final Epoch[] arrayEpoch;

            arrayEpoch = Epoch.values();

            for (int intEpochIndex = 0;
                 intEpochIndex < arrayEpoch.length;
                 intEpochIndex++)
                {
                final Epoch epoch;

                epoch = arrayEpoch[intEpochIndex];

                // Leave the ordering as in the enum
                choices.add(epoch.getName());
                }
            }
        }


    /***********************************************************************************************
     * Build the List of ExportableTabs to add to the choices drop-down.
     *
     * @param context
     * @param choices
     */

    public static void buildExportableTabsList(final CommandProcessorContextInterface context,
                                               final List<String> choices)
        {
        final String SOURCE = "ParameterListBuilder.buildExportableTabsList() ";

        if ((context != null)
            && (context.getObservatoryInstrument() != null)
            && (context.getObservatoryInstrument().getInstrumentPanel() != null)
            && (context.getObservatoryInstrument().getInstrumentPanel().getExportableTabs() != null)
            && (!context.getObservatoryInstrument().getInstrumentPanel().getExportableTabs().isEmpty())
            && (choices != null))
            {
            final List<String> listExportableTabs;

            listExportableTabs = context.getObservatoryInstrument().getInstrumentPanel().getExportableTabs();

            for (int intTabIndex = 0;
                 intTabIndex < listExportableTabs.size();
                 intTabIndex++)
                {
                final String strTabName;

                strTabName = listExportableTabs.get(intTabIndex);
                choices.add(strTabName);
                }
            }
        }


    /***********************************************************************************************
     * Build the List of ExportableTabs to add to the choices drop-down.
     *
     * @param instrumentpanel
     * @param choices
     */

    public static void buildExportableTabsList(final InstrumentUIComponentDecoratorInterface instrumentpanel,
                                               final List<String> choices)
        {
        final String SOURCE = "ParameterHelper.buildExportableTabsList() ";

        if ((instrumentpanel != null)
            && (instrumentpanel.getExportableTabs() != null)
            && (!instrumentpanel.getExportableTabs().isEmpty())
            && (choices != null))
            {
            final List<String> listExportableTabs;

            listExportableTabs = instrumentpanel.getExportableTabs();

            for (int intTabIndex = 0;
                 intTabIndex < listExportableTabs.size();
                 intTabIndex++)
                {
                final String strTabName;

                strTabName = listExportableTabs.get(intTabIndex);
                choices.add(strTabName);
                }
            }
        }


    /***********************************************************************************************
     * Build the List of Mixers with SourceLines to add to the choices drop-down.
     *
     * @param context
     * @param choices
     */

    public static void buildMixerSourceLineList(final CommandProcessorContextInterface context,
                                                final List<String> choices)
        {
        final String SOURCE = "ParameterListBuilder.buildMixerSourceLineList() ";

        if ((context != null)
            && (choices != null))
            {
            final Mixer.Info[] arrayMixerInfo;

            arrayMixerInfo = AudioSystem.getMixerInfo();

            if (arrayMixerInfo != null)
                {
                for (int intMixerIndex = 0;
                     intMixerIndex < arrayMixerInfo.length;
                     intMixerIndex++)
                    {
                    final Mixer.Info mixerInfo;
                    final Mixer mixer;

                    mixerInfo = arrayMixerInfo[intMixerIndex];

                    mixer = AudioSystem.getMixer(mixerInfo);

                    // TODO REVIEW: We only need Mixer Inputs, i.e. SourceLines
                    if ((mixer.getSourceLineInfo() != null)
                        && (mixer.getSourceLineInfo().length > 0))
                        {
                        choices.add(mixerInfo.getName());
                        }
                    }
                }

            if (choices.isEmpty())
                {
                // Prevent comand execution if there are no mixers
                choices.add(NO_AUDIO_MIXERS);
                }
            else
                {
                // Sort the Mixers alphabetically to improve the readability
                Collections.sort(choices);
                }
            }
        }


    /***********************************************************************************************
     * Build the List of Mixers with SourceLines to add to the choices drop-down.
     *
     * @param choices
     */

    public static void buildMixerSourceLineList(final List<String> choices)
        {
        final String SOURCE = "ParameterHelper.buildMixerSourceLineList() ";

        if (choices != null)
            {
            final Mixer.Info[] arrayMixerInfo;

            arrayMixerInfo = AudioSystem.getMixerInfo();

            if (arrayMixerInfo != null)
                {
                for (int intMixerIndex = 0;
                     intMixerIndex < arrayMixerInfo.length;
                     intMixerIndex++)
                    {
                    final Mixer.Info mixerInfo;
                    final Mixer mixer;

                    mixerInfo = arrayMixerInfo[intMixerIndex];

                    mixer = AudioSystem.getMixer(mixerInfo);

                    // TODO REVIEW: We only need Mixer Inputs, i.e. SourceLines
                    if ((mixer.getSourceLineInfo() != null)
                        && (mixer.getSourceLineInfo().length > 0))
                        {
                        choices.add(mixerInfo.getName());
                        }
                    }
                }

            if (choices.isEmpty())
                {
                // Prevent comand execution if there are no mixers
                choices.add(NO_AUDIO_MIXERS);
                }
            else
                {
                // Sort the Mixers alphabetically to improve the readability
                Collections.sort(choices);
                }
            }
        }


    /***********************************************************************************************
     * Build the List of Ephemeris Targets to add to the choices drop-down.
     *
     * @param context
     * @param choices
     */

    public static void buildEphemerisTargets(final CommandProcessorContextInterface context,
                                             final List<String> choices)
        {
        final String SOURCE = "ParameterListBuilder.buildEphemerisTargets() ";

        if ((context != null)
            && (context.getObservatory() != null)
            && (choices != null))
            {
            final Hashtable<String, EphemerisDAOInterface> tableDAOs;

            tableDAOs = context.getObservatory().getEphemerisDaoTable();

            if ((tableDAOs != null)
                && (!tableDAOs.isEmpty()))
                {
                final Enumeration<String> keys;
                final List<String> listKeys;

                keys = tableDAOs.keys();
                listKeys = new ArrayList<String>(tableDAOs.size());

                while (keys.hasMoreElements())
                    {
                    listKeys.add(keys.nextElement());
                    }

                // Sort the Keys alphabetically to improve the readability
                Collections.sort(listKeys);

                for (int i = 0;
                     i < listKeys.size();
                     i++)
                    {
                    choices.add(listKeys.get(i));
                    }
                }
            }
        }


    /***********************************************************************************************
     * Build the List of Ephemeris Targets to add to the choices drop-down.
     *
     * @param observatory
     * @param choices
     */

    public static void buildEphemerisTargets(final ObservatoryInterface observatory,
                                             final List<String> choices)
        {
        final String SOURCE = "ParameterHelper.buildEphemerisTargets() ";

        if ((observatory != null)
            && (choices != null))
            {
            final Hashtable<String, EphemerisDAOInterface> tableDAOs;

            tableDAOs = observatory.getEphemerisDaoTable();

            if ((tableDAOs != null)
                && (!tableDAOs.isEmpty()))
                {
                final Enumeration<String> keys;
                final List<String> listKeys;

                keys = tableDAOs.keys();
                listKeys = new ArrayList<String>(tableDAOs.size());

                while (keys.hasMoreElements())
                    {
                    listKeys.add(keys.nextElement());
                    }

                // Sort the Keys alphabetically to improve the readability
                Collections.sort(listKeys);

                for (int i = 0;
                     i < listKeys.size();
                     i++)
                    {
                    choices.add(listKeys.get(i));
                    }
                }
            }
        }


    /***********************************************************************************************
     * Build the List of Filters to add to the choices drop-down.
     * See if the currently selected Filter has any Parameters to be displayed following the drop-down.
     * Return a list containing the URL to use for Help for the selected item in the Filter List.
     *
     * @param context
     * @param choices
     * @param parameter
     * @param helpfileurls
     *
     * @return List<ParameterType>
     */

    public static List<ParameterType> buildFilterList(final CommandProcessorContextInterface context,
                                                      final List<String> choices,
                                                      final ParameterType parameter,
                                                      final List<URL> helpfileurls)
        {
        final String SOURCE = "ParameterListBuilder.buildFilterList() ";
        List<ParameterType> listSubParameters;

        listSubParameters = null;
        helpfileurls.clear();

        if ((context != null)
            && (choices != null)
            && (parameter != null))
            {
            final DataFilterType[] arrayFilterTypes;

            // ToDo Load Filters at run time, do not assume types via the enum
            arrayFilterTypes = DataFilterType.values();

            for (int intFilterIndex = 0;
                 intFilterIndex < arrayFilterTypes.length;
                 intFilterIndex++)
                {
                final DataFilterType dataFilterType;
                final DataFilterInterface filter;

                // Build the complete list of Filters to choose from
                dataFilterType = arrayFilterTypes[intFilterIndex];
                choices.add(dataFilterType.getName());

                filter = DataFilterHelper.instantiateFilter(dataFilterType.getFilterClassname());

                if (filter != null)
                    {
                    final URL urlHelp;

                    // ToDo REVIEW: This resets the Parameter Value to the default, every time
                    filter.initialiseFilter();

                    // See if the selected Filter has any Parameters to be displayed following the drop-down
                    if ((parameter.getValue() != null)
                        && (!FrameworkStrings.EMPTY_STRING.equals(parameter.getValue()))
                        && (parameter.getValue().equals(dataFilterType.getName())))
                        {
                        final List<ParameterType> listFilterParameters;

                        listFilterParameters = filter.getParameters();
                        listSubParameters = new ArrayList<ParameterType>(listFilterParameters.size());

                        for (int intParameterIndex = 0;
                             intParameterIndex < listFilterParameters.size();
                             intParameterIndex++)
                            {
                            final ParameterType parameterType;

                            parameterType = listFilterParameters.get(intParameterIndex);

                            if (parameterType != null)
                                {
                                // We need to copy the Parameters,
                                // because Filter.dispose() would clear the direct List
                                listSubParameters.add((ParameterType)parameterType.copy());
                                }
                            }
                        }

                    // Build the filename of the associated Helpfile from the Filter package name
                    // Package: org.lmn.fc.common.datafilters.impl.passthroughfilter
                    // Help URL: file:/C:/Java/Starbase/dist/libraries/PassThroughFilter.jar!/org/lmn/fc/common/datafilters/impl/passthroughfilter/PassThroughFilterHelp.html
                    urlHelp = RegistryModelUtilities.getFileURLForClass(filter.getClass(),
                                                                        filter.getFilterType().getName() + SUFFIX_FILTER_HELP);
                    if (urlHelp != null)
                        {
                        helpfileurls.add(urlHelp);
                        }
                    else
                        {
                        // Keep the List index in step
                        try
                            {
                            helpfileurls.add(new URL("http://www.ukraa.com"));
                            }

                        catch (MalformedURLException exception)
                            {
                            LOGGER.error(SOURCE + "Unable to form a suitable URL, the Parameter Help will be incorrect");
                            }
                        }

                    // This clears the DatasetTypes, Parameters and Metadata
                    filter.disposeFilter();
                    }
                }
            }

        return (listSubParameters);
        }


    /***********************************************************************************************
     * Build the List of Filters to add to the choices drop-down.
     * See if the currently selected Filter has any Parameters to be displayed following the drop-down.
     *
     * @param choices
     * @param parameter
     *
     * @return List<ParameterType>
     */

    public static List<ParameterType> buildFilterList(final List<String> choices,
                                                      final ParameterType parameter)
        {
        final String SOURCE = "ParameterHelper.buildFilterList() ";
        List<ParameterType> listSubParameters;

        listSubParameters = null;

        if ((choices != null)
            && (parameter != null))
            {
            final DataFilterType[] arrayFilters;

            // ToDo Load Filters at run time, do not assume types via the enum
            arrayFilters = DataFilterType.values();

            for (int intFilterIndex = 0;
                 intFilterIndex < arrayFilters.length;
                 intFilterIndex++)
                {
                final DataFilterType filter;

                filter = arrayFilters[intFilterIndex];
                choices.add(filter.getName());

                // See if this Filter has any Parameters to be displayed following the drop-down
                if ((parameter.getValue() != null)
                    && (!FrameworkStrings.EMPTY_STRING.equals(parameter.getValue()))
                    && (parameter.getValue().equals(filter.getName())))
                    {
                    final DataFilterInterface filterSelected;

                    // This instance is only used to build the UI
                    filterSelected = DataFilterHelper.instantiateFilter(filter.getFilterClassname());

                    if (filterSelected != null)
                        {
                        // ToDo REVIEW: This resets the Parameter Value to the default, every time
                        filterSelected.initialiseFilter();
                        listSubParameters = filterSelected.getParameters();
                        // TODO dispose??
                        }
                    }
                }
            }

        return (listSubParameters);
        }


    /***********************************************************************************************
     * Expand the List to include all OscillatorWaveforms.
     *
     * @param plugin
     * @param choices
     * @param helpfileurls
     */

    public static void buildOscillatorWaveformList(final AtomPlugin plugin,
                                                   final List<String> choices,
                                                   final List<URL> helpfileurls)
        {
        final String SOURCE = "ParameterListBuilder.buildOscillatorWaveformList() ";

        if ((plugin != null)
            && (choices != null))
            {
            final OscillatorWaveform[] arrayWaveforms;

            arrayWaveforms = OscillatorWaveform.values();

            for (int intWaveformIndex = 0;
                 intWaveformIndex < arrayWaveforms.length;
                 intWaveformIndex++)
                {
                final OscillatorWaveform waveform;

                waveform = arrayWaveforms[intWaveformIndex];

                // Leave the ordering as in the enum
                choices.add(waveform.getWaveformType());

                if (helpfileurls != null)
                    {
                    final URL urlHelp;

                    // Build the filename of the associated Helpfile from the OscillatorWaveform name
                    // Help URL: file:/C:/Java/Starbase/dist/plugins/observatory/Observatory-plugin.jar!/org/lmn/fc/frameworks/starbase/plugins/observatory/audio/help/OscillatorWaveformHelp.html#GaussianNoise
                    urlHelp = RegistryModelUtilities.getFileURLForClass(plugin.getClass(),
                                                                        OSCILLATOR_WAVEFORM_HELP);
                                                                        // Why doesn't this work? OSCILLATOR_WAVEFORM_HELP + "#" + waveform.getWaveformType());
                    if (urlHelp != null)
                        {
                        helpfileurls.add(urlHelp);
                        }
                    else
                        {
                        // Keep the List index in step
                        try
                            {
                            helpfileurls.add(new URL("http://www.ukraa.com"));
                            }

                        catch (final MalformedURLException exception)
                            {
                            LOGGER.error(SOURCE + "Unable to form a suitable URL, the Parameter Help will be incorrect");
                            }
                        }
                    }
                }
            }
        }
    }
