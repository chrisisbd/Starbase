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

package org.lmn.fc.frameworks.starbase;

import org.lmn.fc.common.actions.ContextAction;
import org.lmn.fc.frameworks.starbase.ui.userinterface.StarbaseBrandingUIComponent;
import org.lmn.fc.model.plugins.AtomPlugin;
import org.lmn.fc.model.plugins.impl.FrameworkData;
import org.lmn.fc.model.root.ActionGroup;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.net.URL;


/***************************************************************************************************
 * Starbase.
 */

public class Starbase extends FrameworkData
                      implements StarbaseMBean
    {
    /***********************************************************************************************
     * Construct Starbase.
     */

    private Starbase()
        {
        super(-131413008921463712L);
        }


    /***********************************************************************************************
     * Start up the Atom.
     *
     * @return boolean
     */

    public boolean startupAtom()
        {
        final AtomPlugin atom;
        URL imageURL;

        // For use within inner classes
        atom = this;

        if (super.startupAtom())
            {
            if (REGISTRY_MODEL.getLoggedInUser().getRole().isFrameworkViewer())
                {
                // Test Actions
                imageURL = getClass().getResource(ACTION_ICON_TEST0);

                ContextAction action0 = new ContextAction("Starbase Action 0",
                                                          new ImageIcon(imageURL),
                                                          "Dynamic0",
                                                          KeyEvent.VK_D,
                                                          true,
                                                          false)
                    {
                    public void actionPerformed(final ActionEvent event)
                        {
                        System.out.println("Starbase Action 0 - do nothing");
                        }
                    };

                getUserObjectContextActionGroupByIndex(ActionGroup.DYNAMIC).addContextAction(action0);

                imageURL = getClass().getResource(ACTION_ICON_TEST1);

                ContextAction action = new ContextAction("Starbase Action 1",
                                                         new ImageIcon(imageURL),
                                                         "Dynamic1",
                                                         KeyEvent.VK_D,
                                                         true,
                                                         false)
                    {
                    public void actionPerformed(final ActionEvent event)
                        {
                        System.out.println("Starbase Action 1 - do nothing");
                        }
                    };

                getUserObjectContextActionGroupByIndex(ActionGroup.DYNAMIC).addContextAction(action);
                }

            // Present the User with an initial screen,
            // which will get overwritten as soon as they select anything...
            setUIComponent(new StarbaseBrandingUIComponent());
            getUIComponent().initialiseUI();

            return (true);
            }
        else
            {
            return (false);
            }
        }


    /***********************************************************************************************
     * Shut down the Atom.
     *
     * @return boolean
     */

    public boolean shutdownAtom()
        {
        if (super.shutdownAtom())
            {
            stopUI();

            if (getUIComponent() != null)
                {
                getUIComponent().disposeUI();
                setUIComponent(null);
                }

            return (true);
            }
        else
            {
            return (false);
            }
        }


    /**********************************************************************************************
     * Run the UI of this UserObjectPlugin when its tree node is selected.
     * setUIOccupant() uses this from the navigation tree, a menu, or a toolbar button.
     */

    public void runUI()
        {
        if (getUIComponent() != null)
            {
            getUIComponent().runUI();
            }

        setCaption(getPathname());
        setStatus(REGISTRY.getFramework().getName());
        }


    /**********************************************************************************************
     * Stop the UI of this UserObjectPlugin when its tree node is deselected.
     */

    public void stopUI()
        {
        if (getUIComponent() != null)
            {
            getUIComponent().stopUI();
            }
        }


    /***********************************************************************************************
     * Read all Resources required by Starbase.
     */

    public void readResources()
        {
        super.readResources();

        //LOGGER.log(getName() + ".readResources()");
        }
    }
