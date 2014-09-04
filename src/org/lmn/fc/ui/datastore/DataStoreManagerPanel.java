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

import org.hsqldb.lib.java.JavaSystem;
import org.hsqldb.util.TableSorter;
import org.hsqldb.util.Transfer;
import org.lmn.fc.ui.components.UIComponent;

import javax.swing.*;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.sql.*;
import java.util.Vector;


public final class DataStoreManagerPanel extends UIComponent
                                         implements ActionListener
    {
    private static final String NL = System.getProperty("line.separator");
    private static final int iMaxRecent = 24;

    private DatabaseMetaData dMeta;
    private Statement sStatement;
    private JMenu mRecent;
    private String[] sRecent;
    private int iRecent;
    private JTextArea txtCommand;
    private JScrollPane txtCommandScroll;
    private JButton buttonExecute;
    private JTree tTree;
    private JScrollPane scrollPaneTree;
    private DefaultTreeModel treeModel;
    private TableModel tableModel;
    private DefaultMutableTreeNode rootNode;
    private JPanel pResult;
    private long lTime;
    private int iResult;        // 0: grid; 1: text
    private DataGrid gResult;
    private JTable gResultTable;
    private JScrollPane gScrollPane;
    private JTextArea txtResult;
    private JScrollPane txtResultScroll;
    private JSplitPane nsSplitPane;    // Contains query over results
    private JSplitPane ewSplitPane;    // Contains tree beside nsSplitPane
    private boolean bHelp;
    private static boolean bMustExit;
    private String ifHuge = "";
    JToolBar jtoolbar;

    // variables to hold the default cursors for these top level swing objects
    // so we can restore them when we exit our thread
    private Cursor fMainCursor;
    private Cursor txtCommandCursor;
    private Cursor txtResultCursor;

    /**
     * Wait Cursor
     */
    private static final Cursor waitCursor = new Cursor(Cursor.WAIT_CURSOR);

    private static String defScript;
    private static String defDirectory;
    private Connection connectionManager;


    /**
     * ********************************************************************************************
     *
     * @param connection
     */

    public DataStoreManagerPanel(final Connection connection)
        {
        super();

        connectionManager = connection;
        }


    public final void initialiseUI()
        {
        final JMenuBar bar = new JMenuBar();

        // used shortcuts: CERGTSIUDOLM
        final String[] fitems = {
            "-Connect...", "--", "-Open Script...", "-Save Script...",
            "-Save Result...", "--", "-Exit"
        };

        addMenu(bar, "File", fitems);

        final String[] vitems = {
            "RRefresh Tree", "--", "GResults in Grid", "TResults in Text"
        };

        addMenu(bar, "View", vitems);

        final String[] sitems = {
            "SSELECT", "IINSERT", "UUPDATE", "DDELETE", "EEXECUTE", "---",
            "-CREATE TABLE", "-DROP TABLE", "-CREATE INDEX", "-DROP INDEX",
            "--", "-CHECKPOINT", "-SCRIPT", "-SET", "-SHUTDOWN", "--",
            "-Test Script"
        };

        addMenu(bar, "Command", sitems);

        mRecent = new JMenu("Recent");

        bar.add(mRecent);

        final String[] soptions = {
            "-AutoCommit on", "-AutoCommit off", "OCommit", "LRollback", "--",
            "-Disable MaxRows", "-Set MaxRows to 100", "--", "-Logging on",
            "-Logging off", "--", "-Insert test data"
        };

        addMenu(bar, "Options", soptions);

        final String[] stools = {
            "-Dump", "-Restore", "-Transfer"
        };

        addMenu(bar, "Tools", stools);
        //setJMenuBar(bar);
        initGUI();

        sRecent = new String[iMaxRecent];

        // (ulrivo): load query from command line
        if (defScript != null)
            {
            if (defDirectory != null)
                {
                defScript = defDirectory + File.separator + defScript;
                }

            // if insert stmet is thousands of records...skip showing it
            // as text.  Too huge.
            final StringBuffer buf = new StringBuffer();

            ifHuge = DataStoreManagerCommon.readFile(defScript);

            if (4096 <= ifHuge.length())
                {
                buf.append("This huge file cannot be edited. Please execute\n");
                txtCommand.setText(buf.toString());
                }
            else
                {
                txtCommand.setText(ifHuge);
                }
            }

        txtCommand.requestFocus();

        if (connectionManager == null)
            {
            return;
            }

        connect(connectionManager);
        //refreshTree();
        }


    public final void disposeUI()
        {

        }

    /**
     * ********************************************************************************************
     * Run the UI of this UIComponent.
     */

    public final void runUI()
        {

        }


    /**
     * ********************************************************************************************
     * Stop the UI of this UIComponent.
     */

    public final void stopUI()
        {
        }


    private void connect(final Connection c)
        {
        try
            {
            if ((connectionManager != null)
                && (!connectionManager.isClosed()))
                {
                dMeta = connectionManager.getMetaData();
                sStatement = connectionManager.createStatement();

                refreshTree();
                }
            }
        catch (SQLException e)
            {
            connectionManager = null;
            }
        }


    public final void setMustExit(final boolean b)
        {
        this.bMustExit = b;
        }

    private void addMenu(final JMenuBar b, final String name, final String[] items)
        {

        final JMenu menu = new JMenu(name);

        addMenuItems(menu, items);
        b.add(menu);
        }

    private void addMenuItems(final JMenu menu, final String[] m)
        {

        final Dimension d = Toolkit.getDefaultToolkit().getScreenSize();

        for (int i = 0; i < m.length; i++)
            {
            if (m[i].equals("--"))
                {
                menu.addSeparator();
                }
            else if (m[i].equals("---"))
                {

                // (ulrivo): full size on screen with less than 640 width
                if (d.width >= 640)
                    {
                    menu.addSeparator();
                    }
                else
                    {
                    return;
                    }
                }
            else
                {
                final JMenuItem item = new JMenuItem(m[i].substring(1));
                final char c = m[i].charAt(0);

                if (c != '-')
                    {
                    final KeyStroke key =
                        KeyStroke.getKeyStroke(c, Event.CTRL_MASK);

                    item.setAccelerator(key);
                    }

                item.addActionListener(this);
                menu.add(item);
                }
            }
        }


    public final void actionPerformed(final ActionEvent ev)
        {

        String strCommand = ev.getActionCommand();

        if (strCommand == null)
            {
            if (ev.getSource() instanceof JMenuItem)
                {
                strCommand = ((JMenuItem) ev.getSource()).getText();
                }
            }

        if (strCommand == null)
            {
            }
        else if (strCommand.equals("Execute"))
            {
            execute();
            }
        else if (strCommand.equals("Exit"))
            {
            //windowClosing(null);
            }
        else if (strCommand.equals("Transfer"))
            {
            Transfer.work(null);
            }
        else if (strCommand.equals("Dump"))
            {
            Transfer.work(new String[]{"-d"});
            }
        else if (strCommand.equals("Restore"))
            {
            Transfer.work(new String[]{"-r"});
            }
        else if (strCommand.equals("Logging on"))
            {
            JavaSystem.setLogToSystem(true);
            }
        else if (strCommand.equals("Logging off"))
            {
            JavaSystem.setLogToSystem(false);
            }
        else if (strCommand.equals("Refresh Tree"))
            {
            refreshTree();
            }
        else if (strCommand.startsWith("#"))
            {
            final int i = Integer.parseInt(strCommand.substring(1));

            txtCommand.setText(sRecent[i]);
            }
        else if (strCommand.equals("Connect..."))
            {
            connect(connectionManager);
            }
        else if (strCommand.equals("Results in Grid"))
            {
            iResult = 0;

            pResult.removeAll();
            pResult.add(gScrollPane, BorderLayout.CENTER);
            pResult.doLayout();
            gResult.fireTableChanged(null);
            pResult.repaint();
            }
        else if (strCommand.equals("Open Script..."))
            {
            final JFileChooser f = new JFileChooser(".");

            f.setDialogTitle("Open Script...");

            // (ulrivo): set default directory if set from command line
            if (defDirectory != null)
                {
                f.setCurrentDirectory(new File(defDirectory));
                }

            final int option = f.showOpenDialog(this);

            if (option == JFileChooser.APPROVE_OPTION)
                {
                final File file = f.getSelectedFile();

                if (file != null)
                    {
                    final StringBuffer buf = new StringBuffer();

                    ifHuge = DataStoreManagerCommon.readFile(file.getAbsolutePath());

                    if (4096 <= ifHuge.length())
                        {
                        buf.append("This huge file cannot be edited. Please execute\n");
                        txtCommand.setText(buf.toString());
                        }
                    else
                        {
                        txtCommand.setText(ifHuge);
                        }
                    }
                }
            }
        else if (strCommand.equals("Save Script..."))
            {
            final JFileChooser f = new JFileChooser(".");

            f.setDialogTitle("Save Script");

            // (ulrivo): set default directory if set from command line
            if (defDirectory != null)
                {
                f.setCurrentDirectory(new File(defDirectory));
                }

            final int option = f.showSaveDialog(this);

            if (option == JFileChooser.APPROVE_OPTION)
                {
                final File file = f.getSelectedFile();

                if (file != null)
                    {
                    DataStoreManagerCommon.writeFile(file.getAbsolutePath(),
                                                    txtCommand.getText());
                    }
                }
            }
        else if (strCommand.equals("Save Result..."))
            {
            final JFileChooser f = new JFileChooser(".");

            f.setDialogTitle("Save Result...");

            // (ulrivo): set default directory if set from command line
            if (defDirectory != null)
                {
                f.setCurrentDirectory(new File(defDirectory));
                }

            final int option = f.showSaveDialog(this);

            if (option == JFileChooser.APPROVE_OPTION)
                {
                final File file = f.getSelectedFile();

                if (file != null)
                    {
                    showResultInText();
                    DataStoreManagerCommon.writeFile(file.getAbsolutePath(),
                                                    txtResult.getText());
                    }
                }
            }
        else if (strCommand.equals("Results in Text"))
            {
            iResult = 1;

            pResult.removeAll();
            pResult.add(txtResultScroll, BorderLayout.CENTER);
            pResult.doLayout();
            showResultInText();
            pResult.repaint();
            }
        else if (strCommand.equals("AutoCommit on"))
            {
            try
                {
                connectionManager.setAutoCommit(true);
                }
            catch (SQLException e)
                {
                }
            }
        else if (strCommand.equals("AutoCommit off"))
            {
            try
                {
                connectionManager.setAutoCommit(false);
                }
            catch (SQLException e)
                {
                }
            }
        else if (strCommand.equals("Commit"))
            {
            try
                {
                connectionManager.commit();
                }
            catch (SQLException e)
                {
                }
            }
        else if (strCommand.equals("Rollback"))
            {
            try
                {
                connectionManager.rollback();
                }
            catch (SQLException e)
                {
                }
            }
        else if (strCommand.equals("Disable MaxRows"))
            {
            try
                {
                sStatement.setMaxRows(0);
                }
            catch (SQLException e)
                {
                }
            }
        else if (strCommand.equals("Set MaxRows to 100"))
            {
            try
                {
                sStatement.setMaxRows(100);
                }
            catch (SQLException e)
                {
                }
            }
        else if (strCommand.equals("SELECT"))
            {
            showHelp(DataStoreManagerCommon.selectHelp);
            }
        else if (strCommand.equals("INSERT"))
            {
            showHelp(DataStoreManagerCommon.insertHelp);
            }
        else if (strCommand.equals("UPDATE"))
            {
            showHelp(DataStoreManagerCommon.updateHelp);
            }
        else if (strCommand.equals("DELETE"))
            {
            showHelp(DataStoreManagerCommon.deleteHelp);
            }
        else if (strCommand.equals("EXECUTE"))
            {
            execute();
            }
        else if (strCommand.equals("CREATE TABLE"))
            {
            showHelp(DataStoreManagerCommon.createTableHelp);
            }
        else if (strCommand.equals("DROP TABLE"))
            {
            showHelp(DataStoreManagerCommon.dropTableHelp);
            }
        else if (strCommand.equals("CREATE INDEX"))
            {
            showHelp(DataStoreManagerCommon.createIndexHelp);
            }
        else if (strCommand.equals("DROP INDEX"))
            {
            showHelp(DataStoreManagerCommon.dropIndexHelp);
            }
        else if (strCommand.equals("CHECKPOINT"))
            {
            showHelp(DataStoreManagerCommon.checkpointHelp);
            }
        else if (strCommand.equals("SCRIPT"))
            {
            showHelp(DataStoreManagerCommon.scriptHelp);
            }
        else if (strCommand.equals("SHUTDOWN"))
            {
            showHelp(DataStoreManagerCommon.shutdownHelp);
            }
        else if (strCommand.equals("SET"))
            {
            showHelp(DataStoreManagerCommon.setHelp);
            }
        else if (strCommand.equals("Test Script"))
            {
            showHelp(DataStoreManagerCommon.testHelp);
            }
        }

    private void showHelp(final String[] help)
        {

        txtCommand.setText(help[0]);

        bHelp = true;

        pResult.removeAll();
        pResult.add(txtResultScroll, BorderLayout.CENTER);
        pResult.doLayout();
        txtResult.setText(help[1]);
        pResult.repaint();
        txtCommand.requestFocus();
        txtCommand.setCaretPosition(help[0].length());
        }


    private void clear()
        {

        ifHuge = "";

        txtCommand.setText(ifHuge);
        }

    private static Thread runningThread;

    private void execute()
        {

        if (runningThread != null && runningThread.isAlive())
            {
            Toolkit.getDefaultToolkit().beep();

            return;
            }

        runningThread = new ExecuteThread();

        runningThread.start();
        }

    private void setWaiting(final boolean waiting)
        {

        if (waiting)
            {

            // save the old cursors
            if (fMainCursor == null)
                {
                //fMainCursor = fMain.getCursor();
                txtCommandCursor = txtCommand.getCursor();
                txtResultCursor = txtResult.getCursor();
                }

            // set the cursors to the wait cursor
            //fMain.setCursor(waitCursor);
            txtCommand.setCursor(waitCursor);
            txtResult.setCursor(waitCursor);
            }
        else
            {

            // restore the cursors we saved
            //fMain.setCursor(fMainCursor);
            txtCommand.setCursor(txtCommandCursor);
            txtResult.setCursor(txtResultCursor);
            }
        }

    private final class ExecuteThread extends Thread
        {

        public final void run()
            {

            setWaiting(true);
            gResult.clear();

            final String sCmd;

            if (4096 <= ifHuge.length())
                {
                sCmd = ifHuge;
                }
            else
                {
                sCmd = txtCommand.getText();
                }

            if (sCmd.startsWith("-->>>TEST<<<--"))
                {
                testPerformance();

                return;
                }

            final String[] g = new String[1];

            try
                {
                lTime = System.currentTimeMillis();

                sStatement.execute(sCmd);

                final int r = sStatement.getUpdateCount();

                if (r == -1)
                    {
                    formatResultSet(sStatement.getResultSet());
                    }
                else
                    {
                    g[0] = "update count";

                    gResult.setHead(g);

                    g[0] = "" + r;

                    gResult.addRow(g);
                    }

                lTime = System.currentTimeMillis() - lTime;

                addToRecent(txtCommand.getText());
                }
            catch (SQLException e)
                {
                lTime = System.currentTimeMillis() - lTime;
                g[0] = "SQL Error";

                gResult.setHead(g);

                String s = e.getMessage();

                s += " / Error Code: " + e.getErrorCode();
                s += " / State: " + e.getSQLState();
                g[0] = s;

                gResult.addRow(g);
                }

            // Call with invokeLater because these commands change the gui.
            // Do not want to be updating the gui outside of the AWT event
            // thread.
            // ToDo Consider SwingWorker
            SwingUtilities.invokeLater(new Thread(REGISTRY.getThreadGroup(),
                                                  "Thread DataStoreManangerPanel")
            {
            public void run()
                {

                updateResult();
                gResult.fireTableChanged(null);
                System.gc();
                setWaiting(false);
                }
            });
            }
        }

    private void updateResult()
        {
        if (iResult == 0)
            {

            // in case 'help' has removed the grid
            if (bHelp)
                {
                pResult.removeAll();
                pResult.add(gScrollPane, BorderLayout.CENTER);
                pResult.doLayout();
                gResult.fireTableChanged(null);
                pResult.repaint();

                bHelp = false;
                }
            }
        else
            {
            showResultInText();
            }

        txtCommand.selectAll();
        txtCommand.requestFocus();
        }

    private void formatResultSet(final ResultSet r)
        {

        if (r == null)
            {
            final String[] g = new String[1];

            g[0] = "Result";

            gResult.setHead(g);

            g[0] = "(empty)";

            gResult.addRow(g);

            return;
            }

        try
            {
            final ResultSetMetaData m = r.getMetaData();
            final int col = m.getColumnCount();
            final Object[] h = new Object[col];

            for (int i = 1; i <= col; i++)
                {
                h[i - 1] = m.getColumnLabel(i);
                }

            gResult.setHead(h);

            while (r.next())
                {
                for (int i = 1; i <= col; i++)
                    {
                    h[i - 1] = r.getObject(i);

                    if (r.wasNull())
                        {
                        h[i - 1] = null;    // = "(null)";
                        }
                    }

                gResult.addRow(h);
                }

            r.close();
            }
        catch (SQLException e)
            {
            }
        }

    private void testPerformance()
        {

        String all = txtCommand.getText();
        final StringBuffer b = new StringBuffer();
        long total = 0;

        for (int i = 0; i < all.length(); i++)
            {
            final char c = all.charAt(i);

            if (c != '\n')
                {
                b.append(c);
                }
            }

        all = b.toString();

        final String[] g = new String[4];

        g[0] = "ms";
        g[1] = "count";
        g[2] = "sql";
        g[3] = "error";

        gResult.setHead(g);

        int max = 1;

        lTime = System.currentTimeMillis() - lTime;

        while (!all.equals(""))
            {
            final int i = all.indexOf(';');
            final String sql;

            if (i != -1)
                {
                sql = all.substring(0, i);
                all = all.substring(i + 1);
                }
            else
                {
                sql = all;
                all = "";
                }

            if (sql.startsWith("--#"))
                {
                max = Integer.parseInt(sql.substring(3));

                continue;
                }
            else if (sql.startsWith("--"))
                {
                continue;
                }

            g[2] = sql;

            long l = 0;

            try
                {
                l = DataStoreManagerCommon.testStatement(sStatement, sql, max);
                total += l;
                g[0] = "" + l;
                g[1] = "" + max;
                g[3] = "";
                }
            catch (SQLException e)
                {
                g[0] = g[1] = "n/a";
                g[3] = e.toString();
                }

            gResult.addRow(g);
            System.out.println(l + " ms : " + sql);
            }

        g[0] = "" + total;
        g[1] = "total";
        g[2] = "";

        gResult.addRow(g);

        lTime = System.currentTimeMillis() - lTime;

        updateResult();
        }

    /**
     * Method declaration
     */
    private void showResultInText()
        {

        final Object[] col = gResult.getHead();
        final int width = col.length;
        final int[] size = new int[width];
        final Vector data = gResult.getData();
        Object row[];
        final int height = data.size();

        for (int i = 0; i < width; i++)
            {
            size[i] = col[i].toString().length();
            }

        for (int i = 0; i < height; i++)
            {
            row = (Object[]) data.elementAt(i);

            for (int j = 0; j < width; j++)
                {
                final int l = row[j].toString().length();

                if (l > size[j])
                    {
                    size[j] = l;
                    }
                }
            }

        final StringBuffer b = new StringBuffer();

        for (int i = 0; i < width; i++)
            {
            b.append(col[i]);

            for (int l = col[i].toString().length(); l <= size[i]; l++)
                {
                b.append(' ');
                }
            }

        b.append(NL);

        for (int i = 0; i < width; i++)
            {
            for (int l = 0; l < size[i]; l++)
                {
                b.append('-');
                }

            b.append(' ');
            }

        b.append(NL);

        for (int i = 0; i < height; i++)
            {
            row = (Object[]) data.elementAt(i);

            for (int j = 0; j < width; j++)
                {
                b.append(row[j]);

                for (int l = row[j].toString().length(); l <= size[j]; l++)
                    {
                    b.append(' ');
                    }
                }

            b.append(NL);
            }

        b.append(NL).append(height).append(" row(s) in ").append(lTime).append(" ms");
        txtResult.setText(b.toString());
        }

    private void addToRecent(String s)
        {

        for (int i = 0; i < iMaxRecent; i++)
            {
            if (s.equals(sRecent[i]))
                {
                return;
                }
            }

        if (sRecent[iRecent] != null)
            {
            mRecent.remove(iRecent);
            }

        sRecent[iRecent] = s;

        if (s.length() > 43)
            {
            s = s.substring(0, 40) + "...";
            }

        final JMenuItem item = new JMenuItem(s);

        item.setActionCommand("#" + iRecent);
        item.addActionListener(this);
        mRecent.insert(item, iRecent);

        iRecent = (iRecent + 1) % iMaxRecent;
        }

    private void initGUI()
        {

        final JPanel pCommand = new JPanel();

        pResult = new JPanel();
        nsSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, pCommand,
                                     pResult);

        pCommand.setLayout(new BorderLayout());
        pResult.setLayout(new BorderLayout());

        final Font fFont = new Font("Dialog", Font.PLAIN, 12);

        txtCommand = new JTextArea(10, 40);
        txtCommand.setMargin(new Insets(5, 5, 5, 5));

        txtCommandScroll = new JScrollPane(txtCommand);

        txtResult = new JTextArea(20, 40);
        txtResult.setMargin(new Insets(5, 5, 5, 5));

        txtResultScroll = new JScrollPane(txtResult);

        txtCommand.setFont(fFont);
        txtResult.setFont(new Font("Courier", Font.PLAIN, 12));

        buttonExecute = new JButton("Execute");
        buttonExecute.addActionListener(this);

        pCommand.add(buttonExecute, BorderLayout.EAST);
        pCommand.add(txtCommandScroll, BorderLayout.CENTER);

        gResult = new DataGrid();

        final TableSorter sorter = new TableSorter(gResult);

        tableModel = sorter;
        gResultTable = new JTable(sorter);

        sorter.setTableHeader(gResultTable.getTableHeader());

        gScrollPane = new JScrollPane(gResultTable);

        gResultTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        gResult.setJTable(gResultTable);

        pResult.add(gScrollPane, BorderLayout.CENTER);

        // Set up the tree
        rootNode = new DefaultMutableTreeNode("Connection");
        treeModel = new DefaultTreeModel(rootNode);
        tTree = new JTree(treeModel);
        scrollPaneTree = new JScrollPane(tTree);

        scrollPaneTree.setPreferredSize(new Dimension(120, 400));
        scrollPaneTree.setMinimumSize(new Dimension(70, 100));
        txtCommandScroll.setPreferredSize(new Dimension(360, 100));
        txtCommandScroll.setMinimumSize(new Dimension(180, 100));
        gScrollPane.setPreferredSize(new Dimension(460, 300));

        ewSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                                     scrollPaneTree, nsSplitPane);

        add(ewSplitPane, BorderLayout.CENTER);
        doLayout();
        }

    /* Simple tree node factory method - sets parent and user object.
     */
    private DefaultMutableTreeNode makeNode(final Object userObject,
                                            final MutableTreeNode parent)
        {

        final DefaultMutableTreeNode node = new DefaultMutableTreeNode(userObject);

        if (parent != null)
            {
            treeModel.insertNodeInto(node, parent, parent.getChildCount());
            }

        return node;
        }

    /* Clear all existing nodes from the tree model and rebuild from scratch.
     */
    private void refreshTree()
        {

        DefaultMutableTreeNode nodeProperties;

        // First clear the existing tree by simply enumerating
        // over the root node's children and removing them one by one.
        while (treeModel.getChildCount(rootNode) > 0)
            {
            final DefaultMutableTreeNode child = (DefaultMutableTreeNode) treeModel.getChild(rootNode, 0);

            treeModel.removeNodeFromParent(child);
            child.removeAllChildren();
            child.removeFromParent();
            }

        treeModel.nodeStructureChanged(rootNode);
        treeModel.reload();
        scrollPaneTree.repaint();

        // Now rebuild the tree below its root
        try
            {
            // Start by naming the root node from its URL:
            rootNode.setUserObject(dMeta.getURL());

            // get metadata about user tables by building a vector of table names
            final String[] usertables = {
                "TABLE", "GLOBAL TEMPORARY", "VIEW"
            };
            final ResultSet result = dMeta.getTables(null, null, null, usertables);
            final Vector tables = new Vector();

            final Vector remarks = new Vector();

            while (result.next())
                {
                tables.addElement(result.getString(3));
                remarks.addElement(result.getString(5));
                }

            result.close();

            // For each table, build a tree node with interesting info
            for (int i = 0; i < tables.size(); i++)
                {
                final String name = (String) tables.elementAt(i);
                final DefaultMutableTreeNode tableNode = makeNode(name, rootNode);
                final ResultSet rsColumns = dMeta.getColumns(null, null, name, null);

                final String remark = (String) remarks.elementAt(i);

                if ((remark != null) && !remark.trim().equals(""))
                    {
                    makeNode(remark, tableNode);
                    }

                // With a child for each column containing pertinent attributes
                while (rsColumns.next())
                    {
                    final String strName = rsColumns.getString(4);
                    final DefaultMutableTreeNode columnNode = makeNode(strName, tableNode);

                    final String type = rsColumns.getString(6);
                    makeNode("Type: " + type, columnNode);

                    final int intSize = rsColumns.getInt(7);
                    makeNode("Size: " + intSize, columnNode);

                    final boolean nullable = rsColumns.getInt(11) != DatabaseMetaData.columnNoNulls;
                    makeNode("Nullable: " + nullable, columnNode);
                    }

                rsColumns.close();

                final DefaultMutableTreeNode indexesNode = makeNode("Indices",
                                                                    tableNode);
                final ResultSet ind = dMeta.getIndexInfo(null, null, name, false, false);
                String oldiname = null;
                DefaultMutableTreeNode indexNode = null;

                // A child node to contain each index - and its attributes
                while (ind.next())
                    {
                    final boolean nonunique = ind.getBoolean(4);
                    final String iname = ind.getString(6);

                    if ((oldiname == null || !oldiname.equals(iname)))
                        {
                        indexNode = makeNode(iname, indexesNode);

                        makeNode("Unique: " + !nonunique, indexNode);

                        oldiname = iname;
                        }

                    // And the ordered column list for index components
                    makeNode(ind.getString(9), indexNode);
                    }

                ind.close();
                }

            // Finally - a little additional metadata on this connection
            nodeProperties = makeNode("Properties", rootNode);

            makeNode("User: " + dMeta.getUserName(), nodeProperties);
            makeNode("Catalog: " + connectionManager.getCatalog(), nodeProperties);
            makeNode("ReadOnly: " + connectionManager.isReadOnly(), nodeProperties);
            makeNode("AutoCommit: " + connectionManager.getAutoCommit(), nodeProperties);
            makeNode("Driver: " + dMeta.getDriverName(), nodeProperties);
            makeNode("Product: " + dMeta.getDatabaseProductName(), nodeProperties);
            makeNode("Version: " + dMeta.getDatabaseProductVersion(), nodeProperties);
            }

        catch (SQLException se)
            {
            nodeProperties = makeNode("Error getting metadata:", rootNode);

            makeNode(se.getMessage(), nodeProperties);
            makeNode(se.getSQLState(), nodeProperties);
            }

        treeModel.nodeStructureChanged(rootNode);
        treeModel.reload();
        scrollPaneTree.repaint();
        }
    }
