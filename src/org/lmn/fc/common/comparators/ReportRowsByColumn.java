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

package org.lmn.fc.common.comparators;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Vector;


/***************************************************************************************************
 * Compare two Report rows.
 */
public final class ReportRowsByColumn implements Comparator<Vector>
    {
    private final ArrayList<Integer> listColumns;


    /***********************************************************************************************
     * Sort the Report rows by the specified column index.
     *
     * @param column
     */
    public ReportRowsByColumn(final int column)
        {
        listColumns = new ArrayList<Integer>(10);
        listColumns.add(column);
        }


    /***********************************************************************************************
     * Sort the Report rows by the list of specified column indexes.
     *
     * @param columns
     */
    public ReportRowsByColumn(final ArrayList<Integer> columns)
        {
        listColumns = columns;
        }


    /***********************************************************************************************
     * Compares its two arguments for order.  Returns a negative integer,
     * zero, or a positive integer as the first argument is less than, equal
     * to, or greater than the second.<p>
     *
     * The implementor must ensure that <tt>sgn(compare(x, y)) ==
     * -sgn(compare(y, x))</tt> for all <tt>x</tt> and <tt>y</tt>.  (This
     * implies that <tt>compare(x, y)</tt> must throw an exception if and only
     * if <tt>compare(y, x)</tt> throws an exception.)<p>
     *
     * The implementor must also ensure that the relation is transitive:
     * <tt>((compare(x, y)&gt;0) &amp;&amp; (compare(y, z)&gt;0))</tt> implies
     * <tt>compare(x, z)&gt;0</tt>.<p>
     *
     * Finally, the implementer must ensure that <tt>compare(x, y)==0</tt>
     * implies that <tt>sgn(compare(x, z))==sgn(compare(y, z))</tt> for all
     * <tt>z</tt>.<p>
     *
     * It is generally the case, but <i>not</i> strictly required that
     * <tt>(compare(x, y)==0) == (x.equals(y))</tt>.  Generally speaking,
     * any comparator that violates this condition should clearly indicate
     * this fact.  The recommended language is "Note: this comparator
     * imposes orderings that are inconsistent with equals."
     *
     * @param vec0 the first object to be compared.
     * @param vec1 the second object to be compared.
     * @return a negative integer, zero, or a positive integer as the
     *            first argument is less than, equal to, or greater than the
     *           second.
     * @throws ClassCastException if the arguments' types prevent them from
     *            being compared by this Comparator.
     */
    public final int compare(final Vector vec0,
                             final Vector vec1)
        {
        final Iterator iterColumns;
        int intCompareColumns;
        int intColumnIndex;

        // Be very certain that we are dealing with two rows from a Report...
        if((vec0 != null) &&
               (vec1 != null) &&
               (vec0.size() == vec1.size()) &&
               (listColumns != null) &&
               (listColumns.size() > 0))
            {
            if(listColumns.size() == 1)
                {
                intColumnIndex = listColumns.get(0);

                // Compare Strings
                if((vec0.size() > intColumnIndex) &&
                       (vec0.get(intColumnIndex) instanceof String) &&
                       (vec1.get(intColumnIndex) instanceof String))
                    {
                    intCompareColumns =
                        ((String) vec0.get(intColumnIndex)).compareTo(((String) vec1.get(
                                intColumnIndex)));

                    return (intCompareColumns);
                    }
                else
                    {
                    // Compare Integers
                    if((vec0.size() > intColumnIndex) &&
                           (vec0.get(intColumnIndex) instanceof Integer) &&
                           (vec1.get(intColumnIndex) instanceof Integer))
                        {
                        intCompareColumns =
                            ((Integer) vec0.get(intColumnIndex)).compareTo(((Integer) vec1.get(
                                    intColumnIndex)));

                        return (intCompareColumns);
                        }
                    else
                        {
                        // Compare Times
                        if((vec0.size() > intColumnIndex) &&
                               (vec0.get(intColumnIndex) instanceof Time) &&
                               (vec1.get(intColumnIndex) instanceof Time))
                            {
                            intCompareColumns =
                                ((Time) vec0.get(intColumnIndex)).compareTo(((Time) vec1.get(
                                        intColumnIndex)));

                            return (intCompareColumns);
                            }
                        else
                            {
                            return (0);
                            }
                        }
                    }
                }
            else
                {
                // There must be a List of column indexes...
                iterColumns = listColumns.iterator();
                intCompareColumns = 0;

                while(iterColumns.hasNext())
                    {
                    intColumnIndex = (Integer) iterColumns.next();

                    // Compare Strings
                    if((vec0.size() > intColumnIndex) &&
                           (vec0.get(intColumnIndex) instanceof String) &&
                           (vec1.get(intColumnIndex) instanceof String))
                        {
                        intCompareColumns =
                            ((String) vec0.get(intColumnIndex)).compareTo(((String) vec1.get(
                                    intColumnIndex)));


                        // If the columns are different, return the sort order
                        // otherwise carry on with the next column index
                        if(intCompareColumns != 0)
                            {
                            return (intCompareColumns);
                            }
                        }
                    else
                        {
                        // Compare Integers
                        if((vec0.size() > intColumnIndex) &&
                               (vec0.get(intColumnIndex) instanceof Integer) &&
                               (vec1.get(intColumnIndex) instanceof Integer))
                            {
                            intCompareColumns =
                                ((Integer) vec0.get(intColumnIndex)).compareTo(((Integer) vec1.get(
                                        intColumnIndex)));


                            // If the columns are different, return the sort order
                            // otherwise carry on with the next column index
                            if(intCompareColumns != 0)
                                {
                                return (intCompareColumns);
                                }
                            }
                        else
                            {
                            // Compare Times
                            if((vec0.size() > intColumnIndex) &&
                                   (vec0.get(intColumnIndex) instanceof Time) &&
                                   (vec1.get(intColumnIndex) instanceof Time))
                                {
                                intCompareColumns =
                                    ((Time) vec0.get(intColumnIndex)).compareTo(((Time) vec1.get(
                                            intColumnIndex)));


                                // If the columns are different, return the sort order
                                // otherwise carry on with the next column index
                                if(intCompareColumns != 0)
                                    {
                                    return (intCompareColumns);
                                    }
                                }
                            else
                                {
                                // Something has gone wrong with the column data...
                                break;
                                }
                            }
                        }
                    }

                // If all columns are identical, just return the last sort order (0)
                return (intCompareColumns);
                }
            }
        else
            {
            return (0);
            }
        }
    }
