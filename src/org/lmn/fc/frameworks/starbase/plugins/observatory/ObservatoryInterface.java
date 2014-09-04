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

package org.lmn.fc.frameworks.starbase.plugins.observatory;

import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.ephemerides.EphemerisDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.events.ObservatoryMetadataChangedListener;
import org.lmn.fc.frameworks.starbase.plugins.observatory.events.ObserverMetadataChangedListener;
import org.lmn.fc.model.xmlbeans.ephemerides.EphemeridesDocument;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;
import org.lmn.fc.model.xmlbeans.poi.LineOfInterest;
import org.lmn.fc.model.xmlbeans.poi.PointOfInterest;

import java.util.Hashtable;
import java.util.List;
import java.util.Vector;


/***************************************************************************************************
 * ObservatoryInterface.
 */

public interface ObservatoryInterface
    {
    // String Resources

    // Observatory Atom icons
    // Use: RegistryModelUtilities.getAtomIcon(getObservatoryUI().getHostAtom(), filename)
    // Where the filename is relative to observatory/images

    String FILENAME_ICON_ASCII                  = "ASCII.png";
    String FILENAME_ICON_AUDIO_EXPLORER         = "audio-explorer.png";
    String FILENAME_ICON_AUDIO_EXPLORER_RESCAN  = "audio-explorer-rescan.png";
    String FILENAME_ICON_AVERAGING_FFT          = "toolbar-averaging-fft.png";
    String FILENAME_ICON_CHART_VIEWER           = "chart-viewer.png";
    String FILENAME_ICON_EPHEMERIS              = "ephemeris.png";
    String FILENAME_ICON_HELP                   = "header-help.png";
    String FILENAME_ICON_IQ_ADJUSTER            = "iq-adjuster.png";
    String FILENAME_ICON_MANUAL                 = "header-manual.png";
    String FILENAME_ICON_MAP_VIEWER             = "toolbar-map-viewer.png";
    String FILENAME_ICON_NETWORK_SCANNER        = "toolbar-network-scanner.png";
    String FILENAME_ICON_SDA                    = "header-sda.png";
    String FILENAME_ICON_SERIAL_CONFIG          = "toolbar-serial-config.png";
    String FILENAME_ICON_STAR_MAP               = "star-map.png";
    String FILENAME_ICON_TIME_ZONES             = "time-zones.png";
    String FILENAME_ICON_CONFIGURATION          = "toolbar-configuration.png";
    String FILENAME_ICON_LEXICON                = "toolbar-lexicon.png";
    String FILENAME_ICON_PUBLISHER              = "header-publisher.png";
    String FILENAME_ICON_XML_VIEWER             = "xml-viewer.png";

    // To accommodate e.g. SpectrumLab
    int MAX_CHANNELS = 32;


    /**********************************************************************************************/
    /* Ephemerides                                                                                */
    /***********************************************************************************************
     * Get the Observatory Ephemrides XML document.
     *
     * @return EphemeridesDocument
     */

    EphemeridesDocument getEphemeridesDoc();


    /***********************************************************************************************
     * Get the table of Ephemeris DAOs available to the Observatory (may be empty),
     * keyed by EphemerisName.
     *
     * @return Hashtable{String, EphemerisDAOInterface}
     */

    Hashtable<String, EphemerisDAOInterface> getEphemerisDaoTable();


    /**********************************************************************************************/
    /* Metadata                                                                                   */
    /***********************************************************************************************
     * Get the ObservatoryMetadata.
     *
     * @return List<Metadata>
     */

    List<Metadata> getObservatoryMetadata();


    /***********************************************************************************************
     * Get the ObserverMetadata.
     *
     * @return List<Metadata>
     */

    List<Metadata> getObserverMetadata();


    /***********************************************************************************************
     * Clear all Metadata attached to the Observatory.
     */

    void clearMetadata();


    /***********************************************************************************************
     * Indicate if the ObservatoryMetadata are currently loaded.
     *
     * @return boolean
     */

    boolean areObservatoryMetadataLoaded();


    /***********************************************************************************************
     * Indicate if the ObservatoryMetadata are currently loaded.
     *
     * @param loaded
     */

    void setObservatoryMetadataLoaded(boolean loaded);


    /***********************************************************************************************
     * Indicate if the ObserverMetadata are currently loaded.
     *
     * @return boolean
     */

    boolean areObserverMetadataLoaded();


    /***********************************************************************************************
     * Indicate if the ObserverMetadata are currently loaded.
     *
     * @param loaded
     */

    void setObserverMetadataLoaded(boolean loaded);


    /**********************************************************************************************/
    /* PointOfInterest                                                                            */
    /***********************************************************************************************
     * Add a PointOfInterest to the Observatory.
     *
     * @param poi
     */

    void addPointOfInterest(PointOfInterest poi);


    /***********************************************************************************************
     * Remove all PointsOfInterest from the Observatory.
     */

    void clearPointsOfInterest();


    /***********************************************************************************************
     * Get the list of PointsOfInterest.
     *
     * @return List<PointOfInterest>
     */

    List<PointOfInterest> getPointOfInterestList();


    /***********************************************************************************************
     * Set the Points of Interest for the Observatory.
     *
     * @param pois
     */

    void setPointOfInterestList(List<PointOfInterest> pois);


    /**********************************************************************************************/
    /* LineOfInterest                                                                             */
    /***********************************************************************************************
     * Add a LineOfInterest to the Observatory.
     *
     * @param loi
     */

    void addLineOfInterest(LineOfInterest loi);


    /***********************************************************************************************
     * Remove all LinesOfInterest from the Observatory.
     */

    void clearLinesOfInterest();


    /***********************************************************************************************
     * Get the list of LinesOfInterest.
     *
     * @return List<LineOfInterest>
     */

    List<LineOfInterest> getLineOfInterestList();


    /***********************************************************************************************
     * Set the Lines of Interest for the Observatory.
     *
     * @param lois
     */

    void setLineOfInterestList(List<LineOfInterest> lois);


    /**********************************************************************************************/
    /* Macros                                                                                     */
    /***********************************************************************************************
     * Indicate TRUE if the Observatory is in Record Macro capture mode,
     * or FALSE if Commands and Macros may be executed.
     *
     * @return boolean
     */

    boolean isRecordMacroMode();


    /***********************************************************************************************
     * Set TRUE if the Observatory is in Record Macro capture mode,
     * or FALSE if Commands and Macros may be executed.
     *
     * @param mode
     */

    void setRecordMacroMode(boolean mode);


    /**********************************************************************************************/
    /* Events                                                                                     */
    /***********************************************************************************************
     * Notify all listeners of ObservatoryMetadataChangedEvents.
     *
     * @param eventsource
     * @param metadatakey
     * @param state
     */

    void notifyObservatoryMetadataChangedEvent(Object eventsource,
                                               String metadatakey,
                                               MetadataItemState state);


    /***********************************************************************************************
     * Get the ObservatoryMetadataChanged Listeners (mostly for testing).
     *
     * @return Vector<ObservatoryMetadataChangedListener>
     */

    Vector<ObservatoryMetadataChangedListener> getObservatoryMetadataChangedListeners();


    /***********************************************************************************************
     * Add a listener for this event.
     *
     * @param listener
     */

    void addObservatoryMetadataChangedListener(ObservatoryMetadataChangedListener listener);


    /***********************************************************************************************
     * Remove a listener for this event.
     *
     * @param listener
     */

    void removeObservatoryMetadataChangedListener(ObservatoryMetadataChangedListener listener);


    /***********************************************************************************************
     * Notify all listeners of ObserverMetadataChangedEvents.
     *
     * @param eventsource
     * @param metadatakey
     * @param state
     */

    void notifyObserverMetadataChangedEvent(Object eventsource,
                                            String metadatakey,
                                            MetadataItemState state);


    /***********************************************************************************************
     * Get the ObserverMetadataChanged Listeners (mostly for testing).
     *
     * @return Vector<ObserverMetadataChangedListener>
     */

    Vector<ObserverMetadataChangedListener> getObserverMetadataChangedListeners();


    /***********************************************************************************************
     * Add a listener for this event.
     *
     * @param listener
     */

    void addObserverMetadataChangedListener(ObserverMetadataChangedListener listener);


    /***********************************************************************************************
     * Remove a listener for this event.
     *
     * @param listener
     */

    void removeObserverMetadataChangedListener(ObserverMetadataChangedListener listener);
    }
