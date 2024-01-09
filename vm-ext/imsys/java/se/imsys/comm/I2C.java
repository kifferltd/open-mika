package se.imsys.comm;

import se.imsys.system.IllegalAddressException;
		
/**
 * This class implements an I2C bus master interface. The bus supports clock 
 * syncronization to allow slow slaves, but not arbitration to allow multi-master 
 * operation. Two different pin sets can be used for the two bus signals.
 * Neither of these have on-board pull-up resistors, so these must be added 
 * externally if they are not already present on the socket board. 
 * The maximum speed of the bus is around 800 kbit/s. 
 *
 * @author Imsys Technologies AB, Copyright (C) 2007
 */

public class I2C {
	private int slaveAddress;
	private int bitRateUser;
	private int bitRateActual;

	private native int readPort(int slaveAddress, int bitRate, byte barr[], int off, int len);
	private native int writePort(int slaveAddress, int bitRate, byte barr[], int off, int len);
	private native int writereadPort(int slaveAddress, int bitRate, byte wbarr[], int woff, int wlen, byte rbarr[], int roff, int rlen);
	private native void initPort();
	private native int getBitRateActual(int bitRateUser);

/**
 * Creates an I2C object using port B pins 7 and 6 for SCL and SDA, respectively. 
 *
 */
	public I2C()
	{
		initPort();
	    bitRateUser = 0;
	    bitRateActual = 0;
	}

/**
 * Set the address of the slave. Used to set up the standard 7-bit address
 * of the slave device to communicate with. Note that the address should be
 * 7-bit address without appended R/W bit, a common mistake is to use an
 * 8-bit address including R/W bit taken directly from a data sheet.
 *
 * @param address The address of the slave, in the range 0x00-0x7F
 */	
	public void setAddress(byte address)
    {
		slaveAddress = address;
    }

/**
 * Set the 10-bit address of the slave. Used to set up the extended 10-bit address
 * of the slave device to communicate with, for slaves that support such addressing.
 *
 * @param address The address of the slave, in the range 0x000-0x3FF
 */	
	public void setAddress10(int address)
    {
		slaveAddress = (address & 0x3ff) | 0x80000000;
    }

/**
 * Set the desired maximum bus speed. Used to set the communication speed, for slave 
 * devices that do not regulate this themselves using clock synchronization. 
 * The resulting bus speed will be no higher than the set value. 
 *
 * @param bitRate The maximum bit rate in kHz, in the range 10 and up.
 *
 * @see #getBitRate
 */	
	public void setBitRate(int bitRate)
    {
		bitRateUser = bitRate;
	    bitRateActual = getBitRateActual(bitRate);
    }

/**
 * Returns the bit rate. 
 *
 * @return The bit rate in kHz.
 *
 * @see #setBitRate
 */	
	public int getBitRate()
    {
		return(bitRateActual);
    }

/**
 * Read a number of bytes from the I2C bus. Bytes will be read from the slave
 * device addressed by the <TT>setAddress</TT> or <TT>setAddress10</TT> methods.
 *
 * @param barr The byte array to put the data in.
 * @param off The offset into the byte array.
 * @param len The number of bytes to receive.
 * @throws IllegalAddressException This exception is never thrown.
 *
 * @return Number of bytes read, or -1 if no ACK from slave device.
 */	
	public int read(byte barr[], int off, int len) throws IllegalAddressException
    {
        // Bounds check array access (easier here than in native)
	    if ((len < 0) || (off < 0) || ((len + off) > barr.length))
    	   throw new ArrayIndexOutOfBoundsException();

		return(readPort(slaveAddress, bitRateActual, barr, off, len));
    }

/**
 * Write a number of bytes to the I2C bus. Bytes will be written to the slave
 * device addressed by the <TT>setAddress</TT> or <TT>setAddress10</TT> methods.
 *
 * @param barr The byte array to get the data from.
 * @param off The offset into the byte array.
 * @param len The number of bytes to write.
 *
 * @return Number of bytes written, or -1 if no ACK from slave device.
 * @throws IllegalAddressException This exception is never thrown.
 */	
	public int write(byte barr[], int off, int len) throws IllegalAddressException
    {
        // Bounds check array access (easier here than in native)
	    if ((len < 0) || (off < 0) || ((len + off) > barr.length))
    	   throw new ArrayIndexOutOfBoundsException();

		return(writePort(slaveAddress, bitRateActual, barr, off, len));
    }

/**
 * Write and read a number of bytes to/from the I2C bus. Bytes will first be written
 * to the slave device addressed by the <TT>setAddress</TT> or <TT>setAddress10</TT>
 * methods, then bytes will be read from the same slave device.
 *
 * @param wbarr The byte array to get the data to write from.
 * @param woff The offset into the write byte array.
 * @param wlen The number of bytes to write.
 * @param rbarr The byte array to put the read data in.
 * @param roff The offset into the read byte array.
 * @param rlen The number of bytes to receive.
 *
 * @return Number of bytes read, or -1 if no ACK from slave device.
 */	
	public int writeread(byte wbarr[], int woff, int wlen, byte rbarr[], int roff, int rlen)
    {
        // Bounds check array access (easier here than in native)
	    if ((wlen < 0) || (woff < 0) || ((wlen + woff) > wbarr.length) ||
			(rlen < 0) || (roff < 0) || ((rlen + roff) > rbarr.length))
    	   throw new ArrayIndexOutOfBoundsException();

		return(writereadPort(slaveAddress, bitRateActual, wbarr, woff, wlen, rbarr, roff, rlen));
    }
}


