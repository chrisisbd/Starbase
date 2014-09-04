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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.communicator.ui;

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
 * A UIComponent to show the News via RSS.
 */

public final class SimpleNewsreaderUIComponent extends InstrumentUIComponentDecorator
    {
    // String Resources
    private static final String CONTENT_TYPE_HTML = "text/html";
    private static final String TOOLTIP_COPY = "You may copy text using ctrl-C";

    private JScrollPane scrollpaneNewsBody;
    private JEditorPane textNewsBody;


    /***********************************************************************************************
     * Construct an SimpleNewsreaderUIComponent.
     *
     * @param hostinstrument
     * @param instrumentxml
     * @param hostui
     * @param task
     * @param font
     * @param colour
     * @param resourcekey
     */

    public SimpleNewsreaderUIComponent(final ObservatoryInstrumentInterface hostinstrument,
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

        this.scrollpaneNewsBody = null;
        this.textNewsBody = new JEditorPane();
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
     */

    private JPanel createUIComponents()
        {
        final JPanel panelUI;
        final Document docEditorPane;

        panelUI = new JPanel();
        panelUI.setLayout(new BorderLayout());

        // Make the Newsreader components
        this.textNewsBody = new JEditorPane();

        // Configure the JEditorPane to use HTML
        getNewsBody().setEditable(false);

        // javax.swing.text.html.HTMLEditorKit provides HTML 3.2 support (some 4.0)
        getNewsBody().setContentType(CONTENT_TYPE_HTML);
        getNewsBody().setMargin(new Insets(10, 10, 10, 10));

        getNewsBody().setForeground(DEFAULT_COLOUR_TEXT.getColor());
        getNewsBody().setFont(DEFAULT_FONT.getFont());
        getNewsBody().setToolTipText(TOOLTIP_COPY);

        // See JEditorPane Javadoc
        getNewsBody().setDocument(getNewsBody().getEditorKit().createDefaultDocument());
        docEditorPane = getNewsBody().getDocument();
        docEditorPane.putProperty(Document.StreamDescriptionProperty, null);

        scrollpaneNewsBody = new JScrollPane(getNewsBody());
        getNewsBodyScrollPane().setBackground(Color.WHITE);
        getNewsBodyScrollPane().setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        getNewsBodyScrollPane().setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        panelUI.add(getNewsBodyScrollPane(), BorderLayout.CENTER);
        ToolTipManager.sharedInstance().registerComponent(getNewsBodyScrollPane());

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
        final String SOURCE = "SimpleNewsreaderUIComponent.setWrappedData() ";
        final Vector<Object> vecNewsRawData;

        // This is the simplest way!
        if (daowrapper == null)
            {
            return;
            }

        vecNewsRawData = daowrapper.getRawData();

        if ((vecNewsRawData != null)
            && (vecNewsRawData.size() == 1)
            && (vecNewsRawData.get(0) instanceof String))
            {
            getNewsBody().setText((String)vecNewsRawData.get(0));
            }
        else
            {
            // Handle reset()
            getNewsBody().setText(EMPTY_STRING);

//            SimpleEventLogUIComponent.logEvent(getHostInstrument().getDAO().getEventLogFragment(),
//                                               EventStatus.INFO,
//                                               METADATA_TARGET_COMMUNICATOR
//                                                + METADATA_ACTION_RESET,
//                                               SOURCE,
//                                               getHostInstrument().getDAO().getObservatoryClock());
            }
        }


    /**********************************************************************************************/
    /* News Headlines and Body                                                                    */
    /***********************************************************************************************
     * Get the News Body.
     *
     * @return JEditorPane
     */

    private JEditorPane getNewsBody()
        {
        return (this.textNewsBody);
        }


    /***********************************************************************************************
     * Get the News Text.
     *
     * @return String
     */

    public String getNewsText()
        {
        return (this.textNewsBody.getText());
        }


    /***********************************************************************************************
     * Get the ScrollPane which holds the News Body.
     *
     * @return JScrollPane
     */

    private JScrollPane getNewsBodyScrollPane()
        {
        return (this.scrollpaneNewsBody);
        }


    /***********************************************************************************************
     * Read all the Resources required by the SimpleNewsreaderUIComponent.
     */

    private void readResources()
        {

        }
    }
