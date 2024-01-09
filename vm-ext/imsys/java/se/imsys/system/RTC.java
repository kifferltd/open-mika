package se.imsys.system;

/** 
 * This class gives low level access to the RTC.
 *
 * @author Imsys Technologies AB, Copyright (C) 2003-2007.
 */

public class RTC {
   /**
     * Sets a new time in the RTC, Real Time Clock. Value is always a
     * number of milliseconds since the beginning of the epoch in UTC.
     *
     * @param      	time	New time in milliseconds.
     */
	public static native void setTime(long time);

   /**
     * Reads the current time from the RTC, Real Time Clock. Value is always a
     * number of milliseconds since the beginning of the epoch in UTC
     *
     * @return The current time in milliseconds.
     */
	public static native long getTime();
}



