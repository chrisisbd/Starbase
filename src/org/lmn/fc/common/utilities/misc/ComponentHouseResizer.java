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

package org.lmn.fc.common.utilities.misc;

import sun.awt.image.BufferedImageGraphicsConfig;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


/**
 * High-Quality Image Resize with Java
 * http://www.componenthouse.com/article-20
 *
 * @author Hugo Teixeira
 */
public final class ComponentHouseResizer
    {

    public static void main(final String[] args)
        {
        try
            {
            final BufferedImage image = ImageIO.read(new File("c:\\picture.jpg"));
            ImageIO.write(resizeTrick(image,
                                      24,
                                      24),
                          "png",
                          new File("c:\\picture3.png"));
            }
        catch (IOException e)
            {
            e.printStackTrace();
            }
        }


    public static BufferedImage resizeTrick(final BufferedImage image,
                                            final int width,
                                            final int height)
        {
        BufferedImage newImage;

        newImage = createCompatibleImage(image);
//        newImage = resize(newImage,
//                       100,
//                       100);
        newImage = blurImage(newImage);

        return resize(newImage,
                      width,
                      height);
        }


    private static BufferedImage resize(final BufferedImage image,
                                        final int width,
                                        final int height)
        {
        final int type = image.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : image.getType();
        final BufferedImage resizedImage = new BufferedImage(width,
                                                       height,
                                                       type);
        final Graphics2D g = resizedImage.createGraphics();
        g.setComposite(AlphaComposite.Src);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                           RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING,
                           RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                           RenderingHints.VALUE_ANTIALIAS_ON);
        g.drawImage(image,
                    0,
                    0,
                    width,
                    height,
                    null);
        g.dispose();
        return resizedImage;
        }



    public static BufferedImage blurImage(final BufferedImage image)
        {
        final float ninth = 1.0f / 9.0f;
        final float[] blurKernel = {
                ninth, ninth, ninth,
                ninth, ninth, ninth,
                ninth, ninth, ninth
        };

        final Map<RenderingHints.Key, Object> map = new HashMap<RenderingHints.Key, Object>();
        map.put(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        map.put(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
        map.put(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        final RenderingHints hints = new RenderingHints(map);
        final BufferedImageOp op = new ConvolveOp(new Kernel(3,
                                                       3,
                                                       blurKernel),
                                            ConvolveOp.EDGE_NO_OP,
                                            hints);
        return op.filter(image,
                         null);
        }


    private static BufferedImage createCompatibleImage(final BufferedImage image)
        {
        final GraphicsConfiguration gc = BufferedImageGraphicsConfig.getConfig(image);
        final int w = image.getWidth();
        final int h = image.getHeight();
        final BufferedImage result = gc.createCompatibleImage(w,
                                                        h,
                                                        Transparency.TRANSLUCENT);
        final Graphics2D g2 = result.createGraphics();
        g2.drawRenderedImage(image,
                             null);
        g2.dispose();
        return result;
        }

    }


/***********************************************************************************************
 * Convenience method that returns a scaled instance of the
 * provided {@code BufferedImage}.
 *
 * @param img the original image to be scaled
 * @param targetWidth the desired width of the scaled instance,
 *    in pixels
 * @param targetHeight the desired height of the scaled instance,
 *    in pixels
 * @param hint one of the rendering hints that corresponds to
 *    {@code RenderingHints.KEY_INTERPOLATION} (e.g.
 *    {@code RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR},
 *    {@code RenderingHints.VALUE_INTERPOLATION_BILINEAR},
 *    {@code RenderingHints.VALUE_INTERPOLATION_BICUBIC})
 * @param higherQuality if true, this method will use a multi-step
 *    scaling technique that provides higher quality than the usual
 *    one-step technique (only useful in downscaling cases, where
 *    {@code targetWidth} or {@code targetHeight} is
 *    smaller than the original dimensions, and generally only when
 *    the {@code BILINEAR} hint is specified)
 *
 * @return a scaled version of the original {@code BufferedImage}
 */

//    public static BufferedImage getScaledInstance(final BufferedImage img,
//                                                  final int targetWidth,
//                                                  final int targetHeight,
//                                                  final Object hint,
//                                                  final boolean higherQuality)
//        {
//        final int type = (img.getTransparency() == Transparency.OPAQUE) ?
//            BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
//        BufferedImage ret = img;
//        int w, h;
//
//        if (higherQuality)
//            {
//            // Use multi-step technique: start with original size, then
//            // scale down in multiple passes with drawImage()
//            // until the target size is reached
//            w = img.getWidth();
//            h = img.getHeight();
//            }
//        else
//            {
//            // Use one-step technique: scale directly from original
//            // size to target size with a single drawImage() call
//            w = targetWidth;
//            h = targetHeight;
//            }
//
//        do
//            {
//            final BufferedImage tmp;
//            final Graphics2D g2;
//
//            if (higherQuality && w > targetWidth)
//                {
//                w >>= 1;
//
//                if (w < targetWidth)
//                    {
//                    w = targetWidth;
//                    }
//                }
//
//            if (higherQuality && h > targetHeight)
//                {
//                h >>= 1;
//
//                if (h < targetHeight)
//                    {
//                    h = targetHeight;
//                    }
//                }
//
//            tmp = new BufferedImage(w, h, type);
//            g2 = tmp.createGraphics();
//            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
//            g2.drawImage(ret, 0, 0, w, h, null);
//            g2.dispose();
//
//            ret = tmp;
//            }
//        while (w != targetWidth || h != targetHeight);
//
//        return (ret);
//        }



