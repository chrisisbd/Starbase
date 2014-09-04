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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.audio;

import org.lmn.fc.common.constants.*;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.AudioExplorerExpanderDataInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.AudioExplorerLeafDataInterface;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;
import org.lmn.fc.ui.UIComponentPlugin;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.sampled.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.util.List;


/***************************************************************************************************
 * AudioExplorerTreeBuilder.
 *
 * NOTE!
 * The original of this code is by Andrew Thompson http://pscode.org
 * as published at: http://stackoverflow.com/questions/5304001/javasound-mixer-with-both-ports-and-datalines
 *
 * This package is an extensive rework to get things working in a Starbase way.
 */

public final class AudioExplorerTreeBuilder implements FrameworkConstants,
                                                       FrameworkStrings,
                                                       FrameworkMetadata,
                                                       FrameworkSingletons,
                                                       FrameworkRegex,
                                                       ResourceKeys
    {
    // String Resources
    private static final String NODE_AUDIO         = "Audio";
    private static final String NODE_AUDIO_SAMPLED = "Sampled";
    private static final String NODE_MIXERS        = "Mixers";
    private static final String NODE_MIDI          = "MIDI";
    private static final String NODE_SUFFIXES      = "Suffixes";

    private static final String ICON_AUDIO         = "soundexplorer-sound.png";
    private static final String ICON_SAMPLED       = "soundexplorer-sampled.png";
    private static final String ICON_MIXERS        = "soundexplorer-mixers.png";
    private static final String ICON_MIXER         = "soundexplorer-mixer.png";
    private static final String ICON_MIXER_CONTROL = "soundexplorer-mixer-control.png";
    private static final String ICON_MIDI          = "soundexplorer-midi.png";
    private static final String ICON_MIDI_INFO     = "soundexplorer-midi-info.png";
    private static final String ICON_SUFFIXES      = "soundexplorer-suffixes.png";

    // Input to Port
    private static final String ICON_MICROPHONE   = "soundexplorer-microphone.png";
    private static final String ICON_LINE_INPUT   = "soundexplorer-line-input.png";
    private static final String ICON_COMPACT_DISC = "soundexplorer-compact-disc.png";

    // Output from Port
    private static final String ICON_LOUDSPEAKER = "soundexplorer-loudspeaker.png";
    private static final String ICON_HEADPHONE   = "soundexplorer-headphone.png";
    private static final String ICON_LINE_OUTPUT = "soundexplorer-line-output.png";

    private static final String ICON_UNKNOWN = "soundexplorer-unknown.png";

    private static final String MSG_NOT_SPECIFIED = "Not specified, not defined, or unrestricted";


    /***********************************************************************************************
     * Build the tree of Audio Device nodes using the expander and leaf UIComponents.
     *
     * @param expanderui
     * @param leafui
     * @param metadatalist
     *
     * @return DefaultMutableTreeNode
     */

    public static DefaultMutableTreeNode buildExplorerTreeNodes(final UIComponentPlugin expanderui,
                                                                final UIComponentPlugin leafui,
                                                                final List<Metadata> metadatalist)
        {
        final DefaultMutableTreeNode nodeRoot;
        final DefaultMutableTreeNode nodeAudioSampled;
        final DefaultMutableTreeNode nodeAudioSampledSuffixes;
        final DefaultMutableTreeNode nodeAudioSampledMixers;
        final DefaultMutableTreeNode nodeAudioMIDI;

        final Object[][] arrayAudioTypes;
        final Mixer.Info[] arrayMixerInfo;
        final String[][] arrayMixerData;
        final int[] arrayMidiTypes;
        final MidiDevice.Info[] arrayMidiDeviceInfo;

        AudioExplorerExpanderDataInterface expanderData;
        AudioExplorerLeafDataInterface leafData;
        Object[][] arrayData;
        String[] arrayColumnNames;

        //-----------------------------------------------------------------------------------------
        // Create the Root Node UserObject as an Expander

        nodeRoot = new DefaultMutableTreeNode();
        expanderData = new AudioExplorerExpanderData(NODE_AUDIO,
                                                     NODE_AUDIO,
                                                     null,
                                                     null,
                                                     ICON_AUDIO,
                                                     nodeRoot,
                                                     expanderui);
        expanderData.setChildMetadata(metadatalist);

        //-----------------------------------------------------------------------------------------
        // Expander Audio:Sampled (as opposed to MIDI)

        nodeAudioSampled = new DefaultMutableTreeNode();
        expanderData = new AudioExplorerExpanderData("Audio.Sampled",
                                                     NODE_AUDIO_SAMPLED,
                                                     null,
                                                     null,
                                                     ICON_SAMPLED,
                                                     nodeAudioSampled,
                                                     expanderui);
        expanderData.setChildMetadata(metadatalist);
        nodeRoot.add(nodeAudioSampled);

        //-----------------------------------------------------------------------------------------
        // Expander Audio:Sampled:Mixers

        arrayMixerInfo = AudioSystem.getMixerInfo();
        arrayMixerData = new String[arrayMixerInfo.length][4];

        for (int intMixerInfoIndex = 0;
             intMixerInfoIndex < arrayMixerData.length;
             intMixerInfoIndex++)
            {
            arrayMixerData[intMixerInfoIndex][0] = arrayMixerInfo[intMixerInfoIndex].getName();
            arrayMixerData[intMixerInfoIndex][1] = arrayMixerInfo[intMixerInfoIndex].getVendor();
            arrayMixerData[intMixerInfoIndex][2] = arrayMixerInfo[intMixerInfoIndex].getVersion();
            arrayMixerData[intMixerInfoIndex][3] = arrayMixerInfo[intMixerInfoIndex].getDescription();
            }

        arrayColumnNames = new String[]{"Name",
                                        "Vendor",
                                        "Version",
                                        "Description"};

        nodeAudioSampledMixers = new DefaultMutableTreeNode();
        expanderData = new AudioExplorerExpanderData("Audio.Sampled.Mixers",
                                                     NODE_MIXERS,
                                                     arrayMixerData,
                                                     arrayColumnNames,
                                                     ICON_MIXERS,
                                                     nodeAudioSampledMixers,
                                                     expanderui);
        expanderData.setChildMetadata(metadatalist);
        nodeAudioSampled.add(nodeAudioSampledMixers);

        //-----------------------------------------------------------------------------------------
        // List all Mixers

        for (int intMixerInfoIndex = 0;
             intMixerInfoIndex < arrayMixerInfo.length;
             intMixerInfoIndex++)
            {
            try
                {
                final Mixer mixer;
                final Line.Info[] arraySourceLineInfo;
                final Line.Info[] arrayTargetLineInfo;
                final DefaultMutableTreeNode nodeAudioSampledMixer;
                final DefaultMutableTreeNode nodeSourceLines;
                final DefaultMutableTreeNode nodeTargetLines;
                final Line[] arraySourceLines;
                final Line[] arrayTargetLines;

                // Expander Audio:Sampled:Mixers:<mixer>
                mixer = AudioSystem.getMixer(arrayMixerInfo[intMixerInfoIndex]);
                arrayData = mergeDataArrays("Source",
                                            mixer.getSourceLineInfo(),
                                            "Target",
                                            mixer.getTargetLineInfo());
                arrayColumnNames = new String[]{"Input/Output",
                                                "Line Info"};

                nodeAudioSampledMixer = new DefaultMutableTreeNode();
                expanderData = new AudioExplorerExpanderData("Audio.Sampled.Mixers.Mixer",
                                                             arrayMixerInfo[intMixerInfoIndex].getName(),
                                                             arrayData,
                                                             arrayColumnNames,
                                                             ICON_MIXER,
                                                             nodeAudioSampledMixer,
                                                             expanderui);
                expanderData.setChildMetadata(metadatalist);
                nodeAudioSampledMixers.add(nodeAudioSampledMixer);

                arrayColumnNames = new String[]{"Attribute", "Value"};

                //---------------------------------------------------------------------------------
                // Source Lines

                arrayData = expandDataArray("Source",
                                            mixer.getSourceLineInfo());
                arrayColumnNames = new String[]{"Input",
                                                "Line Info"};
                nodeSourceLines = new DefaultMutableTreeNode();
                expanderData = new AudioExplorerExpanderData("Audio.Sampled.Mixers.Mixer.Inputs",
                                                             "Inputs",
                                                             arrayData,
                                                             arrayColumnNames,
                                                             ICON_LINE_INPUT,
                                                             nodeSourceLines,
                                                             expanderui);
                expanderData.setChildMetadata(metadatalist);
                nodeAudioSampledMixer.add(nodeSourceLines);

                // List all Mixer Source (Input) Lines
                arraySourceLineInfo = mixer.getSourceLineInfo();
                arraySourceLines = new Line[arraySourceLineInfo.length];

                for (int intSourceLineInfoIndex = 0;
                     intSourceLineInfoIndex < arraySourceLineInfo.length;
                     intSourceLineInfoIndex++)
                    {
                    arraySourceLines[intSourceLineInfoIndex] = AudioSystem.getLine(arraySourceLineInfo[intSourceLineInfoIndex]);
                    }

                // Expander Audio:Sampled:Mixers:<mixer>:<source-lines>
                for (int intLineIndex = 0;
                     intLineIndex < arraySourceLines.length;
                     intLineIndex++)
                    {
                    final Line line;
                    final DefaultMutableTreeNode nodeSourceLine;
                    final Control[] arrayControls;
                    final int intControlStartIndex;
                    final String strIconFilename;

                    line = arraySourceLines[intLineIndex];
                    arrayControls = line.getControls();

                    if (line instanceof DataLine)
                        {
                        final DataLine dataLine;
                        //final AudioFormat audioDefaultFormat;
                        final AudioFormat[] arrayFormats;

                        dataLine = (DataLine) line;

                        // This obtains the current format, or the default if never opened
                        //audioDefaultFormat = dataLine.getFormat();
                        arrayFormats = ((DataLine.Info) dataLine.getLineInfo()).getFormats();

                        arrayData = new Object[(arrayFormats.length << 3) + arrayControls.length][2];
                        assembleFormatData(arrayFormats, arrayData, dataLine);

                        strIconFilename = ICON_LINE_INPUT;
                        }
                    else if (line instanceof Port)
                        {
                        final Port port;
                        final Port.Info portInfo;

                        port = (Port) line;
                        portInfo = (Port.Info) port.getLineInfo();
                        arrayData = new Object[2 + arrayControls.length][2];

                        // Remember that all data entries in a Report must be Strings
                        arrayData[0][0] = "Name";
                        arrayData[0][1] = portInfo.getName();

                        arrayData[1][0] = "Source";
                        arrayData[1][1] = portInfo.isSource();

                        // Cheat a little here...
                        strIconFilename = getPortIconFilename(portInfo);
                        }
                    else
                        {
                        arrayData = new Object[1 + arrayControls.length][2];

                        arrayData[0][0] = "Unknown";
                        arrayData[0][1] = "Unknown";

                        strIconFilename = ICON_UNKNOWN;
                        }

                    // List Controls, if any, following the Lines
                    intControlStartIndex = arrayData.length - arrayControls.length;

                    for (int kk = intControlStartIndex;
                         kk < arrayData.length;
                         kk++)
                        {
                        final int intControlIndex;

                        arrayData[kk][0] = "Control";
                        intControlIndex = kk - intControlStartIndex;
                        arrayData[kk][1] = arrayControls[intControlIndex];
                        }

                    nodeSourceLine = new DefaultMutableTreeNode();
                    leafData = new AudioExplorerLeafData("Audio.Sampled.Mixers.Mixer.Inputs.Line",
                                                         getShortLineName(line.getLineInfo().toString()),
                                                         arrayData,
                                                         arrayColumnNames,
                                                         strIconFilename,
                                                         nodeSourceLine,
                                                         leafui);
                    nodeSourceLines.add(nodeSourceLine);
                    }

                //---------------------------------------------------------------------------------
                // Target Lines

                arrayData = expandDataArray("Target",
                                            mixer.getTargetLineInfo());
                arrayColumnNames = new String[]{"Output",
                                                "Line Info"};

                nodeTargetLines = new DefaultMutableTreeNode();
                expanderData = new AudioExplorerExpanderData("Audio.Sampled.Mixers.Mixer.Outputs",
                                                             "Outputs",
                                                             arrayData,
                                                             arrayColumnNames,
                                                             ICON_LINE_OUTPUT,
                                                             nodeTargetLines,
                                                             expanderui);
                expanderData.setChildMetadata(metadatalist);
                nodeAudioSampledMixer.add(nodeTargetLines);

                // List all Mixer Target (Output) Lines
                arrayTargetLineInfo = mixer.getTargetLineInfo();
                arrayTargetLines = new Line[arrayTargetLineInfo.length];

                for (int intTargetLineInfoIndex = 0;
                     intTargetLineInfoIndex < arrayTargetLineInfo.length;
                     intTargetLineInfoIndex++)
                    {
                    arrayTargetLines[intTargetLineInfoIndex] = AudioSystem.getLine(arrayTargetLineInfo[intTargetLineInfoIndex]);
                    }

                // Expander Audio:Sampled:Mixers:<mixer>:<target-lines>
                for (int intLineIndex = 0;
                     intLineIndex < arrayTargetLines.length;
                     intLineIndex++)
                    {
                    final Line line;
                    final DefaultMutableTreeNode nodeTargetLine;
                    final Control[] arrayControls;
                    final int intControlStartIndex;
                    final String strIconFilename;

                    line = arrayTargetLines[intLineIndex];
                    arrayControls = line.getControls();

                    if (line instanceof DataLine)
                        {
                        final DataLine dataLine;
                        //final AudioFormat audioDefaultFormat;
                        final AudioFormat[] arrayFormats;

                        dataLine = (DataLine) line;

                        // This obtains the current format, or the default if never opened
                        //audioDefaultFormat = dataLine.getFormat();
                        arrayFormats = ((DataLine.Info) dataLine.getLineInfo()).getFormats();

                        arrayData = new Object[(arrayFormats.length << 3) + arrayControls.length][2];
                        assembleFormatData(arrayFormats, arrayData, dataLine);

                        strIconFilename = ICON_LINE_OUTPUT;
                        }
                    else if (line instanceof Port)
                        {
                        final Port port;
                        final Port.Info portInfo;

                        port = (Port) line;
                        portInfo = (Port.Info) port.getLineInfo();
                        arrayData = new Object[2 + arrayControls.length][2];

                        // Remember that all data entries in a Report must be Strings
                        arrayData[0][0] = "Name";
                        arrayData[0][1] = portInfo.getName();

                        arrayData[1][0] = "Source";
                        arrayData[1][1] = portInfo.isSource();

                        // Cheat a little here...
                        strIconFilename = getPortIconFilename(portInfo);
                        }
                    else
                        {
                        arrayData = new Object[1 + arrayControls.length][2];

                        arrayData[0][0] = "Unknown";
                        arrayData[0][1] = "Unknown";

                        strIconFilename = ICON_UNKNOWN;
                        }

                    // List Controls, if any, following the Lines
                    intControlStartIndex = arrayData.length - arrayControls.length;

                    for (int kk = intControlStartIndex;
                         kk < arrayData.length;
                         kk++)
                        {
                        final int intControlIndex;

                        arrayData[kk][0] = "Control";
                        intControlIndex = kk - intControlStartIndex;
                        arrayData[kk][1] = arrayControls[intControlIndex];
                        }

                    nodeTargetLine = new DefaultMutableTreeNode();
                    leafData = new AudioExplorerLeafData("Audio.Sampled.Mixers.Mixer.Outputs.Line",
                                                         getShortLineName(line.getLineInfo().toString()),
                                                         arrayData,
                                                         arrayColumnNames,
                                                         strIconFilename,
                                                         nodeTargetLine,
                                                         leafui);
                    nodeTargetLines.add(nodeTargetLine);
                    }
                }

            catch (Exception exception)
                {
                exception.printStackTrace();
                }
            }

        //-----------------------------------------------------------------------------------------
        // MIDI Types

        arrayMidiTypes = MidiSystem.getMidiFileTypes();
        arrayData = new Object[arrayMidiTypes.length][2];

        for (int intMidiTypesIndex = 0;
             intMidiTypesIndex < arrayMidiTypes.length;
             intMidiTypesIndex++)
            {
            final String strMidiDescription;

            arrayData[intMidiTypesIndex][0] = arrayMidiTypes[intMidiTypesIndex];

            switch (arrayMidiTypes[intMidiTypesIndex])
                {
                case 0:
                {
                strMidiDescription = "Single Track";
                break;
                }

                case 1:
                {
                strMidiDescription = "Multi Track";
                break;
                }

                case 2:
                {
                strMidiDescription = "Multi Song";
                break;
                }

                default:
                {
                strMidiDescription = "Unknown";
                }
                }

            arrayData[intMidiTypesIndex][1] = strMidiDescription;
            }

        // Expander: MIDI
        arrayColumnNames = new String[]{"Type", "Description"};

        nodeAudioMIDI = new DefaultMutableTreeNode();
        expanderData = new AudioExplorerExpanderData("Audio.MIDI",
                                                     NODE_MIDI,
                                                     arrayData,
                                                     arrayColumnNames,
                                                     ICON_MIDI,
                                                     nodeAudioMIDI,
                                                     expanderui);
        expanderData.setChildMetadata(metadatalist);
        nodeRoot.add(nodeAudioMIDI);

        arrayColumnNames = new String[]{"Attribute", "Value"};
        arrayMidiDeviceInfo = MidiSystem.getMidiDeviceInfo();

        // List details of each MIDI Device
        for (int intMidiDeviceInfoIndex = 0;
             intMidiDeviceInfoIndex < arrayMidiDeviceInfo.length;
             intMidiDeviceInfoIndex++)
            {
            final int INDEX_ATTRIBUTE = 0;
            final int INDEX_VALUE = 1;
            final DefaultMutableTreeNode nodeMidiInfo;
            final String strVersion;

            arrayData = new Object[6][2];
            arrayData[0][INDEX_ATTRIBUTE] = "Name";
            arrayData[0][INDEX_VALUE] = arrayMidiDeviceInfo[intMidiDeviceInfoIndex].getName();

            arrayData[1][INDEX_ATTRIBUTE] = "Vendor";
            arrayData[1][INDEX_VALUE] = arrayMidiDeviceInfo[intMidiDeviceInfoIndex].getVendor();

            arrayData[2][INDEX_ATTRIBUTE] = "Version";
            strVersion = arrayMidiDeviceInfo[intMidiDeviceInfoIndex].getVersion();
            arrayData[2][INDEX_VALUE] = strVersion.replaceAll("Version ", "");

            arrayData[3][INDEX_ATTRIBUTE] = "Description";
            arrayData[3][INDEX_VALUE] = arrayMidiDeviceInfo[intMidiDeviceInfoIndex].getDescription();

            arrayData[4][INDEX_ATTRIBUTE] = "Maximum Transmitters";
            arrayData[5][INDEX_ATTRIBUTE] = "Maximum Receivers";

            try
                {
                final MidiDevice midiDevice;
                final Object valueTransmitter;
                final Object valueReceiver;

                midiDevice = MidiSystem.getMidiDevice(arrayMidiDeviceInfo[intMidiDeviceInfoIndex]);

                if (midiDevice.getMaxTransmitters() == AudioSystem.NOT_SPECIFIED)
                    {
                    valueTransmitter = MSG_NOT_SPECIFIED;
                    }
                else
                    {
                    valueTransmitter = midiDevice.getMaxTransmitters();
                    }

                if (midiDevice.getMaxReceivers() == AudioSystem.NOT_SPECIFIED)
                    {
                    valueReceiver = MSG_NOT_SPECIFIED;
                    }
                else
                    {
                    valueReceiver = midiDevice.getMaxReceivers();
                    }

                arrayData[4][INDEX_VALUE] = valueTransmitter;
                arrayData[5][INDEX_VALUE] = valueReceiver;
                }

            catch (MidiUnavailableException exception)
                {
                arrayData[4][INDEX_VALUE] = "Unknown";
                arrayData[5][INDEX_VALUE] = "Unknown";
                }

            // Leaf: MIDI Device Info Details
            nodeMidiInfo = new DefaultMutableTreeNode();
            leafData = new AudioExplorerLeafData("Audio.MIDI.Info",
                                                 arrayMidiDeviceInfo[intMidiDeviceInfoIndex].getName(),
                                                 arrayData,
                                                 arrayColumnNames,
                                                 ICON_MIDI_INFO,
                                                 nodeMidiInfo,
                                                 leafui);
            nodeAudioMIDI.add(nodeMidiInfo);
            }

        //-----------------------------------------------------------------------------------------
        // Leaf Audio:Sampled:Suffixes

        arrayAudioTypes = new Object[AudioSystem.getAudioFileTypes().length][1];

        for (int intTypeIndex=0;
             intTypeIndex < AudioSystem.getAudioFileTypes().length;
             intTypeIndex++)
            {
            arrayAudioTypes[intTypeIndex][0] = AudioSystem.getAudioFileTypes()[intTypeIndex];
            }

        arrayColumnNames = new String[1];
        arrayColumnNames[0] = "Audio File Suffixes";

        nodeAudioSampledSuffixes = new DefaultMutableTreeNode();
        expanderData = new AudioExplorerExpanderData("Audio.Sampled.Suffixes",
                                                     NODE_SUFFIXES,
                                                     arrayAudioTypes,
                                                     arrayColumnNames,
                                                     ICON_SUFFIXES,
                                                     nodeAudioSampledSuffixes,
                                                     expanderui);
        expanderData.setChildMetadata(metadatalist);
        nodeAudioSampled.add(nodeAudioSampledSuffixes);

        return (nodeRoot);
        }


    /***********************************************************************************************
     * Assemble the Data for the specified AudioFormats.
     *
     * @param arrayformats
     * @param arraydata
     * @param dataline
     */

    private static void assembleFormatData(final AudioFormat[] arrayformats,
                                           final Object[][] arraydata,
                                           final DataLine dataline)
        {
        for (int intFormatIndex = 0;
             intFormatIndex < arrayformats.length;
             intFormatIndex++)
            {
            final AudioFormat format;
            final int intArrayIndex;

            format = arrayformats[intFormatIndex];
            intArrayIndex = intFormatIndex << 3;

            // Remember that all data entries in a Report must be Strings
            arraydata[intArrayIndex][0] = "Channels";
            arraydata[intArrayIndex][1] = formatAttributeValue(format.getChannels());

            arraydata[1 + intArrayIndex][0] = "Encoding";
            arraydata[1 + intArrayIndex][1] = format.getEncoding();

            arraydata[2 + intArrayIndex][0] = "Frame Rate";
            arraydata[2 + intArrayIndex][1] = formatAttributeValue(format.getFrameRate());

            arraydata[3 + intArrayIndex][0] = "Sample Rate";
            arraydata[3 + intArrayIndex][1] = formatAttributeValue(format.getSampleRate());

            arraydata[4 + intArrayIndex][0] = "Sample Size (bits)";
            arraydata[4 + intArrayIndex][1] = formatAttributeValue(format.getSampleSizeInBits());

            arraydata[5 + intArrayIndex][0] = "Big Endian";
            arraydata[5 + intArrayIndex][1] = format.isBigEndian();

            arraydata[6 + intArrayIndex][0] = "Level";
            arraydata[6 + intArrayIndex][1] = formatAttributeValue(dataline.getLevel());

            arraydata[7 + intArrayIndex][0] = "";
            arraydata[7 + intArrayIndex][1] = "";
            }
        }


    /***********************************************************************************************
     * Format a numerical Attribute as a String, taking account of AudioSystem.NOT_SPECIFIED.
     *
     * @param value
     *
     * @return String
     */

    private static String formatAttributeValue(final Number value)
        {
        final String strValue;

        // Not sure if this is the best way?
        if ((value.doubleValue() == AudioSystem.NOT_SPECIFIED)
            || (value.floatValue() == AudioSystem.NOT_SPECIFIED)
            || (value.intValue() == AudioSystem.NOT_SPECIFIED))
            {
            strValue = MSG_NOT_SPECIFIED;
            }
        else
            {
            strValue = value.toString();
            }

        return (strValue);
        }


    /***********************************************************************************************
     * Get the IconFilename for the specified Port.
     * This does not cross-check the Port Source-Target configuration against the chosen Icon.
     *
     * @param portinfo
     *
     * @return String
     */

    private static String getPortIconFilename(final Port.Info portinfo)
        {
        final String strIconFilename;

        if (portinfo.getName().contains(Port.Info.MICROPHONE.getName()))
            {
            strIconFilename = ICON_MICROPHONE;
            }
        else if (portinfo.getName().contains(Port.Info.LINE_IN.getName()))
            {
            strIconFilename = ICON_LINE_INPUT;
            }
        else if (portinfo.getName().contains(Port.Info.COMPACT_DISC.getName()))
            {
            strIconFilename = ICON_COMPACT_DISC;
            }
        else if (portinfo.getName().contains(Port.Info.SPEAKER.getName()))
            {
            strIconFilename = ICON_LOUDSPEAKER;
            }
        else if (portinfo.getName().contains(Port.Info.HEADPHONE.getName()))
            {
            strIconFilename = ICON_HEADPHONE;
            }
        else if (portinfo.getName().contains(Port.Info.LINE_OUT.getName()))
            {
            strIconFilename = ICON_LINE_OUTPUT;
            }
        else
            {
            strIconFilename = ICON_UNKNOWN;
            }

        return (strIconFilename);
        }


    /***********************************************************************************************
     * Expand one Data array.
     *
     * @param name
     * @param data
     *
     * @return Object[][]
     */

    private static Object[][] expandDataArray(final String name,
                                              final Object[] data)
        {
        final Object[][] arrayData;

        arrayData = new Object[data.length][2];

        for (int intDataIndex = 0;
             intDataIndex < data.length;
             intDataIndex++)
            {
            arrayData[intDataIndex][0] = name;
            arrayData[intDataIndex][1] = data[intDataIndex];
            }

        return (arrayData);
        }


    /***********************************************************************************************
     * Merge two arrays.
     *
     * @param name1
     * @param data1
     * @param name2
     * @param data2
     *
     * @return Object[][]
     */

    private static Object[][] mergeDataArrays(final String name1,
                                              final Object[] data1,
                                              final String name2,
                                              final Object[] data2)
        {
        final Object[][] data;
        final int offset;

        data = new Object[data1.length + data2.length][2];

        for (int intDataIndex = 0;
             intDataIndex < data1.length;
             intDataIndex++)
            {
            data[intDataIndex][0] = name1;
            data[intDataIndex][1] = data1[intDataIndex];
            }

        offset = data1.length;

        for (int intDataIndex = offset;
             intDataIndex < data.length;
             intDataIndex++)
            {
            data[intDataIndex][0] = name2;
            data[intDataIndex][1] = data2[intDataIndex - offset];
            }

        return (data);
        }


    /***********************************************************************************************
     * Get the short name of the Audio device.
     *
     * @param name
     *
     * @return String
     */

    private static String getShortLineName(final String name)
        {
        // ToDo It is unclear where these names come from!
        final String[] lineTypes =
            {
            "Clip",
            "SourceDataLine",
            "TargetDataLine",
            "Speaker",
            "Microphone",
            "Headphone",
            "Compact Disc",
            "Master Volume",
            "Line In",
            "Line Out"
            };

        for (final String strShortName : lineTypes)
            {
            if (name.toLowerCase().replaceAll("_", " ").contains(strShortName.toLowerCase()))
                {
                return (strShortName);
                }
            }

        return (name);
        }
    }