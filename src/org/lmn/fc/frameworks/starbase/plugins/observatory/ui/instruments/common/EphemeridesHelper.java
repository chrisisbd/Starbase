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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common;


import org.lmn.fc.common.constants.*;
import org.lmn.fc.common.utilities.astronomy.AtmosphericRefraction;
import org.lmn.fc.common.utilities.astronomy.CoordinateConversions;
import org.lmn.fc.common.utilities.astronomy.CoordinateType;
import org.lmn.fc.common.utilities.time.AstronomicalCalendar;
import org.lmn.fc.common.utilities.time.AstronomicalCalendarInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.MetadataDictionary;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ObservatoryInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.ephemerides.AbstractEphemerisDAO;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.ephemerides.EphemerisDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.utilities.Epoch;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.utilities.Precess;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.model.datatypes.*;
import org.lmn.fc.model.datatypes.types.HourMinSecDataType;
import org.lmn.fc.model.datatypes.types.RightAscensionDataType;
import org.lmn.fc.model.datatypes.types.YearMonthDayDataType;
import org.lmn.fc.model.plugins.FrameworkPlugin;
import org.lmn.fc.model.xmlbeans.ephemerides.Ephemeris;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;
import org.lmn.fc.model.xmlbeans.metadata.SchemaUnits;
import org.lmn.fc.ui.reports.ReportTableHelper;

import java.awt.geom.Point2D;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;


/***************************************************************************************************
 * EphemeridesHelper.
 */

