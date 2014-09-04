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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.sda;


import org.lmn.fc.model.plugins.AtomPlugin;
import org.lmn.fc.model.registry.RegistryModelUtilities;

import javax.swing.*;


/***************************************************************************************************
 * DatasetState indicates Attach, Detach or Locked (primary dataset).
 */

public enum DatasetState
    {
    ATTACHED    (0, "Attached", "toolbar-attach-dataset-combo.png"),
    DETACHED    (1, "Detached", ""),
    LOCKED      (2, "Locked",   "toolbar-lock-dataset-combo.png");


    private final int intStateIndex;
    private final String strName;
    private final String strIconFilename;


    /***********************************************************************************************
     * DatasetState.
     *
     * @param mode
     * @param name
     * @param iconfilename
     */

    private DatasetState(final int mode,
                         final String name,
                         final String iconfilename)
        {
        this.intStateIndex = mode;
        this.strName = name;
        this.strIconFilename = iconfilename;
        }


    /***********************************************************************************************
     * Get the DatasetState.
     *
     * @return int
     */

    public int getDatasetStateIndex()
        {
        return (this.intStateIndex);
        }


    /***********************************************************************************************
     * Get the DatasetState Name.
     *
     * @return String
     */

    public String getName()
        {
        return (this.strName);
        }


    /***********************************************************************************************
     * Get the DatasetState Icon Filename.
     *
     * @return String
     */

    public String getIconFilename()
        {
        return (this.strIconFilename);
        }


    /***********************************************************************************************
     * Get the DatasetState Icon from the host Atom.
     *
     * @param atom
     *
     * @return String
     */

    public Icon getIcon(final AtomPlugin atom)
        {
        return (RegistryModelUtilities.getAtomIcon(atom, this.strIconFilename));
        }


    /***********************************************************************************************
     * Get the DatasetState Icon Filename.
     *
     * @return String
     */

    public String toString()
        {
        return (this.strIconFilename);
        }
    }
