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

package org.lmn.fc.frameworks.starbase.plugins.observatory.comparators;

import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;

import java.util.Comparator;


/***************************************************************************************************
 * Compares two ObservatoryInstruments by their Identifiers.
 */

public final class ObservatoryInstrumentsByIdentifier implements Comparator
    {
    /***********************************************************************************************
     * Sort the List of ObservatoryInstruments by their Identifiers.
     */

    public ObservatoryInstrumentsByIdentifier()
        {
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
     * @param obj0 the first object to be compared.
     * @param obj1 the second object to be compared.
     *
     * @return a negative integer, zero, or a positive integer as the
     *            first argument is less than, equal to, or greater than the
     *           second.
     *
     * @throws ClassCastException if the arguments' types prevent them from
     *            being compared by this Comparator.
     */

    public int compare(final Object obj0,
                       final Object obj1)
        {
        // The two Instruments being compared must both have ObservatoryGroups specified
        // with at least one Group, otherwise they are treated as being equal for sorting

        if ((obj0 instanceof ObservatoryInstrumentInterface)
            && (obj1 instanceof ObservatoryInstrumentInterface)
            && (((ObservatoryInstrumentInterface)obj0).getInstrument() != null)
            && (((ObservatoryInstrumentInterface)obj0).getInstrument().getIdentifier() != null)
            && (((ObservatoryInstrumentInterface)obj1).getInstrument() != null)
            && (((ObservatoryInstrumentInterface)obj1).getInstrument().getIdentifier() != null))
            {
            return (((ObservatoryInstrumentInterface)obj0).getInstrument().getIdentifier().compareTo(((ObservatoryInstrumentInterface)obj1).getInstrument().getIdentifier()));
            }
        else
            {
            // Return zero if they are equal, or may not be compared
            return (0);
            }
        }
    }
