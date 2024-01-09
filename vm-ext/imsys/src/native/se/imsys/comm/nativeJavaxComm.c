/* 
 * nativeJavaxComm.c
 *
 * Copyright (C) 2002 Imsys AB. All rights reserved
 *
 *
 * native implementation of methods in javax.comm
 * 
 */
			   
#include <global.h>
#include <com.h>
#include <comPrivate.h>
#include <io_macro.h>
#include "superpoller.h"


// Constants a defined in javax.comm.SerialPort
#define JX_DATABITS_5						5
#define JX_DATABITS_6						6
#define JX_DATABITS_7						7
#define JX_DATABITS_8						8
#define JX_STOPBITS_1						1
#define JX_STOPBITS_2						2
#define JX_STOPBITS_1_5						3
#define JX_PARITY_NONE						0
#define JX_PARITY_ODD						1
#define JX_PARITY_EVEN						2
#define JX_PARITY_MARK						3
#define JX_PARITY_SPACE						4
#define JX_FLOWCONTROL_NONE					0x00
#define JX_FLOWCONTROL_RTSCTS_IN			0x01
#define JX_FLOWCONTROL_RTSCTS_OUT			0x02
#define JX_FLOWCONTROL_RTSCTS				0x03
#define JX_FLOWCONTROL_XONXOFF_IN			0x04
#define JX_FLOWCONTROL_XONXOFF_OUT			0x08
#define JX_FLOWCONTROL_XONXOFF				0x0C
#define JX_FLOWCONTROL_ASYMM_OUT			0x20

extern int Java_com_sun_cldc_io_j2me_comm_SerialPortObject_open0(OBJECTREF os, OBJECTREF is, int port, OBJECTREF this);
extern void Java_com_sun_cldc_io_j2me_comm_SerialPortObject_close0(OBJECTREF this);

#pragma warn variables off
/******************************************************************************
 * Java_se_imsys_serial_SerialPortCommDriver_portExists
 *
 * Syntax:  private static native int getNumPorts()
 *
 *
 *****************************************************************************/
int Java_se_imsys_serial_SerialPortCommDriver_getNumPorts()
{
	return comGetNumPorts();	
}

/******************************************************************************
 * Java_se_imsys_serial_SerialPortCommDriver_getPortName
 *
 * Syntax:  private static native String getPortName(int port)
 *
 *
 *****************************************************************************/
OBJECTREF Java_se_imsys_serial_SerialPortCommDriver_getPortName(int port)
{
	const char* name;
	OBJECTREF 	string;

	name = comGetPortName(port);
	string = instantiateString(name, strlen(name));
	return string;	
}


/******************************************************************************
 * Java_se_imsys_serial_SerialPortCommDriver_portExists
 *
 * Syntax:  private static native boolean portExists(int port)
 *
 *
 *****************************************************************************/
int Java_se_imsys_serial_SerialPortCommDriver_findPort(int port)
{
	return comPortExists(port);	
}


/******************************************************************************
 * Java_se_imsys_serial_SerialPortImplementation_open0
 *
 * Syntax:  private native int open0(int port, SerialPortInputStream is, SerialPortOutputStream os)
 *
 *
 *****************************************************************************/
int Java_se_imsys_serial_SerialPortImplementation_open0(OBJECTREF os, OBJECTREF is, int port, OBJECTREF this)
{
	// Open port with default settings
	return Java_com_sun_cldc_io_j2me_comm_SerialPortObject_open0(os, is, port, this);
}

/******************************************************************************
 * Java_se_imsys_serial_SerialPortImplementation_close0
 *
 * Syntax:  private native int close0()
 *
 *
 *****************************************************************************/
void Java_se_imsys_serial_SerialPortImplementation_close0(OBJECTREF this)
{
	// Open port with default settings
	Java_com_sun_cldc_io_j2me_comm_SerialPortObject_close0(this);
}

/******************************************************************************
 * Java_se_imsys_serial_SerialPortImplementation_setFlowControlMode0
 *
 * Syntax: private native void setFlowControlMode0(int handle,int flowcontrol) 
 *
 *
 *****************************************************************************/
void Java_se_imsys_serial_SerialPortImplementation_setFlowControlMode0(int flowcontrol, int handle, OBJECTREF this)
{
	int result = -1;
	char *str;

	if (flowcontrol == JX_FLOWCONTROL_NONE) {
    	result = comSetFlowControlMode(handle, FLOWCONTROL_NONE);
		str = "Flow control must be used";
	} else if ((flowcontrol & JX_FLOWCONTROL_RTSCTS_IN) || (flowcontrol & JX_FLOWCONTROL_RTSCTS_OUT)) {
    	result = comSetFlowControlMode(handle, FLOWCONTROL_RTSCTS);
		str = "RTS/CTS not supported on this port";
	} else if ((flowcontrol & JX_FLOWCONTROL_XONXOFF_IN) || (flowcontrol & JX_FLOWCONTROL_XONXOFF_OUT)) {
    	result = comSetFlowControlMode(handle, FLOWCONTROL_XONXOFF);
		str = "XON/XOFF not supported on this port";
	} else if (flowcontrol & JX_FLOWCONTROL_ASYMM_OUT) {
    	result = comSetFlowControlMode(handle, FLOWCONTROL_ASYMM);
		str = "Asymmetric RTS/CTS not supported on this port";
	} else {
    	str = "Bad flow control mode specified";
    }

	if (result != E_COM_SUCCESS)
    	raiseExceptionW("javax/comm/UnsupportedCommOperationException", str);
}

