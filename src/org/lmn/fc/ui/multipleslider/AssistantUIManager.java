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

package org.lmn.fc.ui.multipleslider;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;


public class AssistantUIManager
    {

    public static ComponentUI createUI(JComponent c)
        {
        String componentName = c.getClass().getName();

        int index = componentName.lastIndexOf(".") + 1;
        StringBuffer sb = new StringBuffer();
        sb.append(componentName.substring(0, index));

        //
        // UIManager.getLookAndFeel().getName()
        //
        // [ Metal ] [  Motif  ] [   Mac   ] [ Windows ]
        //   Metal    CDE/Motif   Macintosh    Windows
        //

        String lookAndFeelName = UIManager.getLookAndFeel().getName();
        if (lookAndFeelName.startsWith("CDE/"))
            {
            lookAndFeelName = lookAndFeelName.substring(4, lookAndFeelName.length());
            }
        sb.append(lookAndFeelName);
        sb.append(componentName.substring(index));
        sb.append("UI");

        ComponentUI componentUI = getInstance(sb.toString());

        if (componentUI == null)
            {
            sb.setLength(0);
            sb.append(componentName.substring(0, index));
            sb.append("Basic");
            sb.append(componentName.substring(index));
            sb.append("UI");
            componentUI = getInstance(sb.toString());
            }

        return componentUI;
        }


    private static ComponentUI getInstance(String name)
        {
        try
            {
            return (ComponentUI) Class.forName(name).newInstance();
            }
        catch (ClassNotFoundException ex)
            {
            System.out.println("AssistantUIManager.setUIName() ClassNotFoundException");
            ex.printStackTrace();
            }
        catch (IllegalAccessException ex)
            {
            System.out.println("AssistantUIManager.setUIName() IllegalAccessException");
            ex.printStackTrace();
            }
        catch (InstantiationException ex)
            {
            System.out.println("AssistantUIManager.setUIName() InstantiationException");
            ex.printStackTrace();
            }

        return null;
        }


    public static void setUIName(JComponent c)
        {
        String key = c.getUIClassID();
        String uiClassName = (String) UIManager.get(key);

        System.out.println("AssistantUIManager.setUIName() key=" + key);
        System.out.println("AssistantUIManager.setUIName() uiClassName=" + uiClassName);

        if (uiClassName == null)
            {
            String componentName = c.getClass().getName();
            int index = componentName.lastIndexOf(".") + 1;
            StringBuffer sb = new StringBuffer();
            sb.append(componentName.substring(0, index));
            System.out.println("AssistantUIManager.setUIName() 0 buffer=" + sb);
            String lookAndFeelName = UIManager.getLookAndFeel().getName();

            if (lookAndFeelName.startsWith("CDE/"))
                {
                lookAndFeelName = lookAndFeelName.substring(4, lookAndFeelName.length());
                }

            sb.append(lookAndFeelName);
            System.out.println("AssistantUIManager.setUIName() 1 buffer=" + sb);
            sb.append(key);
            System.out.println("AssistantUIManager.setUIName() 2 buffer=" + sb);
            UIManager.put(key, sb.toString());

            System.out.println("AssistantUIManager.setUIName() [key=" + key + "] [value=" + sb + "]");
            }
        else
            {
            System.out.println("AssistantUIManager.setUIName() uiClassName not NULL");
            }
        }


    public AssistantUIManager()
        {
        }


    }
