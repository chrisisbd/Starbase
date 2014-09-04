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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.installer.ui;

import org.lmn.fc.common.comparators.ReportRowsByColumn;
import org.lmn.fc.common.exceptions.ReportException;
import org.lmn.fc.common.utilities.files.FileUtilities;
import org.lmn.fc.common.utilities.misc.JarResources;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryClockInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryUIInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.ObservatoryUIHelper;
import org.lmn.fc.model.dao.DataStore;
import org.lmn.fc.model.registry.InstallationFolder;
import org.lmn.fc.model.root.RootPlugin;
import org.lmn.fc.model.xmlbeans.datatypes.SchemaDataType;
import org.lmn.fc.model.xmlbeans.metadata.SchemaUnits;
import org.lmn.fc.ui.components.UIComponentHelper;
import org.lmn.fc.ui.reports.*;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Manifest;


/***************************************************************************************************
 * The ObservatoryPluginsUIComponent.
 */

public class ObservatoryPluginsUIComponent extends ReportTable
                                           implements ReportTablePlugin
    {
    // String Resources
    private static final String REPORT_NAME = "ObservatoryPlugins";
    private static final String MSG_REPORT_CREATED  = REPORT_NAME + " Report created at";
    private static final String FILENAME_DISTRIBUTION_SUFFIX = "-distribution." + FileUtilities.jar;
    private static final String FILENAME_FILTER_DISTRIBUTION_SUFFIX = "Filter-distribution." + FileUtilities.jar;
    private static final String FILENAME_EPHEMERIS_DISTRIBUTION_SUFFIX = "Ephemeris-distribution." + FileUtilities.jar;
    private static final String ICON_DISTRIBUTION = "distribution.png";
    private static final String PATH_FILTER = "org/lmn/fc/common/datafilters/impl/";
    private static final String PATH_EPHEMERIS = "";

    // The Manifest Attributes must agree with those written by Ant during the build process
    private static final String ATTRIBUTE_DISTRIBUTION      = "Distribution";
    private static final String ATTRIBUTE_PLUGIN_CATEGORY   = "Plugin-Category";
    private static final String ATTRIBUTE_VERSION_NUMBER    = "Version-Number";
    private static final String ATTRIBUTE_BUILD_NUMBER      = "Build-Number";
    private static final String ATTRIBUTE_BUILD_STATUS      = "Build-Status";
    private static final String ATTRIBUTE_BUILD_DATE        = "Build-Date";
    private static final String ATTRIBUTE_BUILT_BY          = "Built-By";
    private static final String ATTRIBUTE_AUTHOR            = "Author";
    private static final String ATTRIBUTE_WEBSITE           = "Website";
    private static final String ATTRIBUTE_EMAIL             = "Email";

    // Report Titles
    private static final String TITLE_ICON          = SPACE;
    private static final String TITLE_IDENTIFIER    = "Identifier";
    private static final String TITLE_CATEGORY      = "Category";
    private static final String TITLE_VERSION       = "Version";
    private static final String TITLE_BUILD_DATE    = "BuildDate";
    private static final String TITLE_BUILT_BY      = "BuiltBy";
    private static final String TITLE_AUTHOR        = ATTRIBUTE_AUTHOR;
    private static final String TITLE_WEBSITE       = ATTRIBUTE_WEBSITE;
    private static final String TITLE_EMAIL         = ATTRIBUTE_EMAIL;

    private static final int COLUMN_INDEX_IDENTIFIER = 1;

    // Injections
    private final ObservatoryUIInterface hostUI;
    private final ObservatoryInstrumentInterface hostInstrument;


    /***********************************************************************************************
     * Read the Distribution Jars.
     *
     * @param hostui
     * @param report
     */

    private static void readDistributionJars(final ObservatoryUIInterface hostui,
                                             final Vector<Vector> report)
        {
        final String SOURCE = "ObservatoryPluginsUIComponent.readDistributionJars() ";

        final String strFolderDistribution;
        final File dirJars;

        // Find all distribution Jars
        strFolderDistribution = InstallationFolder.getTerminatedUserDir()
                                    + DataStore.DIST.getLoadFolder();

        dirJars = new File(strFolderDistribution);

        if (dirJars != null)
            {
            final File [] arrayFiles;

            // If this abstract pathname does not denote a directory,
            // then this method returns null.
            arrayFiles = dirJars.listFiles();

            if (arrayFiles != null)
                {
                final List<String> listInstrumentJarNames;
                final List<String> listFilterJarNames;
                final List<String> listEphemeridesJarNames;

                listInstrumentJarNames = new ArrayList<String>(20);
                listFilterJarNames = new ArrayList<String>(20);
                listEphemeridesJarNames = new ArrayList<String>(20);

                for (final File file : arrayFiles)
                    {
                    // Read all files with names 'XXX-distribution.jar' in the distribution folder
                    // This will pick up everything, including Filters and Ephemerides,
                    // but these will be eliminated later
                    if ((file != null)
                        && (file.isFile())
                        && (file.getName().endsWith(FILENAME_DISTRIBUTION_SUFFIX)))
                        {
                        // Get only the meaningful part of the names
                        listInstrumentJarNames.add(file.getName().substring(0, file.getName().lastIndexOf(FILENAME_DISTRIBUTION_SUFFIX)));
                        }
                    }

                // See if there is an installed Instrument
                // where the ResourceKey or InstrumentClassname starts with the filename.
                // If so, that instrument is installed, and should be added to the Report.
                processInstruments(hostui, listInstrumentJarNames, report);

                // Try again for DataFilters
                for (final File file : arrayFiles)
                    {
                    // Read all files with names 'XXXFilter-distribution.jar' in the distribution folder
                    if ((file != null)
                        && (file.isFile())
                        && (file.getName().endsWith(FILENAME_FILTER_DISTRIBUTION_SUFFIX)))
                        {
                        listFilterJarNames.add(file.getName());
                        }
                    }

                processFilters(hostui, listFilterJarNames, report);

                // Try again for Ephemerides
                for (final File file : arrayFiles)
                    {
                    // Read all files with names 'XXXEphemeris-distribution.jar' in the distribution folder
                    if ((file != null)
                        && (file.isFile())
                        && (file.getName().endsWith(FILENAME_EPHEMERIS_DISTRIBUTION_SUFFIX)))
                        {
                        listEphemeridesJarNames.add(file.getName());
                        }
                    }

                processEphemerides(hostui, listEphemeridesJarNames, report);

                // Sort the items by their Identifiers
                Collections.sort(report, new ReportRowsByColumn(COLUMN_INDEX_IDENTIFIER));
                }
            else
                {
                // There are no distribution Jars
                LOGGER.error(SOURCE + "No distribution Jars found");
                }
            }
        else
            {
            // Unable to read the Jars
            // This should never occur!
            LOGGER.error(SOURCE + "Faulty Distribution directory structure");
            }
        }


    /***********************************************************************************************
     * Process all Installed Instruments against the Jars in the Distribution folder.
     * Note that the Jar names have had the '-distribution.jar' removed to assist lookup.
     *
     * @param hostui
     * @param jarnames
     * @param report
     */

    private static void processInstruments(final ObservatoryUIInterface hostui,
                                           final List<String> jarnames,
                                           final Vector<Vector> report)
        {
        final String SOURCE = "ObservatoryPluginsUIComponent.processInstruments() ";

        if ((hostui != null)
            && (jarnames != null)
            && (jarnames.size() > 0)
            && (report != null))
            {
            final List<ObservatoryInstrumentInterface> instruments;
            final Iterator<ObservatoryInstrumentInterface> iterInstruments;
            final List<String> listIdentifiers;

            instruments = hostui.getObservatoryInstruments();
            iterInstruments = instruments.iterator();
            listIdentifiers = new ArrayList<String>(30);

            while (iterInstruments.hasNext())
                {
                final ObservatoryInstrumentInterface instrument;

                instrument = iterInstruments.next();

                if ((instrument != null)
                    && (instrument.getInstrument() != null))
                    {
                    try
                        {
                        final String strResourceKey;

                        // Look for ResourceKeys of installed Instruments
                        // which start with the same characters as the name of a distribution Jar
                        strResourceKey = instrument.getInstrument().getResourceKey();

                        for (int i = 0;
                             i < jarnames.size();
                             i++)
                            {
                            final String strJarName;

                            strJarName = jarnames.get(i);

                            if ((strResourceKey.startsWith(strJarName))
                                && (!listIdentifiers.contains(strJarName)))
                                {
                                final String strJarURL;
                                final URL urlJar;

                                // The Instrument is installed, so the manifest can be read
                                strJarURL = "jar:file:./"
                                            + DataStore.DIST.getLoadFolder()
                                            + "/"
                                            + strJarName
                                            + FILENAME_DISTRIBUTION_SUFFIX
                                            + "!/";
                                urlJar = new URL(strJarURL);

                                if (urlJar != null)
                                    {
                                    final JarURLConnection connJar;

                                    connJar = (JarURLConnection)urlJar.openConnection();

                                    if (connJar != null)
                                        {
                                        final Vector<Object> vecRow;
                                        final Manifest manifest;
                                        final Attributes mainAttributes;
                                        final String strJarFileName;
                                        final JarResources jarResources;
                                        final byte[] byteArray;

                                        vecRow = new Vector<Object>(2);

                                        // The Instrument is installed, so the manifest can be read
                                        manifest = connJar.getManifest();
                                        mainAttributes = manifest.getMainAttributes();

                                        // Now we have the Manifest, record this Instrument once,
                                        // since some appear several times with shared code
                                        listIdentifiers.add(strJarName);

                                        // http://www.javaworld.com/javaworld/javatips/jw-javatip49.html
                                        strJarFileName = "./"
                                                            + DataStore.DIST.getLoadFolder()
                                                            + "/"
                                                            + strJarName
                                                            + FILENAME_DISTRIBUTION_SUFFIX;
                                        jarResources = new JarResources(strJarFileName);
                                        jarResources.debugOn = true;

                                        // Instrument distribution icons are in the root
                                        byteArray = jarResources.getResource(ICON_DISTRIBUTION);

                                        if (byteArray != null)
                                            {
                                            final Image image;

                                            image = Toolkit.getDefaultToolkit().createImage(byteArray);

                                            vecRow.add(new ImageIcon(image));
                                            }
                                        else
                                            {
                                            vecRow.add(new ImageIcon());
                                            }

                                        vecRow.add(mainAttributes.getValue(ATTRIBUTE_DISTRIBUTION));
                                        vecRow.add(mainAttributes.getValue(ATTRIBUTE_PLUGIN_CATEGORY));
                                        vecRow.add(makeVersion(mainAttributes.getValue(ATTRIBUTE_VERSION_NUMBER),
                                                               mainAttributes.getValue(ATTRIBUTE_BUILD_NUMBER),
                                                               mainAttributes.getValue(ATTRIBUTE_BUILD_STATUS)));
                                        vecRow.add(mainAttributes.getValue(ATTRIBUTE_BUILD_DATE));
                                        vecRow.add(mainAttributes.getValue(ATTRIBUTE_BUILT_BY));
                                        vecRow.add(mainAttributes.getValue(ATTRIBUTE_AUTHOR));
                                        vecRow.add(mainAttributes.getValue(ATTRIBUTE_WEBSITE));
                                        vecRow.add(mainAttributes.getValue(ATTRIBUTE_EMAIL));

                                        report.add(vecRow);
                                        }
                                    }
                                }
                            }
                        }

                    catch (MalformedURLException exception)
                        {
                        LOGGER.error(SOURCE + "[exception=" + exception.getMessage() + "]");
                        }

                    catch (IOException exception)
                        {
                        LOGGER.error(SOURCE + "[exception=" + exception.getMessage() + "]");
                        }

                    catch (IllegalArgumentException exception)
                        {
                        LOGGER.error(SOURCE + "[exception=" + exception.getMessage() + "]");
                        }
                    }
                }
            }
        }


    /***********************************************************************************************
     * Process all Installed Filters against the Jars in the Distribution folder.
     * Note that the Jar names are kept intact.
     *
     * @param hostui
     * @param jarnames
     * @param report
     */

    private static void processFilters(final ObservatoryUIInterface hostui,
                                       final List<String> jarnames,
                                       final Vector<Vector> report)
        {
        final String SOURCE = "ObservatoryPluginsUIComponent.processFilters() ";

        if ((hostui != null)
            && (jarnames != null)
            && (jarnames.size() > 0)
            && (report != null))
            {
            try
                {
                for (int intJarIndex = 0;
                     intJarIndex < jarnames.size();
                     intJarIndex++)
                    {
                    final String strJarName;
                    final String strJarURL;
                    final URL urlJar;

                    strJarName = jarnames.get(intJarIndex);

                    // ToDo Determine if the Filter is *installed*, as well as just being distributed

                    // The Filter is installed, so the manifest can be read
                    strJarURL = "jar:file:./"
                                    + DataStore.DIST.getLoadFolder()
                                    + "/"
                                    + strJarName
                                    + "!/";
                    urlJar = new URL(strJarURL);

                    if (urlJar != null)
                        {
                        final JarURLConnection connJar;

                        connJar = (JarURLConnection)urlJar.openConnection();

                        if (connJar != null)
                            {
                            final Vector<Object> vecRow;
                            final Manifest manifest;
                            final Attributes mainFilterAttributes;
                            final String strJarFileName;
                            final String strIconPath;
                            final JarResources jarResources;
                            final byte[] byteArray;

                            vecRow = new Vector<Object>(2);

                            // The Filter is installed, so the manifest can be read
                            manifest = connJar.getManifest();
                            mainFilterAttributes = manifest.getMainAttributes();

                            // http://www.javaworld.com/javaworld/javatips/jw-javatip49.html
                            // ./datastore/distribution/PassThroughFilter-distribution.jar
                            strJarFileName = "./"
                                             + DataStore.DIST.getLoadFolder()
                                             + "/"
                                             + strJarName;
                            jarResources = new JarResources(strJarFileName);
                            jarResources.debugOn = true;

                            strIconPath = PATH_FILTER
                                              + strJarName.substring(0, strJarName.lastIndexOf(FILENAME_DISTRIBUTION_SUFFIX)).toLowerCase()
                                              + "/"
                                              + ICON_DISTRIBUTION;

                            // Filter distribution icons are in the Filter folder, not in the root
                            byteArray = jarResources.getResource(strIconPath);

                            if (byteArray != null)
                                {
                                final Image image;

                                image = Toolkit.getDefaultToolkit().createImage(byteArray);

                                vecRow.add(new ImageIcon(image));
                                }
                            else
                                {
                                vecRow.add(new ImageIcon());
                                }

                            vecRow.add(mainFilterAttributes.getValue(ATTRIBUTE_DISTRIBUTION));
                            vecRow.add(mainFilterAttributes.getValue(ATTRIBUTE_PLUGIN_CATEGORY));
                            vecRow.add(makeVersion(mainFilterAttributes.getValue(ATTRIBUTE_VERSION_NUMBER),
                                                   mainFilterAttributes.getValue(ATTRIBUTE_BUILD_NUMBER),
                                                   mainFilterAttributes.getValue(ATTRIBUTE_BUILD_STATUS)));
                            vecRow.add(mainFilterAttributes.getValue(ATTRIBUTE_BUILD_DATE));
                            vecRow.add(mainFilterAttributes.getValue(ATTRIBUTE_BUILT_BY));
                            vecRow.add(mainFilterAttributes.getValue(ATTRIBUTE_AUTHOR));
                            vecRow.add(mainFilterAttributes.getValue(ATTRIBUTE_WEBSITE));
                            vecRow.add(mainFilterAttributes.getValue(ATTRIBUTE_EMAIL));

                            report.add(vecRow);
                            }
                        }
                    }
                }

            catch (MalformedURLException exception)
                {
                LOGGER.error(SOURCE + "[exception=" + exception.getMessage() + "]");
                }

            catch (IOException exception)
                {
                LOGGER.error(SOURCE + "[exception=" + exception.getMessage() + "]");
                }

            catch (IllegalArgumentException exception)
                {
                LOGGER.error(SOURCE + "[exception=" + exception.getMessage() + "]");
                }
            }
        }


    /***********************************************************************************************
     * Process all Installed Ephemerides against the Jars in the Distribution folder.
     *
     * @param hostui
     * @param jarnames
     * @param report
     */

    private static void processEphemerides(final ObservatoryUIInterface hostui,
                                           final List<String> jarnames,
                                           final Vector<Vector> report)
        {
        final String SOURCE = "ObservatoryPluginsUIComponent.processEphemerides() ";

        if ((hostui != null)
            && (jarnames != null)
            && (jarnames.size() > 0)
            && (report != null))
            {
//            addTitleLine("Ephemerides");




            }
        }


    /***********************************************************************************************
     * Make the full Version.Build.Status string.
     *
     * @param version
     * @param build
     * @param status
     *
     * @return String
     */

    private static String makeVersion(final String version,
                                      final String build,
                                      final String status)
        {
        final StringBuffer buffer;

        buffer = new StringBuffer();
        buffer.append(version);
        buffer.append(".");
        buffer.append(build);
        buffer.append(" ");
        buffer.append(status);

        return (buffer.toString());
        }


   /***********************************************************************************************
     * Construct a ObservatoryPluginsUIComponent.
     * The Report ResourceKey is always that of the host Framework, since this is a general utility.
     *
     * @param task
     * @param hostui
     * @param hostinstrument
     * @param resourcekey
     */

    public ObservatoryPluginsUIComponent(final RootPlugin task,
                                         final ObservatoryUIInterface hostui,
                                         final ObservatoryInstrumentInterface hostinstrument,
                                         final String resourcekey)
        {
        super(task,
              REPORT_NAME,
              resourcekey,
              PRINTABLE,
              EXPORTABLE,
              NON_REFRESHABLE,
              REFRESH_NONE,
              NON_REORDERABLE,
              NON_TRUNCATEABLE,
              LOCK_TOP_ROW,
              SCROLL_LEFT_COLUMNS,
              0,
              ReportTableToolbar.NONE,
              null);

        // Injections
        this.hostUI = hostui;
        this.hostInstrument = hostinstrument;
        }


    /***********************************************************************************************
     * Override the ReportTable.initialiseReport() to allow selections.
     *
     * @return JTable
     *
     * @throws ReportException
     */

    public synchronized JTable initialiseReport() throws ReportException
        {
        final JTable tableReport;

        tableReport = super.initialiseReport();

        if (tableReport != null)
            {
            // Listen for clicks on each Row
            tableReport.getSelectionModel().addListSelectionListener(new ListSelectionListener()
                {
                public void valueChanged(final ListSelectionEvent event)
                    {
                    final ListSelectionModel selectionModel;

                    // Ignore extra messages
                    if (event.getValueIsAdjusting())
                        {
                        return;
                        }

                    selectionModel = (ListSelectionModel)event.getSource();

                    if ((selectionModel != null)
                        && (!selectionModel.isSelectionEmpty()))
                        {
                        final int intRowSelected;

                        intRowSelected = selectionModel.getMinSelectionIndex();

                        // Retrieve the data from this row
                        if ((getReportTableModel() != null)
                            && (getReportTableModel().getRowCount() > 0))
                            {
                            final Vector vecSelectedRow;

                            vecSelectedRow = getReportTableModel().getRowAt(intRowSelected);

                            if (vecSelectedRow != null)
                                {
                                if (vecSelectedRow.size() == defineColumnWidths().length)
                                    {
                                    // This works!
                                    //System.out.println("Selected=" + (String)vecSelectedRow.get(1));
                                    }
                                }
                            }
                        }
                    }
                });
            }

        return (tableReport);
        }


    /***********************************************************************************************
     * Generate the report header.
     *
     * @return Vector
     */

    public final Vector<String> generateHeader()
        {
        final Vector<String> vecHeader;

        vecHeader = new Vector<String>(1);

        vecHeader.add(getReportUniqueName() + SPACE + MSG_REPORT_CREATED + SPACE + getObservatoryClock().getDateTimeNowAsString());

        return (vecHeader);
        }


    /***********************************************************************************************
     * Define the report columns.
     *
     * @return Vector
     */

    public final Vector<ReportColumnMetadata> defineColumns()
        {
        final Vector<ReportColumnMetadata> vecColumns;

        vecColumns = new Vector<ReportColumnMetadata>(defineColumnWidths().length);

        vecColumns.add(new ReportColumnMetadata(TITLE_ICON,
                                                SchemaDataType.STRING,
                                                SchemaUnits.DIMENSIONLESS,
                                                EMPTY_STRING,
                                                SwingConstants.CENTER));
        vecColumns.add(new ReportColumnMetadata(TITLE_IDENTIFIER,
                                                SchemaDataType.STRING,
                                                SchemaUnits.DIMENSIONLESS,
                                                EMPTY_STRING,
                                                SwingConstants.LEFT));
        vecColumns.add(new ReportColumnMetadata(TITLE_CATEGORY,
                                                SchemaDataType.STRING,
                                                SchemaUnits.DIMENSIONLESS,
                                                EMPTY_STRING,
                                                SwingConstants.LEFT));
        vecColumns.add(new ReportColumnMetadata(TITLE_VERSION,
                                                SchemaDataType.STRING,
                                                SchemaUnits.DIMENSIONLESS,
                                                EMPTY_STRING,
                                                SwingConstants.LEFT));
        vecColumns.add(new ReportColumnMetadata(TITLE_BUILD_DATE,
                                                SchemaDataType.STRING,
                                                SchemaUnits.DIMENSIONLESS,
                                                EMPTY_STRING,
                                                SwingConstants.LEFT));
        vecColumns.add(new ReportColumnMetadata(TITLE_BUILT_BY,
                                                SchemaDataType.STRING,
                                                SchemaUnits.DIMENSIONLESS,
                                                EMPTY_STRING,
                                                SwingConstants.LEFT));
        vecColumns.add(new ReportColumnMetadata(TITLE_AUTHOR,
                                                SchemaDataType.STRING,
                                                SchemaUnits.DIMENSIONLESS,
                                                EMPTY_STRING,
                                                SwingConstants.LEFT));
        vecColumns.add(new ReportColumnMetadata(TITLE_WEBSITE,
                                                SchemaDataType.STRING,
                                                SchemaUnits.DIMENSIONLESS,
                                                EMPTY_STRING,
                                                SwingConstants.LEFT));
        vecColumns.add(new ReportColumnMetadata(TITLE_EMAIL,
                                                SchemaDataType.STRING,
                                                SchemaUnits.DIMENSIONLESS,
                                                EMPTY_STRING,
                                                SwingConstants.LEFT));

        return (vecColumns);
        }


    /***********************************************************************************************
     * Define the widths of each column in terms of the objects which they will contain.
     *
     * @return Object []
     */

    public final Object [] defineColumnWidths()
        {
        final Object [] columnWidths =
            {
            ReportIcon.getIcon(ICON_DUMMY_DISTRIBUTION),
            "MMMMMMMMMMMMMMMMM",
            "MMMMMMMMM",
            "9.9.9.999 BETA",
            "MMMMMMMMMMMMMMMM",
            "MMMMMMM",
            "MMMMMMMMMMMMMMMM",
            "MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM",
            "MMMMMMMMMMMMMMMMMMMMMM"
            };

        return (columnWidths);
        }


    /***********************************************************************************************
     * Generate the report data table.
     *
     * @return Vector of report rows
     */

    public final Vector<Vector> generateReport()
        {
        final Vector<Vector> vecReport;

        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               "ObservatoryPluginsUIComponent.generateReport() [isselectedinstrument="
                                + ObservatoryUIHelper.isSelectedInstrument(getHostInstrument())
                                + "] [visible=" + UIComponentHelper.isUIComponentShowing(this) + "]");

        vecReport = new Vector<Vector>(30);

        // Only generate a Report if this UIComponent is visible
        // There is no need to auto-truncate
        if (UIComponentHelper.shouldRefresh(false, getHostInstrument(), this))
            {
            readDistributionJars(getHostUI(), vecReport);
            }
        else
            {
            LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                   "ObservatoryPluginsUIComponent.generateReport() NOT VISIBLE");
            }

        return (vecReport);
        }


    /***********************************************************************************************
     * Refresh the Report data.
     *
     * @return Vector
     */

    public Vector<Vector> refreshReport()
        {
        getReportTable().setRowHeight(ReportIcon.getIcon(ICON_DUMMY_DISTRIBUTION).getIconHeight());

        return (generateReport());
        }


    /***********************************************************************************************
     * Get the ObservatoryUI to which this UIComponent is attached.
     *
     * @return ObservatoryUIInterface
     */

    private synchronized ObservatoryUIInterface getHostUI()
        {
        return (this.hostUI);
        }


    /***********************************************************************************************
     * Get the ObservatoryInstrument to which this UIComponent is attached.
     *
     * @return ObservatoryInstrumentInterface
     */

    private synchronized ObservatoryInstrumentInterface getHostInstrument()
        {
        return (this.hostInstrument);
        }


    /***********************************************************************************************
     * Get the ObservatoryClock.
     *
     * @return ObservatoryClockInterface
     */

    private synchronized ObservatoryClockInterface getObservatoryClock()
        {
        final ObservatoryClockInterface clock;

        clock = getHostInstrument().getObservatoryClock();

        return (clock);
        }
    }
