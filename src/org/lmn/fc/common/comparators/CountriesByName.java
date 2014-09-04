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

import org.lmn.fc.model.locale.CountryPlugin;

import java.util.Comparator;


/**
 * TODO DOCUMENT ME!
 *
 * @version $Id$
 */
public final class CountriesByName implements Comparator
    {
    public static final int ISO = 0;
    public static final int IOC = 1;
    private int intSortType;

    /**
     * Creates a new CountriesByName object.
     *
     * @param type -
     */
    public CountriesByName(int type)
        {
        intSortType = type;
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
     * @return a negative integer, zero, or a positive integer as the
     *            first argument is less than, equal to, or greater than the
     *           second.
     * @throws ClassCastException if the arguments' types prevent them from
     *            being compared by this Comparator.
     */
    public int compare(Object obj0,
                       Object obj1)
        {
        if((obj0 != null) &&
               (!obj0.equals("")) &&
               (obj0 instanceof CountryPlugin) &&
               (obj1 != null) &&
               (!obj1.equals("")) &&
               (obj1 instanceof CountryPlugin))
            {
            if(intSortType == ISO)
                {
                return (((CountryPlugin)obj0).getISOCountryName().compareTo(((CountryPlugin)obj1).getISOCountryName()));
                }

            if(intSortType == IOC)
                {
                return (((CountryPlugin)obj0).getIOCCountryName().compareTo(((CountryPlugin)obj1).getIOCCountryName()));
                }

            return (0);
            }
        else
            {
            return (0);
            }
        }
    }
