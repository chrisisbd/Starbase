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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.commands.publishers;


import org.lmn.fc.common.constants.*;
import org.lmn.fc.frameworks.starbase.plugins.observatory.common.ObservatoryConstants;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageInterface;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;

import java.util.List;


/***************************************************************************************************
 * PublishRealtime.
 *
 * Parameters:
 *
 *  Capture.Trigger         // doPublishChartRealtimeDay() ONLY
 *  Capture.Interval
 *  Capture.Period
 *  Capture.Filter
 *  Upload.Counter
 *
 *  Image.Format
 *  Image.Width
 *  Image.Height
 *  Image.LocalDirectory
 *  Image.LocalFilename
 *  Image.Timestamp
 *
 *  Server.Hostname
 *  Server.Username
 *  Server.Password
 *  Server.RemoteDirectory
 *  Server.RemoteFilename
 *
 *  VerboseLogging
 *
 *  Returns:
 *
 *  Publisher.Timestamp
 */

public final class PublishRealtime implements FrameworkConstants,
                                              FrameworkStrings,
                                              FrameworkMetadata,
                                              FrameworkSingletons,
                                              FrameworkXpath,
                                              ObservatoryConstants
    {
    /***********************************************************************************************
     * doPublishChartRealtime.
     * Publish a chart until told to stop.
     *
     * @param dao
     * @param commandmessage
     * @param metadatalist
     * @param source
     * @param notifyport
     *
     * @return ResponseMessageInterface
     */

    public static ResponseMessageInterface doPublishChartRealtime(final ObservatoryInstrumentDAOInterface dao,
                                                                  final CommandMessageInterface commandmessage,
                                                                  final List<Metadata> metadatalist,
                                                                  final String source,
                                                                  final boolean notifyport)
        {
        return (PublishHelper.doPublishRealtime(dao, commandmessage, metadatalist, source, notifyport));
        }


    /***********************************************************************************************
     * doPublishChartRealtimeDay.
     * Publish a chart as a one-day segment, starting at the Time specified by a parameter.
     *
     * @param dao
     * @param commandmessage
     * @param metadatalist
     * @param source
     * @param notifyport
     *
     * @return ResponseMessageInterface
     */

    public static ResponseMessageInterface doPublishChartRealtimeDay(final ObservatoryInstrumentDAOInterface dao,
                                                                     final CommandMessageInterface commandmessage,
                                                                     final List<Metadata> metadatalist,
                                                                     final String source,
                                                                     final boolean notifyport)
        {
        return (PublishHelper.doPublishRealtimeDay(dao, commandmessage, metadatalist, source, notifyport));
        }
    }
