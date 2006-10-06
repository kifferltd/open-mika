/**************************************************************************
* Copyright (c) 2001 by Acunia N.V. All rights reserved.                  *
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
*                                                                         *
* Modifications copyright (c) 2005 by Chris Gray, /k/ Embedded Java       *
* Solutions. All rights reserved.                                         *
*                                                                         *
**************************************************************************/


package com.acunia.wonka.net.http;

import java.io.*;

class HttpOutputStream extends OutputStream {

  private static final byte[] CHUNKED = "Transfer-Encoding: chunked\r\n\r\n".getBytes();
  private static final byte[] NEWLINE = new byte[]{(byte)'\r',(byte)'\n'};

  /**
   ** The size of the buffer used when no Content-Length was specified.
   ** If more data than this is written to the HttpOutputStream, or
   ** if <code>flush()</code> is called, chunking is applied.
   */
  private static int bufsize;

  /**
   ** Get <var>bufsize</var> from system property <code>wonka.http.request.buffer.size</code>,
   ** with 4096 (bytes) as the default size.
   */
  static {
    bufsize = Integer.getInteger("wonka.http.request.buffer.size", 4096).intValue();
  }

  /**
   ** The OutputStream around which this HttpOutputStream is wrapped.
   */
  private OutputStream out;

  /**
   ** The number of bytes written to this HttpOutputStream and not yet
   ** passed on to <var>out</var>.
   */
  private int count;

  /**
   ** The buffer used to transfer data from this HttpOutputStream to <var>out</var>.
   */
  private byte[] buffer;

  /**
   ** True iff this HttpOutputStream has already been closed.
   */
  private boolean closed;

  /**
   ** True iff this HttpOutputStream has already been internally flushed.
   ** Any data written after this will be ignored.
   */
  private boolean flushed;

  /**
   ** True iff this HttpOutputStream is using chunked mode.
   */
  private boolean chunked;

  /**
   ** True iff we are in non-chunked mode and the content-length header has
   ** yet to be written.
   */
  private boolean contentLengthNeeded;

  /**
   ** The BasicHttpURLConnection by which this HttpOutputStream was created.
   */
  private BasicHttpURLConnection httpCon;

  /**
   ** Construct a HttpOutputStream which wraps <var>out</var>, with parent <var>httpC</var>.
   */
  HttpOutputStream(OutputStream out, BasicHttpURLConnection httpC) throws IOException {
    this.out = out;
    httpCon = httpC;
    contentLengthNeeded = httpC.requestContentLength < 0;
    buffer = new byte[contentLengthNeeded ? bufsize : httpC.requestContentLength];
  }

  /**
   ** If the connection is not already closed, flush the data to <var>out</var>
   ** and close <var>out</out>. Process the response.
   */ 
  public void close() throws IOException {
    if(!closed){
      closed = true;
      if(chunked){
        if(count > 0){
          flushBuffer(buffer,0);
        }
        out.write('0');
        out.write(NEWLINE,0,2);
        out.write(NEWLINE,0,2);
      }
      else {
        if (contentLengthNeeded) {
          out.write(("Content-Length: "+count+"\r\n").getBytes());
          contentLengthNeeded = false;
        }
        out.write(NEWLINE,0,2);
        out.write(buffer,0,count);
        buffer = null;
      }
      out.flush();
    }
  }

  /**
   ** Variant of flush() used by BasicHttpURLConnection.
   ** If operating in chunked mode, flush the current contents of <var>buffer</var> to <var>out</var>.
   ** If not operating in chunked mode and the data indicated by the
   ** request content-length is available, send the data.
   ** Otherwise do nothing. 
   */
  void flush_internal() throws IOException {
    if(closed){
      throw new IOException("Stream is closed");
    }
    if (count > 0){
      if(chunked) {
        flushBuffer(buffer, 0);
      }
      else if (count >= httpCon.requestContentLength) {
        if (contentLengthNeeded) {
          out.write(("Content-Length: "+httpCon.requestContentLength+"\r\n").getBytes());
          contentLengthNeeded = false;
        }
        out.write(NEWLINE,0,2);
        out.write(buffer,0,httpCon.requestContentLength);
        count = 0;
        out.flush();
      }
    }
  }

  /**
   ** Flush the current contents of <var>buffer</var> to <var>out</var>,
   ** forcing chunked mode.
   */
  public void flush() throws IOException {
    if(closed){
      throw new IOException("Stream is closed");
    }
    if (flushed) {
      // silently discard
    }
    if (count > 0){
      flushBuffer(buffer, 0);
    }
  }

  /**
   ** Add one byte to the buffer. If the buffer is already full, flush it first
   ** (thereby forcing "chunked" mode).
   */
  public void write(int b) throws IOException {
    if(closed){
      throw new IOException("Stream is closed");
    }
    if (flushed) {
      // silently discard
    }
    if(count == buffer.length){
      flushBuffer(buffer, 0);
    }
    buffer[count++] = (byte)b;
  }

  /**
   ** Add bytes to the buffer, if there is room. Otherwise first flush the 
   ** buffer to <var>out</var> (thereby forcing "chunked" mode), and then
   ** write the new chars directly to <var>out</var> as a separate chunk.
   */
  public void write(byte[] bytes, int off, int length) throws IOException {
    if(closed){
      throw new IOException("Stream is closed");
    }
    if (flushed) {
      // silently discard
    }
    if(length > (buffer.length - count)){
      if(count > 0){
        flushBuffer(buffer, 0);
      }
      if(length > buffer.length){
        count = length;
        flushBuffer(bytes, off);
        return;
      }
    }
    System.arraycopy(bytes,off,buffer,count,length);
    count += length;
  }

  /**
   ** Flush the given buffer (which may be our internal <var>buffer</var>
   ** or an external one) to <var>out</var>. Enables "chunked" mode if not
   ** already enabled.
   */
  private void flushBuffer(byte[] buf, int off) throws IOException {
    if(!chunked){
      chunked = true;
      contentLengthNeeded = false;
      out.write(CHUNKED,0,CHUNKED.length);
    }
    out.write((Integer.toHexString(count)+"\r\n").getBytes());
    out.write(buf,off, count);
    count = 0;
    out.write(NEWLINE,0,2);
    out.flush();
  }
}
