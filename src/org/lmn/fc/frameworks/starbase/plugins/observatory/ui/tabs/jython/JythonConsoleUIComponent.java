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


import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryUIInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.JythonConsoleInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.JythonConsoleUIComponentInterface;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.ui.components.TextLineNumber;
import org.lmn.fc.ui.components.UIComponent;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;


/***********************************************************************************************
 * A UIComponent to show the JythonConsole.
 */

public final class JythonConsoleUIComponent extends UIComponent
                                            implements JythonConsoleUIComponentInterface
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
    private JythonConsoleInterface jythonConsole;
    private JScrollPane scrollPane;


    /***********************************************************************************************
     * Construct a JythonConsoleUIComponent.
     *
     * @param task
     * @param hostui
     * @param hostinstrument
     * @param font
     * @param colour
     * @param resourcekey
     * @param debug
     */

    public JythonConsoleUIComponent(final TaskPlugin task,
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
        this.jythonConsole = null;
        this.scrollPane= null;
        }


    /***********************************************************************************************
     /* UI State                                                                                  */
    /***********************************************************************************************
     * Initialise this UIComponent.
     */

    public final void initialiseUI()
        {
        final String SOURCE = "JythonConsoleUIComponent.initialiseUI() ";
        final TextLineNumber textLineNumber;

        LOGGER.debug(isDebug(), SOURCE);

        super.initialiseUI();

        removeAll();

        // This is the only creation of JythonConsole
        setJythonConsole(new JythonConsole());

        getJythonConsole().setBackground(Color.white);
        getJythonConsole().setForeground(UIComponent.DEFAULT_COLOUR_TEXT.getColor());
        getJythonConsole().setFont(UIComponent.DEFAULT_FONT_MONOSPACED.getFont());
        getJythonConsole().setTabSize(4);
        getJythonConsole().setToolTipText(TOOLTIP_COPY);
        getJythonConsole().setMargin(new Insets(10, 10, 10, 10));

        scrollPane = new JScrollPane((Component)getJythonConsole(),
                                     JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                     JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        // Add line numbers for convenience
        textLineNumber = new TextLineNumber((JTextComponent)getJythonConsole());
        getJScrollPane().setRowHeaderView(textLineNumber);

        // The host UIComponent uses BorderLayout
        add(getJScrollPane(), BorderLayout.CENTER);
        }


    /***********************************************************************************************
     * Run this UIComponent.
     */

    public void runUI()
        {
        final String SOURCE = "JythonConsoleUIComponent.runUI() ";

        LOGGER.debug(isDebug(), SOURCE);

        super.runUI();
        }


    /***********************************************************************************************
     * Dispose of all components of this UIComponent.
     */

    public void disposeUI()
        {
        final String SOURCE = "JythonConsoleUIComponent.disposeUI() ";

        LOGGER.debug(isDebug(), SOURCE);

        super.disposeUI();
        }


    /***********************************************************************************************
     * Get the JythonConsole.
     *
     * @return JythonConsoleInterface
     */

    public JythonConsoleInterface getJythonConsole()
        {
        return (this.jythonConsole);
        }


    /***********************************************************************************************
     * Set the JythonConsole.
     *
     * @param jythonconsole
     */

    private void setJythonConsole(final JythonConsoleInterface jythonconsole)
        {
        this.jythonConsole = jythonconsole;
        }


    /***********************************************************************************************
     * Get the JScrollPane.
     *
     * @return JScrollPane
     */

    private JScrollPane getJScrollPane()
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