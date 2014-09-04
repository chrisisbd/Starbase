package org.lmn.fc.model.root;


import org.apache.xmlbeans.XmlObject;
import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.common.constants.ResourceKeys;
import org.lmn.fc.ui.UIComponentPlugin;

import javax.swing.tree.DefaultMutableTreeNode;
import java.io.Serializable;


public interface RootPlugin extends FrameworkConstants,
                                    FrameworkStrings,
                                    FrameworkMetadata,
                                    ResourceKeys,
                                    Serializable
    {
    int DESCRIPTION_LENGTH = 255;

    long getID();

    void setID(long id);

    String getLevel();

    void setLevel(String type);

    RootType getType();

    void setType(RootType type);

    /***********************************************************************************************
     * Get the full pathname for the Resource.
     * This must be implemented by the subclasses.
     *
     * @return String
     */

    String getPathname();


    /***********************************************************************************************
     * Get the ResourceKey.
     *
     * @return String
     */

    String getResourceKey();

    DefaultMutableTreeNode getHostTreeNode();

    void setHostTreeNode(DefaultMutableTreeNode node);

    XmlObject getXml();

    void setXml(XmlObject xml);

    short getSortOrder();

    void setSortOrder(short sortorder);

    /***********************************************************************************************
     * Get the Editable field.
     *
     * @return boolean
     */

    boolean isEditable();


    /***********************************************************************************************
     * Set the Editable field.
     *
     * @param editable
     */
    void setEditable(boolean editable);


    /***********************************************************************************************
     * Get the class name of the ResourceEditor.
     *
     * @return String
     */

    String getEditorClassname();


    /***********************************************************************************************
     * Set the class name of the ResourceEditor.
     *
     * @param classname
     */

    void setEditorClassname(String classname);


    /***********************************************************************************************
     * Get the IconFilename field.
     *
     * @return String
     */

    String getIconFilename();


    /***********************************************************************************************
     * Set the IconFilename field.
     *
     * @param filename
     */

    void setIconFilename(String filename);



    /***********************************************************************************************
     * Get the Description field
     *
     * @return String
     */

    String getDescription();


    /***********************************************************************************************
     * Set the Description field
     *
     * @param description
     */

    void setDescription(String description);


    /***********************************************************************************************
     * Run the UI of this UserObjectPlugin when its tree node is selected.
     * setUIOccupant() uses this from the navigation tree, a menu, or a toolbar button.
     *
     */

    void runUI();


    /***********************************************************************************************
     * Stop the UI of this UserObjectPlugin when its tree node is deselected.
     * clearUIOccupant() uses this from the navigation tree, a menu, or a toolbar button.
     */

    void stopUI();

    UIComponentPlugin getEditorComponent();

    void setEditorComponent(final UIComponentPlugin component);

    boolean getBrowseMode();

    void setBrowseMode(boolean mode);

    //----------------------------------------------------------------------------------------------
    // Resources

    boolean isUpdated();

    void setUpdated(boolean flag);

    boolean isUpdateAllowed();

    void setUpdateAllowed(boolean updates);

    void resumeUpdates();

    void suspendUpdates();

    UIComponentPlugin getUIComponent();

    void setUIComponent(UIComponentPlugin uicomponent);

    boolean isClassFound();

    void setClassFound(boolean flag);

    boolean getDebugMode();

    void setDebugMode(boolean flag);

    /***********************************************************************************************
     * Get the Name field.
     *
     * @return String
     */

    String getName();

    /***********************************************************************************************
     * Set the Name field.
     *
     * @param name
     */

    void setName(String name);

    /***********************************************************************************************
     * Get the Active field.
     *
     * @return boolean
     */

    boolean isActive();

    /***********************************************************************************************
     * Set the Active field.
     *
     * @param active
     */

    void setActive(boolean active);


    void showDebugData();

    boolean validatePlugin();

    java.util.List<String> getValidationErrors();

    void updateRoot();
    }
