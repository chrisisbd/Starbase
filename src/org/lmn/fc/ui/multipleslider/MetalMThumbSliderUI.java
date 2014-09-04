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

package org.lmn.fc.ui.multipleslider;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.MouseInputAdapter;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.MetalSliderUI;
import java.awt.*;


public class MetalMThumbSliderUI extends MetalSliderUI
        implements MThumbSliderAdditional
    {

    MThumbSliderAdditionalUI additionalUi;
    MouseInputAdapter        mThumbTrackListener;


    public static ComponentUI createUI(JComponent c)
        {
        return new MetalMThumbSliderUI((JSlider) c);
        }


    public MetalMThumbSliderUI()
        {
        //super(null);
        }


    public MetalMThumbSliderUI(JSlider b)
        {
        //super(null);
        }


    public void installUI(JComponent c)
        {
        additionalUi = new MThumbSliderAdditionalUI(this);
        additionalUi.installUI(c);
        mThumbTrackListener = createMThumbTrackListener((JSlider) c);
        super.installUI(c);
        }


    public void uninstallUI(JComponent c)
        {
        super.uninstallUI(c);
        additionalUi.uninstallUI(c);
        additionalUi = null;
        mThumbTrackListener = null;
        }


    protected MouseInputAdapter createMThumbTrackListener(JSlider slider)
        {
        return additionalUi.trackListener;
        }


    protected TrackListener createTrackListener(JSlider slider)
        {
        return null;
        }


    protected ChangeListener createChangeListener(JSlider slider)
        {
        return additionalUi.changeHandler;
        }


    protected void installListeners(JSlider slider)
        {
        slider.addMouseListener(mThumbTrackListener);
        slider.addMouseMotionListener(mThumbTrackListener);
        slider.addFocusListener(focusListener);
        slider.addComponentListener(componentListener);
        slider.addPropertyChangeListener(propertyChangeListener);
        slider.getModel().addChangeListener(changeListener);
        }


    protected void uninstallListeners(JSlider slider)
        {
        slider.removeMouseListener(mThumbTrackListener);
        slider.removeMouseMotionListener(mThumbTrackListener);
        slider.removeFocusListener(focusListener);
        slider.removeComponentListener(componentListener);
        slider.removePropertyChangeListener(propertyChangeListener);
        slider.getModel().removeChangeListener(changeListener);
        }


    protected void calculateGeometry()
        {
        super.calculateGeometry();
        additionalUi.calculateThumbsSize();
        additionalUi.calculateThumbsLocation();
        }


    protected void calculateThumbLocation()
        {
        }


    Icon thumbRenderer;


    public void paint(Graphics g,
                      JComponent c)
        {
        Rectangle clip = g.getClipBounds();
        Rectangle[] thumbRects = additionalUi.getThumbRects();
        thumbRect = thumbRects[0];
        int thumbNum = additionalUi.getThumbNum();

        if (slider.getPaintTrack() && clip.intersects(trackRect))
            {
            boolean filledSlider_tmp = filledSlider;
            filledSlider = false;
            paintTrack(g);
            filledSlider = filledSlider_tmp;

            if (filledSlider)
                {
                g.translate(trackRect.x, trackRect.y);

                java.awt.Point t1 = new java.awt.Point(0, 0);
                java.awt.Point t2 = new java.awt.Point(0, 0);
                Rectangle maxThumbRect = new Rectangle(thumbRect);
                thumbRect = maxThumbRect;

                if (slider.getOrientation() == JSlider.HORIZONTAL)
                    {
                    t2.y = (trackRect.height - 1) - getThumbOverhang();
                    t1.y = t2.y - (getTrackWidth() - 1);
                    t2.x = trackRect.width - 1;
                    int maxPosition = xPositionForValue(slider.getMaximum());
                    thumbRect.x = maxPosition - (thumbRect.width / 2) - 2;
                    thumbRect.y = trackRect.y;
                    }
                else
                    {
                    t1.x = (trackRect.width - getThumbOverhang()) - getTrackWidth();
                    t2.x = (trackRect.width - getThumbOverhang()) - 1;
                    t2.y = trackRect.height - 1;
                    int maxPosition = yPositionForValue(slider.getMaximum());
                    thumbRect.x = trackRect.x;
                    thumbRect.y = maxPosition - (thumbRect.height / 2) - 2;
                    }

                Color fillColor = ((MThumbSlider) slider).getTrackFillColor();
                if (fillColor == null)
                    {
                    fillColor = MetalLookAndFeel.getControlShadow();
                    }
                fillTrack(g, t1, t2, fillColor);

                for (int i = thumbNum - 1;
                     0 <= i;
                     i--)
                    {
                    thumbRect = thumbRects[i];
                    fillColor = ((MThumbSlider) slider).getFillColorAt(i);
                    if (fillColor == null)
                        {
                        fillColor = MetalLookAndFeel.getControlShadow();
                        }
                    fillTrack(g, t1, t2, fillColor);
                    }

                g.translate(-trackRect.x, -trackRect.y);
                }
            }
        if (slider.getPaintTicks() && clip.intersects(tickRect))
            {
            paintTicks(g);
            }
        if (slider.getPaintLabels() && clip.intersects(labelRect))
            {
            paintLabels(g);
            }

        for (int i = thumbNum - 1;
             0 <= i;
             i--)
            {
            if (clip.intersects(thumbRects[i]))
                {
                thumbRect = thumbRects[i];
                thumbRenderer = ((MThumbSlider) slider).getThumbRendererAt(i);
                if (thumbRenderer == null)
                    {
                    if (slider.getOrientation() == JSlider.HORIZONTAL)
                        {
                        thumbRenderer = horizThumbIcon;
                        }
                    else
                        {
                        thumbRenderer = vertThumbIcon;
                        }
                    }
                paintThumb(g);
                }
            }
        }


    public void paintThumb(Graphics g)
        {
        thumbRenderer.paintIcon(slider, g, thumbRect.x, thumbRect.y);
        }


    public void fillTrack(Graphics g,
                          java.awt.Point t1,
                          java.awt.Point t2,
                          Color fillColor)
        {
        //				     t1-------------------
        //				     |			 |
        //				     --------------------t2
        int middleOfThumb = 0;

        if (slider.getOrientation() == JSlider.HORIZONTAL)
            {
            middleOfThumb = thumbRect.x + (thumbRect.width / 2) - trackRect.x;
            if (slider.isEnabled())
                {
                g.setColor(fillColor);
                g.fillRect(t1.x + 2,
                           t1.y + 2,
                           middleOfThumb - t1.x - 1,
                           t2.y - t1.y - 3);
                g.setColor(fillColor.brighter());
                g.drawLine(t1.x + 1, t1.y + 1, middleOfThumb, t1.y + 1);
                g.drawLine(t1.x + 1, t1.y + 1, t1.x + 1, t2.y - 2);
                }
            else
                {
                g.setColor(fillColor);
                g.fillRect(t1.x,
                           t1.y,
                           middleOfThumb - t1.x + 2,
                           t2.y - t1.y);
                }
            }
        else
            {
            middleOfThumb = thumbRect.y + (thumbRect.height / 2) - trackRect.y;
            if (slider.isEnabled())
                {
                g.setColor(slider.getBackground());
                g.drawLine(t1.x + 1, middleOfThumb, t2.x - 2, middleOfThumb);
                g.drawLine(t1.x + 1, middleOfThumb, t1.x + 1, t2.y - 2);
                g.setColor(fillColor);
                g.fillRect(t1.x + 2,
                           middleOfThumb + 1,
                           t2.x - t1.x - 3,
                           t2.y - 2 - middleOfThumb);
                }
            else
                {
                g.setColor(fillColor);
                g.fillRect(t1.x,
                           middleOfThumb + 2,
                           t2.x - 1 - t1.x,
                           t2.y - t1.y);
                }
            }
        }


    public void scrollByBlock(int direction)
        {
        }


    public void scrollByUnit(int direction)
        {
        }


    //
    //  MThumbSliderAdditional
    //
    /***********************************************************************************************
     * Get the AdditionalUI.
     *
     * @return MThumbSliderAdditionalUI
     */

    public MThumbSliderAdditionalUI getAdditionalUI()
        {
        return(this.additionalUi);
        }



    public Rectangle getTrackRect()
    {
    return trackRect;
    }


    public Dimension getThumbSize()
        {
        return super.getThumbSize();
        }


    public int xPositionForValue(int value)
        {
        return super.xPositionForValue(value);
        }


    public int yPositionForValue(int value)
        {
        return super.yPositionForValue(value);
        }


    }

