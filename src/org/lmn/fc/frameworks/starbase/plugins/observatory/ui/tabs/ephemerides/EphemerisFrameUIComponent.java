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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.ephemerides;

import org.lmn.fc.common.actions.ContextAction;
import org.lmn.fc.common.utilities.astronomy.CoordinateConversions;
import org.lmn.fc.common.utilities.time.AstronomicalCalendar;
import org.lmn.fc.common.utilities.time.AstronomicalCalendarInterface;
import org.lmn.fc.common.utilities.ui.AlignedListCellRenderer;
import org.lmn.fc.frameworks.starbase.plugins.observatory.MetadataDictionary;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ObservatoryInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.ephemerides.EphemerisDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.starmap.ui.StarMapUIComponentUtilities;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.utilities.Epoch;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.DAOWrapperInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.InstrumentState;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryUIInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.DAOWrapper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.EphemeridesHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.InstrumentUIComponentDecorator;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.MetadataHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.EphemerisFrameUIComponentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.EphemerisUIComponentInterface;
import org.lmn.fc.model.datatypes.*;
import org.lmn.fc.model.registry.NavigationUtilities;
import org.lmn.fc.model.registry.RegistryModelUtilities;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.model.xmlbeans.ephemerides.Ephemeris;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;
import org.lmn.fc.ui.components.UIComponentHelper;
import org.lmn.fc.ui.reports.ReportTableHelper;
import org.lmn.fc.ui.reports.ReportTablePlugin;
import uk.me.jstott.util.JulianDateConverter;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.print.PageFormat;
import java.awt.print.PrinterJob;
import java.util.*;
import java.util.List;


/***************************************************************************************************
 * EphemerisFrameUIComponent.
 */

