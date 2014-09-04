//--------------------------------------------------------------------------------------------------
// Revision History
//
//  10-11-03    LMN created file
//  11-11-03    LMN extracted class RootData
//  18-11-03    LMN removed parent object references to RootData
//  30-09-04    LMN added handleException()
//  15-11-04    LMN added script creators
//  20-10-05    LMN added script generation
//
//--------------------------------------------------------------------------------------------------
// RegistryModel package

package org.lmn.fc.model.plugins.impl;


//--------------------------------------------------------------------------------------------------
// Imports

import org.lmn.fc.common.events.ActiveChangeEvent;
import org.lmn.fc.common.events.ActiveChangeListener;
import org.lmn.fc.common.exceptions.FrameworkException;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.plugins.AtomPlugin;
import org.lmn.fc.model.plugins.PluginState;
import org.lmn.fc.model.registry.NavigationUtilities;
import org.lmn.fc.model.registry.RegistryModelUtilities;
import org.lmn.fc.model.registry.impl.ExpanderFactory;
import org.lmn.fc.model.resources.*;
import org.lmn.fc.model.root.RootPlugin;
import org.lmn.fc.model.root.impl.UserObject;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.model.tasks.TaskState;
import org.lmn.fc.model.users.RolePlugin;
import org.lmn.fc.model.xmlbeans.roles.RoleName;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;


/***************************************************************************************************
 * The base class for Framework and Plugin.
 *
 * ToDo Review design of Task state machine
 * ToDo Task logging
 */

