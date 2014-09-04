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
import org.lmn.fc.model.datatypes.DegMinSecInterface;
import org.lmn.fc.model.datatypes.types.LongitudeDataType;
import org.lmn.fc.ui.widgets.impl.DigitalClock;

import javax.swing.*;
import java.awt.*;

public final class DigitalClockTest extends JFrame
    {
    private JPanel rackPanel;

    public static void main(final String[] args)
        {
        final DigitalClockTest ot = new DigitalClockTest();
        ot.setVisible(true);
        }


    public DigitalClockTest()
        {
        super("DigitalClockTest Test");


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
//        backpanel.setMinimumSize(ObservatoryInstrumentInterface.DIM_RACK_PANEL);
//        backpanel.setMaximumSize(ObservatoryInstrumentInterface.DIM_RACK_PANEL);
//        backpanel.setPreferredSize(ObservatoryInstrumentInterface.DIM_RACK_PANEL);
        contentPane.add(Box.createHorizontalGlue());
        contentPane.add(backpanel);
        contentPane.add(Box.createHorizontalGlue());

        DegMinSecInterface dmsLongitude = new LongitudeDataType(true, 52, 10, 0.0);
        DigitalClock clock = new DigitalClock(new Dimension(160, 60),
                                              "00:00:00",
                                              "LMST",
                                              "",
                                              "GMT+00",
                                              dmsLongitude,
                                              TimeSystem.LMT,
                                              false,
                                              null);
        clock.start();
        backpanel.add(clock);

        }

    public final JPanel getRackPanel()
        {
        return (this.rackPanel);
        }

    }
