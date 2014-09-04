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

package org.lmn.fc.frameworks.starbase.plugins.workshop.ui.clock;

import info.clearthought.layout.TableLayout;
import org.lmn.fc.common.utilities.time.TimeSystem;
import org.lmn.fc.model.plugins.FrameworkPlugin;
import org.lmn.fc.model.registry.NavigationUtilities;
import org.lmn.fc.ui.components.UIComponent;
import org.lmn.fc.ui.widgets.impl.DigitalClock;

import javax.swing.*;
import java.awt.*;


/***************************************************************************************************
 * Test the Clocks!
 */

public final class ClockPanel extends UIComponent
    {
    private static final Color COLOR_STATUS     = new Color(42, 123, 198);
    private static final Font FONTSTATUS        = new Font("Dialog", Font.PLAIN, 5);
    private static final int SPACER = 25;

    // ToDo change this to a Vector?
    private final DigitalClock[] digitalClock;       // The array of clocks


    /***********************************************************************************************
     * Create a UIComponent containing one of each type of clock of the given size.
     *
     * @param plugin
     * @param dimension
     */

    public ClockPanel(final FrameworkPlugin plugin,
                      final Dimension dimension)
        {
        super();

        this.digitalClock = new DigitalClock[TimeSystem.values().length];

        try
            {
            initialiseClocks(plugin, dimension, Color.RED);
            }

        catch(Exception exception)
            {
            // ToDo exception -> IndicatorException
            exception.printStackTrace();
            }
        }


    /***********************************************************************************************
     *
     * @param framework
     * @param dimension
     * @param color
     *
     * @throws Exception
     */

    private void initialiseClocks(final FrameworkPlugin framework,
                                  final Dimension dimension,
                                  final Color color) throws Exception
        {
        final JPanel panelClocks;
        final double[][] size =
            {
                { // Columns
                TableLayout.FILL,
                TableLayout.PREFERRED,
                SPACER,
                TableLayout.PREFERRED,
                SPACER,
                TableLayout.PREFERRED,
                SPACER,
                TableLayout.PREFERRED,
                TableLayout.FILL
                },
                { // Rows
                TableLayout.FILL,
                TableLayout.PREFERRED,
                SPACER,
                TableLayout.PREFERRED,
                SPACER,
                TableLayout.PREFERRED,
                TableLayout.FILL
                }
            };

        panelClocks = new JPanel();
        panelClocks.setLayout(new TableLayout(size));

        // Create the (stopped) Digital Clocks and add them to the panel
        digitalClock[TimeSystem.LMT.getIndex()] = new DigitalClock(framework, dimension, "", "", "", "", INDICATOR_BORDER);
        digitalClock[TimeSystem.LMT.getIndex()].setTimeSystem(TimeSystem.LMT);
        digitalClock[TimeSystem.LMT.getIndex()].setValueForeground(color);
        digitalClock[TimeSystem.LMT.getIndex()].setStatusFont(FONTSTATUS);
        digitalClock[TimeSystem.LMT.getIndex()].setStatusColour(COLOR_STATUS);
        panelClocks.add(digitalClock[TimeSystem.LMT.getIndex()], "1, 1, c, c");

        digitalClock[TimeSystem.UT.getIndex()] = new DigitalClock(framework, dimension, "", "", "", "", INDICATOR_BORDER);
        digitalClock[TimeSystem.UT.getIndex()].setTimeSystem(TimeSystem.UT);
        digitalClock[TimeSystem.UT.getIndex()].setValueForeground(color);
        digitalClock[TimeSystem.UT.getIndex()].setStatusFont(FONTSTATUS);
        digitalClock[TimeSystem.UT.getIndex()].setStatusColour(COLOR_STATUS);
        panelClocks.add(digitalClock[TimeSystem.UT.getIndex()], "3, 1, c, c");

        digitalClock[TimeSystem.JD0.getIndex()] = new DigitalClock(framework, dimension, "", "", "", "", INDICATOR_BORDER);
        digitalClock[TimeSystem.JD0.getIndex()].setTimeSystem(TimeSystem.JD0);
        digitalClock[TimeSystem.JD0.getIndex()].setValueForeground(color);
        digitalClock[TimeSystem.JD0.getIndex()].setStatusFont(FONTSTATUS);
        digitalClock[TimeSystem.JD0.getIndex()].setStatusColour(COLOR_STATUS);
        panelClocks.add(digitalClock[TimeSystem.JD0.getIndex()], "5, 1, c, c");

        digitalClock[TimeSystem.JD.getIndex()] = new DigitalClock(framework, dimension, "", "", "", "", INDICATOR_BORDER);
        digitalClock[TimeSystem.JD.getIndex()].setTimeSystem(TimeSystem.JD);
        digitalClock[TimeSystem.JD.getIndex()].setValueForeground(color);
        digitalClock[TimeSystem.JD.getIndex()].setStatusFont(FONTSTATUS);
        digitalClock[TimeSystem.JD.getIndex()].setStatusColour(COLOR_STATUS);
        panelClocks.add(digitalClock[TimeSystem.JD.getIndex()], "7, 1, c, c");

        digitalClock[TimeSystem.GMST0.getIndex()] = new DigitalClock(framework, dimension, "", "", "", "", INDICATOR_BORDER);
        digitalClock[TimeSystem.GMST0.getIndex()].setTimeSystem(TimeSystem.GMST0);
        digitalClock[TimeSystem.GMST0.getIndex()].setValueForeground(color);
        digitalClock[TimeSystem.GMST0.getIndex()].setStatusFont(FONTSTATUS);
        digitalClock[TimeSystem.GMST0.getIndex()].setStatusColour(COLOR_STATUS);
        panelClocks.add(digitalClock[TimeSystem.GMST0.getIndex()], "1, 3, c, c");

        digitalClock[TimeSystem.GAST0.getIndex()] = new DigitalClock(framework, dimension, "", "", "", "", INDICATOR_BORDER);
        digitalClock[TimeSystem.GAST0.getIndex()].setTimeSystem(TimeSystem.GAST0);
        digitalClock[TimeSystem.GAST0.getIndex()].setValueForeground(color);
        digitalClock[TimeSystem.GAST0.getIndex()].setStatusFont(FONTSTATUS);
        digitalClock[TimeSystem.GAST0.getIndex()].setStatusColour(COLOR_STATUS);
        panelClocks.add(digitalClock[TimeSystem.GAST0.getIndex()], "3, 3, c, c");

        digitalClock[TimeSystem.GMST.getIndex()] = new DigitalClock(framework, dimension, "", "", "", "", INDICATOR_BORDER);
        digitalClock[TimeSystem.GMST.getIndex()].setTimeSystem(TimeSystem.GMST);
        digitalClock[TimeSystem.GMST.getIndex()].setValueForeground(color);
        digitalClock[TimeSystem.GMST.getIndex()].setStatusFont(FONTSTATUS);
        digitalClock[TimeSystem.GMST.getIndex()].setStatusColour(COLOR_STATUS);
        panelClocks.add(digitalClock[TimeSystem.GMST.getIndex()], "5, 3, c, c");

        digitalClock[TimeSystem.GAST.getIndex()] = new DigitalClock(framework, dimension, "", "", "", "", INDICATOR_BORDER);
        digitalClock[TimeSystem.GAST.getIndex()].setTimeSystem(TimeSystem.GAST);
        digitalClock[TimeSystem.GAST.getIndex()].setValueForeground(color);
        digitalClock[TimeSystem.GAST.getIndex()].setStatusFont(FONTSTATUS);
        digitalClock[TimeSystem.GAST.getIndex()].setStatusColour(COLOR_STATUS);
        panelClocks.add(digitalClock[TimeSystem.GAST.getIndex()], "7, 3, c, c");

        digitalClock[TimeSystem.LMST.getIndex()] = new DigitalClock(framework, dimension, "", "", "", "", INDICATOR_BORDER);
        digitalClock[TimeSystem.LMST.getIndex()].setTimeSystem(TimeSystem.LMST);
        digitalClock[TimeSystem.LMST.getIndex()].setValueForeground(color);
        digitalClock[TimeSystem.LMST.getIndex()].setStatusFont(FONTSTATUS);
        digitalClock[TimeSystem.LMST.getIndex()].setStatusColour(COLOR_STATUS);
        panelClocks.add(digitalClock[TimeSystem.LMST.getIndex()], "3, 5, c, c");

        digitalClock[TimeSystem.LAST.getIndex()] = new DigitalClock(framework, dimension, "", "", "", "", INDICATOR_BORDER);
        digitalClock[TimeSystem.LAST.getIndex()].setTimeSystem(TimeSystem.LAST);
        digitalClock[TimeSystem.LAST.getIndex()].setValueForeground(color);
        digitalClock[TimeSystem.LAST.getIndex()].setStatusFont(FONTSTATUS);
        digitalClock[TimeSystem.LAST.getIndex()].setStatusColour(COLOR_STATUS);
        panelClocks.add(digitalClock[TimeSystem.LAST.getIndex()], "5, 5, c, c");

        add(panelClocks, BorderLayout.CENTER);
        }


    /***********************************************************************************************
     * Initialise this UIComponent.
     */

    public final void initialiseUI()
        {
        }


    /***********************************************************************************************
     * Run the UI of this UIComponent.
     */

    public final void runUI()
        {
        final TimeSystem[] timeSystems = TimeSystem.values();

        for (int i = 0; i < timeSystems.length; i++)
            {
            final TimeSystem timeSystem = timeSystems[i];

            if (digitalClock[timeSystem.getIndex()] != null)
                {
                digitalClock[timeSystem.getIndex()].start();
                }
            }

        NavigationUtilities.updateComponentTreeUI(this);
        }


    /***********************************************************************************************
     * Stop the UI of this UIComponent.
     */

    public final void stopUI()
        {
        final TimeSystem[] timeSystems = TimeSystem.values();

        for (int i = 0; i < timeSystems.length; i++)
            {
            final TimeSystem timeSystem = timeSystems[i];

            if (digitalClock[timeSystem.getIndex()] != null)
                {
                digitalClock[timeSystem.getIndex()].stop();
                }
            }
        }


    /***********************************************************************************************
     * Dispose of this UIComponent.
     */

    public final void disposeUI()
        {
        final TimeSystem[] timeSystems = TimeSystem.values();

        for (int i = 0; i < timeSystems.length; i++)
            {
            final TimeSystem timeSystem = timeSystems[i];

            if (digitalClock[timeSystem.getIndex()] != null)
                {
                digitalClock[timeSystem.getIndex()].stop();
                digitalClock[timeSystem.getIndex()] = null;
                }
            }
        }
    }

//------------------------------------------------------------------------------
// End of File

