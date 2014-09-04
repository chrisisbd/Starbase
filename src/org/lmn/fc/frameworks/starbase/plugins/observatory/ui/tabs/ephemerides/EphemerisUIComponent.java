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

import org.lmn.fc.common.exceptions.ReportException;
import org.lmn.fc.frameworks.starbase.plugins.observatory.MetadataDictionary;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ObservatoryInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.DAOWrapperInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryClockInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.EphemeridesHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.MetadataHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.EphemerisUIComponentInterface;
import org.lmn.fc.model.datatypes.DataTypeDictionary;
import org.lmn.fc.model.datatypes.DataTypeHelper;
import org.lmn.fc.model.datatypes.DegMinSecFormat;
import org.lmn.fc.model.datatypes.DegMinSecInterface;
import org.lmn.fc.model.plugins.FrameworkPlugin;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.model.xmlbeans.datatypes.SchemaDataType;
import org.lmn.fc.model.xmlbeans.ephemerides.Ephemeris;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;
import org.lmn.fc.model.xmlbeans.metadata.SchemaUnits;
import org.lmn.fc.ui.components.UIComponentHelper;
import org.lmn.fc.ui.reports.ReportColumnMetadata;
import org.lmn.fc.ui.reports.ReportTable;
import org.lmn.fc.ui.reports.ReportTableToolbar;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.Vector;


/***************************************************************************************************
 * An EphemerisUIComponent.
 */

