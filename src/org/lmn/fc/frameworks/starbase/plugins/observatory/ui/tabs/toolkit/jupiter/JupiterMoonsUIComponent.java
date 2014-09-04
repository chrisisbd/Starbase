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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.toolkit.jupiter;

import org.lmn.fc.common.actions.ContextAction;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryUIInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.InstrumentUIComponentDecorator;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.model.registry.NavigationUtilities;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;
import org.lmn.fc.ui.UIComponentPlugin;
import org.lmn.fc.ui.layout.BoxLayoutFixed;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;


/***************************************************************************************************
 * JupiterMoonsUIComponent.
 */

public final class JupiterMoonsUIComponent extends InstrumentUIComponentDecorator
                                           implements UIComponentPlugin,
                                                      ActionListener,
                                                      ComponentListener
    {
    // String Resources
    private static final String CMD_PAUSE = "Pause";
    private static final String CMD_RESET = "Reset";
    private static final String CMD_REVERSE = "Reverse";
    private static final String CMD_RESUME = "Resume";
    private static final String CMD_FORWARD = "Forward";

    private static final int WIDTH_BUTTON = 120;
    private static final Dimension DIM_BUTTON_COMMAND = new Dimension(WIDTH_BUTTON, HEIGHT_TOOLBAR_ICON);

    // UI
    private JPanel panelMoons;
    private JToolBar toolBar;
    private JButton pauseButton;
    private JButton resetButton;
    private JButton reverseButton;
    private final JButton buttonToolbarRecalculate;
    private int intToolbarComponentCount;

    private Moons moons;
    private Thread threadMoons;


    /***********************************************************************************************
     * JupiterMoonsUIComponent.
     *
     * @param hostinstrument
     * @param instrumentxml
     * @param hostui
     * @param task
     * @param font
     * @param colour
     * @param resourcekey
     */

    public JupiterMoonsUIComponent(final ObservatoryInstrumentInterface hostinstrument,
                                   final Instrument instrumentxml,
                                   final ObservatoryUIInterface hostui,
                                   final TaskPlugin task,
                                   final FontInterface font,
                                   final ColourInterface colour,
                                   final String resourcekey)
        {
        super(hostinstrument,
              instrumentxml,
              hostui,
              task,
              font,
              colour,
              resourcekey, 1);

        this.panelMoons = null;
        this.toolBar = null;
        this.intToolbarComponentCount = 0;

        // Add the basic Toolbar button
        buttonToolbarRecalculate = new JButton();
        getRecalculateButton().setBorder(BORDER_BUTTON);
        getRecalculateButton().setText(EMPTY_STRING);

        // These will get overridden later...
        getRecalculateButton().setAction(null);
        getRecalculateButton().setToolTipText(EMPTY_STRING);

        // Ensure that no text appears next to the Icon...
        getRecalculateButton().setHideActionText(true);

        // The button will be enabled when a valid Julian Date is entered
        getRecalculateButton().setEnabled(false);
        }


    /***********************************************************************************************
     * Initialise this UIComponent.
     */

    public void initialiseUI()
        {
        // DO NOT USE super.initialiseUI()

        threadMoons = null;

        panelMoons = new JPanel();
        panelMoons.setLayout(new BoxLayoutFixed(panelMoons, BoxLayoutFixed.PAGE_AXIS));

        // Remove everything from the host UIComponent
        removeAll();

        // Colours
        setBackground(Color.black);

        // Create the JToolBar and initialise it
        setToolBar(new JToolBar());
        getToolBar().setFloatable(false);
        getToolBar().setMinimumSize(DIM_TOOLBAR_SIZE);
        getToolBar().setPreferredSize(DIM_TOOLBAR_SIZE);
        getToolBar().setMaximumSize(DIM_TOOLBAR_SIZE);

        initialiseToolbar(getToolBar());

        // Create the Moons with the current width of the host UI
        moons = new Moons(getWidth(), 150);

        panelMoons.add(Box.createVerticalGlue());
        panelMoons.add(moons);
        panelMoons.add(Box.createVerticalGlue());

        // Put the components together
        add(getToolBar(), BorderLayout.NORTH);
        add(panelMoons, BorderLayout.CENTER);

        addComponentListener(this);
        }


    /***********************************************************************************************
     * Run this UIComponent.
     */

    public void runUI()
        {
        removeAll();
        moons = new Moons(getWidth(), 150);
        add(getToolBar(), BorderLayout.NORTH);
        add(moons, BorderLayout.CENTER);
        threadMoons = new Thread(moons);
        threadMoons.start();
        }


    /***********************************************************************************************
     * Stop this UIComponent.
     */

    public void stopUI()
        {
        if (threadMoons != null)
            {
            try
                {
                threadMoons.join(200);
                threadMoons = null;
                }

            catch (InterruptedException exception)
                {
                // Ignore this Exception
                }
            }
        }


    /***********************************************************************************************
     * Dispose of the UIComponent.
     */

    public void disposeUI()
        {
        stopUI();

        if (getToolBar() != null)
            {
            getToolBar().removeAll();
            setToolBar(null);
            }

        removeAll();

        removeComponentListener(this);

        moons = null;
        threadMoons = null;

        super.disposeUI();
        }


    /***********************************************************************************************
     * Initialise the Toolbar.
     *
     * @param toolbar
     */

    private void initialiseToolbar(final JToolBar toolbar)
        {
        if (toolbar != null)
            {
            final ContextAction actionRecalculate;
            int intCount;

            toolbar.removeAll();
            intCount = 0;

            toolbar.addSeparator(DIM_TOOLBAR_SEPARATOR);
            intCount++;

            pauseButton = new JButton(CMD_PAUSE);
            pauseButton.setMinimumSize(DIM_BUTTON_COMMAND);
            pauseButton.setMaximumSize(DIM_BUTTON_COMMAND);
            pauseButton.setPreferredSize(DIM_BUTTON_COMMAND);
            pauseButton.addActionListener(this);
            pauseButton.setActionCommand(CMD_PAUSE);
            pauseButton.setForeground(getColourData().getColor());
            pauseButton.setFont(getFontData().getFont());
            toolbar.add(pauseButton);
            toolbar.addSeparator(DIM_TOOLBAR_SEPARATOR);
            intCount++;

            resetButton = new JButton(CMD_RESET);
            resetButton.setMinimumSize(DIM_BUTTON_COMMAND);
            resetButton.setMaximumSize(DIM_BUTTON_COMMAND);
            resetButton.setPreferredSize(DIM_BUTTON_COMMAND);
            resetButton.addActionListener(this);
            resetButton.setActionCommand(CMD_RESET);
            resetButton.setForeground(getColourData().getColor());
            resetButton.setFont(getFontData().getFont());
            toolbar.add(resetButton);
            toolbar.addSeparator(DIM_TOOLBAR_SEPARATOR);
            intCount++;

            reverseButton = new JButton(CMD_REVERSE);
            reverseButton.setMinimumSize(DIM_BUTTON_COMMAND);
            reverseButton.setMaximumSize(DIM_BUTTON_COMMAND);
            reverseButton.setPreferredSize(DIM_BUTTON_COMMAND);
            reverseButton.addActionListener(this);
            reverseButton.setActionCommand(CMD_REVERSE);
            reverseButton.setForeground(getColourData().getColor());
            reverseButton.setFont(getFontData().getFont());
            toolbar.add(reverseButton);
            toolbar.addSeparator(DIM_TOOLBAR_SEPARATOR);
            intCount++;

            //getRecalculateButton().setAction(actionRecalculate);
            //getRecalculateButton().setToolTipText((String) actionRecalculate.getValue(Action.SHORT_DESCRIPTION));
            getRecalculateButton().setEnabled(false);
            toolbar.add(getRecalculateButton());
            intCount++;

            setToolbarComponentCount(intCount);
            NavigationUtilities.updateComponentTreeUI(toolbar);
            }
        }


    /***********************************************************************************************
     * Handle the buttons.
     *
     * @param event
     */

    public void actionPerformed(final ActionEvent event)
        {
        final String command;

        command = event.getActionCommand();

        if (CMD_PAUSE.equals(command))
            {
            if (moons.isPaused())
                {
                moons.setPaused(false);
                pauseButton.setText(CMD_PAUSE);
                }
            else
                {
                moons.setPaused(true);
                pauseButton.setText(CMD_RESUME);
                }
            }
        else if (CMD_RESET.equals(command))
            {
            moons.reset();
            }
        else if (CMD_REVERSE.equals(command))
            {
            if (moons.isForward())
                {
                moons.setForward(false);
                reverseButton.setText(CMD_FORWARD);
                }
            else
                {
                moons.setForward(true);
                reverseButton.setText(CMD_REVERSE);
                }
            }
        }


    /***********************************************************************************************
     * Get the JToolBar.
     *
     * @return JToolBar
     */

    private JToolBar getToolBar()
        {
        return (this.toolBar);
        }


    /***********************************************************************************************
     * Set the JToolBar.
     *
     * @param toolbar
     */

    private void setToolBar(final JToolBar toolbar)
        {
        this.toolBar = toolbar;
        }


    /***********************************************************************************************
     * Get the JButton used to recalculate the Date.
     *
     * @return JButton
     */

    private JButton getRecalculateButton()
        {
        return (this.buttonToolbarRecalculate);
        }


    /***********************************************************************************************
     * Get the ToolbarComponentCount.
     *
     * @return int
     */

    private int getToolbarComponentCount()
        {
        return intToolbarComponentCount;
        }


    /***********************************************************************************************
     * Set the ToolbarComponentCount.
     *
     * @param count
     */

    private void setToolbarComponentCount(final int count)
        {
        this.intToolbarComponentCount = count;
        }


    /**
     * Invoked when the component's size changes.
     */
    public void componentResized(ComponentEvent e)
        {
        //System.out.println("JUPITER MOONS RESIZED width=" + getWidth() + " height=" + getHeight() );
        }


    /**
     * Invoked when the component's position changes.
     */
    public void componentMoved(ComponentEvent e)
        {
        //System.out.println("JUPITER MOONS MOVED");
        }


    /**
     * Invoked when the component has been made visible.
     */
    public void componentShown(ComponentEvent e)
        {
        //System.out.println("JUPITER MOONS SHOWN");
        }


    /**
     * Invoked when the component has been made invisible.
     */
    public void componentHidden(ComponentEvent e)
        {
        //System.out.println("JUPITER MOONS HIDDEN");
        }
    }
