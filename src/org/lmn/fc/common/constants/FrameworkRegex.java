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

package org.lmn.fc.common.constants;


/*****************************************************************************************************************************************************
 * FrameworkRegex.
 * Test Installer Regex in http://jakarta.apache.org/regexp/applet.html NOT IntelliJ!
 */

public interface FrameworkRegex
    {
    //  Reference Regex from Installer
    //---------------------------------------------------------------------------------------------
    //  Framework
    //    All data entered in the Framework Panel is mandatory.
    //
    //    Country : [A-Z][A-Z]
    //    TimeZone : ^GMT[+/-]0*([0-9]|1[0-9]|2[0-3]):0*([0-9]|[1-5][0-9])$
    //    Latitude : ^([+\-]((00)|([0-8][0-9])|([0-9])|([1-8][0-9])):(([0-9])|(0[0-9])|([1-5][0-9])):(([0-9])|(0[0-9])|([1-5][0-9]))(\.([0-9]{1,8}))*)$
    //    Longitude : ^([+\-]((0)|([0-9])|(00)|(0[0-9][0-9])|(000)|(0[1-9])|([1-9][0-9])|(1[0-7][0-9])):(([0-9])|(0[0-9])|([1-5][0-9])):(([0-9])|(0[0-9])|([1-5][0-9]))(\.([0-9]){1,8})*)$
    //    HASL : -?([0-9])([0-9])?([0-9])?([0-9])?\.([0-9])
    //
    //---------------------------------------------------------------------------------------------
    //  Observatory
    //    Observatory Metadata mandatory where indicated.
    //
    //    Observatory Name : ([a-zA-Z0-9'.,\- ])*  Mandatory.
    //    Observatory Description : ([a-zA-Z0-9'.,/ \-])*
    //    Website Address : ^(((http|https|ftp)\://(([a-zA-Z0-9_\-\.]+)\.([a-zA-Z]{2,5}){1,25})(/[a-zA-Z0-9_\-\.=\u0026\~\?]+)*)?)$
    //    Email Address : ^(([a-zA-Z0-9_\-\.]+)@([a-zA-Z0-9_\-\.]+)\.([a-zA-Z]{2,5}){1,25})*$
    //    Telephone : ([/+0-9\s]+)?   Izpack uses a special field for this data so the regex would not be usable in Starbase.
    //    Address : ([a-zA-Z0-9'.,/ \-])*
    //    PostCode : ([A-Z0-9. ])*
    //    Country : [A-Z][A-Z] Mandatory
    //    TimeZone : ^GMT[+/-]0*([0-9]|1[0-9]|2[0-3]):0*([0-9]|[1-5][0-9])$
    //    Latitude : ^([+\-]((00)|([0-8][0-9])|([0-9])|([1-8][0-9])):(([0-9])|(0[0-9])|([1-5][0-9])):(([0-9])|(0[0-9])|([1-5][0-9]))(\.([0-9]{1,8}))*)?$
    //    Latitude FIX: ^([+\-]((00)|([0-8][0-9])|([0-9])|([1-8][0-9])):((0[0-9])|([1-5][0-9])):((0[0-9])|([1-5][0-9]))(\.([0-9]{1,8}))*)?$
    //    Longitude : ^([+\-]((0)|([0-9])|(00)|(0[0-9][0-9])|(000)|(0[1-9])|([1-9][0-9])|(1[0-7][0-9])):(([0-9])|(0[0-9])|([1-5][0-9])):(([0-9])|(0[0-9])|([1-5][0-9]))(\.([0-9]){1,8})*)?$
    //    Longitude FIX: ^([+\-]((0)|([0-9])|(00)|(0[0-9][0-9])|(000)|(0[1-9])|([1-9][0-9])|(1[0-7][0-9])):((0[0-9])|([1-5][0-9])):((0[0-9])|([1-5][0-9]))(\.([0-9]){1,8})*)?$
    //    Maidenhead : ^([A-R]{2}[0-9]{2}[a-x]{2})?$
    //    HASL : ^-?(([0-9])([0-9])?([0-9])?([0-9])?\.([0-9]))?$
    //    Observatory Notes : ([a-zA-Z0-9'.,/ \-])*
    //
    //---------------------------------------------------------------------------------------------
    //  Observer
    //    Observer Metadata mandatory where indicated.
    //
    //    Name : ([a-zA-Z0-9'.,\- ])*  mandatory.
    //    Description : ([a-zA-Z0-9'.,/ \-])*
    //    Website : ^(((http|https|ftp)\://(([a-zA-Z0-9_\-\.]+)\.([a-zA-Z]{2,5}){1,25})(/[a-zA-Z0-9_\-\.=\u0026\~\?]+)*)?)$
    //    Email : ^(([a-zA-Z0-9_\-\.]+)@([a-zA-Z0-9_\-\.]+)\.([a-zA-Z]{2,5}){1,25})*$
    //    Telephone : ([/+0-9\s]+)?   Izpack uses a special field for this data so the regex would not be usable in Starbase.
    //    Address : ([a-zA-Z0-9'.,/ \-])*
    //    PostCode : ([A-Z0-9. ])*
    //    Country : [A-Z][A-Z] Mandatory
    //    Notes : ([a-zA-Z0-9'.,/ \-])*
    //---------------------------------------------------------------------------------------------

    // No Regex specified - a marker to help find work in progress...
    String REGEX_NONE = "";

    // Awaiting Development
    String REGEX_AWAITING_DEVELOPMENT = "";

    //------------------------------------------------------------------------------------------------------------------------------------------------
    // Numerics

    // A nominal upper limit of 16 channels.
    String REGEX_CHANNEL_COUNT = "^([0-9]|1[0-6])$";

    // {000...255}
    String REGEX_UNSIGNED_DECIMAL_BYTE = "^0*([0-9]{1,2}|1[0-9]{2}|2[0-4][0-9]|25[0-5])$";

    // {-127...000...+127}
    String REGEX_SIGNED_DECIMAL_BYTE ="^[+-]?([0-9]{1,2}|1[01][0-9]|12[0-7])$";

    String REGEX_DECIMAL_DIGIT = "^[0-9]$";

    // Allow a decimal part, but only if zero!
    // This allows the internal representation to be a double, but the Metadata can say DecimalInteger
    String REGEX_SIGNED_DECIMAL_INTEGER = "^[+-]?\\d{1,50}(\\.([0-9])+)?$";

    // Sign once or not at all, at least one digit, followed by a point and zero or more digits
    String REGEX_SIGNED_DECIMAL_FLOAT = "^[+-]?\\d{1,}(\\.\\d{1,})*$";

    // Improved! This was necessary (initially) to allow GOES data which has exponential format
    // It was taken from the Sun Javadoc, and the Hex part removed because this is a DecimalDouble, not a HexDouble...
    String REGEX_SIGNED_DECIMAL_DOUBLE = "^[\\x00-\\x20]*[+-]?(NaN|Infinity|((((\\p{Digit}+)(\\.)?((\\p{Digit}+)?)([eE][+-]?(\\p{Digit}+))?)|(\\.((\\p{Digit}+))([eE][+-]?(\\p{Digit}+))?)[fFdD]?)))[\\x00-\\x20]*$";

    String REGEX_DECIMAL_PERCENTAGE = "^(([0][0-9][0-9])(\\.([0-9])+)?)|((100)(\\.([0])+)?)$";

    // {00...FF}
    String REGEX_UNSIGNED_HEX_BYTE = "^[0-9A-Fa-f][0-9A-Fa-f]$";

    // {-7F...00...+7F}
    String REGEX_SIGNED_HEX_BYTE = "^[+-]?[0-7][0-9A-Fa-f]$";

    String REGEX_HEX_DIGIT = "^[0-9A-Fa-f]$";

    String REGEX_HEX_INTEGER_FOUR = "^[0-9A-Fa-f][0-9A-Fa-f][0-9A-Fa-f][0-9A-Fa-f]$";

    //------------------------------------------------------------------------------------------------------------------------------------------------
    // Dates and Times

    String REGEX_TIMESYSTEM = "^(LMT|UT|JD0|JD|GMST0|GAST0|GMST|GAST|LMST|LAST)$";

    // YYYY-MM-DD  ISO
    String REGEX_DATE_ISO_YYYY_MM_DD = "^(19|20)\\d\\d([-])(0[1-9]|1[012])\\2(0[1-9]|[12][0-9]|3[01])$";

    // YYYYMMDD for GOES and John Cook
    // Note that this will not work after 2099!
    String REGEX_DATE_YYMMDD = "20((\\d{2}((0[13578]|1[02])(0[1-9]|[12]\\d|3[01])|(0[13456789]|1[012])(0[1-9]|[12]\\d|30)|02(0[1-9]|1\\d|2[0-8])))|([02468][048]|[13579][26])0229)";

    // 00:00:00 to 23:59:59  ISO
    String REGEX_TIME_ISO_HH_MM_SS = "^(([0-1][0-9])|([2][0-3])):([0-5][0-9]):([0-5][0-9])$";

    // GMT-23:59 to GMT+00:00 to GMT+23:59
    String REGEX_TIMEZONE = "^GMT[+/-]0*([0-9]|1[0-9]|2[0-3]):0*([0-9]|[1-5][0-9])$";

    //------------------------------------------------------------------------------------------------------------------------------------------------
    // Angles
    // Decimal parts of seconds are optional
    // Remember to remove the ^ $ in XSD
    //
    // Ensure this is kept in step with:
    //      <instrument>-metadata.xml
    //
    // -359.9999999999 to +000.0000000000 to +359.9999999999  (Optional sign)
    String REGEX_ANGLE_SIGNED = "^([+\\-]?((0)|([0-9])|(00)|(0[0-9][0-9])|(000)|(0[1-9])|([1-9][0-9])|([12][0-9][0-9])|(3[0-5][0-9]))(\\.([0-9]){1,10})*)?$";

    // Ensure this is kept in step with:
    //      ephemerides.xsd
    //      Observatory-ephemerides.xml
    //      <instrument>-metadata.xml (Sandbox)
    //
    // 000.0000000000 to 359.9999999999
    String REGEX_AZIMUTH_DEG_UNSIGNED = "^(((0)|([0-9])|(00)|(0[0-9][0-9])|(000)|(0[1-9])|([1-9][0-9])|([12][0-9][0-9])|(3[0-5][0-9]))(\\.([0-9]){1,10})*)?$";

    // 000:00:00.0000 to 359:59:59.9999
    // Currently used only as an alias for GalacticLongitude
    String REGEX_AZIMUTH_DMS_UNSIGNED = "^(((0)|([0-9])|(00)|(0[0-9][0-9])|(000)|(0[1-9])|([1-9][0-9])|([12][0-9][0-9])|(3[0-5][0-9])):((0[0-9])|([1-5][0-9])):((0[0-9])|([1-5][0-9]))(\\.([0-9]){1,4})*)?$";

    // Ensure this is kept in step with:
    //      ephemerides.xsd
    //      Observatory-ephemerides.xml
    //      <instrument>-metadata.xml (Sandbox)
    //
    // 00.0000000000 to 89.9999999999
    String REGEX_ELEVATION_DEG_UNSIGNED = "^((([0-9])|([0-8][0-9]))(\\.([0-9]){1,10})*)?$";

    // 00:00:00.0000 to 89:59:59.9999
    String REGEX_ELEVATION_DMS_UNSIGNED = "^((([0-9])|([0-8][0-9]))((:[0-5][0-9]){2})(\\.([0-9]{1,4}))*)?$";


    //------------------------------------------------------------------------------------------------------------------------------------------------
    // Latitude - normally DegMinSec
    // Remember to remove the ^ $ in XSD
    //
    // Ensure this is kept in step with:
    //      frameworks.xsd
    //      pointofinterest.xsd
    //      ephemerides.xsd
    //      Framework-metadata.xml
    //      Observatory-metadata.xml
    //      <instrument>-metadata.xml (Sandbox)

    // -89:59:59.9999 to +00:00:00.0000 to +89:59:59.9999
    String REGEX_LATITUDE_DMS_SIGNED = "^([+\\-]((00)|([0-8][0-9])|([0-9])|([1-8][0-9])):((0[0-9])|([1-5][0-9])):((0[0-9])|([1-5][0-9]))(\\.([0-9]{1,4}))*)?$";

    // Currently used in GpsReceiverHelper
    // 89:59:59.9999S to 00:00:00.0000N to 89:59:59.9999N
    String REGEX_LATITUDE_DMS_NS = "^(((00)|([0-8][0-9])|([0-9])|([1-8][0-9])):((0[0-9])|([1-5][0-9])):((0[0-9])|([1-5][0-9]))(\\.([0-9]{1,4}))*)?[NS]$";

    // Currently used only in WMMMetadataFactory and for Ephemeris Metadata
    // -89.9999999999 to 00.0000000000 to 89.9999999999
    String REGEX_LATITUDE_DEG_SIGNED = "^([+\\-](([0-9])|([0-8][0-9]))(\\.([0-9]){1,10})*)?$";

    //------------------------------------------------------------------------------------------------------------------------------------------------
    // Longitude - normally DegMinSec
    // Remember to remove the ^ $ in XSD
    //
    // Ensure this is kept in step with:
    //      frameworks.xsd
    //      pointofinterest.xsd
    //      ephemerides.xsd
    //      Framework-metadata.xml
    //      Observatory-metadata.xml
    //      <instrument>-metadata.xml  (Sandbox)

    // -179:59:59.9999 to +000:00:00.0000 to +179:59:59.9999
    String REGEX_LONGITUDE_DMS_SIGNED = "^([+\\-]((0)|([0-9])|(00)|(0[0-9][0-9])|(000)|(0[1-9])|([1-9][0-9])|(1[0-7][0-9])):((0[0-9])|([1-5][0-9])):((0[0-9])|([1-5][0-9]))(\\.([0-9]){1,4})*)?$";

    // Currently used in GpsReceiverHelper
    // 179:59:59.9999E to 000:00:00.0000W to 179:59:59.9999W
    // WEST is POSITIVE
    String REGEX_LONGITUDE_DMS_EW = "^(((0)|([0-9])|(00)|(0[0-9][0-9])|(000)|(0[1-9])|([1-9][0-9])|(1[0-7][0-9])):((0[0-9])|([1-5][0-9])):((0[0-9])|([1-5][0-9]))(\\.([0-9]){1,4})*)?[EW]$";

    // Currently used only in WMM Metadata Factory
    // -179.9999999999 to +000.0000000000 to +179.9999999999
    String REGEX_LONGITUDE_DEG_SIGNED = "^([+\\-]((0)|([0-9])|(00)|(0[0-9][0-9])|(000)|(0[1-9])|([1-9][0-9])|(1[0-7][0-9]))(\\.([0-9]){1,10})*)?$";

    // Ensure this is kept in step with:
    //      ephemerides.xsd
    //
    // 000:00:00.0000 to 359:59:59.9999
    String REGEX_LONGITUDE_DMS_UNSIGNED = REGEX_AZIMUTH_DMS_UNSIGNED;

    //------------------------------------------------------------------------------------------------------------------------------------------------
    // RA - normally HourMinSec, Dec - normally DegMinSec
    // Remember to remove the ^ $ in XSD
    //
    // Ensure this is kept in step with:
    //      ephemerides.xsd
    //      Observatory-ephemerides.xml
    //      <instrument>-metadata.xml  (Sandbox)
    //
    // 00:00:00.0000 to 23:59:59.9999
    String REGEX_RIGHT_ASCENSION_HMS = "^((([0-9])|([0-1][0-9])|(2[0-3])):((0[0-9])|(0*[1-5][0-9])):((0[0-9])|(0*[1-5][0-9]))(\\.([0-9]{1,4}))*)?$";

    // -89.9999999999 to 00.0000000000 to 89.9999999999
    String REGEX_DECLINATION_DEG_SIGNED = REGEX_LATITUDE_DEG_SIGNED;

    // Ensure this is kept in step with:
    //      ephemerides.xsd
    //      Observatory-ephemerides.xml
    //      <instrument>-metadata.xml
    //
    // -89:59:59.9999 to +00:00:00.0000 to +89:59:59.9999
    String REGEX_DECLINATION_DMS_SIGNED = REGEX_LATITUDE_DMS_SIGNED;


    //------------------------------------------------------------------------------------------------------------------------------------------------
    // Miscellaneous, slowly being sorted out

    String REGEX_HASL = "^-?(([0-9])([0-9])?([0-9])?([0-9])?(\\.([0-9]))?)$";

    String REGEX_STRING = "^\\p{ASCII}{0,1000}$";

    String REGEX_BOOLEAN = "^(true)|(false)$";

    // ToDo could be much improved....
    String REGEX_METADATA_KEY = "([a-zA-Z0-9.])*";

    String REGEX_BIT_STRING = "(0|1)*";

    String REGEX_XML = "^\\p{Graph}{1,1000}$";

    // From the IzPack Installer
    String REGEX_URL = "^(((http|https|ftp)\\://(([a-zA-Z0-9_\\-\\.]+)\\.([a-zA-Z]{2,5}){1,25})(/[a-zA-Z0-9_\\-\\.=\\u0026\\~\\?]+)*)?)$";

    // 2009-12-29 See: http://regexlib.com/REDetails.aspx?regexp_id=1374
    //String REGEX_URL = "^(([\\w]+:)?//)?(([\\d\\w]|%[a-fA-f\\d]{2,2})+(:([\\d\\w]|%[a-fA-f\\d]{2,2})+)?@)?([\\d\\w][-\\d\\w]{0,253}[\\d\\w]\\.)+[\\w]{2,4}(:[\\d]+)?(/([-+_~.\\d\\w]|%[a-fA-f\\d]{2,2})*)*(\\?(&?([-+_~.\\d\\w]|%[a-fA-f\\d]{2,2})=?)*)?(#([-+_~.\\d\\w]|%[a-fA-f\\d]{2,2})*)?$";

    // Colour specifier e.g. "r=0 g=255 b=100"
    String REGEX_COLOUR = "^r=0*([0-9]{1,2}|1[0-9]{2}|2[0-4][0-9]|25[0-5]) g=0*([0-9]{1,2}|1[0-9]{2}|2[0-4][0-9]|25[0-5]) b=0*([0-9]{1,2}|1[0-9]{2}|2[0-4][0-9]|25[0-5])$";

    // Font specifier e.g. "font = Mono spaced style = Plain size = 12"
    // "font\\s*=abcdefghijkl\\s+style\\s*=\\s*[plain|bold|italic|bolditalic]\\s+size\\s*=\\s*[1-9]|([1-9][0-9])"
    // "^font\\s*=\\s*.+\\s+style\\s*=\\s*(plain|bold|italic|bolditalic)\\s+size\\s*=\\s*[1-9][0-9]?$"

    String REGEX_FONT = "^font\\s*=\\s*.+\\s+style\\s*=\\s*(plain|bold|italic|bolditalic)\\s+size\\s*=\\s*[1-9][0-9]?$";

    //------------------------------------------------------------------------------------------------------------------------------------------------
    // Staribus and Starinet

    // Staribus
    String REGEX_STARIBUS_BLOCK = "(\\p{ASCII})*";
    String REGEX_STARIBUS_MULTICHANNEL_DATA = "(\\p{ASCII})*";
    String REGEX_STARIBUS_ADDRESS = "^0*([0-9]{1,2}|1[0-9]{2}|2[0-4][0-9]|25[0-5])$";

    // Starinet
    // http://www.regexlib.com/Search.aspx?k=ipv4&c=-1&m=-1&ps=20
    String REGEX_IPv4_ADDRESS = "^(25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[0-9]{1,2})(\\.(25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[0-9]{1,2})){3}$";
    String REGEX_IPv6_ADDRESS = "^([0-9A-Fa-f]{1,4}:){7}[0-9A-Fa-f]{1,4}$";
    String REGEX_IPxx_ADDRESS = "^((25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[0-9]{1,2})(\\.(25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[0-9]{1,2})){3})|(([0-9A-Fa-f]{1,4}:){7}[0-9A-Fa-f]{1,4})$";

    //------------------------------------------------------------------------------------------------------------------------------------------------
    // Not sure what to do here...

    String REGEX_FILENAME = REGEX_STRING;
    String REGEX_PATHNAME = REGEX_STRING;


    // Match all OR separators '|'
    String REGEX_INDEXED_LIST = "\\x7c";

    // Match all punctuation except '.' '-' and '+'
    String REGEX_INDEXED_NUMERIC_LIST = "[\\p{Punct}&&[^\\.\\-\\+]]";

    String REGEX_MODULEID = "[0-7]";

    String REGEX_IPADDRESS = "((0|1[0-9]{0,2}|2[0-9]{0,1}|2[0-4][0-9]|25[0-5]|[3-9][0-9]{0,1})\\.){3}(0|1[0-9]{0,2}|2[0-9]{0,1}|2[0-4][0-9]|25[0-5]|[3-9][0-9]{0,1})(\\:(6553[0-5]|655[0-2]\\d|65[0-4]\\d{2}|6[0-4]\\d{3}|[1-5]\\d{4}|[1-9]\\d{0,3}|0))?";

    // Used in XML compressor
    String REGEX_COMPRESS_XML = ">\\s*<";

    String REGEX_NEWLINE = "\\n";
    String REGEX_MARKER_START = ">\\s*\\[";
    String REGEX_MARKER_END = "\\]\\s*<";
    String REGEX_CR_LF_WHITESPACE = "\\r\\n\\s*";
    String REGEX_LF_CR_WHITESPACE = "\\n\\r\\s*";

    String REGEX_XML_COMMENT = "<!\\-\\-.*\\-\\->";
    String REGEX_XML_COMMENT_END = "\\s*\\-\\->";
    String REGEX_XML_COMMENT_EMPTY = "<!\\-\\-\\s*\\-\\->";
    String REGEX_XML_COMMENT_DOTS = "<!\\-\\- \\.*";

    String REGEX_RESOURCE_DELIMITER = "\\.";
    }


    // 000...255      (25[0-5]|2[0-4]\d|[01]\d\d|\d?\d)


