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

/*
 * This file is part of "JTA - Telnet/SSH for the JAVA(tm) platform".
 *
 * (c) Matthias L. Jugel, Marcus Meißner 1996-2005. All Rights Reserved.
 *
 * Please visit http://javatelnet.org/ for updates and contact.
 *
 * --LICENSE NOTICE--
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 * --LICENSE NOTICE--
 *
 */
package org.lmn.fc.frameworks.starbase.ui.telnet;

import de.mud.jta.Common;
import de.mud.jta.Plugin;
import de.mud.jta.event.FocusStatusListener;
import de.mud.jta.event.OnlineStatusListener;
import de.mud.jta.event.ReturnFocusRequest;
import de.mud.jta.event.SocketRequest;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.ui.components.BlankUIComponent;
import org.lmn.fc.ui.components.UIComponent;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;


/**
 * <B>JTA - Telnet/SSH for the JAVA(tm) platform</B><P>
 * This is the implementation of whole set of applications. It's modular
 * structure allows to configure the software to act either as a sophisticated
 * terminal emulation and/or, adding the network backend, as telnet
 * implementation. Additional modules provide features like scripting or an
 * improved graphical user interface.<P>
 * This software is written entirely in Java<SUP>tm</SUP>.<P>
 * This is the main program for the command line telnet. It initializes the
 * system and adds all needed components, such as the telnet backend and
 * the terminal front end. In contrast to applet functionality it parses
 * command line arguments used for configuring the software. Additionally
 * this application is not restricted in the sense of Java<SUP>tmp</SUP>
 * security.
 * <p/>
 * <B>Maintainer:</B> Matthias L. Jugel
 *
 * @author Matthias L. Jugel, Marcus Meissner
 * @version $Id: Main.java 499 2005-09-29 08:24:54Z leo $
 */

