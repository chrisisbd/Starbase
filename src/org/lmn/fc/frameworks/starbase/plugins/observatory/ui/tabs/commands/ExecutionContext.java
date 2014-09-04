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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.commands;


import org.apache.xmlbeans.XmlObject;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.ExecutionContextInterface;
import org.lmn.fc.model.xmlbeans.instruments.CommandType;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;
import org.lmn.fc.model.xmlbeans.instruments.MacroType;
import org.lmn.fc.model.xmlbeans.instruments.ParameterType;
import org.lmn.fc.ui.panels.HTMLPanel;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.util.ArrayList;
import java.util.List;


/***************************************************************************************************
 * ExecutionContext.
 */

public final class ExecutionContext implements ExecutionContextInterface
    {
    // Instrument and Command state
    private ObservatoryInstrumentInterface observatoryInstrument;
    private ObservatoryInstrumentDAOInterface daoExecute;
    private Instrument instrumentExecute;
    private XmlObject moduleExecute;
    private MacroType macroExecute;
    private CommandType commandExecute;
    private final List<ParameterType> listExecutionParameters;
    private boolean boolMacroOrCommand;

    // User Interface relevant to Command execution
    private JTextComponent textStarscript;
    private AbstractButton buttonExecute;
    private AbstractButton buttonRepeat;
    private AbstractButton buttonAbort;
    private HTMLPanel helpViewerParameter;


    /***********************************************************************************************
     * Indicate if the specified ExecutionContext is executable.
     *
     * @param context
     *
     * @return boolean
     */

    public static boolean isExecutable(final ExecutionContextInterface context)
        {
        final boolean boolExecutableDAO;
        final boolean boolExecutableLocal;
        final boolean boolExecutableRemote;
        final boolean boolExecutablePort;
        final boolean boolExecutable;

        // We must have a DAO, otherwise we can't execute a Command!
        boolExecutableDAO = (context.getExecutionDAO() != null);

        // We can execute a Local (i.e. not SendToPort) Command at any time
        boolExecutableLocal = ((context.getStarscriptCommand() != null)
                               && (!context.getStarscriptCommand().getSendToPort()));

        // Allow SendToPort Commands and all Macros to be executed remotely
        boolExecutableRemote = (((context.getStarscriptCommand() != null)
                                 && (context.getStarscriptCommand().getSendToPort()))
                                || ((context.getStarscriptMacro() != null)));

        // Allow SendToPort Commands and all Macros if the Port is not busy, or if there is no Port
        boolExecutablePort = ((context.getExecutionDAO().getPort() == null)
                              || ((context.getExecutionDAO().getPort() != null)
                                  && (!context.getExecutionDAO().getPort().isPortBusy())));

        // One or the other might be possible
        boolExecutable = boolExecutableDAO
                            && (boolExecutableLocal || (boolExecutableRemote && boolExecutablePort));

        return (boolExecutable);
        }


    /***********************************************************************************************
     * Construct an ExecutionContext.
     */

    public ExecutionContext()
        {
        // The Instrument.Module.Command to execute
        this.observatoryInstrument = null;
        this.daoExecute = null;
        this.instrumentExecute = null;
        this.moduleExecute = null;
        this.macroExecute = null;
        this.commandExecute = null;
        this.listExecutionParameters = new ArrayList<ParameterType>(10);
        this.boolMacroOrCommand = false;

        // UI
        this.textStarscript = null;
        this.buttonExecute = null;
        this.buttonRepeat = null;
        this.buttonAbort = null;
        this.helpViewerParameter = null;
        }


    /**********************************************************************************************/
    /* Instrument.Module.Command to Execute                                                       */
    /***********************************************************************************************
     * Get the ObservatoryInstrument.
     *
     * @return ObservatoryInstrumentInterface
     */

    public ObservatoryInstrumentInterface getObservatoryInstrument()
        {
        return (this.observatoryInstrument);
        }


    /***********************************************************************************************
     * Set the ObservatoryInstrument.
     *
     * @param obsinstrument
     */

    public void setObservatoryInstrument(final ObservatoryInstrumentInterface obsinstrument)
        {
        this.observatoryInstrument = obsinstrument;
        }


    /***********************************************************************************************
     * Get the DAO to use for Command execution.
     *
     * @return ObservatoryInstrumentDAOInterface
     */

    public ObservatoryInstrumentDAOInterface getExecutionDAO()
        {
        return (this.daoExecute);
        }


    /***********************************************************************************************
     * Set the DAO to use for Command execution.
     *
     * @param dao
     */

    public void setExecutionDAO(final ObservatoryInstrumentDAOInterface dao)
        {
        this.daoExecute = dao;
        }


    /***********************************************************************************************
     * Get the Instrument to execute.
     *
     * @return Instrument
     */

    public Instrument getStarscriptInstrument()
        {
        return (this.instrumentExecute);
        }


    /***********************************************************************************************
     * Set the Instrument to execute.
     *
     * @param instrument
     */

    public void setStarscriptInstrument(final Instrument instrument)
        {
        this.instrumentExecute = instrument;
        }


    /***********************************************************************************************
     * Get the Module to execute.
     *
     * @return XmlObject
     */

    public XmlObject getStarscriptModule()
        {
        return (this.moduleExecute);
        }


    /***********************************************************************************************
     * Set the Module to execute.
     *
     * @param module
     */

    public void setStarscriptModule(final XmlObject module)
        {
        this.moduleExecute = module;
        }


    /***********************************************************************************************
     * Get the Macro to execute.
     *
     * @return MacroType
     */

    public MacroType getStarscriptMacro()
        {
        return (this.macroExecute);
        }


    /***********************************************************************************************
     * Set the Macro to execute.
     *
     * @param macro
     */

    public void setStarscriptMacro(final MacroType macro)
        {
        this.macroExecute = macro;
        }


    /***********************************************************************************************
     * Get the Command to execute.
     *
     * @return CommandType
     */

    public CommandType getStarscriptCommand()
        {
        return (this.commandExecute);
        }


    /***********************************************************************************************
     * Set the Command to execute.
     *
     * @param command
     */

    public void setStarscriptCommand(final CommandType command)
        {
        this.commandExecute = command;
        }


    /***********************************************************************************************
     * Get the List of Parameters to execute.
     *
     * @return List<ParameterType>
     */

    public List<ParameterType> getStarscriptExecutionParameters()
        {
        return (this.listExecutionParameters);
        }


    /***********************************************************************************************
     * Indicate if there is a Macro or Command selected to be executed.
     *
     * @return boolean
     */

    public boolean isSelectedMacroOrCommand()
        {
        return (this.boolMacroOrCommand);
        }


    /***********************************************************************************************
     * Indicate if there is a Macro or Command selected to be executed.
     *
     * @param macroorcommand
     */

    public void setSelectedMacroOrCommand(final boolean macroorcommand)
        {
        this.boolMacroOrCommand = macroorcommand;
        }


    /**********************************************************************************************/
    /* User Interface                                                                             */
    /***********************************************************************************************
     * Get the Starscript Indicator.
     *
     * @return JTextComponent
     */

    public JTextComponent getStarscriptIndicator()
        {
        return (this.textStarscript);
        }


    /***********************************************************************************************
     * Set the Starscript Indicator.
     *
     * @param indicator
     */

    public void setStarscriptIndicator(final JTextComponent indicator)
        {
        this.textStarscript = indicator;
        }


    /***********************************************************************************************
     * Get the Execute button.
     *
     * @return AbstractButton
     */

    public AbstractButton getExecuteButton()
        {
        return (this.buttonExecute);
        }


    /***********************************************************************************************
     * Set the Execute button.
     *
     * @param button
     */

    public void setExecuteButton(final AbstractButton button)
        {
        this.buttonExecute = button;
        }


    /***********************************************************************************************
     * Get the Repeat button.
     *
     * @return AbstractButton
     */

    public AbstractButton getRepeatButton()
        {
        return (this.buttonRepeat);
        }


    /***********************************************************************************************
     * Set the Repeat button.
     *
     * @param button
     */

    public void setRepeatButton(final AbstractButton button)
        {
        this.buttonRepeat = button;
        }


    /***********************************************************************************************
     * Get the Abort button.
     *
     * @return AbstractButton
     */

    public AbstractButton getAbortButton()
        {
        return (this.buttonAbort);
        }


    /***********************************************************************************************
     * Set the Abort button.
     *
     * @param button
     */

    public void setAbortButton(final AbstractButton button)
        {
        this.buttonAbort = button;
        }


    /***********************************************************************************************
     * Get the Parameter HelpViewer UIComponent.
     *
     * @return HTMLPanel
     */

    public HTMLPanel getParameterHelpViewer()
        {
        return (this.helpViewerParameter);
        }


    /***********************************************************************************************
     * Reset the ExecutionContext for the Command, but not the User Interface elements.
     */

    public void resetCommandContext()
        {
        setObservatoryInstrument(null);
        setExecutionDAO(null);
        setStarscriptInstrument(null);
        setStarscriptModule(null);
        setStarscriptMacro(null);
        setStarscriptCommand(null);
        getStarscriptExecutionParameters().clear();
        setSelectedMacroOrCommand(false);
        }


    /***********************************************************************************************
     * Reset the ExecutionContext for the User Interface, but not the Command elements.
     */

    public void resetUIContext()
        {
        setStarscriptIndicator(null);
        setExecuteButton(null);
        setRepeatButton(null);
        setAbortButton(null);
        }
    }
