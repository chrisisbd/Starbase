package org.lmn.fc.model.registry.impl;

import org.lmn.fc.common.exceptions.FrameworkException;
import org.lmn.fc.common.loaders.LoaderProperties;
import org.lmn.fc.common.utilities.time.Chronos;
import org.lmn.fc.model.dao.*;
import org.lmn.fc.model.dao.mysql.RolesMySqlDAO;
import org.lmn.fc.model.dao.xml.*;
import org.lmn.fc.model.logging.EventLogDAOInterface;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.logging.impl.EventLogHsqldbDAO;
import org.lmn.fc.model.logging.impl.EventLogMySqlDAO;
import org.lmn.fc.model.logging.impl.EventLogXmlDAO;
import org.lmn.fc.model.plugins.AtomPlugin;
import org.lmn.fc.model.registry.InstallationFolder;
import org.lmn.fc.model.registry.RegistryManagerPlugin;
import org.lmn.fc.model.resources.QueryPlugin;
import org.lmn.fc.model.users.UserPlugin;

import java.util.Vector;


/***************************************************************************************************
 * The RegistryManager.
 */

public final class RegistryManager implements RegistryManagerPlugin
    {
    private static final BeanAssembler BEAN_ASSEMBLER = BeanAssembler.getInstance();

    private volatile static RegistryManager MANAGER_INSTANCE;

    private static final Vector<Object> vecSqlTrace = new Vector<Object>(100);


    /***********************************************************************************************
     * The RegistryManager is a Singleton!
     *
     * @return RegistryManager
     */

    public static RegistryManager getInstance()
        {
        if (MANAGER_INSTANCE == null)
            {
            synchronized (RegistryManager.class)
                {
                if (MANAGER_INSTANCE == null)
                    {
                    MANAGER_INSTANCE = new RegistryManager();
                    }
                }
            }

        return (MANAGER_INSTANCE);
        }


     /***********************************************************************************************
      * Privately construct the RegistryManager.
      */

     private RegistryManager()
         {
         clearQueryTrace();
         }


    /**********************************************************************************************/
    /* Bean Management                                                                            */
    /***********************************************************************************************
     * Register all available beans.
     *
     * @param properties
     * @param store
     *
     * @return boolean
     */

    public boolean registerBeans(final LoaderProperties properties,
                                 final DataStore store)
        {
        final String folder;
        boolean boolSuccess;

        //LOGGER.debugNavigation("RegistryManager.registerBeans() START");

        if ((properties == null)
            || (properties.getDatabaseOptions() == null)
            || (store == null)
            || (!store.isAvailable()))
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_NULL);
            }

        try
            {
            // The initial folder is defined by the DataStore
            folder = store.getLoadFolder();
            boolSuccess = true;

            //--------------------------------------------------------------------------------------
            // Basic Framework

            // Import the Countries from the DataStore
            importCountries(store,
                            folder,
                            properties.isMasterDebug());

            // Import the Languages from the DataStore
            importLanguages(store,
                            folder,
                            properties.isMasterDebug());

            // Import the UserRoles from the DataStore
            importRoles(store,
                        folder,
                        properties.isMasterDebug());

            // Import the Users from the DataStore
            // This needs to know about Countries, Languages and Roles
            importUsers(store,
                        folder,
                        properties.isMasterDebug());

            // Import the DataTypes from the DataStore
            // 2011-11-20 DataTypes are now handled differently
//            importDataTypes(store,
//                            folder,
//                            properties.isMasterDebug());

            // Import the LookAndFeels from the DataStore
            importLookAndFeels(store,
                               folder,
                               properties.isMasterDebug());

            // Import the EventLog from the DataStore
            // The EventLog is never imported from DataStore.CONFIG
            importEventLog(store,
                           properties.isMasterDebug());

            //--------------------------------------------------------------------------------------
            // Framework Instance

            // Import the Framework from the DataStore
            importFramework(store,
                            folder,
                            properties.isMasterDebug());

            // The Registry now contains a validated Framework
            // Set the debug level from here on...
            REGISTRY.getFramework().setDebugMode(properties.isMasterDebug());

            // Install the Framework Tasks from the DataStore
            importTasks(REGISTRY.getFramework(),
                        store,
                        folder,
                        properties.isMasterDebug());

            // Install the Framework Resources from the DataStore,
            // in the appropriate Language for the Framework instance
            importResources(REGISTRY.getFramework(),
                            store,
                            folder,
                            REGISTRY.getFramework().getLanguageISOCode(),
                            properties.isMasterDebug());

            //--------------------------------------------------------------------------------------
            // Framework Plugins

            // Install the Plugins from the DataStore
            // All Resources must be available in the Language specified in the host Framework
            importPlugins(REGISTRY.getFramework(),
                          store,
                          folder,
                          REGISTRY.getFramework().getLanguageISOCode(),
                          properties.isMasterDebug());
            }

        catch (FrameworkException exception)
            {
            LOGGER.login("RegistryManager.registerBeans() FrameworkException [" + exception.getMessage() + "]");
            boolSuccess = false;
            }

        if (boolSuccess)
            {
            LOGGER.login("Beans registered correctly");
            }

        return (boolSuccess);
        }


    /***********************************************************************************************
     * Validate the Bean Pool before we go any further...
     *
     * @return boolean
     */

    public boolean validateBeanPool()
        {
        final boolean boolSuccess;

        LOGGER.login("Validating bean pool");
        boolSuccess = REGISTRY.validateBeanPool();

        if (boolSuccess)
            {
            LOGGER.login(REGISTRY.size() + SPACE + "beans in Registry validated successfully");
            }

        return (boolSuccess);
        }


    /***********************************************************************************************
     * Assemble a set of Beans appropriate to the Role of the specified User.
     *
     * @return boolean
     */

    public boolean assembleBeansForUser(final UserPlugin user)
        {
        final boolean boolSuccess;

        if ((REGISTRY.getFramework() == null)
            || (!REGISTRY.getFramework().validatePlugin())
            || (user == null)
            || (!user.validatePlugin()))
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_INVALID);
            }

        boolSuccess = BEAN_ASSEMBLER.assembleBeansForUser(user);

        if (boolSuccess)
            {
            LOGGER.logAtomEvent(REGISTRY.getFramework(),
                                REGISTRY.getFramework().getRootTask(),
                                REGISTRY.getFramework().getClass().getName(),
                                METADATA_MODEL_ASSEMBLY
                                + METADATA_COUNT
                                + REGISTRY_MODEL.size()
                                + TERMINATOR
                                + SPACE
                                + METADATA_USER
                                + user.getName()
                                + TERMINATOR,
                                EventStatus.INFO);
            }

        return (boolSuccess);
        }


    /**********************************************************************************************/
    /* Imports and Exports                                                                        */
    /***********************************************************************************************
     * Import the User Roles from the specified DataStore.
     *
     * @param store
     * @param folder
     * @param debug
     *
     * @throws FrameworkException
     */

    private void importRoles(final DataStore store,
                             final String folder,
                             final boolean debug) throws FrameworkException
        {
        final RolesDAOInterface dao;

        LOGGER.login("Importing Roles from [" + store + "]");

        if (DataStore.CONFIG.equals(store))
            {
            dao = new RolesXmlDAO(folder, debug);
            dao.importRoles();
            }
        else if (DataStore.XML.equals(store))
            {
            dao = new RolesXmlDAO(folder, debug);
            dao.importRoles();
            }
        else if (DataStore.MYSQL.equals(store))
            {
            dao = new RolesMySqlDAO(folder, debug);
            dao.importRoles();
            }
        else if (DataStore.HSQLDB.equals(store))
            {
            // ToDo DataStore
            }
        else
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_INVALID);
            }
        }


    /***********************************************************************************************
     * Export the User Roles to the specified DataStore.
     *
     * @param store
     * @param debug
     *
     * @throws FrameworkException
     */

    private void exportRoles(final DataStore store,
                             final boolean debug) throws FrameworkException
        {
        LOGGER.login("Exporting User Roles to [" + store + "]");

        if (DataStore.CONFIG.equals(store))
            {
            // ToDo DataStore
            }
        else if (DataStore.XML.equals(store))
            {
            // ToDo DataStore
            }
        else if (DataStore.MYSQL.equals(store))
            {
            // ToDo DataStore
            }
        else if (DataStore.HSQLDB.equals(store))
            {
            // ToDo DataStore
            }
        else
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_INVALID);
            }
        }


    /***********************************************************************************************
     * Import the Users from the specified DataStore.
     *
     * @param store
     * @param folder
     * @param debug
     *
     * @throws FrameworkException
     */

    private void importUsers(final DataStore store,
                             final String folder,
                             final boolean debug) throws FrameworkException
        {
        final UsersDAOInterface dao;

        LOGGER.login("Importing Users from [" + store + "]");

        if (DataStore.CONFIG.equals(store))
            {
            dao = new UsersXmlDAO(folder,
                                  debug);
            dao.importUsers();
            }
        else if (DataStore.XML.equals(store))
            {
            dao = new UsersXmlDAO(folder,
                                  debug);
            dao.importUsers();
            }
        else if (DataStore.MYSQL.equals(store))
            {
            // ToDo DataStore
            }
        else if (DataStore.HSQLDB.equals(store))
            {
            // ToDo DataStore
            }
        else
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_INVALID);
            }
        }


    /***********************************************************************************************
     * Export the Users to the specified DataStore.
     *
     * @param store
     * @param debug
     *
     * @throws FrameworkException
     */

    private void exportUsers(final DataStore store,
                             final boolean debug) throws FrameworkException
        {
        final UsersDAOInterface dao;

        LOGGER.login("Exporting Users to [" + store + "]");

        if (DataStore.CONFIG.equals(store))
            {
            dao = new UsersXmlDAO(InstallationFolder.EXPORTS.getName(),
                                  debug);
            dao.exportUsers();
            }
        else if (DataStore.XML.equals(store))
            {
            dao = new UsersXmlDAO(InstallationFolder.EXPORTS.getName(),
                                  debug);
            dao.exportUsers();
            }
        else if (DataStore.MYSQL.equals(store))
            {
            // ToDo DataStore
            }
        else if (DataStore.HSQLDB.equals(store))
            {
            // ToDo DataStore
            }
        else
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_INVALID);
            }
        }


    /***********************************************************************************************
     * Import the Countries from the specified DataStore.
     *
     * @param store
     * @param folder
     * @param debug
     *
     * @throws FrameworkException
     */

    private void importCountries(final DataStore store,
                                 final String folder,
                                 final boolean debug) throws FrameworkException
        {
        final CountriesDAOInterface dao;

        LOGGER.login("Importing Countries from [" + store + "]");

        if (DataStore.CONFIG.equals(store))
            {
            dao = new CountriesXmlDAO(folder, debug);
            dao.importCountries();
            }
        else if (DataStore.XML.equals(store))
            {
            dao = new CountriesXmlDAO(folder, debug);
            dao.importCountries();
            }
        else if (DataStore.MYSQL.equals(store))
            {
            // ToDo DataStore
            }
        else if (DataStore.HSQLDB.equals(store))
            {
            // ToDo DataStore
            }
        else
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_INVALID);
            }
        }


    /***********************************************************************************************
     * Export the Countries to the specified DataStore.
     *
     * @param store
     * @param debug
     *
     * @throws FrameworkException
     */

    private void exportCountries(final DataStore store,
                                 final boolean debug) throws FrameworkException
        {
        LOGGER.login("Exporting Countries to [" + store + "]");

        if (DataStore.CONFIG.equals(store))
            {
            // ToDo DataStore
            }
        else if (DataStore.XML.equals(store))
            {
            // ToDo DataStore
            }
        else if (DataStore.MYSQL.equals(store))
            {
            // ToDo DataStore
            }
        else if (DataStore.HSQLDB.equals(store))
            {
            // ToDo DataStore
            }
        else
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_INVALID);
            }
        }


    /***********************************************************************************************
     * Import the Languages from the specified DataStore.
     *
     * @param store
     * @param folder
     * @param debug
     *
     * @throws FrameworkException
     */

    private void importLanguages(final DataStore store,
                                 final String folder,
                                 final boolean debug) throws FrameworkException
        {
        final LanguagesDAOInterface dao;

        LOGGER.login("Importing Languages from [" + store + "]");

        if (DataStore.CONFIG.equals(store))
            {
            dao = new LanguagesXmlDAO(folder,
                                      debug);
            dao.importLanguages();
            }
        else if (DataStore.XML.equals(store))
            {
            dao = new LanguagesXmlDAO(folder,
                                      debug);
            dao.importLanguages();
            }
        else if (DataStore.MYSQL.equals(store))
            {
            // ToDo DataStore
            }
        else if (DataStore.HSQLDB.equals(store))
            {
            // ToDo DataStore
            }
        else
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_INVALID);
            }
        }


    /***********************************************************************************************
     * Export the Languages from the specified DataStore.
     *
     * @param store
     * @param debug
     *
     * @throws FrameworkException
     */

    private void exportLanguages(final DataStore store,
                                 final boolean debug) throws FrameworkException
        {
        LOGGER.login("Exporting Langauges to [" + store + "]");

        if (DataStore.CONFIG.equals(store))
            {
            // ToDo DataStore
            }
        else if (DataStore.XML.equals(store))
            {
            // ToDo DataStore
            }
        else if (DataStore.MYSQL.equals(store))
            {
            // ToDo DataStore
            }
        else if (DataStore.HSQLDB.equals(store))
            {
            // ToDo DataStore
            }
        else
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_INVALID);
            }
        }


    /***********************************************************************************************
     * Import the DataTypes from the specified DataStore.
     *
     * @param store
     * @param folder
     * @param debug
     *
     * @throws FrameworkException
     */

    private void importDataTypes(final DataStore store,
                                 final String folder,
                                 final boolean debug) throws FrameworkException
        {
        final DataTypesDAOInterface dao;

        LOGGER.login("Importing DataTypes from [" + store + "]");

        if (DataStore.CONFIG.equals(store))
            {
//            dao = new DataTypesXmlDAO(folder,
//                                      debug);
//            dao.importDataTypes();
            }
        else if (DataStore.XML.equals(store))
            {
//            dao = new DataTypesXmlDAO(folder,
//                                      debug);
//            dao.importDataTypes();
            }
        else if (DataStore.MYSQL.equals(store))
            {
            // ToDo DataStore
            }
        else if (DataStore.HSQLDB.equals(store))
            {
            // ToDo DataStore
            }
        else
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_INVALID);
            }
        }


    /***********************************************************************************************
     * Export the DataTypes from the specified DataStore.
     *
     * @param store
     * @param debug
     *
     * @throws FrameworkException
     */

    private void exportDataTypes(final DataStore store,
                                 final boolean debug) throws FrameworkException
        {
        LOGGER.login("Exporting DataTypes to [" + store + "]");

        if (DataStore.CONFIG.equals(store))
            {
            // ToDo DataStore
            }
        else if (DataStore.XML.equals(store))
            {
            // ToDo DataStore
            }
        else if (DataStore.MYSQL.equals(store))
            {
            // ToDo DataStore
            }
        else if (DataStore.HSQLDB.equals(store))
            {
            // ToDo DataStore
            }
        else
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_INVALID);
            }
        }


    /***********************************************************************************************
     * Import the LookAndFeels from the specified DataStore.
     *
     * @param store
     * @param folder
     * @param debug
     *
     * @throws FrameworkException
     */

    private void importLookAndFeels(final DataStore store,
                                    final String folder,
                                    final boolean debug) throws FrameworkException
        {
        final LookAndFeelsDAOInterface dao;

        LOGGER.login("Importing LookAndFeels from [" + store + "]");

        if (DataStore.CONFIG.equals(store))
            {
            dao = new LookAndFeelsXmlDAO(folder,
                                         debug);
            dao.importLookAndFeels();
            }
        else if (DataStore.XML.equals(store))
            {
            dao = new LookAndFeelsXmlDAO(folder,
                                         debug);
            dao.importLookAndFeels();
            }
        else if (DataStore.MYSQL.equals(store))
            {
            // ToDo DataStore
            }
        else if (DataStore.HSQLDB.equals(store))
            {
            // ToDo DataStore
            }
        else
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_INVALID);
            }
        }


    /***********************************************************************************************
     * Export the LookAndFeels from the specified DataStore.
     *
     * @param store
     * @param debug
     *
     * @throws FrameworkException
     */

    private void exportLookAndFeels(final DataStore store,
                                    final boolean debug) throws FrameworkException
        {
        LOGGER.login("Exporting LookAndFeels to [" + store + "]");

        if (DataStore.CONFIG.equals(store))
            {
            // ToDo DataStore
            }
        else if (DataStore.XML.equals(store))
            {
            // ToDo DataStore
            }
        else if (DataStore.MYSQL.equals(store))
            {
            // ToDo DataStore
            }
        else if (DataStore.HSQLDB.equals(store))
            {
            // ToDo DataStore
            }
        else
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_INVALID);
            }
        }


    /**********************************************************************************************/
    /* Basic Framework                                                                            */
    /***********************************************************************************************
     * Import the EventLog from the specified DataStore.
     *
     * @param store
     * @param debug
     */

    private void importEventLog(final DataStore store,
                                final boolean debug)
        {
        final EventLogDAOInterface dao;

        LOGGER.login("Importing EventLog from [" + store + "]");

        if (DataStore.CONFIG.equals(store))
            {
            // Do nothing, the EventLog is never imported from the initial configuration
            }
        else if (DataStore.XML.equals(store))
            {
            dao = new EventLogXmlDAO(debug);
            dao.loadEventLog();
            }
        else if (DataStore.MYSQL.equals(store))
            {
            dao = new EventLogMySqlDAO();
            dao.loadEventLog();
            }
        else if (DataStore.HSQLDB.equals(store))
            {
            dao = new EventLogHsqldbDAO();
            dao.loadEventLog();
            }
        else
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_INVALID);
            }
        }


    /***********************************************************************************************
     * Export the EventLog to the specified DataStore.
     *
     * @param store
     * @param debug
     */

    private void exportEventLog(final DataStore store,
                                final boolean debug)
        {
        final EventLogDAOInterface dao;

        LOGGER.login("Exporting EventLog to [" + store + "]");

        if (DataStore.CONFIG.equals(store))
            {
            dao = new EventLogXmlDAO(debug);
            dao.saveEventLog();
            }
        else if (DataStore.XML.equals(store))
            {
            dao = new EventLogXmlDAO(debug);
            dao.saveEventLog();
            }
        else if (DataStore.MYSQL.equals(store))
            {
            dao = new EventLogMySqlDAO();
            dao.saveEventLog();
            }
        else if (DataStore.HSQLDB.equals(store))
            {
            dao = new EventLogHsqldbDAO();
            dao.saveEventLog();
            }
        else
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_INVALID);
            }
        }


    /***********************************************************************************************
     * Import the Framework from the specified DataStore.
     *
     * @param store
     * @param folder
     * @param debug
     *
     * @throws FrameworkException
     */

    private void importFramework(final DataStore store,
                                 final String folder,
                                 final boolean debug) throws FrameworkException
        {
        final FrameworkDAOInterface dao;

        LOGGER.login("Importing Framework from [" + store + "]");

        if (DataStore.CONFIG.equals(store))
            {
            dao = new FrameworkXmlDAO(folder,
                                      debug);
            dao.importFramework();
            }
        else if (DataStore.XML.equals(store))
            {
            dao = new FrameworkXmlDAO(folder,
                                      debug);
            dao.importFramework();
            }
        else if (DataStore.MYSQL.equals(store))
            {
            // ToDo DataStore
            }
        else if (DataStore.HSQLDB.equals(store))
            {
            // ToDo DataStore
            }
        else
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_INVALID);
            }
        }


    /***********************************************************************************************
     * Export the Framework to the specified DataStore.
     *
     * @param store
     * @param debug
     *
     * @throws FrameworkException
     */

    private void exportFramework(final DataStore store,
                                 final boolean debug) throws FrameworkException
        {
        final FrameworkDAOInterface dao;

        LOGGER.login("Exporting Framework to [" + store + "]");

        if (DataStore.CONFIG.equals(store))
            {
            dao = new FrameworkXmlDAO(InstallationFolder.EXPORTS.getName(),
                                      debug);
            dao.exportFramework();
            }
        else if (DataStore.XML.equals(store))
            {
            dao = new FrameworkXmlDAO(InstallationFolder.EXPORTS.getName(),
                                      debug);
            dao.exportFramework();
            }
        else if (DataStore.MYSQL.equals(store))
            {
            // ToDo DataStore
            }
        else if (DataStore.HSQLDB.equals(store))
            {
            // ToDo DataStore
            }
        else
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_INVALID);
            }
        }


    /***********************************************************************************************
     * Import the Plugins from the specified DataStore.
     *
     * @param host
     * @param store
     * @param debug
     *
     * @throws FrameworkException
     */

    private void importPlugins(final AtomPlugin host,
                               final DataStore store,
                               final String folder,
                               final String language,
                               final boolean debug) throws FrameworkException
        {
        final PluginsDAOInterface dao;

        // Check the parameters...
        if ((host == null)
            || (host.getLevel() == null)
            || (host.getName() == null)
            || (store == null)
            || (language == null)
            || (language.length() != 2))
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_INVALID);
            }

        LOGGER.login("Importing " + host.getName() + " Plugins from [" + store + "]");

        if (DataStore.CONFIG.equals(store))
            {
            dao = new PluginsXmlDAO(debug);

            // This method is used recursively, so supply the parameters here
            dao.importPlugins(host,
                              folder,
                              language);
            }
        else if (DataStore.XML.equals(store))
            {
            dao = new PluginsXmlDAO(debug);

            // This method is used recursively, so supply the parameters here
            dao.importPlugins(host,
                              folder,
                              language);
            }
        else if (DataStore.MYSQL.equals(store))
            {
            // ToDo DataStore
            }
        else if (DataStore.HSQLDB.equals(store))
            {
            // ToDo DataStore
            }
        else
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_INVALID);
            }
        }


    /***********************************************************************************************
     * Export the Atom child Atoms (i.e. Plugins) to the specified DataStore.
     *
     * @param host
     * @param store
     * @param debug
     *
     * @throws FrameworkException
     */

    private void exportPlugins(final AtomPlugin host,
                               final DataStore store,
                               final boolean debug) throws FrameworkException
        {
        final PluginsDAOInterface dao;

        // Check the parameters...
        if ((host == null)
            || (host.getLevel() == null)
            || (host.getName() == null)
            || (store == null))
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_INVALID);
            }

        LOGGER.login("\nExporting " + host.getName() + " Plugins to [" + store + "]");

        if (DataStore.CONFIG.equals(store))
            {
            // ToDo DataStore
            }
        else if (DataStore.XML.equals(store))
            {
            // ToDo DataStore
            }
        else if (DataStore.MYSQL.equals(store))
            {
            // ToDo DataStore
            }
        else if (DataStore.HSQLDB.equals(store))
            {
            // ToDo DataStore
            }
        else
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_INVALID);
            }
        }


    /***********************************************************************************************
     * Import the Atom Tasks from the specified DataStore.
     *
     * @param host
     * @param store
     * @param folder
     * @param debug
     *
     * @throws FrameworkException
     */

    public void importTasks(final AtomPlugin host,
                            final DataStore store,
                            final String folder,
                            final boolean debug) throws FrameworkException
        {
        final TasksDAOInterface dao;

        // Check the parameters...
        if ((host == null)
            || (host.getLevel() == null)
            || (host.getName() == null)
            || (folder == null)
            || (EMPTY_STRING.equals(folder))
            || (store == null))
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_INVALID);
            }

        LOGGER.login("Importing " + host.getName() + " Tasks from [" + store + "]");

        if (DataStore.CONFIG.equals(store))
            {
            dao = new TasksXmlDAO(host,
                                  folder,
                                  debug);
            dao.importTasks();
            }
        else if (DataStore.XML.equals(store))
            {
            dao = new TasksXmlDAO(host,
                                  folder,
                                  debug);
            dao.importTasks();
            }
        else if (DataStore.MYSQL.equals(store))
            {
            // ToDo DataStore
            }
        else if (DataStore.HSQLDB.equals(store))
            {
            // ToDo DataStore
            }
        else
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_INVALID);
            }
        }


    /***********************************************************************************************
     * Export the Atom Tasks to the specified DataStore.
     *
     * @param host
     * @param store
     * @param debug
     *
     * @throws FrameworkException
     */

    private void exportTasks(final AtomPlugin host,
                             final DataStore store,
                             final boolean debug) throws FrameworkException
        {
        final TasksDAOInterface dao;

        // Check the parameters...
        if ((host == null)
            || (host.getLevel() == null)
            || (host.getName() == null)
            || (store == null))
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_INVALID);
            }

        LOGGER.login("\nExporting " + host.getName() + " Tasks to [" + store + "]");

        if (DataStore.CONFIG.equals(store))
            {
            dao = new TasksXmlDAO(host,
                                  InstallationFolder.EXPORTS.getName(),
                                  debug);
            dao.exportTasks();
            }
        else if (DataStore.XML.equals(store))
            {
            dao = new TasksXmlDAO(host,
                                  InstallationFolder.EXPORTS.getName(),
                                  debug);
            dao.exportTasks();
            }
        else if (DataStore.MYSQL.equals(store))
            {
            // ToDo DataStore
            }
        else if (DataStore.HSQLDB.equals(store))
            {
            // ToDo DataStore
            }
        else
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_INVALID);
            }
        }


    /***********************************************************************************************
     * Import the Properties, Queries, Strings and Exceptions for this host Atom
     * from the XML file in the specified folder.
     * Where applicable, load Resources in the specified language.
     *
     * @param host
     * @param store
     * @param folder
     * @param language
     * @param debug
     *
     * @throws FrameworkException
     */

    public void importResources(final AtomPlugin host,
                                final DataStore store,
                                final String folder,
                                final String language,
                                final boolean debug) throws FrameworkException
        {
        ResourcesDAOInterface dao;

        // Check the parameters...
        if ((host == null)
            || (host.getLevel() == null)
            || (host.getName() == null)
            || (store == null)
            || (language == null)
            || (language.length() != 2))
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_INVALID);
            }

        LOGGER.login("Importing " + host.getName() + " Resources from [" + store + "] in language [" + language + "]");

        // Import Resources and attach them to the embryonic Atom
        // Also make a reference in the global Resource hashtable
        if (DataStore.CONFIG.equals(store))
            {
            dao = new PropertiesXmlDAO(host,
                                       folder,
                                       language,
                                       debug);
            dao.importResources();

            dao = new StringsXmlDAO(host,
                                    folder,
                                    language,
                                    debug);
            dao.importResources();

            dao = new ExceptionsXmlDAO(host,
                                       folder,
                                       language,
                                       debug);
            dao.importResources();

            dao = new QueriesXmlDAO(host,
                                    folder,
                                    language,
                                    debug);
            dao.importResources();
            }
        else if (DataStore.XML.equals(store))
            {
            dao = new PropertiesXmlDAO(host,
                                       folder,
                                       language,
                                       debug);
            dao.importResources();

            dao = new StringsXmlDAO(host,
                                    folder,
                                    language,
                                    debug);
            dao.importResources();

            dao = new ExceptionsXmlDAO(host,
                                       folder,
                                       language,
                                       debug);
            dao.importResources();

            dao = new QueriesXmlDAO(host,
                                    folder,
                                    language,
                                    debug);
            dao.importResources();
            }
        else if (DataStore.MYSQL.equals(store))
            {
            // ToDo DataStore
            }
        else if (DataStore.HSQLDB.equals(store))
            {
            // ToDo DataStore
            }
        else
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_INVALID);
            }
        }


    /***********************************************************************************************
     * Export the Properties, Queries, Strings and Exceptions for this host Atom
     * to the XML file in the specified folder.
     * Where applicable, save Resources in the specified language.
     *
     * @param host
     * @param store
     * @param language
     * @param debug
     *
     * @throws FrameworkException
     */

    private void exportResources(final AtomPlugin host,
                                 final DataStore store,
                                 final String language,
                                 final boolean debug) throws FrameworkException
        {
        ResourcesDAOInterface dao;

        // Check the parameters...
        if ((host == null)
            || (host.getLevel() == null)
            || (host.getName() == null)
            || (store == null)
            || (language == null)
            || (language.length() != 2))
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_INVALID);
            }

        LOGGER.login("\nExporting " + host.getName() + " Resources to [" + store + "] in language [" + language + "]");

        if (DataStore.CONFIG.equals(store))
            {
            dao = new PropertiesXmlDAO(host,
                                       InstallationFolder.EXPORTS.getName(),
                                       language,
                                       debug);
            dao.exportResources();

            dao = new StringsXmlDAO(host,
                                    InstallationFolder.EXPORTS.getName(),
                                    language,
                                    debug);
            dao.exportResources();

            dao = new ExceptionsXmlDAO(host,
                                       InstallationFolder.EXPORTS.getName(),
                                       language,
                                       debug);
            dao.exportResources();

            dao = new QueriesXmlDAO(host,
                                    InstallationFolder.EXPORTS.getName(),
                                    language,
                                    debug);
            dao.exportResources();
            }
        else if (DataStore.XML.equals(store))
            {
            dao = new PropertiesXmlDAO(host,
                                       InstallationFolder.EXPORTS.getName(),
                                       language,
                                       debug);
            dao.exportResources();

            dao = new StringsXmlDAO(host,
                                    InstallationFolder.EXPORTS.getName(),
                                    language,
                                    debug);
            dao.exportResources();

            dao = new ExceptionsXmlDAO(host,
                                       InstallationFolder.EXPORTS.getName(),
                                       language,
                                       debug);
            dao.exportResources();

            dao = new QueriesXmlDAO(host,
                                    InstallationFolder.EXPORTS.getName(),
                                    language,
                                    debug);
            dao.exportResources();
            }
        else if (DataStore.MYSQL.equals(store))
            {
            // ToDo DataStore
            }
        else if (DataStore.HSQLDB.equals(store))
            {
            // ToDo DataStore
            }
        else
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_INVALID);
            }
        }


    /**********************************************************************************************/
    /* Registry Updates                                                                           */
    /***********************************************************************************************
     * See if any object in the Registry should be written to the database.
     */

    public synchronized final void updateRegistry()
        {
        LOGGER.logTimedEvent("[target=registry] [action=updating]");

        updateAtoms();
        updateTasks();
        updateProperties();
        updateQueries();
        updateStrings();
        updateExceptions();
        updateRoles();
        updateCountries();
        updateLanguages();
        updateDataTypes();
        updateLookAndFeels();

        //setUpdated(false);
        }


    /***********************************************************************************************
     * See if any Atoms have changed, and so need to be written to the database.
     */

    private void updateAtoms()
        {
        // ToDo updateAtoms()
//        ApplicationData applicationData;
//        ComponentData componentData;
//        final Iterator<RootPlugin> iterApplications;
//        Iterator iterComponents;
//
//        if (REGISTRY.getFramework() != null)
//            {
//            // Update the Framework
//            if (REGISTRY.getFramework().isUpdated())
//                {
//                FrameworkMySqlDAO.getInstance().updateData();
//                showDebugMessage(DOT + REGISTRY.getFramework().getName() + SPACE + "updated");
//                }
//
//            // Update the FrameworkTasks
//            REGISTRY.getFramework().updateTasks();
//
//            // Update all Applications and ApplicationTasks
//            if (REGISTRY.getFramework().getAtomsIterator() != null)
//                {
//                iterApplications = REGISTRY.getFramework().getAtomsIterator();
//
//                while ((iterApplications != null)
//                    && (iterApplications.hasNext()))
//                    {
//                    applicationData = (ApplicationData) iterApplications.next();
//
//                    if (applicationData != null)
//                        {
//                        if (applicationData.isUpdated())
//                            {
//                            applicationData.updateData();
//                            showDebugMessage(DOT + applicationData.getName() + SPACE + "updated");
//                            }
//
//                        // Update the ApplicationTasks
//                        applicationData.updateApplicationTasks();
//
//                        // Update all Components and ComponentTasks
//                        if (applicationData.getComponents() != null)
//                            {
//                            iterComponents = applicationData.getComponents();
//
//                            while ((iterComponents != null)
//                                && (iterComponents.hasNext()))
//                                {
//                                componentData = (ComponentData) iterComponents.next();
//
//                                if (componentData != null)
//                                    {
//                                    if (componentData.isUpdated())
//                                        {
//                                        componentData.updateData();
//                                        showDebugMessage(DOT + componentData.getName() + SPACE + "updated");
//                                        }
//
//                                    // Update the ComponentTasks
//                                    componentData.updateComponentTasks();
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }
        }


    /***********************************************************************************************
     * See if any Tasks have changed, and so need to be written to the database.
     */

    private void updateTasks()
        {
        // ToDo updateTasks()
        }


    /***********************************************************************************************
     * See if any Properties have changed, and so need to be written to the database.
     */

    private void updateProperties()
        {
//        RootPlugin property;
//        final Enumeration<RootPlugin> enumProperties;
//
//        enumProperties = getProperties().elements();
//
//        while ((enumProperties != null)
//            && (enumProperties.hasMoreElements()))
//            {
//            property = enumProperties.nextElement();
//
//            if ((property != null)
//                && (property.isUpdated()))
//                {
//                // ToDo ????? DAO
//                //property.writeResource();
//                //showStaticDebugMessage("." + property.getPathname() + " updated");
//                }
//            }
        }


    /***********************************************************************************************
     * See if any Queries have changed, and so need to be written to the database.
     */

    private void updateQueries()
        {
//        QueryData queryData;
//        final Enumeration enumQueries;
//
//        enumQueries = getQueries().elements();
//
//        while ((enumQueries != null)
//            && (enumQueries.hasMoreElements()))
//            {
//            queryData = (QueryData)enumQueries.nextElement();
//
//            if ((queryData != null)
//                && (queryData.isUpdated()))
//                {
//                queryData.writeResource();
//                // logger showStaticDebugMessage("." + queryData.getPathname() + " updated");
//                }
//            }
        }


    /***********************************************************************************************
     * See if any Strings have changed, and so need to be written to the database.
     */

    private void updateStrings()
        {
//        StringData stringData;
//        final Enumeration enumStrings;
//
//        enumStrings = getStrings().elements();
//
//        while ((enumStrings != null)
//            && (enumStrings.hasMoreElements()))
//            {
//            stringData = (StringData)enumStrings.nextElement();
//
//            if ((stringData != null)
//                && (stringData.isUpdated()))
//                {
//                // DAO ?? stringData.writeResource();
//                // logger StringData.showStaticDebugMessage("." + stringData.getPathname() + " updated");
//                }
//            }
        }

    /***********************************************************************************************
     * See if any Exceptions have changed, and so need to be written to the database.
     */

    private void updateExceptions()
        {
//        ExceptionData exceptionData;
//        final Enumeration enumExceptions;
//
//        enumExceptions = getExceptions().elements();
//
//        while ((enumExceptions != null)
//            && (enumExceptions.hasMoreElements()))
//            {
//            exceptionData = (ExceptionData)enumExceptions.nextElement();
//
//            if ((exceptionData != null)
//                && (exceptionData.isUpdated()))
//                {
//                // Todo exceptionData.writeResource();
//                // logger ExceptionData.showStaticDebugMessage("." + exceptionData.getPathname() + " updated");
//                }
//            }
        }

    /***********************************************************************************************
     * See if any Roles have changed, and so need to be written to the database.
     */

    private void updateRoles()
        {
        // ToDo updateRoles()
        }


    /***********************************************************************************************
     * See if any Countries have changed, and so need to be written to the database.
     */

    private void updateCountries()
        {
        // ToDo updateCountries()
//        CountryData countryData;
//        Enumeration enumCountries;
//
//        enumCountries = hashtableCountries.elements();
//
//        while ((enumCountries != null)
//            && (enumCountries.hasMoreElements()))
//            {
//            countryData = (CountryData)enumCountries.nextElement();
//
//            if ((countryData != null)
//                && (countryData.isUpdated()))
//                {
//                countryData.writeResource();
//                showStaticDebugMessage("." + countryData.getPathname() + " updated");
//                }
//            }
        }


    /***********************************************************************************************
     * See if any Languages have changed, and so need to be written to the database.
     */

    private void updateLanguages()
        {
        // ToDo updateLanguages()
        }


    /***********************************************************************************************
     * See if any DataTypes have changed, and so need to be written to the database.
     */

    private void updateDataTypes()
        {
        // ToDo updateDataTypes()
        }


    /***********************************************************************************************
     * See if any LookAndFeels have changed, and so need to be written to the database.
     */

    private void updateLookAndFeels()
        {
        // ToDo updateLookAndFeels()
        }


    /**********************************************************************************************/
    /* Bean Monitoring                                                                            */
    /***********************************************************************************************
     * Get the Query Trace List.
     *
     * @return Vector<Object>
     */

    public final Vector<Object> getQueryTrace()
        {
        return (vecSqlTrace);
        }


    /***********************************************************************************************
     * Initialise the Query Trace List.
     * This should be called once only, every time the program is run!
     */

    public final void clearQueryTrace()
        {
        // TODO The User doesn't need to know this for now!
        //LOGGER.login("Clearing SQL Trace history");
        getQueryTrace().clear();
        }


    /***********************************************************************************************
     * Add the specified QueryPlugin to the QueryTrace list.
     * Record the class which executed the Query.
     *
     * @param query
     * @param objhost
     */

    public final void traceQuery(final QueryPlugin query,
                                 final Object objhost)
        {
        final Vector<Object> vecTraceItem;

        vecTraceItem = new Vector<Object>(TRACE_ITEMS);

        vecTraceItem.add(query);
        vecTraceItem.add(Chronos.getCalendarTimeNow());
        vecTraceItem.add(Chronos.getCalendarTimeNow());

        // Take a snapshot of the ExecutionCount and ExecutionTime
        vecTraceItem.add(Long.toString(query.getExecutionCount()));
        vecTraceItem.add(Long.toString(query.getExecutionTime()));

        // Record the class which executed the statement
        vecTraceItem.add(objhost);

        getQueryTrace().add(vecTraceItem);
        }
    }
