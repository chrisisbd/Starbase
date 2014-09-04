package org.lmn.fc.model.registry;

import org.lmn.fc.common.actions.ContextAction;
import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.common.constants.ResourceKeys;
import org.lmn.fc.common.events.ActiveChangeListener;
import org.lmn.fc.model.plugins.AtomPlugin;
import org.lmn.fc.model.plugins.FrameworkPlugin;
import org.lmn.fc.model.root.RootPlugin;
import org.lmn.fc.model.root.UserObjectPlugin;
import org.lmn.fc.model.tasks.FrameworkManagerPlugin;
import org.lmn.fc.model.tasks.UserInterfacePlugin;
import org.lmn.fc.model.users.UserPlugin;
import org.lmn.fc.ui.UIComponentPlugin;
import org.lmn.fc.ui.manager.FrameworkManagerUIComponentPlugin;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.util.Stack;


/***************************************************************************************************
 * RegistryModelPlugin.
 */

public interface RegistryModelPlugin extends FrameworkConstants,
                                             FrameworkStrings,
                                             FrameworkMetadata,
                                             ResourceKeys
    {
    // ToDo Replace with enum?
    String PACKAGE_ROOT                         = "org.lmn.fc.";
    String PACKAGE_FRAMEWORKS                   = "frameworks.";
    String PACKAGE_PLUGINS                      = "plugins.";
    String PACKAGE_TASKS                        = "tasks.";
    String PACKAGE_SNMP                         = "common.net.snmp.";

    // ToDo Review use of these folders
    // Special case for Observatory.Instruments
    String FOLDER_INSTRUMENTS                   = "instruments";
    String FOLDER_DATABASE_MYSQL                = "database/mysql";
    String FOLDER_DATASTORE_XML                 = "datastore/xml";
    String FOLDER_DATASTORE_MYSQL               = "datastore/mysql";
    String FOLDER_DATASTORE_HSQLDB              = "datastore/hsqldb";

    String TERMINATOR_RESOURCE                  = "~";          // FrameworkDatabase entry
    String DELIMITER_RESOURCE                   = ".";
    String DELIMITER_PATH                       = "/";
    String DELIMITER_PACKAGE                    = ".";          // org.lmn.fc.frameworks. etc.

    int ICON_WIDTH = 32;              // Icon size for the tree view
    int ICON_HEIGHT = 32;

    Dimension DIM_TOOLBAR_SIZE = new Dimension(Integer.MAX_VALUE, 25);


    //----------------------------------------------------------------------------------------------
    // RegistryModel

    void initialiseModel(AtomPlugin atom);

    DefaultMutableTreeNode getRootNode();

    FrameworkPlugin getFramework();

    Stack<RootPlugin> getRunners();

    void addRunner(RootPlugin runner);

    void removeRunner(RootPlugin runner);

    //----------------------------------------------------------------------------------------------
    // ContextActions, Help and Navigation for the current User

    void rebuildNavigation(UserObjectPlugin userobject, UIComponentPlugin uicomponent);

    JMenuBar getMenuBar();

    JToolBar getToolBar();

    void addHelpAction(final ContextAction action);

    void removeHelpAction(final ContextAction action);

    void clearHelpActions();

    //----------------------------------------------------------------------------------------------
    // UserInterface

    UserInterfacePlugin getUserInterface();

    void setUserInterface(UserInterfacePlugin plugin);

    FrameworkManagerPlugin getFrameworkManager();

    void setFrameworkManager(FrameworkManagerPlugin plugin);

    // Todo review this
    FrameworkManagerUIComponentPlugin getFrameworkManagerUI();

    //----------------------------------------------------------------------------------------------
    // Users

    UserPlugin getLoggedInUser();

    void setLoggedInUser(UserPlugin user);

    //----------------------------------------------------------------------------------------------
    // Miscellaneous

    boolean getUpdated();

    void setUpdated(boolean flag);

    boolean getSqlTrace();

    void setSqlTrace(boolean flag);

    boolean getSqlTiming();

    void setSqlTiming(boolean flag);

    boolean getDebugMode();

    void setDebugMode(boolean flag);

    long size();

    void incSize();

    void addActiveChangeListener(ActiveChangeListener listener);
    }
