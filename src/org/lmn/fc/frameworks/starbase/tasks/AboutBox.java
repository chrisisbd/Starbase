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

//--------------------------------------------------------------------------------------------------
// Revision History
//
//  18-01-02    LMN started again...
//  06-02-02    LMN renamed, and passed the RegistryModel
//  25-04-02    LMN added ImageIcon for the background picture!
//  06-05-03    LMN removed RegistryModel & ObservatoryModel, added TaskData
//  16-10-03    LMN moved to a FrameworkTask
//  21-10-03    LMN added Help Action
//  26-05-04    LMN added aboutXXX() methods
//  22-06-04    LMN added ResourceReports
//  04-10-04    LMN changing for addTabListener()
//  05-10-04    LMN added HTMLViewers for Framework and Applications
//  15-10-04    LMN added JavaDoc Tab
//  18-10-04    LMN tidying up...
//  28-10-04    LMN changed to use new NodeReport
//  02-01-05    LMN fixed bug where ContextAction was not removed on shutdown
//  07-06-06    LMN changed to use an AboutUI UIComponent
//  15-11-06    LMN tidying again!
//
//--------------------------------------------------------------------------------------------------

package org.lmn.fc.frameworks.starbase.tasks;

//--------------------------------------------------------------------------------------------------
// Imports

import org.lmn.fc.common.actions.ContextAction;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.model.tasks.impl.TaskData;
import org.lmn.fc.ui.panels.AboutUI;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.net.URL;


/***************************************************************************************************
 * The AboutBox.
 *
 * ToDo Tab colours are wrong
 */

public final class AboutBox extends TaskData
    {
    // String Resources
    private static final String TOOLTIP_ABOUT_BOX   = "Display the AboutBox";
    private static final String STATUS_ABOUT_BOX    = "Showing the AboutBox for";

    private FontInterface pluginFont;
    private ColourInterface pluginColour;
    private ContextAction helpContextAction;


    /***********************************************************************************************
     * Construct the AboutBox.
     */

    private AboutBox()
        {
        super(-892539104995708020L, REGISTRY.getFramework());
        }


    /***********************************************************************************************
     * Initialise the AboutBox.
     *
     * @return boolean flag indicating success or failure
     */

    public final boolean initialiseTask()
        {
        final URL imageURL;

        readResources();
        clearUserObjectContextActionGroups();

        imageURL = getClass().getResource(ACTION_ICON_ABOUT);

        if (imageURL != null)
            {
            // Allow access to the AboutBox from within the inner class
            // Must be final to achieve this!
            final TaskPlugin aboutBox = this;

            helpContextAction = new ContextAction(getName(),
                                                  new ImageIcon(imageURL),
                                                  TOOLTIP_ABOUT_BOX,
                                                  KeyEvent.VK_A,
                                                  true,
                                                  false)
                {
                public void actionPerformed(final ActionEvent event)
                    {
                    aboutBox.actionPerformed(event, true);
                    }
                };


            // Install the AboutBox Help Action into the FrameworkHelp
            REGISTRY_MODEL.addHelpAction(helpContextAction);
            }

        return (true);
        }


    /***********************************************************************************************
     * Run the AboutBox Task.
     *
     * @return boolean flag indicating success or failure
     */

    public final boolean runTask()
        {
        // Read the Resources for the AboutBox
        readResources();

        // Create the panel of Tabs holding the various HTMLPanels and Reports
        setUIComponent(new AboutUI(this, pluginFont, pluginColour));
        getUIComponent().initialiseUI();

        // There is no Editor, and we are always in Browse mode
        setEditorComponent(null);
        setBrowseMode(true);

        return (true);
        }


    /***********************************************************************************************
     * Park the AboutBox Task in Idle.
     *
     * @return boolean flag indicating success or failure
     */

    public final boolean idleTask()
        {
        stopUI();

        return (true);
        }


    /***********************************************************************************************
     * Shutdown the AboutBox after use.
     *
     * @return boolean flag indicating success or failure
     */

    public final boolean shutdownTask()
        {
        stopUI();

        // Clear the ContextActionGroups for the AboutBox Task
        clearUserObjectContextActionGroups();

        // Clear the AboutUI
        if (getUIComponent() != null)
            {
            getUIComponent().disposeUI();
            setUIComponent(null);
            }

        return (true);
        }


    /***********************************************************************************************
     * Run the UI of this UserObjectPlugin.
     */

    public final void runUI()
        {
        // The first Tab should be the Framework AboutBox, which calls its runUI()
        // when created. Other Tabs call runUI() via the Tab Listener.
        if (getUIComponent() != null)
            {
            getUIComponent().runUI();
            }

        setCaption(getPathname());
        setStatus(STATUS_ABOUT_BOX + SPACE + REGISTRY.getFramework().getName());
        }


    /***********************************************************************************************
     * Stop the UI of this UserObjectPlugin.
     */

    public final void stopUI()
        {
        if (getUIComponent() != null)
            {
            getUIComponent().stopUI();
            }
        }


    /***********************************************************************************************
     * Read all the Resources required by the AboutBox.
     *<p>
     * Resources read:
     *
     * <li>Font.Label
     * <li>Colour.Text
     * <li>Enable.Debug
     */

    public final void readResources()
        {
        // Use the parent Framework's Enable.Debug
        setDebugMode(REGISTRY.getFramework().getDebugMode());

        pluginFont = (FontInterface)REGISTRY.getProperty(REGISTRY.getFramework().getResourceKey() + KEY_FONT_LABEL);
        pluginColour = (ColourInterface)REGISTRY.getProperty(REGISTRY.getFramework().getResourceKey() + KEY_COLOUR_TEXT);
        }
    }


//--------------------------------------------------------------------------------------------------
// End of File