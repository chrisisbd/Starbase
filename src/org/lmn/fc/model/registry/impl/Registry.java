package org.lmn.fc.model.registry.impl;

import org.lmn.fc.common.comparators.ReportRowsByColumn;
import org.lmn.fc.common.exceptions.FrameworkException;
import org.lmn.fc.common.utilities.time.Chronos;
import org.lmn.fc.common.utilities.time.ChronosHelper;
import org.lmn.fc.model.actions.ActionDataInterface;
import org.lmn.fc.model.actions.ActionStatus;
import org.lmn.fc.model.actions.impl.ActionData;
import org.lmn.fc.model.datatypes.DataTypeDictionary;
import org.lmn.fc.model.datatypes.DegMinSecInterface;
import org.lmn.fc.model.datatypes.HourMinSecInterface;
import org.lmn.fc.model.datatypes.YearMonthDayInterface;
import org.lmn.fc.model.emails.EmailMessageInterface;
import org.lmn.fc.model.emails.impl.EmailMessageData;
import org.lmn.fc.model.locale.CountryPlugin;
import org.lmn.fc.model.locale.LanguagePlugin;
import org.lmn.fc.model.locale.impl.CountryData;
import org.lmn.fc.model.locale.impl.LanguageData;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.lookandfeels.LookAndFeelPlugin;
import org.lmn.fc.model.plugins.AtomPlugin;
import org.lmn.fc.model.plugins.FrameworkPlugin;
import org.lmn.fc.model.registry.RegistryModelPlugin;
import org.lmn.fc.model.registry.RegistryModelUtilities;
import org.lmn.fc.model.registry.RegistryPlugin;
import org.lmn.fc.model.resources.*;
import org.lmn.fc.model.root.RootPlugin;
import org.lmn.fc.model.root.RootType;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.model.users.RolePlugin;
import org.lmn.fc.model.users.UserPlugin;
import org.lmn.fc.ui.UIComponentPlugin;
import org.lmn.fc.ui.components.BlankUIComponent;
import org.lmn.fc.ui.reports.ReportIcon;
import org.lmn.fc.ui.reports.ReportTableHelper;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Date;
import java.sql.Time;
import java.util.*;


/***************************************************************************************************
 * The Registry.
 */

