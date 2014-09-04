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

//--------------------------------------------------------------------------------------------------
// Revision History
//
//  20-10-03    LMN created file
//
//
//--------------------------------------------------------------------------------------------------
// RegistryModel package

package org.lmn.fc.common.actions;


//--------------------------------------------------------------------------------------------------
// Imports

import org.lmn.fc.model.logging.Logger;
import org.lmn.fc.model.plugins.AtomPlugin;
import org.lmn.fc.model.root.UserObjectPlugin;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.ui.UIComponentPlugin;

import javax.swing.*;
import java.util.Iterator;
import java.util.Vector;


/***************************************************************************************************
 * A ContextActionGroup.
 */

public final class ContextActionGroup
    {
    private static final Logger LOGGER = Logger.getInstance();

    private final String strGroupName;
    private Vector<ContextAction> vecActions;
    private final boolean boolMenu;
    private final boolean boolToolBar;


    /***********************************************************************************************
     * A debugging utility to show an Atom and a Task's ContextActionGroups.
     *
     * @param atom
     * @param task
     */

    public static void showContextActionGroups(final AtomPlugin atom,
                                               final TaskPlugin task)
        {
        if ((atom != null)
            && (task != null))
            {
            ContextActionGroup contextActionGroup;
            ContextAction contextAction;
            Iterator iterGroups;
            Iterator iterContextActions;

            LOGGER.debug(atom.getName() + " ContextActionGroups");

            iterGroups = atom.getUserObjectContextActionGroups().iterator();

            while (iterGroups.hasNext())
                {
                contextActionGroup = (ContextActionGroup)iterGroups.next();

                LOGGER.debug(".   " + contextActionGroup.getName());

                iterContextActions = contextActionGroup.getActions();
                while (iterContextActions.hasNext())
                    {
                    contextAction = (ContextAction)iterContextActions.next();

                    LOGGER.debug(".       " + contextAction.getValue(Action.NAME));
                    }
                }

            LOGGER.debug(task.getName() + " ContextActionGroups");

            iterGroups = task.getUserObjectContextActionGroups().iterator();
            while (iterGroups.hasNext())
                {
                contextActionGroup = (ContextActionGroup) iterGroups.next();

                LOGGER.debug(".   " + contextActionGroup.getName());

                iterContextActions = contextActionGroup.getActions();
                while (iterContextActions.hasNext())
                    {
                    contextAction = (ContextAction)iterContextActions.next();

                    LOGGER.debug(".       " + contextAction.getValue(Action.NAME));
                    }
                }
            }
        }


    /***********************************************************************************************
     * A debugging utility to show the ContextActions within a ContextActionGroup
     * of a UIComponent.
     *
     * @param component
     * @param index
     */

    public static void showUIComponentContextActions(final UIComponentPlugin component,
                                                     final int index)
        {
        if ((component != null)
            && (component.getUIComponentContextActionGroups() != null)
            && (index >= 0)
            && (index < component.getUIComponentContextActionGroups().size())
            && (component.getUIComponentContextActionGroups().get(index) != null))
            {
            final ContextActionGroup actionGroup;
            final Iterator iterActions;

            actionGroup = component.getUIComponentContextActionGroups().get(index);
            LOGGER.debug("UIComponent ContextActionGroup " + actionGroup.getName());

            iterActions = actionGroup.getActions();

            while (iterActions.hasNext())
                {
                final StringBuffer buffer;
                final ContextAction contextAction;

                contextAction = (ContextAction) iterActions.next();
                buffer = new StringBuffer("    ContextAction");

                if (contextAction.isMenu())
                    {
                    buffer.append(" menu");
                    }
                else
                    {
                    buffer.append("     ");
                    }

                if (contextAction.isToolBar())
                    {
                    buffer.append(" toolbar");
                    }
                else
                    {
                    buffer.append("        ");
                    }

                if (contextAction.isSeparator())
                    {
                    buffer.append(" separator ");
                    }
                else
                    {
                    buffer.append("           ");
                    }

                buffer.append(contextAction.getValue(Action.NAME));
                buffer.append(",  ");
                buffer.append(contextAction.getValue(Action.SHORT_DESCRIPTION));
                buffer.append(",  ");
                buffer.append(contextAction.getValue(Action.LONG_DESCRIPTION));
                buffer.append(",  ");
                buffer.append(contextAction.getValue(Action.ACTION_COMMAND_KEY));
                buffer.append(",  ");
                buffer.append(contextAction.getValue(Action.ACCELERATOR_KEY));
                buffer.append(",  ");
                buffer.append(contextAction.getValue(Action.MNEMONIC_KEY));
                LOGGER.debug(buffer.toString());
                }
            }
        else
            {
            LOGGER.debug("UIComponent Context Actions --> Trying to display a null or empty ContextActionGroup");
            }
        }


    /***********************************************************************************************
     * A debugging utility to show the ContextActions within a ContextActionGroup
     * of a UserObject.
     *
     * @param plugin
     * @param index
     */

    public static void showUserObjectContextActions(final UserObjectPlugin plugin,
                                                    final int index)
        {
        if ((plugin != null)
            && (plugin.getUserObjectContextActionGroups() != null)
            && (index >= 0)
            && (index < plugin.getUserObjectContextActionGroups().size())
            && (plugin.getUserObjectContextActionGroups().get(index) != null))
            {
            final ContextActionGroup actionGroup;
            final Iterator iterActions;

            actionGroup = plugin.getUserObjectContextActionGroups().get(index);
            LOGGER.debug("UserObject ContextActionGroup " + actionGroup.getName());

            iterActions = actionGroup.getActions();

            while (iterActions.hasNext())
                {
                final StringBuffer buffer;
                final ContextAction contextAction;

                contextAction = (ContextAction) iterActions.next();
                buffer = new StringBuffer("    ContextAction");

                if (contextAction.isMenu())
                    {
                    buffer.append(" menu");
                    }
                else
                    {
                    buffer.append("     ");
                    }

                if (contextAction.isToolBar())
                    {
                    buffer.append(" toolbar");
                    }
                else
                    {
                    buffer.append("        ");
                    }

                if (contextAction.isSeparator())
                    {
                    buffer.append(" separator ");
                    }
                else
                    {
                    buffer.append("           ");
                    }

                buffer.append(contextAction.getValue(Action.NAME));
                buffer.append(",  ");
                buffer.append(contextAction.getValue(Action.SHORT_DESCRIPTION));
                buffer.append(",  ");
                buffer.append(contextAction.getValue(Action.LONG_DESCRIPTION));
                buffer.append(",  ");
                buffer.append(contextAction.getValue(Action.ACTION_COMMAND_KEY));
                buffer.append(",  ");
                buffer.append(contextAction.getValue(Action.ACCELERATOR_KEY));
                buffer.append(",  ");
                buffer.append(contextAction.getValue(Action.MNEMONIC_KEY));
                LOGGER.debug(buffer.toString());
                }
            }
        else
            {
            LOGGER.debug("UserObject Context Actions --> Trying to display a null or empty ContextActionGroup");
            }
        }


    /***********************************************************************************************
     * A debugging utility to show the ContextActions within a ContextActionGroup.
     *
     * @param actiongroup
     */

    public static void showContextActions(final ContextActionGroup actiongroup)
        {
        if (actiongroup != null)
            {
            final Iterator iterActions;

            LOGGER.debug("ContextActionGroup " + actiongroup.getName());

            iterActions = actiongroup.getActions();

            while (iterActions.hasNext())
                {
                final StringBuffer buffer;
                final ContextAction contextAction;

                contextAction = (ContextAction) iterActions.next();
                buffer = new StringBuffer("    ContextAction");

                if (contextAction.isMenu())
                    {
                    buffer.append(" menu");
                    }
                else
                    {
                    buffer.append("     ");
                    }

                if (contextAction.isToolBar())
                    {
                    buffer.append(" toolbar");
                    }
                else
                    {
                    buffer.append("        ");
                    }

                if (contextAction.isSeparator())
                    {
                    buffer.append(" separator ");
                    }
                else
                    {
                    buffer.append("           ");
                    }

                buffer.append(contextAction.getValue(Action.NAME));
                buffer.append(",  ");
                buffer.append(contextAction.getValue(Action.SHORT_DESCRIPTION));
                buffer.append(",  ");
                buffer.append(contextAction.getValue(Action.LONG_DESCRIPTION));
                buffer.append(",  ");
                buffer.append(contextAction.getValue(Action.ACTION_COMMAND_KEY));
                buffer.append(",  ");
                buffer.append(contextAction.getValue(Action.ACCELERATOR_KEY));
                buffer.append(",  ");
                buffer.append(contextAction.getValue(Action.MNEMONIC_KEY));
                LOGGER.debug(buffer.toString());
                }
            }
        else
            {
            LOGGER.debug("Trying to display a null ContextActionGroup");
            }
        }


    /***********************************************************************************************
     *
     * @param groupname
     */

    public ContextActionGroup(final String groupname,
                              final boolean menu,
                              final boolean toolbar)
        {
        strGroupName = groupname;
        vecActions = new Vector<ContextAction>(10);
        boolMenu = menu;
        boolToolBar = toolbar;
        }


    /***********************************************************************************************
     *
     * @return String
     */

    public final String getName()
        {
        return (strGroupName);
        }


    /***********************************************************************************************
     *
     * @return Iterator<ContextAction>
     */

    public final Iterator<ContextAction> getActions()
        {
        if (vecActions != null)
            {
            return (vecActions.iterator());
            }
        else
            {
            return (null);
            }
        }


    /***********************************************************************************************
     *
     * @param action
     */

    public final void addContextAction(final ContextAction action)
        {
        if ((action != null)
            && (vecActions != null)
            && ((!vecActions.contains(action))))
            {
            final Iterator<ContextAction> iterActions;
            boolean boolExists;

            // 2008-05-04 Is there already an Action with the same *name*?
            // If so, we probably don't want to add it
            iterActions = vecActions.iterator();
            boolExists = false;

            while ((!boolExists) && (iterActions.hasNext()))
                {
                final ContextAction contextAction;

                contextAction = iterActions.next();
                boolExists =  (contextAction.getValue(Action.NAME).equals(action.getValue(Action.NAME)));
                }

            if (!boolExists)
                {
                //LOGGER.log("ContextActionGroup.addContextAction() adding action to group");
                vecActions.add(action);
                }
            }
        }


    /***********************************************************************************************
     *
     * @param action
     */

    public final void removeContextAction(final ContextAction action)
        {
        if ((action != null)
            && (vecActions != null)
            && (vecActions.contains(action)))
            {
            //System.out.println("REALLY removed action " + action.getValue(Action.NAME));
            vecActions.remove(action);
            }
        }


    /***********************************************************************************************
     *
     * @param action
     *
     * @return boolean
     */

    public final boolean containsContextAction(final ContextAction action)
        {
        if (vecActions != null)
            {
            return (vecActions.contains(action));
            }
        else
            {
            return (false);
            }
        }


    /***********************************************************************************************
     *
     */

    public final void clearContextActions()
        {
        vecActions = new Vector<ContextAction>(10);
        }


    /***********************************************************************************************
     *
     * @return boolean
     */

    public final boolean isMenu()
        {
        return (boolMenu);
        }


    /***********************************************************************************************
     *
     * @return boolean
     */

    public final boolean isToolBar()
        {
        return (boolToolBar);
        }


    /***********************************************************************************************
     *
     * @return boolean
     */

    public final boolean isEmpty()
        {
        if (vecActions != null)
            {
            return (vecActions.size() == 0);
            }
        else
            {
            // 2006-11-10 Changed to return TRUE if no group present!
            return (true);
            }
        }
    }


//--------------------------------------------------------------------------------------------------
// End of File
