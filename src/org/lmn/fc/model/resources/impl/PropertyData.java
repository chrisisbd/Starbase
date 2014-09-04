//--------------------------------------------------------------------------------------------------
// Revision History
//
//  23-01-02    LMN created file
//  29-01-02    LMN added functionality
//  04-02-02    LMN added separate instance variables for each DataType
//  05-02-02    LMN finished the first complete version!
//  05-03-02    LMN added TimingMode
//  22-04-02    LMN added new Astronomy data types
//  15-04-03    LMN changed for Icon column
//  19-04-03    LMN added link back to host DefaultMutableTreeNode
//  27-04-03    LMN added setUIComponent() and getUIComponent()
//  02-05-03    LMN changed Properties table
//  13-06-03    LMN modified to be a superclass of ApplicationProperty and ComponentProperty
//  02-07-03    LMN moved to model.datatypes, for PropertyPlugins
//  07-07-03    LMN adding property instantiation on load
//  13-07-03    LMN added iterateApplicationProperties()
//  15-07-03    LMN added writeValue()
//  18-07-03    LMN switched to new storage method
//  22-09-03    LMN extending ResourceData...
//  23-09-03    LMN added Property accessors from RegistryModel
//  29-09-03    LMN tidying up...
//  26-05-04    LMN added aboutDataTypes()
//  04-10-04    LMN removed aboutDataTypes(), added getDataTypesReport()
//  14-10-04    LMN added getURLProperty() and setURLProperty()
//  18-10-04    LMN moved DataTypes to the class DataType
//  26-10-04    LMN sorting out Column Names etc.
//  03-11-05    LMN adding Insert & Delete scripts
//  22-12-05    LMN making major changes for new XMLBeans and DAO architecture...
//  31-01-06    LMN moved all Property accessors to the Registry
//
//--------------------------------------------------------------------------------------------------

package org.lmn.fc.model.resources.impl;

import org.lmn.fc.common.exceptions.FrameworkException;
import org.lmn.fc.common.xml.XmlBeansUtilities;
import org.lmn.fc.model.plugins.AtomPlugin;
import org.lmn.fc.model.locale.LanguagePlugin;
import org.lmn.fc.model.resources.PropertyPlugin;
import org.lmn.fc.model.xmlbeans.properties.PropertyResource;

import java.util.List;
import java.util.Iterator;
import java.util.GregorianCalendar;


/***************************************************************************************************
 * PropertyData to associate with RegistryModel TreeNode.
 */

