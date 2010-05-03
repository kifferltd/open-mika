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

/*
 * Imported by CG 20081213 based on Apache Harmony ("enhanced") revision 721075.
 * Then hacked like hell to reduce the memory usage and vulnerability to
 * fragmentation so that it has a hope of working with Mika.
 */

package java.io;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * <code>BufferedInputStream</code> is a class which takes an input stream and
 * <em>buffers</em> the input. In this way, costly interaction with the original
 * input stream can be minimized by reading buffered amounts of data
 * infrequently. The drawback is that extra space is required to hold the buffer
 * and that copying takes place when reading that buffer.
 * 
 * @see BufferedOutputStream
 */
public class BufferedInputStream extends FilterInputStream {

    /**
     * Get the total capacity of an ArrayList of byte[].
     */
    private static int capacity(ArrayList bufs) {
      int cap = 0;
      Iterator it = bufs.iterator();
      while (it.hasNext()) {
          byte[] b = (byte[])it.next();
          cap += b.length;
      }

      return cap;
    }

    /**
     * Shift bytes <code>pos .. (count -1)</code> down to the start of 
     * <code>localBufs</code>, i.e. byte<code>[pos]</code> moves to
     * byte<code>[0]</code> etc..
     */
    static void shiftdown(ArrayList localBufs, int bufsiz, int pos, int count) {
        int i0 = 0;
        int i1 = pos / bufsiz;
        int j0 = 0;
        int j1 = pos % bufsiz;
        int k = 0;
        int l;
        int nBufs = localBufs.size();
        byte[] b0 = (byte[])localBufs.get(i0);
        byte[] b1 = (byte[])localBufs.get(i1);
        while (k < count) {
            if (count - k < b1.length - j1  && count - k < b0.length - j0) {
                System.arraycopy(b1, j1, b0, j0, count - k);
                k = count;
                // next two assignments are not really needed
                j0 += count - k;
                j1 = count - k;
            }
            else if (b1.length - j1 < b0.length - j0) {
                System.arraycopy(b1, j1, b0, j0, b1.length - j1);
                k += b1.length - j1;
                j0 += b1.length - j1;
                j1 = 0;
                if (++i1 == nBufs) {
                  break;
                }
                b1 = (byte[])localBufs.get(i1);
            }
            else if (b1.length - j1 > b0.length - j0) {
                System.arraycopy(b1, j1, b0, j0, b0.length - j0);
                k += b0.length - j0;
                j0 = 0;
                j1 += b0.length - j0;
                if (++i0 == nBufs) {
                  break;
                }
                b0 = (byte[])localBufs.get(i0);
            }
            else {
                System.arraycopy(b1, j1, b0, j0, b0.length - j0);
                k += b0.length - j0;
                j0 = 0;
                j1 = 0;
                ++i0;
                ++i1;
                if (i0 == nBufs || i1 == nBufs) {
                  break;
                }
                b0 = (byte[])localBufs.get(i0);
                b1 = (byte[])localBufs.get(i1);
            }
        }
    }

    /**
     * The buffer containing the current bytes read from the target InputStream.
     * We store this as an ArrayList of byte[]; every entry is an array of 
     * length <code>bufsiz</code>, with the possible exception of the last
     * element which may be shorter.
     */
    protected volatile ArrayList bufs;

    /**
     * The number of bytes in each array.
     */
    private int bufsiz;

    /**
     * The total number of bytes inside all byte arrays in <code>bufs</code>.
     */
    protected int count;

    /**
     * The current limit, which when passed, invalidates the current mark.
     */
    protected int marklimit;

    /**
     * The currently marked position. -1 indicates no mark has been set or the
     * mark has been invalidated.
     */
    protected int markpos = -1;

    /**
     * The current position within  <code>bufs</code>. In other words we
     * are at byte[pos % bufsiz] of the (pos / bufsiz)'th array.
     */
    protected int pos;

    /**
     * Constructs a new <code>BufferedInputStream</code> on the InputStream
     * <code>in</code>. The default buffer size (8Kb) is allocated and all reads
     * can now be filtered through this stream.
     * 
     * @param in
     *            the InputStream to buffer reads on.
     */
    public BufferedInputStream(InputStream in) {
        super(in);
        bufsiz = 8192;
        bufs = new ArrayList(1);
        bufs.add(new byte[8192]);
    }

