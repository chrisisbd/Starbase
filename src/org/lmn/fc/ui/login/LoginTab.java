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

import org.lmn.fc.common.exceptions.FrameworkException;
import org.lmn.fc.common.utilities.time.Chronos;
import org.lmn.fc.model.dao.DataStore;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.registry.RegistryModelUtilities;
import org.lmn.fc.model.users.UserPlugin;
import org.lmn.fc.ui.UIComponentPlugin;
import org.lmn.fc.ui.components.EditorUIComponent;
import org.lmn.fc.ui.components.EditorUtilities;
import org.lmn.fc.ui.components.UIComponent;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Enumeration;
import java.util.Vector;


/***************************************************************************************************
 * The LoginTab to allow user validation before entering the Framework.
 */

public final class LoginTab extends UIComponent
                            implements UIComponentPlugin
    {
    // String Resources
    private static final String LABEL_USERNAME          = "Username";
    private static final String LABEL_PASSWORD          = "Password";
    private static final String TEXT_USERNAME           = "";

    private static final String TOOLTIP_USERNAME        = "Enter your username";
    private static final String TOOLTIP_PASSWORD        = "Enter your password";
    private static final String TOOLTIP_LOGIN           = "Login to the Framework";
    private static final String TOOLTIP_EXIT            = "Exit from the Framework";

    private static final String TITLE_LOGIN_FAILURE     = "Login failure";
    private static final String MSG_LOGIN_FAIL0         = "It has not been possible to log you in";
    private static final String MSG_LOGIN_FAIL1         = "Note that the password is case-sensitive";
    private static final String MSG_LOGIN_FAIL2         = "Please try again";

    private static final String BUTTON_LOGIN            = "Login";
    private static final String BUTTON_EXIT             = "Exit";

    // The number of standard height rows
    private static final int ROW_COUNT = 2;

    private static final int WIDTH_INPUT = 150;

    private JTextField textUsername;
    private JPasswordField textPassword;

    private JButton buttonLogin;
    private JButton buttonExit;

    private final UIComponentPlugin loginDialog;
    private final DataStore dataStore;
    private final boolean boolFirstLogin;
    private UserPlugin userPlugin;


    /***********************************************************************************************
     * Get the colour of the canvas.
     *
     * @return ColourPlugin
     */

    private static ColourInterface getCanvasColour()
        {
        return (DEFAULT_COLOUR_CANVAS);
        }


    /***********************************************************************************************
     * Get the colour of the text.
     *
     * @return ColourPlugin
     */

    private static ColourInterface getTextColour()
        {
        return (DEFAULT_COLOUR_TEXT);
        }


    /***********************************************************************************************
     * Get the Font for the Labels.
     *
     * @return FontPlugin
     */

    private static FontInterface getLabelFont()
        {
        return FontInterface.DEFAULT_FONT_LABEL;
        }


    /***********************************************************************************************
     * Get the Font for data.
     *
     * @return FontPlugin
     */

    private static FontInterface getDataFont()
        {
        return FontInterface.DEFAULT_FONT_LABEL;
        }


    /***********************************************************************************************
     * Construct a LoginTab.
     *
     * @param dialog
     * @param store
     * @param firstlogin
     */

    LoginTab(final UIComponentPlugin dialog,
             final DataStore store,
             final boolean firstlogin)
        {
        super();

        if ((dialog == null)
            || (store == null))
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_NULL);
            }

        loginDialog = dialog;
        dataStore = store;
        boolFirstLogin = firstlogin;
        userPlugin = null;
        }


    /***********************************************************************************************
     * Create the Login panel.
     */

    public void initialiseUI()
        {
        final JLabel labelUsername;
        final JLabel labelPassword;
        final int intLabelHeight;

        // The left-hand label panel
        labelUsername = EditorUtilities.createLabelDirect(getTextColour(),
                                                          getLabelFont(),
                                                          LABEL_USERNAME);

        labelPassword = EditorUtilities.createLabelDirect(getTextColour(),
                                                          getLabelFont(),
                                                          LABEL_PASSWORD);

        //  The right-hand data panel
        textUsername = EditorUtilities.createTextFieldDirect(getTextColour(),
                                                             getDataFont(),
                                                             TEXT_USERNAME,
                                                             TOOLTIP_USERNAME,
                                                             true);
        textUsername.setText(DEFAULT_USER);
        EditorUtilities.adjustNarrowField(textUsername, WIDTH_INPUT);

        textPassword = EditorUtilities.createPasswordFieldDirect(getTextColour(),
                                                                 getDataFont(),
                                                                 TOOLTIP_PASSWORD,
                                                                 true);
        textPassword.setText(DEFAULT_PASSWORD);
        EditorUtilities.adjustNarrowField(textPassword, WIDTH_INPUT);

        // Text Box Listener
        final DocumentListener listenerText = new DocumentListener()
            {
            public void insertUpdate(final DocumentEvent event)
                {
                dataChanged();
                }

            public void removeUpdate(final DocumentEvent event)
                {
                dataChanged();
                }

            public void changedUpdate(final DocumentEvent event)
                {
                dataChanged();
                }
            };

        textUsername.getDocument().addDocumentListener(listenerText);
        textPassword.getDocument().addDocumentListener(listenerText);

        //--------------------------------------------------------------------------------------
        // The Login button and its listener

        buttonLogin = EditorUtilities.createButtonDirect(getTextColour(),
                                                         getLabelFont(),
                                                         BUTTON_LOGIN,
                                                         TOOLTIP_LOGIN,
                                                         "buttonLogin",
                                                         false);

        final ActionListener loginListener = new ActionListener()
            {
            public void actionPerformed(final ActionEvent event)
                {
                userPlugin = doLogin();
                }
            };

        buttonLogin.addActionListener(loginListener);
        ((LoginDialog)getHostDialog()).getRootPane().setDefaultButton(buttonLogin);
        ((LoginDialog)getHostDialog()).getRootPane().registerKeyboardAction(loginListener,
                                                                            KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
                                                                            JComponent.WHEN_IN_FOCUSED_WINDOW);

        //--------------------------------------------------------------------------------------
        // The Login Exit button and its listener

        buttonExit = EditorUtilities.createButtonDirect(getTextColour(),
                                                        getLabelFont(),
                                                        BUTTON_EXIT,
                                                        TOOLTIP_EXIT,
                                                        "buttonExit",
                                                        true);

        final ActionListener exitListener = new ActionListener()
            {
            // See similar code in FrameworkData.frameworkExit(), UserInterfaceFrame.initialiseUI() and ShutdownDialog
            public void actionPerformed(final ActionEvent event)
                {
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                MODEL_CONTROLLER.stopFramework(REGISTRY.getFramework());
                MODEL_CONTROLLER.exitFramework(REGISTRY.getFramework());
                ((LoginDialog)getHostDialog()).setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

                // Finally, remove the JDialog to allow the main Thread to resume
                ((LoginDialog)getHostDialog()).dispose();
                }
            };

        buttonExit.addActionListener(exitListener);
        ((LoginDialog)getHostDialog()).getRootPane().registerKeyboardAction(exitListener,
                                                                            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                                                                            JComponent.WHEN_IN_FOCUSED_WINDOW);

        //--------------------------------------------------------------------------------------
        // Put all the panels together in the right order

        intLabelHeight = (int)(EditorUIComponent.DIM_ROW_SPACER.getHeight() * ROW_COUNT)
                          + (EditorUtilities.HEIGHT_ROW * ROW_COUNT);

        final JPanel panelEditor = EditorUtilities.createEditorPanel(getCanvasColour());
//        panelEditor.setOpaque(opaque);
        final JPanel panelLabel = EditorUtilities.createLabelPanel(getCanvasColour(),
                                                                   intLabelHeight);
//        panelLabel.setOpaque(opaque);
        final JPanel panelData = EditorUtilities.createDataPanel(getCanvasColour());
//        panelData.setOpaque(opaque);

        final Vector<JButton> vecButtons = new Vector<JButton>(2);
        vecButtons.add(buttonExit);
        vecButtons.add(buttonLogin);
        final JPanel panelButtons = EditorUtilities.createButtonPanel(getCanvasColour(),
                                                                      vecButtons);
//        panelButtons.setOpaque(opaque);

        panelLabel.add(labelUsername);
        panelLabel.add(Box.createRigidArea(EditorUIComponent.DIM_ROW_SPACER));
        panelLabel.add(labelPassword);
        panelLabel.add(Box.createRigidArea(EditorUIComponent.DIM_ROW_SPACER));

        // The Data Panel
        panelData.add(textUsername);
        panelData.add(Box.createRigidArea(EditorUIComponent.DIM_ROW_SPACER));
        panelData.add(textPassword);
        panelData.add(Box.createRigidArea(EditorUIComponent.DIM_ROW_SPACER));

        add(Box.createRigidArea(new Dimension(10, 15)));
        EditorUtilities.installPanels(this,
                                      getCanvasColour(),
                                      panelEditor,
                                      panelLabel,
                                      panelData,
                                      panelButtons);
        setBorder(BorderFactory.createEtchedBorder());
        textUsername.setEnabled(true);

        // Allow quick login...
        dataChanged();

        super.initialiseUI();
        }


    /***********************************************************************************************
     * Attempt to login using the Username and Password entered.
     * Return a UserPlugin with a valid UserRole if the login was successful,
     * otherwise return <code>null</code>.
     *
     * @return UserPlugin
     */

    private UserPlugin doLogin()
        {
        UserPlugin user;
        final StringBuffer bufferPassword;
        final String [] strMessage =
            {
            MSG_LOGIN_FAIL0,
            MSG_LOGIN_FAIL1,
            MSG_LOGIN_FAIL2
            };

        user = null;

        if ((textUsername != null)
            && (textPassword != null)
            && (REGISTRY.getUsers() != null))
            {
            // Retrieve the password exactly as entered
            bufferPassword = new StringBuffer(10);
            bufferPassword.append(textPassword.getPassword());

            try
                {
                // If it is the first login, retrieve the User from the Registry if possible
                //if (isFirstLogin())
                // TODO FIRST LOGIN!!
//                if (true)
//                    {
                    final Enumeration<String> enumKeys;
                    boolean boolUserFound;

                    // Find a User in the Registry with the Username as entered
                    // We must scan the keys rather than use them directly,
                    // because we don't know the case of the entered username.

                    enumKeys = REGISTRY.getUsers().keys();
                    boolUserFound = false;

                    while ((enumKeys.hasMoreElements())
                        && (!boolUserFound))
                        {
                        final String strRegistryKey;
                        final String strUserKey;

                        strRegistryKey = enumKeys.nextElement();
                        strUserKey = PREFIX_USER + KEY_DELIMITER + textUsername.getText().trim();

                        if (strRegistryKey.equalsIgnoreCase(strUserKey))
                            {
                            user = REGISTRY.getUser(strRegistryKey);
                            boolUserFound = true;
                            }
                        }

                    // Did we find a User in the Registry with an appropriate Username?
                    if (boolUserFound)
                        {
                        // Check that the password is as expected for this User
                        // There must be a password of a minimum length of 8 characters
                        if (!user.getPassword().equals(bufferPassword.toString()))
                            {
                            // The passwords don't match, so abort this login...
                            user = null;
                            }
                        }
//                    }
//                else
//                    {
//                    final UsersDAOInterface daoUsers;
//
//                    // Subsequent logins obtain the User and Role from the DataStore
//                    if ((DataStore.XML.equals(getDataStore()))
//                        && (DataStore.XML.isAvailable()))
//                        {
//                        // ToDo XML DataStore DAO
//                        throw new IOException(FrameworkStrings.EXCEPTION_DATASTORE_INVALID);
//                        }
//                    else if ((DataStore.MYSQL.equals(getDataStore()))
//                        && (DataStore.MYSQL.isAvailable()))
//                        {
//                        daoUsers = new UsersMySqlDAO();
//                        }
//                    else if (DataStore.HSQLDB.isValidDataStore(getDataStore()))
//                        {
//                        // ToDo HSQLDB DataStore DAO
//                        throw new IOException(FrameworkStrings.EXCEPTION_DATASTORE_INVALID);
//                        }
//                    else
//                        {
//                        throw new IOException(FrameworkStrings.EXCEPTION_DATASTORE_INVALID);
//                        }
//
//                    // ToDo retrieve and configure the User
//                    user = daoUsers.selectUser(textUsername.getText().trim(),
//                                               bufferPassword.toString());
//                    }

                //----------------------------------------------------------------------------------
                // Check that the UserPlugin we now have is valid...

                if (RegistryModelUtilities.isValidUser(user))
                    {
                    // All went well, so record the fact that we logged in...
                    user.setDateLastLogin(Chronos.getSystemDateNow());
                    user.setTimeLastLogin(Chronos.getSystemTimeNow());

                    // Log the login!
                    LOGGER.logAtomEvent(REGISTRY.getFramework(),
                                        REGISTRY.getFramework().getRootTask(),
                                        this.getClass().getName(),
                                        METADATA_FRAMEWORK_LOGIN
                                            + METADATA_NAME + REGISTRY.getFramework().getName() + TERMINATOR + SPACE
                                            + METADATA_USER + user.getName() + TERMINATOR + SPACE
                                            + METADATA_ROLE + user.getRole().getName() + TERMINATOR,
                                        EventStatus.INFO);

                    // Indicate that the RegistryModel is to be assembled for this User
                    REGISTRY_MODEL.setLoggedInUser(user);

                    // Proceed to remove the dialog
                    buttonLogin.setEnabled(false);
                    buttonExit.setEnabled(true);

                    // The current thread will not stop until the JDialog is hidden again...
                    MODEL_CONTROLLER.disposeLoginDialog();
                    }
                else
                    {
                    // Let the user have another go...
                    JOptionPane.showMessageDialog(null,
                                                  strMessage,
                                                  TITLE_LOGIN_FAILURE,
                                                  JOptionPane.ERROR_MESSAGE);
                    }
                }

            catch (FrameworkException exception)
                {
                // It was an invalid Username and/or Password
                textPassword.setText(EMPTY_STRING);
                }

//            catch (IOException exception)
//                {
//                // It was an invalid Username and/or Password
//                textPassword.setText(EMPTY_STRING);
//                }
            }

        return (user);
        }


    /***********************************************************************************************
     * Dispose of all components of this UIComponent.
     */

    public void disposeUI()
        {
        super.disposeUI();
        }


    /***********************************************************************************************
     * Run this UIComponent.
     */

    public void runUI()
        {
        super.runUI();
        }


    /***********************************************************************************************
     * Stop this UIComponent.
     */

    public void stopUI()
        {
        super.stopUI();
        }


    /***********************************************************************************************
     * Get the UserPlugin which has been validated by the LoginPanel.
     * The calling Thread will not be able to read this until the LoginPanel
     * has validated the User.
     *
     * @return UserData
     *
     * @throws FrameworkException
     */

    public UserPlugin getUserPlugin() throws FrameworkException
        {
        if (!RegistryModelUtilities.isValidUser(this.userPlugin))
            {
            throw new FrameworkException(EXCEPTION_INVALID_USERDATA);
            }

        return (this.userPlugin);
        }


    /***********************************************************************************************
     * Get the host LoginDialog.
     *
     * @return LoginDialog
     */

    private UIComponentPlugin getHostDialog()
        {
        return (this.loginDialog);
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

    private boolean isFirstLogin()
        {
        return (this.boolFirstLogin);
        }


    /*******************************************************************************************
     * A utility to indicate when the data has changed as a result of an edit.
     */

    private void dataChanged()
        {
        buttonLogin.setEnabled(true);
        buttonExit.setEnabled(true);
        }


    /***********************************************************************************************
     * Get the Username box, for setting the focus.
     *
     * @return JTextField
     */

    public JTextField getUsernameBox()
        {
        return (this.textUsername);
        }
    }
