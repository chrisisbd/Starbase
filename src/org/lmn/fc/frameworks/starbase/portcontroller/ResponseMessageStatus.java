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

package org.lmn.fc.frameworks.starbase.portcontroller;

import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.utilities.misc.Utilities;
import org.lmn.fc.frameworks.starbase.plugins.observatory.comparators.ResponseMessageStatusByIndex;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.model.logging.EventStatus;

import java.util.Collections;
import java.util.Iterator;


/***************************************************************************************************
 * ResponseMessageStatus.
 */

public enum ResponseMessageStatus
    {
    // The 16 + 1 status types for a ResponseMessage integer bitfield
    // The Priority is used to select the most appropriate icon for e.g. EventLogs. The number itself is arbitrary.

    // Any error must take priority over SUCCESS.
    SUCCESS                 ( 0, 0x0000, "SUCCESS",                 "Success",                  "Ok",                    0, EventStatus.SUCCESS),

    TIMEOUT                 ( 1, 0x0001, "TIMEOUT",                 "Timeout",                  "Timeout",              14, EventStatus.TIMEOUT),

    // The user expects to see this event, even if other errors have occurred
    ABORT                   ( 2, 0x0002, "ABORT",                   "Abort",                    "Abort",                15, EventStatus.ABORT),

    PREMATURE_TERMINATION   ( 3, 0x0004, "PREMATURE_TERMINATION",   "Premature Termination",    "Failed",               13, EventStatus.FATAL),

    // User Input or Configuration error
    INVALID_PARAMETER       ( 4, 0x0008, "INVALID_PARAMETER",       "Invalid Parameter",        "Invalid Parameter",     8, EventStatus.QUESTION),

    // Communication errors have lower priority than configuration errors
    INVALID_MESSAGE         ( 5, 0x0010, "INVALID_MESSAGE",         "Invalid Message",          "Invalid Message",       7, EventStatus.QUESTION),

    // User Input or Configuration errors
    INVALID_COMMAND         ( 6, 0x0020, "INVALID_COMMAND",         "Invalid Command",          "Invalid Command",       9, EventStatus.QUESTION),
    INVALID_MODULE          ( 7, 0x0040, "INVALID_MODULE",          "Invalid Module",           "Invalid Module",       11, EventStatus.QUESTION),
    INVALID_INSTRUMENT      ( 8, 0x0080, "INVALID_INSTRUMENT",      "Invalid Instrument",       "Invalid Instrument",   12, EventStatus.QUESTION),

    // Communication errors have lower priority than configuration errors
    MODULE_DATABUS          ( 9, 0x0100, "MODULE_DATABUS",          "Module Databus",           "Module Databus",        6, EventStatus.FATAL),
    CRC_ERROR               (10, 0x0200, "CRC_ERROR",               "CRC Error",                "CRC Error",             5, EventStatus.WARNING),

    // A fundamental configuration error, so nothing will work
    INVALID_XML             (11, 0x0400, "INVALID_XML",             "Invalid XML",              "Invalid XML",          16, EventStatus.FATAL),

    // Should not occur in this implementation
    ERROR_12                (12, 0x0800, "ERROR_12",                "Error 12",                 "Unknown Error",         0, EventStatus.FATAL),

    // Should not occur in this implementation
    ERROR_13                (13, 0x1000, "ERROR_13",                "Error 13",                 "Unknown Error",         0, EventStatus.FATAL),

    // These states are not errors, so any true errors have a higher priority EventStatus
    LOOK_AT_ME              (14, 0x2000, "LOOK_AT_ME",              "Look At Me",               "Look At Me",            3, EventStatus.QUESTION),
    BUSY                    (15, 0x4000, "BUSY",                    "Busy",                     "Busy",                  1, EventStatus.WARNING),
    CAPTURE_ACTIVE          (16, 0x8000, "CAPTURE_ACTIVE",          "Capture Active",           "Capture Active",        2, EventStatus.WARNING);


    public static final String RESPONSE_NODATA = "NODATA";
    public static final String RESPONSE_NO_DATA_HTML = "<html><font color=red>No Data</font></html>";

    private static final int MASK_SUCCESS = 0x3fff;

    private final int intIndex;
    private final int intBitMask;
    private final String strMnemonic;
    private final String strName;
    private final String strResponseValue;
    private final int intEventStatusPriority;
    private final EventStatus eventStatus;


    /***********************************************************************************************
     * A convenience method to create a container for ResponseMessageStatus.
     * The add() method appends the specified ResponseMessageStatus element
     * to the end of this list, but only if it is not already present in the Collection.
     *
     * @return ResponseMessageStatusList
     */

    public static ResponseMessageStatusList createResponseMessageStatusList()
        {
        // Allow for all possible status entries!
        return (new ResponseMessageStatusList(ResponseMessageStatus.values().length));
        }


    /***********************************************************************************************
     * Test to see if the ResponseMessageStatus List is valid to allow another command,
     * i.e contains only SUCCESS or BUSY or CAPTURE_ACTIVE. All other states are errors.
     *
     * @param responsestatuslist
     */

    public static boolean wasResponseListSuccessful(final ResponseMessageStatusList responsestatuslist)
        {
        return (wasResponseStatusSuccessful(convertResponseStatusCodesToBits(responsestatuslist)));
        }


    /***********************************************************************************************
     * Test to see if the ResponseMessageStatus status bits are valid to allow another command,
     * i.e indicates SUCCESS or BUSY or CAPTURE_ACTIVE. All other states are errors.
     *
     * @param responsestatusbits
     *
     * @return boolean
     */

    public static boolean wasResponseStatusSuccessful(final int responsestatusbits)
        {
        final int intMaskBits;
        final boolean boolSuccess;

        // Allow only SUCCESS, BUSY and CAPTURE_ACTIVE to represent a success
        // so mask off the top two bits only; all other bits are errors
        intMaskBits = MASK_SUCCESS;
        boolSuccess = ((responsestatusbits & intMaskBits) == 0);

        return (boolSuccess);
        }


    /***********************************************************************************************
     * Test to see if the ResponseMessageStatus is valid to allow another command,
     * i.e SUCCESS or BUSY or CAPTURE_ACTIVE. All other states are errors.
     * Use this to test status for those commands which should not fail if BUSY or CAPTURE_ACTIVE.
     * e.g. IteratedDataCommand.
     *
     * @param response
     *
     * @return boolean
     */

    public static boolean wasResponseSuccessful(final ResponseMessageInterface response)
        {
        final boolean boolSuccess;

        if (response != null)
            {
            final int intMaskBits;

            // Make the assumption that if these states are present then the errors are not!
            // Allow only SUCCESS, BUSY and CAPTURE_ACTIVE to represent a success
            // so mask off the top two bits only; all other bits are errors
            intMaskBits = MASK_SUCCESS;
            boolSuccess = ((response.getStatusBits() & intMaskBits) == 0);
            }
        else
            {
            boolSuccess = false;
            }

        return (boolSuccess);
        }


    /***********************************************************************************************
     * Indicate if the ResponseMessageStatusList does NOT contain any error codes.
     * The List may be NULL or empty.
     *
     * @param list
     *
     * @return boolean
     */

    public static boolean isResponseStatusOk(final ResponseMessageStatusList list)
        {
        return ((list != null)
               && (!list.contains(TIMEOUT))
               && (!list.contains(ABORT))
               && (!list.contains(PREMATURE_TERMINATION))
               && (!list.contains(INVALID_PARAMETER))
               && (!list.contains(INVALID_MESSAGE))
               && (!list.contains(INVALID_COMMAND))
               && (!list.contains(INVALID_MODULE))
               && (!list.contains(INVALID_INSTRUMENT))
               && (!list.contains(MODULE_DATABUS))
               && (!list.contains(CRC_ERROR))
               && (!list.contains(INVALID_XML))
               && (!list.contains(ERROR_12))
               && (!list.contains(ERROR_13)));
        }


    /***********************************************************************************************
     * Remove the specified ResponseMessageStatus from the specified List.
     *
     * @param statuslist
     * @param status
     *
     * @return ResponseMessageStatusList
     */

    public static void removeResponseMessageStatus(final ResponseMessageStatusList statuslist,
                                                   final ResponseMessageStatus status)
        {
        if ((statuslist != null)
            && (status != null)
               & (statuslist.contains(status)))
            {
            statuslist.remove(status);
            }
        }


    /***********************************************************************************************
     * Convert the bit masks of the ResponseStatus codes into a single integer.
     *
     * @param statuslist
     *
     * @return int
     */

    public static int convertResponseStatusCodesToBits(final ResponseMessageStatusList statuslist)
        {
        int intAccumulator;
        final Iterator<ResponseMessageStatus> iterStatusList;

        intAccumulator = 0;

        if (statuslist != null)
            {
            iterStatusList = statuslist.iterator();

            while (iterStatusList.hasNext())
                {
                final ResponseMessageStatus status;

                status = iterStatusList.next();
                intAccumulator |= status.getBitMask();
                }
            }

        return (intAccumulator);
        }


    /***********************************************************************************************
     * Test to see if the ResponseMessageStatus is an INVALID_MODULE.
     * Use this to test status for those commands which should not fail if the Module
     * hardware is just missing, e.g. getConfiguration().
     *
     * @param response
     *
     * @return boolean
     */

    public static boolean wasResponseInvalidModule(final ResponseMessageInterface response)
        {
        final boolean boolWasInvalidModule;

        if (response != null)
            {
            boolWasInvalidModule = ((response.getStatusBits() & INVALID_MODULE.getBitMask()) != 0);
            }
        else
            {
            boolWasInvalidModule = false;
            }

        return (boolWasInvalidModule);
        }


    /***********************************************************************************************
     * A convenience method to indicate if the Instrument should just stop execution now!
     *
     * @param statuslist
     *
     * @return boolean
     */

    public static boolean isToStopNow(final ResponseMessageStatusList statuslist)
        {
        return ((statuslist != null)
                && (!statuslist.isEmpty())
                && ((statuslist.contains(ABORT))
                    || (statuslist.contains(TIMEOUT)
                    || (statuslist.contains(PREMATURE_TERMINATION)))));
        }


    /***********************************************************************************************
     * Expand the List of ResponseMessageStatus codes into their mnemonics for display.
     *
     * @param statuslist
     *
     * @return String
     */

    public static String expandResponseStatusCodes(final ResponseMessageStatusList statuslist)
        {
        final StringBuffer buffer;
        final Iterator<ResponseMessageStatus> iterStatusList;

        buffer = new StringBuffer();

        if (statuslist != null)
            {
            Collections.sort(statuslist, new ResponseMessageStatusByIndex());
            iterStatusList = statuslist.iterator();

            while (iterStatusList.hasNext())
                {
                final ResponseMessageStatus status;

                status = iterStatusList.next();
                buffer.append(status.getMnemonic());
                buffer.append(", ");
                }

            // Prune off the last ', '
            if (buffer.length() > 0)
                {
                buffer.setLength(buffer.length() - 2);
                }
            }

        return (buffer.toString());
        }


    /***********************************************************************************************
     * A convenience method to show the ResponseMessageStatus word as a sequence of bits.
     *
     * @param statusword
     *
     * @return String
     */

    public static String accumulateResponseStatusBits(final int statusword)
        {
        return (Utilities.intToBitString(statusword));
        }


    /***********************************************************************************************
     * Show the contents of the various containers of ResponseMessageStatus, if possible.
     *
     * @param response
     * @param dao
     * @param statuslist
     * @param title
     * @param debug
     */

    public static void showResponseMessageStatus(final ResponseMessageInterface response,
                                                 final ObservatoryInstrumentDAOInterface dao,
                                                 final ResponseMessageStatusList statuslist,
                                                 final String title,
                                                 final boolean debug)
        {
        FrameworkSingletons.LOGGER.debug(debug, title);

        if (response != null)
            {
            FrameworkSingletons.LOGGER.debug(debug,
                                             "    [status.list.response=" + expandResponseStatusCodes(response.getResponseMessageStatusList()) + "]");
            }
        else
            {
            FrameworkSingletons.LOGGER.debug(debug,
                                             "    [status.list.response=null]");
            }

        if (dao != null)
            {
            FrameworkSingletons.LOGGER.debug(debug,
                                             "    [status.list.dao=" + expandResponseStatusCodes(dao.getResponseMessageStatusList()) + "]");
            }
        else
            {
            FrameworkSingletons.LOGGER.debug(debug,
                                             "    [status.list.dao=null]");
            }

        if (dao != null)
            {
            FrameworkSingletons.LOGGER.debug(debug,
                                             "    [status.list.local=" + expandResponseStatusCodes(statuslist) + "]\n");
            }
        else
            {
            FrameworkSingletons.LOGGER.debug(debug,
                                             "    [status.list.local=null]\n");
            }
        }


    /***********************************************************************************************
     * Construct a ResponseMessageStatus.
     *
     * @param index
     * @param bitmask
     * @param mnemonic
     * @param name
     * @param responsevalue
     * @param priority
     * @param status
     */

    private ResponseMessageStatus(final int index,
                                  final int bitmask,
                                  final String mnemonic,
                                  final String name,
                                  final String responsevalue,
                                  final int priority,
                                  final EventStatus status)
        {
        intIndex = index;
        intBitMask = bitmask;
        strMnemonic = mnemonic;
        strName = name;
        strResponseValue = responsevalue;
        intEventStatusPriority = priority;
        eventStatus = status;
        }


    /***********************************************************************************************
     * Get the index.
     *
     * @return int
     */

    public int getIndex()
        {
        return (this.intIndex);
        }


    /***********************************************************************************************
     * Get the bit mask 0x0000 (Success) to 0x8000 (Error 16).
     *
     * @return int
     */

    public int getBitMask()
        {
        return (this.intBitMask);
        }


    /***********************************************************************************************
     * Get the mnemonic.
     *
     * @return String
     */

    public String getMnemonic()
        {
        return(this.strMnemonic);
        }


    /***********************************************************************************************
     * Get the name.
     *
     * @return String
     */

    public String getName()
        {
        return(this.strName);
        }


    /***********************************************************************************************
     * Get the ResponseValue.
     *
     * @return String
     */

    public String getResponseValue()
        {
        return(this.strResponseValue);
        }


    /***********************************************************************************************
     * Get the EventStatusPriority (0 is lowest).
     *
     * @return int
     */

    public int getEventStatusPriority()
        {
        return (this.intEventStatusPriority);
        }


    /***********************************************************************************************
     * Get the EventStatus corresponding to this ResponseMessageStatus.
     *
     * @return EventStatus
     */

    public EventStatus getEventStatus()
        {
        return(this.eventStatus);
        }


    /***********************************************************************************************
     * Get the ResponseMessageStatus as a String.
     *
     * @return String
     */

    public String toString()
        {
        return (this.strName);
        }
    }
