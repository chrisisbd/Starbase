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
import org.lmn.fc.ui.UIComponentState;

import javax.swing.*;
import java.awt.*;
import java.awt.print.PageFormat;
import java.net.URL;
import java.util.Vector;


/***************************************************************************************************
 * UIComponent.
 */

public class UIComponent extends JPanel
                         implements UIComponentPlugin
    {

    // The internal state of this UIComponent
    private UIComponentState uiState;

    // The context-sensitive Actions for the UIComponent
    private Vector<ContextActionGroup> vecContextActionGroups;

    private PageFormat pageFormat;
    private boolean boolDebugMode;


    /***********************************************************************************************
     * Construct a UIComponent.
     */

    public UIComponent()
        {
        super(new BorderLayout());

        this.uiState = UIComponentState.CREATED;
        this.vecContextActionGroups = new Vector<ContextActionGroup>(1);
        this.pageFormat = UIComponentHelper.getDefaultPageFormat();
        this.boolDebugMode = false;
        }


    /***********************************************************************************************
     * Construct a UIComponent.
     *
     * @param layout
     * @param isDoubleBuffered
     */

    public UIComponent(final LayoutManager layout,
                       final boolean isDoubleBuffered)
        {
        super(layout, isDoubleBuffered);

        this.uiState = UIComponentState.CREATED;
        this.vecContextActionGroups = new Vector<ContextActionGroup>(1);
        this.pageFormat = UIComponentHelper.getDefaultPageFormat();
        this.boolDebugMode = false;
        }


    /***********************************************************************************************
     * Construct a UIComponent.
     *
     * @param layout
     */

    public UIComponent(final LayoutManager layout)
        {
        super(layout);

        this.uiState = UIComponentState.CREATED;
        this.vecContextActionGroups = new Vector<ContextActionGroup>(1);
        this.pageFormat = UIComponentHelper.getDefaultPageFormat();
        this.boolDebugMode = false;
        }


    /***********************************************************************************************
     * Construct a UIComponent.
     *
     * @param isDoubleBuffered
     */

    public UIComponent(final boolean isDoubleBuffered)
        {
        super(isDoubleBuffered);

        this.uiState = UIComponentState.CREATED;
        this.vecContextActionGroups = new Vector<ContextActionGroup>(1);
        this.pageFormat = UIComponentHelper.getDefaultPageFormat();
        this.boolDebugMode = false;
        }


    /***********************************************************************************************
     /* UI State                                                                                  */
    /***********************************************************************************************
     * Initialise this UIComponent.
     */

    public void initialiseUI()
        {
        // Override this as required...
        setUIState(UIComponentState.INITIALISED);
        }


    /***********************************************************************************************
     * Run this UIComponent.
     */

    public void runUI()
        {
        // Override this as required...
        setUIState(UIComponentState.RUNNING);
        }


    /***********************************************************************************************
     * Stop this UIComponent.
     */

    public void stopUI()
        {
        // Override this as required...
        setUIState(UIComponentState.STOPPED);
        }


    /***********************************************************************************************
     * Dispose of all components of this UIComponent.
     */

    public void disposeUI()
        {
        removeAll();

        // Override this as required...
        setUIState(UIComponentState.DISPOSED);
        }


    /***********************************************************************************************
     * Remove any Data associated with this UIComponent's appearance on the UI.
     * For instance, remove a Chart regardless of it being visible.
     */

    public void removeUIIdentity()
        {
        }


    /***********************************************************************************************
     * Get the UIComponent internal state.
     *
     * @return UIComponentState
     */

    public UIComponentState getUIState()
        {
        return (this.uiState);
        }


    /***********************************************************************************************
     * Set the UIComponent internal state.
     *
     * @param state
     */

    public void setUIState(final UIComponentState state)
        {
        this.uiState = state;
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
            LOGGER.debugNavigation("Add group to UIComponent [group=" + group.getName() + "] [uicomponent=" + this.getClass().getName() + "]");
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
            //LOGGER.debug("removeContextActionGroup() " + group.getName());
            getUIComponentContextActionGroups().remove(group);
            }
        }


    /***********************************************************************************************
     * Clear the complete list of ContextActionGroups for this UIComponent.
     */

    public final void clearUIComponentContextActionGroups()
        {
        LOGGER.debugNavigation("clearContextActionGroups() for " + this.getClass().getName());
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
     * Find an resource by name (classpath or filesystem).
     *
     * @param resourceName
     *
     * @return URL
     */

    public final URL findResource(final String path,
                                  final String resourceName)
        {
        //System.out.println("findResource --> path=" + path);
        //System.out.println("findResource --> resourceName=" + resourceName);
        final URL url = getClass().getResource(path + resourceName);
//        if (url != null)
//            {
//            System.out.println("findResource --> Derived resource path as " + url.toString());
//            }

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
