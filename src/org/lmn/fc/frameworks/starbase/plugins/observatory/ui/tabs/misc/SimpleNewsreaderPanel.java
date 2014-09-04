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

import org.lmn.fc.common.actions.ContextAction;
import org.lmn.fc.common.utilities.misc.Utilities;
import org.lmn.fc.common.utilities.threads.SwingWorker;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.InstrumentState;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryUIInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.InstrumentUIComponentDecorator;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.RssHelper;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.model.registry.NavigationUtilities;
import org.lmn.fc.model.registry.RegistryModelUtilities;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;
import org.lmn.fc.model.xmlbeans.subscriptions.RssSubscription;
import org.lmn.fc.ui.reports.ReportTableHelper;
import org.lmn.fc.ui.reports.ReportTablePlugin;

import javax.swing.*;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.print.PageFormat;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.util.List;


/**************************************************************************************************
 * A UIComponent to show the News via RSS, with a Toolbar.
 */

public final class SimpleNewsreaderPanel extends InstrumentUIComponentDecorator
    {
    // String Resources
    private static final String CONTENT_TYPE_HTML    = "text/html";
    private static final String TOOLTIP_RELOAD       = "Reload RSS Newsfeed";
    private static final String TOOLTIP_DISPOSE      = "Delete the RSS News to save memory";
    private static final String TOOLTIP_COPY         = "You may copy text using ctrl-C";
    private static final String MSG_RELOAD           = "Click Reload to get the latest newsfeed via RSS";
    private static final String MSG_UNABLE_RETRIEVE  = "Unable to retrieve newsfeed after ";
    private static final String MSG_RETRIES          = " retries";
    private static final String DIALOG_PRINT         = "Print the RSS Viewer contents";
    private static final String MSG_RSS_VIEWER       = "the RSS Viewer";
    private static final String MSG_RSS_VIEWER_PRINTED = "The RSS Viewer has been printed";
    public static final  String DEFAULT_RSS_NEWSFEED = "RSS Newsfeed";

    private static final int UPDATE_DELAY = 1000000;
    private static final int RETRY_COUNT  = 10;

    // Injections
    private final List<RssSubscription> listSubscriptions;

    private JScrollPane scrollpaneNewsBody;
    private JEditorPane textNewsBody;

    // The Thread for handling the Newsreader
    private org.lmn.fc.common.utilities.threads.SwingWorker workerNewsreader;

    private boolean boolNewsreaderRunning;


    /**********************************************************************************************
     * Construct an SimpleNewsreaderPanel.
     *
     * @param hostinstrument
     * @param instrumentxml
     * @param hostui
     * @param task
     * @param rsssubscriptions
     * @param font
     * @param colour
     * @param resourcekey
     */

    public SimpleNewsreaderPanel(final ObservatoryInstrumentInterface hostinstrument,
                                 final Instrument instrumentxml,
                                 final ObservatoryUIInterface hostui,
                                 final TaskPlugin task,
                                 final List<RssSubscription> rsssubscriptions,
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

        this.listSubscriptions = rsssubscriptions;
        this.scrollpaneNewsBody = null;
        this.textNewsBody = new JEditorPane();
        this.boolNewsreaderRunning = false;
        }


    /**********************************************************************************************
     * Initialise the SimpleNewsreaderPanel.
     */

    public final void initialiseUI()
        {
        // DO NOT USE super.initialiseUI()

        // Get the latest Resources
        readResources();

        // Create all UI components
        // The host UIComponent uses BorderLayout
        add(createUIComponents(), BorderLayout.CENTER);

        setNewsText(MSG_RELOAD);
        }


    /**********************************************************************************************
     * Run this UIComponent.
     */

    public void runUI()
        {
        // Update all components of the UI
        //validateAndUpdateUI();
        }


    /**********************************************************************************************
     * Dispose of the UIComponent.
     */

    public void disposeUI()
        {
        if (getNewsreaderWorker() != null)
            {
            getNewsreaderWorker().controlledStop(true, SWING_WORKER_STOP_DELAY);
            getNewsreaderWorker().destroy();
            }

        setRunning(false);
        }


    /**********************************************************************************************
     * Validate and Update the UI of all components.
     */

    private void validateAndUpdateUI()
        {
        // Ensure that everything in the Panel is up to date
        NavigationUtilities.updateComponentTreeUI(this);
        }


    /**********************************************************************************************/
    /* Utilties                                                                                   */
    /**********************************************************************************************
     * Create all screen components.
     *
     * @return JPanel
     */

    private JPanel createUIComponents()
        {
        final JPanel panelUI;
        final JToolBar toolBar;
        final Document docEditorPane;

        panelUI = new JPanel();
        panelUI.setLayout(new BorderLayout());

        toolBar = createToolbar();
        panelUI.add(toolBar, BorderLayout.NORTH);

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


    /**********************************************************************************************
     * Create the Toolbar.
     *
     * @return JToolBar
     */

    private JToolBar createToolbar()
        {
        final JToolBar toolBar;
        final JLabel labelName;
        final String strLabel;
        final ImageIcon icon;
        final ContextAction actionPageSetup;
        final ContextAction actionPrint;
        final JButton buttonPageSetup;
        final JButton buttonPrint;
        final JButton buttonReload;
        final JButton buttonDispose;

        // Find out which feed were are showing
        if ((getRssSubscriptions() != null)
            && (!getRssSubscriptions().isEmpty()))
            {
            strLabel = getRssSubscriptions().get(0).getName();
            icon = RegistryModelUtilities.getCommonIcon(getRssSubscriptions().get(0).getIconFilename());
            }
        else
            {
            // This should never occur!
            strLabel = DEFAULT_RSS_NEWSFEED;
            icon = null;
            }

        toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setMinimumSize(DIM_TOOLBAR_SIZE);
        toolBar.setPreferredSize(DIM_TOOLBAR_SIZE);
        toolBar.setMaximumSize(DIM_TOOLBAR_SIZE);
        toolBar.setBackground(DEFAULT_COLOUR_TAB_BACKGROUND.getColor());

        labelName = new JLabel(strLabel, icon, SwingConstants.LEFT)
            {
            private static final long serialVersionUID = -7225501496446776150L;


            // Enable Antialiasing in Java 1.5
            protected void paintComponent(final Graphics graphics)
                {
                final Graphics2D graphics2D = (Graphics2D) graphics;

                // For antialiasing text
                graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                                            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                super.paintComponent(graphics2D);
                }
            };

        labelName.setFont(getFontData().getFont().deriveFont(ReportTableHelper.SIZE_HEADER_FONT).deriveFont(Font.BOLD));
        labelName.setForeground(getColourData().getColor());
        labelName.setHorizontalTextPosition(SwingConstants.RIGHT);
        labelName.setIconTextGap(TOOLBAR_ICON_TEXT_GAP);

        //-------------------------------------------------------------------------------------
        // Page Setup

        buttonPageSetup = new JButton();
        buttonPageSetup.setBorder(BORDER_BUTTON);
        buttonPageSetup.setHideActionText(true);

        actionPageSetup = new ContextAction(ReportTablePlugin.PREFIX_PAGE_SETUP + MSG_RSS_VIEWER,
                                            RegistryModelUtilities.getCommonIcon(FILENAME_ICON_PAGE_SETUP),
                                            ReportTablePlugin.PREFIX_PAGE_SETUP + MSG_RSS_VIEWER,
                                            KeyEvent.VK_S,
                                            false,
                                            true)
            {
            final static String SOURCE = "ContextAction:PageSetup ";
            private static final long serialVersionUID = 6802400471966299436L;


            public void actionPerformed(final ActionEvent event)
                {
                if (getNewsBody() != null)
                    {
                    final PrinterJob printerJob;
                    final PageFormat pageFormat;

                    printerJob = PrinterJob.getPrinterJob();
                    pageFormat = printerJob.pageDialog(getPageFormat());

                    if (pageFormat != null)
                        {
                        setPageFormat(pageFormat);
                        }
                    }
                else
                    {
                    LOGGER.error(SOURCE + "RSS Viewer UI unexpectedly NULL");
                    }
                }
            };

        buttonPageSetup.setAction(actionPageSetup);
        buttonPageSetup.setToolTipText((String) actionPageSetup.getValue(Action.SHORT_DESCRIPTION));
        buttonPageSetup.setEnabled(true);

        //-------------------------------------------------------------------------------------
        // Printing

        buttonPrint = new JButton();
        buttonPrint.setBorder(BORDER_BUTTON);
        buttonPrint.setHideActionText(true);

        actionPrint = new ContextAction(ReportTablePlugin.PREFIX_PRINT + MSG_RSS_VIEWER,
                                        RegistryModelUtilities.getCommonIcon(FILENAME_ICON_PRINT),
                                        ReportTablePlugin.PREFIX_PRINT + MSG_RSS_VIEWER,
                                        KeyEvent.VK_P,
                                        false,
                                        true)
            {
            final static String SOURCE = "ContextAction:Print ";
            private static final long serialVersionUID = 8346968631811861938L;


            public void actionPerformed(final ActionEvent event)
                {
                final SwingWorker workerPrinter;

                workerPrinter = new SwingWorker(REGISTRY.getThreadGroup(),
                                                "SwingWorker Printer")
                    {
                    public Object construct()
                        {
                        LOGGER.debug(isDebug(), SOURCE + "SwingWorker construct()");

                        // Let the user know what happened
                        return (printDialog());
                        }

                    // Display updates occur on the Event Dispatching Thread
                    public void finished()
                        {
                        final String [] strSuccess =
                            {
                            MSG_RSS_VIEWER_PRINTED,
                            MSG_PRINT_CANCELLED
                            };

                        if ((get() != null)
                            && (get() instanceof Boolean)
                            && ((Boolean) get())
                            && (!isStopping()))
                            {
                            JOptionPane.showMessageDialog(null,
                                                          strSuccess[0],
                                                          DIALOG_PRINT,
                                                          JOptionPane.INFORMATION_MESSAGE,
                                                          RegistryModelUtilities.getCommonIcon(FILENAME_ICON_DIALOG_PRINT));
                            }
                        else
                            {
                            JOptionPane.showMessageDialog(null,
                                                          strSuccess[1],
                                                          DIALOG_PRINT,
                                                          JOptionPane.INFORMATION_MESSAGE,
                                                          RegistryModelUtilities.getCommonIcon(FILENAME_ICON_DIALOG_PRINT));
                            }
                        }
                    };

                // Start the Print Thread
                workerPrinter.start();
                }


            /**********************************************************************************
             * Show the Print dialog.
             *
             * @return boolean
             */

            private boolean printDialog()
                {
                boolean boolSuccess;

                // Check to see that we actually have a printer...
                if (PrinterJob.lookupPrintServices().length == 0)
                    {
                    JOptionPane.showMessageDialog(null,
                                                  ReportTablePlugin.MSG_NO_PRINTER,
                                                  ReportTablePlugin.PREFIX_PRINT + MSG_RSS_VIEWER,
                                                  JOptionPane.WARNING_MESSAGE,
                                                  RegistryModelUtilities.getCommonIcon(FILENAME_ICON_DIALOG_PRINT));
                    boolSuccess = false;
                    }
                else
                    {
                    final PrinterJob printerJob;

                    printerJob = PrinterJob.getPrinterJob();

                    if ((getNewsBody() != null)
                        && (printerJob.printDialog()))
                        {
                        final PageFormat pageFormat;

                        pageFormat = getPageFormat();

                        if (pageFormat != null)
                            {
                            // The RSS Viewer is Printable
                            // ToDo Header & Footer MessageFormats
                            printerJob.setPrintable(getNewsBody().getPrintable(null, null),
                                                    pageFormat);
                            try
                                {
                                printerJob.print();
                                boolSuccess = true;
                                }

                            catch (final PrinterException exception)
                                {
                                LOGGER.error(SOURCE + "[exception=" + exception.getMessage() + "]");
                                boolSuccess = false;
                                }
                            }
                        else
                            {
                            boolSuccess = false;
                            }
                        }
                    else
                        {
                        boolSuccess = false;
                        }
                    }

                return (boolSuccess);
                }
            };

        buttonPrint.setAction(actionPrint);
        buttonPrint.setToolTipText((String) actionPrint.getValue(Action.SHORT_DESCRIPTION));
        buttonPrint.setEnabled(true);

        //------------------------------------------------------------------------------------------
        // Reload RSS Newsfeed

        buttonReload = new JButton();
        buttonReload.setBorderPainted(false);
        buttonReload.setIcon(RegistryModelUtilities.getCommonIcon(FILENAME_ICON_RELOAD));
        buttonReload.setToolTipText(TOOLTIP_RELOAD);

        buttonReload.addActionListener(new ActionListener()
            {
            public void actionPerformed(final ActionEvent event)
                {
                final String SOURCE = "SimpleNewsreaderPanel Reload button ";

                // Always stop any previous activity
                if (getNewsreaderWorker() != null)
                    {
                    getNewsreaderWorker().controlledStop(true, SWING_WORKER_STOP_DELAY);
                    getNewsreaderWorker().destroy();
                    setRunning(false);
                    }

                // This method is only ever called from the UI, so it is ok to show a MessageDialog
                if (InstrumentState.isOff(getHostInstrument()))
                    {
                    final String[] message =
                        {
                        "The Instrument must be switched on in order to receive the Newsfeed.",
                        "Please click the small green button on the control panel and try again."
                        };

                    Toolkit.getDefaultToolkit().beep();
                    JOptionPane.showMessageDialog(null,
                                                  message,
                                                  "Receive Newsfeed",
                                                  JOptionPane.WARNING_MESSAGE);
                    // This is the easiest way!
                    return;
                    }

                // We need a SwingWorker otherwise this would execute on the PortController Thread,
                // which would block while waiting for a Response...

                setNewsreaderWorker(new SwingWorker(REGISTRY.getThreadGroup(), SOURCE)
                    {
                    /*******************************************************************************
                     * Construct the Newsreader
                     *
                     * @return Object
                     */

                    public Object construct()
                        {
                        String strNews;
                        int intRetries;

                        strNews = null;
                        intRetries = RETRY_COUNT;

                        while ((isRunning())
                               && (Utilities.workerCanProceed(getHostInstrument().getDAO(), this))
                               && (strNews == null)
                               && (intRetries > 0))
                            {
                            // Update all Resources each time round
                            readResources();

                            strNews = RssHelper.getNews(getRssSubscriptions(),
                                                        getObservatoryClock());

                            if ((strNews.contains(RssHelper.MSG_NEWS_FAILED))
                                || (strNews.contains(RssHelper.MSG_RSS_NO_DATA)))
                                {
                                strNews = null;

                                // Wait a while before trying again, but allow interruptions by the User
                                Utilities.safeSleepPollWorker(UPDATE_DELAY,
                                                              getHostInstrument().getDAO(),
                                                              this);
                                intRetries--;
                                }
                            }

                        if (intRetries == 0)
                            {
                            strNews = MSG_UNABLE_RETRIEVE + RETRY_COUNT + MSG_RETRIES;
                            }

                        return (strNews);
                        }


                    /***********************************************************************************
                     * When the Thread stops.
                     */

                    public void finished()
                        {
                        setRunning(false);

                        if ((get() != null)
                            && (get() instanceof String))
                            {
                            setNewsText((String) get());
                            }
                        }
                    });

                setNewsText(MSG_PLEASE_WAIT);

                // Start the Thread we have prepared...
                setRunning(true);
                getNewsreaderWorker().start();
                }
            });

        //------------------------------------------------------------------------------------------
        // Remove RSS News

        buttonDispose = new JButton();
        buttonDispose.setBorderPainted(false);
        buttonDispose.setIcon(RegistryModelUtilities.getCommonIcon(FILENAME_ICON_DISPOSE));
        buttonDispose.setToolTipText(TOOLTIP_DISPOSE);

        buttonDispose.addActionListener(new ActionListener()
            {
            public void actionPerformed(final ActionEvent event)
                {
                if (!isRunning())
                    {
                    setNewsText(MSG_RELOAD);
                    ObservatoryInstrumentHelper.runGarbageCollector();
                    }
                else
                    {
                    Toolkit.getDefaultToolkit().beep();
                    }
                }
            });

        //------------------------------------------------------------------------------------------
        // Now assemble the Toolbar

        toolBar.removeAll();

        toolBar.addSeparator(DIM_TOOLBAR_SEPARATOR_BUTTON);
        toolBar.add(labelName);
        toolBar.add(Box.createHorizontalGlue());

        toolBar.add(buttonPageSetup);
        toolBar.addSeparator(DIM_TOOLBAR_SEPARATOR_BUTTON);

        toolBar.add(buttonPrint);
        toolBar.addSeparator(DIM_TOOLBAR_SEPARATOR_BUTTON);

        toolBar.add(buttonReload);
        toolBar.addSeparator(DIM_TOOLBAR_SEPARATOR_BUTTON);

        toolBar.add(buttonDispose);
        toolBar.addSeparator(DIM_TOOLBAR_SEPARATOR_BUTTON);

        NavigationUtilities.updateComponentTreeUI(toolBar);

        return (toolBar);
        }


    /**********************************************************************************************
     * Get the List of RSS Subscriptions.
     *
     * @return List<RssSubscription>
     */

    private List<RssSubscription> getRssSubscriptions()
        {
        return (this.listSubscriptions);
        }


    /**********************************************************************************************/
    /* News Headlines and Body                                                                    */
    /**********************************************************************************************
     * Get the News Body.
     *
     * @return JEditorPane
     */

    private JEditorPane getNewsBody()
        {
        return (this.textNewsBody);
        }


    /**********************************************************************************************
     * Get the News Text.
     *
     * @return String
     */

    public String getNewsText()
        {
        return (this.textNewsBody.getText());
        }


    /**********************************************************************************************
     * Get the ScrollPane which holds the News Body.
     *
     * @return JScrollPane
     */

    private JScrollPane getNewsBodyScrollPane()
        {
        return (this.scrollpaneNewsBody);
        }


    /**********************************************************************************************
     * Set the text on the Newsreader pane.
     *
     * @param text
     */

    private void setNewsText(final String text)
        {
        if (getNewsBody() != null)
            {
            getNewsBody().setText(HTML_PREFIX_FONT_COLOR + DEFAULT_COLOUR_TEXT.toHexFormat() + ">" + text + HTML_SUFFIX_FONT_COLOR);
            }
        }


    /**********************************************************************************************
     * Indicate if the Newsreader is running.
     *
     * @return boolean
     */

    private boolean isRunning()
        {
        return (this.boolNewsreaderRunning);
        }


    /**********************************************************************************************
     * Control the Newsreader state.
     *
     * @param running
     */

    private void setRunning(final boolean running)
        {
        this.boolNewsreaderRunning = running;
        }


    /**********************************************************************************************/
    /* Threads                                                                                    */
    /**********************************************************************************************
     * Get the SwingWorker which handles the Newsreader.
     *
     * @return SwingWorker
     */

    private SwingWorker getNewsreaderWorker()
        {
        return (this.workerNewsreader);
        }


    /**********************************************************************************************
     * Set the SwingWorker which handles the Newsreader.
     *
     * @param worker
     */

    private void setNewsreaderWorker(final SwingWorker worker)
        {
        this.workerNewsreader = worker;
        }


    /**********************************************************************************************
     * Read all the Resources required by the SimpleNewsreaderPanel.
     */

    private void readResources()
        {

        }
    }
