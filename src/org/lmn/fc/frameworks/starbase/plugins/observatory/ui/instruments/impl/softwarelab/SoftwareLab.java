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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.softwarelab;

import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.AbstractObservatoryInstrument;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.InstrumentUIComponentDecoratorInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryUIInterface;
import org.lmn.fc.model.plugins.AtomPlugin;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;


/***********************************************************************************************
 * The SoftwareLab.
 */

public final class SoftwareLab extends AbstractObservatoryInstrument
                               implements ObservatoryInstrumentInterface
    {
    private boolean boolJavaConsoleInstalled;


    /***********************************************************************************************
     * Construct a SoftwareLab.
     *
     * @param instrument
     * @param plugin
     * @param hostui
     * @param resourcekey
     */

    public SoftwareLab(final Instrument instrument,
                       final AtomPlugin plugin,
                       final ObservatoryUIInterface hostui,
                       final String resourcekey)
        {
        super(instrument, plugin, hostui, resourcekey);

        this.boolJavaConsoleInstalled = false;
        }


    /***********************************************************************************************
     * Initialise the SoftwareLab.
     */

    public void initialise()
        {
        final InstrumentUIComponentDecoratorInterface controlPanel;
        final InstrumentUIComponentDecoratorInterface instrumentPanel;

        // Read the Resources for the SoftwareLab
        readResources();

        super.initialise();

        // Create and initialise the ControlPanel
        controlPanel = new SoftwareLabControlPanel(this,
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

        // Create a SoftwareLabInstrumentPanel and initialise it
        instrumentPanel = new SoftwareLabInstrumentPanel(this,
                                                         getInstrument(),
                                                         getHostUI(),
                                                         (TaskPlugin)getHostAtom().getRootTask(),
                                                         getFontData(),
                                                         getColourData(),
                                                         getResourceKey());
        setInstrumentPanel(instrumentPanel);
        addInstrumentStateChangedListener(instrumentPanel);
        getInstrumentPanel().initialiseUI();
        }


    /***********************************************************************************************
     * Shutdown the SoftwareLab after use.
     */

    public synchronized void dispose()
        {
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
        return (super.start());
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
     *  Read all the Resources required by the SoftwareLab.
     */

    public void readResources()
        {
        super.readResources();
        }
    }
