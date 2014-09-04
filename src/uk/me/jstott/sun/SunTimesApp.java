//-----------------------------------------------------------------------------
// SunTimesApp.java
//
// (c) 2004 Jonathan Stott
//
// Created on 13-Apr-2004
//
// 0.2 - 13 Apr 2004
//  - Initial Version
//-----------------------------------------------------------------------------

package uk.me.jstott.sun;

import javax.swing.*;

/**
 * Application to allow the calculation of sunrise, sunset, etc. times using the
 * uk.me.jstott.sun.Sun class.
 *
 * For more information on using this class, look at
 * http://www.jstott.me.uk/jsuntimes/
 *
 * @author Jonathan Stott
 * @version 0.4
 */
public class SunTimesApp extends JFrame {

	/**
	 *
	 */
	private static final long serialVersionUID = 1685281846205038914L;

	/**
	 * Main method
	 *
	 * @param args
	 */
	public static void main(String[] args) {
		new SunTimesApp();
	}

	/**
	 * Constructor
	 */
	public SunTimesApp() {
		super("jSunTimes Example");
		SunTimesPanel panel = new SunTimesPanel();
		getContentPane().add(panel);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setVisible(true);
		pack();
	}
}
