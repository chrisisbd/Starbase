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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs;


import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.common.datatranslators.DatasetType;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.sda.DatasetState;

import java.util.List;


/***************************************************************************************************
 * DatasetManagerInterface.
 */

public interface DatasetManagerInterface extends FrameworkConstants,
                                                 FrameworkStrings,
                                                 FrameworkMetadata,
                                                 FrameworkSingletons
    {
    // String Resources
    String MSG_EVENT_LOG        = "Examine the EventLog tab for possible causes.";
    String MSG_FAIL_ATTACH      = "It was not possible to attach the Imported Dataset, please reset the Instrument and try again.";
    String MSG_FAIL_DETACH      = "It was not possible to detach the Imported Dataset, please reset the Instrument and try again.";
    String MSG_FAIL_DETACHALL   = "It was not possible to detach all datasets, please reset the Instrument and try again.";
    String MSG_FAIL_REMOVE      = "It was not possible to remove the Imported Dataset, please reset the Instrument and try again.";
    String MSG_FAIL_IMPORT      = "The dataset Import did not succeed.";

    int MAX_SECONDARY_DAOS = 32;
    int NO_SELECTION_INDEX = -1;
    double DEFAULT_OFFSET = 0.0;


    /**********************************************************************************************/
    /* DatasetManager State                                                                       */
    /***********************************************************************************************
     * Initialise the DatasetManager.
     */

    void initialise();


    /***********************************************************************************************
     * Dispose the DatasetManager.
     */

    void dispose();


    /**********************************************************************************************/
    /* DatasetManager Selection                                                                   */
    /***********************************************************************************************
     * Get the Secondary Dataset Index. Return -1 on no selection.
     *
     * @return int
     */

    int getSelectedIndex();


    /***********************************************************************************************
     * Set the Secondary Dataset Index. Set -1 for no selection.
     *
     * @param index
     */

    void setSelectedIndex(int index);


    /***********************************************************************************************
     * Get the currently selected DAO, or return NULL if there is no selection.
     *
     * @return ObservatoryInstrumentDAOInterface
     */

    ObservatoryInstrumentDAOInterface getSelectedDAO();


    /***********************************************************************************************
     * Get the currently selected Name, or return EMPTY_STRING if there is no selection.
     *
     * @return String
     */

    String getSelectedName();


    /***********************************************************************************************
     * Get the currently selected DatasetState, or return DETACHED if there is no selection.
     *
     * @return DatasetState
     */

    DatasetState getSelectedState();


    /***********************************************************************************************
     * Get the currently selected Offset, or return 0.0 if there is no selection.
     *
     * @return double
     */

    double getSelectedOffset();


    /***********************************************************************************************
     * Set the Offset of the current Dataset selection.
     *
     * @param offset
     *
     * @return boolean
     */

    boolean setSelectedOffset(double offset);


    /***********************************************************************************************
     * A convenience method to indicate if the Offset may be adjusted.
     * This is not allowed for the Primary Dataset.
     *
     * @return boolean
     */

    boolean canAdjustOffset();


    /***********************************************************************************************
     * A convenience method to indicate if the Dataset may be moved to the Attached state.
     *
     * @return boolean
     */

    boolean canAttach();


    /***********************************************************************************************
     * A convenience method to indicate if the Dataset may be moved to the Detached state.
     * The User will be asked if they really want to Detach the Primary,
     * which will clear all current attachments.
     *
     * @return boolean
     */

    boolean canDetach();


    /***********************************************************************************************
     * A convenience method to indicate if ALL Datasets may be moved to the Detached state,
     * regardless of any current selection.
     *
     * @return boolean
     */

    boolean canDetachAll();


    /***********************************************************************************************
     * A convenience method to indicate if the Dataset may be Removed.
     * A Primary Dataset cannot be removed directly, it must be Detached first.
     *
     * @return boolean
     */

    boolean canRemove();


    /***********************************************************************************************
     * A convenience method to indicate if the DatasetManager is fully occupied.
     *
     * @return boolean
     */

    boolean isFull();


    /***********************************************************************************************
     * A convenience method to indicate if there is a current Dataset selection.
     *
     * @return boolean
     */

    boolean hasSelection();


    /***********************************************************************************************
     * Indicate if the current Dataset selection is the Primary.
     * Return FALSE if there is no selection.
     *
     * @return boolean
     */

    boolean isPrimary();


    /**********************************************************************************************/
    /* DatasetManager Operations                                                                  */
    /***********************************************************************************************
     * Import a Dataset to the DatasetManager.
     *
     * @param hostinstrument
     * @param datasettype
     * @param dao
     *
     * @return boolean
     */

    boolean importDataset(ObservatoryInstrumentInterface hostinstrument,
                          DatasetType datasettype,
                          ObservatoryInstrumentDAOInterface dao);


    /***********************************************************************************************
     * Attach the currently selected Dataset to the Composite.
     *
     * @return boolean
     */

    boolean attach();


    /***********************************************************************************************
     * Detach the currently selected Dataset from the Composite.
     *
     * @return boolean
     */

    boolean detach();


    /***********************************************************************************************
     * Detach all of the Datasets from the Composite, including the Primary.
     * Do not change the Offsets or the current selected Index.
     *
     * @return boolean
     */

    boolean detachAll();


    /***********************************************************************************************
     * Remove the currently selected Dataset completely.
     *
     * @return boolean
     */

    boolean remove();


    /**********************************************************************************************/
    /* DatasetManager Utilities                                                                   */
    /***********************************************************************************************
     * Get the DatasetType of all datasets, defined by the first Import.
     * NULL is returned before the first Import, which sets the type for all subsequent imports.
     *
     * @return DatasetType
     */

    DatasetType getDatasetType();


    /***********************************************************************************************
     * Set the DatasetType of all datasets, defined by the first Import.
     *
     * @param type
     */

    void setDatasetType(DatasetType type);


    /***********************************************************************************************
     * Get the List of Secondary DAOs.
     *
     * @return List<ObservatoryInstrumentDAOInterface>
     */

    List<ObservatoryInstrumentDAOInterface> getSecondaryDAOs();


    /***********************************************************************************************
     * Get the List of Secondary Dataset Names.
     *
     * @return List<String>
     */

    List<String> getSecondaryDatasetNames();


    /***********************************************************************************************
     * Get the List of Secondary DatasetStates.
     *
     * @return List<DatasetState>
     */

    List<DatasetState> getSecondaryStates();


    /***********************************************************************************************
     * Get the List of Secondary Offsets.
     *
     * @return List<Double>
     */

    List<Double> getSecondaryOffsets();


    /***********************************************************************************************
     * Get the size of the DatasetManager. i.e. how many DAOs are available.
     *
     * @return int
     */

    int size();


    /***********************************************************************************************
     * Get the List of DAOs which are currently attached to the Composite.
     *
     * @return List<ObservatoryInstrumentDAOInterface>
     */

    List<ObservatoryInstrumentDAOInterface> getAttachedDAOs();


    /***********************************************************************************************
     * Given a DAO, get the corresponding DatasetIndex.
     * This is to allow working backwards from an AttachedDAO to the index on the DatasetSelector.
     * Return -1 if the DAO is not found in the collection.
     *
     * @param dao
     */

    int getDatasetIndex(ObservatoryInstrumentDAOInterface dao);


    /***********************************************************************************************
     * Indicate if the DatasetManager is set up correctly.
     *
     * @return boolean
     */

    boolean isValidManager();


    /***********************************************************************************************
     * Debug the DatasetManager.
     *
     * @param debug
     * @param message
     */

    void debugManager(boolean debug,
                      String message);
    }
