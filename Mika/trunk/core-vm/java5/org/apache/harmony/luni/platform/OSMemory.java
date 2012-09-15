/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

// BEGIN android-note
// address length was changed from long to int for performance reasons.
// END android-note

package org.apache.harmony.luni.platform;

import java.io.IOException;


/**
 * This class enables direct access to OS memory.
 * <p>
 * Methods that take OS addresses define such parameters as a Java
 * <code>int</code>. The <code>int</code> value is interpreted based on
 * the underlying platform pointer size, such that only the lowest significant
 * <code>POINTER_SIZE</code> bytes of the <code>long</code> value are used.
 * In practice this means that methods on 64-bit platforms use the full eight
 * bytes of the address parameter, and on 32-bit platforms the same methods are
 * truncated to use only the low four bytes.
 * </p>
 * <p>
 * Methods that return OS addresses define the return type to be a Java
 * <code>long</code>. If the platform pointer size is less than eight bytes
 * the OS address value is zero-extended to an eight-byte int to correspond to
 * the subsequent interpretation of that jlong as an OS address as defined
 * above.
 * </p>
 */
final class OSMemory implements IMemorySystem {

    /**
     * Defines the size, in bytes, of a native pointer type for the underlying
     * platform. This will be 4 (for 32-bit machines) or 8 (for 64-bit
     * machines).
     */
    public static final int POINTER_SIZE;

    /**
     * Defines the natural byte order for this machine.
     */
    public static final Endianness NATIVE_ORDER;

    private static final OSMemory singleton = new OSMemory();

    static {
        POINTER_SIZE = getPointerSizeImpl();

        if (isLittleEndianImpl()) {
            NATIVE_ORDER = Endianness.LITTLE_ENDIAN;
        } else {
            NATIVE_ORDER = Endianness.BIG_ENDIAN;
        }
    }

    public static OSMemory getOSMemory() {
        return singleton;
    }

    /**
     * This class is not designed to be publicly instantiated.
     *
     * @see #getOSMemory()
     */
    private OSMemory() {
        super();
    }

    /**
     * Returns whether the byte order of this machine is little endian or not..
     *
	 * @return <code>false</code> for Big Endian, and
	 *         <code>true</code. for Little Endian.
     */
    // BEGIN android-changed
    /*public*/
    private static native boolean isLittleEndianImpl();
    // END android-changed

    public boolean isLittleEndian() {
        return isLittleEndianImpl();
    }

	/**
	 * Returns the natural byte order for this machine.
	 *
	 * @return the native byte order for the current platform.
	 */
	public Endianness getNativeOrder() {
		return NATIVE_ORDER;
	}

	/**
	 * Returns the size of a native pointer type for the underlying platform.
	 *
	 * @return the size of a pointer, in bytes.
	 */
	private static native int getPointerSizeImpl();

	public int getPointerSize() {
		return POINTER_SIZE;
	}

    /**
     * Allocates and returns a pointer to space for a memory block of
     * <code>length</code> bytes. The space is uninitialized and may be larger
     * than the number of bytes requested; however, the guaranteed usable memory
     * block is exactly <code>length</code> bytes int.
     *
	 * @param length
	 *            number of bytes requested.
     * @return the address of the start of the memory block.
	 * @throws OutOfMemoryError
	 *             if the request cannot be satisfied.
     */
    // BEGIN android-changed
    // public long malloc(long length) throws OutOfMemoryError
    // {
    //     OSResourcesMonitor.ensurePhysicalMemoryCapacity();
    //     return mallocNative(length);
    // }
    // private native long mallocNative(long length) throws OutOfMemoryError;
    public native int malloc(int length) throws OutOfMemoryError;
    // END android-changed

    /**
     * Deallocates space for a memory block that was previously allocated by a
     * call to {@link #malloc(int) malloc(int)}. The number of bytes freed is
     * identical to the number of bytes acquired when the memory block was
	 * allocated. If <code>address</code> is zero the method does nothing.
	 * <p>
     * Freeing a pointer to a memory block that was not allocated by
	 * <code>malloc()</code> has unspecified effect.
	 * </p>
     *
	 * @param address
	 *            the address of the memory block to deallocate.
     */
    public native void free(int address);

