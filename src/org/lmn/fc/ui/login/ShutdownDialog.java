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

package org.lmn.fc.ui.login;

import org.lmn.fc.ui.layout.BoxLayoutFixed;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;


/***************************************************************************************************
 * ShutdownDialog.
 */

public final class ShutdownDialog extends JDialog
    {
    /*******************************************************************************************
     * ShutdownDialog.
     *
     * @param title
     * @param message
     */

    public ShutdownDialog(final String title,
                          final String message)
        {
        super((Frame) null, title, false);

        final JPanel panelShutdown = new JPanel();
        panelShutdown.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
        panelShutdown.setLayout(new BoxLayoutFixed(panelShutdown, BoxLayoutFixed.Y_AXIS));

        final JLabel labelShutdown = new JLabel(message);
        panelShutdown.add(labelShutdown);
        getContentPane().add(panelShutdown, BorderLayout.CENTER);
        pack();
        setResizable(false);

        // Now that we have the preferred sizes, we can work out where to put the JDialog
        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        final Dimension windowSize = getPreferredSize();

        setLocation((screenSize.width / 2) - (windowSize.width / 2),
                    (screenSize.height / 2) - (windowSize.height / 2));

        // Add a listener, so we can shutdown cleanly...
        // See similar code in FrameworkData.frameworkExit(), UserInterfaceFrame.initialiseUI() and LoginTab.initialiseUI()
        addWindowListener(new WindowAdapter()
            {
            public void windowClosing(final WindowEvent event)
                {
                // If the user doesn't want to watch, we can't make them...
                dispose();
                }
            });
        }
    }
