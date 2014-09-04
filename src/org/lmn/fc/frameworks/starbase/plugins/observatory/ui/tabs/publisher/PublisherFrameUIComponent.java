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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.publisher;


import org.lmn.fc.common.utilities.files.FileUtilities;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryUIInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.InstrumentUIComponentDecorator;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.PublisherFrameUIComponentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.PublisherUIComponentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.TabToolbarInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.misc.TabToolbar;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;
import org.lmn.fc.ui.components.BlankUIComponent;
import org.lmn.fc.ui.components.UIComponentHelper;

import java.awt.*;
import java.net.URL;


/***************************************************************************************************
 * PublisherFrameUIComponent.
 */

public final class PublisherFrameUIComponent extends InstrumentUIComponentDecorator
                                             implements PublisherFrameUIComponentInterface
    {
    private static final long serialVersionUID = 3974017917106720403L;

    // Injections
    private URL urlContent;
    private final String strHeaderTitle;
    private final String strHeaderIconFilename;

    // UI
    private TabToolbarInterface toolBar;
    private PublisherUIComponentInterface uiPublisher;


    /***********************************************************************************************
     * Construct a PublisherFrameUIComponent.
     *
     * @param hostinstrument
     * @param instrumentxml
     * @param hostui
     * @param task
     * @param defaulturl
     * @param headertitle
     * @param headericonfilename
     * @param font
     * @param colour
     * @param resourcekey
     */

    public PublisherFrameUIComponent(final ObservatoryInstrumentInterface hostinstrument,
                                     final Instrument instrumentxml,
                                     final ObservatoryUIInterface hostui,
                                     final TaskPlugin task,
                                     final URL defaulturl,
                                     final String headertitle,
                                     final String headericonfilename,
                                     final FontInterface font,
                                     final ColourInterface colour,
                                     final String resourcekey)
        {
        super(hostinstrument,
              instrumentxml,
              hostui,
              task,
              font,
              colour,
              resourcekey);

        // Injections
        this.urlContent = defaulturl;
        this.strHeaderTitle = headertitle;
        this.strHeaderIconFilename = headericonfilename;

        // UI
        this.toolBar = null;
        this.uiPublisher = null;
        }


    /**********************************************************************************************/
    /* UI State                                                                                   */
    /***********************************************************************************************
     * Initialise this UIComponent.
     */

    public void initialiseUI()
        {
        final String SOURCE = "PublisherFrameUIComponent.initialiseUI() ";

        LOGGER.debug(isDebug(), SOURCE);

        // Colours
        setBackground(DEFAULT_COLOUR_CANVAS.getColor());

        // See if we can support the requested FileType
        // ToDo Convert FileUtilities to an enum, iterate, and test a field 'Printable'
        if ((getContentURL() != null)
           && (getContentURL().toString().toLowerCase().endsWith("." + FileUtilities.pdf)))
            {
            // Create the TabToolbar and initialise it
            setToolBar(new TabToolbar(getHostInstrument(),
                                      getHeaderTitle(),
                                      getHeaderIconFilename(),
                                      getFontData(),
                                      getColourData(),
                                      DEFAULT_COLOUR_TAB_BACKGROUND,
                                      isDebug()));
            getToolBar().initialiseUI();

            // Create the PdfPublisherUIComponent and initialise it
            // This is the only creation of PdfPublisherUIComponent
            setPublisherUI(new PdfPublisherUIComponent(getHostInstrument(),
                                                       getToolBar(),
                                                       getContentURL(),
                                                       getResourceKey()));
            getPublisherUI().initialiseUI();

            // Link the Publisher to the Toolbar
            getToolBar().setPublisherUI(getPublisherUI());

            // Put the components together
            add((Component) getToolBar(), BorderLayout.NORTH);
            add((Component) getPublisherUI(), BorderLayout.CENTER);
            }
        else
            {
            setToolBar(null);
            setPublisherUI(null);

            add(new BlankUIComponent("Publication file type not supported"), BorderLayout.CENTER);
            }
        }


    /***********************************************************************************************
     * Run this UIComponent.
     */

    public void runUI()
        {
        final String SOURCE = "PublisherFrameUIComponent.runUI() ";

        LOGGER.debug(isDebug(), SOURCE);

        super.runUI();

        if (getPublisherUI() != null)
            {
            UIComponentHelper.runComponentAndTransferActions((Component) getPublisherUI(), this);
            }
        }


    /***********************************************************************************************
     * Stop this UIComponent.
     */

    public void stopUI()
        {
        final String SOURCE = "PublisherFrameUIComponent.stopUI() ";

        LOGGER.debug(isDebug(), SOURCE);

        super.stopUI();

        if (getPublisherUI() != null)
            {
            getPublisherUI().stopUI();
            }
        }


    /***********************************************************************************************
     * Dispose of all components of this UIComponent.
     */

    public void disposeUI()
        {
        final String SOURCE = "PublisherFrameUIComponent.disposeUI() ";

        LOGGER.debug(isDebug(), SOURCE);

        stopUI();

        if (getToolBar() != null)
            {
            getToolBar().disposeUI();
            setToolBar(null);
            }

        if (getPublisherUI() != null)
            {
            getPublisherUI().disposeUI();
            setPublisherUI(null);
            }

        super.disposeUI();
        }


    /**********************************************************************************************/
    /* UI                                                                                         */
    /***********************************************************************************************
     * Get the URL providing the Publisher content.
     *
     * @return URL
     */

    public URL getContentURL()
        {
        return (this.urlContent);
        }


    /***********************************************************************************************
     * Set the URL providing the Publisher content.
     *
     * @param url
     */

    public void setContentURL(final URL url)
        {
        this.urlContent = url;
        }


    /***********************************************************************************************
     * Get the HeaderTitle.
     *
     * @return String
     */

    private String getHeaderTitle()
        {
        return (this.strHeaderTitle);
        }


    /***********************************************************************************************
     * Get the HeaderIconFilename.
     *
     * @return String
     */

    private String getHeaderIconFilename()
        {
        return (this.strHeaderIconFilename);
        }


    /***********************************************************************************************
     * Update the content to be displayed.
     */

    public void updateContent()
        {
        if (getPublisherUI() != null)
            {
            getPublisherUI().updateContent();
            }
        }


    /***********************************************************************************************
     * Get the Viewer TabToolbarInterface.
     *
     * @return TabToolbarInterface
     */

    public TabToolbarInterface getToolBar()
        {
        return (this.toolBar);
        }


    /***********************************************************************************************
     * Set the Viewer TabToolbarInterface.
     *
     * @param toolbar
     */

    private void setToolBar(final TabToolbarInterface toolbar)
        {
        this.toolBar = toolbar;
        }


    /***********************************************************************************************
     * Get the Publisher UI.
     *
     * @return PublisherUIComponentInterface
     */

    public PublisherUIComponentInterface getPublisherUI()
        {
        return (this.uiPublisher);
        }


    /***********************************************************************************************
     * Set the Publisher UI.
     *
     * @param publisherui
     */

    private void setPublisherUI(final PublisherUIComponentInterface publisherui)
        {
        this.uiPublisher = publisherui;
        }
    }
