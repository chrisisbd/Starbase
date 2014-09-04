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

import org.jpedal.PdfDecoder;
import org.jpedal.exception.PdfException;
import org.lmn.fc.common.utilities.threads.SwingWorker;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.PublisherUIComponentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.TabToolbarInterface;
import org.lmn.fc.ui.components.BlankUIComponent;
import org.lmn.fc.ui.components.UIComponent;

import javax.swing.*;
import java.awt.*;
import java.awt.print.Printable;
import java.net.URL;


/**************************************************************************************************
 * PdfPublisherUIComponent.
 */

public final class PdfPublisherUIComponent extends UIComponent
                                           implements PublisherUIComponentInterface
    {
    private static final long serialVersionUID = -3881898156958868531L;

    // Injections
    private final ObservatoryInstrumentInterface observatoryInstrument;
    private final TabToolbarInterface toolBar;
    private URL urlContent;
    private final String strResourceKey;

    private JPanel panelDisplay;
    private JPanel panelWait;
    private PdfDecoder pdfDecoder;
    private JScrollPane scrollPane;

    // UI State
    private int intCurrentPage;
    private float floatScaleFactor;

    // The Thread for handling PdfDecoder
    private SwingWorker workerPdfDecoder;
    private boolean boolPdfDecoderRunning;


    /***********************************************************************************************
     * PdfPublisherUIComponent.
     *
     * @param hostinstrument
     * @param toolbar
     * @param url
     * @param hostresourcekey
     */

    public PdfPublisherUIComponent(final ObservatoryInstrumentInterface hostinstrument,
                                   final TabToolbarInterface toolbar,
                                   final URL url,
                                   final String hostresourcekey)
        {
        super();

        // Injections
        this.observatoryInstrument = hostinstrument;
        this.toolBar = toolbar;
        this.urlContent = url;
        this.strResourceKey = hostresourcekey;

        // UI
        this.panelDisplay = null;
        this.panelWait = null;
        this.pdfDecoder = null;
        this.scrollPane = null;

        // UI State
        this.intCurrentPage = 1;
        this.floatScaleFactor = TabToolbarInterface.ZOOM_INITIAL;

        // The Thread for handling PdfDecoder
        this.workerPdfDecoder = null;
        this.boolPdfDecoderRunning = false;
        }


    /***********************************************************************************************
     * Initialise the PdfPublisherUIComponent.
     */

    public final void initialiseUI()
        {
        final String SOURCE = "PdfPublisherUIComponent.initialiseUI() ";

        super.initialiseUI();

        setBackground(DEFAULT_COLOUR_CANVAS.getColor());

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
     * Run this PdfPublisherUIComponent.
     */

    public void runUI()
        {
        final String SOURCE = "PdfPublisherUIComponent.runUI() ";

        try
            {
            // Set the page number display
            if (getToolBar() != null)
                {
                setCurrentPage(1);

                if ((getPdfDecoder() != null)
                    && (!getPdfDecoder().isOpen()))
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
                        /*******************************************************************************
                         * Run the PdfDecoder
                         *
                         * @return Object
                         */

                        public Object construct()
                            {
                            try
                                {
                                if ((getPdfDecoder() != null)
                                    && (getToolBar() != null)
                                    && (getContentURL() != null))
                                    {
                                    // This opens the PDF and reads its internal details
                                    getPdfDecoder().openPdfFileFromURL(getContentURL().toString());
                                    getPdfDecoder().decodePage(getCurrentPage());
                                    getPdfDecoder().waitForDecodingToFinish();
                                    }
                                }

                            catch (final PdfException exception)
                                {
                                LOGGER.error(SOURCE + exception.getMessage());
                                }

                            catch (final Exception exception)
                                {
                                LOGGER.error(SOURCE + exception.getMessage());
                                }

                            return (getPdfDecoder());
                            }


                        /*******************************************************************************
                         * When the Thread stops.
                         */

                        public void finished()
                            {
                            if ((getPdfDecoder() != null)
                                && (getToolBar() != null))
                                {
                                try
                                    {
                                    // Now show the loaded file
                                    getDisplayPanel().removeAll();
                                    getDisplayPanel().add(getPdfDecoder(), BorderLayout.CENTER);

                                    // Open page 1 at 100% scaling
                                    getPdfDecoder().setPageParameters(getScaleFactor(), getCurrentPage());

                                    if (getPdfDecoder().getCurrentPageCoords().getWidth() < getScrollPane().getWidth())
                                        {
                                        System.out.println(SOURCE + "Magnify [width.pdf=" + getPdfDecoder().getCurrentPageCoords().getWidth()
                                                           + "] [width.pane=" + getScrollPane().getWidth()
                                                           + "] [scale=" + getScaleFactor() + "]");
                                        }
                                    else
                                        {
                                        System.out.println(SOURCE + "Minify [width.pdf=" + getPdfDecoder().getCurrentPageCoords().getWidth()
                                                           + "] [width.pane=" + getScrollPane().getWidth()
                                                           + "] [scale=" + getScaleFactor() + "]");
                                        }

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

                                    getToolBar().updatePageIndicator(getCurrentPage(),
                                                                     getPdfDecoder().getPageCount());
                                    }

                                catch (final Exception exception)
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
                    getToolBar().updatePageIndicator(0, 0);
                    }
                }
            }

        catch (final Exception exception)
            {
            LOGGER.error(SOURCE + exception.getMessage());
            }

        super.runUI();
        }


    /***********************************************************************************************
     * Stop this PdfPublisherUIComponent.
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

        super.stopUI();
        }


    /***********************************************************************************************
     * Dispose of all components of this PdfPublisherUIComponent.
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

        super.disposeUI();
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
     * Get the Viewer TabToolbarInterface.
     *
     * @return TabToolbarInterface
     */

    private TabToolbarInterface getToolBar()
        {
        return (this.toolBar);
        }


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
     * Get the ResourceKey.
     *
     * @return String
     */

    private String getResourceKey()
        {
        return (this.strResourceKey);
        }


    /***********************************************************************************************
     * Update the content to be displayed.
     */

    public void updateContent()
        {
        // ToDo implement
        }


    /***********************************************************************************************
     * Zoom Out by the specified increment, starting from the current scale factor.
     *
     * @param zoommin
     * @param zoommax
     * @param zoomincrement
     * @param scalefactor
     */

    public void zoomOut(final float zoommin,
                        final float zoommax,
                        final float zoomincrement,
                        final float scalefactor)
        {
        if ((pdfDecoder != null)
            && (pdfDecoder.isOpen())
            && (getContentURL() != null))
            {
            try
                {
                setScaleFactor(getScaleFactor() - TabToolbarInterface.ZOOM_INCREMENT);

                if (getScaleFactor() <= TabToolbarInterface.ZOOM_MIN)
                    {
                    setScaleFactor(TabToolbarInterface.ZOOM_MIN);
                    Toolkit.getDefaultToolkit().beep();
                    }

                pdfDecoder.setCursor(new Cursor(Cursor.WAIT_CURSOR));
                pdfDecoder.setPageParameters(getScaleFactor(), getCurrentPage());
                pdfDecoder.invalidate();
                pdfDecoder.repaint();
                //debugSizes();
                pdfDecoder.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                //getScrollPane().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }

            catch (final Exception exception)
                {
                LOGGER.error("PdfPublisherUIComponent --> ZoomOut [exception=" + exception.getMessage() + "]");
                }
            }
        }


    /***********************************************************************************************
     * Zoom In by the specified increment, starting from the current scale factor..
     *
     * @param zoommin
     * @param zoommax
     * @param zoomincrement
     * @param scalefactor
     */

    public void zoomIn(final float zoommin,
                       final float zoommax,
                       final float zoomincrement,
                       final float scalefactor)
        {
        if ((pdfDecoder != null)
            && (pdfDecoder.isOpen())
            && (getContentURL() != null))
            {
            try
                {
                setScaleFactor(getScaleFactor() + TabToolbarInterface.ZOOM_INCREMENT);

                if (getScaleFactor() >= TabToolbarInterface.ZOOM_MAX)
                    {
                    setScaleFactor(TabToolbarInterface.ZOOM_MAX);
                    Toolkit.getDefaultToolkit().beep();
                    }

                //getScrollPane().setCursor(new Cursor(Cursor.WAIT_CURSOR));
                pdfDecoder.setCursor(new Cursor(Cursor.WAIT_CURSOR));
                pdfDecoder.setPageParameters(getScaleFactor(), getCurrentPage());
                pdfDecoder.invalidate();
                pdfDecoder.repaint();
                //debugSizes();
                pdfDecoder.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                //getScrollPane().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }

            catch (final Exception exception)
                {
                LOGGER.error("PdfPublisherUIComponent --> ZoomIn [exception=" + exception.getMessage() + "]");
                }
            }
        }


    /***********************************************************************************************
     * Reload the current document.
     */

    public void reloadDocument()
        {
        if ((pdfDecoder != null)
            && (!pdfDecoder.isOpen()))
            {
            runUI();
            }
        }


    /***********************************************************************************************
     * Remove the current document.
     */

    public void removeDocument()
        {
        if ((pdfDecoder != null)
            && (pdfDecoder.isOpen()))
            {
            try
                {
                //getScrollPane().setCursor(new Cursor(Cursor.WAIT_CURSOR));
                pdfDecoder.setCursor(new Cursor(Cursor.WAIT_CURSOR));
                pdfDecoder.closePdfFile();
                pdfDecoder.invalidate();
                pdfDecoder.repaint();
                pdfDecoder.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                //getScrollPane().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

                setCurrentPage(1);
                ObservatoryInstrumentHelper.runGarbageCollector();
                }

            catch (final Exception exception)
                {
                LOGGER.error("PdfPublisherUIComponent --> Remove [exception=" + exception.getMessage() + "]");
                }
            }
        }


    /***********************************************************************************************
     * Get the Printable Publication.
     *
     * @return Printable
     */

    public Printable getPrintable()
        {
        final Printable printable;

        if (getPdfDecoder() != null)
            {
            printable = getPdfDecoder();
            }
        else
            {
            printable = null;
            }

        return (printable);
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


    /***********************************************************************************************
     * Get the CurrentPage.
     *
     * @return int
     */

    public int getCurrentPage()
        {
        return (this.intCurrentPage);
        }


    /***********************************************************************************************
     * Set the CurrentPage.
     *
     * @param pageid
     */

    public void setCurrentPage(final int pageid)
        {
        final String SOURCE = "PdfPublisherUIComponent.setCurrentPage() ";

        if ((pdfDecoder != null)
            && (pdfDecoder.isOpen())
            && (getContentURL() != null))
            {
            try
                {
                this.intCurrentPage = pageid;

                pdfDecoder.decodePage(getCurrentPage());
                pdfDecoder.waitForDecodingToFinish();
                pdfDecoder.invalidate();
                pdfDecoder.repaint();
                }

            catch (final Exception exception)
                {
                LOGGER.error(SOURCE + pageid);
                exception.printStackTrace();
                }
            }
        else
            {
            this.intCurrentPage = 1;
            }
        }


    /***********************************************************************************************
     * Get the Page Count.
     *
     * @return int
     */

    public int getPageCount()
        {
        final int intPageCount;

        if ((pdfDecoder != null)
            && (pdfDecoder.isOpen()))
            {
            intPageCount = pdfDecoder.getPageCount();
            }
        else
            {
            intPageCount = 0;
            }

        return (intPageCount);
        }


    /***********************************************************************************************
     * Get the display ScaleFactor.
     *
     * @return float
     */

    public float getScaleFactor()
        {
        return (this.floatScaleFactor);
        }


    /***********************************************************************************************
     * Set the display ScaleFactor.
     *
     * @param scale
     */

    public void setScaleFactor(final float scale)
        {
        this.floatScaleFactor = scale;
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

//    private void debugSizes()
//        {
////        System.out.println("ZOOM [factor="
////                + floatScaleFactor
////                + " [bounds_width=" + getPdfDecoder().getBounds().getWidth()
////                + "] [bounds_height=" + getPdfDecoder().getBounds().getHeight()
////                + "] [pdf_width=" + getPdfDecoder().getCurrentPageCoords().getWidth()
////                + "] [pdf_height=" + getPdfDecoder().getCurrentPageCoords().getHeight()
////                + "] [panel_width=" + getScrollPane().getWidth()
////                + "] [panel_height=" + getScrollPane().getHeight());
