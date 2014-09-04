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
import org.lmn.fc.ui.UIComponentPlugin;

import java.awt.*;
import java.util.List;


/***************************************************************************************************
 * ImageUIComponentInterface.
 */

public interface ImageUIComponentInterface extends UIComponentPlugin
    {
    /***********************************************************************************************
     * Get the List of Metadata for the Image.
     *
     * @return List<Metadata>
     */

    List<Metadata> getMetadata();


    /***********************************************************************************************
     * Set the List of Metadata for this Image.
     *
     * @param metadata
     */

    void setMetadata(List<Metadata> metadata);


    /***********************************************************************************************
     * Get the Image to be shown on the ImageUIComponent.
     *
     * @return Image
     */

    Image getImage();


    /***********************************************************************************************
     * Set the Image to be shown on the ImageUIComponent.
     *
     * @param image
     */

    void setImage(Image image);


    /***********************************************************************************************
     * Refresh the Image on a separate thread.
     */

    void refreshImage();
    }
