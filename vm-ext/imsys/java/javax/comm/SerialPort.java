/*
 * SerialPort.java	
 *
 * @author Imsys Technologies AB, Copyright (C) 2003-2007
 *
 */
package javax.comm;

public abstract class SerialPort extends CommPort
{
	/*An RS-232 serial communications port.
	SerialPort describes the low-level interface to a serial
	communications port made available by the underlying system.
	SerialPort defines the minimum required functionality for serial communications ports.
    */
 
	public static final int DATABITS_5 = 5;
	/*5 data bit format.
    */

	public static final int DATABITS_6 = 6;
	/*6 data bit format.
    */

	public static final int DATABITS_7 = 7;
	/*7 data bit format.
    */

	public static final int DATABITS_8 = 8;
	/*8 data bit format.
    */

	public static final int STOPBITS_1 = 1;
	/*Number of STOP bits - 1.
    */

	public static final int STOPBITS_2 = 2;
	/*Number of STOP bits - 2.
    */

	public static final int STOPBITS_1_5 = 3;
	/*Number of STOP bits - 1-1/2. Some UARTs permit 1-1/2 STOP bits only with 5 data bit format, but permit 1 or 2 STOP bits with any format.
    */

	public static final int PARITY_NONE = 0;
	/*No parity bit.
    */

	public static final int PARITY_ODD = 1;
	/*ODD parity scheme. The parity bit is added so there are an odd number of TRUE bits.
    */

	public static final int PARITY_EVEN = 2;
	/*EVEN parity scheme. The parity bit is added so there are an even number of TRUE bits.
    */

	public static final int PARITY_MARK = 3;
	/*MARK parity scheme.
    */

	public static final int PARITY_SPACE = 4;
	/*SPACE parity scheme.
    */

	public static final int FLOWCONTROL_NONE = 0x00;
	/*Flow control off.
    */

	public static final int FLOWCONTROL_RTSCTS_IN = 0x01;
	/*RTS/CTS flow control on input.
    */

	public static final int FLOWCONTROL_RTSCTS_OUT = 0x02;
	/*RTS/CTS flow control on output.
    */

	public static final int FLOWCONTROL_RTSCTS = 0x03;
	/*RTS/CTS flow control on both input and output.
    */

	public static final int FLOWCONTROL_XONXOFF_IN = 0x04;
	/*XON/XOFF flow control on input.
    */

	public static final int FLOWCONTROL_XONXOFF_OUT = 0x08;
	/*XON/XOFF flow control on output.
    */

	public static final int FLOWCONTROL_XONXOFF = 0x0C;
	/*XON/XOFF flow control on both input and output.
    */

	public static final int FLOWCONTROL_ASYMM_OUT = 0x20;
	/*Asymmetric RTS/CTS flow control on output.
    */

	public SerialPort()
    {
    	return;
    }

	public abstract int getBaudRate();
		/*Gets the currently configured baud rate.
	    */
		/*
		Returns: 
		integer value indicating the baud rate
	    */

	public abstract int getDataBits();
		/*Gets the currently configured number of data bits.
	    */
	    /*
		Returns: 
		integer that can be equal to DATABITS_5, DATABITS_6, DATABITS_7, or DATABITS_8 
	    */

	public abstract int getStopBits();
		/*
		Gets the currently defined stop bits. 

		Returns: 
		integer that can be equal to STOPBITS_1, STOPBITS_2, or STOPBITS_1_5
	    */

	public abstract int getParity();
		/*
		Get the currently configured parity setting. 

		Returns: 
		integer that can be equal to PARITY_NONE, PARITY_ODD, PARITY_EVEN, PARITY_MARK or PARITY_SPACE.
	    */

	public abstract void sendBreak(int millis);
		/*
		Sends a break of millis milliseconds duration. Note that it may not be possible to time the duration of the break under certain Operating Systems. Hence this parameter is advisory. 

		Parameters: 
		millis - duration of break to send
	    */

