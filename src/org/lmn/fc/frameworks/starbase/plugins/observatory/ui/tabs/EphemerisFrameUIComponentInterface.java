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

import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.EpochConsumerInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.DAOWrapperInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.InstrumentUIComponentDecoratorInterface;
import org.lmn.fc.model.xmlbeans.ephemerides.Ephemeris;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;

import javax.swing.*;
import java.util.List;


/***************************************************************************************************
 * EphemerisFrameUIComponentInterface.
 */

public interface EphemerisFrameUIComponentInterface extends InstrumentUIComponentDecoratorInterface,
                                                            EpochConsumerInterface
    {
    // String Resources
    String ACTION_RECALCULATE = "Recalculate the Ephemeris";
    String ACTION_RESET = "Reset JD to 0h UT today";
    String DIALOG_PRINT = "Print the Ephemeris";
    String FILENAME_ICON_EPHEMERIS_RECALCULATE = "ephemeris-recalc.png";
    String FILENAME_ICON_EPHEMERIS_RESET = "ephemeris-reset.png";
    String LABEL_EPOCH = "Epoch";
    String LABEL_INTERVAL_SECONDS = "Interval (sec)";
    String LABEL_JULIAN_DATE = "Julian Date";
    String LABEL_TARGET = "Ephemeris";
    String MSG_EPHEMERIS = "the Ephemeris";
    String MSG_EPHEMERIS_PRINTED = "The Ephemeris has been printed";
    String MSG_UNKNOWN = "Unknown";
    String TITLE_EPHEMERIS = "Ephemeris";
    String TOOLTIP_ENTER_JD_START = "Enter the starting Julian Date";
    String TOOLTIP_EPOCH = "Choose the Epoch";
    String TOOLTIP_INTERVAL = "Enter the interval size in seconds (1-20000)";
    String TOOLTIP_TARGET = "Choose an object for the Ephemeris";

    int INTERVAL_DEFAULT = 300;
    int INTERVAL_MAX = 20000;
    int INTERVAL_MIN = 1;
    int LENGTH_INTERVAL = 8;
    int LENGTH_JULIAN_DATE = 14;
    int WIDTH_INTERVAL = 50;
    int WIDTH_JD = 100;
    int WIDTH_TARGET = 150;


    /***********************************************************************************************
     * Get the Ephemeris UI.
     * This is public to allow exportEphemeris().
     *
     * @return EphemerisUIComponentInterface
     */

    EphemerisUIComponentInterface getEphemerisUI();


    /***********************************************************************************************
     * Set the Ephemeris on which this report is based, given its Name.
     *
     * @param ephemerisname
     */

    void setEphemerisFromName(String ephemerisname);


    /***********************************************************************************************
     * Get the Ephemeris on which this report is based.
     *
     * @return Ephemeris
     */

    Ephemeris getEphemeris();


    /***********************************************************************************************
     * Set the data from the DAO finished() method, or from any Command doing a realtime update.
     *
     * @param daowrapper
     * @param updatemetadata
     */

    void setWrappedData(DAOWrapperInterface daowrapper,
                        boolean updatemetadata);


    /***********************************************************************************************
     * Get the Aggregate Metadata.
     *
     * @return List<Metadata>
     */

    List<Metadata> getMetadata();


    /***********************************************************************************************
     * Get the Ephemeris JToolBar.
     *
     * @return JToolBar
     */

    JToolBar getToolBar();


    /***********************************************************************************************
     * Get the Ephemeris Julian Date entry box.
     *
     * @return JTextField
     */

    JTextField getJDText();


    /***********************************************************************************************
     * Get the Ephemeris Epoch combo box.
     *
     * @return JComboBox
     */

    JComboBox getEpochCombo();


    /***********************************************************************************************
     * Get the Step seconds entry box.
     *
     * @return JTextField
     */

    JTextField getIntervalText();


    /***********************************************************************************************
     * Get the JButton used to reset the JulianDate.
     *
     * @return JButton
     */

    JButton getResetButton();


    /***********************************************************************************************
     * Get the JButton used to recalculate the Ephemeris.
     *
     * @return JButton
     */

    JButton getRecalculateButton();


    /***********************************************************************************************
     * Get the Ephemeris Target combo box.
     *
     * @return JComboBox
     */

    JComboBox getTargetCombo();


    /***********************************************************************************************
     * Get the PageSetup button.
     *
     * @return JButton
     */

    JButton getPageSetupButton();


    /***********************************************************************************************
     * Set the PageSetup button.
     *
     * @param button
     */

    void setPageSetupButton(JButton button);


    /***********************************************************************************************
     * Get the Print button.
     *
     * @return JButton
     */

    JButton getPrintButton();


    /***********************************************************************************************
     * Set the Print button.
     *
     * @param button
     */

    void setPrintButton(JButton button);


    /***********************************************************************************************
     * Indicate if Events are currently enabled, i.e. for listeners on Toolbar controls.
     *
     * @return boolean
     */

    boolean areEventsEnabled();
    }
