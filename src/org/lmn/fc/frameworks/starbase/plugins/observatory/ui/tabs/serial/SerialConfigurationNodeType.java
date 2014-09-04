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


import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxUtils;
import org.lmn.fc.common.constants.FrameworkStrings;

import java.awt.*;
import java.util.Hashtable;
import java.util.Map;


/***************************************************************************************************
 * SerialConfigurationNodeType.
 */

public enum SerialConfigurationNodeType
    {
    STARIBUS_INSTRUMENT ("staribusinstrument",  createStaribusInstrumentStyle(), 230, 40),
    STARIBUS_HUB        ("staribushub",         createStaribusHubStyle(),        180, 90),
    SERIAL_INSTRUMENT   ("serialinstrument",    createSerialInstrumentStyle(),   230, 40),
    SERIAL_PORT         ("serialport",          createSerialPortStyle(),         200, 40),
    UNKNOWN_PORT        ("unknownport",         createUnknownPortStyle(),        200, 40);

    private final String strName;
    private final Map<String, Object> mapStyle;
    private final int intVertexWidth;
    private final int intVertexHeight;


    /*********************************************************************************************/
    /* Instruments                                                                               */
    /**********************************************************************************************
     * Create a new style for Staribus Instruments.
     *
     * @return the created style.
     */

    private static Hashtable<String,Object> createStaribusInstrumentStyle()
        {
        final Hashtable<String, Object> style;

        style = new Hashtable<String, Object>(10);

        style.put(mxConstants.STYLE_FILLCOLOR, mxUtils.getHexColorString(new Color(142, 207, 237)));
        style.put(mxConstants.STYLE_STROKECOLOR, mxUtils.getHexColorString(new Color(0, 0, 234)));
        style.put(mxConstants.STYLE_STROKEWIDTH, 1.5);
        style.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_RECTANGLE);
        style.put(mxConstants.STYLE_PERIMETER, mxConstants.PERIMETER_RECTANGLE);
        style.put(mxConstants.STYLE_CLONEABLE, 0);

        style.put(mxConstants.STYLE_FONTFAMILY, "Courier");
        style.put(mxConstants.STYLE_FONTSIZE, 14);

        return (style);
        }


    /**********************************************************************************************
     * Create a new style for Serial Instruments.
     *
     * @return the created style.
     */

    private static Hashtable<String,Object> createSerialInstrumentStyle()
        {
        final Hashtable<String, Object> style;

        style = new Hashtable<String, Object>(10);

        style.put(mxConstants.STYLE_FILLCOLOR, mxUtils.getHexColorString(new Color(92, 170, 230)));
        style.put(mxConstants.STYLE_STROKECOLOR, mxUtils.getHexColorString(new Color(0, 0, 234)));
        style.put(mxConstants.STYLE_STROKEWIDTH, 1.5);
        style.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_RECTANGLE);
        style.put(mxConstants.STYLE_PERIMETER, mxConstants.PERIMETER_RECTANGLE);
        style.put(mxConstants.STYLE_CLONEABLE, 0);

        style.put(mxConstants.STYLE_FONTFAMILY, "Courier");
        style.put(mxConstants.STYLE_FONTSIZE, 14);

        return (style);
        }


    /*********************************************************************************************/
    /* Ports                                                                                     */
    /**********************************************************************************************
     * Create a new style for the Staribus Serial Hub.
     *
     * @return Hashtable<String, Object>
     */

    private static Hashtable<String, Object> createStaribusHubStyle()
        {
        final Hashtable<String, Object> style;

        style = new Hashtable<String, Object>(10);

        style.put(mxConstants.STYLE_FILLCOLOR, mxUtils.getHexColorString(new Color(142, 207, 237)));
        style.put(mxConstants.STYLE_STROKECOLOR, mxUtils.getHexColorString(new Color(0, 110, 0)));
        style.put(mxConstants.STYLE_STROKEWIDTH, 1.5);
        style.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_RECTANGLE);
        style.put(mxConstants.STYLE_PERIMETER, mxConstants.PERIMETER_RECTANGLE);
        style.put(mxConstants.STYLE_ROUNDED, true);
        style.put(mxConstants.STYLE_CLONEABLE, 0);

        style.put(mxConstants.STYLE_FONTFAMILY, "Courier");
        style.put(mxConstants.STYLE_FONTSIZE, 16);

        return (style);
        }


    /**********************************************************************************************
     * Create a new style for Serial Ports.
     *
     * @return Hashtable<String, Object>
     */

    private static Hashtable<String, Object> createSerialPortStyle()
        {
        final Hashtable<String, Object> style;

        style = new Hashtable<String, Object>(10);

        style.put(mxConstants.STYLE_FILLCOLOR, mxUtils.getHexColorString(new Color(20, 199, 158)));
        style.put(mxConstants.STYLE_STROKECOLOR, mxUtils.getHexColorString(new Color(0, 110, 0)));
        style.put(mxConstants.STYLE_STROKEWIDTH, 1.5);
        style.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_RECTANGLE);
        style.put(mxConstants.STYLE_PERIMETER, mxConstants.PERIMETER_RECTANGLE);
        style.put(mxConstants.STYLE_ROUNDED, true);
        style.put(mxConstants.STYLE_CLONEABLE, 0);

        style.put(mxConstants.STYLE_FONTFAMILY, "Courier");
        style.put(mxConstants.STYLE_FONTSIZE, 14);

        return (style);
        }


    /**********************************************************************************************
     * Create a new style for Unknown Ports.
     *
     * @return Hashtable<String, Object>
     */

    private static Hashtable<String, Object> createUnknownPortStyle()
        {
        final Hashtable<String, Object> style;

        style = new Hashtable<String, Object>(10);

        style.put(mxConstants.STYLE_FILLCOLOR, mxUtils.getHexColorString(new Color(212, 212, 212)));
        style.put(mxConstants.STYLE_STROKECOLOR, mxUtils.getHexColorString(new Color(0, 110, 0)));
        style.put(mxConstants.STYLE_STROKEWIDTH, 1.5);
        style.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_RECTANGLE);
        style.put(mxConstants.STYLE_PERIMETER, mxConstants.PERIMETER_RECTANGLE);
        style.put(mxConstants.STYLE_ROUNDED, true);
        style.put(mxConstants.STYLE_CLONEABLE, 0);

        style.put(mxConstants.STYLE_FONTFAMILY, "Courier");
        style.put(mxConstants.STYLE_FONTSTYLE, mxConstants.FONT_ITALIC);
        style.put(mxConstants.STYLE_FONTCOLOR, mxUtils.getHexColorString(Color.RED));
        style.put(mxConstants.STYLE_FONTSIZE, 14);

        return (style);
        }


    /***********************************************************************************************
     * Get the SerialConfigurationNodeType enum corresponding to the specified SerialConfigurationNodeType name.
     * Return NULL if the SerialConfigurationNodeType name is not found.
     *
     * @param name
     *
     * @return SerialConfigurationNodeType
     */

    public static SerialConfigurationNodeType getSerialConfigurationNodeTypeForName(final String name)
        {
        SerialConfigurationNodeType nodeType;

        nodeType = null;

        if ((name != null)
            && (!FrameworkStrings.EMPTY_STRING.equals(name)))
            {
            final SerialConfigurationNodeType[] arrayNodeTypes;
            boolean boolFoundIt;

            arrayNodeTypes = values();
            boolFoundIt = false;

            for (int i = 0;
                 (!boolFoundIt) && (i < arrayNodeTypes.length);
                 i++)
                {
                final SerialConfigurationNodeType nodetype;

                nodetype = arrayNodeTypes[i];

                if (name.equals(nodetype.getName()))
                    {
                    nodeType = nodetype;
                    boolFoundIt = true;
                    }
                }
            }

        return (nodeType);
        }


    /***********************************************************************************************
     * Construct a SerialConfigurationNodeType.
     *
     * @param name
     * @param style
     * @param vertexwidth
     * @param vertexheight
     */

    private SerialConfigurationNodeType(final String name,
                                        final Map<String, Object> style,
                                        final int vertexwidth,
                                        final int vertexheight)
        {
        this.strName = name;
        this.mapStyle = style;
        this.intVertexWidth = vertexwidth;
        this.intVertexHeight = vertexheight;
        }


    /***********************************************************************************************
     * Get the NodeType Name.
     *
     * @return String
     */

    public String getName()
        {
        return (this.strName);
        }


    /***********************************************************************************************
     * Get the Style Map.
     *
     * @return Map<String, Object>
     */

    public Map<String, Object> getStyleMap()
        {
        return (this.mapStyle);
        }


    /***********************************************************************************************
     * Get the Vertex Width.
     *
     * @return int
     */

    public int getVertexWidth()
        {
        return (this.intVertexWidth);
        }


    /***********************************************************************************************
     * Get the Vertex Height.
     *
     * @return int
     */

    public int getVertexHeight()
        {
        return (this.intVertexHeight);
        }
    }
