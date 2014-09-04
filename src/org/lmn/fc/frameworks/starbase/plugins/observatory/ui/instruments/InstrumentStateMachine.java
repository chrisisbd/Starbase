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
import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.ActivityIndicatorUIComponentInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandProcessorContextInterface;

import java.awt.*;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;


/***************************************************************************************************
 * InstrumentStateMachine.
 * See: http://en.wikipedia.org/wiki/Finite-state_machine.
 */

public final class InstrumentStateMachine implements FrameworkConstants,
                                                     FrameworkStrings,
                                                     FrameworkSingletons
    {
    // Injections
    private final Hashtable<InstrumentState, List<InstrumentStateTransition>> tableTransitions;
    private final CommandProcessorContextInterface context;


    /***********************************************************************************************
     * Apply the Transition metadata to the items under its control in the Context.
     *
     * @param transitioncontext
     * @param transition
     */

    private static void processTransition(final CommandProcessorContextInterface transitioncontext,
                                          final InstrumentStateTransition transition)
        {
        if (transitioncontext.getObservatory().isRecordMacroMode())
            {
            doRecordMacroMode(transitioncontext, transition);
            }
        else
            {
            doCommanderMode(transitioncontext, transition);
            }
        }


    /***********************************************************************************************
     * Set the FSM outputs for Record Macro Mode.
     *
     * @param transitioncontext
     * @param transition
     */

    private static void doRecordMacroMode(final CommandProcessorContextInterface transitioncontext,
                                          final InstrumentStateTransition transition)
        {
        //------------------------------------------------------------------------------------------
        // Buttons
        //------------------------------------------------------------------------------------------
        // Record Macro Mode, Commander Tab State

        // Execute, Repeat and Abort do not work in Record Macro mode
        if (transitioncontext.getExecuteButton() != null)
            {
            transitioncontext.getExecuteButton().setEnabled(false);
            transitioncontext.getExecuteButton().setToolTipText(EMPTY_STRING);
            }

        if (transitioncontext.getRepeatButton() != null)
            {
            transitioncontext.getRepeatButton().setEnabled(false);
            transitioncontext.getRepeatButton().setToolTipText(EMPTY_STRING);
            }

        if (transitioncontext.getAbortButton() != null)
            {
            transitioncontext.getAbortButton().setEnabled(false);
            transitioncontext.getAbortButton().setToolTipText(EMPTY_STRING);
            }

        //------------------------------------------------------------------------------------------
        // Record Macro Mode, Macro Manager Tab state

        if (transitioncontext.getRecordMacroButton() != null)
            {
            transitioncontext.getRecordMacroButton().setEnabled(transition.isEnableRecord(transitioncontext));
            transitioncontext.getRecordMacroButton().setToolTipText(transition.getRecordTooltip(transitioncontext));
            }

        if (transitioncontext.getEditMacroButton() != null)
            {
            transitioncontext.getEditMacroButton().setEnabled(transition.isEnableEdit(transitioncontext));
            transitioncontext.getEditMacroButton().setToolTipText(transition.getEditTooltip(transitioncontext));
            }

        if (transitioncontext.getDeleteMacroButton() != null)
            {
            transitioncontext.getDeleteMacroButton().setEnabled(transition.isEnableDelete(transitioncontext));
            transitioncontext.getDeleteMacroButton().setToolTipText(transition.getDeleteTooltip(transitioncontext));
            }

        if (transitioncontext.getShowMacroButton() != null)
            {
            transitioncontext.getShowMacroButton().setEnabled(transition.isEnableShow(transitioncontext));
            transitioncontext.getShowMacroButton().setToolTipText(transition.getShowTooltip(transitioncontext));
            }

        if (transitioncontext.getLoadMacroButton() != null)
            {
            transitioncontext.getLoadMacroButton().setEnabled(transition.isEnableLoad(transitioncontext));
            transitioncontext.getLoadMacroButton().setToolTipText(transition.getLoadTooltip(transitioncontext));
            }

        if (transitioncontext.getSaveMacroButton() != null)
            {
            transitioncontext.getSaveMacroButton().setEnabled(transition.isEnableSave(transitioncontext));
            transitioncontext.getSaveMacroButton().setToolTipText(transition.getSaveTooltip(transitioncontext));
            }

        //------------------------------------------------------------------------------------------
        // Panels
        //------------------------------------------------------------------------------------------
        // The CommandBuilder ViewerContainer must show the CommandLifecycleLog at all times
        // except when in RecordMacro mode, and when ShowMacro is executed.

        if (transitioncontext.getViewerContainer() != null)
            {
            boolean boolShowMacroViewer;

            boolShowMacroViewer = false;

            // TODO extend
            // Only enable the MacroViewer if the Macro Button is set in the Transition
            // AND there is a Module and Macro selected
            if (transitioncontext.getShowMacroButton() != null)
                {
                boolShowMacroViewer = ((transitioncontext.getSelectedModule() != null)
                                        && (transitioncontext.isSelectedMacro())
                                        && (transition.isEnableShow(transitioncontext)));
                }

            if (boolShowMacroViewer)
                {
                transitioncontext.getViewerContainer().removeAll();
                transitioncontext.getViewerContainer().add((Component)transitioncontext.getMacroViewer(),
                                                           BorderLayout.CENTER);
                }
            else
                {
                transitioncontext.getViewerContainer().removeAll();
                transitioncontext.getViewerContainer().add((Component)transitioncontext.getCommandLifecycleLog(),
                                                           BorderLayout.CENTER);
                }
            }
        }


    /***********************************************************************************************
     * Set the FSM outputs for Commander Mode.
     *
     * @param transitioncontext
     * @param transition
     */

    private static void doCommanderMode(final CommandProcessorContextInterface transitioncontext,
                                        final InstrumentStateTransition transition)
        {
        final String SOURCE = "InstrumentStateMachine.doCommanderMode() ";

        //------------------------------------------------------------------------------------------
        // Commander Mode, Commander Tab State

        // Only enable the Execute and Repeat buttons if set in the Transition
        // AND there is a Module and (Macro or Command) selected

        if (transitioncontext.getExecuteButton() != null)
            {
            final boolean boolEnabled;

            boolEnabled = ((transitioncontext.getSelectedModule() != null)
                            && (transitioncontext.isSelectedMacroOrCommand())
                            && (transition.isEnableExecute(transitioncontext)));

            transitioncontext.getExecuteButton().setEnabled(boolEnabled);
            transitioncontext.getExecuteButton().setToolTipText(transition.getExecuteTooltip(transitioncontext));
            }

        if (transitioncontext.getRepeatButton() != null)
            {
            final boolean boolEnabled;

            boolEnabled = ((transitioncontext.getSelectedModule() != null)
                            && (transitioncontext.isSelectedMacroOrCommand())
                            && (transition.isEnableRepeat(transitioncontext)));

            transitioncontext.getRepeatButton().setEnabled(boolEnabled);
            transitioncontext.getRepeatButton().setToolTipText(transition.getRepeatTooltip(transitioncontext));
            }

        // Only allow Abort if there's something to Abort
        if (transitioncontext.getAbortButton() != null)
            {
            final boolean boolEnabled;

            boolEnabled = ((transitioncontext.getSelectedModule() != null)
                            && (transitioncontext.isSelectedMacroOrCommand())
                            && (transition.isEnableAbort(transitioncontext)));

            transitioncontext.getAbortButton().setEnabled(boolEnabled);
            transitioncontext.getAbortButton().setToolTipText(transition.getAbortTooltip(transitioncontext));
            }

        //------------------------------------------------------------------------------------------
        // Commander Mode, Macro Manager Tab state

        // Always allow entry to Record Macro mode
        if (transitioncontext.getRecordMacroButton() != null)
            {
            transitioncontext.getRecordMacroButton().setEnabled(transition.isEnableRecord(transitioncontext));
            transitioncontext.getRecordMacroButton().setToolTipText(transition.getRecordTooltip(transitioncontext));
            }

        // Only enable the Edit Macro button if set in the Transition
        // AND there is a Module and Macro selected
        if (transitioncontext.getEditMacroButton() != null)
            {
            final boolean boolEnabled;

            boolEnabled = ((transitioncontext.getSelectedModule() != null)
                            && (transitioncontext.isSelectedMacro())
                            && (transition.isEnableEdit(transitioncontext)));

            transitioncontext.getEditMacroButton().setEnabled(boolEnabled);
            transitioncontext.getEditMacroButton().setToolTipText(transition.getEditTooltip(transitioncontext));
            }

        // Only enable the Delete Macro button if set in the Transition
        // AND there is a Module and Macro selected
        if (transitioncontext.getDeleteMacroButton() != null)
            {
            final boolean boolEnabled;

            boolEnabled = ((transitioncontext.getSelectedModule() != null)
                            && (transitioncontext.isSelectedMacro())
                            && (transition.isEnableDelete(transitioncontext)));

            transitioncontext.getDeleteMacroButton().setEnabled(boolEnabled);
            transitioncontext.getDeleteMacroButton().setToolTipText(transition.getDeleteTooltip(transitioncontext));
            }

        // Only enable the Show Macro button if set in the Transition
        // AND there is a Module and Macro selected
        if (transitioncontext.getShowMacroButton() != null)
            {
            final boolean boolEnabled;

            boolEnabled = ((transitioncontext.getSelectedModule() != null)
                            && (transitioncontext.isSelectedMacro())
                            && (transition.isEnableShow(transitioncontext)));

            transitioncontext.getShowMacroButton().setEnabled(boolEnabled);
            transitioncontext.getShowMacroButton().setToolTipText(transition.getShowTooltip(transitioncontext));
            }

        // Always allow LoadMacros
        if (transitioncontext.getLoadMacroButton() != null)
            {
            transitioncontext.getLoadMacroButton().setEnabled(transition.isEnableLoad(transitioncontext));
            transitioncontext.getLoadMacroButton().setToolTipText(transition.getLoadTooltip(transitioncontext));
            }

        // Always allow SaveMacros
        if (transitioncontext.getSaveMacroButton() != null)
            {
            transitioncontext.getSaveMacroButton().setEnabled(transition.isEnableSave(transitioncontext));
            transitioncontext.getSaveMacroButton().setToolTipText(transition.getSaveTooltip(transitioncontext));
            }

        //------------------------------------------------------------------------------------------
        // Commander Mode, Panels
        //------------------------------------------------------------------------------------------
        // The CommandBuilder ViewerContainer must show the CommandLifecycleLog at all times
        // except when in RecordMacro mode, and when ShowMacro is executed.
        // Only CommanderMode:ShowMacro applies here.
        }


    /***********************************************************************************************
     * Show the Instrument Status, indicating BUSY or Unsaved Data if appropriate.
     *
     * @param transitioncontext
     * @param transition
     */

    private static void showInstrumentStatus(final CommandProcessorContextInterface transitioncontext,
                                             final InstrumentStateTransition transition)
        {
        // Show any activity if possible, remember that the InstrumentState does not change until
        // the **end** of the transition, so the ActivityIndicator must use the NextState
        // to determine the activity to display.
        if ((transitioncontext.getActivityIndicatorList() != null)
            && (!transitioncontext.getActivityIndicatorList().isEmpty()))
            {
            final Iterator<ActivityIndicatorUIComponentInterface> iterIndicators;

            iterIndicators = transitioncontext.getActivityIndicatorList().iterator();

            // Notify all ActivityIndicators
            while (iterIndicators.hasNext())
                {
                final ActivityIndicatorUIComponentInterface indicator;

                indicator = iterIndicators.next();

                indicator.addStateTransition(transitioncontext, transition);
                }
            }
        }


    /***********************************************************************************************
     * Control the Viewer panel, selecting a CommandLifecycleLog or MacroViewer.
     *
     * @param context
     * @param transition
     */

//    private static void controlViewerPanel(final CommandProcessorContextInterface context,
//                                           final InstrumentStateTransition transition)
//        {
//        if (context.getViewerContainer() != null)
//            {
//            boolean boolShowMacroViewer;
//
//            boolShowMacroViewer = false;
//
//            // Only enable the MacroViewer if the Macro Button is set in the Transition
//            // AND there is a Module and Macro selected
//            if (context.getShowMacroButton() != null)
//                {
//                boolShowMacroViewer = ((context.getSelectedModule() != null)
//                                        && (context.isSelectedMacro())
//                                        && (transition.isEnableShow(context)));
//                }
//
//            if (boolShowMacroViewer)
//                {
//                context.getViewerContainer().removeAll();
//                context.getViewerContainer().add((Component) context.getMacroViewer(),
//                                                  BorderLayout.CENTER);
//                context.setViewingCommands(false);
//                }
//            else
//                {
//                context.getViewerContainer().removeAll();
//                context.getViewerContainer().add((Component) context.getCommandLifecycleLog(),
//                                                  BorderLayout.CENTER);
//                context.setViewingCommands(true);
//                }
//
//            // Update the Commands Tab, if we have one...
//            if ((context.getObservatoryInstrument() != null)
//                && (context.getObservatoryInstrument().getInstrumentPanel() != null)
//                && (context.getObservatoryInstrument().getInstrumentPanel().getCommandsTab() != null))
//                {
//                context.getObservatoryInstrument().getInstrumentPanel().getCommandsTab().runUI();
//                }
//
//            // Tell the MacroViewr that something has changed
//            if (context.getMacroViewer() != null)
//                {
//                context.getMacroViewer().macroChanged();
//                }
//            }
//        }


    /***********************************************************************************************
     * Debug the State of the InstrumentStateMachine.
     *
     * @param debugcontext
     * @param currentstate
     * @param nextstate
     * @param message
     */

    private static void debugStateFSM(final CommandProcessorContextInterface debugcontext,
                                      final InstrumentState currentstate,
                                      final InstrumentState nextstate,
                                      final String message)
        {
        // Don't log this if it is a loopback to the same state
        LOGGER.debugStateEvent(((LOADER_PROPERTIES.isStateDebug())
                                    && (!currentstate.getName().equals(nextstate.getName()))),
                               "InstrumentStateMachine.debugStateFSM() "
                                   + debugcontext.getInstrument().getIdentifier() + ": "
                                   + message
                                   + "[currentstate=" + currentstate.getName()
                                   + "] --> [nextstate=" + nextstate.getName()
                                   + "] [recordmacro=" + debugcontext.getObservatory().isRecordMacroMode() + "]");
        }


    /***********************************************************************************************
     * Construct an InstrumentStateMachine, using a Transition table and a Context to control.
     *
     * @param transitions
     * @param fsmcontext
     */

    public InstrumentStateMachine(final Hashtable<InstrumentState, List<InstrumentStateTransition>> transitions,
                                  final CommandProcessorContextInterface fsmcontext)
        {
        // Injections
        this.tableTransitions = transitions;
        this.context = fsmcontext;
        }


    /***********************************************************************************************
     * The Finite State Machine which controls the Observatory and its Instruments.
     * Given a CurrentState and NextState, make the appropriate InstrumentStateTransition
     * if possible, and return the approved NextState.
     * Any requests for invalid transitions return a NextState of ERROR,
     * which must be handled by the caller to regain control of the Instrument.
     *
     * @param currentstate
     * @param nextstate
     *
     * @return InstrumentState
     */

    public InstrumentState doTransition(final InstrumentState currentstate,
                                        final InstrumentState nextstate)
        {
        final String SOURCE = "InstrumentStateMachine.doTransition() ";
        InstrumentState nextState;

        nextState = InstrumentState.ERROR;

        if ((currentstate != null)
            && (nextstate != null))
            {
            InstrumentStateTransition validTransition;

            validTransition = null;

            if ((getTransitionsTable() != null)
                && (getContext() != null)
                && (getContext().getObservatory() != null))
                {
                final List<InstrumentStateTransition> listValidTransitions;

                // Look up the transition from the CurrentState to the requestednextstate
                listValidTransitions = getTransitionsTable().get(currentstate);

                // See if the List contains a transition containing requestednextstate
                for (int i = 0;
                     ((validTransition == null)
                         && (listValidTransitions != null)
                         && (i < listValidTransitions.size()));
                     i++)
                    {
                    final InstrumentStateTransition transition;

                    transition = listValidTransitions.get(i);

                    if ((transition != null)
                        && (transition.getNextState().getName().equals(nextstate.getName())))
                        {
                        validTransition = transition;
                        }
                    }
                }

            // Did we find it?
            if (validTransition != null)
                {
                // Process the Transition's internal state, depending on the Observatory mode
                // The Instrument is not actually *put* into the next state until the transition
                // is finished, so beware interrogating with getInstrumentState() at the wrong time
                // For instance, InstrumentState.isOccupied() looks at the *Instrument* for the state,
                // not the current or next states in the Context

                showInstrumentStatus(getContext(), validTransition);

                processTransition(getContext(), validTransition);

                nextState = validTransition.getNextState();

                debugStateFSM(getContext(),
                              currentstate, nextstate,
                              SOURCE);
                }
            else
                {
                // The requested transition was never found, so leave in state ERROR
                LOGGER.error(SOURCE + "The requested transition was not found [currentstate="
                                    + currentstate.getName()
                                    + "] --> [nextstate=" + nextstate.getName() + "]");
                }
            }
        else
            {
            LOGGER.error(SOURCE + "Invalid State Parameters");
            }

        return (nextState);
        }


    /***********************************************************************************************
     * Get the table of InstrumentStateTransitions.
     *
     * @return Hashtable<InstrumentState, List<InstrumentStateTransition>>
     */

    private Hashtable<InstrumentState, List<InstrumentStateTransition>> getTransitionsTable()
        {
        return (this.tableTransitions);
        }


    /***********************************************************************************************
     * Clear the InstrumentStateTransition table Transition Lists, for initialisation.
     */

    public void clearTransitionTable()
        {
        if (getTransitionsTable() != null)
            {
            final Enumeration<InstrumentState> enumKeys;

            enumKeys = getTransitionsTable().keys();

            while (enumKeys.hasMoreElements())
                {
                final InstrumentState state;

                state = enumKeys.nextElement();

                // Clear the List for this CurrentState
                getTransitionsTable().get(state).clear();
                }
            }
        }


    /***********************************************************************************************
     * Get the CommandProcessorContext to use with this InstrumentStateMachine.
     * The Context contains those items affected by the Outputs of the FSM.
     *
     * @return CommandProcessorContextInterface
     */

    private CommandProcessorContextInterface getContext()
        {
        return (this.context);
        }
    }
