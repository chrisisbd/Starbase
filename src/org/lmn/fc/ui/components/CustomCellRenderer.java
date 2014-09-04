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

package org.lmn.fc.ui.components;

import org.lmn.fc.model.logging.Logger;
import org.lmn.fc.model.plugins.AtomPlugin;
import org.lmn.fc.model.plugins.ExpanderInterface;
import org.lmn.fc.model.registry.RegistryModelUtilities;
import org.lmn.fc.model.root.RootPlugin;
import org.lmn.fc.model.tasks.TaskPlugin;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;


/***********************************************************************************************
 * Custom TreeCellRenderer to allow custom Icons.
 */

public final class CustomCellRenderer extends DefaultTreeCellRenderer
    {
    private static final Logger LOGGER = Logger.getInstance();


    /*******************************************************************************************
     * Construct the TreeCellRenderer.
     */

    public CustomCellRenderer()
        {
        super();
        }


    /*******************************************************************************************
     * Allow each node to have a different icon by overriding the
     * getTreeCellRendererComponent task and looking at the UserObjectPlugin
     * to determine the type of Node.
     *
     * @param tree
     * @param value
     * @param boolIsSelected
     * @param boolIsExpanded
     * @param boolIsLeaf
     * @param row
     * @param focus
     *
     * @return Component
     */

    public Component getTreeCellRendererComponent(final JTree tree,
                                                  final Object value,
                                                  final boolean boolIsSelected,
                                                  final boolean boolIsExpanded,
                                                  final boolean boolIsLeaf,
                                                  final int row,
                                                  final boolean focus)
        {
        // The Value may be null, but not the JTree!
        if (tree != null)
            {
            super.getTreeCellRendererComponent(tree, value,
                                               boolIsSelected, boolIsExpanded, boolIsLeaf,
                                               row, focus);

            final DefaultMutableTreeNode objectTreeNode = (DefaultMutableTreeNode) value;

            if (objectTreeNode != null)
                {
                final Object userObject = objectTreeNode.getUserObject();

                if (userObject != null)
                    {
                    if (userObject instanceof ExpanderInterface)
                        {
                        final ExpanderInterface expander = (ExpanderInterface)userObject;
                        setIcon(RegistryModelUtilities.getCommonIcon(expander.getIconFilename()));
                        }
                    else if (userObject instanceof AtomPlugin)
                        {
                        final AtomPlugin pluginAtom = (AtomPlugin)userObject;
                        setIcon(RegistryModelUtilities.getAtomIcon(pluginAtom, pluginAtom.getIconFilename()));
                        }
                    else if (userObject instanceof TaskPlugin)
                        {
                        final TaskPlugin pluginTask = (TaskPlugin)userObject;
                        setIcon(RegistryModelUtilities.getCommonIcon(pluginTask.getIconFilename()));

                        if (!pluginTask.isPublic())
                            {
                            setText("<html><i><font color=gray>" + getText() + "</font></i></html>");
                            }
                        }
                    else if (userObject instanceof RootPlugin)
                        {
                        final RootPlugin plugin = (RootPlugin)userObject;
                        setIcon(RegistryModelUtilities.getCommonIcon(plugin.getIconFilename()));
                        }
                    else
                        {
                        LOGGER.debug("Unable to find tree Icon for " + userObject.getClass().getName());
                        setIcon(null);
                        }
                    }
                else
                    {
                    LOGGER.debug("NULL USEROBJECT in CustomCellRenderer.getTreeCellRendererComponent");
                    setIcon(null);
                    }
                }
            else
                {
                LOGGER.debug("NULL VALUE in CustomCellRenderer.getTreeCellRendererComponent");
                setIcon(null);
                }
            }
        else
            {
            LOGGER.debug("NULL TREE in CustomCellRenderer.getTreeCellRendererComponent");
            setIcon(null);
            }

        return (this);
        }
    }