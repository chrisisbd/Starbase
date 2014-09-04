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

package org.lmn.fc.frameworks.starbase.plugins.repository;

import org.lmn.fc.common.actions.ContextAction;
import org.lmn.fc.model.plugins.AtomPlugin;
import org.lmn.fc.model.plugins.impl.PluginData;
import org.lmn.fc.model.root.ActionGroup;
import org.lmn.fc.ui.components.BlankUIComponent;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/***************************************************************************************************
 * Repository.
 */

public final class Repository extends PluginData
                              implements RepositoryMBean
    {
    /***********************************************************************************************
     * Construct Repository.
     */

    private Repository()
        {
        super(7435218836077004085L);
        }


    /***********************************************************************************************
     * Start up the Atom.
     *
     * @return boolean
     */

    public boolean startupAtom()
        {
        final AtomPlugin atom = this;

        // Initialise the Atom
        if (super.startupAtom())
            {
            // Do not mark this as 'final'!
            boolean boolSuccess;
            final ContextAction action0 = new ContextAction("Repository Action",
                                                            null,
                                                            "Repository Action",
                                                            KeyEvent.VK_D,
                                                            true,
                                                            false)
                {
                public void actionPerformed(final ActionEvent event)
                    {
                    System.out.println("Repository do nothing");
                    }
                };

            getUserObjectContextActionGroupByIndex(ActionGroup.PLUGIN).addContextAction(action0);

            setUIComponent(new BlankUIComponent());
            getUIComponent().initialiseUI();

            return (true);
            }
        else
            {
            LOGGER.error(ATOM_STARTUP);
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
        setStatus(getName());
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
     * Read all Resources required by Repository.
     */

    public void readResources()
        {
        super.readResources();

        LOGGER.log(getName() + ".readResources()");
        }
    }
