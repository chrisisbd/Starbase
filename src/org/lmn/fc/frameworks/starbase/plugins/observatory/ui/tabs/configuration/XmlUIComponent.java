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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.configuration;

import org.lmn.fc.common.exceptions.FrameworkException;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryClockInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.XmlUIComponentInterface;
import org.lmn.fc.ui.components.TextLineNumber;
import org.lmn.fc.ui.components.UIComponent;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;


/***********************************************************************************************
 * A UIComponent to show some XML.
 */

public class XmlUIComponent extends UIComponent
                            implements XmlUIComponentInterface
    {
    // Injections
    private final ObservatoryInstrumentInterface hostInstrument;
    private String strXML;
    private final String strResourceKey;

    // UI
    private final JTextArea textArea;
    private JScrollPane scrollPane;


    /***********************************************************************************************
     * Construct an XmlUIComponent.
     *
     * @param hostinstrument
     * @param xml
     * @param hostresourcekey
     */

    public XmlUIComponent(final ObservatoryInstrumentInterface hostinstrument,
                          final String xml,
                          final String hostresourcekey)
        {
        super();

        if ((xml != null)
            && (!EMPTY_STRING.equals(xml))
            && (hostresourcekey != null)
            && (!EMPTY_STRING.equals(hostresourcekey)))
            {
            this.strXML = xml;
            this.strResourceKey = hostresourcekey;
            }
        else
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_INVALID);
            }

        this.hostInstrument = hostinstrument;
        this.textArea = new JTextArea(200, 150);
        this.scrollPane= null;
        }


    /***********************************************************************************************
     * Initialise the XmlUIComponent.
     */

    public final void initialiseUI()
        {
        final TextLineNumber textLineNumber;

        super.initialiseUI();

        getXmlTextArea().setText(getXML());
        getXmlTextArea().setBackground(Color.white);
        getXmlTextArea().setForeground(UIComponent.DEFAULT_COLOUR_TEXT.getColor());
        getXmlTextArea().setFont(UIComponent.DEFAULT_FONT_MONOSPACED.getFont());
        getXmlTextArea().setEditable(false);
        getXmlTextArea().setTabSize(4);
        getXmlTextArea().setToolTipText(TOOLTIP_COPY);
        getXmlTextArea().setMargin(new Insets(10, 10, 10, 10));

        scrollPane = new JScrollPane(getXmlTextArea(),
                                     JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                     JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        // Add line numbers for convenience
        textLineNumber = new TextLineNumber(getXmlTextArea());
        getJScrollPane().setRowHeaderView(textLineNumber);

        add(getJScrollPane(), BorderLayout.CENTER);
        }


    /***********************************************************************************************
     * Run this UIComponent.
     */

    public void runUI()
        {
        super.runUI();

        // Make sure the user sees the latest XML every time the tab is shown
        updateXml();
        }


    /***********************************************************************************************
     * Reload the XML to be displayed.
     */

    public void updateXml()
        {
        // Reload the XML from the Instrument
        // NOTE: This may not be the same XML set during construction!

        if ((getHostInstrument() != null)
            && (getHostInstrument().getInstrument() != null))
            {
            final DefaultCaret caret;

            caret = (DefaultCaret)getXmlTextArea().getCaret();
            caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);

            setXML(getHostInstrument().getInstrument().toString());
            getXmlTextArea().setText(getXML());

            // Make certain we never scroll when new text arrives!
            getXmlTextArea().setSelectionStart(0);
            getXmlTextArea().setSelectionEnd(0);
            getXmlTextArea().moveCaretPosition(0);

            if ((getJScrollPane() != null)
                && (getJScrollPane().getViewport() != null))
                {
                getJScrollPane().getViewport().scrollRectToVisible(new Rectangle(1, 1, 1, 1));
                }
            }
        }


    /***********************************************************************************************
     * Get the TextArea used to display the XML.
     *
     * @return RSyntaxTextArea
     */

    public JTextArea getTextArea()
        {
        return (this.textArea);
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
     * Get the XML.
     *
     * @return String
     */

    private String getXML()
        {
        return (this.strXML);
        }


    /***********************************************************************************************
     * Set the XML.
     *
     * @param xml
     */

    private void setXML(final String xml)
        {
        this.strXML = xml;
        }


    /***********************************************************************************************
     * Get the JTextArea used to display the XML.
     *
     * @return JTextArea
     */

    private JTextArea getXmlTextArea()
        {
        return (this.textArea);
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


    /***********************************************************************************************
     * Get the ObservatoryClock.
     *
     * @return ObservatoryClockInterface
     */

    private ObservatoryClockInterface getObservatoryClock()
        {
        final ObservatoryClockInterface clock;

        clock = getHostInstrument().getObservatoryClock();

        return (clock);
        }


    /***********************************************************************************************
     * Get the ResourceKey.
     *
     * @return String
     */

    private String getResourceKey()
        {
        return (this.strResourceKey);
        }
    }