//    * 000..255:           ^([01][0-9][0-9]|2[0-4][0-9]|25[0-5])$
//    * 0 or 000..255:      ^([01]?[0-9]?[0-9]|2[0-4][0-9]|25[0-5])$
//    * 0 or 000..127:      ^(0?[0-9]?[0-9]|1[0-1][0-9]|12[0-7])$
//    * 0..999:             ^([0-9]|[1-9][0-9]|[1-9][0-9][0-9])$
//    * 000..999:           ^[0-9]{3}$
//    * 0 or 000..999:      ^[0-9]{1,3}$
//    * 1..999:             ^([1-9]|[1-9][0-9]|[1-9][0-9][0-9])$
//    * 001..999:           ^(00[1-9]|0[1-9][0-9]|[1-9][0-9][0-9])$
//    * 1 or 001..999:      ^(0{0,2}[1-9]|0?[1-9][0-9]|[1-9][0-9][0-9])$
//    * 0 or 00..59:        ^(0?[0-9]?[0-9]|1[0-1][0-9]|12[0-7])$
//    * 0 or 000..366:      ^(0?[0-9]?[0-9]|[1-2][0-9][0-9]|3[0-6][0-9]|36[0-6])$

    // Numbers with exponents, you can use: [-+]?[0-9]*\.?[0-9]+([eE][-+]?[0-9]+)?

    // http://www.regular-expressions.info/dates.html    (19|20)\d\d([- /.])(0[1-9]|1[012])\2(0[1-9]|[12][0-9]|3[01])

