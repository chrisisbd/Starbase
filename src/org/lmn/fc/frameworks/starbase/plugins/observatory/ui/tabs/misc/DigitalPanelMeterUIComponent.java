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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.misc;

import info.clearthought.layout.TableLayout;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryUIInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.InstrumentUIComponentDecorator;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.model.registry.NavigationUtilities;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;
import org.lmn.fc.ui.UIComponentPlugin;
import org.lmn.fc.ui.widgets.IndicatorInterface;
import org.lmn.fc.ui.widgets.impl.Indicator;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;


/***********************************************************************************************
 * DigitalPanelMeterUIComponent.
 */

public final class DigitalPanelMeterUIComponent extends InstrumentUIComponentDecorator
    {
    // String Resources
    private static final String DEFAULT_CHANNEL_NAME = "Channel ";
    private static final String DEFAULT_VALUE_FORMAT = "99999";

    private static final Color COLOR_TEXT   = Color.red;
    private static final Color COLOR_STATUS = new Color(42, 123, 198);
    private static final Font FONT_STATUS   = new Font("Dialog", Font.PLAIN, 5);
    private static final int SPACER = 25;

    // Injections
    private final Dimension dimMeter;
    private final List<IndicatorInterface> listMeters;


    /***********************************************************************************************
     * Add a new Meter to the panel.
     * The initialisation Metadata may be incomplete, so just put NO_DATA.
     *
     * @param panel
     * @param dimension
     * @param constraints
     * @param index
     *
     * @return IndicatorInterface
     */

    private static IndicatorInterface addNewMeter(final JPanel panel,
                                                  final Dimension dimension,
                                                  final String constraints,
                                                  final int index)
        {
        final IndicatorInterface meter;
        final String strDefault;

        strDefault = DEFAULT_CHANNEL_NAME + index;

        meter = new Indicator(dimension,
                              EMPTY_STRING,
                              EMPTY_STRING,
                              strDefault,
                              strDefault,
                              INDICATOR_BORDER);

        // Resize Value and Status for the new displayed text
        meter.setValueForeground(COLOR_TEXT);
        meter.setValueFormat(DEFAULT_VALUE_FORMAT);
        meter.setValue(NO_DATA);

        meter.setStatusFont(FONT_STATUS);
        meter.setStatusColour(COLOR_STATUS);
        meter.setStatusBackground(UIComponentPlugin.DEFAULT_COLOUR_TAB_BACKGROUND.getColor());
        meter.setStatusFormat(strDefault);
        meter.setStatus(strDefault);

        meter.setUnits(EMPTY_STRING);
        meter.setToolTip(strDefault);

        panel.add((Component)meter, constraints);

        return (meter);
        }


    /***********************************************************************************************
     * Create a UIComponent containing DigitalPanelMeters.
     *
     * @param hostinstrument
     * @param instrumentxml
     * @param hostui
     * @param task
     * @param font
     * @param colour
     * @param resourcekey
     * @param dimension
     * @param indicatorcount
     */

    public DigitalPanelMeterUIComponent(final ObservatoryInstrumentInterface hostinstrument,
                                        final Instrument instrumentxml,
                                        final ObservatoryUIInterface hostui,
                                        final TaskPlugin task,
                                        final FontInterface font,
                                        final ColourInterface colour,
                                        final String resourcekey,
                                        final Dimension dimension,
                                        final int indicatorcount)
        {
        super(hostinstrument,
              instrumentxml,
              hostui,
              task,
              font,
              colour,
              resourcekey,
              indicatorcount);

        if (indicatorcount == 1)
            {
            this.dimMeter = new Dimension(dimension.width << 1, dimension.height << 1);
            }
        else
            {
            this.dimMeter = dimension;
            }

        this.listMeters = new ArrayList<IndicatorInterface>(indicatorcount);
        }


    /***********************************************************************************************
     * Initialise this UIComponent.
     */

    public final void initialiseUI()
        {
        final JPanel panelUI;
        final JPanel panelMeters;
        final JScrollPane scrollPane;

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
                TableLayout.FILL
                }
            };

        // TableLayout constraints for Meter panels
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
             "7, 3, CENTER, CENTER"
            };

        // DO NOT USE super.initialiseUI()

        panelUI = new JPanel();
        panelUI.setLayout(new BorderLayout());

        // The host UIComponent uses BorderLayout
        add(panelUI, BorderLayout.CENTER);

        panelMeters = new JPanel();
        panelMeters.setLayout(new TableLayout(size));
        panelMeters.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        panelMeters.setBackground(UIComponentPlugin.DEFAULT_COLOUR_CANVAS.getColor());

        for (int intIndicatorIndex = 0;
             (intIndicatorIndex < getIndicatorCount());
             intIndicatorIndex++)
            {
            // Create the DigitalPanelMeters and add them to the panel
            getMeters().add(addNewMeter(panelMeters,
                                        this.dimMeter,
                                        constraints[intIndicatorIndex],
                                        intIndicatorIndex));
            }

        scrollPane = new JScrollPane(panelMeters,
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

        NavigationUtilities.updateComponentTreeUI(this);
        }


    /***********************************************************************************************
     * Dispose of this UIComponent.
     */

    public final void disposeUI()
        {
        getMeters().clear();

        super.disposeUI();
        }


    /***********************************************************************************************
     * Remove any Data associated with this UIComponent's appearance on the UI.
     * For instance, remove a Chart regardless of it being visible.
     */

    public void removeUIIdentity()
        {
        final String SOURCE = "DigitalPanelMeterUIComponent.removeUIIdentity() ";
        final List<IndicatorInterface> listMeterIndicators;

        super.removeUIIdentity();

        listMeterIndicators = getMeters();

        if (listMeterIndicators != null)
            {
            for (int intMeterIndex = 0;
                 intMeterIndex < listMeterIndicators.size();
                 intMeterIndex++)
                {
                final IndicatorInterface meter;

                meter = listMeterIndicators.get(intMeterIndex);

                meter.setValue(EMPTY_STRING);
                }

            LOGGER.debug((LOADER_PROPERTIES.isMasterDebug() || LOADER_PROPERTIES.isChartDebug()),
                         SOURCE + "Cleared Meters [uistate=" + getUIState().getName() + "]");
            }
        }


    /***********************************************************************************************
     * Get the List of DigitalPanelMeters.
     *
     * @return List<IndicatorInterface>
     */

    public List<IndicatorInterface> getMeters()
        {
        return (this.listMeters);
        }
    }
