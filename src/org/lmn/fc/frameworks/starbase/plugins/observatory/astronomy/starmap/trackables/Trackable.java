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
// The Trackable ApplicationComponent
//------------------------------------------------------------------------------
// Revision History
//
//  12-05-02    LMN created file
//  13-05-02    LMN added the AxisData
//
//------------------------------------------------------------------------------
// Trackable package

package org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.starmap.trackables;

//------------------------------------------------------------------------------
// Imports

import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.common.exceptions.FrameworkException;
import org.lmn.fc.model.plugins.FrameworkPlugin;

import java.awt.geom.Point2D;

//------------------------------------------------------------------------------

public final class Trackable implements FrameworkStrings
    {
    public static final int ALTAZ = 0;          // TrackableType = AltAz
    public static final int RADEC = 1;          // TrackableType = RaDec

    private FrameworkPlugin pluginFramework;            // The application data structure

    private String strResourceKey;            // Application.Object for the Hashtable

    private int intTrackableType;                 // Azimuth-Elevation or RaDec
    private String strTrackableName;              // The public name of the Trackable
    private AxisData horizontalData;            // Horizontal axis motor control data
    private AxisData verticalData;              // Vertical axis motor control data


    public Trackable(final FrameworkPlugin componentmodel,
                     final String objectname,
                     final int antennatype,
                     final String resourcekey)
        {
        try
            {
            this.pluginFramework = componentmodel;

            this.strResourceKey = resourcekey;

            if ((intTrackableType != ALTAZ) && (intTrackableType != RADEC))
                {
                throw new FrameworkException(EXCEPTION_PARAMETER_INVALID);
                }

            this.intTrackableType = antennatype;
            }

        catch(Exception exception)
            {
//            ExceptionLibrary.handleObjectException(exception,
//                                                   this.getPointID(),
//                                                   pluginFramework.COMPONENT_INTERFACE_TASKID,
//                                                   getClass().getName(),
//                                                   ExceptionLibrary.EXCEPTION_CONSTRUCT_ANTENNA,
//                                                   ExceptionLibrary.STATUS_FATAL);
            }
        }


    //--------------------------------------------------------------------------
    // Instance Methods
    //--------------------------------------------------------------------------
    // ApplicationMethod: Create an Axis Controller
    // Check to see if it is Active before use, and Lock during use
    // The Method Name must agree with the corresponding database entry!
    // The Method name is passed in to allow different instances of this
    // ApplicationComponent to use different Methods (no hardcoding!)

//    public final AxisController createAxisController(String strMethodName,
//                                                     int intAxis)
//        {
//        String strTaskHashKey;        // The full path to the task App.Obj.Method
//        TaskData taskData;          // The data in the ComponentModel
//
//        // Build up the full pathname of the controllable Method
//        strTaskHashKey = this.strResourceKey
//                           + pluginFramework.PATHDELIMITER
//                           + strMethodName;
//
//        // Read the TaskData from the ComponentModel
//        taskData = pluginFramework.getMethod(strTaskHashKey);
//
//        // Check the chain of activation controls
//        if ((componentData.isActive()) && (taskData.isActive()))
//            {
//            try
//                {
//                // Mark this Method as Locked, i.e. in use
//                taskData.setLocked(true);
//
//                // The body of the Method
//                if ((intAxis != AxisData.AXIS_HORIZONTAL)
//                    && (intAxis != AxisData.AXIS_VERTICAL))
//                    {
//                    throw new Exception(ExceptionLibrary.EXCEPTION_RANGE_AXIS);
//                    }
//
//                AxisController axisController = new AxisController(pluginFramework,
//                                                                   observatoryModel,
//                                                                   this,
//                                                                   taskData,
//                                                                   intAxis);
//
//                // The Method is free again
//                taskData.setLocked(false);
//
//                return(axisController);
//                }
//
//            catch(Exception exception)
//                {
//                // Take care to unlock in the case of an exception
//                taskData.setLocked(false);
//
//                ExceptionLibrary.handleObjectException(exception,
//                                                       this.getPointID(),
//                                                       taskData.getTaskID(),
//                                                       getClass().getName(),
//                                                       ExceptionLibrary.EXCEPTION_CREATE_AXIS,
//                                                       ExceptionLibrary.STATUS_FATAL);
//
//                }
//            }
//        else
//            {
//            String strInactiveName = "";
//
//            if (!taskData.isActive())
//                {
//                strInactiveName = taskData.getName();
//                }
//
//            // Object inactivity takes precedence
//            if (!componentData.isActive())
//                {
//                strInactiveName = componentData.getName();
//                }
//
//            ExceptionLibrary.handleObjectException(new Exception(ExceptionLibrary.EXCEPTION_INACTIVE_AXIS),
//                                                   this.getPointID(),
//                                                   taskData.getTaskID(),
//                                                   getClass().getName(),
//                                                   strInactiveName + " not active",
//                                                   ExceptionLibrary.STATUS_INFO);
//            }
//
//        // To keep the compiler happy...
//        return(null);
//        }


    //--------------------------------------------------------------------------
    // Utilities
    //--------------------------------------------------------------------------
    // Get the Trackable Type, i.e. either AltAz or RaDec

    public final int getTrackableType()
        {
        return(this.intTrackableType);
        }


    //--------------------------------------------------------------------------
    // Set the public name of the Trackable

    public final void setName(final String name)
        {
        this.strTrackableName = name;
        }


    //--------------------------------------------------------------------------
    // Get the public name of the Trackable

    public final String getName()
        {
        return(this.strTrackableName);
        }


    //--------------------------------------------------------------------------
    // Set the Horizontal AxisData

    public final void setHorizontalData(final AxisData axisdata)
        {
        if (axisdata == null)
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_NULL);
            }

        this.horizontalData = axisdata;
        }


    //--------------------------------------------------------------------------
    // Get the Horizontal AxisData

    public final AxisData getHorizontalData()
        {
        if (this.horizontalData == null)
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_NULL);
            }

        return(this.horizontalData);
        }


    //--------------------------------------------------------------------------
    // Set the Vertical AxisData

    public final void setVerticalData(final AxisData axisdata)
        {
        if (axisdata == null)
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_NULL);
            }

        this.verticalData = axisdata;
        }


    //--------------------------------------------------------------------------
    // Get the Vertical AxisData

    public final AxisData getVerticalData()
        {
        if (this.verticalData == null)
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_NULL);
            }

        return(this.verticalData);
        }


    //--------------------------------------------------------------------------
    // Get the specified Topocentric Coordinates

    public final Point2D.Double getTopocentricCoordinates(final int coordinateid)
        {
        if ((coordinateid < AxisData.COORD_REQUESTED)
            || (coordinateid > AxisData.COORD_ACTUAL))
            {
             throw new FrameworkException(EXCEPTION_PARAMETER_RANGE
                                + " getTopocentricCoordinates [coordinateid=" + coordinateid + "]");
            }

        return(new Point2D.Double(horizontalData.getTopocentricCoordinate(coordinateid),
                                  verticalData.getTopocentricCoordinate(coordinateid)));
        }


    //--------------------------------------------------------------------------
    // Get the specified Celestial Coordinates

    public final Point2D.Double getCelestialCoordinates(final int coordinateid)
        {
        if ((coordinateid < AxisData.COORD_REQUESTED)
            || (coordinateid > AxisData.COORD_ACTUAL))
            {
             throw new FrameworkException(EXCEPTION_PARAMETER_RANGE
                                + " getCelestialCoordinates [coordinateid=" + coordinateid + "]");
            }

        return(new Point2D.Double(horizontalData.getCelestialCoordinate(coordinateid),
                                  verticalData.getCelestialCoordinate(coordinateid)));
        }
    }

