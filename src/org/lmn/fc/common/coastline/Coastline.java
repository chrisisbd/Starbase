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

package org.lmn.fc.common.coastline;

import org.lmn.fc.common.coastline.impl.CoastlineSegment;
import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.common.datatranslators.DataFormat;
import org.lmn.fc.common.utilities.astronomy.CoordinateConversions;
import org.lmn.fc.common.utilities.files.FileUtilities;
import org.lmn.fc.model.datatypes.DegMinSecFormat;
import org.lmn.fc.model.datatypes.DegMinSecInterface;
import org.lmn.fc.model.datatypes.types.LatitudeDataType;
import org.lmn.fc.model.datatypes.types.LongitudeDataType;

import java.awt.geom.Point2D;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

// The original Coastline file contains segments:
//        # -b
//        -6.450276	49.872593
//        -6.450570	49.873180
//        -6.449983	49.873767
//        -6.447636	49.873767
//        -6.447049	49.873180
//        -6.447049	49.872593
//        -6.447636	49.872007
//        -6.449983	49.872007
//        -6.450276	49.872593

/***************************************************************************************************
 * Coastline.
 * See: http://rimmer.ngdc.noaa.gov
 *
 * Use NO COMPRESSION,
 * MAPGEN FORMAT,
 * GMT PLOT,
 * WORLD DATA BANK II
 * WEST is NEGATIVE.
 */

