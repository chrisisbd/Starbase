// Copyright 2000, 2001, 2002, 2003, 04, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013, 2013
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

package org.lmn.fc.common.datatranslators.skypipe;

import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.common.exceptions.FrameworkException;
import org.lmn.fc.common.utilities.time.ChronosHelper;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.TimeZone;

//--------------------------------------------------------------------------------------------------
// http://www.radiosky.com/skypipehelp/datastructure.html
//
// The header has a fixed structure, followed by a variable length string called the Note.
// The length of the Note may be found in the fixed header parameter Notelength
// and is the  number of bytes, that is, the number of  ASCII characters in the Note.
// The Note may contain additional parameters simply encrypted.
// Channel labels, for example, are encrypted in the Note. See below.
//
// [Fixed Length Portion of Header]
// [Variable Length Portion of Header = Note + embedded parameters]
// [Data]
//
//Type structure of the fixed length portion of the header.
//
//Type RadioSkyPipeHeader
//    version As String * 10
//    Start As Double
//    Finish As Double
//    Lat As Double
//    Lng As Double
//    MaxY As Double
//    MinY As Double
//    TimeZone As Integer
//    Source As String * 10
//    Author As String * 20
//    LocalName As String * 20
//    Location As String * 40
//    Channels As Integer
//    NoteLength As Long
//End Type
//
// Start and Finish are declared as Doubles even though they actually
// contain Visual Basic dates. They represent the beginning and ending
// date/times of the observation. Consult Visual Basic documentation
// for an explanation of how a date can be represented by a Double.
//
// http://www.xtremevbtalk.com/archive/index.php/t-35431.html
// The Date type is an 8 byte double precision floating point number
// representing the number of days since midnight, Dec 30, 1899.
// The fractional part of the date represents the time in days.
// So 12 noon would be half a day, or 0.5.
//
// Strings within this structure are fixed length strings. Their lengths
// are denoted by * 10, * 20, etc.
//
// The Note is the place where text information as seen on the Options
// Identity page of the program is kept within the file. It was seen
// early on that there needed to be a way to introduce new parameters
// into the header information for these data files in a way which
// could change over time but maintain backwards compatibility. Thus
// it was decided to hide any new parameters into the Note and strip
// them out before the Note text is displayed for its original
// purpose.  Below is the code used
// to embed these parameters into the Note:
//
//Dim TNSx As String
//Rem tMetaData(1,200) is a multi-dimensional string array
//Rem TChannelOffset is a double type number array
//Rem TempNotesString is the unmodified Note string
//Rem NotesString is the final string
//Rem TChannelLabel is a string array which holds the Channel Labels
//
//TNSx = TempNotesString + "*[[*"
//
//If FileLoggedUsingUT Then TNSx = TNSx + "Logged Using UT" + Chr(255)
//
//Rem Pro Version option for no timestamps
//If NoTimeStamps Then TNSx = TNSx + "No Time Stamps" + Chr(255)
//
//Rem channel labels
//For u = 0 To NumChannels - 1
//TNSx = TNSx + "CHL" + Trim(Str$(u)) + TChannelLabel(u) + Chr(255)
//Next u
//
//Rem channel offsets
//For u = 0 To NumChannels - 1
//TNSx = TNSx + "CHO" + Trim(Str$(u)) + Trim(Str$(TChannelOffset(u))) + Chr(255)
//Next u
//
//Rem Pro version option to save in Integer format
//If SaveAsIntegers Then TNSx = TNSx + "Integer Save" + Chr(255)
//
//Rem New X and Y chart label feature
//If Trim(iStripChartX1.XAxisTitle) <> "" Then TNSx = TNSx + "XALABEL" + iStripChartX1.XAxisTitle + Chr(255)
//If Trim(iStripChartX1.YAxisTitle) <> "" Then TNSx = TNSx + "YALABEL" + iStripChartX1.YAxisTitle + Chr(255)
//
//Rem Up to 200 meta-data name/value pairs
//Rem  tMetaData(0, u) contains the name string and  tMetaData(1, u) contains the value string
//For u = 0 To UBound(tMetaData, 2)
//        TNSx = TNSx + "MetaData_" + tMetaData(0, u) + Chr(200) + tMetaData(1, u) + Chr(255)
//Next u
//
//NotesString = TNSx + "*]]*"
//
//Rem End of code sample
//
// The sample values are recorded in the data portion of the file.
// Timestamps are usually used however Pro version users have an option to not use timestamps.
// In that case the beginning and ending times in the header are used and timestamps are calculated
// under the assumption that the sample intervals are equal.
// Pro version users also have an option to use an integer format that reduces the file size
// considerably at the cost of dynamic range and precision.
//
// Data - Default Format
//
// The actual data is a series of groups of Date and Sample values.
// A VB date (remember it is encoded as an 8 byte Double) is
// followed by Double data type Sample values. If there are multiple
// channels, then data for each of the channels follows the date. So
// for a two channel file the structure would be:
//
//[Date] (8 bytes)
//[Channel 1 Data] (8 bytes)
//[Channel 2 Data] (8 bytes)
//
//Data - Integer Save Format
//[Date] (8 bytes)
//[Channel 1 Data] (2 bytes)
//[Channel 2 Data] (2 bytes)
//
//Data - No Timestamps Format (non-Integer)
//[Channel 1 Data] (8 bytes)
//[Channel 2 Data] (8 bytes)
//
//Data - No Timestamps Format (Integer)
//[Channel 1 Data] (2 bytes)
//[Channel 2 Data] (2 bytes)


