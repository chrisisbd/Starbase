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
//  25-05-03    LMN created file
//  07-10-03    LMN moved into the FrameworkTasks!
//  15-10-03    LMN made it work!!
//  20-10-03    LMN adding ContextActions
//  21-10-03    LMN adding Framework ToolBar Group entry
//  26-10-04    LMN changing the UI to a ReportTable
//  27-10-04    LMN made JavaConsoleReport into a StreamObserver
//  02-01-05    LMN fixed bug where ContextAction was not removed on shutdown
//  04-04-05    LMN tidying logging
//  27-05-06    LMN converting for XmlBeans rewrite
//  26-01-07    LMN fixing for ui interaction sequence
//  28-01-07    LMN finally made it work!
//
//--------------------------------------------------------------------------------------------------

package org.lmn.fc.frameworks.starbase.tasks;

//--------------------------------------------------------------------------------------------------
// Imports

import org.lmn.fc.common.actions.ContextAction;
import org.lmn.fc.common.actions.ContextActionGroup;
import org.lmn.fc.common.exceptions.ReportException;
import org.lmn.fc.frameworks.starbase.ui.console.JavaConsoleReport;
import org.lmn.fc.frameworks.starbase.ui.console.JavaConsoleReportInterface;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.root.ActionGroup;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.model.tasks.impl.TaskData;
import org.lmn.fc.ui.reports.ReportTablePlugin;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.net.URL;


/***************************************************************************************************
 * The Framework JavaConsole.
 */

