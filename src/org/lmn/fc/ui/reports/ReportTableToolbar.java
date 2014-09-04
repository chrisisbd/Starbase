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

package org.lmn.fc.ui.reports;


import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.ui.UIComponentPlugin;

import java.awt.*;


/***************************************************************************************************
 * ReportTableToolbar.
 * Configure the orientation and buttons for showing a subset with refresh of the view (DataViewMode),
 * removal of the view, truncation of the view, and complete disposal of the underlying dataset.
 * Assume that vertical toobars do not have the DataViewMode functionality.
 */

public enum ReportTableToolbar
    {
    NONE                            ("",                 0, false, false, false, false, false, false, null),

    // Horizontal toolbars can have RefreshView (with DataViewMode), RemoveView, TruncateView and DisposeAll (left to right)
    HORIZ_NORTH_PRT                 (BorderLayout.NORTH, 1, false,  true, false, false, false, false, new Dimension(2000, UIComponentPlugin.HEIGHT_TOOLBAR_ICON + 11)),
    HORIZ_NORTH_RNG_PRT_RF_RV_TV_DA (BorderLayout.NORTH, 2,  true,  true,  true,  true,  true,  true, new Dimension(2000, UIComponentPlugin.HEIGHT_TOOLBAR_ICON + 11)),
    HORIZ_SOUTH_RNG_PRT_RF_RV_TV_DA (BorderLayout.SOUTH, 3,  true,  true,  true,  true,  true,  true, new Dimension(2000, UIComponentPlugin.HEIGHT_TOOLBAR_ICON + 11)),

    // Vertical toolbars have DisposeAll and TruncateView (top to bottom)
    VERT_EAST_PRT_TV_DA             (BorderLayout.EAST,  4, false,  true, false, false,  true,  true, new Dimension(UIComponentPlugin.WIDTH_TOOLBAR_ICON + 8, 1000)),
    VERT_WEST_PRT_TV_DA             (BorderLayout.WEST,  5, false,  true, false, false,  true,  true, new Dimension(UIComponentPlugin.WIDTH_TOOLBAR_ICON + 8, 1000));


    private final int intIndex;
    private final String strOrientation;
    private final boolean boolRanges;
    private final boolean boolPrinting;
    private final boolean boolRefreshView;
    private final boolean boolRemoveView;
    private final boolean boolTruncateView;
    private final boolean boolDisposeAll;
    private Dimension dimToolbar;


    /***********************************************************************************************
     * Get the ReportTableToolbar enum corresponding to the specified ReportTableToolbar orientation.
     * Return NULL if the ReportTableToolbar orientation is not found.
     *
     * @param orientation
     *
     * @return ReportTableToolbar
     */

    public static ReportTableToolbar getReportTableToolbarForOrientation(final String orientation)
        {
        final String SOURCE = "ReportTableToolbar.getReportTableToolbarForOrientation() ";
        ReportTableToolbar toolbarType;

        toolbarType = null;

        if ((orientation != null)
            && (!FrameworkStrings.EMPTY_STRING.equals(orientation)))
            {
            final ReportTableToolbar[] types;
            boolean boolFoundIt;

            types = values();
            boolFoundIt = false;

            for (int i = 0;
                 (!boolFoundIt) && (i < types.length);
                 i++)
                {
                final ReportTableToolbar type;

                type = types[i];

                if (orientation.equals(type.getOrientation()))
                    {
                    toolbarType = type;
                    boolFoundIt = true;
                    }
                }
            }

        return (toolbarType);
        }


    /***********************************************************************************************
     * Construct a ReportTableToolbar.
     * Configure the orientation and buttons for showing a subset with refresh of the view (DataViewMode),
     * removal of the view, truncation of the view, and complete disposal of the underlying dataset.
     * Assume that vertical toobars do not have the DataViewMode functionality.
     *
     * @param orientation
     * @param index
     * @param ranges
     * @param printing
     * @param refreshview
     * @param removeview
     * @param truncateview
     * @param disposeall
     * @param dimension
     */

    private ReportTableToolbar(final String orientation,
                               final int index,
                               final boolean ranges,
                               final boolean printing,
                               final boolean refreshview,
                               final boolean removeview,
                               final boolean truncateview,
                               final boolean disposeall,
                               final Dimension dimension)
        {
        this.intIndex = index;
        this.strOrientation = orientation;
        this.boolRanges = ranges;
        this.boolPrinting = printing;
        this.boolRefreshView = refreshview;
        this.boolRemoveView = removeview;
        this.boolTruncateView = truncateview;
        this.boolDisposeAll = disposeall;
        this.dimToolbar = dimension;
        }


    /***********************************************************************************************
     * Get the ReportTableToolbar type index.
     *
     * @return int
     */

    public int getIndex()
        {
        return (this.intIndex);
        }


    /***********************************************************************************************
     * Get the ReportTableToolbar Orientation.
     *
     * @return String
     */

    public String getOrientation()
        {
        return(this.strOrientation);
        }


    /***********************************************************************************************
     * Indicate if this Toolbar has the data range buttons.
     *
     * @return boolean
     */

    public boolean hasRanges()
        {
        return (this.boolRanges);
        }


    /***********************************************************************************************
     * Indicate if this Toolbar has the printing buttons.
     *
     * @return boolean
     */

    public boolean hasPrinting()
        {
        return (this.boolPrinting);
        }


    /***********************************************************************************************
     * Indicate if this Toolbar has the RefreshView button.
     *
     * @return boolean
     */

    public boolean hasRefreshView()
        {
        return (this.boolRefreshView);
        }


    /***********************************************************************************************
     * Indicate if this Toolbar has the RemoveView button.
     *
     * @return boolean
     */

    public boolean hasRemoveView()
        {
        return (this.boolRemoveView);
        }


    /***********************************************************************************************
     * Indicate if this Toolbar has the TruncateView button.
     *
     * @return boolean
     */

    public boolean hasTruncateView()
        {
        return (this.boolTruncateView);
        }


    /***********************************************************************************************
     * Indicate if this Toolbar has the DisposeAll button.
     *
     * @return boolean
     */

    public boolean hasDisposeAll()
        {
        return (this.boolDisposeAll);
        }


    /***********************************************************************************************
     * Get the ReportTableToolbar Dimension.
     *
     * @return Dimension
     */

    public Dimension getDimension()
        {
        return(this.dimToolbar);
        }


    /***********************************************************************************************
     * Get the ReportTableToolbar Orientation as a String.
     *
     * @return String
     */

    public String toString()
        {
        return (this.strOrientation);
        }
    }