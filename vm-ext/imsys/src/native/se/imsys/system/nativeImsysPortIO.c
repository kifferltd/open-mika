/*******************************************************************************
**
**  @Name:   	nativeImsysPortIO.c
**
**  @Input:	Nada
**	
**  @Output:	Nada
**
**  @Function:	Implementation of the sockets layer for the tcp/ip stack.
**		A good idea is to look at the man pages to understand how
**		it is supposed to work.
**
**		Most of the constants are defined in endpoint.h
**
**  @Author: 	Jan Karlberg, Copyright (C) 2001 Imsys AB
**
**  @Email:	jan@imsys.se
**
**  @www:	www.imsys.se
**
**  @Date: 	20010703
**  @Change:	Created
**
**  @Date: 	20010802
**  @Change:	Rewrote most of it to use the macros defined in IOmacro instead.
**		Added the raiseException() if an illegal port is specified. 
**		Added support for the IO ports that are accessed through the FPGA
**		JA
**
**  @Date: 	20011108
**  @Change:	Made it work
**		AG
*******************************************************************************/
#include <ctype.h>	   
#include <global.h>	   
#include <io_macro.h>

char *illegalPort = "/java/io/IOException";		// Is this a good exception to throw?

/**********************************************************
 * setControlValue
 *********************************************************/
void Java_se_imsys_PortIO_setControlValue(int value,int portName)
{

	switch( tolower( portName ) ) {
    	case 'a':
		OutCA( value );
	    	return;

    	case 'b':
		OutCB( value );
	    	return;

	case 'c':
		OutCC( value );
	    	return;
	}

	raiseException( illegalPort );
}

/**********************************************************
 * setDataValue
 *********************************************************/
void Java_se_imsys_PortIO_setDataValue(int value,int portName)
{
	switch( tolower( portName ) ) {
    	case 'a':
		OutDA( value );
	    	return;

    	case 'b':
		OutDB( value );
	    	return;

    	case 'c':
		OutDC( value );
	    	return;

//    	case 'd':
//		OutOD( value );
//	    	return;
//
//    	case 'e':
//		OutOE( value );
//	    	return;
	}

	raiseException( illegalPort );
}

/**********************************************************
 * getControlValue
 *********************************************************/
int Java_se_imsys_PortIO_getControlValue(int portName)
{
	switch( tolower( portName ) ) {
    	case 'a':
		return( InCA() );

    	case 'b':
		return( InCB() );

    	case 'c':
		return( InCC() );
	}

	raiseException( illegalPort );
        return( 0 );
}

/**********************************************************
 * getDataValue
 *********************************************************/
int Java_se_imsys_PortIO_getDataValue(int portName)
{
	switch( tolower( portName ) ) {
    	case 'a':
		return( InDA() );

    	case 'b':
		return( InDB() );

    	case 'c':
		return( InDC() );
	}

	raiseException( illegalPort );
        return( 0 );
}

/**********************************************************
 * setControlBitValue
 *********************************************************/
void Java_se_imsys_PortIO_setControlBitValue(int newValue,int bitNum,int portName)
{
    char mask;

	mask = 0x01 << bitNum;

	asm("push sw; dis");
	if( newValue ) {
		Java_se_imsys_PortIO_setControlValue(Java_se_imsys_PortIO_getControlValue(portName) | mask, portName);
	} else {
	    mask ^= 0xff;
		Java_se_imsys_PortIO_setControlValue(Java_se_imsys_PortIO_getControlValue(portName) & mask, portName);
	}
	asm("pop sw");
}

/**********************************************************
 * setDataBitValue
 *********************************************************/
void Java_se_imsys_PortIO_setDataBitValue(int newValue,int bitNum,int portName)
{
    char mask;

	mask = 0x01 << bitNum;

	asm("push sw; dis");
	if( newValue ) {
		Java_se_imsys_PortIO_setDataValue(Java_se_imsys_PortIO_getDataValue(portName) | mask, portName);
	} else {
	    mask ^= 0xff;
		Java_se_imsys_PortIO_setDataValue(Java_se_imsys_PortIO_getDataValue(portName) & mask, portName);
	}
	asm("pop sw");
}



/**********************************************************
 * getControlBitValue
 *********************************************************/
int Java_se_imsys_PortIO_getControlBitValue(int bitNum,int portName)
{
	return((Java_se_imsys_PortIO_getControlValue(portName) >> bitNum ) & 0x01);
}


/**********************************************************
 * getDataBitValue
 *********************************************************/
int Java_se_imsys_PortIO_getDataBitValue(int bitNum,int portName)
{
	return((Java_se_imsys_PortIO_getDataValue(portName) >> bitNum ) & 0x01);
}


/**********************************************************
 * writeByte
 *********************************************************/
void Java_se_imsys_PortIO_writeByte(int value, int address)
{
 	OutB(address,value);
}

/**********************************************************
 * writeShort
 *********************************************************/
void Java_se_imsys_PortIO_writeShort(int value, int address)
{												    
 	OutS(address,value);
}

/**********************************************************
 * writeInt
 *********************************************************/
void Java_se_imsys_PortIO_writeInt(int value, int address)
{
 	OutI(address,value);
}

/**********************************************************
 * readByte
 *********************************************************/
int Java_se_imsys_PortIO_readByte(int address)
{
 	return InB(address);
}

/**********************************************************
 * readShort
 *********************************************************/
int Java_se_imsys_PortIO_readShort(int address)
{
 	return InS(address);
}

/**********************************************************
 * readInt
 *********************************************************/
int Java_se_imsys_PortIO_readInt(int address)
{
 	return InI(address);
}


const NativeImplementationType se_imsys_PortIO_natives[] = 
{ 
   {"setControlValue",     	(NativeFunctionPtr)Java_se_imsys_PortIO_setControlValue},	// OK
   {"setDataValue",     	(NativeFunctionPtr)Java_se_imsys_PortIO_setDataValue},		// OK
   {"getControlValue",     	(NativeFunctionPtr)Java_se_imsys_PortIO_getControlValue},    // OK
   {"getDataValue",     	(NativeFunctionPtr)Java_se_imsys_PortIO_getDataValue},	    // OK

   {"setControlBitValue",   (NativeFunctionPtr)Java_se_imsys_PortIO_setControlBitValue},	    // OK
   {"getControlBitValue",   (NativeFunctionPtr)Java_se_imsys_PortIO_getControlBitValue},	    // OK
   {"setDataBitValue",   	(NativeFunctionPtr)Java_se_imsys_PortIO_setDataBitValue},	    // OK
   {"getDataBitValue",   	(NativeFunctionPtr)Java_se_imsys_PortIO_getDataBitValue},	    // OK

   {"writeByte",			(NativeFunctionPtr)Java_se_imsys_PortIO_writeByte},
   {"writeShort",			(NativeFunctionPtr)Java_se_imsys_PortIO_writeShort},
   {"writeInt",				(NativeFunctionPtr)Java_se_imsys_PortIO_writeInt},
   {"readByte",				(NativeFunctionPtr)Java_se_imsys_PortIO_readByte},
   {"readShort",			(NativeFunctionPtr)Java_se_imsys_PortIO_readShort},
   {"readInt",				(NativeFunctionPtr)Java_se_imsys_PortIO_readInt},

    NATIVE_END_OF_LIST
};

