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

package org.lmn.fc.common.datatranslators.fits;

import net.grelf.Util;
import net.grelf.grip.Accumulator;
import net.grelf.grip.AccumulatorDouble;
import net.grelf.grip.AccumulatorInt;
import net.grelf.grip.Im;
import nom.tam.fits.*;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;


/**
 * For loading images from files in FITS format, using the nom.tam.fits library from
 * http://heasarc.gsfc.nasa.gov/docs/heasarc/fits/java/v1.0/.
 * NB: Loads each image found in the file but does not attempt to analyse metadata (eg, exposure settings).
 * Depending on the data type in an image, this method will either return a java.awt.image.BufferedImage
 * to its caller, for the caller to display, or create a net.grelf.grip.Accumulator and provide user interaction
 * to read that out through a look-up curve into a BufferedImage (16 bits per channel, TYPE_CUSTOM) which is
 * immediately displayed in a net.grelf.grip.ImFrame. The behaviour is as indicated in this table:<br>
 * <table border="1">
 * <tr><th>Data type</th><th>BScale=1,BZero=0?</th><th>Result</th></tr>
 * <tr><th>byte [][]</th><th>yes</th><th>BufferedImage.TYPE_BYTE_GRAY</th></tr>
 * <tr><th>byte [][]</th><th>no</th><th>AccumulatorDouble</th></tr>
 * <tr><th>short [][]</th><th>yes</th><th>BufferedImage.TYPE_USHORT_GRAY</th></tr>
 * <tr><th>short [][]</th><th>no</th><th>AccumulatorDouble</th></tr>
 * <tr><th>int [][]</th><th>yes</th><th>AccumulatorInt</th></tr>
 * <tr><th>int [][]</th><th>no</th><th>AccumulatorDouble</th></tr>
 * <tr><th>long [][]</th><th>either</th><th>Not handled</th></tr>
 * <tr><th>float [][]</th><th>either</th><th>AccumulatorDouble</th></tr>
 * <tr><th>double [][]</th><th>either</th><th>AccumulatorDouble</th></tr>
 * <tr><th>byte [][][]</th><th>yes</th><th>BufferedImage.TYPE_INT_RGB</th></tr>
 * <tr><th>byte [][][]</th><th>no</th><th>AccumulatorDouble</th></tr>
 * <tr><th>short [][][]</th><th>yes</th><th>BufferedImage.TYPE_INT_RGB</th></tr>
 * <tr><th>short [][][]</th><th>no</th><th>AccumulatorDouble</th></tr>
 * <tr><th>int [][][]</th><th>yes</th><th>AccumulatorInt</th></tr>
 * <tr><th>int [][][]</th><th>no</th><th>AccumulatorDouble</th></tr>
 * <tr><th>long [][][]</th><th>either</th><th>Not handled</th></tr>
 * <tr><th>float [][][]</th><th>either</th><th>AccumulatorDouble</th></tr>
 * <tr><th>double [][][]</th><th>either</th><th>AccumulatorDouble</th></tr>
 * </table><br>
 * Notes:<br>
 * <ol>
 * <li>The FITS specification does not include 64-bit integers as a data type but nom.tam.fits can nevertheless
 * create long arrays. No examples of this have yet been seen by me.</li>
 * <li>For 3D arrays (FITS NAXES=3) it is assumed that the axis with the smallest length is the channels (bands)
 * axis.</li>
 * <li>The code can create BufferedImage objects with more than 3 channels (TYPE_CUSTOM). GRIP is written to be
 * able to process such images in nearly all places but some problems could be caused.</li>
 * </ol>
 */
