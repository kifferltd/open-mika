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
** $Id: UARTOutputStream.java,v 1.2 2006/10/04 14:24:21 cvsroot Exp $
*/

package com.acunia.device.uart;

import java.io.*;

public class UARTOutputStream extends OutputStream {

  private native void createFromString(String path)
    throws SecurityException;

  private boolean open;

  public UARTOutputStream(String name) {
  // TODO: security check?
    open = true;
    createFromString(name);
  }

  public native void write(int b) 
    throws IOException;

  private native void writeFromBuffer(byte[] b, int off, int len)
    throws IOException;

  public void write(byte[] b) throws IOException, NullPointerException {
    writeFromBuffer(b, 0, b.length);
  }

  public void write(byte[] b, int off, int len)
    throws IOException, NullPointerException, ArrayIndexOutOfBoundsException 
  {
    if(off<0 || len<0 || off > b.length - len)
      throw new ArrayIndexOutOfBoundsException();

    writeFromBuffer(b, off,len);
  }

  public native void flush() throws IOException;

  private native void close0() throws IOException;
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
