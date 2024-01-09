/* 
 * nativeSPI.c
 *
 * Copyright (C) 2003 Imsys AB. All rights reserved
 *
 * Implementation of native methods in class SPI.
 * 
 */

#include <global.h>
#include "spi.h"


#define MAXBUF		128


/******************************************************************************************
 * Java_se_imsys_comm_SPI_initSPI
 *
 * native implementation of method:	 
 * 
 * 	private static native void initSPI();
 *
 */
void Java_se_imsys_comm_SPI_initSPI()
{
   	spiInit();
}

#pragma warn variables off
/******************************************************************************************
 * Java_se_imsys_comm_SPI_setPolarity0
 *
 * native implementation of method:	 
 * 
 * 	private native int setPolarity0(int mode, boolean CPOL)
 *
 */
int Java_se_imsys_comm_SPI_setPolarity0(int CPOL, int mode, OBJECTREF this)
{
	spiSetPolarity((unsigned int*)&mode, CPOL);
    return mode;
}
#pragma warn variables on


#pragma warn variables off
/******************************************************************************************
 * Java_se_imsys_comm_SPI_setPhase0
 *
 * native implementation of method:	 
 * 
 * 	private native int setPhase0(int mode, boolean CPHA)
 *
 */
int Java_se_imsys_comm_SPI_setPhase0(int CPHA, int mode, OBJECTREF this)
{
	spiSetPhase((unsigned int*)&mode, CPHA);
    return mode;
}
#pragma warn variables on


#pragma warn variables off
/******************************************************************************************
 * Java_se_imsys_comm_SPI_setBitOrder0
 *
 * native implementation of method:	 
 * 
 * 	private native int setBitOrder0(int mode, boolean bitOrder)
 *
 */
int Java_se_imsys_comm_SPI_setBitOrder0(int bitOrder, int mode, OBJECTREF this)
{
	spiSetBitOrder((unsigned int*)&mode, bitOrder);
    return mode;
}
#pragma warn variables on



#pragma warn variables off
/******************************************************************************************
 * Java_se_imsys_comm_SPI_setSlaveSelect0
 *
 * native implementation of method:	 
 * 
 * 		private native int setSlaveSelect0(int mode, int port, int bitMask, boolean use_ss, boolean pol_ss);
 *
 */
int Java_se_imsys_comm_SPI_setSlaveSelect0(int pol_ss, int use_ss, int bitMask, int port, int mode, OBJECTREF this)
{
	spiSetSlaveSelect((unsigned int*)&mode, port, bitMask, use_ss, pol_ss);
    return mode;
}
#pragma warn variables on


#pragma warn variables off
/******************************************************************************************
 * Java_se_imsys_comm_SPI_getBitRateActual
 *
 * native implementation of method:	 
 * 
 * 	private native int getBitRate(int bitRate)
 *
 */
int Java_se_imsys_comm_SPI_getBitRateActual(int bitRate, OBJECTREF this)
{
	int delay;

	spiSetBitRate(&delay, bitRate);
    return delay;
}
#pragma warn variables on




#pragma warn variables off
/******************************************************************************************
 * Java_se_imsys_comm_SPI_xmitSPI
 *
 * native implementation of method:	 
 * 
 * 	private native int xmitSPI(int mode, byte[] ba, int off, int len, int bitRate)
 *
 */

int Java_se_imsys_comm_SPI_xmitSPI(int bitRate, int len, int off, OBJECTREF ba, int mode, OBJECTREF this)
{
	int 	res;
    ARRAY 	array;
    unsigned char buf[MAXBUF], *pBuf;

	if (len > MAXBUF) 
    	pBuf = (unsigned char*)malloc(len);
    else
    	pBuf = buf;

	// Copy to buffer
	pthread_mutex_lock(&gc_Mutex);
	array = ARRAYPOINTER(ba);	
	memmove(pBuf, &((BYTEARRAY)array)->bdata[off], len);
	pthread_mutex_unlock(&gc_Mutex);
		
	// Write/Read
	res = spiXmit(pBuf, len, bitRate, mode);

	// Copy from buffer
	pthread_mutex_lock(&gc_Mutex);
	array = ARRAYPOINTER(ba);	
	memmove(&((BYTEARRAY)array)->bdata[off], pBuf, len);
	pthread_mutex_unlock(&gc_Mutex);

	if (len > MAXBUF)
    	free(pBuf);
	
	return res;
}
#pragma warn variables on


const NativeImplementationType Java_se_imsys_comm_SPI_natives[] = 
{ 
	{"initSPI",					(NativeFunctionPtr)Java_se_imsys_comm_SPI_initSPI},
	{"xmitSPI",					(NativeFunctionPtr)Java_se_imsys_comm_SPI_xmitSPI},
	{"setPolarity0",			(NativeFunctionPtr)Java_se_imsys_comm_SPI_setPolarity0},
	{"setPhase0",				(NativeFunctionPtr)Java_se_imsys_comm_SPI_setPhase0},
	{"setBitOrder0",			(NativeFunctionPtr)Java_se_imsys_comm_SPI_setBitOrder0},
	{"setSlaveSelect0",			(NativeFunctionPtr)Java_se_imsys_comm_SPI_setSlaveSelect0},
	{"getBitRateActual",		(NativeFunctionPtr)Java_se_imsys_comm_SPI_getBitRateActual},
	NATIVE_END_OF_LIST
};



