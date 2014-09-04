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
// SunTimesPanel.java
//
// (c) 2004 Jonathan Stott
//
// Created on 12-Apr-2004
//
// 0.2 - 12 Apr 2004
//  - Initial Version
//-----------------------------------------------------------------------------

package uk.me.jstott.sun;

import uk.me.jstott.coordconv.LatitudeLongitude;
import uk.me.jstott.util.JulianDateConverter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * Panel to provide all the relevant text entry fields in order to allow the
 * calculation of sunrise, sunset, etc. for a particular latitude and longitude.
 * The fields are initially populated with today's date and the latitude and
 * longitude for Canterbury, England.
 *
 * For more information on using this class, look at
 * http://www.jstott.me.uk/jsuntimes/
 *
 * @author Jonathan Stott
 * @version 0.4
 */
public class SunTimesPanel extends JPanel {

	/**
	 *
	 */
	private static final long serialVersionUID = -6875061905078672943L;

	// Input fields and labels
	private JTextField northingDegrees = new JTextField("51");
	private JTextField northingMinutes = new JTextField("16");
	private JTextField northingSeconds = new JTextField("48");

	private JTextField eastingDegrees = new JTextField("1");
	private JTextField eastingMinutes = new JTextField("4");
	private JTextField eastingSeconds = new JTextField("48");

	private JLabel northingLabel = new JLabel("Northing");
	private JLabel eastingLabel = new JLabel("Easting");
	private JLabel northingDegreesLabel = new JLabel("�");
	private JLabel northingMinutesLabel = new JLabel("'");
	private JLabel northingSecondsLabel = new JLabel("\"");
	private JLabel eastingDegreesLabel = new JLabel("�");
	private JLabel eastingMinutesLabel = new JLabel("'");
	private JLabel eastingSecondsLabel = new JLabel("\"");

	private JLabel dateLabel = new JLabel("Date");
	private Calendar date = Calendar.getInstance();
	private JTextField dateDay = new JTextField(Integer.toString(date
			.get(Calendar.DAY_OF_MONTH)));
	private JComboBox dateMonth = new JComboBox(new String[] { "January",
			"February", "March", "April", "May", "June", "July", "August",
			"September", "October", "November", "December" });
	private JTextField dateYear = new JTextField(Integer.toString(date
			.get(Calendar.YEAR)));

	private JLabel timeZoneLabel = new JLabel("Time Zone");
	private JComboBox timeZones;

	private JCheckBox daylightSavingsTime = new JCheckBox(
			"Daylight Savings Time", true);

	// Go button
	private JButton goButton = new JButton("Go");

	// Output fields
	private JTextField morningAstroTwilight = new JTextField(10);
	private JTextField morningNauticalTwilight = new JTextField(10);
	private JTextField morningCivilTwilight = new JTextField(10);
	private JTextField sunrise = new JTextField(10);
	private JTextField sunset = new JTextField(10);
	private JTextField eveningCivilTwilight = new JTextField(10);
	private JTextField eveningNauticalTwilight = new JTextField(10);
	private JTextField eveningAstroTwilight = new JTextField(10);

	// Output labels
	private JLabel morningAstoTwilightLabel = new JLabel(
			"Morning Astronomical Twilight");
	private JLabel morningNauticalTwilightLabel = new JLabel(
			"Morning Nautical Twilight");
	private JLabel morningCivilTwilightLabel = new JLabel(
			"Morning Civil Twilight");
	private JLabel sunriseLabel = new JLabel("Sunrise");
	private JLabel sunsetLabel = new JLabel("Sunset");
	private JLabel eveningCivilTwilightLabel = new JLabel(
			"Evening Civil Twilight");
	private JLabel eveningNauticalTwilightLabel = new JLabel(
			"Evening Nautical Twilight");
	private JLabel eveningAstoTwilightLabel = new JLabel(
			"Evening Astronomical Twilight");

