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

package org.lmn.fc.common.utilities.java;

import java.util.Enumeration;
import java.util.NoSuchElementException;

// note: class is unsynchronized ! ObjectExpr is supposed to do the synchronization.

public class OrderedHashtable implements Cloneable
    {

    private transient OrderedHashtableEntry table[];
    private transient int count;
    private int threshold;
    private float loadFactor;

    private OrderedHashtableEntry first = null;
    private OrderedHashtableEntry last = null;
    private int garbage = 0;
    private float gcFactor = 0.5f;

    public OrderedHashtable(int initialCapacity, float loadFactor)
        {
        if ((initialCapacity <= 0) || (loadFactor <= 0.0))
            {
            throw new IllegalArgumentException();
            }
        this.loadFactor = loadFactor;
        table = new OrderedHashtableEntry[initialCapacity];
        threshold = (int) (initialCapacity * loadFactor);
        }

    public OrderedHashtable(int initialCapacity)
        {
        this(initialCapacity, 0.75f);
        }

    public OrderedHashtable()
        {
        this(101, 0.75f);
        }

    public int size()
        {
        return count;
        }

    public Enumeration keys()
        {
        collect(0);
        return new OrderedHashtableEnumerator(first, true);
        }

    public Enumeration elements()
        {
        collect(0);
        return new OrderedHashtableEnumerator(first, false);
        }

    public boolean containsKey(Object key)
        {
        OrderedHashtableEntry tab[] = table;
        int hash = key.hashCode();
        int index = (hash & 0x7FFFFFFF) % tab.length;
        for (OrderedHashtableEntry e = tab[index]; e != null; e = e.next)
            {
            if ((e.hash == hash) && e.key.equals(key))
                {
                return true;
                }
            }
        return false;
        }

    public Object get(Object key)
        {
        OrderedHashtableEntry tab[] = table;
        int hash = key.hashCode();
        int index = (hash & 0x7FFFFFFF) % tab.length;
        for (OrderedHashtableEntry e = tab[index]; e != null; e = e.next)
            {
            if ((e.hash == hash) && e.key.equals(key))
                {
                return e.value;
                }
            }
        return null;
        }

    public boolean remove(Object key)
        {
        OrderedHashtableEntry tab[] = table;
        int hash = key.hashCode();
        int index = (hash & 0x7FFFFFFF) % tab.length;
        OrderedHashtableEntry e = tab[index];
        OrderedHashtableEntry prev = null;
        while (e != null)
            {
            if ((e.hash == hash) && e.key.equals(key))
                {
                // kill entry for GC
                e.key = null;
                e.value = null;

                // unlink entry
                if (prev == null)
                    tab[index] = e.next;
                else
                    prev.next = e.next;
                e.next = null;

                count--;
                garbage++;
                return true;
                }
            prev = e;
            e = e.next;
            }
        return false;
        }

    protected void rehash()
        {
        int oldCapacity = table.length;
        OrderedHashtableEntry oldTable[] = table;

        int newCapacity = oldCapacity * 2 + 1;
        OrderedHashtableEntry newTable[] = new OrderedHashtableEntry[newCapacity];

        threshold = (int) (newCapacity * loadFactor);
        table = newTable;

        for (int i = oldCapacity; i-- > 0;)
            {
            for (OrderedHashtableEntry old = oldTable[i]; old != null;)
                {
                OrderedHashtableEntry e = old;
                old = old.next;

                int index = (e.hash & 0x7FFFFFFF) % newCapacity;
                e.next = newTable[index];
                newTable[index] = e;
                }
            }
        }

    // remove garbage entries out of definition list, if any
    protected void collect(int gcthreshold)
        {
        if (garbage > gcthreshold)
            {
//            Log.debugln("[Object GC collect " + garbage + "]");

            OrderedHashtableEntry e = first;
            OrderedHashtableEntry prev = null;

            while (e != null)
                {
                if (e.key == null)
                    {    // garbage entry
                    if (prev == null)
                        first = e.follower;
                    else
                        prev.follower = e.follower;
                    if (e.follower == null)
                        last = prev;
                    e = e.follower;     // prev stays the same
                    garbage--;
                    }
                else
                    {
                    prev = e;
                    e = e.follower;
                    }
                }
            if (garbage != 0)
                throw new InternalError("OrderedHashtable collect failed");
            }
        }

    public Object put(Object key, Object value)
        {
        if (value == null)
            throw new NullPointerException();

        collect((int) (gcFactor * table.length));

        // Makes sure the key is not already in the hashtable.
        OrderedHashtableEntry tab[] = table;
        int hash = key.hashCode();
        int index = (hash & 0x7FFFFFFF) % tab.length;
        for (OrderedHashtableEntry e = tab[index]; e != null; e = e.next)
            {
            if ((e.hash == hash) && e.key.equals(key))
                {
                Object old = e.value;
                e.value = value;
                return old;
                }
            }

        if (count >= threshold)
            {
            // Rehash the table if the threshold is exceeded
            rehash();
            return put(key, value);
            }

        // Creates the new entry.
        OrderedHashtableEntry e = new OrderedHashtableEntry();
        e.hash = hash;
        e.key = key;
        e.value = value;
        e.next = tab[index];
        tab[index] = e;

        // insert in def chain
        if (first == null)
            first = last = e;
        else
            {
            last.follower = e;
            last = e;
            }

        count++;
        return null;
        }

    public Object clone()
        {
        OrderedHashtable t = new OrderedHashtable();
        OrderedHashtableEntry e = first;
        while (e != null)
            {
            if (e.key != null)
                t.put(e.key, e.value);
            e = e.follower;
            }
        return t;
        }

    }

class OrderedHashtableEnumerator implements Enumeration
    {
    OrderedHashtableEntry entry;
    boolean keys;

    OrderedHashtableEnumerator(OrderedHashtableEntry first, boolean keys)
        {
        entry = first;
        while (entry != null && entry.key == null) entry = entry.follower;
        this.keys = keys;
        }

    public boolean hasMoreElements()
        {
        return entry != null;
        }

    public Object nextElement()
        {
        if (entry == null)
            throw new NoSuchElementException("OrderedHashtableEnumerator");
        else
            {
            OrderedHashtableEntry R = entry;
            entry = entry.follower;
            while (entry != null && entry.key == null) entry = entry.follower;
            return keys ? R.key : R.value;
            }
        }
    }


class OrderedHashtableEntry
    {
    int hash;
    Object key;
    Object value;
    OrderedHashtableEntry next;
    OrderedHashtableEntry follower;
    }
