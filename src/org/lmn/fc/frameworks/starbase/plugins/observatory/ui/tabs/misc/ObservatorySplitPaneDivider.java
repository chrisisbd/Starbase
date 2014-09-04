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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.misc;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import java.awt.*;


// See: http://demo.spars.info/j/frameset.cgi?compo_id=365822&CASE=1&location=1111111111111111111&mode=frameset&MORPHO=1&ref=3&LANG=1

/***************************************************************************************************
 * ObservatorySplitPaneDivider.
 */

public final class ObservatorySplitPaneDivider extends BasicSplitPaneDivider
    {
    /***********************************************************************************************
     * Construct an ObservatorySplitPaneDivider.
     *
     * @param ui
     */

    public ObservatorySplitPaneDivider(final BasicSplitPaneUI ui)
        {
        super(ui);
        }


    /***********************************************************************************************
     *
     * @return JButton
     */

    protected JButton createLeftOneTouchButton()
        {
        final JButton button;

        button = new JButton()
            {
            /***************************************************************************************`
             * Remove the Border.
             *
             * @param border
             */

            public void setBorder(final Border border)
                {
                }


            /***************************************************************************************
             *
             * @param graphics
             */

            public void paint(final Graphics graphics)
                {
                if (splitPane != null)
                    {
                    final int[] xs = new int[3];
                    final int[] ys = new int[3];
                    final int blockSize;

                    // Fill the background first ...
                    graphics.setColor(this.getBackground());
                    graphics.fillRect(0,
                               0,
                               this.getWidth(),
                               this.getHeight());

                    // ... then draw the arrow.
                    graphics.setColor(Color.yellow);
                    if (orientation == JSplitPane.VERTICAL_SPLIT)
                        {
                        blockSize = Math.min(getHeight(), ONE_TOUCH_SIZE);
                        xs[0] = blockSize;
                        xs[1] = 0;
                        xs[2] = blockSize << 1;
                        ys[0] = 0;
                        ys[1] = ys[2] = blockSize;
                        graphics.drawPolygon(xs,
                                      ys,
                                      3); // Little trick to make the
                                          // arrows of equal size
                        }
                    else
                        {
                        blockSize = Math.min(getWidth(), ONE_TOUCH_SIZE);
                        xs[0] = xs[2] = blockSize;
                        xs[1] = 0;
                        ys[0] = 0;
                        ys[1] = blockSize;
                        ys[2] = blockSize << 1;
                        }

                    graphics.fillPolygon(xs,
                                  ys,
                                  3);
                    }
                }


            // Don't want the button to participate in focus traversable.
            public boolean isFocusTraversable()
                {
                return false;
                }

            public boolean isFocusable()
                {
                return false;
                }
            };

        button.setMinimumSize(new Dimension(ONE_TOUCH_SIZE, ONE_TOUCH_SIZE));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setRequestFocusEnabled(false);

        return button;
        }


    /***********************************************************************************************
     *
     * @return JButton
     */

    protected JButton createRightOneTouchButton()
        {
        final JButton button;

        button = new JButton()
            {
            /***************************************************************************************`
             * Remove the Border.
             *
             * @param border
             */

            public void setBorder(final Border border)
                {
                }


            public void paint(final Graphics g)
                {
                if (splitPane != null)
                    {
                    final int[] xs = new int[3];
                    final int[] ys = new int[3];
                    final int blockSize;

                    // Fill the background first ...
                    g.setColor(this.getBackground());
                    g.fillRect(0,
                               0,
                               this.getWidth(),
                               this.getHeight());

                    // ... then draw the arrow.
                    if (orientation == JSplitPane.VERTICAL_SPLIT)
                        {
                        blockSize = Math.min(getHeight(), ONE_TOUCH_SIZE);
                        xs[0] = blockSize;
                        xs[1] = blockSize << 1;
                        xs[2] = 0;
                        ys[0] = blockSize;
                        ys[1] = ys[2] = 0;
                        }
                    else
                        {
                        blockSize = Math.min(getWidth(), ONE_TOUCH_SIZE);
                        xs[0] = xs[2] = 0;
                        xs[1] = blockSize;
                        ys[0] = 0;
                        ys[1] = blockSize;
                        ys[2] = blockSize << 1;
                        }

                    g.setColor(Color.yellow);
                    g.fillPolygon(xs,
                                  ys,
                                  3);
                    }
                }


            // Don't want the button to participate in focus traversable.
            public boolean isFocusTraversable()
                {
                return false;
                }

            public boolean isFocusable()
                {
                return false;
                }
            };

        button.setMinimumSize(new Dimension(ONE_TOUCH_SIZE, ONE_TOUCH_SIZE));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setRequestFocusEnabled(false);

        return button;
        }
    }