	/**
	 * Construct a SunTimesPanel object
	 */
	public SunTimesPanel() {
		super(new BorderLayout());

		JPanel inputPanel = new JPanel(new GridLayout(5, 2));
		inputPanel.add(northingLabel);
		JPanel northingPanel = new JPanel(new GridLayout(1, 6));
		northingPanel.add(northingDegrees);
		northingPanel.add(northingDegreesLabel);
		northingPanel.add(northingMinutes);
		northingPanel.add(northingMinutesLabel);
		northingPanel.add(northingSeconds);
		northingPanel.add(northingSecondsLabel);
		inputPanel.add(northingPanel);
		inputPanel.add(eastingLabel);
		JPanel eastingPanel = new JPanel(new GridLayout(1, 6));
		eastingPanel.add(eastingDegrees);
		eastingPanel.add(eastingDegreesLabel);
		eastingPanel.add(eastingMinutes);
		eastingPanel.add(eastingMinutesLabel);
		eastingPanel.add(eastingSeconds);
		eastingPanel.add(eastingSecondsLabel);
		inputPanel.add(eastingPanel);
		inputPanel.add(dateLabel);
		JPanel datePanel = new JPanel(new GridLayout(1, 3));
		datePanel.add(dateDay);
		dateMonth.setSelectedIndex(date.get(Calendar.MONTH));
		datePanel.add(dateMonth);
		datePanel.add(dateYear);
		inputPanel.add(datePanel);
		inputPanel.add(timeZoneLabel);
		String[] timeZoneList = TimeZone.getAvailableIDs();
		Arrays.sort(timeZoneList);
		timeZones = new JComboBox(timeZoneList);
		inputPanel.add(timeZones);
		inputPanel.add(new JPanel());
		inputPanel.add(daylightSavingsTime);

		JPanel outputPanel = new JPanel(new GridLayout(8, 2));
		outputPanel.add(morningAstoTwilightLabel);
		outputPanel.add(morningAstroTwilight);
		outputPanel.add(morningNauticalTwilightLabel);
		outputPanel.add(morningNauticalTwilight);
		outputPanel.add(morningCivilTwilightLabel);
		outputPanel.add(morningCivilTwilight);
		outputPanel.add(sunriseLabel);
		outputPanel.add(sunrise);
		outputPanel.add(sunsetLabel);
		outputPanel.add(sunset);
		outputPanel.add(eveningCivilTwilightLabel);
		outputPanel.add(eveningCivilTwilight);
		outputPanel.add(eveningNauticalTwilightLabel);
		outputPanel.add(eveningNauticalTwilight);
		outputPanel.add(eveningAstoTwilightLabel);
		outputPanel.add(eveningAstroTwilight);

		add(inputPanel, BorderLayout.NORTH);
		add(goButton, BorderLayout.SOUTH);
		add(outputPanel, BorderLayout.CENTER);

		// Listen for clicks on the Go button
		goButton.addActionListener(new ActionListener() {
			/**
			 * Respond to clicks on the Go button
			 *
			 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
			 */
			public void actionPerformed(ActionEvent e) {
				// Get all the values from the various text fields
				int nDay = Integer.parseInt(dateDay.getText());
				int nMonth = dateMonth.getSelectedIndex();
				int nYear = Integer.parseInt(dateYear.getText());
				Calendar cal = Calendar.getInstance();
				cal.set(Calendar.DAY_OF_MONTH, nDay);
				cal.set(Calendar.MONTH, nMonth);
				cal.set(Calendar.YEAR, nYear);
				double jd = JulianDateConverter.dateToJulian(cal);
				int nDeg = Integer.parseInt(northingDegrees.getText());
				int nMin = Integer.parseInt(northingMinutes.getText());
				int nSec = Integer.parseInt(northingSeconds.getText());
				int eDeg = Integer.parseInt(eastingDegrees.getText());
				int eMin = Integer.parseInt(eastingMinutes.getText());
				int eSec = Integer.parseInt(eastingSeconds.getText());

				// Construct the LatitudeLongitude and TimeZone objects
				LatitudeLongitude ll = new LatitudeLongitude(nDeg, nMin, nSec,
						eDeg, eMin, eSec);
				TimeZone timeZone = TimeZone.getTimeZone((String) timeZones
						.getSelectedItem());
				boolean dst = daylightSavingsTime.isSelected();

				// Calculate all the relevant Times
				Time mornATTime = SunFixed.morningAstronomicalTwilightTime(jd, ll,
                                                                           timeZone, dst);
				Time mornNTTime = SunFixed.morningNauticalTwilightTime(jd, ll,
                                                                       timeZone, dst);
				Time mornCTTime = SunFixed.morningCivilTwilightTime(jd, ll,
                                                                    timeZone, dst);
				Time sunriseTime = SunFixed.sunriseTime(jd, ll, timeZone, dst);
				Time sunsetTime = SunFixed.sunsetTime(jd, ll, timeZone, dst);
				Time evenCTTime = SunFixed.eveningCivilTwilightTime(jd, ll,
                                                                    timeZone, dst);
				Time evenNTTime = SunFixed.eveningNauticalTwilightTime(jd, ll,
                                                                       timeZone, dst);
				Time evenATTime = SunFixed.eveningAstronomicalTwilightTime(jd, ll,
                                                                           timeZone, dst);

				// Display the output in the output fields
				morningAstroTwilight.setText(mornATTime.toString());
				morningNauticalTwilight.setText(mornNTTime.toString());
				morningCivilTwilight.setText(mornCTTime.toString());
				sunrise.setText(sunriseTime.toString());
				sunset.setText(sunsetTime.toString());
				eveningAstroTwilight.setText(evenATTime.toString());
				eveningNauticalTwilight.setText(evenNTTime.toString());
				eveningCivilTwilight.setText(evenCTTime.toString());
			}
		});
	}
}
