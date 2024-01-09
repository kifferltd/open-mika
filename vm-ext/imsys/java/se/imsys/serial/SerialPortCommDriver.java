/*
 * SerialPortCommDriver.java	
 *
 * @author Imsys Technologies AB, Copyright (C) 2003-2007
 *
 */

package se.imsys.serial;

import javax.comm.*;

public class SerialPortCommDriver implements CommDriver
{
	private int numPorts;

	private SerialPortImplementation[] portList;
	private String[] portNameList;

	private static native int getNumPorts();
	private static native String getPortName(int port);
	private static native boolean findPort(int port);

   	/*
	initialize() will be called by the CommPortIdentifier's static initializer.
	The responsibility of this method is:
	1) Ensure that that the hardware is present.
	2) Load any required native libraries.
	3) Register the port names with the CommPortIdentifier.
    */
	public void initialize()
    {
	    numPorts = getNumPorts();
		portList = new SerialPortImplementation[numPorts];
	    portNameList = new String[numPorts];

	    for (int i=0;i<numPorts;i++) {
		    if(findPort(i+1)) {
		    	portNameList[i] = getPortName(i+1);
		    	CommPortIdentifier.addPortName(portNameList[i], CommPortIdentifier.PORT_SERIAL,this);
		    }
    	}
	    return;
    }

   	/*
	getCommPort() will be called by CommPortIdentifier from its open() method.
	portName is a string that was registered earlier using the CommPortIdentifier.addPortName() method.
	getCommPort() returns an object that extends either SerialPort or ParallelPort. 
    */
	public CommPort getCommPort(String portName, int portType) throws PortInUseException
    {
		for (int i=0;i<numPorts;i++) {
	    	if (portNameList[i] != null) {
		    	if (portNameList[i].compareTo(portName)==0) {
			    	if (portList[i] == null) {
			    		portList[i] = new SerialPortImplementation();
			    	}
		    		portList[i].open(i+1);
				    return portList[i];
			    }
		    }
		}
	    return null;
    }
}

