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

package org.lmn.fc.ui.login;

import org.lmn.fc.model.registry.InstallationFolder;
import org.lmn.fc.model.registry.RegistryModelUtilities;
import org.lmn.fc.ui.UIComponentPlugin;
import org.lmn.fc.ui.components.UIComponent;
import org.lmn.fc.ui.panels.HTMLPanel;

import java.awt.*;

/***************************************************************************************************
 * The LicenceTab for the Login panel.
 */

final class LicenceTab extends UIComponent
                       implements UIComponentPlugin
    {
    private HTMLPanel htmlPanel;


    /***********************************************************************************************
     * Construct a LicenceTab.
     */

    LicenceTab()
        {
        super(new BorderLayout());

        // REGISTRY.getFramework().getResourceKey() + KEY_RESOURCE_LOGIN
        }


    /***********************************************************************************************
     * Initialise this UIComponent.
     */

    public void initialiseUI()
        {
        // Remove any previous LicenceTab
        disposeUI();

        htmlPanel = new HTMLPanel(RegistryModelUtilities.getDistributionURL(REGISTRY.getFramework(),
                                                                            InstallationFolder.LICENCES,
                                                                            REGISTRY.getFramework().getLicenceFilename()));
        htmlPanel.setDebug(isDebug());
        htmlPanel.initialiseUI();
        add(htmlPanel);

        super.initialiseUI();
        }


    /***********************************************************************************************
     * Dispose of all components of this UIComponent.
     */

    public void disposeUI()
        {
        if (htmlPanel != null)
            {
            htmlPanel.disposeUI();
            htmlPanel = null;
            }

        super.disposeUI();
        }


    /***********************************************************************************************
     * Run this UIComponent.
     */

    public void runUI()
        {
        if (htmlPanel != null)
            {
            htmlPanel.runUI();
            }

        super.runUI();
        }


    /***********************************************************************************************
     * Stop this UIComponent.
     */

    public void stopUI()
        {
        if (htmlPanel != null)
            {
            htmlPanel.stopUI();
            }

        super.stopUI();
        }
    }
