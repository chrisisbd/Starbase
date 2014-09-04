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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.jython;


import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.RSyntaxUtilities;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryUIInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.JythonEditorUIComponentInterface;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.ui.components.UIComponent;

import java.awt.*;


/***********************************************************************************************
 * A UIComponent to show the JythonEditor.
 * This implementation is a simple wrapper around RSyntaxTextArea.
 * See: http://fifesoft.com/rsyntaxtextarea
 */

public final class JythonEditorUIComponent extends UIComponent
                                           implements JythonEditorUIComponentInterface
    {
    private static final long serialVersionUID = -2065831140983759773L;

    // Injections
    private final TaskPlugin pluginTask;
    private final ObservatoryUIInterface observatoryUI;
    private final ObservatoryInstrumentInterface hostInstrument;
    private final FontInterface pluginFont;
    private final ColourInterface pluginColour;
    private final String strResourceKey;

    // UI
    private RSyntaxTextArea textArea;
    private RTextScrollPane scrollPane;


    /***********************************************************************************************
     * Construct a JythonEditorUIComponent.
     *
     * @param task
     * @param hostui
     * @param hostinstrument
     * @param font
     * @param colour
     * @param resourcekey
     * @param debug
     */

    public JythonEditorUIComponent(final TaskPlugin task,
                                   final ObservatoryUIInterface hostui,
                                   final ObservatoryInstrumentInterface hostinstrument,
                                   final FontInterface font,
                                   final ColourInterface colour,
                                   final String resourcekey,
                                   final boolean debug)
        {
        // UIComponent has a BorderLayout
        super();

        // Injections
        this.pluginTask = task;
        this.observatoryUI = hostui;
        this.hostInstrument = hostinstrument;
        this.pluginFont = font;
        this.pluginColour = colour;
        this.strResourceKey = resourcekey;
        setDebug(debug);

        // UI
        this.textArea = null;
        this.scrollPane = null;
        }


    /***********************************************************************************************
     /* UI State                                                                                  */
    /***********************************************************************************************
     * Initialise this UIComponent.
     */

    public final void initialiseUI()
        {
        final String SOURCE = "JythonEditorUIComponent.initialiseUI() ";

        LOGGER.debug(isDebug(), SOURCE);

        super.initialiseUI();

        removeAll();

        // This is the only creation of RSyntaxTextArea
        setTextArea(new RSyntaxTextArea(20, 60));

        getTextArea().setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_PYTHON);
        getTextArea().setCodeFoldingEnabled(true);
        getTextArea().setAntiAliasingEnabled(true);

        scrollPane = new RTextScrollPane(getTextArea());
        getScrollPane().setFoldIndicatorEnabled(true);

        // The gutter, or null if the text area is not in an RTextScrollPane
        RSyntaxUtilities.getGutter(getTextArea()).setBackground(DEFAULT_COLOUR_TAB_BACKGROUND.getColor());

        // The host UIComponent uses BorderLayout
        add(getScrollPane(), BorderLayout.CENTER);
        }


    /***********************************************************************************************
     * Run this UIComponent.
     */

    public void runUI()
        {
        final String SOURCE = "JythonEditorUIComponent.runUI() ";

        LOGGER.debug(isDebug(), SOURCE);

        super.runUI();
        }


    /***********************************************************************************************
     * Dispose of all components of this UIComponent.
     */

    public void disposeUI()
        {
        final String SOURCE = "JythonEditorUIComponent.disposeUI() ";

        LOGGER.debug(isDebug(), SOURCE);

        super.disposeUI();
        }


    /***********************************************************************************************
     * Get the TextArea.
     *
     * @return RSyntaxTextArea
     */

    public RSyntaxTextArea getTextArea()
        {
        return (this.textArea);
        }


    /***********************************************************************************************
     * Set the TextArea.
     *
     * @param textarea
     */

    private void setTextArea(final RSyntaxTextArea textarea)
        {
        this.textArea = textarea;
        }


    /***********************************************************************************************
     * Get the ScrollPane.
     *
     * @return RTextScrollPane
     */

    private RTextScrollPane getScrollPane()
        {
        return (this.scrollPane);
        }


    /**********************************************************************************************/
    /* Injections                                                                                 */
    /***********************************************************************************************
     * Get the Task on which this Report is based.
     *
     * @return TaskData
     */

    private TaskPlugin getTask()
        {
        return (this.pluginTask);
        }


    /***********************************************************************************************
     * Get the host ObservatoryUI.
     *
     * @return ObservatoryUIInterface
     */

    public ObservatoryUIInterface getObservatoryUI()
        {
        return (this.observatoryUI);
        }


    /***********************************************************************************************
     * Get the ObservatoryInstrument to which this UIComponent is attached.
     *
     * @return ObservatoryInstrumentInterface
     */

    private ObservatoryInstrumentInterface getHostInstrument()
        {
        return (this.hostInstrument);
        }


    /***********************************************************************************************
     * Get the FontDataType.
     *
     * @return FontPlugin
     */

    private FontInterface getFontData()
        {
        return (this.pluginFont);
        }


    /***********************************************************************************************
     * Get the ColourDataType.
     *
     * @return ColourPlugin
     */

    private ColourInterface getColourData()
        {
        return (this.pluginColour);
        }


    /***********************************************************************************************
     * Get the ResourceKey for the Report.
     *
     * @return String
     */

    private String getResourceKey()
        {
        return (this.strResourceKey);
        }
    }