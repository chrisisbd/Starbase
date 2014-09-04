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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.toolkit;

import org.lmn.fc.common.utilities.files.FileUtilities;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryUIInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.InstrumentPanelTabFactory;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.InstrumentUIComponentDecorator;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.InstrumentUIHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.toolkit.fourier.FourierUIComponent;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.toolkit.jupiter.JupiterMoonsUIComponent;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.toolkit.smithchart.SmithChartUIComponent;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;
import org.lmn.fc.ui.UIComponentPlugin;
import org.lmn.fc.ui.components.BlankUIComponent;
import org.lmn.fc.ui.components.UIComponentHelper;

import javax.swing.*;
import java.awt.*;


/***************************************************************************************************
 * The ToolkitInstrumentPanel.
 */

public final class ToolkitInstrumentPanel extends InstrumentUIComponentDecorator
    {
    // String Resources
    private static final String ICON_HEADER_TOOLKIT = "toolkit-header.png";

    private static final String TAB_MOONS = "Jupiter Moons";
    private static final String TAB_FILTER = "Digital Filter";
    private static final String TAB_FOURIER = "Fourier";
    private static final String TAB_SMITH_CHART = "Smith Chart";
    private static final String INSTRUMENT_HELP = "ToolkitHelp.html";

    private UIComponentPlugin tabJupiterMoons;
    private UIComponentPlugin tabFilter;
    private UIComponentPlugin tabFourier;
    private UIComponentPlugin tabSmithChart;


    /***********************************************************************************************
     * Construct a ToolkitInstrumentPanel.
     *
     * @param instrument
     * @param instrumentxml
     * @param hostui
     * @param task
     * @param font
     * @param colour
     * @param resourcekey
     * @param chartmin
     * @param chartmax
     */

    public ToolkitInstrumentPanel(final ObservatoryInstrumentInterface instrument,
                                  final Instrument instrumentxml,
                                  final ObservatoryUIInterface hostui,
                                  final TaskPlugin task,
                                  final FontInterface font,
                                  final ColourInterface colour,
                                  final String resourcekey,
                                  final double chartmin,
                                  final double chartmax)
        {
        super(instrument,
              instrumentxml,
              hostui,
              task,
              font,
              colour,
              resourcekey, 1);
        }


    /***********************************************************************************************
     * Initialise the ToolkitInstrumentPanel.
     */

    public final void initialiseUI()
        {
        super.initialiseUI();

        InstrumentUIHelper.configureInstrumentPanelHeader(getHeaderUIComponent(),
                                                          getObservatoryUI(),
                                                          this,
                                                          getHostTask().getParentAtom(),
                                                          getInstrument(),
                                                          ICON_HEADER_TOOLKIT,
                                                          getFontData(),
                                                          getColourData());

        // Create the ToolkitInstrumentPanel and add it to the host UIComponent
        createAndInitialiseTabs();
        }


    /**********************************************************************************************/
    /* Utilities                                                                                  */
    /***********************************************************************************************
     * Create and initialise the JTabbedPane for the ToolkitInstrumentPanel.
     */

    private void createAndInitialiseTabs()
        {
        setTabbedPane(new JTabbedPane(JTabbedPane.BOTTOM));
        configureTabbedPane();

        tabJupiterMoons = new JupiterMoonsUIComponent(getHostInstrument(),
                                                      getInstrument(),
                                                      getObservatoryUI(),
                                                      getHostTask(),
                                                      getFontData(),
                                                      getColourData(),
                                                      REGISTRY.getFrameworkResourceKey());
        getTabbedPane().addTab(TAB_MOONS,
                               (Component) getJupiterMoonsTab());

        tabFilter = new BlankUIComponent("Filter");
        getTabbedPane().addTab(TAB_FILTER,
                               (Component) getFilterTab());

        tabFourier = new FourierUIComponent(getHostInstrument(),
                                            getInstrument(),
                                            getObservatoryUI(),
                                            getHostTask(),
                                            getFontData(),
                                            getColourData(),
                                            REGISTRY.getFrameworkResourceKey());
//        getTabbedPane().addTab(TAB_FOURIER,
//                               (Component) getFourierTab());

        tabSmithChart = new SmithChartUIComponent();
        getTabbedPane().addTab(TAB_SMITH_CHART,
                               (Component) getSmithChartTab());

        InstrumentPanelTabFactory.addMetadataExplorerTab(this,
                                                         TAB_META_DATA_EXPLORER,
                                                         REGISTRY_MODEL.getLoggedInUser());

        InstrumentPanelTabFactory.addXmlTab(this,
                                            TAB_XML,
                                            REGISTRY_MODEL.getLoggedInUser());

        InstrumentPanelTabFactory.addHelpTab(this,
                                             TAB_HELP,
                                             FileUtilities.html,
                                             INSTRUMENT_HELP,
                                             REGISTRY_MODEL.getLoggedInUser());

        // Initialise all UIComponentPlugins on the tabs of the JTabbedPane
        // This will apply ContextActions for each UIComponentPlugin
        UIComponentHelper.initialiseAllTabComponents(getTabbedPane());

        // Listen for clicks on the JTabbedPane
        // On click, this calls stopUI() for all tabs, then runUI() for the selected tab
        UIComponentHelper.addTabListener(getHostTask(), this, getTabbedPane());

        // Add the tabs to the UIComponent
        this.add(getTabbedPane());
        }


    /***********************************************************************************************
     * Get the Jupiter Moons Tab.
     *
     * @return UIComponentPlugin
     */

    private UIComponentPlugin getJupiterMoonsTab()
        {
        return (this.tabJupiterMoons);
        }


    /***********************************************************************************************
     * Get the Filter Tab.
     *
     * @return UIComponentPlugin
     */

    private UIComponentPlugin getFilterTab()
        {
        return (this.tabFilter);
        }


    /***********************************************************************************************
     * Get the Fourier Tab.
     *
     * @return UIComponentPlugin
     */

    private UIComponentPlugin getFourierTab()
        {
        return (this.tabFourier);
        }


    /***********************************************************************************************
     * Get the SmithChart Tab.
     *
     * @return UIComponentPlugin
     */

    private UIComponentPlugin getSmithChartTab()
        {
        return (this.tabSmithChart);
        }
    }
