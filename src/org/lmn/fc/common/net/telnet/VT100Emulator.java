package org.lmn.fc.common.net.telnet;

/*
 * WebTerm version 1.3
 * ~~~~~~~
 *
 * VT100Emulator class: a subclass of Emulator that implements a
 * standard VT100 emulation.
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
 * - Some escape sequence for scrolling rows is not working correctly...
 *   this can be seen in emacs, where pressing RETURN in the middle
 *   of a document does not scroll the text below the cursor.
 *
 * ----------------------------------------------------------------------
 *
 * History
 * ~~~~~~~
 *
 * 1.3: Created this file.
 *
 * 1.3.1: Added "XTERM" and "VT100" synonyms for emulation.
 *
 */

/* VT Emulation info:

   One cost-effective document is "VT220 Programmer Pocket Guide", part
   number EK-VT220-HR-001, $21.00 (US). From the 48-state United States,
   call 1-800/DIGITAL to order this. From elsewhere, dial +1 603/884-6660.
*/

import java.awt.*;
import java.io.IOException;

public class VT100Emulator extends Emulator
    {

    static final int STATE_ASCII = 0;
    static final int STATE_ESCAPE = 1;
    static final int STATE_CSI = 2;      // Escape-[
    static final int STATE_G0 = 3;       // Escape-(
    static final int STATE_G1 = 4;       // Escape-)
    static final int STATE_MODE = 5;     // Escape-[?

    static final char NUL = (char) 0;
    static final char ENQ = (char) 05;
    static final char BEL = (char) 07;
    static final char BS = (char) 010;
    static final char HT = (char) 011;
    static final char LF = (char) 012;
    static final char VT = (char) 013;
    static final char FF = (char) 014;
    static final char CR = (char) 015;
    static final char SO = (char) 016;
    static final char SI = (char) 017;
    static final char DC1 = (char) 021;
    static final char DC3 = (char) 023;
    static final char CAN = (char) 030;
    static final char SUB = (char) 032;
    static final char ESC = (char) 033;
    static final char DEL = (char) 0177;
    static final char CSI = (char) 0x9B;

    static final int MAX_ARGS = 10;

    int state = STATE_ASCII;
    int cur_arg = 0;
    int[] args = new int[MAX_ARGS];
    boolean[] has_args = new boolean[MAX_ARGS];

    int saved_row = 0;
    int saved_col = 0;

    public VT100Emulator(Terminal terminal, Telnet telnet)
        {
        super(terminal, telnet);
        }

    public String getName(int num)
        {
        switch (num)
            {
            case 0:
                return "XTERM";
            case 1:
                return "DEC-VT100";
            case 2:
                return "VT100";
            }
        return null;
        }

    public boolean checkChar(char c)
        {
        if (state == STATE_ASCII && c != ESC && c != CSI
            && c != VT && c != FF && c != SO && c != SI
            && c != DC1 && c != DC3 && c != CAN && c != SUB
            && c != DEL)
            return true;
        return false;
        }

    void reset_state()
        {
        if (state == STATE_CSI || state == STATE_G0
            || state == STATE_G1 || state == STATE_MODE)
            {
            for (int i = 0; i < MAX_ARGS; i++)
                {
                args[i] = 0;
                has_args[i] = false;
                }
            cur_arg = 0;
            }
        state = STATE_ASCII;
        }

    public boolean doChar(char c)
        {
        if (c == CAN || c == SUB)
            {
            reset_state();
            return false;
            }
        else if (c == ESC)
            {
            reset_state();
            }
        else if (c == SO || c == SI || c == DC1 || c == DC3 || c == DEL)
            {
            return false;
            }

        switch (state)
            {
            case STATE_ASCII:
                if (c == VT || c == FF)
                    {
                    if (terminal != null)
                        {
                        terminal.write('\n', terminal.OUTF_PARTIAL);
                        }
                    return false;
                    }
                else if (c == ESC)
                    {
                    if (DEBUG) System.out.println("Entered ESCAPE");
                    state = STATE_ESCAPE;
                    return false;
                    }
                else if (c == CSI)
                    {
                    if (DEBUG) System.out.println("Entered CSI");
                    state = STATE_CSI;
                    return false;
                    }
                return true;
            case STATE_ESCAPE:
                switch (c)
                    {
                    case '[':        // Start CSI
                        if (DEBUG) System.out.println("Entered CSI");
                        state = STATE_CSI;
                        return false;
                    case '>':        // Reset terminal
                        if (DEBUG) System.out.println("RESET");
                        terminal.setStyle(terminal.STYLE_PLAIN);
                        state = STATE_ASCII;
                        return false;
                    case '8':        // Restore cursor position
                        if (DEBUG) System.out.println("Restore cursor position");
                        if (terminal != null) terminal.setCursorPos(saved_row, saved_col);
                        state = STATE_ASCII;
                        return false;
                    case '7':        // Save cursor position
                        if (DEBUG) System.out.println("Save cursor position");
                        if (terminal != null)
                            {
                            saved_row = terminal.getCursorRow();
                            saved_col = terminal.getCursorCol();
                            }
                        state = STATE_ASCII;
                        return false;
                    case 'D':        // Index
                        if (DEBUG) System.out.println("Index");
                        if (terminal != null)
                            {
                            terminal.write('\n', terminal.OUTF_PARTIAL);
                            }
                        state = STATE_ASCII;
                        return false;
                    case 'E':
                        if (DEBUG) System.out.println("Next line");
                        if (terminal != null)
                            {
                            int row = terminal.getCursorRow();
                            if (row < terminal.getNumRows())
                                row++;
                            else
                                terminal.screenScroll(-1);
                            terminal.setCursorPos(row, terminal.getCursorCol());
                            }
                        state = STATE_ASCII;
                        return false;
                    case 'M':
                        if (DEBUG) System.out.println("Reverse index");
                        if (terminal != null)
                            {
                            int row = terminal.getCursorRow();
                            if (row > 0)
                                row--;
                            else
                                terminal.screenScroll(1);
                            terminal.setCursorPos(row, 0);
                            }
                        state = STATE_ASCII;
                        return false;
                    case 'H':
                        if (true)
                            {
                            if (DEBUG) System.out.println("Set tab");
                            }
                        else
                            {
                            if (DEBUG) System.out.println("Cursor home");
                            if (terminal != null)
                                {
                                terminal.setCursorPos(0, 0);
                                }
                            }
                        state = STATE_ASCII;
                        return false;
                    case 'I':
                        if (DEBUG) System.out.println("Reverse line feed");
                        state = STATE_ASCII;
                        return false;
                    case 'c':
                        if (DEBUG) System.out.println("Reset to initial state");
                        if (terminal != null)
                            {
                            terminal.screenScroll(-terminal.getNumRows());
                            terminal.setStyle(terminal.STYLE_PLAIN);
                            }
                        state = STATE_ASCII;
                        return false;
                    case '(':
                        if (DEBUG) System.out.println("Entered GO");
                        state = STATE_G0;
                        return false;
                    case ')':
                        if (DEBUG) System.out.println("Entered G1");
                        state = STATE_G1;
                        return false;
                    }
                if (DEBUG) System.out.println("Got ESCAPE-" + c + " (" + (int) c + ")");
                state = STATE_ASCII;
                if (terminal != null)
                    {
                    terminal.write((char) 0x1B, terminal.OUTF_PARTIAL);
                    terminal.write(c, terminal.OUTF_PARTIAL);
                    }
                return false;
            case STATE_CSI:
                if (c == '?')
                    {
                    if (DEBUG) System.out.println("Entered MODE");
                    state = STATE_MODE;
                    return false;
                    }
            case STATE_G0:
            case STATE_G1:
            case STATE_MODE:
                if (false && DEBUG) System.out.println("Got char " + c);
                if (c >= '0' && c <= '9')
                    {
                    if (cur_arg < MAX_ARGS)
                        {
                        args[cur_arg] *= 10;
                        args[cur_arg] += (int) (c - '0');
                        has_args[cur_arg] = true;
                        if (false && DEBUG)
                            System.out.println("Arg #" + cur_arg +
                                               " now " + args[cur_arg]);
                        }
                    return false;
                    }
                else if (c == ';')
                    {
                    cur_arg++;
                    if (cur_arg > MAX_ARGS) cur_arg = MAX_ARGS;
                    if (false && DEBUG) System.out.println("Switch to arg #" + cur_arg);
                    return false;
                    }
                if (false && DEBUG) System.out.println("Last argument: " + cur_arg);
                if (cur_arg >= MAX_ARGS) cur_arg = MAX_ARGS - 1;
                if (cur_arg < MAX_ARGS)
                    {
                    if (has_args[cur_arg]) cur_arg++;
                    }
                switch (state)
                    {
                    case STATE_CSI:
                        doCommand(c, args, has_args, cur_arg);
                        break;
                    case STATE_G0:
                        doG0(c, args, has_args, cur_arg);
                        break;
                    case STATE_G1:
                        doG1(c, args, has_args, cur_arg);
                        break;
                    case STATE_MODE:
                        doMode(c, args, has_args, cur_arg);
                        break;
                    default:
                        System.out.println("Unknown state " + state);
                    }
                state = STATE_ASCII;
                for (int i = 0; i < MAX_ARGS; i++)
                    {
                    args[i] = 0;
                    has_args[i] = false;
                    }
                cur_arg = 0;
                return false;
            default:
                state = STATE_ASCII;
                System.out.println("*** Unknown VT100 emulator state!");
                return false;
            }
        }

    void print_args(int[] args, boolean[] given, int cnt)
        {
        int i;
        String str;
        str = "Arguments:";
        for (i = 0; i < cnt; i++)
            {
            if (given[i])
                str = str + " " + args[i];
            else
                str = str + " <DEF>";
            }
        System.out.println(str);
        }

    public void doCommand(char cmd, int[] args, boolean[] given, int cnt)
        {
        switch (cmd)
            {
            case '@':                     // Insert blanks
                if (DEBUG) System.out.println("Insert blanks.");
                if (DEBUG) print_args(args, given, cnt);
                if (terminal != null)
                    {
                    int num = args[0];
                    if (!given[0]) num = 1;
                    char[] buf = new char[num];
                    for (int i = 0; i < num; i++) buf[i] = ' ';
                    terminal.write(buf, 0, num, terminal.OUTF_PARTIAL);
                    }
                break;
            case 'A':                     // Cursor up
                if (DEBUG) System.out.println("Move cursor up.");
                if (DEBUG) print_args(args, given, cnt);
                if (terminal != null)
                    {
                    int num = args[0];
                    int row;
                    if (!given[0]) num = 1;
                    row = terminal.getCursorRow() - num;
                    terminal.setCursorPos(row, terminal.getCursorCol());
                    }
                break;
            case 'B':                     // Cursor down
                if (DEBUG) System.out.println("Move cursor down.");
                if (DEBUG) print_args(args, given, cnt);
                if (terminal != null)
                    {
                    int num = args[0];
                    int row;
                    if (!given[0]) num = 1;
                    row = terminal.getCursorRow() + num;
                    terminal.setCursorPos(row, terminal.getCursorCol());
                    }
                break;
            case 'C':                     // Cursor right
                if (DEBUG) System.out.println("Move cursor right.");
                if (DEBUG) print_args(args, given, cnt);
                if (terminal != null)
                    {
                    int num = args[0];
                    int col;
                    if (!given[0]) num = 1;
                    col = terminal.getCursorCol() + num;
                    terminal.setCursorPos(terminal.getCursorRow(), col);
                    }
                break;
            case 'D':                     // Cursor left
                if (DEBUG) System.out.println("Move cursor left.");
                if (DEBUG) print_args(args, given, cnt);
                if (terminal != null)
                    {
                    int num = args[0];
                    int col;
                    if (!given[0]) num = 1;
                    col = terminal.getCursorCol() - num;
                    terminal.setCursorPos(terminal.getCursorRow(), col);
                    }
                break;
            case 'E':                     // Cursor to next line
                if (DEBUG) System.out.println("Move to next line.");
                if (DEBUG) print_args(args, given, cnt);
                if (terminal != null)
                    {
                    int num = args[0];
                    int row;
                    if (!given[0]) num = 1;
                    row = terminal.getCursorRow() + num;
                    terminal.setCursorPos(row, 0);
                    }
                break;
            case 'F':                     // Cursor to previous line
                if (DEBUG) System.out.println("Move to previous line.");
                if (DEBUG) print_args(args, given, cnt);
                if (terminal != null)
                    {
                    int num = args[0];
                    int row;
                    if (!given[0]) num = 1;
                    row = terminal.getCursorRow() - num;
                    terminal.setCursorPos(row, 0);
                    }
                break;
            case 'H':                     // Set cursor pos
            case 'f':
                if (DEBUG) System.out.println("Set cursor pos.");
                if (DEBUG) print_args(args, given, cnt);
                if (terminal != null)
                    {
                    int row = terminal.getCursorRow();
                    int col = terminal.getCursorCol();
                    if (cnt == 0)
                        row = col = 0;
                    else
                        {
                        if (given[0] && cnt >= 1) row = args[0] - 1;
                        if (given[1] && cnt >= 2) col = args[1] - 1;
                        }
                    terminal.setCursorPos(row, col);
                    }
                break;
            case 'r':                     // Set scroll region
                if (DEBUG) System.out.println("Set scroll region.");
                if (DEBUG) print_args(args, given, cnt);
                if (terminal != null)
                    {
                    int top = 0;
                    int bottom = terminal.getNumRows() - 1;
                    if (given[0] && cnt >= 1) top = args[0] - 1;
                    if (given[1] && cnt >= 2) bottom = args[1] - 1;
                    terminal.setRegion(top, bottom);
                    }
                break;
            case 'n':                     // Cursor position report
                if (terminal != null && telnet != null)
                    {
                    String report = null;
                    if (cnt >= 1 && args[0] == 6)
                        {
                        if (DEBUG) System.out.println("Cursor position report.");
                        if (DEBUG) print_args(args, given, cnt);
                        report = new String("\033[" +
                                            (terminal.getCursorRow() + 1) + ';' +
                                            (terminal.getCursorCol() + 1) +
                                            "R");
                        }
                    else if (cnt >= 1 && args[0] == 5)
                        {
                        if (DEBUG) System.out.println("Terminal status report.");
                        if (DEBUG) print_args(args, given, cnt);
                        report = new String("\033[0n");
                        }
                    else
                        {
                        if (DEBUG) System.out.println("Unknown terminal request!.");
                        if (DEBUG) print_args(args, given, cnt);
                        }
                    if (report != null)
                        {
                        byte[] b = new byte[report.length()];
                        for (int i = 0; i < report.length(); i++)
                            {
                            b[i] = (byte) report.charAt(i);
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
                break;
            case 'c':                     // Terminal type report
                if (DEBUG) System.out.println("Terminal type report.");
                if (DEBUG) print_args(args, given, cnt);
                if (telnet != null)
                    {
                    String report = new String("\033[?1;0C");
                    byte[] b = new byte[report.length()];
                    for (int i = 0; i < report.length(); i++)
                        {
                        b[i] = (byte) report.charAt(i);
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
                break;
            case 'I':                     // Move to tabstop N
                if (DEBUG) System.out.println("Move to tabstop.");
                if (DEBUG) print_args(args, given, cnt);
                break;
            case 'J':                     // Clear to end of display
                if (DEBUG) System.out.println("Clear in display.");
                if (DEBUG) print_args(args, given, cnt);
                if (terminal != null)
                    {
                    if (!given[0] || args[0] == 0)
                        terminal.screenClearEOD();
                    else if (args[0] == 1)
                        terminal.screenClearBOD();
                    else if (args[0] == 2) terminal.screenScroll(-terminal.getNumRows());
                    }
                break;
            case 'K':                     // Clear to end of line
                if (DEBUG) System.out.println("Clear in line.");
                if (DEBUG) print_args(args, given, cnt);
                if (terminal != null)
                    {
                    if (!given[0] || args[0] == 0)
                        terminal.screenClearEOL();
                    else if (args[0] == 1)
                        terminal.screenClearBOL();
                    else if (args[0] == 2) terminal.screenClearLine();
                    }
                break;
            case 'L':                     // Insert lines above cursor
                if (DEBUG) System.out.println("Insert line.");
                if (DEBUG) print_args(args, given, cnt);
                if (terminal != null)
                    {
                    int num = args[0];
                    if (!given[0]) num = 1;
                    int row = terminal.getCursorRow();
                    if (row < 0) row = 0;
                    terminal.screenScrollRegion(row, terminal.getRegionBottom(), num);
                    }
                break;
            case 'M':                     // Delete lines at cursor
                if (DEBUG) System.out.println("Delete line.");
                if (DEBUG) print_args(args, given, cnt);
                if (terminal != null)
                    {
                    int num = args[0];
                    if (!given[0]) num = 1;
                    terminal.screenScrollRegion(terminal.getCursorRow(),
                                                terminal.getRegionBottom(), -num);
                    }
                break;
            case 'm':
                if (DEBUG) System.out.println("Set mode.");
                if (DEBUG) print_args(args, given, cnt);
                if (cnt <= 0)
                    {
                    terminal.setStyle(terminal.STYLE_PLAIN);
                    }
                else
                    {
                    for (int i = 0; i < cnt; i++)
                        {
                        switch (args[i])
                            {
                            case 0:
                                if (DEBUG) System.out.println("Plain style.");
                                terminal.setStyle(terminal.STYLE_PLAIN);
                                break;
                            case 1:
                                if (DEBUG) System.out.println("Bold style.");
                                terminal.setStyle(terminal.getStyle() | terminal.STYLE_BOLD);
                                break;
                            case 4:
                                if (DEBUG) System.out.println("Underscore style.");
                                terminal.setStyle(terminal.getStyle() | terminal.STYLE_UNDERSCORE);
                                break;
                            case 5:
                                if (DEBUG) System.out.println("Italic style.");
                                terminal.setStyle(terminal.getStyle() | terminal.STYLE_ITALIC);
                                break;
                            case 7:
                                if (DEBUG) System.out.println("Inverse style.");
                                terminal.setStyle(terminal.getStyle() | terminal.STYLE_INVERSE);
                                break;
                                /* VT220 control code */
                            case 22:
                                if (DEBUG) System.out.println("Bold style off.");
                                terminal.setStyle(terminal.getStyle() & ~terminal.STYLE_BOLD);
                                break;
                            case 24:
                                if (DEBUG) System.out.println("Underscore style off.");
                                terminal.setStyle(terminal.getStyle()
                                                  & ~terminal.STYLE_UNDERSCORE);
                                break;
                            case 25:
                                if (DEBUG) System.out.println("Italic style off.");
                                terminal.setStyle(terminal.getStyle() & ~terminal.STYLE_ITALIC);
                                break;
                            case 27:
                                if (DEBUG) System.out.println("Inverse style off.");
                                terminal.setStyle(terminal.getStyle() & ~terminal.STYLE_INVERSE);
                                break;
                            default:
                                if (DEBUG)
                                    System.out.println("Unknown style: " + args[i]);
                                break;
                            }
                        }
                    }
                state = STATE_ASCII;
                break;
            default:
                {
                System.out.println("Unknown command: " + cmd + " (" + (int) cmd + ")");
                print_args(args, given, cnt);
                }
            }
        }

    public void doG0(char cmd, int[] args, boolean[] given, int cnt)
        {
        System.out.println("Unknown G0: " + cmd + " (" + (int) cmd + ")");
        print_args(args, given, cnt);
        }

    public void doG1(char cmd, int[] args, boolean[] given, int cnt)
        {
        System.out.println("Unknown G1: " + cmd + " (" + (int) cmd + ")");
        print_args(args, given, cnt);
        }

    public void doMode(char cmd, int[] args, boolean[] given, int cnt)
        {
        System.out.println("Unknown Mode: " + cmd + " (" + (int) cmd + ")");
        print_args(args, given, cnt);
        }

    public void handleEvent(Event e)
        {
        byte[] b = null;
        if (e.id == e.KEY_ACTION)
            {
            if (e.key == e.LEFT)
                {
                b = new byte[3];
                b[0] = 0x1B;
                b[1] = (byte) '[';
                b[2] = (byte) 'D';
                }
            else if (e.key == e.RIGHT)
                {
                b = new byte[3];
                b[0] = 0x1B;
                b[1] = (byte) '[';
                b[2] = (byte) 'C';
                }
            else if (e.key == e.UP)
                {
                b = new byte[3];
                b[0] = 0x1B;
                b[1] = (byte) '[';
                b[2] = (byte) 'A';
                }
            else if (e.key == e.DOWN)
                {
                b = new byte[3];
                b[0] = 0x1B;
                b[1] = (byte) '[';
                b[2] = (byte) 'B';
                }
            }
        if (b != null && telnet != null)
            {
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

    }
