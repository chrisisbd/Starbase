//--------------------------------------------------------------------------------------------------
// Revision History
//
//  23-01-02    LMN created file
//  01-02-02    LMN had the structure working, with database load
//  04-02-02    LMN added get() and set() for all items
//  05-02-02    LMN finished the first complete version!
//  04-03-02    LMN added TimingMode
//  24-04-02    LMN rewrote the Property Parser, to avoid database timing bugs?
//  26-04-02    LMN added getApplicationID() to help object use...
//  19-04-03    LMN reduced default hashtable size
//  22-04-03    LMN added setHostframeworkModel()
//  02-06-03    LMN extensive rewrite to break into several methods etc.
//  21-06-03    LMN added dynamic creation of LookAndFeel menu!
//  24-06-03    LMN completed conversion to loading multiple Applications
//  25-06-03    LMN added Context Menus
//  30-09-03    LMN had new Framework hierarchy loaded and displayed!
//  07-10-03    LMN made major changes for dynamic resources etc.
//  06-11-03    LMN removed the last of the FrameworkManager items
//  09-11-04    LMN adding LoginPanel narrative
//  16-01-06    LMN making major changes for plugins
//  17-07-06    LMN removed Controller methods, tidied up at last!
//
//--------------------------------------------------------------------------------------------------

package org.lmn.fc.model.registry.impl;

//--------------------------------------------------------------------------------------------------
// Imports

import org.lmn.fc.common.actions.ContextAction;
import org.lmn.fc.common.events.ActiveChangeListener;
import org.lmn.fc.common.exceptions.FrameworkException;
import org.lmn.fc.common.loaders.LoaderProperties;
import org.lmn.fc.model.logging.Logger;
import org.lmn.fc.model.plugins.AtomPlugin;
import org.lmn.fc.model.plugins.FrameworkPlugin;
import org.lmn.fc.model.registry.NavigationUtilities;
import org.lmn.fc.model.registry.RegistryModelPlugin;
import org.lmn.fc.model.registry.RegistryModelUtilities;
import org.lmn.fc.model.registry.RegistryPlugin;
import org.lmn.fc.model.root.RootPlugin;
import org.lmn.fc.model.root.UserObjectPlugin;
import org.lmn.fc.model.tasks.FrameworkManagerPlugin;
import org.lmn.fc.model.tasks.UserInterfacePlugin;
import org.lmn.fc.model.users.UserPlugin;
import org.lmn.fc.ui.UIComponentPlugin;
import org.lmn.fc.ui.manager.FrameworkManagerUIComponentPlugin;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.util.Stack;
import java.util.Vector;


/***************************************************************************************************
 * The RegistryModel holds a specific configuration for the current User.
 */

