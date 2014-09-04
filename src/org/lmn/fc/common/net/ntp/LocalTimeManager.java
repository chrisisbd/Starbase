
package org.lmn.fc.common.net.ntp;

import java.util.*;


/**
 * This class implements TimeManager.setTime(Date) through a native method under Windows 95 (this
 * will presumably also work under NT, if you have the correct permissions).
 */

public class LocalTimeManager extends TimeManager
    {
    // Load the DLL when the class is loaded
//    static
//        {
//        System.out.println("Load LocalTimeManager START");
//        try
//            {
//            System.loadLibrary("SetTime");
//            System.out.println("DLL loaded");
//            boolLoaded = true;
//            }
//
//        catch (UnsatisfiedLinkError exception)
//            {
//            boolLoaded = false;
//            System.out.println("!!!!!!!!! Load LocalTimeManager UnsatisfiedLinkError " + exception.getMessage());
//            }
//        System.out.println("Load LocalTimeManager END");
//        }

    public static final String CLASS_NAME = "org.lmn.fc.common.net.ntp.LocalTimeManager";

    private static TimeZone UTC = new SimpleTimeZone(0, "UTC");


    /**
     * Sets the local time to a given date. For this method to work settime.dll should be in the
     * library loadpath (for example in "c:\windows")
     */

    public void setTime(Date d) throws UnsatisfiedLinkError,
                                       SecurityException
        {
        Calendar c = new GregorianCalendar(UTC);
        c.setTime(d);
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH) + 1;
        int day = c.get(Calendar.DAY_OF_MONTH);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        int second = c.get(Calendar.SECOND);
        int millis = c.get(Calendar.MILLISECOND);

        // Call the settime DLL
        if (boolLoaded)
            {
            System.out.println("TRY TO DO DLL SetTime");
            nativeSetTime(year, month, day, hour, minute, second, millis);
            }
        }


    /***********************************************************************************************
     * Call the settime DLL method.
     *
     * @param year
     * @param month
     * @param day
     * @param hour
     * @param minute
     * @param second
     * @param millis
     */

    private native void nativeSetTime(int year, int month, int day,
                                      int hour, int minute, int second, int millis);
    }






