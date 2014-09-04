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

package org.lmn.fc.frameworks.starbase.ui.userinterface;

import org.jfree.ui.RefineryUtilities;
import org.lmn.fc.common.actions.ContextActionGroup;
import org.lmn.fc.common.exceptions.FrameworkException;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.lookandfeels.LookAndFeelPlugin;
import org.lmn.fc.model.plugins.AtomPlugin;
import org.lmn.fc.model.registry.NavigationUtilities;
import org.lmn.fc.model.registry.RegistryModelUtilities;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.ui.UIComponentPlugin;
import org.lmn.fc.ui.components.BlankUIComponent;
import org.lmn.fc.ui.manager.UserInterfaceUIComponentPlugin;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.print.PageFormat;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;


/***************************************************************************************************
 * The UserInterface JFrame.
 */

public final class UserInterfaceFrame extends JFrame
                                      implements UserInterfaceUIComponentPlugin
    {
    // String Resources
    public static final String LOGIN_LOOK_AND_FEEL = "javax.swing.plaf.metal.MetalLookAndFeel";

    private static final String DEFAULT_LOOK_AND_FEEL = "com.jgoodies.looks.plastic.Plastic3DLookAndFeel";

    private static final String MSG_CONFIRM_SHUTDOWN_0 = "Are you sure that you want to shut down the application?";
    private static final String MSG_CONFIRM_SHUTDOWN_1 = "All running tasks will be stopped, data will be discarded";
    private static final String ICON_FRAMEWORK = "frame-icon.png";

    private AtomPlugin pluginAtom;
    private TaskPlugin pluginTask;
    private String strResourceKey;

    // The context-sensitive Actions for the UIComponent
    private Vector<ContextActionGroup> vecContextActionGroups;


    // UserInterface Resources not available in TaskData
    private String strLookAndFeel;
    private int intWindowX;
    private int intWindowY;
    private int intWindowWidth;
    private int intWindowHeight;

    private PageFormat pageFormat;
    private boolean boolDebugMode;


    /***********************************************************************************************
     * Construct the JFrame which will become the UserInterface.
     *
     * @param atom
     * @param task
     * @param resourcekey
     */

    public UserInterfaceFrame(final AtomPlugin atom,
                              final TaskPlugin task,
                              final String resourcekey)
        {
        // Construct the JFrame
        super();

        // Make sure we can intercept the closing event
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        if ((atom == null)
            || (!atom.validatePlugin())
            || (task == null)
            || (!task.validatePlugin())
            || (resourcekey == null)
            || (EMPTY_STRING.equals(resourcekey)))
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_INVALID);
            }

        this.pluginAtom = atom;
        this.pluginTask = task;
        this.strResourceKey = resourcekey;

        // Initialise the Resources to some defaults
        this.strLookAndFeel = DEFAULT_LOOK_AND_FEEL;

        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.intWindowX = screenSize.width >> 2;
        this.intWindowY = screenSize.height >> 2;
        this.intWindowWidth = screenSize.width >> 1;
        this.intWindowHeight = screenSize.height >> 1;

        this.pageFormat = new PageFormat();
        this.boolDebugMode = false;
        }


    /***********************************************************************************************
     * Initialise the UserInterface UI.
     */

    public final void initialiseUI()
        {
        final UIComponentPlugin blank;

        try
            {
            final ImageIcon iconFrame;

            // Update any changes to Frame position etc.
            readResources();

            // Set an icon for the Frame of the Framework
            // This icon is always named ICON_FRAMEWORK
            iconFrame = RegistryModelUtilities.getAtomIcon(REGISTRY.getFramework(), ICON_FRAMEWORK);

            // Did we find an Icon for this Framework?
            if (iconFrame != null)
                {
                setIconImage(iconFrame.getImage());
                }

            // Set some defaults for the JFrame's ContentPane
            getContentPane().removeAll();
            getContentPane().setBackground(new Color(224, 68, 89));
            getContentPane().setLayout(new BorderLayout());
            getContentPane().setPreferredSize(new Dimension(this.intWindowWidth,
                                                            this.intWindowHeight));

            // Show the default background canvas until a FrameworkManager is loaded
            blank = new BlankUIComponent();
            blank.initialiseUI();
            getContentPane().add((Component)blank);

            // Add a listener, so we can shutdown and exit cleanly...
            // See similar code in FrameworkData.frameworkExit(), LoginTab.initialiseUI() and ShutdownDialog
            addWindowListener(new WindowAdapter()
                {
                public void windowClosing(final WindowEvent event)
                    {
                    final int intChoice;
                    final String [] strMessage =
                        {
                        MSG_CONFIRM_SHUTDOWN_0,
                        MSG_CONFIRM_SHUTDOWN_1
                        };

                    intChoice = JOptionPane.showOptionDialog(null,
                                                             strMessage,
                                                             REGISTRY.getFramework().getName() + SPACE + DIALOG_SHUTDOWN,
                                                             JOptionPane.YES_NO_OPTION,
                                                             JOptionPane.QUESTION_MESSAGE,
                                                             null,
                                                             null,
                                                             null);
                    if (intChoice == JOptionPane.YES_OPTION)
                        {
                        setVisible(false);
                        MODEL_CONTROLLER.stopFramework(REGISTRY.getFramework());
                        MODEL_CONTROLLER.exitFramework(REGISTRY.getFramework());
                        }
                    }
                });

            // Try and set the requested Look & Feel from the Resources
            // This will also set the size and position of the JFrame
            setUILookAndFeel(strLookAndFeel);
            }

        catch (ClassNotFoundException exception)
            {
            applySystemLookAndFeel(exception);
            }

        catch (InstantiationException exception)
            {
            applySystemLookAndFeel(exception);
            }

        catch (IllegalAccessException exception)
            {
            applySystemLookAndFeel(exception);
            }

        catch (UnsupportedLookAndFeelException exception)
            {
            applySystemLookAndFeel(exception);
            }
        }


    /***********************************************************************************************
     * Sets the border of this component.
     *
     * @param border the border to be rendered for this component
     *
     * @see Border
     * @see CompoundBorder
     */

    public void setBorder(final Border border)
        {

        }


    /***********************************************************************************************
     * Remove any Data associated with this UIComponent's appearance on the UI.
     * For instance, remove a Chart regardless of it being visible.
     */

    public void removeUIIdentity()
        {
        }


    /***********************************************************************************************
     * Apply the SystemLookAndFeel as a result of an Exception.
     *
     * @param lookandfeelexception
     */

    private void applySystemLookAndFeel(final Exception lookandfeelexception)
        {
        //System.out.println("!!!!!!!!!!!!!!!!! applySystemLookAndFeel " + lookandfeelexception.getMessage());
        try
            {
            // Not found, so revert to the standard offering, or at least try to do so...
            // The exceptions are:
            // ClassNotFoundException - if the LookAndFeel class could not be found
            // InstantiationException - if a new instance of the class couldn't be created
            // IllegalAccessException - if the class or initializer isn't accessible
            // UnsupportedLookAndFeelException - if lnf.isSupportedLookAndFeel() is false
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            NavigationUtilities.updateComponentTreeUI(this);

            // There may not be a FrameworkManager installed, so check first
            if ((REGISTRY_MODEL.getFrameworkManagerUI() != null))
                {
                REGISTRY_MODEL.getFrameworkManagerUI().validateAndUpdateUI();
                }

            // Save the change in the RegistryModel
            REGISTRY.setStringProperty(getResourceKey() + KEY_LOOK_AND_FEEL,
                                       UIManager.getSystemLookAndFeelClassName());

            LOGGER.logAtomEvent(getAtom(),
                                getAtom().getRootTask(),
                                getClass().getName(),
                                METADATA_UI_LOOKANDFEEL
                                    + SPACE
                                    + METADATA_RESULT
                                    + METADATA_FAIL
                                    + SPACE
                                    + METADATA_NAME
                                    + strLookAndFeel
                                    + TERMINATOR
                                    + SPACE
                                    + METADATA_EXCEPTION
                                    + lookandfeelexception.getClass()
                                    + TERMINATOR,
                                EventStatus.INFO);
            }

        catch (ClassNotFoundException exception)
            {
            logLookAndFeelException(lookandfeelexception);
            }

        catch (InstantiationException exception)
            {
            logLookAndFeelException(lookandfeelexception);
            }

        catch (IllegalAccessException exception)
            {
            logLookAndFeelException(lookandfeelexception);
            }

        catch (UnsupportedLookAndFeelException exception)
            {
            logLookAndFeelException(lookandfeelexception);
            }
        }


    /***********************************************************************************************
     * Log a LookAndFeel Exception.
     *
     * @param lookandfeelexception
     */

    private void logLookAndFeelException(final Exception lookandfeelexception)
        {
        LOGGER.logAtomEvent(getAtom(),
                            getAtom().getRootTask(),
                            getClass().getName(),
                            METADATA_UI_LOOKANDFEEL
                                + SPACE
                                + METADATA_EXCEPTION
                                + lookandfeelexception.getClass()
                                + TERMINATOR,
                            EventStatus.WARNING);
        }


    /***********************************************************************************************
     * Run the UserInterface UI.
     */

    public final void runUI()
        {
        // Update any changes to Frame position etc.
        readResources();

        // Set a default caption
        setCaption(getAtom().getPathname());

        // Create the navigation structure for the current User
        // Add whatever MenuBar has been prepared by the RegistryModel navigation builder
        // Remember that the Toolbar is set by the FrameworkManager
//        System.out.println("UserInterface Frame rebuild...");
//        TODO EXPLAIN!! REGISTRY_MODEL.rebuildNavigation(getHostTask().getContextActionGroups());

        try
            {
            setUILookAndFeel(strLookAndFeel);
            }

        catch (ClassNotFoundException exception)
            {
            applySystemLookAndFeel(exception);
            }

        catch (InstantiationException exception)
            {
            applySystemLookAndFeel(exception);
            }

        catch (IllegalAccessException exception)
            {
            applySystemLookAndFeel(exception);
            }

        catch (UnsupportedLookAndFeelException exception)
            {
            applySystemLookAndFeel(exception);
            }

        // Force all Components to change their appearance...
        // and reveal ourselves to the World...
        NavigationUtilities.updateComponentTreeUI(this);
        pack();
        setVisible(true);
        validate();

        // Set up the Frame
        setFramePosition();
        }


    /***********************************************************************************************
     * Stop the UserInterface UI.
     */

    public final void stopUI()
        {
        // Save the window setup for next time...
        REGISTRY.setIntegerProperty(getResourceKey() + KEY_DIMENSION_X, getX());
        REGISTRY.setIntegerProperty(getResourceKey() + KEY_DIMENSION_Y, getY());
        REGISTRY.setIntegerProperty(getResourceKey() + KEY_DIMENSION_WIDTH, getWidth());
        REGISTRY.setIntegerProperty(getResourceKey() + KEY_DIMENSION_HEIGHT, getHeight());

        // Save the current Look&Feel
//        REGISTRY.setStringProperty(getResourceKey() + KEY_LOOK_AND_FEEL,
//                                   UIManager.getLookAndFeel().getClass().getName());

        // Go into hiding
        setVisible(false);
        }


    /***********************************************************************************************
     * Dispose of the UserInterface Frame.
     */

    public final void disposeUI()
        {
        dispose();
        }


    /***********************************************************************************************
     * Set the opaque property.
     *
     * @param opaque
     */

    public void setOpaque(final boolean opaque)
        {
         // Does nothing
        }


    /***********************************************************************************************
     * Get the Container which holds the currently visible UIComponent.
     *
     * @return Container
     */

    public Container getContainer()
        {
        return (getContentPane());
        }


    /***********************************************************************************************
     * Set the Caption of the UserInterface.
     *
     * @param caption
     */

    public final void setCaption(final String caption)
        {
        setTitle(caption);
        }


    /**********************************************************************************************/
    /* ContextAction Implementations                                                              */
    /***********************************************************************************************
     * A ContextAction to select the specified Look & Feel if possible.
     * Specify the LookAndFeel classname.
     *
     * @param event
     * @param lookandfeel
     */

    public final void setLookAndFeel(final ActionEvent event,
                                     final String lookandfeel)
        {
        try
            {
            JTree treeNavigation;
            TreePath treePath;
            DefaultMutableTreeNode treeNode;

            treeNavigation = null;
            treePath = null;
            treeNode = null;

            // Save the window position before we do anything
            intWindowX = getX();
            intWindowY = getY();
            intWindowWidth = getWidth();
            intWindowHeight = getHeight();

            if (REGISTRY_MODEL.getFrameworkManagerUI() != null)
                {
                // Save the tree selection (if any)
                treeNavigation = REGISTRY_MODEL.getFrameworkManagerUI().getNavigationTree();

                if ((REGISTRY_MODEL.getFrameworkManagerUI().getSelectedTreeNode() != null)
                    && (treeNavigation != null))
                    {
                    treePath = treeNavigation.getSelectionPath();
                    treeNode = REGISTRY_MODEL.getFrameworkManagerUI().getSelectedTreeNode();
                    }
                }

            // Try to change the LookAndFeel, reposition the window
            setUILookAndFeel(lookandfeel);

            // Force all Components to change their appearance...
            NavigationUtilities.updateComponentTreeUI(this);

            if (REGISTRY_MODEL.getMenuBar() != null)
                {
                REGISTRY_MODEL.getMenuBar().revalidate();
                }

            // There may not be a FrameworkManager installed, so check first
            if ((REGISTRY_MODEL.getFrameworkManagerUI() != null))
                {
                REGISTRY_MODEL.getFrameworkManagerUI().validateAndUpdateUI();
                }

            // Restore the selection on the navigation tree (if any)
            // See similar code in RootData.actionPerformed()
            if ((treeNavigation != null)
                && (treePath != null)
                && (treeNode != null)
                && (REGISTRY_MODEL.getFrameworkManagerUI() != null))
                {
                treeNavigation.setSelectionPath(treePath);
                REGISTRY_MODEL.getFrameworkManagerUI().setSelectedTreeNode(treeNode);
                }

            // Reposition the Frame to ensure update? Todo review this
            //setFramePosition();
            }

        catch (ClassNotFoundException exception)
            {
            applySystemLookAndFeel(exception);
            }

        catch (InstantiationException exception)
            {
            applySystemLookAndFeel(exception);
            }

        catch (IllegalAccessException exception)
            {
            applySystemLookAndFeel(exception);
            }

        catch (UnsupportedLookAndFeelException exception)
            {
            applySystemLookAndFeel(exception);
            }
        }


    /***********************************************************************************************
     * Set the position of the UserInterface Frame.
     */

    private void setFramePosition()
        {
        // Position the JFrame on the screen
        setSize(new Dimension(intWindowWidth, intWindowHeight));

        // Causes this Window to be sized to fit the preferred size and layouts
        // of its subcomponents. If the window and/or its owner are not yet displayable,
        // both are made displayable before calculating the preferred size.
        // The Window will be validated after the preferredSize is calculated.
        pack();

        //setLocation(new Point(intWindowX, intWindowY));
        RefineryUtilities.centerFrameOnScreen(this);

        // Save the window setup in the Registry
        REGISTRY.setIntegerProperty(getResourceKey() + KEY_DIMENSION_X, getX());
        REGISTRY.setIntegerProperty(getResourceKey() + KEY_DIMENSION_Y, getY());
        REGISTRY.setIntegerProperty(getResourceKey() + KEY_DIMENSION_WIDTH, getWidth());
        REGISTRY.setIntegerProperty(getResourceKey() + KEY_DIMENSION_HEIGHT, getHeight());
        }


    /***********************************************************************************************
     * Try to set the specified Look&Feel from the specified classname.
     * Update all the visual components that we can know about.
     *
     * @param lookandfeel
     *
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws UnsupportedLookAndFeelException
     */

    private void setUILookAndFeel(final String lookandfeel) throws ClassNotFoundException,
                                                                   InstantiationException,
                                                                   IllegalAccessException,
                                                                   UnsupportedLookAndFeelException
        {
        final Collection<LookAndFeelPlugin> plugins;
        boolean boolFound;
        String strKey;

        // Update any changes to Frame position etc.
        readResources();

        // Map the L&F classname to its ResourceKey
        // This could be done with XPath...
        plugins = REGISTRY.getLookAndFeels().values();
        boolFound = false;
        strKey = EMPTY_STRING;

        for (Iterator iterPlugins = plugins.iterator();
             (iterPlugins.hasNext() && !boolFound);)
            {
            final LookAndFeelPlugin plugin;

            plugin = (LookAndFeelPlugin) iterPlugins.next();

            if (plugin.getClassName().equals(lookandfeel))
                {
                strKey = PREFIX_LOOKANDFEEL + KEY_DELIMITER + plugin.getName();
                boolFound = true;
                }
            }

        // See if the Registry contains a L&F with the appropriate key
        if (REGISTRY.getLookAndFeels().containsKey(strKey))
            {
            // Try to set the new Look&Feel
            UIManager.setLookAndFeel(lookandfeel);
            NavigationUtilities.updateComponentTreeUI(this);

            // There may not be a FrameworkManager installed, so check first
            if ((REGISTRY_MODEL.getFrameworkManagerUI() != null))
                {
                REGISTRY_MODEL.getFrameworkManagerUI().validateAndUpdateUI();
                }
            }
        else
            {
            LOGGER.debug("Requested LookAndFeel not found in the Registry");
            throw new UnsupportedLookAndFeelException("Requested LookAndFeel not found in the Registry");
            }

        strLookAndFeel = lookandfeel;

        // If we didn't throw an Exception, save the successful change in the RegistryModel
        // Save the L&F classname for now,
        // because that is what's returned by UIManager.getInstalledLookAndFeels()
        REGISTRY.setStringProperty(getResourceKey() + KEY_LOOK_AND_FEEL, strLookAndFeel);
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


    /*********************************************************************************************
     * Set the X alignment.
     *
     * @param alignment
     */

    public void setAlignmentX(final float alignment)
        {
        // Not a valid concept for the UI Frame
        }


    /*********************************************************************************************
     * Set the Y alignment.
     *
     * @param alignment
     */

    public void setAlignmentY(final float alignment)
        {
        // Not a valid concept for the UI Frame
        }


    /**********************************************************************************************/
    /* Utilities                                                                                  */
    /***********************************************************************************************
     * Find the specified Resource.
     *
     * @param path
     * @param resourceName
     *
     * @return URL
     */

    public final URL findResource(final String path,
                                  final String resourceName)
        {
        final URL url = getClass().getResource(path + resourceName);

        return (url);
        }


    /***********************************************************************************************
     * Get the host Atom.
     *
     * @return AtomPlugin
     */

    private AtomPlugin getAtom()
        {
        return (this.pluginAtom);
        }


    /***********************************************************************************************
     * Get the host Task.
     *
     * @return TaskPlugin
     */

    private TaskPlugin getHostTask()
        {
        return (this.pluginTask);
        }


    /***********************************************************************************************
     * Get the ResourceKey.
     *
     * @return String
     */

    private String getResourceKey()
        {
        return (this.strResourceKey);
        }


    /***********************************************************************************************
     * Read all the Resources required by the UserInterface.
     */

    private void readResources()
        {
        // getResourceKey() returns '<Framework>.UserInterface.'
        setDebug(REGISTRY.getBooleanProperty(getResourceKey() + KEY_ENABLE_DEBUG));

        strLookAndFeel = REGISTRY.getStringProperty(getResourceKey() + KEY_LOOK_AND_FEEL);
        intWindowX = REGISTRY.getIntegerProperty(getResourceKey() + KEY_DIMENSION_X);
        intWindowY = REGISTRY.getIntegerProperty(getResourceKey() + KEY_DIMENSION_Y);
        intWindowWidth = REGISTRY.getIntegerProperty(getResourceKey() + KEY_DIMENSION_WIDTH);
        intWindowHeight = REGISTRY.getIntegerProperty(getResourceKey() + KEY_DIMENSION_HEIGHT);
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
