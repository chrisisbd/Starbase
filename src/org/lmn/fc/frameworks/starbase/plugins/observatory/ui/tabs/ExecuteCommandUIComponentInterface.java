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

import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.ui.UIComponentPlugin;

import javax.swing.*;
import java.awt.*;


/***************************************************************************************************
 * ExecuteCommandUIComponentInterface.
 */

public interface ExecuteCommandUIComponentInterface extends UIComponentPlugin
    {
    // String Resources
    String BUTTON_EXECUTE = "Execute";
    String BUTTON_ABORT = "Abort";

    Dimension DIM_BUTTON_TOOLBAR_EXECUTE = new Dimension(60, 24);
    Dimension DIM_BUTTON_TOOLBAR_ABORT = new Dimension(60, 24);


    /**********************************************************************************************/
    /* UI State                                                                                   */
    /***********************************************************************************************
     * Get the JToolBar.
     *
     * @return JToolBar
     */

    JToolBar getToolBar();


    /***********************************************************************************************
     * Get the Activity Indicator.
     *
     * @return ActivityIndicatorUIComponentInterface
     */

    ActivityIndicatorUIComponentInterface getActivityIndicator();


    /***********************************************************************************************
     * Set the Activity Indicator.
     *
     * @param activityindicator
     */

    void setActivityIndicator(ActivityIndicatorUIComponentInterface activityindicator);


    /***********************************************************************************************
     * Get the Execute Button.
     *
     * @return JButton
     */

    JButton getExecuteButton();


    /***********************************************************************************************
     * Set the Execute Button.
     *
     * @param button
     */

    void setExecuteButton(JButton button);


    /***********************************************************************************************
     * Get the Abort Button.
     *
     * @return JButton
     */

    JButton getAbortButton();


    /***********************************************************************************************
     * Set the Abort Button.
     *
     * @param button
     */

    void setAbortButton(JButton button);


    /***********************************************************************************************
     * Get the Viewer Button.
     *
     * @return JButton
     */

    JButton getViewerButton();


    /***********************************************************************************************
     * Set the Viewer Button.
     *
     * @param button
     */

    void setViewerButton(JButton button);


    /***********************************************************************************************
     * Get the Indicator which shows the current Command segment.
     *
     * @return XmlObject
     */

    JTextArea getStarscriptIndicator();


    /***********************************************************************************************
     * Set the Indicator which shows the current Command segment.
     *
     * @param indicator
     */

    void setStarscriptIndicator(JTextArea indicator);


    /***********************************************************************************************
     * Get the ExecutionContext.
     *
     * @return ExecutionContextInterface
     */

    ExecutionContextInterface getExecutionContext();


    /**********************************************************************************************/
    /* Injections                                                                                 */
    /***********************************************************************************************
     * Get the host ObservatoryInstrument.
     *
     * @return ObservatoryInstrumentInterface
     */

    ObservatoryInstrumentInterface getObservatoryInstrument();


    /***********************************************************************************************
     * Get the Instrument Identifier for Command Execution.
     *
     * @return String
     */

    String getInstrumentIdentifier();


    /***********************************************************************************************
     * Get the Module Identifier for Command Execution.
     *
     * @return String
     */

    String getModuleIdentifier();


    /***********************************************************************************************
     * Get the Command Identifier for Command Execution.
     *
     * @return String
     */

    String getCommandIdentifier();


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
