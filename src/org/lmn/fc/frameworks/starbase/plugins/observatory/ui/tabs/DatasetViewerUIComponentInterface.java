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

import org.lmn.fc.common.utilities.misc.Semaphore;
import org.lmn.fc.frameworks.starbase.plugins.observatory.events.CommitChangeListener;
import org.lmn.fc.frameworks.starbase.plugins.observatory.events.MetadataChangedListener;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.DAOWrapperInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.ui.UIComponentPlugin;
import org.lmn.fc.ui.widgets.ControlKnobInterface;
import org.lmn.fc.ui.widgets.IndicatorInterface;

import javax.swing.*;
import java.awt.*;


/***************************************************************************************************
 * DatasetViewerUIComponentInterface.
 */

public interface DatasetViewerUIComponentInterface extends CommitChangeListener,
                                                           MetadataChangedListener,
                                                           UIComponentPlugin
    {
    // String Resources
    String MSG_DATASET_VIEWER = "Dataset Viewer";

    String TOOLTIP_OFFSET = "The offset between selected dataset and the Composite";
    String TOOLTIP_UNITS = "The Units of the offset (time or samples)";
    String TOOLTIP_OFFSET_CONTROL_COARSE = "Rotate to vary the percentage offset between the selected dataset and the composite (ctrl-click to reset)";
    String TOOLTIP_OFFSET_CONTROL_FINE = "Rotate to fine tune the percentage offset between the dataset and the composite (ctrl-click to reset)";
    String TOOLTIP_ATTACH = "Attach selected Dataset to Composite";
    String TOOLTIP_DETACH = "Detach selected Dataset from Composite";
    String TOOLTIP_CHART = "Show the Chart of the Dataset";
    String TOOLTIP_METADATA = "Show the Metadata for the Dataset";
    String TOOLTIP_REMOVE = "Remove selected Dataset";
    String TOOLTIP_DATASET_TYPE = "The DatasetType of all imported datasets";
    String TOOLTIP_DATASET_IMPORTER = "Import a new Dataset";

    Dimension DIM_OFFSET_INDICATOR = new Dimension(70, 24);
    Dimension DIM_OFFSET_CONTROL_KNOB = new Dimension(28, 28);

    int OFFSET_CONTROL_TICKLENGTH = 2;


    /***********************************************************************************************
     * Set the data from the DAO finished() method.
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
     * Get the JComboBox used for secondary dataset selection.
     *
     * @return JComboBox
     */

    JComboBox getDatasetSelector();


    /***********************************************************************************************
     * Set the JComboBox used for dataset selection.
     *
     * @param selector
     */

    void setDatasetSelector(JComboBox  selector);


    /***********************************************************************************************
     * Get the Semaphore used to control e.g. the ActionListener on the DatasetSelector.
     *
     * @return Semaphore
     */

    Semaphore getDatasetSemaphore();


    /***********************************************************************************************
     * Get the Offset Indicator.
     *
     * @return IndicatorInterface
     */

    IndicatorInterface getOffsetIndicator();


    /***********************************************************************************************
     * Set the Offset Indicator.
     *
     * @param indicator
     */

    void setOffsetIndicator(IndicatorInterface indicator);


    /***********************************************************************************************
     * Get the Offset Control.
     *
     * @return ControlKnobInterface
     */

    ControlKnobInterface getOffsetControl();


    /***********************************************************************************************
     * Set the Offset Control.
     *
     * @param knob
     */

    void setOffsetControl(ControlKnobInterface knob);


    /***********************************************************************************************
     * Get the Attach button.
     *
     * @return JButton
     */

    JButton getAttachButton();


    /***********************************************************************************************
     * Set the Attach button.
     *
     * @param button
     */

    void setAttachButton(JButton button);


    /***********************************************************************************************
     * Get the Detach button.
     *
     * @return JButton
     */

     JButton getDetachButton();


    /***********************************************************************************************
     * Set the Detach button.
     *
     * @param button
     */

    void setDetachButton(JButton button);


    /***********************************************************************************************
     * Get the Metadata button.
     *
     * @return JButton
     */

    JButton getMetadataButton();


    /***********************************************************************************************
     * Set the Metadata button.
     *
     * @param button
     */

    void setMetadataButton(JButton button);


    /***********************************************************************************************
     * Get the Remove button.
     *
     * @return JButton
     */

    JButton getRemoveButton();


    /***********************************************************************************************
     * Set the Remove button.
     *
     * @param button
     */

    void setRemoveButton(JButton button);


    /***********************************************************************************************
     * Get the Import button.
     *
     * @return JButton
     */

    JButton getImportButton();


    /***********************************************************************************************
     * Set the Import button.
     *
     * @param button
     */

    void setImportButton(JButton button);


    /***********************************************************************************************
     * Get the JLabel which indicates the chosen DatasetType.
     *
     * @return JLabel
     */

    JLabel getDatasetTypeLabel();


    /***********************************************************************************************
     * Set the JLabel which indicates the chosen DatasetType.
     *
     * @param label
     */

    void setDatasetTypeLabel(JLabel label);


    /***********************************************************************************************
     * Get the Viewer UI container, which appears at the bottom of the UI.
     *
     * @return UIComponentPlugin
     */

    UIComponentPlugin getViewerContainer();


    /***********************************************************************************************
     * Get the Chart Viewer, which appears at the bottom of the UI.
     *
     * @return ChartUIComponentPlugin
     */

    ChartUIComponentPlugin getChartViewer();


    /***********************************************************************************************
     * Get the Metadata Viewer, which appears at the bottom of the UI.
     *
     * @return UIComponentPlugin
     */

    UIComponentPlugin getMetadataViewer();


    /***********************************************************************************************
     * Indicate if all of the Viewer UI Components are not NULL.
     *
     * @return boolean
     */

    boolean isValidViewerUI();


    /**********************************************************************************************/
    /* Injections                                                                                 */
    /***********************************************************************************************
     * Get the host ObservatoryInstrument.
     *
     * @return ObservatoryInstrumentInterface
     */

    ObservatoryInstrumentInterface getObservatoryInstrument();


    /***********************************************************************************************
     * Get the host SuperposedDataAnalyser UIComponent.
     *
     * @return SuperposedDataAnalyserUIComponentInterface
     */

    SuperposedDataAnalyserUIComponentInterface getSdaUI();


    /***********************************************************************************************
     * Get the FontData.
     *
     * @return FontPlugin
     */

    FontInterface getFontData();


    /***********************************************************************************************
     * Get the ColourData.
     *
     * @return ColourPlugin
     */

    ColourInterface getColourData();
    }
