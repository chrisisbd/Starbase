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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.fcdrx.dao;

import org.apache.xmlbeans.XmlObject;
import org.lmn.fc.common.utilities.misc.Utilities;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.AbstractObservatoryInstrumentDAO;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.CommandPoolList;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.commands.core.Reset;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.DAOHelper;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageInterface;
import org.lmn.fc.model.xmlbeans.instruments.CommandType;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;
import uk.org.funcube.fcdapi.FCD;

import java.util.Vector;


/***************************************************************************************************
 * FunCubeDongleRxDAO.
 *
 * http://www.funcubedongle.com
 * http://www.oz9aec.net/index.php/funcube-dongle
 * http://www.oz9aec.net/index.php/funcube-dongle/qthid-fcd-controller
 *
 * http://ashbysoft.com/wiki/moin.cgi
 * http://sourceforge.net/apps/mediawiki/hamlib/index.php?title=Main_Page
 */

public final class FunCubeDongleRxDAO extends AbstractObservatoryInstrumentDAO
                                      implements ObservatoryInstrumentDAOInterface
    {
    private static final int DAO_CHANNEL_COUNT = 1;


    /***********************************************************************************************
     * A simple test routine (from Phil Ashby).
     * See: https://github.com/phlash/qthid/tree/phlash
     *
     * @throws Exception
     */

    private static void testFDC() throws Exception
        {
        final String SOURCE = "FUNcube Dongle: ";
        final int MAX_TRIES = 100;
        final FCD fcd;
        int intTries;
        int intReturn;

        fcd = new FCD();
        intTries = 0;

        do
            {
            intReturn = fcd.fcdGetMode();
            LOGGER.log(SOURCE + "fcdGetMode()=" + intReturn);

            if (FCD.FME_APP != intReturn)
                {
                LOGGER.log(SOURCE + "Not in application mode, trying to reset.. [" + intTries + "]");
                intReturn = fcd.fcdBlReset();

                Utilities.safeSleep(1000);
                }

            intTries++;
            }
        while ((intTries < MAX_TRIES)
               && (intReturn != FCD.FME_APP));

        // Did we succeed?
        if (FCD.FME_APP == intReturn)
            {
            final StringBuffer buffer;
            int intFrequencykHz;

            buffer = new StringBuffer();
            intReturn = fcd.fcdGetFwVerStr(buffer);
            LOGGER.log(SOURCE + "fcdGetFwVerStr()=" + intReturn + " ver=" + buffer);

            for (intFrequencykHz = 50000;
                 intFrequencykHz <= 1500000;
                 intFrequencykHz += 50000)
                {
                intReturn = fcd.fcdAppSetFreqkHz(intFrequencykHz);
                LOGGER.log(SOURCE + "fcdAppSetFreqkHz(" + intFrequencykHz + ")=" + intReturn);

                Utilities.safeSleep(250);
                }
            }
        else
            {
            LOGGER.log(SOURCE + "Unable to reset the dongle!");
            }
        }


    /***********************************************************************************************
     * Build the CommandPool using method names in this DAO.
     *
     * @param pool
     */

    private static void addSubclassToCommandPool(final CommandPoolList pool)
        {
        }


    /***********************************************************************************************
     * Construct a FunCubeDongleRxDAO.
     *
     * @param hostinstrument
     */

    public FunCubeDongleRxDAO(final ObservatoryInstrumentInterface hostinstrument)
        {
        super(hostinstrument);

        setRawData(new Vector<Object>(1000));

        addSubclassToCommandPool(getCommandPool());
        }


    /***********************************************************************************************
     * Initialise the DAO.
     *
     * @param resourcekey
     */

    public boolean initialiseDAO(final String resourcekey)
        {
        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               "FunCubeDongleRxDAO.initialiseDAO() [resourcekey=" + resourcekey + "]");

        super.initialiseDAO(resourcekey);

        DAOHelper.loadSubClassResourceBundle(this);

        setRawData(new Vector<Object>(1000));

        return (true);
        }


    /***********************************************************************************************
     * Shut down the DAO and dispose of all Resources.
     */

    public void disposeDAO()
        {
        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               "FunCubeDongleRxDAO.disposeDAO()");

        super.disposeDAO();
        }


    /***********************************************************************************************
     * Construct a CommandMessage appropriate to this DAO.
     *
     * @param dao
     * @param instrumentxml
     * @param module
     * @param command
     * @param starscript
     *
     * @return CommandMessageInterface
     */

    public CommandMessageInterface constructCommandMessage(final ObservatoryInstrumentDAOInterface dao,
                                                           final Instrument instrumentxml,
                                                           final XmlObject module,
                                                           final CommandType command,
                                                           final String starscript)
        {
        return (new FunCubeDongleRxCommandMessage(dao,
                                                  instrumentxml,
                                                  module,
                                                  command,
                                                  starscript.trim()));
        }


    /***********************************************************************************************
     * Construct a ResponseMessage appropriate to this DAO.
     *
     *
     * @param portname
     * @param instrumentxml
     * @param module
     * @param command
     * @param starscript
     * @param responsestatusbits
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface constructResponseMessage(final String portname,
                                                             final Instrument instrumentxml,
                                                             final XmlObject module,
                                                             final CommandType command,
                                                             final String starscript,
                                                             final int responsestatusbits)
        {
        return (new FunCubeDongleRxResponseMessage(portname,
                                                   instrumentxml,
                                                   module,
                                                   command,
                                                   starscript.trim(),
                                                   responsestatusbits));
        }


    /**********************************************************************************************/
    /* DAO Local Commands                                                                         */
    /***********************************************************************************************
     * reset() resets the whole Instrument.
     * This local Command may be overridden by a Command which sends a reset() to the DAO Port.
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface reset(final CommandMessageInterface commandmessage)
        {
        LOGGER.debugStaribusEvent(LOADER_PROPERTIES.isStaribusDebug(),
                                  "Entered DAO reset");

        try
            {
            testFDC();
            }

        catch (Exception exception)
            {
            LOGGER.error("FunCubeDongleRxDAO.reset() [exception=" + exception.getMessage() + "]");
            }

        return Reset.doReset(this, commandmessage);
        }


    /***********************************************************************************************
     * Read all the Resources required by the DAO.
     */

    public void readResources()
        {
        //System.out.println("FunCubeDongleRxDAO.readResources() [ResourceKey=" + getResourceKey() + "]");

        super.readResources();
        }
    }

