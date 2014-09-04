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

package org.lmn.fc.ui.components;

import org.lmn.fc.common.actions.ContextActionGroup;
import org.lmn.fc.ui.UIComponentPlugin;

import javax.swing.*;
import java.awt.*;
import java.awt.print.PageFormat;
import java.net.URL;
import java.util.Vector;


public class ScrollUI extends JScrollPane
                      implements UIComponentPlugin
    {
    private UIComponentPlugin pluginComponent;

    // The context-sensitive Actions for the UIComponent
    private Vector<ContextActionGroup> vecContextActionGroups;

    private PageFormat pageFormat;
    private boolean boolDebugMode;


    /***********************************************************************************************
     * ScrollUI.
     *
     * @param plugin
     */

    public ScrollUI(final UIComponentPlugin plugin)
        {
        super((Component)plugin);

        this.pluginComponent = plugin;
        this.vecContextActionGroups = new Vector<ContextActionGroup>(1);
        this.pageFormat = UIComponentHelper.getDefaultPageFormat();
        this.boolDebugMode = false;
        }

    // Related to Atom and Task state
    public void initialiseUI()
        {
        pluginComponent.initialiseUI();
        }

    public void runUI()
        {
        pluginComponent.runUI();
        }

    public void stopUI()
        {
        pluginComponent.stopUI();
        }

    public void disposeUI()
        {
        pluginComponent.disposeUI();
        }


    /***********************************************************************************************
     * Remove any Data associated with this UIComponent's appearance on the UI.
     * For instance, remove a Chart regardless of it being visible.
     */

    public void removeUIIdentity()
        {
        }


    /***********************************************************************************************
    /* Context Actions                                                                            */
    /***********************************************************************************************
     * Get the vector of ContextActionGroups for this UIComponent.
     *
     * @return Vector
     */

    public final Vector<ContextActionGroup> getUIComponentContextActionGroups()
        {
        return (this.vecContextActionGroups);
        }


    /***********************************************************************************************
     * Set the vector of ContextActionGroups for this UIComponent.
     *
     * @param actiongroups
     */

    public final void setUIComponentContextActionGroups(final Vector<ContextActionGroup> actiongroups)
        {
        this.vecContextActionGroups = actiongroups;
        }


    /***********************************************************************************************
     * Add an ActionGroup to the list of ContextActionGroups for this UIComponent.
     *
     * @param group
     */

    public final void addUIComponentContextActionGroup(final ContextActionGroup group)
        {
        if ((group != null)
            && (getUIComponentContextActionGroups() != null)
            && (!getUIComponentContextActionGroups().contains(group)))
            {
            //LOGGER.debug("Add group to UIComponent [group=" + group.getName() + "] [uicomponent=" + getName() + "]");
            getUIComponentContextActionGroups().add(group);
            }
        }


    /***********************************************************************************************
     * Remove an ActionGroup from the list of ContextActionGroups for this UIComponent.
     *
     * @param group
     */

    public final void removeUIComponentContextActionGroup(final ContextActionGroup group)
        {
        if ((group != null)
            && (getUIComponentContextActionGroups() != null)
            && (getUIComponentContextActionGroups().contains(group)))
            {
            //LOGGER.debug("removeContextActionGroup() " + group.getName() + " in " + getName());
            getUIComponentContextActionGroups().remove(group);
            }
        }


    /***********************************************************************************************
     * Clear the complete list of ContextActionGroups for this UIComponent.
     */

    public final void clearUIComponentContextActionGroups()
        {
        //LOGGER.debug("clearContextActionGroups() in " + getName());
        this.vecContextActionGroups = new Vector<ContextActionGroup>(1);
        }


    /***********************************************************************************************
     * Clear the specified ContextActionGroup for this UIComponent.
     *
     * @param group
     */
    public final void clearUIComponentContextActionGroup(final ContextActionGroup group)
        {
        if ((group != null)
            && (getUIComponentContextActionGroups() != null)
            && (getUIComponentContextActionGroups().contains(group)))
            {
            //LOGGER.debug("clearContextActionGroup() " + group.getName() + " in " + getName());
            group.clearContextActions();
            }
        }


    /************************************************************************************************
     * Get the PageFormat for printing.
     *
     * @return PageFormat
     */

    public PageFormat getPageFormat()
        {
        return (this.pageFormat);
        }


    /*********************************************************************************************
     * Set the PageFormat for printing.
     *
     * @param pageformat
     */

    public void setPageFormat(final PageFormat pageformat)
        {
        this.pageFormat = pageformat;
        }


    /***********************************************************************************************
    /* Miscellaneous                                                                              */
    /***********************************************************************************************
     * Find a resource by name (classpath or filesystem).
     *
     * @param resourceName
     *
     * @return URL
     */

    public URL findResource(final String path,
                            final String resourceName)
        {
        final URL url = getClass().getResource(path + resourceName);

        return (url);
        }


    /***********************************************************************************************
     * Indicate if we are in debug mode.
     *
     * @return boolean
     */

    public boolean isDebug()
        {
        return (this.boolDebugMode);
        }


    /************************************************************************************************
     * Indicate if we are in debug mode.
     *
     * @param debug
     */

    public void setDebug(final boolean debug)
        {
        this.boolDebugMode = debug;
        }
    }
