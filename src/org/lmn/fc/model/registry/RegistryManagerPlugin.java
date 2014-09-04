package org.lmn.fc.model.registry;

import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.common.constants.ResourceKeys;
import org.lmn.fc.common.exceptions.FrameworkException;
import org.lmn.fc.common.loaders.LoaderProperties;
import org.lmn.fc.database.impl.FrameworkDatabase;
import org.lmn.fc.model.dao.DataStore;
import org.lmn.fc.model.logging.Logger;
import org.lmn.fc.model.plugins.AtomPlugin;
import org.lmn.fc.model.registry.impl.Registry;
import org.lmn.fc.model.registry.impl.RegistryModel;
import org.lmn.fc.model.resources.QueryPlugin;
import org.lmn.fc.model.users.UserPlugin;

import java.util.Vector;


/***************************************************************************************************
 * The RegistryManagerPlugin.
 */

public interface RegistryManagerPlugin extends FrameworkConstants,
                                               FrameworkStrings,
                                               FrameworkMetadata,
                                               ResourceKeys
    {
    FrameworkDatabase DATABASE = FrameworkDatabase.getInstance();
    RegistryPlugin REGISTRY = Registry.getInstance();
    RegistryModelPlugin REGISTRY_MODEL = RegistryModel.getInstance();
    Logger LOGGER = Logger.getInstance();

    // Indexes into the SQLTrace Vector
    int TRACE_ITEMS = 6;
    int INDEX_TRACE_QUERY_DATA = 0;
    int INDEX_TRACE_DATE = 1;
    int INDEX_TRACE_TIME = 2;
    int INDEX_TRACE_EXEC_COUNT = 3;
    int INDEX_TRACE_EXEC_TIME = 4;
    int INDEX_TRACE_HOST_CLASS = 5;

    //----------------------------------------------------------------------------------------------
    // Bean Management

    boolean registerBeans(LoaderProperties properties,
                          DataStore store);

    boolean validateBeanPool();

    boolean assembleBeansForUser(UserPlugin user);

    void updateRegistry();

    void importTasks(AtomPlugin host,
                     DataStore store,
                     String folder,
                     boolean debug) throws FrameworkException;

    void importResources(AtomPlugin host,
                         DataStore store,
                         String folder, String language,
                         boolean debug) throws FrameworkException;

    //----------------------------------------------------------------------------------------------
    // Bean monitoring

    Vector getQueryTrace();

    void clearQueryTrace();

    void traceQuery(QueryPlugin query,
                    Object objhost);
    }
