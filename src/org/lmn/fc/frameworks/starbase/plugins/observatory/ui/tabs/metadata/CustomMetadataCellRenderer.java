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

import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.MetadataExpanderDataInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.MetadataLeafDataInterface;
import org.lmn.fc.model.logging.Logger;
import org.lmn.fc.model.registry.RegistryModelUtilities;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;


/***********************************************************************************************
 * Custom TreeCellRenderer to allow custom Icons.
 */

public final class CustomMetadataCellRenderer extends DefaultTreeCellRenderer
    {
    private static final Logger LOGGER = Logger.getInstance();
    private static final long serialVersionUID = 8619074277019315860L;


    /*******************************************************************************************
     * Construct the TreeCellRenderer.
     */

    public CustomMetadataCellRenderer()
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
        final String SOURCE = "CustomMetadataCellRenderer.getTreeCellRendererComponent() ";

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
                    if (userObject instanceof MetadataExpanderDataInterface)
                        {
                        final MetadataExpanderDataInterface expanderData;

                        expanderData = (MetadataExpanderDataInterface)userObject;
                        setIcon(RegistryModelUtilities.getCommonIcon(expanderData.getIconFilename()));
                        }
                    else if (userObject instanceof MetadataLeafDataInterface)
                        {
                        final MetadataLeafDataInterface leafData;

                        leafData = (MetadataLeafDataInterface)userObject;
                        setIcon(RegistryModelUtilities.getCommonIcon(leafData.getIconFilename()));
                        }
                    else
                        {
                        LOGGER.debug(SOURCE + "Unable to find tree Icon for " + userObject.getClass().getName());
                        setIcon(null);
                        }
                    }
                else
                    {
                    LOGGER.debug(SOURCE + "NULL UserObject");
                    setIcon(null);
                    }
                }
            else
                {
                LOGGER.debug(SOURCE + "NULL TreeCell VALUE");
                setIcon(null);
                }
            }
        else
            {
            LOGGER.debug(SOURCE + "NULL JTree");
            setIcon(null);
            }

        return (this);
        }
    }
