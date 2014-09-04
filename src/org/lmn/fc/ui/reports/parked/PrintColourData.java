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

package org.lmn.fc.ui.reports.parked;

import java.awt.*;

public class PrintColourData
    {

    public Color m_color;
    public Object m_data;
    public static Color GREEN = new Color(0, 128, 0);
    public static Color RED = Color.red;

    public PrintColourData(Color color, Object data)
        {
        m_color = color;
        m_data = data;
        }

    public PrintColourData(Double data)
        {
        m_color = data.doubleValue() >= 0 ? GREEN : RED;
        m_data = data;
        }

    public Color getColor() {
        return m_color;
    }

    public String toString()
        {
        return m_data.toString();
        }
    }
