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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.serial;


import org.lmn.fc.common.constants.FrameworkStrings;


/***************************************************************************************************
 * SerialFlowControl.
 */

public enum SerialFlowControl
    {
    FLOW_NONE       ("None",     "None"),
    FLOW_XON_XOFF   ("XON/XOFF", "XON/XOFF"),
    FLOW_RTS_CTS    ("RTS/CTS",  "RTS/CTS");

    private final String strFlowControl;
    private final String strName;


    /***********************************************************************************************
     * Get the SerialFlowControl enum corresponding to the specified SerialFlowControl name.
     * Return NULL if the SerialFlowControl name is not found.
     *
     * @param name
     *
     * @return SerialFlowControl
     */

    public static SerialFlowControl getSerialFlowControlForName(final String name)
        {
        SerialFlowControl flowControl;

        flowControl = null;

        if ((name != null)
            && (!FrameworkStrings.EMPTY_STRING.equals(name)))
            {
            final SerialFlowControl[] arrayFlowControl;
            boolean boolFoundIt;

            arrayFlowControl = values();
            boolFoundIt = false;

            for (int i = 0;
                 (!boolFoundIt) && (i < arrayFlowControl.length);
                 i++)
                {
                final SerialFlowControl flowcontrol;

                flowcontrol = arrayFlowControl[i];

                if (name.equals(flowcontrol.getName()))
                    {
                    flowControl = flowcontrol;
                    boolFoundIt = true;
                    }
                }
            }

        return (flowControl);
        }


    /***********************************************************************************************
     * Get the SerialFlowControl enum corresponding to the specified SerialFlowControl value.
     * Return FLOW_NONE if the SerialFlowControl value is invalid.
     *
     * @param flowcontrol
     *
     * @return SerialFlowControl
     */

    public static SerialFlowControl getSerialFlowControlForValue(final String flowcontrol)
        {
        SerialFlowControl flowControl;

        flowControl = FLOW_NONE;

        if ((flowcontrol != null)
            && (!FrameworkStrings.EMPTY_STRING.equals(flowcontrol)))
            {
            final SerialFlowControl[] arrayFlowControl;
            boolean boolFoundIt;

            arrayFlowControl = values();
            boolFoundIt = false;

            for (int i = 0;
                 (!boolFoundIt) && (i < arrayFlowControl.length);
                 i++)
                {
                final SerialFlowControl flow;

                flow = arrayFlowControl[i];

                if (flow.getName().equals(flowcontrol))
                    {
                    flowControl = flow;
                    boolFoundIt = true;
                    }
                }
            }

        return (flowControl);
        }


    /***********************************************************************************************
     * Construct a SerialFlowControl.
     *
     * @param flowcontrol
     * @param displayname
     */

    private SerialFlowControl(final String flowcontrol,
                              final String displayname)
        {
        this.strFlowControl = flowcontrol;
        this.strName = displayname;
        }


    /***********************************************************************************************
     * Get the FlowControl.
     *
     * @return String
     */

    public String getFlowControl()
        {
        return (this.strFlowControl);
        }


    /***********************************************************************************************
     * Get the FlowControl Name.
     *
     * @return String
     */

    public String getName()
        {
        return (this.strName);
        }


    /***********************************************************************************************
     * Get the FlowControl Name.
     *
     * @return String
     */

    public String toString()
        {
        return (this.strName);
        }
    }
