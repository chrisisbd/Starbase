//--------------------------------------------------------------------------------------------------
// Revision History
//
//  26-04-06    LMN created file
//  24-05-06    LMN added extra flags for Editors etc.
//
//--------------------------------------------------------------------------------------------------

package org.lmn.fc.model.users.impl;

//--------------------------------------------------------------------------------------------------

import org.lmn.fc.common.exceptions.FrameworkException;
import org.lmn.fc.common.xml.XmlBeansUtilities;
import org.lmn.fc.model.root.impl.RootData;
import org.lmn.fc.model.users.RolePlugin;
import org.lmn.fc.model.xmlbeans.roles.Role;
import org.lmn.fc.model.xmlbeans.roles.RoleName;

import java.awt.*;
import java.util.List;


/***************************************************************************************************
 * The RoleData.
 */

public final class RoleData extends RootData
                            implements RolePlugin
    {
    /***********************************************************************************************
     * Debug the UserRoles.
     *
     * @param roles
     */

    public static void showRoles(final List<RolePlugin> roles)
        {
        if (roles != null)
            {
            LOGGER.log("[Roles]");

            for (final RolePlugin rolePlugin : roles)
                {
                LOGGER.log(INDENT + rolePlugin.getName());
                }
            }
        }


    /***********************************************************************************************
     * Construct a RoleData from the specified XML Object.
     *
     * @param role
     */

    public RoleData(final Role role)
        {
        super(7689146881716814121L);

        if ((role == null)
            || (!XmlBeansUtilities.isValidXml(role)))
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_NULL);
            }

        setXml(role);

        // Make the Roles accessible on the navigation tree
        getHostTreeNode().setUserObject(this);
        }


    /**********************************************************************************************/
    /* RolePlugin implementations                                                                 */
    /***********************************************************************************************
     * Get a flag indicating if this Role is a FrameworkViewer.
     *
     * @return boolean
     */

    public boolean isFrameworkViewer()
        {
        return (getXml().getFrameworkViewer());
        }


    /***********************************************************************************************
     * Set a flag indicating if this Role is a FrameworkViewer.
     *
     * @param flag
     */

    public void setFrameworkViewer(final boolean flag)
        {
        getXml().setFrameworkViewer(flag);
        updateRoot();
        }


    /***********************************************************************************************
     * Get a flag indicating if this Role is a FrameworkEditor.
     *
     * @return boolean
     */

    public boolean isFrameworkEditor()
        {
        return (getXml().getFrameworkEditor());
        }


    /***********************************************************************************************
     * Set a flag indicating if this Role is a FrameworkEditor.
     *
     * @param flag
     */

    public void setFrameworkEditor(final boolean flag)
        {
        getXml().setFrameworkEditor(flag);
        updateRoot();
        }


    /***********************************************************************************************
     * Get a flag indicating if this Role is an AtomViewer.
     *
     * @return boolean
     */

    public boolean isAtomViewer()
        {
        return (getXml().getAtomViewer());
        }


    /***********************************************************************************************
     * Set a flag indicating if this Role is an AtomViewer.
     *
     * @param flag
     */

    public void setAtomViewer(final boolean flag)
        {
        getXml().setAtomViewer(flag);
        updateRoot();
        }


    /***********************************************************************************************
     * Get a flag indicating if this Role is an AtomEditor.
     *
     * @return boolean
     */

    public boolean isAtomEditor()
        {
        return (getXml().getAtomEditor());
        }


    /***********************************************************************************************
     * Set a flag indicating if this Role is an AtomEditor.
     *
     * @param flag
     */

    public void setAtomEditor(final boolean flag)
        {
        getXml().setAtomEditor(flag);
        updateRoot();
        }


    /***********************************************************************************************
     * Get a flag indicating if this Role is a PluginMonitor.
     *
     * @return boolean
     */

    public boolean isTaskViewer()
        {
        return (getXml().getTaskViewer());
        }


    /***********************************************************************************************
     * Set a flag indicating if this Role is a PluginMonitor.
     *
     * @param flag
     */

    public void setTaskViewer(final boolean flag)
        {
        getXml().setTaskViewer(flag);
        updateRoot();
        }


    /***********************************************************************************************
     * Get a flag indicating if this Role is a TaskEditor.
     *
     * @return boolean
     */

    public boolean isTaskEditor()
        {
        return (getXml().getTaskEditor());
        }


    /***********************************************************************************************
     * Set a flag indicating if this Role is a TaskEditor.
     *
     * @param flag
     */

    public void setTaskEditor(final boolean flag)
        {
        getXml().setTaskEditor(flag);
        updateRoot();
        }


    /***********************************************************************************************
     * Get a flag indicating if this Role is a ResourceViewer.
     *
     * @return boolean
     */

    public boolean isResourceViewer()
        {
        return (getXml().getResourceViewer());
        }


    /***********************************************************************************************
     * Set a flag indicating if this Role is a ResourceViewer.
     *
     * @param flag
     */

    public void setResourceViewer(final boolean flag)
        {
        getXml().setResourceViewer(flag);
        updateRoot();
        }


    /***********************************************************************************************
     * Get a flag indicating if this Role is a ResourceEditor.
     *
     * @return boolean
     */

    public boolean isResourceEditor()
        {
        return (getXml().getResourceEditor());
        }


    /***********************************************************************************************
     * Set a flag indicating if this Role is a ResourceEditor.
     *
     * @param flag
     */

    public void setResourceEditor(final boolean flag)
        {
        getXml().setResourceEditor(flag);
        updateRoot();
        }


    /***********************************************************************************************
     * Get a flag indicating if this Role is a UserViewer.
     *
     * @return boolean
     */

    public boolean isUserViewer()
        {
        return (getXml().getUserViewer());
        }


    /***********************************************************************************************
     * Set a flag indicating if this Role is a UserViewer.
     *
     * @param flag
     */

    public void setUserViewer(final boolean flag)
        {
        getXml().setUserViewer(flag);
        updateRoot();
        }


    /***********************************************************************************************
     * Get a flag indicating if this Role is a UserEditor.
     *
     * @return boolean
     */

    public boolean isUserEditor()
        {
        return (getXml().getUserEditor());
        }


    /***********************************************************************************************
     * Set a flag indicating if this Role is a UserEditor.
     *
     * @param flag
     */

    public void setUserEditor(final boolean flag)
        {
        getXml().setUserEditor(flag);
        updateRoot();
        }


    /***********************************************************************************************
     * Get a flag indicating if this Role is a UserCreator.
     *
     * @return boolean
     */

    public boolean isUserCreator()
        {
        return (getXml().getUserCreator());
        }


    /***********************************************************************************************
     * Set a flag indicating if this Role is a UserCreator.
     *
     * @param flag
     */

    public void setUserCreator(final boolean flag)
        {
        getXml().setUserCreator(flag);
        updateRoot();
        }


    /***********************************************************************************************
     * Get a flag indicating if this Role is a UserDeletor.
     *
     * @return boolean
     */

    public boolean isUserDeletor()
        {
        return (getXml().getUserDeletor());
        }


    /***********************************************************************************************
     * Set a flag indicating if this Role is a UserDeletor.
     *
     * @param flag
     */

    public void setUserDeletor(final boolean flag)
        {
        getXml().setUserDeletor(flag);
        updateRoot();
        }


    /***********************************************************************************************
     * Get a flag indicating if this Role should start in FullScreen mode.
     *
     * @return boolean
     */

    public boolean isFullScreen()
        {
        return (getXml().getFullScreen());
        }


    /***********************************************************************************************
     * Set a flag indicating if this Role should start in FullScreen mode.
     *
     * @param flag
     */

    public void setFullScreen(final boolean flag)
        {
        getXml().setFullScreen(flag);
        updateRoot();
        }


    /**********************************************************************************************/
    /* RootPlugin implementations                                                                 */
    /***********************************************************************************************
     * Get the Role ResourceKey.
     *
     * @return String
     */

    public String getResourceKey()
        {
        return (PREFIX_ROLE + KEY_DELIMITER + getName());
        }


    /***********************************************************************************************
     * Get the full pathname for the Resource.
     *
     * @return String
     */

    public final String getPathname()
        {
        return (getResourceKey());
        }


    /***********************************************************************************************
     * Get the Name.
     *
     * @return String
     */

    public String getName()
        {
        return (getXml().getRoleName().toString());
        }


    /***********************************************************************************************
     * Set the Name.
     *
     * @param name
     */

    public void setName(final String name)
        {
        // Only allow valid RoleNames to be set
        if ((name == null)
            || (RoleName.Enum.forString(name) == null))
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_INVALID);
            }

        getXml().setRoleName(RoleName.Enum.forString(name));
        }


    /***********************************************************************************************
     * Get a flag indicating if this Resource is active.
     *
     * @return boolean
     */

    public final boolean isActive()
        {
        return (true);
        }


    /***********************************************************************************************
     * Set a flag indicating if this Resource is active.
     *
     * @param flag
     */

    public final void setActive(final boolean flag)
        {
        }


    /***********************************************************************************************
     * Get the sort order for displayed lists of Resources.
     *
     * @return short
     */

    public final short getSortOrder()
        {
        return (0);
        }


    /***********************************************************************************************
     * Set the sort order for displayed lists of Resources.
     *
     * @param sortorder
     */

    public final void setSortOrder(final short sortorder)
        {
        }


    /***********************************************************************************************
     * Get a flag indicating if the Resource is Editable.
     *
     * @return boolean
     */

    public final boolean isEditable()
        {
        return (false);
        }


    /***********************************************************************************************
     * Set a flag indicating if the Resource is Editable.
     *
     * @param flag
     */

    public final void setEditable(final boolean flag)
        {
        }


    /***********************************************************************************************
     * Get the class name of the ResourceEditor.
     *
     * @return String
     */

    public final String getEditorClassname()
        {
        return (EMPTY_STRING);
        }


    /***********************************************************************************************
     * Set the class name of the ResourceEditor.
     *
     * @param classname
     */

    public final void setEditorClassname(final String classname)
        {
        }


    /***********************************************************************************************
     * Get the Resource Description.
     *
     * @return String
     */

    public final String getDescription()
        {
        return (EMPTY_STRING);
        }


    /***********************************************************************************************
     * Set the Resource Description.
     *
     * @param description
     */

    public final void setDescription(final String description)
        {
        }


    /***********************************************************************************************
     * Get the IconFilename.
     *
     * @return String
     */

    public final String getIconFilename()
        {
        return (EMPTY_STRING);
        }


    /***********************************************************************************************
     * Set the IconFilename.
     *
     * @param filename
     */

    public final void setIconFilename(final String filename)
        {
        }


    /**********************************************************************************************/
    /* User Interface and Debugging                                                               */
    /***********************************************************************************************
     * Override toString() to provide the User name which appears on the navigation tree.
     *
     * @return String
     */

    public String toString()
        {
        return(getName());
        }


    /***********************************************************************************************
     * Run the UI of this UserObjectPlugin when its tree node is selected.
     * setUIOccupant() uses this from the navigation tree, a menu, or a toolbar button.
     */

    public final void runUI()
        {
        }


    /***********************************************************************************************
     * Stop the UI of this UserObjectPlugin when its tree node is deselected.
     * clearUIOccupant() uses this from the navigation tree, a menu, or a toolbar button.
     */

    public final void stopUI()
        {
        }


    /***********************************************************************************************
     * The action to be performed when the tree node containing this Resource is selected.
     *
     * @param event
     * @param mode
     */

    public final void actionPerformed(final AWTEvent event,
                                      final boolean mode)
        {
        }


    /***********************************************************************************************
     * Show the Role debug data.
     */

    public void showDebugData()
        {
        if (getDebugMode())
            {
            LOGGER.debug("Role");
            LOGGER.debug(INDENT + "[id=" + getID() + "]");
            LOGGER.debug(INDENT + "[resourcekey=" + getResourceKey() + "]");
            LOGGER.debug(INDENT + "[rolename=" + getName() + "]");

            LOGGER.debug(INDENT + "[atomviewer=" + isAtomViewer() + "]");
            LOGGER.debug(INDENT + "[atomeditor=" + isAtomEditor() + "]");
            LOGGER.debug(INDENT + "[taskviewer=" + isTaskViewer() + "]");
            LOGGER.debug(INDENT + "[taskeditor=" + isTaskEditor() + "]");
            LOGGER.debug(INDENT + "[resourceviewer=" + isResourceViewer() + "]");
            LOGGER.debug(INDENT + "[resourceeditor=" + isResourceEditor() + "]");
            LOGGER.debug(INDENT + "[userviewer=" + isUserViewer() + "]");
            LOGGER.debug(INDENT + "[usereditor=" + isUserEditor() + "]");
            LOGGER.debug(INDENT + "[usercreator=" + isUserCreator() + "]");
            LOGGER.debug(INDENT + "[userdeletor=" + isUserDeletor() + "]");
            LOGGER.debug(INDENT + "[description=" + getDescription() + "]");
            }
        }


    /***********************************************************************************************
     * Get the XML part of the Role.
     *
     * @return XmlObject
     */

     public final Role getXml()
         {
         if (super.getXml() == null)
             {
             throw new FrameworkException(EXCEPTION_PARAMETER_NULL);
             }

         return ((Role)super.getXml());
         }
    }


//--------------------------------------------------------------------------------------------------
// End of File
