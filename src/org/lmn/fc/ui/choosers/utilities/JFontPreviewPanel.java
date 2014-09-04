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

package org.lmn.fc.ui.choosers.utilities;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;


/**
 * A simple panel that renders a font preview for {@link JFontChooser JFontChooser} component.
 */

public class JFontPreviewPanel extends JPanel
    {
    // Injections
    private Font font;


    /**
     * Constructs a font preview panel initialized to the specified font.
     *
     * @param f The font used to render the preview
     */
    public JFontPreviewPanel(final Font f)
        {
        super();

        setFont(f);
        setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED), "Preview"));
        }


    /**
     * Sets the font used to render the preview text.
     *
     * @param f The font used to render the preview
     */
    public void setFont(final Font f)
        {
        this.font = f;
        repaint();
        }


    public void update(final Graphics g)
        {
        paintComponent(g);
        paintBorder(g);
        }


    public void paintComponent(final Graphics g)
        {
        final Image osi = createImage(getSize().width, getSize().height);
        final Graphics osg = osi.getGraphics();

        osg.setFont(this.font);

        final Rectangle2D bounds = font.getStringBounds(font.getFamily(), 0,
                                                  font.getFamily().length(),
                                                  new FontRenderContext(null, true, false));

        //final int width = (new Double(bounds.getWidth())).intValue();
        final int height = (new Double(bounds.getHeight())).intValue();
        osg.drawString(font.getFamily(), 5, (((getSize().height - height) / 2) + height));

        g.drawImage(osi, 0, 0, this);
        }


    public Dimension getPreferredSize()
        {
        return new Dimension(getSize().width, 75);
        }


    public Dimension getMinimumSize()
        {
        return getPreferredSize();
        }
    }