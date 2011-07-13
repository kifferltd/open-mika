/**************************************************************************
* Parts copyright (c) 2001 by Punch Telematix. All rights reserved.       *
* Parts copyright (c) 2005, 2011 by Chris Gray, /k/ Embedded Java         *
* Solutions.  All rights reserved.                                        *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of Punch Telematix or of /k/ Embedded Java Solutions*
*    nor the names of other contributors may be used to endorse or promote*
*    products derived from this software without specific prior written   *
*    permission.                                                          *
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL PUNCH TELEMATIX, /K/ EMBEDDED JAVA SOLUTIONS OR OTHER *
* CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,   *
* EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,     *
* PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR      *
* PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF  *
* LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING    *
* NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS      *
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.            *
**************************************************************************/

package wonka.net.http;

import java.io.IOException;
import java.io.OutputStream;

class HttpOutputStream extends OutputStream {

  private static final byte[] CHUNKED = "Transfer-Encoding: chunked\r\n\r\n".getBytes();
  private static final byte[] NEWLINE = new byte[]{(byte)'\r',(byte)'\n'};

  /**
   ** The default size of the buffer used.  Chunking is applied if the content
   ** length is unknown and more data than this is written to the
   ** HttpOutputStream, or if <code>flush()</code> is called.
   */
  private static int default_bufsize;

  /**
   ** Get <var>default_bufsize</var> from system property 
   ** <code>wonka.http.request.buffer.size</code>; default is 4096 bytes.
   */
  static {
    default_bufsize = Integer.getInteger("wonka.http.request.buffer.size", 4096).intValue();
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
   ** True iff this HttpOutputStream is using chunked mode.
   */
  private boolean chunked;

  private int contentLength;

  private boolean contentLengthSent;

  /**
   ** Construct a HttpOutputStream which wraps <var>out</var>.
   */
  HttpOutputStream(OutputStream out, int contentLength) throws IOException {
    this.out = out;
    if (contentLength < 0) {
      buffer = new byte[default_bufsize];
    }
    else {
      contentLengthSent = true;
    }
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
        finishHeadersAndFlushBuffer();
      }
      out.flush();
    }
  }

  private void finishHeadersAndFlushBuffer() throws IOException {
    if (!contentLengthSent) {
      out.write(("Content-Length: "+count+"\r\n").getBytes());
      contentLengthSent = true;
    }
    out.write(NEWLINE,0,2);
    if (buffer != null) {
      out.write(buffer,0,count);
      count = 0;
      buffer = null;
    }
  }


  /**
   ** Variant of flush() used by BasicHttpURLConnection.
   ** If operating in chunked mode, flush the current contents of <var>buffer</var> to <var>out</var>.
   ** If not operating in chunked mode and data is available, send the data.
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
      else {
        finishHeadersAndFlushBuffer();
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

    if (buffer == null) {
      out.write(b);

      return;
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

    if (buffer == null) {
      out.write(bytes, off, length);

      return;
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
      out.write(CHUNKED,0,CHUNKED.length);
    }
    out.write((Integer.toHexString(count)+"\r\n").getBytes());
    out.write(buf,off, count);
    count = 0;
    out.write(NEWLINE,0,2);
    out.flush();
  }
}
