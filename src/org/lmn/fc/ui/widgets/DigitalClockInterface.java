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

package org.lmn.fc.ui.widgets;

import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.common.utilities.time.AstronomicalCalendarInterface;
import org.lmn.fc.common.utilities.time.TimeSystem;

import javax.accessibility.Accessible;
import java.awt.*;
import java.awt.image.ImageObserver;
import java.io.Serializable;



public interface DigitalClockInterface extends IndicatorInterface,
                                               ImageObserver,
                                               MenuContainer,
                                               Serializable,
                                               Accessible,
                                               FrameworkConstants,
                                               FrameworkSingletons,
                                               FrameworkStrings
    {
    void start();


    void stop();


    void setTimeSystem(TimeSystem timesystem);


    TimeSystem getTimeSystem();


    void setUpdatePeriod(int period) throws Exception;


    int getUpdatePeriod();


    AstronomicalCalendarInterface getCalendar();
    }