public final class Registry implements RegistryPlugin
    {
    private volatile static Registry REGISTRY_INSTANCE;

    private static final String THREAD_GROUP_NAME = "Framework";
    private static final String MSG_VALIDATION_FAILURE = "Validation failure of";
    private static final int INITIAL_CAPACITY = 100;

    // Initial capacities for the Registry
    private static final int ATOM_COUNT = INITIAL_CAPACITY;
    private static final int TASK_COUNT = INITIAL_CAPACITY;
    private static final int PROPERTY_COUNT = INITIAL_CAPACITY;
    private static final int QUERY_COUNT = INITIAL_CAPACITY;
    private static final int STRING_COUNT = INITIAL_CAPACITY;
    private static final int EXCEPTION_COUNT = INITIAL_CAPACITY;
    private static final int USER_COUNT = 10;
    private static final int ROLE_COUNT = 10;
    private static final int COUNTRY_COUNT = INITIAL_CAPACITY;
    private static final int LANGUAGE_COUNT = INITIAL_CAPACITY;
    private static final int DATATYPE_COUNT = 10;
    private static final int LOOKANDFEEL_COUNT = 10;
    private static final int ACTIONLIST_COUNT = 10;
    private static final int EMAILOUTBOX_COUNT = 10;

    private static final int REPORT_ROWS = 25;
    private static final int REPORT_COLUMNS = 10;

    // These Hashtables hold *all* of the installed Atoms, Tasks and Resources etc.
    private final Hashtable<String, RootPlugin>  hashtableAtoms;
    private final Hashtable<String, RootPlugin>  hashtableTasks;
    private final Hashtable<String, RootPlugin>  hashtableProperties;
    private final Hashtable<String, RootPlugin>  hashtableQueries;
    private final Hashtable<String, RootPlugin>  hashtableStrings;
    private final Hashtable<String, RootPlugin>  hashtableExceptions;
    private final Hashtable<String, UserPlugin> hashtableUsers;
    private final Hashtable<String, RolePlugin> hashtableRoles;
    private final Hashtable<String, CountryPlugin>  hashtableCountries;
    private final Hashtable<String, LanguagePlugin> hashtableLanguages;
    private final Hashtable<String, DataTypeDictionary> hashtableDataTypes;
    private final Hashtable<String, LookAndFeelPlugin> hashtableLookAndFeels;

    // The Registry holds the ActionData so that Actions persist over Logins
    private final Vector<ActionDataInterface> vecActionList;

    // The Registry holds the EmailOutbox so that Emails persist over Logins
    private final Vector<EmailMessageInterface> vecEmailOutbox;

    private final Hashtable<String, String> hashtableVersionNumbers;
    private final Hashtable<String, String> hashtableBuildNumbers;
    private final Hashtable<String, String> hashtableBuildStatuses;

    // The Framework ResourceKey, the primary key of the Registry!
    private String strFrameworkResourceKey;

    // The Framework ThreadGroup
    private ThreadGroup threadGroup;

    private UIComponentPlugin uiBlank;
    private TimeZone timeZone;


    /***********************************************************************************************
     * The Registry is a Singleton!
     *
     * @return Registry
     */

    public static Registry getInstance()
        {
        if (REGISTRY_INSTANCE == null)
            {
            synchronized (Registry.class)
                {
                if (REGISTRY_INSTANCE == null)
                    {
                    REGISTRY_INSTANCE = new Registry();
                    }
                }
            }

        return (REGISTRY_INSTANCE);
        }


    /***********************************************************************************************
     * Validate a Hashtable of Plugins.
     *
     * @param plugins
     *
     * @return boolean
     */

    private static boolean validatePlugins(final Hashtable plugins,
                                           final boolean valid)
        {
        final Enumeration enumPlugins;
        boolean boolValid;

        enumPlugins = plugins.elements();
        boolValid = valid;

        while (enumPlugins.hasMoreElements())
            {
            final RootPlugin plugin;

            plugin = (RootPlugin)enumPlugins.nextElement();

            if (!plugin.validatePlugin())
                {
                // Record all validation failures
                LOGGER.login(MSG_VALIDATION_FAILURE + SPACE + plugin.getName());
                boolValid = false;
                }
            }

        return (boolValid);
        }


    /***********************************************************************************************
     * Privately construct the Registry.
     */

    private Registry()
        {
        hashtableAtoms = new Hashtable<String, RootPlugin>(ATOM_COUNT);
        hashtableTasks = new Hashtable<String, RootPlugin>(TASK_COUNT);
        hashtableProperties = new Hashtable<String, RootPlugin>(PROPERTY_COUNT);
        hashtableQueries = new Hashtable<String, RootPlugin>(QUERY_COUNT);
        hashtableStrings = new Hashtable<String, RootPlugin>(STRING_COUNT);
        hashtableExceptions = new Hashtable<String, RootPlugin>(EXCEPTION_COUNT);
        hashtableUsers = new Hashtable<String, UserPlugin>(USER_COUNT);
        hashtableRoles = new Hashtable<String, RolePlugin>(ROLE_COUNT);
        hashtableCountries = new Hashtable<String, CountryPlugin>(COUNTRY_COUNT);
        hashtableLanguages = new Hashtable<String, LanguagePlugin>(LANGUAGE_COUNT);
        hashtableDataTypes = new Hashtable<String, DataTypeDictionary>(DATATYPE_COUNT);
        hashtableLookAndFeels = new Hashtable<String, LookAndFeelPlugin>(LOOKANDFEEL_COUNT);
        vecActionList = new Vector<ActionDataInterface>(ACTIONLIST_COUNT);
        vecEmailOutbox = new Vector<EmailMessageInterface>(EMAILOUTBOX_COUNT);

        hashtableVersionNumbers = new Hashtable<String, String>(10);
        hashtableBuildNumbers = new Hashtable<String, String>(10);
        hashtableBuildStatuses = new Hashtable<String, String>(10);

        this.threadGroup = new ThreadGroup(THREAD_GROUP_NAME);

        this.uiBlank = null;
        this.timeZone = null;
        }


    /***********************************************************************************************
     * Get the maximum recursion depth allowed by the registered Framework.
     *
     * @return int
     */

    public int getMaxRecursionDepth()
        {
        if ((getFramework() != null)
            && (getFramework().getRecursionLevels() != null))
            {
            return (getFramework().getRecursionLevels().size());
            }
        else
            {
            // Something has gone wrong, or this is premature!
            return (0);
            }
        }


    /**********************************************************************************************/
    /* List Accessors.                                                                            */
    /***********************************************************************************************
     * Get the Hashtable of all of the Atoms.
     *
     * @return Hashtable<String, RootPlugin>
     */

    public Hashtable<String, RootPlugin> getAtoms()
        {
        return (this.hashtableAtoms);
        }


    /***********************************************************************************************
     * Get the Hashtable of all of the Tasks.
     *
     * @return Hashtable<String, RootPlugin>
     */

    public Hashtable<String, RootPlugin> getTasks()
        {
        return (this.hashtableTasks);
        }


    /***********************************************************************************************
     * Get the Hashtable of all of the Properties.
     *
     * @return Hashtable<String, RootPlugin>
     */

    public Hashtable<String, RootPlugin> getProperties()
        {
        return (this.hashtableProperties);
        }


    /***********************************************************************************************
     * Get the Hashtable of all of the Queries.
     *
     * @return Hashtable<String, RootPlugin>
     */

    public Hashtable<String, RootPlugin> getQueries()
        {
        return (this.hashtableQueries);
        }


    /***********************************************************************************************
     * Get the Hashtable of all of the Strings.
     *
     * @return Hashtable<String, RootPlugin>
     */

    public Hashtable<String, RootPlugin> getStrings()
        {
        return (this.hashtableStrings);
        }


    /***********************************************************************************************
     * Get the Hashtable of all of the Exceptions.
     *
     * @return Hashtable<String, RootPlugin>
     */

    public Hashtable<String, RootPlugin> getExceptions()
        {
        return (this.hashtableExceptions);
        }


    /***********************************************************************************************
     * Get the Hashtable of all of the Users.
     *
     * @return Hashtable<String, UserPlugin>
     */

    public Hashtable<String, UserPlugin> getUsers()
        {
        return (this.hashtableUsers);
        }


    /***********************************************************************************************
     * Get the Hashtable of all of the Roles.
     *
     * @return Hashtable<String, RolePlugin>
     */

    public Hashtable<String, RolePlugin> getRoles()
        {
        return (this.hashtableRoles);
        }


    /***********************************************************************************************
     * Get the Hashtable of all of the Countries.
     *
     * @return Hashtable<String, CountryPlugin>
     */

    public Hashtable<String, CountryPlugin> getCountries()
        {
        return (this.hashtableCountries);
        }


    /***********************************************************************************************
     * Get the Hashtable of all of the Languages.
     *
     * @return Hashtable<String, LanguagePlugin>
     */

    public Hashtable<String, LanguagePlugin> getLanguages()
        {
        return (this.hashtableLanguages);
        }


    /***********************************************************************************************
     * Get the Hashtable of all of the DataTypes.
     *
     * @return Hashtable<String, DataTypeDictionary>
     */

    public Hashtable<String, DataTypeDictionary> getDataTypes()
        {
        return (this.hashtableDataTypes);
        }


    /***********************************************************************************************
     * Get the Hashtable of all of the LookAndFeels.
     *
     * @return Hashtable<String, LookAndFeelPlugin>
     */

    public Hashtable<String, LookAndFeelPlugin> getLookAndFeels()
        {
        return (this.hashtableLookAndFeels);
        }


    /***********************************************************************************************
     * Get the Hashtable of all of the VersionNumbers.
     *
     * @return Hashtable<String, String>
     */

    public Hashtable<String, String> getVersionNumbers()
        {
        return (this.hashtableVersionNumbers);
        }


    /***********************************************************************************************
     * Get the Hashtable of all of the BuildNumbers.
     *
     * @return Hashtable<String, String>
     */

    public Hashtable<String, String> getBuildNumbers()
        {
        return (this.hashtableBuildNumbers);
        }


    /***********************************************************************************************
     * Get the Hashtable of all of the BuildStatuses.
     *
     * @return Hashtable<String, String>
     */

    public Hashtable<String, String> getBuildStatuses()
        {
        return (this.hashtableBuildStatuses);
        }


    /***********************************************************************************************
     * Get the Vector of Actions from the ActionList.
     *
     * @return Vector<ActionDataInterface>
     */

    public Vector<ActionDataInterface> getActionList()
        {
        return (this.vecActionList);
        }


    /***********************************************************************************************
     * Get the Vector of EmailMessages from the EmailOutbox.
     *
     * @return Vector<EmailMessageInterface>
     */

    public Vector<EmailMessageInterface> getEmailOutbox()
        {
        return (this.vecEmailOutbox);
        }


    /***********************************************************************************************
     * Get the Framework ThreadGroup.
     *
     * @return ThreadGroup
     */

    public ThreadGroup getThreadGroup()
        {
        return (this.threadGroup);
        }


    /**********************************************************************************************/
    /* Registration methods.                                                                      */
    /***********************************************************************************************
     * Add the Atom to the Registry, using the specified key.
     *
     * @param key
     * @param plugin
     */

    public void addAtom(final String key,
                        final RootPlugin plugin)
        {
        if ((key != null)
            && (!EMPTY_STRING.equals(key))
            && (plugin != null)
            && (plugin.getType() != null)
            && (RootType.ATOM.equals(plugin.getType()))
            && (getAtoms() != null))
            {
            getAtoms().put(key, plugin);
            }
        else
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_NULL);
            }
        }


    /***********************************************************************************************
     * Add the Task to the Registry, using the specified key.
     *
     * @param key
     * @param plugin
     */

    public void addTask(final String key,
                        final RootPlugin plugin)
        {
        if ((key != null)
            && (!EMPTY_STRING.equals(key))
            && (plugin != null)
            && (plugin.getType() != null)
            && (RootType.TASK.equals(plugin.getType()))
            && (getTasks() != null))
            {
            getTasks().put(key, plugin);
            }
        else
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_NULL);
            }
        }


    /***********************************************************************************************
     * Add the Property to the Registry, using the specified key.
     *
     * @param key
     * @param plugin
     */

    public void addProperty(final String key,
                            final RootPlugin plugin)
        {
        if ((key != null)
            && (!EMPTY_STRING.equals(key))
            && (plugin != null)
            && (plugin.getType() != null)
            && (RootType.PROPERTY.equals(plugin.getType()))
            && (getProperties() != null))
            {
            getProperties().put(key, plugin);
            }
        else
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_NULL);
            }
        }


    /***********************************************************************************************
     * Add the Query to the Registry, using the specified key.
     *
     * @param key
     * @param plugin
     */

    public void addQuery(final String key,
                         final RootPlugin plugin)
        {
        if ((key != null)
            && (!EMPTY_STRING.equals(key))
            && (plugin != null)
            && (plugin.getType() != null)
            && (RootType.QUERY.equals(plugin.getType()))
            && (getQueries() != null))
            {
            getQueries().put(key, plugin);
            }
        else
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_NULL);
            }
        }


    /***********************************************************************************************
     * Add the String to the Registry, using the specified key.
     *
     * @param key
     * @param plugin
     */

    public void addString(final String key,
                          final RootPlugin plugin)
        {
        if ((key != null)
            && (!EMPTY_STRING.equals(key))
            && (plugin != null)
            && (plugin.getType() != null)
            && (RootType.STRING.equals(plugin.getType()))
            && (getStrings() != null))
            {
            getStrings().put(key, plugin);
            }
        else
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_NULL);
            }
        }


    /***********************************************************************************************
     * Add the Exception to the Registry, using the specified key.
     *
     * @param key
     * @param plugin
     */

    public void addException(final String key,
                             final RootPlugin plugin)
        {
        if ((key != null)
            && (!EMPTY_STRING.equals(key))
            && (plugin != null)
            && (plugin.getType() != null)
            && (RootType.EXCEPTION.equals(plugin.getType()))
            && (getExceptions() != null))
            {
            getExceptions().put(key, plugin);
            }
        else
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_NULL);
            }
        }


    /***********************************************************************************************
     * Add the User to the Registry, using the specified key.
     *
     * @param key
     * @param plugin
     */

    public void addUser(final String key,
                        final UserPlugin plugin)
        {
        if ((key != null)
            && (plugin != null)
            && (getUsers() != null))
            {
            getUsers().put(key, plugin);
            }
        else
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_NULL);
            }
        }


    /***********************************************************************************************
     * Add the Role to the Registry, using the specified key.
     *
     * @param key
     * @param plugin
     */

    public void addRole(final String key,
                        final RolePlugin plugin)
        {
        if ((key != null)
            && (plugin != null)
            && (getRoles() != null))
            {
            getRoles().put(key, plugin);
            }
        else
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_NULL);
            }
        }


    /***********************************************************************************************
     * Add the Country to the Registry, using the specified key.
     *
     * @param key
     * @param plugin
     */

    public void addCountry(final String key,
                           final CountryPlugin plugin)
        {
        if ((key != null)
            && (!EMPTY_STRING.equals(key))
            && (plugin != null)
            && (getCountries() != null))
            {
            getCountries().put(key, plugin);
            }
        else
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_NULL);
            }
        }


    /***********************************************************************************************
     * Add the Language to the Registry, using the specified key.
     *
     * @param key
     * @param plugin
     */

    public void addLanguage(final String key,
                            final LanguagePlugin plugin)
        {
        if ((key != null)
            && (!EMPTY_STRING.equals(key))
            && (plugin != null)
            && (getLanguages() != null))
            {
            getLanguages().put(key, plugin);
            }
        else
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_NULL);
            }
        }


    /***********************************************************************************************
     * Add the DataType to the Registry, using the specified key.
     *
     * @param key
     * @param dataTypeParserInterface
     */