public final class Coastline implements FrameworkStrings,
                                        FrameworkSingletons
    {
    // String Resources
    private static final String SEGMENT_HEADER = "# -b";

    private final List<CoastlineSegmentInterface> listSegments;

    // The maximum extents of the Coastline map defined in degrees
    private double dblExtentEast;
    private double dblExtentWest;
    private double dblExtentSouth;
    private double dblExtentNorth;


    /***********************************************************************************************
     * Construct a Coastline.
     */

    public Coastline()
        {
        this.listSegments = new ArrayList<CoastlineSegmentInterface>(1000);

        // Set extents which must change
        this.dblExtentEast = CoordinateConversions.LONGITUDE_RANGE_MIN;
        this.dblExtentWest = CoordinateConversions.LONGITUDE_RANGE_MAX;
        this.dblExtentSouth = CoordinateConversions.LATITUDE_RANGE_MAX;
        this.dblExtentNorth = CoordinateConversions.LATITUDE_RANGE_MIN;
        }


    /***********************************************************************************************
     * Import a coastline file as downloaded from http://rimmer.ngdc.noaa.gov in Mapgen format.
     *
     * @param filename
     * @param eventlog
     *
     * @return boolean
     */

    public boolean importCoastline(final String filename,
                                   final Vector<Vector> eventlog)
        {
        boolean boolSuccess;

        boolSuccess = false;

//        SimpleEventLogUIComponent.logEvent(eventlog,
//                                           EventStatus.INFO,
//                                           "message",
//                                           clock);

        if (filename != null)
            {
            try
                {
                final File file;
                final BufferedReader reader;
                final long longFileLength;
                String strLine;
                CoastlineSegmentInterface segment;

                file = new File(filename);
                reader = new BufferedReader(new FileReader(file));
                longFileLength = file.length();
                getSegments().clear();
                segment = null;

                while ((strLine = reader.readLine()) != null)
                    {
                    // Make the line into "-6.450276<tab>49.872593" OR "# -b"
                    strLine = strLine.trim();

                    // Is this line the header "# -b" ?
                    if (strLine.contains(SEGMENT_HEADER))
                        {
                        // Do we have a previous segment to add?
                        if (segment != null)
                            {
                            addSegmentToCoastline(segment);
                            }

                        // Create a new segment
                        segment = new CoastlineSegment();
                        }
                    else
                        {
                        final double longitude;
                        final double latitude;

                        // Read the points from the line and add them to the segment created earlier
                        longitude = Double.parseDouble(strLine.substring(0, strLine.indexOf(0x09)));
                        latitude = Double.parseDouble(strLine.substring(strLine.indexOf(0x09) + 1));
                        segment.addPoint(longitude, latitude);
                        }
                    }

                // Do we have a final segment to add?
                if (segment != null)
                    {
                    addSegmentToCoastline(segment);
                    }

                boolSuccess = true;
                reader.close();

                // ToDo Log the file length?
                }

            catch (FileNotFoundException exception)
                {
                // ToDo Log error
                exception.printStackTrace();
                }

            catch (IOException exception)
                {
                // ToDo Log error
                exception.printStackTrace();
                }

            catch (NumberFormatException exception)
                {
                // ToDo Log error
                exception.printStackTrace();
                }
            }
        else
            {
            // ToDo Log the error
            }

        return (boolSuccess);
        }


    /***********************************************************************************************
     * Export a coastline.
     * Intended only as a check on the imported data, the CSV is not required for map generation.
     *
     * @param filename
     * @param timestamp
     * @param eventlog
     *
     * @return boolean
     */

    public boolean exportCoastline(final String filename,
                                   final boolean timestamp,
                                   final Vector<Vector> eventlog)
        {
        boolean boolSuccess;

        boolSuccess = false;

        if ((filename != null)
            && (!FrameworkStrings.EMPTY_STRING.equals(filename))
            && (eventlog != null)
            && (getSegments() != null))
            {
            try
                {
                final File file;
                final BufferedWriter writer;
                final Iterator<CoastlineSegmentInterface> iterSegments;
                final StringBuffer buffer;
                DegMinSecInterface dmsLongitude;
                DegMinSecInterface dmsLatitude;
                final String strWestExtent;
                final String strEastExtent;
                final String strSouthExtent;
                final String strNorthExtent;

                file = new File(FileUtilities.buildFullFilename(filename, timestamp, DataFormat.TSV));
                FileUtilities.overwriteFile(file);
                writer = new BufferedWriter(new FileWriter(file));
                buffer = new StringBuffer();

                // WEST is NEGATIVE in the Coastline, but POSITIVE in DegMinSec, so change sign
                dmsLongitude = new LongitudeDataType(-getWestExtent());
                dmsLongitude.setDisplayFormat(DegMinSecFormat.EW);
                strWestExtent = dmsLongitude.toString();

                // EAST is POSITIVE in the Coastline, but NEGATIVE in DegMinSec, so change sign
                dmsLongitude = new LongitudeDataType(-getEastExtent());
                dmsLongitude.setDisplayFormat(DegMinSecFormat.EW);
                strEastExtent = dmsLongitude.toString();

                dmsLatitude = new LatitudeDataType(getSouthExtent());
                dmsLatitude.setDisplayFormat(DegMinSecFormat.NS);
                strSouthExtent = dmsLatitude.toString();

                dmsLatitude = new LatitudeDataType(getNorthExtent());
                dmsLatitude.setDisplayFormat(DegMinSecFormat.NS);
                strNorthExtent = dmsLatitude.toString();

                // Give the user some helpful information about the coverage of the map
                buffer.append("# Starbase Coastline Converter");
                writer.write(buffer.toString());
                writer.newLine();

                buffer.setLength(0);
                buffer.append("# Coastline data generated by Coastline Extractor at http://rimmer.ngdc.noaa.gov");
                writer.write(buffer.toString());
                writer.newLine();

                buffer.setLength(0);
                buffer.append("# Settings: World Data Bank II, No Compression, Mapgen Format");
                writer.write(buffer.toString());
                writer.newLine();

                buffer.setLength(0);
                buffer.append("# Note that the Coastline Extractor treats WEST as NEGATIVE,");
                writer.write(buffer.toString());
                writer.newLine();

                buffer.setLength(0);
                buffer.append("# but Starbase follows the astronomical convention that WEST is POSITIVE");
                writer.write(buffer.toString());
                writer.newLine();

                buffer.setLength(0);
                buffer.append("# Top Left is at longitude ");
                buffer.append(strWestExtent);
                buffer.append("  latitude ");
                buffer.append(strNorthExtent);
                buffer.append("  Coastline:(");
                buffer.append(getWestExtent());
                buffer.append(", ");
                buffer.append(getNorthExtent());
                buffer.append(")");
                writer.write(buffer.toString());
                writer.newLine();

                buffer.setLength(0);
                buffer.append("# Bottom Right is at longitude ");
                buffer.append(strEastExtent);
                buffer.append("  latitude ");
                buffer.append(strSouthExtent);
                buffer.append("  Coastline:(");
                buffer.append(getEastExtent());
                buffer.append(", ");
                buffer.append(getSouthExtent());
                buffer.append(")");
                writer.write(buffer.toString());
                writer.newLine();

                iterSegments = getSegments().iterator();

                while (iterSegments.hasNext())
                    {
                    final CoastlineSegmentInterface segment;
                    final Iterator<Point2D.Double> iterPoints;

                    segment = iterSegments.next();
                    iterPoints = segment.getPoints().iterator();

                    buffer.setLength(0);
                    buffer.append("# Segment [count=");
                    buffer.append(segment.getPoints().size());
                    buffer.append("]");
                    writer.write(buffer.toString());
                    writer.newLine();

                    while (iterPoints.hasNext())
                        {
                        final Point2D.Double point;

                        point = iterPoints.next();

                        buffer.setLength(0);
                        buffer.append(point.getX());
                        buffer.append(SPACE);
                        buffer.append(point.getY());
                        writer.write(buffer.toString());
                        writer.newLine();
                        }
                    }

                // Tidy up
                writer.flush();
                writer.close();
                boolSuccess = true;
                }

            catch (FileNotFoundException exception)
                {
                // ToDo Log error
                exception.printStackTrace();
                }

            catch (IOException exception)
                {
                // ToDo Log error
                exception.printStackTrace();
                }
            }
        else
            {
            // ToDo Log the error
            }

        return (boolSuccess);
        }


    /***********************************************************************************************
     * Add the specified segment to the Coastline, updating the extents if necessary.
     *
     * @param segment
     */

    private void addSegmentToCoastline(final CoastlineSegmentInterface segment)
        {
        if ((getSegments() != null)
            && (segment != null))
            {
            getSegments().add(segment);

            // Update the extents
            if (segment.getWestExtent() < getWestExtent())
                {
                setWestExtent(segment.getWestExtent());
                }
            else if (segment.getEastExtent() > getEastExtent())
                {
                setEastExtent(segment.getEastExtent());
                }

            if (segment.getSouthExtent() < getSouthExtent())
                {
                setSouthExtent(segment.getSouthExtent());
                }
            else if (segment.getNorthExtent() > getNorthExtent())
                {
                setNorthExtent(segment.getNorthExtent());
                }
            }
        }


    /***********************************************************************************************
     * Enlarge the extents of the map by the specified amounts in each direction,
     * to give a border area.
     * WEST is NEGATIVE.
     *
     * @param longitudeoffset
     * @param latitudeoffset
     */

    private void enlargeExtentsForBorder(final double longitudeoffset,
                                         final double latitudeoffset)
        {
        setWestExtent(getWestExtent() - Math.abs(longitudeoffset));
        setEastExtent(getEastExtent() + Math.abs(longitudeoffset));
        setSouthExtent(getSouthExtent() - Math.abs(latitudeoffset));
        setNorthExtent(getNorthExtent() + Math.abs(latitudeoffset));
        }


    /***********************************************************************************************
     * Get the List of segments making up the Coastline.
     *
     * @return List<CoastlineSegmentInterface>
     */

    public List<CoastlineSegmentInterface> getSegments()
        {
        return (this.listSegments);
        }


    /***********************************************************************************************
     * Get the aspect ratio of width/height which will give 'square degrees'.
     *
     * @return
     */

    public double getAspectRatio()
        {
        if ((getNorthExtent() - getSouthExtent()) > 0.0)
            {
            return((getEastExtent() - getWestExtent()) / (getNorthExtent() - getSouthExtent()));
            }
        else
            {
            return (1.0);
            }
        }


    /***********************************************************************************************
     * Get the CoastlineMap TopLeft coordinates.
     * BEWARE! The Coastline treats WEST as NEGATIVE.
     * Starbase treats WEST as POSITIVE - see Meeus pg. 89.
     *
     * @return Point2D.Double
     */

    public Point2D.Double getTopLeft()
        {
        return (new Point2D.Double(getWestExtent(), getNorthExtent()));
        }


    /***********************************************************************************************
     * Get the CoastlineMap BottomRight coordinates.
     * BEWARE! The Coastline treats WEST as NEGATIVE.
     * Starbase treats WEST as POSITIVE - see Meeus pg. 89.
     *
     * @return Point2D.Double
     */

    public Point2D.Double getBottomRight()
        {
        return (new Point2D.Double(getEastExtent(), getSouthExtent()));
        }


    /***********************************************************************************************
     * Get the EastExtent.
     * WEST is NEGATIVE.
     *
     * @return double
     */

    public double getEastExtent()
        {
        return (this.dblExtentEast);
        }


    /***********************************************************************************************
     * Set the EastExtent.
     * WEST is NEGATIVE.
     *
     * @return double
     */

    private void setEastExtent(final double extent)
        {
        this.dblExtentEast = extent;
        }


    /***********************************************************************************************
     * Get the WestExtent.
     * WEST is NEGATIVE.
     *
     * @return double
     */

    public double getWestExtent()
        {
        return (this.dblExtentWest);
        }


    /***********************************************************************************************
     * Set the WestExtent.
     * WEST is NEGATIVE.
     *
     * @return double
     */

    private void setWestExtent(final double extent)
        {
        this.dblExtentWest = extent;
        }


    /***********************************************************************************************
     * Get the SouthExtent.
     *
     * @return double
     */

    public double getSouthExtent()
        {
        return (this.dblExtentSouth);
        }


    /***********************************************************************************************
     * Set the SouthExtent.
     *
     * @return double
     */

    private void setSouthExtent(final double extent)
        {
        this.dblExtentSouth = extent;
        }


    /***********************************************************************************************
     * Get the NorthExtent.
     *
     * @return double
     */

    public double getNorthExtent()
        {
        return (this.dblExtentNorth);
        }


    /***********************************************************************************************
     * Set the NorthExtent.
     *
     * @return double
     */

    private void setNorthExtent(final double extent)
        {
        this.dblExtentNorth = extent;
        }
    }
