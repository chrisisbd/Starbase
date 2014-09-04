//--------------------------------------------------------------------------------------------------
// Revision History
//
//  17-09-03    LMN created file
//  18-09-03    LMN completed method coding
//  19-09-03    LMN converted to ResourceData superclass
//  23-09-03    LMN added ApplicationExceptions and ComponentExceptions
//  29-09-03    LMN tidying up...
//  26-10-04    LMN replacing Description etc.
//  08-05-06    LMN rewriting for XmlBeans...
//
//--------------------------------------------------------------------------------------------------

package org.lmn.fc.model.resources.impl;

import org.lmn.fc.common.exceptions.FrameworkException;
import org.lmn.fc.common.xml.XmlBeansUtilities;
import org.lmn.fc.model.plugins.AtomPlugin;
import org.lmn.fc.model.resources.ExceptionPlugin;
import org.lmn.fc.model.xmlbeans.exceptions.ExceptionResource;

import java.util.List;
import java.util.Iterator;
import java.util.GregorianCalendar;


/***************************************************************************************************
 * ExceptionData to associate with RegistryModel TreeNode.
 */

public final class ExceptionData extends ResourceData
                                 implements ExceptionPlugin
    {
    private static final long VERSION_ID = 7800223742130784178L;

    /***********************************************************************************************
     * Construct a ExceptionData from an Exception XMLBean.
     *
     * @param host
     * @param exception
     * @param language
     */

    public ExceptionData(final AtomPlugin host,
                         final ExceptionResource exception,
                         final String language)
        {
        super(VERSION_ID, host, exception, language);

        // These setters must not use the XMLBean data!
        setUpdateAllowed(false);
        setUpdated(false);
        setInstalled(false);
        setClassFound(false);
        setResourceKey(EMPTY_STRING);
        setName(EMPTY_STRING);
        setIconFilename(EMPTY_STRING);

        // Check that we know enough to try to import this Exception
        if ((exception != null)
            && (XmlBeansUtilities.isValidXml(exception)))
            {
            final Object objException = BEAN_FACTORY_XML.validateResourceDataType(EXCEPTION_CLASSNAME,
                                                                             getXml().getText());
            if (objException != null)
                {
                final StringBuffer key;

                key = new StringBuffer();

                // Initialise the ResourceKey with the host's ResourceKey
                // which always ends with DELIMITER_PATH
                key.append(host.getResourceKey());
                key.append(createResourceKey(exception.getResourceKey().getKeyList()));
                setResourceKey(key.toString());

                setResource(objException);
                setName(getResourceKey());
                setInstalled(true);
                setClassFound(true);
                setIconFilename(EXCEPTION_ICON);
                setUpdateAllowed(true);
                setUpdated(false);
                }
            else
                {
                LOGGER.error("Unable to instantiate a " + EXCEPTION_CLASSNAME);
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
        final ExceptionResource.ResourceKey resourceKey;

        resourceKey = ExceptionResource.ResourceKey.Factory.newInstance();
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
        return (getXml().getLanguageCode());
        }


    /***********************************************************************************************
     * Set the Language ISO code.
     *
     * @param code
     */

    public final void setISOLanguageCode(final String code)
        {
        getXml().setLanguageCode(code);
        updateRoot();
        }


    /***********************************************************************************************
     * Get the DataType ClassName.
     *
     * @return String
     */

    public final String getDataType()
        {
        return (EXCEPTION_CLASSNAME);
        }


    /***********************************************************************************************
     * Set the DataType ClassName.
     *
     * @param classname
     */

    public final void setDataType(final String classname)
        {
        }



    /**********************************************************************************************
      * Get the class name of the ResourceEditor.
      *
      * @return String
      */

     public final String getEditorClassname()
         {
         return (getXml().getEditorClassname());
         }


     /**********************************************************************************************
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
     * Get the Description field
     *
     * @return String
     */

    public String getDescription()
        {
        return (getXml().getDescription());
        }


    /***********************************************************************************************
     * Set the Description field
     *
     * @param description
     */

    public final void setDescription(final String description)
        {
        getXml().setDescription(description);
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
     * Fully debug the ExceptionData.
     */

    public void showDebugData()
        {
        if (getDebugMode())
            {
            LOGGER.log("Exception Miscellaneous");
            LOGGER.log(INDENT + "[Host=" + getHostAtom().getName() + "]");
            LOGGER.log(INDENT + "[ID=" + getID() + "]");
            LOGGER.log(INDENT + "[Level=" + getLevel() + "]");
            LOGGER.log(INDENT + "[Pathname=" + getPathname() + "]");
            LOGGER.log(INDENT + "[ResourceKey=" + getResourceKey() + "]");
            LOGGER.log(INDENT + "[Name=" + getName() + "]");
            LOGGER.log(INDENT + "[IconFilename=" + getIconFilename() + "]");
            LOGGER.log(INDENT + "[Installed=" + isInstalled() + "]");
            LOGGER.log(INDENT + "[Updated=" + isUpdated() + "]");
            LOGGER.log(INDENT + "[UpdatesAllowed=" + isUpdateAllowed() + "]");
            LOGGER.log(INDENT + "[DebugMode=" + getDebugMode() + "]");

            LOGGER.log("Exception XML Bean");
            LOGGER.log(INDENT + "[ResourceKeys]");
            final Iterator iterKeys = getResourceKeys().iterator();

            while (iterKeys.hasNext())
                {
                LOGGER.log(INDENT + INDENT + "[Key=" + iterKeys.next() + "]");
                }

            LOGGER.log(INDENT + "[ISOLanguageCode=" + getISOLanguageCode() + "]");
            LOGGER.log(INDENT + "[Editable=" + isEditable() + "]");
            LOGGER.log(INDENT + "[DataType=" + getDataType() + "]");
            LOGGER.log(INDENT + "[EditorClassname=" + getEditorClassname() + "]");
            LOGGER.log(INDENT + "[Text=" + getResource() + "]");
            LOGGER.log(INDENT + "[Description=" + getDescription() + "]");

            LOGGER.log("Exception Dates and Times");
            LOGGER.log(INDENT + "[CreatedDate=" + getCreatedDate() + "]");
            LOGGER.log(INDENT + "[CreatedTime=" + getCreatedTime() + "]");
            LOGGER.log(INDENT + "[ModifiedDate=" + getModifiedDate() + "]");
            LOGGER.log(INDENT + "[ModifiedTime=" + getModifiedTime() + "]");
            }
        }


    /***********************************************************************************************
     * Get the XML part of the Exception.
     *
     * @return XmlObject
     */

     public final ExceptionResource getXml()
         {
         if (super.getXml() == null)
             {
             throw new FrameworkException(EXCEPTION_PARAMETER_NULL);
             }

         return ((ExceptionResource)super.getXml());
         }
    }


//--------------------------------------------------------------------------------------------------
// End of File
