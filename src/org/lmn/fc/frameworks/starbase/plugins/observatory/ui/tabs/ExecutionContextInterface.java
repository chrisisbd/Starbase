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


import org.apache.xmlbeans.XmlObject;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.model.xmlbeans.instruments.CommandType;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;
import org.lmn.fc.model.xmlbeans.instruments.MacroType;
import org.lmn.fc.model.xmlbeans.instruments.ParameterType;
import org.lmn.fc.ui.panels.HTMLPanel;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.util.List;


/***************************************************************************************************
 * ExecutionContextInterface.
 */

public interface ExecutionContextInterface
    {
    /**********************************************************************************************/
    /* Instrument.Module.Command for the Import                                                   */
    /***********************************************************************************************
     * Get the ObservatoryInstrument.
     *
     * @return ObservatoryInstrumentInterface
     */

    ObservatoryInstrumentInterface getObservatoryInstrument();


    /***********************************************************************************************
     * Set the ObservatoryInstrument.
     *
     * @param obsinstrument
     */

    void setObservatoryInstrument(ObservatoryInstrumentInterface obsinstrument);


    /***********************************************************************************************
     * Get the DAO to use for Command execution.
     *
     * @return ObservatoryInstrumentDAOInterface
     */

    ObservatoryInstrumentDAOInterface getExecutionDAO();


    /***********************************************************************************************
     * Set the DAO to use for Command execution.
     *
     * @param dao
     */

    void setExecutionDAO(ObservatoryInstrumentDAOInterface dao);


    /***********************************************************************************************
     * Get the Instrument to execute.
     *
     * @return Instrument
     */

    Instrument getStarscriptInstrument();


    /***********************************************************************************************
     * Set the Instrument to execute.
     *
     * @param instrument
     */

    void setStarscriptInstrument(Instrument instrument);


    /***********************************************************************************************
     * Get the Module to execute.
     *
     * @return XmlObject
     */

    XmlObject getStarscriptModule();


    /***********************************************************************************************
     * Set the Module to execute.
     *
     * @param module
     */

    void setStarscriptModule(XmlObject module);


    /***********************************************************************************************
     * Get the Macro to execute.
     *
     * @return MacroType
     */

    MacroType getStarscriptMacro();


    /***********************************************************************************************
     * Set the Macro to execute.
     *
     * @param macro
     */

    void setStarscriptMacro(MacroType macro);


    /***********************************************************************************************
     * Get the Command to execute.
     *
     * @return CommandType
     */

    CommandType getStarscriptCommand();


    /***********************************************************************************************
     * Set the Command to execute.
     *
     * @param command
     */

    void setStarscriptCommand(CommandType command);


    /***********************************************************************************************
     * Get the List of Parameters to execute.
     *
     * @return List<ParameterType>
     */

    List<ParameterType> getStarscriptExecutionParameters();


    /***********************************************************************************************
     * Indicate if there is a Macro or Command selected to be executed.
     *
     * @return boolean
     */

    boolean isSelectedMacroOrCommand();


    /***********************************************************************************************
     * Indicate if there is a Macro or Command selected to be executed.
     *
     * @param macroorcommand
     */

    void setSelectedMacroOrCommand(boolean macroorcommand);


    /**********************************************************************************************/
    /* User Interface                                                                             */
    /***********************************************************************************************
     * Get the Starscript Indicator.
     *
     * @return JTextArea
     */

    JTextComponent getStarscriptIndicator();


    /***********************************************************************************************
     * Set the Starscript Indicator.
     *
     * @param indicator
     */

    void setStarscriptIndicator(JTextComponent indicator);


    /***********************************************************************************************
     * Get the Execute button.
     *
     * @return AbstractButton
     */

    AbstractButton getExecuteButton();


    /***********************************************************************************************
     * Set the Execute button.
     *
     * @param button
     */

    void setExecuteButton(AbstractButton button);


    /***********************************************************************************************
     * Get the Repeat button.
     *
     * @return AbstractButton
     */

    AbstractButton getRepeatButton();


    /***********************************************************************************************
     * Set the Repeat button.
     *
     * @param button
     */

    void setRepeatButton(AbstractButton button);


    /***********************************************************************************************
     * Get the Abort button.
     *
     * @return AbstractButton
     */

    AbstractButton getAbortButton();


    /***********************************************************************************************
     * Set the Repeat button.
     *
     * @param button
     */

    void setAbortButton(AbstractButton button);


    /***********************************************************************************************
     * Get the Parameter HelpViewer UIComponent.
     *
     * @return HTMLPanel
     */

    HTMLPanel getParameterHelpViewer();


    /***********************************************************************************************
     * Reset the ExecutionContext for the Command, but not the User Interface elements.
     */

    void resetCommandContext();


    /***********************************************************************************************
     * Reset the ExecutionContext for the User Interface, but not the Command elements.
     */

    void resetUIContext();
    }
