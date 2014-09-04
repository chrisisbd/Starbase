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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.misc;

import org.lmn.fc.ui.UIComponentPlugin;
import org.lmn.fc.ui.components.UIComponent;

import javax.swing.*;
import javax.swing.text.Document;
import java.awt.*;


/***************************************************************************************************
 * The HTMLUIComponent.
 */

public final class HTMLUIComponent extends UIComponent
                                   implements UIComponentPlugin
    {
    // String Resources
    private static final String CONTENT_TYPE_HTML = "text/html";

    private String strText;
    private JEditorPane editorPane;


    /***********************************************************************************************
     * Construct an HTMLUIComponent.
     *
     * @param text
     */

    public HTMLUIComponent(final String text)
        {
        super();

        this.strText = text;
        this.editorPane = new JEditorPane();
        }


    /***********************************************************************************************
     * Initialise the HTMLPanel.
     */

    public final void initialiseUI()
        {
        // Create the HTMLUIComponent and add it to the UIComponent
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        createHTMLViewer(strText);
        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }


    /***********************************************************************************************
     * Create the HTMLPanel and add it to the UIComponent.
     */

    private void createHTMLViewer(final String text)
        {
        final JScrollPane editorScrollPane;

        editorPane = new JEditorPane();

        // Configure the JEditorPane to use HTML
        editorPane.setEditable(false);

        // javax.swing.text.html.HTMLEditorKit provides HTML 3.2 support
        editorPane.setContentType(CONTENT_TYPE_HTML);
        editorPane.setMargin(new Insets(10, 10, 10, 10));
        editorPane.setText(text);

        editorScrollPane = new JScrollPane(editorPane);
        editorScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        editorScrollPane.setMinimumSize(new Dimension(100, 100));

        this.add(editorScrollPane);
        }


    /***********************************************************************************************
     * Just set the text of the HTMLUIComponent directly.
     * The string should be specified in terms of HTML.
     *
     * @param text
     */

    public void setHTMLText(final String text)
        {
        if ((editorPane != null)
            && (text != null))
            {
            final Document doc;

            // See JEditorPane Javadoc
            editorPane.setDocument(editorPane.getEditorKit().createDefaultDocument());
            doc = editorPane.getDocument();
            doc.putProperty(Document.StreamDescriptionProperty, null);

            this.strText = text;
            editorPane.setText(text);
            }
        else
            {
            this.strText = EMPTY_STRING;
            editorPane.setText(text);
            }
        }
    }
