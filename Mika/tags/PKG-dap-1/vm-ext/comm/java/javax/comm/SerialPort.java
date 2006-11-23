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
 

  
