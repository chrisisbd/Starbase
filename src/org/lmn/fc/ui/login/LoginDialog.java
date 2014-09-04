// Copyright 2000, 2001, 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013
// Laurence Newell
// starbase@ukraa.com
// radio.telescope@btinternet.com
//
// This file is part of Starbase.
//
// Starbase is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// Starbase is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with Starbase.  If not, see http://www.gnu.org/licenses.

package org.lmn.fc.ui.login;

import org.lmn.fc.common.actions.ContextActionGroup;
import org.lmn.fc.common.exceptions.FrameworkException;
import org.lmn.fc.frameworks.starbase.ui.userinterface.UserInterfaceFrame;
import org.lmn.fc.model.dao.DataStore;
import org.lmn.fc.model.users.UserPlugin;
import org.lmn.fc.ui.UIComponentPlugin;
import org.lmn.fc.ui.components.UIComponentHelper;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import java.awt.*;
import java.awt.print.PageFormat;
import java.net.URL;
import java.util.Vector;


/***************************************************************************************************
 * The LoginDialog to allow user validation before entering the Framework.
 */

public final class LoginDialog extends JDialog
                               implements UIComponentPlugin
    {
    private UIComponentPlugin loginPanel;
    private final DataStore dataStore;
    private final boolean boolFirstLogin;

    // The context-sensitive Actions for the UIComponent
    private Vector<ContextActionGroup> vecContextActionGroups;

    private PageFormat pageFormat;
    private boolean boolDebugMode;


    /***********************************************************************************************
     * Construct a LoginDialog.
     *
     * @param store
     * @param firstlogin
     */

    public LoginDialog(final DataStore store,
                       final boolean firstlogin)
        {
        // Create the modal JDialog
        super((Frame)null, "", true);

        if (store == null)
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_NULL);
            }

        this.dataStore = store;
        this.boolFirstLogin = firstlogin;
        this.vecContextActionGroups = new Vector<ContextActionGroup>(1);

        setLayout(new BorderLayout());
        this.pageFormat = UIComponentHelper.getDefaultPageFormat();
        this.boolDebugMode = false;
        }


    /***********************************************************************************************
     * Initialise this UIComponent.
     */

    public void initialiseUI()
        {
        setUndecorated(true);
        //getRootPane().setWindowDecorationStyle(JRootPane.PLAIN_DIALOG);

        try
            {
            // We need to use a different L&F because Plastic doesn't work!
            UIManager.setLookAndFeel(UserInterfaceFrame.LOGIN_LOOK_AND_FEEL);
            }

        catch (ClassNotFoundException exception)
            {
            LOGGER.error(exception.getMessage());
            }

        catch (InstantiationException exception)
            {
            LOGGER.error(exception.getMessage());
            }

        catch (IllegalAccessException exception)
            {
            LOGGER.error(exception.getMessage());
            }

        catch (UnsupportedLookAndFeelException exception)
            {
            LOGGER.error(exception.getMessage());
            }

        // Create a LoginPanel and add it to the ContentPane
        loginPanel = new LoginPanel(this, getDataStore(), isFirstLogin());
        getContentPane().add((JComponent)loginPanel);

        // Causes this Window to be sized to fit the preferred size and layouts of its subcomponents.
        // If the window and/or its owner are not yet displayable,
        // both are made displayable before calculating the preferred size.
        // The Window will be validated after the preferredSize is calculated.
        getLoginPanel().initialiseUI();
        getLoginPanel().runUI();
        pack();

        // Now that we have the preferred sizes, we can work out where to put the JDialog
        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        final Dimension dialogSize = getPreferredSize();
        setLocation((screenSize.width >> 1 ) - (dialogSize.width >> 1),
                    (screenSize.height >> 1) - (dialogSize.height >> 1));
        //setLocation(100, 100);
        }


    /***********************************************************************************************
     * Dispose of all components of this UIComponent.
     */

    public void disposeUI()
        {
        getLoginPanel().stopUI();
        getLoginPanel().disposeUI();
        super.dispose();
        }


    /***********************************************************************************************
     * Run this UIComponent.
     */

    public void runUI()
        {
        getLoginPanel().runUI();
        }


    /***********************************************************************************************
     * Stop this UIComponent.
     */

    public void stopUI()
        {
        getLoginPanel().stopUI();
        }


    /***********************************************************************************************
     * Remove any Data associated with this UIComponent's appearance on the UI.
     * For instance, remove a Chart regardless of it being visible.
     */

    public void removeUIIdentity()
        {
        }


    /***********************************************************************************************
     * Sets the border of this component.
     *
     * @param border the border to be rendered for this component
     *
     * @see Border
     * @see CompoundBorder
     */

    public void setBorder(final Border border)
        {

        }


    /***********************************************************************************************
     * Set the opaque property.
     *
     * @param opaque
     */

    public void setOpaque(final boolean opaque)
        {
        // Does nothing
        }


    /*********************************************************************************************
     * Set the X alignment.
     *
     * @param alignment
     */

    public void setAlignmentX(final float alignment)
        {
        // Not a valid concept for the LoginDialog
        }


    /*********************************************************************************************
     * Set the Y alignment.
     *
     * @param alignment
     */

    public void setAlignmentY(final float alignment)
        {
        // Not a valid concept for the LoginDialog
        }


    /***********************************************************************************************
     *
     * @param message
     */

    public void setNarrative(final String message)
        {
        ((LoginPanel)getLoginPanel()).setNarrative(message);
        }


    /***********************************************************************************************
     *
     * @param firstlogin
     */

    public void enableLogin(final boolean firstlogin)
        {
        ((LoginPanel)getLoginPanel()).enableLogin(firstlogin);
        }


    /***********************************************************************************************
     * Get the UserData which has been validated by the LoginPanel.
     * The calling Thread will not be able to read this until the LoginPanel
     * has validated the User.
     *
     * @return UserData
     *
     * @throws FrameworkException
     */

    public UserPlugin getUserData() throws FrameworkException
        {
        return (((LoginPanel)getLoginPanel()).getUserData());
        }


    /***********************************************************************************************
     * Get the DataStore currently used for this Framework.
     *
     * @return DataStore
     */

    private DataStore getDataStore()
        {
        return (this.dataStore);
        }


    /***********************************************************************************************
     * Indicate if this is the first login.
     *
     * @return boolean
     */

    public boolean isFirstLogin()
        {
        return (this.boolFirstLogin);
        }


    /***********************************************************************************************
     *
     * @return LoginPanel
     */

    public UIComponentPlugin getLoginPanel()
        {
        return (this.loginPanel);
        }


    /***********************************************************************************************
    /* Context Actions                                                                            */
    /***********************************************************************************************
     * Get the vector of ContextActionGroups for this UIComponent.
     *
     * @return Vector
     */

    public final Vector<ContextActionGroup> getUIComponentContextActionGroups()
        {
        return (this.vecContextActionGroups);
        }


    /***********************************************************************************************
     * Set the vector of ContextActionGroups for this UIComponent.
     *
     * @param actiongroups
     */

    public final void setUIComponentContextActionGroups(final Vector<ContextActionGroup> actiongroups)
        {
        this.vecContextActionGroups = actiongroups;
        }


    /***********************************************************************************************
     * Add an ActionGroup to the list of ContextActionGroups for this UIComponent.
     *
     * @param group
     */

    public final void addUIComponentContextActionGroup(final ContextActionGroup group)
        {
        if ((group != null)
            && (getUIComponentContextActionGroups() != null)
            && (!getUIComponentContextActionGroups().contains(group)))
            {
            //LOGGER.debug("Add group to UIComponent [group=" + group.getName() + "] [uicomponent=" + getName() + "]");
            getUIComponentContextActionGroups().add(group);
            }
        }


    /***********************************************************************************************
     * Remove an ActionGroup from the list of ContextActionGroups for this UIComponent.
     *
     * @param group
     */

    public final void removeUIComponentContextActionGroup(final ContextActionGroup group)
        {
        if ((group != null)
            && (getUIComponentContextActionGroups() != null)
            && (getUIComponentContextActionGroups().contains(group)))
            {
            //LOGGER.debug("removeContextActionGroup() " + group.getName() + " in " + getName());
            getUIComponentContextActionGroups().remove(group);
            }
        }


    /***********************************************************************************************
     * Clear the complete list of ContextActionGroups for this UIComponent.
     */

    public final void clearUIComponentContextActionGroups()
        {
        //LOGGER.debug("clearContextActionGroups() in " + getName());
        this.vecContextActionGroups = new Vector<ContextActionGroup>(1);
        }


    /***********************************************************************************************
     * Clear the specified ContextActionGroup for this UIComponent.
     *
     * @param group
     */
    public final void clearUIComponentContextActionGroup(final ContextActionGroup group)
        {
        if ((group != null)
            && (getUIComponentContextActionGroups() != null)
            && (getUIComponentContextActionGroups().contains(group)))
            {
            //LOGGER.debug("clearContextActionGroup() " + group.getName() + " in " + getName());
            group.clearContextActions();
            }
        }


    /************************************************************************************************
     * Get the PageFormat for printing.
     *
     * @return PageFormat
     */

    public PageFormat getPageFormat()
        {
        return (this.pageFormat);
        }


    /*********************************************************************************************
     * Set the PageFormat for printing.
     *
     * @param pageformat
     */

    public void setPageFormat(final PageFormat pageformat)
        {
        this.pageFormat = pageformat;
        }


    /***********************************************************************************************
    /* Miscellaneous                                                                              */
    /***********************************************************************************************
     * Find an resource by name (classpath or filesystem).
     *
     * @param resourceName
     *
     * @return URL
     */

    public URL findResource(final String path,
                            final String resourceName)
        {
        final URL url = getClass().getResource(path + resourceName);

        return (url);
        }


    /***********************************************************************************************
     * Indicate if we are in debug mode.
     *
     * @return boolean
     */

    public boolean isDebug()
        {
        return (this.boolDebugMode);
        }


    /************************************************************************************************
     * Indicate if we are in debug mode.
     *
     * @param debug
     */

    public void setDebug(final boolean debug)
        {
        this.boolDebugMode = debug;
        }
    }