    /**
     * Places <code>value</code> into first <code>length</code> bytes of the
	 * memory block starting at <code>address</code>.
	 * <p>
	 * The behavior is unspecified if
	 * <code>(address ... address + length)</code> is not wholly within the
	 * range that was previously allocated using <code>malloc()</code>.
	 * </p>
     *
	 * @param address
	 *            the address of the first memory location.
	 * @param value
	 *            the byte value to set at each location.
	 * @param length
	 *            the number of byte-length locations to set.
     */
    public native void memset(int address, byte value, long length);

    /**
     * Copies <code>length</code> bytes from <code>srcAddress</code> to
	 * <code>destAddress</code>. Where any part of the source memory block
	 * and the destination memory block overlap <code>memmove()</code> ensures
	 * that the original source bytes in the overlapping region are copied
	 * before being overwritten.
	 * <p>
	 * The behavior is unspecified if
	 * <code>(srcAddress ... srcAddress + length)</code> and
	 * <code>(destAddress ... destAddress + length)</code> are not both wholly
	 * within the range that was previously allocated using
	 * <code>malloc()</code>.
	 * </p>
     *
	 * @param destAddress
	 *            the address of the destination memory block.
	 * @param srcAddress
	 *            the address of the source memory block.
	 * @param length
	 *            the number of bytes to move.
     */
    public native void memmove(int destAddress, int srcAddress, long length);

    /**
     * Copies <code>length</code> bytes from the memory block at
	 * <code>address</code> into the byte array <code>bytes</code> starting
	 * at element <code>offset</code> within the byte array.
	 * <p>
	 * The behavior of this method is undefined if the range
	 * <code>(address ... address + length)</code> is not within a memory
     * block that was allocated using {@link #malloc(int) malloc(int)}.
	 * </p>
     *
	 * @param address
	 *            the address of the OS memory block from which to copy bytes.
	 * @param bytes
	 *            the byte array into which to copy the bytes.
	 * @param offset
	 *            the index of the first element in <code>bytes</code> that
	 *            will be overwritten.
	 * @param length
	 *            the total number of bytes to copy into the byte array.
	 * @throws NullPointerException
	 *             if <code>bytes</code> is <code>null</code>.
	 * @throws IndexOutOfBoundsException
	 *             if <code>offset + length > bytes.length</code>.
     */
    public native void getByteArray(int address, byte[] bytes, int offset,
            int length) throws NullPointerException, IndexOutOfBoundsException;

    /**
     * Copies <code>length</code> bytes from the byte array <code>bytes</code>
     * into the memory block at <code>address</code>, starting at element
	 * <code>offset</code> within the byte array.
	 * <p>
	 * The behavior of this method is undefined if the range
	 * <code>(address ... address + length)</code> is not within a memory
     * block that was allocated using {@link #malloc(int) malloc(int)}.
	 * </p>
     *
	 * @param address
	 *            the address of the OS memory block into which to copy the
	 *            bytes.
	 * @param bytes
	 *            the byte array from which to copy the bytes.
	 * @param offset
	 *            the index of the first element in <code>bytes</code> that
	 *            will be read.
	 * @param length
	 *            the total number of bytes to copy from <code>bytes</code>
	 *            into the memory block.
	 * @throws NullPointerException
	 *             if <code>bytes</code> is <code>null</code>.
	 * @throws IndexOutOfBoundsException
	 *             if <code>offset + length > bytes.length</code>.
     */
    public native void setByteArray(int address, byte[] bytes, int offset,
            int length) throws NullPointerException, IndexOutOfBoundsException;

    // BEGIN android-added
    /**
     * Copies <code>length</code> shorts from the short array <code>shorts</code>
     * into the memory block at <code>address</code>, starting at element
     * <code>offset</code> within the short array.
     * <p>
     * The behavior of this method is undefined if the range
     * <code>(address ... address + 2*length)</code> is not within a memory
     * block that was allocated using {@link #malloc(int) malloc(int)}.
     * </p>
     *
     * @param address
     *            the address of the OS memory block into which to copy the
     *            shorts.
     * @param shorts
     *            the short array from which to copy the shorts.
     * @param offset
     *            the index of the first element in <code>shorts</code> that
     *            will be read.
     * @param length
     *            the total number of shorts to copy from <code>shorts</code>
     *            into the memory block.
     * @param swap
     *            true if the shorts should be written in reverse byte order.
     * @throws NullPointerException
     *             if <code>bytes</code> is <code>null</code>.
     * @throws IndexOutOfBoundsException
     *             if <code>offset + length > bytes.length</code>.
     */
    public native void setShortArray(int address, short[] shorts, int offset,
            int length, boolean swap) throws NullPointerException,
            IndexOutOfBoundsException;

