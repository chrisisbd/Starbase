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

//-----------------------------------------------------------------------------
// GridReferenceConverter.java
//
// (c) 2003 Jonathan Stott
//
// 0.2 - 02 Mar 2004
//  - Added ability to convert UTM to/from latitude/longitude
//  - Added ability to convert OSGB grid references to/from latitude/longitude
// 0.1 - 11 Nov 2003
//  - First version
//-----------------------------------------------------------------------------
//Ellipsoid and projection constants
//
//Shape and size of biaxial ellipsoids used in the UK
//
//Name Semi-major
//axis a (m) Semi-minor
//axis b (m) Associated datums and projections
//Airy 1830 6 377 563.396 6 356 256.910 OSGB36�, National Grid
//Airy 1830 modified 6 377 340.189 6 356 034.447 Ireland 65, Irish National Grid
//International 1924, aka Hayford 1909 6 378 388.000 6 356 911.946 ED50, UTM
//GRS80 aka WGS84 ellipsoid 6 378 137.000 6 356 752.3141 WGS84, ITRS, ETRS89.
//
//The ellipsoid squared eccentricity constant  is computed from a and b by equation (A1).
//
//Transverse Mercator projections used in the UK
//
//Projection  Scale factor on central meridian   True origin,  and   Map coordinates of true origin (metres),
// and   Ellipsoid
//National Grid 0.9996012717 lat 49� N
//long 2� W E 400 000
//N -100 000 Airy 1830
//Irish National Grid 1.000035 lat 53� 30� N
//long 8� W E 200 000
//N 250 000 Airy 1830 modified
//UTM zone 29 0.9996 lat 0�
//long 9� W E 500 000
//N 0 International 1924
//UTM zone 30 0.9996 lat 0�
//long 3� W E 500 000
//N 0 International 1924
//UTM zone 31 0.9996 lat 0�
//long 3� E E 500 000
//N 0 International 1924

package org.lmn.fc.common.utilities.coords;

import org.lmn.fc.model.locale.CountryPlugin;

/**
 * Convert latitude/longitude to OSGB grid references and vice versa. Convert latitude/longitude to
 * UTM references and vice/versa.
 * <p/>
 * Based on algorithm published by the Ordnance Survey at http://www.gps.gov.uk/guidecontents.asp
 *
 * @author Jonathan Stott
 * @version 0.2
 *
 * See: http://www.jstott.me.uk/jcoord/
 */

