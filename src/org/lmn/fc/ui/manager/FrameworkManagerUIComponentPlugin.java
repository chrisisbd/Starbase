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

package org.lmn.fc.ui.manager;

import org.lmn.fc.model.root.UserObjectPlugin;
import org.lmn.fc.ui.UIComponentPlugin;
import org.lmn.fc.ui.status.StatusIndicatorInterface;
import org.lmn.fc.ui.status.StatusIndicatorKey;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.util.EnumMap;


public interface FrameworkManagerUIComponentPlugin extends UIComponentPlugin
    {
    Dimension DIM_TREE_SIZE_MIN = new Dimension(200, 150);
    Dimension DIM_TREE_SIZE_MAX = new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
    Dimension DIM_PANEL_SIZE_MIN = new Dimension(150, 150);
    Dimension DIM_PANEL_SIZE_MAX = new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);

    boolean MODE_BROWSE = true;
    boolean MODE_EDIT = false;

    String FM_FONT = "font=Dialog style=Plain size=11";
    String FM_COLOUR = "r=0 g=0 b=200";

    String PRIVATE_TASK = "[Private]";


    void clearUIOccupant(UserObjectPlugin occupant);

    UserObjectPlugin getUIOccupant();

    void setUIOccupant(UserObjectPlugin occupant);

    void setSelectedTreeNode(DefaultMutableTreeNode node);

    DefaultMutableTreeNode getSelectedTreeNode();

    JTree getNavigationTree();

    void setNavigationTree(JTree tree);

    void validateAndUpdateUI();

    //----------------------------------------------------------------------------------------------
    // StatusBar

    void rebuildStatusBar(JComponent statusbar,
                          EnumMap<StatusIndicatorKey,
                          StatusIndicatorInterface> indicators);

    void disposeStatusBar(JComponent statusbar,
                          EnumMap<StatusIndicatorKey,
                          StatusIndicatorInterface> indicators);

    JComponent getStatusBar();

    void setStatusBar(JComponent bar);

    StatusIndicatorInterface getStatusIndicator(StatusIndicatorKey key);

    void addStatusIndicator(StatusIndicatorKey key,
                            StatusIndicatorInterface indicator);

    void removeStatusIndicator(StatusIndicatorKey key);

    void clearStatusIndicators(EnumMap<StatusIndicatorKey, StatusIndicatorInterface> indicators);

    EnumMap<StatusIndicatorKey, StatusIndicatorInterface> getStatusIndicators();

    //----------------------------------------------------------------------------------------------

    boolean getFullScreen();

    void setFullScreen(boolean fullscreen);

    UIComponentPlugin getFullScreenComponent();

    void setFullScreenComponent(UIComponentPlugin component);

    JComponent getSplitScreenComponent();

    void setSplitScreenComponent(JComponent splitpane);

    void resetCellRenderer(JTree tree);

    void toggleScreenMode();

    void toggleCustomIcons();


    void setAndSaveDividerLocation(double dividerpercentage);
    }
