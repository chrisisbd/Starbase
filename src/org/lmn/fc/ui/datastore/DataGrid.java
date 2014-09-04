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


package org.lmn.fc.ui.datastore;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import java.awt.*;
import java.util.Vector;

// sqlbob@users 20020401 - patch 1.7.0 by sqlbob (RMP) - enhancements
// deccles@users 20040412 - patch 933671 - various bug fixes

/**
 * Simple table model to represent a grid of tuples.
 *
 * @version 1.7.2
 */
final class DataGrid extends AbstractTableModel
    {

    JTable jtable;
    Object[] headers;
    final Vector rows;

    /**
     * Default constructor.
     */
    DataGrid()
        {

        super();

        headers = new Object[0];    // initially empty
        rows = new Vector();     // initially empty
        }

    /**
     * Get the name for the specified column.
     */
    public final String getColumnName(final int i)
        {
        return headers[i].toString();
        }

    public final Class getColumnClass(final int i)
        {

        if (rows.size() > 0)
            {
            final Object o = getValueAt(0, i);

            if (o != null)
                {
                return o.getClass();
                }
            }

        return super.getColumnClass(i);
        }

    /**
     * Get the number of columns.
     */
    public final int getColumnCount()
        {
        return headers.length;
        }

    /**
     * Get the number of rows currently in the table.
     */
    public final int getRowCount()
        {
        return rows.size();
        }

    /**
     * Get the current column headings.
     */
    public final Object[] getHead()
        {
        return headers;
        }

    /**
     * Get the current table data.
     * Each row is represented as a <code>String[]</code>
     * with a single non-null value in the 0-relative
     * column position.
     * <p>The first row is at offset 0, the nth row at offset n etc.
     */
    public final Vector getData()
        {
        return rows;
        }

    /**
     * Get the object at the specified cell location.
     */
    public final Object getValueAt(final int row, final int col)
        {

        if (row >= rows.size())
            {
            return null;
            }

        final Object[] colArray = (Object[]) rows.elementAt(row);

        if (col >= colArray.length)
            {
            return null;
            }

        return colArray[col];
        }

    /**
     * Set the name of the column headings.
     */
    public final void setHead(final Object[] h)
        {

        headers = new Object[h.length];

        // System.arraycopy(h, 0, headers, 0, h.length);
        for (int i = 0; i < h.length; i++)
            {
            headers[i] = h[i];
            }
        }

    /**
     * Append a tuple to the end of the table.
     */
    public final void addRow(final Object[] r)
        {

        final Object[] row = new Object[r.length];

        // System.arraycopy(r, 0, row, 0, r.length);
        for (int i = 0; i < r.length; i++)
            {
            row[i] = r[i];

            if (row[i] == null)
                {

//                row[i] = "(null)";
                }
            }

        rows.addElement(row);
        }

    /**
     * Remove data from all cells in the table (without
     * affecting the current headings).
     */
    public final void clear()
        {
        rows.removeAllElements();
        }

    public final void setJTable(final JTable table)
        {
        jtable = table;
        }

    public final void fireTableChanged(final TableModelEvent e)
        {
        super.fireTableChanged(e);
        autoSizeTableColumns(jtable);
        }

    public static void autoSizeTableColumns(final JTable table)
        {

        final TableModel model = table.getModel();
        TableColumn column;
        Component comp;
        int headerWidth;
        int maxCellWidth;
        int cellWidth;
        final TableCellRenderer headerRenderer =
            table.getTableHeader().getDefaultRenderer();

        for (int i = 0; i < table.getColumnCount(); i++)
            {
            column = table.getColumnModel().getColumn(i);
            comp = headerRenderer.getTableCellRendererComponent(table,
                                                                column.getHeaderValue(), false, false, 0, 0);
            headerWidth = comp.getPreferredSize().width + 10;
            maxCellWidth = Integer.MIN_VALUE;

            for (int j = 0; j < Math.min(model.getRowCount(), 30); j++)
                {
                final TableCellRenderer r = table.getCellRenderer(j, i);

                comp = r.getTableCellRendererComponent(table,
                                                       model.getValueAt(j, i),
                                                       false, false, j, i);
                cellWidth = comp.getPreferredSize().width;

                if (cellWidth >= maxCellWidth)
                    {
                    maxCellWidth = cellWidth;
                    }
                }

            column.setPreferredWidth(Math.max(headerWidth, maxCellWidth)
                + 10);
            }
        }
    }
