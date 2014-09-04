// Copyright 2000, 2001, 2002, 2003, 04, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013, 2013
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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.monitor.ui;

import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.DAOWrapperInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryUIInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.InstrumentUIComponentDecorator;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.model.registry.NavigationUtilities;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;

import javax.swing.*;
import javax.swing.text.Document;
import java.awt.*;
import java.util.Vector;


/***********************************************************************************************
 * A UIComponent to show an RSS feed.
 */

public final class SimpleRssUIComponent extends InstrumentUIComponentDecorator
    {
    // String Resources
    private static final String CONTENT_TYPE_HTML = "text/html";
    private static final String TOOLTIP_COPY = "You may copy text using ctrl-C";

    private JScrollPane scrollpaneRssBody;
    private JEditorPane textRssBody;


    /***********************************************************************************************
     * Construct an SimpleRssUIComponent.
     *
     * @param hostinstrument
     * @param instrumentxml
     * @param hostui
     * @param task
     * @param font
     * @param colour
     * @param resourcekey
     */

    public SimpleRssUIComponent(final ObservatoryInstrumentInterface hostinstrument,
                                final Instrument instrumentxml,
                                final ObservatoryUIInterface hostui,
                                final TaskPlugin task,
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
              resourcekey, 1);

        this.scrollpaneRssBody = null;
        this.textRssBody = new JEditorPane();
        }


    /***********************************************************************************************
     * Initialise the XmlUIComponent.
     */

    public final void initialiseUI()
        {
        // DO NOT USE super.initialiseUI()

        // Get the latest Resources
        readResources();

        // Create all UI components
        // The host UIComponent uses BorderLayout
        add(createUIComponents(), BorderLayout.CENTER);
        }


    /***********************************************************************************************
     * Run this UIComponent.
     */

    public void runUI()
        {
        // Update all components of the UI
        validateAndUpdateUI();
        }


    /***********************************************************************************************
     * Dispose of all components of this UIComponent.
     */

    public void disposeUI()
        {
        // Remove everything from the host UIComponent
        removeAll();
        }


    /***********************************************************************************************
     * Create all screen components.
     *
     * @return JPanel
     */

    private JPanel createUIComponents()
        {
        final JPanel panelUI;
        final Document docEditorPane;

        panelUI = new JPanel();
        panelUI.setLayout(new BorderLayout());

        // Make the Rssreader components
        this.textRssBody = new JEditorPane();

        // Configure the JEditorPane to use HTML
        getRssBody().setEditable(false);

        // javax.swing.text.html.HTMLEditorKit provides HTML 3.2 support (some 4.0)
        getRssBody().setContentType(CONTENT_TYPE_HTML);
        getRssBody().setMargin(new Insets(10, 10, 10, 10));

        getRssBody().setForeground(DEFAULT_COLOUR_TEXT.getColor());
        getRssBody().setFont(DEFAULT_FONT.getFont());
        getRssBody().setToolTipText(TOOLTIP_COPY);

        // See JEditorPane Javadoc
        getRssBody().setDocument(getRssBody().getEditorKit().createDefaultDocument());
        docEditorPane = getRssBody().getDocument();
        docEditorPane.putProperty(Document.StreamDescriptionProperty, null);

        scrollpaneRssBody = new JScrollPane(getRssBody());
        getRssBodyScrollPane().setBackground(Color.WHITE);
        getRssBodyScrollPane().setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        getRssBodyScrollPane().setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        panelUI.add(getRssBodyScrollPane(), BorderLayout.CENTER);
        ToolTipManager.sharedInstance().registerComponent(getRssBodyScrollPane());

        return (panelUI);
        }


    /***********************************************************************************************
     * Validate and Update the UI of all components.
     */

    private void validateAndUpdateUI()
        {
        // Ensure that everything in the Panel is up to date
        NavigationUtilities.updateComponentTreeUI(this);
        }


    /***********************************************************************************************
     * Set the data from the DAO finished() method.
     * Optionally refresh the UI of data tabs or update the associated Metadata.
     *
     * @param daowrapper
     * @param updatedata
     * @param updatemetadata
     */

    public void setWrappedData(final DAOWrapperInterface daowrapper,
                               final boolean updatedata,
                               final boolean updatemetadata)
        {
        final String SOURCE = "SimpleRssUIComponent.setWrappedData() ";
        final Vector<Object> vecRssRawData;

        // This is the simplest way!
        if (daowrapper == null)
            {
            return;
            }

        // WARNING! Since ObservatoryMonitor IS A ObservatoryLogInterface then doing:
        // super.setWrappedData(wrapper, refreshdata, updatemetadata);
        // would make an infinite loop!
        // So... treat this UIComponent as the end of the line,
        // and do NOT pass the wrapped data on any further

        vecRssRawData = daowrapper.getRawData();

        if ((vecRssRawData != null)
            && (vecRssRawData.size() == 1)
            && (vecRssRawData.get(0) instanceof String))
            {
            getRssBody().setText((String)vecRssRawData.get(0));
            }
        else
            {
            // Handle reset()
            getRssBody().setText(EMPTY_STRING);
            }
        }


    /**********************************************************************************************/
    /* RSS                                                                                        */
    /***********************************************************************************************
     * Get the Rss Body.
     *
     * @return JEditorPane
     */

    private JEditorPane getRssBody()
        {
        return (this.textRssBody);
        }


    /***********************************************************************************************
     * Get the Rss Text.
     *
     * @return String
     */

    public String getRssText()
        {
        return (this.textRssBody.getText());
        }


    /***********************************************************************************************
     * Get the ScrollPane which holds the Rss Body.
     *
     * @return JScrollPane
     */

    private JScrollPane getRssBodyScrollPane()
        {
        return (this.scrollpaneRssBody);
        }


    /***********************************************************************************************
     * Read all the Resources required by the SimpleRssUIComponent.
     */

    private void readResources()
        {

        }
    }
