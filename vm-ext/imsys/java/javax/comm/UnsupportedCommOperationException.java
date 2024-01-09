/*
 * UnsupportedCommOperationException.java	
 *
 * @author Imsys Technologies AB, Copyright (C) 2003-2007
 *
 */

package javax.comm;

public class UnsupportedCommOperationException extends Exception
{
	/*Thrown when a driver doesn't allow the specified operation.
	 */
    private static String text = new String("UnsupportedCommOperationException");

	public UnsupportedCommOperationException(String str)
    {
		/*Constructs an UnsupportedCommOperationException with the specified detail message. 
		 *
		 *Parameters: 
		 *s - the detail message. 
		 */

		super(str);
    	return;
    }

	public UnsupportedCommOperationException()
    {
		/*Constructs an UnsupportedCommOperationException with no detail message. 
	     */

		this( text );
    	return;
    }
}
