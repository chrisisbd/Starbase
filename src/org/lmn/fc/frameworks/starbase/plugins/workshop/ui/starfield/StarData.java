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

package org.lmn.fc.frameworks.starbase.plugins.workshop.ui.starfield;

import java.awt.*;

public final class StarData
    {
    private final int H;
    private final int V;
    private int x;
    private final int y;
    private int z;
    private final int type;

    public StarData(final int width,
                    final int height,
                    final int depth,
                    final int type)
        {
        this.type = type;
        H = width / 2;
        V = height / 2;
        x = (int) (Math.random() * width) - H;
        y = (int) (Math.random() * height) - V;
        if ((x == 0) && (y == 0)) x = 10;
        z = (int) (Math.random() * depth);
        }


    public void drawStar(final Graphics g,
                         final double rot)
        {
        final double X;
        final double Y;
        final int h;
        final int v;
        final int hh;
        final int vv;
        int d;

        z -= 2;
        if (z < -63) z = 100;
        hh = (x * 64) / (64 + z);
        vv = (y * 64) / (64 + z);
        X = (hh * Math.cos(rot)) - (vv * Math.sin(rot));
        Y = (hh * Math.sin(rot)) + (vv * Math.cos(rot));
        h = (int) X + H;
        v = (int) Y + V;

        if ((h < 0) || (h > (2 * H))) z = 100;
        if ((v < 0) || (v > (2 * H))) z = 100;

        Grey(g);

        if (type == 0)
            {
            d = (100 - z) / 50;
            if (d == 0) d = 1;
            g.fillRect(h, v, d, d);
            }
        else
            {
            d = (100 - z) / 20;
            g.drawLine(h - d, v, h + d, v);
            g.drawLine(h, v - d, h, v + d);

            if (z < 50)
                {
                d /= 2;
                g.drawLine(h - d, v - d, h + d, v + d);
                g.drawLine(h + d, v - d, h - d, v + d);
                }
            }
        }

    private void Grey(final Graphics g)
        {
        if (z > 75)
            {
            g.setColor(Color.DARK_GRAY);
            }
        else if (z > 50)
            {
            g.setColor(Color.BLUE);
            }
        else if (z > 25)
            {
            g.setColor(Color.ORANGE);
            }
        else
            {
            g.setColor(Color.white);
            }
        }
    }

