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

package org.lmn.fc.frameworks.starbase.plugins.observatory.common;


import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.ui.UIComponentPlugin;


/**********************************************************************************************
 * Enum for IP Version configuration.
 */

public enum IPVersion
    {
    // 192.168.123.123  --> 4,294,967,296 addresses
    IPV4 ("IPv4", 4, 3, '.', 10, "\\", 30, 110 + (8 * 3), UIComponentPlugin.HEIGHT_TOOLBAR_ICON - 4),

    // 2001:0db8:85a3:0042:0000:8a2e:0370:7334 --> 340,282,366,920,938,463,463,374,607,431,768,211,456 addresses!
    IPV6 ("IPv6", 8, 4, ':', 16, "",   36, 268 + (8 * 7), UIComponentPlugin.HEIGHT_TOOLBAR_ICON - 4);


    private final String strName;
    private final int intFieldCount;
    private final int intFieldWidth;
    private final char charSeparator;
    private final int intRadix;
    private final String strRegexPrefix;
    private final int intOctetWidth;
    private final int intComponentWidth;
    private final int intComponentHeight;


    /***********************************************************************************************
     * Get the IPVersion enum corresponding to the specified IPVersion name.
     * Return NULL if the IPVersion name is not found.
     *
     * @param name
     *
     * @return IPVersion
     */

    public static IPVersion getIPVersionForName(final String name)
        {
        IPVersion ipVersion;

        ipVersion = null;

        if ((name != null)
            && (!FrameworkStrings.EMPTY_STRING.equals(name)))
            {
            final IPVersion[] types;
            boolean boolFoundIt;

            types = values();
            boolFoundIt = false;

            for (int i = 0;
                 (!boolFoundIt) && (i < types.length);
                 i++)
                {
                final IPVersion version;

                version = types[i];

                if (name.equals(version.getName()))
                    {
                    ipVersion = version;
                    boolFoundIt = true;
                    }
                }
            }

        return (ipVersion);
        }


    /***********************************************************************************************
     * Pad a String representation of an IPv4 IP Address and Port with trailing underscores
     * to suit storage in the Instrument XML IPAddress element.
     * The format is "192.168.1.68:1205____".
     * The incoming IPAddress and Port are ASSUMED to be of the correct formats and lengths.
     * ToDo Add IPVersion parameter.
     *
     * @param ipaddress
     * @param port
     *
     * @return String
     */

    public static String addTrailingPaddingToIPAddressAndPort(final String ipaddress,
                                                              final String port)
        {
        final int FIELD_WIDTH_IPV4 = 21;    // 000.000.000.000:00000 ToDo calculate this from the enum
        final StringBuffer buffer;
        final int intLengthShortfall;

        buffer = new StringBuffer();

        buffer.append(ipaddress);
        buffer.append(":");
        buffer.append(port);
        intLengthShortfall = FIELD_WIDTH_IPV4 - buffer.length();

        // Do we need to pad with trailing underscores?

        for (int intPadIndex = 0;
             intPadIndex < intLengthShortfall;
             intPadIndex++)
            {
            buffer.append("_");
            }

        return (buffer.toString());
        }


    /***********************************************************************************************
     * Strip any trailing padding underscores from an IPv4 IPAddress and Port.
     * The format is "192.168.1.68:1205____".
     * The incoming IPAddress and Port are ASSUMED to be of the correct formats and lengths.
     *
     * @param ipaddressandport
     *
     * @return String
     */

    public static String stripTrailingPaddingFromIPAddressAndPort(final String ipaddressandport)
        {
        final StringBuffer buffer;

        buffer = new StringBuffer();

        if (ipaddressandport.contains("_"))
            {
            buffer.append(ipaddressandport.substring(0, ipaddressandport.indexOf("_")));
            }
        else
            {
            // There are no underscores to strip
            buffer.append(ipaddressandport);
            }

        return (buffer.toString());
        }


    /*******************************************************************************************
     * IPVersion.
     *
     * @param name
     * @param fieldcount
     * @param fieldwidth
     * @param separator
     * @param radix
     * @param regexprefix
     * @param octetwidth
     * @param componentwidth
     * @param componentheight
     */

    private IPVersion(final String name,
                      final int fieldcount,
                      final int fieldwidth,
                      final char separator,
                      final int radix,
                      final String regexprefix,
                      final int octetwidth,
                      final int componentwidth,
                      final int componentheight)
        {
        this.strName = name;
        this.intFieldCount = fieldcount;
        this.intFieldWidth = fieldwidth;
        this.charSeparator = separator;
        this.intRadix = radix;
        this.strRegexPrefix = regexprefix;
        this.intOctetWidth = octetwidth;
        this.intComponentWidth = componentwidth;
        this.intComponentHeight = componentheight;
        }


    /*******************************************************************************************
     * Get the Version Name.
     *
     * @return String
     */

    public String getName()
        {
        return (this.strName);
        }


    /*******************************************************************************************
     * Get the Octet Field count.
     *
     * @return int
     */

    public int getFieldCount()
        {
        return (this.intFieldCount);
        }


    /*******************************************************************************************
     * Get the Octet Field width in characters.
     *
     * @return int
     */

    public int getFieldWidth()
        {
        return (this.intFieldWidth);
        }


    /*******************************************************************************************
     * Get the separator character.
     *
     * @return char
     */

    public char getSeparatorChar()
        {
        return (this.charSeparator);
        }


    /*******************************************************************************************
     * Get the radix used for the Octet fields.
     *
     * @return int
     */

    public int getRadix()
        {
        return (this.intRadix);
        }


    /*******************************************************************************************
     * Get the prefix of gthe Regex expression used to split the IP address into Octets.
     *
     * @return String
     */

    public String getRegexPrefix()
        {
        return (this.strRegexPrefix);
        }


    /*******************************************************************************************
     * Get the width of the Octet field in pixels.
     *
     * @return int
     */

    public int getOctetWidth()
        {
        return (this.intOctetWidth);
        }


    /*******************************************************************************************
     * Get the width of the component in pixels.
     *
     * @return int
     */

    public int getComponentWidth()
        {
        return (this.intComponentWidth);
        }


    /*******************************************************************************************
     * Get the height of the component in pixels.
     *
     * @return int
     */

    public int getComponentHeight()
        {
        return (this.intComponentHeight);
        }
    }


