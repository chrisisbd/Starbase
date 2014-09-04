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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.serial.validation;


import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.serial.SerialConfigurationNodeType;

import java.util.Arrays;


/***************************************************************************************************
 * ConstraintNode.
 * Taken from: http://forum.jgraph.com/questions/5222/custom-graph-validation
 */

public final class ConstraintNode
    {
    // Indicates SOURCE or TARGET
    public final boolean boolSource;

    // Enforced type; the list acts as "OR" connections
    public final SerialConfigurationNodeType[] arrayNodeTypes;
    public final int intConnectionsMin;
    public final int intConnectionsMax;


    /***********************************************************************************************
     * Construct a Constraint for a Node.
     * The SINGLE SerialConfigurationNodeType is treated as an "UP TO nnn" Constraint.
     *
     * @param source
     * @param type
     * @param min
     * @param max
     */

    public ConstraintNode(final boolean source,
                          final SerialConfigurationNodeType type,
                          final int min,
                          final int max)
        {
        this.boolSource = source;
        this.arrayNodeTypes = new SerialConfigurationNodeType[]{type};
        this.intConnectionsMin = min;
        this.intConnectionsMax = max;
        }


    /***********************************************************************************************
     * Construct a set of Constraints for a Node.
     * The ARRAY of SerialConfigurationNodeType is treated as a "ONE OF nnn" Constraint.
     *
     * @param source
     * @param arraytypes
     * @param min
     * @param max
     */

    public ConstraintNode(final boolean source,
                          final SerialConfigurationNodeType[] arraytypes,
                          final int min,
                          final int max)
        {
        this.boolSource = source;
        this.arrayNodeTypes = arraytypes;
        this.intConnectionsMin = min;
        this.intConnectionsMax = max;
        }


    /***********************************************************************************************
     * Indicate some details of the Constraint.
     *
     * @return String
     */

    public String toString()
        {
        final StringBuffer buffer;

        buffer = new StringBuffer();

        if (boolSource)
            {
            buffer.append("[constraint.source] [types=");
            }
        else
            {
            buffer.append("[constraint.target] [types=");
            }

        buffer.append(Arrays.asList(arrayNodeTypes));
        buffer.append("] [min=");
        buffer.append(intConnectionsMin);
        buffer.append("] [max=");
        buffer.append(intConnectionsMin);
        buffer.append("]");

        return (buffer.toString());
        }
    }