    /**
     * Constructs a new BufferedInputStream on the InputStream <code>in</code>.
     * The buffer size is specified by the parameter <code>size</code> and all
     * reads can now be filtered through this BufferedInputStream.
     * 
     * @param in
     *            the InputStream to buffer reads on.
     * @param size
     *            the size of buffer to allocate.
     */
    public BufferedInputStream(InputStream in, int size) {
        super(in);
        if (size <= 0) {
            throw new IllegalArgumentException("size must be > 0");
        }
        bufsiz = size;
        bufs = new ArrayList(1);
        bufs.add(new byte[size]);
    }

    /**
     * Answers an int representing the number of bytes that are available before
     * this BufferedInputStream will block. This method returns the number of
     * bytes available in the buffer plus those available in the target stream.
     * 
     * @return the number of bytes available before blocking.
     * 
     * @throws IOException
     *             If an error occurs in this stream.
     */
    public synchronized int available() throws IOException {
        InputStream localIn = in; // 'in' could be invalidated by close()
        if (bufs == null || localIn == null) {
            throw new IOException("stream is closed");
        }
        return count - pos + localIn.available();
    }

    /**
     * Close this BufferedInputStream. This implementation closes the target
     * stream and releases any resources associated with it.
     * 
     * @throws IOException
     *             If an error occurs attempting to close this stream.
     */
    public void close() throws IOException {
        bufs = null;
        InputStream localIn = in;
        in = null;
        if (localIn != null) {
            localIn.close();
        }
    }

    private int fillbuf(InputStream localIn, ArrayList localBufs)
            throws IOException {
        if (markpos == -1 || (pos - markpos >= marklimit)) {
            /* Mark position not set or exceeded readlimit */
            Iterator it = localBufs.iterator();
            int result = 0;
            while (it.hasNext()) {
              byte[] b = (byte[])it.next();
              int l = localIn.read(b);
              if (l > 0) {
                  result += l;
              }
              if (l < 0 && result == 0) {
                  result = l;
              }
              if (l < b.length) {
                break;
              }
            }
            if (result > 0) {
                markpos = -1;
                pos = 0;
                count = result == -1 ? 0 : result;
            }
            return  result;
        }

        // Mark is set and is valid.
        int currentCapacity = capacity(localBufs);
        int currentNumArrays = localBufs.size();
        int headroom = currentNumArrays * bufsiz - currentCapacity;
        int basis = (currentNumArrays - 1) * bufsiz;
        if (markpos == 0 && marklimit > currentCapacity) {
            int newLength = ((count / bufsiz) + 1) * bufsiz;
            if (newLength > marklimit) {
                newLength = marklimit;
            }
            if (newLength > currentCapacity) {
                // Extend the buffer.
                if (newLength - currentCapacity <= headroom) {
                    // We just need to make the last byte[] longer
                    byte[] oldarray = (byte[])localBufs.get(currentNumArrays - 1);
                    byte[] newarray = new byte[newLength - basis];
                    System.arraycopy(oldarray, 0, newarray, 0, currentCapacity - basis);
                    localBufs.set(currentNumArrays -1, newarray);
                }
                else {
                    // We need to add a byte[bufsiz].
                    if (headroom > 0) {
                        // Last array is not full size, make it so.
                        byte[] oldarray = (byte[])localBufs.get(currentNumArrays - 1);
                        byte[] newarray = new byte[bufsiz];
                        System.arraycopy(oldarray, 0, newarray, 0, currentCapacity - basis);
                        localBufs.set(currentNumArrays - 1, newarray);
                        currentCapacity = currentNumArrays * bufsiz;
                    }
                    // Now add a full-size byte[].
                    localBufs.add(new byte[newLength - currentCapacity]);
                }
            }
        } else if (markpos > 0) {
            // The first part of the buffer has already been read by the
            // client, shuffle the data up to save space.
            shiftdown(localBufs, bufsiz, markpos, capacity(localBufs) - markpos);
        }
        /* Set the new position and mark position */
        pos -= markpos;
        count = markpos = 0;
        int i = pos / bufsiz;
        int j = pos % bufsiz;
        byte[] b = (byte[])localBufs.get(i);
        // Read data in to fill up the buffer where pos is located.
        int l = localIn.read(b, j, bufsiz - j);
        if (l < 0) {
            // No more data available, tell the client it's over.
            count = pos;
            return l;
        }

        // We read at least some bytes, let's try to read more.
        int bytesread = 0;
        while (l > 0) {
          bytesread += l;
          if (l < bufsiz - j) {
              // Short read, that's it for now.
              // (But it's still worth trying again later, particularly
              // if the underlying stream is a socket.)
              break;
          }
          if (++i >= localBufs.size()) {
               // All buffers full, job done.
              break;
          }
          // Read in to the next buffer, and so it goes on.
          j = 0;
          b = (byte[])localBufs.get(i);
          l = localIn.read(b, j, bufsiz - j);
        }
        count = bytesread <= 0 ? pos : pos + bytesread;
 
        return bytesread;
    }

