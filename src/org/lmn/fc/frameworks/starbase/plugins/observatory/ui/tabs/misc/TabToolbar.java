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
import org.lmn.fc.common.utilities.threads.SwingWorker;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.PublisherFrameUIComponentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.PublisherUIComponentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.TabToolbarInterface;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.model.registry.NavigationUtilities;
import org.lmn.fc.model.registry.RegistryModelUtilities;
import org.lmn.fc.ui.UIComponentPlugin;
import org.lmn.fc.ui.reports.ReportTableHelper;
import org.lmn.fc.ui.reports.ReportTablePlugin;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.print.PageFormat;
import java.awt.print.PrinterJob;


/***************************************************************************************************
 * TabToolbar.
 */

public class TabToolbar extends JToolBar
                        implements TabToolbarInterface
    {
    private static final long serialVersionUID = 6329314371450244841L;

    // Injections
    private final ObservatoryInstrumentInterface observatoryInstrument;
    private final String strTabName;
    private final String strTabIconFilename;
    private final FontInterface pluginFont;
    private final ColourInterface colourForeground;
    private final ColourInterface colourBackground;
    private final boolean boolDebugMode;

    // The Publication controlled by this Toolbar
    private PublisherUIComponentInterface uiPublisher;

    // Toolbar Components
    private final JButton buttonStart;
    private final JButton buttonFastBack;
    private final JButton buttonBack;
    private final JLabel labelPage;
    private final JLabel labelOf;
    private final JTextField textfieldPage;
    private final JButton buttonForward;
    private final JButton buttonFastForward;
    private final JButton buttonEnd;
    private final JButton buttonZoomOut;
    private final JButton buttonZoomIn;
    private final JButton buttonReload;
    private final JButton buttonRemove;
    private final JButton buttonPageSetup;
    private final JButton buttonPrint;


    /***********************************************************************************************
     * TabToolbar.
     *
     * @param obsinstrument
     * @param tabname
     * @param tabiconfilename
     * @param fontdata
     * @param colourforeground
     * @param colourbackground
     * @param debug
     */

    public TabToolbar(final ObservatoryInstrumentInterface obsinstrument,
                      final String tabname,
                      final String tabiconfilename,
                      final FontInterface fontdata,
                      final ColourInterface colourforeground,
                      final ColourInterface colourbackground,
                      final boolean debug)
        {
        // Injections
        this.observatoryInstrument = obsinstrument;
        this.strTabName = tabname;
        this.strTabIconFilename = tabiconfilename;
        this.pluginFont = fontdata;
        this.colourForeground = colourforeground;
        this.colourBackground = colourbackground;
        this.boolDebugMode = debug;

        // The Publication controlled by this Toolbar
        this.uiPublisher = null;

        // Toolbar Components
        this.buttonStart = new JButton();
        this.buttonFastBack = new JButton();
        this.buttonBack = new JButton();
        this.labelPage = new JLabel();
        this.textfieldPage = new JTextField();
        this.labelOf = new JLabel();
        this.buttonForward = new JButton();
        this.buttonFastForward = new JButton();
        this.buttonEnd = new JButton();
        this.buttonZoomIn = new JButton();
        this.buttonZoomOut = new JButton();
        this.buttonReload = new JButton();
        this.buttonRemove = new JButton();
        this.buttonPageSetup = new JButton();
        this.buttonPrint = new JButton();
        }


    /**********************************************************************************************/
    // UI                                                                                         */
    /***********************************************************************************************
     * Dispose of all UI components on the toolbar.
     */

    public void initialiseUI()
        {
        final String SOURCE = "TabToolbar.initialiseUI() ";
        final JLabel labelName;
        final ContextAction actionPageSetup;
        final ContextAction actionPrint;

        setFloatable(false);
        setMinimumSize(UIComponentPlugin.DIM_TOOLBAR_SIZE);
        setPreferredSize(UIComponentPlugin.DIM_TOOLBAR_SIZE);
        setMaximumSize(UIComponentPlugin.DIM_TOOLBAR_SIZE);
        setBackground(getBackgroundColour().getColor());

        //-------------------------------------------------------------------------------------
        // Initialise the Label

        labelName = new JLabel(getTabName(),
                               RegistryModelUtilities.getAtomIcon(getHostInstrument().getHostAtom(),
                                                                  getTabIconFilename()),
                               SwingConstants.LEFT)
            {
            private static final long serialVersionUID = 7580736117336162922L;

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
        labelName.setForeground(getForegroundColour().getColor());
        labelName.setIconTextGap(UIComponentPlugin.TOOLBAR_ICON_TEXT_GAP);

        //------------------------------------------------------------------------------------------
        // Navigation
        //------------------------------------------------------------------------------------------
        // Back to page 1

        buttonStart.setBorderPainted(false);
        buttonStart.setIcon(RegistryModelUtilities.getCommonIcon(UIComponentPlugin.FILENAME_ICON_START));
        buttonStart.setToolTipText(TOOLTIP_START);

        buttonStart.addActionListener(new ActionListener()
            {
            public void actionPerformed(final ActionEvent event)
                {
                if (getPublisherUI() != null)
                    {
                    getPublisherUI().setCurrentPage(1);

                    // Set page number display
                    updatePageIndicator(getPublisherUI().getCurrentPage(),
                                        getPublisherUI().getPageCount());
                    }
                }
            });

        //------------------------------------------------------------------------------------------
        // Back 10 icon

        buttonFastBack.setBorderPainted(false);
        buttonFastBack.setIcon(RegistryModelUtilities.getCommonIcon(UIComponentPlugin.FILENAME_ICON_FAST_BACKWARD));
        buttonFastBack.setToolTipText(TOOLTIP_BACK_TEN);

        buttonFastBack.addActionListener(new ActionListener()
        {
        public void actionPerformed(final ActionEvent event)
            {
            if (getPublisherUI() != null)
                {
                final int intNewPage;

                intNewPage = getPublisherUI().getCurrentPage() - 10;

                if ((intNewPage > 0)
                    && (intNewPage <= getPublisherUI().getPageCount()))
                    {
                    getPublisherUI().setCurrentPage(intNewPage);
                    }
                else
                    {
                    getPublisherUI().setCurrentPage(1);
                    }

                // Set page number display
                updatePageIndicator(getPublisherUI().getCurrentPage(),
                                    getPublisherUI().getPageCount());
                }
            }
        });

        //------------------------------------------------------------------------------------------
        // Back icon

        buttonBack.setBorderPainted(false);
        buttonBack.setIcon(RegistryModelUtilities.getCommonIcon(UIComponentPlugin.FILENAME_ICON_BACK));
        buttonBack.setToolTipText(TOOLTIP_BACK_ONE);

        buttonBack.addActionListener(new ActionListener()
            {
            public void actionPerformed(final ActionEvent event)
                {
                if (getPublisherUI() != null)
                    {
                    final int intNewPage;

                    intNewPage = getPublisherUI().getCurrentPage() - 1;

                    if ((intNewPage > 0)
                        && (intNewPage <= getPublisherUI().getPageCount()))
                        {
                        getPublisherUI().setCurrentPage(intNewPage);
                        }
                    else
                        {
                        getPublisherUI().setCurrentPage(1);
                        }

                    // Set page number display
                    updatePageIndicator(getPublisherUI().getCurrentPage(),
                                        getPublisherUI().getPageCount());
                    }
                }
            });

        //------------------------------------------------------------------------------------------
        // Page Number

        labelPage.setText(PAGE);
        labelPage.setForeground(getForegroundColour().getColor());
        labelPage.setFont(getFontData().getFont());

        textfieldPage.setText(PAGE_NUMBER_BLANK);
        textfieldPage.setMinimumSize(DIM_PAGE_NUMBER);
        textfieldPage.setPreferredSize(DIM_PAGE_NUMBER);
        textfieldPage.setMaximumSize(DIM_PAGE_NUMBER);
        textfieldPage.setForeground(getForegroundColour().getColor());
        textfieldPage.setFont(getFontData().getFont());

        textfieldPage.setEditable(true);
        textfieldPage.addActionListener(new ActionListener()
            {
            public void actionPerformed(final ActionEvent a)
                {
                if (getPublisherUI() != null)
                    {
                    final String strValue;
                    final int intNewPage;

                    strValue = textfieldPage.getText().trim();

                    try
                        {
                        intNewPage = Integer.parseInt(strValue);

                        if ((intNewPage > getPublisherUI().getPageCount())
                            || (intNewPage < 1))
                            {
                            getToolkit().beep();
                            return;
                            }

                        getPublisherUI().setCurrentPage(intNewPage);
                        }

                    catch (final NumberFormatException exception)
                        {
                        JOptionPane.showMessageDialog(null,
                                                      "[" + strValue + "] is Not a valid Value.\nPlease enter a number between 1 and "
                                                        + getPublisherUI().getPageCount());
                        }
                    }
                }
            });

        labelOf.setText(PAGE_OF_BLANK);
        labelOf.setForeground(getForegroundColour().getColor());
        labelOf.setFont(getFontData().getFont());

        //------------------------------------------------------------------------------------------
        // Forward icon

        buttonForward.setBorderPainted(false);
        buttonForward.setIcon(RegistryModelUtilities.getCommonIcon(UIComponentPlugin.FILENAME_ICON_FORWARD));
        buttonForward.setToolTipText(TOOLTIP_FORWARD_ONE);

        buttonForward.addActionListener(new ActionListener()
            {
            public void actionPerformed(final ActionEvent event)
                {
                if (getPublisherUI() != null)
                    {
                    final int intNewPage;

                    intNewPage = getPublisherUI().getCurrentPage() + 1;

                    if ((intNewPage > 0)
                        && (intNewPage <= getPublisherUI().getPageCount()))
                        {
                        getPublisherUI().setCurrentPage(intNewPage);
                        }
                    else
                        {
                        getPublisherUI().setCurrentPage(getPublisherUI().getPageCount());
                        }

                    // Set page number display
                    updatePageIndicator(getPublisherUI().getCurrentPage(),
                                        getPublisherUI().getPageCount());
                    }
                }
            });

        //------------------------------------------------------------------------------------------
        // Fast forward icon

        buttonFastForward.setBorderPainted(false);
        buttonFastForward.setIcon(RegistryModelUtilities.getCommonIcon(UIComponentPlugin.FILENAME_ICON_FAST_FORWARD));
        buttonFastForward.setToolTipText(TOOLTIP_FORWARD_TEN);

        buttonFastForward.addActionListener(new ActionListener()
            {
            public void actionPerformed(final ActionEvent event)
                {
                if (getPublisherUI() != null)
                    {
                    final int intNewPage;

                    intNewPage = getPublisherUI().getCurrentPage() + 10;

                    if ((intNewPage > 0)
                        && (intNewPage <= getPublisherUI().getPageCount()))
                        {
                        getPublisherUI().setCurrentPage(intNewPage);
                        }
                    else
                        {
                        getPublisherUI().setCurrentPage(getPublisherUI().getPageCount());
                        }

                    // Set page number display
                    updatePageIndicator(getPublisherUI().getCurrentPage(),
                                        getPublisherUI().getPageCount());
                    }
                }
            });

        //------------------------------------------------------------------------------------------
        // Goto last page

        buttonEnd.setBorderPainted(false);
        buttonEnd.setIcon(RegistryModelUtilities.getCommonIcon(UIComponentPlugin.FILENAME_ICON_END));
        buttonEnd.setToolTipText(TOOLTIP_END);

        buttonEnd.addActionListener(new ActionListener()
            {
            public void actionPerformed(final ActionEvent event)
                {
                if (getPublisherUI() != null)
                    {
                    getPublisherUI().setCurrentPage(getPublisherUI().getPageCount());

                    // Set page number display
                    updatePageIndicator(getPublisherUI().getCurrentPage(),
                                        getPublisherUI().getPageCount());
                    }
                }
            });

        //------------------------------------------------------------------------------------------
        // Zoom Out

        buttonZoomOut.setBorderPainted(false);
        buttonZoomOut.setIcon(RegistryModelUtilities.getCommonIcon(UIComponentPlugin.FILENAME_ICON_ZOOM_OUT));
        buttonZoomOut.setToolTipText(TOOLTIP_ZOOM_OUT);

        buttonZoomOut.addActionListener(new ActionListener()
            {
            public void actionPerformed(final ActionEvent event)
                {
                if (getPublisherUI() != null)
                    {
                    getPublisherUI().zoomOut(TabToolbarInterface.ZOOM_MIN,
                                             TabToolbarInterface.ZOOM_MAX,
                                             TabToolbarInterface.ZOOM_INCREMENT,
                                             getPublisherUI().getScaleFactor());
                    }
                }
            });

        //------------------------------------------------------------------------------------------
        // Zoom In

        buttonZoomIn.setBorderPainted(false);
        buttonZoomIn.setIcon(RegistryModelUtilities.getCommonIcon(UIComponentPlugin.FILENAME_ICON_ZOOM_IN));
        buttonZoomIn.setToolTipText(TOOLTIP_ZOOM_IN);

        buttonZoomIn.addActionListener(new ActionListener()
            {
            public void actionPerformed(final ActionEvent event)
                {
                if (getPublisherUI() != null)
                    {
                    getPublisherUI().zoomIn(TabToolbarInterface.ZOOM_MIN,
                                            TabToolbarInterface.ZOOM_MAX,
                                            TabToolbarInterface.ZOOM_INCREMENT,
                                            getPublisherUI().getScaleFactor());
                    }
                }
            });

        //------------------------------------------------------------------------------------------
        // Reload Document

        buttonReload.setBorderPainted(false);
        buttonReload.setIcon(RegistryModelUtilities.getCommonIcon(UIComponentPlugin.FILENAME_ICON_RELOAD));
        buttonReload.setToolTipText(TOOLTIP_RELOAD);

        buttonReload.addActionListener(new ActionListener()
            {
            public void actionPerformed(final ActionEvent event)
                {
                if (getPublisherUI() != null)
                    {
                    getPublisherUI().reloadDocument();
                    }
                }
            });

        //------------------------------------------------------------------------------------------
        // Remove Document

        buttonRemove.setBorderPainted(false);
        buttonRemove.setIcon(RegistryModelUtilities.getCommonIcon(UIComponentPlugin.FILENAME_ICON_REMOVE));
        buttonRemove.setToolTipText(TOOLTIP_REMOVE);

        buttonRemove.addActionListener(new ActionListener()
            {
            public void actionPerformed(final ActionEvent event)
                {
                if (getPublisherUI() != null)
                    {
                    getPublisherUI().removeDocument();

                    textfieldPage.setText(PAGE_NUMBER_BLANK);
                    labelOf.setText(PAGE_OF_BLANK);
                    }
                }
            });

        //-------------------------------------------------------------------------------------
        // Printing
        //-------------------------------------------------------------------------------------
        // Page Setup

        buttonPageSetup.setBorderPainted(false);
        buttonPageSetup.setHideActionText(true);

        actionPageSetup = new ContextAction(ReportTablePlugin.PREFIX_PAGE_SETUP + MSG_TAB_VIEWER,
                                            RegistryModelUtilities.getCommonIcon(UIComponentPlugin.FILENAME_ICON_PAGE_SETUP),
                                            ReportTablePlugin.PREFIX_PAGE_SETUP + MSG_TAB_VIEWER,
                                            KeyEvent.VK_S,
                                            false,
                                            true)
            {
            final static String SOURCE = "ContextAction:PageSetup ";
            private static final long serialVersionUID = 6802400471966299436L;


            public void actionPerformed(final ActionEvent event)
                {
                if (getPublisherUI() != null)
                    {
                    final PrinterJob printerJob;
                    final PageFormat pageFormat;

                    printerJob = PrinterJob.getPrinterJob();
                    pageFormat = printerJob.pageDialog(getPublisherUI().getPageFormat());

                    if (pageFormat != null)
                        {
                        System.out.println("SET PAGE FORMAT");
                        getPublisherUI().setPageFormat(pageFormat);
                        }
                    }
                else
                    {
                    LOGGER.error(SOURCE + "Publisher Viewer UI unexpectedly NULL");
                    }
                }
            };

        buttonPageSetup.setAction(actionPageSetup);
        buttonPageSetup.setToolTipText((String) actionPageSetup.getValue(Action.SHORT_DESCRIPTION));
        buttonPageSetup.setEnabled(true);

        //-----------------------------------------------------------------------------------------
        // Print

        buttonPrint.setBorderPainted(false);
        buttonPrint.setBorder(UIComponentPlugin.BORDER_BUTTON);
        buttonPrint.setHideActionText(true);
        buttonPrint.setEnabled(false);

        actionPrint = new ContextAction(ReportTablePlugin.PREFIX_PRINT + MSG_TAB_VIEWER,
                                        RegistryModelUtilities.getCommonIcon(UIComponentPlugin.FILENAME_ICON_PRINT),
                                        ReportTablePlugin.PREFIX_PRINT + MSG_TAB_VIEWER,
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
                            MSG_TAB_VIEWER_PRINTED,
                            PublisherFrameUIComponentInterface.MSG_PRINT_CANCELLED
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
                                                          RegistryModelUtilities.getCommonIcon(UIComponentPlugin.FILENAME_ICON_DIALOG_PRINT));
                            }
                        else
                            {
                            JOptionPane.showMessageDialog(null,
                                                          strSuccess[1],
                                                          DIALOG_PRINT,
                                                          JOptionPane.INFORMATION_MESSAGE,
                                                          RegistryModelUtilities.getCommonIcon(UIComponentPlugin.FILENAME_ICON_DIALOG_PRINT));
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
                                                  ReportTablePlugin.PREFIX_PRINT + MSG_TAB_VIEWER,
                                                  JOptionPane.WARNING_MESSAGE,
                                                  RegistryModelUtilities.getCommonIcon(UIComponentPlugin.FILENAME_ICON_DIALOG_PRINT));
                    boolSuccess = false;
                    }
                else
                    {
                    final PrinterJob printerJob;

                    printerJob = PrinterJob.getPrinterJob();

                    if ((getPublisherUI() != null)
                        && (printerJob.printDialog()))
                        {
                        final PageFormat pageFormat;

                        pageFormat = getPublisherUI().getPageFormat();

                        if ((pageFormat != null)
                            && (getPublisherUI().getPrintable() != null))
                            {
                            System.out.println("SET PRINTABLE format=" + pageFormat);

                            // The PublisherUI is Printable
                            // ToDo Header & Footer MessageFormats
                            printerJob.setPrintable(getPublisherUI().getPrintable(),
                                                    pageFormat);
//                            try
//                                {
//                                System.out.println("PRINT!!!!!!!!!!!");
//                                DocPrintJob job = new DocPrintJob()
//                                {
//                                };
//                                javax.print.ServiceUI.printDialog()
//
//                                PdfBook pdfBook = new PdfBook(decodePdf, printingDevice, attributeSet);
//                                SimpleDoc doc = new SimpleDoc(pdfBook, DocFlavor.SERVICE_FORMATTED.PAGEABLE, null);
//
//                                printerJob.print();
//                                boolSuccess = true;
//                                }
//
//                            catch (final PrinterException exception)
//                                {
//                                LOGGER.error(SOURCE + "[exception=" + exception.getMessage() + "]");
//                                boolSuccess = false;
//                                }
                            boolSuccess = false;
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

        //-------------------------------------------------------------------------------------
        // Put it all together

        removeAll();
        addSeparator(UIComponentPlugin.DIM_TOOLBAR_SEPARATOR_BUTTON);
        add(labelName);

        addSeparator(UIComponentPlugin.DIM_TOOLBAR_SEPARATOR);
        add(Box.createHorizontalGlue());
        add(buttonStart);
        add(buttonFastBack);
        add(buttonBack);

        // Page count in middle of forward and back
        addSeparator(UIComponentPlugin.DIM_TOOLBAR_SEPARATOR);
        add(labelPage);
        addSeparator(UIComponentPlugin.DIM_TOOLBAR_SEPARATOR_HALFWIDTH);
        add(textfieldPage);
        addSeparator(UIComponentPlugin.DIM_TOOLBAR_SEPARATOR_HALFWIDTH);
        add(labelOf);

        addSeparator(UIComponentPlugin.DIM_TOOLBAR_SEPARATOR);
        add(buttonForward);
        add(buttonFastForward);
        add(buttonEnd);

        addSeparator(UIComponentPlugin.DIM_TOOLBAR_SEPARATOR);
        add(buttonZoomOut);
        add(buttonZoomIn);

        addSeparator(UIComponentPlugin.DIM_TOOLBAR_SEPARATOR);
        add(buttonReload);
        add(buttonRemove);

        addSeparator(UIComponentPlugin.DIM_TOOLBAR_SEPARATOR);
        add(buttonPageSetup);
        add(buttonPrint);
        addSeparator(UIComponentPlugin.DIM_TOOLBAR_SEPARATOR);

        NavigationUtilities.updateComponentTreeUI(this);
        }


    /***********************************************************************************************
     * Dispose of all UI components on the toolbar.
     */

    public void disposeUI()
        {
        removeAll();
        }


    /***********************************************************************************************
     * Get the host Publisher UIComponent.
     *
     * @return PublisherUIComponentInterface
     */

    private PublisherUIComponentInterface getPublisherUI()
        {
        return (this.uiPublisher);
        }


    /***********************************************************************************************
     * Set the PublisherUI controlled by this Toolbar.
     *
     * @param publisher
     */

    public void setPublisherUI(final PublisherUIComponentInterface publisher)
        {
        this.uiPublisher = publisher;
        }


    /**********************************************************************************************/
    // UI State                                                                                   */
    /***********************************************************************************************
     * Update the page indicator with the current page and total count.
     *
     * @param currentpage
     * @param pagecount
     */

    public void updatePageIndicator(final int currentpage,
                                    final int pagecount)
        {
        if (getPublisherUI() != null)
            {
            textfieldPage.setText(String.valueOf(getPublisherUI().getCurrentPage()));
            labelOf.setText(TabToolbarInterface.PAGE_OF_BLANK + getPublisherUI().getPageCount());
            }
        else
            {
            textfieldPage.setText("000");
            labelOf.setText(TabToolbarInterface.PAGE_OF_BLANK + " 0");
            }
        }


    /**********************************************************************************************/
    /* Injections                                                                                 */
    /***********************************************************************************************
     * Get the host ObservatoryInstrument.
     *
     * @return ObservatoryInstrumentInterface
     */

    private ObservatoryInstrumentInterface getHostInstrument()
        {
        return (this.observatoryInstrument);
        }


    /***********************************************************************************************
     * Get the Tab Name.
     *
     * @return String
     */

    private String getTabName()
        {
        return (this.strTabName);
        }


    /***********************************************************************************************
     * Get the Tab Icon Filename.
     *
     * @return String
     */

    private String getTabIconFilename()
        {
        return (this.strTabIconFilename);
        }


    /***********************************************************************************************
     * Get the FontDataType.
     *
     * @return FontPlugin
     */

    private FontInterface getFontData()
        {
        return (this.pluginFont);
        }


    /***********************************************************************************************
     * Get the Foreground ColourDataType.
     *
     * @return ColourPlugin
     */

    private ColourInterface getForegroundColour()
        {
        return (this.colourForeground);
        }


    /***********************************************************************************************
     * Get the Background ColourDataType.
     *
     * @return ColourPlugin
     */

    private ColourInterface getBackgroundColour()
        {
        return (this.colourBackground);
        }


    /***********************************************************************************************
     * Indicate if we are in debug mode.
     *
     * @return boolean
     */

    private boolean isDebug()
        {
        return (this.boolDebugMode);
        }
    }