/******************************************************************************
 * Java_se_imsys_serial_SerialPortImplementation_setSerialPortParams0
 *
 * Syntax: public native void setSerialPortParams0(int handle,int baudrate, int databits,int stopbits, int parity)
 *				throws UnsupportedCommOperationException 
 *
 *
 *****************************************************************************/
void Java_se_imsys_serial_SerialPortImplementation_setSerialPortParams0(int parity, int stopbits, 
								int databits, int baudrate, int handle, OBJECTREF this)
{
	// Convert to com.h defines
    if (databits == JX_DATABITS_7)
    	databits = DATABITS_7;
    else if (databits == JX_DATABITS_8)
    	databits = DATABITS_8;

    if (stopbits == JX_STOPBITS_1)
    	stopbits = STOPBITS_1;
    else if ((stopbits == JX_STOPBITS_1_5) || (stopbits == JX_STOPBITS_2))
    	stopbits = STOPBITS_2;

	if (parity == JX_PARITY_NONE)
    	parity = PARITY_NONE;
	else if (parity == JX_PARITY_EVEN)
    	parity = PARITY_EVEN;
	else if (parity == JX_PARITY_ODD)
    	parity = PARITY_ODD;

	if (comConfig(handle, baudrate, databits, stopbits, parity) != E_COM_SUCCESS)
    	raiseExceptionW("javax/comm/UnsupportedCommOperationException", "Illegal serial port parameter");
    	
}

/******************************************************************************
 * Java_se_imsys_serial_SerialPortImplementation_addEventListener0
 *
 * Syntax: private native void addEventListener0(int handle) 
 *
 *
 *****************************************************************************/
void Java_se_imsys_serial_SerialPortImplementation_addEventListener0(int handle, OBJECTREF this)
{
	
	InstallComEventListener(handle, this);				

}

/******************************************************************************
 * Java_se_imsys_serial_SerialPortImplementation_removeEventListener0
 *
 * Syntax: private native void removeEventListener0(int handle) 
 *
 *
 *****************************************************************************/
void Java_se_imsys_serial_SerialPortImplementation_removeEventListener0(int handle, OBJECTREF this)
{

	RemoveComEventListener(handle, this);				
}

/******************************************************************************
 * Java_se_imsys_serial_SerialPortImplementation_notifyOn
 *
 * Syntax: private native void notifyOn(int event)
 *
 *
 *****************************************************************************/
void Java_se_imsys_serial_SerialPortImplementation_notifyOn(int enable, int event, int handle, OBJECTREF this)
{
	NotifyOnEvent(handle, event, enable, this);				
}


/******************************************************************************
 * Java_se_imsys_serial_SerialPortImplementation_setInputBufferSize0
 *
 * Syntax: private native void setInputBufferSize0(int handle,int size) 
 *
 *
 *****************************************************************************/
void Java_se_imsys_serial_SerialPortImplementation_setInputBufferSize0(int size, int handle, OBJECTREF this)
{
	comSetInputBufferSize(handle, size);
}

/******************************************************************************
 * Java_se_imsys_serial_SerialPortImplementation_setOutputBufferSize0
 *
 * Syntax: private native void setOutputBufferSize0(int handle,int size) 
 *
 *
 *****************************************************************************/
void Java_se_imsys_serial_SerialPortImplementation_setOutputBufferSize0(int size, int handle, OBJECTREF this)
{
	comSetOutputBufferSize(handle, size);
}

/******************************************************************************
 * Java_se_imsys_serial_SerialPortImplementation_getPortName
 *
 * Syntax:  private static native String getPortName(int port)
 *
 *
 *****************************************************************************/
OBJECTREF Java_se_imsys_serial_SerialPortImplementation_getPortName(int port)
{
	const char* name;
	OBJECTREF 	string;

	name = comGetPortName(port);
	string = instantiateString(name, strlen(name));
	return string;	
}

/******************************************************************************
 * Java_se_imsys_serial_SerialPortImplementation_sendBreak0
 *
 * Syntax:  private native void sendBreak0(int millis)
 *
 *
 *****************************************************************************/
void Java_se_imsys_serial_SerialPortImplementation_sendBreak0(int millis, int handle, OBJECTREF this)
{
	comSendBreak(handle, millis);
}

