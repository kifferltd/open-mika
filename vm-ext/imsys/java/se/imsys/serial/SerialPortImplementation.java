/*
 * SerialPortImplementation.java	
 *
 * @author Imsys Technologies AB, Copyright (C) 2003-2007
 *
 */

package se.imsys.serial;

import java.util.*;
import java.io.*;
import com.sun.cldc.io.j2me.comm.*;
import javax.comm.*;

public class SerialPortImplementation extends SerialPort
{
	private SerialPortInputStream is;
    private SerialPortOutputStream os;

	private int handle = 0;

	private final int RTS = 0;
	private final int CTS = 1;
	private final int DTR = 2;
	private final int DSR = 3;
	private final int CD  = 4;
	private final int RI  = 5;

	private final int ON_DATA_AVAILABLE	= 0x0001;
	private final int ON_OUTPUT_EMPTY	= 0x0002;
	private final int ON_OVERRUN		= 0x0004;
	private final int ON_PARITYERROR	= 0x0008;
	private final int ON_FRAMINGERROR	= 0x0010;
	private final int ON_BREAK			= 0x0020;

	private int baudRate = 9600;
    private int dataBits = DATABITS_8;
    private int stopBits = STOPBITS_1;
    private int parity 	 = PARITY_NONE;
    private int flowControlMode = FLOWCONTROL_NONE;

    private int inputBufferSize = 1024;
    private int outputBufferSize = 1024;

    private int trigger = 0;
    private boolean dtr = false;
    private boolean rts = false;
    private boolean cts = false;
    private boolean dsr = false;
    private boolean ri = false;
    private boolean cd = false;

    private SerialPortEventListener listener;
    private SerialPortEvent event;

    private boolean notifyOnDataAvailable = false;
    private boolean notifyOnOutputEmpty = false;
    private boolean notifyOnCTS = false;
    private boolean notifyOnDSR = false;
    private boolean notifyOnRingIndicator = false;
    private boolean notifyOnCarrierDetect = false;
    private boolean notifyOnOverrunError = false;
    private boolean notifyOnParityError = false;
    private boolean notifyOnFramingError = false;
    private boolean notifyOnBreakInterrupt = false;

    private boolean receiveFramingEnabled = false;
    private boolean receiveTimeoutEnabled = false;
    private boolean receiveThresholdEnabled = false;

    private int receiveFramingByte;
	private int receiveTimeout;
    private int receiveThreshold;

	/*
     * Constructor
	 *
	 */
	public SerialPortImplementation()
    {
    	is = new SerialPortInputStream();
	    os = new SerialPortOutputStream();

    	event = new SerialPortEvent(this, SerialPortEvent.DATA_AVAILABLE, true, false);
    }

	
	public void open(int port) throws PortInUseException
    {
    	handle = open0(port, is, os);
	    if (handle == 0)
	    	throw(new PortInUseException("serial port is already opened"));

	    try {
	    	setSerialPortParams(baudRate, dataBits, stopBits, parity);
		    setInputBufferSize(inputBufferSize);
		    setOutputBufferSize(outputBufferSize);
		    setFlowControlMode(flowControlMode);
		    name = getPortName(port);
	    }

	    catch(UnsupportedCommOperationException e) {
	    }
    }

	public int getBaudRate()
    {
	    return baudRate;
    }

	public int getDataBits()
    {
	    return dataBits;
    }

	public int getStopBits()
    {
	    return stopBits;
    }

	public int getParity()
    {
	    return parity;
    }

	public void sendBreak(int millis)
    {
    	sendBreak0(handle, millis);
    }

	public void setFlowControlMode(int flowcontrol)
		throws UnsupportedCommOperationException
    {
	    if ((flowcontrol == FLOWCONTROL_NONE) || 
		    (flowcontrol == FLOWCONTROL_RTSCTS) ||
		    (flowcontrol == FLOWCONTROL_XONXOFF) ||
		    (flowcontrol == FLOWCONTROL_ASYMM_OUT))
	    	setFlowControlMode0(handle, flowcontrol);
	    else {
	    	throw new UnsupportedCommOperationException("Unsupported flow control mode");
	    }
	    flowControlMode = flowcontrol;
    }

	public int getFlowControlMode()
	{
	    return flowControlMode;
    }

	public void setRcvFifoTrigger(int trigger)
    {
	    this.trigger = trigger;
    }