    /**
     * Copies <code>length</code> ints from the int array <code>ints</code>
     * into the memory block at <code>address</code>, starting at element
     * <code>offset</code> within the int array.
     * <p>
     * The behavior of this method is undefined if the range
     * <code>(address ... address + 2*length)</code> is not within a memory
     * block that was allocated using {@link #malloc(int) malloc(int)}.
     * </p>
     *
     * @param address
     *            the address of the OS memory block into which to copy the
     *            ints.
     * @param ints
     *            the int array from which to copy the ints.
     * @param offset
     *            the index of the first element in <code>ints</code> that
     *            will be read.
     * @param length
     *            the total number of ints to copy from <code>ints</code>
     *            into the memory block.
     * @param swap
     *            true if the ints should be written in reverse byte order.
     * @throws NullPointerException
     *             if <code>bytes</code> is <code>null</code>.
     * @throws IndexOutOfBoundsException
     *             if <code>offset + length > bytes.length</code>.
     */
    public native void setIntArray(int address, int[] ints, int offset,
            int length, boolean swap) throws NullPointerException,
            IndexOutOfBoundsException;
    // END android-added

    // Primitive get & set methods

    /**
	 * Gets the value of the single byte at the given address.
	 * <p>
	 * The behavior is unspecified if <code>address</code> is not in the range
	 * that was previously allocated using <code>malloc()</code>.
	 * </p>
     *
	 * @param address
	 *            the platform address of the byte.
     * @return the byte value.
     */
    public native byte getByte(int address);

    /**
	 * Sets the given single byte value at the given address.
	 * <p>
	 * The behavior is unspecified if <code>address</code> is not in the range
	 * that was previously allocated using <code>malloc()</code>.
	 * </p>
     *
	 * @param address
	 *            the address at which to set the byte value.
	 * @param value
	 *            the value to set.
     */
    public native void setByte(int address, byte value);

    /**
     * Gets the value of the signed two-byte integer stored in platform byte
	 * order at the given address.
	 * <p>
	 * The behavior is unspecified if <code>(address ... address + 2)</code>
	 * is not wholly within the range that was previously allocated using
	 * <code>malloc()</code>.
	 * </p>
     *
	 * @param address
	 *            the platform address of the start of the two-byte value.
     * @return the value of the two-byte integer as a Java <code>short</code>.
     */
    public native short getShort(int address);

    public short getShort(int address, Endianness endianness) {
        return (endianness == NATIVE_ORDER) ? getShort(address)
                : swap(getShort(address));
    }

    /**
     * Sets the value of the signed two-byte integer at the given address in
	 * platform byte order.
	 * <p>
	 * The behavior is unspecified if <code>(address ... address + 2)</code>
	 * is not wholly within the range that was previously allocated using
	 * <code>malloc()</code>.
	 * </p>
     *
	 * @param address
	 *            the platform address of the start of the two-byte value.
	 * @param value
	 *            the value of the two-byte integer as a Java <code>short</code>.
     */
    public native void setShort(int address, short value);

    public void setShort(int address, short value, Endianness endianness) {
        if (endianness == NATIVE_ORDER) {
            setShort(address, value);
        } else {
            setShort(address, swap(value));
        }
    }

    /**
     * Gets the value of the signed four-byte integer stored in platform
	 * byte-order at the given address.
	 * <p>
	 * The behavior is unspecified if <code>(address ... address + 4)</code>
	 * is not wholly within the range that was previously allocated using
	 * <code>malloc()</code>.
	 * </p>
     *
	 * @param address
	 *            the platform address of the start of the four-byte value.
     * @return the value of the four-byte integer as a Java <code>int</code>.
     */
    public native int getInt(int address);

