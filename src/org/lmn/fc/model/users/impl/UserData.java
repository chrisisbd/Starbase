//--------------------------------------------------------------------------------------------------
// Revision History
//
//  25-11-04    LMN created file
//  30-04-06    LMN implementing!
//  15-05-06    LMN seemed to have finished?!
//
//--------------------------------------------------------------------------------------------------

package org.lmn.fc.model.users.impl;

//--------------------------------------------------------------------------------------------------

import org.lmn.fc.common.exceptions.FrameworkException;
import org.lmn.fc.common.xml.XmlBeansUtilities;
import org.lmn.fc.model.root.impl.RootData;
import org.lmn.fc.model.users.RolePlugin;
import org.lmn.fc.model.users.UserPlugin;
import org.lmn.fc.model.xmlbeans.roles.RoleName;
import org.lmn.fc.model.xmlbeans.users.User;
import org.lmn.fc.model.registry.RegistryModelUtilities;

import java.awt.*;
import java.sql.Date;
import java.sql.Time;


/***************************************************************************************************
 * The UserData.
 */

public final class UserData extends RootData
                            implements UserPlugin
    {
    // These items are not available in the initial XML file
    private Date dateLastLogin;
    private Time timeLastLogin;

    private RolePlugin rolePlugin;


    /***********************************************************************************************
     * Construct a UserData from the specified XML Object.
     * Look up the Role in the Registry which corresponds to the RoleName.
     * The Roles <b>must</b> have been loaded before attempting to load the Users!
     *
     * @param user
     */

    public UserData(final User user)
        {
        super(8622627436819452452L);

        if ((user == null)
            || (!XmlBeansUtilities.isValidXml(user)))
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_INVALID);
            }

        setXml(user);

        // Assign a Role
        final String strRoleKey;

        // Check that the RoleName corresponds to a Role in the Registry
        strRoleKey = PREFIX_ROLE + KEY_DELIMITER + user.getRoleName();

        if (REGISTRY.getRoles().containsKey(strRoleKey))
            {
            // Assign the Role to the User
            setRole(REGISTRY.getRole(strRoleKey));
            }
        else
            {
            // Unable to assign a UserRole
            throw new FrameworkException(EXCEPTION_PARAMETER_INVALID);
            }

        // Check the complete User configuration, including the Role
        if (!RegistryModelUtilities.isValidUser(this))
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_INVALID);
            }

        // Make the Users accessible on the navigation tree
        getHostTreeNode().setUserObject(this);
        }


    /**********************************************************************************************/
    /* UserPlugin implementations                                                                 */
    /***********************************************************************************************
     * Get the Password.
     *
     * @return String
     */

    public final String getPassword()
        {
        return (getXml().getPassword());
        }


    /***********************************************************************************************
     * Set the Password.
     *
     * @param password
     */

    public final void setPassword(final String password)
        {
        getXml().setPassword(password);
        updateRoot();
        }


    /***********************************************************************************************
     * Get the RoleName.
     *
     * @return String
     */

    public final String getRoleName()
        {
        return (getXml().getRoleName().toString());
        }


    /***********************************************************************************************
     * Set the RoleName.
     *
     * @param name
     */

    public final void setRoleName(final String name)
        {
        // Only allow valid RoleNames to be set
        if ((name == null)
            || (RoleName.Enum.forString(name) == null))
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_INVALID);
            }

        getXml().setRoleName(RoleName.Enum.forString(name));
        updateRoot();
        }


    /***********************************************************************************************
     * Get the Country Code.
     *
     * @return String
     */

    public final String getCountryCode()
        {
        return (getXml().getCountryCode());
        }


    /***********************************************************************************************
     * Set the Country Code.
     *
     * @param code
     */

    public final void setCountryCode(final String code)
        {
        getXml().setCountryCode(code);
        updateRoot();
        }


    /***********************************************************************************************
     * Get the Language Code.
     *
     * @return String
     */

    public final String getLanguageCode()
        {
        return (getXml().getLanguageCode());
        }


    /***********************************************************************************************
     * Set the Language Code.
     *
     * @param code
     */

    public final void setLanguageCode(final String code)
        {
        getXml().setLanguageCode(code);
        updateRoot();
        }


    /***********************************************************************************************
     * Get the Email.
     *
     * @return String
     */

    public final String getEmail()
        {
        return (getXml().getEmail());
        }


    /***********************************************************************************************
     * Set the Email.
     *
     * @param email
     */

    public final void setEmail(final String email)
        {
        getXml().setEmail(email);
        updateRoot();
        }


    /***********************************************************************************************
     * Get the Date of the last login.
     *
     * @return Date
     */

    public final Date getDateLastLogin()
        {
        return (this.dateLastLogin);
        }


    /***********************************************************************************************
     * Set the Date of the last login.
     *
     * @param date
     */

    public final void setDateLastLogin(final Date date)
        {
        this.dateLastLogin = date;
        updateRoot();
        }


    /***********************************************************************************************
     * Get the Date of the last login.
     *
     * @return Time
     */

    public final Time getTimeLastLogin()
        {
        return (this.timeLastLogin);
        }


    /***********************************************************************************************
     * Set the Time of the last login.
     *
     * @param time
     */

    public final void setTimeLastLogin(final Time time)
        {
        this.timeLastLogin = time;
        updateRoot();
        }


    /**********************************************************************************************/
    /* RootPlugin implementations                                                                 */
    /***********************************************************************************************
     * Get the User ResourceKey.
     *
     * @return String
     */

    public final String getResourceKey()
        {
        return (PREFIX_USER + KEY_DELIMITER + getName());
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

    public final String getName()
        {
        return (getXml().getUserName());
        }


    /***********************************************************************************************
     * Set the Name.
     *
     * @param name
     */

    public final void setName(final String name)
        {
        getXml().setUserName(name);
        updateRoot();
        }


    /***********************************************************************************************
     * Get a flag indicating if this Resource is active.
     *
     * @return boolean
     */

    public final boolean isActive()
        {
        return (getXml().getActive());
        }


    /***********************************************************************************************
     * Set a flag indicating if this Resource is active.
     *
     * @param flag
     */

    public final void setActive(final boolean flag)
        {
        getXml().setActive(flag);
        updateRoot();
        }


    /***********************************************************************************************
     * Get the sort order for displayed lists of Resources.
     *
     * @return short
     */

    public final short getSortOrder()
        {
        return (getXml().getSortOrder());
        }


    /***********************************************************************************************
     * Set the sort order for displayed lists of Resources.
     *
     * @param sortorder
     */

    public final void setSortOrder(final short sortorder)
        {
        getXml().setSortOrder(sortorder);
        updateRoot();
        }


    /***********************************************************************************************
     * Get a flag indicating if the Resource is Editable.
     *
     * @return boolean
     */

    public final boolean isEditable()
        {
        return (getXml().getEditable());
        }


    /***********************************************************************************************
     * Set a flag indicating if the Resource is Editable.
     *
     * @param flag
     */

    public final void setEditable(final boolean flag)
        {
        getXml().setEditable(flag);
        updateRoot();
        }


    /***********************************************************************************************
     * Get the class name of the ResourceEditor.
     *
     * @return String
     */

    public final String getEditorClassname()
        {
        return (getXml().getEditorClassname());
        }


    /***********************************************************************************************
     * Set the class name of the ResourceEditor.
     *
     * @param classname
     */

    public final void setEditorClassname(final String classname)
        {
        getXml().setEditorClassname(classname);
        updateRoot();
        }


    /***********************************************************************************************
     * Get the Resource Description.
     *
     * @return String
     */

    public final String getDescription()
        {
        return (getXml().getDescription());
        }


    /***********************************************************************************************
     * Set the Resource Description.
     *
     * @param description
     */

    public final void setDescription(final String description)
        {
        getXml().setDescription(description);
        updateRoot();
        }


    /***********************************************************************************************
     * Get the IconFilename.
     *
     * @return String
     */

    public final String getIconFilename()
        {
        return (getXml().getIconFilename());
        }


    /***********************************************************************************************
     * Set the IconFilename.
     *
     * @param filename
     */

    public final void setIconFilename(final String filename)
        {
        getXml().setIconFilename(filename);
        updateRoot();
        }


    /***********************************************************************************************
     * Get the RolePlugin associated with this User.
     *
     * @return RolePlugin
     */

    public RolePlugin getRole()
        {
        return (this.rolePlugin);
        }


    /***********************************************************************************************
     * Set the RolePlugin to be associated with this User.
     *
     * @param role
     */

    public void setRole(final RolePlugin role)
        {
        if (role != null)
            {
            setRoleName(role.getName());
            this.rolePlugin = role;
            }
        else
            {
            // Unable to assign a UserRole
            throw new FrameworkException(EXCEPTION_PARAMETER_NULL);
            }
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
     * Show the Resource debug data.
     */

    public final void showDebugData()
        {
        if (getDebugMode())
            {
            LOGGER.debug("User");
            LOGGER.debug(INDENT + "[id=" + getID() + "]");
            LOGGER.debug(INDENT + "[resourcekey=" + getResourceKey() + "]");
            LOGGER.debug(INDENT + "[pathname=" + getPathname() + "]");

            LOGGER.debug(INDENT + "[name=" + getName() + "]");
            LOGGER.debug(INDENT + "[password=" + getPassword() + "]");
            LOGGER.debug(INDENT + "[isactive=" + isActive() + "]");
            LOGGER.debug(INDENT + "[sortorder=" + getSortOrder() + "]");
            LOGGER.debug(INDENT + "[editable=" + isEditable() + "]");
            LOGGER.debug(INDENT + "[userrole=" + getRoleName() + "]");
            LOGGER.debug(INDENT + "[country=" + getCountryCode() + "]");
            LOGGER.debug(INDENT + "[language=" + getLanguageCode() + "]");
            LOGGER.debug(INDENT + "[email=" + getEmail() + "]");
            LOGGER.debug(INDENT + "[editorclassname=" + getEditorClassname() + "]");
            LOGGER.debug(INDENT + "[description=" + getDescription() + "]");
            LOGGER.debug(INDENT + "[iconfilename=" + getIconFilename() + "]");

            LOGGER.debug(INDENT + "[datelastlogin=" + getDateLastLogin() + "]");
            LOGGER.debug(INDENT + "[timelastlogin=" + getTimeLastLogin() + "]");
            LOGGER.debug(INDENT + "[updated=" + isUpdated() + "]");
            }
        }


    /***********************************************************************************************
     * Get the XML part of the Resource.
     *
     * @return XmlObject
     */

     public final User getXml()
         {
         if (super.getXml() == null)
             {
             throw new FrameworkException(EXCEPTION_PARAMETER_NULL);
             }

         return ((User)super.getXml());
         }
   }


//--------------------------------------------------------------------------------------------------
// End of File
