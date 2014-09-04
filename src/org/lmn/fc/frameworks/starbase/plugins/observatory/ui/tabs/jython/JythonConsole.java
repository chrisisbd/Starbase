package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.jython;

import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.JythonConsoleInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.jython.streams.ConsoleInputStream;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.jython.streams.ConsoleOutputStream;
import org.python.core.PyException;
import org.python.util.InteractiveInterpreter;

import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.print.PageFormat;
import java.awt.print.PrinterException;
import java.awt.print.PrinterGraphics;
import java.io.Reader;
import java.io.Writer;


/***************************************************************************************************
 * JythonConsole.
 *
 * See: http://www.javaprogrammingforums.com/java-swing-tutorials/4907-java-tip-jul-29-2010-swing-console-component.html
 * Many thanks to user helloworld922.
 */

public final class JythonConsole extends JTextArea
                                 implements JythonConsoleInterface
    {
    private static final long serialVersionUID = -5866169869353990380L;

    private final Reader in;
    private final Writer out;
    private final Writer err;
    private final CommandHistory history;
    private int intEditStart;
    private boolean boolRunning;
    private final InteractiveInterpreter engine;
    private final ConsoleFilter filter;
    private Thread pythonThread;


    /************************************************************************************************
     * Construct a JythonConsole.
     */

    public JythonConsole()
        {
        // create streams that will link with this
        in = new ConsoleInputStream(this);
        // System.setIn(in);
        out = new ConsoleOutputStream(this);
        // System.setOut(new PrintStream(out));
        err = new ConsoleOutputStream(this);
        // setup the command history
        history = new CommandHistory();

        // setup the script engine
        engine = new InteractiveInterpreter();
        engine.setIn(in);
        engine.setOut(out);
        engine.setErr(err);

        setTabSize(4);
        // setup the event handlers and input processing
        // setup the document filter so output and old text can't be modified
        addKeyListener(this);
        filter = new ConsoleFilter(this);
        ((AbstractDocument) getDocument()).setDocumentFilter(filter);

        // start text and edit location
        setText(MSG_SIGN_ON);
        getCaret().setDot(intEditStart);
        }


    /***********************************************************************************************
     * Set the displayed text.
     *
     * @param text
     */

    public void setText(final String text)
        {
        setText(text, true);
        }


    /***********************************************************************************************
     * @param text
     * @param updateEditStart
     */

    private void setText(final String text,
                         final boolean updateEditStart)
        {
        filter.useFilters = false;
        super.setText(text);
        filter.useFilters = true;

        if (updateEditStart)
            {
            intEditStart = text.length();
            }

        getCaret().setDot(text.length());
        }


    /***********************************************************************************************
     * Invoked when a key has been pressed.
     *
     * @param event
     */

    public void keyPressed(final KeyEvent event)
        {
        if (event.isControlDown())
            {
            if (event.getKeyCode() == KeyEvent.VK_A && !event.isShiftDown() && !event.isAltDown())
                {
                // handle select all
                // if selection start is in the editable region, try to select
                // only editable text
                if (getSelectionStart() >= intEditStart)
                    {
                    // however, if we already have the editable region selected,
                    // default select all
                    if (getSelectionStart() != intEditStart || getSelectionEnd() != this.getText().length())
                        {
                        setSelectionStart(intEditStart);
                        setSelectionEnd(this.getText().length());
                        // already handled, don't use default handler
                        event.consume();
                        }
                    }
                }
            else if (event.getKeyCode() == KeyEvent.VK_DOWN && !event.isShiftDown() && !event.isAltDown())
                {
                // next in history
                final StringBuilder temp = new StringBuilder(getText());
                // remove the current command
                temp.delete(intEditStart, temp.length());
                temp.append(history.getNextCommand());
                setText(temp.toString(), false);
                event.consume();
                }
            else if (event.getKeyCode() == KeyEvent.VK_UP && !event.isShiftDown() && !event.isAltDown())
                {
                // prev in history
                final StringBuilder temp = new StringBuilder(getText());
                // remove the current command
                temp.delete(intEditStart, temp.length());
                temp.append(history.getPrevCommand());
                setText(temp.toString(), false);
                event.consume();
                }
            }
        else if (event.getKeyCode() == KeyEvent.VK_ENTER)
            {
            // handle script execution
            if (!event.isShiftDown() && !event.isAltDown())
                {
                if (isRunning())
                    {
                    // we need to put text into the input stream
                    final StringBuilder text = new StringBuilder(this.getText());
                    text.append(System.getProperty("line.separator"));
                    final String command = text.substring(intEditStart);
                    setText(text.toString());
                    ((ConsoleInputStream) in).addText(command);
                    }
                else
                    {
                    // run the engine
                    final StringBuilder text = new StringBuilder(this.getText());
                    final String command = text.substring(intEditStart);
                    text.append(System.getProperty("line.separator"));
                    setText(text.toString());
                    // add to the history
                    history.add(command);
                    // run on a separate thread
                    pythonThread = new Thread(new PythonRunner(command));
                    // so this thread can't hang JVM shutdown
                    pythonThread.setDaemon(true);
                    pythonThread.start();
                    }
                event.consume();
                }
            else if (!event.isAltDown())
                {
                // shift+enter
                final StringBuilder text = new StringBuilder(this.getText());
                if (getSelectedText() != null)
                    {
                    // replace text
                    text.delete(getSelectionStart(), getSelectionEnd());
                    }
                text.insert(getSelectionStart(), System.getProperty("line.separator"));
                setText(text.toString(), false);
                }
            }
        else if (event.getKeyCode() == KeyEvent.VK_HOME)
            {
            final int selectStart = getSelectionStart();
            if (selectStart > intEditStart)
                {
                // we're after edit start, see if we're on the same line as edit
                // start
                for (int i = intEditStart;
                     i < selectStart;
                     i++)
                    {
                    if (this.getText().charAt(i) == '\n')
                        {
                        // not on the same line
                        // use default handle
                        return;
                        }
                    }
                if (event.isShiftDown())
                    {
                    // move to edit start
                    getCaret().moveDot(intEditStart);
                    }
                else
                    {
                    // move select end, too
                    getCaret().setDot(intEditStart);
                    }
                event.consume();
                }
            }
        }


    /**********************************************************************************************
     * Invoked when a key has been released.
     *
     * @param event
     */

    public void keyReleased(final KeyEvent event)
        {
        // don't need to use this for anything
        }


    /***********************************************************************************************
     * Invoked when a key has been typed.
     *
     * @param event
     */

    public void keyTyped(final KeyEvent event)
        {
        // don't need to use this for anything
        }


    /***********************************************************************************************
     * Prints the page at the specified index into the specified
     * {@link Graphics} context in the specified
     * format.  A <code>PrinterJob</code> calls the
     * <code>Printable</code> interface to request that a page be
     * rendered into the context specified by
     * <code>graphics</code>.  The format of the page to be drawn is
     * specified by <code>pageFormat</code>.  The zero based index
     * of the requested page is specified by <code>pageIndex</code>.
     * If the requested page does not exist then this method returns
     * NO_SUCH_PAGE; otherwise PAGE_EXISTS is returned.
     * The <code>Graphics</code> class or subclass implements the
     * {@link PrinterGraphics} interface to provide additional
     * information.  If the <code>Printable</code> object
     * aborts the print job then it throws a {@link PrinterException}.
     *
     * @param graphics the context into which the page is drawn
     * @param pageformat the size and orientation of the page being drawn
     * @param pageindex the zero based index of the page to be drawn
     *
     * @return PAGE_EXISTS if the page is rendered successfully
     *         or NO_SUCH_PAGE if <code>pageIndex</code> specifies a
     *	       non-existent page.
     *
     * @exception PrinterException
     *         thrown when the print job is terminated.
     */

    public int print(final Graphics graphics,
                     final PageFormat pageformat,
                     final int pageindex) throws PrinterException
        {
        if (pageindex > 0)
            {
            return (NO_SUCH_PAGE);
            }
        else
            {
            final Graphics2D graphics2D;
            final RepaintManager repaintManager;

            graphics2D = (Graphics2D) graphics;
            graphics2D.translate(pageformat.getImageableX(), pageformat.getImageableY());
            repaintManager = RepaintManager.currentManager(this);

            repaintManager.setDoubleBufferingEnabled(false);
            paint(graphics2D);
            repaintManager.setDoubleBufferingEnabled(true);

            return (PAGE_EXISTS);
            }
        }


    /***********************************************************************************************
     * Get the Jython InteractiveInterpreter.
     *
     * @return InteractiveInterpreter
     */

    public InteractiveInterpreter getInterpreter()
        {
        return (this.engine);
        }


    /***********************************************************************************************
     * Get the Edit Start index.
     *
     * @return int
     */

    public int getEditStart()
        {
        return (this.intEditStart);
        }


    /***********************************************************************************************
     * Indicate if the InteractiveInterpreter is currently running.
     *
     * @return boolean
     */

    public boolean isRunning()
        {
        return (this.boolRunning);
        }


    /***********************************************************************************************
     * Tidy up.
     */

    public void finalize() throws Throwable
        {
        if (boolRunning)
            {
            // I know it's deprecated, but since this object is being destroyed,
            // this thread should go, too
            pythonThread.stop();
            pythonThread.destroy();
            }

        super.finalize();
        }


    //---------------------------------------------------------------------------------------------
    // Inner Classes

    private class ConsoleFilter extends DocumentFilter
        {
        private final JythonConsoleInterface console;
        public boolean useFilters;


        ConsoleFilter(final JythonConsoleInterface con)
            {
            this.console = con;
            useFilters = true;
            }


        @Override
        public void insertString(final DocumentFilter.FilterBypass fb,
                                 final int offset,
                                 final String string,
                                 final AttributeSet attr)
                throws BadLocationException
            {
            if (useFilters)
                {
                // determine if we can insert
                if (console.getSelectionStart() >= console.getEditStart())
                    {
                    // can insert
                    fb.insertString(offset, string, attr);
                    }
                else
                    {
                    // insert at the end of the document
                    fb.insertString(console.getText().length(), string, attr);
                    // move cursor to the end
                    console.getCaret().setDot(console.getText().length());
                    // console.setSelectionEnd(console.getText().length());
                    // console.setSelectionStart(console.getText().length());
                    }
                }
            else
                {
                fb.insertString(offset, string, attr);
                }
            }


        @Override
        public void replace(final DocumentFilter.FilterBypass fb,
                            final int offset,
                            final int length,
                            final String text,
                            final AttributeSet attrs)
                throws BadLocationException
            {
            if (useFilters)
                {
                // determine if we can replace
                if (console.getSelectionStart() >= console.getEditStart())
                    {
                    // can replace
                    fb.replace(offset, length, text, attrs);
                    }
                else
                    {
                    // insert at end
                    fb.insertString(console.getText().length(), text, attrs);
                    // move cursor to the end
                    console.getCaret().setDot(console.getText().length());
                    // console.setSelectionEnd(console.getText().length());
                    // console.setSelectionStart(console.getText().length());
                    }
                }
            else
                {
                fb.replace(offset, length, text, attrs);
                }
            }


        @Override
        public void remove(final DocumentFilter.FilterBypass fb,
                           final int offset,
                           final int length) throws BadLocationException
            {
            if (useFilters)
                {
                if (offset > console.getEditStart())
                    {
                    // can remove
                    fb.remove(offset, length);
                    }
                else
                    {
                    // only remove the portion that's editable
                    fb.remove(console.getEditStart(), length - (console.getEditStart() - offset));
                    // move selection to the start of the editable section
                    console.getCaret().setDot(console.getEditStart());
                    // console.setSelectionStart(console.intEditStart);
                    // console.setSelectionEnd(console.intEditStart);
                    }
                }
            else
                {
                fb.remove(offset, length);
                }
            }
        }


    private class PythonRunner implements Runnable
        {
        private final String commands;


        PythonRunner(final String cmds)
            {
            this.commands = cmds;
            }


        public void run()
            {
            boolRunning = true;

            try
                {
                engine.runsource(commands);
                }

            catch (PyException e)
                {
                // prints out the python error message to the console
                e.printStackTrace();
                }

            // engine.eval(commands, context);
            final StringBuilder text;

            text = new StringBuilder(getText());
            text.append(">>> ");
            setText(text.toString());
            boolRunning = false;
            }
        }
    }
