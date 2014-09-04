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

import org.lmn.fc.common.constants.*;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.MetadataHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.MetadataExpanderDataInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.MetadataExpanderUIComponentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.MetadataLeafDataInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.MetadataLeafUIComponentInterface;
import org.lmn.fc.model.plugins.AtomPlugin;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.*;


/***************************************************************************************************
 * MetadataTreeBuilder.
 */

public final class MetadataTreeBuilder implements FrameworkConstants,
                                                  FrameworkStrings,
                                                  FrameworkMetadata,
                                                  FrameworkSingletons,
                                                  FrameworkRegex,
                                                  ResourceKeys
    {
    // String Resources
    private static final String NODE_METADATA = "Metadata";
    private static final String EXPANDER_ICON = AtomPlugin.PLUGINS_ICON;
    private static final String LEAF_ICON = "action.png";


    /***********************************************************************************************
     * Build the tree of nodes given the Metadata list, and the expander and leaf UIComponents.
     *
     * @param metadatalist
     * @param expanderui
     * @param leafui
     *
     * @return DefaultMutableTreeNode
     */

    public static DefaultMutableTreeNode buildTreeFromMetadata(final List<Metadata> metadatalist,
                                                               final MetadataExpanderUIComponentInterface expanderui,
                                                               final MetadataLeafUIComponentInterface leafui)
        {
        final MetadataExpanderDataInterface expanderData;
        final DefaultMutableTreeNode root;

        // Create the Root Node UserObject as an Expander,
        // this exists regardless of the Metadata state
        root = new DefaultMutableTreeNode();
        expanderData = new MetadataExpanderData(NODE_METADATA,
                                                HTML_PREFIX_ITALIC + NODE_METADATA + HTML_SUFFIX_ITALIC,
                                                EXPANDER_ICON,
                                                root,
                                                expanderui);
        expanderData.setChildMetadata(metadatalist);
        root.setUserObject(expanderData);

        // Read all of the Metadata Keys
        if ((metadatalist != null)
            && (!metadatalist.isEmpty()))
            {
            final List<String> listKeys;

            listKeys = new ArrayList<String>(100);

            for (int i = 0;
                 i < metadatalist.size();
                 i++)
                {
                final Metadata md;
                final String strKey;

                md = metadatalist.get(i);
                strKey = md.getKey();
                listKeys.add(strKey);
                }

            // Sort the Keys alphabetically to improve the Tree view
            Collections.sort(listKeys);

            // Traverse the structure implied by the Keys and build a set of TreeNodes
            try
                {
                buildNodes(metadatalist,
                           listKeys,
                           root,
                           expanderui,
                           leafui);
                }

            catch (final Exception exception)
                {
                LOGGER.error("MetadataTreeBuilder.buildTreeFromMetadata() Generic Exception");
                exception.printStackTrace();
                }
            }
        else
            {
            //LOGGER.warn("MetadataTreeBuilder.buildTreeFromMetadata() has no Metadata");
            }

        return (root);
        }


    /***********************************************************************************************
     * Traverse the structure implied by the Keys and build a set of TreeNodes attached to the Root.
     *
     * @param metadata
     * @param keys
     * @param rootnode
     * @param expanderui
     * @param leafui
     */

    private static void buildNodes(final List<Metadata> metadata,
                                   final List<String> keys,
                                   final DefaultMutableTreeNode rootnode,
                                   final MetadataExpanderUIComponentInterface expanderui,
                                   final MetadataLeafUIComponentInterface leafui)
        {
        StringBuffer bufferPath;
        Map<String, DefaultMutableTreeNode> mapUsedNodes;

        bufferPath = new StringBuffer();
        mapUsedNodes = new Hashtable<String, DefaultMutableTreeNode>(100);

        // Process the List of Keys in alphabetical order
        for (int i = 0;
             i < keys.size();
             i++)
            {
            final String strKey;

            strKey = keys.get(i);

            processOneKey(metadata,
                          keys,
                          strKey,
                          bufferPath,
                          rootnode,
                          mapUsedNodes,
                          expanderui,
                          leafui);
            }

        // Help the GC?
        bufferPath = null;
        mapUsedNodes = null;
        }


    /***********************************************************************************************
     * Traverse the structure implied by one Key and build a set of TreeNodes attached to the Root.
     *
     * @param metadata
     * @param keys
     * @param key
     * @param path
     * @param parentnode
     * @param usednodes
     * @param expanderui
     * @param leafui
     */

    private static void processOneKey(final List<Metadata> metadata,
                                      final List<String> keys,
                                      final String key,
                                      final StringBuffer path,
                                      final DefaultMutableTreeNode parentnode,
                                      final Map<String, DefaultMutableTreeNode> usednodes,
                                      final MetadataExpanderUIComponentInterface expanderui,
                                      final MetadataLeafUIComponentInterface leafui)
        {
        DefaultMutableTreeNode lastNode;
        final String[] arrayNames;

        // Keep track of the Parent Node as we move along the Key name parts
        // Don't store the root node in the Map, only the new ones
        lastNode = parentnode;

        // Record the path for each Key as it is traversed
        path.setLength(0);

        // Split the Key into its component parts, delimited by dots
        // Ignore PatternSyntaxException
        arrayNames = key.split(REGEX_RESOURCE_DELIMITER);

        // Process the array of Key parts
        // If none are found, then the ParentNode remains unchanged
        for (int i = 0;
             i < arrayNames.length;
             i++)
            {
            final String name;
            final DefaultMutableTreeNode node;

            name = arrayNames[i];
            node = new DefaultMutableTreeNode();

            // Is this an Expander or a Leaf?
            if (i == (arrayNames.length-1))
                {
                final MetadataLeafDataInterface leafData;
                final Metadata metadataitem;

                // It is a Leaf
                // Trap the case of a single Leaf and no Expander...
                if ((arrayNames.length) > 1)
                    {
                    path.append(DOT);
                    }
                path.append(name);

                // Leaf Nodes can never be re-used, so always make a new one
                leafData = new MetadataLeafData(path.toString(),
                                                name,
                                                LEAF_ICON,
                                                node,
                                                leafui);

                // If a Leaf, the UserObject holds the single Metadata item to be edited
                metadataitem = MetadataHelper.getMetadataByKey(metadata, key);
                leafData.setMetadataItem(metadataitem);

                node.setUserObject(leafData);
                lastNode.add(node);

                // Save the Node in the Map (this should never get used)
                usednodes.put(path.toString(), node);

                // Move along the nodes (this value should never get used)
                lastNode = node;
                }
            else
                {
                // Trap the case of the first Expander to avoid a leading dot
                if (i > 0)
                    {
                    path.append(DOT);
                    }
                path.append(name);

                // It is an Expander, but have we already created the Node?
                if (!usednodes.containsKey(path.toString()))
                    {
                    final MetadataExpanderDataInterface expanderData;
                    final List<Metadata> listMetadata;

                    expanderData = new MetadataExpanderData(path.toString(),
                                                            name,
                                                            EXPANDER_ICON,
                                                            node,
                                                            expanderui);

                    // If an Expander, record in the UserObject the list of Metadata below this node
                    // So we need the List of Metadata which starts with the same pathname to this point
                    listMetadata = getChildMetadata(metadata, keys, path);
                    expanderData.setChildMetadata(listMetadata);

                    node.setUserObject(expanderData);
                    lastNode.add(node);
                    usednodes.put(path.toString(), node);

                    // Move along the nodes, ready for the next Expander or Leaf
                    lastNode = node;
                    }
                else
                    {
                    // The node already exists with a valid MetadataExpanderData,
                    // so move the lastNode to the one in the Map with this Path

                    lastNode = usednodes.get(path.toString());
                    }
                }
            }
        }


    /***********************************************************************************************
     * Get the List of Metadata which starts with the specified pathname.
     *
     * @param metadata
     * @param keys
     * @param path
     *
     * @return List<Metadata>
     */

    private static List<Metadata> getChildMetadata(final List<Metadata> metadata,
                                                   final List<String> keys,
                                                   final StringBuffer path)
        {
        final List<Metadata> childMetadata;

        childMetadata = new ArrayList<Metadata>(keys.size());

        for (int i = 0;
             i < keys.size();
             i++)
            {
            final String strKey;

            strKey = keys.get(i);

            if (strKey.startsWith(path.toString() + DOT))
                {
                childMetadata.add(MetadataHelper.getMetadataByKey(metadata, strKey));
                }
            }

        return (childMetadata);
        }
    }