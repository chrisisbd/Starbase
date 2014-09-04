package org.lmn.fc.model.registry;

import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.common.constants.ResourceKeys;
import org.lmn.fc.model.actions.ActionDataInterface;
import org.lmn.fc.model.actions.ActionStatus;
import org.lmn.fc.model.datatypes.DegMinSecInterface;
import org.lmn.fc.model.datatypes.HourMinSecInterface;
import org.lmn.fc.model.datatypes.YearMonthDayInterface;
import org.lmn.fc.model.emails.EmailMessageInterface;
import org.lmn.fc.model.locale.CountryPlugin;
import org.lmn.fc.model.locale.LanguagePlugin;
import org.lmn.fc.model.logging.Logger;
import org.lmn.fc.model.lookandfeels.LookAndFeelPlugin;
import org.lmn.fc.model.plugins.AtomPlugin;
import org.lmn.fc.model.plugins.FrameworkPlugin;
import org.lmn.fc.model.registry.impl.RegistryManager;
import org.lmn.fc.model.registry.impl.RegistryModel;
import org.lmn.fc.model.resources.ExceptionPlugin;
import org.lmn.fc.model.resources.PropertyPlugin;
import org.lmn.fc.model.resources.QueryPlugin;
import org.lmn.fc.model.resources.StringPlugin;
import org.lmn.fc.model.root.RootPlugin;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.model.users.RolePlugin;
import org.lmn.fc.model.users.UserPlugin;
import org.lmn.fc.ui.UIComponentPlugin;

import java.net.URL;
import java.sql.Date;
import java.sql.Time;
import java.util.Hashtable;
import java.util.TimeZone;
import java.util.Vector;


/***************************************************************************************************
 * The RegistryPlugin.
 */

