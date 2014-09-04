// Copyright 2000, 2001, 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009, 2010,
//           2011, 2012, 2013, 2014
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

package org.lmn.fc.ui.panels;


import org.jopendocument.model.OpenDocument;
import org.jopendocument.print.DefaultDocumentPrinter;
import org.lmn.fc.ui.UIComponentPlugin;
import org.lmn.fc.ui.components.BlankUIComponent;
import org.lmn.fc.ui.components.UIComponent;

import java.awt.*;
import java.io.IOException;
import java.net.URL;


/***************************************************************************************************
 * The OdfUIComponent.
 * http://www.jopendocument.org
 */

public final class OdfUIComponent extends UIComponent
                                  implements UIComponentPlugin
    {
    private static final long serialVersionUID = 3466709610251095041L;

    // Injections
    private URL urlODF;

    private OpenDocument openDocument;
    private final Color colorBackground;


    /***********************************************************************************************
     * Construct an OdfUIComponent.
     *
     * http://www.jopendocument.org/
     * http://www.theguardian.com/technology/2014/jan/29/uk-government-plans-switch-to-open-source-from-microsoft-office-suite
     * http://www.theregister.co.uk/2014/01/30/uk_picks_open_document_format_for_all_government_files
     * https://www.openoffice.org/why/why_odf.html
     * http://www.opendocumentformat.org/aboutODF
     * http://opendocsociety.org/tools/odf-tools
     *
     * @param url
     * @param filetype
     */

    public OdfUIComponent(final URL url,
                          final String filetype)
        {
        super();

        this.urlODF = url;
        this.openDocument = new OpenDocument();
        this.colorBackground = Color.white;
        }


    /***********************************************************************************************
     * Initialise the OdfUIComponent.
     */

    public final void initialiseUI()
        {
        super.initialiseUI();

        // Create the OdfUIComponent and add it to the UIComponent
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        createODFViewer(getURL(),
                        getBackgroundColor());
        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }


    /***********************************************************************************************
     * Create the OdfUIComponent and add it to the UIComponent.
     *
     * @param url
     * @param bgcolor
     */

    private void createODFViewer(final URL url,
                                 final Color bgcolor)
        {
        removeAll();
        setBackground(DEFAULT_COLOUR_CANVAS.getColor());

        try
            {
            if (url != null)
                {
                final DefaultDocumentPrinter printer;
                final ODSViewerPanel viewerPanel;

                this.urlODF = url;
                openDocument.loadFrom(urlODF);
                printer = new DefaultDocumentPrinter();
                viewerPanel = new ODSViewerPanel(openDocument, printer, true);

                add(viewerPanel, BorderLayout.CENTER);
                }
            else
                {
                this.urlODF = null;
                openDocument = null;

                add(new BlankUIComponent("ODF doocument URL is NULL"), BorderLayout.CENTER);
                }
            }

        catch (final IOException exception)
            {
            exception.printStackTrace();
            this.urlODF = null;
            openDocument = null;

            add(new BlankUIComponent("Could not load ODF document from URL " + url.toString()), BorderLayout.CENTER);
            }
        }


    /***********************************************************************************************
     * Get the URL of the ODF document, or NULL.
     *
     * @return URL
     */

    private URL getURL()
        {
        return (this.urlODF);
        }


    /***********************************************************************************************
     * Get the background color of the OdfUIComponent.
     *
     * @return Color
     */

    private Color getBackgroundColor()
        {
        return (this.colorBackground);
        }
    }
