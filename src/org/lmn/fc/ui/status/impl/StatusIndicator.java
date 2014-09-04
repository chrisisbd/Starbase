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
//  16-11-04    LMN created file
//
//--------------------------------------------------------------------------------------------------

package org.lmn.fc.ui.status.impl;

//--------------------------------------------------------------------------------------------------

import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.model.plugins.FrameworkPlugin;
import org.lmn.fc.ui.status.StatusIndicatorInterface;

import javax.swing.*;
import java.awt.*;


/***************************************************************************************************
 * The base class for all StatusIndicators.
 */

public class StatusIndicator extends JLabel
                             implements StatusIndicatorInterface
    {
    /***********************************************************************************************
     *
     * @param colour
     * @param font
     * @param text
     * @param tooltip
     */

    public StatusIndicator(final ColourInterface colour,
                           final FontInterface font,
                           final String text,
                           final String tooltip)
        {
        super();

        setAlignmentY(Component.CENTER_ALIGNMENT);
        setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        setTextColour(colour);
        setTextFont(font);
        setText(text);
        setTooltip(tooltip);
        }


    public void initialiseUI()
        {
        }

    public void disposeUI()
        {
        }

    public void runUI()
        {
        }

    public void stopUI()
        {
        }

    /***********************************************************************************************
     *
     * @param text
     */

    public final void setText(final String text)
        {
        if (text != null)
            {
            super.setText(text);
            }
        else
            {
            super.setText("");
            }
        }


    /***********************************************************************************************
     *
     * @return String
     */

    public final String getText()
        {
        return (super.getText());
        }


    /***********************************************************************************************
     *
     * @param colour
     */

    public final void setTextColour(final ColourInterface colour)
        {
        if (colour != null)
            {
            setForeground(colour.getColor());
            }
        else
            {
            setForeground(Color.BLACK);
            }
        }


    /***********************************************************************************************
     *
     * @param font
     */

    public final void setTextFont(final FontInterface font)
        {
        if (font != null)
            {
            setFont(font.getFont());
            }
        }


    /***********************************************************************************************
     *
     */

    public final String getTooltip()
        {
        return(getToolTipText());
        }


    /***********************************************************************************************
     *
     * @param tooltip
     */

    public final void setTooltip(final String tooltip)
        {
        if (tooltip != null)
            {
            setToolTipText(tooltip);
            }
        }

    /***********************************************************************************************
     *
     * @param icon
     */

    public void setIcon(final Icon icon)
        {
        super.setIcon(icon);
        }


    /***********************************************************************************************
     * Dispose of all UI components and stop any Timers.
     */

    public void dispose()
        {
        super.removeAll();
        }


    /***********************************************************************************************
     * Get the LocaleAutoUpdate status as a String, intended for use in tooltips.
     *
     * @param framework
     *
     * @return String
     */

    public String getUpdateStatus(final FrameworkPlugin framework)
        {
        final StringBuffer buffer;

        buffer = new StringBuffer(" (Auto Update is ");

        if (framework != null)
            {
            if (framework.isAutoUpdate())
                {
                buffer.append("ON)");
                }
            else
                {
                buffer.append("OFF)");
                }
            }

        return (buffer.toString());
        }
    }


//--------------------------------------------------------------------------------------------------
// End of File
