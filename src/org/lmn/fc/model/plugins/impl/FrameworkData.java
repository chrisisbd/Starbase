//--------------------------------------------------------------------------------------------------
// Revision History
//
//  18-01-02    LMN started again...
//  22-04-02    LMN added ObservatoryModel
//  26-04-02    LMN changed ExceptionLibrary and logging
//  03-07-02    LMN simplified the Topos constructor
//  05-05-03    LMN implemented ApplicationLoadAtStart & MethodRunAtStart
//  06-05-03    LMN achieved auto-load of UserInterface and FrameworkManager!
//  04-06-03    LMN added new Application/Component Iterators for Task startup
//  05-06-03    LMN added application.properties file for initialisation properties
//  13-06-03    LMN added startupAllTasks()
//  20-06-03    LMN finally removed redundant code in main()
//  31-08-03    LMN added Language selection
//  04-09-03    LMN added QueryLoader property
//  07-10-03    LMN added startupFramework()...
//  08-10-03    LMN renamed as FrameworkData, removed some bits to FrameworkLoader
//  13-10-03    LMN added shutdownFrameworkTasks()
//  10-11-03    LMN extended AtomData
//  16-02-04    LMN set Framework resource key to null
//  26-05-04    LMN added aboutFramework()
//  29-10-04    LMN slowly changing for a database table version...
//  08-11-04    LMN trying to get table version going...
//  09-11-04    LMN succeeded! The whole Framework is now data-driven...
//  12-11-04    LMN added FrameworkExportsFolder
//  15-11-04    LMN added script creators
//  25-11-04    LMN added UserRoles
//  03-01-05    LMN placed Node backup on a separate SwingWorker thread
//  05-01-06    LMN making major changes for XML Beans...
//  12-01-06    LMN added JMX control!
//  29-05-06    LMN removed StatusIndicator instance variables
//
//--------------------------------------------------------------------------------------------------

package org.lmn.fc.model.plugins.impl;

//--------------------------------------------------------------------------------------------------
// Imports

import org.lmn.fc.common.actions.ContextAction;
import org.lmn.fc.common.actions.ContextActionGroup;
import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkRegex;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.common.exceptions.FrameworkException;
import org.lmn.fc.common.loaders.LoaderProperties;
import org.lmn.fc.common.metadata.CommonMetadataHelper;
import org.lmn.fc.common.metadata.FrameworkKeys;
import org.lmn.fc.common.os.OperatingSystem;
import org.lmn.fc.common.support.jmx.HttpMBeanAdaptor;
import org.lmn.fc.common.utilities.threads.SwingWorker;
import org.lmn.fc.frameworks.starbase.events.FrameworkChangedEvent;
import org.lmn.fc.frameworks.starbase.events.FrameworkChangedListener;
import org.lmn.fc.frameworks.starbase.tasks.UserInterface;
import org.lmn.fc.model.datatypes.*;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.plugins.AtomPlugin;
import org.lmn.fc.model.plugins.FrameworkPlugin;
import org.lmn.fc.model.registry.RegistryModelPlugin;
import org.lmn.fc.model.registry.RegistryModelUtilities;
import org.lmn.fc.model.root.ActionGroup;
import org.lmn.fc.model.root.RootPlugin;
import org.lmn.fc.model.root.RootType;
import org.lmn.fc.model.xmlbeans.frameworks.Framework;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;
import org.lmn.fc.model.xmlbeans.metadata.SchemaUnits;
import org.lmn.fc.model.xmlbeans.poi.LineOfInterest;
import org.lmn.fc.model.xmlbeans.poi.PointOfInterest;
import org.lmn.fc.model.xmlbeans.roles.RoleName;
import org.lmn.fc.ui.UIComponentPlugin;
import org.lmn.fc.ui.panels.PDFPanel;

import javax.management.*;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;


/***************************************************************************************************
 * FrameworkData object to associate with the RegistryModel.
 * There will only ever be one of these!
 * The Framework is a PointOfInterest in order to show its location on a map.
 * WARNING! This class *cannot* extend PluginData, because XmlBeans do not support inheritance
 * in a useful way... This means a lot of repetition of methods, unfortunately  :-(
 */

