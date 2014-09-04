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

package org.lmn.fc.frameworks.starbase.plugins.observatory;


import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.model.logging.Logger;


/***************************************************************************************************
 * MetadataDictionary.
 */

public enum MetadataDictionary
    {
    //---------------------------------------------------------------------------------------------------------------------------------------
    // Framework

    KEY_FRAMEWORK_ROOT                              ("Framework.",                                      true, false, false, false, false, false),

    KEY_FRAMEWORK_LONGITUDE                         ("Framework.Longitude",                             true, false, false, false, false, false),
    KEY_FRAMEWORK_LATITUDE                          ("Framework.Latitude",                              true, false, false, false, false, false),
    KEY_FRAMEWORK_HASL                              ("Framework.HASL",                                  true, false, false, false, false, false),
    KEY_FRAMEWORK_NOTES                             ("Framework.Notes",                                 true, false, false, false, false, false),

    // Framework Locale
    KEY_FRAMEWORK_TIMEZONE                          ("Framework.TimeZone",                              true, false, false, false, false, false),
    KEY_FRAMEWORK_COUNTRY                           ("Framework.Country",                               true, false, false, false, false, false),
    KEY_FRAMEWORK_LANGUAGE                          ("Framework.Language",                              true, false, false, false, false, false),

    //---------------------------------------------------------------------------------------------------------------------------------------
    // Observatory

    KEY_OBSERVATORY_ROOT                            ("Observatory.",                                    false, true, false, false, false, false),

    KEY_OBSERVATORY_NAME                            ("Observatory.Name",                                false, true, false, false, false, false),
    KEY_OBSERVATORY_DESCRIPTION                     ("Observatory.Description",                         false, true, false, false, false, false),

    KEY_OBSERVATORY_ADDRESS_LINE1                   ("Observatory.Address.Line1",                       false, true, false, false, false, false),
    KEY_OBSERVATORY_ADDRESS_LINE2                   ("Observatory.Address.Line2",                       false, true, false, false, false, false),
    KEY_OBSERVATORY_ADDRESS_LINE3                   ("Observatory.Address.Line3",                       false, true, false, false, false, false),
    KEY_OBSERVATORY_ADDRESS_LINE4                   ("Observatory.Address.Line4",                       false, true, false, false, false, false),
    KEY_OBSERVATORY_POSTCODE                        ("Observatory.Address.Postcode",                    false, true, false, false, false, false),

    KEY_OBSERVATORY_CONTACT_TELEPHONE               ("Observatory.Contact.Telephone",                   false, true, false, false, false, false),
    KEY_OBSERVATORY_CONTACT_EMAIL                   ("Observatory.Contact.Email",                       false, true, false, false, false, false),
    KEY_OBSERVATORY_CONTACT_URL                     ("Observatory.Contact.URL",                         false, true, false, false, false, false),

    KEY_OBSERVATORY_LONGITUDE                       ("Observatory.Longitude",                           false, true, false, false, false, false),
    KEY_OBSERVATORY_LATITUDE                        ("Observatory.Latitude",                            false, true, false, false, false, false),
    KEY_OBSERVATORY_LOCATION                        ("Observatory.Location",                            false, true, false, false, false, false),

    KEY_OBSERVATORY_GEODETIC_DATUM                  ("Observatory.GeodeticDatum",                       false, true, false, false, false, false),
    KEY_OBSERVATORY_GEOMAGNETICMODEL                ("Observatory.GeomagneticModel",                    false, true, false, false, false, false),
    KEY_OBSERVATORY_GEOMAGNETICLATITUDE             ("Observatory.GeomagneticLatitude",                 false, true, false, false, false, false),
    KEY_OBSERVATORY_GEOMAGNETICLONGITUDE            ("Observatory.GeomagneticLongitude",                false, true, false, false, false, false),

    KEY_OBSERVATORY_MAIDENHEADLOCATOR               ("Observatory.MaidenheadLocator",                   false, true, false, false, false, false),
    KEY_OBSERVATORY_HASL                            ("Observatory.HASL",                                false, true, false, false, false, false),

    KEY_OBSERVATORY_NOTES                           ("Observatory.Notes",                               false, true, false, false, false, false),

    // Observatory Locale
    KEY_OBSERVATORY_TIMEZONE                        ("Observatory.TimeZone",                            false, true, false, false, false, false),
    KEY_OBSERVATORY_COUNTRY                         ("Observatory.Country",                             false, true, false, false, false, false),
    KEY_OBSERVATORY_LANGUAGE                        ("Observatory.Language",                            false, true, false, false, false, false),

    KEY_OBSERVATORY_SUNRISE                         ("Observatory.Sunrise",                             false, true, false, false, false, false),
    KEY_OBSERVATORY_SUNSET                          ("Observatory.Sunset",                              false, true, false, false, false, false),

    // Defaults
    KEY_OBSERVATORY_DEFAULT_COLOUR                  ("Observatory.Default.Colour.",                     false, true, false, false,  true, false),

    //---------------------------------------------------------------------------------------------------------------------------------------
    // Observer

    KEY_OBSERVER_ROOT                               ("Observer.",                                       false, false, true, false, false, false),

    KEY_OBSERVER_NAME                               ("Observer.Name",                                   false, false, true, false, false, false),
    KEY_OBSERVER_DESCRIPTION                        ("Observer.Description",                            false, false, true, false, false, false),

    KEY_OBSERVER_ADDRESS_LINE1                      ("Observer.Address.Line1",                          false, false, true, false, false, false),
    KEY_OBSERVER_ADDRESS_LINE2                      ("Observer.Address.Line2",                          false, false, true, false, false, false),
    KEY_OBSERVER_ADDRESS_LINE3                      ("Observer.Address.Line3",                          false, false, true, false, false, false),
    KEY_OBSERVER_ADDRESS_LINE4                      ("Observer.Address.Line4",                          false, false, true, false, false, false),
    KEY_OBSERVER_POSTCODE                           ("Observer.Address.Postcode",                       false, false, true, false, false, false),
    KEY_OBSERVER_COUNTRY                            ("Observer.Country",                                false, false, true, false, false, false),

    KEY_OBSERVER_CONTACT_TELEPHONE                  ("Observer.Contact.Telephone",                      false, false, true, false, false, false),
    KEY_OBSERVER_CONTACT_EMAIL                      ("Observer.Contact.Email",                          false, false, true, false, false, false),
    KEY_OBSERVER_CONTACT_URL                        ("Observer.Contact.URL",                            false, false, true, false, false, false),

    KEY_OBSERVER_NOTES                              ("Observer.Notes",                                  false, false, true, false, false, false),

    //---------------------------------------------------------------------------------------------------------------------------------------
    // Observation

    KEY_OBSERVATION_ROOT                            ("Observation.",                                    false, false, false, true, false, false),

    KEY_OBSERVATION_CENTROID_LATITUDE               ("Observation.Centroid.Latitude",                   false, false, false, true, false, false),
    KEY_OBSERVATION_CENTROID_LONGITUDE              ("Observation.Centroid.Longitude",                  false, false, false, true, false, false),
    KEY_OBSERVATION_DATE                            ("Observation.Date",                                false, false, false, true, false, false),
    KEY_OBSERVATION_FINISH_DATE                     ("Observation.Finish.Date",                         false, false, false, true, false, false),
    KEY_OBSERVATION_FINISH_TIME                     ("Observation.Finish.Time",                         false, false, false, true, false, false),
    KEY_OBSERVATION_LATITUDE                        ("Observation.Latitude",                            false, false, false, true, false, false),
    KEY_OBSERVATION_LONGITUDE                       ("Observation.Longitude",                           false, false, false, true, false, false),
    KEY_OBSERVATION_NOTES                           ("Observation.Notes",                               false, false, false, true, false, false),
    KEY_OBSERVATION_START_DATE                      ("Observation.Start.Date",                          false, false, false, true, false, false),
    KEY_OBSERVATION_START_TIME                      ("Observation.Start.Time",                          false, false, false, true, false, false),
    KEY_OBSERVATION_TIME                            ("Observation.Time",                                false, false, false, true, false, false),
    KEY_OBSERVATION_TIMESYSTEM                      ("Observation.TimeSystem",                          false, false, false, true, false, false),
    KEY_OBSERVATION_TIMEZONE                        ("Observation.TimeZone",                            false, false, false, true, false, false),
    KEY_OBSERVATION_TITLE                           ("Observation.Title",                               false, false, false, true, false, false),
    KEY_OBSERVATION_X                               ("Observation.X",                                   false, false, false, true, false, false),

    // Chart Axes
    // This is not marked 'isChannel' because it is complete, i.e. does not need a ChannelID
    KEY_OBSERVATION_AXIS_LABEL_X                    ("Observation.Axis.Label.X",                        false, false, false, true, false, false),

    // There is usually only one Y axis, but there may be more, so we need an index value (not ChannelID)
    KEY_OBSERVATION_AXIS_LABEL_Y                    ("Observation.Axis.Label.Y.",                       false, false, false, true,  true, false),

    //---------------------------------------------------------------------------------------------------------------------------------------
    // Observation.FixUpdate

    KEY_OBSERVATION_FIXUPDATE_SOURCE                ("Observation.FixUpdate.Source",                    false, false, false, true, false, false),
    KEY_OBSERVATION_FIXUPDATE_TARGET                ("Observation.FixUpdate.Target",                    false, false, false, true, false, false),

    //---------------------------------------------------------------------------------------------------------------------------------------
    // Observation.WMM

    KEY_OBSERVATION_WMM_ALTITUDE                    ("Observation.WMM.Altitude",                        false, false, false, true, false, false),
    KEY_OBSERVATION_WMM_DECLINATION                 ("Observation.WMM.Declination",                     false, false, false, true, false, false),
    KEY_OBSERVATION_WMM_DIP                         ("Observation.WMM.Dip",                             false, false, false, true, false, false),
    KEY_OBSERVATION_WMM_EPOCH                       ("Observation.WMM.Epoch",                           false, false, false, true, false, false),
    KEY_OBSERVATION_WMM_INTENSITY_EAST              ("Observation.WMM.Intensity.East",                  false, false, false, true, false, false),
    KEY_OBSERVATION_WMM_INTENSITY_HORIZONTAL        ("Observation.WMM.Intensity.Horizontal",            false, false, false, true, false, false),
    KEY_OBSERVATION_WMM_INTENSITY_NORTH             ("Observation.WMM.Intensity.North",                 false, false, false, true, false, false),
    KEY_OBSERVATION_WMM_INTENSITY_TOTAL             ("Observation.WMM.Intensity.Total",                 false, false, false, true, false, false),
    KEY_OBSERVATION_WMM_INTENSITY_VERTICAL          ("Observation.WMM.Intensity.Vertical",              false, false, false, true, false, false),
    KEY_OBSERVATION_WMM_LATITUDE                    ("Observation.WMM.Latitude",                        false, false, false, true, false, false),
    KEY_OBSERVATION_WMM_LONGITUDE                   ("Observation.WMM.Longitude",                       false, false, false, true, false, false),

    //---------------------------------------------------------------------------------------------------------------------------------------
    // Observation.VLSR

    KEY_OBSERVATION_VLSR_RA                         ("Observation.VLSR.RightAscension",                 false, false, false, true, false, false),
    KEY_OBSERVATION_VLSR_DEC                        ("Observation.VLSR.Declination",                    false, false, false, true, false, false),
    KEY_OBSERVATION_VLSR_EPOCH                      ("Observation.VLSR.Epoch",                          false, false, false, true, false, false),
    KEY_OBSERVATION_VLSR_FRAME                      ("Observation.VLSR.ReferenceFrame",                 false, false, false, true, false, false),
    KEY_OBSERVATION_VLSR_VALUE                      ("Observation.VLSR.Value",                          false, false, false, true, false, false),

    //---------------------------------------------------------------------------------------------------------------------------------------
    // Observation.Channel
    // All marked 'isChannel' require a trailing ChannelID to be appended

    KEY_OBSERVATION_CHANNEL_ROOT                    ("Observation.Channel.",                            false, false, false, true, true, false),

    // This is not marked 'isChannel' because it is complete, i.e. does not need a ChannelID
    KEY_OBSERVATION_CHANNEL_COUNT                   ("Observation.Channel.Count",                       false, false, false, true, false, false),

    KEY_OBSERVATION_CHANNEL_COLOUR                  ("Observation.Channel.Colour.",                     false, false, false, true, true, false),
    KEY_OBSERVATION_CHANNEL_COLOUR_DEFAULT          ("Observation.Channel.Colour.Default.",             false, false, false, true, true, false),
    KEY_OBSERVATION_CHANNEL_DATA_TYPE               ("Observation.Channel.DataType.",                   false, false, false, true, true, false),
    KEY_OBSERVATION_CHANNEL_DESCRIPTION             ("Observation.Channel.Description.",                false, false, false, true, true, false),
    KEY_OBSERVATION_CHANNEL_FREQUENCY               ("Observation.Channel.Frequency.",                  false, false, false, true, true, false),
    KEY_OBSERVATION_CHANNEL_NAME                    ("Observation.Channel.Name.",                       false, false, false, true, true, false),
    KEY_OBSERVATION_CHANNEL_UNITS                   ("Observation.Channel.Units.",                      false, false, false, true, true, false),
    KEY_OBSERVATION_CHANNEL_VALUE                   ("Observation.Channel.Value.",                      false, false, false, true, true, false),

    // The old-style Virtual Channel, to be avoided if possible
    KEY_OBSERVATION_CHANNEL_COLOUR_VIRTUAL          ("Observation.Channel.Colour.Virtual.",             false, false, false, true, true, false),
    KEY_OBSERVATION_CHANNEL_DATA_TYPE_VIRTUAL       ("Observation.Channel.DataType.Virtual.",           false, false, false, true, true, false),
    KEY_OBSERVATION_CHANNEL_DESCRIPTION_VIRTUAL     ("Observation.Channel.Description.Virtual.",        false, false, false, true, true, false),
    KEY_OBSERVATION_CHANNEL_NAME_VIRTUAL            ("Observation.Channel.Name.Virtual.",               false, false, false, true, true, false),
    KEY_OBSERVATION_CHANNEL_UNITS_VIRTUAL           ("Observation.Channel.Units.Virtual.",              false, false, false, true, true, false),
    KEY_OBSERVATION_CHANNEL_VALUE_VIRTUAL           ("Observation.Channel.Value.Virtual.",              false, false, false, true, true, false),

    // Peculiar to RadioSkyPipe (for now)
    KEY_OBSERVATION_CHANNEL_OFFSET                  ("Observation.Channel.Offset.",                     false, false, false, true, true, false),

    // The slow-speed Temperature channel
    // These are not marked 'isChannel' because they are complete, i.e. don't need a ChannelID or Suffix
    KEY_OBSERVATION_CHANNEL_NAME_TEMPERATURE        ("Observation.Channel.Name.Temperature",            false, false, false, true, false, false),
    KEY_OBSERVATION_CHANNEL_VALUE_TEMPERATURE       ("Observation.Channel.Value.Temperature",           false, false, false, true, false, false),
    KEY_OBSERVATION_CHANNEL_DATA_TYPE_TEMPERATURE   ("Observation.Channel.DataType.Temperature",        false, false, false, true, false, false),
    KEY_OBSERVATION_CHANNEL_UNITS_TEMPERATURE       ("Observation.Channel.Units.Temperature",           false, false, false, true, false, false),
    KEY_OBSERVATION_CHANNEL_DESCRIPTION_TEMPERATURE ("Observation.Channel.Description.Temperature",     false, false, false, true, false, false),
    KEY_OBSERVATION_CHANNEL_COLOUR_TEMPERATURE      ("Observation.Channel.Colour.Temperature",          false, false, false, true, false, false),

    //---------------------------------------------------------------------------------------------------------------------------------------
    // Instrument

    KEY_INSTRUMENT_ROOT                             ("Instrument.",                                     false, false, false, false, false, true),

    KEY_INSTRUMENT_STARIBUS_ADDRESS                 ("Instrument.Staribus.Address",                     false, false, false, false, false, true),
    KEY_INSTRUMENT_TIMECONSTANT                     ("Instrument.TimeConstant",                         false, false, false, false, false, true),

    KEY_INSTRUMENT_ANTENNA_ROOT                     ("Instrument.Antenna.",                             false, false, false, false, false, true),
    KEY_INSTRUMENT_ANTENNA_GALACTIC_LATITUDE        ("Instrument.Antenna.Galactic.Latitude",            false, false, false, false, false, true),
    KEY_INSTRUMENT_ANTENNA_GALACTIC_LONGITUDE       ("Instrument.Antenna.Galactic.Longitude",           false, false, false, false, false, true),
    KEY_INSTRUMENT_ANTENNA_BEAMWIDTH                ("Instrument.Antenna.Beamwidth",                    false, false, false, false, false, true),

    // Control Panel reserved Keys, for special displays
    // Used in InstrumentUIHelper.updateControlPanelIndicators()
    KEY_INSTRUMENT_RESERVED_ADDRESS                 ("Instrument.Reserved.Address",                     false, false, false, false, false, true),
    KEY_INSTRUMENT_RESERVED_PORT                    ("Instrument.Reserved.Port",                        false, false, false, false, false, true),

    //---------------------------------------------------------------------------------------------------------------------------------------
    // Controller (for completeness)

    KEY_CONTROLLER_ROOT                             ("Controller.",                                     false, false, false, false, false, false),

    //---------------------------------------------------------------------------------------------------------------------------------------
    // Metadata

    KEY_METADATA_ROOT                               ("Metadata.",                                       false, false, false, false, false, false),

    KEY_METADATA_NAME                               ("Metadata.Name.",                                  false, false, false, false, true,  false),
    KEY_METADATA_REGEX                              ("Metadata.Regex.",                                 false, false, false, false, true,  false),
    KEY_METADATA_DATATYPE                           ("Metadata.DataType.",                              false, false, false, false, true,  false),
    KEY_METADATA_UNITS                              ("Metadata.Units.",                                 false, false, false, false, true,  false),
    KEY_METADATA_DESCRIPTION                        ("Metadata.Description.",                           false, false, false, false, true,  false),

    //---------------------------------------------------------------------------------------------------------------------------------------
    // General-purpose Column

    KEY_COLUMN_ROOT                                 ("Column.",                                         false, false, false, false, true,  false),

    KEY_COLUMN_NAME                                 ("Column.Name.",                                    false, false, false, false, true,  false),
    KEY_COLUMN_REGEX                                ("Column.Regex.",                                   false, false, false, false, true,  false),
    KEY_COLUMN_DATATYPE                             ("Column.DataType.",                                false, false, false, false, true,  false),
    KEY_COLUMN_UNITS                                ("Column.Units.",                                   false, false, false, false, true,  false),
    KEY_COLUMN_DESCRIPTION                          ("Column.Description.",                             false, false, false, false, true,  false),

    //---------------------------------------------------------------------------------------------------------------------------------------
    // Radio Sky Pipe

    KEY_RSP_ROOT                                    ("Rsp.",                                            false, false, false, false, false, false),

    KEY_RSP_VERSION                                 ("Rsp.Version",                                     false, false, false, false, false, false),
    KEY_RSP_MAX_Y                                   ("Rsp.MaxY",                                        false, false, false, false, false, false),
    KEY_RSP_MIN_Y                                   ("Rsp.MinY",                                        false, false, false, false, false, false),
    KEY_RSP_SOURCE                                  ("Rsp.Source",                                      false, false, false, false, false, false),
    KEY_RSP_NOTE_LENGTH                             ("Rsp.Note.Length",                                 false, false, false, false, false, false),
    KEY_RSP_NOTE_RAW                                ("Rsp.Note.Raw",                                    false, false, false, false, false, false),
    KEY_RSP_TIMESTAMPED                             ("Rsp.Timestamped",                                 false, false, false, false, false, false),
    KEY_RSP_DOUBLE                                  ("Rsp.Double",                                      false, false, false, false, false, false),

    //---------------------------------------------------------------------------------------------------------------------------------------
    // Miscellaneous

    // The Observatory Clock Indicator Key
    // This is marked 'isChannel' because it requires the TimeSystem mnemonic to be complete
    KEY_CLOCK_DISPLAY_TIME_SYSTEM                   ("Clock.Display.TimeSystem.",                       false, false, false, false,  true, false),

    // A handy prefix for debugging
    KEY_TEST_ROOT                                   ("Test.",                                           false, false, false, false,  true, false);

    //---------------------------------------------------------------------------------------------------------------------------------------
    // Suffixes

    public static final String SUFFIX_NAME          = "Name";
    public static final String SUFFIX_DATA_TYPE     = "DataType";
    public static final String SUFFIX_UNITS         = "Units";
    public static final String SUFFIX_DESCRIPTION   = "Description";

    // Channels
    public static final String SUFFIX_CHANNEL_ZERO  = "0";
    public static final String SUFFIX_CHANNEL_ONE   = "1";
    public static final String SUFFIX_CHANNEL_TWO   = "2";

    // Used for Y Axis
    public static final String SUFFIX_SERIES_ZERO   = "0";
    public static final String SUFFIX_SERIES_ONE    = "1";

    // Column Indices
    public static final String SUFFIX_COLUMN_0      = "0";
    public static final String SUFFIX_COLUMN_1      = "1";
    public static final String SUFFIX_COLUMN_2      = "2";
    public static final String SUFFIX_COLUMN_3      = "3";
    public static final String SUFFIX_COLUMN_4      = "4";
    public static final String SUFFIX_COLUMN_5      = "5";
    public static final String SUFFIX_COLUMN_6      = "6";
    public static final String SUFFIX_COLUMN_7      = "7";
    public static final String SUFFIX_COLUMN_8      = "8";
    public static final String SUFFIX_COLUMN_9      = "9";
    public static final String SUFFIX_COLUMN_10     = "10";
    public static final String SUFFIX_COLUMN_11     = "11";
    public static final String SUFFIX_COLUMN_12     = "12";
    public static final String SUFFIX_COLUMN_13     = "13";
    public static final String SUFFIX_COLUMN_14     = "14";
    public static final String SUFFIX_COLUMN_15     = "15";

    //---------------------------------------------------------------------------------------------------------------------------------------

    private static final Logger LOGGER = Logger.getInstance();

    private final String strKey;
    private final boolean boolIsFramework;
    private final boolean boolIsObservatory;
    private final boolean boolIsObserver;
    private final boolean boolIsObservation;
    private final boolean boolIsChannel;
    private final boolean boolIsInstrument;


    /***********************************************************************************************
     * Get the MetadataDictionary enum corresponding to the specified MetadataDictionary entry Key.
     * Return NULL if the MetadataDictionary Key is not found.
     *
     * @param key
     *
     * @return MetadataDictionary
     */

    public static MetadataDictionary getMetadataDictionaryEntryForKey(final String key)
        {
        final String SOURCE = "MetadataDictionary.getMetadataDictionaryEntryForKey() ";
        MetadataDictionary dictionary;

        dictionary = null;

        if ((key != null)
            && (!FrameworkStrings.EMPTY_STRING.equals(key)))
            {
            final MetadataDictionary[] types;
            boolean boolFoundIt;

            types = values();
            boolFoundIt = false;

            for (int i = 0;
                 (!boolFoundIt) && (i < types.length);
                 i++)
                {
                final MetadataDictionary dictionaryKey;

                dictionaryKey = types[i];

                if (key.equals(dictionaryKey.getKey()))
                    {
                    dictionary = dictionaryKey;
                    boolFoundIt = true;
                    }
                }
            }

        return (dictionary);
        }


    /***********************************************************************************************
     * Check to see if the specified Metadata Key is in the Dictionary.
     * Remember that some entries are incomplete by design,
     * i.e. they need to have a ChannelID appended, which must be in the range {0...31}.
     * Also for some we can't know the whole Key in advance, e.g. Controller or Instrument.
     *
     * @param key
     *
     * @return boolean
     */

    public static boolean isValidMetadataDictionaryKey(final String key)
        {
        final boolean boolIsValid;

        // First check the complete Key
        // Return NULL if the MetadataDictionary name is not found
        if (getMetadataDictionaryEntryForKey(key) != null)
            {
            boolIsValid = true;
            }

        // Now check to see if the Key is incomplete
        else if ((key != null)
            && (key.endsWith(FrameworkStrings.DOT)))
            {
            // An incomplete key cannot ever be valid
            boolIsValid = false;
            }

        // Now see if there is a ChannelID as the last token of the Key
        else if ((key != null)
             && (isLastKeyTokenChannelId(key)))
            {
            final String strPrunedKey;
            final MetadataDictionary metadataDictionary;

            // Prune off the ChannelID and see if what's left is valid as an incomplete Key,
            // and the dictionary entry is flagged as isChannel()
            // Remember to leave the trailing '.' because that is in the Dictionary
            strPrunedKey = key.substring(0, key.lastIndexOf(FrameworkStrings.DOT) + 1);
            metadataDictionary = getMetadataDictionaryEntryForKey(strPrunedKey);

            boolIsValid = (metadataDictionary != null)
                          && (metadataDictionary.isChannel());
            }

        else if ((key != null)
             && (isLastTokenMetadataSuffix(key)))
            {
            final String strPrunedKey;
            final MetadataDictionary metadataDictionary;

            // Prune off the last token and see if what's left is valid as an incomplete Key,
            // and the dictionary entry is flagged as isChannel()
            // Remember to leave the trailing '.' because that is in the Dictionary
            strPrunedKey = key.substring(0, key.lastIndexOf(FrameworkStrings.DOT) + 1);
            metadataDictionary = getMetadataDictionaryEntryForKey(strPrunedKey);

            boolIsValid = (metadataDictionary != null)
                          && (metadataDictionary.isChannel());
            }

        // See if the Key refers to Instrument
        // If so, we probably just accept it so long as it doesn't end with a dot
        else if ((key != null)
             && (key.startsWith(KEY_INSTRUMENT_ROOT.getKey()))
             && (!key.endsWith(FrameworkStrings.DOT)))
            {
            // Instrument keys could get too complicated to parse completely
            boolIsValid = true;
            }

        // See if the Key refers to Controller
        // If so, we probably just accept it so long as it doesn't end with a dot
        else if ((key != null)
             && (key.startsWith(KEY_CONTROLLER_ROOT.getKey()))
             && (!key.endsWith(FrameworkStrings.DOT)))
            {
            // Controller keys could get too complicated to parse completely
            boolIsValid = true;
            }

        // See if it is a Test Key
        else if ((key != null)
             && (key.startsWith(KEY_TEST_ROOT.getKey()))
             && (!key.endsWith(FrameworkStrings.DOT)))
            {
            // Accept all Test Keys
            boolIsValid = true;
            }

        // Anything else must fail anyway
        else
            {
            boolIsValid = false;
            }

        return (boolIsValid);
        }


    /***********************************************************************************************
     * See if there is a ChannelID as the last token of the Key.
     * We know that the Key does not end with '.'.
     *
     * @param key
     *
     * @return boolean
     */

    public static boolean isLastKeyTokenChannelId(final String key)
        {
        boolean boolIsValid;

        if ((key != null)
             && (!FrameworkStrings.EMPTY_STRING.equals(key))
             && (key.contains(FrameworkStrings.DOT)))
            {
            try
                {
                final String strLastToken;
                final int intChannelID;

                strLastToken = key.substring(key.lastIndexOf(FrameworkStrings.DOT) + 1);
                intChannelID = Integer.parseInt(strLastToken);

                // Is this token a valid integer in the range {0...31} ?
                boolIsValid = ((intChannelID >=0 ) && (intChannelID < ObservatoryInterface.MAX_CHANNELS));
                }

            catch (NumberFormatException exception)
                {
                // All invalid integers must fail
                boolIsValid = false;
                }
            }
        else
            {
            boolIsValid = false;
            }

        return (boolIsValid);
        }


    /***********************************************************************************************
     * See if there is a Metadata suffix as the last token of the Key.
     * We know that the Key does not end with '.'.
     *
     * @param key
     *
     * @return boolean
     */

    private static boolean isLastTokenMetadataSuffix(final String key)
        {
        final boolean boolIsValid;

        if ((key != null)
             && (!FrameworkStrings.EMPTY_STRING.equals(key))
             && (key.contains(FrameworkStrings.DOT))
             && (key.startsWith(KEY_METADATA_ROOT.getKey())))
            {
            final String strLastToken;

            strLastToken = key.substring(key.lastIndexOf(FrameworkStrings.DOT) + 1);

            // Is this token a valid Metadata Suffix?
            boolIsValid = (SUFFIX_NAME.equals(strLastToken))
                            || (SUFFIX_DATA_TYPE.equals(strLastToken))
                            || (SUFFIX_UNITS.equals(strLastToken))
                            || (SUFFIX_DESCRIPTION.equals(strLastToken));
            }
        else
            {
            boolIsValid = false;
            }

        return (boolIsValid);
        }


    /***********************************************************************************************
     * Construct a MetadataDictionary entry.
     *
     * @param key
     * @param isframework
     * @param isobservatory
     * @param isobserver
     * @param isobservation
     * @param ischannel
     * @param iisinstrument
     */

    private MetadataDictionary(final String key,
                               final boolean isframework,
                               final boolean isobservatory,
                               final boolean isobserver,
                               final boolean isobservation,
                               final boolean ischannel,
                               final boolean iisinstrument)
        {
        strKey = key;
        boolIsFramework = isframework;
        boolIsObservatory = isobservatory;
        boolIsObserver = isobserver;
        boolIsObservation = isobservation;
        boolIsChannel = ischannel;
        boolIsInstrument = iisinstrument;
        }


    /***********************************************************************************************
     * Get the MetadataDictionary entry key.
     *
     * @return String
     */

    public String getKey()
        {
        return(this.strKey);
        }


    /***********************************************************************************************
     * Indicate if this MetadataDictionary entry key is related to Observatory.
     *
     * @return boolean
     */

    public boolean isObservatory()
        {
        return (this.boolIsObservatory);
        }


    /***********************************************************************************************
     * Indicate if this MetadataDictionary entry key is related to Framework.
     *
     * @return boolean
     */

    public boolean isFramework()
        {
        return (this.boolIsFramework);
        }


    /***********************************************************************************************
     * Indicate if this MetadataDictionary entry key is related to Observer.
     *
     * @return boolean
     */

    public boolean isObserver()
        {
        return (this.boolIsObserver);
        }


    /***********************************************************************************************
     * Indicate if this MetadataDictionary entry key is related to Observation.
     *
     * @return boolean
     */

    public boolean isObservation()
        {
        return (this.boolIsObservation);
        }


    /***********************************************************************************************
     * Indicate if this MetadataDictionary entry key is related to Channel.
     *
     * @return boolean
     */

    public boolean isChannel()
        {
        return (this.boolIsChannel);
        }


    /***********************************************************************************************
     * Indicate if this MetadataDictionary entry key is related to Instrument.
     *
     * @return boolean
     */

    public boolean isInstrument()
        {
        return (this.boolIsInstrument);
        }


    /***********************************************************************************************
     * Get the MetadataDictionary entry key as a String.
     *
     * @return String
     */

    public String toString()
        {
        return (this.strKey);
        }
    }
