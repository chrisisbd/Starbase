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

import org.lmn.fc.common.utilities.time.Chronos;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.ui.UIComponentPlugin;
import org.lmn.fc.ui.components.UIComponent;

import javax.swing.*;
import java.awt.*;


/***************************************************************************************************
 *
 */

final class LogTab extends UIComponent
                   implements UIComponentPlugin
    {
    private JTextArea textLog;


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
        return FontInterface.DEFAULT_FONT_INTERFACE;
        }


    /***********************************************************************************************
     * Construct a LogTab.
     */

    LogTab()
        {
        super();

        // REGISTRY.getFramework().getResourceKey() + KEY_RESOURCE_LOGIN
        }


    /***********************************************************************************************
     * Initialise this UIComponent.
     */

    public void initialiseUI()
        {
        final JScrollPane scrollPane;

        setLayout(new BorderLayout());
        setOpaque(true);

        textLog = new JTextArea();
        textLog.setEditable(true);
        textLog.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        textLog.setForeground(getTextColour().getColor());
        textLog.setBackground(getCanvasColour().getColor());
        textLog.setFont(getDataFont().getFont());

        scrollPane = new JScrollPane(textLog,
                                     JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                     JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane, BorderLayout.CENTER);
        super.initialiseUI();
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
     *
     * @param text
     */

    public void setText(final String text)
        {
        if ((textLog != null)
            && (text != null))
            {
            textLog.append(Chronos.getSystemTimeNow() + SPACE + SPACE + text + "\n");
            textLog.setCaretPosition(textLog.getDocument().getLength());
            }
        }
    }
