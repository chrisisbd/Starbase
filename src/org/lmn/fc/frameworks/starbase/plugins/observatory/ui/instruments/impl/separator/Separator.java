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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.separator;

import org.lmn.fc.common.utilities.files.FileUtilities;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.*;
import org.lmn.fc.model.plugins.AtomPlugin;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;


/***********************************************************************************************
 * A Separator.
 */

public final class Separator extends AbstractObservatoryInstrument
                             implements ObservatoryInstrumentInterface
    {
    private static final String PREFIX_ICON_HEADER = "separator-header-";


    /***********************************************************************************************
     * Construct a Separator.
     *
     * @param instrument
     * @param plugin
     * @param hostui
     * @param resourcekey
     */

    public Separator(final Instrument instrument,
                     final AtomPlugin plugin,
                     final ObservatoryUIInterface hostui,
                     final String resourcekey)
        {
        super(instrument, plugin, hostui, resourcekey);
        }


    /***********************************************************************************************
     * Initialise the Separator.
     */

    public void initialise()
        {
        final InstrumentUIComponentDecoratorInterface controlPanel;
        final InstrumentUIComponentDecoratorInterface instrumentPanel;
        final SeparatorPanel separatorPanel;
        final StringBuffer bufferIconName;

        // Read the Resources for the Separator
        readResources();

        super.initialise();

        // Create and initialise the SeparatorControlPanel for the Control Panel
        // The appearance will depend on the SeparatorPanel
        // which is derived from the ResourceKey, a bodge to avoid changing the schema

        separatorPanel = SeparatorPanel.findSeparatorFromResourceKey(getResourceKey());

        // Control Panels show the Instrument Name
        controlPanel = new SeparatorControlPanel(this,
                                                 getInstrument(),
                                                 getHostUI(),
                                                 (TaskPlugin)getHostAtom().getRootTask(),
                                                 getFontData(),
                                                 getColourData(),
                                                 getResourceKey(),
                                                 separatorPanel,
                                                 getInstrument().getName());
        // All Separators have blank *display* names
        setControlPanel(controlPanel, EMPTY_STRING);
        addInstrumentStateChangedListener(controlPanel);
        getControlPanel().initialiseUI();

        // Make the filename of the Separator Header icon for the Instrument Panel
        // This is independent of the SeparatorPanel, since the icon is always shown
        bufferIconName = new StringBuffer();
        bufferIconName.append(PREFIX_ICON_HEADER);
        bufferIconName.append(getInstrument().getName().toLowerCase());
        bufferIconName.append(DOT);
        bufferIconName.append(FileUtilities.png);

        // The InstrumentPanel shows the gradient background,
        // with a HeaderUIComponent with a header icon
        instrumentPanel = new SeparatorInstrumentPanel(this,
                                                       getInstrument(),
                                                       getHostUI(),
                                                       (TaskPlugin)getHostAtom().getRootTask(),
                                                       getResourceKey(),
                                                       bufferIconName.toString(),
                                                       getFontData(),
                                                       getColourData(),
                                                       true,
                                                       true);
        setInstrumentPanel(instrumentPanel);
        addInstrumentStateChangedListener(instrumentPanel);
        getInstrumentPanel().initialiseUI();
        }
    }
