package org.lmn.fc.model.users;

import org.lmn.fc.model.root.RootPlugin;


/***************************************************************************************************
 * The RolePlugin.
 */

public interface RolePlugin extends RootPlugin
    {
    boolean isFrameworkViewer();

    void setFrameworkViewer(boolean flag);

    boolean isFrameworkEditor();

    void setFrameworkEditor(boolean flag);

    boolean isAtomViewer();

    void setAtomViewer(boolean flag);

    boolean isAtomEditor();

    void setAtomEditor(boolean flag);

    boolean isTaskViewer();

    void setTaskViewer(boolean flag);

    boolean isTaskEditor();

    void setTaskEditor(boolean flag);

    boolean isResourceViewer();

    void setResourceViewer(boolean flag);

    boolean isResourceEditor();

    void setResourceEditor(boolean flag);

    boolean isUserViewer();

    void setUserViewer(boolean flag);

    boolean isUserEditor();

    void setUserEditor(boolean flag);

    boolean isUserCreator();

    void setUserCreator(boolean flag);

    boolean isUserDeletor();

    void setUserDeletor(boolean flag);

    boolean isFullScreen();

    void setFullScreen(boolean flag);
    }
