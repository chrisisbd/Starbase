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
import javax.swing.plaf.basic.BasicSliderUI;
import java.awt.*;


public class BasicMThumbSliderUI extends BasicSliderUI
                                 implements MThumbSliderAdditional
    {

    MThumbSliderAdditionalUI additionalUi;
    MouseInputAdapter        mThumbTrackListener;


    public static ComponentUI createUI(final JComponent c)
        {
        return new BasicMThumbSliderUI((JSlider) c);
        }


    public BasicMThumbSliderUI()
        {
        super(null);
        }


    public BasicMThumbSliderUI(final JSlider b)
        {
        super(b);
        }


    /***********************************************************************************************
     * Get the AdditionalUI.
     *
     * @return MThumbSliderAdditionalUI
     */

    public MThumbSliderAdditionalUI getAdditionalUI()
        {
        return(this.additionalUi);
        }


    public void installUI(final JComponent c)
        {
        additionalUi = new MThumbSliderAdditionalUI(this);
        additionalUi.installUI(c);
        mThumbTrackListener = createMThumbTrackListener((JSlider) c);
        super.installUI(c);
        }


    public void uninstallUI(final JComponent c)
        {
        super.uninstallUI(c);
        additionalUi.uninstallUI(c);
        additionalUi = null;
        mThumbTrackListener = null;
        }


    protected MouseInputAdapter createMThumbTrackListener(final JSlider mslider)
        {
        return additionalUi.trackListener;
        }


    protected TrackListener createTrackListener(final JSlider mslider)
        {
        return null;
        }


    protected ChangeListener createChangeListener(final JSlider mslider)
        {
        return additionalUi.changeHandler;
        }


    protected void installListeners(final JSlider mslider)
        {
        mslider.addMouseListener(mThumbTrackListener);
        mslider.addMouseMotionListener(mThumbTrackListener);
        mslider.addFocusListener(focusListener);
        mslider.addComponentListener(componentListener);
        mslider.addPropertyChangeListener(propertyChangeListener);
        mslider.getModel().addChangeListener(changeListener);
        }


    protected void uninstallListeners(final JSlider mslider)
        {
        mslider.removeMouseListener(mThumbTrackListener);
        mslider.removeMouseMotionListener(mThumbTrackListener);
        mslider.removeFocusListener(focusListener);
        mslider.removeComponentListener(componentListener);
        mslider.removePropertyChangeListener(propertyChangeListener);
        mslider.getModel().removeChangeListener(changeListener);
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


    Rectangle zeroRect = new Rectangle();


    public void paint(final Graphics graphics,
                      final JComponent c)
        {
        final int INDEX_LEFT = 0;
        final int INDEX_RIGHT = 1;
        final Rectangle clip;
        final Rectangle rectTrack;
        final int thumbNum;
        final Rectangle[] thumbRects;

        clip = graphics.getClipBounds();
        thumbRect = zeroRect;

        super.paint(graphics, c);

        thumbNum = additionalUi.getThumbNum();
        thumbRects = additionalUi.getThumbRects();
        rectTrack = additionalUi.getTrackRect();

        for (int i = thumbNum - 1;
             0 <= i;
             i--)
            {
            if ((clip != null)
                && (clip.intersects(thumbRects[i])))
                {
                final Color colorGraphics;

                thumbRect = thumbRects[i];

                colorGraphics = graphics.getColor();
                graphics.setColor(Color.gray);

                // Diagnostic
//                graphics.drawRect((int)rectTrack.getX(),
//                                  (int)rectTrack.getY(),
//                                  (int)rectTrack.getWidth(),
//                                  (int)rectTrack.getHeight());

                if (i == INDEX_LEFT)
                    {
                    graphics.fillRect((int)rectTrack.getX(), // - ((int) thumbRect.getWidth() >> 1),
                                      (int)rectTrack.getY(),
                                      (int)thumbRect.getX() - (int)rectTrack.getX(), // + ((int) thumbRect.getWidth() >> 1),
                                      (int)rectTrack.getHeight());
                    }
                else if (i == INDEX_RIGHT)
                    {
                    graphics.fillRect((int)thumbRect.getX() + (int)thumbRect.getWidth(),
                                      (int)rectTrack.getY(),
                                      ((int)rectTrack.getX() + (int)rectTrack.getWidth()) // + ((int) thumbRect.getWidth() >> 1))
                                            - ((int)thumbRect.getX() + (int)thumbRect.getWidth()),
                                      (int)rectTrack.getHeight());
                    }

                graphics.setColor(colorGraphics);
                paintThumb(graphics);
                }
            }
        }


    public void scrollByBlock(final int direction)
        {
        }


    public void scrollByUnit(final int direction)
        {
        }


    //
    // MThumbSliderAdditional
    //
    public Rectangle getTrackRect()
    {
    return trackRect;
    }


    public Dimension getThumbSize()
        {
        return super.getThumbSize();
        }


    public int xPositionForValue(final int value)
        {
        return super.xPositionForValue(value);
        }


    public int yPositionForValue(final int value)
        {
        return super.yPositionForValue(value);
        }

    }

