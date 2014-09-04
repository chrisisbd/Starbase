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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs;

import org.lmn.fc.ui.UIComponentPlugin;

import javax.swing.*;


/***************************************************************************************************
 * AudioExplorerUIComponentInterface.
 */

public interface AudioExplorerFrameUIComponentInterface extends UIComponentPlugin
    {
    // String Resources
    String AUDIO_EXPLORER_NAME = "Audio Explorer";

    String ACTION_NAME_RESCAN = "Rescan for Audio devices";

    String ACTION_TOOLTIP_RESCAN = "Rescan for Audio devices";

    String DIALOG_PRINT = "Print the Audio Explorer contents";
    String MSG_AUDIO_EXPLORER = "the Audio Explorer";
    String MSG_AUDIO_EXPLORER_PRINTED = "The Audio Explorer has been printed";


    /***********************************************************************************************
     * Get the AudioExplorer UI.
     *
     * @return AudioExplorerUIComponentInterface
     */

    AudioExplorerUIComponentInterface getAudioExplorerUI();


    /***********************************************************************************************
     * Get the Rescan button.
     *
     * @return JButton
     */

    JButton getRescanButton();


    /***********************************************************************************************
     * Set the Rescan button.
     *
     * @param button
     */

    void setRescanButton(JButton button);


    /***********************************************************************************************
     * Get the PageSetup button.
     *
     * @return JButton
     */

    JButton getPageSetupButton();


    /***********************************************************************************************
     * Set the PageSetup button.
     *
     * @param button
     */

    void setPageSetupButton(JButton button);


    /***********************************************************************************************
     * Get the Print button.
     *
     * @return JButton
     */

    JButton getPrintButton();


    /***********************************************************************************************
     * Set the Print button.
     *
     * @param button
     */

    void setPrintButton(JButton button);
    }
