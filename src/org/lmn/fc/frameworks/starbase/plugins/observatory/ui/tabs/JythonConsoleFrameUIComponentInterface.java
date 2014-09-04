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
 * JythonConsoleFrameUIComponentInterface.
 */

public interface JythonConsoleFrameUIComponentInterface extends UIComponentPlugin
    {
    // String Resources
    String DIALOG_PRINT                     = "Print the Jython Console contents";
    String JYTHON_CONSOLE_NAME              = "Jython Console";
    String MSG_DIAGRAM_PRINTED              = "The Jython Console has been printed";
    String MSG_JYTHON_CONSOLE               = "the Jython Console";
    String MSG_RUNNING_0                    = "The Jython Interpreter is already running.";
    String MSG_RUNNING_1                    = "Please stop execution with Reset before running another script.";
    String TITLE_DIALOG_JYTHON              = "Jython Interpreter";
    String TOOLTIP_EXECUTE_JYTHON_FILE      = "Execute a Jython script or module from a file";
    String TOOLTIP_EXECUTE_JYTHON_SCRIPT    = "Execute the Jython script from the Editor tab";
    String TOOLTIP_EXPORT_JYTHON            = "Export the Jython Console to a text file";
    String TOOLTIP_RESET_JYTHON             = "Reset the Jython Console";


    /***********************************************************************************************
     * Get the associated JythonEditorFrame.
     *
     * @return UIComponentPlugin
     */

    UIComponentPlugin getJythonEditorFrame();


    /***********************************************************************************************
     * Set the associated JythonEditorFrame.
     *
     * @param jythoneditor
     */

    void setJythonEditorFrame(UIComponentPlugin jythoneditor);


    /***********************************************************************************************
     * Get the JythonConsole UI.
     *
     * @return JythonConsoleUIComponentInterface
     */

    JythonConsoleUIComponentInterface getJythonConsoleUI();


    /***********************************************************************************************
     * Get the Execute Script button.
     *
     * @return JButton
     */

    JButton getExecuteScriptButton();


    /***********************************************************************************************
     * Set the Execute Script button.
     *
     * @param button
     */

    void setExecuteScriptButton(JButton button);


    /***********************************************************************************************
     * Get the Execute File button.
     *
     * @return JButton
     */

    JButton getExecuteFileButton();


    /***********************************************************************************************
     * Set the Execute File button.
     *
     * @param button
     */

    void setExecuteFileButton(JButton button);


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
