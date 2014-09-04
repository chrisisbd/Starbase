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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.gpsrx.receiver;

import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.gpsrx.GpsInstrumentReceiverInterface;
import org.lmn.fc.model.datatypes.types.HourMinSecDataType;
import org.lmn.fc.model.datatypes.types.YearMonthDayDataType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

//--------------------------------------------------------------------------------------------------
//    $GPGSV
//
//    GPS Satellites in view
//
//    eg. $GPGSV,3,1,11,03,03,111,00,04,15,270,00,06,01,010,00,13,06,292,00*74
//        $GPGSV,3,2,11,14,25,170,00,16,57,208,39,18,67,296,40,19,40,246,00*74
//        $GPGSV,3,3,11,22,42,067,42,24,14,311,43,27,05,244,00,,,,*4D
//
//
//        $GPGSV,1,1,13,02,02,213,,03,-3,000,,11,00,121,,14,13,172,05*62
//
//
//    1    = Total number of messages of this type in this cycle
//    2    = Message number
//    3    = Total number of SVs in view
//    4    = SV PRN number
//    5    = Elevation in degrees, 90 maximum
//    6    = Azimuth, degrees from true north, 000 to 359
//    7    = SNR, 00-99 dB (null when not tracking)
//    8-11 = Information about second SV, same as field 4-7
//    12-15= Information about third SV, same as field 4-7
//    16-19= Information about fourth SV, same as field 4-7
//
//--------------------------------------------------------------------------------------------------
//    $GPRMC
//
//    Recommended minimum specific GPS/TRANSIT data
//
//    eg1. $GPRMC,081836,A,3751.65,S,14507.36,E,000.0,360.0,130998,011.3,E*62
//    eg2. $GPRMC, 225446, A, 4916.45, N, 12311.12, W, 000.5, 054.7, 191194, 020.3, E*68
//
// Garmin  $GPRMC, 122348,     A, 5206.2825, N, 00118.7085, E, 0.0,   61.1,  080611, 2.1, W, D*37
// USB     $GPRMC, 082005.622, A, 5206.2820, N, 00118.7042, E, 0.09, 147.97, 090611,    , *00]
//
//               225446       Time of fix 22:54:46 UTC
//               A            Navigation receiver warning A = Valid position, V = Warning
//               4916.45,N    Latitude 49 deg. 16.45 min. North
//               12311.12,W   Longitude 123 deg. 11.12 min. West
//               000.5        Speed over ground, Knots
//               054.7        Course Made Good, degrees true
//               191194       UTC Date of fix, 19 November 1994
//               020.3,E      Magnetic variation, 20.3 deg. East
//               *68          mandatory checksum
//
//
//    eg3. $GPRMC,220516,A,5133.82,N,00042.24,W,173.8,231.8,130694,004.2,W*70
//                  1    2    3    4    5     6    7    8      9     10  11 12
//
//
//          1   220516     Time Stamp
//          2   A          validity - A-ok, V-invalid
//          3   5133.82    current Latitude
//          4   N          North/South
//          5   00042.24   current Longitude
//          6   W          East/West
//          7   173.8      Speed in knots
//          8   231.8      True course
//          9   130694     Date Stamp
//          10  004.2      Variation
//          11  W          East/West
//          12  *70        checksum
//
//
//    eg4. for NMEA 0183 version 3.00 active the Mode indicator field is added
//         $GPRMC,hhmmss.ss,A,llll.ll,a,yyyyy.yy,a,x.x,x.x,ddmmyy,x.x,a,m*hh
//    Field #
//    1    = UTC time of fix
//    2    = Data status (A=Valid position, V=navigation receiver warning)
//    3    = Latitude of fix
//    4    = N or S of longitude
//    5    = Longitude of fix
//    6    = E or W of longitude
//    7    = Speed over ground in knots
//    8    = Track made good in degrees True
//    9    = UTC date of fix
//    10   = Magnetic variation degrees (Easterly var. subtracts from true course)
//    11   = E or W of magnetic variation
//    12   = Mode indicator, (A=Autonomous, D=Differential, E=Estimated, N=Data not valid)
//    13   = Checksum
//--------------------------------------------------------------------------------------------------
//    $GPGGA
//
//    Global Positioning System Fix Data
//
//    eg1. $GPGGA,170834,4124.8963,N,08151.6838,W,1,05,1.5,280.2,M,-34.0,M,,,*75
//
//    Name 	Example Data 	Description
//    Sentence Identifier 	$GPGGA 	Global Positioning System Fix Data
//    Time 	170834 	17:08:34 UTC
//    Latitude 	4124.8963, N 	41d 24.8963' N or 41d 24' 54" N
//    Longitude 	08151.6838, W 	81d 51.6838' W or 81d 51' 41" W
//    Fix Quality:
//    - 0 = Invalid
//    - 1 = GPS fix
//    - 2 = DGPS fix 	1 	Data is from a GPS fix
//    Number of Satellites 	05 	5 Satellites are in view
//    Horizontal Dilution of Precision (HDOP) 	1.5 	Relative accuracy of horizontal position
//    Altitude 	280.2, M 	280.2 meters above mean sea level
//    Height of geoid above WGS84 ellipsoid 	-34.0, M 	-34.0 meters
//    Time since last DGPS update 	blank 	No last update
//    DGPS reference station id 	blank 	No station id
//    Checksum 	*75 	Used by program to check for transmission errors
//
//    Courtesy of Brian McClure, N8PQI.
//
//    Global Positioning System Fix Data. Time, position and fix related data for a GPS receiver.
//
//    eg2. $GPGGA, hhmmss.ss,  ddmm.mmm,  a, dddmm.mmm,  b, q, xx, p.p, a.b,  M,  c.d, M, x.x, nnnn
//
// USB     $GPGGA, 082005.622, 5206.2820, N, 00118.7042, E, 1, 05, 1.5, 86.2, M, 47.0, M, 0.0, 0000*45]
//
//    hhmmss.ss = UTC of position
//    ddmm.mmm = latitude of position
//    a = N or S, latitutde hemisphere
//    dddmm.mmm = longitude of position
//    b = E or W, longitude hemisphere
//    q = GPS Quality indicator (0=No fix, 1=Non-differential GPS fix, 2=Differential GPS fix, 6=Estimated fix)
//    xx = number of satellites in use
//    p.p = horizontal dilution of precision
//    a.b = Antenna altitude above mean-sea-level
//    M = units of antenna altitude, meters
//    c.d = Geoidal height
//    M = units of geoidal height, meters
//    x.x = Age of Differential GPS data (seconds since last valid RTCM transmission)
//    nnnn = Differential reference station ID, 0000 to 1023
//--------------------------------------------------------------------------------------------------

//    $GPGSA
//
//    GPS DOP and active satellites
//
//    eg1. $GPGSA,A,3,,,,,,16,18,,22,24,,,3.6,2.1,2.2*3C
//    eg2. $GPGSA,A,3,19,28,14,18,27,22,31,39,,,,,1.7,1.0,1.3*34
//
//
//    1    = Mode:
//           M=Manual, forced to operate in 2D or 3D
//           A=Automatic, 3D/2D
//    2    = Mode:
//           1=Fix not available
//           2=2D
//           3=3D
//    3-14 = PRN's of Satellite Vechicles (SV's) used in position fix (null for unused fields)
//    15   = Position Dilution of Precision (PDOP)
//    16   = Horizontal Dilution of Precision (HDOP)
//    17   = Vertical Dilution of Precision (VDOP)
//
/***************************************************************************************************
 * GpsReceiverNMEAParsers.
 */

