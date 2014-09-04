//--------------------------------------------------------------------------------------------------
// Revision History
//
//  23-01-01    LMN created file
//  28-01-01    LMN added functionality...
//  05-02-02    LMN finished the first complete version!
//  21-02-02    LMN added ActiveChangeEvent
//  04-03-02    LMN added TimingMode
//  15-04-03    LMN changed for Icon column
//  19-04-03    LMN added link back to host DefaultMutableTreeNode
//  24-04-03    LMN incorporated TaskTaskData
//  25-04-03    LMN added setInstrumentState() etc.
//  03-05-03    LMN changed Tasks table
//  15-05-03    LMN added TaskAction
//  17-05-03    LMN converting for Task initialise(), run() and shutdown()
//  23-05-03    LMN added visibleTask(), idleTask()
//  25-05-03    LMN rewrote to allow ComponentTask and ApplicationTask
//  25-06-03    LMN added Context Menus
//  11-11-03    LMN tidying up
//  11-11-03    LMN extracted class RootData
//  26-10-04    LMN moved column names in from subclasses
//  15-11-04    LMN added script creators
//  18-01-06    LMN making major changes for plugins...
//  17-05-06    LMN finalising for XmlBeans
//
//--------------------------------------------------------------------------------------------------

package org.lmn.fc.model.tasks.impl;

//--------------------------------------------------------------------------------------------------
// Imports

import org.lmn.fc.common.events.ActiveChangeEvent;
import org.lmn.fc.common.events.ActiveChangeListener;
import org.lmn.fc.common.exceptions.FrameworkException;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.plugins.AtomPlugin;
import org.lmn.fc.model.plugins.impl.NullData;
import org.lmn.fc.model.registry.NavigationUtilities;
import org.lmn.fc.model.registry.RegistryModelPlugin;
import org.lmn.fc.model.registry.RegistryModelUtilities;
import org.lmn.fc.model.root.RootType;
import org.lmn.fc.model.root.impl.UserObject;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.model.tasks.TaskState;
import org.lmn.fc.model.users.RolePlugin;
import org.lmn.fc.model.users.impl.RoleData;
import org.lmn.fc.model.xmlbeans.roles.RoleName;
import org.lmn.fc.model.xmlbeans.tasks.Task;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;


/***************************************************************************************************
 * TaskData.
 */