    private void emptybuf(ArrayList localBufs)
            throws IOException {
        if (markpos == -1 || (pos - markpos >= marklimit)) {
          if (count - pos < capacity(localBufs)) {
            // The first part of the buffer has already been read by the
            // client, shuffle the data up to save space.
            shiftdown(localBufs, bufsiz, pos, count - pos);
            count -= pos;
            pos = 0;

            // Remove trailing empty byte[]s.
            // n0 is the index of the last byte[] which still contains data.
            int n0 = count / bufsiz;
            if (n0 * bufsiz < count) {
              ++n0;
            }
            // n1 is the number of byte[] in the whole buffer.
            int n1 = localBufs.size();
            for (int i = n0 + 1; i < n1; ++i) {
              bufs.remove(--n1);
            }
          }
        }
      }

    /**
     * Set a Mark position in this BufferedInputStream. The parameter
     * <code>readLimit</code> indicates how many bytes can be read before a mark
     * is invalidated. Sending reset() will reposition the Stream back to the
     * marked position provided <code>readLimit</code> has not been surpassed.
     * The underlying buffer may be increased in size to allow
     * <code>readlimit</code> number of bytes to be supported.
     * 
     * @param readlimit
     *            the number of bytes to be able to read before invalidating the
     *            mark.
     */
    public synchronized void mark(int readlimit) {
        marklimit = readlimit;
        markpos = pos;
    }

    /**
     * Answers a boolean indicating whether or not this BufferedInputStream
     * supports mark() and reset(). This implementation answers
     * <code>true</code>.
     * 
     * @return <code>true</code> for BufferedInputStreams.
     */
    public boolean markSupported() {
        return true;
    }

    /**
     * Reads a single byte from this BufferedInputStream and returns the result
     * as an int. The low-order byte is returned or -1 of the end of stream was
     * encountered. If the underlying buffer does not contain any available
     * bytes then it is filled and the first byte is returned.
     * 
     * @return the byte read or -1 if end of stream.
     * 
     * @throws IOException
     *             If the stream is already closed or another IOException
     *             occurs.
     */
    public synchronized int read() throws IOException {
        // Use local refs since buf and in may be invalidated by an
        // unsynchronized close()
        ArrayList localBufs = bufs;
        InputStream localIn = in;
        if (localBufs == null || localIn == null) {
            throw new IOException("stream is closed");
        }

        /* Are there buffered bytes available? */
        if (pos >= count && fillbuf(localIn, localBufs) == -1) {
            return -1; /* no, fill buffer */
        }
        // localBuf may have been invalidated by fillbuf
        if (localBufs != bufs) {
            localBufs = bufs;
            if (localBufs == null) {
                throw new IOException("stream is closed");
            }
        }

        /* Did filling the buffer fail with -1 (EOF)? */
        if (count - pos > 0) {
            int result = ((byte[])localBufs.get(pos / bufsiz))[pos % bufsiz] & 0xFF;
            ++pos;
            emptybuf(localBufs);
            return result;
        }
        return -1;
    }

