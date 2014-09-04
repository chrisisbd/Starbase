// Copyright 2000, 2001, 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009,
//           2010, 2011, 2012, 2013, 2014
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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.fft;

import org.lmn.fc.frameworks.starbase.plugins.observatory.MetadataItemState;
import org.lmn.fc.frameworks.starbase.plugins.observatory.events.MetadataChangedListener;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryUIInterface;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;
import org.lmn.fc.ui.UIComponentPlugin;

import javax.swing.*;
import java.util.List;
import java.util.Vector;


/***************************************************************************************************
 * AveragingFFTSidebarInterface.
 */

public interface AveragingFFTSidebarInterface extends UIComponentPlugin
    {
    int SIDEBAR_HEIGHT_MARGIN = 8;
    int SIDEBAR_HEIGHT_SEPARATOR = 15;


    /**********************************************************************************************/
    /* User Interface                                                                             */
    /***********************************************************************************************
     * Get the IQPlot component.
     *
     * @return JComponent
     */

    JComponent getIQPlot();


    /***********************************************************************************************
     * Set the IQPlot component.
     *
     * @param plot
     */

    void setIQPlot(JComponent plot);


    /***********************************************************************************************
     * Get the Metadata associated with this UIComponent.
     *
     * @return List<Metadata>
     */

    List<Metadata> getMetadataList();


    /**********************************************************************************************/
    /* Injections                                                                                 */
    /**********************************************************************************************
     * Get the host ObservatoryUI.
     *
     * @return ObservatoryUIInterface
     */

    ObservatoryUIInterface getObservatoryUI();


    /******************************************************************************************/
    /* Events                                                                                 */
    /***********************************************************************************************
     * Notify all listeners of MetadataChangedEvents.
     *
     * @param eventsource
     * @param metadatakey
     * @param metadatavalue
     * @param state
     */

    void notifyMetadataChangedEvent(Object eventsource,
                                    String metadatakey,
                                    String metadatavalue,
                                    MetadataItemState state);


    /***********************************************************************************************
     * Get the MetadataChanged Listeners (mostly for testing).
     *
     * @return Vector<MetadataChangedListener>
     */

    Vector<MetadataChangedListener> getMetadataChangedListeners();


    /***********************************************************************************************
     * Add a listener for this event, uniquely.
     *
     * @param listener
     */

    void addMetadataChangedListener(MetadataChangedListener listener);


    /***********************************************************************************************
     * Remove a listener for this event.
     *
     * @param listener
     */

    void removeMetadataChangedListener(MetadataChangedListener listener);
    }
