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

import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;
import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.common.utilities.misc.Utilities;
import org.lmn.fc.common.utilities.threads.SwingWorker;
import org.lmn.fc.frameworks.starbase.plugins.observatory.common.ObservatoryConstants;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.DAOWrapperInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.DAOWrapper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.InstrumentHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.ResponseMessageHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.TimeoutHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.communicator.CommunicatorDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.logs.LogHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.logs.SimpleEventLogUIComponent;
import org.lmn.fc.frameworks.starbase.portcontroller.AbstractResponseMessage;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageStatus;
import org.lmn.fc.model.xmlbeans.datatypes.SchemaDataType;
import org.lmn.fc.model.xmlbeans.instruments.CommandType;
import org.lmn.fc.model.xmlbeans.instruments.ParameterType;
import org.lmn.fc.model.xmlbeans.subscriptions.RssSubscription;
import org.lmn.fc.model.xmlbeans.subscriptions.TwitterSubscription;
import winterwell.jtwitter.Twitter;
import winterwell.jtwitter.TwitterException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Vector;

import static org.lmn.fc.model.logging.EventStatus.INFO;
import static org.lmn.fc.model.logging.EventStatus.WARNING;


/***************************************************************************************************
 * RunNewsreader.
 */

public final class RunNewsreader implements FrameworkConstants,
                                            FrameworkStrings,
                                            FrameworkMetadata,
                                            FrameworkSingletons,
                                            ObservatoryConstants
    {
    private static final String MSG_ALREADY_STOPPED = "newsreader is already stopped";
    private static final String MSG_ALREADY_RUNNING = "newsreader is already running";
    private static final String MSG_RSS_FAILED = "Failed to read RSS Subscriptions";
    private static final String MSG_NEWS_FAILED = "Failed to read News Subscriptions";
    private static final String NEWLINE = "<br>";
    private static final String LINE = "<hr>";

    // The Thread for handling Communicator
    private static SwingWorker workerCommunicator;

    private static boolean boolCommunicatorRunning;


    /***********************************************************************************************
     * Run the Newsreader on a separate Thread.
     *
     * @param dao
     * @param commandmessage
     * @param updatemillis
     *
     * @return ResponseMessageInterface
     */

    public static ResponseMessageInterface doRunNewsreader(final ObservatoryInstrumentDAOInterface dao,
                                                           final CommandMessageInterface commandmessage,
                                                           final long updatemillis)
        {
        final String SOURCE = "RunNewsreader.doRunNewsreader()";
        final int PARAMETER_COUNT = 1;
        final CommandType commandType;
        final List<ParameterType> listParameters;
        ResponseMessageInterface responseMessage;
        final Vector<Vector> vecDaoLogFragment;

        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               SOURCE);

        // Get the latest Resources
        dao.readResources();

        // Don't affect the CommandType of the incoming Command
        commandType = (CommandType)commandmessage.getCommandType().copy();

        // Clear the DAO's Logs for this run
        dao.getEventLogFragment().clear();
        dao.getInstrumentLogFragment().clear();

        // We expect one parameter, a control boolean
        listParameters = commandType.getParameterList();
        responseMessage = null;

        vecDaoLogFragment = commandmessage.getDAO().getEventLogFragment();

        // Create a new Communicator SwingWorker, or stop the existing
        if ((listParameters != null)
            && (listParameters.size() == PARAMETER_COUNT)
            && (listParameters.get(0) != null)
            && (SchemaDataType.BOOLEAN.equals(listParameters.get(0).getInputDataType().getDataTypeName()))
            && (dao.getHostInstrument() != null)
            && (dao instanceof CommunicatorDAOInterface)
            && (commandmessage.getDAO() != null))
            {
            final boolean boolRunRequested;

            // This should never throw NumberFormatException, because it has already been parsed
            boolRunRequested = Boolean.parseBoolean(listParameters.get(0).getValue());

            if ((!isRunning())
                && (boolRunRequested))
                {
                // We've been asked to Run a stopped Communicator Newsreader
                // We need a SwingWorker otherwise this would execute on the PortController Thread,
                // which would block while waiting for a Response...

                setCommunicatorWorker(new SwingWorker(REGISTRY.getThreadGroup(), SOURCE)
                    {
                    /*******************************************************************************
                     * Run the Communicator Newsreader
                     *
                     * @return Object
                     */

                    public Object construct()
                        {
                        final DAOWrapperInterface daoWrapper;

                        daoWrapper = new DAOWrapper(commandmessage,
                                                    null,
                                                    ResponseMessageStatus.SUCCESS.getResponseValue(),
                                                    dao);

                        while (isRunning()
                               && Utilities.workerCanProceed(dao, this))
                            {
                            final Vector<Object> vecNewsRawData;
                            final String strNews;

                            // Update all Resources each time round
                            dao.readResources();

                            // We musn't timeout, since this might run forever...
                            TimeoutHelper.restartDAOTimeoutTimerInfinite(dao);

                            // The News is the Raw Data
                            vecNewsRawData = new Vector<Object>(1);

                            strNews = getNews((CommunicatorDAOInterface)dao);
                            vecNewsRawData.add(strNews);

                            // Pass the News data to the DAO DAOWrapper
                            daoWrapper.setRawData(vecNewsRawData);
                            dao.setWrappedData(daoWrapper);

                            // Keep re-applying the updated DAO Wrapper to the host Instrument,
                            // to ensure that News Reports get updated
                            // Only refresh the data if visible
                            dao.getHostInstrument().setWrappedData(dao.getWrappedData(), false, false);

                            Utilities.safeSleepPollWorker(updatemillis, dao, this);
                            }

                        return (null);
                        }


                    /***********************************************************************************
                     * When the Thread stops.
                     */

                    public void finished()
                        {
                        setRunning(false);

                        // Put the Timeout back to what it should be for a single default command
                        TimeoutHelper.resetDAOTimeoutTimerFromRegistryDefault(dao);
                        }
                    });

                // Start the Thread we have prepared...
                setRunning(true);
                getCommunicatorWorker().start();

                SimpleEventLogUIComponent.logEvent(vecDaoLogFragment,
                                                   INFO,
                                                   METADATA_TARGET_COMMUNICATOR
                                                        + METADATA_ACTION_START,
                                                   SOURCE,
                                                   dao.getObservatoryClock());
                LogHelper.updateEventLogFragment(dao);

                // Create the SUCCESS ResponseMessage
                commandType.getResponse().setValue(ResponseMessageStatus.SUCCESS.getResponseValue());

                responseMessage = ResponseMessageHelper.constructSuccessfulResponse(dao,
                                                                                    commandmessage,
                                                                                    commandType);
                }
            else if ((isRunning())
                    && (boolRunRequested))
                {
                // We've been asked to Run a running Communicator, which must fail, with no action taken
                SimpleEventLogUIComponent.logEvent(vecDaoLogFragment,
                                                   WARNING,
                                                   METADATA_TARGET_COMMUNICATOR
                                                           + METADATA_ACTION_START
                                                           + METADATA_MESSAGE + MSG_ALREADY_RUNNING + TERMINATOR,
                                                   SOURCE,
                                                   dao.getObservatoryClock());
                LogHelper.updateEventLogFragment(dao);
                }
            else if ((isRunning())
                    && (!boolRunRequested))
                {
                // We've been asked to Stop a running Communicator
                // Put the Timeout back to what it should be for a single default command
                TimeoutHelper.resetDAOTimeoutTimerFromRegistryDefault(dao);

                // We must try to stop the Communicator
                SwingWorker.disposeWorker(getCommunicatorWorker(), true, SWING_WORKER_STOP_DELAY);
                setCommunicatorWorker(null);
                setRunning(false);

                SimpleEventLogUIComponent.logEvent(vecDaoLogFragment,
                                                   INFO,
                                                   METADATA_TARGET_COMMUNICATOR
                                                        + METADATA_ACTION_STOP,
                                                   SOURCE,
                                                   dao.getObservatoryClock());
                LogHelper.updateEventLogFragment(dao);

                // Create the SUCCESS ResponseMessage
                commandType.getResponse().setValue(ResponseMessageStatus.SUCCESS.getResponseValue());

                responseMessage = ResponseMessageHelper.constructSuccessfulResponse(dao,
                                                                                    commandmessage,
                                                                                    commandType);
                }
            else if ((!isRunning())
                    && (!boolRunRequested))
                {
                // We've been asked to Stop a stopped Communicator, which must fail, with no action taken
                SimpleEventLogUIComponent.logEvent(vecDaoLogFragment,
                                                   WARNING,
                                                   METADATA_TARGET_COMMUNICATOR
                                                           + METADATA_ACTION_STOP
                                                           + METADATA_MESSAGE + MSG_ALREADY_STOPPED + TERMINATOR,
                                                   SOURCE,
                                                   dao.getObservatoryClock());
                LogHelper.updateEventLogFragment(dao);
                }
            }

        // Did we still fail?
        if (responseMessage == null)
            {
            // Invalid Parameters
            // Create the ResponseMessage
            dao.getResponseMessageStatusList().add(ResponseMessageStatus.INVALID_PARAMETER);
            responseMessage = ResponseMessageHelper.constructEmptyResponse(dao,
                                                                           commandmessage,
                                                                           commandmessage.getInstrument(),
                                                                           commandmessage.getModule(),
                                                                           commandType,
                                                                           AbstractResponseMessage.buildResponseResourceKey(commandmessage.getInstrument(),
                                                                                                                            commandmessage.getModule(),
                                                                                                                            commandType));
            }

        // Something has changed, we may need to refresh a browser etc.
        InstrumentHelper.notifyInstrumentChanged(dao.getHostInstrument());

        return (responseMessage);
        }


    /***********************************************************************************************
     * Get the News from the DAO.
     *
     * @param dao
     *
     * @return String
     */

    private static String getNews(final CommunicatorDAOInterface dao)
        {
        final String SOURCE = "RunNewsreader.getNews() ";

        StringBuffer buffer;

        buffer = new StringBuffer("No Data");

        //--------------------------------------------------------------------------------------
        // Process Twitter

        if ((dao != null)
            && (dao.getSubscriptionsDoc() != null)
            && (dao.getSubscriptionsDoc().getSubscriptions() != null)
            && (dao.getSubscriptionsDoc().getSubscriptions().getTwitterList() != null))
            {
            final winterwell.jtwitter.Twitter twitter;

            buffer = new StringBuffer();

            // Log in as ukraastarbase
            twitter = new Twitter(UKRAA_USERNAME, UKRAA_PASSWORD);

            buffer.append("Last Updated: ");
            buffer.append(dao.getObservatoryClock().getDateTimeNowAsString());
            buffer.append(" UT");
            buffer.append(NEWLINE);
            buffer.append(NEWLINE);

            for (int i = 0;
                 i < dao.getSubscriptionsDoc().getSubscriptions().getTwitterList().size();
                 i++)
                {
                try
                    {
                    final TwitterSubscription subscription;
                    subscription = dao.getSubscriptionsDoc().getSubscriptions().getTwitterList().get(i);

                    if (subscription != null)
                        {
                        final Twitter.Status status;

                        status = twitter.getStatus(subscription.getUsername());

                        // Output only if there's no exception
                        buffer.append("Twitter ");
                        buffer.append(subscription.getUsername());
                        buffer.append(": ");
                        buffer.append(status);
                        buffer.append(NEWLINE);
                        }
                    }

                catch (TwitterException exception)
                    {
                    // TwitterException can be ignored
                    LOGGER.error("Twitter " + exception.getMessage());
                    }
                }

            // Did we get any Twitter?
            if (buffer.indexOf("Twitter") > 0)
                {
                buffer.append(LINE);
                }
            }

        //--------------------------------------------------------------------------------------
        // Process RSS

        if ((dao != null)
            && (dao.getSubscriptionsDoc() != null)
            && (dao.getSubscriptionsDoc().getSubscriptions() != null)
            && (dao.getSubscriptionsDoc().getSubscriptions().getRSSList() != null))
            {
            for (int intSubscriptionIndex = 0;
                 intSubscriptionIndex < dao.getSubscriptionsDoc().getSubscriptions().getRSSList().size();
                 intSubscriptionIndex++)
                {
                try
                    {
                    final RssSubscription subscription;
                    final int intDetailCount;

                    subscription = dao.getSubscriptionsDoc().getSubscriptions().getRSSList().get(intSubscriptionIndex);

                    if (subscription != null)
                        {
                        final URL feedUrl;
                        final SyndFeed feed;
                        final List<SyndEntry> listEntries;
                        final SyndFeedInput input;

                        LOGGER.logTimedEvent(SOURCE + "Read Subscription [index=" + intSubscriptionIndex
                                                    + "] [url=" + subscription.getURL() + "]");

                        feedUrl = new URL(subscription.getURL());
                        input = new SyndFeedInput();
                        feed = input.build(new XmlReader(feedUrl));
                        listEntries = feed.getEntries();

                        // Output only if there's no exception
                        if ((feed.getTitle() != null)
                            && (!EMPTY_STRING.equals(feed.getTitle())))
                            {
                            buffer.append(NEWLINE);
                            buffer.append("RSS Feed: ");
                            buffer.append(feed.getTitle());
                            buffer.append(NEWLINE);
                            buffer.append(LINE);
                            }

                        if (subscription.getShowDetail())
                            {
                            intDetailCount = listEntries.size();
                            }
                        else
                            {
                            // Show only the first entry
                            intDetailCount = 1;
                            }

                        for (int j = 0;
                             j < intDetailCount;
                             j++)
                            {
                            final SyndEntry entry;
                            final List<SyndContent> listContents;

                            entry = listEntries.get(j);

                            if (entry != null)
                                {
                                if ((entry.getTitle() != null)
                                    && (!EMPTY_STRING.equals(entry.getTitle())))
                                    {
                                    buffer.append("<b>");
                                    buffer.append(entry.getTitle());
                                    buffer.append("</b>");
                                    buffer.append(NEWLINE);
                                    }

                                if ((entry.getDescription() != null)
                                    && (entry.getDescription().getValue() != null)
                                    && (!FrameworkStrings.EMPTY_STRING.equals(entry.getDescription().getValue())))
                                    {
                                    buffer.append(entry.getDescription().getValue());
                                    buffer.append(NEWLINE);
                                    }

                                if ((entry.getLink() != null)
                                    && (!FrameworkStrings.EMPTY_STRING.equals(entry.getLink())))
                                    {
                                    buffer.append(entry.getLink());
                                    buffer.append(NEWLINE);
                                    }

                                if ((entry.getAuthor() != null)
                                    && (!EMPTY_STRING.equals(entry.getAuthor())))
                                    {
                                    buffer.append("Author: ");
                                    buffer.append(entry.getAuthor());
                                    buffer.append(NEWLINE);
                                    }

                                if (entry.getPublishedDate() != null)
                                    {
                                    buffer.append(entry.getPublishedDate());
                                    buffer.append(NEWLINE);
                                    }

                                listContents = entry.getContents();

                                if ((listContents != null)
                                    && (listContents.size() > 0)
                                    && (listContents.get(0) != null))
                                    {
                                    buffer.append(listContents.get(0).getValue());
                                    buffer.append(NEWLINE);

//                                    for (int k = 0;
//                                         k < listContents.size();
//                                         k++)
//                                        {
//                                        SyndContent content = listContents.get(k);
//
//                                        if (content != null)
//                                            {
//                                            System.out.println("Syndicated Content [index=" + k + "] [" + content.getValue() + "]");
//                                            }
//                                        }
                                    }

                                buffer.append(LINE);
                                }
                            }
                        }
                    }

                catch (MalformedURLException exception)
                    {
                    buffer.append(NEWLINE);
                    buffer.append(MSG_RSS_FAILED + SPACE + exception.getMessage());
                    buffer.append(LINE);

                    LOGGER.error(SOURCE + MSG_RSS_FAILED + " MalformedURLException " + exception.getMessage());
                    }

                catch (FeedException exception)
                    {
                    buffer.append(NEWLINE);
                    buffer.append(MSG_RSS_FAILED + SPACE + exception.getMessage());
                    buffer.append(LINE);

                    LOGGER.error(SOURCE + MSG_RSS_FAILED + " FeedException " + exception.getMessage());
                    }

                catch (IllegalArgumentException exception)
                    {
                    buffer.append(NEWLINE);
                    buffer.append(MSG_RSS_FAILED + SPACE + exception.getMessage());
                    buffer.append(LINE);

                    LOGGER.error(SOURCE + MSG_RSS_FAILED + " IllegalArgumentException " + exception.getMessage());
                    }

                catch (IOException exception)
                    {
                    buffer.append(NEWLINE);
                    buffer.append(MSG_NEWS_FAILED + SPACE + exception.getMessage());
                    buffer.append(LINE);

                    LOGGER.error(SOURCE + MSG_NEWS_FAILED + " IOException " + exception.getMessage());
                    }

                catch (Exception exception)
                    {
                    buffer.append(NEWLINE);
                    buffer.append(MSG_NEWS_FAILED + SPACE + exception.getMessage());
                    buffer.append(LINE);

                    LOGGER.error(SOURCE + MSG_NEWS_FAILED + " Exception " + exception.getMessage());
                    }
                }

            buffer.append("Completed at: ");
            buffer.append(dao.getObservatoryClock().getDateTimeNowAsString());
            buffer.append(" UT");
            buffer.append(NEWLINE);
            }

        return (buffer.toString());
        }


    /***********************************************************************************************
     * Indicate if the Communicator is running.
     *
     * @return boolean
     */

    private static boolean isRunning()
        {
        return (boolCommunicatorRunning);
        }


    /***********************************************************************************************
     * Control the Communicator state.
     *
     * @param running
     */

    private static void setRunning(final boolean running)
        {
        boolCommunicatorRunning = running;
        }


    /**********************************************************************************************/
    /* Threads                                                                                    */
    /***********************************************************************************************
     * Get the SwingWorker which handles the Communicator Newsreader.
     *
     * @return SwingWorker
     */

    private static SwingWorker getCommunicatorWorker()
        {
        return (workerCommunicator);
        }


    /***********************************************************************************************
     * Set the SwingWorker which handles the Communicator Newsreader.
     *
     * @param worker
     */

    private static void setCommunicatorWorker(final SwingWorker worker)
        {
        workerCommunicator = worker;
        }
    }