public final class RegistryModel implements RegistryModelPlugin
    {
    private static final RegistryPlugin REGISTRY = Registry.getInstance();
    private static final LoaderProperties LOADER_PROPERTIES = LoaderProperties.getInstance();
    private static final Logger LOGGER = Logger.getInstance();

    private static final String TOOLTIP_LOOK_AND_FEEL = "Look and Feel:";

    private volatile static RegistryModelPlugin REGISTRY_MODEL;

    private DefaultMutableTreeNode nodeModel;           // The root Node of the RegistryModel
    private AtomPlugin pluginFramework;                 // The loaded Framework
    private UserInterfacePlugin userInterface;          // The UserInterface (Frame and Menus)
    private FrameworkManagerPlugin frameworkManager;    // The FrameworkManager (ToolBar & Status)
    private UserPlugin userPlugin;

    // Navigation structure
    private JMenuBar menuBar;
    private JToolBar toolBar;

    // Help Actions added by Tasks when initialising
    private Vector<ContextAction> vecHelpActions;

    // A stack of running RootPlugins
    private Stack<RootPlugin> stackRunning;

    // Miscellaneous
    private long longSize;
    private boolean boolUpdated;


    /***********************************************************************************************
     * The RegistryModel is a Singleton!
     *
     * @return RegistryModel
     */

    public static RegistryModelPlugin getInstance()
        {
        if (REGISTRY_MODEL == null)
            {
            synchronized (RegistryModel.class)
                {
                if (REGISTRY_MODEL == null)
                    {
                    REGISTRY_MODEL = new RegistryModel();
                    }
                }
            }

        return (REGISTRY_MODEL);
        }


    /***********************************************************************************************
     * Construct a RegistryModel.
     */

    private RegistryModel()
        {
        // Only reset the User during construction of the Singleton
        // The LoginDialog requires the RegistryModel to hold the User's details
        this.userPlugin = null;
        }


    /***********************************************************************************************
     * Initialise the RegistryModel.
     * Specify the Atom to appear at the root of the model (i.e. the Framework).
     *
     * @param atom
     */

    public void initialiseModel(final AtomPlugin atom)
        {
        LOGGER.logTimedEvent(METADATA_MODEL_INITIALISE);

        // Wipe all traces of user-specific structure from the Registry
        RegistryModelUtilities.unlinkPlugins(REGISTRY.getAtoms());
        RegistryModelUtilities.unlinkPlugins(REGISTRY.getTasks());
        RegistryModelUtilities.unlinkPlugins(REGISTRY.getProperties());
        RegistryModelUtilities.unlinkPlugins(REGISTRY.getStrings());
        RegistryModelUtilities.unlinkPlugins(REGISTRY.getExceptions());
        RegistryModelUtilities.unlinkPlugins(REGISTRY.getQueries());

        this.userInterface = null;
        this.frameworkManager = null;
        this.menuBar = new JMenuBar();
        this.toolBar = new JToolBar();
        this.toolBar.setMinimumSize(DIM_TOOLBAR_SIZE);
        this.toolBar.setPreferredSize(DIM_TOOLBAR_SIZE);
        this.toolBar.setMaximumSize(DIM_TOOLBAR_SIZE);
        this.vecHelpActions = new Vector<ContextAction>(10);
        this.stackRunning = new Stack<RootPlugin>();

        if ((atom != null)
            && (atom.getHostTreeNode() != null))
            {
            this.pluginFramework = atom;
            this.nodeModel = atom.getHostTreeNode();
            this.nodeModel.removeAllChildren();
            atom.addMainExpanders();
            }
        else
            {
            LOGGER.error(EXCEPTION_PARAMETER_NULL);
            this.nodeModel = new DefaultMutableTreeNode();
            }

        // Register the Atom's root node!
        this.longSize = 1;

        this.boolUpdated = false;
        }


    /***********************************************************************************************
     * Get the RootNode for the TreeModel.
     *
     * @return DefaultMutableTreeNode
     */

    public final DefaultMutableTreeNode getRootNode()
        {
        return(this.nodeModel);
        }


    /***********************************************************************************************
     * Get the Framework.
     *
     * @return FrameworkPlugin
     */

    public final FrameworkPlugin getFramework()
        {
        // Only cast when we need to...
        return((FrameworkPlugin)this.pluginFramework);
        }


    /***********************************************************************************************
     * Get the Stack of running RootPlugins.
     *
     * @return Stack<RootPlugin>
     */

    public Stack<RootPlugin> getRunners()
        {
        return (this.stackRunning);
        }


    /***********************************************************************************************
     * Add a running Plugin or Task.
     *
     * @param runner
     */

    public void addRunner(final RootPlugin runner)
        {
        if ((runner != null)
            && (getRunners() != null)
            && (!getRunners().contains(runner)))
            {
            getRunners().push(runner);
            }
        }


    /***********************************************************************************************
     * Remove a running Plugin or Task, but only if it is not running!.
     *
     * @param runner
     */

    public void removeRunner(final RootPlugin runner)
        {
        if ((getRunners() != null)
            && (!getRunners().isEmpty())
            && (getRunners().contains(runner)))
            {
            // ToDo test running state
            getRunners().remove(runner);
            }
        }


    /**********************************************************************************************/
    /* Navigation and Help                                                                        */
    /***********************************************************************************************
     * Rebuild the navigation Menu and Toolbar for the current User.
     * Add the Framework static and dynamic ContextActionGroups,
     * then all Atom ContextActionGroups in their SortOrder,
     * and finally append the specified ContextActionGroups (probably from the current task).
     * The last entry on the Menu only is the set of Help Actions.
     * Actions appear on the Menu and/or Toolbar as specified in their parameters.
     *
     * <code>FrameworkStatic FrameworkDynamic Atom0 Atom1 ... CurrentTask Help</code>
     *
     * @param userobject
     * @param uicomponent
     */

    public final void rebuildNavigation(final UserObjectPlugin userobject,
                                        final UIComponentPlugin uicomponent)
        {
        FrameworkManagerUIComponentPlugin managerUI;

        LOGGER.debugNavigation("REBUILD NAVIGATION-----------------------------------------------");

        // WARNING! We must make sure that nothing happens if this is called during Login,
        // before the MenuBar and ToolBar have been created!
        if ((getMenuBar() != null)
            && (getToolBar() != null))
            {
            managerUI = null;

            // See if we have a FrameworkManager (or not)
            if ((getFrameworkManager() != null)
                && (getFrameworkManager().getUI() != null))
                {
                managerUI = getFrameworkManager().getUI();
                }

            // Start again with an empty MenuBar
            getMenuBar().removeAll();
            getMenuBar().setToolTipText(TOOLTIP_LOOK_AND_FEEL + SPACE + UIManager.getLookAndFeel().getName());

            // Start again with an empty Toolbar
            getToolBar().removeAll();

            // Assemble the Framework static and dynamic (instance) ContextActions
            // We may or may not have a FrameworkManager UI at this point...
            NavigationUtilities.buildFrameworkActions(getLoggedInUser(),
                                                      getFramework(),
                                                      managerUI,
                                                      getMenuBar(),
                                                      getToolBar());

            // Assemble the actions for all Atoms which are children of the Framework
            NavigationUtilities.buildAllPluginActions(getLoggedInUser(),
                                                      getFramework(),
                                                      managerUI,
                                                      getMenuBar(),
                                                      getToolBar());

            // Assemble the menu for the current UserObject and UIComponent
            // We assume that the User can see these, otherwise the UIComponent would not be visible!
            // Assume that we don't repeat any Plugin Actions, since this would be confusing?!
            NavigationUtilities.buildContextActions(userobject,
                                                    uicomponent,
                                                    getMenuBar(),
                                                    getToolBar());

            // Assemble the Help Menu
            // All Users can always see the Help!
            NavigationUtilities.rebuildHelp(getHelpActions(),
                                            getMenuBar(),
                                            getToolBar());

            // We have finished rebuilding the navigation, so now tell the world...

            // The UserInterface is responsible for visualising the new MenuBar
            if ((getUserInterface() != null)
                && (getUserInterface().getUI() != null))
                {
                // This is the only place where the MenuBar is set on the UI
                getUserInterface().getUI().setJMenuBar(getMenuBar());

                getMenuBar().revalidate();

                getUserInterface().getUI().validate();
                }

            // The FrameworkManager is responsible for visualising the ToolBar
            if ((getFrameworkManager() != null)
                && (getFrameworkManager().getUI() != null))
                {
                // The FrameworkManager is responsible for adding the ToolBar to the layout
                // in setUIOccupant() --> assembleUIComponents() etc.
                // so we only need to redraw the Toolbar here...
                getToolBar().revalidate();

                getFrameworkManager().getUI().validate();
                }
            }
        else
            {
            LOGGER.debugNavigation("MenuBar and ToolBar not available");
            }

        LOGGER.debugNavigation("END OF REBUILD NAVIGATION-----------------------------------------");
        }


    /***********************************************************************************************
     * Get the UserInterface MenuBar.
     *
     * @return JMenuBar
     */

    public JMenuBar getMenuBar()
        {
        return (this.menuBar);
        }


    /***********************************************************************************************
     * Get the FrameworkManager ToolBar.
     *
     * @return JToolBar
     */

    public JToolBar getToolBar()
        {
        return (this.toolBar);
        }


    /***********************************************************************************************
     * Add an Action to the Help menu (or toolbar).
     *
     * @param action
     */

    public final void addHelpAction(final ContextAction action)
        {
        if ((action != null)
            && (getHelpActions() != null))
            {
            getHelpActions().add(action);
            }
        else
            {
            throw new FrameworkException(EXCEPTION_INVALID_REGISTRY_MODEL);
            }
        }


    /***********************************************************************************************
     * Remove an Action from the Help menu (or toolbar).
     *
     * @param action
     */

    public final void removeHelpAction(final ContextAction action)
        {
        if ((action != null)
            && (getHelpActions() != null))
            {
            getHelpActions().remove(action);
            }
        else
            {
            throw new FrameworkException(EXCEPTION_INVALID_REGISTRY_MODEL);
            }
        }


    /***********************************************************************************************
     * Clear the Help Actions.
     */

    public final void clearHelpActions()
        {
        getHelpActions().clear();
        }


    /***********************************************************************************************
     * Get the Help ContextActions.
     *
     * @return Vector<ContextAction>
     */

    private Vector<ContextAction> getHelpActions()
        {
        return (this.vecHelpActions);
        }


    /**********************************************************************************************/
    /* User Interface                                                                             */
    /***********************************************************************************************
     *
     * @return UserInterface
     */

    public UserInterfacePlugin getUserInterface()
        {
        return (this.userInterface);
        }


    /***********************************************************************************************
     *
     * @param plugin
     */

    public void setUserInterface(final UserInterfacePlugin plugin)
        {
        this.userInterface = plugin;
        }


    /***********************************************************************************************
     *
     * @return FrameworkManagerPlugin
     */

    public FrameworkManagerPlugin getFrameworkManager()
        {
        return (this.frameworkManager);
        }


    /***********************************************************************************************
     *
     * @param plugin
     */

    public void setFrameworkManager(final FrameworkManagerPlugin plugin)
        {
        this.frameworkManager = plugin;
        }


    /***********************************************************************************************
     * todo remove!!!
     * @return FrameworkManagerUIComponentPlugin
     */

    public FrameworkManagerUIComponentPlugin getFrameworkManagerUI()
        {
        if ((getFrameworkManager() == null)
            || (getFrameworkManager().getUI() == null))
            {
            return (null);
            }
        else
            {
            return (getFrameworkManager().getUI());
            }
        }


    /**********************************************************************************************/
    /* Users                                                                                      */
    /***********************************************************************************************
     * Get the Framework logged-in User.
     *
     * @return UserData
     */

    public UserPlugin getLoggedInUser()
        {
        return (this.userPlugin);
        }


    /***********************************************************************************************
     * Set the Framework logged-in User.
     *
     * @param user
     */

    public void setLoggedInUser(final UserPlugin user)
        {
        userPlugin = user;
        }


    /**********************************************************************************************/
    /* Miscellaneous                                                                              */
    /***********************************************************************************************
     * Get the Updated flag.
     *
     * @return boolean
     */

    public final boolean getUpdated()
        {
        return this.boolUpdated;
        }


    /***********************************************************************************************
     * Set the Updated flag.
     *
     * @param flag
     */

    public final void setUpdated(final boolean flag)
        {
        this.boolUpdated = flag;
        }


    /***********************************************************************************************
     * Get the SQL Trace flag.
     *
     * @return boolean
     */

    public final boolean getSqlTrace()
        {
        return (LOADER_PROPERTIES.isSqlTrace());
        }


    /***********************************************************************************************
     * Set the SQL Trace flag.
     *
     * @param flag
     */

    public final void setSqlTrace(final boolean flag)
        {
        LOADER_PROPERTIES.setSqlTrace(flag);
        }


    /***********************************************************************************************
     * Get the SQL Timing flag.
     *
     * @return boolean
     */

    public final boolean getSqlTiming()
        {
        return (LOADER_PROPERTIES.isSqlTiming());
        }


    /***********************************************************************************************
     * Set the SQL Timing flag.
     *
     * @param flag
     */

    public final void setSqlTiming(final boolean flag)
        {
        LOADER_PROPERTIES.setSqlTiming(flag);
        }


    /***********************************************************************************************
     * Get the Debug Mode flag.
     *
     * @return boolean
     */

    public final boolean getDebugMode()
        {
        return (LOADER_PROPERTIES.isMasterDebug());
        }


    /***********************************************************************************************
     * Set the Debug Mode flag.
     *
     * @param flag
     */

    public final void setDebugMode(final boolean flag)
        {
        LOADER_PROPERTIES.setMasterDebug(flag);
        }


    /***********************************************************************************************
     * Get the number of Beans attached to the RegistryModel.
     *
     * @return long
     */

    public long size()
        {
        return(this.longSize);
        }


    /***********************************************************************************************
     * Increment the size of the RegistryModel.
     */

    public void incSize()
        {
        if ((size() + 1) <= REGISTRY.size())
            {
            this.longSize++;
            }
        else
            {
            throw new FrameworkException(EXCEPTION_INVALID_REGISTRY_MODEL);
            }
        }


    /***********************************************************************************************
     * Add the ActiveChangeListener as required.
     *
     * @param listener
     */

    public void addActiveChangeListener(final ActiveChangeListener listener)
        {
//        AtomPlugin applicationData;
//        TaskPlugin applicationTask;
//        AtomPlugin componentData;
//        TaskPlugin componentTask;
//        Enumeration enumeration;

        // Enumerate the entries in each hashtable

//        for (enumeration = hashtableApplications.elements();
//             enumeration.hasMoreElements();)
//            {
//            applicationData = (ApplicationData) enumeration.nextElement();
//            applicationData.addActiveChangeListener(listener);
//            }
//
//        for (enumeration = hashtableApplicationTasks.elements();
//             enumeration.hasMoreElements();)
//            {
//            applicationTask = (ApplicationTask) enumeration.nextElement();
//            applicationTask.addActiveChangeListener(listener);
//            }
//
//        for (enumeration = hashtableComponents.elements();
//             enumeration.hasMoreElements();)
//            {
//            componentData = (ComponentData) enumeration.nextElement();
//            componentData.addActiveChangeListener(listener);
//            }
//
//        for (enumeration = hashtableComponentTasks.elements();
//             enumeration.hasMoreElements();)
//            {
//            componentTask = (ComponentTask) enumeration.nextElement();
//            componentTask.addActiveChangeListener(listener);
//            }
        }
    }


//----------------------------------------------------------------------------------------------------------------------
// End of File