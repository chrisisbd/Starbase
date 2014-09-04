// Copyright 2000, 2001, 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009,
//           2010, 2011, 2012, 2013, 2014
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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common;


import org.lmn.fc.common.constants.*;
import org.lmn.fc.common.utilities.astronomy.CoordinateConversions;
import org.lmn.fc.common.utilities.files.FileUtilities;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ObservatoryInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.ephemerides.EphemerisDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.starmap.plugins.StarMapPlugin;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.starmap.plugins.StarMapPointInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.starmap.plugins.impl.EphemeridesObjects;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.starmap.plugins.impl.RaDecGrid;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.starmap.plugins.impl.Sky;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.starmap.plugins.impl.Sun;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.starmap.ui.StarMapUIComponentPlugin;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.starmap.ui.impl.StarMapUIComponent;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.InstrumentUIComponentDecoratorInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryUIInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.communicator.ui.SimpleNewsreaderUIComponent;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.MapUIComponentPlugin;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.MetadataExplorerFrameUIComponentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.audio.AudioExplorerFrameUIComponent;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.charts.ChartUIHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.charts.GOESChartUIComponent;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.charts.GpsScatterPlotUIComponent;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.charts.LogLinChartUIComponent;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.clocks.MultiClockUIComponent;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.commands.CommandBuilderUIComponent;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.commands.CommandLexiconUIComponent;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.configuration.ConfigurationUIComponent;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.configuration.XmlFrameUIComponent;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.data.DataUpdateType;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.data.ProcessedDataUIComponent;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.data.RawDataUIComponent;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.ephemerides.EphemerisFrameUIComponent;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.hexeditor.HexEditorFrameUIComponent;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.images.ImageIconUIComponent;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.images.ImageUIComponent;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.jython.JythonConsoleFrameUIComponent;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.jython.JythonEditorFrameUIComponent;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.logs.GpsInstrumentLogUIComponent;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.logs.NtpInstrumentLogUIComponent;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.logs.SimpleEventLogUIComponent;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.logs.SimpleInstrumentLogUIComponent;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.maps.RegionalMapUIComponent;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.metadata.MetadataExplorerFrameUIComponent;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.misc.DigitalPanelMeterUIComponent;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.misc.JavaConsoleUIComponent;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.misc.SimpleNewsreaderPanel;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.publisher.PublisherFrameUIComponent;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.scanner.NetworkScannerFrameUIComponent;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.sda.SuperposedDataAnalyserUIComponent;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.serial.SerialConfigurationFrameUIComponent;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.timezones.TimeZonesFrameUIComponent;
import org.lmn.fc.model.locale.CountryPlugin;
import org.lmn.fc.model.locale.impl.CountryData;
import org.lmn.fc.model.registry.InstallationFolder;
import org.lmn.fc.model.users.UserPlugin;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;
import org.lmn.fc.model.xmlbeans.subscriptions.RssSubscription;
import org.lmn.fc.ui.UIComponentPlugin;
import org.lmn.fc.ui.panels.HTMLPanel;
import org.lmn.fc.ui.reports.ReportTablePlugin;

import javax.swing.*;
import java.awt.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;


/***************************************************************************************************
 * InstrumentPanelTabFactory.
 * Helper methods for the addition of Tabs to Instrument Panels.
 *
 * ToDo
 *
 *  CommunicatorInstrumentPanel
 *      SubscriptionsUIComponent
 *
 *  DataProcessorInstrumentPanel
 *      PatchPanelUIComponent
 *
 *  ObservatoryInstallerInstrumentPanel
 *      ObservatoryPluginsUIComponent
 *
 *  ObservatoryMonitorInstrumentPanel
 *      CommandLifecycleUIComponent
 *      PortMonitorUIComponent
 *      MemoryUsageChartUIComponent
 *      ActionsUIComponent
 *
 *  RepositoryInstrumentPanel
 *
 *  StarinetTesterInstrumentPanel
 *      HTMLUIComponent
 *
 *  TerminalEmulatorInstrumentPanel
 *      TerminalConsoleUIComponent
 *
 *  ToolkitInstrumentPanel
 *      JupiterMoonsUIComponent
 *      FourierUIComponent
 *      SmithChartUIComponent
 *
 *  WebServerInstrumentPanel
 *      HTMLPanel
 *      XMLUIComponent
 *
 */

