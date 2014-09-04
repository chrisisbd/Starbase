package org.lmn.fc.model.registry;

import org.lmn.fc.common.actions.ContextAction;
import org.lmn.fc.common.actions.ContextActionGroup;
import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.common.constants.ResourceKeys;
import org.lmn.fc.common.exceptions.FrameworkException;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.logging.Logger;
import org.lmn.fc.model.plugins.AtomPlugin;
import org.lmn.fc.model.plugins.FrameworkPlugin;
import org.lmn.fc.model.plugins.impl.NullData;
import org.lmn.fc.model.registry.impl.RegistryModel;
import org.lmn.fc.model.root.ActionGroup;
import org.lmn.fc.model.root.RootPlugin;
import org.lmn.fc.model.root.UserObjectPlugin;
import org.lmn.fc.model.tasks.FrameworkManagerPlugin;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.model.tasks.TaskState;
import org.lmn.fc.model.tasks.UserInterfacePlugin;
import org.lmn.fc.model.users.UserPlugin;
import org.lmn.fc.ui.UIComponentPlugin;
import org.lmn.fc.ui.manager.FrameworkManagerUIComponentPlugin;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.Iterator;
import java.util.Vector;


/***************************************************************************************************
 * Utilities to build the Menus and ToolBar.
 */

