package org.lmn.fc.model.lookandfeels.impl;

import org.lmn.fc.common.exceptions.FrameworkException;
import org.lmn.fc.common.xml.XmlBeansUtilities;
import org.lmn.fc.model.lookandfeels.LookAndFeelPlugin;
import org.lmn.fc.model.root.impl.RootData;
import org.lmn.fc.model.xmlbeans.lookandfeels.LookAndFeel;

import java.awt.*;


/***************************************************************************************************
 * LookAndFeelData Bean.
 *
 * @author $author$
 * @version $Revision$
 */

public final class LookAndFeelData extends RootData
                                   implements LookAndFeelPlugin
    {
    /***********************************************************************************************
     * Creates a new LookAndFeelData object.
     *
     * @param lookandfeel -
     */

    public LookAndFeelData(final LookAndFeel lookandfeel)
        {
        super(4377013687936838861L);

        if ((lookandfeel == null)
            || (!XmlBeansUtilities.isValidXml(lookandfeel)))
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_INVALID);
            }

        setXml(lookandfeel);

        // Make the LookAndFeels accessible on the navigation tree
        getHostTreeNode().setUserObject(this);
        }


    /**********************************************************************************************/
    /* LookAndFeelPlugin implementations                                                          */
    /***********************************************************************************************
     * Get a flag indicating if this LookAndFeel is installed.
     *
     * @return boolean
     */

    public boolean isInstalled()
        {
        return (getXml().getInstalled());
        }


    /***********************************************************************************************
     * Set a flag indicating if this LookAndFeel is installed.
     *
     * @param flag
     */

    public void setInstalled(final boolean flag)
        {
        getXml().setInstalled(flag);
        updateRoot();
        }


    /***********************************************************************************************
     * Set the ClassName of the LookAndFeel.
     *
     * @return -
     */
    public String getClassName()
        {
        return (getXml().getClassName());
        }


    /***********************************************************************************************
     * Get the ClassName of the LookAndFeel.
     *
     * @param classname -
     */
    public void setClassName(final String classname)
        {
        getXml().setClassName(classname);
        updateRoot();
        }


    /***********************************************************************************************
     * Set the LicenceFilename of the LookAndFeel.
     *
     * @return -
     */
    public String getLicenceFilename()
        {
        return (getXml().getLicenceFilename());
        }


    /***********************************************************************************************
     * Get the LicenceFilename of the LookAndFeel.
     *
     * @param filename -
     */
    public void setLicenceFilename(final String filename)
        {
        getXml().setLicenceFilename(filename);
        updateRoot();
        }


    /**********************************************************************************************/
    /* RootPlugin implementations                                                                 */
    /***********************************************************************************************
     * Get the ResourceKey.
     *
     * @return String
     */

    public final String getResourceKey()
        {
        return (PREFIX_LOOKANDFEEL + KEY_DELIMITER + getName());
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
        return (getXml().getName());
        }


    /***********************************************************************************************
     * Set the Name.
     *
     * @param name
     */

    public final void setName(final String name)
        {
        getXml().setName(name);
        updateRoot();
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
     * Show the LookAndFeel debug data.
     */

    public void showDebugData()
        {
        if (getDebugMode())
            {
            LOGGER.debug("LookAndFeel");
            LOGGER.debug(INDENT + "[id=" + getID() + "]");
            LOGGER.debug(INDENT + "[resourcekey=" + getResourceKey() + "]");

            LOGGER.debug(INDENT + "[installed=" + isInstalled() + "]");
            LOGGER.debug(INDENT + "[name=" + getName() + "]");
            LOGGER.debug(INDENT + "[classname=" + getClassName() + "]");
            LOGGER.debug(INDENT + "[licencefilename=" + getLicenceFilename() + "]");
            }
        }


    /***********************************************************************************************
     * Get the XML part of the LookAndFeel.
     *
     * @return XmlObject
     */

     public final LookAndFeel getXml()
         {
         if (super.getXml() == null)
             {
             throw new FrameworkException(EXCEPTION_PARAMETER_NULL);
             }

         return ((LookAndFeel)super.getXml());
         }
    }
