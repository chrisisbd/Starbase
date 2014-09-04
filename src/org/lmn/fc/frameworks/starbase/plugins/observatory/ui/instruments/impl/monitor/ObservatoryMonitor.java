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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.monitor;

import org.lmn.fc.common.utilities.time.Chronos;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.*;
import org.lmn.fc.frameworks.starbase.portcontroller.PortControllerInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.events.PortControllerHeartbeatEvent;
import org.lmn.fc.frameworks.starbase.portcontroller.events.PortControllerHeartbeatListener;
import org.lmn.fc.frameworks.starbase.portcontroller.impl.PortController;
import org.lmn.fc.model.actions.ActionStatus;
import org.lmn.fc.model.plugins.AtomPlugin;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;


/***********************************************************************************************
 * The ObservatoryMonitor.
 */

public final class ObservatoryMonitor extends AbstractObservatoryInstrument
                                      implements ObservatoryInstrumentInterface,
                                                 PortControllerHeartbeatListener,
                                                 ObservatoryLogInterface
    {
    // This cannot be in FrameworkSingletons because it won't be loaded at the right time...
    private static final PortControllerInterface PORT_CONTROLLER = PortController.getInstance();

    private int intHeartbeatCounter;
    private boolean boolHeartbeat;
    private boolean boolMonitorCommands;
    private boolean boolMonitorPorts;
    private boolean boolMonitorMemory;
    private boolean boolJavaConsoleInstalled;


    /***********************************************************************************************
     * Construct a ObservatoryMonitor.
     *
     * @param instrument
     * @param plugin
     * @param hostui
     * @param resourcekey
     */

    public ObservatoryMonitor(final Instrument instrument,
                              final AtomPlugin plugin,
                              final ObservatoryUIInterface hostui,
                              final String resourcekey)
        {
        super(instrument, plugin, hostui, resourcekey);

        this.intHeartbeatCounter = 0;
        this.boolHeartbeat = true;
        this.boolMonitorCommands = false;
        this.boolMonitorPorts = false;
        this.boolMonitorMemory = false;
        this.boolJavaConsoleInstalled = false;
        }


    /***********************************************************************************************
     * Initialise the ObservatoryMonitor.
     */

    public void initialise()
        {
        final InstrumentUIComponentDecoratorInterface controlPanel;
        final InstrumentUIComponentDecoratorInterface instrumentPanel;

        // Read the Resources for the ObservatoryMonitor
        readResources();

        this.boolHeartbeat = true;

        super.initialise();

        // Create and initialise the ControlPanel
        controlPanel = new ObservatoryMonitorControlPanel(this,
                                                          getInstrument(),
                                                          getHostUI(),
                                                          (TaskPlugin)getHostAtom().getRootTask(),
                                                          getFontData(),
                                                          getColourData(),
                                                          getResourceKey());
        setControlPanel(controlPanel,
                        getInstrument().getName());
        addInstrumentStateChangedListener(controlPanel);
        getControlPanel().initialiseUI();

        // Create a ObservatoryMonitorInstrumentPanel and initialise it
        instrumentPanel = new ObservatoryMonitorInstrumentPanel(this,
                                                                getInstrument(),
                                                                getHostUI(),
                                                                (TaskPlugin)getHostAtom().getRootTask(),
                                                                getFontData(),
                                                                getColourData(),
                                                                getResourceKey());
        setInstrumentPanel(instrumentPanel);
        addInstrumentStateChangedListener(instrumentPanel);
        getInstrumentPanel().initialiseUI();

        PORT_CONTROLLER.addPortControllerHeartbeatListener(this);
        }


    /***********************************************************************************************
     * Shutdown the ObservatoryMonitor after use.
     */

    public synchronized void dispose()
        {
        PORT_CONTROLLER.removePortControllerHeartbeatListener(this);

        stop();

        super.dispose();
        }


    /***********************************************************************************************
     * Start this ObservatoryInstrument.
     *
     * @return boolean
     */

    public synchronized boolean start()
        {
        REGISTRY.addActionToList(REGISTRY_MODEL.getLoggedInUser(),
                                 Chronos.getSystemDateNow(),
                                 Chronos.getSystemTimeNow(),
                                 "Welcome to the Observatory Monitor - this list will show any Actions which need attention",
                                 ActionStatus.INFO);

        return (super.start());
        }


    /***********************************************************************************************
     * Indicate that there has been a PortControllerHeartbeatEvent.
     *
     * @param event
     */

    public void heartBeat(final PortControllerHeartbeatEvent event)
        {
//        if ((InstrumentState.isDoingSomething(this))
//            && (PORT_CONTROLLER.isRunning())
//            && (getOnButton() != null)
//            && (!getOnButton().isEnabled())
//            && (intHeartbeatCounter < PortController.LATENCY_MILLISEC))
//            {
//            if (boolHeartbeat)
//                {
//                getOnButton().setBackground(getOnButton().getBackground().brighter());
//                }
//            else
//                {
//                getOnButton().setBackground(getOnButton().getBackground().darker());
//                }
//
//            intHeartbeatCounter++;
//            }
//        else
//            {
//            intHeartbeatCounter = 0;
//            }
//
//        boolHeartbeat = !boolHeartbeat;
        }


    /***********************************************************************************************
     * Indicate if the CommandMonitor is running.
     *
     * @return boolean
     */

    public boolean isCommandMonitorRunning()
        {
        return (this.boolMonitorCommands);
        }


    /***********************************************************************************************
     * Set the CommandMonitor running.
     *
     * @param monitor
     */

    public void setCommandMonitorRunning(final boolean monitor)
        {
        this.boolMonitorCommands = monitor;
        }


    /***********************************************************************************************
     * Indicate if the PortMonitor is running.
     *
     * @return boolean
     */

    public boolean isPortMonitorRunning()
        {
        return (this.boolMonitorPorts);
        }


    /***********************************************************************************************
     * Set the PortMonitor running.
     *
     * @param monitor
     */

    public void setPortMonitorRunning(final boolean monitor)
        {
        this.boolMonitorPorts = monitor;
        }


    /***********************************************************************************************
     * Indicate if the MemoryMonitor is running.
     *
     * @return boolean
     */

    public boolean isMemoryMonitorRunning()
        {
        return (this.boolMonitorMemory);
        }


    /***********************************************************************************************
     * Set the MemoryMonitor running.
     *
     * @param monitor
     */

    public void setMemoryMonitorRunning(final boolean monitor)
        {
        this.boolMonitorMemory = monitor;
        }


    /***********************************************************************************************
     * Indicate if the JavaConsole is installed.
     *
     * @return boolean
     */

    public boolean isJavaConsoleInstalled()
        {
        return (this.boolJavaConsoleInstalled);
        }


    /***********************************************************************************************
     * Install the JavaConsole
     *
     * @param installed
     */

    public void setJavaConsoleInstalled(final boolean installed)
        {
        this.boolJavaConsoleInstalled = installed;
        }


    /**********************************************************************************************/
    /* Utilities                                                                                  */
    /***********************************************************************************************
     *  Read all the Resources required by the ObservatoryMonitor.
     */

    public void readResources()
        {
        //LOGGER.debugTimedEvent("ObservatoryMonitor [ResourceKey=" + getResourceKey() + "]");

        super.readResources();
        }
    }
