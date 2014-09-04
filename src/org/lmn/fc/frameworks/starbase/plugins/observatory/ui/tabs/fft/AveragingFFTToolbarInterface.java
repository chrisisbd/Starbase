// Copyright 2000, 2001, 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009,
//           2010, 2011, 2012, 2013, 2014
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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.fft;

import org.lmn.fc.common.constants.*;

import javax.swing.*;


/***************************************************************************************************
 * AveragingFFTToolbarInterface.
 */

public interface AveragingFFTToolbarInterface extends FrameworkConstants,
                                                      FrameworkStrings,
                                                      FrameworkMetadata,
                                                      FrameworkRegex,
                                                      FrameworkSingletons,
                                                      ResourceKeys
    {
    // String Resources
    String TOOLBAR_TITLE = "FFT Averager";

    String ACTION_NAME_OPEN_FILE = "Open";
    String ACTION_NAME_PLAY = "Play";
    String ACTION_NAME_PAUSE = "Pause";
    String ACTION_NAME_STOP = "Stop";
    String ACTION_NAME_METADATA = "Metadata";
    String ACTION_NAME_EXPORT = "Export";
    String ACTION_NAME_HELP = "Help";

    String ACTION_TOOLTIP_OPEN_FILE = "Open a File";
    String ACTION_TOOLTIP_PLAY = "Process the signal";
    String ACTION_TOOLTIP_PAUSE = "Pause Processing";
    String ACTION_TOOLTIP_STOP = "Stop Processing";
    String ACTION_TOOLTIP_METADATA = "View the Metadata";
    String ACTION_TOOLTIP_EXPORT = "Save the current data in a File";
    String ACTION_TOOLTIP_HELP = "Show Help for the FFT Averager";

    String DIALOG_PRINT = "Print the Template contents";
    String MSG_TEMPLATE = "the Template";
    String MSG_TEMPLATE_PRINTED = "The Template has been printed";


    /***********************************************************************************************
     * initialise.
     */

    void initialise();


    /***********************************************************************************************
     * Removes all the components from this container.
     * This method also notifies the layout manager to remove the
     * components from this container's layout via the
     * <code>removeLayoutComponent</code> method.
     */

    void removeAll();


    /***********************************************************************************************
     * Get the Title to be displayed on the Toolbar.
     *
     * @return String
     */

    String getTitle();


    /**********************************************************************************************/
    /* Toolbar Buttons                                                                            */
    /***********************************************************************************************
     * Get the OpenFile button.
     *
     * @return JButton
     */

    JButton getOpenFileButton();


    /***********************************************************************************************
     * Get the PlayPause button.
     *
     * @return JButton
     */

    JButton getPlayPauseButton();


    /***********************************************************************************************
     * Get the Stop button.
     *
     * @return JButton
     */

    JButton getStopButton();


    /***********************************************************************************************
     * Get the ProgressBar component.
     *
     * @return JProgressBar
     */

    JProgressBar getProgressBar();


    /***********************************************************************************************
     * Set the ProgressBar component.
     *
     * @param bar
     */

    void setProgressBar(JProgressBar bar);


    /***********************************************************************************************
     * Get the Metadata button.
     *
     * @return JButton
     */

    JButton getMetadataButton();


    /***********************************************************************************************
     * Get the Export button.
     *
     * @return JButton
     */

    JButton getExportButton();


    /***********************************************************************************************
     * Get the PageSetup button.
     *
     * @return JButton
     */

    JButton getPageSetupButton();


    /***********************************************************************************************
     * Get the Print button.
     *
     * @return JButton
     */

    JButton getPrintButton();


    /***********************************************************************************************
     * Get the Help button.
     *
     * @return JButton
     */

    JButton getHelpButton();
    }
