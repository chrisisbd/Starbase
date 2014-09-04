package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.jython.streams;

import org.lmn.fc.common.utilities.misc.Utilities;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.JythonConsoleInterface;

import java.io.IOException;
import java.io.Reader;


/**
 * Data written into this is data from the console
 *
 * @author Andrew
 */
public class ConsoleInputStream extends Reader
    {
    private JythonConsoleInterface console;
    private StringBuilder stream;


    /**
     * @param jyconsole
     */
    public ConsoleInputStream(final JythonConsoleInterface jyconsole)
        {
        this.console = jyconsole;
        stream = new StringBuilder();
        }


    /**
     * @param text
     */
    public void addText(final String text)
        {
        synchronized (stream)
            {
            stream.append(text);
            }
        }


    @Override
    public synchronized void close() throws IOException
        {
        console = null;
        stream = null;
        }


    @Override
    public int read(final char[] buf,
                    final int off,
                    final int len) throws IOException
        {
        int count = 0;
        boolean doneReading = false;

        for (int i = off;
             i < off + len && !doneReading;
             i++)
            {
            // determine if we have a character we can read
            // we need the lock for stream
            int length = 0;
            while (length == 0)
                {
                // sleep this thread until there is something to read
                //				try
                //				{
                Utilities.safeSleep(100);
                //Thread.sleep(100);
                //				}
                //				catch (InterruptedException e)
                //				{
                //					// TODO Auto-generated catch block
                //					e.printStackTrace();
                //				}

                synchronized (stream)
                    {
                    length = stream.length();
                    }
                }

            synchronized (stream)
                {
                // get the character
                buf[i] = stream.charAt(0);
                // delete it from the buffer
                stream.deleteCharAt(0);
                count++;

                if (buf[i] == '\n')
                    {
                    doneReading = true;
                    }
                }
            }
        return count;
        }
    }