//--------------------------------------------------------------------------------------------------
// Java binding to libfcd.so using JNA (http://jna.java.net)
// TODO:GPL3 here

//package uk.org.funcube.fcdapi;
//
//import com.sun.jna.Library;
//import com.sun.jna.Native;
//import com.sun.jna.NativeLibrary;
//import java.net.URI;
//
//public class FCD {
//
//// Return consts..
//public static final int FME_NONE = 0;
//public static final int FME_BL = 1;
//public static final int FME_APP = 2;
//
//// Declare the methods of the underlying C library as an interface
//// Keep in step with fcd.h!
//public interface libfcd extends Library {
//int fcdGetMode();
//int fcdGetFwVerStr(byte[] str);
//int fcdAppReset();
//int fcdAppSetFreqkHz(int nFreq);
//int fcdAppSetParam(byte cmd, byte[] data, int len);
//int fcdAppGetParam(byte cmd, byte[] data, int len);
//int fcdBlReset();
//int fcdBlErase();
//int fcdBlWriteFirmware(byte[] pc,long n64Size);
//int fcdBlVerifyFirmware(byte[] pc,long n64Size);
//}
//
//// instantiate a private static instance of the library
//private static libfcd inst;
//static {
//try {
//// Some ugly path hackery to avoid LD_LIBRARY_PATH et al:
//// 1. Get a URL for a known file in our jar file, extract path component..
//String pth = FCD.class.getResource("/uk/org/funcube/fcdapi/FCD.class").getPath();
////System.err.println("Res path="+pth);
//// 2. Decode via URI incase we have whitespace in the path, extract next path component..
//pth = new URI(pth).getPath();
////System.err.println("Dec path="+pth);
//// 3. Trim off trailing text to get our installation directory.
//int i1 = pth.indexOf('!');
//int i2 = i1>0 ? pth.lastIndexOf('/', i1) : -1;
//if (i2>0) {
//pth = pth.substring(0, i2+1);
////System.err.println("native library: " + pth);
//NativeLibrary.addSearchPath("fcd", pth);
//}
//inst = (libfcd) Native.loadLibrary("fcd", libfcd.class);
//} catch (Exception e) {
//e.printStackTrace();
//}
//}
//
//// Provide synchronized wrappers for library
//public synchronized int fcdGetMode() {
//return inst.fcdGetMode();
//}
//
//public synchronized int fcdGetFwVerStr(StringBuffer ver) {
//byte[] buf = new byte[10]; // XXX:Beware undocumented buffer size in fcd.h/c!
//int rv = inst.fcdGetFwVerStr(buf);
//if (FME_APP == rv)
//ver.append(Native.toString(buf));
//return rv;
//}
//
//public synchronized int fcdAppReset() {
//return inst.fcdAppReset();
//}
//
//public synchronized int fcdAppSetFreqkHz(int nFreq) {
//return inst.fcdAppSetFreqkHz(nFreq);
//}
//
//public synchronized int fcdAppSetParam(byte cmd, byte[] data, int len) {
//return inst.fcdAppSetParam(cmd, data, len);
//}
//
//public synchronized int fcdAppGetParam(byte cmd, byte[] data, int len) {
//return inst.fcdAppGetParam(cmd, data, len);
//}
//
//public synchronized int fcdBlReset() {
//return inst.fcdBlReset();
//}
//
//public synchronized int fcdBlErase() {
//return inst.fcdBlErase();
//}
//
//public synchronized int fcdBlWriteFirmware(byte[] pc,long n64Size) {
//return inst.fcdBlWriteFirmware(pc, n64Size);
//}
//
//public synchronized int fcdBlVerifyFirmware(byte[] pc,long n64Size) {
//return inst.fcdBlVerifyFirmware(pc, n64Size);
//}
//
//// Test entry point
//public static void main(String[] args) {
//FCD test = new FCD();
//System.out.println("FCDGetMode()=" + test.fcdGetMode());
//}
//}

