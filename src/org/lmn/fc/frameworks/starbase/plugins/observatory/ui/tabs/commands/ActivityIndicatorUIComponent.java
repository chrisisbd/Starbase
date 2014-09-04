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


import org.lmn.fc.common.utilities.files.FileUtilities;
import org.lmn.fc.common.utilities.misc.Utilities;
import org.lmn.fc.common.utilities.threads.SwingWorker;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.InstrumentState;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.InstrumentStateTransition;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.InstrumentHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.ActivityIndicatorUIComponentInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandProcessorContextInterface;
import org.lmn.fc.model.registry.RegistryModelUtilities;
import org.lmn.fc.ui.UIComponentPlugin;
import org.lmn.fc.ui.components.UIComponent;
import org.lmn.fc.ui.layout.BoxLayoutFixed;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;


/***************************************************************************************************
 * ActivityIndicatorUIComponent.
 */

public class ActivityIndicatorUIComponent extends UIComponent
                                          implements ActivityIndicatorUIComponentInterface
    {
    private static final Border BORDER_ACTIVITY_IDLE = null; // BorderFactory.createEtchedBorder(EtchedBorder.RAISED);
    private static final Border BORDER_ACTIVITY_ACTIVE = null;
    private static final Border BORDER_ACTIVITY_UNSAVED = null;
    private static final int STATUS_UPDATE_MILLIS = 100;

    // Injections
    private CommandProcessorContextInterface hostContext;

    private final Queue<InstrumentStateTransition> queueTransitions;

    private SwingWorker workerStatus;
    private boolean boolStatusRunning;
    private JLabel labelStatus;

    private final List<ImageIcon> listAnimatorIcons;
    private SwingWorker workerAnimator;
    private boolean boolAnimatorRunning;
    private JLabel labelActivity;


    /***********************************************************************************************
     * Create the Activity Indicator.
     *
     * @param context
     *
     * @return JLabel
     */

    private static JLabel createActivityComponent(final CommandProcessorContextInterface context)
        {
        final JLabel labelActivity;

        labelActivity = new JLabel()
            {
            // Enable Antialiasing in Java 1.5
            protected void paintComponent(final Graphics graphics)
                {
                final Graphics2D graphics2D;

                graphics2D = (Graphics2D) graphics;

                // For antialiasing text
                graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                                            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                super.paintComponent(graphics2D);
                }
            };

        labelActivity.setAlignmentX(Component.LEFT_ALIGNMENT);
        labelActivity.setAlignmentY(Component.CENTER_ALIGNMENT);
        labelActivity.setMinimumSize(DIM_ACTIVITY);
        labelActivity.setMaximumSize(DIM_ACTIVITY);
        labelActivity.setPreferredSize(DIM_ACTIVITY);
        labelActivity.setFont(context.getFontData().getFont());
        labelActivity.setHorizontalAlignment(JLabel.CENTER);
        labelActivity.setOpaque(true);

        // Start in the Idle state
        labelActivity.setBorder(BORDER_ACTIVITY_IDLE);
        labelActivity.setBackground(COLOR_ACTIVITY_BG_IDLE);
        labelActivity.setToolTipText(ObservatoryInstrumentInterface.TOOLTIP_ACTIVITY);
        labelActivity.setIcon(null);

        labelActivity.addMouseListener(new MouseAdapter()
            {
            @Override
            public void mouseClicked(final MouseEvent event)
                {
                // Clear the Unsaved Data flag, only if the icon is visible
                // Note that this clears the Instrument DAO flag

                if ((context != null)
                    && (context.getObservatoryInstrument() != null)
                    && (context.getObservatoryInstrument().getDAO() != null)
                    && (TOOLTIP_ACTIVITY_UNSAVED.equals(labelActivity.getToolTipText())))
                    {
                    context.getObservatoryInstrument().getDAO().setUnsavedData(false);
                    InstrumentHelper.notifyInstrumentChanged(context.getObservatoryInstrument().getDAO().getHostInstrument());
                    }
                }
            });

        return (labelActivity);
        }


    /***********************************************************************************************
     * Create the Command StatusIndicator.
     *
     * @param context
     *
     * @return JLabel
     */

    private static JLabel createStatusComponent(final CommandProcessorContextInterface context)
        {
        final JLabel labelStatus;

        labelStatus = new JLabel(InstrumentState.STOPPED.getStatus())
            {
            // Enable Antialiasing in Java 1.5
            protected void paintComponent(final Graphics graphics)
                {
                final Graphics2D graphics2D;

                graphics2D = (Graphics2D) graphics;

                // For antialiasing text
                graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                                            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                super.paintComponent(graphics2D);
                }
            };

        labelStatus.setAlignmentX(Component.LEFT_ALIGNMENT);
        labelStatus.setAlignmentY(Component.CENTER_ALIGNMENT);
        labelStatus.setMinimumSize(DIM_STATUS);
        labelStatus.setMaximumSize(DIM_STATUS);
        labelStatus.setPreferredSize(DIM_STATUS);
        labelStatus.setFont(context.getFontData().getFont());
        labelStatus.setBackground(COLOR_STATUS_BG_NORMAL);
        labelStatus.setForeground(COLOR_STATUS_FG_NORMAL);
        labelStatus.setHorizontalAlignment(JLabel.CENTER);
        labelStatus.setOpaque(true);
        labelStatus.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        labelStatus.setToolTipText(ObservatoryInstrumentInterface.TOOLTIP_INSTRUMENT_STATUS);

        return (labelStatus);
        }


    /***********************************************************************************************
     * Are there unsaved data in the DAO?
     * Indicate on the ActivityIndicator.
     */

    private static void checkUnsavedData(final ObservatoryInstrumentInterface obsinstrument,
                                         final JLabel activitylabel)
        {
        final String SOURCE = "ActivityIndicatorUIComponent.checkUnsavedData() ";

        // Note that this tests the Instrument DAO

        if ((obsinstrument.getDAO() != null)
            && (obsinstrument.getDAO().hasUnsavedData()))
            {
            final ImageIcon icon;

            LOGGER.debugStateEvent(LOADER_PROPERTIES.isStateDebug(),
                                   SOURCE + "Instrument DAO has unsaved data");

            icon = RegistryModelUtilities.getAtomIcon(obsinstrument.getHostAtom(),
                                                      ICON_ACTIVITY_UNSAVED);
            activitylabel.setIcon(icon);
            activitylabel.setBorder(BORDER_ACTIVITY_UNSAVED);
            activitylabel.setBackground(COLOR_ACTIVITY_BG_UNSAVED);
            activitylabel.setToolTipText(TOOLTIP_ACTIVITY_UNSAVED);
            }
        else
            {
            final ImageIcon icon;

            LOGGER.debugStateEvent(LOADER_PROPERTIES.isStateDebug(),
                                   SOURCE + "Show status of Idle");

            icon = RegistryModelUtilities.getAtomIcon(obsinstrument.getHostAtom(),
                                                      ICON_ACTIVITY_IDLE);
            activitylabel.setIcon(icon);
            activitylabel.setBorder(BORDER_ACTIVITY_IDLE);
            activitylabel.setBackground(COLOR_ACTIVITY_BG_IDLE);
            activitylabel.setToolTipText(TOOLTIP_ACTIVITY_IDLE);
            }
        }


    /***********************************************************************************************
     * A convenience method to indicate if the Instrument is 'occupied' executing a Command
     * in the **next** state to be entered, i.e. the one currently being processed.
     *
     * @param transition
     *
     * @return boolean
     */

    private static boolean isOccupiedInNextState(final InstrumentStateTransition transition)
        {
        return ((transition != null)
                && ((InstrumentState.BUSY.equals(transition.getNextState()))
                    || (InstrumentState.REPEATING.equals(transition.getNextState()))));
        }


    /***********************************************************************************************
     * Construct a ActivityIndicatorUIComponent.
     *
     * @param context
     */

    public ActivityIndicatorUIComponent(final CommandProcessorContextInterface context)
        {
        super();

        // Injections
        this.hostContext = context;

        this.queueTransitions = new ConcurrentLinkedQueue<InstrumentStateTransition>();

        this.workerStatus = null;
        this.boolStatusRunning = false;
        this.labelStatus = null;

        this.listAnimatorIcons = new ArrayList<ImageIcon>(10);
        this.workerAnimator = null;
        this.boolAnimatorRunning = false;
        this.labelActivity = null;
        }


    /**********************************************************************************************/
    /* UI State                                                                                   */
    /***********************************************************************************************
     * Initialise this UIComponent.
     */

    public void initialiseUI()
        {
        final String SOURCE = "ActivityIndicatorUIComponent.initialiseUI() ";

        super.initialiseUI();

        // Build the UI
        removeAll();
        setLayout(new BoxLayoutFixed(this, BoxLayoutFixed.X_AXIS));
        setBackground(UIComponentPlugin.DEFAULT_COLOUR_TAB_BACKGROUND.getColor());
        setAlignmentX(Component.LEFT_ALIGNMENT);
        setAlignmentY(Component.CENTER_ALIGNMENT);

        setMinimumSize(DIM_INDICATOR);
        setMaximumSize(DIM_INDICATOR);
        setPreferredSize(DIM_INDICATOR);

        labelActivity = createActivityComponent(getContext());
        add(getActivityComponent());
        add(Box.createHorizontalStrut(WIDTH_STRUT));
        labelStatus = createStatusComponent(getContext());
        add(getStatusComponent());

        // Clear the Transition Queue
        getTransitionQueue().clear();

        // Load the Icons ready for animation in BUSY etc.
        getAnimatorIcons().clear();

        for (int intIconIndex = 0;
             intIconIndex < ANIMATION_COUNT;
             intIconIndex++)
            {
            final ImageIcon icon;

            icon = RegistryModelUtilities.getAtomIcon(getContext().getObservatoryInstrument().getHostAtom(),
                                                      FILENAME_ICON_BUSY + intIconIndex + DOT + FileUtilities.png);
            getAnimatorIcons().add(icon);
            }

        // Stop any existing SwingWorkers (very unlikely)
        SwingWorker.disposeWorker(getStatusWorker(), true, STATUS_STOP_DELAY);
        setStatusRunning(false);
        SwingWorker.disposeWorker(getAnimatorWorker(), true, ANIMATOR_STOP_DELAY);
        setAnimatorRunning(false);

        // Now start a new Status Thread
        setStatusWorker(createStatusSwingWorker());
        setStatusRunning(true);
        getStatusWorker().start();

        LOGGER.debugStateEvent(LOADER_PROPERTIES.isStateDebug(),
                               SOURCE + "[ui.state=" + getUIState().getName()
                                      + "] [queue.size=" + getTransitionQueue().size() + "]");
        }


    /***********************************************************************************************
     * Run this UIComponent.
     */

    public void runUI()
        {
        final String SOURCE = "ActivityIndicatorUIComponent.runUI() ";

        super.runUI();

        // The Status Worker is still running, and is aware of the UI State

        LOGGER.debugStateEvent(LOADER_PROPERTIES.isStateDebug(),
                               SOURCE + "[ui.state=" + getUIState().getName()
                               + "] [queue.size=" + getTransitionQueue().size() + "]");
        }


    /***********************************************************************************************
     * Stop this UIComponent.
     */

    public void stopUI()
        {
        final String SOURCE = "ActivityIndicatorUIComponent.stopUI() ";

        // Stop any animation, if it can't be seen!
        // It will be recreated if necessary when runUI() is called
        SwingWorker.disposeWorker(getAnimatorWorker(), true, ANIMATOR_STOP_DELAY);
        setAnimatorRunning(false);

        super.stopUI();

        LOGGER.debugStateEvent(LOADER_PROPERTIES.isStateDebug(),
                               SOURCE + "[ui.state=" + getUIState().getName()
                                      + "] [queue.size=" + getTransitionQueue().size() + "]");
        }


    /***********************************************************************************************
     * Dispose of all components of this UIComponent.
     */

    public void disposeUI()
        {
        final String SOURCE = "ActivityIndicatorUIComponent.disposeUI() ";

        stopUI();

        if (getStatusWorker() != null)
            {
            setStatusRunning(false);
            SwingWorker.disposeWorker(getStatusWorker(), true, STATUS_STOP_DELAY);
            setStatusWorker(null);
            }

        if (getAnimatorIcons() != null)
            {
            getAnimatorIcons().clear();
            }

        if (getAnimatorWorker() != null)
            {
            setAnimatorRunning(false);
            SwingWorker.disposeWorker(getAnimatorWorker(), true, ANIMATOR_STOP_DELAY);
            setAnimatorWorker(null);
            }

        super.disposeUI();

        LOGGER.debugStateEvent(LOADER_PROPERTIES.isStateDebug(),
                               SOURCE + "[ui.state=" + getUIState().getName()
                                      + "] [queue.size=" + getTransitionQueue().size() + "]");
        }


    /**********************************************************************************************/
    /* Transition Management                                                                      */
    /***********************************************************************************************
     * Set the InstrumentStateTransition to be processed, using the CommandProcessorContext.
     * This is set from InstrumentStateMachine.
     *
     * @param context
     * @param transition
     */

    public void addStateTransition(final CommandProcessorContextInterface context,
                                   final InstrumentStateTransition transition)
        {
        final String SOURCE = "ActivityIndicatorUIComponent.addStateTransition() ";

        LOGGER.debugStateEvent(LOADER_PROPERTIES.isStateDebug(),
                               SOURCE + "[transition=" + transition.getCommandStatusText()
                                   + "] [host.instrument=" + context.getObservatoryInstrument().getInstrument().getIdentifier()
                                   + "] [ui.state=" + getUIState().getName()
                                   + "] [queue.size=" + getTransitionQueue().size() + "]");

        this.hostContext = context;

        // Inserts the specified element into this queue
        // if it is possible to do so immediately without violating capacity restrictions
        // We know that space *should* be immediately available
        getTransitionQueue().offer(transition);
        }


    /***********************************************************************************************
     * Create the Status SwingWorker.
     * This assumes the StatusIndicator is NOT NULL.
     */

    private SwingWorker createStatusSwingWorker()
        {
        final String SOURCE = "ActivityIndicatorUIComponent.createStatusSwingWorker() ";
        final SwingWorker worker;

        // Prepare the Status Thread
        worker = new SwingWorker(REGISTRY.getThreadGroup(), SOURCE)
            {
            /***************************************************************************************
             * Run the Status Thread.
             *
             * @return Object
             */

            public Object construct()
                {
                while ((isStatusRunning())
                    && (!isStopping()))
                    {
                    InstrumentStateTransition transition;

                    // Retrieves, but does not remove, the head of this queue,
                    // or returns null if this queue is empty.
                    // We know that an item *should* be immediately available
                    transition = getTransitionQueue().peek();

                    // See if we have some Status to display
                    if (transition != null)
                        {
                        transition = getTransitionQueue().remove();
                        LOGGER.debugStateEvent(LOADER_PROPERTIES.isStateDebug(),
                                               SOURCE + "Remove Transition from Queue [text=" + transition.getCommandStatusText()
                                                      + "] [queue.size=" + getTransitionQueue().size() + "]");

                        // Command Status Indicator
                        if ((getContext().getObservatoryInstrument() == null)
                            || (getContext().getObservatoryInstrument().getDAO() == null))
                            {
                            LOGGER.debugStateEvent(LOADER_PROPERTIES.isStateDebug(),
                                                   SOURCE + "No Instrument or DAO [dao.isnull="
                                                   + (getContext().getObservatoryInstrument().getDAO() == null) + "]");

                            // There's not much we can do... this should never happen?
                            getStatusComponent().setIcon(null);
                            getStatusComponent().setBackground(COLOR_STATUS_BG_NORMAL);
                            getStatusComponent().setForeground(COLOR_STATUS_FG_NORMAL);
                            getStatusComponent().setText(transition.getCommandStatusText(getContext()));
                            }
                        else
                            {
                            if (getContext().getObservatoryInstrument().getDAO().hasUnsavedData())
                                {
                                final StringBuffer buffer;

                                LOGGER.debugStateEvent(LOADER_PROPERTIES.isStateDebug(),
                                                       SOURCE + "Show status of unsaved data");

                                buffer = new StringBuffer();
                                buffer.append(transition.getCommandStatusText(getContext()));
                                buffer.append(MSG_UNSAVED_DATA);

                                getStatusComponent().setIcon(null);
                                getStatusComponent().setBackground(COLOR_STATUS_BG_ALERT);
                                getStatusComponent().setForeground(COLOR_STATUS_FG_ALERT);
                                getStatusComponent().setText(buffer.toString());
                                }
                            else
                                {
                                LOGGER.debugStateEvent(LOADER_PROPERTIES.isStateDebug(),
                                                       SOURCE + "Show status [text=" + transition.getCommandStatusText(getContext()) + "]");

                                getStatusComponent().setIcon(null);
                                getStatusComponent().setBackground(COLOR_STATUS_BG_NORMAL);
                                getStatusComponent().setForeground(COLOR_STATUS_FG_NORMAL);
                                getStatusComponent().setText(transition.getCommandStatusText(getContext()));
                                }
                            }

                        invalidate();
                        repaint();

                        // See if special Activity Animation is required
                        // Spawn another SwingWorker if so...
                        handleActivityAnimation(transition);
                        }

                    // If we are no longer running, leave immediately without waiting
                    if ((isStatusRunning())
                        && (!isStopping()))
                        {
                        Utilities.safeSleep(STATUS_UPDATE_MILLIS);
                        }
                    }

                return (null);
                }


            /***************************************************************************************
             * When the Status Thread stops.
             */

            public void finished()
                {
                LOGGER.debugStateEvent(LOADER_PROPERTIES.isStateDebug(),
                                       SOURCE + "ActivityIndicator Status Thread finished");
                setStatusRunning(false);
                }
            };

        return (worker);
        }


    /***********************************************************************************************
     * Handle the InstrumentState by showing whatever ActivityIndicator is required.
     * Do this on SwingWorkers to try to resolve issues about updating the UI!
     */

    private void handleActivityAnimation(final InstrumentStateTransition transition)
        {
        final String SOURCE = "ActivityIndicatorUIComponent.handleActivityAnimation() ";

        // We can't do much without this...
        if (getContext() != null)
            {
            LOGGER.debugStateEvent(LOADER_PROPERTIES.isStateDebug(),
                                   SOURCE + " [currentstate=" + transition.getCurrentState().getName()
                                   + "] [nextstate=" + transition.getNextState().getName()
                                   + "] [host.instrument=" + getContext().getObservatoryInstrument().getInstrument().getIdentifier()
                                   + "] [ui.state=" + getUIState().getName() + "]");

            //--------------------------------------------------------------------------------------
            // Activity Indicator

            if (getActivityComponent() != null)
                {
                // Work through the list of things we need to show as Activity
                // BUSY or REPEATING are the most important, and need an animation
                // Remember if we lost tab focus then the animator will have stopped and must be recreated
                // Unsaved DAO data is the next on the list, with an alert icon

                // Firstly, should we be animating?
                if (isOccupiedInNextState(transition))
                    {
                    // Do we have an Animator already?
                    if (getAnimatorWorker() != null)
                        {
                        // Do we have a running Animator?
                        if ((isAnimatorRunning())
                            && (!getAnimatorWorker().isStopping()))
                            {
                            // If so, we still need it, so leave it alone
                            LOGGER.debugStateEvent(LOADER_PROPERTIES.isStateDebug(),
                                                   SOURCE + "Animation required, have existing running Animator, so leave it alone");
                            }
                        else
                            {
                            // We have the remains of an old Animator, so make a new Animator and run it
                            LOGGER.debugStateEvent(LOADER_PROPERTIES.isStateDebug(),
                                                   SOURCE + "Animation required, but have old stopped Animator, so make a new Animator and run it");

                            setAnimatorWorker(createAnimatorSwingWorker(transition));
                            setAnimatorRunning(true);
                            getAnimatorWorker().start();
                            }
                        }
                    else
                        {
                        // NULL Animator, so make a new Animator and run it
                        LOGGER.debugStateEvent(LOADER_PROPERTIES.isStateDebug(),
                                               SOURCE + "Animation required, currently NULL Animator, so make a new Animator and run it");

                        setAnimatorWorker(createAnimatorSwingWorker(transition));
                        setAnimatorRunning(true);
                        getAnimatorWorker().start();
                        }
                    }
                else
                    {
                    LOGGER.debugStateEvent(LOADER_PROPERTIES.isStateDebug(),
                                           SOURCE + "No animation required, so Dispose of Animator Worker");

                    // Stop any animation, if it can't be seen!
                    SwingWorker.disposeWorker(getAnimatorWorker(), true, ANIMATOR_STOP_DELAY);

                    // Are there unsaved data in the DAO?
                    checkUnsavedData(getContext().getObservatoryInstrument(),
                                     getActivityComponent());
                    }
                }
            }
        else
            {
            LOGGER.error(SOURCE + "Invalid Parameters for Status update");
            }
        }


    /***********************************************************************************************
     * Create the Animator SwingWorker, used only for the ActivityIndicator.
     * This thread runs until the animation is complete.
     * This assumes the ActivityIndicator is NOT NULL.
     *
     * @param transition
     */

    private SwingWorker createAnimatorSwingWorker(final InstrumentStateTransition transition)
        {
        final String SOURCE = "ActivityIndicatorUIComponent.createAnimatorSwingWorker() ";
        final SwingWorker worker;

        getActivityComponent().setIcon(null);

        // Prepare the Animator Thread
        worker = new SwingWorker(REGISTRY.getThreadGroup(), SOURCE)
            {
            /***************************************************************************************
             * Run the Animator
             *
             * @return Object
             */

            public Object construct()
                {
                final long longDelayMillis;

                longDelayMillis = ANIMATION_CYCLE_MILLIS / getAnimatorIcons().size();

                // Note that this loop assumes it being used to indicate BUSY or REPEATING
                // The Instrument is not actually *put* into the next state until the transition
                // is finished, so beware interrogating with getInstrumentState() at the wrong time
                // For instance, InstrumentState.isOccupied() looks at the *Instrument* for the state,
                // not the current or next states in the Transition, so use isOccupiedInNextState()

                while ((isAnimatorRunning())
                       && (!isStopping())
                       && (isOccupiedInNextState(transition)))
                    {
                    getActivityComponent().setBorder(BORDER_ACTIVITY_ACTIVE);
                    getActivityComponent().setBackground(COLOR_ACTIVITY_BG_ACTIVE);
                    getActivityComponent().setToolTipText(TOOLTIP_ACTIVITY_ACTIVE + transition.getNextState().getStatus());

                    // If we are no longer occupied, leave immediately
                    for (int intIconIndex = 0;
                         ((intIconIndex < getAnimatorIcons().size())
                          && (isAnimatorRunning())
                          && (!isStopping()));
                         intIconIndex++)
                        {
                        final ImageIcon icon;

                        icon = getAnimatorIcons().get(intIconIndex);
                        getActivityComponent().setIcon(icon);
                        getActivityComponent().repaint();

                        // If we are no longer running, leave animation immediately without waiting
                        if ((isAnimatorRunning())
                            && (!isStopping()))
                            {
                            Utilities.safeSleep(longDelayMillis);
                            }
                        }
                    }

                return (null);
                }


            /***************************************************************************************
             * When the Animation Thread stops.
             */

            public void finished()
                {
                setAnimatorRunning(false);

                LOGGER.debugStateEvent(LOADER_PROPERTIES.isStateDebug(),
                                       SOURCE + "ActivityIndicator Animation Thread finished, go to checkUnsavedData()");

                // Are there unsaved data in the DAO?
                checkUnsavedData(getContext().getObservatoryInstrument(),
                                 getActivityComponent());
                }
            };

        return (worker);
        }


    /***********************************************************************************************
     * Get the Queue of InstrumentStateTransitions.
     *
     * @return Queue<InstrumentStateTransition>
     */

    private Queue<InstrumentStateTransition> getTransitionQueue()
        {
        return (this.queueTransitions);
        }


    /**********************************************************************************************/
    /* Status Indicator                                                                           */
    /***********************************************************************************************
     * Indicate if the Status Thread is running.
     *
     * @return boolean
     */

    private boolean isStatusRunning()
        {
        return (this.boolStatusRunning);
        }


    /***********************************************************************************************
     * Control the Status Thread state.
     *
     * @param running
     */

    private void setStatusRunning(final boolean running)
        {
        this.boolStatusRunning = running;
        }


    /***********************************************************************************************
     * Get the SwingWorker which handles the Status Indicator.
     *
     * @return SwingWorker
     */

    private SwingWorker getStatusWorker()
        {
        return (this.workerStatus);
        }


    /***********************************************************************************************
     * Set the SwingWorker which handles the Status Indicator.
     *
     * @param worker
     */

    private void setStatusWorker(final SwingWorker worker)
        {
        this.workerStatus = worker;
        }


    /**********************************************************************************************/
    /* Activity Animator                                                                          */
    /***********************************************************************************************
     * Get the List of Icons to be animated on the Activity Indicator.
     *
     * @return List<ImageIcon>
     */

    private List<ImageIcon> getAnimatorIcons()
        {
        return (this.listAnimatorIcons);
        }


    /***********************************************************************************************
     * Indicate if the Animator is running.
     *
     * @return boolean
     */

    private boolean isAnimatorRunning()
        {
        return (this.boolAnimatorRunning);
        }


    /***********************************************************************************************
     * Control the Animator state.
     *
     * @param running
     */

    private void setAnimatorRunning(final boolean running)
        {
        this.boolAnimatorRunning = running;
        }


    /***********************************************************************************************
     * Get the SwingWorker which handles the Animator.
     *
     * @return SwingWorker
     */

    private SwingWorker getAnimatorWorker()
        {
        return (this.workerAnimator);
        }


    /***********************************************************************************************
     * Set the SwingWorker which handles the Animator.
     *
     * @param worker
     */

    private void setAnimatorWorker(final SwingWorker worker)
        {
        this.workerAnimator = worker;
        }


    /**********************************************************************************************/
    /* UI Components                                                                              */
    /***********************************************************************************************
     * Get the CommandStatus JLabel.
     *
     * @return JLabel
     */

    public JLabel getStatusComponent()
        {
        return (this.labelStatus);
        }


    /***********************************************************************************************
     * Get the Activity JLabel.
     *
     * @return JLabel
     */

    public JLabel getActivityComponent()
        {
        return (this.labelActivity);
        }


    /**********************************************************************************************/
    /* Injections                                                                                 */
    /***********************************************************************************************
     * Get the CommandProcessorContext for this Activity.
     *
     * @return CommandProcessorContextInterface
     */

    private CommandProcessorContextInterface getContext()
        {
        return (this.hostContext);
        }
    }
