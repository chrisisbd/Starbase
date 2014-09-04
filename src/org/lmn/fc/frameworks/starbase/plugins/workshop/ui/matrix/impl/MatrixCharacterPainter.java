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

package org.lmn.fc.frameworks.starbase.plugins.workshop.ui.matrix.impl;

import org.lmn.fc.frameworks.starbase.plugins.workshop.ui.matrix.MatrixCodePanelInterface;
import org.lmn.fc.model.plugins.AtomPlugin;
import org.lmn.fc.model.registry.RegistryModelUtilities;

import java.awt.*;
import java.awt.image.ImageObserver;


public final class MatrixCharacterPainter implements ImageObserver
    {
    // _numOfCharacters should indicate the number of characters (rows) in the image
    private static final int _numOfCharacters = 27;

    private int _charHeight;
    private int _charWidth;
    private final Image _image;
    private AtomPlugin hostPlugin;


    /**
     * Creates a new instance of MatrixCharacterPainter
     */
    // we assume the image contains two columns of characters, where
    // the second column contains the bright versions of the characters
    // used for initial character paint.

    MatrixCharacterPainter(final AtomPlugin plugin,
                           final MatrixCodePanelInterface codepanel)
        {
        hostPlugin = plugin;

        int imageWidth, imageHeight;
        _image = RegistryModelUtilities.getAtomIcon(hostPlugin,
                                                    codepanel.getFontFileName()).getImage();

        if (_image != null)
            {
            do
                {
                imageWidth = _image.getWidth(this);
                }
            while (imageWidth == -1);

            do
                {
                imageHeight = _image.getHeight(this);
                }
            while (imageHeight == -1);

            // There are two columns of characters in the image
            // The second column is the brighter version
            _charWidth = imageWidth >> 1;
            _charHeight = imageHeight / _numOfCharacters;

            System.out.println("_charWidth="+ getCharWidth());
            System.out.println("_charHeight="+ getCharHeight());
            }
        else
            {
            System.out.println("IMAGE WAS NULL");
            }
        }


    // paints a character at the specified position
    // x and y specify the column and row
    public final void paintCharacter(final int charNum,
                                     final Graphics g2,
                                     final int col,
                                     final int row,
                                     final boolean bright)
        {
        final int x;
        final int y;
        final int _x;
        final int _y;

        if ((getCharWidth() > 0)
            && (getCharHeight() > 0)
            && (_image != null))
            {
            x = col * getCharWidth();
            y = row * getCharHeight();

            _x = bright ? getCharWidth() : 0;
            _y = charNum * getCharHeight();

            g2.drawImage(_image,
                         x,
                         y,
                         x + getCharWidth(),
                         y + getCharHeight(),
                         _x,
                         _y,
                         _x + getCharWidth(),
                         _y + getCharHeight(),
                         this);
            }
        else
            {
            System.out.println("can't paint");
            }
        }

    // fades the character at the specified position
    public final void fadeCharacter()
        {

        }

    // totally erases the character at the specified position
    public final void eraseCharacter(final Graphics graphics,
                                     final int col,
                                     final int row)
        {
        if ((getCharWidth() > 0)
            && (getCharHeight() > 0)
            && (col >= 0)
            && (row >= 0)
            && (graphics != null))
            {
            graphics.clearRect(col * getCharWidth(),
                               row * getCharHeight(),
                               getCharWidth(),
                               getCharHeight());
            }
        else
            {
            System.out.println("can't clear rect");
            }
        }

    public final int getNumOfCharacters()
        {
        return _numOfCharacters;
        }

    public final int getCharWidth()
        {
        return _charWidth;
        }

    public final int getCharHeight()
        {
        return _charHeight;
        }

    // need this method to support ImageObserver interface for async awt method calls
    public final boolean imageUpdate(final Image img,
                                     final int infoflags,
                                     final int x,
                                     final int y,
                                     final int width,
                                     final int height)
        {
        return false;
        }

    }
