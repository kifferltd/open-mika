/**************************************************************************
* Copyright (c) 2001 by Punch Telematix. All rights reserved.             *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of Punch Telematix nor the names of                 *
*    other contributors may be used to endorse or promote products        *
*    derived from this software without specific prior written permission.*
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL PUNCH TELEMATIX OR OTHER CONTRIBUTORS BE LIABLE       *
* FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR            *
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF    *
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR         *
* BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,   *
* WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE    *
* OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN  *
* IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                           *
**************************************************************************/


/*
** $Id: CommPort.java,v 1.1.1.1 2004/07/12 14:07:48 cvs Exp $
*/

package javax.comm;

import java.io.*;

public abstract class CommPort {

  protected String name;
  protected boolean closed;

  CommPortIdentifier commPortIdentifier;

  public String getName() {
    return name;
  }

  public String toString() {
    return name;
  }

  // getInputStream returns an InputStream (or null if the device
  // is write-only).  Only way for an application to read from the port.
 
  public abstract InputStream getInputStream() throws IOException;

  // ditto but for output.

  public abstract OutputStream getOutputStream() throws IOException;

  public synchronized void close () {
    commPortIdentifier.handle_close();
    try{
      getInputStream().close();
    } catch (IOException e) {}
    try{
      getOutputStream().close();
    } catch (IOException e) {}

    // [CG 20010201] Moved here from just before the two try{} clauses
    // above, as getXXputStream throw an IllegalStateException if the 
    // CommPort is already closed ...
    closed = true;
  }

  // if implemented, tells driver to block until thresh bytes in buffer
  // (or buffer full, or timeout if enabled).
  public abstract void enableReceiveThreshold(int thresh) 
    throws UnsupportedCommOperationException;

  public abstract void disableReceiveThreshold();

  public abstract boolean isReceiveThresholdEnabled();

  public abstract int getReceiveThreshold();

  // if implemented, tells driver to block until rcvTimeout ms elapse
  // (or buffer full, or threshold if enabled).
  public abstract void enableReceiveTimeout(int rcvTimeout) 
    throws UnsupportedCommOperationException;

  public abstract void disableReceiveTimeout();

  public abstract boolean isReceiveTimeoutEnabled();

  public abstract int getReceiveTimeout();

  // if implemented, tells driver to block until framingByte received
  // (or buffer full/thresholdl, or timeout if enabled).
  public abstract void enableReceiveFraming(int framingByte) 
    throws UnsupportedCommOperationException;

  public abstract void disableReceiveFraming();

  public abstract boolean isReceiveFramingEnabled();

  public abstract int getReceiveFramingByte();

  public abstract void setInputBufferSize(int size); // advisory only

  public abstract int getInputBufferSize(); // advisory only

  public abstract void setOutputBufferSize(int size); // advisory only

  public abstract int getOutputBufferSize(); // advisory only

 }
 

