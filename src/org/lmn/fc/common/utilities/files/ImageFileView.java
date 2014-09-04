// Copyright 2000, 2001, 2002, 2003, 04, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013, 2013
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

//--------------------------------------------------------------------------------------------------
// Utilities package

package org.lmn.fc.common.utilities.files;

import javax.swing.*;
import javax.swing.filechooser.FileView;
import java.io.File;

/* ImageFileView.java is a 1.4 example used by FileChooserDemo2.java. */
public class ImageFileView extends FileView {

    ImageIcon jpgIcon = FileUtilities.createImageIcon("images/jpgIcon.gif");
    ImageIcon gifIcon = FileUtilities.createImageIcon("images/gifIcon.gif");
    ImageIcon tiffIcon = FileUtilities.createImageIcon("images/tiffIcon.gif");
    ImageIcon pngIcon = FileUtilities.createImageIcon("images/pngIcon.png");

    public String getName(File f) {
        return null; //let the L&F FileView figure this out
    }

    public String getDescription(File f) {
        return null; //let the L&F FileView figure this out
    }

    public Boolean isTraversable(File f) {
        return null; //let the L&F FileView figure this out
    }

    public String getTypeDescription(File f) {
        String extension = FileUtilities.getExtension(f);
        String type = null;

        if (extension != null) {
            if (extension.equals(FileUtilities.jpeg) ||
                extension.equals(FileUtilities.jpg)) {
                type = "JPEG Image";
            } else if (extension.equals(FileUtilities.gif)){
                type = "GIF Image";
            } else if (extension.equals(FileUtilities.tiff) ||
                       extension.equals(FileUtilities.tif)) {
                type = "TIFF Image";
            } else if (extension.equals(FileUtilities.png)){
                type = "PNG Image";
            }
        }
        return type;
    }

    public Icon getIcon(File f) {
        String extension = FileUtilities.getExtension(f);
        Icon icon = null;

        if (extension != null) {
            if (extension.equals(FileUtilities.jpeg) ||
                extension.equals(FileUtilities.jpg)) {
                icon = jpgIcon;
            } else if (extension.equals(FileUtilities.gif)) {
                icon = gifIcon;
            } else if (extension.equals(FileUtilities.tiff) ||
                       extension.equals(FileUtilities.tif)) {
                icon = tiffIcon;
            } else if (extension.equals(FileUtilities.png)) {
                icon = pngIcon;
            }
        }
        return icon;
    }
}
