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

package org.lmn.fc.common.datatranslators;


import org.jfree.data.time.Day;
import org.jfree.data.time.Hour;
import org.jfree.data.time.Minute;
import org.lmn.fc.common.constants.FrameworkStrings;

import java.util.Calendar;


/***************************************************************************************************
 * SegmentSize.
 */

public enum SegmentSize
    {
    DAY         (0, "Day",      Day.class,    Calendar.DAY_OF_YEAR, 86400000, false),
    DAYTIME     (1, "Daytime",  Day.class,    Calendar.DAY_OF_YEAR, 86400000, true),
    HOUR        (2, "Hour",     Hour.class,   Calendar.HOUR_OF_DAY, 3600000,  false),
    MINUTE      (3, "Minute",   Minute.class, Calendar.MINUTE,      60000,    false);


    private final int intIndex;
    private final String strName;
    private final Class timePeriodClass;
    private final int intCalendarField;
    private final long longMilliseconds;
    private final boolean boolPostProcess;


    /***********************************************************************************************
     * Get the SegmentSize enum corresponding to the specified SegmentSize name.
     * Return NULL if not found.
     *
     * @param name
     *
     * @return SegmentSize
     */

    public static SegmentSize getSegmentSizeForName(final String name)
        {
        SegmentSize segmentSize;

        segmentSize = null;

        if ((name != null)
            && (!FrameworkStrings.EMPTY_STRING.equals(name)))
            {
            final SegmentSize[] segmentSizes;
            boolean boolFoundIt;

            segmentSizes = values();
            boolFoundIt = false;

            for (int i = 0;
                 (!boolFoundIt) && (i < segmentSizes.length);
                 i++)
                {
                final SegmentSize size;

                size = segmentSizes[i];

                if (name.equals(size.getSegmentSizeName()))
                    {
                    segmentSize = size;
                    boolFoundIt = true;
                    }
                }
            }

        return (segmentSize);
        }


    /***********************************************************************************************
     * SegmentSize.
     * An enumeration of possible ways to segment a TimeSeries.
     * Note: Only one time period class can be used within a single series (enforced).
     * e.g. if you add a data item with a Year for the time period,
     * then all subsequent data items must also have a Year for the time period.
     *
     * @param index
     * @param name
     * @param periodclass
     * @param calendarfield
     * @param milliseconds
     * @param postprocess
     */

    private SegmentSize(final int index,
                        final String name,
                        final Class periodclass,
                        final int calendarfield,
                        final long milliseconds,
                        final boolean postprocess)
        {
        intIndex = index;
        strName = name;
        timePeriodClass = periodclass;
        intCalendarField = calendarfield;
        longMilliseconds = milliseconds;
        boolPostProcess = postprocess;
        }


    /***********************************************************************************************
     * Get the Index.
     *
     * @return int
     */

    public int getIndex()
        {
        return (this.intIndex);
        }


    /***********************************************************************************************
     * Get the SegmentSize name.
     *
     * @return String
     */

    public String getSegmentSizeName()
        {
        return (this.strName);
        }


    /***********************************************************************************************
     * Get the TimePeriod Class.
     * Note: Only one time period class can be used within a single series (enforced).
     * e.g. if you add a data item with a Year for the time period,
     * then all subsequent data items must also have a Year for the time period.
     *
     * @return Class
     */

    public Class getTimePeriodClass()
        {
        return (this.timePeriodClass);
        }


    /***********************************************************************************************
     * Get the CalendarField.
     *
     * @return int
     */

    public int getCalendarField()
        {
        return (this.intCalendarField);
        }


    /***********************************************************************************************
     * Get the number of milliseconds represented by this SegmentSize.
     *
     * @return long
     */

    public long getMilliseconds()
        {
        return (this.longMilliseconds);
        }


    /***********************************************************************************************
     * Get the PostProcess flag.
     * This indicates if further processing is required,
     * after segmenting the data into CalendarField segments.
     *
     * @return boolean
     */

    public boolean isPostProcess()
        {
        return (this.boolPostProcess);
        }


    /***********************************************************************************************
     * Get the SegmentSize name.
     *
     * @return String
     */

    public String toString()
        {
        return (this.strName);
        }
    }
