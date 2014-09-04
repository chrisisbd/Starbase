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

package org.lmn.fc.common.utilities.terminal;


public final class SerialPortData
    {
    private String strPortOwner;
    private String strPortName;
    private int intBaudrate;
    private int intDatabits;
    private int intStopbits;
    private int intParity;
    private String strFlowControl;
    private int intBufferSize;
    private boolean boolLocalEcho;


    /***********************************************************************************************
     * SerialPortData.
     */

    public SerialPortData()
        {
        setPortOwner("SerialPortData");
        setPortName("COM1");
        setBaudrate(4800);
        setDatabits(8);
        setStopbits(1);
        setParity(0);
        setFlowControl("None");
        setBufferSize(10000);
        setLocalEcho(false);
        }


    /***********************************************************************************************
     *
     * @param portowner
     * @param portname
     * @param baudrate
     * @param databits
     * @param stopbits
     * @param parity
     * @param flowcontrol
     * @param buffersize
     * @param localecho
     */

    public SerialPortData(final String portowner,
                          final String portname,
                          final int baudrate,
                          final int databits,
                          final int stopbits,
                          final int parity,
                          final String flowcontrol,
                          final int buffersize,
                          final boolean localecho)
        {
        strPortOwner = portowner;
        strPortName = portname;
        intBaudrate = baudrate;
        intDatabits = databits;
        intStopbits = stopbits;
        intParity = parity;
        strFlowControl = flowcontrol;
        intBufferSize = buffersize;
        boolLocalEcho = localecho;
        }



    public final String getPortOwner()
        {
        return strPortOwner;
        }

    public final void setPortOwner(final String strPortOwner)
        {
        this.strPortOwner = strPortOwner;
        }

    public final String getPortName()
        {
        return strPortName;
        }

    public final void setPortName(final String strPortName)
        {
        this.strPortName = strPortName;
        }

    public final int getBaudrate()
        {
        return intBaudrate;
        }

    public final void setBaudrate(final int intBaudrate)
        {
        this.intBaudrate = intBaudrate;
        }

    public final int getDatabits()
        {
        return intDatabits;
        }

    public final void setDatabits(final int intDatabits)
        {
        this.intDatabits = intDatabits;
        }

    public final int getStopbits()
        {
        return intStopbits;
        }

    public final void setStopbits(final int intStopbits)
        {
        this.intStopbits = intStopbits;
        }

    public final int getParity()
        {
        return intParity;
        }

    public final void setParity(final int intParity)
        {
        this.intParity = intParity;
        }

    public final String getFlowControl()
        {
        return strFlowControl;
        }

    public final void setFlowControl(final String strFlowControl)
        {
        this.strFlowControl = strFlowControl;

        }

    public final int getBufferSize()
        {
        return (this.intBufferSize);
        }


    public final void setBufferSize(final int size)
        {
        this.intBufferSize = size;
        }


    public boolean isLocalEcho()
        {
        return boolLocalEcho;
        }


    public void setLocalEcho(final boolean localecho)
        {
        this.boolLocalEcho = localecho;
        }
    }
