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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common;

import org.jfree.data.xy.XYDataset;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.DAOWrapperInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.ChartUIComponentPlugin;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageInterface;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;
import org.lmn.fc.model.xmlbeans.poi.LineOfInterest;
import org.lmn.fc.model.xmlbeans.poi.PointOfInterest;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;


/***************************************************************************************************
 * DAOWrapper is a convenience class to simultaneously return different types of data
 * from a DAO, for e.g. SwingWorkers driving Tabbed displays.
 * The String response value is always a copy of that in CommandType.Response.Value.
 */

public final class DAOWrapper implements DAOWrapperInterface
    {
    // Injections
    private final CommandMessageInterface commandMessage;
    private final ResponseMessageInterface responseMessage;

    // Displayable ResponseValue
    private final String strResponseValue;

    private final ObservatoryInstrumentDAOInterface daoInterface;


    //----------------------------------------------------------------------------------------------
    // The following references point to items in the DAO

    // Metadata Payloads
    private List<Metadata> listMetadataMetadata;
    private List<Metadata> listCurrentObservatoryMetadata;
    private List<Metadata> listCurrentObserverMetadata;
    private List<Metadata> listObservationMetadata;
    private List<Metadata> listInstrumentMetadata;
    private List<Metadata> listControllerMetadata;
    private List<Metadata> listPluginMetadata;
    private List<Metadata> listRawDataMetadata;
    private List<Metadata> listXYDatasetMetadata;

    // POIs and LOIs
    private List<PointOfInterest> listPOIs;
    private List<LineOfInterest> listLOIs;

    // Data Payloads
    private Vector<Object> vecRawData;
    private XYDataset xyDataset;
    private Image imageData;
    private Object userObject;

    // Convenience Reference to Chart
    private ChartUIComponentPlugin associatedChartUI;

    // Logging
    private List<Metadata> listEventLogMetadata;
    private Vector<Vector> vecEventLogFragment;

    private List<Metadata> listInstrumentLogMetadata;
    private Vector<Vector> vecInstrumentLogFragment;

    // End of DAO references
    //----------------------------------------------------------------------------------------------


    /***********************************************************************************************
     * Debug the specified DAO Wrapper.
     * As I have said before, I am not entirely happy about the Wrapper concept,
     * but it would be a lot of work to change it now!
     *
     * @param wrapper
     * @param dao
     * @param message
     * @param debug
     */

    public static void debugDAOWrapper(final DAOWrapperInterface wrapper,
                                       final ObservatoryInstrumentDAOInterface dao,
                                       final String message,
                                       final boolean debug)
        {
        LOGGER.debug(debug, "-------------------------------------------------------------------------------------------");
        LOGGER.debug(debug, "DAOWrapper vs. DAO " + message);

        if (wrapper != null)
            {
            LOGGER.debug(debug, "Wrapped DAO [wrappeddao.notnull=" + (wrapper.getWrappedDAO() != null) + "]");

            if (wrapper.getWrappedDAO() != null)
                {
                LOGGER.debug(debug, "Wrapped DAO [wrappeddao.classname=" + wrapper.getWrappedDAO().getClass().getName() + "]");
                }
            }

        LOGGER.debug(debug, "DAO [dao.notnull=" + (dao != null) + "]");

        if (dao != null)
            {
            LOGGER.debug(debug, "DAO [dao.classname=" + dao.getClass().getName() + "]");
            }

        if ((wrapper != null)
            && wrapper.getMetadataMetadata() != null)
            {
            LOGGER.debug(debug, "wrapper.MetadataMetadata [size=" + wrapper.getMetadataMetadata().size() + "]");
            }
        else
            {
            LOGGER.debug(debug, "wrapper.MetadataMetadata is NULL");
            }

        if ((dao != null)
            && (dao.getMetadataMetadata() != null))
            {
            LOGGER.debug(debug, "dao.MetadataMetadata [size=" + dao.getMetadataMetadata().size() + "]");
            }
        else
            {
            LOGGER.debug(debug, "dao.MetadataMetadata is NULL");
            }

        if ((wrapper != null)
            && wrapper.getCurrentObservatoryMetadata() != null)
            {
            LOGGER.debug(debug, "wrapper.CurrentObservatoryMetadata [size=" + wrapper.getCurrentObservatoryMetadata().size() + "]");
            }
        else
            {
            LOGGER.debug(debug, "wrapper.CurrentObservatoryMetadata is NULL");
            }

        if ((dao != null)
            && (dao.getCurrentObservatoryMetadata() != null))
            {
            LOGGER.debug(debug, "dao.CurrentObservatoryMetadata [size=" + dao.getCurrentObservatoryMetadata().size() + "]");
            }
        else
            {
            LOGGER.debug(debug, "dao.CurrentObservatoryMetadata is NULL");
            }

        if ((wrapper != null)
            && wrapper.getCurrentObserverMetadata() != null)
            {
            LOGGER.debug(debug, "wrapper.CurrentObserverMetadata [size=" + wrapper.getCurrentObserverMetadata().size() + "]");
            }
        else
            {
            LOGGER.debug(debug, "wrapper.CurrentObserverMetadata is NULL");
            }

        if ((dao != null)
            && (dao.getCurrentObserverMetadata() != null))
            {
            LOGGER.debug(debug, "dao.CurrentObserverMetadata [size=" + dao.getCurrentObserverMetadata().size() + "]");
            }
        else
            {
            LOGGER.debug(debug, "dao.CurrentObserverMetadata is NULL");
            }

        if ((wrapper != null)
            && wrapper.getObservationMetadata() != null)
            {
            LOGGER.debug(debug, "wrapper.ObservationMetadata [size=" + wrapper.getObservationMetadata().size() + "]");
            }
        else
            {
            LOGGER.debug(debug, "wrapper.ObservationMetadata is NULL");
            }

        if ((dao != null)
            && (dao.getObservationMetadata() != null))
            {
            LOGGER.debug(debug, "dao.ObservationMetadata [size=" + dao.getObservationMetadata().size() + "]");
            }
        else
            {
            LOGGER.debug(debug, "dao.ObservationMetadata is NULL");
            }

        if ((wrapper != null)
            && wrapper.getInstrumentMetadata() != null)
            {
            LOGGER.debug(debug, "wrapper.InstrumentMetadata [size=" + wrapper.getInstrumentMetadata().size() + "]");
            }
        else
            {
            LOGGER.debug(debug, "wrapper.InstrumentMetadata is NULL");
            }

        if ((dao != null)
            && (dao.getInstrumentMetadata() != null))
            {
            LOGGER.debug(debug, "dao.InstrumentMetadata [size=" + dao.getInstrumentMetadata().size() + "]");
            }
        else
            {
            LOGGER.debug(debug, "dao.InstrumentMetadata is NULL");
            }

        if ((wrapper != null)
            && wrapper.getControllerMetadata() != null)
            {
            LOGGER.debug(debug, "wrapper.ControllerMetadata [size=" + wrapper.getControllerMetadata().size() + "]");
            }
        else
            {
            LOGGER.debug(debug, "wrapper.ControllerMetadata is NULL");
            }

        if ((dao != null)
            && (dao.getControllerMetadata() != null))
            {
            LOGGER.debug(debug, "dao.ControllerMetadata [size=" + dao.getControllerMetadata().size() + "]");
            }
        else
            {
            LOGGER.debug(debug, "dao.ControllerMetadata is NULL");
            }

        if ((wrapper != null)
            && wrapper.getPluginMetadata() != null)
            {
            LOGGER.debug(debug, "wrapper.PluginMetadata [size=" + wrapper.getPluginMetadata().size() + "]");
            }
        else
            {
            LOGGER.debug(debug, "wrapper.PluginMetadata is NULL");
            }

        if ((dao != null)
            && (dao.getPluginMetadata() != null))
            {
            LOGGER.debug(debug, "dao.PluginMetadata [size=" + dao.getPluginMetadata().size() + "]");
            }
        else
            {
            LOGGER.debug(debug, "dao.PluginMetadata is NULL");
            }

        if ((wrapper != null)
            && wrapper.getRawDataMetadata() != null)
            {
            LOGGER.debug(debug, "wrapper.RawDataMetadata [size=" + wrapper.getRawDataMetadata().size() + "]");
            }
        else
            {
            LOGGER.debug(debug, "wrapper.RawDataMetadata is NULL");
            }

        if ((dao != null)
            && (dao.getRawDataMetadata() != null))
            {
            LOGGER.debug(debug, "dao.RawDataMetadata [size=" + dao.getRawDataMetadata().size() + "]");
            }
        else
            {
            LOGGER.debug(debug, "dao.RawDataMetadata is NULL");
            }

        if ((wrapper != null)
            && wrapper.getXYDatasetMetadata() != null)
            {
            LOGGER.debug(debug, "wrapper.XYDatasetMetadata [size=" + wrapper.getXYDatasetMetadata().size() + "]");
            }
        else
            {
            LOGGER.debug(debug, "wrapper.XYDatasetMetadata is NULL");
            }

        if ((dao != null)
            && (dao.getXYDatasetMetadata() != null))
            {
            LOGGER.debug(debug, "dao.XYDatasetMetadata [size=" + dao.getXYDatasetMetadata().size() + "]");
            }
        else
            {
            LOGGER.debug(debug, "dao.XYDatasetMetadata is NULL");
            }

        if ((wrapper != null)
            && wrapper.getPointOfInterestList() != null)
            {
            LOGGER.debug(debug, "wrapper.PointOfInterestList [size=" + wrapper.getPointOfInterestList().size() + "]");
            }
        else
            {
            LOGGER.debug(debug, "wrapper.PointOfInterestList is NULL");
            }

        if ((dao != null)
            && (dao.getPointOfInterestList() != null))
            {
            LOGGER.debug(debug, "dao.PointOfInterestList [size=" + dao.getPointOfInterestList().size() + "]");
            }
        else
            {
            LOGGER.debug(debug, "dao.PointOfInterestList is NULL");
            }

        if ((wrapper != null)
            && wrapper.getLineOfInterestList() != null)
            {
            LOGGER.debug(debug, "wrapper.LineOfInterestList [size=" + wrapper.getLineOfInterestList().size() + "]");
            }
        else
            {
            LOGGER.debug(debug, "wrapper.LineOfInterestList is NULL");
            }

        if ((dao != null)
            && (dao.getLineOfInterestList() != null))
            {
            LOGGER.debug(debug, "dao.LineOfInterestList [size=" + dao.getLineOfInterestList().size() + "]");
            }
        else
            {
            LOGGER.debug(debug, "dao.LineOfInterestList is NULL");
            }

        if ((wrapper != null)
            && wrapper.getRawData() != null)
            {
            LOGGER.debug(debug, "wrapper.RawData [size=" + wrapper.getRawData().size() + "]");
            }
        else
            {
            LOGGER.debug(debug, "wrapper.RawData is NULL");
            }

        if ((dao != null)
            && (dao.getRawData() != null))
            {
            LOGGER.debug(debug, "dao.RawData [size=" + dao.getRawData().size() + "]");
            }
        else
            {
            LOGGER.debug(debug, "dao.RawData is NULL");
            }

        if (wrapper != null)
            {
            LOGGER.debug(debug, "RawData [wrapper.channel.count=" + wrapper.getRawDataChannelCount() + "]");
            }

        if (dao != null)
            {
            LOGGER.debug(debug, "RawData [dao.channel.count=" + dao.getRawDataChannelCount() + "]");
            }

        if (wrapper != null)
            {
            LOGGER.debug(debug, "RawData [wrapper.temperature=" + wrapper.hasTemperatureChannel() + "]");
            }

        if (dao != null)
            {
            LOGGER.debug(debug, "RawData [dao.temperature=" + dao.hasTemperatureChannel() + "]");
            }

        if (wrapper != null)
            {
            LOGGER.debug(debug, "RawData [wrapper.unsaved.data=" + wrapper.hasUnsavedData() + "]");
            }

        if (dao != null)
            {
            LOGGER.debug(debug, "RawData [dao.unsaved.data=" + dao.hasUnsavedData() + "]");
            LOGGER.debug(debug, "RawData [dao.changed.datsettype=" + dao.isDatasetTypeChanged() + "]");
            LOGGER.debug(debug, "RawData [dao.changed.channelcount=" + dao.isChannelCountChanged() + "]");
            LOGGER.debug(debug, "RawData [dao.changed.rawdata=" + dao.isRawDataChanged() + "]");
            LOGGER.debug(debug, "RawData [dao.changed.processeddata=" + dao.isProcessedDataChanged() + "]");
            }

        if ((wrapper != null)
            && wrapper.getXYDataset() != null)
            {
            LOGGER.debug(debug, "wrapper.XYDataset [series.count=" + wrapper.getXYDataset().getSeriesCount() + "]");
            }
        else
            {
            LOGGER.debug(debug, "wrapper.XYDataset is NULL");
            }

        if ((dao != null)
            && (dao.getXYDataset() != null))
            {
            LOGGER.debug(debug, "dao.XYDataset [series.count=" + dao.getXYDataset().getSeriesCount() + "]");
            }
        else
            {
            LOGGER.debug(debug, "dao.XYDataset is NULL");
            }

        if ((wrapper != null)
            && wrapper.getImageData() != null)
            {
            LOGGER.debug(debug, "wrapper.ImageData present");
            }
        else
            {
            LOGGER.debug(debug, "wrapper.ImageData is NULL");
            }

        if ((dao != null)
            && (dao.getImageData() != null))
            {
            LOGGER.debug(debug, "dao.ImageData present");
            }
        else
            {
            LOGGER.debug(debug, "dao.ImageData is NULL");
            }

        if ((wrapper != null)
            && wrapper.getUserObject() != null)
            {
            LOGGER.debug(debug, "wrapper.UserObject present");
            }
        else
            {
            LOGGER.debug(debug, "wrapper.UserObject is NULL");
            }

        if ((dao != null)
            && (dao.getUserObject() != null))
            {
            LOGGER.debug(debug, "dao.UserObject present");
            }
        else
            {
            LOGGER.debug(debug, "dao.UserObject is NULL");
            }

        if ((wrapper != null)
            && (wrapper.getChartUI() != null)
            && (wrapper.getChartUI().getChartPanel() != null)
            && (wrapper.getChartUI().getChartPanel().getChart() != null))
            {
            LOGGER.debug(debug, "wrapper.Chart [title=" + wrapper.getChartUI().getChartPanel().getChart().getTitle() + "]");
            }
        else
            {
            LOGGER.debug(debug, "wrapper.Chart is NULL");
            }

        if ((dao != null)
            && (dao.getChartUI() != null)
            && (dao.getChartUI().getChartPanel() != null)
            && (dao.getChartUI().getChartPanel().getChart() != null))
            {
            LOGGER.debug(debug, "dao.Chart [title=" + dao.getChartUI().getChartPanel().getChart().getTitle() + "]");
            }
        else
            {
            LOGGER.debug(debug, "dao.Chart is NULL");
            }

        if ((wrapper != null)
            && wrapper.getEventLogMetadata() != null)
            {
            LOGGER.debug(debug, "wrapper.EventLogMetadata [size=" + wrapper.getEventLogMetadata().size() + "]");
            }
        else
            {
            LOGGER.debug(debug, "wrapper.EventLogMetadata is NULL");
            }

        if ((dao != null)
            && (dao.getEventLogMetadata() != null))
            {
            LOGGER.debug(debug, "dao.EventLogMetadata [size=" + dao.getEventLogMetadata().size() + "]");
            }
        else
            {
            LOGGER.debug(debug, "dao.EventLogMetadata is NULL");
            }

        if ((wrapper != null)
            && wrapper.getEventLogFragment() != null)
            {
            LOGGER.debug(debug, "wrapper.EventLogFragment [size=" + wrapper.getEventLogFragment().size() + "]");
            }
        else
            {
            LOGGER.debug(debug, "wrapper.EventLogFragment is NULL");
            }

        if ((dao != null)
            && (dao.getEventLogFragment() != null))
            {
            LOGGER.debug(debug, "dao.EventLogFragment [size=" + dao.getEventLogFragment().size() + "]");
            }
        else
            {
            LOGGER.debug(debug, "dao.EventLogFragment is NULL");
            }

        if ((wrapper != null)
            && wrapper.getInstrumentLogMetadata() != null)
            {
            LOGGER.debug(debug, "wrapper.InstrumentLogMetadata [size=" + wrapper.getInstrumentLogMetadata().size() + "]");
            }
        else
            {
            LOGGER.debug(debug, "wrapper.InstrumentLogMetadata is NULL");
            }

        if ((dao != null)
            && (dao.getInstrumentLogMetadata() != null))
            {
            LOGGER.debug(debug, "dao.InstrumentLogMetadata [size=" + dao.getInstrumentLogMetadata().size() + "]");
            }
        else
            {
            LOGGER.debug(debug, "dao.InstrumentLogMetadata is NULL");
            }

        if ((wrapper != null)
            && wrapper.getInstrumentLogFragment() != null)
            {
            LOGGER.debug(debug, "wrapper.InstrumentLogFragment [size=" + wrapper.getInstrumentLogFragment().size() + "]");
            }
        else
            {
            LOGGER.debug(debug, "wrapper.InstrumentLogFragment is NULL");
            }

        if ((dao != null)
            && (dao.getInstrumentLogFragment() != null))
            {
            LOGGER.debug(debug, "dao.InstrumentLogFragment [size=" + dao.getInstrumentLogFragment().size() + "]");
            }
        else
            {
            LOGGER.debug(debug, "dao.InstrumentLogFragment is NULL");
            }

        LOGGER.debug(debug, "-------------------------------------------------------------------------------------------");
        }


    /***********************************************************************************************
     * Construct a DAOWrapper, using the DAO directly.
     *
     * @param commandmessage
     * @param responsemessage
     * @param responsevalue
     * @param dao
     */

    public DAOWrapper(final CommandMessageInterface commandmessage,
                      final ResponseMessageInterface responsemessage,
                      final String responsevalue,
                      final ObservatoryInstrumentDAOInterface dao)
        {
        // Message context
        this.commandMessage = commandmessage;
        this.responseMessage = responsemessage;

        this.strResponseValue = responsevalue;

        this.daoInterface = dao;

        // This shouldn't be necessary!
        harmoniseWrapperWithDAO(dao);
        }


    /***********************************************************************************************
     * Construct a DAOWrapper, using the DAO directly.
     *
     * @param dao
     */

    public DAOWrapper(final ObservatoryInstrumentDAOInterface dao)
        {
        // Message context
        this.commandMessage = null;
        this.responseMessage = null;
        this.strResponseValue = null;

        this.daoInterface = dao;

        // This shouldn't be necessary!
        harmoniseWrapperWithDAO(dao);
        }


    /**********************************************************************************************/
    /* Command Lifecycle                                                                          */
    /***********************************************************************************************
     * Get the CommandMessage responsible for producing these data.
     *
     * @return CommandMessageInterface
     */

    public CommandMessageInterface getCommandMessage()
        {
        return (this.commandMessage);
        }


    /***********************************************************************************************
     * Get the ResponseMessage resulting from the command execution.
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface getResponseMessage()
        {
        return (this.responseMessage);
        }


    /***********************************************************************************************
     * Get the ResponseValue.
     *
     * @return String
     */

    public String getResponseValue()
        {
        return (this.strResponseValue);
        }


    /***********************************************************************************************
     * Get the Wrapped DAO.
     *
     * @return ObservatoryInstrumentDAOInterface
     */

    public ObservatoryInstrumentDAOInterface getWrappedDAO()
        {
        return (this.daoInterface);
        }


    /**********************************************************************************************/
    /* Bodge Central                                                                              */
    /***********************************************************************************************
     * Make the contents of the Wrapper agree with the contents of the DAO.
     * This really shows we need to rethink the Wrapper concept!
     */

    public void harmoniseWrapperWithDAO(final ObservatoryInstrumentDAOInterface dao)
        {
        final String SOURCE = "DAOWrapper.harmoniseWrapperWithDAO() ";

        // This shouldn't be necessary!
        if (dao != null)
            {
            // Metadata containers
            this.listMetadataMetadata = dao.getMetadataMetadata();
            this.listCurrentObservatoryMetadata = dao.getCurrentObservatoryMetadata();
            this.listCurrentObserverMetadata = dao.getCurrentObserverMetadata();
            this.listObservationMetadata = dao.getObservationMetadata();
            this.listInstrumentMetadata = dao.getInstrumentMetadata();
            this.listControllerMetadata = dao.getControllerMetadata();
            this.listPluginMetadata = dao.getPluginMetadata();
            this.listRawDataMetadata = dao.getRawDataMetadata();
            this.listXYDatasetMetadata = dao.getXYDatasetMetadata();

            // POIs and LOIs
            this.listPOIs = dao.getPointOfInterestList();
            this.listLOIs = dao.getLineOfInterestList();

            // Data containers
            this.vecRawData = dao.getRawData();
            this.xyDataset = dao.getXYDataset();
            this.imageData = dao.getImageData();
            this.userObject = dao.getUserObject();

            LOGGER.debug(LOADER_PROPERTIES.isChartDebug(),
                         SOURCE + "DAO Chart --> Wrapper Chart");
            this.associatedChartUI = dao.getChartUI();

            // Logging
            this.vecEventLogFragment = dao.getEventLogFragment();
            this.listEventLogMetadata = dao.getEventLogMetadata();
            this.vecInstrumentLogFragment = dao.getInstrumentLogFragment();
            this.listInstrumentLogMetadata = dao.getInstrumentLogMetadata();
            }
        else
            {
            // Metadata containers
            this.listMetadataMetadata = new ArrayList<Metadata>(1);
            this.listCurrentObservatoryMetadata = new ArrayList<Metadata>(1);
            this.listCurrentObserverMetadata = new ArrayList<Metadata>(1);
            this.listObservationMetadata = new ArrayList<Metadata>(1);
            this.listInstrumentMetadata = new ArrayList<Metadata>(1);
            this.listControllerMetadata = new ArrayList<Metadata>(1);
            this.listPluginMetadata = new ArrayList<Metadata>(1);
            this.listRawDataMetadata = new ArrayList<Metadata>(1);
            this.listXYDatasetMetadata = new ArrayList<Metadata>(1);

            // POIs and LOIs
            this.listPOIs = new ArrayList<PointOfInterest>(1);
            this.listLOIs = new ArrayList<LineOfInterest>(1);

            // Data containers
            this.vecRawData = new Vector<Object>(1);
            this.xyDataset = null;
            this.imageData = null;
            this.userObject = null;

            LOGGER.debug(LOADER_PROPERTIES.isChartDebug(),
                         SOURCE + "NULL --> Wrapper Chart");
            this.associatedChartUI = null;

            // Logging
            this.vecEventLogFragment = new Vector<Vector>(1);
            this.listEventLogMetadata = new ArrayList<Metadata>(1);
            this.vecInstrumentLogFragment = new Vector<Vector>(1);
            this.listInstrumentLogMetadata = new ArrayList<Metadata>(1);

            //LOGGER.error("DAOWrapper Initialising for NULL DAO");
            }
        }


    /**********************************************************************************************/
    /* Metadata                                                                                   */
    /***********************************************************************************************
     * Get the Metadata Metadata List.
     *
     * @return List<Metadata>
     */

    public List<Metadata> getMetadataMetadata()
        {
        return (this.listMetadataMetadata);
        }


    /***********************************************************************************************
     * Set the Metadata Metadata List.
     *
     * @param metadata
     */

    private void setMetadataMetadata(final List<Metadata> metadata)
        {
        this.listMetadataMetadata = metadata;
        }


    /*********************************************************************************************
     * Get the CurrentObservatoryMetadata.
     *
     * @return List<Metadata>
     */

    public List<Metadata> getCurrentObservatoryMetadata()
        {
        return (this.listCurrentObservatoryMetadata);
        }


    /***********************************************************************************************
     * Set the CurrentObservatoryMetadata List.
     *
     * @param metadata
     */

    private void setCurrentObservatoryMetadata(final List<Metadata> metadata)
        {
        this.listCurrentObservatoryMetadata = metadata;
        }


    /***********************************************************************************************
     * Get the CurrentObserverMetadata.
     *
     * @return List<Metadata>
     */

    public List<Metadata> getCurrentObserverMetadata()
        {
        return (this.listCurrentObserverMetadata);
        }


    /***********************************************************************************************
     * Set the CurrentObserverMetadata List.
     *
     * @param metadata
     */

    private void setCurrentObserverMetadata(final List<Metadata> metadata)
        {
        this.listCurrentObserverMetadata = metadata;
        }


    /***********************************************************************************************
     * Get the ObservationMetadata List.
     *
     * @return List<Metadata>
     */

    public List<Metadata> getObservationMetadata()
        {
        return (this.listObservationMetadata);
        }


    /***********************************************************************************************
     * Set the ObservationMetadata List.
     *
     * @param metadata
     */

    private void setObservationMetadata(final List<Metadata> metadata)
        {
        this.listObservationMetadata = metadata;
        }


    /***********************************************************************************************
     * Get the InstrumentMetadata List.
     *
     * @return List<Metadata>
     */

    public List<Metadata> getInstrumentMetadata()
        {
        return (this.listInstrumentMetadata);
        }


    /***********************************************************************************************
     * Set the InstrumentMetadata List.
     *
     * @param metadata
     */

    private void setInstrumentMetadata(final List<Metadata> metadata)
        {
        this.listInstrumentMetadata = metadata;
        }


    /***********************************************************************************************
     * Get the ControllerMetadata List.
     *
     * @return List<Metadata>
     */

    public List<Metadata> getControllerMetadata()
        {
        return (this.listControllerMetadata);
        }


    /***********************************************************************************************
     * Set the ControllerMetadata List.
     *
     * @param metadata
     */

    private void setControllerMetadata(final List<Metadata> metadata)
        {
        this.listControllerMetadata = metadata;
        }


    /***********************************************************************************************
     * Get the PluginMetadata List.
     *
     * @return List<Metadata>
     */

    public List<Metadata> getPluginMetadata()
        {
        return (this.listPluginMetadata);
        }


    /***********************************************************************************************
     * Set the PluginMetadata List.
     *
     * @param metadata
     */

    private void setPluginMetadata(final List<Metadata> metadata)
        {
        this.listPluginMetadata = metadata;
        }


    /***********************************************************************************************
     * Get the RawData Metadata List.
     *
     * @return List<Metadata>
     */

    public List<Metadata> getRawDataMetadata()
        {
        return (this.listRawDataMetadata);
        }


    /***********************************************************************************************
     * Set the RawData Metadata List.
     *
     * @param metadata
     */

    private void setRawDataMetadata(final List<Metadata> metadata)
        {
        this.listRawDataMetadata = metadata;
        }


    /***********************************************************************************************
     * Get the XYDataset Metadata List.
     *
     * @return List<Metadata>
     */

    public List<Metadata> getXYDatasetMetadata()
        {
        return (this.listXYDatasetMetadata);
        }


    /***********************************************************************************************
     * Set the XYDataset Metadata List.
     *
     * @param metadata
     */

    private void setXYDatasetMetadata(final List<Metadata> metadata)
        {
        this.listXYDatasetMetadata = metadata;
        }


    /***********************************************************************************************
     * Clear all Metadata containers.
     */

    public void clearMetadata()
        {
        if (getMetadataMetadata() != null)
            {
            getMetadataMetadata().clear();
            }
        else
            {
            setMetadataMetadata(new ArrayList<Metadata>(INITIAL_CAPACITY));
            }

        if (getCurrentObservatoryMetadata() != null)
            {
            getCurrentObservatoryMetadata().clear();
            }
        else
            {
            setCurrentObservatoryMetadata(new ArrayList<Metadata>(INITIAL_CAPACITY));
            }

        if (getCurrentObserverMetadata() != null)
            {
            getCurrentObserverMetadata().clear();
            }
        else
            {
            setCurrentObserverMetadata(new ArrayList<Metadata>(INITIAL_CAPACITY));
            }

        if (getObservationMetadata() != null)
            {
            getObservationMetadata().clear();
            }
        else
            {
            setObservationMetadata(new ArrayList<Metadata>(INITIAL_CAPACITY));
            }

        if (getInstrumentMetadata() != null)
            {
            getInstrumentMetadata().clear();
            }
        else
            {
            setInstrumentMetadata(new ArrayList<Metadata>(INITIAL_CAPACITY));
            }

        if (getControllerMetadata() != null)
            {
            getControllerMetadata().clear();
            }
        else
            {
            setControllerMetadata(new ArrayList<Metadata>(INITIAL_CAPACITY));
            }

        if (getPluginMetadata() != null)
            {
            getPluginMetadata().clear();
            }
        else
            {
            setPluginMetadata(new ArrayList<Metadata>(INITIAL_CAPACITY));
            }
        }


    /**********************************************************************************************/
    /* PointOfInterest                                                                            */
    /***********************************************************************************************
     * Get the list of PointsOfInterest.
     *
     * @return List<PointOfInterest>
     */

    public List<PointOfInterest> getPointOfInterestList()
        {
        return (this.listPOIs);
        }


    /***********************************************************************************************
     * Set the Points of Interest for the Wrapper.
     *
     * @param pois
     */

    public final void setPointOfInterestList(final List<PointOfInterest> pois)
        {
        this.listPOIs = pois;
        }


    /**********************************************************************************************/
    /* LineOfInterest                                                                             */
    /***********************************************************************************************
     * Get the list of LinesOfInterest.
     *
     * @return List<LineOfInterest>
     */

    public List<LineOfInterest> getLineOfInterestList()
        {
        return (this.listLOIs);
        }


    /***********************************************************************************************
     * Set the Lines of Interest for the Wrapper.
     *
     * @param lois
     */

    public final void setLineOfInterestList(final List<LineOfInterest> lois)
        {
        this.listLOIs = lois;
        }


    /**********************************************************************************************/
    /* Data Containers                                                                            */
    /***********************************************************************************************
     * Get the Vector of raw data.
     *
     * @return Vector<Object>
     */

    public Vector<Object> getRawData()
        {
        return (this.vecRawData);
        }


    /***********************************************************************************************
     * Set the Vector of raw data.
     *
     * @param data
     */

    public void setRawData(final Vector<Object> data)
        {
        this.vecRawData = data;
        }


    /***********************************************************************************************
     * Get the XYDataset.
     *
     * @return XYDataset
     */

    public XYDataset getXYDataset()
        {
        return (this.xyDataset);
        }


    /***********************************************************************************************
     * Set the XYDataset.
     *
     * @param dataset
     */

    public void setXYDataset(final XYDataset dataset)
        {
        this.xyDataset = dataset;
        }

    /***********************************************************************************************
     * Get the ImageData.
     *
     * @return Image
     */

    public Image getImageData()
        {
        return (this.imageData);
        }


    /***********************************************************************************************
     * Set the ImageData.
     *
     * @param image
     */

    public void setImageData(final Image image)
        {
        this.imageData = image;
        }


    /***********************************************************************************************
     * Get the UserObject.
     *
     * @return Object
     */

    public Object getUserObject()
        {
        return (this.userObject);
        }


    /***********************************************************************************************
     * Set the UserObject.
     *
     * @param userobject
     */

    public void setUserObject(final Object userobject)
        {
        this.userObject = userobject;
        }


    /***********************************************************************************************
     * Clear all Data containers.
     */

    public void clearData()
        {
        if (getRawDataMetadata() != null)
            {
            getRawDataMetadata().clear();
            }
        else
            {
            setRawDataMetadata(new ArrayList<Metadata>(INITIAL_CAPACITY));
            }

        if (getXYDatasetMetadata() != null)
            {
            getXYDatasetMetadata().clear();
            }
        else
            {
            setXYDatasetMetadata(new ArrayList<Metadata>(INITIAL_CAPACITY));
            }

        if (getRawData() != null)
            {
            getRawData().clear();
            }
        else
            {
            setRawData(new Vector<Object>(INITIAL_CAPACITY));
            }

        if (getXYDataset() != null)
            {
            setXYDataset(null);
            }

        if (getImageData() != null)
            {
            setImageData(null);
            }

        if (getUserObject() != null)
            {
            setUserObject(null);
            }
        }


    /***********************************************************************************************
     * Get the Channel count.
     *
     * @return int
     */

    public int getRawDataChannelCount()
        {
        return (getWrappedDAO().getRawDataChannelCount());
        }


    /***********************************************************************************************
     * Indicate if the first data channel represents Temperature Usually a Staribus dataset).
     *
     * @return boolean
     */

    public boolean hasTemperatureChannel()
        {
        return (getWrappedDAO().hasTemperatureChannel());
        }


    /***********************************************************************************************
     * Indicate if this DAO has unsaved data.
     * Data may be RawData or Image.
     *
     * @return boolean
     */

    public boolean hasUnsavedData()
        {
        return (getWrappedDAO().hasUnsavedData());
        }


    /***********************************************************************************************
     * Get the currently selected Chart for use with this DAO.
     *
     * @return JFreeChart
     */

    public ChartUIComponentPlugin getChartUI()
        {
        return (this.associatedChartUI);
        }


    /**********************************************************************************************/
    /* Logging                                                                                    */
    /***********************************************************************************************
     * Get the EventLog Metadata List.
     *
     * @return List<Metadata>
     */

    public List<Metadata> getEventLogMetadata()
        {
        return (this.listEventLogMetadata);
        }


    /***********************************************************************************************
     * Get the EventLogFragment.
     *
     * @return Vector<Vector>
     */

    public Vector<Vector> getEventLogFragment()
        {
        return (this.vecEventLogFragment);
        }


    /***********************************************************************************************
     * Get the InstrumentLog Metadata List.
     *
     * @return List<Metadata>
     */

    public List<Metadata> getInstrumentLogMetadata()
        {
        return (this.listInstrumentLogMetadata);
        }


    /***********************************************************************************************
     * Get the InstrumentLogFragment.
     *
     * @return Vector<Vector>
     */

    public Vector<Vector> getInstrumentLogFragment()
        {
        return (this.vecInstrumentLogFragment);
        }
    }
