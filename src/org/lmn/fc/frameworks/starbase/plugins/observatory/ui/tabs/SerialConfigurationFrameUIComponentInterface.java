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
 * SerialConfigurationFrameUIComponentInterface.
 */

public interface SerialConfigurationFrameUIComponentInterface extends UIComponentPlugin
    {
    // String Resources
    String MSG_SERIAL_CONFIGURATION = "the Serial Configuration diagram";
    String TOOLTIP_REVERT = "Revert to the Configuration before the last Commit";
    String TOOLTIP_COMMIT = "Apply Configuration changes if valid";
    String TOOLTIP_EXPORT = "Export the Serial Configuration diagram";
    String DIALOG_PRINT = "Print Serial Configuration Diagram";
    String DIALOG_EXPORT = "Export Serial Configuration Diagram";
    String MSG_DIAGRAM_PRINTED = "The Diagram has been printed";
    String TITLE_APPLY_CHANGES = "Apply Configuration Changes";


    /***********************************************************************************************
     * Get the Serial Config UI.
     *
     * @return SerialConfigurationUIComponentInterface
     */

    SerialConfigurationUIComponentInterface getSerialConfigUI();


    /***********************************************************************************************
     * Get the Revert button.
     *
     * @return JButton
     */

    JButton getRevertButton();


    /***********************************************************************************************
     * Set the Revert button.
     *
     * @param button
     */

    void setRevertButton(JButton button);


    /***********************************************************************************************
     * Get the Commit button.
     *
     * @return JButton
     */

    JButton getCommitButton();


    /***********************************************************************************************
     * Set the Commit button.
     *
     * @param button
     */

    void setCommitButton(JButton button);


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


    /***********************************************************************************************
     * Get the Export button.
     *
     * @return JButton
     */

    JButton getExportButton();


    /***********************************************************************************************
     * Set the Export button.
     *
     * @param button
     */

    void setExportButton(JButton button);
    }
