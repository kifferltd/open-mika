/*
 * SerialPortEvent.java	
 *
 * Copyright (C) 2003-2007 Imsys Technologies AB, All rights reserved.
 *
 */

package javax.comm;
import java.util.*;

public class SerialPortEvent extends EventObject
{
	private int eventtype;
    private SerialPort sourcePort;
    private boolean newValue;
    private boolean oldValue;
    private static String emptyString = new String("");

	public int eventType;

	public static final int DATA_AVAILABLE=0x1;

	public static final int OUTPUT_BUFFER_EMPTY=0x2;

	public static final int CTS=0x4;
	public static final int DSR=0x8;
	public static final int RI=0x10;
	public static final int CD=0x20;
	public static final int OE=0x40;
	public static final int PE=0x80;
	public static final int FE=0x100;
	public static final int BI=0x200;

	public SerialPortEvent(SerialPort srcport, int eventtype, boolean oldvalue, boolean newvalue)
    {
	    super(emptyString);

		sourcePort = srcport;
	    this.eventtype = eventtype;
	    newValue = newvalue;
	    oldValue = oldvalue;

    	return;
    }

	public void setEventType(int event)
	{
		this.eventtype = event;
	}

	public void setOldValue(boolean oldValue)
	{
		this.oldValue = oldValue;
	}

	public void setNewValue(boolean newValue)
	{
		this.newValue = newValue;
	}

	public int getEventType()
    {
		return eventtype;
    }

	public boolean getNewValue()
    {
	    return newValue;
    }

	public boolean getOldValue()
    {
		return oldValue;
    }

}
