package org.lmn.fc.model.tasks;

import org.lmn.fc.ui.manager.FrameworkManagerUIComponentPlugin;


public interface FrameworkManagerPlugin extends TaskPlugin
    {
    FrameworkManagerUIComponentPlugin getUI();
    }
