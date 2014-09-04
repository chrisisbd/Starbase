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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.scanner;


import org.lmn.fc.common.comparators.ReportRowsByColumn;
import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.NetworkScannerUIComponentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.widgets.IpTextField;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.registry.RegistryModelUtilities;
import org.lmn.fc.ui.UIComponentPlugin;
import org.lmn.fc.ui.reports.ReportTableHelper;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;
import java.util.Vector;


/***************************************************************************************************
 * NetworkScannerHelper.
 */

public final class NetworkScannerHelper implements FrameworkConstants,
                                                   FrameworkStrings,
                                                   FrameworkMetadata,
                                                   FrameworkSingletons
    {
    private static final int WIDTH_PORT = 40;
    private static final int PORT_MIN = 1200;
    private static final int PORT_MAX = 49151;


    /***********************************************************************************************
     * Initialise the IpTextField for entry of the Address.
     *
     * @param scannerui
     * @param initaddress
     * @param startaddress
     * @param endaddress
     * @param port
     * @param rescanbutton
     * @param tooltip
     * @param defaultaddress
     *
     * @return IpTextField
     */

    public static IpTextField initialiseAddress(final NetworkScannerUIComponentInterface scannerui,
                                                final IpTextField initaddress,
                                                final IpTextField startaddress,
                                                final IpTextField endaddress,
                                                final JTextField port,
                                                final JButton rescanbutton,
                                                final String tooltip,
                                                final String defaultaddress)
        {
        final String SOURCE = "NetworkScannerHelper.initialiseAddress() ";
        final DocumentListener listener;

        // Allow free-text entry for Address
        initaddress.setToolTipText(tooltip);

        // We should have an initialised panel by now
        if (scannerui != null)
            {
            // Copy the style of the NetworkScanner Panel
            initaddress.setBackground(Color.white);
            initaddress.setForeground(scannerui.getTextColour().getColor());
            initaddress.setFont(scannerui.getReportFont().getFont());
            }

        // Set the default Address
        initaddress.setIpAddressAsString(defaultaddress);

        // Text Field Listener
        listener = new DocumentListener()
            {
            public void insertUpdate(final DocumentEvent event)
                {
                //LOGGER.logTimedEvent("Address insertUpdate");
                rescanbutton.setEnabled(isValidRangeAndPortFromUI(startaddress, endaddress, port));
                }

            public void removeUpdate(final DocumentEvent event)
                {
                //LOGGER.logTimedEvent("Address removeUpdate DO NOTHING");
                //rescanbutton.setEnabled(isValidRangeAndPortFromUI(startaddress, endaddress, port));
                }

            public void changedUpdate(final DocumentEvent event)
                {
                //LOGGER.logTimedEvent("Address changedUpdate");
                rescanbutton.setEnabled(isValidRangeAndPortFromUI(startaddress, endaddress, port));
                }
            };

        // Add this listener to each of the IpTextField Octet fields
        initaddress.addDocumentListener(listener);

        return (initaddress);
        }


    /***********************************************************************************************
     * Initialise the JTextField for entry of the Port.
     *
     * @param scannerui
     * @param port
     * @param startaddress
     * @param endaddress
     * @param rescanbutton
     * @param tooltip
     * @param defaultport
     *
     * @return JTextField
     */

    public static JTextField initialisePort(final NetworkScannerUIComponentInterface scannerui,
                                            final JTextField port,
                                            final IpTextField startaddress,
                                            final IpTextField endaddress,
                                            final JButton rescanbutton,
                                            final String tooltip,
                                            final int defaultport)
        {
        final String SOURCE = "NetworkScannerHelper.initialisePort() ";
        final DocumentListener listener;

        // Allow free-text entry for Port
        port.setPreferredSize(new Dimension(WIDTH_PORT, UIComponentPlugin.HEIGHT_TOOLBAR_ICON - 4));
        port.setMaximumSize(new Dimension(WIDTH_PORT, UIComponentPlugin.HEIGHT_TOOLBAR_ICON - 4));
        port.setMinimumSize(new Dimension(WIDTH_PORT, UIComponentPlugin.HEIGHT_TOOLBAR_ICON - 4)) ;
        port.setMargin(new Insets(0, 5, 0, 5));
        port.setToolTipText(tooltip);

        // We should have an initialised panel by now
        if (scannerui != null)
            {
            // Copy the style of the NetworkScanner Panel
            port.setForeground(scannerui.getTextColour().getColor());
            port.setFont(scannerui.getReportFont().getFont());
            }

        // Set the default Port
        port.setText(Integer.toString(defaultport));

        // Text Field Listener
        listener = new DocumentListener()
            {
            public void insertUpdate(final DocumentEvent event)
                {
                //LOGGER.logTimedEvent("Port insertUpdate");
                rescanbutton.setEnabled(isValidRangeAndPortFromUI(startaddress, endaddress, port));
                }

            public void removeUpdate(final DocumentEvent event)
                {
                //LOGGER.logTimedEvent("Port removeUpdate");
                rescanbutton.setEnabled(isValidRangeAndPortFromUI(startaddress, endaddress, port));
                }

            public void changedUpdate(final DocumentEvent event)
                {
                //LOGGER.logTimedEvent("Port changedUpdate");
                rescanbutton.setEnabled(isValidRangeAndPortFromUI(startaddress, endaddress, port));
                }
            };

        port.getDocument().addDocumentListener(listener);

        return (port);
        }


    /***********************************************************************************************
     * Check that the Address range and Port selection are valid.
     *
     * @param startaddress
     * @param endaddress
     * @param port
     */

    public static boolean isValidRangeAndPortFromUI(final IpTextField startaddress,
                                                    final IpTextField endaddress,
                                                    final JTextField port)
        {
        final String SOURCE = "NetworkScannerHelper.isValidRangeAndPortFromUI() ";
        boolean boolIsValid;

        //LOGGER.logTimedEvent(SOURCE);

        try
            {
            final long longAddressStart;
            final long longAddressEnd;

            // ToDO IPv6
            longAddressStart = ipV4ToLong(startaddress.getIpAddressAsString());
            longAddressEnd = ipV4ToLong(endaddress.getIpAddressAsString());

            boolIsValid = isValidRangeAndPort(longAddressStart,
                                              longAddressEnd,
                                              Integer.parseInt(port.getText().trim()));
            }

        catch (UnknownHostException exception)
            {
            LOGGER.logTimedEvent(SOURCE + "UnknownHostException");
            boolIsValid = false;
            }

        catch (NumberFormatException exception)
            {
            LOGGER.logTimedEvent(SOURCE + "NumberFormatException");
            boolIsValid = false;
            }

        return (boolIsValid);
        }


    /***********************************************************************************************
     * Check that the Address range and Port selection are valid.
     *
     * @param startaddress
     * @param endaddress
     * @param port
     */

    public static boolean isValidRangeAndPort(final long startaddress,
                                              final long endaddress,
                                              final int port)
        {
        final String SOURCE = "NetworkScannerHelper.isValidRangeAndPortFrom() ";
        boolean boolIsValid;

        boolIsValid = false;

        //LOGGER.logTimedEvent(SOURCE);
        try
            {
            //LOGGER.logTimedEvent(SOURCE + "Long Octets [startaddress=" + Long.toHexString(startaddress) + "]");
            //LOGGER.logTimedEvent(SOURCE + "Long Octets [endaddress=" + Long.toHexString(endaddress) + "]");

            if ((startaddress >= 0)
                && (endaddress > 0)
                && (endaddress > startaddress)
                && ((endaddress - startaddress) <= 254L))
                {
                //LOGGER.logTimedEvent(SOURCE + "Check port " + port);

                // Now check the Port, but only if the addresses were valid
                boolIsValid = ((port >= PORT_MIN)
                               && (port <= PORT_MAX));
                }
            else
                {
                //LOGGER.logTimedEvent(SOURCE + "Address range error");
                }
            }

        catch (NumberFormatException exception)
            {
            LOGGER.logTimedEvent(SOURCE + "NumberFormatException");
            boolIsValid = false;
            }

        return (boolIsValid);
        }


    /***********************************************************************************************
     * Convert an IP Address to a long.
     *
     * @param ipaddress
     *
     * @return long
     *
     * @throws UnknownHostException
     * @throws SecurityException
     */

    public static long ipV4ToLong(final String ipaddress) throws UnknownHostException,
                                                                 SecurityException
        {
        final String SOURCE = "NetworkScannerHelper.ipV4ToLong() ";
        final byte[] arrayBytes;
        final int octet1;
        final int octet2;
        final int octet3;
        final int octet4;
        long longAddress;

        //LOGGER.logTimedEvent(SOURCE + "String [address=" + ipaddress + "]");

        arrayBytes = InetAddress.getByName(ipaddress).getAddress();
        //LOGGER.logTimedEvent(SOURCE + "Bytes [address=" + Utilities.byteArrayToSpacedHex(arrayBytes) + "]");

        octet1 = (arrayBytes[0] & 0xFF) << 24;
        octet2 = (arrayBytes[1] & 0xFF) << 16;
        octet3 = (arrayBytes[2] & 0xFF) << 8;
        octet4 = arrayBytes[3]  & 0xFF;

        longAddress = octet1 | octet2 | octet3 | octet4;
        longAddress = longAddress & 0x00000000FFFFFFFFL;

        //LOGGER.logTimedEvent(SOURCE + "Long [address=" + longAddress + "]");

        return (longAddress);
        }


    /***********************************************************************************************
     * Convert a long to an IP Address, as four dotted octets.
     *
     * @param ipaddress
     *
     * @return String
     */

    public static String longToIPv4(final long ipaddress)
        {
        final String SOURCE = "NetworkScannerHelper.longToIPv4() ";
        final int octet1;
        final int octet2;
        final int octet3;
        final int octet4;
        final String strIP;

        //LOGGER.logTimedEvent(SOURCE + "Long [address=" + ipaddress + "]");

        octet1 = (int)((ipaddress & 0x00000000FF000000L) >>> 24);
        octet2 = (int)((ipaddress & 0x0000000000FF0000L) >>> 16);
        octet3 = (int)((ipaddress & 0x000000000000FF00L) >>> 8);
        octet4 = (int)(ipaddress  & 0x00000000000000FFL);

        strIP = String.valueOf(octet1) + "." + octet2 + "." + octet3 + "." + octet4;

        //LOGGER.logTimedEvent(SOURCE + "String [address=" + strIP + "]");

        return (strIP);
        }


    /***********************************************************************************************
     * Convert the specified List of NetworkScannerData to a Report format.
     * This occurs every time a single IP address is attempted.
     *
     * @param listdata
     * @param columncount
     *
     * @return Vector<Vector>
     */

    public static Vector<Vector> convertScanDataToReport(final List<NetworkScannerData> listdata,
                                                         final int columncount)
        {
        final String SOURCE = "NetworkScannerHelper.convertScanDataToReport() ";
        final int INDEX_IPADDRESS = 1;
        Vector<Vector> vecReport;

        vecReport = new Vector<Vector>(1);

        if (listdata != null)
            {
            // Resize the report Vector to suit the scan data List
            vecReport = new Vector<Vector>(listdata.size());

            // The incoming data must be converted to a form suitable for a Report
            for (int intRowIndex = 0;
                 intRowIndex < listdata.size();
                 intRowIndex++)
                {
                if (listdata.get(intRowIndex) !=  null)
                    {
                    final NetworkScannerData scannerData;

                    scannerData = listdata.get(intRowIndex);

                    if (scannerData != null)
                        {
                        final Vector<Object> vecReportRow;
                        final ImageIcon imageIcon;

                        // Build the Report row
                        vecReportRow = new Vector<Object>(columncount);

                        imageIcon = RegistryModelUtilities.getCommonIcon(scannerData.getEventStatus().getIconFilename());
                        vecReportRow.add(imageIcon);

                        if (EventStatus.INFO.equals(scannerData.getEventStatus()))
                            {
                            // No HTML tags on the IP Address, because it is used for sorting
                            vecReportRow.add(longToIPv4(scannerData.getIPAddress()));
                            vecReportRow.add(Integer.toString(scannerData.getTargetPort()));
                            vecReportRow.add(HTML_PREFIX_BOLD + scannerData.getStatus() + HTML_SUFFIX_BOLD);
                            vecReportRow.add(HTML_PREFIX_BOLD + scannerData.getHostname() + HTML_SUFFIX_BOLD);
                            vecReportRow.add("MAC");
                            vecReportRow.add("Version");
                            vecReportRow.add(scannerData.getDateAsString());
                            vecReportRow.add(scannerData.getTimeAsString());
                            }
                        else
                            {
                            // Indicate that something failed, somewhere
                            vecReportRow.add(longToIPv4(scannerData.getIPAddress()));
                            vecReportRow.add(Integer.toString(scannerData.getTargetPort()));
                            vecReportRow.add(ReportTableHelper.greyCell(scannerData.getStatus(), true));
                            vecReportRow.add(ReportTableHelper.greyCell(scannerData.getHostname(), true));
                            vecReportRow.add(ReportTableHelper.greyCell("MAC", true));
                            vecReportRow.add(ReportTableHelper.greyCell("Version", true));
                            vecReportRow.add(ReportTableHelper.greyCell(scannerData.getDateAsString(), true));
                            vecReportRow.add(ReportTableHelper.greyCell(scannerData.getTimeAsString(), true));
                            }

                        vecReport.add(vecReportRow);
                        }
                    else
                        {
                        LOGGER.error(SOURCE + "Invalid Report row");
                        }
                    }
                else
                    {
                    LOGGER.error(SOURCE + "Invalid Report Vector");
                    }
                }
            }

        // Sort the report by the IP address column
        Collections.sort(vecReport,
                         new ReportRowsByColumn(INDEX_IPADDRESS));

        return (vecReport);
        }


    /***********************************************************************************************
     * Indicate if the UserObject data are a representation of a Network Scan.
     * A bit of a bodge, but it is progress...
     *
     * @param userobject
     *
     * @return boolean
     */

    public static boolean isUserObjectNetworkScan(final Object userobject)
        {
        final boolean boolIsScan;

        // If this is all true, there's a good chance the data are a Scan
        // Recall that Type Erasure means we can't fully generify this
        boolIsScan = ((userobject != null)
                       && (userobject instanceof java.util.List)
                       && (((List)userobject).size() > 0)
                       && (((List)userobject).get(0) instanceof NetworkScannerData));

        return (boolIsScan);
        }
    }
