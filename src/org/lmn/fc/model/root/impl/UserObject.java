package org.lmn.fc.model.root.impl;

import org.lmn.fc.model.root.UserObjectPlugin;
import org.lmn.fc.model.root.ActionGroup;
import org.lmn.fc.ui.status.StatusIndicatorKey;
import org.lmn.fc.ui.components.BlankUIComponent;
import org.lmn.fc.ui.components.ScrollUI;
import org.lmn.fc.ui.UIComponentPlugin;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.common.actions.ContextActionGroup;
import org.lmn.fc.common.constants.FrameworkStrings;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Vector;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.Locale;


/***********************************************************************************************
 * The UserObject is the base class for all items which may be selected by the User.
 */

public abstract class UserObject extends RootData
                                 implements UserObjectPlugin,
                                            FrameworkStrings
    {
    // The context-sensitive Actions for the UserObject
    private Vector<ContextActionGroup> vecContextActionGroups;

//    private Date dateCreated;               // The date on which the RootData was created
//    private Time timeCreated;
//    private Date dateModified;              // The date on which the RootData was last modified
//    private Time timeModified;


    /***********************************************************************************************
     * A utility to get a Calendar initialised to the Framework's TimeZone and Locale.
     *
     * @return GregorianCalendar
     */

    public static GregorianCalendar getFrameworkCalendar()
        {
        final GregorianCalendar calendar;
        final TimeZone timeZone;
        final Locale locale;

        timeZone = REGISTRY.getFrameworkTimeZone();
        locale = new Locale(REGISTRY.getFramework().getLanguageISOCode(),
                            REGISTRY.getFramework().getCountryISOCode());
        calendar = new GregorianCalendar(timeZone, locale);

        return (calendar);
        }


    /***********************************************************************************************
     * Construct a UserObject.
     *
     * @param id
     */

    public UserObject(final long id)
        {
        super(id);

        this.vecContextActionGroups = new Vector<ContextActionGroup>(1);
        }


    /***********************************************************************************************
     * Get the vector of ContextActionGroups for this UserObject.
     *
     * @return Vector
     */

    public final Vector<ContextActionGroup> getUserObjectContextActionGroups()
        {
        return (this.vecContextActionGroups);
        }


    /***********************************************************************************************
     * Get the ContextActionGroup specified by the group Enum.
     *
     * @param group
     *
     * @return ContextActionGroup
     */

    public final ContextActionGroup getUserObjectContextActionGroupByIndex(final ActionGroup group)
        {
        if ((group != null)
            && (getUserObjectContextActionGroups() != null)
            && (group.getIndex() < getUserObjectContextActionGroups().size()))
            {
            //LOGGER.debug("got group by index " + group.name());
            return (getUserObjectContextActionGroups().get(group.getIndex()));
            }
        else
            {
            final ContextActionGroup contextActionGroup;

            if (getUserObjectContextActionGroups() == null)
                {
                setUserObjectContextActionGroups(new Vector<ContextActionGroup>(1));
                }

            // Add a Group to this UserObject, taking the name of the UserObject
            // ToDo Think about menu/toolbar flags?
            contextActionGroup = new ContextActionGroup(getName(), true, true);
            addUserObjectContextActionGroup(contextActionGroup);
            //LOGGER.debug("created group by name " + getName());

            return (contextActionGroup);
            }
        }


    /***********************************************************************************************
     * Get or create the ContextActionGroup with the specified name.
     *
     * @param name
     *
     * @return ContextActionGroup
     */

//    public final ContextActionGroup getContextActionGroupByName(final String name)
//        {
//        ContextActionGroup contextActionGroup;
//        final Iterator<ContextActionGroup> iterGroups;
//        boolean boolExists;
//
//        contextActionGroup = null;
//        boolExists = false;
//        iterGroups = getContextActionGroups().iterator();
//
//        while ((iterGroups.hasNext())
//            && (!boolExists))
//            {
//            contextActionGroup = iterGroups.next();
//
//            if ((contextActionGroup != null)
//                && (contextActionGroup.getName() != null)
//                && (contextActionGroup.getName().equals(name)))
//                {
//                boolExists = true;
//                }
//            }
//
//        // If we didn't find the Group, make a new one with the required name
//        if (!boolExists)
//            {
//            //LOGGER.debug("Creating ContextActionGroup " + name);
//            // ToDo Think about menu/toolbar flags?
//            contextActionGroup = new ContextActionGroup(name, true, true);
//
//            // Add the Group to this Task
//            addContextActionGroup(contextActionGroup);
//            }
//
//        return (contextActionGroup);
//        }


    /***********************************************************************************************
     * Set the vector of ContextActionGroups for this UserObject.
     *
     * @param actiongroups
     */

    public final void setUserObjectContextActionGroups(final Vector<ContextActionGroup> actiongroups)
        {
        this.vecContextActionGroups = actiongroups;
        }


    /***********************************************************************************************
     * Add an ActionGroup to the list of ContextActionGroups.
     *
     * @param group
     */

    public final void addUserObjectContextActionGroup(final ContextActionGroup group)
        {
        if ((group != null)
            && (getUserObjectContextActionGroups() != null)
            && (!getUserObjectContextActionGroups().contains(group)))
            {
            //LOGGER.debug("Add group to UserObject [group=" + group.getName() + "] [userobject=" + getName() + "]");
            getUserObjectContextActionGroups().add(group);
            }
        }


    /***********************************************************************************************
     * Remove an ActionGroup from the list of ContextActionGroups.
     *
     * @param group
     */

    public final void removeUserObjectContextActionGroup(final ContextActionGroup group)
        {
        if ((group != null)
            && (getUserObjectContextActionGroups() != null)
            && (getUserObjectContextActionGroups().contains(group)))
            {
            //LOGGER.debug("removeContextActionGroup() " + group.getName() + " in " + getName());
            getUserObjectContextActionGroups().remove(group);
            }
        }


    /***********************************************************************************************
     * Clear the complete list of ContextActionGroups for this UserObject.
     */

    public final void clearUserObjectContextActionGroups()
        {
        //LOGGER.debug("clearContextActionGroups() in " + getName());
        this.vecContextActionGroups = new Vector<ContextActionGroup>(1);
        }


    /***********************************************************************************************
     * Clear the specified ContextActionGroup for this Task.
     *
     * @param group
     */
    public final void clearUserObjectContextActionGroup(final ContextActionGroup group)
        {
        if ((group != null)
            && (getUserObjectContextActionGroups() != null)
            && (getUserObjectContextActionGroups().contains(group)))
            {
            //LOGGER.debug("clearContextActionGroup() " + group.getName() + " in " + getName());
            group.clearContextActions();
            }
        }


    /***********************************************************************************************
     * Get or create the ContextActionGroup with the same name as the Task.
     *
     * @return ContextActionGroup
     */

    public final ContextActionGroup getPrimaryUserObjectContextActionGroup()
        {
        final Enumeration<ContextActionGroup> enumContextActionGroups;
        ContextActionGroup contextActionGroup;

        LOGGER.debug("GET PrimaryContextActionGroup for " + getName());

        if (getUserObjectContextActionGroups().isEmpty())
            {
            LOGGER.debug("EMPTY PrimaryContextActionGroup!");
            }

        // Does this Task already have a ContextActionGroup with the same name as the Task?
        enumContextActionGroups = getUserObjectContextActionGroups().elements();

        while (enumContextActionGroups.hasMoreElements())
            {
            contextActionGroup = enumContextActionGroups.nextElement();

            if (contextActionGroup.getName().equals(getName()))
                {
                // Todo remove multiple returns... :-)
                LOGGER.debug("FOUND PrimaryContextActionGroup " + getName());
                return (contextActionGroup);
                }
            }

        LOGGER.debug("CREATING PrimaryContextActionGroup " + getName());
        // If we get here, we didn't find the primary group, so create one
        // ToDo Think about menu/toolbar flags?
        contextActionGroup = new ContextActionGroup(getName(), true, true);

        // Add this ContextActionGroup to the Task's ContextActionGroups
        addUserObjectContextActionGroup(contextActionGroup);

        return (contextActionGroup);
        }


    /***********************************************************************************************
     * See if this Task has a ContextActionGroup with the specified name.
     *
     * @param name
     *
     * @return boolean
     */

//    public final boolean isExistingActionGroupName(final String name)
//        {
//        ContextActionGroup contextActionGroup;
//        final Iterator<ContextActionGroup> iterGroups;
//        boolean boolExists;
//
//        boolExists = false;
//        iterGroups = getContextActionGroups().iterator();
//
//        while ((iterGroups.hasNext())
//            && (!boolExists))
//            {
//            contextActionGroup = iterGroups.next();
//
//            if ((contextActionGroup != null)
//                && (contextActionGroup.getName() != null)
//                && (contextActionGroup.getName().equals(name)))
//                {
//                boolExists = true;
//                }
//            }
//
//        return (boolExists);
//        }


    /***********************************************************************************************
     * Force the Navigation Tree selection to the node containing this UserObject.
     *
     * @param node
     */

    public void selectNodeOnTree(final DefaultMutableTreeNode node)
        {
        if ((node != null)
            && (REGISTRY_MODEL.getFrameworkManager() != null)
            && (REGISTRY_MODEL.getFrameworkManager().getUI() != null)
            && (REGISTRY_MODEL.getFrameworkManager().getUI().getNavigationTree() != null))
            {
            LOGGER.debugNavigation("UserObject.selectNodeOnTree() " + node);

            // Force the Tree selection to the node containing this UserObject
            REGISTRY_MODEL.getFrameworkManager().getUI().getNavigationTree().setSelectionPath(new TreePath(node.getPath()));

            // Record the new selection, regardless of whether the tree is visible
            REGISTRY_MODEL.getFrameworkManager().getUI().setSelectedTreeNode(node);
            }
        }


    /***********************************************************************************************
     * Set the Caption of the UserInterface, if installed.
     *
     * @param caption
     */

    public final void setCaption(final String caption)
        {
        if ((REGISTRY_MODEL.getUserInterface() != null)
            && (REGISTRY_MODEL.getUserInterface().getUI() != null))
            {
            LOGGER.debugNavigation("UserObject.setCaption(" + caption + ")");
            REGISTRY_MODEL.getUserInterface().getUI().setCaption(caption);
            }
        }


    /***********************************************************************************************
     * Show some status text and an icon, if possible.
     * Double check that there is a UserInterface and FrameworkManager, since we may be in Login...
     *
     * @param status
     * @param icon
     */

    public void setStatus(final String status, final Icon icon)
        {
        if ((REGISTRY_MODEL.getUserInterface() != null)
            && (REGISTRY_MODEL.getUserInterface().getUI() != null)
            && (REGISTRY_MODEL.getFrameworkManagerUI() != null)
            && (REGISTRY_MODEL.getFrameworkManagerUI().getStatusBar() != null)
            && (REGISTRY_MODEL.getFrameworkManagerUI().getStatusIndicator(StatusIndicatorKey.STATUS) != null))
            {
            LOGGER.debugNavigation("UserObject..setResponseStatus() [status=" + status + "]");

            REGISTRY_MODEL.getFrameworkManagerUI().getStatusIndicator(StatusIndicatorKey.STATUS).setText(status);
            REGISTRY_MODEL.getFrameworkManagerUI().getStatusIndicator(StatusIndicatorKey.STATUS).setIcon(icon);
            REGISTRY_MODEL.getFrameworkManagerUI().getStatusBar().repaint();
            }
        else
            {
            LOGGER.debugNavigation("UserObject..setResponseStatus() Cannot show status! [" + status + "]");
            }
        }


    /***********************************************************************************************
     * Edit the UserObject.
     */

    public void editUserObject()
        {
        final String strEditor;

        // We are in Edit mode
        try
            {
            setCaption(getPathname());

            // Attempt to create an Editor
            strEditor = getEditorClassname();
            LOGGER.debugNavigation("Attempt to create an Editor -->" + strEditor);

            if ((strEditor == null)
                || (EMPTY_STRING.equals(strEditor.trim())))
                {
                // No editor installed, so use a blank panel instead of an editor...
                // Place it in the Editor Component
                setEditorComponent(new BlankUIComponent());

                // This seems a bit dodgy, but it works...
                REGISTRY_MODEL.getFrameworkManagerUI().setUIOccupant(this);

                setStatus(EMPTY_STRING);
                }
            else
                {
                // Try to get the class name of the required editor
                // This may fail with ClassNotFoundException
                final Class classEditor = Class.forName(strEditor);

                // Get the constructor for the Editor
                final Class[] classParameters = { UserObjectPlugin.class };
                final Constructor constructorResource = classEditor.getDeclaredConstructor(classParameters);

                // Construct the Editor to edit 'this'
                final Object[] objArguments = { this };
                final Object objectResource = constructorResource.newInstance(objArguments);

                // Place the Editor in a ScrollUI and then into the Editor Component
                setEditorComponent(new ScrollUI((UIComponentPlugin)objectResource));

                // This seems a bit dodgy, but it works...
                REGISTRY_MODEL.getFrameworkManagerUI().setUIOccupant(this);

                // Tell the world what we're doing...
                // NullData does not have an editor
                if (isEditable())
                    {
                    setStatus("Editing " + this.getPathname());
                    }
                else
                    {
                    setStatus("May not be edited");
                    }
                }
            }

        catch(ClassNotFoundException exception)
           {
           LOGGER.handleAtomException(REGISTRY.getFramework(),
                                      REGISTRY.getFramework().getRootTask(),
                                      RootData.class.getName(),
                                      exception,
                                      LOAD_RESOURCE + " [ClassNotFoundException]",
                                      EventStatus.FATAL);
           }

        catch(InvocationTargetException exception)
           {
           LOGGER.handleAtomException(REGISTRY.getFramework(),
                                      REGISTRY.getFramework().getRootTask(),
                                      RootData.class.getName(),
                                      exception,
                                      LOAD_RESOURCE + " [InvocationTargetException]",
                                      EventStatus.FATAL);
           }

        catch(InstantiationException exception)
           {
           LOGGER.handleAtomException(REGISTRY.getFramework(),
                                      REGISTRY.getFramework().getRootTask(),
                                      RootData.class.getName(),
                                      exception,
                                      LOAD_RESOURCE + " [InstantiationException]",
                                      EventStatus.FATAL);
           }

        catch(IllegalAccessException exception)
           {
           LOGGER.handleAtomException(REGISTRY.getFramework(),
                                      REGISTRY.getFramework().getRootTask(),
                                      RootData.class.getName(),
                                      exception,
                                      LOAD_RESOURCE + " [IllegalAccessException]",
                                      EventStatus.FATAL);
           }

        catch(NoSuchMethodException exception)
           {
           LOGGER.handleAtomException(REGISTRY.getFramework(),
                                      REGISTRY.getFramework().getRootTask(),
                                      RootData.class.getName(),
                                      exception,
                                      LOAD_RESOURCE + " [NoSuchMethodException]",
                                      EventStatus.INFO);
           }
        }


    /***********************************************************************************************
     * Mark the UserObject as Updated.
     */

    public void updateRoot()
        {
        super.updateRoot();

        if (isUpdateAllowed())
            {
//            setModifiedDate(Chronos.getSystemDateNow());
//            setModifiedTime(Chronos.getSystemTimeNow());
            }
        }
    }
