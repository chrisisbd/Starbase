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
 * JythonEditorFrameUIComponentInterface.
 */

public interface JythonEditorFrameUIComponentInterface extends UIComponentPlugin
    {
    // String Resources
    String DIALOG_EXPORT = "Export the Jython Editor contents";
    String DIALOG_PRINT = "Print the Jython Editor contents";
    String JYTHON_EDITOR_NAME = "Jython Editor";
    String MSG_EDITOR_PRINTED = "The Jython Editor has been printed";
    String MSG_JYTHON_EDITOR = "the Jython Editor";
    String TOOLTIP_OPENFILE_EDITOR = "Load the Jython Editor contents from a text file";
    String TOOLTIP_SAVEAS_EDITOR = "Save the Jython Editor contents to a text file";
    String TOOLTIP_RESET_EDITOR = "Reset the Jython Editor";

    String ACTION_TOOLTIP_OPEN_FILE = "Open a File to edit";
    String ACTION_TOOLTIP_SAVE_AS_FILE = "Save the current data in a File";


    /***********************************************************************************************
     * Get the associated JythonConsoleFrame.
     *
     * @return UIComponentPlugin
     */

    UIComponentPlugin getJythonConsoleFrame();


    /***********************************************************************************************
     * Set the associated JythonConsoleFrame.
     *
     * @param jythonconsole
     */

    void setJythonConsoleFrame(UIComponentPlugin jythonconsole);


    /***********************************************************************************************
     * Get the JythonEditor UI.
     *
     * @return JythonEditorUIComponentInterface
     */

    JythonEditorUIComponentInterface getJythonEditorUI();


    /***********************************************************************************************
     * Get the Syntax Highlight combo box.
     *
     * @return JComboBox
     */

    JComboBox getSyntaxHighlightCombo();


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
     * Get the SaveAs button.
     *
     * @return JButton
     */

    JButton getSaveAsButton();


    /***********************************************************************************************
     * Set the SaveAs button.
     *
     * @param button
     */

    void setSaveAsButton(JButton button);


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
     * Get the Reset button.
     *
     * @return JButton
     */

    JButton getResetButton();


    /***********************************************************************************************
     * Set the Reset button.
     *
     * @param button
     */

    void setResetButton(JButton button);
    }
