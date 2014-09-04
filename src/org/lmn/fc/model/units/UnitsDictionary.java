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

package org.lmn.fc.model.units;


import org.lmn.fc.model.xmlbeans.metadata.SchemaUnits;


/***************************************************************************************************
 * UnitsDictionary.
 * A temporary class while working out what to do with Units.
 *
 * JSR-275
 * http://kenai.com/projects/jsr-275/pages/Home
 * http://www.jcp.org/en/jsr/detail?id=275
 * http://www.jcp.org/en/jsr/results?id=5064
 * http://jscience.org/api/javax/measure/unit/UnitFormat.html
 * http://jscience.org/api/javax/measure/unit/UnitFormat.html#parseSingleUnit%28java.lang.CharSequence,%20java.text.ParsePosition%29 *
 *
 */

public final class UnitsDictionary
    {
    /***********************************************************************************************
     * Get the SchemaUnits corresponding to the specified Units Name.
     *
     * @param name
     *
     * @return SchemaUnits.Enum
     */

    public static SchemaUnits.Enum getSchemaUnitsForName(final String name)
        {
        return (SchemaUnits.Enum.forString(name));
        }
    }
