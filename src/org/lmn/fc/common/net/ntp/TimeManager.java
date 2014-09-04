package org.lmn.fc.common.net.ntp;

import java.util.Date;


/**
 * This is an abstract class encapsulating the method for setting the system time on the local
 * system. Setting the local time can  be accomplished by some external process of shell-script, or
 * more likely by using the 'Java Native Interface'.
 *
 */

public abstract class TimeManager
    {
    public static boolean boolLoaded;


    /***********************************************************************************************
     *
     * @return boolean
     */

    public boolean isLoaded()
        {
        return (boolLoaded);
        }


    /**
     * Set the local time to a given date.
     */
    public abstract void setTime(Date d);

    /**
     * Add a given offset to the local time.
     */
    // WARNING - which clock do you want to use?!
//    public final void setTime(final long offset)
//        {
//        setTime(new Date(Chronos.getSystemTimeMillis() + offset));
//        }

    /**
     * This method could be used to terminate an external helper process. The default implementation
     * does nothing.
     *
     */
    private static void dispose()
        {
        }

    /**
     * The default implementation of this method just invokes disposeUI().
     */
    public final void finalize()
        {
        try
            {
            dispose();
            }
        catch (Exception e)
            {
            System.out.println(e.getMessage());
            e.printStackTrace();
            }
        }

    /**
     * The factory method for obtaining an instance of 'TimeManager'. The default implementation
     * returns an instance of 'LocalTimeManager'. More sophisticated implementations should check
     * the name of the current operating system and act accordingly.
     *
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws InstantiationException
     *
     * @see LocalTimeManager
     */
    public static TimeManager getInstance() throws ClassNotFoundException,
                                                   InstantiationException,
                                                   IllegalAccessException,
                                                   NoClassDefFoundError,
                                                   ExceptionInInitializerError,
                                                   UnsatisfiedLinkError
        {
        final Class c = Class.forName(LocalTimeManager.CLASS_NAME);

        return (((TimeManager) c.newInstance()));
        }

    }
