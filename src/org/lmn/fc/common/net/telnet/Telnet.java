package org.lmn.fc.common.net.telnet;

/*
 * WebTerm version 1.3
 * ~~~~~~~
 *
 * Telnet class: a standard Telnet client.
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
 *
 * ----------------------------------------------------------------------
 *
 * History
 * ~~~~~~~
 *
 * 1.3: Created this file.
 *
 * 1.3.1: Now correctly processes multiple terminal types from emulator.
 *
 */

import org.lmn.fc.model.registry.RegistryPlugin;
import org.lmn.fc.model.registry.impl.Registry;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

class PromptInfo
    {
    char[] prompt = null;
    int pos = 0;
    String reply = null;
    boolean terminate = false;

    public PromptInfo(String prompt, String reply)
        {
        if (prompt != null) this.prompt = prompt.toCharArray();
        this.reply = reply;
        }

    public PromptInfo(String prompt, String reply, boolean term)
        {
        this(prompt, reply);
        this.terminate = term;
        }
    }

;

public class Telnet implements Runnable
    {
    private static final RegistryPlugin REGISTRY = Registry.getInstance();
    static final boolean DEBUG = false;

    static final int IAC = 255;
    static final int DONT = 254;
    static final int DO = 253;
    static final int WONT = 252;
    static final int WILL = 251;
    static final int SB = 250;
    static final int GA = 249;
    static final int EL = 248;
    static final int EC = 247;
    static final int AYT = 246;
    static final int AO = 245;
    static final int IP = 244;
    static final int BREAK = 243;
    static final int DM = 242;
    static final int NOP = 241;
    static final int SE = 240;
    static final int EOR = 239;
    static final int ABORT = 238;
    static final int SUSP = 237;
    static final int xEOF = 236;

    static final int SYNC = 242;

    static final int TELCMD_FIRST = xEOF;
    static final int TELCMD_LAST = IAC;

    static final String[] telcmds = {
        "EOF", "SUSP", "ABORT", "EOR",
        "SE", "NOP", "DMARK", "BRK", "IP", "AO", "AYT", "EC",
        "EL", "GA", "SB", "WILL", "WONT", "DO", "DONT", "IAC",
        null
    };

    static final int TELOPT_BINARY = 0;
    static final int TELOPT_ECHO = 1;
    static final int TELOPT_RCP = 2;
    static final int TELOPT_SGA = 3;
    static final int TELOPT_NAMS = 4;
    static final int TELOPT_STATUS = 5;
    static final int TELOPT_TM = 6;
    static final int TELOPT_RCTE = 7;
    static final int TELOPT_NAOL = 8;
    static final int TELOPT_NAOP = 9;
    static final int TELOPT_NAOCRD = 10;
    static final int TELOPT_NAOHTS = 11;
    static final int TELOPT_NAOHTD = 12;
    static final int TELOPT_NAOFFD = 13;
    static final int TELOPT_NAOVTS = 14;
    static final int TELOPT_NAOVTD = 15;
    static final int TELOPT_NAOLFD = 16;
    static final int TELOPT_XASCII = 17;
    static final int TELOPT_LOGOUT = 18;
    static final int TELOPT_BM = 19;
    static final int TELOPT_DET = 20;
    static final int TELOPT_SUPDUP = 21;
    static final int TELOPT_SUPDUPOUTPUT = 22;
    static final int TELOPT_SNDLOC = 23;
    static final int TELOPT_TTYPE = 24;
    static final int TELOPT_EOR = 25;
    static final int TELOPT_TUID = 26;
    static final int TELOPT_OUTMRK = 27;
    static final int TELOPT_TTYLOC = 28;
    static final int TELOPT_3270REGIME = 29;
    static final int TELOPT_X3PAD = 30;
    static final int TELOPT_NAWS = 31;
    static final int TELOPT_TSPEED = 32;
    static final int TELOPT_LFLOW = 33;
    static final int TELOPT_LINEMODE = 34;
    static final int TELOPT_XDISPLOC = 35;
    static final int TELOPT_OLD_ENVIRON = 36;
    static final int TELOPT_AUTHENTICATION = 37;
    static final int TELOPT_ENCRYPT = 38;
    static final int TELOPT_NEW_ENVIRON = 39;
    static final int TELOPT_EXOPL = 255;

    static final String[] telopts = {
        /*  1 */ "BINARY", "ECHO", "RCP", "SUPPRESS GO AHEAD", "NAME",
        /*  2 */ "STATUS", "TIMING MARK", "RCTE", "NAOL", "NAOP",
        /*  3 */ "NAOCRD", "NAOHTS", "NAOHTD", "NAOFFD", "NAOVTS",
        /*  4 */ "NAOVTD", "NAOLFD", "EXTEND ASCII", "LOGOUT", "BYTE MACRO",
        /*  5 */ "DATA ENTRY TERMINAL", "SUPDUP", "SUPDUP OUTPUT",
        /*  6 */ "SEND LOCATION", "TERMINAL TYPE", "END OF RECORD",
        /*  7 */ "TACACS UID", "OUTPUT MARKING", "TTYLOC",
        /*  8 */ "3270 REGIME", "X.3 PAD", "NAWS", "TSPEED", "LFLOW",
        /*  9 */ "LINEMODE", "XDISPLOC", "OLD-ENVIRON", "AUTHENTICATION",
        /* 10 */ "ENCRYPT", "NEW-ENVIRON",
        null
    };

    static final boolean[] telopts_impl = {
        /*  1 */ false, false, false, false, false,
        /*  2 */ false, false, false, false, false,
        /*  3 */ false, false, false, false, false,
        /*  4 */ false, false, false, false, false,
        /*  5 */ false, false, false,
        /*  6 */ false, true, false,
        /*  7 */ false, false, false,
        /*  8 */ false, false, true, false, false,
        /*  9 */ false, false, false, false,
        /* 10 */ false, false,
        false
    };

    static final int TELOPT_FIRST = TELOPT_BINARY;
    static final int TELOPT_LAST = TELOPT_NEW_ENVIRON;

    Emulator emulator;
    boolean connected = false;
    Socket s;
    BufferedInputStream in;
    OutputStream out;
    Thread thread;
    char[] parseBuff = null;
    int parsePos = 0;
    boolean isTelnet = false;

    int curWidth = 0, curHeight = 0;

    int curEmuName = -1;

    boolean doPrompts = true;
    PromptInfo[] prompts = null;

    boolean[] options = new boolean[TELOPT_LAST - TELOPT_FIRST + 1];

    public Telnet(Emulator emulator)
        {
        this.emulator = emulator;
        }

    public Telnet(Emulator emulator, String host, int port)
        {
        this(emulator);
        connect(host, port);
        }

    public static String[][] getParameterInfo()
        {
        String[][] info = {
            {"prompt#", "string", "prompt to look for in text stream"},
            {"reply#", "string", "reply to send when prompt# is encountered"},
            {"endprompts#", "boolean",
             "if true, turn of all prompts when this one is encountered"}
        };

        return info;
        }

    String parseString(String in)
        {
        if (in == null) return null;
        StringBuffer out = new StringBuffer();
        for (int i = 0; i < in.length(); i++)
            {
            if (in.charAt(i) != '\\')
                out.append(in.charAt(i));
            else if (++i < in.length())
                {
                switch (in.charAt(i))
                    {
                    case '\\':
                        out.append('\\');
                        break;
                    case 'n':
                        out.append('\n');
                        break;
                    case 'r':
                        out.append('\r');
                        break;
                    case 't':
                        out.append('\t');
                        break;
                    case 'f':
                        out.append('\f');
                        break;
                    case 'b':
                        out.append('\b');
                        break;
                    case 'u':
                        {
                        int c = 0;
                        for (int j = 0; j < 4 && i < in.length(); j++, i++)
                            {
                            c <<= 4;
                            c = c + (int) (in.charAt(i) - '0');
                            }
                        out.append((char) c);
                        }
                        break;
                    case 'x':
                        {
                        int c = 0;
                        for (int j = 0; j < 2 && i < in.length(); j++, i++)
                            {
                            c <<= 4;
                            c = c + (int) (in.charAt(i) - '0');
                            }
                        out.append((char) c);
                        }
                        break;
                    default:
                        if (in.charAt(i) >= '0' && in.charAt(i) <= '9')
                            {
                            int c = 0;
                            for (int j = 0; j < 3 && i < in.length(); j++, i++)
                                {
                                c <<= 3;
                                c = c + (int) (in.charAt(i) - '0');
                                }
                            out.append((char) c);
                            }
                    }
                }
            }
        return out.toString();
        }

    public void parseParameters(Parameters p)
        {
        boolean have_prompts = false;

        int num_prompts;
        for (num_prompts = 1;
             p.getParameter("prompt" + num_prompts) != null;
             num_prompts++)
            ;

        if (DEBUG) System.out.println("Found " + num_prompts + " prompts");

        prompts = new PromptInfo[num_prompts];

        for (int i = 0; i < num_prompts; i++)
            {
            boolean term = false;
            String term_s = p.getParameter("endprompts" + i);
            if (term_s != null && term_s.compareTo("true") == 0)
                term = true;
            else
                term = false;

            prompts[i] = new PromptInfo(parseString(p.getParameter("prompt" + i)),
                                        parseString(p.getParameter("reply" + i)),
                                        term);
            if (prompts[i].prompt != null) have_prompts = true;

            if (DEBUG)
                {
                char[] prompt = {};
                String reply = "";
                if (prompts[i].prompt != null) prompt = prompts[i].prompt;
                if (prompts[i].reply != null) reply = prompts[i].reply;
                System.out.println("Prompt " + i + ": prompt=" + prompt +
                                   ", reply=" + reply + ", term=" +
                                   prompts[i].terminate);
                }
            }

        if (!have_prompts) prompts = null;
        }

    public void clearPrompts()
        {
        doPrompts = false;
        if (prompts != null)
            {
            for (int i = 0; i < prompts.length; i++) prompts[i].pos = 0;
            }
        }

    public void setEmulator(Emulator emulator)
        {
        this.emulator = emulator;
        curEmuName = -1;
        if (emulator != null)
            {
            curWidth = emulator.getNumCols();
            curHeight = emulator.getNumRows();
            }
        else
            {
            curWidth = 0;
            curHeight = 0;
            }
        }

    public Emulator getEmulator()
        {
        return emulator;
        }

    public synchronized boolean connect(String host, int port)
        {
        disconnect();
        isTelnet = false;
        clearPrompts();
        doPrompts = true;
        if (emulator != null)
            {
            emulator.receive("\r\nOpening connection to " + host + "...\r\n");
            }
        try
            {
            if (DEBUG)
                System.out.println("Opening socket at " + host +
                                   " / " + port);
            s = new Socket(host, port);
            if (DEBUG) System.out.println("Getting input stream...");
            in = new BufferedInputStream(s.getInputStream());
            if (DEBUG) System.out.println("Getting output stream...");
            out = s.getOutputStream();
            connected = true;
            if (DEBUG) System.out.println("Starting input thread...");
            thread = new Thread(REGISTRY.getThreadGroup(), this, "Telnet input monitor");
            if (DEBUG) System.out.println("Thread: " + thread);
            // This is now an insecure function in Netscape 3.0!
            //thread.setDaemon(true);
            thread.start();
            }
        catch (UnknownHostException e)
            {
            if (emulator != null)
                {
                emulator.receive("\r\n" + e.toString() + "\r\n");
                }
            }
        catch (IOException e)
            {
            if (emulator != null)
                {
                emulator.receive("\r\n" + e.toString() + "\r\n");
                }
            }
        catch (Exception e)
            {
            if (emulator != null)
                {
                emulator.receive("\r\n" + e.toString() + "\r\n");
                }
            }
        finally
            {
            if (!connected)
                {
                if (DEBUG) System.out.println("Oops, disconnecting.");
                disconnect();
                if (emulator != null)
                    {
                    emulator.receive("\r\nUnable to connect to host: " + host + "\r\n");
                    }
                return false;
                }
            else
                {
                if (emulator != null) emulator.connect(host, port);
                }
            }
        if (DEBUG) System.out.println("Connection complete.");
        if (DEBUG) System.out.println("Active threads: " + Thread.activeCount());
        return true;
        }

    public synchronized void disconnect()
        {
        clearPrompts();
        if (thread != null && thread != Thread.currentThread())
            {
            if (DEBUG) System.out.println("Stopping input thread.");
            thread.stop();
            }
        if (connected && emulator != null) emulator.disconnect();
        if (s != null)
            {
            if (emulator != null)
                {
                emulator.receive("\r\nConnection closed.\r\n");
                }
            }
        try
            {
            if (out != null) out.close();
            if (in != null) in.close();
            if (s != null) s.close();
            }
        catch (IOException e)
            {
            if (emulator != null)
                {
                emulator.receive("\r\n" + e.toString() + "\r\n");
                }
            }
        finally
            {
            thread = null;
            out = null;
            in = null;
            s = null;
            connected = false;
            isTelnet = false;
            for (int i = 0; i < options.length; i++) options[i] = false;
            if (DEBUG)
                System.out.println("Active threads: " +
                                   Thread.activeCount());
            }
        }

    public synchronized void setWindowSize(int width, int height)
        {
        curWidth = width;
        curHeight = height;
        try
            {
            send_naws(width, height);
            }
        catch (IOException e)
            {
            if (emulator != null)
                {
                emulator.receive("\r\n" + e.toString() + "\r\n");
                }
            }
        }

    public synchronized void write(int b) throws IOException
        {
        if (DEBUG) System.out.println("Put: " + (char) b + " (" + b + ")");
        out.write(b);
        }

    public synchronized void write(byte[] b) throws IOException
        {
        String str = new String(b, 0);
        if (DEBUG) System.out.println("Put: " + str + " (" + b + ")");
        out.write(b);
        }

    public synchronized void write(String str) throws IOException
        {
        byte[] b = new byte[str.length()];
        str.getBytes(0, str.length(), b, 0);
        out.write(b);
        }

    public synchronized char next_char() throws IOException
        {
        if (parseBuff != null && parsePos < parseBuff.length)
            {
            parsePos++;
            return parseBuff[parsePos - 1];
            }
        parseBuff = null;
        parsePos = 0;
        return (char) in.read();
        }

    public void run()
        {
        boolean client_kill = false;
        if (DEBUG) System.out.println("Telnet input thread has started.");
        if (DEBUG) System.out.println("Telnet thread: " + Thread.currentThread());
        try
            {
            for (; ;)
                {
                int len = in.available();
                if (len > 1)
                    {
                    byte[] b = new byte[len];
                    String str;
                    char[] c;
                    if (DEBUG) System.out.println("Ready to read " + len + " bytes.");
                    len = in.read(b, 0, len);
                    str = new String(b, 0);
                    if (DEBUG) System.out.println("Actually read " + len + " bytes.");
                    if (len < 0) break;
                    c = str.toCharArray();
                    len = c.length;
                    if (DEBUG) System.out.println("Processing: " + str);
                    for (int i = 0; i < len;)
                        {
                        int j = i;
                        while (j < len && c[j] != IAC) j++;
                        if (j > i) process_data(c, i, j - i);
                        parseBuff = c;
                        parsePos = j + 1;
                        if (j < len) process_cmd(c[j]);
                        i = parsePos;
                        }
                    parseBuff = null;
                    }
                else
                    {
                    int b;
                    if (DEBUG) System.out.println("Ready to read a character.");
                    b = in.read();
                    parseBuff = null;
                    if (b < 0) break;
                    if (b == IAC)
                        process_cmd(b);
                    else
                        process_data((char) b);
                    }
                }
            }
        catch (IOException e)
            {
            if (emulator != null)
                {
                emulator.receive("\r\n" + e.toString() + "\r\n");
                }
            }
        finally
            {
            if (in != null && !client_kill && emulator != null)
                {
                //emulator.receive("\r\nConnection closed by server.\r\n");
                disconnect();
                }
            try
                {
                if (in != null) in.close();
                }
            catch (IOException e)
                {
                }
            }
        if (DEBUG) System.out.println("Input thread is terminating.");
        //System.exit(0);
        }

    void handlePrompts(char c) throws IOException
        {
        if (doPrompts && prompts != null)
            {
            for (int i = 0; i < prompts.length; i++)
                {
                PromptInfo pi = prompts[i];
                if (pi != null && pi.prompt != null)
                    {
                    if (pi.prompt[pi.pos] == c)
                        {
                        pi.pos++;
                        if (DEBUG)
                            System.out.println(i + ": prompt=" +
                                               pi.prompt + ", pos=" + pi.pos);
                        if (pi.pos >= pi.prompt.length)
                            {
                            if (pi.reply != null) write(pi.reply);
                            if (pi.terminate)
                                {
                                clearPrompts();
                                return;
                                }
                            pi.pos = 0;
                            }
                        }
                    else
                        {
                        pi.pos = 0;
                        }
                    }
                }
            }
        }

    synchronized void process_data(char b) throws IOException
        {
        if (DEBUG) System.out.println("Got: " + b + " (" + (int) b + ")");
        handlePrompts(b);
        if (emulator != null)
            {
            emulator.receive(b);
            }
        }

    synchronized void process_data(char[] b, int off, int len)
        throws IOException
        {
        if (DEBUG) System.out.println("Got: " + new String(b, off, len));
        if (doPrompts && prompts != null)
            {
            for (int pos = off, left = len; left > 0; pos++, left--)
                {
                handlePrompts(b[pos]);
                }
            }
        if (emulator != null)
            {
            emulator.receive(b, off, len);
            }
        }

    synchronized void process_cmd(int cmd) throws IOException
        {
        print_cmd("Server", cmd);
        cmd = (int) next_char();
        print_cmd("Server", cmd);

        switch (cmd)
            {
            case IAC:
                process_data((char) cmd);
                break;
            case DONT:
            case DO:
            case WONT:
            case WILL:
                {
                int opt = (int) next_char();
                print_opt("Server Opt", opt);
                process_opt(cmd, opt);
                break;
                }
            case SB:
                {
                int opt = (int) next_char();
                print_opt("Server Sub", opt);
                process_sb(opt);
                break;
                }
            default:
                return;
            }

        if (!isTelnet)
            {
            isTelnet = true;
            send_opt(WILL, TELOPT_NAWS, false);
            send_opt(DO, TELOPT_ECHO, false);
            send_opt(DO, TELOPT_SGA, false);
            }
        }

    synchronized void send_opt(int cmd, int opt, boolean force)
        throws IOException
        {

        /* Send this command if we are being forced to,
           OR if it a 'DO' request for the host,
           OR if it is a 'DONT' request for the host,
           OR if it is a change in the state of our options. */
        if (force || cmd == DO || cmd == DONT ||
            (telopt_ok(opt) &&
            (cmd == WONT && options[opt - TELOPT_FIRST])
            || (cmd == WILL && !options[opt - TELOPT_FIRST])))
            {
            byte[] reply = new byte[3];
            reply[0] = (byte) IAC;
            reply[1] = (byte) cmd;
            reply[2] = (byte) opt;
            print_cmd("Client", IAC);
            print_cmd("Client", cmd);
            print_opt("Client", opt);
            out.write(reply);
            }

        /* Change our options state.  We really shouldn't be turning
           options on until we get a 'do' reply, but this isn't
           a problem yet for the options that are currently implemented... */
        if (telopt_ok(opt))
            {
            if (cmd == WILL)
                {
                options[opt - TELOPT_FIRST] = true;
                }
            else if (cmd == WONT)
                {
                options[opt - TELOPT_FIRST] = false;
                }
            }
        }

    synchronized void process_opt(int cmd, int opt)
        throws IOException
        {

        /* If this is an option we don not understand or have not implemented,
           refuse any 'DO' request. */
        if (!telopt_ok(opt))
            {
            if (cmd == DO) send_opt(WONT, opt, true);

            /* If this is a DONT request, (possibly) send a reply and turn off
               the option. */
            }
        else if (cmd == DONT)
            {
            send_opt(WONT, opt, false);

            /* If this is a DO request, (possibly) send a reply and turn on
               the option. */
            }
        else if (cmd == DO)
            {
            send_opt(WILL, opt, false);
            if (opt == TELOPT_NAWS) send_naws(curWidth, curHeight);
            if (opt == TELOPT_TTYPE) curEmuName = -1;
            }
        }

    synchronized void clean_sb()
        throws IOException
        {
        for (; ;)
            {
            int d = (int) next_char();
            if (d == IAC)
                {
                d = (int) next_char();
                if (d == SE) return;
                }
            }
        }

    int put_byte(byte[] b, int pos, byte val)
        {
        b[pos++] = val;
        if (val == (byte) IAC) b[pos++] = val;
        return pos;
        }

    synchronized void send_naws(int width, int height)
        throws IOException
        {
        if (options[TELOPT_NAWS - TELOPT_FIRST])
            {
            byte[] reply = new byte[14];
            int i = 0;
            reply[i++] = (byte) IAC;
            reply[i++] = (byte) SB;
            reply[i++] = (byte) TELOPT_NAWS;
            i = put_byte(reply, i, (byte) ((width >> 8) & 0xFF));
            i = put_byte(reply, i, (byte) (width & 0xFF));
            i = put_byte(reply, i, (byte) ((height >> 8) & 0xFF));
            i = put_byte(reply, i, (byte) (height & 0xFF));
            reply[i++] = (byte) IAC;
            reply[i++] = (byte) SE;
            print_cmd("Client", IAC);
            print_cmd("Client", SB);
            print_opt("Client " + width + " x " + height, TELOPT_NAWS);
            print_cmd("Client", IAC);
            print_cmd("Client", SE);
            if (DEBUG) System.out.println("Client: len=" + i + ", dat=" + reply);
            out.write(reply, 0, i);
            }
        }

    synchronized void send_ttype()
        throws IOException
        {
        if (emulator != null)
            {
            String nstr = emulator.getName(curEmuName);
            while (nstr == null) nstr = emulator.getName(curEmuName - 1);
            if (nstr == null)
                {
                curEmuName = 0;
                nstr = emulator.getName(curEmuName - 1);
                }
            if (nstr == null) nstr = "UNKNOWN";
            char[] name = nstr.toCharArray();
            byte reply[] = new byte[name.length + 6];
            int i = 0;
            reply[i++] = (byte) IAC;
            reply[i++] = (byte) SB;
            reply[i++] = (byte) TELOPT_TTYPE;
            reply[i++] = 0;
            for (int j = 0; j < name.length; j++)
                {
                reply[i++] = (byte) name[j];
                }
            reply[i++] = (byte) IAC;
            reply[i++] = (byte) SE;
            print_cmd("Client", IAC);
            print_cmd("Client", SB);
            print_opt("Client " + name, TELOPT_TTYPE);
            print_cmd("Client", IAC);
            print_cmd("Client", SE);
            out.write(reply);
            }
        }

    synchronized void process_sb(int opt)
        throws IOException
        {
        switch (opt)
            {
            case TELOPT_TTYPE:
                int d = (int) next_char();
                clean_sb();
                if (d != 1)
                    {
                    break;
                    }
                curEmuName++;
                send_ttype();
                break;
            default:
                clean_sb();
                break;
            }
        }

    void print_cmd(String label, int cmd)
        {
        if (DEBUG)
            System.out.println(label + ": Cmd " + telcmd(cmd)
                               + " (" + cmd + ")");
        }

    void print_opt(String label, int opt)
        {
        if (DEBUG)
            {
            boolean flag = false;
            if (opt >= TELOPT_FIRST && opt <= TELOPT_LAST)
                {
                flag = telopts_impl[opt - TELOPT_FIRST];
                }
            System.out.println(label + ": Opt " + telopt(opt)
                               + " (" + opt + ") parsers=" + flag);
            }
        }

    boolean telcmd_ok(int cmd)
        {
        return (cmd <= TELCMD_LAST && cmd >= TELCMD_FIRST);
        }

    boolean telopt_ok(int opt)
        {
        return (opt <= TELOPT_LAST && opt >= TELOPT_FIRST
            && telopts_impl[opt - TELOPT_FIRST]);
        }

    String telcmd(int cmd)
        {
        if (telcmd_ok(cmd))
            {
            return telcmds[cmd - TELCMD_FIRST];
            }
        return "<?CMD?>";
        }

    String telopt(int opt)
        {
        if (opt >= TELOPT_FIRST && opt <= TELOPT_LAST)
            {
            return telopts[opt - TELOPT_FIRST];
            }
        return "<?OPT?>";
        }
    }