	public abstract void setFlowControlMode(int flowcontrol) throws UnsupportedCommOperationException;
		/*
		Sets the flow control mode. 

		Parameters:
		 
		flow - control Can be a bitmask combination of 
		FLOWCONTROL_NONE: no flow control 
		FLOWCONTROL_RTSCTS_IN: RTS/CTS (hardware) flow control for input 
		FLOWCONTROL_RTSCTS_OUT: RTS/CTS (hardware) flow control for output 
		FLOWCONTROL_RTSCTS: RTS/CTS (hardware) flow control for both input and output 
		FLOWCONTROL_XONXOFF_IN: XON/XOFF (software) flow control for input 
		FLOWCONTROL_XONXOFF_OUT: XON/XOFF (software) flow control for output
		FLOWCONTROL_XONXOFF: XON/XOFF (software) flow control for both input and output
		FLOWCONTROL_ASYMM_OUT: Asymmetric RTS/CTS (hardware) flow control for output

		Throws: UnsupportedCommOperationException 
		if any of the flow control mode was not supported by the underline OS, or if input and output flow control are set to different values, i.e. one hardware and one software. The flow control mode will revert to the value before the call was made.
	    */

	public abstract int getFlowControlMode();
		/*
		Gets the currently configured flow control mode. 

		Returns: 
		an integer bitmask of the modes FLOWCONTROL_NONE, FLOWCONTROL_RTSCTS, FLOWCONTROL_XONXOFF, and FLOWCONTROL_ASYMM_OUT.
	    */

	public void setRcvFifoTrigger(int trigger)
    {
		/*Note: setRcvFifoTrigger() is deprecated. This was advisory only. 

		Set the Receive Fifo trigger level If the uart has a FIFO and if it can have programmable trigger levels, then this method will cause the uart to raise an interrupt after trigger bytes have been received. 

		Parameters: 
		trigger - level
	    */

		return;
    }

	public abstract void setSerialPortParams(int baudrate, int dataBits, int stopBits, int parity)
		throws UnsupportedCommOperationException;
		/*
		Sets serial port parameters. 

		Parameters: 

		baudrate - If the baudrate passed in by the application is unsupported by the driver,
		the driver will throw an UnsupportedCommOperationException

		dataBits - 
		DATABITS_5: 5 bits 
		DATABITS_6: 6 bits 
		DATABITS_7: 7 bits 
		DATABITS_8: 8 bits 

		stopBits - 
		STOPBITS_1: 1 stop bit 
		STOPBITS_2: 2 stop bits 
		STOPBITS_1_5: 1.5 stop bits 

		parity - 
		PARITY_NONE: no parity 
		PARITY_ODD: odd parity 
		PARITY_EVEN: even parity 
		PARITY_MARK: mark parity 
		PARITY_SPACE: space parity 

		Throws: UnsupportedCommOperationException 
		if any of the above parameters are specified incorrectly.
		All four of the parameters will revert to the values before the call was made. 

		DEFAULT: 9600 baud, 8 data bits, 1 stop bit, no parity
	    */

	public abstract void setDTR(boolean dtr);
		/*
		Sets or clears the DTR (Data Terminal Ready) bit in the UART, if supported by the underlying implementation. 

		Parameters: 
		dtr - 
		true: set DTR 
		false: clear DTR
	    */

	public abstract boolean isDTR();
		/*
		Gets the state of the DTR (Data Terminal Ready) bit in the UART, if supported by the underlying implementation.
	    */

	public abstract void setRTS(boolean rts);
		/*
		Sets or clears the RTS (Request To Send) bit in the UART, if supported by the underlying implementation. 

		Parameters: 
		rts - 
		true: set RTS 
		false: clear RTS
	    */

	public abstract boolean isRTS();
		/*
		Gets the state of the RTS (Request To Send) bit in the UART, if supported by the underlying implementation.
	    */

	public abstract boolean isCTS();
		/*
		Gets the state of the CTS (Clear To Send) bit in the UART, if supported by the underlying implementation.
	    */

	public abstract boolean isDSR();
		/*
		Gets the state of the DSR (Data Set Ready) bit in the UART, if supported by the underlying implementation.
	    */

	public abstract boolean isRI();
		/*
		Gets the state of the RI (Ring Indicator) bit in the UART, if supported by the underlying implementation.
	    */

	public abstract boolean isCD();
		/*
		Gets the state of the CD (Carrier Detect) bit in the UART, if supported by the underlying implementation.
	    */