public final class InstrumentPanelTabFactory implements FrameworkConstants,
                                                        FrameworkStrings,
                                                        FrameworkMetadata,
                                                        FrameworkRegex,
                                                        FrameworkSingletons,
                                                        ResourceKeys
    {
    // String Resources
    private static final String HEADER_TITLE_HELP = "Instrument Help";
    private static final String HEADER_TITLE_MANUAL = "User Manual";
    private static final String HEADER_TITLE_PUBLISHER = "Publication Viewer";

    private static final Dimension DIMENSION_METER = new Dimension(140, 80);


    /***********************************************************************************************
     * Add a Commands Tab to the Instrument, if allowed for this User.
     *
     * @param decorator
     * @param title
     * @param user
     */

    public static void addCommandsTab(final InstrumentUIComponentDecoratorInterface decorator,
                                      final String title,
                                      final UserPlugin user)
        {
        if ((decorator != null)
            && (decorator.getTabbedPane() != null)
            && (decorator.getHostTask() != null)
            && (decorator.getHostInstrument() != null)
            && (decorator.getObservatoryUI() != null)
            && (decorator.getInstrument() != null))
            {
            decorator.setCommandsTab(new CommandBuilderUIComponent(decorator.getHostInstrument(),
                                                                   decorator.getInstrument(),
                                                                   decorator.getObservatoryUI(),
                                                                   decorator.getHostTask(),
                                                                   decorator.getFontData(),
                                                                   decorator.getColourData(),
                                                                   decorator.getResourceKey()));
            decorator.getTabbedPane().addTab(title,
                                             (Component) decorator.getCommandsTab());

            // Indicate that this Tab may be Exported or Published
            decorator.getExportableTabs().add(title.replaceAll("\\s", ""));
            }
        }


    /***********************************************************************************************
     * Add a LogLin Chart Tab to the Instrument, if allowed for this User.
     *
     * @param decorator
     * @param title
     * @param metdatalist
     * @param updatetype
     * @param lin_fixed_min_y
     * @param lin_fixed_max_y
     * @param log_fixed_min_y
     * @param log_fixed_max_y
     * @param user
     */

    public static void addLogLinChartTab(final InstrumentUIComponentDecoratorInterface decorator,
                                         final String title,
                                         final List<Metadata> metdatalist,
                                         final DataUpdateType updatetype,
                                         final double lin_fixed_min_y,
                                         final double lin_fixed_max_y,
                                         final double log_fixed_min_y,
                                         final double log_fixed_max_y,
                                         final UserPlugin user)
        {
        if ((decorator != null)
            && (decorator.getTabbedPane() != null)
            && (decorator.getHostTask() != null)
            && (decorator.getHostInstrument() != null)
            && (decorator.getHostInstrument().getHostAtom() != null))
            {
            decorator.setChartTab(new LogLinChartUIComponent(decorator.getHostTask(),
                                                             decorator.getHostInstrument(),
                                                             title,
                                                             metdatalist,
                                                             REGISTRY.getFrameworkResourceKey(),
                                                             updatetype,
                                                             REGISTRY.getIntegerProperty(decorator.getHostInstrument().getHostAtom().getResourceKey() + KEY_DISPLAY_DATA_MAX),
                                                             lin_fixed_min_y,
                                                             lin_fixed_max_y,
                                                             log_fixed_min_y,
                                                             log_fixed_max_y)
                {
                /***********************************************************************************************
                 * Get the optional JToolBar.
                 * Default is NULL, override if needed.
                 *
                 * @return JToolBar
                 */

                public JToolBar getToolBar()
                    {
                    return (ChartUIHelper.createDefaultToolbar(getHostInstrument(),
                                                               this,
                                                               getChartName(),
                                                               getHostInstrument().getFontData(),
                                                               getHostInstrument().getColourData(),
                                                               DEFAULT_COLOUR_TAB_BACKGROUND,
                                                               isDebug()));
                    }
                });

            decorator.getTabbedPane().addTab(title,
                                             null,
                                             (Component) decorator.getChartTab(),
                                             InstrumentUIHelper.showChartTabTooltip(decorator.getHostInstrument(),
                                                                                    updatetype));

            // Indicate that this Tab may be Exported or Published
            decorator.getExportableTabs().add(title.replaceAll("\\s", ""));
            }
        }


    /***********************************************************************************************
     * Add a GOES Chart Tab to the Instrument, if allowed for this User.
     *
     * @param decorator
     * @param title
     * @param metdatalist
     * @param updatetype
     * @param user
     */

    public static void addGOESChartTab(final InstrumentUIComponentDecoratorInterface decorator,
                                       final String title,
                                       final List<Metadata> metdatalist,
                                       final DataUpdateType updatetype,
                                       final UserPlugin user)
        {
        if ((decorator != null)
            && (decorator.getTabbedPane() != null)
            && (decorator.getHostTask() != null)
            && (decorator.getHostInstrument() != null)
            && (decorator.getHostInstrument().getHostAtom() != null))
            {
            decorator.setChartTab(new GOESChartUIComponent(decorator.getHostTask(),
                                                           decorator.getHostInstrument(),
                                                           title,
                                                           metdatalist,
                                                           REGISTRY.getFrameworkResourceKey(),
                                                           updatetype,
                                                           REGISTRY.getIntegerProperty(decorator.getHostInstrument().getHostAtom().getResourceKey() + KEY_DISPLAY_DATA_MAX)));
            decorator.getTabbedPane().addTab(title,
                                             null,
                                             (Component) decorator.getChartTab(),
                                             InstrumentUIHelper.showChartTabTooltip(decorator.getHostInstrument(),
                                                                                    updatetype));

            // Indicate that this Tab may be Exported or Published
            decorator.getExportableTabs().add(title.replaceAll("\\s", ""));
            }
        }


    /***********************************************************************************************
     * Add a GPS Scatter Chart Tab to the Instrument, if allowed for this User.
     *
     * @param decorator
     * @param title
     * @param metdatalist
     * @param user
     */

    public static void addGPSScatterChartTab(final InstrumentUIComponentDecoratorInterface decorator,
                                             final String title,
                                             final List<Metadata> metdatalist,
                                             final UserPlugin user)
        {
        if ((decorator != null)
            && (decorator.getTabbedPane() != null)
            && (decorator.getHostTask() != null)
            && (decorator.getHostInstrument() != null))
            {
            decorator.setChartTab(new GpsScatterPlotUIComponent(decorator.getHostTask(),
                                                                decorator.getHostInstrument(),
                                                                title,
                                                                metdatalist,
                                                                REGISTRY.getFrameworkResourceKey()));
            decorator.getTabbedPane().addTab(title,
                                             (Component) decorator.getChartTab());

            // Indicate that this Tab may be Exported or Published
            decorator.getExportableTabs().add(title.replaceAll("\\s", ""));
            }
        }


    /***********************************************************************************************
     * Add a Meters Tab to the Instrument, if allowed for this User.
     *
     * @param decorator
     * @param title
     * @param user
     */

    public static void addMetersTab(final InstrumentUIComponentDecoratorInterface decorator,
                                    final String title,
                                    final UserPlugin user)
        {
        if ((decorator != null)
            && (decorator.getTabbedPane() != null)
            && (decorator.getHostTask() != null)
            && (decorator.getHostInstrument() != null)
            && (decorator.getHostInstrument().getInstrument() != null)
            && (decorator.getObservatoryUI() != null))
            {
            decorator.setMetersTab(new DigitalPanelMeterUIComponent(decorator.getHostInstrument(),
                                                                    decorator.getInstrument(),
                                                                    decorator.getObservatoryUI(),
                                                                    decorator.getHostTask(),
                                                                    decorator.getFontData(),
                                                                    decorator.getColourData(),
                                                                    decorator.getResourceKey(),
                                                                    DIMENSION_METER,
                                                                    decorator.getIndicatorCount()));
            decorator.getTabbedPane().addTab(title,
                                             (Component) decorator.getMetersTab());
            }
        }


    /***********************************************************************************************
     * Add a Clocks Tab to the Instrument, if allowed for this User.
     *
     * @param decorator
     * @param title
     * @param dimension
     * @param user
     */

    public static void addClocksTab(final InstrumentUIComponentDecoratorInterface decorator,
                                    final String title,
                                    final Dimension dimension,
                                    final UserPlugin user)
        {
        if ((decorator != null)
            && (decorator.getTabbedPane() != null)
            && (decorator.getHostTask() != null)
            && (decorator.getHostInstrument() != null)
            && (decorator.getHostInstrument().getInstrument() != null)
            && (decorator.getObservatoryUI() != null))
            {
            decorator.setClocksTab(new MultiClockUIComponent(decorator.getHostInstrument(),
                                                             decorator.getInstrument(),
                                                             decorator.getObservatoryUI(),
                                                             decorator.getHostTask(),
                                                             decorator.getFontData(),
                                                             decorator.getColourData(),
                                                             decorator.getResourceKey(),
                                                             dimension));
            decorator.getTabbedPane().addTab(title,
                                             (Component) decorator.getClocksTab());
            }
        }


    /***********************************************************************************************
     * Add an AudioExplorer Tab to the Instrument, if allowed for this User.
     *
     * @param decorator
     * @param title
     * @param user
     */

    public static void addAudioExplorerTab(final InstrumentUIComponentDecoratorInterface decorator,
                                           final String title,
                                           final UserPlugin user)
        {
        if ((decorator != null)
            && (decorator.getTabbedPane() != null)
            && (decorator.getHostTask() != null)
            && (decorator.getHostInstrument() != null)
            && (decorator.getHostInstrument().getInstrument() != null)
            && (decorator.getObservatoryUI() != null))
            {
            decorator.setAudioExplorerTab(new AudioExplorerFrameUIComponent(decorator.getHostInstrument(),
                                                                            decorator.getInstrument(),
                                                                            decorator.getObservatoryUI(),
                                                                            decorator.getHostTask(),
                                                                            decorator.getFontData(),
                                                                            decorator.getColourData(),
                                                                            REGISTRY.getFrameworkResourceKey()));
            decorator.getTabbedPane().addTab(title,
                                             (Component) decorator.getAudioExplorerTab());
            }
        }


    /***********************************************************************************************
     * Add a SerialConfiguration Tab to the Instrument, if allowed for this User.
     *
     * @param decorator
     * @param title
     * @param user
     */

    public static void addSerialConfigurationTab(final InstrumentUIComponentDecoratorInterface decorator,
                                                 final String title,
                                                 final UserPlugin user)
        {
        if ((decorator != null)
            && (decorator.getTabbedPane() != null)
            && (decorator.getHostTask() != null)
            && (decorator.getHostInstrument() != null)
            && (decorator.getHostInstrument().getInstrument() != null)
            && (decorator.getObservatoryUI() != null))
            {
            decorator.setSerialConfigurationTab(new SerialConfigurationFrameUIComponent(decorator.getHostInstrument(),
                                                                                        decorator.getInstrument(),
                                                                                        decorator.getObservatoryUI(),
                                                                                        decorator.getHostTask(),
                                                                                        decorator.getFontData(),
                                                                                        decorator.getColourData(),
                                                                                        REGISTRY.getFrameworkResourceKey()));
            decorator.getTabbedPane().addTab(title,
                                             (Component) decorator.getSerialConfigurationTab());
            }
        }


    /***********************************************************************************************
     * Add a JavaConsole Tab to the Instrument, if allowed for this User.
     *
     * @param decorator
     * @param title
     * @param user
     */

    public static void addJavaConsoleTab(final InstrumentUIComponentDecoratorInterface decorator,
                                         final String title,
                                         final UserPlugin user)
        {
        if ((decorator != null)
            && (decorator.getTabbedPane() != null)
            && (decorator.getHostTask() != null)
            && (decorator.getHostInstrument() != null))
            {
            // This is the only creation of JavaConsoleUIComponent
            decorator.setJavaConsoleTab(new JavaConsoleUIComponent(decorator.getHostTask(),
                                                                   decorator.getHostInstrument(),
                                                                   REGISTRY.getFrameworkResourceKey()));
            // Auto-scroll to the last entry
            ((ReportTablePlugin) decorator.getJavaConsoleTab()).setScrollToRow(-1);

            decorator.getTabbedPane().addTab(title,
                                             (Component) decorator.getJavaConsoleTab());
            }
        }


    /***********************************************************************************************
     * Add a JythonConsole Tab to the Instrument, if allowed for this User.
     *
     * @param decorator
     * @param title
     * @param user
     */

    public static void addJythonConsoleTab(final InstrumentUIComponentDecoratorInterface decorator,
                                           final String title,
                                           final UserPlugin user)
        {
        if ((decorator != null)
            && (decorator.getTabbedPane() != null)
            && (decorator.getHostTask() != null)
            && (decorator.getHostInstrument() != null)
            && (decorator.getHostInstrument().getInstrument() != null)
            && (decorator.getObservatoryUI() != null))
            {
            // This is the only creation of JythonConsoleFrameUIComponent
            decorator.setJythonConsoleTab(new JythonConsoleFrameUIComponent(decorator.getHostInstrument(),
                                                                            decorator.getInstrument(),
                                                                            decorator.getObservatoryUI(),
                                                                            decorator.getHostTask(),
                                                                            decorator.getFontData(),
                                                                            decorator.getColourData(),
                                                                            REGISTRY.getFrameworkResourceKey()));
            decorator.getTabbedPane().addTab(title,
                                             (Component) decorator.getJythonConsoleTab());
            }
        }


    /***********************************************************************************************
     * Add a JythonEditor Tab to the Instrument, if allowed for this User.
     *
     * @param decorator
     * @param title
     * @param user
     */

    public static void addJythonEditorTab(final InstrumentUIComponentDecoratorInterface decorator,
                                          final String title,
                                          final UserPlugin user)
        {
        if ((decorator != null)
            && (decorator.getTabbedPane() != null)
            && (decorator.getHostTask() != null)
            && (decorator.getHostInstrument() != null)
            && (decorator.getHostInstrument().getInstrument() != null)
            && (decorator.getObservatoryUI() != null))
            {
            // This is the only creation of JythonEditorFrameUIComponent
            decorator.setJythonEditorTab(new JythonEditorFrameUIComponent(decorator.getHostInstrument(),
                                                                          decorator.getInstrument(),
                                                                          decorator.getObservatoryUI(),
                                                                          decorator.getHostTask(),
                                                                          decorator.getFontData(),
                                                                          decorator.getColourData(),
                                                                          REGISTRY.getFrameworkResourceKey()));
            decorator.getTabbedPane().addTab(title,
                                             (Component) decorator.getJythonEditorTab());
            }
        }


    /***********************************************************************************************
     * Add a HexEditor Tab to the Instrument, if allowed for this User.
     *
     * @param decorator
     * @param title
     * @param user
     */

    public static void addHexEditorTab(final InstrumentUIComponentDecoratorInterface decorator,
                                       final String title,
                                       final UserPlugin user)
        {
        if ((decorator != null)
            && (decorator.getTabbedPane() != null)
            && (decorator.getHostTask() != null)
            && (decorator.getHostInstrument() != null)
            && (decorator.getHostInstrument().getInstrument() != null)
            && (decorator.getObservatoryUI() != null))
            {
            // This is the only creation of HexEditorFrameUIComponent
            decorator.setHexEditorTab(new HexEditorFrameUIComponent(decorator.getHostInstrument(),
                                                                    decorator.getInstrument(),
                                                                    decorator.getObservatoryUI(),
                                                                    decorator.getHostTask(),
                                                                    decorator.getFontData(),
                                                                    decorator.getColourData(),
                                                                    REGISTRY.getFrameworkResourceKey()));
            decorator.getTabbedPane().addTab(title,
                                             (Component) decorator.getHexEditorTab());
            }
        }


    /***********************************************************************************************
     * Add a ProcessedData Tab to the Instrument, if allowed for this User.
     *
     * @param decorator
     * @param title
     * @param user
     */

    public static void addProcessedDataTab(final InstrumentUIComponentDecoratorInterface decorator,
                                           final String title,
                                           final UserPlugin user)
        {
        if ((decorator != null)
            && (decorator.getTabbedPane() != null)
            && (decorator.getHostTask() != null)
            && (decorator.getHostInstrument() != null)
            && (decorator.getHostInstrument().getHostAtom() != null)
            && (decorator.getHostInstrument().getInstrument() != null))
            {
            decorator.setProcessedDataTab(new ProcessedDataUIComponent(decorator.getHostTask(),
                                                                       decorator.getHostInstrument(),
                                                                       REGISTRY.getFrameworkResourceKey(),
                                                                       title,
                                                                       REGISTRY.getIntegerProperty(decorator.getHostInstrument().getHostAtom().getResourceKey() + KEY_DISPLAY_DATA_MAX)));
            // Auto-scroll to the last entry
            ((ReportTablePlugin) decorator.getProcessedDataTab()).setScrollToRow(-1);
            decorator.getTabbedPane().addTab(title,
                                             null,
                                             (Component) decorator.getProcessedDataTab(),
                                             InstrumentUIHelper.showTruncatedTooltip(decorator.getHostInstrument()));

            // Indicate that this Tab may be Exported or Published
            decorator.getExportableTabs().add(title.replaceAll("\\s", ""));
            }
        }


    /***********************************************************************************************
     * Add a SuperposedDataAnalyser Tab to the Instrument, if allowed for this User.
     *
     * @param decorator
     * @param title
     * @param user
     */

    public static void addSuperposedDataAnalyserTab(final InstrumentUIComponentDecoratorInterface decorator,
                                                    final String title,
                                                    final UserPlugin user)
        {
        if ((decorator != null)
            && (decorator.getTabbedPane() != null)
            && (decorator.getHostTask() != null)
            && (decorator.getHostInstrument() != null)
            && (decorator.getHostInstrument().getHostAtom() != null)
            && (decorator.getHostInstrument().getInstrument() != null))
            {
            decorator.setSuperposedDataAnalyserTab(new SuperposedDataAnalyserUIComponent(decorator.getHostInstrument(),
                                                                                         decorator.getInstrument(),
                                                                                         decorator.getObservatoryUI(),
                                                                                         decorator.getHostTask(),
                                                                                         decorator.getFontData(),
                                                                                         decorator.getColourData(),
                                                                                         decorator.getResourceKey()));
            decorator.getTabbedPane().addTab(title,
                                             (Component) decorator.getSuperposedDataAnalyserTab());

            // ToDo REVIEW: Indicate that this Tab may be Exported or Published
            //decorator.getExportableTabs().add(title.replaceAll("\\s", ""));
            }
        }


    /***********************************************************************************************
     * Add a RawData Tab to the Instrument, if allowed for this User.
     *
     * @param decorator
     * @param title
     * @param user
     */

    public static void addRawDataTab(final InstrumentUIComponentDecoratorInterface decorator,
                                     final String title,
                                     final UserPlugin user)
        {
        if ((decorator != null)
            && (decorator.getTabbedPane() != null)
            && (decorator.getHostTask() != null)
            && (decorator.getHostInstrument() != null)
            && (decorator.getHostInstrument().getHostAtom() != null)
            && (decorator.getHostInstrument().getInstrument() != null))
            {
            decorator.setRawDataTab(new RawDataUIComponent(decorator.getHostTask(),
                                                           decorator.getHostInstrument(),
                                                           REGISTRY.getFrameworkResourceKey(),
                                                           title,
                                                           REGISTRY.getIntegerProperty(decorator.getHostInstrument().getHostAtom().getResourceKey() + KEY_DISPLAY_DATA_MAX)));
            // Auto-scroll to the last entry
            ((ReportTablePlugin) decorator.getRawDataTab()).setScrollToRow(-1);
            decorator.getTabbedPane().addTab(title,
                                             null,
                                             (Component) decorator.getRawDataTab(),
                                             InstrumentUIHelper.showTruncatedTooltip(decorator.getHostInstrument()));

            // Indicate that this Tab may be Exported or Published
            decorator.getExportableTabs().add(title.replaceAll("\\s", ""));
            }
        }


    /***********************************************************************************************
     * Add a Image Tab to the Instrument, if allowed for this User.
     *
     * @param decorator
     * @param title
     * @param metadatalist
     * @param user
     */

    public static void addImageTab(final InstrumentUIComponentDecoratorInterface decorator,
                                   final String title,
                                   final List<Metadata> metadatalist,
                                   final UserPlugin user)
        {
        if ((decorator != null)
            && (decorator.getTabbedPane() != null)
            && (decorator.getHostTask() != null)
            && (decorator.getHostInstrument() != null))
            {
            decorator.setImageTab(new ImageUIComponent(decorator.getHostInstrument(),
                                                       metadatalist,
                                                       REGISTRY.getFrameworkResourceKey()));
            decorator.getTabbedPane().addTab(title,
                                             (Component) decorator.getImageTab());

            // Indicate that this Tab may be Exported or Published
            decorator.getExportableTabs().add(title.replaceAll("\\s", ""));
            }
        }


    /***********************************************************************************************
     * Add a ImageIcon Tab to the Instrument, if allowed for this User.
     * Intended for static images that don't change.
     * Use with caution, since this uses the <b>same</b> reference as an Image Tab.
     *
     * @param decorator
     * @param title
     * @param imageicon
     * @param metadatalist
     * @param user
     */

    public static void addImageIconTab(final InstrumentUIComponentDecoratorInterface decorator,
                                       final String title,
                                       final ImageIcon imageicon,
                                       final List<Metadata> metadatalist,
                                       final UserPlugin user)
        {
        if ((decorator != null)
            && (imageicon != null)
            && (decorator.getTabbedPane() != null)
            && (decorator.getHostTask() != null)
            && (decorator.getHostInstrument() != null))
            {
            decorator.setImageTab(new ImageIconUIComponent(decorator.getHostInstrument(),
                                                           imageicon,
                                                           metadatalist,
                                                           REGISTRY.getFrameworkResourceKey()));
            decorator.getTabbedPane().addTab(title,
                                             (Component) decorator.getImageTab());

            // Indicate that this Tab may be Exported or Published
            decorator.getExportableTabs().add(title.replaceAll("\\s", ""));
            }
        }


    /***********************************************************************************************
     * Add a MetadataExplorer Tab to the Instrument, if allowed for this User.
     *
     * @param decorator
     * @param title
     * @param user
     */

    public static void addMetadataExplorerTab(final InstrumentUIComponentDecoratorInterface decorator,
                                              final String title,
                                              final UserPlugin user)
        {
        if ((decorator != null)
            && (decorator.getTabbedPane() != null)
            && (decorator.getHostTask() != null)
            && (decorator.getHostInstrument() != null)
            && (decorator.getHostInstrument().getInstrument() != null)
            && (user != null))
            {
            decorator.setMetadataExplorerTab(new MetadataExplorerFrameUIComponent(decorator.getHostInstrument(),
                                                                                  decorator.getInstrument(),
                                                                                  decorator.getObservatoryUI(),
                                                                                  decorator.getHostTask(),
                                                                                  decorator.getInstrument().getInstrumentMetadataList(),
                                                                                  true,
                                                                                  decorator.getFontData(),
                                                                                  decorator.getColourData(),
                                                                                  REGISTRY.getFrameworkResourceKey()));
            decorator.getTabbedPane().addTab(title,
                                             (Component) decorator.getMetadataExplorerTab());

            // Make sure we are notified if the Metadata changes in any way
            if ((decorator.getMetadataExplorerTab() != null)
                && (decorator.getMetadataExplorerTab() instanceof MetadataExplorerFrameUIComponentInterface)
                && (((MetadataExplorerFrameUIComponentInterface) decorator.getMetadataExplorerTab()).getMetadataExplorerUI() != null)
                && (((MetadataExplorerFrameUIComponentInterface) decorator.getMetadataExplorerTab()).getMetadataExplorerUI().getTheLeafUI() != null))
                {
                ((MetadataExplorerFrameUIComponentInterface)decorator.getMetadataExplorerTab()).getMetadataExplorerUI().getTheLeafUI().addMetadataChangedListener(decorator);
                }

            // Indicate that this Tab may be Exported or Published
            decorator.getExportableTabs().add(title.replaceAll("\\s", ""));
            }
        }


    /***********************************************************************************************
     * Add a RegionalMap Tab to the Instrument, if allowed for this User.
     *
     * @param decorator
     * @param title
     * @param user
     */

    public static void addRegionalMapTab(final InstrumentUIComponentDecoratorInterface decorator,
                                         final String title,
                                         final UserPlugin user)
        {
        if ((decorator != null)
            && (decorator.getTabbedPane() != null)
            && (decorator.getHostInstrument() != null)
            && (user != null))
            {
            final CountryPlugin country;

            country = REGISTRY.getCountry(CountryData.getResourceKeyFromCode(user.getCountryCode()));
            decorator.setRegionalMapTab(new RegionalMapUIComponent(decorator.getHostInstrument(),
                                                                   country,
                                                                   decorator.getFontData(),
                                                                   decorator.getColourData(),
                                                                   UIComponentPlugin.DEFAULT_COLOUR_TAB_BACKGROUND));
            ((MapUIComponentPlugin) decorator.getRegionalMapTab()).collectPOIandLOI();

            decorator.getTabbedPane().addTab(title,
                                             (Component) decorator.getRegionalMapTab());

            // Indicate that this Tab may be Exported or Published
            decorator.getExportableTabs().add(title.replaceAll("\\s", ""));
            }
        }


    /***********************************************************************************************
     * Add a TimeZone Tab to the Instrument, if allowed for this User.
     *
     * @param decorator
     * @param title
     * @param user
     */

    public static void addTimeZoneTab(final InstrumentUIComponentDecoratorInterface decorator,
                                      final String title,
                                      final UserPlugin user)
        {
        if ((decorator != null)
            && (decorator.getTabbedPane() != null)
            && (decorator.getHostTask() != null)
            && (decorator.getHostInstrument() != null)
            && (decorator.getHostInstrument().getHostAtom() != null)
            && (decorator.getHostInstrument().getInstrument() != null)
            && (user != null))
            {
            decorator.setTimeZoneTab(new TimeZonesFrameUIComponent(decorator.getHostInstrument(),
                                                                   decorator.getInstrument(),
                                                                   decorator.getObservatoryUI(),
                                                                   decorator.getHostTask(),
                                                                   decorator.getFontData(),
                                                                   decorator.getColourData(),
                                                                   decorator.getResourceKey()));
            decorator.getTabbedPane().addTab(title,
                                             (Component) decorator.getTimeZoneTab());

            // ToDo REVIEW: Indicate that this Tab may be Exported or Published
            decorator.getExportableTabs().add(title.replaceAll("\\s", ""));
            }
        }


    /***********************************************************************************************
     * Add a EventLog Tab to the Instrument, if allowed for this User.
     *
     * @param decorator
     * @param title
     * @param user
     */

    public static void addEventLogTab(final InstrumentUIComponentDecoratorInterface decorator,
                                      final String title,
                                      final UserPlugin user)
        {
        if ((decorator != null)
            && (decorator.getTabbedPane() != null)
            && (decorator.getHostTask() != null)
            && (decorator.getHostInstrument() != null)
            && (decorator.getHostInstrument().getInstrument() != null))
            {
            decorator.setEventLogTab(new SimpleEventLogUIComponent(decorator.getHostTask(),
                                                                   decorator.getHostInstrument(),
                                                                   REGISTRY.getFrameworkResourceKey(),
                                                                   title));
            decorator.getTabbedPane().addTab(title,
                                             null,
                                             (Component) decorator.getEventLogTab(),
                                             InstrumentUIHelper.showTruncatedTooltip(decorator.getHostInstrument()));

            // Indicate that this Tab may be Exported or Published
            decorator.getExportableTabs().add(title.replaceAll("\\s", ""));
            }
        }


    /***********************************************************************************************
     * Add a InstrumentLog Tab to the Instrument, if allowed for this User.
     *
     * @param decorator
     * @param title
     * @param user
     */

    public static void addInstrumentLogTab(final InstrumentUIComponentDecoratorInterface decorator,
                                           final String title,
                                           final UserPlugin user)
        {
        if ((decorator != null)
            && (decorator.getTabbedPane() != null)
            && (decorator.getHostTask() != null)
            && (decorator.getHostInstrument() != null))
            {
            decorator.setInstrumentLogTab(new SimpleInstrumentLogUIComponent(decorator.getHostTask(),
                                                                             decorator.getHostInstrument(),
                                                                             REGISTRY.getFrameworkResourceKey(),
                                                                             title));
            decorator.getTabbedPane().addTab(title,
                                             null,
                                             (Component) decorator.getInstrumentLogTab(),
                                             InstrumentUIHelper.showTruncatedTooltip(decorator.getHostInstrument()));

            // Indicate that this Tab may be Exported or Published
            decorator.getExportableTabs().add(title.replaceAll("\\s", ""));
            }
        }


    /***********************************************************************************************
     * Add a NTP InstrumentLog Tab to the Instrument, if allowed for this User.
     *
     * @param decorator
     * @param title
     * @param user
     */

    public static void addNTPInstrumentLogTab(final InstrumentUIComponentDecoratorInterface decorator,
                                              final String title,
                                              final UserPlugin user)
        {
        if ((decorator != null)
            && (decorator.getTabbedPane() != null)
            && (decorator.getHostTask() != null)
            && (decorator.getHostInstrument() != null))
            {
            decorator.setInstrumentLogTab(new NtpInstrumentLogUIComponent(decorator.getHostTask(),
                                                                          decorator.getHostInstrument(),
                                                                          REGISTRY.getFrameworkResourceKey(),
                                                                          title));
            decorator.getTabbedPane().addTab(title,
                                             null,
                                             (Component) decorator.getInstrumentLogTab(),
                                             InstrumentUIHelper.showTruncatedTooltip(decorator.getHostInstrument()));

            // Indicate that this Tab may be Exported or Published
            decorator.getExportableTabs().add(title.replaceAll("\\s", ""));
            }
        }


    /***********************************************************************************************
     * Add a GPS InstrumentLog Tab to the Instrument, if allowed for this User.
     *
     * @param decorator
     * @param title
     * @param user
     */

    public static void addGPSInstrumentLogTab(final InstrumentUIComponentDecoratorInterface decorator,
                                              final String title,
                                              final UserPlugin user)
        {
        if ((decorator != null)
            && (decorator.getTabbedPane() != null)
            && (decorator.getHostTask() != null)
            && (decorator.getHostInstrument() != null))
            {
            decorator.setInstrumentLogTab(new GpsInstrumentLogUIComponent(decorator.getHostTask(),
                                                                          decorator.getHostInstrument(),
                                                                          REGISTRY.getFrameworkResourceKey(),
                                                                          title));
            decorator.getTabbedPane().addTab(title,
                                             null,
                                             (Component) decorator.getInstrumentLogTab(),
                                             InstrumentUIHelper.showTruncatedTooltip(decorator.getHostInstrument()));

            // Indicate that this Tab may be Exported or Published
            decorator.getExportableTabs().add(title.replaceAll("\\s", ""));
            }
        }


    /***********************************************************************************************
     * Add a StarMap Tab to the Instrument, if allowed for this User.
     *
     * @param decorator
     * @param title
     * @param user
     */

    public static void addStarMapTab(final InstrumentUIComponentDecoratorInterface decorator,
                                     final String title,
                                     final UserPlugin user)
        {
        if ((decorator != null)
            && (decorator.getTabbedPane() != null)
            && (decorator.getHostInstrument() != null)
            && (decorator.getObservatoryUI() != null))
            {
            decorator.setStarMapTab(createStarMap(decorator.getHostInstrument(),
                                                  decorator.getObservatoryUI()));
            decorator.getTabbedPane().addTab(title,
                                             (Component) decorator.getStarMapTab());

            // Indicate that this Tab may be Exported or Published
            decorator.getExportableTabs().add(title.replaceAll("\\s", ""));
            }
        }


    /***********************************************************************************************
     * Add a Ephemerides Tab to the Instrument, if allowed for this User.
     *
     * @param decorator
     * @param title
     * @param user
     */

    public static void addEphemeridesTab(final InstrumentUIComponentDecoratorInterface decorator,
                                         final String title,
                                         final UserPlugin user)
        {
        if ((decorator != null)
            && (decorator.getTabbedPane() != null)
            && (decorator.getHostTask() != null)
            && (decorator.getHostInstrument() != null)
            && (decorator.getHostInstrument().getHostAtom() != null)
            && (decorator.getHostInstrument().getInstrument() != null)
            && (user != null))
            {
            decorator.setEphemeridesTab(new EphemerisFrameUIComponent(decorator.getHostInstrument(),
                                                                      decorator.getInstrument(),
                                                                      decorator.getObservatoryUI(),
                                                                      decorator.getHostTask(),
                                                                      decorator.getFontData(),
                                                                      decorator.getColourData(),
                                                                      decorator.getResourceKey()));
            decorator.getTabbedPane().addTab(title,
                                             (Component) decorator.getEphemeridesTab());

            // Indicate that this Tab may be Exported or Published
            decorator.getExportableTabs().add(title.replaceAll("\\s", ""));
            }
        }


    /***********************************************************************************************
     * Add a Configuration Tab to the Instrument, if allowed for this User.
     *
     * @param decorator
     * @param title
     * @param user
     */

    public static void addConfigurationTab(final InstrumentUIComponentDecoratorInterface decorator,
                                           final String title,
                                           final UserPlugin user)
        {
        if ((decorator != null)
            && (decorator.getTabbedPane() != null)
            && (decorator.getHostInstrument() != null))
            {
            decorator.setConfigurationTab(new ConfigurationUIComponent(decorator.getHostTask(),
                                                                       decorator.getHostInstrument(),
                                                                       title,
                                                                       REGISTRY.getFrameworkResourceKey(),
                                                                       decorator.getResourceKey()));
            decorator.getTabbedPane().addTab(title,
                                             (Component) decorator.getConfigurationTab());

            // Indicate that this Tab may be Exported or Published
            decorator.getExportableTabs().add(title.replaceAll("\\s", ""));
            }
        }


    /***********************************************************************************************
     * Add a Mantis Tab to the Instrument, if allowed for this User.
     *
     * @param decorator
     * @param title
     * @param mantisname
     * @param mantisurl
     * @param user
     */

    public static void addMantisTab(final InstrumentUIComponentDecoratorInterface decorator,
                                    final String title,
                                    final String mantisname,
                                    final String mantisurl,
                                    final UserPlugin user)
        {
        if ((decorator != null)
            && (decorator.getTabbedPane() != null)
            && (decorator.getHostInstrument() != null)
            && (mantisname != null)
            && (mantisurl != null))
            {
            decorator.setMantisTab(createRssUIComponent(decorator,
                                                        mantisname,
                                                        mantisurl,
                                                        UIComponentPlugin.FILENAME_ICON_MANTIS,
                                                        true));
            decorator.getTabbedPane().addTab(title,
                                             (Component) decorator.getMantisTab());
            }
        }


    /***********************************************************************************************
     * Add a Subversion Tab to the Instrument, if allowed for this User.
     *
     * @param decorator
     * @param title
     * @param svnname
     * @param svnurl
     * @param user
     */

    public static void addSubversionTab(final InstrumentUIComponentDecoratorInterface decorator,
                                        final String title,
                                        final String svnname,
                                        final String svnurl,
                                        final UserPlugin user)
        {
        if ((decorator != null)
            && (decorator.getTabbedPane() != null)
            && (decorator.getHostInstrument() != null)
            && (svnname != null)
            && (svnurl != null))
            {
            decorator.setSubversionTab(createRssUIComponent(decorator,
                                                            svnname,
                                                            svnurl,
                                                            UIComponentPlugin.FILENAME_ICON_SUBVERSION,
                                                            true));
            decorator.getTabbedPane().addTab(title,
                                             (Component) decorator.getSubversionTab());
            }
        }


    /***********************************************************************************************
     * Add a Jenkins Tab to the Instrument, if allowed for this User.
     *
     * @param decorator
     * @param title
     * @param jenkinsname
     * @param jenkinsurl
     * @param user
     */

    public static void addJenkinsTab(final InstrumentUIComponentDecoratorInterface decorator,
                                     final String title,
                                     final String jenkinsname,
                                     final String jenkinsurl,
                                     final UserPlugin user)
        {
        if ((decorator != null)
            && (decorator.getTabbedPane() != null)
            && (decorator.getHostInstrument() != null)
            && (jenkinsname != null)
            && (jenkinsurl != null))
            {
            decorator.setJenkinsTab(createRssUIComponent(decorator,
                                                         jenkinsname,
                                                         jenkinsurl,
                                                         UIComponentPlugin.FILENAME_ICON_JENKINS,
                                                         true));
            decorator.getTabbedPane().addTab(title,
                                             (Component) decorator.getJenkinsTab());
            }
        }


    /***********************************************************************************************
     * Add an Instrument Newsreader Tab to the Instrument,
     * but only if there's an RSS newsfeed in the XML, and if allowed for this User.
     *
     * @param decorator
     * @param title
     * @param user
     */

    public static void addInstrumentNewsreaderTab(final InstrumentUIComponentDecoratorInterface decorator,
                                                  final String title,
                                                  final UserPlugin user)
        {
        if ((decorator != null)
            && (decorator.getTabbedPane() != null)
            && (decorator.getHostTask() != null)
            && (decorator.getHostInstrument() != null)
            && (decorator.getHostInstrument().getInstrument() != null)
            && (decorator.getHostInstrument().getInstrument().getRSSList() != null)
            && (!decorator.getHostInstrument().getInstrument().getRSSList().isEmpty()))
            {
            decorator.setNewsreaderTab(new SimpleNewsreaderPanel(decorator.getHostInstrument(),
                                                                 decorator.getInstrument(),
                                                                 decorator.getObservatoryUI(),
                                                                 decorator.getHostTask(),
                                                                 decorator.getHostInstrument().getInstrument().getRSSList(),
                                                                 decorator.getFontData(),
                                                                 decorator.getColourData(),
                                                                 REGISTRY.getFrameworkResourceKey()));
            decorator.getTabbedPane().addTab(title,
                                             (Component) decorator.getNewsreaderTab());
            }
        }


    /***********************************************************************************************
     * Add an Communicator Newsreader Tab to the Instrument, if allowed for this User.
     *
     * @param decorator
     * @param title
     * @param user
     */

    public static void addCommunicatorNewsreaderTab(final InstrumentUIComponentDecoratorInterface decorator,
                                                    final String title,
                                                    final UserPlugin user)
        {
        if ((decorator != null)
            && (decorator.getTabbedPane() != null)
            && (decorator.getHostTask() != null)
            && (decorator.getHostInstrument() != null)
            && (decorator.getHostInstrument().getInstrument() != null)
            && (decorator.getObservatoryUI() != null))
            {
            decorator.setNewsreaderTab(new SimpleNewsreaderUIComponent(decorator.getHostInstrument(),
                                                                       decorator.getInstrument(),
                                                                       decorator.getObservatoryUI(),
                                                                       decorator.getHostTask(),
                                                                       decorator.getFontData(),
                                                                       decorator.getColourData(),
                                                                       REGISTRY.getFrameworkResourceKey()));
            decorator.getTabbedPane().addTab(title,
                                             (Component) decorator.getNewsreaderTab());
            }
        }


    /***********************************************************************************************
     * Create an RSS Reader on a UIComponent.
     * Currently used only in ObservatoryMonitorInstrumentPanel.
     *
     * @param decorator
     * @param rssname
     * @param rssurl
     * @param iconfilename
     * @param showdetail
     *
     * @return UIComponentPlugin
     */

    public static UIComponentPlugin createRssUIComponent(final InstrumentUIComponentDecoratorInterface decorator,
                                                         final String rssname,
                                                         final String rssurl,
                                                         final String iconfilename,
                                                         final boolean showdetail)
        {
        final UIComponentPlugin uicomponentRSS;
        final List<RssSubscription> listRssSubscriptions;
        final RssSubscription subscription;

        listRssSubscriptions = new ArrayList<RssSubscription>(1);

        subscription = RssSubscription.Factory.newInstance();
        subscription.setName(rssname);
        subscription.setURL(rssurl);
        subscription.setIconFilename(iconfilename);
        subscription.setShowDetail(showdetail);

        listRssSubscriptions.add(subscription);

        uicomponentRSS = new SimpleNewsreaderPanel(decorator.getHostInstrument(),
                                                   decorator.getInstrument(),
                                                   decorator.getObservatoryUI(),
                                                   decorator.getHostTask(),
                                                   listRssSubscriptions,
                                                   decorator.getFontData(),
                                                   decorator.getColourData(),
                                                   REGISTRY.getFrameworkResourceKey());
        return (uicomponentRSS);
        }


    /***********************************************************************************************
     * Add a CommandLexicon Tab to the Instrument, if allowed for this User.
     *
     * @param decorator
     * @param title
     * @param user
     */

    public static void addCommandLexiconTab(final InstrumentUIComponentDecoratorInterface decorator,
                                            final String title,
                                            final UserPlugin user)
        {
        if ((decorator != null)
            && (decorator.getTabbedPane() != null)
            && (decorator.getHostTask() != null)
            && (decorator.getHostInstrument() != null)
            && (decorator.getInstrument() != null))
            {
            decorator.setCommandLexiconTab(new CommandLexiconUIComponent(decorator.getHostTask(),
                                                                         decorator.getHostInstrument(),
                                                                         decorator.getInstrument(),
                                                                         REGISTRY.getFrameworkResourceKey()));
            decorator.getTabbedPane().addTab(title,
                                             (Component) decorator.getCommandLexiconTab());

            // Indicate that this Tab may be Exported or Published
            decorator.getExportableTabs().add(title.replaceAll("\\s", ""));
            }
        }


    /***********************************************************************************************
     * Add a Network Scanner Tab to the Instrument, if allowed for this User.
     *
     * @param decorator
     * @param title
     * @param user
     */

    public static void addNetworkScannerTab(final InstrumentUIComponentDecoratorInterface decorator,
                                            final String title,
                                            final UserPlugin user)
        {
        if ((decorator != null)
            && (decorator.getTabbedPane() != null)
            && (decorator.getHostInstrument() != null)
            && (decorator.getInstrument() != null))
            {
            decorator.setNetworkScannerTab(new NetworkScannerFrameUIComponent(decorator.getHostTask(),
                                                                              decorator.getHostInstrument(),
                                                                              REGISTRY.getFrameworkResourceKey()));
            decorator.getTabbedPane().addTab(title,
                                             (Component) decorator.getNetworkScannerTab());
            }
        }


    /***********************************************************************************************
     * Add a XML Tab to the Instrument, if allowed for this User.
     *
     * @param decorator
     * @param title
     * @param user
     */

    public static void addXmlTab(final InstrumentUIComponentDecoratorInterface decorator,
                                 final String title,
                                 final UserPlugin user)
        {
        if ((decorator != null)
            && (decorator.getTabbedPane() != null)
            && (decorator.getHostInstrument() != null)
            && (decorator.getInstrument() != null))
            {
            decorator.setXMLTab(new XmlFrameUIComponent(decorator.getHostInstrument(),
                                                        decorator.getInstrument(),
                                                        decorator.getObservatoryUI(),
                                                        decorator.getHostTask(),
                                                        decorator.getFontData(),
                                                        decorator.getColourData(),
                                                        REGISTRY.getFrameworkResourceKey()));
            decorator.getTabbedPane().addTab(title,
                                             (Component) decorator.getXMLTab());

            // Indicate that this Tab may be Exported or Published
            decorator.getExportableTabs().add(title.replaceAll("\\s", ""));
            }
        }


    /***********************************************************************************************
     * Add a Help Tab to the Instrument, if allowed for this User.
     * The FileType selects the type of document to be displayed.
     * Do nothing if the file type is inappropriate.
     *
     * @param decorator
     * @param title
     * @param filetype
     * @param filename
     * @param user
     */

    public static void addHelpTab(final InstrumentUIComponentDecoratorInterface decorator,
                                  final String title,
                                  final String filetype,
                                  final String filename,
                                  final UserPlugin user)
        {
        final String SOURCE = "InstrumentPanelTabFactory.addHelpTab() ";

        if ((decorator != null)
            && (decorator.getTabbedPane() != null)
            && (decorator.getHostInstrument() != null)
            && (decorator.getInstrument() != null)
            && (filetype != null))
            {
            if (FileUtilities.html.equals(filetype))
                {
                decorator.setHelpTab(new HTMLPanel(InstrumentHelper.getInstrumentHelpURL(decorator, filename)));

                decorator.getTabbedPane().addTab(title,
                                                 (Component) decorator.getHelpTab());
                }
            else if (FileUtilities.pdf.equals(filetype))
                {
                final URL urlFile;

                urlFile = InstrumentHelper.getInstrumentHelpURL(decorator, filename);

                //decorator.setHelpTab(new PDFPanel(InstrumentHelper.getInstrumentHelpURL(decorator, filename)));
                decorator.setHelpTab(new PublisherFrameUIComponent(decorator.getHostInstrument(),
                                                                   decorator.getInstrument(),
                                                                   decorator.getObservatoryUI(),
                                                                   decorator.getHostTask(),
                                                                   urlFile,
                                                                   HEADER_TITLE_HELP,
                                                                   ObservatoryInterface.FILENAME_ICON_HELP,
                                                                   decorator.getFontData(),
                                                                   decorator.getColourData(),
                                                                   REGISTRY.getFrameworkResourceKey()));
                decorator.getTabbedPane().addTab(title,
                                                 (Component) decorator.getHelpTab());
                }
            else
                {
                LOGGER.error(SOURCE + "Unrecognised Help Document File Type");
                }
            }
        }


    /***********************************************************************************************
     * Add a Manual Tab to the Instrument, if allowed for this User.
     * The FileType selects the type of document to be displayed.
     * Do nothing if the file type is inappropriate.
     *
     * @param decorator
     * @param title
     * @param filetype
     * @param filename
     * @param user
     */

    public static void addManualTab(final InstrumentUIComponentDecoratorInterface decorator,
                                    final String title,
                                    final String filetype,
                                    final String filename,
                                    final UserPlugin user)
        {
        final String SOURCE = "InstrumentPanelTabFactory.addManualTab() ";

        if ((decorator != null)
            && (decorator.getTabbedPane() != null)
            && (decorator.getHostInstrument() != null)
            && (decorator.getInstrument() != null)
            && (filetype != null))
            {
            if (FileUtilities.html.equals(filetype))
                {
                decorator.setManualTab(new HTMLPanel(InstrumentHelper.getInstrumentHelpURL(decorator, filename)));

                decorator.getTabbedPane().addTab(title,
                                                 (Component) decorator.getManualTab());
                }
            else if (FileUtilities.pdf.equals(filetype))
                {
                final URL urlFile;

                urlFile = InstrumentHelper.getInstrumentHelpURL(decorator, filename);

                //decorator.setHelpTab(new PDFPanel(InstrumentHelper.getInstrumentHelpURL(decorator, filename)));
                decorator.setManualTab(new PublisherFrameUIComponent(decorator.getHostInstrument(),
                                                                     decorator.getInstrument(),
                                                                     decorator.getObservatoryUI(),
                                                                     decorator.getHostTask(),
                                                                     urlFile,
                                                                     HEADER_TITLE_MANUAL,
                                                                     ObservatoryInterface.FILENAME_ICON_MANUAL,
                                                                     decorator.getFontData(),
                                                                     decorator.getColourData(),
                                                                     REGISTRY.getFrameworkResourceKey()));
                decorator.getTabbedPane().addTab(title,
                                                 (Component) decorator.getManualTab());
                }
            else
                {
                LOGGER.error(SOURCE + "Unrecognised Manual Document File Type");
                }
            }
        }


    /***********************************************************************************************
     * Add a Publisher Tab to the Instrument, if allowed for this User.
     * Display nothing if the default file type is inappropriate for the viewer.
     *
     * @param decorator
     * @param title
     * @param defaultfilename
     * @param user
     */

    public static void addPublisherTab(final InstrumentUIComponentDecoratorInterface decorator,
                                       final String title,
                                       final String defaultfilename,
                                       final UserPlugin user)
        {
        final String SOURCE = "InstrumentPanelTabFactory.addPublisherTab() ";

        if ((decorator != null)
            && (decorator.getTabbedPane() != null)
            && (decorator.getHostInstrument() != null)
            && (decorator.getInstrument() != null))
            {
            try
                {
                final StringBuffer buffer;
                final URL urlFile;

                // Assume for now that Publications are in the doc/publications folder
                // to allow easy replacement by the User
                buffer = new StringBuffer();
                buffer.append("file:///");
                buffer.append(InstallationFolder.getTerminatedUserDir());
                buffer.append(InstallationFolder.PUBLICATIONS.getName());
                buffer.append(System.getProperty("file.separator"));
                buffer.append(defaultfilename);
                urlFile = new URL(buffer.toString());

                decorator.setPublisherTab(new PublisherFrameUIComponent(decorator.getHostInstrument(),
                                                                        decorator.getInstrument(),
                                                                        decorator.getObservatoryUI(),
                                                                        decorator.getHostTask(),
                                                                        urlFile,
                                                                        HEADER_TITLE_PUBLISHER,
                                                                        ObservatoryInterface.FILENAME_ICON_PUBLISHER,
                                                                        decorator.getFontData(),
                                                                        decorator.getColourData(),
                                                                        REGISTRY.getFrameworkResourceKey()));
                decorator.getTabbedPane().addTab(title,
                                                 (Component) decorator.getPublisherTab());
                }

            catch (final MalformedURLException exception)
                {
                LOGGER.error(SOURCE + "MalformedURLException [exception=" + exception + "]");
                exception.printStackTrace();
                }
            }
        }


    /***********************************************************************************************
     * Create a populated StarMap.
     *
     * @param instrument
     * @param ui
     *
     * @return StarMapUIComponentPlugin
     */

    private static StarMapUIComponentPlugin createStarMap(final ObservatoryInstrumentInterface instrument,
                                                          final ObservatoryUIInterface ui)
        {
        final StarMapUIComponentPlugin starMap;
        final Sky pluginSky;
        final RaDecGrid pluginRaDecGrid;
        final Sun pluginSun;

        starMap = new StarMapUIComponent(instrument);

        // Extents and Scales rely on an existing Viewport
        starMap.setExtents(CoordinateConversions.AZI_MIN,
                           CoordinateConversions.AZI_MAX,
                           CoordinateConversions.ELEV_MIN,
                           CoordinateConversions.ELEV_MAX);
        starMap.enableScales(true);

        // Add the Sky
        pluginSky = new Sky(ui,
                            starMap,
                            "Sky",
                            "Night",
                            Color.black);
        pluginSky.setActive(true);
        starMap.addPlugin(pluginSky);

        // Add a grid
        pluginRaDecGrid = new RaDecGrid(ui,
                                        starMap,
                                        "Projection Grid",
                                        "RaDec",
                                        StarMapPointInterface.NOTCLICKABLE,
                                        new Color(200, 30, 30));
        pluginRaDecGrid.setActive(true);
        starMap.addPlugin(pluginRaDecGrid);

        // Add the Sun, so it can be individually controlled
        pluginSun = new Sun(ui,
                            starMap,
                            "Solar Ephemeris",
                            "Sun",
                            StarMapPointInterface.CLICKABLE,
                            true,
                            Color.yellow);
        pluginSun.setActive(true);
        starMap.addPlugin(pluginSun);

        // Add all Observatory Ephemeris targets as a single collection
        if ((instrument.getHostAtom() != null)
            && (instrument.getHostAtom() instanceof ObservatoryInterface))
            {
            final ObservatoryInterface observatory;
            final Hashtable<String, EphemerisDAOInterface> tableDAOs;

            observatory = (ObservatoryInterface)instrument.getHostAtom();
            tableDAOs = observatory.getEphemerisDaoTable();

            if ((tableDAOs != null)
                && (!tableDAOs.isEmpty()))
                {
                final StarMapPlugin plugin;

                plugin = new EphemeridesObjects(ui,
                                                starMap,
                                                tableDAOs,
                                                true,
                                                true);
                plugin.setActive(true);
                starMap.addPlugin(plugin);
                }
            }

        return (starMap);
        }
    }
