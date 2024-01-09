/*
 * PortInUseException.java	
 *
 * @author Imsys Technologies AB, Copyright (C) 2003-2007
 *
 */

package javax.comm;
import java.lang.*;

public class PortInUseException extends Exception
{
	/*Thrown when the specified port is in use.
    */
	
	public String currentOwner;
		/*Describes the current owner of the communications port.
	    */

	public PortInUseException(String description)
	{
		super(description);
	}	

	public PortInUseException()
	{
		super();
	}	
}



