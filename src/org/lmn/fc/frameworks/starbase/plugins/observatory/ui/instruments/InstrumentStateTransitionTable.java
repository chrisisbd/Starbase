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

import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandProcessorContextInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.impl.CommandProcessorContext;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import static org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.InstrumentState.*;
import static org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface.*;


/***********************************************************************************************
 * InstrumentStateTransitionTable.
 */

public final class InstrumentStateTransitionTable implements FrameworkConstants,
                                                             FrameworkStrings,
                                                             FrameworkMetadata,
                                                             FrameworkSingletons
    {
    // The InstrumentStateTransitionTable is a Singleton!
    private volatile static InstrumentStateTransitionTable TRANSITION_TABLE_INSTANCE;

    private final Hashtable<InstrumentState, List<InstrumentStateTransition>> tableTransitions;


    /***********************************************************************************************
     * The InstrumentStateTransitionTable is a Singleton!
     *
     * @return InstrumentStateTransitionTable
     */

    public static InstrumentStateTransitionTable getInstance()
        {
        if (TRANSITION_TABLE_INSTANCE == null)
            {
            synchronized (InstrumentStateTransitionTable.class)
                {
                if (TRANSITION_TABLE_INSTANCE == null)
                    {
                    TRANSITION_TABLE_INSTANCE = new InstrumentStateTransitionTable();
                    }
                }
            }

        return (TRANSITION_TABLE_INSTANCE);
        }


    /***********************************************************************************************
     * Add a InstrumentStateTransition to this state machine transition table.
     * Return NULL if the addition failed.
     *
     * @param table
     * @param transition
     *
     * @return InstrumentStateTransition
     */

    private static InstrumentStateTransition addTransition(final Hashtable<InstrumentState, List<InstrumentStateTransition>> table,
                                                           final InstrumentStateTransition transition)
        {
        InstrumentStateTransition stateTransition;

        stateTransition = null;

        if ((transition != null)
            && (table != null)
            && (table.get(transition.getCurrentState()) != null))
            {
            table.get(transition.getCurrentState()).add(transition);
            stateTransition = transition;
            }

        return (stateTransition);
        }


    /***********************************************************************************************
     * Privately construct the InstrumentStateTransitionTable, just once.
     */

    private InstrumentStateTransitionTable()
        {
        this.tableTransitions = new Hashtable<InstrumentState, List<InstrumentStateTransition>>(25);

        populateInstrumentStateTransitionTable(getTransitionsTable());
        }


    /***********************************************************************************************
     * Get the table of InstrumentStateTransitions.
     * These transitions are the same for all Instruments, and so may come from a Singleton.
     *
     * @return Hashtable<InstrumentState, List<InstrumentStateTransition>>
     */

    public Hashtable<InstrumentState, List<InstrumentStateTransition>> getTransitionsTable()
        {
        return (this.tableTransitions);
        }


    /***********************************************************************************************
     * Populate the InstrumentStateTransitionTable for this specific application.
     *
     * @param table
     */

    private static void populateInstrumentStateTransitionTable(final Hashtable<InstrumentState, List<InstrumentStateTransition>> table)
        {
        final InstrumentState[] states;

        // Make entries in the table for every possible (current) InstrumentState
        states = InstrumentState.values();

        for (int i = 0;
             i < states.length;
             i++)
            {
            final InstrumentState state;

            state = states[i];
            table.put(state, new ArrayList<InstrumentStateTransition>(10));
            }

        //------------------------------------------------------------------------------------------
        // Add all valid transitions
        //------------------------------------------------------------------------------------------
        // Going to INITIALISED

        // For those Instruments which were created but never started
        addTransition(table,
                      new InstrumentStateTransition(INITIALISED, INITIALISED,
                                                    false, false, false, false,
                                                    false, false, false, false,
                                                    false, false,
                                                    false,
                                                    INITIALISED.getStatus(),
                                                    TOOLTIP_STOPPED,
                                                    TOOLTIP_STOPPED,
                                                    TOOLTIP_STOPPED,
                                                    INITIALISED.getStatus(),
                                                    TOOLTIP_STOPPED,
                                                    TOOLTIP_STOPPED,
                                                    TOOLTIP_STOPPED,
                                                    TOOLTIP_STOPPED,
                                                    TOOLTIP_STOPPED,
                                                    TOOLTIP_STOPPED));

        addTransition(table,
                      new InstrumentStateTransition(CREATED, INITIALISED,
                                                    false, false, false, false,
                                                    false, false, false, false,
                                                    false, false,
                                                    false,
                                                    INITIALISED.getStatus(),
                                                    TOOLTIP_STOPPED,
                                                    TOOLTIP_STOPPED,
                                                    TOOLTIP_STOPPED,
                                                    INITIALISED.getStatus(),
                                                    TOOLTIP_STOPPED,
                                                    TOOLTIP_STOPPED,
                                                    TOOLTIP_STOPPED,
                                                    TOOLTIP_STOPPED,
                                                    TOOLTIP_STOPPED,
                                                    TOOLTIP_STOPPED));

        //------------------------------------------------------------------------------------------
        // Going to READY

        // Allow loopback for e.g. Command and Macro selection
        addTransition(table,
                      new InstrumentStateTransition(READY, READY,
                                                    true, true, true, false,
                                                    true, true, true, true,
                                                    true, true,
                                                    true,
                                                    READY.getStatus(),
                                                    TOOLTIP_COMMAND_EXECUTE,
                                                    TOOLTIP_COMMAND_REPEAT,
                                                    TOOLTIP_COMMAND_ABORT,
                                                    READY.getStatus(),
                                                    TOOLTIP_COMMAND_RECORD,
                                                    TOOLTIP_COMMAND_DELETE,
                                                    TOOLTIP_COMMAND_EDIT,
                                                    TOOLTIP_COMMAND_LOAD,
                                                    TOOLTIP_COMMAND_SAVE,
                                                    TOOLTIP_COMMAND_SHOW));

        addTransition(table,
                      new InstrumentStateTransition(INITIALISED, READY,
                                                    true, true, true, false,
                                                    true, true, true, true,
                                                    true, true,
                                                    true,
                                                    READY.getStatus(),
                                                    TOOLTIP_COMMAND_EXECUTE,
                                                    TOOLTIP_COMMAND_REPEAT,
                                                    TOOLTIP_COMMAND_ABORT,
                                                    READY.getStatus(),
                                                    TOOLTIP_COMMAND_RECORD,
                                                    TOOLTIP_COMMAND_DELETE,
                                                    TOOLTIP_COMMAND_EDIT,
                                                    TOOLTIP_COMMAND_LOAD,
                                                    TOOLTIP_COMMAND_SAVE,
                                                    TOOLTIP_COMMAND_SHOW));

        addTransition(table, new InstrumentStateTransition(BUSY,  READY,
                                                           true, true, true, false,
                                                           true, true, true, true,
                                                           true, true,
                                                           true,
                                                           READY.getStatus(),
                                                           TOOLTIP_COMMAND_EXECUTE,
                                                           TOOLTIP_COMMAND_REPEAT,
                                                           TOOLTIP_COMMAND_ABORT,
                                                           READY.getStatus(),
                                                           TOOLTIP_COMMAND_RECORD,
                                                           TOOLTIP_COMMAND_DELETE,
                                                           TOOLTIP_COMMAND_EDIT,
                                                           TOOLTIP_COMMAND_LOAD,
                                                           TOOLTIP_COMMAND_SAVE,
                                                           TOOLTIP_COMMAND_SHOW));

        addTransition(table, new InstrumentStateTransition(REPEATING, READY,
                                                           true, true, true, false,
                                                           true, true, true, true,
                                                           true, true,
                                                           true,
                                                           READY.getStatus(),
                                                           TOOLTIP_COMMAND_EXECUTE,
                                                           TOOLTIP_COMMAND_REPEAT,
                                                           TOOLTIP_COMMAND_ABORT,
                                                           READY.getStatus(),
                                                           TOOLTIP_COMMAND_RECORD,
                                                           TOOLTIP_COMMAND_DELETE,
                                                           TOOLTIP_COMMAND_EDIT,
                                                           TOOLTIP_COMMAND_LOAD,
                                                           TOOLTIP_COMMAND_SAVE,
                                                           TOOLTIP_COMMAND_SHOW));

        addTransition(table, new InstrumentStateTransition(EDIT_MACRO, READY,
                                                           true, true, true, false,
                                                           true, true, true, true,
                                                           true, true,
                                                           true,
                                                           READY.getStatus(),
                                                           TOOLTIP_COMMAND_EXECUTE,
                                                           TOOLTIP_COMMAND_REPEAT,
                                                           TOOLTIP_COMMAND_ABORT,
                                                           READY.getStatus(),
                                                           TOOLTIP_COMMAND_RECORD,
                                                           TOOLTIP_COMMAND_DELETE,
                                                           TOOLTIP_COMMAND_EDIT,
                                                           TOOLTIP_COMMAND_LOAD,
                                                           TOOLTIP_COMMAND_SAVE,
                                                           TOOLTIP_COMMAND_SHOW));

        addTransition(table, new InstrumentStateTransition(DELETE_MACRO, READY,
                                                           true, true, true, false,
                                                           true, true, true, true,
                                                           true, true,
                                                           true,
                                                           READY.getStatus(),
                                                           TOOLTIP_COMMAND_EXECUTE,
                                                           TOOLTIP_COMMAND_REPEAT,
                                                           TOOLTIP_COMMAND_ABORT,
                                                           READY.getStatus(),
                                                           TOOLTIP_COMMAND_RECORD,
                                                           TOOLTIP_COMMAND_DELETE,
                                                           TOOLTIP_COMMAND_EDIT,
                                                           TOOLTIP_COMMAND_LOAD,
                                                           TOOLTIP_COMMAND_SAVE,
                                                           TOOLTIP_COMMAND_SHOW));

        addTransition(table, new InstrumentStateTransition(SHOW_MACRO, READY,
                                                           true, true, true, false,
                                                           true, true, true, true,
                                                           true, true,
                                                           true,
                                                           READY.getStatus(),
                                                           TOOLTIP_COMMAND_EXECUTE,
                                                           TOOLTIP_COMMAND_REPEAT,
                                                           TOOLTIP_COMMAND_ABORT,
                                                           READY.getStatus(),
                                                           TOOLTIP_COMMAND_RECORD,
                                                           TOOLTIP_COMMAND_DELETE,
                                                           TOOLTIP_COMMAND_EDIT,
                                                           TOOLTIP_COMMAND_LOAD,
                                                           TOOLTIP_COMMAND_SAVE,
                                                           TOOLTIP_COMMAND_SHOW));

        addTransition(table, new InstrumentStateTransition(LOAD_MACROS, READY,
                                                           true, true, true, false,
                                                           true, true, true, true,
                                                           true, true,
                                                           true,
                                                           READY.getStatus(),
                                                           TOOLTIP_COMMAND_EXECUTE,
                                                           TOOLTIP_COMMAND_REPEAT,
                                                           TOOLTIP_COMMAND_ABORT,
                                                           READY.getStatus(),
                                                           TOOLTIP_COMMAND_RECORD,
                                                           TOOLTIP_COMMAND_DELETE,
                                                           TOOLTIP_COMMAND_EDIT,
                                                           TOOLTIP_COMMAND_LOAD,
                                                           TOOLTIP_COMMAND_SAVE,
                                                           TOOLTIP_COMMAND_SHOW));

        addTransition(table, new InstrumentStateTransition(SAVE_MACROS, READY,
                                                           true, true, true, false,
                                                           true, true, true, true,
                                                           true, true,
                                                           true,
                                                           READY.getStatus(),
                                                           TOOLTIP_COMMAND_EXECUTE,
                                                           TOOLTIP_COMMAND_REPEAT,
                                                           TOOLTIP_COMMAND_ABORT,
                                                           READY.getStatus(),
                                                           TOOLTIP_COMMAND_RECORD,
                                                           TOOLTIP_COMMAND_DELETE,
                                                           TOOLTIP_COMMAND_EDIT,
                                                           TOOLTIP_COMMAND_LOAD,
                                                           TOOLTIP_COMMAND_SAVE,
                                                           TOOLTIP_COMMAND_SHOW));

        addTransition(table, new InstrumentStateTransition(STOPPED, READY,
                                                           true, true, true, false,
                                                           true, true, true, true,
                                                           true, true,
                                                           true,
                                                           READY.getStatus(),
                                                           TOOLTIP_COMMAND_EXECUTE,
                                                           TOOLTIP_COMMAND_REPEAT,
                                                           TOOLTIP_COMMAND_ABORT,
                                                           READY.getStatus(),
                                                           TOOLTIP_COMMAND_RECORD,
                                                           TOOLTIP_COMMAND_DELETE,
                                                           TOOLTIP_COMMAND_EDIT,
                                                           TOOLTIP_COMMAND_LOAD,
                                                           TOOLTIP_COMMAND_SAVE,
                                                           TOOLTIP_COMMAND_SHOW));


        //------------------------------------------------------------------------------------------
        // Going to BUSY

        // Updates while BUSY
        addTransition(table,
                      new InstrumentStateTransition(BUSY, BUSY,
                                                    true, false, false, true,
                                                    true, false, false, false,
                                                    false, false,
                                                    false,
                                                    BUSY.getStatus(),
                                                    TOOLTIP_COMMAND_EXECUTING,
                                                    TOOLTIP_COMMAND_EXECUTING,
                                                    TOOLTIP_COMMAND_ABORT,
                                                    BUSY.getStatus(),
                                                    EMPTY_STRING,
                                                    EMPTY_STRING,
                                                    EMPTY_STRING,
                                                    EMPTY_STRING,
                                                    EMPTY_STRING,
                                                    EMPTY_STRING));

        addTransition(table,
                      new InstrumentStateTransition(READY, BUSY,
                                                    true, false, false, true,
                                                    true, false, false, false,
                                                    false, false,
                                                    false,
                                                    BUSY.getStatus(),
                                                    TOOLTIP_COMMAND_EXECUTING,
                                                    TOOLTIP_COMMAND_EXECUTING,
                                                    TOOLTIP_COMMAND_ABORT,
                                                    BUSY.getStatus(),
                                                    EMPTY_STRING,
                                                    EMPTY_STRING,
                                                    EMPTY_STRING,
                                                    EMPTY_STRING,
                                                    EMPTY_STRING,
                                                    EMPTY_STRING));

        //------------------------------------------------------------------------------------------
        // Going to REPEATING

        final InstrumentStateTransition transitionReadyToRepeating;

        transitionReadyToRepeating = new InstrumentStateTransition(READY, REPEATING,
                                                                   true, false, false, true,
                                                                   true, false, false, false,
                                                                   false, false,
                                                                   false,
                                                                   REPEATING.getStatus(),  // This will never be seen
                                                                   TOOLTIP_COMMAND_REPEATING,
                                                                   TOOLTIP_COMMAND_REPEATING,
                                                                   TOOLTIP_COMMAND_ABORT,
                                                                   REPEATING.getStatus(),
                                                                   EMPTY_STRING,
                                                                   EMPTY_STRING,
                                                                   EMPTY_STRING,
                                                                   EMPTY_STRING,
                                                                   EMPTY_STRING,
                                                                   EMPTY_STRING)
            {
            /***************************************************************************************
             * Get the CommandStatus text.
             *
             * @return String
             * @param context
             */

            public String getCommandStatusText(final CommandProcessorContextInterface context)
                {
                String strStatusText;

                strStatusText = super.getCommandStatusText(context);

                if (context != null)
                    {
                    final StringBuffer buffer;

                    buffer = new StringBuffer();
                    buffer.append(getNextState().getStatus());
                    buffer.append(SPACE);
                    buffer.append(CommandProcessorContext.getRepeatNumber(context));
                    buffer.append(SPACE);
                    buffer.append(context.getRepeatText());

                    strStatusText = buffer.toString();
                    }

                return (strStatusText);
                }


            /***************************************************************************************
             * Get the MacroStatus text.
             *
             * @return String
             */

            public String getMacroStatusText(final CommandProcessorContextInterface context)
                {
                String strStatusText;

                strStatusText = super.getMacroStatusText(context);

                if (context != null)
                    {
                    final StringBuffer buffer;

                    buffer = new StringBuffer();
                    buffer.append(getNextState().getStatus());
                    buffer.append(SPACE);
                    buffer.append(CommandProcessorContext.getRepeatNumber(context));
                    buffer.append(SPACE);
                    buffer.append(context.getRepeatText());

                    strStatusText = buffer.toString();
                    }

                return (strStatusText);
                }
            };

        addTransition(table, transitionReadyToRepeating);

        final InstrumentStateTransition transitionRepeatingToRepeating;

        transitionRepeatingToRepeating = new InstrumentStateTransition(REPEATING, REPEATING,
                                                                       true, false, false, true,
                                                                       true, false, false, false,
                                                                       false, false,
                                                                       false,
                                                                       REPEATING.getStatus(),  // This will never be seen
                                                                       TOOLTIP_COMMAND_REPEATING,
                                                                       TOOLTIP_COMMAND_REPEATING,
                                                                       TOOLTIP_COMMAND_ABORT,
                                                                       REPEATING.getStatus(),
                                                                       EMPTY_STRING,
                                                                       EMPTY_STRING,
                                                                       EMPTY_STRING,
                                                                       EMPTY_STRING,
                                                                       EMPTY_STRING,
                                                                       EMPTY_STRING)
            {
            /***************************************************************************************
             * Get the CommandStatus text.
             *
             * @return String
             * @param context
             */

            public String getCommandStatusText(final CommandProcessorContextInterface context)
                {
                String strStatusText;

                strStatusText = super.getCommandStatusText(context);

                if (context != null)
                    {
                    final StringBuffer buffer;

                    buffer = new StringBuffer();
                    buffer.append(getNextState().getStatus());
                    buffer.append(SPACE);
                    buffer.append(CommandProcessorContext.getRepeatNumber(context));
                    buffer.append(SPACE);
                    buffer.append(context.getRepeatText());

                    strStatusText = buffer.toString();
                    }

                return (strStatusText);
                }


            /***************************************************************************************
             * Get the MacroStatus text.
             *
             * @return String
             */

            public String getMacroStatusText(final CommandProcessorContextInterface context)
                {
                String strStatusText;

                strStatusText = super.getMacroStatusText(context);

                if (context != null)
                    {
                    final StringBuffer buffer;

                    buffer = new StringBuffer();
                    buffer.append(getNextState().getStatus());
                    buffer.append(SPACE);
                    buffer.append(CommandProcessorContext.getRepeatNumber(context));
                    buffer.append(SPACE);
                    buffer.append(context.getRepeatText());

                    strStatusText = buffer.toString();
                    }

                return (strStatusText);
                }
            };

        addTransition(table, transitionRepeatingToRepeating);

        //------------------------------------------------------------------------------------------
        // Going to EDIT_MACRO

        addTransition(table,
                      new InstrumentStateTransition(EDIT_MACRO, EDIT_MACRO,
                                                    true, false, false, false,
                                                    true, false, false, true,
                                                    false, false,
                                                    false,
                                                    EDIT_MACRO.getStatus(),
                                                    EMPTY_STRING,
                                                    EMPTY_STRING,
                                                    EMPTY_STRING,
                                                    EDIT_MACRO.getStatus(),
                                                    EMPTY_STRING,
                                                    EMPTY_STRING,
                                                    TOOLTIP_COMMAND_EDIT,
                                                    EMPTY_STRING,
                                                    EMPTY_STRING,
                                                    EMPTY_STRING));

        addTransition(table,
                      new InstrumentStateTransition(READY, EDIT_MACRO,
                                                    true, false, false, false,
                                                    true, false, false, true,
                                                    false, false,
                                                    false,
                                                    EDIT_MACRO.getStatus(),
                                                    EMPTY_STRING,
                                                    EMPTY_STRING,
                                                    EMPTY_STRING,
                                                    EDIT_MACRO.getStatus(),
                                                    EMPTY_STRING,
                                                    EMPTY_STRING,
                                                    TOOLTIP_COMMAND_EDIT,
                                                    EMPTY_STRING,
                                                    EMPTY_STRING,
                                                    EMPTY_STRING));

        //------------------------------------------------------------------------------------------
        // Going to DELETE_MACRO

        addTransition(table,
                      new InstrumentStateTransition(DELETE_MACRO, DELETE_MACRO,
                                                    true, false, false, false,
                                                    true, false, false, false,
                                                    false, false,
                                                    false,
                                                    DELETE_MACRO.getStatus(),
                                                    EMPTY_STRING,
                                                    EMPTY_STRING,
                                                    EMPTY_STRING,
                                                    DELETE_MACRO.getStatus(),
                                                    EMPTY_STRING,
                                                    TOOLTIP_COMMAND_DELETE,
                                                    EMPTY_STRING,
                                                    EMPTY_STRING,
                                                    EMPTY_STRING,
                                                    EMPTY_STRING));

        addTransition(table,
                      new InstrumentStateTransition(READY, DELETE_MACRO,
                                                    true, false, false, false,
                                                    true, false, false, false,
                                                    false, false,
                                                    false,
                                                    DELETE_MACRO.getStatus(),
                                                    EMPTY_STRING,
                                                    EMPTY_STRING,
                                                    EMPTY_STRING,
                                                    DELETE_MACRO.getStatus(),
                                                    EMPTY_STRING,
                                                    TOOLTIP_COMMAND_DELETE,
                                                    EMPTY_STRING,
                                                    EMPTY_STRING,
                                                    EMPTY_STRING,
                                                    EMPTY_STRING));

        //------------------------------------------------------------------------------------------
        // Going to SHOW_MACRO

        addTransition(table,
                      new InstrumentStateTransition(SHOW_MACRO, SHOW_MACRO,
                                                    true, false, false, false,
                                                    true, false, false, false,
                                                    false, false,
                                                    true,
                                                    SHOW_MACRO.getStatus(),
                                                    EMPTY_STRING,
                                                    EMPTY_STRING,
                                                    EMPTY_STRING,
                                                    SHOW_MACRO.getStatus(),
                                                    EMPTY_STRING,
                                                    EMPTY_STRING,
                                                    EMPTY_STRING,
                                                    EMPTY_STRING,
                                                    EMPTY_STRING,
                                                    TOOLTIP_COMMAND_SHOW));

        addTransition(table,
                      new InstrumentStateTransition(READY, SHOW_MACRO,
                                                    true, false, false, false,
                                                    true, false, false, false,
                                                    false, false,
                                                    true,
                                                    SHOW_MACRO.getStatus(),
                                                    EMPTY_STRING,
                                                    EMPTY_STRING,
                                                    EMPTY_STRING,
                                                    SHOW_MACRO.getStatus(),
                                                    EMPTY_STRING,
                                                    EMPTY_STRING,
                                                    EMPTY_STRING,
                                                    EMPTY_STRING,
                                                    EMPTY_STRING,
                                                    TOOLTIP_COMMAND_SHOW));

        //------------------------------------------------------------------------------------------
        // Going to LOAD_MACROS

        addTransition(table,
                      new InstrumentStateTransition(READY, LOAD_MACROS,
                                                    true, false, false, false,
                                                    true, false, false, false,
                                                    false, false,
                                                    false,
                                                    LOAD_MACROS.getStatus(),
                                                    EMPTY_STRING,
                                                    EMPTY_STRING,
                                                    EMPTY_STRING,
                                                    LOAD_MACROS.getStatus(),
                                                    EMPTY_STRING,
                                                    EMPTY_STRING,
                                                    EMPTY_STRING,
                                                    TOOLTIP_COMMAND_LOADING,
                                                    EMPTY_STRING,
                                                    EMPTY_STRING));

        //------------------------------------------------------------------------------------------
        // Going to SAVE_MACROS

        addTransition(table,
                      new InstrumentStateTransition(READY, SAVE_MACROS,
                                                    true, false, false, false,
                                                    true, false, false, false,
                                                    false, false,
                                                    false,
                                                    SAVE_MACROS.getStatus(),
                                                    EMPTY_STRING,
                                                    EMPTY_STRING,
                                                    EMPTY_STRING,
                                                    SAVE_MACROS.getStatus(),
                                                    EMPTY_STRING,
                                                    EMPTY_STRING,
                                                    EMPTY_STRING,
                                                    EMPTY_STRING,
                                                    TOOLTIP_COMMAND_SAVING,
                                                    EMPTY_STRING));

        //------------------------------------------------------------------------------------------
        // Going to STOPPED

        // For shutdown
        addTransition(table,
                      new InstrumentStateTransition(STOPPED, STOPPED,
                                                    true, false, false, false,
                                                    true, false, false, false,
                                                    false, false,
                                                    false,
                                                    STOPPED.getStatus(),
                                                    TOOLTIP_STOPPED,
                                                    TOOLTIP_STOPPED,
                                                    TOOLTIP_STOPPED,
                                                    STOPPED.getStatus(),
                                                    TOOLTIP_STOPPED,
                                                    TOOLTIP_STOPPED,
                                                    TOOLTIP_STOPPED,
                                                    TOOLTIP_STOPPED,
                                                    TOOLTIP_STOPPED,
                                                    TOOLTIP_STOPPED));

        addTransition(table,
                      new InstrumentStateTransition(INITIALISED, STOPPED,
                                                    true, false, false, false,
                                                    true, false, false, false,
                                                    false, false,
                                                    false,
                                                    STOPPED.getStatus(),
                                                    TOOLTIP_STOPPED,
                                                    TOOLTIP_STOPPED,
                                                    TOOLTIP_STOPPED,
                                                    STOPPED.getStatus(),
                                                    TOOLTIP_STOPPED,
                                                    TOOLTIP_STOPPED,
                                                    TOOLTIP_STOPPED,
                                                    TOOLTIP_STOPPED,
                                                    TOOLTIP_STOPPED,
                                                    TOOLTIP_STOPPED));

        addTransition(table,
                      new InstrumentStateTransition(READY, STOPPED,
                                                    true, false, false, false,
                                                    true, false, false, false,
                                                    false, false,
                                                    false,
                                                    STOPPED.getStatus(),
                                                    TOOLTIP_STOPPED,
                                                    TOOLTIP_STOPPED,
                                                    TOOLTIP_STOPPED,
                                                    STOPPED.getStatus(),
                                                    TOOLTIP_STOPPED,
                                                    TOOLTIP_STOPPED,
                                                    TOOLTIP_STOPPED,
                                                    TOOLTIP_STOPPED,
                                                    TOOLTIP_STOPPED,
                                                    TOOLTIP_STOPPED));

        addTransition(table,
                      new InstrumentStateTransition(BUSY, STOPPED,
                                                    true, false, false, false,
                                                    true, false, false, false,
                                                    false, false,
                                                    false,
                                                    STOPPED.getStatus(),
                                                    TOOLTIP_STOPPED,
                                                    TOOLTIP_STOPPED,
                                                    TOOLTIP_STOPPED,
                                                    STOPPED.getStatus(),
                                                    TOOLTIP_STOPPED,
                                                    TOOLTIP_STOPPED,
                                                    TOOLTIP_STOPPED,
                                                    TOOLTIP_STOPPED,
                                                    TOOLTIP_STOPPED,
                                                    TOOLTIP_STOPPED));

        addTransition(table,
                      new InstrumentStateTransition(REPEATING, STOPPED,
                                                    true, false, false, false,
                                                    true, false, false, false,
                                                    false, false,
                                                    false,
                                                    STOPPED.getStatus(),
                                                    TOOLTIP_STOPPED,
                                                    TOOLTIP_STOPPED,
                                                    TOOLTIP_STOPPED,
                                                    STOPPED.getStatus(),
                                                    TOOLTIP_STOPPED,
                                                    TOOLTIP_STOPPED,
                                                    TOOLTIP_STOPPED,
                                                    TOOLTIP_STOPPED,
                                                    TOOLTIP_STOPPED,
                                                    TOOLTIP_STOPPED));

        addTransition(table,
                      new InstrumentStateTransition(EDIT_MACRO, STOPPED,
                                                    true, false, false, false,
                                                    true, false, false, false,
                                                    false, false,
                                                    false,
                                                    STOPPED.getStatus(),
                                                    TOOLTIP_STOPPED,
                                                    TOOLTIP_STOPPED,
                                                    TOOLTIP_STOPPED,
                                                    STOPPED.getStatus(),
                                                    TOOLTIP_STOPPED,
                                                    TOOLTIP_STOPPED,
                                                    TOOLTIP_STOPPED,
                                                    TOOLTIP_STOPPED,
                                                    TOOLTIP_STOPPED,
                                                    TOOLTIP_STOPPED));

        addTransition(table,
                      new InstrumentStateTransition(SHOW_MACRO, STOPPED,
                                                    true, false, false, false,
                                                    true, false, false, false,
                                                    false, false,
                                                    false,
                                                    STOPPED.getStatus(),
                                                    TOOLTIP_STOPPED,
                                                    TOOLTIP_STOPPED,
                                                    TOOLTIP_STOPPED,
                                                    STOPPED.getStatus(),
                                                    TOOLTIP_STOPPED,
                                                    TOOLTIP_STOPPED,
                                                    TOOLTIP_STOPPED,
                                                    TOOLTIP_STOPPED,
                                                    TOOLTIP_STOPPED,
                                                    TOOLTIP_STOPPED));

        //------------------------------------------------------------------------------------------
        // Going to DISPOSED

        addTransition(table,
                      new InstrumentStateTransition(INITIALISED, DISPOSED,
                                                    false, false, false, false,
                                                    false, false, false, false,
                                                    false, false,
                                                    false,
                                                    EMPTY_STRING,
                                                    EMPTY_STRING,
                                                    EMPTY_STRING,
                                                    EMPTY_STRING,
                                                    EMPTY_STRING,
                                                    EMPTY_STRING,
                                                    EMPTY_STRING,
                                                    EMPTY_STRING,
                                                    EMPTY_STRING,
                                                    EMPTY_STRING,
                                                    EMPTY_STRING));


        addTransition(table,
                      new InstrumentStateTransition(SHOW_MACRO, DISPOSED,
                                                    false, false, false, false,
                                                    false, false, false, false,
                                                    false, false,
                                                    false,
                                                    EMPTY_STRING,
                                                    EMPTY_STRING,
                                                    EMPTY_STRING,
                                                    EMPTY_STRING,
                                                    EMPTY_STRING,
                                                    EMPTY_STRING,
                                                    EMPTY_STRING,
                                                    EMPTY_STRING,
                                                    EMPTY_STRING,
                                                    EMPTY_STRING,
                                                    EMPTY_STRING));

        addTransition(table,
                      new InstrumentStateTransition(STOPPED, DISPOSED,
                                                    false, false, false, false,
                                                    false, false, false, false,
                                                    false, false,
                                                    false,
                                                    EMPTY_STRING,
                                                    EMPTY_STRING,
                                                    EMPTY_STRING,
                                                    EMPTY_STRING,
                                                    EMPTY_STRING,
                                                    EMPTY_STRING,
                                                    EMPTY_STRING,
                                                    EMPTY_STRING,
                                                    EMPTY_STRING,
                                                    EMPTY_STRING,
                                                    EMPTY_STRING));
        }
    }