public final class EphemeridesHelper implements FrameworkConstants,
                                                FrameworkStrings,
                                                FrameworkMetadata,
                                                FrameworkSingletons
    {
    // String Resources
    // The prefix 'Ephemerides' is needed because it is the name of the Command Module
    public static final String KEY_EPHEMERIS_TARGET         = "Ephemerides.Ephemeris.Target";
    public static final String KEY_EPHEMERIS_DATE_START     = "Ephemerides.Ephemeris.Date.Start";
    public static final String KEY_EPHEMERIS_TIME_START     = "Ephemerides.Ephemeris.Time.Start";
    public static final String KEY_EPHEMERIS_DATE_END       = "Ephemerides.Ephemeris.Date.End";
    public static final String KEY_EPHEMERIS_TIME_END       = "Ephemerides.Ephemeris.Time.End";
    public static final String KEY_EPHEMERIS_INTERVAL       = "Ephemerides.Ephemeris.Time.Interval";
    public static final String KEY_EPHEMERIS_EPOCH          = "Ephemerides.Ephemeris.Epoch";

    private static final String DAO_NOT_INSTANTIATED        = "Unable to instantiate the Ephemeris DAO ";

    private static final String PATTERN_DATE                = "2010-00-00";
    private static final String PATTERN_TIME                = "00:00:00";

    private static final int EPHEMERIS_DAO_CHANNEL_COUNT = 3;


    /***********************************************************************************************
     * Instantiate an EphemerisDAO.
     *
     * @param ephemeris
     *
     * @return EphemerisDAOInterface
     */

    public static EphemerisDAOInterface instantiateEphemerisDAO(final Ephemeris ephemeris)
        {
        EphemerisDAOInterface daoInterface;

        daoInterface = null;

        try
            {
            final Class classObject;
            final Class[] interfaces;
            final String strInterface;
            final boolean boolLoaded;

            classObject = Class.forName(ephemeris.getDaoClassname());

            // Does the target implement the EphemerisDAOInterface?
            interfaces = classObject.getInterfaces();
            strInterface = EphemerisDAOInterface.class.getName();
            boolLoaded = false;

            if ((interfaces != null)
                && (interfaces.length > 0))
                {
                if (!classObject.isInterface())
                    {
                    // Try to find the mandatory interface
                    for (int i = 0;
                         ((i < interfaces.length) && (!boolLoaded));
                         i++)
                        {
                        if (strInterface.equals(interfaces[i].getName()))
                            {
                            // We have found the correct interface
                            LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                                   "Ephemeris: [" + ephemeris + " implements " + strInterface + "]");

                            // Prove that the real Ephemeris is a subclass of AbstractEphemerisDAO
                            final Class superClass = classObject.getSuperclass();

                            if (superClass != null)
                                {
                                if (AbstractEphemerisDAO.class.getName().equals(superClass.getName()))
                                    {
                                    final Constructor constructor;
                                    final Class[] parameterTypes =
                                        {
                                        Ephemeris.class
                                        };

                                    // Now get hold of the Constructor
                                    constructor = classObject.getDeclaredConstructor(parameterTypes);

                                    if (constructor != null)
                                        {
                                        final Object objArguments[];

                                        objArguments = new Object[1];
                                        objArguments[0] = ephemeris;

                                        daoInterface = (EphemerisDAOInterface)constructor.newInstance(objArguments);
                                        }
                                    else
                                        {
                                        LOGGER.error(DAO_NOT_INSTANTIATED + "Constructor not found");
                                        }
                                    }
                                else
                                    {
                                    LOGGER.error(DAO_NOT_INSTANTIATED + "Class is not a subclass of " + AbstractEphemerisDAO.class.getName());
                                    }
                                }
                            else
                                {
                                LOGGER.error(DAO_NOT_INSTANTIATED + "Class has no superclass");
                                }
                            }
                        else
                            {
                            LOGGER.error(DAO_NOT_INSTANTIATED + "Incorrect interface " + interfaces[i].getName());
                            }
                        }
                    }
                else
                    {
                    LOGGER.error(DAO_NOT_INSTANTIATED + "Class is an interface only");
                    }
                }
            else
                {
                LOGGER.error(DAO_NOT_INSTANTIATED + "No interfaces found");
                }
            }

        catch(NoSuchMethodException exception)
            {
            LOGGER.error(DAO_NOT_INSTANTIATED + "NoSuchMethodException [classname=" + ephemeris + "]");
            }

        catch(SecurityException exception)
            {
            LOGGER.error(DAO_NOT_INSTANTIATED + "SecurityException [classname=" + ephemeris + "]");
            }

        catch (InstantiationException exception)
            {
            LOGGER.error(DAO_NOT_INSTANTIATED + "InstantiationException [classname=" + ephemeris + "]");
            }

        catch (IllegalAccessException exception)
            {
            LOGGER.error(DAO_NOT_INSTANTIATED + "IllegalAccessException [classname=" + ephemeris + "]");
            }

        catch (IllegalArgumentException exception)
            {
            LOGGER.error(DAO_NOT_INSTANTIATED + "IllegalArgumentException [classname=" + ephemeris + "]");
            }

        catch (InvocationTargetException exception)
            {
            LOGGER.error(DAO_NOT_INSTANTIATED + "InvocationTargetException [classname=" + ephemeris + "]");
            }

        catch (ClassNotFoundException exception)
            {
            // Suppress empty classnames, because these are probably intentional
            if ((ephemeris != null)
                && (!FrameworkStrings.EMPTY_STRING.equals(ephemeris.getDaoClassname())))
                {
                LOGGER.error(DAO_NOT_INSTANTIATED + "ClassNotFoundException [classname=" + ephemeris + "]");
                }
            }

        return (daoInterface);
        }


    /***********************************************************************************************
     * Set the Ephemeris for this UIComponent, given its name.
     * Return NULL if the Ephemeris cannot be found.
     *
     * @param instrument
     * @param ephemerisname
     *
     * @return Ephemeris
     */

    public static Ephemeris getEphemerisForName(final ObservatoryInstrumentInterface instrument,
                                                final String ephemerisname)
        {
        Ephemeris ephemeris;

        ephemeris = null;

        if ((instrument != null)
            && (instrument.getHostAtom() != null)
            && (instrument.getHostAtom() instanceof ObservatoryInterface)
            && (ephemerisname != null))
            {
            final ObservatoryInterface observatory;
            final Hashtable<String, EphemerisDAOInterface> tableDAOs;

            observatory = (ObservatoryInterface)instrument.getHostAtom();
            tableDAOs = observatory.getEphemerisDaoTable();

            if ((tableDAOs != null)
                && (tableDAOs.containsKey(ephemerisname))
                && (tableDAOs.get(ephemerisname) != null))
                {
                ephemeris = tableDAOs.get(ephemerisname).getEphemeris();
                }
            }

        return (ephemeris);
        }


    /***********************************************************************************************
     * Get the EphemerisDAO for the specified Name.
     * Return NULL if not found.
     *
     * @param instrument
     * @param ephemerisname
     *
     * @return EphemerisDAOInterface
     */

    public static EphemerisDAOInterface getEphemerisDAOforName(final ObservatoryInstrumentInterface instrument,
                                                               final String ephemerisname)
        {
        EphemerisDAOInterface daoEphemeris;

        daoEphemeris = null;

        if ((instrument != null)
            && (instrument.getHostAtom() != null)
            && (instrument.getHostAtom() instanceof ObservatoryInterface)
            && (ephemerisname != null))
            {
            final ObservatoryInterface observatory;
            final Hashtable<String, EphemerisDAOInterface> tableDAOs;

            observatory = (ObservatoryInterface)instrument.getHostAtom();
            tableDAOs = observatory.getEphemerisDaoTable();

            if ((tableDAOs != null)
                && (tableDAOs.containsKey(ephemerisname)))
                {
                daoEphemeris = tableDAOs.get(ephemerisname);
                }
            }

        return (daoEphemeris);
        }


    /***********************************************************************************************
     * Generate an Ephemeris as a Vector, given the place and initial conditions.
     * jend must be after jstart.
     * Note that calculation of the Apparent Elevation is left until the data are actually required.
     * The caller should ensure that any required Metadata are present in the DAO.
     *
     * @param dao
     * @param jdstart
     * @param jdend
     * @param intervalseconds
     * @param epochfinal
     * @param longitude
     * @param latitude
     * @param timezone
     *
     * @return Vector<Object>
     */

    public static Vector<Object> generateEphemerisData(final EphemerisDAOInterface dao,
                                                       final double jdstart,
                                                       final double jdend,
                                                       final int intervalseconds,
                                                       final Epoch epochfinal,
                                                       final double longitude,
                                                       final double latitude,
                                                       final TimeZone timezone)
        {
        final String SOURCE = "EphemeridesHelper.generateEphemerisData() ";
        final Vector<Object> vecEphemeris;
        final double dblDayFraction;
        final int intJDEndIndex;
        final Epoch epochOriginal;

        vecEphemeris = new Vector<Object>((int)(AstronomyConstants.SECONDS_PER_DAY / intervalseconds));

        dblDayFraction = (double)intervalseconds / AstronomyConstants.SECONDS_PER_DAY;

        intJDEndIndex =  (int)Math.rint((jdend - jdstart) / dblDayFraction) ;

        if ((dao.getEphemeris() != null)
            && (dao.getEphemeris().getEpoch() != null))
            {
            epochOriginal = Epoch.getEpochForName(dao.getEphemeris().getEpoch().toString());
            }
        else
            {
            epochOriginal = Epoch.J2000;
            LOGGER.error(SOURCE + "Unable to identify original Epoch in Ephemeris, using [epoch=" + Epoch.J2000.getName() + "]");
            }

        for (int intJDIndex = 0;
             intJDIndex < intJDEndIndex;
             intJDIndex++)
            {
            final Vector<Object> vecRow;
            final AstronomicalCalendarInterface calendarRow;
            final CoordinateType coordinateType;
            final Point2D.Double pointAzEl;
            final Point2D.Double pointRaDec;
            final Point2D.Double pointGalacticlb;
            final double dblJD;
            final double dblLAST;

            dblJD = jdstart + (intJDIndex * dblDayFraction);

            // Find out all we can about the time on this row
            // We need a new calendar for each row...
            calendarRow = new AstronomicalCalendar(dblJD,
                                                   timezone,
                                                   longitude);
            dblLAST = calendarRow.getLAST();

            // Recalculate the coordinates for this JD with the DAO applicable to this Ephemeris
            // This uses the original Epoch
            coordinateType = dao.recalculateForJulianDate(dblJD,
                                                          dblLAST,
                                                          latitude);

            // ToDo getHostStarMap().getHostInstrument().getDAO().getEventLogFragment().addAll(getEphemerisDAO().getEventLogFragment());

            switch (coordinateType)
                {
                case UNASSIGNED:
                    {
                    // No Precession required
                    pointAzEl = new Point2D.Double(0.0, 0.0);
                    pointRaDec = new Point2D.Double(0.0, 0.0);
                    pointGalacticlb = new Point2D.Double(0.0, 0.0);

                    LOGGER.warn(SOURCE + "CoordinateType UNASSIGNED in use");
                    break;
                    }

                case AZEL:
                case AZEL_METADATA:
                    {
                    final Point2D.Double pointRaDecOriginal;

                    pointAzEl = dao.getCoordinates();

                    // (Ra, Dec) RA in HOURS, Dec in DEGREES
                    pointRaDecOriginal = CoordinateConversions.convertAzElToRaDec(pointAzEl,
                                                                                  EphemerisDAOInterface.ORIGIN_IS_NORTH,
                                                                                  dblLAST,
                                                                                  latitude);
                    if (epochOriginal.getJD() != epochfinal.getJD())
                        {
                        // Everything is derived from (RA, Dec), so just precess that
                        pointRaDec = Precess.precessEquatorial(pointRaDecOriginal,
                                                               epochOriginal,
                                                               epochfinal);
                        }
                    else
                        {
                        pointRaDec = pointRaDecOriginal;
                        }

                    pointGalacticlb = CoordinateConversions.convertRaDecToGalactic(pointRaDec);
                    break;
                    }

                case RADEC:
                case RADEC_METADATA:
                    {
                    final Point2D.Double pointRaDecOriginal;

                    pointRaDecOriginal = dao.getCoordinates();

                    if (epochOriginal.getJD() != epochfinal.getJD())
                        {
                        // Everything is derived from (RA, Dec), so just precess that
                        pointRaDec = Precess.precessEquatorial(pointRaDecOriginal,
                                                               epochOriginal,
                                                               epochfinal);
                        }
                    else
                        {
                        pointRaDec = pointRaDecOriginal;
                        }

                    pointAzEl = CoordinateConversions.convertRaDecToAzEl(pointRaDec,
                                                                         EphemerisDAOInterface.ORIGIN_IS_NORTH,
                                                                         dblLAST,
                                                                         latitude);

                    pointGalacticlb = CoordinateConversions.convertRaDecToGalactic(pointRaDec);
                    break;
                    }

                case GALACTIC:
                case GALACTIC_METADATA:
                    {
                    final Point2D.Double pointGalacticOriginal;
                    final Point2D.Double pointRaDecOriginal;

                    pointGalacticOriginal = dao.getCoordinates();

                    // We need (RA, Dec) in case we need to precess
                    pointRaDecOriginal = CoordinateConversions.convertGalacticToRaDec(pointGalacticOriginal);

                    if (epochOriginal.getJD() != epochfinal.getJD())
                        {
                        pointRaDec = Precess.precessEquatorial(pointRaDecOriginal,
                                                               epochOriginal,
                                                               epochfinal);
                        }
                    else
                        {
                        pointRaDec = pointRaDecOriginal;
                        }

                    // I hope this is the right thing to do...
                    // Transform the (possibly) precessed (RA, Dec) back to Galactic
                    pointGalacticlb = CoordinateConversions.convertRaDecToGalactic(pointRaDec);

                    pointAzEl = CoordinateConversions.convertRaDecToAzEl(pointRaDec,
                                                                         EphemerisDAOInterface.ORIGIN_IS_NORTH,
                                                                         dblLAST,
                                                                         latitude);
                    break;
                    }

                default:
                    {
                    // No Precession required
                    pointAzEl = new Point2D.Double(0.0, 0.0);
                    pointRaDec = new Point2D.Double(0.0, 0.0);
                    pointGalacticlb = new Point2D.Double(0.0, 0.0);

                    LOGGER.error(SOURCE + "Unrecognised CoordinateType [type=" + coordinateType.getName() + "]");
                    }
                }

            // This data Vector must hold the underlying objects, not Strings
            // The row format is: <Calendar> <AzEl> <RaDec> <GalLongGalLat>
            vecRow = new Vector<Object>(EPHEMERIS_DAO_CHANNEL_COUNT + 1);

            // Dates and Times are all in the calendar
            vecRow.add(calendarRow);

            // Azimuth and Elevation are in DEGREES
            // Note that calculation of the Apparent Elevation is left until the data are actually required
            vecRow.add(pointAzEl);

            // Right Ascension in HOURS
            // Declination in DEGREES
            vecRow.add(pointRaDec);

            // Galactic Longitude and Latitude are in DEGREES
            vecRow.add(pointGalacticlb);

            // Prepare the data which will be passed back
            vecEphemeris.add(vecRow);
            }

        return (vecEphemeris);
        }


    /***********************************************************************************************
     * Define the widths of each column in terms of the objects which they will contain.
     *
     * @return Object []
     */

    public static Object [] defineEphemerisColumnWidths()
        {
        final Object [] columnWidths =
            {
            PATTERN_DATE,       // Date
            PATTERN_TIME,       // UT

            DecimalFormatPattern.JD.getFormatter().toPattern(),  // Julian Date
            PATTERN_TIME,       // LAST

            DecimalFormatPattern.AZIMUTH.getFormatter().toPattern(),
            DecimalFormatPattern.ELEVATION.getFormatter().toPattern(),  // True Elevation
            DecimalFormatPattern.ELEVATION.getFormatter().toPattern(),  // Apparent Elevation
            PATTERN_TIME,
            DecimalFormatPattern.SECONDS_DECLINATION.getFormatter().toPattern(),
            DecimalFormatPattern.LONGITUDE_GALACTIC.getFormatter().toPattern(),
            DecimalFormatPattern.LATITUDE_GALACTIC.getFormatter().toPattern()
            };

        return (columnWidths);
        }


    /***********************************************************************************************
     * Convert the specified data Vector to a Report format.
     * The data format is: {Calendar} <AzEl> <RaDec> <GalLongGalLat>.
     * The Report format is: {Date} {Time} {JulianDate} {LAST}
     * {Azimuth} {Elevation} {RightAscension} {Declination} {GalacticLongitude} {GalacticLatitude}.
     *
     * @param data
     * @param columncount
     *
     * @return Vector<Vector>
     */

    public static Vector<Vector> convertEphemerisDataToReport(final Vector<Object> data,
                                                              final int columncount)
        {
        final String SOURCE = "EphemeridesHelper.convertEphemerisDataToReport() ";
        Vector<Vector> vecReport;

        vecReport = new Vector<Vector>(1);

        if (data != null)
            {
            boolean boolCurrentElevationSign;

            // Resize the Vector to suit the data
            vecReport = new Vector<Vector>(data.size());

            boolCurrentElevationSign = false;

            // The incoming data must be converted to a form suitable for a Report
            for (int intRowIndex = 0;
                 intRowIndex < data.size();
                 intRowIndex++)
                {
                if ((data.get(intRowIndex) !=  null)
                    && (data.get(intRowIndex) instanceof Vector))
                    {
                    final Vector vecRow;

                    vecRow = (Vector)data.get(intRowIndex);

                    if ((vecRow != null)
                        && (vecRow.size() == (EPHEMERIS_DAO_CHANNEL_COUNT + 1)))
                        {
                        final int INDEX_CALENDAR = 0;
                        final int INDEX_AZ_EL = 1;
                        final int INDEX_RA_DEC = 2;
                        final int INDEX_L_B = 3;
                        final Vector<Object> vecReportRow;
                        final AstronomicalCalendarInterface calendar;
                        final YearMonthDayInterface ymdUT;
                        final HourMinSecInterface hmsUT;
                        final HourMinSecInterface hmsLAST;
                        final HourMinSecInterface hmsRA;
                        final Point2D.Double pointAzEl;
                        final Point2D.Double pointRaDec;
                        final Point2D.Double pointGalacticlb;
                        final boolean boolElevationSign;

                        // The row format is: <Calendar> <AzEl> <RaDec> <GalLongGalLat>
                        // Azimuth and Elevation are in DEGREES
                        // Right Ascension in HOURS
                        // Declination in DEGREES
                        // Galactic Longitude and Latitude are in DEGREES

                        if (vecRow.get(INDEX_CALENDAR) instanceof AstronomicalCalendarInterface)
                            {
                            calendar = (AstronomicalCalendarInterface)vecRow.get(INDEX_CALENDAR);

                            if (vecRow.get(INDEX_AZ_EL) instanceof Point2D.Double)
                                {
                                pointAzEl = (Point2D.Double)vecRow.get(INDEX_AZ_EL);

                                if (vecRow.get(INDEX_RA_DEC) instanceof Point2D.Double)
                                    {
                                    pointRaDec = (Point2D.Double)vecRow.get(INDEX_RA_DEC);

                                    if (vecRow.get(INDEX_L_B) instanceof Point2D.Double)
                                        {
                                        pointGalacticlb = (Point2D.Double)vecRow.get(INDEX_L_B);

                                        // All data are valid, so we can proceed to build the Report

                                        // Track changes in Elevation
                                        // Are we currently above the horizon?
                                        if (intRowIndex == 0.0)
                                            {
                                            boolCurrentElevationSign = (pointAzEl.getY() >= 0.0);
                                            }

                                        boolElevationSign = (pointAzEl.getY() >= 0.0);

                                        ymdUT = new YearMonthDayDataType((Calendar)calendar);
                                        ymdUT.enableFormatSign(false);

                                        hmsUT = new HourMinSecDataType(calendar.getUT());
                                        hmsUT.enableFormatSign(false);

                                        hmsLAST = new HourMinSecDataType(calendar.getLAST());
                                        hmsLAST.enableFormatSign(false);

                                        // Build the Report row
                                        vecReportRow = new Vector<Object>(columncount);

                                        // See if the Elevation has changed sign since we started
                                        if (boolElevationSign == boolCurrentElevationSign)
                                            {
                                            // Dates and Times
                                            vecReportRow.add(ymdUT.toString());
                                            vecReportRow.add(hmsUT.toString_HH_MM_SS());

                                            vecReportRow.add(DecimalFormatPattern.JD.format(calendar.getJD()));
                                            vecReportRow.add(hmsLAST.toString_HH_MM_SS());

                                            // Azimuth and Elevation are in DEGREES
                                            vecReportRow.add(DecimalFormatPattern.AZIMUTH.format(pointAzEl.getX()));
                                            vecReportRow.add(DecimalFormatPattern.ELEVATION.format(pointAzEl.getY()));

                                            // Calculate Refraction corrections on the fly
                                            vecReportRow.add(DecimalFormatPattern.ELEVATION.format(pointAzEl.getY()
                                                                                                   + AtmosphericRefraction.atmosphericRefraction(pointAzEl.getY())));
                                            // Right Ascension in HOURS
                                            hmsRA = new RightAscensionDataType(pointRaDec.getX());
                                            hmsRA.enableFormatSign(false);

                                            vecReportRow.add(hmsRA.toString_HH_MM_SS());

                                            // Declination in DEGREES
                                            vecReportRow.add(DecimalFormatPattern.SECONDS_DECLINATION.format(pointRaDec.getY()));

                                            // Galactic Longitude and Latitude are in DEGREES
                                            vecReportRow.add(DecimalFormatPattern.LONGITUDE_GALACTIC.format(pointGalacticlb.getX()));
                                            vecReportRow.add(DecimalFormatPattern.LATITUDE_GALACTIC.format(pointGalacticlb.getY()));
                                            }
                                        else
                                            {
                                            // Indicate a change of sign, just once...

                                            // Dates and Times
                                            vecReportRow.add(HTML_PREFIX_BOLD + ymdUT.toString() + HTML_SUFFIX_BOLD);
                                            vecReportRow.add(HTML_PREFIX_BOLD + hmsUT.toString_HH_MM_SS() + HTML_SUFFIX_BOLD);

                                            vecReportRow.add(HTML_PREFIX_BOLD + DecimalFormatPattern.JD.format(calendar.getJD()) + HTML_SUFFIX_BOLD);
                                            vecReportRow.add(HTML_PREFIX_BOLD + hmsLAST.toString_HH_MM_SS() + HTML_SUFFIX_BOLD);

                                            vecReportRow.add(HTML_PREFIX_BOLD + DecimalFormatPattern.AZIMUTH.format(pointAzEl.getX()) + HTML_SUFFIX_BOLD);
                                            vecReportRow.add(HTML_PREFIX_BOLD + DecimalFormatPattern.ELEVATION.format(pointAzEl.getY()) + HTML_SUFFIX_BOLD);

                                            // Calculate Refraction corrections on the fly
                                            vecReportRow.add(HTML_PREFIX_BOLD + DecimalFormatPattern.ELEVATION.format(pointAzEl.getY()
                                                                                                                        + AtmosphericRefraction.atmosphericRefraction(pointAzEl.getY()))
                                                                + HTML_SUFFIX_BOLD);
                                            // Right Ascension in HOURS
                                            hmsRA = new RightAscensionDataType(pointRaDec.getX());
                                            hmsRA.enableFormatSign(false);

                                            vecReportRow.add(HTML_PREFIX_BOLD + hmsRA.toString_HH_MM_SS() + HTML_SUFFIX_BOLD);

                                            // Declination in DEGREES
                                            vecReportRow.add(HTML_PREFIX_BOLD + DecimalFormatPattern.SECONDS_DECLINATION.format(pointRaDec.getY()) + HTML_SUFFIX_BOLD);

                                            // Galactic Longitude and Latitude are in DEGREES
                                            vecReportRow.add(HTML_PREFIX_BOLD + DecimalFormatPattern.LONGITUDE_GALACTIC.format(pointGalacticlb.getX()) + HTML_SUFFIX_BOLD);
                                            vecReportRow.add(HTML_PREFIX_BOLD + DecimalFormatPattern.LATITUDE_GALACTIC.format(pointGalacticlb.getY()) + HTML_SUFFIX_BOLD);

                                            // ... and look for the next change
                                            boolCurrentElevationSign = boolElevationSign;
                                            }

                                        vecReport.add(vecReportRow);

                                        ReportTableHelper.debugVector(vecReportRow,
                                                                      LOADER_PROPERTIES.isMetadataDebug());
                                        }
                                    else
                                        {
                                        LOGGER.error(SOURCE + "Not a valid point GalacticLong, GalacticLat");
                                        }
                                    }
                                else
                                    {
                                    LOGGER.error(SOURCE + "Not a valid point Ra, Dec");
                                    }
                                }
                            else
                                {
                                LOGGER.error(SOURCE + "Not a valid point Az, El");
                                }
                            }
                        else
                            {
                            LOGGER.error(SOURCE + "Not a valid AstronomicalCalendar");
                            }
                        }
                    else
                        {
                        LOGGER.error(SOURCE + "Invalid Report row");
                        }
                    }
                else
                    {
                    LOGGER.error(SOURCE + "Invalid Report Vector");
                    }
                }
            }

        return (vecReport);
        }


    /***********************************************************************************************
     * Indicate if the UserObject data are a representation of an Ephemeris.
     * A bit of a bodge, but it is progress...
     *
     * @param userobject
     *
     * @return boolean
     */

    public static boolean isUserObjectAnEphemeris(final Object userobject)
        {
        final int INDEX_CALENDAR = 0;
        final int INDEX_AZ_EL = 1;
        final int INDEX_RA_DEC = 2;
        final int INDEX_L_B = 3;
        final boolean boolIsEphemeris;

        // If this is all true, there's a good chance the data are an Ephemeris
        boolIsEphemeris = ((userobject != null)
                            && (userobject instanceof Vector)
                            && (((Vector)userobject).size() > 0)
                            && (((Vector)userobject).get(0) !=  null)
                            && (((Vector)userobject).get(0) instanceof Vector)
                            && (((Vector)((Vector)userobject).get(0)).size() == 4)
                            && (((Vector)((Vector)userobject).get(0)).get(INDEX_CALENDAR) instanceof AstronomicalCalendarInterface)
                            && (((Vector)((Vector)userobject).get(0)).get(INDEX_AZ_EL) instanceof Point2D.Double)
                            && (((Vector)((Vector)userobject).get(0)).get(INDEX_RA_DEC) instanceof Point2D.Double)
                            && (((Vector)((Vector)userobject).get(0)).get(INDEX_L_B) instanceof Point2D.Double));

        return (boolIsEphemeris);
        }


    /***********************************************************************************************
     * Get the current Calendar for the Observatory,
     * or the Framework if the Observatory Metadata are not available.
     *
     * @param framework
     * @param hostinstrument
     *
     * @return AstronomicalCalendarInterface
     */

    public static AstronomicalCalendarInterface getCalendarNow(final FrameworkPlugin framework,
                                                               final ObservatoryInstrumentInterface hostinstrument,
                                                               final boolean debug)
        {
        final String SOURCE = "EphemeridesHelper.getCalendarNow() ";
        final List<Metadata> listMetadata;

        // Now try to find the Observatory topocentric information
        listMetadata = MetadataHelper.collectAggregateMetadataTraced(framework,
                                                                     (ObservatoryInterface) hostinstrument.getHostAtom(),
                                                                     hostinstrument,
                                                                     hostinstrument.getDAO(), null,
                                                                     SOURCE,
                                                                     debug);

        return (getCalendarNow(framework, hostinstrument, listMetadata));
        }


        /***********************************************************************************************
        * Get the current Calendar for the Observatory,
        * or the Framework if the Observatory Metadata are not available.
        *
        * @param framework
        * @param hostinstrument
        * @param metadatalist
        *
        * @return AstronomicalCalendarInterface
        */

    public static AstronomicalCalendarInterface getCalendarNow(final FrameworkPlugin framework,
                                                               final ObservatoryInstrumentInterface hostinstrument,
                                                               final List<Metadata> metadatalist)
        {
        final String SOURCE = "EphemeridesHelper.getCalendarNow() ";
        final AstronomicalCalendarInterface calendarNow;
        final TimeZone timeZone;
        final Locale locale;
        final double dblLongitude;
        final Metadata metadataLongitude;
        final Metadata metadataTimeZone;
        final List<String> warnings;

        warnings = new ArrayList<String>(10);

        // Firstly try to find the Observatory topocentric information in the supplied Metadata
        metadataLongitude = MetadataHelper.getMetadataByKey(metadatalist,
                                                            MetadataDictionary.KEY_OBSERVATORY_LONGITUDE.getKey());

        metadataTimeZone = MetadataHelper.getMetadataByKey(metadatalist,
                                                           MetadataDictionary.KEY_OBSERVATORY_TIMEZONE.getKey());

        // Have we got a complete valid Observatory location?
        if ((metadataLongitude != null)
            && (metadataTimeZone != null))
            {
            final DegMinSecInterface dmsLongitudeObservatory;

            // Longitude -179:59:59.9999 to +000:00:00.0000 to +179:59:59.9999  deg:mm:ss
            dmsLongitudeObservatory = (DegMinSecInterface) DataTypeHelper.parseDataTypeFromValueField(metadataLongitude.getValue(),
                                                                                                      DataTypeDictionary.SIGNED_LONGITUDE,
                                                                                                      EMPTY_STRING,
                                                                                                      EMPTY_STRING,
                                                                                                      warnings);
            if ((dmsLongitudeObservatory != null)
                && (warnings.size() == 0))
                {
                // These must be correct to have passed the Metadata parsing above
                dblLongitude = dmsLongitudeObservatory.toDouble();

                // This returns the GMT zone if the given ID cannot be understood
                timeZone = TimeZone.getTimeZone(metadataTimeZone.getValue());

                // Use the Framework Locale for the Observatory
                locale = new Locale(framework.getLanguageISOCode(),
                                    framework.getCountryISOCode());
                }
            else
                {
                // We must use the Framework location
                dblLongitude = framework.getLongitude().toDouble();

                timeZone = TimeZone.getTimeZone(framework.getTimeZoneCode());
                locale = new Locale(framework.getLanguageISOCode(),
                                    framework.getCountryISOCode());

                warnings.add("Using Framework location because Observation location missing or invalid");
                }
            }
        else
            {
            // We must use the Framework location
            dblLongitude = framework.getLongitude().toDouble();

            timeZone = TimeZone.getTimeZone(framework.getTimeZoneCode());
            locale = new Locale(framework.getLanguageISOCode(),
                                framework.getCountryISOCode());

            warnings.add("Using Framework location because Observation location missing or invalid");
            }

        if (hostinstrument.getObservatoryClock() != null)
            {
            // Why can't we read back the Locale?
            if ((hostinstrument.getObservatoryClock().getAstronomicalCalendar() != null)
                && (hostinstrument.getObservatoryClock().getAstronomicalCalendar().getTimeZone().hasSameRules(timeZone))
                && (Math.abs(hostinstrument.getObservatoryClock().getAstronomicalCalendar().getLongitude() - dblLongitude) < 0.1))
                {
                // Save time by not making a new Calendar
                calendarNow = hostinstrument.getObservatoryClock().getAstronomicalCalendar();

                // ...but reset using the new Longitude
                calendarNow.setLongitude(dblLongitude);
                }
            else
                {
                calendarNow = new AstronomicalCalendar(hostinstrument.getObservatoryClock().getSystemCalendar(timeZone, locale),
                                                       dblLongitude);
                warnings.add("Using new Clock calendar because location has changed");
                }
            }
        else
            {
            calendarNow = new AstronomicalCalendar(new GregorianCalendar(), dblLongitude);
            warnings.add("Using host Clock because Observation Clock not found");
            }

        // Show any Errors as just Warnings
        if (warnings.size() > 0)
            {
            LOGGER.warnings("Warning: " + SOURCE, warnings);
            }

        return (calendarNow);
        }


    /***********************************************************************************************
     * Get the Observatory Longitude, or the Framework Longitude if the Observatory is not available.
     *
     * @param framework
     * @param observatory
     * @param debug
     *
     * @return double
     */

    public static double getLongitude(final FrameworkPlugin framework,
                                      final ObservatoryInterface observatory,
                                      final boolean debug)
        {
        final String SOURCE = "EphemeridesHelper.getLongitude() ";
        final List<Metadata> listMetadata;
        final Metadata metadataLongitude;
        final double dblLongitudeObservatory;
        final List<String> errors;

        errors = new ArrayList<String>(10);

        // Now try to find the Observatory topocentric information
        // We don't need the Instrument, DAOWrapper or DAO, since we are only looking for Observatory
        listMetadata = MetadataHelper.collectAggregateMetadataTraced(framework,
                                                                     observatory,
                                                                     null,
                                                                     null, null,
                                                                     SOURCE,
                                                                     debug);

        metadataLongitude = MetadataHelper.getMetadataByKey(listMetadata,
                                                            MetadataDictionary.KEY_OBSERVATORY_LONGITUDE.getKey());
        if (metadataLongitude != null)
            {
            final DegMinSecInterface dmsLongitudeObservatory;

            // Longitude  -179:59:59.9999 to +000:00:00.0000 to +179:59:59.9999  deg:mm:ss
            dmsLongitudeObservatory = (DegMinSecInterface)DataTypeHelper.parseDataTypeFromValueField(metadataLongitude.getValue(),
                                                                                                     DataTypeDictionary.SIGNED_LONGITUDE,
                                                                                                     EMPTY_STRING,
                                                                                                     EMPTY_STRING,
                                                                                                     errors);
            if ((dmsLongitudeObservatory != null)
                && (errors.size() == 0))
                {
                // This must be correct to have passed the Metadata parsing above
                dblLongitudeObservatory = dmsLongitudeObservatory.toDouble();

//                LOGGER.log(SOURCE + "[Observatory.Longitude="
//                           + Double.toString(dblLongitudeObservatory) + "]");
                }
            else
                {
                // Observatory.Longitude was invalid, so use Framework.Longitude
                dblLongitudeObservatory = REGISTRY.getFramework().getLongitude().toDouble();

                LOGGER.warn(SOURCE + "Observatory.Longitude was not found [Framework.Longitude="
                            + Double.toString(dblLongitudeObservatory) + "]");

                // Show any Errors as just Warnings
                if (errors.size() > 0)
                    {
                    LOGGER.errors("Warning: " + SOURCE, errors);
                    }
                }
            }
        else
            {
            // Observatory.Longitude was not found, so use Framework.Longitude
            dblLongitudeObservatory = REGISTRY.getFramework().getLongitude().toDouble();

            LOGGER.warn(SOURCE + "Observatory.Longitude was not found [Framework.Longitude="
                        + Double.toString(dblLongitudeObservatory) + "]");
            }

        return (dblLongitudeObservatory);
        }


    /***********************************************************************************************
     * Get the Observatory Latitude, or the Framework Latitude if the Observatory is not available.
     *
     * @param framework
     * @param observatory
     * @param debug
     *
     * @return double
     */

    public static double getLatitude(final FrameworkPlugin framework,
                                     final ObservatoryInterface observatory,
                                     final boolean debug)
        {
        final String SOURCE = "EphemeridesHelper.getLatitude() ";
        final List<Metadata> listMetadata;
        final Metadata metadataLatitude;
        final double dblLatitudeObservatory;
        final List<String> errors;

        errors = new ArrayList<String>(10);

        // Now try to find the Observatory topocentric information
        // We don't need the Instrument, DAOWrapper or DAO, since we are only looking for Observatory
        listMetadata = MetadataHelper.collectAggregateMetadataTraced(framework,
                                                                     observatory,
                                                                     null,
                                                                     null, null,
                                                                     SOURCE,
                                                                     debug);

        metadataLatitude = MetadataHelper.getMetadataByKey(listMetadata,
                                                           MetadataDictionary.KEY_OBSERVATORY_LATITUDE.getKey());
        if (metadataLatitude != null)
            {
            final DegMinSecInterface dmsLatitudeObservatory;

            // Latitude  -89:59:59.9999 to +00:00:00.0000 to +89:59:59.9999  deg:mm:ss
            dmsLatitudeObservatory = (DegMinSecInterface)DataTypeHelper.parseDataTypeFromValueField(metadataLatitude.getValue(),
                                                                                                    DataTypeDictionary.LATITUDE,
                                                                                                    EMPTY_STRING,
                                                                                                    EMPTY_STRING,
                                                                                                    errors);
            if ((dmsLatitudeObservatory != null)
                && (errors.size() == 0))
                {
                // This must be correct to have passed the Metadata parsing above
                dblLatitudeObservatory = dmsLatitudeObservatory.toDouble();

//                LOGGER.log(SOURCE + "[Observatory.Latitude="
//                           + Double.toString(dblLatitudeObservatory) + "]");
                }
            else
                {
                // Observatory.Latitude was invalid, so use Framework.Latitude
                dblLatitudeObservatory = REGISTRY.getFramework().getLatitude().toDouble();

                LOGGER.warn(SOURCE + "Observatory.Latitude was not found [Framework.Latitude="
                            + Double.toString(dblLatitudeObservatory) + "]");

                // Show any Errors as just Warnings
                if (errors.size() > 0)
                    {
                    LOGGER.errors("Warning: " + SOURCE, errors);
                    }
                }
            }
        else
            {
            // Observatory.Latitude was not found, so use Framework.Latitude
            dblLatitudeObservatory = REGISTRY.getFramework().getLatitude().toDouble();

            LOGGER.warn(SOURCE + "Observatory.Latitude was not found [Framework.Latitude="
                        + Double.toString(dblLatitudeObservatory) + "]");
            }

        return (dblLatitudeObservatory);
        }


    /***********************************************************************************************
     * Get the Observatory HASL, or the Framework HASL if the Observatory is not available.
     *
     * @param framework
     * @param observatory
     * @param debug
     *
     * @return double
     */

    public static double getHASL(final FrameworkPlugin framework,
                                 final ObservatoryInterface observatory,
                                 final boolean debug)
        {
        final String SOURCE = "EphemeridesHelper.getHASL() ";
        final List<Metadata> listMetadata;
        final Metadata metadataHASL;
        double dblHASL;

        // Now try to find the Observatory topocentric information
        // We don't need the Instrument, DAOWrapper or DAO, since we are only looking for Observatory
        listMetadata = MetadataHelper.collectAggregateMetadataTraced(framework,
                                                                     observatory,
                                                                     null,
                                                                     null,
                                                                     null,
                                                                     SOURCE,
                                                                     debug);

        metadataHASL = MetadataHelper.getMetadataByKey(listMetadata,
                                                       MetadataDictionary.KEY_OBSERVATORY_HASL.getKey());
        if (metadataHASL != null)
            {
            try
                {
                // The very unlikely event of a NumberFormatException is trapped below
                dblHASL = Double.parseDouble(metadataHASL.getValue());
                }

            catch (final NumberFormatException exception)
                {
                // Observatory.HASL was not found, so use Framework.HASL
                dblHASL = REGISTRY.getFramework().getHASL();

                LOGGER.warn(SOURCE + "Observatory.HASL was not found [Framework.HASL="
                            + Double.toString(dblHASL) + "]");
                }
            }
        else
            {
            // Observatory.HASL was not found, so use Framework.HASL
            dblHASL = REGISTRY.getFramework().getHASL();

            LOGGER.warn(SOURCE + "Observatory.HASL was not found [Framework.HASL="
                        + Double.toString(dblHASL) + "]");
            }

        return (dblHASL);
        }


    /***********************************************************************************************
     * Create the Metadata describing the Ephemeris.
     *
     * @return List<Metadata>
     */

    public static List<Metadata> createEphemerisMetadata()
        {
        // ToDo Review need for Metadata on Ephemeris Report
        return null;
        }


    /***********************************************************************************************
     * Add the Metadata which describe the full context of the Ephemeris.
     *
     * @param metadatalist
     * @param target
     * @param jdstart
     * @param interval
     * @param epoch
     * @param longitude
     * @param timezone
     */

    public static void addEphemerisContextMetadata(final List<Metadata> metadatalist,
                                                   final String target,
                                                   final double jdstart,
                                                   final int interval,
                                                   final Epoch epoch,
                                                   final DegMinSecInterface longitude,
                                                   final TimeZone timezone)
        {
        final String SOURCE = "EphemeridesHelper.addEphemerisContextMetadata() ";
        final AstronomicalCalendarInterface calendar;
        final Metadata metadataTarget;
        final Metadata metadataDateStart;
        final Metadata metadataDateEnd;
        final Metadata metadataTimeStart;
        final Metadata metadataTimeEnd;
        final YearMonthDayInterface ymdUTStart;
        final YearMonthDayInterface ymdUTEnd;
        final HourMinSecInterface hmsUTStart;
        final HourMinSecInterface hmsUTEnd;
        final Metadata metadataInterval;
        final Metadata metadataEpoch;

        calendar = new AstronomicalCalendar(jdstart,
                                            timezone,
                                            longitude.toDouble());

        ymdUTStart = new YearMonthDayDataType((Calendar)calendar);
        ymdUTStart.enableFormatSign(false);

        hmsUTStart = new HourMinSecDataType(calendar.getUT());
        hmsUTStart.enableFormatSign(false);

        // ToDo Replace these with MetadataHelper.createOrUpdateMetadataItemTraced()
        metadataTarget = MetadataHelper.createMetadata(MetadataDictionary.KEY_INSTRUMENT_ROOT.getKey()
                                                            + KEY_EPHEMERIS_TARGET,
                                                       target,
                                                       FrameworkRegex.REGEX_STRING,
                                                       DataTypeDictionary.STRING,
                                                       SchemaUnits.DIMENSIONLESS,
                                                       "The target object for the Ephemeris");
        MetadataHelper.addOrUpdateMetadataItemTraced(metadatalist,
                                                     metadataTarget,
                                                     SOURCE,
                                                     LOADER_PROPERTIES.isMetadataDebug());

        metadataDateStart = MetadataHelper.createMetadata(MetadataDictionary.KEY_INSTRUMENT_ROOT.getKey()
                                                                + KEY_EPHEMERIS_DATE_START,
                                                          ymdUTStart.toString(),
                                                          FrameworkRegex.REGEX_DATE_ISO_YYYY_MM_DD,
                                                          DataTypeDictionary.DATE_YYYY_MM_DD,
                                                          SchemaUnits.YEAR_MONTH_DAY,
                                                          "The Date on which to start the Ephemeris");
        MetadataHelper.addOrUpdateMetadataItemTraced(metadatalist,
                                                     metadataDateStart,
                                                     SOURCE,
                                                     LOADER_PROPERTIES.isMetadataDebug());

        metadataTimeStart = MetadataHelper.createMetadata(MetadataDictionary.KEY_INSTRUMENT_ROOT.getKey()
                                                                + KEY_EPHEMERIS_TIME_START,
                                                          hmsUTStart.toString_HH_MM_SS(),
                                                          FrameworkRegex.REGEX_TIME_ISO_HH_MM_SS,
                                                          DataTypeDictionary.TIME_HH_MM_SS,
                                                          SchemaUnits.HOUR_MIN_SEC,
                                                          "The Time at which to start the Ephemeris");
        MetadataHelper.addOrUpdateMetadataItemTraced(metadatalist,
                                                     metadataTimeStart,
                                                     SOURCE,
                                                     LOADER_PROPERTIES.isMetadataDebug());

        // Restrict to 24 hours range
        calendar.roll(Calendar.DATE, true);

        ymdUTEnd = new YearMonthDayDataType((Calendar)calendar);
        ymdUTEnd.enableFormatSign(false);

        hmsUTEnd = new HourMinSecDataType(calendar.getUT());
        hmsUTEnd.enableFormatSign(false);

        metadataDateEnd = MetadataHelper.createMetadata(MetadataDictionary.KEY_INSTRUMENT_ROOT.getKey()
                                                            + KEY_EPHEMERIS_DATE_END,
                                                        ymdUTEnd.toString(),
                                                        FrameworkRegex.REGEX_DATE_ISO_YYYY_MM_DD,
                                                        DataTypeDictionary.DATE_YYYY_MM_DD,
                                                        SchemaUnits.YEAR_MONTH_DAY,
                                                        "The Date on which to finish the Ephemeris");
        MetadataHelper.addOrUpdateMetadataItemTraced(metadatalist,
                                                     metadataDateEnd,
                                                     SOURCE,
                                                     LOADER_PROPERTIES.isMetadataDebug());

        metadataTimeEnd = MetadataHelper.createMetadata(MetadataDictionary.KEY_INSTRUMENT_ROOT.getKey()
                                                            + KEY_EPHEMERIS_TIME_END,
                                                        hmsUTEnd.toString_HH_MM_SS(),
                                                        FrameworkRegex.REGEX_TIME_ISO_HH_MM_SS,
                                                        DataTypeDictionary.TIME_HH_MM_SS,
                                                        SchemaUnits.HOUR_MIN_SEC,
                                                        "The Time at which to finish the Ephemeris");
        MetadataHelper.addOrUpdateMetadataItemTraced(metadatalist,
                                                     metadataTimeEnd,
                                                     SOURCE,
                                                     LOADER_PROPERTIES.isMetadataDebug());

        metadataInterval = MetadataHelper.createMetadata(MetadataDictionary.KEY_INSTRUMENT_ROOT.getKey()
                                                            + KEY_EPHEMERIS_INTERVAL,
                                                         Integer.toString(interval),
                                                         "^([1-9][0-9]{0,3}|1[0-9]{4}|20000)$",
                                                         DataTypeDictionary.DECIMAL_INTEGER,
                                                         SchemaUnits.SECONDS,
                                                         "The time interval between Ephemeris results (1 to 20000 seconds)");
        MetadataHelper.addOrUpdateMetadataItemTraced(metadatalist,
                                                     metadataInterval,
                                                     SOURCE,
                                                     LOADER_PROPERTIES.isMetadataDebug());

        metadataEpoch = MetadataHelper.createMetadata(MetadataDictionary.KEY_INSTRUMENT_ROOT.getKey()
                                                        + KEY_EPHEMERIS_EPOCH,
                                                     epoch.getName(),
                                                     FrameworkRegex.REGEX_NONE,
                                                     DataTypeDictionary.STRING,
                                                     SchemaUnits.DIMENSIONLESS,
                                                     "The Epoch for the Ephemeris, e.g. J2000");
        MetadataHelper.addOrUpdateMetadataItemTraced(metadatalist,
                                                     metadataEpoch,
                                                     SOURCE,
                                                     LOADER_PROPERTIES.isMetadataDebug());
        }
    }