public class TelnetUI extends UIComponent
    {
    private static final String DEFAULT_CONFIG = "/de/mud/jta/default.conf";
    private static final String DEFAULT_CLIPBOARD_NAME = "de.mud.jta.Main";

    private static final String KEY_SOCKET_HOST = "Socket.host";
    private static final String KEY_SOCKET_PORT = "Socket.port";
    private static final String KEY_TERMINAL_ID = "Terminal.id";
    private static final String KEY_TERMINAL_BUFFER = "Terminal.buffer";
    private static final String KEY_TIMEOUT_SECONDS = "Timeout.seconds";
    private static final String KEY_TIMEOUT_COMMAND = "Timeout.command";
    private static final String KEY_TERMINAL_FONT = "Terminal.font";
    private static final String KEY_TERMINAL_FONT_STYLE = "Terminal.fontStyle";
    private static final String KEY_TERMINAL_FONT_SIZE = "Terminal.fontSize";
    private static final String KEY_TERMINAL_FOREGROUND = "Terminal.foreground";
    private static final String KEY_TERMINAL_BACKGROUND = "Terminal.background";
    private static final String KEY_TERMINAL_CURSOR_FOREGROUND = "Terminal.cursor.foreground";
    private static final String KEY_TERMINAL_CURSOR_BACKGROUND = "Terminal.cursor.background";
    private static final String KEY_PLUGINS = "plugins";

    private static final String PLUGINS_SOCKET_TELNET_TERMINAL = "Socket,Telnet,Terminal";
    private static final String PLUGINS_SOCKET_SSH_TERMINAL = "Socket,SSH,Terminal";

    private Plugin focussedPlugin;
    private Common setup;

    // Resources
    private TaskPlugin pluginTask;
    private String strHost;
    private int intPort;
    private boolean boolEnableSSH;
    private String strID;
    private int intBufferSize;
    private int intTimeout;
    private String strCommand;
    private FontInterface fontDisplay;
    private ColourInterface colourForeground;
    private ColourInterface colourBackground;
    private ColourInterface colourCursorForeground;
    private ColourInterface colourCursorBackground;


    /***********************************************************************************************
     * Debug the Telnet Options.
     *
     * @param options
     */

    private static void debugOptions(final Properties options)
        {
        LOGGER.debug("Telnet Options");
        LOGGER.debug(INDENT + KEY_SOCKET_HOST + EQUALS + options.getProperty(KEY_SOCKET_HOST));
        LOGGER.debug(INDENT + KEY_SOCKET_PORT + EQUALS + options.getProperty(KEY_SOCKET_PORT));
        LOGGER.debug(INDENT + KEY_TERMINAL_ID + EQUALS + options.getProperty(KEY_TERMINAL_ID));
        LOGGER.debug(INDENT + KEY_TERMINAL_BUFFER + EQUALS + options.getProperty(KEY_TERMINAL_BUFFER));
        LOGGER.debug(INDENT + KEY_TIMEOUT_SECONDS + EQUALS + options.getProperty(KEY_TIMEOUT_SECONDS));
        LOGGER.debug(INDENT + KEY_TIMEOUT_COMMAND + EQUALS + options.getProperty(KEY_TIMEOUT_COMMAND));
        LOGGER.debug(INDENT + KEY_TERMINAL_FONT + EQUALS + options.getProperty(KEY_TERMINAL_FONT));
        LOGGER.debug(INDENT + KEY_TERMINAL_FONT_STYLE + EQUALS + options.getProperty(KEY_TERMINAL_FONT_STYLE));
        LOGGER.debug(INDENT + KEY_TERMINAL_FONT_SIZE + EQUALS + options.getProperty(KEY_TERMINAL_FONT_SIZE));
        LOGGER.debug(INDENT + KEY_TERMINAL_FOREGROUND + EQUALS + options.getProperty(KEY_TERMINAL_FOREGROUND));
        LOGGER.debug(INDENT + KEY_TERMINAL_BACKGROUND + EQUALS + options.getProperty(KEY_TERMINAL_BACKGROUND));
        LOGGER.debug(INDENT + KEY_TERMINAL_CURSOR_FOREGROUND + EQUALS + options.getProperty(KEY_TERMINAL_CURSOR_FOREGROUND));
        LOGGER.debug(INDENT + KEY_TERMINAL_CURSOR_BACKGROUND + EQUALS + options.getProperty(KEY_TERMINAL_CURSOR_BACKGROUND));
        }


    /***********************************************************************************************
     * Construct a TelnetUI.
     *
     * @param task
     * @param host
     * @param port
     * @param ssh
     * @param id
     * @param bufferSize
     * @param timeout
     * @param command
     * @param font
     * @param foreground
     * @param background
     * @param cursorForeground
     * @param cursorBackground
     */

    public TelnetUI(final TaskPlugin task,
                    final String host,
                    final int port,
                    final boolean ssh,
                    final String id,
                    final int bufferSize,
                    final int timeout,
                    final String command,
                    final FontInterface font,
                    final ColourInterface foreground,
                    final ColourInterface background,
                    final ColourInterface cursorForeground,
                    final ColourInterface cursorBackground)
        {
        // ToDo null checks
        this.pluginTask = task;
        this.strHost = host;
        this.intPort = port;
        this.boolEnableSSH = ssh;
        this.strID = id;
        this.intBufferSize = bufferSize;
        this.intTimeout = timeout;
        this.strCommand = command;
        this.fontDisplay = font;
        this.colourForeground = foreground;
        this.colourBackground = background;
        this.colourCursorForeground = cursorForeground;
        this.colourCursorBackground = cursorBackground;
        }


    /***********************************************************************************************
     * Initialise this UIComponent.
     */

    public final void initialiseUI()
        {
        final Properties options;
        final Map componentList;
        final Iterator iterKeys;

        super.initialiseUI();

        //------------------------------------------------------------------------------------------
        // Set up the clipboard

        Clipboard clipboard;

        try
            {
            clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            }

        catch (HeadlessException e)
            {
            LOGGER.error("jta: system clipboard access denied");
            LOGGER.error("jta: copy & paste only within the JTA");
            clipboard = new Clipboard(DEFAULT_CLIPBOARD_NAME);
            }

        //------------------------------------------------------------------------------------------
        // Load the initial properties from the default config file in the jar

        options = new Properties();

        try
            {
            options.load(TelnetUI.class.getResourceAsStream(DEFAULT_CONFIG));
            }

        catch (IOException e)
            {
            LOGGER.error("jta: cannot load default.conf");
            }

        // Override the defaults with our Resources
        options.setProperty(KEY_SOCKET_HOST, strHost);
        options.setProperty(KEY_SOCKET_PORT, Integer.toString(intPort));

        options.setProperty(KEY_TERMINAL_ID, strID);
        options.setProperty(KEY_TERMINAL_BUFFER, Integer.toString(intBufferSize));
        options.setProperty(KEY_TIMEOUT_SECONDS, Integer.toString(intTimeout));
        options.setProperty(KEY_TIMEOUT_COMMAND, strCommand);

        options.setProperty(KEY_TERMINAL_FONT, fontDisplay.getFontName());
        options.setProperty(KEY_TERMINAL_FONT_STYLE, fontDisplay.getFontStyle());
        options.setProperty(KEY_TERMINAL_FONT_SIZE, Integer.toString(fontDisplay.getFontSize()));

        options.setProperty(KEY_TERMINAL_FOREGROUND, colourForeground.toHexFormat());
        options.setProperty(KEY_TERMINAL_BACKGROUND, colourBackground.toHexFormat());
        options.setProperty(KEY_TERMINAL_CURSOR_FOREGROUND, colourCursorForeground.toHexFormat());
        options.setProperty(KEY_TERMINAL_CURSOR_BACKGROUND, colourCursorBackground.toHexFormat());

        if (boolEnableSSH)
            {
            options.setProperty(KEY_PLUGINS, PLUGINS_SOCKET_SSH_TERMINAL);
            }
        else
            {
            options.setProperty(KEY_PLUGINS, PLUGINS_SOCKET_TELNET_TERMINAL);
            }
        debugOptions(options);

        // Read back the things we need
        strHost = options.getProperty(KEY_SOCKET_HOST);
        intPort = Integer.parseInt(options.getProperty(KEY_SOCKET_PORT));

        //------------------------------------------------------------------------------------------
        // Configure the application and load all plugins

        setup = new Common(options);

        setup.registerPluginListener(new OnlineStatusListener()
            {
            public void online()
                {
                pluginTask.setStatus("Telnet: " + strHost + ":" + intPort);
                }

            public void offline()
                {
                pluginTask.setStatus("Telnet: offline");
                }
            });

        // Register a focus status listener, so we know when a plugin got focus
        setup.registerPluginListener(new FocusStatusListener()
            {
            public void pluginGainedFocus(final Plugin plugin)
                {
                LOGGER.debug("Main: " + plugin + " got focus");
                focussedPlugin = plugin;
                }

            public void pluginLostFocus(final Plugin plugin)
                {
                // We ignore the lost focus
                LOGGER.debug("Main: " + plugin + " lost focus");
                }
            });

        // Add all visual components to this UIComponent
        componentList = setup.getComponents();
        iterKeys = componentList.keySet().iterator();

        while (iterKeys.hasNext())
            {
            final String name = (String) iterKeys.next();
            final JComponent component = (JComponent) componentList.get(name);

            if (options.getProperty("layout." + name) == null)
                {
                LOGGER.debug("jta: no layout property set for '" + name + "'");
                //frame.add("South", component);
                }
            else
                {
                add(options.getProperty("layout." + name), component);
                }
            }
        }


    /***********************************************************************************************
     * Run this UIComponent.
     */

    public void runUI()
        {
        super.runUI();

        if ((strHost != null)
            && (strHost.length() > 0)
            && (setup != null))
            {
            LOGGER.debug("TelnetUI.runUI() [host=" + strHost + "] [length=" + strHost.length() + "] [port=" + intPort + "]");
            try
                {
                //setup.broadcast(new SocketRequest());
                setup.broadcast(new SocketRequest(strHost, intPort));

                LOGGER.logAtomEvent(pluginTask.getParentAtom(),
                                    pluginTask,
                                    getClass().getName(),
                                    METADATA_TARGET
                                        + strHost
                                        + TERMINATOR
                                        + METADATA_ACTION_LOGIN,
                                    EventStatus.INFO);

                // Make sure the focus goes somewhere to start off with
                if (setup != null)
                    {
                    System.out.println("FOCUS");
                    setup.broadcast(new ReturnFocusRequest());
                    }
                }

            catch (Exception e)
                {
                removeAll();
                add((Component)new BlankUIComponent());
                System.out.println("no connect?");
                e.printStackTrace();
                }
            }
        else
            {
            removeAll();
            add((Component)new BlankUIComponent());
            pluginTask.setStatus("TelnetUI.runUI() No Host configuration");
            }
        }


    /***********************************************************************************************
     * Stop this UIComponent.
     */

    public void stopUI()
        {
        if (setup != null)
            {
            setup.broadcast(new SocketRequest());
            LOGGER.logAtomEvent(pluginTask.getParentAtom(),
                                pluginTask,
                                getClass().getName(),
                                METADATA_TARGET
                                    + strHost
                                    + TERMINATOR
                                    + METADATA_ACTION_LOGOUT,
                                EventStatus.INFO);
            }

        // ToDo remove listeners??

        super.stopUI();
        }


    /***********************************************************************************************
     * Dispose of all components of this UIComponent.
     */

    public void disposeUI()
        {
        stopUI();

        removeAll();
        }


    /***********************************************************************************************
     *
     * @param frame
     * @param setup
     * @param options
     */

//    private void personalJava(final JFrame frame,
//                              final Common setup,
//                              final Properties options)
//        {
//        Iterator names;
//        frame.addWindowListener(new WindowAdapter()
//            {
//            public void windowClosing(final WindowEvent evt)
//                {
//                setup.broadcast(new SocketRequest());
//                frame.setVisible(false);
//                frame.dispose();
//                System.exit(0);
//                }
//            });
//
//        // add a menu bar
//        final JMenuBar mb = new JMenuBar();
//        final JMenu file = new JMenu("File");
//        file.setMnemonic(KeyEvent.VK_F);
//        JMenuItem tmp;
//        file.add(tmp = new JMenuItem("Connect"));
//        tmp.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.SHIFT_MASK | KeyEvent.CTRL_MASK));
//        tmp.addActionListener(new ActionListener()
//            {
//            public void actionPerformed(final ActionEvent evt)
//                {
//                final String destination =
//                    JOptionPane.showInputDialog(frame,
//                                                new JLabel("Enter your destination host (host[:port])"),
//                                                "Connect", JOptionPane.QUESTION_MESSAGE
//                    );
//                if (destination != null)
//                    {
//                    int sep = 0;
//                    if ((sep = destination.indexOf(' ')) > 0 || (sep = destination.indexOf(':')) > 0)
//                        {
//                        host = destination.substring(0, sep);
//                        port = destination.substring(sep + 1);
//                        }
//                    else
//                        {
//                        host = destination;
//                        }
//                    setup.broadcast(new SocketRequest());
//                    setup.broadcast(new SocketRequest(host, Integer.parseInt(port)));
//                    }
//                }
//            });
//
//        file.add(tmp = new JMenuItem("Disconnect"));
//        tmp.addActionListener(new ActionListener()
//            {
//            public void actionPerformed(final ActionEvent evt)
//                {
//                setup.broadcast(new SocketRequest());
//                }
//            });
//        file.addSeparator();
//
//        if (setup.getComponents().get("Terminal") != null)
//            {
//            file.add(tmp = new JMenuItem("Print"));
//            tmp.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.CTRL_MASK));
//            tmp.addActionListener(new ActionListener()
//                {
//                public void actionPerformed(final ActionEvent evt)
//                    {
//                    final PrintJob printJob =
//                        frame.getToolkit().getPrintJob(frame, "JTA Terminal", null);
//                    // return if the user clicked cancel
//                    if (printJob == null) return;
//                    ((JComponent) setup.getComponents().get("Terminal"))
//                        .print(printJob.getGraphics());
//                    printJob.end();
//                    }
//                });
//            file.addSeparator();
//            }
//        file.add(tmp = new JMenuItem("Exit"));
//
//        tmp.addActionListener(new ActionListener()
//            {
//            public void actionPerformed(final ActionEvent evt)
//                {
//                frame.dispose();
//                System.exit(0);
//                }
//            });
//        mb.add(file);
//
//        final JMenu edit = new JMenu("Edit");
//        edit.add(tmp = new JMenuItem("Copy"));
//        tmp.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_MASK));
//
//        tmp.addActionListener(new ActionListener()
//            {
//            public void actionPerformed(final ActionEvent evt)
//                {
//                if (focussedPlugin instanceof VisualTransferPlugin)
//                    ((VisualTransferPlugin) focussedPlugin).copy(clipboard);
//                }
//            });
//        edit.add(tmp = new JMenuItem("Paste"));
//        tmp.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_MASK));
//
//        tmp.addActionListener(new ActionListener()
//            {
//            public void actionPerformed(final ActionEvent evt)
//                {
//                if (focussedPlugin instanceof VisualTransferPlugin)
//                    ((VisualTransferPlugin) focussedPlugin).paste(clipboard);
//                }
//            });
//        mb.add(edit);
//
//        final Map menuList = setup.getMenus();
//        names = menuList.keySet().iterator();
//        while (names.hasNext())
//            {
//            final String name = (String) names.next();
//            mb.add((JMenu) menuList.get(name));
//            }
//
//        final JMenu help = new JMenu("Help");
//        help.setMnemonic(KeyEvent.VK_HELP);
//        help.add(tmp = new JMenuItem("General"));
//
//        tmp.addActionListener(new ActionListener()
//            {
//            public void actionPerformed(final ActionEvent e)
//                {
//                Help.show(frame, options.getProperty("Help.url"));
//                }
//            });
//        mb.add(help);
//
//        frame.setJMenuBar(mb);
//        }
/**
 * Parse the command line argumens and override any standard options
 * with the new values if applicable.
 * <P><SMALL>
 * This method did not work with jdk 1.1.x as the setProperty()
 * method is not available. So it uses now the put() method from
 * Hashtable instead.
 * </SMALL>
 * @param options the original options
 * @param args the command line parameters
 * @return a possible error message if problems occur
 */
private String parseOptions(final Properties options, final String[] args) {
  boolean host = false, port = false;
  for (int n = 0; n < args.length; n++) {
    if (args[n].equals("-config"))
      if (!args[n + 1].startsWith("-"))
        options.put("Main.config", args[++n]);
      else
        return "missing parameter for -config";
    else if (args[n].equals("-plugins"))
      if (!args[n + 1].startsWith("-"))
        options.put("plugins", args[++n]);
      else
        return "missing parameter for -plugins";
    else if (args[n].equals("-addplugin"))
      if (!args[n + 1].startsWith("-"))
        options.put("plugins", args[++n] + "," + options.get("plugins"));
      else
        return "missing parameter for -addplugin";
    else if (args[n].equals("-term"))
      if (!args[n + 1].startsWith("-"))
        options.put(KEY_TERMINAL_ID, args[++n]);
      else
        return "missing parameter for -term";
    else if (!host) {
      options.put(KEY_SOCKET_HOST, args[n]);
      host = true;
    } else if (host && !port) {
      options.put(KEY_SOCKET_PORT, args[n]);
      port = true;
    } else
      return "unknown parameter '" + args[n] + "'";
  }
  return null;
}
    }

//        final String error = parseOptions(options, args);
//
//        if (error != null)
//            {
//            LOGGER.error(error);
//            LOGGER.error("usage: de.mud.jta.Main [-plugins pluginlist] "
//                + "[-addplugin plugin] "
//                + "[-config url_or_file] "
//                + "[-term id] [host [port]]");
//            System.exit(0);
//            }

//        final String config = options.getProperty("Main.config");
//
//        if (config != null)
//            {
//            try
//                {
//                options.load(new URL(config).openStream());
//                }
//
//            catch (IOException e)
//                {
//                try
//                    {
//                    options.load(new FileInputStream(config));
//                    }
//
//                catch (FileNotFoundException fe)
//                    {
//                    LOGGER.error("jta: cannot load " + config);
//                    }
//
//                catch (IOException fe)
//                    {
//                    LOGGER.error("jta: cannot load " + config);
//                    }
//                }
//            }
