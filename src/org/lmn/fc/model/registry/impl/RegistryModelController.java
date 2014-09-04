package org.lmn.fc.model.registry.impl;

import org.lmn.fc.common.comparators.RootPluginBySortOrder;
import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.exceptions.FrameworkException;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.logging.Logger;
import org.lmn.fc.model.plugins.AtomPlugin;
import org.lmn.fc.model.plugins.FrameworkPlugin;
import org.lmn.fc.model.plugins.PluginState;
import org.lmn.fc.model.registry.NavigationUtilities;
import org.lmn.fc.model.registry.RegistryModelControllerInterface;
import org.lmn.fc.model.registry.RegistryModelUtilities;
import org.lmn.fc.model.root.RootPlugin;
import org.lmn.fc.model.tasks.FrameworkManagerPlugin;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.model.tasks.TaskState;
import org.lmn.fc.ui.components.BlankUIComponent;
import org.lmn.fc.ui.login.LoginDialog;
import org.lmn.fc.ui.login.LoginPanel;
import org.lmn.fc.ui.login.LoginTab;
import org.lmn.fc.ui.login.ShutdownDialog;

import javax.swing.*;
import java.util.*;


/***************************************************************************************************
 * The RegistryModelController.
 */

public final class RegistryModelController implements RegistryModelControllerInterface,
                                                      FrameworkSingletons
    {
    // String Resources
    private static final String WARNING_EXCEPTION_START = "****Exceptions following this point are caused by faults inside Java!";
    private static final String WARNING_EXCEPTION_FINISH = "****Normal exception handling service restored!\n\n";

    private static final Logger LOGGER = Logger.getInstance();

    private volatile static RegistryModelControllerInterface CONTROLLER_INSTANCE;



    /***********************************************************************************************
     * The RegistryModelController is a Singleton!
     *
     * @return RegistryModelController
     */

    public static RegistryModelControllerInterface getInstance()
        {
        if (CONTROLLER_INSTANCE == null)
            {
            synchronized (RegistryModelController.class)
                {
                if (CONTROLLER_INSTANCE == null)
                    {
                    CONTROLLER_INSTANCE = new RegistryModelController();
                    }
                }
            }

        return (CONTROLLER_INSTANCE);
        }


    // The LoginDialog used to login to the RegistryModel bean pool
    private LoginDialog loginDialog;


    /***********************************************************************************************
     * Privately construct the RegistryModelController.
     */

    private RegistryModelController()
        {
        loginDialog = null;
        }


    /**********************************************************************************************/
    /* Framework, Plugin and Task Management                                                      */
    /***********************************************************************************************
     * Login to the specified Framework.
     *
     * @param framework
     */

    public void loginFramework(final FrameworkPlugin framework)
        {
        // Wake the user up again...
        showLoginDialog(framework);

        // This Thread will resume when the User has been validated, so...
        // if we get here, there should be a valid UserData waiting in the RegistryModel!
        // Now that the beans are fully loaded, assemble the nodes of the RegistryModel,
        // using the logged-in UserRole to filter access

        if ((RegistryModelUtilities.isValidUser(REGISTRY_MODEL.getLoggedInUser()))
            && (REGISTRY_MANAGER.assembleBeansForUser(REGISTRY_MODEL.getLoggedInUser())))
            {
            // If we have a good model, start the Framework
            // Start all Plugins, and run their Tasks marked 'Run At Start'
            startFramework(framework);
            }
        else
            {
            throw new FrameworkException(EXCEPTION_START_FRAMEWORK);
            }
        }


    /***********************************************************************************************
     * Logout of the specified Framework.
     *
     * @param framework
     */

    public void logoutFramework(final FrameworkPlugin framework)
        {
        final ShutdownDialog dialogShutdown;

        if ((framework != null)
            && (framework.validatePlugin()))
            {
            // Create a non-modal ShutdownDialog
            // This does not affect the current Thread
            dialogShutdown = new ShutdownDialog(framework.getName() + SPACE + DIALOG_SHUTDOWN,
                                                MSG_SHUTDOWN);
            dialogShutdown.toFront();
            dialogShutdown.setVisible(true);

            // Log the Logout
            LOGGER.logAtomEvent(framework,
                                framework.getRootTask(),
                                framework.getClass().getName(),
                                METADATA_FRAMEWORK_LOGOUT
                                    + METADATA_NAME + framework.getName() + TERMINATOR + SPACE
                                    + METADATA_USER + REGISTRY_MODEL.getLoggedInUser().getName() + TERMINATOR + SPACE
                                    + METADATA_ROLE + REGISTRY_MODEL.getLoggedInUser().getRole().getName() + TERMINATOR,
                                EventStatus.INFO);

            // Shut down the Framework, but don't exit!
            // See similar code in UserInterfaceFrame & LoginTab, WindowClosingEvent.
            stopFramework(framework);

            dialogShutdown.setVisible(false);
            dialogShutdown.dispose();
            }
        else
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_INVALID);
            }
        }


    /***********************************************************************************************
     * Start up the Framework, all child Plugins and their Tasks marked as <code>RunAtStart</code>.
     *
     * @param framework
     */

    private void startFramework(final FrameworkPlugin framework)
        {
        // We can't use startPluginAndShowUI() here, because there is no FrameworkManager yet...
        // ...so this just sets the State, but not the UI
        if (setPluginState(framework, PluginState.RUNNING))
            {
            // Tell the world that this Framework has started!
            // The Framework is only ever started here, and so we can do the logging here
            // rather than in startupAtom(), which must be completed fully in subclasses

            // Run all Framework Tasks
            // Remember that this does *not* call runUI() for the Task
            runAllTasks(framework.getTasks());

            // Now recursively start all Framework Plugins and their Tasks,
            // obeying their SortOrder
            // Remember that this does *not* call runUI() for the Plugin
            startAllChildPlugins(framework);

            // Log the Framework Login!
            LOGGER.logAtomEvent(framework,
                                framework.getRootTask(),
                                framework.getClass().getName(),
                                METADATA_FRAMEWORK_LOGIN
                                    + METADATA_NAME + framework.getName() + TERMINATOR + SPACE
                                    + METADATA_USER + REGISTRY_MODEL.getLoggedInUser().getName() + TERMINATOR + SPACE
                                    + METADATA_ROLE + REGISTRY_MODEL.getLoggedInUser().getRole().getName() + TERMINATOR,
                                EventStatus.INFO);

            // Welcome the user...
            LOGGER.login(MSG_WELCOME + SPACE + framework.getName());

            // Show a simple UI for debugging
            RegistryModelUtilities.debugModel(REGISTRY_MODEL, framework.getDebugMode());

            // Show the UserInterface and FrameworkManager, if possible
            // Remember that this must explicitly call runUI() for each Task
            LOGGER.login(WARNING_EXCEPTION_START);
            NavigationUtilities.showFrameworkUI(REGISTRY_MODEL.getUserInterface(),
                                                REGISTRY_MODEL.getFrameworkManager());
            LOGGER.login(WARNING_EXCEPTION_FINISH);
            }
        else
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_INVALID);
            }
        }


    /***********************************************************************************************
     * Stop the Framework, all child Plugins and their Tasks.
     *
     * @param framework
     */

    public void stopFramework(final FrameworkPlugin framework)
        {
        final Stack<RootPlugin> stack;
        final Enumeration<RootPlugin> enumAtoms;
        final Enumeration<RootPlugin> enumTasks;

        LOGGER.logTimedEvent("[target=all] [action=shutdown]");

        //------------------------------------------------------------------------------------------
        // TODO REVIEW L&F RESET
        // Reset the Look and Feel setting
//        try
//            {
//            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//            NavigationUtilities.updateComponentTreeUI((Component)REGISTRY_MODEL.getUserInterface().getUI());
//
//            // There may not be a FrameworkManager installed, so check first
//            if ((REGISTRY_MODEL.getFrameworkManagerUI() != null))
//                {
//                REGISTRY_MODEL.getFrameworkManagerUI().validateAndUpdateUI();
//                }
//
//            // Save the change in the RegistryModel
//            REGISTRY.setStringProperty(REGISTRY_MODEL.getUserInterface().getResourceKey() + ResourceKeys.KEY_LOOK_AND_FEEL,
//                                       UIManager.getSystemLookAndFeelClassName());
//            }
//
//        catch (NullPointerException e)
//            {
//            LOGGER.error("NullPointerException while trying to reapply System LookAndFeel");
//            }
//
//        catch (ClassNotFoundException e)
//            {
//            LOGGER.error("ClassNotFoundException while trying to reapply System LookAndFeel");
//            }
//
//        catch (InstantiationException e)
//            {
//            LOGGER.error("InstantiationException while trying to reapply System LookAndFeel");
//            }
//
//        catch (IllegalAccessException e)
//            {
//            LOGGER.error("IllegalAccessException while trying to reapply System LookAndFeel");
//            }
//
//        catch (UnsupportedLookAndFeelException e)
//            {
//            LOGGER.error("UnsupportedLookAndFeelException while trying to reapply System LookAndFeel");
//            }

        //------------------------------------------------------------------------------------------
        // Write any pending Registry updates to the database

        // We *must* wait for this to finish, so don't use another Thread
        REGISTRY_MANAGER.updateRegistry();

        //------------------------------------------------------------------------------------------
        // The RegistryModel holds everything from the Framework downwards
        // Stop all items currently registered as Running
        // Pop the stack so the most recently started item is stopped first
        // This transition should be possible, since we are trying READY --> STOPPED/SHUTDOWN

        // Retrieve the list of currently running items
        stack = REGISTRY_MODEL.getRunners();

        while ((stack != null)
            && (!stack.isEmpty()))
            {
            final RootPlugin plugin = stack.pop();

            if (plugin instanceof TaskPlugin)
                {
                if (((TaskPlugin)plugin).isRunning()
                    && (((TaskPlugin)plugin).getParentAtom() != null))
                    {
                    // Logging is done by the Task controller
                    setTaskState(((TaskPlugin)plugin).getParentAtom(),
                                  (TaskPlugin)plugin,
                                  TaskState.SHUTDOWN);
                    }
                else
                    {
                    LOGGER.error("The Registry Model list of runners should not contain Task [name="
                                 + plugin.getPathname() + "]");
                    }
                }
            else if (plugin instanceof AtomPlugin)
                {
                if (((AtomPlugin)plugin).isRunning())
                    {
                    // Logging is done by the Plugin controller
                    setPluginState(((AtomPlugin)plugin),
                                   PluginState.STOPPED);
                    }
                else
                    {
                    LOGGER.error("The Registry Model list of runners should not contain Plugin [name="
                                 + plugin.getPathname() + "]");
                    }

                }
            else
                {
                // Something went wrong - not a stoppable!
                LOGGER.error("Registry Model Runners had an invalid entry [name="
                             + plugin.getPathname() + "]");
                }
            }

        //------------------------------------------------------------------------------------------
        // Now we must set ALL Atoms and Tasks to SHUTDOWN, regardless of their previous state,
        // because they must be re-initialised if the User logs back in.
        // The only valid transition is from SHUTDOWN to INITIALISED
        // It does not matter about the order now, because no Atoms or Tasks are actually running

        // Shutdown all Tasks
        enumTasks = REGISTRY.getTasks().elements();

        while ((enumTasks != null)
            && (enumTasks.hasMoreElements()))
            {
            final TaskPlugin pluginTask;

            pluginTask = (TaskPlugin)enumTasks.nextElement();

            // Any Tasks currently SHUTDOWN will not be affected
            if (!TaskState.SHUTDOWN.equals(pluginTask.getState()))
                {
                // Logging is done by the Task controller
                setTaskState(pluginTask.getParentAtom(),
                             pluginTask,
                             TaskState.SHUTDOWN);
                }
            }

        // Now stop all the Atoms
        enumAtoms = REGISTRY.getAtoms().elements();

        while ((enumAtoms != null)
            && (enumAtoms.hasMoreElements()))
            {
            final AtomPlugin pluginAtom;

            pluginAtom = (AtomPlugin)enumAtoms.nextElement();

            // Any Plugins currently STOPPED will not be affected
            if (!PluginState.STOPPED.equals(pluginAtom.getState()))
                {
                // Logging is done by the Plugin controller
                setPluginState(pluginAtom,
                               PluginState.STOPPED);
                }
            }

        // Re-initialise the RegistryModel, ready for the next Login
        REGISTRY_MODEL.initialiseModel(framework);
        }


    /***********************************************************************************************
     * Exit the Framework back to the Operating System.
     * Ideally call stopFramework() first!
     *
     * @param framework
     */

    public void exitFramework(final FrameworkPlugin framework)
        {
        final Enumeration<RootPlugin> enumAtoms;

        // Unregister the plugins from the MBean server, if possible
        enumAtoms = REGISTRY.getAtoms().elements();

        while (enumAtoms.hasMoreElements())
            {
            final AtomPlugin pluginAtom;

            pluginAtom = (AtomPlugin)enumAtoms.nextElement();
            framework.unregisterAtom(pluginAtom);
            }

        // Write any pending Registry updates to the database
        // We *must* wait for this to finish, so don't use another Thread
        // This should have been done in stopFramework()
        REGISTRY_MANAGER.updateRegistry();

        // Close the Framework database
        // Any Atom Databases will have been closed by stopFramework()
        DATABASE.closeConnection();

        // Return to the Operating System with a 'no error' status
        System.exit(0);
        }


    /**********************************************************************************************/
    /* Plugin Management                                                                          */
    /***********************************************************************************************
     * Recursively start up all Plugins which are children of the specified AtomPlugin.
     *
     * @param host
     */

    public void startAllChildPlugins(final AtomPlugin host)
        {
        final Vector<RootPlugin> atoms;
        final Iterator<RootPlugin> iterAtoms;
        final List<RootPlugin> childPlugins;
        final Iterator<RootPlugin> iterChosenPlugins;

        if ((host == null)
            || (host.getAtoms() == null))
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_INVALID);
            }

        atoms = host.getAtoms();
        iterAtoms = atoms.iterator();
        childPlugins = new Vector<RootPlugin>(host.getAtoms().size());

        // Copy the Plugins to a new List so as to
        // preserve the order stored in the host after they are sorted
        while (iterAtoms.hasNext())
            {
            final AtomPlugin atomchild = (AtomPlugin) iterAtoms.next();

            childPlugins.add(atomchild);
            }

        // Sort the child Plugins by SortOrder
        Collections.sort(childPlugins, new RootPluginBySortOrder());

        // Iterate over the sorted list, and start up the Plugins
        iterChosenPlugins = childPlugins.iterator();

        while (iterChosenPlugins.hasNext())
            {
            final AtomPlugin chosenPlugin = (AtomPlugin)iterChosenPlugins.next();

            // The host's Tasks are now running, and one of those hosts will be the Framework,
            // so there should be a FrameworkManager in the Registry,
            // so we can use startPluginAndShowUI()
//            Todo if (startPluginAndShowUI(chosenPlugin, REGISTRY_MODEL.getFrameworkManager()))
            if (setPluginState(chosenPlugin, PluginState.RUNNING))
                {
                // Tell the world that this Plugin has started!
                // Plugins are only ever started here, and so we can do the logging etc. here
                // rather than in startupAtom(), which must be completed fully in subclasses

                // Start all child Tasks if they are marked as 'RunAtStart'
                // The UI will only be shown if required by the Task's run() method,
                // e.g. the UserInterface and FrameworkManager UIs
                runAllTasks(chosenPlugin.getTasks());

                // Recursively traverse into each Plugin's children
                startAllChildPlugins(chosenPlugin);
                }
            else
                {
                LOGGER.error("Unable to start Plugin [name=" + chosenPlugin.getPathname() + "]");
                }
            }
        }


    /***********************************************************************************************
     * Run a specified Plugin and show its UI.
     * Set the UI Component of the specified Plugin in the FrameworkManager, if possible.
     * If the FrameworkManager does not exist, show a warning box.
     * Called only by actionPerformed().
     *
     * @param plugin
     * @param manager
     */

    public boolean startPluginAndShowUI(final AtomPlugin plugin,
                                        final FrameworkManagerPlugin manager)
        {
        boolean boolSuccess;

        LOGGER.debugNavigation("RegistryModelController.startPluginAndShowUI() START");

        boolSuccess = false;

        if ((plugin != null)
            && (plugin.validatePlugin()))
            {
            // Do we have a good FrameworkManager?
            if ((manager != null)
                && (manager.validatePlugin())
                && (manager.isRunning())
                && (manager.getUI() != null))
                {
                // Is the Plugin running already?
                if (plugin.isRunning())
                    {
                    // Just show the UI panel that already exists
                    NavigationUtilities.installUserObjectUI(plugin, manager);
                    boolSuccess = true;
                    }
                else
                    {
                    // It isn't Running yet, so try to move to READY
                    if (setPluginState(plugin, PluginState.RUNNING))
                        {
                        // It is now Running, so show the UI Component
                        NavigationUtilities.installUserObjectUI(plugin, manager);
                        boolSuccess = true;
                        }
                    else
                        {
                        // Unable to move into the Running state - this is really an error
                        // The Plugin was parked as STOPPED; overwrite the UI with a blank panel
                        plugin.setUIComponent(new BlankUIComponent());
                        NavigationUtilities.installUserObjectUI(plugin, manager);
                        }
                    }
                }
            else
                {
                NavigationUtilities.unableToPerformAction();
                }
            }
        else
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_INVALID);
            }

        LOGGER.debugNavigation("RegistryModelController.startPluginAndShowUI() END");

        return (boolSuccess);
        }


    /***********************************************************************************************
     * Set the state of the specified AtomPlugin.
     * The UI will only be shown if required by the Plugin's startupAtom() method.
     * Called in this class and actionHalted().
     * This is a simple state machine...
     *
     * @param plugin
     * @param newstate
     */

    public boolean setPluginState(final AtomPlugin plugin,
                                  final PluginState newstate)
        {
        final boolean boolSuccess;

        if ((plugin != null)
            && (plugin.validatePlugin())
            && (newstate != null))
            {
            // We can't do anything if the chain is not complete
            if (isPluginActivationChainOk(plugin))
                {
                final boolean boolValidTransition;

                boolValidTransition = ((PluginState.STOPPED.equals(plugin.getState()) && (PluginState.RUNNING.equals(newstate)))
                                      || (PluginState.RUNNING.equals(plugin.getState()) && (PluginState.STOPPED.equals(newstate))));

                if (boolValidTransition)
                    {
                    switch (newstate)
                        {
                        case STOPPED:
                            {
                            // ToDo shutdown cleanly, stop all children and tasks???
                            // Mark this Plugin as Locked, i.e. in use
                            plugin.setLocked(true);

                            // Try to stop the Plugin
                            if (plugin.shutdownAtom())
                                {
                                // Set the Stop, and log the event...
                                plugin.setStopDate(new GregorianCalendar());
                                plugin.setStopTime(new GregorianCalendar());

                                // Make sure that the plugin is not in the Runners list
                                REGISTRY_MODEL.removeRunner(plugin);

                                LOGGER.logAtomEvent(plugin,
                                                    plugin.getRootTask(),
                                                    plugin.getClass().getName(),
                                                    METADATA_PLUGIN_SHUTDOWN
                                                        + SPACE
                                                        + METADATA_NAME
                                                        + plugin.getPathname()
                                                        + TERMINATOR,
                                                    EventStatus.INFO);
                                // Set the new state
                                boolSuccess = plugin.setState(newstate);
                                }
                            else
                                {
                                // Log all failed attempts (for now)
                                plugin.setStopDate(new GregorianCalendar());
                                plugin.setStopTime(new GregorianCalendar());

                                // Make sure that the plugin is no longer in the Runners list
                                REGISTRY_MODEL.removeRunner(plugin);

                                // Log all failed attempts (for now)
                                LOGGER.logAtomEvent(plugin,
                                                    plugin.getRootTask(),
                                                    plugin.getClass().getName(),
                                                    METADATA_PLUGIN_SHUTDOWN_FAIL
                                                        + SPACE
                                                        + METADATA_NAME
                                                        + plugin.getPathname()
                                                        + TERMINATOR,
                                                    EventStatus.INFO);

                                // Park an unsuccessful Plugin in STOPPED
                                boolSuccess = plugin.setState(PluginState.STOPPED);
                                }

                            // The Plugin is free again
                            plugin.setLocked(false);
                            break;
                            }

                        case RUNNING:
                            {
                            // ToDo start all child plugins and tasks???

                            // Mark this Plugin as Locked, i.e. in use
                            plugin.setLocked(true);

                            if (plugin.startupAtom())
                                {
                                // If we moved successfully to Running,
                                // set the Start and log the event...
                                plugin.setStartDate(new GregorianCalendar());
                                plugin.setStartTime(new GregorianCalendar());

                                // Remove any previous Stop
                                plugin.setStopDate(null);
                                plugin.setStopTime(null);

                                // Set the new state
                                boolSuccess = plugin.setState(newstate);

                                // Record the running item
                                REGISTRY_MODEL.addRunner(plugin);

                                LOGGER.logAtomEvent(plugin,
                                                    plugin.getRootTask(),
                                                    plugin.getClass().getName(),
                                                    METADATA_PLUGIN_START
                                                        + SPACE
                                                        + METADATA_NAME
                                                        + plugin.getPathname()
                                                        + TERMINATOR,
                                                    EventStatus.INFO);
                                }
                            else
                                {
                                // Log all failed attempts (for now)
                                plugin.setStopDate(new GregorianCalendar());
                                plugin.setStopTime(new GregorianCalendar());

                                // Make sure that the plugin is no longer in the Runners list
                                REGISTRY_MODEL.removeRunner(plugin);

                                // Log all failed attempts (for now)
                                LOGGER.logAtomEvent(plugin,
                                                    plugin.getRootTask(),
                                                    plugin.getClass().getName(),
                                                    METADATA_PLUGIN_START_FAIL
                                                        + SPACE
                                                        + METADATA_NAME
                                                        + plugin.getPathname()
                                                        + TERMINATOR,
                                                    EventStatus.INFO);

                                // Park an unsuccessful Plugin in STOPPED
                                boolSuccess = plugin.setState(PluginState.STOPPED);
                                }

                            // The Plugin is free again
                            plugin.setLocked(false);
                            break;
                            }

                        default:
                            {
                            // Keep the compiler happy - this should never happen!
                            throw new FrameworkException(EXCEPTION_PARAMETER_INVALID);
                            }
                        }
                    }
                else
                    {
                    // It was a request for an invalid transition
                    // which is likely to be a configuration error...

                    // Log all failed attempts (for now)
                    plugin.setStartDate(null);
                    plugin.setStartTime(null);
                    plugin.setStopDate(null);
                    plugin.setStopTime(null);

                    // Make sure that the plugin is not in the Runners list
                    REGISTRY_MODEL.removeRunner(plugin);

                    LOGGER.logAtomEvent(plugin,
                                        plugin.getRootTask(),
                                        plugin.getClass().getName(),
                                        METADATA_PLUGIN_STATE_ERROR
                                            + SPACE
                                            + METADATA_NAME
                                            + plugin.getPathname()
                                            + TERMINATOR,
                                        EventStatus.INFO);

                    // Force the STOPPED state
                    plugin.setState(PluginState.STOPPED);

                    // ...and force a failed outcome
                    boolSuccess = false;
                    }
                }
            else
                {
                // We can't change the Plugin state at the moment
                // because the activation chain is not complete
                final StringBuffer buffer = new StringBuffer(METADATA_PLUGIN_STATE_ERROR
                                                               + SPACE
                                                               + METADATA_NAME
                                                               + plugin.getPathname()
                                                               + TERMINATOR
                                                               + SPACE);
                if (!plugin.isActive())
                    {
                    buffer.append(METADATA_INACTIVE);
                    }

                if (plugin.isLocked())
                    {
                    buffer.append(METADATA_LOCKED);
                    }

                // Tell the user, and record the problem
                LOGGER.logAtomEvent(plugin,
                                    plugin.getRootTask(),
                                    plugin.getClass().getName(),
                                    buffer.toString(),
                                    EventStatus.INFO);
                boolSuccess = false;
                }

            // See if any of the above plugin.setInstrumentState() failed
            if (!boolSuccess)
                {
                // TODO review start & stop times in plugins & tasks for all states...
                plugin.setStartDate(null);
                plugin.setStartTime(null);
                plugin.setStopDate(null);
                plugin.setStopTime(null);

                // Make doubly sure that the plugin is not in the Runners list
                REGISTRY_MODEL.removeRunner(plugin);

                // Log all failed attempts (for now)
                LOGGER.logAtomEvent(plugin,
                                    plugin.getRootTask(),
                                    plugin.getClass().getName(),
                                    METADATA_PLUGIN_STATE_ERROR
                                        + SPACE
                                        + METADATA_NAME
                                        + plugin.getPathname()
                                        + TERMINATOR,
                                    EventStatus.INFO);

                // Try to park an unsuccessful Plugin in STOPPED,
                // but preserve the original boolSuccess (which failed)
                plugin.setState(PluginState.STOPPED);
                }
            }
        else
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_INVALID);
            }

        return (boolSuccess);
        }


    /***********************************************************************************************
     * Is the chain of Plugin activation controls (active, locked) all Ok back to the Framework root?
     *
     * @param plugin
     *
     * @return boolean
     */

    public boolean isPluginActivationChainOk(final AtomPlugin plugin)
        {
        AtomPlugin atom;
        boolean boolFoundRoot;
        boolean boolChainOk;

        atom = plugin;
        boolFoundRoot = false;
        boolChainOk = (atom != null) && (atom.validatePlugin());

        while ((!boolFoundRoot)
            && (atom != null)
            && (atom.validatePlugin()))
            {
            if ((!atom.isActive())
                ||(atom.isLocked()))
                {
                boolChainOk = false;
                }

            if (atom.getParentAtom() == null)
                {
                boolFoundRoot = true;
                }
            else
                {
                atom = (AtomPlugin)atom.getParentAtom();
                }
            }

        return (boolChainOk);
        }


    /***********************************************************************************************
     * Show a message that something went wrong when trying to control a Plugin.
     *
     * @param plugin
     */

    public void unableToControlPlugin(final AtomPlugin plugin)
        {
        if (plugin != null)
            {
            final String [] strMessage =
                {
                "It has not been possible to start up or control the " + plugin.getName(),
                "Try to identify the problem from the EventLog",
                "You may need to logout and login before trying again"
                };

            JOptionPane.showMessageDialog(null,
                                          strMessage,
                                          plugin.getName() + " Plugin Controller",
                                          JOptionPane.WARNING_MESSAGE);
            }
        }


    /**********************************************************************************************/
    /* Task Management                                                                            */
    /***********************************************************************************************
     * Try to run all Tasks marked as Runnable and RunAtStart in the list specified.
     * The UI will only be shown if required by the Task's run() method.
     *
     * @param tasks
     */

    public final void runAllTasks(final Vector<RootPlugin> tasks)
        {
        if (tasks == null)
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_NULL);
            }

        // Retrieve the Tasks in the order in which they were added
        final Iterator<RootPlugin> iterTasks = tasks.iterator();

        while ((iterTasks != null)
            && (iterTasks.hasNext()))
            {
            final TaskPlugin task = (TaskPlugin)iterTasks.next();

            if ((task != null)
                && (task.validatePlugin()))
                {
                final AtomPlugin atom;

                // Ensure all Tasks start unlocked
                task.setLocked(false);

                // Find the parent of the Task
                atom = task.getParentAtom();

                // All Tasks must be initialised before use...
                // An Exception may be thrown if we can't initialise
                if ((atom != null)
                    && (setTaskState(atom, task, TaskState.INITIALISED)))
                    {
                    //LOGGER.login("RunAtStart " + MSG_TASK_INIT + SPACE + task.getName());

                    // The Task has been initialised successfully
                    // Start up those Runnable Tasks marked as RunAtStart
                    // An Exception may be thrown if we can't run
                    if ((task.isRunAtStart())
                        && (task.isRunnable())
                        && (setTaskState(atom, task, TaskState.RUNNING)))
                        {
                        // Do nothing if all went well, or it was not a runnable Task...
                        }
                    }
                else
                    {
                    throw new FrameworkException(EXCEPTION_TASK_INITIALISE);
                    }
                }
            else
                {
                throw new FrameworkException(EXCEPTION_PARAMETER_NULL);
                }
            }
        }


    /***********************************************************************************************
     * Run a specified Task and show its UI.
     * Set the UI Component of the specified Task in the FrameworkManager, if possible.
     * If the FrameworkManager does not exist, show a warning box.
     * Called only by actionPerformed().
     *
     * @param task
     * @param manager
     */

    public final void runTaskAndShowUI(final TaskPlugin task,
                                       final FrameworkManagerPlugin manager)
        {
        final AtomPlugin atom;

        LOGGER.debugNavigation("RegistryModelController.runTaskAndShowUI() START");

        if ((task != null)
            && (task.validatePlugin())
            && (task.getParentAtom() != null)
            && (task.getParentAtom().validatePlugin()))
            {
            // Do we have a good FrameworkManager?
            if ((manager != null)
                && (manager.validatePlugin())
                && (manager.isRunning())
                && (manager.getUI() != null))
                {
                // Find the parent of the Task (we know this to be valid)
                atom = task.getParentAtom();

                // See if this Task can be run
                // Is it a public task? If not, we can't do anything...
                if (atom.isTaskPublic(task))
                    {
                    // Start the selected Task if possible, and show the UI, if any...
                    // What kind of Task is it?
                    if (atom.isTaskRunnable(task))
                        {
                        // It is Runnable, so is it running already?
                        if (atom.isTaskRunning(task))
                            {
                            // Just show the panel that already exists
                            NavigationUtilities.installUserObjectUI(task, manager);
                            }
                        else
                            {
                            // It isn't Running yet, so try to execute STATE_TASK_RUNNING
                            // setTaskState() does not in itself require a UI
                            if (setTaskState(atom, task, TaskState.RUNNING))
                                {
                                // It is now Running, so show the JPanel
                                NavigationUtilities.installUserObjectUI(task, manager);
                                }
                            else
                                {
                                // Unable to move into the Running state - this is really an error
                                // The Task was parked as STATE_TASK_SHUTDOWN
                                task.setUIComponent(new BlankUIComponent());
                                NavigationUtilities.installUserObjectUI(task, manager);
                                }
                            }
                        }
                    else
                        {
                        // Not Runnable, so try to make visible immediately in STATE_TASK_RUNNING
                        // It can't already be visible because we clicked somewhere else...
                        // setTaskState() does not in itself require a UI
                        if (setTaskState(atom, task, TaskState.RUNNING))
                            {
                            // It is now Running, so show the JPanel
                            NavigationUtilities.installUserObjectUI(task, manager);
                            }
                        else
                            {
                            // Unable to move into the Running state - this is really an error
                            // The Task was parked as STATE_TASK_SHUTDOWN
                            task.setUIComponent(new BlankUIComponent());
                            NavigationUtilities.installUserObjectUI(task, manager);
                            }
                        }
                    }
                else
                    {
                    // Private tasks always show a Blank panel
                    // because we can't affect them
                    task.setUIComponent(new BlankUIComponent());
                    NavigationUtilities.installUserObjectUI(task, manager);
                    }
                }
            else
                {
                NavigationUtilities.unableToPerformAction();
                }
            }
        else
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_INVALID);
            }

        LOGGER.debugNavigation("RegistryModelController.runTaskAndShowUI() END");
        }


    /***********************************************************************************************
     * Set the state of the specified TaskPlugin.
     * The UI will only be shown if required by the Task's run() method.
     * Called in actionHalted() etc.
     * This is a simple state machine...
     *
     * @param host
     * @param plugin
     * @param newstate
     *
     * @return boolean
     */

    public final boolean setTaskState(final AtomPlugin host,
                                      final TaskPlugin plugin,
                                      final TaskState newstate)
        {
        boolean boolValidTransition;
        final boolean boolTaskSuccessful;

        boolValidTransition = false;

        // Check the chain of activation controls
        // We can't do anything if the chain is not complete
        if ((isTaskActivationChainOk(plugin)))
            {
            // Handle the careless and pointless...
            if (((plugin.getState() == TaskState.CREATED) && (newstate == TaskState.CREATED))
                || ((plugin.getState() == TaskState.INITIALISED) && (newstate == TaskState.INITIALISED))
                || ((plugin.getState() == TaskState.IDLE) && (newstate == TaskState.IDLE))
                || ((plugin.getState() == TaskState.RUNNING) && (newstate == TaskState.RUNNING))
                || ((plugin.getState() == TaskState.SHUTDOWN) && (newstate == TaskState.SHUTDOWN)))
                {
                return (true);
                }

            // Firstly, check that we are asked to make a valid transition
            // ToDo use enums
            if (((plugin.getState() == TaskState.CREATED) && (newstate == TaskState.INITIALISED))
                || ((plugin.getState() == TaskState.CREATED) && (newstate == TaskState.SHUTDOWN))

                || ((plugin.getState() == TaskState.INITIALISED) && (newstate == TaskState.IDLE))
                || ((plugin.getState() == TaskState.INITIALISED) && (newstate == TaskState.RUNNING))
                || ((plugin.getState() == TaskState.INITIALISED) && (newstate == TaskState.SHUTDOWN))

                || ((plugin.getState() == TaskState.SHUTDOWN) && (newstate == TaskState.INITIALISED))

                || ((plugin.getState() == TaskState.IDLE) && (newstate == TaskState.RUNNING))
                || ((plugin.getState() == TaskState.IDLE) && (newstate == TaskState.SHUTDOWN))

                || ((plugin.getState() == TaskState.RUNNING) && (newstate == TaskState.IDLE))
                || ((plugin.getState() == TaskState.RUNNING) && (newstate == TaskState.SHUTDOWN)))
                {
                boolValidTransition = true;
                }

            // If the transition is allowed, attempt to change the task state
            // Check for the right conditions to:
            //    initialiseTask()
            //    runTask()
            //    idleTask()
            //    shutdownTask()

            if (boolValidTransition)
                {
                // initialiseTask()
                if (newstate == TaskState.INITIALISED)
                    {
                    //LOGGER.debug(".setTaskState [initialising " + plugin.getPathname() + "]");
                    //System.out.println(".setTaskState [initialising " + plugin.getPathname() + "]");

                    // Mark this Task as Locked, i.e. in use
                    plugin.setLocked(true);

                    if (plugin.initialiseTask())
                        {
                        // Clear the initialisation times
                        plugin.setStartDate(null);
                        plugin.setStartTime(null);
                        plugin.setStopDate(null);
                        plugin.setStopTime(null);

                        // Make sure that the plugin is not in the Runners list
                        REGISTRY_MODEL.removeRunner(plugin);

                        LOGGER.logAtomEvent(plugin.getParentAtom(),
                                            plugin,
                                            plugin.getClass().getName(),
                                            METADATA_TASK_INITIALISE
                                                + SPACE
                                                + METADATA_NAME
                                                + plugin.getPathname()
                                                + TERMINATOR,
                                            EventStatus.INFO);

                        boolTaskSuccessful = plugin.setState(newstate);
                        }
                    else
                        {
                        // Log all failed attempts (for now)
                        plugin.setStartDate(null);
                        plugin.setStartTime(null);
                        plugin.setStopDate(null);
                        plugin.setStopTime(null);

                        // Make sure that the plugin is no longer in the Runners list
                        REGISTRY_MODEL.removeRunner(plugin);

                        LOGGER.logAtomEvent(plugin.getParentAtom(),
                                            plugin,
                                            plugin.getClass().getName(),
                                            METADATA_TASK_INITIALISE_FAIL
                                                + SPACE
                                                + METADATA_NAME
                                                + plugin.getPathname()
                                                + TERMINATOR,
                                            EventStatus.INFO);

                        // Park an unsuccessful Task in STATE_TASK_SHUTDOWN
                        boolTaskSuccessful = plugin.setState(TaskState.SHUTDOWN);
                        }

                    // The Task is free again
                    plugin.setLocked(false);
                    }

                // runTask()
                else if (newstate == TaskState.RUNNING)
                    {
                    LOGGER.debugNavigation("RegistryModelController.setTaskState [running " + plugin.getPathname() + "]");
                    //System.out.println(".setTaskState [running " + plugin.getPathname() + "]");

                    // Mark this Task as Locked, i.e. in use
                    plugin.setLocked(true);

                    // Run the Task!!
                    if (plugin.runTask())
                        {
                        // If we moved successfully to Running,
                        // set the Start and log the event...
                        plugin.setStartDate(new GregorianCalendar());
                        plugin.setStartTime(new GregorianCalendar());

                        // Remove any previous Stop
                        plugin.setStopDate(null);
                        plugin.setStopTime(null);

                        boolTaskSuccessful = plugin.setState(newstate);

                        // Record the running item
                        REGISTRY_MODEL.addRunner(plugin);

                        // Tell the world that this Task has started!
                        LOGGER.logAtomEvent(plugin.getParentAtom(),
                                            plugin,
                                            plugin.getClass().getName(),
                                            METADATA_TASK_START
                                                + SPACE
                                                + METADATA_NAME
                                                + plugin.getPathname()
                                                + TERMINATOR,
                                            EventStatus.INFO);
                        }
                    else
                        {
                        // Log all failed attempts (for now)
                        plugin.setStartDate(null);
                        plugin.setStartTime(null);
                        plugin.setStopDate(null);
                        plugin.setStopTime(null);

                        // Make sure that the plugin is not in the Runners list
                        REGISTRY_MODEL.removeRunner(plugin);

                        LOGGER.logAtomEvent(plugin.getParentAtom(),
                                            plugin,
                                            plugin.getClass().getName(),
                                            METADATA_TASK_START_FAIL
                                                + SPACE
                                                + METADATA_NAME
                                                + plugin.getPathname()
                                                + TERMINATOR,
                                            EventStatus.INFO);

                        // Park an unsuccessful Task in STATE_TASK_SHUTDOWN
                        boolTaskSuccessful = plugin.setState(TaskState.SHUTDOWN);
                        }

                    // The Task is free again
                    plugin.setLocked(false);
                    }

                // idleTask()
                else if (newstate == TaskState.IDLE)
                    {
                    //LOGGER.debug(".setTaskState [idle " + plugin.getPathname() + "]");

                    // Mark this Task as Locked, i.e. in use
                    plugin.setLocked(true);

                    if (plugin.idleTask())
                        {
                        // The Task has effectively 'stopped' in Idle,
                        // but only if it was previously started
                        if ((plugin.getStartDate() != null)
                            && (plugin.getStartTime() != null))
                            {
                            plugin.setStopDate(new GregorianCalendar());
                            plugin.setStopTime(new GregorianCalendar());
                            }

                        // Remove the running item if possible
                        REGISTRY_MODEL.removeRunner(plugin);

                        LOGGER.logAtomEvent(plugin.getParentAtom(),
                                            plugin,
                                            plugin.getClass().getName(),
                                            METADATA_TASK_IDLE
                                                + SPACE
                                                + METADATA_NAME
                                                + plugin.getPathname()
                                                + TERMINATOR,
                                            EventStatus.INFO);

                        boolTaskSuccessful = plugin.setState(newstate);
                        }
                    else
                        {
                        // Log all failed attempts (for now)
                        //task.setStartTime(null);
                        plugin.setStopDate(new GregorianCalendar());
                        plugin.setStopTime(new GregorianCalendar());

                        // Make sure that the plugin is not in the Runners list
                        REGISTRY_MODEL.removeRunner(plugin);

                        LOGGER.logAtomEvent(plugin.getParentAtom(),
                                            plugin,
                                            plugin.getClass().getName(),
                                            METADATA_TASK_IDLE_FAIL
                                                + SPACE
                                                + METADATA_NAME
                                                + plugin.getPathname()
                                                + TERMINATOR,
                                            EventStatus.INFO);

                        // Park an unsuccessful Task in STATE_TASK_SHUTDOWN
                        boolTaskSuccessful = plugin.setState(TaskState.SHUTDOWN);
                        }

                    // The Task is free again
                    plugin.setLocked(false);
                    }

                // shutdownTask()
                else if (newstate == TaskState.SHUTDOWN)
                    {
                    //LOGGER.debug(".setTaskState [shutdown " + plugin.getPathname() + "]");
                    //System.out.println(".setTaskState [shutdown " + plugin.getPathname() + "]");

                    // Mark this Task as Locked, i.e. in use
                    plugin.setLocked(true);

                    if (plugin.shutdownTask())
                        {
                        // Set the Stop, and log the event...
                        plugin.setStopDate(new GregorianCalendar());
                        plugin.setStopTime(new GregorianCalendar());

                        // Remove the running item if possible
                        REGISTRY_MODEL.removeRunner(plugin);

                        LOGGER.logAtomEvent(plugin.getParentAtom(),
                                            plugin,
                                            plugin.getClass().getName(),
                                            METADATA_TASK_SHUTDOWN
                                                + SPACE
                                                + METADATA_NAME
                                                + plugin.getPathname()
                                                + TERMINATOR,
                                            EventStatus.INFO);

                        boolTaskSuccessful = plugin.setState(newstate);
                        }
                    else
                        {
                        // Log all failed attempts (for now)
                        plugin.setStartDate(null);
                        plugin.setStartTime(null);
                        plugin.setStopDate(new GregorianCalendar());
                        plugin.setStopTime(new GregorianCalendar());

                        // Make sure that the plugin is not in the Runners list
                        REGISTRY_MODEL.removeRunner(plugin);

                        LOGGER.logAtomEvent(plugin.getParentAtom(),
                                            plugin,
                                            plugin.getClass().getName(),
                                            METADATA_TASK_SHUTDOWN_FAIL
                                                + SPACE
                                                + METADATA_NAME
                                                + plugin.getPathname()
                                                + TERMINATOR,
                                            EventStatus.INFO);

                        // Park an unsuccessful Task in STATE_TASK_SHUTDOWN
                        boolTaskSuccessful = plugin.setState(TaskState.SHUTDOWN);
                        }

                    // The Task is free again
                    plugin.setLocked(false);
                    }
                else
                    {
                    // This cannot happen - an invalid newstate
                    boolTaskSuccessful = false;
                    }

                // See if any of the above task.setInstrumentState() failed
                if (!boolTaskSuccessful)
                    {
                    // Log all failed attempts (for now)
                    plugin.setStartDate(null);
                    plugin.setStartTime(null);
                    plugin.setStopDate(null);
                    plugin.setStopTime(null);

                    // Make sure that the plugin is not in the Runners list
                    REGISTRY_MODEL.removeRunner(plugin);

                    LOGGER.logAtomEvent(plugin.getParentAtom(),
                                        plugin,
                                        plugin.getClass().getName(),
                                        METADATA_TASK_STATE_ERROR
                                            + SPACE
                                            + METADATA_NAME
                                            + plugin.getPathname()
                                            + TERMINATOR,
                                        EventStatus.INFO);

                    // Try to park an unsuccessful Task in STATE_TASK_SHUTDOWN,
                    // but preserve the original boolTaskSuccessful (which failed)
                    plugin.setState(TaskState.SHUTDOWN);
                    }

                return (boolTaskSuccessful);
                }
            else
                {
                // It was a request for an invalid transition
                // which is likely to be a configuration error...
                plugin.setState(TaskState.CREATED);
                plugin.setStartDate(null);
                plugin.setStartTime(null);
                plugin.setStopDate(null);
                plugin.setStopTime(null);

                // Make sure that the plugin is not in the Runners list
                REGISTRY_MODEL.removeRunner(plugin);

                LOGGER.logAtomEvent(plugin.getParentAtom(),
                                    plugin,
                                    plugin.getClass().getName(),
                                    METADATA_TASK_STATE_ERROR
                                        + SPACE
                                        + METADATA_NAME
                                        + plugin.getPathname()
                                        + TERMINATOR,
                                    EventStatus.INFO);

                return (false);
                }
            }
        else
            {
            // We can't change the Task state at the moment
            // because the activation chain is not complete
            final StringBuffer buffer = new StringBuffer(METADATA_TASK_STATE_ERROR
                                                           + SPACE
                                                           + METADATA_NAME
                                                           + plugin.getPathname()
                                                           + TERMINATOR
                                                           + SPACE);
            if (!host.isActive())
                {
                buffer.append(METADATA_INACTIVEPARENT);
                }

            if (!plugin.isActive())
                {
                buffer.append(METADATA_INACTIVE);
                }

            if (plugin.isLocked())
                {
                buffer.append(METADATA_LOCKED);
                }

            // Tell the user, and record the problem
            LOGGER.logAtomEvent(plugin.getParentAtom(),
                                plugin,
                                plugin.getClass().getName(),
                                buffer.toString(),
                                EventStatus.INFO);
            return (false);
            }
        }


    /***********************************************************************************************
     * Is the chain of Task activation controls (active, locked) all Ok back to the Framework root?
     *
     * @param plugin
     *
     * @return boolean
     */

    public boolean isTaskActivationChainOk(final TaskPlugin plugin)
        {
        boolean boolChainOk;

        // Test the Task first, and then move up into the Plugin chain
        boolChainOk = ((plugin != null)
                    && (plugin.validatePlugin())
                    && (plugin.isActive())
                    && (!plugin.isLocked()));

        // If the Task chain fails, don't bother to check the parent Plugin chain
        if (boolChainOk)
            {
            boolChainOk = isPluginActivationChainOk(plugin.getParentAtom());
            }

        return (boolChainOk);
        }


    /***********************************************************************************************
     * Show a message that something went wrong when trying to control a Task.
     *
     * @param plugin
     */

    public void unableToControlTask(final TaskPlugin plugin)
        {
        if (plugin != null)
            {
            final String [] strMessage =
                {
                "It has not been possible to control the " + plugin.getName(),
                "Try to identify the problem from the EventLog",
                "You may need to logout and login before trying again"
                };

            JOptionPane.showMessageDialog(null,
                                          strMessage,
                                          plugin.getName() + " Task Controller",
                                          JOptionPane.WARNING_MESSAGE);
            }
        }


    /**********************************************************************************************/
    /* Login Support                                                                              */
    /***********************************************************************************************
     * Initialise and show the LoginDialog, which gets the User credentials,
     * so that the RegistryManager can assemble the appropriate beans for the UserRole.
     *
     * @param plugin
     */

    public void showLoginDialog(final FrameworkPlugin plugin)
        {
        // Zap the current User
        REGISTRY_MODEL.setLoggedInUser(null);

        if ((plugin != null)
            && (DATABASE.getDatabaseOptions() != null))
            {
            setLoginDialog(new LoginDialog(DATABASE.getDatabaseOptions().getDataStore(),
                                           false));
            getLoginDialog().initialiseUI();
            LOGGER.flushLoginLog();

            // The current Thread will stop when the JDialog is made visible
            // Enable the Login tab, now we have some beans to configure
            // Create the Administrator User if this is the first time in...

            getLoginDialog().enableLogin(true);
            getLoginDialog().toFront();

            // Make the Username have the focus
            // TODO Move this down one day!
            if ((getLoginDialog() != null)
                && (getLoginDialog().getLoginPanel() != null)
                && (((LoginPanel)getLoginDialog().getLoginPanel()).getLoginTab() != null)
                && (((LoginTab)((LoginPanel)getLoginDialog().getLoginPanel()).getLoginTab()).getUsernameBox() != null))
                {
                ((LoginTab)((LoginPanel)getLoginDialog().getLoginPanel()).getLoginTab()).getUsernameBox().requestFocusInWindow();
                }

            getLoginDialog().setVisible(true);
            }
        }


    /***********************************************************************************************
     * Dispose of the LoginDialog.
     */

    public void disposeLoginDialog()
        {
        // Flush the LoginPanel log buffer if we can
        LOGGER.flushLoginLog();

        // Remove the LoginDialog
        if (getLoginDialog() != null)
            {
            getLoginDialog().disposeUI();
            }
        }


    /***********************************************************************************************
     * Get the LoginDialog.
     *
     * @return LoginDialog
     */

    public LoginDialog getLoginDialog()
        {
        return (loginDialog);
        }


    /***********************************************************************************************
     * Set the LoginDialog.
     *
     * @param dialog
     */

    public void setLoginDialog(final LoginDialog dialog)
        {
        loginDialog = dialog;
        }
    }
