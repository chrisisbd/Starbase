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

import org.lmn.fc.common.utilities.misc.Utilities;
import org.lmn.fc.frameworks.starbase.plugins.workshop.ui.matrix.MatrixCodePanelInterface;
import org.lmn.fc.model.plugins.AtomPlugin;
import org.lmn.fc.ui.components.UIComponent;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;


public class MatrixCodePanel2 extends UIComponent
                             implements MatrixCodePanelInterface,
                                        ImageObserver,
                                        Runnable
    {
    final AtomPlugin hostPlugin;
    private Thread anim;
    private BufferedImage _bimg;
    private MatrixCharacterPainter painter;
    private int _numOfCharacters;
    private int countColumns;
    private int _numOfRows;
    private int _charHeight;
    private int _charWidth;

    private final int _delay = 500;

    private LineOfCode arrayLinesOfCode[];


    /***********************************************************************************************
     *
     * @param atom
     */

    public MatrixCodePanel2(final AtomPlugin atom)
        {
        super();

        this.hostPlugin = atom;
        }


    public void initialiseUI()
        {
        System.out.println("MatrixCodePanel initialiseUI");
//        setBackground(Color.black);
//        setMinimumSize(new Dimension(200, 200));
//        setPreferredSize(new Dimension(200, 200));
//
//        painter = new MatrixCharacterPainter(this.hostPlugin, this);
//
//        if (painter != null)
//            {
//            _numOfCharacters = painter.getNumOfCharacters();
//            _charHeight = painter.getCharHeight();
//            _charWidth = painter.getCharWidth();
//
//            System.out.println("chars=" + _numOfCharacters);
//            System.out.println("_charWidth="+ _charWidth);
//            System.out.println("_charHeight="+ _charHeight);
//
//            final Dimension d = getSize();
//            if ((d.width == 0)
//                || (d.height == 0))
//                {
//                System.out.println("size gone in init of panel");
//                }
//
//            countColumns = d.width / _charWidth;
//            _numOfRows = d.height / _charHeight;
//            System.out.println("painter cols=" + countColumns);
//            System.out.println("painter rows=" + _numOfRows);
//
//            arrayLinesOfCode = new LineOfCode[countColumns];
//
//            for (int i = 0; i < countColumns; i++)
//                {
//                System.out.println("fill arrayLinesOfCode i=" + i);
//                arrayLinesOfCode[i] = new LineOfCode(i, this);
//                }
//            }
//        else
//            {
//            System.out.println("painter was NULL");
//            }
        }

    public final void runUI()
        {
        System.out.println("panel run ui");
        setBackground(Color.black);
        setMinimumSize(new Dimension(200, 200));
        setPreferredSize(new Dimension(200, 200));

//        painter = new MatrixCharacterPainter(this.hostPlugin, this);
//
//        if (painter != null)
//            {
//            _numOfCharacters = painter.getNumOfCharacters();
//            _charHeight = painter.getCharHeight();
//            _charWidth = painter.getCharWidth();
//
//            System.out.println("chars=" + _numOfCharacters);
//            System.out.println("_charWidth="+ _charWidth);
//            System.out.println("_charHeight="+ _charHeight);
//
//            final Dimension d = getSize();
//            if ((d.width == 0)
//                || (d.height == 0))
//                {
//                System.out.println("size gone in init of panel");
//                }
//
//            countColumns = d.width / _charWidth;
//            _numOfRows = d.height / _charHeight;
//            System.out.println("painter cols=" + countColumns);
//            System.out.println("painter rows=" + _numOfRows);
//
//            arrayLinesOfCode = new LineOfCode[countColumns];
//
//            for (int i = 0; i < countColumns; i++)
//                {
//                System.out.println("fill arrayLinesOfCode i=" + i);
//                arrayLinesOfCode[i] = new LineOfCode(i, this);
//                }
//            }
//        else
//            {
//            System.out.println("painter was NULL");
//            }
        // ToDo Consider SwingWorker
        anim = new Thread(REGISTRY.getThreadGroup(),
                          this, "" +
            "Thread MatrixCodePanel2");
        anim.start();
        }

    public final synchronized void stopUI()
        {
        anim = null;
        notify();
        }


    public final MatrixCharacterPainter getCharacterPainter()
        {
        return painter;
        }

    public final int getNumOfColumns()
        {
        return countColumns;
        }

    public final int getNumOfRows()
        {
        return _numOfRows;
        }

    public String getFontFileName()
        {
        return "small_matrix_font.png";
        }

    private void updateBuffer(final int w,
                              final int h,
                              final Graphics2D graphics)
        {
        for (int i = 0;
             i < countColumns;
             i++)
            {
            System.out.println("updateBuffer: paint column=" + i);
            arrayLinesOfCode[i].paint(graphics);
            }
        }


    private Graphics2D createGraphics2D(final int w, final int h)
        {
        final Graphics2D g2;

        if (_bimg == null || _bimg.getWidth() != w || _bimg.getHeight() != h)
            {
            _bimg = (BufferedImage) createImage(w, h);
            }

        g2 = _bimg.createGraphics();
        g2.setBackground(getBackground());
        g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                            RenderingHints.VALUE_RENDER_QUALITY);
        return g2;
        }


    public final void paint(final Graphics g)
        {
        final Dimension d = getSize();
        final Graphics2D g2;

        //System.out.println("MATRIX PAINT");

        if ((d.width > 0)
            && (d.height > 0))
            {
            g2 = createGraphics2D(d.width, d.height);

            // draw to buffer
            updateBuffer(d.width, d.height, g2);
            g2.dispose();

            // copy buffer image to screen
            g.drawImage(_bimg, 0, 0, this);
            }
        else
            {
            System.out.println("panel can't paint");
            }
        }

    public final boolean imageUpdate(final Image img, final int infoflags,
                                     final int x, final int y, final int width, final int height)
        {
        return false;
        }

    public final synchronized void run()
        {
        while ((anim != null)
            && (!anim.isInterrupted()))
            {
            Utilities.safeSleep(_delay);
//
//            try
//                {
//                Thread.sleep(_delay);
//                //System.out.println("WAITING IN MATRIX");
//                }
//            catch (InterruptedException e)
//                {
//                return;
//                }
//
            //paint(this.getGraphics());
             repaint();
            }
        }
    }