public abstract class AtomData extends UserObject
                               implements AtomPlugin
    {
    // String Resources
    private static final String DIALOG_PLUGIN_CONTROL = "Plugin Control";
    private static final String MSG_STOP_PLUGIN_0 = "You may not stop a running Plugin while using its Editor";
    private static final String MSG_STOP_PLUGIN_1 = "Please leave the Editor and try again";

    private RootPlugin pluginParent;
    private String strVersionNumber;
    private String strBuildNumber;
    private String strBuildStatus;
    private RootPlugin taskInterface;

    // The UserRoles permitted to use this Atom
    private List<RolePlugin> listRoles;

    private short shortSortOrder;
    private PluginState enumPluginState;
    private boolean boolLocked;

    private final Vector<RootPlugin> vecAtoms;
    private final Vector<RootPlugin> vecTasks;
    private final Vector<ResourcePlugin> vecProperties;
    private final Vector<ResourcePlugin> vecStrings;
    private final Vector<ResourcePlugin> vecExceptions;
    private final Vector<ResourcePlugin> vecQueries;

    private DefaultMutableTreeNode nodePluginExpander;
    private DefaultMutableTreeNode nodeTaskExpander;
    private DefaultMutableTreeNode nodePropertyExpander;
    private DefaultMutableTreeNode nodeStringExpander;
    private DefaultMutableTreeNode nodeExceptionExpander;
    private DefaultMutableTreeNode nodeQueryExpander;

    // Event listeners
    private Vector<ActiveChangeListener> vecListeners = new Vector<ActiveChangeListener>(10);


    /***********************************************************************************************
     * Assign RolePlugins to the specified Plugin, as defined by the RoleNames in the Plugin description.
     * The corresponding RolePlugins must exist in the Registry.
     *
     * @param plugin
     */

    public static void assignRoles(final AtomPlugin plugin)
        {
        if ((plugin != null)
            && (plugin.validatePlugin())
            && (REGISTRY.getRoles() != null))
            {
            final List<RoleName.Enum> listRoleNames;
            String strRoleKey;

            // Read the list of RoleNames applicable to this Plugin
            listRoleNames = plugin.getRoleNames();

            for (final RoleName.Enum name : listRoleNames)
                {
                // Check that the RoleName corresponds to a Role in the Registry
                strRoleKey = PREFIX_ROLE + KEY_DELIMITER + name.toString();

                if (REGISTRY.getRoles().containsKey(strRoleKey))
                    {
                    // Assign the Role to the Plugin
                    plugin.addUserRole(REGISTRY.getRole(strRoleKey));
                    }
                }
            }
        else
            {
            LOGGER.error("Unable to assign Plugin Roles");
            }
        }


   /************************************************************************************************
    * Construct an AtomData.
    *
    * @param id
    * @param expanders
    */

    public AtomData(final long id,
                    final boolean expanders)
        {
        super(id);

        final DefaultMutableTreeNode nodeResourceExpander;

        // Bidirectionally link the AtomData to the host node
        getHostTreeNode().setUserObject(this);
        getHostTreeNode().setAllowsChildren(true);

        // Does this Atom plugin need navigation tree expanders?
        if (expanders)
            {
            addMainExpanders();
            }

        // Initialise the Plugin State, but not via the RegistryModelController
        this.enumPluginState = PluginState.STOPPED;

        // Vectors of attached items
        this.listRoles = new ArrayList<RolePlugin>(REGISTRY.getRoles().size());
        this.vecAtoms = new Vector<RootPlugin>(10);
        this.vecTasks = new Vector<RootPlugin>(10);
        this.vecProperties = new Vector<ResourcePlugin>(10);
        this.vecStrings = new Vector<ResourcePlugin>(10);
        this.vecExceptions = new Vector<ResourcePlugin>(10);
        this.vecQueries = new Vector<ResourcePlugin>(10);

        // Miscellaneous properties
        // ToDo - A, T, R are different??
        this.shortSortOrder = 0;

        this.boolLocked = false;

        this.vecListeners = new Vector<ActiveChangeListener>(10);

        this.strVersionNumber = EMPTY_STRING;
        this.strBuildNumber = EMPTY_STRING;
        this.strBuildStatus = EMPTY_STRING;
        }


    /***********************************************************************************************
     * Get the UserRoles.
     *
     * @return long
     */

    public final List<RolePlugin> getUserRoles()
        {
        return (this.listRoles);
        }


   /***********************************************************************************************
    * Set the UserRoles.
    *
    * @param roles
    */

   public final void setUserRoles(final List<RolePlugin> roles)
       {
       this.listRoles = roles;
       }


   /***********************************************************************************************
    * Add a UserRole to the List of Roles supported by this Atom.
    * Only add valid Roles which are not already attached, and exist in the Registry.
    *
    * @param role
    */

   public final void addUserRole(final RolePlugin role)
       {
       if ((role != null)
           && (getUserRoles() != null)
           && (!getUserRoles().contains(role))
           && (REGISTRY.getRoles() != null)
           && (REGISTRY.getRoles().contains(role)))
           {
           getUserRoles().add(role);
           }
       }


    /***********************************************************************************************
     * Get the Root Task.
     *
     * @return RootPlugin
     */

    public final RootPlugin getRootTask()
        {
        if (this.taskInterface == null)
            {
            throw new FrameworkException(EXCEPTION_ROOT_TASK_NULL);
            }

        return (this.taskInterface);
        }


    /***********************************************************************************************
     * Set the Root Task.
     *
     * @param taskinterface
     */

    public final void setRootTask(final RootPlugin taskinterface)
        {
        this.taskInterface = taskinterface;
        }


    /***********************************************************************************************
     * Get the SortOrder field.
     *
     * @return int
     */

    public final short getSortOrder()
        {
        return (this.shortSortOrder);
        }


    /***********************************************************************************************
     * Set the SortOrder field.
     *
     * @param sortorder
     */

    public final void setSortOrder(final short sortorder)
        {
        this.shortSortOrder = sortorder;
        updateRoot();
        }


    /***********************************************************************************************
     * Get the Locked field.
     *
     * @return boolean
     */

    public final boolean isLocked()
        {
        return this.boolLocked;
        }


    /***********************************************************************************************
     * Set the Locked field.
     *
     * @param locked
     */

    public final void setLocked(final boolean locked)
        {
        this.boolLocked = locked;
        updateRoot();
        }


    /***********************************************************************************************
     * Get the VersionNumber.
     *
     * @return String
     */

    public final String getVersionNumber()
        {
        return (this.strVersionNumber);
        }


    /***********************************************************************************************
     * Set the VersionNumber.
     *
     * @param versionnumber
     */

    public final void setVersionNumber(final String versionnumber)
        {
        this.strVersionNumber = versionnumber;
        }


    /***********************************************************************************************
     * Get the BuildNumber.
     *
     * @return String
     */

    public final String getBuildNumber()
        {
        return (this.strBuildNumber);
        }


    /***********************************************************************************************
     * Set the BuildNumber.
     *
     * @param buildnumber
     */

    public final void setBuildNumber(final String buildnumber)
        {
        this.strBuildNumber = buildnumber;
        }


    /***********************************************************************************************
     * Get the BuildStatus.
     *
     * @return String
     */

    public final String getBuildStatus()
        {
        return (this.strBuildStatus);
        }


    /***********************************************************************************************
     * Set the BuildStatus.
     *
     * @param buildstatus
     */

    public final void setBuildStatus(final String buildstatus)
        {
        this.strBuildStatus = buildstatus;
        }


    /***********************************************************************************************
     * Get the parent of this AtomPlugin.
     * Should return <code>null</code> at the Framework root.
     *
     * @return RootPlugin
     */

    public final RootPlugin getParentAtom()
        {
        return (this.pluginParent);
        }


    /***********************************************************************************************
     * Set the parent of this AtomPlugin.
     *
     * @param atom
     */

    public final void setParentAtom(final RootPlugin atom)
        {
        this.pluginParent = atom;
        }


    /***********************************************************************************************
     * Add an AtomPlugin to the Vector of supported Atoms.
     *
     * @param atom
     */

    public final void addAtom(final RootPlugin atom)
        {
        this.vecAtoms.add(atom);
        }


    /***********************************************************************************************
     * Remove an AtomPlugin from the Vector of supported Atoms.
     *
     * @param atom
     */

    public final void removeAtom(final RootPlugin atom)
        {
        this.vecAtoms.remove(atom);
        }


    /***********************************************************************************************
     * Clear all Atoms from the Vector of attached Atoms.
     */

     public final void clearAtoms()
         {
         this.vecAtoms.clear();
         }


    /***********************************************************************************************
     * Return the Vector of Atoms attached to this Atom.
     *
     * @return Vector
     */

    public final Vector<RootPlugin> getAtoms()
        {
        return (this.vecAtoms);
        }


    /***********************************************************************************************
     * Show the Atoms attached to this Atom.
     *
     * @param show
     */

    public final void showAttachedAtoms(final boolean show)
        {
        if (show)
            {
            final Iterator<RootPlugin> iterAtoms;

            LOGGER.log("Atoms for " + getName());

            iterAtoms = getAtoms().iterator();

            while (iterAtoms.hasNext())
                {
                final RootPlugin pluginAtom = iterAtoms.next();

                LOGGER.log(getName() + SPACE + pluginAtom.getPathname());
                }
            }
        }


    /***********************************************************************************************
     *
     * @param resource
     */

    public final void addResource(final ResourcePlugin resource)
        {
        vecProperties.add(resource);
        }


    /***********************************************************************************************
     * Add the main expanders to this Atom.
     * Plugins, Tasks, Resources, Properties, Strings, Exceptions, Queries.
     */

    public final void addMainExpanders()
        {
        final DefaultMutableTreeNode nodeResourceExpander;

        // Create some expanders for this Atom, and link them together
        // BEWARE! getPathname() does not work at this point in the load...
        nodePluginExpander = new DefaultMutableTreeNode();
        nodePluginExpander.setUserObject(ExpanderFactory.getInstance().getExpander(ExpanderFactory.EXPANDER_PLUGINS,
                                                                                   ExpanderFactory.EXPANDER_PLUGINS,
                                                                                   AtomPlugin.PLUGINS_ICON));
        nodePluginExpander.setAllowsChildren(true);
        getHostTreeNode().add(nodePluginExpander);

        nodeTaskExpander = new DefaultMutableTreeNode();
        nodeTaskExpander.setUserObject(ExpanderFactory.getInstance().getExpander(ExpanderFactory.EXPANDER_TASKS,
                                                                                 ExpanderFactory.EXPANDER_TASKS,
                                                                                 TaskPlugin.TASKS_ICON));
        nodeTaskExpander.setAllowsChildren(true);
        getHostTreeNode().add(nodeTaskExpander);

        nodeResourceExpander = new DefaultMutableTreeNode();
        nodeResourceExpander.setUserObject(ExpanderFactory.getInstance().getExpander(ExpanderFactory.EXPANDER_RESOURCES,
                                                                                     ExpanderFactory.EXPANDER_RESOURCES,
                                                                                     ResourcePlugin.RESOURCES_ICON));
        nodeResourceExpander.setAllowsChildren(true);
        getHostTreeNode().add(nodeResourceExpander);

        nodePropertyExpander = new DefaultMutableTreeNode();
        nodePropertyExpander.setUserObject(ExpanderFactory.getInstance().getExpander(ExpanderFactory.EXPANDER_PROPERTIES,
                                                                                     ExpanderFactory.EXPANDER_PROPERTIES,
                                                                                     PropertyPlugin.PROPERTIES_ICON));
        nodePropertyExpander.setAllowsChildren(true);
        nodeResourceExpander.add(nodePropertyExpander);

        nodeStringExpander = new DefaultMutableTreeNode();
        nodeStringExpander.setUserObject(ExpanderFactory.getInstance().getExpander(ExpanderFactory.EXPANDER_STRINGS,
                                                                                   ExpanderFactory.EXPANDER_STRINGS,
                                                                                   StringPlugin.STRINGS_ICON));
        nodeStringExpander.setAllowsChildren(true);
        nodeResourceExpander.add(nodeStringExpander);

        nodeExceptionExpander = new DefaultMutableTreeNode();
        nodeExceptionExpander.setUserObject(ExpanderFactory.getInstance().getExpander(ExpanderFactory.EXPANDER_EXCEPTIONS,
                                                                                      ExpanderFactory.EXPANDER_EXCEPTIONS,
                                                                                      ExceptionPlugin.EXCEPTIONS_ICON));
        nodeExceptionExpander.setAllowsChildren(true);
        nodeResourceExpander.add(nodeExceptionExpander);

        nodeQueryExpander = new DefaultMutableTreeNode();
        nodeQueryExpander.setUserObject(ExpanderFactory.getInstance().getExpander(ExpanderFactory.EXPANDER_QUERIES,
                                                                                  ExpanderFactory.EXPANDER_QUERIES,
                                                                                  QueryPlugin.QUERIES_ICON));
        nodeQueryExpander.setAllowsChildren(true);
        nodeResourceExpander.add(nodeQueryExpander);
        }


    /***********************************************************************************************
     *
     * @return DefaultMutableTreeNode
     */

    public DefaultMutableTreeNode getPluginExpander()
        {
        return (this.nodePluginExpander);
        }


    /***********************************************************************************************
     *
     * @return DefaultMutableTreeNode
     */

    public DefaultMutableTreeNode getTaskExpander()
        {
        return (this.nodeTaskExpander);
        }


    /***********************************************************************************************
     *
     * @return DefaultMutableTreeNode
     */

    public DefaultMutableTreeNode getPropertyExpander()
        {
        return (this.nodePropertyExpander);
        }

    /***********************************************************************************************
     *
     * @return DefaultMutableTreeNode
     */
    public DefaultMutableTreeNode getStringExpander()
        {
        return (this.nodeStringExpander);
        }


    /***********************************************************************************************
     *
     * @return DefaultMutableTreeNode
     */
    public DefaultMutableTreeNode getExceptionExpander()
        {
        return (this.nodeExceptionExpander);
        }


    /***********************************************************************************************
     *
     * @return DefaultMutableTreeNode
     */
    public DefaultMutableTreeNode getQueryExpander()
        {
        return (this.nodeQueryExpander);
        }


    /***********************************************************************************************
     * Add a Property to the Vector of attached Properties.
     *
     * @param property
     */

     public final void addProperty(final ResourcePlugin property)
         {
         this.vecProperties.add(property);
         }


    /***********************************************************************************************
     * Remove a Property from the Vector of attached Properties.
     *
     * @param property
     */

     public final void removeProperty(final PropertyPlugin property)
         {
         this.vecProperties.remove(property);
         }


    /***********************************************************************************************
     * Clear all Properties from the Vector of attached Properties.
     */

     public final void clearProperties()
         {
         this.vecProperties.clear();
         }


    /***********************************************************************************************
     * Return the attached Properties.
     *
     * @return Iterator
     */

    public final Vector<ResourcePlugin> getProperties()
        {
        return (this.vecProperties);
        }


    /***********************************************************************************************
     * Show the Properties attached to this Atom.
     *
     * @param show
     */

    public final void showAttachedProperties(final boolean show)
        {
        if (show)
            {
            final Iterator<ResourcePlugin> iterProperties;

            LOGGER.log("Properties for " + getName());

            iterProperties = getProperties().iterator();

            while (iterProperties.hasNext())
                {
                final ResourcePlugin property = iterProperties.next();

                LOGGER.log(INDENT + "[" + property.getResourceKey()
                            + "=" + property.getResource() + "]");
                }
            }
        }


    /***********************************************************************************************
     * Add a String to the Vector of attached Strings.
     *
     * @param string
     */

     public final void addString(final ResourcePlugin string)
         {
         this.vecStrings.add(string);
         }


    /***********************************************************************************************
     * Remove a String from the Vector of attached Strings.
     *
     * @param string
     */

     public final void removeString(final ResourcePlugin string)
         {
         this.vecStrings.remove(string);
         }


    /***********************************************************************************************
     * Clear all Strings from the Vector of attached Strings.
     */

     public final void clearStrings()
         {
         this.vecStrings.clear();
         }


    /***********************************************************************************************
     * Return the attached Strings.
     *
     * @return Iterator
     */

    public final Vector<ResourcePlugin> getStrings()
        {
        return (this.vecStrings);
        }


    /***********************************************************************************************
     * Show the Strings attached to this Atom.
     *
     * @param show
     */

    public final void showAttachedStrings(final boolean show)
        {
        if (show)
            {
            final Iterator<ResourcePlugin> iterStrings;

            LOGGER.log("Strings for " + getName());

            iterStrings = getStrings().iterator();

            while (iterStrings.hasNext())
                {
                final ResourcePlugin stringData = iterStrings.next();

                LOGGER.log(INDENT + "[" + stringData.getResourceKey()
                            + "=" + stringData.getResource() + "]");
                }
            }
        }


    /***********************************************************************************************
     * Add an Exception to the Vector of attached Exceptions.
     *
     * @param exception
     */

     public final void addException(final ResourcePlugin exception)
         {
         this.vecExceptions.add(exception);
         }


    /***********************************************************************************************
     * Remove an Exception from the Vector of attached Exceptions.
     *
     * @param exception
     */

     public final void removeException(final ExceptionPlugin exception)
         {
         this.vecExceptions.remove(exception);
         }


    /***********************************************************************************************
     * Clear all Exceptions from the Vector of attached Exceptions.
     */

     public final void clearExceptions()
         {
         this.vecExceptions.clear();
         }


    /***********************************************************************************************
     * Return the attached Exceptions.
     *
     * @return Vector
     */

    public final Vector<ResourcePlugin> getExceptions()
        {
        return (this.vecExceptions);
        }


    /***********************************************************************************************
     * Show the Exceptions attached to this Atom.
     *
     * @param show
     */

    public final void showAttachedExceptions(final boolean show)
        {
        if (show)
            {
            final Iterator<ResourcePlugin> iterExceptions;

            LOGGER.log("Exceptions for " + getName());

            iterExceptions = getExceptions().iterator();

            while (iterExceptions.hasNext())
                {
                final ResourcePlugin plugin = iterExceptions.next();

                LOGGER.log(getName() + SPACE + plugin.getPathname() + EQUALS + plugin.getResource());
                }
            }
        }


    /***********************************************************************************************
     * Add a Query to the Vector of attached Queries.
     *
     * @param query
     */

     public final void addQuery(final ResourcePlugin query)
         {
         this.vecQueries.add(query);
         }


    /***********************************************************************************************
     * Remove a Query from the Vector of attached Queries.
     *
     * @param query
     */

     public final void removeQuery(final QueryPlugin query)
         {
         this.vecQueries.remove(query);
         }


    /***********************************************************************************************
     * Clear all Queries from the Vector of attached Queries.
     */

     public final void clearQueries()
         {
         this.vecQueries.clear();
         }


    /***********************************************************************************************
     * Return the attached Queries.
     *
     * @return Vector
     */

    public final Vector<ResourcePlugin> getQueries()
        {
        return (this.vecQueries);
        }


    /***********************************************************************************************
     * Show the Queries attached to this Atom.
     *
     * @param show
     */

    public final void showAttachedQueries(final boolean show)
        {
        if (show)
            {
            final Iterator<ResourcePlugin> iterQueries;

            LOGGER.log("Queries for " + getName());

            iterQueries = getQueries().iterator();

            while (iterQueries.hasNext())
                {
                final ResourcePlugin plugin = iterQueries.next();

                LOGGER.log(getName() + SPACE + plugin.getPathname() + EQUALS + plugin.getResource());
                }
            }
        }


    /***********************************************************************************************
     * Get the Plugin state.
     *
     * @return PluginState
     */

    public PluginState getState()
        {
        return (this.enumPluginState);
        }


    /***********************************************************************************************
     * Set the Plugin state.
     *
     * @param state
     *
     * @return boolean
     */

    public final boolean setState(final PluginState state)
        {
        this.enumPluginState = state;

        return (true);
        }


    /***********************************************************************************************
     * Return a flag to indicate if this Plugin is Running.
     * To be used by the MonitoredItem interface.
     *
     * @return boolean
     */

    public final boolean isRunning()
        {
        return (PluginState.RUNNING.equals(getState()));
        }


    /**********************************************************************************************/
    /* Task Management                                                                            */
    /***********************************************************************************************
     * Add a TaskPlugin to the Vector of supported Tasks.
     *
     * @param task
     */

    public final void addTask(final RootPlugin task)
        {
        this.vecTasks.add(task);
        }


    /***********************************************************************************************
     * Remove a TaskPlugin from the Vector of supported Tasks.
     *
     * @param task
     */

    public final void removeTask(final RootPlugin task)
        {
        this.vecTasks.remove(task);
        }


    /***********************************************************************************************
     * Clear all Tasks from the Vector of attached Tasks.
     */

     public final void clearTasks()
         {
         this.vecTasks.clear();
         }


    /***********************************************************************************************
     * Return the attached Tasks.
     *
     * @return Vector<RootPlugin>
     */

    public final Vector<RootPlugin> getTasks()
        {
        return (this.vecTasks);
        }


    /***********************************************************************************************
     * Show the Tasks attached to this Atom.
     *
     * @param show
     */

    public final void showAttachedTasks(final boolean show)
        {
        if (show)
            {
            final Iterator<RootPlugin> iterTasks;

            LOGGER.log("Tasks for " + getName());

            iterTasks = getTasks().iterator();

            while (iterTasks.hasNext())
                {
                final TaskPlugin plugin = (TaskPlugin)iterTasks.next();

                LOGGER.log(INDENT + plugin.getPathname());
                }
            }
        }


    /***********************************************************************************************
     * Get the state of the specified TaskPlugin.
     *
     * @param task
     *
     * @return int
     */

    public final TaskState getTaskState(final TaskPlugin task)
        {
        return (task.getState());
        }


    /***********************************************************************************************
     * See if the specified TaskPlugin has the public attribute.
     *
     * @param task
     *
     * @return boolean
     */

    public final boolean isTaskPublic(final TaskPlugin task)
        {
        return (task.isPublic());
        }


    /***********************************************************************************************
     * See if the specified TaskPlugin has the runnable attribute.
     *
     * @param task
     * @return boolean
     */

    public final boolean isTaskRunnable(final TaskPlugin task)
        {
        return (task.isRunnable());
        }


    /***********************************************************************************************
     * See if the specified TaskPlugin is initialised.
     *
     * @param task
     * @return boolean
     */

    public final boolean isTaskInitialised(final TaskPlugin task)
        {
        return (task.getState() == TaskState.INITIALISED);
        }


    /***********************************************************************************************
     * See if the specified TaskPlugin is running.
     *
     * @param task
     * @return boolean
     */

    public final boolean isTaskRunning(final TaskPlugin task)
        {
        return (task.getState() == TaskState.RUNNING);
        }


    /***********************************************************************************************
     * See if the specified TaskPlugin is idle.
     *
     * @param task
     * @return boolean
     */

    public final boolean isTaskIdle(final TaskPlugin task)
        {
        return (task.getState() == TaskState.IDLE);
        }


    /***********************************************************************************************
     * See if the specified TaskPlugin is shut down.
     *
     * @param task
     * @return boolean
     */

    public final boolean isTaskShutdown(final TaskPlugin task)
        {
        return (task.getState() == TaskState.SHUTDOWN);
        }


    /***********************************************************************************************
     * The action performed when a Context Action runs this Atom.
     * This can be called from the navigation tree, a menu, or a toolbar button.
     *
     * @param event
     * @param browsemode
     */

    public final void actionPerformed(final AWTEvent event,
                                      final boolean browsemode)
        {
        // ActionPerformed must:
        //      use FrameworkManagerUIComponentPlugin to:
        //          clear the previous UI occupant
        //          set the new UI occupant
        //      use RegistryModelController to:
        //          setPluginState() or setTaskState()
        //      use UserObject to:
        //          record the new selection

        LOGGER.debugNavigation("START ATOM actionPerformed browsemode=" + browsemode);

        // Set the Browse mode of the RootData
        setBrowseMode(browsemode);

        if (getBrowseMode())
            {
            // Attempt to run the Atom Plugin with startPluginAndShowUI()
            // This clears the UIOccupant, and sets the new UIOccupant if the Plugin may be run
            // The Plugin may have other actions which do not require a UI
            REGISTRY_CONTROLLER.startPluginAndShowUI(this, REGISTRY_MODEL.getFrameworkManager());

            // Select the Plugin node on the navigation tree
            selectNodeOnTree(getHostTreeNode());
            }
        else
            {
            // We are in Edit mode
            // Check that there is a valid FrameworkManager and UI installed!
            if ((this.validatePlugin())
                && (REGISTRY_MODEL.getFrameworkManager() != null)
                && (REGISTRY_MODEL.getFrameworkManager().isRunning())
                && (REGISTRY_MODEL.getFrameworkManager().getUI() != null))
                {
                // Remove the previous occupant of the UI Panel
                REGISTRY_MODEL.getFrameworkManager().getUI().clearUIOccupant(this);

                // Install the appropriate Editor (this calls setUIOccupant())
                editUserObject();

                // Select the node on the navigation tree
                selectNodeOnTree(getHostTreeNode());
                }
            else
                {
                // Otherwise do nothing at all..
                NavigationUtilities.unableToPerformAction();
                }
            }

        LOGGER.debugNavigation("END ATOM actionPerformed()");
        LOGGER.debugNavigation("***************************************************************\n\n");
        }


    /***********************************************************************************************
     * The action performed when a Context Action stops this Atom.
     * This can be called from the navigation tree, a menu, or a toolbar button.
     *
     * @param event
     * @param browsemode
     */

    public void actionHalted(final AWTEvent event,
                             final boolean browsemode)
        {
        // ActionHalted must:
        //      use FrameworkManagerUIComponentPlugin to:
        //          clear the previous UI occupant
        //          set the new UI occupant to BlankUI
        //      use RegistryModelController to:
        //          setPluginState() or setTaskState()
        //      use UserObject to:
        //          record the new selection

        // Set the Browse mode of the RootData
        setBrowseMode(browsemode);

        // Check that there is a valid FrameworkManager and UI installed!
        if ((this.validatePlugin())
            && (REGISTRY_MODEL.getFrameworkManager() != null)
            && (REGISTRY_MODEL.getFrameworkManager().isRunning())
            && (REGISTRY_MODEL.getFrameworkManager().getUI() != null))
            {
            LOGGER.debugNavigation("ATOM actionHalted browsemode=" + browsemode);

            if (getBrowseMode())
                {
                // Remove the previous occupant of the UI Panel and show the default UIComponent
                REGISTRY_MODEL.getFrameworkManager().getUI().clearUIOccupant(new NullData());
                REGISTRY_MODEL.getFrameworkManager().getUI().setUIOccupant(new NullData());

                // Place the Plugin in STOPPED
                REGISTRY_CONTROLLER.setPluginState(this,
                                                   PluginState.STOPPED);

                // Select the node on the navigation tree
                selectNodeOnTree(getHostTreeNode());
                }
            else
                {
                final String [] strMessage =
                    {
                    MSG_STOP_PLUGIN_0,
                    MSG_STOP_PLUGIN_1
                    };

                // Trying to stop a Plugin while in Edit mode...
                JOptionPane.showMessageDialog(null,
                                              strMessage,
                                              getPathname() + SPACE + DIALOG_PLUGIN_CONTROL,
                                              JOptionPane.WARNING_MESSAGE);

                // Select the node on the navigation tree
                selectNodeOnTree(getHostTreeNode());
                }
            }
        else
            {
            // Otherwise do nothing at all..
            NavigationUtilities.unableToPerformAction();
            }
        }


    /***********************************************************************************************
     * Show some status text and an icon from the Plugin images set.
     *
     * @param status
     */

    public final void setStatus(final String status)
        {
        if (status != null)
            {
            final Icon icon;

            icon = RegistryModelUtilities.getAtomIcon(this, getIconFilename());
            setStatus(status, icon);
            }
        else
            {
            LOGGER.debug(".setResponseStatus() Cannot show status!");
            }
        }


    /***********************************************************************************************
     * Handle an Exception for this Atom.
     * This may be overridden if anything more than a simple log entry is required.
     *
     * @param exception
     * @param identifier
     * @param status
     */

    public final void handleException(final Exception exception,
                                      final String identifier,
                                      final EventStatus status)
        {
        LOGGER.handleAtomException(this,
                                   this.getRootTask(),
                                   getClass().getName(),
                                   exception,
                                   identifier,
                                   status);
        }


    /**********************************************************************************************/
    /* Events                                                                                     */
    /***********************************************************************************************
     * Notify all listeners of ActiveChangeEvents.
     *
     * @param objectsource
     * @param sourcename
     * @param oldvalue
     * @param newvalue
     * @param debugmode
     */

    public final void notifyActiveChangeEvent(final Object objectsource,
                                              final String sourcename,
                                              final boolean oldvalue,
                                              final boolean newvalue,
                                              final boolean debugmode)
        {
        final List<ActiveChangeListener> listeners;
        final ActiveChangeEvent activeChangeEvent;

        // Create a Thread-safe List of Listeners
        listeners = new CopyOnWriteArrayList<ActiveChangeListener>(this.vecListeners);

        // Create an ActiveChangeEvent
        activeChangeEvent = new ActiveChangeEvent(objectsource,
                                                  sourcename,
                                                  oldvalue,
                                                  newvalue,
                                                  debugmode);
        // Fire the event to every listener
        synchronized(listeners)
            {
            for (int i = 0; i < listeners.size(); i++)
                {
                final ActiveChangeListener activeChangeListener;

                activeChangeListener = listeners.get(i);
                activeChangeListener.activeUpdate(activeChangeEvent);
                }
            }
        }


    /***********************************************************************************************
     * Add a listener for this event.
     *
     * @param listener
     */

    public final void addActiveChangeListener(final ActiveChangeListener listener)
        {
        vecListeners.addElement(listener);
        }


    /***********************************************************************************************
     * Remove a listener for this event.
     *
     * @param listener
     */

    public final void removeActiveChangeListener(final ActiveChangeListener listener)
        {
        vecListeners.removeElement(listener);
        }
    }


