/*
 * Copyright (c) 2002 Imsys AB, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of 
 * Imsys AB ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Sun.
 *
 * IMSYS MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
 * SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, OR NON-INFRINGEMENT. IMSYS SHALL NOT BE LIABLE FOR ANY DAMAGES
 * SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 * THIS SOFTWARE OR ITS DERIVATIVES.
 */
package com.sun.cldc.io.j2me.comm;

import com.sun.cldc.io.*;
import java.io.*;
import javax.microedition.io.*;
import java.util.*;

public class Protocol extends ConnectionBase implements StreamConnection
{
    private SerialPortObject com;

	private static String strBaudrate 		= new String("baudrate=");
    private static String strDatabits 		= new String("databits=");
    private static String strStopbits 		= new String("stopbits=");
    private static String strParity 		= new String("parity=");

    private static String strFlowControl 	= new String("flowcontrol=");
    private static String strBufferSize 	= new String("buffersize=");

	public void Protocol()
    {
    	return;
    }

    /**
     * @param name The target of the connection
     * @param mode A specifier of the access mode
     * @param timeouts A flag to indicate that the called wants timeout exceptions
     */
    public void open(String name, int mode, boolean timeouts)
		throws IOException
	{
	    int baudrate = 9600;									// Default baudrate 
 	    int databits = SerialPortObject.DATABITS_8;				// Default data bits length
	    int stopbits = SerialPortObject.STOPBITS_1;				// Default stop bits length
	    int parity 	 = SerialPortObject.PARITY_NONE;			// Default parity
	    int bufsize  = 1024;									// Default buffer size
	    int flowcontrol = SerialPortObject.FLOWCONTROL_NONE;	// Default flow control mode	

		int port, pos0, pos1;
	    String str;

		// Work with lowercase string
		name = name.toLowerCase();

		// Get port number
	    try {
		    port = Integer.parseInt(name.substring(0 ,name.indexOf(';')));
	    }
	    catch (NumberFormatException e) {
	    	throw new IOException( "Invalid port name" );
	    }

		// Get baudrate
	    if ((pos0 = name.indexOf(strBaudrate)) != -1) {
	    	pos0 += strBaudrate.length();
		    if ((pos1 = name.indexOf(";", pos0)) != -1)
			    str = name.substring(pos0, pos1);
		    else
		    	str = name.substring(pos0);
		    baudrate = Integer.valueOf(str).intValue();
		 }

		// Get number of data bits
	    if ((pos0 = name.indexOf(strDatabits)) != -1) {
	    	pos0 += strDatabits.length();
		    if ((pos1 = name.indexOf(";", pos0)) != -1)
			    str = name.substring(pos0, pos1);
		    else
		    	str = name.substring(pos0);
		    if (str.compareTo("7") == 0) 	
		     	databits = SerialPortObject.DATABITS_7;
		    else if (str.compareTo("8") == 0)
		     	databits = SerialPortObject.DATABITS_8;
		    else
		    	throw new IOException( "Illegal data bit size" );
	    }

		// Get number of stop bits
	    if ((pos0 = name.indexOf(strStopbits)) != -1) {
	    	pos0 += strStopbits.length();
		    if ((pos1 = name.indexOf(";", pos0)) != -1)
			    str = name.substring(pos0, pos1);
		    else
		    	str = name.substring(pos0);
		    if (str.compareTo("1") == 0) 	
		     	stopbits = SerialPortObject.STOPBITS_1;
		    else if (str.compareTo("1.5") == 0)
		     	stopbits = SerialPortObject.STOPBITS_1_5;
			else if (str.compareTo("2") == 0)
		     	stopbits = SerialPortObject.STOPBITS_2;
		    else
		    	throw new IOException( "Illegal stop bit size" );
	    }


		// Get number parity
	    if ((pos0 = name.indexOf(strParity)) != -1) {
	    	pos0 += strParity.length();
		    if ((pos1 = name.indexOf(";", pos0)) != -1)
			    str = name.substring(pos0, pos1);
		    else
		    	str = name.substring(pos0);
		    if (str.compareTo("e") == 0) 	
		     	parity = SerialPortObject.PARITY_EVEN;
		    else if (str.compareTo("o") == 0)
		     	parity = SerialPortObject.PARITY_ODD;
			else if (str.compareTo("n") == 0)
		     	parity = SerialPortObject.PARITY_NONE;
		    else
		    	throw new IOException( "Illegal parity" );
	    }

		// Get buffer size
		if((pos0 = name.indexOf(strBufferSize)) !=-1) {
	    	pos0 += strBufferSize.length();
		    if ((pos1 = name.indexOf(";", pos0)) != -1)
			    str = name.substring(pos0, pos1);
		    else
		    	str = name.substring(pos0);
			
			try {
		    	bufsize = Integer.parseInt(str);
			}

			catch (NumberFormatException e) {
		    	throw new IOException( "Illegal buffer size" );	
		    }
	    }

		// Get flow control mode
	    if ((pos0 = name.indexOf(strFlowControl)) != -1) {
	    	pos0 += strFlowControl.length();
		    if ((pos1 = name.indexOf(";", pos0)) != -1)
			    str = name.substring(pos0, pos1);
		    else
		    	str = name.substring(pos0);
		    if (str.compareTo("none") == 0)
		     	flowcontrol = SerialPortObject.FLOWCONTROL_NONE;
		    else if (str.compareTo("rtscts") == 0)
		     	flowcontrol = SerialPortObject.FLOWCONTROL_RTSCTS;
		    else if (str.compareTo("xonxoff") == 0)
		     	flowcontrol = SerialPortObject.FLOWCONTROL_XONXOFF;
		    else
		    	throw new IOException("Illegal flow control mode");
	    }

		// Create and init serial port object
    	com = new SerialPortObject();
		com.open(port);
	    com.setSerialPortParams(baudrate, databits, stopbits, parity, flowcontrol, bufsize);
    }

    public InputStream openInputStream() throws IOException
	{
    	if (com == null)
	    	throw new IOException("");
        return (InputStream)com.getInputStream();
    }

    public OutputStream openOutputStream() throws IOException
	{
    	if (com == null)
	    	throw new IOException("");
        return (OutputStream)com.getOutputStream();
    }

    public void close() throws IOException
	{
    	if (com == null)
	    	throw new IOException("");
	    com.close();
    	return;
    }
}