public class FitsHelper
    {
    // There may seem to be a huge amount of code duplication in this class but it is because it is handling
    // many possible primitive array types.


    public static Im load(final File file)
        {
        BufferedImage bim = null;

        // Do this quietly...
        Util.setShowErrors(false);

        if (!file.exists())
            {
            System.out.println("File not found: " + file.getPath());
            }
        else
            {
            try
                {
                final Fits fits = new Fits(file);
                final BasicHDU[] hdus = fits.read();
                int imHduNo = 0;

                for (final BasicHDU hdu : hdus)
                    {
                    if (hdu instanceof ImageHDU)
                        {
                        final ImageHDU imHDU = (ImageHDU) hdu;
                        imHduNo++;

                        final StringBuffer info = new StringBuffer();
                        info.append("HDU ");
                        info.append(imHduNo);
                        info.append(" of ");
                        info.append(hdus.length);
                        info.append(" is an image\n");

                        /* Never seen an example where these are non-zero in the file:
                                    info.append ("Minimum value = ");
                                    info.append (imHDU.getMinimumValue ());
                                    info.append ("\n");

                                    info.append ("Maximum value = ");
                                    info.append (imHDU.getMaximumValue ());
                                    info.append ("\n");
                        */
                        final double bScale = imHDU.getBScale();
                        info.append("BScale = ");
                        info.append(bScale);
                        info.append("\n");

                        final double bZero = imHDU.getBZero();
                        info.append("BZero = ");
                        info.append(bZero);
                        info.append("\n");

                        info.append("BUnit = ");
                        info.append(imHDU.getBUnit());
                        info.append("\n");

                        final int bitpix = imHDU.getBitPix();
                        info.append("bitpix = ");
                        info.append(bitpix);
                        info.append("\n");

                        final int[] axes = imHDU.getAxes();

                        if (null == axes)
                            {
                            Util.warning("Error", "No axes specified in HDU");
                            }
                        else
                            {
                            for (int j = 0;
                                 j < axes.length;
                                 j++)
                                {
                                info.append("nAxes [");
                                info.append(j);
                                info.append("] = ");
                                info.append(axes[j]);
                                info.append("\n");
                                }

                            Util.message("Information", info.toString());
                            /**** EITHER:
                             nom.tam.image.ImageTiler tiler = ihdu.getTiler ();

                             if (null == tiler)
                             {
                             net.grelf.Util.warning ("Error", "Could not create ImageTiler (fits.jar)");
                             }
                             else
                             {
                             Object obj = tiler.getCompleteImage ();
                             ****/
                            /**** OR: */
                            final Data imdata = imHDU.getData();

                            if (null == imdata)
                                {
                                Util.warning("Error", "Null data");
                                }
                            else
                                {
                                final Object obj = imdata.getData();
                                /****/
                                switch (imHDU.getBitPix())
                                    {
                                    case ImageHDU.BITPIX_BYTE:
                                    {
                                    switch (axes.length)
                                        {
                                        case 2:
                                        {
                                        final byte[][] data = (byte[][]) obj;
                                        bim = createBufferedImage(data, file, bScale, bZero);
                                        }
                                        break;

                                        case 3:
                                        {
                                        final byte[][][] data = (byte[][][]) obj;
                                        bim = createBufferedImage(data, file, bScale, bZero,
                                                                  findChannelIndex(axes));
                                        }
                                        break;

                                        default:
                                            Util.warning("Error",
                                                                   "Unrecognised axes length" + axes.length);
                                        }
                                    }
                                    break;

                                    case ImageHDU.BITPIX_SHORT:
                                    {
                                    switch (axes.length)
                                        {
                                        case 2:
                                        {
                                        final short[][] data = (short[][]) obj;
                                        bim = createBufferedImage(data, file, bScale, bZero);
                                        }
                                        break;

                                        case 3:
                                        {
                                        final short[][][] data = (short[][][]) obj;
                                        bim = createBufferedImage(data, file, bScale, bZero,
                                                                  findChannelIndex(axes));
                                        }
                                        break;

                                        default:
                                            Util.warning("Error",
                                                                   "Unrecognised axes length" + axes.length);
                                        }
                                    }
                                    break;

                                    case ImageHDU.BITPIX_INT:
                                    {
                                    switch (axes.length)
                                        {
                                        case 2:
                                        {
                                        final int[][] data = (int[][]) obj;
                                        bim = createBufferedImage(data, file, bScale, bZero);
                                        }
                                        break;

                                        case 3:
                                        {
                                        final int[][][] data = (int[][][]) obj;
                                        bim = createBufferedImage(data, file, bScale, bZero,
                                                                  findChannelIndex(axes));
                                        }
                                        break;

                                        default:
                                            Util.warning("Error",
                                                                   "Unrecognised axes length" + axes.length);
                                        }
                                    }
                                    break;

                                    case ImageHDU.BITPIX_LONG:
                                    {
                                    switch (axes.length)
                                        {
                                        case 2:
                                        {
                                        final long[][] data = (long[][]) obj;
                                        bim = createBufferedImage(data, file, bScale, bZero);
                                        }
                                        break;

                                        case 3:
                                        {
                                        final long[][][] data = (long[][][]) obj;
                                        bim = createBufferedImage(data, file, bScale, bZero,
                                                                  findChannelIndex(axes));
                                        }
                                        break;

                                        default:
                                            Util.warning("Error",
                                                                   "Unrecognised axes length" + axes.length);
                                        }
                                    }
                                    break;

                                    case ImageHDU.BITPIX_FLOAT:
                                    {
                                    switch (axes.length)
                                        {
                                        case 2:
                                        {
                                        final float[][] data = (float[][]) obj;
                                        bim = createBufferedImage(data, file, bScale, bZero);
                                        }
                                        break;

                                        case 3:
                                        {
                                        final float[][][] data = (float[][][]) obj;
                                        bim = createBufferedImage(data, file, bScale, bZero,
                                                                  findChannelIndex(axes));
                                        }
                                        break;

                                        default:
                                            Util.warning("Error",
                                                                   "Unrecognised axes length" + axes.length);
                                        }
                                    }
                                    break;

                                    case ImageHDU.BITPIX_DOUBLE:
                                    {
                                    switch (axes.length)
                                        {
                                        case 2:
                                        {
                                        final double[][] data = (double[][]) obj;
                                        bim = createBufferedImage(data, file, bScale, bZero);
                                        }
                                        break;

                                        case 3:
                                        {
                                        final double[][][] data = (double[][][]) obj;
                                        bim = createBufferedImage(data, file, bScale, bZero,
                                                                  findChannelIndex(axes));
                                        }
                                        break;

                                        default:
                                            Util.warning("Error",
                                                                   "Unrecognised axes length" + axes.length);
                                        }
                                    }

                                    default:
                                        Util.warning("Error",
                                                               "Unrecognised BITPIX value" + bitpix);
                                    }
                                }
                            }
                        }
                    else if (hdu instanceof TableHDU)
                        {
                        Util.logInfo("TableHDU not handled");
                        }
                    else if (hdu instanceof RandomGroupsHDU)
                        {
                        Util.logInfo("RandomGroupsHDU not handled");
                        }
                    else if (hdu instanceof UndefinedHDU)
                        {
                        Util.logInfo("UndefinedHDU not handled");
                        }
                    else
                        {
                        Util.logInfo("Unknown HDU type not handled");
                        }
                    }
                }
            catch (FitsException ex)
                {
                Util.warning("Error", ex);
                }
            /**** Needed only if using ImageTiler
             catch (java.io.IOException ex)
             {
             net.grelf.Util.warning ("Error", ex);
             }
             ****/
            catch (Exception ex) // Needed to catch ClassNotFoundException
                {
                if (ex instanceof ClassNotFoundException)
                    {
                    Util.warning("Error",
                                           "File fits.jar not found on the classpath\nGet it from http://heasarc.gsfc.nasa.gov/docs/heasarc/fits/java/v1.0/");
                    }
                else
                    {
                    Util.warning("Error", ex);
                    }
                }
            }

        if (null == bim) // Eg, because we used an Accumulator instead
            {
            return null;
            }

        return new Im(bim);
        } // load


    /**********************************************************************************************/
    /* Helpers                                                                                    */
    /***********************************************************************************************
     *
     * @param data
     * @param file
     * @param bScale
     * @param bZero
     *
     * @return BufferedImage
     */

    private static BufferedImage createBufferedImage(final byte[][] data,
                                                     final File file,
                                                     final double bScale,
                                                     final double bZero)
        {
        if (bScale != 1.0 || bZero != 0.0)
            {
            final Accumulator accum = new AccumulatorDouble(data, bScale, bZero);
            accum.displayAs3x16bitImage(file.getName(), file.getPath(), null, true);
            return null;
            }
        else // No offset or scaling needed:
            {
            final int width = data[0].length;
            final int height = data.length;
            final BufferedImage bim =
                    new BufferedImage(width, height,
                                                     BufferedImage.TYPE_BYTE_GRAY);
            final WritableRaster wr = bim.getRaster();
            final int[] px = new int[1];

            for (int y = 0;
                 y < height;
                 y++)
                {
                for (int x = 0;
                     x < width;
                     x++)
                    {
                    px[0] = data[y][x];
                    wr.setPixel(x, y, px);
                    }
                }

            return bim;
            }
        } // createBufferedImage [byte2]


    private static BufferedImage createBufferedImage(
            final short[][] data,
            final File file,
            final double bScale,
            final double bZero)
        {
        if (bScale != 1.0 || bZero != 0.0)
            {
            final Accumulator accum = new AccumulatorDouble(data, bScale, bZero);
            accum.displayAs3x16bitImage(file.getName(), file.getPath(), null, true);
            return null;
            }
        else // No offset or scaling needed:
            {
            final int width = data[0].length;
            final int height = data.length;
            final BufferedImage bim =
                    new BufferedImage(width, height,
                                                     BufferedImage.TYPE_USHORT_GRAY);
            final WritableRaster wr = bim.getRaster();
            final int[] px = new int[1];

            for (int y = 0;
                 y < height;
                 y++)
                {
                for (int x = 0;
                     x < width;
                     x++)
                    {
                    px[0] = data[y][x];
                    wr.setPixel(x, y, px);
                    }
                }

            return bim;
            }
        } // createBufferedImage [short2]


    private static BufferedImage createBufferedImage(
            final int[][] data,
            final File file,
            final double bScale,
            final double bZero)
        {
        final Accumulator accum;

        if (bScale != 1.0 || bZero != 0.0)
            {
            accum = new AccumulatorDouble(data, bScale, bZero);
            }
        else // No offset or scaling needed:
            {
            accum = new AccumulatorInt(data);
            }

        accum.displayAs3x16bitImage(file.getName(), file.getPath(), null, true);
        return null;
        } // createBufferedImage [int2]


    private static BufferedImage createBufferedImage(
            final long[][] data,
            final File file,
            final double bScale,
            final double bZero)
        {
        Util.warning("Sorry", "long x 1 channel not yet implemented");
        return null;
        } // createBufferedImage [long2]


    private static BufferedImage createBufferedImage(
            final float[][] data,
            final File file,
            final double bScale,
            final double bZero)
        {
        final Accumulator accum;

        if (bScale != 1.0 || bZero != 0.0)
            {
            accum = new AccumulatorDouble(data, bScale, bZero);
            }
        else // No offset or scaling needed:
            {
            accum = new AccumulatorDouble(data);
            }

        accum.displayAs3x16bitImage(file.getName(), file.getPath(), null, true);
        return null;
        } // createBufferedImage [float2]


    private static BufferedImage createBufferedImage(
            final double[][] data,
            final File file,
            final double bScale,
            final double bZero)
        {
        final Accumulator accum;

        if (bScale != 1.0 || bZero != 0.0)
            {
            accum = new AccumulatorDouble(data, bScale, bZero);
            }
        else // No offset or scaling needed:
            {
            accum = new AccumulatorDouble(data);
            }

        accum.displayAs3x16bitImage(file.getName(), file.getPath(), null, true);
        return null;
        } // createBufferedImage [double2]


    private static BufferedImage createBufferedImage(
            final byte[][][] data,
            final File file,
            final double bScale,
            final double bZero,
            final int channelIndex)
        {
        final int width;
        final int height;
        final int nBands;

        switch (channelIndex)
            {
            case 0:
                nBands = data.length;
                height = data[0].length;
                width = data[0][0].length;
                break;
            case 1:
                height = data.length;
                nBands = data[0].length;
                width = data[0][0].length;
                break;
            default:
                height = data.length;
                width = data[0].length;
                nBands = data[0][0].length;
            }

        if (3 < nBands)
            {
            Util.warning("Sorry", "byte x " + nBands + " channel not yet implemented");
            return null;
            }
        else
            {
            if (bScale != 1.0 || bZero != 0.0)
                {
                final Accumulator accum = new AccumulatorDouble(data, bScale, bZero, channelIndex);
                accum.displayAs3x16bitImage(file.getName(), file.getPath(), null, true);
                return null;
                }
            else // No offset or scaling needed:
                {
                final BufferedImage bim =
                        new BufferedImage(width, height,
                                                         BufferedImage.TYPE_INT_RGB);
                final WritableRaster wr = bim.getRaster();
                final int[] px = new int[]{0,
                                     0,
                                     0};

                for (int y = 0;
                     y < height;
                     y++)
                    {
                    for (int x = 0;
                         x < width;
                         x++)
                        {
                        for (int b = 0;
                             b < nBands;
                             b++)
                            {
                            switch (channelIndex)
                                {
                                case 0:
                                    px[b] = data[b][y][x];
                                    break;
                                case 1:
                                    px[b] = data[y][b][x];
                                    break;
                                case 2:
                                    px[b] = data[y][x][b];
                                    break;
                                }
                            }

                        wr.setPixel(x, y, px);
                        }
                    }

                return bim;
                }
            }
        } // createBufferedImage [byte3]


    private static BufferedImage createBufferedImage(
            final short[][][] data,
            final File file,
            final double bScale,
            final double bZero,
            final int channelIndex)
        {
        final int width;
        final int height;
        final int nBands;

        switch (channelIndex)
            {
            case 0:
                nBands = data.length;
                height = data[0].length;
                width = data[0][0].length;
                break;
            case 1:
                height = data.length;
                nBands = data[0].length;
                width = data[0][0].length;
                break;
            default:
                height = data.length;
                width = data[0].length;
                nBands = data[0][0].length;
            }

        if (3 < nBands)
            {
            Util.warning("Sorry", "short x " + nBands + " channels not yet implemented");
            return null;
            }
        else
            {
            if (bScale != 1.0 || bZero != 0.0)
                {
                final Accumulator accum = new AccumulatorDouble(data, bScale, bZero, channelIndex);
                accum.displayAs3x16bitImage(file.getName(), file.getPath(), null, true);
                return null;
                }
            else // No offset or scaling needed:
                {
                final BufferedImage bim =
                        new BufferedImage(width, height,
                                                         BufferedImage.TYPE_INT_RGB);
                final WritableRaster wr = bim.getRaster();
                final int[] px = new int[]{0,
                                     0,
                                     0};

                for (int y = 0;
                     y < height;
                     y++)
                    {
                    for (int x = 0;
                         x < width;
                         x++)
                        {
                        for (int b = 0;
                             b < nBands;
                             b++)
                            {
                            switch (channelIndex)
                                {
                                case 0:
                                    px[b] = data[b][y][x];
                                    break;
                                case 1:
                                    px[b] = data[y][b][x];
                                    break;
                                case 2:
                                    px[b] = data[y][x][b];
                                    break;
                                }
                            }

                        wr.setPixel(x, y, px);
                        }
                    }

                return bim;
                }
            }
        } // createBufferedImage [short3]


    private static BufferedImage createBufferedImage(
            final int[][][] data,
            final File file,
            final double bScale,
            final double bZero,
            final int channelIndex)
        {
        final int width;
        final int height;
        final int nBands;

        switch (channelIndex)
            {
            case 0:
                nBands = data.length;
                height = data[0].length;
                width = data[0][0].length;
                break;
            case 1:
                height = data.length;
                nBands = data[0].length;
                width = data[0][0].length;
                break;
            default:
                height = data.length;
                width = data[0].length;
                nBands = data[0][0].length;
            }

        if (3 < nBands)
            {
            Util.warning("Sorry", "int x " + nBands + " channels not yet implemented");
            return null;
            }
        else
            {
            final Accumulator accum;

            if (bScale != 1.0 || bZero != 0.0)
                {
                accum = new AccumulatorDouble(data, bScale, bZero, channelIndex);
                }
            else // No offset or scaling needed:
                {
                accum = new AccumulatorInt(data, channelIndex);
                }

            accum.displayAs3x16bitImage(file.getName(), file.getPath(), null, true);
            return null;
            }
        } // createBufferedImage [int3]


    private static BufferedImage createBufferedImage(
            final long[][][] data,
            final File file,
            final double bScale,
            final double bZero,
            final int channelIndex)
        {
        Util.warning("Sorry", "long data values in image not yet handled");
        return null;
        } // createBufferedImage [long3]


    private static BufferedImage createBufferedImage(
            final float[][][] data,
            final File file,
            final double bScale,
            final double bZero,
            final int channelIndex)
        {
        final AccumulatorDouble accum;

        if (bScale != 1.0 || bZero != 0.0)
            {
            accum = new AccumulatorDouble(data, bScale, bZero, channelIndex);
            }
        else // No offset or scaling needed:
            {
            accum = new AccumulatorDouble(data, channelIndex);
            }

        accum.displayAs3x16bitImage(file.getName(), file.getPath(), null, true);
        return null;
        } // createBufferedImage [float3]


    private static BufferedImage createBufferedImage(
            final double[][][] data,
            final File file,
            final double bScale,
            final double bZero,
            final int channelIndex)
        {
        final AccumulatorDouble accum;

        if (bScale != 1.0 || bZero != 0.0)
            {
            accum = new AccumulatorDouble(data, bScale, bZero, channelIndex);
            }
        else // No offset or scaling needed:
            {
            accum = new AccumulatorDouble(data, channelIndex);
            }

        accum.displayAs3x16bitImage(file.getName(), file.getPath(), null, true);
        return null;
        } // createBufferedImage [double3]


    /**
     * Assume that the smallest of the (more than 2) axes must be the channel index.
     */
    private static int findChannelIndex(final int[] axes)
        {
        int iMin = 0;
        int min = axes[0];

        for (int i = 1;
             i < axes.length;
             i++)
            {
            if (axes[i] < min)
                {
                min = axes[i];
                iMin = i;
                }
            }

        return iMin;
        } // findChannelIndex

    } // FITS
