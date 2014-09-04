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

//-----------------------------------------------------------------------------
// SunTimesApplet.java
//
// (c) 2004 Jonathan Stott
//
// Created on 12-Apr-2004
//
// 0.2 - 12 Apr 2004
//  - Initial Version
//-----------------------------------------------------------------------------

package uk.me.jstott.sun;

import javax.swing.*;
import java.awt.*;

/**
 * A simple applet to allow calculation of sunrise, sunset, etc. times using the
 * uk.me.jstott.sun.Sun class.
 *
 * For more information on using this class, look at
 * http://www.jstott.me.uk/jsuntimes/
 *
 * @author Jonathan Stott
 * @version 0.4
 */
public class SunTimesApplet extends JApplet {

	/**
	 *
	 */
	private static final long serialVersionUID = -7989425564676903148L;

	SunTimesPanel panel = new SunTimesPanel();

	/**
	 * Initialise the applet
	 *
	 * @see java.applet.Applet#init()
	 */
	public void init() {
		getContentPane().add(panel, BorderLayout.CENTER);
	}
}