	public void setSerialPortParams(int baudrate, int dataBits, int stopBits, int parity)
		throws UnsupportedCommOperationException
    {
		// Validate the parameters
	    if((dataBits != DATABITS_7) && (dataBits != DATABITS_8))
	    	throw new UnsupportedCommOperationException("Unsupported data bit format");

		if((parity != PARITY_NONE) && (parity != PARITY_ODD) && (parity != PARITY_EVEN))
	    	throw new UnsupportedCommOperationException("Unsupported parity mode");

	    this.baudRate = baudrate;
		this.dataBits = dataBits;
		this.stopBits = stopBits;
		this.parity = parity;

	    setSerialPortParams0(handle, baudRate, dataBits, stopBits, parity);
    }

	public void setDTR(boolean dtr)
    {
    	setBit(handle, DTR, dtr);
    }

	public boolean isDTR()
    {
	    return getBit(handle, DTR);
    }

	public void setRTS(boolean rts)
    {
    	setBit(handle, RTS, rts);
    }

	public boolean isRTS()
    {
	    return getBit(handle, RTS);
    }

	public boolean isCTS()
    {
	    return getBit(handle, CTS);
    }

	public boolean isDSR()
    {
	    return getBit(handle, DSR);
    }

	public boolean isRI()
    {
	    return getBit(handle, RI);
    }

	public boolean isCD()
    {
	    return getBit(handle, CD);
    }

	private void callback(int e)
    {
	    int eventType = 0;

		if ((e & ON_DATA_AVAILABLE) == ON_DATA_AVAILABLE) {
	    	eventType =  SerialPortEvent.DATA_AVAILABLE;
		    e &= ~SerialPortEvent.DATA_AVAILABLE;
//	    	event = new SerialPortEvent(this, eventType, true, false);
			event.setEventType(eventType);
    		listener.serialEvent(event);
	    }

		if ((e & ON_OUTPUT_EMPTY) == ON_OUTPUT_EMPTY) {
	    	eventType =  SerialPortEvent.OUTPUT_BUFFER_EMPTY;
		    e &= ~SerialPortEvent.OUTPUT_BUFFER_EMPTY;
//	    	event = new SerialPortEvent(this, eventType, true, false);
			event.setEventType(eventType);
    		listener.serialEvent(event);
	    }

		if (e != 0) {
			if ((e & ON_OVERRUN) == ON_OVERRUN)
		    	eventType =  SerialPortEvent.OE;
			else if ((e & ON_PARITYERROR) == ON_PARITYERROR)
	    		eventType =  SerialPortEvent.PE;
			else if ((e & ON_FRAMINGERROR) == ON_FRAMINGERROR)
		    	eventType =  SerialPortEvent.FE;
			else if ((e & ON_BREAK) == ON_BREAK)
		    	eventType =  SerialPortEvent.BI;
	    
//    		event = new SerialPortEvent(this, eventType, true, false);
			event.setEventType(eventType);
	    	listener.serialEvent(event);
	    }

    	return;
    }

	public void addEventListener(SerialPortEventListener lsnr)
		throws java.util.TooManyListenersException
    {
	    if(handle == 0)
	    	return;

	    listener = lsnr;

	    addEventListener0(handle);

		if (notifyOnDataAvailable)
		    notifyOn(handle, ON_DATA_AVAILABLE, true);

	    if (notifyOnOutputEmpty)
		    notifyOn(handle, ON_OUTPUT_EMPTY, true);

    	if (notifyOnOverrunError)
		    notifyOn(handle, ON_OVERRUN, true);

    	if (notifyOnParityError)
		    notifyOn(handle, ON_PARITYERROR, true);

    	if (notifyOnFramingError)
		    notifyOn(handle, ON_FRAMINGERROR, true);
    }

	public void removeEventListener()
    {
	    listener = null;
	    removeEventListener0(handle);
    }

	public void notifyOnDataAvailable(boolean enable)
    {
	    notifyOnDataAvailable = enable;
	    notifyOn(handle, ON_DATA_AVAILABLE, enable);
    }

	public void notifyOnOutputEmpty(boolean enable)
    {
	    notifyOnOutputEmpty = enable;
	    notifyOn(handle, ON_OUTPUT_EMPTY, enable);
    }

	public void notifyOnCTS(boolean enable)
    {
    }

	public void notifyOnDSR(boolean enable)
    {
    }

	public void notifyOnRingIndicator(boolean enable)
    {
    }

	public void notifyOnCarrierDetect(boolean enable)
    {
    }

	public void notifyOnOverrunError(boolean enable)
    {
    	notifyOnOverrunError = enable;
	    notifyOn(handle, ON_OVERRUN, enable);
    }

