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

import java.util.*;
import java.io.*;

public class SerialPortObject extends Object
{
	private SerialPortInputStream is;
    private SerialPortOutputStream os;

	private int baudRate			= 9600;
    private int dataBits			= DATABITS_8;
    private int stopBits			= STOPBITS_1;
    private int parity				= PARITY_NONE;
    private int flowcontrol 		= FLOWCONTROL_NONE;
    private int bufsize				= 1024;

	public static final int DATABITS_5 = 0x1;
	public static final int DATABITS_6 = 0x2;
	public static final int DATABITS_7 = 0x4;
	public static final int DATABITS_8 = 0x8;

	public static final int STOPBITS_1 = 0x10;
	public static final int STOPBITS_2 = 0x20;
	public static final int STOPBITS_1_5 = 0x40;

	public static final int PARITY_NONE = 0x80;
	public static final int PARITY_ODD = 0x100;
	public static final int PARITY_EVEN = 0x200;
	public static final int PARITY_MARK = 0x400;
	public static final int PARITY_SPACE = 0x800;

	public static final int FLOWCONTROL_NONE = 0;
	public static final int FLOWCONTROL_RTSCTS = 1;
	public static final int FLOWCONTROL_XONXOFF = 2;
									
	protected SerialPortObject()
    {
    	is = new SerialPortInputStream();
	    os = new SerialPortOutputStream();
    }


	public void open(int port) throws IOException
    {
    	if(open0(port, is, os) == 0 )
	    	throw new IOException("Failed to open serial port");

    	setSerialPortParams(baudRate, dataBits, stopBits, parity, flowcontrol, bufsize);
	    return;
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

	protected void setSerialPortParams(int baudrate, int dataBits,	int stopBits, int parity, int flowcontrol, int bufsize) 
		throws IOException
    {
	    if(setSerialPortParams0(baudrate, dataBits, stopBits, parity, flowcontrol, bufsize) != 0)
	    	throw new IOException("Unable to set settings");

		this.baudRate = baudrate;
	    this.dataBits = dataBits;
    	this.stopBits = stopBits;
	    this.parity = parity;
	    this.flowcontrol = flowcontrol;
	    this.bufsize = bufsize;
    }

	public InputStream getInputStream() throws IOException
    {
	    if(is == null)
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
	    }
	    catch(IOException e) {

	    }
	    is = null;
	    os = null;
	    close0();
    }

	private native int open0(int port, SerialPortInputStream is, SerialPortOutputStream os);
	private native void close0();
 	private native int setSerialPortParams0(int baudrate, int dataBits,int stopBits, int parity, int flowcontol, int bufsize);
}
