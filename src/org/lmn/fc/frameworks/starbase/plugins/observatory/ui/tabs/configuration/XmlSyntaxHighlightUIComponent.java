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

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.RSyntaxUtilities;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.lmn.fc.common.exceptions.FrameworkException;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryClockInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.XmlUIComponentInterface;
import org.lmn.fc.ui.components.UIComponent;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;


/***********************************************************************************************
 * A UIComponent to show some XML, with syntax highlighting.
 */

public class XmlSyntaxHighlightUIComponent extends UIComponent
                                           implements XmlUIComponentInterface
    {
    private static final long serialVersionUID = 779104130354971676L;

    // Injections
    private final ObservatoryInstrumentInterface hostInstrument;
    private String strXML;
    private final String strResourceKey;

    // UI
    private JTextArea textArea;
    private RTextScrollPane scrollPane;


    /***********************************************************************************************
     * Construct an XmlUIComponent.
     *
     * @param hostinstrument
     * @param xml
     * @param hostresourcekey
     */

    public XmlSyntaxHighlightUIComponent(final ObservatoryInstrumentInterface hostinstrument,
                                         final String xml,
                                         final String hostresourcekey)
        {
        super();

        // Injections
        this.hostInstrument = hostinstrument;

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

        // UI
        this.textArea = null;
        this.scrollPane = null;
        }


    /***********************************************************************************************
     * Initialise the XmlSyntaxHighlightUIComponent.
     */

    public final void initialiseUI()
        {
        final String SOURCE = "XmlSyntaxHighlightUIComponent.initialiseUI() ";

        super.initialiseUI();

        removeAll();

        // This is the only creation of RSyntaxTextArea
        setTextArea(new RSyntaxTextArea(20, 60));

        getTextArea().setEditable(false);
        getTextArea().setToolTipText(TOOLTIP_COPY);
        getTextArea().setText(getXML());

        if (getTextArea() instanceof RSyntaxTextArea)
            {
            ((RSyntaxTextArea)getTextArea()).setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_XML);
            ((RSyntaxTextArea)getTextArea()).setCodeFoldingEnabled(true);
            ((RSyntaxTextArea)getTextArea()).setAntiAliasingEnabled(true);

            scrollPane = new RTextScrollPane(((RSyntaxTextArea)getTextArea()));
            getScrollPane().setFoldIndicatorEnabled(true);

            // The gutter, or null if the text area is not in an RTextScrollPane
            RSyntaxUtilities.getGutter(((RSyntaxTextArea)getTextArea())).setBackground(DEFAULT_COLOUR_TAB_BACKGROUND.getColor());
            }
        else
            {
            scrollPane = new RTextScrollPane();
            }

        // The host UIComponent uses BorderLayout
        add(getScrollPane(), BorderLayout.CENTER);
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

            caret = (DefaultCaret) getTextArea().getCaret();
            caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);

            setXML(getHostInstrument().getInstrument().toString());
            getTextArea().setText(getXML());

            // Make certain we never scroll when new text arrives!
            getTextArea().setSelectionStart(0);
            getTextArea().setSelectionEnd(0);
            getTextArea().moveCaretPosition(0);

            if ((getScrollPane() != null)
                && (getScrollPane().getViewport() != null))
                {
                getScrollPane().getViewport().scrollRectToVisible(new Rectangle(1, 1, 1, 1));
                }
            }
        }


    /***********************************************************************************************
     * Set the TextArea used to display the XML.
     *
     * @param textarea
     */

    private void setTextArea(final RSyntaxTextArea textarea)
        {
        this.textArea = textarea;
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
