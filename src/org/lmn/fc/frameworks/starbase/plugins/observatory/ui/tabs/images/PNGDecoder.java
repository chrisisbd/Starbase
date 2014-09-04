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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.images;

import javax.swing.*;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.zip.CRC32;
import java.util.zip.InflaterInputStream;


public class PNGDecoder
    {
    public static void main(final String[] args) throws
                                           Exception
        {
        String name = "logo.png";
        if (args.length > 0)
            {
            name = args[0];
            }
        final InputStream in = PNGDecoder.class.getResourceAsStream(name);
        final BufferedImage image = decode(in);
        in.close();

        final JFrame f = new JFrame()
        {
        public void paint(final Graphics g)
            {
            final Insets insets = getInsets();
            g.drawImage(image,
                        insets.left,
                        insets.top,
                        null);
            }
        };
        f.setVisible(true);
        final Insets insets = f.getInsets();
        f.setSize(image.getWidth() + insets.left + insets.right,
                  image
                          .getHeight()
                          + insets.top + insets.bottom);
        }


    public static BufferedImage decode(final InputStream in) throws
                                                       IOException
        {
        final DataInputStream dataIn = new DataInputStream(in);
        readSignature(dataIn);
        final PNGData chunks = readChunks(dataIn);

        final long widthLong = chunks.getWidth();
        final long heightLong = chunks.getHeight();
        if (widthLong > Integer.MAX_VALUE || heightLong > Integer.MAX_VALUE)
            {
            throw new IOException("That image is too wide or tall.");
            }
        final int width = (int) widthLong;
        final int height = (int) heightLong;

        final ColorModel cm = chunks.getColorModel();
        final WritableRaster raster = chunks.getRaster();

        final BufferedImage image = new BufferedImage(cm,
                                                raster,
                                                false,
                                                null);

        return image;
        }


    protected static void readSignature(final DataInputStream in) throws
                                                            IOException
        {
        final long signature = in.readLong();
        if (signature != 0x89504e470d0a1a0aL)
            {
            throw new IOException("PNG signature not found!");
            }
        }


    protected static PNGData readChunks(final DataInputStream in) throws
                                                            IOException
        {
        final PNGData chunks = new PNGData();

        boolean trucking = true;
        while (trucking)
            {
            try
                {
                // Read the length.
                final int length = in.readInt();
                if (length < 0)
                    {
                    throw new IOException("Sorry, that file is too long.");
                    }
                // Read the type.
                final byte[] typeBytes = new byte[4];
                in.readFully(typeBytes);
                // Read the data.
                final byte[] data = new byte[length];
                in.readFully(data);
                // Read the CRC.
                final long crc = in.readInt() & 0x00000000ffffffffL; // Make it
                // unsigned.
                if (verifyCRC(typeBytes,
                              data,
                              crc) == false)
                    {
                    throw new IOException("That file appears to be corrupted.");
                    }

                final PNGChunk chunk = new PNGChunk(typeBytes,
                                              data);
                chunks.add(chunk);
                }
            catch (EOFException eofe)
                {
                trucking = false;
                }
            }
        return chunks;
        }


    protected static boolean verifyCRC(final byte[] typeBytes,
                                       final byte[] data,
                                       final long crc)
        {
        final CRC32 crc32 = new CRC32();
        crc32.update(typeBytes);
        crc32.update(data);
        final long calculated = crc32.getValue();
        return (calculated == crc);
        }
    }

