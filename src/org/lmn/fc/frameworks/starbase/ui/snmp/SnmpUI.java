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

package org.lmn.fc.frameworks.starbase.ui.snmp;

import org.lmn.fc.common.exceptions.FrameworkException;
import org.lmn.fc.common.net.snmp.*;
import org.lmn.fc.model.registry.impl.RegistryModel;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.ui.components.UIComponent;
import org.lmn.fc.ui.components.UIComponentHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.StringTokenizer;
import java.util.Vector;


/***************************************************************************************************
 * The SnmpUI.
 */

public final class SnmpUI extends UIComponent
                          implements ActionListener,
                                     Runnable
    {
    private static final String OBJECT_ID_START = "1.3.6.1.2.1";
    private static final String PACKAGE_SNMP = RegistryModel.PACKAGE_ROOT + RegistryModel.PACKAGE_SNMP;

    private static final int LOG_ENTRY_SIZE = 4;
    private static final int COLUMNS_TEXT = 20;
    private static final double WEIGHT_CENTRE = 0.5;

    private static final String ACTION_GET_DATA = "data";
    private static final String ACTION_GET_NEXT = "next";
    private static final String ACTION_GET_TABLE = "table";
    private static final String ACTION_SET_VALUE = "value";
    private static final String ACTION_GET_TREEWALK_DATA = "treewalk";

    private static final String MSG_INVALID_DATA = "<html><i>Invalid data</i></html>";

    private JTextField textHostID;
    private JTextField textCommunity;
    private JTextField textOID;
    private JTextField textValue;
    private JButton buttonTreewalk;
    private JComboBox comboValueType;
    private SnmpObjectReport reportSnmpObject;

    private TaskPlugin pluginTask;
    private String strResourceKey;
    private Thread threadTreewalk;


    /***********************************************************************************************
     * Log the details of an SNMP Object.
     *
     * @param oid
     * @param classname
     * @param value
     *
     * @return Vector<Object>
     */

    private static Vector<Object> logSNMPObject(final SNMPObjectIdentifier oid,
                                                final String classname,
                                                final SNMPObject value)
        {
        final Vector<Object> vecLogEntry;

        vecLogEntry = new Vector<Object>(LOG_ENTRY_SIZE);

        if ((oid != null)
            && (classname != null)
            && (value != null))
            {
            if (SNMPOctetString.class.getName().equals(classname))
                {
                String snmpString = value.toString();

                // Truncate at first null character
                final int indexNull = snmpString.indexOf(NULL);

                if (indexNull >= 0)
                    {
                    snmpString = snmpString.substring(0, indexNull);
                    }

                vecLogEntry.add(oid.toString());
                vecLogEntry.add(snmpString);
                vecLogEntry.add(((SNMPOctetString)value).toHexString());
                vecLogEntry.add(classname.substring(PACKAGE_SNMP.length()));
                }
            else
                {
                vecLogEntry.add(oid.toString());
                vecLogEntry.add(value.toString());
                vecLogEntry.add(SPACE);
                vecLogEntry.add(classname.substring(PACKAGE_SNMP.length()));
                }
            }
        else
            {
            vecLogEntry.add(MSG_INVALID_DATA);
            vecLogEntry.add(SPACE);
            vecLogEntry.add(SPACE);
            vecLogEntry.add(SPACE);
            }

        return (vecLogEntry);
        }


    /***********************************************************************************************
     * Log a message.
     *
     * @param message
     *
     * @return Vector<Object>
     */

    private static Vector<Object> logMessage(final String message)
        {
        final Vector<Object> vecLogEntry;

        vecLogEntry = new Vector<Object>(LOG_ENTRY_SIZE);

        if (message != null)
            {
            vecLogEntry.add(message);
            vecLogEntry.add(SPACE);
            vecLogEntry.add(SPACE);
            vecLogEntry.add(SPACE);
            }
        else
            {
            vecLogEntry.add(MSG_INVALID_DATA);
            vecLogEntry.add(SPACE);
            vecLogEntry.add(SPACE);
            vecLogEntry.add(SPACE);
            }

        return (vecLogEntry);
        }


    /***********************************************************************************************
     * Construct the SnmpUI.
     *
     * @param task
     * @param resourcekey
     */

    public SnmpUI(final TaskPlugin task,
                  final String resourcekey)
        {
        super();

        if ((task == null)
            || (!task.validatePlugin())
            || (resourcekey == null))
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_NULL);
            }

        this.pluginTask = task;
        this.strResourceKey = resourcekey;
        // ToDo Consider SwingWorker
        this.threadTreewalk = new Thread(REGISTRY.getThreadGroup(),
                                         this,
                                         "Thread SnmpUI");
        }


    /***********************************************************************************************
     * Respond to button pushes, menu selections.
     *
     * @param event
     */

    public void actionPerformed(final ActionEvent event)
        {
        final String command = event.getActionCommand();

        //------------------------------------------------------------------------------------------

        if (command.equals(ACTION_GET_DATA))
            {
            try
                {
                getHostTask().setStatus(EMPTY_STRING);

                final int version = 0;    // SNMPv1
                final String community = textCommunity.getText();
                final InetAddress hostAddress = InetAddress.getByName(textHostID.getText());
                final SNMPv1CommunicationInterface comInterface = new SNMPv1CommunicationInterface(version, hostAddress, community);

                final StringTokenizer st = new StringTokenizer(textOID.getText(), " ,;");

                while (st.hasMoreTokens())
                    {
                    final String itemID = st.nextToken();
                    final SNMPVarBindList newVars = comInterface.getMIBEntry(itemID);
                    final SNMPSequence pair = (SNMPSequence) (newVars.getSNMPObjectAt(0));
                    final SNMPObjectIdentifier snmpOID = (SNMPObjectIdentifier) pair.getSNMPObjectAt(0);
                    final SNMPObject snmpValue = pair.getSNMPObjectAt(1);
                    final String typeString = snmpValue.getClass().getName();

                    reportSnmpObject.logger(logSNMPObject(snmpOID, typeString, snmpValue));
                    }
                }

            catch (InterruptedIOException e)
                {
                reportSnmpObject.logger(logMessage("Interrupted during retrieval"));
                }

            catch (UnknownHostException e)
                {
                reportSnmpObject.logger(logMessage("UnknownHostException"));
                }

            catch (IOException e)
                {
                reportSnmpObject.logger(logMessage("IOException"));
                }

            catch (SNMPBadValueException e)
                {
                reportSnmpObject.logger(logMessage("SNMPBadValueException"));
                }

            catch (SNMPGetException e)
                {
                reportSnmpObject.logger(logMessage("SNMPGetException"));
                }

            reportSnmpObject.refreshTable();
            }

        //------------------------------------------------------------------------------------------

        if (command.equals(ACTION_GET_NEXT))
            {
            try
                {
                getHostTask().setStatus(EMPTY_STRING);

                final String community = textCommunity.getText();
                final int version = 0;    // SNMPv1
                final InetAddress hostAddress = InetAddress.getByName(textHostID.getText());
                final SNMPv1CommunicationInterface comInterface = new SNMPv1CommunicationInterface(version, hostAddress, community);

                final StringTokenizer st = new StringTokenizer(textOID.getText(), " ,;");

                while (st.hasMoreTokens())
                    {
                    final String itemID = st.nextToken();
                    final SNMPVarBindList newVars = comInterface.getNextMIBEntry(itemID);
                    final SNMPSequence pair = (SNMPSequence) (newVars.getSNMPObjectAt(0));
                    final SNMPObjectIdentifier snmpOID = (SNMPObjectIdentifier) pair.getSNMPObjectAt(0);
                    final SNMPObject snmpValue = pair.getSNMPObjectAt(1);
                    final String typeString = snmpValue.getClass().getName();

                    reportSnmpObject.logger(logSNMPObject(snmpOID, typeString, snmpValue));
                    }
                }

            catch (InterruptedIOException e)
                {
                reportSnmpObject.logger(logMessage("Interrupted during retrieval"));
                }

            catch (UnknownHostException e)
                {
                reportSnmpObject.logger(logMessage("UnknownHostException"));
                }

            catch (IOException e)
                {
                reportSnmpObject.logger(logMessage("IOException"));
                }

            catch (SNMPBadValueException e)
                {
                reportSnmpObject.logger(logMessage("SNMPBadValueException"));
                }

            catch (SNMPGetException e)
                {
                reportSnmpObject.logger(logMessage("SNMPGetException"));
                }

            reportSnmpObject.refreshTable();
            }

        //------------------------------------------------------------------------------------------

        if (command.equals(ACTION_GET_TABLE))
            {
            try
                {
                getHostTask().setStatus(EMPTY_STRING);

                final String community = textCommunity.getText();
                final int version = 0;    // SNMPv1
                final InetAddress hostAddress = InetAddress.getByName(textHostID.getText());
                final SNMPv1CommunicationInterface comInterface = new SNMPv1CommunicationInterface(version, hostAddress, community);

                final String itemID = textOID.getText();

                final SNMPVarBindList newVars = comInterface.retrieveMIBTable(itemID);

                // print the retrieved stuff
                for (int i = 0; i < newVars.size(); i++)
                    {
                    final SNMPSequence pair = (SNMPSequence) (newVars.getSNMPObjectAt(i));

                    final SNMPObjectIdentifier snmpOID = (SNMPObjectIdentifier) pair.getSNMPObjectAt(0);
                    final SNMPObject snmpValue = pair.getSNMPObjectAt(1);
                    final String typeString = snmpValue.getClass().getName();

                    reportSnmpObject.logger(logSNMPObject(snmpOID, typeString, snmpValue));
                    }
                }

            catch (InterruptedIOException e)
                {
                reportSnmpObject.logger(logMessage("Interrupted during retrieval"));
                }

            catch (UnknownHostException e)
                {
                reportSnmpObject.logger(logMessage("UnknownHostException"));
                }

            catch (IOException e)
                {
                reportSnmpObject.logger(logMessage("IOException"));
                }

            catch (SNMPBadValueException e)
                {
                reportSnmpObject.logger(logMessage("SNMPBadValueException"));
                }

            catch (SNMPGetException e)
                {
                reportSnmpObject.logger(logMessage("SNMPGetException"));
                }

            reportSnmpObject.refreshTable();
            }

        //------------------------------------------------------------------------------------------

        if (command.equals(ACTION_SET_VALUE))
            {
            try
                {
                getHostTask().setStatus(EMPTY_STRING);

                final String community = textCommunity.getText();
                final int version = 0;    // SNMPv1
                final InetAddress hostAddress = InetAddress.getByName(textHostID.getText());
                final SNMPv1CommunicationInterface comInterface = new SNMPv1CommunicationInterface(version, hostAddress, community);

                final String itemID = textOID.getText();
                final String valueString = textValue.getText();
                String valueTypeString = (String) comboValueType.getSelectedItem();

                // Prepend the appropriate package name to form the class name...
                valueTypeString = PACKAGE_SNMP + valueTypeString;

                final SNMPObject itemValue;
                final Class valueClass = Class.forName(valueTypeString);
                itemValue = (SNMPObject) valueClass.newInstance();
                itemValue.setValue(valueString);

                final SNMPVarBindList newVars = comInterface.setMIBEntry(itemID, itemValue);
                final SNMPSequence pair = (SNMPSequence) (newVars.getSNMPObjectAt(0));
                final SNMPObjectIdentifier snmpOID = (SNMPObjectIdentifier) pair.getSNMPObjectAt(0);
                final SNMPObject snmpValue = pair.getSNMPObjectAt(1);
                final String typeString = snmpValue.getClass().getName();

                reportSnmpObject.logger(logSNMPObject(snmpOID, typeString, snmpValue));
                }

            catch (ClassNotFoundException e)
                {
                reportSnmpObject.logger(logMessage("ClassNotFoundException"));
                }

            catch (InstantiationException e)
                {
                reportSnmpObject.logger(logMessage("InstantiationException"));
                }

            catch (IllegalAccessException e)
                {
                reportSnmpObject.logger(logMessage("IllegalAccessException"));
                }

            catch (UnknownHostException e)
                {
                reportSnmpObject.logger(logMessage("UnknownHostException"));
                }

            catch (IOException e)
                {
                reportSnmpObject.logger(logMessage("IOException"));
                }

            catch (SNMPBadValueException e)
                {
                reportSnmpObject.logger(logMessage("SNMPBadValueException"));
                }

            catch (SNMPSetException e)
                {
                reportSnmpObject.logger(logMessage("SNMPSetException"));
                }

            reportSnmpObject.refreshTable();
            }

        //------------------------------------------------------------------------------------------

        if (command.equals(ACTION_GET_TREEWALK_DATA))
            {
            getHostTask().setStatus(EMPTY_STRING);

            if (!threadTreewalk.isAlive())
                {
                // ToDo Consider SwingWorker
                threadTreewalk = new Thread(REGISTRY.getThreadGroup(),
                                            this,
                                            "Thread SnmpUI Treewalk");
                threadTreewalk.start();
                buttonTreewalk.setText("Stop OID retrieval");
                }
            else
                {
                threadTreewalk.interrupt();
                }
            }
        }


    /***********************************************************************************************
     * Retrieve the SNMP Object details on a separate Thread.
     */

    public void run()
        {
        try
            {
            getHostTask().setStatus(EMPTY_STRING);

            final String community = textCommunity.getText();
            final int version = 0;    // SNMPv1
            final InetAddress hostAddress = InetAddress.getByName(textHostID.getText());
            final SNMPv1CommunicationInterface comInterface = new SNMPv1CommunicationInterface(version, hostAddress, community);

            String itemID = SPACE;
            String retrievedID = OBJECT_ID_START;

            while (!Thread.interrupted() && !retrievedID.equals(itemID))
                {
                itemID = retrievedID;

                final SNMPVarBindList newVars = comInterface.getNextMIBEntry(itemID);
                final SNMPSequence pair = (SNMPSequence) (newVars.getSNMPObjectAt(0));
                final SNMPObjectIdentifier snmpOID = (SNMPObjectIdentifier) pair.getSNMPObjectAt(0);
                final SNMPObject snmpValue = pair.getSNMPObjectAt(1);

                retrievedID = snmpOID.toString();

                reportSnmpObject.logger(logSNMPObject(snmpOID,
                                                      snmpValue.getClass().getName(),
                                                      snmpValue));
                }
            }

        catch (InterruptedIOException e)
            {
            reportSnmpObject.logger(logMessage("Interrupted during retrieval"));
            }

        catch (UnknownHostException e)
            {
            reportSnmpObject.logger(logMessage("UnknownHostException"));
            }

        catch (IOException e)
            {
            reportSnmpObject.logger(logMessage("IOException"));
            }

        catch (SNMPBadValueException e)
            {
            reportSnmpObject.logger(logMessage("SNMPBadValueException"));
            }

        catch (SNMPGetException e)
            {
            reportSnmpObject.logger(logMessage("SNMPGetException"));
            }

        reportSnmpObject.refreshTable();
        buttonTreewalk.setText("Get all OID values");
        }


    /***********************************************************************************************
     * Initialise this UIComponent.
     */

    public final void initialiseUI()
        {
        removeAll();

        reportSnmpObject = new SnmpObjectReport(getHostTask(), getResourceKey());
        reportSnmpObject.initialiseUI();

        final JLabel hostIDLabel = new JLabel("Device address");
        textHostID = new JTextField(COLUMNS_TEXT);
        textHostID.setText("localhost");
        textHostID.setEditable(true);

        final JLabel OIDLabel = new JLabel("OID");
        textOID = new JTextField(COLUMNS_TEXT);
        textOID.setEditable(true);

        final JLabel valueLabel = new JLabel("Value (for Set)");
        textValue = new JTextField(COLUMNS_TEXT);
        textValue.setEditable(true);

        final JLabel communityLabel = new JLabel("Community");
        textCommunity = new JTextField(COLUMNS_TEXT);
        textCommunity.setText("public");
        textCommunity.setEditable(true);

        final JButton getDataButton = new JButton("Get OID value");
        getDataButton.setActionCommand(ACTION_GET_DATA);
        getDataButton.addActionListener(this);

        final JButton setValueButton = new JButton("Set OID value");
        setValueButton.setActionCommand(ACTION_SET_VALUE);
        setValueButton.addActionListener(this);

        final JButton getTableButton = new JButton("Get table");
        getTableButton.setActionCommand(ACTION_GET_TABLE);
        getTableButton.addActionListener(this);

        final JButton getNextButton = new JButton("Get next OID value");
        getNextButton.setActionCommand(ACTION_GET_NEXT);
        getNextButton.addActionListener(this);

        buttonTreewalk = new JButton("Get all OID values");
        buttonTreewalk.setActionCommand(ACTION_GET_TREEWALK_DATA);
        buttonTreewalk.addActionListener(this);

        comboValueType = new JComboBox();
        comboValueType.addItem("SNMPInteger");
        comboValueType.addItem("SNMPCounter32");
        comboValueType.addItem("SNMPCounter64");
        comboValueType.addItem("SNMPGauge32");
        comboValueType.addItem("SNMPOctetString");
        comboValueType.addItem("SNMPIPAddress");
        comboValueType.addItem("SNMPNSAPAddress");
        comboValueType.addItem("SNMPObjectIdentifier");
        comboValueType.addItem("SNMPTimeTicks");
        comboValueType.addItem("SNMPUInteger32");

        //------------------------------------------------------------------------------------------
        // Now layout the components

        // set params for layout manager
        final GridBagLayout theLayout = new GridBagLayout();
        final GridBagConstraints c = new GridBagConstraints();

        c.gridwidth = 1;
        c.gridheight = 1;
        c.fill = GridBagConstraints.NONE;
        c.ipadx = 0;
        c.ipady = 0;
        c.insets = new Insets(2, 2, 2, 2);
        c.anchor = GridBagConstraints.CENTER;
        c.weightx = 0;
        c.weighty = 0;

        final JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(theLayout);

        c.gridx = 1;
        c.gridy = 1;
        theLayout.setConstraints(getDataButton, c);
        buttonPanel.add(getDataButton);

        c.gridx = 2;
        c.gridy = 1;
        theLayout.setConstraints(getNextButton, c);
        buttonPanel.add(getNextButton);

        c.gridx = 3;
        c.gridy = 1;
        theLayout.setConstraints(getTableButton, c);
        buttonPanel.add(getTableButton);

        c.gridx = 4;
        c.gridy = 1;
        theLayout.setConstraints(buttonTreewalk, c);
        buttonPanel.add(buttonTreewalk);

        c.gridx = 5;
        c.gridy = 1;
        theLayout.setConstraints(setValueButton, c);
        buttonPanel.add(setValueButton);

        final JPanel hostPanel = new JPanel();
        hostPanel.setLayout(theLayout);

        c.gridx = 1;
        c.gridy = 1;
        theLayout.setConstraints(hostIDLabel, c);
        hostPanel.add(hostIDLabel);

        c.gridx = 2;
        c.gridy = 1;
        theLayout.setConstraints(textHostID, c);
        hostPanel.add(textHostID);

        c.gridx = 1;
        c.gridy = 2;
        theLayout.setConstraints(communityLabel, c);
        hostPanel.add(communityLabel);

        c.gridx = 2;
        c.gridy = 2;
        theLayout.setConstraints(textCommunity, c);
        hostPanel.add(textCommunity);

        final JPanel oidPanel = new JPanel();
        oidPanel.setLayout(theLayout);

        c.gridx = 1;
        c.gridy = 1;
        theLayout.setConstraints(OIDLabel, c);
        oidPanel.add(OIDLabel);

        c.gridx = 2;
        c.gridy = 1;
        theLayout.setConstraints(textOID, c);
        oidPanel.add(textOID);

        c.gridx = 1;
        c.gridy = 2;
        theLayout.setConstraints(valueLabel, c);
        oidPanel.add(valueLabel);

        c.gridx = 2;
        c.gridy = 2;
        theLayout.setConstraints(textValue, c);
        oidPanel.add(textValue);

        c.gridx = 3;
        c.gridy = 2;
        theLayout.setConstraints(comboValueType, c);
        oidPanel.add(comboValueType);

        c.gridwidth = 1;
        c.anchor = GridBagConstraints.CENTER;

        final JPanel messagesPanel = new JPanel(new BorderLayout());
        messagesPanel.add(reportSnmpObject);

        c.fill = GridBagConstraints.BOTH;
        c.gridx = 1;
        c.gridy = 2;
        c.gridwidth = 2;
        c.weightx = WEIGHT_CENTRE;
        c.weighty = WEIGHT_CENTRE;
        c.anchor = GridBagConstraints.CENTER;
        theLayout.setConstraints(reportSnmpObject, c);
        messagesPanel.add(reportSnmpObject);

        c.gridwidth = 1;
        c.weightx = 0;
        c.weighty = 0;

        setLayout(theLayout);

        c.gridx = 1;
        c.gridy = 1;
        theLayout.setConstraints(hostPanel, c);
        add(hostPanel);

        c.gridx = 1;
        c.gridy = 2;
        theLayout.setConstraints(oidPanel, c);
        add(oidPanel);

        c.gridx = 1;
        c.gridy = 3;
        theLayout.setConstraints(buttonPanel, c);
        add(buttonPanel);

        c.fill = GridBagConstraints.BOTH;
        c.gridx = 1;
        c.gridy = 4;
        c.weightx = WEIGHT_CENTRE;
        c.weighty = WEIGHT_CENTRE;
        theLayout.setConstraints(messagesPanel, c);
        add(messagesPanel);
        }


    /**********************************************************************************************
     * Run this UIComponent.
     */

    public final void runUI()
        {
        LOGGER.debugNavigation("SnmpUI.runUI()");

        UIComponentHelper.runComponentAndTransferActions(reportSnmpObject, this);
        }


    /**********************************************************************************************
     * Stop this UIComponent.
     */

    public final void stopUI()
        {
        if (reportSnmpObject != null)
            {
            reportSnmpObject.stopUI();
            }
        }


    /**********************************************************************************************
     * Dispose of all components of this UIComponent.
     */

    public final void disposeUI()
        {
        if (reportSnmpObject != null)
            {
            reportSnmpObject.disposeUI();
            }

        removeAll();
        }


    /***********************************************************************************************
     * Get the host Task.
     *
     * @return TaskPlugin
     */

    private TaskPlugin getHostTask()
        {
        return (this.pluginTask);
        }


    /***********************************************************************************************
     * Get the ResourceKey for the Report.
     *
     * @return String
     */

    public final String getResourceKey()
        {
        return (this.strResourceKey);
        }
    }