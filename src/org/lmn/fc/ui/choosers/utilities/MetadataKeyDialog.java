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

package org.lmn.fc.ui.choosers.utilities;


import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.MetadataHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.MetadataExpanderDataInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.MetadataExpanderUIComponentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.MetadataLeafDataInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.MetadataLeafUIComponentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.metadata.CustomMetadataCellRenderer;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.metadata.MetadataExpanderUIComponent;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.metadata.MetadataLeafUIComponent;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.metadata.MetadataTreeBuilder;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.model.registry.NavigationUtilities;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;
import org.lmn.fc.ui.choosers.ChooserInterface;
import org.lmn.fc.ui.layout.BoxLayoutFixed;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;


/***************************************************************************************************
 * MetadataKeyDialog.
 */

public class MetadataKeyDialog extends JDialog
                               implements ActionListener,
                                          FrameworkStrings,
                                          FrameworkSingletons
    {
    private static final Dimension DIM_PREFERRED_SIZE = new Dimension(250, 450);

    private static MetadataKeyDialog dialog;
    private static String strValue = EMPTY_STRING;


    // Injections
    private final ObservatoryInstrumentInterface observatoryInstrument;
    private final FontInterface pluginFont;
    private final ColourInterface pluginColour;

    private JTree treeMetadata;
    private DefaultTreeModel treeModel;
    private TreeCellRenderer treeCellRenderer;
    private DefaultMutableTreeNode treenodeSelected;
    private boolean boolCustomIcons;


    /***********************************************************************************************
     * Set up and show the dialog.  The first Component argument
     * determines which frame the dialog depends on; it should be
     * a component in the dialog's controlling frame. The second
     * Component argument should be null if you want the dialog
     * to come up with its left corner in the center of the screen;
     * otherwise, it should be the component on top of which the
     * dialog should appear.
     *
     * @param framecomponent
     * @param locationcomponent
     * @param labeltext
     * @param title
     * @param obsinstrument
     * @param font
     * @param colourforeground
     * @param initialvalue
     *
     * @return String
     */

    public static String showDialog(final Component framecomponent,
                                    final Component locationcomponent,
                                    final String labeltext,
                                    final String title,
                                    final ObservatoryInstrumentInterface obsinstrument,
                                    final FontInterface font,
                                    final ColourInterface colourforeground,
                                    final String initialvalue)
        {
        final Frame frame;

        frame = JOptionPane.getFrameForComponent(framecomponent);

        dialog = new MetadataKeyDialog(frame,
                                       locationcomponent,
                                       labeltext,
                                       title,
                                       obsinstrument,
                                       font,
                                       colourforeground,
                                       initialvalue);
        dialog.setVisible(true);

        return (strValue);
        }


    /***********************************************************************************************
     * Set the selected Key Value.
     *
     * @param value
     */

    private static void setValue(final String value)
        {
        strValue = value;
        }


    /***********************************************************************************************
     * MetadataKeyDialog.
     *
     * @param frame
     * @param locationcomponent
     * @param labeltext
     * @param title
     * @param obsinstrument
     * @param font
     * @param colourforeground
     * @param initialvalue
     */

    private MetadataKeyDialog(final Frame frame,
                              final Component locationcomponent,
                              final String labeltext,
                              final String title,
                              final ObservatoryInstrumentInterface obsinstrument,
                              final FontInterface font,
                              final ColourInterface colourforeground,
                              final String initialvalue)
        {
        super(frame, title, true);

        // Injections
        this.observatoryInstrument = obsinstrument;
        this.pluginFont = font;
        this.pluginColour = colourforeground;

        this.boolCustomIcons = true;

        createUIComponents(font, colourforeground);

        // Initialize value
        setValue(initialvalue);
        pack();
        setLocationRelativeTo(locationcomponent);
        }


    /***********************************************************************************************
     *  Create all screen components.
     *
     * @param font
     * @param colourforeground
     */

    private void createUIComponents(final FontInterface font,
                                    final ColourInterface colourforeground)
        {
        final JScrollPane scrollTree;
        final JPanel panelButtons;
        final JButton buttonSelect;
        final JButton buttonCancel;
        final JLabel labelKeyPath;
        final Container contentPane;

        setMetadataTree(createMetadataKeyTree());
        scrollTree = new JScrollPane(getMetadataTree());

        ToolTipManager.sharedInstance().registerComponent(getMetadataTree());

        // Allow customized node icons if enabled
        // Do this every time the panel is requested, to ensure state is up to date
        resetCellRenderer(getMetadataTree());

        // Now set up the TreeModel to allow examination of the data
        // and make sure that the tree uses only DefaultMutableTreeNodes
        setTreeModel((DefaultTreeModel)getMetadataTree().getModel());
        getTreeModel().setAsksAllowsChildren(true);

        labelKeyPath = new JLabel();
        labelKeyPath.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        labelKeyPath.setEnabled(false);
        labelKeyPath.setFont(font.getFont());
        labelKeyPath.setForeground(colourforeground.getColor());

        // Set up for selections from the Split Pane tree

        // Only one Node at a time may be selected
        getMetadataTree().getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        // Listen for Mouse events
        final MouseListener listenerMouse = new MouseAdapter()
            {
            public void mousePressed(final MouseEvent mouseEvent)
                {
                if (getMetadataTree() != null)
                    {
                    final DefaultMutableTreeNode treenodeSelected;
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
                            treenodeSelected = (DefaultMutableTreeNode) treePath.getLastPathComponent();

                            if (SwingUtilities.isLeftMouseButton(mouseEvent)
                                && (!mouseEvent.isShiftDown())
                                && (!mouseEvent.isAltDown())
                                && (mouseEvent.getClickCount() == 1)
                                && (treenodeSelected != null))
                                {
                                final Object userObject;
                                final String strSelection;

                                // Make the new selection
                                treeSelection(mouseEvent,
                                              treenodeSelected,
                                              getMetadataTree().getRowBounds(rowClicked));

                                userObject = treenodeSelected.getUserObject();

                                if (userObject instanceof MetadataExpanderDataInterface)
                                    {
                                    strSelection = ((MetadataExpanderDataInterface)userObject).getPathname();
                                    }
                                else if (userObject instanceof MetadataLeafDataInterface)
                                    {
                                    strSelection = ((MetadataLeafDataInterface)userObject).getPathname();
                                    }
                                else
                                    {
                                    strSelection = EMPTY_STRING;
                                    }

                                labelKeyPath.setText(strSelection);
                                }
                            else
                                {
                                // Invalid tree node or mouse state
                                labelKeyPath.setText(EMPTY_STRING);
                                }
                            }
                        else
                            {
                            //System.out.println("NULL tree path");
                            labelKeyPath.setText(EMPTY_STRING);
                            }
                        }
                    else
                        {
                        // Selection not on node
                        labelKeyPath.setText(EMPTY_STRING);
                        }
                    }
                else
                    {
                    LOGGER.error("MetadataKeyDialog.createUIComponents() Metadata TREE is NULL");
                    labelKeyPath.setText(EMPTY_STRING);
                    }
                }
            };

        getMetadataTree().addMouseListener(listenerMouse);

        // Create and initialize the buttons
        buttonSelect = new JButton(ChooserInterface.BUTTON_SELECT);
        buttonSelect.setFont(font.getFont());
        buttonSelect.setForeground(colourforeground.getColor());
        buttonSelect.setActionCommand(ChooserInterface.BUTTON_SELECT);
        buttonSelect.addActionListener(this);
        buttonSelect.setEnabled(false);
        getRootPane().setDefaultButton(buttonSelect);

        buttonCancel = new JButton(ChooserInterface.BUTTON_CANCEL);
        buttonCancel.setFont(font.getFont());
        buttonCancel.setForeground(colourforeground.getColor());
        buttonCancel.addActionListener(this);

        // Lay out the buttons from left to right
        panelButtons = new JPanel();
        panelButtons.setLayout(new BoxLayoutFixed(panelButtons, BoxLayout.LINE_AXIS));
        panelButtons.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panelButtons.add(Box.createHorizontalGlue());
        panelButtons.add(buttonSelect);
        panelButtons.add(Box.createRigidArea(new Dimension(10, 0)));
        panelButtons.add(buttonCancel);

        // Put everything together, using the content pane's BorderLayout
        contentPane = getContentPane();
        contentPane.setPreferredSize(DIM_PREFERRED_SIZE);
        contentPane.add(scrollTree, BorderLayout.PAGE_START);
        contentPane.add(labelKeyPath, BorderLayout.CENTER);
        contentPane.add(panelButtons, BorderLayout.PAGE_END);
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
        if (treenode != null)
            {
            final Object userObject;

            userObject = treenode.getUserObject();

            // See if the user clicked an Expander or a Leaf
            // The User must have a complete Key before selection is allowed
            if (userObject instanceof MetadataExpanderDataInterface)
                {
                // Clicking on an expander means the Key is incomplete, so cannot be selected
                getRootPane().getDefaultButton().setEnabled(false);

                // Record the new selection
                getMetadataTree().setSelectionPath(new TreePath(treenode.getPath()));
                setSelectedTreeNode(treenode);
                }
            else if (userObject instanceof MetadataLeafDataInterface)
                {
                // A complete Key may be selected
                getRootPane().getDefaultButton().setEnabled(true);

                // Record the new selection
                getMetadataTree().setSelectionPath(new TreePath(treenode.getPath()));
                setSelectedTreeNode(treenode);
                }
            else
                {
                // The UserObject is not of the correct type...
                getRootPane().getDefaultButton().setEnabled(false);
                LOGGER.error("MetadataKeyDialog.treeSelection() Unrecognised subclass of UserObject on Tree");
                }

            // Now repaint everything...
            validateAndUpdateUI();
            }
        else
            {
            LOGGER.error("MetadataKeyDialog.treeSelection() NULL TreeNode or no selection");
            }
        }


    /***********************************************************************************************
     * Create the Metadata Key JTree.
     *
     * @return JTree
     */

    private JTree createMetadataKeyTree()
        {
        final String SOURCE = "MetadataKeyDialog.createMetadataKeyTree() ";
        List<Metadata> listMetadata;
        final JTree tree;
        final MetadataExpanderUIComponentInterface expanderUI;
        final MetadataLeafUIComponentInterface leafUI;
        final DefaultMutableTreeNode root;

        listMetadata = null;

        if ((getObservatoryInstrument() != null)
            && (getObservatoryInstrument().getContext() != null))
            {
            listMetadata = MetadataHelper.collectAggregateMetadataTraced(REGISTRY.getFramework(),
                                                                         getObservatoryInstrument().getContext().getObservatory(),
                                                                         getObservatoryInstrument(),
                                                                         getObservatoryInstrument().getDAO(),
                                                                         getObservatoryInstrument().getDAO().getWrappedData(),
                                                                         SOURCE,
                                                                         LOADER_PROPERTIES.isMetadataDebug());
            // Set the Aggregate Metadata on the host Instrument
            // All e.g. Control panel data are taken from here
            // NOTE THAT The DAO data take precedence over those in the Wrapper
            getObservatoryInstrument().setAggregateMetadata(listMetadata);
            }

        // Make one MetadataExpanderUIComponent to use everywhere
        // Use a NULL Task and empty ResourceKey
        // This only affects the expanders, which are not needed here
        // Don't inform listeners of MetadataChanged events
        expanderUI = new MetadataExpanderUIComponent(null,
                                                     getObservatoryInstrument(),
                                                     listMetadata,
                                                     EMPTY_STRING);

        // Make one MetadataLeafUIComponent to use everywhere
        leafUI = new MetadataLeafUIComponent(getObservatoryInstrument(), false);

        root = MetadataTreeBuilder.buildTreeFromMetadata(listMetadata,
                                                         expanderUI,
                                                         leafUI);
        tree = new JTree(root)
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

                    strTooltip = "";

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
     * Validate and Update the UI of all components.
     */

    public final void validateAndUpdateUI()
        {
        if (getMetadataTree() != null)
            {
            // Re-create the TreeCellRenderer()
            resetCellRenderer(getMetadataTree());

            getMetadataTree().revalidate();
            }

        // Ensure that everything in the Panel is up to date
        NavigationUtilities.updateComponentTreeUI(this);
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
            && (tree != null))
            {
            tree.setCellRenderer(getTreeCellRenderer());
            }
        }



    /***********************************************************************************************
     * Handle clicks on the Select and Cancel buttons.
     *
     * @param event
     */

    public void actionPerformed(final ActionEvent event)
        {
        if (ChooserInterface.BUTTON_SELECT.equals(event.getActionCommand()))
            {
            strValue = EMPTY_STRING;

            if ((getMetadataTree() != null)
//                && (getMetadataTree().getSelectionPath().getLastPathComponent() != null)
//                && (getMetadataTree().getSelectionPath().getLastPathComponent() instanceof DefaultMutableTreeNode))
                && (getSelectedTreeNode() != null))
                {
                final Object userObject;

                userObject = getSelectedTreeNode().getUserObject();

                if (userObject instanceof MetadataExpanderDataInterface)
                    {
                    strValue = ((MetadataExpanderDataInterface)userObject).getPathname();
                    }
                else if (userObject instanceof MetadataLeafDataInterface)
                    {
                    strValue = ((MetadataLeafDataInterface)userObject).getPathname();
                    }
                else
                    {
                    strValue = EMPTY_STRING;
                    }
                }
            }

        dialog.setVisible(false);
        }


    /***********************************************************************************************
     * Get the ObservatoryInstrument.
     *
     * @return ObservatoryInstrumentInterface
     */

    protected ObservatoryInstrumentInterface getObservatoryInstrument()
        {
        return (this.observatoryInstrument);
        }


    /***********************************************************************************************
     * Get the FontDataType.
     *
     * @return FontPlugin
     */

    private FontInterface getFontData()
        {
        return (this.pluginFont);
        }


    /***********************************************************************************************
     * Get the ColourDataType.
     *
     * @return ColourPlugin
     */

    private ColourInterface getColourData()
        {
        return (this.pluginColour);
        }
    }