/******************************************************************************
 * Java_se_imsys_serial_SerialPortImplementation_setBit
 *
 * Syntax:  private native void setBit(int handle, int bit, boolean value)
 *
 *
 *****************************************************************************/
void Java_se_imsys_serial_SerialPortImplementation_setBit(int value, int bit, int handle, OBJECTREF this)
{
    comSetBit(handle, bit, value);
}

/******************************************************************************
 * Java_se_imsys_serial_SerialPortImplementation_getBit
 *
 * Syntax:  private native boolean getBit(int bit)
 *
 *
 *****************************************************************************/
int Java_se_imsys_serial_SerialPortImplementation_getBit(int bit, int handle, OBJECTREF this)
{
	return comGetBit(handle, bit);
}


/******************************************************************************
 * Java_se_imsys_serial_SerialPortImplementation_enableReceiveTimeout0
 *
 * Syntax:  private native void enableReceiveTimeout0(int rcvTimeout)
 *
 *
 *****************************************************************************/
void Java_se_imsys_serial_SerialPortImplementation_enableReceiveTimeout0(int rcvTimeout, int handle, OBJECTREF this)
{
	comSetReceiveTimeout(handle, rcvTimeout);	
}

/******************************************************************************
 * Java_se_imsys_serial_SerialPortImplementation_enableReceiveThreshold0
 *
 * Syntax:  private native void enableReceiveThreshold0(int handle, int thresh)
 *
 *
 *****************************************************************************/
void Java_se_imsys_serial_SerialPortImplementation_enableReceiveThreshold0(int thresh, int handle, OBJECTREF this)
{
	comSetReceiveThreshold(handle, thresh);	
}

/******************************************************************************
 * Java_se_imsys_serial_SerialPortImplementation_enableReceiveFraming0
 *
 * Syntax:  private native void enableReceiveFraming0(int handle, int framingByte)
 *
 *
 *****************************************************************************/
void Java_se_imsys_serial_SerialPortImplementation_enableReceiveFraming0(int framingByte, int handle, OBJECTREF this)
{
	comSetReceiveFraming(handle, framingByte);	
}
#pragma warn variables on


const NativeImplementationType Java_se_imsys_serial_SerialPortCommDriver_natives[] = 
{
	{"getNumPorts",		(NativeFunctionPtr)Java_se_imsys_serial_SerialPortCommDriver_getNumPorts},
	{"getPortName",		(NativeFunctionPtr)Java_se_imsys_serial_SerialPortCommDriver_getPortName},
	{"findPort",		(NativeFunctionPtr)Java_se_imsys_serial_SerialPortCommDriver_findPort},
    NATIVE_END_OF_LIST
};

const NativeImplementationType Java_se_imsys_serial_SerialPortImplementation_natives[] = 
{
	{"open0",					(NativeFunctionPtr)Java_se_imsys_serial_SerialPortImplementation_open0},
	{"close0",					(NativeFunctionPtr)Java_se_imsys_serial_SerialPortImplementation_close0},
	{"setFlowControlMode0",		(NativeFunctionPtr)Java_se_imsys_serial_SerialPortImplementation_setFlowControlMode0},
	{"setSerialPortParams0",	(NativeFunctionPtr)Java_se_imsys_serial_SerialPortImplementation_setSerialPortParams0},
	{"addEventListener0",		(NativeFunctionPtr)Java_se_imsys_serial_SerialPortImplementation_addEventListener0},
	{"removeEventListener0",	(NativeFunctionPtr)Java_se_imsys_serial_SerialPortImplementation_removeEventListener0},
	{"notifyOn",				(NativeFunctionPtr)Java_se_imsys_serial_SerialPortImplementation_notifyOn},
	{"setInputBufferSize0",		(NativeFunctionPtr)Java_se_imsys_serial_SerialPortImplementation_setInputBufferSize0},
	{"setOutputBufferSize0",	(NativeFunctionPtr)Java_se_imsys_serial_SerialPortImplementation_setOutputBufferSize0},
	{"getPortName",				(NativeFunctionPtr)Java_se_imsys_serial_SerialPortImplementation_getPortName},
	{"sendBreak0",				(NativeFunctionPtr)Java_se_imsys_serial_SerialPortImplementation_sendBreak0},
	{"setBit",					(NativeFunctionPtr)Java_se_imsys_serial_SerialPortImplementation_setBit},
	{"getBit",					(NativeFunctionPtr)Java_se_imsys_serial_SerialPortImplementation_getBit},
	{"enableReceiveTimeout0",	(NativeFunctionPtr)Java_se_imsys_serial_SerialPortImplementation_enableReceiveTimeout0},
	{"enableReceiveThreshold0",	(NativeFunctionPtr)Java_se_imsys_serial_SerialPortImplementation_enableReceiveThreshold0},
	{"enableReceiveFraming0",	(NativeFunctionPtr)Java_se_imsys_serial_SerialPortImplementation_enableReceiveFraming0},
    NATIVE_END_OF_LIST
};

