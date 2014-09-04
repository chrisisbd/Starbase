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

import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryUIInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.InstrumentUIComponentDecorator;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.model.registry.NavigationUtilities;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;
import org.lmn.fc.ui.components.JSplitPaneAdapter;
import org.lmn.fc.ui.reports.ReportTablePlugin;

import javax.swing.*;
import javax.swing.text.Document;
import java.awt.*;
import java.util.Vector;


/***********************************************************************************************
 * A UIComponent to show the News via RSS.
 *
 * For Twitter see
 * http://www.winterwell.com/software/jtwitter.php
 * http://www.winterwell.com/software/jtwitter/javadoc/
 */

public final class RssNewsreaderUIComponent extends InstrumentUIComponentDecorator
    {
    // String Resources
    private static final String RSS_UKRAA = "http://www.ukraa.com/main/news_files/blog.xml";
    private static final String CONTENT_TYPE_HTML = "text/html";
    private static final String TOOLTIP_COPY = "You may copy text using ctrl-C";
    private static final String NEWLINE = "<br>";

    private static final double DEFAULT_DIVIDER_LOCATION = 50.0;
    private static final Dimension DIM_DIVIDER = new Dimension(200, 150);

    private JSplitPane splitPaneMain;
    private JScrollPane scrollpaneSubscriptions;
    private JList listNewsSubscriptions;
    private JSplitPaneAdapter adapterMain;

    private JSplitPane splitPaneNews;
    private ReportTablePlugin tableNewsHeadlines;
    private JScrollPane scrollpaneNewsBody;
    private JEditorPane textNewsBody;
    private final JSplitPaneAdapter adapterNews;

    private double dblDividerLocation;      // The position of the divider as a percentage of width



    /***********************************************************************************************
     * Construct an RssNewsreaderUIComponent.
     *
     * @param hostinstrument
     * @param instrumentxml
     * @param hostui
     * @param task
     * @param font
     * @param colour
     * @param resourcekey
     */

    public RssNewsreaderUIComponent(final ObservatoryInstrumentInterface hostinstrument,
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

        this.splitPaneMain = null;
        this.scrollpaneSubscriptions = null;
        this.listNewsSubscriptions = null;
        this.adapterMain = null;

        this.splitPaneNews = null;
        this.tableNewsHeadlines = null;
        this.scrollpaneNewsBody = null;
        this.textNewsBody = new JEditorPane();
        this.adapterNews = null;

        this.dblDividerLocation = DEFAULT_DIVIDER_LOCATION;
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
        createUIComponents();

        // Remove everything from the host UIComponent
        removeAll();

        if (getMainSplitPane() != null)
            {
            add(getMainSplitPane(), BorderLayout.CENTER);
            getMainSplitPane().setVisible(true);
            }
        }


    /***********************************************************************************************
     * Run this UIComponent.
     */

    public void runUI()
        {
        // Set the SplitPane divider location as a percentage of the screen width,
        // from data saved in the RegistryModel the last time we were here
        // Reset the position of the divider, but not until we are visible
        setAndSaveDividerLocation(dblDividerLocation);

        // Update all components of the UI
        validateAndUpdateUI();
        }


    /***********************************************************************************************
     * Dispose of all components of this UIComponent.
     */

    public void disposeUI()
        {
        removeAll();
        }


    /***********************************************************************************************
     * Create all screen components.
     */

    private void createUIComponents()
        {
        final Vector<String> vecFeedNames;
        final JPanel panelUI;
        final JSplitPane mainSplitPane;
        final JSplitPane newsSplitPane;
        final Document docEditorPane;

        panelUI = new JPanel();
        panelUI.setLayout(new BorderLayout());

        // The host UIComponent uses BorderLayout
        add(panelUI, BorderLayout.CENTER);
        }


    /***********************************************************************************************
     * Validate and Update the UI of all components.
     */

    public final void validateAndUpdateUI()
        {
        // Ensure that everything in the Panel is up to date
        NavigationUtilities.updateComponentTreeUI(this);
        }


    /***********************************************************************************************
    /* Utilities                                                                                  */
    /***********************************************************************************************
     * Get the main JSplitPane.
     *
     * @return JSplitPane
     */

    private JSplitPane getMainSplitPane()
        {
        return (this.splitPaneMain);
        }


    /***********************************************************************************************
     * Set the main JSplitPane.
     *
     * @param splitpane
     */

    private void setMainSplitPane(final JSplitPane splitpane)
        {
        this.splitPaneMain = splitpane;
        }


    /***********************************************************************************************
     * Get the main JSplitPaneAdapter.
     *
     * @return JSplitPaneAdapter
     */

    private JSplitPaneAdapter getMainAdapter()
        {
        return (this.adapterMain);
        }


    /***********************************************************************************************
     * Get the ScrollPane which holds the Subscriptions.
     *
     * @return JScrollPane
     */

    private JScrollPane getSubscriptionsScrollPane()
        {
        return (this.scrollpaneSubscriptions);
        }


    /***********************************************************************************************
     * Get the Subscriptions List.
     *
     * @return JList
     */

    private JList getSubscriptionsList()
        {
        return (this.listNewsSubscriptions);
        }


    /***********************************************************************************************
     * Set the position of the SplitPane divider to the specified percentage,
     * and save in the RegistryModel.
     *
     * @param dividerpercentage
     */

    public void setAndSaveDividerLocation(final double dividerpercentage)
        {
        final double dblLocation;

        // Save the percentage location of the divider
        // Trap the careless...
        if ((dividerpercentage < 0.0) || (dividerpercentage > 100.0))
            {
            dblLocation = 50.0;
            }
        else
            {
            dblLocation = dividerpercentage;
            }
        }


    /**********************************************************************************************/
    /* News Headlines and Body                                                                    */
    /***********************************************************************************************
     * Get the News JSplitPane.
     *
     * @return JSplitPane
     */

    private JSplitPane getNewsSplitPane()
        {
        return (this.splitPaneNews);
        }


    /***********************************************************************************************
     * Set the News JSplitPane.
     *
     * @param splitpane
     */

    private void setNewsSplitPane(final JSplitPane splitpane)
        {
        this.splitPaneNews = splitpane;
        }


    /***********************************************************************************************
     * Get the News Headlines.
     *
     * @return ReportTablePlugin
     */

    private ReportTablePlugin getNewsHeadlines()
        {
        return (this.tableNewsHeadlines);
        }


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
     * Get the ScrollPane which holds the News Body.
     *
     * @return JScrollPane
     */

    private JScrollPane getNewsBodyScrollPane()
        {
        return (this.scrollpaneNewsBody);
        }


    /***********************************************************************************************
     * Get the News JSplitPaneAdapter.
     *
     * @return JSplitPaneAdapter
     */

    private JSplitPaneAdapter getNewsAdapter()
        {
        return (this.adapterMain);
        }


    /***********************************************************************************************
     * Read all the Resources required by the MetadataExplorerUIComponent.
     */

    private void readResources()
        {

        }
    }