public abstract class TaskData extends UserObject
                               implements TaskPlugin
    {
    // String Resources
    private static final String DIALOG_TASK_CONTROL = "Task Control";
    private static final String MSG_STOP_TASK_0 = "You may not stop a running Task while using its Editor";
    private static final String MSG_STOP_TASK_1 = "Please leave the Editor and try again";

    // The parent Atom
    private AtomPlugin pluginParent;

    // The UserRoles permitted to use this Task
    private List<RolePlugin> listRoles;

    // Task status
    private boolean boolRootTask;
    private TaskState enumTaskState;
    private boolean boolLocked;

    // Task timing
//    private GregorianCalendar calendarStartTime;
//    private GregorianCalendar calendarStopTime;

    // Event Listeners for changes in the Active state
    private Vector<ActiveChangeListener> vecListeners;


    /***********************************************************************************************
     * Assign RolePlugins to the specified Task, as defined by the RoleNames in the Task description.
     * The corresponding RolePlugins must exist in the Registry.
     *
     * @param plugin
     */

    public static void assignRoles(final TaskPlugin plugin)
        {
        if ((plugin != null)
            && (plugin.validatePlugin())
            && (REGISTRY.getRoles() != null))
            {
            final List<RoleName.Enum> listRoleNames;
            String strRoleKey;

            // Read the list of RoleNames applicable to this Task
            listRoleNames = plugin.getRoleNames();

            for (final RoleName.Enum name : listRoleNames)
                {
                // Check that the RoleName corresponds to a Role in the Registry
                strRoleKey = PREFIX_ROLE + KEY_DELIMITER + name.toString();

                if (REGISTRY.getRoles().containsKey(strRoleKey))
                    {
                    // Assign the Role to the Task
                    plugin.addUserRole(REGISTRY.getRole(strRoleKey));
                    }
                }
            }
        else
            {
            LOGGER.error("Unable to assign Task Roles");
            }
        }


    /***********************************************************************************************
     * Construct a Task for a parent Atom.
     *
     * @param id
     * @param atom
     */

    public TaskData(final long id,
                    final AtomPlugin atom)
        {
        super(id);

        if (atom == null)
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_NULL);
            }

        this.setType(RootType.TASK);
        this.setParentAtom(atom);
        this.setLevel(atom.getLevel());
        this.setRootTask(false);

        this.setXml(null);
        initialiseNonXmlTaskData();
        initialiseListeners();
        }


    /***********************************************************************************************
     * Construct a TaskData.
     *
     * @param id
     */

    public TaskData(final long id)
        {
        super(id);

        this.setType(RootType.TASK);
        this.setParentAtom(null);
        this.setLevel(null);
        this.setRootTask(false);

        this.setXml(null);
        initialiseNonXmlTaskData();
        initialiseListeners();
        }


  /***********************************************************************************************
   * Initialise the TaskData object, and link to its host node.
   */

    private void initialiseNonXmlTaskData()
        {
        this.listRoles = new ArrayList<RolePlugin>(REGISTRY.getRoles().size());

        this.boolRootTask = false;
        this.enumTaskState = TaskState.CREATED;
        this.boolLocked = false;

        // Bidirectionally link the TaskData to the host node
        getHostTreeNode().setUserObject(this);

//        this.calendarStartTime = null;
//        this.calendarStopTime = null;
        }


    /***********************************************************************************************
     * Initialise all Listeners.
     */

    private synchronized void initialiseListeners()
        {
        this.vecListeners = new Vector<ActiveChangeListener>(5);
        }


    /***********************************************************************************************
     * Get the Atom parent of this Task.
     *
     * @return AtomPlugin
     */

    public final AtomPlugin getParentAtom()
        {
        return (this.pluginParent);
        }


    /***********************************************************************************************
     * Set the Atom parent of this Task.
     *
     * @param atom
     */

    public final void setParentAtom(final AtomPlugin atom)
        {
        this.pluginParent = atom;
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
     * Add a UserRole to the List of Roles supported by this Task.
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
     * Get the full pathname for the Task.
     *
     * @return String
     */

     public final String getPathname()
         {
         return (getParentAtom().getResourceKey() + getName());
         }


    /***********************************************************************************************
     * Get the Registry ResourceKey for the Task.
     *
     * @return String
     */

    public final String getResourceKey()
        {
        return (getPathname() + RegistryModelPlugin.DELIMITER_RESOURCE);
        }


    /**********************************************************************************************/
    /* TaskPlugin implementations                                                                 */
    /***********************************************************************************************
     * Get the TaskName field.
     *
     * @return String
     */

    public final String getName()
        {
        return (getXml().getName());
        }


    /***********************************************************************************************
     * Set the TaskName field.
     *
     * @param name
     */

    public final void setName(final String name)
        {
        getXml().setName(name);
        updateRoot();
        }


    /***********************************************************************************************
     * Get the List of RoleNames.
     *
     * @return String
     */

    public final List<RoleName.Enum> getRoleNames()
        {
        return (getXml().getRoleNameList());
        }


    /***********************************************************************************************
     * Set the array of RoleNames.
     *
     * @param rolenames
     */

    public final void setRoleNames(final RoleName.Enum[] rolenames)
        {
        // This is a bit odd, should be deprecated, but there is no setRoleNameList()
        getXml().setRoleNameArray(rolenames);
        updateRoot();
        }


    /***********************************************************************************************
     * Get the SortOrder field.
     *
     * @return int
     */

    public final short getSortOrder()
        {
        return (getXml().getSortOrder());
        }


    /***********************************************************************************************
     * Set the SortOrder field.
     *
     * @param sortorder
     */

    public final void setSortOrder(final short sortorder)
        {
        getXml().setSortOrder(sortorder);
        updateRoot();
        }


    /**********************************************************************************************
     * Get the Editable field.
     *
     * @return boolean
     */

    public final boolean isEditable()
        {
        return (getXml().getEditable());
        }


    /**********************************************************************************************
     * Set the Editable field.
     *
     * @param editable
     */
    public final void setEditable(final boolean editable)
        {
        getXml().setEditable(editable);
        updateRoot();
        }


    /***********************************************************************************************
     * Get the Public status for this Task.
     *
     * @return boolean
     */

    public final boolean isPublic()
        {
        return (getXml().getPublic());
        }


    /***********************************************************************************************
     * Set the Public status for this Task.
     * This should not really be used except when constructing.
     *
     * @param status
     */

    public final void setPublic(final boolean status)
        {
        getXml().setPublic(status);
        updateRoot();
        }


    /***********************************************************************************************
     * Get the Runnable status for this Task.
     *
     * @return boolean
     */

    public final boolean isRunnable()
        {
        return (getXml().getRunnable());
        }


    /***********************************************************************************************
     * Set the Runnable status for this Task.
     * This should not really be used except when constructing..
     *
     * @param status
     */

    public final void setRunnable(final boolean status)
        {
        getXml().setRunnable(status);
        updateRoot();
        }


    /***********************************************************************************************
     * Get the RunAtStart status for this Task.
     *
     * @return boolean
     */

    public final boolean isRunAtStart()
        {
        return (getXml().getRunAtStart());
        }


    /***********************************************************************************************
     * Set the RunAtStart status for this Task.
     * This should not really be used except when constructing..
     *
     * @param status
     */

    public final void setRunAtStart(final boolean status)
        {
        getXml().setRunAtStart(status);
        updateRoot();
        }


    /***********************************************************************************************
     * Get the TaskActive field.
     *
     * @return boolean
     */

    public final boolean isActive()
        {
        return (getXml().getActive());
        }


    /***********************************************************************************************
     * Set the TaskActive field.
     *
     * @param active
     */

    public final void setActive(final boolean active)
        {
        // Tell everyone that the Active flag has changed
        if (isUpdateAllowed())
            {
            notifyActiveChangeEvent(this,
                                    getPathname(),
                                    isActive(),
                                    active,
                                    getDebugMode());
            }

        getXml().setActive(active);
        updateRoot();
        }


    /**********************************************************************************************
     * Get the class name of the Editor.
     *
     * @return String
     */

    public final String getEditorClassname()
        {
        return (getXml().getEditorClassname());
        }


    /**********************************************************************************************
     * Set the class name of the Editor.
     *
     * @param classname
     */

    public final void setEditorClassname(final String classname)
        {
        getXml().setEditorClassname(classname);
        updateRoot();
        }


    /**********************************************************************************************
     * Get the Description field.
     *
     * @return String
     */

    public final String getDescription()
        {
        return (getXml().getDescription());
        }


    /**********************************************************************************************
     * Set the Description field.
     *
     * @param description
     */

    public final void setDescription(final String description)
        {
        getXml().setDescription(description);
        updateRoot();
        }


    /***********************************************************************************************
     * Get the Task IconFilename field.
     *
     * @return String
     */

    public final String getIconFilename()
        {
        return (getXml().getIconFilename());
        }


    /**********************************************************************************************
     * Set the Task IconFilename field.
     *
     * @param filename
     */

    public final void setIconFilename(final String filename)
        {
        getXml().setIconFilename(filename);
        updateRoot();
        }


    /**********************************************************************************************/
    /* XML Persistence Dates and Times                                                            */
    /***********************************************************************************************
     * Get the StartDate for this Task.
     *
     * @return GregorianCalendar
     */

    public final GregorianCalendar getStartDate()
        {
        GregorianCalendar calendar;

        calendar = null;

        if (getXml().getStartDate() != null)
            {
            // Initialise to time NOW, with the Framework TimeZone and Locale
            calendar = getFrameworkCalendar();
            calendar.setTimeInMillis(getXml().getStartDate().getTimeInMillis());
            }

        return (calendar);
        }


    /***********************************************************************************************
     * Set the StartDate for this Task.
     *
     * @param calendar
     */

    public final void setStartDate(final GregorianCalendar calendar)
        {
        getXml().setStartDate(calendar);
        updateRoot();
        }


    /***********************************************************************************************
     * Get the StartTime for this Task.
     *
     * @return GregorianCalendar
     */

    public final GregorianCalendar getStartTime()
        {
        GregorianCalendar calendar;

        calendar = null;

        if (getXml().getStartTime() != null)
            {
            // Initialise to time NOW, with the Framework TimeZone and Locale
            calendar = getFrameworkCalendar();
            calendar.setTimeInMillis(getXml().getStartTime().getTimeInMillis());
            }

        return (calendar);
        }


    /***********************************************************************************************
     * Set the StartTime for this Task.
     *
     * @param calendar
     */

    public final void setStartTime(final GregorianCalendar calendar)
        {
        getXml().setStartTime(calendar);
        updateRoot();
        }


    /***********************************************************************************************
     * Get the StopDate for this Task.
     *
     * @return GregorianCalendar
     */

    public final GregorianCalendar getStopDate()
        {
        GregorianCalendar calendar;

        calendar = null;

        if (getXml().getStopDate() != null)
            {
            // Initialise to time NOW, with the Framework TimeZone and Locale
            calendar = getFrameworkCalendar();
            calendar.setTimeInMillis(getXml().getStopDate().getTimeInMillis());
            }

        return (calendar);
        }


    /***********************************************************************************************
     * Set the StopDate for this Task.
     *
     * @param calendar
     */

    public final void setStopDate(final GregorianCalendar calendar)
        {
        getXml().setStopDate(calendar);
        updateRoot();
        }


    /***********************************************************************************************
     * Get the StopTime for this Task.
     *
     * @return GregorianCalendar
     */

    public final GregorianCalendar getStopTime()
        {
        GregorianCalendar calendar;

        calendar = null;

        if (getXml().getStopTime() != null)
            {
            // Initialise to time NOW, with the Framework TimeZone and Locale
            calendar = getFrameworkCalendar();
            calendar.setTimeInMillis(getXml().getStopTime().getTimeInMillis());
            }

        return (calendar);
        }


    /***********************************************************************************************
     * Set the StopTime for this Task.
     *
     * @param calendar
     */

    public final void setStopTime(final GregorianCalendar calendar)
        {
        getXml().setStopTime(calendar);
        updateRoot();
        }


    /***********************************************************************************************
     * Get the DateCreated field.
     *
     * @return Date
     */

    public final GregorianCalendar getCreatedDate()
        {
        final GregorianCalendar calendar;

        calendar = getFrameworkCalendar();

        if (getXml().getCreatedDate() != null)
            {
            calendar.setTimeInMillis(getXml().getCreatedDate().getTimeInMillis());
            }

        return (calendar);
        }


    /***********************************************************************************************
     * Set the DateCreated field.
     *
     * @param calendar
     */

    public final void setCreatedDate(final GregorianCalendar calendar)
        {
        getXml().setCreatedDate(calendar);
        updateRoot();
        }


    /***********************************************************************************************
     * Get the TimeCreated field.
     *
     * @return Time
     */

    public final GregorianCalendar getCreatedTime()
        {
        final GregorianCalendar calendar;

        calendar = getFrameworkCalendar();

        if (getXml().getCreatedTime() != null)
            {
            calendar.setTimeInMillis(getXml().getCreatedTime().getTimeInMillis());
            }

        return (calendar);
        }


    /***********************************************************************************************
     * Set the TimeCreated field.
     *
     * @param calendar
     */

    public final void setCreatedTime(final GregorianCalendar calendar)
        {
        getXml().setCreatedTime(calendar);
        updateRoot();
        }


    /***********************************************************************************************
     * Get the DateModified field.
     *
     * @return Date
     */

    public final GregorianCalendar getModifiedDate()
        {
        final GregorianCalendar calendar;

        calendar = getFrameworkCalendar();

        if (getXml().getModifiedDate() != null)
            {
            calendar.setTimeInMillis(getXml().getModifiedDate().getTimeInMillis());
            }

        return (calendar);
        }


    /***********************************************************************************************
     * Set the DateModified field.
     *
     * @param calendar
     */

    public final void setModifiedDate(final GregorianCalendar calendar)
        {
        // Do not mark the RootData as updated if this item changes!
        getXml().setModifiedDate(calendar);
        }


    /***********************************************************************************************
     * Get the TimeModified field.
     *
     * @return Time
     */

    public final GregorianCalendar getModifiedTime()
        {
        final GregorianCalendar calendar;

        calendar = getFrameworkCalendar();

        if (getXml().getModifiedTime() != null)
            {
            calendar.setTimeInMillis(getXml().getModifiedTime().getTimeInMillis());
            }

        return (calendar);
        }


    /***********************************************************************************************
     * Set the TimeModified field.
     *
     * @param calendar
     */

    public final void setModifiedTime(final GregorianCalendar calendar)
        {
        // Do not mark the RootData as updated if this item changes!
        getXml().setModifiedTime(calendar);
        }


    /**********************************************************************************************/
    /* Task Management                                                                            */
    /***********************************************************************************************
     * Get a flag to indicate if this is the Root Task.
     *
     * @return boolean
     */

    public final boolean isRootTask()
        {
        return (this.boolRootTask);
        }


    /***********************************************************************************************
     * Set a flag to indicate if this is the Root Task.
     *
     * @param flag
     */

    public final void setRootTask(final boolean flag)
        {
        this.boolRootTask = flag;
        }


    /***********************************************************************************************
     * Get the TaskLocked field.
     *
     * @return boolean
     */

    public final boolean isLocked()
        {
        return (this.boolLocked);
        }


    /***********************************************************************************************
     * Set the TaskLocked field.
     *
     * @param locked
     */

    public final void setLocked(final boolean locked)
        {
        this.boolLocked = locked;
        }


    /***********************************************************************************************
     * Get the Task state.
     *
     * @return TaskState
     */

    public final TaskState getState()
        {
        return (this.enumTaskState);
        }


    /***********************************************************************************************
     * Set the Task state.
     *
     * @param state
     *
     * @return boolean
     */

    public final boolean setState(final TaskState state)
        {
        this.enumTaskState = state;

        return (true);
        }


    /***********************************************************************************************
     * Return a flag to indicate if this Task is Running.
     * To be used by the MonitoredItem interface.
     *
     * @return boolean
     */

    public final boolean isRunning()
        {
        return (TaskState.RUNNING).equals(getState());
        }


    /**********************************************************************************************/
    /* Miscellaneous                                                                              */
    /***********************************************************************************************
     * Handle an Exception for this Task.
     * This may be overridden if anything more than a simple log entry is required.
     *
     * @param exception
     * @param identifier
     * @param status
     */

    public void handleException(final Exception exception,
                                final String identifier,
                                final EventStatus status)
        {
        LOGGER.handleAtomException(this.getParentAtom(),
                                   this,
                                   getClass().getName(),
                                   exception,
                                   identifier,
                                   status);
        }


    /***********************************************************************************************
     * Show some status text and an icon from the default Common set.
     *
     * @param status
     */

    public final void setStatus(final String status)
        {
        if (status != null)
            {
            final Icon icon;

            icon = RegistryModelUtilities.getCommonIcon(getIconFilename());
            setStatus(status, icon);
            }
        else
            {
            LOGGER.debug(".setResponseStatus() Cannot show Task status!");
            }
        }


    /***********************************************************************************************
     * Fully debug the Task.
     */

    public final void showDebugData()
        {
        if (getDebugMode())
            {
            LOGGER.log("Task");
            LOGGER.log(INDENT + "[ID=" + getID() + "]");
            LOGGER.log(INDENT + "[Level=" + getLevel() + "]");
            LOGGER.log(INDENT + "[Name=" + getName() + "]");
            RoleData.showRoles(getUserRoles());
            LOGGER.log(INDENT + "[SortOrder=" + getSortOrder() + "]");
            LOGGER.log(INDENT + "[Editable=" + isEditable() + "]");
            LOGGER.log(INDENT + "[Public=" + isPublic() + "]");
            LOGGER.log(INDENT + "[Runnable=" + isRunnable() + "]");
            LOGGER.log(INDENT + "[RunAtStart=" + isRunAtStart() + "]");
            LOGGER.log(INDENT + "[Active=" + isActive() + "]");
            LOGGER.log(INDENT + "[EditorClassname=" + getEditorClassname() + "]");
            LOGGER.log(INDENT + "[Description=" + getDescription() + "]");
            LOGGER.log(INDENT + "[IconFilename=" + getIconFilename() + "]");

            LOGGER.log(INDENT + "[Parent=" + getParentAtom().getName() + "]");
            LOGGER.log(INDENT + "[Pathname=" + getPathname() + "]");
            LOGGER.log(INDENT + "[ResourceKey=" + getResourceKey() + "]");
            LOGGER.log(INDENT + "[ClassFound=" + isClassFound() + "]");
            LOGGER.log(INDENT + "[State=" + getState() + "]");
            LOGGER.log(INDENT + "[RootTask=" + isRootTask() + "]");
            LOGGER.log(INDENT + "[Locked=" + isLocked() + "]");
            LOGGER.log(INDENT + "[Running=" + isRunning() + "]");
            LOGGER.log(INDENT + "[Updated=" + isUpdated() + "]");
            LOGGER.log(INDENT + "[UpdateAllowed=" + isUpdateAllowed() + "]");
            LOGGER.log(INDENT + "[DebugMode=" + getDebugMode() + "]");

            LOGGER.log(INDENT + "[StartDate=" + getStartDate() + "]");
            LOGGER.log(INDENT + "[StartTime=" + getStartTime() + "]");
            LOGGER.log(INDENT + "[StopDate=" + getStopDate() + "]");
            LOGGER.log(INDENT + "[StopTime=" + getStopTime() + "]");
            LOGGER.log(INDENT + "[CreatedDate=" + getCreatedDate() + "]");
            LOGGER.log(INDENT + "[CreatedTime=" + getCreatedTime() + "]");
            LOGGER.log(INDENT + "[ModifiedDate=" + getModifiedDate() + "]");
            LOGGER.log(INDENT + "[ModifiedTime=" + getModifiedTime() + "]");
            }
        }


    /***********************************************************************************************
     * Override toString() to provide the Task name which appears on the navigation tree.
     *
     * @return String
     */

    public String toString()
        {
        return(getName());
        }


    /**********************************************************************************************/
    /* Events                                                                                     */
    /***********************************************************************************************
     * The action performed when a Context Action runs this Task.
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

        LOGGER.debugNavigation("START TASK actionPerformed() browsemode=" + browsemode);

        // Set the Browse mode of the RootData
        setBrowseMode(browsemode);

        if (getBrowseMode())
            {
            // Attempt to run the Task with runTask()
            // This clears the UIOccupant, and sets the new UIOccupant if the Task may be run
            // The Task may have other actions which do not require a UI
            REGISTRY_CONTROLLER.runTaskAndShowUI(this, REGISTRY_MODEL.getFrameworkManager());

            // Select the node on the navigation tree
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

        LOGGER.debugNavigation("END TASK actionPerformed()");
        LOGGER.debugNavigation("***************************************************************\n\n");
        }


    /***********************************************************************************************
     * The action performed when a Context Action stops this Task.
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
            LOGGER.debugNavigation("TASK actionHalted browsemode=" + browsemode);

            if (getBrowseMode())
                {
                // Remove the previous occupant of the UI Panel
                REGISTRY_MODEL.getFrameworkManager().getUI().clearUIOccupant(new NullData());
                REGISTRY_MODEL.getFrameworkManager().getUI().setUIOccupant(new NullData());

                // Place the Task in STATE_TASK_IDLE
                REGISTRY_CONTROLLER.setTaskState(this.getParentAtom(),
                                              this,
                                              TaskState.IDLE);

                // Select the node on the navigation tree
                selectNodeOnTree(getHostTreeNode());
                }
            else
                {
                final String [] strMessage =
                    {
                    MSG_STOP_TASK_0,
                    MSG_STOP_TASK_1
                    };

                // Trying to stop a Task while in Edit mode...
                JOptionPane.showMessageDialog(null,
                                              strMessage,
                                              getPathname() + SPACE + DIALOG_TASK_CONTROL,
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
     * Notify all listeners of ActiveChangeEvents.
     *
     * @param objectsource
     * @param sourcename
     * @param oldvalue
     * @param newvalue
     * @param debugmode
     */

    private void notifyActiveChangeEvent(final Object objectsource,
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
     * Add a listener for ActiveChangeEvents.
     *
     * @param listener
     */

    public final synchronized void addActiveChangeListener(final ActiveChangeListener listener)
        {
        vecListeners.addElement(listener);
        }


    /***********************************************************************************************
     * Remove a listener for ActiveChangeEvents.
     *
     * @param listener
     */

    public final synchronized void removeActiveChangeListener(final ActiveChangeListener listener)
        {
        vecListeners.removeElement(listener);
        }


    /***********************************************************************************************
     * Get the XML part of the Task.
     *
     * @return XmlObject
     */

     public final Task getXml()
         {
         if (super.getXml() == null)
             {
             throw new FrameworkException(EXCEPTION_PARAMETER_NULL);
             }

         return ((Task)super.getXml());
         }
    }


//--------------------------------------------------------------------------------------------------
// End of File
