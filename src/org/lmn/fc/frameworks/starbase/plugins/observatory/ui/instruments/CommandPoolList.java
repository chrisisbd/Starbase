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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments;


import org.lmn.fc.common.constants.FrameworkStrings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;


/***************************************************************************************************
 * CommandPoolList.
 */

public final class CommandPoolList extends ArrayList<String>
    {
    /***********************************************************************************************
     * Construct a List of Command Names, with custom behaviours.
     *
     * @param initialcapacity
     */

    public CommandPoolList(final int initialcapacity)
        {
        super(initialcapacity);
        }


    /***************************************************************************************
     * Appends the specified Command Name element to the end of this list,
     * but only if it is not already present in the Collection.
     *
     * @param name element to be appended to this list
     *
     * @return boolean
     */

    public boolean add(final String name)
        {
        // The collection needs to be a List in order to use contains()
        if ((name != null)
            && (!this.contains(name)))
            {
            return (super.add(name));
            }
        else
            {
            // The Collection is not changed
            return (false);
            }
        }


    /***************************************************************************************
     * Add all items in the specified Collection to the List of Command Names,
     * provided that they are not already contained in the List.
     * This operation means we need our own special form of addAll().
     *
     * @param collection
     *
     * @return boolean
     */

    public boolean addAll(final Collection<? extends String> collection)
        {
        boolean boolCollectionChanged;

        boolCollectionChanged = false;

        if (collection != null)
            {
            final Iterator<? extends String> iterNames;

            iterNames = collection.iterator();

            while (iterNames.hasNext())
                {
                final String strName;

                strName = iterNames.next();
                add(strName);

                // Ideally test each add()
                boolCollectionChanged = true;
                }
            }
        else
            {
            // Do this to have the same behaviour as addAll() in the superclass
            throw new NullPointerException(FrameworkStrings.EXCEPTION_PARAMETER_NULL);
            }

        return (boolCollectionChanged);
        }


    /**********************************************************************************************
     * Removes all of the elements from this list.
     * The list will be empty after this call returns.
     */

    public void clear()
        {
        super.clear();
        }


    /**********************************************************************************************
     * Returns <tt>true</tt> if this list contains the specified element.
     * More formally, returns <tt>true</tt> if and only if this list contains
     * at least one element <tt>e</tt> such that
     * <tt>(o==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;o.equals(e))</tt>.
     *
     * @param object element whose presence in this list is to be tested
     *
     * @return <tt>true</tt> if this list contains the specified element
     */

    public boolean contains(final Object object)
        {
        return (super.contains(object));
        }
    }

