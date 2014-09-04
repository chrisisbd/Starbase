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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments;

import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.misc.GroupRearrangerMenu;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.misc.InstrumentRearrangerMenu;
import org.lmn.fc.model.plugins.AtomPlugin;
import org.lmn.fc.model.users.UserPlugin;
import org.lmn.fc.model.xmlbeans.attributes.AttributesDocument;
import org.lmn.fc.model.xmlbeans.groups.Definition;
import org.lmn.fc.model.xmlbeans.groups.ObservatoryGroupDefinitionsDocument;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;
import org.lmn.fc.model.xmlbeans.instruments.InstrumentsDocument;
import org.lmn.fc.ui.UIComponentPlugin;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;


public interface ObservatoryUIInterface extends UIComponentPlugin
    {
    // String Resources
    String TOOLTIP_HIDE_RACK = "Click to hide the rack of Instruments";
    String TOOLTIP_SHOW_RACK = "Click to show the rack of Instruments";

    String FONT_TTF_OBSERVATORY = "LucidaBrightItalic.ttf";

    String ICON_SELECTOR_HIDE = "selector_hide.png";
    String ICON_SELECTOR_SHOW = "selector_show.png";

    float FONT_SIZE_TAB = 14.0f;
    int INDEX_INITIAL_GROUP_SELECTION = 0;
    int INDEX_INITIAL_GROUP_SORT_INDEX = 0;

    // The maximum number of Instrument Groups is constrained by the XSD
    int MAX_GROUPS = 10;


    /***********************************************************************************************
     * Get the List of all Instruments in the Observatory.
     *
     * @return List<ObservatoryInstrumentInterface>
     */

    List<ObservatoryInstrumentInterface> getObservatoryInstruments();


    /***********************************************************************************************
     * Get the currently selected GroupID.
     *
     * @return int
     */

    int getCurrentGroupID();


    /***********************************************************************************************
     * Set the currently selected GroupID.
     *
     * @param groupid
     */

    void setCurrentGroupID(final int groupid);


    /***********************************************************************************************
     * Get the InstrumentSelector panel for the current Group.
     *
     * @return InstrumentSelector
     */

    InstrumentSelector getCurrentGroupInstrumentSelector();


    /***********************************************************************************************
     * Get the List of GroupIDs.
     *
     * @return List<Integer>
     */

    List<Integer> getGroupIDs();


    /***********************************************************************************************
     * Get the Hashtable of Instrument Group Definitions,indexed by GroupID.
     *
     * @return Hashtable<Integer, Definition>
     */

    Hashtable<Integer, Definition> getGroupDefinitionsTable();


    /***********************************************************************************************
     * Get the Hashtable of Lists of Instruments in the whole Observatory,indexed by GroupID.
     *
     * @return Hashtable<Integer, Instrument>
     */

    Hashtable<Integer, java.util.List<Instrument>> getGroupsToInstrumentsTable();


    /***********************************************************************************************
     * Get the table of InstrumentSelectors.
     *
     * @return List<InstrumentSelector>
     */

    Hashtable<Integer, InstrumentSelector> getInstrumentSelectorsTable();


    /***********************************************************************************************
     * Get the UIPanel.
     *
     * @return JComponent
     */

    JComponent getUIPanel();


    /***********************************************************************************************
     * Get the Observatory Font.
     *
     * @return Font
     */
    Font getObservatoryFont();


    /***********************************************************************************************
     * Set the Observatory Font.
     *
     * @param font
     */

    void setObservatoryFont(Font font);


    /***********************************************************************************************
     * Get the PopupMenu used to rearrange Group Tabs.
     *
     * @return GroupRearrangerMenu
     */

    GroupRearrangerMenu getGroupRearrangerMenu();


    /***********************************************************************************************
     * Set the PopupMenu used to rearrange Group Tabs.
     *
     * @param menu
     */

    void setGroupRearrangerMenu(GroupRearrangerMenu menu);


    /***********************************************************************************************
     * Get the PopupMenu used to rearrange Instruments in the Rack.
     *
     * @return InstrumentRearrangerMenu
     */

    InstrumentRearrangerMenu getInstrumentRearrangerMenu();


    /***********************************************************************************************
     * Set the PopupMenu used to rearrange Instruments in the Rack.
     *
     * @param menuInstrument
     */

    void setInstrumentRearrangerMenu(InstrumentRearrangerMenu menuInstrument);



    UIComponentPlugin getUIOccupant();

    void setUIOccupant(UIComponentPlugin newoccupant);

    /***********************************************************************************************
     * Get the ObservatoryGroups document.
     *
     * @return ObservatoryGroupDefinitionsDocument
     */

    InstrumentsDocument getInstrumentsDoc();

    /***********************************************************************************************
     * Get the Attributes document.
     *
     * @return AttributesDocument
     */

    AttributesDocument getAttributesDoc();


    ObservatoryGroupDefinitionsDocument getObservatoryGroupsDoc();

    /***********************************************************************************************
     * Get the UserPlugin.
     *
     * @return UserPlugin
     */

    UserPlugin getUserPlugin();


    AtomPlugin getHostAtom();


    /**********************************************************************************************/
    /* Provide access to useful Instruments                                                       */
    /***********************************************************************************************
     * Get the ObservatoryClock.
     *
     * @return ObservatoryClockInterface
     */

    ObservatoryClockInterface getObservatoryClock();


    /***********************************************************************************************
     * Set the ObservatoryClock.
     *
     * @param clock
     */

    void setObservatoryClock(ObservatoryClockInterface clock);


    /***********************************************************************************************
     * Indicate if the ObservatoryClock has been found, prior to using PlatformClock if not found.
     *
     * @return boolean
     */

    boolean hasObservatoryClock();

    void foundObservatoryClock(boolean found);

    ObservatoryClockInterface getPlatformClock();

    ObservatoryLogInterface getObservatoryLog();

    void setObservatoryLog(ObservatoryLogInterface log);


    String getResourceKey();
    }