public final class NavigationUtilities implements FrameworkConstants,
                                                  FrameworkStrings,
                                                  FrameworkMetadata,
                                                  ResourceKeys
    {
    private static final Logger LOGGER = Logger.getInstance();
    private static final RegistryModelPlugin REGISTRY_MODEL = RegistryModel.getInstance();

    private static final Dimension DIM_TOOLBAR_SEPARATOR = new Dimension(10, 5);
    private static final Border BORDER_BUTTON = BorderFactory.createEmptyBorder(3, 3, 3, 3);
    private static final Color COLOUR_CLASSNOTFOUND = new Color(200, 10, 10);

    // String Resources
    private static final String MENU_HELP = "Help";
    private static final String MENU_TOOLTIP_EXPAND_TO = "Expand to";
    private static final String MSG_CONFIRM_STOP = "Are you sure that you wish to stop";
    private static final String MSG_CONFIRM_START = "Are you sure that you wish to start";
    private static final String DIALOG_PLUGIN_CONTROL = "Control";
    private static final String DIALOG_CONFIGURATION = "Framework Configuration";
    private static final String TOOLTIP_VERSION_CONFIGURATION = "The Version Configuration for this Plugin";


    /***********************************************************************************************
     * Build the Framework Actions for the Menu and Toolbar.
     *
     * @param user
     * @param framework
     * @param manager
     * @param menubar
     * @param toolbar
     */

    public static void buildFrameworkActions(final UserPlugin user,
                                             final FrameworkPlugin framework,
                                             final FrameworkManagerUIComponentPlugin manager,
                                             final JMenuBar menubar,
                                             final JToolBar toolbar)
        {
        // We may or may not have a FrameworkManager UI at this point...
        // and so we cannot check the parameter
        if ((user != null)
            && (user.validatePlugin())
            && (framework != null)
            && (framework.validatePlugin())
            && (menubar != null)
            && (toolbar != null))
            {
            final JMenu menuFrameworkStatic;

            // Add a Menu for the Framework STATIC items to the Menu and ToolBar
            // We assume the User *can* see these, because they must see the Framework
            // and Actions are an attribute of the Atom, not a Task
            menuFrameworkStatic = addActionGroupToMenuAndToolbar(framework.getUserObjectContextActionGroupByIndex(ActionGroup.STATIC),
                                                                 new JMenu(framework.getRecursionLevels().get(0)),
                                                                 toolbar);

            // Finally add the BuildNumber for this Plugin, if one exists
            addVersionConfiguration(framework, menuFrameworkStatic);

            // Add the finished menu to the menuFrameworkStatic bar
            if (menuFrameworkStatic.getItemCount() > 0)
                {
                menubar.add(menuFrameworkStatic);
                }

            //--------------------------------------------------------------------------------------
            // Do we allow the User to see the Framework instance?

            if (REGISTRY_MODEL.getLoggedInUser().getRole().isFrameworkViewer())
                {
                // Add a Menu for the Framework instance, the Framework Tasks
                // and the DYNAMIC Actions to the Menu and ToolBar
                final JMenu menuFrameworkInstance;

                // Create a Menu for this Group
                menuFrameworkInstance = new JMenu(framework.getName());

                // Firstly add the Framework instance Plugin itself as the top item
                addPluginToMenu(user,
                                manager,
                                framework,
                                menuFrameworkInstance);

                if (menuFrameworkInstance.getItemCount() > 0)
                    {
                    menuFrameworkInstance.addSeparator();
                    }

                // Now handle the Framework Tasks
                // These only appear on the menus!
                // It is possible that the User is not allowed to see certain Tasks,
                // and so each one must be checked
                addTasksToMenu(user,
                               manager,
                               framework.getTasks(),
                               menuFrameworkInstance);

                // Now add the Framework DYNAMIC ContextActions after the Tasks
                // We assume the User *can* see these, because they must see the Framework
                // and Actions are an attribute of the Atom, not a Task
                if ((framework.getUserObjectContextActionGroupByIndex(ActionGroup.DYNAMIC) != null)
                    && (!framework.getUserObjectContextActionGroupByIndex(ActionGroup.DYNAMIC).isEmpty()))
                    {
                    if (menuFrameworkInstance.getItemCount() > 0)
                        {
                        menuFrameworkInstance.addSeparator();
                        }

                    addActionGroupToMenuAndToolbar(framework.getUserObjectContextActionGroupByIndex(ActionGroup.DYNAMIC),
                                                   menuFrameworkInstance,
                                                   toolbar);
                    }

                // Finally add the BuildNumber for this Plugin, if one exists
                addVersionConfiguration(framework, menuFrameworkInstance);

                // Add the finished menu to the menu bar, if there is anything to see
                if (menuFrameworkInstance.getItemCount() > 0)
                    {
                    menubar.add(menuFrameworkInstance);
                    }
                }
            }
        }


    /***********************************************************************************************
     * Build the Plugin Menus and Toolbar.
     * Assemble the actions for all Plugins which are children of the specified AtomPlugin.
     * Check that the User has permission to see this Plugin.
     *
     * Show:
     *     Child Plugins
     *     Tasks
     *     Context Actions
     *
     * @param user
     * @param host
     * @param manager
     * @param menubar
     * @param toolbar
     */

    public static void buildAllPluginActions(final UserPlugin user,
                                             final AtomPlugin host,
                                             final FrameworkManagerUIComponentPlugin manager,
                                             final JMenuBar menubar,
                                             final JToolBar toolbar)
        {
        // We may or may not have a FrameworkManager UI at this point...
        if ((user != null)
            && (user.validatePlugin())
            && (host != null)
            && (host.validatePlugin())
            && (host.getAtoms() != null)
            && (menubar != null)
            && (toolbar != null))
            {
            final Iterator<RootPlugin> iterPlugins;

            iterPlugins = host.getAtoms().iterator();

            while (iterPlugins.hasNext())
                {
                final AtomPlugin plugin;
                final JMenu menuPlugin;

                plugin = (AtomPlugin) iterPlugins.next();

                // Check that the User has permission to see this Plugin
                // If not, go no further!
                if ((plugin.validatePlugin())
                    && (plugin.getUserRoles().contains(user.getRole())))
                    {
                    // For each Plugin, make a top-level menu
                    menuPlugin = new JMenu(plugin.getName());

                    // Recursively add plugins to the menu and toolbar
                    buildMenuAndToolbar(user, plugin, manager, menuPlugin, toolbar);

                    // Add the finished menu to the menu bar, if there is anything to see
                    if (menuPlugin.getItemCount() > 0)
                        {
                        menubar.add(menuPlugin);
                        }
                    }
                }
            }
        }


    /***********************************************************************************************
     * Build the Actions for the child plugins of the specified plugin.
     * Check that the User has permission to see each child.
     *
     * @param user
     * @param host
     * @param manager
     * @param hostmenu
     * @param toolbar
     */

    private static void buildMenuAndToolbar(final UserPlugin user,
                                            final AtomPlugin host,
                                            final FrameworkManagerUIComponentPlugin manager,
                                            final JMenu hostmenu,
                                            final JToolBar toolbar)
        {
        // It is possible to have a null FrameworkManager,
        // so we cannot check the parameter
        if ((user != null)
            && (user.validatePlugin())
            && (host != null)
            && (host.validatePlugin())
            && (host.getAtoms() != null)
            && (hostmenu != null)
            && (toolbar != null))
            {
            final Iterator<RootPlugin> iterPlugins;

            iterPlugins = host.getAtoms().iterator();

            while (iterPlugins.hasNext())
                {
                final AtomPlugin plugin;
                final JMenu menuPlugin;

                plugin = (AtomPlugin) iterPlugins.next();

                // Check that the User has permission to see this child Plugin
                // If not, do not recurse!
                if ((plugin.validatePlugin())
                    && (plugin.getUserRoles().contains(user.getRole())))
                    {
                    final URL imageURL;

                    // For each Plugin, make a menu attached to the host menu
                    // This just points to the plugin's real menu, i.e. it does not execute anything
                    menuPlugin = new JMenu(HTML_PREFIX_ITALIC
                                           + plugin.getName()
                                           + HTML_SUFFIX_ITALIC);
                    menuPlugin.setToolTipText(MENU_TOOLTIP_EXPAND_TO + SPACE + plugin.getName());
                    imageURL = plugin.getClass().getResource(ACTION_ICON_SUBMENU);

                    if (imageURL != null)
                        {
                        menuPlugin.setIcon(new ImageIcon(imageURL));
                        }

                    hostmenu.add(menuPlugin);

                    // Recursively build menus for child plugins
                    buildMenuAndToolbar(user, plugin, manager, menuPlugin, toolbar);
                    }
                }

            // Add the host Plugin itself as the top item
            addPluginToMenu(user,
                            manager,
                            host,
                            hostmenu);

            if (hostmenu.getItemCount() > 0)
                {
                hostmenu.addSeparator();
                }

            // Add the host Plugin Tasks
            // Check that the User has permission to see each Task
            addTasksToMenu(user,
                           manager,
                           host.getTasks(),
                           hostmenu);

            // Now add the host Plugin ContextActions after the Tasks
            if ((host.getUserObjectContextActionGroupByIndex(ActionGroup.PLUGIN) != null)
                && (!host.getUserObjectContextActionGroupByIndex(ActionGroup.PLUGIN).isEmpty()))
                {
                if (hostmenu.getItemCount() > 0)
                    {
                    hostmenu.addSeparator();
                    }

                // We can only get there if we have permission to see the host plugin,
                // so we don't need to check permissions any further
                addActionGroupToMenuAndToolbar(host.getUserObjectContextActionGroupByIndex(ActionGroup.PLUGIN),
                                               hostmenu,
                                               toolbar);
                }

            // Finally add the BuildNumber for this Plugin, if one exists
            addVersionConfiguration(host, hostmenu);
            }
        }


    /***********************************************************************************************
     * Add the VersionNumber, BuildNumber and BuildStatus for the specified Plugin to the specified JMenu.
     *
     * @param plugin
     * @param menu
     */

    private static void addVersionConfiguration(final AtomPlugin plugin,
                                                final JMenu menu)
        {
        if ((plugin != null)
            && (menu != null)
            && (!EMPTY_STRING.equals(plugin.getVersionNumber()))
            && (!EMPTY_STRING.equals(plugin.getBuildNumber())))
            {
            final JMenuItem menuItem;
            final ImageIcon icon;

            icon = RegistryModelUtilities.getCommonIcon(EventStatus.INFO.getIconFilename());

            if (menu.getItemCount() > 0)
                {
                menu.addSeparator();
                }

            menuItem = new JMenuItem(VERSION_MENU
                                     + plugin.getVersionNumber()
                                     + DOT
                                     + plugin.getBuildNumber()
                                     + " (" + plugin.getBuildStatus() + ")");
            menuItem.setToolTipText(TOOLTIP_VERSION_CONFIGURATION);
            menuItem.setIcon(icon);
            menuItem.setEnabled(false);
            menu.add(menuItem);
            }
        }


    /***********************************************************************************************
     * Assemble the menu for the current UserObject and UIComponent Context.
     *
     * @param userobject
     * @param uicomponent
     * @param menubar
     * @param toolbar
     */

    public static void buildContextActions(final UserObjectPlugin userobject,
                                           final UIComponentPlugin uicomponent,
                                           final JMenuBar menubar,
                                           final JToolBar toolbar)
        {
        // Now process the ContextActions which probably come from the current Task
        // We assume that the User can see these,
        // otherwise the UserObject and UIComponent would not be visible!
        if ((menubar != null)
            && (toolbar != null))
            {
            if ((userobject != null)
                && (userobject.getUserObjectContextActionGroups() != null)
                && (!userobject.getUserObjectContextActionGroups().isEmpty())
                && (userobject.getBrowseMode()))
                {
                // Build *only* if we are in Browse mode,
                // assume no extra ContextActions in Edit mode
                buildActions(toolbar, menubar, userobject.getUserObjectContextActionGroups());
                LOGGER.debugNavigation("Adding UserObject ContextActions to the menu and toolbar");
                }
            else
                {
                LOGGER.debugNavigation("No UserObject ContextActions were supplied for the menu or toolbar");
                }

            if ((uicomponent != null)
                && (uicomponent.getUIComponentContextActionGroups() != null)
                && (!uicomponent.getUIComponentContextActionGroups().isEmpty()))
                {
                if (userobject != null)
                    {
                    if (userobject.getBrowseMode())
                        {
                        buildActions(toolbar, menubar, uicomponent.getUIComponentContextActionGroups());
                        LOGGER.debugNavigation("Adding UIComponent ContextActions to the menu and toolbar, for UserObject in Browse mode");
                        }
                    else
                        {
                        LOGGER.debugNavigation("UserObject in Edit mode, so no ContextActions added");
                        }
                    }
                else
                    {
                    buildActions(toolbar, menubar, uicomponent.getUIComponentContextActionGroups());
                    LOGGER.debugNavigation("Adding UIComponent ContextActions to the menu and toolbar, for NULL UserObject");
                    }
                }
            else
                {
                LOGGER.debugNavigation("No UIComponent ContextActions were supplied for the menu or toolbar");
                }
            }
        }


    /***********************************************************************************************
     * Build the Toolbar and Menu Actions from the specified ContextActionGroup.
     *
     * @param toolbar
     * @param menubar
     * @param groups
     */

    private static void buildActions(final JToolBar toolbar,
                                     final JMenuBar menubar,
                                     final Vector<ContextActionGroup> groups)
        {
        final Iterator<ContextActionGroup> iterGroups;

        iterGroups = groups.iterator();

        while (iterGroups.hasNext())
            {
            final ContextActionGroup contextActionGroup;
            final JMenu menuContext;

            contextActionGroup = iterGroups.next();
            menuContext = addActionGroupToMenuAndToolbar(contextActionGroup,
                                                         new JMenu(contextActionGroup.getName()),
                                                         toolbar);

            // Add the finished menu to the menu bar, if there is anything to see
            if (menuContext.getItemCount() > 0)
                {
                menubar.add(menuContext);
                }
            }
        }


    /***********************************************************************************************
     * Add the Actions in the specified ContextActionGroup to the Menu and ToolBar.
     * This can never be called unless the User has permission to see the Plugin.
     *
     * @param group
     * @param menu
     * @param toolbar
     *
     * @return JMenu
     */

    private static JMenu addActionGroupToMenuAndToolbar(final ContextActionGroup group,
                                                        final JMenu menu,
                                                        final JToolBar toolbar)
        {
        // It is permissible to have a null or empty group,
        // for instance in initialising a Report with no ContextActions defined,
        // so just do nothing in that case and return an unchanged Menu
        if ((group != null)
            && (!group.isEmpty())
            && (menu != null)
            && (toolbar != null))
            {
            //ContextActionGroup.showContextActions(group);

            final Iterator<ContextAction> iterActions;
            boolean boolFoundToolbarAction;

            iterActions = group.getActions();
            boolFoundToolbarAction = false;

            while (iterActions.hasNext())
                {
                final ContextAction action;

                action = iterActions.next();

                // Add the Action to the Menu if required
                if ((group.isMenu())
                    && (action.isMenu())
                    && (action.getComponent() == null))
                    {
                    if (!action.isSeparator())
                        {
                        final JMenuItem menuItem;

                        // Create the MenuItem
                        menuItem = new JMenuItem((String)action.getValue(Action.NAME));

                        // The Action just executes immediately, with no confirmation dialog
                        menuItem.setAction(action);
                        menuItem.setToolTipText((String)action.getValue(Action.SHORT_DESCRIPTION));
                        menu.add(menuItem);
                        }
                    else
                        {
                        menu.addSeparator();
                        }
                    }

                // Add the Action to the ToolBar, as a JButton or JComponent
                if (action.isToolBar())
                    {
                    if (!action.isSeparator())
                        {
                        if (action.getComponent() == null)
                            {
                            final JButton buttonToolBar = new JButton();
                            buttonToolBar.setBorder(BORDER_BUTTON);
                            buttonToolBar.setAction(action);
                            buttonToolBar.setText(EMPTY_STRING);
                            buttonToolBar.setToolTipText((String)action.getValue(Action.SHORT_DESCRIPTION));
                            toolbar.add(buttonToolBar);

                            // We did find at least one Toolbar Action, which wasn't a separator
                            // so there is a Group on the Toolbar
                            boolFoundToolbarAction = true;
                            }
                        else
                            {
                            // Add the JComponent directly to the JToolBar
                            action.getComponent().setToolTipText((String)action.getValue(Action.SHORT_DESCRIPTION));
                            toolbar.add(action.getComponent());

                            // We did find at least one Toolbar Action, which wasn't a separator
                            // so there is a Group on the Toolbar
                            boolFoundToolbarAction = true;
                            }
                        }
                    else
                        {
                        //LOGGER.debugNavigation("Adding separator to Toolbar *in* group [group=" + group.getName() + "]");
                        toolbar.addSeparator(DIM_TOOLBAR_SEPARATOR);
                        }
                    }
                }

            // Separate each ActionGroup on the ToolBar
            if (boolFoundToolbarAction)
                {
                //LOGGER.debugNavigation("Adding separator to Toolbar *after* group [group=" + group.getName() + "]");
                toolbar.addSeparator(DIM_TOOLBAR_SEPARATOR);
                }
            }

        return (menu);
        }


    /**********************************************************************************************/
    /* Plugin and Task Control                                                                    */
    /***********************************************************************************************
     * Add the specified Plugin to the Menu.
     *
     * @param user
     * @param manager
     * @param plugin
     * @param menu
     */

    private static void addPluginToMenu(final UserPlugin user,
                                        final FrameworkManagerUIComponentPlugin manager,
                                        final AtomPlugin plugin,
                                        final JMenu menu)
        {
        // Check that the User has permission to see this Plugin
        // It is possible to have a null FrameworkManager,
        // so we cannot check the parameter
        if ((user != null)
            && (user.validatePlugin())
            && (user.getRole().isAtomViewer())
            && (plugin != null)
            && (plugin.validatePlugin())
            && (plugin.getUserRoles().contains(user.getRole()))
            && (menu != null))
            {
            final JMenuItem menuItem;

            // Create a menu item for this Plugin and add to the Dynamic menu
            // This is the item which will stop or start the plugin
            menuItem = new JMenuItem(plugin.getName());
            menuItem.setIcon(RegistryModelUtilities.getAtomIcon(plugin, plugin.getIconFilename()));
            menuItem.setToolTipText(plugin.getDescription());

            menuItem.addActionListener(new ActionListener()
                {
                public void actionPerformed(final ActionEvent event)
                    {
                    LOGGER.debugNavigation("\nMenu click on PLUGIN");

                    if (manager != null)
                        {
                        // Click on running plugin --> show the running ui
                        // Ctrl-click or right click on running plugin --> dialog to ask if ok to stop
                        // if Yes, stop plugin; if No, show running ui
                        // Click on stopped plugin --> dialog to ask if ok to start
                        // if Yes, run plugin; if No, leave BlankUI
                        // Ctrl-click or right click on stopped plugin --> do nothing
                        controlPluginOrTaskFromMenu(plugin, plugin.isRunning(), event);
                        }
                    else
                        {
                        unableToPerformAction();
                        }
                    }
                });

            if (!plugin.isClassFound())
                {
                menuItem.setForeground(COLOUR_CLASSNOTFOUND);
                }

            // Do not add the Item if the User does not have permission!
            // ToDo Review if it is ok to add if the class is not found?
            menu.add(menuItem);
            }
        else
            {
            LOGGER.error("addPluginToMenu()" + SPACE + EXCEPTION_PARAMETER_INVALID);
            }
        }


    /***********************************************************************************************
     * Add the specified Tasks to the Menu,
     * checking for recursive calls into the FrameworkManager and UserInterface.
     * Check that the User has permission to see each Task.
     *
     * @param user
     * @param manager
     * @param tasks
     * @param menu
     */

    private static void addTasksToMenu(final UserPlugin user,
                                       final FrameworkManagerUIComponentPlugin manager,
                                       final Vector<RootPlugin> tasks,
                                       final JMenu menu)
        {
        final Iterator iterTasks;

        // We may or may not have a FrameworkManager UI at this point...
        if ((user != null)
            && (user.validatePlugin())
            && (user.getRole().isTaskViewer())
            && (tasks != null)
            && (!tasks.isEmpty())
            && (menu != null))
            {
            iterTasks = tasks.iterator();

            while (iterTasks.hasNext())
                {
                final TaskPlugin task = (TaskPlugin) iterTasks.next();

                // Check that the User has permission to see this Task
                if ((task.validatePlugin())
                    && (task.getUserRoles().contains(user.getRole())))
                    {
                    addTaskToMenu(user, manager, task, menu);
                    }
                }
            }
        }


    /***********************************************************************************************
     * Create a menu item for this Task and add to the Dynamic menu.
     *
     * @param user
     * @param manager
     * @param task
     * @param menu
     */

    private static void addTaskToMenu(final UserPlugin user,
                                      final FrameworkManagerUIComponentPlugin manager,
                                      final TaskPlugin task,
                                      final JMenu menu)
        {
        final URL imageURL;
        final JMenuItem menuItem;

        // We ASSUME that the parameters have been validated by the caller

        menuItem = new JMenuItem(task.getName());
        menuItem.setToolTipText(task.getDescription());
        imageURL = task.getClass().getResource(RegistryModelUtilities.getCommonImagesRoot()
                                               + task.getIconFilename());
        if (imageURL != null)
            {
            menuItem.setIcon(new ImageIcon(imageURL));
            }

        if (task.isPublic())
            {
            menuItem.addActionListener(new ActionListener()
                {
                public void actionPerformed(final ActionEvent event)
                    {
                    LOGGER.debugNavigation("\nMenu click on public TASK");

                    if (manager != null)
                        {
                        // Confirm that the User realises they are controlling
                        // a runnable (background) task
                        if (task.isRunnable())
                            {
                            // Click on running plugin --> show the running ui
                            // Ctrl-click or right click on running plugin --> dialog to ask if ok to stop
                            // if Yes, stop plugin; if No, show running ui
                            // Click on stopped plugin --> dialog to ask if ok to start
                            // if Yes, run plugin; if No, leave BlankUI
                            // Ctrl-click or right click on stopped plugin --> do nothing
                            controlPluginOrTaskFromMenu(task, task.isRunning(), event);
                            }
                        else
                            {
                            // A non-runnable Task, so just do it!
                            // Start the Task immediately
                            // The Task will stop when it loses focus
                            // Are we trying to recurse into ourselves?!
                            // Force the Plugin into Browse mode
                            executeOrBlockRecursion(task, event, FrameworkManagerUIComponentPlugin.MODE_BROWSE);
                            }
                        }
                    else
                        {
                        unableToPerformAction();
                        }
                    }
                });
            }
        else
            {
            // Private Tasks may not be executed by the User
            menuItem.setEnabled(false);
            }

        // This should never happen...
        if (!task.isClassFound())
            {
            menuItem.setForeground(COLOUR_CLASSNOTFOUND);
            }

        // Do not add the Item if the User does not have permission!
        menu.add(menuItem);
        }


    /***********************************************************************************************
     * Control the specified Plugin from a menu, using the action in the Event.
     *
     * @param plugin
     * @param running
     * @param event
     */

    private static void controlPluginOrTaskFromMenu(final UserObjectPlugin plugin,
                                                    final boolean running,
                                                    final ActionEvent event)
        {
        // We ASSUME that the parameters have been validated by the caller

        // Is it a running Plugin?
        if (running)
            {
            if (isPlainClick(event))
                {
                // Click on running plugin --> show the running ui, force into Browse mode
                LOGGER.debugNavigation("controlPluginOrTaskFromMenu isRunning isPlainClick executeOrBlockRecursion");
                executeOrBlockRecursion(plugin, event, FrameworkManagerUIComponentPlugin.MODE_BROWSE);
                }
            else if (isSpecialClick(event))
                {
                // Ctrl-click or right click on running plugin --> dialog to ask if ok to stop
                // if Yes, stop plugin; if No, show running ui
                // If in Editor mode, force the User to choose Browse mode first

                if (showConfirmDialog(plugin, running) == JOptionPane.YES_OPTION)
                    {
                    // Just put the Plugin into the opposite state
                    // Stop any recursion into ourselves!
                    LOGGER.debugNavigation("controlPluginOrTaskFromMenu isRunning isSpecialClick YES haltOrBlockRecursion");
                    haltOrBlockRecursion(plugin, event, plugin.getBrowseMode());
                    }
                else
                    {
                    LOGGER.debugNavigation("controlPluginOrTaskFromMenu isRunning isSpecialClick NO executeOrBlockRecursion");
                    executeOrBlockRecursion(plugin, event, plugin.getBrowseMode());
                    }
                }
            else
                {
                // Invalid gesture
                LOGGER.debugNavigation("controlPluginOrTaskFromMenu isRunning Invalid gesture");
                Toolkit.getDefaultToolkit().beep();
                }
            }
        else
            {
            // The Plugin is STOPPED
            if (isPlainClick(event))
                {
                // Click on stopped plugin --> dialog to ask if ok to start
                // if Yes, run plugin; if No, leave unchanged UI

                if (showConfirmDialog(plugin, running) == JOptionPane.YES_OPTION)
                    {
                    // Just put the Plugin into the opposite state, force into Browse mode
                    // Stop any recursion into ourselves!
                    LOGGER.debugNavigation("controlPluginOrTaskFromMenu stopped plugin, isPlainClick YES executeOrBlockRecursion");
                    executeOrBlockRecursion(plugin, event, FrameworkManagerUIComponentPlugin.MODE_BROWSE);
                    }
                else
                    {
                    // Blank UI
                    LOGGER.debugNavigation("controlPluginOrTaskFromMenu stopped plugin, isPlainClick NO leave unchanged UI");
                    }
                }
            else if (isSpecialClick(event))
                {
                // Ctrl-click or right click on stopped plugin --> do nothing
                LOGGER.debugNavigation("controlPluginOrTaskFromMenu stopped plugin, Ctrl-click or right click --> do nothing");
                Toolkit.getDefaultToolkit().beep();
                }
            else
                {
                // Invalid gesture
                LOGGER.debugNavigation("controlPluginOrTaskFromMenu stopped plugin, Invalid gesture");
                Toolkit.getDefaultToolkit().beep();
                }
            }
        }


    /**********************************************************************************************/
    /* Navigation Tree                                                                            */
    /***********************************************************************************************
     * Build the Task Popup Menu for the specified UserObjectPlugin.
     *
     * @param userobject
     * @param manager
     * @param menu
     * @param event
     * @param rectangle
     */

    public static void buildTaskPopupMenu(final UserObjectPlugin userobject,
                                          final FrameworkManagerUIComponentPlugin manager,
                                          final JPopupMenu menu,
                                          final MouseEvent event,
                                          final Rectangle rectangle)
        {
        JMenuItem menuItem;

        if (((TaskPlugin)userobject).isRunnable())
            {
            //--------------------------------------------------------------------------------------
            // Runnable Tasks must have a Start/Stop menu

            menuItem = new JMenuItem("Start Task");

            menuItem.addActionListener(new ActionListener()
                {
                public void actionPerformed(final ActionEvent event)
                    {
                    // Are we trying to recurse into ourselves?!
                    // This calls clearUIOccupant(), setUIOccupant() and setTaskState()
                    // We should never be able to show this menu for *recursive* calls,
                    // since the Task would have to be selected first,
                    // and that is already blocked in treeSelection()
                    executeOrBlockRecursion(userobject,
                                            event,
                                            FrameworkManagerUIComponentPlugin.MODE_BROWSE);
                    }
                });

            // Modify the appearance of the menu item to suit the Task state
            // *at the time the menu is built*
            // Is the Task active?
            if (userobject.isActive())
                {
                // The Task may be started only if currently in Idle or Initialised
                // and it is not the RootTask, UserInterface or FrameworkManager!
                if (((TaskState.INITIALISED.equals(((TaskPlugin)userobject).getState()))
                    || (TaskState.IDLE.equals(((TaskPlugin)userobject).getState())))
                    && (!((TaskPlugin)userobject).isRootTask())
                    && (!userobject.equals(REGISTRY_MODEL.getUserInterface()))
                    && (!userobject.equals(REGISTRY_MODEL.getFrameworkManager())))
                    {
                    menuItem.setEnabled(true);
                    menuItem.setToolTipText("Start this Task now");
                    }
                else
                    {
                    menuItem.setEnabled(false);
                    menuItem.setToolTipText("The Task may not be started");
                    }
                }
            else
                {
                menuItem.setEnabled(false);
                menuItem.setToolTipText("The Task is inactive");
                }

            menu.add(menuItem);

            //--------------------------------------------------------------------------------------
            // Tasks may be moved to STATE_TASK_IDLE (not STATE_TASK_STOPPED)

            menuItem = new JMenuItem("Stop Task");
            menuItem.addActionListener(new ActionListener()
                {
                public void actionPerformed(final ActionEvent event)
                    {
                    // Force the Plugin into Browse mode to stop it
                    haltOrBlockRecursion(userobject,
                                         event,
                                         FrameworkManagerUIComponentPlugin.MODE_BROWSE);
                    }
                });

            // Modify the menu item to suit the Task state
            // *at the time the menu is built*
            // Is the Task active?
            if (userobject.isActive())
                {
                // The Task may be stopped (Idle) only if currently in Running
                // and it is not a RootTask, UserInterface or FrameworkManager!
                if ((TaskState.RUNNING.equals(((TaskPlugin)userobject).getState()))
                    && (!((TaskPlugin)userobject).isRootTask())
                    && (!userobject.equals(REGISTRY_MODEL.getUserInterface()))
                    && (!userobject.equals(REGISTRY_MODEL.getFrameworkManager())))
                    {
                    menuItem.setEnabled(true);
                    menuItem.setToolTipText("Stop this Task now");
                    }
                else
                    {
                    menuItem.setEnabled(false);
                    menuItem.setToolTipText("The Task may not be stopped");
                    }
                }
            else
                {
                menuItem.setEnabled(false);
                menuItem.setToolTipText("The Task is inactive");
                }

            menu.add(menuItem);
            }
        else
            {
            //System.out.println("TASK NOT RUNNABLE");
            // Non-Runnable Tasks Menu
            // Just Execute this Task immediately, unless it's already there
            menuItem = new JMenuItem("Execute Task");

            menuItem.addActionListener(new ActionListener()
                {
                public void actionPerformed(final ActionEvent event)
                    {
                    executeOrBlockRecursion(userobject,
                                            event,
                                            FrameworkManagerUIComponentPlugin.MODE_BROWSE);
                    }
                });

            // Modify the menu item to suit the Task state
            // *at the time the menu is built*
            // Is the Task active?
            if (userobject.isActive())
                {
                // Non-runnable Tasks may already be visible
                // So check if this is a click on the same Task as before
                if (manager.getUIOccupant().equals(userobject))
                    {
                    // If visible, do nothing
                    menuItem.setEnabled(false);
                    menuItem.setToolTipText("This Task is already visible");
                    //System.out.println(".treeSelection() Non-runnable visible do nothing");
                    }
                else
                    {
                    // Otherwise, show a blank panel until user has made a decision
                    menuItem.setEnabled(true);
                    menuItem.setToolTipText("Execute this Task now");
                    //System.out.println(".treeSelection() Non-runnable show Blank panel");
                    }
                }
            else
                {
                menuItem.setEnabled(false);
                menuItem.setToolTipText("The Task is inactive");
                }

            menu.add(menuItem);
            }

        //------------------------------------------------------------------------------------------
        // All Tasks may be configured via the Editor

        menuItem = new JMenuItem("Configure");

        // Is there an editor for this Task?
        if (((userobject.getEditorClassname() != null)
            && (!EMPTY_STRING.equals(userobject.getEditorClassname().trim()))))
            {
            menuItem.addActionListener(new ActionListener()
                {
                public void actionPerformed(final ActionEvent event)
                    {
                    // Run the Editor
                    LOGGER.debugNavigation("Run the Editor from popup " + userobject.getEditorClassname());
                    executeOrBlockRecursion(userobject,
                                            event,
                                            FrameworkManagerUIComponentPlugin.MODE_EDIT);
                    }
                });
            menuItem.setEnabled(true);
            menuItem.setToolTipText("Edit the properties of this Task");
            }
        else
            {
            // There is no Editor installed...
            menuItem.setEnabled(false);
            menuItem.setToolTipText("There is no Editor installed");
            }

        // Position the menu relative to the selected node
        menu.addSeparator();
        menu.add(menuItem);

        positionPopupMenu(menu,
                          event,
                          rectangle,
                          ((JSplitPane) manager.getSplitScreenComponent()).getDividerLocation());
        }


    /**********************************************************************************************/
    /* Execute, Halt and BlockRecursion                                                           */
    /***********************************************************************************************
     * Perform the action of the specified UserObjectPlugin,
     * but check first that the user is not trying to recurse
     * into the FrameworkManager or the UserInterface tasks.
     *
     * @param userobject
     * @param event
     * @param browsemode
     */

    public static void executeOrBlockRecursion(final UserObjectPlugin userobject,
                                               final AWTEvent event,
                                               final boolean browsemode)
        {
        if ((userobject != null)
            && (userobject.validatePlugin())
            && (REGISTRY_MODEL.getFrameworkManager() != null)
            && (REGISTRY_MODEL.getFrameworkManager().getUI() != null)
            && (REGISTRY_MODEL.getUserInterface() != null))
            {
            LOGGER.debugNavigation("\n\n***************************************************************");
            LOGGER.debugNavigation("ExecuteOrBlockRecursion " + userobject.getPathname());

            // The FrameworkManager can exist only if there is also a UserInterface installed
            // so we must check that the User is not trying to select
            // the Framework, the FrameworkManager or the UserInterface
            // ToDo Review recursion into Framework
            if (((userobject.equals(REGISTRY_MODEL.getFramework()))
                || (userobject.equals(REGISTRY_MODEL.getFrameworkManager()))
                || (userobject.equals(REGISTRY_MODEL.getUserInterface()))))
                {
                blockRecursion(REGISTRY_MODEL.getFrameworkManager().getUI(), userobject);
                }
            else
                {
                // Otherwise, we can execute the UserObject action
                // !! This is where Plugins are started and Tasks are run !!

                userobject.setCaption(userobject.getPathname());
                userobject.setStatus(userobject.getPathname());

                // ActionPerformed must:
                //      use FrameworkManagerUIComponentPlugin to:
                //          clear the previous UI occupant
                //          set the new UI occupant
                //          record the new navigation tree selection
                //      use RegistryModelController to:
                //          setPluginState() or setTaskState()
                userobject.actionPerformed(event, browsemode);
                }
            }
        else
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_INVALID);
            }
        }


    /***********************************************************************************************
     * Halt the action of the specified UserObjectPlugin,
     * but check first that the user is not trying to recurse
     * into the FrameworkManager or the UserInterface tasks.
     *
     * @param userobject
     * @param event
     * @param browsemode
     */

    public static void haltOrBlockRecursion(final UserObjectPlugin userobject,
                                            final AWTEvent event,
                                            final boolean browsemode)
        {
        if ((userobject != null)
            && (userobject.validatePlugin())
            && (REGISTRY_MODEL.getFrameworkManager() != null)
            && (REGISTRY_MODEL.getFrameworkManager().getUI() != null)
            && (REGISTRY_MODEL.getUserInterface() != null))
            {
            LOGGER.debugNavigation("\n\n***************************************************************");
            LOGGER.debugNavigation("HaltOrBlockRecursion " + userobject.getPathname());

            // The FrameworkManager can exist only if there is also a UserInterface installed
            // so we must check that the User is not trying to select
            // the Framework, the FrameworkManager or the UserInterface
            // ToDo Review recursion into Framework
            if (((userobject.equals(REGISTRY_MODEL.getFramework()))
                || (userobject.equals(REGISTRY_MODEL.getFrameworkManager()))
                || (userobject.equals(REGISTRY_MODEL.getUserInterface()))))
                {
                blockRecursion(REGISTRY_MODEL.getFrameworkManager().getUI(), userobject);
                }
            else
                {
                // Otherwise, we can halt the UserObject action
                // !! This is where Plugins are Shutdown and Tasks are made Idle !!

                userobject.setCaption(userobject.getPathname());
                userobject.setStatus(userobject.getPathname());

                // ActionHalted must:
                //      use FrameworkManagerUIComponentPlugin to:
                //          clear the previous UI occupant
                //          set the new UI occupant to BlankUI
                //      use RegistryModelController to:
                //          setPluginState() or setTaskState()
                userobject.actionHalted(event, browsemode);
                }
            }
        else
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_INVALID);
            }
        }


    /***********************************************************************************************
     * Block recursion into the specified UserObjectPlugin.
     *
     * @param manager
     * @param userobject
     */

    private static void blockRecursion(final FrameworkManagerUIComponentPlugin manager,
                                       final UserObjectPlugin userobject)
        {
        LOGGER.debugNavigation("BlockRecursion into " + userobject.getPathname());

        // Remind the User of what they selected incorrectly
        userobject.setCaption(userobject.getPathname());
        userobject.setStatus("Recursive use of"
                                + SPACE
                                + userobject.getPathname()
                                + SPACE
                                + "is not allowed!");

        // We cannot execute the UserObject action, so...
        // Remove the previous occupant of the UI Panel
        manager.clearUIOccupant(userobject);

        // Use a blank panel instead of the FrameworkManager's real UI component...
        manager.setUIOccupant(new NullData());

        // Record the new selection
        manager.getNavigationTree().setSelectionPath(new TreePath(userobject.getHostTreeNode().getPath()));
        manager.setSelectedTreeNode(userobject.getHostTreeNode());
        }


    /***********************************************************************************************
     * Is it a plain click.
     *
     * @param event
     *
     * @return boolean
     */

    private static boolean isPlainClick(final ActionEvent event)
        {
        boolean boolClick;
        final int intModifiers;

        intModifiers = event.getModifiers();

        boolClick = ((intModifiers & ActionEvent.CTRL_MASK) == 0);
        boolClick = boolClick && ((intModifiers & ActionEvent.ALT_MASK) == 0);
        boolClick = boolClick && ((intModifiers & ActionEvent.SHIFT_MASK) == 0);

        return (boolClick);
        }


    /***********************************************************************************************
     * Is it a special click.
     *
     * @param event
     *
     * @return boolean
     */

    private static boolean isSpecialClick(final ActionEvent event)
        {
        boolean boolClick;
        final int intModifiers;

        intModifiers = event.getModifiers();

        boolClick = ((intModifiers & ActionEvent.CTRL_MASK) != 0);
        boolClick = boolClick && ((intModifiers & ActionEvent.ALT_MASK) == 0);
        boolClick = boolClick && ((intModifiers & ActionEvent.SHIFT_MASK) == 0);

        return (boolClick);
        }


    /***********************************************************************************************
     * Show the dialog to confirm the User's action to start or stop a Plugin or Task,
     * depending on its running state.
     *
     * @param plugin
     * @param running
     *
     * @return int JOptionPane choice
     */

    private static int showConfirmDialog(final UserObjectPlugin plugin,
                                         final boolean running)
        {
        int intIndex;
        final int intChoice;
        final String [] strMessage =
            {
            MSG_CONFIRM_STOP,
            MSG_CONFIRM_START
            };

        // todo more dialog explanation about consequences of shutdown of plugins etc.

        // Prepare the appropriate message index
        intIndex = 0;

        if (!running)
            {
            intIndex = 1;
            }

        intChoice = JOptionPane.showOptionDialog(null,
                                                 strMessage[intIndex]
                                                     + SPACE
                                                     + plugin.getName()
                                                     + QUERY,
                                                 plugin.getPathname()
                                                     + SPACE
                                                     + DIALOG_PLUGIN_CONTROL,
                                                 JOptionPane.YES_NO_OPTION,
                                                 JOptionPane.QUESTION_MESSAGE,
                                                 null,
                                                 null,
                                                 null);
        return (intChoice);
        }


    /***********************************************************************************************
     * Indicate that the Action could not be performed.
     */

    public static void unableToPerformAction()
        {
        final String [] message =
            {
            MSG_CANNOT_COMPLETE,
            MSG_NO_RENDERER
            };

        Toolkit.getDefaultToolkit().beep();
        JOptionPane.showMessageDialog(null,
                                      message,
                                      DIALOG_CONFIGURATION,
                                      JOptionPane.WARNING_MESSAGE);
        }


    /**********************************************************************************************/
    /* Miscellaneous                                                                              */
    /***********************************************************************************************
     * Assemble the Help Menu.
     * All users can always see the Help!
     *
     * @param helpactions
     * @param menubar
     * @param toolbar
     */

    public static void rebuildHelp(final Vector<ContextAction> helpactions,
                                   final JMenuBar menubar,
                                   final JToolBar toolbar)
        {
        if ((helpactions != null)
            && (menubar != null)
            && (toolbar != null))
            {
            final JMenu menuHelp;

            // The dynamic Help menu
            menuHelp = new JMenu(MENU_HELP);

            // See if any Atom or Task has added some Help
            final Iterator<ContextAction> iterActions;

            iterActions = helpactions.iterator();

            while (iterActions.hasNext())
                {
                final ContextAction helpAction;

                helpAction = iterActions.next();

                // Add the Action to the Menu
                if (helpAction.isMenu())
                    {
                    final JMenuItem menuItem;

                    // Create the MenuItem
                    menuItem = new JMenuItem((String)helpAction.getValue(Action.NAME));
                    menuItem.setAction(helpAction);
                    menuItem.setToolTipText((String)helpAction.getValue(Action.SHORT_DESCRIPTION));
                    menuHelp.add(menuItem);
                    menuHelp.add(menuItem);
                    menuHelp.add(menuItem);
                    }
                }

            // Add the Help to the menu bar
            menubar.add(menuHelp);
            }
        }


    /***********************************************************************************************
     * Position a Popup Menu relative to the selected node,
     * but always so that the left edge is not beyond the tree panel given by dividerlocation.
     *
     * @param popupMenu
     * @param event
     * @param rectangle
     * @param dividerlocation
     */

    public static void positionPopupMenu(final JPopupMenu popupMenu,
                                         final MouseEvent event,
                                         final Rectangle rectangle,
                                         final int dividerlocation)
        {
        int intPopupLeft;

        intPopupLeft = (int)rectangle.getX() + (int)rectangle.getWidth();

        if (intPopupLeft > dividerlocation)
            {
            intPopupLeft = dividerlocation - 50;
            }

        popupMenu.show(event.getComponent(),
                       intPopupLeft,
                       (int)rectangle.getY() + ((int) rectangle.getHeight() >> 1) );
        }


    /***********************************************************************************************
     * Show the currently installed LookAndFeels.
     */

    public static void showLookAndFeels()
        {
        final UIManager.LookAndFeelInfo[] lookAndFeels;

        LOGGER.debugNavigation("Installed Look and Feels");
        lookAndFeels = UIManager.getInstalledLookAndFeels();

        for (int j = 0; j < lookAndFeels.length; j++)
             {
             LOGGER.debugNavigation(INDENT + lookAndFeels[j].getClassName());
             }
        }


    /***********************************************************************************************
     * Get the specified UserObjectPlugin's UIComponentPlugin to be shown in the current mode.
     *
     * @param userobject
     *
     * @return UIComponentPlugin
     */

    public static UIComponentPlugin getVisibleComponentOfUserObjectAndRunUI(final UserObjectPlugin userobject)
        {
        UIComponentPlugin uiComponent;

        uiComponent = null;

        if (userobject != null)
            {
            LOGGER.debugNavigation("START getVisibleComponentOfUserObjectAndRunUI() " + userobject.getPathname());

            if (userobject.getBrowseMode())
                {
                uiComponent = userobject.getUIComponent();
                //System.out.println("getVisibleComponentOfUserObjectAndRunUI() UIComponent=" + uiComponent.getClass().getName());
                }
            else
                {
                uiComponent = userobject.getEditorComponent();
                //System.out.println("getVisibleComponentOfUserObjectAndRunUI() EditorComponent=" + uiComponent.getClass().getName());
                }

            if (uiComponent != null)
                {
                uiComponent.setMinimumSize(FrameworkManagerUIComponentPlugin.DIM_PANEL_SIZE_MIN);
                uiComponent.setPreferredSize(FrameworkManagerUIComponentPlugin.DIM_PANEL_SIZE_MAX);

                // This is the only call to UserObject.runUI()
                LOGGER.debugNavigation("getVisibleComponentOfUserObjectAndRunUI() --> userobject.runUI()");
                userobject.runUI();
                }
            }

        LOGGER.debugNavigation("END getVisibleComponentOfUserObjectAndRunUI()");

        return (uiComponent);
        }


    /***********************************************************************************************
     * Install the UI Component of the UserObjectPlugin in the FrameworkManager, if possible.
     * If the FrameworkManager does not exist, show a warning box.
     *
     * @param userobject
     * @param manager
     */

    public static void installUserObjectUI(final UserObjectPlugin userobject,
                                           final FrameworkManagerPlugin manager)
        {
        LOGGER.debugNavigation("NavigationUtilities.installUserObjectUI()");

        if ((userobject != null)
            && (userobject.validatePlugin())
            && (manager != null)
            && (manager.validatePlugin())
            && (manager.isRunning())
            && (manager.getUI() != null))
            {
            // Remove the previous occupant of the UI Panel
            // Set running Tasks to Idle
            // Dispose of any Editor component
            manager.getUI().clearUIOccupant(userobject);
            manager.getUI().setUIOccupant(userobject);
            }
        else
            {
            unableToPerformAction();
            }
        }


    /***********************************************************************************************
     * Taken from the JDK and improved!!
     * A simple minded look and feel change: ask each node in the tree
     * to <code>updateUI()</code> -- that is, to initialize its UI property
     * with the current look and feel.
     *
     * @param component
     */

    public static void updateComponentTreeUI(final Component component)
        {
        SwingUtilities.invokeLater(new Runnable()
            {
            public void run()
                {
                if (component != null)
                    {
                    try
                        {
                        updateComponentTreeUI0(component);
                        component.invalidate();
                        component.validate();         // Array index exception
                        component.repaint();
                        }

                    catch (Exception exception)
                        {
                        LOGGER.logTimedEvent("NavigationUtilities.updateComponentTreeUI() Exception absorbed silently");

                        // Let's not make a fuss about something we can't change...
                        //exception.printStackTrace();
                        }
                    }
                }
            });
        }


    /***********************************************************************************************
     * A recursive walk along the UI tree.
     *
     * @param component
     */

    private static void updateComponentTreeUI0(final Component component)
        {
        try
            {
            if (component != null)
                {
                if (component instanceof JComponent)
                    {
                    //System.out.println("JComponent class=" + component.getClass().getName() + " name=" + component.getName());
                    ((JComponent)component).updateUI();
                    }

                Component[] children = null;

                if (component instanceof JMenu)
                    {
                    children = ((JMenu)component).getMenuComponents();
                    }
                else if (component instanceof Container)
                    {
                    children = ((Container)component).getComponents();
                    }

                if (children != null)
                    {
                    for(int i = 0; i < children.length; i++)
                        {
                        if (children[i] != null)
                            {
                            updateComponentTreeUI0(children[i]);
                            }
                        }
                    }
                }
            }

        catch (Throwable e)
            {
            LOGGER.error("NavigationUtilities.updateComponentTreeUI0() component=" + component);
            e.printStackTrace();
            }
        }


    /***********************************************************************************************
     * Show the UserInterface and FrameworkManager, if possible.
     *
     * @param userinterface
     * @param manager
     */

    public static void showFrameworkUI(final UserInterfacePlugin userinterface,
                                       final FrameworkManagerPlugin manager)
        {
        if ((userinterface != null)
            && (userinterface.isRunning())
            && (userinterface.getUIComponent() != null))
            {
            userinterface.getUI().runUI();
            userinterface.getUI().getContainer().validate();
            userinterface.getUI().getContainer().requestFocusInWindow();

            REGISTRY_MODEL.rebuildNavigation(userinterface, userinterface.getUI());
            }

        if ((manager != null)
            && (manager.isRunning())
            && (manager.getUIComponent() != null))
            {
            manager.getUI().runUI();

            REGISTRY_MODEL.rebuildNavigation(manager, manager.getUI());
            }
        }
    }
