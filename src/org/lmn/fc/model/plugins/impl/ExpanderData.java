package org.lmn.fc.model.plugins.impl;

import org.lmn.fc.common.exceptions.FrameworkException;
import org.lmn.fc.model.plugins.AtomPlugin;
import org.lmn.fc.model.plugins.ExpanderInterface;
import org.lmn.fc.model.registry.RegistryModelPlugin;
import org.lmn.fc.model.xmlbeans.plugins.Plugin;
import org.lmn.fc.model.xmlbeans.roles.RoleName;
import org.lmn.fc.ui.UIComponentPlugin;
import org.lmn.fc.ui.components.BlankUIComponent;

import javax.management.ObjectName;
import java.util.GregorianCalendar;
import java.util.List;


/***************************************************************************************************
 *
 */

public final class ExpanderData extends AtomData
                                implements ExpanderInterface
    {
    private static final long VERSION_ID = -1303451462677114028L;

    private final String strPathname;
    private final String strName;
    private String strIconFilename;


    /***********************************************************************************************
     * Constructor.
     *
     * @param pathname
     * @param name
     * @param iconfilename
     * @param treenode
     */

//    public ExpanderData(final String pathname,
//                        final String name,
//                        final String iconfilename,
//                        final DefaultMutableTreeNode treenode)
//        {
//        super(VERSION_ID, false);
//
//        // This could check for null references...
//        this.strPathname = pathname;
//        this.strName = name;
//        this.strIconFilename = iconfilename;
//
//        // Bidirectionally link the NullData to the host node
//        getHostTreeNode().setUserObject(this);
//
//        // Create a default panel
//        setUIComponent(new BlankUIComponent());
//        }


    /***********************************************************************************************
     *
     * @param pathname
     * @param name
     * @param iconfilename
     */

    public ExpanderData(final String pathname,
                        final String name,
                        final String iconfilename)
        {
        super(VERSION_ID, false);

        // This could check for null references...
        this.strPathname = pathname;
        this.strName = name;
        this.strIconFilename = iconfilename;

        // Bidirectionally link the ExpanderData to the host node
        getHostTreeNode().setUserObject(this);

        // Create a default panel
        setUIComponent(new BlankUIComponent());
        }


    /***********************************************************************************************
     *
     */

//    public ExpanderData()
//        {
//        super(VERSION_ID, false);
//
//        // This could check for null references...
//        this.strPathname = EMPTY_STRING;
//        this.strName = EMPTY_STRING;
//        this.strIconFilename = EMPTY_STRING;
//
//        // Bidirectionally link the NullData to the host node
//        getHostTreeNode().setUserObject(this);
//
//        // Create a default panel
//        setUIComponent(new BlankUIComponent());
//        }


    public ObjectName getObjectName()
        {
        return null;
        }

    /***********************************************************************************************
     * Start up the Atom.
     *
     * @return boolean
     */

    public boolean startupAtom()
        {
        if (getUIComponent() != null)
            {
            getUIComponent().initialiseUI();
            }

        return (true);
        }


    /***********************************************************************************************
     * Shut down the Atom.
     *
     * @return boolean
     */

    public boolean shutdownAtom()
        {
        stopUI();
        clearUserObjectContextActionGroups();

        if (getUIComponent() != null)
            {
            getUIComponent().disposeUI();
            setUIComponent(null);
            }

        return (true);
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
        return (null);
        }


    /***********************************************************************************************
     * Set the StartDate for this Task.
     *
     * @param calendar
     */

    public final void setStartDate(final GregorianCalendar calendar)
        {
        }


    /***********************************************************************************************
     * Get the StartTime for this Task.
     *
     * @return GregorianCalendar
     */

    public final GregorianCalendar getStartTime()
        {
        return (null);
        }


    /***********************************************************************************************
     * Set the StartTime for this Task.
     *
     * @param calendar
     */

    public final void setStartTime(final GregorianCalendar calendar)
        {
        }


    /***********************************************************************************************
     * Get the StopDate for this Task.
     *
     * @return GregorianCalendar
     */

    public final GregorianCalendar getStopDate()
        {
        return (null);
        }


    /***********************************************************************************************
     * Set the StopDate for this Task.
     *
     * @param calendar
     */

    public final void setStopDate(final GregorianCalendar calendar)
        {
        }


    /***********************************************************************************************
     * Get the StopTime for this Task.
     *
     * @return GregorianCalendar
     */

    public final GregorianCalendar getStopTime()
        {
        return (null);
        }


    /***********************************************************************************************
     * Set the StopTime for this Task.
     *
     * @param calendar
     */

    public final void setStopTime(final GregorianCalendar calendar)
        {
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

    public void startupTasks()
        {
        //To change body of implemented methods use File | Settings | File Templates.
        }

    public void shutdownTasks()
        {
        //To change body of implemented methods use File | Settings | File Templates.
        }

    public void updateTasks()
        {
        //To change body of implemented methods use File | Settings | File Templates.
        }


    public void registerAtom(final AtomPlugin atom, final ObjectName name)
        {
        //To change body of implemented methods use File | Settings | File Templates.
        }
    /**
     * ********************************************************************************************
     * Get the Editable field.
     *
     * @return boolean
     */

    public boolean isEditable()
        {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
        }

    /**
     * ********************************************************************************************
     * Set the Editable field.
     *
     * @param editable
     */
    public void setEditable(final boolean editable)
        {
        //To change body of implemented methods use File | Settings | File Templates.
        }

    /**
     * ********************************************************************************************
     * Get the class name of the ResourceEditor.
     *
     * @return String
     */

    public String getEditorClassname()
        {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

    /**
     * ********************************************************************************************
     * Set the class name of the ResourceEditor.
     *
     * @param classname
     */

    public void setEditorClassname(final String classname)
        {
        //To change body of implemented methods use File | Settings | File Templates.
        }

    /**
     * ********************************************************************************************
     * Get the IconFilename field.
     *
     * @return String
     */

    public String getIconFilename()
        {
        return (this.strIconFilename);
        }

    /**
     * ********************************************************************************************
     * Set the IconFilename field.
     *
     * @param filename
     */

    public void setIconFilename(final String filename)
        {
        this.strIconFilename = filename;
        }

    /**
     * ********************************************************************************************
     * Get the Description field
     *
     * @return String
     */

    public String getDescription()
        {
        return (this.strPathname);
        }

    /**
     * ********************************************************************************************
     * Set the Description field
     *
     * @param description
     */

    public void setDescription(final String description)
        {

        }

    /***********************************************************************************************
     * Get the DateCreated field.
     *
     * @return Date
     */

    public final GregorianCalendar getCreatedDate()
        {
        return (null);
        }


    /***********************************************************************************************
     * Set the DateCreated field.
     *
     * @param calendar
     */

    public final void setCreatedDate(final GregorianCalendar calendar)
        {
        }


    /***********************************************************************************************
     * Get the TimeCreated field.
     *
     * @return Time
     */

    public final GregorianCalendar getCreatedTime()
        {
        return (null);
        }


    /***********************************************************************************************
     * Set the TimeCreated field.
     *
     * @param calendar
     */

    public final void setCreatedTime(final GregorianCalendar calendar)
        {
        }


    /***********************************************************************************************
     * Get the DateModified field.
     *
     * @return Date
     */

    public final GregorianCalendar getModifiedDate()
        {
        return (null);
        }


    /***********************************************************************************************
     * Set the DateModified field.
     *
     * @param calendar
     */

    public final void setModifiedDate(final GregorianCalendar calendar)
        {
        }


    /***********************************************************************************************
     * Get the TimeModified field.
     *
     * @return Time
     */

    public final GregorianCalendar getModifiedTime()
        {
        return (null);
        }


    /***********************************************************************************************
     * Set the TimeModified field.
     *
     * @param calendar
     */

    public final void setModifiedTime(final GregorianCalendar calendar)
        {
        }

    /***********************************************************************************************
     * Run the UI of this UserObjectPlugin when its tree node is selected.
     */

    public void runUI()
        {
        }


    /***********************************************************************************************
     * Stop the UI of this UserObjectPlugin when its tree node is deselected.
     */

    public void stopUI()
        {
        }


    /***********************************************************************************************
     * Get the ResourceKey for the NullData.
     *  ToDo FIX THIS!!
     * @return String
     */

    public final String getResourceKey()
        {
        return ("");
        }


    /***********************************************************************************************
     * Get the full pathname of the NullData object.
     *
     * @return String
     */

    public final String getPathname()
        {
        if (this.strPathname.equals(""))
            {
            return (this.strName);
            }
        else if (this.strName.equals(""))
            {
            return (this.strPathname);
            }
        else
            {
            return (this.strPathname + RegistryModelPlugin.DELIMITER_RESOURCE + this.strName);
            }
        }


    /***********************************************************************************************
     * Get the name of the NullData object.
     *
     * @return String
     */

    public final String getName()
        {
        return (this.strName);
        }

    /**
     * ********************************************************************************************
     * Set the AtomName field.
     *
     * @param name
     */

    public void setName(final String name)
        {
        //To change body of implemented methods use File | Settings | File Templates.
        }


    /**
     * ********************************************************************************************
     * Get the Active field.
     *
     * @return boolean
     */

    public boolean isActive()
        {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
        }

    /**
     * ********************************************************************************************
     * Set the Active field.
     *
     * @param active
     */

    public void setActive(final boolean active)
        {
        //To change body of implemented methods use File | Settings | File Templates.
        }

    public boolean isLoadAtStart()
        {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
        }

    public void setLoadAtStart(final boolean load)
        {
        //To change body of implemented methods use File | Settings | File Templates.
        }

    /**
     * ********************************************************************************************
     * Get the HelpFilename field.
     *
     * @return String
     */

    public String getHelpFilename()
        {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

    /**
     * ********************************************************************************************
     * Set the HelpFilename field.
     *
     * @param filename
     */

    public void setHelpFilename(final String filename)
        {
        //To change body of implemented methods use File | Settings | File Templates.
        }

    /**
     * ********************************************************************************************
     * Get the filename of the Atom AboutBox HTML file.
     *
     * @return String
     */

    public String getAboutFilename()
        {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

    /**
     * ********************************************************************************************
     * Set the filename of the Framework AboutBox HTML file.
     *
     * @param filename
     */

    public void setAboutFilename(final String filename)
        {
        //To change body of implemented methods use File | Settings | File Templates.
        }

    public UIComponentPlugin aboutBox()
        {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

    public String getTooltip()
        {
        return getName();
        }


    public void readResources()
        {
        //To change body of implemented methods use File | Settings | File Templates.
        }

    public void showDebugData()
        {
        if (getDebugMode())
            {

            }
        }


    /***********************************************************************************************
     * Override toString() in order to be able to name this object for the tree.
     *
     * @return String
     */

    public final String toString()
        {
        return (this.strName);
        }


    /***********************************************************************************************
     * Todo move to AtomData Get the XML part of the Atom.
     *
     * @return Plugin
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
