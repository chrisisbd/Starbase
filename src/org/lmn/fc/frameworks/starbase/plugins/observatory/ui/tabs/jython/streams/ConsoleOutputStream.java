package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.jython.streams;

import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.JythonConsoleInterface;

import java.io.IOException;
import java.io.Writer;


/**
 * Data written to this will be displayed into the console
 *
 * @author Andrew
 */
public class ConsoleOutputStream extends Writer
    {
    private JythonConsoleInterface console;


    /**
     * @param jyconsole
     */
    public ConsoleOutputStream(final JythonConsoleInterface jyconsole)
        {
        this.console = jyconsole;
        }


    @Override
    public synchronized void close() throws IOException
        {
        console = null;
        }


    @Override
    public void flush() throws IOException
        {
        // no extra flushing needed
        }


    @Override
    public synchronized void write(final char[] cbuf,
                                   final int off,
                                   final int len) throws IOException
        {
        final StringBuilder temp = new StringBuilder(console.getText());

        for (int i = off;
             i < off + len;
             i++)
            {
            temp.append(cbuf[i]);
            }

        console.setText(temp.toString());
        }
    }
