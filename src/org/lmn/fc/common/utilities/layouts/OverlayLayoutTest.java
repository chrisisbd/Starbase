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

// http://www.java2s.com/Code/Java/Swing-JFC/AtestoftheOverlayLayoutmanagerallowingexperimentation.htm
/*
Java Swing, 2nd Edition
By Marc Loy, Robert Eckstein, Dave Wood, James Elliott, Brian Cole
ISBN: 0-596-00408-7
Publisher: O'Reilly
*/
//OverlayTest.java
//A test of the OverlayLayout manager allowing experimentation.
//

import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.InstrumentSelector;
import org.lmn.fc.model.registry.RegistryModelUtilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public final class OverlayLayoutTest extends JFrame
    {

    public static void main(final String[] args)
        {
        final OverlayLayoutTest ot = new OverlayLayoutTest();
        ot.setVisible(true);
        }

    public OverlayLayoutTest()
        {
        super("OverlayLayout Test");
        setSize(500, 300);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        final Container c = getContentPane();
        c.setLayout(new GridBagLayout());
        c.setBackground(Color.white);

        final JPanel p1 = new GridPanel();

        final OverlayLayout overlay = new OverlayLayout(p1);
        p1.setLayout(overlay);


        final ImageIcon icon;
        final JLabel label = new JLabel();

        icon = RegistryModelUtilities.getCommonIcon("blankpanel.png");
        label.setIcon(icon);
        label.setBackground(Color.green);
        label.setMinimumSize(InstrumentSelector.DIM_SELECTOR_SIZE_MIN);
        label.setMaximumSize(InstrumentSelector.DIM_SELECTOR_SIZE_MIN);
        label.setPreferredSize(InstrumentSelector.DIM_SELECTOR_SIZE_MIN);
        label.setToolTipText("jdjdjdjd");



        final JButton jb1 = new JButton("B1");
        final JButton jb2 = new JButton("B2");
        final JButton jb3 = new JButton("B3");

        final Dimension b1 = new Dimension(60, 60);
        final Dimension b2 = new Dimension(170, 30);
        final Dimension b3 = new Dimension(250, 77);

        jb1.setMinimumSize(b1);
        jb1.setMaximumSize(b1);
        jb1.setPreferredSize(b1);
        jb2.setMinimumSize(b2);
        jb2.setMaximumSize(b2);
        jb2.setPreferredSize(b2);
        jb3.setMinimumSize(b3);
        jb3.setMaximumSize(b3);
        jb3.setPreferredSize(b3);

        final SimpleReporter reporter = new SimpleReporter();
        jb1.addActionListener(reporter);
        jb2.addActionListener(reporter);
        jb3.addActionListener(reporter);

        p1.add(jb1);
        p1.add(jb2);
        //p1.add(jb3);
        p1.add(label);

        final JPanel p2 = new JPanel();
        p2.setLayout(new GridLayout(2, 6));
        p2.add(new JLabel("B1 X", JLabel.CENTER));
        p2.add(new JLabel("B1 Y", JLabel.CENTER));
        p2.add(new JLabel("B2 X", JLabel.CENTER));
        p2.add(new JLabel("B2 Y", JLabel.CENTER));
        p2.add(new JLabel("B3 X", JLabel.CENTER));
        p2.add(new JLabel("B3 Y", JLabel.CENTER));
        p2.add(new JLabel(""));

        final JTextField x1 = new JTextField("0.0", 4); // Button1 x alignment
        final JTextField y1 = new JTextField("0.0", 4); // Button1 y alignment
        final JTextField x2 = new JTextField("0.0", 4);
        final JTextField y2 = new JTextField("0.0", 4);
        final JTextField x3 = new JTextField("0.0", 4);
        final JTextField y3 = new JTextField("0.0", 4);

        p2.add(x1);
        p2.add(y1);
        p2.add(x2);
        p2.add(y2);
        p2.add(x3);
        p2.add(y3);


        final GridBagConstraints constraints = new GridBagConstraints();
        c.add(p1, constraints);

        constraints.gridx = 1;
        final JButton updateButton = new JButton("Update");
        updateButton.addActionListener(new ActionListener()
        {
        public void actionPerformed(final ActionEvent ae)
            {
            jb1.setAlignmentX(
                Float.valueOf(x1.getText().trim()));
            jb1.setAlignmentY(
                Float.valueOf(y1.getText().trim()));
            jb2.setAlignmentX(
                Float.valueOf(x2.getText().trim()));
            jb2.setAlignmentY(
                Float.valueOf(y2.getText().trim()));
            label.setAlignmentX(
                Float.valueOf(x3.getText().trim()));
            label.setAlignmentY(
                Float.valueOf(y3.getText().trim()));

            p1.revalidate();
            }
        });
        c.add(updateButton, constraints);

        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 2;
        c.add(p2, constraints);
        }

    public final class SimpleReporter implements ActionListener
        {
        public final void actionPerformed(final ActionEvent ae)
            {
            System.out.println(ae.getActionCommand());
            }
        }

    public final class GridPanel extends JPanel
        {
        public GridPanel()
            {
            setBackground(Color.white);
            }

        public final void paint(final Graphics g)
            {
            super.paint(g);
            final int w = getSize().width;
            final int h = getSize().height;

            g.setColor(Color.red);
            g.drawRect(0, 0, w - 1, h - 1);
            g.drawLine(w >> 1, 0, w >> 1, h);
            g.drawLine(0, h >> 1, w, h >> 1);
            }
        }
    }