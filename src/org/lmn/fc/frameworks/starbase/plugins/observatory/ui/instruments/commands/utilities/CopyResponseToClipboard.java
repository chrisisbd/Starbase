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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.commands.utilities;

import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.frameworks.starbase.plugins.observatory.common.ObservatoryConstants;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.ResponseMessageHelper;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageInterface;
import org.lmn.fc.model.xmlbeans.instruments.CommandType;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;


/***************************************************************************************************
 * CopyResponseToClipboard.
 */

public final class CopyResponseToClipboard implements FrameworkConstants,
                                                      FrameworkStrings,
                                                      FrameworkMetadata,
                                                      FrameworkSingletons,
                                                      ObservatoryConstants
    {
    // String Resources
    private static final String CLIPBOARD_DEFAULT_NAME = "Starbase Local Clipboard";
    private static final String CLIPBOARD_NO_DATA = "No Data";

    /***********************************************************************************************
     * copyResponseToClipboard().
     *
     * @param dao
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */


    public static ResponseMessageInterface doCopyResponseToClipboard(final ObservatoryInstrumentDAOInterface dao,
                                                                     final CommandMessageInterface commandmessage)
        {
        final CommandType commandType;
        final ResponseMessageInterface responseMessage;
        Clipboard clipboard;
        final StringSelection responseValue;

        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               "AbstractObservatoryInstrumentDAO.copyResponseToClipboard() LOCAL COMMAND");

        // Don't affect the CommandType of the incoming Command
        commandType = (CommandType)commandmessage.getCommandType().copy();

        // Set up the clipboard
        try
            {
            clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            }

        catch (HeadlessException exception)
            {
            LOGGER.error("System clipboard access denied, using local clipboard");
            clipboard = new Clipboard(CLIPBOARD_DEFAULT_NAME);
            }

        if ((dao.getWrappedData() != null)
            && (dao.getWrappedData().getResponseValue() != null)
            && (!EMPTY_STRING.equals(dao.getWrappedData().getResponseValue())))
            {
            responseValue = new StringSelection(dao.getWrappedData().getResponseValue());
            clipboard.setContents(responseValue, dao);
            }
        else
            {
            responseValue = new StringSelection(CLIPBOARD_NO_DATA);
            clipboard.setContents(responseValue, dao);
            }

        // Create the ResponseMessage
        // The getResponseToClipboard() operation normally just requires an Ack, i.e. no Response
        responseMessage = ResponseMessageHelper.constructSuccessfulResponse(dao,
                                                                            commandmessage,
                                                                            commandType);
        return (responseMessage);
        }
    }