public final class JavaConsole extends TaskData
    {
    // String Resources
    private static final String STATUS_CONSOLE  = "The Java Console for";

    private String ACTION_RUN;                  // The text for the Framework shortcut Action

    private ContextAction runContextAction;

    // Properties
    private FontInterface pluginFont;                  // The Font of the display text
    private ColourInterface pluginColour;              // The colour of the display text
    private ColourInterface colourTable;             // The colour of the table on which the text is drawn
    private ColourInterface colourCanvas;            // The colour of the display canvas
    private int intBufferSize;                  // The number of characters to buffer


    /***********************************************************************************************
     * Construct a JavaConsole as a FrameworkTask under the specified Framework.
     * See: http://www.subrahmanyam.com/javaapps/console.
     */

    private JavaConsole()
        {
        super(6501044456859733834L, REGISTRY.getFramework());
        }


    /***********************************************************************************************
     * Initialise the JavaConsole.
     *
     * @return boolean Flag indicating success or failure
     */

    public final boolean initialiseTask()
        {
        final ContextActionGroup groupStatic;
        final URL imageURL;

        LOGGER.debugNavigation("JavaConsole.initialiseTask()");

        // Get the latest Resources
        readResources();

        // This ContextAction remains visible at all times, in the Framework
        if ((REGISTRY.getFramework().getUserObjectContextActionGroups() != null)
            && (REGISTRY.getFramework().getUserObjectContextActionGroups().size() > ActionGroup.STATIC.getIndex())
            && (REGISTRY_MODEL.getLoggedInUser().getRole().isFrameworkViewer()))
            {
            groupStatic = REGISTRY.getFramework().getUserObjectContextActionGroupByIndex(ActionGroup.STATIC);

            imageURL = getClass().getResource(ACTION_ICON_CONSOLE_RUN);

            if ((groupStatic != null)
                && (imageURL != null))
                {
                // Allow access to this JavaConsole from within the inner class
                // Must be final to achieve this!
                final TaskPlugin javaConsole = this;

                // This is a member variable in order to allow removal in dispose()
                runContextAction = new ContextAction(getName(),
                                                     new ImageIcon(imageURL),
                                                     ACTION_RUN,
                                                     KeyEvent.VK_J,
                                                     false,
                                                     true)
                    {
                    public void actionPerformed(final ActionEvent event)
                        {
                        // Run the JavaConsole
                        javaConsole.actionPerformed(event, true);
                        }
                    };

                groupStatic.addContextAction(runContextAction);
                }
            }

        return (true);
        }


    /***********************************************************************************************
     * Run the JavaConsole.
     *
     * @return boolean Flag indicating success or failure
     */

    public final boolean runTask()
        {
        LOGGER.debugNavigation("JavaConsole.runTask()");

        // Get the latest Resources
        readResources();

        // Set up the UI of a JavaConsoleReport
        try
            {
            // Remove any previous JavaConsoleReport
            if (getUIComponent() != null)
                {
                getUIComponent().disposeUI();
                }

            // Create a JavaConsoleReport
            // getResourceKey() returns <Framework>.JavaConsole.
            setUIComponent(new JavaConsoleReport(this,
                                                 REGISTRY.getFrameworkResourceKey()));
            getUIComponent().initialiseUI();

            // Limit the buffer depth to that set by the property
            ((JavaConsoleReportInterface)getUIComponent()).setBufferSize(intBufferSize);

            // There is no Editor, and we are always in Browse mode
            setEditorComponent(null);
            setBrowseMode(true);
            }

        catch (ReportException exception)
            {
            handleException(exception,
                            "runTask()",
                            EventStatus.WARNING);
            }


        LOGGER.logTimedEvent("System.out and System.err will be redirected to the JavaConsole task");

        // Save the default streams
        ((JavaConsoleReportInterface)getUIComponent()).saveStreams(System.out, System.err);

        // Activate the JavaConsole streams
        ((JavaConsoleReportInterface)getUIComponent()).setOutputDevice();
        ((JavaConsoleReportInterface)getUIComponent()).setErrorDevice();

        return (true);
        }


    /***********************************************************************************************
     * Put the JavaConsole in Idle.
     *
     * @return boolean Flag indicating success or failure
     */

    public final boolean idleTask()
        {
        // Clear the JavaConsoleReport data from the display buffer
        stopUI();

        if (getUIComponent() != null)
            {
            // Restore the streams
            ((JavaConsoleReportInterface)getUIComponent()).clearConsole();
            ((JavaConsoleReportInterface)getUIComponent()).resetOutputDevice();
            ((JavaConsoleReportInterface)getUIComponent()).resetErrorDevice();

            LOGGER.debugNavigation("JavaConsole.idleTask() START");
            LOGGER.logTimedEvent("System.out and System.err have been restored");

            // Remove the JavaConsoleReport
            // Clear the UIComponent ContextActionGroups
            getUIComponent().disposeUI();
            setUIComponent(null);
            }

        LOGGER.debugNavigation("JavaConsole.idleTask() END");

        return (true);
        }


    /***********************************************************************************************
     * Shutdown the JavaConsole.
     *
     * @return boolean Flag indicating success or failure
     */

    public final boolean shutdownTask()
        {
        stopUI();

        if (getUIComponent() != null)
            {
            if (getUIComponent() instanceof JavaConsoleReportInterface)
                {
                // Restore the streams
                ((JavaConsoleReportInterface)getUIComponent()).clearConsole();
                ((JavaConsoleReportInterface)getUIComponent()).resetOutputDevice();
                ((JavaConsoleReportInterface)getUIComponent()).resetErrorDevice();
                }

            LOGGER.logTimedEvent("System.out and System.err have been restored");

            // Remove the JavaConsoleReport
            // Clear the UIComponent ContextActionGroups
            getUIComponent().disposeUI();
            setUIComponent(null);
            }

        // Remove the persistent Run ContextAction from the Framework Static ContextActionGroup
        if ((REGISTRY.getFramework() != null)
            && (REGISTRY.getFramework().getUserObjectContextActionGroups() != null)
            && (REGISTRY.getFramework().getUserObjectContextActionGroups().size() > ActionGroup.STATIC.getIndex())
            && (REGISTRY.getFramework().getUserObjectContextActionGroups().get(ActionGroup.STATIC.getIndex()) != null))
            {
            REGISTRY.getFramework().getUserObjectContextActionGroups().get(ActionGroup.STATIC.getIndex()).removeContextAction(runContextAction);
            }

        // Clear the ContextActionGroups for the JavaConsole Task UserObject
        clearUserObjectContextActionGroups();

        LOGGER.debugNavigation("JavaConsole.shutdownTask() END");

        return (true);
        }


    /***********************************************************************************************
     * Run the UI of this UserObjectPlugin when its tree node is selected.
     */

    public final void runUI()
        {
        LOGGER.debugNavigation("JavaConsole.runUI()");

        if (getUIComponent() != null)
            {
            LOGGER.debugNavigation("JavaConsole.runUI() setting properties");
            // Configure the UI
            getUIComponent().setDebug(getDebugMode());
            ((ReportTablePlugin)getUIComponent()).setShowGrid(false);
            ((ReportTablePlugin)getUIComponent()).setTextColour(pluginColour);
            ((ReportTablePlugin)getUIComponent()).setTableColour(colourTable);
            ((ReportTablePlugin)getUIComponent()).setCanvasColour(colourCanvas);
            ((ReportTablePlugin)getUIComponent()).setReportFont(pluginFont);
            ((JavaConsoleReportInterface)getUIComponent()).setBufferSize(intBufferSize);

            getUIComponent().runUI();
            }

        setCaption(getPathname());
        setStatus(STATUS_CONSOLE + SPACE + REGISTRY.getFramework().getPathname());
        }


    /***********************************************************************************************
     * Stop the UI of this UserObjectPlugin when its tree node is deselected.
     */

    public final void stopUI()
        {
        LOGGER.debugNavigation("JavaConsole.stopUI()");

        if (getUIComponent() != null)
            {
            // Reduce resources as far as possible
            // Stop any Timers and remove the table data
            // Clear the UIComponent ContextActions
            getUIComponent().stopUI();
            }
        }


    /***********************************************************************************************
     * Read all the Resources required by the JavaConsole.
     *
     * <br>Framework Properties
     * <code>
     * <li>JavaConsole.Font
     * <li>JavaConsole.Colour.Text
     * <li>JavaConsole.Colour.Table
     * <li>JavaConsole.Colour.Canvas
     * <li>JavaConsole.Enable.Debug
     * <li>JavaConsole.BufferSize
     * </code>
     * Framework Strings
     * <code>
     * <li>JavaConsole.Action.Run
     * </code>
     */

    public final void readResources()
        {
        // getResourceKey() returns <Framework>.JavaConsole.
        LOGGER.debugNavigation("JavaConsole.readResources() [ResourceKey=" + getResourceKey() + "]");

        setDebugMode(REGISTRY.getBooleanProperty(getResourceKey() + KEY_ENABLE_DEBUG));
        pluginFont = (FontInterface)REGISTRY.getProperty(getResourceKey() + KEY_FONT);
        pluginColour = (ColourInterface)REGISTRY.getProperty(getResourceKey() + KEY_COLOUR_TEXT);
        colourTable = (ColourInterface) REGISTRY.getProperty(getResourceKey() + KEY_COLOUR_TABLE);
        colourCanvas = (ColourInterface)REGISTRY.getProperty(getResourceKey() + KEY_COLOUR_CANVAS);
        intBufferSize = REGISTRY.getIntegerProperty(getResourceKey() + KEY_BUFFER_SIZE);

        // Strings
        ACTION_RUN = REGISTRY.getString(getResourceKey() + KEY_ACTION_RUN);

        LOGGER.debugNavigation("JavaConsole.readResources() END");
        }
    }


//--------------------------------------------------------------------------------------------------
// End of File
