package se.imsys.system;

import se.imsys.system.IllegalAddressException;

public class IllegalAddressException extends Exception
{
	public IllegalAddressException()
    {
    }


	public IllegalAddressException(String msg)
    {
		super(msg);
    }
}
