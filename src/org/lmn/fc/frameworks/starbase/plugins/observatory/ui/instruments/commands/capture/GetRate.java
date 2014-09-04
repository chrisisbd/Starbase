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


/***************************************************************************************************
 * GetRate.
 */

public final class GetRate implements FrameworkConstants,
                                      FrameworkStrings,
                                      FrameworkMetadata,
                                      FrameworkSingletons,
                                      ObservatoryConstants
    {
    /***********************************************************************************************
     * doGetRate().
     *
     * @param dao
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public static ResponseMessageInterface doGetRate(final StaribusCoreHostMemoryInterface dao,
                                                     final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "GetRate.doGetRate() ";
        final CommandType cmdGetRate;
        final String strResponseValue;
        final ResponseMessageInterface responseMessage;

        // Don't affect the CommandType of the incoming Command
        cmdGetRate = (CommandType)commandmessage.getCommandType().copy();

        // There must always be a valid Rate
        strResponseValue = DecimalFormatPattern.DECIMAL_INTEGER.format(dao.getSampleRate());
        dao.getResponseMessageStatusList().add(ResponseMessageStatus.SUCCESS);

        if (dao.isCaptureMode())
            {
            // We don't mind about CAPTURE_ACTIVE, but remind the User anyway
            dao.getResponseMessageStatusList().add(ResponseMessageStatus.CAPTURE_ACTIVE);
            }

        responseMessage = ResponseMessageHelper.createResponseMessage(dao,
                                                                      commandmessage,
                                                                      cmdGetRate,
                                                                      null,
                                                                      null,
                                                                      strResponseValue);
        return (responseMessage);
        }
    }