public final class PropertyData extends ResourceData implements PropertyPlugin
    {
    private static final long VERSION_ID = -8254871350879147447L;

    // Indicate if the Property can be instantiated using its DataType classname
    private boolean boolInstantiated;

    /**********************************************************************************************/
    /* Instance methods                                                                           */
    /***********************************************************************************************
     * Construct a PropertyData from an XMLBean.
     *
     * @param host
     * @param property
     * @param language
     */

    public PropertyData(final AtomPlugin host,
                        final PropertyResource property,
                        final String language)
        {
        super(VERSION_ID, host, property, language);

        // These setters must not use the XMLBean data!
        setUpdateAllowed(false);
        setUpdated(false);
        setInstalled(false);
        setInstantiated(false);
        setClassFound(false);
        setResourceKey(EMPTY_STRING);
        setName(EMPTY_STRING);
        setIconFilename(EMPTY_STRING);

        // Check that we know enough to try to import this Property
        if ((property != null)
            && (XmlBeansUtilities.isValidXml(property)))
            {
            final Object objProperty = BEAN_FACTORY_XML.validateResourceDataType(getDataType(),
                                                                            (getXml()).getValue());
            if (objProperty != null)
                {
                final StringBuffer key;

                key = new StringBuffer();

                // Initialise the ResourceKey with the host's ResourceKey
                // which always ends with DELIMITER_PATH
                key.append(host.getResourceKey());
                key.append(createResourceKey(property.getResourceKey().getKeyList()));
                setResourceKey(key.toString());

                setResource(objProperty);
                setName(getResourceKey());
                setInstalled(true);
                setInstantiated(true);
                setClassFound(true);
                setIconFilename(PROPERTY_ICON);
                setUpdateAllowed(true);
                setUpdated(false);
                }
            else
                {
                LOGGER.error("Unable to instantiate a " + getDataType());
                }
            }
        }


    /***********************************************************************************************
     * Get the ResourceKey List.
     *
     * @return String
     */

    public final List<String> getResourceKeys()
        {
        return (getXml().getResourceKey().getKeyList());
        }


    /***********************************************************************************************
     * Set the ResourceKey List.
     *
     * @param keys
     */

    public final void setResourceKeys(final List<String> keys)
        {
        final PropertyResource.ResourceKey resourceKey;

        resourceKey = PropertyResource.ResourceKey.Factory.newInstance();
        resourceKey.setKeyArray((String[])keys.toArray());
        getXml().setResourceKey(resourceKey);
        updateRoot();
        }


    /***********************************************************************************************
     * Get the Language ISO code.
     *
     * @return String
     */

    public final String getISOLanguageCode()
        {
        return (LanguagePlugin.DEFAULT_LANGUAGE);
        }


    /***********************************************************************************************
     * Set the Language ISO code.
     *
     * @param code
     */

    public final void setISOLanguageCode(final String code)
        {
        }


    /***********************************************************************************************
     * Get the DataTypeClassName field
     *
     * @return String
     */

    public final String getDataType()
        {
        return ((getXml()).getDataTypeClassName());
        }


    /***********************************************************************************************
     * Set the DataTypeClassName field
     *
     * @param classname
     */

    public final void setDataType(final String classname)
        {
        (getXml()).setDataTypeClassName(classname);
        updateRoot();
        }


     /***********************************************************************************************
      * Get the class name of the ResourceEditor.
      *
      * @return String
      */

     public final String getEditorClassname()
         {
         return ((getXml()).getEditorClassname());
         }


     /**********************************************************************************************
      * Set the class name of the ResourceEditor.
      *
      * @param classname
      */

     public final void setEditorClassname(final String classname)
         {
         (getXml()).setEditorClassname(classname);
         updateRoot();
         }


    /***********************************************************************************************
     * Get the Editable field.
     *
     * @return boolean
     */

    public final boolean isEditable()
        {
        return ((getXml()).getEditable());
        }


    /***********************************************************************************************
     * Set the Editable field.
     *
     * @param editable
     */

    public final void setEditable(final boolean editable)
        {
        (getXml()).setEditable(editable);
        updateRoot();
        }


    /***********************************************************************************************
     * Get the Description field
     *
     * @return String
     */

    public String getDescription()
        {
        return ((getXml()).getDescription());
        }


    /***********************************************************************************************
     * Set the Description field
     *
     * @param description
     */

    public final void setDescription(final String description)
        {
        (getXml()).setDescription(description);
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


    /***********************************************************************************************
     * Get the Instantiated field.
     * True indicates that the Property has been instantiated, and so the Value is valid.
     * False indicates that the Value will be null.
     *
     * @return boolean
     */

    public final boolean getInstantiated()
        {
        return (this.boolInstantiated);
        }


    /***********************************************************************************************
     * Set the Instantiated field.
     * True indicates that the Property has been instantiated, and so the Value is valid.
     * False indicates that the Value will be null.
     *
     * @param instantiated
     */

    public void setInstantiated(final boolean instantiated)
        {
        this.boolInstantiated = instantiated;
        }


    /***********************************************************************************************
     * Fully debug the Property.
     */

    public final void showDebugData()
        {
        if (getDebugMode())
            {
            LOGGER.log("Property Miscellaneous");
            LOGGER.log(INDENT + "[Host=" + getHostAtom().getName() + "]");
            LOGGER.log(INDENT + "[ID=" + getID() + "]");
            LOGGER.log(INDENT + "[Level=" + getLevel() + "]");
            LOGGER.log(INDENT + "[Pathname=" + getPathname() + "]");
            LOGGER.log(INDENT + "[ResourceKey=" + getResourceKey() + "]");
            LOGGER.log(INDENT + "[Name=" + getName() + "]");
            LOGGER.log(INDENT + "[IconFilename=" + getIconFilename() + "]");
            LOGGER.log(INDENT + "[ISOLanguageCode=" + getISOLanguageCode() + "]");
            LOGGER.log(INDENT + "[Installed=" + isInstalled() + "]");
            LOGGER.log(INDENT + "[Updated=" + isUpdated() + "]");
            LOGGER.log(INDENT + "[UpdatesAllowed=" + isUpdateAllowed() + "]");
            LOGGER.log(INDENT + "[DebugMode=" + getDebugMode() + "]");

            LOGGER.log("Property XML Bean");
            LOGGER.log(INDENT + "[ResourceKeys]");
            final Iterator iterKeys = getResourceKeys().iterator();

            while (iterKeys.hasNext())
                {
                LOGGER.log(INDENT + INDENT + "[Key=" + iterKeys.next() + "]");
                }

            LOGGER.log(INDENT + "[Editable=" + isEditable() + "]");
            LOGGER.log(INDENT + "[DataType=" + getDataType() + "]");
            LOGGER.log(INDENT + "[EditorClassname=" + getEditorClassname() + "]");
            LOGGER.log(INDENT + "[Value=" + getResource() + "]");
            LOGGER.log(INDENT + "[Description=" + getDescription() + "]");

            LOGGER.log("Property Dates and Times");
            LOGGER.log(INDENT + "[CreatedDate=" + getCreatedDate() + "]");
            LOGGER.log(INDENT + "[CreatedTime=" + getCreatedTime() + "]");
            LOGGER.log(INDENT + "[ModifiedDate=" + getModifiedDate() + "]");
            LOGGER.log(INDENT + "[ModifiedTime=" + getModifiedTime() + "]");
            }
        }


    /***********************************************************************************************
     * Get the XML part of the Property.
     *
     * @return XmlObject
     */

     public final PropertyResource getXml()
         {
         if (super.getXml() == null)
             {
             throw new FrameworkException(EXCEPTION_PARAMETER_NULL);
             }

         return ((PropertyResource)super.getXml());
         }
    }


//--------------------------------------------------------------------------------------------------
// End of File