public final class GridReferenceConverter
    {

    private static final double OSGB_F0 = 0.9996012717;
    private static final double UTM_F0 = 0.9996;
    private static final double IRISH_F0 = 1.000035;

    /**
     * Convert an OSGB grid reference to latitude and longitude
     *
     * @param osgb the OSGB grid reference to convert
     * @return the latitude and longitude
     * @since 0.1
     */
    public static LatitudeLongitude OSGBGridReferenceToLatitudeLongitude(final OSGBGridReference osgb)
        {

        final ReferenceEllipsoid ellipsoid = ReferenceEllipsoids.AIRY_1830;
        final double N0 = -100000.0;
        final double E0 = 400000.0;
        final double phi0 = Math.toRadians(49.0);
        final double lambda0 = Math.toRadians(-2.0);
        final double a = ellipsoid.getSemiMajorAxis();
        final double b = ellipsoid.getSemiMinorAxis();
        final double eSquared = ellipsoid.getEccentricitySquared();
        double phi = 0.0;
        double lambda = 0.0;
        final double E = osgb.getEasting();
        final double N = osgb.getNorthing();
        final double n = (a - b) / (a + b);
        double M = 0.0;
        double phiPrime = ((N - N0) / (a * OSGB_F0)) + phi0;
        do
            {
            M =
                (b * OSGB_F0)
                * (((1 + n + ((5.0 / 4.0) * n * n) + ((5.0 / 4.0) * n * n * n))
                * (phiPrime - phi0))
                - (((3 * n) + (3 * n * n) + ((21.0 / 8.0) * n * n * n))
                * sin(phiPrime - phi0)
                * cos(phiPrime + phi0))
                + ((((15.0 / 8.0) * n * n) + ((15.0 / 8.0) * n * n * n))
                * sin(2.0 * (phiPrime - phi0))
                * cos(2.0 * (phiPrime + phi0)))
                - (((35.0 / 24.0) * n * n * n)
                * sin(3.0 * (phiPrime - phi0))
                * cos(3.0 * (phiPrime + phi0))));
            phiPrime += (N - N0 - M) / (a * OSGB_F0);
            }
        while ((N - N0 - M) >= 0.001);
        final double v = a * OSGB_F0 * Math.pow(1.0 - eSquared * sinSquared(phiPrime), -0.5);
        final double rho =
            a
            * OSGB_F0
            * (1.0 - eSquared)
            * pow(1.0 - eSquared * sinSquared(phiPrime), -1.5);
        final double etaSquared = (v / rho) - 1.0;
        final double VII = tan(phiPrime) / (2 * rho * v);
        final double VIII =
            (tan(phiPrime) / (24.0 * rho * pow(v, 3.0)))
            * (5.0
            + (3.0 * tanSquared(phiPrime))
            + etaSquared
            - (9.0 * tanSquared(phiPrime) * etaSquared));
        final double IX =
            (tan(phiPrime) / (720.0 * rho * pow(v, 5.0)))
            * (61.0
            + (90.0 * tanSquared(phiPrime))
            + (45.0 * tanSquared(phiPrime) * tanSquared(phiPrime)));
        final double X = sec(phiPrime) / v;
        final double XI =
            (sec(phiPrime) / (6.0 * v * v * v))
            * ((v / rho) + (2 * tanSquared(phiPrime)));
        final double XII =
            (sec(phiPrime) / (120.0 * pow(v, 5.0)))
            * (5.0
            + (28.0 * tanSquared(phiPrime))
            + (24.0 * tanSquared(phiPrime) * tanSquared(phiPrime)));
        final double XIIA =
            (sec(phiPrime) / (5040.0 * pow(v, 7.0)))
            * (61.0
            + (662.0 * tanSquared(phiPrime))
            + (1320.0 * tanSquared(phiPrime) * tanSquared(phiPrime))
            + (720.0
            * tanSquared(phiPrime)
            * tanSquared(phiPrime)
            * tanSquared(phiPrime)));
        phi =
            phiPrime
            - (VII * pow(E - E0, 2.0))
            + (VIII * pow(E - E0, 4.0))
            - (IX * pow(E - E0, 6.0));
        lambda =
            lambda0
            + (X * (E - E0))
            - (XI * pow(E - E0, 3.0))
            + (XII * pow(E - E0, 5.0))
            - (XIIA * pow(E - E0, 7.0));

        final LatitudeLongitude latLong =
            new LatitudeLongitude(Math.toDegrees(phi), Math.toDegrees(lambda));
        return latLong;
        }


    /***********************************************************************************************
     * Convert a latitude and longitude into a grid reference for the specified Country.
     *
     * @param country
     * @param latitudeLongitude
     *
     * @return OSGBGridReference
     */

    public static OSGBGridReference convertLatLongToGridRef(CountryPlugin country,
                                                            final LatitudeLongitude latitudeLongitude)
        {
        // ToDo Select the correct National Grid using CountryData
        return (convertLatLongToGridRef(latitudeLongitude));
        }


    /**
     * Convert a latitude and longitude into an OSGB grid reference
     *
     * @param latitudeLongitude the latitude and longitude to convert
     * @return the OSGB grid reference
     * @since 0.1
     */
    public static OSGBGridReference convertLatLongToGridRef(final LatitudeLongitude latitudeLongitude)
        {

        final ReferenceEllipsoid ellipsoid = ReferenceEllipsoids.AIRY_1830;
        final double N0 = -100000.0;
        final double E0 = 400000.0;
        final double phi0 = Math.toRadians(49.0);
        final double lambda0 = Math.toRadians(-2.0);
        final double a = ellipsoid.getSemiMajorAxis();
        final double b = ellipsoid.getSemiMinorAxis();
        final double eSquared = ellipsoid.getEccentricitySquared();
        final double phi = Math.toRadians(latitudeLongitude.getLatitude());
        final double lambda = Math.toRadians(latitudeLongitude.getLongitude());
        double E = 0.0;
        double N = 0.0;
        final double n = (a - b) / (a + b);
        final double v = a * OSGB_F0 * Math.pow(1.0 - eSquared * sinSquared(phi), -0.5);
        final double rho =
            a * OSGB_F0 * (1.0 - eSquared) * pow(1.0 - eSquared * sinSquared(phi), -1.5);
        final double etaSquared = (v / rho) - 1.0;
        final double M =
            (b * OSGB_F0)
            * (((1 + n + ((5.0 / 4.0) * n * n) + ((5.0 / 4.0) * n * n * n))
            * (phi - phi0))
            - (((3 * n) + (3 * n * n) + ((21.0 / 8.0) * n * n * n))
            * sin(phi - phi0)
            * cos(phi + phi0))
            + ((((15.0 / 8.0) * n * n) + ((15.0 / 8.0) * n * n * n))
            * sin(2.0 * (phi - phi0))
            * cos(2.0 * (phi + phi0)))
            - (((35.0 / 24.0) * n * n * n)
            * sin(3.0 * (phi - phi0))
            * cos(3.0 * (phi + phi0))));
        final double I = M + N0;
        final double II = (v / 2.0) * sin(phi) * cos(phi);
        final double III =
            (v / 24.0)
            * sin(phi)
            * pow(cos(phi), 3.0)
            * (5.0 - tanSquared(phi) + (9.0 * etaSquared));
        final double IIIA =
            (v / 720.0)
            * sin(phi)
            * pow(cos(phi), 5.0)
            * (61.0 - (58.0 * tanSquared(phi)) + pow(tan(phi), 4.0));
        final double IV = v * cos(phi);
        final double V = (v / 6.0) * pow(cos(phi), 3.0) * ((v / rho) - tanSquared(phi));
        final double VI =
            (v / 120.0)
            * pow(cos(phi), 5.0)
            * (5.0
            - (18.0 * tanSquared(phi))
            + (pow(tan(phi), 4.0))
            + (14 * etaSquared)
            - (58 * tanSquared(phi) * etaSquared));

        N =
            I
            + (II * pow(lambda - lambda0, 2.0))
            + (III * pow(lambda - lambda0, 4.0))
            + (IIIA * pow(lambda - lambda0, 6.0));
        E =
            E0
            + (IV * (lambda - lambda0))
            + (V * pow(lambda - lambda0, 3.0))
            + (VI * pow(lambda - lambda0, 5.0));

        final OSGBGridReference eastNorth = new OSGBGridReference((int) E, (int) N);
        return eastNorth;
        }


    /**
     * Convert an UTM reference to a latitude and longitude
     *
     * @param ellipsoid A reference ellipsoid to use
     * @param utm       the UTM reference to convert
     * @return the converted latitude and longitude
     * @since 0.2
     */
    public static LatitudeLongitude UTMReferenceToLatitudeLongitude(final ReferenceEllipsoid ellipsoid,
                                                                    final UTMReference utm)
        {

        final double a = ellipsoid.getSemiMajorAxis();
        final double eSquared = ellipsoid.getEccentricitySquared();
        final double ePrimeSquared = eSquared / (1.0 - eSquared);
        final double e1 = (1 - sqrt(1 - eSquared)) / (1 + sqrt(1 - eSquared));
        final double x = utm.getEasting() - 500000.0;
        ;
        double y = utm.getNorthing();
        final int zoneNumber = utm.getLongitudeZone();
        final char zoneLetter = utm.getLatitudeZone();
        final double longitudeOrigin = (zoneNumber - 1.0) * 6.0 - 180.0 + 3.0;

        // Correct y for southern hemisphere
        if ((zoneLetter - 'N') < 0)
            {
            y -= 10000000.0;
            }

        final double m = y / UTM_F0;
        final double mu =
            m
            / (a
            * (1.0
            - eSquared / 4.0
            - 3.0 * eSquared * eSquared / 64.0
            - 5.0
            * pow(eSquared, 3.0)
            / 256.0));

        final double phi1Rad =
            mu
            + (3.0 * e1 / 2.0 - 27.0 * pow(e1, 3.0) / 32.0) * sin(2.0 * mu)
            + (21.0 * e1 * e1 / 16.0 - 55.0 * pow(e1, 4.0) / 32.0)
            * sin(4.0 * mu)
            + (151.0 * pow(e1, 3.0) / 96.0) * sin(6.0 * mu);

        final double n =
            a
            / sqrt(1.0 - eSquared * sin(phi1Rad) * sin(phi1Rad));
        final double t = tan(phi1Rad) * tan(phi1Rad);
        final double c = ePrimeSquared * cos(phi1Rad) * cos(phi1Rad);
        final double r =
            a
            * (1.0 - eSquared)
            / pow(1.0 - eSquared * sin(phi1Rad) * sin(phi1Rad),
                  1.5);
        final double d = x / (n * UTM_F0);

        final double latitude = (

            phi1Rad
            - (n * tan(phi1Rad) / r)
            * (d * d / 2.0
            - (5.0
            + (3.0 * t)
            + (10.0 * c)
            - (4.0 * c * c)
            - (9.0 * ePrimeSquared))
            * pow(d, 4.0)
            / 24.0
            + (61.0
            + (90.0 * t)
            + (298.0 * c)
            + (45.0 * t * t)
            - (252.0 * ePrimeSquared)
            - (3.0 * c * c))
            * pow(d, 6.0)
            / 720.0)) * (180.0 / Math.PI);

        final double longitude = longitudeOrigin + (
            (d
            - (1.0 + 2.0 * t + c) * pow(d, 3.0) / 6.0
            + (5.0
            - (2.0 * c)
            + (28.0 * t)
            - (3.0 * c * c)
            + (8.0 * ePrimeSquared)
            + (24.0 * t * t))
            * pow(d, 5.0)
            / 120.0)
            / cos(phi1Rad)) * (180.0 / Math.PI);

        return new LatitudeLongitude(latitude, longitude);
        }


    /**
     * Convert a latitude and longitude to an UTM reference
     *
     * @param ellipsoid         A reference ellipsoid to use
     * @param latitudeLongitude The latitude and longitude to convert
     * @return the converted UTM reference
     * @since 0.2
     */
    public static UTMReference latitudeLongitudeToUTMReference(final ReferenceEllipsoid ellipsoid,
                                                               final LatitudeLongitude latitudeLongitude)
        {

        final double a = ellipsoid.getSemiMajorAxis();
        final double eSquared = ellipsoid.getEccentricitySquared();
        final double longitude = latitudeLongitude.getLongitude();
        final double latitude = latitudeLongitude.getLatitude();

        final double latitudeRad = latitude * (Math.PI / 180.0);
        final double longitudeRad = longitude * (Math.PI / 180.0);
        int longitudeZone = (int) ((longitude + 180.0) / 6.0) + 1;

        // Special zone for Norway
        if (latitude >= 56.0
            && latitude < 64.0
            && longitude >= 3.0
            && longitude < 12.0)
            {
            longitudeZone = 32;
            }

        // Special zones for Svalbard
        if (latitude >= 72.0 && latitude < 84.0)
            {
            if (longitude >= 0.0 && longitude < 9.0)
                {
                longitudeZone = 31;
                }
            else if (longitude >= 9.0 && longitude < 21.0)
                {
                longitudeZone = 33;
                }
            else if (longitude >= 21.0 && longitude < 33.0)
                {
                longitudeZone = 35;
                }
            else if (longitude >= 33.0 && longitude < 42.0)
                {
                longitudeZone = 37;
                }
            }

        final double longitudeOrigin = (longitudeZone - 1) * 6 - 180 + 3;
        final double longitudeOriginRad = longitudeOrigin * (Math.PI / 180.0);

        final char UTMZone = getUTMLatitudeZoneLetter(latitude);

        final double ePrimeSquared = (eSquared) / (1 - eSquared);

        final double n = a / sqrt(1 - eSquared * sin(latitudeRad) * sin(latitudeRad));
        final double t = tan(latitudeRad) * tan(latitudeRad);
        final double c = ePrimeSquared * cos(latitudeRad) * cos(latitudeRad);
        final double A = cos(latitudeRad) * (longitudeRad - longitudeOriginRad);

        final double M =
            a
            * ((1
            - eSquared / 4
            - 3 * eSquared * eSquared / 64
            - 5 * eSquared * eSquared * eSquared / 256)
            * latitudeRad
            - (3 * eSquared / 8
            + 3 * eSquared * eSquared / 32
            + 45 * eSquared * eSquared * eSquared / 1024)
            * sin(2 * latitudeRad)
            + (15 * eSquared * eSquared / 256
            + 45 * eSquared * eSquared * eSquared / 1024)
            * sin(4 * latitudeRad)
            - (35 * eSquared * eSquared * eSquared / 3072)
            * sin(6 * latitudeRad));

        final double UTMEasting =
            (UTM_F0
            * n
            * (A
            + (1 - t + c) * pow(A, 3.0) / 6
            + (5 - 18 * t + t * t + 72 * c - 58 * ePrimeSquared)
            * pow(A, 5.0)
            / 120)
            + 500000.0);

        double UTMNorthing =
            (UTM_F0
            * (M
            + n
            * tan(latitudeRad)
            * (A * A / 2
            + (5 - t + (9 * c) + (4 * c * c)) * pow(A, 4.0) / 24
            + (61 - (58 * t) + (t * t) + (600 * c) - (330 * ePrimeSquared))
            * pow(A, 6.0)
            / 720)));

        // Adjust for the southern hemisphere
        if (latitude < 0)
            {
            UTMNorthing += 10000000.0;
            }

        return new UTMReference(UTMEasting, UTMNorthing, UTMZone, longitudeZone);
        }


    /**
     * Work out the UTM latitude zone from the latitude
     *
     * @param latitude
     * @return
     * @since 0.2
     */
    private static char getUTMLatitudeZoneLetter(final double latitude)
        {
        if ((84 >= latitude) && (latitude >= 72))
            return 'X';
        else if ((72 > latitude) && (latitude >= 64))
            return 'W';
        else if ((64 > latitude) && (latitude >= 56))
            return 'V';
        else if ((56 > latitude) && (latitude >= 48))
            return 'U';
        else if ((48 > latitude) && (latitude >= 40))
            return 'T';
        else if ((40 > latitude) && (latitude >= 32))
            return 'S';
        else if ((32 > latitude) && (latitude >= 24))
            return 'R';
        else if ((24 > latitude) && (latitude >= 16))
            return 'Q';
        else if ((16 > latitude) && (latitude >= 8))
            return 'P';
        else if ((8 > latitude) && (latitude >= 0))
            return 'N';
        else if ((0 > latitude) && (latitude >= -8))
            return 'M';
        else if ((-8 > latitude) && (latitude >= -16))
            return 'L';
        else if ((-16 > latitude) && (latitude >= -24))
            return 'K';
        else if ((-24 > latitude) && (latitude >= -32))
            return 'J';
        else if ((-32 > latitude) && (latitude >= -40))
            return 'H';
        else if ((-40 > latitude) && (latitude >= -48))
            return 'G';
        else if ((-48 > latitude) && (latitude >= -56))
            return 'F';
        else if ((-56 > latitude) && (latitude >= -64))
            return 'E';
        else if ((-64 > latitude) && (latitude >= -72))
            return 'D';
        else if ((-72 > latitude) && (latitude >= -80))
            return 'C';
        else
            return 'Z';
        }


    /**
     * Convert an UTM reference to an OSGB grid reference
     *
     * @param ellipsoid
     * @param osgb
     * @return
     * @since 0.2
     */
    public static UTMReference OSGBGridReferenceToUTMReference(final ReferenceEllipsoid ellipsoid,
                                                               final OSGBGridReference osgb)
        {
        final LatitudeLongitude ll = OSGBGridReferenceToLatitudeLongitude(osgb);
        return latitudeLongitudeToUTMReference(ellipsoid, ll);
        }


    /**
     * Convert an OSGB grid reference to an UTM reference
     *
     * @param ellipsoid
     * @param utm
     * @return
     * @since 0.2
     */
    public static OSGBGridReference UTMReferenceToOSGBGridReference(final ReferenceEllipsoid ellipsoid,
                                                                    final UTMReference utm)
        {
        final LatitudeLongitude ll = UTMReferenceToLatitudeLongitude(ellipsoid, utm);
        return convertLatLongToGridRef(ll);
        }


    /**
     * Calculate sin(x)
     *
     * @param x
     * @return
     * @since 0.1
     */
    private static double sin(final double x)
        {
        return Math.sin(x);
        }


    /**
     * Calculate sin^2(x)
     *
     * @param x
     * @return
     * @since 0.1
     */
    private static double sinSquared(final double x)
        {
        return sin(x) * sin(x);
        }


    /**
     * Calculate cos(x)
     *
     * @param x
     * @return
     * @since 0.1
     */
    private static double cos(final double x)
        {
        return Math.cos(x);
        }


    /**
     * Calculate tan(x)
     *
     * @param x
     * @return
     * @since 0.1
     */
    private static double tan(final double x)
        {
        return Math.tan(x);
        }


    /**
     * Calculate tan^2(x)
     *
     * @param x
     * @return
     * @since 0.1
     */
    private static double tanSquared(final double x)
        {
        return tan(x) * tan(x);
        }


    /**
     * Calculate sec(x)
     *
     * @param x
     * @return
     * @since 0.1
     */
    private static double sec(final double x)
        {
        return 1.0 / cos(x);
        }


    /**
     * Calculate x^e
     *
     * @param x
     * @param e
     * @return
     * @since 0.1
     */
    private static double pow(final double x, final double e)
        {
        return Math.pow(x, e);
        }


    /**
     * Calculate sqrt(x)
     *
     * @param x
     * @return
     * @since 0.2
     */
    private static double sqrt(final double x)
        {
        return Math.sqrt(x);
        }
    }
