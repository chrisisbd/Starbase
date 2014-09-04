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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.commands.capture;

import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.frameworks.starbase.plugins.observatory.common.ObservatoryConstants;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.ResponseMessageHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.dao.StaribusCoreHostMemoryInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageStatus;
import org.lmn.fc.model.datatypes.DecimalFormatPattern;
import org.lmn.fc.model.xmlbeans.instruments.CommandType;

import java.io.File;


/***************************************************************************************************
 * GetSpace.
 */

public final class GetSpace implements FrameworkConstants,
                                       FrameworkStrings,
                                       FrameworkMetadata,
                                       FrameworkSingletons,
                                       ObservatoryConstants
    {
    /***********************************************************************************************
     * doGetSpace().
     *
     * @param dao
     * @param commandmessage
     * @param filesize
     *
     * @return ResponseMessageInterface
     */

    public static ResponseMessageInterface doGetSpace(final StaribusCoreHostMemoryInterface dao,
                                                      final CommandMessageInterface commandmessage,
                                                      final long filesize)
        {
        final String SOURCE = "GetSpace.doGetSpace() ";
        final CommandType cmdGetSpace;
        final String strResponseValue;
        final ResponseMessageInterface responseMessage;
        File fileHostMemory;

        // Don't affect the CommandType of the incoming Command
        cmdGetSpace = (CommandType)commandmessage.getCommandType().copy();

        // Find out the name of the HostMemory file for this Instrument
        fileHostMemory = new File(Capture.buildHostMemoryFilename(dao.getHostInstrument(),
                                                                  StaribusCoreHostMemoryInterface.TIMESTAMPED_HOSTMEMORYFILE));

        // We don't mind about CAPTURE_ACTIVE, but there must be a file to measure
        if (fileHostMemory.exists())
            {
            final double dblSpacePercentage;

            // How many bytes have been used as a percentage of the specified filesize?
            dblSpacePercentage = 100 - (100 * ((double)fileHostMemory.length() / filesize));

            if ((dblSpacePercentage >= 0.0)
                && (dblSpacePercentage <= 100.0))
                {
                strResponseValue = DecimalFormatPattern.PERCENTAGE.format(dblSpacePercentage);
                dao.getResponseMessageStatusList().add(ResponseMessageStatus.SUCCESS);

                if (dao.isCaptureMode())
                    {
                    // We don't mind about CAPTURE_ACTIVE, but remind the User anyway
                    dao.getResponseMessageStatusList().add(ResponseMessageStatus.CAPTURE_ACTIVE);
                    }
                }
            else
                {
                // We must have overrun? This should not be possible....
                strResponseValue = ResponseMessageStatus.INVALID_PARAMETER.getResponseValue();
                dao.getResponseMessageStatusList().add(ResponseMessageStatus.INVALID_PARAMETER);
                }
            }
        else
            {
            // If there is no current file, then the space available is theoretically 100%
            strResponseValue = StaribusCoreHostMemoryInterface.MSG_HOST_MEMORY_FILE_NOT_FOUND;
            dao.getResponseMessageStatusList().add(ResponseMessageStatus.PREMATURE_TERMINATION);
            }

        // Dispose the File
        fileHostMemory = null;

        responseMessage = ResponseMessageHelper.createResponseMessage(dao,
                                                                      commandmessage,
                                                                      cmdGetSpace,
                                                                      null,
                                                                      null,
                                                                      strResponseValue
        );
        return (responseMessage);
        }
    }