// See: https://github.com/phlash/qthid/blob/phlash/fcdhidcmd.h
//
///* Commands applicable in bootloader mode */
//#define FCD_CMD_BL_QUERY 1 /*!< Returns string with "FCDAPP version". */
//#define FCD_CMD_BL_RESET 8 /*!< Reset to application mode. */
//#define FCD_CMD_BL_ERASE 24 /*!< Erase firmware from FCD flash. */
//#define FCD_CMD_BL_SET_BYTE_ADDR 25 /*!< TBD */
//#define FCD_CMD_BL_GET_BYTE_ADDR_RANGE 26 /*!< Get address range. */
//#define FCD_CMD_BL_WRITE_FLASH_BLOCK 27 /*!< Write flash block. */
//#define FCD_CMD_BL_READ_FLASH_BLOCK 28 /*!< Read flash block. */
//
///* Commands applicable in application mode */
//#define FCD_CMD_APP_SET_FREQ_KHZ 100 /*!< Send with 3 byte unsigned little endian frequency in kHz. */
//#define FCD_CMD_APP_SET_FREQ_HZ 101 /*!< Send with 4 byte unsigned little endian frequency in Hz, returns wit actual frequency set in Hz */
//#define FCD_CMD_APP_GET_FREQ_HZ 102 /*!< Returns 4 byte unsigned little endian frequency in Hz. */
//
//#define FCD_CMD_APP_GET_IF_RSSI 104 /*!< Returns 1 byte unsigned IF RSSI, -35dBm ~=0, -10dBm ~=70. */
//#define FCD_CMD_APP_GET_PLL_LOCK 105 /*!< Returns 1 bit, true if locked. */
//
//#define FCD_CMD_APP_SET_DC_CORR 106 /*!< Send with 2 byte unsigned I DC correction followed by 2 byte unsigned Q DC correction. 32768 is the default centre value. */
//#define FCD_CMD_APP_GET_DC_CORR 107 /*!< Returns 2 byte unsigned I DC correction followed by 2 byte unsigned Q DC correction. 32768 is the default centre value. */
//#define FCD_CMD_APP_SET_IQ_CORR 108 /*!< Send with 2 byte signed phase correction followed by 2 byte unsigned gain correction. 0 is the default centre value for phase correction, 32768 is the default centre value for gain. */
//#define FCD_CMD_APP_GET_IQ_CORR 109 /*!< Returns 2 byte signed phase correction followed by 2 byte unsigned gain correction. 0 is the default centre value for phase correction, 32768 is the default centre value for gain. */
//
//#define FCD_CMD_APP_SET_LNA_GAIN 110 /*!< Send a 1 byte value, see enums for reference. */
//#define FCD_CMD_APP_SET_LNA_ENHANCE 111
//#define FCD_CMD_APP_SET_BAND 112
//#define FCD_CMD_APP_SET_RF_FILTER 113
//#define FCD_CMD_APP_SET_MIXER_GAIN 114
//#define FCD_CMD_APP_SET_BIAS_CURRENT 115
//#define FCD_CMD_APP_SET_MIXER_FILTER 116
//#define FCD_CMD_APP_SET_IF_GAIN1 117
//#define FCD_CMD_APP_SET_IF_GAIN_MODE 118
//#define FCD_CMD_APP_SET_IF_RC_FILTER 119
//#define FCD_CMD_APP_SET_IF_GAIN2 120
//#define FCD_CMD_APP_SET_IF_GAIN3 121
//#define FCD_CMD_APP_SET_IF_FILTER 122
//#define FCD_CMD_APP_SET_IF_GAIN4 123
//#define FCD_CMD_APP_SET_IF_GAIN5 124
//#define FCD_CMD_APP_SET_IF_GAIN6 125
//
//#define FCD_CMD_APP_GET_LNA_GAIN 150 // Retrieve a 1 byte value, see enums for reference
//#define FCD_CMD_APP_GET_LNA_ENHANCE 151
//#define FCD_CMD_APP_GET_BAND 152
//#define FCD_CMD_APP_GET_RF_FILTER 153
//#define FCD_CMD_APP_GET_MIXER_GAIN 154
//#define FCD_CMD_APP_GET_BIAS_CURRENT 155
//#define FCD_CMD_APP_GET_MIXER_FILTER 156
//#define FCD_CMD_APP_GET_IF_GAIN1 157
//#define FCD_CMD_APP_GET_IF_GAIN_MODE 158
//#define FCD_CMD_APP_GET_IF_RC_FILTER 159
//#define FCD_CMD_APP_GET_IF_GAIN2 160
//#define FCD_CMD_APP_GET_IF_GAIN3 161
//#define FCD_CMD_APP_GET_IF_FILTER 162
//#define FCD_CMD_APP_GET_IF_GAIN4 163
//#define FCD_CMD_APP_GET_IF_GAIN5 164
//#define FCD_CMD_APP_GET_IF_GAIN6 165
//
//#define FCD_CMD_APP_SEND_I2C_BYTE 200
//#define FCD_CMD_APP_RECV_I2C_BYTE 201
//
//#define FCD_CMD_APP_RESET 255 // Reset to bootloader
//
//typedef enum
//{
//TLGE_N5_0DB=0,
//TLGE_N2_5DB=1,
//TLGE_P0_0DB=4,
//TLGE_P2_5DB=5,
//TLGE_P5_0DB=6,
//TLGE_P7_5DB=7,
//TLGE_P10_0DB=8,
//TLGE_P12_5DB=9,
//TLGE_P15_0DB=10,
//TLGE_P17_5DB=11,
//TLGE_P20_0DB=12,
//TLGE_P25_0DB=13,
//TLGE_P30_0DB=14
//} TUNER_LNA_GAIN_ENUM;
//
//typedef enum
//{
//  TLEE_OFF=0,
//  TLEE_0=1,
//  TLEE_1=3,
//  TLEE_2=5,
//  TLEE_3=7
//} TUNER_LNA_ENHANCE_ENUM;
//
//typedef enum
//{
//  TBE_VHF2,
//  TBE_VHF3,
//  TBE_UHF,
//  TBE_LBAND
//} TUNER_BAND_ENUM;
//
//typedef enum
//{
//  // Band 0, VHF II
//  TRFE_LPF268MHZ=0,
//  TRFE_LPF299MHZ=8,
//  // Band 1, VHF III
//  TRFE_LPF509MHZ=0,
//  TRFE_LPF656MHZ=8,
//  // Band 2, UHF
//  TRFE_BPF360MHZ=0,
//  TRFE_BPF380MHZ=1,
//  TRFE_BPF405MHZ=2,
//  TRFE_BPF425MHZ=3,
//  TRFE_BPF450MHZ=4,
//  TRFE_BPF475MHZ=5,
//  TRFE_BPF505MHZ=6,
//  TRFE_BPF540MHZ=7,
//  TRFE_BPF575MHZ=8,
//  TRFE_BPF615MHZ=9,
//  TRFE_BPF670MHZ=10,
//  TRFE_BPF720MHZ=11,
//  TRFE_BPF760MHZ=12,
//  TRFE_BPF840MHZ=13,
//  TRFE_BPF890MHZ=14,
//  TRFE_BPF970MHZ=15,
//  // Band 2, L band
//  TRFE_BPF1300MHZ=0,
//  TRFE_BPF1320MHZ=1,
//  TRFE_BPF1360MHZ=2,
//  TRFE_BPF1410MHZ=3,
//  TRFE_BPF1445MHZ=4,
//  TRFE_BPF1460MHZ=5,
//  TRFE_BPF1490MHZ=6,
//  TRFE_BPF1530MHZ=7,
//  TRFE_BPF1560MHZ=8,
//  TRFE_BPF1590MHZ=9,
//  TRFE_BPF1640MHZ=10,
//  TRFE_BPF1660MHZ=11,
//  TRFE_BPF1680MHZ=12,
//  TRFE_BPF1700MHZ=13,
//  TRFE_BPF1720MHZ=14,
//  TRFE_BPF1750MHZ=15
//} TUNER_RF_FILTER_ENUM;
//
//typedef enum
//{
//  TMGE_P4_0DB=0,
//  TMGE_P12_0DB=1
//} TUNER_MIXER_GAIN_ENUM;
//
//typedef enum
//{
//  TBCE_LBAND=0,
//  TBCE_1=1,
//  TBCE_2=2,
//  TBCE_VUBAND=3
//} TUNER_BIAS_CURRENT_ENUM;
//
//typedef enum
//{
//  TMFE_27_0MHZ=0,
//  TMFE_4_6MHZ=8,
//  TMFE_4_2MHZ=9,
//  TMFE_3_8MHZ=10,
//  TMFE_3_4MHZ=11,
//  TMFE_3_0MHZ=12,
//  TMFE_2_7MHZ=13,
//  TMFE_2_3MHZ=14,
//  TMFE_1_9MHZ=15
//} TUNER_MIXER_FILTER_ENUM;
//
//typedef enum
//{
//  TIG1E_N3_0DB=0,
//  TIG1E_P6_0DB=1
//} TUNER_IF_GAIN1_ENUM;
//
//typedef enum
//{
//  TIGME_LINEARITY=0,
//  TIGME_SENSITIVITY=1
//} TUNER_IF_GAIN_MODE_ENUM;
//
//typedef enum
//{
//  TIRFE_21_4MHZ=0,
//  TIRFE_21_0MHZ=1,
//  TIRFE_17_6MHZ=2,
//  TIRFE_14_7MHZ=3,
//  TIRFE_12_4MHZ=4,
//  TIRFE_10_6MHZ=5,
//  TIRFE_9_0MHZ=6,
//  TIRFE_7_7MHZ=7,
//  TIRFE_6_4MHZ=8,
//  TIRFE_5_3MHZ=9,
//  TIRFE_4_4MHZ=10,
//  TIRFE_3_4MHZ=11,
//  TIRFE_2_6MHZ=12,
//  TIRFE_1_8MHZ=13,
//  TIRFE_1_2MHZ=14,
//  TIRFE_1_0MHZ=15
//} TUNER_IF_RC_FILTER_ENUM;
//
//typedef enum
//{
//  TIG2E_P0_0DB=0,
//  TIG2E_P3_0DB=1,
//  TIG2E_P6_0DB=2,
//  TIG2E_P9_0DB=3
//} TUNER_IF_GAIN2_ENUM;
//
//typedef enum
//{
//  TIG3E_P0_0DB=0,
//  TIG3E_P3_0DB=1,
//  TIG3E_P6_0DB=2,
//  TIG3E_P9_0DB=3
//} TUNER_IF_GAIN3_ENUM;
//
//typedef enum
//{
//  TIG4E_P0_0DB=0,
//  TIG4E_P1_0DB=1,
//  TIG4E_P2_0DB=2
//} TUNER_IF_GAIN4_ENUM;
//
//typedef enum
//{
//  TIFE_5_50MHZ=0,
//  TIFE_5_30MHZ=1,
//  TIFE_5_00MHZ=2,
//  TIFE_4_80MHZ=3,
//  TIFE_4_60MHZ=4,
//  TIFE_4_40MHZ=5,
//  TIFE_4_30MHZ=6,
//  TIFE_4_10MHZ=7,
//  TIFE_3_90MHZ=8,
//  TIFE_3_80MHZ=9,
//  TIFE_3_70MHZ=10,
//  TIFE_3_60MHZ=11,
//  TIFE_3_40MHZ=12,
//  TIFE_3_30MHZ=13,
//  TIFE_3_20MHZ=14,
//  TIFE_3_10MHZ=15,
//  TIFE_3_00MHZ=16,
//  TIFE_2_95MHZ=17,
//  TIFE_2_90MHZ=18,
//  TIFE_2_80MHZ=19,
//  TIFE_2_75MHZ=20,
//  TIFE_2_70MHZ=21,
//  TIFE_2_60MHZ=22,
//  TIFE_2_55MHZ=23,
//  TIFE_2_50MHZ=24,
//  TIFE_2_45MHZ=25,
//  TIFE_2_40MHZ=26,
//  TIFE_2_30MHZ=27,
//  TIFE_2_28MHZ=28,
//  TIFE_2_24MHZ=29,
//  TIFE_2_20MHZ=30,
//  TIFE_2_15MHZ=31
//} TUNER_IF_FILTER_ENUM;
//
//typedef enum
//{
//  TIG5E_P3_0DB=0,
//  TIG5E_P6_0DB=1,
//  TIG5E_P9_0DB=2,
//  TIG5E_P12_0DB=3,
//  TIG5E_P15_0DB=4
//} TUNER_IF_GAIN5_ENUM;
//
//typedef enum
//{
//  TIG6E_P3_0DB=0,
//  TIG6E_P6_0DB=1,
//  TIG6E_P9_0DB=2,
//  TIG6E_P12_0DB=3,
//  TIG6E_P15_0DB=4
//} TUNER_IF_GAIN6_ENUM;