    public int getInt(int address, Endianness endianness) {
        return (endianness == NATIVE_ORDER) ? getInt(address)
                : swap(getInt(address));
    }

    /**
     * Sets the value of the signed four-byte integer at the given address in
	 * platform byte order.
	 * <p>
	 * The behavior is unspecified if <code>(address ... address + 4)</code>
	 * is not wholly within the range that was previously allocated using
	 * <code>malloc()</code>.
	 * </p>
     *
	 * @param address
	 *            the platform address of the start of the four-byte value.
	 * @param value
	 *            the value of the four-byte integer as a Java <code>int</code>.
     */
    public native void setInt(int address, int value);

    public void setInt(int address, int value, Endianness endianness) {
        if (endianness == NATIVE_ORDER) {
            setInt(address, value);
        } else {
            setInt(address, swap(value));
        }
    }

    /**
     * Gets the value of the signed eight-byte integer stored in platform byte
	 * order at the given address.
	 * <p>
	 * The behavior is unspecified if <code>(address ... address + 8)</code>
	 * is not wholly within the range that was previously allocated using
	 * <code>malloc()</code>.
	 * </p>
     *
	 * @param address
	 *            the platform address of the start of the eight-byte value.
     * @return the value of the eight-byte integer as a Java <code>long</code>.
     */
    public native long getLong(int address);

    public long getLong(int address, Endianness endianness) {
        return (endianness == NATIVE_ORDER) ? getLong(address)
                : swap(getLong(address));
    }

    /**
     * Sets the value of the signed eight-byte integer at the given address in
	 * the platform byte order.
	 * <p>
	 * The behavior is unspecified if <code>(address ... address + 8)</code>
	 * is not wholly within the range that was previously allocated using
	 * <code>malloc()</code>.
	 * </p>
     *
	 * @param address
	 *            the platform address of the start of the eight-byte value.
	 * @param value
	 *            the value of the eight-byte integer as a Java
     *            <code>long</code>.
     */
    public native void setLong(int address, long value);

    public void setLong(int address, long value, Endianness endianness) {
        if (endianness == NATIVE_ORDER) {
            setLong(address, value);
        } else {
            setLong(address, swap(value));
        }
    }

    /**
     * Gets the value of the IEEE754-format four-byte float stored in platform
	 * byte order at the given address.
	 * <p>
	 * The behavior is unspecified if <code>(address ... address + 4)</code>
	 * is not wholly within the range that was previously allocated using
	 * <code>malloc()</code>.
	 * </p>
     *
	 * @param address
	 *            the platform address of the start of the eight-byte value.
     * @return the value of the four-byte float as a Java <code>float</code>.
     */
    public native float getFloat(int address);

    public float getFloat(int address, Endianness endianness) {
        if (endianness == NATIVE_ORDER) {
            return getFloat(address);
        }
        int floatBits = swap(getInt(address));
        return Float.intBitsToFloat(floatBits);
    }

    /**
     * Sets the value of the IEEE754-format four-byte float stored in platform
	 * byte order at the given address.
	 * <p>
	 * The behavior is unspecified if <code>(address ... address + 4)</code>
	 * is not wholly within the range that was previously allocated using
	 * <code>malloc()</code>.
	 * </p>
     *
	 * @param address
	 *            the platform address of the start of the eight-byte value.
	 * @param value
	 *            the value of the four-byte float as a Java <code>float</code>.
     */
    public native void setFloat(int address, float value);

    public void setFloat(int address, float value, Endianness endianness) {
        if (endianness == NATIVE_ORDER) {
            setFloat(address, value);
        } else {
            int floatBits = Float.floatToIntBits(value);
            setInt(address, swap(floatBits));
        }
    }

    /**
     * Gets the value of the IEEE754-format eight-byte float stored in platform
	 * byte order at the given address.
	 * <p>
	 * The behavior is unspecified if <code>(address ... address + 8)</code>
	 * is not wholly within the range that was previously allocated using
	 * <code>malloc()</code>.
	 * </p>
     *
	 * @param address
	 *            the platform address of the start of the eight-byte value.
     * @return the value of the eight-byte float as a Java <code>double</code>.
     */
    public native double getDouble(int address);

