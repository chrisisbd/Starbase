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

import org.lmn.fc.common.constants.*;
import org.lmn.fc.frameworks.starbase.plugins.observatory.events.InstrumentStateChangedListener;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandProcessorContextInterface;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.model.plugins.AtomPlugin;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;
import org.lmn.fc.model.xmlbeans.instruments.MacrosDocument;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;
import org.lmn.fc.model.xmlbeans.poi.LineOfInterest;
import org.lmn.fc.model.xmlbeans.poi.PointOfInterest;

import javax.swing.*;
import java.util.List;
import java.util.Vector;


/***************************************************************************************************
 * ObservatoryInstrumentInterface.
 */

public interface ObservatoryInstrumentInterface extends FrameworkConstants,
                                                        FrameworkStrings,
                                                        FrameworkMetadata,
                                                        FrameworkSingletons,
                                                        ResourceKeys
    {
    // String Resources
    String INSTRUMENT_NOT_LOADED = "Instrument not loaded:";
    String TOOLTIP_START = "Start the Instrument";
    String TOOLTIP_STOP = "Stop the Instrument";
    String TOOLTIP_BUSY = "The Instrument is Busy, try again later or Abort the current command";
    String TOOLTIP_STOPPED = "You must start the Instrument before executing a Command";
    String TOOLTIP_INSTRUMENT_STATUS = "The status of the Instrument";
    String TOOLTIP_ACTIVITY = "Indicates Instrument activity";

    String TOOLTIP_COMMAND_EXECUTE = "Execute the " + STARSCRIPT + " command";
    String TOOLTIP_COMMAND_REPEAT = "Repeat the " + STARSCRIPT + " command until Abort";
    String TOOLTIP_COMMAND_EXECUTING = "The Instrument is executing the Command";
    String TOOLTIP_COMMAND_REPEATING = "The Instrument is repeating the Command";
    String TOOLTIP_COMMAND_ABORT = "Abort the current Command";

    String TOOLTIP_COMMAND_RECORD = "Record a new Macro";
    String TOOLTIP_COMMAND_EDIT = "Edit or Hide the selected Macro";
    String TOOLTIP_COMMAND_DELETE = "Delete the selected Macro";
    String TOOLTIP_COMMAND_LOAD = "Load the set of Instrument Macros";
    String TOOLTIP_COMMAND_LOADING = "Loading Instrument Macros";
    String TOOLTIP_COMMAND_SAVE = "Save the set of Instrument Macros";
    String TOOLTIP_COMMAND_SAVING = "Saving Instrument Macros";
    String TOOLTIP_COMMAND_SHOW = "Show or Hide the Starscript of the selected Macro";

    String TOOLTIP_INVALID_STATE = "The Instrument is in an invalid state";
    String MSG_NO_CONNECTION = "There is no data connection with the Instrument";

    String INSTRUMENT_DATA_MODE_FTP = "FTP";
    String INSTRUMENT_NOT_FOUND = "No Instrument found";
    String CONTROLLER_NOT_FOUND = "No Controller found";

    String COMMANDS_NOT_FOUND = "No Commands were found";
    String MACROS_NOT_FOUND = "No Macros were found";
    String COMMANDS_NOT_REQUIRED = "No Commands are required";

    String COMMAND_ERROR = "There was an unexpected error in invoking the Command";
    String COMMAND_FAILED = "It was not possible to execute the Command";
    String COMMAND_NOT_AVAILABLE = "The Command is not available";
    String COMMAND_DENIED_ACCESS = "The Command has been denied access";
    String COMMAND_NOT_ACCESSIBLE = "The Command is not accessible";
    String COMMAND_ILLEGAL_ARGUMENT = "The Command has not been correctly specified";
    String COMMAND_INVOCATION_TARGET = "The Command has thrown an unexpected Exception";
    String COMMAND_INITIALISER = "The Command failed to initialise correctly";
    String COMMAND_INDEX = "The Command failed because an index was incorrect";

    String PARAMETERS_NOT_FOUND = "No Parameters were found";
    String PARAMETERS_NOT_REQUIRED = "No Parameters are required";

    String PORT_BUSY ="The Port is busy";
    String PORT_NOT_BUSY ="The Port is not busy";


    /***********************************************************************************************
     * Initialise the ObservatoryInstrument.
     */

    void initialise();


    /***********************************************************************************************
     * Start the ObservatoryInstrument.
     *
     * @return boolean
     */

    boolean start();


    /***********************************************************************************************
     * Stop the ObservatoryInstrument.
     *
     * @return boolean
     */

    boolean stop();


    /***********************************************************************************************
     * Shutdown the ObservatoryInstrument after use.
     */

    void dispose();


    /***********************************************************************************************
     * Reset the ObservatoryInstrument.
     *
     * @param resetmode
     */

    void reset(ResetMode resetmode);


    /***********************************************************************************************
     * Get the ObservatoryInstrumentDAO.
     *
     * @return ObservatoryInstrumentDAOInterface
     */

    ObservatoryInstrumentDAOInterface getDAO();


    /***********************************************************************************************
     * Set the ObservatoryInstrumentDAO.
     *
     * @param dao
     */

    void setDAO(ObservatoryInstrumentDAOInterface dao);


    /***********************************************************************************************
     * This method is called (on the Event Dispatching Thread)
     * by the Update SwingWorker when the update operation is complete and data are available.
     * The Instrument may pass data to a UIComponent, or perform further processing.
     * Optionally refresh the UI of data tabs or update the associated Metadata.
     *
     * @param daowrapper
     * @param forcerefreshdata
     * @param updatemetadata
     */

    void setWrappedData(DAOWrapperInterface daowrapper,
                        boolean forcerefreshdata,
                        boolean updatemetadata);


    /***********************************************************************************************
     * Remove any Data associated with this Instrument's appearance on the UI,
     * on the InstrumentPanel. For instance, remove a Chart.
     */

    void removeInstrumentIdentity();


    /***********************************************************************************************
     * Get the InstrumentState.
     *
     * @return InstrumentState
     */

    InstrumentState getInstrumentState();


    /***********************************************************************************************
     * Set the InstrumentState.
     *
     * @param newstate
     */

    void setInstrumentState(InstrumentState newstate);


    /**********************************************************************************************/
    /* Provide access to useful Instruments                                                       */
    /***********************************************************************************************
     * Get the ObservatoryClock.
     *
     * @return ObservatoryClockInterface
     */

    ObservatoryClockInterface getObservatoryClock();


    /**********************************************************************************************/
    /* UI Components                                                                              */
    /***********************************************************************************************
     * Get the Instrument HostPanel.
     *
     * @return JComponent
     */

    JComponent getHostPanel();


    /***********************************************************************************************
     * Get the Instrument SelectorPanel (used by RackCabinet).
     *
     * @return JComponent
     */

    JComponent getSelectorPanel();


    /***********************************************************************************************
     * Set the Instrument SelectorPanel.
     *
     * @param panel
     */

    void setSelectorPanel(JComponent panel);


    /***********************************************************************************************
     * Get the Instrument ControlPanel.
     *
     * @return InstrumentUIComponentDecorator
     */

    InstrumentUIComponentDecoratorInterface getControlPanel();


    /***********************************************************************************************
     * Set the ControlPanel to a decorated version of a UIComponent.
     *
     * @param panel
     * @param displayname
     */

    void setControlPanel(InstrumentUIComponentDecoratorInterface panel,
                         String displayname);


    /***********************************************************************************************
     * Get the On Button.
     *
     * @return JButton
     */

    JButton getOnButton();


    /***********************************************************************************************
     * Get the Off Button.
     *
     * @return JButton
     */

    JButton getOffButton();


    /***********************************************************************************************
     * Get the Instrument InstrumentPanel.
     *
     * @return InstrumentUIComponentDecorator
     */

    InstrumentUIComponentDecoratorInterface getInstrumentPanel();


    /***********************************************************************************************
     * Set the Instrument InstrumentPanel.
     *
     * @param panel
     */

    void setInstrumentPanel(InstrumentUIComponentDecoratorInterface panel);


    /***********************************************************************************************
     * Get the Instrument Xml.
     *
     * @return Instrument
     */

    Instrument getInstrument();


    /***********************************************************************************************
     * Set the Instrument Xml.
     *
     * @param  xml
     */

    void setInstrument(Instrument xml);


    /***********************************************************************************************
     * Get the host AtomPlugin.
     *
     * @return AtomPlugin
     */

    AtomPlugin getHostAtom();


    /***********************************************************************************************
     * Get the host UI.
     *
     * @return ObservatoryUIInterface
     */

    ObservatoryUIInterface getHostUI();


    /***********************************************************************************************
     * Get the FontDataType.
     *
     * @return FontPlugin
     */

    FontInterface getFontData();


    /***********************************************************************************************
     * Get the ColourDataType.
     *
     * @return ColourPlugin
     */

    ColourInterface getColourData();


    /***********************************************************************************************
     * Get the Vector Configuration data to append to a Report.
     *
     * @return Vector<Vector>
     */

    Vector<Vector> getInstrumentConfiguration();


    /***************************************************************************************************
     * Set the Vector of configuration data to append to a Report.
     *
     * @param config
     */

    void setInstrumentConfiguration(Vector<Vector> config);


    /***********************************************************************************************
     * Get the Macros associated with this Instrument.
     *
     * @return MacrosDocument
     */

    MacrosDocument getInstrumentMacros();


    /***********************************************************************************************
     * Set the Macros associated with this Instrument.
     *
     * @param macros
     */

    void setInstrumentMacros(MacrosDocument macros);


    /**********************************************************************************************/
    /* Metadata                                                                                   */
    /***********************************************************************************************
     * Get the ObservatoryInstrument Aggregate Metadata.
     *
     * @return Vector<Vector>
     */

    List<Metadata> getAggregateMetadata();


    /***********************************************************************************************
     * Set the ObservatoryInstrument Aggregate Metadata.
     *
     * @param metadata
     */

    void setAggregateMetadata(List<Metadata> metadata);


    /**********************************************************************************************/
    /* PointOfInterest                                                                            */
    /***********************************************************************************************
     * Get the list of Composite PointsOfInterest.
     *
     * @return List<PointOfInterest>
     */

    List<PointOfInterest> getCompositePointOfInterestList();


    /***********************************************************************************************
     * Set the Composite Points of Interest for the Instrument.
     *
     * @param pois
     */

    void setCompositePointOfInterestList(List<PointOfInterest> pois);


    /**********************************************************************************************/
    /* LineOfInterest                                                                             */
    /***********************************************************************************************
     * Get the list of Composite LinesOfInterest.
     *
     * @return List<LineOfInterest>
     */

    List<LineOfInterest> getCompositeLineOfInterestList();


    /***********************************************************************************************
     * Set the Composite Lines of Interest for the Instrument.
     *
     * @param lois
     */

    void setCompositeLineOfInterestList(List<LineOfInterest> lois);


    /**********************************************************************************************/
    /* Logging                                                                                    */
    /***********************************************************************************************
     * Get the ObservatoryInstrument EventLog Metadata, i.e describing the columns of the EventLog.
     * Instruments must override this if their log implementations are different from the default.
     *
     * @return List<Metadata>
     */

    List<Metadata> getEventLogMetadata();


    /***********************************************************************************************
     * Get the ObservatoryInstrument EventLog.
     *
     * @return Vector<Vector>
     */

    Vector<Vector> getEventLog();


    /***********************************************************************************************
     * Get the number of columns in the ObservatoryInstrument EventLog.
     * Instruments must override this if their logs are different from the default.
     *
     * @return int
     */

    int getEventLogWidth();


    /***********************************************************************************************
     * Add an ObservatoryInstrument EventLog fragment, i.e. a collection of entries.
     * The EventLog will be sorted on any Report.
     * This is really a Vector<Vector<Object>> !
     *
     * Entry0   object0  object1  object2
     * Entry1   objectA  objectB  objectC
     * Entry2   objectP  objectQ  objectR
     *
     * @param logfragment
     */

    void addEventLogFragment(Vector<Vector> logfragment);


    /***********************************************************************************************
     * Get the ObservatoryInstrument InstrumentLog Metadata,
     * i.e describing the columns of the InstrumentLog.
     * Instruments must override this if their log implementations are different from the default.
     *
     * @return List<Metadata>
     */

    List<Metadata> getInstrumentLogMetadata();


    /***********************************************************************************************
     * Get the ObservatoryInstrument InstrumentLog.
     *
     * @return Vector<Vector>
     */

    Vector<Vector> getInstrumentLog();


    /***********************************************************************************************
     * Get the number of columns in the ObservatoryInstrument InstrumentLog.
     * Instruments must override this if their logs are different from the default.
     *
     * @return int
     */

    int getInstrumentLogWidth();


    /***********************************************************************************************
     * Add an ObservatoryInstrument InstrumentLog fragment, i.e. a collection of entries.
     * The InstrumentLog will be sorted on any Report.
     * This is really a Vector<Vector<Object>> !
     *
     * Entry0   object0  object1  object2
     * Entry1   objectA  objectB  objectC
     * Entry2   objectP  objectQ  objectR
     *
     * @param logfragment
     */

    void addInstrumentLogFragment(Vector<Vector> logfragment);


    /***********************************************************************************************
     * Get the Instrument ResourceKey.
     *
     * @return String
     */

    String getResourceKey();


    /***********************************************************************************************
     * Get the CommandProcessorContext.
     *
     * @return CommandProcessorContext
     */

    CommandProcessorContextInterface getContext();


    /***********************************************************************************************
     * Set the CommandProcessorContext.
     *
     * @param context
     */

    void setContext(CommandProcessorContextInterface context);


    /***********************************************************************************************
     * Indicate if the Instrument is in debug mode.
     *
     * @return boolean
     */

    boolean isDebugMode();


    /***********************************************************************************************
     *  Read all the Resources required by the ObservatoryInstrument.
     */

    void readResources();


    /**********************************************************************************************/
    /* Events                                                                                     */
    /***********************************************************************************************
     * Notify all listeners of InstrumentStateChangedEvents.
     *
     * @param eventsource
     * @param instrument
     * @param currentstate
     * @param nextstate
     * @param repeatnumber
     * @param repeattext
     */

    void notifyInstrumentStateChangedEvent(Object eventsource,
                                           ObservatoryInstrumentInterface instrument,
                                           InstrumentState currentstate,
                                           InstrumentState nextstate,
                                           long repeatnumber,
                                           String repeattext);


    /***********************************************************************************************
     * Get the InstrumentStateChanged Listeners (mostly for testing).
     *
     * @return Vector<InstrumentStateChangedListener>
     */

    Vector<InstrumentStateChangedListener> getInstrumentStateChangedListeners();


    /***********************************************************************************************
     * Add a listener for this event.
     *
     * @param listener
     */

    void addInstrumentStateChangedListener(InstrumentStateChangedListener listener);


    /***********************************************************************************************
     * Remove a listener for this event.
     *
     * @param listener
     */

    void removeInstrumentStateChangedListener(InstrumentStateChangedListener listener);
    }