public abstract class FrameworkData extends AtomData
                                    implements FrameworkPlugin,
                                               FrameworkConstants,
                                               FrameworkStrings,
                                               FrameworkMetadata,
                                               FrameworkRegex,
                                               FrameworkKeys,
                                               Serializable
    {
    // String Resources
    public static final String TAB_SYSTEM_PROPERTIES = "SystemProperties";
    public static final String TAB_ACKNOWLEDGEMENTS = "Acknowledgements";
    public static final String TAB_LICENCE = "Licence";

    private static final String MSG_LOGOUT = ACTION_LOGOUT;
    private static final String MSG_CONFIRM_LOGOUT_0 = "Are you sure that you wish to Log out?";
    private static final String MSG_CONFIRM_LOGOUT_1 = "The Framework, Plugins and Tasks will be stopped, data will be discarded";
    private static final String MSG_CONFIRM_SHUTDOWN_0 = "Are you sure that you want to shut down the application?";
    private static final String MSG_CONFIRM_SHUTDOWN_1 = "The Framework, Plugins and Tasks will be stopped, data will be discarded";
    private static final String EXCEPTION_UNABLE_TO_LOGOUT = "Unable to Log Out";
    private static final String DEFAULT_DESCRIPTION_TIMEZONE = "The TimeZone containing the Framework";
    private static final String DEFAULT_DESCRIPTION_HASL = "The Height of the Framework above Sea Level in metres";
    private static final String DEFAULT_DESCRIPTION_LATITUDE = "The Latitude of the Framework (North is positive)";
    private static final String DEFAULT_DESCRIPTION_LONGITUDE = "The Longitude of the Framework (West is positive)";

    // The Framework MBean Server instance
    private MBeanServer serverMBean;

    // The HTTP Adaptor for the Framework MBeans JMX Interface
    private HttpMBeanAdaptor adaptorHTTP;

    // The Thread used for the Backup ContextAction
    private SwingWorker workerBackup;

    // The current platform
    private OperatingSystem operatingSystem;

    // Framework Metadata
    private final List<Metadata> listFrameworkMetadata;

    // Framework POIs and LOIs
    private List<PointOfInterest> listFrameworkPOIs;
    private List<LineOfInterest> listFrameworkLOIs;

    private final Vector<FrameworkChangedListener> vecFrameworkChangedListeners;


    /***********************************************************************************************
     * Pseudo-Singleton Constructor.
     *
     * @param id
     */

    protected FrameworkData(final long id)
        {
        super(id, true);

        this.setType(RootType.ATOM);
        this.setParentAtom(null);
        this.serverMBean = null;
        this.adaptorHTTP = null;

        // Metadata
        this.listFrameworkMetadata = new ArrayList<Metadata>(100);

        // POIs and LOIs
        this.listFrameworkPOIs = new ArrayList<PointOfInterest>(10);
        this.listFrameworkLOIs = new ArrayList<LineOfInterest>(10);

        this.vecFrameworkChangedListeners = new Vector<FrameworkChangedListener>(10);
        }


    /***********************************************************************************************
     * Get the MBean server ObjectName for the Atom.
     * The syntax is <code>framework:Name=framework,Level=levelname</code>.
     *
     * @return ObjectName
     */

    public ObjectName getObjectName()
        {
        final ObjectName objectName;

        try
            {
            objectName = new ObjectName(getName()
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
     * Get the current OperatingSystem.
     *
     * @return OperatingSystem
     */

    public OperatingSystem getOperatingSystem()
        {
        return (this.operatingSystem);
        }


    /***********************************************************************************************
     * Set the current OperatingSystem (usually done by the FrameworkLoader).
     *
     * @param os
     */

    public void setOperatingSystem(final OperatingSystem os)
        {
        this.operatingSystem = os;
        }


    /***********************************************************************************************
     * Create the Framework's AboutBox.
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
     * Get the tooltip to be used at the Framework's PointOfInterest.
     *
     * @return String
     */

    public final String getTooltip()
        {
        return (getName());
        }


    /***********************************************************************************************
     * Read all Resources required by the Framework, which was set when the Framework was loaded.
     */

    public void readResources()
        {
        // Use the RegistryModel's debug mode
        setDebugMode(REGISTRY_MODEL.getDebugMode());
        }


    /***********************************************************************************************
     * Get the ResourceKey for the Framework.
     *
     * @return String
     */

    public final String getResourceKey()
        {
        return (getName() + RegistryModelPlugin.DELIMITER_RESOURCE);
        }


    /***********************************************************************************************
     * Get the full RegistryModel pathname for the Framework.
     *
     * @return String
     */

    public final String getPathname()
        {
        return (getName());
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

    // todo I'm not sure if this is necessary, but it seemed like a good idea...
//    System.setProperty("user.timezone", timezoneFramework.getID());


    /**********************************************************************************************/
    /* Methods to access the XML part of the Framework.                                           */
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
     * Get the List of RecursionLevels.
     *
     * @return List<String>
     */

    public final List<String> getRecursionLevels()
        {
        return (getXml().getRecursionLevelList());
        }


    /***********************************************************************************************
     * Set the array of RecursionLevels.
     *
     * @param levels
     */

    public final void setRecursionLevels(final String[] levels)
        {
        // This is a bit odd, should be deprecated, but there is no setRecursionLevelList()
        getXml().setRecursionLevelArray(levels);
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
     * Get the FrameworkLoadAtStart field.
     *
     * @return boolean
     */

    public final boolean isLoadAtStart()
        {
        return (getXml().getLoadAtStart());
        }


    /***********************************************************************************************
     * Set the FrameworkLoadAtStart field.
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
     * Get the Framework Language ISO code.
     *
     * @return String
     */

    public final String getLanguageISOCode()
        {
        return (getXml().getLanguage());
        }


    /***********************************************************************************************
     * Set the Framework Language ISO code.
     *
     * @param code
     */

    public final void setLanguageISOCode(final String code)
        {
        getXml().setLanguage(code);
        updateRoot();
        }


    /***********************************************************************************************
     * Get the Framework Country ISO code.
     *
     * @return String
     */

    public final String getCountryISOCode()
        {
        return (getXml().getCountry());
        }


    /***********************************************************************************************
     * Set the Framework Country ISO code.
     *
     * @param code
     */

    public final void setCountryISOCode(final String code)
        {
        getXml().setCountry(code);
        updateRoot();
        }


    /**********************************************************************************************/
    /* The below have been incorporated into Framework Metadata                                   */
    /***********************************************************************************************
     * Get the Framework TimeZone code.
     * Return GMT+00:00 if not valid.
     *
     * @return String
     */

    public final String getTimeZoneCode()
        {
        Metadata metadataTZ;

        // We must not have any dependencies on a sub-plugin,
        // so have another MetadataHelper in the Framework
        metadataTZ = CommonMetadataHelper.getMetadataByKey(getFrameworkMetadata(), KEY_FRAMEWORK_TIMEZONE);

        if (metadataTZ == null)
            {
            // Make a known good TimeZone Metadata item
            metadataTZ = CommonMetadataHelper.addMetadata(getFrameworkMetadata(),
                                                          KEY_FRAMEWORK_TIMEZONE,
                                                          REGISTRY.getGMTTimeZone().getDisplayName(),
                                                          DataTypeDictionary.TIME_ZONE.getRegex(),
                                                          DataTypeDictionary.TIME_ZONE,
                                                          SchemaUnits.DIMENSIONLESS,
                                                          DEFAULT_DESCRIPTION_TIMEZONE);
            }

        return (metadataTZ.getValue());
        }


    /***********************************************************************************************
     * Set the Framework TimeZone code.
     *
     * @param code
     */

    public final void setTimeZoneCode(final String code)
        {
        final String SOURCE = "FrameworkData.setTimeZoneCode() ";
        final List<String> errors;
        Metadata metadataTZ;

        errors = new ArrayList<String>(10);

        // We must not have any dependencies on a sub-plugin, so have another MetadataHelper
        metadataTZ = CommonMetadataHelper.getMetadataByKey(getFrameworkMetadata(), KEY_FRAMEWORK_TIMEZONE);

        if (metadataTZ == null)
            {
            // Make a known good TimeZone Metadata item
            metadataTZ = CommonMetadataHelper.addMetadata(getFrameworkMetadata(),
                                                          KEY_FRAMEWORK_TIMEZONE,
                                                          REGISTRY.getGMTTimeZone().getDisplayName(),
                                                          DataTypeDictionary.TIME_ZONE.getRegex(),
                                                          DataTypeDictionary.TIME_ZONE,
                                                          SchemaUnits.DIMENSIONLESS,
                                                          DEFAULT_DESCRIPTION_TIMEZONE);
            }

        // Update with the specified Value only if it is valid
        // If the specified Metadata contains Regex,
        // then this takes precedence over any Regex in the DataType
        if (CommonMetadataHelper.validateDataTypeOfMetadataValue(code,
                                                                 DataTypeDictionary.TIME_ZONE,
                                                                 DataTypeDictionary.TIME_ZONE.getRegex(),
                                                                 errors) == 0)
            {
            metadataTZ.setValue(code);
            updateRoot();
            notifyFrameworkChangedEvent(this);
            }
        else
            {
            LOGGER.errors(SOURCE, errors);
            }
        }


    /***********************************************************************************************
     * Longitude is POSITIVE to the WEST.
     *
     * @return DegMinSec
     */

    public final DegMinSecInterface getLongitude()
        {
        Metadata metadataLongitude;
        final DegMinSecInterface dmsLongitude;
        final List<String> errors;

        errors = new ArrayList<String>(10);

        // We must not have any dependencies on a sub-plugin, so have another MetadataHelper
        metadataLongitude = CommonMetadataHelper.getMetadataByKey(getFrameworkMetadata(), KEY_FRAMEWORK_LONGITUDE);

        if (metadataLongitude == null)
            {
            // Make a known good Longitude Metadata item
            metadataLongitude = CommonMetadataHelper.addMetadata(getFrameworkMetadata(),
                                                                 KEY_FRAMEWORK_LONGITUDE,
                                                                 "+000:00:00.0000",
                                                                 REGEX_LONGITUDE_DMS_SIGNED,
                                                                 DataTypeDictionary.SIGNED_LONGITUDE,
                                                                 SchemaUnits.DEG_MIN_SEC,
                                                                 DEFAULT_DESCRIPTION_LONGITUDE);
            }

        // We've either retrieved the original Metadata, or made a new one
        dmsLongitude = (DegMinSecInterface) DataTypeHelper.parseDataTypeFromValueField(metadataLongitude.getValue(),
                                                                                       DataTypeDictionary.SIGNED_LONGITUDE,
                                                                                       EMPTY_STRING,
                                                                                       EMPTY_STRING,
                                                                                       errors);
        dmsLongitude.setDisplayFormat(DegMinSecFormat.EW);
        dmsLongitude.apply360DegreeSecondsPattern(DecimalFormatPattern.SECONDS_LONGITUDE);

        return (dmsLongitude);
        }


    /***********************************************************************************************
     * Longitude is POSITIVE to the WEST.
     *
     * @param longitude
     */

    public final void setLongitude(final DegMinSecInterface longitude)
        {
        final String SOURCE = "FrameworkData.setLongitude() ";

        if (longitude != null)
            {
            final List<String> errors;
            final DegMinSecInterface dmsLongitude;
            Metadata metadataLongitude;

            errors = new ArrayList<String>(10);

            dmsLongitude = longitude;

            // Ensure that we always store the DegMinSec in the correct format
            // to be parsed back when reloaded
            dmsLongitude.setDisplayFormat(DegMinSecFormat.SIGN);
            dmsLongitude.apply360DegreeSecondsPattern(DecimalFormatPattern.SECONDS_LONGITUDE);

            // We must not have any dependencies on a sub-plugin, so have another MetadataHelper
            metadataLongitude = CommonMetadataHelper.getMetadataByKey(getFrameworkMetadata(), KEY_FRAMEWORK_LONGITUDE);

            if (metadataLongitude == null)
                {
                // Make a known good Longitude Metadata item
                metadataLongitude = CommonMetadataHelper.addMetadata(getFrameworkMetadata(),
                                                                     KEY_FRAMEWORK_LONGITUDE,
                                                                     "+000:00:00.0000",
                                                                     REGEX_LONGITUDE_DMS_SIGNED,
                                                                     DataTypeDictionary.SIGNED_LONGITUDE,
                                                                     SchemaUnits.DEG_MIN_SEC,
                                                                     DEFAULT_DESCRIPTION_LONGITUDE);
                }

            // We've either retrieved the original Metadata, or made a new one
            // Update with the specified Value only if it is valid
            // If the specified Metadata contains Regex,
            // then this takes precedence over any Regex in the DataType
            if (CommonMetadataHelper.validateDataTypeOfMetadataValue(dmsLongitude.toString(),
                                                                     DataTypeDictionary.SIGNED_LONGITUDE,
                                                                     REGEX_LONGITUDE_DMS_SIGNED,
                                                                     errors) == 0)
                {
                metadataLongitude.setValue(dmsLongitude.toString());
                updateRoot();
                notifyFrameworkChangedEvent(this);
                }
            else
                {
                LOGGER.errors(SOURCE, errors);
                }
            }
        }


    /***********************************************************************************************
     * Latitude is POSITIVE to the NORTH.
     *
     * @return DegMinSec
     */

    public final DegMinSecInterface getLatitude()
        {
        Metadata metadataLatitude;
        final DegMinSecInterface dmsLatitude;
        final List<String> errors;

        errors = new ArrayList<String>(10);

        // We must not have any dependencies on a sub-plugin, so have another MetadataHelper
        metadataLatitude = CommonMetadataHelper.getMetadataByKey(getFrameworkMetadata(), KEY_FRAMEWORK_LATITUDE);

        if (metadataLatitude == null)
            {
            // Make a known good Latitude Metadata item
            metadataLatitude = CommonMetadataHelper.addMetadata(getFrameworkMetadata(),
                                                                 KEY_FRAMEWORK_LATITUDE,
                                                                 "+00:00:00.0000",
                                                                 REGEX_LATITUDE_DMS_SIGNED,
                                                                 DataTypeDictionary.LATITUDE,
                                                                 SchemaUnits.DEG_MIN_SEC,
                                                                 DEFAULT_DESCRIPTION_LATITUDE);
            }

        // We've either retrieved the original Metadata, or made a new one
        dmsLatitude = (DegMinSecInterface) DataTypeHelper.parseDataTypeFromValueField(metadataLatitude.getValue(),
                                                                                      DataTypeDictionary.LATITUDE,
                                                                                      EMPTY_STRING,
                                                                                      EMPTY_STRING,
                                                                                      errors);
        dmsLatitude.setDisplayFormat(DegMinSecFormat.NS);
        dmsLatitude.apply90DegreeSecondsPattern(DecimalFormatPattern.SECONDS_LATITUDE);

        return (dmsLatitude);
        }


    /***********************************************************************************************
     * Latitude is POSITIVE to the NORTH.
     *
     * @param latitude
     */

    public final void setLatitude(final DegMinSecInterface latitude)
        {
        final String SOURCE = "FrameworkData.setLatitude() ";

        if (latitude != null)
            {
            final List<String> errors;
            final DegMinSecInterface dmsLatitude;
            Metadata metadataLatitude;

            errors = new ArrayList<String>(10);

            dmsLatitude = latitude;

            // Ensure that we always store the DegMinSec in the correct format
            // to be parsed back when reloaded
            dmsLatitude.setDisplayFormat(DegMinSecFormat.SIGN);
            dmsLatitude.apply90DegreeSecondsPattern(DecimalFormatPattern.SECONDS_LATITUDE);

            // We must not have any dependencies on a sub-plugin, so have another MetadataHelper
            metadataLatitude = CommonMetadataHelper.getMetadataByKey(getFrameworkMetadata(), KEY_FRAMEWORK_LATITUDE);

            if (metadataLatitude == null)
                {
                // Make a known good Longitude Metadata item
                metadataLatitude = CommonMetadataHelper.addMetadata(getFrameworkMetadata(),
                                                                    KEY_FRAMEWORK_LATITUDE,
                                                                    "+00:00:00.0000",
                                                                    REGEX_LATITUDE_DMS_SIGNED,
                                                                    DataTypeDictionary.LATITUDE,
                                                                    SchemaUnits.DEG_MIN_SEC,
                                                                    DEFAULT_DESCRIPTION_LATITUDE);
                }

            // We've either retrieved the original Metadata, or made a new one
            // Update with the specified Value only if it is valid
            // If the specified Metadata contains Regex,
            // then this takes precedence over any Regex in the DataType
            if (CommonMetadataHelper.validateDataTypeOfMetadataValue(dmsLatitude.toString(),
                                                                     DataTypeDictionary.LATITUDE,
                                                                     REGEX_LATITUDE_DMS_SIGNED,
                                                                     errors) == 0)
                {
                metadataLatitude.setValue(dmsLatitude.toString());
                updateRoot();
                notifyFrameworkChangedEvent(this);
                }
            else
                {
                LOGGER.errors(SOURCE, errors);
                }
            }
        }


    /***********************************************************************************************
     * Get the Height Above Sea Level in metres.
     * Return 0.0 if not valid.
     *
     * @return double
     */

    public double getHASL()
        {
        Metadata metadataHASL;

        // We must not have any dependencies on a sub-plugin, so have another MetadataHelper
        metadataHASL = CommonMetadataHelper.getMetadataByKey(getFrameworkMetadata(), KEY_FRAMEWORK_HASL);

        if (metadataHASL == null)
            {
            // Make a known good HASL Metadata item
            metadataHASL = CommonMetadataHelper.addMetadata(getFrameworkMetadata(),
                                                            KEY_FRAMEWORK_HASL,
                                                            "0.0",
                                                            REGEX_HASL,
                                                            DataTypeDictionary.DECIMAL_FLOAT,
                                                            SchemaUnits.M,
                                                            DEFAULT_DESCRIPTION_HASL);
            }

        return (Double.parseDouble(metadataHASL.getValue()));
        }


    /***********************************************************************************************
     * Set the Height Above Sea Level in metres.
     *
     * @param hasl
     */

    public void setHASL(final double hasl)
        {
        final String SOURCE = "FrameworkData.setHASL() ";
        final List<String> errors;
        Metadata metadataHASL;

        errors = new ArrayList<String>(10);

        // We must not have any dependencies on a sub-plugin, so have another MetadataHelper
        metadataHASL = CommonMetadataHelper.getMetadataByKey(getFrameworkMetadata(), KEY_FRAMEWORK_HASL);

        if (metadataHASL == null)
            {
            // Make a known good HASL Metadata item
            metadataHASL = CommonMetadataHelper.addMetadata(getFrameworkMetadata(),
                                                            KEY_FRAMEWORK_HASL,
                                                            "0.0",
                                                            REGEX_HASL,
                                                            DataTypeDictionary.DECIMAL_FLOAT,
                                                            SchemaUnits.M,
                                                            DEFAULT_DESCRIPTION_HASL);
            }

        // Update with the specified Value only if it is valid
        if (CommonMetadataHelper.validateDataTypeOfMetadataValue(Double.toString(hasl),
                                                                 DataTypeDictionary.DECIMAL_FLOAT,
                                                                 REGEX_HASL,
                                                                 errors) == 0)
            {
            metadataHASL.setValue(Double.toString(hasl));
            updateRoot();
            notifyFrameworkChangedEvent(this);
            }
        else
            {
            LOGGER.errors(SOURCE, errors);
            }
        }


    /**********************************************************************************************/
    /* The above have been incorporated into Framework Metadata                                   */
    /***********************************************************************************************
     * Get the AutoUpdate field.
     *
     * @return boolean
     */

    public final boolean isAutoUpdate()
        {
        return getXml().getAutoUpdate();
        }


    /***********************************************************************************************
     * Set the AutoUpdate field.
     *
     * @param auto
     */

    public final void setAutoUpdate(final boolean auto)
        {
        getXml().setAutoUpdate(auto);
        updateRoot();
        }


    /***********************************************************************************************
     * Get the filename of the Framework LoginPanel.
     *
     * @return String
     */

    public final String getSplashScreenFilename()
        {
        return (getXml().getSplashScreenFilename());
        }


    /***********************************************************************************************
     * Set the filename of the Framework LoginPanel.
     *
     * @param filename
     */

    public final void setSplashScreenFilename(final String filename)
        {
        getXml().setSplashScreenFilename(filename);
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
     * Get the filename of the Framework AboutBox HTML file.
     *
     * @return String
     */

    public final String getAboutFilename()
        {
        return (getXml().getAboutFilename());
        }


    /***********************************************************************************************
     * Set the filename of the Framework AboutBox HTML file.
     *
     * @param filename
     */

    public final void setAboutFilename(final String filename)
        {
        getXml().setAboutFilename(filename);
        updateRoot();
        }


    /***********************************************************************************************
     * Get the filename of the Framework Licence HTML file.
     *
     * @return String
     */

    public final String getLicenceFilename()
        {
        return (getXml().getLicenceFilename());
        }


    /***********************************************************************************************
     * Set the filename of the Framework Licence HTML file.
     *
     * @param filename
     */

    public final void setLicenceFilename(final String filename)
        {
        getXml().setLicenceFilename(filename);
        updateRoot();
        }


    /**********************************************************************************************/
    /* Metadata                                                                                   */
    /***********************************************************************************************
     * Get the List of Metadata for the Framework.
     *
     * @return List<Metadata>
     */

    public final List<Metadata> getFrameworkMetadata()
        {
        return (this.listFrameworkMetadata);
        }


    /**********************************************************************************************/
    /* PointOfInterest                                                                            */
    /***********************************************************************************************
     * Add a PointOfInterest to the Framework.
     *
     * @param poi
     */

    public final void addPointOfInterest(final PointOfInterest poi)
        {
        if ((poi != null)
            && (getPointOfInterestList() != null))
            {
            getPointOfInterestList().add(poi);
            }
        }


    /***********************************************************************************************
     * Remove all PointsOfInterest from the Framework.
     */

    public void clearPointsOfInterest()
        {
        if (getPointOfInterestList() != null)
            {
            getPointOfInterestList().clear();
            }
        }


    /***********************************************************************************************
     * Get the Points of Interest for the Framework.
     *
     * @return List<PointOfInterest>
     */

    public final List<PointOfInterest> getPointOfInterestList()
        {
        return (this.listFrameworkPOIs);
        }


    /***********************************************************************************************
     * Set the Points of Interest for the Framework.
     *
     * @param pois
     */

    public final void setPointOfInterestList(final List<PointOfInterest> pois)
        {
        this.listFrameworkPOIs = pois;
        }


    /**********************************************************************************************/
    /* LineOfInterest                                                                             */
    /***********************************************************************************************
     * Add a LineOfInterest to the Framework.
     *
     * @param loi
     */

    public final void addLineOfInterest(final LineOfInterest loi)
        {
        if ((loi != null)
            && (getLineOfInterestList() != null))
            {
            getLineOfInterestList().add(loi);
            }
        }


    /***********************************************************************************************
     * Remove all LinesOfInterest from the Framework.
     */

    public void clearLinesOfInterest()
        {
        if (getLineOfInterestList() != null)
            {
            getLineOfInterestList().clear();
            }
        }


    /***********************************************************************************************
     * Get the list of LinesOfInterest.
     *
     * @return List<LineOfInterest>
     */

    public List<LineOfInterest> getLineOfInterestList()
        {
        return (this.listFrameworkLOIs);
        }


    /***********************************************************************************************
     * Set the Lines of Interest for the Framework.
     *
     * @param lois
     */

    public final void setLineOfInterestList(final List<LineOfInterest> lois)
        {
        this.listFrameworkLOIs = lois;
        }


    /**********************************************************************************************/
    /* Mapping                                                                                    */
    /***********************************************************************************************
     *
     * @return String
     */

    public final String getMapFilename()
        {
        return (getXml().getMapFilename());
        }


    /***********************************************************************************************
     *
     * @param filename
     */

    public final void setMapFilename(final String filename)
        {
        getXml().setMapFilename(filename);
        updateRoot();
        }


    /***********************************************************************************************
     *
     * @return DegMinSec
     */

    public final DegMinSecInterface getMapTopLeftLongitude()
        {
        final DegMinSecInterface dmsLongitude;
        final List<String> errors;

        errors = new ArrayList<String>(10);

        dmsLongitude = (DegMinSecInterface) DataTypeHelper.parseDataTypeFromValueField(getXml().getMapTopLeftLongitude(),
                                                                                       DataTypeDictionary.SIGNED_LONGITUDE,
                                                                                       EMPTY_STRING,
                                                                                       EMPTY_STRING,
                                                                                       errors);
        dmsLongitude.apply360DegreeSecondsPattern(DecimalFormatPattern.SECONDS_LONGITUDE);
        dmsLongitude.setDisplayFormat(DegMinSecFormat.SIGN);

        //LOGGER.logTimedEvent("FRAMEWORK [top_left_long=" + dmsLongitude.toString() + "]");
        return (dmsLongitude);
        }


    /***********************************************************************************************
     *
     * @param longitude
     */

    public final void setMapTopLeftLongitude(final DegMinSecInterface longitude)
        {
        final DegMinSecInterface dmsTopLeftLongitude;

        dmsTopLeftLongitude = longitude;
        dmsTopLeftLongitude.apply360DegreeSecondsPattern(DecimalFormatPattern.SECONDS_LONGITUDE);
        dmsTopLeftLongitude.setDisplayFormat(DegMinSecFormat.SIGN);

        getXml().setMapTopLeftLongitude(dmsTopLeftLongitude.toString());
        updateRoot();
        }


    /***********************************************************************************************
     *
     * @return DegMinSec
     */

    public final DegMinSecInterface getMapTopLeftLatitude()
        {
        final DegMinSecInterface dmsLatitude;
        final List<String> errors;

        errors = new ArrayList<String>(10);

        dmsLatitude = (DegMinSecInterface) DataTypeHelper.parseDataTypeFromValueField(getXml().getMapTopLeftLatitude(),
                                                                                      DataTypeDictionary.LATITUDE,
                                                                                      EMPTY_STRING,
                                                                                      EMPTY_STRING,
                                                                                      errors);
        dmsLatitude.setDisplayFormat(DegMinSecFormat.SIGN);

        //LOGGER.logTimedEvent("FRAMEWORK [top_left_lat=" + dmsLatitude.toString() + "]");
        return (dmsLatitude);
        }


    /***********************************************************************************************
     *
     * @param latitude
     */

    public final void setMapTopLeftLatitude(final DegMinSecInterface latitude)
        {
        final DegMinSecInterface dmsTopLeftLatitude;

        dmsTopLeftLatitude = latitude;
        dmsTopLeftLatitude.apply90DegreeSecondsPattern(DecimalFormatPattern.SECONDS_LATITUDE);
        dmsTopLeftLatitude.setDisplayFormat(DegMinSecFormat.SIGN);

        getXml().setMapTopLeftLatitude(dmsTopLeftLatitude.toString());
        updateRoot();
        }


    /***********************************************************************************************
     *
     * @return DegMinSec
     */

    public final DegMinSecInterface getMapBottomRightLongitude()
        {
        final DegMinSecInterface dmsLongitude;
        final List<String> errors;

        errors = new ArrayList<String>(10);

        dmsLongitude = (DegMinSecInterface) DataTypeHelper.parseDataTypeFromValueField(getXml().getMapBottomRightLongitude(),
                                                                                       DataTypeDictionary.SIGNED_LONGITUDE,
                                                                                       EMPTY_STRING,
                                                                                       EMPTY_STRING,
                                                                                       errors);
        dmsLongitude.apply360DegreeSecondsPattern(DecimalFormatPattern.SECONDS_LONGITUDE);
        dmsLongitude.setDisplayFormat(DegMinSecFormat.SIGN);

        //LOGGER.logTimedEvent("FRAMEWORK [bottom_right_long=" + dmsLongitude.toString() + "]");
        return (dmsLongitude);
        }


    /***********************************************************************************************
     *
     * @param longitude
     */

    public final void setMapBottomRightLongitude(final DegMinSecInterface longitude)
        {
        final DegMinSecInterface dmsBottomRightLongitude;

        dmsBottomRightLongitude = longitude;
        dmsBottomRightLongitude.apply360DegreeSecondsPattern(DecimalFormatPattern.SECONDS_LONGITUDE);
        dmsBottomRightLongitude.setDisplayFormat(DegMinSecFormat.SIGN);

        getXml().setMapBottomRightLongitude(dmsBottomRightLongitude.toString());
        updateRoot();
        }


    /***********************************************************************************************
     *
     * @return DegMinSec
     */

    public final DegMinSecInterface getMapBottomRightLatitude()
        {
        final DegMinSecInterface dmsLatitude;
        final List<String> errors;

        errors = new ArrayList<String>(10);

        dmsLatitude = (DegMinSecInterface) DataTypeHelper.parseDataTypeFromValueField(getXml().getMapBottomRightLatitude(),
                                                                                      DataTypeDictionary.LATITUDE,
                                                                                      EMPTY_STRING,
                                                                                      EMPTY_STRING,
                                                                                      errors);
        //LOGGER.logTimedEvent("FRAMEWORK [bottom_right_lat=" + dmsLatitude.toString() + "]");
        return (dmsLatitude);
        }


    /***********************************************************************************************
     *
     * @param latitude
     */

    public final void setMapBottomRightLatitude(final DegMinSecInterface latitude)
        {
        final DegMinSecInterface dmsBottomRightLatitude;

        dmsBottomRightLatitude = latitude;
        dmsBottomRightLatitude.apply90DegreeSecondsPattern(DecimalFormatPattern.SECONDS_LATITUDE);
        dmsBottomRightLatitude.setDisplayFormat(DegMinSecFormat.SIGN);

        getXml().setMapBottomRightLatitude(dmsBottomRightLatitude.toString());
        updateRoot();
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
     * Set the StartDate for this Task.
     *
     * @param calendar
     */

    public final void setStartDate(final GregorianCalendar calendar)
        {
        getXml().setStartDate(calendar);
        updateRoot();
        }


    /***********************************************************************************************
     * Get the StartTime for this Task.
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
     * Set the StartTime for this Task.
     *
     * @param calendar
     */

    public final void setStartTime(final GregorianCalendar calendar)
        {
        getXml().setStartTime(calendar);
        updateRoot();
        }


    /***********************************************************************************************
     * Get the StopDate for this Task.
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
     * Set the StopDate for this Task.
     *
     * @param calendar
     */

    public final void setStopDate(final GregorianCalendar calendar)
        {
        getXml().setStopDate(calendar);
        updateRoot();
        }


    /***********************************************************************************************
     * Get the StopTime for this Task.
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
     * Set the StopTime for this Task.
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
    /* MBeans                                                                                     */
    /***********************************************************************************************
     * Initialise the MBeanServer and the HttpMBeanAdaptor.
     * This should only happen during login.
     */

    public final void initialiseMBeanServer()
        {
        final LoaderProperties properties;

        properties = LoaderProperties.getInstance();
        setMBeanServer(ManagementFactory.getPlatformMBeanServer());

        LOGGER.login(METADATA_MBEAN_SERVER_INITIALISE);

        setHttpAdaptor(new HttpMBeanAdaptor(properties.getJmxUsername(),
                                            properties.getJmxPassword(),
                                            properties.getJmxPort()));
        getHttpAdaptor().start();
        LOGGER.login(METADATA_HTTP_ADAPTOR_START);

        // Register the HTTP Adaptor server with the MBean server
        try
            {
            getMBeanServer().registerMBean(getHttpAdaptor().getServer(),
                                           new ObjectName(HttpMBeanAdaptor.IDENTIFIER_MBEAN_HTTP));
            LOGGER.login(METADATA_MBEAN_REGISTER
                          + SPACE
                          + METADATA_NAME
                          + HttpMBeanAdaptor.IDENTIFIER_MBEAN_HTTP
                          + TERMINATOR);
            }

        catch (InstanceAlreadyExistsException exception)
            {
            LOGGER.login(METADATA_MBEAN_REGISTER
                            + SPACE
                            + METADATA_EXCEPTION
                            + "InstanceAlreadyExistsException"
                            + TERMINATOR);
            }

        catch (MBeanRegistrationException exception)
            {
            LOGGER.login(METADATA_MBEAN_REGISTER
                            + SPACE
                            + METADATA_EXCEPTION
                            + "MBeanRegistrationException"
                            + TERMINATOR);
            }

        catch (NotCompliantMBeanException exception)
            {
            LOGGER.login(METADATA_MBEAN_REGISTER
                            + SPACE
                            + METADATA_EXCEPTION
                            + "NotCompliantMBeanException"
                            + TERMINATOR);
            }

        catch (MalformedObjectNameException exception)
            {
            LOGGER.login(METADATA_MBEAN_REGISTER
                            + SPACE
                            + METADATA_EXCEPTION
                            + "MalformedObjectNameException"
                            + TERMINATOR);
            }
        }


    /***********************************************************************************************
     * Register the specified AtomPlugin with the MBean server.
     * This should only happen during login.
     *
     * @param atom
     */

    public final void registerAtom(final AtomPlugin atom)
        {
        if ((atom != null)
            && (atom.getObjectName() != null)
            && (getMBeanServer() != null)
            && (!getMBeanServer().isRegistered(atom.getObjectName())))
            {
            try
                {
                getMBeanServer().registerMBean(atom, atom.getObjectName());
                LOGGER.login(METADATA_MBEAN_REGISTER
                              + SPACE
                              + METADATA_NAME
                              + atom.getPathname()
                              + TERMINATOR);
                }

            catch (InstanceAlreadyExistsException e)
                {
                LOGGER.login(METADATA_MBEAN_REGISTER
                              + SPACE
                              + METADATA_EXCEPTION
                              + "InstanceAlreadyExistsException"
                              + TERMINATOR
                              + SPACE
                              + METADATA_NAME
                              + atom.getPathname()
                              + TERMINATOR);
                }

            catch (MBeanRegistrationException e)
                {
                LOGGER.login(METADATA_MBEAN_REGISTER
                              + SPACE
                              + METADATA_EXCEPTION
                              + "MBeanRegistrationException"
                              + TERMINATOR
                              + SPACE
                              + METADATA_NAME
                              + atom.getPathname()
                              + TERMINATOR);
                }

            catch (NotCompliantMBeanException e)
                {
                LOGGER.login(METADATA_MBEAN_REGISTER
                              + SPACE
                              + METADATA_EXCEPTION
                              + "NotCompliantMBeanException"
                              + TERMINATOR
                              + SPACE
                              + METADATA_NAME
                              + atom.getPathname()
                              + TERMINATOR);
                }
            }
        else
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_INVALID);
            }
        }


    /***********************************************************************************************
     * Unregister the specified AtomPlugin from the Framework MBean server.
     *
     * @param atom
     */

    public void unregisterAtom(final AtomPlugin atom)
        {
        if ((atom != null)
            && (atom.getObjectName() != null)
            && (getMBeanServer() != null)
            && (getMBeanServer().isRegistered(atom.getObjectName())))
            {
            try
                {
                getMBeanServer().unregisterMBean(atom.getObjectName());
                LOGGER.logTimedEvent(METADATA_MBEAN_UNREGISTER
                                        + SPACE
                                        + METADATA_NAME
                                        + atom.getPathname()
                                        + TERMINATOR);
                }

            catch (InstanceNotFoundException exception)
                {
                LOGGER.logTimedEvent(METADATA_MBEAN_UNREGISTER
                                        + SPACE
                                        + METADATA_EXCEPTION
                                        + "InstanceNotFoundException"
                                        + TERMINATOR
                                        + SPACE
                                        + METADATA_NAME
                                        + atom.getPathname()
                                        + TERMINATOR);
                }

            catch (MBeanRegistrationException exception)
                {
                LOGGER.logTimedEvent(METADATA_MBEAN_UNREGISTER
                                        + SPACE
                                        + METADATA_EXCEPTION
                                        + "MBeanRegistrationException"
                                        + TERMINATOR
                                        + SPACE
                                        + METADATA_NAME
                                        + atom.getPathname()
                                        + TERMINATOR);
                }
            }
        else
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_INVALID);
            }
        }


    /***********************************************************************************************
     * Get the MBeanServer.
     *
     * @return MBeanServer
     */

    public final MBeanServer getMBeanServer()
        {
        return (this.serverMBean);
        }


    /***********************************************************************************************
     * Set the MBeanServer.
     *
     * @param server
     */

    public final void setMBeanServer(final MBeanServer server)
        {
        this.serverMBean = server;
        }


    /***********************************************************************************************
     * Get the HttpMBeanAdaptor which allows HTTP access to the MBean server.
     *
     * @return HttpMBeanAdaptor
     */

    public final HttpMBeanAdaptor getHttpAdaptor()
        {
        return (this.adaptorHTTP);
        }


    /***********************************************************************************************
     * Set the HttpMBeanAdaptor to allow HTTP access to the MBean server.
     *
     * @param adaptor
     */

    public final void setHttpAdaptor(final HttpMBeanAdaptor adaptor)
        {
        this.adaptorHTTP = adaptor;
        }


    /**********************************************************************************************/
    /* Atom Management                                                                            */
    /***********************************************************************************************
     * Startup the Atom.
     *
     * @return boolean
     */

    public boolean startupAtom()
        {
        // Set up all ContextActions for the new Framework...
        clearUserObjectContextActionGroups();
        setFrameworkContextActions();

        return (true);
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

        return (true);
        }


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
    /* ContextAction Management                                                                   */
    /***********************************************************************************************
     * Set up the Framework ContextActions.
     * There is one ContextActionGroup for 'static' items, which must be visible at all times,
     * and another for implementation-specific items.
     */

     private void setFrameworkContextActions()
         {
         final ContextActionGroup groupStatic;
         final ContextActionGroup groupDynamic;
         ContextAction actionContext;
         URL imageURL;

         // We *assume* that we start with NO ContextActionGroups defined!
         // The Static group is a container for anything like LanguageChooser,
         // which must be there all the time
         // It is named from the Level of the Framework, i.e. in most cases 'Framework'
         groupStatic = new ContextActionGroup(getLevel() + ActionGroup.STATIC.getName(),
                                              true,
                                              true);
         // This is at index ActionGroup.STATIC
         addUserObjectContextActionGroup(groupStatic);

         // The Dynamic group is specific to this Framework implementation
         // and has the name of the Atom, e.g. 'Starbase'
         groupDynamic = new ContextActionGroup(getName() + ActionGroup.DYNAMIC.getName(),
                                               true,
                                               true);
         // This is at index ActionGroup.DYNAMIC
         addUserObjectContextActionGroup(groupDynamic);

         //-----------------------------------------------------------------------------------------
         // Add the Static Actions (there are no Framework Dynamic Actions)

         // ContextAction: Logout the current User
         imageURL = getClass().getResource(ACTION_ICON_LOGOUT);

         if (imageURL != null)
             {
             actionContext = new ContextAction(ACTION_LOGOUT,
                                               new ImageIcon(imageURL),
                                               TOOLTIP_ACTION_LOGOUT,
                                               KeyEvent.VK_L,
                                               true,
                                               false)
                 {
                 public void actionPerformed(final ActionEvent event)
                     {
                     try
                         {
                         frameworkLogout(event);
                         }

                     catch (FrameworkException exception)
                         {
                         handleException(exception,
                                         EXCEPTION_UNABLE_TO_LOGOUT,
                                         EventStatus.WARNING);
                         }
                     }
                 };

             groupStatic.addContextAction(actionContext);
             }

         // ContextAction: Leave the Framework
         imageURL = getClass().getResource(ACTION_ICON_EXIT);

         if (imageURL != null)
             {
             actionContext = new ContextAction(ACTION_EXIT,
                                               new ImageIcon(imageURL),
                                               TOOLTIP_ACTION_EXIT,
                                               KeyEvent.VK_X,
                                               true,
                                               false)
                 {
                 public void actionPerformed(final ActionEvent event)
                     {
                     frameworkExit(event);
                     }
                 };

             groupStatic.addContextAction(actionContext);
             }

         // Do we allow the User to see the Framework instance?
         if (REGISTRY_MODEL.getLoggedInUser().getRole().isFrameworkViewer())
             {
             // Look and Feel Actions
             final ContextActionGroup actionGroup;

             actionGroup = UserInterface.createContextActionGroup("LookAndFeel");

             if ((actionGroup != null)
                 && (!actionGroup.isEmpty()))
                 {
                 final Iterator<ContextAction> iterActions;

                 groupStatic.addContextAction(ContextAction.getSeparator(true, false));
                 iterActions = actionGroup.getActions();

                 while (iterActions.hasNext())
                     {
                     groupStatic.addContextAction(iterActions.next());
                     }
                 }
             else
                 {
                 LOGGER.debug("NULL ContextActionGroup in building Framework Menu");
                 }
             }

         // Add a separator, but only to the menu
         groupStatic.addContextAction(ContextAction.getSeparator(true, false));

         // ContextAction: Save all Resource changes to the database
         imageURL = getClass().getResource(ACTION_ICON_SAVE);

         if (imageURL != null)
             {
             actionContext = new ContextAction(ACTION_SAVE,
                                               new ImageIcon(imageURL),
                                               TOOLTIP_ACTION_SAVE,
                                               KeyEvent.VK_S,
                                               true,
                                               true)
             {
             public void actionPerformed(final ActionEvent event)
                 {
                 // Stop any existing SwingWorker
                 SwingWorker.disposeWorker(workerBackup, true, SWING_WORKER_STOP_DELAY);
                 workerBackup = null;

                 // Prepare another thread to do the Node backup
                 // so that the UI does not freeze...
                 workerBackup = new SwingWorker(REGISTRY.getThreadGroup(),
                                                "SwingWorker FrameworkData")
                     {
                     public Object construct()
                         {
                         LOGGER.debugTimerTick("FrameworkData");
                         // Get the latest Resources
                         readResources();

                         if (!isStopping())
                             {
                             // Backup the RegistryModel to the DataStore
                             REGISTRY_MANAGER.updateRegistry();
                             }

                         // There is no result to pass to the Event Dispatching Thread
                         return (null);
                         }

                     // Display updates occur on the Event Dispatching Thread
                     public void finished()
                         {
                         // There is nothing to do on the Event Dispatching Thread
                         LOGGER.logTimedEvent("[target=registry] [action=updated]");
                         }
                     };

                 // Start the Thread we have prepared...
                 workerBackup.start();
                 }
             };

             groupStatic.addContextAction(actionContext);
             }

         // ContextAction: Run the Garbage Collector
         imageURL = getClass().getResource(ACTION_ICON_DELETE);

         if (imageURL != null)
             {
             actionContext = new ContextAction(ACTION_GC,
                                               new ImageIcon(imageURL),
                                               TOOLTIP_ACTION_GC,
                                               KeyEvent.VK_G,
                                               true,
                                               true)
                 {
                 public void actionPerformed(final ActionEvent event)
                     {
                     final Runtime runTime = Runtime.getRuntime();

                     System.gc();
                     LOGGER.logTimedEvent("[target=gc] [action=run] "
                                             + "[max=" + runTime.maxMemory() + "] "
                                             + "[total=" + runTime.totalMemory() + "] "
                                             + "[free=" + runTime.freeMemory() + "] "
                                             + "[cpus=" + runTime.availableProcessors() + "]");
                     }
                 };

             groupStatic.addContextAction(actionContext);
             }
         }


     /*********************************************************************************************/
     /* ContextAction Implementations                                                             */
     /**********************************************************************************************
      * Performs the action defined for "Framework|Logout".
      *
      * @param event
      *
      * @throws FrameworkException
      */

     private void frameworkLogout(final ActionEvent event) throws FrameworkException
         {
         final int intChoice;
         final String [] strMessage =
             {
                     MSG_CONFIRM_LOGOUT_0,
                     MSG_CONFIRM_LOGOUT_1
             };

         intChoice = JOptionPane.showOptionDialog(null,
                                                  strMessage,
                                                  REGISTRY.getFramework().getName() + SPACE + MSG_LOGOUT,
                                                  JOptionPane.YES_NO_OPTION,
                                                  JOptionPane.QUESTION_MESSAGE,
                                                  null,
                                                  null,
                                                  null);

         if (intChoice == JOptionPane.YES_OPTION)
             {
             // Remove the current configuration from the Registry Model
             // and log back in again when the User is ready
             REGISTRY_CONTROLLER.logoutFramework(REGISTRY.getFramework());
             REGISTRY_CONTROLLER.loginFramework(REGISTRY.getFramework());
             }
         }


    /**********************************************************************************************
     * Performs the action defined for the ContextAction "Framework|Exit".
     * See similar code in UserInterfaceFrame.initialiseUI(), LoginTab.initialiseUI() and ShutdownDialog.
     *
     * @param event
     */

    private void frameworkExit(final ActionEvent event)
        {
        final int intChoice;
        final String [] strMessage =
            {
            MSG_CONFIRM_SHUTDOWN_0,
            MSG_CONFIRM_SHUTDOWN_1
            };

        intChoice = JOptionPane.showOptionDialog(null,
                                                 strMessage,
                                                 REGISTRY.getFramework().getName() + SPACE + DIALOG_SHUTDOWN,
                                                 JOptionPane.YES_NO_OPTION,
                                                 JOptionPane.QUESTION_MESSAGE,
                                                 null,
                                                 null,
                                                 null);
        if (intChoice == JOptionPane.YES_OPTION)
            {
            REGISTRY_CONTROLLER.stopFramework(REGISTRY.getFramework());
            REGISTRY_CONTROLLER.exitFramework(REGISTRY.getFramework());
            }
        }


    /**********************************************************************************************/
    /* Utilities                                                                                  */
    /***********************************************************************************************
     * Fully debug the Framework.
     */

     public final void showDebugData()
         {
         if (getDebugMode())
             {
             LOGGER.log("Framework Miscellaneous");
             LOGGER.log(INDENT + "[ID=" + getID() + "]");
             LOGGER.log(INDENT + "[Level=" + getLevel() + "]");

             // RecursionLevels
             LOGGER.log(INDENT + "[Recursion Levels]");
             final List<String> listLevels = getRecursionLevels();

             for (int i = 0; i < listLevels.size(); i++)
                 {
                 final String level = listLevels.get(i);
                 LOGGER.log(INDENT + INDENT + "[Level " + i + "=" + level + "]");
                 }

             LOGGER.log(INDENT + "[Pathname=" + getPathname() + "]");
             LOGGER.log(INDENT + "[ResourceKey=" + getResourceKey() + "]");
             LOGGER.log(INDENT + "[ClassFound=" + isClassFound() + "]");
             LOGGER.log(INDENT + "[Locked=" + isLocked() + "]");
             LOGGER.log(INDENT + "[Updated=" + isUpdated() + "]");
             LOGGER.log(INDENT + "[UpdatesAllowed=" + isUpdateAllowed() + "]");
             LOGGER.log(INDENT + "[DebugMode=" + getDebugMode() + "]");

             LOGGER.log("Framework XML Bean");
             LOGGER.log(INDENT + "[Name=" + getName() + "]");
             LOGGER.log(INDENT + "[UserRoleMask=" + getUserRoles() + "]");
             LOGGER.log(INDENT + "[Editable=" + isEditable() + "]");
             LOGGER.log(INDENT + "[Active=" + isActive() + "]");
             LOGGER.log(INDENT + "[LoadAtStart=" + isLoadAtStart() + "]");
             LOGGER.log(INDENT + "[EditorClassname=" + getEditorClassname() + "]");
             LOGGER.log(INDENT + "[Description=" + getDescription() + "]");
             LOGGER.log(INDENT + "[LanguageISOCode=" + getLanguageISOCode() + "]");
             LOGGER.log(INDENT + "[CountryISOCode=" + getCountryISOCode() + "]");
             LOGGER.log(INDENT + "[TimeZoneCode=" + getTimeZoneCode() + "]");
             LOGGER.log(INDENT + "[Longitude=" + getLongitude() + "]");
             LOGGER.log(INDENT + "[Latitude=" + getLatitude() + "]");
             LOGGER.log(INDENT + "[SplashScreenFilename=" + getSplashScreenFilename() + "]");
             LOGGER.log(INDENT + "[IconFilename=" + getIconFilename() + "]");
             LOGGER.log(INDENT + "[HelpFilename=" + getHelpFilename() + "]");
             LOGGER.log(INDENT + "[AboutFilename=" + getAboutFilename() + "]");
             LOGGER.log(INDENT + "[MapFilename=" + getMapFilename() + "]");
             // TODO POIs LOGGER.log(INDENT + "[PointOfInterestFilename=" + getCompositePointOfInterestList() + "]");
             LOGGER.log(INDENT + "[MapTopLeftLongitude=" + getMapTopLeftLongitude() + "]");
             LOGGER.log(INDENT + "[MapTopLeftLatitude=" + getMapTopLeftLatitude() + "]");
             LOGGER.log(INDENT + "[MapBottomRightLongitude=" + getMapBottomRightLongitude() + "]");
             LOGGER.log(INDENT + "[MapBottomRightLatitude=" + getMapBottomRightLatitude() + "]");

             LOGGER.log("Framework Dates and Times");
             LOGGER.log(INDENT + "[CreatedDate=" + getCreatedDate() + "]");
             LOGGER.log(INDENT + "[CreatedTime=" + getCreatedTime() + "]");
             LOGGER.log(INDENT + "[ModifiedDate=" + getModifiedDate() + "]");
             LOGGER.log(INDENT + "[ModifiedTime=" + getModifiedTime() + "]");
             }
         }


    /***********************************************************************************************
     * Validate the Framework.
     *
     * @return boolean
     */

    public boolean validatePlugin()
        {
        boolean boolValid;

        boolValid = super.validatePlugin();

//        if ((getUIComponent() == null)
//            || (getHostTreeNode() == null)
//            || (getRecursionLevels().size() < 1))
////            || (getAtoms() == null)
////            || (getTasks() == null)
////            || (getUserRoles() == null))
//            {
//            getValidationErrors().add("Failed validation in " + getName());
//            boolValid = false;
//            }

        return (boolValid);
        }


    /***********************************************************************************************
     * Get the XML part of the Framework.
     *
     * @return XmlObject
     */

     public final Framework getXml()
         {
         if (super.getXml() == null)
             {
             throw new FrameworkException(EXCEPTION_PARAMETER_NULL);
             }

         return ((Framework)super.getXml());
         }


    /**********************************************************************************************/
    /* Events                                                                                     */
    /***********************************************************************************************
     * Notify all listeners of FrameworkChangedEvents.
     *
     * @param eventsource
     */

    public final void notifyFrameworkChangedEvent(final Object eventsource)
        {
        List<FrameworkChangedListener> listeners;
        final FrameworkChangedEvent changeEvent;

        // Create a Thread-safe List of Listeners
        listeners = new CopyOnWriteArrayList<FrameworkChangedListener>(getFrameworkChangedListeners());

        // Create an FrameworkChangedEvent
        changeEvent = new FrameworkChangedEvent(eventsource, this);

        // Fire the event to every listener
        synchronized(listeners)
            {
            for (int i = 0; i < listeners.size(); i++)
                {
                final FrameworkChangedListener changeListener;

                changeListener = listeners.get(i);
                changeListener.frameworkChanged(changeEvent);
                }
            }

        // Help the GC?
        listeners = null;
        }


    /***********************************************************************************************
     * Get the FrameworkChanged Listeners (mostly for testing).
     *
     * @return Vector<FrameworkChangedListener>
     */

    public final Vector<FrameworkChangedListener> getFrameworkChangedListeners()
        {
        return (this.vecFrameworkChangedListeners);
        }


    /***********************************************************************************************
     * Add a listener for this event.
     *
     * @param listener
     */

    public final void addFrameworkChangedListener(final FrameworkChangedListener listener)
        {
        if ((listener != null)
            && (getFrameworkChangedListeners() != null))
            {
            getFrameworkChangedListeners().addElement(listener);
            }
        }


    /***********************************************************************************************
     * Remove a listener for this event.
     *
     * @param listener
     */

    public final void removeFrameworkChangedListener(final FrameworkChangedListener listener)
        {
        if ((listener != null)
            && (getFrameworkChangedListeners() != null))
            {
            getFrameworkChangedListeners().removeElement(listener);
            }
        }
    }
