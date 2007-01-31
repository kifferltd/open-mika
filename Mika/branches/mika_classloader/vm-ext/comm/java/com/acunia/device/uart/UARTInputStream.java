/**************************************************************************
* Copyright  (c) 2001 by Acunia N.V. All rights reserved.                 *
*                                                                         *
* This software is copyrighted by and is the sole property of Acunia N.V. *
* and its licensors, if any. All rights, title, ownership, or other       *
* interests in the software remain the property of Acunia N.V. and its    *
* licensors, if any.                                                      *
*                                                                         *
* This software may only be used in accordance with the corresponding     *
* license agreement. Any unauthorized use, duplication, transmission,     *
*  distribution or disclosure of this software is expressly forbidden.    *
*                                                                         *
* This Copyright notice may not be removed or modified without prior      *
* written consent of Acunia N.V.                                          *
*                                                                         *
* Acunia N.V. reserves the right to modify this software without notice.  *
*                                                                         *
*   Acunia N.V.                                                           *
*   Vanden Tymplestraat 35      info@acunia.com                           *
*   3000 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
**************************************************************************/


/*
** $Id: UARTInputStream.java,v 1.3 2006/10/04 14:24:21 cvsroot Exp $
*/

package com.acunia.device.uart;

import wonka.vm.Etc;
import java.io.*;

/**
 * UARTInputStream implements InputStream for a UART of the SM platform.
 * It supports the setting of thresholds and timeouts for reading data.
 */
public class UARTInputStream extends InputStream implements Runnable {

  /**
   ** The currently applicable Threshold and Timeout values, as supplied
   ** by the user of the InputStream.  Note that an rxThreshold of zero
   ** really means one (read returns as soon as *some* data is available),
   ** and an rxTimeout of zero really means infinity. We call the effective
   ** Threshold (the minimum number of bytes which must be available before
   ** read will return), "desired".
   */
  private int rxThreshold = 0;
  private int rxTimeout = 0;
  private int desired;

  /**
   ** micronap is the pause built into the spinlock when we
   ** are waiting for some data to become available.  meganap
   ** is used in the monitor thread where theoretically we
   ** should execute an open-ended wait(), but we haven't the
   ** balls.
   */
  private static final long micronap = 100; // milliseconds
  private static final long meganap = 1000; // milliseconds

  /**
   ** microbuffer is a one-byte buffer used to implement read() 
   ** as a special case of read(array,offset,length).
   */
  private byte [] microbuffer = new byte[1];

  /**
   ** uartMonitor is the thread which monitors the number of input
   ** bytes available and wakes up the read method as required.
   ** It is started when a non-zero timeout or threashold is specified,
   ** and terminates when the InputStream is closed ("open" becomes false).
   */
  private Thread uartMonitor = null;

  /**
   ** "open" is set true when the InputStream is created, and false when
   ** close() is called.
   */
  private boolean open;

  /**
   ** The name of the underlying stream.
   */
  private String name;

  /**
   ** createFromString initialises this InputStream object using a
   ** lookup based on "portname".
   */
  private native void createFromString(String portname)
    throws SecurityException;

  /**
   ** Constructor UARTInputStream(String) calls createFromString
   ** and starts up the monitor thread.
   */
  public UARTInputStream(String portname) {
    // TODO: security check?

    createFromString(portname);
    name = portname;
    open = true;
  }
  
  /**
   ** Method readIntoBuffer performs the "guts" of a read,
   ** shifting bytes from the i/o driver into the buffer.
   */
  private native int readIntoBuffer(byte[] b, int off, int len)
    throws IOException;
  
  /** Method read() (single-character read) is implemented as a
   ** special case of read(array,offset,length).  This could be
   ** done more efficiently: feel free if you have the time.
   */
  public synchronized int read()
    throws IOException
  {
    int readlen = read(microbuffer,0,1);

    if (readlen>0) {

      return microbuffer[0] & 0xff;

    }
    else return readlen;

  }
  
  /**
   ** Meethod read(array) is a connvenience method, a shorthand
   ** for read(array, 0, b.length).
   */
  public synchronized int read(byte[] b)
    throws IOException, NullPointerException 
  {
    return read(b, 0, b.length);
  }
  
