package org.lmn.fc.common.net.telnet;

/*
 * WebTerm version 1.3
 * ~~~~~~~
 *
 * Emulator class: an abstract class that defines the interface
 * to a terminal emulator.  It typically sits between a Terminal class,
 * where it interacts with the user, and a Telnet class, which it
 * uses to communicate with a remote machine.
 *
 * Written by Dianne Hackborn and Melanie Johnson.
 *
 * The design and implementation of WebTerm are available for
 * royalty-free adoption and use for non-commercial purposes, by any
 * public or private organization.  Copyright is retained by the
 * Northwest Alliance for Computational Science and Engineering and
 * Oregon State University.  Redistribution of any part of WebTerm or
 * any derivative works must include this notice.
 *
 * All Rights Reserved.
 *
 * Please address correspondence to nacse-questions@nacse.org.
 *
 * ----------------------------------------------------------------------
 *
 * Known Bugs
 * ~~~~~~~~~~
 *
 * ----------------------------------------------------------------------
 *
 * History
 * ~~~~~~~
 *
 * 1.3: Created this file.
 *
 */

import java.awt.*;
import java.io.IOException;

public class Emulator
    {

    static final boolean DEBUG = false;

    Terminal terminal;
    Telnet telnet;

    public Emulator(Terminal terminal, Telnet telnet)
        {
        setTerminal(terminal);
        setTelnet(telnet);
        }

    public String getName(int num)
        {
        return "DUMB";
        }

    public synchronized void setTerminal(Terminal terminal)
        {
        this.terminal = terminal;
        if (terminal != null)
            {
            terminal.setEmulator(this);
            }
        }

    public synchronized void setTelnet(Telnet telnet)
        {
        this.telnet = telnet;
        if (telnet != null)
            {
            telnet.setEmulator(this);
            if (terminal != null)
                {
                setWindowSize(terminal.getNumCols(), terminal.getNumRows());
                }
            }
        }

    public Terminal getTerminal()
        {
        return this.terminal;
        }

    public Telnet getTelnet()
        {
        return this.telnet;
        }

    public synchronized void setWindowSize(int cols, int rows)
        {
        if (telnet != null)
            {
            telnet.setWindowSize(getNumCols(), getNumRows());
            }
        }

    public int getNumRows()
        {
        if (terminal != null) return terminal.getNumRows();
        return 0;
        }

    public int getNumCols()
        {
        if (terminal != null) return terminal.getNumCols();
        return 0;
        }

    public void connect(String host, int port)
        {
        if (terminal != null) terminal.connect(host, port);
        }

    public void disconnect()
        {
        if (terminal != null) terminal.disconnect();
        }

    public synchronized void receive(char[] d, int off, int len)
        {
        int cnt = 0;
        while (len > 0)
            {
            if (!checkChar(d[off + cnt]))
                {
                if (cnt > 0)
                    {
                    if (terminal != null)
                        terminal.write(d, off, cnt,
                                       terminal.OUTF_PARTIAL);
                    off += cnt;
                    cnt = 0;
                    }
                doChar(d[off]);
                off++;
                }
            else
                {
                cnt++;
                }
            len--;
            }
        if (terminal != null) terminal.write(d, off, cnt, 0);
        }

    public synchronized void receive(String str)
        {
        receive(str.toCharArray(), 0, str.length());
        }

    public synchronized void receive(char c)
        {
        if (checkChar(c))
            {
            if (terminal != null) terminal.write(c, 0);
            }
        else
            {
            doChar(c);
            if (terminal != null) terminal.write(null, 0, 0, terminal.OUTF_PARTIAL);
            }
        }

    public synchronized void send(char[] d, int off, int len)
        {
        if (telnet != null)
            {
            byte[] b = new byte[len];
            int i = 0;
            while (len > 0)
                {
                b[i] = (byte) d[off];
                i++;
                off++;
                len--;
                }
            try
                {
                telnet.write(b);
                }
            catch (IOException ex)
                {
                if (terminal != null)
                    {
                    terminal.write("\r\n" + ex.toString(), 0);
                    }
                }
            }
        }

    public synchronized void send(String str)
        {
        if (telnet != null)
            {
            try
                {
                telnet.write(str);
                }
            catch (IOException ex)
                {
                if (terminal != null)
                    {
                    terminal.write("\r\n" + ex.toString(), 0);
                    }
                }
            }
        }

    public synchronized void send(char c)
        {
        if (telnet != null)
            {
            try
                {
                telnet.write((int) c);
                }
            catch (IOException ex)
                {
                if (terminal != null)
                    {
                    terminal.write("\r\n" + ex.toString(), 0);
                    }
                }
            }
        }

    public synchronized void handleEvent(Event e)
        {
        }

    public boolean checkChar(char c)
        {
        return true;
        }

    /* Override this method to implement a particular emulator.  Return
       true to pass the given character on the the terminal untouched,
       or false if doing your own processing of it. */
    public boolean doChar(char c)
        {
        return true;
        }
    }
