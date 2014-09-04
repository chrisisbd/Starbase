package org.lmn.fc.model.tasks;

import org.lmn.fc.common.events.ActiveChangeListener;
import org.lmn.fc.ui.manager.UserInterfaceUIComponentPlugin;


public interface UserInterfacePlugin extends TaskPlugin,
                                             ActiveChangeListener
    {
    UserInterfaceUIComponentPlugin getUI();
    }
