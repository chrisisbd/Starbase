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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs;

import org.lmn.fc.model.xmlbeans.metadata.Metadata;

import javax.swing.tree.DefaultMutableTreeNode;


/***************************************************************************************************
 * MetadataLeafDataInterface.
 * A marker interface to identify Metadata Leaves.
 */

public interface MetadataLeafDataInterface
    {
    /***********************************************************************************************
     * Get the full pathname of the MetadataLeafData object.
     *
     * @return String
     */

    String getPathname();


    /***********************************************************************************************
     * Get the name of the MetadataLeafData object.
     *
     * @return String
     */

    String getName();


    /***********************************************************************************************
     * Get the IconFilename field.
     *
     * @return String
     */

    String getIconFilename();


    /***********************************************************************************************
     * Get the HostTreeNode.
     *
     * @return DefaultMutableTreeNode
     */

    DefaultMutableTreeNode getHostTreeNode();


    /***********************************************************************************************
     * Set the HostTreeNode.
     *
     * @param node
     */

    void setHostTreeNode(DefaultMutableTreeNode node);


    /***********************************************************************************************
     * Get the MetadataLeafUI.
     *
     * @return MetadataLeafUIComponentInterface
     */

    MetadataLeafUIComponentInterface getUIComponent();


    /***********************************************************************************************
     * Set the MetadataLeafUI.
     *
     * @param uileaf
     */

    void setUIComponent(MetadataLeafUIComponentInterface uileaf);


    /***********************************************************************************************
     * Get the Metadata below this node.
     *
     * @return Metadata
     */

    Metadata getMetadataItem();


    /***********************************************************************************************
     * Set the Metadata below this node.
     *
     * @param metadataitem
     */

    void setMetadataItem(Metadata metadataitem);
    }
