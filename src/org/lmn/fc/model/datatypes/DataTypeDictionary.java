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

package org.lmn.fc.model.datatypes;

import org.lmn.fc.model.datatypes.parsers.*;
import org.lmn.fc.model.logging.Logger;
import org.lmn.fc.model.xmlbeans.datatypes.SchemaDataType;

import static org.lmn.fc.common.constants.FrameworkRegex.*;
import static org.lmn.fc.common.constants.FrameworkStrings.EMPTY_STRING;


/***************************************************************************************************
 * DataTypeDictionary.
 */

public enum DataTypeDictionary
    {
    // The Schema Names of these types must be kept in step with those in SchemaDataType in datatypes.xsd

    //-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    // These Types may be used in Metadata
    // Those without Regex are suspect, since parsing will probably fail....
    // Don't bother with a static import for SchemaDataType because of name conflicts...

    // Name                     SchemaDataType                                Schema Name                     Display Name                Metadata Numeric Radix Width   Regex                       Dummy Data Template                        Parser Instance                       Chooser Classname

    // Numeric
    UNSIGNED_DECIMAL_BYTE       (SchemaDataType.UNSIGNED_DECIMAL_BYTE,        "UnsignedDecimalByte",         "Byte (Unsigned Decimal)",       true,  true, 10,    3,  REGEX_UNSIGNED_DECIMAL_BYTE,   "255",                                     UnsignedDecimalByteParser.getInstance(),    EMPTY_STRING),
    SIGNED_DECIMAL_BYTE         (SchemaDataType.SIGNED_DECIMAL_BYTE,          "SignedDecimalByte",           "Byte (Signed Decimal)",         true,  true, 10,    4,  REGEX_SIGNED_DECIMAL_BYTE,     "-127",                                    SignedDecimalByteParser.getInstance(),      EMPTY_STRING),

    DECIMAL_DIGIT               (SchemaDataType.DECIMAL_DIGIT,                "DecimalDigit",                "Digit (Decimal)",               true,  true, 10,    1,  REGEX_DECIMAL_DIGIT,           "7",                                       DecimalDigitParser.getInstance(),           EMPTY_STRING),
    DECIMAL_INTEGER             (SchemaDataType.DECIMAL_INTEGER,              "DecimalInteger",              "Integer (Decimal)",             true,  true, 10,    4,  REGEX_SIGNED_DECIMAL_INTEGER,  "9999",                                    DecimalIntegerParser.getInstance(),         EMPTY_STRING),
    DECIMAL_FLOAT               (SchemaDataType.DECIMAL_FLOAT,                "DecimalFloat",                "Float (Decimal)",               true,  true, 10,   50,  REGEX_SIGNED_DECIMAL_FLOAT,    "1.234",                                   DecimalFloatParser.getInstance(),           EMPTY_STRING),
    DECIMAL_DOUBLE              (SchemaDataType.DECIMAL_DOUBLE,               "DecimalDouble",               "Double (Decimal)",              true,  true, 10,   50,  REGEX_SIGNED_DECIMAL_DOUBLE,   "-1.234E-04",                              DecimalDoubleParser.getInstance(),          EMPTY_STRING),

    UNSIGNED_HEX_BYTE           (SchemaDataType.UNSIGNED_HEX_BYTE,            "UnsignedHexByte",             "Byte (Unsigned Hex)",           true,  true, 16,    2,  REGEX_UNSIGNED_HEX_BYTE,       "FF",                                      UnsignedHexByteParser.getInstance(),        EMPTY_STRING),
    SIGNED_HEX_BYTE             (SchemaDataType.SIGNED_HEX_BYTE,              "SignedHexByte",               "Byte (Signed Hex)",             true,  true, 16,    3,  REGEX_SIGNED_HEX_BYTE,         "-7F",                                     SignedHexByteParser.getInstance(),          EMPTY_STRING),

    HEX_DIGIT                   (SchemaDataType.HEX_DIGIT,                    "HexDigit",                    "Digit (Hex)",                   true,  true, 16,    1,  REGEX_HEX_DIGIT,               "F",                                       HexDigitParser.getInstance(),               EMPTY_STRING),
    HEX_INTEGER                 (SchemaDataType.HEX_INTEGER,                  "HexInteger",                  "Integer (Hex)",                 true,  true, 16,    4,  REGEX_HEX_INTEGER_FOUR,        "ABCD",                                    HexIntegerParser.getInstance(),             EMPTY_STRING),

    // Date and Time
    DATE_YYYY_MM_DD             (SchemaDataType.DATE,                         "Date",                        "Date",                          true, false,  0,   10,  REGEX_DATE_ISO_YYYY_MM_DD,     "2011-11-20",                              YearMonthDayParser.getInstance(),           EMPTY_STRING),
    TIME_HH_MM_SS               (SchemaDataType.TIME,                         "Time",                        "Time",                          true, false,  0,    8,  REGEX_TIME_ISO_HH_MM_SS,       "12:34:56",                                HourMinSecParser.getInstance(),             EMPTY_STRING),
    TIME_ZONE                   (SchemaDataType.TIME_ZONE,                    "TimeZone",                    "TimeZone",                      true, false,  0,    9,  REGEX_TIMEZONE,                "GMT+00:00",                               TimeZoneParser.getInstance(),               EMPTY_STRING),

    // Angles
    ANGLE                       (SchemaDataType.ANGLE,                        "Angle",                       "Angle",                         true, false, 10,   15,  REGEX_ANGLE_SIGNED,            "+359.9999999999",                         AngleParser.getInstance(),                  EMPTY_STRING),
    AZIMUTH                     (SchemaDataType.AZIMUTH,                      "Azimuth",                     "Azimuth",                       true, false, 10,   14,  REGEX_AZIMUTH_DEG_UNSIGNED,    "359.9999999999",                          AzimuthParser.getInstance(),                EMPTY_STRING),
    ELEVATION                   (SchemaDataType.ELEVATION,                    "Elevation",                   "Elevation",                     true, false, 10,   13,  REGEX_ELEVATION_DEG_UNSIGNED,  "89.9999999999",                           ElevationParser.getInstance(),              EMPTY_STRING),

    LATITUDE                    (SchemaDataType.LATITUDE,                     "Latitude",                    "Latitude",                      true, false, 10,   14,  REGEX_LATITUDE_DMS_SIGNED,     "+89:59:59.9999",                          LatitudeParser.getInstance(),               EMPTY_STRING),
    GALACTIC_LATITUDE           (SchemaDataType.GALACTIC_LATITUDE,            "GalacticLatitude",            "Galactic Latitude",             true, false, 10,   14,  REGEX_LATITUDE_DMS_SIGNED,     "+89:59:59.9999",                          LatitudeParser.getInstance(),               EMPTY_STRING),

    UNSIGNED_LONGITUDE          (SchemaDataType.UNSIGNED_LONGITUDE,           "UnsignedLongitude",           "Unsigned Longitude",            true, false, 10,   14,  REGEX_LONGITUDE_DMS_UNSIGNED,  "359:59:59.9999",                          UnsignedLongitudeParser.getInstance(),      EMPTY_STRING),
    GALACTIC_LONGITUDE          (SchemaDataType.GALACTIC_LONGITUDE,           "GalacticLongitude",           "Galactic Longitude",            true, false, 10,   14,  REGEX_LONGITUDE_DMS_UNSIGNED,  "359:59:59.9999",                          UnsignedLongitudeParser.getInstance(),      EMPTY_STRING),

    LONGITUDE                   (SchemaDataType.LONGITUDE,                    "Longitude",                   "Signed Longitude",              true, false, 10,   15,  REGEX_LONGITUDE_DMS_SIGNED,    "+179:59:59.9999",                         SignedLongitudeParser.getInstance(),        EMPTY_STRING),
    SIGNED_LONGITUDE            (SchemaDataType.SIGNED_LONGITUDE,             "SignedLongitude",             "Signed Longitude",              true, false, 10,   15,  REGEX_LONGITUDE_DMS_SIGNED,    "+179:59:59.9999",                         SignedLongitudeParser.getInstance(),        EMPTY_STRING),

    DECLINATION                 (SchemaDataType.DECLINATION,                  "Declination",                 "Declination",                   true, false, 10,   14,  REGEX_DECLINATION_DMS_SIGNED,  "+89:59:59.9999",                          DeclinationParser.getInstance(),            EMPTY_STRING),
    RIGHT_ASCENSION             (SchemaDataType.RIGHT_ASCENSION,              "RightAscension",              "Right Ascension",               true, false, 10,   13,  REGEX_RIGHT_ASCENSION_HMS,     "23:59:59.9999",                           RightAscensionParser.getInstance(),         EMPTY_STRING),

    // The following are to allow editing of Metadata with DataTypes and Units specified as Values
    METADATA_KEY                (SchemaDataType.METADATA_KEY,                 "MetadataKey",                 "Metadata Key",                  true, false,  0,  100,  REGEX_METADATA_KEY,            "One.Two.Three",                           MetadataKeyParser.getInstance(),            "org.lmn.fc.ui.choosers.impl.MetadataKeyChooser"),
    DATA_TYPE                   (SchemaDataType.DATA_TYPE,                    "DataType",                    "Data Type",                     true, false,  0,   25,  REGEX_AWAITING_DEVELOPMENT,    "DecimalInteger",                          DataTypeParser.getInstance(),               "org.lmn.fc.ui.choosers.impl.DataTypeChooser"),
    UNITS                       (SchemaDataType.UNITS,                        "Units",                       "Units",                         true, false,  0,   20,  REGEX_AWAITING_DEVELOPMENT,    "Dimensionless",                           UnitsParser.getInstance(),                  "org.lmn.fc.ui.choosers.impl.UnitsChooser"),

    // Miscellaneous
    STRING                      (SchemaDataType.STRING,                       "String",                      "String",                        true, false,  0, 1000,  REGEX_STRING,                  "A sample string",                         StringParser.getInstance(),                 EMPTY_STRING),
    FILE_NAME                   (SchemaDataType.FILE_NAME,                    "FileName",                    "File Name",                     true, false,  0,  100,  REGEX_FILENAME,                "a/b/filename.ext",                        FileNameParser.getInstance(),               "org.lmn.fc.ui.choosers.impl.FileNameChooser"),
    PATH_NAME                   (SchemaDataType.PATH_NAME,                    "PathName",                    "Path Name",                     true, false,  0,  100,  REGEX_PATHNAME,                "foldera/folderb/",                        PathNameParser.getInstance(),               "org.lmn.fc.ui.choosers.impl.PathNameChooser"),
    URL                         (SchemaDataType.URL,                          "URL",                         "URL",                           true, false,  0,  100,  REGEX_URL,                     "http://www.ukraa.com",                    UrlParser.getInstance(),                    EMPTY_STRING),

    // This is potentially Numeric, because the value of the expression is a double. It is also a String, when left as the expression.
    MATHEMATICAL_EXPRESSION     (SchemaDataType.MATHEMATICAL_EXPRESSION,      "MathematicalExpression",      "Mathematical Expression",       true, true,   0, 1000,  REGEX_AWAITING_DEVELOPMENT,    "(2^3-1)*sin(pi/4)/ln(pi^2)",              MathematicalExpressionParser.getInstance(), EMPTY_STRING),

    BOOLEAN                     (SchemaDataType.BOOLEAN,                      "Boolean",                     "Boolean",                       true, false,  2,    1,  REGEX_BOOLEAN,                 "true",                                    BooleanParser.getInstance(),                EMPTY_STRING),
    BIT_STRING                  (SchemaDataType.BIT_STRING,                   "BitString",                   "Bit String",                    true, false,  2,  100,  REGEX_BIT_STRING,              "101010101010",                            BitStringParser.getInstance(),              EMPTY_STRING),
    COLOUR_DATA                 (SchemaDataType.COLOUR_DATA,                  "ColourData",                  "Colour (rgb)",                  true, false,  0,   17,  REGEX_COLOUR,                  "r=000 b=000 g=000",                       ColourParser.getInstance(),                 "org.lmn.fc.ui.choosers.impl.ColourChooser"),
    FONT_DATA                   (SchemaDataType.FONT_DATA,                    "FontData",                    "Font",                          true, false,  0,  100,  REGEX_FONT,                    "font=dialog style=plain size=12",         FontParser.getInstance(),                   "org.lmn.fc.ui.choosers.impl.FontChooser"),

    // Bus and Network Addresses
    STARIBUS_ADDRESS            (SchemaDataType.STARIBUS_ADDRESS,             "StaribusAddress",             "Staribus Address",              true,  true, 10,    3,  REGEX_STARIBUS_ADDRESS,        "254",                                     StaribusAddressParser.getInstance(),        EMPTY_STRING),
    IPv4_ADDRESS                (SchemaDataType.I_PV_4_ADDRESS,               "IPv4Address",                 "IPv4 Address",                  true, false, 10,   15,  REGEX_IPv4_ADDRESS,            "192.168.123.123",                         IPv4AddressParser.getInstance(),            EMPTY_STRING),
    IPv6_ADDRESS                (SchemaDataType.I_PV_6_ADDRESS,               "IPv6Address",                 "IPv6 Address",                  true, false, 16,   39,  REGEX_IPv6_ADDRESS,            "2001:0DB8:AC10:FE01:0000:0000:0000:0000", IPv6AddressParser.getInstance(),            EMPTY_STRING),

    //-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    // These Types are probably not suitable for Metadata, but I am open to suggestions :-)

    SCALED_HEX_INTEGER          (SchemaDataType.SCALED_HEX_INTEGER,           "ScaledHexInteger",            "Scaled Integer (Hex)",         false,  true, 16,    4,  REGEX_AWAITING_DEVELOPMENT,    "ABCD",                                    ScaledHexIntegerParser.getInstance(),       EMPTY_STRING),
    NUMERIC_INDEXED_LIST        (SchemaDataType.NUMERIC_INDEXED_LIST,         "NumericIndexedList",          "Indexed List (Hex)",           false, false, 16,    0,  REGEX_AWAITING_DEVELOPMENT,    "1",                                       NumericIndexedListParser.getInstance(),     EMPTY_STRING),
    STARIBUS_BLOCK              (SchemaDataType.STARIBUS_BLOCK,               "StaribusBlock",               "Staribus Block",               false, false,  0,  512,  REGEX_AWAITING_DEVELOPMENT,    "DataType Awaiting Development",           StaribusBlockParser.getInstance(),          EMPTY_STRING),
    STARIBUS_MULTICHANNEL_DATA  (SchemaDataType.STARIBUS_MULTICHANNEL_DATA,   "StaribusMultichannelData",    "Staribus Multichannel Data",   false, false,  0, 1000,  REGEX_AWAITING_DEVELOPMENT,    "1234!5678!0987!2345",                     StaribusMultichannelDataParser.getInstance(), EMPTY_STRING),

    LIST_DATA                   (SchemaDataType.LIST_DATA,                    "ListData",                    "List Data",                    false, false,  0,    0,  REGEX_AWAITING_DEVELOPMENT,    "DataType Awaiting Development",           ListParser.getInstance(),                   EMPTY_STRING),
    VECTOR_DATA                 (SchemaDataType.VECTOR_DATA,                  "VectorData",                  "Vector Data",                  false, false,  0,    0,  REGEX_AWAITING_DEVELOPMENT,    "DataType Awaiting Development",           VectorDataParser.getInstance(),             EMPTY_STRING),
    XY_DATASET                  (SchemaDataType.XY_DATASET,                   "XYDataset",                   "XY Dataset",                   false, false,  0,    0,  REGEX_AWAITING_DEVELOPMENT,    "DataType Awaiting Development",           XYDatasetParser.getInstance(),              EMPTY_STRING),
    IMAGE_DATA                  (SchemaDataType.IMAGE_DATA,                   "ImageData",                   "Image Data",                   false, false,  0,    0,  REGEX_AWAITING_DEVELOPMENT,    "DataType Awaiting Development",           ImageParser.getInstance(),                  EMPTY_STRING),

    XML_DATA                    (SchemaDataType.XML,                          "XML",                         "XML",                          false, false,  0,    0,  REGEX_XML,                     "<Name>Some XML content</Name>",           XmlDataParser.getInstance(),                EMPTY_STRING);


    private static final Logger LOGGER = Logger.getInstance();

    private final SchemaDataType.Enum schemaDataType;
    private final String strName;
    private final String strDisplayName;
    private final boolean boolIsMetadataType;
    private final boolean boolIsNumeric;
    private final int intRadix;
    private final int intFieldWidth;
    private final String strRegex;
    private final String strDummyValue;
    private final DataTypeParserInterface parserInstance;
    private final String strChooserClassname;


    /***********************************************************************************************
     * Get the DataTypeDictionary enum corresponding to the specified DataTypeDictionary entry name.
     * Return NULL if not found.
     *
     * @param name
     *
     * @return DataType
     */

    public static DataTypeDictionary getDataTypeDictionaryEntryForName(final String name)
        {
        final String SOURCE = "DataTypeDictionary.getDataTypeDictionaryEntryForName() ";
        DataTypeDictionary dataType;

        dataType = null;

        if ((name != null)
            && (!EMPTY_STRING.equals(name)))
            {
            final DataTypeDictionary[] arrayDataTypes;
            boolean boolFoundIt;

            arrayDataTypes = DataTypeDictionary.values();
            boolFoundIt = false;

            for (int i = 0;
                 (!boolFoundIt) && (i < arrayDataTypes.length);
                 i++)
                {
                final DataTypeDictionary type;

                type = arrayDataTypes[i];

                if (name.equals(type.getName()))
                    {
                    dataType = type;
                    boolFoundIt = true;
                    }
                }
            }

        // Debug only, because NULL is sometimes used as a test
//        if (dataType == null)
//            {
//            LOGGER.error(SOURCE + "Could not locate [name=" + name + "]");
//            }

        return (dataType);
        }


    /***********************************************************************************************
     * Get the DataTypeParser corresponding to the specified DataType name.
     * Return NULL if not found,
     *
     * @param name
     *
     * @return DataTypeParserInterface
     */

    public static DataTypeParserInterface getDataTypeParserForName(final String name)
        {
        DataTypeParserInterface parser;
        final DataTypeDictionary dataType;

        parser = null;
        dataType = getDataTypeDictionaryEntryForName(name);

        if (dataType != null)
            {
            parser = dataType.getParser();
            }

        return (parser);
        }


    /***********************************************************************************************
     * Construct a DataTypeDictionary entry.
     *
     * @param schemadatatype
     * @param name
     * @param displayname
     * @param ismetadatatype
     * @param isnumeric
     * @param radix
     * @param fieldwidth
     * @param regex
     * @param dummyvalue
     * @param parser
     * @param chooserclassname
     */

    private DataTypeDictionary(final SchemaDataType.Enum schemadatatype,
                               final String name,
                               final String displayname,
                               final boolean ismetadatatype,
                               final boolean isnumeric,
                               final int radix,
                               final int fieldwidth,
                               final String regex,
                               final String dummyvalue,
                               final DataTypeParserInterface parser,
                               final String chooserclassname)
        {
        this.schemaDataType = schemadatatype;
        this.strName = name;
        this.strDisplayName = displayname;
        this.boolIsMetadataType = ismetadatatype;
        this.boolIsNumeric = isnumeric;
        this.intRadix = radix;
        this.intFieldWidth = fieldwidth;
        this.strRegex = regex;
        this.strDummyValue = dummyvalue;
        this.parserInstance = parser;
        this.strChooserClassname = chooserclassname;
        }


    /***********************************************************************************************
     * Get the SchemaDataType Enum associated with the DataTypeDictionary entry.
     *
     * @return String
     */

    public SchemaDataType.Enum getSchemaDataType()
        {
        return (this.schemaDataType);
        }


    /***********************************************************************************************
     * Get the name of the DataTypeDictionary entry.
     *
     * @return String
     */

    public String getName()
        {
        return (this.strName);
        }


    /***********************************************************************************************
     * Get the display name of the DataTypeDictionary entry.
     *
     * @return String
     */

    public String getDisplayName()
        {
        return (this.strDisplayName);
        }


    /***********************************************************************************************
     * Get the flag to indicate if this DataTypeDictionary entry may be used in Metadata.
     *
     * @return boolean
     */

    public boolean isMetadataType()
        {
        return (this.boolIsMetadataType);
        }


    /***********************************************************************************************
     * Get the flag to indicate if this DataTypeDictionary entry is numeric.
     *
     * @return boolean
     */

    public boolean isNumeric()
        {
        return (this.boolIsNumeric);
        }


    /***********************************************************************************************
     * Get the radix of the DataTypeDictionary entry.
     *
     * @return int
     */

    public int getRadix()
        {
        return (this.intRadix);
        }


    /***********************************************************************************************
     * Get the field width of the DataTypeDictionary entry.
     *
     * @return int
     */

    public int getFieldWidth()
        {
        return (this.intFieldWidth);
        }


    /***********************************************************************************************
     * Get the Regex to use for parsing this DataTypeDictionary entry.
     *
     * @return String
     */

    public String getRegex()
        {
        return (this.strRegex);
        }


    /***********************************************************************************************
     * Get the dummy Value to use for stub DAOs etc.
     *
     * @return String
     */

    public String getDummyValue()
        {
        return (this.strDummyValue);
        }


    /***********************************************************************************************
     * Get an instance of a DataTypeParser.
     *
     * @return String
     */

    public DataTypeParserInterface getParser()
        {
        return (this.parserInstance);
        }


    /***********************************************************************************************
     * Get the Chooser classname for this DataTypeDictionary entry.
     *
     * @return String
     */

    public String getChooserClassname()
        {
        return (this.strChooserClassname);
        }


    /***********************************************************************************************
     * Show the name of the DataTypeDictionary entry.
     *
     * @return String
     */

    public String toString()
        {
        return (this.strName);
        }
    }
