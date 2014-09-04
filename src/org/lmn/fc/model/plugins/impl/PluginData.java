package org.lmn.fc.model.plugins.impl;

import org.lmn.fc.common.actions.ContextActionGroup;
import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.common.exceptions.FrameworkException;
import org.lmn.fc.model.plugins.AtomPlugin;
import org.lmn.fc.model.registry.RegistryModelPlugin;
import org.lmn.fc.model.registry.RegistryModelUtilities;
import org.lmn.fc.model.root.ActionGroup;
import org.lmn.fc.model.root.RootPlugin;
import org.lmn.fc.model.root.RootType;
import org.lmn.fc.model.xmlbeans.plugins.Plugin;
import org.lmn.fc.model.xmlbeans.roles.RoleName;
import org.lmn.fc.ui.UIComponentPlugin;
import org.lmn.fc.ui.panels.PDFPanel;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.io.Serializable;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;


/***************************************************************************************************
 * PluginData object to associate with the RegistryModel.
 */

public abstract class PluginData extends AtomData
                                 implements AtomPlugin,
                                            FrameworkConstants,
                                            FrameworkStrings,
                                            FrameworkMetadata,
                                            Serializable
    {
    /***********************************************************************************************
     * Pseudo-Singleton Constructor.
     *
     * @param id
     */

    protected PluginData(final long id)
        {
        super(id, true);

        this.setType(RootType.ATOM);
        this.setParentAtom(null);
        this.setLevel(null);
        }


    /***********************************************************************************************
     * Get the MBean server ObjectName for the Plugin.
     * The syntax is <code>framework.plugin:Name=plugin,Level=levelname</code>.
     *
     * @return ObjectName
     */

    public ObjectName getObjectName()
        {
        final ObjectName objectName;

        try
            {
            objectName = new ObjectName(getPathname()
                                            + COLON
                                            + "Name="
                                            + getName()
                                            + ",Level="
                                            + getLevel());
            }

        catch (MalformedObjectNameException exception)
            {
            throw new FrameworkException(EXCEPTION_OBJECT_NAME, exception);
            }

        return(objectName);
        }


    /***********************************************************************************************
     * Startup the Atom.
     *
     * @return boolean
     */

    public boolean startupAtom()
        {
        // Make a ContextActionGroup for this plugin, at index ActionGroup.PLUGIN
        clearUserObjectContextActionGroups();
        addUserObjectContextActionGroup(new ContextActionGroup(ActionGroup.PLUGIN.getName(),
                                                               true,
                                                               true));
        return(true);
        }


    /***********************************************************************************************
     * Shut down the Atom.
     *
     * @return boolean
     */

    public boolean shutdownAtom()
        {
        // Clear the ContextActionGroups for the Atom
        clearUserObjectContextActionGroups();

        return(true);
        }


    /***********************************************************************************************
     * Create the Plugin's AboutBox.
     *
     * @return UIComponentPlugin
     */

    public final UIComponentPlugin aboutBox()
        {
        final UIComponentPlugin plugin;

        plugin = new PDFPanel(RegistryModelUtilities.getAboutURL(this));
        plugin.initialiseUI();
        plugin.runUI();

        return (plugin);
        }


    /***********************************************************************************************
     * Get the tooltip to be used at the Plugin's PointOfInterest.
     *
     * @return String
     */

    public final String getTooltip()
        {
        return (getName());
        }


    /***********************************************************************************************
     * Read all Resources required by the Plugin, which was set when the Plugin was loaded.
     */

    public void readResources()
        {
        // Use the RegistryModel's debug mode
        setDebugMode(REGISTRY_MODEL.getDebugMode());
        }


    /***********************************************************************************************
     * Get the full RegistryModel pathname for the Plugin.
     *
     * @return String
     */

    public final String getPathname()
        {
        return (getParentAtom().getResourceKey() + getName());
        }


    /***********************************************************************************************
     * Get the ResourceKey for the Plugin.
     *
     * @return String
     */

    public final String getResourceKey()
        {
        return (getPathname() + RegistryModelPlugin.DELIMITER_RESOURCE);
        }


    /***********************************************************************************************
     * Override toString() in order to be able to name this object for the JTree.
     *
     * @return String
     */

    public final String toString()
        {
        return (getName());
        }


    /**********************************************************************************************/
    /* Methods to access the XML part of the Plugin.                                              */
    /***********************************************************************************************
     * Get the AtomName field.
     *
     * @return String
     */

    public final String getName()
        {
        return (getXml().getName());
        }


    /***********************************************************************************************
     * Set the AtomName field.
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
     * @return List<RoleName.Enum>
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
     * Get the Editable field.
     *
     * @return boolean
     */

    public final boolean isEditable()
        {
        return (getXml().getEditable());
        }


    /***********************************************************************************************
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
     * Get the Active field.
     *
     * @return boolean
     */

    public final boolean isActive()
        {
        return (getXml().getActive());
        }


    /***********************************************************************************************
     * Set the Active field.
     *
     * @param active
     */

    public final void setActive(final boolean active)
        {
        if (getXml().getActive() != active)
            {
            // Tell everyone that the Active flag has changed
            notifyActiveChangeEvent(this,
                                    this.getPathname(),
                                    getXml().getActive(),
                                    active,
                                    getDebugMode());

            getXml().setActive(active);
            updateRoot();
            }
        }


    /***********************************************************************************************
    * Get the LoadAtStart field.
    *
    * @return boolean
    */

   public final boolean isLoadAtStart()
       {
       return (getXml().getLoadAtStart());
       }


   /***********************************************************************************************
    * Set the LoadAtStart field.
    *
    * @param load
    */

   public final void setLoadAtStart(final boolean load)
       {
       getXml().setLoadAtStart(load);
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
     * Get the Description.
     *
     * @return String
     */

    public final String getDescription()
        {
        return (getXml().getDescription());
        }


    /***********************************************************************************************
     * Set the Description.
     *
     * @param description
     */

    public final void setDescription(final String description)
        {
        getXml().setDescription(description);
        updateRoot();
        }


    /***********************************************************************************************
     * Get the IconFilename field.
     *
     * @return String
     */

    public final String getIconFilename()
        {
        return (getXml().getIconFilename());
        }


    /***********************************************************************************************
     * Set the IconFilename field.
     *
     * @param filename
     */

    public final void setIconFilename(final String filename)
        {
        getXml().setIconFilename(filename);
        updateRoot();
        }


    /***********************************************************************************************
     * Get the HelpFilename field.
     *
     * @return String
     */

    public final String getHelpFilename()
        {
        return (getXml().getHelpFilename());
        }


    /***********************************************************************************************
     * Set the HelpFilename field.
     *
     * @param filename
     */

    public final void setHelpFilename(final String filename)
        {
        getXml().setHelpFilename(filename);
        updateRoot();
        }


    /***********************************************************************************************
     * Get the filename of the Plugin AboutBox HTML file.
     *
     * @return String
     */

    public final String getAboutFilename()
        {
        return (getXml().getAboutFilename());
        }


    /***********************************************************************************************
     * Set the filename of the Plugin AboutBox HTML file.
     *
     * @param filename
     */

    public final void setAboutFilename(final String filename)
        {
        getXml().setAboutFilename(filename);
        updateRoot();
        }


    /**********************************************************************************************/
    /* XML Persistence Dates and Times                                                            */
    /***********************************************************************************************
     * Get the StartDate for this Plugin.
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
     * Set the StartDate for this Plugin.
     *
     * @param calendar
     */

    public final void setStartDate(final GregorianCalendar calendar)
        {
        getXml().setStartDate(calendar);
        updateRoot();
        }


    /***********************************************************************************************
     * Get the StartTime for this Plugin.
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
     * Set the StartTime for this Plugin.
     *
     * @param calendar
     */

    public final void setStartTime(final GregorianCalendar calendar)
        {
        getXml().setStartTime(calendar);
        updateRoot();
        }


    /***********************************************************************************************
     * Get the StopDate for this Plugin.
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
     * Set the StopDate for this Plugin.
     *
     * @param calendar
     */

    public final void setStopDate(final GregorianCalendar calendar)
        {
        getXml().setStopDate(calendar);
        updateRoot();
        }


    /***********************************************************************************************
     * Get the StopTime for this Plugin.
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
     * Set the StopTime for this Plugin.
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
     * See if any Task has changed, and so needs to be written to the database.
     */

    public final void updateTasks()
        {
        final Iterator<RootPlugin> iterateTasks;
        RootPlugin pluginTask;

        iterateTasks = getTasks().iterator();

        while ((iterateTasks != null)
            && (iterateTasks.hasNext()))
            {
            pluginTask = iterateTasks.next();

            if ((pluginTask != null)
                && (pluginTask.isUpdated()))
                {
                // ToDo UPDATE DATA!!!!!!!!!!!!
                //pluginTask.updateData();
                LOGGER.debug(getDebugMode(), DOT + pluginTask.getName() + " updated");
                }
            }
        }


    /**********************************************************************************************/
    /* Utilities                                                                                  */
    /***********************************************************************************************
     * Fully debug the Plugin.
     */

     public final void showDebugData()
         {
         if (getDebugMode())
             {
             LOGGER.log("Plugin Miscellaneous");
             LOGGER.log(INDENT + "[ID=" + getID() + "]");
             LOGGER.log(INDENT + "[Level=" + getLevel() + "]");

             LOGGER.log(INDENT + "[Pathname=" + getPathname() + "]");
             LOGGER.log(INDENT + "[ResourceKey=" + getResourceKey() + "]");
             LOGGER.log(INDENT + "[ClassFound=" + isClassFound() + "]");
             LOGGER.log(INDENT + "[Locked=" + isLocked() + "]");
             LOGGER.log(INDENT + "[Updated=" + isUpdated() + "]");
             LOGGER.log(INDENT + "[UpdatesAllowed=" + isUpdateAllowed() + "]");
             LOGGER.log(INDENT + "[DebugMode=" + getDebugMode() + "]");

             LOGGER.log("Plugin XML Bean");
             LOGGER.log(INDENT + "[Name=" + getName() + "]");
             LOGGER.log(INDENT + "[UserRoleMask=" + getUserRoles() + "]");
             LOGGER.log(INDENT + "[Editable=" + isEditable() + "]");
             LOGGER.log(INDENT + "[Active=" + isActive() + "]");
             LOGGER.log(INDENT + "[LoadAtStart=" + isLoadAtStart() + "]");
             LOGGER.log(INDENT + "[EditorClassname=" + getEditorClassname() + "]");
             LOGGER.log(INDENT + "[Description=" + getDescription() + "]");
             LOGGER.log(INDENT + "[IconFilename=" + getIconFilename() + "]");
             LOGGER.log(INDENT + "[HelpFilename=" + getHelpFilename() + "]");
             LOGGER.log(INDENT + "[AboutFilename=" + getAboutFilename() + "]");

             LOGGER.log("Plugin Dates and Times");
             LOGGER.log(INDENT + "[CreatedDate=" + getCreatedDate() + "]");
             LOGGER.log(INDENT + "[CreatedTime=" + getCreatedTime() + "]");
             LOGGER.log(INDENT + "[ModifiedDate=" + getModifiedDate() + "]");
             LOGGER.log(INDENT + "[ModifiedTime=" + getModifiedTime() + "]");
             }
         }


    /***********************************************************************************************
     * Validate the Plugin.
     *
     * @return boolean
     */

    public boolean validatePlugin()
        {
        boolean boolValid;

        boolValid = super.validatePlugin();

        // Todo review
//        if ((getUIComponent() == null)
//            || (getHostTreeNode() == null))
//            {
//            getValidationErrors().add("Failed validation in " + getName());
//            boolValid = false;
//            }

        return (boolValid);
        }


    /***********************************************************************************************
     * Get the XML part of the Plugin.
     *
     * @return XmlObject
     */

     public final Plugin getXml()
         {
         if (super.getXml() == null)
             {
             throw new FrameworkException(EXCEPTION_PARAMETER_NULL);
             }

         return ((Plugin)super.getXml());
         }
    }