	public void notifyOnParityError(boolean enable)
    {
    	notifyOnParityError = enable;
	    notifyOn(handle, ON_PARITYERROR, enable);
    }

	public void notifyOnFramingError(boolean enable)
    {
    	notifyOnFramingError = enable;
	    notifyOn(handle, ON_FRAMINGERROR, enable);
    }

	public void notifyOnBreakInterrupt(boolean enable)
    {
    }

	public InputStream getInputStream() throws IOException
    {
	    if (is == null)
	    	throw new IOException("InputStream closed");
	    return is;
    }

	public OutputStream getOutputStream() throws IOException
    {
	    if(os == null)
	    	throw new IOException("OutputStream closed");
	    return os;
    }

	public void close()
    {
	    try {
	    	is.close();
		    os.close();

			close0();

			CommPortIdentifier cpi = CommPortIdentifier.getPortIdentifier(name);
		    cpi.close(this);
	    }

	    catch( IOException e ) {
	    }

	    catch( NoSuchPortException e ) {
	    }

	    handle = 0;
    }

	public void enableReceiveThreshold(int thresh)
		throws UnsupportedCommOperationException
    {
    	if (thresh > 0) {
	    	receiveThreshold = thresh;
    		enableReceiveThreshold0(handle, thresh);
		    receiveThresholdEnabled = true;
	    }    	
    }

	public void disableReceiveThreshold()
    {
    	enableReceiveThreshold0(handle, -1);
    	receiveThresholdEnabled = false;
    }

	public boolean isReceiveThresholdEnabled()
    {
	    return receiveThresholdEnabled;
    }

	public int getReceiveThreshold()
    {
	    return receiveThresholdEnabled ? receiveThreshold : 0;
    }

	public void enableReceiveTimeout(int rcvTimeout)
		throws UnsupportedCommOperationException
    {
    	if (rcvTimeout >= 0) {
	    	receiveTimeout = rcvTimeout;
    		enableReceiveTimeout0(handle, rcvTimeout);
		    receiveTimeoutEnabled = true;
	    }
    }

	public void disableReceiveTimeout()
    {
    	if (receiveTimeoutEnabled) {
			enableReceiveTimeout0(handle, -1);
		    receiveTimeoutEnabled = false;
	    }
    }

	public boolean isReceiveTimeoutEnabled()
    {
	    return receiveTimeoutEnabled;
    }
	
	public int getReceiveTimeout()
    {
	    return receiveTimeoutEnabled ? receiveTimeout : 0;
    }

	public void enableReceiveFraming(int framingByte)
		throws UnsupportedCommOperationException
    {
		receiveFramingByte = framingByte; 	
		enableReceiveFraming0(handle, framingByte);    	
	    receiveFramingEnabled = true;
    }

	public void disableReceiveFraming()
    {
		enableReceiveFraming0(handle, -1);    	
	    receiveFramingEnabled = false;
    }

	public boolean isReceiveFramingEnabled()
    {
	    return receiveFramingEnabled;
    }

	public int getReceiveFramingByte()
    {
	    return receiveFramingEnabled ? receiveFramingByte & 0xff : 0;
    }

	public void setInputBufferSize(int size)
    {
	    inputBufferSize = size;
	    setInputBufferSize0(handle, size);
    }

	public int getInputBufferSize()
    {
	    return inputBufferSize;
    }

	public void setOutputBufferSize(int size)
    {
	    outputBufferSize = size;
	    setOutputBufferSize0(handle, size);
    }

	public int getOutputBufferSize()
    {
	    return outputBufferSize;
    }

	public int getHandle()
    {
    	return(handle);
    }

	// Native methods
	private native int open0(int port, SerialPortInputStream is, SerialPortOutputStream os);
	private native void close0();
	private native void setFlowControlMode0(int handle,int flowcontrol);
	public native void setSerialPortParams0(int handle,int baudrate, int dataBits,int stopBits, int parity)
		throws UnsupportedCommOperationException;
    private native void addEventListener0(int handle);
	private native void removeEventListener0(int handle);
    private native void notifyOn(int handle, int event, boolean enable);
	private native void setInputBufferSize0(int handle,int size);
	private native void setOutputBufferSize0(int handle,int size);
    private native void sendBreak0(int handle, int millis);
    private native void setBit(int handle, int bit, boolean value);
    private native boolean getBit(int handle, int bit);
	private static native String getPortName(int port);
    private native void enableReceiveTimeout0(int handle, int rcvTimeout);
    private native void enableReceiveThreshold0(int handle, int thresh);
    private native void enableReceiveFraming0(int handle, int framingByte);

}
