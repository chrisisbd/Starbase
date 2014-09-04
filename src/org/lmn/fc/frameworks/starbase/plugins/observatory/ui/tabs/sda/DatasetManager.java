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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.sda;


import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYSeriesCollection;
import org.lmn.fc.common.datatranslators.DatasetType;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.DatasetManagerInterface;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/***************************************************************************************************
 * DatasetManager.
 */

public final class DatasetManager implements DatasetManagerInterface
    {

    // All managed Datasets must be of the same type
    private DatasetType datasetType;

    // The index into the List of Secondary Datasets
    private int intSelectionIndex;

    private final List<ObservatoryInstrumentDAOInterface> listSecondaryDAOs;
    private final List<String> listSecondaryDatasetNames;
    private final List<DatasetState> listSecondaryStates;
    private final List<Double> listSecondaryOffsets;
    private final List<ObservatoryInstrumentDAOInterface> listAttachedDAOs;


    /***********************************************************************************************
     * Construct a DatasetManager.
     */

    public DatasetManager()
        {
        this.datasetType = null;

        // Indicate No Selection
        this.intSelectionIndex = NO_SELECTION_INDEX;

        this.listSecondaryDAOs = new ArrayList<ObservatoryInstrumentDAOInterface>(MAX_SECONDARY_DAOS);
        this.listSecondaryDatasetNames = new ArrayList<String>(MAX_SECONDARY_DAOS);
        this.listSecondaryStates = new ArrayList<DatasetState>(MAX_SECONDARY_DAOS);
        this.listSecondaryOffsets = new ArrayList<Double>(MAX_SECONDARY_DAOS);

        this.listAttachedDAOs = new ArrayList<ObservatoryInstrumentDAOInterface>(MAX_SECONDARY_DAOS);
        }


    /**********************************************************************************************/
    /* DatasetManager State                                                                       */
    /***********************************************************************************************
     * Initialise the DatasetManager.
     */

    public void initialise()
        {
        setDatasetType(null);

        setSelectedIndex(NO_SELECTION_INDEX);

        getSecondaryDAOs().clear();
        getSecondaryDatasetNames().clear();
        getSecondaryStates().clear();
        getSecondaryOffsets().clear();

        getAttachedDAOs().clear();
        }


    /***********************************************************************************************
     * Dispose the DatasetManager.
     */

    public void dispose()
        {
        if ((getSecondaryDAOs() != null)
            && (!getSecondaryDAOs().isEmpty()))
            {
            final Iterator<ObservatoryInstrumentDAOInterface> iterDAOs;

            iterDAOs = getSecondaryDAOs().iterator();

            while (iterDAOs.hasNext())
                {
                final ObservatoryInstrumentDAOInterface daoSecondary;

                daoSecondary = iterDAOs.next();

                if (daoSecondary != null)
                    {
                    daoSecondary.disposeDAO();
                    }
                }
            }

        setSelectedIndex(NO_SELECTION_INDEX);

        getSecondaryDAOs().clear();
        getSecondaryDatasetNames().clear();
        getSecondaryStates().clear();
        getSecondaryOffsets().clear();

        getAttachedDAOs().clear();
        }


    /**********************************************************************************************/
    /* DatasetManager Selection                                                                   */
    /***********************************************************************************************
     * Get the Secondary Dataset Index. Return -1 on no selection.
     *
     * @return int
     */

    public int getSelectedIndex()
        {
        return (this.intSelectionIndex);
        }


    /***********************************************************************************************
     * Set the Secondary Dataset Index. Set -1 for no selection.
     *
     * @param index
     */

    public void setSelectedIndex(final int index)
        {
        this.intSelectionIndex = index;
        }


    /***********************************************************************************************
     * Get the currently selected DAO, or return NULL if there is no selection.
     *
     * @return ObservatoryInstrumentDAOInterface
     */

    public ObservatoryInstrumentDAOInterface getSelectedDAO()
        {
        final ObservatoryInstrumentDAOInterface daoSelected;

        if (hasSelection())
            {
            daoSelected = getSecondaryDAOs().get(getSelectedIndex());
            }
        else
            {
            daoSelected = null;
            }

        return (daoSelected);
        }


    /***********************************************************************************************
     * Get the currently selected Name, or return EMPTY_STRING if there is no selection.
     *
     * @return String
     */

    public String getSelectedName()
        {
        final String strSelectedName;

        if (hasSelection())
            {
            strSelectedName = getSecondaryDatasetNames().get(getSelectedIndex());
            }
        else
            {
            strSelectedName = EMPTY_STRING;
            }

        return (strSelectedName);
        }


    /***********************************************************************************************
     * Get the currently selected DatasetState, or return DETACHED if there is no selection.
     *
     * @return DatasetState
     */

    public DatasetState getSelectedState()
        {
        final DatasetState stateSelected;

        if (hasSelection())
            {
            stateSelected = getSecondaryStates().get(getSelectedIndex());
            }
        else
            {
            stateSelected = DatasetState.DETACHED;
            }

        return (stateSelected);
        }


    /***********************************************************************************************
     * Get the currently selected Offset, or return 0.0 if there is no selection.
     *
     * @return double
     */

    public double getSelectedOffset()
        {
        final double dblSelectedOffset;

        if (hasSelection())
            {
            dblSelectedOffset = getSecondaryOffsets().get(getSelectedIndex());
            }
        else
            {
            dblSelectedOffset = 0.0;
            }

        return (dblSelectedOffset);
        }


    /***********************************************************************************************
     * Set the Offset of the current Dataset selection.
     * Return FALSE if there is no selection.
     *
     * @param offset
     *
     * @return boolean
     */

    public boolean setSelectedOffset(final double offset)
        {
        final boolean boolSuccess;

        boolSuccess = hasSelection();

        if (boolSuccess)
            {
            getSecondaryOffsets().set(getSelectedIndex(), offset);
            }

        return (boolSuccess);
        }


    /***********************************************************************************************
     * A convenience method to indicate if the Offset may be adjusted.
     * This is not allowed for the Primary Dataset.
     *
     * @return boolean
     */

    public boolean canAdjustOffset()
        {
        final boolean boolCanAdjust;

        boolCanAdjust = hasSelection() && !isPrimary();

        return (boolCanAdjust);
        }


    /***********************************************************************************************
     * A convenience method to indicate if the Dataset may be moved to the Attached state.
     *
     * @return boolean
     */

    public boolean canAttach()
        {
        final boolean boolCanAttach;

        boolCanAttach = hasSelection()
                        && (!DatasetState.ATTACHED.equals(getSelectedState()))
                        && (!DatasetState.LOCKED.equals(getSelectedState()));

        return (boolCanAttach);
        }


    /***********************************************************************************************
     * A convenience method to indicate if the Dataset may be moved to the Detached state.
     *
     * @return boolean
     */

    public boolean canDetach()
        {
        final boolean boolCanDetach;

        boolCanDetach = hasSelection()
                        && ((DatasetState.ATTACHED.equals(getSelectedState()))
                            || (DatasetState.LOCKED.equals(getSelectedState())));

        return (boolCanDetach);
        }


    /***********************************************************************************************
     * A convenience method to indicate if ALL Datasets may be moved to the Detached state,
     * regardless of any current selection.
     *
     * @return boolean
     */

    public boolean canDetachAll()
        {
        final boolean boolCanDetachAll;

        boolCanDetachAll = (getAttachedDAOs().size() > 0);

        return (boolCanDetachAll);
        }


    /***********************************************************************************************
     * A convenience method to indicate if the Dataset may be Removed.
     * A Primary Dataset cannot be removed directly, it must be Detached first.
     *
     * @return boolean
     */

    public boolean canRemove()
        {
        final boolean boolCanRemove;

        boolCanRemove = hasSelection()
                        && (!isPrimary())
                        && ((DatasetState.ATTACHED.equals(getSelectedState()))
                            || (DatasetState.DETACHED.equals(getSelectedState())));

        return (boolCanRemove);
        }


    /***********************************************************************************************
     * A convenience method to indicate if the DatasetManager is fully occupied.
     *
     * @return boolean
     */

    public boolean isFull()
        {
        final boolean boolFull;

        boolFull = (getSecondaryDAOs().size() == MAX_SECONDARY_DAOS);

        return (boolFull);
        }


    /***********************************************************************************************
     * A convenience method to indicate if there is a current Dataset selection.
     *
     * @return boolean
     */

    public boolean hasSelection()
        {
        final boolean boolSuccess;

        boolSuccess = (getSelectedIndex() > NO_SELECTION_INDEX);

        return (boolSuccess);
        }


    /***********************************************************************************************
     * Indicate if the current Dataset selection is the Primary.
     * Return FALSE if there is no selection.
     *
     * @return boolean
     */

    public boolean isPrimary()
        {
        final boolean boolPrimary;

        boolPrimary = hasSelection()
                      && (DatasetState.LOCKED.equals(getSelectedState()));

        return (boolPrimary);
        }


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

    public synchronized boolean importDataset(final ObservatoryInstrumentInterface hostinstrument,
                                              final DatasetType datasettype,
                                              final ObservatoryInstrumentDAOInterface dao)
        {
        final String SOURCE = "DatasetManager.importDataset() ";
        final boolean boolSuccess;

        // Do not use isValidManager() because this would fail when empty!
        if ((!isFull())
            && (hostinstrument != null)
            && (datasettype != null)
            && (dao != null))
            {
            final String strName;

            // Is this the first Import?
            if (!hasSelection())
                {
                // Ensure we remove all previous references if so
                initialise();
                }

            // Manage: DatasetType, DAO, Name, DatasetState, Offset

            setDatasetType(datasettype);

            getSecondaryDAOs().add(dao);

            // This name will appear on the selection drop-down,
            // so try to make it meaningful
            strName = SuperposedDataAnalyserHelper.buildDatasetName(hostinstrument, dao);
            getSecondaryDatasetNames().add(strName);

            // The new dataset is not yet Attached
            getSecondaryStates().add(DatasetState.DETACHED);

            // Always start with an offset of zero
            getSecondaryOffsets().add(DEFAULT_OFFSET);

            // Update the DAO selection index to this imported dataset
            setSelectedIndex(getSecondaryDAOs().size() - 1);

            boolSuccess = true;
            }
        else
            {
            LOGGER.error(SOURCE + "The DatasetManager cannot hold any more DAOs");

            boolSuccess = false;
            }

        return (boolSuccess);
        }


    /***********************************************************************************************
     * Attach the currently selected Dataset to the Composite.
     * The first DAO to be attached is marked as the Primary, with a different icon.
     *
     * @return boolean
     */

    public synchronized boolean attach()
        {
        final String SOURCE = "DatasetManager.attach() ";
        boolean boolSuccess;

        boolSuccess = false;

        if ((isValidManager())
            && (canAttach()))
            {
            final DatasetState datasetState;

            // The first DAO to be attached is marked as the Primary
            if (getAttachedDAOs().isEmpty())
                {
                datasetState =  DatasetState.LOCKED;

                // The Offset of the Primary is always forced to be zero
                getSecondaryOffsets().set(getSelectedIndex(), DEFAULT_OFFSET);
                }
            else
                {
                datasetState =  DatasetState.ATTACHED;
                }

            getSecondaryStates().set(getSelectedIndex(), datasetState);

            if ((getAttachedDAOs() != null)
                && (getSecondaryDAOs() != null))
                {
                final ObservatoryInstrumentDAOInterface daoSource;

                daoSource = getSecondaryDAOs().get(getSelectedIndex());
                boolSuccess = getAttachedDAOs().add(daoSource);
                }
            }
        else
            {
            LOGGER.error(SOURCE + "Unable to Attach [state=" + getSelectedState().getName() + "]");
            }

        return (boolSuccess);
        }


    /***********************************************************************************************
     * Detach the currently selected Dataset from the Composite.
     *
     * @return boolean
     */

    public synchronized boolean detach()
        {
        final String SOURCE = "DatasetManager.detach() ";
        boolean boolSuccess;

        boolSuccess = false;

        if ((isValidManager())
            && (canDetach()))
            {
            // Update the DatasetState of the Selected DAO
            getSecondaryStates().set(getSelectedIndex(), DatasetState.DETACHED);

            // Detach the DAO from the Composite
            boolSuccess = getAttachedDAOs().remove(getSelectedDAO());
            }
        else
            {
            LOGGER.error(SOURCE + "Unable to Detach [state=" + getSelectedState().getName() + "]");
            }

        return (boolSuccess);
        }


    /***********************************************************************************************
     * Detach all of the Datasets from the Composite, including the Primary.
     * Do not change the Offsets or the current selected Index.
     *
     * @return boolean
     */

    public synchronized boolean detachAll()
        {
        final String SOURCE = "DatasetManager.detachAll() ";
        boolean boolSuccess;

        boolSuccess = false;

        // Traverse the List of States, mark as DETACHED
        // Do not change the current selected index
        if ((isValidManager())
            && (canDetachAll()))
            {
            // Mark all entries as DETACHED regardless
            for (int intStateIndex = 0;
               intStateIndex < size();
               intStateIndex++)
               {
               getSecondaryStates().set(intStateIndex, DatasetState.DETACHED);
               }

            // Also remove all DAOs from the Composite
            getAttachedDAOs().clear();

            boolSuccess = true;
            }
        else
            {
            LOGGER.error(SOURCE + "Unable to Detach [state=" + getSelectedState().getName() + "]");
            }

        return (boolSuccess);
        }


    /***********************************************************************************************
     * Remove the currently selected Dataset completely.
     *
     * @return boolean
     */

    public synchronized boolean remove()
        {
        final String SOURCE = "DatasetManager.remove() ";
        boolean boolSuccess;

        boolSuccess = false;

        if ((isValidManager())
            && (hasSelection()))
            {
            final ObservatoryInstrumentDAOInterface daoRemoved;

            daoRemoved = getSecondaryDAOs().get(getSelectedIndex());

            // Help the GarbageCollector?
            switch(getDatasetType())
                {
                case TIMESTAMPED:
                    {
                    ((TimeSeriesCollection)daoRemoved.getXYDataset()).removeAllSeries();
                    break;
                    }

                case XY:
                    {
                    ((XYSeriesCollection)daoRemoved.getXYDataset()).removeAllSeries();
                    break;
                    }

                default:
                    {
                    // But this won't remove the previous chart?
                    daoRemoved.setXYDataset(null);

                    LOGGER.error(SOURCE + "Invalid DatasetType [xydatset=null]");
                    }
                }

            daoRemoved.getXYDatasetMetadata().clear();

            // Remember to dispose each DAO as it is removed
            daoRemoved.disposeDAO();

            // Manage: DatasetType, DAO, Name, DatasetState, Offset

            getSecondaryDAOs().remove(getSelectedIndex());
            getSecondaryDatasetNames().remove(getSelectedIndex());
            getSecondaryStates().remove(getSelectedIndex());
            getSecondaryOffsets().remove(getSelectedIndex());

            // We need to remove a DAO which may or may not be Attached!
            // So use the object reference instead
            getAttachedDAOs().remove(daoRemoved);

            // Did we delete the only item?
            if (getSecondaryDAOs().size() == 0)
                {
                // If there are no datasets left, then we cannot have a valid DatasetType...
                setDatasetType(null);

                // Always return to a known state
                setSelectedIndex(NO_SELECTION_INDEX);
                }
            else
                {
                // Return to select the first item each time
                setSelectedIndex(0);
                }

            boolSuccess = true;
            }
        else
            {
            LOGGER.error(SOURCE + "Unable to Remove [state=" + getSelectedState().getName() + "]");
            }

        return (boolSuccess);
        }


    /**********************************************************************************************/
    /* DatasetManager Utilities                                                                   */
    /***********************************************************************************************
     * Get the DatasetType of all datasets, defined by the first Import.
     * NULL is returned before the first Import, which sets the type for all subsequent imports.
     *
     * @return DatasetType
     */

    public DatasetType getDatasetType()
        {
        return (this.datasetType);
        }


    /***********************************************************************************************
     * Set the DatasetType of all datasets, defined by the first Import.
     *
     * @param type
     */

    public void setDatasetType(final DatasetType type)
        {
        this.datasetType = type;
        }


    /***********************************************************************************************
     * Get the List of Secondary DAOs.
     *
     * @return List<ObservatoryInstrumentDAOInterface>
     */

    public List<ObservatoryInstrumentDAOInterface> getSecondaryDAOs()
        {
        return (this.listSecondaryDAOs);
        }


    /***********************************************************************************************
     * Get the List of Secondary Dataset Names.
     *
     * @return List<String>
     */

    public List<String> getSecondaryDatasetNames()
        {
        return (this.listSecondaryDatasetNames);
        }


    /***********************************************************************************************
     * Get the List of Secondary DatasetStates.
     *
     * @return List<DatasetState>
     */

    public List<DatasetState> getSecondaryStates()
        {
        return (this.listSecondaryStates);
        }


    /***********************************************************************************************
     * Get the List of Secondary Offsets.
     *
     * @return List<Double>
     */

    public List<Double> getSecondaryOffsets()
        {
        return (this.listSecondaryOffsets);
        }


    /***********************************************************************************************
     * Get the size of the DatasetManager. i.e. how many DAOs are available.
     *
     * @return int
     */

    public int size()
        {
        return (getSecondaryDAOs().size());
        }


    /***********************************************************************************************
     * Get the List of DAOs which are currently attached to the Composite.
     *
     * @return List<ObservatoryInstrumentDAOInterface>
     */

    public List<ObservatoryInstrumentDAOInterface> getAttachedDAOs()
        {
        return (this.listAttachedDAOs);
        }


    /***********************************************************************************************
     * Given a DAO, get the corresponding DatasetIndex.
     * This is to allow working backwards from an AttachedDAO to the index on the DatasetSelector.
     * Return -1 if the DAO is not found in the collection.
     *
     * @param dao
     */

    public int getDatasetIndex(final ObservatoryInstrumentDAOInterface dao)
        {
        int intDatasetIndex;

        try
            {
            intDatasetIndex = getSecondaryDAOs().indexOf(dao);
            }

        catch (final ClassCastException exception)
            {
            intDatasetIndex = NO_SELECTION_INDEX;
            }

        catch (final NullPointerException exception)
            {
            intDatasetIndex = NO_SELECTION_INDEX;
            }

        return (intDatasetIndex);
        }


    /***********************************************************************************************
     * Indicate if the DatasetManager is set up correctly.
     *
     * @return boolean
     */

    public boolean isValidManager()
        {
        final boolean boolValid;

        boolValid = ((getSecondaryDAOs() != null)
                     && (!getSecondaryDAOs().isEmpty())
                     && (getSecondaryDatasetNames() != null)
                     && (!getSecondaryDatasetNames().isEmpty())
                     && (getSecondaryStates() != null)
                     && (!getSecondaryStates().isEmpty())
                     && (getSecondaryOffsets() != null)
                     && (!getSecondaryOffsets().isEmpty())
                     && (getSecondaryDAOs().size() == getSecondaryOffsets().size())
                     && (getSecondaryDatasetNames().size() == getSecondaryOffsets().size())
                     && (getSecondaryStates().size() == getSecondaryOffsets().size())
                     && (getAttachedDAOs() != null));

        return (boolValid);

        }


    /***********************************************************************************************
     * Debug the DatasetManager.
     *
     * @param debug
     * @param message
     */

    public void debugManager(final boolean debug,
                             final String message)
        {
        LOGGER.debug(debug,
                     message + "[selection.index=" + getSelectedIndex()
                         + "] [size.manager" + size()
                         + "] [size.secondarydaos" + getSecondaryDAOs().size()
                         + "] [size.secondarynames" + getSecondaryDatasetNames().size()
                         + "] [size.secondarystates" + getSecondaryStates().size()
                         + "] [size.secondaryoffsets" + getSecondaryOffsets().size()
                         + "] [size.attacheddaos" + getAttachedDAOs().size()
                         + "]");
        }
    }
