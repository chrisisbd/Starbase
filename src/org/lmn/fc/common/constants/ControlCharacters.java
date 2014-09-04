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

package org.lmn.fc.common.constants;

public enum ControlCharacters
    {
    NUL (0x00, "<nul>"),
    SOH (0x01, "<soh>"),
    STX (0x02, "<stx>"),
    ETX (0x03, "<etx>"),
    EOT (0x04, "<eot>"),
    ENQ (0x05, "<enq>"),
    ACK (0x06, "<ack>"),
    BEL (0x07, "<bel>"),
    BS (0x08, "<bs>"),
    HT (0x09, "<ht>"),
    LF (0x0A, "<lf>"),
    VT (0x0B, "<vt>"),
    FF (0x0C, "<ff>"),
    CR (0x0D, "<cr>"),
    SO (0x0E, "<so>"),
    SI (0x0F, "<si>"),
    DLE (0x10, "<dle>"),
    XON (0x11, "<xon>"),
    DC2 (0x12, "<dc2>"),
    XOFF (0x13, "<xoff>"),
    DC4 (0x14, "<dc4>"),
    NAK (0x15, "<nak>"),
    SYN (0x16, "<syn>"),
    ETB (0x17, "<etb>"),
    CAN (0x18, "<can>"),
    EM (0x19, "<em>"),
    SUB (0x1A, "<sub>"),
    ESC (0x1B, "<esc>"),
    FS (0x1C, "<fs>"),
    GS (0x1D, "<gs>"),
    RS (0x1E, "<rs>"),
    US (0x1F, "<us>");


    private final int byteCode;
    private final String strSymbol;

    private ControlCharacters(final int bytecode,
                              final String symbol)
        {
        this.byteCode = bytecode;
        this.strSymbol = symbol;
        }


    public char getAsChar()
        {
        return ((char)byteCode);
        }


    public byte getByteCode()
        {
        return ((byte)byteCode);
        }


    public String getSymbol()
        {
        return strSymbol;
        }
    }