//--------------------------------------------------------------------------------------------------
// End of File



/***********************************************************************************************
 * Write the complete set of Scripts for this AtomData
 */

//    public final boolean writeScripts(final AtomData atom,
//                                      final String pathname)
//        {
//        final File fileScript;
//        final FileWriter writerOutput;
//        final String strPathname;
//        Iterator iterComponents;
//
//        strPathname = pathname
//                        + System.getProperty("file.separator")
//                        + getName()
//                        + FILETYPE_SQL;
//
//        try
//            {
//            fileScript = new File(strPathname);
//
//            // Overwrite existing output file or create a new file
//            if (fileScript.exists())
//                {
//                fileScript.delete();
//                fileScript.createNewFile();
//                }
//            else
//                {
//                fileScript.createNewFile();
//                }
//
//            writerOutput = new FileWriter(fileScript);
//
//            // Write the Install Scripts to the file
//            writerOutput.write("# ----------------------------------------------------------");
//            writerOutput.write("\n# Install Scripts for " + atom.getName() + "\n\n");
//            createInstallScripts(writerOutput, atom);
//

//            if (atom instanceof ApplicationData)
//                {
//                // Step through all Application Components
//                iterComponents = ((ApplicationData)atom).getComponents();
//
//                while ((iterComponents != null)
//                    && (iterComponents.hasNext()))
//                    {
//                    final ComponentData componentData = (ComponentData) iterComponents.next();
//
//                    if (componentData != null)
//                        {
//                        writerOutput.write("# Install Scripts for Component " + componentData.getName() + "\n\n");
//                        createInstallScripts(writerOutput, componentData);
//                        }
//                    }
//                }
//
//            // Write the Uninstall Scripts to the file
//            writerOutput.write("# ----------------------------------------------------------");
//            writerOutput.write("\n# Uninstall Scripts for " + getName() + "\n\n");
//            createUninstallScripts(writerOutput, this);
//
//            if (atom instanceof ApplicationData)
//                {
//                // Step through all Application Components
//                iterComponents = ((ApplicationData)atom).getComponents();
//
//                while ((iterComponents != null)
//                    && (iterComponents.hasNext()))
//                    {
//                    final ComponentData componentData = (ComponentData) iterComponents.next();
//
//                    if (componentData != null)
//                        {
//                        writerOutput.write("# Uninstall Scripts for Component " + componentData.getName() + "\n\n");
//                        createUninstallScripts(writerOutput, componentData);
//                        }
//                    }
//                }
//
//            writerOutput.close();
//            }
//
//        catch (IOException exception)
//            {
//            return (false);
//            }
//
//        return (true);
//        }


