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


import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.sda.DatasetState;


/***************************************************************************************************
 * ListCellDatasetState.
 * Holds a name and a state containing icons appropriate to the state, used on e.g. a JComboBox.
 */

public final class ListCellDatasetState
    {
    private String strText;
    private String strTooltipText;
    private int intIndex;
    private DatasetState datasetState;


    /***************************************************************************************************
     * Construct a ListCellDatasetState, defaulted to NO_SELECTION index.
     *
     * @param text
     * @param state
     */

    public ListCellDatasetState(final String text,
                                final DatasetState state)
        {
        this.strText = text;
        this.strTooltipText = "";
        this.intIndex = -1;
        this.datasetState = state;
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
     * Get the Tooltip Text.
     *
     * @return String
     */

    public String getTooltipText()
        {
        return (this.strTooltipText);
        }


    /***********************************************************************************************
     * Set the Tooltip Text.
     *
     * @param text
     */

    public void setTooltipText(final String text)
        {
        this.strTooltipText = text;
        }


    /***********************************************************************************************
     * Get the Cell Index.
     *
     * @return int
     */

    public int getIndex()
        {
        return (this.intIndex);
        }


    /***********************************************************************************************
     * Set the Cell Index.
     *
     * @param index
     */

    public void setIndex(final int index)
        {
        this.intIndex = index;
        }


    /***********************************************************************************************
     * Get the DatasetState.
     *
     * @return DatasetState
     */

    public DatasetState getDatasetState()
        {
        return (this.datasetState);
        }


    /***********************************************************************************************
     * Set the DatasetState.
     *
     * @param datasetstate
     */

    public void setDatasetState(final DatasetState datasetstate)
        {
        this.datasetState = datasetstate;
        }
    }
