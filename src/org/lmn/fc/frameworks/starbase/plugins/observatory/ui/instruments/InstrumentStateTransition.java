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

import org.lmn.fc.frameworks.starbase.portcontroller.CommandProcessorContextInterface;


/***********************************************************************************************
 * InstrumentStateTransition.
 * See: http://en.wikipedia.org/wiki/Finite-state_machine.
 */

public class InstrumentStateTransition
    {
    private final InstrumentState currentState;
    private final InstrumentState nextState;

    private final boolean boolEnableCommandStatus;
    private final boolean boolEnableExecute;
    private final boolean boolEnableRepeat;
    private final boolean boolEnableAbort;

    private final boolean boolEnableMacroStatus;
    private final boolean boolEnableRecord;
    private final boolean boolEnableDelete;
    private final boolean boolEnableEdit;
    private final boolean boolEnableLoad;
    private final boolean boolEnableSave;
    private final boolean boolEnableShow;

    private final String strCommandStatus;
    private final String strExecute;
    private final String strRepeat;
    private final String strAbort;

    private final String strMacroStatus;
    private final String strRecord;
    private final String strDelete;
    private final String strEdit;
    private final String strLoad;
    private final String strSave;
    private final String strShow;


    /***********************************************************************************************
     * Construct one transition of the InstrumentStateMachine.
     *
     * @param currentstate
     * @param nextstate
     * @param commandstatus
     * @param execute
     * @param repeat
     * @param abort
     * @param macrostatus
     * @param record
     * @param delete
     * @param edit
     * @param load
     * @param save
     * @param show
     * @param executetooltip
     * @param repeattooltip
     * @param aborttooltip
     * @param macrostatustext
     * @param recordtooltip
     * @param deletetooltip
     * @param edittooltip
     * @param loadtooltip
     * @param savetooltip
     * @param showtooltip
     */

    public InstrumentStateTransition(final InstrumentState currentstate,
                                     final InstrumentState nextstate,
                                     final boolean commandstatus,
                                     final boolean execute,
                                     final boolean repeat,
                                     final boolean abort,
                                     final boolean macrostatus,
                                     final boolean record,
                                     final boolean delete,
                                     final boolean edit,
                                     final boolean load,
                                     final boolean save,
                                     final boolean show,
                                     final String commandstatustext,
                                     final String executetooltip,
                                     final String repeattooltip,
                                     final String aborttooltip,
                                     final String macrostatustext,
                                     final String recordtooltip,
                                     final String deletetooltip,
                                     final String edittooltip,
                                     final String loadtooltip,
                                     final String savetooltip,
                                     final String showtooltip)
        {
        this.currentState = currentstate;
        this.nextState = nextstate;

        this.boolEnableCommandStatus = commandstatus;
        this.boolEnableExecute = execute;
        this.boolEnableRepeat = repeat;
        this.boolEnableAbort = abort;

        this.boolEnableMacroStatus = macrostatus;
        this.boolEnableRecord = record;
        this.boolEnableDelete = delete;
        this.boolEnableEdit = edit;
        this.boolEnableLoad = load;
        this.boolEnableSave = save;
        this.boolEnableShow = show;

        this.strCommandStatus = commandstatustext;
        this.strExecute = executetooltip;
        this.strRepeat = repeattooltip;
        this.strAbort = aborttooltip;

        this.strMacroStatus = macrostatustext;
        this.strRecord = recordtooltip;
        this.strDelete = deletetooltip;
        this.strEdit = edittooltip;
        this.strLoad = loadtooltip;
        this.strSave = savetooltip;
        this.strShow = showtooltip;
        }


    /***********************************************************************************************
     * Get the Current State.
     *
     * @return InstrumentState
     */

    public InstrumentState getCurrentState()
        {
        return (this.currentState);
        }


    /***********************************************************************************************
     * Get the Next State.
     *
     * @return InstrumentState
     */

    public InstrumentState getNextState()
        {
        return (this.nextState);
        }


    /**********************************************************************************************/
    /* Commander                                                                                  */
    /***********************************************************************************************
     * Get the CommandStatus status.
     *
     * @param context
     *
     * @return boolean
     */

    public boolean isEnableCommandStatus(final CommandProcessorContextInterface context)
        {
        return (this.boolEnableCommandStatus);
        }


    /***********************************************************************************************
     * Get the Execute status.
     *
     * @param context
     *
     * @return boolean
     */

    public boolean isEnableExecute(final CommandProcessorContextInterface context)
        {
        return (this.boolEnableExecute);
        }


    /***********************************************************************************************
     * Get the Repeat status.
     *
     * @param context
     *
     * @return boolean
     */

    public boolean isEnableRepeat(final CommandProcessorContextInterface context)
        {
        return (this.boolEnableRepeat);
        }


    /***********************************************************************************************
     * Get the Abort status.
     *
     * @param context
     *
     * @return boolean
     */

    public boolean isEnableAbort(final CommandProcessorContextInterface context)
        {
        return (this.boolEnableAbort);
        }


    /**********************************************************************************************/
    /* MacroManager                                                                               */
    /***********************************************************************************************
     * Get the MacroStatus status.
     *
     * @param context
     *
     * @return boolean
     */

    public boolean isEnableMacroStatus(final CommandProcessorContextInterface context)
        {
        return (this.boolEnableMacroStatus);
        }


    /***********************************************************************************************
     * Get the Record status.
     *
     * @param context
     *
     * @return boolean
     */

    public boolean isEnableRecord(final CommandProcessorContextInterface context)
        {
        return (this.boolEnableRecord);
        }


    /***********************************************************************************************
     * Get the Delete status.
     *
     * @param context
     *
     * @return boolean
     */

    public boolean isEnableDelete(final CommandProcessorContextInterface context)
        {
        return (this.boolEnableDelete);
        }


    /***********************************************************************************************
     * Get the Edit status.
     *
     * @param context
     *
     * @return boolean
     */

    public boolean isEnableEdit(final CommandProcessorContextInterface context)
        {
        return (this.boolEnableEdit);
        }


    /***********************************************************************************************
     * Get the Load status.
     *
     * @param context
     *
     * @return boolean
     */

    public boolean isEnableLoad(final CommandProcessorContextInterface context)
        {
        return (this.boolEnableLoad);
        }


    /***********************************************************************************************
     * Get the Save status.
     *
     * @param context
     *
     * @return boolean
     */

    public boolean isEnableSave(final CommandProcessorContextInterface context)
        {
        return (this.boolEnableSave);
        }


    /***********************************************************************************************
     * Get the Show status.
     *
     * @param context
     *
     * @return boolean
     */

    public boolean isEnableShow(final CommandProcessorContextInterface context)
        {
        return (this.boolEnableShow);
        }


    /**********************************************************************************************/
    /* Commander                                                                                  */
    /***********************************************************************************************
     * Get the CommandStatus text.
     *
     * @param context
     *
     * @return String
     */

    public String getCommandStatusText(final CommandProcessorContextInterface context)
        {
        return (this.strCommandStatus);
        }


    /***********************************************************************************************
     * Get the CommandStatus text, when Repeat support is not required.
     *
     * @return String
     */

    public String getCommandStatusText()
        {
        return (this.strCommandStatus);
        }


    /***********************************************************************************************
     * Get the Execute tooltip.
     *
     * @param context
     *
     * @return String
     */

    public String getExecuteTooltip(final CommandProcessorContextInterface context)
        {
        return (this.strExecute);
        }


    /***********************************************************************************************
     * Get the Repeat tooltip.
     *
     * @param context
     *
     * @return String
     */

    public String getRepeatTooltip(final CommandProcessorContextInterface context)
        {
        return (this.strRepeat);
        }


    /***********************************************************************************************
     * Get the Abort tooltip.
     *
     * @param context
     *
     * @return String
     */

    public String getAbortTooltip(final CommandProcessorContextInterface context)
        {
        return (this.strAbort);
        }


    /**********************************************************************************************/
    /* MacroManager                                                                               */
    /***********************************************************************************************
     * Get the MacroStatus text.
     *
     * @param context
     *
     * @return String
     */

    public String getMacroStatusText(final CommandProcessorContextInterface context)
        {
        return (this.strMacroStatus);
        }


    /***********************************************************************************************
     * Get the Record tooltip.
     *
     * @param context
     *
     * @return String
     */

    public String getRecordTooltip(final CommandProcessorContextInterface context)
        {
        return (this.strRecord);
        }


    /***********************************************************************************************
     * Get the Delete tooltip.
     *
     * @param context
     *
     * @return String
     */

    public String getDeleteTooltip(final CommandProcessorContextInterface context)
        {
        return (this.strDelete);
        }


    /***********************************************************************************************
     * Get the Edit tooltip.
     *
     * @param context
     *
     * @return String
     */

    public String getEditTooltip(final CommandProcessorContextInterface context)
        {
        return (this.strEdit);
        }


    /***********************************************************************************************
     * Get the Load tooltip.
     *
     * @param context
     *
     * @return String
     */

    public String getLoadTooltip(final CommandProcessorContextInterface context)
        {
        return (this.strLoad);
        }


    /***********************************************************************************************
     * Get the Save tooltip.
     *
     * @param context
     *
     * @return String
     */

    public String getSaveTooltip(final CommandProcessorContextInterface context)
        {
        return (this.strSave);
        }


    /***********************************************************************************************
     * Get the Show tooltip.
     *
     * @param context
     *
     * @return String
     */

    public String getShowTooltip(final CommandProcessorContextInterface context)
        {
        return (this.strShow);
        }
    }
