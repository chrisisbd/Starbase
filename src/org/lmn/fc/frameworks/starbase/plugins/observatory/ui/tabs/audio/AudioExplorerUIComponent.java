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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.audio;


import org.lmn.fc.common.exceptions.FrameworkException;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryUIInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.AudioExplorerExpanderDataInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.AudioExplorerLeafDataInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.AudioExplorerListUIComponentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.AudioExplorerUIComponentInterface;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.model.registry.NavigationUtilities;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;
import org.lmn.fc.ui.UIComponentPlugin;
import org.lmn.fc.ui.components.BlankUIComponent;
import org.lmn.fc.ui.components.JSplitPaneAdapter;
import org.lmn.fc.ui.components.UIComponent;
import org.lmn.fc.ui.reports.ReportTablePlugin;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;


/**************************************************************************************************
 * AudioExplorerUIComponent.
 *
 * NOTE!
 * The original of this code is by Andrew Thompson http://pscode.org
 * as published at: http://stackoverflow.com/questions/5304001/javasound-mixer-with-both-ports-and-datalines
 *
 * This package is an extensive rework to get things working in a Starbase way.
 */

public final class AudioExplorerUIComponent extends UIComponent
                                            implements AudioExplorerUIComponentInterface
    {
    private static final long serialVersionUID = -469868766197757553L;

    private static final double DEFAULT_DIVIDER_LOCATION = 50.0;
    private static final Dimension DIM_DIVIDER = new Dimension(270, 150);

    // Injections
    private final TaskPlugin pluginTask;
    private final ObservatoryUIInterface observatoryUI;
    private final ObservatoryInstrumentInterface hostInstrument;
    private final FontInterface pluginFont;
    private final ColourInterface pluginColour;
    private final String strResourceKey;

    // UI
    private JSplitPane splitPane;
    private JSplitPaneAdapter adapter;

    private JTree treeExplorer;
    private TreeModel treeModel;
    private TreeCellRenderer treeCellRenderer;
    private DefaultMutableTreeNode treenodeSelected;
    private JScrollPane scrollpaneTree;

    private final UIComponentPlugin theExpander;
    private final UIComponentPlugin theLeaf;
    private UIComponentPlugin selectedExpander;
    private UIComponentPlugin selectedLeaf;

    private double dblDividerLocation;      // The position of the divider as a percentage of width
    private boolean boolSortable;
    private boolean boolCustomIcons;


    /***********************************************************************************************
     * Construct a AudioExplorerUIComponent.
     *
     * @param hostinstrument
     * @param instrumentxml
     * @param hostui
     * @param task
     * @param font
     * @param colour
     * @param resourcekey
     * @param debug
     */

    public AudioExplorerUIComponent(final ObservatoryInstrumentInterface hostinstrument,
                                    final Instrument instrumentxml,
                                    final ObservatoryUIInterface hostui,
                                    final TaskPlugin task,
                                    final FontInterface font,
                                    final ColourInterface colour,
                                    final String resourcekey,
                                    final boolean debug)
        {
        // UIComponent has a BorderLayout
        super();

        // Injections
        this.pluginTask = task;
        this.observatoryUI = hostui;
        this.hostInstrument = hostinstrument;
        this.pluginFont = font;
        this.pluginColour = colour;
        this.strResourceKey = resourcekey;
        setDebug(debug);

        // UI
        this.splitPane = null;
        this.treeExplorer = null;
        this.scrollpaneTree = null;

        this.treeModel = null;
        this.treeCellRenderer = null;
        this.treenodeSelected = null;

        this.dblDividerLocation = DEFAULT_DIVIDER_LOCATION;
        this.boolSortable = false;
        this.boolCustomIcons = true;

        this.theExpander = new AudioExplorerListUIComponent(getHostTask(),
                                                            getHostInstrument(),
                                                            null,
                                                            null,
                                                            getFontData(),
                                                            getColourData(),
                                                            getResourceKey(),
                                                            isDebug());

        this.theLeaf = new AudioExplorerListUIComponent(getHostTask(),
                                                        getHostInstrument(),
                                                        null,
                                                        null,
                                                        getFontData(),
                                                        getColourData(),
                                                        getResourceKey(),
                                                        isDebug());
        this.selectedExpander = null;
        this.selectedLeaf = null;
        }


    /**********************************************************************************************/
    /* UIComponent State                                                                          */
    /***********************************************************************************************
     * Initialise this UIComponent.
     */

    public final void initialiseUI()
        {
        // DO NOT USE super.initialiseUI()

        // Get the latest Resources
        readResources();

        // Colours
        setBackground(DEFAULT_COLOUR_CANVAS.getColor());

        createUIComponents();

        // Remove everything from the host UIComponent
        removeAll();

        if (getSplitPane() != null)
            {
            add(getSplitPane(), BorderLayout.CENTER);
            }

        // Initialise all Components
        if (getSelectedExpanderUI() != null)
            {
            getSelectedExpanderUI().initialiseUI();
            }
        }


    /***********************************************************************************************
     * Run this UIComponent.
     */

    public void runUI()
        {
        super.runUI();

        // Set the SplitPane divider location as a percentage of the screen width,
        // from data saved in the RegistryModel the last time we were here
        // Reset the position of the divider, but not until we are visible
        setAndSaveDividerLocation(dblDividerLocation);

        // Update all components of the UI
        validateAndUpdateUI();

        if (getSelectedExpanderUI() != null)
            {
            getSelectedExpanderUI().runUI();
            }
        }


    /***********************************************************************************************
     * Stop this UIComponent.
     */

    public void stopUI()
        {
        super.stopUI();

        if (getSelectedExpanderUI() != null)
            {
            getSelectedExpanderUI().stopUI();
            }
        }


    /***********************************************************************************************
     * Dispose of this UIComponent.
     */

    public final void disposeUI()
        {
        super.disposeUI();

        if (getTheExpanderUI() != null)
            {
            getTheExpanderUI().disposeUI();
            }

        if (getSelectedExpanderUI() != null)
            {
            getSelectedExpanderUI().disposeUI();
            }

        if (getTheLeafUI() != null)
            {
            getTheLeafUI().disposeUI();
            }

        if (getSelectedLeafUI() != null)
            {
            getSelectedLeafUI().disposeUI();
            }

        // Remove the tree data
        setExplorerTreeModel(null);
        setExplorerTree(null);
        setSelectedTreeNode(null);
        setTreeCellRenderer(null);
        }


    /**********************************************************************************************/
    /* UI Components                                                                              */
    /***********************************************************************************************
     * Create the components for the UI on the specified JPanel.
     */

    private void createUIComponents()
        {
        final JSplitPane jSplitPane;

        // Create the JTree for the explorer, and register it with the ToolTipManager
        // Return with a selected Node
        // This is the only call to createExplorerTree()

        setExplorerTree(createExplorerTree());
        setExplorerTreeModel(getExplorerTree().getModel());
        ToolTipManager.sharedInstance().registerComponent(getExplorerTree());

        // Allow customized node icons if enabled
        // Do this every time the panel is requested, to ensure state is up to date
        resetCellRenderer(getExplorerTree());

        // Make a scrolling pane for the tree, because it will get large
        scrollpaneTree = new JScrollPane(getExplorerTree());
        scrollpaneTree.setBackground(Color.WHITE);

        // Set size limits for the split
        getScrollPaneTree().setMinimumSize(DIM_DIVIDER);
        getScrollPaneTree().setPreferredSize(DIM_DIVIDER);

        // Add the Tree scrolling pane and a blank panel to the split pane, and initialise

        jSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                                    getScrollPaneTree(),
                                    new BlankUIComponent());
        jSplitPane.setOneTouchExpandable(true);
        jSplitPane.setContinuousLayout(false);
        jSplitPane.setDividerSize(SIZE_SPLIT_PANE_DIVIDER);
        // The right component gets all the extra space
        jSplitPane.setResizeWeight(0.0);
        setSplitPane(jSplitPane);
        ToolTipManager.sharedInstance().registerComponent(getSplitPane());

        // Listen for changes in the Split Pane divider location
        adapter = new JSplitPaneAdapter(getSplitPane(),
                                        getScrollPaneTree(),
                                        REGISTRY,
                                        getResourceKey() + KEY_DIMENSION_DIVIDER_LOCATION);
        getSplitPane().addPropertyChangeListener(JSplitPane.LAST_DIVIDER_LOCATION_PROPERTY,
                                                 getAdapter());

        // Create the UI with an Expander showing
        // Leave it to the caller to initialise
        getSplitPane().setRightComponent((Component)((AudioExplorerExpanderDataInterface)getSelectedTreeNode().getUserObject()).getUIComponent());
        getSplitPane().setVisible(true);

        // Always start with an Expander, and no Leaf
        setSelectedExpanderUI((UIComponentPlugin) getSplitPane().getRightComponent());
        setSelectedLeafUI(null);

        // Double check that we have the right kind of UIComponent
        // Leave it to runUI() to refresh
        if (getSelectedExpanderUI() instanceof AudioExplorerListUIComponentInterface)
            {
            ((AudioExplorerListUIComponentInterface) getSelectedExpanderUI()).setColumnNames(((AudioExplorerExpanderDataInterface) getSelectedTreeNode().getUserObject()).getColumnNames());
            ((AudioExplorerListUIComponentInterface) getSelectedExpanderUI()).setData(((AudioExplorerExpanderDataInterface) getSelectedTreeNode().getUserObject()).getData());

            // We must re-initialise in order to redraw the column names
            getSelectedExpanderUI().initialiseUI();
            }
        }


    /***********************************************************************************************
     * Create the Explorer JTree.
     * Leave with the Root selected.
     *
     * @return JTree
     */

    private JTree createExplorerTree()
        {
        final DefaultMutableTreeNode root;
        final TreeModel treeModelExplorer;
        final JTree tree;

        root = AudioExplorerTreeBuilder.buildExplorerTreeNodes(getTheExpanderUI(),
                                                               getTheLeafUI(),
                                                               null);

        // Now set up the TreeModel to allow examination of the data
        // and make sure that the tree uses only DefaultMutableTreeNodes
        treeModelExplorer = new DefaultTreeModel(root);

        tree = new JTree(treeModelExplorer)
            {
            // Override the JTree.getToolTipText(), to give separate text for each Node
            public String getToolTipText(final MouseEvent mouseEvent)
                {
                // Returns -1 if any of the elements in path are hidden under a collapsed parent
                if (getRowForLocation(mouseEvent.getX(), mouseEvent.getY()) == -1)
                    {
                    return (null);
                    }
                else
                    {
                    final TreePath curPath;
                    final DefaultMutableTreeNode lastPathComponent;
                    final Object userObject;
                    String strTooltip;

                    strTooltip = EMPTY_STRING;

                    // There's a valid Node under the mouse
                    curPath = getPathForLocation(mouseEvent.getX(), mouseEvent.getY());

                    // Get the last Node of the path
                    lastPathComponent = (DefaultMutableTreeNode) curPath.getLastPathComponent();

                    // Recover ToolTip text from the UserObjects
                    userObject = lastPathComponent.getUserObject();

                    if (userObject instanceof AudioExplorerExpanderDataInterface)
                        {
                        strTooltip = ((AudioExplorerExpanderDataInterface)userObject).getPathname();
                        }
                    else if (userObject instanceof AudioExplorerLeafDataInterface)
                        {
                        strTooltip = ((AudioExplorerLeafDataInterface)userObject).getPathname();
                        }

                    return (strTooltip);
                    }
                }
            };

        // Set up for selections from the Split Pane tree

        // Only one Node at a time may be selected
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        // Listen for Mouse events
        final MouseListener listenerMouse = new MouseAdapter()
            {
            public void mousePressed(final MouseEvent mouseEvent)
                {
                final String SOURCE = "AudioExplorerExplorerUIComponent.createExplorerTree() mousePressed() ";

                if (tree != null)
                    {
                    final DefaultMutableTreeNode nodeSelected;
                    final int rowClicked;

                    // Returns -1 if the location is not within the bounds of a displayed cell
                    rowClicked = tree.getRowForLocation(mouseEvent.getX(),
                                                        mouseEvent.getY());
                    if (rowClicked != -1)
                        {
                        final TreePath treePath;

                        treePath = tree.getPathForRow(rowClicked);

                        if (treePath != null)
                            {
                            nodeSelected = (DefaultMutableTreeNode) treePath.getLastPathComponent();

                            if (SwingUtilities.isLeftMouseButton(mouseEvent)
                                && (!mouseEvent.isShiftDown())
                                && (!mouseEvent.isAltDown())
                                && (mouseEvent.getClickCount() == 1)
                                && (nodeSelected != null))
                                {
                                // Make the new selection
                                treeSelection(mouseEvent,
                                              nodeSelected,
                                              tree.getRowBounds(rowClicked));
                                }
                            else
                                {
                                // Invalid tree node or mouse state
                                }
                            }
                        else
                            {
                            // NULL tree path
                            }
                        }
                    else
                        {
                        // Selection not on node
                        }
                    }
                else
                    {
                    LOGGER.error(SOURCE + "TREE is NULL");
                    }
                }
            };

        tree.addMouseListener(listenerMouse);

        tree.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        for (int intRowIndex = 0;
             intRowIndex < tree.getRowCount();
             intRowIndex++)
            {
            tree.expandRow(intRowIndex);
            }

        // Always start at the root, which must be on row 0
        tree.setRootVisible(true);
        tree.scrollRowToVisible(0);
        tree.setSelectionPath(tree.getPathForRow(0));
        setSelectedTreeNode((DefaultMutableTreeNode)tree.getModel().getRoot());

        tree.setForeground(getColourData().getColor());
        tree.setFont(getFontData().getFont());

        return (tree);
        }


    /***********************************************************************************************
     * Process the Tree selection.
     *
     * @param event The MouseEvent causing the selection
     * @param treenode The tree node being selected
     * @param rectangle The selected area
     */

    private void treeSelection(final MouseEvent event,
                               final DefaultMutableTreeNode treenode,
                               final Rectangle rectangle)
        {
        final String SOURCE = "AudioExplorerUIComponent.treeSelection() ";

        // Update the resources on every click...
        readResources();

        if (treenode != null)
            {
            final Object userObject;

            userObject = treenode.getUserObject();

            // See if the user clicked an Expander or a Leaf

            if (userObject instanceof AudioExplorerExpanderDataInterface)
                {
                // Stop any existing Leaf...
                if (getSelectedLeafUI() != null)
                    {
                    getSelectedLeafUI().stopUI();
                    setSelectedLeafUI(null);
                    }

                // But also stop any existing Expander...
                if (getSelectedExpanderUI() != null)
                    {
                    getSelectedExpanderUI().stopUI();
                    }

                // ...and set up the new one
                getSplitPane().setRightComponent((Component)((AudioExplorerExpanderDataInterface)userObject).getUIComponent());
                getSplitPane().setVisible(true);

                // Record the new selection
                getExplorerTree().setSelectionPath(new TreePath(treenode.getPath()));
                setSelectedTreeNode(treenode);

                // Set and run the new Expander
                setSelectedExpanderUI((UIComponentPlugin) getSplitPane().getRightComponent());

                // Double check that we have the right kind of UIComponent
                if ((getSelectedExpanderUI() != null)
                    && (getSelectedExpanderUI() instanceof AudioExplorerListUIComponentInterface))
                    {
                    ((AudioExplorerListUIComponentInterface) getSelectedExpanderUI()).setColumnNames(((AudioExplorerExpanderDataInterface) getSelectedTreeNode().getUserObject()).getColumnNames());
                    ((AudioExplorerListUIComponentInterface) getSelectedExpanderUI()).setData(((AudioExplorerExpanderDataInterface) getSelectedTreeNode().getUserObject()).getData());

                    // We must re-initialise in order to redraw the column names
                    getSelectedExpanderUI().initialiseUI();
                    getSelectedExpanderUI().runUI();
                    }
                }
            else if (userObject instanceof AudioExplorerLeafDataInterface)
                {
                // Stop any existing Expander...
                if (getSelectedExpanderUI() != null)
                    {
                    getSelectedExpanderUI().stopUI();
                    setSelectedExpanderUI(null);
                    }

                // ...and Leaf
                if (getSelectedLeafUI() != null)
                    {
                    getSelectedLeafUI().stopUI();
                    }

                getSplitPane().setRightComponent((Component)((AudioExplorerLeafDataInterface)userObject).getUIComponent());
                getSplitPane().setVisible(true);

                // Record the new selection
                getExplorerTree().setSelectionPath(new TreePath(treenode.getPath()));
                setSelectedTreeNode(treenode);

                // Set, initialise and run the new Leaf
                setSelectedLeafUI((UIComponentPlugin) getSplitPane().getRightComponent());

                // Double check that we have the right kind of UIComponent
                if ((getSelectedLeafUI() != null)
                    && (getSelectedLeafUI() instanceof AudioExplorerListUIComponentInterface))
                    {
                    ((AudioExplorerListUIComponentInterface) getSelectedLeafUI()).setColumnNames(((AudioExplorerLeafDataInterface) getSelectedTreeNode().getUserObject()).getColumnNames());
                    ((AudioExplorerListUIComponentInterface) getSelectedLeafUI()).setData(((AudioExplorerLeafDataInterface) getSelectedTreeNode().getUserObject()).getData());

                    // We must re-initialise in order to redraw the column names
                    getSelectedLeafUI().initialiseUI();
                    getSelectedLeafUI().runUI();
                    }
                }
            else
                {
                // The UserObject is not of the correct type...
                LOGGER.error(SOURCE + "Unrecognised subclass of UserObject on Tree");
                // ToDO exception
                throw new FrameworkException(REGISTRY.getException("TODO_MODEL_CORRUPT"));
                }

            // Now repaint everything...
            validateAndUpdateUI();
            }
        else
            {
            LOGGER.error(SOURCE + "NULL TreeNode or no selection");
            }
        }

    /***********************************************************************************************
     /* Utilities                                                                                  */
    /***********************************************************************************************
     * Get the selected TreeNode.
     *
     * @return DefaultMutableTreeNode
     */

    public final DefaultMutableTreeNode getSelectedTreeNode()
        {
        return (this.treenodeSelected);
        }


    /***********************************************************************************************
     * Set the selected TreeNode.
     *
     * @param treenode
     */

    public final void setSelectedTreeNode(final DefaultMutableTreeNode treenode)
        {
        this.treenodeSelected = treenode;
        }


    /***********************************************************************************************
     * Get the TreeModel, used by the JTree.
     *
     * @return TreeModel
     */

    private TreeModel getExplorerTreeModel()
        {
        return (this.treeModel);
        }


    /***********************************************************************************************
     * Set the TreeModel, used by the JTree.
     *
     * @param model
     */

    private void setExplorerTreeModel(final TreeModel model)
        {
        this.treeModel = model;
        }


    /***********************************************************************************************
     * Get the Explorer JTree.
     *
     * @return JTree
     */

    public final JTree getExplorerTree()
        {
        return (this.treeExplorer);
        }


    /***********************************************************************************************
     * Set the Explorer JTree.
     *
     * @param tree
     */

    public final void setExplorerTree(final JTree tree)
        {
        this.treeExplorer = tree;
        }


    /***********************************************************************************************
     * Get the TreeCellRenderer.
     *
     * @return TreeCellRenderer
     */

    private TreeCellRenderer getTreeCellRenderer()
        {
        return (this.treeCellRenderer);
        }


    /***********************************************************************************************
     * Set the TreeCellRenderer.
     *
     * @param renderer
     */

    private void setTreeCellRenderer(final TreeCellRenderer renderer)
        {
        this.treeCellRenderer = renderer;
        }


    /***********************************************************************************************
     * Reset the TreeCellRenderer.
     * This is required when changing Look&Feel.
     *
     * @param tree
     */

    public final void resetCellRenderer(final JTree tree)
        {
        if (boolCustomIcons)
            {
            setTreeCellRenderer(new AudioExplorerCellRenderer());
            }
        else
            {
            setTreeCellRenderer(new DefaultTreeCellRenderer());
            }

        // Set the Tree cell renderer
        if ((getTreeCellRenderer() != null)
            && (tree != null))
            {
            tree.setCellRenderer(getTreeCellRenderer());

            if (tree.getCellRenderer() instanceof DefaultTreeCellRenderer)
                {
                final DefaultTreeCellRenderer renderer;

                renderer = (DefaultTreeCellRenderer)(tree.getCellRenderer());


                renderer.setBackgroundNonSelectionColor(Color.white);
                renderer.setTextNonSelectionColor(getColourData().getColor());

                renderer.setBorderSelectionColor(Color.red);
                renderer.setBackgroundSelectionColor(DEFAULT_COLOUR_TAB_BACKGROUND.getColor());
                renderer.setTextSelectionColor(Color.black);

                renderer.setFont(getFontData().getFont());
                }
            }
        }


    /***********************************************************************************************
     * Get the JSplitPaneAdapter.
     *
     * @return JSplitPaneAdapter
     */

    private JSplitPaneAdapter getAdapter()
        {
        return (this.adapter);
        }


    /***********************************************************************************************
     * Get the ScrollPane which holds the navigation JTree.
     *
     * @return JScrollPane
     */

    private JScrollPane getScrollPaneTree()
        {
        return (this.scrollpaneTree);
        }


    /***********************************************************************************************
     * Get the JSplitPane.
     *
     * @return JSplitPane
     */

    public final JSplitPane getSplitPane()
        {
        return (this.splitPane);
        }


    /***********************************************************************************************
     * Set the JSplitPane.
     *
     * @param splitpane
     */

    public final void setSplitPane(final JSplitPane splitpane)
        {
        this.splitPane = splitpane;
        }


    /***********************************************************************************************
     * Get the Explorer ReportTable occupant.
     *
     * @return ReportTablePlugin
     */

    public ReportTablePlugin getReportTable()
        {
        ReportTablePlugin printable;

        printable = null;

        if ((getSplitPane() != null)
            && (getSplitPane().getRightComponent() instanceof ReportTablePlugin))
            {
            // This should be either AudioExplorerListUIComponent or AudioExplorerListUIComponent
            printable = (ReportTablePlugin)getSplitPane().getRightComponent();
            }

        return (printable);
        }


    /***********************************************************************************************
     * Get the singleton ExpanderUI.
     *
     * @return UIComponentPlugin
     */

    public UIComponentPlugin getTheExpanderUI()
        {
        return (this.theExpander);
        }


    /***********************************************************************************************
     * Get the Selected ExpanderUI.
     *
     * @return UIComponentPlugin
     */

    public UIComponentPlugin getSelectedExpanderUI()
        {
        return (this.selectedExpander);
        }


    /***********************************************************************************************
     * Set the Selected ExpanderUI.
     *
     * @param expanderui
     */

    private void setSelectedExpanderUI(final UIComponentPlugin expanderui)
        {
        this.selectedExpander = expanderui;
        }


    /***********************************************************************************************
     * Get the singleton LeafUI.
     *
     * @return UIComponentPlugin
     */

    public UIComponentPlugin getTheLeafUI()
        {
        return (this.theLeaf);
        }


    /***********************************************************************************************
     * Get the Selected LeafUI.
     *
     * @return UIComponentPlugin
     */

    public UIComponentPlugin getSelectedLeafUI()
        {
        return (this.selectedLeaf);
        }


    /***********************************************************************************************
     * Set the Selected LeafUI.
     *
     * @param leafui
     */

    private void setSelectedLeafUI(final UIComponentPlugin leafui)
        {
        this.selectedLeaf = leafui;
        }


    /***********************************************************************************************
     * Validate and Update the UI of all components.
     */

    public final void validateAndUpdateUI()
        {
        if (getExplorerTree() != null)
            {
            // Re-create the TreeCellRenderer()
            resetCellRenderer(getExplorerTree());

            getExplorerTree().revalidate();
            }

        // Ensure that everything in the Panel is up to date
        NavigationUtilities.updateComponentTreeUI(this);
        }


    /***********************************************************************************************
     * Set the position of the SplitPane divider to the specified percentage,
     * and save in the RegistryModel.
     *
     * @param dividerpercentage
     */

    public void setAndSaveDividerLocation(final double dividerpercentage)
        {
        final double dblLocation;

        // Save the percentage location of the divider
        // Trap the careless...
        if ((dividerpercentage < 0.0) || (dividerpercentage > 100.0))
            {
            dblLocation = 50.0;
            }
        else
            {
            dblLocation = dividerpercentage;
            }
        }


    /***********************************************************************************************
     * Read all the Resources required by the AudioExplorerUIComponent.
     */

    private void readResources()
        {

        }


    /**********************************************************************************************/
    /* Injections                                                                                 */
    /**********************************************************************************************
     * Get the Task on which this Report is based.
     *
     * @return TaskData
     */

    private TaskPlugin getHostTask()
        {
        return (this.pluginTask);
        }


    /**********************************************************************************************
     * Get the host ObservatoryUI.
     *
     * @return ObservatoryUIInterface
     */

    public ObservatoryUIInterface getObservatoryUI()
        {
        return (this.observatoryUI);
        }


    /**********************************************************************************************
     * Get the ObservatoryInstrument to which this UIComponent is attached.
     *
     * @return ObservatoryInstrumentInterface
     */

    private ObservatoryInstrumentInterface getHostInstrument()
        {
        return (this.hostInstrument);
        }


    /**********************************************************************************************
     * Get the FontDataType.
     *
     * @return FontPlugin
     */

    private FontInterface getFontData()
        {
        return (this.pluginFont);
        }


    /**********************************************************************************************
     * Get the ColourDataType.
     *
     * @return ColourPlugin
     */

    private ColourInterface getColourData()
        {
        return (this.pluginColour);
        }


    /**********************************************************************************************
     * Get the ResourceKey for the Report.
     *
     * @return String
     */

    private String getResourceKey()
        {
        return (this.strResourceKey);
        }
    }