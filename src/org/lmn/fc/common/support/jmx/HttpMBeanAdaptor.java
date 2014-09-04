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

package org.lmn.fc.common.support.jmx;

import com.sun.jdmk.comm.AuthInfo;
import com.sun.jdmk.comm.HtmlAdaptorServer;


public final class HttpMBeanAdaptor
    {
    public static final String IDENTIFIER_MBEAN_HTTP = "JMX:Name=HttpAdaptor,Type=Management";

    private HtmlAdaptorServer serverAdaptor;
    private final String userName;
    private final String password;
    private static final int DEFAULT_PORT = 8082;
    private int port;

    /**
     * Creates a new HttpMBeanAdaptor object.
     *
     * @param userName -
     * @param password -
     */
    public HttpMBeanAdaptor(final String userName,
                            final String password,
                            final String strPort)
        {
        super();
        this.userName = userName;
        this.password = password;

        try
            {
            this.port = Integer.parseInt(strPort);
            }
        catch(NumberFormatException e)
            {
            this.port = DEFAULT_PORT;
            }
        }

    public final HtmlAdaptorServer getServer()
        {
        return (this.serverAdaptor);
        }
    /**
     * Start Http Adaptor
     */
    public final void start()
        {
        serverAdaptor =  new HtmlAdaptorServer();

        final AuthInfo userInfo = new AuthInfo(this.userName, this.password);
        serverAdaptor.addUserAuthenticationInfo(userInfo);
        serverAdaptor.setPort(this.port);

        serverAdaptor.start();
        }


    /**
     * Stop Http Adaptor
     */
    public final void stop()
        {
        serverAdaptor = new HtmlAdaptorServer();

        serverAdaptor.stop();
        }
    }