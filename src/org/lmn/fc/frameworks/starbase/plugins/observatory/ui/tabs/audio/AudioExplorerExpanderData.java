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

import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.AudioExplorerExpanderDataInterface;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;
import org.lmn.fc.ui.UIComponentPlugin;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.List;


/***************************************************************************************************
 * AudioExplorerExpanderData.
 */

public final class AudioExplorerExpanderData implements AudioExplorerExpanderDataInterface
    {
    // Injections
    private final String strPathname;
    private final String strName;
    private final Object[][] arrayData;
    private final String[] arrayColumnNames;
    private final String strIconFilename;
    private DefaultMutableTreeNode hostTreeNode;
    private UIComponentPlugin uiPlugin;

    private List<Metadata> listMetadata;


    /***********************************************************************************************
     * AudioExplorerExpanderData.
     *
     * @param pathname
     * @param name
     * @param data
     * @param columnnames
     * @param iconfilename
     * @param hostnode
     * @param uiplugin
     */

    public AudioExplorerExpanderData(final String pathname,
                                     final String name,
                                     final Object[][] data,
                                     final String[] columnnames,
                                     final String iconfilename,
                                     final DefaultMutableTreeNode hostnode,
                                     final UIComponentPlugin uiplugin)
        {
        // This could check for null references...
        this.strPathname = pathname;
        this.strName = name;
        this.arrayData = data;
        this.arrayColumnNames = columnnames;
        this.strIconFilename = iconfilename;
        this.hostTreeNode = hostnode;
        this.uiPlugin = uiplugin;

        this.listMetadata = null;

        // Bidirectionally link the AudioExplorerExpanderData to the host node
        getHostTreeNode().setUserObject(this);
        }


    /***********************************************************************************************
     * Get the full pathname of the AudioExplorerExpanderData object.
     *
     * @return String
     */

    public final String getPathname()
        {
        return (this.strPathname);
        }


    /***********************************************************************************************
     * Get the name of the AudioExplorerExpanderData object.
     *
     * @return String
     */

    public final String getName()
        {
        return (this.strName);
        }


    /*******************************************************************************************
     * Get the Data array.
     *
     * @return Object[][]
     */

    public Object[][] getData()
        {
        return (this.arrayData);
        }


    /*******************************************************************************************
     * Get the Column Names.
     *
     * @return String[]
     */

    public String[] getColumnNames()
        {
        return (this.arrayColumnNames);
        }


    /***********************************************************************************************
     * Get the IconFilename field.
     *
     * @return String
     */

    public final String getIconFilename()
        {
        return (this.strIconFilename);
        }


    /***********************************************************************************************
     * Get the HostTreeNode.
     *
     * @return DefaultMutableTreeNode
     */

    public final DefaultMutableTreeNode getHostTreeNode()
        {
        return (this.hostTreeNode);
        }


    /***********************************************************************************************
     * Set the HostTreeNode.
     *
     * @param node
     */

    public final void setHostTreeNode(final DefaultMutableTreeNode node)
        {
        this.hostTreeNode = node;
        }


    /***********************************************************************************************
     * Get the UIComponentPlugin.
     *
     * @return UIComponentPlugin
     */

    public final UIComponentPlugin getUIComponent()
        {
        return (this.uiPlugin);
        }


    /***********************************************************************************************
     * Set the UIComponentPlugin.
     *
     * @param uicomponent
     */

    public final void setUIComponent(final UIComponentPlugin uicomponent)
        {
        this.uiPlugin = uicomponent;
        }


    /***********************************************************************************************
     * Get the List of Metadata below this node.
     *
     * @return List<Metadata>
     */

    public final List<Metadata> getChildMetadata()
        {
        return (this.listMetadata);
        }


    /***********************************************************************************************
     * Set the List of Metadata below this node.
     *
     * @param metadata
     */

    public final void setChildMetadata(final List<Metadata> metadata)
        {
        this.listMetadata = metadata;
        }


    /***********************************************************************************************
     * Override toString() in order to be able to name this object for the tree.
     *
     * @return String
     */

    public final String toString()
        {
        return (getName());
        }
    }
