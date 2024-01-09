/*
 * SerialPortEventListener.java	
 *
 * @author Imsys Technologies AB, Copyright (C) 2003-2007
 *
 */

package javax.comm;
import java.util.*;

public interface SerialPortEventListener extends EventListener
{
	/*
	Propagates serial port events.
    */

	public abstract void serialEvent(SerialPortEvent ev);
		/*
		Propagates a SerialPortEvent event.
	    */
}
