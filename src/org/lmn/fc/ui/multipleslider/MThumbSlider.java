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

import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.frameworks.starbase.plugins.observatory.common.ObservatoryConstants;

import javax.accessibility.AccessibleContext;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;


// This was taken from elsewhere, and is a bit of a mess! :-)

// See also?? http://ernienotes.wordpress.com/2010/12/27/creating-a-java-swing-range-slider/

public class MThumbSlider extends JSlider
                          implements FrameworkConstants,
                                     FrameworkStrings,
                                     FrameworkMetadata,
                                     FrameworkSingletons,
                                     ObservatoryConstants
    {
    protected int                 thumbNum;
    protected BoundedRangeModel[] sliderModels;
    protected Icon[]              thumbRenderers;
    protected Color[]             fillColors;
    protected Color               trackFillColor;

    private static final String uiClassID = "MThumbSliderUI";


//    public MThumbSlider(int n)
//        {
//        createThumbs(n);
//        updateUI();
//        }


    public MThumbSlider(final int n,
                        final int minvalue,
                        final int maxvalue)
        {
        createThumbs(n, minvalue, maxvalue);
        updateUI();
        }


//    protected void createThumbs(int n)
//        {
//        thumbNum = n;
//        sliderModels = new BoundedRangeModel[n];
//        thumbRenderers = new Icon[n];
//        fillColors = new Color[n];
//        for (int i = 0;
//             i < n;
//             i++)
//            {
//            sliderModels[i] = new DefaultBoundedRangeModel(50, 0, 0, 100);
//            thumbRenderers[i] = null;
//            fillColors[i] = null;
//            }
//        }


    protected void createThumbs(final int n,
                                final int minvalue,
                                final int maxvalue)
        {
        thumbNum = n;
        sliderModels = new BoundedRangeModel[n];
        thumbRenderers = new Icon[n];
        fillColors = new Color[n];
        for (int i = 0;
             i < n;
             i++)
            {
            sliderModels[i] = new DefaultBoundedRangeModel(50, 0, minvalue, maxvalue);
            thumbRenderers[i] = null;
            fillColors[i] = null;
            }
        }


    public void updateUI()
        {
        updateLabelUIs();
        setUI(new BasicMThumbSliderUI(this));
        }


    public String getUIClassID()
        {
        return uiClassID;
        }


    public int getThumbNum()
        {
        return thumbNum;
        }


    public int getValueAt(int index)
        {
        return getModelAt(index).getValue();
        }


    /***********************************************************************************************
     * Set the value of the specified Thumb.
     * fireStateChanged() depending on isAdjusting.
     *
     * @param value
     * @param index
     */

    public void setValueAt(final int value,
                           final int index)
        {
        final String SOURCE = "MThumbSlider.setValueAt() ";

        //getModelAt(index).setValue(value);
        // should I fire?  <--- What on Earth was this all about? [LMN]

        final int oldValue;

        oldValue = getModelAt(index).getValue();

        LOGGER.debug(LOADER_PROPERTIES.isChartDebug(),
                     SOURCE + "[new.value=" + value
                           + "] [old.value=" + oldValue
                           + "] [index=" + index
                           + "] [slider.isadjusting=" + getValueIsAdjusting()
                           + "] [model.isadjusting=" + getModelAt(index).getValueIsAdjusting() + "]");

        if (oldValue == value)
            {
            return;
            }

        // This will fireStateChanged() depending on isAdjusting
        getModelAt(index).setValue(value);

        // Fire ChangeEvents ONLY if the slider is still moving, to enable crosshairs to follow the value
        if ((getModelAt(index).getValueIsAdjusting())
            && (getModelAt(index) instanceof DefaultBoundedRangeModel))
            {
            final ChangeListener[] arrayChangeListeners;

            //arrayChangeListeners = ((DefaultBoundedRangeModel)getModelAt(index)).getChangeListeners();
            arrayChangeListeners = getChangeListeners();

            LOGGER.debug(LOADER_PROPERTIES.isChartDebug(),
                         SOURCE + "Fire MThumbSlider ChangeEvents");

            for (int intListenerIndex = 0;
                 intListenerIndex < arrayChangeListeners.length;
                 intListenerIndex++ )
                {
                LOGGER.debug(LOADER_PROPERTIES.isChartDebug(),
                             SOURCE + "Fire ChangeEvent to Listener index=" + intListenerIndex);
                arrayChangeListeners[intListenerIndex].stateChanged(new ChangeEvent(this));
                }
            }
        else
            {
            LOGGER.debug(LOADER_PROPERTIES.isChartDebug(),
                         SOURCE + "Do nothing with extra ChangeEvent");
            }


        if (accessibleContext != null)
            {
            accessibleContext.firePropertyChange(AccessibleContext.ACCESSIBLE_VALUE_PROPERTY,
                                                 oldValue,
                                                 getModelAt(index).getValue());
            }
        }

    /**
     * Sets the slider's current value to {@code n}.  This method
     * forwards the new value to the model.
     * <p>
     * The data model (an instance of {@code BoundedRangeModel})
     * handles any mathematical
     * issues arising from assigning faulty values.  See the
     * {@code BoundedRangeModel} documentation for details.
     * <p>
     * If the new value is different from the previous value,
     * all change listeners are notified.
     *
     * @see     #getValue
     * @see     #addChangeListener
     * @see     BoundedRangeModel#setValue
     * @beaninfo
     *   preferred: true
     * description: The sliders current value.
     */
//    public void setValue(int n) {
//        BoundedRangeModel m = getModel();
//        int oldValue = m.getValue();
//        if (oldValue == n) {
//            return;
//        }
//        m.setValue(n);
//
//        if (accessibleContext != null) {
//            accessibleContext.firePropertyChange(
//                                                AccessibleContext.ACCESSIBLE_VALUE_PROPERTY,
//                                                new Integer(oldValue),
//                                                new Integer(m.getValue()));
//        }
//    }


    public int getMinimum()
        {
        return getModelAt(0).getMinimum();
        }


    public int getMaximum()
        {
        return getModelAt(0).getMaximum();
        }


    public BoundedRangeModel getModelAt(int index)
        {
        return sliderModels[index];
        }


    public Icon getThumbRendererAt(int index)
        {
        return thumbRenderers[index];
        }


    public void setThumbRendererAt(Icon icon,
                                   int index)
        {
        thumbRenderers[index] = icon;
        }


    public Color getFillColorAt(int index)
        {
        return fillColors[index];
        }


    public void setFillColorAt(Color color,
                               int index)
        {
        fillColors[index] = color;
        }


    public Color getTrackFillColor()
        {
        return trackFillColor;
        }


    public void setTrackFillColor(Color color)
        {
        trackFillColor = color;
        }
    }