//    /***********************************************************************************************
//     * Create all Install scripts for this AtomData.
//     *
//     * @param writerOutput
//     * @param atom
//     *
//     * @throws IOException
//     */
//
//    private static void createInstallScripts(final FileWriter writerOutput,
//                                             final AtomData atom) throws IOException
//        {
//        Iterator iterAtoms;
//
//        writerOutput.write("# INSERT " + atom.getName() + "\n");
//        //writerOutput.write(atom.createSQLInsertScript().toString() + "\n\n");
//
//        // Step through all Tasks
//        iterAtoms = atom.getTasks();
//
//        while ((iterAtoms != null)
//            && (iterAtoms.hasNext()))
//            {
//            final TaskData taskData = (TaskData) iterAtoms.next();
//
//            if (taskData != null)
//                {
//                writerOutput.write("# INSERT Task " + taskData.getName() + "\n");
//               // writerOutput.write(taskData.createSQLInsertScript().toString() + "\n\n");
//                }
//            }
//
//        // Step through all Properties
//        iterAtoms = atom.getProperties();
//
//        while ((iterAtoms != null)
//            && (iterAtoms.hasNext()))
//            {
//            final PropertyPlugin propertyPlugin = (PropertyPlugin) iterAtoms.next();
//
//            if (propertyPlugin != null)
//                {
//                writerOutput.write("# INSERT Property " + propertyPlugin.getPathname() + "\n");
//                //writerOutput.write(propertyData.createSQLInsertScript().toString() + "\n\n");
//                }
//            }
//
//        // Step through all Strings
//        iterAtoms = atom.getStrings();
//
//        while ((iterAtoms != null)
//            && (iterAtoms.hasNext()))
//            {
//            final StringPlugin stringPlugin = (StringPlugin) iterAtoms.next();
//
//            if (stringPlugin != null)
//                {
//                writerOutput.write("# INSERT String " + stringPlugin.getPathname() + "\n");
//                //writerOutput.write(stringData.createSQLInsertScript().toString() + "\n\n");
//                }
//            }
//
//        // Step through all Exceptions
//        iterAtoms = atom.getExceptions();
//
//        while ((iterAtoms != null)
//            && (iterAtoms.hasNext()))
//            {
//            final ExceptionPlugin exceptionPlugin = (ExceptionPlugin) iterAtoms.next();
//
//            if (exceptionPlugin != null)
//                {
//                writerOutput.write("# INSERT Exception " + exceptionPlugin.getPathname() + "\n");
//                //writerOutput.write(exceptionData.createSQLInsertScript().toString() + "\n\n");
//                }
//            }
//
//        // Step through all Queries
//        iterAtoms = atom.getQueries();
//
//        while ((iterAtoms != null)
//            && (iterAtoms.hasNext()))
//            {
//            final QueryPlugin queryPlugin = (QueryPlugin) iterAtoms.next();
//
//            if (queryPlugin != null)
//                {
//                writerOutput.write("# INSERT Query " + queryPlugin.getPathname() + "\n");
//                //writerOutput.write(queryData.createSQLInsertScript().toString() + "\n\n");
//                }
//            }
//        }
//
//
//    /***********************************************************************************************
//     * Create all Uninstall scripts for this AtomData.
//     *
//     * @param writerOutput
//     * @param atom
//     *
//     * @throws IOException
//     */
//
//    private static void createUninstallScripts(final FileWriter writerOutput,
//                                               final AtomData atom)
//        throws IOException
//        {
//        Iterator iterAtoms;
//
//        writerOutput.write("# DELETE " + atom.getName() + "\n");
////        writerOutput.write(atom.createSQLDeleteScript().toString() + "\n\n");
//
//        // Step through all Tasks
//        iterAtoms = atom.getTasks();
//
//        while ((iterAtoms != null)
//            && (iterAtoms.hasNext()))
//            {
//            final TaskData taskData = (TaskData) iterAtoms.next();
//
//            if (taskData != null)
//                {
//                writerOutput.write("# DELETE Task " + taskData.getName() + "\n");
//               // writerOutput.write(taskData.createSQLDeleteScript().toString() + "\n\n");
//                }
//            }
//
//        // Step through all Properties
//        iterAtoms = atom.getProperties();
//
//        while ((iterAtoms != null)
//            && (iterAtoms.hasNext()))
//            {
//            final PropertyPlugin propertyPlugin = (PropertyPlugin) iterAtoms.next();
//
//            if (propertyPlugin != null)
//                {
//                writerOutput.write("# DELETE Property " + propertyPlugin.getPathname() + "\n");
////                writerOutput.write(propertyData.createSQLDeleteScript().toString() + "\n\n");
//                }
//            }
//
//        // Step through all Strings
//        iterAtoms = atom.getStrings();
//
//        while ((iterAtoms != null)
//            && (iterAtoms.hasNext()))
//            {
//            final StringPlugin stringPlugin = (StringPlugin) iterAtoms.next();
//
//            if (stringPlugin != null)
//                {
//                writerOutput.write("# DELETE String " + stringPlugin.getPathname() + "\n");
//                //writerOutput.write(stringData.createSQLDeleteScript().toString() + "\n\n");
//                }
//            }
//
//        // Step through all Exceptions
//        iterAtoms = atom.getExceptions();
//
//        while ((iterAtoms != null)
//            && (iterAtoms.hasNext()))
//            {
//            final ExceptionPlugin exceptionPlugin = (ExceptionPlugin) iterAtoms.next();
//
//            if (exceptionPlugin != null)
//                {
//                writerOutput.write("# DELETE Exception " + exceptionPlugin.getPathname() + "\n");
//                //writerOutput.write(exceptionData.createSQLDeleteScript().toString() + "\n\n");
//                }
//            }
//
//        // Step through all Queries
//        iterAtoms = atom.getQueries();
//
//        while ((iterAtoms != null)
//            && (iterAtoms.hasNext()))
//            {
//            final QueryPlugin queryPlugin = (QueryPlugin) iterAtoms.next();
//
//            if (queryPlugin != null)
//                {
//                writerOutput.write("# DELETE Query " + queryPlugin.getPathname() + "\n");
//                //writerOutput.write(queryData.createSQLDeleteScript().toString() + "\n\n");
//                }
//            }
//        }
//