public final class GpsReceiverNMEAParsers implements FrameworkConstants,
                                                     FrameworkStrings,
                                                     FrameworkSingletons
    {
    private static final String NMEA_DELIMITER = ",";
    private static final String CHECKSUM_DELIMITER = "*";
    private static final String REGEX_CHECKSUM_DELIMITER = "\\*";
    private static final int RADIX_16 = 16;
    private static final int MAX_MESSAGES = 50;
    private static final int GPRMC_TOKENS = 12;
    private static final int GPGGA_TOKENS = 14;


    /***********************************************************************************************
     * Get the list of NMEA sentences supported by these parsers.
     *
     * @return List<String>
     */

    public static List<String> getNMEASentences()
        {
        final List<String> listNMEASupport;

        listNMEASupport = new ArrayList<String>(4);

        listNMEASupport.add(GpsInstrumentReceiverInterface.SENTENCE_GPRMC);
        listNMEASupport.add(GpsInstrumentReceiverInterface.SENTENCE_GPGGA);
        listNMEASupport.add(GpsInstrumentReceiverInterface.SENTENCE_GPGSA);
        listNMEASupport.add(GpsInstrumentReceiverInterface.SENTENCE_GPGSV);

        return (listNMEASupport);
        }


    /***********************************************************************************************
     * Parse the set of $GPGSV sentence(s), if they are valid.
     *
     * @param sentence
     * @param msgcount
     * @param valid
     *
     * @param debug
     * @return GPGSVData
     */

    public static GPGSVData parseGPGSV(final List<String> sentence,
                                       final int msgcount,
                                       final boolean valid,
                                       final boolean debug)
        {
        GPGSVData dataGPGSV;
        boolean boolSuccess;

        // Set some defaults
        dataGPGSV = new GPGSVData();
        boolSuccess = true;

        LOGGER.debugGpsEvent(debug,
                             GpsInstrumentReceiverInterface.LINE);

        if ((msgcount < 1) || (msgcount > MAX_MESSAGES))
            {
            // Input parameter invalid
            LOGGER.debugGpsEvent(debug,
                                 "$GPGSV Invalid message count [count=" + msgcount + "]");
            return(dataGPGSV);
            }

        if (!valid)
            {
            LOGGER.debugGpsEvent(debug,
                                 "$GPGSV not valid, returning defaults");
            return (dataGPGSV);
            }

        try
            {
            // Parse msgcount sentences
            for (int msgcounter = 0;
                 (boolSuccess) && (msgcounter < msgcount);
                 msgcounter++)
                {
                final String[] arrayTokens;
                final int intTokenCount;
                int intTokenPointer;
                final String strSentence;
                final int intMessageCount;
                final int intMessageNumber;

                // Retrieve the ith parsed $GPGSV sentence and tokenise
                strSentence = sentence.get(msgcounter);
                arrayTokens = strSentence.split(NMEA_DELIMITER);

    //            for (int i = 0;
    //                 i < arrayTokens.length;
    //                 i++)
    //                {
    //                final String token = arrayTokens[i];
    //
    //                LOGGER.debugGpsEvent(i + " [token=" + token + "]");
    //                }

                intTokenCount = arrayTokens.length;
                intTokenPointer = 0;

                LOGGER.debugGpsEvent(debug,
                                     "Parsing $GPGSV(" + (msgcounter+1) + ") [tokens=" + intTokenCount + "]");
                LOGGER.debugGpsEvent(debug,
                                     "[sentence=" + strSentence + "]");

                // Are there enough tokens to get the basics?
                if (arrayTokens.length >= 3)
                    {
                    final int intInView;

                    // Step over the sentence identifier $GPGSV
                    intTokenPointer++;

                    // Read Field 1, the total number of messages in this cycle
                    intMessageCount = Integer.parseInt(arrayTokens[intTokenPointer++]);

                    // Double check for correct parsing earlier
                    if (intMessageCount == msgcount)
                        {
                        LOGGER.debugGpsEvent(debug,
                                             "[messagecount=" + intMessageCount + "]");

                        // Read Field 2, the MessageNumber
                        intMessageNumber = Integer.parseInt(arrayTokens[intTokenPointer++]);

                        // Check that the sentences have remained in sequence
//                        if (intMessageNumber == (msgcounter+1))
//                            {
                            LOGGER.debugGpsEvent(debug,
                                                 "[messagenumber=" + intMessageNumber + "]");

                            // Read Field 3, the SatellitesInView count
                            intInView = Integer.parseInt(arrayTokens[intTokenPointer++]);

                            LOGGER.debugGpsEvent(debug,
                                                 "[satellitesinview=" + intInView + "]");

                            // There's now a *maximum* of four sets of satellite data,
                            // each with four fields
                            // The checksum field also contains the last SNR (duh!)

                            for (int countSV = 0;
                                 ((boolSuccess)
                                     && (countSV < 4)
                                     && (intTokenPointer < arrayTokens.length));
                                 countSV++)
                                {
                                final SatelliteData GPGSVsatellite;
                                final String strChecksum;
                                final int intChecksum;
                                String strTemp;

                                LOGGER.debugGpsEvent(debug,
                                                     "[countSV=" + countSV + "]");

                                // Prepare to store the data for this satellite
                                GPGSVsatellite = new SatelliteData();

                                // Read the Satellite PRN number, or EMPTY_STRING if none present
                                strTemp = arrayTokens[intTokenPointer++];

                                if ((strTemp != null)
                                    && (!EMPTY_STRING.equals(strTemp))
                                    && (intTokenPointer < arrayTokens.length))
                                    {
                                    // Save the PRN in this satellite's data structure
                                    GPGSVsatellite.setSatellitePRN(Integer.parseInt(strTemp));
                                    LOGGER.debugGpsEvent(debug,
                                                         "[satellitePRN=" + strTemp + "]");

                                    // Now read the Elevation
                                    strTemp = arrayTokens[intTokenPointer++];

                                    if ((strTemp != null)
                                        && (!EMPTY_STRING.equals(strTemp))
                                        && (intTokenPointer < arrayTokens.length))
                                        {
                                        GPGSVsatellite.setElevation(Integer.parseInt(strTemp));
                                        LOGGER.debugGpsEvent(debug,
                                                             "[elevation=" + strTemp + "]");

                                        // Now read the Azimuth
                                        strTemp = arrayTokens[intTokenPointer++];

                                        if ((strTemp != null)
                                            && (!EMPTY_STRING.equals(strTemp))
                                            && (intTokenPointer < arrayTokens.length))
                                            {
                                            GPGSVsatellite.setAzimuth(Integer.parseInt(strTemp));
                                            LOGGER.debugGpsEvent(debug,
                                                                 "[azimuth=" + strTemp + "]");

                                            // Now read the SNR, checking for the last satellite field
                                            // which has a different syntax...
                                            strTemp = arrayTokens[intTokenPointer++];

                                            if (strTemp != null)
                                                {
                                                // See if it is the last satellite in the group,
                                                // so we need to chop up the SNR and checksum
                                                if (strTemp.contains(CHECKSUM_DELIMITER))
                                                    {
                                                    // The SNR may be missing?!
                                                    // If the CHECKSUM_DELIMITER is first, there's no SNR
                                                    if (CHECKSUM_DELIMITER.equals(strTemp.substring(0, 1)))
                                                        {
                                                        GPGSVsatellite.setSNRdB(-1);
                                                        LOGGER.debugGpsEvent(debug,
                                                                             "[snr=null]");

                                                        // There is no SNR information, so just read the checksum
                                                        // from CHECKSUM_DELIMITER onwards
                                                        strChecksum = strTemp.substring(1);
                                                        }
                                                    else
                                                        {
                                                        final String[] arrayEnd;

                                                        // We have an SNR and checksum combined,
                                                        // so retokenize using the checksum delimiter
                                                        // <SNR><CHECKSUM_DELIMITER><CHECKSUM>
                                                        arrayEnd = strTemp.split(REGEX_CHECKSUM_DELIMITER);

                                                        if (arrayEnd.length == 2)
                                                            {
                                                            // Read the SNR
                                                            if ((arrayEnd[0] != null)
                                                                && (!EMPTY_STRING.equals(arrayEnd[0])))
                                                                {
                                                                GPGSVsatellite.setSNRdB(Integer.parseInt(arrayEnd[0]));
                                                                LOGGER.debugGpsEvent(debug,
                                                                                     "[snr=" + arrayEnd[0] + "]");
                                                                }
                                                            else
                                                                {
                                                                // Something went wrong...
                                                                GPGSVsatellite.setSNRdB(-1);
                                                                LOGGER.debugGpsEvent(debug,
                                                                                     "[snr=null]");
                                                                }

                                                            // Read the checksum as a String
                                                            strChecksum = arrayEnd[1];
                                                            }
                                                        else
                                                            {
                                                            // Something went wrong...
                                                            GPGSVsatellite.setSNRdB(-1);
                                                            LOGGER.debugGpsEvent(debug,
                                                                                 "[snr=null]");

                                                            strChecksum = "00";
                                                            }
                                                        }

                                                    if ((strChecksum != null)
                                                        && (!EMPTY_STRING.equals(strChecksum)))
                                                        {
                                                        intChecksum = Integer.parseInt(strChecksum, RADIX_16);
                                                        }
                                                    else
                                                        {
                                                        intChecksum = 0;
                                                        }

                                                    LOGGER.debugGpsEvent(debug,
                                                                         "[strchecksum=" + strChecksum + "]");
                                                    LOGGER.debugGpsEvent(debug,
                                                                         "[checksum=" + Integer.toHexString(intChecksum) + "]");

                                                    // ToDo Check the checksum...
                                                    if (true)
                                                        {
                                                        // Commit the data received
                                                        LOGGER.debugGpsEvent(debug,
                                                                             "Commit data to SatellitesInView");
                                                        dataGPGSV.getSatellitesInView().add(GPGSVsatellite);
                                                        }
                                                    else
                                                        {
                                                        // Checksum delimiter invalid (very unlikely)
                                                        LOGGER.debugGpsEvent(debug,
                                                                             "Invalid checksum");
                                                        boolSuccess = false;
                                                        }
                                                    }
                                                else
                                                    {
                                                    // It is not the last satellite,
                                                    // so this should be just the SNR field
                                                    if (!EMPTY_STRING.equals(strTemp))
                                                        {
                                                        GPGSVsatellite.setSNRdB(Integer.parseInt(strTemp));
                                                        LOGGER.debugGpsEvent(debug,
                                                                             "[snr=" + strTemp + "]");
                                                        }
                                                    else
                                                        {
                                                        // SNR is null, so not tracking this satellite
                                                        // Set SNR = -1?

                                                        GPGSVsatellite.setSNRdB(-1);
                                                        LOGGER.debugGpsEvent(debug,
                                                                             "[snr=null]");
                                                        }

                                                    // Commit the data received
                                                    LOGGER.debugGpsEvent(debug,
                                                                         "Commit data to SatellitesInView");
                                                    dataGPGSV.getSatellitesInView().add(GPGSVsatellite);
                                                    }
                                                }
                                            else
                                                {
                                                // Leave the inner loop early...
                                                LOGGER.debugGpsEvent(debug,
                                                                     "Null token for SNR field");
                                                boolSuccess = false;
                                                }
                                            }
                                        else
                                            {
                                            // Something's wrong - delimiter where Azimuth should be!
                                            // Leave the inner loop early...
                                            LOGGER.debugGpsEvent(debug,
                                                                 "[azimutherror=" + strTemp + "]");
                                            boolSuccess = false;
                                            }
                                        }
                                    else
                                        {
                                        // Something's wrong - delimiter where Elevation should be!
                                        // Leave the inner loop early...
                                        LOGGER.debugGpsEvent(debug,
                                                             "[elevationerror=" + strTemp + "]");
                                        boolSuccess = false;
                                        }
                                    }
                                else
                                    {
                                    //    4    = SV PRN number
                                    //    5    = Elevation in degrees, 90 maximum
                                    //    6    = Azimuth, degrees from true north, 000 to 359
                                    //    7    = SNR, 00-99 dB (null when not tracking)

                                    // The PRN field was null, so skip all fields for this satellite
                                    LOGGER.debugGpsEvent(debug,
                                                         "Skipping null PRN");

                                    // Elevation
                                    intTokenPointer++;
                                    LOGGER.debugGpsEvent(debug,
                                                         "Skipping null Elevation");

                                    // Azimuth
                                    intTokenPointer++;
                                    LOGGER.debugGpsEvent(debug,
                                                         "Skipping null Azimuth");

                                    // SNR, checking for the last satellite (includes checksum)
                                    strTemp = arrayTokens[intTokenPointer++];

                                    // Now read the SNR, checking for the last satellite field
                                    // which has a different syntax...
                                    if (strTemp != null)
                                        {
                                        // See if it is the last satellite in the group,
                                        // so we need to chop up the SNR and checksum
                                        if (strTemp.contains(CHECKSUM_DELIMITER))
                                            {
                                            // The SNR may not be missing?!
                                            // If the CHECKSUM_DELIMITER is first, there's no SNR
                                            if (CHECKSUM_DELIMITER.equals(strTemp.substring(0, 1)))
                                                {
                                                LOGGER.debugGpsEvent(debug,
                                                                     "[snr=null]");

                                                // There is no SNR information, so just read the checksum
                                                strChecksum = strTemp.substring(1);
                                                }
                                            else
                                                {
                                                final String[] arrayEnd;

                                                // We have an SNR and checksum combined,
                                                // so retokenize using the checksum delimiter
                                                // <SNR><CHECKSUM_DELIMITER><CHECKSUM>
                                                arrayEnd = strTemp.split(REGEX_CHECKSUM_DELIMITER);

                                                if (arrayEnd.length == 2)
                                                    {
                                                    // Read the SNR, but discard because it is not valid data?
                                                    LOGGER.debugGpsEvent(debug,
                                                                         "[snr=" + arrayEnd[0] + "]");

                                                    // Read the checksum as a String
                                                    strChecksum = arrayEnd[1];
                                                    }
                                                else
                                                    {
                                                    // Something went wrong...
                                                    LOGGER.debugGpsEvent(debug,
                                                                         "[snr=null]");

                                                    strChecksum = "00";
                                                    }
                                                }

                                            intChecksum = Integer.parseInt(strChecksum, RADIX_16);

                                            LOGGER.debugGpsEvent(debug,
                                                                 "[strchecksum=" + strChecksum + "]");
                                            LOGGER.debugGpsEvent(debug,
                                                                 "[checksum=" + Integer.toHexString(intChecksum) + "]");

                                            // ToDO Check the checksum...
                                            if (false)
                                                {
                                                // Checksum delimiter invalid (very unlikely)
                                                LOGGER.debugGpsEvent(debug,
                                                                     "Invalid checksum");
                                                boolSuccess = false;
                                                }

                                            // Otherwise just leave the loop, there are no data to save
                                            }
                                        else
                                            {
                                            // It is not the last satellite
                                            // Assume the SNR is null
                                            LOGGER.debugGpsEvent(debug,
                                                                 "Skipping null SNR");
                                            }
                                        }
                                    }
                                }
//                            }
//                        else
//                            {
//                            // Leave the loop early...
//                            LOGGER.debugGpsEvent("[messagenumber=" + intMessageNumber + "] [expected=" + (msgcounter+1) + "]",
//                                                 debug);
//                            boolSuccess = false;
//                            }
                        }
                    else
                        {
                        // Leave the loop early...
                        LOGGER.debugGpsEvent(debug,
                                             "[messagecount=" + intMessageCount + "] [expected=" + msgcount + "]");
                        boolSuccess = false;
                        }
                    }
                else
                    {
                    LOGGER.debugGpsEvent(debug,
                                         "Insufficient tokens [tokencount=" + intTokenCount + "]");
                    boolSuccess = false;
                    }
                }
            }

        catch (IndexOutOfBoundsException exception)
            {
            // Just in case things go horribly wrong!
            LOGGER.debugGpsEvent(debug,
                                 "IndexOutOfBounds");
            boolSuccess = false;
            }

        catch (NumberFormatException exception)
            {
            LOGGER.debugGpsEvent(debug,
                                 "Invalid NumberFormat");
            boolSuccess = false;
            }

        // Everything worked Ok!
        if (boolSuccess)
            {
            final Iterator<SatelliteData> iterSatelliteData;

            iterSatelliteData = dataGPGSV.getSatellitesInView().iterator();

            while (iterSatelliteData.hasNext())
                {
                final SatelliteData data;

                data = iterSatelliteData.next();

                LOGGER.debugGpsEvent(debug,
                                     "[satellitedata="
                                         + data.getSatellitePRN() + ", "
                                         + data.getElevation() + ", "
                                         + data.getAzimuth() + ", "
                                         + data.getSNRdB() + "] ");
                }
            }
        else
            {
            dataGPGSV = new GPGSVData();
            LOGGER.debugGpsEvent(debug,
                                 "GPGSV Returning default data");
            }

        return (dataGPGSV);
        }


    /***********************************************************************************************
     * Decode the $GPRMC sentence.
     * Obtains Latitude and Longitude.
     * This is horrible, there must be a very much better way.
     *
     * @param sentence
     * @param valid
     * @param debug
     *
     * @return boolean
     */

    public static GPRMCData parseGPRMC(final String sentence,
                                       final boolean valid,
                                       final boolean debug)
        {
        GPRMCData dataGPRMC;
        boolean boolSuccess;

        dataGPRMC = new GPRMCData();

        if (!valid)
            {
            LOGGER.debugGpsEvent(debug, "$GPRMC not valid, returning defaults");
            return (dataGPRMC);
            }

        try
            {
            final String[] arrayTokens;
            final int intTokenCount;
            int intTokenPointer;
            final String strTimeStamp;
            final String strValidity;
            final String strLatitude;
            final String strLatHemisphere;
            final String strLongitude;
            final String strLongHemisphere;
            final String strSpeed;
            final String strCourse;
            final String strDateStamp;
            final String strVariation;
            final String strVariationHemisphere;
            final String strModeIndicator;
            final String strChecksum;
            final int intChecksum;
            String strTemp;

            arrayTokens = sentence.split(NMEA_DELIMITER);

            LOGGER.debugGpsEvent(debug, GpsInstrumentReceiverInterface.LINE);
            LOGGER.debugGpsEvent(debug, "GpsReceiverNMEAParsers.parseGPRMC()");

            for (int i = 0;
                 i < arrayTokens.length;
                 i++)
                {
                final String token = arrayTokens[i];

                LOGGER.debugGpsEvent(debug, i + " [token=" + token + "]");
                }

            intTokenCount = arrayTokens.length;
            intTokenPointer = 0;

            // There must be at least 12 tokens (NMEA v2), but no more than 13 (NMEA v3)
            if ((intTokenCount == GPRMC_TOKENS)
                || (intTokenCount == GPRMC_TOKENS + 1))
                {
                LOGGER.debugGpsEvent(debug,
                                     GpsInstrumentReceiverInterface.LINE);
                LOGGER.debugGpsEvent(debug,
                                     "Parsing $GPRMC [tokens=" + intTokenCount + "]");

                // Discard the sentence identifier
                intTokenPointer++;

                // Read the TimeStamp
                strTimeStamp = arrayTokens[intTokenPointer++];

                if ((strTimeStamp != null)
                    && (!EMPTY_STRING.equals(strTimeStamp)))
                    {
                    // Note that seconds can have a fractional part
                    dataGPRMC.setTimeStamp(new HourMinSecDataType(true,
                                                                  Integer.parseInt(strTimeStamp.substring(0, 2)),
                                                                  Integer.parseInt(strTimeStamp.substring(2, 4)),
                                                                  Double.parseDouble(strTimeStamp.substring(4))));
                    LOGGER.debugGpsEvent(debug,
                                         "[timestamp=" + dataGPRMC.getTimeStamp().toString() + "]");
                    }

                // Read the validity indicator
                strValidity = arrayTokens[intTokenPointer++];

                LOGGER.debugGpsEvent(debug,
                                     "[validity=" + strValidity + "]");

                if (GpsInstrumentReceiverInterface.VALIDITY_A.equals(strValidity))
                    {
                    // $GPRMC,220516,A,5133.82,N,00042.24,W,173.8,231.8,130694,004.2,W*70

                    // Read the latitude and decode the degrees and minutes
                    strLatitude = arrayTokens[intTokenPointer++];

                    if ((strLatitude != null)
                        && (!EMPTY_STRING.equals(strLatitude)))
                        {
                        // Convert the minutes to degrees
                        // 5133.82
                        dataGPRMC.setLatitude(Double.parseDouble(strLatitude.substring(2)) / GpsInstrumentReceiverInterface.MINUTES_PER_DEGREE);

                        // Add in the degrees (two digits)
                        dataGPRMC.setLatitude(dataGPRMC.getLatitude() + Double.parseDouble(strLatitude.substring(0, 2)));

                        // and the latitude hemisphere
                        strLatHemisphere = arrayTokens[intTokenPointer++];

                        if ((strLatHemisphere != null)
                            && (!EMPTY_STRING.equals(strLatHemisphere)))
                            {
                            if ((GpsInstrumentReceiverInterface.HEMISPHERE_SOUTH.equals(strLatHemisphere))
                                || (GpsInstrumentReceiverInterface.HEMISPHERE_NORTH.equals(strLatHemisphere)))
                                {
                                if (GpsInstrumentReceiverInterface.HEMISPHERE_SOUTH.equals(strLatHemisphere))
                                    {
                                    // Latitude is POSITIVE to the NORTH.
                                    dataGPRMC.setLatitude(-dataGPRMC.getLatitude());
                                    }

                                LOGGER.debugGpsEvent(debug,
                                                     "[latitude=" + dataGPRMC.getLatitude() + "]");

                                // $GPRMC,220516,A,5133.82,N,00042.24,W,173.8,231.8,130694,004.2,W*70

                                // Read the longitude and decode the degrees and minutes
                                strLongitude = arrayTokens[intTokenPointer++];

                                if ((strLongitude != null)
                                    && (!EMPTY_STRING.equals(strLongitude)))
                                    {
                                    // $GPRMC,220516,A,5133.82,N,00042.24,W,173.8,231.8,130694,004.2,W*70

                                    // Convert the minutes to degrees
                                    // 00042.24
                                    dataGPRMC.setLongitude(Double.parseDouble(strLongitude.substring(3)) / GpsInstrumentReceiverInterface.MINUTES_PER_DEGREE);
                                    // Add in the degrees (three digits)
                                    dataGPRMC.setLongitude(dataGPRMC.getLongitude() + Double.parseDouble(strLongitude.substring(0, 3)));

                                    // and the longitude hemisphere
                                    strLongHemisphere = arrayTokens[intTokenPointer++];

                                    if ((strLongHemisphere != null)
                                        && (!EMPTY_STRING.equals(strLongHemisphere)))
                                        {
                                        if ((GpsInstrumentReceiverInterface.HEMISPHERE_EAST.equals(strLongHemisphere))
                                            || (GpsInstrumentReceiverInterface.HEMISPHERE_WEST.equals(strLongHemisphere)))
                                            {
                                            // Astronomical convention is that Longitudes are POSITIVE in the WEST
                                            // but the rest of the world does it the other way round....
                                            if (GpsInstrumentReceiverInterface.HEMISPHERE_EAST.equals(strLongHemisphere))
                                                {
                                                dataGPRMC.setLongitude(-dataGPRMC.getLongitude());
                                                }

                                            LOGGER.debugGpsEvent(debug,
                                                                 "[longitude=" + dataGPRMC.getLongitude() + "]");

                                            // Read the Speed
                                            strSpeed = arrayTokens[intTokenPointer++];

                                            if ((strSpeed != null)
                                                && (!EMPTY_STRING.equals(strSpeed)))
                                                {
                                                dataGPRMC.setSpeed(Double.parseDouble(strSpeed));
                                                LOGGER.debugGpsEvent(debug,
                                                                     "[speed=" + dataGPRMC.getSpeed() + "]");
                                                }

                                            // Read the course
                                            strCourse = arrayTokens[intTokenPointer++];

                                            if ((strCourse != null)
                                                && (!EMPTY_STRING.equals(strCourse)))
                                                {
                                                dataGPRMC.setCourse(Double.parseDouble(strCourse));
                                                LOGGER.debugGpsEvent(debug,
                                                                     "[course=" + dataGPRMC.getCourse() + "]");
                                                }

                                            // Read the DateStamp (note the adjustment for the year 2000)
                                            strDateStamp = arrayTokens[intTokenPointer++];

                                            if ((strDateStamp != null)
                                                && (!EMPTY_STRING.equals(strDateStamp)))
                                                {
                                                dataGPRMC.setDateStamp(new YearMonthDayDataType(true,
                                                                                        GpsInstrumentReceiverInterface.YEAR_OFFSET + Integer.parseInt(strDateStamp.substring(4)),
                                                                                        Integer.parseInt(strDateStamp.substring(2, 4)),
                                                                                        Integer.parseInt(strDateStamp.substring(0, 2))));
                                                LOGGER.debugGpsEvent(debug,
                                                                     "[datestamp=" + dataGPRMC.getDateStamp().toString() + "]");
                                                }

                                            // Read the magnetic variation, which may be missing
                                            strVariation = arrayTokens[intTokenPointer++];

                                            if ((strVariation != null)
                                                && (!EMPTY_STRING.equals(strVariation)))
                                                {
                                                dataGPRMC.setVariation(Double.parseDouble(strVariation));
                                                }
                                            else
                                                {
                                                dataGPRMC.setVariation(0.0);
                                                }

                                            LOGGER.debugGpsEvent(debug,
                                                                 "[unsignedvariation=" + dataGPRMC.getVariation() + "]");

                                            //----------------------------------------------------------------------------------
                                            // How many more tokens are there?

                                            if (intTokenPointer == (intTokenCount-1))
                                                {
                                                // We've reached the end, it must be NMEA v2
                                                // The last token contains the Variation Hemisphere AND the checksum
                                                strTemp = arrayTokens[intTokenPointer];

                                                // Minimum length is *NN i.e. variation hemisphere missing
                                                if ((strTemp != null)
                                                    && (!EMPTY_STRING.equals(strTemp))
                                                    && (strTemp.length() >= 2))
                                                    {
                                                    if (!CHECKSUM_DELIMITER.equals(strTemp.substring(0, 1)))
                                                        {
                                                        // There should be a Variation Hemisphere token
                                                        strVariationHemisphere = strTemp.substring(0, 1);

                                                        // Check the variation hemisphere
                                                        if ((GpsInstrumentReceiverInterface.HEMISPHERE_EAST.equals(strVariationHemisphere))
                                                            || (GpsInstrumentReceiverInterface.HEMISPHERE_WEST.equals(strVariationHemisphere)))
                                                            {
                                                            if (GpsInstrumentReceiverInterface.HEMISPHERE_EAST.equals(strVariationHemisphere))
                                                                {
                                                                dataGPRMC.setVariation(-dataGPRMC.getVariation());
                                                                }

                                                            LOGGER.debugGpsEvent(debug,
                                                                                 "[variation=" + dataGPRMC.getVariation() + "]");

                                                            // Read the checksum, including the prefix '*'
                                                            strChecksum = strTemp.substring(1);

                                                            // Read the HEX checksum
                                                            intChecksum = Integer.parseInt(strChecksum.substring(1), RADIX_16);
                                                            LOGGER.debugGpsEvent(debug,
                                                                                 "[checksum=" + Integer.toHexString(intChecksum) + "]");

                                                            // Check the checksum...
                                                            // This could be expanded
                                                            if (CHECKSUM_DELIMITER.equals(strChecksum.substring(0, 1)))
                                                                {
                                                                // Everything worked Ok!
                                                                LOGGER.debugGpsEvent(debug,
                                                                                     "$GPRMC decoded successfully (NMEA v2)");
                                                                boolSuccess = true;
                                                                }
                                                            else
                                                                {
                                                                // Checksum delimiter invalid (very unlikely)
                                                                LOGGER.debugGpsEvent(debug,
                                                                                     "Invalid checksum");
                                                                boolSuccess = false;
                                                                }
                                                            }
                                                        else
                                                            {
                                                            // Variation hemisphere was expected to be "E" or "W"
                                                            LOGGER.debugGpsEvent(debug,
                                                                                 "Invalid Variation hemisphere");
                                                            boolSuccess = false;
                                                            }
                                                        }
                                                    else
                                                        {
                                                        // Only the checksum expected, i.e. variation hemisphere missing
                                                        // Read the checksum, including the prefix '*'
                                                        strChecksum = strTemp;

                                                        // Read the HEX checksum
                                                        intChecksum = Integer.parseInt(strChecksum.substring(1), RADIX_16);
                                                        LOGGER.debugGpsEvent(debug,
                                                                             "[checksum=" + Integer.toHexString(intChecksum) + "]");

                                                        // Check the checksum...
                                                        // This could be expanded
                                                        if (CHECKSUM_DELIMITER.equals(strChecksum.substring(0, 1)))
                                                            {
                                                            // Everything worked Ok!
                                                            LOGGER.debugGpsEvent(debug,
                                                                                 "$GPRMC decoded successfully (NMEA v2)");
                                                            boolSuccess = true;
                                                            }
                                                        else
                                                            {
                                                            // Checksum delimiter invalid (very unlikely)
                                                            LOGGER.debugGpsEvent(debug,
                                                                                 "Invalid checksum");
                                                            boolSuccess = false;
                                                            }
                                                        }
                                                    }
                                                else
                                                    {
                                                    LOGGER.debugGpsEvent(debug,
                                                                         "Invalid final token");
                                                    boolSuccess = false;
                                                    }
                                                }

                                            //--------------------------------------------------------------------------

                                            else if (intTokenPointer == (intTokenCount-2))
                                                {
                                                // There are two fields, the Variation Hemisphere (which may be missing)
                                                // then the Mode Indicator AND the checksum (duh!)
                                                // This seems to be anomalous NMEA syntax. Oh well...
                                                // This must be NMEA v3

                                                // The next token contains the Variation Hemisphere
                                                strTemp = arrayTokens[intTokenPointer++];

                                                if ((strTemp != null)
                                                    && (!EMPTY_STRING.equals(strTemp))
                                                    && (strTemp.length() == 1))
                                                    {
                                                    strVariationHemisphere = strTemp;

                                                    // Check the variation hemisphere
                                                    if ((GpsInstrumentReceiverInterface.HEMISPHERE_EAST.equals(strVariationHemisphere))
                                                        || (GpsInstrumentReceiverInterface.HEMISPHERE_WEST.equals(strVariationHemisphere)))
                                                        {
                                                        if (GpsInstrumentReceiverInterface.HEMISPHERE_EAST.equals(strVariationHemisphere))
                                                            {
                                                            dataGPRMC.setVariation(-dataGPRMC.getVariation());
                                                            }
                                                        }
                                                    else
                                                        {
                                                        // Variation hemisphere was expected to be "E" or "W"
                                                        LOGGER.debugGpsEvent(debug,
                                                                             "Invalid Variation hemisphere");
                                                        // Allow this to pass for simplicity (!)
                                                        dataGPRMC.setVariation(0.0);
                                                        }
                                                    }
                                                else
                                                    {
                                                    // Assume a Variation of zero if the field is empty
                                                    dataGPRMC.setVariation(0.0);
                                                    }

                                                LOGGER.debugGpsEvent(debug,
                                                                     "[variation=" + dataGPRMC.getVariation() + "]");

                                                // Now read the Mode Indicator AND the checksum
                                                strTemp = arrayTokens[intTokenPointer];

                                                if ((strTemp != null)
                                                    && (!EMPTY_STRING.equals(strTemp))
                                                    && (strTemp.length() >= 2))
                                                    {
                                                    strModeIndicator = strTemp.substring(0, 1);

                                                    // Mode indicator, (A=Autonomous, D=Differential, E=Estimated, N=Data not valid)
                                                    if ((GpsInstrumentReceiverInterface.MODE_AUTONOMOUS.equals(strModeIndicator))
                                                        || (GpsInstrumentReceiverInterface.MODE_DIFFERENTIAL.equals(strModeIndicator))
                                                        || (GpsInstrumentReceiverInterface.MODE_ESTIMATED.equals(strModeIndicator))
                                                        || (GpsInstrumentReceiverInterface.MODE_NOTVALID.equals(strModeIndicator)))
                                                        {
                                                        LOGGER.debugGpsEvent(debug,
                                                                             "[mode=" + strModeIndicator + "]");

                                                        // Read the checksum, including the prefix '*'
                                                        strChecksum = strTemp.substring(1);

                                                        // Read the HEX checksum
                                                        intChecksum = Integer.parseInt(strChecksum.substring(1), RADIX_16);
                                                        LOGGER.debugGpsEvent(debug,
                                                                             "[checksum=" + Integer.toHexString(intChecksum) + "]");

                                                        // Check the checksum...
                                                        // This could be expanded
                                                        if (CHECKSUM_DELIMITER.equals(strChecksum.substring(0, 1)))
                                                            {
                                                            // Everything worked Ok!
                                                            LOGGER.debugGpsEvent(debug,
                                                                                 "$GPRMC decoded successfully (NMEA v3)");
                                                            boolSuccess = true;
                                                            }
                                                        else
                                                            {
                                                            // Checksum delimiter invalid (very unlikely)
                                                            LOGGER.debugGpsEvent(debug,
                                                                                 "Invalid checksum");
                                                            boolSuccess = false;
                                                            }
                                                        }
                                                    else
                                                        {
                                                        // Mode Indicator was incorrect
                                                        LOGGER.debugGpsEvent(debug,
                                                                             "Invalid Mode Indicator");
                                                        boolSuccess = false;
                                                        }
                                                    }
                                                else
                                                    {
                                                    LOGGER.debugGpsEvent(debug,
                                                                         "Invalid final token");
                                                    boolSuccess = false;
                                                    }
                                                }

                                            //-----------------------------------------------------------------------------------

                                            else
                                                {
                                                // Something is very wrong with the pointers
                                                LOGGER.debugGpsEvent(debug,
                                                                     "Invalid number of final tokens");
                                                boolSuccess = false;
                                                }
                                            }
                                        else
                                            {
                                            // Longitude hemisphere was expected to be "E" or "W"
                                            LOGGER.debugGpsEvent(debug,
                                                                 "Invalid Longitude hemisphere (E, W)");
                                            boolSuccess = false;
                                            }
                                        }
                                    else
                                        {
                                        LOGGER.debugGpsEvent(debug,
                                                             "Invalid Longitude hemisphere token");
                                        boolSuccess = false;
                                        }
                                    }
                                else
                                    {
                                    LOGGER.debugGpsEvent(debug,
                                                         "Invalid Longitude token");
                                    boolSuccess = false;
                                    }
                                }
                            else
                                {
                                // Latitude hemisphere was expected to be "S" or "N"
                                LOGGER.debugGpsEvent(debug,
                                                     "Invalid Latitude hemisphere (S, N)");
                                boolSuccess = false;
                                }
                            }
                        else
                            {
                            LOGGER.debugGpsEvent(debug,
                                                 "Invalid Latitude hemisphere token");
                            boolSuccess = false;
                            }
                        }
                    else
                        {
                        LOGGER.debugGpsEvent(debug,
                                             "Invalid Latitude token");
                        boolSuccess = false;
                        }
                    }
                else
                    {
                    // The data is not valid (receiver warning)
                    LOGGER.debugGpsEvent(debug,
                                         "Invalid data (receiver warning)");
                    boolSuccess = false;
                    }
                }
            else
                {
                // Not enough tokens were found to be decoded correctly
                LOGGER.debugGpsEvent(debug,
                                     "$GPRMC Invalid token count [found=" + intTokenCount + "]");
                boolSuccess = false;
                }
            }

        catch (IndexOutOfBoundsException exception)
            {
            // Just in case things go horribly wrong!
            LOGGER.debugGpsEvent(debug,
                                 "IndexOutOfBounds");
            boolSuccess = false;
            }

        catch (NumberFormatException exception)
            {
            LOGGER.debugGpsEvent(debug,
                                 "Invalid NumberFormat");
            boolSuccess = false;
            }

        if (!boolSuccess)
            {
            dataGPRMC = new GPRMCData();
            LOGGER.debugGpsEvent(debug,
                                 "GPRMC Returning default data");
            }

        return (dataGPRMC);
        }


    /***********************************************************************************************
     * Decode the $GPGGA sentence.
     * Obtains Latitude and Longitude.
     *
     * @param sentence
     * @param valid
     * @param debug
     *
     * @return GPGGAData
     */

    public static GPGGAData parseGPGGA(final String sentence,
                                       final boolean valid,
                                       final boolean debug)
        {
        GPGGAData dataGPGGA;
        boolean boolSuccess;


        dataGPGGA = new GPGGAData();

        LOGGER.debugGpsEvent(debug,
                             GpsInstrumentReceiverInterface.LINE);
        LOGGER.debugGpsEvent(debug,
                             "GpsReceiverNMEAParsers.parseGPGGA()");

        if ((!valid)
            || (sentence == null)
            || (sentence.trim().length() == 0))
            {
            LOGGER.debugGpsEvent(debug,
                                 "$GPGGA not valid, returning defaults");
            return (dataGPGGA);
            }

        try
            {
            final String[] arrayTokens;
            final int intTokenCount;
            int intTokenPointer;
            final String strChecksum;
            final int intChecksum;
            final String strTimeStamp;
            String strUnits;
            final String strLatitude;
            final String strLongitude;
            final String strLatHemisphere;
            final String strLongHemisphere;

            arrayTokens = sentence.split(NMEA_DELIMITER);

            if ((arrayTokens != null)
                && (arrayTokens.length > 0))
                {
                for (int i = 0;
                     i < arrayTokens.length;
                     i++)
                    {
                    final String token;

                    token = arrayTokens[i];

                    LOGGER.debugGpsEvent(debug,
                                         i + " [token=" + token + "]");
                    }

                intTokenCount = arrayTokens.length;
                intTokenPointer = 0;

                // There must be enough tokens to get as far as the Fix Quality
                if (intTokenCount >= (GPGGA_TOKENS - 7))
                    {
                    LOGGER.debugGpsEvent(debug,
                                         GpsInstrumentReceiverInterface.LINE);
                    LOGGER.debugGpsEvent(debug,
                                         "Parsing $GPGGA [tokens=" + intTokenCount + "]");

                    // Discard the sentence identifier
                    intTokenPointer++;

                    // Read the TimeStamp
                    strTimeStamp = arrayTokens[intTokenPointer++];

                    if ((strTimeStamp != null)
                        && (!EMPTY_STRING.equals(strTimeStamp)))
                        {
                        // Note that seconds can have a fractional part
                        dataGPGGA.setTimeStamp(new HourMinSecDataType(true,
                                                                      Integer.parseInt(strTimeStamp.substring(0, 2)),
                                                                      Integer.parseInt(strTimeStamp.substring(2, 4)),
                                                                      Double.parseDouble(strTimeStamp.substring(4))));
                        LOGGER.debugGpsEvent(debug,
                                             "[timestamp=" + dataGPGGA.getTimeStamp().toString() + "]");
                        }

                    // Read the latitude and decode the degrees and minutes
                    strLatitude = arrayTokens[intTokenPointer++];

                    if ((strLatitude != null)
                        && (!EMPTY_STRING.equals(strLatitude)))
                        {
                        // Convert the minutes to degrees
                        dataGPGGA.setLatitude(Double.parseDouble(strLatitude.substring(2)) / GpsInstrumentReceiverInterface.MINUTES_PER_DEGREE);
                        // Add in the degrees (two digits)
                        dataGPGGA.setLatitude(dataGPGGA.getLatitude() + Double.parseDouble(strLatitude.substring(0, 2)));

                        // and the latitude hemisphere
                        strLatHemisphere = arrayTokens[intTokenPointer++];

                        if ((strLatHemisphere != null)
                            && (!EMPTY_STRING.equals(strLatHemisphere)))
                            {
                            if ((GpsInstrumentReceiverInterface.HEMISPHERE_SOUTH.equals(strLatHemisphere))
                                || (GpsInstrumentReceiverInterface.HEMISPHERE_NORTH.equals(strLatHemisphere)))
                                {
                                // Latitude is POSITIVE to the NORTH.
                                if (GpsInstrumentReceiverInterface.HEMISPHERE_SOUTH.equals(strLatHemisphere))
                                    {
                                    dataGPGGA.setLatitude(-dataGPGGA.getLatitude());
                                    }

                                LOGGER.debugGpsEvent(debug,
                                                     "[latitude=" + dataGPGGA.getLatitude() + "]");

                                // Read the longitude and decode the degrees and minutes
                                strLongitude = arrayTokens[intTokenPointer++];

                                if ((strLongitude != null)
                                    && (!EMPTY_STRING.equals(strLongitude)))
                                    {
                                    // $GPGGA,170834,4124.8963,N,08151.6838,W,1,05,1.5,280.2,M,-34.0,M,,,*75

                                    // Convert the minutes to degrees
                                    // 08151.6838
                                    dataGPGGA.setLongitude(Double.parseDouble(strLongitude.substring(3)) / GpsInstrumentReceiverInterface.MINUTES_PER_DEGREE);
                                    // Add in the degrees (three digits)
                                    dataGPGGA.setLongitude(dataGPGGA.getLongitude() + Double.parseDouble(strLongitude.substring(0, 3)));

                                    // and the longitude hemisphere
                                    strLongHemisphere = arrayTokens[intTokenPointer++];

                                    if ((strLongHemisphere != null)
                                        && (!EMPTY_STRING.equals(strLongHemisphere)))
                                        {
                                        if ((GpsInstrumentReceiverInterface.HEMISPHERE_EAST.equals(strLongHemisphere))
                                            || (GpsInstrumentReceiverInterface.HEMISPHERE_WEST.equals(strLongHemisphere)))
                                            {
                                            // Astronomical convention is that Longitudes are POSITIVE in the WEST
                                            if (GpsInstrumentReceiverInterface.HEMISPHERE_EAST.equals(strLongHemisphere))
                                                {
                                                dataGPGGA.setLongitude(-dataGPGGA.getLongitude());
                                                }

                                            LOGGER.debugGpsEvent(debug,
                                                                 "[longitude=" + dataGPGGA.getLongitude() + "]");

                                            // Read the Fix Quality, and subsequent tokens if Ok
                                            dataGPGGA.setFixQuality(Integer.parseInt(arrayTokens[intTokenPointer++]));
                                            LOGGER.debugGpsEvent(debug,
                                                                 "[fixquality=" + dataGPGGA.getFixQuality() + "]");

                                            if (dataGPGGA.getFixQuality() > 0)
                                                {
                                                // Now check that we have enough tokens left
                                                LOGGER.debugGpsEvent(debug,
                                                                     "[tokensleft=" + (intTokenCount-intTokenPointer) + "]");

                                                // Read the number of satellites in view
                                                dataGPGGA.setSatellitesInUseCount(Integer.parseInt(arrayTokens[intTokenPointer++]));
                                                LOGGER.debugGpsEvent(debug,
                                                                     "[satellites=" + dataGPGGA.getSatellitesInUseCount() + "]");

                                                // Read HDOP
                                                dataGPGGA.setHDOP(Double.parseDouble(arrayTokens[intTokenPointer++]));
                                                LOGGER.debugGpsEvent(debug,
                                                                     "[HDOP=" + dataGPGGA.getHDOP() + "]");

                                                // Read Altitude
                                                dataGPGGA.setAltitude(Double.parseDouble(arrayTokens[intTokenPointer++]));
                                                LOGGER.debugGpsEvent(debug,
                                                                     "[altitude=" + dataGPGGA.getAltitude() + "]");

                                                // Read altitude units
                                                strUnits = arrayTokens[intTokenPointer++];
                                                LOGGER.debugGpsEvent(debug,
                                                                     "[units=" + strUnits + "]");

                                                if (GpsInstrumentReceiverInterface.UNITS_METRES.equals(strUnits))
                                                    {
                                                    // Read height of geoid above WGS84 ellipsoid
                                                    dataGPGGA.setGeoid(Double.parseDouble(arrayTokens[intTokenPointer++]));
                                                    LOGGER.debugGpsEvent(debug,
                                                                         "[geoid=" + dataGPGGA.getGeoid() + "]");

                                                    // Read geoid units
                                                    strUnits = arrayTokens[intTokenPointer];
                                                    LOGGER.debugGpsEvent(debug,
                                                                         "[units=" + strUnits + "]");

                                                    if (GpsInstrumentReceiverInterface.UNITS_METRES.equals(strUnits))
                                                        {
                                                        //      $GPGGA, 170834,     4124.8963, N, 08151.6838, W, 1, 05, 1.5, 280.2, M, -34.0, M,    ,  ,*75
                                                        //      $GPGGA, hhmmss.ss,  ddmm.mmm,  a, dddmm.mmm,  b, q, xx, p.p, a.b,   M,   c.d, M, x.x, nnnn
                                                        // USB  $GPGGA, 082005.622, 5206.2820, N, 00118.7042, E, 1, 05, 1.5, 86.2,  M,  47.0, M, 0.0, 0000*45]

                                                        // Read the HEX checksum
                                                        // Skip any remaining fields, until the Checksum at the end
                                                        // The checksum field may be '*NN' or 'NNNN*NN'
                                                        strChecksum = arrayTokens[arrayTokens.length-1];

                                                        // so just take the very *last* two characters
                                                        intChecksum = Integer.parseInt(strChecksum.substring(strChecksum.length()-2), RADIX_16);
                                                        LOGGER.debugGpsEvent(debug,
                                                                             "[checksum=" + Integer.toHexString(intChecksum) + "]");

                                                        // Check the checksum...
                                                        // This could be expanded
                                                        // The delimiter must be three from the end
                                                        if (CHECKSUM_DELIMITER.equals(strChecksum.substring(strChecksum.length()-3,
                                                                                                            strChecksum.length()-2)))
                                                            {
                                                            // Everything worked Ok!
                                                            LOGGER.debugGpsEvent(debug,
                                                                                 "$GPGGA decoded successfully");
                                                            boolSuccess = true;
                                                            }
                                                        else
                                                            {
                                                            // Checksum delimiter invalid (very unlikely)
                                                            LOGGER.debugGpsEvent(debug,
                                                                                 "Invalid checksum");
                                                            boolSuccess = false;
                                                            }
                                                        }
                                                    else
                                                        {
                                                        // Geoid Units were expected to be Metres
                                                        LOGGER.debugGpsEvent(debug,
                                                                             "Invalid geoid units");
                                                        boolSuccess = false;
                                                        }
                                                    }
                                                else
                                                    {
                                                    // Altitude Units were expected to be Metres
                                                    LOGGER.debugGpsEvent(debug,
                                                                         "Invalid altitude units");
                                                    boolSuccess = false;
                                                    }
                                                }
                                            else
                                                {
                                                // Unable to get a valid fix
                                                LOGGER.debugGpsEvent(debug,
                                                                     "Invalid fix");
                                                boolSuccess = false;
                                                }
                                            }
                                        else
                                            {
                                            // Longitude hemisphere was expected to be "E" or "W"
                                            LOGGER.debugGpsEvent(debug,
                                                                 "Invalid Longitude hemisphere (E, W)");
                                            boolSuccess = false;
                                            }
                                        }
                                    else
                                        {
                                        LOGGER.debugGpsEvent(debug,
                                                             "Invalid Longitude hemisphere token");
                                        boolSuccess = false;
                                        }
                                    }
                                else
                                    {
                                    LOGGER.debugGpsEvent(debug,
                                                         "Invalid Longitude token");
                                    boolSuccess = false;
                                    }
                                }
                            else
                                {
                                // Latitude hemisphere was expected to be "S" or "N"
                                LOGGER.debugGpsEvent(debug,
                                                     "Invalid Latitude hemisphere (S, N)");
                                boolSuccess = false;
                                }
                            }
                        else
                            {
                            LOGGER.debugGpsEvent(debug,
                                                 "Invalid Latitude hemisphere token");
                            boolSuccess = false;
                            }
                        }
                    else
                        {
                        LOGGER.debugGpsEvent(debug,
                                             "Invalid Latitude token");
                        boolSuccess = false;
                        }
                    }
                else
                    {
                    // Not enough tokens were found to be decoded correctly
                    LOGGER.debugGpsEvent(debug,
                                         "Not enough tokens in $GPGGA ([found=" + intTokenCount + "]");
                    boolSuccess = false;
                    }
                }
            else
                {
                boolSuccess = false;
                }
            }

        catch (IndexOutOfBoundsException exception)
            {
            LOGGER.debugGpsEvent(debug,
                                 "IndexOutOfBoundsException");
            boolSuccess = false;
            }

        catch (NumberFormatException exception)
            {
            LOGGER.debugGpsEvent(debug,
                                 "Invalid NumberFormat");
            boolSuccess = false;
            }

        if (!boolSuccess)
            {
            dataGPGGA = new GPGGAData();
            LOGGER.debugGpsEvent(debug,
                                 "GPGGA Returning default data");

            }

        return (dataGPGGA);
        }


    /***********************************************************************************************
     * Decode the $GPGSA sentence.
     *
     * @param sentence
     * @param valid
     *
     * @param debug
     * @return GPGSAData
     */

    public static GPGSAData parseGPGSA(final String sentence,
                                       final boolean valid,
                                       final boolean debug)
        {
        GPGSAData dataGPGSA;
        boolean boolSuccess;

        dataGPGSA = new GPGSAData();

        try
            {
            final String[] arrayTokens;
            final int intTokenCount;
            int intTokenPointer;
            final String strChecksum;
            final int intChecksum;
            final String strFixType;
            final String strPDOP;
            final String strHDOP;
            final String strVDOP;
            String strTemp;

            LOGGER.debugGpsEvent(debug,
                                 GpsInstrumentReceiverInterface.LINE);
            if (!valid)
                {
                LOGGER.debugGpsEvent(debug,
                                     "$GPGSA not valid, returning defaults"
                );
                return (dataGPGSA);
                }

            arrayTokens = sentence.split(NMEA_DELIMITER);

            LOGGER.debugGpsEvent(debug,
                                 "GpsReceiverNMEAParsers.parseGPGSA()"
            );
            for (int i = 0;
                 i < arrayTokens.length;
                 i++)
                {
                final String token = arrayTokens[i];

                LOGGER.debugGpsEvent(debug,
                                     i + " [token=" + token + "]");
                }

            intTokenCount = arrayTokens.length;
            intTokenPointer = 0;

            LOGGER.debugGpsEvent(debug,
                                 GpsInstrumentReceiverInterface.LINE);
            LOGGER.debugGpsEvent(debug,
                                 "Parsing $GPGSA [tokens=" + intTokenCount + "]");

            // Discard the sentence identifier
            intTokenPointer++;

            // Read the Mode
            dataGPGSA.setFixMode(arrayTokens[intTokenPointer++]);
            LOGGER.debugGpsEvent(debug,
                                 "[fixmode=" + dataGPGSA.getFixMode() + "]");

            // Read the Fix Type
            strFixType = arrayTokens[intTokenPointer++];

            if ((strFixType != null)
                && (!EMPTY_STRING.equals(strFixType)))
                {
                dataGPGSA.setFixType(Integer.parseInt(strFixType));
                LOGGER.debugGpsEvent(debug,
                                     "[fixtype=" + dataGPGSA.getFixType() + "]");
                }

            if (dataGPGSA.getFixType() > 1)
                {
                // Read the satellites in view used for the fix
                // A maximum of 12 entries
                for (int i = 0; i < GpsInstrumentReceiverInterface.MAX_SATELLITES_IN_VIEW; i++)
                    {
                    // Read a token, and see if the field is occupied
                    strTemp = arrayTokens[intTokenPointer++];

                    if ((strTemp != null)
                        && (!EMPTY_STRING.equals(strTemp))
                        && (!NMEA_DELIMITER.equals(strTemp)))
                        {
                        // Record the satellite ID in the vector
                        dataGPGSA.getSatellitesInUse().add(strTemp);
                        LOGGER.debugGpsEvent(debug,
                                             "[add satelliteid=" + strTemp + "]");
                        }
                    else
                        {
                        LOGGER.debugGpsEvent(debug,
                                             "[skip null satelliteid=" +  strTemp + "]");
                        }
                    }

                // Read the PDOP
                strPDOP = arrayTokens[intTokenPointer++];

                if ((strPDOP != null)
                    && (!EMPTY_STRING.equals(strPDOP)))
                    {
                    dataGPGSA.setPDOP(Double.parseDouble(strPDOP));
                    LOGGER.debugGpsEvent(debug,
                                         "[PDOP=" + dataGPGSA.getPDOP() + "]");
                    }

                // Read the HDOP
                strHDOP = arrayTokens[intTokenPointer++];

                if ((strHDOP != null)
                    && (!EMPTY_STRING.equals(strHDOP)))
                    {
                    dataGPGSA.setHDOP(Double.parseDouble(strHDOP));
                    LOGGER.debugGpsEvent(debug,
                                         "[HDOP=" + dataGPGSA.getHDOP() + "]");
                    }

                // The last token contains the VDOP and the checksum (duh!)
                strTemp = arrayTokens[intTokenPointer];

                // The VDOP may be missing?!
                if ((strTemp != null)
                    && (!EMPTY_STRING.equals(strTemp))
                    && (strTemp.length() >= 2)
                    && (CHECKSUM_DELIMITER.equals(strTemp.substring(0, 1))))
                    {
                    // Set a neutral VDOP
                    strVDOP = "1.0";

                    // There is no VDOP information, so just read the checksum
                    strChecksum = strTemp.substring(1);
                    }
                else
                    {
                    final String[] arrayEndTokens;

                    // We have a VDOP and checksum, so retokenize using the checksum delimiter
                    arrayEndTokens = strTemp.split(REGEX_CHECKSUM_DELIMITER);

                    for (int i = 0;
                         i < arrayEndTokens.length;
                         i++)
                        {
                        final String token;

                        token = arrayEndTokens[i];

                        LOGGER.debugGpsEvent(debug,
                                             i + " [token=" + token + "]");
                        }

                    if (arrayEndTokens.length >= 2)
                        {
                        // Read the VDOP
                        strVDOP = arrayEndTokens[0];

                        // Read the checksum
                        strChecksum = arrayEndTokens[1];
                        LOGGER.debugGpsEvent(debug,
                                             "[strchecksum=" + strChecksum + "]");
                        }
                    else
                        {
                        strVDOP = "0";
                        strChecksum = "00";
                        }
                    }

                if ((strVDOP != null)
                    && (!EMPTY_STRING.equals(strVDOP)))
                    {
                    dataGPGSA.setVDOP(Double.parseDouble(strVDOP));
                    LOGGER.debugGpsEvent(debug,
                                         "[VDOP=" + dataGPGSA.getVDOP() + "]");
                    }

                intChecksum = Integer.parseInt(strChecksum, RADIX_16);
                LOGGER.debugGpsEvent(debug,
                                     "[checksum=" + Integer.toHexString(intChecksum) + "]");

                // ToDo Check the checksum...
                // This could be expanded
                if (true)
                    {
                    // Everything worked Ok!
                    LOGGER.debugGpsEvent(debug,
                                         "$GPGSA decoded successfully");
                    boolSuccess = true;
                    }
                else
                    {
                    // Checksum delimiter invalid (very unlikely)
                    LOGGER.debugGpsEvent(debug,
                                         "Invalid checksum");
                    boolSuccess = false;
                    }
                }
            else
                {
                // No fix available
                LOGGER.debugGpsEvent(debug,
                                     "No fix available");
                boolSuccess = false;
                }
            }

        catch (IndexOutOfBoundsException exception)
            {
            // Just in case things go horribly wrong!
            LOGGER.debugGpsEvent(debug,
                                 "IndexOutOfBounds");
            boolSuccess = false;
            }

        catch (NumberFormatException exception)
            {
            LOGGER.debugGpsEvent(debug,
                                 "Invalid NumberFormat");
            boolSuccess = false;
            }

        if (!boolSuccess)
            {
            dataGPGSA = new GPGSAData();
            LOGGER.debugGpsEvent(debug,
                                 "GPGSA Returning default data");
            }

        return (dataGPGSA);
        }
    }
