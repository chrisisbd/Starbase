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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.communicator.ui;

import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryClockInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.communicator.CommunicatorDAOInterface;
import org.lmn.fc.model.root.RootPlugin;
import org.lmn.fc.model.xmlbeans.datatypes.SchemaDataType;
import org.lmn.fc.model.xmlbeans.metadata.SchemaUnits;
import org.lmn.fc.model.xmlbeans.subscriptions.RssSubscription;
import org.lmn.fc.model.xmlbeans.subscriptions.TwitterSubscription;
import org.lmn.fc.ui.reports.*;

import javax.swing.*;
import java.util.Vector;


/***********************************************************************************************
 * A UIComponent to show the Newsfeed Subscriptions.
 */

public final class SubscriptionsUIComponent extends ReportTable
                                            implements ReportTablePlugin
    {
    // String Resources
    private static final String REPORT_NAME          = "Subscriptions";
    private static final String MSG_REPORT_CREATED   = "Report created at";

    private static final String TWITTER_URL          = "http://twitter.com/";
    private static final String ICON_TWITTER         = "twitter.png";
    private static final String ICON_RSS             = "rss.png";

    private static final String TYPE_TWITTER         = "Twitter";
    private static final String TYPE_RSS             = "RSS";

    private static final String TITLE_ICON           = SPACE;
    private static final String TITLE_TYPE           = "Type";
    private static final String TITLE_NAME           = "Name";
    private static final String TITLE_URL            = "URL";
    private static final String TITLE_DETAIL         = "Detail";

    private static final int REPORT_COLUMNS = 5;

    // Injections
    private final ObservatoryInstrumentInterface hostInstrument;


    /***********************************************************************************************
     * Get the Subscriptions from the DAO.
     *
     * @param dao
     *
     * @return Vector<Vector>
     */

    private static Vector<Vector> getSubscriptions(final CommunicatorDAOInterface dao)
        {
        final Vector<Vector> vecReport;

        vecReport = new Vector<Vector>(50);

        try
            {
            //--------------------------------------------------------------------------------------
            // Process Twitter

            if ((dao != null)
                && (dao.getSubscriptionsDoc() != null)
                && (dao.getSubscriptionsDoc().getSubscriptions() != null)
                && (dao.getSubscriptionsDoc().getSubscriptions().getTwitterList() != null))
                {
                for (int i = 0;
                     i < dao.getSubscriptionsDoc().getSubscriptions().getTwitterList().size();
                     i++)
                    {
                    final TwitterSubscription subscription;

                    subscription = dao.getSubscriptionsDoc().getSubscriptions().getTwitterList().get(i);

                    if (subscription != null)
                        {
                        final Vector vecRow;

                        vecRow = new Vector(REPORT_COLUMNS);

                        vecRow.add(ReportIcon.getIcon(ICON_TWITTER));
                        vecRow.add(TYPE_TWITTER);
                        vecRow.add(subscription.getUsername());
                        vecRow.add(TWITTER_URL + subscription.getUsername());

                        // There is no ShowDetail flag for Twitter
                        vecRow.add(SPACE);

                        vecReport.add(vecRow);
                        }
                    }
                }

            //--------------------------------------------------------------------------------------
            // Process RSS

            if ((dao != null)
                && (dao.getSubscriptionsDoc() != null)
                && (dao.getSubscriptionsDoc().getSubscriptions() != null)
                && (dao.getSubscriptionsDoc().getSubscriptions().getRSSList() != null))
                {
                for (int i = 0;
                     i < dao.getSubscriptionsDoc().getSubscriptions().getRSSList().size();
                     i++)
                    {
                    final RssSubscription subscription;

                    subscription = dao.getSubscriptionsDoc().getSubscriptions().getRSSList().get(i);

                    if (subscription != null)
                        {
                        final Vector vecRow;

                        vecRow = new Vector(REPORT_COLUMNS);

                        vecRow.add(ReportIcon.getIcon(ICON_RSS));
                        vecRow.add(TYPE_RSS);
                        vecRow.add(subscription.getName());
                        vecRow.add(subscription.getURL());
                        vecRow.add(subscription.getShowDetail());

                        vecReport.add(vecRow);
                        }
                    }
                }
            }

        catch (IllegalArgumentException exception)
            {
            LOGGER.error("Failed to read RSS Subscriptions" + SPACE + exception.getMessage());
            }

        return (vecReport);
        }


    /***********************************************************************************************
     * Construct an SubscriptionsUIComponent.
     *
     * @param task
     * @param hostinstrument
     * @param resourcekey
     */

    public SubscriptionsUIComponent(final RootPlugin task,
                                    final ObservatoryInstrumentInterface hostinstrument,
                                    final String resourcekey)
        {
        super(task,
              REPORT_NAME,
              resourcekey,
              PRINTABLE,
              EXPORTABLE,
              NON_REFRESHABLE,
              REFRESH_NONE,
              NON_REORDERABLE,
              NON_TRUNCATEABLE,
              LOCK_TOP_ROW,
              SCROLL_LEFT_COLUMNS,
              0,
              ReportTableToolbar.NONE,
              null);

        this.hostInstrument = hostinstrument;
        }


    /***********************************************************************************************
     * Generate the report header.
     *
     * @return Vector
     */

    public final Vector<String> generateHeader()
        {
        final Vector<String> vecHeader;

        vecHeader = new Vector<String>(1);

        vecHeader.add(getReportUniqueName() + SPACE + MSG_REPORT_CREATED + SPACE + getObservatoryClock().getDateTimeNowAsString());

        return (vecHeader);
        }


    /***********************************************************************************************
     * Define the report columns.
     *
     * @return Vector
     */

    public final Vector<ReportColumnMetadata> defineColumns()
        {
        final Vector<ReportColumnMetadata> vecColumns;

        vecColumns = new Vector<ReportColumnMetadata>(defineColumnWidths().length);

        vecColumns.add(new ReportColumnMetadata(TITLE_ICON,
                                                SchemaDataType.STRING,
                                                SchemaUnits.DIMENSIONLESS,
                                                EMPTY_STRING,
                                                SwingConstants.CENTER));
        vecColumns.add(new ReportColumnMetadata(TITLE_TYPE,
                                                SchemaDataType.STRING,
                                                SchemaUnits.DIMENSIONLESS,
                                                EMPTY_STRING,
                                                SwingConstants.LEFT));
        vecColumns.add(new ReportColumnMetadata(TITLE_NAME,
                                                SchemaDataType.STRING,
                                                SchemaUnits.DIMENSIONLESS,
                                                EMPTY_STRING,
                                                SwingConstants.LEFT));
        vecColumns.add(new ReportColumnMetadata(TITLE_URL,
                                                SchemaDataType.STRING,
                                                SchemaUnits.DIMENSIONLESS,
                                                EMPTY_STRING,
                                                SwingConstants.LEFT));
        vecColumns.add(new ReportColumnMetadata(TITLE_DETAIL,
                                                SchemaDataType.STRING,
                                                SchemaUnits.DIMENSIONLESS,
                                                EMPTY_STRING,
                                                SwingConstants.LEFT));
        return (vecColumns);
        }


    /***********************************************************************************************
     * Define the widths of each column in terms of the objects which they will contain.
     *
     * @return Object []
     */

    public final Object [] defineColumnWidths()
        {
        final Object [] columnWidths =
            {
            ReportIcon.getIcon(ICON_DUMMY_DISTRIBUTION),
            "MMMMMM",
            "MMMMMMMMMMMMMMM",
            "MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM",
            "MMM"
            };

        return (columnWidths);
        }


    /***********************************************************************************************
     * Generate the report data table.
     *
     * @return Vector of report rows
     */

    public final Vector<Vector> generateReport()
        {
        final Vector<Vector> vecReport;

        if ((getHostInstrument() != null)
            && (getHostInstrument().getDAO() != null)
            && (getHostInstrument().getDAO() instanceof CommunicatorDAOInterface))
            {
            vecReport = getSubscriptions((CommunicatorDAOInterface)getHostInstrument().getDAO());
            }
        else
            {
            vecReport = new Vector<Vector>(1);
            }

        return (vecReport);
        }


    /***********************************************************************************************
     * Refresh the Report data.
     *
     * @return Vector
     */

    public Vector<Vector> refreshReport()
        {
        // Have larger rows to show Icons, because there won't be many subscriptions
        getReportTable().setRowHeight(ReportIcon.getIcon(ICON_DUMMY_DISTRIBUTION).getIconHeight());

        return (generateReport());
        }


    /***********************************************************************************************
     * Get the ObservatoryInstrument to which this UIComponent is attached.
     *
     * @return ObservatoryInstrumentInterface
     */

    private ObservatoryInstrumentInterface getHostInstrument()
        {
        return (this.hostInstrument);
        }


    /***********************************************************************************************
     * Get the ObservatoryClock.
     *
     * @return ObservatoryClockInterface
     */

    private synchronized ObservatoryClockInterface getObservatoryClock()
        {
        final ObservatoryClockInterface clock;

        clock = getHostInstrument().getObservatoryClock();

        return (clock);
        }
    }
