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
** $Id: UARTControlStream.java,v 1.1.1.1 2004/07/12 14:07:48 cvs Exp $
*/

package org.open_mika.device.uart;

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