//    public void addDataType(final String key,
//                            final DataTypeParserInterface dataTypeParserInterface)
//        {
//        if ((key != null)
//            && (!EMPTY_STRING.equals(key))
//            && (dataTypeParserInterface != null)
//            && (getDataTypes() != null))
//            {
//            getDataTypes().put(key, dataTypeParserInterface);
//            }
//        else
//            {
//            throw new FrameworkException(EXCEPTION_PARAMETER_NULL);
//            }
//        }


    /***********************************************************************************************
     * Add the LookAndFeel to the Registry, using the specified key.
     *
     * @param key
     * @param plugin
     */

    public void addLookAndFeel(final String key,
                               final LookAndFeelPlugin plugin)
        {
        if ((key != null)
            && (!EMPTY_STRING.equals(key))
            && (plugin != null)
            && (getLookAndFeels() != null))
            {
            getLookAndFeels().put(key, plugin);
            }
        else
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_NULL);
            }
        }


    /***********************************************************************************************
     * Add a VersionNumber to the Registry, using the specified key.
     *
     * @param key
     * @param versionnumber
     */

    public void addVersionNumber(final String key,
                                 final String versionnumber)
        {
        if ((key != null)
            && (!EMPTY_STRING.equals(key))
            && (versionnumber != null)
            && (!EMPTY_STRING.equals(versionnumber))
            && (getVersionNumbers() != null))
            {
            getVersionNumbers().put(key, versionnumber);
            }
        else
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_NULL);
            }
        }


    /***********************************************************************************************
     * Add a BuildNumber to the Registry, using the specified key.
     *
     * @param key
     * @param buildnumber
     */

    public void addBuildNumber(final String key,
                               final String buildnumber)
        {
        if ((key != null)
            && (!EMPTY_STRING.equals(key))
            && (buildnumber != null)
            && (!EMPTY_STRING.equals(buildnumber))
            && (getBuildNumbers() != null))
            {
            getBuildNumbers().put(key, buildnumber);
            }
        else
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_NULL);
            }
        }


    /***********************************************************************************************
     * Add a BuildStatus to the Registry, using the specified key.
     *
     * @param key
     * @param buildstatus
     */

    public void addBuildStatus(final String key,
                               final String buildstatus)
        {
        if ((key != null)
            && (!EMPTY_STRING.equals(key))
            && (buildstatus != null)
            && (!EMPTY_STRING.equals(buildstatus))
            && (getBuildStatuses() != null))
            {
            getBuildStatuses().put(key, buildstatus);
            }
        else
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_NULL);
            }
        }


    /**********************************************************************************************/
    /* Atom Accessors                                                                             */
    /***********************************************************************************************
     * A convenience method to get the currently loaded Framework.
     *
     * @return FrameworkPlugin
     */

    public FrameworkPlugin getFramework()
        {
        if (!isFrameworkLoaded())
            {
            throw new FrameworkException(EXCEPTION_NO_FRAMEWORK);
            }

        return ((FrameworkPlugin)getAtoms().get(getFrameworkResourceKey()));
        }


    /***********************************************************************************************
     * Return true if the Framework is currently loaded into the Registry.
     * This is intended for use during loading.
     *
     * @return boolean
     */

    public boolean isFrameworkLoaded()
        {
        return (((getFrameworkResourceKey() != null)
                && (!EMPTY_STRING.equals(getFrameworkResourceKey().trim()))
                && (getAtoms() != null)
                && (getAtoms().containsKey(getFrameworkResourceKey()))
                && ((getAtoms().get(getFrameworkResourceKey()) instanceof FrameworkPlugin))));
        }


    /***********************************************************************************************
     * Get the Framework ResourceKey.
     *
     * @return String
     */

    public String getFrameworkResourceKey()
        {
        return (this.strFrameworkResourceKey);
        }


    /***********************************************************************************************
     * Get the Framework TimeZone.
     *
     * @return TimeZone
     */

    public TimeZone getFrameworkTimeZone()
        {
        if ((getFramework() != null)
            && (getFramework().getTimeZoneCode() != null)
            && (this.timeZone == null))
            {
            this.timeZone = TimeZone.getTimeZone(getFramework().getTimeZoneCode());
            }

        return (this.timeZone);
        }


    /***********************************************************************************************
     * Get the GMT TimeZone.
     *
     * @return TimeZone
     */

    public TimeZone getGMTTimeZone()
        {
        return (TimeZone.getTimeZone(DEFAULT_TIME_ZONE_ID));
        }


    /***********************************************************************************************
     * Get the LevelID of the specified AtomPlugin.
     *
     * @param plugin
     *
     * @return int
     */

    public int getLevelID(final AtomPlugin plugin)
        {
        // ToDo error handling
        return(getFramework().getRecursionLevels().indexOf(plugin.getLevel()));
        }


    /***********************************************************************************************
     * Set the Framework ResourceKey when the Framework is registered by a DAO.
     * This allows access to the top of the tree of beans in the pool...
     *
     * @param key
     */

    public void setFrameworkResourceKey(final String key)
        {
        if ((key != null)
            && (!EMPTY_STRING.equals(key.trim())))
            {
            this.strFrameworkResourceKey = key.trim();
            }
        else
            {
            this.strFrameworkResourceKey = EMPTY_STRING;
            }
        }


    /***********************************************************************************************
     * Get the AtomPlugin corresponding to the specified key.
     *
     * @param key
     *
     * @return AtomPlugin
     */

    public AtomPlugin getAtom(final String key)
        {
        if ((getAtoms() != null)
            && (getAtoms().containsKey(key))
            && (getAtoms().get(key) instanceof AtomPlugin))
            {
            return ((AtomPlugin)getAtoms().get(key));
            }
        else
            {
            LOGGER.handleAtomException(getFramework(),
                                       getFramework().getRootTask(),
                                       Registry.class.getName(),
                                       new FrameworkException("[Key=" + key + "]"),
                                       getException("Registry.Key.Atom") + " [key=" + key + "]",
                                       EventStatus.FATAL);
            }

        return (null);
        }


    /***********************************************************************************************
     * List all of the loaded Atoms.
     *
     * @param debugmode
     */

    public void showAtoms(final boolean debugmode)
        {
        if ((debugmode)
            && (getAtoms() != null))
            {
            LOGGER.log("\nLoaded Atoms");

            for (Enumeration<RootPlugin> enumeration = getAtoms().elements();
                 enumeration.hasMoreElements();)
                {
                final RootPlugin atom = enumeration.nextElement();

                LOGGER.log(INDENT + "[" + atom.getResourceKey() + "]");
                }
            }
        }


    /***********************************************************************************************
     * ToDo ATOM report.
     *
     * @return Vector<Vector>
     */

    public Vector<Vector> getAtomReport()
        {
        return null;
        }


    /***********************************************************************************************
     * Get the TaskPlugin corresponding to the specified key.
     *
     * @param key
     *
     * @return TaskPlugin
     */

    public TaskPlugin getTask(final String key)
        {
        if ((getTasks() != null)
            && (getTasks().containsKey(key))
            && (getTasks().get(key) instanceof TaskPlugin))
            {
            return ((TaskPlugin)getTasks().get(key));
            }
        else
            {
            LOGGER.handleAtomException(getFramework(),
                                       getFramework().getRootTask(),
                                       Registry.class.getName(),
                                       new FrameworkException("[Key=" + key + "]"),
                                       getException("Registry.Key.Task") + " [key=" + key + "]",
                                       EventStatus.FATAL);
            }

        return (null);
        }


    /***********************************************************************************************
     * List all of the loaded Tasks.
     *
     * @param debugmode
     */

    public void showTasks(final boolean debugmode)
        {
        if ((debugmode)
            && (getTasks() != null))
            {
            LOGGER.log("\nLoaded Tasks");

            for (Enumeration<RootPlugin> enumeration = getTasks().elements();
                 enumeration.hasMoreElements();)
                {
                final RootPlugin task = enumeration.nextElement();

                LOGGER.log(INDENT + "[" + task.getResourceKey() + "]");
                }
            }
        }


    /***********************************************************************************************
     * Todo TASK report.
     *
     * @return Vector<Vector>
     */

    public Vector<Vector> getTaskReport()
        {
        return null;
        }


    /**********************************************************************************************/
    /* Property Accessors                                                                         */
    /***********************************************************************************************
     * Get the PropertyPlugin for the specified Property ResourceKey.
     *
     *  @param key The Property ResourceKey, of the form "Level0.Level1.Level2.Level3"
     *
     *  @return PropertyPlugin
     **/

    public PropertyPlugin getPropertyData(final String key)
        {
        if ((getProperties() != null)
            && (getProperties().containsKey(key))
            && (getProperties().get(key) instanceof PropertyPlugin))
            {
            return ((PropertyPlugin)getProperties().get(key));
            }
        else
            {
            // Cannot get the requested PropertyPlugin...
            LOGGER.handleAtomException(getFramework(),
                                       getFramework().getRootTask(),
                                       Registry.class.getName(),
                                       new FrameworkException("[key=" + key + "]"),
                                       getException("Registry.Key.Property") + " [key=" + key + "]",
                                       EventStatus.FATAL);
            }

        return (null);
        }


    /***********************************************************************************************
     * Get a Property Value from the RegistryModel HashTable as an Object
     * Check that the Property was instantiated correctly
     *
     * @param key
     *
     * @return Object
     */

    public Object getProperty(final String key)
        {
        try
            {
            final PropertyPlugin propertyData = (PropertyPlugin)getProperties().get(key);

            if (propertyData != null)
                {
                return((propertyData.getResource()));
                }
            else
                {
                throw new FrameworkException("RegistryModel.Get.Property" + " [key=" + key + "]");
                }
            }

        catch(FrameworkException exception)
            {
            LOGGER.handleAtomException(getFramework(),
                                       getFramework().getRootTask(),
                                       Registry.class.getName(),
                                       new FrameworkException("[key=" + key + "]"),
                                       getException("Registry.Key.Property") + " [key=" + key + "]",
                                       EventStatus.FATAL);
            }

        return (null);
        }


    /***********************************************************************************************
     * Todo ATOM property.
     *
     * @param plugin
     * @param key
     *
     * @return PropertyPlugin
     */

    public PropertyPlugin getAtomProperty(final AtomPlugin plugin,
                                          final String key)
        {
        return null;
        }


    /***********************************************************************************************
     * Set a Property value from a specified object.
     * Check that the value classname is the same as in the database.
     *
     * @param key
     * @param value
     */

    public void setProperty(final String key, final Object value)
        {
        try
            {
            final PropertyPlugin propertyData = (PropertyPlugin)getProperties().get(key);

            if ((propertyData != null)
                && (value.getClass().getName().equals(propertyData.getDataType())))
                {
                propertyData.setResource(value);
                }
            else
                {
                throw new FrameworkException(getException("Registry.Key.Property") + " [key=" + key + "]");
                }
            }

        catch(FrameworkException exception)
            {
            LOGGER.handleAtomException(getFramework(),
                                       getFramework().getRootTask(),
                                       Registry.class.getName(),
                                       exception,
                                       getException("Registry.Key.Property") + " [key=" + key + "]",
                                       EventStatus.FATAL);
            }
        }


    /***********************************************************************************************
     * Get a Property Value from the RegistryModel HashTable as a java.lang.Integer
     *
     * @param key
     *
     * @return int
     */

    public int getIntegerProperty(final String key)
        {
        try
            {
            final PropertyPlugin propertyData = (PropertyPlugin)getProperties().get(key);

            if ((propertyData != null)
                    && (propertyData.getDataType().equals(Integer.class.getName())))
                {
                return((Integer) propertyData.getResource());
                }
            else
                {
                throw new FrameworkException(getException("Registry.Key.Property.Integer") + " [key=" + key + "]");
                }
            }

        catch(FrameworkException exception)
            {
            LOGGER.handleAtomException(getFramework(),
                                       getFramework().getRootTask(),
                                       Registry.class.getName(),
                                       exception,
                                       getException("Registry.Key.Property.Integer") + " [key=" + key + "]",
                                       EventStatus.FATAL);
            }

        return (0);
        }


    /***********************************************************************************************
     * Set an Integer Property.
     *
     * @param key
     * @param value
     */

    public void setIntegerProperty(final String key, final int value)
        {
        try
             {
             final PropertyPlugin propertyData = (PropertyPlugin)getProperties().get(key);

             if ((propertyData != null)
                     && (propertyData.getDataType().equals(Integer.class.getName())))
                 {
                 propertyData.setResource(value);
                 }
             else
                 {
                 throw new FrameworkException(getException("Registry.Key.Property.Integer") + " [key=" + key + "] [value=" + value + "]");
                 }
             }

         catch(FrameworkException exception)
             {
             LOGGER.handleAtomException(getFramework(),
                                        getFramework().getRootTask(),
                                        Registry.class.getName(),
                                        exception,
                                        getException("Registry.Key.Property.Integer") + " [key=" + key + "]",
                                        EventStatus.FATAL);
             }
        }


    /***********************************************************************************************
     * Get a Property Value from the RegistryModel HashTable as a java.lang.Double
     *
     * @param key
     * @return double
     */

    public double getDoubleProperty(final String key)
        {
        try
            {
            final PropertyPlugin propertyData = (PropertyPlugin)getProperties().get(key);

            if ((propertyData != null)
                    && (propertyData.getDataType().equals(Double.class.getName())))
                {
                return((Double) propertyData.getResource());
                }
            else
                {
                throw new FrameworkException(getException("Registry.Key.Property.Double") + " [key=" + key + "]");
                }
            }

        catch(FrameworkException exception)
            {
            LOGGER.handleAtomException(getFramework(),
                                       getFramework().getRootTask(),
                                       Registry.class.getName(),
                                       exception,
                                       getException("Registry.Key.Property.Double") + " [key=" + key + "]",
                                       EventStatus.FATAL);
            }

        return (0.0);
        }


    /***********************************************************************************************
     * Set a Double Property.
     *
     * @param key
     * @param value
     */

    public void setDoubleProperty(final String key, final double value)
        {
        try
             {
             final PropertyPlugin propertyData = (PropertyPlugin)getProperties().get(key);

             if ((propertyData != null)
                     && (propertyData.getDataType().equals(Double.class.getName())))
                 {
                 propertyData.setResource(value);
                 }
             else
                 {
                 throw new FrameworkException(getException("Registry.Key.Property.Double") + " [key=" + key + "] [value=" + value + "]");
                 }
             }

         catch(FrameworkException exception)
             {
             LOGGER.handleAtomException(getFramework(),
                                        getFramework().getRootTask(),
                                        Registry.class.getName(),
                                        exception,
                                        getException("Registry.Key.Property.Double") + " [key=" + key + "]",
                                        EventStatus.FATAL);
             }
        }


    /***********************************************************************************************
     * Get a Property Value from the RegistryModel HashTable as a boolean.
     *
     * @param key
     * @return boolean
     */

    public boolean getBooleanProperty(final String key)
        {
        try
            {
            final PropertyPlugin propertyData = (PropertyPlugin)getProperties().get(key);

            if ((propertyData != null)
                    && (propertyData.getDataType().equals(Boolean.class.getName())))
                {
                return((Boolean) propertyData.getResource());
                }
            else
                {
                LOGGER.log("[key=" + key + "]");
                throw new FrameworkException(getException("Registry.Key.Property.Boolean") + " [key=" + key + "]");
                }
            }

        catch(FrameworkException exception)
            {
            LOGGER.handleAtomException(getFramework(),
                                       getFramework().getRootTask(),
                                       Registry.class.getName(),
                                       exception,
                                       getException("Registry.Key.Property.Boolean") + " [key=" + key + "]",
                                       EventStatus.FATAL);
            }

        return (false);
        }


    /***********************************************************************************************
     * Set a Boolean Property.
     *
     * @param key
     * @param value
     */

    public void setBooleanProperty(final String key, final boolean value)
        {
        try
             {
             final PropertyPlugin propertyData = (PropertyPlugin)getProperties().get(key);

             if ((propertyData != null)
                     && (propertyData.getDataType().equals(Boolean.class.getName())))
                 {
                 propertyData.setResource(value);
                 }
             else
                 {
                 throw new FrameworkException(getException("Registry.Key.Property.Boolean") + " [key=" + key + "] [value=" + value + "]");
                 }
             }

         catch(FrameworkException exception)
             {
             LOGGER.handleAtomException(getFramework(),
                                        getFramework().getRootTask(),
                                        Registry.class.getName(),
                                        exception,
                                        getException("Registry.Key.Property.Boolean") + " [key=" + key + "]",
                                        EventStatus.FATAL);
             }
        }


    /***********************************************************************************************
     * Get a Property Value from the RegistryModel HashTable as an String.
     *
     * @param key
     * @return String
     */

    public String getStringProperty(final String key)
        {
        try
            {
            final PropertyPlugin propertyData = (PropertyPlugin)getProperties().get(key);

            if ((propertyData != null)
                    && (propertyData.getDataType().equals(String.class.getName())))
                {
                return ((String)propertyData.getResource());
                }
            else
                {
                throw new FrameworkException("Not found [key=" + key + "]");
                }
            }

        catch(FrameworkException exception)
            {
            LOGGER.handleAtomException(getFramework(),
                                       getFramework().getRootTask(),
                                       Registry.class.getName(),
                                       exception,
                                       getException("Registry.Key.Property.String") + " [key=" + key + "]",
                                       EventStatus.FATAL);
            }

        return ("");
        }


    /***********************************************************************************************
     * Set a String Property.
     *
     * @param key
     * @param value
     */

    public void setStringProperty(final String key, final String value)
        {
        try
             {
             final PropertyPlugin propertyData = (PropertyPlugin)getProperties().get(key);

             if ((propertyData != null)
                     && (propertyData.getDataType().equals(String.class.getName())))
                 {
                 propertyData.setResource(new String(value));
                 }
             else
                 {
                 throw new FrameworkException(getException("Registry.Key.Property.String") + " [key=" + key + "] [value=" + value + "]");
                 }
             }

         catch(FrameworkException exception)
             {
             LOGGER.handleAtomException(getFramework(),
                                        getFramework().getRootTask(),
                                        Registry.class.getName(),
                                        exception,
                                        getException("Registry.Key.Property.String") + " [key=" + key + "]",
                                        EventStatus.FATAL);
             }
        }


    /***********************************************************************************************
     * Get a Property Value from the RegistryModel HashTable as a DegMinSec.
     * NOT USED, CONTAINS ERRORS.
     *
     * @param key
     * @return DegMinSec
     */

    public DegMinSecInterface getDegMinSecProperty(final String key)
        {
        try
            {
            final PropertyPlugin propertyData = (PropertyPlugin)getProperties().get(key);

//            if ((propertyData != null)
//                    && (propertyData.getDataType().equals(DegMinSec.class.getName())))
//                {
//                return ((DegMinSecInterface)propertyData.getResource());
//                }
//            else
//                {
//                throw new FrameworkException(getException("Registry.Key.Property.DMS") + " [key=" + key + "]");
//                }
            }

        catch(FrameworkException exception)
            {
            LOGGER.handleAtomException(getFramework(),
                                       getFramework().getRootTask(),
                                       Registry.class.getName(),
                                       exception,
                                       getException("Registry.Key.Property.DMS") + " [key=" + key + "]",
                                       EventStatus.FATAL);
            }

        return (null);
        }


    /***********************************************************************************************
     * Set a DegMinSec Property.
     * NOT USED, CONTAINS ERRORS.
     *
     * @param key
     * @param value
     */

    public void setDegMinSecProperty(final String key, final DegMinSecInterface value)
        {
        try
             {
             final PropertyPlugin propertyData = (PropertyPlugin)getProperties().get(key);

//             if ((propertyData != null)
//                     && (propertyData.getDataType().equals(DegMinSec.class.getName())))
//                 {
//                 propertyData.setResource(value);
//                 }
//             else
//                 {
//                 throw new FrameworkException(getException("Registry.Key.Property.DMS") + " [key=" + key + "] [value=" + value + "]");
//                 }
             }

         catch(FrameworkException exception)
             {
             LOGGER.handleAtomException(getFramework(),
                                        getFramework().getRootTask(),
                                        Registry.class.getName(),
                                        exception,
                                        getException("Registry.Key.Property.DMS") + " [key=" + key + "]",
                                        EventStatus.FATAL);
             }
        }


    /***********************************************************************************************
     * Get a Property Value from the RegistryModel HashTable as an HourMinSecDataType.
     * NOT USED - CONTAINS ERRORS.
     *
     * @param key
     *
     * @return HourMinSecInterface
     */

    public HourMinSecInterface getHourMinSecProperty(final String key)
        {
        try
            {
            final PropertyPlugin propertyData = (PropertyPlugin)getProperties().get(key);

//            if ((propertyData != null)
//                    && (propertyData.getDataType().equals(HourMinSecDataType.class.getName())))
//                {
//                return ((HourMinSecInterface)propertyData.getResource());
//                }
//            else
//                {
//                throw new FrameworkException(getException("Registry.Key.Property.HMS") + " [key=" + key + "]");
//                }
            }

        catch(FrameworkException exception)
            {
            LOGGER.handleAtomException(getFramework(),
                                       getFramework().getRootTask(),
                                       Registry.class.getName(),
                                       exception,
                                       getException("Registry.Key.Property.HMS") + " [key=" + key + "]",
                                       EventStatus.FATAL);
            }

        return (null);
        }


    /***********************************************************************************************
     * Set a HourMinSecDataType Property.
     * NOT USED - CONTAINS ERRORS.
     *
     * @param key
     * @param value
     */

    public void setHourMinSecProperty(final String key, final HourMinSecInterface value)
        {
        try
             {
             final PropertyPlugin propertyData = (PropertyPlugin)getProperties().get(key);

//             if ((propertyData != null)
//                     && (propertyData.getDataType().equals(HourMinSecDataType.class.getName())))
//                 {
//                 propertyData.setResource(value);
//                 }
//             else
//                 {
//                 throw new FrameworkException(getException("Registry.Key.Property.HMS") + " [key=" + key + "] [value=" + value + "]");
//                 }
             }

         catch(FrameworkException exception)
             {
             LOGGER.handleAtomException(getFramework(),
                                        getFramework().getRootTask(),
                                        Registry.class.getName(),
                                        exception,
                                        getException("Registry.Key.Property.HMS") + " [key=" + key + "]",
                                        EventStatus.FATAL);
             }
        }


    /***********************************************************************************************
     * Get a Property Value from the RegistryModel HashTable as a YearMonthDayDataType.
     * NOT USED - CONTAINS ERRORS.
     *
     * @param key
     *
     * @return YearMonthDayInterface
     */

    public YearMonthDayInterface getYearMonthDayProperty(final String key)
        {
        try
            {
            final PropertyPlugin propertyData = (PropertyPlugin)getProperties().get(key);

//            if ((propertyData != null)
//                    && (propertyData.getDataType().equals(YearMonthDayDataType.class.getName())))
//                {
//                return ((YearMonthDayInterface)propertyData.getResource());
//                }
//            else
//                {
//                throw new FrameworkException(getException("Registry.Key.Property.YMD") + " [key=" + key + "]");
//                }
            }

        catch(FrameworkException exception)
            {
            LOGGER.handleAtomException(getFramework(),
                                       getFramework().getRootTask(),
                                       Registry.class.getName(),
                                       exception,
                                       getException("Registry.Key.Property.YMD") + " [key=" + key + "]",
                                       EventStatus.FATAL);
            }

        return (null);
        }


    /***********************************************************************************************
     * Set a YearMonthDayDataType Property.
     * NOT USED - CONTAINS ERRORS.
     *
     * @param key
     * @param value
     */

    public void setYearMonthDayProperty(final String key, final YearMonthDayInterface value)
        {
        try
             {
             final PropertyPlugin propertyData = (PropertyPlugin)getProperties().get(key);

//             if ((propertyData != null)
//                     && (propertyData.getDataType().equals(YearMonthDayDataType.class.getName())))
//                 {
//                 propertyData.setResource(value);
//                 }
//             else
//                 {
//                 throw new FrameworkException(getException("Registry.Key.Property.YMD") + " [key=" + key + "] [value=" + value + "]");
//                 }
             }

         catch(FrameworkException exception)
             {
             LOGGER.handleAtomException(getFramework(),
                                        getFramework().getRootTask(),
                                        Registry.class.getName(),
                                        exception,
                                        getException("Registry.Key.Property.YMD") + " [key=" + key + "]",
                                        EventStatus.FATAL);
             }
        }


    /***********************************************************************************************
     * Get a Property Value from the RegistryModel HashTable as a java.net.URL
     *
     * @param key
     * @return URL
     */

    public URL getURLProperty(final String key)
        {
        try
            {
            final PropertyPlugin propertyData = (PropertyPlugin)getProperties().get(key);

            if ((propertyData != null)
                    && (propertyData.getDataType().equals(URL.class.getName())))
                {
                return ((URL)propertyData.getResource());
                }
            else
                {
                throw new FrameworkException(getException("Registry.Key.Property.URL") + " [key=" + key + "]");
                }
            }

        catch(FrameworkException exception)
            {
            LOGGER.handleAtomException(getFramework(),
                                       getFramework().getRootTask(),
                                       Registry.class.getName(),
                                       exception,
                                       getException("Registry.Key.Property.URL") + " [key=" + key + "]",
                                       EventStatus.FATAL);
            }

        return (null);
        }


    /***********************************************************************************************
     * Set a java.net.URL Property.
     *
     * @param key
     * @param value
     */

    public void setURLProperty(final String key, final String value)
        {
        try
             {
             final PropertyPlugin propertyData = (PropertyPlugin)getProperties().get(key);

             if ((propertyData != null)
                     && (propertyData.getDataType().equals(URL.class.getName())))
                 {
                 propertyData.setResource(new URL(value));
                 }
             else
                 {
                 throw new FrameworkException(getException("Registry.Key.Property.URL") + " [key=" + key + "] [value=" + value + "]");
                 }
             }

         catch(MalformedURLException exception)
             {
             LOGGER.handleAtomException(getFramework(),
                                        getFramework().getRootTask(),
                                        Registry.class.getName(),
                                        exception,
                                        getException("Registry.Key.Property.URL") + " [key=" + key + "]",
                                        EventStatus.FATAL);
             }

         catch(FrameworkException exception)
             {
             LOGGER.handleAtomException(getFramework(),
                                        getFramework().getRootTask(),
                                        Registry.class.getName(),
                                        exception,
                                        getException("Registry.Key.Property.URL") + " [key=" + key + "]",
                                        EventStatus.FATAL);
             }
        }


    /***********************************************************************************************
     * List all of the loaded Properties.
     *
     * @param debugmode
     */

    public void showProperties(final boolean debugmode)
        {
        if ((debugmode)
            && (getProperties() != null))
            {
            LOGGER.log("\nLoaded Properties");

            for (Enumeration<RootPlugin> enumeration = getProperties().elements();
                 enumeration.hasMoreElements();)
                {
                final RootPlugin resource = enumeration.nextElement();

                LOGGER.log(INDENT + "[" + resource.getResourceKey()
                                       + "=" + ((ResourcePlugin)resource).getResource() + "]");
                }
            }
        }


    /***********************************************************************************************
     * List the Properties as a Vector of {updated, key, value, datemodified}.
     *
     * The Vector contains a sub-Vector with the following elements:
     * <code>
     * <li>Icon
     * <li>Active
     * <li>Updated
     * <li>Key
     * <li>Value
     * <li>Description
     * <li>Parent
     * <li>Modified Date
     * <li>Modified Time
     * <li>Classname
     * </code>
     *
     * @return Vector
     */

       public Vector<Vector> getPropertyReport()
           {
           final Vector<Vector> vecReport;
           Vector<Object> vecRow;
           final Enumeration<RootPlugin> enumResources;

           vecReport = new Vector<Vector>(PROPERTY_COUNT);
           enumResources = getProperties().elements();

           while (enumResources.hasMoreElements())
               {
               final RootPlugin resource;

               resource = enumResources.nextElement();
               vecRow = new Vector<Object>(REPORT_COLUMNS);

               vecRow.add(ReportIcon.getIcon(resource.getIconFilename()));
               vecRow.add(((ResourcePlugin)resource).isInstalled());
               vecRow.add(resource.isUpdated());

               if (resource.getPathname() != null)
                   {
                   vecRow.add(resource.getPathname());
                   }
               else
                   {
                   vecRow.add(MSG_NULL);
                   }

               if (((ResourcePlugin)resource).getResource() != null)
                   {
                   vecRow.add(((ResourcePlugin)resource).getResource().toString());
                   }
               else
                   {
                   vecRow.add(MSG_NULL);
                   }

               if (resource.getDescription() != null)
                   {
                   vecRow.add(resource.getDescription());
                   }
               else
                   {
                   vecRow.add(MSG_NULL);
                   }

               vecRow.add(EMPTY_STRING);
               vecRow.add(ChronosHelper.toDateString(((ResourcePlugin)resource).getModifiedDate()));
               vecRow.add(ChronosHelper.toTimeString(((ResourcePlugin)resource).getModifiedTime()));
               vecRow.add(PropertyPlugin.class.getName());

               vecReport.add(vecRow);
               }

           // Sort the Properties by their pathnames (column 3)
           Collections.sort(vecReport, new ReportRowsByColumn(3));

           return (vecReport);
           }


    /**********************************************************************************************/
    /* Queries                                                                                    */
    /***********************************************************************************************
     * Get the QueryData for the specified Query ResourceKey.
     *
     *  @param key The Query ResourceKey, of the form "Level0.Level1.Level2.Level3"
     *
     *  @return QueryData
     */

    public QueryPlugin getQueryData(final String key)
         {
         if ((getQueries() != null)
             && (getQueries().containsKey(key))
             && (getQueries().get(key) instanceof QueryPlugin))
             {
             return (QueryPlugin)getQueries().get(key);
             }
         else
             {
             // Cannot get the requested QueryData...
             LOGGER.handleAtomException(getFramework(),
                                        getFramework().getRootTask(),
                                        Registry.class.getName(),
                                        new FrameworkException("[key=" + key + "]"),
                                        getException("Registry.Key.Property.Query") + " [key=" + key + "]",
                                        EventStatus.FATAL);
             }

         return (null);
         }


    /***********************************************************************************************
     * ToDo getAtomQuery.
     *
     * @param plugin
     * @param key
     *
     * @return QueryPlugin
     */

    public QueryPlugin getAtomQuery(final AtomPlugin plugin,
                                    final String key)
        {
        return null;
        }


    /***********************************************************************************************
     * List all of the loaded Queries.
     *
     * @param debugmode
     */

    public void showQueries(final boolean debugmode)
        {
        if (debugmode)
            {
            LOGGER.log("Loaded Queries");

            for (Enumeration<RootPlugin> enumeration = getQueries().elements();
                 enumeration.hasMoreElements();)
                {
                final RootPlugin resource = enumeration.nextElement();

                LOGGER.log(INDENT + "[" + resource.getResourceKey()
                                       + "=" + ((ResourcePlugin)resource).getResource() + "]");
                }
            }
        }


     /***********************************************************************************************
      * List the Queries as a Vector of {updated, key, value, datemodified}.
      *
      * The Vector contains a sub-Vector with the following elements:
      * <code>
      * <li>Icon
      * <li>Active
      * <li>Updated
      * <li>Key
      * <li>Value
      * <li>Description
      * <li>Parent
      * <li>Modified Date
      * <li>Modified Time
      * <li>Classname
      * </code>
      *
      * @return Vector
      */

      public Vector<Vector> getQueryReport()
          {
          final Vector<Vector> vecReport;
          Vector<Object> vecRow;
          final Enumeration enumResources;

          vecReport = new Vector<Vector>(REPORT_ROWS);
          enumResources = getQueries().elements();

          while (enumResources.hasMoreElements())
              {
              vecRow = new Vector<Object>(REPORT_COLUMNS);
              final ResourcePlugin resource = (ResourcePlugin)enumResources.nextElement();

              vecRow.add(ReportIcon.getIcon(resource.getIconFilename()));
              vecRow.add(resource.isInstalled());
              vecRow.add(resource.isUpdated());
              vecRow.add(resource.getPathname());
              vecRow.add(resource.getResource().toString());
              vecRow.add(resource.getDescription());
              vecRow.add(EMPTY_STRING);
              vecRow.add(ChronosHelper.toDateString(resource.getModifiedDate()));
              vecRow.add(ChronosHelper.toTimeString(resource.getModifiedTime()));
              vecRow.add(ResourcePlugin.class.getName());

              vecReport.add(vecRow);
              }

          // Sort the Queries by their pathnames (column 3)
          Collections.sort(vecReport, new ReportRowsByColumn(3));

          return (vecReport);
          }


    /**********************************************************************************************/
    /* Strings                                                                                    */
    /***********************************************************************************************
     * Get the StringData corresponding to the specified key.
     *
     * @param key The String ResourceKey, of the form "Level0.Level1.Level2.Level3"
     *
     * @return StringData
     */

    public StringPlugin getStringData(final String key)
        {
        if ((getStrings() != null)
            && (getStrings().containsKey(key))
            && (getStrings().get(key) instanceof StringPlugin))
            {
            return ((StringPlugin)getStrings().get(key));
            }
        else
            {
            LOGGER.handleAtomException(getFramework(),
                                       getFramework().getRootTask(),
                                       Registry.class.getName(),
                                       new FrameworkException("[Key=" + key + "]"),
                                       getException("Registry.Key.Property.String") + " [key=" + key + "]",
                                       EventStatus.FATAL);
            }

        return (null);
        }


    /***********************************************************************************************
     * Get the String corresponding to the specified key.
     *
     * @param key
     *
     * @return The String
     */

    public String getString(final String key)
        {
        if ((getStrings() != null)
            && (getStrings().containsKey(key))
            && (getStrings().get(key) instanceof ResourcePlugin))
            {
            return ((String)((ResourcePlugin)getStrings().get(key)).getResource());
            }
        else
            {
            System.out.println("String not found: key=" + key);
//            LOGGER.handleAtomException(getFramework(),
//                                       getFramework().getRootTask(),
//                                       Registry.class.getName(),
//                                       new FrameworkException("[Key=" + key + "]"),
//                                       getException("Registry.Key.Property.String") + " [key=" + key + "]",
//                                       EventStatus.FATAL);
            }

        return (EMPTY_STRING);
        }


    /***********************************************************************************************
     * ToDo getAtomString.
     *
     * @param plugin
     * @param key
     *
     * @return StringPlugin
     */

    public StringPlugin getAtomString(final AtomPlugin plugin,
                                      final String key)
        {
        return null;
        }


    /***********************************************************************************************
     *
     * @param debugmode
     */

    public void showStrings(final boolean debugmode)
        {
        if (debugmode)
            {
            LOGGER.log("Loaded Strings");

            for (Enumeration<RootPlugin> enumeration = getStrings().elements();
                 enumeration.hasMoreElements();)
                {
                final RootPlugin resource = enumeration.nextElement();

                LOGGER.log(INDENT + "[" + resource.getResourceKey()
                                       + "=" + ((ResourcePlugin)resource).getResource() + "]");
                }
            }
        }


    /***********************************************************************************************
     * List the Strings as a Vector of {updated, key, value, datemodified}.
     *
     * The Vector contains a sub-Vector with the following elements:
     * <code>
     * <li>Icon
     * <li>Active
     * <li>Updated
     * <li>Key
     * <li>Value
     * <li>Description
     * <li>Parent
     * <li>Modified Date
     * <li>Modified Time
     * <li>Classname
     * </code>
     *
     * @return Vector
     */

     public Vector<Vector> getStringReport()
         {
         final Vector<Vector> vecReport;
         Vector<Object> vecRow;
         final Enumeration enumResources;

         vecReport = new Vector<Vector>(REPORT_ROWS);
         enumResources = getStrings().elements();

         while (enumResources.hasMoreElements())
             {
             vecRow = new Vector<Object>(REPORT_COLUMNS);
             final StringPlugin stringPlugin = (StringPlugin)enumResources.nextElement();

             vecRow.add(ReportIcon.getIcon(StringPlugin.STRING_ICON));
             vecRow.add(stringPlugin.isInstalled());
             vecRow.add(stringPlugin.isUpdated());
             vecRow.add(stringPlugin.getPathname());
             vecRow.add(stringPlugin.getResource().toString());
             vecRow.add(stringPlugin.getDescription());
             vecRow.add(EMPTY_STRING);
             vecRow.add(ChronosHelper.toDateString(stringPlugin.getModifiedDate()));
             vecRow.add(ChronosHelper.toTimeString(stringPlugin.getModifiedTime()));
             vecRow.add(Registry.class.getName());

             vecReport.add(vecRow);
             }

         // Sort the Strings by their pathnames (column 3)
         Collections.sort(vecReport, new ReportRowsByColumn(3));

         return (vecReport);
         }


    /**********************************************************************************************/
    /* Exceptions                                                                                 */
    /***********************************************************************************************
     * Get the ExceptionData corresponding to the specified key
     *
     * Handles ExceptionLibrary.INVALID_EXCEPTION_KEY
     *
     * @param key
     *
     * @return ExceptionData
     */

    public ExceptionPlugin getExceptionData(final String key)
        {
        if ((getExceptions() != null)
            && (getExceptions().containsKey(key))
            && (getExceptions().get(key) instanceof ExceptionPlugin))
            {
            return ((ExceptionPlugin)getExceptions().get(key));
            }
        else
            {
            LOGGER.handleAtomException(getFramework(),
                                       getFramework().getRootTask(),
                                       Registry.class.getName(),
                                       new FrameworkException("[Key=" + key + "]"),
                                       getException("Registry.Key.Property.Exception") + " [key=" + key + "]",
                                       EventStatus.FATAL);
            }

        return (null);
        }


    /***********************************************************************************************
    * Get the Exception corresponding to the specified key.
    *
    * @param key
    *
    * @return The Exception message
    */

    public String getException(final String key)
       {
       if ((getExceptions() != null)
           && (getExceptions().containsKey(key))
           && (getExceptions().get(key) instanceof ResourcePlugin))
           {
           return ((String)((ResourcePlugin)getExceptions().get(key)).getResource());
           }
       else
           {
           System.out.println("Registry.getException() ERROR! Exception key=" + key);
//           LOGGER.handleAtomException(getFramework(),
//                                      getFramework().getRootTask(),
//                                      Registry.class.getName(),
//                                      new FrameworkException("[Key=" + key + "]"),
//                                      getException("Registry.Key.Property.Exception") + " [key=" + key + "]",
//                                      EventStatus.FATAL);
           }

       return (key);
       }


    /***********************************************************************************************
     * Get the Atom Exception corresponding to the specified key.
     * The AtomPlugin is passed in to avoid hardcoding the name of the
     * Atom when creating the ResourceKey.
     *
     * @param plugin
     * @param key
     *
     * @return ExceptionPlugin
     */

    public ExceptionPlugin getAtomException(final AtomPlugin plugin,
                                            final String key)
        {
        String strKey;

        strKey = "";

        if (plugin != null)
            {
            strKey = plugin.getPathname()
                    + RegistryModelPlugin.DELIMITER_RESOURCE
                    + key;

            if ((getExceptions() != null)
                && (getExceptions().containsKey(strKey))
                && (getExceptions().get(key) instanceof ResourcePlugin))
                {
                return (((ExceptionPlugin)getExceptions().get(strKey)));
                }
            }

        LOGGER.handleAtomException(getFramework(),
                                   getFramework().getRootTask(),
                                   Registry.class.getName(),
                                   new FrameworkException("[Key=" + strKey + "]"),
                                   getException("Registry.Key.Property.Exception") + " [key=" + key + "]",
                                   EventStatus.FATAL);
        return (null);
        }


    /***********************************************************************************************
     *
     * @param debugmode
     */

    public void showExceptions(final boolean debugmode)
        {
        if (debugmode)
            {
            LOGGER.log("Loaded Exceptions");

            for (Enumeration<RootPlugin> enumeration = getExceptions().elements();
                 enumeration.hasMoreElements();)
                {
                final RootPlugin resource = enumeration.nextElement();

                LOGGER.log(INDENT + "[" + resource.getResourceKey()
                            + "=" + ((ResourcePlugin)resource).getResource() + "]");
                }
            }
        }


    /***********************************************************************************************
     * List the Exceptions as a Vector of {updated, key, value, datemodified}.
     *
     * The Vector contains a sub-Vector with the following elements:
     * <code>
     * <li>Icon
     * <li>Active
     * <li>Updated
     * <li>Key
     * <li>Value
     * <li>Description
     * <li>Parent
     * <li>Modified Date
     * <li>Modified Time
     * <li>Classname
     * </code>
     *
     * @return Vector
     */

     public Vector<Vector> getExceptionReport()
         {
         final Vector<Vector> vecReport;
         Vector<Object> vecRow;
         final Enumeration enumResources;

         vecReport = new Vector<Vector>(REPORT_ROWS);
         enumResources = getExceptions().elements();

         while (enumResources.hasMoreElements())
             {
             vecRow = new Vector<Object>(REPORT_COLUMNS);
             final ExceptionPlugin exceptionPlugin = (ExceptionPlugin)enumResources.nextElement();

             vecRow.add(ReportIcon.getIcon(ExceptionPlugin.EXCEPTION_ICON));
             vecRow.add(exceptionPlugin.isInstalled());
             vecRow.add(exceptionPlugin.isUpdated());
             vecRow.add(exceptionPlugin.getPathname());
             vecRow.add(exceptionPlugin.getResource().toString());
             vecRow.add(exceptionPlugin.getDescription());
             vecRow.add(EMPTY_STRING);
             vecRow.add(ChronosHelper.toDateString(exceptionPlugin.getModifiedDate()));
             vecRow.add(ChronosHelper.toTimeString(exceptionPlugin.getModifiedTime()));
             vecRow.add(Registry.class.getName());

             vecReport.add(vecRow);
             }

         // Sort the Exceptions by their pathnames (column 3)
         Collections.sort(vecReport, new ReportRowsByColumn(3));

         return (vecReport);
         }


    /***********************************************************************************************
     * Get the UserPlugin corresponding to the specified key.
     *
     * @param key
     *
     * @return UserPlugin
     */

    public UserPlugin getUser(final String key)
        {
        if ((getUsers() != null)
            && (getUsers().containsKey(key)))
            {
            return (getUsers().get(key));
            }
        else
            {
            LOGGER.handleAtomException(getFramework(),
                                       getFramework().getRootTask(),
                                       Registry.class.getName(),
                                       new FrameworkException("[Key=" + key + "]"),
                                       getException("Registry.Key.User") + " [key=" + key + "]",
                                       EventStatus.FATAL);
            }

        return (null);
        }


    /***********************************************************************************************
     * List all of the loaded Users.
     *
     * @param debugmode
     */

    public void showUsers(final boolean debugmode)
        {
        if ((debugmode)
            && (getUsers() != null))
            {
            LOGGER.log("\nLoaded Users");

            for (Enumeration<UserPlugin> enumeration = getUsers().elements();
                 enumeration.hasMoreElements();)
                {
                final UserPlugin user = enumeration.nextElement();

                LOGGER.log(INDENT + "[" + user.getName() + "]");
                }
            }
        }


    /***********************************************************************************************
     * ToDo USER report.
     *
     * @return Vector<Vector>
     */

    public Vector<Vector> getUserReport()
        {
        return null;
        }


    /***********************************************************************************************
     * Get the RolePlugin corresponding to the specified key.
     *
     * @param key
     *
     * @return RolePlugin
     */

    public RolePlugin getRole(final String key)
        {
        if ((getRoles() != null)
            && (getRoles().containsKey(key)))
            {
            return (getRoles().get(key));
            }
        else
            {
            LOGGER.handleAtomException(getFramework(),
                                       getFramework().getRootTask(),
                                       Registry.class.getName(),
                                       new FrameworkException("[Key=" + key + "]"),
                                       getException("Registry.Key.Role") + " [key=" + key + "]",
                                       EventStatus.FATAL);
            }

        return (null);
        }


    /***********************************************************************************************
     * List all of the loaded Roles.
     *
     * @param debugmode
     */

    public void showRoles(final boolean debugmode)
        {
        if ((debugmode)
            && (getRoles() != null))
            {
            LOGGER.log("\nLoaded Roles");

            for (Enumeration<RolePlugin> enumeration = getRoles().elements();
                 enumeration.hasMoreElements();)
                {
                final RolePlugin role = enumeration.nextElement();

                LOGGER.log(INDENT + "[" + role.getName() + "]");
                }
            }
        }


    /***********************************************************************************************
     * ToDo ROLE report.
     *
     * @return Vector<Vector>
     */

    public Vector<Vector> getRoleReport()
        {
        return new Vector<Vector>(1);
        }


    /***********************************************************************************************
     * Get the CountryPlugin corresponding to the specified key.
     *
     * @param key
     *
     * @return CountryPlugin
     */

    public CountryPlugin getCountry(final String key)
        {
        if ((getCountries() != null)
            && (getCountries().containsKey(key)))
            {
            return (getCountries().get(key));
            }
        else
            {
            LOGGER.handleAtomException(getFramework(),
                                       getFramework().getRootTask(),
                                       Registry.class.getName(),
                                       new FrameworkException("[Key=" + key + "]"),
                                       getException("Registry.Key.Country") + " [key=" + key + "]",
                                       EventStatus.FATAL);
            }

        return (null);
        }


    /***********************************************************************************************
     * List all of the loaded Countries.
     *
     * @param debugmode
     */

    public void showCountries(final boolean debugmode)
        {
        if ((debugmode)
            && (getCountries() != null))
            {
            // Remember that the Framework is not yet loaded..
            LOGGER.log("\nLoaded Countries");

            for (Enumeration<CountryPlugin> enumeration = getCountries().elements();
                 enumeration.hasMoreElements();)
                {
                final CountryPlugin country = enumeration.nextElement();

                LOGGER.log(INDENT + "[" + country.getIOCCountryNameLower() + "]");
                }
            }
        }


    /***********************************************************************************************
     * List the Countries as a Vector.
     *
     * The Vector contains a sub-Vector with the following elements:
     * <code>
     * <li>Icon
     * <li>Active
     * <li>Updated
     * <li>Key
     * <li>Value
     * <li>Description
     * <li>Parent
     * <li>Modified Date
     * <li>Modified Time
     * <li>Classname
     * </code>
     *
     * @return Vector
     */

     public Vector<Vector> getCountriesReport()
         {
         final Vector<Vector> vecReport;
         Vector<Object> vecRow;
         final Enumeration enumResources;

         vecReport = new Vector<Vector>(INITIAL_CAPACITY);
         enumResources = getCountries().elements();

         while ((enumResources != null)
             && (enumResources.hasMoreElements()))
             {
             vecRow = new Vector<Object>(10);
             final CountryPlugin countryPlugin = (CountryPlugin)enumResources.nextElement();

             if (countryPlugin != null)
                 {
                 vecRow.add(RegistryModelUtilities.getNationalFlagIcon(countryPlugin.getISOCode2()));
                 vecRow.add(countryPlugin.isInstalled());
                 vecRow.add(false);
                 vecRow.add(ReportTableHelper.greyCell(countryPlugin.getISOCode2(),
                                                       !countryPlugin.isInstalled()));
                 vecRow.add(ReportTableHelper.greyCell(countryPlugin.getISOCountryName(),
                                                       !countryPlugin.isInstalled()));
                 vecRow.add("");
                 vecRow.add("");
                 vecRow.add("");
                 vecRow.add("");
                 vecRow.add(CountryData.class.getName());

                 vecReport.add(vecRow);
                 }
             }

         // Sort the Countries by their ISO2 Codes (column 3)
         Collections.sort(vecReport, new ReportRowsByColumn(3));

         return (vecReport);
         }


    /***********************************************************************************************
     * Get the LanguagePlugin corresponding to the specified key.
     *
     * @param key
     *
     * @return LanguagePlugin
     */

    public LanguagePlugin getLanguage(final String key)
        {
        if ((getLanguages() != null)
            && (getLanguages().containsKey(key)))
            {
            return (getLanguages().get(key));
            }
        else
            {
            LOGGER.handleAtomException(getFramework(),
                                       getFramework().getRootTask(),
                                       Registry.class.getName(),
                                       new FrameworkException("[Key=" + key + "]"),
                                       getException("Registry.Key.Language") + " [key=" + key + "]",
                                       EventStatus.FATAL);
            }

        return (null);
        }


    /***********************************************************************************************
     * List all of the loaded Languages.
     *
     * @param debugmode
     */

    public void showLanguages(final boolean debugmode)
        {
        if ((debugmode)
            && (getLanguages() != null))
            {
            // Remember that the Framework is not yet loaded...
            LOGGER.log("\nLoaded Languages");

            for (Enumeration<LanguagePlugin> enumeration = getLanguages().elements();
                 enumeration.hasMoreElements();)
                {
                final LanguagePlugin language = enumeration.nextElement();

                LOGGER.log(INDENT + "[" + language.getName() + "]");
                }
            }
        }


    /***********************************************************************************************
     * List the Languages as a Vector of {Flag, ISO2, ISO3, Name}.
     *
     * The Vector contains a sub-Vector with the following elements:
     * <code>
     * <li>Icon
     * <li>Active
     * <li>Updated
     * <li>Key
     * <li>Value
     * <li>Description
     * <li>Parent
     * <li>Modified Date
     * <li>Modified Time
     * <li>Classname
     * </code>
     *
     * @return Vector<Vector>
     */

    public Vector<Vector> getLanguagesReport()
        {
        final Vector<Vector> vecReport;
        Vector<Object> vecRow;
        final Enumeration enumResources;

        vecReport = new Vector<Vector>(INITIAL_CAPACITY);
        enumResources = getLanguages().elements();

        while ((enumResources != null)
            && (enumResources.hasMoreElements()))
            {
            vecRow = new Vector<Object>(10);
            final LanguagePlugin resourceData = (LanguagePlugin)enumResources.nextElement();

            if (resourceData != null)
                {
                vecRow.add(RegistryModelUtilities.getLanguageFlagIcon(resourceData.getISOCode2()));
                vecRow.add(resourceData.isInstalled());
                vecRow.add(false);
                vecRow.add(ReportTableHelper.greyCell(resourceData.getISOCode2(),
                                                      !resourceData.isInstalled()));
                vecRow.add(ReportTableHelper.greyCell(resourceData.getName(),
                                                      !resourceData.isInstalled()));
                vecRow.add("");
                vecRow.add("");
                vecRow.add("");
                vecRow.add("");
                vecRow.add(LanguageData.class.getName());

                vecReport.add(vecRow);
                }
            }

        // Sort the Languages by their ISO2 Codes (column 3)
        Collections.sort(vecReport, new ReportRowsByColumn(3));

        return (vecReport);
        }


   /***********************************************************************************************
    * Get the DataTypePlugin corresponding to the specified key.
    *
    * @param key
    *
    * @return DataTypePlugin
    */