public final class EphemerisFrameUIComponent extends InstrumentUIComponentDecorator
                                             implements EphemerisFrameUIComponentInterface
    {
    private static final long serialVersionUID = -251510981775558598L;

    // UI
    private EphemerisUIComponentInterface uiEphemeris;
    private JToolBar toolBar;

    private final JComboBox comboTarget;
    private final JTextField textJD;
    private final JComboBox comboEpoch;
    private Epoch selectedEpoch;
    private final JTextField textInterval;

    private final JButton buttonToolbarResetJD;
    private final JButton buttonToolbarRecalculate;
    private JButton buttonPageSetup;
    private JButton buttonPrint;

    private boolean boolEnableEvents;
    private Ephemeris ephemeris;
    private List<Metadata> listMetadata;


    /***********************************************************************************************
     * Initialise the Commands Toolbar.
     *
     * @param toolbar
     * @param obsinstrument
     * @param efui
     * @param fontdata
     * @param colourforeground
     * @param colourbackground
     * @param debug
     */

    private static void initialiseCommandsToolbar(final JToolBar toolbar,
                                                  final ObservatoryInstrumentInterface obsinstrument,
                                                  final EphemerisFrameUIComponentInterface efui,
                                                  final FontInterface fontdata,
                                                  final ColourInterface colourforeground,
                                                  final ColourInterface colourbackground,
                                                  final boolean debug)
        {
        final String SOURCE = "EphemerisFrameUIComponent.initialiseCommandsToolbar() ";
        final JLabel labelName;
        final JLabel labelTarget;
        final JLabel labelJD;
        final JLabel labelEpoch;
        final JLabel labelIntervalSeconds;
        final ContextAction actionReset;
        final ContextAction actionRecalculate;
        final ContextAction actionPageSetup;
        final ContextAction actionPrint;

        toolbar.setBackground(colourbackground.getColor());

        //-------------------------------------------------------------------------------------
        // Initialise the Labels

        labelName = new JLabel(TITLE_EPHEMERIS,
                               RegistryModelUtilities.getAtomIcon(obsinstrument.getHostAtom(),
                                                                  ObservatoryInterface.FILENAME_ICON_EPHEMERIS),
                               SwingConstants.LEFT)
            {
            private static final long serialVersionUID = 7580736117336162922L;

            // Enable Antialiasing in Java 1.5
            protected void paintComponent(final Graphics graphics)
                {
                final Graphics2D graphics2D = (Graphics2D) graphics;

                // For antialiasing text
                graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                                            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                super.paintComponent(graphics2D);
                }
            };

        labelName.setFont(efui.getFontData().getFont().deriveFont(ReportTableHelper.SIZE_HEADER_FONT).deriveFont(Font.BOLD));
        labelName.setForeground(efui.getColourData().getColor());
        labelName.setIconTextGap(TOOLBAR_ICON_TEXT_GAP);

        //labelTarget = new JLabel(LABEL_TARGET);
        labelTarget = new JLabel(EMPTY_STRING);
        labelTarget.setFont(fontdata.getFont());
        labelTarget.setForeground(colourforeground.getColor());
        labelTarget.setToolTipText(TOOLTIP_TARGET);

        labelJD = new JLabel(LABEL_JULIAN_DATE);
        labelJD.setFont(fontdata.getFont());
        labelJD.setForeground(colourforeground.getColor());
        labelJD.setToolTipText(TOOLTIP_ENTER_JD_START);

        labelEpoch = new JLabel(LABEL_EPOCH);
        labelEpoch.setFont(fontdata.getFont());
        labelEpoch.setForeground(colourforeground.getColor());
        labelEpoch.setToolTipText(TOOLTIP_EPOCH);

        labelIntervalSeconds = new JLabel(LABEL_INTERVAL_SECONDS);
        labelIntervalSeconds.setFont(fontdata.getFont());
        labelIntervalSeconds.setForeground(colourforeground.getColor());
        labelIntervalSeconds.setToolTipText(TOOLTIP_INTERVAL);

        //-------------------------------------------------------------------------------------
        // Initialise the Buttons

        efui.getResetButton().setBorder(BORDER_BUTTON);
        efui.getResetButton().setHideActionText(true);
        efui.getResetButton().setEnabled(true);

        efui.getRecalculateButton().setBorder(BORDER_BUTTON);
        efui.getRecalculateButton().setHideActionText(true);

        // The button will be enabled when a valid Julian Date is entered
        efui.getRecalculateButton().setEnabled(false);

        efui.setPageSetupButton(new JButton());
        efui.getPageSetupButton().setBorder(BORDER_BUTTON);

        // Ensure that no text appears next to the Icon...
        efui.getPageSetupButton().setHideActionText(true);

        efui.setPrintButton(new JButton());
        efui.getPrintButton().setBorder(BORDER_BUTTON);

        // Ensure that no text appears next to the Icon...
        efui.getPrintButton().setHideActionText(true);

        //-------------------------------------------------------------------------------------
        // Reset

        actionReset = new ContextAction(ACTION_RESET,
                                        RegistryModelUtilities.getAtomIcon(efui.getHostInstrument().getHostAtom(),
                                                                           FILENAME_ICON_EPHEMERIS_RESET),
                                        ACTION_RESET,
                                        KeyEvent.VK_J,
                                        false,
                                        true)
            {
            final static String SOURCE = "ContextAction:Reset ";
            private static final long serialVersionUID = 5723242277990824195L;


            public void actionPerformed(final ActionEvent event)
                {
                final AstronomicalCalendarInterface calendarNow;

                // Get the Calendar from the Observatory Metadata, or the Framework if necessary
                calendarNow = EphemeridesHelper.getCalendarNow(REGISTRY.getFramework(),
                                                               efui.getHostInstrument(),
                                                               efui.getMetadata());

                // Set the default Julian Day back to 0hr UT today
                efui.getJDText().setText(Double.toString(calendarNow.getJD0()));

                doRecalculate(efui, SOURCE);
                }
            };

        efui.getResetButton().setAction(actionReset);
        efui.getResetButton().setToolTipText((String) actionReset.getValue(Action.SHORT_DESCRIPTION));
        efui.getResetButton().setEnabled(true);

        //-------------------------------------------------------------------------------------
        // Epochs

        StarMapUIComponentUtilities.createEpochCombo(efui.getEpochCombo(),
                                                     fontdata,
                                                     colourforeground,
                                                     DEFAULT_COLOUR_TAB_BACKGROUND,
                                                     efui);

        //-------------------------------------------------------------------------------------
        // Create the toolbar button to recalculate the Ephemeris

        actionRecalculate = new ContextAction(ACTION_RECALCULATE,
                                              RegistryModelUtilities.getAtomIcon(efui.getHostInstrument().getHostAtom(),
                                                                                 FILENAME_ICON_EPHEMERIS_RECALCULATE),
                                              ACTION_RECALCULATE,
                                              KeyEvent.VK_R,
                                              false,
                                              true)
            {
            final static String SOURCE = "ContextAction:Recalculate ";
            private static final long serialVersionUID = 6944747714371214404L;


            public void actionPerformed(final ActionEvent event)
                {
                doRecalculate(efui, SOURCE);
                }
            };

        efui.getRecalculateButton().setAction(actionRecalculate);
        efui.getRecalculateButton().setToolTipText((String) actionRecalculate.getValue(Action.SHORT_DESCRIPTION));
        efui.getRecalculateButton().setEnabled(false);

        //-------------------------------------------------------------------------------------
        // Printing
        //-------------------------------------------------------------------------------------
        // Page Setup

        actionPageSetup = new ContextAction(ReportTablePlugin.PREFIX_PAGE_SETUP + MSG_EPHEMERIS,
                                            RegistryModelUtilities.getCommonIcon(FILENAME_ICON_PAGE_SETUP),
                                            ReportTablePlugin.PREFIX_PAGE_SETUP + MSG_EPHEMERIS,
                                            KeyEvent.VK_S,
                                            false,
                                            true)
            {
            final static String SOURCE = "ContextAction:PageSetup ";
            private static final long serialVersionUID = 6802400471966299436L;


            public void actionPerformed(final ActionEvent event)
                {
                if (efui.getEphemerisUI() != null)
                    {
                    final PrinterJob printerJob;
                    final PageFormat pageFormat;

                    printerJob = PrinterJob.getPrinterJob();
                    pageFormat = printerJob.pageDialog(efui.getEphemerisUI().getPageFormat());

                    if (pageFormat != null)
                        {
                        efui.getEphemerisUI().setPageFormat(pageFormat);
                        }
                    }
                else
                    {
                    LOGGER.error(SOURCE + "TimeZonesUI unexpectedly NULL");
                    }
                }
            };

        efui.getPageSetupButton().setAction(actionPageSetup);
        efui.getPageSetupButton().setToolTipText((String) actionPageSetup.getValue(Action.SHORT_DESCRIPTION));
        efui.getPageSetupButton().setEnabled(true);

        //-------------------------------------------------------------------------------------
        // Print

        actionPrint = new ContextAction(ReportTablePlugin.PREFIX_PRINT + MSG_EPHEMERIS,
                                        RegistryModelUtilities.getCommonIcon(FILENAME_ICON_PRINT),
                                        ReportTablePlugin.PREFIX_PRINT + MSG_EPHEMERIS,
                                        KeyEvent.VK_P,
                                        false,
                                        true)
            {
            final static String SOURCE = "ContextAction:Print ";
            private static final long serialVersionUID = 8346968631811861938L;


            public void actionPerformed(final ActionEvent event)
                {
                // Check to see that we actually have a printer...
                if (PrinterJob.lookupPrintServices().length == 0)
                    {
                    JOptionPane.showMessageDialog(null,
                                                  ReportTablePlugin.MSG_NO_PRINTER,
                                                  ReportTablePlugin.PREFIX_PRINT + MSG_EPHEMERIS,
                                                  JOptionPane.WARNING_MESSAGE);
                    return;
                    }

                // Print the Report
                efui.getEphemerisUI().printReport();
                }
            };

        efui.getPrintButton().setAction(actionPrint);
        efui.getPrintButton().setToolTipText((String) actionPrint.getValue(Action.SHORT_DESCRIPTION));
        efui.getPrintButton().setEnabled(true);

        //-------------------------------------------------------------------------------------
        // Put it all together

        toolbar.removeAll();

        toolbar.addSeparator(DIM_TOOLBAR_SEPARATOR_BUTTON);
        toolbar.add(labelName);
        toolbar.addSeparator(DIM_TOOLBAR_SEPARATOR);

        toolbar.add(Box.createHorizontalGlue());

        toolbar.add(labelTarget);
        toolbar.addSeparator(DIM_LABEL_SEPARATOR);

        toolbar.add(createTargetCombo(efui, fontdata, colourforeground, colourbackground, debug));
        toolbar.addSeparator(DIM_TOOLBAR_SEPARATOR_BUTTON);

        toolbar.add(labelJD);
        toolbar.addSeparator(DIM_LABEL_SEPARATOR);

        toolbar.add(initialiseInputJD(efui, fontdata, colourforeground, colourbackground, debug));
        toolbar.addSeparator(DIM_TOOLBAR_SEPARATOR_BUTTON);

        toolbar.add(efui.getResetButton());
        toolbar.addSeparator(DIM_TOOLBAR_SEPARATOR_BUTTON);

        toolbar.add(labelEpoch);
        toolbar.addSeparator(DIM_LABEL_SEPARATOR);

        toolbar.add(efui.getEpochCombo());
        toolbar.addSeparator(DIM_TOOLBAR_SEPARATOR_BUTTON);

        toolbar.add(labelIntervalSeconds);
        toolbar.addSeparator(DIM_LABEL_SEPARATOR);

        toolbar.add(initialiseIntervalSeconds(efui, fontdata, colourforeground, colourbackground, debug));
        toolbar.addSeparator(DIM_TOOLBAR_SEPARATOR_BUTTON);

        toolbar.add(efui.getRecalculateButton());
        toolbar.addSeparator(DIM_TOOLBAR_SEPARATOR_BUTTON);

        toolbar.add(efui.getPageSetupButton());
        toolbar.addSeparator(DIM_TOOLBAR_SEPARATOR_BUTTON);

        toolbar.add(efui.getPrintButton());
        toolbar.addSeparator(DIM_TOOLBAR_SEPARATOR_BUTTON);

        NavigationUtilities.updateComponentTreeUI(toolbar);
        }


    /***********************************************************************************************
     * Recalculate the Ephemeris and reapply the data to the Instrument.
     * This UIComponent must behave like a DAO and return a DAOWrapper,
     * containing the Ephemeris data in the UserObject,
     * and Instrument Metadata describing the Ephemeris context.
     * The Metadata are not updated when the DAOWrapper is set.
     *
     * @param hostinstrument
     * @param ephemerisrecalc
     * @param toolbar
     * @param target
     * @param jdstart
     * @param interval
     * @param epoch
     */

    private static void recalculateEphemerisIntoDAOWrapper(final ObservatoryInstrumentInterface hostinstrument,
                                                           final Ephemeris ephemerisrecalc,
                                                           final JToolBar toolbar,
                                                           final String target,
                                                           final double jdstart,
                                                           final int interval,
                                                           final Epoch epoch)
        {
        final String SOURCE = "EphemerisFrameUIComponent.recalculateEphemerisIntoDAOWrapper() ";
        final Metadata metadataLongitude;
        final Metadata metadataLatitude;
        final Metadata metadataTimeZone;
        final List<Metadata> listAggregateMetadata;
        final List<String> errors;

        // This method is only ever called from the UI, so it is ok to show a MessageDialog
        if (InstrumentState.isOff(hostinstrument))
            {
            final String [] message =
                {
                "The Instrument must be switched on in order to generate an Ephemeris.",
                "Please click the small green button on the control panel and try again."
                };

            Toolkit.getDefaultToolkit().beep();
            JOptionPane.showMessageDialog(null,
                                          message,
                                          "Ephemeris Generator",
                                          JOptionPane.WARNING_MESSAGE);
            // This is the easiest way!
            return;
            }

        errors = new ArrayList<String>(10);

        // Update the Aggregate Metadata every time we recalculate
        listAggregateMetadata = MetadataHelper.collectAggregateMetadataTraced(REGISTRY.getFramework(),
                                                                              (ObservatoryInterface) hostinstrument.getHostAtom(),
                                                                              hostinstrument,
                                                                              hostinstrument.getDAO(), null,
                                                                              SOURCE,
                                                                              LOADER_PROPERTIES.isMetadataDebug());
        MetadataHelper.showMetadataList(listAggregateMetadata,
                                        SOURCE + "COMPOSITE AFTER collectAggregateMetadataTraced",
                                        LOADER_PROPERTIES.isMetadataDebug());
        MetadataHelper.showMetadataList(hostinstrument.getDAO().getInstrumentMetadata(),
                                        SOURCE + "hostinstrument.getDAO().getEphemerisMetadata() AFTER collectAggregateMetadataTraced",
                                        LOADER_PROPERTIES.isMetadataDebug());


        metadataLongitude = MetadataHelper.getMetadataByKey(listAggregateMetadata,
                                                            MetadataDictionary.KEY_OBSERVATORY_LONGITUDE.getKey());

        metadataLatitude = MetadataHelper.getMetadataByKey(listAggregateMetadata,
                                                           MetadataDictionary.KEY_OBSERVATORY_LATITUDE.getKey());

        metadataTimeZone = MetadataHelper.getMetadataByKey(listAggregateMetadata,
                                                           MetadataDictionary.KEY_OBSERVATORY_TIMEZONE.getKey());

        // See if we have the Observatory location in the AggregateMetadata
        if ((metadataLongitude != null)
            && (metadataLatitude != null)
            && (metadataTimeZone != null))
            {
            try
                {
                final DegMinSecInterface dmsLongitudeObservatory;
                final DegMinSecInterface dmsLatitudeObservatory;
                final TimeZone timeZoneObservatory;
                final EphemerisDAOInterface daoEphemeris;

                // Longitude -179:59:59.9999 to +000:00:00.0000 to +179:59:59.9999  deg:mm:ss
                dmsLongitudeObservatory = (DegMinSecInterface) DataTypeHelper.parseDataTypeFromValueField(metadataLongitude.getValue(),
                                                                                                          DataTypeDictionary.SIGNED_LONGITUDE,
                                                                                                          EMPTY_STRING,
                                                                                                          EMPTY_STRING,
                                                                                                          errors);
                // Latitude  -89:59:59.9999 to +00:00:00.0000 to +89:59:59.9999  deg:mm:ss
                dmsLatitudeObservatory = (DegMinSecInterface)DataTypeHelper.parseDataTypeFromValueField(metadataLatitude.getValue(),
                                                                                                        DataTypeDictionary.LATITUDE,
                                                                                                        EMPTY_STRING,
                                                                                                        EMPTY_STRING,
                                                                                                        errors);
                // This returns the GMT zone if the given ID cannot be understood
                timeZoneObservatory = TimeZone.getTimeZone(metadataTimeZone.getValue());

                // Do we have a valid Ephemeris?
                if (ephemerisrecalc != null)
                    {
                    daoEphemeris = EphemeridesHelper.getEphemerisDAOforName(hostinstrument,
                                                                            ephemerisrecalc.getName());
                    }
                else
                    {
                    daoEphemeris = null;
                    }

                // Do we now have the full context for the Ephemeris?
                if ((dmsLongitudeObservatory != null)
                    && (dmsLatitudeObservatory != null)
                    && (daoEphemeris != null)
                    && (hostinstrument != null)
                    && (hostinstrument.getDAO() != null)
                    && (errors.size() == 0))
                    {
                    final Vector<Object> vecEphemeris;

                    MetadataHelper.showMetadataList(hostinstrument.getDAO().getInstrumentMetadata(),
                                                    SOURCE + "hostinstrument.getDAO().getEphemerisMetadata() JUST BEFORE addEphemerisContextMetadata",
                                                    LOADER_PROPERTIES.isMetadataDebug());

                    // Pass the Metadata to the data consumers via the DAO's InstrumentMetadata,
                    // which is the same container as the Parameters of the associated Ephemeris Command
                    EphemeridesHelper.addEphemerisContextMetadata(hostinstrument.getDAO().getInstrumentMetadata(),
                                                                  target,
                                                                  jdstart,
                                                                  interval,
                                                                  epoch,
                                                                  dmsLongitudeObservatory,
                                                                  timeZoneObservatory);

                    // Make sure the Ephemeris DAO knows about the InstrumentMetadata,
                    // although this isn't used currently
                    daoEphemeris.setEphemerisMetadata(hostinstrument.getDAO().getInstrumentMetadata());

                    // Generate the Ephemeris, restricting to 24 hours range for simplicity
                    // Use the calculateTopocentricEphemeris() command if a greater range is required
                    vecEphemeris = EphemeridesHelper.generateEphemerisData(daoEphemeris,
                                                                           jdstart,
                                                                           jdstart + 1,
                                                                           interval,
                                                                           epoch,
                                                                           dmsLongitudeObservatory.toDouble(),
                                                                           dmsLatitudeObservatory.toDouble(),
                                                                           timeZoneObservatory);

                    // Pass to the DAO for use in EphemerisUIComponent.setWrappedData()
                    hostinstrument.getDAO().setUserObject(vecEphemeris);
                    }
                else
                    {
                    errors.add("Invalid Observatory location, Instrument DAO or Ephemeris DAO not found");
                    }
                }

            catch (NumberFormatException exception)
                {
                errors.add("Unable to parse Interval seconds");
                }
            }
        else
            {
            // If no Target selection has been made yet, then just fail silently
            if ((target != null)
                && (!EMPTY_STRING.equals(target)))
                {
                final String [] message =
                    {
                    "The Observatory location was not found in the Metadata for this Instrument",
                    "so it is not possible to generate an Ephemeris.",
                    "The Metadata required are:",
                    "      Observatory.Longitude",
                    "      Observatory.Latitude",
                    "      Observatory.TimeZone",
                    "Please import or edit the Observatory Metadata and try again.",
                    "Remember that the instrument must be switched on to generate an Ephemeris."
                    };

                Toolkit.getDefaultToolkit().beep();
                JOptionPane.showMessageDialog(null,
                                              message,
                                              "Ephemeris Generator",
                                              JOptionPane.WARNING_MESSAGE);
                }

            errors.add("The Observatory location was not found in the Metadata");
            }

        if (errors.size() == 0)
            {
            if ((hostinstrument != null)
                && (hostinstrument.getDAO() != null)
                && (hostinstrument.getInstrumentPanel() != null))
                {
                final DAOWrapperInterface daoWrapper;

                // Pass the data if any back via the DAO
                daoWrapper = new DAOWrapper(null,
                                            null,
                                            EMPTY_STRING,
                                            hostinstrument.getDAO());
                // NOTE!
                // Refresh the Ephemeris data regardless of visibility
                // We don't want to use the Metadata to update the *Toolbar* state,
                // since there's no need to change anything
                // This will end up in EphemerisUIComponent.setWrappedData()
                hostinstrument.getInstrumentPanel().setWrappedData(daoWrapper, true, false);
                }

            // Remind the User that it all worked!
            if ((toolbar != null)
                && (ephemerisrecalc != null))
                {
                toolbar.setToolTipText("Ephemeris for " + ephemerisrecalc.getName());
                }
            }
        else
            {
            if ((hostinstrument != null)
                && (hostinstrument.getDAO() != null))
                {
                hostinstrument.getDAO().setUserObject(null);
                }

            if (toolbar != null)
                {
                toolbar.setToolTipText(EMPTY_STRING);
                }

            LOGGER.errors(SOURCE, errors);
            }
        }


    /***********************************************************************************************
     * Initialise the JTextField for entry of the Julian Date.
     *
     * @param efui
     * @param fontdata
     * @param colourforeground
     * @param colourbackground
     * @param debug
     *
     * @return  JTextField
     */

    private static JTextField initialiseInputJD(final EphemerisFrameUIComponentInterface efui,
                                                final FontInterface fontdata,
                                                final ColourInterface colourforeground,
                                                final ColourInterface colourbackground,
                                                final boolean debug)
        {
        final AstronomicalCalendarInterface calendarNow;
        final DocumentListener listener;

        // Allow free-text entry for JD
        efui.getJDText().setMaximumSize(new Dimension(WIDTH_JD, HEIGHT_TOOLBAR_ICON - 4));
        efui.getJDText().setPreferredSize(new Dimension(WIDTH_JD, HEIGHT_TOOLBAR_ICON - 4));
        efui.getJDText().setMargin(new Insets(0, 5, 0, 5));
        efui.getJDText().setToolTipText(TOOLTIP_ENTER_JD_START);

        // We should have an initialised panel by now
        if (efui.getEphemerisUI() != null)
            {
            // Copy the style of the EphemerisPanel
            efui.getJDText().setForeground(colourforeground.getColor());
            efui.getJDText().setFont(fontdata.getFont());
            }

        // Get the Calendar from the Observatory Metadata, or the Framework if necessary
        calendarNow = EphemeridesHelper.getCalendarNow(REGISTRY.getFramework(),
                                                       efui.getHostInstrument(),
                                                       efui.getMetadata());

        // Set the default Julian Day at 0hr UT
        efui.getJDText().setText(Double.toString(calendarNow.getJD0()));

        // Text Field Listener
        listener = new DocumentListener()
            {
            public void insertUpdate(final DocumentEvent event)
                {
                if (efui.areEventsEnabled())
                    {
                    updateJD(efui.getJDText().getText().trim(),
                             efui.getRecalculateButton(),
                             efui.getEphemerisUI());
                    }
                }

            public void removeUpdate(final DocumentEvent event)
                {
                if (efui.areEventsEnabled())
                    {
                    updateJD(efui.getJDText().getText().trim(),
                             efui.getRecalculateButton(),
                             efui.getEphemerisUI());
                    }
                }

            public void changedUpdate(final DocumentEvent event)
                {
                if (efui.areEventsEnabled())
                    {
                    updateJD(efui.getJDText().getText().trim(),
                             efui.getRecalculateButton(),
                             efui.getEphemerisUI());
                    }
                }
            };

        efui.getJDText().getDocument().addDocumentListener(listener);

        return (efui.getJDText());
        }


    /***********************************************************************************************
     * Update the value of the Julian Date at the start, if possible.
     *
     * @param jd
     * @param button
     * @param ephemerispanel
     */

    private static void updateJD(final String jd,
                                 final JButton button,
                                 final EphemerisUIComponentInterface ephemerispanel)
        {
        final double dblJD;

        dblJD = CoordinateConversions.parseJD(jd);

        if ((dblJD >= 0.0)
            && (ephemerispanel != null))
            {
            // Allow recalculation
            button.setEnabled(true);
            }
        else
            {
            button.setEnabled(false);
            }
        }


    /***********************************************************************************************
     * Initialise the JTextField for entry of the Interval seconds.
     *
     * @param efui
     * @param fontdata
     * @param colourforeground
     * @param colourbackground
     * @param debug
     *
     * @return JTextField
     */

    private static JTextField initialiseIntervalSeconds(final EphemerisFrameUIComponentInterface efui,
                                                        final FontInterface fontdata,
                                                        final ColourInterface colourforeground,
                                                        final ColourInterface colourbackground,
                                                        final boolean debug)
        {
        final DocumentListener listener;

        // Allow free-text entry for Interval
        efui.getIntervalText().setMaximumSize(new Dimension(WIDTH_INTERVAL, HEIGHT_TOOLBAR_ICON - 4));
        efui.getIntervalText().setPreferredSize(new Dimension(WIDTH_INTERVAL, HEIGHT_TOOLBAR_ICON - 4));
        efui.getIntervalText().setMargin(new Insets(0, 5, 0, 5));
        efui.getIntervalText().setEnabled(true);
        efui.getIntervalText().setToolTipText(TOOLTIP_INTERVAL);

        // We should have an initialised panel by now
        if (efui.getEphemerisUI() != null)
            {
            // Copy the style of the EphemerisPanel
            efui.getIntervalText().setForeground(colourforeground.getColor());
            efui.getIntervalText().setFont(fontdata.getFont());
            }

        efui.getIntervalText().setText(Integer.toString(INTERVAL_DEFAULT));

        // Text Field Listener
        listener = new DocumentListener()
            {
            public void insertUpdate(final DocumentEvent event)
                {
                if (efui.areEventsEnabled())
                    {
                    updateInterval(efui.getIntervalText().getText().trim(),
                                   efui.getRecalculateButton(),
                                   efui.getEphemerisUI());
                    }
                }

            public void removeUpdate(final DocumentEvent event)
                {
                if (efui.areEventsEnabled())
                    {
                    updateInterval(efui.getIntervalText().getText().trim(),
                                   efui.getRecalculateButton(),
                                   efui.getEphemerisUI());
                    }
                }

            public void changedUpdate(final DocumentEvent event)
                {
                if (efui.areEventsEnabled())
                    {
                    updateInterval(efui.getIntervalText().getText().trim(),
                                   efui.getRecalculateButton(),
                                   efui.getEphemerisUI());
                    }
                }
            };

        efui.getIntervalText().getDocument().addDocumentListener(listener);

        return (efui.getIntervalText());
        }


    /***********************************************************************************************
     * Update the value of the Ephemeris Interval, if possible.
     *
     * @param interval
     * @param button
     * @param ephemerispanel
     */

    private static void updateInterval(final String interval,
                                       final JButton button,
                                       final EphemerisUIComponentInterface ephemerispanel)
        {
        try
            {
            final int intInterval;

            intInterval = Integer.parseInt(interval);

            if ((intInterval >= INTERVAL_MIN)
                && (intInterval <= INTERVAL_MAX)
                && (ephemerispanel != null))
                {
                // Allow recalculation
                button.setEnabled(true);
                }
            else
                {
                button.setEnabled(false);
                }
            }

        catch (NumberFormatException exception)
            {
            button.setEnabled(false);
            }
        }


    /***********************************************************************************************
     * Create a drop-down of Ephemeris targets.
     *
     * @param efui
     * @param fontdata
     * @param colourforeground
     * @param colourbackground
     * @param debug
     *
     * @return JComboBox
     */

    private static JComboBox createTargetCombo(final EphemerisFrameUIComponentInterface efui,
                                               final FontInterface fontdata,
                                               final ColourInterface colourforeground,
                                               final ColourInterface colourbackground,
                                               final boolean debug)
        {
        final String SOURCE = "EphemerisFrameUIComponent.createTargetCombo() ";
        final ActionListener choiceListener;

        if (efui.getEphemerisUI() != null)
            {
            // Copy the style of the EphemerisPanel
            efui.getTargetCombo().setFont(fontdata.getFont());
            efui.getTargetCombo().setForeground(colourforeground.getColor());
            efui.getTargetCombo().setRenderer(new AlignedListCellRenderer(SwingConstants.LEFT,
                                                                          fontdata,
                                                                          colourforeground,
                                                                          DEFAULT_COLOUR_TAB_BACKGROUND));
            }

        // Do NOT allow the combo box to take up all the remaining space!
        efui.getTargetCombo().setPreferredSize(new Dimension(WIDTH_TARGET, HEIGHT_TOOLBAR_ICON - 4));
        efui.getTargetCombo().setMaximumSize(new Dimension(WIDTH_TARGET, HEIGHT_TOOLBAR_ICON - 4));
        efui.getTargetCombo().setAlignmentX(0);

        efui.getTargetCombo().setToolTipText(TOOLTIP_TARGET);
        efui.getTargetCombo().setEnabled(true);
        efui.getTargetCombo().setEditable(false);

        //------------------------------------------------------------------------------------------
        // Enumerate the Ephemerides to build the combo box

        if ((efui.getHostInstrument().getHostAtom() != null)
            && (efui.getHostInstrument().getHostAtom() instanceof ObservatoryInterface))
            {
            final ObservatoryInterface observatory;
            final Hashtable<String, EphemerisDAOInterface> tableDAOs;

            observatory = (ObservatoryInterface)efui.getHostInstrument().getHostAtom();
            tableDAOs = observatory.getEphemerisDaoTable();

            if ((tableDAOs != null)
                && (!tableDAOs.isEmpty()))
                {
                final Enumeration<String> keys;
                final List<String> listKeys;
                int intRowCount;

                keys = tableDAOs.keys();
                listKeys = new ArrayList<String>(tableDAOs.size());
                intRowCount = 0;

                // First entry is blank, to force a selection
                efui.getTargetCombo().addItem(EMPTY_STRING);
                intRowCount++;

                while (keys.hasMoreElements())
                    {
                    listKeys.add(keys.nextElement());
                    }

                // Sort the Keys alphabetically to improve the readability
                Collections.sort(listKeys);

                for (int i = 0;
                     i < listKeys.size();
                     i++)
                    {
                    efui.getTargetCombo().addItem(listKeys.get(i));
                    intRowCount++;
                    }

                efui.getTargetCombo().setMaximumRowCount(intRowCount);
                }
            }

        // Beware that there might not have been any valid Ephemerides
        if (efui.getTargetCombo().getItemCount() > 0)
            {
            efui.getTargetCombo().setSelectedIndex(0);
            efui.getTargetCombo().revalidate();
            }

        choiceListener = new ActionListener()
            {
            public void actionPerformed(final ActionEvent event)
                {
                if (efui.getEphemerisUI() != null)
                    {
                    efui.setEphemerisFromName((String) efui.getTargetCombo().getSelectedItem());
                    efui.getEphemerisUI().setEphemerisFromName((String) efui.getTargetCombo().getSelectedItem());

                    efui.getRecalculateButton().setEnabled(false);
                    doRecalculate(efui, SOURCE);
                    }
                }
            };

        efui.getTargetCombo().addActionListener(choiceListener);

        return (efui.getTargetCombo());
        }


    /***********************************************************************************************
     * Recalculate the Ephemeris from the current input settings.
     *
     * @param efui
     * @param source
     */

    private static void doRecalculate(final EphemerisFrameUIComponentInterface efui,
                                      final String source)
        {
        try
            {
            final double dblJDStart;
            final int intIntervalSeconds;

            dblJDStart = Double.parseDouble(efui.getJDText().getText());
            intIntervalSeconds = Integer.parseInt(efui.getIntervalText().getText());

            // This generates the Report using the parameters from the Toolbar and the Observatory Metadata
            LOGGER.debugTimedEvent(LOADER_PROPERTIES.isMetadataDebug(),
                                   source + "Recalculate --> recalculateEphemerisIntoDAOWrapper()");

            if (efui.areEventsEnabled())
                {
                recalculateEphemerisIntoDAOWrapper(efui.getHostInstrument(),
                                                   efui.getEphemeris(),
                                                   efui.getToolBar(),
                                                   (String) efui.getTargetCombo().getSelectedItem(),
                                                   dblJDStart,
                                                   intIntervalSeconds,
                                                   efui.getSelectedEpoch());
                }
            }

        catch (NumberFormatException exception)
            {
            // This should never happen!
            }
        }


    /***********************************************************************************************
     * Construct a EphemerisFrameUIComponent.
     *
     * @param hostinstrument
     * @param instrumentxml
     * @param hostui
     * @param task
     * @param font
     * @param colour
     * @param resourcekey
     */

    public EphemerisFrameUIComponent(final ObservatoryInstrumentInterface hostinstrument,
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
              resourcekey);

        // UI
        this.uiEphemeris = null;
        this.toolBar = null;

        this.comboTarget = new JComboBox();
        this.textJD = new JTextField(LENGTH_JULIAN_DATE);
        this.comboEpoch = new JComboBox();
        this.selectedEpoch = null;
        this.textInterval = new JTextField(LENGTH_INTERVAL);

        this.buttonToolbarResetJD = new JButton();
        this.buttonToolbarRecalculate = new JButton();
        this.buttonPageSetup = null;
        this.buttonPrint = null;

        this.boolEnableEvents = true;

        this.ephemeris = null;
        this.listMetadata = null;
        }


    /**********************************************************************************************/
    /* UI State                                                                                   */
    /***********************************************************************************************
     * Initialise this UIComponent.
     */

    public void initialiseUI()
        {
        final String SOURCE = "EphemerisFrameUIComponent.initialiseUI() ";
        final List<Metadata> listAggregateMetadata;

        // Do NOT use super.initialiseUI() in this context!

        LOGGER.debug(isDebug(), SOURCE);

        // Set up some initial Metadata
        listAggregateMetadata = MetadataHelper.collectAggregateMetadataTraced(REGISTRY.getFramework(),
                                                                              (ObservatoryInterface) getHostInstrument().getHostAtom(),
                                                                              getHostInstrument(),
                                                                              getHostInstrument().getDAO(),
                                                                              null,
                                                                              SOURCE,
                                                                              LOADER_PROPERTIES.isMetadataDebug());
        setMetadata(listAggregateMetadata);

        // Colours
        setBackground(DEFAULT_COLOUR_CANVAS.getColor());

        // Create the EphemerisUIComponent and initialise it
        // Do this first to get the colours and fonts
        // This is the only creation of EphemerisUIComponent
        // Use the Framework ResourceKey, not the host component
        setEphemerisUI(new EphemerisUIComponent(getHostTask(),
                                                getHostInstrument(),
                                                REGISTRY.getFrameworkResourceKey()));
        // Set the default Julian Day at 0hr UT
        // This may be changed by User entries
        getEphemerisUI().initialiseUI();

        // Create the Ephemeris JToolBar and initialise it
        setToolBar(new JToolBar());
        getToolBar().setFloatable(false);
        getToolBar().setMinimumSize(DIM_TOOLBAR_SIZE);
        getToolBar().setPreferredSize(DIM_TOOLBAR_SIZE);
        getToolBar().setMaximumSize(DIM_TOOLBAR_SIZE);

        initialiseCommandsToolbar(getToolBar(),
                                  getHostInstrument(),
                                  this,
                                  getFontData(),
                                  getColourData(),
                                  DEFAULT_COLOUR_TAB_BACKGROUND,
                                  isDebug());

        // Put the components together
        add(getToolBar(), BorderLayout.NORTH);
        add((Component) getEphemerisUI(), BorderLayout.CENTER);

        setEventsEnabled(true);
        }


    /***********************************************************************************************
     * Run this UIComponent.
     */

    public void runUI()
        {
        final String SOURCE = "EphemerisFrameUIComponent.runUI() ";

        LOGGER.debug(isDebug(), SOURCE);

        super.runUI();

        if (getEphemerisUI() != null)
            {
            // runUI() will create ContextActions in the ReportTable and add to the EphemerisPanel
            // Then transfer from the EphemerisPanel to this EphemerisFrameUIComponent
            UIComponentHelper.runComponentAndTransferActions((Component) getEphemerisUI(), this);
            }
        }


    /***********************************************************************************************
     * Stop this UIComponent.
     */

    public void stopUI()
        {
        final String SOURCE = "EphemerisFrameUIComponent.stopUI() ";

        LOGGER.debug(isDebug(), SOURCE);

        super.stopUI();

        if (getEphemerisUI() != null)
            {
            getEphemerisUI().stopUI();
            }
        }


    /***********************************************************************************************
     * Dispose of all components of this UIComponent.
     */

    public void disposeUI()
        {
        final String SOURCE = "EphemerisFrameUIComponent.disposeUI() ";

        LOGGER.debug(isDebug(), SOURCE);

        stopUI();

        if (getToolBar() != null)
            {
            getToolBar().removeAll();
            setToolBar(null);
            }

        if (getEphemerisUI() != null)
            {
            getEphemerisUI().disposeUI();
            setEphemerisUI(null);
            }

        setEphemerisFromName(null);
        setMetadata(null);

        super.disposeUI();
        }


    /**********************************************************************************************/
    /* UI                                                                                         */
    /***********************************************************************************************
     * Get the Ephemeris UI.
     * This is public to allow exportEphemeris().
     *
     * @return EphemerisUIComponentInterface
     */

    public EphemerisUIComponentInterface getEphemerisUI()
        {
        return (this.uiEphemeris);
        }


    /***********************************************************************************************
     * Set the Ephemeris UI.
     *
     * @param ephemerisui
     */

    private void setEphemerisUI(final EphemerisUIComponentInterface ephemerisui)
        {
        this.uiEphemeris = ephemerisui;
        }


    /***********************************************************************************************
     * Set the Ephemeris on which this report is based, given its Name.
     *
     * @param ephemerisname
     */

    public void setEphemerisFromName(final String ephemerisname)
        {
        this.ephemeris = EphemeridesHelper.getEphemerisForName(getHostInstrument(), ephemerisname);
        }


    /***********************************************************************************************
     * Get the Ephemeris on which this report is based.
     *
     * @return Ephemeris
     */

    public Ephemeris getEphemeris()
        {
        return (this.ephemeris);
        }


    /***********************************************************************************************
     * Set the data from the DAO finished() method, or from any Command doing a realtime update.
     *
     * @param daowrapper
     * @param updatemetadata
     */

    public void setWrappedData(final DAOWrapperInterface daowrapper,
                               final boolean updatemetadata)
        {
        final String SOURCE = "EphemerisFrameUIComponent.setWrappedData() ";

        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isMetadataDebug(),
                               SOURCE + "--> MetadataHelper.collectAggregateMetadataTraced()");

        if (daowrapper != null)
            {
            // Now set the Toolbar to match the settings in the Metadata,
            // **but only if we came from a Command execution**
            if (updatemetadata)
                {
                final List<Metadata> listAggregateMetadata;
                final Metadata metadataTarget;
                final Metadata metadataLongitude;
                final Metadata metadataTimeZone;
                final Metadata metadataDateStart;
                final Metadata metadataTimeStart;
                final Metadata metadataInterval;
                final Metadata metadataEpoch;
                final List<String> errors;

                errors = new ArrayList<String>(10);

                // Command execution has produced the following Metadata:
                //      Instrument.Ephemerides.Ephemeris.Target
                //      Instrument.Ephemerides.Ephemeris.Date.Start
                //      Instrument.Ephemerides.Ephemeris.Time.Start
                //      Instrument.Ephemerides.Ephemeris.Date.End
                //      Instrument.Ephemerides.Ephemeris.Time.End
                //      Instrument.Ephemerides.Ephemeris.Time.Interval
                //      Instrument.Ephemerides.Ephemeris.Epoch
                // and has used:
                //      Observatory.Longitude
                //      Observatory.Latitude
                //      Observatory.HASL
                //      Observatory.TimeZone
                // all of which should be present in the Metadata in the Wrapper

                // Update the Aggregate Metadata every time we get new data
                listAggregateMetadata = MetadataHelper.collectAggregateMetadataTraced(REGISTRY.getFramework(),
                                                                                      (ObservatoryInterface) getHostInstrument().getHostAtom(),
                                                                                      getHostInstrument(),
                                                                                      getHostInstrument().getDAO(), daowrapper,
                                                                                      SOURCE,
                                                                                      LOADER_PROPERTIES.isMetadataDebug());
                setMetadata(listAggregateMetadata);

                MetadataHelper.showMetadataList(getMetadata(),
                                                SOURCE + "AFTER collectAggregateMetadataTraced",
                                                LOADER_PROPERTIES.isMetadataDebug());
                // Target
                metadataTarget = MetadataHelper.getMetadataByKey(getMetadata(),
                                                                 MetadataDictionary.KEY_INSTRUMENT_ROOT.getKey()
                                                                    + EphemeridesHelper.KEY_EPHEMERIS_TARGET);
                if (metadataTarget != null)
                    {
                    setEphemerisFromName(metadataTarget.getValue());

                    setEventsEnabled(false);
                    getTargetCombo().setSelectedItem(metadataTarget.getValue());
                    setEventsEnabled(true);

                    getRecalculateButton().setEnabled(false);
                    }
                else
                    {
                    errors.add(MetadataDictionary.KEY_INSTRUMENT_ROOT.getKey()
                                   + EphemeridesHelper.KEY_EPHEMERIS_TARGET
                                   + " is invalid");
                    setEphemerisFromName(null);
                    getTargetCombo().setSelectedIndex(0);

                    getRecalculateButton().setEnabled(false);
                    }

                // Julian Date Start
                metadataLongitude = MetadataHelper.getMetadataByKey(getMetadata(),
                                                                    MetadataDictionary.KEY_OBSERVATORY_LONGITUDE.getKey());

                metadataTimeZone = MetadataHelper.getMetadataByKey(getMetadata(),
                                                                   MetadataDictionary.KEY_OBSERVATORY_TIMEZONE.getKey());

                metadataDateStart = MetadataHelper.getMetadataByKey(getMetadata(),
                                                                    MetadataDictionary.KEY_INSTRUMENT_ROOT.getKey()
                                                                        + EphemeridesHelper.KEY_EPHEMERIS_DATE_START);
                metadataTimeStart = MetadataHelper.getMetadataByKey(getMetadata(),
                                                                    MetadataDictionary.KEY_INSTRUMENT_ROOT.getKey()
                                                                        + EphemeridesHelper.KEY_EPHEMERIS_TIME_START);
                if ((metadataLongitude != null)
                    && (metadataTimeZone != null)
                    && (metadataDateStart != null)
                    && (metadataTimeStart != null))
                    {
                    final DegMinSecInterface dmsLongitudeObservatory;

                    // Longitude -179:59:59.9999 to +000:00:00.0000 to +179:59:59.9999  deg:mm:ss
                    dmsLongitudeObservatory = (DegMinSecInterface)DataTypeHelper.parseDataTypeFromValueField(metadataLongitude.getValue(),
                                                                                                             DataTypeDictionary.SIGNED_LONGITUDE,
                                                                                                             EMPTY_STRING,
                                                                                                             EMPTY_STRING,
                                                                                                             errors);
                    if ((dmsLongitudeObservatory != null)
                        && (errors.size() == 0))
                        {
                        try
                            {
                            final double dblLongitudeObservatory;
                            final TimeZone timeZoneObservatory;
                            final StringBuffer buffer;
                            final double dblJDStart;

                            // These must be correct to have passed the Metadata parsing above
                            dblLongitudeObservatory = dmsLongitudeObservatory.toDouble();

                            // This returns the GMT zone if the given ID cannot be understood
                            timeZoneObservatory = TimeZone.getTimeZone(metadataTimeZone.getValue());

                            buffer = new StringBuffer();
                            buffer.append(metadataDateStart.getValue());
                            buffer.append(" ");
                            buffer.append(metadataTimeStart.getValue());

                            dblJDStart = JulianDateConverter.dateToJulian(new AstronomicalCalendar(buffer.toString(),
                                                                                                   timeZoneObservatory,
                                                                                                   dblLongitudeObservatory));
                            setEventsEnabled(false);
                            getJDText().setText(DecimalFormatPattern.JD.format(dblJDStart));
                            setEventsEnabled(true);
                            }

                        catch (Exception exception)
                            {
                            errors.add("Could not create an AstronomicalCalendar");
                            getJDText().setText(MSG_UNKNOWN);
                            }
                        }
                    else
                        {
                        errors.add("Observatory.Longitude is invalid");
                        getJDText().setText(MSG_UNKNOWN);
                        }
                    }
                else
                    {
                    //errors.add("Supplied Observatory Metadata are invalid");
                    LOGGER.error("Supplied Observatory Metadata are invalid");
                    getJDText().setText(MSG_UNKNOWN);
                    }

                // Interval
                metadataInterval = MetadataHelper.getMetadataByKey(getMetadata(),
                                                                   MetadataDictionary.KEY_INSTRUMENT_ROOT.getKey()
                                                                        + EphemeridesHelper.KEY_EPHEMERIS_INTERVAL);
                if (metadataInterval != null)
                    {
                    setEventsEnabled(false);
                    getIntervalText().setText(metadataInterval.getValue());
                    setEventsEnabled(true);
                    }
                else
                    {
                    //errors.add("Instrument.Ephemeris.Time.Interval is invalid");
                    LOGGER.error(MetadataDictionary.KEY_INSTRUMENT_ROOT.getKey()
                                   + EphemeridesHelper.KEY_EPHEMERIS_INTERVAL
                                   + " is invalid");
                    getIntervalText().setText(Integer.toString(INTERVAL_DEFAULT));
                    }

                // Epoch
                metadataEpoch = MetadataHelper.getMetadataByKey(getMetadata(),
                                                                MetadataDictionary.KEY_INSTRUMENT_ROOT.getKey()
                                                                    + EphemeridesHelper.KEY_EPHEMERIS_EPOCH);
                if (metadataEpoch != null)
                    {
                    final Epoch epochInMetadata;

                    setEventsEnabled(false);

                    // Remember to set the enum Object, not just its name
                    epochInMetadata = Epoch.getEpochForName(metadataEpoch.getValue());

                    if (epochInMetadata != null)
                        {
                        getEpochCombo().setSelectedItem(epochInMetadata);
                        }
                    else
                        {
                        getEpochCombo().setSelectedItem(null);
                        }

                    setEventsEnabled(true);
                    }
                else
                    {
                    //errors.add("Instrument.Ephemerides.Ephemeris.Epoch is invalid");
                    LOGGER.error(MetadataDictionary.KEY_INSTRUMENT_ROOT.getKey()
                                   + EphemeridesHelper.KEY_EPHEMERIS_EPOCH
                                   + " is invalid");
                    getEpochCombo().setSelectedIndex(0);
                    }

                if (errors.size() > 0)
                    {
                    LOGGER.errors(SOURCE, errors);
                    }
                }
            else
                {
                // We don't need to change anything!
                // This is because the data were generated by this Toolbar, so nothing changes
                }
            }
        else
            {
            setEphemerisFromName(null);
            setMetadata(null);
            }

        setEventsEnabled(true);
        }


    /***********************************************************************************************
     * Get the Aggregate Metadata.
     *
     * @return List<Metadata>
     */

    public List<Metadata> getMetadata()
        {
        return (this.listMetadata);
        }


    /***********************************************************************************************
     * Set the Aggregate Metadata.
     *
     * @param metadata
     */

    private void setMetadata(final List<Metadata> metadata)
        {
        this.listMetadata = metadata;
        }


    /***********************************************************************************************
     * Get the Ephemeris JToolBar.
     *
     * @return JToolBar
     */

    public JToolBar getToolBar()
        {
        return (this.toolBar);
        }


    /***********************************************************************************************
     * Set the Ephemeris JToolBar.
     *
     * @param toolbar
     */

    private void setToolBar(final JToolBar toolbar)
        {
        this.toolBar = toolbar;
        }


    /***********************************************************************************************
     * Get the Ephemeris Target combo box.
     *
     * @return JComboBox
     */

    public JComboBox getTargetCombo()
        {
        return (this.comboTarget);
        }


    /***********************************************************************************************
     * Get the Ephemeris Julian Date entry box.
     *
     * @return JTextField
     */

    public JTextField getJDText()
        {
        return (this.textJD);
        }


    /***********************************************************************************************
     * Get the selected Epoch.
     *
     * @return Epoch
     */

    public Epoch getSelectedEpoch()
        {
        final Epoch epoch;

        if (this.selectedEpoch != null)
            {
            epoch = this.selectedEpoch;
            }
        else
            {
            epoch = Epoch.J2000;
            }

        return (epoch);
        }


    /***********************************************************************************************
     * Set the selected Epoch.
     *
     * @param epoch
     */

    public void setSelectedEpoch(final Epoch epoch)
        {
        final String SOURCE = "EphemerisFrameUIComponent.setSelectedEpoch() ";

        this.selectedEpoch = epoch;

        doRecalculate(this, SOURCE);
        }


    /***********************************************************************************************
     * Get the Ephemeris Epoch combo box.
     *
     * @return JComboBox
     */

    public JComboBox getEpochCombo()
        {
        return (this.comboEpoch);
        }


    /***********************************************************************************************
     * Get the Step seconds entry box.
     *
     * @return JTextField
     */

    public JTextField getIntervalText()
        {
        return (this.textInterval);
        }


    /***********************************************************************************************
     * Get the JButton used to reset the JulianDate.
     *
     * @return JButton
     */

    public JButton getResetButton()
        {
        return (this.buttonToolbarResetJD);
        }


    /***********************************************************************************************
     * Get the JButton used to recalculate the Ephemeris.
     *
     * @return JButton
     */

    public JButton getRecalculateButton()
        {
        return (this.buttonToolbarRecalculate);
        }


    /***********************************************************************************************
     * Get the PageSetup button.
     *
     * @return JButton
     */

    public JButton getPageSetupButton()
        {
        return (this.buttonPageSetup);
        }


    /***********************************************************************************************
     * Set the PageSetup button.
     *
     * @param button
     */

    public void setPageSetupButton(final JButton button)
        {
        this.buttonPageSetup = button;
        }


    /***********************************************************************************************
     * Get the Print button.
     *
     * @return JButton
     */

    public JButton getPrintButton()
        {
        return (this.buttonPrint);
        }


    /***********************************************************************************************
     * Set the Print button.
     *
     * @param button
     */

    public void setPrintButton(final JButton button)
        {
        this.buttonPrint = button;
        }


    /***********************************************************************************************
     * Indicate if Events are currently enabled, i.e. for listeners on Toolbar controls.
     *
     * @return boolean
     */

    public boolean areEventsEnabled()
        {
        return (this.boolEnableEvents);
        }


    /***********************************************************************************************
     * Indicate if Events are currently enabled, i.e. for listeners on Toolbar controls.
     *
     * @param enabled
     */

    private void setEventsEnabled(final boolean enabled)
        {
        this.boolEnableEvents = enabled;
        }
    }
