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


import org.lmn.fc.common.utilities.ui.AlignedListIconCellRenderer;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.SerialConfigurationCellDataInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.SerialConfigurationIndicatorInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.SerialConfigurationUIComponentInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.impl.streams.StreamType;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.model.registry.NavigationUtilities;
import org.lmn.fc.model.resources.PropertyPlugin;
import org.lmn.fc.ui.UIComponentPlugin;
import org.lmn.fc.ui.widgets.IndicatorInterface;
import org.lmn.fc.ui.widgets.impl.ToolbarIndicator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/***************************************************************************************************
 * SerialConfigurationIndicator.
 */

public final class SerialConfigurationIndicator extends JToolBar
                                                implements SerialConfigurationIndicatorInterface
    {
    private static final long serialVersionUID = 8321603346438531402L;

    private IndicatorInterface indicatorPortName;

    private JComboBox comboBaudRate;
    private JComboBox comboDataBits;
    private JComboBox comboStopBits;
    private JComboBox comboParity;
    private JComboBox comboFlowControl;

    private SerialConfigurationCellDataInterface cellData;
    private boolean hasSelection;


    /***********************************************************************************************
     * Construct a SerialConfigurationIndicator.
     *
     * @param obsinstrument
     * @param scui
     * @param fontdata
     * @param colourforeground
     * @param colourbackground
     * @param debug
     */

    public SerialConfigurationIndicator(final ObservatoryInstrumentInterface obsinstrument,
                                        final SerialConfigurationUIComponentInterface scui,
                                        final FontInterface fontdata,
                                        final ColourInterface colourforeground,
                                        final ColourInterface colourbackground,
                                        final boolean debug)
        {
        super();

        this.cellData = null;
        this.hasSelection = false;

        initialiseIndicator(obsinstrument,
                            scui,
                            fontdata,
                            colourforeground,
                            colourbackground,
                            debug);
        }


    /***********************************************************************************************
     * Initialise the Port Configuration Indicator.
     *
     * @param obsinstrument
     * @param scui
     * @param fontdata
     * @param colourforeground
     * @param colourbackground
     * @param debug
     */

    public void initialiseIndicator(final ObservatoryInstrumentInterface obsinstrument,
                                    final SerialConfigurationUIComponentInterface scui,
                                    final FontInterface fontdata,
                                    final ColourInterface colourforeground,
                                    final ColourInterface colourbackground,
                                    final boolean debug)
        {
        final String SOURCE = "SerialConfigurationIndicator.initialiseIndicator() ";
        final JLabel labelBaudRate;
        final JLabel labelDataBits;
        final JLabel labelStopBits;
        final JLabel labelParity;
        final JLabel labelFlowControl;

        ActionListener choiceListener;

        setFloatable(false);
        setMinimumSize(UIComponentPlugin.DIM_TOOLBAR_SIZE);
        setPreferredSize(UIComponentPlugin.DIM_TOOLBAR_SIZE);
        setMaximumSize(UIComponentPlugin.DIM_TOOLBAR_SIZE);
        setBackground(colourbackground.getColor());

        //-------------------------------------------------------------------------------------
        // Initialise the Port.Name Indicator and the Labels

        indicatorPortName = new ToolbarIndicator(new Dimension(150, 20),
                                                 EMPTY_STRING,
                                                 TOOLTIP_PORTNAME);
        getPortName().setValueFormat("COM999");
        getPortName().setValueBackground(Color.BLACK);
        getPortName().setValue("No Selection");

        labelBaudRate = new JLabel(LABEL_RATE)
            {
            private static final long serialVersionUID = 2489506841248758545L;


            // Enable Antialiasing in Java 1.5
            protected void paintComponent(final Graphics graphics)
                {
                final Graphics2D graphics2D = (Graphics2D) graphics;

                // For antialiasing text
                graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                                            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                super.paintComponent(graphics2D);
                }
            };

        labelBaudRate.setFont(fontdata.getFont());
        labelBaudRate.setForeground(colourforeground.getColor());

        labelDataBits = new JLabel(LABEL_DATA)
            {
            private static final long serialVersionUID = 3094749061817061486L;


            // Enable Antialiasing in Java 1.5
            protected void paintComponent(final Graphics graphics)
                {
                final Graphics2D graphics2D = (Graphics2D) graphics;

                // For antialiasing text
                graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                                            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                super.paintComponent(graphics2D);
                }
            };

        labelDataBits.setFont(fontdata.getFont());
        labelDataBits.setForeground(colourforeground.getColor());

        labelStopBits = new JLabel(LABEL_STOP)
            {
            private static final long serialVersionUID = 2822106845518063661L;


            // Enable Antialiasing in Java 1.5
            protected void paintComponent(final Graphics graphics)
                {
                final Graphics2D graphics2D = (Graphics2D) graphics;

                // For antialiasing text
                graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                                            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                super.paintComponent(graphics2D);
                }
            };

        labelStopBits.setFont(fontdata.getFont());
        labelStopBits.setForeground(colourforeground.getColor());

        labelParity = new JLabel(LABEL_PARITY)
            {
            private static final long serialVersionUID = 6689366864907814051L;


            // Enable Antialiasing in Java 1.5
            protected void paintComponent(final Graphics graphics)
                {
                final Graphics2D graphics2D = (Graphics2D) graphics;

                // For antialiasing text
                graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                                            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                super.paintComponent(graphics2D);
                }
            };

        labelParity.setFont(fontdata.getFont());
        labelParity.setForeground(colourforeground.getColor());

        labelFlowControl = new JLabel(LABEL_FLOW)
            {
            private static final long serialVersionUID = -9035756081324283303L;


            // Enable Antialiasing in Java 1.5
            protected void paintComponent(final Graphics graphics)
                {
                final Graphics2D graphics2D = (Graphics2D) graphics;

                // For antialiasing text
                graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                                            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                super.paintComponent(graphics2D);
                }
            };

        labelFlowControl.setFont(fontdata.getFont());
        labelFlowControl.setForeground(colourforeground.getColor());

        //-------------------------------------------------------------------------------------
        // Baud Rate

        comboBaudRate = new JComboBox(SerialBaudRate.values());

        getBaudRate().setFont(fontdata.getFont());
        getBaudRate().setForeground(colourforeground.getColor());
        getBaudRate().setRenderer(new AlignedListIconCellRenderer(obsinstrument.getHostAtom(),
                                                                  SwingConstants.LEFT, // Sets the alignment of the label's contents along the X axis
                                                                  SwingConstants.LEFT, // Sets the horizontal position of the label's text, relative to its image
                                                                  fontdata,
                                                                  colourforeground,
                                                                  colourbackground));

        // Do NOT allow the combo box to take up all the remaining space!
        getBaudRate().setPreferredSize(new Dimension(WIDTH_PORT_RATE_DROPDOWN, UIComponentPlugin.HEIGHT_TOOLBAR_ICON - 4));
        getBaudRate().setMaximumSize(new Dimension(WIDTH_PORT_RATE_DROPDOWN, UIComponentPlugin.HEIGHT_TOOLBAR_ICON - 4));
        getBaudRate().setAlignmentX(0);
        getBaudRate().setToolTipText(TOOLTIP_RATE);
        getBaudRate().setEnabled(hasSelection());
        getBaudRate().setEditable(false);

        ToolTipManager.sharedInstance().registerComponent(getBaudRate());

        choiceListener = new ActionListener()
            {
            public void actionPerformed(final ActionEvent event)
                {
                final PropertyPlugin propertyData;

                // This event can't occur unless there is a valid ResourceKey selection
                propertyData = (PropertyPlugin)REGISTRY.getProperties().get(getSelectedResourceKey() + KEY_PORT_BAUDRATE);

                if ((propertyData != null)
                    && (propertyData.getDataType().equals(Integer.class.getName()))
                    && (propertyData.getResource() != null))
                    {
                    final SerialBaudRate baudRate;

                    baudRate = (SerialBaudRate)getBaudRate().getSelectedItem();
                    propertyData.setResource(baudRate.getBaudRate());

                    getSelectedCellData().setChanged(true);
                    updateGraph(scui);

                    SerialConfigurationHelper.debugCellData(null,
                                                            getSelectedCellData(),
                                                            SOURCE + "comboBaudRate",
                                                            debug);
                    SerialConfigurationHelper.logPortProperties(getSelectedResourceKey(),
                                                                getSelectedCellData().getStreamType(),
                                                                SOURCE + "comboBaudRate",
                                                                debug);
                    }
                }
            };

        getBaudRate().addActionListener(choiceListener);

        //-------------------------------------------------------------------------------------
        // Data Bits

        comboDataBits = new JComboBox(SerialDataBits.values());

        getDataBits().setFont(fontdata.getFont());
        getDataBits().setForeground(colourforeground.getColor());
        getDataBits().setRenderer(new AlignedListIconCellRenderer(obsinstrument.getHostAtom(),
                                                                  SwingConstants.LEFT,
                                                                  SwingConstants.LEFT,
                                                                  fontdata,
                                                                  colourforeground,
                                                                  colourbackground));

        getDataBits().setPreferredSize(new Dimension(WIDTH_PORT_CONFIG_DROPDOWN, UIComponentPlugin.HEIGHT_TOOLBAR_ICON - 4));
        getDataBits().setMaximumSize(new Dimension(WIDTH_PORT_CONFIG_DROPDOWN, UIComponentPlugin.HEIGHT_TOOLBAR_ICON - 4));
        getDataBits().setAlignmentX(0);
        getDataBits().setToolTipText(TOOLTIP_DATABITS);
        getDataBits().setEnabled(hasSelection());
        getDataBits().setEditable(false);

        ToolTipManager.sharedInstance().registerComponent(getDataBits());

        choiceListener = new ActionListener()
            {
            public void actionPerformed(final ActionEvent event)
                {
                final PropertyPlugin propertyData;

                // This event can't occur unless there is a valid ResourceKey selection
                propertyData = (PropertyPlugin)REGISTRY.getProperties().get(getSelectedResourceKey() + KEY_PORT_DATA_BITS);

                if ((propertyData != null)
                    && (propertyData.getDataType().equals(Integer.class.getName()))
                    && (propertyData.getResource() != null))
                    {
                    final SerialDataBits dataBits;

                    dataBits = (SerialDataBits)getDataBits().getSelectedItem();
                    propertyData.setResource(dataBits.getDataBits());

                    getSelectedCellData().setChanged(true);
                    updateGraph(scui);

                    SerialConfigurationHelper.debugCellData(null,
                                                            getSelectedCellData(),
                                                            SOURCE + "comboDataBits",
                                                            debug);
                    SerialConfigurationHelper.logPortProperties(getSelectedResourceKey(),
                                                                getSelectedCellData().getStreamType(),
                                                                SOURCE + "comboDataBits",
                                                                debug);
                    }
                }
            };

        getDataBits().addActionListener(choiceListener);

        //-------------------------------------------------------------------------------------
        // Stop Bits

        comboStopBits = new JComboBox(SerialStopBits.values());

        getStopBits().setFont(fontdata.getFont());
        getStopBits().setForeground(colourforeground.getColor());
        getStopBits().setRenderer(new AlignedListIconCellRenderer(obsinstrument.getHostAtom(),
                                                                  SwingConstants.LEFT,
                                                                  SwingConstants.LEFT,
                                                                  fontdata,
                                                                  colourforeground,
                                                                  colourbackground));

        getStopBits().setPreferredSize(new Dimension(WIDTH_PORT_CONFIG_DROPDOWN, UIComponentPlugin.HEIGHT_TOOLBAR_ICON - 4));
        getStopBits().setMaximumSize(new Dimension(WIDTH_PORT_CONFIG_DROPDOWN, UIComponentPlugin.HEIGHT_TOOLBAR_ICON - 4));
        getStopBits().setAlignmentX(0);
        getStopBits().setToolTipText(TOOLTIP_STOPBITS);
        getStopBits().setEnabled(hasSelection());
        getStopBits().setEditable(false);

        ToolTipManager.sharedInstance().registerComponent(getStopBits());

        choiceListener = new ActionListener()
            {
            public void actionPerformed(final ActionEvent event)
                {
                final PropertyPlugin propertyData;

                // This event can't occur unless there is a valid ResourceKey selection
                propertyData = (PropertyPlugin)REGISTRY.getProperties().get(getSelectedResourceKey() + KEY_PORT_STOP_BITS);

                if ((propertyData != null)
                    && (propertyData.getDataType().equals(Integer.class.getName()))
                    && (propertyData.getResource() != null))
                    {
                    final SerialStopBits stopBits;

                    stopBits = (SerialStopBits)getStopBits().getSelectedItem();
                    propertyData.setResource(stopBits.getStopBits());

                    getSelectedCellData().setChanged(true);
                    updateGraph(scui);

                    SerialConfigurationHelper.debugCellData(null,
                                                            getSelectedCellData(),
                                                            SOURCE + "comboStopBits",
                                                            debug);
                    SerialConfigurationHelper.logPortProperties(getSelectedResourceKey(),
                                                                getSelectedCellData().getStreamType(),
                                                                SOURCE + "comboStopBits",
                                                                debug);
                    }
                }
            };

        getStopBits().addActionListener(choiceListener);

        //-------------------------------------------------------------------------------------
        // Parity

        comboParity = new JComboBox(SerialParity.values());

        getParity().setFont(fontdata.getFont());
        getParity().setForeground(colourforeground.getColor());
        getParity().setRenderer(new AlignedListIconCellRenderer(obsinstrument.getHostAtom(),
                                                                SwingConstants.LEFT,
                                                                SwingConstants.LEFT,
                                                                fontdata,
                                                                colourforeground,
                                                                colourbackground));

        getParity().setPreferredSize(new Dimension(WIDTH_PORT_CONFIG_DROPDOWN, UIComponentPlugin.HEIGHT_TOOLBAR_ICON - 4));
        getParity().setMaximumSize(new Dimension(WIDTH_PORT_CONFIG_DROPDOWN, UIComponentPlugin.HEIGHT_TOOLBAR_ICON - 4));
        getParity().setAlignmentX(0);
        getParity().setToolTipText(TOOLTIP_PARITY);
        getParity().setEnabled(hasSelection());
        getParity().setEditable(false);

        ToolTipManager.sharedInstance().registerComponent(getParity());

        choiceListener = new ActionListener()
            {
            public void actionPerformed(final ActionEvent event)
                {
                final PropertyPlugin propertyData;

                // This event can't occur unless there is a valid ResourceKey selection
                propertyData = (PropertyPlugin)REGISTRY.getProperties().get(getSelectedResourceKey() + KEY_PORT_PARITY);

                if ((propertyData != null)
                    && (propertyData.getDataType().equals(Integer.class.getName()))
                    && (propertyData.getResource() != null))
                    {
                    final SerialParity parity;

                    parity = (SerialParity)getParity().getSelectedItem();
                    propertyData.setResource(parity.getParity());

                    getSelectedCellData().setChanged(true);
                    updateGraph(scui);

                    SerialConfigurationHelper.debugCellData(null,
                                                            getSelectedCellData(),
                                                            SOURCE + "comboParity",
                                                            debug);
                    SerialConfigurationHelper.logPortProperties(getSelectedResourceKey(),
                                                                getSelectedCellData().getStreamType(),
                                                                SOURCE + "comboParity",
                                                                debug);
                    }
                }
            };

        getParity().addActionListener(choiceListener);

        //-------------------------------------------------------------------------------------
        // Flow Control

        comboFlowControl = new JComboBox(SerialFlowControl.values());

        getFlowControl().setFont(fontdata.getFont());
        getFlowControl().setForeground(colourforeground.getColor());
        getFlowControl().setRenderer(new AlignedListIconCellRenderer(obsinstrument.getHostAtom(),
                                                                     SwingConstants.LEFT,
                                                                     SwingConstants.LEFT,
                                                                     fontdata,
                                                                     colourforeground,
                                                                     colourbackground));

        getFlowControl().setPreferredSize(new Dimension(WIDTH_PORT_CONFIG_DROPDOWN + 20, UIComponentPlugin.HEIGHT_TOOLBAR_ICON - 4));
        getFlowControl().setMaximumSize(new Dimension(WIDTH_PORT_CONFIG_DROPDOWN + 20, UIComponentPlugin.HEIGHT_TOOLBAR_ICON - 4));
        getFlowControl().setAlignmentX(0);
        getFlowControl().setToolTipText(TOOLTIP_FLOWCONTROL);
        getFlowControl().setEnabled(hasSelection());
        getFlowControl().setEditable(false);

        ToolTipManager.sharedInstance().registerComponent(getFlowControl());

        choiceListener = new ActionListener()
            {
            public void actionPerformed(final ActionEvent event)
                {
                final PropertyPlugin propertyData;

                // This event can't occur unless there is a valid ResourceKey selection
                propertyData = (PropertyPlugin)REGISTRY.getProperties().get(getSelectedResourceKey() + KEY_PORT_FLOW_CONTROL);

                if ((propertyData != null)
                    && (propertyData.getDataType().equals(String.class.getName()))
                    && (propertyData.getResource() != null))
                    {
                    final SerialFlowControl flowControl;

                    flowControl = (SerialFlowControl)getFlowControl().getSelectedItem();
                    propertyData.setResource(flowControl.getFlowControl());

                    getSelectedCellData().setChanged(true);
                    updateGraph(scui);

                    SerialConfigurationHelper.debugCellData(null,
                                                            getSelectedCellData(),
                                                            SOURCE + "comboFlowControl",
                                                            debug);
                    SerialConfigurationHelper.logPortProperties(getSelectedResourceKey(),
                                                                getSelectedCellData().getStreamType(),
                                                                SOURCE + "comboFlowControl",
                                                                debug);
                    }
                }
            };

        getFlowControl().addActionListener(choiceListener);

        //-------------------------------------------------------------------------------------
        // Put it all together

        removeAll();

        addSeparator(UIComponentPlugin.DIM_TOOLBAR_SEPARATOR_BUTTON);
        add((Component) getPortName());
        addSeparator(UIComponentPlugin.DIM_TOOLBAR_SEPARATOR);

        add(labelBaudRate);
        addSeparator(UIComponentPlugin.DIM_LABEL_SEPARATOR);
        add(getBaudRate());
        addSeparator(UIComponentPlugin.DIM_TOOLBAR_SEPARATOR);

        add(labelDataBits);
        addSeparator(UIComponentPlugin.DIM_LABEL_SEPARATOR);
        add(getDataBits());
        addSeparator(UIComponentPlugin.DIM_TOOLBAR_SEPARATOR);

        add(labelStopBits);
        addSeparator(UIComponentPlugin.DIM_LABEL_SEPARATOR);
        add(getStopBits());
        addSeparator(UIComponentPlugin.DIM_TOOLBAR_SEPARATOR);

        add(labelParity);
        addSeparator(UIComponentPlugin.DIM_LABEL_SEPARATOR);
        add(getParity());
        addSeparator(UIComponentPlugin.DIM_TOOLBAR_SEPARATOR);

        add(labelFlowControl);
        addSeparator(UIComponentPlugin.DIM_LABEL_SEPARATOR);
        add(getFlowControl());
        addSeparator(UIComponentPlugin.DIM_TOOLBAR_SEPARATOR);

        add(Box.createHorizontalGlue());

        NavigationUtilities.updateComponentTreeUI(this);
        }


    /***********************************************************************************************
     * Set the CellData containing links to the Properties of the new selection.
     * Setting NULL results in no selection.
     *
     * @param celldata
     * @param debug
     */

    public void setSelectedCellData(final SerialConfigurationCellDataInterface celldata,
                                    final boolean debug)
        {
        this.cellData = celldata;

        if (getSelectedCellData() == null)
            {
            setSelected(false);

            getPortName().setValue("No Selection");
            getBaudRate().setSelectedIndex(0);
            getBaudRate().setEnabled(hasSelection());
            getDataBits().setSelectedIndex(0);
            getDataBits().setEnabled(hasSelection());
            getStopBits().setSelectedIndex(0);
            getStopBits().setEnabled(hasSelection());
            getParity().setSelectedIndex(0);
            getParity().setEnabled(hasSelection());
            getFlowControl().setSelectedIndex(0);
            getFlowControl().setEnabled(hasSelection());
            }
        else
            {
            final String strResourceKey;
            final StreamType streamtype;

            strResourceKey = getSelectedCellData().getResourceKey();
            streamtype = getSelectedCellData().getStreamType();

            // Do not use the usual Registry methods, we don't want Exceptions!
            switch (streamtype)
                {
                case VIRTUAL:
                    {
                    // Virtual doesn't have Serial Ports
                    setSelected(false);
                    getPortName().setValue(streamtype.getName());
                    getBaudRate().setSelectedIndex(0);
                    getBaudRate().setEnabled(hasSelection());
                    getDataBits().setSelectedIndex(0);
                    getDataBits().setEnabled(hasSelection());
                    getStopBits().setSelectedIndex(0);
                    getStopBits().setEnabled(hasSelection());
                    getParity().setSelectedIndex(0);
                    getParity().setEnabled(hasSelection());
                    getFlowControl().setSelectedIndex(0);
                    getFlowControl().setEnabled(hasSelection());
                    break;
                    }

                case STARIBUS:
                    {
                    PropertyPlugin propertyData;

                    setSelected(true);

                    propertyData = (PropertyPlugin)REGISTRY.getProperties().get(strResourceKey + KEY_PORT_NAME);

                    if ((propertyData != null)
                        && (propertyData.getDataType().equals(String.class.getName()))
                        && (propertyData.getResource() != null))
                        {
                        getPortName().setValue((String) propertyData.getResource());
                        }
                    else
                        {
                        getPortName().setValue(NO_DATA);
                        }

                    propertyData = (PropertyPlugin)REGISTRY.getProperties().get(strResourceKey + KEY_PORT_BAUDRATE);

                    if ((propertyData != null)
                        && (propertyData.getDataType().equals(Integer.class.getName()))
                        && (propertyData.getResource() != null))
                        {
                        final SerialBaudRate baudRate;

                        baudRate = SerialBaudRate.getSerialBaudRateForValue((Integer) propertyData.getResource());
                        getBaudRate().setSelectedItem(baudRate);
                        getBaudRate().setEnabled(hasSelection());
                        }
                    else
                        {
                        getBaudRate().setSelectedIndex(0);
                        getBaudRate().setEnabled(false);
                        }

                    propertyData = (PropertyPlugin)REGISTRY.getProperties().get(strResourceKey + KEY_PORT_DATA_BITS);

                    if ((propertyData != null)
                        && (propertyData.getDataType().equals(Integer.class.getName()))
                        && (propertyData.getResource() != null))
                        {
                        final SerialDataBits dataBits;

                        dataBits = SerialDataBits.getSerialDataBitsForValue((Integer)propertyData.getResource());
                        getDataBits().setSelectedItem(dataBits);
                        getDataBits().setEnabled(hasSelection());
                        }
                    else
                        {
                        getDataBits().setSelectedIndex(0);
                        getDataBits().setEnabled(false);
                        }

                    propertyData = (PropertyPlugin)REGISTRY.getProperties().get(strResourceKey + KEY_PORT_STOP_BITS);

                    if ((propertyData != null)
                        && (propertyData.getDataType().equals(Integer.class.getName()))
                        && (propertyData.getResource() != null))
                        {
                        final SerialStopBits stopBits;

                        stopBits = SerialStopBits.getSerialStopBitsForValue((Integer) propertyData.getResource());
                        getStopBits().setSelectedItem(stopBits);
                        getStopBits().setEnabled(hasSelection());
                        }
                    else
                        {
                        getStopBits().setSelectedIndex(0);
                        getStopBits().setEnabled(false);
                        }

                    propertyData = (PropertyPlugin)REGISTRY.getProperties().get(strResourceKey + KEY_PORT_PARITY);

                    if ((propertyData != null)
                        && (propertyData.getDataType().equals(Integer.class.getName()))
                        && (propertyData.getResource() != null))
                        {
                        final SerialParity parity;

                        parity = SerialParity.getSerialParityForValue((Integer) propertyData.getResource());
                        getParity().setSelectedItem(parity);
                        getParity().setEnabled(hasSelection());
                        }
                    else
                        {
                        getParity().setSelectedIndex(0);
                        getParity().setEnabled(false);
                        }

                    propertyData = (PropertyPlugin)REGISTRY.getProperties().get(strResourceKey + KEY_PORT_FLOW_CONTROL);

                    if ((propertyData != null)
                        && (propertyData.getDataType().equals(String.class.getName()))
                        && (propertyData.getResource() != null))
                        {
                        final SerialFlowControl flowControl;

                        flowControl = SerialFlowControl.getSerialFlowControlForValue((String)propertyData.getResource());
                        getFlowControl().setSelectedItem(flowControl);
                        getFlowControl().setEnabled(hasSelection());
                        }
                    else
                        {
                        getFlowControl().setSelectedIndex(0);
                        getFlowControl().setEnabled(false);
                        }

                    break;
                    }

                case SERIAL:
                    {
                    PropertyPlugin propertyData;

                    setSelected(true);

                    propertyData = (PropertyPlugin)REGISTRY.getProperties().get(strResourceKey + KEY_PORT_NAME);

                    if ((propertyData != null)
                        && (propertyData.getDataType().equals(String.class.getName()))
                        && (propertyData.getResource() != null))
                        {
                        getPortName().setValue((String) propertyData.getResource());
                        }
                    else
                        {
                        getPortName().setValue(NO_DATA);
                        }

                    propertyData = (PropertyPlugin)REGISTRY.getProperties().get(strResourceKey + KEY_PORT_BAUDRATE);

                    if ((propertyData != null)
                        && (propertyData.getDataType().equals(Integer.class.getName()))
                        && (propertyData.getResource() != null))
                        {
                        final SerialBaudRate baudRate;

                        baudRate = SerialBaudRate.getSerialBaudRateForValue((Integer) propertyData.getResource());
                        getBaudRate().setSelectedItem(baudRate);
                        getBaudRate().setEnabled(hasSelection());
                        }
                    else
                        {
                        getBaudRate().setSelectedIndex(0);
                        getBaudRate().setEnabled(false);
                        }

                    propertyData = (PropertyPlugin)REGISTRY.getProperties().get(strResourceKey + KEY_PORT_DATA_BITS);

                    if ((propertyData != null)
                        && (propertyData.getDataType().equals(Integer.class.getName()))
                        && (propertyData.getResource() != null))
                        {
                        final SerialDataBits dataBits;

                        dataBits = SerialDataBits.getSerialDataBitsForValue((Integer)propertyData.getResource());
                        getDataBits().setSelectedItem(dataBits);
                        getDataBits().setEnabled(hasSelection());
                        }
                    else
                        {
                        getDataBits().setSelectedIndex(0);
                        getDataBits().setEnabled(false);
                        }

                    propertyData = (PropertyPlugin)REGISTRY.getProperties().get(strResourceKey + KEY_PORT_STOP_BITS);

                    if ((propertyData != null)
                        && (propertyData.getDataType().equals(Integer.class.getName()))
                        && (propertyData.getResource() != null))
                        {
                        final SerialStopBits stopBits;

                        stopBits = SerialStopBits.getSerialStopBitsForValue((Integer) propertyData.getResource());
                        getStopBits().setSelectedItem(stopBits);
                        getStopBits().setEnabled(hasSelection());
                        }
                    else
                        {
                        getStopBits().setSelectedIndex(0);
                        getStopBits().setEnabled(false);
                        }

                    propertyData = (PropertyPlugin)REGISTRY.getProperties().get(strResourceKey + KEY_PORT_PARITY);

                    if ((propertyData != null)
                        && (propertyData.getDataType().equals(Integer.class.getName()))
                        && (propertyData.getResource() != null))
                        {
                        final SerialParity parity;

                        parity = SerialParity.getSerialParityForValue((Integer) propertyData.getResource());
                        getParity().setSelectedItem(parity);
                        getParity().setEnabled(hasSelection());
                        }
                    else
                        {
                        getParity().setSelectedIndex(0);
                        getParity().setEnabled(false);
                        }

                    propertyData = (PropertyPlugin)REGISTRY.getProperties().get(strResourceKey + KEY_PORT_FLOW_CONTROL);

                    if ((propertyData != null)
                        && (propertyData.getDataType().equals(String.class.getName()))
                        && (propertyData.getResource() != null))
                        {
                        final SerialFlowControl flowControl;

                        flowControl = SerialFlowControl.getSerialFlowControlForValue((String)propertyData.getResource());
                        getFlowControl().setSelectedItem(flowControl);
                        getFlowControl().setEnabled(hasSelection());
                        }
                    else
                        {
                        getFlowControl().setSelectedIndex(0);
                        getFlowControl().setEnabled(false);
                        }

                    break;
                    }

                case STARINET:
                    {
                    // Starinet doesn't have Serial Ports
                    setSelected(false);
                    getPortName().setValue(streamtype.getName());
                    getPortName().setValue(streamtype.getName());
                    getBaudRate().setSelectedIndex(0);
                    getBaudRate().setEnabled(hasSelection());
                    getDataBits().setSelectedIndex(0);
                    getDataBits().setEnabled(hasSelection());
                    getStopBits().setSelectedIndex(0);
                    getStopBits().setEnabled(hasSelection());
                    getParity().setSelectedIndex(0);
                    getParity().setEnabled(hasSelection());
                    getFlowControl().setSelectedIndex(0);
                    getFlowControl().setEnabled(hasSelection());
                    break;
                    }

                case ETHERNET:
                    {
                    // Ethernet doesn't have Serial Ports
                    setSelected(false);
                    getPortName().setValue(streamtype.getName());
                    getPortName().setValue(streamtype.getName());
                    getBaudRate().setSelectedIndex(0);
                    getBaudRate().setEnabled(hasSelection());
                    getDataBits().setSelectedIndex(0);
                    getDataBits().setEnabled(hasSelection());
                    getStopBits().setSelectedIndex(0);
                    getStopBits().setEnabled(hasSelection());
                    getParity().setSelectedIndex(0);
                    getParity().setEnabled(hasSelection());
                    getFlowControl().setSelectedIndex(0);
                    getFlowControl().setEnabled(hasSelection());
                    break;
                    }

                default:
                    {
                    setSelected(false);
                    getPortName().setValue(streamtype.getName());
                    getPortName().setValue(streamtype.getName());
                    getBaudRate().setSelectedIndex(0);
                    getBaudRate().setEnabled(hasSelection());
                    getDataBits().setSelectedIndex(0);
                    getDataBits().setEnabled(hasSelection());
                    getStopBits().setSelectedIndex(0);
                    getStopBits().setEnabled(hasSelection());
                    getParity().setSelectedIndex(0);
                    getParity().setEnabled(hasSelection());
                    getFlowControl().setSelectedIndex(0);
                    getFlowControl().setEnabled(hasSelection());
                    }
                }
            }

        validate();
        }


    /***********************************************************************************************
     * Get the CellData of the selected Port.
     *
     * @return SerialConfigurationCellDataInterface
     */

    private SerialConfigurationCellDataInterface getSelectedCellData()
        {
        return (this.cellData);
        }


    /***********************************************************************************************
     * Update the Graph, if possible.
     *
     * @param scui
     */

    private static void updateGraph(final SerialConfigurationUIComponentInterface scui)
        {
        if ((scui != null)
            && (scui.getGraphComponent() != null))
            {
            scui.getGraphComponent().clearCellOverlays();
            scui.getGraphComponent().validateGraph();
            }
        }


    /***********************************************************************************************
     * Get the ResourceKey of the configuration for the selected Port.
     *
     * @return String
     */

    private String getSelectedResourceKey()
        {
        if (getSelectedCellData() != null)
            {
            return (getSelectedCellData().getResourceKey());
            }
        else
            {
            return (EMPTY_STRING);
            }
        }


    /***********************************************************************************************
     * Indicate if there is an active Port selection.
     *
     * @return boolean
     */

    private boolean hasSelection()
        {
        return (this.hasSelection);
        }


    /***********************************************************************************************
     * Indicate if there is an active Port selection.
     *
     * @param selected
     */

    private void setSelected(final boolean selected)
        {
        this.hasSelection = selected;
        }


    /***********************************************************************************************
     * Get the Port.Name Indicator.
     *
     * @return IndicatorInterface
     */

    private IndicatorInterface getPortName()
        {
        return (this.indicatorPortName);
        }


    /***********************************************************************************************
     * Get the BaudRate JComboBox.
     *
     * @return JComboBox
     */

    private JComboBox getBaudRate()
        {
        return (this.comboBaudRate);
        }


    /***********************************************************************************************
     * Get the DataBits JComboBox.
     *
     * @return JComboBox
     */

    private JComboBox getDataBits()
        {
        return (this.comboDataBits);
        }


    /***********************************************************************************************
     * Get the StopBits JComboBox.
     *
     * @return JComboBox
     */

    private JComboBox getStopBits()
        {
        return (this.comboStopBits);
        }


    /***********************************************************************************************
     * Get the Parity JComboBox.
     *
     * @return JComboBox
     */

    private JComboBox getParity()
        {
        return (this.comboParity);
        }


    /***********************************************************************************************
     * Get the FlowControl JComboBox.
     *
     * @return JComboBox
     */

    private JComboBox getFlowControl()
        {
        return (this.comboFlowControl);
        }
    }