    public double getDouble(int address, Endianness endianness) {
        if (endianness == NATIVE_ORDER) {
            return getDouble(address);
        }
        long doubleBits = swap(getLong(address));
        return Double.longBitsToDouble(doubleBits);
    }

    /**
     * Sets the value of the IEEE754-format eight-byte float store in platform
	 * byte order at the given address.
	 * <p>
	 * The behavior is unspecified if <code>(address ... address + 8)</code>
	 * is not wholly within the range that was previously allocated using
	 * <code>malloc()</code>.
	 * </p>
     *
	 * @param address
	 *            the platform address of the start of the eight-byte value.
	 * @param value
	 *            the value of the eight-byte float as a Java
     *            <code>double</code>.
     */
    public native void setDouble(int address, double value);

    public void setDouble(int address, double value, Endianness endianness) {
        if (endianness == NATIVE_ORDER) {
            setDouble(address, value);
        } else {
            long doubleBits = Double.doubleToLongBits(value);
            setLong(address, swap(doubleBits));
        }
    }

    /**
	 * Gets the value of the platform pointer at the given address.
	 * <p>
	 * The length of the platform pointer is defined by
	 * <code>POINTER_SIZE</code>.
	 * </p>
	 * The behavior is unspecified if
	 * <code>(address ... address + POINTER_SIZE)</code> is not wholly within
	 * the range that was previously allocated using <code>malloc()</code>.
	 * </p>
     *
	 * @param address
	 *            the platform address of the start of the platform pointer.
     * @return the value of the platform pointer as a Java <code>int</code>.
     */
    public native int getAddress(int address);

    /**
	 * Sets the value of the platform pointer at the given address.
	 * <p>
	 * The length of the platform pointer is defined by
	 * <code>POINTER_SIZE</code>. This method only sets
	 * <code>POINTER_SIZE</code> bytes at the given address.
	 * </p>
	 * The behavior is unspecified if
	 * <code>(address ... address + POINTER_SIZE)</code> is not wholly within
	 * the range that was previously allocated using <code>malloc()</code>.
	 * </p>
     *
	 * @param address
	 *            the platform address of the start of the platform pointer.
	 * @param value
     *            the value of the platform pointer as a Java <code>int</code>.
     */
    public native void setAddress(int address, int value);

    /*
         * Memory mapped file
         */
    private native int mmapImpl(int fileDescriptor, long alignment,
            long size, int mapMode);

    public int mmap(int fileDescriptor, long alignment, long size,
            int mapMode) throws IOException {
        int address = mmapImpl(fileDescriptor, alignment, size, mapMode);
        if (address == -1) {
            throw new IOException();
        }
        return address;
    }

    private native void unmapImpl(int addr, long size);

    public void unmap(int addr, long size) {
        unmapImpl(addr, size);
    }

    public void load(int addr, long size) {
        loadImpl(addr, size);
    }

    private native int loadImpl(int l, long size);

    public boolean isLoaded(int addr, long size) {
		return size == 0 ? true : isLoadedImpl(addr, size);
	}

    private native boolean isLoadedImpl(int l, long size);

    public void flush(int addr, long size) {
		flushImpl(addr, size);
	}

    private native int flushImpl(int l, long size);

	/*
	 * Helper methods to change byte order.
	 */
	private short swap(short value) {
        // BEGIN android-note
        // is Integer.reverseBytes() >>> 16 be faster?
        // END android-note
		int topEnd = value << 8;
		int btmEnd = (value >> 8) & 0xFF;
		return (short) (topEnd | btmEnd);
	}

	private int swap(int value) {
        // BEGIN android-note
        // is Integer.reverseBytes() be faster?
        // END android-note
		short left = (short) (value >> 16);
		short right = (short) value;
		int topEnd = swap(right) << 16;
		int btmEnd = swap(left) & 0xFFFF;
		return topEnd | btmEnd;
	}

	private long swap(long value) {
        // BEGIN android-note
        // is Long.reverseBytes() be faster?
        // END android-note
		int left = (int) (value >> 32);
		int right = (int) value;
		long topEnd = ((long) swap(right)) << 32;
		long btmEnd = swap(left) & 0xFFFFFFFFL;
		return topEnd | btmEnd;
	}
}