//   public DataTypeParserInterface getDataType(final String key)
//       {
//       if ((getDataTypes() != null)
//           && (getDataTypes().containsKey(key)))
//           {
//           return (getDataTypes().get(key));
//           }
//       else
//           {
//           LOGGER.handleAtomException(getFramework(),
//                                      getFramework().getRootTask(),
//                                      Registry.class.getName(),
//                                      new FrameworkException("[Key=" + key + "]"),
//                                      getException("Registry.Key.DataType") + " [key=" + key + "]",
//                                      EventStatus.FATAL);
//           }
//
//       return (null);
//       }


    /***********************************************************************************************
     * List all of the loaded DataTypes.
     *
     * @param debugmode
     */

//    public void showDataTypes(final boolean debugmode)
//        {
//        if ((debugmode)
//            && (getDataTypes() != null))
//            {
//            LOGGER.log("\nLoaded DataTypes");
//
//            for (Enumeration<DataTypeParserInterface> enumeration = getDataTypes().elements();
//                 enumeration.hasMoreElements();)
//                {
//                final DataTypeParserInterface datatype = enumeration.nextElement();
//
//                LOGGER.log(INDENT + "[" + datatype.getName() + "]");
//                }
//            }
//        }

//         iterateResources = iterateDataTypes();
//
//         while ((iterateResources != null)
//             && (iterateResources.hasNext()))
//             {
//             vecRow = new Vector<Object>();
//
//             vecRow.add(ReportIcon.getIcon(DataType.DATATYPE_ICON));
//             vecRow.add(new Boolean(true));
//             vecRow.add(new Boolean(false));
//             vecRow.add(iterateResources.next());
//             vecRow.add("");
//             vecRow.add("");
//             vecRow.add("");
//             vecRow.add("");
//             vecRow.add("");
//             vecRow.add(DataType.class.getName());
//
//             vecReport.add(vecRow);
//             }



    /***********************************************************************************************
     * ToDo DATATYPES report.
     *
     * @return Vector<Vector>
     */

    public Vector<Vector> getDataTypesReport()
        {
        return new Vector<Vector>(1);
        }


   /***********************************************************************************************
    * Get the LookAndFeelPlugin corresponding to the specified key.
    *
    * @param key
    *
    * @return LookAndFeelPlugin
    */

   public LookAndFeelPlugin getLookAndFeel(final String key)
       {
       if ((getLookAndFeels() != null)
           && (getLookAndFeels().containsKey(key)))
           {
           return (getLookAndFeels().get(key));
           }
       else
           {
           LOGGER.handleAtomException(getFramework(),
                                      getFramework().getRootTask(),
                                      Registry.class.getName(),
                                      new FrameworkException("[Key=" + key + "]"),
                                      getException("Registry.Key.LookAndFeel") + " [key=" + key + "]",
                                      EventStatus.FATAL);
           }

       return (null);
       }


    /***********************************************************************************************
     * List all of the loaded LookAndFeels.
     *
     * @param debugmode
     */

    public void showLookAndFeels(final boolean debugmode)
        {
        if ((debugmode)
            && (getLookAndFeels() != null))
            {
            LOGGER.log("\nLoaded LookAndFeels");

            for (Enumeration<LookAndFeelPlugin> enumeration = getLookAndFeels().elements();
                 enumeration.hasMoreElements();)
                {
                final LookAndFeelPlugin lookandfeel = enumeration.nextElement();

                LOGGER.log(INDENT + "[" + lookandfeel.toString() + "]");
                }
            }
        }


    /***********************************************************************************************
     * ToDo LOOKANDFEELS report.
     *
     * @return Vector<Vector>
     */

    public Vector<Vector> getLookAndFeelsReport()
        {
        return new Vector<Vector>(1);
        }


    /***********************************************************************************************
     * Add an Action to the ActionList.
     *
     * @param actiondata
     */

    public void addActionToList(final ActionDataInterface actiondata)
        {
        if ((actiondata != null)
            && (this.vecActionList != null))
            {
            this.vecActionList.add(actiondata);
            }
        }


    /***********************************************************************************************
     * Add an Action to the ActionList, prepared from the raw data.
     *
     * @param userplugin
     * @param dateraised
     * @param timeraised
     * @param action
     * @param status
     */

    public void addActionToList(final UserPlugin userplugin,
                                final Date dateraised,
                                final Time timeraised,
                                final String action,
                                final ActionStatus status)
        {
        if ((userplugin != null)
            && (dateraised != null)
            && (timeraised != null)
            && (action != null)
            && (!EMPTY_STRING.equals(action))
            && (status != null)
            && (this.vecActionList != null))
            {
            this.vecActionList.add(new ActionData(userplugin,
                                                  Chronos.getCalendarDateNow(),
                                                  Chronos.getCalendarTimeNow(),
                                                  action,
                                                  status));
            }
        }


    /***********************************************************************************************
     * Put an EmailMessage in the EmailOutbox.
     *
     * @param emaildata
     */

    public void addEmailToOutbox(final EmailMessageInterface emaildata)
        {
        if ((emaildata != null)
            && (this.vecEmailOutbox != null))
            {
            this.vecEmailOutbox.add(emaildata);
            }
        }


    /***********************************************************************************************
     * Put an EmailMessage in the EmailOutbox, prepared from the raw data.
     *
     * @param userdata
     * @param dateraised
     * @param timeraised
     * @param message
     */

    public void addEmailToOutbox(final UserPlugin userdata,
                                 final Date dateraised,
                                 final Time timeraised,
                                 final String message)
        {
        if ((userdata != null)
            && (dateraised != null)
            && (timeraised != null)
            && (message != null)
            && (!EMPTY_STRING.equals(message))
            && (this.vecEmailOutbox != null))
            {
            // ToDo Construct the Email!
            this.vecEmailOutbox.add(new EmailMessageData());
            }
        }


    /**********************************************************************************************/
    /* Miscellaneous                                                                              */
    /***********************************************************************************************
     * List the SystemProperties.
     *
     * The Vector contains a sub-Vector with the following elements:
     * <code>
     * <li>Icon
     * <li>Active
     * <li>Updated
     * <li>Key
     * <li>Value
     * <li>Description
     * <li>Parent
     * <li>Modified Date
     * <li>Modified Time
     * <li>Classname
     * </code>
     *
     * @return Vector
     */

     public Vector<Vector> getSystemReport()
         {
         final Vector<Vector> vecReport;
         Vector<Object> vecRow;
         final Enumeration<?> enumResources;
         String strKey;

         vecReport = new Vector<Vector>(System.getProperties().size());
         enumResources = System.getProperties().propertyNames();

         while (enumResources.hasMoreElements())
             {
             vecRow = new Vector<Object>(REPORT_COLUMNS);
             strKey = (String)enumResources.nextElement();

             vecRow.add(ReportIcon.getIcon(PropertyPlugin.PROPERTY_ICON));
             vecRow.add(true);
             vecRow.add(false);
             vecRow.add(strKey);
             vecRow.add(System.getProperty(strKey));
             vecRow.add(EMPTY_STRING);
             vecRow.add(EMPTY_STRING);
             vecRow.add(EMPTY_STRING);
             vecRow.add(EMPTY_STRING);
             vecRow.add(Properties.class.getName());

             vecReport.add(vecRow);
             }

         // Sort the Properties by their pathnames (column 3)
         Collections.sort(vecReport, new ReportRowsByColumn(3));

         return (vecReport);
         }


    /***********************************************************************************************
     * Get a single BlankUIComponent to cut down on the number of instances...
     *
     * @return UIComponentPlugin
     */

    public UIComponentPlugin getBlankUIComponent()
        {
        if (this.uiBlank == null)
            {
            this.uiBlank = new BlankUIComponent();
            }

        return (this.uiBlank);
        }


    /***********************************************************************************************
     * Validate the Registry bean pool.
     *
     * @return boolean
     */

    public boolean validateBeanPool()
        {
        boolean boolValid;

        boolValid = true;

        boolValid = validatePlugins(getAtoms(), boolValid);
        boolValid = validatePlugins(getTasks(), boolValid);
        boolValid = validatePlugins(getProperties(), boolValid);
        boolValid = validatePlugins(getQueries(), boolValid);
        boolValid = validatePlugins(getStrings(), boolValid);
        boolValid = validatePlugins(getExceptions(), boolValid);
        boolValid = validatePlugins(getUsers(), boolValid);
        boolValid = validatePlugins(getRoles(), boolValid);
        boolValid = validatePlugins(getCountries(), boolValid);
        boolValid = validatePlugins(getLanguages(), boolValid);
//        boolValid = validatePlugins(getDataTypes(), boolValid);
        boolValid = validatePlugins(getLookAndFeels(), boolValid);

        try
            {
            getFramework();
            getFramework().getRootTask();
            }

        catch (FrameworkException exception)
            {
            LOGGER.login(MSG_VALIDATION_FAILURE + SPACE + "Framework");
            boolValid = false;
            }

        return (boolValid);
        }


    /***********************************************************************************************
     * Get the number of Beans currently in the Registry (excluding the EventLog).
     *
     * @return long
     */

    public long size()
        {
        long longSize;

        longSize = 0;
        longSize += getAtoms().size();
        longSize += getTasks().size();
        longSize += getProperties().size();
        longSize += getQueries().size();
        longSize += getStrings().size();
        longSize += getExceptions().size();
        longSize += getUsers().size();
        longSize += getRoles().size();
        longSize += getCountries().size();
        longSize += getLanguages().size();
//        longSize += getDataTypes().size();
        longSize += getLookAndFeels().size();

        return (longSize);
        }
    }
