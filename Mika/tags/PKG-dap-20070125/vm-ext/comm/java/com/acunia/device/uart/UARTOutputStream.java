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
