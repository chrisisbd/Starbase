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

import org.lmn.fc.common.constants.FrameworkSingletons;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MouseInputAdapter;
import javax.swing.plaf.basic.BasicSliderUI;
import java.awt.*;
import java.awt.event.MouseEvent;

// This was taken from elsewhere, and is a bit of a mess!

public class MThumbSliderAdditionalUI implements FrameworkSingletons
    {
    public static final int INDEX_LEFT = 0;
    public static final int INDEX_RIGHT = 1;


    MThumbSlider  mSlider;
    final BasicSliderUI ui;
    Rectangle[] rectThumbs;
    int intThumbCount;
    private transient boolean isDragging;
    Icon thumbRenderer;

    Rectangle rectTrack;

    ChangeHandler changeHandler;
    TrackListener trackListener;


    public MThumbSliderAdditionalUI(final BasicSliderUI theui)
        {
        this.ui = theui;
        }


    public void installUI(final JComponent c)
        {
        mSlider = (MThumbSlider) c;
        intThumbCount = mSlider.getThumbNum();
        rectThumbs = new Rectangle[intThumbCount];

        for (int i = 0;
             i < intThumbCount;
             i++)
            {
            rectThumbs[i] = new Rectangle();
            }

        isDragging = false;
        trackListener = new MThumbSliderAdditionalUI.TrackListener(mSlider);
        changeHandler = new ChangeHandler();
        }


    public void uninstallUI(final JComponent c)
        {
        rectThumbs = null;
        trackListener = null;
        changeHandler = null;
        }


    protected void calculateThumbsSize()
        {
        final Dimension size;

        size = ((MThumbSliderAdditional) ui).getThumbSize();

        for (int i = 0;
             i < intThumbCount;
             i++)
            {
            rectThumbs[i].setSize(size.width, size.height);
            }
        }


    protected void calculateThumbsLocation()
        {
        for (int intThumbIndex = 0;
             intThumbIndex < intThumbCount;
             intThumbIndex++)
            {
            if (mSlider.getSnapToTicks())
                {
                int tickSpacing = mSlider.getMinorTickSpacing();

                if (tickSpacing == 0)
                    {
                    tickSpacing = mSlider.getMajorTickSpacing();
                    }

                if (tickSpacing != 0)
                    {
                    final int sliderValue = mSlider.getValueAt(intThumbIndex);
                    final int snappedValue;
                    //int min = mSlider.getMinimumAt(i);
                    final int min = mSlider.getMinimum();

                    if ((sliderValue - min) % tickSpacing != 0)
                        {
                        final float temp = (float) (sliderValue - min) / (float) tickSpacing;
                        final int whichTick = Math.round(temp);
                        snappedValue = min + (whichTick * tickSpacing);

                        // This is only used if snapping to ticks
                        mSlider.setValueAt(snappedValue, intThumbIndex);
                        }
                    }
                }

            rectTrack = getTrackRect();

            if (mSlider.getOrientation() == JSlider.HORIZONTAL)
                {
                final int value;
                final int valuePosition;

                value = mSlider.getValueAt(intThumbIndex);
                valuePosition = ((MThumbSliderAdditional) ui).xPositionForValue(value);
                rectThumbs[intThumbIndex].x = valuePosition - (rectThumbs[intThumbIndex].width / 2);
                rectThumbs[intThumbIndex].y = rectTrack.y;
                }
            else
                {
                final int valuePosition;     // need

                valuePosition = ((MThumbSliderAdditional) ui).yPositionForValue(mSlider.getValueAt(intThumbIndex));
                rectThumbs[intThumbIndex].x = rectTrack.x;
                rectThumbs[intThumbIndex].y = valuePosition - (rectThumbs[intThumbIndex].height / 2);
                }
            }
        }


    public int getThumbNum()
        {
        return intThumbCount;
        }


    public Rectangle[] getThumbRects()
        {
        return rectThumbs;
        }


    private static final Rectangle unionRect = new Rectangle();


    public void setThumbLocationAt(final int x,
                                   final int y,
                                   final int index)
        {
        final Rectangle rect;

        rect = rectThumbs[index];
        unionRect.setBounds(rect);

        rect.setLocation(x, y);
        SwingUtilities.computeUnion(rect.x, rect.y, rect.width, rect.height, unionRect);
        mSlider.repaint(unionRect.x, unionRect.y, unionRect.width, unionRect.height);
        }


    public Rectangle getTrackRect()
        {
        return ((MThumbSliderAdditional) ui).getTrackRect();
        }

    /***********************************************************************************************
     * Get the TrackListener.
     *
     * @return TrackListener
     */

    public TrackListener getTrackListener()
        {
        return (this.trackListener);
        }



    /***********************************************************************************************
     * ChangeHandler.
     */

    public class ChangeHandler implements ChangeListener
        {
        /*******************************************************************************************
         * Repaint the Slider, if it is not moving.
         *
         * @param event
         */

        public void stateChanged(final ChangeEvent event)
            {
            final String SOURCE = "MThumbSliderAdditionalUI.ChangeHandler.stateChanged() ";

            LOGGER.debug(LOADER_PROPERTIES.isChartDebug(),
                         SOURCE + "[dragging=" + isDragging + "] [adjusting=" + mSlider.getValueIsAdjusting() + "]");

            // Only repaint the Slider if the Thumb has stopped moving
            if ((!isDragging)
                && (!mSlider.getValueIsAdjusting()))
                {
                calculateThumbsLocation();
                mSlider.repaint();

                LOGGER.debug(LOADER_PROPERTIES.isChartDebug(),
                             SOURCE + "REPAINT SLIDER");
                }
            }
        }


    /***********************************************************************************************
     * TrackListener.
     */

    public class TrackListener extends MouseInputAdapter
        {
        protected transient int offset;
        protected transient int currentMouseX;
        protected transient int currentMouseY;
        protected Rectangle adjustingThumbRect;
        protected int       adjustingThumbIndex;
        protected final MThumbSlider slider;
        protected Rectangle trackRectInner;


        /*******************************************************************************************
         * TrackListener.
         *
         * @param theslider
         */

        TrackListener(final MThumbSlider theslider)
            {
            this.slider = theslider;
            }


        /*******************************************************************************************
         * Press the Mouse.
         *
         * @param event
         */

        public void mousePressed(final MouseEvent event)
            {
            final String SOURCE = "MThumbSliderAdditionalUI.TrackListener.mousePressed() ";
            final int intModifiers;

            if (!slider.isEnabled())
                {
                return;
                }

            currentMouseX = event.getX();
            currentMouseY = event.getY();
            slider.requestFocus();
            isDragging = false;

            intModifiers = event.getModifiers();

            // Right click means Reset To Extents
            if (SwingUtilities.isRightMouseButton(event))
                {
                final int trackLeft;
                final int trackRight;

                LOGGER.debug(LOADER_PROPERTIES.isChartDebug(),
                             SOURCE + "RIGHT CLICK --> setting Slider & Models adjusting TRUE --> reset to extents");
                slider.setValueIsAdjusting(true);
                slider.getModelAt(INDEX_LEFT).setValueIsAdjusting(true);
                slider.getModelAt(INDEX_RIGHT).setValueIsAdjusting(true);

                // Track dimensions
                trackRectInner = getTrackRect();
                trackLeft = trackRectInner.x;
                trackRight = trackRectInner.x + (trackRectInner.width - 1);

                // Move the left to left extent
                moveThumbToAbsoluteX(trackLeft - ((int) rectThumbs[INDEX_LEFT].getWidth() >> 1),
                                     INDEX_LEFT,
                                     INDEX_RIGHT);

                // Move the right to right extent
                moveThumbToAbsoluteX(trackRight - ((int) rectThumbs[INDEX_RIGHT].getWidth() >> 1),
                                     INDEX_RIGHT,
                                     INDEX_LEFT);
                }
            else
                {
                LOGGER.debug(LOADER_PROPERTIES.isChartDebug(),
                             SOURCE + "LEFT CLICK --> setting adjusting TRUE ");

                // Left click moves the selected Thumb
                for (int intThumbIndex = 0;
                     intThumbIndex < intThumbCount;
                     intThumbIndex++)
                    {
                    final Rectangle rect;

                    rect = rectThumbs[intThumbIndex];

                    // Is this the selected Thumb?
                    if (rect.contains(currentMouseX, currentMouseY))
                        {
                        switch (slider.getOrientation())
                            {
                            case JSlider.VERTICAL:
                                offset = currentMouseY - rect.y;
                                break;

                            case JSlider.HORIZONTAL:
                                offset = currentMouseX - rect.x;
                                break;

                            default:
                                {
                                // Do nothing
                                }
                            }

                        isDragging = true;

                        adjustingThumbRect = rect;
                        adjustingThumbIndex = intThumbIndex;

                        // This Thumb IS adjusting
                        slider.setValueIsAdjusting(true);
                        slider.getModelAt(adjustingThumbIndex).setValueIsAdjusting(true);

                        // WARNING!!
                        return;
                        }
                    else
                        {
                        // This Thumb is NOT adjusting, so try the next
                        slider.getModelAt(intThumbIndex).setValueIsAdjusting(false);
                        }
                    }
                }
            }


        /*******************************************************************************************
         * Drag the Mouse.
         *
         * @param mouseevent
         */

        public void mouseDragged(final MouseEvent mouseevent)
            {
            final String SOURCE = "TrackListener.mouseDragged() ";

            if (!slider.isEnabled()
                || !isDragging
                || adjustingThumbRect == null)
                {
                // We end up here if the user is dragging on the slider bar, not a Thumb
                return;
                }

            switch (slider.getOrientation())
                {
                case JSlider.VERTICAL:
                    {
                    final int thumbMiddle;
                    final Rectangle rectThumb;
                    final int halfThumbHeight;
                    int thumbTop;
                    final int trackTop;
                    final int trackBottom;

                    currentMouseX = mouseevent.getX();
                    currentMouseY = mouseevent.getY();

                    rectThumb = rectThumbs[adjustingThumbIndex];
                    trackRectInner = getTrackRect();

                    halfThumbHeight = rectThumb.height / 2;
                    thumbTop = mouseevent.getY() - offset;
                    trackTop = trackRectInner.y;
                    trackBottom = trackRectInner.y + (trackRectInner.height - 1);

                    thumbTop = Math.max(thumbTop, trackTop - halfThumbHeight);
                    thumbTop = Math.min(thumbTop, trackBottom - halfThumbHeight);

                    setThumbLocationAt(rectThumb.x, thumbTop, adjustingThumbIndex);

                    thumbMiddle = thumbTop + halfThumbHeight;

                    setSliderValue(slider,
                                   ui.valueForYPosition(thumbMiddle),
                                   adjustingThumbIndex);
                    break;
                    }

                // Lock the Thumbs if control is down, but only if HORIZONTAL
                case JSlider.HORIZONTAL:
                    {
                    if ((mouseevent.isControlDown())
                        && (getThumbNum() == 2))
                        {
                        if (adjustingThumbIndex == INDEX_LEFT)
                            {
                            final Rectangle rectOriginalThumbLeft;
                            final Rectangle rectOriginalThumbRight;
                            final Rectangle rectMovedThumbLeft;
                            final Rectangle rectMovedThumbRight;
                            final int intSeparationX;

                            // This calls setValueAt()
                            rectOriginalThumbLeft = moveThumbToMouse(mouseevent,
                                                                     INDEX_LEFT,
                                                                     INDEX_RIGHT);
                            rectMovedThumbLeft = rectThumbs[INDEX_LEFT];

                            // Mouse is adjusting the Left, so move the locked right thumb
                            rectOriginalThumbRight = rectThumbs[INDEX_RIGHT];

                            intSeparationX = (int)rectOriginalThumbRight.getX() - (int)rectOriginalThumbLeft.getX();

                            // This calls setValueAt()
                            moveThumbToAbsoluteX((int) rectMovedThumbLeft.getX() + intSeparationX,
                                                 INDEX_RIGHT,
                                                 INDEX_LEFT);
                            rectMovedThumbRight = rectThumbs[INDEX_RIGHT];

                            debugLocations(LOADER_PROPERTIES.isChartDebug(),
                                           SOURCE + "Mouse is adjusting the Left, so move the locked right thumb",
                                           rectOriginalThumbLeft,
                                           rectOriginalThumbRight,
                                           rectMovedThumbLeft,
                                           rectMovedThumbRight,
                                           intSeparationX);
                            }
                        else if (adjustingThumbIndex == INDEX_RIGHT)
                            {
                            final Rectangle rectOriginalThumbLeft;
                            final Rectangle rectOriginalThumbRight;
                            final Rectangle rectMovedThumbLeft;
                            final Rectangle rectMovedThumbRight;
                            final int intSeparationX;

                            // This calls setValueAt()
                            rectOriginalThumbRight = moveThumbToMouse(mouseevent,
                                                                      INDEX_RIGHT,
                                                                      INDEX_LEFT);
                            rectMovedThumbRight = rectThumbs[INDEX_RIGHT];

                            // Mouse is adjusting Right, so need to move the left thumb too
                            rectOriginalThumbLeft = rectThumbs[INDEX_LEFT];

                            intSeparationX = (int)rectOriginalThumbRight.getX() - (int)rectOriginalThumbLeft.getX();

                            // This calls setValueAt()
                            moveThumbToAbsoluteX((int) rectThumbs[INDEX_RIGHT].getX() - intSeparationX,
                                                 INDEX_LEFT,
                                                 INDEX_RIGHT);
                            rectMovedThumbLeft = rectThumbs[INDEX_LEFT];

                            debugLocations(LOADER_PROPERTIES.isChartDebug(),
                                           SOURCE + "Mouse is adjusting Right, so move the left thumb too",
                                           rectOriginalThumbLeft,
                                           rectOriginalThumbRight,
                                           rectMovedThumbLeft,
                                           rectMovedThumbRight,
                                           intSeparationX);
                            }
                        else
                            {
                            LOGGER.debug(LOADER_PROPERTIES.isChartDebug(),
                                         SOURCE + "Invalid thumb index=" + adjustingThumbIndex);
                            }
                        }
                    else
                        {
                        // All done if there's only one thumb to move
                        if (adjustingThumbIndex == INDEX_LEFT)
                            {
                            // This calls setValueAt()
                            moveThumbToMouse(mouseevent,
                                             INDEX_LEFT,
                                             INDEX_RIGHT);
                            }
                        else if (adjustingThumbIndex == INDEX_RIGHT)
                            {
                            // This calls setValueAt()
                            moveThumbToMouse(mouseevent,
                                             INDEX_RIGHT,
                                             INDEX_LEFT);
                            }
                        else
                            {
                            LOGGER.debug(LOADER_PROPERTIES.isChartDebug(),
                                         SOURCE + "Invalid thumb index=" + adjustingThumbIndex);
                            }
                        }

                    break;
                    }

                default:
                    {
                    // Do nothing
                    }
                }

            // All valid paths will have called setValueAt()
            }


        /*******************************************************************************************
         * Release the Mouse.
         *
         * @param e
         */

        public void mouseReleased(final MouseEvent e)
            {
            final String SOURCE = "TrackListener.mouseReleased() ";

            if (!slider.isEnabled())
                {
                return;
                }

            LOGGER.debug(LOADER_PROPERTIES.isChartDebug(),
                         SOURCE + "MOUSE RELEASED: Slider & Models adjusting set FALSE");

            offset = 0;
            isDragging = false;
            slider.setValueIsAdjusting(false);
            slider.getModelAt(INDEX_LEFT).setValueIsAdjusting(false);
            slider.getModelAt(INDEX_RIGHT).setValueIsAdjusting(false);

            slider.repaint();
            }


        /*******************************************************************************************
         * Set the Value of the Slider at the specified Index.
         *
         * @param theslider
         * @param thevalue
         * @param theindex
         */

        private void setSliderValue(final MThumbSlider theslider,
                                    final int thevalue,
                                    final int theindex)
            {
            theslider.setValueAt(thevalue, theindex);
            }


        /*******************************************************************************************
         * Move the Thumb at the specified Index to the new Mouse position.
         * Uses 'offset' from the mouse position to the real thumb position.
         * Don't allow the moving thumb to bump into the fixed thumb.
         *
         * @param mouseevent
         * @param indexmoving
         * @param indexfixed
         *
         * @return Rectangle
         */

        private Rectangle moveThumbToMouse(final MouseEvent mouseevent,
                                           final int indexmoving,
                                           final int indexfixed)
            {
            final String SOURCE = "TrackListener.moveThumbToMouse() ";
            final Rectangle rectOriginalMovingThumb;
            final Rectangle rectFixedThumb;
            int thumbMovingLeft;
            final int thumbMovingMiddle;
            final int thumbMovingHalfWidth;
            final int trackLeft;
            final int trackRight;

            currentMouseX = mouseevent.getX();
            currentMouseY = mouseevent.getY();

            // Track dimensions
            trackRectInner = getTrackRect();
            trackLeft = trackRectInner.x;
            trackRight = trackRectInner.x + (trackRectInner.width - 1);

            // Moving Thumb
            rectOriginalMovingThumb = (Rectangle) rectThumbs[indexmoving].clone();
            thumbMovingHalfWidth = rectOriginalMovingThumb.width / 2;
            thumbMovingLeft = mouseevent.getX() - offset;

            rectFixedThumb = rectThumbs[indexfixed];

            // Prevent the thumb moving further than the end or an adjacent thumb
            if (indexmoving == INDEX_LEFT)
                {
                // Make sure this moving thumb stays on the left side of the fixed thumb
                // regardless of other constraints
                if (thumbMovingLeft > (rectFixedThumb.getX() - rectOriginalMovingThumb.getWidth()))
                    {
                    thumbMovingLeft = (int)(rectFixedThumb.getX() - rectOriginalMovingThumb.getWidth());
                    }

                // The left side limit is the track edge
                // The right side limit is the right thumb
                thumbMovingLeft = Math.max(thumbMovingLeft, trackLeft - thumbMovingHalfWidth);
                thumbMovingLeft = Math.min(thumbMovingLeft, (int)(rectFixedThumb.getX() - rectOriginalMovingThumb.getWidth()));
                }
            else if (indexmoving == INDEX_RIGHT)
                {
                // Make sure this moving thumb stays on the right side of the fixed thumb
                // regardless of other constraints
                if (thumbMovingLeft < (rectFixedThumb.getX() + rectFixedThumb.getWidth()))
                    {
                    thumbMovingLeft = (int)(rectFixedThumb.getX() + rectFixedThumb.getWidth());
                    }

                // The left side limit is the left thumb
                // The right side limit is the track edge
                thumbMovingLeft = Math.max(thumbMovingLeft, (int)(rectFixedThumb.getX() + rectFixedThumb.getWidth()));
                thumbMovingLeft = Math.min(thumbMovingLeft, trackRight - thumbMovingHalfWidth);
                }
            else
                {
                LOGGER.debug(LOADER_PROPERTIES.isChartDebug(),
                             SOURCE + "Invalid thumb index");
                }

            // BEWARE!! This changes rectThumbs[index]
            setThumbLocationAt(thumbMovingLeft, rectOriginalMovingThumb.y, indexmoving);
            thumbMovingMiddle = thumbMovingLeft + thumbMovingHalfWidth;

            /**
             * Returns a value give an x position.  If xPos is past the track at the left or the
             * right it will set the value to the min or max of the slider, depending if the
             * slider is inverted or not.
             */
            setSliderValue(slider,
                           ui.valueForXPosition(thumbMovingMiddle),
                           indexmoving);

            return rectOriginalMovingThumb;
            }


        /*******************************************************************************************
         * Move the Thumb at the specified Index to the new absolute X position,
         * i.e. does not use 'offset'.
         * Don't allow the moving thumb to bump into the fixed thumb.
         *
         * @param x
         * @param indexmoving
         * @param indexfixed
         *
         * @return Rectangle
         */

        private Rectangle moveThumbToAbsoluteX(final int x,
                                               final int indexmoving,
                                               final int indexfixed)
            {
            final String SOURCE = "TrackListener.moveThumbToAbsoluteX() ";
            final Rectangle rectOriginalMovingThumb;
            final Rectangle rectFixedThumb;
            int thumbMovingLeft;
            final int thumbMovingMiddle;
            final int thumbMovingHalfWidth;
            final int trackLeft;
            final int trackRight;

            // Track dimensions
            trackRectInner = getTrackRect();
            trackLeft = trackRectInner.x;
            trackRight = trackRectInner.x + (trackRectInner.width - 1);

            // Moving Thumb
            rectOriginalMovingThumb = (Rectangle) rectThumbs[indexmoving].clone();
            thumbMovingHalfWidth = rectOriginalMovingThumb.width / 2;
            thumbMovingLeft = x;

            rectFixedThumb = rectThumbs[indexfixed];

            // Prevent the thumb moving further than the end or an adjacent thumb
            if (indexmoving == INDEX_LEFT)
                {
                // Make sure this moving thumb stays on the left side of the fixed thumb
                // regardless of other constraints
                if (thumbMovingLeft > (rectFixedThumb.getX() - rectOriginalMovingThumb.getWidth()))
                    {
                    thumbMovingLeft = (int)(rectFixedThumb.getX() - rectOriginalMovingThumb.getWidth());
                    }

                // The left side limit is the track edge
                // The right side limit is the right thumb
                thumbMovingLeft = Math.max(thumbMovingLeft, trackLeft - thumbMovingHalfWidth);
                thumbMovingLeft = Math.min(thumbMovingLeft, (int)(rectFixedThumb.getX() - rectOriginalMovingThumb.getWidth()));
                }
            else if (indexmoving == INDEX_RIGHT)
                {
                // Make sure this moving thumb stays on the right side of the fixed thumb
                // regardless of other constraints
                if (thumbMovingLeft < (rectFixedThumb.getX() + rectFixedThumb.getWidth()))
                    {
                    thumbMovingLeft = (int)(rectFixedThumb.getX() + rectFixedThumb.getWidth());
                    }

                // The left side limit is the left thumb
                // The right side limit is the track edge
                thumbMovingLeft = Math.max(thumbMovingLeft, (int)(rectFixedThumb.getX() + rectFixedThumb.getWidth()));
                thumbMovingLeft = Math.min(thumbMovingLeft, trackRight - thumbMovingHalfWidth);
                }
            else
                {
                LOGGER.debug(LOADER_PROPERTIES.isChartDebug(),
                             SOURCE + "Invalid thumb index");
                }

            // BEWARE!! This changes rectThumbs[index]
            setThumbLocationAt(thumbMovingLeft, rectOriginalMovingThumb.y, indexmoving);
            thumbMovingMiddle = thumbMovingLeft + thumbMovingHalfWidth;

            setSliderValue(slider,
                           ui.valueForXPosition(thumbMovingMiddle),
                           indexmoving);

            return (rectOriginalMovingThumb);
            }


        /*******************************************************************************************
         * Get the index of the currently adjusting Thumb.
         *
         * @return int
         */

        public int getAdjustingThumbIndex()
            {
            return (this.adjustingThumbIndex);
            }


        /*******************************************************************************************
         * Reset the slider thumbs to their maximum extents.
         * Return a true flag if the reset operation was performed,
         * i.e. the sliders were not already at the extents.
         *
         * @return boolean
         */

        public boolean resetToExtents()
            {
            final String SOURCE = "MThumbSliderAdditionalUI.resetToExtents() ";
            final int trackLeft;
            final int trackRight;
            final boolean boolReset;

            // Track dimensions
            trackRectInner = getTrackRect();
            trackLeft = trackRectInner.x;
            trackRight = trackRectInner.x + (trackRectInner.width - 1);

            //           xxxxxxx                                             xxxxxxx
            //           x  ...x.............................................x...  x
            //           x  .  x                                             x  .  x
            //           x  .  x                                             x  .  x
            //           x  .  x                                             x  .  x
            //           x  ...x.............................................x...  x
            //           xxxxxxx                                             xxxxxxx
            //              ^                                                   ^
            //              track left                                          track right
            //           ^                                                   ^
            //           |                                                   |
            //           |thumb left rect X                                  |thumb right rect X


//            LOGGER.debug(LOADER_PROPERTIES.isChartDebug(),
//                         SOURCE + "[rectThumbs[INDEX_LEFT].x=" + rectThumbs[INDEX_LEFT].x
//                             + "] [trackLeft=" + trackLeft
//                             + "] [width_left=" + ((int) rectThumbs[INDEX_LEFT].getWidth())
//                             + "] [rectThumbs[INDEX_RIGHT].x=" + rectThumbs[INDEX_RIGHT].x
//                             + "] [trackRight=" + trackRight
//                             + "] [width_right=" + ((int) rectThumbs[INDEX_RIGHT].getWidth()) + "]");

            // Indicate if either slider was NOT originally at an extent,
            // i.e. true means that a reset was performed
            boolReset = ((rectThumbs[INDEX_LEFT].x != trackLeft - ((int) rectThumbs[INDEX_LEFT].getWidth() >> 1))
                         || (rectThumbs[INDEX_RIGHT].x != trackRight - ((int) rectThumbs[INDEX_RIGHT].getWidth() >> 1)));

            // Move the left to left extent
            moveThumbToAbsoluteX(trackLeft - ((int) rectThumbs[INDEX_LEFT].getWidth() >> 1),
                                 INDEX_LEFT,
                                 INDEX_RIGHT);

            // Move the right to right extent
            moveThumbToAbsoluteX(trackRight - ((int) rectThumbs[INDEX_RIGHT].getWidth() >> 1),
                                 INDEX_RIGHT,
                                 INDEX_LEFT);

            LOGGER.debug(LOADER_PROPERTIES.isChartDebug(),
                         SOURCE + "[reset_to_extents=" + boolReset + "]");

            return(boolReset);
            }


        /*******************************************************************************************
         * Debug the slider locations.
         *
         * @param debug
         * @param message
         * @param rectOriginalThumbLeft
         * @param rectOriginalThumbRight
         * @param rectMovedThumbLeft
         * @param rectMovedThumbRight
         * @param intSeparationX
         */

        private void debugLocations(final boolean debug,
                                    final String message,
                                    final Rectangle rectOriginalThumbLeft,
                                    final Rectangle rectOriginalThumbRight,
                                    final Rectangle rectMovedThumbLeft,
                                    final Rectangle rectMovedThumbRight,
                                    final int intSeparationX)
            {
            if (debug)
                {
                final StringBuffer buffer;

                buffer = new StringBuffer();

                buffer.append(message);

                buffer.append("\n[left_original_x=");
                buffer.append(rectOriginalThumbLeft.getX());

                buffer.append("] [left_original_y=");
                buffer.append(rectOriginalThumbLeft.getY());

                buffer.append("] \n[right_original_x=");
                buffer.append(rectOriginalThumbRight.getX());

                buffer.append("] [right_original_y=");
                buffer.append(rectOriginalThumbRight.getY());

                buffer.append("] \n[left_moved_x=");
                buffer.append(rectMovedThumbLeft.getX());

                buffer.append("] [left_moved_y=");
                buffer.append(rectMovedThumbLeft.getY());

                buffer.append("] \n[right_moved_x=");
                buffer.append(rectMovedThumbRight.getX());

                buffer.append("] [right_moved_y=");
                buffer.append(rectMovedThumbRight.getY());

                buffer.append("] \n[separation=");
                buffer.append(intSeparationX);
                buffer.append("] \n[offset=");
                buffer.append(offset);
                buffer.append("]");

                LOGGER.log(buffer.toString());
                }
            }
        }
    }