class PNGData
    {
    private int mNumberOfChunks;

    private PNGChunk[] mChunks;


    public PNGData()
        {
        mNumberOfChunks = 0;
        mChunks = new PNGChunk[10];
        }


    public void add(final PNGChunk chunk)
        {
        mChunks[mNumberOfChunks++] = chunk;
        if (mNumberOfChunks >= mChunks.length)
            {
            final PNGChunk[] largerArray = new PNGChunk[mChunks.length + 10];
            System.arraycopy(mChunks,
                             0,
                             largerArray,
                             0,
                             mChunks.length);
            mChunks = largerArray;
            }
        }


    public long getWidth()
        {
        return getChunk("IHDR").getUnsignedInt(0);
        }


    public long getHeight()
        {
        return getChunk("IHDR").getUnsignedInt(4);
        }


    public short getBitsPerPixel()
        {
        return getChunk("IHDR").getUnsignedByte(8);
        }


    public short getColorType()
        {
        return getChunk("IHDR").getUnsignedByte(9);
        }


    public short getCompression()
        {
        return getChunk("IHDR").getUnsignedByte(10);
        }


    public short getFilter()
        {
        return getChunk("IHDR").getUnsignedByte(11);
        }


    public short getInterlace()
        {
        return getChunk("IHDR").getUnsignedByte(12);
        }


    public ColorModel getColorModel()
        {
        final short colorType = getColorType();
        final int bitsPerPixel = getBitsPerPixel();

        if (colorType == 3)
            {
            final byte[] paletteData = getChunk("PLTE").getData();
            final int paletteLength = paletteData.length / 3;
            return new IndexColorModel(bitsPerPixel,
                                       paletteLength,
                                       paletteData,
                                       0,
                                       false);
            }
        System.out.println("Unsupported color type: " + colorType);
        return null;
        }


    public WritableRaster getRaster()
        {
        final int width = (int) getWidth();
        final int height = (int) getHeight();
        final int bitsPerPixel = getBitsPerPixel();
        final short colorType = getColorType();

        if (colorType == 3)
            {
            final byte[] imageData = getImageData();
            final DataBuffer db = new DataBufferByte(imageData,
                                               imageData.length);
            final WritableRaster raster = Raster.createPackedRaster(db,
                                                              width,
                                                              height,
                                                              bitsPerPixel,
                                                              null);
            return raster;
            }
        else
            {
            System.out.println("Unsupported color type!");
            }
        return null;
        }


    public byte[] getImageData()
        {
        try
            {
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            // Write all the IDAT data into the array.
            for (int i = 0;
                 i < mNumberOfChunks;
                 i++)
                {
                final PNGChunk chunk = mChunks[i];
                if (chunk.getTypeString().equals("IDAT"))
                    {
                    out.write(chunk.getData());
                    }
                }
            out.flush();
            // Now deflate the data.
            final InflaterInputStream in = new InflaterInputStream(
                    new ByteArrayInputStream(out.toByteArray()));
            final ByteArrayOutputStream inflatedOut = new ByteArrayOutputStream();
            int readLength;
            final byte[] block = new byte[8192];
            while ((readLength = in.read(block)) != -1)
                {
                inflatedOut.write(block,
                                  0,
                                  readLength);
                }
            inflatedOut.flush();
            final byte[] imageData = inflatedOut.toByteArray();
            // Compute the real length.
            final int width = (int) getWidth();
            final int height = (int) getHeight();
            final int bitsPerPixel = getBitsPerPixel();
            final int length = width * height * bitsPerPixel >> 3;

            final byte[] prunedData = new byte[length];

            // We can only deal with non-interlaced images.
            if (getInterlace() == 0)
                {
                int index = 0;
                for (int i = 0;
                     i < length;
                     i++)
                    {
                    if (((i << 3) / bitsPerPixel) % width == 0)
                        {
                        index++; // Skip the filter byte.
                        }
                    prunedData[i] = imageData[index++];
                    }
                }
            else
                {
                System.out.println("Couldn't undo interlacing.");
                }

            return prunedData;
            }
        catch (IOException ioe)
            {
            System.out.println("PNG ERROR!");
            }
        return null;
        }


    public PNGChunk getChunk(final String type)
        {
        for (int i = 0;
             i < mNumberOfChunks;
             i++)
            {
            if (mChunks[i].getTypeString().equals(type))
                {
                return mChunks[i];
                }
            }
        return null;
        }
    }

class PNGChunk
    {
    private byte[] mType;

    private byte[] mData;


    public PNGChunk(final byte[] type,
                    final byte[] data)
        {
        mType = type;
        mData = data;
        }


    public String getTypeString()
        {
        try
            {
            return new String(mType,
                              "UTF8");
            }
        catch (UnsupportedEncodingException uee)
            {
            return "";
            }
        }


    public byte[] getData()
        {
        return mData;
        }


    public long getUnsignedInt(final int offset)
        {
        long value = 0;
        for (int i = 0;
             i < 4;
             i++)
            {
            value += (mData[offset + i] & 0xff) << ((3 - i) << 3);
            }
        return value;
        }


    public short getUnsignedByte(final int offset)
        {
        return (short) (mData[offset] & 0x00ff);
        }
    }

