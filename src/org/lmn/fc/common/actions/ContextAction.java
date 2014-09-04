// Copyright 2000, 2001, 2002, 2003, 04, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013, 2013
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
//  20-10-03    LMN created file
//  30-06-06    LMN added Separator
//
//--------------------------------------------------------------------------------------------------

package org.lmn.fc.common.actions;

import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkStrings;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;


/***************************************************************************************************
 * ContextActions for the navigation structure.
 */

public abstract class ContextAction extends AbstractAction
                                    implements FrameworkConstants,
                                               FrameworkStrings
    {
    /***********************************************************************************************
     * Get a ContextAction defined as a separator.
     *
     * @return ContextAction
     * @param menu
     * @param toolbar
     */

    public static ContextAction getSeparator(final boolean menu,
                                             final boolean toolbar)
        {
        final ContextAction separator;

        separator = new ContextAction("SEPARATOR",
                                      null,
                                      EMPTY_STRING,
                                      KeyEvent.VK_0,
                                      menu,
                                      toolbar)
            {
            public void actionPerformed(final ActionEvent event)
                {
                // No action!
                }
            };

        separator.boolSeparator = true;

        return (separator);
        }


    private final boolean boolMenu;
    private final boolean boolToolBar;
    private final JComponent componentAction;
    private boolean boolSeparator;


    /***********************************************************************************************
     *
     * @param name
     * @param icon
     * @param tooltip
     * @param mnemonic
     * @param menu
     * @param toolbar
     */

    public ContextAction(final String name,
                         final ImageIcon icon,
                         final String tooltip,
                         final Integer mnemonic,
                         final boolean menu,
                         final boolean toolbar)
        {
        // The Icon becomes Action.SMALL_ICON
        super(name, icon);

        // The name disappears into key Action.NAME

        boolMenu = menu;
        boolToolBar = toolbar;
        boolSeparator = false;
        componentAction = null;

        putValue(SHORT_DESCRIPTION, tooltip);
        putValue(MNEMONIC_KEY, mnemonic);
        }


    /***********************************************************************************************
     *
     * @param name
     * @param icon
     * @param tooltip
     * @param mnemonic
     * @param menu
     * @param toolbar
     * @param component
     */

    public ContextAction(final String name,
                         final ImageIcon icon,
                         final String tooltip,
                         final Integer mnemonic,
                         final boolean menu,
                         final boolean toolbar,
                         final JComponent component)
        {
        // The Icon becomes Action.SMALL_ICON
        super(name, icon);

        boolMenu = menu;
        boolToolBar = toolbar;
        boolSeparator = false;
        componentAction = component;

        putValue(SHORT_DESCRIPTION, tooltip);
        putValue(MNEMONIC_KEY, mnemonic);
        }


    /***********************************************************************************************
     *
     * @return boolean
     */

    public final boolean isMenu()
        {
        return (this.boolMenu);
        }


    /***********************************************************************************************
     *
     * @return boolean
     */

    public final boolean isToolBar()
        {
        return (this.boolToolBar);
        }


    /***********************************************************************************************
     *
     * @return boolean
     */

    public final boolean isSeparator()
        {
        return (this.boolSeparator);
        }


    /***********************************************************************************************
     *
     * @return JComponent
     */

    public final JComponent getComponent()
        {
        return (this.componentAction);
        }
    }


//--------------------------------------------------------------------------------------------------
// End of File
