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
** $Id: SerialPort.java,v 1.1.1.1 2004/07/12 14:07:48 cvs Exp $
*/

package javax.comm;

import java.util.TooManyListenersException;

public abstract class SerialPort extends CommPort {

  public static final int DATABITS_5 = 5;
  public static final int DATABITS_6 = 6;
  public static final int DATABITS_7 = 7;
  public static final int DATABITS_8 = 8;

  public static final int STOPBITS_1 = 1;
  public static final int STOPBITS_2 = 2;
  public static final int STOPBITS_1_5 = 3;

  public static final int PARITY_NONE = 0;
  public static final int PARITY_ODD  = 1;
  public static final int PARITY_EVEN = 2;
  public static final int PARITY_MARK = 3;
  public static final int PARITY_SPACE = 4;

  public static final int FLOWCONTROL_NONE = 0;
  public static final int FLOWCONTROL_RTSCTS_IN = 1;
  public static final int FLOWCONTROL_RTSCTS_OUT = 2;
  public static final int FLOWCONTROL_XONXOFF_IN = 4;
  public static final int FLOWCONTROL_XONXOFF_OUT = 8;

  public SerialPort() {
  }

  public abstract int getBaudRate();

  public abstract int getDataBits();

  public abstract int getStopBits();

  public abstract int getParity();

  public abstract void sendBreak(int millis);

  public abstract void setFlowControlMode (int flowcontrol)
    throws UnsupportedCommOperationException;

  public abstract int getFlowControlMode();

  public abstract void setSerialPortParams (int baudrate, int dataBits, int stopBits, int parity)
    throws UnsupportedCommOperationException;

  public abstract void setDTR(boolean dtr);

  public abstract boolean isDTR();

  public abstract void setRTS(boolean rts);

  public abstract boolean isRTS();

  public abstract boolean isCTS();

  public abstract boolean isDSR();

  public abstract boolean isRI();

  public abstract boolean isCD();

  public abstract void addEventListener(SerialPortEventListener lsnr)
    throws TooManyListenersException;

  public abstract void removeEventListener(); // automatic on port close()

  public abstract void notifyOnDataAvailable(boolean enable);
  // will get DATA_AVAILABLE when buffer goes from empty to non-empty

  public abstract void notifyOnOutputEmpty(boolean enable);
  // will get OUTPUT_BUFFER_EMPTY when buffer goes from non-empty to empty

  public abstract void notifyOnCTS(boolean enable);

  public abstract void notifyOnDSR(boolean enable);

  public abstract void notifyOnRingIndicator(boolean enable);

  public abstract void notifyOnCarrierDetect(boolean enable);

  public abstract void notifyOnOverrunError(boolean enable);

  public abstract void notifyOnParityError(boolean enable);

  public abstract void notifyOnFramingError(boolean enable);

   public abstract void notifyOnBreakInterrupt(boolean enable);

}
 

  