	public abstract void addEventListener(SerialPortEventListener lsnr)
		throws java.util.TooManyListenersException;
	    /*
		Registers a SerialPortEventListener object to listen for SerialEvents. Interest in specific events may be expressed using the notifyOnXXX calls. The serialEvent method of SerialPortEventListener will be called with a SerialEvent object describing the event. 
		The current implementation only allows one listener per SerialPort. Once a listener is registered, subsequent call attempts to addEventListener will throw a TooManyListenersException without effecting the listener already registered. 

		All the events received by this listener are generated by one dedicated thread that belongs to the SerialPort object. After the port is closed, no more event will be generated. Another call to open() of the port's CommPortIdentifier object will return a new CommPort object, and the lsnr has to be added again to the new CommPort object to receive event from this port. 

		Parameters: 
		lsnr - The SerialPortEventListener object whose serialEvent method will be called with a SerialEvent describing the event. 

		Throws: TooManyListenersException 
		If an initial attempt to attach a listener succeeds, subsequent attempts will throw TooManyListenersException without effecting the first listener.
	    */

	public abstract void removeEventListener();
    	/*
		Deregisters event listener registered using addEventListener. 
		This is done automatically at port close.
	    */

	public abstract void notifyOnDataAvailable(boolean enable);
    	/*
		Expresses interest in receiving notification when input data is available. This may be used to drive asynchronous input. When data is available in the input buffer, this event is propagated to the listener registered using addEventListener. 
		The event will be generated once when new data arrive at the serial port. Even if the user doesn't read the data, it won't be generated again until next time new data arrive. 


		Parameters: 
		enable - 
		true: enable notification 
		false: disable notification
	    */

	public abstract void notifyOnOutputEmpty(boolean enable);
		/*
		Expresses interest in receiving notification when the output buffer is empty. This may be used to drive asynchronous output. When the output buffer becomes empty, this event is propagated to the listener registered using addEventListener. The event will be generated after a write is completed, when the system buffer becomes empty again. 
		This notification is hardware dependent and may not be supported by all implementations. 


		Parameters: 
		enable - 
		true: enable notification 
		false: disable notification
	    */

	public abstract void notifyOnCTS(boolean enable);
		/*
		Expresses interest in receiving notification when the CTS (Clear To Send) bit changes. 
		This notification is hardware dependent and may not be supported by all implementations. 


		Parameters: 
		enable - 
		true: enable notification
	    */

	public abstract void notifyOnDSR(boolean enable);
		/*
		Expresses interest in receiving notification when the DSR (Data Set Ready) bit changes. 
		This notification is hardware dependent and may not be supported by all implementations. 


		Parameters: 
		enable - 
		true: enable notification 
		false: disable notification
	    */

	public abstract void notifyOnRingIndicator(boolean enable);
		/*
		Expresses interest in receiving notification when the RI (Ring Indicator) bit changes. 
		This notification is hardware dependent and may not be supported by all implementations. 


		Parameters: 
		enable - 
		true: enable notification 
		false: disable notification
	    */

	public abstract void notifyOnCarrierDetect(boolean enable);
		/*
		Expresses interest in receiving notification when the CD (Carrier Detect) bit changes. 
		This notification is hardware dependent and may not be supported by all implementations. 


		Parameters: 
		enable - 
		true: enable notification 
		false: disable notification
	    */

	public abstract void notifyOnOverrunError(boolean enable);
		/*
		Expresses interest in receiving notification when there is an overrun error. 
		This notification is hardware dependent and may not be supported by all implementations. 


		Parameters: 
		enable - 
		true: enable notification 
		false: disable notification
	    */

	public abstract void notifyOnParityError(boolean enable);
		/*
		Expresses interest in receiving notification when there is a parity error. 
		This notification is hardware dependent and may not be supported by all implementations. 


		Parameters: 
		enable - 
		true: enable notification 
		false: disable notification
	    */

	public abstract void notifyOnFramingError(boolean enable);
		/*
		Expresses interest in receiving notification when there is a framing error. 
		This notification is hardware dependent and may not be supported by all implementations. 


		Parameters: 
		enable - 
		true: enable notification 
		false: disable notification
	    */

	public abstract void notifyOnBreakInterrupt(boolean enable);
		/*
		Expresses interest in receiving notification when there is a break interrupt on the line. 
		This notification is hardware dependent and may not be supported by all implementations. 


		Parameters: 
		enable - 
		true: enable notification 
		false: disable notification 
	    */

}