public final class EphemerisUIComponent extends ReportTable
                                        implements EphemerisUIComponentInterface
    {
    private static final long serialVersionUID = 909700801782822486L;

    // Injections
    private final ObservatoryInstrumentInterface hostInstrument;

    // The Ephemeris and its Data
    private Ephemeris ephemeris;
    private List<Metadata> listMetadata;
    private Vector<Object> vecEphemerisData;


    /***********************************************************************************************
     * Create the Metadata associated with the Ephemeris Report.
     * ToDo One day this could be done automatically by a rewrite of ReportColumnMetadata
     * to include the Metadata as below. Then all data are retrieved from the same place.
     *
     * @param framework
     * @param observatory
     * @param hostinstrument
     * @param wrapper
     * @param dao
     * @param tracemessage
     * @param traceon
     *
     * @return List<Metadata>
     */

    public static List<Metadata> createEphemerisMetadata(final FrameworkPlugin framework,
                                                         final ObservatoryInterface observatory,
                                                         final ObservatoryInstrumentInterface hostinstrument,
                                                         final DAOWrapperInterface wrapper,
                                                         final ObservatoryInstrumentDAOInterface dao,
                                                         final String tracemessage,
                                                         final boolean traceon)
        {
        final String SOURCE = "EphemerisUIComponent.createEphemerisMetadata() ";
        final List<Metadata> listMetadata;
        final List<Metadata> listAggregateMetadata;
        final Metadata metadataTarget;
        final Metadata metadataDateStart;
        final Metadata metadataTimeStart;
        final Metadata metadataInterval;
        final Metadata metadataEpoch;
        final Metadata metadataObservatory;
        final Metadata metadataLongitude;
        final Metadata metadataLatitude;
        final Metadata metadataHASL;
        final Metadata metadataTimeZone;
        final List<String> errors;

        // Each column has Name, Regex, Datatype, Units and Description
        listMetadata = new ArrayList<Metadata>((COLUMN_COUNT * 5) + 10);

        errors = new ArrayList<String>(10);

        // Date
        MetadataHelper.addColumnMetadata(listMetadata,
                                         TITLE_EPHEMERIS_DATE,
                                         REGEX_DATE_ISO_YYYY_MM_DD,
                                         DataTypeDictionary.DATE_YYYY_MM_DD,
                                         SchemaUnits.YEAR_MONTH_DAY,
                                         "The Date of the Ephemeris entry");
        // UT
        MetadataHelper.addColumnMetadata(listMetadata,
                                         TITLE_UT,
                                         REGEX_TIME_ISO_HH_MM_SS,
                                         DataTypeDictionary.TIME_HH_MM_SS,
                                         SchemaUnits.HOUR_MIN_SEC,
                                         "The Universal Time of the Ephemeris entry");
        // Julian Date
        MetadataHelper.addColumnMetadata(listMetadata,
                                         TITLE_JD,
                                         REGEX_SIGNED_DECIMAL_FLOAT,
                                         DataTypeDictionary.DECIMAL_FLOAT,
                                         SchemaUnits.DAYS,
                                         "The Julian Day of the Ephemeris entry");
        // LAST
        MetadataHelper.addColumnMetadata(listMetadata,
                                         TITLE_LAST,
                                         REGEX_TIME_ISO_HH_MM_SS,
                                         DataTypeDictionary.TIME_HH_MM_SS,
                                         SchemaUnits.HOUR_MIN_SEC,
                                         "The Local Apparent Sidereal Time of the Ephemeris entry");
        // Azimuth
        MetadataHelper.addColumnMetadata(listMetadata,
                                         TITLE_AZIMUTH,
                                         REGEX_AZIMUTH_DEG_UNSIGNED,
                                         DataTypeDictionary.AZIMUTH,
                                         SchemaUnits.DEGREES,
                                         "The Azimuth of the Ephemeris entry");
        // True Elev
        MetadataHelper.addColumnMetadata(listMetadata,
                                         TITLE_ELEVATION_TRUE,
                                         REGEX_ELEVATION_DEG_UNSIGNED,
                                         DataTypeDictionary.ELEVATION,
                                         SchemaUnits.DEGREES,
                                         "The True Elevation of the Ephemeris entry");
        // Apparent Elev
        MetadataHelper.addColumnMetadata(listMetadata,
                                         TITLE_ELEVATION_APPARENT,
                                         REGEX_ELEVATION_DEG_UNSIGNED,
                                         DataTypeDictionary.ELEVATION,
                                         SchemaUnits.DEGREES,
                                         "The Apparent Elevation of the Ephemeris entry");
        // Right Ascension
        MetadataHelper.addColumnMetadata(listMetadata,
                                         TITLE_RA,
                                         REGEX_RIGHT_ASCENSION_HMS,
                                         DataTypeDictionary.RIGHT_ASCENSION,
                                         SchemaUnits.HOUR_MIN_SEC,
                                         "The Right Ascension of the Ephemeris entry");
        // Declination
        MetadataHelper.addColumnMetadata(listMetadata,
                                         TITLE_DEC,
                                         REGEX_DECLINATION_DEG_SIGNED,
                                         DataTypeDictionary.DECLINATION,
                                         SchemaUnits.DEGREES,
                                         "The Declination of the Ephemeris entry");
        // Galactic l
        MetadataHelper.addColumnMetadata(listMetadata,
                                         TITLE_GALACTIC_LONGITUDE,
                                         REGEX_LONGITUDE_DMS_UNSIGNED,
                                         DataTypeDictionary.GALACTIC_LONGITUDE,
                                         SchemaUnits.DEGREES,
                                         "The Galactic Longitude of the Ephemeris entry");
        // Galactic b
        MetadataHelper.addColumnMetadata(listMetadata,
                                         TITLE_GALACTIC_LATITUDE,
                                         REGEX_LATITUDE_DEG_SIGNED,
                                         DataTypeDictionary.GALACTIC_LATITUDE,
                                         SchemaUnits.DEGREES,
                                         "The Galactic Latitude of the Ephemeris entry");

        // Add the Metadata for the context of the generated Ephemeris,
        // as given in the report header

        listAggregateMetadata = MetadataHelper.collectAggregateMetadataTraced(framework,
                                                                              observatory,
                                                                              hostinstrument,
                                                                              dao, wrapper,
                                                                              SOURCE,
                                                                              traceon);

        // Try to find the Observatory topocentric information in the supplied Metadata
        metadataObservatory = MetadataHelper.getMetadataByKey(listAggregateMetadata,
                                                              MetadataDictionary.KEY_OBSERVATORY_NAME.getKey());

        metadataLongitude = MetadataHelper.getMetadataByKey(listAggregateMetadata,
                                                            MetadataDictionary.KEY_OBSERVATORY_LONGITUDE.getKey());

        metadataLatitude = MetadataHelper.getMetadataByKey(listAggregateMetadata,
                                                           MetadataDictionary.KEY_OBSERVATORY_LATITUDE.getKey());

        metadataHASL = MetadataHelper.getMetadataByKey(listAggregateMetadata,
                                                       MetadataDictionary.KEY_OBSERVATORY_HASL.getKey());

        metadataTimeZone = MetadataHelper.getMetadataByKey(listAggregateMetadata,
                                                           MetadataDictionary.KEY_OBSERVATORY_TIMEZONE.getKey());

        // Only proceed if we have a complete valid Observatory location
        if ((metadataObservatory != null)
            && (metadataLongitude != null)
            && (metadataLatitude != null)
            && (metadataHASL != null)
            && (metadataTimeZone != null))
            {
            final DegMinSecInterface dmsLongitudeObservatory;
            final DegMinSecInterface dmsLatitudeObservatory;
            final TimeZone timeZoneObservatory;

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

            if ((dmsLongitudeObservatory != null)
                && (dmsLatitudeObservatory != null)
                && (errors.size() == 0))
                {
                dmsLongitudeObservatory.setDisplayFormat(DegMinSecFormat.SIGN);
                dmsLatitudeObservatory.setDisplayFormat(DegMinSecFormat.SIGN);

                MetadataHelper.addOrUpdateMetadataItemTraced(listMetadata,
                                                             metadataObservatory,
                                                             tracemessage,
                                                             traceon);
                MetadataHelper.addOrUpdateMetadataItemTraced(listMetadata,
                                                             metadataLongitude,
                                                             tracemessage,
                                                             traceon);
                MetadataHelper.addOrUpdateMetadataItemTraced(listMetadata,
                                                             metadataLatitude,
                                                             tracemessage,
                                                             traceon);
                MetadataHelper.addOrUpdateMetadataItemTraced(listMetadata,
                                                             metadataHASL,
                                                             tracemessage,
                                                             traceon);
                MetadataHelper.addOrUpdateMetadataItemTraced(listMetadata,
                                                             metadataTimeZone,
                                                             tracemessage,
                                                             traceon);
                }
            else
                {
                LOGGER.error(SOURCE + "Invalid Metadata for Ephemeris Location");
                LOGGER.errors(SOURCE, errors);
                }
            }
        else
            {
            LOGGER.error(SOURCE + "Invalid Metadata for Ephemeris Location");
            LOGGER.errors(SOURCE, errors);
            }

        // Now add the Ephemeris Parameters
        metadataTarget = MetadataHelper.getMetadataByKey(listAggregateMetadata,
                                                         MetadataDictionary.KEY_INSTRUMENT_ROOT.getKey()
                                                            + EphemeridesHelper.KEY_EPHEMERIS_TARGET);
        metadataDateStart = MetadataHelper.getMetadataByKey(listAggregateMetadata,
                                                            MetadataDictionary.KEY_INSTRUMENT_ROOT.getKey()
                                                                + EphemeridesHelper.KEY_EPHEMERIS_DATE_START);
        metadataTimeStart = MetadataHelper.getMetadataByKey(listAggregateMetadata,
                                                            MetadataDictionary.KEY_INSTRUMENT_ROOT.getKey()
                                                                + EphemeridesHelper.KEY_EPHEMERIS_TIME_START);
        metadataInterval = MetadataHelper.getMetadataByKey(listAggregateMetadata,
                                                           MetadataDictionary.KEY_INSTRUMENT_ROOT.getKey()
                                                                + EphemeridesHelper.KEY_EPHEMERIS_INTERVAL);
        metadataEpoch = MetadataHelper.getMetadataByKey(listAggregateMetadata,
                                                        MetadataDictionary.KEY_INSTRUMENT_ROOT.getKey()
                                                            + EphemeridesHelper.KEY_EPHEMERIS_EPOCH);
        if ((metadataTarget != null)
            && (metadataDateStart != null)
            && (metadataTimeStart != null)
            && (metadataInterval != null)
            && (metadataEpoch != null))
            {
            MetadataHelper.addOrUpdateMetadataItemTraced(listMetadata,
                                                         metadataTarget,
                                                         tracemessage,
                                                         traceon);
            MetadataHelper.addOrUpdateMetadataItemTraced(listMetadata,
                                                         metadataDateStart,
                                                         tracemessage,
                                                         traceon);
            MetadataHelper.addOrUpdateMetadataItemTraced(listMetadata,
                                                         metadataTimeStart,
                                                         tracemessage,
                                                         traceon);
            MetadataHelper.addOrUpdateMetadataItemTraced(listMetadata,
                                                         metadataInterval,
                                                         tracemessage,
                                                         traceon);
            MetadataHelper.addOrUpdateMetadataItemTraced(listMetadata,
                                                         metadataEpoch,
                                                         tracemessage,
                                                         traceon);
            }
        else
            {
            LOGGER.error(SOURCE + "Invalid Metadata for Ephemeris Parameters");
            LOGGER.errors(SOURCE, errors);
            }

        return (listMetadata);
        }


    /***********************************************************************************************
     * Construct an EphemerisUIComponent for the specified Ephemeris.
     * The injected ObservatoryInstrument provides the EventLog data.
     * The ResourceKey is always that of the host Framework, since this is a general utility.
     *
     * @param task
     * @param hostinstrument
     * @param resourcekey
     */

    public EphemerisUIComponent(final TaskPlugin task,
                                final ObservatoryInstrumentInterface hostinstrument,
                                final String resourcekey)
        {
        super(task,
              HEADER_TITLE,
              resourcekey,
              PRINTABLE,
              EXPORTABLE,
              REFRESHABLE,
              REFRESH_CLICK,
              NON_REORDERABLE,
              NON_TRUNCATEABLE,
              LOCK_TOP_ROW,
              SCROLL_LEFT_COLUMNS,
              0,
              ReportTableToolbar.NONE,
              null);

        // Injections
        this.hostInstrument = hostinstrument;

        // The Ephemeris and its Data
        this.ephemeris = null;
        this.listMetadata = null;
        this.vecEphemerisData = null;

        // Uniquely identify this UIComponent, use in the absence of an Ephemeris name
        if ((hostinstrument != null)
            && (hostinstrument.getInstrument() != null))
            {
            setReportUniqueName(hostinstrument.getInstrument().getName()+ SPACE + getReportTabName());
            }
        }


    /**********************************************************************************************/
    /* UI State                                                                                   */
    /***********************************************************************************************
     * initialiseUI().
     */

    public synchronized void initialiseUI()
        {
        super.initialiseUI();

        setEphemerisFromName(null);
        setMetadata(null);
        setEphemerisData(null);
        }


    /***********************************************************************************************
     * Dispose of all UI components and remove the Toolbar Actions.
     */

    public synchronized void disposeUI()
        {
        setEphemerisFromName(null);
        setMetadata(null);
        setEphemerisData(null);

        super.disposeUI();
        }


    /***********************************************************************************************
     * Generate the report header.
     * This assumes the Metadata have not changed since the report itself was generated.
     *
     * @return Vector<String>
     */

    public final Vector<String> generateHeader()
        {
        final String SOURCE = "EphemerisUIComponent.generateHeader() ";
        final Vector<String> vecHeader;
        final List<String> errors;

        final Metadata metadataTarget;
        final Metadata metadataDateStart;
        final Metadata metadataTimeStart;
        final Metadata metadataInterval;
        final Metadata metadataEpoch;

        final Metadata metadataObservatory;
        final Metadata metadataLongitude;
        final Metadata metadataLatitude;
        final Metadata metadataHASL;
        final Metadata metadataTimeZone;

        vecHeader = new Vector<String>(10);
        errors = new ArrayList<String>(10);

        //-----------------------------------------------------------------------------------------
        // Title

        if ((getEphemeris() != null)
            && (getEphemeris().getName() != null))
            {
            vecHeader.add(getEphemeris().getName() + SPACE + HEADER_TITLE);
            }
        else
            {
            vecHeader.add(getReportUniqueName());
            }

        vecHeader.add(EMPTY_STRING);

        //-----------------------------------------------------------------------------------------
        // Location

        // Now try to find the Observatory topocentric information in the supplied Metadata
        metadataObservatory = MetadataHelper.getMetadataByKey(getMetadata(),
                                                              MetadataDictionary.KEY_OBSERVATORY_NAME.getKey());

        metadataLongitude = MetadataHelper.getMetadataByKey(getMetadata(),
                                                            MetadataDictionary.KEY_OBSERVATORY_LONGITUDE.getKey());

        metadataLatitude = MetadataHelper.getMetadataByKey(getMetadata(),
                                                           MetadataDictionary.KEY_OBSERVATORY_LATITUDE.getKey());

        metadataHASL = MetadataHelper.getMetadataByKey(getMetadata(),
                                                       MetadataDictionary.KEY_OBSERVATORY_HASL.getKey());

        metadataTimeZone = MetadataHelper.getMetadataByKey(getMetadata(),
                                                           MetadataDictionary.KEY_OBSERVATORY_TIMEZONE.getKey());

        // Only proceed if we have a complete valid Observatory location

        if ((metadataLongitude != null)
            && (metadataLatitude != null)
            && (metadataHASL != null)
            && (metadataTimeZone != null))
            {
            final DegMinSecInterface dmsLongitudeObservatory;
            final DegMinSecInterface dmsLatitudeObservatory;
            final TimeZone timeZoneObservatory;

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

            if ((dmsLongitudeObservatory != null)
                && (dmsLatitudeObservatory != null)
                && (errors.size() == 0))
                {
                dmsLongitudeObservatory.setDisplayFormat(DegMinSecFormat.SIGN);
                dmsLatitudeObservatory.setDisplayFormat(DegMinSecFormat.SIGN);

                if (metadataObservatory != null)
                    {
                    vecHeader.add(MetadataDictionary.KEY_OBSERVATORY_NAME.getKey() + EQUALS_SPACED + metadataObservatory.getValue());
                    }

                vecHeader.add(MetadataDictionary.KEY_OBSERVATORY_LONGITUDE.getKey() + EQUALS_SPACED + dmsLongitudeObservatory.toString());
                vecHeader.add(MetadataDictionary.KEY_OBSERVATORY_LATITUDE.getKey() + EQUALS_SPACED + dmsLatitudeObservatory.toString());
                vecHeader.add(MetadataDictionary.KEY_OBSERVATORY_TIMEZONE.getKey() + EQUALS_SPACED + timeZoneObservatory.getDisplayName());
                vecHeader.add(MetadataDictionary.KEY_OBSERVATORY_HASL.getKey() + EQUALS_SPACED + metadataHASL.getValue());
                vecHeader.add(EMPTY_STRING);
                }
            else
                {
                LOGGER.error(SOURCE + "Invalid Metadata for Ephemeris header");
                LOGGER.errors(SOURCE, errors);
                }
            }
//        else
//            {
//            LOGGER.error(SOURCE + "Unable to add Location Metadata to Ephemeris header");
//            }

        //-----------------------------------------------------------------------------------------
        // Footer

        // Reference URL from Ephemeris
        if ((getEphemeris() != null)
            && (getEphemeris().getURL() != null))
            {
            vecHeader.add("See: "  + getEphemeris().getURL());
            vecHeader.add(EMPTY_STRING);
            }

        vecHeader.add(FOOTER_WEBSITE);
        vecHeader.add(EMPTY_STRING);
        vecHeader.add(FOOTER_EMAIL);
        vecHeader.add(EMPTY_STRING);
        vecHeader.add(MSG_EPHEMERIS_CREATED + SPACE + getObservatoryClock().getDateTimeNowAsString());

        //-----------------------------------------------------------------------------------------
        // Start another column in the Report Header
        // Be aware of ReportTablePlugin.HEADER_ROWS_PER_COLUMN!
        // ToDo Consider how we might have a 'newline' indicator?

        vecHeader.add(EMPTY_STRING);
        vecHeader.add(EMPTY_STRING);
        vecHeader.add(EMPTY_STRING);
        vecHeader.add(EMPTY_STRING);
        vecHeader.add(EMPTY_STRING);

        //-----------------------------------------------------------------------------------------
        // Ephemeris Context

        metadataTarget = MetadataHelper.getMetadataByKey(getMetadata(),
                                                         MetadataDictionary.KEY_INSTRUMENT_ROOT.getKey()
                                                            + EphemeridesHelper.KEY_EPHEMERIS_TARGET);
        metadataDateStart = MetadataHelper.getMetadataByKey(getMetadata(),
                                                            MetadataDictionary.KEY_INSTRUMENT_ROOT.getKey()
                                                                + EphemeridesHelper.KEY_EPHEMERIS_DATE_START);
        metadataTimeStart = MetadataHelper.getMetadataByKey(getMetadata(),
                                                            MetadataDictionary.KEY_INSTRUMENT_ROOT.getKey()
                                                                + EphemeridesHelper.KEY_EPHEMERIS_TIME_START);
        metadataInterval = MetadataHelper.getMetadataByKey(getMetadata(),
                                                           MetadataDictionary.KEY_INSTRUMENT_ROOT.getKey()
                                                            + EphemeridesHelper.KEY_EPHEMERIS_INTERVAL);
        metadataEpoch = MetadataHelper.getMetadataByKey(getMetadata(),
                                                        MetadataDictionary.KEY_INSTRUMENT_ROOT.getKey()
                                                         + EphemeridesHelper.KEY_EPHEMERIS_EPOCH);
        if (metadataTarget != null)
            {
            vecHeader.add(MetadataDictionary.KEY_INSTRUMENT_ROOT.getKey()
                            + EphemeridesHelper.KEY_EPHEMERIS_TARGET + EQUALS_SPACED + metadataTarget.getValue());
            }
//        else
//            {
//            LOGGER.error(SOURCE + "Invalid Ephemeris Target");
//            }

        if ((metadataDateStart != null)
            && (metadataTimeStart != null)
            && (metadataInterval != null)
            && (metadataEpoch != null))
            {
            vecHeader.add(MetadataDictionary.KEY_INSTRUMENT_ROOT.getKey()
                            + EphemeridesHelper.KEY_EPHEMERIS_DATE_START + EQUALS_SPACED + metadataDateStart.getValue());
            vecHeader.add(MetadataDictionary.KEY_INSTRUMENT_ROOT.getKey()
                            + EphemeridesHelper.KEY_EPHEMERIS_TIME_START + EQUALS_SPACED + metadataTimeStart.getValue());
            vecHeader.add(MetadataDictionary.KEY_INSTRUMENT_ROOT.getKey()
                            + EphemeridesHelper.KEY_EPHEMERIS_INTERVAL + EQUALS_SPACED + metadataInterval.getValue());
            vecHeader.add(MetadataDictionary.KEY_INSTRUMENT_ROOT.getKey()
                            + EphemeridesHelper.KEY_EPHEMERIS_EPOCH + EQUALS_SPACED + metadataEpoch.getValue());
            vecHeader.add(EMPTY_STRING);
            }
//        else
//            {
//            LOGGER.error(SOURCE + "Invalid Metadata for Ephemeris Context");
//            LOGGER.errors(SOURCE, errors);
//            }

        return (vecHeader);
        }


    /***********************************************************************************************
     * Define the report columns.
     *
     * @return Vector
     */

    public Vector<ReportColumnMetadata> defineColumns()
        {
        final String SOURCE = "EphemerisUIComponent.defineColumns() ";
        final Vector<ReportColumnMetadata> vecColumns;

        vecColumns = new Vector<ReportColumnMetadata>(COLUMN_COUNT);

        vecColumns.add(new ReportColumnMetadata(TITLE_EPHEMERIS_DATE,
                                                SchemaDataType.STRING,
                                                SchemaUnits.DIMENSIONLESS,
                                                EMPTY_STRING,
                                                SwingConstants.LEFT));
        vecColumns.add(new ReportColumnMetadata(TITLE_UT,
                                                SchemaDataType.STRING,
                                                SchemaUnits.DIMENSIONLESS,
                                                EMPTY_STRING,
                                                SwingConstants.LEFT));

        vecColumns.add(new ReportColumnMetadata(TITLE_JD,
                                                SchemaDataType.STRING,
                                                SchemaUnits.DIMENSIONLESS,
                                                EMPTY_STRING,
                                                SwingConstants.LEFT));
        vecColumns.add(new ReportColumnMetadata(TITLE_LAST,
                                                SchemaDataType.STRING,
                                                SchemaUnits.DIMENSIONLESS,
                                                EMPTY_STRING,
                                                SwingConstants.LEFT ));

        vecColumns.add(new ReportColumnMetadata(TITLE_AZIMUTH,
                                                SchemaDataType.STRING,
                                                SchemaUnits.DIMENSIONLESS,
                                                EMPTY_STRING,
                                                SwingConstants.LEFT ));
        vecColumns.add(new ReportColumnMetadata(TITLE_ELEVATION_TRUE,
                                                SchemaDataType.STRING,
                                                SchemaUnits.DIMENSIONLESS,
                                                EMPTY_STRING,
                                                SwingConstants.LEFT ));
        vecColumns.add(new ReportColumnMetadata(TITLE_ELEVATION_APPARENT,
                                                SchemaDataType.STRING,
                                                SchemaUnits.DIMENSIONLESS,
                                                EMPTY_STRING,
                                                SwingConstants.LEFT ));

        vecColumns.add(new ReportColumnMetadata(TITLE_RA,
                                                SchemaDataType.STRING,
                                                SchemaUnits.DIMENSIONLESS,
                                                EMPTY_STRING,
                                                SwingConstants.LEFT ));
        vecColumns.add(new ReportColumnMetadata(TITLE_DEC,
                                                SchemaDataType.STRING,
                                                SchemaUnits.DIMENSIONLESS,
                                                EMPTY_STRING,
                                                SwingConstants.LEFT ));

        vecColumns.add(new ReportColumnMetadata(TITLE_GALACTIC_LONGITUDE,
                                                SchemaDataType.STRING,
                                                SchemaUnits.DIMENSIONLESS,
                                                EMPTY_STRING,
                                                SwingConstants.LEFT ));
        vecColumns.add(new ReportColumnMetadata(TITLE_GALACTIC_LATITUDE,
                                                SchemaDataType.STRING,
                                                SchemaUnits.DIMENSIONLESS,
                                                EMPTY_STRING,
                                                SwingConstants.LEFT ));
        return (vecColumns);
        }


    /***********************************************************************************************
     * Define the widths of each column in terms of the objects which they will contain.
     *
     * @return Object []
     */

    public final Object [] defineColumnWidths()
        {
        final String SOURCE = "EphemerisUIComponent.defineColumnWidths() ";

        return (EphemeridesHelper.defineEphemerisColumnWidths());
        }


    /***********************************************************************************************
     * Refresh the EphemerisUIComponent data table.
     * This is called by ReportTable refreshTable().
     *
     * @return Vector
     */

    public final Vector<Vector> refreshReport()
        {
        final String SOURCE = "EphemerisUIComponent.refreshReport() ";

        return (generateReport());
        }


    /***********************************************************************************************
     * Generate the EphemerisUIComponent table.
     *
     * @return Vector of report rows
     */

    public final Vector<Vector> generateReport()
        {
        final String SOURCE = "EphemerisUIComponent.generateReport() ";
        final Vector<Vector> vecReport;

        // Only generate a Report if this UIComponent is visible
        // There is no need to auto-truncate
        if (UIComponentHelper.shouldRefresh(false, getHostInstrument(), this))
            {
            vecReport = generateRawReport();
            }
        else
            {
            LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                   SOURCE + "NOT VISIBLE");

            vecReport = new Vector<Vector>(1);
            }

        return (vecReport);
        }


    /***********************************************************************************************
     * Generate the raw Report, i.e not truncated,
     * and regardless of whether the component is visible.
     * This is used for e.g. exports.
     *
     * @return Vector<Vector>
     *
     * @throws ReportException
     */

    public Vector<Vector> generateRawReport() throws ReportException
        {
        final String SOURCE = "EphemerisUIComponent.generateRawReport() ";

        return (EphemeridesHelper.convertEphemerisDataToReport(getEphemerisData(), COLUMN_COUNT));
        }


    /***********************************************************************************************
     * Set the data from the DAO finished() method, or from any Command doing a realtime update.
     * The Ephemeris data appear in the UserObject.
     *
     * @param daowrapper
     * @param updatemetadata
     */

    public void setWrappedData(final DAOWrapperInterface daowrapper,
                               final boolean updatemetadata)
        {
        final String SOURCE = "EphemerisUIComponent.setWrappedData() ";

        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isMetadataDebug(),
                               SOURCE + "--> MetadataHelper.collectAggregateMetadataTraced()");

        if (daowrapper != null)
            {
            final List<Metadata> listAggregateMetadata;
            final Metadata metadataTarget;

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
            //
            // EphemerisFrameUIComponent execution has produced the following Metadata:
            //      Instrument.Ephemerides.Ephemeris.Target

            listAggregateMetadata = MetadataHelper.collectAggregateMetadataTraced(REGISTRY.getFramework(),
                                                                                  (ObservatoryInterface) getHostInstrument().getHostAtom(),
                                                                                  getHostInstrument(),
                                                                                  getHostInstrument().getDAO(), daowrapper,
                                                                                  SOURCE,
                                                                                  LOADER_PROPERTIES.isMetadataDebug());
            setMetadata(listAggregateMetadata);

            // The Ephemeris details are needed for the Report header
            metadataTarget = MetadataHelper.getMetadataByKey(getMetadata(),
                                                               MetadataDictionary.KEY_INSTRUMENT_ROOT.getKey()
                                                                    + EphemeridesHelper.KEY_EPHEMERIS_TARGET);
            if ((metadataTarget != null)
                && (daowrapper.getUserObject() instanceof Vector))
                {
                LOGGER.debugTimedEvent(LOADER_PROPERTIES.isMetadataDebug(),
                                       SOURCE + MetadataDictionary.KEY_INSTRUMENT_ROOT.getKey()
                                            + EphemeridesHelper.KEY_EPHEMERIS_TARGET
                                            + metadataTarget.getValue());

                setEphemerisFromName(metadataTarget.getValue());
                setEphemerisData((Vector)daowrapper.getUserObject());

                refreshTable();

                // We don't want the wrapper data any more, so help the gc?
                daowrapper.setUserObject(null);
                }
            else
                {
                setMetadata(null);
                setEphemerisFromName(null);
                setEphemerisData(null);

                refreshTable();
                }
            }
        else
            {
            setMetadata(null);
            setEphemerisFromName(null);
            setEphemerisData(null);

            refreshTable();
            }
        }


    /**********************************************************************************************/
    /* Utilities                                                                                  */
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

    private Ephemeris getEphemeris()
        {
        return (this.ephemeris);
        }


    /***********************************************************************************************
     * Get the Aggregate Metadata.
     *
     * @return List<Metadata>
     */

    private List<Metadata> getMetadata()
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
        final String SOURCE = "EphemerisUIComponent.setMetadata() ";

        this.listMetadata = metadata;

        MetadataHelper.showMetadataList(getMetadata(),
                                        SOURCE,
                                        LOADER_PROPERTIES.isMetadataDebug());
        }


    /***********************************************************************************************
     * Get the Vector of Ephemeris data.
     *
     * @return Vector<Object>
     */

    private Vector<Object> getEphemerisData()
        {
        return (this.vecEphemerisData);
        }


    /***********************************************************************************************
     * Set the Vector of Ephemeris data.
     *
     * @param data
     */

    private void setEphemerisData(final Vector<Object> data)
        {
        this.vecEphemerisData = data;
        }


    /**********************************************************************************************/
    /* Injections                                                                                 */
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
