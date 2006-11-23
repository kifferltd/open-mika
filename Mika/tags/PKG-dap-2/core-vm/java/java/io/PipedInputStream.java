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
** $Id: PipedInputStream.java,v 1.1.1.1 2004/07/12 14:07:45 cvs Exp $
*/

package java.io;

public class PipedInputStream extends InputStream {

  /** The (default) size of the pipe buffer.
  */

  protected static final int PIPE_SIZE = 1024;

  /** The buffer in which bytes are stored.
  */

  protected byte[] buffer = new byte[PIPE_SIZE];
  private byte[] onebuffer = new byte[1];

  /** The index where the next byte will be stored.
  */

  protected int in;

  /** The index where the next byte will be retrieved.
  */

  protected int out;
  
  /** The PipedOutputStream to which we are connected.
  */

  PipedOutputStream src;

  private Thread producer;
  private Thread consumer;

  int readable;
  int storable = PIPE_SIZE;

  /** When 'consumerclosed' is true, the pipe is really closed.
  */
  
  private boolean consumerclosed;
  volatile boolean producerclosed;
  
  public PipedInputStream(PipedOutputStream src) throws IOException {
    connect(src);
  }

  public PipedInputStream() {}

  public void connect(PipedOutputStream src) throws IOException {

    if (this.src == null && src.dst == null) {
      this.src = src;
      src.dst = this;
    }
    else {
      throw new IOException("allready connected");
    }
  }

  public synchronized int available() throws IOException {

    if (this.consumerclosed) {
      return 0;
    }
    else {
      return this.readable;
    }
    
  }

  public void close() throws IOException {
    this.consumerclosed = true;
  }

  public synchronized int read(byte[] buffer, int offset, int count) throws IOException {

    int result = 0;
    int chunk;
    
    if (this.consumerclosed) {
      throw new IOException("closed");
    }
    
    if (src == null) {
      throw new IOException("unconnected");
    }

    if (this.consumer == null) {
      this.consumer = Thread.currentThread();
    }

    if(count < 0 || offset < 0){
      throw new ArrayIndexOutOfBoundsException();
    }

    if(count == 0){
      return 0;
    }
    try {
      while (this.readable == 0) {
        this.wait(500);
        if (this.producer != null && ! this.producer.isAlive()) {
          throw new IOException("no producer");
        }
        else if (this.producerclosed) {
          break;
        }
      }

      while(this.readable > 0 && count > 0) {
        if (in <= out) {
          chunk = PIPE_SIZE - out;
        }
        else {
          chunk = in - out;
        }

        if (chunk > count) {
          chunk = count;
        }

        System.arraycopy(this.buffer, out, buffer, offset, chunk);
        offset += chunk;
        count -= chunk;
        this.readable -= chunk;
        this.storable += chunk;
        result += chunk;
        out += chunk;
        if (out == PIPE_SIZE) {
          out = 0;
        }
        if (out == in) {
          out = 0;
          in = -1;
        }
      }
    }
    catch (InterruptedException ie) {
      throw new InterruptedIOException();
    }

    this.notify();

    return (result == 0) ? -1 : result;
    
  }

  public synchronized int read() throws IOException {

    int result = this.read(this.onebuffer, 0, 1);

    if (result == 1) {
      return (0x000000ff & this.onebuffer[0]);
    }
    else {
      return -1;
    }

  }

  synchronized void receive(byte[] buffer, int offset, int count) throws IOException {

    int chunk;

    if (this.consumerclosed) {
      throw new IOException("closed");
    }

    if (this.producer == null) {
      this.producer = Thread.currentThread();
    }

    if (this.consumer != null && !this.consumer.isAlive()) {
      this.consumerclosed = true;
      throw new IOException("consumer dead");
    }

    while (count > 0) {
      try {
      
        /*
        ** We check wether there is space for writing bytes and if not, we notify the consumer
        ** that he should read a bit to clear up some space. After each wait pause, we see if either
        ** the pipe has not been closed or the consumer, if there is allready one, is still alive. If
        ** that is not the case, we close the pipe and throw IOException. Note that when 'storable'
        ** really is 0, we do an implied flush operation by setting 'readable' to 'storable'.
        */
        
        while (this.storable == 0) {
          this.readable = PIPE_SIZE;
          this.notify();
          this.wait(500);
          if (this.consumerclosed) {
            throw new IOException("closed");
          }
          if (this.consumer != null && ! this.consumer.isAlive()) {
            this.consumerclosed = true;
            throw new IOException("consumer dead");
          }
        }

        /*
        ** See how many bytes we can write, taking wraparound in the stream buffer
        ** into account. First see if we have an empty pipe (in < 0) and than see
        ** if the 'in' index has wrapped around allready or not, adjusting the length
        ** of the chunk we can copy in one go.
        **
        ** Note that we don't change the 'readable' field nor do we invoke a notify;
        ** when the while loop above has been taken, a notify and change of 'readable'
        ** has been issued allready, otherwise, we leave it up to the flush operation
        ** of the PipedOutputStream to issue a notify and change of 'readable'.
        */

        if (this.in < 0) {
          this.in = 0;
        }
       
        if (in < out) {
          chunk = out - in;
        }
        else {
          chunk = PIPE_SIZE - in;
        }

        if (count < chunk) {
          chunk = count;
        }
                                
        System.arraycopy(buffer, offset, this.buffer, in, chunk);
        offset += chunk;
        count -= chunk;
        this.readable += chunk;
        this.storable -= chunk;
        this.in += chunk;
        if (this.in == PIPE_SIZE) {
          this.in = 0;
        }
      }
      catch (InterruptedException ie) {
        throw new InterruptedIOException();
      }
    }
              
  }

  protected synchronized void receive(int oneByte) throws IOException {
    this.onebuffer[0] = (byte) (oneByte & 0xff);
    this.receive(this.onebuffer, 0, 1);
  }

}
