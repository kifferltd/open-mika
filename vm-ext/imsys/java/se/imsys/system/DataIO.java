package se.imsys.system;

/**
 * <P> 
 * This class gives low level access to the processor's native I/O expansion bus.
 *
 * <P>
 * The processors native I/O expansion bus is used to connect high speed peripherals. 
 *
 * @author Imsys Technologies AB, Copyright (C) 2007
 */

public class DataIO {
/**
 * The address to read/write to/from.
 */
	private int address = 0;

/**
 * Create a DataIO object that communicates with the specified address.
 *
 * @param address The address to talk to.
 */
	public DataIO(int address) {
		this.address = address;
	}

	private native int read0(int address, int len);
	private native void write0(int address, int data, int len);

/** 
 * Set the active address.
 *
 * @param address The address
 */
	public void setAddress(int address) {
		this.address = address;
	}

/**
 * Read a byte from the specified address.
 *
 * @return The read <TT>byte</TT>.
 */
	public byte readByte() {
		return (byte)read0(address, 1);
	}

/**
 * Read a short from the specified address.
 *
 * @return The read <TT>short</TT>.
 */
	public short readShort() {
		return (short)read0(address, 2);
	}

/**
 * Read a int from the specified address.
 *
 * @return The read <TT>int</TT>.
 */
	public int readInt() {
		return read0(address, 4);
	}

/**
 * Write a byte to the specified address.
 *
 * @param data The byte to write.
 */
	public void write(byte data) {
		write0(address, data, 1);
	}

/**
 * Write a short to the specified address.
 *
 * @param data The short to write.
 */
	public void write(short data) {
		write0(address, data, 2);
	}

/**
 * Write an int to the specified address.
 *
 * @param data The int to write.
 */
	public void write(int data) {
		write0(address, data, 4);
	}
}
