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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs;

import org.lmn.fc.common.constants.*;
import org.lmn.fc.ui.UIComponentPlugin;

import javax.accessibility.Accessible;
import java.awt.*;
import java.awt.image.ImageObserver;
import java.io.Serializable;


/***************************************************************************************************
 * TabToolbarInterface.
 */

public interface TabToolbarInterface extends ImageObserver,
                                             MenuContainer,
                                             Serializable,
                                             Accessible,
                                             FrameworkConstants,
                                             FrameworkStrings,
                                             FrameworkMetadata,
                                             FrameworkRegex,
                                             FrameworkSingletons,
                                             ResourceKeys
    {
    // String Resources
    String TOOLTIP_START = "Back to first page";
    String TOOLTIP_BACK_ONE = "Back one page";
    String TOOLTIP_BACK_TEN = "Back ten pages";
    String TOOLTIP_FORWARD_ONE = "Forward one page";
    String TOOLTIP_FORWARD_TEN = "Forward ten pages";
    String TOOLTIP_END = "Forward to last page";
    String TOOLTIP_ZOOM_IN = "Zoom In";
    String TOOLTIP_ZOOM_OUT = "Zoom Out";
    String TOOLTIP_REMOVE = "Remove Document";
    String TOOLTIP_RELOAD = "Reload Document";

    String PAGE = "Page";
    String PAGE_NUMBER_BLANK = "   ";
    String PAGE_OF_BLANK = "of ";

    String DIALOG_PRINT = "Print the tab contents";
    String MSG_TAB_VIEWER = "the Viewer tab";
    String MSG_TAB_VIEWER_PRINTED = "The Viewer tab has been printed";

    Dimension DIM_PAGE_NUMBER = new Dimension(30, UIComponentPlugin.HEIGHT_TOOLBAR_ICON);

    float ZOOM_INITIAL = 1.4f;
    float ZOOM_INCREMENT = 0.4f;
    float ZOOM_MIN = 0.6f;
    float ZOOM_MAX = 3.0f;


    /**********************************************************************************************/
    // UI                                                                                         */
    /***********************************************************************************************
     * Initialise all UI components on the toolbar.
     */

    void initialiseUI();


    /***********************************************************************************************
     * Dispose of all UI components on the toolbar.
     */

    void disposeUI();


    /***********************************************************************************************
     * Set the PublisherUI controlled by this Toolbar.
     *
     * @param publisher
     */

    void setPublisherUI(PublisherUIComponentInterface publisher);


    /**********************************************************************************************/
    // UI State                                                                                   */
    /***********************************************************************************************
     * Update the page indicator with the current page and total count.
     *
     * @param currentpage
     * @param pagecount
     */

    void updatePageIndicator(int currentpage,
                             int pagecount);
    }
