package se.imsys.system;

/** 
 * This class gives access to the general purpose I/O ports A-J on the processor.
 * Access is also provided to the internal ports K-P.
 *
 * <P>
 * Every I/O port consists of one control register and one data register. 
 * The control register defines what pins are input and which pins that are 
 * output. The data register contains the data to be written or read.
 * The internal ports, not being connected to any external pins, can have
 * entirely different functions for control and data registers.
 *
 * <P>
 * In the control register a '1' (set) in a specific position means that this 
 * bit will be used for output, while a '0' (cleared) indicates an input.
 *
 * @author Imsys Technologies AB, Copyright (C) 2007
 */


public class PortIO {

	public static final int PORT_A		= 0;
	public static final int PORT_B		= 1;
	public static final int PORT_C		= 2;
	public static final int PORT_D		= 3;
	public static final int PORT_E		= 4;
	public static final int PORT_H		= 7;
	public static final int PORT_I		= 8;
	public static final int PORT_J		= 9;
	public static final int PORT_K		= 10;
	public static final int PORT_L		= 11;
	public static final int PORT_M		= 12;
	public static final int PORT_N		= 13;
	public static final int PORT_O		= 14;
	public static final int PORT_P		= 15;

	private static final int CONTROL_REG	= 0;
	private static final int DATA_REG	= 1;

	private int portname = 0;

	public PortIO(int portname) {
    	this.portname = portname;
    }

	private native void setBit(int reg, int portname, int bitNumber, int value);
	private native int getBit(int reg, int portname, int bitNumber);

	private native void setByte(int reg, int portname, int value);
	private native byte getByte(int reg, int portname);

	/** 
	 * Sets a specified bit in the control register. Setting a bit in
	 * the control register means that the bit will be used for output.
	 * <P>
	 * Valid values are in the range 0 to 7.
	 */
	public void setControlBit(int bitNumber) {
		setBit(CONTROL_REG, portname, bitNumber, 1);
	}

	/** 
	 * Clears a specified bit in the control register. Clearing a bit in
	 * the control register means that the bit will be used for input.
	 * <P>
	 * Valid values are in the range 0 to 7.
	 */
	public void clearControlBit(int bitNumber) {
		setBit(CONTROL_REG, portname, bitNumber, 0);
	}

	/** 
	 * Reads a specified bit in the control register. Get the state of
	 * a specific bit in the control register.
	 * <P>
	 * Valid values are in the range 0 to 7.
	 */
	public int getControlBit(int bitNumber) {
		return getBit(CONTROL_REG, portname, bitNumber);
	}

	/** 
	 * Sets a specified bit in the data register. Setting a bit in
	 * the data register means that the output of that pin will be logic
	 * high. The output on the actual pin will only occur if the 
	 * corresponding bit in the control register is set to '1', else the
	 * output won't be affected.
	 * <P>
	 * Valid values are in the range 0 to 7.
	 */
	public void setDataBit(int bitNumber) {
		setBit(DATA_REG, portname, bitNumber, 1);
	}

	/** 
	 * Clears a specified bit in the data register. Clearing a bit in
	 * the data register means that the output of that pin will be logic
	 * low. The output on the actual pin will only occur if the 
	 * corresponding bit in the control register is set to '1', else the
	 * output won't be affected.
	 * <P>
	 * Valid values are in the range 0 to 7.
	 */
	public void clearDataBit(int bitNumber) {
		setBit(DATA_REG, portname, bitNumber, 0);
	}

	/** 
	 * Reads a specified bit in the data register. If the corresponding bit
	 * in the control register is '0' (input) the state of the pin will be
	 * read, else the result is undefined.
	 * <P>
	 * Valid values are in the range 0 to 7.
	 */
	public int getDataBit(int bitNumber) {
		return getBit(DATA_REG, portname, bitNumber);
	}

	/** 
	 *  Writes a byte to the control register. A value of '1' in a specific
	 *  position will setup the correspinding pin as an output, while a '0'
	 *  will make the pin an input.
	 */
	public void setControlReg(int value) {
		setByte(CONTROL_REG, portname, value);
	}

	/** 
	 *  Reads a byte from the control register. A '1' in a position means
	 *  that the bit is used for output, while a '0' indicates an input.
	 */
	public byte getControlReg() {
		return getByte(CONTROL_REG, portname);
	}

	/** 
	 *  Writes a byte from the data register. Only those bits that are set
	 *  to '1' in the control register will be written, the rest of the bits
	 *  are ignored.
	 */
	public void setDataReg(int value) {
		setByte(DATA_REG, portname, value);
	}

	/** 
	 *  Reads a byte from the data register. Only those bits that are set
	 *  to '0' in the control register contains valid data, the rest of
	 *  the bits are undefined.
	 */
	public byte getDataReg() {
		return getByte(DATA_REG, portname);
	}
}
