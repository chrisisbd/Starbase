package org.lmn.fc.model.lookandfeels;

import org.lmn.fc.model.root.RootPlugin;


/***************************************************************************************************
 * The LookAndFeelPlugin.
 */

public interface LookAndFeelPlugin extends RootPlugin
    {
    // LookAndFeel table column names
    String LOOKANDFEEL_ID                  = "LookAndFeelID";
    String LOOKANDFEEL_INSTALLED           = "LookAndFeelInstalled";
    String LOOKANDFEEL_NAME                = "LookAndFeelName";
    String LOOKANDFEEL_CLASSNAME           = "LookAndFeelClassname";
    // Queries
    String QUERY_SELECT_LOOKANDFEELS_ALL   = "Select.LookAndFeel.All";
    String ICON_LOOKANDFEEL                = "/toolbarButtonGraphics/general/Preferences16.gif";

    boolean isInstalled();

    void setInstalled(boolean flag);

    String getClassName();

    void setClassName(String classname);

    String getLicenceFilename();

    void setLicenceFilename(String filename);
    }
