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

import org.lmn.fc.model.registry.NavigationUtilities;
import org.lmn.fc.ui.UIComponentPlugin;

import java.awt.*;


/***************************************************************************************************
 * Maintain a host container for a child UIComponent.
 */

public final class UIComponentHost extends UIComponent
    {
    private UIComponentPlugin hostedComponent;


    /***********************************************************************************************
     * Construct the UIComponentHost.
     */

    public UIComponentHost(final UIComponentPlugin uicomponent)
        {
        super(new BorderLayout());

        hostedComponent = uicomponent;
        }


    /***********************************************************************************************
     * Initialise this UIComponent.
     */

    public final void initialiseUI()
        {
        super.initialiseUI();

        // Ensure that we start with an empty panel
        removeAll();

        // Add the new Properties panel to the host UIComponentHost
        add((Component)getHostedComponent());

        // We might need to initialise the component
        getHostedComponent().initialiseUI();

        // I am not quite sure why this is needed  :-)
        NavigationUtilities.updateComponentTreeUI(this);
        }


    /***********************************************************************************************
     * Dispose of all components of this UIComponent.
     */

    public final void disposeUI()
        {
        super.disposeUI();
        removeAll();
        }


   /***********************************************************************************************
     *
     * @return UIComponentPlugin
     */

    private UIComponentPlugin getHostedComponent()
        {
        return (this.hostedComponent);
        }


    /***********************************************************************************************
     *
     * @param uicomponent
     */

    public void setHostedComponent(final UIComponentPlugin uicomponent)
        {
        this.hostedComponent = uicomponent;
        }
    }


//--------------------------------------------------------------------------------------------------
// End of File