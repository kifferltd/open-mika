/*
 * CommDriver.java	
 *
 * @author Imsys Technologies AB, Copyright (C) 2003-2007
 *
 */

package javax.comm;

public interface CommDriver
{ 
	/*Part of the loadable device driver interface.
	CommDriver should not be used by application-level programs.
    */

	public abstract void initialize();
    	/*
		initialize() will be called by the CommPortIdentifier's static initializer.
		The responsibility of this method is:
		1) Ensure that that the hardware is present.
		2) Load any required native libraries.
		3) Register the port names with the CommPortIdentifier.
	    */

	public abstract CommPort getCommPort(String portName, int portType) throws PortInUseException;
    	/*
		getCommPort() will be called by CommPortIdentifier from its open() method.
		portName is a string that was registered earlier using the CommPortIdentifier.addPortName() method.
		getCommPort() returns an object that extends either SerialPort or ParallelPort. 
	    */

}	
