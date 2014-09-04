// Copyright 2000, 2001, 2002, 2003, 04, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013, 2013
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

package org.lmn.fc.model.dao.mysql;

import org.lmn.fc.model.logging.Logger;
import org.lmn.fc.model.registry.RegistryPlugin;
import org.lmn.fc.model.registry.impl.Registry;
import org.lmn.fc.ui.components.EditorUIComponent;


public class TasksSqlDAO
    {
    private static final Logger LOGGER = Logger.getInstance();
    private static final RegistryPlugin REGISTRY = Registry.getInstance();

    // Column Names in Tasks tables
    public static final String FRAMEWORK_TASKS_TABLE        = "FrameworkTasks";
    public static final String APPLICATION_TASKS_TABLE      = "ApplicationTasks";
    public static final String COMPONENT_TASKS_TABLE        = "ComponentTasks";
    public static final String APPLICATION_ID               = "ApplicationID";
    public static final String COMPONENT_ID                 = "ComponentID";
    public static final String FRAMEWORKTASK_ID             = "FrameworkTaskID";
    public static final String APPLICATIONTASK_ID           = "ApplicationTaskID";
    public static final String COMPONENTTASK_ID             = "ComponentTaskID";
    public static final String USER_ROLE_MASK               = "UserRoleMask";
    public static final String TASK_NAME                    = "TaskName";
    public static final String TASK_SORTORDER               = "TaskSortOrder";
    public static final String TASK_EDITABLE                = "TaskEditable";
    public static final String TASK_PUBLIC                  = "TaskPublic";
    public static final String TASK_RUNNABLE                = "TaskRunnable";
    public static final String TASK_RUNATSTART              = "TaskRunAtStart";
    public static final String TASK_ACTIVE                  = "TaskActive";
    public static final String EDITOR_ID                    = "EditorID";
    public static final String TASK_DESCRIPTION             = "TaskDescription";
    public static final String DATE_CREATED                 = "DateCreated";
    public static final String TIME_CREATED                 = "TimeCreated";
    public static final String DATE_MODIFIED                = "DateModified";
    public static final String TIME_MODIFIED                = "TimeModified";
    // Column name in the Editors table, joined on EditorID
    public static final String EDITOR_CLASSNAME             = EditorUIComponent.EDITOR_CLASSNAME;
    public static final int DESCRIPTION_LENGTH = 255;


    /***********************************************************************************************
      * Create the SQL script to delete this TaskData.
      *
      * @return StringBuffer
      */

     public final StringBuffer createSQLDeleteScript()
         {
         final StringBuffer bufferScript;

         bufferScript = new StringBuffer();

//         switch (getTaskType())
//             {
//             case FRAMEWORK_TASK:
//                 {
//                 bufferScript.append(SQL_DELETE);
//                 bufferScript.append(TasksSqlDAO.FRAMEWORK_TASKS_TABLE);
//                 bufferScript.append(SQL_WHERE);
//                 bufferScript.append(TasksSqlDAO.FRAMEWORKTASK_ID);
//                 bufferScript.append(SQL_EQUALS);
//                 bufferScript.append(getID());
//
//                 return (bufferScript);
//                 }
//
//             case APPLICATION_TASK:
//                 {
//                 bufferScript.append(SQL_DELETE);
//                 bufferScript.append(TasksSqlDAO.APPLICATION_TASKS_TABLE);
//                 bufferScript.append(SQL_WHERE);
//                 bufferScript.append(TasksSqlDAO.APPLICATION_ID);
//                 bufferScript.append(SQL_EQUALS);
//                 bufferScript.append(getApplicationID());
//                 bufferScript.append(SQL_AND);
//                 bufferScript.append(TasksSqlDAO.APPLICATIONTASK_ID);
//                 bufferScript.append(SQL_EQUALS);
//                 bufferScript.append(getID());
//
//                 return (bufferScript);
//                 }
//
//             case COMPONENT_TASK:
//                 {
//                 bufferScript.append(SQL_DELETE);
//                 bufferScript.append(TasksSqlDAO.COMPONENT_TASKS_TABLE);
//                 bufferScript.append(SQL_WHERE);
//                 bufferScript.append(TasksSqlDAO.APPLICATION_ID);
//                 bufferScript.append(SQL_EQUALS);
//                 bufferScript.append(getApplicationID());
//                 bufferScript.append(SQL_AND);
//                 bufferScript.append(TasksSqlDAO.COMPONENT_ID);
//                 bufferScript.append(SQL_EQUALS);
//                 bufferScript.append(getPointID());
//                 bufferScript.append(SQL_AND);
//                 bufferScript.append(TasksSqlDAO.COMPONENTTASK_ID);
//                 bufferScript.append(SQL_EQUALS);
//                 bufferScript.append(getID());

//                 return (bufferScript);
//                 }
//
//             default:
//                 {
                 return (bufferScript);
//                 }
//             }
         }


     /***********************************************************************************************
      * Create the SQL script to install this TaskData.
      *
      * @return StringBuffer
      */

     public final StringBuffer createSQLInsertScript()
         {
         final StringBuffer bufferScript;

         bufferScript = new StringBuffer();

//         switch (getTaskType())
//             {
//             case FRAMEWORK_TASK:
//                 {
//                 bufferScript.append(SQL_INSERT);
//                 bufferScript.append(TasksSqlDAO.FRAMEWORK_TASKS_TABLE);
//                 bufferScript.append(SQL_LEFT_PAREN);
//                 bufferScript.append(TasksSqlDAO.FRAMEWORKTASK_ID);
//                 bufferScript.append(SQL_COMMA);
//                 break;
//                 }
//
//             case APPLICATION_TASK:
//                 {
//                 bufferScript.append(SQL_INSERT);
//                 bufferScript.append(TasksSqlDAO.APPLICATION_TASKS_TABLE);
//                 bufferScript.append(SQL_LEFT_PAREN);
//                 bufferScript.append(TasksSqlDAO.APPLICATION_ID);
//                 bufferScript.append(SQL_COMMA);
//                 bufferScript.append(TasksSqlDAO.APPLICATIONTASK_ID);
//                 bufferScript.append(SQL_COMMA);
//                 break;
//                 }
//
//             case COMPONENT_TASK:
//                 {
//                 bufferScript.append(SQL_INSERT);
//                 bufferScript.append(TasksSqlDAO.COMPONENT_TASKS_TABLE);
//                 bufferScript.append(SQL_LEFT_PAREN);
//                 bufferScript.append(TasksSqlDAO.APPLICATION_ID);
//                 bufferScript.append(SQL_COMMA);
//                 bufferScript.append(TasksSqlDAO.COMPONENT_ID);
//                 bufferScript.append(SQL_COMMA);
//                 bufferScript.append(TasksSqlDAO.COMPONENTTASK_ID);
//                 bufferScript.append(SQL_COMMA);
//                 break;
//                 }
//             }
//
//         bufferScript.append(TasksSqlDAO.USER_ROLE_MASK);
//         bufferScript.append(SQL_COMMA);
//         bufferScript.append(TasksSqlDAO.TASK_NAME);
//         bufferScript.append(SQL_COMMA);
//         bufferScript.append(TasksSqlDAO.TASK_SORTORDER);
//         bufferScript.append(SQL_COMMA);
//         bufferScript.append(TasksSqlDAO.TASK_EDITABLE);
//         bufferScript.append(SQL_COMMA);
//         bufferScript.append(TasksSqlDAO.TASK_PUBLIC);
//         bufferScript.append(SQL_COMMA);
//         bufferScript.append(TasksSqlDAO.TASK_RUNNABLE);
//         bufferScript.append(SQL_COMMA);
//         bufferScript.append(TasksSqlDAO.TASK_RUNATSTART);
//         bufferScript.append(SQL_COMMA);
//         bufferScript.append(TasksSqlDAO.TASK_ACTIVE);
//         bufferScript.append(SQL_COMMA);
//         bufferScript.append(TasksSqlDAO.EDITOR_ID);
//         bufferScript.append(SQL_COMMA);
//         bufferScript.append(TasksSqlDAO.TASK_DESCRIPTION);
//
//         switch (getTaskType())
//             {
//             case FRAMEWORK_TASK:
//                 {
//                 bufferScript.append(SQL_VALUES);
//                 bufferScript.append(getID());
//                 bufferScript.append(SQL_COMMA);
//                 break;
//                 }
//
//             case APPLICATION_TASK:
//                 {
//                 bufferScript.append(SQL_VALUES);
//                 bufferScript.append(getApplicationID());
//                 bufferScript.append(SQL_COMMA);
//                 bufferScript.append(getID());
//                 bufferScript.append(SQL_COMMA);
//                 break;
//                 }
//
//             case COMPONENT_TASK:
//                 {
//                 bufferScript.append(SQL_VALUES);
//                 bufferScript.append(getApplicationID());
//                 bufferScript.append(SQL_COMMA);
//                 bufferScript.append(getPointID());
//                 bufferScript.append(SQL_COMMA);
//                 bufferScript.append(getID());
//                 bufferScript.append(SQL_COMMA);
//                 break;
//                 }
//             }
//
//         bufferScript.append(FrameworkMySqlDAO.toSQL(getUserRoleMask()));
//         bufferScript.append(SQL_COMMA);
//         bufferScript.append(FrameworkMySqlDAO.toSQL(getName()));
//         bufferScript.append(SQL_COMMA);
//         bufferScript.append(getSortOrder());
//         bufferScript.append(SQL_COMMA);
//         bufferScript.append(FrameworkMySqlDAO.toSQL(isEditable()));
//         bufferScript.append(SQL_COMMA);
//         bufferScript.append(FrameworkMySqlDAO.toSQL(isPublic()));
//         bufferScript.append(SQL_COMMA);
//         bufferScript.append(FrameworkMySqlDAO.toSQL(isRunnable()));
//         bufferScript.append(SQL_COMMA);
//         bufferScript.append(FrameworkMySqlDAO.toSQL(isRunAtStart()));
//         bufferScript.append(SQL_COMMA);
//         bufferScript.append(FrameworkMySqlDAO.toSQL(isActive()));
//         bufferScript.append(SQL_COMMA);
//         bufferScript.append(EditorUtilities.getResourceEditorID(getEditorClassname()));
//         bufferScript.append(SQL_COMMA);
//         bufferScript.append(FrameworkMySqlDAO.toSQL(getDescription()));
//         bufferScript.append(SQL_RIGHT_PAREN);

         return (bufferScript);
         }


    }
