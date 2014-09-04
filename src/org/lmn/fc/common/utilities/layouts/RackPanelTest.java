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

package org.lmn.fc.common.utilities.layouts;

import org.lmn.fc.common.utilities.time.TimeSystem;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.InstrumentSelector;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.RackPanel;
import org.lmn.fc.model.datatypes.DegMinSecInterface;
import org.lmn.fc.model.datatypes.types.LongitudeDataType;
import org.lmn.fc.model.registry.RegistryModelUtilities;
import org.lmn.fc.ui.widgets.impl.DigitalClock;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public final class RackPanelTest extends JFrame
    {
    private JPanel rackPanel;

    public static void main(final String[] args)
        {
        final RackPanelTest ot = new RackPanelTest();
        ot.setVisible(true);
        }


    public RackPanelTest()
        {
        super("RackPanelTest Test");


        final JLabel label;
        final ImageIcon icon;

        setSize(500, 300);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        final Container contentPane = getContentPane();
        BoxLayout boxlayout = new BoxLayout(contentPane, BoxLayout.X_AXIS);
        contentPane.setLayout(boxlayout);
        contentPane.setBackground(Color.white);

        JPanel backpanel = new JPanel(new BorderLayout());
        backpanel.setBackground(Color.gray);
        backpanel.setMinimumSize(InstrumentSelector.DIM_SELECTOR_SIZE_MIN);
        backpanel.setMaximumSize(InstrumentSelector.DIM_SELECTOR_SIZE_MIN);
        backpanel.setPreferredSize(InstrumentSelector.DIM_SELECTOR_SIZE_MIN);
        contentPane.add(Box.createHorizontalGlue());
        contentPane.add(backpanel);
        contentPane.add(Box.createHorizontalGlue());

        //------------------------------------------------------------------------------------------

        rackPanel = new JPanel();
        getRackPanel().setLayout(new OverlayLayout(getRackPanel()));
        getRackPanel().setBackground(Color.red);
        getRackPanel().setToolTipText("Rack panel");
        backpanel.add(getRackPanel(), BorderLayout.CENTER);

        JPanel panel1 = new JPanel();
        panel1.setLayout(new BoxLayout(panel1, BoxLayout.Y_AXIS));
        final Dimension dimPanel = new Dimension(RackPanel.PANEL_1U.getPixelWidth() - 36,
                                                 (RackPanel.PANEL_1U.getPixelHeight() << 1) - 8);
        //panel1.setBorder(BorderFactory.createEtchedBorder());
        panel1.setOpaque(false);
        panel1.setMinimumSize(dimPanel);
        panel1.setMaximumSize(dimPanel);
        panel1.setPreferredSize(dimPanel);
        panel1.setAlignmentX(0.5f);
        panel1.setAlignmentY(0.5f);
        getRackPanel().add(panel1);



        final Dimension dimControlPanel = new Dimension(dimPanel.width,
                                                        dimPanel.height - 18);
        final JPanel controlPanel = new JPanel();
        controlPanel.setOpaque(false);
        controlPanel.setMinimumSize(dimControlPanel);
        controlPanel.setMaximumSize(dimControlPanel);
        controlPanel.setPreferredSize(dimControlPanel);

        panel1.add(controlPanel);

        //------------------------------------------------------------------------------------------
        // Buttons

        final JPanel panelButtons = new JPanel();
        final JButton buttonOn = new JButton();
        final JButton buttonOff = new JButton();
        final Dimension dimButton = new Dimension(15, 15);

//        panelButtons.setBorder(BorderFactory.createEtchedBorder());
        panelButtons.setLayout(new BoxLayout(panelButtons, BoxLayout.X_AXIS));
        panelButtons.setMinimumSize(new Dimension(35, 17));
        panelButtons.setMaximumSize(new Dimension(35, 17));
        panelButtons.setPreferredSize(new Dimension(35, 17));

        buttonOn.setBackground(Color.green);
        buttonOn.setMinimumSize(dimButton);
        buttonOn.setMaximumSize(dimButton);
        buttonOn.setPreferredSize(dimButton);
        buttonOn.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                buttonOn.setBackground(buttonOn.getBackground().darker());
                buttonOn.setEnabled(false);
                buttonOn.setToolTipText("");

                buttonOff.setBackground(buttonOff.getBackground().brighter());
                buttonOff.setEnabled(true);
                buttonOff.setToolTipText("Stop the Instrument");
                }
            });

        buttonOff.setBackground(Color.red);
        buttonOff.setMinimumSize(dimButton);
        buttonOff.setMaximumSize(dimButton);
        buttonOff.setPreferredSize(dimButton);
        buttonOff.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                buttonOn.setBackground(buttonOn.getBackground().brighter());
                buttonOn.setEnabled(true);
                buttonOn.setToolTipText("Start the Instrument");

                buttonOff.setBackground(buttonOff.getBackground().darker());
                buttonOff.setEnabled(false);
                buttonOff.setToolTipText("");
                }
            });

        buttonOn.setBackground(buttonOn.getBackground().brighter());
        buttonOn.setEnabled(true);
        buttonOn.setToolTipText("Start the Instrument");

        buttonOff.setBackground(buttonOff.getBackground().darker());
        buttonOff.setEnabled(false);
        buttonOff.setToolTipText("");

        panelButtons.add(buttonOn);
        panelButtons.add(Box.createHorizontalStrut(4));
        panelButtons.add(buttonOff);

        //------------------------------------------------------------------------------------------

        final JPanel fixedPanel = new JPanel();
//        fixedPanel.setBorder(BorderFactory.createEtchedBorder());
        fixedPanel.setLayout(new BoxLayout(fixedPanel, BoxLayout.X_AXIS));

        JLabel labelText = new JLabel("<html><font size=2>Observatory Clock</font></html>",
                                      SwingConstants.RIGHT);
        fixedPanel.add(panelButtons);
        fixedPanel.add(labelText);

        panel1.add(fixedPanel);

        label = new JLabel();
        icon = RegistryModelUtilities.getCommonIcon("blankpanel.png");
        label.setIcon(icon);
        label.setBackground(Color.green);
        label.setToolTipText("Blank Panel");
        label.setAlignmentX(0.5f);
        label.setAlignmentY(0.5f);
        label.addMouseListener(new MouseAdapter()
            {
            public void mouseClicked(final MouseEvent event)
                {
                System.out.println("rack image click");
                }
            });
        getRackPanel().add(label);

        DegMinSecInterface dmsLongitude = new LongitudeDataType(true, 52, 10, 0.0);
        DigitalClock clock = new DigitalClock(new Dimension(150, 35),
                                              "00:00:00",
                                              "",
                                              "",
                                              "GMT+00",
                                              dmsLongitude,
                                              TimeSystem.LMT,
                                              false,
                                              null);
        clock.start();
        controlPanel.add(clock);

        }

    public final JPanel getRackPanel()
        {
        return (this.rackPanel);
        }

    }