/***************************************************************************************************
 * RadioSkyPipeHeader.
 */

public final class RadioSkyPipeHeader implements FrameworkConstants,
                                                 FrameworkStrings,
                                                 FrameworkSingletons
    {
    public static final int OFFSET_VERSION = 0;
    public static final int OFFSET_START = 10;
    public static final int OFFSET_FINISH = 18;
    public static final int OFFSET_LAT = 26;
    public static final int OFFSET_LONG = 34;
    public static final int OFFSET_MAX_Y = 42;
    public static final int OFFSET_MIN_Y = 50;
    public static final int OFFSET_TIMEZONE = 58;
    public static final int OFFSET_SOURCE = 60;
    public static final int OFFSET_AUTHOR = 70;
    public static final int OFFSET_LOCAL_NAME = 90;
    public static final int OFFSET_LOCATION = 110;
    public static final int OFFSET_CHANNELS = 150;
    public static final int OFFSET_NOTE_LENGTH = 152;
    public static final int OFFSET_NOTE_TEXT = 156;

    public static final int LENGTH_VERSION = 10;
    public static final int LENGTH_SOURCE = 10;
    public static final int LENGTH_AUTHOR = 20;
    public static final int LENGTH_LOCAL_NAME = 20;
    public static final int LENGTH_LOCATION = 40;

    // The SkyPipe metadata/configuration is very crude...
    private static final String NOTE_START = "*[[*";
    private static final String NOTE_END = "*]]*";
    private static final String METADATA_UT = "Logged Using UT";
    private static final String METADATA_NO_TIMESTAMPS = "No Time Stamps";
    private static final String METADATA_INTEGER_SAVE = "Integer Save";
    private static final String METADATA_CHANNEL_LABEL = "CHL";
    private static final String METADATA_CHANNEL_OFFSET = "CHO";
    private static final String METADATA_LABEL_XAXIS = "XALABEL";
    private static final String METADATA_LABEL_YAXIS = "YALABEL";
    private static final String METADATA_METADATA = "MetaData_";
    private static final String REGEX_METADATA = "\\xff";
    private static final String REGEX_NVP = "\\xc8";

    private static final int MAX_RSP_CHANNELS = 10;
    private static final int MAX_RSP_METADATA = 200;

    private final String strFilename;
    private final String strVersion;

    private final double dblStart;
    private final double dblFinish;
    private final double lat;
    private final double lng;
    private final double maxY;
    private final double minY;
    private final double dblTimeZone;
    private final TimeZone timeZone;

    private final String strSource;
    private final String strAuthor;
    private final String strLocalName;
    private final String strLocation;
    private final int intChannelCount;
    private int noteLength;
    private String strRawNote;
    private String strPlainNote;

    // Metadata derived from the header
    private boolean boolLoggedUsingUT;
    private boolean boolDataIsDouble;
    private boolean boolDataIsTimestamped;
    private int intDateSize;
    private int intDataSize;
    private String [] arrayChannelLabels;
    private int [] arrayChannelOffsets;
    private Hashtable<String, String> mapMetaData;
    private String strXaxisLabel;
    private String strYaxisLabel;


    /***********************************************************************************************
     * Parse the Channel Labels.
     * These are indicated by 'CHL' 'channel_id' 'label_text'.
     *
     * @param labels
     * @param channelcount
     * @param token
     */

    private static void parseChannel(final String[] labels,
                                     final int channelcount,
                                     final String token)
        {
        if ((labels != null)
            && (token != null)
            && (token.length() > (METADATA_CHANNEL_LABEL.length() + 2)))
            {
            final String strChannelID;
            final int intChannelID;

            // Get the ChannelID, which must be a single character
            strChannelID = token.substring(METADATA_CHANNEL_LABEL.length(),
                                           METADATA_CHANNEL_LABEL.length()+ 1);
            intChannelID = Integer.valueOf(strChannelID);

            if ((intChannelID >= 0)
                && (intChannelID < channelcount))
                {
                labels[intChannelID] = token.substring(METADATA_CHANNEL_LABEL.length() + 1);
                }
            }
        }


    /***********************************************************************************************
     * Parse the Channel Offsets.
     * These are indicated by 'CHO' 'channel_id' 'offset'.
     *
     * @param offsets
     * @param channelcount
     * @param token
     */

    private static void parseChannelOffsets(final int[] offsets,
                                            final int channelcount,
                                            final String token)
        {
        if ((offsets != null)
            && (token != null)
            && (token.length() > (METADATA_CHANNEL_OFFSET.length() + 2)))
            {
            final String strChannelID;
            final int intChannelID;

            // Get the ChannelID, which must be a single character
            strChannelID = token.substring(METADATA_CHANNEL_OFFSET.length(),
                                           METADATA_CHANNEL_OFFSET.length()+ 1);
            intChannelID = Integer.valueOf(strChannelID);

            if ((intChannelID >= 0)
                && (intChannelID < channelcount))
                {
                offsets[intChannelID] = Integer.valueOf(
                        token.substring(METADATA_CHANNEL_OFFSET.length() + 1));
                }
            }
        }


    /***********************************************************************************************
     * Parse the axis Labels.
     * This is indicated by 'XALABEL' 'label_text'.
     *
     * @param token
     *
     * @return String
     */

    private static String parseAxisLabel(final String token)
        {
        String strLabel;

        strLabel = EMPTY_STRING;

        if ((token != null)
            && (token.length() > (METADATA_LABEL_XAXIS.length() + 1)))
            {
            strLabel = token.substring(METADATA_LABEL_XAXIS.length());
            }

        return (strLabel);
        }


    /***********************************************************************************************
     * Parse the MetaData (up to 200 items).
     * These are indicated by 'MetaData_' 'key' '=' 'value'.
     *
     * @param map
     * @param token
     */

    private static void parseMetaData(final Hashtable<String, String> map,
                                      final String token)
        {
        // The length must be at least MetaData_n=v
        if ((map != null)
            && (token != null)
            && (token.length() > (METADATA_METADATA.length() + 3)))
            {
            final String [] nvp = token.split(REGEX_NVP);

            if (nvp.length == 2)
                {
                // Remove the prefix for our own use...
                map.put(nvp[0].substring(METADATA_METADATA.length()), nvp[1]);
                }
            }
        }


    /***********************************************************************************************
     * Construct the RadioSkyPipeHeader.
     *
     * @param filename
     * @param version
     * @param start
     * @param finish
     * @param latitude
     * @param longitude
     * @param ymax
     * @param ymin
     * @param timezone
     * @param source
     * @param author
     * @param localname
     * @param location
     * @param channels
     * @param notelength
     * @param rawnote
     */

    public RadioSkyPipeHeader(final String filename,
                              final String version,
                              final double start,
                              final double finish,
                              final double latitude,
                              final double longitude,
                              final double ymax,
                              final double ymin,
                              final double timezone,
                              final String source,
                              final String author,
                              final String localname,
                              final String location,
                              final int channels,
                              final int notelength,
                              final String rawnote)
        {
        this.strFilename = filename;
        this.strVersion = version;
        this.dblStart = start;
        this.dblFinish = finish;
        this.lat = latitude;
        this.lng = longitude;
        this.maxY = ymax;
        this.minY = ymin;
        this.dblTimeZone = timezone;
        this.timeZone = TimeZone.getTimeZone(RadioSkyPipeHelper.normaliseRspTimeZoneID(timezone));
        this.strSource = source;
        this.strAuthor = author;
        this.strLocalName = localname;
        this.strLocation = location;
        this.intChannelCount = channels;

        // We can only deal with channels {0...9} in this strVersion
        if (this.intChannelCount > MAX_RSP_CHANNELS)
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_INVALID + SPACE + "Too many channels");
            }

        this.noteLength = notelength;
        this.strRawNote = rawnote;

        boolLoggedUsingUT = strRawNote.contains(METADATA_UT);
        boolDataIsTimestamped = !strRawNote.contains(METADATA_NO_TIMESTAMPS);
        boolDataIsDouble = !strRawNote.contains(METADATA_INTEGER_SAVE);

        if (boolDataIsTimestamped)
            {
            intDateSize = 8;
            }
        else
            {
            // No Timestamps
            intDateSize = 0;
            }

        if (boolDataIsDouble)
            {
            // Double data
            intDataSize = 8;
            }
        else
            {
            // Integer data
            intDataSize = 2;
            }

        arrayChannelLabels = new String[getChannelCount()];
        arrayChannelOffsets = new int[getChannelCount()];
        strXaxisLabel = EMPTY_STRING;
        strYaxisLabel = EMPTY_STRING;
        mapMetaData = new Hashtable<String, String>(MAX_RSP_METADATA);

        // Parse the Note field
        parseNote(strRawNote);
        }


    /***********************************************************************************************
     * Parse the Note field.
     *
     * @param note
     */

    private void parseNote(final String note)
        {
        if ((note != null)
            && (!EMPTY_STRING.equals(note)))
            {
            // Firstly, check that there is anything other than a plain note...
            if (!note.contains(NOTE_START))
                {
                strPlainNote = note;
                }
            else
                {
                final StringBuffer buffer;
                final String [] arrayTokens;

                // Try to isolate the plain note and the subsequent meta data
                strPlainNote = note.substring(0, note.indexOf(NOTE_START));

                // Chop up the Note to look for meta data
                buffer = new StringBuffer(note.substring(note.indexOf(NOTE_START)));
                arrayTokens = buffer.toString().split(REGEX_METADATA);

                for (int i = 0; i < arrayTokens.length; i++)
                    {
                    if (arrayTokens[i].startsWith(METADATA_CHANNEL_LABEL))
                        {
                        // Parse the Channel Labels
                        // These are indicated by 'CHL' 'channel_id' 'label_text' '0xff'
                        parseChannel(arrayChannelLabels, intChannelCount, arrayTokens[i]);
                        }
                    else if (arrayTokens[i].startsWith(METADATA_CHANNEL_OFFSET))
                        {
                        // Parse the Channel Offsets
                        // These are indicated by 'CHO' 'channel_id' 'offset' '0xff'
                        parseChannelOffsets(arrayChannelOffsets, intChannelCount, arrayTokens[i]);
                        }
                    else if (arrayTokens[i].startsWith(METADATA_LABEL_XAXIS))
                        {
                        // Parse the X axis Labels
                        // This is indicated by 'XALABEL' 'label_text' '0xff'
                        strXaxisLabel = parseAxisLabel(arrayTokens[i]);
                        }
                    else if (arrayTokens[i].startsWith(METADATA_LABEL_YAXIS))
                        {
                        // Parse the Y axis Labels
                        // This is indicated by 'YALABEL' 'label_text' '0xff'
                        strYaxisLabel = parseAxisLabel(arrayTokens[i]);
                        }
                    else if (arrayTokens[i].startsWith(METADATA_METADATA))
                        {
                        // Parse the MetaData (up to 200 items)
                        // These are indicated by 'MetaData_' 'key' '=' 'value' '0xff'
                        parseMetaData(mapMetaData, arrayTokens[i]);
                        }
                    }
                }
            }
        }


    /***********************************************************************************************
     * Get the original Filename.
     *
     * @return String
     */

    public String getFilename()
        {
        return (this.strFilename);
        }


    /***********************************************************************************************
     * Get the RSP Version.
     *
     * @return String
     */

    public String getVersion()
        {
        return (this.strVersion);
        }


    /***********************************************************************************************
     * Get the Start Time.
     *
     * @return double
     */

    public double getStart()
        {
        return (this.dblStart);
        }


    /***********************************************************************************************
     * Get the Finish Time.
     *
     * @return double
     */

    public double getFinish()
        {
        return (this.dblFinish);
        }


    /***********************************************************************************************
     * Get the Latitude.
     *
     * @return double
     */

    public double getLatitude()
        {
        return (this.lat);
        }


    /***********************************************************************************************
     * Get the Longitude.
     *
     * @return double
     */

    public double getLongitude()
        {
        return (this.lng);
        }


    /***********************************************************************************************
     * Get the Max Y Value.
     *
     * @return double
     */

    public double getMaxY()
        {
        return (this.maxY);
        }


    /***********************************************************************************************
     * Get the Min Y Value.
     *
     * @return double
     */

    public double getMinY()
        {
        return (this.minY);
        }


    /***********************************************************************************************
     * Get the Time Zone.
     * BEWARE - this is not a Java Time Zone.
     *
     * @return double
     */

    public double getTimeZoneAsDouble()
        {
        return (this.dblTimeZone);
        }


    /***********************************************************************************************
     * Get the Time Zone.
     *
     * @return TimeZone
     */

    public TimeZone getTimeZone()
        {
        return (this.timeZone);
        }


    /***********************************************************************************************
     * Get the Source.
     *
     * @return String
     */

    public String getSource()
        {
        return (this.strSource);
        }


    /***********************************************************************************************
     * Get the Author.
     *
     * @return String
     */

    public String getAuthor()
        {
        return (this.strAuthor);
        }


    /***********************************************************************************************
     * Get the Local Name.
     *
     * @return String
     */

    public String getLocalName()
        {
        return (this.strLocalName);
        }


    /***********************************************************************************************
     * Get the Location.
     *
     * @return String
     */

    public String getLocation()
        {
        return (this.strLocation);
        }


    /***********************************************************************************************
     * Get the Channel Count.
     *
     * @return int
     */

    public int getChannelCount()
        {
        return (this.intChannelCount);
        }


    /***********************************************************************************************
     * Get the Note Length.
     *
     * @return int
     */

    public int getNoteLength()
        {
        return (this.noteLength);
        }


    /***********************************************************************************************
     * Get the Note Text Raw, i.e. no parsing of tokens.
     *
     * @return String
     */

    public String getRawNoteText()
        {
        return (this.strRawNote);
        }


    /***********************************************************************************************
     * Get the Note part of the Note field.
     *
     * @return String
     */

    public String getPlainNoteText()
        {
        return (this.strPlainNote);
        }


    /***********************************************************************************************
     * Get the Date Size in bytes..
     * The DateSize may be zero, if Timestamps are not used.
     *
     * @return int
     */

    public int getDateSize()
        {
        return (this.intDateSize);
        }


    /***********************************************************************************************
     * Get the Data Size in bytes.
     *
     * @return int
     */

    public int getDataSize()
        {
        return (this.intDataSize);
        }


    /***********************************************************************************************
     * Indicate if UT was used.
     *
     * @return boolean
     */

    public boolean isUT()
        {
        return (this.boolLoggedUsingUT);
        }


    /***********************************************************************************************
     * Indicate if the data are recorded as doubles.
     *
     * @return boolean
     */

    public boolean isDataDouble()
        {
        return (this.boolDataIsDouble);
        }


    /***********************************************************************************************
     * Indicate of the data are timestamped.
     *
     * @return boolean
     */

    public boolean isDataTimestamped()
        {
        return (this.boolDataIsTimestamped);
        }


    /***********************************************************************************************
     * Get the Channel Labels.
     *
     * @return String[]
     */

    public String[] getChannelLabels()
        {
        return (this.arrayChannelLabels);
        }


    /***********************************************************************************************
     * Get the Channel Offsets.
     *
     * @return int[]
     */

    public int[] getChannelOffsets()
        {
        return (this.arrayChannelOffsets);
        }


    /***********************************************************************************************
     * Get the Hashtable of RSP Metadata.
     *
     * @return Hashtable
     */

    public Hashtable getMetaData()
        {
        return (this.mapMetaData);
        }


    /***********************************************************************************************
     * Get the X Axis Label.
     *
     * @return String
     */

    public String getAxisLabelX()
        {
        return (this.strXaxisLabel);
        }


    /***********************************************************************************************
     * Get the Y Axis Label.
     *
     * @return String
     */

    public String getAxisLabelY()
        {
        return (this.strYaxisLabel);
        }


    /***********************************************************************************************
     * Debug the SkyPipe Header.
     */

    public void debugHeader()
        {
        LOGGER.logTimedEvent("SkyPipe Header");

        LOGGER.logTimedEvent(INDENT + "[Filename=" + getFilename() + "]");
        LOGGER.logTimedEvent(INDENT + "[Version=" + getVersion() + "]");
        LOGGER.logTimedEvent(INDENT + "[Start=" + ChronosHelper.toCalendarString(RadioSkyPipeHelper.doubleToCalendar(this,
                                                                                                                     getStart(),
                                                                                                                     getTimeZone(),
                                                                                                                     true)) + "]");
        LOGGER.logTimedEvent(INDENT + "[Finish=" + ChronosHelper.toCalendarString(RadioSkyPipeHelper.doubleToCalendar(this,
                                                                                                                      getFinish(),
                                                                                                                      getTimeZone(),
                                                                                                                      true)) + "]");
        LOGGER.logTimedEvent(INDENT + "[Latitude=" + getLatitude() + "]");
        LOGGER.logTimedEvent(INDENT + "[Longitude=" + getLongitude() + "]");
        LOGGER.logTimedEvent(INDENT + "[MaxY=" + getMaxY() + "]");
        LOGGER.logTimedEvent(INDENT + "[MinY=" + getMinY() + "]");
        LOGGER.logTimedEvent(INDENT + "[TimeZoneAsDouble=" + getTimeZoneAsDouble() + "]");
        LOGGER.logTimedEvent(INDENT + "[TimeZone_RSP=" + RadioSkyPipeHelper.normaliseRspTimeZoneID(getTimeZoneAsDouble()) + "]");
        LOGGER.logTimedEvent(INDENT + "[TimeZone_Java=" + getTimeZone().getID() + "]");
        LOGGER.logTimedEvent(INDENT + "[Source=" + getSource() + "]");
        LOGGER.logTimedEvent(INDENT + "[Author=" + getAuthor() + "]");
        LOGGER.logTimedEvent(INDENT + "[LocalName=" + getLocalName() + "]");
        LOGGER.logTimedEvent(INDENT + "[Location=" + getLocation() + "]");
        LOGGER.logTimedEvent(INDENT + "[Channels=" + getChannelCount() + "]");
        LOGGER.logTimedEvent(INDENT + "[NoteLength=" + getNoteLength() + "]");
        LOGGER.logTimedEvent(INDENT + "[NoteRaw=" + getRawNoteText() + "]");
        LOGGER.logTimedEvent(INDENT + "[NoteText=" + getPlainNoteText() + "]");
        LOGGER.logTimedEvent(INDENT + "[UT=" + isUT() + "]");
        LOGGER.logTimedEvent(INDENT + "[Timestamped=" + isDataTimestamped() + "]");
        LOGGER.logTimedEvent(INDENT + "[Double=" + isDataDouble() + "]");

        if (getChannelLabels() != null)
            {
            LOGGER.logTimedEvent(INDENT + "[Channel Labels]");
            for (int i = 0; i < getChannelLabels().length; i++)
                {
                LOGGER.logTimedEvent(INDENT + INDENT + i + SPACE + getChannelLabels()[i]);
                }
            }

        if (getChannelOffsets() != null)
            {
            LOGGER.logTimedEvent(INDENT + "[Channel Offsets]");
            for (int i = 0; i < getChannelOffsets().length; i++)
                {
                LOGGER.logTimedEvent(INDENT + INDENT + i + SPACE + getChannelOffsets()[i]);
                }
            }

        if (getMetaData() != null)
            {
            LOGGER.logTimedEvent(INDENT + "[MetaData]");

            final Enumeration enumKeys;
            enumKeys = getMetaData().keys();

            while (enumKeys.hasMoreElements())
                {
                final String key;
                final String value;

                key = (String)enumKeys.nextElement();
                value = (String)getMetaData().get(key);
                LOGGER.logTimedEvent(INDENT + INDENT + key + EQUALS + value);
                }
            }

        LOGGER.logTimedEvent(INDENT + "[AxisLabel.X=" + getAxisLabelX() + "]");
        LOGGER.logTimedEvent(INDENT + "[AxisLabel.Y=" + getAxisLabelY() + "]");
        }
    }
