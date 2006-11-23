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
** $Id: UARTControlStream.java,v 1.1.1.1 2004/07/12 14:07:48 cvs Exp $
*/

package com.acunia.device.uart;

import javax.comm.SerialPort;
import javax.comm.SerialPortEvent;

import java.io.IOException;

public class UARTControlStream {

  private native void createFromString(String path)
    throws SecurityException, IOException;

  public UARTControlStream(String name) throws IOException {
  // TODO: security check?
    createFromString(name);
  }

  public synchronized native int getbaudrate();

  public synchronized native int setbaudrate(int newrate);

  public synchronized native int getstopbits();

  public synchronized native int setstopbits(int newbits);

  public synchronized native int getdatabits();

  public synchronized native int setdatabits(int newbits);

  public synchronized native int getflowcontrol();

  public synchronized native int setflowcontrol(int newflowcon);

  public synchronized native int getparity();

  public synchronized native int setparity(int newparity);

  public synchronized native boolean getdsr();

  public synchronized native boolean getcd();

  public synchronized native boolean getcts();

  public synchronized native boolean getri();

  public synchronized native boolean setdtr(boolean newval);

  public synchronized native boolean setrts(boolean newval);

  public synchronized native void sendbreak(int millis);

  private native void waitforevent(SerialPortEvent e);

  public SerialPortEvent getevent(SerialPort port) {
    SerialPortEvent e = new SerialPortEvent(port);

    this.waitforevent(e);

    return e;
  }

  public synchronized native void close();

  /*
  ** If we are garbage-collected, make sure our run() method also terminates.
  ** (Do we need to do this?)
  */
  protected void finalize() {
    close();
  }

}
