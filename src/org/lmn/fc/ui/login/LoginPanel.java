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

//--------------------------------------------------------------------------------------------------
// Revision History
//
//  17-10-03    LMN created file
//  15-02-06    LMN changed to a Singleton, initialised from a FrameworkPlugin
//  17-02-06    LMN adding Log and Login tabs
//  27-02-06    LMN fully implemented as interfaces...
//
//--------------------------------------------------------------------------------------------------

package org.lmn.fc.ui.login;

import org.lmn.fc.common.exceptions.FrameworkException;
import org.lmn.fc.model.dao.DataStore;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.model.plugins.FrameworkPlugin;
import org.lmn.fc.model.registry.RegistryModelUtilities;
import org.lmn.fc.model.users.UserPlugin;
import org.lmn.fc.ui.UIComponentPlugin;
import org.lmn.fc.ui.components.UIComponent;
import org.lmn.fc.ui.components.UIComponentHelper;
import org.lmn.fc.ui.layout.BoxLayoutFixed;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;


/***************************************************************************************************
 *
 */

public final class LoginPanel extends UIComponent
                              implements UIComponentPlugin
    {
    private static final String TOOLTIP_LOGIN = "Login to the Framework with Username and Password";
    private static final String TOOLTIP_LOG = "The Framework initialisation log";
    private static final String TOOLTIP_CONFIG = "The Framework initialisation properties";
    private static final String TOOLTIP_HELP = "Login Help";
    private static final String TOOLTIP_LICENCE = "The Framework Licence";
    private static final String TAB_LOGIN = "Login";
    private static final String TAB_LOG = "Log";
    private static final String TAB_CONFIGURATION = "Configuration";
    private static final String TAB_HELP = "Help";
    private static final String TAB_LICENCE = "Licence";
    private static final String ERROR_TAB_INDEX = "Invalid tab index in LoginPanel";
    private static final String ERROR_SPLASH_SCREEN = "Unable to locate the SplashScreen image";

    private static final int HEIGHT_LOGIN = 150;
    private static final int TAB_INDEX_LOGIN = 0;
    private static final int TAB_INDEX_LOG = 1;
    private static final int TAB_INDEX_CONFIG = 2;
    private static final int TAB_INDEX_HELP = 3;
    private static final int TAB_INDEX_LICENCE = 4;

    private final UIComponentPlugin hostLoginDialog;
    private final DataStore dataStore;
    private final boolean boolFirstLogin;

    private UIComponentPlugin tabLog;
    private UIComponentPlugin tabLogin;
    private UIComponentPlugin tabConfig;
    private UIComponentPlugin tabHelp;
    private UIComponentPlugin tabLicence;
    private JTabbedPane tabbedPane;


    /***********************************************************************************************
     * Construct the LoginPanel.
     *
     * @param dialog
     * @param store
     * @param firstlogin
     */

    LoginPanel(final UIComponentPlugin dialog,
               final DataStore store,
               final boolean firstlogin)
        {
        super();

        if ((dialog == null)
            || (store == null))
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_NULL);
            }

        hostLoginDialog = dialog;
        dataStore = store;
        boolFirstLogin = firstlogin;

        setLayout(new BoxLayoutFixed(this, BoxLayoutFixed.Y_AXIS));
        setBackground(DEFAULT_COLOUR_CANVAS.getColor());
        }


    /***********************************************************************************************
     *
     */

    public void initialiseUI()
        {
        final FrameworkPlugin plugin;
        final String strName;
        final JLabel labelIcon;
        final ImageIcon icon;
        final int intIconWidth;

        plugin = REGISTRY.getFramework();
        strName = VERSION_LOGIN
                  + plugin.getVersionNumber()
                  + DOT
                  + plugin.getBuildNumber()
                  + " (" + plugin.getBuildStatus() + ")";

        if ((plugin.getSplashScreenFilename() != null)
            && (!EMPTY_STRING.equals(plugin.getSplashScreenFilename())))
            {
            // Get the SplashScreen Icon
            icon = RegistryModelUtilities.getAtomIcon(plugin, plugin.getSplashScreenFilename());

            if (icon != null)
                {
                labelIcon = new JLabel(icon)
                    {
                    public void paintComponent(final Graphics graphics)
                        {
                        super.paintComponent(graphics);

                        final Font font;
                        final FontMetrics metrics;
                        final Rectangle2D rectangleTrial;

                        graphics.setColor(COLOUR_RAG_TEXT.getColor());
                        font = FontInterface.DEFAULT_FONT_BANNER.getFont();
                        graphics.setFont(font);
                        metrics = this.getFontMetrics(font);
                        rectangleTrial = metrics.getStringBounds(strName, graphics);

                        // Draw the text two-thirds of the way down the JLabel,
                        // in the centre horizontally
                        graphics.drawString(strName,
                                            ((getWidth() - (int) rectangleTrial.getWidth()) >> 1),
                                            ((getHeight() + (int) rectangleTrial.getHeight()) << 1) / 3);
                        }
                    };

                intIconWidth = icon.getIconWidth();
                }
            else
                {
                labelIcon = new JLabel(ERROR_SPLASH_SCREEN);
                intIconWidth = (int)(Toolkit.getDefaultToolkit().getScreenSize().getWidth()/3);
                }
            labelIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
            add(labelIcon);

            // Create the LogTab UIComponent
            setLogTab(new LogTab());

            // Create the LoginTab UIComponent
            setLoginTab(new LoginTab(getHostDialog(), getDataStore(), isFirstLogin()));

            // Create the ConfigurationTab UIComponent
            // This relies on having the RootTask task installed...
            setConfigTab(new ConfigurationTab());

            // Create the HelpTab UIComponent
            setHelpTab(new HelpTab());

            // Create the LicenceTab UIComponent
            setLicenceTab(new LicenceTab());

            setTabbedPane(new JTabbedPane(JTabbedPane.BOTTOM));
            getTabbedPane().setForeground(DEFAULT_COLOUR_TEXT.getColor());
            getTabbedPane().setBackground(DEFAULT_COLOUR_CANVAS.getColor());
            getTabbedPane().setFont(FontInterface.DEFAULT_FONT_LABEL.getFont());
            //getTabbedPane().setBorder(BorderFactory.createEtchedBorder());
            ToolTipManager.sharedInstance().registerComponent(getTabbedPane());

            getTabbedPane().addTab(TAB_LOGIN, (JComponent)getLoginTab());
            getTabbedPane().addTab(TAB_LOG, (JComponent)getLogTab());
            getTabbedPane().addTab(TAB_CONFIGURATION, (JComponent)getConfigTab());
            getTabbedPane().addTab(TAB_HELP, (JComponent)getHelpTab());
            getTabbedPane().addTab(TAB_LICENCE, (JComponent)getLicenceTab());

            getTabbedPane().setToolTipTextAt(TAB_INDEX_LOGIN, TOOLTIP_LOGIN);
            getTabbedPane().setToolTipTextAt(TAB_INDEX_LOG, TOOLTIP_LOG);
            getTabbedPane().setToolTipTextAt(TAB_INDEX_CONFIG, TOOLTIP_CONFIG);
            getTabbedPane().setToolTipTextAt(TAB_INDEX_HELP, TOOLTIP_HELP);
            getTabbedPane().setToolTipTextAt(TAB_INDEX_LICENCE, TOOLTIP_LICENCE);

            setTabEnabled(TAB_INDEX_LOG, true);
            setTabEnabled(TAB_INDEX_LOGIN, false);
            setTabEnabled(TAB_INDEX_CONFIG, true);
            setTabEnabled(TAB_INDEX_HELP, true);
            setTabEnabled(TAB_INDEX_LICENCE, true);
            getTabbedPane().setSelectedIndex(TAB_INDEX_LOG);

            getTabbedPane().setMaximumSize(new Dimension(intIconWidth, HEIGHT_LOGIN));
            getTabbedPane().setMinimumSize(new Dimension(intIconWidth, HEIGHT_LOGIN));
            getTabbedPane().setPreferredSize(new Dimension(intIconWidth, HEIGHT_LOGIN));
            getTabbedPane().setAlignmentX(Component.CENTER_ALIGNMENT);

            // Initialise all UIComponentPlugins on the tabs of the JTabbedPane
            // This will apply ContextActions for each UIComponentPlugin
            UIComponentHelper.initialiseAllTabComponents(getTabbedPane());

            // Put the Login dialog together
            this.add(getTabbedPane());
            }
        }


    /***********************************************************************************************
     * Dispose of all components of this UIComponent.
     */

    public void disposeUI()
        {
        getLogTab().disposeUI();
        getLoginTab().disposeUI();
        getConfigTab().disposeUI();
        getHelpTab().disposeUI();
        getLicenceTab().disposeUI();
        super.disposeUI();
        }


    /***********************************************************************************************
     * Run this UIComponent.
     */

    public void runUI()
        {
        getLogTab().runUI();
        getLoginTab().runUI();
        getConfigTab().runUI();
        getHelpTab().runUI();
        getLicenceTab().runUI();
        super.runUI();
        }


    /***********************************************************************************************
     * Stop this UIComponent.
     */

    public void stopUI()
        {
        getLogTab().stopUI();
        getLoginTab().stopUI();
        getConfigTab().stopUI();
        getHelpTab().stopUI();
        getLicenceTab().stopUI();
        super.stopUI();
        }


    /***********************************************************************************************
     *
     * @param text
     */

    public void setNarrative(final String text)
        {
        if ((getLogTab() != null)
            && (text != null))
            {
            ((LogTab)getLogTab()).setText(text);
            //LOGGER.log("LoginPanel narrative " + text);
            }
        }


    /***********************************************************************************************
     *
     * @param firstlogin
     */

    public void enableLogin(final boolean firstlogin)
        {
        if (getTabbedPane() != null)
            {
            setTabEnabled(TAB_INDEX_LOGIN, true);
            getTabbedPane().setSelectedIndex(TAB_INDEX_LOGIN);
            ((JDialog)getHostDialog()).toFront();
            }
        }


    /***********************************************************************************************
     *
     * @return
     *
     * @throws FrameworkException
     */

    public UserPlugin getUserData() throws FrameworkException
        {
        UserPlugin userPlugin;

        userPlugin = null;

        if (getLoginTab() != null)
            {
            userPlugin = ((LoginTab)getLoginTab()).getUserPlugin();
            }

        return (userPlugin);
        }


    /***********************************************************************************************
     *
     * @param index
     * @param enabled
     */

    private void setTabEnabled(final int index,
                               final boolean enabled)
        {
        if (getTabbedPane() != null)
            {
            try
                {
                getTabbedPane().setEnabledAt(index, enabled);
                }

            catch (ArrayIndexOutOfBoundsException exception)
                {
                // Just do nothing...
                LOGGER.error(ERROR_TAB_INDEX);
                }
            }
        }


    /***********************************************************************************************
     *
     * @return LoginDialog
     */

    private UIComponentPlugin getHostDialog()
        {
        return (this.hostLoginDialog);
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
     * @return LogTab
     */

    private UIComponentPlugin getLogTab()
        {
        return tabLog;
        }


    /***********************************************************************************************
     *
     * @param tab
     */

    private void setLogTab(final UIComponentPlugin tab)
        {
        this.tabLog = tab;
        }


    /***********************************************************************************************
     *
     * @return LoginTab
     */

    public UIComponentPlugin getLoginTab()
        {
        return tabLogin;
        }


    /***********************************************************************************************
     *
     * @param tab
     */

    private void setLoginTab(final UIComponentPlugin tab)
        {
        this.tabLogin = tab;
        }


    /***********************************************************************************************
     *
     * @return ConfigurationTab
     */

    private UIComponentPlugin getConfigTab()
        {
        return tabConfig;
        }


    /***********************************************************************************************
     *
     * @param tab
     */

    private void setConfigTab(final UIComponentPlugin tab)
        {
        this.tabConfig = tab;
        }


    /***********************************************************************************************
     *
     * @return HelpTab
     */

    private UIComponentPlugin getHelpTab()
        {
        return tabHelp;
        }


    /***********************************************************************************************
     *
     * @param tab
     */

    private void setHelpTab(final UIComponentPlugin tab)
        {
        this.tabHelp = tab;
        }


    /***********************************************************************************************
     *
     * @return LicenceTab
     */

    private UIComponentPlugin getLicenceTab()
        {
        return (this.tabLicence);
        }


    /***********************************************************************************************
     *
     * @param tab
     */

    private void setLicenceTab(final UIComponentPlugin tab)
        {
        this.tabLicence = tab;
        }


    /***********************************************************************************************
     *
     * @return JTabbedPane
     */

    private JTabbedPane getTabbedPane()
        {
        return tabbedPane;
        }


    /***********************************************************************************************
     *
     * @param pane
     */

    private void setTabbedPane(final JTabbedPane pane)
        {
        this.tabbedPane = pane;
        }
    }


//--------------------------------------------------------------------------------------------------
// End of File
