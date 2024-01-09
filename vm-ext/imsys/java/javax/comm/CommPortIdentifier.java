/*
 * CommPortIdentifier.java	
 *
 * @author Imsys Technologies AB, Copyright (C) 2003-2007
 *
 */

package javax.comm;

import java.util.*;
import java.lang.*;
import java.io.*;
import com.sun.cldc.io.j2me.comm.*;
import se.imsys.serial.*;

public class CommPortIdentifier extends Object
{
	/*
	Communications port management.
	CommPortIdentifier is the central class for controlling access to communications ports.
	It includes methods for:
	Determining the communications ports made available by the driver. 
	Opening communications ports for I/O operations. 
	Determining port ownership. 
	Resolving port ownership contention. 
	Managing events that indicate changes in port ownership status. 
	An application first uses methods in CommPortIdentifier to negotiate with the driver
	to discover which communication ports are available and then select a port for opening.
	It then uses methods in other classes like CommPort, ParallelPort and SerialPort to communicate through the port.
    */

	private static Vector portIds;

    private String portName;
	private int portType;
	private CommDriver commDriver;
    private CommPort commPort;
    private CommPortOwnershipListener commPortOwnershipListener;
    private boolean owned;
    private String owner;
    private static String portInUseExceptionString=new String("PortInUseException");

	public static final int PORT_SERIAL=0x1;
	/*RS-232 serial port
    */

	public static final int PORT_PARALLEL=0x2;
	/*IEEE 1284 parallel port
    */

    static
    {
     	portIds = new Vector();
		SerialPortCommDriver spc = new SerialPortCommDriver();
	    spc.initialize();
    }

	private CommPortIdentifier(String portName, int portType, CommDriver driver)
    {
    	this.portName = portName;
	    this.portType = portType;
	    this.commDriver = driver;
	    this.commPortOwnershipListener = null;
	    this.owned = false;
    	return;
    }

	public static Enumeration getPortIdentifiers()
    {
     	portIds = new Vector();
		SerialPortCommDriver spc = new SerialPortCommDriver();
	    spc.initialize();

		return portIds.elements();
    }

	public static CommPortIdentifier getPortIdentifier(String portName)
		throws NoSuchPortException
    {
	    CommPortIdentifier portId;
	    for (int i=0;i<portIds.size();i++) {
	    	portId = (CommPortIdentifier)portIds.elementAt(i);
		    if (portName.compareTo(portId.getName())==0)
		    	return portId;
	    }
	    throw new NoSuchPortException("Port " + portName + " does not exist");
    }

	public static CommPortIdentifier getPortIdentifier(CommPort commPort)
		throws NoSuchPortException
    {
	    CommPortIdentifier portId;
	    for(int i=0;i<portIds.size();i++) {
	    	portId = (CommPortIdentifier)portIds.elementAt(i);
		    if( commPort==portId.commPort )
		    	return portId;
	    }
	    throw new NoSuchPortException("Port " + commPort.getName() + " does not exist");
    }

	public static void addPortName(String portName, int portType, CommDriver driver)
    {
		CommPortIdentifier cpi = new CommPortIdentifier(portName,portType,driver);
	    portIds.addElement(cpi);
		return;
    }

	public String getName()
    {
		return portName;
    }

	public int getPortType()
    {
 		return portType;
	}

	public String getCurrentOwner()
    {
		return owner;
    }

	public boolean isCurrentlyOwned()
    {
		return owned;
    }

	public synchronized CommPort open(String appname, int timeout)
		throws PortInUseException
    {
		if(owned) {
	    	if(commPortOwnershipListener!=null) {
		    	commPortOwnershipListener.ownershipChange(CommPortOwnershipListener.PORT_OWNERSHIP_REQUESTED);
		    }
	    	PortInUseException piue = new PortInUseException();
		    piue.currentOwner = portInUseExceptionString;
	    	throw piue;
	    }
	    owned = true;
	    owner = appname;
	    commPort = commDriver.getCommPort(portName,portType);
		return (SerialPort)commPort;
    }

	public synchronized void close(CommPort commPort)
    {
    	if (owned && (this.commPort == commPort))
		    owned = false;
    }

	/*
	public CommPort open(FileDescriptor fd) throws UnsupportedCommOperationException
    {

		throw new UnsupportedCommOperationException("Commport open with FileDescriptor Unsupported");
    }
    */

	public void addPortOwnershipListener(CommPortOwnershipListener listener)
    {
		commPortOwnershipListener = listener;
		return;
    }

	public void removePortOwnershipListener(CommPortOwnershipListener listener)
    {
		commPortOwnershipListener = null;
		return;
    }
}

