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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.commands.news;


import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.common.utilities.threads.SwingWorker;
import org.lmn.fc.frameworks.starbase.plugins.observatory.common.ObservatoryConstants;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.ResponseMessageHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.RssHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.logs.SimpleEventLogUIComponent;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageInterface;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.xmlbeans.instruments.CommandType;


/***************************************************************************************************
 * GetMantisIssues.
 */

public final class GetMantisIssues implements FrameworkConstants,
                                              FrameworkStrings,
                                              FrameworkMetadata,
                                              FrameworkSingletons,
                                              ObservatoryConstants
    {
    public static final String RSS_NAME = "UKRAA-Starbase-Mantis";
    // See http://www.futureware.biz/blog/index.php?title=rss_feeds_in_mantis_1_0_0a3&more=1&c=1&tb=1&pb=1
    // Starbase is Project ID = 1
    public static final String RSS_URL = "http://www.ukraa.com/bt/issues_rss.php?project_id=1";


    /***********************************************************************************************
     * doGetMantisIssues().
     *
     * @param dao
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public static ResponseMessageInterface doGetMantisIssues(final ObservatoryInstrumentDAOInterface dao,
                                                             final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "GetMantisIssues.doGetMantisIssues() ";
        final CommandType commandType;
        final ResponseMessageInterface responseMessage;

        // Don't affect the CommandType of the incoming Command
        commandType = (CommandType)commandmessage.getCommandType().copy();

        if ((dao != null)
            && (dao.getHostInstrument() != null)
            && (dao.getHostInstrument().getInstrument() != null))
            {
            final SwingWorker workerRSS;

            // We need a SwingWorker otherwise this would execute on the PortController Thread,
            // which would block while waiting for a Response...
            workerRSS = RssHelper.createRssThread(dao, commandmessage, RSS_NAME, RSS_URL, true, SOURCE);

            // Start the Thread we have prepared...
            workerRSS.start();

            SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                               EventStatus.INFO,
                                               METADATA_TARGET
                                                   + "Mantis" + TERMINATOR
                                                   + " [action=getissues]",
                                               SOURCE,
                                               dao.getObservatoryClock());
            }

        // Create the ResponseMessage
        responseMessage = ResponseMessageHelper.constructSuccessfulResponse(dao,
                                                                            commandmessage,
                                                                            commandType);
        return (responseMessage);
        }
    }
