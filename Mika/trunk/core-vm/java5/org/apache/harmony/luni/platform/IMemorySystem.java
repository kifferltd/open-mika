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
 * IMemorySystem
 * 
 */
public interface IMemorySystem {

    /**
     * The constant representing read-only access to data in a memory map
     * request.
     */
    public final int MMAP_READ_ONLY = 1;

    /**
     * The constant representing read-write access to data in a memory map
     * request.
     */
    public final int MMAP_READ_WRITE = 2;

    /**
     * The constant representing copy-on-write access to data in a memory map
     * request.
     */
    public final int MMAP_WRITE_COPY = 4;

    /**
     * Returns true if the platform is little endian, otherwise it may be
     * assumed to be big endian..
     * 
     * @return true if the platform is little endian.
     */
    public boolean isLittleEndian();

    /**
     * Returns the platform pointer size.
     * 
     * @return the native platform pointer size, in bytes.
     */
    public int getPointerSize();

    /**
     * Allocates and returns a pointer to space for a memory block of
     * <code>length</code> bytes. The space is uninitialized and may be larger
     * than the number of bytes requested; however, the guaranteed usable memory
     * block is exactly <code>length</code> bytes long.
     * 
     * @param length
     *            number of bytes requested.
     * @return the address of the start of the memory block.
     * @throws OutOfMemoryError
     *             if the request cannot be satisfied.
     */
    public int malloc(int length) throws OutOfMemoryError;

    /**
     * Deallocates space for a memory block that was previously allocated by a
     * call to {@link #malloc(int) malloc(long)}. The number of bytes freed is
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
    public void free(int address);

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
    public void memset(int address, byte value, long length);

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
    public void memmove(int destAddress, int srcAddress, long length);

    /**
     * Copies <code>length</code> bytes from the memory block at
     * <code>address</code> into the byte array <code>bytes</code> starting
     * at element <code>offset</code> within the byte array.
     * <p>
     * The behavior of this method is undefined if the range
     * <code>(address ... address + length)</code> is not within a memory
     * block that was allocated using {@link #malloc(int) malloc(long)}.
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
    public void getByteArray(int address, byte[] bytes, int offset, int length)
            throws NullPointerException, IndexOutOfBoundsException;

    /**
     * Copies <code>length</code> bytes from the byte array <code>bytes</code>
     * into the memory block at <code>address</code>, starting at element
     * <code>offset</code> within the byte array.
     * <p>
     * The behavior of this method is undefined if the range
     * <code>(address ... address + length)</code> is not within a memory
     * block that was allocated using {@link #malloc(int) malloc(long)}.
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
    public void setByteArray(int address, byte[] bytes, int offset, int length)
            throws NullPointerException, IndexOutOfBoundsException;
    
    // BEGIN android-added
    /**
     * Copies <code>length</code> shorts from the short array <code>short</code>
     * into the memory block at <code>address</code>, starting at element
     * <code>offset</code> within the short array.
     * <p>
     * The behavior of this method is undefined if the range
     * <code>(address ... address + length*2)</code> is not within a memory
     * block that was allocated using {@link #malloc(int) malloc(long)}.
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
     *             if <code>shorts</code> is <code>null</code>.
     * @throws IndexOutOfBoundsException
     *             if <code>offset + length > shorts.length</code>.
     */
    public void setShortArray(int address, short[] shorts, int offset,
            int length, boolean swap)
            throws NullPointerException, IndexOutOfBoundsException;

    /**
     * Copies <code>length</code> ints from the int array <code>ints</code>
     * into the memory block at <code>address</code>, starting at element
     * <code>offset</code> within the int array.
     * <p>
     * The behavior of this method is undefined if the range
     * <code>(address ... address + length*4)</code> is not within a memory
     * block that was allocated using {@link #malloc(int) malloc(long)}.
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
     *             if <code>ints</code> is <code>null</code>.
     * @throws IndexOutOfBoundsException
     *             if <code>offset + length > ints.length</code>.
     */
    public void setIntArray(int address, int[] ints, int offset, int length,
            boolean swap)
            throws NullPointerException, IndexOutOfBoundsException;
    // END android-added

    /**
     * Returns the value of a single byte at the given address.
     * <p>
     * The behavior is unspecified if <code>address</code> is not in the range
     * that was previously allocated using <code>malloc()</code>.
     * </p>
     *
     * @param address
     *            the address at which to get the byte value.
     * @return the value of the byte.
     */
    public byte getByte(int address);

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
    public void setByte(int address, byte value);

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
    public short getShort(int address);

    /**
     * Gets the value of the signed two-byte integer stored in the given byte
     * order at the given address.
     * <p>
     * The behavior is unspecified if <code>(address ... address + 2)</code> is
     * not wholly within the range that was previously allocated using
     * <code>malloc()</code>.
     * </p>
     *
     * @param address
     *            the platform address of the start of the two-byte value.
     * @param endianness
     *            the required interpretation of the short endianness.
     * @return the value of the two-byte integer as a Java <code>short</code>.
     */
    public short getShort(int address, Endianness endianness);

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
    public void setShort(int address, short value);

    public void setShort(int address, short value, Endianness endianness);

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
    public int getInt(int address);

    public int getInt(int address, Endianness endianness);

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
    public void setInt(int address, int value);

    public void setInt(int address, int value, Endianness endianness);

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
    public long getLong(int address);

    public long getLong(int address, Endianness endianness);

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
    public void setLong(int address, long value);

    public void setLong(int address, long value, Endianness endianness);

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
    public float getFloat(int address);

    public float getFloat(int address, Endianness endianness);

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
    public void setFloat(int address, float value);

    public void setFloat(int address, float value, Endianness endianness);

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
    public double getDouble(int address);

    public double getDouble(int address, Endianness endianness);

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
    public void setDouble(int address, double value);

    public void setDouble(int address, double value, Endianness endianness);

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
     * @return the value of the platform pointer as a Java <code>long</code>.
     */
    public int getAddress(int address);

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
     *            the value of the platform pointer as a Java <code>long</code>.
     */
    public void setAddress(int address, int value);

    /**
     * Map file content into memory.
     *
     * @param fileDescriptor
     *            a handle to the file that is to be memory mapped.
     * @param alignment
     *            the offset in the file where the mapping should begin.
     * @param size
     *            the number of bytes that are requested to map.
     * @param mapMode
     *            the desired access mode as defined by one of the constants
     *            {@link IMemorySystem#MMAP_READ_ONLY},
     *            {@link IMemorySystem#MMAP_READ_WRITE},
     *            {@link IMemorySystem#MMAP_WRITE_COPY}
     * @return the start address of the mapped memory area.
     * @throws IOException
     *             if an exception occurs mapping the file into memory.
     */
    public int mmap(int fileDescriptor, long alignment, long size,  int mapMode)
            throws IOException;

    /**
     * TODO: JavaDoc
     * 
     * @param addr
     * @throws IOException
     */
    public void unmap(int addr, long size);

    /**
     * TODO: JavaDoc
     */
    public void load(int addr, long size);

    /**
     * TODO: JavaDoc
     */
    public boolean isLoaded(int addr, long size);

    /**
     * TODO : JavaDoc
     */
    public void flush(int addr, long size);

}
