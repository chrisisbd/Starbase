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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.misc;

import javax.swing.*;
import java.awt.*;


public class Test
        implements SwingConstants
    {
    public static void main(String[] args)
        {
        new Test();
        }


    public Test()
        {
        JFrame fr = new JFrame("Test");
        fr.getContentPane().add(testComponent());
        fr.setSize(new Dimension(600,
                                 400));
        fr.show();
        }


    JComponent testComponent()
        {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(0,
                                       3));
        panel.add(makeTabpane(null,
                              sEnglish,
                              VerticalTextIcon.ROTATE_DEFAULT));
        panel.add(makeTabpane(new Font("Osaka",
                                       0,
                                       12),
                              sJapanese,
                              VerticalTextIcon.ROTATE_DEFAULT));
        panel.add(makeTabpane(null,
                              sEnglish,
                              VerticalTextIcon.ROTATE_NONE));
        return panel;
        }


    JTabbedPane makeTabpane(Font font,
                            String[] strings,
                            int rotateHint)
        {
        JTabbedPane panel = new JTabbedPane(LEFT);
        Icon graphicIcon = UIManager.getIcon("FileView.computerIcon");
        if (font != null)
            {
            panel.setFont(font);
            }
        for (int i = 0;
             i < strings.length;
             i++)
            {
            VerticalTextIcon textIcon = new VerticalTextIcon(panel,
                                               strings[i],
                                               rotateHint);
            CompositeIcon icon = new CompositeIcon(graphicIcon,
                                                   textIcon);
            panel.addTab(null,
                         icon,
                         makePane());
            }
        return panel;
        }


    JPanel makePane()
        {
        JPanel p = new JPanel();
        p.setOpaque(false);
        return p;
        }


    static String[] sEnglish = {"Apple", "Java", "OS X"};
    static String[] sJapanese = {"\u65e5\u672c\u8a9e", "\u5c45\u3068\u3001\u304d\u3087\u3068"};
    }

