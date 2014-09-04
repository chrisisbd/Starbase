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


package org.lmn.fc.ui.datastore;

import javax.swing.*;
import java.awt.*;

// sqlbob@users 20020407 - patch 1.7.0 - reengineering

/**
 * Common code in the Swing versions of DatabaseManager and Tranfer
 *
 * @version 1.7.0
 */
final class CommonSwing
    {

    static void setDefaultColor()
        {

        final Color hsqlBlue = new Color(102, 153, 204);
        final Color hsqlGreen = new Color(153, 204, 204);
        final UIDefaults d = UIManager.getLookAndFeelDefaults();

        d.put("MenuBar.background", SystemColor.control);
        d.put("Menu.background", SystemColor.control);
        d.put("Menu.selectionBackground", hsqlBlue);
        d.put("MenuItem.background", SystemColor.menu);
        d.put("MenuItem.selectionBackground", hsqlBlue);
        d.put("Separator.foreground", SystemColor.controlDkShadow);
        d.put("Button.background", SystemColor.control);
        d.put("CheckBox.background", SystemColor.control);
        d.put("Label.background", SystemColor.control);
        d.put("Label.foreground", Color.black);
        d.put("Panel.background", SystemColor.control);
        d.put("PasswordField.selectionBackground", hsqlGreen);
        d.put("PasswordField.background", SystemColor.white);
        d.put("TextArea.selectionBackground", hsqlGreen);
        d.put("TextField.background", SystemColor.white);
        d.put("TextField.selectionBackground", hsqlGreen);
        d.put("TextField.background", SystemColor.white);
        d.put("ScrollBar.background", SystemColor.controlHighlight);
        d.put("ScrollBar.foreground", SystemColor.control);
        d.put("ScrollBar.track", SystemColor.controlHighlight);
        d.put("ScrollBar.trackHighlight", SystemColor.controlDkShadow);
        d.put("ScrollBar.thumb", SystemColor.control);
        d.put("ScrollBar.thumbHighlight", SystemColor.controlHighlight);
        d.put("ScrollBar.thumbDarkShadow", SystemColor.controlDkShadow);
        d.put("ScrollBar.thumbLightShadow", SystemColor.controlShadow);
        d.put("ComboBox.background", SystemColor.control);
        d.put("ComboBox.selectionBackground", hsqlBlue);
        d.put("Table.background", SystemColor.white);
        d.put("Table.selectionBackground", hsqlBlue);
        d.put("TableHeader.background", SystemColor.control);

        // This doesn't seem to work.
        d.put("SplitPane.background", SystemColor.control);
        d.put("Tree.selectionBackground", hsqlBlue);
        d.put("List.selectionBackground", hsqlBlue);
        }

    private CommonSwing()
        {
        }
    }
