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
//  22-03-04    LMN created file from Swing book
//  21-03-05    LMN added PrintableIcons
//
//--------------------------------------------------------------------------------------------------

package org.lmn.fc.ui.reports;


import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.AbstractTableModel;
import java.util.Vector;


/**************************************************************************************************
 * ReportTableModel.
 */

public final class ReportTableModel extends AbstractTableModel
	                                implements TableColumnModelListener
    {
    private Vector<String> vecHeader;
    private final Vector<PrintableIconInterface> vecIcons;
    private final Vector<ReportColumnMetadata> vecColumns;
    private int intColumnCount;
    private final Object [] objColumnWidths;
    private Vector<Vector> vecRows;


    /**********************************************************************************************
     * ReportTableModel.
     *
     * @param header
     * @param icons
     * @param columns
     * @param widths
     * @param rows
     */

	public ReportTableModel(final Vector<String> header,
                            final Vector<PrintableIconInterface> icons,
                            final Vector<ReportColumnMetadata> columns,
                            final Object [] widths,
                            final Vector<Vector> rows)
        {
        vecHeader = header;
        vecIcons = icons;
        vecColumns = columns;
        intColumnCount = vecColumns.size();
        objColumnWidths = widths;
        vecRows = rows;
 	    }


    /**********************************************************************************************/
    /* Rows                                                                                       */
    /**********************************************************************************************
     * Get the Rows of the Report.
     *
     * @return Vector<Vector>
     */

    public synchronized Vector<Vector> getRows()
        {
        return (this.vecRows);
        }


    /**********************************************************************************************
     * Set the Rows of the Report.
     *
     * @param rows
     */

    public synchronized final void setRows(final Vector<Vector> rows)
        {
        this.vecRows = rows;
        }


    /***********************************************************************************************
     * Clear the Rows of the ReportTable.
     */

    public synchronized void clearRows()
        {
        if (this.vecRows != null)
            {
            this.vecRows.clear();
            }
        }


    /**********************************************************************************************
     * Get the RowCount.
     *
     * @return int
     */

	public synchronized final int getRowCount()
        {
        final int intRowCount;

        if (this.vecRows == null)
            {
            intRowCount = 0;
            }
        else
            {
            intRowCount = vecRows.size();
            }

		return(intRowCount);
	    }


    /***********************************************************************************************
     * Get the specified Row of the Report.
     *
     * @return Vector
     *
     * @param row
     */

    public synchronized final Vector getRowAt(final int row)
        {
        if ((getRows() != null)
            && (!getRows().isEmpty()))
            {
            return (getRows().elementAt(row));
            }
        else
            {
            return (null);
            }
        }


    /*********************************************************************************************/
    /* Columns                                                                                   */
    /**********************************************************************************************
     * Get the ColumnCount.
     *
     * @return int
     */

	public synchronized final int getColumnCount()
        {
		return (this.intColumnCount);
	    }


    /**********************************************************************************************
     *  Returns a default name for the column using spreadsheet conventions:
     *  A, B, C, ... Z, AA, AB, etc.  If <code>column</code> cannot be found,
     *  returns an empty string.
     *
     * @param column  the column being queried
     *
     * @return a string containing the default name of <code>column</code>
     */

    public synchronized final String getColumnName(final int column)
        {
        return (vecColumns.elementAt(column).getName());
        }


    /**********************************************************************************************
     * Get the ReportColumnMetadata for the specified Column.
     *
     * @param column
     *
     * @return ReportColumnMetadata
     */

    public synchronized final ReportColumnMetadata getColumnMetadata(final int column)
        {
        return vecColumns.elementAt(column);
        }


    /**********************************************************************************************
     * Get the array of objects which define the default widths of the columns.
     *
     * @return Object []
     */

    public synchronized final Object [] getColumnWidths()
        {
        return (this.objColumnWidths);
        }


    /*******************************************************************************************
     * Get the Class of the specified Column.
     *
     * @param column
     *
     * @return Class
     */

    public Class getColumnClass(final int column)
        {
        // This example implementation only works if the table has been loaded with data...
        // return (getValueAt(0, c).getClass());

        // This is more reliable...
        return (getColumnWidths()[column].getClass());
        }


    /*********************************************************************************************/
    /* Utilities                                                                                 */
    /**********************************************************************************************
     * Indicate if the specified cell is editable.
     *
     * @param row
     * @param column
     *
     * @return boolean
     */

	public synchronized final boolean isCellEditable(final int row, final int column)
        {
		return (false);
	    }


    /**********************************************************************************************
     * Return the Object at (row, column) in the Table.
     *
     * @param row
     * @param column
     *
     * @return Object
     */

	public synchronized final Object getValueAt(final int row, final int column)
        {
		if (row < 0 || row >= getRowCount())
            {
			return ("");
            }

        // Recover the row
		final Vector vecRow;

        vecRow = vecRows.elementAt(row);

        if ((column < 0) || (column >= vecRow.size()))
            {
            return ("");
            }

		return (vecRow.elementAt(column));
	    }


    /**********************************************************************************************
     * Get the ReportHeader.
     *
     * @return Vector
     */

    public synchronized final Vector<String> getHeader()
        {
        return (this.vecHeader);
        }


    /***********************************************************************************************
     * Set the ReportHeader.
     *
     * @param header
     */

    public synchronized void setHeader(final Vector<String> header)
        {
        this.vecHeader = header;
        }


    /**********************************************************************************************
     * Get the Report Icons.
     *
     * @return Vector
     */

    public synchronized final Vector<PrintableIconInterface> getIcons()
        {
        return (this.vecIcons);
        }


    /**********************************************************************************************
     * Get the Report Title.
     *
     * @return String
     */

	public synchronized final String getTitle()
        {
		return this.vecHeader.elementAt(0);
	    }


    /*********************************************************************************************/
    /* TableColumnModelListener                                                                  */
    /**********************************************************************************************
     * Tells listeners that a column was added to the model.
     *
     * @param event
     */

	public synchronized final void columnAdded(final TableColumnModelEvent event)
        {
		intColumnCount++;
	    }


    /**********************************************************************************************
     *
     * @param event
     */

	public synchronized final void columnRemoved(final TableColumnModelEvent event)
        {
		intColumnCount--;
	    }


    /**********************************************************************************************
     *
     * @param event
     */

	public synchronized final void columnMarginChanged(final ChangeEvent event)
        {
        }


    /**********************************************************************************************
     *
     * @param event
     */

	public synchronized final void columnMoved(final TableColumnModelEvent event)
        {
        }


    /**********************************************************************************************
     *
     * @param event
     */

	public synchronized final void columnSelectionChanged(final ListSelectionEvent event)
        {
        }
    }
