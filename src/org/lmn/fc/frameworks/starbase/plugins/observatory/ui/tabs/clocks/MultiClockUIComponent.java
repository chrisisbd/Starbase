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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.clocks;

import info.clearthought.layout.TableLayout;
import org.lmn.fc.common.exceptions.FrameworkException;
import org.lmn.fc.common.utilities.time.TimeSystem;
import org.lmn.fc.frameworks.starbase.plugins.observatory.events.ObservatoryClockChangedEvent;
import org.lmn.fc.frameworks.starbase.plugins.observatory.events.ObservatoryClockChangedListener;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryUIInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.InstrumentUIComponentDecorator;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.model.registry.NavigationUtilities;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;
import org.lmn.fc.ui.UIComponentPlugin;
import org.lmn.fc.ui.components.UIComponentHelper;
import org.lmn.fc.ui.widgets.IndicatorInterface;
import org.lmn.fc.ui.widgets.impl.Indicator;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;


/***********************************************************************************************
 * MultiClockUIComponent.
 */

public final class MultiClockUIComponent extends InstrumentUIComponentDecorator
                                         implements ObservatoryClockChangedListener
    {
    // String Resources
    private static final String TOOLTIP_TIME_ZONE = "Time Zone ";

    private static final long serialVersionUID = 8353074319815563904L;
    private static final Color COLOR_TEXT   = Color.red;
    private static final Color COLOR_STATUS = new Color(42, 123, 198);
    private static final Font FONT_STATUS   = new Font("Dialog", Font.PLAIN, 5);
    private static final int SPACER = 25;

    // Injections
    private final Dimension dimClock;

    private final List<IndicatorInterface> listDigitalClocks;


    /***********************************************************************************************
     * Add a new clock to the panel, for the specified TimeSystem.
     *
     * @param panel
     * @param dimension
     * @param constraints
     * @param timesystem
     *
     * @return IndicatorInterface
     */

    private static IndicatorInterface addNewClock(final JPanel panel,
                                                  final Dimension dimension,
                                                  final String constraints,
                                                  final TimeSystem timesystem)
        {
        final IndicatorInterface clock;

        clock = new Indicator(dimension,
                              EMPTY_STRING,
                              timesystem.getMnemonic(),
                              timesystem.getName(),
                              timesystem.getName(),
                              INDICATOR_BORDER);

        // Resize Value and Status for the new displayed text for the new Time System
        clock.setValueForeground(COLOR_TEXT);
        clock.setValueFormat(timesystem.getFormat());
        clock.setValue(timesystem.getFormat());

        clock.setStatusFont(FONT_STATUS);
        clock.setStatusColour(COLOR_STATUS);
        clock.setStatusBackground(UIComponentPlugin.DEFAULT_COLOUR_TAB_BACKGROUND.getColor());
        clock.setStatusFormat(timesystem.getName());
        clock.setStatus(timesystem.getName());

        clock.setUnits(timesystem.getMnemonic());
        clock.setToolTip(TOOLTIP_TIME_ZONE + REGISTRY.getFramework().getTimeZoneCode());

        panel.add((Component)clock, constraints);

        return (clock);
        }


    /***********************************************************************************************
     * Create a UIComponent containing one of each type of TimeSystem clock of the given size.
     *
     * @param hostinstrument
     * @param instrumentxml
     * @param hostui
     * @param task
     * @param font
     * @param colour
     * @param resourcekey
     * @param dimension
     */

    public MultiClockUIComponent(final ObservatoryInstrumentInterface hostinstrument,
                                 final Instrument instrumentxml,
                                 final ObservatoryUIInterface hostui,
                                 final TaskPlugin task,
                                 final FontInterface font,
                                 final ColourInterface colour,
                                 final String resourcekey,
                                 final Dimension dimension)
        {
        super(hostinstrument,
              instrumentxml,
              hostui,
              task,
              font,
              colour,
              resourcekey, 1);

        if (dimension == null)
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_INVALID);
            }

        this.dimClock = dimension;

        // Create space for a clock for every TimeSystem
        this.listDigitalClocks = new ArrayList<IndicatorInterface>(TimeSystem.values().length);
        }


    /***********************************************************************************************
     * Initialise this UIComponent.
     */

    public final void initialiseUI()
        {
        final JPanel panelUI;
        final JPanel panelClocks;
        final JScrollPane scrollPane;
        final TimeSystem[] timeSystems;

        // https://tablelayout.dev.java.net/articles/TableLayoutTutorialPart1/TableLayoutTutorialPart1.html
        // https://tablelayout.dev.java.net/servlets/ProjectDocumentList?folderID=3487&expandFolder=3487&folderID=3487

        // TableLayout row and column size definitions
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

        // TableLayout constraints for Clock panels
        final String[] constraints =
            {
            // Row 1
             "1, 1, CENTER, CENTER",
             "3, 1, CENTER, CENTER",
             "5, 1, CENTER, CENTER",
             "7, 1, CENTER, CENTER",
            // Row 3
             "1, 3, CENTER, CENTER",
             "3, 3, CENTER, CENTER",
             "5, 3, CENTER, CENTER",
             "7, 3, CENTER, CENTER",
            // Row 5
             "3, 5, CENTER, CENTER",
             "5, 5, CENTER, CENTER"
            };

        // DO NOT USE super.initialiseUI()

        panelUI = new JPanel();
        panelUI.setLayout(new BorderLayout());

        // The host UIComponent uses BorderLayout
        add(panelUI, BorderLayout.CENTER);

        // The clock panel contains the Clocks :-)
        panelClocks = new JPanel();
        panelClocks.setLayout(new TableLayout(size));
        panelClocks.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        panelClocks.setBackground(UIComponentPlugin.DEFAULT_COLOUR_CANVAS.getColor());

        // Let's show all TimeSystems...
        timeSystems = TimeSystem.values();

        for (int intIndexTimeSystem = 0;
             ((getObservatoryClock() != null)
                && (intIndexTimeSystem < timeSystems.length));
             intIndexTimeSystem++)
            {
            final TimeSystem timeSystem;

            timeSystem = timeSystems[intIndexTimeSystem];

            // Create the Digital Clocks and add them to the panel
            getClocks().add(addNewClock(panelClocks,
                                        this.dimClock,
                                        constraints[intIndexTimeSystem],
                                        timeSystem));
            }

        scrollPane = new JScrollPane(panelClocks,
                                     JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                     JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        panelUI.add(scrollPane, BorderLayout.CENTER);
        }


    /***********************************************************************************************
     * Run this UIComponent.
     */

    public void runUI()
        {
        super.runUI();

        // Listen to the ObservatoryClock for LAST updates
        if ((getHostInstrument() != null)
            && (getHostInstrument().getObservatoryClock() != null)
            && (getHostInstrument().getObservatoryClock().getClockDAO() != null))
            {
            getHostInstrument().getObservatoryClock().getClockDAO().addObservatoryClockChangedListener(this);
            }

        NavigationUtilities.updateComponentTreeUI(this);
        }


    /***********************************************************************************************
     * Stop this UIComponent.
     */

    public void stopUI()
        {
        super.stopUI();

        if ((getHostInstrument() != null)
            && (getHostInstrument().getObservatoryClock() != null)
            && (getHostInstrument().getObservatoryClock().getClockDAO() != null))
            {
            getHostInstrument().getObservatoryClock().getClockDAO().removeObservatoryClockChangedListener(this);
            }
        }


    /***********************************************************************************************
     * Dispose of this UIComponent.
     */

    public final void disposeUI()
        {
        getClocks().clear();

        if ((getHostInstrument() != null)
            && (getHostInstrument().getObservatoryClock() != null)
            && (getHostInstrument().getObservatoryClock().getClockDAO() != null))
            {
            getHostInstrument().getObservatoryClock().getClockDAO().removeObservatoryClockChangedListener(this);
            }

        super.disposeUI();
        }


    /***********************************************************************************************
     * Indicate that the ObservatoryClock has changed.
     *
     * @param event
     */

    public void clockChanged(final ObservatoryClockChangedEvent event)
        {
        // Update only if visible
        if ((event != null)
            && (event.hasChanged())
            && (UIComponentHelper.shouldRefresh(false, getHostInstrument(), this)))
            {
            if ((getObservatoryClock() != null)
                && (getObservatoryClock().getAstronomicalCalendar() != null))
                {
                final TimeSystem[] timeSystems;

                // Let's show all TimeSystems...
                timeSystems = TimeSystem.values();

                for (int intIndexTimeSystem = 0;
                     intIndexTimeSystem < timeSystems.length;
                     intIndexTimeSystem++)
                    {
                    final TimeSystem timeSystem;
                    final IndicatorInterface clockInterface;

                    timeSystem = timeSystems[intIndexTimeSystem];
                    clockInterface = getClocks().get(intIndexTimeSystem);
                    clockInterface.setValue(getObservatoryClock().getAstronomicalCalendar().toString_HH_MM_SS(timeSystem));
                    }
                }
            else
                {
                for (int intIndexTimeSystem = 0;
                     intIndexTimeSystem < TimeSystem.values().length;
                     intIndexTimeSystem++)
                    {
                    final IndicatorInterface clockInterface;

                    clockInterface = getClocks().get(intIndexTimeSystem);
                    clockInterface.setValue(NO_CLOCK);
                    }
                }
            }
        }


    /***********************************************************************************************
     * Get the List of DigitalClocks.
     *
     * @return List<IndicatorInterface>
     */

    private List<IndicatorInterface> getClocks()
        {
        return (this.listDigitalClocks);
        }
    }
