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
import javax.swing.plaf.SplitPaneUI;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import java.awt.*;


/***************************************************************************************************
 * UIUtilities.
 */

public final class UIUtilities
    {
    /***********************************************************************************************
     * Change the colour of a JSplitPane divider.
     *
     * @param pane
     * @param color
     */

    public static void setSplitPaneDividerColor(final JSplitPane pane,
                                                final Color color)
        {
        final SplitPaneUI splitUI;

        splitUI = pane.getUI();

        // Obviously this will not work if the ui doen't extend BasicSplitPaneUI
        if (splitUI instanceof BasicSplitPaneUI)
            {
            //System.out.println("set split colour");
            final BasicSplitPaneDivider divider;

            divider = ((BasicSplitPaneUI) splitUI).getDivider();

            if (divider != null)
                {
                //System.out.println("got divider");
                final Border divBorder;
                final Border newBorder;
                final Border colorBorder;

                divBorder = divider.getBorder();

                class FilledBorder implements Border
                    {
                    private Color color;
                    private final Insets NO_INSETS = new Insets(0, 0, 0, 0);
                    final Rectangle r = new Rectangle();

                    private FilledBorder(final Color bgcolor)
                        {
                        this.color = bgcolor;
                        }


                    /***********************************************************************************
                     *
                     * @param c
                     * @param g
                     * @param x
                     * @param y
                     * @param width
                     * @param height
                     */

                    public void paintBorder(final Component c,
                                            final Graphics g,
                                            final int x,
                                            final int y,
                                            final int width,
                                            final int height)
                        {
                        //System.out.println("paint border in set divider colour");
                        g.setColor(this.color);
                        g.fillRect(x, y, width, height);

                        if (c instanceof Container)
                            {
                            final Container cont;

                            cont = (Container) c;

                            for (int i = 0, n = cont.getComponentCount(); i < n; i++)
                                {
                                final Component comp;
                                final Graphics tmpg;

                                comp = cont.getComponent(i);
                                comp.getBounds(r);
                                tmpg = g.create(r.x, r.y, r.width, r.height);
                                comp.paint(tmpg);
                                tmpg.dispose();
                                }
                            }
                        }

                    public Insets getBorderInsets(final Component c)
                        {
                        return NO_INSETS;
                        }

                    public boolean isBorderOpaque()
                        {
                        return true;
                        }
                    }

                colorBorder = new FilledBorder(color);

                if (divBorder == null)
                    {
                    //System.out.println("added to null border");
                    newBorder = colorBorder;
                    }
                else
                    {
                    //System.out.println("compound border");
                    newBorder = BorderFactory.createCompoundBorder(divBorder, colorBorder);
                    }

                //System.out.println("set border on divider");
                divider.setBorder(newBorder);
                }
            }
        }


    /***********************************************************************************************
     * Gradient Fill a JSplitPane divider on the specified BasicSplitPaneUI,
     * by setting a filled Border.
     *
     * @param ui
     *
     * @return BasicSplitPaneUI
     */

    public static BasicSplitPaneUI addSplitPaneDividerGradientBorder(final BasicSplitPaneUI ui)
        {
        if (ui != null)
            {
            final BasicSplitPaneDivider divider;

            divider = ui.getDivider();

            if (divider != null)
                {
                final Border borderDivider;
                final Border borderNew;
                final Border borderFilled;

                borderDivider = divider.getBorder();

                // Create the filled Border
                borderFilled = new GradientBorder();

                if (borderDivider == null)
                    {
                    // Add a border where it did not exist
                    borderNew = borderFilled;
                    }
                else
                    {
                    // Add the new border to the original
                    borderNew = BorderFactory.createCompoundBorder(borderDivider, borderFilled);
                    }

                divider.setBorder(borderNew);
                //System.out.println("border set on divider");
                }
            }

        return (ui);
        }


    /***********************************************************************************************
     * Add Tooltips to the Splitter arrow icons.
     *
     * @param splitpane
     * @param hide
     * @param reveal
     */

    public static void addSplitPaneDividerTooltips(final JSplitPane splitpane,
                                                   final String hide,
                                                   final String reveal)
        {
        final int intComponentCount;

        // See: http://forums.sun.com/thread.jspa?threadID=714732&tstart=23790
        // How to add the ToolTip to the little widgets in the JSplitpane
        intComponentCount = splitpane.getComponentCount();

        for (int i = 0;
             i < intComponentCount;
             i++)
            {
            final Component component;

            component = splitpane.getComponent(i);

            // Find the Divider Component
            if (component instanceof BasicSplitPaneDivider)
                {
                final BasicSplitPaneDivider divider;
                final int subComponentCount;

                divider = (BasicSplitPaneDivider)component;

                subComponentCount = divider.getComponentCount();

                if (subComponentCount == 2)
                    {
                    if ((divider.getComponent(0) instanceof JButton)
                        && (divider.getComponent(1) instanceof JButton))
                        {
                        ((JButton)divider.getComponent(0)).setToolTipText(hide);
                        ((JButton)divider.getComponent(1)).setToolTipText(reveal);
                        //System.out.println("tooltips set");
                        }
                    }
                }
            }
        }


    /***********************************************************************************************
       * Add Tooltips to the Splitter arrow icons.
       *
       * @param splitpane
       * @param hide
       * @param reveal
       */

      public static void addTooltipsToSplitter(final JSplitPane splitpane,
                                               final String hide,
                                               final String reveal)
          {
          final int intComponentCount;

          // See: http://forums.sun.com/thread.jspa?threadID=714732&tstart=23790
          // How to add the ToolTip to the little widgets in the JSplitpane
          intComponentCount = splitpane.getComponentCount();

          for (int i = 0;
               i < intComponentCount;
               i++)
              {
              final Component component;

              component = splitpane.getComponent(i);

              if (component instanceof BasicSplitPaneDivider)
                  {
                  final BasicSplitPaneDivider divider;
                  final int subComponentCount;

                  divider = (BasicSplitPaneDivider)component;

                  subComponentCount = divider.getComponentCount();

                  if (subComponentCount == 2)
                      {
                      if ((divider.getComponent(0) instanceof JButton)
                          && (divider.getComponent(1) instanceof JButton))
                          {
                          ((JButton)divider.getComponent(0)).setToolTipText(hide);
                          ((JButton)divider.getComponent(1)).setToolTipText(reveal);
                          }
                      }
                  }
              }
          }

    }