public interface RegistryPlugin extends FrameworkConstants,
                                        FrameworkStrings,
                                        FrameworkMetadata,
                                        ResourceKeys
    {
    RegistryManagerPlugin REGISTRY_MANAGER = RegistryManager.getInstance();
    RegistryModelPlugin REGISTRY_MODEL = RegistryModel.getInstance();
    Logger LOGGER = Logger.getInstance();


    int getMaxRecursionDepth();

    //----------------------------------------------------------------------------------------------
    // Registry Lists

    Hashtable<String, RootPlugin> getAtoms();

    Hashtable<String, RootPlugin> getTasks();

    Hashtable<String, RootPlugin> getProperties();

    Hashtable<String, RootPlugin> getQueries();

    Hashtable<String, RootPlugin> getStrings();

    Hashtable<String, RootPlugin> getExceptions();

    Hashtable<String, RolePlugin> getRoles();

    Hashtable<String, CountryPlugin> getCountries();

    Hashtable<String, LanguagePlugin> getLanguages();

//    Hashtable<String, DataTypeParserInterface> getDataTypes();

    Hashtable<String, LookAndFeelPlugin> getLookAndFeels();


    /***********************************************************************************************
     * Get the Hashtable of all of the VersionNumbers.
     *
     * @return Hashtable<String, String>
     */

    Hashtable<String, String> getVersionNumbers();


    /***********************************************************************************************
     * Get the Hashtable of all of the BuildNumbers.
     *
     * @return Hashtable<String, String>
     */

    Hashtable<String, String> getBuildNumbers();


    /***********************************************************************************************
     * Get the Hashtable of all of the BuildStatuses.
     *
     * @return Hashtable<String, String>
     */

    Hashtable<String, String> getBuildStatuses();


    Vector<ActionDataInterface> getActionList();

    Vector<EmailMessageInterface> getEmailOutbox();


    /***********************************************************************************************
     * Get the Framework ThreadGroup.
     *
     * @return ThreadGroup
     */

    ThreadGroup getThreadGroup();

    //----------------------------------------------------------------------------------------------
    // Registration

    void addAtom(String key, RootPlugin plugin);

    void addTask(String key, RootPlugin plugin);

    void addProperty(String key, RootPlugin plugin);

    void addQuery(String key, RootPlugin plugin);

    void addString(String key, RootPlugin plugin);

    void addException(String key, RootPlugin plugin);

    void addRole(String key, RolePlugin plugin);

    void addCountry(String key, CountryPlugin plugin);

    void addLanguage(String key, LanguagePlugin plugin);

//    void addDataType(String key, DataTypeParserInterface dataTypeParserInterface);

    void addLookAndFeel(String key, LookAndFeelPlugin plugin);


    /***********************************************************************************************
     * Add a VersionNumber to the Registry, using the specified key.
     *
     * @param key
     * @param versionnumber
     */

    void addVersionNumber(String key,
                          String versionnumber);


    /***********************************************************************************************
     * Add a BuildNumber to the Registry, using the specified key.
     *
     * @param key
     * @param buildnumber
     */

    void addBuildNumber(String key,
                        String buildnumber);


    /***********************************************************************************************
     * Add a BuildStatus to the Registry, using the specified key.
     *
     * @param key
     * @param buildstatus
     */

    void addBuildStatus(String key,
                        String buildstatus);


    //----------------------------------------------------------------------------------------------
    // Framework convenience methods

    FrameworkPlugin getFramework();

    boolean isFrameworkLoaded();

    void setFrameworkResourceKey(String key);

    int getLevelID(AtomPlugin plugin);

    //----------------------------------------------------------------------------------------------
    // Atoms

    AtomPlugin getAtom(String key);

    void showAtoms(boolean debugmode);

    Vector<Vector> getAtomReport();

    //----------------------------------------------------------------------------------------------
    // Tasks

    TaskPlugin getTask(String key);

    void showTasks(boolean debugmode);

    Vector<Vector> getTaskReport();

    //----------------------------------------------------------------------------------------------
    // Properties

    PropertyPlugin getPropertyData(String key);

    Object getProperty(String key);

    PropertyPlugin getAtomProperty(AtomPlugin plugin, String key);

    void setProperty(String key, Object value);

    int getIntegerProperty(String key);

    void setIntegerProperty(String key, int value);

    double getDoubleProperty(String key);

    void setDoubleProperty(String key, double value);

    boolean getBooleanProperty(String key);

    void setBooleanProperty(String key, boolean value);

    String getStringProperty(String key);

    void setStringProperty(String key, String value);

    DegMinSecInterface getDegMinSecProperty(String key);

    void setDegMinSecProperty(String key, DegMinSecInterface value);

    HourMinSecInterface getHourMinSecProperty(String key);

    void setHourMinSecProperty(String key, HourMinSecInterface value);

    YearMonthDayInterface getYearMonthDayProperty(String key);

    void setYearMonthDayProperty(String key, YearMonthDayInterface value);

    URL getURLProperty(String key);

    void setURLProperty(String key, String value);

    void showProperties(boolean debugmode);

    Vector<Vector> getPropertyReport();

    //----------------------------------------------------------------------------------------------
    // Queries

    QueryPlugin getQueryData(String key);

    QueryPlugin getAtomQuery(AtomPlugin plugin, String key);

    void showQueries(boolean debugmode);

    Vector<Vector> getQueryReport();

    //----------------------------------------------------------------------------------------------
    // Strings

    StringPlugin getStringData(String key);

    String getString(String key);

    StringPlugin getAtomString(AtomPlugin plugin, String key);

    void showStrings(boolean debugmode);

    Vector<Vector> getStringReport();

    //----------------------------------------------------------------------------------------------
    // Exceptions

    ExceptionPlugin getExceptionData(String key);

    String getException(String key);

    ExceptionPlugin getAtomException(AtomPlugin plugin, String key);

    void showExceptions(boolean debugmode);

    Vector<Vector> getExceptionReport();

    //----------------------------------------------------------------------------------------------
    // Roles

    RolePlugin getRole(String key);

    void showRoles(boolean debugmode);

    Vector<Vector> getRoleReport();

    //----------------------------------------------------------------------------------------------
    // Countries

    CountryPlugin getCountry(String key);

    void showCountries(boolean debugmode);

    Vector<Vector> getCountriesReport();

    //----------------------------------------------------------------------------------------------
    // Languages

    LanguagePlugin getLanguage(String key);

    void showLanguages(boolean debugmode);

    Vector<Vector> getLanguagesReport();

    //----------------------------------------------------------------------------------------------
    // DataTypes

//    DataTypeParserInterface getDataType(String key);

//    void showDataTypes(boolean debugmode);

    Vector<Vector> getDataTypesReport();

    //----------------------------------------------------------------------------------------------
    // LookAndFeels

    LookAndFeelPlugin getLookAndFeel(String key);

    void showLookAndFeels(boolean debugmode);

    Vector<Vector> getLookAndFeelsReport();

    //----------------------------------------------------------------------------------------------
    // ActionList

    void addActionToList(ActionDataInterface actiondata);

    void addActionToList(UserPlugin userdata,
                         Date dateraised,
                         Time timeraised,
                         String action,
                         ActionStatus status);

    //----------------------------------------------------------------------------------------------
    // EmailOutbox

    void addEmailToOutbox(EmailMessageInterface emaildata);

    void addEmailToOutbox(UserPlugin userdata,
                          Date dateraised,
                          Time timeraised,
                          String message);

    //----------------------------------------------------------------------------------------------
    // Miscellaneous


    /***********************************************************************************************
     * Get a single BlankUIComponent to cut down on the number of instances...
     *
     * @return UIComponentPlugin
     */

    UIComponentPlugin getBlankUIComponent();

    Vector<Vector> getSystemReport();

    Hashtable<String, UserPlugin> getUsers();

    void addUser(String key, UserPlugin plugin);

    String getFrameworkResourceKey();

    /***********************************************************************************************
     * Get the Framework TimeZone.
     *
     * @return TimeZone
     */

    TimeZone getFrameworkTimeZone();


    /***********************************************************************************************
     * Get the GMT TimeZone.
     *
     * @return TimeZone
     */

    TimeZone getGMTTimeZone();

    UserPlugin getUser(String key);

    void showUsers(boolean debugmode);

    Vector<Vector> getUserReport();

    boolean validateBeanPool();

    long size();
    }
