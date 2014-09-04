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

import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryUIInterface;
import org.lmn.fc.model.xmlbeans.attributes.AttributesDocument;
import org.lmn.fc.model.xmlbeans.attributes.Configuration;
import org.lmn.fc.model.xmlbeans.groups.Group;
import org.lmn.fc.model.xmlbeans.groups.ObservatoryGroups;

import javax.swing.*;
import java.awt.*;


/***************************************************************************************************
 * InstrumentRearrangerMenu.
 */

public final class InstrumentRearrangerMenu extends JPopupMenu
    {
    private ObservatoryUIInterface hostUI;
    private ObservatoryInstrumentInterface observatoryInstrument;


    /***********************************************************************************************
     * Indicate if the specified Instrument is locked against rearrangments in the specified Group.
     *
     * @param attributes
     * @param groupid
     * @param instrument
     *
     * @return boolean
     */

    public static boolean isLocked(final AttributesDocument.Attributes attributes,
                                   final int groupid,
                                   final ObservatoryInstrumentInterface instrument)
        {
        boolean boolLocked;

        boolLocked = false;

        if ((attributes != null)
            && (groupid >= 0)
            && (instrument != null)
            && (instrument.getInstrument() != null))
            {
            final java.util.List<Configuration> listConfiguration;
            boolean boolSuccess;

            // Try to find the Locked status where:
            // Attributes:Configuration:Identifier = instrument.getInstrument().getIdentifier()
            // AND
            // Attributes:Configuration:ObservatoryGroups:Group:GroupID = hostui.getCurrentGroupID()

            listConfiguration = attributes.getConfigurationList();
            boolSuccess = false;

            // This could be done using XPath, which would do much the same internally...
            for (int i = 0;
                 (!boolSuccess) && (i < listConfiguration.size());
                 i++)
                {
                final Configuration configuration;

                configuration = listConfiguration.get(i);

                if ((configuration != null)
                    && (configuration.getIdentifier().equals(instrument.getInstrument().getIdentifier())))
                    {
                    final ObservatoryGroups groups;
                    final java.util.List<Group> listGroup;

                    // We have found the Instrument, now search its ObservatoryGroups for CurrentGroupID
                    groups = configuration.getObservatoryGroups();
                    listGroup = groups.getGroupList();

                    for (int j = 0;
                         j < listGroup.size();
                         j++)
                        {
                        final Group group;

                        group = listGroup.get(j);

                        if ((group != null)
                            && (group.getGroupID() == groupid))
                            {
                            // We have found the correct Group, so get the Lock status
                            boolLocked = group.getLocked();

                            // Leave immediately
                            boolSuccess = true;
                            }
                        }
                    }
                }
            }

        return (boolLocked);
        }


    /***********************************************************************************************
     * Set the lock status of the specified Instrument for rearrangments in the specified Group.
     *
     * @param attributes
     * @param groupid
     * @param instrument
     * @param locked
     */

    public static void setLocked(final AttributesDocument.Attributes attributes,
                                 final int groupid,
                                 final ObservatoryInstrumentInterface instrument,
                                 final boolean locked)
        {
        if ((attributes != null)
            && (groupid >= 0)
            && (instrument != null)
            && (instrument.getInstrument() != null))
            {
            final java.util.List<Configuration> listConfiguration;
            boolean boolSuccess;

            // Try to set the Locked status where:
            // Attributes:Configuration:Identifier = instrument.getInstrument().getIdentifier()
            // AND
            // Attributes:Configuration:ObservatoryGroups:Group:GroupID = hostui.getCurrentGroupID()

            listConfiguration = attributes.getConfigurationList();
            boolSuccess = false;

            // This could be done using XPath, which would do much the same internally...
            for (int i = 0;
                 (!boolSuccess) && (i < listConfiguration.size());
                 i++)
                {
                final Configuration configuration;

                configuration = listConfiguration.get(i);

                if ((configuration != null)
                    && (configuration.getIdentifier().equals(instrument.getInstrument().getIdentifier())))
                    {
                    final ObservatoryGroups groups;
                    final java.util.List<Group> listGroup;

                    // We have found the Instrument, now search its ObservatoryGroups for CurrentGroupID
                    groups = configuration.getObservatoryGroups();
                    listGroup = groups.getGroupList();

                    for (int j = 0;
                         j < listGroup.size();
                         j++)
                        {
                        final Group group;

                        group = listGroup.get(j);

                        if ((group != null)
                            && (group.getGroupID() == groupid))
                            {
                            // We have found the correct Group, so set the Lock status
                            group.setLocked(locked);

                            // Leave immediately
                            boolSuccess = true;
                            }
                        }
                    }
                }
            }
        }


    /***********************************************************************************************
     * Displays the popup menu at the position x,y in the coordinate
     * space of the component invoker.
     *
     * @param hostui
     * @param instrument
     * @param invoker the component in whose space the popup menu is to appear
     * @param x the x coordinate in invoker's coordinate space at which
     *        the popup menu is to be displayed
     * @param y the y coordinate in invoker's coordinate space at which
     * @param controldown allows the user to override the lock
     */

    public void show(final ObservatoryUIInterface hostui,
                     final ObservatoryInstrumentInterface instrument,
                     final Component invoker,
                     final int x,
                     final int y,
                     final boolean controldown)
        {
        setHostUI(hostui);
        setObservatoryInstrument(instrument);

        // Only show the menu if we are allowed to rearrange!
        if ((controldown)
            || (!isLocked(getHostUI().getAttributesDoc().getAttributes(),
                  getHostUI().getCurrentGroupID(),
                  getObservatoryInstrument())))
            {
            super.show(invoker, x, y);
            }
        else
            {
            Toolkit.getDefaultToolkit().beep();
            }
        }


    /***********************************************************************************************
     * Get the ObservatoryUI that is associated with this Menu.
     *
     * @return ObservatoryUIInterface
     */

    private ObservatoryUIInterface getHostUI()
        {
        return (this.hostUI);
        }


    /***********************************************************************************************
     * Set the ObservatoryUI that is associated with this Menu.
     *
     * @param hostui
     */

    private void setHostUI(final ObservatoryUIInterface hostui)
        {
        this.hostUI = hostui;
        }


    /***********************************************************************************************
     * Get the ObservatoryInstrument that is associated with this Menu.
     *
     * @return ObservatoryInstrumentInterface
     */

    public ObservatoryInstrumentInterface getObservatoryInstrument()
        {
        return (this.observatoryInstrument);
        }


    /***********************************************************************************************
     * Set the ObservatoryInstrument that is associated with this Menu.
     *
     * @param instrument
     */

    private void setObservatoryInstrument(final ObservatoryInstrumentInterface instrument)
        {
        this.observatoryInstrument = instrument;
        }
    }
