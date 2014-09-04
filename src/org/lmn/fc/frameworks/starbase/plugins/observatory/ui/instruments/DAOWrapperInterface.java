// Copyright 2000, 2001, 2002, 2003, 04, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013, 2013
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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments;

import org.jfree.data.xy.XYDataset;
import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.ChartUIComponentPlugin;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageInterface;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;
import org.lmn.fc.model.xmlbeans.poi.LineOfInterest;
import org.lmn.fc.model.xmlbeans.poi.PointOfInterest;

import java.awt.*;
import java.util.List;
import java.util.Vector;


public interface DAOWrapperInterface extends FrameworkSingletons
    {
    int INITIAL_CAPACITY = 50;


    /**********************************************************************************************/
    /* Command Lifecycle                                                                          */
    /***********************************************************************************************
     * Get the CommandMessage responsible for producing these data.
     *
     * @return PortMessageEvent
     */

    CommandMessageInterface getCommandMessage();


    /***********************************************************************************************
     * Get the ResponseMessage resulting from the command execution.
     *
     * @return ResponseMessageInterface
     */

    ResponseMessageInterface getResponseMessage();


    /***********************************************************************************************
     * Get the ResponseValue.
     *
     * @return String
     */

    String getResponseValue();


    /***********************************************************************************************
     * Get the Wrapped DAO.
     *
     * @return ObservatoryInstrumentDAOInterface
     */

    ObservatoryInstrumentDAOInterface getWrappedDAO();


    /**********************************************************************************************/
    /* Bodge Central                                                                              */
    /***********************************************************************************************
     * Make the contents of the Wrapper agree with the contents of the DAO.
     * This really shows we need to rethink the Wrapper concept!
     */

    void harmoniseWrapperWithDAO(ObservatoryInstrumentDAOInterface dao);


    /**********************************************************************************************/
    /* Metadata                                                                                   */
    /***********************************************************************************************
     * Get the Metadata Metadata List.
     *
     * @return List<Metadata>
     */

    List<Metadata> getMetadataMetadata();


    /***********************************************************************************************
     * Get the CurrentObservatoryMetadata.
     *
     * @return List<Metadata>
     */

    List<Metadata> getCurrentObservatoryMetadata();


    /***********************************************************************************************
     * Get the CurrentObserverMetadata.
     *
     * @return List<Metadata>
     */

    List<Metadata> getCurrentObserverMetadata();


    /***********************************************************************************************
     * Get the ObservationMetadata List.
     *
     * @return List<Metadata>
     */

    List<Metadata> getObservationMetadata();


    /***********************************************************************************************
     * Get the InstrumentMetadata List.
     *
     * @return List<Metadata>
     */

    List<Metadata> getInstrumentMetadata();


    /***********************************************************************************************
     * Get the ControllerMetadata List.
     *
     * @return List<Metadata>
     */

    List<Metadata> getControllerMetadata();


    /***********************************************************************************************
     * Get the PluginMetadata List.
     *
     * @return List<Metadata>
     */

    List<Metadata> getPluginMetadata();


    /***********************************************************************************************
     * Get the RawData Metadata List.
     *
     * @return List<Metadata>
     */

    List<Metadata> getRawDataMetadata();


    /***********************************************************************************************
     * Get the XYDataset Metadata List.
     *
     * @return List<Metadata>
     */

    List<Metadata> getXYDatasetMetadata();


    /***********************************************************************************************
     * Clear all Metadata containers.
     */

    void clearMetadata();


    /**********************************************************************************************/
    /* PointOfInterest                                                                            */
    /***********************************************************************************************
     * Get the list of PointsOfInterest.
     *
     * @return List<PointOfInterest>
     */

    List<PointOfInterest> getPointOfInterestList();


    /***********************************************************************************************
     * Set the Points of Interest for the Wrapper.
     *
     * @param pois
     */

    void setPointOfInterestList(List<PointOfInterest> pois);


    /**********************************************************************************************/
    /* LineOfInterest                                                                             */
    /***********************************************************************************************
     * Get the list of LinesOfInterest.
     *
     * @return List<LineOfInterest>
     */

    List<LineOfInterest> getLineOfInterestList();


    /***********************************************************************************************
     * Set the Lines of Interest for the Wrapper.
     *
     * @param lois
     */

    void setLineOfInterestList(List<LineOfInterest> lois);


    /**********************************************************************************************/
    /* Data Containers                                                                            */
    /***********************************************************************************************
     * Get the Vector of raw data.
     *
     * @return Vector<Object>
     */

    Vector<Object> getRawData();


    /***********************************************************************************************
     * Set the Vector of raw data.
     *
     * @param data
     */

    void setRawData(Vector<Object> data);


    /***********************************************************************************************
     * Get the XYDataset.
     *
     * @return XYDataset
     */

    XYDataset getXYDataset();


    /***********************************************************************************************
     * Set the XYDataset.
     *
     * @param dataset
     */

    void setXYDataset(XYDataset dataset);


    /***********************************************************************************************
     * Get the ImageData.
     *
     * @return Image
     */

    Image getImageData();


    /***********************************************************************************************
     * Set the ImageData.
     *
     * @param image
     */

    void setImageData(Image image);


    /***********************************************************************************************
     * Get the UserObject.
     *
     * @return Object
     */

    Object getUserObject();


    /***********************************************************************************************
     * Set the UserObject.
     *
     * @param userobject
     */

    void setUserObject(Object userobject);


    /***********************************************************************************************
     * Clear all Data containers.
     */

    void clearData();


    /***********************************************************************************************
     * Get the RawData Channel count.
     *
     * @return int
     */

    int getRawDataChannelCount();


    /***********************************************************************************************
     * Indicate if the first data channel represents Temperature (Usually a Staribus dataset).
     *
     * @return boolean
     */

    boolean hasTemperatureChannel();


    /***********************************************************************************************
     * Indicate if this DAO has unsaved data.
     * Data may be RawData or Image.
     *
     * @return boolean
     */

    boolean hasUnsavedData();


    /***********************************************************************************************
     * Get the currently selected Chart for use with this DAO.
     *
     * @return ChartUIComponentPlugin
     */

    ChartUIComponentPlugin getChartUI();


    /**********************************************************************************************/
    /* Logging                                                                                    */
    /***********************************************************************************************
     * Get the EventLog Metadata List.
     *
     * @return List<Metadata>
     */

    List<Metadata> getEventLogMetadata();


    /***********************************************************************************************
     * Get the EventLogFragment.
     *
     * @return Vector<Vector>
     */

    Vector<Vector> getEventLogFragment();


    /***********************************************************************************************
     * Get the InstrumentLog Metadata List.
     *
     * @return List<Metadata>
     */

    List<Metadata> getInstrumentLogMetadata();


    /***********************************************************************************************
     * Get the InstrumentLogFragment.
     *
     * @return Vector<Vector>
     */

    Vector<Vector> getInstrumentLogFragment();
    }
