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

package org.lmn.fc.ui.status;

import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.model.plugins.FrameworkPlugin;

import javax.accessibility.Accessible;
import javax.swing.*;
import java.awt.*;
import java.awt.image.ImageObserver;
import java.io.Serializable;


public interface StatusIndicatorInterface extends ImageObserver,
                                                  MenuContainer,
                                                  Serializable,
                                                  SwingConstants,
                                                  Accessible,
                                                  FrameworkConstants,
                                                  FrameworkStrings,
                                                  FrameworkSingletons
    {
    // String Resources
    String STATUS_NO_FIX = "No fix";
    String STATUS_NO_DATA = "No data";

    int STATUS_HEIGHT = 26;

    Dimension DIM_STATUS_MIN = new Dimension(100, STATUS_HEIGHT);
    Dimension DIM_STATUS_PREF = new Dimension(Short.MAX_VALUE, STATUS_HEIGHT);
    Dimension DIM_STATUS_MAX = new Dimension(Short.MAX_VALUE, STATUS_HEIGHT);

    void initialiseUI();

    void runUI();

    void stopUI();

    void disposeUI();

    void setText(String text);

    String getText();

    void setTextColour(ColourInterface colour);

    void setTextFont(FontInterface font);

    String getTooltip();

    void setTooltip(String tooltip);

    void setIcon(Icon icon);

    String getUpdateStatus(FrameworkPlugin framework);
    }
