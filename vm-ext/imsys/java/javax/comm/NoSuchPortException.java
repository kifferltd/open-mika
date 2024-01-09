/*
 * NoSuchPortException.java	
 *
 * @author Imsys Technologies AB, Copyright (C) 2003-2007
 *
 */

package javax.comm;

public class NoSuchPortException extends Exception
{
	/*Thrown when a driver can't find the specified port.
    */
    public NoSuchPortException(String s) {
	super(s);
    }
}

