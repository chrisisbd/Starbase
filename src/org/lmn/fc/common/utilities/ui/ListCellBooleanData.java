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

package org.lmn.fc.common.utilities.ui;


import javax.swing.*;


/***************************************************************************************************
 * ListCellBooleanData.
 * This holds text and a Boolean with associated true/false Icons for display on e.g. a JComboBox.
 */

public final class ListCellBooleanData
    {
    private String strText;
    private Boolean boolState;
    private final Icon iconTrue;
    private final Icon iconFalse;


    /***************************************************************************************************
     * Construct a ListCellBooleanData.
     *
     * @param text
     * @param state
     * @param icontrue
     * @param iconfalse
     */

    public ListCellBooleanData(final String text,
                               final Boolean state,
                               final Icon icontrue,
                               final Icon iconfalse)
        {
        this.strText = text;
        this.boolState = state;
        this.iconTrue = icontrue;
        this.iconFalse = iconfalse;
        }


    /***********************************************************************************************
     * Get the Text.
     *
     * @return String
     */

    public String getText()
        {
        return (this.strText);
        }


    /***********************************************************************************************
     * Set the Text.
     *
     * @param text
     */

    public void setText(final String text)
        {
        this.strText = text;
        }


    /***********************************************************************************************
     * Get the State.
     *
     * @return Boolean
     */

    public Boolean getState()
        {
        return (this.boolState);
        }


    /***********************************************************************************************
     * Set the State.
     *
     * @param state
     */

    public void setState(final Boolean state)
        {
        this.boolState = state;
        }


    /***********************************************************************************************
     * Get the True Icon.
     *
     * @return Icon
     */

    public Icon getTrueIcon()
        {
        return (this.iconTrue);
        }


    /***********************************************************************************************
     * Get the False Icon.
     *
     * @return
     */

    public Icon getFalseIcon()
        {
        return (this.iconFalse);
        }


    /***********************************************************************************************
     * Return the text, to display.
     *
     * @return String
     */

    public String toString()
        {
        return (this.strText);
        }
    }
