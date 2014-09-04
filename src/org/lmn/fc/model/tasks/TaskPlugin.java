//--------------------------------------------------------------------------------------------------
// Revision History
//
//  15-05-03    LMN created file
//  23-05-03    LMN added visibleTask(), idleTask()
//  25-05-03    LMN added RegistryModel.getInstance()
//  25-06-03    LMN added Context Menus
//  14-10-03    LMN extending for abstract TaskData
//  10-01-06    LMN changing for plugins...
//  18-05-06    LMN finalising for XmlBeans
//
//--------------------------------------------------------------------------------------------------

package org.lmn.fc.model.tasks;

//--------------------------------------------------------------------------------------------------
// Imports

import org.lmn.fc.common.events.ActiveChangeListener;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.plugins.AtomPlugin;
import org.lmn.fc.model.users.RolePlugin;
import org.lmn.fc.model.xmlbeans.roles.RoleName;
import org.lmn.fc.model.root.UserObjectPlugin;

import java.util.GregorianCalendar;
import java.util.List;


//--------------------------------------------------------------------------------------------------

public interface TaskPlugin extends UserObjectPlugin
    {
    // Some fairly arbitrary size limits
    int MAX_TASKS = 20;

    // String Resources
    String TASKS_ICON = "tasks.png";

    //----------------------------------------------------------------------------------------------
    // Atoms

    AtomPlugin getParentAtom();

    void setParentAtom(AtomPlugin atom);

    //----------------------------------------------------------------------------------------------
    // Roles

    List<RolePlugin> getUserRoles();

    void setUserRoles(List<RolePlugin> roles);

    void addUserRole(RolePlugin role);

    List<RoleName.Enum> getRoleNames();

    void setRoleNames(RoleName.Enum[] rolenames);

    //----------------------------------------------------------------------------------------------
    // Task Configuration

    boolean isRootTask();

    void setRootTask(boolean flag);

    boolean isPublic();

    void setPublic(boolean flag);

    boolean isRunnable();

    void setRunnable(boolean status);

    boolean isRunAtStart();

    void setRunAtStart(final boolean status);

    //----------------------------------------------------------------------------------------------
    // XML Persistence Dates and Times (See also AtomPlugin)

    GregorianCalendar getStartDate();

    void setStartDate(GregorianCalendar calendar);

    GregorianCalendar getStartTime();

    void setStartTime(GregorianCalendar calendar);

    GregorianCalendar getStopDate();

    void setStopDate(GregorianCalendar calendar);

    GregorianCalendar getStopTime();

    void setStopTime(GregorianCalendar calendar);

    //----------------------------------------------------------------------------------------------
    // Task Management

    boolean initialiseTask();

    boolean runTask();

    boolean idleTask();

    boolean shutdownTask();

    TaskState getState();

    boolean setState(TaskState state);

    boolean isRunning();

    boolean isLocked();

    void setLocked(boolean locked);

    //----------------------------------------------------------------------------------------------
    // Miscellaneous

    void handleException(Exception exception,
                         String identifier,
                         EventStatus status);

    void readResources();

    void addActiveChangeListener(ActiveChangeListener listener);

    void removeActiveChangeListener(ActiveChangeListener listener);
    }


//--------------------------------------------------------------------------------------------------
// End of File

