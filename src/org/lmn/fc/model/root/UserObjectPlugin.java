package org.lmn.fc.model.root;

import org.lmn.fc.common.actions.ContextActionGroup;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.*;
import java.util.Vector;
import java.util.GregorianCalendar;
import java.awt.*;


/***********************************************************************************************
 * The UserObjectPlugin.
 */

public interface UserObjectPlugin extends RootPlugin
    {
    // UserObject ContextActions
    Vector<ContextActionGroup> getUserObjectContextActionGroups();

    void addUserObjectContextActionGroup(ContextActionGroup actiongroup);

    void removeUserObjectContextActionGroup(ContextActionGroup actiongroup);

    void clearUserObjectContextActionGroups();

    void clearUserObjectContextActionGroup(ContextActionGroup group);

    void setUserObjectContextActionGroups(Vector<ContextActionGroup> group);

    ContextActionGroup getPrimaryUserObjectContextActionGroup();

    ContextActionGroup getUserObjectContextActionGroupByIndex(ActionGroup index);

    // UserObject Action

    // ActionPerformed must:
    //      use FrameworkManagerUIComponentPlugin to:
    //          clear the previous UI occupant
    //          set the new UI occupant
    //      use RegistryModelController to:
    //          setPluginState() or setTaskState()
    //      use UserObject to:
    //          record the new selection
    void actionPerformed(AWTEvent event, boolean mode);

    // ActionHalted must:
    //      use FrameworkManagerUIComponentPlugin to:
    //          clear the previous UI occupant
    //          set the new UI occupant to BlankUI
    //      use RegistryModelController to:
    //          setPluginState() or setTaskState()
    //      use UserObject to:
    //          record the new selection
    void actionHalted(AWTEvent event, boolean browsemode);

    void selectNodeOnTree(DefaultMutableTreeNode node);

    void setCaption(String caption);

    void setStatus(String status);

    void setStatus(String status, Icon icon);

    void editUserObject();

    //----------------------------------------------------------------------------------------------
    // XML Persistence Dates and Times

    GregorianCalendar getCreatedDate();

    void setCreatedDate(GregorianCalendar calendar);

    GregorianCalendar getCreatedTime();

    void setCreatedTime(GregorianCalendar calendar);

    GregorianCalendar getModifiedDate();

    void setModifiedDate(GregorianCalendar calendar);

    GregorianCalendar getModifiedTime();

    void setModifiedTime(GregorianCalendar calendar);
    }
