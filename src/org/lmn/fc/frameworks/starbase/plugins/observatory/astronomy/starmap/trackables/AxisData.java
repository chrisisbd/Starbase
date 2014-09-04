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

//------------------------------------------------------------------------------
// Axis Data object
//------------------------------------------------------------------------------
// Revision History
//
//  21-03-02    LMN created file
//  20-10-02    LMN modified coordinate storage to suit StarMapCursor Plugins
//
//------------------------------------------------------------------------------
// Trackable package

package org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.starmap.trackables;

import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.common.exceptions.FrameworkException;
import org.lmn.fc.model.datatypes.DegMinSecInterface;


//------------------------------------------------------------------------------

public final class AxisData implements FrameworkStrings
    {
    // These define axis for which AxisData is stored
    public static final int AXIS_HORIZONTAL = 0;
    public static final int AXIS_VERTICAL = 1;

    // These are indexes into the arrays of Coordinates
    public static final int COORD_REQUESTED = 0;
    public static final int COORD_ACTUAL = 1;

    // Used to range-check supplied coordinates
    private static final double TOPOS_HORIZ_MIN = 0.0;
    private static final double TOPOS_HORIZ_MIDPOINT = 180.0;
    private static final double TOPOS_HORIZ_MAX = 360.0;
    private static final double TOPOS_VERT_MIN = 0.0;
    private static final double TOPOS_VERT_MIDPOINT = 45.0;
    private static final double TOPOS_VERT_MAX = 90.0;

    private static final double COSMOS_HORIZ_MIN = 0.0;
    private static final double COSMOS_HORIZ_MIDPOINT = 180.0;
    private static final double COSMOS_HORIZ_MAX = 360.0;
    private static final double COSMOS_VERT_MIN = -90.0;
    private static final double COSMOS_VERT_MIDPOINT = 0.0;
    private static final double COSMOS_VERT_MAX = 90.0;

    //--------------------------------------------------------------------------
    // Instance Constants & Variables
    //--------------------------------------------------------------------------
    // private = 'visible only in this class, visible in all instances'
    // public = 'publically accessible fields'

    public String strAxisName;                  // The name of the axis
    private int intAxisID;                      // HORIZONTAL or VERTICAL

    // The arrays contain Requested and Actual positions and so on.
    public double dblTopocentric [];  // Azimuth/Elevation
    public double dblCelestial [];    // Right Ascension/Declination


    // UNDER DEVELOPMENT...
    public boolean boolServoLock;               // True if the servo is in lock
    public boolean boolZenithLimit;             // True if at the Zenith limit switch
    public boolean boolHorizonLimit;            // True if at the Horizon limit switch
    public boolean boolBrake;                   // True if the brake is on

    public int intMotorIndex;                   // Indexes into the phase table
    public int intPulseRate;                    // The stepping motor pulse rate
    public int intPulseRateSlow;                // The stepping motor pulse rate for slow moves
    public int intPulseRateMax;                 // The maximum pulse rate
    public long longPulseCounter;               // Counts stepping pulses

    private long longShaftEncoderRaw;
    private long longShaftEncoder;

    public int intUpdateRate;                   // The number of times to update the displays
    public String strIndicatorFontName;
    public String strStatusFontName;

    public final boolean[][] boolMotorPhases  =      // The motor phase table
        {
            {true, false, true, false},
            {true, true, false, false},
            {false, true, false, true},
            {true, false, true, true}
        };

    public int intClockTimeSystem;              // Indicates which clock time to use
    public boolean boolSimulatorMode;           // True if we are in Simulate mode


    //--------------------------------------------------------------------------
    // Class Methods
    //--------------------------------------------------------------------------


    //--------------------------------------------------------------------------
    // Constructor
    //--------------------------------------------------------------------------

    public AxisData(final int axisid, final String axisname)
        {
        if ((axisid < AXIS_HORIZONTAL)
            || (axisid > AXIS_VERTICAL))
            {
             throw new FrameworkException(EXCEPTION_OUTOFRANGE
                                + " AxisData [axisid=" + axisid + "]");
            }

        strAxisName = axisname;
        intAxisID = axisid;

        dblTopocentric = new double[COORD_ACTUAL+1];
        dblCelestial = new double[COORD_ACTUAL+1];

        for (int i = COORD_REQUESTED; i < COORD_ACTUAL+1; i++)
            {
            switch (intAxisID)
                {
                case 0:                     // HORIZONTAL
                    {
                    dblTopocentric[i] = TOPOS_HORIZ_MIDPOINT;
                    dblCelestial[i] = COSMOS_HORIZ_MIDPOINT;
                    break;
                    }

                case 1:                     // VERTICAL
                    {
                    dblTopocentric[i] = TOPOS_VERT_MIDPOINT;
                    dblCelestial[i] = COSMOS_VERT_MIDPOINT;
                    break;
                    }

                default:
                    {
                    //System.out.println("ERROR in Axis Data switch()");
                    }
                }
            }
        }

    //--------------------------------------------------------------------------
    // Instance Methods
    //--------------------------------------------------------------------------
    // Get the specified Topocentric Coordinate

    public final double getTopocentricCoordinate(final int coordinateid)
        {
        if ((coordinateid < COORD_REQUESTED)
            || (coordinateid > COORD_ACTUAL))
            {
             throw new FrameworkException(EXCEPTION_OUTOFRANGE
                                + " getTopocentricCoordinate [coordinateid=" + coordinateid + "]");
            }

        return(dblTopocentric[coordinateid]);
        }


    //--------------------------------------------------------------------------
    // Set the specified Topocentric Coordinate

    public final void setTopocentricCoordinate(final int coordinateid,
                                               final double coordinate)
        {
        switch (intAxisID)
            {
            case 0:                     // HORIZONTAL
                {
                if ((coordinate < TOPOS_HORIZ_MIN)
                    || (coordinate > TOPOS_HORIZ_MAX))
                    {
                    throw new FrameworkException(EXCEPTION_OUTOFRANGE
                                        + " setTopocentricCoordinate [coordinate=" + coordinate + "]");
                    }
                break;
                }

            case 1:                     // VERTICAL
                {
                if ((coordinate < TOPOS_VERT_MIN)
                    || (coordinate > TOPOS_VERT_MAX))
                    {
                    throw new FrameworkException(EXCEPTION_OUTOFRANGE
                                        + " setTopocentricCoordinate [coordinate=" + coordinate + "]");
                    }
                break;
                }

            default:
                {
                throw new FrameworkException(EXCEPTION_OUTOFRANGE
                                    + " setTopocentricCoordinate [axisid=" + intAxisID + "]");
                }
            }

        if ((coordinateid < COORD_REQUESTED)
            || (coordinateid > COORD_ACTUAL))
            {
             throw new FrameworkException(EXCEPTION_OUTOFRANGE
                                + " setTopocentricCoordinate [coordinateid=" + coordinateid + "]");
            }

        // It's now safe to set the coordinate
        dblTopocentric[coordinateid] = coordinate;
        }


    //--------------------------------------------------------------------------
    // Get the specified Celestial Coordinate

    public final double getCelestialCoordinate(final int coordinateid)
        {
        if ((coordinateid < COORD_REQUESTED)
            || (coordinateid > COORD_ACTUAL))
            {
             throw new FrameworkException(EXCEPTION_OUTOFRANGE
                                + " getCelestialCoordinate [coordinateid=" + coordinateid + "]");
            }

        return(dblCelestial[coordinateid]);
        }


    //--------------------------------------------------------------------------
    // Set the specified Celestial Coordinate

    public final void setCelestialCoordinate(final int coordinateid,
                                             final double coordinate)
        {
        switch (intAxisID)
            {
            case 0:                     // HORIZONTAL
                {
                if ((coordinate < COSMOS_HORIZ_MIN)
                    || (coordinate > COSMOS_HORIZ_MAX))
                    {
                    throw new FrameworkException(EXCEPTION_OUTOFRANGE
                                        + " setCelestialCoordinate [coordinate=" + coordinate + "]");
                    }
                break;
                }

            case 1:                     // VERTICAL
                {
                if ((coordinate < COSMOS_VERT_MIN)
                    || (coordinate > COSMOS_VERT_MAX))
                    {
                    throw new FrameworkException(EXCEPTION_OUTOFRANGE
                                        + " setCelestialCoordinate [coordinate=" + coordinate + "]");
                    }
                break;
                }

            default:
                {
                throw new FrameworkException(EXCEPTION_OUTOFRANGE
                                    + " setCelestialCoordinate [axisid=" + intAxisID + "]");
                }
            }

        if ((coordinateid < COORD_REQUESTED)
            || (coordinateid > COORD_ACTUAL))
            {
             throw new FrameworkException(EXCEPTION_OUTOFRANGE
                                + " setCelestialCoordinate [coordinateid=" + coordinateid + "]");
            }

        // It's now safe to set the coordinate
        dblCelestial[coordinateid] = coordinate;
        }


    //--------------------------------------------------------------------------
    // Hardware Interfaces
    //--------------------------------------------------------------------------
    // Read the shaft encoder for this axis

    public final void readShaftEncoder()
        {
        // Read the interface

        //..............

        longShaftEncoderRaw = 0;
        }


    //--------------------------------------------------------------------------
    // Get the raw Shaft Encoder reading

    public final long getShaftEncoderRaw()
        {
        return(longShaftEncoderRaw);
        }


    //--------------------------------------------------------------------------
    // Get the Shaft Encoder reading as a DegMinSec object

    public final DegMinSecInterface getShaftEncoder()
        {
        // Adjust for gear ratios etc.

//        return(new DegMinSec((double)longShaftEncoderRaw));
        return(null);
        }


    //--------------------------------------------------------------------------
    // Events
    //--------------------------------------------------------------------------

  }

//------------------------------------------------------------------------------
// End of File

