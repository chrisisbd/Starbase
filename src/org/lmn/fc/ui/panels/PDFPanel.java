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

package org.lmn.fc.ui.panels;

import org.jpedal.PdfDecoder;
import org.jpedal.exception.PdfException;
import org.lmn.fc.common.utilities.threads.SwingWorker;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentHelper;
import org.lmn.fc.model.registry.RegistryModelUtilities;
import org.lmn.fc.ui.UIComponentPlugin;
import org.lmn.fc.ui.components.BlankUIComponent;
import org.lmn.fc.ui.components.UIComponent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;


/**************************************************************************************************
 * PDFPanel.
 */

public final class PDFPanel extends UIComponent
                            implements UIComponentPlugin
    {
    private static final String TOOLTIP_START = "Back to first page";
    private static final String TOOLTIP_BACK_ONE = "Back one page";
    private static final String TOOLTIP_BACK_TEN = "Back ten pages";
    private static final String TOOLTIP_FORWARD_ONE = "Forward one page";
    private static final String TOOLTIP_FORWARD_TEN = "Forward ten pages";
    private static final String TOOLTIP_END = "Forward to last page";
    private static final String TOOLTIP_ZOOM_IN = "Zoom In";
    private static final String TOOLTIP_ZOOM_OUT = "Zoom Out";
    private static final String TOOLTIP_REMOVE = "Remove Document";
    private static final String TOOLTIP_RELOAD = "Reload Document";

    private static final String PAGE_NUMBER_BLANK = "   ";
    private static final String PAGE_OF_BLANK = "of ";

    private static final float ZOOM_INITIAL = 1.4f;
    private static final float ZOOM_INCREMENT = 0.5f;
    private static final float ZOOM_MIN = 0.6f;
    private static final float ZOOM_MAX = 3.0f;
    private static final Dimension DIM_PAGE_NUMBER = new Dimension(30, HEIGHT_TOOLBAR_ICON);

    // Injections
    private final URL urlPDF;

    private JPanel panelDisplay;
    private JPanel panelWait;
    private PdfDecoder pdfDecoder;

    private JLabel labelPage;
    private JLabel labelOf;
    private JTextField textfieldPage;
    private JScrollPane scrollPane;

    private int intCurrentPage;
    private float floatScaleFactor;

    // The Thread for handling PdfDecoder
    private org.lmn.fc.common.utilities.threads.SwingWorker workerPdfDecoder;

    private boolean boolPdfDecoderRunning;


    /***********************************************************************************************
     * PDFPanel.
     *
     * @param url
     */

    public PDFPanel(final URL url)
        {
        super();

        this.urlPDF = url;

        this.panelDisplay = null;
        this.panelWait = null;
        this.pdfDecoder = null;

        this.intCurrentPage = 1;
        this.floatScaleFactor = ZOOM_INITIAL;

        this.workerPdfDecoder = null;
        this.boolPdfDecoderRunning = false;
        }


    /***********************************************************************************************
     * Initialise the PDFPanel.
     */

    public final void initialiseUI()
        {
        final String SOURCE = "PDFPanel.initialiseUI() ";
        final JToolBar toolBar;

        setBackground(DEFAULT_COLOUR_CANVAS.getColor());

        labelPage = new JLabel("Page");
        labelPage.setForeground(DEFAULT_COLOUR_TEXT.getColor());
        labelPage.setFont(DEFAULT_FONT.getFont());

        textfieldPage = new JTextField(PAGE_NUMBER_BLANK);
        textfieldPage.setMinimumSize(DIM_PAGE_NUMBER);
        textfieldPage.setPreferredSize(DIM_PAGE_NUMBER);
        textfieldPage.setMaximumSize(DIM_PAGE_NUMBER);
        textfieldPage.setForeground(DEFAULT_COLOUR_TEXT.getColor());
        textfieldPage.setFont(DEFAULT_FONT.getFont());

        labelOf = new JLabel(PAGE_OF_BLANK);
        labelOf.setForeground(DEFAULT_COLOUR_TEXT.getColor());
        labelOf.setFont(DEFAULT_FONT.getFont());

        setDisplayPanel(new JPanel());
        getDisplayPanel().setBackground(DEFAULT_COLOUR_CANVAS.getColor());
        getDisplayPanel().setLayout(new BorderLayout());

        setWaitPanel(new BlankUIComponent(MSG_LOADING_PLEASE_WAIT));
        getDisplayPanel().add(getWaitPanel(), BorderLayout.CENTER);

        pdfDecoder = new PdfDecoder(true);
        getPdfDecoder().setBackground(DEFAULT_COLOUR_CANVAS.getColor());
        getPdfDecoder().useHiResScreenDisplay(true);

        // Ensure non-embedded fonts map to sensible replacements
        PdfDecoder.setFontReplacements(getPdfDecoder());

        toolBar = createNavigatorToolbar();
        this.add(toolBar, BorderLayout.NORTH);

        scrollPane = new JScrollPane(getDisplayPanel());
        getScrollPane().setPreferredSize(DIM_UNIVERSE);
        getScrollPane().setMaximumSize(DIM_UNIVERSE);
        getScrollPane().setBackground(DEFAULT_COLOUR_CANVAS.getColor());
        getScrollPane().setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        getScrollPane().setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        this.add(getScrollPane(), BorderLayout.CENTER);

        // Stop any existing SwingWorker
        if (getPdfDecoderWorker() != null)
            {
            getPdfDecoderWorker().controlledStop(true, SWING_WORKER_STOP_DELAY);
            getPdfDecoderWorker().destroy();
            }
        }


    /***********************************************************************************************
     * Run this PDFPanel.
     */

    public void runUI()
        {
        final String SOURCE = "PDFPanel.runUI() ";

        try
            {
            // Set the page number display
            intCurrentPage = 1;

            if ((getPdfDecoder() != null)
                && (!getPdfDecoder().isOpen())
                && (getURL() != null))
                {
                getDisplayPanel().removeAll();
                getDisplayPanel().add(getWaitPanel(), BorderLayout.CENTER);

                getScrollPane().setCursor(new Cursor(Cursor.WAIT_CURSOR));
                getWaitPanel().setCursor(new Cursor(Cursor.WAIT_CURSOR));
                getDisplayPanel().setCursor(new Cursor(Cursor.WAIT_CURSOR));

                getWaitPanel().invalidate();
                getDisplayPanel().invalidate();
                repaint();

                // Now try to open the PDF on another Thread...
                // Make a SwingWorker for later

                setPdfDecoderWorker(new SwingWorker(REGISTRY.getThreadGroup(), SOURCE)
                    {
                    /**************************************************************************************
                     * Run the PdfDecoder
                     *
                     * @return Object
                     */

                    public Object construct()
                        {
                        try
                            {
                            if (getPdfDecoder() != null)
                                {
                                // This opens the PDF and reads its internal details
                                getPdfDecoder().openPdfFileFromURL(getURL().toString());
                                getPdfDecoder().decodePage(intCurrentPage);
                                getPdfDecoder().waitForDecodingToFinish();
                                }
                            }

                        catch (PdfException exception)
                            {
                            LOGGER.error(SOURCE + exception.getMessage());
                            }

                        catch (Exception exception)
                            {
                            LOGGER.error(SOURCE + exception.getMessage());
                            }

                        return (getPdfDecoder());
                        }

                    /**************************************************************************************
                     * When the Thread stops.
                     */

                    public void finished()
                        {
                        if (getPdfDecoder() != null)
                            {
                            try
                                {
                                // Now show the loaded file
                                getDisplayPanel().removeAll();
                                getDisplayPanel().add(getPdfDecoder(), BorderLayout.CENTER);

                                // Open page 1 at 100% scaling
                                getPdfDecoder().setPageParameters(floatScaleFactor, intCurrentPage);

                                getWaitPanel().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                                getDisplayPanel().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                                getScrollPane().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

                                getPdfDecoder().invalidate();
                                getDisplayPanel().invalidate();
                                repaint();

                                // Adjust Zoom factor to try to fit the screen width
                                //                            if (getPdfDecoder().getCurrentPageCoords().getWidth() < getScrollPane().getWidth())
                                //                                {
                                //                                while ((getPdfDecoder().getCurrentPageCoords().getWidth() < getScrollPane().getWidth())
                                //                                    && (floatScaleFactor <= ZOOM_MAX))
                                //                                    {
                                //                                    System.out.println("Magnify [pdf=" + getPdfDecoder().getCurrentPageCoords().getWidth()
                                //                                    + "] [pane=" + getScrollPane().getWidth() + "] [scale=" + floatScaleFactor + "]");
                                //
                                //                                    floatScaleFactor += ZOOM_INCREMENT;
                                //                                    getPdfDecoder().setPageParameters(floatScaleFactor, intCurrentPage);
                                //                                    getPdfDecoder().invalidate();
                                //                                    repaint();
                                //                                    }
                                //                                }
                                //                            else
                                //                                {
                                //                                while ((getPdfDecoder().getCurrentPageCoords().getWidth() > getScrollPane().getWidth())
                                //                                    && (floatScaleFactor >= ZOOM_MIN))
                                //                                    {
                                //                                    System.out.println("Minify [pdf=" + getPdfDecoder().getCurrentPageCoords().getWidth()
                                //                                    + "] [pane=" + getScrollPane().getWidth() + "] [scale=" + floatScaleFactor + "]");
                                //
                                //                                    floatScaleFactor -= ZOOM_INCREMENT;
                                //                                    getPdfDecoder().setPageParameters(floatScaleFactor, intCurrentPage);
                                //                                    getPdfDecoder().invalidate();
                                //                                    repaint();
                                //                                    }
                                //                                }

                                textfieldPage.setText(String.valueOf(intCurrentPage));
                                labelOf.setText(PAGE_OF_BLANK + getPdfDecoder().getPageCount());
                                }

                            catch (Exception exception)
                                {
                                // Absorb, just in case!
                                // Sometimes we see a NullPointerException
                                }
                            }

                        setRunning(false);
                        }
                    });

                // Start the Thread we have prepared...
                setRunning(true);
                getPdfDecoderWorker().start();
                }
            else
                {
                textfieldPage.setText("000");
                labelOf.setText(PAGE_OF_BLANK + " 0");
                }
            }

        catch (Exception exception)
            {
            LOGGER.error(SOURCE + exception.getMessage());
            }
        }


    /***********************************************************************************************
     * Stop this PDFPanel.
     */

    public void stopUI()
        {
        // Stop any existing SwingWorker
        if (getPdfDecoderWorker() != null)
            {
            getPdfDecoderWorker().controlledStop(true, SWING_WORKER_STOP_DELAY);
            getPdfDecoderWorker().destroy();
            }

        if (getPdfDecoder() != null)
            {
            getPdfDecoder().closePdfFile();
            }

        // Keep the Wait panel

        if (getDisplayPanel() != null)
            {
            getDisplayPanel().removeAll();
            }
        }


    /***********************************************************************************************
     * Dispose of all components of this PDFPanel.
     */

    public void disposeUI()
        {
        // Stop any existing SwingWorker
        if (getPdfDecoderWorker() != null)
            {
            getPdfDecoderWorker().controlledStop(true, SWING_WORKER_STOP_DELAY);
            getPdfDecoderWorker().destroy();
            }

        if (getPdfDecoder() != null)
            {
            getPdfDecoder().closePdfFile();
            }

        if (getWaitPanel() != null)
            {
            getWaitPanel().removeAll();
            setWaitPanel(null);
            }

        if (getDisplayPanel() != null)
            {
            getDisplayPanel().removeAll();
            setDisplayPanel(null);
            }

        removeAll();
        }


    /**********************************************************************************************/
    /* Utilties                                                                                   */
    /***********************************************************************************************
     * Create the Navigator Panel components.
     *
     * @return JToolBar
     */

    private JToolBar createNavigatorToolbar()
        {
        final JToolBar toolBar;

        toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setMinimumSize(DIM_TOOLBAR_SIZE);
        toolBar.setPreferredSize(DIM_TOOLBAR_SIZE);
        toolBar.setMaximumSize(DIM_TOOLBAR_SIZE);
        toolBar.setBackground(UIComponentPlugin.DEFAULT_COLOUR_TAB_BACKGROUND.getColor());

        toolBar.add(Box.createHorizontalGlue());
        toolBar.addSeparator(DIM_TOOLBAR_SEPARATOR);

        //------------------------------------------------------------------------------------------
        // Back to page 1

        final JButton start = new JButton();
        start.setBorderPainted(false);
        start.setIcon(RegistryModelUtilities.getCommonIcon(FILENAME_ICON_START));
        start.setToolTipText(TOOLTIP_START);
        toolBar.add(start);

        start.addActionListener(new ActionListener()
            {
            public void actionPerformed(final ActionEvent event)
                {
                if ((getPdfDecoder() != null)
                    && (getPdfDecoder().isOpen())
                    && (getURL() != null)
                    && (intCurrentPage != 1))
                    {
                    intCurrentPage = 1;
                    try
                        {
                        getPdfDecoder().decodePage(intCurrentPage);
                        getPdfDecoder().waitForDecodingToFinish();
                        getPdfDecoder().invalidate();
                        repaint();
                        }

                    catch (Exception exception)
                        {
                        LOGGER.error("back to page 1");
                        exception.printStackTrace();
                        }

                    //set page number display
                    textfieldPage.setText(String.valueOf(intCurrentPage));
                    }
                }
            });

        //------------------------------------------------------------------------------------------
        // Back 10 icon

        final JButton fback = new JButton();
        fback.setBorderPainted(false);
        fback.setIcon(RegistryModelUtilities.getCommonIcon(FILENAME_ICON_FAST_BACKWARD));
        fback.setToolTipText(TOOLTIP_BACK_TEN);
        toolBar.add(fback);

        fback.addActionListener(new ActionListener()
            {
            public void actionPerformed(final ActionEvent event)
                {
                if ((getPdfDecoder() != null)
                    && (getPdfDecoder().isOpen())
                    && (getURL() != null)
                    && (intCurrentPage > 10))
                    {
                    intCurrentPage -= 10;
                    try
                        {
                        getPdfDecoder().decodePage(intCurrentPage);
                        getPdfDecoder().waitForDecodingToFinish();
                        getPdfDecoder().invalidate();
                        repaint();
                        }

                    catch (Exception exception)
                        {
                        LOGGER.error("back 10 pages");
                        exception.printStackTrace();
                        }

                    //            set page number display
                    textfieldPage.setText(String.valueOf(intCurrentPage));
                    }
                }
            });

        //------------------------------------------------------------------------------------------
        // Back icon

        final JButton back = new JButton();
        back.setBorderPainted(false);
        back.setIcon(RegistryModelUtilities.getCommonIcon(FILENAME_ICON_BACK));
        back.setToolTipText(TOOLTIP_BACK_ONE);
        toolBar.add(back);

        back.addActionListener(new ActionListener()
            {
            public void actionPerformed(final ActionEvent event)
                {
                if ((getPdfDecoder() != null)
                    && (getPdfDecoder().isOpen())
                    && (getURL() != null)
                    && (intCurrentPage > 1))
                    {
                    intCurrentPage -= 1;
                    try
                        {
                        if (getPdfDecoder() != null)
                            {
                            getPdfDecoder().decodePage(intCurrentPage);
                            getPdfDecoder().waitForDecodingToFinish();
                            getPdfDecoder().invalidate();
                            repaint();
                            }
                        }

                    catch (Exception exception)
                        {
                        LOGGER.error("back 1 page");
                        exception.printStackTrace();
                        }

                    //          set page number display
                    textfieldPage.setText(String.valueOf(intCurrentPage));
                    }
                }
            });

        //------------------------------------------------------------------------------------------
        // Page Number entry

        textfieldPage.setEditable(true);
        textfieldPage.addActionListener(new ActionListener()
            {
            public void actionPerformed(final ActionEvent a)
                {
                if ((getPdfDecoder() != null)
                    && (getPdfDecoder().isOpen()))
                    {
                    final String value;
                    final int newPage;

                    value = textfieldPage.getText().trim();

                    // Allow for bum values
                    try
                        {
                        newPage = Integer.parseInt(value);

                        if ((newPage > getPdfDecoder().getPageCount()) | (newPage < 1))
                            {
                            return;
                            }

                        intCurrentPage = newPage;

                        try
                            {
                            getPdfDecoder().decodePage(intCurrentPage);
                            getPdfDecoder().waitForDecodingToFinish();
                            getPdfDecoder().invalidate();
                            repaint();
                            }

                        catch (Exception exception)
                            {
                            LOGGER.error("page number entered");
                            exception.printStackTrace();
                            }

                        }

                    catch (NumberFormatException exception)
                        {
                        JOptionPane.showMessageDialog(null,
                                                      '[' + value + "] is Not a valid Value.\nPlease enter a number between 1 and " + getPdfDecoder().getPageCount());
                        }
                    }
                }
            });

        //------------------------------------------------------------------------------------------
        // Put page count in middle of forward and back

        toolBar.addSeparator(DIM_TOOLBAR_SEPARATOR);
        toolBar.add(labelPage);
        toolBar.addSeparator(DIM_TOOLBAR_SEPARATOR_HALFWIDTH);
        toolBar.add(textfieldPage);
        toolBar.addSeparator(DIM_TOOLBAR_SEPARATOR_HALFWIDTH);
        toolBar.add(labelOf);
        toolBar.addSeparator(DIM_TOOLBAR_SEPARATOR);

        //------------------------------------------------------------------------------------------
        // Forward icon

        final JButton forward = new JButton();
        forward.setBorderPainted(false);
        forward.setIcon(RegistryModelUtilities.getCommonIcon(FILENAME_ICON_FORWARD));
        forward.setToolTipText(TOOLTIP_FORWARD_ONE);
        toolBar.add(forward);

        forward.addActionListener(new ActionListener()
            {
            public void actionPerformed(final ActionEvent event)
                {
                if ((getPdfDecoder() != null)
                    && (getPdfDecoder().isOpen())
                    && (getURL() != null)
                    && (intCurrentPage < getPdfDecoder().getPageCount()))
                    {
                    intCurrentPage += 1;
                    try
                        {
                        getPdfDecoder().decodePage(intCurrentPage);
                        getPdfDecoder().waitForDecodingToFinish();
                        getPdfDecoder().invalidate();
                        repaint();
                        }

                    catch (Exception exception)
                        {
                        LOGGER.error(TOOLTIP_FORWARD_ONE);
                        exception.printStackTrace();
                        }

                    //        set page number display
                    textfieldPage.setText(String.valueOf(intCurrentPage));
                    }
                }
            });

        //------------------------------------------------------------------------------------------
        // Fast forward icon

        final JButton fforward = new JButton();
        fforward.setBorderPainted(false);
        fforward.setIcon(RegistryModelUtilities.getCommonIcon(FILENAME_ICON_FAST_FORWARD));
        fforward.setToolTipText(TOOLTIP_FORWARD_TEN);
        toolBar.add(fforward);

        fforward.addActionListener(new ActionListener()
            {
            public void actionPerformed(final ActionEvent event)
                {
                if ((getPdfDecoder() != null)
                    && (getPdfDecoder().isOpen())
                    && (getURL() != null)
                    && (intCurrentPage < getPdfDecoder().getPageCount() - 9))
                    {
                    intCurrentPage += 10;
                    try
                        {
                        getPdfDecoder().decodePage(intCurrentPage);
                        getPdfDecoder().waitForDecodingToFinish();
                        getPdfDecoder().invalidate();
                        repaint();
                        }

                    catch (Exception exception)
                        {
                        LOGGER.error("forward 10 pages");
                        exception.printStackTrace();
                        }

                    //        set page number display
                    textfieldPage.setText(String.valueOf(intCurrentPage));
                    }
                }
            });

        //------------------------------------------------------------------------------------------
        // Goto last page

        final JButton end = new JButton();
        end.setBorderPainted(false);
        end.setIcon(RegistryModelUtilities.getCommonIcon(FILENAME_ICON_END));
        end.setToolTipText(TOOLTIP_END);
        toolBar.add(end);

        end.addActionListener(new ActionListener()
            {
            public void actionPerformed(final ActionEvent event)
                {
                if ((getPdfDecoder() != null)
                    && (getPdfDecoder().isOpen())
                    && (getURL() != null)
                    && (intCurrentPage < getPdfDecoder().getPageCount()))
                    {
                    intCurrentPage = getPdfDecoder().getPageCount();

                    try
                        {
                        getPdfDecoder().decodePage(intCurrentPage);
                        getPdfDecoder().waitForDecodingToFinish();
                        getPdfDecoder().invalidate();
                        repaint();
                        }

                    catch (Exception exception)
                        {
                        LOGGER.error("forward to last page");
                        exception.printStackTrace();
                        }

                    // set page number display
                    textfieldPage.setText(String.valueOf(intCurrentPage));
                    }
                }
            });

        //------------------------------------------------------------------------------------------
        // Zoom Out

        final JButton buttonZoomOut;

        buttonZoomOut = new JButton();
        buttonZoomOut.setBorderPainted(false);
        buttonZoomOut.setIcon(RegistryModelUtilities.getCommonIcon(FILENAME_ICON_ZOOM_OUT));
        buttonZoomOut.setToolTipText(TOOLTIP_ZOOM_OUT);
        toolBar.addSeparator(DIM_TOOLBAR_SEPARATOR);
        toolBar.add(buttonZoomOut);

        buttonZoomOut.addActionListener(new ActionListener()
            {
            public void actionPerformed(final ActionEvent event)
                {
                if ((getPdfDecoder() != null)
                    && (getPdfDecoder().isOpen()))
                    {
                    try
                        {
                        floatScaleFactor -= ZOOM_INCREMENT;

                        if (floatScaleFactor <= ZOOM_MIN)
                            {
                            floatScaleFactor = ZOOM_MIN;
                            Toolkit.getDefaultToolkit().beep();
                            }

                        getScrollPane().setCursor(new Cursor(Cursor.WAIT_CURSOR));
                        getPdfDecoder().setCursor(new Cursor(Cursor.WAIT_CURSOR));
                        getPdfDecoder().setPageParameters(floatScaleFactor, intCurrentPage);
                        getPdfDecoder().invalidate();
                        repaint();
                        debugSizes();
                        getPdfDecoder().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                        getScrollPane().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                        }

                    catch (Exception exception)
                        {
                        LOGGER.error("PDFPanel --> ZoomOut [exception=" + exception.getMessage() + "]");
                        }
                    }
                }
            });

        //------------------------------------------------------------------------------------------
        // Zoom In

        final JButton buttonZoomIn;

        buttonZoomIn = new JButton();
        buttonZoomIn.setBorderPainted(false);
        buttonZoomIn.setIcon(RegistryModelUtilities.getCommonIcon(FILENAME_ICON_ZOOM_IN));
        buttonZoomIn.setToolTipText(TOOLTIP_ZOOM_IN);
        toolBar.add(buttonZoomIn);

        buttonZoomIn.addActionListener(new ActionListener()
            {
            public void actionPerformed(final ActionEvent event)
                {
                if ((getPdfDecoder() != null)
                    && (getPdfDecoder().isOpen()))
                    {
                    try
                        {
                        floatScaleFactor += ZOOM_INCREMENT;

                        if (floatScaleFactor >= ZOOM_MAX)
                            {
                            floatScaleFactor = ZOOM_MAX;
                            Toolkit.getDefaultToolkit().beep();
                            }

                        getScrollPane().setCursor(new Cursor(Cursor.WAIT_CURSOR));
                        getPdfDecoder().setCursor(new Cursor(Cursor.WAIT_CURSOR));
                        getPdfDecoder().setPageParameters(floatScaleFactor, intCurrentPage);
                        getPdfDecoder().invalidate();
                        repaint();
                        debugSizes();
                        getPdfDecoder().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                        getScrollPane().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                        }

                    catch (Exception exception)
                        {
                        LOGGER.error("PDFPanel --> ZoomIn [exception=" + exception.getMessage() + "]");
                        }
                    }
                }
            });

        //------------------------------------------------------------------------------------------
        // Reload Document

        final JButton buttonReload;

        buttonReload = new JButton();
        buttonReload.setBorderPainted(false);
        buttonReload.setIcon(RegistryModelUtilities.getCommonIcon(FILENAME_ICON_RELOAD));
        buttonReload.setToolTipText(TOOLTIP_RELOAD);
        toolBar.addSeparator(DIM_TOOLBAR_SEPARATOR);
        toolBar.add(buttonReload);

        buttonReload.addActionListener(new ActionListener()
            {
            public void actionPerformed(final ActionEvent event)
                {
                if ((getPdfDecoder() != null)
                    && (!getPdfDecoder().isOpen()))
                    {
                    runUI();
                    }
                }
            });

        //------------------------------------------------------------------------------------------
        // Remove Document

        final JButton buttonRemove;

        buttonRemove = new JButton();
        buttonRemove.setBorderPainted(false);
        buttonRemove.setIcon(RegistryModelUtilities.getCommonIcon(FILENAME_ICON_REMOVE));
        buttonRemove.setToolTipText(TOOLTIP_REMOVE);
        toolBar.add(buttonRemove);

        buttonRemove.addActionListener(new ActionListener()
            {
            public void actionPerformed(final ActionEvent event)
                {
                if ((getPdfDecoder() != null)
                    && (getPdfDecoder().isOpen()))
                    {
                    try
                        {
                        textfieldPage.setText(PAGE_NUMBER_BLANK);
                        labelOf.setText(PAGE_OF_BLANK);

                        getScrollPane().setCursor(new Cursor(Cursor.WAIT_CURSOR));
                        getPdfDecoder().setCursor(new Cursor(Cursor.WAIT_CURSOR));
                        getPdfDecoder().closePdfFile();
                        getPdfDecoder().invalidate();
                        repaint();
                        getPdfDecoder().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                        getScrollPane().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

                        ObservatoryInstrumentHelper.runGarbageCollector();
                        }

                    catch (Exception exception)
                        {
                        LOGGER.error("PDFPanel --> Remove [exception=" + exception.getMessage() + "]");
                        }
                    }
                }
            });

        toolBar.add(Box.createHorizontalGlue());

        return (toolBar);
        }


    private void debugSizes()
        {
//        System.out.println("ZOOM [factor="
//                + floatScaleFactor
//                + " [bounds_width=" + getPdfDecoder().getBounds().getWidth()
//                + "] [bounds_height=" + getPdfDecoder().getBounds().getHeight()
//                + "] [pdf_width=" + getPdfDecoder().getCurrentPageCoords().getWidth()
//                + "] [pdf_height=" + getPdfDecoder().getCurrentPageCoords().getHeight()
//                + "] [panel_width=" + getScrollPane().getWidth()
//                + "] [panel_height=" + getScrollPane().getHeight());
        }


    /***********************************************************************************************
     * Get the URL of the PDF document.
     *
     * @return URL
     */

    private URL getURL()
        {
        return (this.urlPDF);
        }


    /***********************************************************************************************
     * Get the Display JPanel.
     *
     * @return JPanel
     */

    private JPanel getDisplayPanel()
        {
        return (this.panelDisplay);
        }


    /***********************************************************************************************
     * Set the Display JPanel.
     *
     * @param panel
     */

    private void setDisplayPanel(final JPanel panel)
        {
        this.panelDisplay = panel;
        }


    /***********************************************************************************************
     * Get the Waiting JPanel.
     *
     * @return JPanel
     */

    private JPanel getWaitPanel()
        {
        return (this.panelWait);
        }


    /***********************************************************************************************
     * Set the Waiting JPanel.
     *
     * @param panel
     */

    private void setWaitPanel(final JPanel panel)
        {
        this.panelWait = panel;
        }


    /***********************************************************************************************
     * Get the PdfDecoder JPanel.
     *
     * @return PdfDecoder
     */

    private PdfDecoder getPdfDecoder()
        {
        return (this.pdfDecoder);
        }


    /***********************************************************************************************
     * Get the ScrollPane.
     *
     * @return JScrollPane
     */

    private JScrollPane getScrollPane()
        {
        return (this.scrollPane);
        }


    /**********************************************************************************************/
    /* Threads                                                                                    */
    /***********************************************************************************************
     * Indicate if the PdfDecoder is running.
     *
     * @return boolean
     */

    private boolean isRunning()
        {
        return (boolPdfDecoderRunning);
        }


    /***********************************************************************************************
     * Control the PdfDecoder state.
     *
     * @param running
     */

    private void setRunning(final boolean running)
        {
        boolPdfDecoderRunning = running;
        }


    /***********************************************************************************************
     * Get the SwingWorker which handles the PdfDecoder.
     *
     * @return SwingWorker
     */

    private SwingWorker getPdfDecoderWorker()
        {
        return (workerPdfDecoder);
        }


    /***********************************************************************************************
     * Set the SwingWorker which handles the PdfDecoder.
     *
     * @param worker
     */

    private void setPdfDecoderWorker(final SwingWorker worker)
        {
        workerPdfDecoder = worker;
        }
    }
