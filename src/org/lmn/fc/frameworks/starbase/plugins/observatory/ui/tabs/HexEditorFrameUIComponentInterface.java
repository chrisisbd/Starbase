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

import org.fife.ui.hex.event.HexEditorListener;
import org.fife.ui.hex.event.SelectionChangedListener;
import org.lmn.fc.ui.UIComponentPlugin;

import javax.swing.*;


/***************************************************************************************************
 * HexEditorFrameUIComponentInterface.
 */

public interface HexEditorFrameUIComponentInterface extends UIComponentPlugin,
                                                            HexEditorListener,
                                                            SelectionChangedListener
    {
    // String Resources
    String HEX_EDITOR_NAME = "Hex Editor";

    String ACTION_NAME_OPEN_FILE = "Open";
    String ACTION_NAME_SAVE_AS_FILE = "SaveAs";
    String ACTION_NAME_CUT = "Cut";
    String ACTION_NAME_COPY = "Copy";
    String ACTION_NAME_PASTE = "Paste";
    String ACTION_NAME_DELETE = "Delete";
    String ACTION_NAME_CLEAR_ALL = "ClearAll";
    String ACTION_NAME_UNDO = "Undo";
    String ACTION_NAME_REDO = "Redo";

    String ACTION_TOOLTIP_OPEN_FILE = "Open a File to edit";
    String ACTION_TOOLTIP_SAVE_AS_FILE = "Save the current data in a File";
    String ACTION_TOOLTIP_CUT = "Cut the selected bytes to the clipboard";
    String ACTION_TOOLTIP_COPY = "Copy the selected bytes to the clipboard";
    String ACTION_TOOLTIP_PASTE = "Paste the bytes on the clipboard to the current insertion point";
    String ACTION_TOOLTIP_DELETE = "Delete the selected bytes";
    String ACTION_TOOLTIP_CLEAR_ALL = "Clear all editor data";
    String ACTION_TOOLTIP_UNDO = "Undo the last editing action";
    String ACTION_TOOLTIP_REDO = "Redo the last editing action";

    String DIALOG_PRINT = "Print the Hex Editor contents";
    String MSG_HEX_EDITOR = "the Hex Editor";
    String MSG_HEX_EDITOR_PRINTED = "The Hex Editor has been printed";

    String EMPTY_RECORD = "\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0";


    /***********************************************************************************************
     * Get the HexEditor UI.
     *
     * @return HexEditorUIComponentInterface
     */

    HexEditorUIComponentInterface getHexEditorUI();


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
     * Get the Undo button.
     *
     * @return JButton
     */

    JButton getUndoButton();


    /***********************************************************************************************
     * Set the Undo button.
     *
     * @param button
     */

    void setUndoButton(JButton button);


    /***********************************************************************************************
     * Get the Redo button.
     *
     * @return JButton
     */

    JButton getRedoButton();


    /***********************************************************************************************
     * Set the Redo button.
     *
     * @param button
     */

    void setRedoButton(JButton button);


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