  /**
   ** OK, now for the real business.
   ** Method read(byte[] b, int off, int len) first (after sanity checks)
   ** determines the minimum number of characters to be read ("desired")
   ** and then wakes up the monitor thread.  If a Timeout was specified
   ** we then wait to be notified by the monitor thread, until at least
   ** "desired" characters are available.  If no Timeout was specified
   ** then we just spinlock (using micronap) until at least "desired" 
   ** characters are available.  In both cases the wait loop is aborted
   ** if an interrupt is received.
   ** Finally we either copy the lesser of (available(),length) bytes
   ** into the buffer and return the number of bytes copied, or (if no
   ** data is available, i.e. we timed out or were interrupted) return -1.
   */
  public synchronized int read(byte[] b, int off, int len)
    throws IOException, NullPointerException, ArrayIndexOutOfBoundsException 
  {
    long deadline;  // absolute time when timeout will expire
    long remaining; // elapsed time from now until deadline

    if(b==null) throw new NullPointerException();

    if(off<0 || len<0 || off > b.length - len) {
      throw new ArrayIndexOutOfBoundsException();
    }

    if (rxThreshold>0) {
      desired = rxThreshold;
    } else desired = 1;

    if (rxTimeout>0) {
      deadline = System.currentTimeMillis()+rxTimeout;
      remaining = rxTimeout;
    }
    else {
      deadline = Long.MAX_VALUE;
      remaining = Long.MAX_VALUE;
    }

    this.notify();

    try {
        while (remaining>0 && available()<desired) {
      if (rxTimeout>0) {
            this.wait(remaining);
            remaining = deadline - System.currentTimeMillis();
      }
      else { // no rxTimeout
            this.wait(micronap);
      }
        }
    } catch (InterruptedException e) {}

    int bytesToRead = available();

    desired = 0;

    if (bytesToRead==0) {

      return -1;

    }
/*
    else if (bytesToRead>b.length) {

      return readIntoBuffer(b, off, b.length);

    }
    else return readIntoBuffer(b, off, bytesToRead);
*/
    else if (bytesToRead>len) {

	return readIntoBuffer(b, off, len);

    }
    else return readIntoBuffer(b, off, bytesToRead);

  }
  
  /**
   ** Method skip0 removes and discards exctly n characters from the
   ** input stream (unless an IOException happens first).
   */
  public native long skip0(long n)
    throws IOException;

  /**
   ** Method skip calls skip0 to skip either n characters or available()
   ** characters, whichever is the less.
   */
  public long skip(long n)
    throws IOException
  {
    int bytesToRead = available();

    if (bytesToRead==0) {

      return 0;

    }
    else if (bytesToRead>n) {

      return skip0(n);

    }
    else return skip0(bytesToRead);

  }

  /**
   ** Method available() returns the maximum number of bytes which could
   ** now be read from the input stream without blocking.
   */
  public native synchronized int available()
    throws IOException;
  

  private native void close0();
  /**
   ** Method close() sets "open" false.
   */
  public synchronized void close()
    throws IOException
  {
    if(open){
      open = false;
      close0();
    }
  }
  
  
  /**
   ** Method setRxThreshold(t) adjusts the value of rxThreshold and
   ** then gives the monitor a "kick".
   */
  public synchronized void setRxThreshold(int t) {
    if (t==0) {
      clearRxThreshold();
    }
    else {
      if (uartMonitor == null) {
        Etc.woempa(9,"setRxThreshold: starting a monitor thread for "+name);
        uartMonitor = new Thread(this,name+"-monitor");
        uartMonitor.start();
      }
      rxThreshold = t;
      this.notify();
    }
  }

  /**
   ** Method clearRxThreshold(t) sets rxThreshold to zero (and hence
   ** desired to 1) and then gives the monitor a "kick".
   */
  public void clearRxThreshold() {
    rxThreshold = 0;
    desired = 1;
    this.notify();
  }

  /**
   ** Method setRxTimeout(t) adjusts the value of rxTimeout and
   ** then gives the monitor a "kick".
   */
  public synchronized void setRxTimeout(int t) {
    if (t==0) {
      clearRxTimeout();
    }
    else {
      if (uartMonitor == null) {
        Etc.woempa(9,"setRxTimeout: starting a monitor thread for "+name);
        uartMonitor = new Thread(this,name+"-monitor");
        uartMonitor.start();
      }
      rxTimeout = t;
      this.notify();
    }
  }

  /**
   ** Method clearRxTimeout(t) sets rxTimeout to zero and
   ** then gives the monitor a "kick".
   */
  public void clearRxTimeout() {
    rxTimeout = 0;
    this.notify();
  }

  /**
   ** Method run() implements the monitor thread.  This sits in an endless
   ** loop until the InputStream is closed:
   **   - wait until at least "desired" bytes are available;
   **   - notify the reading thread;
   **   - wait to be notified in return (e.g. because of a new read).
   ** The last wait should really be open-ended, but that would be too
   ** scary.
   */
  public synchronized void run() {
    while (open) {
      try {
        while (available()<desired) {
          this.wait(micronap);
        }
        this.notify();
        this.wait(meganap);
      } catch (InterruptedException e) {
      } catch (IOException e) {
      }
    }
  }

  /*
  ** If we are garbage-collected, make sure our run() method also terminates.
  ** (Do we need to do this?)
  */
  protected void finalize() {
    try {
      close();
    }
    catch(IOException e) {
    }
  }

}
