package org.lmn.fc.model.registry;

import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.model.plugins.AtomPlugin;
import org.lmn.fc.model.plugins.FrameworkPlugin;
import org.lmn.fc.model.plugins.PluginState;
import org.lmn.fc.model.root.RootPlugin;
import org.lmn.fc.model.tasks.FrameworkManagerPlugin;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.model.tasks.TaskState;
import org.lmn.fc.ui.login.LoginDialog;

import java.util.Vector;


/***************************************************************************************************
 * The RegistryModelControllerInterface.
 */

public interface RegistryModelControllerInterface extends FrameworkConstants,
                                                          FrameworkStrings,
                                                          FrameworkMetadata
    {
    // Framework
    void loginFramework(FrameworkPlugin framework);

    void logoutFramework(FrameworkPlugin framework);

    void stopFramework(FrameworkPlugin framework);

    void exitFramework(FrameworkPlugin framework);

    // Plugins
    void startAllChildPlugins(AtomPlugin host);

    boolean startPluginAndShowUI(AtomPlugin plugin,
                                 FrameworkManagerPlugin manager);

    boolean setPluginState(AtomPlugin plugin,
                           PluginState newstate);

    boolean isPluginActivationChainOk(AtomPlugin userobject);

    void unableToControlPlugin(AtomPlugin plugin);

    // Tasks
    void runAllTasks(Vector<RootPlugin> tasks);

    void runTaskAndShowUI(TaskPlugin task,
                          FrameworkManagerPlugin manager);

    boolean setTaskState(AtomPlugin host,
                         TaskPlugin plugin,
                         TaskState newstate);

    boolean isTaskActivationChainOk(TaskPlugin userobject);

    void unableToControlTask(TaskPlugin plugin);

    // Login Utilities
    void showLoginDialog(FrameworkPlugin plugin);

    void disposeLoginDialog();

    LoginDialog getLoginDialog();

    void setLoginDialog(LoginDialog dialog);
    }
