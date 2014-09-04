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

package org.lmn.fc.common.utilities.printing;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;


/***************************************************************************************************
 * PrintUtilities.
 */

public class PrintUtilities implements Printable
    {
    private final Component componentToBePrinted;


    /***********************************************************************************************
     * Print the specified Component.
     *
     * @param component
     *
     * @return boolean
     */

    public static boolean printComponent(final Component component)
        {
        return(new PrintUtilities(component).print());
        }


    /***********************************************************************************************
     * Print the specified Component using the PageFormat.
     *
     * @param component
     * @param pageformat
     *
     * @return boolean
     */

    public static boolean printComponent(final Component component,
                                         final PageFormat pageformat)
        {
        return(new PrintUtilities(component).print(pageformat));
        }


    /***********************************************************************************************
     * Get the ScaleFactor to ensure the lareger of Width or Height fit the paper size.
     *
     * @param originaldim
     * @param tofitdim
     *
     * @return double
     */

    public static double getScaleFactorToFit(final Dimension originaldim,
                                             final Dimension tofitdim)
        {
        double dblScale;

        dblScale = 1.0;

        if ((originaldim != null)
            && (tofitdim != null))
            {
            final double dScaleWidth;
            final double dScaleHeight;

            dScaleWidth = getScaleFactor(originaldim.width, tofitdim.width);
            dScaleHeight = getScaleFactor(originaldim.height, tofitdim.height);

            dblScale = Math.min(dScaleHeight, dScaleWidth);
            }

        return (dblScale);
        }


    /***********************************************************************************************
     * Scale either a specified width or height to fit the target size.
     *
     * @param originalsize
     * @param targetsize
     *
     * @return double
     */

    private static double getScaleFactor(final int originalsize,
                                         final int targetsize)
        {
        final double dblScale;

        if (originalsize > targetsize)
            {
            dblScale = (double) targetsize / (double) originalsize;
            }
        else
            {
            dblScale = (double) targetsize / (double) originalsize;
            }

        return (dblScale);
        }


    /***********************************************************************************************
     * The speed and quality of printing suffers dramatically if
     * any of the containers have double buffering turned on.
     * So this turns if off globally.
     *
     * @param component
     */

    private static void disableDoubleBuffering(final Component component)
        {
        final RepaintManager currentManager;

        currentManager = RepaintManager.currentManager(component);
        currentManager.setDoubleBufferingEnabled(false);
        }


    /***********************************************************************************************
     * Re-enables double buffering globally.
     *
     * @param component
     */

    private static void enableDoubleBuffering(final Component component)
        {
        final RepaintManager currentManager;

        currentManager = RepaintManager.currentManager(component);
        currentManager.setDoubleBufferingEnabled(true);
        }


    /***********************************************************************************************
     * PrintUtilities.
     *
     * @param component
     */

    private PrintUtilities(final Component component)
        {
        this.componentToBePrinted = component;
        }


    /***********************************************************************************************
     * Print this Component.
     *
     * @return boolean
     */

    public boolean print()
        {
        final PrinterJob printJob;
        boolean boolSuccess;

        printJob = PrinterJob.getPrinterJob();
        printJob.setPrintable(this);
        boolSuccess = false;

        if (printJob.printDialog())
            {
            try
                {
                printJob.print();
                boolSuccess = true;
                }

            catch (final PrinterException exception)
                {
                System.out.println("Error printing: " + exception);
                }
            }

        return (boolSuccess);
        }


    /***********************************************************************************************
     * Print this Component using the PageFormat.
     *
     * @return boolean
     */

    public boolean print(final PageFormat pageformat)
        {
        final PrinterJob printJob;
        boolean boolSuccess;

        printJob = PrinterJob.getPrinterJob();
        printJob.setPrintable(this, pageformat);
        boolSuccess = false;

        if (printJob.printDialog())
            {
            try
                {
                printJob.print();
                boolSuccess = true;
                }

            catch (final PrinterException exception)
                {
                System.out.println("Error printing: " + exception);
                }
            }

        return (boolSuccess);
        }


    /**********************************************************************************************/
    /* See: StackOverflow Question 17904518                                                                                           */
    /***********************************************************************************************
     * Prints the page at the specified index into the specified
     * {@link Graphics} context in the specified
     * format.  A <code>PrinterJob</code> calls the
     * <code>Printable</code> interface to request that a page be
     * rendered into the context specified by
     * <code>graphics</code>.  The format of the page to be drawn is
     * specified by <code>pageFormat</code>.  The zero based index
     * of the requested page is specified by <code>pageIndex</code>.
     * If the requested page does not exist then this method returns
     * NO_SUCH_PAGE; otherwise PAGE_EXISTS is returned.
     * The <code>Graphics</code> class or subclass implements the
     * {@link java.awt.print.PrinterGraphics} interface to provide additional
     * information.  If the <code>Printable</code> object
     * aborts the print job then it throws a {@link PrinterException}.
     *
     * @param graphics the context into which the page is drawn
     * @param pageFormat the size and orientation of the page being drawn
     * @param pageIndex the zero based index of the page to be drawn
     *
     * @return PAGE_EXISTS if the page is rendered successfully
     *         or NO_SUCH_PAGE if <code>pageIndex</code> specifies a
     *	       non-existent page.
     *
     * @exception java.awt.print.PrinterException
     *         thrown when the print job is terminated.
     */

    public int print(final Graphics graphics,
                     final PageFormat pageFormat,
                     final int pageIndex) throws PrinterException
        {
        if (pageIndex > 0)
            {
            return (NO_SUCH_PAGE);
            }
        else
            {
            final Dimension dimComponent;
            final Dimension dimPrint;
            double dblScaleFactor;
            final double dblScaleWidth;
            final double dblScaleHeight;
            final AffineTransform affineTransform;
            final Graphics2D graphics2D;
            final double dblX;
            final double dblY;

//            final Graphics2D g2d = (Graphics2D) graphics;
//            g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
//            disableDoubleBuffering(componentToBePrinted);
//            componentToBePrinted.paint(g2d);
//            enableDoubleBuffering(componentToBePrinted);

            // Get the preferred size of the component...
            dimComponent = componentToBePrinted.getPreferredSize();

            // Make sure we size the Printable to the preferred size
            componentToBePrinted.setSize(dimComponent);

            // Get the the print size from the PageFormat
            dimPrint = new Dimension();
            dimPrint.setSize(pageFormat.getImageableWidth(),
                             pageFormat.getImageableHeight());

            // Calculate the scale factor to fit the Component to the Paper
            dblScaleFactor = getScaleFactorToFit(dimComponent, dimPrint);

            // Don't want to scale up, only want to scale down
            if (dblScaleFactor > 1.0)
                {
                dblScaleFactor = 1.0;
                }

            // Calculate the scaled size...
            dblScaleWidth = dimComponent.width * dblScaleFactor;
            dblScaleHeight = dimComponent.height * dblScaleFactor;

            // Create a clone of the graphics context.
            // This allows us to manipulate the graphics context
            // without begin worried about what effects
            // it might have once we're finished
            graphics2D = (Graphics2D) graphics.create();

            // Calculate the x/y position of the component,
            // this will center the result on the page if it can
            dblX = ((pageFormat.getImageableWidth() - dblScaleWidth) / 2.0) + pageFormat.getImageableX();
            dblY = ((pageFormat.getImageableHeight() - dblScaleHeight) / 2.0) + pageFormat.getImageableY();

            // Create a new AffineTransformation
            affineTransform = new AffineTransform();

            // Translate the offset to our "center" of page
            affineTransform.translate(dblX, dblY);

            // Set the scaling
            affineTransform.scale(dblScaleFactor, dblScaleFactor);

            // Apply the transformation
            graphics2D.transform(affineTransform);

            // Print the component
            disableDoubleBuffering(componentToBePrinted);
            componentToBePrinted.printAll(graphics2D);
            enableDoubleBuffering(componentToBePrinted);

            // Dispose of the copy of the graphics context, freeing up memory and discarding
            // our changes
            graphics2D.dispose();

            componentToBePrinted.invalidate();

            return (PAGE_EXISTS);
            }
        }
    }
