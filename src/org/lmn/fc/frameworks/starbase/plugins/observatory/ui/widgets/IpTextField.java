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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.widgets;


import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.frameworks.starbase.plugins.observatory.common.IPVersion;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.scanner.NetworkScannerHelper;
import org.lmn.fc.ui.layout.BoxLayoutFixed;

import javax.swing.*;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;


/***************************************************************************************************
 * IpTextField.
 * JIpTextField An idea originally from Naveed Quadri,
 * with extensive modifications, corrections, and IntelliJ inspections.
 * See: http://code.google.com/p/swing-components/source/browse/#svn%2Ftrunk%2Fsrc%2Forg%2Fgpl%2FSwingComponents%2FJIPTextField
 */

public class IpTextField extends JPanel
                         implements FrameworkConstants,
                                    FrameworkStrings,
                                    FrameworkMetadata,
                                    FrameworkSingletons
    {
    private static final int WIDTH_SEPARATOR = 8;
    private static final char NULL_CHAR = '\0';

    // Injections
    private IPVersion versionIP;

    // UI
    private JTextField[] arrayOctets;
    private boolean boolFocus;


    /**********************************************************************************************
     * Creates a borderless, non-editable, non-focusable JTextfield component
     * with the separator, '.' for IPV4, ':' for IPV6.
     *
     * @param parenttextfield
     * @param ipversion
     *
     * @return JTextField
     */

    private static JTextField constructSeparatorComponent(final IpTextField parenttextfield,
                                                          final IPVersion ipversion)
        {
        final String SOURCE = "IpTextField.constructSeparatorComponent() ";
        final JTextField textField;

        textField = new JTextField(1);
        textField.setFocusable(false);
        textField.setEditable(false);

        textField.setBorder(BorderFactory.createEmptyBorder());
        textField.setHorizontalAlignment(JTextField.CENTER);
        textField.setAlignmentY(Component.CENTER_ALIGNMENT);
        textField.setText(String.valueOf(ipversion.getSeparatorChar()));

        textField.setPreferredSize(new Dimension(WIDTH_SEPARATOR, ipversion.getComponentHeight()));
        textField.setMaximumSize(new Dimension(WIDTH_SEPARATOR, ipversion.getComponentHeight()));

        return (textField);
        }


    /**********************************************************************************************
     * Constructs the IPTextField for the specified version and initial IP Address.
     *
     * @param ipversion
     * @param address
     */

    public IpTextField(final IPVersion ipversion,
                       final InetAddress address)
        {
        this.versionIP = ipversion;
        initialiseComponent();

        if (address != null)
            {
            setInetAddress(address);
            }
        }


    /**********************************************************************************************
     * Initializes the component
     */

    private void initialiseComponent()
        {
        final String SOURCE = "IpTextField.initialiseComponent() ";
        final JTextField[] arraySeparators;

        removeAll();

        setBorder(BorderFactory.createLineBorder(Color.gray, 1));

        setPreferredSize(new Dimension(getVersion().getComponentWidth(), getVersion().getComponentHeight()));
        setMaximumSize(new Dimension(getVersion().getComponentWidth(), getVersion().getComponentHeight()));
        setAlignmentY(Component.CENTER_ALIGNMENT);

        setLayout(new BoxLayoutFixed(this, BoxLayoutFixed.X_AXIS));

        arrayOctets = constructOctetComponents();
        arraySeparators = constructSeparatorComponents();

        add(Box.createHorizontalGlue());

        for (int i = 0;
             i < getVersion().getFieldCount();
             i++)
            {
            add(arrayOctets[i]);

            if (i != getVersion().getFieldCount() - 1)
                {
                add(arraySeparators[i]);
                }
            }

        add(Box.createHorizontalGlue());
        }


    /**********************************************************************************************
     * Constructs the OctetComponents.
     * Returns an array of 4 OctetComponents if IPV4, 8 OctetComponents if IPV6.
     *
     * @return JTextField[]
     */

    private JTextField[] constructOctetComponents()
        {
        final String SOURCE = "IpTextField.constructOctetComponents() ";
        final JTextField[] textFields;

        textFields = new JTextField[getVersion().getFieldCount()];

        for (int intOctetIndex = 0;
             (intOctetIndex < textFields.length);
             intOctetIndex++)
            {
            textFields[intOctetIndex] = new OctetComponent(this, getVersion());
            }

        return (textFields);
        }


    /**********************************************************************************************
     * Constructs the Separator components.
     *
     * @return JTextField[]
     */

    private JTextField[] constructSeparatorComponents()
        {
        final String SOURCE = "IpTextField.constructSeparatorComponents() ";
        final JTextField[] textFields;

        textFields = new JTextField[getVersion().getFieldCount() - 1];

        for (int intSeparatorIndex = 0;
             intSeparatorIndex < textFields.length;
             intSeparatorIndex++)
            {
            textFields[intSeparatorIndex] = constructSeparatorComponent(this, getVersion());
            }

        return (textFields);
        }


    /**********************************************************************************************
     * Gets the IP Version.
     *
     * @return IPVersion
     */

    public IPVersion getVersion()
        {
        final String SOURCE = "IpTextField.getVersion() ";

        return (this.versionIP);
        }


    /**********************************************************************************************
     * Sets the IP Version.
     *
     * @param ipversion
     */

    public void setVersion(final IPVersion ipversion)
        {
        final String SOURCE = "IpTextField.setVersion() ";

        this.versionIP = ipversion;

        removeAll();
        initialiseComponent();
        }


    /***********************************************************************************************
     * Set the foreground text colour.
     *
     * @param color
     */

    public void setForeground(final Color color)
        {
        final String SOURCE = "IpTextField.setForeground() ";
        final Component[] arrayComponents;

        super.setForeground(color);

        arrayComponents = getComponents();

        for (int intComponentIndex = 0;
             intComponentIndex < arrayComponents.length;
             intComponentIndex++)
            {
            final Component component;

            component = arrayComponents[intComponentIndex];
            component.setForeground(color);
            }

        repaint();
        }


    /***********************************************************************************************
     * Set the background text colour.
     *
     * @param color
     */

    public void setBackground(final Color color)
        {
        final String SOURCE = "IpTextField.setBackground() ";
        final Component[] arrayComponents;

        super.setBackground(color);

        arrayComponents = getComponents();

        for (int intComponentIndex = 0;
             intComponentIndex < arrayComponents.length;
             intComponentIndex++)
            {
            final Component component;

            component = arrayComponents[intComponentIndex];
            component.setBackground(color);
            }

        repaint();
        }


    /***********************************************************************************************
     * Set the text Font.
     *
     * @param font
     */

    public void setFont(final Font font)
        {
        final String SOURCE = "IpTextField.setFont() ";
        final Component[] arrayComponents;

        super.setFont(font);

        arrayComponents = getComponents();

        for (int intComponentIndex = 0;
             intComponentIndex < arrayComponents.length;
             intComponentIndex++)
            {
            final Component component;

            component = arrayComponents[intComponentIndex];
            component.setFont(font);
            }

        repaint();
        }


    /***********************************************************************************************
     * Set the Tooltip.
     *
     * @param tooltip
     */

    public void setToolTipText(final String tooltip)
        {
        final String SOURCE = "IpTextField.setToolTipText() ";
        final Component[] arrayComponents;

        super.setToolTipText(tooltip);

        arrayComponents = getComponents();

        for (int intComponentIndex = 0;
             intComponentIndex < arrayComponents.length;
             intComponentIndex++)
            {
            final Component component;

            component = arrayComponents[intComponentIndex];

            if (component instanceof JComponent)
                {
                ((JComponent)component).setToolTipText(tooltip);
                }
            }

        repaint();
        }


    /**********************************************************************************************
     * Registers the given observer to begin receiving notifications
     * when changes are made to the document associated with each Octet field.
     *
     * @param listener the observer to register
     */

    public void addDocumentListener(final DocumentListener listener)
        {
        for (int intOctetIndex = 0;
             intOctetIndex < getVersion().getFieldCount();
             intOctetIndex++)
            {
            arrayOctets[intOctetIndex].getDocument().addDocumentListener(listener);
            }
        }


    /**********************************************************************************************
     * Gets the InetAddress.
     *
     * @return IP Address
     *
     * @throws UnknownHostException
     * @throws SecurityException
     */

    public InetAddress getInetAddress() throws UnknownHostException,
                                               SecurityException
        {
        final String SOURCE = "IpTextField.getInetAddress() ";

        return Inet4Address.getByName(getIpAddressAsString());
        }


    /**********************************************************************************************
     * Sets the InetAddress.
     *
     * @param inetaddress
     */

    public void setInetAddress(final InetAddress inetaddress)
        {
        final String SOURCE = "IpTextField.setInetAddress() ";
        final String strIP;
        final String[] arrayTokens;
        final String strRegex;

        if ((inetaddress instanceof Inet4Address)
            && (!IPVersion.IPV4.equals(getVersion())))
            {
            LOGGER.error(SOURCE + "The IPVersion of this component is IPV6, but the IP Address passed in is IPV4");
            }

        if ((inetaddress instanceof Inet6Address)
            && (!IPVersion.IPV6.equals(getVersion())))
            {
            LOGGER.error(SOURCE + "The IPVersion of this component is IPV4, but the IP Address passed in is IPV6");
            }

        strIP = inetaddress.getHostAddress();

        strRegex = getVersion().getRegexPrefix() + String.valueOf(getVersion().getSeparatorChar());
        arrayTokens = strIP.split(strRegex);

        for (int intOctetIndex = 0;
             intOctetIndex < arrayTokens.length;
             intOctetIndex++)
            {
            this.arrayOctets[intOctetIndex].setText(arrayTokens[intOctetIndex]);
            }
        }


    /**********************************************************************************************
     * Gets the IP Address as a long.
     *
     * @return long
     *
     * @throws UnknownHostException
     * @throws SecurityException
     */

    public long getIPAddressAsLong() throws UnknownHostException,
                                            SecurityException
        {
        final String SOURCE = "IpTextField.getIPAddressAsLong() ";

        return (NetworkScannerHelper.ipV4ToLong(getIpAddressAsString()));
        }


    /**********************************************************************************************
     * Gets the IP address as a String
     *
     * @return IP Address String
     */

    public String getIpAddressAsString()
        {
        final String SOURCE = "IpTextField.getIpAddressAsString() ";
        final StringBuilder builder;

        builder = new StringBuilder();

        for (final JTextField octet : arrayOctets)
            {
            builder.append((octet).getText());
            builder.append(getVersion().getSeparatorChar());
            }

        // Remove the last separator
        builder.deleteCharAt(builder.length() - 1);

        return (builder.toString());
        }


    /***********************************************************************************************
     * Set the IP Address to appear in each of the Octet fields.
     *
     * @param ipaddress
     */

    public void setIpAddressAsString(final String ipaddress)
        {
        final String SOURCE = "IpTextField.setIpAddressAsString() ";

        try
            {
            final InetAddress inetAddress;

            if (IPVersion.IPV4.equals(getVersion()))
                {
                inetAddress = Inet4Address.getByName(ipaddress);
                }
            else
                {
                inetAddress = Inet6Address.getByName(ipaddress);
                }

            setInetAddress(inetAddress);
            }

        catch (UnknownHostException exception)
            {
            LOGGER.error(SOURCE + "The IP address is invalid [ipaddress=" + ipaddress + "]");
            }

        catch (SecurityException exception)
            {
            LOGGER.error(SOURCE + "SecurityException [exception=" + exception.getMessage() + "]");
            }
        }


    /**********************************************************************************************
     * Gets an array of integers containing each Octet field.
     *
     * @return int[]
     */

    public int[] getOctets()
        {
        final String SOURCE = "IpTextField.getOctets() ";
        final int[] octets;

        octets = new int[getVersion().getFieldCount()];

        for (int intOctetIndex = 0;
             intOctetIndex < getVersion().getFieldCount();
             intOctetIndex++)
            {
            octets[intOctetIndex] = Integer.parseInt((arrayOctets[intOctetIndex]).getText(), getVersion().getRadix());
            }

        return (octets);
        }


    /***********************************************************************************************
     * OctetComponent.
     */

    class OctetComponent extends JTextField
                         implements ActionListener,
                                    FocusListener,
                                    KeyListener
        {
        private final IpTextField ipFieldParent;
        private final IPVersion ipVersion;


        /*******************************************************************************************
         * Construct a OctetComponent.
         *
         * @param parenttextfield
         * @param ipversion
         */

        private OctetComponent(final IpTextField parenttextfield,
                               final IPVersion ipversion)
            {
            // Construct a JTextField with the correct number of columns
            super(ipversion.getFieldWidth());

            this.ipFieldParent = parenttextfield;
            this.ipVersion = ipversion;

            initialiseComponent();
            }


        /*******************************************************************************************
         * Initialise the TextComponent.
         */

        private void initialiseComponent()
            {
            setPreferredSize(new Dimension(ipVersion.getOctetWidth(), ipVersion.getComponentHeight()));
            setMaximumSize(new Dimension(ipVersion.getOctetWidth(), ipVersion.getComponentHeight()));
            setMinimumSize(new Dimension(ipVersion.getOctetWidth(), ipVersion.getComponentHeight()));

            setBorder(BorderFactory.createEmptyBorder());
            setHorizontalAlignment(CENTER);
            setAlignmentY(Component.CENTER_ALIGNMENT);

            addActionListener(this);
            addFocusListener(this);
            addKeyListener(this);

            if (IPVersion.IPV4.equals(getVersion()))
                {
                setInputVerifier(new IPV4Verifier(ipVersion));
                }
            else
                {
                setInputVerifier(new IPV6Verifier(ipVersion));
                }
            }


        /*******************************************************************************************
         * The listener interface for receiving action events.
         *
         * @param event
         */

        public void actionPerformed(final ActionEvent event)
            {
            //LOGGER.logTimedEvent("IpTextField actionPerformed");
            transferFocus();
            }


        /******************************************************************************************
         * Invoked when a component gains the keyboard focus.
         */

        public void focusGained(final FocusEvent event)
            {
            //LOGGER.logTimedEvent("IpTextField focusGained");
            selectText(((JTextField) event.getComponent()));
            boolFocus = true;
            ipFieldParent.repaint();
            }


        /******************************************************************************************
         * Invoked when a component loses the keyboard focus.
         */

        public void focusLost(final FocusEvent event)
            {
            //LOGGER.logTimedEvent("IpTextField focusLost");
            boolFocus = false;
            ipFieldParent.repaint();
            }


        /******************************************************************************************
         * Invoked when a key has been typed.
         * See the class description for {@link KeyEvent} for a definition of a key typed event.
         */

        public void keyTyped(final KeyEvent event)
            {
            //LOGGER.logTimedEvent("IpTextField keyTyped");
            final JTextField source;

            source = (JTextField) event.getSource();

            if (event.getKeyChar() == ipVersion.getSeparatorChar())
                {
                event.setKeyChar(NULL_CHAR);

                if (!source.getText().isEmpty())
                    {
                    transferFocus();
                    }
                }

            if ((source.getText().isEmpty())
                && (event.getKeyChar() == KeyEvent.VK_BACK_SPACE))
                {
                transferFocusBackward();
                }

            switch (getVersion())
                {
                case IPV4:
                    {
                    if (!Character.isDigit(event.getKeyChar()))
                        {
                        event.setKeyChar(NULL_CHAR);
                        }

                    break;
                    }

                case IPV6:
                    {
                    try
                        {
                        // IP v6 is in Hex
                        Integer.parseInt(String.valueOf(event.getKeyChar()), getVersion().getRadix());
                        }

                    catch (NumberFormatException exception)
                        {
                        event.setKeyChar(NULL_CHAR);
                        }

                    break;
                    }
                }
            }


        /*******************************************************************************************
         * Invoked when a key has been pressed.
         *
         * @param event
         */

        public void keyPressed(final KeyEvent event)
            {
            //LOGGER.logTimedEvent("IpTextField keyPressed");
            }


        /*******************************************************************************************
         * Invoked when a key has been released.
         *
         * @param event
         */

        public void keyReleased(final KeyEvent event)
            {
            //LOGGER.logTimedEvent("IpTextField keyReleased");
            if (((JTextField) event.getSource()).getText().length() == ipVersion.getFieldWidth())
                {
                transferFocus();
                }
            }


        /******************************************************************************************
         * Select the text in the field.
         *
         * @param textfield
         */

        private void selectText(final JTextField textfield)
            {
            final String text;

            text = textfield.getText();

            if (text.isEmpty())
                {
                return;
                }

            textfield.setSelectionStart(0);
            textfield.setSelectionEnd(text.length());
            }
        }


    /***********************************************************************************************
     * IPV6Verifier.
     */

    private class IPV6Verifier extends InputVerifier
        {
        private final IPVersion ipVersion;


        /*******************************************************************************************
         * Construct an IPV6Verifier.
         *
         * @param ipversion
         */

        private IPV6Verifier(final IPVersion ipversion)
            {
            this.ipVersion = ipversion;
            }


        /******************************************************************************************
         * Checks whether the JComponent's input is valid. This method should
         * have no side effects. It returns a boolean indicating the status of the argument's input.
         *
         * @param input the JComponent to verify
         * @return <code>true</code> when valid, <code>false</code> when invalid
         * @see JComponent#setInputVerifier
         * @see JComponent#getInputVerifier
         */
        @Override
        public boolean verify(final JComponent input)
            {
            final JTextField inputTxt;

            inputTxt = (JTextField) input;

            // Don't allow empty octets!
            // That would take too long to return an UnknownHostException
            if (inputTxt.getText().isEmpty())
                {
                inputTxt.setText(Integer.toString(0));
                return (true);
                }

            try
                {
                // Check that we have a HEX number
                Integer.parseInt(inputTxt.getText(), ipVersion.getRadix());

                // Normalise to upper case
                inputTxt.setText(inputTxt.getText().toUpperCase());

                return (true);
                }

            catch (NumberFormatException exception)
                {
                return (false);
                }
            }
        }


    /***********************************************************************************************
     * IPV4Verifier.
     */

    private class IPV4Verifier extends InputVerifier
        {
        private final IPVersion ipVersion;


        /*******************************************************************************************
         * Construct an IPV4Verifier.
         *
         * @param ipversion
         */

        private IPV4Verifier(final IPVersion ipversion)
            {
            this.ipVersion = ipversion;
            }


        /******************************************************************************************
         * Checks whether the JComponent's input is valid. This method should
         * have no side effects. It returns a boolean indicating the status of the argument's input.
         *
         * @param input the JComponent to verify
         * @return <code>true</code> when valid, <code>false</code> when invalid
         * @see JComponent#setInputVerifier
         * @see JComponent#getInputVerifier
         */
        @Override
        public boolean verify(final JComponent input)
            {
            final JTextField inputTxt;

            inputTxt = (JTextField) input;

            // Don't allow empty octets!
            // That would take too long to return an UnknownHostException
            if (inputTxt.getText().isEmpty())
                {
                inputTxt.setText(Integer.toString(0));
                return (true);
                }

            try
                {
                int value;

                value = Integer.parseInt(inputTxt.getText(), ipVersion.getRadix());

                if (value < 0)
                    {
                    value = 0;
                    }

                if (value > 255)
                    {
                    value = 255;
                    }

                inputTxt.setText(Integer.toString(value));

                return (true);
                }

            catch (NumberFormatException exception)
                {
                return (false);
                }
            }
        }
    }
