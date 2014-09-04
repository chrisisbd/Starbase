package org.lmn.fc.model.plugins;

import org.lmn.fc.common.events.ActiveChangeListener;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.resources.ExceptionPlugin;
import org.lmn.fc.model.resources.PropertyPlugin;
import org.lmn.fc.model.resources.QueryPlugin;
import org.lmn.fc.model.resources.ResourcePlugin;
import org.lmn.fc.model.root.RootPlugin;
import org.lmn.fc.model.root.UserObjectPlugin;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.model.tasks.TaskState;
import org.lmn.fc.model.users.RolePlugin;
import org.lmn.fc.model.xmlbeans.roles.RoleName;
import org.lmn.fc.ui.UIComponentPlugin;

import javax.management.ObjectName;
import javax.swing.tree.DefaultMutableTreeNode;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Vector;


public interface AtomPlugin extends UserObjectPlugin
    {
    // String Resources
    String PLUGINS_ICON = "plugins.png";

    //----------------------------------------------------------------------------------------------
    // Atoms

    ObjectName getObjectName();

    RootPlugin getParentAtom();

    void setParentAtom(RootPlugin atom);

    void addAtom(RootPlugin atom);

    void removeAtom(RootPlugin atom);

    void clearAtoms();

    Vector<RootPlugin> getAtoms();

    void showAttachedAtoms(boolean show);

    //----------------------------------------------------------------------------------------------
    // Roles

    List<RolePlugin> getUserRoles();

    void setUserRoles(List<RolePlugin> roles);

    void addUserRole(RolePlugin role);

    List<RoleName.Enum> getRoleNames();

    void setRoleNames(RoleName.Enum[] rolenames);

    //----------------------------------------------------------------------------------------------
    // Tasks

    void addTask(RootPlugin task);

    void removeTask(RootPlugin task);

    void clearTasks();

    Vector<RootPlugin> getTasks();

    void updateTasks();

    void showAttachedTasks(boolean show);

    TaskState getTaskState(TaskPlugin task);

    boolean isTaskPublic(TaskPlugin task);

    boolean isTaskRunnable(TaskPlugin task);

    boolean isTaskInitialised(TaskPlugin task);

    boolean isTaskRunning(TaskPlugin task);

    boolean isTaskIdle(TaskPlugin task);

    boolean isTaskShutdown(TaskPlugin task);

    // ToDo should this be TaskPlugin?
    RootPlugin getRootTask();

    void setRootTask(RootPlugin plugin);

    //----------------------------------------------------------------------------------------------
    // Resources

    void addResource(ResourcePlugin resource);

    void addProperty(ResourcePlugin property);

    void showAttachedProperties(boolean show);

    void addString(ResourcePlugin string);

    void showAttachedStrings(boolean show);

    void addException(ResourcePlugin exception);

    void showAttachedExceptions(boolean show);

    void addQuery(ResourcePlugin query);

    void showAttachedQueries(boolean show);

    void readResources();

    //----------------------------------------------------------------------------------------------
    // Atom State

    boolean startupAtom();

    boolean shutdownAtom();

    PluginState getState();

    boolean setState(PluginState state);

    boolean isRunning();

    boolean isLocked();

    void setLocked(boolean locked);


    /***********************************************************************************************
     * Get the VersionNumber.
     *
     * @return String
     */

    String getVersionNumber();


    /***********************************************************************************************
     * Set the VersionNumber.
     *
     * @param versionnumber
     */

    void setVersionNumber(final String versionnumber);


    /***********************************************************************************************
     * Get the BuildNumber.
     *
     * @return String
     */

    String getBuildNumber();


    /***********************************************************************************************
     * Set the BuildNumber.
     *
     * @param buildnumber
     */

    void setBuildNumber(final String buildnumber);


    /***********************************************************************************************
     * Get the BuildStatus.
     *
     * @return String
     */

    String getBuildStatus();


    /***********************************************************************************************
     * Set the BuildStatus.
     *
     * @param buildstatus
     */

    void setBuildStatus(final String buildstatus);


    //----------------------------------------------------------------------------------------------
    // XML Persistence Dates and Times (See also TaskPlugin)

    GregorianCalendar getStartDate();

    void setStartDate(GregorianCalendar calendar);

    GregorianCalendar getStartTime();

    void setStartTime(GregorianCalendar calendar);

    GregorianCalendar getStopDate();

    void setStopDate(GregorianCalendar calendar);

    GregorianCalendar getStopTime();

    void setStopTime(GregorianCalendar calendar);

    //----------------------------------------------------------------------------------------------
    // Registry Model Nodes

    void addMainExpanders();

    DefaultMutableTreeNode getPluginExpander();

    DefaultMutableTreeNode getTaskExpander();

    DefaultMutableTreeNode getPropertyExpander();

    DefaultMutableTreeNode getStringExpander();

    DefaultMutableTreeNode getExceptionExpander();

    DefaultMutableTreeNode getQueryExpander();

    //----------------------------------------------------------------------------------------------
    // XML Persistence

    /***********************************************************************************************
     *
     * @return boolean
     */

    boolean isLoadAtStart();


    /***********************************************************************************************
     *
     * @param load
     */

    void setLoadAtStart(boolean load);


    /***********************************************************************************************
     * Get the HelpFilename field.
     *
     * @return String
     */

    String getHelpFilename();


    /***********************************************************************************************
     * Set the HelpFilename field.
     *
     * @param filename
     */

    void setHelpFilename(String filename);


    /***********************************************************************************************
     * Get the filename of the Atom AboutBox HTML file.
     *
     * @return String
     */

    String getAboutFilename();


    /***********************************************************************************************
     * Set the filename of the Framework AboutBox HTML file.
     *
     * @param filename
     */

    void setAboutFilename(String filename);


    //----------------------------------------------------------------------------------------------
    // Database persistence




    //----------------------------------------------------------------------------------------------
    // User Interface

    String getTooltip();

    UIComponentPlugin aboutBox();

    //----------------------------------------------------------------------------------------------
    // Resources

    void removeProperty(PropertyPlugin property);

    void clearProperties();

    Vector<ResourcePlugin> getProperties();

    void removeString(ResourcePlugin string);

    void clearStrings();

    Vector<ResourcePlugin> getStrings();

    void removeException(ExceptionPlugin exception);

    void clearExceptions();

    Vector<ResourcePlugin> getExceptions();

    void removeQuery(QueryPlugin query);

    void clearQueries();

    Vector<ResourcePlugin> getQueries();

    //----------------------------------------------------------------------------------------------
    // Miscellaneous

    void handleException(Exception exception,
                         String identifier,
                         EventStatus status);

    void notifyActiveChangeEvent(Object objectsource,
                                 String sourcename,
                                 boolean oldvalue,
                                 boolean newvalue,
                                 boolean debugmode);

    void addActiveChangeListener(ActiveChangeListener listener);

    void removeActiveChangeListener(ActiveChangeListener listener);
    }
