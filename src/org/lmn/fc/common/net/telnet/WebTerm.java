package org.lmn.fc.common.net.telnet;

/*
 * WebTerm version 1.3
 * ~~~~~~~
 *
 * Telnet session client, VT100 terminal emulator, and terminal screen
 * driver.
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
 * 1.3: Divided classes into separate files.
 *
 * 1.2: Added support for text styles.
 *
 * 1.1: Applet arguments are now handed to the Telnet class, which
 *      parses the new prompt[n], reply[n], and endprompt[n] arguments.
 *
 * 1.0: First release.
 *
 */

import org.lmn.fc.ui.components.UIComponent;

import java.awt.*;

public class WebTerm extends UIComponent implements Parameters
    {

    static final boolean DEBUG = false;

    static public String VersionString = "1.3";

    Terminal terminal;
    Emulator emulator;
    Telnet telnet;
    GridBagLayout layout;
    Button conn_but;
    Button dis_but;
    TextField host_txt;
    TextField port_txt;

    public WebTerm()
        {
        super();
        }


    public String getAppletInfo()
        {
        return ("WebTerm v" + VersionString + ".\n\n" +
            "This program is Copyright (C) 1996 by\r\n" +
            "Oregon State University, Corvallis, Oregon and the\n" +
            "Northwest Alliance for Computational Science\n" +
            "and Engineering (NACSE).  All Rights Reserved.\n");
        }

    public String[][] getParameterInfo()
        {
        String[][] telnet_info = Telnet.getParameterInfo();
        String[][] terminal_info = Terminal.getParameterInfo();
        String[][] info =
            new String[telnet_info.length + terminal_info.length + 2][];
        String[][] applet_info = {
            {"host", "string", "initial name of telnet host"},
            {"port", "integer", "initial port number (Telnet is 23)"}
        };

        int i = 0;

        for (int pos = 0; i < telnet_info.length; i++, pos++)
            {
            info[i] = telnet_info[pos];
            }
        for (int pos = 0; i < terminal_info.length; i++, pos++)
            {
            info[i] = terminal_info[pos];
            }
        for (int pos = 0; i < applet_info.length; i++, pos++)
            {
            info[i] = applet_info[pos];
            }

        return info;
        }

    /* Implement our parameter interface */
    public String getParameter(String name)
        {
        //return super.getParameter(name);
        return "";
        }

    boolean written_copyright = false;

    public void start()
        {
        if (!written_copyright && terminal != null)
            {
            written_copyright = true;
            terminal.write("\r\nWebTerm v" + VersionString + ":\r\n" +
                           "VT100 terminal emulator and telnet client\r\n" +
                           "written by Dianne Hackborn and Melanie Johnson.\r\n" +
                           "\r\nCopyright (C) 1996 by Oregon State University and\r\n" +
                           "the Northwest Alliance for Computational Science\r\n" +
                           "and Engineering (NACSE).  All Rights Reserved.\r\n" +
                           "\r\nPlease address correspondence to " +
                           "nacse-questions@nacse.org.\r\n\r\n",
                           0);
            }
        }

// Initialize applet -- create GUI components, emulator, telnet client.
    public void initialiseUI()
        {
        String host = getParameter("host");
        String port = getParameter("port");
        Label lab;

//        if (host == null) host = getCodeBase().getHost();
        if (port == null) port = "23";

        GridBagConstraints cn = new GridBagConstraints();
        layout = new GridBagLayout();
        this.setLayout(layout);

        // Create the top row of controls.

        cn.gridx = cn.RELATIVE;
        cn.gridy = 0;
        cn.gridwidth = 1;
        cn.gridheight = 1;
        cn.fill = cn.NONE;
        cn.anchor = cn.CENTER;
        cn.weightx = 0;
        cn.weighty = 0;

        conn_but = new Button("Connect");
        layout.setConstraints(conn_but, cn);
        this.add(conn_but);
        dis_but = new Button("Disconnect");
        layout.setConstraints(dis_but, cn);
        this.add(dis_but);
        dis_but.disable();

        lab = new Label("Host:");
        layout.setConstraints(lab, cn);
        this.add(lab);

        cn.fill = cn.HORIZONTAL;
        cn.weightx = 2;
        host_txt = new RubberTextField(host, 100);
        layout.setConstraints(host_txt, cn);
        this.add(host_txt);

        cn.fill = cn.NONE;
        cn.weightx = 0;
        lab = new Label("Port:");
        layout.setConstraints(lab, cn);
        this.add(lab);

        cn.fill = cn.HORIZONTAL;
        cn.weightx = 1;
        port_txt = new RubberTextField(port, 5);
        layout.setConstraints(port_txt, cn);
        this.add(port_txt);

        // Create the terminal screen area.

        cn.gridx = 0;
        cn.gridy = 1;
        cn.gridwidth = cn.REMAINDER;
        cn.gridheight = 1;
        cn.fill = cn.BOTH;
        cn.anchor = cn.CENTER;
        cn.weightx = 1;
        cn.weighty = 1;

        terminal = new Terminal();
        //terminal.resize(this.size());
        layout.setConstraints(terminal, cn);
        this.add(terminal);

        terminal.parseParameters((Parameters) this);
        terminal.init();

        //this.resize(this.size());
        this.layout();

        telnet = new Telnet(null);
        telnet.parseParameters((Parameters) this);

        emulator = new VT100Emulator(terminal, telnet);
        }

    public void destroy()
        {
        if (telnet != null) telnet.disconnect();
        }

    public boolean action(Event event, Object arg)
        {
        if (DEBUG) System.out.println("Applet: " + event);
        if (event.target == conn_but)
            {
            if (telnet != null)
                {
                try
                    {
                    telnet.connect(host_txt.getText(),
                                   Integer.parseInt(port_txt.getText()));
                    }
                catch (NumberFormatException ex)
                    {
                    if (terminal != null)
                        terminal.write("\r\nPort must be a number.\r\n", 0);
                    }
                }
            return true;
            }
        else if (event.target == dis_but)
            {
            if (telnet != null) telnet.disconnect();
            return true;
            }
        else if (event.target == terminal && event.id == Event.ACTION_EVENT)
            {
            if (arg == Terminal.ACTION_DISCONNECT)
                {
                disconnect();
                return true;
                }
            else if (arg == Terminal.ACTION_CONNECT)
                {
                connect();
                return true;
                }
            }
        return super.action(event, arg);
        }

    public void connect()
        {
        conn_but.disable();
        dis_but.enable();
        host_txt.disable();
        port_txt.disable();
        if (terminal != null) terminal.requestFocus();
        }

    public void disconnect()
        {
        conn_but.enable();
        dis_but.disable();
        host_txt.enable();
        port_txt.enable();
        conn_but.requestFocus();
        if (terminal != null) terminal.setStyle(terminal.STYLE_PLAIN);
        }
    }

class RubberTextField extends TextField
    {
    public RubberTextField()
        {
        super();
        }

    public RubberTextField(int cols)
        {
        super(cols);
        }

    public RubberTextField(String text)
        {
        super(text);
        }

    public RubberTextField(String text, int cols)
        {
        super(text, cols);
        }

    //public Dimension preferredSize() { return preferredSize(2); }
    public Dimension minimumSize()
        {
        return minimumSize(2);
        }
    }