    /**
     * Reads at most <code>length</code> bytes from this BufferedInputStream and
     * stores them in byte array <code>buffer</code> starting at offset
     * <code>offset</code>. Answer the number of bytes actually read or -1 if no
     * bytes were read and end of stream was encountered. If all the buffered
     * bytes have been used, a mark has not been set, and the requested number
     * of bytes is larger than the receiver's buffer size, this implementation
     * bypasses the buffer and simply places the results directly into
     * <code>buffer</code>.
     * 
     * @param buffer
     *            the byte array in which to store the read bytes.
     * @param offset
     *            the offset in <code>buffer</code> to store the read bytes.
     * @param length
     *            the maximum number of bytes to store in <code>buffer</code>.
     * @return the number of bytes actually read or -1 if end of stream.
     * 
     * @throws IOException
     *             If the stream is already closed or another IOException
     *             occurs.
     */
    public synchronized int read(byte[] buffer, int offset, int length)
            throws IOException {
        // Use local ref since bufs may be invalidated by an unsynchronized
        // close()
        ArrayList localBufs = bufs;
        if (localBufs == null) {
            throw new IOException("stream is closed");
        }
        // avoid int overflow
        if (offset > buffer.length - length || offset < 0 || length < 0) {
            throw new IndexOutOfBoundsException();
        }
        if (length == 0) {
            return 0;
        }
        InputStream localIn = in;
        if (localIn == null) {
            throw new IOException("stream is closed");
        }

        int required;
        if (pos < count) {
            /* There are bytes available in the buffer. */
            int copylength = count - pos >= length ? length : count - pos;
            int i = pos / bufsiz;
            int j = pos % bufsiz;
            int k = 0;
            byte[] buf_i = (byte[])localBufs.get(i);
            while (k < copylength) {
                if (bufsiz - j < copylength - k) {
                    // Fill up the current buffer and move to the next one.
                    System.arraycopy(buf_i, j, buffer, offset + k, bufsiz - j);
                    ++i;
                    buf_i = (byte[])localBufs.get(i);
                    k += bufsiz - j;
                    j = 0;
                }
                else {
                    // Copy the remaining data and we're done.
                    System.arraycopy(buf_i, j, buffer, offset + k, copylength - k);
                    k = copylength;
                }
            }
            pos += copylength;
            if (copylength == length || localIn.available() == 0) {
                emptybuf(localBufs);
                return copylength;
            }
            offset += copylength;
            required = length - copylength;
        } else {
            required = length;
        }

        while (true) {
            int read;
            /*
             * If we're not marked and the required size is greater than the
             * buffer, simply read the bytes directly bypassing the buffer.
             */
            if (markpos == -1 && required >= capacity(localBufs)) {
                read = localIn.read(buffer, offset, required);
                if (read == -1) {
                    return required == length ? -1 : length - required;
                }
            } else {
                if (fillbuf(localIn, localBufs) == -1) {
                    return required == length ? -1 : length - required;
                }

                // Check for an asynchronous close().
                if (bufs == null) {
                    throw new IOException("stream is closed");
                }

                read = count - pos >= required ? required : count - pos;
                if (read > 0) {
                    int i = pos / bufsiz;
                    int j = pos % bufsiz;
                    int k = 0;
                    byte[] buf_i = (byte[])localBufs.get(i);
                    while (k < read) {
                        if (bufsiz - j < read - k) {
                            System.arraycopy(buf_i, j, buffer, offset + k, bufsiz - j);
                            ++i;
                            j = 0;
                            buf_i = (byte[])localBufs.get(i);
                            k += bufsiz - j;
                        }
                        else {
                            System.arraycopy(buf_i, j, buffer, offset + k, read - k);
                            k = read;
                        }
                    }
                    pos += read;
                }
            }
            required -= read;
            if (required == 0) {
                emptybuf(localBufs);
                return length;
            }
            if (localIn.available() == 0) {
                emptybuf(localBufs);
                return length - required;
            }
            offset += read;
        }
    }

    /**
     * Reset this BufferedInputStream to the last marked location. If the
     * <code>readlimit</code> has been passed or no <code>mark</code> has been
     * set, throw IOException. This implementation resets the target stream.
     * 
     * @throws IOException
     *             If the stream is already closed or another IOException
     *             occurs.
     */
    public synchronized void reset() throws IOException {
        if (bufs == null) {
            throw new IOException("stream is closed");	
        }
        if (-1 == markpos) {
            throw new IOException("mark has been invalidated");
        }
        pos = markpos;
    }

    /**
     * Skips <code>amount</code> number of bytes in this BufferedInputStream.
     * Subsequent <code>read()</code>'s will not return these bytes unless
     * <code>reset()</code> is used.
     * 
     * @param amount
     *            the number of bytes to skip.
     * @return the number of bytes actually skipped.
     * 
     * @throws IOException
     *             If the stream is already closed or another IOException
     *             occurs.
     */
    public synchronized long skip(long amount) throws IOException {
        // Use local refs since buf and in may be invalidated by an
        // unsynchronized close()
        ArrayList localBufs = bufs;
        InputStream localIn = in;
        if (localBufs == null || localIn == null) {
            throw new IOException("stream is closed");
        }
        if (amount < 1) {
            return 0;
        }

        if (count - pos >= amount) {
            pos += amount;
            return amount;
        }
        long read = count - pos;
        pos = count;

        if (markpos != -1) {
            if (amount <= marklimit) {
                if (fillbuf(localIn, localBufs) == -1) {
                    return read;
                }
                if (count - pos >= amount - read) {
                    pos += amount - read;
                    return amount;
                }
                // Couldn't get all the bytes, skip what we read
                read += (count - pos);
                pos = count;
                return read;
            }
            markpos = -1;
        }
        return read + localIn.skip(amount - read);
    }
}
