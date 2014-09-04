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

import com.sun.java.swing.plaf.motif.MotifSliderUI;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.MouseInputAdapter;
import javax.swing.plaf.ComponentUI;
import java.awt.*;


public class MotifMThumbSliderUI extends MotifSliderUI
        implements MThumbSliderAdditional
    {

    MThumbSliderAdditionalUI additionalUi;
    MouseInputAdapter        mThumbTrackListener;


    public static ComponentUI createUI(JComponent c)
        {
        return new MotifMThumbSliderUI((JSlider) c);
        }


    public MotifMThumbSliderUI()
        {
        super(null);
        }


    public MotifMThumbSliderUI(JSlider b)
        {
        super(b);
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


    Rectangle zeroRect = new Rectangle();


    public void paint(Graphics g,
                      JComponent c)
        {

        Rectangle clip = g.getClipBounds();
        thumbRect = zeroRect;

        super.paint(g, c);

        int thumbNum = additionalUi.getThumbNum();
        Rectangle[] thumbRects = additionalUi.getThumbRects();

        for (int i = thumbNum - 1;
             0 <= i;
             i--)
            {
            if (clip.intersects(thumbRects[i]))
                {
                thumbRect = thumbRects[i];
                paintThumb(g);
                }
            }
        }


    protected void installKeyboardActions(JSlider slider)
        {
        }


    protected void uninstallKeyboardActions(JSlider slider)
        {
        }


    public void scrollByBlock(int direction)
        {
        }


    public void scrollByUnit(int direction)
        {
        }


    //
    // MThumbSliderAdditional
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

