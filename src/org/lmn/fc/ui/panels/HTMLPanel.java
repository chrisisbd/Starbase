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

//--------------------------------------------------------------------------------------------------
// Revision History
//
//  28-04-03    LMN created file
//  08-05-03    LMN added Hyperlink listener
//  03-09-04    LMN adding runUI
//  05-10-04    LMN adding disposeHTMLPanelTab()
//  30-09-05    LMN changed constructor to use URL, for use in a Jar
//  07-06-06    LMN polishing!
//
//--------------------------------------------------------------------------------------------------
// Viewer package

package org.lmn.fc.ui.panels;

//--------------------------------------------------------------------------------------------------
// Imports

import org.lmn.fc.ui.UIComponentPlugin;
import org.lmn.fc.ui.components.UIComponent;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;
import java.awt.*;
import java.io.IOException;
import java.net.ConnectException;
import java.net.URL;
import java.net.URLConnection;


/***************************************************************************************************
 * The HTMLPanel.
 */

public final class HTMLPanel extends UIComponent
                             implements UIComponentPlugin
    {
    // String Resources
    private static final String CONTENT_TYPE_HTML = "text/html";
    private static final String MSG_FILENOTFOUND = "<html><b>The file is not available</b></html>";
    private static final String MSG_CONNECTION_REFUSED = "<html><b>The Connection has been refused</b></html>";

    private URL urlHTML;
    private JEditorPane editorPane;
    private final boolean boolScrollPane;
    private final Color colorBackground;


    /***********************************************************************************************
     * Construct an HTMLPanel, shown on a ScrollPane.
     *
     * @param url
     */

    public HTMLPanel(final URL url)
        {
        super();

        this.urlHTML = url;
        this.editorPane = new JEditorPane();
        this.boolScrollPane = true;
        this.colorBackground = Color.white;
        }


    /***********************************************************************************************
     * Construct an HTMLPanel, shown on an optional ScrollPane.
     *
     * @param url
     * @param scrollpane
     * @param bgcolor
     */

    public HTMLPanel(final URL url,
                     final boolean scrollpane,
                     final Color bgcolor)
        {
        super();

        this.urlHTML = url;
        this.editorPane = new JEditorPane();
        this.boolScrollPane = scrollpane;
        this.colorBackground = bgcolor;
        }


    /***********************************************************************************************
     * Initialise the HTMLPanel.
     */

    public final void initialiseUI()
        {
        super.initialiseUI();

        // Create the HTMLPanel and add it to the UIComponent
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        createHTMLViewer(getURL(),
                         hasScrollPane(),
                         getBackgroundColor());
        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }


    /***********************************************************************************************
     * Create the HTMLPanel and add it to the UIComponent.
     * Optionally show the panel on a ScrollPane.
     *
     * @param url
     * @param scrollpane
     * @param bgcolor
     */

    private void createHTMLViewer(final URL url,
                                  final boolean scrollpane,
                                  final Color bgcolor)
        {
        editorPane = new JEditorPane();

        // Configure the JEditorPane to use HTML
        editorPane.setEditable(false);
        editorPane.setBackground(bgcolor);

        // javax.swing.text.html.HTMLEditorKit provides HTML 3.2 support
        editorPane.setContentType(CONTENT_TYPE_HTML);
        editorPane.setMargin(new Insets(10, 10, 10, 10));

        try
            {
            editorPane.addHyperlinkListener(new Hyperactive());

            if (url != null)
                {
                final URLConnection connection;

                this.urlHTML = url;
                connection = url.openConnection();
                connection.setAllowUserInteraction(true);
                editorPane.setPage(url);
                }
            }

        catch (ConnectException exception)
            {
            editorPane.setText(MSG_CONNECTION_REFUSED);
            //exception.printStackTrace();
            }

        catch (IOException exception)
            {
            editorPane.setText(MSG_FILENOTFOUND);
            //exception.printStackTrace();
            }

        removeAll();

        if (scrollpane)
            {
            final JScrollPane editorScrollPane;

            editorScrollPane = new JScrollPane(editorPane);
            editorScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            editorScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            editorScrollPane.setMinimumSize(new Dimension(50, 50));

            this.add(editorScrollPane, BorderLayout.CENTER);
            }
        else
            {
            editorPane.setMinimumSize(new Dimension(100, 100));

            this.add(editorPane, BorderLayout.CENTER);
            }
        }


    /***********************************************************************************************
     * Go to a new URL.
     *
     * @param newurl
     */

    public void gotoURL(final URL newurl)
        {
        if ((newurl != null)
            && (editorPane != null))
            {
            try
                {
                final URLConnection connection;
                final Document doc;

                this.urlHTML = newurl;
                connection = newurl.openConnection();
                connection.setAllowUserInteraction(true);

                // If the desired URL is the one currently being displayed,
                // the document will not be reloaded. To force a document reload it is necessary
                // to clear the stream description property of the document.
                // Do this BEFORE setting the new page! (found by experiment...)
                doc = editorPane.getDocument();
                doc.putProperty(Document.StreamDescriptionProperty, null);

                try
                    {
                    editorPane.setPage(newurl);
                    }

                catch (NullPointerException exception)
                    {
                    // We seem to need to absorb the effect of the above property change?
                    // exception.printStackTrace();
                    }
                }

            catch (ConnectException exception)
                {
                // This isn't really an error that the User can do anything about
                //LOGGER.error("HTMLPanel.gotoURL() " + exception.getMessage());
                editorPane.setText(MSG_CONNECTION_REFUSED);
                }

            catch (IOException exception)
                {
                //LOGGER.error("HTMLPanel.gotoURL() " + exception.getMessage());
                editorPane.setText(MSG_FILENOTFOUND);
                }
            }
        }


    /***********************************************************************************************
     * Just set the text of the HTML panel directly.
     * The string should be specified in terms of HTML.
     *
     * @param text
     */

    public void setHTMLText(final String text)
        {
        if (editorPane != null)
            {
            final Document doc;

            // See JEditorPane Javadoc
            editorPane.setDocument(editorPane.getEditorKit().createDefaultDocument());
            doc = editorPane.getDocument();
            doc.putProperty(Document.StreamDescriptionProperty, null);

            editorPane.setText(text);
            }
        }

    /***********************************************************************************************
     * Get the URL of the HTML document.
     *
     * @return URL
     */

    private URL getURL()
        {
        return (this.urlHTML);
        }


    /***********************************************************************************************
     * Indicate if this HTMLPanel is shown on a ScrollPane.
     *
     * @return boolean
     */

    private boolean hasScrollPane()
        {
        return (this.boolScrollPane);
        }


    /***********************************************************************************************
     * Get the background color of the EditorPane.
     *
     * @return
     */

    private Color getBackgroundColor()
        {
        return (this.colorBackground);
        }


    /***********************************************************************************************
     * A class to respond to hyperlink events.
     */

    private static final class Hyperactive implements HyperlinkListener
        {
        /*******************************************************************************************
         * Respond to hyperlink events.
         *
         * @param event
         */

        public void hyperlinkUpdate(final HyperlinkEvent event)
            {
             if (HyperlinkEvent.EventType.ACTIVATED.equals(event.getEventType()))
                {
                 final JEditorPane pane = (JEditorPane) event.getSource();

                 if (event instanceof HTMLFrameHyperlinkEvent)
                    {
                    final HTMLFrameHyperlinkEvent evt = (HTMLFrameHyperlinkEvent)event;
                    final HTMLDocument doc = (HTMLDocument)pane.getDocument();

                    doc.processHTMLFrameHyperlinkEvent(evt);
                    }
                else
                    {
                    try
                        {
                        // ToDo record the URL in the parent class?
                        pane.setPage(event.getURL());
                        }

                    catch (IOException exception)
                        {
                        LOGGER.error("HyperlinkListener.hyperlinkUpdate " + exception.getMessage());
                        }
                     }
                }
            }
        }
    }


//--------------------------------------------------------------------------------------------------
// End of File




