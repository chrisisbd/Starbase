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
 * TimeZonesFrameUIComponentInterface.
 */

public interface TimeZonesFrameUIComponentInterface extends UIComponentPlugin
    {
    // String Resources
    String TITLE_TIME_ZONES = "Time Zones";

    String ACTION_NAME_OPEN_FILE = "Open";
    String ACTION_NAME_SAVE_AS_FILE = "SaveAs";
    String ACTION_NAME_CUT = "Cut";
    String ACTION_NAME_COPY = "Copy";
    String ACTION_NAME_PASTE = "Paste";
    String ACTION_NAME_DELETE = "Delete";
    String ACTION_NAME_CLEAR_ALL = "ClearAll";

    String ACTION_TOOLTIP_OPEN_FILE = "Open a File";
    String ACTION_TOOLTIP_SAVE_AS_FILE = "Save the current data in a File";
    String ACTION_TOOLTIP_CUT = "Cut the selection to the clipboard";
    String ACTION_TOOLTIP_COPY = "Copy the selection to the clipboard";
    String ACTION_TOOLTIP_PASTE = "Paste the clipboard to the current insertion point";
    String ACTION_TOOLTIP_DELETE = "Delete the selected bytes";
    String ACTION_TOOLTIP_CLEAR_ALL = "Clear all editor data";

    String DIALOG_PRINT = "Print the Time Zones contents";
    String MSG_TZ = "the Time Zones";
    String MSG_TZ_PRINTED = "The Time Zones has been printed";


    /***********************************************************************************************
     * Get the TimeZones UI.
     *
     * @return TimeZonesUIComponentInterface
     */

    TimeZonesUIComponentInterface getTimeZonesUI();


    /***********************************************************************************************
     * Get the OpenFile button.
     *
     * @return JButton
     */

    JButton getOpenFileButton();


    /***********************************************************************************************
     * Set the OpenFile button.
     *
     * @param button
     */

    void setOpenFileButton(JButton button);


    /***********************************************************************************************
     * Get the SaveAsFile button.
     *
     * @return JButton
     */

    JButton getSaveAsFileButton();


    /***********************************************************************************************
     * Set the SaveAsFile button.
     *
     * @param button
     */

    void setSaveAsFileButton(JButton button);


    /***********************************************************************************************
     * Get the Cut button.
     *
     * @return JButton
     */

    JButton getCutButton();


    /***********************************************************************************************
     * Set the Cut button.
     *
     * @param button
     */

    void setCutButton(JButton button);


    /***********************************************************************************************
     * Get the Copy button.
     *
     * @return JButton
     */

    JButton getCopyButton();


    /***********************************************************************************************
     * Set the Copy button.
     *
     * @param button
     */

    void setCopyButton(JButton button);


    /***********************************************************************************************
     * Get the Paste button.
     *
     * @return JButton
     */

    JButton getPasteButton();


    /***********************************************************************************************
     * Set the Paste button.
     *
     * @param button
     */

    void setPasteButton(JButton button);


    /***********************************************************************************************
     * Get the Delete button.
     *
     * @return JButton
     */

    JButton getDeleteButton();


    /***********************************************************************************************
     * Set the Delete button.
     *
     * @param button
     */

    void setDeleteButton(JButton button);


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
     * Get the ClearAll button.
     *
     * @return JButton
     */

    JButton getClearAllButton();


    /***********************************************************************************************
     * Set the ClearAll button.
     *
     * @param button
     */

    void setClearAllButton(final JButton button);
    }
