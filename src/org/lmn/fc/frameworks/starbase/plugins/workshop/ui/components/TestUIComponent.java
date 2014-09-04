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

package org.lmn.fc.frameworks.starbase.plugins.workshop.ui.components;

import info.clearthought.layout.TableLayout;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.ui.UIComponentPlugin;
import org.lmn.fc.ui.components.UIComponent;
import org.lmn.fc.ui.widgets.impl.ControlKnobUIComponent;

import javax.swing.*;
import java.awt.*;


public final class TestUIComponent extends UIComponent
                                   implements UIComponentPlugin
    {
    private static final Dimension DIM_KNOB   = new Dimension(91, 91);
    private static final int       SPACER     = 20;
    public static final  int       TICKLENGTH = 3;

    private ColourInterface colourCanvas;
    private ColourInterface colourGradientTop;
    private ColourInterface colourGradientBottom;
    private String          strName;


    /***********************************************************************************************
     * TestUIComponent.
     *
     * @param name
     */

    public TestUIComponent(final String name)
        {
        super();

        strName = name;
        colourCanvas = DEFAULT_COLOUR_CANVAS;
        colourGradientTop = DEFAULT_COLOUR_GRADIENT_TOP;
        colourGradientBottom = DEFAULT_COLOUR_GRADIENT_BOTTOM;

        initialiseKnobs();
        }


    /***********************************************************************************************
     * initialiseKnobs().
     */

    private void initialiseKnobs()
        {
        final JPanel panelKnobs;
        final double[][] size =
                {
                        { // Columns
                          TableLayout.FILL,
                          // 0
                          TableLayout.PREFERRED,
                          // 1
                          SPACER,
                          // 2
                          TableLayout.PREFERRED,
                          // 3
                          SPACER,
                          // 4
                          TableLayout.PREFERRED,
                          // 5
                SPACER,                 // 6
                TableLayout.PREFERRED,  // 7
                TableLayout.FILL        // 8
                },
                { // Rows
                TableLayout.FILL,       // 0
                TableLayout.PREFERRED,  // 1
                SPACER,                 // 2
                TableLayout.PREFERRED,  // 3
                SPACER,                 // 4
                TableLayout.PREFERRED,  // 5
                TableLayout.FILL        // 6
                }
            };

        panelKnobs = new JPanel();
        panelKnobs.setLayout(new TableLayout(size));

        panelKnobs.add(new ControlKnobUIComponent(DIM_KNOB, true, TICKLENGTH, -100, 100, "Knob", UIComponentPlugin.DEFAULT_COLOUR_TAB_BACKGROUND), "1, 1, c, c");
        panelKnobs.add(new ControlKnobUIComponent(DIM_KNOB, true, TICKLENGTH, -100, 100, "Knob", UIComponentPlugin.DEFAULT_COLOUR_TAB_BACKGROUND), "1, 3, c, c");
        panelKnobs.add(new ControlKnobUIComponent(DIM_KNOB, true, TICKLENGTH, -100, 100, "Knob", UIComponentPlugin.DEFAULT_COLOUR_TAB_BACKGROUND), "1, 5, c, c");

        panelKnobs.add(new ControlKnobUIComponent(DIM_KNOB, true, TICKLENGTH, -100, 100, "Knob", UIComponentPlugin.DEFAULT_COLOUR_TAB_BACKGROUND), "3, 1, c, c");
        panelKnobs.add(new ControlKnobUIComponent(DIM_KNOB, true, TICKLENGTH, -100, 100, "Knob", UIComponentPlugin.DEFAULT_COLOUR_TAB_BACKGROUND), "3, 3, c, c");
        panelKnobs.add(new ControlKnobUIComponent(DIM_KNOB, true, TICKLENGTH, -100, 100, "Knob", UIComponentPlugin.DEFAULT_COLOUR_TAB_BACKGROUND), "3, 5, c, c");

        panelKnobs.add(new ControlKnobUIComponent(DIM_KNOB, true, TICKLENGTH, -100, 100, "Knob", UIComponentPlugin.DEFAULT_COLOUR_TAB_BACKGROUND), "5, 1, c, c");
        panelKnobs.add(new ControlKnobUIComponent(DIM_KNOB, true, TICKLENGTH, -100, 100, "Knob", UIComponentPlugin.DEFAULT_COLOUR_TAB_BACKGROUND), "5, 3, c, c");
        panelKnobs.add(new ControlKnobUIComponent(DIM_KNOB, true, TICKLENGTH, -100, 100, "Knob", UIComponentPlugin.DEFAULT_COLOUR_TAB_BACKGROUND), "5, 5, c, c");

        panelKnobs.add(new ControlKnobUIComponent(DIM_KNOB, true, TICKLENGTH, -100, 100, "Knob", UIComponentPlugin.DEFAULT_COLOUR_TAB_BACKGROUND), "7, 1, c, c");
        panelKnobs.add(new ControlKnobUIComponent(DIM_KNOB, true, TICKLENGTH, -100, 100, "Knob", UIComponentPlugin.DEFAULT_COLOUR_TAB_BACKGROUND), "7, 3, c, c");
        panelKnobs.add(new ControlKnobUIComponent(DIM_KNOB, true, TICKLENGTH, -100, 100, "Knob", UIComponentPlugin.DEFAULT_COLOUR_TAB_BACKGROUND), "7, 5, c, c");

        add(panelKnobs, BorderLayout.CENTER);

//        knob.addChangeListener(new ChangeListener()
//            {
//            public void stateChanged(ChangeEvent event)
//                {
//                ControlKnobUIComponent knob = (ControlKnobUIComponent) event.getSource();
//                }
//            });


        }


    /***********************************************************************************************
     *
     * @param graphics
     */

//    public void paint(final Graphics graphics)
//        {
//        super.paint(graphics);
//
//        final GradientPaint gradientPaint;
//
//        // The original background colour cannot be seen if gradient paint is used
//        if ((graphics != null)
//            && (getGradientColourTop() != null)
//            && (getGradientColourBottom() != null))
//            {
//            gradientPaint = new GradientPaint(getWidth() >> 1,
//                                              0,
//                                              getGradientColourTop().getColor(),
//                                              getWidth() >> 1,
//                                              getHeight(),
//                                              getGradientColourBottom().getColor());
//
//            ((Graphics2D)graphics).setPaint(gradientPaint);
//            ((Graphics2D)graphics).fill(new Rectangle(getWidth(), getHeight()));
//            }
//
//        // Show the name over whatever background was painted
//        if ((graphics != null)
//            && (strName != null)
//            && (getWidth() > 0)
//            && (getHeight() > 0))
//            {
//            graphics.setColor(Color.ORANGE);
//            graphics.drawString(strName, getWidth()/3, getHeight() >> 1);
//            //System.out.println("drawString-->" + strName);
//            }
//
//        // For debugging (it was a very long day)
////        if ((REGISTRY_MODEL.getUserInterface() != null)
////            && (REGISTRY_MODEL.getUserInterface().getUI() != null)
////            && (REGISTRY_MODEL.getUserInterface().getUI().getJMenuBar() == null))
////            {
////            System.out.println("menu bar is NULL in blank ui");
////            }
//        }


    /***********************************************************************************************
     * A utility so other classes can get canvas colour.
     *
     * @return Color
     */

    public final Color getCanvasColour()
        {
        return (colourCanvas.getColor());
        }


    /***********************************************************************************************
     * A utility so other classes can set canvas colour.
     *
     * @param colour
     */

    public final void setCanvasColour(final ColourInterface colour)
        {
        if (colour != null)
            {
            colourCanvas = colour;
           // setBackground(colourCanvas.getColor());
            }
        }


    /***********************************************************************************************
     *
     * @return ColourPlugin
     */

    public final ColourInterface getGradientColourTop()
        {
        return (this.colourGradientTop);
        }


    /***********************************************************************************************
     *
     * @param colour
     */

    public final void setGradientColourTop(final ColourInterface colour)
        {
        colourGradientTop = colour;
        }


    /***********************************************************************************************
     *
     * @return ColourPlugin
     */

    public final ColourInterface getGradientColourBottom()
        {
        return (this.colourGradientBottom);
        }


    /***********************************************************************************************
     *
     * @param colour
     */

    public final void setGradientColourBottom(final ColourInterface colour)
        {
        colourGradientBottom = colour;
        }
    }
