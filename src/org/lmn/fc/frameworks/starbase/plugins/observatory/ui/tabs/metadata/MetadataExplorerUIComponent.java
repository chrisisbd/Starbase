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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.metadata;

import org.lmn.fc.common.exceptions.FrameworkException;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.MetadataHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.*;
import org.lmn.fc.model.registry.NavigationUtilities;
import org.lmn.fc.model.root.RootPlugin;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;
import org.lmn.fc.ui.UIComponentPlugin;
import org.lmn.fc.ui.components.BlankUIComponent;
import org.lmn.fc.ui.components.JSplitPaneAdapter;
import org.lmn.fc.ui.components.UIComponent;
import org.lmn.fc.ui.components.UIComponentHelper;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;


/***************************************************************************************************
 * MetadataExplorerUIComponent.
 */

public final class MetadataExplorerUIComponent extends UIComponent
                                               implements MetadataExplorerUIComponentInterface
    {
    private static final long serialVersionUID = 2986454710235268769L;

    private static final double DEFAULT_DIVIDER_LOCATION = 50.0;
    private static final Dimension DIM_DIVIDER = new Dimension(200, 150);

    // Injections
    private final RootPlugin hostTask;
    private final ObservatoryInstrumentInterface hostInstrument;
    private final JToolBar toolbarCustom;
    private List<Metadata> listMetadata;
    private final String strResourceKey;

    // UI
    private JToolBar toolBar;
    private JSplitPane splitPane;
    private JTree treeMetadata;
    private DefaultTreeModel treeModel;
    private TreeCellRenderer treeCellRenderer;
    private DefaultMutableTreeNode treenodeSelected;
    private JScrollPane scrollpaneTree;
    private JSplitPaneAdapter adapter;
    private double dblDividerLocation;      // The position of the divider as a percentage of width
    private boolean boolCustomIcons;        // Controls custom icons in the JTree

    private final MetadataExpanderUIComponentInterface theExpander;
    private final MetadataLeafUIComponentInterface theLeaf;
    private MetadataExpanderUIComponentInterface selectedExpander;
    private MetadataLeafUIComponentInterface selectedLeaf;


    /***********************************************************************************************
     * Construct an MetadataExplorerUIComponent, using a custom Toolbar (which may be NULL).
     * The metadata is contained in the List of Metadata (name, value, units, type)
     * The ResourceKey is always that of the host Framework, since this is a general utility.
     *
     * @param task
     * @param hostinstrument
     * @param toolbar
     * @param metadata
     * @param hostresourcekey
     * @param debug
     */

    public MetadataExplorerUIComponent(final TaskPlugin task,
                                       final ObservatoryInstrumentInterface hostinstrument,
                                       final JToolBar toolbar,
                                       final List<Metadata> metadata,
                                       final String hostresourcekey,
                                       final boolean debug)
        {
        super();

        if ((task == null)
            || (hostinstrument == null)
            || (hostresourcekey == null)
            || (EMPTY_STRING.equals(hostresourcekey)))
            {
            throw new FrameworkException("MetadataExplorerUIComponent " + EXCEPTION_PARAMETER_INVALID);
            }

        // Injections
        this.hostTask = task;
        this.hostInstrument = hostinstrument;
        this.toolbarCustom = toolbar;
        this.listMetadata = metadata;
        this.strResourceKey = hostresourcekey;
        setDebug(debug);

        // UI
        this.toolBar = null;
        this.splitPane = null;
        this.treeMetadata = null;
        this.scrollpaneTree = null;

        this.dblDividerLocation = DEFAULT_DIVIDER_LOCATION;
        this.boolCustomIcons = true;

        // Make one MetadataExpanderUIComponent to use everywhere
        this.theExpander = new MetadataExpanderUIComponent(task,
                                                           hostinstrument,
                                                           metadata,
                                                           hostresourcekey);

        // Make one MetadataLeafUIComponent to use everywhere
        this.theLeaf = new MetadataLeafUIComponent(hostinstrument, true);

        this.selectedExpander = null;
        this.selectedLeaf = null;
        }


    /***********************************************************************************************
     * Construct an MetadataExplorerUIComponent, with no Toolbar.
     * The metadata is contained in the List of Metadata (name, value, units, type)
     * The ResourceKey is always that of the host Framework, since this is a general utility.
     *
     * @param task
     * @param hostinstrument
     * @param metadata
     * @param hostresourcekey
     * @param debug
     */

    public MetadataExplorerUIComponent(final TaskPlugin task,
                                       final ObservatoryInstrumentInterface hostinstrument,
                                       final List<Metadata> metadata,
                                       final String hostresourcekey,
                                       final boolean debug)
        {
        super();

        if ((task == null)
            || (hostinstrument == null)
            || (hostresourcekey == null)
            || (EMPTY_STRING.equals(hostresourcekey)))
            {
            throw new FrameworkException("MetadataExplorerUIComponent " + EXCEPTION_PARAMETER_INVALID);
            }

        // Injections
        this.hostTask = task;
        this.hostInstrument = hostinstrument;
        this.toolbarCustom = null;
        this.listMetadata = metadata;
        this.strResourceKey = hostresourcekey;
        setDebug(debug);

        // UI
        this.toolBar = null;
        this.splitPane = null;
        this.treeMetadata = null;
        this.scrollpaneTree = null;

        this.dblDividerLocation = DEFAULT_DIVIDER_LOCATION;
        this.boolCustomIcons = true;

        // Make one MetadataExpanderUIComponent to use everywhere
        this.theExpander = new MetadataExpanderUIComponent(task,
                                                           hostinstrument,
                                                           metadata,
                                                           hostresourcekey);

        // Make one MetadataLeafUIComponent to use everywhere
        this.theLeaf = new MetadataLeafUIComponent(hostinstrument, true);

        this.selectedExpander = null;
        this.selectedLeaf = null;
        }


    /**********************************************************************************************/
    /* UIComponent State                                                                          */
    /***********************************************************************************************
     * Initialise this UIComponent.
     */

    public void initialiseUI()
        {
        super.initialiseUI();

        // Get the latest Resources
        readResources();

        // Create all UI components
        createUIComponents();

        // Remove everything from the host UIComponent
        removeAll();

        // There may not be a Toolbar
        if (getToolBar() != null)
            {
            add(getToolBar(), BorderLayout.NORTH);
            }

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

        if (getSelectedLeafUI() != null)
            {
            getSelectedLeafUI().runUI();
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

        if (getSelectedLeafUI() != null)
            {
            getSelectedLeafUI().stopUI();
            }
        }


    /***********************************************************************************************
     * Dispose of all components of this UIComponent.
     */

    public void disposeUI()
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
        setTreeModel(null);
        setMetadataTree(null);
        setSelectedTreeNode(null);
        }


    /**********************************************************************************************/
    /* UI Components                                                                              */
    /***********************************************************************************************
     * Create all screen components.
     */

    private void createUIComponents()
        {
        final String SOURCE = "MetadataExplorerUIComponent.createUIComponents() ";
        final JSplitPane jSplitPane;

        // Create the JToolBar, but only if required
        // Allow a non-NULL but empty List
        if (getCustomToolbar() != null)
            {
            setToolBar(getCustomToolbar());
            getToolBar().setFloatable(false);
            getToolBar().setMinimumSize(DIM_TOOLBAR_SIZE);
            getToolBar().setPreferredSize(DIM_TOOLBAR_SIZE);
            getToolBar().setMaximumSize(DIM_TOOLBAR_SIZE);
            getToolBar().setBackground(UIComponentPlugin.DEFAULT_COLOUR_TAB_BACKGROUND.getColor());
            }
        else
            {
            setToolBar(null);
            }

        //------------------------------------------------------------------------------------------
        // Create the JTree for the explorer, and register it with the ToolTipManager
        // Return with a selected Node
        // This is the only call to createMetadataTree()

        setMetadataTree(createMetadataTree());
        ToolTipManager.sharedInstance().registerComponent(getMetadataTree());

        // Allow customized node icons if enabled
        // Do this every time the panel is requested, to ensure state is up to date
        resetCellRenderer(getMetadataTree());

        // Now set up the TreeModel to allow examination of the data
        // and make sure that the tree uses only DefaultMutableTreeNodes
        setTreeModel((DefaultTreeModel)getMetadataTree().getModel());
        getTreeModel().setAsksAllowsChildren(true);

        //------------------------------------------------------------------------------------------
        // Make a scrolling pane for the tree, because it will get large

        scrollpaneTree = new JScrollPane(getMetadataTree());
        scrollpaneTree.setBackground(Color.WHITE);

        // Set size limits for the split
        getScrollPaneTree().setMinimumSize(DIM_DIVIDER);
        getScrollPaneTree().setPreferredSize(DIM_DIVIDER);

        //------------------------------------------------------------------------------------------
        // Add the Tree scrolling pane and a blank Metadata panel to the split pane, and initialise

        jSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                                    getScrollPaneTree(),
                                    new BlankUIComponent());
        jSplitPane.setOneTouchExpandable(true);
        jSplitPane.setContinuousLayout(false);
        jSplitPane.setDividerSize(SIZE_SPLIT_PANE_DIVIDER);
        // The right component gets all the extra space
        jSplitPane.setResizeWeight(0.0);
        setSplitPane(jSplitPane);

        // Listen for changes in the Split Pane divider location
        adapter = new JSplitPaneAdapter(getSplitPane(),
                                        getScrollPaneTree(),
                                        REGISTRY,
                                        getResourceKey() + KEY_DIMENSION_DIVIDER_LOCATION);
        getSplitPane().addPropertyChangeListener(JSplitPane.LAST_DIVIDER_LOCATION_PROPERTY,
                                                            getAdapter());

        //------------------------------------------------------------------------------------------
        // Set up for selections from the Split Pane tree

        // Only one Node at a time may be selected
        getMetadataTree().getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        // Listen for Mouse events
        final MouseListener listenerMouse = new MouseAdapter()
            {
            public void mousePressed(final MouseEvent mouseEvent)
                {
                final String SOURCE = "MetadataExplorerUIComponent.createUIComponents() mousePressed() ";

                if (getMetadataTree() != null)
                    {
                    final DefaultMutableTreeNode nodeSelected;
                    final int rowClicked;

                    // Returns -1 if the location is not within the bounds of a displayed cell
                    rowClicked = getMetadataTree().getRowForLocation(mouseEvent.getX(),
                                                                     mouseEvent.getY());
                    if (rowClicked != -1)
                        {
                        final TreePath treePath;

                        treePath = getMetadataTree().getPathForRow(rowClicked);

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
                                              getMetadataTree().getRowBounds(rowClicked));
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
                    LOGGER.error(SOURCE + "Metadata TREE is NULL");
                    }
                }
            };

        getMetadataTree().addMouseListener(listenerMouse);
        ToolTipManager.sharedInstance().registerComponent(getSplitPane());

        // Create the UI with a MetadataExpanderUIComponent showing
        // Leave it to the caller to initialise
        getSplitPane().setRightComponent((Component)((MetadataExpanderDataInterface)getSelectedTreeNode().getUserObject()).getUIComponent());
        getSplitPane().setVisible(true);

        // Always start with an Expander, and no Leaf
        setSelectedExpanderUI((MetadataExpanderUIComponentInterface) getSplitPane().getRightComponent());
        setSelectedLeafUI(null);

        // Show the correct chunk of child Metadata on the MetadataExpanderUI
        // Leave it to runUI() to refresh
        getSelectedExpanderUI().setMetadataList(((MetadataExpanderDataInterface) getSelectedTreeNode().getUserObject()).getChildMetadata());
        }


    /***********************************************************************************************
     * Create the Metadata JTree.
     *
     * @return JTree
     */

    private JTree createMetadataTree()
        {
        final JTree tree;
        final DefaultMutableTreeNode root;

        // Make a Tree which informs listeners of MetadataChangedEvents
        root = MetadataTreeBuilder.buildTreeFromMetadata(getMetadataList(),
                                                         getTheExpanderUI(),
                                                         getTheLeafUI());
        tree = new JTree(root)
            {
            private static final long serialVersionUID = -4473995516684231166L;


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

                    if (curPath != null)
                        {
                        // Get the last Node of the path
                        lastPathComponent = (DefaultMutableTreeNode) curPath.getLastPathComponent();

                        // Recover ToolTip text from the UserObjects
                        userObject = lastPathComponent.getUserObject();

                        if (userObject instanceof MetadataExpanderDataInterface)
                            {
                            strTooltip = ((MetadataExpanderDataInterface)userObject).getPathname();
                            }
                        else if (userObject instanceof MetadataLeafDataInterface)
                            {
                            strTooltip = ((MetadataLeafDataInterface)userObject).getPathname();
                            }
                        }

                    return (strTooltip);
                    }
                }
            };

        tree.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Always start at the root, which must be on row 0
        tree.scrollRowToVisible(0);
        tree.setSelectionPath(tree.getPathForRow(0));
        setSelectedTreeNode((DefaultMutableTreeNode)tree.getModel().getRoot());

        return (tree);
        }


    /***********************************************************************************************
     * Process the Tree selection.
     * See similar code in setMetadata().
     *
     * @param event The MouseEvent causing the selection
     * @param treenode The tree node being selected
     * @param rectangle The selected area
     */

    private void treeSelection(final MouseEvent event,
                               final DefaultMutableTreeNode treenode,
                               final Rectangle rectangle)
        {
        final String SOURCE = "MetadataExplorerUIComponent.treeSelection() ";

        // Update the resources on every click...
        readResources();

        if (treenode != null)
            {
            final Object userObject;

            userObject = treenode.getUserObject();

            // See if the user clicked an Expander or a Leaf
            // Expanders show the child Metadata
            // Leaves show the Metadata editor panel

            if (userObject instanceof MetadataExpanderDataInterface)
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
                getSplitPane().setRightComponent((Component)((MetadataExpanderDataInterface)userObject).getUIComponent());
                getSplitPane().setVisible(true);

                // Set and run the new Expander
                setSelectedExpanderUI((MetadataExpanderUIComponentInterface) getSplitPane().getRightComponent());

                // Show the correct chunk of child Metadata on the MetadataExpanderUI
                // Double check that we have the right kind of UIComponent
                if (getSelectedExpanderUI() != null)
                    {
                    getSelectedExpanderUI().setMetadataList(((MetadataExpanderDataInterface) userObject).getChildMetadata());
                    getSelectedExpanderUI().runUI();
                    }

                // Record the new selection
                getMetadataTree().setSelectionPath(new TreePath(treenode.getPath()));
                setSelectedTreeNode(treenode);
                }
            else if (userObject instanceof MetadataLeafDataInterface)
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

                getSplitPane().setRightComponent((Component)((MetadataLeafDataInterface)userObject).getUIComponent());
                getSplitPane().setVisible(true);

                // Set, initialise and run the new Leaf
                setSelectedLeafUI((MetadataLeafUIComponentInterface) getSplitPane().getRightComponent());

                // Show the correct item of Metadata on the MetadataLeafUI
                // Double check that we have the right kind of UIComponent
                if (getSelectedLeafUI() != null)
                    {
                    getSelectedLeafUI().initialiseUI();
                    getSelectedLeafUI().setEditedMetadata(((MetadataLeafDataInterface) userObject).getMetadataItem());
                    getSelectedLeafUI().runUI();
                    }

                // Record the new selection
                getMetadataTree().setSelectionPath(new TreePath(treenode.getPath()));
                setSelectedTreeNode(treenode);
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
     * @return DefaultTreeModel
     */

    private DefaultTreeModel getTreeModel()
        {
        return (this.treeModel);
        }


    /***********************************************************************************************
     * Set the TreeModel, used by the JTree.
     *
     * @param model
     */

    private void setTreeModel(final DefaultTreeModel model)
        {
        treeModel = model;
        }


    /***********************************************************************************************
     * Get the navigation JTree.
     *
     * @return JTree
     */

    public final JTree getMetadataTree()
        {
        return (this.treeMetadata);
        }


    /***********************************************************************************************
     * Set the navigation JTree.
     *
     * @param tree
     */

    public final void setMetadataTree(final JTree tree)
        {
        this.treeMetadata = tree;
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
     * Validate and Update the UI of all components.
     */

    public final void validateAndUpdateUI()
        {
        if ((getMetadataTree() != null)
            && (UIComponentHelper.shouldRefresh(false, getHostInstrument(), this)))
            {
            // Re-create the TreeCellRenderer()
            resetCellRenderer(getMetadataTree());

            getMetadataTree().revalidate();

            // Ensure that everything in the Panel is up to date
            NavigationUtilities.updateComponentTreeUI(this);
            }
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
     * Reset the TreeCellRenderer.
     * This is required when changing Look&Feel.
     *
     * @param tree
     */

    public final void resetCellRenderer(final JTree tree)
        {
        if (boolCustomIcons)
            {
            setTreeCellRenderer(new CustomMetadataCellRenderer());
            }
        else
            {
            setTreeCellRenderer(new DefaultTreeCellRenderer());
            }

        // Set the Tree cell renderer
        if ((getTreeCellRenderer() != null)
            && (tree != null)
            && (UIComponentHelper.shouldRefresh(false, getHostInstrument(), this)))
            {
            tree.setCellRenderer(getTreeCellRenderer());
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
     * Get the JToolBar.
     *
     * @return JToolBar
     */

    private JToolBar getToolBar()
        {
        return (this.toolBar);
        }


    /***********************************************************************************************
     * Set the JToolBar.
     *
     * @param toolbar
     */

    private void setToolBar(final JToolBar toolbar)
        {
        this.toolBar = toolbar;
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
     * Get the singleton MetadataExpanderUI.
     *
     * @return MetadataExpanderUIComponentInterface
     */

    public MetadataExpanderUIComponentInterface getTheExpanderUI()
        {
        return (this.theExpander);
        }


    /***********************************************************************************************
     * Get the Selected MetadataExpanderUI.
     *
     * @return MetadataExpanderUIComponentInterface
     */

    public MetadataExpanderUIComponentInterface getSelectedExpanderUI()
        {
        return (this.selectedExpander);
        }


    /***********************************************************************************************
     * Set the Selected MetadataExpanderUI.
     *
     * @param expanderui
     */

    private void setSelectedExpanderUI(final MetadataExpanderUIComponentInterface expanderui)
        {
        this.selectedExpander = expanderui;
        }


    /***********************************************************************************************
     * Get the singleton MetadataLeafUI.
     *
     * @return MetadataLeafUIComponentInterface
     */

    public MetadataLeafUIComponentInterface getTheLeafUI()
        {
        return (this.theLeaf);
        }


    /***********************************************************************************************
     * Get the Selected MetadataLeafUI.
     *
     * @return MetadataLeafUIComponentInterface
     */

    public MetadataLeafUIComponentInterface getSelectedLeafUI()
        {
        return (this.selectedLeaf);
        }


    /***********************************************************************************************
     * Set the Selected MetadataLeafUI.
     *
     * @param leafui
     */

    private void setSelectedLeafUI(final MetadataLeafUIComponentInterface leafui)
        {
        this.selectedLeaf = leafui;
        }


    /**********************************************************************************************/
    /* Injections                                                                                 */
    /***********************************************************************************************
     * Get the Task on which this UIComponent is based.
     *
     * @return TaskData
     */

    private TaskPlugin getHostTask()
        {
        return ((TaskPlugin)this.hostTask);
        }


    /***********************************************************************************************
     * Get the ObservatoryInstrument to which this UIComponent is attached.
     *
     * @return ObservatoryInstrumentInterface
     */

    private ObservatoryInstrumentInterface getHostInstrument()
        {
        return (this.hostInstrument);
        }


    /***********************************************************************************************
     * Get the Toolbar for this MetadataExplorer.
     *
     * @return JToolBar
     */

    private JToolBar getCustomToolbar()
        {
        return (this.toolbarCustom);
        }


    /***********************************************************************************************
     * Get the List of Metadata upon which the UIComponent is based.
     *
     * @return List<Metadata>
     */

    private List<Metadata> getMetadataList()
        {
        return (this.listMetadata);
        }


    /***********************************************************************************************
     * Set the List of Metadata for this UIComponent.
     * See similar code in treeSelection().
     *
     * @param metadatalist
     */

    public void setMetadataList(final List<Metadata> metadatalist)
        {
        final String SOURCE = "MetadataExplorerUIComponent.setMetadataList() ";

        this.listMetadata = metadatalist;

        MetadataHelper.showMetadataList(metadatalist,
                                        SOURCE + "Metadata available to Editor",
                                        isDebug());

        // Rebuild the existing Tree with the new Metadata
        if ((getMetadataTree() != null)
            && (getMetadataList() != null)
            && (!getMetadataList().isEmpty()))
            {
            try
                {
                final TreeModel model;

                // Make a Tree which informs listeners of MetadataChangedEvents
                model = new DefaultTreeModel(MetadataTreeBuilder.buildTreeFromMetadata(getMetadataList(),
                                                                                       getTheExpanderUI(),
                                                                                       getTheLeafUI()));
                if (model.getRoot() != null)
                    {
                    getMetadataTree().setModel(model);

                    // Always start at the root, which must be on row 0
                    getMetadataTree().scrollRowToVisible(0);
                    getMetadataTree().setSelectionPath(getMetadataTree().getPathForRow(0));
                    setSelectedTreeNode((DefaultMutableTreeNode)getMetadataTree().getModel().getRoot());

                    NavigationUtilities.updateComponentTreeUI(getMetadataTree());

                    // We know we should always be looking at an Expander at the Root,
                    // so kill off any Leaf
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
                    if ((getSelectedTreeNode() != null)
                        && (getSelectedTreeNode().getUserObject() != null)
                        && (getSelectedTreeNode().getUserObject() instanceof MetadataExpanderDataInterface)
                        && ((MetadataExpanderDataInterface)getSelectedTreeNode().getUserObject()).getUIComponent() != null)
                        {
                        getSplitPane().setRightComponent((Component)((MetadataExpanderDataInterface)getSelectedTreeNode().getUserObject()).getUIComponent());
                        getSplitPane().setVisible(true);

                        // Set and run the new Expander
                        setSelectedExpanderUI((MetadataExpanderUIComponentInterface) getSplitPane().getRightComponent());

                        // The selection is now at the Root,
                        // so we can see the entire set of Metadata if we have an ExpanderPanel
                        if (getSelectedExpanderUI() != null)
                            {
                            getSelectedExpanderUI().setMetadataList(metadatalist);
                            getSelectedExpanderUI().runUI();
                            }
                        }
                    else
                        {
                        LOGGER.warn(SOURCE + "UserObject or UIComponent is NULL, unable to set Metadata on MetadataExplorer");
                        }
                    }
                else
                    {
                    LOGGER.warn(SOURCE + "Tree Model is NULL, unable to set Metadata on MetadataExplorer");
                    }
                }

            catch (final Exception exception)
                {
                // I can't find the reason for this, so just warn quietly
                if (isDebug())
                    {
                    LOGGER.warn(SOURCE + "Unable to set Metadata on MetadataExplorer (Generic Exception)");
                    //exception.printStackTrace();
                    }
                }
            }
        else
            {
            LOGGER.warn(SOURCE + "Tree or Metadata are NULL, unable to set Metadata on MetadataExplorer");
            }

        // Now repaint everything...
        validateAndUpdateUI();
        }


    /***********************************************************************************************
     * Get the ResourceKey for the UIComponent.
     *
     * @return String
     */

    private String getResourceKey()
        {
        return (this.strResourceKey);
        }


    /***********************************************************************************************
     * Read all the Resources required by the MetadataExplorerUIComponent.
     */

    private void readResources()
        {

        }
    }
