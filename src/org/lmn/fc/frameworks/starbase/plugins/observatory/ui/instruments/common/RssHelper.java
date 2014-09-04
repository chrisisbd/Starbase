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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common;


import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;
import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.common.utilities.misc.Utilities;
import org.lmn.fc.common.utilities.threads.SwingWorker;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.DAOWrapperInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryClockInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageStatus;
import org.lmn.fc.model.xmlbeans.subscriptions.RssSubscription;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Vector;


/***************************************************************************************************
 * RssHelper.
 *
 * https://rometools.jira.com/wiki/display/ROME/Rome+API+FAQ
 */

public final class RssHelper
    {
    // String Resources
    public static final String MSG_NEWS_FAILED = "Failed to read News Subscriptions";
    public static final String MSG_RSS_NO_DATA = "No Data";

    private static final String TITLE_RSS_FEED = "RSS Feed: ";
    private static final String MSG_RSS_FAILED = "Failed to read RSS";
    private static final String MSG_IO_FAILED = "Failed to read input";
    private static final String NEWLINE = "<br>";
    private static final String LINE = "<hr>";


    /***********************************************************************************************
     * Create a SwingWorker Thread to read the RSS feed and return the data via the DAO.
     * Add the Thread to the Registry ThreadGroup.
     * Used in Commands GetMantisIssues and GetSubversionLog.
     *
     * @param dao
     * @param commandmessage
     * @param rssname
     * @param rssurl
     * @param showdetail
     * @param source
     *
     * @return SwingWorker
     */

    public static SwingWorker createRssThread(final ObservatoryInstrumentDAOInterface dao,
                                              final CommandMessageInterface commandmessage,
                                              final String rssname,
                                              final String rssurl,
                                              final boolean showdetail,
                                              final String source)
        {
        return new SwingWorker(FrameworkSingletons.REGISTRY.getThreadGroup(), source)
            {
            /*******************************************************************************
             * Run the RSS reader
             *
             * @return Object
             */

            public Object construct()
                {
                final RssSubscription subscription;
                final DAOWrapperInterface daoWrapper;

                subscription = RssSubscription.Factory.newInstance();
                subscription.setName(rssname);
                subscription.setURL(rssurl);
                subscription.setShowDetail(showdetail);

                daoWrapper = new DAOWrapper(commandmessage,
                                            null,
                                            ResponseMessageStatus.SUCCESS.getResponseValue(),
                                            dao);

                while (Utilities.workerCanProceed(dao, this))
                    {
                    final Vector<Object> vecRssRawData;
                    final String strRSS;

                    // Update all Resources each time round
                    dao.readResources();

                    // We musn't timeout, since this might run forever...
                    TimeoutHelper.restartDAOTimeoutTimerInfinite(dao);

                    // The RSS is the Raw Data, a single String
                    vecRssRawData = new Vector<Object>(1);

                    strRSS = getRSS(subscription, dao.getObservatoryClock());
                    vecRssRawData.add(strRSS);

                    // Pass the RSS data to the DAOWrapper
                    daoWrapper.setRawData(vecRssRawData);

                    controlledStop(true, 1000);
                    }

                return (daoWrapper);
                }


            /***********************************************************************************
             * When the Thread stops.
             */

            public void finished()
                {
                // Put the Timeout back to what it should be for a single default command
                TimeoutHelper.resetDAOTimeoutTimerFromRegistryDefault(dao);

                if ((get() != null)
                    && (get() instanceof DAOWrapperInterface))
                    {
                    final DAOWrapperInterface daoWrapper;

                    daoWrapper = (DAOWrapperInterface)get();

                    dao.setWrappedData(daoWrapper);

                    // Keep re-applying the updated DAO Wrapper to the host Instrument,
                    // to ensure that RSS tabs get updated
                    // Only refresh the data if visible
                    dao.getHostInstrument().setWrappedData(dao.getWrappedData(), false, false);
                    }
                }
            };
        }


    /***********************************************************************************************
     * Get the RSS from the RssSubscription.
     *
     * @param rsssubscription
     * @param clock
     *
     * @return String
     */

    private static String getRSS(final RssSubscription rsssubscription,
                                 final ObservatoryClockInterface clock)
        {
        final StringBuffer buffer;

        buffer = new StringBuffer();

        try
            {
            final URL feedUrl;
            final SyndFeed feed;
            final List<SyndEntry> listEntries;
            final SyndFeedInput input;
            final int intDetailCount;

            feedUrl = new URL(rsssubscription.getURL());
            input = new SyndFeedInput();
            feed = input.build(new XmlReader(feedUrl));
            listEntries = feed.getEntries();

            // Output only if there's no exception
            if ((feed.getTitle() != null)
                && (!FrameworkStrings.EMPTY_STRING.equals(feed.getTitle())))
                {
                buffer.append(TITLE_RSS_FEED);
                buffer.append(feed.getTitle());
                buffer.append(NEWLINE);
                buffer.append(LINE);
                }

            if (rsssubscription.getShowDetail())
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
                        && (!FrameworkStrings.EMPTY_STRING.equals(entry.getTitle())))
                        {
                        buffer.append(entry.getTitle());
                        buffer.append(NEWLINE);
                        }

                    if ((entry.getAuthor() != null)
                        && (!FrameworkStrings.EMPTY_STRING.equals(entry.getAuthor())))
                        {
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
                        }

                    buffer.append(LINE);
                    }
                }

            buffer.append("Completed at: ");
            buffer.append(clock.getDateTimeNowAsString());
            buffer.append(" UT");
            buffer.append(NEWLINE);
            }

        catch (FeedException exception)
            {
            buffer.append(NEWLINE);
            buffer.append(MSG_RSS_FAILED);
            buffer.append(FrameworkStrings.SPACE);
            buffer.append(exception.getMessage());
            FrameworkSingletons.LOGGER.error(buffer.toString());
            }

        catch (IllegalArgumentException exception)
            {
            buffer.append(NEWLINE);
            buffer.append(MSG_RSS_FAILED);
            buffer.append(FrameworkStrings.SPACE);
            buffer.append(exception.getMessage());
            FrameworkSingletons.LOGGER.error(buffer.toString());
            }

        catch (IOException exception)
            {
            buffer.append(NEWLINE);
            buffer.append(MSG_IO_FAILED);
            buffer.append(FrameworkStrings.SPACE);
            buffer.append(exception.getMessage());
            FrameworkSingletons.LOGGER.error(buffer.toString());
            }

        return (buffer.toString());
        }


    /***********************************************************************************************
     * Get the News using the supplied List of RssSubscriptions.
     * Used by the SimpleNewsreaderPanel.
     * Return NO_DATA is no News is available.
     *
     * @param rsssubscriptions
     * @param clock
     *
     * @return String
     */

    public static String getNews(final List<RssSubscription> rsssubscriptions,
                                 final ObservatoryClockInterface clock)
        {
        StringBuffer buffer;

        buffer = new StringBuffer(MSG_RSS_NO_DATA);

        try
            {
            // Process RSS
            if (rsssubscriptions != null)
                {
                for (int i = 0;
                     i < rsssubscriptions.size();
                     i++)
                    {
                    final RssSubscription subscription;
                    final int intDetailCount;

                    subscription = rsssubscriptions.get(i);

                    if (subscription != null)
                        {
                        final URL feedUrl;
                        final SyndFeed feed;
                        final List<SyndEntry> listEntries;
                        final SyndFeedInput input;

                        buffer = new StringBuffer();
                        feedUrl = new URL(subscription.getURL());
                        input = new SyndFeedInput();
                        //input.setPreserveWireFeed(true);
                        feed = input.build(new XmlReader(feedUrl));
                        listEntries = feed.getEntries();

                        //System.out.println("RSS FEED");
                        //System.out.println("{{" + feed + "}}");

                        // Output only if there's no exception
                        if ((feed.getTitle() != null)
                            && (!FrameworkStrings.EMPTY_STRING.equals(feed.getTitle())))
                            {
                            buffer.append("RSS Feed: ");
                            buffer.append(feed.getTitle());
                            buffer.append(NEWLINE);
                            buffer.append(LINE);
                            }

                        if (subscription.getShowDetail())
                            {
                            intDetailCount = listEntries.size();
                            //System.out.println("RSS NEWS SHOW DETAIL count=" + intDetailCount);
                            }
                        else
                            {
                            // Show only the first entry
                            intDetailCount = 1;
                            }

                        for (int intDetailIndex = 0;
                             intDetailIndex < intDetailCount;
                             intDetailIndex++)
                            {
                            final SyndEntry entry;
                            final List<SyndContent> listContents;

                            entry = listEntries.get(intDetailIndex);

                            if (entry != null)
                                {
                                if ((entry.getTitle() != null)
                                    && (!FrameworkStrings.EMPTY_STRING.equals(entry.getTitle())))
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
                                    && (!FrameworkStrings.EMPTY_STRING.equals(entry.getAuthor())))
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

                                    for (int k = 0;
                                         k < listContents.size();
                                         k++)
                                        {
                                        final SyndContent content;

                                        content = listContents.get(k);

                                        if (content != null)
                                            {
                                            System.out.println("Syndicated Content [index=" + k + "] [" + content.getValue() + "]");
                                            }
                                        }
                                    }

                                buffer.append(LINE);
                                }
                            }
                        }
                    }
                }

            buffer.append("Completed at: ");
            buffer.append(clock.getDateTimeNowAsString());
            buffer.append(" UT");
            buffer.append(NEWLINE);
            }

        catch (FeedException exception)
            {
            buffer.append(NEWLINE);
            buffer.append(MSG_RSS_FAILED + FrameworkStrings.SPACE + exception.getMessage());
            FrameworkSingletons.LOGGER.error(MSG_RSS_FAILED + FrameworkStrings.SPACE + exception.getMessage());
            }

        catch (IllegalArgumentException exception)
            {
            buffer.append(NEWLINE);
            buffer.append(MSG_RSS_FAILED + FrameworkStrings.SPACE + exception.getMessage());
            FrameworkSingletons.LOGGER.error(MSG_RSS_FAILED + FrameworkStrings.SPACE + exception.getMessage());
            }

        catch (IOException exception)
            {
            buffer.append(NEWLINE);
            buffer.append(MSG_NEWS_FAILED + FrameworkStrings.SPACE + exception.getMessage());
            FrameworkSingletons.LOGGER.error(MSG_NEWS_FAILED + FrameworkStrings.SPACE + exception.getMessage());
            }

        return (buffer.toString());
        }
    }
